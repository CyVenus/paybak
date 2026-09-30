package app.paybak.paybak.feature.launch

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import app.paybak.paybak.R
import app.paybak.paybak.navigation.Destination
import app.paybak.paybak.rive.PaybakRiveAsset
import app.paybak.paybak.rive.PaybakRiveIllustration
import app.paybak.paybak.ui.components.PbButton
import app.paybak.paybak.ui.components.PbOnboardingTopBar
import app.paybak.paybak.ui.components.PbPageDots
import app.paybak.paybak.ui.components.PbScreen
import app.paybak.paybak.ui.theme.LocalReduceMotion
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbMotion
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles
import app.rive.RivePointerInputMode
import kotlin.math.abs

/** The copy of one Welcome step (screens-launch.md §2.4). */
private class StepCopy(@param:StringRes val headline: Int, @param:StringRes val body: Int)

private val stepCopy =
    listOf(
        StepCopy(R.string.welcome_headline_1, R.string.welcome_body_1),
        StepCopy(R.string.welcome_headline_2, R.string.welcome_body_2),
        StepCopy(R.string.welcome_headline_3, R.string.welcome_body_3),
    )

// The text change (screens-launch.md §2.5), timed to the Rive's own exit and entrance.
private const val TEXT_OUT_MILLIS = 150
private const val TEXT_IN_MILLIS = 300
private val TextShift = 24.dp
private val TextInEasing = CubicBezierEasing(0.2f, 0f, 0f, 1f)

/** Figma's 362 content width, so the headlines break where the designs do on wider phones. */
private val TextMeasure = 362.dp

/**
 * Lines kept for every headline, so the body stays put between steps. (Step 1's headline fits on
 * one line once kerned, although Figma wraps it.)
 */
private const val HEADLINE_LINES = 2

// A swipe changes the step once it travels this far, or flicks this fast (per second).
private val SwipeDistance = 50.dp
private val SwipeSpeed = 300.dp

/**
 * `welcome1`–`welcome3`: one screen with a [step] (screens-launch.md §2). Continue and horizontal
 * swipes change the step: the Onboarding Rive animates its own slide, the text slides and fades,
 * the dots grow and the CTA label crossfades. System back is the navigator's.
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
    val stepTransition = updateTransition(step, label = "Welcome step")
    // One step at a time: taps and swipes wait until the text has settled on the current step.
    val settled = stepTransition.currentState == stepTransition.targetState
    val lastStep = step == Destination.WELCOME_STEPS
    val showStep = { target: Int ->
        if (settled && target in 1..Destination.WELCOME_STEPS) onStepChange(target)
    }
    val reduceMotion = LocalReduceMotion.current
    val density = LocalDensity.current
    val textShift = with(density) { TextShift.roundToPx() }
    var dragged by remember { mutableFloatStateOf(0f) }

    PbScreen(
        id = "welcome$step",
        modifier =
            Modifier.draggable(
                state = rememberDraggableState { dragged += it },
                orientation = Orientation.Horizontal,
                onDragStarted = { dragged = 0f },
                onDragStopped = { velocity ->
                    val swipe =
                        with(density) {
                            when {
                                abs(velocity) >= SwipeSpeed.toPx() -> velocity
                                abs(dragged) >= SwipeDistance.toPx() -> dragged
                                else -> 0f
                            }
                        }
                    // The finger moving right to left goes forward.
                    if (swipe < 0) showStep(step + 1) else if (swipe > 0) showStep(step - 1)
                },
            ),
    ) {
        PbOnboardingTopBar(
            showSkip = !lastStep,
            onSkip = onSkip,
            skipModifier = Modifier.testTag("welcome.skip"),
        )
        Spacer(Modifier.height(PbSpace.S8))
        PaybakRiveIllustration(
            asset = PaybakRiveAsset.Onboarding,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            pointerInputMode = RivePointerInputMode.Observe,
            numbers = mapOf(PaybakRiveAsset.STEP_PROPERTY to step.toFloat()),
        )
        Spacer(Modifier.height(PbSpace.S32))
        stepTransition.AnimatedContent(transitionSpec = { textChange(reduceMotion, textShift) }) {
            StepText(stepCopy[it - 1])
        }
        Spacer(Modifier.weight(1f))
        PbPageDots(active = step, count = Destination.WELCOME_STEPS)
        Spacer(Modifier.height(PbSpace.S24))
        PbButton(
            label =
                stringResource(
                    if (lastStep) R.string.welcome_get_started else R.string.welcome_continue
                ),
            onClick = {
                when {
                    !settled -> Unit
                    lastStep -> onGetStarted()
                    else -> showStep(step + 1)
                }
            },
            modifier = Modifier.fillMaxWidth().testTag("welcome.continue"),
        )
        Spacer(Modifier.height(PbSpace.S16))
    }
}

@Composable
private fun StepText(copy: StepCopy) {
    Column(
        modifier = Modifier.widthIn(max = TextMeasure),
        verticalArrangement = Arrangement.spacedBy(PbSpace.S12),
    ) {
        Text(
            text = stringResource(copy.headline),
            modifier = Modifier.testTag("welcome.headline").semantics { heading() },
            style = PbTextStyles.Title1,
            color = PbColors.Text.Primary,
            minLines = HEADLINE_LINES,
        )
        Text(
            text = stringResource(copy.body),
            style = PbTextStyles.Body,
            color = PbColors.Text.Secondary,
        )
    }
}

/**
 * Going forward, the old text fades out to the left, then the new one fades in from the right;
 * going back mirrors it. With reduce motion it is a plain crossfade.
 */
private fun AnimatedContentTransitionScope<Int>.textChange(
    reduceMotion: Boolean,
    shift: Int,
): ContentTransform {
    if (reduceMotion) {
        return fadeIn(tween(PbMotion.SWAP_MILLIS)) togetherWith fadeOut(tween(PbMotion.SWAP_MILLIS))
    }
    val direction = if (targetState > initialState) 1 else -1
    val enter = tween<Float>(TEXT_IN_MILLIS, delayMillis = TEXT_OUT_MILLIS, easing = TextInEasing)
    val enterSlide =
        tween<IntOffset>(TEXT_IN_MILLIS, delayMillis = TEXT_OUT_MILLIS, easing = TextInEasing)
    val exit = tween<Float>(TEXT_OUT_MILLIS, easing = EaseIn)
    val exitSlide = tween<IntOffset>(TEXT_OUT_MILLIS, easing = EaseIn)
    return (fadeIn(enter) + slideInHorizontally(enterSlide) { direction * shift }) togetherWith
        (fadeOut(exit) + slideOutHorizontally(exitSlide) { -direction * shift }) using
        SizeTransform(clip = false)
}
