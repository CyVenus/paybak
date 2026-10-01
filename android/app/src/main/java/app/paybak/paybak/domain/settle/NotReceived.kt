package app.paybak.paybak.domain.settle

import app.paybak.paybak.domain.calc.LedgerView
import app.paybak.paybak.domain.calc.claimCard
import app.paybak.paybak.domain.calc.paymentFor
import app.paybak.paybak.domain.format.Money
import app.paybak.paybak.domain.model.ME
import app.paybak.paybak.domain.model.PaymentMethod
import app.paybak.paybak.domain.model.PaymentStatus

/**
 * The Not received sheet for a friend's payment claim (screens-settle §8.3): the question, the
 * claim's context line, the editable note to the payer and the helper under it.
 */
data class NotReceivedDraft(
    val paymentId: String,
    val title: String,
    val context: String,
    val note: String,
    val helper: String,
)

/** The sheet for [paymentId], or null once that claim is no longer waiting for your answer. */
fun LedgerView.notReceivedDraft(paymentId: String): NotReceivedDraft? {
    val payment = ledger.payment(paymentId) ?: return null
    if (payment.toId != ME || payment.status != PaymentStatus.Pending) return null
    val name = first(payment.fromId)
    val amount = Money.format(payment.amount, payment.currency)
    // "for Dinner at Olive Garden"; nothing for a payment that isn't for anything.
    val named = payment.expenseId != null || payment.groupId != null || payment.loanId != null
    val what = if (named) " for ${paymentFor(payment)}" else ""
    return NotReceivedDraft(
        paymentId,
        "Let $name know you haven’t received $amount?",
        claimCard(payment).detail,
        "Hi $name, I haven’t received $amount$what yet. " + checkPhrase(payment.method),
        "$name still owes you $amount until a payment is confirmed.",
    )
}

/** "Could you check your UPI app?"; cash and other payments have no app to check. */
private fun checkPhrase(method: PaymentMethod): String =
    when (method) {
        PaymentMethod.Upi -> "Could you check your UPI app?"
        PaymentMethod.Bank -> "Could you check your bank app?"
        PaymentMethod.Card -> "Could you check your card app?"
        PaymentMethod.Cash,
        PaymentMethod.Other -> "Could you check?"
    }
