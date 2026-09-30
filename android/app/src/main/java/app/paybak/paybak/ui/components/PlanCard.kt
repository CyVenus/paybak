package app.paybak.paybak.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbMotion
import app.paybak.paybak.ui.theme.PbShapes
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles

/**
 * `Card / Plan` (`PBPlanCard`): a paywall plan option, 104 dp tall; two share a row with a 20 dp
 * gap. Selected is black with white text (the detail at 72 %); tapping selects it. [badge] is the
 * white "Save 33%" pill.
 */
@Composable
fun PbPlanCard(
    period: String,
    price: String,
    detail: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badge: String? = null,
) {
    val fill by
        animateColorAsState(
            targetValue = if (selected) PbColors.Bg.Inverse else PbColors.Bg.Card,
            animationSpec = tween(PbMotion.FADE_MILLIS),
            label = "PbPlanCard fill",
        )
    val content = if (selected) PbColors.Text.Inverse else PbColors.Text.Primary
    Column(
        modifier =
            modifier
                .height(104.dp)
                .clip(PbShapes.Card)
                .background(fill)
                .selectable(
                    selected = selected,
                    interactionSource = null,
                    indication = null,
                    role = Role.RadioButton,
                    onClick = onClick,
                )
                .padding(PbSpace.S16),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(PbSpace.S8),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = period,
                modifier = Modifier.weight(1f),
                style = PbTextStyles.Headline,
                color = content,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (badge != null) PbBadge(badge, style = PbBadgeStyle.OnCard)
        }
        Column {
            Text(price, style = PbTextStyles.AmountMedium, color = content, maxLines = 1)
            Text(
                text = detail,
                modifier = Modifier.alpha(if (selected) 0.72f else 1f),
                style = PbTextStyles.Footnote,
                color = if (selected) PbColors.Text.Inverse else PbColors.Text.Secondary,
                maxLines = 1,
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 362)
@Composable
private fun PbPlanCardPreview() {
    Row(horizontalArrangement = Arrangement.spacedBy(PbSpace.S20)) {
        PbPlanCard("Yearly", "₹799/year", "₹67/month", true, {}, Modifier.weight(1f), "Save 33%")
        PbPlanCard("Monthly", "₹99/month", "Billed monthly", false, {}, Modifier.weight(1f))
    }
}
