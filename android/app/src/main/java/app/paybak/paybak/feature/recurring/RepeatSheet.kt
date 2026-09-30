package app.paybak.paybak.feature.recurring

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.paybak.paybak.domain.model.RepeatRule
import app.paybak.paybak.feature.RoutePlaceholder
import app.paybak.paybak.navigation.LocalAppClock
import app.paybak.paybak.navigation.LocalMainNavigator
import app.paybak.paybak.navigation.Route
import app.paybak.paybak.navigation.RouteResult
import app.paybak.paybak.ui.components.PbButton

/**
 * The `repeatRule` route sheet (recurringRepeat). PLACEHOLDER owned by lane C (M9): replace this
 * file and keep the signature.
 */
@Composable
fun RepeatSheet(route: Route.RepeatRule) {
    val navigator = LocalMainNavigator.current
    val clock = LocalAppClock.current
    RoutePlaceholder(route, title = "Repeat") {
        PbButton(
            "Monthly",
            onClick = {
                val rule = RepeatRule(anchorDate = route.startDate ?: clock.today())
                navigator.complete(route.request.id, RouteResult.Repeat(rule))
            },
            Modifier.fillMaxWidth(),
        )
    }
}
