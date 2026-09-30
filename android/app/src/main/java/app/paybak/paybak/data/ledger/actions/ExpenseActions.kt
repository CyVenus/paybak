package app.paybak.paybak.data.ledger.actions

import app.paybak.paybak.data.ledger.LedgerRepository
import app.paybak.paybak.domain.actions.addComment
import app.paybak.paybak.domain.actions.addExpense
import app.paybak.paybak.domain.actions.deleteExpense
import app.paybak.paybak.domain.actions.enterDraftAmount
import app.paybak.paybak.domain.actions.flagExpense
import app.paybak.paybak.domain.actions.removeFlag
import app.paybak.paybak.domain.actions.resolveFlag
import app.paybak.paybak.domain.actions.restoreExpense
import app.paybak.paybak.domain.actions.updateExpense
import app.paybak.paybak.domain.model.ExpenseDraft
import app.paybak.paybak.domain.model.ME

/**
 * Expense actions of the store (domain.md §11), at the clock's now. Validation failures throw
 * `LedgerRuleException` with user-facing copy.
 */

/** Saves a new expense and returns its id. */
fun LedgerRepository.addExpense(draft: ExpenseDraft): String = mutateReturning {
    it.addExpense(draft, context())
}

fun LedgerRepository.updateExpense(id: String, draft: ExpenseDraft) = mutate {
    it.updateExpense(id, draft, context())
}

fun LedgerRepository.deleteExpense(id: String) = mutate { it.deleteExpense(id, context()) }

fun LedgerRepository.restoreExpense(id: String) = mutate { it.restoreExpense(id, context()) }

fun LedgerRepository.addComment(expenseId: String, text: String, by: String = ME) = mutate {
    it.addComment(expenseId, text, context(), by)
}

fun LedgerRepository.flagExpense(id: String, note: String, by: String = ME) = mutate {
    it.flagExpense(id, by, note, context())
}

fun LedgerRepository.removeFlag(id: String, by: String = ME) = mutate {
    it.removeFlag(id, context(), by)
}

fun LedgerRepository.resolveFlag(id: String) = mutate { it.resolveFlag(id, context()) }

/** Creates the expense for a recurring draft and returns its id. */
fun LedgerRepository.enterDraftAmount(draftId: String, amount: Long): String = mutateReturning {
    it.enterDraftAmount(draftId, amount, context())
}
