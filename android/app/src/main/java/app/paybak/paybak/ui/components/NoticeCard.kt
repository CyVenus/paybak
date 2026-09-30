package app.paybak.paybak.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbShapes
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles

/** `Layout` of a [PbNoticeCard]. */
enum class PbNoticeLayout {
    /** Icon on the left, text beside it: in-flow notices. */
    Leading,

    /** Everything centred, bigger icon and Title/3: a Pro lock (11-03). */
    Centered,
}

/** A button of a [PbNoticeCard]. */
data class PbNoticeAction(val label: String, val onClick: () -> Unit)

/**
 * `Card / Notice` (`PBNoticeCard`): an in-flow notice on #F5F5F5 (pending confirmation, invite a
 * guest, simplified debts, a dispute, a read-only project, a Pro lock), all grey and black, never
 * red. One action is a full-width Large pill; two are a black and a white Small pill sharing the
 * width. Parts are tagged "[testTag].primary" and "[testTag].secondary".
 *
 * @param icon Activity (pending), Shuffle, Mail, Flag, Lock or Check Circle.
 * @param badge An Inverse pill on the title line ("Pro").
 */
@Composable
fun PbNoticeCard(
    body: String,
    icon: PbIcon,
    modifier: Modifier = Modifier,
    title: String? = null,
    badge: String? = null,
    layout: PbNoticeLayout = PbNoticeLayout.Leading,
    primaryAction: PbNoticeAction? = null,
    secondaryAction: PbNoticeAction? = null,
    testTag: String? = null,
) {
    val centered = layout == PbNoticeLayout.Centered
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .partTag(testTag)
                .background(PbColors.Bg.Card, PbShapes.Card)
                .padding(if (centered) PbSpace.S24 else PbSpace.S16),
        verticalArrangement = Arrangement.spacedBy(if (centered) PbSpace.S12 else PbSpace.S16),
        horizontalAlignment = if (centered) Alignment.CenterHorizontally else Alignment.Start,
    ) {
        if (centered) {
            CenteredContent(body, icon, title, badge)
        } else {
            LeadingContent(body, icon, title, badge)
        }
        if (primaryAction != null) {
            NoticeActions(
                primaryAction,
                secondaryAction,
                testTag,
                Modifier.padding(top = if (centered) PbSpace.S12 else PbSpace.S0),
            )
        }
    }
}

@Composable
private fun LeadingContent(body: String, icon: PbIcon, title: String?, badge: String?) {
    Row(horizontalArrangement = Arrangement.spacedBy(PbSpace.S12)) {
        PbAvatar(PbAvatarContent.Symbol(icon), onCard = true)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(PbSpace.S2)) {
            if (title != null) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(PbSpace.S8),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        title,
                        Modifier.weight(1f),
                        style = PbTextStyles.Headline,
                        color = PbColors.Text.Primary,
                    )
                    if (badge != null) PbBadge(badge, style = PbBadgeStyle.Inverse)
                }
            }
            Text(body, style = PbTextStyles.Subheadline, color = PbColors.Text.Secondary)
        }
    }
}

@Composable
private fun CenteredContent(body: String, icon: PbIcon, title: String?, badge: String?) {
    PbAvatar(PbAvatarContent.Symbol(icon), size = PbAvatarSize.Lg, onCard = true)
    if (badge != null) PbBadge(badge, style = PbBadgeStyle.Inverse)
    Column(
        verticalArrangement = Arrangement.spacedBy(PbSpace.S8),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (title != null) {
            Text(
                title,
                style = PbTextStyles.Title3,
                color = PbColors.Text.Primary,
                textAlign = TextAlign.Center,
            )
        }
        Text(
            body,
            style = PbTextStyles.Subheadline,
            color = PbColors.Text.Secondary,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun NoticeActions(
    primary: PbNoticeAction,
    secondary: PbNoticeAction?,
    testTag: String?,
    modifier: Modifier = Modifier,
) {
    if (secondary == null) {
        PbButton(
            primary.label,
            onClick = primary.onClick,
            modifier = modifier.fillMaxWidth().partTag(testTag, "primary"),
        )
        return
    }
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(PbSpace.S8)) {
        PbButton(
            primary.label,
            onClick = primary.onClick,
            modifier = Modifier.weight(1f).partTag(testTag, "primary"),
            size = PbButtonSize.Small,
        )
        PbButton(
            secondary.label,
            onClick = secondary.onClick,
            modifier = Modifier.weight(1f).partTag(testTag, "secondary"),
            style = PbButtonStyle.OnCard,
            size = PbButtonSize.Small,
        )
    }
}

@Preview(showBackground = true, widthDp = 362)
@Composable
private fun PbNoticeCardPreview() {
    Column(verticalArrangement = Arrangement.spacedBy(PbSpace.S16)) {
        PbNoticeCard(
            "Waiting for Meera to confirm",
            PbIcon.Activity,
            title = "Pending confirmation",
        )
        PbNoticeCard(
            "See where your money goes each month.",
            PbIcon.Lock,
            title = "Insights are a Pro feature",
            badge = "Pro",
            layout = PbNoticeLayout.Centered,
            primaryAction = PbNoticeAction("See Pro", onClick = {}),
            secondaryAction = PbNoticeAction("Not now", onClick = {}),
        )
    }
}
