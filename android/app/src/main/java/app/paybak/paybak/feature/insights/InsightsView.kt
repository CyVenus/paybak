package app.paybak.paybak.feature.insights

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import app.paybak.paybak.ui.components.PbBadge
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles

/**
 * The Activity tab's Insights segment (insightsSeptember, insightsScrolled, insightsLocked): the
 * content under the Activity header; its month is `navigator.insightsMonth`. PLACEHOLDER owned by
 * lane C (M9): replace this file and keep the signature.
 */
@Composable
fun InsightsView(modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxWidth().testTag("activity.insights"),
        verticalArrangement = Arrangement.spacedBy(PbSpace.S8),
    ) {
        PbBadge("Placeholder · lane C")
        Text(
            "Insights (screens-insights-ai §2)",
            style = PbTextStyles.Headline,
            color = PbColors.Text.Primary,
        )
    }
}
