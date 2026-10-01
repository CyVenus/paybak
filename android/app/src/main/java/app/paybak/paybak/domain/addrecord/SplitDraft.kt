package app.paybak.paybak.domain.addrecord

import app.paybak.paybak.domain.actions.PERCENT_TOTAL_BPS
import app.paybak.paybak.domain.calc.Splits
import app.paybak.paybak.domain.format.Money
import app.paybak.paybak.domain.model.Itemized
import app.paybak.paybak.domain.model.ME
import app.paybak.paybak.domain.model.Split
import app.paybak.paybak.domain.model.SplitMode
import app.paybak.paybak.domain.model.SplitRow
import kotlinx.serialization.Serializable

/**
 * The split editor's state (add-expense §3.5, §7): the mode, who is left out, and what was typed
 * per person: Exact amounts ("700"), percents ("25") or shares ("1"). Someone with nothing typed
 * joins with ₹0 / 0 % / 1 share.
 */
@Serializable
data class SplitDraft(
    val mode: SplitMode = SplitMode.Equal,
    val excluded: Set<String> = emptySet(),
    val values: Map<String, String> = emptyMap(),
) {
    /** The people who share it, in [people]'s order. */
    fun included(people: List<String>): List<String> = people.filter { it !in excluded }

    /** What [person] typed, or the value a newcomer starts with. */
    fun value(person: String): String = values[person] ?: if (mode == SplitMode.Shares) "1" else "0"

    /** Ticks or unticks [person]; the last person ticked stays ticked. */
    fun toggle(person: String, people: List<String>): SplitDraft =
        when {
            person in excluded -> copy(excluded = excluded - person)
            included(people).size <= 1 -> this
            else -> copy(excluded = excluded + person)
        }

    /** Drops what belonged to people no longer on the expense. */
    fun keepOnly(people: List<String>): SplitDraft {
        val kept =
            copy(
                excluded = excluded intersect people.toSet(),
                values = values.filterKeys { it in people },
            )
        return if (kept.included(people).isEmpty()) kept.copy(excluded = emptySet()) else kept
    }

    fun withValue(person: String, text: String): SplitDraft =
        copy(values = values + (person to text))

    /** The shares, what's left and the footer for [total] among [people] (see [SplitPreview]). */
    fun preview(
        total: Long,
        people: List<String>,
        currency: String,
        order: List<String> = people,
        counter: Int = 0,
        itemized: Itemized? = null,
    ): SplitPreview {
        val sharers = order.filter { it in included(people) }
        val zeros = people.associateWith { 0L }
        fun balanced(shares: Map<String, Long>) =
            SplitPreview(
                zeros + shares,
                0,
                "${Money.format(0, currency)} left",
                "${Money.format(total, currency)} of ${Money.format(total, currency)}",
            )
        return when (mode) {
            SplitMode.Equal -> balanced(Splits.equal(total, sharers, counter).shares)
            SplitMode.Shares ->
                balanced(
                    Splits.weighted(total, weights(sharers, ::shareCount), sharers, counter).shares
                )
            SplitMode.Itemized -> {
                val items = itemized?.items.orEmpty().map { it.amount to it.personIds }
                balanced(Splits.itemized(items, total, sharers, counter).first.shares)
            }
            SplitMode.Exact -> {
                val shares = sharers.associateWith { AmountEntry.minor(value(it), currency) }
                val status = Splits.exactStatus(total, shares.values, currency)
                SplitPreview(zeros + shares, status.remaining, status.left, status.detail)
            }
            SplitMode.Percent -> {
                val bps = weights(sharers, ::basisPoints)
                val entered = bps.values.sum()
                val remaining = PERCENT_TOTAL_BPS - entered
                SplitPreview(
                    zeros + Splits.weighted(total, bps, sharers, counter).shares,
                    remaining,
                    if (remaining >= 0) "${percent(remaining)} left"
                    else "${percent(-remaining)} over",
                    "${percent(entered)} of 100%",
                )
            }
        }
    }

    /**
     * Switches to [next], starting from the current money split: Exact takes each share, Percent
     * the shares as percentages (adding up to 100 %), Shares 1 each, Equally recomputes.
     */
    fun switchTo(
        next: SplitMode,
        preview: SplitPreview,
        people: List<String>,
        currency: String,
    ): SplitDraft {
        if (next == mode) return this
        val sharers = included(people)
        val total = sharers.sumOf { preview.shares[it] ?: 0L }
        val values =
            when (next) {
                SplitMode.Exact ->
                    sharers.associateWith {
                        AmountEntry.text(preview.shares[it] ?: 0, currency).ifEmpty { "0" }
                    }
                SplitMode.Percent -> {
                    val weights =
                        if (total > 0) sharers.associateWith { preview.shares[it] ?: 0L }
                        else sharers.associateWith { 1L }
                    Splits.weighted(PERCENT_TOTAL_BPS, weights, sharers).shares.mapValues {
                        percentText(it.value)
                    }
                }
                SplitMode.Shares -> sharers.associateWith { "1" }
                else -> emptyMap()
            }
        return copy(mode = next, values = values)
    }

    /** The saved split rows: everyone on the expense, with the typed value in its unit. */
    fun rows(people: List<String>, currency: String): List<SplitRow> = people.map { person ->
        val included = person !in excluded
        val value =
            when (mode) {
                SplitMode.Exact -> if (included) AmountEntry.minor(value(person), currency) else 0
                SplitMode.Percent -> if (included) basisPoints(person) else 0
                SplitMode.Shares -> if (included) shareCount(person) else 0
                else -> null
            }
        SplitRow(person, included, value)
    }

    /** The form's Split row: "Equally · ₹700 each", "Exact · 4 people", "Doesn't add up". */
    fun summary(
        preview: SplitPreview,
        people: List<String>,
        total: Long,
        currency: String,
    ): SplitSummary {
        val count = included(people).size
        if (!preview.balanced) return SplitSummary("Doesn’t add up", error = true)
        val counted = "$count ${if (count == 1) "person" else "people"}"
        val text =
            when (mode) {
                SplitMode.Equal ->
                    when {
                        people.size < 2 || total == 0L -> "Equally"
                        total % count == 0L ->
                            "Equally · ${Money.format(total / count, currency)} each"
                        else -> "Equally · $counted"
                    }
                else -> "${mode.label} · $counted"
            }
        return SplitSummary(text)
    }

    private fun weights(sharers: List<String>, of: (String) -> Long) = sharers.associateWith(of)

    private fun shareCount(person: String): Long = value(person).toLongOrNull() ?: 0

    private fun basisPoints(person: String): Long = AmountEntry.minor(value(person), "INR")

    companion object {
        /** The draft of a saved split, for edit mode. */
        fun of(split: Split, currency: String): SplitDraft =
            SplitDraft(
                mode = split.mode,
                excluded = split.rows.filterNot { it.included }.map { it.personId }.toSet(),
                values =
                    split.rows
                        .filter { it.included && it.value != null }
                        .associate { row ->
                            val value = row.value!!
                            row.personId to
                                when (split.mode) {
                                    SplitMode.Exact ->
                                        AmountEntry.text(value, currency).ifEmpty { "0" }
                                    SplitMode.Percent -> percentText(value)
                                    else -> value.toString()
                                }
                        },
            )

        /** 2500 bps → "25", 3333 → "33.33". */
        fun percentText(bps: Long): String = AmountEntry.text(bps, "INR").ifEmpty { "0" }

        /** 2500 bps → "25%". */
        fun percent(bps: Long): String = percentText(bps) + "%"

        /**
         * The rotation order the ledger splits in (domain.md §4.1): a group's member order, else
         * you first, then the others as added.
         */
        fun order(people: List<String>, groupMembers: List<String>?): List<String> =
            if (groupMembers != null) {
                groupMembers.filter { it in people } + people.filter { it !in groupMembers }
            } else {
                people.filter { it == ME } + people.filter { it != ME }
            }
    }
}

/** Each person's share in minor units, what's left (money or basis points) and the footer. */
data class SplitPreview(
    val shares: Map<String, Long>,
    val remaining: Long,
    val left: String,
    val detail: String,
) {
    val balanced: Boolean
        get() = remaining == 0L
}

/** The form's Split row value; [error] reads in red. */
data class SplitSummary(val text: String, val error: Boolean = false)
