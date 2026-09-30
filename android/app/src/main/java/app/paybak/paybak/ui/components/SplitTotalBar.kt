package app.paybak.paybak.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.icons.PbIconImage
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbShapes
import app.paybak.paybak.ui.theme.PbSize
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles

/**
 * `Card / Split Total` (`PBSplitTotalBar`): the live footer of the split editor, a 56 dp #F5F5F5
 * bar. [left] is what's still to assign ("₹0 left"), [detail] the progress ("₹2,650 of ₹2,800").
 * [isError] (anything left, or over-assigned) adds the alert and turns [left] red; the screen keeps
 * Done disabled meanwhile. TalkBack announces changes.
 */
@Composable
fun PbSplitTotalBar(
    left: String,
    detail: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(PbColors.Bg.Card, PbShapes.Card)
                .padding(horizontal = PbSpace.S16)
                .semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite },
        horizontalArrangement = Arrangement.spacedBy(PbSpace.S8),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isError) {
            PbIconImage(
                PbIcon.Alert,
                contentDescription = null,
                size = PbSize.IconMd,
                tint = PbColors.Icon.Destructive,
            )
        }
        Text(
            text = left,
            style = PbTextStyles.AmountMedium,
            color = if (isError) PbColors.Text.Destructive else PbColors.Text.Secondary,
            maxLines = 1,
        )
        Text(
            text = detail,
            modifier = Modifier.weight(1f),
            style = PbTextStyles.Subheadline,
            color = PbColors.Text.Secondary,
            textAlign = TextAlign.End,
            maxLines = 1,
        )
    }
}

@Preview(showBackground = true, widthDp = 362)
@Composable
private fun PbSplitTotalBarPreview() {
    Column(verticalArrangement = Arrangement.spacedBy(PbSpace.S16)) {
        PbSplitTotalBar("₹0 left", "₹2,800 of ₹2,800")
        PbSplitTotalBar("₹150 left", "₹2,650 of ₹2,800", isError = true)
    }
}
