package app.paybak.paybak.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class InboxType {
    @SerialName("paymentReminder") PaymentReminder,
    @SerialName("monthlySummary") MonthlySummary,
    @SerialName("paymentConfirmed") PaymentConfirmed,
    @SerialName("paymentOverdue") PaymentOverdue,
    @SerialName("newExpenseInGroup") NewExpenseInGroup,
    @SerialName("paymentNotReceived") PaymentNotReceived,
    @SerialName("expenseFlagged") ExpenseFlagged,
    @SerialName("flagResolved") FlagResolved,
}

/**
 * The snapshot an inbox item was created with (domain.md §6.8), so its text never changes later.
 * Each type uses a subset of the fields.
 */
@Serializable
data class InboxParams(
    val personId: String? = null,
    val actorId: String? = null,
    val amount: Long? = null,
    val total: Long? = null,
    val share: Long? = null,
    val currency: String? = null,
    val title: String? = null,
    val dueDate: Day? = null,
    val groupId: String? = null,
    val expenseId: String? = null,
    val loanId: String? = null,
    val paymentId: String? = null,
    val method: PaymentMethod? = null,
    val year: Int? = null,
    val month: Int? = null,
    val spent: Long? = null,
    val owed: Long? = null,
    val owe: Long? = null,
    val note: String? = null,
)

/** Something that happened to you: the Notifications screen (domain.md §1.10). */
@Serializable
data class InboxItem(
    val id: String,
    val type: InboxType,
    val createdAt: Moment,
    val read: Boolean = false,
    val params: InboxParams = InboxParams(),
)
