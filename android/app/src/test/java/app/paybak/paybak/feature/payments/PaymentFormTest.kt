package app.paybak.paybak.feature.payments

import app.paybak.paybak.domain.Demo
import app.paybak.paybak.domain.model.ME
import app.paybak.paybak.navigation.RecordPaymentArgs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Record payment's form (record-lend-group §2.4): the route's prefill and a new friend. */
class PaymentFormTest {
    private val view = Demo.load("eshaClaimsPayment")

    @Test
    fun aRouteAmountStaysInItsCurrencyInAForeignGroup() {
        val args =
            RecordPaymentArgs(
                fromId = ME,
                toId = "p-kabir",
                amount = 137_000,
                currency = "INR",
                groupId = "g-dubai",
            )
        val form = PaymentForm.new(args, view, view.today)
        assertEquals("INR", form.currency)
        assertEquals("1370", form.amount)
        assertEquals("g-dubai", form.groupId)
        // Without an amount of its own, the form follows the group's currency.
        val open = PaymentForm.new(args.copy(amount = null, currency = null), view, view.today)
        assertEquals("AED", open.currency)
    }

    @Test
    fun anotherFriendStartsOverWithNothingToFileItUnder() {
        val args =
            RecordPaymentArgs(
                fromId = "p-esha",
                toId = ME,
                amount = 70_000,
                currency = "INR",
                expenseId = "e-olive",
            )
        val form = PaymentForm.new(args, view, view.today)
        assertEquals("e-olive", form.expenseId)
        val dev = form.picked("p-dev", tappedFrom = true)
        assertEquals("p-dev", dev.friendId)
        assertEquals(false, dev.youPaid)
        assertNull(dev.expenseId)
        assertNull(dev.groupId)
        assertNull(dev.loanId)
        assertEquals("e-olive", form.picked("p-esha", tappedFrom = true).expenseId)
        assertEquals("e-olive", form.picked(ME, tappedFrom = false).expenseId)
    }
}
