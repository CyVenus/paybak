package app.paybak.paybak.ui.components.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import app.paybak.paybak.ui.components.PbAvatar
import app.paybak.paybak.ui.components.PbAvatarContent
import app.paybak.paybak.ui.components.PbBadge
import app.paybak.paybak.ui.components.PbBadgeStyle
import app.paybak.paybak.ui.components.PbButton
import app.paybak.paybak.ui.components.PbButtonSize
import app.paybak.paybak.ui.components.PbButtonStyle
import app.paybak.paybak.ui.components.PbPeepHead
import app.paybak.paybak.ui.components.animatePressColor
import app.paybak.paybak.ui.components.partTag
import app.paybak.paybak.ui.components.pressable
import app.paybak.paybak.ui.components.rememberPressState
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbShapes
import app.paybak.paybak.ui.theme.PbSize
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles

/**
 * `Row / Attention` (`PBAttentionRow`): a Due soon item on Home, 88 dp tall on a #F5F5F5 card.
 * [overdue] shows the red badge (money owed to you, Remind); otherwise the badge is white (Due Fri,
 * Settle). The [detail] after the title truncates so the row keeps its height (components-app
 * §8.2). A tap on the card opens the person or group; the button runs [onAction]. The button is
 * tagged "[testTag].[actionTag]". Settle up leaves out the [badge] of a debt with no due date and
 * the [action] of a payment waiting for its confirmation.
 */
@Composable
fun PbAttentionRow(
    avatar: PbAvatarContent,
    title: String,
    detail: String,
    badge: String?,
    overdue: Boolean,
    amount: String,
    action: String?,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    testTag: String? = null,
    actionTag: String? = action?.lowercase(),
) {
    val press = rememberPressState(null)
    val fill =
        animatePressColor(
            if (press.isPressed && onClick != null) PbColors.Bg.CardPressed else PbColors.Bg.Card,
            "Attention row",
        )
    Row(
        modifier
            .fillMaxWidth()
            .partTag(testTag)
            .clip(PbShapes.Card)
            .background(fill)
            .then(
                if (onClick != null) Modifier.pressable(press, enabled = true, onClick = onClick)
                else Modifier
            )
            .semantics(mergeDescendants = true) {}
            .padding(horizontal = PbSpace.S16, vertical = PbSpace.S12),
        horizontalArrangement = Arrangement.spacedBy(PbSpace.S12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PbAvatar(avatar, onCard = true)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(PbSpace.S6)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(PbSpace.S6),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    title,
                    style = PbTextStyles.Headline,
                    color = PbColors.Text.Primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    detail,
                    Modifier.weight(1f, fill = false),
                    style = PbTextStyles.Subheadline,
                    color = PbColors.Text.Secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (badge != null) {
                PbBadge(badge, style = if (overdue) PbBadgeStyle.Overdue else PbBadgeStyle.OnCard)
            }
        }
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(PbSpace.S6),
        ) {
            Text(
                amount,
                style = PbTextStyles.AmountMedium,
                color = PbColors.Text.Primary,
                maxLines = 1,
            )
            if (action != null) {
                PbButton(
                    action,
                    onClick = onAction,
                    modifier = Modifier.partTag(testTag, actionTag),
                    style = PbButtonStyle.OnCard,
                    size = PbButtonSize.Small,
                )
            } else {
                // Keeps the row 88 tall, with the amount where it sits above a button.
                Spacer(Modifier.height(PbSize.ButtonSm))
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 362)
@Composable
private fun PbAttentionRowPreview() {
    Column(verticalArrangement = Arrangement.spacedBy(PbSpace.S8)) {
        PbAttentionRow(
            PbAvatarContent.Art(PbPeepHead.Rohan),
            "Rohan",
            "Movie tickets",
            "Overdue 3 days",
            overdue = true,
            amount = "₹800",
            action = "Remind",
            onAction = {},
        )
        PbAttentionRow(
            PbAvatarContent.Symbol(PbIcon.Groups),
            "Goa Trip",
            "Your share",
            "Due Fri",
            overdue = false,
            amount = "₹1,400",
            action = "Settle",
            onAction = {},
        )
        PbAttentionRow(
            PbAvatarContent.Art(PbPeepHead.Priya),
            "Priya",
            "Dinner at Olive Garden with the whole gang",
            "Due Sun",
            overdue = false,
            amount = "₹700",
            action = "Remind",
            onAction = {},
        )
        PbAttentionRow(
            PbAvatarContent.Art(PbPeepHead.Kabir),
            "Kabir",
            "Goa Trip",
            "Pending",
            overdue = false,
            amount = "₹1,400",
            action = null,
            onAction = {},
        )
    }
}
