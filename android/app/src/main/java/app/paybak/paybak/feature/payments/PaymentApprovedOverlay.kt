package app.paybak.paybak.feature.payments

import android.os.Build
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.WindowManager
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.currentStateAsState
import app.paybak.paybak.R
import app.paybak.paybak.data.ledger.LedgerRepository
import app.paybak.paybak.domain.settle.PaymentApprovals
import app.paybak.paybak.rive.PaybakRive
import app.paybak.paybak.rive.PaybakRiveAsset
import app.paybak.paybak.service.HapticKind
import app.paybak.paybak.service.rememberHaptics
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbLayout
import app.paybak.paybak.ui.theme.PbShapes
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles
import app.rive.Fit
import app.rive.RivePointerInputMode
import kotlinx.coroutines.delay

/**
 * One pass of the coin sequence: hand-rise, coin-drop, hand-grab, coin-shatter and hand-lower are
 * 460 frames at 60 fps, after which the file loops.
 */
private const val SEQUENCE_MILLIS = 7_667L

/** How long the headline alone shows when the scene can't be drawn. */
private const val FALLBACK_MILLIS = 2_000L

private const val FADE_IN_MILLIS = 250
private const val FADE_OUT_MILLIS = 300

/**
 * Plays the payment-approved scene when friends confirm payments you made ([queue]). It runs while
 * the app is in front: approvals that arrive in the background, or while one plays, wait and are
 * shown together. Going to the background ends a scene that's playing. Put it once in the main
 * app's root.
 */
@Composable
fun PaymentApprovedHost(queue: PaymentApprovalQueue, ledger: LedgerRepository) {
    val waiting by queue.waiting.collectAsState()
    val lifecycle by LocalLifecycleOwner.current.lifecycle.currentStateAsState()
    val isResumed = lifecycle.isAtLeast(Lifecycle.State.RESUMED)
    // Saved, so a scene that's playing survives a recreated activity.
    var headline by rememberSaveable { mutableStateOf<String?>(null) }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(waiting, headline, isResumed) {
        if (headline == null && isResumed && waiting.isNotEmpty()) {
            // The scene's window takes the focus, so the keyboard goes down with the text field.
            focusManager.clearFocus()
            headline = PaymentApprovals.headline(queue.takeAll(), ledger.ledger.value)
        }
    }
    LaunchedEffect(lifecycle) {
        if (!lifecycle.isAtLeast(Lifecycle.State.STARTED)) headline = null
    }
    headline?.let { PaymentApprovedOverlay(it, onFinish = { headline = null }) }
}

/**
 * The payer's "payment approved" moment: `paybak-payment.riv` fills the screen (its `main`
 * artboard brings its own white) under [headline], such as "Meera confirmed ₹450". It's a
 * full-screen dialog window, so it covers whatever is open, sheets and alerts included, and draws
 * behind the system bars. It fades in with a success haptic, plays the coin sequence once and fades
 * out; a tap or back closes it sooner. It plays with Remove animations too. Tagged
 * `paymentApproved`.
 */
@Composable
private fun PaymentApprovedOverlay(headline: String, onFinish: () -> Unit) {
    var closing by remember { mutableStateOf(false) }
    Dialog(
        onDismissRequest = { closing = true },
        properties =
            DialogProperties(
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false,
                windowTitle = headline,
            ),
    ) {
        val view = LocalView.current
        val window = (view.parent as? DialogWindowProvider)?.window
        SideEffect {
            window ?: return@SideEffect
            // Full screen, drawing its own system bar backgrounds (else they stay black). Compose
            // only does this itself when it grows a WRAP_CONTENT window.
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.setLayout(MATCH_PARENT, MATCH_PARENT)
            window.setDimAmount(0f)
            // Content fades in and out instead.
            window.setWindowAnimations(0)
            // 3-button navigation would draw a scrim over the palm.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                window.isNavigationBarContrastEnforced = false
            }
            // Dark icons over the scene's white, as on every screen.
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = true
                isAppearanceLightNavigationBars = true
            }
        }

        val haptics = rememberHaptics()
        val alpha = remember { Animatable(0f) }
        // Null until the scene says whether it can draw.
        var drawn by remember { mutableStateOf<Boolean?>(null) }
        LaunchedEffect(Unit) {
            haptics.perform(HapticKind.Success)
            alpha.animateTo(1f, tween(FADE_IN_MILLIS))
        }
        LaunchedEffect(drawn) {
            val canDraw = drawn ?: return@LaunchedEffect
            delay(if (canDraw) SEQUENCE_MILLIS else FALLBACK_MILLIS)
            closing = true
        }
        LaunchedEffect(closing) {
            if (closing) {
                alpha.animateTo(0f, tween(FADE_OUT_MILLIS))
                onFinish()
            }
        }

        val closeLabel = stringResource(R.string.payment_approved_close)
        Box(
            Modifier.fillMaxSize()
                .graphicsLayer { this.alpha = alpha.value }
                .background(PbColors.Bg.Primary)
                .pointerInput(Unit) { detectTapGestures { closing = true } }
                .clearAndSetSemantics {
                    testTagsAsResourceId = true
                    testTag = "paymentApproved"
                    contentDescription = headline
                    onClick(label = closeLabel) {
                        closing = true
                        true
                    }
                }
        ) {
            PaybakRive(
                resId = PaybakRiveAsset.Payment.resId,
                artboard = PaybakRiveAsset.Payment.artboard,
                stateMachine = PaybakRiveAsset.Payment.stateMachine,
                modifier = Modifier.fillMaxSize(),
                // The default scale factor is 1 px; the artboard is laid out in dp.
                fit = Fit.Layout(LocalDensity.current.density),
                // Taps reach the box, which closes the scene.
                pointerInputMode = RivePointerInputMode.Observe,
                onLoaded = { drawn = it },
            )
            Text(
                text = headline,
                style = PbTextStyles.Title2,
                color = PbColors.Text.Primary,
                textAlign = TextAlign.Center,
                modifier =
                    Modifier.align(Alignment.TopCenter)
                        .windowInsetsPadding(
                            WindowInsets.safeDrawing.only(
                                WindowInsetsSides.Top + WindowInsetsSides.Horizontal
                            )
                        )
                        .padding(horizontal = PbLayout.ScreenMargin)
                        .padding(top = PbSpace.S16)
                        // White on the scene's white, so it only shows when the coin bursts: its
                        // lines cross the top and the scene flashes black for a moment.
                        .background(PbColors.Bg.Primary, PbShapes.Card)
                        .padding(horizontal = PbSpace.S16, vertical = PbSpace.S8),
            )
        }
    }
}
