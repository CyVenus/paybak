package app.paybak.paybak.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbSize

/** `Inset` of `Divider / Line`. Leading lines up with row text after a 40 dp avatar + 12 gap. */
enum class PbDividerInset {
    None,
    Leading,
}

/** `Divider / Line` (`PBDivider`): a 1 dp `border/subtle` hairline. Use sparingly. */
@Composable
fun PbDivider(modifier: Modifier = Modifier, inset: PbDividerInset = PbDividerInset.None) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(start = if (inset == PbDividerInset.Leading) 52.dp else 0.dp)
                .height(PbSize.Hairline)
                .background(PbColors.Border.Subtle)
    )
}

@Preview(showBackground = true)
@Composable
private fun PbDividerPreview() {
    PbDivider(inset = PbDividerInset.Leading)
}
