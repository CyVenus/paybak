package app.paybak.paybak.feature.signin

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.paybak.paybak.feature.PlaceholderScreen
import app.paybak.paybak.ui.components.PbButton
import app.paybak.paybak.ui.components.PbButtonStyle
import app.paybak.paybak.ui.components.PbOnboardingTopBar

/**
 * `verify` / `verifyWrong`: one screen, [wrongCode] is its error state. PLACEHOLDER: the Sign-in
 * phase builds the real screen (screens-signin.md §2–3).
 */
@Composable
fun VerifyScreen(
    wrongCode: Boolean,
    onWrongCodeChange: (Boolean) -> Unit,
    onVerified: () -> Unit,
    onBack: () -> Unit,
) {
    PlaceholderScreen(
        id = if (wrongCode) "verifyWrong" else "verify",
        phase = "Sign-in",
        header = { PbOnboardingTopBar(showBack = true, onBack = onBack) },
    ) {
        PbButton("Enter 000000", onClick = onVerified, Modifier.fillMaxWidth())
        PbButton(
            label = if (wrongCode) "Clear the error" else "Enter a wrong code",
            onClick = { onWrongCodeChange(!wrongCode) },
            modifier = Modifier.fillMaxWidth(),
            style = PbButtonStyle.Secondary,
        )
    }
}
