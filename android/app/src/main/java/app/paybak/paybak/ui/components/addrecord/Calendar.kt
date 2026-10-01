package app.paybak.paybak.ui.components.addrecord

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.paybak.paybak.domain.format.Dates
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbShapes
import app.paybak.paybak.ui.theme.PbSize
import app.paybak.paybak.ui.theme.PbTextStyles
import java.time.LocalDate
import java.time.YearMonth

private val DayCell = 38.dp
private val WeekdayColor = Color(0x4D3C3C43)
private val Weekdays = listOf("S", "M", "T", "W", "T", "F", "S")

/** The kit picker's geometry (add-expense §10.1): 354 × 325 for a five-week month. */
private val HeaderHeight = 40.dp
private val HeaderDrop = 5.dp
private val WeekdayTop = 8.dp
private val WeekdayHeight = 20.dp
private val GridTop = 7.dp
private val RowGap = 12.dp
private val BottomPadding = 12.dp

/** The month and year wheels take the place of a five-week grid, so the sheet keeps its size. */
private val CalendarBodyHeight = WeekdayTop + WeekdayHeight + GridTop + DayCell * 5 + RowGap * 4

/**
 * The inline month calendar of the date sheets (add-expense §10.1): the iOS kit's graphical picker
 * drawn in Manrope. "October 2026 ›" opens the month and year wheels, ‹ › change month, then the
 * weekday initials and 38 dp day cells on a 50 dp grid; the selected day is a black circle with a
 * white SemiBold number, and days outside [earliest]…[latest] are grey and can't be picked. Days
 * are tagged "[testTag].day.<yyyy-MM-dd>".
 */
@Composable
fun PbCalendar(
    month: YearMonth,
    onMonthChange: (YearMonth) -> Unit,
    selected: LocalDate?,
    onSelect: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    earliest: LocalDate? = null,
    latest: LocalDate? = null,
    testTag: String = "calendar",
) {
    var choosingMonth by rememberSaveable { mutableStateOf(false) }
    Column(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .padding(bottom = BottomPadding)
            .testTag(testTag)
    ) {
        Row(
            Modifier.fillMaxWidth().height(HeaderHeight).offset(y = HeaderDrop),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MonthTitle(month, choosingMonth, "$testTag.month") { choosingMonth = !choosingMonth }
            Spacer(Modifier.weight(1f))
            if (!choosingMonth) {
                // The kit's chevrons sit 6 dp from the edge, so their targets overhang it.
                Row(Modifier.offset(x = 8.dp)) {
                    val canGoBack = earliest == null || month > YearMonth.from(earliest)
                    val canGoOn = latest == null || month < YearMonth.from(latest)
                    MonthArrow(true, "Previous month", canGoBack, "$testTag.previous") {
                        onMonthChange(month.minusMonths(1))
                    }
                    MonthArrow(false, "Next month", canGoOn, "$testTag.next") {
                        onMonthChange(month.plusMonths(1))
                    }
                }
            }
        }
        if (choosingMonth) {
            PbMonthYearWheels(
                month,
                onMonthChange,
                Modifier.height(CalendarBodyHeight),
                earliest = earliest,
                latest = latest,
                testTag = testTag,
            )
        } else {
            DayGrid(month, selected, onSelect, earliest, latest, testTag)
        }
    }
}

/** "October 2026" and the disclosure, which turns down while the wheels are open. */
@Composable
private fun MonthTitle(month: YearMonth, open: Boolean, testTag: String, onClick: () -> Unit) {
    val title = "${Dates.monthName(month.month)} ${month.year}"
    Row(
        Modifier.clip(PbShapes.Pill)
            .clickable(onClickLabel = "Choose month and year", onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .testTag(testTag),
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            title,
            style = PbTextStyles.Headline.copy(fontSize = 17.sp, fontWeight = FontWeight.SemiBold),
            color = PbColors.Text.Primary,
        )
        KitChevron(
            width = 5.5.dp,
            height = 9.dp,
            stroke = 1.8.dp,
            color = PbColors.Icon.Primary,
            modifier = Modifier.rotate(if (open) 90f else 0f),
        )
    }
}

