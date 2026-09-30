package app.paybak.paybak.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles

/**
 * `Row / Comment` (`PBCommentRow`): one comment on an expense: a 32 dp avatar, the name with the
 * date beside it on one baseline, and the comment below, wrapping. Rows stack with no gap or
 * divider.
 */
@Composable
fun PbCommentRow(
    name: String,
    date: String,
    text: String,
    avatar: PbAvatarContent,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier.fillMaxWidth().padding(vertical = PbSpace.S8).semantics(
                mergeDescendants = true
            ) {},
        horizontalArrangement = Arrangement.spacedBy(PbSpace.S12),
        verticalAlignment = Alignment.Top,
    ) {
        PbAvatar(avatar, size = PbAvatarSize.Sm)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(PbSpace.S2)) {
            Row(horizontalArrangement = Arrangement.spacedBy(PbSpace.S8)) {
                Text(
                    text = name,
                    modifier = Modifier.alignByBaseline().weight(1f, fill = false),
                    style = PbTextStyles.Headline,
                    color = PbColors.Text.Primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = date,
                    modifier = Modifier.alignByBaseline(),
                    style = PbTextStyles.Footnote,
                    color = PbColors.Text.Tertiary,
                    maxLines = 1,
                )
            }
            Text(text, style = PbTextStyles.Body, color = PbColors.Text.Primary)
        }
    }
}

@Preview(showBackground = true, widthDp = 362)
@Composable
private fun PbCommentRowPreview() {
    PbCommentRow(
        "Priya",
        "27 Sep",
        "Was breakfast included?",
        PbAvatarContent.Art(PbPeepHead.Priya),
    )
}
