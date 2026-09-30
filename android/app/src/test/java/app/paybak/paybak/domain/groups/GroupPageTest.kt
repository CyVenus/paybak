package app.paybak.paybak.domain.groups

import app.paybak.paybak.domain.Demo
import app.paybak.paybak.domain.model.ME
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Group detail, settings and Leave (screens-groups §4–5) from the demo at Figma parity. */
class GroupPageTest {
    private val view = Demo.load("eshaClaimsPayment")

    @Test
    fun goaTrip() {
        val page = view.groupPage("g-goa")!!
        assertEquals("21–25 Sep · 5 members · ₹39,500 spent", page.subtitle)
        assertEquals(listOf(ME, "p-kabir", "p-priya", "p-esha", "p-dev"), page.memberIds)
        with(page.balance) {
            assertEquals(Standing.Owe, standing)
            assertEquals("−₹1,400", amount)
            assertEquals("You owe Kabir · Due Fri 2 Oct", caption)
            assertEquals(GroupSettle.Pay("p-kabir", 140_000, "INR", "g-goa"), settle)
        }
        assertEquals(
            listOf(
                listOf("You", "Paid ₹6,500 · Share ₹7,900", "₹1,400", "You owe"),
                listOf("Kabir", "Paid ₹18,000 · Share ₹7,900", "₹10,100", "Gets back"),
                listOf("Priya", "Paid ₹3,500 · Share ₹7,900", "₹4,400", "Owes"),
                listOf("Esha", "Paid ₹5,000 · Share ₹7,900", "₹2,900", "Owes"),
                listOf("Dev", "Paid ₹6,500 · Share ₹7,900", "₹1,400", "Owes"),
            ),
            page.members.map { listOf(it.name, it.subtitle, it.amount, it.label) },
        )
        assertEquals(
            listOf("Simplify debts is on. You, Priya, Esha and Dev each pay Kabir directly."),
            page.notes,
        )
        assertEquals(
            listOf("Fri 25 Sep", "Thu 24 Sep", "Wed 23 Sep", "Tue 22 Sep", "Mon 21 Sep"),
            page.days.map { it.label },
        )
        assertEquals(
            listOf(
                listOf("Fuel", "Dev paid · Your share ₹500", "₹2,500"),
                listOf("Beach shack lunch", "Dev paid · Your share ₹800", "₹4,000"),
                listOf("Parasailing", "Esha paid · Your share ₹1,000", "₹5,000"),
                listOf("Seafood dinner at Britto’s", "You paid · Your share ₹1,300", "₹6,500"),
                listOf("Villa (3 nights)", "Kabir paid · Your share ₹3,600", "₹18,000"),
                listOf("Scooter rentals", "Priya paid · Your share ₹700", "₹3,500"),
            ),
            page.days.flatMap { it.rows }.map { listOf(it.expense.title, it.subtitle, it.amount) },
        )
    }

    @Test
    fun dubaiWeekend() {
        val page = view.groupPage("g-dubai")!!
        assertEquals("6–8 Mar · 3 members · AED", page.subtitle)
        with(page.balance) {
            assertEquals(Standing.Settled, standing)
            assertEquals("Settled", amount)
            assertEquals("You paid Kabir AED 60 on 14 Mar", caption)
            assertNull(settle)
        }
        assertEquals(
            listOf("Paid AED 540 · Share AED 600", "Paid AED 960 · Share AED 600", "Paid AED 300 · Share AED 600"),
            page.members.map { it.subtitle },
        )
        assertTrue(page.members.all { it.standing == Standing.Settled })
        assertEquals(listOf("Total AED 1,800 · ≈ ₹41,118 at saved rates"), page.notes)
        assertEquals(
            listOf(
                listOf("Dinner at the Marina", "Meera paid · Your share AED 100", "AED 300", "≈ ₹6,870 · ₹22.90 per AED"),
                listOf("Desert safari", "You paid · Your share AED 180", "AED 540", "≈ ₹12,312 · ₹22.80 per AED"),
                listOf("Hotel", "Kabir paid · Your share AED 320", "AED 960", "≈ ₹21,936 · ₹22.85 per AED"),
            ),
            page.days.flatMap { it.rows }.map { listOf(it.expense.title, it.subtitle, it.amount, it.detail) },
        )
    }

    @Test
    fun aNewGroupIsEmpty() {
        val trek = Demo.load("eshaClaimsPayment", "weekendTrek").groupPage("g-trek")!!
        assertEquals("Trip · 4 members · INR", trek.subtitle)
        assertTrue(trek.isEmpty)
        with(trek.balance) {
            assertEquals("₹0", amount)
            assertNull(caption)
            assertNull(settle)
            assertTrue(showsDisabledAction)
        }
    }

    @Test
    fun leaveIsBlockedWhileYouOwe() {
        val goa = view.leaveCheck("g-goa") as LeaveCheck.Blocked
        assertEquals(
            "You owe ₹1,400 in Goa Trip. Settle up with Kabir first, then you can leave.",
            goa.message,
        )
        assertEquals(GroupSettle.Pay("p-kabir", 140_000, "INR", "g-goa"), goa.settle)
        val dubai = view.leaveCheck("g-dubai") as LeaveCheck.Confirm
        assertEquals("Leave Dubai Weekend?", dubai.title)
    }

    @Test
    fun groupSettings() {
        val me = MemberIdentity("Arjun Mehta", "arjun@okaxis", "arjun")
        val goa = view.groupSettingsPage("g-goa", me)!!
        assertEquals("Fri 2 Oct", goa.settleBy)
        assertEquals("INR ₹", goa.currency)
        assertEquals("None", goa.recurring)
        assertEquals(
            listOf(
                "Arjun Mehta (you)" to "arjun@okaxis",
                "Kabir Singh" to "kabir@okaxis",
                "Priya Sharma" to "priya@okhdfcbank",
                "Esha Kapoor" to "esha@okicici",
                "Dev Malhotra" to "dev@oksbi",
            ),
            goa.members.map { it.name to it.subtitle },
        )
        assertEquals("3 rules", view.groupSettingsPage("g-flat302", me)!!.recurring)
        assertEquals("AED", view.groupSettingsPage("g-dubai", me)!!.currency)
        assertEquals("None", view.groupSettingsPage("g-dubai", me)!!.settleBy)
    }
}
