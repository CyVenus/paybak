package app.paybak.paybak.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles

/**
 * `Row / Transfer` (`PBTransferRow`): one payment of a settle-up plan ("Rohan owes Dev" ₹8,500), 64
 * dp, inside a [PbCard]: the from → to pair, the title and the amount. The divider starts at the
 * title; hide it on the last row.
 */
@Composable
fun PbTransferRow(
    title: String,
    amount: String,
    from: PbAvatarContent,
    to: PbAvatarContent,
    modifier: Modifier = Modifier,
    showDivider: Boolean = true,
    onClick: (() -> Unit)? = null,
) {
    val press = rememberPressState(interactionSource = null)
    val tap = if (onClick == null) Modifier else Modifier.pressable(press, true, onClick = onClick)
    Box(
        modifier
            .fillMaxWidth()
            .background(rowPressColor(press.isPressed, onCard = true))
            .then(tap)
            .semantics(mergeDescendants = true) {}
    ) {
        Row(
            modifier =
                Modifier.fillMaxWidth()
                    .heightIn(min = 64.dp)
                    .padding(horizontal = PbSpace.S16, vertical = PbSpace.S8),
            horizontalArrangement = Arrangement.spacedBy(PbSpace.S12),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PbAvatarPair(from, to, onCard = true)
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                style = PbTextStyles.Headline,
                color = PbColors.Text.Primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(amount, style = PbTextStyles.AmountMedium, color = PbColors.Text.Primary)
        }
        if (showDivider) PbDivider(Modifier.align(Alignment.BottomStart).padding(start = 116.dp))
    }
}

@Preview(showBackground = true, widthDp = 362)
@Composable
private fun PbTransferRowPreview() {
    PbCard {
        PbTransferRow(
            "Rohan owes Dev",
            "₹8,500",
            PbAvatarContent.Art(PbPeepHead.Rohan),
            PbAvatarContent.Art(PbPeepHead.Dev),
            showDivider = false,
        )
    }
}
