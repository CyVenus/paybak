package app.paybak.paybak.domain.addrecord

import app.paybak.paybak.domain.calc.LedgerView
import app.paybak.paybak.domain.calc.ObligationKind
import app.paybak.paybak.domain.calc.settlePlan
import app.paybak.paybak.domain.format.Dates
import app.paybak.paybak.domain.format.Money
import app.paybak.paybak.domain.model.ME
import app.paybak.paybak.domain.model.Payment
import app.paybak.paybak.domain.model.PaymentMethod
import app.paybak.paybak.domain.model.PaymentStatus

/**
 * What a payment settles: a group or project ([groupId]), a loan ([loanId]), or what's directly
 * between the two of you (neither).
 */
data class PaymentContext(val groupId: String? = null, val loanId: String? = null)

/** Record payment opened from the ＋ sheet: whom you pay, how much and what for. */
data class PaymentPrefill(val toId: String, val amount: Long, val context: PaymentContext)

/**
 * The open balance with [friendId] in [context], in its currency: positive when they owe you (after
 * Simplify debts in a group).
 */
fun LedgerView.openBalance(friendId: String, context: PaymentContext): Long =
    when {
        context.groupId != null ->
            groupPlan(context.groupId).sumOf {
                when {
                    it.debtorId == friendId && it.creditorId == ME -> it.amount
                    it.debtorId == ME && it.creditorId == friendId -> -it.amount
                    else -> 0L
                }
            }
        context.loanId != null -> loan(context.loanId)?.let { loanContext(it)?.amount } ?: 0
        else -> directContext(friendId)?.amount ?: 0
    }

/** The context's currency: the group's, the loan's, or the default one. */
fun LedgerView.currencyOf(context: PaymentContext): String =
    context.groupId?.let { group(it)?.currency }
        ?: context.loanId?.let { loan(it)?.currency }
        ?: defaultCurrency

/** The For row: "Flat 302", "Loan · Laptop repair", or "None". */
fun LedgerView.contextLabel(context: PaymentContext): String =
    context.groupId?.let { group(it)?.name }
        ?: context.loanId?.let { loan(it)?.let { loan -> "Loan · ${loan.title}" } }
        ?: "None"

/**
 * The debt Record payment starts from when opened from the ＋ sheet (record-lend-group §2.4): the
 * friend you owe most recently, the open amount and its context. Null when you owe nobody.
 */
fun LedgerView.recordPaymentPrefill(): PaymentPrefill? {
    val candidates =
        settlePlan().pay.mapNotNull { row ->
            val lead = row.lead ?: return@mapNotNull null
            val context =
                when (lead.kind) {
                    ObligationKind.Group,
                    ObligationKind.Project -> PaymentContext(groupId = lead.ref)
                    ObligationKind.Loan -> PaymentContext(loanId = lead.ref)
                    ObligationKind.Direct -> PaymentContext()
                }
            val since =
                when (lead.kind) {
                    ObligationKind.Loan -> loan(lead.ref)?.createdAt
                    ObligationKind.Direct -> expense(lead.ref)?.createdAt
                    else ->
                        liveExpenses()
                            .filter { it.groupId == lead.ref && it.payerId != ME }
                            .maxOfOrNull { it.createdAt }
                }
            val amount = -openBalance(row.friendId, context)
            if (amount <= 0) null
            else Triple(PaymentPrefill(row.friendId, amount, context), since, row)
        }
    return candidates.maxByOrNull { it.second ?: java.time.Instant.MIN }?.first
}

/** Record payment's copy (record-lend-group §2.4): the helper line and the summary. */
object PaymentCopy {
    /** "in cash", "by UPI" (designed); Bank, Card and Other are proposals (Other says nothing). */
    fun methodPhrase(method: PaymentMethod): String? =
        when (method) {
            PaymentMethod.Cash -> "in cash"
            PaymentMethod.Upi -> "by UPI"
            PaymentMethod.Bank -> "by bank transfer"
            PaymentMethod.Card -> "by card"
            PaymentMethod.Other -> null
        }

