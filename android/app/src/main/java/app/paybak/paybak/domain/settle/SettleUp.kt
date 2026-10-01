package app.paybak.paybak.domain.settle

import app.paybak.paybak.domain.calc.LedgerView
import app.paybak.paybak.domain.calc.Obligation
import app.paybak.paybak.domain.calc.ObligationKind
import app.paybak.paybak.domain.calc.SettlePlan
import app.paybak.paybak.domain.calc.SettleRow
import app.paybak.paybak.domain.calc.pendingPaymentTo
import app.paybak.paybak.domain.calc.settlePlan
import app.paybak.paybak.domain.format.Dates
import app.paybak.paybak.domain.format.Money
import app.paybak.paybak.domain.model.ReminderContext
import java.time.LocalDate

/**
 * A row of the Settle up plan (screens-settle §3): the unsigned amount, the badge ("Due Fri",
 * "Overdue 3 days", or "Pending" while a payment you recorded waits for its confirmation) and the
 * action. A pending row has no action; it opens that payment.
 */
data class SettleUpRow(
    val row: SettleRow,
    val name: String,
    val amount: String,
    val badge: String?,
    val overdue: Boolean,
) {
    val friendId: String
        get() = row.friendId

    /** The payment you recorded to this friend, still waiting for them to confirm. */
    val pendingPaymentId: String?
        get() = row.pendingPaymentId.takeIf { row.pay }
}

/** "2 payments to make" and "4 people owe you", with their rows; an empty section is hidden. */
data class SettleUpPage(val pay: List<SettleUpRow>, val get: List<SettleUpRow>) {
    val payTitle: String
        get() = if (pay.size == 1) "1 payment to make" else "${pay.size} payments to make"

    val getTitle: String
        get() = if (get.size == 1) "1 person owes you" else "${get.size} people owe you"

    val allSettled: Boolean
        get() = pay.isEmpty() && get.isEmpty()
}

/** Settle up for everyone ([settlePlan]) or for one group ([groupSettlePlan]). */
fun LedgerView.settleUpPage(plan: SettlePlan = settlePlan()): SettleUpPage =
    SettleUpPage(plan.pay.map(::settleUpRow), plan.get.map(::settleUpRow))

/**
 * The group's own plan from your side (§3.1 step 1): one row per transfer between you and a
 * member, in the default currency, ordered like the full plan.
 */
fun LedgerView.groupSettlePlan(groupId: String): SettlePlan {
    val order = ledger.people.map { it.id }
    val rows =
        contexts()
            .filter { it.ref == groupId && it.amount != 0L }
            .map { context ->
                val item = context.items.first()
                SettleRow(
                    context.friendId,
                    kotlin.math.abs(context.amount),
                    context.amount < 0,
                    context.title,
                    item.due,
                    item,
                    context.items,
                    pendingPaymentTo(context.friendId)?.id,
                )
            }
    val key = planOrder(order)
    return SettlePlan(
        rows.filter { it.pay }.sortedWith(key),
        rows.filterNot { it.pay }.sortedWith(key),
    )
}

/** Overdue first, then the earliest due date, then the friends' order (§3.1 step 6). */
private fun LedgerView.planOrder(order: List<String>): Comparator<SettleRow> =
    compareBy(
        { it.due?.isBefore(today) != true },
        { it.due ?: LocalDate.MAX },
        { order.indexOf(it.friendId) },
    )

private fun LedgerView.settleUpRow(row: SettleRow): SettleUpRow {
    val due = row.due
    val pending = row.pay && row.pendingPaymentId != null
    return SettleUpRow(
        row,
        first(row.friendId),
        Money.format(row.amount, defaultCurrency),
        badge = if (pending) PENDING else due?.let { Dates.dueBadge(it, today) },
        overdue = !pending && due != null && Dates.isOverdue(due, today),
    )
}

/** What a reminder or a payment about this debt is filed under: its expense, group or loan. */
val Obligation.reminderContext: ReminderContext
    get() =
        when (kind) {
            ObligationKind.Direct -> ReminderContext(expenseId = ref)
            ObligationKind.Group,
            ObligationKind.Project -> ReminderContext(groupId = ref)
            ObligationKind.Loan -> ReminderContext(loanId = ref, installment = installment)
        }

private const val PENDING = "Pending"
