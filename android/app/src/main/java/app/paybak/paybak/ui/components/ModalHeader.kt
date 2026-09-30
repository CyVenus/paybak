package app.paybak.paybak.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.paybak.paybak.ui.theme.PbSize
import app.paybak.paybak.ui.theme.PbSpace

/**
 * `Navigation / Modal Header` (`PBModalHeader`): the 44 dp toolbar of a full-screen modal (Add
 * expense, Record payment, New group, Ask Paybak, the paywall): the glass ✕ on the left, a centred
 * Headline title and a black Small pill on the right ("Save", "Create", "Add"), disabled until the
 * form is valid. No fill. Parts are tagged "[testTag].close" and "[testTag].[actionTag]".
 *
 * @param action The pill's label; null for no pill (Ask Paybak, the paywall).
 * @param actionTag The pill's part tag, e.g. "save" or "create".
 */
@Composable
fun PbModalHeader(
    title: String?,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    action: String? = null,
    actionEnabled: Boolean = true,
    onAction: () -> Unit = {},
    testTag: String? = null,
    actionTag: String = "action",
) {
    Box(modifier.fillMaxWidth().height(PbSize.Tap).partTag(testTag)) {
        PbGlassCloseButton(
            onClick = onClose,
            modifier = Modifier.align(Alignment.CenterStart).partTag(testTag, "close"),
            diameter = PbSize.Tap,
        )
        if (title != null) HeaderTitle(title, width = 200.dp, Modifier.align(Alignment.Center))
        if (action != null) {
            PbButton(
                label = action,
                onClick = onAction,
                modifier = Modifier.align(Alignment.CenterEnd).partTag(testTag, actionTag),
                size = PbButtonSize.Small,
                enabled = actionEnabled,
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 402)
@Composable
private fun PbModalHeaderPreview() {
    Column(Modifier.padding(PbSpace.S20), verticalArrangement = Arrangement.spacedBy(PbSpace.S24)) {
        PbModalHeader("Add expense", onClose = {}, action = "Save")
        PbModalHeader("Add expense", onClose = {}, action = "Save", actionEnabled = false)
        PbModalHeader("Ask Paybak", onClose = {})
    }
}
