package app.paybak.paybak.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.icons.PbIconImage
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbShapes
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles

/** `Style` of `Badge / Pill`. Overdue is the only coloured badge. */
enum class PbBadgeStyle(val fill: Color, val content: Color) {
    /** On white surfaces. */
    Muted(PbColors.Bg.Card, PbColors.Text.Secondary),

    /** On #F5F5F5 cards. */
    OnCard(PbColors.Bg.Primary, PbColors.Text.Secondary),
    Inverse(PbColors.Bg.Inverse, PbColors.Text.Inverse),

    /** Reserved for overdue items. */
    Overdue(PbColors.Bg.Destructive, PbColors.Text.Inverse),
}

/** `Badge / Pill` (`PBBadge`): a 24 dp status pill in Caption/1 with an optional 14 dp icon. */
@Composable
fun PbBadge(
    label: String,
    modifier: Modifier = Modifier,
    style: PbBadgeStyle = PbBadgeStyle.Muted,
    icon: PbIcon? = null,
) {
    Row(
        modifier =
            modifier
                .height(24.dp)
                .background(style.fill, PbShapes.Pill)
                .padding(horizontal = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(PbSpace.S4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            PbIconImage(icon, contentDescription = null, size = 14.dp, tint = style.content)
        }
        Text(text = label, style = PbTextStyles.Caption1, color = style.content, maxLines = 1)
    }
}

@Preview(showBackground = true)
@Composable
private fun PbBadgePreview() {
    Column(verticalArrangement = Arrangement.spacedBy(PbSpace.S8)) {
        PbBadgeStyle.entries.forEach { PbBadge("Due Fri", style = it, icon = PbIcon.Calendar) }
    }
}
