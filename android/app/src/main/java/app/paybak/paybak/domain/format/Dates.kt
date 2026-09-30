package app.paybak.paybak.domain.format

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/**
 * Date and time copy in English with fixed names (domain.md §3). The platform's en-GB data would
 * write September as "Sept", so the names are spelled out here.
 */
object Dates {
    private val shortMonths =
        listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

    fun shortMonth(month: Month): String = shortMonths[month.ordinal]

    fun monthName(month: Month): String = month.name.lowercase().replaceFirstChar(Char::uppercase)

    fun shortWeekday(day: DayOfWeek): String = weekdayName(day).take(3)

    fun weekdayName(day: DayOfWeek): String = day.name.lowercase().replaceFirstChar(Char::uppercase)

    /** "Mon 28 Sep". */
    fun day(d: LocalDate): String =
        "${shortWeekday(d.dayOfWeek)} ${d.dayOfMonth} ${shortMonth(d.month)}"

    /** "26 Sep". */
    fun short(d: LocalDate): String = "${d.dayOfMonth} ${shortMonth(d.month)}"

    /** "9:12 pm". */
    fun time(t: LocalDateTime): String {
        val hour = t.hour % 12
        return "${if (hour == 0) 12 else hour}:${t.minute.toString().padStart(2, '0')} " +
            if (t.hour < 12) "am" else "pm"
    }

    fun time(moment: Instant, zone: ZoneId): String = time(LocalDateTime.ofInstant(moment, zone))

    /** List rows: Today · Yesterday · 26 Sep · 26 Sep 2025. */
    fun rowDate(d: LocalDate, today: LocalDate): String =
        when (d) {
            today -> "Today"
            today.minusDays(1) -> "Yesterday"
            else -> if (d.year == today.year) short(d) else "${short(d)} ${d.year}"
        }

    /** Timeline day groups: Today · Yesterday · Mon 28 Sep (+ year when it isn't this year). */
    fun dayHeader(d: LocalDate, today: LocalDate): String =
        if (d == today || d == today.minusDays(1)) {
            rowDate(d, today)
        } else if (d.year == today.year) {
            day(d)
        } else {
            "${day(d)} ${d.year}"
        }

    /** Due Fri (within 6 days) · Due 12 Oct · Overdue 3 days · Overdue 1 day. */
    fun dueBadge(due: LocalDate, today: LocalDate): String {
        val days = ChronoUnit.DAYS.between(today, due)
        return when {
            days < 0 -> "Overdue ${-days} day" + if (days == -1L) "" else "s"
            days <= 6 -> "Due ${shortWeekday(due.dayOfWeek)}"
            else -> "Due ${short(due)}"
        }
    }

    fun isOverdue(due: LocalDate, today: LocalDate): Boolean = due.isBefore(today)

    /** "Due Sun 4 Oct". */
    fun dueLabel(due: LocalDate): String = "Due ${day(due)}"

    /** Loan meta: Today · Yesterday · Wed 30 Sep (under 60 days) · 12 Jun (+ year if other). */
    fun loanMetaDate(d: LocalDate, today: LocalDate): String {
        val age = ChronoUnit.DAYS.between(d, today)
        if (age == 0L || age == 1L) return rowDate(d, today)
        val text = if (age < 60) day(d) else short(d)
        return if (d.year == today.year) text else "$text ${d.year}"
    }

    /** 21–25 Sep · 28 Sep – 2 Oct · 26 Sep (one day). */
    fun range(start: LocalDate, end: LocalDate): String =
        when {
            start == end -> short(start)
            start.year == end.year && start.month == end.month ->
                "${start.dayOfMonth}–${end.dayOfMonth} ${shortMonth(end.month)}"
            else -> "${short(start)} – ${short(end)}"
        }

    /** "1 Sep – 30 Sep 2026": the export range line. */
    fun rangeWithYear(start: LocalDate, end: LocalDate): String =
        "${short(start)} – ${short(end)} ${end.year}"

    /** The same day of month [months] later, clamped to the month's last day. */
    fun addMonths(d: LocalDate, months: Long, day: Int): LocalDate {
        val month = d.withDayOfMonth(1).plusMonths(months)
        return month.withDayOfMonth(minOf(day, month.lengthOfMonth()))
    }

    fun lastDayOfMonth(d: LocalDate): LocalDate = d.withDayOfMonth(d.lengthOfMonth())

    /** Recently deleted: whole days from today to the purge day. "24 days left", "1 day left". */
    fun daysLeft(purgeDay: LocalDate, today: LocalDate): String {
        val days = ChronoUnit.DAYS.between(today, purgeDay).coerceAtLeast(0)
        return "$days day" + (if (days == 1L) "" else "s") + " left"
    }

    /** The reminder body's due phrase (domain.md §3): "It’s due Friday." … */
    fun duePhrase(due: LocalDate, on: LocalDate): String {
        val days = ChronoUnit.DAYS.between(on, due)
        return when {
            days == 0L -> "It’s due today."
            days == 1L -> "It’s due tomorrow."
            days in 2..6 -> "It’s due ${weekdayName(due.dayOfWeek)}."
            days > 6 -> "It’s due on ${short(due)}."
            else -> "It was due on ${short(due)}."
        }
    }

    /** "Good morning/afternoon/evening" by the local hour (5–12, 12–17, else). */
    fun partOfDay(hour: Int): String =
        when (hour) {
            in 5..11 -> "morning"
            in 12..16 -> "afternoon"
            else -> "evening"
        }

    /** Monthly on the 1st · 2nd · 3rd · 28th. */
    fun ordinal(n: Int): String {
        val suffix =
            if (n % 100 in 11..13) "th"
            else
                when (n % 10) {
                    1 -> "st"
                    2 -> "nd"
                    3 -> "rd"
                    else -> "th"
                }
        return "$n$suffix"
    }
}
