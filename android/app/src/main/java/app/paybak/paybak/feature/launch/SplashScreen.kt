package app.paybak.paybak.feature.launch

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import app.paybak.paybak.ui.components.PbLogo
import app.paybak.paybak.ui.components.PbLogoLayout
import app.paybak.paybak.ui.theme.LocalReduceMotion
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbMotion
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Time on screen before moving on (prototype AFTER_TIMEOUT 1500 ms). */
private const val HOLD_MILLIS = 1_500L

private const val MARK_START_SCALE = 0.86f
private const val MARK_FADE_MILLIS = 300
private val MarkSpring = spring<Float>(dampingRatio = 0.72f, stiffness = 130f)

private const val WORDMARK_DELAY_MILLIS = 350
private const val WORDMARK_MILLIS = 500
private val WordmarkRise = 8.dp
private val WordmarkEasing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)

/** With reduce motion the lockup only fades in. */
private const val REDUCED_FADE_MILLIS = 200

/**
 * `splash`: the stacked logo on white, centred in the whole screen rather than the safe area. The
 * mark springs in and the wordmark rises after it (screens-launch.md §1.4); with reduce motion the
 * lockup only fades in. After 1.5 s it calls [onFinished], and the flow dissolves to the next
 * screen.
 */
@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val reduceMotion = LocalReduceMotion.current
    val entrance = remember { SplashEntrance(reduceMotion) }
    val currentOnFinished by rememberUpdatedState(onFinished)
    LaunchedEffect(Unit) {
        // A cold start's first frame can take a while to draw. Starting once it is on screen keeps
        // the entrance from being skipped and times the hold from when the splash is visible.
        repeat(2) { withFrameNanos {} }
        launch { entrance.play() }
        delay(HOLD_MILLIS)
        currentOnFinished()
    }
    Box(
        modifier = Modifier.fillMaxSize().background(PbColors.Bg.Primary).testTag("screen.splash"),
        contentAlignment = Alignment.Center,
    ) {
        PbLogo(
            layout = PbLogoLayout.Stacked,
            markModifier =
                Modifier.graphicsLayer {
                    scaleX = entrance.markScale.value
                    scaleY = entrance.markScale.value
                    alpha = entrance.markAlpha.value
                },
            wordmarkModifier =
                Modifier.graphicsLayer {
                    alpha = entrance.wordmarkAlpha.value
                    translationY = entrance.wordmarkDrop.value * WordmarkRise.toPx()
                },
        )
    }
}

/** The lockup's entrance values, starting hidden. */
private class SplashEntrance(private val reduceMotion: Boolean) {
    val markScale = Animatable(if (reduceMotion) 1f else MARK_START_SCALE)
    val markAlpha = Animatable(0f)
    val wordmarkAlpha = Animatable(0f)

    /** 1 while the wordmark sits [WordmarkRise] below its place, 0 once it is in place. */
    val wordmarkDrop = Animatable(if (reduceMotion) 0f else 1f)

    suspend fun play() = coroutineScope {
        if (reduceMotion) {
            val fade = tween<Float>(REDUCED_FADE_MILLIS)
            launch { markAlpha.animateTo(1f, fade) }
            launch { wordmarkAlpha.animateTo(1f, fade) }
        } else {
            val wordmark = tween<Float>(WORDMARK_MILLIS, WORDMARK_DELAY_MILLIS, WordmarkEasing)
            launch { markScale.animateTo(1f, MarkSpring) }
            launch { markAlpha.animateTo(1f, tween(MARK_FADE_MILLIS, easing = PbMotion.EaseOut)) }
            launch { wordmarkAlpha.animateTo(1f, wordmark) }
            launch { wordmarkDrop.animateTo(0f, wordmark) }
        }
    }
}
