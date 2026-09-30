package app.paybak.paybak.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.icons.PbIconImage
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbMotion
import app.paybak.paybak.ui.theme.PbSize
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles

/**
 * `Row / Currency` (`PBCurrencyRow`): symbol tile, name and code, and a radio. The whole 56 dp row
 * is the tap target. Symbols of up to two characters use Headline; longer ones (a letter code such
 * as "AED") use Caption/1.
 *
 * @param onCard True inside a #F5F5F5 card: the symbol tile turns white (Settings › Currency).
 */
@Composable
fun PbCurrencyRow(
    symbol: String,
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onCard: Boolean = false,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .height(56.dp)
                .selectable(
                    selected = selected,
                    interactionSource = null,
                    indication = null,
                    role = Role.RadioButton,
                    onClick = onClick,
                ),
        horizontalArrangement = Arrangement.spacedBy(PbSpace.S12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier =
                Modifier.size(PbSize.AvatarMd)
                    .background(if (onCard) PbColors.Bg.Primary else PbColors.Bg.Card, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = symbol,
                style = if (symbol.length <= 2) PbTextStyles.Headline else PbTextStyles.Caption1,
                color = PbColors.Text.Primary,
                maxLines = 1,
            )
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(PbSpace.S2)) {
            Text(
                text = title,
                style = PbTextStyles.Headline,
                color = PbColors.Text.Primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = subtitle,
                style = PbTextStyles.Subheadline,
                color = PbColors.Text.Secondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Radio(selected)
    }
}

@Composable
private fun Radio(selected: Boolean) {
    Crossfade(selected, animationSpec = tween(PbMotion.FADE_MILLIS), label = "Currency radio") { on
        ->
        if (on) {
            Box(
                modifier = Modifier.size(22.dp).background(PbColors.Bg.Inverse, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                PbIconImage(
                    PbIcon.Check,
                    contentDescription = null,
                    size = 14.dp,
                    tint = PbColors.Icon.Inverse,
                )
            }
        } else {
            Box(Modifier.size(22.dp).border(1.5.dp, PbColors.Bg.Indicator, CircleShape))
        }
    }
}

@Preview(showBackground = true, widthDp = 362)
@Composable
private fun PbCurrencyRowPreview() {
    Column {
        PbCurrencyRow(
            "₹",
            "Indian Rupee",
            "INR · Based on your region",
            selected = true,
            onClick = {},
        )
        PbCurrencyRow("AED", "UAE Dirham", "AED", selected = false, onClick = {})
    }
}
