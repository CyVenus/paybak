package app.paybak.paybak.domain.model

import kotlinx.serialization.Serializable

/**
 * What a form (or Ask Paybak, or a receipt scan) hands to `addExpense` / `updateExpense`. Split
 * rows carry the entered [SplitRow.value]s; their shares are computed on save (domain.md §4). Nulls
 * take the defaults: the group's (or the default) currency, today, you paying it all.
 */
@Serializable
data class ExpenseDraft(
    val title: String = "",
    val amount: Long = 0,
    val currency: String? = null,
    val rate: Rate? = null,
    val groupId: String? = null,
    val category: String = Category.Other.id,
    val date: Day? = null,
    val dueDate: Day? = null,
    val payers: List<Payer> = emptyList(),
    val split: Split = Split(SplitMode.Equal, emptyList()),
    val itemized: Itemized? = null,
    val notes: String? = null,
    val receipt: Receipt? = null,
    val repeat: RepeatRule? = null,
) {
    /** Everyone on the expense, in the order they were added. */
    val personIds: List<String>
        get() = split.rows.map { it.personId }

    companion object {
        /** An equal split among [personIds] (you first, then the others as added). */
        fun equal(personIds: List<String>) =
            ExpenseDraft(split = Split(SplitMode.Equal, personIds.map { SplitRow(it) }))
    }
}

/** Input of `recordPayment`; [id] is only set by the demo scenarios. */
@Serializable
data class PaymentDraft(
    val fromId: String,
    val toId: String,
    val amount: Long,
    val currency: String? = null,
    val rate: Rate? = null,
    val method: PaymentMethod = PaymentMethod.Cash,
    val date: Day? = null,
    val groupId: String? = null,
    val loanId: String? = null,
    val expenseId: String? = null,
    val note: String? = null,
    val proof: String? = null,
    val recordedBy: String = ME,
    val id: String? = null,
)

/** Input of `addLoan`. */
@Serializable
data class LoanDraft(
    val lenderId: String,
    val borrowerId: String,
    val amount: Long,
    val currency: String? = null,
    val rate: Rate? = null,
    val reason: String? = null,
    val date: Day? = null,
    val installments: Installments? = null,
    val dueDate: Day? = null,
    val id: String? = null,
)

/** Input of `addGroup` (a group, or a project when [project] is set). */
@Serializable
data class GroupDraft(
    val name: String,
    val kind: GroupKind = GroupKind.Group,
    val type: GroupType? = null,
    val icon: String? = null,
    val currency: String? = null,
    val memberIds: List<String> = listOf(ME),
    val simplifyDebts: Boolean = true,
    val settleBy: Day? = null,
    val project: ProjectInfo? = null,
    val id: String? = null,
)

/** Input of `addComponent`. */
@Serializable
data class ComponentDraft(
    val projectId: String,
    val name: String,
    val estimatedCost: Long? = null,
    val actualCost: Long? = null,
    val paidBy: String = ME,
    val receipt: Receipt? = null,
)

/** The Repeat row's value (the `repeatRule` sheet's result). The day comes from [anchorDate]. */
@Serializable
data class RepeatRule(
    val frequency: Frequency = Frequency.Monthly,
    val anchorDate: Day,
    val variable: Boolean = false,
)

@Serializable data class ScanItem(val label: String, val amount: Long)

/** A tax or service line; [rate] in basis points when printed ("GST 5%"). */
@Serializable data class ScanTax(val label: String, val rate: Long? = null, val amount: Long)

/** What receipt reading found (insights §4.6); amounts in minor units of the receipt currency. */
@Serializable
data class ReceiptScan(
    val merchant: String? = null,
    val date: Day? = null,
    val time: String? = null,
    val items: List<ScanItem> = emptyList(),
    val subtotal: Long? = null,
    val taxes: List<ScanTax> = emptyList(),
    val tip: Long? = null,
    val total: Long? = null,
)

/** The scan flow's result for the Add expense form (`RouteResult.Receipt`). */
@Serializable
data class ReceiptResult(
    val draft: ExpenseDraft,
    val scan: ReceiptScan? = null,
    val photo: String? = null,
)
