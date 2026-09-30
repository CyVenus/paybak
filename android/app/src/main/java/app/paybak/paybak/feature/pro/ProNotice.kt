package app.paybak.paybak.feature.pro

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import app.paybak.paybak.R
import app.paybak.paybak.ui.components.PbButton
import app.paybak.paybak.ui.components.PbButtonSize
import app.paybak.paybak.ui.components.PbButtonStyle
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbMaterial
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles

private val CardShape = RoundedCornerShape(34.dp)

/**
 * A one-button notice in the `Overlay / Alert` card ("No purchases to restore." · OK): the
 * native single-action alert the spec asks for, drawn in the kit's style.
 */
@Composable
internal fun ProNotice(message: String, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        val window = (LocalView.current.parent as? DialogWindowProvider)?.window
        SideEffect { window?.setDimAmount(PbColors.Bg.Scrim.alpha) }
        Column(
            Modifier.width(300.dp)
                .testTag("paywall.notice")
                .dropShadow(CardShape, PbMaterial.Glass.shadow)
                .background(PbColors.Bg.Primary, CardShape)
                .padding(PbSpace.S20),
            verticalArrangement = Arrangement.spacedBy(PbSpace.S20),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                message,
                style = PbTextStyles.Headline,
                color = PbColors.Text.Primary,
                textAlign = TextAlign.Center,
            )
            PbButton(
                stringResource(R.string.settings_ok),
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                style = PbButtonStyle.Secondary,
                size = PbButtonSize.Small,
            )
        }
    }
}

@Preview
@Composable
private fun ProNoticePreview() {
    ProNotice("No purchases to restore.", onDismiss = {})
}
