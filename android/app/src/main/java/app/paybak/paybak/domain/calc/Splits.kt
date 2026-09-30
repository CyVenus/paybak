package app.paybak.paybak.domain.calc

import app.paybak.paybak.domain.format.Money

/** Money split among people, and the rotation counter to save afterwards (domain.md §4.1). */
data class SplitResult(val shares: Map<String, Long>, val counter: Int)

/** Splits and rounding (domain.md §4): a straight port of verify.py. */
object Splits {
    /** Gives [extra] single minor units to people in [order], starting at [counter] mod n. */
    fun rotateExtra(order: List<String>, extra: Int, counter: Int): SplitResult {
        val bonus = order.associateWith { 0L }.toMutableMap()
        for (i in 0 until extra) {
            val person = order[Math.floorMod(counter + i, order.size)]
            bonus[person] = bonus.getValue(person) + 1
        }
        return SplitResult(bonus, counter + extra)
    }

    /** ⌊total/n⌋ each plus the rotated leftover: ₹1,000 ÷ 3 → 333.34 / 333.33 / 333.33. */
    fun equal(total: Long, order: List<String>, counter: Int = 0): SplitResult {
        if (order.isEmpty()) return SplitResult(emptyMap(), counter)
        val base = Math.floorDiv(total, order.size.toLong())
        val extra = Math.floorMod(total, order.size.toLong()).toInt()
        val rotated = rotateExtra(order, extra, counter)
        return SplitResult(
            order.associateWith { base + rotated.shares.getValue(it) },
            rotated.counter,
        )
    }

    /**
     * Percent (basis points) and shares: floors, then the largest remainders, ties broken by the
     * fair rotation order.
     */
    fun weighted(
        total: Long,
        weights: Map<String, Long>,
        order: List<String>,
        counter: Int = 0,
    ): SplitResult {
        val weightSum = order.sumOf { weights[it] ?: 0L }
        if (order.isEmpty() || weightSum == 0L)
            return SplitResult(order.associateWith { 0L }, counter)
        val floors = order.associateWith { total * (weights[it] ?: 0L) / weightSum }.toMutableMap()
        val remainders = order.associateWith { total * (weights[it] ?: 0L) % weightSum }
        val extra = (total - floors.values.sum()).toInt()
        val start = Math.floorMod(counter, order.size)
        val rotated = order.drop(start) + order.take(start)
        rotated
            .sortedByDescending { remainders.getValue(it) }
            .take(extra)
            .forEach {
                floors[it] = floors.getValue(it) + 1
            }
        return SplitResult(floors, counter + extra)
    }

    /** The Exact editor footer: (remaining, "₹150 left" / "₹50 over", "₹2,650 of ₹2,800"). */
    fun exactStatus(total: Long, amounts: Collection<Long>, currency: String = "INR"): ExactStatus {
        val entered = amounts.sum()
        val remaining = total - entered
        val left =
            if (remaining >= 0) "${Money.format(remaining, currency)} left"
            else "${Money.format(-remaining, currency)} over"
        return ExactStatus(
            remaining,
            left,
            "${Money.format(entered, currency)} of ${Money.format(total, currency)}",
        )
    }

    /** Whole percentages that add up to 100, by largest remainder (Insights captions, §6.4). */
    fun <K> largestRemainderPercent(values: Map<K, Long>): Map<K, Int> {
        val total = values.values.sum()
        if (total == 0L) return values.mapValues { 0 }
        val floors = values.mapValues { (it.value * 100 / total).toInt() }.toMutableMap()
        val remainders = values.mapValues { it.value * 100 % total }
        values.keys
            .sortedByDescending { remainders.getValue(it) }
            .take(100 - floors.values.sum())
            .forEach { floors[it] = floors.getValue(it) + 1 }
        return floors
    }

    /**
     * A receipt split (§4.2): each item's price equally among its people, then [total] (with tax
     * and tip) in proportion to those subtotals. Returns the totals and the item subtotals.
     */
    fun itemized(
        items: List<Pair<Long, List<String>>>,
        total: Long,
        order: List<String>,
        counter: Int = 0,
    ): Pair<SplitResult, Map<String, Long>> {
        val subtotals = order.associateWith { 0L }.toMutableMap()
        var running = counter
        for ((price, people) in items) {
            val split = equal(price, people, running)
            running = split.counter
            split.shares.forEach { (person, share) ->
                subtotals[person] = (subtotals[person] ?: 0L) + share
            }
        }
        return weighted(total, subtotals, order, running) to subtotals
    }

    /** Loan installments: `amount ÷ count`, leftover units to the earliest (§4.3). */
    fun installments(amount: Long, count: Int): List<Long> {
        val keys = (0 until count).map(Int::toString)
        val shares = equal(amount, keys).shares
        return keys.map(shares::getValue)
    }
}

data class ExactStatus(val remaining: Long, val left: String, val detail: String)
