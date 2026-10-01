package app.paybak.paybak.feature.groups

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import app.paybak.paybak.R
import app.paybak.paybak.data.ledger.collectSnapshot
import app.paybak.paybak.domain.calc.LedgerView
import app.paybak.paybak.domain.groups.ExpenseDay
import app.paybak.paybak.domain.groups.GroupBalanceCard
import app.paybak.paybak.domain.groups.GroupPage
import app.paybak.paybak.domain.groups.Standing
import app.paybak.paybak.domain.groups.groupPage
import app.paybak.paybak.domain.model.Category
import app.paybak.paybak.domain.model.ExpenseDraft
import app.paybak.paybak.domain.model.ME
import app.paybak.paybak.navigation.AddExpenseArgs
import app.paybak.paybak.navigation.LocalLedger
import app.paybak.paybak.navigation.LocalMainNavigator
import app.paybak.paybak.navigation.Route
import app.paybak.paybak.rive.PaybakRiveAsset
import app.paybak.paybak.ui.components.PbActivityRow
import app.paybak.paybak.ui.components.PbAvatarContent
import app.paybak.paybak.ui.components.PbBalanceCard
import app.paybak.paybak.ui.components.PbBalanceType
import app.paybak.paybak.ui.components.PbCard
import app.paybak.paybak.ui.components.PbEmptyAction
import app.paybak.paybak.ui.components.PbEmptyState
import app.paybak.paybak.ui.components.PbHeaderAction
import app.paybak.paybak.ui.components.PbPersonRow
import app.paybak.paybak.ui.components.PbPersonRowSize
import app.paybak.paybak.ui.components.PbPersonTrailing
import app.paybak.paybak.ui.components.PbSectionHeader
import app.paybak.paybak.ui.components.PbTitleHeader
import app.paybak.paybak.ui.components.groups.PbPushedPage
import app.paybak.paybak.ui.components.iconForKey
import app.paybak.paybak.ui.components.pbIcon
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles

/**
 * The `group` route (screens-groups §4): the header with the member stack, your balance with Settle
 * up, every member's paid vs share with the simplify and exchange-rate notes, then the expenses by
 * date. A new group (record-lend-group §7, `newGroupCreated`) shows ₹0, a disabled Settle up and
 * the "No expenses yet." card instead.
 */
@Composable
fun GroupDetailScreen(route: Route.Group) {
    val navigator = LocalMainNavigator.current
    val snapshot by LocalLedger.current.collectSnapshot()
    val view = snapshot.view
    val page = remember(snapshot, route.groupId) { view.groupPage(route.groupId) }
    PbPushedPage(
        id = "group",
        onBack = { navigator.back() },
        action =
            PbHeaderAction.Icon(PbIcon.Settings, stringResource(R.string.groups_settings)) {
                navigator.open(Route.GroupSettings(route.groupId))
            },
        actionTag = "settings",
    ) {
        if (page == null) return@PbPushedPage
        PbTitleHeader(
            page.group.name,
            PbAvatarContent.Symbol(iconForKey(page.group.icon)),
            Modifier.testTag("group.title"),
            subtitle = page.subtitle,
            memberAvatars = rememberAvatars(view, page.memberIds.take(4)),
        )
        Spacer(Modifier.height(PbSpace.S16))
        BalanceCard(
            page.balance,
            onSettle = { page.balance.settle?.let { navigator.settle(view, it) } },
        )
        Spacer(Modifier.height(PbSpace.S24))
        if (page.isEmpty) {
            PbEmptyState(
                title = stringResource(R.string.groups_no_expenses_title),
                body = stringResource(R.string.groups_no_expenses_body),
                illustration = PaybakRiveAsset.HomeFirstDay,
                primaryAction =
                    PbEmptyAction(
                        stringResource(R.string.groups_add_expense),
                        PbIcon.Plus,
                        { navigator.open(Route.AddExpense(AddExpenseArgs(draft = page.draft()))) },
                        testTag = "group.addExpense",
                    ),
                testTag = "group.empty",
            )
        } else {
            Balances(page, view)
            Spacer(Modifier.height(PbSpace.S24))
            Expenses(page.days)
        }
    }
}

