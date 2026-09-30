package app.paybak.paybak.domain.calc

import app.paybak.paybak.domain.model.Expense
import app.paybak.paybak.domain.model.GroupKind
import app.paybak.paybak.domain.model.ME
import app.paybak.paybak.domain.model.Payment
import java.time.LocalDate

/**
 * One friend in Settle up and the breakdowns (§6.2–6.3): [amount] = |net|, [pay] = you pay them.
 * [lead] is the earliest-due open item (its title is the context, its due the badge).
 */
data class SettleRow(
    val friendId: String,
    val amount: Long,
    val pay: Boolean,
    val context: String,
    val due: LocalDate?,
    val lead: Obligation?,
    val items: List<Obligation>,
    val pendingPaymentId: String?,
)

/** "Payments to make" and "People who owe you". */
data class SettlePlan(val pay: List<SettleRow>, val get: List<SettleRow>)

fun LedgerView.settlePlan(): SettlePlan {
    val nets = friendNets()
    val items = openItems()
    val order = ledger.people.map { it.id }
    val rows =
        nets
            .filter { it.value != 0L }
            .map { (friend, net) ->
                val mine = items.filter { it.friendId == friend }
                val lead =
                    mine.filter { it.due != null }.minByOrNull { it.due!! } ?: mine.firstOrNull()
                SettleRow(
                    friend,
                    kotlin.math.abs(net),
                    net < 0,
                    lead?.title.orEmpty(),
                    lead?.due,
                    lead,
                    mine,
                    pendingPaymentTo(friend)?.id,
                )
            }
    val key =
        compareBy<SettleRow>(
            { !(it.due != null && it.due.isBefore(today)) },
            { it.due ?: LocalDate.MAX },
            { order.indexOf(it.friendId) },
        )
    return SettlePlan(
        rows.filter { it.pay }.sortedWith(key),
        rows.filterNot { it.pay }.sortedWith(key),
    )
}

/**
 * Groups whose You owe breakdown shows "{group} uses simplified debts, so you pay {name} directly."
 * (§6.2): you pay someone other than who paid for what you owe since you were last square there.
 */
fun LedgerView.simplifiedFootnoteGroups(): List<String> =
    ledger.groups.mapNotNull { group ->
        if (!group.simplifyDebts || group.kind != GroupKind.Group) return@mapNotNull null
        val payees = groupPlan(group.id).filter { it.debtorId == ME }.map { it.creditorId }.toSet()
        if (payees.isEmpty()) return@mapNotNull null
        val events =
            liveExpenses().filter { it.groupId == group.id }.map { it.createdAt to it as Any } +
                confirmedPayments()
                    .filter { it.groupId == group.id }
                    .map { it.confirmedAt!! to it as Any }
        var running = 0L
        val sinceZero = mutableListOf<String>()
        for ((_, record) in events.sortedBy { it.first }) {
            if (record is Expense) {
                running += record.paidBy(ME) - myShare(record)
                if (record.payerId != ME && myShare(record) != 0L) sinceZero += record.payerId
            } else if (record is Payment) {
                running +=
                    when (ME) {
                        record.fromId -> record.amount
                        record.toId -> -record.amount
                        else -> 0
                    }
            }
            if (running == 0L) sinceZero.clear()
        }
        if (sinceZero.toSet() != payees) group.name else null
    }
