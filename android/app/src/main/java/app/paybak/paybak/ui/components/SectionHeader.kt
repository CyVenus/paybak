package app.paybak.paybak.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles

private val SectionHeaderHeight = 32.dp

/**
 * `Row / Section Header` (`PBSectionHeader`): a Title/3 section title with an optional secondary
 * text action ("See all") on the right. The row is 32 dp tall; the action's 44 dp tap target
 * overhangs it above and below. [actionTestTag] tags the action for UI tests.
 */
@Composable
fun PbSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    action: String? = null,
    onAction: () -> Unit = {},
    actionTestTag: String? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth().height(SectionHeaderHeight),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            modifier = Modifier.semantics { heading() },
            style = PbTextStyles.Title3,
            color = PbColors.Text.Primary,
            maxLines = 1,
        )
        if (action != null) {
            PbTextButton(
                label = action,
                onClick = onAction,
                modifier = Modifier.wrapContentHeight(unbounded = true).partTag(actionTestTag),
                style = PbTextButtonStyle.Secondary,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PbSectionHeaderPreview() {
    Column(Modifier.width(362.dp), verticalArrangement = Arrangement.spacedBy(PbSpace.S16)) {
        PbSectionHeader("Suggested")
        PbSectionHeader("Recent activity", action = "See all")
    }
}
