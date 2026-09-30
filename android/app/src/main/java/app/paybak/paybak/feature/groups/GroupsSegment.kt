package app.paybak.paybak.feature.groups

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import app.paybak.paybak.R
import app.paybak.paybak.domain.groups.GroupsList
import app.paybak.paybak.navigation.LocalMainNavigator
import app.paybak.paybak.navigation.Route
import app.paybak.paybak.rive.PaybakRiveAsset
import app.paybak.paybak.ui.components.PbEmptyAction
import app.paybak.paybak.ui.components.PbEmptyState
import app.paybak.paybak.ui.components.PbSectionHeader
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.theme.PbSpace

/**
 * The Groups segment (§3.2): groups and projects in one list, open balances first, then the
 * read-only Archived projects; or, with none, the "No groups yet." card (§3.4).
 */
@Composable
internal fun GroupsSegmentContent(groups: GroupsList) {
    val navigator = LocalMainNavigator.current
    Spacer(Modifier.height(PbSpace.S24))
    if (groups.rows.isEmpty() && groups.archived.isEmpty()) {
        PbEmptyState(
            title = stringResource(R.string.groups_empty_title),
            body = stringResource(R.string.groups_empty_body),
            illustration = PaybakRiveAsset.GetStarted,
            primaryAction =
                PbEmptyAction(
                    stringResource(R.string.groups_new_group),
                    PbIcon.Plus,
                    { navigator.open(Route.NewGroup()) },
                    testTag = "groups.empty.newGroup",
                ),
            secondaryAction =
                PbEmptyAction(
                    stringResource(R.string.groups_invite_friends),
                    PbIcon.UserAdd,
                    { navigator.open(Route.AddFriend) },
                    testTag = "groups.empty.inviteFriends",
                ),
            testTag = "groups.empty",
        )
        return
    }
    Column {
        groups.rows.forEachIndexed { index, row ->
            GroupRowItem(
                row,
                tagPrefix = "groups.row",
                showDivider = index < groups.rows.lastIndex,
                onClick = { navigator.open(row.group.route()) },
            )
        }
    }
    if (groups.archived.isNotEmpty()) {
        Spacer(Modifier.height(PbSpace.S24))
        Column(
            Modifier.testTag("groups.archived"),
            verticalArrangement = Arrangement.spacedBy(PbSpace.S4),
        ) {
            PbSectionHeader(stringResource(R.string.groups_archived))
            Column {
                groups.archived.forEachIndexed { index, row ->
                    GroupRowItem(
                        row,
                        tagPrefix = "groups.row",
                        showDivider = index < groups.archived.lastIndex,
                        onClick = { navigator.open(Route.Project(row.group.id)) },
                    )
                }
            }
        }
    }
}
