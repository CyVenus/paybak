package app.paybak.paybak.domain.calc

import app.paybak.paybak.domain.format.Dates
import app.paybak.paybak.domain.model.Draft
import app.paybak.paybak.domain.model.Frequency
import app.paybak.paybak.domain.model.RecurringRule
import java.time.LocalDate

/** The occurrence after [after] (§10): the anchor's day of month, weekday or day and month. */
fun nextOccurrence(rule: RecurringRule, after: LocalDate): LocalDate {
    val anchor = rule.anchorDate
    return when (rule.frequency) {
        Frequency.Weekly,
        Frequency.Biweekly -> {
            val delta = Math.floorMod(anchor.dayOfWeek.value - after.dayOfWeek.value - 1, 7) + 1
            after.plusDays(delta.toLong())
        }
        Frequency.Yearly -> {
            val candidate = anchor.withYear(after.year)
            if (candidate.isAfter(after)) candidate else anchor.withYear(after.year + 1)
        }
        Frequency.Monthly -> {
            val candidate = Dates.addMonths(after.withDayOfMonth(1), 0, anchor.dayOfMonth)
            if (candidate.isAfter(after)) candidate
            else Dates.addMonths(after.withDayOfMonth(1), 1, anchor.dayOfMonth)
        }
    }
}

/** A rule with its schedule copy: "Monthly on the 1st", "Next Thu 1 Oct". */
data class RuleRow(
    val rule: RecurringRule,
    val schedule: String,
    val next: LocalDate,
    val nextLabel: String,
)

/** A variable rule's occurrence still waiting for its amount: "September draft · 28 Sep". */
data class DraftRow(val draft: Draft, val rule: RecurringRule, val label: String)

data class RecurringSummary(val rules: List<RuleRow>, val drafts: List<DraftRow>)

fun LedgerView.recurring(groupId: String?): RecurringSummary {
    val rules = ledger.recurringRules.filter { it.groupId == groupId && it.active }
    return RecurringSummary(
        rules =
            rules.map { rule ->
                val next = nextOccurrence(rule, maxOf(today, rule.lastOccurrence ?: today))
                RuleRow(rule, scheduleText(rule), next, "Next ${Dates.day(next)}")
            },
        drafts =
            ledger.drafts
                .filter { it.expenseId == null }
                .mapNotNull { draft ->
                    val rule = rules.firstOrNull { it.id == draft.ruleId } ?: return@mapNotNull null
                    val date = draft.occurrenceDate
                    DraftRow(
                        draft,
                        rule,
                        "${Dates.monthName(date.month)} draft · ${Dates.short(date)}",
                    )
                },
    )
}

/** "Monthly on the 28th", "Weekly on Mondays", "Yearly on 12 Mar". */
fun scheduleText(rule: RecurringRule): String =
    when (rule.frequency) {
        Frequency.Monthly -> "Monthly on the ${Dates.ordinal(rule.anchorDate.dayOfMonth)}"
        Frequency.Weekly -> "Weekly on ${Dates.weekdayName(rule.anchorDate.dayOfWeek)}s"
        Frequency.Biweekly -> "Every 2 weeks on ${Dates.weekdayName(rule.anchorDate.dayOfWeek)}s"
        Frequency.Yearly -> "Yearly on ${Dates.short(rule.anchorDate)}"
    }
