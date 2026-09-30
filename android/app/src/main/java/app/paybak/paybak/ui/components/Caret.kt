package app.paybak.paybak.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.paybak.paybak.ui.theme.LocalReduceMotion
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbMotion
import kotlinx.coroutines.delay

/** Width of every drawn caret (the code digits, the amount display). */
internal val CaretWidth = 2.dp

/**
 * Blinks every 0.5 s while [active], restarting visible whenever [restartKey] changes (the caret
 * moved); steady under reduce motion.
 */
@Composable
internal fun rememberCaretBlink(active: Boolean, restartKey: Any? = null): Boolean {
    val reduceMotion = LocalReduceMotion.current
    var visible by remember { mutableStateOf(true) }
    LaunchedEffect(active, restartKey, reduceMotion) {
        visible = true
        if (!active || reduceMotion) return@LaunchedEffect
        while (true) {
            delay(PbMotion.CARET_BLINK_MILLIS.toLong())
            visible = !visible
        }
    }
    return visible
}

/**
 * A blinking `bg/inverse` caret [height] tall, for controls that draw their own text in place of a
 * visible text field.
 */
@Composable
internal fun PbCaret(height: Dp, modifier: Modifier = Modifier, restartKey: Any? = null) {
    val visible = rememberCaretBlink(active = true, restartKey = restartKey)
    Box(
        modifier
            .size(CaretWidth, height)
            .alpha(if (visible) 1f else 0f)
            .background(PbColors.Bg.Inverse)
    )
}
