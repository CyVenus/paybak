package app.paybak.paybak.feature.loans

import androidx.compose.runtime.saveable.Saver
import app.paybak.paybak.domain.addrecord.AmountEntry
import app.paybak.paybak.domain.addrecord.LoanSchedule
import app.paybak.paybak.domain.calc.Rates
import app.paybak.paybak.domain.model.Day
import app.paybak.paybak.domain.model.Frequency
import app.paybak.paybak.domain.model.Installments
import app.paybak.paybak.domain.model.LedgerJson
import app.paybak.paybak.domain.model.Loan
import app.paybak.paybak.domain.model.LoanDraft
import app.paybak.paybak.domain.model.ME
import kotlinx.serialization.Serializable

/**
 * What the Lend money form holds (record-lend-group §4): which way the money went, the amount, the
 * other person, reason and date, and either an installment plan or a single due date.
 *
 * @param firstDue Null follows the loan date (one period later) until it's picked.
 */
@Serializable
data class LoanForm(
    val lent: Boolean = true,
    val amount: String = "",
    val currency: String,
    val friendId: String? = null,
    val reason: String = "",
    val date: Day,
    val installments: Boolean = true,
    val count: Int = 3,
    val frequency: Frequency = Frequency.Monthly,
    val firstDue: Day? = null,
    val due: Day? = null,
) {
    val total: Long
        get() = AmountEntry.minor(amount, currency)

    val effectiveFirstDue: Day
        get() = firstDue ?: LoanSchedule.defaultFirstDue(date, frequency)

    fun toDraft(rates: Rates, defaultCurrency: String): LoanDraft {
        val friend = checkNotNull(friendId)
        return LoanDraft(
            lenderId = if (lent) ME else friend,
            borrowerId = if (lent) friend else ME,
            amount = total,
            currency = currency,
            rate = if (currency == defaultCurrency) null else rates.rate(currency, defaultCurrency),
            reason = reason,
            date = date,
            installments =
                if (installments) Installments(count, frequency, effectiveFirstDue) else null,
            dueDate = if (installments) null else due,
        )
    }

    companion object {
        fun of(loan: Loan): LoanForm =
            LoanForm(
                lent = loan.lenderId == ME,
                amount = AmountEntry.text(loan.amount, loan.currency),
                currency = loan.currency,
                friendId = loan.friendId,
                reason = loan.reason.orEmpty(),
                date = loan.date,
                installments = loan.installments != null,
                count = loan.installments?.count ?: 3,
                frequency = loan.installments?.frequency ?: Frequency.Monthly,
                firstDue = loan.installments?.firstDue,
                due = loan.dueDate,
            )

        val Saver: Saver<LoanForm, String> =
            Saver(
                save = { LedgerJson.encodeToString(serializer(), it) },
                restore = { LedgerJson.decodeFromString(serializer(), it) },
            )
    }
}
