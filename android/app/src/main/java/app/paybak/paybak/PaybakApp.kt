package app.paybak.paybak

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import app.paybak.paybak.navigation.AppFlow
import app.paybak.paybak.navigation.DebugStartScreen
import app.paybak.paybak.navigation.Destination
import app.paybak.paybak.navigation.LocalAppClock
import app.paybak.paybak.navigation.LocalDebugStartScreen
import app.paybak.paybak.navigation.LocalProfileStore
import app.paybak.paybak.navigation.LocalSubscriptions
import app.paybak.paybak.navigation.MainState
import app.paybak.paybak.navigation.canonicalBackStack
import app.paybak.paybak.navigation.rememberAppNavigator

/** What the app shows at launch. */
sealed interface StartTarget {
    /**
     * The normal flow, starting at [destination] (Splash unless a debug hook says otherwise).
     * [main] is where the app opens; [debugScreen] the debug start id its owning screen applies.
     */
    data class Flow(
        val destination: Destination,
        val main: MainState = MainState(),
        val debugScreen: String? = null,
    ) : StartTarget

    /** A debug-only tool, such as the component gallery, shown instead of the flow. */
    class Tool(val content: @Composable () -> Unit) : StartTarget
}

/**
 * The app's root. It provides the profile store and the clock to every screen (the ledger comes
 * with the app's main root, so onboarding never waits for it). Test tags are exposed as resource
 * ids, so UI tests and uiautomator/adb find elements by the flow.md ids (`screen.welcome1`,
 * `welcome.continue`, …).
 */
@Composable
fun PaybakApp(app: PaybakApplication, start: StartTarget) {
    val debugScreen = (start as? StartTarget.Flow)?.debugScreen
    CompositionLocalProvider(
        LocalProfileStore provides app.profileStore,
        LocalAppClock provides app.clock,
        LocalSubscriptions provides app.subscriptions,
        LocalDebugStartScreen provides
            remember(debugScreen) { debugScreen?.let(::DebugStartScreen) },
    ) {
        Box(Modifier.fillMaxSize().semantics { testTagsAsResourceId = true }) {
            when (start) {
                is StartTarget.Tool -> start.content()
                is StartTarget.Flow ->
                    AppFlow(
                        navigator = rememberAppNavigator(canonicalBackStack(start.destination)),
                        profileStore = app.profileStore,
                        mainStart = start.main,
                        links = app.links,
                        ledger = { app.ledger },
                    )
            }
        }
    }
}
