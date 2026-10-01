package app.paybak.paybak.debug.gallery

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.paybak.paybak.rive.PaybakRiveAsset
import app.paybak.paybak.rive.PaybakRiveController
import app.paybak.paybak.rive.PaybakRiveIllustration
import app.paybak.paybak.ui.components.PbButton
import app.paybak.paybak.ui.components.PbButtonSize
import app.paybak.paybak.ui.components.PbButtonStyle
import app.paybak.paybak.ui.components.PbSegmentedControl
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbSize
import app.paybak.paybak.ui.theme.PbSpace
import app.rive.RivePointerInputMode

/** A trigger that plays each file's tap animation, to check `fire()` from code. */
private val replayTriggers =
    mapOf(
        PaybakRiveAsset.GetStarted to "tapMiddle",
        PaybakRiveAsset.Notifications to "bellTapped",
        PaybakRiveAsset.AllSet to "tapBadge",
        PaybakRiveAsset.HomeFirstDay to "tapCharacter",
        PaybakRiveAsset.HomeAllSquare to "tapped",
    )

/**
 * The six illustrations at their Figma slot sizes (the red outline is the slot; three artboards
 * overhang it by 12 dp). Tap a character: its animation plays, the phone gives a light haptic, and
 * the counter goes up.
 */
@Composable
internal fun RivePage() {
    GalleryPage {
        // The payment scene is full screen, not an illustration.
        PaybakRiveAsset.entries
            .filter { it != PaybakRiveAsset.Payment }
            .forEach { asset -> RiveSample(asset) }
    }
}

@Composable
private fun RiveSample(asset: PaybakRiveAsset) {
    var taps by remember { mutableIntStateOf(0) }
    var step by remember { mutableIntStateOf(1) }
    var controller by remember { mutableStateOf<PaybakRiveController?>(null) }
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(PbSpace.S8),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val slot = asset.slotSize
        val view = asset.viewSize
        GalleryLabel(
            "${asset.artboard} · slot ${slot.width.value.toInt()}×${slot.height.value.toInt()}" +
                " · view ${view.width.value.toInt()}×${view.height.value.toInt()}"
        )
        PaybakRiveIllustration(
            asset = asset,
            modifier =
                Modifier.border(PbSize.Hairline, PbColors.Border.Destructive.copy(alpha = 0.4f)),
            pointerInputMode = RivePointerInputMode.Observe,
            numbers =
                if (asset == PaybakRiveAsset.Onboarding)
                    mapOf(PaybakRiveAsset.STEP_PROPERTY to step.toFloat())
                else emptyMap(),
            onTap = { taps++ },
            onReady = { controller = it },
        )
        if (asset == PaybakRiveAsset.Onboarding) {
            PbSegmentedControl(
                options = listOf("Step 1", "Step 2", "Step 3"),
                selectedIndex = step - 1,
                onSelect = { step = it + 1 },
                modifier = Modifier.width(330.dp),
            )
        }
        replayTriggers[asset]?.let { trigger ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(PbSpace.S12),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                GalleryLabel("${asset.tapTrigger} × $taps")
                PbButton(
                    label = "Fire $trigger",
                    onClick = { controller?.fire(trigger) },
                    style = PbButtonStyle.Secondary,
                    size = PbButtonSize.Small,
                )
            }
        }
    }
}
