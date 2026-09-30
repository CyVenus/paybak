package app.paybak.paybak.domain.actions

import app.paybak.paybak.domain.model.Ledger
import app.paybak.paybak.domain.model.Loan
import app.paybak.paybak.domain.model.LoanDraft
import app.paybak.paybak.domain.model.ME
import app.paybak.paybak.domain.model.newId
import app.paybak.paybak.domain.model.replacing

/** Adds an IOU (domain.md §1.6); repayments go through `recordPayment` with its id. */
fun Ledger.addLoan(draft: LoanDraft, ctx: ActionContext): Pair<Ledger, String> {
    ensure(draft.amount > 0) { "Enter an amount." }
    ensure(ME == draft.lenderId || ME == draft.borrowerId) { "One side of a loan is you." }
    val id = draft.id ?: newId()
    val currency = draft.currency ?: ctx.defaultCurrency
    val loan =
        Loan(
            id = id,
            lenderId = draft.lenderId,
            borrowerId = draft.borrowerId,
            amount = draft.amount,
            currency = currency,
            rate = draft.rate.takeIf { currency != ctx.defaultCurrency },
            reason = draft.reason?.trim()?.ifEmpty { null },
            date = draft.date ?: ctx.today,
            installments = draft.installments,
            dueDate = draft.dueDate,
            createdAt = ctx.at,
            createdBy = ME,
        )
    return copy(loans = loans + loan) to id
}

/** Lend money in edit mode. */
fun Ledger.updateLoan(id: String, draft: LoanDraft): Ledger {
    requireNotNull(loan(id)) { "No loan $id" }
    return copy(
        loans =
            loans.replacing({ it.id == id }) {
                it.copy(
                    lenderId = draft.lenderId,
                    borrowerId = draft.borrowerId,
                    amount = draft.amount,
                    reason = draft.reason?.trim()?.ifEmpty { null },
                    date = draft.date ?: it.date,
                    installments = draft.installments,
                    dueDate = draft.dueDate,
                )
            }
    )
}