/** A ‹ or › in `text/primary` with a 44 dp target; grey when there's no month that way. */
@Composable
private fun MonthArrow(
    back: Boolean,
    description: String,
    enabled: Boolean,
    testTag: String,
    onClick: () -> Unit,
) {
    Box(
        Modifier.size(PbSize.Tap)
            .clip(CircleShape)
            .clickable(enabled = enabled, onClickLabel = description, onClick = onClick)
            .testTag(testTag)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        KitChevron(
            width = 10.dp,
            height = 16.5.dp,
            stroke = 2.1.dp,
            color = if (enabled) PbColors.Icon.Primary else PbColors.Icon.Tertiary,
            modifier = Modifier.rotate(if (back) 180f else 0f),
        )
    }
}

/** The kit's SF chevron (pointing right): [width] × [height] outside, round [stroke]. */
@Composable
private fun KitChevron(width: Dp, height: Dp, stroke: Dp, color: Color, modifier: Modifier) {
    Canvas(modifier.size(width, height)) {
        val inset = stroke.toPx() / 2
        val path =
            Path().apply {
                moveTo(inset, inset)
                lineTo(size.width - inset, size.height / 2)
                lineTo(inset, size.height - inset)
            }
        drawPath(
            path,
            color,
            style = Stroke(stroke.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
    }
}

@Composable
private fun DayGrid(
    month: YearMonth,
    selected: LocalDate?,
    onSelect: (LocalDate) -> Unit,
    earliest: LocalDate?,
    latest: LocalDate?,
    testTag: String,
) {
    Row(
        Modifier.fillMaxWidth().padding(top = WeekdayTop),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Weekdays.forEach {
            Text(
                it,
                Modifier.size(DayCell, WeekdayHeight),
                style = PbTextStyles.Footnote.copy(fontWeight = FontWeight.SemiBold),
                color = WeekdayColor,
                textAlign = TextAlign.Center,
            )
        }
    }
    val lead = month.atDay(1).dayOfWeek.value % 7 // Sunday first
    val cells = List(lead) { null } + (1..month.lengthOfMonth()).map(month::atDay)
    Column(
        Modifier.padding(top = GridTop),
        verticalArrangement = Arrangement.spacedBy(RowGap),
    ) {
        cells.chunked(7).forEach { week ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                (week + List(7 - week.size) { null }).forEach { day ->
                    DayCell(day, day == selected, enabled(day, earliest, latest), onSelect, testTag)
                }
            }
        }
    }
}

private fun enabled(day: LocalDate?, earliest: LocalDate?, latest: LocalDate?): Boolean =
    day != null && (earliest == null || day >= earliest) && (latest == null || day <= latest)

@Composable
private fun DayCell(
    day: LocalDate?,
    selected: Boolean,
    enabled: Boolean,
    onSelect: (LocalDate) -> Unit,
    testTag: String,
) {
    if (day == null) {
        Box(Modifier.size(DayCell))
        return
    }
    Box(
        Modifier.size(DayCell)
            .clip(CircleShape)
            .background(if (selected) PbColors.Bg.Inverse else Color.Transparent)
            .clickable(enabled = enabled) { onSelect(day) }
            .testTag("$testTag.day.$day")
            .semantics {
                this.selected = selected
                contentDescription = Dates.day(day)
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            day.dayOfMonth.toString(),
            style =
                PbTextStyles.Body.copy(
                    fontSize = 20.sp,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                ),
            color =
                when {
                    selected -> PbColors.Text.Inverse
                    enabled -> PbColors.Text.Primary
                    else -> PbColors.Text.Tertiary
                },
        )
    }
}

@Preview(showBackground = true, widthDp = 354)
@Composable
private fun PbCalendarPreview() {
    PbCalendar(
        YearMonth.of(2026, 10),
        onMonthChange = {},
        selected = LocalDate.of(2026, 10, 4),
        onSelect = {},
        earliest = LocalDate.of(2026, 10, 1),
    )
}
