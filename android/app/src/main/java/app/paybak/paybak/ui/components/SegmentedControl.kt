package app.paybak.paybak.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbMotion
import app.paybak.paybak.ui.theme.PbShapes
import app.paybak.paybak.ui.theme.PbSize
import app.paybak.paybak.ui.theme.PbTextStyles

/**
 * `Control / Segment` (`PBSegment`): one 30 dp option of [PbSegmentedControl]. Selected is a black
 * pill with white text; unselected has no fill and grey text.
 */
@Composable
fun PbSegment(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val fill by
        animateColorAsState(
            targetValue = if (selected) PbColors.Bg.Inverse else Color.Transparent,
            animationSpec = tween(PbMotion.FADE_MILLIS),
            label = "PbSegment fill",
        )
    val text by
        animateColorAsState(
            targetValue = if (selected) PbColors.Text.Inverse else PbColors.Text.Secondary,
            animationSpec = tween(PbMotion.FADE_MILLIS),
            label = "PbSegment label",
        )
    Box(
        modifier =
            modifier
                .height(30.dp)
                .background(fill, PbShapes.Pill)
                .selectable(
                    selected = selected,
                    interactionSource = null,
                    indication = null,
                    role = Role.Tab,
                    onClick = onClick,
                ),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = label, style = PbTextStyles.ButtonSmall, color = text, maxLines = 1)
    }
}

/**
 * `Control / Segmented` (`PBSegmentedControl`): 2–4 equal segments on a #F5F5F5 pill, single
 * selection. Figma widths: 240 (2 options), 330 (3), 362 (4); pass the width through [modifier].
 *
 * @param segmentTags One UI-test tag per option, e.g. "activity.segment.timeline".
 */
@Composable
fun PbSegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    segmentTags: List<String>? = null,
) {
    Row(
        modifier =
            modifier
                .height(PbSize.ButtonSm)
                .background(PbColors.Bg.Card, PbShapes.Pill)
                .padding(3.dp)
                .selectableGroup()
    ) {
        options.forEachIndexed { index, option ->
            PbSegment(
                label = option,
                selected = index == selectedIndex,
                onClick = { onSelect(index) },
                modifier = Modifier.weight(1f).partTag(segmentTags?.getOrNull(index)),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PbSegmentedControlPreview() {
    PbSegmentedControl(
        listOf("Groups", "Friends"),
        selectedIndex = 0,
        onSelect = {},
        Modifier.width(240.dp),
    )
}
