package app.paybak.paybak.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles

/** `Surface` of a row: on white, or inside a #F5F5F5 card (white circles and chips). */
enum class PbRowSurface {
    Plain,
    OnCard,
}

/**
 * `Row / Activity` (`PBActivityRow`): an activity, timeline or notification row, at least 64 dp
 * tall. The title wraps to 2 lines and the subtitle to 3 (activity §3.5). The trailing column shows
 * the [amount] (black when [amountPrimary], grey otherwise), the [date], a status [badge] or a
 * small [action]; [unread] adds the 8 dp dot and [showDivider] a leading-inset hairline.
 *
 * @param leading The 40 dp circle: a category icon ([PbAvatarContent.Symbol]) or a person.
 * @param badgeStyle The badge's pill; by default grey (Muted, or On Card inside a card). A loan
 *   installment that's overdue now uses the red Overdue pill.
 */
@Composable
fun PbActivityRow(
    leading: PbAvatarContent,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    detail: String? = null,
    amount: String? = null,
    amountPrimary: Boolean = true,
    date: String? = null,
    badge: String? = null,
    badgeStyle: PbBadgeStyle? = null,
    action: String? = null,
    onAction: () -> Unit = {},
    unread: Boolean = false,
    showDivider: Boolean = false,
    surface: PbRowSurface = PbRowSurface.Plain,
    onClick: (() -> Unit)? = null,
    testTag: String? = null,
) {
    val onCard = surface == PbRowSurface.OnCard
    val press = rememberPressState(null)
    Box(
        modifier
            .fillMaxWidth()
            .partTag(testTag)
            .background(rowPressColor(press.isPressed && onClick != null, onCard))
            .then(
                if (onClick != null) Modifier.pressable(press, enabled = true, onClick = onClick)
                else Modifier
            )
            .semantics(mergeDescendants = true) {}
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp).padding(vertical = PbSpace.S8),
            horizontalArrangement = Arrangement.spacedBy(PbSpace.S12),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PbAvatar(leading, onCard = onCard)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(PbSpace.S2)) {
                Text(
                    title,
                    style = PbTextStyles.Headline,
                    color = PbColors.Text.Primary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (subtitle != null) {
                    Text(
                        subtitle,
                        style = PbTextStyles.Subheadline,
                        color = PbColors.Text.Secondary,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (detail != null) {
                    Text(
                        detail,
                        style = PbTextStyles.Footnote,
                        color = PbColors.Text.Tertiary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Trailing(amount, amountPrimary, date, badge, badgeStyle, action, onAction, onCard)
            if (unread) Box(Modifier.size(8.dp).background(PbColors.Bg.Inverse, CircleShape))
        }
        if (showDivider)
            PbDivider(Modifier.align(Alignment.BottomStart), inset = PbDividerInset.Leading)
    }
}

@Composable
private fun Trailing(
    amount: String?,
    amountPrimary: Boolean,
    date: String?,
    badge: String?,
    badgeStyle: PbBadgeStyle?,
    action: String?,
    onAction: () -> Unit,
    onCard: Boolean,
) {
    if (amount == null && date == null && badge == null && action == null) return
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(PbSpace.S2),
    ) {
        if (amount != null) {
            Text(
                amount,
                style = PbTextStyles.AmountMedium,
                color = if (amountPrimary) PbColors.Text.Primary else PbColors.Text.Secondary,
                maxLines = 1,
            )
        }
        if (date != null)
            Text(date, style = PbTextStyles.Footnote, color = PbColors.Text.Tertiary, maxLines = 1)
        if (badge != null)
            PbBadge(
                badge,
                style = badgeStyle ?: if (onCard) PbBadgeStyle.OnCard else PbBadgeStyle.Muted,
            )
        if (action != null) {
            PbButton(
                action,
                onClick = onAction,
                style = if (onCard) PbButtonStyle.OnCard else PbButtonStyle.Secondary,
                size = PbButtonSize.Small,
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 362)
@Composable
private fun PbActivityRowPreview() {
    Column {
        PbActivityRow(
            PbAvatarContent.Symbol(PbIcon.Food),
            "Dinner at Olive Garden",
            subtitle = "You paid · 4 people",
            amount = "₹2,800",
            date = "Today",
        )
        PbActivityRow(
            PbAvatarContent.Symbol(PbIcon.Bolt),
            "Electricity bill",
            subtitle = "Flat 302 · You owe",
            amount = "−₹450",
            amountPrimary = false,
            date = "26 Sep",
        )
        PbActivityRow(
            PbAvatarContent.Art(PbPeepHead.Priya),
            "Priya paid you",
            subtitle = "UPI",
            amount = "₹1,050",
            date = "Yesterday",
            unread = true,
        )
        PbActivityRow(
            PbAvatarContent.Symbol(PbIcon.Flame),
            "Cooking gas draft created",
            subtitle = "Flat 302 · Needs an amount",
            badge = "Draft",
        )
        PbActivityRow(
            PbAvatarContent.Symbol(PbIcon.Food),
            "Snacks",
            subtitle = "₹300 · Goa Trip",
            detail = "Deleted by Priya on 24 Sep · 24 days left",
            action = "Restore",
            showDivider = true,
        )
    }
}
