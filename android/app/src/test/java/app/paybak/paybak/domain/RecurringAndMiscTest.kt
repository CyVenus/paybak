package app.paybak.paybak.domain

import app.paybak.paybak.domain.actions.ActionContext
import app.paybak.paybak.domain.actions.tick
import app.paybak.paybak.domain.calc.dueSoon
import app.paybak.paybak.domain.calc.exportDefaultTicks
import app.paybak.paybak.domain.calc.nextOccurrence
import app.paybak.paybak.domain.calc.recurring
import app.paybak.paybak.domain.format.Dates
import app.paybak.paybak.domain.model.Frequency
import app.paybak.paybak.domain.model.ME
import app.paybak.paybak.domain.model.Plan
import app.paybak.paybak.domain.model.RecurringRule
import app.paybak.paybak.domain.model.RuleSplit
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

/** verify.py `check_recurring_and_misc`. */
class RecurringAndMiscTest {
    private val ledger = Demo.load()

    @Test
    fun recurringDates() {
        assertEquals(
            mapOf("Rent" to "Thu 1 Oct", "Wi-Fi" to "Mon 5 Oct", "Cooking gas" to "Wed 28 Oct"),
            ledger.ledger.recurringRules.associate {
                it.title to Dates.day(nextOccurrence(it, ledger.today))
            },
        )
        val draft = ledger.ledger.drafts.first()
        assertEquals(
            "September draft · 28 Sep",
            "${Dates.monthName(draft.occurrenceDate.month)} draft · ${Dates.short(draft.occurrenceDate)}",
        )
        assertEquals(
            listOf("September draft · 28 Sep"),
            ledger.recurring("g-flat302").drafts.map { it.label },
        )
        assertEquals(
            listOf("Next Thu 1 Oct", "Next Mon 5 Oct", "Next Wed 28 Oct"),
            ledger.recurring("g-flat302").rules.map { it.nextLabel },
        )
        assertEquals("Monthly on the 28th", ledger.recurring("g-flat302").rules.last().schedule)
    }

    @Test
    fun everyTwoWeeksCatchesUpEveryOtherWeek() {
        val monday = LocalDate.of(2026, 9, 28)
        val cleaner =
            RecurringRule(
                id = "r-cleaner",
                groupId = "g-flat302",
                title = "Cleaner",
                amount = rupee(600),
                currency = "INR",
                frequency = Frequency.Biweekly,
                anchorDate = monday,
                startDate = monday,
                lastOccurrence = monday,
                split = RuleSplit(personIds = listOf(ME, "p-meera", "p-kabir")),
                createdAt = ledger.now,
            )
        val caughtUp =
            ledger.ledger
                .copy(recurringRules = ledger.ledger.recurringRules + cleaner)
                .tick(ActionContext(ledger.now.plus(Duration.ofDays(42)), Demo.zone, "INR"))
        assertEquals(
            listOf(
                LocalDate.of(2026, 10, 12),
                LocalDate.of(2026, 10, 26),
                LocalDate.of(2026, 11, 9),
            ),
            caughtUp.expenses.filter { it.recurringRuleId == cleaner.id }.map { it.date },
        )
    }

    @Test
    fun proAndTrial() {
        assertEquals("Wed 7 Oct", Dates.day(ledger.today.plusDays(7)))
        assertEquals(Plan.Pro, Demo.load("pro").ledger.settings.entitlement.plan)
    }

    @Test
    fun exportTicks() {
        assertEquals(
            setOf("Goa Trip", "Flat 302", "Build a Drone", "Without a group"),
            ledger.exportDefaultTicks(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30)),
        )
        assertEquals(
            "1 Sep – 30 Sep 2026",
            Dates.rangeWithYear(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30)),
        )
    }

    @Test
    fun tickIsIdempotent() {
        val again = ledger.ledger.tick(ActionContext(ledger.now, Demo.zone, "INR"))
        assertEquals(ledger.ledger, again)
    }

    @Test
    fun rentOnTheFirst() {
        val later = Demo.load(now = LocalDateTime.of(2026, 10, 1, 10, 0))
        assertEquals(rupee(36000), later.ledger.expense("e-r-rent-2026-10-01")!!.amount)
        assertEquals(rupee(-450 + 24000), later.groupNets("g-flat302")[ME])
    }

    @Test
    fun anotherDay() {
        val shifted =
            Demo.load(now = LocalDateTime.of(2026, 10, 1, 9, 0), anchor = LocalDate.of(2026, 10, 1))
        val totals = shifted.homeTotals()
        assertEquals(rupee(2900) to rupee(1850), totals.owed to totals.owe)
        assertEquals(listOf("Overdue 3 days", "Due Sat"), shifted.dueSoon().map { it.badge })
        assertFalse(shifted.ledger.reminders.any { shifted.localDate(it.sentAt) == shifted.today })
    }
}
