package app.paybak.paybak.feature.groups

import app.paybak.paybak.domain.calc.LedgerView
import app.paybak.paybak.domain.groups.GroupSettle
import app.paybak.paybak.domain.model.ME
import app.paybak.paybak.domain.model.PaymentMethod
import app.paybak.paybak.navigation.MainNavigator
import app.paybak.paybak.navigation.RecordPaymentArgs
import app.paybak.paybak.navigation.Route

/**
 * Settle up from a group: Record payment prefilled with the one person you pay there (UPI when
 * they have an ID), or the group's Settle up plan.
 */
internal fun MainNavigator.settle(view: LedgerView, settle: GroupSettle) {
    when (settle) {
        is GroupSettle.Pay ->
            open(
                Route.RecordPayment(
                    RecordPaymentArgs(
                        fromId = ME,
                        toId = settle.toId,
                        amount = settle.amount,
                        currency = settle.currency,
                        method = PaymentMethod.Upi.takeIf { view.person(settle.toId)?.upi != null },
                        groupId = settle.groupId,
                    )
                )
            )
        is GroupSettle.Plan -> open(Route.SettleUp(settle.groupId))
    }
}
