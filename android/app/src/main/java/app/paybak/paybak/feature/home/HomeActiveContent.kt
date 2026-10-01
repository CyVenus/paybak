package app.paybak.paybak.feature.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import app.paybak.paybak.R
import app.paybak.paybak.domain.calc.DueAction
import app.paybak.paybak.domain.calc.DueSoonRow
import app.paybak.paybak.domain.calc.HomeActivityRow
import app.paybak.paybak.domain.calc.HomeSummary
import app.paybak.paybak.domain.calc.LedgerView
import app.paybak.paybak.domain.format.Money
import app.paybak.paybak.domain.format.MoneySign
import app.paybak.paybak.domain.model.Category
import app.paybak.paybak.navigation.ActivitySegment
import app.paybak.paybak.navigation.LocalMainNavigator
import app.paybak.paybak.navigation.Route
import app.paybak.paybak.navigation.Tab
import app.paybak.paybak.ui.components.PbActivityRow
import app.paybak.paybak.ui.components.PbAvatarContent
import app.paybak.paybak.ui.components.PbBalanceSummary
import app.paybak.paybak.ui.components.PbSectionHeader
import app.paybak.paybak.ui.components.avatarContent
import app.paybak.paybak.ui.components.home.PbAttentionRow
import app.paybak.paybak.ui.components.pbIcon
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.theme.LocalReduceMotion
import app.paybak.paybak.ui.theme.PbLayout
import app.paybak.paybak.ui.theme.PbMotion
import app.paybak.paybak.ui.theme.PbSpace

/** A new Recent activity row (e.g. "Esha paid you" after Confirm) grows in at the top. */
private const val ROW_IN_MILLIS = 250

/**
 * Home with data (screens-home §2): the balance summary, Due soon and Recent activity, 24 dp
 * apart. Every element opens its flow (app-architecture §2.3).
 */
@Composable
internal fun HomeActiveContent(home: HomeSummary, view: LedgerView) {
    val navigator = LocalMainNavigator.current
    Column(verticalArrangement = Arrangement.spacedBy(PbLayout.SectionGap)) {
        PbBalanceSummary(
            owedAmount = Money.format(home.totals.owed, view.defaultCurrency, MoneySign.Signed),
            owedCaption =
                if (home.totals.owed > 0) home.totals.owedCaption
                else stringResource(R.string.home_nothing_pending),
            oweAmount = Money.format(-home.totals.owe, view.defaultCurrency, MoneySign.Signed),
            oweCaption =
                if (home.totals.owe > 0) home.totals.oweCaption
                else stringResource(R.string.home_nothing_to_pay),
            onOwed = { navigator.open(Route.OwedBreakdown) },
            onOwe = { navigator.open(Route.OweBreakdown) },
            onSettleUp = { navigator.open(Route.SettleUp()) },
            testTag = "home.balance",
        )
        if (home.dueSoon.isNotEmpty()) DueSoonSection(home.dueSoon, view)
        if (home.recent.isNotEmpty()) RecentActivitySection(home.recent, view)
    }
}

/** Overdue or due within two days, at most three (domain.md §6.1). */
@Composable
private fun DueSoonSection(rows: List<DueSoonRow>, view: LedgerView) {
    val navigator = LocalMainNavigator.current
    Column(verticalArrangement = Arrangement.spacedBy(PbSpace.S12)) {
        PbSectionHeader(stringResource(R.string.home_due_soon))
        Column(verticalArrangement = Arrangement.spacedBy(PbSpace.S8)) {
            rows.forEach { row ->
                PbAttentionRow(
                    avatar =
                        if (row.isGroupRow) PbAvatarContent.Symbol(PbIcon.Groups)
                        else
                            view.person(row.obligation.friendId)?.avatarContent()
                                ?: PbAvatarContent.Symbol(PbIcon.Profile),
                    title = row.title,
                    detail = row.detail,
                    badge = row.badge,
                    overdue = row.overdue,
                    amount = Money.format(row.amount, view.defaultCurrency),
                    action =
                        stringResource(
                            if (row.action == DueAction.Remind) R.string.home_remind
                            else R.string.home_settle
                        ),
                    onAction = { navigator.open(row.actionRoute(view)) },
                    onClick = { navigator.open(row.bodyRoute(view)) },
                    testTag = "home.due.${row.id}",
                )
            }
        }
    }
}

/**
 * The three newest expenses and payments; See all switches to the Activity timeline. A row that
 * appears while Home is shown grows in.
 */
@Composable
private fun RecentActivitySection(rows: List<HomeActivityRow>, view: LedgerView) {
    val navigator = LocalMainNavigator.current
    val firstRefs = remember { rows.map { it.event.ref }.toSet() }
    val enter =
        if (LocalReduceMotion.current) fadeIn(tween(ROW_IN_MILLIS))
        else
            fadeIn(tween(ROW_IN_MILLIS)) +
                expandVertically(tween(ROW_IN_MILLIS, easing = PbMotion.EaseOut))
    Column(verticalArrangement = Arrangement.spacedBy(PbSpace.S4)) {
        PbSectionHeader(
            stringResource(R.string.home_recent_activity),
            action = stringResource(R.string.home_see_all),
            onAction = {
                navigator.select(Tab.Activity)
                navigator.activitySegment = ActivitySegment.Timeline
            },
            actionTestTag = "home.seeAll",
        )
        rows.forEach { row ->
            val ref = row.event.ref
            key(ref) {
                val visibility = remember {
                    MutableTransitionState(ref in firstRefs).apply { targetState = true }
                }
                AnimatedVisibility(visibility, enter = enter) {
                    PbActivityRow(
                        leading = row.leading(view),
                        title = row.title,
                        subtitle = row.subtitle,
                        amount = row.amount,
                        amountPrimary = row.primary,
                        date = row.date,
                        onClick = { navigator.open(row.route()) },
                        testTag = "home.activity.row.$ref",
                    )
                }
            }
        }
    }
}

/** A payment shows the other person; an expense its category icon. */
private fun HomeActivityRow.leading(view: LedgerView): PbAvatarContent =
    event.personId?.let { view.person(it)?.avatarContent() }
        ?: PbAvatarContent.Symbol(Category.of(event.category.orEmpty()).pbIcon)
