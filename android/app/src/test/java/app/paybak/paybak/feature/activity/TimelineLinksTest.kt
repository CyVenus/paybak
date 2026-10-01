package app.paybak.paybak.feature.activity

import app.paybak.paybak.domain.Demo
import app.paybak.paybak.domain.calc.TimelineKind
import app.paybak.paybak.domain.calc.timeline
import app.paybak.paybak.navigation.Route
import app.paybak.paybak.ui.components.PbAvatarContent
import app.paybak.paybak.ui.components.PbPeepHead
import app.paybak.paybak.ui.icons.PbIcon
import org.junit.Assert.assertEquals
import org.junit.Test

/** Where the demo timeline's rows lead and what they show (activity §3.5, §3.9). */
class TimelineLinksTest {
    private val demo = Demo.load("eshaClaimsPayment")
    private val events = demo.timeline()

    private fun event(title: String) = events.first { it.title == title }

    @Test
    fun expenseRowsOpenTheExpense() {
        assertEquals(
            Route.Expense("e-goa-villa"),
            demo.route(event("Kabir changed Villa (3 nights)")),
        )
        assertEquals(
            Route.Expense("e-olive"),
            demo.route(event("You added Dinner at Olive Garden")),
        )
        assertEquals(
            PbAvatarContent.Symbol(PbIcon.Bed),
            demo.leading(event("Kabir changed Villa (3 nights)")),
        )
    }

    @Test
    fun paymentRowsOpenThePaymentWithThePayer() {
        val priya = event("Priya paid you")
        assertEquals(Route.Payment("pay-priya-groceries"), demo.route(priya))
        assertEquals(PbAvatarContent.Art(PbPeepHead.Priya), demo.leading(priya))
    }

    @Test
    fun remindersOpenTheDebtTheyAreAbout() {
        val reminder = events.first { it.kind == TimelineKind.ReminderSent }
        assertEquals("Reminder sent to Rohan", reminder.title)
        assertEquals(Route.Expense("e-movie"), demo.route(reminder))
        assertEquals(PbAvatarContent.Symbol(PbIcon.Bell), demo.leading(reminder))
    }

    @Test
    fun aDraftOpensItsGroupsRecurringExpenses() {
        assertEquals(
            Route.Recurring("g-flat302"),
            demo.route(event("Cooking gas draft created")),
        )
    }

    @Test
    fun projectPartsOpenTheProject() {
        val camera = event("Dev bought Camera")
        assertEquals("Build a Drone", camera.subtitle)
        assertEquals("₹7,500", camera.amount)
        assertEquals(Route.Project("pj-drone"), demo.route(camera))
        assertEquals(PbAvatarContent.Symbol(PbIcon.Drone), demo.leading(camera))
    }

    @Test
    fun rowTagsAreUnique() {
        val tags = events.map { it.rowTag("activity") }
        assertEquals(tags.size, tags.toSet().size)
        assertEquals(
            "activity.row.e-goa-villa.edited",
            event("Kabir changed Villa (3 nights)").rowTag("activity"),
        )
    }
}
