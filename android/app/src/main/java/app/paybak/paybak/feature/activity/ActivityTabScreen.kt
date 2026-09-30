package app.paybak.paybak.feature.activity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import app.paybak.paybak.data.ledger.collectSnapshot
import app.paybak.paybak.feature.insights.InsightsView
import app.paybak.paybak.navigation.ActivitySegment
import app.paybak.paybak.navigation.LocalLedger
import app.paybak.paybak.navigation.LocalMainNavigator
import app.paybak.paybak.navigation.LocalTabBarPadding
import app.paybak.paybak.navigation.Route
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbLayout
import app.paybak.paybak.ui.theme.PbSpace

/**
 * The `activity` tab root (M2's container, activity §3): the [ActivityHeader] over the selected
 * segment's content. The timeline (lane A's [ActivityTimelineView]) keeps the header fixed while it
 * scrolls; Insights (lane C's [InsightsView]) scrolls the header away and collapses it to its
 * inline bar (insights §2.4), so it gets the header to place itself. The segment lives in the
 * navigator, so it survives tab switches and deep links can set it.
 */
@Composable
fun ActivityTabScreen(route: Route.Activity) {
    val navigator = LocalMainNavigator.current
    val snapshot by LocalLedger.current.collectSnapshot()
    val segment = navigator.activitySegment
    val header: @Composable () -> Unit = {
        ActivityHeader(
            segment = segment,
            onSegment = { navigator.activitySegment = it },
            showRestore = snapshot.recentlyDeleted.isNotEmpty(),
            onRestore = { navigator.open(Route.RecentlyDeleted) },
        )
    }
    Box(Modifier.fillMaxSize().background(PbColors.Bg.Primary).testTag("screen.activity")) {
        when (segment) {
            ActivitySegment.Timeline ->
                Column(
                    Modifier.statusBarsPadding().padding(horizontal = PbLayout.ScreenMargin)
                ) {
                    header()
                    Spacer(Modifier.height(PbSpace.S24))
                    ActivityTimelineView(
                        Modifier.fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(LocalTabBarPadding.current)
                    )
                }
            ActivitySegment.Insights -> InsightsView(header = header)
        }
    }
}
