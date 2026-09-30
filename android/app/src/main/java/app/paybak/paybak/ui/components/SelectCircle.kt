package app.paybak.paybak.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.icons.PbIconImage
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbMotion
import app.paybak.paybak.ui.theme.PbSize
import app.paybak.paybak.ui.theme.PbSpace

/**
 * The 24 dp multi-select circle of `Row / Person` (Select On / Off) and `Row / Split Person`: a
 * black disc with a white 16 dp tick, or an empty 1.5 dp `border/strong` ring. Decorative: its row
 * carries the selected state for TalkBack.
 */
@Composable
fun PbSelectCircle(selected: Boolean, modifier: Modifier = Modifier) {
    val fill by
        animateColorAsState(
            targetValue = if (selected) PbColors.Bg.Inverse else Color.Transparent,
            animationSpec = tween(PbMotion.FADE_MILLIS),
            label = "PbSelectCircle fill",
        )
    Box(
        modifier =
            modifier
                .size(PbSize.IconLg)
                .background(fill, CircleShape)
                .border(FieldRingWidth, PbColors.Border.Strong, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            PbIconImage(
                PbIcon.Check,
                contentDescription = null,
                size = PbSize.IconSm,
                tint = PbColors.Icon.Inverse,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PbSelectCirclePreview() {
    Row(horizontalArrangement = Arrangement.spacedBy(PbSpace.S12)) {
        PbSelectCircle(selected = true)
        PbSelectCircle(selected = false)
    }
}
