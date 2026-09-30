package app.paybak.paybak.debug

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import app.paybak.paybak.PaybakApplication
import app.paybak.paybak.debug.menu.ActivityDebugActions
import app.paybak.paybak.debug.menu.AddRecordDebugActions
import app.paybak.paybak.debug.menu.DebugAction
import app.paybak.paybak.debug.menu.DebugContext
import app.paybak.paybak.debug.menu.DebugSection
import app.paybak.paybak.debug.menu.GroupsDebugActions
import app.paybak.paybak.debug.menu.HomeDebugActions
import app.paybak.paybak.debug.menu.InsightsDebugActions
import app.paybak.paybak.debug.menu.ProfileDebugActions
import app.paybak.paybak.debug.menu.ProjectsDebugActions
import app.paybak.paybak.debug.menu.SettingsDebugActions
import app.paybak.paybak.debug.menu.SettleDebugActions
import app.paybak.paybak.debug.menu.coreSections
import app.paybak.paybak.domain.format.Dates
import app.paybak.paybak.navigation.LocalAppClock
import app.paybak.paybak.navigation.LocalMainNavigator
import app.paybak.paybak.navigation.Route
import app.paybak.paybak.ui.components.PbCard
import app.paybak.paybak.ui.components.PbSettingRow
import app.paybak.paybak.ui.components.PbSheet
import app.paybak.paybak.ui.components.PbSheetDetent
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles

/**
 * The debug menu (long-press the Home logo; app-architecture §3.10): M2's data, clock, Pro and
 * friend's-side sections, then each module's own actions. A row closes the menu, then runs.
 */
@Composable
fun DebugMenu(route: Route.DebugMenu) {
    val navigator = LocalMainNavigator.current
    val context = LocalContext.current
    val clock = LocalAppClock.current
    val debug =
        DebugContext(
            context.applicationContext as PaybakApplication,
            navigator,
            context as Activity,
        )
    var pending by remember { mutableStateOf<DebugAction?>(null) }
    val sections =
        coreSections(debug) +
            listOf(
                    DebugSection("Home", HomeDebugActions),
                    DebugSection("Add & Record", AddRecordDebugActions),
                    DebugSection("Groups & Friends", GroupsDebugActions),
                    DebugSection("Settle up", SettleDebugActions),
                    DebugSection("Activity & Notifications", ActivityDebugActions),
                    DebugSection("Projects", ProjectsDebugActions),
                    DebugSection("Profile", ProfileDebugActions),
                    DebugSection("Settings & Pro", SettingsDebugActions),
                    DebugSection("Insights & AI", InsightsDebugActions),
                )
                .filter { it.actions.isNotEmpty() }
    PbSheet(
        onDismiss = {
            navigator.dismissSheet()
            pending?.run?.invoke(debug)
        },
        title = "Debug",
        detent = PbSheetDetent.Large,
        testTag = "debugMenu.sheet",
    ) { dismiss ->
        val now = clock.now().atZone(clock.zone)
        LazyColumn(
            Modifier.fillMaxSize().testTag("screen.debugMenu"),
            contentPadding = PaddingValues(bottom = PbSpace.S24),
            verticalArrangement = Arrangement.spacedBy(PbSpace.S16),
        ) {
            item {
                val clockMode = if (clock.pinned.value != null) "Pinned" else "Real time"
                Text(
                    "$clockMode: ${Dates.day(now.toLocalDate())}, ${Dates.time(now.toLocalDateTime())}",
                    style = PbTextStyles.Footnote,
                    color = PbColors.Text.Tertiary,
                )
            }
            sections.forEach { section ->
                item {
                    Text(
                        section.title,
                        style = PbTextStyles.Headline,
                        color = PbColors.Text.Primary,
                    )
                }
                item {
                    PbCard {
                        section.actions.forEachIndexed { index, action ->
                            PbSettingRow(
                                title = action.title,
                                subtitle = action.subtitle,
                                onClick = {
                                    pending = action
                                    dismiss()
                                },
                                modifier = Modifier.testTag("debugMenu.${action.title}"),
                                showDivider = index < section.actions.lastIndex,
                            )
                        }
                    }
                }
            }
        }
    }
}
