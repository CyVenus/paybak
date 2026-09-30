package app.paybak.paybak.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import app.paybak.paybak.R
import app.paybak.paybak.ui.components.PbAddAction
import app.paybak.paybak.ui.components.PbAddSheetRows
import app.paybak.paybak.ui.components.PbSheet

/**
 * The ＋ Add sheet (`addSheet`, screens-home §5): four rows over the current tab root. A row slides
 * the sheet away, then opens its modal (app-architecture §2.3). Tagged `home.addSheet`, its close
 * `home.addSheet.close` and rows `home.addSheet.<expense|payment|lend|group>`.
 */
@Composable
fun AddSheet(route: Route.AddSheet) {
    val navigator = LocalMainNavigator.current
    var next by remember { mutableStateOf<Route?>(null) }
    PbSheet(
        onDismiss = { next?.let(navigator::replaceSheet) ?: navigator.dismissSheet() },
        title = stringResource(R.string.shell_add_title),
        testTag = "home.addSheet",
    ) { dismiss ->
        PbAddSheetRows(
            onAction = { action ->
                next =
                    when (action) {
                        PbAddAction.Expense -> Route.AddExpense()
                        PbAddAction.Payment -> Route.RecordPayment()
                        PbAddAction.Lend -> Route.LendMoney()
                        PbAddAction.Group -> Route.NewGroup()
                    }
                dismiss()
            },
            testTag = "home.addSheet",
        )
    }
}
