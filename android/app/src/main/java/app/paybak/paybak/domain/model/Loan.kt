package app.paybak.paybak.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class Frequency(val label: String) {
    @SerialName("weekly") Weekly("Weekly"),
    @SerialName("biweekly") Biweekly("Every 2 weeks"),
    @SerialName("monthly") Monthly("Monthly"),
    @SerialName("yearly") Yearly("Yearly"),
}

@Serializable data class Installments(val count: Int, val frequency: Frequency, val firstDue: Day)

/** An IOU (domain.md §1.6, §9). One side is [ME]; repayments are payments with its id. */
@Serializable
data class Loan(
    val id: String,
    val lenderId: String,
    val borrowerId: String,
    val amount: Long,
    val currency: String,
    val rate: Rate? = null,
    val reason: String? = null,
    val date: Day,
    val installments: Installments? = null,
    val dueDate: Day? = null,
    val createdAt: Moment,
    val createdBy: String = ME,
) {
    /** The other person. */
    val friendId: String
        get() = if (lenderId == ME) borrowerId else lenderId

    val title: String
        get() = reason ?: "Loan"
}
