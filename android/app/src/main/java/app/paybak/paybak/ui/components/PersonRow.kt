package app.paybak.paybak.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.icons.PbIconImage
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles

/** `Size` of `Row / Person`: Regular (64) for white surfaces, Compact (56) inside cards. */
enum class PbPersonRowSize(
    internal val avatar: PbAvatarSize,
    internal val minHeight: Dp,
    internal val verticalPadding: Dp,
    internal val nameMaxWidth: Dp,
) {
    Regular(PbAvatarSize.Md, 64.dp, PbSpace.S8, 200.dp),
    Compact(PbAvatarSize.Sm, 56.dp, PbSpace.S6, 180.dp),
}

/** The trailing element of a [PbPersonRow] (`Trailing`). */
sealed interface PbPersonTrailing {
    /**
     * Owed ("+₹700", black) or Owe ("−₹700", grey) in Amount/Medium, or a plain Headline Value
     * ("₹700", "25%") when [balance] is null. [label] reads under it ("Due Sun 4 Oct") and
     * [overdue] adds the red badge under that ("Overdue 3 days").
     */
    data class Amount(
        val amount: String,
        val balance: PbBalance? = null,
        val label: String? = null,
        val overdue: String? = null,
    ) : PbPersonTrailing

    /** Muted: a grey status ("Settled", "Added", "No balance"). */
    data class Status(val text: String) : PbPersonTrailing

    /** A black tick: the single choice (Paid by). */
    data object Check : PbPersonTrailing

    /** The select circle, for picking several people (Select On / Off). */
    data class Select(val selected: Boolean) : PbPersonTrailing

    /** A ✕ that removes the person, with its own 44 dp target. */
    data class Remove(val contentDescription: String, val onRemove: () -> Unit) : PbPersonTrailing

    /** A small pill ("Invite", "Remind") with its own tap. */
    data class Action(val label: String, val onClick: () -> Unit) : PbPersonTrailing
}

/**
 * `Row / Person` (`PBPersonRow`): the one people row (split pickers, balances, breakdowns,
 * contacts, chat answers): an avatar, the name with an optional [tag] ("Guest"), a subtitle and a
 * trailing element. The divider starts at the name; hide it on the last row.
 *
 * @param onCard The row sits in a #F5F5F5 card: white avatar circle, tag and pill, and the 6 %
 *   pressed overlay. Compact rows are always drawn for cards.
 * @param onClick The whole row: open the person, or pick them (Check / Select).
 */
@Composable
fun PbPersonRow(
    name: String,
    avatar: PbAvatarContent,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    tag: String? = null,
    trailing: PbPersonTrailing? = null,
    size: PbPersonRowSize = PbPersonRowSize.Regular,
    onCard: Boolean = size == PbPersonRowSize.Compact,
    onClick: (() -> Unit)? = null,
    showDivider: Boolean = true,
) {
    val press = rememberPressState(interactionSource = null)
    val tap =
        when {
            onClick == null -> Modifier
            trailing is PbPersonTrailing.Select ->
                Modifier.toggleable(
                    value = trailing.selected,
                    interactionSource = press.source,
                    indication = null,
                    role = Role.Checkbox,
                    onValueChange = { onClick() },
                )
            else -> Modifier.pressable(press, enabled = true, onClick = onClick)
        }
    val selection =
        if (trailing is PbPersonTrailing.Check) Modifier.semantics { selected = true } else Modifier
    Box(
        modifier
            .fillMaxWidth()
            .background(rowPressColor(press.isPressed, onCard))
            .then(tap)
            .then(selection)
    ) {
        Row(
            modifier =
                Modifier.fillMaxWidth()
                    .heightIn(min = size.minHeight)
                    .padding(horizontal = PbSpace.S16, vertical = size.verticalPadding),
            horizontalArrangement = Arrangement.spacedBy(PbSpace.S12),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PbAvatar(avatar, size = size.avatar, onCard = onCard)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(PbSpace.S2)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(PbSpace.S8),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = name,
                        modifier = Modifier.widthIn(max = size.nameMaxWidth),
                        style = PbTextStyles.Headline,
                        color = PbColors.Text.Primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (tag != null) {
                        PbBadge(
                            tag,
                            style = if (onCard) PbBadgeStyle.OnCard else PbBadgeStyle.Muted,
                        )
                    }
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
            if (trailing != null) PersonTrailing(trailing, onCard)
        }
        if (showDivider) {
            PbDivider(
                Modifier.align(Alignment.BottomStart)
                    .padding(start = PbSpace.S16 + size.avatar.diameter + PbSpace.S12)
            )
        }
    }
}

@Composable
private fun PersonTrailing(trailing: PbPersonTrailing, onCard: Boolean) {
    when (trailing) {
        is PbPersonTrailing.Amount ->
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(PbSpace.S2),
            ) {
                if (trailing.balance != null) {
                    SignedAmount(trailing.amount, trailing.balance)
                } else {
                    Text(
                        trailing.amount,
                        style = PbTextStyles.Headline,
                        color = PbColors.Text.Primary,
                        maxLines = 1,
                    )
                }
                if (trailing.label != null) {
                    Text(
                        trailing.label,
                        style = PbTextStyles.Footnote,
                        color = PbColors.Text.Tertiary,
                        maxLines = 1,
                    )
                }
                if (trailing.overdue != null) {
                    PbBadge(trailing.overdue, style = PbBadgeStyle.Overdue)
                }
            }
        is PbPersonTrailing.Status ->
            Text(
                trailing.text,
                style = PbTextStyles.Subheadline,
                color = PbColors.Text.Secondary,
                maxLines = 1,
            )
        PbPersonTrailing.Check -> PbIconImage(PbIcon.Check, contentDescription = null)
        is PbPersonTrailing.Select -> PbSelectCircle(trailing.selected)
        is PbPersonTrailing.Remove ->
            SmallIconButton(
                PbIcon.Close,
                contentDescription = trailing.contentDescription,
                onClick = trailing.onRemove,
            )
        is PbPersonTrailing.Action ->
            PbButton(
                label = trailing.label,
                onClick = trailing.onClick,
                style = if (onCard) PbButtonStyle.OnCard else PbButtonStyle.Secondary,
                size = PbButtonSize.Small,
            )
    }
}

@Preview(showBackground = true, widthDp = 362)
@Composable
private fun PbPersonRowPreview() {
    val priya = PbAvatarContent.Art(PbPeepHead.Priya)
    Column {
        PbPersonRow(
            "Priya",
            priya,
            subtitle = "Dinner at Olive Garden",
            trailing = PbPersonTrailing.Amount("₹700", PbBalance.Owed, "Due Sun 4 Oct"),
        )
        PbPersonRow(
            "Aarav",
            PbAvatarContent.Initials("AR"),
            subtitle = "Invited",
            tag = "Guest",
            trailing = PbPersonTrailing.Action("Invite", onClick = {}),
        )
        PbCard {
            PbPersonRow(
                "Priya",
                priya,
                subtitle = "Dinner at Olive Garden",
                trailing = PbPersonTrailing.Select(selected = true),
                size = PbPersonRowSize.Compact,
                onClick = {},
                showDivider = false,
            )
        }
    }
}
