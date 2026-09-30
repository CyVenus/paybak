package app.paybak.paybak.feature.friends

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import app.paybak.paybak.R
import app.paybak.paybak.data.ledger.actions.setRemindersMuted
import app.paybak.paybak.data.ledger.collectSnapshot
import app.paybak.paybak.domain.calc.FriendHistoryKind
import app.paybak.paybak.domain.groups.FriendBalanceCard
import app.paybak.paybak.domain.groups.FriendPageModel
import app.paybak.paybak.domain.groups.Standing
import app.paybak.paybak.domain.groups.friendPageModel
import app.paybak.paybak.domain.groups.inviteNotice
import app.paybak.paybak.domain.model.ExpenseDraft
import app.paybak.paybak.domain.model.ME
import app.paybak.paybak.domain.model.PaymentMethod
import app.paybak.paybak.feature.groups.GroupRowItem
import app.paybak.paybak.feature.groups.route
import app.paybak.paybak.navigation.ActivityFilter
import app.paybak.paybak.navigation.AddExpenseArgs
import app.paybak.paybak.navigation.LocalLedger
import app.paybak.paybak.navigation.LocalMainNavigator
import app.paybak.paybak.navigation.RecordPaymentArgs
import app.paybak.paybak.navigation.Route
import app.paybak.paybak.ui.components.PbActivityRow
import app.paybak.paybak.ui.components.PbAvatarContent
import app.paybak.paybak.ui.components.PbBalanceCard
import app.paybak.paybak.ui.components.PbBalanceType
import app.paybak.paybak.ui.components.PbButton
import app.paybak.paybak.ui.components.PbButtonSize
import app.paybak.paybak.ui.components.PbButtonStyle
import app.paybak.paybak.ui.components.PbCard
import app.paybak.paybak.ui.components.PbNoticeAction
import app.paybak.paybak.ui.components.PbNoticeCard
import app.paybak.paybak.ui.components.PbSectionHeader
import app.paybak.paybak.ui.components.PbSettingRow
import app.paybak.paybak.ui.components.PbSettingTrailing
import app.paybak.paybak.ui.components.PbTitleHeader
import app.paybak.paybak.ui.components.avatarContent
import app.paybak.paybak.ui.components.groups.PbPushedPage
import app.paybak.paybak.ui.components.iconForKey
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles

/**
 * The `friend` route (screens-groups §6): the one net between you with its actions (Remind and
 * Record payment while they owe you, Settle up while you owe), the History, Groups together and
 * the Automatic reminders toggle. A guest gets the invite notice first; with nothing shared yet
 * the page shows "No balance yet" and Add expense instead (§6.7).
 */
