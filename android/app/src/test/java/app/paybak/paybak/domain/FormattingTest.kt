package app.paybak.paybak.domain

import app.paybak.paybak.domain.format.Dates
import app.paybak.paybak.domain.format.Money
import app.paybak.paybak.domain.format.MoneySign
import java.time.LocalDate
import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Test

/** verify.py `check_formatting`. */
class FormattingTest {
    private val figmaDay = Demo.figmaDay

    @Test
    fun money() {
        assertEquals("+₹2,900", Money.format(rupee(2900), sign = MoneySign.Signed))
        assertEquals("−₹1,850", Money.format(-rupee(1850), sign = MoneySign.Signed))
        assertEquals("₹1,00,000", Money.format(rupee(100000)))
        assertEquals("₹99,99,999.50", Money.format(rupee(9999999.5)))
        assertEquals("AED 1,800", Money.format(rupee(1800), "AED"))
        assertEquals("€1,200", Money.format(rupee(1200), "EUR"))
        assertEquals("¥1,234", Money.format(1234, "JPY"))
        assertEquals(rupee(21936), Money.convert(rupee(960), "22.85", "AED", "INR"))
    }

    @Test
    fun moreMoneyVectors() {
        assertEquals("₹2,800", Money.format(rupee(2800)))
        assertEquals("₹12,500", Money.format(rupee(12500)))
        assertEquals("₹1,234.50", Money.format(rupee(1234.5)))
        assertEquals("₹333.34", Money.format(33334))
        assertEquals("S$40", Money.format(rupee(40), "SGD"))
        assertEquals("−₹450", Money.format(-rupee(450), sign = MoneySign.Debit))
        assertEquals("₹450", Money.format(rupee(450), sign = MoneySign.Debit))
        assertEquals(
            "≈ ₹21,936 · ₹22.85 per AED",
            Money.approxLine(rupee(960), "22.85", "AED", "INR"),
        )
    }

    @Test
    fun dates() {
        assertEquals("26 Sep", Dates.rowDate(LocalDate.of(2026, 9, 26), figmaDay))
        assertEquals("Mon 28 Sep", Dates.dayHeader(LocalDate.of(2026, 9, 28), figmaDay))
        assertEquals("Due Fri", Dates.dueBadge(LocalDate.of(2026, 10, 2), figmaDay))
        assertEquals("Overdue 3 days", Dates.dueBadge(LocalDate.of(2026, 9, 27), figmaDay))
        assertEquals("9:12 pm", Dates.time(LocalDateTime.of(2026, 9, 30, 21, 12)))
        assertEquals("21–25 Sep", Dates.range(LocalDate.of(2026, 9, 21), LocalDate.of(2026, 9, 25)))
        assertEquals("12 Jun", Dates.loanMetaDate(LocalDate.of(2026, 6, 12), figmaDay))
        assertEquals("Wed 30 Sep", Dates.loanMetaDate(figmaDay, LocalDate.of(2026, 11, 3)))
    }

    @Test
    fun moreDateVectors() {
        assertEquals("Today", Dates.rowDate(figmaDay, figmaDay))
        assertEquals("Yesterday", Dates.dayHeader(figmaDay.minusDays(1), figmaDay))
        assertEquals("Overdue 1 day", Dates.dueBadge(figmaDay.minusDays(1), figmaDay))
        assertEquals("Due 12 Oct", Dates.dueBadge(LocalDate.of(2026, 10, 12), figmaDay))
        assertEquals("Due Sun 4 Oct", Dates.dueLabel(LocalDate.of(2026, 10, 4)))
        assertEquals(
            "28 Sep – 2 Oct",
            Dates.range(LocalDate.of(2026, 9, 28), LocalDate.of(2026, 10, 2)),
        )
        assertEquals("12:05 am", Dates.time(LocalDateTime.of(2026, 9, 30, 0, 5)))
        assertEquals(LocalDate.of(2027, 2, 28), Dates.addMonths(LocalDate.of(2027, 1, 31), 1, 31))
        assertEquals(LocalDate.of(2027, 3, 31), Dates.addMonths(LocalDate.of(2027, 1, 31), 2, 31))
        assertEquals("It’s due Friday.", Dates.duePhrase(LocalDate.of(2026, 10, 2), figmaDay))
        assertEquals("It was due on 27 Sep.", Dates.duePhrase(LocalDate.of(2026, 9, 27), figmaDay))
        assertEquals("24 days left", Dates.daysLeft(LocalDate.of(2026, 10, 24), figmaDay))
        assertEquals("1 day left", Dates.daysLeft(figmaDay.plusDays(1), figmaDay))
        assertEquals("28th", Dates.ordinal(28))
        assertEquals("1st", Dates.ordinal(1))
        assertEquals("11th", Dates.ordinal(11))
    }
}
