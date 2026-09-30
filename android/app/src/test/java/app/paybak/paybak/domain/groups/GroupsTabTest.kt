package app.paybak.paybak.domain.groups

import app.paybak.paybak.domain.Demo
import app.paybak.paybak.domain.calc.friendBalances
import app.paybak.paybak.domain.calc.groupSummaries
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** The Groups tab rows (screens-groups §3) from the demo at Figma parity. */
class GroupsTabTest {
    private val view = Demo.load("eshaClaimsPayment")

    @Test
    fun groupsListMatchesFigma() {
        val list = view.groupsList(view.groupSummaries())
        assertEquals(
            listOf(
                listOf("Goa Trip", "5 members · Due Fri 2 Oct", "Owe", "₹1,400", null),
                listOf("Flat 302", "3 members · Due Mon 5 Oct", "Owe", "₹450", null),
                listOf("Build a Drone", "Project · 4 members", "Settled", null, "You’re settled"),
                listOf("College Gang", "6 members", "Settled", null, "Settled"),
                listOf("Dubai Weekend", "3 members · AED", "Settled", null, "Settled"),
            ),
            list.rows.map {
                listOf(it.group.name, it.subtitle, it.standing.name, it.amount, it.status)
            },
        )
        val drone = list.rows.first { it.group.id == "pj-drone" }.budget!!
        assertEquals("₹52,000 of ₹60,000", drone.spent)
        assertEquals("₹8,000 left", drone.left)
        assertEquals(0.867f, drone.progress, 0.001f)
        val archived = list.archived.single()
        assertEquals("Hackathon Kit", archived.group.name)
        assertEquals("Project · Closed 30 Aug", archived.subtitle)
        assertEquals("Read-only", archived.status)
        assertNull(archived.budget)
    }

    @Test
    fun anOverBudgetProjectFillsToTheBudgetPoint() {
        val over = Demo.load("devBuysGps")
        val drone = over.groupsList(over.groupSummaries()).rows.first { it.group.id == "pj-drone" }
        val budget = drone.budget!!
        assertEquals(true, budget.over)
        assertEquals("₹61,500 of ₹60,000", budget.spent)
        assertEquals("₹1,500 over budget", budget.left)
        assertEquals(60_000f / 61_500f, budget.progress, 0.001f)
    }

    @Test
    fun friendsListMatchesFigma() {
        val friends = view.friendBalances()
        val rows = friends.map(view::friendListRow)
        assertEquals(
            listOf(
                listOf("Rohan", "Movie tickets", "₹800", null, "Overdue 3 days", null),
                listOf("Priya", "Due Sun 4 Oct", "₹700", "Owes you", null, null),
                listOf("Esha", "Due Sun 4 Oct", "₹700", "Owes you", null, null),
                listOf("Dev", "Due Sun 4 Oct", "₹700", "Owes you", null, null),
                listOf("Kabir", "Goa Trip · Due Fri 2 Oct", "₹1,400", "You owe", null, null),
                listOf("Meera", "Flat 302 · Due Mon 5 Oct", "₹450", "You owe", null, null),
                listOf("Ananya", null, null, null, null, "No balance"),
            ),
            rows.map { listOf(it.name, it.subtitle, it.amount, it.label, it.overdue, it.status) },
        )
        assertEquals(true, rows.last().guest)
        val summary = view.friendsSummary(friends)
        assertEquals("+₹2,900", summary.owedText)
        assertEquals("−₹1,850", summary.oweText)
    }

    @Test
    fun aSettledFriendReadsSettled() {
        val settled = Demo.load("allSettled")
        val kabir = settled.friendBalances().first { it.person.id == "p-kabir" }
        val row = settled.friendListRow(kabir)
        assertEquals("Settled", row.status)
        assertNull(row.amount)
    }
}
