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
import app.paybak.paybak.domain.format.Money
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbMotion
import app.paybak.paybak.ui.theme.PbShapes
import app.paybak.paybak.ui.theme.PbSize
import app.paybak.paybak.ui.theme.PbSpace

/**
 * A number edited in place: a split person's amount, percent or shares, or a misread receipt line.
 * The field shows [prefix] + [value] + [suffix] ("₹700", "25%") and edits only the digits.
 *
 * @param currency Groups the whole part as amounts in that currency show it ("₹2,800"); null for
 *   percents and shares.
 * @param onDone Called when editing ends: the keyboard's Done, or focus moving elsewhere.
 * @param decimal Amounts with decimals get the decimal pad; counts and percents the number pad.
 * @param focusRequester Lets the screen focus the field (the next row, a start state).
 */
data class PbAmountEditor(
    val value: String,
    val onValueChange: (String) -> Unit,
    val prefix: String = "",
    val suffix: String = "",
    val currency: String? = null,
    val onDone: () -> Unit = {},
    val decimal: Boolean = false,
    val focusRequester: FocusRequester? = null,
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
    val ownFocus = remember { FocusRequester() }
    val focus = editor.focusRequester ?: ownFocus
    val focusManager = LocalFocusManager.current
    val onDone by rememberUpdatedState(editor.onDone)
    var hadFocus by remember { mutableStateOf(false) }
    val text = rememberEndCursorText(editor.value, editor.onValueChange)
    val display =
        remember(editor.prefix, editor.suffix, editor.currency) {
            AmountDisplay(editor.prefix, editor.suffix, editor.currency)
        }
    if (autoFocus) LaunchedEffect(Unit) { focus.requestFocus() }
    // A text field takes all the width it's offered, so size it to its text (and the caret); the
    // box
    // then aligns it and keeps the field's minimum width.
    val measurer = rememberTextMeasurer()
    val shown = display.filter(AnnotatedString(editor.value)).text.text
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
                KeyboardOptions(
                    keyboardType =
                        if (editor.decimal) KeyboardType.Decimal else KeyboardType.Number,
                    imeAction = ImeAction.Done,
                ),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            singleLine = true,
            visualTransformation = display,
            interactionSource = source,
            cursorBrush = SolidColor(PbColors.Text.Primary),
        )
    }
}

/**
 * Shows a prefix and suffix around the typed digits, grouping the whole part when there's a
 * [currency] ("2800" shows "₹2,800"); the cursor never enters the prefix, suffix or separators.
 */
private data class AmountDisplay(
    val prefix: String,
    val suffix: String,
    val currency: String?,
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val typed = text.text
        val whole = typed.substringBefore('.')
        val grouped =
            whole
                .takeIf { currency != null && it.isNotEmpty() && !it.startsWith('0') }
                ?.toLongOrNull()
                ?.let { Money.groupDigits(it, currency!!) } ?: whole
        val body = grouped + typed.substring(whole.length)
        val shown = AnnotatedString(prefix + body + suffix)
        // Where each typed character sits in [body]: separators push the later digits along.
        val positions = IntArray(typed.length + 1)
        var at = 0
        for (index in whole.indices) {
            while (grouped[at] == ',') at++
            positions[index] = at++
        }
        for (index in whole.length..typed.length) {
            positions[index] = grouped.length + index - whole.length
        }
        return TransformedText(
            shown,
            object : OffsetMapping {
                // The end of the digits maps past the suffix, so the caret follows "25%".
                override fun originalToTransformed(offset: Int): Int =
                    if (offset >= typed.length) shown.length else prefix.length + positions[offset]

                override fun transformedToOriginal(offset: Int): Int =
                    positions.indexOfLast { prefix.length + it <= offset }.coerceIn(0, typed.length)
            },
        )
    }
}
