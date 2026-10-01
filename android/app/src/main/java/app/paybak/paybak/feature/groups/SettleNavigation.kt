package app.paybak.paybak.feature.groups

import app.paybak.paybak.domain.calc.LedgerView
import app.paybak.paybak.domain.groups.GroupSettle
import app.paybak.paybak.domain.model.ReminderContext
import app.paybak.paybak.feature.settle.recordPaymentTo
import app.paybak.paybak.navigation.MainNavigator
import app.paybak.paybak.navigation.Route

/**
 * Settle up from a group: Record payment prefilled with the one person you pay there (UPI when
 * they have an ID), or the group's Settle up plan.
 */
internal fun MainNavigator.settle(view: LedgerView, settle: GroupSettle) {
    when (settle) {
        is GroupSettle.Pay ->
            open(
                view.recordPaymentTo(
                    settle.toId,
                    settle.amount,
                    settle.currency,
                    ReminderContext(groupId = settle.groupId),
                )
            )
        is GroupSettle.Plan -> open(Route.SettleUp(settle.groupId))
    }
}
