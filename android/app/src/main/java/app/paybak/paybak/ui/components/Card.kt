package app.paybak.paybak.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbShapes

/**
 * A #F5F5F5 radius-20 card (`bg/card`, `radius/card`) that stacks rows drawn for cards with no gap:
 * settings groups, split people, transfers, compact person rows. It clips, so a row's pressed fill
 * follows the rounded corners.
 */
@Composable
fun PbCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier.fillMaxWidth().clip(PbShapes.Card).background(PbColors.Bg.Card),
        content = content,
    )
}
