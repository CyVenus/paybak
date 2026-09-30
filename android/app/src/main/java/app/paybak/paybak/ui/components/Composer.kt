package app.paybak.paybak.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.paybak.paybak.R
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbLayout
import app.paybak.paybak.ui.theme.PbShapes
import app.paybak.paybak.ui.theme.PbSize
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles

/**
 * `Control / Composer` (`PBComposer`): the one-line input for expense comments and Ask Paybak, a 52
 * dp #F5F5F5 field (radius 14). Empty shows the placeholder and the optional mic; typing swaps the
 * mic for the black 36 dp send button. Send (or the keyboard's send key) calls [onSend]; the screen
 * then clears [value].
 *
 * @param pinned The keyboard bar: full width, white, a hairline on top, 8/20 dp padding, riding the
 *   keyboard (or the navigation bar when it's hidden). Otherwise the bare field.
 * @param onMic Shows the mic while empty: speech-to-text into the field, never a send.
 */
@Composable
fun PbComposer(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    pinned: Boolean = false,
    onMic: (() -> Unit)? = null,
    interactionSource: MutableInteractionSource? = null,
    fieldModifier: Modifier = Modifier,
) {
    if (!pinned) {
        ComposerField(
            value,
            onValueChange,
            onSend,
            placeholder,
            modifier,
            onMic,
            interactionSource,
            fieldModifier,
        )
        return
    }
    Column(
        modifier
            .fillMaxWidth()
            .background(PbColors.Bg.Primary)
            .windowInsetsPadding(
                WindowInsets.ime.union(WindowInsets.navigationBars).only(WindowInsetsSides.Bottom)
            )
    ) {
        PbDivider()
        ComposerField(
            value,
            onValueChange,
            onSend,
            placeholder,
            Modifier.padding(
                start = PbLayout.ScreenMargin,
                end = PbLayout.ScreenMargin,
                top = PbSpace.S8 - PbSize.Hairline,
                bottom = PbSpace.S8,
            ),
            onMic,
            interactionSource,
            fieldModifier,
        )
    }
}

@Composable
private fun ComposerField(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    onMic: (() -> Unit)? = null,
    interactionSource: MutableInteractionSource? = null,
    fieldModifier: Modifier = Modifier,
) {
    val typing = value.isNotEmpty()
    val text = rememberEndCursorText(value, onValueChange)
    BasicTextField(
        value = text.value,
        onValueChange = text.onValueChange,
        modifier = modifier.then(fieldModifier).fillMaxWidth(),
        textStyle = PbTextStyles.Body.copy(color = PbColors.Text.Primary),
        keyboardOptions =
            KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Send,
            ),
        keyboardActions = KeyboardActions(onSend = { if (value.isNotBlank()) onSend() }),
        singleLine = true,
        interactionSource = interactionSource,
        cursorBrush = SolidColor(PbColors.Text.Primary),
        decorationBox = { innerTextField ->
            Row(
                modifier =
                    Modifier.fillMaxWidth()
                        .height(PbSize.ButtonLg)
                        .background(PbColors.Bg.Card, PbShapes.Input)
                        .padding(
                            start = PbSpace.S16,
                            end = if (typing) PbSpace.S8 else PbSpace.S16,
                        ),
                horizontalArrangement = Arrangement.spacedBy(PbSpace.S8),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    if (!typing) {
                        Text(
                            text = placeholder,
                            style = PbTextStyles.Body,
                            color = PbColors.Text.Tertiary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    innerTextField()
                }
                when {
                    typing ->
                        PbIconButton(
                            PbIcon.ArrowUp,
                            contentDescription = stringResource(R.string.pb_send),
                            onClick = { if (value.isNotBlank()) onSend() },
                            style = PbIconButtonStyle.Inverse,
                            diameter = PbSize.ButtonSm,
                        )
                    onMic != null ->
                        SmallIconButton(
                            PbIcon.Mic,
                            contentDescription = stringResource(R.string.pb_dictate),
                            onClick = onMic,
                            size = PbSize.IconLg,
                        )
                }
            }
        },
    )
}

@Preview(showBackground = true, widthDp = 402)
@Composable
private fun PbComposerPreview() {
    Column(verticalArrangement = Arrangement.spacedBy(PbSpace.S16)) {
        PbComposer(
            "",
            {},
            onSend = {},
            "Add a comment",
            Modifier.padding(horizontal = 20.dp),
            onMic = {},
        )
        PbComposer("Add a comment", {}, onSend = {}, "Add a comment", pinned = true)
    }
}
