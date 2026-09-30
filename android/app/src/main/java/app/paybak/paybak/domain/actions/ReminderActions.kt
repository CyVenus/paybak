package app.paybak.paybak.domain.actions

import app.paybak.paybak.domain.model.Ledger
import app.paybak.paybak.domain.model.ME
import app.paybak.paybak.domain.model.Reminder
import app.paybak.paybak.domain.model.ReminderContext
import app.paybak.paybak.domain.model.ReminderTone
import app.paybak.paybak.domain.model.ReminderVia
import app.paybak.paybak.domain.model.newId

/** A manual reminder from the Remind sheet ("Send in Paybak" or "Share…"). */
fun Ledger.sendReminder(
    personId: String,
    amount: Long,
    context: ReminderContext?,
    tone: ReminderTone?,
    message: String?,
    via: ReminderVia,
    ctx: ActionContext,
): Pair<Ledger, String> {
    val id = newId()
    val reminder =
        Reminder(
            id = id,
            toId = personId,
            fromId = ME,
            amount = amount,
            currency = ctx.defaultCurrency,
            expenseId = context?.expenseId,
            groupId = context?.groupId,
            loanId = context?.loanId,
            installment = context?.installment,
            sentAt = ctx.at,
            automatic = false,
            message = message,
            tone = tone,
            via = via,
        )
    return copy(reminders = reminders + reminder) to id
}

fun Ledger.markInboxRead(id: String): Ledger =
    copy(inbox = inbox.map { if (it.id == id) it.copy(read = true) else it })

fun Ledger.markAllInboxRead(): Ledger = copy(inbox = inbox.map { it.copy(read = true) })
