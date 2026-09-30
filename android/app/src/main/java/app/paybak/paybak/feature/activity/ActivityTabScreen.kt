package app.paybak.paybak.feature.activity

import androidx.compose.foundation.background
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
 * The `activity` tab root (M2's container, activity §3): the fixed [ActivityHeader] over the
 * selected segment's content: lane A's [ActivityTimelineView] or lane C's [InsightsView]. The
 * segment lives in the navigator, so it survives tab switches and deep links can set it.
 */
@Composable
fun ActivityTabScreen(route: Route.Activity) {
    val navigator = LocalMainNavigator.current
    val snapshot by LocalLedger.current.collectSnapshot()
    val segment = navigator.activitySegment
    Column(
        Modifier.fillMaxSize()
            .background(PbColors.Bg.Primary)
            .testTag("screen.activity")
            .statusBarsPadding()
            .padding(horizontal = PbLayout.ScreenMargin)
    ) {
        ActivityHeader(
            segment = segment,
            onSegment = { navigator.activitySegment = it },
            showRestore = snapshot.recentlyDeleted.isNotEmpty(),
            onRestore = { navigator.open(Route.RecentlyDeleted) },
        )
        Spacer(Modifier.height(PbSpace.S24))
        val content =
            Modifier.fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(LocalTabBarPadding.current)
        when (segment) {
            ActivitySegment.Timeline -> ActivityTimelineView(content)
            ActivitySegment.Insights -> InsightsView(content)
        }
    }
}
