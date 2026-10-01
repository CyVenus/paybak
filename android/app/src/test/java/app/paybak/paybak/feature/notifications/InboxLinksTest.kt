package app.paybak.paybak.feature.notifications

import app.paybak.paybak.domain.Demo
import app.paybak.paybak.domain.model.InboxType
import app.paybak.paybak.domain.model.ME
import app.paybak.paybak.domain.model.PaymentMethod
import app.paybak.paybak.domain.model.ReminderContext
import app.paybak.paybak.navigation.RecordPaymentArgs
import app.paybak.paybak.navigation.Route
import app.paybak.paybak.ui.components.PbAvatarContent
import app.paybak.paybak.ui.components.PbPeepHead
import app.paybak.paybak.ui.icons.PbIcon
import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Test

/** Where the demo inbox's rows lead (activity §6, app-architecture §2.4). */
class InboxLinksTest {
    private val demo = Demo.load("eshaClaimsPayment")

    private fun item(type: InboxType) =
        demo.ledger.inbox.filter { it.type == type }.maxBy { it.createdAt }

    @Test
    fun theKabirReminderRecordsThePaymentToHim() {
        val reminder = item(InboxType.PaymentReminder)
        assertEquals(
            InboxTarget.Open(
                Route.RecordPayment(
                    RecordPaymentArgs(
                        fromId = ME,
                        toId = "p-kabir",
                        amount = 140_000,
                        currency = "INR",
                        method = PaymentMethod.Upi,
                        groupId = "g-goa",
                    )
                )
            ),
            demo.target(reminder),
        )
        assertEquals(PbAvatarContent.Symbol(PbIcon.Calendar), demo.leading(reminder))
    }

    @Test
    fun theSummaryOpensThatMonthsInsights() {
        val summary = item(InboxType.MonthlySummary)
        assertEquals(InboxTarget.Insights(YearMonth.of(2026, 9)), demo.target(summary))
        assertEquals(PbAvatarContent.Symbol(PbIcon.Chart), demo.leading(summary))
    }

    @Test
    fun theOverdueAlertRemindsRohan() {
        val overdue = item(InboxType.PaymentOverdue)
        assertEquals(
            InboxTarget.Open(Route.Remind("p-rohan", ReminderContext(expenseId = "e-movie"))),
            demo.target(overdue),
        )
        assertEquals(PbAvatarContent.Art(PbPeepHead.Rohan), demo.leading(overdue))
    }

    @Test
    fun paymentAndExpenseNewsOpenTheirDetail() {
        assertEquals(
            InboxTarget.Open(Route.Payment("pay-priya-groceries")),
            demo.target(item(InboxType.PaymentConfirmed)),
        )
        val electricity =
            demo.ledger.inbox.first {
                it.type == InboxType.NewExpenseInGroup && it.params.title == "Electricity bill"
            }
        assertEquals(
            InboxTarget.Open(Route.Expense(electricity.params.expenseId!!)),
            demo.target(electricity),
        )
        assertEquals(PbAvatarContent.Symbol(PbIcon.Bolt), demo.leading(electricity))
    }
}