@Composable
fun FriendScreen(route: Route.Friend) {
    val navigator = LocalMainNavigator.current
    val ledger = LocalLedger.current
    val snapshot by ledger.collectSnapshot()
    val page =
        remember(snapshot, route.personId) {
            snapshot.view.friendPageModel(route.personId, snapshot.groups)
        }
    PbPushedPage(id = "friend", onBack = { navigator.back() }) {
        if (page == null) return@PbPushedPage
        PbTitleHeader(
            page.person.name,
            page.person.avatarContent(),
            Modifier.testTag("friend.title"),
            subtitle = page.subtitle,
            tag = if (page.guest) stringResource(R.string.groups_guest) else null,
        )
        if (page.guest) {
            Spacer(Modifier.height(PbSpace.S24))
            InviteNotice(page)
        }
        val balance = page.balance
        if (balance == null) {
            Spacer(Modifier.height(if (page.guest) PbSpace.S32 else PbSpace.S24))
            NoBalance(page)
            return@PbPushedPage
        }
        Spacer(Modifier.height(if (page.guest) PbSpace.S24 else PbSpace.S16))
        BalanceAndActions(page, balance)
        if (page.history.isNotEmpty()) {
            Spacer(Modifier.height(PbSpace.S24))
            History(page)
        }
        if (page.groups.isNotEmpty()) {
            Spacer(Modifier.height(PbSpace.S24))
            Column(verticalArrangement = Arrangement.spacedBy(PbSpace.S4)) {
                PbSectionHeader(stringResource(R.string.groups_together))
                Column {
                    page.groups.forEachIndexed { index, row ->
                        GroupRowItem(
                            row,
                            tagPrefix = "friend.group",
                            showDivider = index < page.groups.lastIndex,
                            onClick = { navigator.open(row.group.route()) },
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(PbSpace.S24))
        Column(verticalArrangement = Arrangement.spacedBy(PbSpace.S8)) {
            PbCard {
                PbSettingRow(
                    stringResource(R.string.groups_auto_reminders),
                    Modifier.testTag("friend.autoReminders"),
                    trailing =
                        PbSettingTrailing.Toggle(!page.person.remindersMuted) { on ->
                            ledger.setRemindersMuted(page.person.id, muted = !on)
                        },
                    icon = PbIcon.Bell,
                    showDivider = false,
                )
            }
            Text(
                stringResource(R.string.groups_auto_reminders_note, page.firstName),
                style = PbTextStyles.Footnote,
                color = PbColors.Text.Secondary,
            )
        }
    }
}

/** The balance card, then Remind + Record payment (they owe you) or Settle up (you owe). */
@Composable
private fun BalanceAndActions(page: FriendPageModel, balance: FriendBalanceCard) {
    val navigator = LocalMainNavigator.current
    val person = page.person
    val context = page.paymentContext
    fun payment(fromId: String, toId: String) =
        Route.RecordPayment(
            RecordPaymentArgs(
                fromId = fromId,
                toId = toId,
                amount = kotlin.math.abs(page.net),
                method = PaymentMethod.Upi.takeIf { fromId == ME && person.upi != null },
                groupId = context.groupId,
                loanId = context.loanId,
                expenseId = context.expenseId,
            )
        )
    Column(verticalArrangement = Arrangement.spacedBy(PbSpace.S8)) {
        Column(verticalArrangement = Arrangement.spacedBy(PbSpace.S12)) {
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
                label = balance.label,
                showChevron = false,
                badge = balance.overdue,
                testTag = "friend.balance",
            )
            when (balance.standing) {
                Standing.Owed ->
                    Row(horizontalArrangement = Arrangement.spacedBy(PbSpace.S12)) {
                        PbButton(
                            stringResource(R.string.groups_remind),
                            onClick = {
                                navigator.open(Route.Remind(person.id, page.remindContext))
                            },
                            modifier = Modifier.weight(1f).testTag("friend.remind"),
                            leadingIcon = PbIcon.Bell,
                        )
                        PbButton(
                            stringResource(R.string.groups_record_payment),
                            onClick = { navigator.open(payment(fromId = person.id, toId = ME)) },
                            modifier = Modifier.weight(1f).testTag("friend.recordPayment"),
                            style = PbButtonStyle.Secondary,
                        )
                    }
                Standing.Owe ->
                    PbButton(
                        stringResource(R.string.pb_settle_up),
                        onClick = { navigator.open(payment(fromId = ME, toId = person.id)) },
                        modifier = Modifier.fillMaxWidth().testTag("friend.settleUp"),
                    )
                Standing.Settled -> Unit
            }
        }
        page.lastReminder?.let {
            Text(
                it,
                Modifier.testTag("friend.lastReminder"),
                style = PbTextStyles.Footnote,
                color = PbColors.Text.Tertiary,
            )
        }
    }
}

@Composable
private fun History(page: FriendPageModel) {
    val navigator = LocalMainNavigator.current
    Column(verticalArrangement = Arrangement.spacedBy(PbSpace.S4)) {
        PbSectionHeader(
            stringResource(R.string.groups_history),
            action = if (page.moreHistory) stringResource(R.string.groups_see_all) else null,
            onAction = {
                navigator.open(Route.ActivityLog(ActivityFilter.Person(page.person.id)))
            },
        )
        Column {
            page.history.forEach { row ->
                val item = row.item
                PbActivityRow(
                    leading = PbAvatarContent.Symbol(historyIcon(row.icon)),
                    title = row.title,
                    subtitle = row.subtitle,
                    amount = row.amount,
                    amountPrimary = row.open,
                    date = row.date,
                    onClick = {
                        navigator.open(
                            when (item.kind) {
                                FriendHistoryKind.Expense -> Route.Expense(item.ref)
                                FriendHistoryKind.Payment -> Route.Payment(item.ref)
                                FriendHistoryKind.Loan -> Route.Loan(item.ref)
                            }
                        )
                    },
                    testTag = "friend.history.${item.ref}",
                )
            }
        }
    }
}

private fun historyIcon(key: String): PbIcon =
    when (key) {
        "money-in" -> PbIcon.MoneyIn
        "money-out" -> PbIcon.MoneyOut
        else -> iconForKey(key)
    }

/** "Invite Ananya to Paybak" with Send invite: the share sheet with your invite link. */
@Composable
private fun InviteNotice(page: FriendPageModel) {
    val share = rememberInviteShare()
    val (title, body) = inviteNotice(page.person)
    PbNoticeCard(
        body,
        PbIcon.Mail,
        title = title,
        primaryAction =
            PbNoticeAction(stringResource(R.string.groups_send_invite), share, PbIcon.Share),
        testTag = "friend.invite",
    )
}

/** Nothing shared yet: "No balance yet" and Add expense with this friend already in the split. */
@Composable
private fun NoBalance(page: FriendPageModel) {
    val navigator = LocalMainNavigator.current
    Column(
        Modifier.fillMaxWidth().testTag("friend.noBalance"),
        verticalArrangement = Arrangement.spacedBy(PbSpace.S16),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(PbSpace.S4),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                stringResource(R.string.groups_no_balance_title),
                style = PbTextStyles.Headline,
                color = PbColors.Text.Primary,
                textAlign = TextAlign.Center,
            )
            Text(
                stringResource(R.string.groups_no_balance_body, page.firstName),
                style = PbTextStyles.Footnote,
                color = PbColors.Text.Secondary,
                textAlign = TextAlign.Center,
            )
        }
        PbButton(
            stringResource(R.string.groups_add_expense),
            onClick = {
                val draft = ExpenseDraft.equal(listOf(ME, page.person.id))
                navigator.open(Route.AddExpense(AddExpenseArgs(draft = draft)))
            },
            modifier = Modifier.testTag("friend.addExpense"),
            style = PbButtonStyle.Secondary,
            size = PbButtonSize.Small,
            leadingIcon = PbIcon.Plus,
        )
    }
}
