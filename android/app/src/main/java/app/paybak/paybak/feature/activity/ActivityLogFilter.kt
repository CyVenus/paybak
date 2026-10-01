package app.paybak.paybak.feature.activity

import app.paybak.paybak.domain.calc.LedgerView
import app.paybak.paybak.domain.calc.TimelineEvent
import app.paybak.paybak.domain.calc.TimelineKind
import app.paybak.paybak.domain.calc.timeline
import app.paybak.paybak.domain.format.Dates
import app.paybak.paybak.domain.model.Category
import app.paybak.paybak.navigation.ActivityFilter
import java.time.YearMonth

/*
 * The `activityLog(filter)` route's content (proposal: projects §3.7, groups §6.4, insights bar
 * rows): the timeline narrowed to one person, group, project or category in a month. Pure, so the
 * JVM tests check it against the demo.
 */

/** The timeline events that belong to [filter], newest first. */
internal fun LedgerView.logEvents(filter: ActivityFilter): List<TimelineEvent> =
    timeline().filter { matches(it, filter) }

/** The pushed screen's title: "Build a Drone · History", "Food · September". */
internal fun LedgerView.logSubject(filter: ActivityFilter): String =
    when (filter) {
        is ActivityFilter.Person -> person(filter.personId)?.name ?: first(filter.personId)
        is ActivityFilter.Group -> group(filter.groupId)?.name.orEmpty()
        is ActivityFilter.Project -> group(filter.projectId)?.name.orEmpty()
        is ActivityFilter.Category ->
            "${Category.of(filter.category).label} · " +
                Dates.monthName(YearMonth.parse(filter.month).month)
    }

private fun LedgerView.matches(event: TimelineEvent, filter: ActivityFilter): Boolean =
    when (filter) {
        is ActivityFilter.Person -> filter.personId in peopleOf(event)
        is ActivityFilter.Group -> groupOf(event) == filter.groupId
        is ActivityFilter.Project -> groupOf(event) == filter.projectId
        is ActivityFilter.Category -> {
            val expense = expenseOf(event)
            expense != null &&
                expense.category == filter.category &&
                YearMonth.from(expense.date) == YearMonth.parse(filter.month)
        }
    }

private fun LedgerView.expenseOf(event: TimelineEvent) =
    when (event.kind) {
        TimelineKind.ExpenseAdded,
        TimelineKind.ExpenseEdited -> expense(event.ref)
        else -> null
    }

/** Expense rows count everyone on the expense; the others name one person. */
private fun LedgerView.peopleOf(event: TimelineEvent): Set<String> {
    val expense = expenseOf(event) ?: return setOfNotNull(event.personId)
    return expense.payers.map { it.personId }.toSet() + expense.split.rows.map { it.personId }
}

/** A reminder row knows its group through the reminder (directly, or through its expense). */
private fun LedgerView.groupOf(event: TimelineEvent): String? {
    if (event.groupId != null || event.kind != TimelineKind.ReminderSent) return event.groupId
    val reminder = ledger.reminders.firstOrNull { it.id == event.ref } ?: return null
    return reminder.groupId ?: reminder.expenseId?.let { expense(it)?.groupId }
}
