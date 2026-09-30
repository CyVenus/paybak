package app.paybak.paybak.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbSize
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles

/**
 * `Row / Receipt Line` (`PBReceiptLineRow`): one line of a scanned receipt, 44 dp inside a
 * [PbCard]: the label and the amount. [total] is the Headline total line with a divider above it.
 * Tapping the amount calls [onEdit]; while an [editor] is given, the amount becomes a focused
 * inline field so a misread value can be fixed in place.
 */
@Composable
fun PbReceiptLineRow(
    label: String,
    amount: String,
    modifier: Modifier = Modifier,
    total: Boolean = false,
    editor: PbAmountEditor? = null,
    onEdit: (() -> Unit)? = null,
) {
    val style = if (total) PbTextStyles.Headline else PbTextStyles.Body
    Box(modifier.fillMaxWidth().height(PbSize.Tap)) {
        Row(
            modifier = Modifier.fillMaxWidth().fillMaxHeight().padding(horizontal = PbSpace.S16),
            horizontalArrangement = Arrangement.spacedBy(PbSpace.S12),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                modifier = Modifier.weight(1f),
                style = style,
                color = PbColors.Text.Primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (editor != null) {
                InlineAmountField(editor, style, autoFocus = true)
            } else {
                val press = rememberPressState(interactionSource = null)
                val edit =
                    if (onEdit == null) Modifier
                    else Modifier.pressable(press, true, onClick = onEdit)
                Box(
                    Modifier.fillMaxHeight().alpha(if (press.isPressed) 0.5f else 1f).then(edit),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(amount, style = style, color = PbColors.Text.Primary, maxLines = 1)
                }
            }
        }
        if (total) {
            PbDivider(Modifier.align(Alignment.TopStart).padding(horizontal = PbSpace.S16))
        }
    }
}

@Preview(showBackground = true, widthDp = 362)
@Composable
private fun PbReceiptLineRowPreview() {
    PbCard {
        PbReceiptLineRow("Chicken biryani", "₹430")
        PbReceiptLineRow("Paneer tikka", "₹370", editor = PbAmountEditor("370", {}, "₹"))
        PbReceiptLineRow("Total", "₹2,300", total = true)
    }
}
