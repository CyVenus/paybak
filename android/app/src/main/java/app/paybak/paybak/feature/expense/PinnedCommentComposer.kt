package app.paybak.paybak.feature.expense

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import app.paybak.paybak.R
import app.paybak.paybak.ui.components.PbComposer

/**
 * The comment composer while you type (activity §4.6, `expenseComment`): pinned above the keyboard,
 * focused as it appears. Hiding the keyboard (system back, a tap outside) or leaving the field ends
 * composing through [onDone]; the draft stays in [value].
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun PinnedCommentComposer(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    onDone: () -> Unit,
) {
    val focus = remember { FocusRequester() }
    val done by rememberUpdatedState(onDone)
    val imeVisible = WindowInsets.isImeVisible
    var imeShown by remember { mutableStateOf(false) }
    var focused by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { focus.requestFocus() }
    LaunchedEffect(imeVisible) {
        if (imeVisible) imeShown = true else if (imeShown) done()
    }
    PbComposer(
        value,
        onValueChange,
        onSend = onSend,
        placeholder = stringResource(R.string.add_comment_placeholder),
        pinned = true,
        fieldModifier =
            Modifier.focusRequester(focus).testTag("expense.composer").onFocusChanged {
                if (it.isFocused) focused = true else if (focused) done()
            },
    )
}
