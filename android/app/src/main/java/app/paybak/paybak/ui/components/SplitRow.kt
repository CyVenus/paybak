package app.paybak.paybak.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles

/** How a [PbSplitRow] shows the person's part (`Mode`). */
sealed interface PbSplitMode {
    /** The computed share, read-only ("₹700"). */
    data class Equally(val amount: String) : PbSplitMode

    /** The share typed in an inline field. */
    data class Exact(val editor: PbAmountEditor) : PbSplitMode

    /** The percent typed in an inline field, and the [amount] it comes to under the name. */
    data class Percent(val editor: PbAmountEditor, val amount: String) : PbSplitMode

    /** Shares in a small field with the stepper, and the [amount] they come to under the name. */
    data class Shares(
        val editor: PbAmountEditor,
        val amount: String,
        val onDecrement: () -> Unit,
        val onIncrement: () -> Unit,
    ) : PbSplitMode
}

/**
 * `Row / Split Person` (`PBSplitRow`): one person in the split editor, 64 dp, inside a [PbCard].
 * The row toggles whether they're in the split (the select circle; pressed shows
 * `bg/card-pressed`). Excluded people read grey, and their fields and stepper are disabled; pass
 * their zero values ("₹0", "0"), as the split gives them nothing. The divider starts at the name.
 */
@Composable
fun PbSplitRow(
    name: String,
    avatar: PbAvatarContent,
    included: Boolean,
    onIncludedChange: (Boolean) -> Unit,
    mode: PbSplitMode,
    modifier: Modifier = Modifier,
    showDivider: Boolean = true,
) {
    val press = rememberPressState(interactionSource = null)
    val pressFill =
        animatePressColor(
            PbColors.Bg.CardPressed.copy(alpha = if (press.isPressed) 1f else 0f),
            label = "PbSplitRow press",
        )
    val primary = if (included) PbColors.Text.Primary else PbColors.Text.Tertiary
    val secondLine =
        when (mode) {
            is PbSplitMode.Percent -> mode.amount
            is PbSplitMode.Shares -> mode.amount
            else -> null
        }
    Box(
        modifier
            .fillMaxWidth()
            .background(pressFill)
            .toggleable(
                value = included,
                interactionSource = press.source,
                indication = null,
                role = Role.Checkbox,
                onValueChange = onIncludedChange,
            )
    ) {
        Row(
            modifier =
                Modifier.fillMaxWidth()
                    .heightIn(min = 64.dp)
                    .padding(horizontal = PbSpace.S16, vertical = PbSpace.S8),
            horizontalArrangement = Arrangement.spacedBy(PbSpace.S12),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PbSelectCircle(included)
            PbAvatar(avatar, size = PbAvatarSize.Sm, onCard = true)
            Column(Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = PbTextStyles.Headline,
                    color = primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (secondLine != null) {
                    Text(
                        text = secondLine,
                        style = PbTextStyles.Footnote,
                        color = if (included) PbColors.Text.Secondary else PbColors.Text.Tertiary,
                        maxLines = 1,
                    )
                }
            }
            SplitValue(mode, included)
        }
        if (showDivider) PbDivider(Modifier.align(Alignment.BottomStart).padding(start = 96.dp))
    }
}

@Composable
private fun SplitValue(mode: PbSplitMode, included: Boolean) {
    val style = PbTextStyles.Headline
    when (mode) {
        is PbSplitMode.Equally ->
            Text(
                text = mode.amount,
                style = style,
                color = if (included) PbColors.Text.Primary else PbColors.Text.Tertiary,
                maxLines = 1,
            )
        is PbSplitMode.Exact -> InlineAmountField(mode.editor, style, enabled = included)
        is PbSplitMode.Percent -> InlineAmountField(mode.editor, style, enabled = included)
        is PbSplitMode.Shares ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(PbSpace.S8),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                InlineAmountField(
                    mode.editor,
                    style,
                    minWidth = 48.dp,
                    centered = true,
                    enabled = included,
                )
                PbStepper(
                    onDecrement = mode.onDecrement,
                    onIncrement = mode.onIncrement,
                    enabled = included,
                    canDecrement = (mode.editor.value.toIntOrNull() ?: 0) > 0,
                )
            }
    }
}

@Preview(showBackground = true, widthDp = 362)
@Composable
private fun PbSplitRowPreview() {
    val priya = PbAvatarContent.Art(PbPeepHead.Priya)
    PbCard {
        PbSplitRow("Priya", priya, true, {}, PbSplitMode.Equally("₹700"))
        PbSplitRow("Priya", priya, true, {}, PbSplitMode.Exact(PbAmountEditor("700", {}, "₹")))
        PbSplitRow(
            "Priya",
            priya,
            true,
            {},
            PbSplitMode.Percent(PbAmountEditor("25", {}, suffix = "%"), "₹700"),
        )
        PbSplitRow(
            "Priya",
            priya,
            false,
            {},
            PbSplitMode.Shares(PbAmountEditor("0", {}), "₹0", {}, {}),
            showDivider = false,
        )
    }
}
