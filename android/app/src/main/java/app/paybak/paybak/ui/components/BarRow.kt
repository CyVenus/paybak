package app.paybak.paybak.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles

/**
 * `Row / Bar` (`PBBarRow`): a share bar row (Insights by category or group, "Paid vs fair share"),
 * 56 dp with no side padding: a 40 dp leading, the title with a grey [caption] ("51%", "Paid
 * ₹25,500"), the amount on the right and a Small bar filled to [progress] underneath.
 *
 * @param balance Owed "+" / Owe "−" in Amount/Medium; null is a plain Headline amount ("₹12,000",
 *   "Settled").
 * @param mark The fair-share tick on the bar (people rows).
 * @param onCard White leading circle, for rows inside #F5F5F5 cards (people); icon rows on white
 *   keep the grey circle.
 */
@Composable
fun PbBarRow(
    title: String,
    amount: String,
    progress: Float,
    leading: PbAvatarContent,
    modifier: Modifier = Modifier,
    caption: String? = null,
    balance: PbBalance? = null,
    mark: Float? = null,
    onCard: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val press = rememberPressState(interactionSource = null)
    val tap = if (onClick == null) Modifier else Modifier.pressable(press, true, onClick = onClick)
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(rowPressColor(press.isPressed, onCard))
                .then(tap)
                .semantics(mergeDescendants = true) {}
                .padding(vertical = PbSpace.S8),
        horizontalArrangement = Arrangement.spacedBy(PbSpace.S12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PbAvatar(leading, onCard = onCard)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(PbSpace.S8)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(PbSpace.S8),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(PbSpace.S8),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = title,
                        modifier = Modifier.weight(1f, fill = false),
                        style = PbTextStyles.Headline,
                        color = PbColors.Text.Primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (caption != null) {
                        Text(
                            caption,
                            style = PbTextStyles.Footnote,
                            color = PbColors.Text.Secondary,
                            maxLines = 1,
                        )
                    }
                }
                if (balance != null) {
                    SignedAmount(amount, balance)
                } else {
                    Text(
                        amount,
                        style = PbTextStyles.Headline,
                        color = PbColors.Text.Primary,
                        maxLines = 1,
                    )
                }
            }
            PbProgressBar(progress, mark = mark)
        }
    }
}

@Preview(showBackground = true, widthDp = 362)
@Composable
private fun PbBarRowPreview() {
    Column {
        PbBarRow("Rent", "₹12,000", 0.51f, PbAvatarContent.Symbol(PbIcon.Home), caption = "51%")
        PbBarRow(
            "Dev",
            "₹12,000",
            1f,
            PbAvatarContent.Art(PbPeepHead.Dev),
            caption = "Paid ₹25,500",
            balance = PbBalance.Owed,
            mark = 0.51f,
            onCard = true,
        )
    }
}
