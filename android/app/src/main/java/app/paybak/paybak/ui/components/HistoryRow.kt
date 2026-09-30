package app.paybak.paybak.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbSize
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles

/**
 * `Row / History` (`PBHistoryRow`): one entry of an expense's edit history, newest first: an 8 dp
 * grey dot and, except on the [last] entry, a hairline down to the next one. Rows stack with no
 * gap.
 */
@Composable
fun PbHistoryRow(
    text: String,
    date: String,
    modifier: Modifier = Modifier,
    last: Boolean = false,
) {
    Row(
        modifier =
            modifier.fillMaxWidth().height(IntrinsicSize.Min).semantics(mergeDescendants = true) {},
        horizontalArrangement = Arrangement.spacedBy(PbSpace.S12),
    ) {
        Column(
            modifier = Modifier.width(8.dp).fillMaxHeight().padding(top = 6.dp),
            verticalArrangement = Arrangement.spacedBy(PbSpace.S4),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(Modifier.size(8.dp).background(PbColors.Icon.Tertiary, CircleShape))
            if (!last) {
                Box(Modifier.width(PbSize.Hairline).weight(1f).background(PbColors.Border.Subtle))
            }
        }
        Column(
            modifier = Modifier.weight(1f).padding(bottom = if (last) 0.dp else PbSpace.S16),
            verticalArrangement = Arrangement.spacedBy(PbSpace.S2),
        ) {
            Text(text, style = PbTextStyles.Subheadline, color = PbColors.Text.Primary)
            Text(date, style = PbTextStyles.Footnote, color = PbColors.Text.Tertiary)
        }
    }
}

@Preview(showBackground = true, widthDp = 362)
@Composable
private fun PbHistoryRowPreview() {
    Column {
        PbHistoryRow("Kabir changed the amount from ₹17,500 to ₹18,000", "28 Sep")
        PbHistoryRow("Kabir added this", "27 Sep", last = true)
    }
}
