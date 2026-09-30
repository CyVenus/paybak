package app.paybak.paybak.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.longOrNull

/** A saved exchange rate: units of [to] per one unit of the record currency (domain.md §0). */
@Serializable data class Rate(val value: String, val to: String)

@Serializable data class Payer(val personId: String, val amount: Long)

@Serializable
enum class SplitMode(val label: String) {
    @SerialName("equal") Equal("Equally"),
    @SerialName("exact") Exact("Exact"),
    @SerialName("percent") Percent("Percent"),
    @SerialName("shares") Shares("Shares"),
    @SerialName("itemized") Itemized("Itemized"),
}

/**
 * One person's row of a split. [value] is the entered exact amount (minor), basis points or shares
 * (null for equal); [share] is the saved result in minor units.
 */
@Serializable
data class SplitRow(
    val personId: String,
    val included: Boolean = true,
    val value: Long? = null,
    val share: Long = 0,
)

@Serializable data class Split(val mode: SplitMode = SplitMode.Equal, val rows: List<SplitRow>)

@Serializable
data class ItemizedItem(val label: String, val amount: Long, val personIds: List<String>)

@Serializable data class ItemizedLine(val label: String, val amount: Long)

/** A receipt split (domain.md §4.2): items, then tax and tip lines. */
@Serializable
data class Itemized(
    val items: List<ItemizedItem>,
    val lines: List<ItemizedLine> = emptyList(),
    val subtotal: Long,
)

/** A receipt photo ([photo] file in the photos folder) or a bundled demo [asset]. */
@Serializable
data class Receipt(
    val photo: String? = null,
    val asset: String? = null,
    val addedBy: String = ME,
    val addedAt: Moment,
)

@Serializable
enum class HistoryKind {
    @SerialName("created") Created,
    @SerialName("amountChanged") AmountChanged,
    @SerialName("titleChanged") TitleChanged,
    @SerialName("dateChanged") DateChanged,
    @SerialName("splitChanged") SplitChanged,
    @SerialName("payersChanged") PayersChanged,
    @SerialName("categoryChanged") CategoryChanged,
    @SerialName("receiptAdded") ReceiptAdded,
    @SerialName("flagged") Flagged,
    @SerialName("flagRemoved") FlagRemoved,
    @SerialName("flagResolved") FlagResolved,
    @SerialName("deleted") Deleted,
    @SerialName("restored") Restored,
}

/** One entry of an expense's History (oldest first). [old]/[new] hold amounts or text. */
@Serializable
data class HistoryEntry(
    val kind: HistoryKind,
    val at: Moment,
    val by: String,
    val old: JsonPrimitive? = null,
    val new: JsonPrimitive? = null,
) {
    val oldAmount: Long?
        get() = old?.longOrNull

    val newAmount: Long?
        get() = new?.longOrNull
}

@Serializable data class Comment(val id: String, val by: String, val at: Moment, val text: String)

/** "Disputed": the expense still counts everywhere (domain.md §1.4). */
@Serializable data class Flag(val by: String, val note: String, val at: Moment)

/** An expense (domain.md §1.4). Deleted ones ([deletedAt] set) count nowhere. */
@Serializable
data class Expense(
    val id: String,
    val groupId: String? = null,
    val title: String,
    val category: String = Category.Other.id,
    val amount: Long,
    val currency: String,
    val rate: Rate? = null,
    val date: Day,
    val dueDate: Day? = null,
    val payers: List<Payer>,
    val split: Split,
    val itemized: Itemized? = null,
    val notes: String? = null,
    val receipt: Receipt? = null,
    val recurringRuleId: String? = null,
    val occurrenceDate: Day? = null,
    val createdAt: Moment,
    val createdBy: String = ME,
    val history: List<HistoryEntry> = emptyList(),
    val comments: List<Comment> = emptyList(),
    val flag: Flag? = null,
    val deletedAt: Moment? = null,
    val deletedBy: String? = null,
) {
    /** The (first) payer; one payer in every designed case. */
    val payerId: String
        get() = payers.first().personId

    fun shareOf(personId: String): Long =
        split.rows.firstOrNull { it.personId == personId }?.share ?: 0

    fun paidBy(personId: String): Long =
        payers.filter { it.personId == personId }.sumOf { it.amount }

    /** Everyone with a share (included rows). */
    val participantIds: List<String>
        get() = split.rows.filter { it.included }.map { it.personId }
}
