package app.paybak.paybak.feature.home

import app.paybak.paybak.domain.calc.DueAction
import app.paybak.paybak.domain.calc.DueSoonRow
import app.paybak.paybak.domain.calc.HomeActivityRow
import app.paybak.paybak.domain.calc.LedgerView
import app.paybak.paybak.domain.calc.Obligation
import app.paybak.paybak.domain.calc.ObligationKind
import app.paybak.paybak.domain.calc.TimelineKind
import app.paybak.paybak.domain.model.GroupKind
import app.paybak.paybak.domain.model.ME
import app.paybak.paybak.domain.model.PaymentMethod
import app.paybak.paybak.domain.model.ReminderContext
import app.paybak.paybak.navigation.RecordPaymentArgs
import app.paybak.paybak.navigation.Route

/*
 * Where Home's rows lead (app-architecture §2.3, screens-home-v2 §2.2). Pure, so the JVM tests
 * check them against the demo.
 */

/** A Due soon row about a group you owe in ("Goa Trip · Your share") rather than a person. */
internal val DueSoonRow.isGroupRow: Boolean
    get() = obligation.debtorId == ME && obligation.kind == ObligationKind.Group

/** The row's stable id for test tags: the group, or the friend. */
internal val DueSoonRow.id: String
    get() = if (isGroupRow) obligation.ref else obligation.friendId

/**
 * Remind opens the Remind sheet about this debt; Settle opens Record payment prefilled from the
 * plan: you pay the simplified payee (Goa Trip → Kabir) the item's amount, by UPI when they have
 * one, filed under the group, loan or expense.
 */
internal fun DueSoonRow.actionRoute(view: LedgerView): Route {
    val item = obligation
    return when (action) {
        DueAction.Remind -> Route.Remind(item.friendId, item.reminderContext())
        DueAction.Settle ->
            Route.RecordPayment(
                RecordPaymentArgs(
                    fromId = ME,
                    toId = item.friendId,
                    amount = item.amount,
                    currency = view.defaultCurrency,
                    method = PaymentMethod.Upi.takeIf { view.person(item.friendId)?.upi != null },
                    groupId = item.ref.takeIf { item.isGroupContext },
                    loanId = item.ref.takeIf { item.kind == ObligationKind.Loan },
                    expenseId = item.ref.takeIf { item.kind == ObligationKind.Direct },
                )
            )
    }
}

/** The row body: the group (or project) you owe in, otherwise the friend's page. */
internal fun DueSoonRow.bodyRoute(view: LedgerView): Route =
    if (isGroupRow) view.groupRoute(obligation.ref) else Route.Friend(obligation.friendId)

/** A Recent activity row opens the expense or the payment. */
internal fun HomeActivityRow.route(): Route =
    if (event.kind == TimelineKind.Payment) Route.Payment(event.ref) else Route.Expense(event.ref)

private val Obligation.isGroupContext: Boolean
    get() = kind == ObligationKind.Group || kind == ObligationKind.Project

private fun Obligation.reminderContext(): ReminderContext =
    when (kind) {
        ObligationKind.Direct -> ReminderContext(expenseId = ref)
        ObligationKind.Group,
        ObligationKind.Project -> ReminderContext(groupId = ref)
        ObligationKind.Loan -> ReminderContext(loanId = ref, installment = installment)
    }

private fun LedgerView.groupRoute(groupId: String): Route =
    if (group(groupId)?.kind == GroupKind.Project) Route.Project(groupId) else Route.Group(groupId)
