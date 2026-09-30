package app.paybak.paybak.feature.signin

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.paybak.paybak.feature.PlaceholderScreen
import app.paybak.paybak.ui.components.PbButton
import app.paybak.paybak.ui.components.PbOnboardingTopBar

/** `signIn`. PLACEHOLDER: the Sign-in phase builds the real screen (screens-signin.md §1). */
@Composable
fun SignInScreen(onCodeSent: () -> Unit, onBack: () -> Unit) {
    PlaceholderScreen(
        id = "signIn",
        phase = "Sign-in",
        header = { PbOnboardingTopBar(showBack = true, onBack = onBack) },
    ) {
        PbButton("Send code", onClick = onCodeSent, Modifier.fillMaxWidth())
    }
}
