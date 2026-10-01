package app.paybak.paybak.feature.settle

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import app.paybak.paybak.R
import app.paybak.paybak.data.ledger.actions.markNotReceived
import app.paybak.paybak.data.ledger.collectSnapshot
import app.paybak.paybak.domain.settle.NotReceivedDraft
import app.paybak.paybak.domain.settle.notReceivedDraft
import app.paybak.paybak.navigation.LocalLedger
import app.paybak.paybak.navigation.LocalMainNavigator
import app.paybak.paybak.navigation.Route
import app.paybak.paybak.ui.components.PbButton
import app.paybak.paybak.ui.components.PbButtonStyle
import app.paybak.paybak.ui.components.PbSheet
import app.paybak.paybak.ui.components.PbTextArea
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles

/**
 * The `notReceived` route (settleNotReceived, screens-settle §8): instead of silently rejecting a
 * friend's payment claim, the receiver sends them an editable note. Send marks the claim Not
 * received (its card leaves Home, Activity and the inbox; the debt stays owed) and closes; Cancel,
 * the scrim or a swipe keeps the claim. Tagged `screen.notReceived`, parts
 * `notReceived.<note|send|cancel>`.
 */
@Composable
fun NotReceivedSheet(route: Route.NotReceived) {
    val navigator = LocalMainNavigator.current
    val ledger = LocalLedger.current
    val snapshot by ledger.collectSnapshot()
    val draft = remember(snapshot, route) { snapshot.view.notReceivedDraft(route.paymentId) }
    if (draft == null) {
        // The claim was already answered (confirmed elsewhere, or cancelled by the payer).
        LaunchedEffect(Unit) { navigator.dismissSheet() }
        return
    }
    var note by rememberSaveable { mutableStateOf(draft.note) }
    var send by remember { mutableStateOf(false) }
    PbSheet(
        onDismiss = {
            // Sent once the sheet has slid away, so the claim card leaves behind it.
            if (send) ledger.markNotReceived(draft.paymentId, note.trim().ifEmpty { null })
            navigator.dismissSheet()
        },
        showClose = false,
        testTag = "notReceived",
    ) { dismiss ->
        NotReceivedContent(
            draft,
            note,
            onNoteChange = { note = it },
            onSend = {
                send = true
                dismiss()
            },
            onCancel = dismiss,
        )
    }
}

@Composable
private fun NotReceivedContent(
    draft: NotReceivedDraft,
    note: String,
    onNoteChange: (String) -> Unit,
    onSend: () -> Unit,
    onCancel: () -> Unit,
) {
    Column(
        Modifier.fillMaxWidth().padding(top = PbSpace.S8).testTag("screen.notReceived"),
        verticalArrangement = Arrangement.spacedBy(PbSpace.S24),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(PbSpace.S8)) {
            Text(draft.title, style = PbTextStyles.Title3, color = PbColors.Text.Primary)
            Text(draft.context, style = PbTextStyles.Subheadline, color = PbColors.Text.Secondary)
        }
        PbTextArea(
            value = note,
            onValueChange = onNoteChange,
            label = stringResource(R.string.settle_note),
            helper = draft.helper,
            fieldModifier = Modifier.testTag("notReceived.note"),
        )
        Column(verticalArrangement = Arrangement.spacedBy(PbSpace.S12)) {
            PbButton(
                stringResource(R.string.pb_send),
                onClick = onSend,
                modifier = Modifier.fillMaxWidth().testTag("notReceived.send"),
            )
            PbButton(
                stringResource(R.string.settle_cancel),
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth().testTag("notReceived.cancel"),
                style = PbButtonStyle.Secondary,
            )
        }
    }
}
