package app.paybak.paybak.feature.pickers

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import app.paybak.paybak.feature.RoutePlaceholder
import app.paybak.paybak.navigation.LocalLedger
import app.paybak.paybak.navigation.LocalMainNavigator
import app.paybak.paybak.navigation.PickMode
import app.paybak.paybak.navigation.Route
import app.paybak.paybak.navigation.RouteResult
import app.paybak.paybak.ui.components.PbButton

/**
 * The `pickPeople` route (addExpenseSplitWith). PLACEHOLDER owned by lane A (M3): replace this file
 * and keep the signature. Done answers with every friend (or the first, in single mode).
 */
@Composable
fun PeoplePickerScreen(route: Route.PickPeople) {
    val navigator = LocalMainNavigator.current
    val ledger by LocalLedger.current.ledger.collectAsState()
    val people = ledger.people.filter { route.allowsGuests || !it.isGuest }.map { it.id }
    RoutePlaceholder(route) {
        PbButton(
            "Done",
            onClick = {
                val result =
                    if (route.mode == PickMode.Single) RouteResult.Person(people.first())
                    else RouteResult.People(people)
                navigator.complete(route.request.id, result)
            },
            modifier = Modifier.fillMaxWidth().testTag("pickPeople.done"),
            enabled = people.isNotEmpty(),
        )
    }
}
