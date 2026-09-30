package app.paybak.paybak.feature.payments

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import app.paybak.paybak.data.ledger.actions.cancelPayment
import app.paybak.paybak.data.ledger.actions.confirmPayment
import app.paybak.paybak.data.ledger.collectSnapshot
import app.paybak.paybak.domain.addrecord.PaymentContext
import app.paybak.paybak.domain.addrecord.cancelPaymentMessage
import app.paybak.paybak.domain.addrecord.openBalance
import app.paybak.paybak.domain.addrecord.paymentDetail
import app.paybak.paybak.domain.format.Dates
import app.paybak.paybak.domain.format.Money
import app.paybak.paybak.domain.model.ME
import app.paybak.paybak.domain.model.PaymentStatus
import app.paybak.paybak.feature.pickers.rememberPeopleDirectory
import app.paybak.paybak.navigation.LocalLedger
import app.paybak.paybak.navigation.LocalMainNavigator
import app.paybak.paybak.navigation.PhotoRef
import app.paybak.paybak.navigation.RecordPaymentArgs
import app.paybak.paybak.navigation.Route
import app.paybak.paybak.navigation.rememberDebugStartScreen
import app.paybak.paybak.service.HapticKind
import app.paybak.paybak.service.rememberHaptics
import app.paybak.paybak.ui.components.PbAlert
import app.paybak.paybak.ui.components.PbAmountHero
import app.paybak.paybak.ui.components.PbCard
import app.paybak.paybak.ui.components.PbHeaderAction
import app.paybak.paybak.ui.components.PbHeroLeading
import app.paybak.paybak.ui.components.PbNoticeAction
import app.paybak.paybak.ui.components.PbNoticeCard
import app.paybak.paybak.ui.components.PbPushHeader
import app.paybak.paybak.ui.components.PbSettingRow
import app.paybak.paybak.ui.components.PbSettingTone
import app.paybak.paybak.ui.components.PbSettingTrailing
import app.paybak.paybak.ui.components.addrecord.PbPinnedHeaderScreen
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles

/**
 * The `payment` route (`paymentRecorded`, `settlePaymentPending`; record-lend-group §3, settle §5):
 * a payment from either side. The payer's view waits for confirmation (grey, never red) with Edit
 * and Cancel payment; the receiver can Confirm or say Not received; Confirmed and Not received show
 * their notice.
 */
@Composable
fun PaymentDetailScreen(route: Route.Payment) {
    val navigator = LocalMainNavigator.current
    val ledger = LocalLedger.current
    val snapshot by ledger.collectSnapshot()
    val people = rememberPeopleDirectory()
    val haptics = rememberHaptics()
    val start = rememberDebugStartScreen("paymentCancelAlert")
    var cancelling by rememberSaveable { mutableStateOf(start == "paymentCancelAlert") }
    val detail = snapshot.view.paymentDetail(route.paymentId)
    val payment = detail?.payment
    val pending = payment?.status == PaymentStatus.Pending
    val mine = payment?.recordedBy == ME && detail?.youPaid == true

    PbPinnedHeaderScreen(
        testTag = "screen.payment",
        header = {
            PbPushHeader(
                "Payment",
                onBack = { navigator.back() },
                action =
                    if (pending && mine) {
                        PbHeaderAction.Text(
                            "Edit",
                            {
                                navigator.open(
                                    Route.RecordPayment(
                                        RecordPaymentArgs(editing = route.paymentId)
                                    )
                                )
                            },
                        )
                    } else null,
                testTag = "paymentRecorded",
                actionTag = "edit",
            )
        },
        gap = PbSpace.S24,
    ) {
        if (detail == null || payment == null) {
            Text(
                "This payment isn’t available any more.",
                style = PbTextStyles.Body,
                color = PbColors.Text.Secondary,
            )
            return@PbPinnedHeaderScreen
        }
        PbAmountHero(
            detail.title,
            Money.format(payment.amount, payment.currency),
            detail.meta,
            PbHeroLeading.FromTo(people.avatar(payment.fromId), people.avatar(payment.toId)),
        )
        val receiving = !detail.youPaid && pending
        PbNoticeCard(
            detail.statusBody,
            when (payment.status) {
                PaymentStatus.Confirmed -> PbIcon.CheckCircle
                PaymentStatus.NotReceived -> PbIcon.Flag
                else -> PbIcon.Activity
            },
            Modifier.testTag("paymentRecorded.status"),
            title = detail.statusTitle,
            primaryAction =
                if (receiving) {
                    PbNoticeAction("Confirm") {
                        ledger.confirmPayment(payment.id)
                        haptics.perform(HapticKind.Success)
                        navigator.toast("Payment confirmed")
                    }
                } else null,
            secondaryAction =
                if (receiving) {
                    PbNoticeAction("Not received") { navigator.open(Route.NotReceived(payment.id)) }
                } else null,
        )
        Column(verticalArrangement = Arrangement.spacedBy(PbSpace.S8)) {
            val rows =
                listOfNotNull(
                    "From" to people.first(payment.fromId),
                    "To" to people.first(payment.toId),
                    "Method" to payment.method.label,
                    detail.paidTo?.let { "Paid to" to it },
                    "Date" to Dates.day(payment.date),
                    detail.forLabel?.let { "For" to it },
                    "Proof" to if (payment.proof != null) "1 photo" else "None",
                )
            PbCard(Modifier.testTag("paymentRecorded.details")) {
                rows.forEachIndexed { index, (title, value) ->
                    val proof = payment.proof.takeIf { title == "Proof" }
                    PbSettingRow(
                        title,
                        value = value,
                        trailing =
                            if (proof != null) PbSettingTrailing.Chevron
                            else PbSettingTrailing.None,
                        onClick =
                            proof?.let {
                                { navigator.open(Route.PhotoViewer(PhotoRef(file = it))) }
                            },
                        showDivider = index < rows.lastIndex,
                    )
                }
            }
            detail.footnote?.let {
                Text(it, style = PbTextStyles.Footnote, color = PbColors.Text.Secondary)
            }
        }
        if (pending && mine) {
            PbCard {
                PbSettingRow(
                    "Cancel payment",
                    Modifier.testTag("paymentRecorded.cancel"),
                    icon = PbIcon.Delete,
                    tone = PbSettingTone.Destructive,
                    trailing = PbSettingTrailing.None,
                    onClick = { cancelling = true },
                    showDivider = false,
                )
            }
        }
    }
    if (payment != null && cancelling) {
        val owed =
            -snapshot.view.openBalance(
                payment.toId,
                PaymentContext(payment.groupId, payment.loanId),
            )
        PbAlert(
            title = "Cancel this payment?",
            message = snapshot.view.cancelPaymentMessage(payment, owed),
            cancelLabel = "Keep",
            actionLabel = "Cancel payment",
            onCancel = { cancelling = false },
            onAction = {
                cancelling = false
                ledger.cancelPayment(payment.id)
                navigator.back()
            },
            testTag = "paymentRecorded.alert",
        )
    }
}
