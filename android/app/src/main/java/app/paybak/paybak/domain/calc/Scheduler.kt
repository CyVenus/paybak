package app.paybak.paybak.domain.calc

import app.paybak.paybak.domain.format.Dates
import app.paybak.paybak.domain.model.Draft
import app.paybak.paybak.domain.model.Expense
import app.paybak.paybak.domain.model.HistoryEntry
import app.paybak.paybak.domain.model.HistoryKind
import app.paybak.paybak.domain.model.InboxItem
import app.paybak.paybak.domain.model.InboxParams
import app.paybak.paybak.domain.model.InboxType
import app.paybak.paybak.domain.model.Ledger
import app.paybak.paybak.domain.model.ME
import app.paybak.paybak.domain.model.Payer
import app.paybak.paybak.domain.model.ProjectStatus
import app.paybak.paybak.domain.model.Reminder
import app.paybak.paybak.domain.model.ReminderSchedule
import app.paybak.paybak.domain.model.Scheduler
import app.paybak.paybak.domain.model.Split
import app.paybak.paybak.domain.model.SplitMode
import app.paybak.paybak.domain.model.SplitRow
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * `tick(now)` (domain.md §10): walks each day from the scheduler cursor to [until] and runs the
 * recurring, overdue, month-end summary and reminder jobs at their local times, then purges
 * expenses deleted 30 days ago and archives settled closed projects. Ids are deterministic and past
 * moments never re-run, so it is idempotent.
 */
object LedgerScheduler {
    private val dayStamp = DateTimeFormatter.ofPattern("yyyyMMdd")
    private val monthStamp = DateTimeFormatter.ofPattern("yyyyMM")

    fun tick(ledger: Ledger, until: Instant, defaultCurrency: String, zone: ZoneId): Ledger {
        val cursor = ledger.scheduler.cursor ?: return ledger.copy(scheduler = Scheduler(until))
        if (until <= cursor) return ledger
        var current = ledger
        val reminderTime = LocalTime.parse(ledger.settings.reminderSchedule.time)
        val jobs: List<Pair<LocalTime, (Ledger, Instant, LedgerView) -> Ledger>> =
            listOf(
                LocalTime.of(9, 0) to ::recurringJob,
                LocalTime.of(9, 0) to ::overdueJob,
                LocalTime.of(20, 0) to ::summaryJob,
                reminderTime to ::reminderJob,
            )
        var day = cursor.atZone(zone).toLocalDate()
        val lastDay = until.atZone(zone).toLocalDate()
        while (!day.isAfter(lastDay)) {
            for ((time, job) in jobs) {
                val moment = day.atTime(time).atZone(zone).toInstant()
                if (cursor < moment && moment <= until) {
                    current =
                        job(current, moment, LedgerView(current, defaultCurrency, until, zone))
                }
            }
            day = day.plusDays(1)
        }
        current = purge(current, until, zone)
        current = archiveSettledProjects(current, until, defaultCurrency, zone)
        return current.copy(scheduler = Scheduler(until))
    }

    /** Whether a reminder fires on [day] for a debt due on [due]. */
    fun firesOn(due: LocalDate, day: LocalDate, schedule: ReminderSchedule): Boolean {
        if (schedule.twoDaysBefore && day == due.minusDays(2)) return true
        if (schedule.onDueDate && day == due) return true
        return schedule.overdueEvery3Days &&
            day.isAfter(due) &&
            java.time.temporal.ChronoUnit.DAYS.between(due, day) % 3 == 0L
    }

    private fun recurringJob(ledger: Ledger, moment: Instant, view: LedgerView): Ledger {
        val day = view.localDate(moment)
        var current = ledger
        for (rule in ledger.recurringRules) {
            if (!rule.active) continue
            val after = rule.lastOccurrence ?: rule.startDate.minusDays(1)
            val occurrence = nextOccurrence(rule, after)
            if (occurrence != day) continue
            current =
                current.copy(
                    recurringRules =
                        current.recurringRules.map {
                            if (it.id == rule.id) it.copy(lastOccurrence = occurrence) else it
                        }
                )
            if (rule.variable || rule.amount == null) {
                val id = "d-${rule.id}-$occurrence"
                if (current.draft(id) == null) {
                    current =
                        current.copy(
                            drafts = current.drafts + Draft(id, rule.id, occurrence, moment)
                        )
                }
            } else {
                val id = "e-${rule.id}-$occurrence"
                if (current.expense(id) != null) continue
                val key = rule.groupId ?: rule.split.personIds.sorted().joinToString("+")
                val split =
                    Splits.equal(rule.amount, rule.split.personIds, current.rotation[key] ?: 0)
                val expense =
                    Expense(
                        id = id,
                        groupId = rule.groupId,
                        title = rule.title,
                        category = rule.category,
                        amount = rule.amount,
                        currency = rule.currency,
                        date = occurrence,
                        payers = listOf(Payer(rule.payerId, rule.amount)),
                        split =
                            Split(
                                SplitMode.Equal,
                                rule.split.personIds.map {
                                    SplitRow(it, share = split.shares.getValue(it))
                                },
                            ),
                        recurringRuleId = rule.id,
                        occurrenceDate = occurrence,
                        createdAt = moment,
                        createdBy = rule.createdBy,
                        history = listOf(HistoryEntry(HistoryKind.Created, moment, rule.createdBy)),
                    )
                current =
                    current.copy(
                        expenses = current.expenses + expense,
                        rotation = current.rotation + (key to split.counter),
                    )
            }
        }
        return current
    }

