package app.paybak.paybak.domain

import app.paybak.paybak.domain.calc.HomeState
import app.paybak.paybak.domain.calc.HomeTotals
import app.paybak.paybak.domain.calc.homeSummary
import app.paybak.paybak.domain.calc.timeline
import app.paybak.paybak.domain.format.Money
import app.paybak.paybak.domain.format.MoneySign
import app.paybak.paybak.domain.model.ME
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** verify.py `check_payments`. */
class PaymentsTest {
    @Test
    fun pendingPaymentsChangeNothing() {
        assertEquals(
            "−₹1,850",
            Money.format(
                -Demo.load("paymentToMeeraPending").homeTotals().owe,
                sign = MoneySign.Signed,
            ),
        )
        assertEquals(rupee(1850), Demo.load("paymentToKabirPending").homeTotals().owe)
    }

    @Test
    fun meeraConfirms() {
        val totals = Demo.load("paymentToMeeraConfirmed").homeTotals()
        assertEquals(
            "−₹1,400" to "across 1 group",
            Money.format(-totals.owe, sign = MoneySign.Signed) to totals.oweCaption,
        )
    }

    @Test
    fun allSettled() {
        val settled = Demo.load("allSettled")
        assertEquals(HomeTotals(0, 0, 0, 0, 0), settled.homeTotals())
        assertEquals(HomeState.AllSettled, settled.homeSummary().state)
    }

    @Test
    fun disputedExpenseStillCounts() {
        val flagged = Demo.load("eshaFlagsSeafood")
        assertEquals(
            "I left before dessert. Can we check the bill?",
            flagged.ledger.expense("e-goa-seafood")!!.flag!!.note,
        )
        assertEquals(-rupee(1400), flagged.groupNets("g-goa")[ME])
    }

    @Test
    fun emptyAccount() {
        val empty = Demo.load("empty")
        assertEquals(0L, empty.homeTotals().owed)
        assertTrue(empty.timeline().isEmpty())
        assertEquals(HomeState.FirstDay, empty.homeSummary().state)
    }

    @Test
    fun weekendTrek() {
        val trek = Demo.load("weekendTrek")
        val group = trek.ledger.group("g-trek")!!
        assertEquals(
            "Trip · 4 members · INR",
            "Trip · ${group.memberIds.size} members · ${group.currency}",
        )
        assertEquals(setOf(0L), trek.groupNets("g-trek").values.toSet())
    }
}
