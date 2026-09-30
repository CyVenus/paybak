package app.paybak.paybak.feature.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.paybak.paybak.R
import app.paybak.paybak.data.ledger.collectSnapshot
import app.paybak.paybak.domain.format.Dates
import app.paybak.paybak.domain.format.Money
import app.paybak.paybak.domain.insights.InsightRow
import app.paybak.paybak.domain.insights.InsightsPage
import app.paybak.paybak.domain.insights.LoanBadge
import app.paybak.paybak.domain.insights.LoanLine
import app.paybak.paybak.navigation.ActivityFilter
import app.paybak.paybak.navigation.LocalLedger
import app.paybak.paybak.navigation.LocalMainNavigator
import app.paybak.paybak.navigation.Route
import app.paybak.paybak.ui.components.PbAvatar
import app.paybak.paybak.ui.components.PbAvatarContent
import app.paybak.paybak.ui.components.PbAvatarSize
import app.paybak.paybak.ui.components.PbBadge
import app.paybak.paybak.ui.components.PbBadgeStyle
import app.paybak.paybak.ui.components.PbBarRow
import app.paybak.paybak.ui.components.PbMonthlyBarChart
import app.paybak.paybak.ui.components.PbSectionHeader
import app.paybak.paybak.ui.components.PbSegmentedControl
import app.paybak.paybak.ui.components.avatarContent
import app.paybak.paybak.ui.components.iconForKey
import app.paybak.paybak.ui.components.pressable
import app.paybak.paybak.ui.components.rememberPressState
import app.paybak.paybak.ui.components.rowPressColor
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbShapes
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles

private val HeroHeight = 280.dp

/** Everything below the month row (insights §2.2, §2.4), for a Pro member. */
@Composable
internal fun InsightsReport(page: InsightsPage) {
    Column(Modifier.fillMaxWidth()) {
        HeroCard(page)
        Spacer(Modifier.height(PbSpace.S16))
        CategorySection(page)
        if (page.total > 0) WhoSection(page)
        LentSection(page)
    }
}

/** "Your share of shared expenses", the total with its trend and the six-month chart. */
@Composable
internal fun HeroCard(page: InsightsPage, content: Modifier = Modifier) {
    Column(
        Modifier.fillMaxWidth()
            .height(HeroHeight)
            .background(PbColors.Bg.Card, PbShapes.Card)
            .padding(PbSpace.S20),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        HeroSummary(page, content)
        PbMonthlyBarChart(
            values = page.chart.map { it.heightPt.toFloat() },
            labels = page.chart.map { it.label },
            description = page.chartDescription,
            modifier = content.testTag("insights.chart"),
        )
    }
}

@Composable
private fun HeroSummary(page: InsightsPage, modifier: Modifier) {
    val currency = LocalLedger.current.defaultCurrency
    Column(modifier, verticalArrangement = Arrangement.spacedBy(PbSpace.S4)) {
        Text(
            stringResource(R.string.insights_your_share),
            style = PbTextStyles.Subheadline,
            color = PbColors.Text.Secondary,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(PbSpace.S8),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                Money.format(page.total, currency),
                Modifier.testTag("insights.total"),
                style = PbTextStyles.Title1,
                color = PbColors.Text.Primary,
            )
            page.trend?.let {
                PbBadge(it, Modifier.testTag("insights.trend"), style = PbBadgeStyle.OnCard)
            }
        }
    }
}

/** "By category": one bar per category, largest first. */
@Composable
internal fun CategorySection(page: InsightsPage, rows: Modifier = Modifier) {
    val navigator = LocalMainNavigator.current
    Column(Modifier.fillMaxWidth().padding(top = PbSpace.S8)) {
        PbSectionHeader(stringResource(R.string.insights_by_category), rows)
        if (page.categories.isEmpty()) {
            Text(
                stringResource(R.string.insights_no_expenses, Dates.monthName(page.month.month)),
                Modifier.padding(top = PbSpace.S8),
                style = PbTextStyles.Footnote,
                color = PbColors.Text.Tertiary,
            )
        }
        page.categories.forEach { row ->
            ShareRow(row, rows.testTag("insights.category.${row.key}")) {
                navigator.open(
                    Route.ActivityLog(ActivityFilter.Category(row.key, page.month.toString()))
                )
            }
        }
    }
}

