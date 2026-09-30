package app.paybak.paybak.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import app.paybak.paybak.R
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbSize
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles

/** Where the user stands in a [PbGroupRow] (`Balance`). */
sealed interface PbGroupBalance {
    /** "−₹1,400" over "You owe". */
    data class Owe(val amount: String) : PbGroupBalance

    /** "+₹1,400" over "You’re owed". */
    data class Owed(val amount: String) : PbGroupBalance

    /** A grey status: "Settled", or "You’re settled" for a project. */
    data class Settled(val status: String) : PbGroupBalance
}

/**
 * A project's budget line under a [PbGroupRow]: spent ÷ budget, and both captions. Over budget
 * ([over]), [progress] is the budget point (budget ÷ spent) and the rest of the bar turns red.
 */
data class PbGroupBudget(
    val progress: Float,
    val spent: String,
    val left: String,
    val over: Boolean = false,
)

/**
 * `Row / Group` (`PBGroupRow`): a group or project in the Groups list and "Groups together", on
 * white with no side padding (it lives in the screen margins). A project adds its [budget] bar
 * under the text. [archived] greys the row and reads "Read-only" whatever the balance. The divider
 * starts at the name; hide it on the last row.
 *
 * @param icon The group type: Plane (trip), Home, People (friends), Tag (other); Drone or Package
 *   for projects.
 */
@Composable
fun PbGroupRow(
    name: String,
    subtitle: String,
    icon: PbIcon,
    balance: PbGroupBalance,
    modifier: Modifier = Modifier,
    budget: PbGroupBudget? = null,
    archived: Boolean = false,
    showDivider: Boolean = true,
    onClick: (() -> Unit)? = null,
) {
    val press = rememberPressState(interactionSource = null)
    val tap = if (onClick == null) Modifier else Modifier.pressable(press, true, onClick = onClick)
    Box(
        modifier
            .fillMaxWidth()
            .background(rowPressColor(press.isPressed, onCard = false))
            .then(tap)
            .semantics(mergeDescendants = true) {}
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = PbSpace.S16),
            verticalArrangement = Arrangement.spacedBy(PbSpace.S12),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(PbSpace.S12),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PbAvatar(
                    PbAvatarContent.Symbol(
                        icon,
                        tint = if (archived) PbColors.Icon.Tertiary else PbColors.Icon.Primary,
                    )
                )
                Column(Modifier.weight(1f)) {
                    Text(
                        text = name,
                        style = PbTextStyles.Headline,
                        color = if (archived) PbColors.Text.Tertiary else PbColors.Text.Primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = subtitle,
                        style = PbTextStyles.Footnote,
                        color = PbColors.Text.Secondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                GroupTrailing(if (archived) null else balance)
            }
            if (budget != null) BudgetLine(budget)
        }
        if (showDivider) {
            PbDivider(Modifier.align(Alignment.BottomStart), inset = PbDividerInset.Leading)
        }
    }
}

/** The balance on the right; null is an archived group's "Read-only". */
@Composable
private fun GroupTrailing(balance: PbGroupBalance?) {
    when (balance) {
        is PbGroupBalance.Owe ->
            BalanceColumn(balance.amount, PbBalance.Owe, stringResource(R.string.pb_you_owe))
        is PbGroupBalance.Owed ->
            BalanceColumn(balance.amount, PbBalance.Owed, stringResource(R.string.pb_you_are_owed))
        is PbGroupBalance.Settled -> Status(balance.status, PbColors.Text.Secondary)
        null -> Status(stringResource(R.string.pb_read_only), PbColors.Text.Tertiary)
    }
}

@Composable
private fun BalanceColumn(amount: String, balance: PbBalance, label: String) {
    Column(horizontalAlignment = Alignment.End) {
        SignedAmount(amount, balance)
        Text(label, style = PbTextStyles.Footnote, color = PbColors.Text.Secondary, maxLines = 1)
    }
}

@Composable
private fun Status(text: String, color: Color) {
    Text(text, style = PbTextStyles.Subheadline, color = color, maxLines = 1)
}

/** The project budget, indented under the text: a Small bar and "spent of budget" · "left". */
@Composable
private fun BudgetLine(budget: PbGroupBudget) {
    Column(
        modifier = Modifier.padding(start = PbSize.AvatarMd + PbSpace.S12),
        verticalArrangement = Arrangement.spacedBy(PbSpace.S8),
    ) {
        PbProgressBar(budget.progress, over = budget.over)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(budget.spent, style = PbTextStyles.Footnote, color = PbColors.Text.Secondary)
            Text(budget.left, style = PbTextStyles.Footnote, color = PbColors.Text.Secondary)
        }
    }
}

@Preview(showBackground = true, widthDp = 362)
@Composable
private fun PbGroupRowPreview() {
    Column {
        PbGroupRow(
            "Goa Trip",
            "5 members · Due Fri 2 Oct",
            PbIcon.Plane,
            PbGroupBalance.Owe("₹1,400"),
        )
        PbGroupRow(
            "Drone build",
            "4 members · Due Fri 2 Oct",
            PbIcon.Drone,
            PbGroupBalance.Owed("₹1,400"),
            budget = PbGroupBudget(0.87f, "₹52,000 of ₹60,000", "₹8,000 left"),
        )
        PbGroupRow(
            "Old flat",
            "3 members",
            PbIcon.Package,
            PbGroupBalance.Settled("Settled"),
            archived = true,
            showDivider = false,
        )
    }
}
