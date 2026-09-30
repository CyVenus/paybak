package app.paybak.paybak.domain

import app.paybak.paybak.domain.calc.friendPage
import app.paybak.paybak.domain.calc.groupSheet
import app.paybak.paybak.domain.calc.groupSummaries
import app.paybak.paybak.domain.calc.recurring
import app.paybak.paybak.domain.calc.simplifiedFootnoteGroups
import app.paybak.paybak.domain.format.Dates
import app.paybak.paybak.domain.format.Money
import app.paybak.paybak.domain.model.ME
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** verify.py `check_groups`. */
class GroupsTest {
    private val ledger = Demo.load()

    @Test
    fun goaTrip() {
        val (paid, share) = ledger.groupPaidShare("g-goa")
        val nets = ledger.groupNets("g-goa")
        assertEquals(39500L, paid.values.sum() / 100)
        assertEquals(
            mapOf(
                "You" to listOf(6500L, 7900L, -1400L),
                "Kabir" to listOf(18000L, 7900L, 10100L),
                "Priya" to listOf(3500L, 7900L, -4400L),
                "Esha" to listOf(5000L, 7900L, -2900L),
                "Dev" to listOf(6500L, 7900L, -1400L),
            ),
            nets.keys.associate {
                ledger.first(it) to
                    listOf(
                        paid.getValue(it) / 100,
                        share.getValue(it) / 100,
                        nets.getValue(it) / 100,
                    )
            },
        )
        val sheet = ledger.groupSheet("g-goa")!!
        assertEquals("21–25 Sep · 5 members · ₹39,500 spent", sheet.titleRow)
        assertEquals(
            listOf(
                    listOf("Dev", "Kabir", 1400L),
                    listOf("Esha", "Kabir", 2900L),
                    listOf("Priya", "Kabir", 4400L),
                    listOf("You", "Kabir", 1400L),
                )
                .sortedBy { it.toString() },
            ledger
                .groupPlan("g-goa")
                .map {
                    listOf(ledger.first(it.debtorId), ledger.first(it.creditorId), it.amount / 100)
                }
                .sortedBy { it.toString() },
        )
        assertEquals(
            "Simplify debts is on. You, Priya, Esha and Dev each pay Kabir directly.",
            sheet.footnote,
        )
        assertNotNull(ledger.ledger.expense("e-goa-snacks")!!.deletedAt)
        assertEquals(3600L, ledger.ledger.expense("e-goa-villa")!!.amount / 5 / 100)
        assertEquals(listOf("Goa Trip"), ledger.simplifiedFootnoteGroups())
    }

    @Test
    fun flat302AndCollegeGang() {
        assertEquals(
            mapOf(ME to -45000L, "p-meera" to 90000L, "p-kabir" to -45000L),
            ledger.groupNets("g-flat302"),
        )
        assertEquals(
            listOf(Triple(ME, "p-meera", 45000L), Triple("p-kabir", "p-meera", 45000L)),
            ledger.groupPlan("g-flat302").map { Triple(it.debtorId, it.creditorId, it.amount) },
        )
        assertEquals(setOf(0L), ledger.groupNets("g-college").values.toSet())
        assertEquals(3, ledger.recurring("g-flat302").rules.size)
    }

    @Test
    fun dubaiWeekend() {
        val (paid, share) = ledger.groupPaidShare("g-dubai")
        assertEquals(
            mapOf(
                "You" to ("AED 540" to "AED 600"),
                "Kabir" to ("AED 960" to "AED 600"),
                "Meera" to ("AED 300" to "AED 600"),
            ),
            paid.keys.associate {
                ledger.first(it) to
                    (Money.format(paid.getValue(it), "AED") to
                        Money.format(share.getValue(it), "AED"))
            },
        )
        assertEquals(setOf(0L), ledger.groupNets("g-dubai").values.toSet())
        val sheet = ledger.groupSheet("g-dubai")!!
        assertEquals(
            listOf(
                "≈ ₹6,870 · ₹22.90 per AED",
                "≈ ₹12,312 · ₹22.80 per AED",
                "≈ ₹21,936 · ₹22.85 per AED",
            ),
            sheet.expenses.map { sheet.rateLines.getValue(it.id) },
        )
        assertEquals("Total AED 1,800 · ≈ ₹41,118 at saved rates", sheet.totalLine)
        assertEquals("You paid Kabir AED 60 on 14 Mar", sheet.settledCaption)
    }

    @Test
    fun groupsList() {
        val rows = ledger.groupSummaries()
        assertEquals(
            listOf(
                "Goa Trip",
                "Flat 302",
                "Build a Drone",
                "College Gang",
                "Dubai Weekend",
                "Hackathon Kit",
            ),
            rows.map { it.group.name },
        )
        assertEquals(
            listOf("5 members", "3 members", "6 members", "3 members"),
            listOf("g-goa", "g-flat302", "g-college", "g-dubai").map { id ->
                "${rows.first { it.group.id == id }.memberCount} members"
            },
        )
    }

    @Test
    fun rohan() {
        val page = ledger.friendPage("p-rohan")!!
        val expenses =
            page.history.filter { it.kind.name == "Expense" }.map { ledger.expense(it.ref)!! }
        assertEquals(
            listOf(
                Triple("Movie tickets", "₹1,600", "20 Sep"),
                Triple("Farewell dinner", "₹9,000", "12 Mar"),
            ),
            expenses.map { Triple(it.title, Money.format(it.amount), Dates.short(it.date)) },
        )
        assertEquals(listOf("College Gang", "Build a Drone"), page.groupsTogether.map { it.name })
        assertEquals("Last reminder sent today", page.lastReminder)
        assertEquals(
            listOf(25, 27, 30),
            ledger.ledger.reminders
                .filter { it.toId == "p-rohan" }
                .map { ledger.localDate(it.sentAt).dayOfMonth }
                .sorted(),
        )
        assertTrue(page.balance.overdue)
    }
}
