package app.paybak.paybak.feature.activity

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.paybak.paybak.domain.calc.LedgerView
import app.paybak.paybak.domain.calc.TimelineDay
import app.paybak.paybak.navigation.LocalMainNavigator
import app.paybak.paybak.ui.components.PbActivityRow
import app.paybak.paybak.ui.components.PbSectionHeader
import app.paybak.paybak.ui.theme.PbSpace

/**
 * Timeline day groups, newest first (activity §3.4): a Title/3 day header, then the rows, with 24
 * dp between days. Amounts are unsigned, black when you paid or were paid; drafts carry the Muted
 * "Draft" pill. [onTop] goes under the first header (the pending claims on Today). Rows are tagged
 * "[screen].row.<id>".
 */
@Composable
internal fun TimelineDays(
    days: List<TimelineDay>,
    view: LedgerView,
    screen: String,
    modifier: Modifier = Modifier,
    onTop: @Composable () -> Unit = {},
) {
    val navigator = LocalMainNavigator.current
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(PbSpace.S24)) {
        days.forEachIndexed { index, day ->
            Column(verticalArrangement = Arrangement.spacedBy(PbSpace.S8)) {
                PbSectionHeader(day.header)
                Column {
                    if (index == 0) onTop()
                    day.events.forEach { event ->
                        val route = view.route(event)
                        PbActivityRow(
                            leading = view.leading(event),
                            title = event.title,
                            subtitle = event.subtitle,
                            amount = event.amount,
                            amountPrimary = event.primary,
                            badge = event.badge,
                            onClick = route?.let { { navigator.open(it) } },
                            testTag = event.rowTag(screen),
                        )
                    }
                }
            }
        }
    }
}
