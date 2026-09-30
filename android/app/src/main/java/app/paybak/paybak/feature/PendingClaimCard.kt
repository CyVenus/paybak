package app.paybak.paybak.feature

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.paybak.paybak.R
import app.paybak.paybak.data.ledger.actions.confirmPayment
import app.paybak.paybak.data.ledger.view
import app.paybak.paybak.domain.calc.ClaimCard
import app.paybak.paybak.domain.calc.paymentFor
import app.paybak.paybak.domain.format.Money
import app.paybak.paybak.navigation.LocalLedger
import app.paybak.paybak.navigation.LocalMainNavigator
import app.paybak.paybak.navigation.Route
import app.paybak.paybak.service.HapticKind
import app.paybak.paybak.service.rememberHaptics
import app.paybak.paybak.ui.components.PbAvatarContent
import app.paybak.paybak.ui.components.PbConfirmPaymentCard
import app.paybak.paybak.ui.components.avatarContent
import app.paybak.paybak.ui.theme.LocalReduceMotion
import kotlinx.coroutines.delay

/** The Confirmed animation (250 ms) plus the hold that lets it read (home-v2 §3.9). */
private const val CONFIRMED_HOLD_MILLIS = 1_050L

/**
 * A pending claim's Confirm card wired to the store (components-app §3.9): Confirm animates to
 * Confirmed, holds, then confirms the payment and shows "Payment confirmed" (the card then leaves
 * the read models; the list owner collapses it). Not received opens its sheet. The same card works
 * on Home, Activity and Notifications. Tagged "claim.<paymentId>".
 */
@Composable
fun PendingClaimCard(claim: ClaimCard, modifier: Modifier = Modifier) {
    val ledger = LocalLedger.current
    val navigator = LocalMainNavigator.current
    val haptics = rememberHaptics()
    val payment = claim.payment
    val view = ledger.view
    val confirmedToast = stringResource(R.string.shell_payment_confirmed)
    var confirmed by rememberSaveable(payment.id) { mutableStateOf(false) }
    val committed = remember(payment.id) { mutableStateOf(false) }
    fun commit() {
        if (committed.value) return
        committed.value = true
        ledger.confirmPayment(payment.id)
        navigator.toast(confirmedToast)
    }
    val hold = if (LocalReduceMotion.current) 0L else CONFIRMED_HOLD_MILLIS
    LaunchedEffect(confirmed) {
        if (confirmed) {
            delay(hold)
            commit()
        }
    }
    DisposableEffect(payment.id) { onDispose { if (confirmed) commit() } }
    PbConfirmPaymentCard(
        avatar = view.person(payment.fromId)?.avatarContent() ?: PbAvatarContent.Initials("?"),
        title = claim.title,
        detail = claim.detail,
        confirmedTitle =
            "${view.first(payment.fromId)} paid you ${Money.format(payment.amount, payment.currency)}",
        confirmedDetail = "${view.paymentFor(payment)} · ${payment.method.label} · Confirmed",
        confirmed = confirmed,
        onConfirm = {
            haptics.perform(HapticKind.Success)
            confirmed = true
        },
        onNotReceived = { navigator.open(Route.NotReceived(payment.id)) },
        modifier = modifier,
        testTag = "claim.${payment.id}",
    )
}
