package app.paybak.paybak.feature.setup

import android.content.ClipData
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import app.paybak.paybak.R
import app.paybak.paybak.data.ProfileStore
import app.paybak.paybak.data.isPlausibleUpiId
import app.paybak.paybak.ui.components.PbButton
import app.paybak.paybak.ui.components.PbPaymentPreview
import app.paybak.paybak.ui.components.PbScreenBody
import app.paybak.paybak.ui.components.PbTextField
import app.paybak.paybak.ui.components.PbTitleBlock
import app.paybak.paybak.ui.components.PbToastHost
import app.paybak.paybak.ui.components.rememberPbToastState
import app.paybak.paybak.ui.components.rememberUserAvatar
import app.paybak.paybak.ui.theme.PbLayout
import app.paybak.paybak.ui.theme.PbSize
import app.paybak.paybak.ui.theme.PbSpace
import kotlinx.coroutines.launch

/**
 * `setup3`: the UPI ID friends pay to (screens-setup.md §3), optional. The preview card mirrors the
 * field live and copies the ID. Continue saves it (or no ID when empty) and rejects text that isn't
 * a UPI ID; Skip, in the header, leaves the saved ID as it was.
 */
@Composable
internal fun PaymentStep(profileStore: ProfileStore, onContinue: () -> Unit) {
    val profile by profileStore.profile.collectAsState()
    var upiId by rememberSaveable { mutableStateOf(profile.upiId) }
    var invalid by rememberSaveable { mutableStateOf(false) }
    val trimmed = upiId.trim()
    val submit = {
        if (trimmed.isEmpty() || isPlausibleUpiId(trimmed)) {
            profileStore.update { it.copy(upiId = trimmed) }
            onContinue()
        } else {
            invalid = true
        }
    }

    val toast = rememberPbToastState()
    val copiedMessage = stringResource(R.string.setup3_copied)
    val clipLabel = stringResource(R.string.setup3_label)
    val clipboard = LocalClipboard.current
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val copy = {
        scope.launch {
            clipboard.setClipEntry(ClipEntry(ClipData.newPlainText(clipLabel, trimmed)))
        }
        haptics.performHapticFeedback(HapticFeedbackType.Confirm)
        toast.show(copiedMessage)
    }

    val focusManager = LocalFocusManager.current
    Box(
        Modifier.fillMaxSize().pointerInput(Unit) {
            detectTapGestures(onTap = { focusManager.clearFocus() })
        }
    ) {
        PbScreenBody(
            modifier = Modifier.padding(horizontal = PbLayout.ScreenMargin),
            footer = {
                PbButton(
                    label = stringResource(R.string.setup_continue),
                    onClick = submit,
                    modifier = Modifier.fillMaxWidth().testTag("setup3.continue"),
                )
            },
        ) {
            Spacer(Modifier.height(PbSpace.S24))
            PbTitleBlock(
                title = stringResource(R.string.setup3_headline),
                body = stringResource(R.string.setup3_body),
            )
            Spacer(Modifier.height(PbSpace.S24))
            PbTextField(
                value = upiId,
                onValueChange = {
                    upiId = it
                    invalid = false
                },
                label = stringResource(R.string.setup3_label),
                placeholder = stringResource(R.string.setup3_placeholder),
                helper =
                    stringResource(
                        if (invalid) R.string.setup3_invalid else R.string.setup3_helper
                    ),
                isError = invalid,
                keyboardOptions =
                    KeyboardOptions(
                        capitalization = KeyboardCapitalization.None,
                        autoCorrectEnabled = false,
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Done,
                    ),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                fieldModifier = Modifier.testTag("setup3.upi"),
            )
            Spacer(Modifier.height(PbSpace.S24))
            PbPaymentPreview(
                name = profile.name,
                upiId = trimmed,
                avatar = rememberUserAvatar(profile, profileStore),
                onCopy = copy,
                placeholder = stringResource(R.string.setup3_placeholder),
                copyButtonModifier = Modifier.testTag("setup3.copy"),
            )
        }
        // 16 dp above Continue: at the Toast's usual spot it would cover the button.
        PbToastHost(
            toast,
            Modifier.align(Alignment.BottomCenter).padding(bottom = PbSize.ButtonLg + PbSpace.S16),
        )
    }
}
