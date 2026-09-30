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
 * The `pickGroup` route sheet (Record payment's For, Add expense's Group). PLACEHOLDER owned by
 * lane A (M3): replace this file and keep the signature.
 */
@Composable
fun GroupPickerSheet(route: Route.PickGroup) {
    val navigator = LocalMainNavigator.current
    RoutePlaceholder(route, title = "Group") {
        PbButton(
            "None",
            onClick = { navigator.complete(route.request.id, RouteResult.Group(null)) },
            Modifier.fillMaxWidth(),
        )
    }
}
