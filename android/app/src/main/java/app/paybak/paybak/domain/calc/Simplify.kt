package app.paybak.paybak.domain.calc

/** One payment in a plan: [debtorId] pays [creditorId] [amount] (minor units). */
data class Transfer(val debtorId: String, val creditorId: String, val amount: Long)

/**
 * Fewest transfers (domain.md §5.3): the largest debtor pays the largest creditor, ties by member
 * [order], until no debtor or creditor is left.
 */
fun simplify(nets: Map<String, Long>, order: List<String>): List<Transfer> {
    val remaining = nets.toMutableMap()
    val transfers = mutableListOf<Transfer>()
    while (true) {
        val debtor =
            order
                .filter { (remaining[it] ?: 0) < 0 }
                .minWithOrNull(compareBy({ remaining.getValue(it) }, { order.indexOf(it) }))
        val creditor =
            order
                .filter { (remaining[it] ?: 0) > 0 }
                .minWithOrNull(compareBy({ -remaining.getValue(it) }, { order.indexOf(it) }))
        if (debtor == null || creditor == null) return transfers
        val amount = minOf(-remaining.getValue(debtor), remaining.getValue(creditor))
        transfers += Transfer(debtor, creditor, amount)
        remaining[debtor] = remaining.getValue(debtor) + amount
        remaining[creditor] = remaining.getValue(creditor) - amount
    }
}
