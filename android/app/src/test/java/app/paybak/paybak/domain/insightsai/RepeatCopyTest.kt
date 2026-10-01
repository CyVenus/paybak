package app.paybak.paybak.domain.insightsai

import app.paybak.paybak.domain.model.Frequency
import app.paybak.paybak.domain.model.RepeatRule
import app.paybak.paybak.domain.recurring.RepeatCopy
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

/** The Repeat sheet's copy and next dates (insights §5.3, §5.5). */
class RepeatCopyTest {
    private val today = LocalDate.of(2026, 9, 30)
    private val gas = RepeatRule(Frequency.Monthly, LocalDate.of(2026, 9, 28), variable = true)

    @Test
    fun cookingGasDraftsOnThe28th() {
        assertEquals("Day of month", RepeatCopy.anchorTitle(gas.frequency))
        assertEquals("28th", RepeatCopy.anchorValue(gas))
        assertEquals(
            "Paybak adds a draft on the 28th and asks you for the amount.",
            RepeatCopy.helper(gas),
        )
        val next = RepeatCopy.next(gas, LocalDate.of(2026, 9, 28), today)
        assertEquals("Next draft: Wed 28 Oct", RepeatCopy.nextLine(gas, next))
    }

    @Test
    fun fixedWeeklyAndYearlyRules() {
        val rent = RepeatRule(Frequency.Monthly, LocalDate.of(2026, 9, 1))
        assertEquals("Paybak adds this expense on the 1st of every month.", RepeatCopy.helper(rent))
        assertEquals(
            "Next: Thu 1 Oct",
            RepeatCopy.nextLine(rent, RepeatCopy.next(rent, today, today)),
        )
        val weekly = RepeatRule(Frequency.Weekly, LocalDate.of(2026, 9, 28))
        assertEquals("Monday", RepeatCopy.anchorValue(weekly))
        assertEquals("Paybak adds this expense every Monday.", RepeatCopy.helper(weekly))
        assertEquals(LocalDate.of(2026, 10, 5), RepeatCopy.next(weekly, today, today))
        val yearly = RepeatRule(Frequency.Yearly, LocalDate.of(2026, 9, 30), variable = true)
        assertEquals("28 Sep".length, RepeatCopy.anchorValue(yearly).length)
        assertEquals(
            "Paybak adds a draft on 30 Sep each year and asks you for the amount.",
            RepeatCopy.helper(yearly),
        )
        assertEquals(LocalDate.of(2027, 9, 30), RepeatCopy.next(yearly, today, today))
    }
}
