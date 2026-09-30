package app.paybak.paybak.feature.pickers

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.paybak.paybak.feature.RoutePlaceholder
import app.paybak.paybak.navigation.LocalAppClock
import app.paybak.paybak.navigation.LocalMainNavigator
import app.paybak.paybak.navigation.Route
import app.paybak.paybak.navigation.RouteResult
import app.paybak.paybak.ui.components.PbButton

/**
 * The `pickDate` route sheet (addExpenseDueDate, addExpenseDate). PLACEHOLDER owned by lane A (M3):
 * replace this file and keep the signature.
 */
@Composable
fun DatePickerSheet(route: Route.PickDate) {
    val navigator = LocalMainNavigator.current
    val clock = LocalAppClock.current
    RoutePlaceholder(route, title = "Date") {
        PbButton(
            "Today",
            onClick = { navigator.complete(route.request.id, RouteResult.Day(clock.today())) },
            Modifier.fillMaxWidth(),
        )
    }
}
