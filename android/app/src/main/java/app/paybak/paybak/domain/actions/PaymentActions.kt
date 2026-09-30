package app.paybak.paybak.domain.actions

import app.paybak.paybak.domain.calc.paymentFor
import app.paybak.paybak.domain.model.InboxItem
import app.paybak.paybak.domain.model.InboxParams
import app.paybak.paybak.domain.model.InboxType
import app.paybak.paybak.domain.model.Ledger
import app.paybak.paybak.domain.model.ME
import app.paybak.paybak.domain.model.Payment
import app.paybak.paybak.domain.model.PaymentDraft
import app.paybak.paybak.domain.model.PaymentStatus
import app.paybak.paybak.domain.model.newId
import app.paybak.paybak.domain.model.replacing

/**
 * Records a payment (domain.md §1.5): pending until the receiver confirms, unless the receiver is
 * the one recording it.
 */
fun Ledger.recordPayment(draft: PaymentDraft, ctx: ActionContext): Pair<Ledger, String> {
    ensure(draft.amount > 0) { "Enter an amount." }
    ensure(draft.fromId != draft.toId) { "Pick who paid and who received it." }
    val id = draft.id ?: newId()
    val currency = draft.groupId?.let(::group)?.currency ?: draft.currency ?: ctx.defaultCurrency
    val confirmed = draft.recordedBy == draft.toId
    val payment =
        Payment(
            id = id,
            fromId = draft.fromId,
            toId = draft.toId,
            amount = draft.amount,
            currency = currency,
            rate = draft.rate.takeIf { currency != ctx.defaultCurrency },
            method = draft.method,
            date = draft.date ?: ctx.today,
            groupId = draft.groupId,
            loanId = draft.loanId,
            expenseId = draft.expenseId,
            note = draft.note,
            proof = draft.proof,
            status = if (confirmed) PaymentStatus.Confirmed else PaymentStatus.Pending,
            recordedBy = draft.recordedBy,
            createdAt = ctx.at,
            confirmedAt = if (confirmed) ctx.at else null,
        )
    return copy(payments = payments + payment) to id
}

/** Edits a pending payment you recorded (Payment detail › Edit). */
fun Ledger.updatePayment(id: String, draft: PaymentDraft, ctx: ActionContext): Ledger =
    copy(
        payments =
            payments.replacing({ it.id == id }) {
                it.copy(
                    fromId = draft.fromId,
                    toId = draft.toId,
                    amount = draft.amount,
                    currency = draft.currency ?: it.currency,
                    method = draft.method,
                    date = draft.date ?: it.date,
                    groupId = draft.groupId,
                    loanId = draft.loanId,
                    expenseId = draft.expenseId,
                    note = draft.note,
                    proof = draft.proof,
                )
            }
    )

/** The recorder cancels a pending payment; it stays for history and counts nowhere. */
fun Ledger.cancelPayment(id: String): Ledger =
    setStatus(id) { it.copy(status = PaymentStatus.Cancelled) }

/** The receiver confirms: balances change; a payment to you adds a (read) inbox item. */
fun Ledger.confirmPayment(id: String, ctx: ActionContext): Ledger {
    val next = setStatus(id) { it.copy(status = PaymentStatus.Confirmed, confirmedAt = ctx.at) }
    val payment = next.payment(id)!!
    if (payment.toId != ME) return next
    val item =
        InboxItem(
            id = "n-confirmed-$id",
            type = InboxType.PaymentConfirmed,
            createdAt = ctx.at,
            read = true,
            params =
                InboxParams(
                    paymentId = id,
                    personId = payment.fromId,
                    amount = payment.amount,
                    currency = payment.currency,
                    title = ctx.view(next).paymentFor(payment),
                    method = payment.method,
                ),
        )
    return next.copy(inbox = next.inbox.filter { it.id != item.id } + item)
}

/** The receiver says it never arrived; the debt stays. */
fun Ledger.markNotReceived(id: String, note: String?, ctx: ActionContext): Ledger {
    val next = setStatus(id) { it.copy(status = PaymentStatus.NotReceived, notReceivedNote = note) }
    val payment = next.payment(id)!!
    if (payment.fromId != ME) return next
    // Your own payment bounced back (the friend's side is simulated): tell you about it.
    val item =
        InboxItem(
            id = "n-not-received-$id",
            type = InboxType.PaymentNotReceived,
            createdAt = ctx.at,
            params = InboxParams(paymentId = id, personId = payment.toId, note = note),
        )
    return next.copy(inbox = next.inbox.filter { it.id != item.id } + item)
}

private fun Ledger.setStatus(id: String, change: (Payment) -> Payment): Ledger {
    requireNotNull(payment(id)) { "No payment $id" }
    return copy(payments = payments.replacing({ it.id == id }, change))
}