    private fun overdueJob(ledger: Ledger, moment: Instant, view: LedgerView): Ledger {
        val yesterday = view.localDate(moment).minusDays(1)
        val items =
            view
                .openItems(moment)
                .filter { it.owedToMe && it.due == yesterday }
                .map { item ->
                    InboxItem(
                        id = "n-overdue-${item.ref}-${dayStamp.format(view.localDate(moment))}",
                        type = InboxType.PaymentOverdue,
                        createdAt = moment,
                        params =
                            InboxParams(
                                personId = item.friendId,
                                amount = item.amount,
                                currency = view.defaultCurrency,
                                title = item.title,
                                expenseId = item.ref.takeIf { item.kind == ObligationKind.Direct },
                                dueDate = item.due,
                            ),
                    )
                }
        return ledger.withInbox(items)
    }

    private fun summaryJob(ledger: Ledger, moment: Instant, view: LedgerView): Ledger {
        val day = view.localDate(moment)
        if (day != Dates.lastDayOfMonth(day)) return ledger
        val report = view.monthTotals(YearMonth.from(day), moment)
        val totals = view.homeTotals(moment)
        val item =
            InboxItem(
                id = "n-summary-${monthStamp.format(day)}",
                type = InboxType.MonthlySummary,
                createdAt = moment,
                params =
                    InboxParams(
                        year = day.year,
                        month = day.monthValue,
                        spent = report.total,
                        owed = totals.owed,
                        owe = totals.owe,
                    ),
            )
        return ledger.withInbox(listOf(item))
    }

    private fun reminderJob(ledger: Ledger, moment: Instant, view: LedgerView): Ledger {
        val schedule = ledger.settings.reminderSchedule
        val day = view.localDate(moment)
        val stamp = dayStamp.format(day)
        val reminders = mutableListOf<Reminder>()
        val inbox = mutableListOf<InboxItem>()
        for (item in view.openItems(moment)) {
            val due = item.due ?: continue
            if (!firesOn(due, day, schedule)) continue
            if (item.owedToMe) {
                if (view.person(item.friendId)?.remindersMuted == true) continue
                reminders +=
                    Reminder(
                        id = "rem-${item.ref}-$stamp",
                        toId = item.friendId,
                        fromId = ME,
                        amount = item.amount,
                        currency = view.defaultCurrency,
                        expenseId = item.ref.takeIf { item.kind == ObligationKind.Direct },
                        groupId = item.ref.takeIf { item.kind == ObligationKind.Group },
                        loanId = item.ref.takeIf { item.kind == ObligationKind.Loan },
                        installment = item.installment,
                        sentAt = moment,
                        automatic = true,
                    )
            } else {
                inbox +=
                    InboxItem(
                        id = "n-reminder-${item.ref}-$stamp",
                        type = InboxType.PaymentReminder,
                        createdAt = moment,
                        params =
                            InboxParams(
                                personId = item.friendId,
                                amount = item.amount,
                                currency = view.defaultCurrency,
                                title = item.title,
                                dueDate = due,
                                groupId = item.ref.takeIf { item.kind == ObligationKind.Group },
                            ),
                    )
            }
        }
        val known = ledger.reminders.map { it.id }.toSet()
        return ledger
            .copy(
                reminders =
                    ledger.reminders + reminders.filter { it.id !in known }.distinctBy { it.id }
            )
            .withInbox(inbox)
    }

    /** Recently deleted keeps an expense for 30 days. */
    private fun purge(ledger: Ledger, until: Instant, zone: ZoneId): Ledger {
        val expired =
            ledger.expenses.filter {
                it.deletedAt != null &&
                    it.deletedAt.atZone(zone).plusDays(RETENTION_DAYS).toInstant() <= until
            }
        return if (expired.isEmpty()) ledger
        else ledger.copy(expenses = ledger.expenses - expired.toSet())
    }

    /** A closed project archives once everyone in it is square. */
    private fun archiveSettledProjects(
        ledger: Ledger,
        until: Instant,
        defaultCurrency: String,
        zone: ZoneId,
    ): Ledger {
        val view = LedgerView(ledger, defaultCurrency, until, zone)
        val settled =
            ledger.groups
                .filter { it.project?.status == ProjectStatus.Closed }
                .filter { group -> view.groupNets(group.id, until).values.all { it == 0L } }
                .map { it.id }
                .toSet()
        if (settled.isEmpty()) return ledger
        return ledger.copy(
            groups =
                ledger.groups.map { group ->
                    if (group.id in settled) {
                        group.copy(
                            project =
                                group.project!!.copy(
                                    status = ProjectStatus.Archived,
                                    archivedAt = until,
                                )
                        )
                    } else {
                        group
                    }
                }
        )
    }

    private fun Ledger.withInbox(items: List<InboxItem>): Ledger {
        if (items.isEmpty()) return this
        val known = inbox.map { it.id }.toSet()
        return copy(inbox = inbox + items.filter { it.id !in known }.distinctBy { it.id })
    }
}
