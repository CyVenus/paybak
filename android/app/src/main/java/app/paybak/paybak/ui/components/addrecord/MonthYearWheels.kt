package app.paybak.paybak.ui.components.addrecord

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.paybak.paybak.domain.format.Dates
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbTextStyles
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

private val RowHeight = 34.dp
private const val VISIBLE_ROWS = 7
private val SelectionBand = Color(0x1F767680)

/** Years offered when the calendar has no bound that way. */
private const val OPEN_YEARS = 10

/**
 * The month and year wheels behind "October 2026 ›" (add-expense §10.1 proposal; the iOS kit's
 * graphical picker does the same): months on the left, years on the right, the chosen row in a grey
 * band. Months outside [earliest]…[latest] are grey, and choosing one moves to the nearest allowed
 * month. Rows are tagged "[testTag].monthWheel.<1–12>" and "[testTag].yearWheel.<year>".
 */
@Composable
fun PbMonthYearWheels(
    month: YearMonth,
    onMonthChange: (YearMonth) -> Unit,
    modifier: Modifier = Modifier,
    earliest: LocalDate? = null,
    latest: LocalDate? = null,
    testTag: String = "calendar",
) {
    val first = earliest?.let(YearMonth::from)
    val last = latest?.let(YearMonth::from)
    val years =
        remember(first, last) {
            ((first?.year ?: (month.year - OPEN_YEARS))..(last?.year ?: (month.year + OPEN_YEARS)))
                .toList()
        }
    /** Moves to [next], or the nearest allowed month, and returns where the calendar is now. */
    fun choose(next: YearMonth): YearMonth {
        val allowed =
            when {
                first != null && next < first -> first
                last != null && next > last -> last
                else -> next
            }
        if (allowed != month) onMonthChange(allowed)
        return allowed
    }
    Box(modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Box(
            Modifier.fillMaxWidth()
                .height(RowHeight)
                .background(SelectionBand, RoundedCornerShape(8.dp))
        )
        Row(Modifier.fillMaxWidth().height(RowHeight * VISIBLE_ROWS)) {
            Wheel(
                labels = Month.entries.map(Dates::monthName),
                selected = month.monthValue - 1,
                enabled = { YearMonth.of(month.year, it + 1).let { m -> inRange(m, first, last) } },
                onSelect = { choose(YearMonth.of(month.year, it + 1)).monthValue - 1 },
                modifier = Modifier.weight(3f),
                testTag = "$testTag.monthWheel",
                tagOf = { (it + 1).toString() },
            )
            Wheel(
                labels = years.map(Int::toString),
                selected = years.indexOf(month.year).coerceAtLeast(0),
                enabled = { true },
                onSelect = { years.indexOf(choose(month.withYear(years[it])).year) },
                modifier = Modifier.weight(2f),
                testTag = "$testTag.yearWheel",
                tagOf = { years[it].toString() },
            )
        }
        // The wheel's curve: rows fade towards the top and bottom edges.
        Box(
            Modifier.fillMaxWidth()
                .height(RowHeight * VISIBLE_ROWS)
                .background(
                    Brush.verticalGradient(
                        0f to PbColors.Bg.Primary,
                        0.4f to Color.Transparent,
                        0.6f to Color.Transparent,
                        1f to PbColors.Bg.Primary,
                    )
                )
        )
    }
}

private fun inRange(month: YearMonth, first: YearMonth?, last: YearMonth?) =
    (first == null || month >= first) && (last == null || month <= last)

/**
 * One wheel: it snaps a row into the band and, once the scroll settles, reports it to [onSelect],
 * which answers the row to rest on (a month out of range springs back). Tapping a row scrolls it
 * into the band, and the wheel follows [selected] when the choice changes from outside.
 */
@Composable
private fun Wheel(
    labels: List<String>,
    selected: Int,
    enabled: (Int) -> Boolean,
    onSelect: (Int) -> Int,
    modifier: Modifier,
    testTag: String,
    tagOf: (Int) -> String,
) {
    val state = rememberLazyListState(initialFirstVisibleItemIndex = selected)
    val scope = rememberCoroutineScope()
    val current by rememberUpdatedState(selected)
    val select by rememberUpdatedState(onSelect)
    LaunchedEffect(selected) {
        if (state.firstVisibleItemIndex != selected || state.firstVisibleItemScrollOffset != 0) {
            state.animateScrollToItem(selected)
        }
    }
    LaunchedEffect(state) {
        snapshotFlow { state.isScrollInProgress }
            .filter { !it }
            .collect {
                val index = state.firstVisibleItemIndex
                if (index != current) {
                    val rest = select(index)
                    if (rest != index) state.animateScrollToItem(rest)
                }
            }
    }
    LazyColumn(
        modifier.fillMaxSize().testTag(testTag),
        state = state,
        contentPadding = PaddingValues(vertical = RowHeight * (VISIBLE_ROWS / 2)),
        flingBehavior = rememberSnapFlingBehavior(state),
    ) {
        itemsIndexed(labels) { index, label ->
            Text(
                label,
                Modifier.fillMaxWidth()
                    .height(RowHeight)
                    .clickable { scope.launch { state.animateScrollToItem(index) } }
                    .wrapContentHeight()
                    .padding(horizontal = 16.dp)
                    .testTag("$testTag.${tagOf(index)}")
                    .semantics { this.selected = index == selected },
                style = PbTextStyles.Body.copy(fontSize = 20.sp),
                color = if (enabled(index)) PbColors.Text.Primary else PbColors.Text.Tertiary,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 338)
@Composable
private fun PbMonthYearWheelsPreview() {
    PbMonthYearWheels(
        YearMonth.of(2026, 10),
        onMonthChange = {},
        earliest = LocalDate.of(2026, 10, 1),
    )
}
