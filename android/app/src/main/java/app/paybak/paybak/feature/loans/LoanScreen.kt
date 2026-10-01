package app.paybak.paybak.feature.loans

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import app.paybak.paybak.R
import app.paybak.paybak.data.ledger.collectSnapshot
import app.paybak.paybak.domain.addrecord.LoanSchedule
import app.paybak.paybak.domain.calc.loanDetail
import app.paybak.paybak.domain.format.Dates
import app.paybak.paybak.domain.format.Money
import app.paybak.paybak.domain.model.ME
import app.paybak.paybak.domain.model.ReminderContext
import app.paybak.paybak.feature.pickers.rememberPeopleDirectory
import app.paybak.paybak.navigation.LendMoneyArgs
import app.paybak.paybak.navigation.LocalLedger
import app.paybak.paybak.navigation.LocalMainNavigator
import app.paybak.paybak.navigation.RecordPaymentArgs
import app.paybak.paybak.navigation.Route
import app.paybak.paybak.navigation.pinnedFooter
import app.paybak.paybak.ui.components.PbActivityRow
import app.paybak.paybak.ui.components.PbAmountHero
import app.paybak.paybak.ui.components.PbAvatarContent
import app.paybak.paybak.ui.components.PbBadgeStyle
import app.paybak.paybak.ui.components.PbButton
import app.paybak.paybak.ui.components.PbButtonStyle
import app.paybak.paybak.ui.components.PbCard
import app.paybak.paybak.ui.components.PbHeaderAction
import app.paybak.paybak.ui.components.PbHeroLeading
import app.paybak.paybak.ui.components.PbLoanProgressCard
import app.paybak.paybak.ui.components.PbPushHeader
import app.paybak.paybak.ui.components.PbRowSurface
import app.paybak.paybak.ui.components.PbSectionHeader
import app.paybak.paybak.ui.components.addrecord.PbPinnedHeaderScreen
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles

/**
 * The `loan` route (`loanAdded`, `loanPaidBack`, `loanOverdue`; record-lend-group §5): original →
 * paid → remaining with the bar, then the installments ("Paid 14 Sep · 2 days late" in grey, the
 * red Overdue badge only for what's overdue now). While money is owed, Record repayment (and Remind
 * when an installment is overdue) stay pinned at the bottom.
 */
@Composable
fun LoanScreen(route: Route.Loan) {
    val navigator = LocalMainNavigator.current
    val snapshot by LocalLedger.current.collectSnapshot()
    val people = rememberPeopleDirectory()
    val detail = snapshot.view.loanDetail(route.loanId)
    val loan = detail?.loan
    val paidBack = detail != null && detail.remaining == 0L
    val lent = loan?.lenderId == ME
    val overdue = detail?.rows?.any { it.overdue } == true
    val next = detail?.rows?.firstOrNull { it.installment.paidOn == null }
    val noDueDate = stringResource(R.string.add_no_due_date)

    PbPinnedHeaderScreen(
        testTag = "screen.loan",
        header = {
            PbPushHeader(
                stringResource(R.string.add_loan),
                onBack = { navigator.back() },
                action =
                    if (loan != null && !paidBack) {
                        PbHeaderAction.Text(
                            stringResource(R.string.add_edit),
                            {
                                navigator.open(Route.LendMoney(LendMoneyArgs(editing = loan.id)))
                            },
                        )
                    } else null,
                testTag = "loan",
                actionTag = "edit",
            )
        },
        footer =
            if (loan != null && !paidBack) {
                {
                    Column(
                        Modifier.pinnedFooter(),
                        verticalArrangement = Arrangement.spacedBy(PbSpace.S12),
                    ) {
                        if (overdue && lent) {
                            PbButton(
                                stringResource(R.string.add_remind, people.first(loan.friendId)),
                                onClick = {
                                    navigator.open(
                                        Route.Remind(
                                            loan.friendId,
                                            ReminderContext(loanId = loan.id),
                                        )
                                    )
                                },
                                modifier = Modifier.fillMaxWidth().testTag("loan.remind"),
                                style = PbButtonStyle.Secondary,
                            )
                        }
                        PbButton(
                            stringResource(R.string.add_record_repayment),
                            onClick = {
                                navigator.open(
                                    Route.RecordPayment(
                                        RecordPaymentArgs(
                                            fromId = loan.borrowerId,
                                            toId = loan.lenderId,
                                            amount = next?.installment?.amount ?: detail.remaining,
                                            currency = loan.currency,
                                            loanId = loan.id,
                                        )
                                    )
                                )
                            },
                            modifier = Modifier.fillMaxWidth().testTag("loan.recordRepayment"),
                        )
                    }
                }
            } else null,
    ) {
        if (detail == null || loan == null) {
            Text(
                stringResource(R.string.add_loan_gone),
                style = PbTextStyles.Body,
                color = PbColors.Text.Secondary,
            )
            return@PbPinnedHeaderScreen
        }
        val name = people.first(loan.friendId)
        PbAmountHero(
            if (lent) stringResource(R.string.add_you_lent, name)
            else stringResource(R.string.add_you_borrowed, name),
            Money.format(loan.amount, loan.currency),
            detail.meta,
            PbHeroLeading.Single(people.avatar(loan.friendId)),
            Modifier.testTag("loan.hero"),
            status = stringResource(R.string.add_paid_back).takeIf { paidBack },
        )
        PbLoanProgressCard(
            original = Money.format(loan.amount, loan.currency),
            paid = Money.format(detail.paid, loan.currency),
            remaining = Money.format(detail.remaining, loan.currency),
            progress = if (loan.amount == 0L) 0f else detail.paid.toFloat() / loan.amount,
            caption = detail.paidBackOn ?: "${detail.percentPaid}% paid back",
            modifier = Modifier.testTag("loan.progress"),
            paidBack = paidBack,
        )
        Column(verticalArrangement = Arrangement.spacedBy(PbSpace.S12)) {
            val plan = loan.installments
            PbSectionHeader(
                if (plan != null) LoanSchedule.header(plan.count, plan.frequency)
                else stringResource(R.string.add_due)
            )
            Column(verticalArrangement = Arrangement.spacedBy(PbSpace.S8)) {
                PbCard {
                    Column(Modifier.padding(horizontal = PbSpace.S16)) {
                        detail.rows.forEachIndexed { index, row ->
                            val paid = row.installment.paidOn != null
                            val due = row.installment.due
                            PbActivityRow(
                                PbAvatarContent.Symbol(
                                    if (paid) PbIcon.CheckCircle else PbIcon.Calendar
                                ),
                                title =
                                    if (plan != null)
                                        stringResource(R.string.add_installment, index + 1)
                                    else loan.title,
                                subtitle =
                                    if (row.overdue && due != null) Dates.dueLabel(due)
                                    else row.label.ifEmpty { noDueDate },
                                amount = Money.format(row.installment.amount, loan.currency),
                                badge = row.label.takeIf { row.overdue },
                                badgeStyle = PbBadgeStyle.Overdue.takeIf { row.overdue },
                                showDivider = index < detail.rows.lastIndex,
                                surface = PbRowSurface.OnCard,
                                testTag = "loan.installment.${index + 1}",
                            )
                        }
                    }
                }
                if (overdue && detail.lastReminder != null) {
                    Text(
                        detail.lastReminder,
                        Modifier.testTag("loan.lastReminder"),
                        style = PbTextStyles.Footnote,
                        color = PbColors.Text.Secondary,
                    )
                }
            }
        }
    }
}
