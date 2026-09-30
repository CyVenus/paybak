package app.paybak.paybak.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbMaterial
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles

/** The alert's 34 dp corners (a literal in Figma, not a radius token). */
private val AlertShape = RoundedCornerShape(34.dp)

/** `Action` of `Overlay / Alert`: what the right-hand button does. */
enum class PbAlertAction {
    /** Red: something is destroyed ("Discard", "Delete"). */
    Destructive,

    /** Black: nothing is destroyed ("Settle up"). */
    Primary,
}

/**
 * `Overlay / Alert` (`PBAlert`) shown as a dialog over the 40 % scrim: the choice must be explicit,
 * so tapping outside does nothing, and system back cancels. Show it while the screen's state asks
 * for it; both buttons should clear that state. Parts are tagged "[testTag].cancel" and
 * "[testTag].action".
 */
@Composable
fun PbAlert(
    title: String,
    message: String,
    cancelLabel: String,
    actionLabel: String,
    onCancel: () -> Unit,
    onAction: () -> Unit,
    action: PbAlertAction = PbAlertAction.Destructive,
    testTag: String? = null,
) {
    Dialog(
        onDismissRequest = onCancel,
        properties =
            DialogProperties(dismissOnClickOutside = false, usePlatformDefaultWidth = false),
    ) {
        val window = (LocalView.current.parent as? DialogWindowProvider)?.window
        SideEffect { window?.setDimAmount(PbColors.Bg.Scrim.alpha) }
        PbAlertCard(
            title,
            message,
            cancelLabel,
            actionLabel,
            onCancel,
            onAction,
            action = action,
            testTag = testTag,
        )
    }
}

/**
 * The alert card itself: 300 dp wide, radius 34, a centred Headline title and Subheadline message,
 * and two Small pills sharing the width: `Button / Secondary` to cancel and a Destructive or
 * Primary action.
 */
@Composable
fun PbAlertCard(
    title: String,
    message: String,
    cancelLabel: String,
    actionLabel: String,
    onCancel: () -> Unit,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
    action: PbAlertAction = PbAlertAction.Destructive,
    testTag: String? = null,
) {
    Column(
        modifier =
            modifier
                .width(300.dp)
                .partTag(testTag)
                .dropShadow(AlertShape, PbMaterial.Glass.shadow)
                .background(PbColors.Bg.Primary, AlertShape)
                .padding(PbSpace.S20),
        verticalArrangement = Arrangement.spacedBy(PbSpace.S20),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(PbSpace.S4),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = title,
                modifier = Modifier.semantics { heading() },
                style = PbTextStyles.Headline,
                color = PbColors.Text.Primary,
                textAlign = TextAlign.Center,
            )
            Text(
                text = message,
                style = PbTextStyles.Subheadline,
                color = PbColors.Text.Secondary,
                textAlign = TextAlign.Center,
            )
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(PbSpace.S8)) {
            PbButton(
                label = cancelLabel,
                onClick = onCancel,
                modifier = Modifier.weight(1f).partTag(testTag, "cancel"),
                style = PbButtonStyle.Secondary,
                size = PbButtonSize.Small,
            )
            PbButton(
                label = actionLabel,
                onClick = onAction,
                modifier = Modifier.weight(1f).partTag(testTag, "action"),
                style =
                    when (action) {
                        PbAlertAction.Destructive -> PbButtonStyle.Destructive
                        PbAlertAction.Primary -> PbButtonStyle.Primary
                    },
                size = PbButtonSize.Small,
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 340)
@Composable
private fun PbAlertCardPreview() {
    Column(Modifier.padding(PbSpace.S20), verticalArrangement = Arrangement.spacedBy(PbSpace.S24)) {
        PbAlertCard(
            "Discard changes?",
            "Your avatar edits won’t be saved.",
            cancelLabel = "Keep editing",
            actionLabel = "Discard",
            onCancel = {},
            onAction = {},
        )
        PbAlertCard(
            "Settle up with Rohan?",
            "This records ₹800 as paid.",
            cancelLabel = "Not now",
            actionLabel = "Settle up",
            onCancel = {},
            onAction = {},
            action = PbAlertAction.Primary,
        )
    }
}
