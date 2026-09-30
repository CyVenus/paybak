package app.paybak.paybak.domain.actions

import app.paybak.paybak.domain.model.Ledger
import app.paybak.paybak.domain.model.RecurringRule
import app.paybak.paybak.domain.model.replacing

/** Recurring rules (domain.md §1.8). Deleting a rule stops it; its expenses stay. */
fun Ledger.addRecurringRule(rule: RecurringRule): Ledger =
    copy(recurringRules = recurringRules + rule)

fun Ledger.updateRecurringRule(id: String, change: (RecurringRule) -> RecurringRule): Ledger {
    requireNotNull(rule(id)) { "No rule $id" }
    return copy(recurringRules = recurringRules.replacing({ it.id == id }, change))
}

fun Ledger.deleteRecurringRule(id: String): Ledger =
    updateRecurringRule(id) { it.copy(active = false) }
