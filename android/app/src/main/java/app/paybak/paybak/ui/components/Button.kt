package app.paybak.paybak.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.icons.PbIconImage
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbMotion
import app.paybak.paybak.ui.theme.PbShapes
import app.paybak.paybak.ui.theme.PbSize
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles

/**
 * The four pill-button component sets: `Button / Primary`, `/ Secondary`, `/ On Card`, `/
 * Destructive`.
 */
enum class PbButtonStyle {
    /** Black pill: the one main action per screen. */
    Primary,

    /** #F5F5F5 pill on white backgrounds. */
    Secondary,

    /** White pill for #F5F5F5 cards. */
    OnCard,

    /** Red pill: delete and sign out only. */
    Destructive,
}

/** `Size` of the pill buttons: Large 52 dp (one per screen) or Small 36 dp (rows and cards). */
enum class PbButtonSize(
    val height: Dp,
    val horizontalPadding: Dp,
    val iconGap: Dp,
    val iconSize: Dp,
    val textStyle: TextStyle,
) {
    Large(PbSize.ButtonLg, PbSpace.S24, PbSpace.S8, PbSize.IconMd, PbTextStyles.ButtonLarge),
    Small(PbSize.ButtonSm, PbSpace.S16, PbSpace.S6, PbSize.IconSm, PbTextStyles.ButtonSmall),
}

/**
 * Paybak pill button (`PBButton`). Hugs its label unless the caller stretches it (screens use
 * `Modifier.fillMaxWidth()`); the icon and label stay centred as a group, and a new label
 * crossfades in place. Pressed swaps only the fill. A Small button's touch target still reaches the
 * 44 dp minimum through Compose's minimum-touch-target expansion.
 *
 * @param leadingIcon Tinted with the label colour, except brand logos that keep their colours.
 */
@Composable
fun PbButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: PbButtonStyle = PbButtonStyle.Primary,
    size: PbButtonSize = PbButtonSize.Large,
    leadingIcon: PbIcon? = null,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
) {
    val press = rememberPressState(interactionSource)
    val colors = style.colors(pressed = press.isPressed, enabled = enabled)
    val fill = animatePressColor(colors.fill, label = "PbButton fill")
    Row(
        modifier =
            modifier
                .height(size.height)
                .clip(PbShapes.Pill)
                .background(fill)
                .pressable(press, enabled, onClick = onClick)
                .padding(horizontal = size.horizontalPadding),
        horizontalArrangement = Arrangement.spacedBy(size.iconGap, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leadingIcon != null) {
            PbIconImage(
                leadingIcon,
                contentDescription = null,
                size = size.iconSize,
                tint = colors.content,
            )
        }
        AnimatedContent(
            targetState = label,
            transitionSpec = {
                fadeIn(tween(PbMotion.SWAP_MILLIS)) togetherWith
                    fadeOut(tween(PbMotion.SWAP_MILLIS)) using
                    SizeTransform(clip = false)
            },
            contentAlignment = Alignment.Center,
            label = "PbButton label",
        ) { shownLabel ->
            Text(
                text = shownLabel,
                color = colors.content,
                maxLines = 1,
                style = size.textStyle,
                autoSize = shrinkToFit(size.textStyle),
            )
        }
    }
}

private class ButtonColors(val fill: Color, val content: Color)

private fun PbButtonStyle.colors(pressed: Boolean, enabled: Boolean): ButtonColors =
    when (this) {
        PbButtonStyle.Primary ->
            when {
                !enabled -> ButtonColors(PbColors.Bg.Disabled, PbColors.Text.Disabled)
                pressed -> ButtonColors(PbColors.Bg.InversePressed, PbColors.Text.Inverse)
                else -> ButtonColors(PbColors.Bg.Inverse, PbColors.Text.Inverse)
            }

        PbButtonStyle.Secondary ->
            when {
                !enabled -> ButtonColors(PbColors.Bg.Card, PbColors.Text.Disabled)
                pressed -> ButtonColors(PbColors.Bg.CardPressed, PbColors.Text.Primary)
                else -> ButtonColors(PbColors.Bg.Card, PbColors.Text.Primary)
            }

        PbButtonStyle.OnCard ->
            when {
                !enabled -> ButtonColors(PbColors.Bg.Primary, PbColors.Text.Disabled)
                pressed -> ButtonColors(PbColors.Bg.CardPressed, PbColors.Text.Primary)
                else -> ButtonColors(PbColors.Bg.Primary, PbColors.Text.Primary)
            }

        PbButtonStyle.Destructive ->
            when {
                !enabled -> ButtonColors(PbColors.Bg.Disabled, PbColors.Text.Disabled)
                pressed -> ButtonColors(PbColors.Bg.DestructivePressed, PbColors.Text.Inverse)
                else -> ButtonColors(PbColors.Bg.Destructive, PbColors.Text.Inverse)
            }
    }

@Preview(showBackground = true)
@Composable
private fun PbButtonPreview() {
    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        PbButton(
            "Continue with Apple",
            onClick = {},
            Modifier.fillMaxWidth(),
            leadingIcon = PbIcon.Apple,
        )
        PbButton(
            "Continue with Google",
            onClick = {},
            Modifier.fillMaxWidth(),
            style = PbButtonStyle.Secondary,
            leadingIcon = PbIcon.Google,
        )
        PbButton("Continue", onClick = {}, enabled = false)
        PbButton(
            "Delete",
            onClick = {},
            style = PbButtonStyle.Destructive,
            size = PbButtonSize.Small,
        )
    }
}

/** A label too long for its pill (an alert's half-width "Close project") shrinks before it clips. */
private fun shrinkToFit(style: TextStyle) =
    TextAutoSize.StepBased(minFontSize = 11.sp, maxFontSize = style.fontSize, stepSize = 0.5.sp)
