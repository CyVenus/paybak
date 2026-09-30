package app.paybak.paybak.feature.home

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.paybak.paybak.feature.PlaceholderScreen
import app.paybak.paybak.navigation.HomeState
import app.paybak.paybak.ui.components.PbAddButton
import app.paybak.paybak.ui.components.PbButton

/**
 * `homeFirstDay`, `homeActive`, `homeAllSettled` and `homeAddSheet`: one screen in [state], with
 * the Add sheet open or closed. PLACEHOLDER: the Home phase builds it (screens-home.md).
 */
@Composable
fun HomeScreen(state: HomeState, addSheetOpen: Boolean, onAddSheetOpenChange: (Boolean) -> Unit) {
    PlaceholderScreen(id = if (addSheetOpen) "homeAddSheet" else state.id, phase = "Home") {
        if (addSheetOpen) {
            PbButton(
                "Close the Add sheet",
                onClick = { onAddSheetOpenChange(false) },
                Modifier.fillMaxWidth(),
            )
        } else {
            PbAddButton(onClick = { onAddSheetOpenChange(true) })
        }
    }
}
