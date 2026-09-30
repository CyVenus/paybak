package app.paybak.paybak.feature.addexpense

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import app.paybak.paybak.data.ledger.actions.addExpense
import app.paybak.paybak.domain.model.ExpenseDraft
import app.paybak.paybak.domain.model.ME
import app.paybak.paybak.domain.model.newId
import app.paybak.paybak.feature.RoutePlaceholder
import app.paybak.paybak.navigation.LocalLedger
import app.paybak.paybak.navigation.LocalMainNavigator
import app.paybak.paybak.navigation.PickRequest
import app.paybak.paybak.navigation.Route
import app.paybak.paybak.navigation.RouteResult
import app.paybak.paybak.navigation.RouteResultEffect
import app.paybak.paybak.ui.components.PbButton
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbTextStyles

/** The placeholder's sample amount: ₹1,000. */
private const val SAMPLE_AMOUNT = 100_000L

/**
 * The `addExpense` route (addExpenseEmpty … addExpenseDiscard, scanAddExpense). PLACEHOLDER owned
 * by lane A (M3): replace this file and keep the signature. It saves a sample equal split with the
 * people picked in `pickPeople`, to exercise the result channel and `didSave`.
 */
@Composable
fun AddExpenseScreen(route: Route.AddExpense) {
    val navigator = LocalMainNavigator.current
    val ledger = LocalLedger.current
    val requestId = rememberSaveable { newId() }
    var people by rememberSaveable { mutableStateOf(route.args.draft?.personIds.orEmpty() - ME) }
    RouteResultEffect(requestId) { result ->
        (result as? RouteResult.People)?.let { people = it.personIds }
    }
    RoutePlaceholder(route) {
        Text(
            "Split with: ${people.joinToString()}",
            style = PbTextStyles.Subheadline,
            color = PbColors.Text.Secondary,
        )
        PbButton(
            "Split with…",
            onClick = {
                navigator.open(Route.PickPeople(PickRequest(requestId), selected = people))
            },
            modifier = Modifier.fillMaxWidth().testTag("addExpense.people"),
        )
        PbButton(
            "Save",
            onClick = {
                val draft =
                    ExpenseDraft.equal(listOf(ME) + people)
                        .copy(title = "Placeholder expense", amount = SAMPLE_AMOUNT)
                navigator.didSave(Route.Expense(ledger.addExpense(draft)), "Expense added")
            },
            modifier = Modifier.fillMaxWidth().testTag("addExpense.save"),
            enabled = people.isNotEmpty(),
        )
    }
}
