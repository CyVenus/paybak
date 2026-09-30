package app.paybak.paybak.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbSize
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles

/**
 * `Header / Title Row` (`PBTitleHeader`): the content header of a group, friend or project page, 16
 * dp under the push header. A 56 dp [leading] circle (the group's icon, or the person's avatar or
 * initials), the Title/2 title with an optional [tag] ("Guest", "Archived"), the subtitle, and the
 * [members] stack 8 dp below. A title-only header centres on the circle.
 *
 * @param members 2–4 heads; fewer hides the stack, more shows the first four.
 */
@Composable
fun PbTitleHeader(
    title: String,
    leading: PbAvatarContent,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    tag: String? = null,
    members: List<PbPeepHead> = emptyList(),
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(PbSpace.S16),
        verticalAlignment = Alignment.Top,
    ) {
        PbAvatar(leading, size = PbAvatarSize.Lg)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(PbSpace.S8)) {
            Column(
                modifier = Modifier.heightIn(min = PbSize.AvatarLg),
                verticalArrangement = Arrangement.spacedBy(PbSpace.S2, Alignment.CenterVertically),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(PbSpace.S8),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = title,
                        modifier = Modifier.widthIn(max = 260.dp).semantics { heading() },
                        style = PbTextStyles.Title2,
                        color = PbColors.Text.Primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (tag != null) PbBadge(tag)
                }
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = PbTextStyles.Subheadline,
                        color = PbColors.Text.Secondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            if (members.size >= 2) PbAvatarStack(members.take(4))
        }
    }
}

@Preview(showBackground = true, widthDp = 362)
@Composable
private fun PbTitleHeaderPreview() {
    Column(verticalArrangement = Arrangement.spacedBy(PbSpace.S24)) {
        PbTitleHeader(
            "Goa Trip",
            PbAvatarContent.Symbol(PbIcon.Plane),
            subtitle = "21–25 Sep · 5 members · ₹39,500 spent",
            members = listOf(PbPeepHead.Arjun, PbPeepHead.Priya, PbPeepHead.Rohan, PbPeepHead.Esha),
        )
        PbTitleHeader("Aarav", PbAvatarContent.Initials("AR"), tag = "Guest")
    }
}
