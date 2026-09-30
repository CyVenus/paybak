package app.paybak.paybak.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbMotion
import app.paybak.paybak.ui.theme.PbTextStyles

private val BarWidth = 32.dp
private val PlotHeight = 120.dp
private val LabelHeight = 20.dp
private val BarShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)

/**
 * `Chart / Monthly Bars` (`PBMonthlyBarChart`): six months of your share, 140 dp tall, columns
 * spread across the width. Bars grow up from a zero baseline, the tallest reaching 120 dp; the
 * current month is black with a black label, the others grey.
 *
 * @param values One per month, oldest first, in any unit (they're scaled to the largest).
 * @param labels The month names ("Apr" … "Sep").
 * @param description What TalkBack reads for the whole chart.
 */
@Composable
fun PbMonthlyBarChart(
    values: List<Float>,
    labels: List<String>,
    description: String,
    modifier: Modifier = Modifier,
    currentIndex: Int = values.lastIndex,
) {
    require(values.size == labels.size) { "One label per month" }
    val largest = values.maxOrNull()?.takeIf { it > 0f } ?: 1f
    Row(
        modifier =
            modifier.fillMaxWidth().height(PlotHeight + LabelHeight).clearAndSetSemantics {
                contentDescription = description
            },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        values.forEachIndexed { index, value ->
            val current = index == currentIndex
            val height by
                animateFloatAsState(
                    (value / largest).coerceIn(0f, 1f),
                    tween(300, easing = PbMotion.EaseOut),
                    label = "Month bar",
                )
            Column(Modifier.width(BarWidth), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier.size(BarWidth, PlotHeight),
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    Box(
                        Modifier.fillMaxWidth()
                            .fillMaxHeight(height)
                            .background(
                                if (current) PbColors.Chart.Fill else PbColors.Chart.Bar,
                                BarShape,
                            )
                    )
                }
                Box(Modifier.height(LabelHeight), contentAlignment = Alignment.Center) {
                    Text(
                        text = labels[index],
                        style = PbTextStyles.Footnote,
                        color = if (current) PbColors.Text.Primary else PbColors.Text.Secondary,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        softWrap = false,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 322)
@Composable
private fun PbMonthlyBarChartPreview() {
    PbMonthlyBarChart(
        values = listOf(18_400f, 21_900f, 19_600f, 20_600f, 22_100f, 23_300f),
        labels = listOf("Apr", "May", "Jun", "Jul", "Aug", "Sep"),
        description = "Your share by month",
    )
}
