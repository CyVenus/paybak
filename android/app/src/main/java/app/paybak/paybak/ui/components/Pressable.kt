package app.paybak.paybak.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbMotion

/**
 * Press state of a Paybak control. Pressed variants are static Figma colours swapped while the
 * finger is down, so there is no ripple (README rule 11).
 */
internal class PressState(val source: MutableInteractionSource, pressed: State<Boolean>) {
    val isPressed by pressed
}

@Composable
internal fun rememberPressState(interactionSource: MutableInteractionSource?): PressState {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    return PressState(source, source.collectIsPressedAsState())
}

/** A ripple-free click target driven by [press]. */
internal fun Modifier.pressable(
    press: PressState,
    enabled: Boolean,
    role: Role = Role.Button,
    onClick: () -> Unit,
): Modifier =
    clickable(
        interactionSource = press.source,
        indication = null,
        enabled = enabled,
        role = role,
        onClick = onClick,
    )

/** Fades between the default and pressed colours in ≤ 100 ms. */
@Composable
internal fun animatePressColor(target: Color, label: String): Color {
    val color by animateColorAsState(target, tween(PbMotion.PRESS_MILLIS), label = label)
    return color
}

/**
 * The pressed fill of a tappable row, tile or card (README rule 11): `bg/card-pressed` on white
 * surfaces, a 6 % `bg/selected` overlay inside #F5F5F5 cards. Clear while not pressed.
 */
@Composable
internal fun rowPressColor(pressed: Boolean, onCard: Boolean): Color {
    val fill = if (onCard) PbColors.Bg.Selected else PbColors.Bg.CardPressed
    return animatePressColor(if (pressed) fill else fill.copy(alpha = 0f), label = "Row press")
}
