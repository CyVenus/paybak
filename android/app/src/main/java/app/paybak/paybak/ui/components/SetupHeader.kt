package app.paybak.paybak.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import app.paybak.paybak.R
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbMotion
import app.paybak.paybak.ui.theme.PbShapes
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles

/** Number of setup steps shown by [PbSetupHeader]. */
const val SetupStepCount = 4

/**
 * `Navigation / Setup Header` (`PBSetupHeader`): back, optional Skip, a 4-segment progress bar and
 * "Step N of 4". When [step] changes, the next segment fills from its leading edge (0.35 s), the
 * number slides, and Skip fades, so keeping one header across the setup steps animates in place.
 */
@Composable
fun PbSetupHeader(
    step: Int,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    showSkip: Boolean = false,
    onSkip: () -> Unit = {},
) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(PbSpace.S8)) {
        NavigationRow {
            PbBackButton(onBack)
            Spacer(Modifier.weight(1f))
            SkipButton(visible = showSkip, onClick = onSkip)
        }
        Row(
            modifier = Modifier.fillMaxWidth().height(4.dp).clearAndSetSemantics {},
            horizontalArrangement = Arrangement.spacedBy(PbSpace.S4),
        ) {
            repeat(SetupStepCount) { index ->
                ProgressSegment(filled = index < step, Modifier.weight(1f))
            }
        }
        AnimatedContent(
            targetState = step,
            transitionSpec = {
                val direction = if (targetState > initialState) 1 else -1
                val spec = tween<IntOffset>(PbMotion.SWAP_MILLIS)
                (slideInVertically(spec) { it * direction } + fadeIn(tween(PbMotion.SWAP_MILLIS)))
                    .togetherWith(
                        slideOutVertically(spec) { -it * direction } +
                            fadeOut(tween(PbMotion.SWAP_MILLIS))
                    )
            },
            label = "Setup step",
        ) { shownStep ->
            Text(
                text = stringResource(R.string.pb_setup_step, shownStep, SetupStepCount),
                style = PbTextStyles.Footnote,
                color = PbColors.Text.Secondary,
            )
        }
    }
}

/** A grey track whose black fill grows from the leading edge. */
@Composable
private fun ProgressSegment(filled: Boolean, modifier: Modifier = Modifier) {
    val fraction by
        animateFloatAsState(
            targetValue = if (filled) 1f else 0f,
            animationSpec = tween(PbMotion.PUSH_MILLIS, easing = PbMotion.EaseInOut),
            label = "Progress segment",
        )
    Box(modifier.fillMaxHeight().background(PbColors.Bg.Indicator, PbShapes.Pill)) {
        Box(
            Modifier.fillMaxHeight()
                .fillMaxWidth(fraction)
                .background(PbColors.Bg.Inverse, PbShapes.Pill)
        )
    }
}

@Preview(showBackground = true, widthDp = 362)
@Composable
private fun PbSetupHeaderPreview() {
    Column(Modifier.width(362.dp), verticalArrangement = Arrangement.spacedBy(PbSpace.S16)) {
        PbSetupHeader(step = 1, onBack = {})
        PbSetupHeader(step = 3, onBack = {}, showSkip = true)
    }
}
