package app.paybak.paybak.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbMotion
import app.paybak.paybak.ui.theme.PbShapes
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles

/**
 * `Control / Text Area` (`PBTextArea`): multi-line text in the input-field style (#F5F5F5, radius
 * 14, padding 16), at least 104 dp tall and growing with the text: the Remind message, the Not
 * received note. Focused draws the black ring over the field without moving the text.
 *
 * @param fieldModifier Applied to the editable field: its test tag or a focus requester.
 */
@Composable
fun PbTextArea(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String = "",
    helper: String? = null,
    keyboardOptions: KeyboardOptions =
        KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
    interactionSource: MutableInteractionSource? = null,
    fieldModifier: Modifier = Modifier,
) {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val focused by source.collectIsFocusedAsState()
    val ring by
        animateColorAsState(
            targetValue = if (focused) PbColors.Border.Strong else Color.Transparent,
            animationSpec = tween(PbMotion.FADE_MILLIS),
            label = "PbTextArea ring",
        )
    val text = rememberEndCursorText(value, onValueChange)
    Column(modifier, verticalArrangement = Arrangement.spacedBy(PbSpace.S8)) {
        if (label != null) {
            Text(label, style = PbTextStyles.Subheadline, color = PbColors.Text.Secondary)
        }
        BasicTextField(
            value = text.value,
            onValueChange = text.onValueChange,
            modifier = fieldModifier.fillMaxWidth(),
            textStyle = PbTextStyles.Body.copy(color = PbColors.Text.Primary),
            keyboardOptions = keyboardOptions,
            interactionSource = source,
            cursorBrush = SolidColor(PbColors.Text.Primary),
            decorationBox = { innerTextField ->
                Box(
                    Modifier.fillMaxWidth()
                        .heightIn(min = 104.dp)
                        .background(PbColors.Bg.Card, PbShapes.Input)
                        .border(FieldRingWidth, ring, PbShapes.Input)
                        .padding(PbSpace.S16)
                ) {
                    if (value.isEmpty()) {
                        Text(placeholder, style = PbTextStyles.Body, color = PbColors.Text.Tertiary)
                    }
                    innerTextField()
                }
            },
        )
        if (helper != null) {
            Text(helper, style = PbTextStyles.Footnote, color = PbColors.Text.Tertiary)
        }
    }
}

@Preview(showBackground = true, widthDp = 362)
@Composable
private fun PbTextAreaPreview() {
    PbTextArea(
        value =
            "Hi Rohan! Just a gentle reminder about ₹800 for the movie tickets on 20 Sep. You " +
                "can pay me on UPI at arjun@okaxis. Thanks.",
        onValueChange = {},
        label = "Message",
        helper = "You can edit this message.",
    )
}
