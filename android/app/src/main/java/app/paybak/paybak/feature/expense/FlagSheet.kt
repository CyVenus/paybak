package app.paybak.paybak.feature.expense

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import app.paybak.paybak.data.ledger.actions.flagExpense
import app.paybak.paybak.navigation.LocalLedger
import app.paybak.paybak.ui.components.PbButton
import app.paybak.paybak.ui.components.PbSheet
import app.paybak.paybak.ui.components.PbTextArea
import app.paybak.paybak.ui.theme.PbSpace

/**
 * "Flag an issue" (activity §4.3-G, proposal): a note on what looks wrong; Flag expense marks the
 * expense Disputed (it still counts) and tells everyone on it.
 */
@Composable
fun FlagSheet(expenseId: String, onDismiss: () -> Unit) {
    val ledger = LocalLedger.current
    var note by rememberSaveable { mutableStateOf("") }
    var send by remember { mutableStateOf(false) }
    PbSheet(
        onDismiss = {
            if (send) ledger.flagExpense(expenseId, note.trim())
            onDismiss()
        },
        title = "Flag an issue",
        testTag = "flag.sheet",
    ) { dismiss ->
        Column(verticalArrangement = Arrangement.spacedBy(PbSpace.S16)) {
            PbTextArea(
                note,
                { note = it.take(MAX_NOTE) },
                placeholder = "What looks wrong?",
                fieldModifier = Modifier.testTag("flag.note"),
            )
            PbButton(
                "Flag expense",
                onClick = {
                    send = true
                    dismiss()
                },
                modifier = Modifier.fillMaxWidth().testTag("flag.send"),
                enabled = note.isNotBlank(),
            )
        }
    }
}

private const val MAX_NOTE = 300
