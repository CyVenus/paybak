package app.paybak.paybak.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.paybak.paybak.ui.components.PbTabBar
import app.paybak.paybak.ui.components.PbTabItemSpec
import app.paybak.paybak.ui.theme.PbLayout
import app.paybak.paybak.ui.theme.PbSize

/** Figma places the tab bar 21 dp above the bottom edge (13 dp into the home-indicator area). */
private val TabBarBottom = 21.dp
private val TabBarIntoInset = 13.dp
private val TabBarContentGap = 24.dp

/**
 * The tab bar's distance from the bottom edge: Figma's 21 dp, or more over tall navigation bars.
 */
@Composable
fun tabBarBottomOffset(): Dp {
    val inset = with(LocalDensity.current) { WindowInsets.navigationBars.getBottom(this).toDp() }
    return maxOf(TabBarBottom, inset - TabBarIntoInset)
}

/**
 * The tab shell (app-architecture §2.1): the selected tab's root, each keeping its own saved state,
 * under the floating glass tab bar. ＋ opens the Add sheet over any tab. Tab items are tagged
 * `home.tab.<home|groups|add|activity|profile>`.
 */
@Composable
fun TabShell(navigator: MainNavigator, stateHolder: SaveableStateHolder) {
    val bottom = tabBarBottomOffset()
    Box(Modifier.fillMaxSize()) {
        CompositionLocalProvider(
            LocalTabBarPadding provides
                PaddingValues(bottom = bottom + PbSize.TabBar + TabBarContentGap)
        ) {
            val tab = navigator.selectedTab
            stateHolder.SaveableStateProvider("tab:${tab.id}") { RouteContent(tab.route) }
        }
        PbTabBar(
            items =
                Tab.entries.map {
                    PbTabItemSpec(stringResource(it.label), it.icon, "home.tab.${it.id}")
                },
            selectedIndex = navigator.selectedTab.ordinal,
            onSelect = { navigator.select(Tab.entries[it]) },
            onAdd = { navigator.open(Route.AddSheet) },
            modifier =
                Modifier.align(Alignment.BottomCenter)
                    .widthIn(max = PbLayout.MaxContentWidth)
                    .padding(
                        start = PbLayout.ScreenMargin,
                        end = PbLayout.ScreenMargin,
                        bottom = bottom,
                    ),
            addTestTag = "home.tab.add",
        )
    }
}
