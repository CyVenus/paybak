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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.icons.PbIconImage
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbSize
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles

/** The trailing element of a [PbSettingRow] (`Trailing`). */
sealed interface PbSettingTrailing {
    /** Pushes a screen. */
    data object Chevron : PbSettingTrailing

    /** The kit switch; tapping anywhere on the row flips it. */
    data class Toggle(val checked: Boolean, val onCheckedChange: (Boolean) -> Unit) :
        PbSettingTrailing

    /** The kit stepper; the row itself doesn't take taps. */
    data class Stepper(
        val onDecrement: () -> Unit,
        val onIncrement: () -> Unit,
        val canDecrement: Boolean = true,
        val canIncrement: Boolean = true,
    ) : PbSettingTrailing

    /**
     * Single-select lists: a black tick when [selected] (Check), else an empty slot (Unchecked).
     */
    data class Check(val selected: Boolean) : PbSettingTrailing

    data object None : PbSettingTrailing
}

/** `Tone` of a [PbSettingRow]: Destructive is red and never shows a chevron. */
enum class PbSettingTone {
    Default,
    Destructive,
}

/**
 * `Row / Setting` (`PBSettingRow`): a settings row for a [PbCard] group, 56 dp tall (taller with a
 * [subtitle]). The whole row is the tap target; pressed lays the 6 % overlay over it. The divider
 * starts at the title (x 52 with an [icon], 16 without); hide it on a group's last row.
 *
 * @param onClick Chevron, Check and None rows; Toggle rows flip their switch instead.
 * @param badge An Inverse `Badge / Pill` before the value ("Pro", "Try free").
 * @param valueColor The value's colour: `text/secondary`, or e.g. red for "Doesn’t add up".
 * @param valueLeading Drawn just before the value, e.g. an attached receipt's thumbnail.
 */
@Composable
fun PbSettingRow(
    title: String,
    modifier: Modifier = Modifier,
    trailing: PbSettingTrailing = PbSettingTrailing.Chevron,
    onClick: (() -> Unit)? = null,
    icon: PbIcon? = null,
    value: String? = null,
    subtitle: String? = null,
    badge: String? = null,
    tone: PbSettingTone = PbSettingTone.Default,
    showDivider: Boolean = true,
    valueColor: Color = PbColors.Text.Secondary,
    valueLeading: (@Composable () -> Unit)? = null,
) {
    val destructive = tone == PbSettingTone.Destructive
    val press = rememberPressState(interactionSource = null)
    val tap =
        when {
            trailing is PbSettingTrailing.Toggle ->
                Modifier.toggleable(
                    value = trailing.checked,
                    interactionSource = press.source,
                    indication = null,
                    role = Role.Switch,
                    onValueChange = trailing.onCheckedChange,
                )
            onClick == null || trailing is PbSettingTrailing.Stepper -> Modifier
            trailing is PbSettingTrailing.Check ->
                Modifier.selectable(
                    selected = trailing.selected,
                    interactionSource = press.source,
                    indication = null,
                    role = Role.RadioButton,
                    onClick = onClick,
                )
            else -> Modifier.pressable(press, enabled = true, onClick = onClick)
        }
    Box(
        modifier.fillMaxWidth().background(rowPressColor(press.isPressed, onCard = true)).then(tap)
    ) {
        Row(
            modifier =
                Modifier.fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .padding(horizontal = PbSpace.S16, vertical = PbSpace.S12),
            horizontalArrangement = Arrangement.spacedBy(PbSpace.S12),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                PbIconImage(
                    icon,
                    contentDescription = null,
                    tint = if (destructive) PbColors.Icon.Destructive else PbColors.Icon.Primary,
                )
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(PbSpace.S2)) {
                Text(
                    text = title,
                    style = PbTextStyles.Headline,
                    color = if (destructive) PbColors.Text.Destructive else PbColors.Text.Primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (subtitle != null) {
                    Text(subtitle, style = PbTextStyles.Footnote, color = PbColors.Text.Secondary)
                }
            }
            if (badge != null) PbBadge(badge, style = PbBadgeStyle.Inverse)
            valueLeading?.invoke()
            if (value != null) {
                Text(value, style = PbTextStyles.Body, color = valueColor, maxLines = 1)
            }
            SettingTrailing(trailing, destructive)
        }
        if (showDivider) {
            PbDivider(
                Modifier.align(Alignment.BottomStart)
                    .padding(start = if (icon != null) 52.dp else PbSpace.S16)
            )
        }
    }
}

@Composable
private fun SettingTrailing(trailing: PbSettingTrailing, destructive: Boolean) {
    when (trailing) {
        PbSettingTrailing.Chevron ->
            if (!destructive) {
                PbIconImage(
                    PbIcon.ChevronRight,
                    contentDescription = null,
                    size = PbSize.IconMd,
                    tint = PbColors.Icon.Tertiary,
                )
            }
        is PbSettingTrailing.Toggle -> PbSwitch(trailing.checked, onCheckedChange = null)
        is PbSettingTrailing.Stepper ->
            PbStepper(
                onDecrement = trailing.onDecrement,
                onIncrement = trailing.onIncrement,
                canDecrement = trailing.canDecrement,
                canIncrement = trailing.canIncrement,
            )
        is PbSettingTrailing.Check ->
            Box(Modifier.size(PbSize.IconLg)) {
                if (trailing.selected) PbIconImage(PbIcon.Check, contentDescription = null)
            }
        PbSettingTrailing.None -> Unit
    }
}

@Preview(showBackground = true, widthDp = 362)
@Composable
private fun PbSettingRowPreview() {
    PbCard {
        PbSettingRow("Payment details", icon = PbIcon.Wallet, value = "UPI", onClick = {})
        PbSettingRow(
            "Reminders",
            icon = PbIcon.Bell,
            trailing = PbSettingTrailing.Toggle(checked = true, onCheckedChange = {}),
        )
        PbSettingRow(
            "Sign out",
            icon = PbIcon.Logout,
            trailing = PbSettingTrailing.None,
            tone = PbSettingTone.Destructive,
            onClick = {},
            showDivider = false,
        )
    }
}
