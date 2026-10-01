package app.paybak.paybak.feature.recurring

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import app.paybak.paybak.R
import app.paybak.paybak.data.ledger.actions.deleteRecurringRule
import app.paybak.paybak.data.ledger.actions.updateRecurringRule
import app.paybak.paybak.data.ledger.collectSnapshot
import app.paybak.paybak.domain.calc.DraftRow
import app.paybak.paybak.domain.calc.RuleRow
import app.paybak.paybak.domain.calc.recurring
import app.paybak.paybak.domain.calc.scheduleText
import app.paybak.paybak.domain.format.Money
import app.paybak.paybak.domain.model.ExpenseDraft
import app.paybak.paybak.domain.model.Frequency
import app.paybak.paybak.domain.model.ME
import app.paybak.paybak.domain.model.RepeatRule
import app.paybak.paybak.domain.model.newId
import app.paybak.paybak.feature.settings.leave
import app.paybak.paybak.navigation.AddExpenseArgs
import app.paybak.paybak.navigation.LocalLedger
import app.paybak.paybak.navigation.LocalMainNavigator
import app.paybak.paybak.navigation.PickRequest
import app.paybak.paybak.navigation.Route
import app.paybak.paybak.navigation.RouteResult
import app.paybak.paybak.navigation.RouteResultEffect
import app.paybak.paybak.ui.components.PbActivityRow
import app.paybak.paybak.ui.components.PbAvatar
import app.paybak.paybak.ui.components.PbAvatarContent
import app.paybak.paybak.ui.components.PbBadge
import app.paybak.paybak.ui.components.PbBadgeStyle
import app.paybak.paybak.ui.components.PbButton
import app.paybak.paybak.ui.components.PbButtonSize
import app.paybak.paybak.ui.components.PbButtonStyle
import app.paybak.paybak.ui.components.PbHeaderAction
import app.paybak.paybak.ui.components.PbPushHeader
import app.paybak.paybak.ui.components.PbRowSurface
import app.paybak.paybak.ui.components.PbScreenFrame
import app.paybak.paybak.ui.components.PbSectionHeader
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbLayout
import app.paybak.paybak.ui.theme.PbShapes
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles

/**
 * The `recurring` route (recurringFlat302; insights §5.2): a group's repeating expenses. Drafts of
 * variable rules wait under "Needs your amount" (Enter amount turns one into an expense); the rules
 * list their schedule, who pays and the next date. Tapping a rule opens its Repeat sheet (Never
 * stops it); Add starts a monthly expense in the group. Tagged `screen.recurring`, parts
 * `recurring.*`.
 */
