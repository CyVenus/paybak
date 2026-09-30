package app.paybak.paybak.domain.addrecord

import app.paybak.paybak.domain.format.Dates
import app.paybak.paybak.domain.model.ReminderSchedule
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters

/** The due-date quick chips (add-expense §3.7), shared by Add expense and Lend money. */
enum class DueChip(val label: String, val tag: String) {
    Tomorrow("Tomorrow", "tomorrow"),

    /** The coming Sunday (today on a Sunday). */
    ThisWeekend("This weekend", "weekend"),
    NextWeek("Next week", "nextWeek");

    fun date(today: LocalDate): LocalDate =
        when (this) {
            Tomorrow -> today.plusDays(1)
            ThisWeekend -> today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
            NextWeek -> today.plusDays(7)
        }

    companion object {
        /** The chip that shows as selected for [due], if one gives that date. */
        fun matching(due: LocalDate?, today: LocalDate): DueChip? = due?.let {
            entries.firstOrNull { chip -> chip.date(today) == due }
        }
    }
}

/** Date copy of the forms and the date sheet (add-expense §3.6, §10). */
object DateCopy {
    /** The date chip and rows: "Today", "Yesterday", "Mon 28 Sep". */
    fun label(date: LocalDate, today: LocalDate): String =
        when (date) {
            today -> "Today"
            today.minusDays(1) -> "Yesterday"
            else -> Dates.day(date)
        }

    /**
     * The sheet's summary line: "Sun 4 Oct · in 4 days" for a due date, "Mon 28 Sep · 2 days ago"
     * for when something happened.
     */
    fun summary(date: LocalDate, today: LocalDate, due: Boolean): String {
        val days = ChronoUnit.DAYS.between(today, date)
        val relative =
            when {
                days == 0L -> if (due) "today" else "Today"
                days == 1L -> "tomorrow"
                days == -1L -> if (due) "yesterday" else "Yesterday"
                days > 1 -> "in $days days"
                else -> "${-days} days ago"
            }
        return "${Dates.day(date)} · $relative"
    }

    /**
     * The Due date sheet's hint, from the Settings reminder schedule: "Paybak reminds them 2 days
     * before, on the day, and every 3 days if it’s overdue."
     */
    fun reminderHint(schedule: ReminderSchedule): String {
        val parts =
            listOfNotNull(
                "2 days before".takeIf { schedule.twoDaysBefore },
                "on the day".takeIf { schedule.onDueDate },
                "every 3 days if it’s overdue".takeIf { schedule.overdueEvery3Days },
            )
        return when (parts.size) {
            0 -> "Paybak won’t send reminders for it. You can still remind them yourself."
            1 -> "Paybak reminds them ${parts[0]}."
            else ->
                "Paybak reminds them ${parts.dropLast(1).joinToString(", ")}, and ${parts.last()}."
        }
    }
}
