package app.paybak.paybak.feature.groups

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import app.paybak.paybak.feature.RoutePlaceholder
import app.paybak.paybak.navigation.LocalMainNavigator
import app.paybak.paybak.navigation.Route
import app.paybak.paybak.ui.components.PbButton
import app.paybak.paybak.ui.components.PbButtonStyle

/**
 * The `groups` tab root (groupsList, friendsList, groupsEmpty, friendsEmpty); its segment is
 * `navigator.groupsSegment`. PLACEHOLDER owned by lane B (M4): replace this file and keep the
 * signature.
 */
@Composable
fun GroupsTabScreen(route: Route.Groups) {
    val navigator = LocalMainNavigator.current
    RoutePlaceholder(route, title = "Groups") {
        PbButton(
            "New group",
            onClick = { navigator.open(Route.NewGroup()) },
            Modifier.fillMaxWidth().testTag("groups.newGroup"),
        )
        PbButton(
            "Add friend",
            onClick = { navigator.open(Route.AddFriend) },
            Modifier.fillMaxWidth(),
            style = PbButtonStyle.Secondary,
        )
    }
}