    /**
     * "You paid Meera ₹450 in cash for Flat 302.\nMeera will be asked to confirm. Paybak never
     * moves money." When a friend paid you, it counts at once.
     */
    fun summary(
        friendName: String,
        youPaid: Boolean,
        amount: String,
        method: PaymentMethod,
        forLabel: String?,
    ): String {
        val how = methodPhrase(method)?.let { " $it" }.orEmpty()
        val what = forLabel?.let { " for $it" }.orEmpty()
        return if (youPaid) {
            "You paid $friendName $amount$how$what.\n" +
                "$friendName will be asked to confirm. Paybak never moves money."
        } else {
            "$friendName paid you $amount$how$what.\nIt counts once you save. Paybak never moves money."
        }
    }

    /** "You owe Meera ₹450 in Flat 302", "Dev owes you ₹6,000 for Laptop repair". */
    fun helper(
        friendName: String,
        balance: Long,
        currency: String,
        where: String?,
        loan: Boolean,
    ): String? {
        if (balance == 0L) return null
        val amount = Money.format(kotlin.math.abs(balance), currency)
        val suffix = where?.let { if (loan) " for $it" else " in $it" }.orEmpty()
        return if (balance < 0) "You owe $friendName $amount$suffix"
        else "$friendName owes you $amount$suffix"
    }
}

/** The payment detail's copy (record-lend-group §3). */
data class PaymentDetail(
    val payment: Payment,
    val friendId: String,
    val youPaid: Boolean,
    val title: String,
    val meta: String,
    val forLabel: String?,
    val paidTo: String?,
    val statusTitle: String,
    val statusBody: String,
    val footnote: String?,
)

fun LedgerView.paymentDetail(paymentId: String): PaymentDetail? {
    val payment = ledger.payment(paymentId) ?: return null
    val youPaid = payment.fromId == ME
    val friendId = if (youPaid) payment.toId else payment.fromId
    val name = first(friendId)
    val context = PaymentContext(payment.groupId, payment.loanId)
    val forLabel =
        if (context.groupId != null || context.loanId != null) contextLabel(context)
        else payment.expenseId?.let { expense(it)?.title }
    val paidTo =
        when (payment.method) {
            PaymentMethod.Upi -> if (youPaid) person(friendId)?.upi else null
            else -> null
        }
    val (statusTitle, statusBody) =
        when (payment.status) {
            PaymentStatus.Pending ->
                if (youPaid) "Pending confirmation" to "Waiting for $name to confirm"
                else "Pending confirmation" to "Confirm once the money has arrived"
            PaymentStatus.Confirmed ->
                "Confirmed" to
                    "${if (youPaid) name else "You"} confirmed on ${Dates.day(localDate(payment.confirmedAt ?: payment.createdAt))}"
            PaymentStatus.NotReceived ->
                "Not received" to
                    listOfNotNull("$name says they haven’t received it", payment.notReceivedNote)
                        .joinToString("\n")
            PaymentStatus.Cancelled -> "Cancelled" to "You cancelled this payment"
        }
    return PaymentDetail(
        payment = payment,
        friendId = friendId,
        youPaid = youPaid,
        title = if (youPaid) "You paid $name" else "$name paid you",
        meta =
            listOfNotNull(
                    payment.method.label,
                    Dates.rowDate(payment.date, today),
                    forLabel,
                )
                .joinToString(" · "),
        forLabel = forLabel,
        paidTo = paidTo,
        statusTitle = statusTitle,
        statusBody = statusBody,
        footnote =
            if (payment.status == PaymentStatus.Pending && youPaid)
                "Your balance updates once $name confirms."
            else null,
    )
}

/**
 * The Cancel payment alert's message: "Meera won’t be asked to confirm. You’ll still owe her ₹450."
 * [stillOwed] is what you'd still owe in the payment's context.
 */
fun LedgerView.cancelPaymentMessage(payment: Payment, stillOwed: Long): String {
    val friend = person(payment.toId)
    val name = friend?.firstName ?: "They"
    val them =
        when (friend?.pronoun?.subject) {
            "she" -> "her"
            "he" -> "him"
            else -> name
        }
    val owe =
        if (stillOwed > 0) " You’ll still owe $them ${Money.format(stillOwed, payment.currency)}."
        else ""
    return "$name won’t be asked to confirm.$owe"
}
