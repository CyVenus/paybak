package app.paybak.paybak.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbMotion
import app.paybak.paybak.ui.theme.PbShapes
import app.paybak.paybak.ui.theme.PbSize
import app.paybak.paybak.ui.theme.PbSpace

/**
 * A number edited in place: a split person's amount, percent or shares, or a misread receipt line.
 * The field shows [prefix] + [value] + [suffix] ("₹700", "25%") and edits only the digits.
 *
 * @param onDone Called when editing ends: the keyboard's Done, or focus moving elsewhere.
 */
data class PbAmountEditor(
    val value: String,
    val onValueChange: (String) -> Unit,
    val prefix: String = "",
    val suffix: String = "",
    val onDone: () -> Unit = {},
)

/**
 * The inline white field of `Row / Split Person` and `Row / Receipt Line` (Editing): 36 dp tall, at
 * least [minWidth] and growing with the text, radius 14, with the 1.5 dp black ring and the caret
 * while focused. Its content sits at the end, or in the centre with [centered] (shares).
 */
@Composable
internal fun InlineAmountField(
    editor: PbAmountEditor,
    textStyle: TextStyle,
    modifier: Modifier = Modifier,
    minWidth: Dp = 96.dp,
    centered: Boolean = false,
    enabled: Boolean = true,
    autoFocus: Boolean = false,
) {
    val source = remember { MutableInteractionSource() }
    val focused by source.collectIsFocusedAsState()
    val ring by
        animateColorAsState(
            targetValue = if (focused) PbColors.Border.Strong else Color.Transparent,
            animationSpec = tween(PbMotion.FADE_MILLIS),
            label = "Inline field ring",
        )
    val focus = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val onDone by rememberUpdatedState(editor.onDone)
    var hadFocus by remember { mutableStateOf(false) }
    val text = rememberEndCursorText(editor.value, editor.onValueChange)
    val affixes = remember(editor.prefix, editor.suffix) { Affixes(editor.prefix, editor.suffix) }
    if (autoFocus) LaunchedEffect(Unit) { focus.requestFocus() }
    // A text field takes all the width it's offered, so size it to its text (and the caret); the
    // box
    // then aligns it and keeps the field's minimum width.
    val measurer = rememberTextMeasurer()
    val shown = editor.prefix + editor.value + editor.suffix
    val textWidth =
        with(LocalDensity.current) { measurer.measure(shown, textStyle).size.width.toDp() } +
            CaretWidth

    Box(
        modifier =
            modifier
                .height(PbSize.ButtonSm)
                .widthIn(min = minWidth)
                .clip(PbShapes.Input)
                .background(PbColors.Bg.Primary)
                .border(FieldRingWidth, ring, PbShapes.Input)
                .clickable(enabled = enabled, interactionSource = null, indication = null) {
                    focus.requestFocus()
                }
                .padding(horizontal = PbSpace.S12),
        contentAlignment = if (centered) Alignment.Center else Alignment.CenterEnd,
    ) {
        BasicTextField(
            value = text.value,
            onValueChange = { edited ->
                if (edited.text.all { it.isDigit() || it == '.' }) text.onValueChange(edited)
            },
            modifier =
                Modifier.width(textWidth).focusRequester(focus).onFocusChanged { state ->
                    if (hadFocus && !state.isFocused) onDone()
                    hadFocus = state.isFocused
                },
            enabled = enabled,
            textStyle =
                textStyle.copy(
                    color = if (enabled) PbColors.Text.Primary else PbColors.Text.Tertiary
                ),
            keyboardOptions =
                KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            singleLine = true,
            visualTransformation = affixes,
            interactionSource = source,
            cursorBrush = SolidColor(PbColors.Text.Primary),
        )
    }
}

/** Shows a prefix and suffix around the typed digits; the cursor never enters them. */
private data class Affixes(val prefix: String, val suffix: String) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val shown = AnnotatedString(prefix + text.text + suffix)
        val length = text.length
        return TransformedText(
            shown,
            object : OffsetMapping {
                // The end of the digits maps past the suffix, so the caret follows "25%".
                override fun originalToTransformed(offset: Int): Int =
                    if (offset >= length) shown.length else prefix.length + offset

                override fun transformedToOriginal(offset: Int): Int =
                    (offset - prefix.length).coerceIn(0, length)
            },
        )
    }
}
