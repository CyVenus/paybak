package app.paybak.paybak.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

/** The [TextFieldValue] a Paybak text field shows, and what it does when the user edits it. */
internal class EndCursorText(
    val value: TextFieldValue,
    val onValueChange: (TextFieldValue) -> Unit,
)

/**
 * Keeps the user's cursor and selection while they type, and puts the cursor at the end of text set
 * from outside (a prefilled value, Clear). The String overload of BasicTextField would put the
 * cursor of a prefilled value at its start.
 */
@Composable
internal fun rememberEndCursorText(text: String, onTextChange: (String) -> Unit): EndCursorText {
    var editing by remember { mutableStateOf(TextFieldValue(text, TextRange(text.length))) }
    val shown = if (editing.text == text) editing else TextFieldValue(text, TextRange(text.length))
    return EndCursorText(shown) { edited ->
        editing = edited
        if (edited.text != text) onTextChange(edited.text)
    }
}
