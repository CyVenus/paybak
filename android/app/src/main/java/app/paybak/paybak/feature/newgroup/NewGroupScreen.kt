package app.paybak.paybak.feature.newgroup

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import app.paybak.paybak.data.ledger.actions.addGroup
import app.paybak.paybak.domain.model.GroupDraft
import app.paybak.paybak.domain.model.GroupKind
import app.paybak.paybak.domain.model.ProjectInfo
import app.paybak.paybak.feature.RoutePlaceholder
import app.paybak.paybak.navigation.LocalLedger
import app.paybak.paybak.navigation.LocalMainNavigator
import app.paybak.paybak.navigation.NewGroupMode
import app.paybak.paybak.navigation.Route
import app.paybak.paybak.ui.components.PbButton

/**
 * The `newGroup` route (newGroup, newGroupProject). PLACEHOLDER owned by lane A (M3): replace this
 * file and keep the signature. Create adds a sample group or project, to exercise `didCreateGroup`.
 */
@Composable
fun NewGroupScreen(route: Route.NewGroup) {
    val navigator = LocalMainNavigator.current
    val ledger = LocalLedger.current
    val project = route.mode == NewGroupMode.Project
    RoutePlaceholder(route) {
        PbButton(
            "Create",
            onClick = {
                val draft =
                    if (project)
                        GroupDraft("New project", kind = GroupKind.Project, project = ProjectInfo())
                    else GroupDraft("New group")
                navigator.didCreateGroup(ledger.addGroup(draft), project)
            },
            modifier = Modifier.fillMaxWidth().testTag("newGroup.create"),
        )
    }
}
