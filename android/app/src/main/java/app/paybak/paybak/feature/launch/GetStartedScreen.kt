package app.paybak.paybak.feature.launch

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.paybak.paybak.feature.PlaceholderScreen
import app.paybak.paybak.ui.components.PbButton
import app.paybak.paybak.ui.components.PbButtonStyle
import app.paybak.paybak.ui.components.PbTextButton
import app.paybak.paybak.ui.icons.PbIcon

/**
 * `getStarted`. PLACEHOLDER: the Launch phase builds the real screen (screens-launch.md §3). Apple
 * and Google go straight to Setup 1; email or phone goes to Sign in.
 */
@Composable
fun GetStartedScreen(
    onContinueWithApple: () -> Unit,
    onContinueWithGoogle: () -> Unit,
    onContinueWithEmailOrPhone: () -> Unit,
) {
    PlaceholderScreen(id = "getStarted", phase = "Launch") {
        PbButton(
            "Continue with Apple",
            onContinueWithApple,
            Modifier.fillMaxWidth(),
            leadingIcon = PbIcon.Apple,
        )
        PbButton(
            "Continue with Google",
            onContinueWithGoogle,
            Modifier.fillMaxWidth(),
            style = PbButtonStyle.Secondary,
            leadingIcon = PbIcon.Google,
        )
        PbTextButton("Continue with email or phone", onClick = onContinueWithEmailOrPhone)
    }
}
