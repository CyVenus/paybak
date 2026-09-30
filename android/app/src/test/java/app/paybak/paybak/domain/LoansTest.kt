package app.paybak.paybak.domain

import app.paybak.paybak.domain.calc.loanDetail
import app.paybak.paybak.domain.format.Dates
import app.paybak.paybak.domain.format.Money
import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

/** verify.py `check_loans`. */
class LoansTest {
    @Test
    fun kabirBikeService() {
        val detail = Demo.load().loanDetail("l-kabir-bike")!!
        assertEquals(
            listOf("Paid 10 Jul", "Paid 12 Aug", "Paid 14 Sep · 2 days late"),
            detail.rows.map { it.label },
        )
        assertEquals("Bike service · 12 Jun", detail.meta)
        assertEquals("Paid back on 14 Sep", detail.paidBackOn)
    }

    @Test
    fun devLaptopRepair() {
        val added = Demo.load("lendDev")
        val detail = added.loanDetail("l-dev-laptop")!!
        assertEquals(
            listOf("Fri 30 Oct" to "₹2,000", "Mon 30 Nov" to "₹2,000", "Wed 30 Dec" to "₹2,000"),
            detail.rows.map {
                Dates.day(it.installment.due!!) to Money.format(it.installment.amount)
            },
        )
        assertEquals("Laptop repair · Today", detail.meta)
        assertEquals(rupee(6700), added.friendNets()["p-dev"])
    }

    @Test
    fun devLoanOverdue() {
        val overdue = Demo.load("lendDevOverdue")
        assertEquals(LocalDateTime.of(2026, 11, 3, 10, 0), overdue.localDateTime(overdue.now))
        val detail = overdue.loanDetail("l-dev-laptop")!!
        assertEquals("Overdue 4 days", detail.rows.first().label)
        val sent =
            overdue.ledger.reminders
                .filter { it.loanId == "l-dev-laptop" }
                .map { overdue.localDate(it.sentAt) }
                .sorted()
        assertEquals(listOf("Wed 28 Oct", "Fri 30 Oct", "Mon 2 Nov"), sent.map(Dates::day))
        assertEquals("Last reminder sent Mon 2 Nov", detail.lastReminder)
        assertEquals("Laptop repair · Wed 30 Sep", detail.meta)
        assertFalse(
            "Snacks purged 30 days after it was deleted",
            overdue.ledger.expenses.any { it.id == "e-goa-snacks" },
        )
    }
}
