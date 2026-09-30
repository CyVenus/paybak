package app.paybak.paybak.feature.activity

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import app.paybak.paybak.R
import app.paybak.paybak.navigation.ActivitySegment
import app.paybak.paybak.ui.components.PbNavAction
import app.paybak.paybak.ui.components.PbNavHeader
import app.paybak.paybak.ui.components.PbSegmentedControl
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.theme.PbSpace

/**
 * The fixed header of the Activity tab (activity §3.2–3.3): the "Activity" large title with the
 * glass Restore button (only while Recently deleted has items) and the Timeline | Insights
 * segmented control across the content width.
 */
@Composable
fun ActivityHeader(
    segment: ActivitySegment,
    onSegment: (ActivitySegment) -> Unit,
    showRestore: Boolean,
    onRestore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(PbSpace.S16)) {
        PbNavHeader(
            stringResource(R.string.shell_activity_title),
            action =
                if (showRestore) {
                    PbNavAction(
                        PbIcon.Restore,
                        stringResource(R.string.shell_recently_deleted),
                        onRestore,
                        testTag = "activity.recentlyDeleted",
                    )
                } else {
                    null
                },
        )
        PbSegmentedControl(
            options =
                listOf(
                    stringResource(R.string.shell_timeline),
                    stringResource(R.string.shell_insights),
                ),
            selectedIndex = segment.ordinal,
            onSelect = { onSegment(ActivitySegment.entries[it]) },
            modifier = Modifier.fillMaxWidth(),
            segmentTags = listOf("activity.segment.timeline", "activity.segment.insights"),
        )
    }
}

@Preview(showBackground = true, widthDp = 362)
@Composable
private fun ActivityHeaderPreview() {
    ActivityHeader(ActivitySegment.Timeline, {}, showRestore = true, onRestore = {})
}
