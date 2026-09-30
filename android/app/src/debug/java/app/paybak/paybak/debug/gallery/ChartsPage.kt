package app.paybak.paybak.debug.gallery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import app.paybak.paybak.ui.components.PbAvatarContent
import app.paybak.paybak.ui.components.PbBalance
import app.paybak.paybak.ui.components.PbBarRow
import app.paybak.paybak.ui.components.PbBudgetCard
import app.paybak.paybak.ui.components.PbBudgetState
import app.paybak.paybak.ui.components.PbButton
import app.paybak.paybak.ui.components.PbButtonSize
import app.paybak.paybak.ui.components.PbButtonStyle
import app.paybak.paybak.ui.components.PbCard
import app.paybak.paybak.ui.components.PbLoanProgressCard
import app.paybak.paybak.ui.components.PbMonthlyBarChart
import app.paybak.paybak.ui.components.PbPeepHead
import app.paybak.paybak.ui.components.PbProgressBar
import app.paybak.paybak.ui.components.PbProgressBarSize
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.theme.PbSpace

@Composable
internal fun ChartsPage() {
    GalleryPage {
        GallerySection("Control / Progress Bar: Small · Large × Default · Projected · Over") {
            ProgressBars()
        }
        GallerySection("Show mark (examples)") {
            PbProgressBar(0.6f, mark = 0.51f)
            PbProgressBar(0.976f, size = PbProgressBarSize.Large, over = true, mark = 0.976f)
        }
        GallerySection("Row / Bar: Icon · Avatar × Neutral · Owed · Owe") { BarRows() }
        GallerySection("Chart / Monthly Bars") {
            PbCard {
                PbMonthlyBarChart(
                    values = listOf(18_400f, 21_950f, 19_600f, 20_600f, 22_150f, 23_300f),
                    labels = listOf("Apr", "May", "Jun", "Jul", "Aug", "Sep"),
                    description = "Your share, April to September",
                    modifier = Modifier.padding(PbSpace.S20),
                )
            }
        }
        GallerySection("Card / Budget: On track · Over budget · Closed") { BudgetCards() }
        GallerySection("Card / Loan Progress: Active · Paid back") {
            PbLoanProgressCard("₹6,000", "₹0", "₹6,000", 0f, "0% paid back")
            PbLoanProgressCard("₹4,500", "₹4,500", "₹0", 1f, "Paid back on 14 Sep", paidBack = true)
        }
    }
}

@Composable
private fun ProgressBars() {
    PbProgressBarSize.entries.forEach { size ->
        PbProgressBar(0.6f, size = size)
        PbProgressBar(0.87f, size = size, projected = 0.97f)
        PbProgressBar(0.976f, size = size, over = true)
    }
    GalleryLabel("Live: changes grow over 0.3 s")
    var progress by remember { mutableFloatStateOf(0.3f) }
    PbProgressBar(
        progress,
        size = PbProgressBarSize.Large,
        projected = (progress + 0.1f).coerceAtMost(1f),
    )
    Row(horizontalArrangement = Arrangement.spacedBy(PbSpace.S8)) {
        listOf(0f, 0.3f, 0.87f, 1f).forEach { value ->
            PbButton(
                "${(value * 100).toInt()}%",
                onClick = { progress = value },
                style = PbButtonStyle.Secondary,
                size = PbButtonSize.Small,
            )
        }
    }
}

@Composable
private fun BarRows() {
    val home = PbAvatarContent.Symbol(PbIcon.Home)
    val dev = PbAvatarContent.Art(PbPeepHead.Dev)
    Column {
        listOf(null, PbBalance.Owed, PbBalance.Owe).forEach { balance ->
            PbBarRow("Rent", "₹12,000", 0.51f, home, caption = "51%", balance = balance)
        }
    }
    PbCard {
        listOf(null to 0.51f, PbBalance.Owed to 1f, PbBalance.Owe to 0.35f).forEach {
            (balance, fill) ->
            PbBarRow(
                "Rent",
                "₹12,000",
                fill,
                dev,
                Modifier.padding(horizontal = PbSpace.S16),
                caption = "51%",
                balance = balance,
                mark = 0.51f,
                onCard = true,
            )
        }
    }
}

@Composable
private fun BudgetCards() {
    PbBudgetCard(
        "₹52,000",
        "of ₹60,000",
        progress = 52f / 60f,
        state = PbBudgetState.OnTrack("87% used", "₹8,000 left", projected = 58f / 60f),
        planned = "Planned items bring it to ₹58,000",
    )
    PbBudgetCard(
        "₹52,000",
        "of ₹60,000",
        progress = 0.976f,
        state = PbBudgetState.Over("₹1,500 over budget"),
        planned = "Planned items bring it to ₹58,000",
    )
    PbBudgetCard(
        "₹52,000",
        "of ₹60,000",
        progress = 52f / 60f,
        state = PbBudgetState.Closed("87% used", "₹8,000 left"),
    )
    GalleryLabel("Example: over budget (10-03)")
    PbBudgetCard(
        "₹61,500",
        "of ₹60,000",
        progress = 60f / 61.5f,
        state = PbBudgetState.Over("₹1,500 over budget"),
        planned = "All planned items are bought.",
    )
}
