package app.paybak.paybak.data.ledger.actions

import app.paybak.paybak.data.ledger.LedgerRepository
import app.paybak.paybak.domain.actions.addLoan
import app.paybak.paybak.domain.actions.cancelPayment
import app.paybak.paybak.domain.actions.confirmPayment
import app.paybak.paybak.domain.actions.markNotReceived
import app.paybak.paybak.domain.actions.recordPayment
import app.paybak.paybak.domain.actions.updateLoan
import app.paybak.paybak.domain.actions.updatePayment
import app.paybak.paybak.domain.model.LoanDraft
import app.paybak.paybak.domain.model.PaymentDraft

/** Payment and loan actions of the store (domain.md §11). */

/** Records a payment (pending unless you received it) and returns its id. */
fun LedgerRepository.recordPayment(draft: PaymentDraft): String = mutateReturning {
    it.recordPayment(draft, context())
}

fun LedgerRepository.updatePayment(id: String, draft: PaymentDraft) = mutate {
    it.updatePayment(id, draft, context())
}

fun LedgerRepository.cancelPayment(id: String) = mutate { it.cancelPayment(id) }

fun LedgerRepository.confirmPayment(id: String) = mutate { it.confirmPayment(id, context()) }

fun LedgerRepository.markNotReceived(id: String, note: String?) = mutate {
    it.markNotReceived(id, note, context())
}

/** Adds a loan and returns its id. */
fun LedgerRepository.addLoan(draft: LoanDraft): String = mutateReturning {
    it.addLoan(draft, context())
}

fun LedgerRepository.updateLoan(id: String, draft: LoanDraft) = mutate { it.updateLoan(id, draft) }