/** A new expense in this group, split equally with everyone in it. */
private fun GroupPage.draft(): ExpenseDraft =
    ExpenseDraft.equal(memberIds).copy(groupId = group.id, currency = group.currency)

@Composable
private fun BalanceCard(balance: GroupBalanceCard, onSettle: () -> Unit) {
    PbBalanceCard(
        type =
            when (balance.standing) {
                Standing.Owe -> PbBalanceType.Owe
                Standing.Owed -> PbBalanceType.Owed
                Standing.Settled -> PbBalanceType.Settled
            },
        amount = balance.amount,
        caption = balance.caption,
        modifier = Modifier.fillMaxWidth(),
        label = stringResource(R.string.groups_your_balance),
        showChevron = false,
        action =
            if (balance.settle != null || balance.showsDisabledAction) {
                stringResource(R.string.pb_settle_up)
            } else {
                null
            },
        onAction = onSettle,
        actionEnabled = balance.settle != null,
        testTag = "group.balance",
    )
}

@Composable
private fun Balances(page: GroupPage, view: LedgerView) {
    val navigator = LocalMainNavigator.current
    val avatars = rememberAvatars(view, page.members.map { it.personId })
    Column(verticalArrangement = Arrangement.spacedBy(PbSpace.S4)) {
        PbSectionHeader(stringResource(R.string.groups_balances))
        Column(verticalArrangement = Arrangement.spacedBy(PbSpace.S8)) {
            PbCard {
                page.members.forEachIndexed { index, member ->
                    PbPersonRow(
                        name = member.name,
                        avatar = avatars[index],
                        modifier = Modifier.testTag("group.balances.row.${member.personId}"),
                        subtitle = member.subtitle,
                        trailing =
                            member.amount?.let {
                                PbPersonTrailing.Amount(it, member.standing.balance(), member.label)
                            } ?: PbPersonTrailing.Status(stringResource(R.string.groups_settled)),
                        size = PbPersonRowSize.Compact,
                        onClick =
                            if (member.personId == ME) null
                            else ({ navigator.open(Route.Friend(member.personId)) }),
                        showDivider = index < page.members.lastIndex,
                    )
                }
            }
            page.simplifyNote?.let { Note(it, "group.simplifyNote") }
            page.totalNote?.let { Note(it, "group.totalNote") }
        }
    }
}

@Composable
private fun Note(text: String, testTag: String) {
    Text(
        text,
        Modifier.testTag(testTag),
        style = PbTextStyles.Footnote,
        color = PbColors.Text.Secondary,
    )
}

@Composable
private fun Expenses(days: List<ExpenseDay>) {
    val navigator = LocalMainNavigator.current
    Column(verticalArrangement = Arrangement.spacedBy(PbSpace.S4)) {
        PbSectionHeader(stringResource(R.string.groups_expenses))
        Column(verticalArrangement = Arrangement.spacedBy(PbSpace.S8)) {
            days.forEach { day ->
                Column(verticalArrangement = Arrangement.spacedBy(PbSpace.S4)) {
                    Text(day.label, style = PbTextStyles.Footnote, color = PbColors.Text.Secondary)
                    Column {
                        day.rows.forEach { row ->
                            PbActivityRow(
                                leading =
                                    PbAvatarContent.Symbol(
                                        Category.of(row.expense.category).pbIcon
                                    ),
                                title = row.expense.title,
                                subtitle = row.subtitle,
                                detail = row.detail,
                                amount = row.amount,
                                onClick = { navigator.open(Route.Expense(row.expense.id)) },
                                testTag = "group.expense.${row.expense.id}",
                            )
                        }
                    }
                }
            }
        }
    }
}
