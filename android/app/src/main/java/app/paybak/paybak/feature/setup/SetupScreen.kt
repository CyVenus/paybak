package app.paybak.paybak.feature.setup

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.paybak.paybak.feature.PlaceholderScreen
import app.paybak.paybak.ui.components.PbButton
import app.paybak.paybak.ui.components.PbSetupHeader

/** Setup steps 3 and 4 are optional and show Skip. */
private const val FIRST_OPTIONAL_STEP = 3

/**
 * `setup1`–`setup4`: one screen with a [step], so the setup header animates in place. PLACEHOLDER:
 * the Setup phase builds each step (screens-setup.md §1–4).
 *
 * @param onContinue Continue, Skip and Not now: the next step, or All set after step 4.
 */
@Composable
fun SetupScreen(step: Int, onContinue: () -> Unit, onBack: () -> Unit) {
    PlaceholderScreen(
        id = "setup$step",
        phase = "Setup",
        header = {
            PbSetupHeader(
                step = step,
                onBack = onBack,
                showSkip = step >= FIRST_OPTIONAL_STEP,
                onSkip = onContinue,
            )
        },
    ) {
        PbButton("Continue", onClick = onContinue, Modifier.fillMaxWidth())
    }
}
