package app.paybak.paybak.data.ledger.actions

import app.paybak.paybak.data.ledger.LedgerRepository
import app.paybak.paybak.domain.actions.addRecurringRule
import app.paybak.paybak.domain.actions.deleteRecurringRule
import app.paybak.paybak.domain.actions.markAllInboxRead
import app.paybak.paybak.domain.actions.markInboxRead
import app.paybak.paybak.domain.actions.sendReminder
import app.paybak.paybak.domain.actions.updateRecurringRule
import app.paybak.paybak.domain.model.RecurringRule
import app.paybak.paybak.domain.model.ReminderContext
import app.paybak.paybak.domain.model.ReminderTone
import app.paybak.paybak.domain.model.ReminderVia

/** Reminders, the inbox and recurring rules (domain.md §11). */

/** Logs a manual reminder (Remind sheet) and returns its id. */
fun LedgerRepository.sendReminder(
    personId: String,
    amount: Long,
    context: ReminderContext?,
    tone: ReminderTone?,
    message: String?,
    via: ReminderVia,
): String = mutateReturning {
    it.sendReminder(personId, amount, context, tone, message, via, context())
}

fun LedgerRepository.markInboxRead(id: String) = mutate { it.markInboxRead(id) }

fun LedgerRepository.markAllInboxRead() = mutate { it.markAllInboxRead() }

fun LedgerRepository.addRecurringRule(rule: RecurringRule) = mutate { it.addRecurringRule(rule) }

fun LedgerRepository.updateRecurringRule(id: String, change: (RecurringRule) -> RecurringRule) =
    mutate {
        it.updateRecurringRule(id, change)
    }

fun LedgerRepository.deleteRecurringRule(id: String) = mutate { it.deleteRecurringRule(id) }