@Composable
fun RecurringScreen(route: Route.Recurring) {
    val navigator = LocalMainNavigator.current
    val ledger = LocalLedger.current
    val snapshot by ledger.collectSnapshot()
    val view = snapshot.view
    val group = view.group(route.groupId)
    if (group == null) {
        LaunchedEffect(Unit) { navigator.leave(route) }
        return
    }
    val summary = remember(snapshot, route.groupId) { view.recurring(route.groupId) }
    val request = PickRequest(rememberSaveable { newId() })
    var editing by rememberSaveable { mutableStateOf<String?>(null) }
    val stopped = stringResource(R.string.insights_recurring_stopped)
    RouteResultEffect(request.id) { result ->
        val ruleId = editing ?: return@RouteResultEffect
        val repeat = (result as? RouteResult.Repeat)?.rule
        val rule = summary.rules.firstOrNull { it.rule.id == ruleId }?.rule ?: return@RouteResultEffect
        if (repeat == null) {
            ledger.deleteRecurringRule(ruleId)
            navigator.toast(stopped.format(rule.title))
        } else {
            ledger.updateRecurringRule(ruleId) {
                // A rule with no amount of its own can only make drafts.
                it.copy(
                    frequency = repeat.frequency,
                    anchorDate = repeat.anchorDate,
                    variable = repeat.variable || it.amount == null,
                )
            }
        }
        editing = null
    }
    PbScreenFrame(id = route.info.id) {
        PbPushHeader(
            stringResource(R.string.insights_recurring_title),
            onBack = { navigator.leave(route) },
            modifier = Modifier.padding(horizontal = PbLayout.ScreenMargin),
            action =
                PbHeaderAction.Text(
                    stringResource(R.string.pb_add),
                    onClick = {
                    val today = ledger.clock.today()
                    navigator.open(
                        Route.AddExpense(
                            AddExpenseArgs(
                                draft =
                                    ExpenseDraft.equal(group.memberIds)
                                        .copy(
                                            groupId = group.id,
                                            repeat = RepeatRule(Frequency.Monthly, today),
                                        )
                            )
                        )
                    )
                    },
                ),
            testTag = "recurring",
            actionTag = "add",
        )
        Column(
            Modifier.fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = PbLayout.ScreenMargin)
                .padding(top = PbSpace.S8, bottom = PbSpace.S24)
        ) {
            Text(
                stringResource(R.string.insights_recurring_intro, group.name),
                style = PbTextStyles.Footnote,
                color = PbColors.Text.Secondary,
            )
            if (summary.drafts.isNotEmpty()) {
                PbSectionHeader(
                    stringResource(R.string.insights_recurring_needs_amount),
                    Modifier.padding(top = PbSpace.S24),
                )
                Column(
                    Modifier.padding(top = PbSpace.S8),
                    verticalArrangement = Arrangement.spacedBy(PbSpace.S8),
                ) {
                    summary.drafts.forEach { draft ->
                        DraftRowView(draft) {
                            navigator.open(Route.EnterDraftAmount(draft.draft.id))
                        }
                    }
                }
                Text(
                    stringResource(R.string.insights_recurring_drafts_note),
                    Modifier.padding(top = PbSpace.S8),
                    style = PbTextStyles.Footnote,
                    color = PbColors.Text.Secondary,
                )
            }
            PbSectionHeader(
                stringResource(R.string.insights_recurring_rules),
                Modifier.padding(top = PbSpace.S24),
            )
            if (summary.rules.isEmpty()) {
                Text(
                    stringResource(R.string.insights_recurring_empty, group.name),
                    Modifier.padding(top = PbSpace.S8),
                    style = PbTextStyles.Footnote,
                    color = PbColors.Text.Tertiary,
                )
            } else {
                Column(
                    Modifier.padding(top = PbSpace.S8)
                        .fillMaxWidth()
                        .background(PbColors.Bg.Card, PbShapes.Card)
                        .padding(horizontal = PbSpace.S16, vertical = PbSpace.S4)
                ) {
                    summary.rules.forEachIndexed { i, row ->
                        RuleRowView(row, showDivider = i < summary.rules.lastIndex) {
                            editing = row.rule.id
                            navigator.open(
                                Route.RepeatRule(
                                    request,
                                    RepeatRule(row.rule.frequency, row.rule.anchorDate, row.rule.variable),
                                    startDate = row.rule.lastOccurrence ?: row.rule.startDate,
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

/** "Cooking gas · September draft · 28 Sep" with its Draft badge and Enter amount. */
@Composable
private fun DraftRowView(row: DraftRow, onEnterAmount: () -> Unit) {
    Row(
        Modifier.fillMaxWidth()
            .testTag("recurring.draft.${row.rule.id}")
            .background(PbColors.Bg.Card, PbShapes.Card)
            .padding(PbSpace.S12),
        horizontalArrangement = Arrangement.spacedBy(PbSpace.S8),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PbAvatar(PbAvatarContent.Symbol(ruleIcon(row.rule.title, row.rule.category)), onCard = true)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(PbSpace.S6)) {
            Text(
                buildAnnotatedString {
                    withStyle(PbTextStyles.Headline.toSpanStyle().copy(color = PbColors.Text.Primary)) {
                        append(row.rule.title)
                    }
                    append("\n")
                    withStyle(PbTextStyles.Footnote.toSpanStyle().copy(color = PbColors.Text.Secondary)) {
                        append(row.label)
                    }
                }
            )
            PbBadge(stringResource(R.string.insights_draft), style = PbBadgeStyle.OnCard)
        }
        PbButton(
            stringResource(R.string.insights_enter_amount),
            onClick = onEnterAmount,
            modifier = Modifier.testTag("recurring.draft.${row.rule.id}.enterAmount"),
            style = PbButtonStyle.OnCard,
            size = PbButtonSize.Small,
        )
    }
}

/** "Rent · Monthly on the 1st · Paid by you · Next Thu 1 Oct" and its amount (or Varies). */
@Composable
private fun RuleRowView(row: RuleRow, showDivider: Boolean, onClick: () -> Unit) {
    val snapshot by LocalLedger.current.collectSnapshot()
    val rule = row.rule
    val payer =
        if (rule.payerId == ME) stringResource(R.string.insights_paid_by_you)
        else stringResource(R.string.insights_paid_by, snapshot.view.first(rule.payerId))
    PbActivityRow(
        leading = PbAvatarContent.Symbol(ruleIcon(rule.title, rule.category)),
        title = rule.title,
        subtitle = "${scheduleText(rule)}\n$payer",
        detail = row.nextLabel,
        amount =
            if (rule.variable || rule.amount == null) stringResource(R.string.insights_varies)
            else Money.format(rule.amount, rule.currency),
        showDivider = showDivider,
        surface = PbRowSurface.OnCard,
        onClick = onClick,
        testTag = "recurring.rule.${rule.id}",
    )
}
