package app.paybak.paybak.feature.home

import app.paybak.paybak.domain.Demo
import app.paybak.paybak.domain.calc.DueAction
import app.paybak.paybak.domain.calc.DueSoonRow
import app.paybak.paybak.domain.calc.ObligationKind
import app.paybak.paybak.domain.calc.dueSoon
import app.paybak.paybak.domain.calc.homeSummary
import app.paybak.paybak.domain.model.ME
import app.paybak.paybak.domain.model.PaymentMethod
import app.paybak.paybak.domain.model.ReminderContext
import app.paybak.paybak.navigation.RecordPaymentArgs
import app.paybak.paybak.navigation.Route
import org.junit.Assert.assertEquals
import org.junit.Test

/** Where Home's rows lead on the demo (app-architecture §2.3, screens-home-v2 §2.2). */
class HomeLinksTest {
    private val demo = Demo.load()

    @Test
    fun rohanRemindsAboutTheMovieTickets() {
        val rohan = demo.dueSoon().first()
        assertEquals("p-rohan", rohan.id)
        assertEquals(
            Route.Remind("p-rohan", ReminderContext(expenseId = "e-movie")),
            rohan.actionRoute(demo),
        )
        assertEquals(Route.Friend("p-rohan"), rohan.bodyRoute(demo))
    }

    @Test
    fun goaTripSettlesWithKabirDirectly() {
        val goa = demo.dueSoon()[1]
        assertEquals("g-goa", goa.id)
        assertEquals(
            Route.RecordPayment(
                RecordPaymentArgs(
                    fromId = ME,
                    toId = "p-kabir",
                    amount = 140_000,
                    currency = "INR",
                    method = PaymentMethod.Upi,
                    groupId = "g-goa",
                )
            ),
            goa.actionRoute(demo),
        )
        assertEquals(Route.Group("g-goa"), goa.bodyRoute(demo))
    }

    @Test
    fun aLoanRemindsAboutItsFirstInstallment() {
        val view = Demo.load("lendDev")
        val item =
            view.openItems().first { it.kind == ObligationKind.Loan && it.friendId == "p-dev" }
        val row =
            DueSoonRow(
                title = "Dev",
                detail = item.title,
                amount = item.amount,
                badge = "",
                overdue = false,
                action = DueAction.Remind,
                due = item.due!!,
                obligation = item,
            )
        assertEquals("p-dev", row.id)
        assertEquals(
            Route.Remind("p-dev", ReminderContext(loanId = "l-dev-laptop", installment = 1)),
            row.actionRoute(view),
        )
        assertEquals(Route.Friend("p-dev"), row.bodyRoute(view))
    }

    @Test
    fun recentRowsOpenTheirDetail() {
        assertEquals(
            listOf(
                Route.Expense("e-olive"),
                Route.Payment("pay-priya-groceries"),
                Route.Expense("e-flat-elec-09"),
            ),
            demo.homeSummary().recent.map { it.route() },
        )
    }
}
