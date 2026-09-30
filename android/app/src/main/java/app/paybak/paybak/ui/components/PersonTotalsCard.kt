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
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.icons.PbIconImage
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbShapes
import app.paybak.paybak.ui.theme.PbSize
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles

/** One column of a [PbPersonTotalsCard]. */
data class PbPersonTotal(val name: String, val amount: String, val avatar: PbAvatarContent)

/**
 * `Card / Person Totals` (`PBPersonTotalsCard`): the live per-person totals pinned above Continue
 * on Assign items: a status line (with a check once [complete]) and a right-aligned [note], then a
 * column per person with their share. TalkBack announces updates.
 */
@Composable
fun PbPersonTotalsCard(
    status: String,
    note: String,
    totals: List<PbPersonTotal>,
    modifier: Modifier = Modifier,
    complete: Boolean = true,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(PbColors.Bg.Card, PbShapes.Card)
                .padding(PbSpace.S16)
                .semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite },
        verticalArrangement = Arrangement.spacedBy(PbSpace.S6),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(PbSpace.S6),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (complete) {
                PbIconImage(PbIcon.CheckCircle, contentDescription = null, size = PbSize.IconSm)
            }
            Text(status, style = PbTextStyles.Footnote, color = PbColors.Text.Primary, maxLines = 1)
            Text(
                text = note,
                modifier = Modifier.weight(1f),
                style = PbTextStyles.Footnote,
                color = PbColors.Text.Secondary,
                textAlign = TextAlign.End,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(PbSpace.S12)) {
            totals.forEach { total ->
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(PbSpace.S8),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PbAvatar(total.avatar, size = PbAvatarSize.Sm, onCard = true)
                    Column {
                        Text(
                            total.name,
                            style = PbTextStyles.Footnote,
                            color = PbColors.Text.Secondary,
                            maxLines = 1,
                        )
                        Text(
                            total.amount,
                            style = PbTextStyles.AmountMedium,
                            color = PbColors.Text.Primary,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 362)
@Composable
private fun PbPersonTotalsCardPreview() {
    PbPersonTotalsCard(
        status = "All items assigned",
        note = "Includes GST and tip",
        totals =
            listOf(
                PbPersonTotal("You", "₹989", PbAvatarContent.Art(PbPeepHead.Arjun)),
                PbPersonTotal("Esha", "₹621", PbAvatarContent.Art(PbPeepHead.Esha)),
                PbPersonTotal("Dev", "₹690", PbAvatarContent.Art(PbPeepHead.Dev)),
            ),
    )
}
