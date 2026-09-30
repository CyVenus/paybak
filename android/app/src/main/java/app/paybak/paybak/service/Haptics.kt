package app.paybak.paybak.service

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView

/** The feedback kinds of app-architecture §7.2. */
enum class HapticKind {
    /** Chips, segments, tiles. */
    Selection,

    /** Save, Confirm. */
    Success,

    /** A validation error. */
    Warning,

    /** A Rive character tap. */
    Light,
}

/** Haptic feedback through the view (no vibrate permission), with fallbacks before API 30. */
class Haptics(private val view: View) {
    fun perform(kind: HapticKind) {
        val modern = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
        val constant =
            when (kind) {
                HapticKind.Selection -> HapticFeedbackConstants.CLOCK_TICK
                HapticKind.Success ->
                    if (modern) HapticFeedbackConstants.CONFIRM
                    else HapticFeedbackConstants.VIRTUAL_KEY
                HapticKind.Warning ->
                    if (modern) HapticFeedbackConstants.REJECT
                    else HapticFeedbackConstants.LONG_PRESS
                HapticKind.Light -> HapticFeedbackConstants.CONTEXT_CLICK
            }
        view.performHapticFeedback(constant)
    }
}

@Composable
fun rememberHaptics(): Haptics {
    val view = LocalView.current
    return remember(view) { Haptics(view) }
}
