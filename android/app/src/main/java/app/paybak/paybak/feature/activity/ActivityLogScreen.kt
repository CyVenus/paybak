package app.paybak.paybak.feature.activity

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import app.paybak.paybak.R
import app.paybak.paybak.data.ledger.collectSnapshot
import app.paybak.paybak.domain.calc.timelineDays
import app.paybak.paybak.navigation.ActivityFilter
import app.paybak.paybak.navigation.LocalLedger
import app.paybak.paybak.navigation.LocalMainNavigator
import app.paybak.paybak.navigation.Route
import app.paybak.paybak.ui.components.PbPushHeader
import app.paybak.paybak.ui.components.addrecord.PbPinnedHeaderScreen
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbTextStyles

/**
 * The `activityLog` route (proposal: projects §3.7, groups §6.4, insights bar rows): the timeline
 * of one person, group, project or category in a month, titled "Build a Drone · History" (or "Food
 * · September"). Rows lead where they do on the Activity tab.
 */
@Composable
fun ActivityLogScreen(route: Route.ActivityLog) {
    val navigator = LocalMainNavigator.current
    val snapshot by LocalLedger.current.collectSnapshot()
    val view = snapshot.view
    val days = remember(snapshot, route.filter) { view.timelineDays(view.logEvents(route.filter)) }
    val subject = view.logSubject(route.filter)
    PbPinnedHeaderScreen(
        testTag = "screen.activityLog",
        header = {
            PbPushHeader(
                if (route.filter is ActivityFilter.Category) subject
                else stringResource(R.string.activity_log_title, subject),
                onBack = { navigator.back() },
                testTag = "activityLog",
            )
        },
    ) {
        if (days.isEmpty()) {
            Text(
                stringResource(R.string.activity_log_empty),
                style = PbTextStyles.Body,
                color = PbColors.Text.Secondary,
            )
        } else {
            TimelineDays(days, view, screen = "activityLog")
        }
    }
}
