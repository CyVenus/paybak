package app.paybak.paybak.feature.activity

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.paybak.paybak.PaybakApplication
import app.paybak.paybak.R
import app.paybak.paybak.data.ledger.collectSnapshot
import app.paybak.paybak.domain.LedgerSnapshot
import app.paybak.paybak.domain.calc.TimelineDay
import app.paybak.paybak.domain.format.Dates
import app.paybak.paybak.domain.model.InboxType
import app.paybak.paybak.feature.PendingClaimStack
import app.paybak.paybak.navigation.LocalLedger
import app.paybak.paybak.navigation.LocalTabBarPadding
import app.paybak.paybak.navigation.rememberDebugStartScreen
import app.paybak.paybak.ui.components.PbEmptyState
import app.paybak.paybak.ui.theme.PbSpace

/**
 * The Activity tab's Timeline segment (activityTimeline, activityEmpty; activity §3): everything
 * that changed, grouped by day, newest first, with the payments waiting for you on top of Today. A
 * new account shows the First day card instead.
 */
@Composable
fun ActivityTimelineView(modifier: Modifier = Modifier) {
    val snapshot by LocalLedger.current.collectSnapshot()
    PostDebugLockScreen(snapshot)
    val claims = snapshot.home.pendingClaims
    val days = remember(snapshot) { snapshot.withToday() }
    if (days.isEmpty()) {
        EmptyTimeline(modifier)
        return
    }
    TimelineDays(
        days,
        snapshot.view,
        screen = "activity",
        modifier = modifier.testTag("activity.timeline"),
        onTop = { PendingClaimStack(claims, gap = PbSpace.S8) },
    )
}

/** The days, with Today first whenever a claim waits for you, even if nothing else is new today. */
private fun LedgerSnapshot.withToday(): List<TimelineDay> {
    val today = view.today
    if (home.pendingClaims.isEmpty() || timeline.firstOrNull()?.date == today) return timeline
    return listOf(TimelineDay(today, Dates.dayHeader(today, today), emptyList())) + timeline
}

/**
 * The First day card, centred between the segmented control and the tab bar (activity §3.8). The
 * list scrolls, so it has no height to centre in: the card takes the visible part of the window
 * below the list's top, less the tab bar's padding.
 */
@Composable
private fun EmptyTimeline(modifier: Modifier) {
    val density = LocalDensity.current
    val windowHeight = with(density) { LocalWindowInfo.current.containerSize.height.toDp() }
    val tabBar = LocalTabBarPadding.current.calculateBottomPadding()
    var top by remember { mutableStateOf<Float?>(null) }
    val visible = top?.let { windowHeight - with(density) { it.toDp() } - tabBar } ?: 0.dp
    Box(
        modifier
            .fillMaxWidth()
            .onGloballyPositioned { if (top == null) top = it.positionInWindow().y }
            .testTag("activity.empty")
    ) {
        Box(Modifier.fillMaxWidth().heightIn(min = visible), contentAlignment = Alignment.Center) {
            PbEmptyState(
                stringResource(R.string.activity_empty_title),
                stringResource(R.string.activity_empty_body),
            )
        }
    }
}

/**
 * Debug start screens for the two lock-screen pushes (activity §7): `lockConfirmRequest` posts
 * Esha's claim and `lockReminder` tonight's Kabir reminder, so they show over the app.
 */
@Composable
private fun PostDebugLockScreen(snapshot: LedgerSnapshot) {
    val start = rememberDebugStartScreen("lockConfirmRequest", "lockReminder") ?: return
    val notifications = (LocalContext.current.applicationContext as PaybakApplication).notifications
    LaunchedEffect(start) {
        when (start) {
            "lockConfirmRequest" ->
                snapshot.home.pendingClaims.firstOrNull()?.let {
                    notifications.postPaymentToConfirm(it.payment.id)
                }
            else ->
                snapshot.ledger.inbox
                    .lastOrNull { it.type == InboxType.PaymentReminder }
                    ?.let { notifications.postInboxItem(it.id) }
        }
    }
}
