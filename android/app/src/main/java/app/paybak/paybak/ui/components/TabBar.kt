package app.paybak.paybak.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.icons.PbIconImage
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbMaterial
import app.paybak.paybak.ui.theme.PbMotion
import app.paybak.paybak.ui.theme.PbShapes
import app.paybak.paybak.ui.theme.PbSize
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles
import app.paybak.paybak.ui.theme.pbMaterial

/** One tab of [PbTabBar]: its label, icon and UI-test tag. */
data class PbTabItemSpec(val label: String, val icon: PbIcon, val testTag: String)

private val TabItemWidth = 68.dp
private val TabItemHeight = 52.dp

/**
 * `Navigation / Tab Bar Item` (`PBTabItem`): a 68 × 52 capsule with the icon over a Caption/2
 * label. Active is black on the 6 % pill; inactive is grey. Pressing dims it slightly.
 */
@Composable
fun PbTabItem(
    label: String,
    icon: PbIcon,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val press = rememberPressState(null)
    val fill by
        animateColorAsState(
            if (selected) PbColors.Bg.Selected else Color.Transparent,
            tween(PbMotion.FADE_MILLIS),
            label = "PbTabItem fill",
        )
    val tint = if (selected) PbColors.Icon.Primary else PbColors.Icon.Secondary
    Column(
        modifier =
            modifier
                .size(TabItemWidth, TabItemHeight)
                .background(fill, PbShapes.Pill)
                .selectable(
                    selected = selected,
                    interactionSource = press.source,
                    indication = null,
                    role = Role.Tab,
                    onClick = onClick,
                )
                .alpha(if (press.isPressed) PRESSED_ALPHA else 1f),
        verticalArrangement = Arrangement.spacedBy(PbSpace.S2, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        PbIconImage(icon, contentDescription = null, tint = tint)
        Text(
            text = label,
            style = PbTextStyles.Caption2,
            color = if (selected) PbColors.Text.Primary else PbColors.Text.Secondary,
            maxLines = 1,
        )
    }
}

private const val PRESSED_ALPHA = 0.6f

/**
 * `Navigation / Tab Bar` (`PBTabBar`): the floating glass capsule with two tabs, the black ＋ and
 * two more tabs, spread evenly. Place it 20 dp from the sides and 21 dp above the bottom.
 *
 * @param items The four tabs, in order; the ＋ sits between the second and third.
 */
@Composable
fun PbTabBar(
    items: List<PbTabItemSpec>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
    addTestTag: String? = null,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .height(PbSize.TabBar)
                .pbMaterial(PbMaterial.Glass, PbShapes.Pill)
                .padding(horizontal = TabBarPaddingX, vertical = TabBarPaddingY)
                .selectableGroup(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items.forEachIndexed { index, item ->
            if (index == items.size / 2) {
                Box(if (addTestTag != null) Modifier.testTag(addTestTag) else Modifier) {
                    PbAddButton(onClick = onAdd)
                }
            }
            PbTabItem(
                label = item.label,
                icon = item.icon,
                selected = index == selectedIndex,
                onClick = { onSelect(index) },
                modifier = Modifier.testTag(item.testTag),
            )
        }
    }
}

/** The items sit 6 dp from the ends (5 dp padding + the 1 dp stroke) and 5 dp from the edges. */
private val TabBarPaddingX = 6.dp
private val TabBarPaddingY = 5.dp

@Preview(showBackground = true, widthDp = 402, backgroundColor = 0xFFF5F5F5)
@Composable
private fun PbTabBarPreview() {
    PbTabBar(
        items =
            listOf(
                PbTabItemSpec("Home", PbIcon.Home, "home"),
                PbTabItemSpec("Groups", PbIcon.Groups, "groups"),
                PbTabItemSpec("Activity", PbIcon.Activity, "activity"),
                PbTabItemSpec("Profile", PbIcon.Profile, "profile"),
            ),
        selectedIndex = 0,
        onSelect = {},
        onAdd = {},
        modifier = Modifier.padding(20.dp),
    )
}
