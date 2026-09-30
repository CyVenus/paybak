package app.paybak.paybak.feature.setup

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import app.paybak.paybak.data.ProfileStore
import app.paybak.paybak.navigation.popTransition
import app.paybak.paybak.navigation.pushTransition
import app.paybak.paybak.ui.components.PbScreenFrame
import app.paybak.paybak.ui.components.PbSetupHeader
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbLayout

/** Steps 3 and 4 are optional and show Skip. */
private const val FIRST_OPTIONAL_STEP = 3

/**
 * `setup1`–`setup4`: the first-run setup (screens-setup.md). One header stays pinned and animates
 * its progress in place, while the step below it slides like a push, forward or back. Each step
 * saves its part of the profile when the user moves on.
 *
 * The keyboard doesn't resize the screen: Setup 1's Continue rides it, while Setup 2 and 3 keep
 * their Continue pinned so the list and the payment preview stay visible (§0.14).
 *
 * @param onNext Continue, Skip and Not now: the next step, or All set after step 4.
 */
@Composable
fun SetupScreen(step: Int, profileStore: ProfileStore, onNext: () -> Unit, onBack: () -> Unit) {
    // A step still sliding out can't move the flow on: only the current step's buttons count.
    val currentStep by rememberUpdatedState(step)
    val nextFrom = { from: Int -> if (from == currentStep) onNext() }

    PbScreenFrame(id = "setup$step", resizeForKeyboard = false) {
        PbSetupHeader(
            step = step,
            onBack = onBack,
            modifier = Modifier.padding(horizontal = PbLayout.ScreenMargin),
            showSkip = step >= FIRST_OPTIONAL_STEP,
            onSkip = { nextFrom(step) },
        )
        AnimatedContent(
            targetState = step,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            transitionSpec = {
                if (targetState > initialState) pushTransition() else popTransition()
            },
            label = "Setup step",
        ) { shownStep ->
            // Opaque, so the step sliding over another hides it.
            Box(Modifier.fillMaxSize().background(PbColors.Bg.Primary)) {
                val next = { nextFrom(shownStep) }
                when (shownStep) {
                    1 -> NameStep(profileStore, onContinue = next)
                    2 -> CurrencyStep(profileStore, onContinue = next)
                    3 -> PaymentStep(profileStore, onContinue = next)
                    else -> NotificationsStep(profileStore, onDone = next)
                }
            }
        }
    }
}
