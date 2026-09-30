package app.paybak.paybak.feature.signin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import app.paybak.paybak.R
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbSize
import app.paybak.paybak.ui.theme.PbTextStyles
import kotlin.math.roundToInt

/** Extra touch area either side of "Change". */
private val ChangeSlop = 8.dp

/**
 * "Sent to {contact} · Change" (Body): the sentence in secondary text, then "Change" in primary,
 * underlined. "Change" gets a 44 dp tall hit area laid over the word, so the text's layout stays
 * exactly as designed; pressing fades the word to 50 %. The line wraps for a long contact, with
 * "Change" staying at its end.
 */
@Composable
internal fun SentToLine(contact: String, onChange: () -> Unit, modifier: Modifier = Modifier) {
    val sentence = stringResource(R.string.verify_sent_to, contact)
    val change = stringResource(R.string.verify_change)
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val text = buildAnnotatedString {
        withStyle(SpanStyle(color = PbColors.Text.Secondary)) { append(sentence) }
        withStyle(
            SpanStyle(
                color = PbColors.Text.Primary.copy(alpha = if (pressed) 0.5f else 1f),
                textDecoration = TextDecoration.Underline,
            )
        ) {
            append(change)
        }
    }
    var layout by remember { mutableStateOf<TextLayoutResult?>(null) }

    Box(modifier.fillMaxWidth()) {
        Text(text = text, style = PbTextStyles.Body, onTextLayout = { layout = it })
        layout?.let { result ->
            val word =
                result
                    .getBoundingBox(sentence.length)
                    .spanning(result.getBoundingBox(sentence.length + change.length - 1))
            ChangeButton(word, change, interaction, onChange)
        }
    }
}

/**
 * An invisible button over [word] (px), at least 44 dp tall and centred on it. It takes no space in
 * the layout, so the text line keeps its own height.
 */
@Composable
private fun ChangeButton(
    word: Rect,
    label: String,
    interaction: MutableInteractionSource,
    onClick: () -> Unit,
) {
    val density = LocalDensity.current
    val slop = with(density) { ChangeSlop.toPx() }
    val height = maxOf(with(density) { PbSize.Tap.toPx() }, word.height)
    val target =
        Rect(
            left = word.left - slop,
            top = word.center.y - height / 2,
            right = word.right + slop,
            bottom = word.center.y + height / 2,
        )
    Box(
        Modifier.layout { measurable, _ ->
                val placeable =
                    measurable.measure(
                        Constraints.fixed(target.width.roundToInt(), target.height.roundToInt())
                    )
                layout(0, 0) { placeable.place(target.left.roundToInt(), target.top.roundToInt()) }
            }
            .testTag("verify.change")
            .clickable(
                interactionSource = interaction,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            )
            .semantics { contentDescription = label }
    )
}

/** The smallest rectangle holding both. */
private fun Rect.spanning(other: Rect) =
    Rect(
        left = minOf(left, other.left),
        top = minOf(top, other.top),
        right = maxOf(right, other.right),
        bottom = maxOf(bottom, other.bottom),
    )
