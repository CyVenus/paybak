package app.paybak.paybak

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import app.paybak.paybak.data.ProfileStore
import app.paybak.paybak.navigation.AppFlow
import app.paybak.paybak.navigation.Destination
import app.paybak.paybak.navigation.canonicalBackStack
import app.paybak.paybak.navigation.rememberAppNavigator

/** What the app shows at launch. */
sealed interface StartTarget {
    /** The normal flow, starting at [destination] (Splash unless a debug hook says otherwise). */
    data class Flow(val destination: Destination) : StartTarget

    /** A debug-only tool, such as the component gallery, shown instead of the flow. */
    class Tool(val content: @Composable () -> Unit) : StartTarget
}

/**
 * The app's root. Test tags are exposed as resource ids, so UI tests and uiautomator/adb find
 * elements by the flow.md ids (`screen.welcome1`, `welcome.continue`, …).
 */
@Composable
fun PaybakApp(profileStore: ProfileStore, start: StartTarget) {
    Box(Modifier.fillMaxSize().semantics { testTagsAsResourceId = true }) {
        when (start) {
            is StartTarget.Tool -> start.content()
            is StartTarget.Flow ->
                AppFlow(
                    navigator = rememberAppNavigator(canonicalBackStack(start.destination)),
                    profileStore = profileStore,
                )
        }
    }
}
