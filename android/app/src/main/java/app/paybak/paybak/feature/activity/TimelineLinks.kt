package app.paybak.paybak.feature.activity

import app.paybak.paybak.domain.calc.LedgerView
import app.paybak.paybak.domain.calc.TimelineEvent
import app.paybak.paybak.domain.calc.TimelineKind
import app.paybak.paybak.domain.model.Category
import app.paybak.paybak.domain.model.GroupKind
import app.paybak.paybak.domain.model.Reminder
import app.paybak.paybak.navigation.Route
import app.paybak.paybak.ui.components.PbAvatarContent
import app.paybak.paybak.ui.components.avatarContent
import app.paybak.paybak.ui.components.iconForKey
import app.paybak.paybak.ui.components.pbIcon
import app.paybak.paybak.ui.icons.PbIcon

/*
 * Where timeline rows lead and what they show on the left (activity §3.5, §3.9). Pure, so the JVM
 * tests check them against the demo.
 */

/**
 * An expense row opens the expense, a payment its detail, a loan the loan, a recurring draft its
 * group's recurring expenses, and a reminder the debt it was about. Null when that record is gone.
 */
internal fun LedgerView.route(event: TimelineEvent): Route? =
    when (event.kind) {
        TimelineKind.ExpenseAdded,
        TimelineKind.ExpenseEdited -> Route.Expense(event.ref)
        TimelineKind.Payment -> Route.Payment(event.ref)
        TimelineKind.LoanAdded -> Route.Loan(event.ref)
        TimelineKind.DraftCreated -> event.groupId?.let(Route::Recurring)
        TimelineKind.ComponentChanged -> event.groupId?.let(Route::Project)
        TimelineKind.ReminderSent ->
            ledger.reminders.firstOrNull { it.id == event.ref }?.let { subjectRoute(it) }
    }

/** A reminder is about an expense, a loan or a group balance. */
private fun LedgerView.subjectRoute(reminder: Reminder): Route? =
    reminder.expenseId?.let(Route::Expense)
        ?: reminder.loanId?.let(Route::Loan)
        ?: reminder.groupId?.let(::groupRoute)

/** A group opens its page; a project its dashboard. */
internal fun LedgerView.groupRoute(groupId: String): Route =
    if (group(groupId)?.kind == GroupKind.Project) Route.Project(groupId) else Route.Group(groupId)

/**
 * The 40 dp circle: the bell for a reminder, the person for a payment or loan, the project's icon
 * for a part, else the category.
 */
internal fun LedgerView.leading(event: TimelineEvent): PbAvatarContent =
    when (event.kind) {
        TimelineKind.ReminderSent -> PbAvatarContent.Symbol(PbIcon.Bell)
        TimelineKind.ComponentChanged ->
            PbAvatarContent.Symbol(iconForKey(event.groupId?.let { group(it)?.icon }.orEmpty()))
        TimelineKind.Payment,
        TimelineKind.LoanAdded ->
            event.personId?.let { person(it)?.avatarContent() }
                ?: PbAvatarContent.Symbol(PbIcon.Profile)
        else -> PbAvatarContent.Symbol(Category.of(event.category.orEmpty()).pbIcon)
    }

/**
 * A stable test tag. An expense has an "added" and an "edited" row and a part one row per change,
 * so those get a suffix.
 */
internal fun TimelineEvent.rowTag(screen: String): String =
    "$screen.row.$ref" +
        when (kind) {
            TimelineKind.ExpenseEdited -> ".edited"
            TimelineKind.ComponentChanged -> ".${at.epochSecond}"
            else -> ""
        }
