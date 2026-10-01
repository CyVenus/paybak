package app.paybak.paybak.feature.recurring

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import app.paybak.paybak.R
import app.paybak.paybak.data.ledger.actions.enterDraftAmount
import app.paybak.paybak.data.ledger.collectSnapshot
import app.paybak.paybak.domain.addrecord.AmountEntry
import app.paybak.paybak.domain.format.Dates
import app.paybak.paybak.domain.model.ME
import app.paybak.paybak.navigation.LocalLedger
import app.paybak.paybak.navigation.LocalMainNavigator
import app.paybak.paybak.navigation.Route
import app.paybak.paybak.service.HapticKind
import app.paybak.paybak.service.rememberHaptics
import app.paybak.paybak.ui.components.PbAmountField
import app.paybak.paybak.ui.components.PbBadge
import app.paybak.paybak.ui.components.PbBadgeStyle
import app.paybak.paybak.ui.components.PbCard
import app.paybak.paybak.ui.components.PbModalHeader
import app.paybak.paybak.ui.components.PbScreenFrame
import app.paybak.paybak.ui.components.PbSettingRow
import app.paybak.paybak.ui.components.PbSettingTrailing
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbLayout
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles

/**
 * The `enterDraftAmount` route (recurringEnterAmount; insights §5.4): turns a variable rule's draft
 * into an expense. The amount is focused on open; Add (enabled once it's above zero) saves the
 * expense on the draft's date, split as the rule says, then closes with "Expense added". Tagged
 * `screen.enterDraftAmount`, parts `enterAmount.*`.
 */
@Composable
fun EnterAmountScreen(route: Route.EnterDraftAmount) {
    val navigator = LocalMainNavigator.current
    val ledger = LocalLedger.current
    val haptics = rememberHaptics()
    val snapshot by ledger.collectSnapshot()
    val view = snapshot.view
    val draft = view.ledger.drafts.firstOrNull { it.id == route.draftId }
    val rule = draft?.let { d -> view.ledger.recurringRules.firstOrNull { it.id == d.ruleId } }
    // Already entered (or gone): nothing left to do here.
    if (draft == null || rule == null || draft.expenseId != null) {
        LaunchedEffect(Unit) { if (navigator.screen.route == route) navigator.dismissModal() }
        return
    }
    var amount by rememberSaveable { mutableStateOf("") }
    val minor = AmountEntry.minor(amount, rule.currency)
    val added = stringResource(R.string.insights_expense_added)
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { focus.requestFocus() }
    PbScreenFrame(id = route.info.id) {
        PbModalHeader(
            rule.title,
            onClose = navigator::dismissModal,
            modifier = Modifier.padding(horizontal = PbLayout.ScreenMargin),
            action = stringResource(R.string.pb_add),
            actionEnabled = minor > 0,
            onAction = {
                ledger.enterDraftAmount(draft.id, minor)
                haptics.perform(HapticKind.Success)
                navigator.dismissModal()
                navigator.toast(added)
            },
            testTag = "enterAmount",
        )
        Column(Modifier.padding(horizontal = PbLayout.ScreenMargin)) {
            Row(
                Modifier.padding(top = PbSpace.S8),
                horizontalArrangement = Arrangement.spacedBy(PbSpace.S8),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PbBadge(stringResource(R.string.insights_draft), style = PbBadgeStyle.Muted)
                Text(
                    listOfNotNull(
                            rule.groupId?.let { view.group(it)?.name },
                            Dates.monthName(draft.occurrenceDate.month),
                        )
                        .joinToString(" · "),
                    style = PbTextStyles.Footnote,
                    color = PbColors.Text.Secondary,
                )
            }
            PbAmountField(
                value = amount,
                onValueChange = { text -> AmountEntry.accept(text)?.let { amount = it } },
                formatted = AmountEntry.display(amount, rule.currency),
                placeholder = AmountEntry.placeholder(rule.currency),
                currency = rule.currency,
                onCurrencyClick = {},
                modifier = Modifier.padding(top = PbSpace.S16),
                helper = stringResource(R.string.insights_draft_helper),
                allowDecimals = AmountEntry.allowsDecimals(rule.currency),
                fieldModifier = Modifier.focusRequester(focus),
                testTag = "enterAmount",
            )
            PbCard(Modifier.padding(top = PbSpace.S16)) {
                PbSettingRow(
                    stringResource(R.string.insights_paid_by_title),
                    trailing = PbSettingTrailing.None,
                    icon = PbIcon.Wallet,
                    value =
                        if (rule.payerId == ME) stringResource(R.string.insights_you)
                        else view.first(rule.payerId),
                )
                PbSettingRow(
                    stringResource(R.string.insights_split),
                    trailing = PbSettingTrailing.None,
                    icon = PbIcon.Split,
                    value =
                        stringResource(R.string.insights_split_equally, rule.split.personIds.size),
                )
                PbSettingRow(
                    stringResource(R.string.insights_date),
                    trailing = PbSettingTrailing.None,
                    icon = PbIcon.Calendar,
                    value = Dates.day(draft.occurrenceDate),
                    showDivider = false,
                )
            }
        }
    }
}
