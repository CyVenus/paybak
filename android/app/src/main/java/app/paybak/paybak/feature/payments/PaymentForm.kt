package app.paybak.paybak.feature.payments

import androidx.compose.runtime.saveable.Saver
import app.paybak.paybak.domain.addrecord.AmountEntry
import app.paybak.paybak.domain.addrecord.PaymentContext
import app.paybak.paybak.domain.addrecord.currencyOf
import app.paybak.paybak.domain.addrecord.openBalance
import app.paybak.paybak.domain.addrecord.recordPaymentPrefill
import app.paybak.paybak.domain.calc.LedgerView
import app.paybak.paybak.domain.calc.Rates
import app.paybak.paybak.domain.model.Day
import app.paybak.paybak.domain.model.LedgerJson
import app.paybak.paybak.domain.model.ME
import app.paybak.paybak.domain.model.Payment
import app.paybak.paybak.domain.model.PaymentDraft
import app.paybak.paybak.domain.model.PaymentMethod
import app.paybak.paybak.navigation.RecordPaymentArgs
import kotlinx.serialization.Serializable

/**
 * What the Record payment form holds (record-lend-group §2): the other person, which way the money
 * went, the typed amount, method, context, date and proof.
 *
 * @param amountEdited False while the amount is still the prefilled open balance, so a new person
 *   or context refills it.
 */
@Serializable
data class PaymentForm(
    val friendId: String? = null,
    val youPaid: Boolean = true,
    val amount: String = "",
    val amountEdited: Boolean = false,
    val currency: String,
    val method: PaymentMethod = PaymentMethod.Cash,
    val groupId: String? = null,
    val loanId: String? = null,
    val expenseId: String? = null,
    val date: Day,
    val proof: String? = null,
) {
    val context: PaymentContext
        get() = PaymentContext(groupId, loanId)

    val total: Long
        get() = AmountEntry.minor(amount, currency)

    val fromId: String?
        get() = if (youPaid) ME else friendId

    val toId: String?
        get() = if (youPaid) friendId else ME

    /**
     * The person picked on the side that was tapped. You there puts the friend on the other side;
     * anyone else becomes the friend, with you on the other side. A different friend starts over
     * with nothing to file it under.
     */
    fun picked(personId: String, tappedFrom: Boolean): PaymentForm {
        if (personId == ME) return copy(youPaid = tappedFrom)
        val sameFriend = personId == friendId
        return copy(
            friendId = personId,
            youPaid = !tappedFrom,
            groupId = groupId.takeIf { sameFriend },
            loanId = loanId.takeIf { sameFriend },
            expenseId = expenseId.takeIf { sameFriend },
        )
    }

    /** Refills the amount and currency from the open balance, unless the amount was typed. */
    fun refilled(view: LedgerView): PaymentForm {
        val friend = friendId ?: return this
        val currency = view.currencyOf(context)
        if (amountEdited) return copy(currency = currency)
        val open = view.openBalance(friend, context)
        val owed = if (youPaid) -open else open
        return copy(currency = currency, amount = AmountEntry.text(owed.coerceAtLeast(0), currency))
    }

    fun toDraft(rates: Rates, defaultCurrency: String): PaymentDraft =
        PaymentDraft(
            fromId = checkNotNull(fromId),
            toId = checkNotNull(toId),
            amount = total,
            currency = currency,
            rate = if (currency == defaultCurrency) null else rates.rate(currency, defaultCurrency),
            method = method,
            date = date,
            groupId = groupId,
            loanId = loanId,
            expenseId = expenseId,
            proof = proof,
        )

    companion object {
        /** Edit mode: the pending payment as it was recorded. */
        fun of(payment: Payment): PaymentForm {
            val youPaid = payment.fromId == ME
            return PaymentForm(
                friendId = if (youPaid) payment.toId else payment.fromId,
                youPaid = youPaid,
                amount = AmountEntry.text(payment.amount, payment.currency),
                amountEdited = true,
                currency = payment.currency,
                method = payment.method,
                groupId = payment.groupId,
                loanId = payment.loanId,
                expenseId = payment.expenseId,
                date = payment.date,
                proof = payment.proof,
            )
        }

        /**
         * A new payment: prefilled by [args] (a Settle row, a friend page, a loan's repayment),
         * otherwise from the debt you owe most recently (record-lend-group §2.4).
         */
        fun new(args: RecordPaymentArgs, view: LedgerView, today: Day): PaymentForm {
            val prefill =
                if (args.fromId == null && args.toId == null) view.recordPaymentPrefill() else null
            val youPaid = args.fromId?.let { it == ME } ?: (args.toId != ME)
            val friend = prefill?.toId ?: if (youPaid) args.toId else args.fromId
            val form =
                PaymentForm(
                    friendId = friend?.takeIf { it != ME },
                    youPaid = youPaid,
                    amount =
                        (args.amount ?: prefill?.amount)?.let {
                            AmountEntry.text(it, args.currency ?: view.defaultCurrency)
                        } ?: "",
                    amountEdited = args.amount != null || prefill != null,
                    currency = args.currency ?: view.defaultCurrency,
                    method = args.method ?: PaymentMethod.Cash,
                    groupId = args.groupId ?: prefill?.context?.groupId,
                    loanId = args.loanId ?: prefill?.context?.loanId,
                    expenseId = args.expenseId,
                    date = today,
                )
            // An amount the route gives in its own currency stays in that currency.
            return if (args.amount != null && args.currency != null) form else form.refilled(view)
        }

        val Saver: Saver<PaymentForm, String> =
            Saver(
                save = { LedgerJson.encodeToString(serializer(), it) },
                restore = { LedgerJson.decodeFromString(serializer(), it) },
            )
    }
}
