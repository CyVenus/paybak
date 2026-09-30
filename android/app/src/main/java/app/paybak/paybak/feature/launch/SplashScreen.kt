package app.paybak.paybak.feature.launch

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import app.paybak.paybak.feature.PlaceholderScreen
import app.paybak.paybak.ui.components.PbButton
import app.paybak.paybak.ui.components.PbLogo
import app.paybak.paybak.ui.components.PbLogoLayout
import kotlinx.coroutines.delay

/** Splash hold before moving on (prototype AFTER_TIMEOUT 1500 ms). */
private const val SPLASH_HOLD_MILLIS = 1_500L

/**
 * `splash`. PLACEHOLDER: the Launch phase replaces the body with the animated lockup
 * (screens-launch.md §1). It already moves on after the Figma timeout.
 */
@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val currentOnFinished by rememberUpdatedState(onFinished)
    LaunchedEffect(Unit) {
        delay(SPLASH_HOLD_MILLIS)
        currentOnFinished()
    }
    PlaceholderScreen(id = "splash", phase = "Launch", content = { PbLogo(PbLogoLayout.Stacked) }) {
        PbButton("Next", onClick = onFinished, Modifier.fillMaxWidth())
    }
}
