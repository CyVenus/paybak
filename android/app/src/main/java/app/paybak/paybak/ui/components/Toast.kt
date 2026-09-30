package app.paybak.paybak.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.icons.PbIconImage
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbMotion
import app.paybak.paybak.ui.theme.PbShapes
import app.paybak.paybak.ui.theme.PbSize
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles
import kotlinx.coroutines.delay

private const val TOAST_VISIBLE_MILLIS = 2_000L
private val ToastRise = 8.dp

/**
 * `Overlay / Toast` (`PBToast`): a short confirmation such as "UPI ID copied", in a 44 dp black
 * capsule with an optional white icon. It is never a link. Show it through a [PbToastHost].
 */
@Composable
fun PbToast(text: String, modifier: Modifier = Modifier, icon: PbIcon? = PbIcon.CheckCircle) {
    Row(
        modifier =
            modifier
                .height(PbSize.Tap)
                .background(PbColors.Bg.Inverse, PbShapes.Pill)
                .padding(start = PbSpace.S16, end = PbSpace.S20),
        horizontalArrangement = Arrangement.spacedBy(PbSpace.S8),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            PbIconImage(
                icon,
                contentDescription = null,
                size = PbSize.IconMd,
                tint = PbColors.Icon.Inverse,
            )
        }
        Text(text, style = PbTextStyles.ButtonSmall, color = PbColors.Text.Inverse, maxLines = 1)
    }
}

/** The toast a [PbToastHost] shows. Each [show] is a new message, even with the same text. */
@Stable
class PbToastState {
    internal var message by mutableStateOf<Message?>(null)

    fun show(text: String) {
        message = Message(text)
    }

    internal class Message(val text: String)
}

@Composable fun rememberPbToastState(): PbToastState = remember { PbToastState() }

/**
 * Shows [state]'s toast: it fades in rising 8 dp (0.2 s), stays 2 s and fades out (0.2 s). A new
 * message restarts the 2 s. TalkBack announces it.
 */
@Composable
fun PbToastHost(state: PbToastState, modifier: Modifier = Modifier) {
    val message = state.message
    LaunchedEffect(message) {
        if (message != null) {
            delay(TOAST_VISIBLE_MILLIS)
            state.message = null
        }
    }
    val rise = with(LocalDensity.current) { ToastRise.roundToPx() }
    AnimatedContent(
        targetState = message,
        modifier = modifier,
        transitionSpec = {
            (fadeIn(tween(PbMotion.SWAP_MILLIS)) +
                slideInVertically(tween<IntOffset>(PbMotion.SWAP_MILLIS)) { rise }) togetherWith
                fadeOut(tween(PbMotion.SWAP_MILLIS))
        },
        contentAlignment = Alignment.Center,
        contentKey = { it != null },
        label = "PbToastHost",
    ) { shown ->
        if (shown != null) {
            PbToast(shown.text, Modifier.semantics { liveRegion = LiveRegionMode.Polite })
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PbToastPreview() {
    Column(Modifier.padding(PbSpace.S20), verticalArrangement = Arrangement.spacedBy(PbSpace.S12)) {
        PbToast("UPI ID copied")
        PbToast("Payment recorded", icon = null)
    }
}
