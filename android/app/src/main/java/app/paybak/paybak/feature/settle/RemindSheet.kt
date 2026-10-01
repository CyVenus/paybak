package app.paybak.paybak.feature.settle

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import app.paybak.paybak.R
import app.paybak.paybak.data.ledger.actions.sendReminder
import app.paybak.paybak.data.ledger.collectSnapshot
import app.paybak.paybak.domain.model.ReminderTone
import app.paybak.paybak.domain.model.ReminderVia
import app.paybak.paybak.domain.settle.RemindDraft
import app.paybak.paybak.domain.settle.remindDraft
import app.paybak.paybak.navigation.LocalLedger
import app.paybak.paybak.navigation.LocalMainNavigator
import app.paybak.paybak.navigation.LocalProfileStore
import app.paybak.paybak.navigation.Route
import app.paybak.paybak.navigation.rememberDebugStartScreen
import app.paybak.paybak.service.HapticKind
import app.paybak.paybak.service.rememberHaptics
import app.paybak.paybak.ui.components.PbAvatarContent
import app.paybak.paybak.ui.components.PbBalance
import app.paybak.paybak.ui.components.PbButton
import app.paybak.paybak.ui.components.PbButtonStyle
import app.paybak.paybak.ui.components.PbCard
import app.paybak.paybak.ui.components.PbPersonRow
import app.paybak.paybak.ui.components.PbPersonTrailing
import app.paybak.paybak.ui.components.PbSegmentedControl
import app.paybak.paybak.ui.components.PbSheet
import app.paybak.paybak.ui.components.PbTextArea
import app.paybak.paybak.ui.components.avatarContent
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles

private val Tones = listOf(ReminderTone.Friendly, ReminderTone.Neutral)

/**
 * The `remind` route (settleRemind, settleRemindShare; screens-settle §6–7): a pre-written reminder
 * the user can edit, in a Friendly or Neutral tone. Send in Paybak logs it, closes the sheet and
 * shows "Reminder sent to {name}"; Share… hands the message to the system share sheet and closes
 * the sheet once an app was picked. Reminders change no balance. Tagged `screen.remind`, parts
 * `remind.<close|tone.friendly|tone.neutral|message|send|share>`.
 */
@Composable
fun RemindSheet(route: Route.Remind) {
    val navigator = LocalMainNavigator.current
    val snapshot by LocalLedger.current.collectSnapshot()
    val profile by LocalProfileStore.current.profile.collectAsState()
    val draft =
        remember(snapshot, route, profile.upiId) {
            snapshot.view.remindDraft(route.personId, route.context, profile.upiId)
        }
    if (draft == null) {
        // They owe you nothing (any more): there's nothing to remind them of.
        LaunchedEffect(Unit) { navigator.dismissSheet() }
        return
    }
    PbSheet(onDismiss = navigator::dismissSheet, title = draft.title, testTag = "remind") { dismiss ->
        RemindContent(draft, dismiss)
    }
}

@Composable
private fun RemindContent(draft: RemindDraft, dismiss: () -> Unit) {
    val navigator = LocalMainNavigator.current
    val ledger = LocalLedger.current
    val haptics = rememberHaptics()
    val person = ledger.ledger.value.person(draft.personId)
    var tone by rememberSaveable { mutableStateOf(ReminderTone.Friendly) }
    var message by rememberSaveable { mutableStateOf(draft.message(ReminderTone.Friendly)) }
    var edited by rememberSaveable { mutableStateOf(false) }
    val sentToast = stringResource(R.string.settle_reminder_sent, draft.name)
    fun log(via: ReminderVia) =
        ledger.sendReminder(draft.personId, draft.amount, draft.context, tone, message, via)
    val share = rememberReminderShare {
        log(ReminderVia.Share)
        dismiss()
    }
    if (rememberDebugStartScreen("settleRemindShare") != null) {
        LaunchedEffect(Unit) { share(message) }
    }
    Column(
        Modifier.fillMaxWidth().testTag("screen.remind"),
        verticalArrangement = Arrangement.spacedBy(PbSpace.S24),
    ) {
        PbCard {
            PbPersonRow(
                draft.name,
                person?.avatarContent() ?: PbAvatarContent.Initials(draft.name.take(1)),
                subtitle = draft.subtitle,
                trailing =
                    PbPersonTrailing.Amount(
                        draft.amountText,
                        PbBalance.Owed,
                        label = draft.dueLabel,
                        overdue = draft.overdue,
                    ),
                onCard = true,
                showDivider = false,
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(PbSpace.S8)) {
            Text(
                stringResource(R.string.settle_tone),
                style = PbTextStyles.Subheadline,
                color = PbColors.Text.Secondary,
            )
            PbSegmentedControl(
                options =
                    listOf(
                        stringResource(R.string.settle_tone_friendly),
                        stringResource(R.string.settle_tone_neutral),
                    ),
                selectedIndex = Tones.indexOf(tone),
                onSelect = { index ->
                    haptics.perform(HapticKind.Selection)
                    tone = Tones[index]
                    // A tone swaps in its own message unless the user wrote their own.
                    if (!edited) message = draft.message(tone)
                },
                modifier = Modifier.fillMaxWidth(),
                segmentTags = listOf("remind.tone.friendly", "remind.tone.neutral"),
            )
        }
        PbTextArea(
            value = message,
            onValueChange = {
                message = it
                edited = true
            },
            label = stringResource(R.string.settle_message),
            helper = stringResource(R.string.settle_message_helper),
            fieldModifier = Modifier.testTag("remind.message"),
        )
        Column(verticalArrangement = Arrangement.spacedBy(PbSpace.S12)) {
            PbButton(
                stringResource(R.string.settle_send_in_paybak),
                onClick = {
                    haptics.perform(HapticKind.Success)
                    log(ReminderVia.Paybak)
                    navigator.toast(sentToast)
                    dismiss()
                },
                modifier = Modifier.fillMaxWidth().testTag("remind.send"),
                enabled = message.isNotBlank(),
            )
            PbButton(
                stringResource(R.string.settle_share),
                onClick = { share(message) },
                modifier = Modifier.fillMaxWidth().testTag("remind.share"),
                style = PbButtonStyle.Secondary,
                enabled = message.isNotBlank(),
            )
        }
    }
}
