package app.paybak.paybak.feature.settle

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.paybak.paybak.R
import app.paybak.paybak.data.ledger.collectSnapshot
import app.paybak.paybak.domain.calc.LedgerView
import app.paybak.paybak.domain.settle.SettleUpRow
import app.paybak.paybak.domain.settle.groupSettlePlan
import app.paybak.paybak.domain.settle.settleUpPage
import app.paybak.paybak.navigation.LocalLedger
import app.paybak.paybak.navigation.LocalMainNavigator
import app.paybak.paybak.navigation.Route
import app.paybak.paybak.rive.PaybakRiveAsset
import app.paybak.paybak.ui.components.PbAvatarContent
import app.paybak.paybak.ui.components.PbEmptyState
import app.paybak.paybak.ui.components.PbNoticeCard
import app.paybak.paybak.ui.components.PbSectionHeader
import app.paybak.paybak.ui.components.avatarContent
import app.paybak.paybak.ui.components.groups.PbPushedPage
import app.paybak.paybak.ui.components.home.PbAttentionRow
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.theme.PbSpace

/**
 * The `settleUp` route (settleUp, screens-settle §3): the fewest-payments plan from your side,
 * for everyone or for one group. Payments to make open Record payment prefilled (Settle); people
 * who owe you get the Remind sheet over this list. A payment you recorded keeps its row as
 * Pending, without a button, until it's confirmed. Buttons are tagged
 * `settleUp.<pay|remind>.<friendId>`.
 */
@Composable
fun SettleUpScreen(route: Route.SettleUp) {
    val navigator = LocalMainNavigator.current
    val snapshot by LocalLedger.current.collectSnapshot()
    val view = snapshot.view
    val page =
        remember(snapshot, route.groupId) {
            view.settleUpPage(route.groupId?.let(view::groupSettlePlan) ?: snapshot.settlePlan)
        }
    PbPushedPage(
        id = "settleUp",
        onBack = { navigator.back() },
        title = stringResource(R.string.pb_settle_up),
        contentTop = PbSpace.S24,
    ) {
        PbNoticeCard(stringResource(R.string.settle_notice), PbIcon.Shuffle)
        if (page.allSettled) {
            Spacer(Modifier.height(PbSpace.S24))
            PbEmptyState(
                stringResource(R.string.settle_all_square_title),
                stringResource(R.string.settle_all_square_body),
                illustration = PaybakRiveAsset.HomeAllSquare,
                testTag = "settleUp.allSettled",
            )
        }
        if (page.pay.isNotEmpty()) {
            Spacer(Modifier.height(PbSpace.S24))
            PlanSection(page.payTitle, page.pay, view)
        }
        if (page.get.isNotEmpty()) {
            Spacer(Modifier.height(PbSpace.S24))
            PlanSection(page.getTitle, page.get, view)
        }
    }
}

@Composable
private fun PlanSection(title: String, rows: List<SettleUpRow>, view: LedgerView) {
    val navigator = LocalMainNavigator.current
    val settle = stringResource(R.string.settle_settle)
    val remind = stringResource(R.string.settle_remind)
    Column(verticalArrangement = Arrangement.spacedBy(PbSpace.S8)) {
        PbSectionHeader(title)
        Column(verticalArrangement = Arrangement.spacedBy(PbSpace.S12)) {
            rows.forEach { row ->
                val pays = row.row.pay
                PbAttentionRow(
                    avatar =
                        view.person(row.friendId)?.avatarContent()
                            ?: PbAvatarContent.Initials(row.name.take(1)),
                    title = row.name,
                    detail = row.row.context,
                    badge = row.badge,
                    overdue = row.overdue,
                    amount = row.amount,
                    action = if (row.pendingPaymentId != null) null else if (pays) settle else remind,
                    onAction = {
                        navigator.open(if (pays) view.settleRoute(row) else row.remindRoute())
                    },
                    onClick = { navigator.open(row.bodyRoute()) },
                    testTag = "settleUp",
                    actionTag = "${if (pays) "pay" else "remind"}.${row.friendId}",
                )
            }
        }
    }
}