/** "Who you spent with": the month by group, or by friend. */
@Composable
private fun WhoSection(page: InsightsPage) {
    val navigator = LocalMainNavigator.current
    var friends by rememberSaveable { mutableIntStateOf(0) }
    Column(
        Modifier.fillMaxWidth().padding(top = PbSpace.S24),
        verticalArrangement = Arrangement.spacedBy(PbSpace.S8),
    ) {
        PbSectionHeader(stringResource(R.string.insights_who_you_spent_with))
        PbSegmentedControl(
            listOf(stringResource(R.string.insights_groups), stringResource(R.string.insights_friends)),
            selectedIndex = friends,
            onSelect = { friends = it },
            modifier = Modifier.fillMaxWidth(),
            segmentTags = listOf("insights.who.groups", "insights.who.friends"),
        )
        Column {
            if (friends == 0) {
                page.groups.forEach { row ->
                    ShareRow(row, Modifier.testTag("insights.group.${row.groupId ?: "none"}")) {
                        row.groupId?.let {
                            navigator.open(Route.ActivityLog(ActivityFilter.Group(it)))
                        }
                    }
                }
            } else {
                page.friends.forEach { row ->
                    ShareRow(row, Modifier.testTag("insights.friend.${row.personId}")) {
                        row.personId?.let {
                            navigator.open(Route.ActivityLog(ActivityFilter.Person(it)))
                        }
                    }
                }
            }
        }
    }
}

/** A share bar: an icon (category, group) or a friend's head in the grey circle. */
@Composable
private fun ShareRow(row: InsightRow, modifier: Modifier, onClick: () -> Unit) {
    val snapshot by LocalLedger.current.collectSnapshot()
    val currency = LocalLedger.current.defaultCurrency
    val leading =
        row.personId?.let { snapshot.view.person(it)?.avatarContent() }
            ?: PbAvatarContent.Symbol(iconForKey(row.icon ?: "tag"))
    PbBarRow(
        title = row.label,
        amount = Money.format(row.amount, currency),
        progress = row.percent / 100f,
        leading = leading,
        modifier = modifier,
        caption = "${row.percent}%",
        onClick = onClick,
    )
}

/** "Lent vs borrowed since April": the loans of the chart's six months, and the footnote. */
@Composable
private fun LentSection(page: InsightsPage) {
    val currency = LocalLedger.current.defaultCurrency
    Column(
        Modifier.fillMaxWidth().padding(top = PbSpace.S24),
        verticalArrangement = Arrangement.spacedBy(PbSpace.S8),
    ) {
        PbSectionHeader(page.lentTitle)
        Column(verticalArrangement = Arrangement.spacedBy(PbSpace.S12)) {
            Column(
                Modifier.fillMaxWidth()
                    .background(PbColors.Bg.Card, PbShapes.Card)
                    .padding(PbSpace.S16),
                verticalArrangement = Arrangement.spacedBy(PbSpace.S12),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(PbSpace.S16)) {
                    Total(
                        stringResource(R.string.insights_lent),
                        Money.format(page.lent, currency),
                        Modifier.weight(1f).testTag("insights.lent"),
                    )
                    Total(
                        stringResource(R.string.insights_borrowed),
                        Money.format(page.borrowed, currency),
                        Modifier.weight(1f).testTag("insights.borrowed"),
                    )
                }
                page.loans.forEach { LoanRow(it) }
            }
            Text(
                stringResource(R.string.insights_footnote),
                style = PbTextStyles.Footnote,
                color = PbColors.Text.Tertiary,
            )
        }
    }
}

@Composable
private fun Total(label: String, amount: String, modifier: Modifier) {
    Column(modifier) {
        Text(label, style = PbTextStyles.Footnote, color = PbColors.Text.Secondary)
        Text(amount, style = PbTextStyles.AmountLarge, color = PbColors.Text.Primary)
    }
}

/** "Kabir · Bike service" with its status; opens the loan. */
@Composable
private fun LoanRow(line: LoanLine) {
    val navigator = LocalMainNavigator.current
    val snapshot by LocalLedger.current.collectSnapshot()
    val person = snapshot.view.person(line.personId)
    val press = rememberPressState(interactionSource = null)
    Row(
        Modifier.fillMaxWidth()
            .testTag("insights.loan.${line.loanId}")
            .background(rowPressColor(press.isPressed, onCard = true), PbShapes.Pill)
            .pressable(press, enabled = true) { navigator.open(Route.Loan(line.loanId)) },
        horizontalArrangement = Arrangement.spacedBy(PbSpace.S8),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        person?.let { PbAvatar(it.avatarContent(), size = PbAvatarSize.Xs, onCard = true) }
        Text(
            line.label,
            Modifier.weight(1f),
            style = PbTextStyles.Footnote,
            color = PbColors.Text.Secondary,
            maxLines = 1,
        )
        when (val badge = line.badge) {
            LoanBadge.PaidBack ->
                PbBadge(stringResource(R.string.insights_paid_back), style = PbBadgeStyle.Inverse)
            LoanBadge.Overdue ->
                PbBadge(stringResource(R.string.insights_overdue), style = PbBadgeStyle.Overdue)
            is LoanBadge.Due ->
                PbBadge(
                    stringResource(R.string.insights_due, Dates.short(badge.date)),
                    style = PbBadgeStyle.OnCard,
                )
            null -> Unit
        }
    }
}

@Preview(showBackground = true, widthDp = 362)
@Composable
private fun TotalPreview() {
    Row(horizontalArrangement = Arrangement.spacedBy(PbSpace.S16)) {
        Total("Lent", "₹4,500", Modifier.weight(1f))
        Total("Borrowed", "₹0", Modifier.weight(1f))
    }
}
