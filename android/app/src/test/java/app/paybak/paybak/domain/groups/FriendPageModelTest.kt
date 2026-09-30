package app.paybak.paybak.domain.groups

import app.paybak.paybak.domain.Demo
import app.paybak.paybak.domain.calc.groupSummaries
import app.paybak.paybak.domain.model.ReminderContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** The friend page (screens-groups §6) from the demo at Figma parity. */
class FriendPageModelTest {
    private val view = Demo.load("eshaClaimsPayment")

    private fun page(id: String) = view.friendPageModel(id, view.groupSummaries())!!

    @Test
    fun rohanOwesYouAndIsOverdue() {
        val rohan = page("p-rohan")
        assertEquals("rohan@ybl", rohan.subtitle)
        with(rohan.balance!!) {
            assertEquals("Rohan owes you", label)
            assertEquals("+₹800", amount)
            assertEquals("Movie tickets · Due Sun 27 Sep", caption)
            assertEquals("Overdue 3 days", overdue)
        }
        assertEquals("Last reminder sent today.", rohan.lastReminder)
        assertEquals(ReminderContext(expenseId = "e-movie"), rohan.remindContext)
        assertEquals(
            listOf(
                listOf("Movie tickets", "You paid · Rohan owes ₹800", "₹1,600", "20 Sep", true),
                listOf("Farewell dinner", "College Gang · Settled", "₹9,000", "12 Mar", false),
            ),
            rohan.history.map { listOf(it.title, it.subtitle, it.amount, it.date, it.open) },
        )
        assertEquals(
            listOf("College Gang" to "Settled", "Build a Drone" to "You’re settled"),
            rohan.groups.map { it.group.name to it.status },
        )
        assertNull(rohan.groups[1].budget)
    }

    @Test
    fun youOweKabir() {
        with(page("p-kabir").balance!!) {
            assertEquals("You owe Kabir", label)
            assertEquals("−₹1,400", amount)
            assertEquals("Goa Trip · Due Fri 2 Oct", caption)
            assertNull(overdue)
        }
        assertEquals(PaymentContext(groupId = "g-goa"), page("p-kabir").paymentContext)
        assertNull(page("p-kabir").lastReminder)
    }

    @Test
    fun aGuestWithNothingSharedHasNoBalance() {
        val ananya = page("p-ananya")
        assertNull(ananya.subtitle)
        assertNull(ananya.balance)
        assertEquals(emptyList<FriendHistoryRow>(), ananya.history)
        assertEquals(
            "Invite Ananya to Paybak" to
                "Ananya isn’t on Paybak yet. You can still split with her. When she joins with " +
                    "the same phone or email, her history moves to her account.",
            inviteNotice(ananya.person),
        )
    }
}
