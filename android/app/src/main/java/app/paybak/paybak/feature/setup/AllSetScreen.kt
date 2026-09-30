package app.paybak.paybak.feature.setup

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.paybak.paybak.feature.PlaceholderScreen
import app.paybak.paybak.ui.components.PbButton

/** `allSet`. PLACEHOLDER: the Setup phase builds the real screen (screens-setup.md §5). */
@Composable
fun AllSetScreen(onGoHome: () -> Unit) {
    PlaceholderScreen(id = "allSet", phase = "Setup") {
        PbButton("Go to Home", onClick = onGoHome, Modifier.fillMaxWidth())
    }
}
