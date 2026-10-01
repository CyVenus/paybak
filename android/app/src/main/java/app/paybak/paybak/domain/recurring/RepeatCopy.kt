package app.paybak.paybak.domain.recurring

import app.paybak.paybak.domain.calc.nextOccurrence
import app.paybak.paybak.domain.format.Dates
import app.paybak.paybak.domain.model.Frequency
import app.paybak.paybak.domain.model.RecurringRule
import app.paybak.paybak.domain.model.RepeatRule
import app.paybak.paybak.domain.model.RuleSplit
import java.time.Instant
import java.time.LocalDate

/**
 * The Repeat sheet's copy (insights §5.3, §5.5) for a rule whose day comes from its anchor: what the
 * day row says, the helper under the card and the next occurrence.
 */
object RepeatCopy {
    /** "Day of month" · "Day of week" · "Date". */
    fun anchorTitle(frequency: Frequency): String =
        when (frequency) {
            Frequency.Monthly -> "Day of month"
            Frequency.Weekly,
            Frequency.Biweekly -> "Day of week"
            Frequency.Yearly -> "Date"
        }

    /** "28th" · "Monday" · "28 Sep". */
    fun anchorValue(rule: RepeatRule): String =
        when (rule.frequency) {
            Frequency.Monthly -> Dates.ordinal(rule.anchorDate.dayOfMonth)
            Frequency.Weekly,
            Frequency.Biweekly -> Dates.weekdayName(rule.anchorDate.dayOfWeek)
            Frequency.Yearly -> Dates.short(rule.anchorDate)
        }

    /**
     * "Paybak adds a draft on the 28th and asks you for the amount." for a variable rule, "Paybak
     * adds this expense on the 28th of every month." for a fixed one.
     */
    fun helper(rule: RepeatRule): String {
        val day = rule.anchorDate
        val weekday = Dates.weekdayName(day.dayOfWeek)
        val ordinal = Dates.ordinal(day.dayOfMonth)
        return if (rule.variable) {
            val on =
                when (rule.frequency) {
                    Frequency.Monthly -> "on the $ordinal"
                    Frequency.Weekly -> "every $weekday"
                    Frequency.Biweekly -> "every other $weekday"
                    Frequency.Yearly -> "on ${Dates.short(day)} each year"
                }
            "Paybak adds a draft $on and asks you for the amount."
        } else {
            val on =
                when (rule.frequency) {
                    Frequency.Monthly -> "on the $ordinal of every month"
                    Frequency.Weekly -> "every $weekday"
                    Frequency.Biweekly -> "every other $weekday"
                    Frequency.Yearly -> "on ${Dates.short(day)} every year"
                }
            "Paybak adds this expense $on."
        }
    }

    /** The first occurrence after the expense on [start] that's still to come. */
    fun next(rule: RepeatRule, start: LocalDate, today: LocalDate): LocalDate =
        nextOccurrence(
            RecurringRule(
                id = "",
                title = "",
                currency = "",
                variable = rule.variable,
                frequency = rule.frequency,
                anchorDate = rule.anchorDate,
                startDate = start,
                split = RuleSplit(personIds = emptyList()),
                createdAt = Instant.EPOCH,
            ),
            maxOf(start, today),
        )

    /** "Next draft: Wed 28 Oct" (variable) · "Next: Wed 28 Oct". */
    fun nextLine(rule: RepeatRule, next: LocalDate): String =
        (if (rule.variable) "Next draft: " else "Next: ") + Dates.day(next)
}
