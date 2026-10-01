package app.paybak.paybak.feature.notifications

import app.paybak.paybak.domain.calc.LedgerView
import app.paybak.paybak.domain.model.Category
import app.paybak.paybak.domain.model.InboxItem
import app.paybak.paybak.domain.model.InboxType
import app.paybak.paybak.domain.model.ME
import app.paybak.paybak.domain.model.PaymentMethod
import app.paybak.paybak.domain.model.ReminderContext
import app.paybak.paybak.navigation.RecordPaymentArgs
import app.paybak.paybak.navigation.Route
import app.paybak.paybak.ui.components.PbAvatarContent
import app.paybak.paybak.ui.components.avatarContent
import app.paybak.paybak.ui.components.pbIcon
import app.paybak.paybak.ui.icons.PbIcon
import java.time.YearMonth

/*
 * Where inbox rows lead and what they show on the left (activity §6, app-architecture §2.4). Pure,
 * so the JVM tests check them against the demo.
 */

/** What tapping an inbox row does. */
internal sealed interface InboxTarget {
    /** Push, present or float [route] over the inbox. */
    data class Open(val route: Route) : InboxTarget

    /** Switch to the Activity tab's Insights for [month]. */
    data class Insights(val month: YearMonth) : InboxTarget
}

/**
 * A reminder opens Record payment to the friend you owe, prefilled; the monthly summary that
 * month's Insights; an overdue alert the Remind sheet; payments their detail; expense news the
 * expense. Null when the item names nothing to open.
 */
internal fun LedgerView.target(item: InboxItem): InboxTarget? {
    val p = item.params
    return when (item.type) {
        InboxType.PaymentReminder ->
            p.personId?.let { payee ->
                InboxTarget.Open(
                    Route.RecordPayment(
                        RecordPaymentArgs(
                            fromId = ME,
                            toId = payee,
                            amount = p.amount,
                            currency = p.currency,
                            method = PaymentMethod.Upi.takeIf { person(payee)?.upi != null },
                            groupId = p.groupId,
                        )
                    )
                )
            }
        InboxType.MonthlySummary ->
            if (p.year != null && p.month != null) {
                InboxTarget.Insights(YearMonth.of(p.year, p.month))
            } else {
                null
            }
        InboxType.PaymentOverdue ->
            p.personId?.let {
                InboxTarget.Open(
                    Route.Remind(it, ReminderContext(p.expenseId, p.groupId, p.loanId))
                )
            }
        InboxType.PaymentConfirmed,
        InboxType.PaymentNotReceived -> p.paymentId?.let { InboxTarget.Open(Route.Payment(it)) }
        InboxType.NewExpenseInGroup,
        InboxType.ExpenseFlagged,
        InboxType.FlagResolved -> p.expenseId?.let { InboxTarget.Open(Route.Expense(it)) }
    }
}

/** The 40 dp circle: the person for payments, an icon for reminders, summaries and expenses. */
internal fun LedgerView.leading(item: InboxItem): PbAvatarContent {
    val p = item.params
    val icon =
        when (item.type) {
            InboxType.PaymentReminder -> PbIcon.Calendar
            InboxType.MonthlySummary -> PbIcon.Chart
            InboxType.NewExpenseInGroup ->
                Category.of(p.expenseId?.let { expense(it)?.category }.orEmpty()).pbIcon
            InboxType.ExpenseFlagged,
            InboxType.FlagResolved -> PbIcon.Flag
            InboxType.PaymentConfirmed,
            InboxType.PaymentOverdue,
            InboxType.PaymentNotReceived ->
                return p.personId?.let { person(it)?.avatarContent() }
                    ?: PbAvatarContent.Symbol(PbIcon.Profile)
        }
    return PbAvatarContent.Symbol(icon)
}
