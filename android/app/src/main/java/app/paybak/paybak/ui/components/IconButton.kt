package app.paybak.paybak.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.icons.PbIconImage
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbMaterial
import app.paybak.paybak.ui.theme.PbSize
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.pbMaterial

/** `Style` of `Button / Icon`. */
enum class PbIconButtonStyle {
    /** No fill; the back chevron and the copy button. */
    Plain,

    /** #F5F5F5 disc. */
    Filled,

    /** `Material/Glass Small`: floating toolbar buttons such as the Home bell. */
    Glass,

    /** Black disc with a white icon. */
    Inverse,
}

/**
 * `Button / Icon` (`PBIconButton`): a 44 dp circle with a 24 dp icon. [badge] shows the small
 * unread dot at the top right.
 *
 * @param contentDescription Required: icon-only buttons must be labelled for TalkBack.
 * @param diameter Figma resizes some instances, such as the 36 dp send button of the composer; the
 *   [badge] dot is placed for the 44 dp size.
 */
@Composable
fun PbIconButton(
    icon: PbIcon,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: PbIconButtonStyle = PbIconButtonStyle.Plain,
    badge: Boolean = false,
    diameter: Dp = PbSize.Tap,
    interactionSource: MutableInteractionSource? = null,
) {
    val press = rememberPressState(interactionSource)
    val fill = animatePressColor(style.fill(press.isPressed), label = "PbIconButton fill")
    val surface =
        if (style == PbIconButtonStyle.Glass) {
            Modifier.pbMaterial(PbMaterial.GlassSmall, CircleShape, fill)
        } else {
            Modifier.background(fill, CircleShape)
        }
    Box(
        modifier =
            modifier
                .size(diameter)
                .then(surface)
                .pressable(press, enabled = true, onClick = onClick)
                .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        val inverse = style == PbIconButtonStyle.Inverse
        PbIconImage(
            icon = icon,
            contentDescription = null,
            tint = if (inverse) PbColors.Icon.Inverse else PbColors.Icon.Primary,
        )
        if (badge) {
            BadgeDot(
                fill = if (inverse) PbColors.Bg.Primary else PbColors.Bg.Inverse,
                ring = if (inverse) PbColors.Bg.Inverse else PbColors.Bg.Primary,
                modifier = Modifier.align(Alignment.TopStart),
            )
        }
    }
}

/** The 10 dp unread dot at (26, 9) with a 2 dp ring outside it. */
@Composable
private fun BadgeDot(fill: Color, ring: Color, modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .offset(x = 24.dp, y = 7.dp)
                .size(14.dp)
                .background(ring, CircleShape)
                .padding(2.dp)
                .background(fill, CircleShape)
    )
}

/**
 * A bare 16–24 dp icon whose 44 dp tap target overhangs it, so it lays out at the icon's size: the
 * clear ✕ of a field, a chip's remove ✕, a person row's ✕, the composer's mic. Pressed dims it.
 */
@Composable
internal fun SmallIconButton(
    icon: PbIcon,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = PbSize.IconMd,
    tint: Color = PbColors.Icon.Secondary,
) {
    val press = rememberPressState(interactionSource = null)
    Box(modifier.size(size), contentAlignment = Alignment.Center) {
        Box(
            modifier =
                Modifier.requiredSize(PbSize.Tap)
                    .graphicsLayer { alpha = if (press.isPressed) 0.5f else 1f }
                    .pressable(press, enabled = true, onClick = onClick)
                    .semantics { this.contentDescription = contentDescription },
            contentAlignment = Alignment.Center,
        ) {
            PbIconImage(icon, contentDescription = null, size = size, tint = tint)
        }
    }
}

private fun PbIconButtonStyle.fill(pressed: Boolean): Color =
    when (this) {
        PbIconButtonStyle.Plain -> if (pressed) PbColors.Bg.Selected else Color.Transparent
        PbIconButtonStyle.Filled -> if (pressed) PbColors.Bg.CardPressed else PbColors.Bg.Card
        PbIconButtonStyle.Glass -> if (pressed) PbColors.Bg.Card else PbColors.Bg.Glass
        PbIconButtonStyle.Inverse ->
            if (pressed) PbColors.Bg.InversePressed else PbColors.Bg.Inverse
    }

@Preview(showBackground = true)
@Composable
private fun PbIconButtonPreview() {
    Row(horizontalArrangement = Arrangement.spacedBy(PbSpace.S12)) {
        PbIconButton(PbIcon.ChevronLeft, contentDescription = "Back", onClick = {})
        PbIconButton(
            PbIcon.Bell,
            "Notifications",
            onClick = {},
            style = PbIconButtonStyle.Filled,
            badge = true,
        )
        PbIconButton(
            PbIcon.Bell,
            "Notifications",
            onClick = {},
            style = PbIconButtonStyle.Glass,
            badge = true,
        )
        PbIconButton(
            PbIcon.Bell,
            "Notifications",
            onClick = {},
            style = PbIconButtonStyle.Inverse,
            badge = true,
        )
    }
}
