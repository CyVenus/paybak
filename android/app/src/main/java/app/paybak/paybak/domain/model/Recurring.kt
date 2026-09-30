package app.paybak.paybak.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class RuleSplit(val mode: SplitMode = SplitMode.Equal, val personIds: List<String>)

/**
 * A recurring expense (domain.md §1.8). The schedule's day comes from [anchorDate]; a [variable]
 * rule creates drafts that need an amount instead of expenses.
 */
@Serializable
data class RecurringRule(
    val id: String,
    val groupId: String? = null,
    val title: String,
    val category: String = Category.Other.id,
    val amount: Long? = null,
    val currency: String,
    val variable: Boolean = false,
    val frequency: Frequency = Frequency.Monthly,
    val anchorDate: Day,
    val startDate: Day,
    val lastOccurrence: Day? = null,
    val payerId: String = ME,
    val split: RuleSplit,
    val createdAt: Moment,
    val createdBy: String = ME,
    val active: Boolean = true,
)

/** An occurrence of a variable rule waiting for its amount; drafts never count. */
@Serializable
data class Draft(
    val id: String,
    val ruleId: String,
    val occurrenceDate: Day,
    val createdAt: Moment,
    val expenseId: String? = null,
)
