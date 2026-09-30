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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import app.paybak.paybak.R
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.icons.PbIconImage
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbShapes
import app.paybak.paybak.ui.theme.PbSize
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles

/**
 * `Card / Loan Progress` (`PBLoanProgressCard`): an IOU's progress on the loan detail: Original →
 * Paid → Remaining, a divider, a Large bar filled to paid ÷ original ([progress]) and the [caption]
 * ("0% paid back", "Paid back on 14 Sep"). [paidBack] adds the check before the caption. Never red:
 * an overdue installment shows its badge on its activity row instead.
 */
@Composable
fun PbLoanProgressCard(
    original: String,
    paid: String,
    remaining: String,
    progress: Float,
    caption: String,
    modifier: Modifier = Modifier,
    paidBack: Boolean = false,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(PbColors.Bg.Card, PbShapes.Card)
                .padding(PbSpace.S16),
        verticalArrangement = Arrangement.spacedBy(PbSpace.S12),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(PbSpace.S12)) {
            LoanStat(stringResource(R.string.pb_original), original, Modifier.weight(1f))
            LoanStat(stringResource(R.string.pb_paid), paid, Modifier.weight(1f))
            LoanStat(stringResource(R.string.pb_remaining), remaining, Modifier.weight(1f))
        }
        PbDivider()
        PbProgressBar(progress, size = PbProgressBarSize.Large)
        Row(
            horizontalArrangement = Arrangement.spacedBy(PbSpace.S6),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (paidBack) {
                PbIconImage(PbIcon.CheckCircle, contentDescription = null, size = PbSize.IconSm)
            }
            Text(caption, style = PbTextStyles.Footnote, color = PbColors.Text.Secondary)
        }
    }
}

@Composable
private fun LoanStat(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier.semantics(mergeDescendants = true) {},
        verticalArrangement = Arrangement.spacedBy(PbSpace.S2),
    ) {
        Text(label, style = PbTextStyles.Footnote, color = PbColors.Text.Secondary)
        Text(value, style = PbTextStyles.AmountMedium, color = PbColors.Text.Primary, maxLines = 1)
    }
}

@Preview(showBackground = true, widthDp = 362)
@Composable
private fun PbLoanProgressCardPreview() {
    Column(verticalArrangement = Arrangement.spacedBy(PbSpace.S16)) {
        PbLoanProgressCard("₹6,000", "₹0", "₹6,000", 0f, "0% paid back")
        PbLoanProgressCard("₹4,500", "₹4,500", "₹0", 1f, "Paid back on 14 Sep", paidBack = true)
    }
}
