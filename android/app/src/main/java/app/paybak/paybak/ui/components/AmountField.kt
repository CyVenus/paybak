package app.paybak.paybak.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.paybak.paybak.R
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles

private val HiddenSelection = TextSelectionColors(Color.Transparent, Color.Transparent)

/**
 * `Control / Amount Display` (`PBAmountField`): the amount-first entry of Add expense, Record
 * payment, Lend money and the receipt review. Two chips open the currency and date sheets; below
 * them the amount reads in Amount/Display, "₹0" in grey while empty, with a 2 × 56 caret while
 * focused. Typing goes through a hidden field with the system number pad, never a custom keypad.
 * Long amounts shrink (down to half size) instead of wrapping. Parts are tagged
 * "[testTag].currency", "[testTag].date" and "[testTag].amount".
 *
 * @param value The digits typed so far ("2800", or "12.5" with [allowDecimals]).
 * @param formatted [value] as it reads, with the currency symbol and grouping ("₹2,800").
 * @param placeholder Shown while [value] is empty: "₹0" in the chosen currency's symbol.
 * @param date The date chip's label ("Today"); null hides it.
 */
@Composable
fun PbAmountField(
    value: String,
    onValueChange: (String) -> Unit,
    formatted: String,
    placeholder: String,
    currency: String,
    onCurrencyClick: () -> Unit,
    modifier: Modifier = Modifier,
    date: String? = null,
    onDateClick: () -> Unit = {},
    helper: String? = null,
    allowDecimals: Boolean = false,
    interactionSource: MutableInteractionSource? = null,
    fieldModifier: Modifier = Modifier,
    testTag: String? = null,
) {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val focused by source.collectIsFocusedAsState()
    val description = stringResource(R.string.pb_amount)
    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = PbSpace.S4).partTag(testTag),
        verticalArrangement = Arrangement.spacedBy(PbSpace.S12),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(PbSpace.S8)) {
            PbCategoryChip(
                currency,
                Modifier.partTag(testTag, "currency"),
                onClick = onCurrencyClick,
            )
            if (date != null) {
                PbCategoryChip(date, Modifier.partTag(testTag, "date"), onClick = onDateClick)
            }
        }
        // The hidden field's own cursor and handles stay invisible; the display draws the caret.
        CompositionLocalProvider(LocalTextSelectionColors provides HiddenSelection) {
            BasicTextField(
                // The cursor stays at the end: amounts are typed and deleted from the right.
                value = TextFieldValue(value, TextRange(value.length)),
                onValueChange = { input ->
                    val next = amountInput(input.text, allowDecimals)
                    if (next != value) onValueChange(next)
                },
                modifier =
                    fieldModifier.fillMaxWidth().partTag(testTag, "amount").semantics {
                        contentDescription = description
                    },
                keyboardOptions =
                    KeyboardOptions(
                        keyboardType =
                            if (allowDecimals) KeyboardType.Decimal else KeyboardType.Number
                    ),
                singleLine = true,
                interactionSource = source,
                cursorBrush = SolidColor(Color.Transparent),
                decorationBox = { innerTextField ->
                    AmountDisplay(
                        text = if (value.isEmpty()) placeholder else formatted,
                        empty = value.isEmpty(),
                        focused = focused,
                        caretKey = value,
                        helper = helper,
                    )
                    Box(Modifier.size(1.dp).alpha(0f)) { innerTextField() }
                },
            )
        }
    }
}

@Composable
private fun AmountDisplay(
    text: String,
    empty: Boolean,
    focused: Boolean,
    caretKey: String,
    helper: String?,
) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(PbSpace.S2, Alignment.CenterHorizontally),
        ) {
            BasicText(
                text = text,
                modifier = Modifier.weight(1f, fill = false),
                style =
                    PbTextStyles.AmountDisplay.copy(
                        color = if (empty) PbColors.Text.Tertiary else PbColors.Text.Primary,
                        textAlign = TextAlign.Center,
                    ),
                maxLines = 1,
                autoSize = TextAutoSize.StepBased(minFontSize = 28.sp, maxFontSize = 56.sp),
            )
            if (focused) PbCaret(height = 56.dp, Modifier.padding(top = PbSpace.S4), caretKey)
        }
        if (helper != null) {
            Text(
                text = helper,
                style = PbTextStyles.Footnote,
                color = PbColors.Text.Secondary,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/**
 * What the amount field keeps of [text]: digits, plus one decimal point with up to two decimals
 * when [allowDecimals]. Leading zeros go ("05" is "5"; "0.5" stays).
 */
internal fun amountInput(text: String, allowDecimals: Boolean): String {
    val kept = text.filter { it.isDigit() || (allowDecimals && it == '.') }
    val point = kept.indexOf('.')
    val whole = (if (point < 0) kept else kept.substring(0, point)).trimStart('0')
    if (point < 0) return whole
    val decimals = kept.substring(point + 1).filter(Char::isDigit).take(2)
    return whole.ifEmpty { "0" } + "." + decimals
}

@Preview(showBackground = true, widthDp = 402)
@Composable
private fun PbAmountFieldPreview() {
    Column(verticalArrangement = Arrangement.spacedBy(PbSpace.S24)) {
        PbAmountField("", {}, "", "₹0", "INR", onCurrencyClick = {}, date = "Today")
        PbAmountField(
            "2800",
            {},
            "₹2,800",
            "₹0",
            "INR",
            onCurrencyClick = {},
            helper = "You owe Meera ₹450 in Flat 302",
        )
    }
}
