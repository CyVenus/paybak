package app.paybak.paybak.rive

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import app.rive.RivePointerInputMode

/**
 * Below this share of its Figma size an illustration would only be a speck, so it is left out and
 * its space goes to the text and actions. (Rive also can't draw into an empty surface.)
 */
private const val MIN_SCALE = 0.25f

/**
 * A Paybak illustration laid out like Figma: it reserves [PaybakRiveAsset.slotSize] and draws the
 * Rive view at [PaybakRiveAsset.viewSize], centred on the slot and not clipped, so the bleed
 * artboards overhang it by 12 dp. When the space is narrower or shorter than the slot, the slot and
 * the view shrink together, keeping their proportions (screens-launch.md §5); they never grow. A
 * tap on a character (the file's own listener) gives a light haptic.
 *
 * @param numbers Number properties to drive, e.g. the Onboarding `step`.
 * @param onTap Called after the haptic whenever the file reports a tap.
 */
@Composable
fun PaybakRiveIllustration(
    asset: PaybakRiveAsset,
    modifier: Modifier = Modifier,
    pointerInputMode: RivePointerInputMode = RivePointerInputMode.Consume,
    numbers: Map<String, Float> = emptyMap(),
    onTap: () -> Unit = {},
    onReady: (PaybakRiveController) -> Unit = {},
) {
    val haptics = LocalHapticFeedback.current
    BoxWithConstraints(modifier) {
        val slot = asset.slotSize
        val scale = minOf(1f, maxWidth / slot.width, maxHeight / slot.height)
        if (scale >= MIN_SCALE) {
            Box(Modifier.size(slot * scale), contentAlignment = Alignment.Center) {
                PaybakRive(
                    resId = asset.resId,
                    artboard = asset.artboard,
                    stateMachine = asset.stateMachine,
                    modifier = Modifier.requiredSize(asset.viewSize * scale),
                    pointerInputMode = pointerInputMode,
                    numbers = numbers,
                    observedTriggers = listOfNotNull(asset.tapTrigger),
                    onTrigger = {
                        haptics.performHapticFeedback(HapticFeedbackType.ContextClick)
                        onTap()
                    },
                    onReady = onReady,
                )
            }
        }
    }
}
