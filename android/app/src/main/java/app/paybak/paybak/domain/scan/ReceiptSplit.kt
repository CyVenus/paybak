package app.paybak.paybak.domain.scan

import app.paybak.paybak.domain.ask.ExpensePhrase
import app.paybak.paybak.domain.calc.Splits
import app.paybak.paybak.domain.model.Category
import app.paybak.paybak.domain.model.ExpenseDraft
import app.paybak.paybak.domain.model.Itemized
import app.paybak.paybak.domain.model.ItemizedItem
import app.paybak.paybak.domain.model.ItemizedLine
import app.paybak.paybak.domain.model.ME
import app.paybak.paybak.domain.model.Payer
import app.paybak.paybak.domain.model.ReceiptScan
import app.paybak.paybak.domain.model.Split
import app.paybak.paybak.domain.model.SplitMode
import app.paybak.paybak.domain.model.SplitRow
import java.time.LocalDate
import java.time.LocalTime

/** The live totals of Assign items: who pays what, tax and tip included. */
data class AssignTotals(
    /** Per person (in [people] order), with tax and tip in proportion to their items. */
    val totals: Map<String, Long>,
    /** Items nobody has been picked for yet. */
    val unassigned: Int,
)

/**
 * The receipt maths of Check receipt and Assign items (insights §4.6): the total, each person's
 * share and the expense the scan ends in.
 */
object ReceiptSplit {
    private val foodPlaces =
        listOf("cafe", "café", "restaurant", "dhaba", "bar", "bistro", "kitchen")

    /** Subtotal + tax lines + tip: the Total row, recomputed on every edit. */
    fun total(scan: ReceiptScan): Long =
        (scan.subtotal ?: 0) + scan.taxes.sumOf { it.amount } + (scan.tip ?: 0)

    /** "Items add up to …" unless the items match the subtotal. */
    fun itemsMatch(scan: ReceiptScan): Boolean = scan.items.sumOf { it.amount } == scan.subtotal

    /** "Tip 10%" from the tip and subtotal; "Tip" when it isn't a whole percentage. */
    fun tipLabel(scan: ReceiptScan): String {
        val tip = scan.tip ?: return "Tip"
        val subtotal = scan.subtotal ?: return "Tip"
        if (subtotal == 0L || tip * 100 % subtotal != 0L) return "Tip"
        return "Tip ${tip * 100 / subtotal}%"
    }

    /**
     * Each item's price shared evenly among the people picked for it, then tax and tip in
     * proportion (₹860 / ₹540 / ₹600 × 1.15 → ₹989 / ₹621 / ₹690). Only assigned items count.
     */
    fun totals(
        scan: ReceiptScan,
        assigned: List<List<String>>,
        people: List<String>,
    ): AssignTotals {
        val picked = scan.items.zip(assigned).filter { (_, who) -> who.isNotEmpty() }
        val subtotal = scan.subtotal ?: 0
        val itemsTotal = picked.sumOf { it.first.amount }
        val withExtras =
            if (subtotal == 0L) itemsTotal
            else Math.round(itemsTotal.toDouble() * total(scan) / subtotal)
        val split =
            Splits.itemized(picked.map { (item, who) -> item.amount to who }, withExtras, people)
        return AssignTotals(
            people.associateWith { split.first.shares[it] ?: 0L },
            scan.items.size - picked.size,
        )
    }

    /** "Shared by 3 · ₹80 each" splits [price] evenly among [count]; the share before rotation. */
    fun eachShare(price: Long, count: Int): Long = if (count == 0) 0 else price / count

    /**
     * The expense the scan ends in (insights §4.6 #5): the total paid by you, split itemized among
     * [people], titled "Lunch at Leopold Cafe" for a meal (by the receipt's time), dated the
     * receipt's day unless that is still to come.
     */
    fun draft(
        scan: ReceiptScan,
        assigned: List<List<String>>,
        people: List<String>,
        today: LocalDate,
    ): ExpenseDraft {
        val total = total(scan)
        val category = category(scan)
        val merchant = scan.merchant?.takeIf { it.isNotBlank() }
        val meal = scan.time?.let { runCatching { LocalTime.parse(it) }.getOrNull() }?.let(::meal)
        return ExpenseDraft(
            title =
                when {
                    merchant == null -> ""
                    category == Category.Food && meal != null -> "$meal at $merchant"
                    else -> merchant
                },
            amount = total,
            category = category.id,
            date = scan.date?.takeIf { !it.isAfter(today) } ?: today,
            payers = listOf(Payer(ME, total)),
            split = Split(SplitMode.Itemized, people.map { SplitRow(it) }),
            itemized =
                Itemized(
                    items =
                        scan.items.zip(assigned) { item, who ->
                            ItemizedItem(item.label, item.amount, who)
                        },
                    lines =
                        scan.taxes.map { ItemizedLine(it.label, it.amount) } +
                            listOfNotNull(scan.tip?.let { ItemizedLine(tipLabel(scan), it) }),
                    subtotal = scan.subtotal ?: scan.items.sumOf { it.amount },
                ),
        )
    }

    /**
     * "Attach photo" on an unreadable receipt: no amount or items, only who the expense is with, so
     * the photo is attached and the form keeps its people.
     */
    fun attachOnly(people: List<String>): ExpenseDraft = ExpenseDraft.equal(people)

    /** Food for a cafe or restaurant, or when most items are food; Other otherwise. */
    fun category(scan: ReceiptScan): Category {
        val merchant = scan.merchant.orEmpty().lowercase()
        val place = merchant.split(Regex("""\W+""")).any { it in foodPlaces }
        val foodItems = scan.items.count { ExpensePhrase.guessCategory(it.label) == Category.Food }
        return if (place || foodItems * 2 >= scan.items.size && foodItems > 0) Category.Food
        else Category.Other
    }

    private fun meal(time: LocalTime): String =
        when (time.hour) {
            in 5..10 -> "Breakfast"
            in 11..15 -> "Lunch"
            in 16..18 -> "Snacks"
            else -> "Dinner"
        }
}
