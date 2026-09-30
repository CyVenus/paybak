package app.paybak.paybak.feature.pickers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import app.paybak.paybak.R
import app.paybak.paybak.data.ledger.collectSnapshot
import app.paybak.paybak.domain.addrecord.DateCopy
import app.paybak.paybak.navigation.DateKind
import app.paybak.paybak.navigation.LocalAppClock
import app.paybak.paybak.navigation.LocalLedger
import app.paybak.paybak.navigation.LocalMainNavigator
import app.paybak.paybak.navigation.Route
import app.paybak.paybak.navigation.RouteResult
import app.paybak.paybak.ui.components.PbButton
import app.paybak.paybak.ui.components.PbSheet
import app.paybak.paybak.ui.components.PbTextButton
import app.paybak.paybak.ui.components.addrecord.PbCalendar
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles
import java.time.LocalDate
import java.time.YearMonth

/**
 * The `pickDate` route sheet (`addExpenseDueDate`, `addExpenseDate`; add-expense §10). Due date:
 * the calendar from tomorrow on, "Sun 4 Oct · in 4 days", the reminder schedule, Set due date and
 * No due date. Date: when something happened, today or earlier, and Set date. ✕, the scrim and a
 * swipe close it unchanged.
 */
@Composable
fun DatePickerSheet(route: Route.PickDate) {
    val navigator = LocalMainNavigator.current
    val snapshot by LocalLedger.current.collectSnapshot()
    val today = LocalAppClock.current.today()
    val due = route.kind == DateKind.DueDate
    val earliest = route.earliest ?: if (due) today.plusDays(1) else null
    val latest = route.latest ?: if (due) null else today
    var day by rememberSaveable {
        mutableStateOf(route.selected ?: if (due) today.plusDays(1) else today)
    }
    var month by rememberSaveable { mutableStateOf(YearMonth.from(day)) }
    var answer by remember { mutableStateOf<RouteResult.Day?>(null) }
    val tag = if (due) "dueDate" else "date"
    PbSheet(
        onDismiss = {
            answer?.let { navigator.complete(route.request.id, it) } ?: navigator.dismissSheet()
        },
        title =
            if (due) stringResource(R.string.add_due_date) else stringResource(R.string.add_date),
        testTag = "$tag.sheet",
    ) { dismiss ->
        Column(
            Modifier.testTag("screen.pickDate"),
            verticalArrangement = Arrangement.spacedBy(PbSpace.S16),
        ) {
            PbCalendar(
                month = month,
                onMonthChange = { month = it },
                selected = day,
                onSelect = { day = it },
                earliest = earliest,
                latest = latest,
                testTag = "$tag.calendar",
            )
            Column(verticalArrangement = Arrangement.spacedBy(PbSpace.S4)) {
                Text(
                    DateCopy.summary(day, today, due),
                    Modifier.testTag("$tag.summary"),
                    style = PbTextStyles.Headline,
                    color = PbColors.Text.Primary,
                )
                if (due) {
                    Text(
                        DateCopy.reminderHint(snapshot.ledger.settings.reminderSchedule),
                        style = PbTextStyles.Footnote,
                        color = PbColors.Text.Secondary,
                    )
                }
            }
            Column(
                Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(PbSpace.S8),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                PbButton(
                    if (due) stringResource(R.string.add_set_due_date)
                    else stringResource(R.string.add_set_date),
                    onClick = {
                        answer = RouteResult.Day(day)
                        dismiss()
                    },
                    modifier = Modifier.fillMaxWidth().testTag("$tag.set"),
                    enabled = inRange(day, earliest, latest),
                )
                if (due && route.allowsNone) {
                    PbTextButton(
                        stringResource(R.string.add_no_due_date),
                        onClick = {
                            answer = RouteResult.Day(null)
                            dismiss()
                        },
                        modifier = Modifier.testTag("$tag.none"),
                    )
                }
            }
        }
    }
}

private fun inRange(day: LocalDate, earliest: LocalDate?, latest: LocalDate?): Boolean =
    (earliest == null || day >= earliest) && (latest == null || day <= latest)
