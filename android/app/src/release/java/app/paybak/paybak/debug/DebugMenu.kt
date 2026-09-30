package app.paybak.paybak.debug

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import app.paybak.paybak.navigation.LocalMainNavigator
import app.paybak.paybak.navigation.Route

/** Release builds have no debug menu: the route closes at once. */
@Composable
fun DebugMenu(route: Route.DebugMenu) {
    val navigator = LocalMainNavigator.current
    LaunchedEffect(route) { navigator.dismissSheet() }
}
