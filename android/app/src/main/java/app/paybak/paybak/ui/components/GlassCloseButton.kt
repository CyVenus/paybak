package app.paybak.paybak.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import app.paybak.paybak.R
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.icons.PbIconImage
import app.paybak.paybak.ui.theme.PbColors

/** Kit glass colours on white: the 0.5 pt ring and the SF Symbol's `Labels - Vibrant` tint. */
private val KitRing = Color(0xFFE8E8E8)
private val KitGlyph = Color(0xFF1A1A1A)
private val KitShadow =
    Shadow(radius = 15.dp, color = Color.Black, offset = DpOffset(0.dp, 8.dp), alpha = 0.02f)

/**
 * The iOS kit glass ✕ ("Button - Liquid Glass - Symbol") drawn for Android: a white disc with a 0.5
 * dp #E8E8E8 ring and a faint shadow, holding a 19 dp ✕ (`close.svg` at 38 dp) in #1A1A1A. It is 50
 * dp on sheets and 44 dp in `Navigation / Modal Header`. Pressed fills it `bg/card`.
 */
@Composable
fun PbGlassCloseButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    diameter: Dp = 50.dp,
) {
    val press = rememberPressState(interactionSource = null)
    val fill =
        animatePressColor(
            if (press.isPressed) PbColors.Bg.Card else PbColors.Bg.Primary,
            label = "PbGlassCloseButton fill",
        )
    val description = stringResource(R.string.pb_close)
    Box(
        modifier =
            modifier
                .size(diameter)
                .dropShadow(CircleShape, KitShadow)
                .background(fill, CircleShape)
                .border(0.5.dp, KitRing, CircleShape)
                .pressable(press, enabled = true, onClick = onClick)
                .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        PbIconImage(PbIcon.Close, contentDescription = null, size = 38.dp, tint = KitGlyph)
    }
}

@Preview(showBackground = true)
@Composable
private fun PbGlassCloseButtonPreview() {
    PbGlassCloseButton(onClick = {})
}
