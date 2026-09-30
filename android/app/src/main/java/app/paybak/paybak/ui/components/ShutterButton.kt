package app.paybak.paybak.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.paybak.paybak.R
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbMotion
import app.paybak.paybak.ui.theme.PbSpace

/**
 * `Control / Shutter` (`PBShutterButton`): the camera shutter of Scan receipt, always on the dark
 * `bg/camera` backdrop: a 76 dp white ring around a 62 dp white disc, which shrinks to 56 dp in
 * `bg/card-pressed` while pressed. A tap gives a light haptic.
 */
@Composable
fun PbShutterButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource? = null,
) {
    val press = rememberPressState(interactionSource)
    val haptics = LocalHapticFeedback.current
    val disc by
        animateDpAsState(
            if (press.isPressed) 56.dp else 62.dp,
            tween(PbMotion.PRESS_MILLIS),
            label = "Shutter disc",
        )
    val fill by
        animateColorAsState(
            if (press.isPressed) PbColors.Bg.CardPressed else PbColors.Bg.Primary,
            tween(PbMotion.PRESS_MILLIS),
            label = "Shutter fill",
        )
    val description = stringResource(R.string.pb_take_photo)
    Box(
        modifier =
            modifier
                .size(76.dp)
                .border(PbSpace.S4, PbColors.Bg.Primary, CircleShape)
                .pressable(press, enabled = true) {
                    haptics.performHapticFeedback(HapticFeedbackType.ContextClick)
                    onClick()
                }
                .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Box(Modifier.size(disc).background(fill, CircleShape))
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF2B2B2B)
@Composable
private fun PbShutterButtonPreview() {
    PbShutterButton(onClick = {}, Modifier.padding(PbSpace.S16))
}
