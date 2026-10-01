package app.paybak.paybak.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles

/** A person who can take a receipt item, and whether they had it. */
data class PbAssignee(val name: String, val avatar: PbAvatarContent, val selected: Boolean)

/**
 * `Row / Assign Item` (`PBAssignItemRow`): one receipt item on Assign items, on white with no side
 * padding: the item and its price, an optional [sharedCaption] ("Shared by 3 · ₹80 each"), and a
 * people chip per person (wrapping when there are many), black when they had it. Tapping a chip
 * calls [onToggle] with its index. Chips are tagged "[testTag].person.<name in lower case>".
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PbAssignItemRow(
    item: String,
    price: String,
    people: List<PbAssignee>,
    onToggle: (Int) -> Unit,
    modifier: Modifier = Modifier,
    sharedCaption: String? = null,
    showDivider: Boolean = true,
    testTag: String? = null,
) {
    Box(modifier.fillMaxWidth().partTag(testTag)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = PbSpace.S12),
            verticalArrangement = Arrangement.spacedBy(PbSpace.S8),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(PbSpace.S12)) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = item,
                        style = PbTextStyles.Headline,
                        color = PbColors.Text.Primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (sharedCaption != null) {
                        Text(
                            sharedCaption,
                            style = PbTextStyles.Footnote,
                            color = PbColors.Text.Secondary,
                            maxLines = 1,
                        )
                    }
                }
                Text(price, style = PbTextStyles.AmountMedium, color = PbColors.Text.Primary)
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(PbSpace.S8),
                verticalArrangement = Arrangement.spacedBy(PbSpace.S8),
            ) {
                people.forEachIndexed { index, person ->
                    PbCategoryChip(
                        label = person.name,
                        modifier = Modifier.partTag(testTag, "person.${person.name.lowercase()}"),
                        selected = person.selected,
                        onClick = { onToggle(index) },
                        leading = PbChipLeading.Avatar(person.avatar),
                    )
                }
            }
        }
        if (showDivider) PbDivider(Modifier.align(Alignment.BottomStart))
    }
}

@Preview(showBackground = true, widthDp = 362)
@Composable
private fun PbAssignItemRowPreview() {
    PbAssignItemRow(
        item = "Chicken biryani",
        price = "₹430",
        people =
            listOf(
                PbAssignee("You", PbAvatarContent.Art(PbPeepHead.Arjun), selected = false),
                PbAssignee("Esha", PbAvatarContent.Art(PbPeepHead.Esha), selected = false),
                PbAssignee("Dev", PbAvatarContent.Art(PbPeepHead.Dev), selected = true),
            ),
        onToggle = {},
    )
}
