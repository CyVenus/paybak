package app.paybak.paybak.feature.settle

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import app.paybak.paybak.R
import app.paybak.paybak.data.ledger.collectSnapshot
import app.paybak.paybak.domain.settle.owedBreakdown
import app.paybak.paybak.navigation.LocalLedger
import app.paybak.paybak.navigation.Route
import app.paybak.paybak.ui.components.PbBalance

/**
 * The `owedBreakdown` route (settleOwedBreakdown, screens-settle §1): everyone who owes you,
 * overdue first, behind Home's You’re owed card.
 */
@Composable
fun OwedBreakdownScreen(route: Route.OwedBreakdown) {
    val snapshot by LocalLedger.current.collectSnapshot()
    val breakdown = remember(snapshot) { snapshot.view.owedBreakdown(snapshot.settlePlan) }
    BreakdownPage(
        id = "owedBreakdown",
        title = stringResource(R.string.pb_you_are_owed),
        section = stringResource(R.string.settle_who_owes_you),
        breakdown = breakdown,
        balance = PbBalance.Owed,
    )
}
