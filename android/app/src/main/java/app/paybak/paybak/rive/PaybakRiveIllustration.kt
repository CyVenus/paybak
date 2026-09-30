package app.paybak.paybak.rive

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import app.rive.RivePointerInputMode

/**
 * A Paybak illustration laid out like Figma: it reserves [PaybakRiveAsset.slotSize] and draws the
 * Rive view at its native [PaybakRiveAsset.viewSize], centred on the slot and not clipped, so the
 * bleed artboards overhang by 12 dp without being scaled down. A tap on a character (the file's own
 * listener) gives a light haptic.
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
    Box(modifier.size(asset.slotSize), contentAlignment = Alignment.Center) {
        PaybakRive(
            resId = asset.resId,
            artboard = asset.artboard,
            stateMachine = asset.stateMachine,
            modifier = Modifier.requiredSize(asset.viewSize),
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
