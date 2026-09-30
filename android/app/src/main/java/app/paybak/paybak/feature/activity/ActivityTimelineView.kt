package app.paybak.paybak.feature.activity

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
 * The Activity tab's Timeline segment (activityTimeline, activityEmpty): the content under the
 * Activity header. PLACEHOLDER owned by lane A (M6): replace this file and keep the signature.
 */
@Composable
fun ActivityTimelineView(modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxWidth().testTag("activity.timeline"),
        verticalArrangement = Arrangement.spacedBy(PbSpace.S8),
    ) {
        PbBadge("Placeholder · lane A")
        Text(
            "Timeline (screens-activity §3)",
            style = PbTextStyles.Headline,
            color = PbColors.Text.Primary,
        )
    }
}
