package app.paybak.paybak.feature.settle

import app.paybak.paybak.domain.Demo
import app.paybak.paybak.domain.model.ME
import app.paybak.paybak.domain.model.PaymentMethod
import app.paybak.paybak.domain.model.ReminderContext
import app.paybak.paybak.domain.settle.settleUpPage
import app.paybak.paybak.navigation.RecordPaymentArgs
import app.paybak.paybak.navigation.Route
import org.junit.Assert.assertEquals
import org.junit.Test

/** Where Settle up's rows lead (screens-settle §3.3, §4.2) on the demo at Figma parity. */
class SettleRoutesTest {
    private val demo = Demo.load("eshaClaimsPayment")
    private val page = demo.settleUpPage()

    private fun row(friendId: String) = (page.pay + page.get).first { it.friendId == friendId }

    @Test
    fun settleOpensRecordPaymentPrefilledForKabir() {
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
            demo.settleRoute(row("p-kabir")),
        )
        assertEquals("g-flat302", demo.settleRoute(row("p-meera")).args.groupId)
    }

    @Test
    fun remindIsAboutTheOneDebt() {
        assertEquals(
            Route.Remind("p-rohan", ReminderContext(expenseId = "e-movie")),
            row("p-rohan").remindRoute(),
        )
        assertEquals(Route.Friend("p-priya"), row("p-priya").bodyRoute())
    }

    @Test
    fun aPendingRowOpensItsPayment() {
        val kabir =
            Demo.load("eshaClaimsPayment", "paymentToKabirPending").settleUpPage().pay.first()
        assertEquals(Route.Payment("pay-me-kabir"), kabir.bodyRoute())
    }
}
