package app.paybak.paybak.feature.groups

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.paybak.paybak.R
import app.paybak.paybak.domain.groups.FriendListRow
import app.paybak.paybak.domain.groups.FriendsSummary
import app.paybak.paybak.navigation.LocalMainNavigator
import app.paybak.paybak.navigation.Route
import app.paybak.paybak.rive.PaybakRiveAsset
import app.paybak.paybak.ui.components.PbEmptyAction
import app.paybak.paybak.ui.components.PbEmptyState
import app.paybak.paybak.ui.components.PbPersonRow
import app.paybak.paybak.ui.components.PbPersonTrailing
import app.paybak.paybak.ui.components.avatarContent
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.theme.PaybakTheme
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles

/**
 * The Friends segment (§3.3): the owed / owe summary (the Home totals) over one row per friend,
 * or, with no friends yet, the "No friends yet." card (§3.3 proposal).
 */
@Composable
internal fun FriendsSegmentContent(friends: List<FriendListRow>, summary: FriendsSummary) {
    val navigator = LocalMainNavigator.current
    if (friends.isEmpty()) {
        Spacer(Modifier.height(PbSpace.S24))
        PbEmptyState(
            title = stringResource(R.string.groups_friends_empty_title),
            body = stringResource(R.string.groups_friends_empty_body),
            illustration = PaybakRiveAsset.GetStarted,
            primaryAction =
                PbEmptyAction(
                    stringResource(R.string.groups_add_friend),
                    PbIcon.UserAdd,
                    { navigator.open(Route.AddFriend) },
                    testTag = "friends.empty.addFriend",
                ),
            testTag = "friends.empty",
        )
        return
    }
    Spacer(Modifier.height(PbSpace.S16))
    FriendsSummaryLine(summary)
    Spacer(Modifier.height(PbSpace.S16))
    Column {
        friends.forEachIndexed { index, row ->
            PbPersonRow(
                name = row.name,
                avatar = row.balance.person.avatarContent(),
                modifier = Modifier.testTag("friends.row.${row.personId}"),
                subtitle = row.subtitle,
                tag = if (row.guest) stringResource(R.string.groups_guest) else null,
                trailing =
                    row.status?.let(PbPersonTrailing::Status)
                        ?: PbPersonTrailing.Amount(
                            row.amount.orEmpty(),
                            row.standing.balance(),
                            label = row.label,
                            overdue = row.overdue,
                        ),
                onClick = { navigator.open(Route.Friend(row.personId)) },
                showDivider = index < friends.lastIndex,
                sidePadding = 0.dp,
            )
        }
    }
}

/** "You’re owed +₹2,900" on the left, "You owe −₹1,850" on the right; a zero side is grey. */
@Composable
private fun FriendsSummaryLine(summary: FriendsSummary) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SummaryPart(
            stringResource(R.string.groups_summary_owed),
            summary.owedText,
            if (summary.owed == 0L) PbColors.Text.Tertiary else PbColors.Text.Primary,
            bold = true,
            testTag = "friends.summary.owed",
        )
        SummaryPart(
            stringResource(R.string.groups_summary_owe),
            summary.oweText,
            if (summary.owe == 0L) PbColors.Text.Tertiary else PbColors.Text.Secondary,
            bold = false,
            testTag = "friends.summary.owe",
        )
    }
}

@Composable
private fun SummaryPart(
    label: String,
    amount: String,
    color: Color,
    bold: Boolean,
    testTag: String,
) {
    Row(
        Modifier.testTag(testTag).semantics(mergeDescendants = true) {},
        horizontalArrangement = Arrangement.spacedBy(PbSpace.S6),
    ) {
        Text(label, style = PbTextStyles.Subheadline, color = PbColors.Text.Secondary)
        Text(
            amount,
            style =
                if (bold) PbTextStyles.Subheadline.copy(fontWeight = FontWeight.Bold)
                else PbTextStyles.Subheadline,
            color = color,
        )
    }
}

@Preview(showBackground = true, widthDp = 362)
@Composable
private fun FriendsSummaryLinePreview() {
    PaybakTheme { FriendsSummaryLine(FriendsSummary(290_000, 185_000, "INR")) }
}
