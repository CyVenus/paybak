package app.paybak.paybak.ui.theme

import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext

/**
 * Motion values. Figma only designs the prototype transitions; the rest are the spec's proposals
 * (README §5.3), kept here so every component animates alike.
 */
object PbMotion {
    /** Figma EASE_IN_AND_OUT, used by every push. */
    val EaseInOut = CubicBezierEasing(0.42f, 0f, 0.58f, 1f)

    /** Figma EASE_OUT (splash dissolve). */
    val EaseOut = CubicBezierEasing(0f, 0f, 0.58f, 1f)

    /** Push navigation (prototype PUSH, 350 ms). */
    const val PUSH_MILLIS = 350

    /** Splash → next screen dissolve. */
    const val DISSOLVE_MILLIS = 400

    /** Skip → Get Started crossfade. */
    const val SKIP_MILLIS = 300

    /** Page-dot width and colour change between Welcome steps. */
    const val PAGE_DOTS_MILLIS = 250

    /** Pressed-state fill swap (≤ 100 ms, README rule 11). */
    const val PRESS_MILLIS = 100

    /** Small state fades: rings, radios, the avatar-option ring. */
    const val FADE_MILLIS = 150

    /** Skip appearing or hiding, and the "Step N of 4" number change. */
    const val SWAP_MILLIS = 200

    /** Text-field and code-digit caret blink half-period. */
    const val CARET_BLINK_MILLIS = 500
}

/**
 * True when the system "Remove animations" setting is on. Provided by [PaybakTheme]; read it to
 * skip decorative motion (caret blink, Rive's `reduceMotion` pose).
 */
val LocalReduceMotion = staticCompositionLocalOf { false }

/**
 * Tracks `Settings.Global.ANIMATOR_DURATION_SCALE == 0` (Android's reduce-motion switch) and
 * updates live while composed.
 */
@Composable
internal fun rememberSystemReduceMotion(): Boolean {
    val resolver = LocalContext.current.applicationContext.contentResolver
    val read =
        remember(resolver) {
            {
                Settings.Global.getFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) ==
                    0f
            }
        }
    var reduceMotion by remember(resolver) { mutableStateOf(read()) }
    DisposableEffect(resolver) {
        val observer =
            object : ContentObserver(Handler(Looper.getMainLooper())) {
                override fun onChange(selfChange: Boolean) {
                    reduceMotion = read()
                }
            }
        resolver.registerContentObserver(
            Settings.Global.getUriFor(Settings.Global.ANIMATOR_DURATION_SCALE),
            false,
            observer,
        )
        reduceMotion = read()
        onDispose { resolver.unregisterContentObserver(observer) }
    }
    return reduceMotion
}
