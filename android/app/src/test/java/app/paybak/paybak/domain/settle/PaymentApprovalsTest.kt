package app.paybak.paybak.domain.settle

import app.paybak.paybak.domain.Demo
import app.paybak.paybak.domain.actions.ActionContext
import app.paybak.paybak.domain.actions.cancelPayment
import app.paybak.paybak.domain.actions.confirmPayment
import app.paybak.paybak.domain.actions.markNotReceived
import app.paybak.paybak.domain.actions.recordPayment
import app.paybak.paybak.domain.model.Ledger
import app.paybak.paybak.domain.model.ME
import app.paybak.paybak.domain.model.PaymentDraft
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The payer's side of a confirm ([PaymentApprovals]): which changes count as a payment of yours
 * just approved or just recorded, and the payment-approved scene's headline.
 */
class PaymentApprovalsTest {
    private val ctx = ActionContext(Demo.load().now, Demo.zone, "INR")

    @Test
    fun confirmingYourPaymentApprovesIt() {
        val before = Demo.load("paymentToMeeraPending").ledger
        val after = before.confirmPayment("pay-me-meera", ctx)
        val approved = PaymentApprovals.approved(before, after)
        assertEquals(listOf("pay-me-meera"), approved.map { it.id })
        assertEquals("Meera confirmed ₹450", PaymentApprovals.headline(approved, after))
    }

    @Test
    fun confirmingAClaimToYouIsNoApproval() {
        val before = Demo.load("eshaClaimsPayment").ledger
        val after = before.confirmPayment("pay-esha-olive", ctx)
        assertTrue(PaymentApprovals.approved(before, after).isEmpty())
    }

    @Test
    fun otherChangesAreNoApprovals() {
        val pending = Demo.load("paymentToMeeraPending").ledger
        assertTrue(PaymentApprovals.approved(pending, pending.cancelPayment("pay-me-meera")).isEmpty())
        val notReceived = pending.markNotReceived("pay-me-meera", "Not yet", ctx)
        assertTrue(PaymentApprovals.approved(pending, notReceived).isEmpty())

        val confirmed = Demo.load("paymentToMeeraConfirmed").ledger
        // Confirmed before the change, or never pending (a whole ledger loaded at once).
        assertTrue(PaymentApprovals.approved(confirmed, confirmed).isEmpty())
        assertTrue(PaymentApprovals.approved(Ledger(), confirmed).isEmpty())
    }

    @Test
    fun recordingAPaymentToAFriend() {
        val before = Demo.load().ledger
        val (paid, id) = before.recordPayment(PaymentDraft(ME, "p-meera", 45_000), ctx)
        assertEquals(listOf(id), PaymentApprovals.recorded(before, paid).map { it.id })
        assertTrue(PaymentApprovals.recorded(paid, paid).isEmpty())

        // A claim a friend records is for you to confirm, not to wait on.
        val (claimed, _) =
            before.recordPayment(PaymentDraft("p-esha", ME, 70_000, recordedBy = "p-esha"), ctx)
        assertTrue(PaymentApprovals.recorded(before, claimed).isEmpty())
    }

    @Test
    fun headlineForSeveralPayments() {
        val ledger = Demo.load("paymentToMeeraPending", "paymentToKabirPending").ledger
        val meera = ledger.payment("pay-me-meera")!!
        val kabir = ledger.payment("pay-me-kabir")!!
        val meeraAgain = meera.copy(id = "pay-me-meera-2", amount = 20_000)

        assertEquals(
            "Meera confirmed ₹650",
            PaymentApprovals.headline(listOf(meera, meeraAgain), ledger),
        )
        assertEquals(
            "Meera and Kabir confirmed your payments",
            PaymentApprovals.headline(listOf(meera, kabir), ledger),
        )
        assertEquals(
            "Meera confirmed your payments",
            PaymentApprovals.headline(listOf(meera, meeraAgain.copy(currency = "EUR")), ledger),
        )
    }
}
