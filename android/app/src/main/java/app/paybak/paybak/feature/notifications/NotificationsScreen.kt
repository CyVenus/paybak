package app.paybak.paybak.feature.notifications

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import app.paybak.paybak.R
import app.paybak.paybak.data.ledger.actions.markAllInboxRead
import app.paybak.paybak.data.ledger.actions.markInboxRead
import app.paybak.paybak.data.ledger.collectSnapshot
import app.paybak.paybak.domain.calc.InboxRow
import app.paybak.paybak.domain.calc.LedgerView
import app.paybak.paybak.domain.format.Dates
import app.paybak.paybak.domain.model.InboxType
import app.paybak.paybak.feature.PendingClaimStack
import app.paybak.paybak.navigation.DeepLink
import app.paybak.paybak.navigation.LocalLedger
import app.paybak.paybak.navigation.LocalMainNavigator
import app.paybak.paybak.navigation.Route
import app.paybak.paybak.ui.components.PbActivityRow
import app.paybak.paybak.ui.components.PbBadgeStyle
import app.paybak.paybak.ui.components.PbHeaderAction
import app.paybak.paybak.ui.components.PbPushHeader
import app.paybak.paybak.ui.components.PbSectionHeader
import app.paybak.paybak.ui.components.addrecord.PbPinnedHeaderScreen
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles

/**
 * The `notifications` route (activity §6), from the Home bell: what happened to you, Today and
 * Earlier, newest first. Payments to confirm are handled inline; opening a row marks it read, and
 * Mark all read clears every dot (and the bell's).
 */
@Composable
fun NotificationsScreen(route: Route.Notifications) {
    val navigator = LocalMainNavigator.current
    val ledger = LocalLedger.current
    val snapshot by ledger.collectSnapshot()
    val claims = snapshot.home.pendingClaims
    val view = snapshot.view
    val (today, earlier) = snapshot.inbox.partition { it.today }
    PbPinnedHeaderScreen(
        testTag = "screen.notifications",
        header = {
            PbPushHeader(
                stringResource(R.string.activity_notifications_title),
                onBack = { navigator.back() },
                action =
                    PbHeaderAction.Text(
                        stringResource(R.string.activity_mark_all_read),
                        onClick = { ledger.markAllInboxRead() },
                        wide = true,
                        enabled = snapshot.unreadCount > 0,
                    ),
                testTag = "notifications",
                actionTag = "markAllRead",
            )
        },
        gap = PbSpace.S24,
    ) {
        if (today.isNotEmpty() || claims.isNotEmpty()) {
            InboxSection(Dates.dayHeader(view.today, view.today), today, view) {
                PendingClaimStack(claims, gap = PbSpace.S8)
            }
        }
        if (earlier.isNotEmpty()) {
            InboxSection(stringResource(R.string.activity_earlier), earlier, view)
        }
        if (today.isEmpty() && earlier.isEmpty() && claims.isEmpty()) {
            Text(
                stringResource(R.string.activity_notifications_empty),
                Modifier.testTag("notifications.empty"),
                style = PbTextStyles.Body,
                color = PbColors.Text.Secondary,
            )
        }
    }
}

/** A section header, [onTop] (the Confirm cards on Today), then the rows with no gap. */
@Composable
private fun InboxSection(
    title: String,
    rows: List<InboxRow>,
    view: LedgerView,
    onTop: @Composable () -> Unit = {},
) {
    val navigator = LocalMainNavigator.current
    val ledger = LocalLedger.current
    Column(verticalArrangement = Arrangement.spacedBy(PbSpace.S8)) {
        PbSectionHeader(title)
        Column {
            onTop()
            rows.forEach { row ->
                val item = row.item
                val overdue = item.type == InboxType.PaymentOverdue
                PbActivityRow(
                    leading = view.leading(item),
                    title = row.title,
                    subtitle = row.body,
                    date = row.time.takeUnless { overdue },
                    badge = stringResource(R.string.activity_overdue).takeIf { overdue },
                    badgeStyle = PbBadgeStyle.Overdue.takeIf { overdue },
                    unread = !item.read,
                    onClick = {
                        if (!item.read) ledger.markInboxRead(item.id)
                        when (val target = view.target(item)) {
                            is InboxTarget.Open -> navigator.open(target.route)
                            is InboxTarget.Insights ->
                                navigator.open(DeepLink.Insights(target.month))
                            null -> Unit
                        }
                    },
                    testTag = "notifications.row.${item.id}",
                )
            }
        }
    }
}
