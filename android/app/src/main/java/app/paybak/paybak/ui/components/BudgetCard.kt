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
import androidx.compose.ui.tooling.preview.Preview
import app.paybak.paybak.R
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.icons.PbIconImage
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbShapes
import app.paybak.paybak.ui.theme.PbSize
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles

/** `State` of `Card / Budget`. */
sealed interface PbBudgetState {
    /**
     * Under budget: the bar fills to spent ÷ budget, and [projected] shows (spent + planned) ÷
     * budget. [left] reads "₹8,000 left" ("… under budget" once the project is done).
     */
    data class OnTrack(val percent: String, val left: String, val projected: Float?) : PbBudgetState

    /** Over budget: the bar's red runs from the budget point, and [warning] says by how much. */
    data class Over(val warning: String) : PbBudgetState

    /** A closed project: the plain bar, no projection and no planned line. */
    data class Closed(val percent: String, val left: String) : PbBudgetState
}

/**
 * `Card / Budget` (`PBBudgetCard`): budget against spending on a project: "Spent" ₹52,000 "of
 * ₹60,000", a Large bar, the stats line and, unless closed, the [planned] line under a divider.
 * Over budget is the only red: the bar's over segment and the warning.
 *
 * @param progress Spent ÷ budget; over budget, budget ÷ spent (where the red starts).
 */
@Composable
fun PbBudgetCard(
    spent: String,
    budget: String,
    progress: Float,
    state: PbBudgetState,
    modifier: Modifier = Modifier,
    planned: String? = null,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(PbColors.Bg.Card, PbShapes.Card)
                .padding(PbSpace.S16),
        verticalArrangement = Arrangement.spacedBy(PbSpace.S12),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(PbSpace.S2)) {
            Text(
                stringResource(R.string.pb_spent),
                style = PbTextStyles.Footnote,
                color = PbColors.Text.Secondary,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(PbSpace.S8)) {
                Text(
                    spent,
                    Modifier.alignByBaseline(),
                    style = PbTextStyles.Title1,
                    color = PbColors.Text.Primary,
                )
                Text(
                    budget,
                    Modifier.alignByBaseline(),
                    style = PbTextStyles.Headline,
                    color = PbColors.Text.Secondary,
                )
            }
        }
        PbProgressBar(
            progress = progress,
            size = PbProgressBarSize.Large,
            projected = (state as? PbBudgetState.OnTrack)?.projected,
            over = state is PbBudgetState.Over,
            mark = if (state is PbBudgetState.Over) progress else null,
        )
        BudgetStats(state)
        if (planned != null && state !is PbBudgetState.Closed) {
            PbDivider()
            Text(planned, style = PbTextStyles.Footnote, color = PbColors.Text.Secondary)
        }
    }
}

@Composable
private fun BudgetStats(state: PbBudgetState) {
    when (state) {
        is PbBudgetState.OnTrack -> UsedAndLeft(state.percent, state.left)
        is PbBudgetState.Closed -> UsedAndLeft(state.percent, state.left)
        is PbBudgetState.Over ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(PbSpace.S6),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PbIconImage(
                    PbIcon.Alert,
                    contentDescription = null,
                    size = PbSize.IconSm,
                    tint = PbColors.Icon.Destructive,
                )
                Text(
                    state.warning,
                    style = PbTextStyles.Subheadline,
                    color = PbColors.Text.Destructive,
                )
            }
    }
}

@Composable
private fun UsedAndLeft(percent: String, left: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(percent, style = PbTextStyles.Subheadline, color = PbColors.Text.Secondary)
        Text(left, style = PbTextStyles.Subheadline, color = PbColors.Text.Primary)
    }
}

@Preview(showBackground = true, widthDp = 362)
@Composable
private fun PbBudgetCardPreview() {
    Column(verticalArrangement = Arrangement.spacedBy(PbSpace.S16)) {
        PbBudgetCard(
            "₹52,000",
            "of ₹60,000",
            progress = 52f / 60f,
            state = PbBudgetState.OnTrack("87% used", "₹8,000 left", projected = 58f / 60f),
            planned = "Planned items bring it to ₹58,000",
        )
        PbBudgetCard(
            "₹61,500",
            "of ₹60,000",
            progress = 60f / 61.5f,
            state = PbBudgetState.Over("₹1,500 over budget"),
            planned = "All planned items are bought.",
        )
    }
}
