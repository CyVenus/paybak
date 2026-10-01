package app.paybak.paybak.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp

/**
 * Marks a screen's pinned bottom buttons: reports how far they reach above the screen's bottom
 * edge, so the app's toast ("Loan added", "Component added") sits 12 dp above them instead of over
 * them ([MainNavigator.pinnedFooterHeight]); cleared when the screen goes.
 */
@Composable
fun Modifier.pinnedFooter(): Modifier {
    val navigator = LocalMainNavigator.current
    val density = LocalDensity.current
    val view = LocalView.current
    DisposableEffect(navigator) { onDispose { navigator.pinnedFooterHeight = 0.dp } }
    return onGloballyPositioned { coordinates ->
        val top = coordinates.positionInWindow().y
        navigator.pinnedFooterHeight = with(density) { (view.height - top).toDp() }
    }
}
