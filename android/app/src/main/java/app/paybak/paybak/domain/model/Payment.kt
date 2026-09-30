package app.paybak.paybak.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class PaymentMethod(val label: String) {
    @SerialName("cash") Cash("Cash"),
    @SerialName("upi") Upi("UPI"),
    @SerialName("bank") Bank("Bank"),
    @SerialName("card") Card("Card"),
    @SerialName("other") Other("Other"),
}

/** Only [Confirmed] changes balances (domain.md §1.5). */
@Serializable
enum class PaymentStatus {
    @SerialName("pending") Pending,
    @SerialName("confirmed") Confirmed,
    @SerialName("notReceived") NotReceived,
    @SerialName("cancelled") Cancelled,
}

/**
 * A settlement recorded in Paybak; the money moved elsewhere (domain.md §1.5). The context is
 * [groupId] (counts in that group or project), [loanId] (a repayment) or neither (direct);
 * [expenseId] is only the "for" label.
 */
@Serializable
data class Payment(
    val id: String,
    val fromId: String,
    val toId: String,
    val amount: Long,
    val currency: String,
    val rate: Rate? = null,
    val method: PaymentMethod = PaymentMethod.Cash,
    val date: Day,
    val groupId: String? = null,
    val loanId: String? = null,
    val expenseId: String? = null,
    val note: String? = null,
    val proof: String? = null,
    val status: PaymentStatus = PaymentStatus.Pending,
    val recordedBy: String = ME,
    val createdAt: Moment,
    val confirmedAt: Moment? = null,
    val notReceivedNote: String? = null,
)
