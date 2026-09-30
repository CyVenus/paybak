package app.paybak.paybak.ui.components.addrecord

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.paybak.paybak.domain.format.Dates
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.icons.PbIconImage
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbSize
import app.paybak.paybak.ui.theme.PbTextStyles
import java.time.LocalDate
import java.time.YearMonth

private val DayCell = 38.dp
private val WeekdayColor = Color(0x4D3C3C43)
private val Weekdays = listOf("S", "M", "T", "W", "T", "F", "S")

/**
 * The inline month calendar of the date sheets (add-expense §10.1): the iOS kit's graphical picker
 * drawn in Manrope. "October 2026" with ‹ › to change month, the weekday initials, and 38 dp day
 * cells on a 50 dp grid; the selected day is a black circle with a white SemiBold number, and days
 * outside [earliest]…[latest] are grey and can't be picked. Days are tagged
 * "[testTag].day.<yyyy-MM-dd>".
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
    Column(modifier.fillMaxWidth().padding(horizontal = 8.dp).testTag(testTag)) {
        Row(
            Modifier.fillMaxWidth().height(40.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "${Dates.monthName(month.month)} ${month.year}",
                Modifier.weight(1f).padding(start = 8.dp),
                style =
                    PbTextStyles.Headline.copy(fontSize = 17.sp, fontWeight = FontWeight.SemiBold),
                color = PbColors.Text.Primary,
            )
            val canGoBack = earliest == null || month > YearMonth.from(earliest)
            val canGoOn = latest == null || month < YearMonth.from(latest)
            MonthArrow(PbIcon.ChevronLeft, "Previous month", canGoBack, "$testTag.previous") {
                onMonthChange(month.minusMonths(1))
            }
            MonthArrow(PbIcon.ChevronRight, "Next month", canGoOn, "$testTag.next") {
                onMonthChange(month.plusMonths(1))
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Weekdays.forEach {
                Text(
                    it,
                    Modifier.size(DayCell, 22.dp),
                    style = PbTextStyles.Footnote.copy(fontWeight = FontWeight.SemiBold),
                    color = WeekdayColor,
                    textAlign = TextAlign.Center,
                )
            }
        }
        val first = month.atDay(1)
        val lead = first.dayOfWeek.value % 7 // Sunday first
        val cells = List(lead) { null } + (1..month.lengthOfMonth()).map(month::atDay)
        cells.chunked(7).forEach { week ->
            Row(
                Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                (week + List(7 - week.size) { null }).forEach { day ->
                    DayCell(day, day == selected, enabled(day, earliest, latest), onSelect, testTag)
                }
            }
        }
    }
}

/** A ‹ or › in `text/primary` with a 44 dp target; grey when there's no month that way. */
@Composable
private fun MonthArrow(
    icon: PbIcon,
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
        PbIconImage(
            icon,
            contentDescription = null,
            tint = if (enabled) PbColors.Icon.Primary else PbColors.Icon.Tertiary,
        )
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
