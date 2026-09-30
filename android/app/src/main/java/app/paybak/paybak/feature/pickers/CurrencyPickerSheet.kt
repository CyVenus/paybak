package app.paybak.paybak.feature.pickers

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.paybak.paybak.feature.RoutePlaceholder
import app.paybak.paybak.navigation.LocalMainNavigator
import app.paybak.paybak.navigation.Route
import app.paybak.paybak.navigation.RouteResult
import app.paybak.paybak.ui.components.PbButton

/**
 * The `pickCurrency` route sheet (addExpenseCurrency). PLACEHOLDER owned by lane A (M3): replace
 * this file and keep the signature.
 */
@Composable
fun CurrencyPickerSheet(route: Route.PickCurrency) {
    val navigator = LocalMainNavigator.current
    RoutePlaceholder(route, title = route.title ?: "Currency") {
        PbButton(
            "AED",
            onClick = { navigator.complete(route.request.id, RouteResult.Currency("AED")) },
            Modifier.fillMaxWidth(),
        )
    }
}
