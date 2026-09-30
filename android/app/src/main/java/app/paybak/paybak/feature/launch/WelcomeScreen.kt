package app.paybak.paybak.feature.launch

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.paybak.paybak.feature.PlaceholderScreen
import app.paybak.paybak.navigation.Destination
import app.paybak.paybak.ui.components.PbButton
import app.paybak.paybak.ui.components.PbOnboardingTopBar
import app.paybak.paybak.ui.components.PbPageDots

/**
 * `welcome1`–`welcome3`: one screen with a [step]. PLACEHOLDER: the Launch phase builds the real
 * screen (screens-launch.md §2) with the Onboarding Rive and swipe between steps.
 *
 * @param onSkip Skip on steps 1–2 (crossfade to Get Started).
 * @param onGetStarted The step-3 CTA (push to Get Started).
 */
@Composable
fun WelcomeScreen(
    step: Int,
    onStepChange: (Int) -> Unit,
    onSkip: () -> Unit,
    onGetStarted: () -> Unit,
) {
    val lastStep = step == Destination.WELCOME_STEPS
    PlaceholderScreen(
        id = "welcome$step",
        phase = "Launch",
        header = { PbOnboardingTopBar(showSkip = !lastStep, onSkip = onSkip) },
        content = { PbPageDots(active = step, count = Destination.WELCOME_STEPS) },
    ) {
        if (lastStep) {
            PbButton("Get started", onClick = onGetStarted, Modifier.fillMaxWidth())
        } else {
            PbButton("Continue", onClick = { onStepChange(step + 1) }, Modifier.fillMaxWidth())
        }
    }
}
