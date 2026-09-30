package app.paybak.paybak.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.icons.PbIconImage
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbMotion
import app.paybak.paybak.ui.theme.PbSize
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles

/** `Style` of `Button / Text`. */
enum class PbTextButtonStyle {
    Primary,
    Secondary,
    Destructive,
}

/**
 * `Button / Text` (`PBTextButton`): See all, Skip, Continue with email or phone. 44 dp tall (the
 * hit area) and as wide as its label; pressed fades the whole button to 50 %.
 */
@Composable
fun PbTextButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: PbTextButtonStyle = PbTextButtonStyle.Primary,
    trailingChevron: Boolean = false,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
) {
    val press = rememberPressState(interactionSource)
    val alpha by
        animateFloatAsState(
            targetValue = if (press.isPressed) 0.5f else 1f,
            animationSpec = tween(PbMotion.PRESS_MILLIS),
            label = "PbTextButton alpha",
        )
    val color = if (enabled) style.labelColor else PbColors.Text.Disabled
    Row(
        modifier =
            modifier
                .height(PbSize.Tap)
                .graphicsLayer { this.alpha = alpha }
                .pressable(press, enabled, onClick = onClick),
        horizontalArrangement = Arrangement.spacedBy(PbSpace.S2),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, style = PbTextStyles.ButtonSmall, color = color, maxLines = 1)
        if (trailingChevron) {
            PbIconImage(
                PbIcon.ChevronRight,
                contentDescription = null,
                size = PbSize.IconSm,
                tint = color,
            )
        }
    }
}

private val PbTextButtonStyle.labelColor: Color
    get() =
        when (this) {
            PbTextButtonStyle.Primary -> PbColors.Text.Primary
            PbTextButtonStyle.Secondary -> PbColors.Text.Secondary
            PbTextButtonStyle.Destructive -> PbColors.Text.Destructive
        }

@Preview(showBackground = true)
@Composable
private fun PbTextButtonPreview() {
    Row(horizontalArrangement = Arrangement.spacedBy(PbSpace.S24)) {
        PbTextButton("See all", onClick = {}, trailingChevron = true)
        PbTextButton("Skip", onClick = {}, style = PbTextButtonStyle.Secondary)
        PbTextButton("Delete", onClick = {}, style = PbTextButtonStyle.Destructive)
    }
}
