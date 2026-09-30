package app.paybak.paybak.navigation

import app.paybak.paybak.domain.model.ExpenseDraft
import app.paybak.paybak.domain.model.PaymentMethod
import app.paybak.paybak.domain.model.newId
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Identifies one picker request: the picker answers with `navigator.complete(request.id, …)` and
 * the caller receives it in `RouteResultEffect(request.id)` (app-architecture §2.7).
 */
@Serializable data class PickRequest(val id: String = newId())

@Serializable
enum class PickMode {
    @SerialName("multi") Multi,
    @SerialName("single") Single,
}

/** `pickDate`'s field: the expense date, or a due date (with quick chips and "None"). */
@Serializable
enum class DateKind {
    @SerialName("date") Date,
    @SerialName("dueDate") DueDate,
}

/** What an Activity log lists (activity §3.9, projects §3.10, groups §6.4). */
@Serializable
sealed interface ActivityFilter {
    @Serializable @SerialName("person") data class Person(val personId: String) : ActivityFilter

    @Serializable @SerialName("group") data class Group(val groupId: String) : ActivityFilter

    @Serializable @SerialName("project") data class Project(val projectId: String) : ActivityFilter

    /** One Insights category in a month (`yyyy-MM`). */
    @Serializable
    @SerialName("category")
    data class Category(val category: String, val month: String) : ActivityFilter
}

/** Add expense: new (optionally prefilled by [draft]) or editing an existing expense. */
@Serializable
data class AddExpenseArgs(
    val editing: String? = null,
    val draft: ExpenseDraft? = null,
    val focusAmount: Boolean = true,
)

/** Record payment: new (optionally prefilled, e.g. from a Settle row) or editing. */
@Serializable
data class RecordPaymentArgs(
    val editing: String? = null,
    val fromId: String? = null,
    val toId: String? = null,
    val amount: Long? = null,
    val currency: String? = null,
    val method: PaymentMethod? = null,
    val groupId: String? = null,
    val loanId: String? = null,
    val expenseId: String? = null,
)

@Serializable
enum class LendDirection {
    @SerialName("lent") Lent,
    @SerialName("borrowed") Borrowed,
}

@Serializable
data class LendMoneyArgs(
    val editing: String? = null,
    val personId: String? = null,
    val direction: LendDirection = LendDirection.Lent,
)

@Serializable
enum class NewGroupMode {
    @SerialName("group") Group,
    @SerialName("project") Project,
}

/** A photo to show full screen: a file in the photos folder or a bundled demo asset. */
@Serializable data class PhotoRef(val file: String? = null, val asset: String? = null)
