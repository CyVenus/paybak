package app.paybak.paybak.domain.calc

import app.paybak.paybak.domain.format.Dates
import app.paybak.paybak.domain.format.Money
import app.paybak.paybak.domain.model.HistoryKind
import app.paybak.paybak.domain.model.ME
import app.paybak.paybak.domain.model.Payment
import app.paybak.paybak.domain.model.PaymentStatus
import java.time.Instant
import java.time.LocalDate

enum class TimelineKind {
    ExpenseAdded,
    ExpenseEdited,
    Payment,
    ReminderSent,
    DraftCreated,
    LoanAdded,
}

/**
 * One derived timeline event (domain.md §6.6). [amount] is unsigned (null: no amount column);
 * [primary] = black amount (you paid / money to you). [ref] is the expense, payment, reminder,
 * draft or loan id. [category] / [personId] pick the leading icon or avatar.
 */
data class TimelineEvent(
    val at: Instant,
    val kind: TimelineKind,
    val title: String,
    val subtitle: String,
    val amount: String?,
    val primary: Boolean = false,
    val home: Boolean = false,
    val method: String? = null,
    val badge: String? = null,
    val ref: String,
    val category: String? = null,
    val personId: String? = null,
    val groupId: String? = null,
)

data class TimelineDay(val date: LocalDate, val header: String, val events: List<TimelineEvent>)

/** Events for you, newest first by moment (§6.6). Comments and group creation aren't events. */
fun LedgerView.timeline(): List<TimelineEvent> {
    val events = mutableListOf<TimelineEvent>()
    for (expense in ledger.expenses) {
        val group = groupOf(expense)
        val involved =
            expense.split.rows.any { it.personId == ME } || (group != null && ME in group.memberIds)
        if (!involved) continue
        val payer = expense.payerId
        val total = Money.format(expense.amount, expense.currency)
        if (expense.deletedAt == null) {
            val subtitle =
                if (payer == ME) {
                    if (group == null) "You paid · ${expense.split.rows.size} people"
                    else "${group.name} · You paid"
                } else {
                    val word = if (iOwePayer(expense)) "You owe" else "Your share"
                    "${group?.name ?: first(payer)} · $word ${Money.format(myShare(expense), expense.currency)}"
                }
            val actor = expense.createdBy
            events +=
                TimelineEvent(
                    at = expense.createdAt,
                    kind = TimelineKind.ExpenseAdded,
                    title =
                        if (actor == ME) "You added ${expense.title}"
                        else "${first(actor)} added ${expense.title}",
                    subtitle = subtitle,
                    amount = total,
                    primary = payer == ME,
                    home = true,
                    ref = expense.id,
                    category = expense.category,
                    groupId = expense.groupId,
                )
        }
        for (entry in expense.history) {
            if (entry.kind != HistoryKind.AmountChanged) continue
            events +=
                TimelineEvent(
                    at = entry.at,
                    kind = TimelineKind.ExpenseEdited,
                    title = "${first(entry.by)} changed ${expense.title}",
                    subtitle =
                        "${group?.name.orEmpty()} · Was ${Money.format(entry.oldAmount ?: 0, expense.currency)}",
                    amount = total,
                    primary = payer == ME,
                    ref = expense.id,
                    category = expense.category,
                    groupId = expense.groupId,
                )
        }
    }
    for (payment in ledger.payments) {
        if (ME != payment.fromId && ME != payment.toId) continue
        if (payment.status != PaymentStatus.Confirmed || payment.confirmedAt == null) continue
        val other = if (payment.fromId == ME) payment.toId else payment.fromId
        events +=
            TimelineEvent(
                at = payment.confirmedAt,
                kind = TimelineKind.Payment,
                title =
                    if (payment.toId == ME) "${first(other)} paid you"
                    else "You paid ${first(other)}",
                subtitle = "${paymentFor(payment)} · ${payment.method.label} · Confirmed",
                amount = Money.format(payment.amount, payment.currency),
                primary = payment.toId == ME,
                home = true,
                method = payment.method.label,
                ref = payment.id,
                personId = other,
                groupId = payment.groupId,
            )
    }
    for (reminder in ledger.reminders) {
        if (reminder.fromId != ME) continue
        val what =
            reminder.expenseId?.let { expense(it)?.title }
                ?: reminder.groupId?.let { group(it)?.name }
                ?: reminder.loanId?.let { loan(it)?.title }
                ?: ""
        val how = if (reminder.automatic) "Sent automatically" else "Sent by you"
        events +=
            TimelineEvent(
                at = reminder.sentAt,
                kind = TimelineKind.ReminderSent,
                title = "Reminder sent to ${first(reminder.toId)}",
                subtitle = "$what · ${Money.format(reminder.amount, reminder.currency)} · $how",
                amount = null,
                ref = reminder.id,
                personId = reminder.toId,
            )
    }
    for (draft in ledger.drafts) {
        val rule = ledger.rule(draft.ruleId) ?: continue
        events +=
            TimelineEvent(
                at = draft.createdAt,
                kind = TimelineKind.DraftCreated,
                title = "${rule.title} draft created",
                subtitle = "${rule.groupId?.let { group(it)?.name }.orEmpty()} · Needs an amount",
                amount = null,
                badge = "Draft",
                ref = draft.id,
                category = rule.category,
                groupId = rule.groupId,
            )
    }
    for (loan in ledger.loans) {
        val other = loan.friendId
        events +=
            TimelineEvent(
                at = loan.createdAt,
                kind = TimelineKind.LoanAdded,
                title =
                    if (loan.lenderId == ME) "You lent ${first(other)}"
                    else "${first(other)} lent you",
                subtitle = loan.title,
                amount = Money.format(loan.amount, loan.currency),
                primary = loan.lenderId == ME,
                ref = loan.id,
                personId = other,
            )
    }
    return events.filter { it.at <= now }.sortedByDescending { it.at }
}

/** The timeline grouped by day: Today, Yesterday, Mon 28 Sep, … */
fun LedgerView.timelineDays(events: List<TimelineEvent> = timeline()): List<TimelineDay> =
    events
        .groupBy { localDate(it.at) }
        .map { (date, dayEvents) -> TimelineDay(date, Dates.dayHeader(date, today), dayEvents) }

/** What a payment was for: the expense title, group name, loan reason or "Payment". */
fun LedgerView.paymentFor(payment: Payment): String =
    payment.expenseId?.let { expense(it)?.title }
        ?: payment.groupId?.let { group(it)?.name }
        ?: payment.loanId?.let { loan(it)?.title }
        ?: "Payment"

/** A Confirm card: a payment a friend recorded to you, waiting for your answer (§6.7). */
data class ClaimCard(val payment: Payment, val title: String, val detail: String)

/** Every pending payment to you, newest first. */
fun LedgerView.pendingClaims(): List<ClaimCard> =
    ledger.payments
        .filter { it.toId == ME && it.status == PaymentStatus.Pending }
        .sortedByDescending { it.createdAt }
        .map { claimCard(it) }

fun LedgerView.claimCard(payment: Payment): ClaimCard {
    val pronoun = person(payment.fromId)?.pronoun?.subject ?: "they"
    return ClaimCard(
        payment,
        "${first(payment.fromId)} says $pronoun paid you ${Money.format(payment.amount, payment.currency)}",
        "${paymentFor(payment)} · ${payment.method.label} · ${Dates.time(payment.createdAt, zone)}",
    )
}
