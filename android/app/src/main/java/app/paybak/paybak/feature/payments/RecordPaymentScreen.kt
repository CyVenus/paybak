package app.paybak.paybak.feature.payments

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import app.paybak.paybak.data.ledger.actions.recordPayment
import app.paybak.paybak.domain.model.ME
import app.paybak.paybak.domain.model.PaymentDraft
import app.paybak.paybak.domain.model.PaymentMethod
import app.paybak.paybak.feature.RoutePlaceholder
import app.paybak.paybak.navigation.LocalLedger
import app.paybak.paybak.navigation.LocalMainNavigator
import app.paybak.paybak.navigation.Route
import app.paybak.paybak.ui.components.PbButton

/**
 * The `recordPayment` route (recordPayment, settleRecordKabir). PLACEHOLDER owned by lane A (M3):
 * replace this file and keep the signature. With prefilled args it records them, to exercise
 * `didSave`.
 */
@Composable
fun RecordPaymentScreen(route: Route.RecordPayment) {
    val navigator = LocalMainNavigator.current
    val ledger = LocalLedger.current
    val args = route.args
    RoutePlaceholder(route) {
        PbButton(
            "Save",
            onClick = {
                val id =
                    ledger.recordPayment(
                        PaymentDraft(
                            fromId = args.fromId ?: ME,
                            toId = args.toId!!,
                            amount = args.amount!!,
                            method = args.method ?: PaymentMethod.Cash,
                            groupId = args.groupId,
                            loanId = args.loanId,
                            expenseId = args.expenseId,
                        )
                    )
                navigator.didSave(Route.Payment(id), "Payment recorded")
            },
            modifier = Modifier.fillMaxWidth().testTag("recordPayment.save"),
            enabled = args.toId != null && args.amount != null && args.editing == null,
        )
    }
}
