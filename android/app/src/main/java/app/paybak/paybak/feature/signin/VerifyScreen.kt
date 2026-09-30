package app.paybak.paybak.feature.signin

import android.os.SystemClock
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import app.paybak.paybak.R
import app.paybak.paybak.data.SignInCode
import app.paybak.paybak.ui.components.CodeLength
import app.paybak.paybak.ui.components.PbCodeField
import app.paybak.paybak.ui.components.PbOnboardingTopBar
import app.paybak.paybak.ui.components.PbScreen
import app.paybak.paybak.ui.components.PbTextButton
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles
import kotlinx.coroutines.delay

/** A short pause after the sixth digit, so it visibly fills before the code is checked. */
private const val VERIFY_DELAY_MILLIS = 250L

/** The countdown is announced this often (and at 0), not every second. */
private const val ANNOUNCE_EVERY_SECONDS = 10

/**
 * `verify` / `verifyWrong`: the 6-digit code (screens-signin.md §2–3). The code is checked as soon
 * as the sixth digit arrives: `000000` goes on to setup, anything else shows the wrong-code state
 * until a digit is edited. Resend unlocks 30 s after the code was sent; it clears the boxes and
 * restarts the countdown.
 *
 * @param rejectedCode The wrong code the error state shows, or null.
 * @param onRejectedCodeChange Reports a wrong code, or null once the error is cleared.
 * @param onBack Back and "Change": both return to Sign in.
 */
@Composable
fun VerifyScreen(
    contact: String,
    rejectedCode: String?,
    onRejectedCodeChange: (String?) -> Unit,
    onVerified: () -> Unit,
    onBack: () -> Unit,
) {
    val isError = rejectedCode != null
    var code by rememberSaveable { mutableStateOf(rejectedCode.orEmpty()) }
    // Opened straight into the error state (Figma's wrong-code frame), Resend is already available.
    var resendAt by rememberSaveable {
        mutableLongStateOf(if (isError) 0L else now() + SignInCode.RESEND_AFTER_SECONDS * 1_000L)
    }
    val secondsLeft = rememberSecondsUntil(resendAt)
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { focus.requestFocus() }

    val haptics = LocalHapticFeedback.current
    val currentOnVerified by rememberUpdatedState(onVerified)
    val currentOnRejectedCodeChange by rememberUpdatedState(onRejectedCodeChange)
    LaunchedEffect(code, isError) {
        if (code.length < CodeLength || isError) return@LaunchedEffect
        delay(VERIFY_DELAY_MILLIS)
        if (SignInCode.isCorrect(code)) {
            currentOnVerified()
        } else {
            haptics.performHapticFeedback(HapticFeedbackType.Reject)
            currentOnRejectedCodeChange(code)
        }
    }
    val resend: () -> Unit = {
        code = ""
        if (isError) onRejectedCodeChange(null)
        resendAt = now() + SignInCode.RESEND_AFTER_SECONDS * 1_000L
        focus.requestFocus()
    }

    PbScreen(id = if (isError) "verifyWrong" else "verify") {
        PbOnboardingTopBar(showBack = true, onBack = onBack)
        Spacer(Modifier.height(PbSpace.S24))
        Text(
            text = stringResource(R.string.verify_headline),
            modifier = Modifier.semantics { heading() },
            style = PbTextStyles.Title1,
            color = PbColors.Text.Primary,
        )
        Spacer(Modifier.height(PbSpace.S12))
        SentToLine(contact, onChange = onBack)
        Spacer(Modifier.height(PbSpace.S32))
        PbCodeField(
            code = code,
            onCodeChange = {
                code = it
                if (isError) onRejectedCodeChange(null)
            },
            modifier = Modifier.focusRequester(focus).testTag("verify.code"),
            isError = isError,
        )
        if (isError) {
            Spacer(Modifier.height(PbSpace.S16))
            Text(
                text = stringResource(R.string.verify_wrong_code),
                modifier = Modifier.testTag("verify.error"),
                style = PbTextStyles.Footnote,
                color = PbColors.Text.Destructive,
            )
        }
        if (secondsLeft > 0) {
            Spacer(Modifier.height(PbSpace.S16))
            Countdown(secondsLeft)
        } else {
            // The button's label then starts where the countdown text did.
            Spacer(Modifier.height(PbSpace.S4))
            PbTextButton(
                label = stringResource(R.string.verify_resend),
                onClick = resend,
                modifier = Modifier.testTag("verify.resend"),
            )
        }
    }
}

/** "Resend code in 0:24", announced every 10 s rather than every second. */
@Composable
private fun Countdown(secondsLeft: Int) {
    val text = stringResource(R.string.verify_resend_in, formatCountdown(secondsLeft))
    val announced =
        stringResource(
            R.string.verify_resend_in,
            formatCountdown(roundUp(secondsLeft, ANNOUNCE_EVERY_SECONDS)),
        )
    Text(
        text = text,
        modifier =
            Modifier.testTag("verify.countdown").clearAndSetSemantics {
                this.text = AnnotatedString(announced)
                liveRegion = LiveRegionMode.Polite
            },
        style = PbTextStyles.Footnote,
        color = PbColors.Text.Tertiary,
    )
}

/** m:ss, e.g. "0:05". */
internal fun formatCountdown(seconds: Int): String =
    "${seconds / 60}:${(seconds % 60).toString().padStart(2, '0')}"

private fun roundUp(value: Int, step: Int): Int = (value + step - 1) / step * step

/** Keeps counting in deep sleep and across process death, so a saved deadline stays right. */
private fun now(): Long = SystemClock.elapsedRealtime()

/** Whole seconds left until [deadline] (rounded up), ticking down to 0. */
@Composable
private fun rememberSecondsUntil(deadline: Long): Int {
    var seconds by remember(deadline) { mutableIntStateOf(secondsUntil(deadline)) }
    LaunchedEffect(deadline) {
        while (seconds > 0) {
            // Wake just as the rounded-up value changes.
            delay((deadline - now() - 1) % 1_000 + 1)
            seconds = secondsUntil(deadline)
        }
    }
    return seconds
}

private fun secondsUntil(deadline: Long): Int =
    ((deadline - now() + 999) / 1_000).toInt().coerceAtLeast(0)
