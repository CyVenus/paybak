package app.paybak.paybak.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbMotion
import app.paybak.paybak.ui.theme.PbShapes
import app.paybak.paybak.ui.theme.PbSpace

private val TrackWidth = 64.dp
private val TrackHeight = 28.dp
private val KnobWidth = 38.dp
private val KnobHeight = 24.dp
private val KnobInset = 2.dp

/** The kit switch's off track (iOS `Fills - Primary` on white). */
private val TrackOff = Color(0xFFE9E9EA)
private val KnobShadow =
    Shadow(radius = 8.dp, color = Color.Black, offset = DpOffset(0.dp, 3.dp), alpha = 0.12f)

/**
 * The iOS kit `Toggle - Switch` drawn for Android (components-app.md §1.2): a 64 × 28 capsule,
 * black (`bg/inverse`) when on and #E9E9EA when off, with a white 38 × 24 knob that slides across.
 *
 * @param onCheckedChange Null when a parent handles the tap, as a `Row / Setting` does.
 */
@Composable
fun PbSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val track by
        animateColorAsState(
            targetValue = if (checked) PbColors.Bg.Inverse else TrackOff,
            animationSpec = tween(PbMotion.SWAP_MILLIS),
            label = "PbSwitch track",
        )
    val knobX by
        animateDpAsState(
            targetValue = if (checked) TrackWidth - KnobWidth - KnobInset else KnobInset,
            animationSpec = tween(PbMotion.SWAP_MILLIS),
            label = "PbSwitch knob",
        )
    val toggle =
        if (onCheckedChange == null) {
            Modifier
        } else {
            Modifier.toggleable(
                value = checked,
                interactionSource = null,
                indication = null,
                enabled = enabled,
                role = Role.Switch,
                onValueChange = onCheckedChange,
            )
        }
    Box(
        modifier
            .then(toggle)
            .size(TrackWidth, TrackHeight)
            .alpha(if (enabled) 1f else 0.4f)
            .background(track, PbShapes.Pill)
    ) {
        Box(
            Modifier.offset { IntOffset(knobX.roundToPx(), KnobInset.roundToPx()) }
                .size(KnobWidth, KnobHeight)
                .dropShadow(PbShapes.Pill, KnobShadow)
                .background(PbColors.Bg.Primary, PbShapes.Pill)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PbSwitchPreview() {
    Row(horizontalArrangement = Arrangement.spacedBy(PbSpace.S12)) {
        PbSwitch(checked = true, onCheckedChange = {})
        PbSwitch(checked = false, onCheckedChange = {})
    }
}
