package app.paybak.paybak.feature.settle

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import app.paybak.paybak.R
import app.paybak.paybak.data.ledger.collectSnapshot
import app.paybak.paybak.domain.settle.oweBreakdown
import app.paybak.paybak.navigation.LocalLedger
import app.paybak.paybak.navigation.Route
import app.paybak.paybak.ui.components.PbBalance

/**
 * The `oweBreakdown` route (settleOweBreakdown, screens-settle §2): everyone you pay, behind Home's
 * You owe card, with the simplified-debts footnote.
 */
@Composable
fun OweBreakdownScreen(route: Route.OweBreakdown) {
    val snapshot by LocalLedger.current.collectSnapshot()
    val breakdown = remember(snapshot) { snapshot.view.oweBreakdown(snapshot.settlePlan) }
    BreakdownPage(
        id = "oweBreakdown",
        title = stringResource(R.string.pb_you_owe),
        section = stringResource(R.string.settle_who_you_owe),
        breakdown = breakdown,
        balance = PbBalance.Owe,
    )
}
