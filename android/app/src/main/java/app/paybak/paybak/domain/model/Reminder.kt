package app.paybak.paybak.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ReminderTone {
    @SerialName("friendly") Friendly,
    @SerialName("neutral") Neutral,
    @SerialName("firm") Firm,
}

@Serializable
enum class ReminderVia {
    @SerialName("paybak") Paybak,
    @SerialName("share") Share,
}

/** A reminder Paybak sent to a friend (domain.md §1.9); it changes no balance. */
@Serializable
data class Reminder(
    val id: String,
    val toId: String,
    val fromId: String = ME,
    val amount: Long,
    val currency: String,
    val expenseId: String? = null,
    val groupId: String? = null,
    val loanId: String? = null,
    val installment: Int? = null,
    val sentAt: Moment,
    val automatic: Boolean,
    val message: String? = null,
    val tone: ReminderTone? = null,
    val via: ReminderVia? = null,
)

/** What a reminder is about: one of an expense, a group or a loan (or nothing: the whole net). */
@Serializable
data class ReminderContext(
    val expenseId: String? = null,
    val groupId: String? = null,
    val loanId: String? = null,
    val installment: Int? = null,
)
