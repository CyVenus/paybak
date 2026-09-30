package app.paybak.paybak.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import app.paybak.paybak.R
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbSize

/**
 * `Button / Add` (`PBAddButton`): the black ＋ in the centre of the tab bar. Its plus is the one
 * icon drawn with a 2 dp stroke (`add-button-plus.svg`).
 */
@Composable
fun PbAddButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String = stringResource(R.string.pb_add),
    interactionSource: MutableInteractionSource? = null,
) {
    val press = rememberPressState(interactionSource)
    val fill =
        animatePressColor(
            if (press.isPressed) PbColors.Bg.InversePressed else PbColors.Bg.Inverse,
            label = "PbAddButton fill",
        )
    Box(
        modifier =
            modifier
                .size(PbSize.AddButton)
                .background(fill, CircleShape)
                .pressable(press, enabled = true, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.ic_add_button_plus),
            contentDescription = contentDescription,
            modifier = Modifier.size(PbSize.IconLg),
            colorFilter = ColorFilter.tint(PbColors.Icon.Inverse),
        )
    }
}

@Preview
@Composable
private fun PbAddButtonPreview() {
    PbAddButton(onClick = {})
}
