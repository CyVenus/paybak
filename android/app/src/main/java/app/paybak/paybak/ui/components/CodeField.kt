package app.paybak.paybak.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceAtMost
import androidx.compose.ui.unit.dp
import app.paybak.paybak.R
import app.paybak.paybak.ui.theme.LocalReduceMotion
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbMotion
import app.paybak.paybak.ui.theme.PbShapes
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles
import kotlinx.coroutines.delay

/** Number of boxes in the verification code. */
const val CodeLength = 6

private val DigitWidth = 48.dp
private val DigitHeight = 56.dp
private val MinDigitGap = PbSpace.S12

/** `State` of `Control / Code Digit`. */
enum class PbCodeDigitState {
    Empty,
    Focused,
    Filled,
    Error,
}

/**
 * `Control / Code Digit` (`PBCodeDigit`): one 48 × 56 box of the code. Focused shows the black ring
 * and a 2 × 24 caret; Error keeps the digit black and only rings the box in red.
 */
@Composable
fun PbCodeDigit(
    digit: Char?,
    state: PbCodeDigitState,
    modifier: Modifier = Modifier,
    width: Dp = DigitWidth,
    caretVisible: Boolean = true,
) {
    val ring by
        animateColorAsState(
            targetValue =
                when (state) {
                    PbCodeDigitState.Focused -> PbColors.Border.Strong
                    PbCodeDigitState.Error -> PbColors.Border.Destructive
                    else -> Color.Transparent
                },
            animationSpec = tween(PbMotion.FADE_MILLIS),
            label = "PbCodeDigit ring",
        )
    Box(
        modifier =
            modifier
                .size(width, DigitHeight)
                .background(PbColors.Bg.Card, PbShapes.Input)
                .border(FieldRingWidth, ring, PbShapes.Input),
        contentAlignment = Alignment.Center,
    ) {
        when (state) {
            PbCodeDigitState.Empty -> Unit
            PbCodeDigitState.Focused ->
                Box(
                    Modifier.size(2.dp, 24.dp)
                        .alpha(if (caretVisible) 1f else 0f)
                        .background(PbColors.Bg.Inverse)
                )

            PbCodeDigitState.Filled,
            PbCodeDigitState.Error ->
                Text(
                    text = digit?.toString().orEmpty(),
                    style = PbTextStyles.Title2,
                    color = PbColors.Text.Primary,
                )
        }
    }
}

/**
 * `Control / Code Input` (`PBCodeField`): six [PbCodeDigit]s backed by one hidden text field, so
 * the number pad, paste and SMS one-time-code autofill all work. It fills the width with the boxes
 * spread out (14.8 dp gaps on a 362 dp column) and narrows the boxes on small screens so the gap
 * never drops below 12 dp. Focus it with `Modifier.focusRequester(...)`.
 *
 * @param code Digits entered so far (0–6).
 * @param onCodeChange Receives digits only, at most [CodeLength].
 * @param isError Rings every box in red (the wrong-code state).
 */
@Composable
fun PbCodeField(
    code: String,
    onCodeChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    interactionSource: MutableInteractionSource? = null,
) {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val focused by source.collectIsFocusedAsState()
    val focusedIndex = if (focused && !isError && code.length < CodeLength) code.length else null
    val caretVisible = rememberCaretBlink(focusedIndex)
    val description = stringResource(R.string.pb_code_field)

    // The hidden field's own cursor and handles stay invisible; the boxes draw the caret.
    CompositionLocalProvider(LocalTextSelectionColors provides HiddenSelection) {
        BasicTextField(
            value = code,
            onValueChange = { input -> onCodeChange(input.filter(Char::isDigit).take(CodeLength)) },
            modifier =
                modifier.fillMaxWidth().semantics {
                    contentType = ContentType.SmsOtpCode
                    contentDescription = description
                },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            singleLine = true,
            interactionSource = source,
            cursorBrush = SolidColor(Color.Transparent),
            decorationBox = { innerTextField ->
                BoxWithConstraints {
                    val digitWidth =
                        ((maxWidth - MinDigitGap * (CodeLength - 1)) / CodeLength).coerceAtMost(
                            DigitWidth
                        )
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        repeat(CodeLength) { index ->
                            PbCodeDigit(
                                digit = code.getOrNull(index),
                                state =
                                    when {
                                        isError -> PbCodeDigitState.Error
                                        index == focusedIndex -> PbCodeDigitState.Focused
                                        index < code.length -> PbCodeDigitState.Filled
                                        else -> PbCodeDigitState.Empty
                                    },
                                width = digitWidth,
                                caretVisible = caretVisible,
                            )
                        }
                    }
                    Box(Modifier.size(1.dp).alpha(0f)) { innerTextField() }
                }
            },
        )
    }
}

private val HiddenSelection = TextSelectionColors(Color.Transparent, Color.Transparent)

/**
 * Blinks every 0.5 s, restarting (visible) whenever the caret moves; steady under reduce motion.
 */
@Composable
private fun rememberCaretBlink(focusedIndex: Int?): Boolean {
    val reduceMotion = LocalReduceMotion.current
    var visible by remember { mutableStateOf(true) }
    LaunchedEffect(focusedIndex, reduceMotion) {
        visible = true
        if (focusedIndex == null || reduceMotion) return@LaunchedEffect
        while (true) {
            delay(PbMotion.CARET_BLINK_MILLIS.toLong())
            visible = !visible
        }
    }
    return visible
}

@Preview(showBackground = true, widthDp = 402)
@Composable
private fun PbCodeFieldPreview() {
    Box(Modifier.size(402.dp, 96.dp), contentAlignment = Alignment.Center) {
        PbCodeField(
            code = "482917",
            onCodeChange = {},
            Modifier.size(362.dp, 56.dp),
            isError = true,
        )
    }
}
