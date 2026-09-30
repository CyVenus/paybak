package app.paybak.paybak.feature.groups

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import app.paybak.paybak.R
import app.paybak.paybak.ui.components.PbButton
import app.paybak.paybak.ui.components.PbSheet
import app.paybak.paybak.ui.components.PbTextField
import app.paybak.paybak.ui.theme.PbSpace

/**
 * Group settings › Name (§5.3 proposal): a Medium sheet with the name focused and Save, which is
 * off while the name is blank. Renaming shows everywhere the group does.
 */
@Composable
internal fun RenameGroupSheet(name: String, onSave: (String) -> Unit, onDismiss: () -> Unit) {
    var text by rememberSaveable { mutableStateOf(name) }
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { focus.requestFocus() }
    PbSheet(
        onDismiss = onDismiss,
        title = stringResource(R.string.groups_rename_title),
        testTag = "groupSettings.renameSheet",
    ) { dismiss ->
        val save = {
            if (text.isNotBlank()) {
                onSave(text.trim())
                dismiss()
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(PbSpace.S16)) {
            PbTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxWidth().testTag("groupSettings.renameField"),
                placeholder = stringResource(R.string.groups_rename_placeholder),
                onClear = { text = "" },
                keyboardOptions =
                    KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Done,
                    ),
                keyboardActions = KeyboardActions(onDone = { save() }),
                fieldModifier = Modifier.focusRequester(focus),
            )
            PbButton(
                stringResource(R.string.pb_save),
                onClick = save,
                modifier = Modifier.fillMaxWidth().testTag("groupSettings.renameSave"),
                enabled = text.isNotBlank(),
            )
        }
    }
}
