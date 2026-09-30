package app.paybak.paybak.feature.groups

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import app.paybak.paybak.R
import app.paybak.paybak.data.ledger.collectSnapshot
import app.paybak.paybak.domain.groups.friendListRow
import app.paybak.paybak.domain.groups.friendsSummary
import app.paybak.paybak.domain.groups.groupsList
import app.paybak.paybak.navigation.GroupsSegment
import app.paybak.paybak.navigation.LocalLedger
import app.paybak.paybak.navigation.LocalMainNavigator
import app.paybak.paybak.navigation.LocalTabBarPadding
import app.paybak.paybak.navigation.Route
import app.paybak.paybak.ui.components.PbNavAction
import app.paybak.paybak.ui.components.PbNavHeader
import app.paybak.paybak.ui.components.PbNavHeaderInline
import app.paybak.paybak.ui.components.PbSegmentedControl
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbLayout
import app.paybak.paybak.ui.theme.PbSize
import app.paybak.paybak.ui.theme.PbSpace

/**
 * The `groups` tab root (screens-groups §3): the large "Groups" title with New group (＋) or Add
 * friend, the Groups | Friends segments (kept in the navigator while the app runs), and the
 * segment's list or empty state. The inline title bar fades in once the large title scrolls away.
 * A hidden `groups.state.<groupsList|friendsList|groupsEmpty|friendsEmpty>` names the state.
 */
@Composable
fun GroupsTabScreen(route: Route.Groups) {
    val navigator = LocalMainNavigator.current
    val snapshot by LocalLedger.current.collectSnapshot()
    val segment = navigator.groupsSegment
    val view = snapshot.view
    val groups = remember(snapshot) { view.groupsList(snapshot.groups) }
    val friends = remember(snapshot) { snapshot.friends.map(view::friendListRow) }
    val summary = remember(snapshot) { view.friendsSummary(snapshot.friends) }
    val state =
        when {
            segment == GroupsSegment.Friends -> if (friends.isEmpty()) "friendsEmpty" else "friendsList"
            groups.rows.isEmpty() && groups.archived.isEmpty() -> "groupsEmpty"
            else -> "groupsList"
        }
    val scroll = rememberScrollState()
    val titleHeight = with(LocalDensity.current) { PbSize.Tap.roundToPx() }
    Box(
        Modifier.fillMaxSize().background(PbColors.Bg.Primary).testTag("screen.groups"),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            Modifier.widthIn(max = PbLayout.MaxContentWidth)
                .fillMaxSize()
                .verticalScroll(scroll)
                .statusBarsPadding()
                .padding(LocalTabBarPadding.current)
                .padding(horizontal = PbLayout.ScreenMargin)
        ) {
            PbNavHeader(
                stringResource(R.string.groups_title),
                Modifier.testTag("groups.title"),
                action =
                    if (segment == GroupsSegment.Groups) {
                        PbNavAction(
                            PbIcon.Plus,
                            stringResource(R.string.groups_new_group),
                            { navigator.open(Route.NewGroup()) },
                            testTag = "groups.action",
                        )
                    } else {
                        PbNavAction(
                            PbIcon.UserAdd,
                            stringResource(R.string.groups_add_friend),
                            { navigator.open(Route.AddFriend) },
                            testTag = "groups.action",
                        )
                    },
            )
            Spacer(Modifier.height(PbSpace.S16))
            PbSegmentedControl(
                options =
                    listOf(
                        stringResource(R.string.groups_segment_groups),
                        stringResource(R.string.groups_segment_friends),
                    ),
                selectedIndex = segment.ordinal,
                onSelect = { navigator.groupsSegment = GroupsSegment.entries[it] },
                modifier = Modifier.fillMaxWidth(),
                segmentTags = listOf("groups.segment.groups", "groups.segment.friends"),
            )
            when (segment) {
                GroupsSegment.Groups -> GroupsSegmentContent(groups)
                GroupsSegment.Friends -> FriendsSegmentContent(friends, summary)
            }
        }
        PbNavHeaderInline(stringResource(R.string.groups_title), visible = scroll.value > titleHeight)
        Box(Modifier.testTag("groups.state.$state"))
    }
}
