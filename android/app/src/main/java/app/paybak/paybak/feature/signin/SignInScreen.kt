package app.paybak.paybak.feature.signin

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.windowInsetsPadding
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
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import app.paybak.paybak.R
import app.paybak.paybak.data.SignInContact
import app.paybak.paybak.data.parseSignInContact
import app.paybak.paybak.ui.components.PbButton
import app.paybak.paybak.ui.components.PbOnboardingTopBar
import app.paybak.paybak.ui.components.PbScreen
import app.paybak.paybak.ui.components.PbTextField
import app.paybak.paybak.ui.components.PbTitleBlock
import app.paybak.paybak.ui.components.keyboardWithGap
import app.paybak.paybak.ui.theme.PbSpace

/**
 * `signIn`: one field for an email or a phone number (screens-signin.md §1). Send code, which rides
 * 12 dp above the keyboard, is enabled only for a plausible contact.
 *
 * @param initialContact The contact the code last went to, so "Change" on Verify returns to it.
 * @param onSendCode Receives the normalised contact (phone numbers get "+91 " when they have no
 *   country code).
 */
@Composable
fun SignInScreen(
    initialContact: String,
    onSendCode: (SignInContact) -> Unit,
    onBack: () -> Unit,
) {
    var input by rememberSaveable { mutableStateOf(initialContact) }
    val contact = parseSignInContact(input)
    val send = { contact?.let(onSendCode) }
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { focus.requestFocus() }

    PbScreen(
        id = "signIn",
        content = {
            PbOnboardingTopBar(showBack = true, onBack = onBack)
            Spacer(Modifier.height(PbSpace.S24))
            PbTitleBlock(
                title = stringResource(R.string.sign_in_headline),
                body = stringResource(R.string.sign_in_body),
            )
            Spacer(Modifier.height(PbSpace.S24))
            PbTextField(
                value = input,
                onValueChange = { input = it },
                label = stringResource(R.string.sign_in_label),
                placeholder = stringResource(R.string.sign_in_placeholder),
                helper = stringResource(R.string.sign_in_helper),
                keyboardOptions =
                    KeyboardOptions(
                        capitalization = KeyboardCapitalization.None,
                        autoCorrectEnabled = false,
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Send,
                    ),
                keyboardActions = KeyboardActions(onSend = { send() }),
                fieldModifier =
                    Modifier.focusRequester(focus).testTag("signIn.field").semantics {
                        contentType = ContentType.Username
                    },
            )
        },
        footer = {
            PbButton(
                label = stringResource(R.string.sign_in_send_code),
                onClick = { send() },
                modifier =
                    Modifier.windowInsetsPadding(WindowInsets.keyboardWithGap)
                        .fillMaxWidth()
                        .testTag("signIn.sendCode"),
                enabled = contact != null,
            )
        },
    )
}
