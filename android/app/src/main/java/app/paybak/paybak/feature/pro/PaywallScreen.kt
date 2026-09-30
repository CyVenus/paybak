package app.paybak.paybak.feature.pro

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import app.paybak.paybak.data.ledger.actions.startTrial
import app.paybak.paybak.feature.RoutePlaceholder
import app.paybak.paybak.navigation.LocalLedger
import app.paybak.paybak.navigation.LocalMainNavigator
import app.paybak.paybak.navigation.Route
import app.paybak.paybak.ui.components.PbButton

/**
 * The `paywall` route (paywall, proWelcome). PLACEHOLDER owned by lane C (M8): replace this file
 * and keep the signature. The trial flips the entitlement and continues, to exercise `requirePro`
 * and `finishPaywall`.
 */
@Composable
fun PaywallScreen(route: Route.Paywall) {
    val navigator = LocalMainNavigator.current
    val ledger = LocalLedger.current
    RoutePlaceholder(route) {
        PbButton(
            "Start free trial",
            onClick = {
                ledger.startTrial()
                navigator.finishPaywall()
            },
            modifier = Modifier.fillMaxWidth().testTag("paywall.trial"),
        )
    }
}
