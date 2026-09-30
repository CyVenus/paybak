package app.paybak.paybak.feature.settle

import app.paybak.paybak.domain.calc.LedgerView
import app.paybak.paybak.domain.model.ME
import app.paybak.paybak.domain.model.PaymentMethod
import app.paybak.paybak.domain.model.ReminderContext
import app.paybak.paybak.domain.settle.SettleUpRow
import app.paybak.paybak.domain.settle.reminderContext
import app.paybak.paybak.navigation.RecordPaymentArgs
import app.paybak.paybak.navigation.Route

/*
 * Where a Settle up row leads (screens-settle §3.3, §4.2). Pure, so the JVM tests check them
 * against the demo.
 */

/**
 * Record payment prefilled from the plan (settleRecordKabir): you pay [toId] [amount], by UPI when
 * they have an ID, filed under [context]'s group, expense or loan.
 */
internal fun LedgerView.recordPaymentTo(
    toId: String,
    amount: Long,
    currency: String,
    context: ReminderContext?,
): Route.RecordPayment =
    Route.RecordPayment(
        RecordPaymentArgs(
            fromId = ME,
            toId = toId,
            amount = amount,
            currency = currency,
            method = PaymentMethod.Upi.takeIf { person(toId)?.upi != null },
            groupId = context?.groupId,
            loanId = context?.loanId,
            expenseId = context?.expenseId,
        )
    )

/** Settle: pay the row's amount, filed under its earliest-due debt ("For Goa Trip"). */
internal fun LedgerView.settleRoute(row: SettleUpRow): Route.RecordPayment =
    recordPaymentTo(row.friendId, row.row.amount, defaultCurrency, row.row.lead?.reminderContext)

/** Remind: about the one debt, or about everything when they owe you for several things. */
internal fun SettleUpRow.remindRoute(): Route.Remind =
    Route.Remind(friendId, row.items.singleOrNull()?.reminderContext)

/** The row body: the payment waiting for its confirmation, otherwise the friend's page. */
internal fun SettleUpRow.bodyRoute(): Route =
    pendingPaymentId?.let(Route::Payment) ?: Route.Friend(friendId)
