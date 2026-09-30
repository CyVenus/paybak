package app.paybak.paybak.feature.insights

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.paybak.paybak.R
import app.paybak.paybak.data.ledger.collectSnapshot
import app.paybak.paybak.domain.insights.InsightsPage
import app.paybak.paybak.domain.insights.insightsPage
import app.paybak.paybak.navigation.LocalLedger
import app.paybak.paybak.navigation.LocalMainNavigator
import app.paybak.paybak.navigation.LocalTabBarPadding
import app.paybak.paybak.navigation.Route
import app.paybak.paybak.navigation.rememberDebugStartScreen
import app.paybak.paybak.ui.components.PbIconButton
import app.paybak.paybak.ui.components.PbIconButtonStyle
import app.paybak.paybak.ui.components.PbNavHeaderInline
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbLayout
import app.paybak.paybak.ui.theme.PbSize
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles
import java.time.YearMonth

/** Figma scrolls `insightsScrolled` by 676 pt; the column stops at its end if that's sooner. */
private val DebugScrollOffset = 676.dp

/**
 * The Activity tab's Insights segment (insightsSeptember, insightsScrolled, insightsLocked;
 * insights §2): your share of shared expenses for the month in `navigator.insightsMonth` (null =
 * this month). Unlike the timeline, the whole column scrolls, [header] included, and once the large
 * title has gone under the status bar the inline "Activity" bar fades in (§2.4). On the free plan
 * the report is blurred under the Pro notice and doesn't scroll (§2.5).
 *
 * @param header The Activity header (large title, Restore, segments), from the tab container.
 */
@Composable
fun InsightsView(modifier: Modifier = Modifier, header: @Composable () -> Unit = {}) {
    val navigator = LocalMainNavigator.current
    val snapshot by LocalLedger.current.collectSnapshot()
    val view = snapshot.view
    val month = navigator.insightsMonth ?: YearMonth.from(view.today)
    val page = remember(snapshot, month) { view.insightsPage(month) }
    val locked = !snapshot.isPro
    val scroll = rememberScrollState()
    val density = LocalDensity.current
    val collapseAt = with(density) { PbSize.Tap.roundToPx() }
    val collapsed by remember { derivedStateOf { scroll.value >= collapseAt } }
    val debugStart = rememberDebugStartScreen("insightsScrolled")
    LaunchedEffect(debugStart) {
        if (debugStart != null) scroll.scrollTo(with(density) { DebugScrollOffset.roundToPx() })
    }
    Box(modifier.fillMaxSize().testTag("screen.insights")) {
        Column(
            Modifier.fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(scroll, enabled = !locked)
                .padding(horizontal = PbLayout.ScreenMargin)
                .padding(LocalTabBarPadding.current)
        ) {
            header()
            Spacer(Modifier.height(PbSpace.S8))
            MonthRow(
                page,
                onMonth = { if (!locked) navigator.insightsMonth = it },
            )
            Spacer(Modifier.height(PbSpace.S16))
            if (locked) {
                LockedReport(page, onSeePro = { navigator.open(Route.Paywall()) })
            } else {
                InsightsReport(page)
            }
        }
        PbNavHeaderInline(
            stringResource(R.string.shell_activity_title),
            visible = collapsed && !locked,
            modifier = Modifier.testTag("activity.inlineHeader"),
        )
    }
}

/** ‹ September 2026 ›: the next chevron is dimmed on the current month. */
@Composable
private fun MonthRow(page: InsightsPage, onMonth: (YearMonth) -> Unit) {
    Row(
        Modifier.fillMaxWidth().height(PbSize.Tap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MonthButton(
            PbIcon.ChevronLeft,
            stringResource(R.string.insights_previous_month),
            enabled = page.hasPrevious,
            onClick = { onMonth(page.month.minusMonths(1)) },
            testTag = "insights.monthPrev",
        )
        Text(
            page.monthLabel,
            Modifier.weight(1f).testTag("insights.month"),
            style = PbTextStyles.Headline,
            color = PbColors.Text.Primary,
            textAlign = TextAlign.Center,
        )
        MonthButton(
            PbIcon.ChevronRight,
            stringResource(R.string.insights_next_month),
            enabled = page.hasNext,
            onClick = { onMonth(page.month.plusMonths(1)) },
            testTag = "insights.monthNext",
        )
    }
}

@Composable
private fun MonthButton(
    icon: PbIcon,
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    testTag: String,
) {
    PbIconButton(
        icon,
        contentDescription = label,
        onClick = { if (enabled) onClick() },
        modifier =
            Modifier.testTag(testTag)
                .alpha(if (enabled) 1f else DISABLED_ALPHA)
                .semantics { if (!enabled) disabled() },
        style = PbIconButtonStyle.Glass,
    )
}

private const val DISABLED_ALPHA = 0.3f
