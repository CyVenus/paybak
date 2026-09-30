package app.paybak.paybak.domain

import app.paybak.paybak.domain.calc.DueAction
import app.paybak.paybak.domain.calc.ObligationKind
import app.paybak.paybak.domain.calc.dueSoon
import app.paybak.paybak.domain.calc.friendBalances
import app.paybak.paybak.domain.calc.groupDueAnswer
import app.paybak.paybak.domain.calc.homeSummary
import app.paybak.paybak.domain.calc.settlePlan
import app.paybak.paybak.domain.calc.whoOwesMeAnswer
import app.paybak.paybak.domain.format.Dates
import app.paybak.paybak.domain.format.Money
import app.paybak.paybak.domain.format.MoneySign
import org.junit.Assert.assertEquals
import org.junit.Test

/** verify.py `check_home_and_friends`. */
class HomeAndFriendsTest {
    private val ledger = Demo.load()

    @Test
    fun homeTotals() {
        val totals = ledger.homeTotals()
        assertEquals("+₹2,900", Money.format(totals.owed, sign = MoneySign.Signed))
        assertEquals("from 4 people", totals.owedCaption)
        assertEquals("−₹1,850", Money.format(-totals.owe, sign = MoneySign.Signed))
        assertEquals("across 2 groups", totals.oweCaption)
        assertEquals(
            "You still owe ₹1,850 and are owed ₹2,900.",
            "You still owe ${Money.format(totals.owe)} and are owed ${Money.format(totals.owed)}.",
        )
    }

    @Test
    fun friendNets() {
        assertEquals(
            mapOf(
                "Rohan" to 800L,
                "Priya" to 700L,
                "Esha" to 700L,
                "Dev" to 700L,
                "Kabir" to -1400L,
                "Meera" to -450L,
                "Ananya" to 0L,
            ),
            ledger.friendNets().mapKeys { ledger.first(it.key) }.mapValues { it.value / 100 },
        )
    }

    @Test
    fun dueSoon() {
        assertEquals(
            listOf(
                listOf("Rohan", "Movie tickets", 800L, "Overdue 3 days", DueAction.Remind),
                listOf("Goa Trip", "Your share", 1400L, "Due Fri", DueAction.Settle),
            ),
            ledger.dueSoon().map {
                listOf(it.title, it.detail, it.amount / 100, it.badge, it.action)
            },
        )
    }

    @Test
    fun recentActivity() {
        val recent = ledger.homeSummary().recent
        assertEquals(
            listOf("Dinner at Olive Garden", "Priya paid you", "Electricity bill"),
            recent.map { it.title },
        )
        assertEquals(listOf("Today", "Yesterday", "26 Sep"), recent.map { it.date })
        assertEquals("Flat 302 · You owe", recent[2].subtitle)
        assertEquals("−₹450", recent[2].amount)
        assertEquals("You paid · 4 people", recent[0].subtitle)
        assertEquals("₹2,800", recent[0].amount)
        assertEquals("UPI", recent[1].subtitle)
        assertEquals("₹1,050", recent[1].amount)
    }

    @Test
    fun friendsOrder() {
        assertEquals(
            listOf("Rohan", "Priya", "Esha", "Dev", "Kabir", "Meera", "Ananya"),
            ledger.friendBalances().map { it.person.firstName },
        )
    }

    @Test
    fun settleUp() {
        val plan = ledger.settlePlan()
        assertEquals(
            listOf(
                listOf("Kabir", 1400L, "Goa Trip", "Due Fri"),
                listOf("Meera", 450L, "Flat 302", "Due Mon"),
            ),
            plan.pay.map {
                listOf(
                    ledger.first(it.friendId),
                    it.amount / 100,
                    it.context,
                    Dates.dueBadge(it.due!!, ledger.today),
                )
            },
        )
        assertEquals(
            listOf(
                listOf("Rohan", 800L, "Movie tickets", "Overdue 3 days"),
                listOf("Priya", 700L, "Dinner at Olive Garden", "Due Sun"),
                listOf("Esha", 700L, "Dinner at Olive Garden", "Due Sun"),
                listOf("Dev", 700L, "Dinner at Olive Garden", "Due Sun"),
            ),
            plan.get.map {
                listOf(
                    ledger.first(it.friendId),
                    it.amount / 100,
                    it.context,
                    Dates.dueBadge(it.due!!, ledger.today),
                )
            },
        )
        assertEquals(listOf("Fri 2 Oct", "Mon 5 Oct"), plan.pay.map { Dates.day(it.due!!) })
        assertEquals("2 groups", ledger.homeTotals().oweCaption.removePrefix("across "))
    }

    @Test
    fun askPaybak() {
        assertEquals(
            "4 people owe you ₹2,900: Rohan ₹800 (overdue since 27 Sep), and Priya, Esha and Dev ₹700 each for tonight’s dinner.",
            ledger.whoOwesMeAnswer(),
        )
        val goa = ledger.openItems().first { it.kind == ObligationKind.Group && it.ref == "g-goa" }
        assertEquals(rupee(1400), goa.amount)
        assertEquals(
            "Your Goa Trip share of ₹1,400 is due Fri 2 Oct.",
            ledger.groupDueAnswer("g-goa"),
        )
    }
}
