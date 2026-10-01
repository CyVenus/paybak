package app.paybak.paybak.feature.projects

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import app.paybak.paybak.R
import app.paybak.paybak.data.ledger.actions.addMembers
import app.paybak.paybak.data.ledger.actions.closeProject
import app.paybak.paybak.data.ledger.collectSnapshot
import app.paybak.paybak.data.ledger.lanes.setBudget
import app.paybak.paybak.data.ledger.lanes.setContribution
import app.paybak.paybak.data.ledger.lanes.setPool
import app.paybak.paybak.domain.addrecord.AmountEntry
import app.paybak.paybak.domain.addrecord.ContributionDraft
import app.paybak.paybak.domain.calc.LedgerView
import app.paybak.paybak.domain.model.Contribution
import app.paybak.paybak.domain.model.ContributionRule
import app.paybak.paybak.domain.model.Group
import app.paybak.paybak.domain.model.LedgerJson
import app.paybak.paybak.domain.model.ME
import app.paybak.paybak.domain.model.ProjectStatus
import app.paybak.paybak.domain.model.newId
import app.paybak.paybak.domain.projects.ContributionEdit
import app.paybak.paybak.feature.groups.rememberAvatars
import app.paybak.paybak.navigation.LocalLedger
import app.paybak.paybak.navigation.LocalMainNavigator
import app.paybak.paybak.navigation.PickMode
import app.paybak.paybak.navigation.PickRequest
import app.paybak.paybak.navigation.Route
import app.paybak.paybak.navigation.RouteResult
import app.paybak.paybak.navigation.RouteResultEffect
import app.paybak.paybak.navigation.rememberDebugStartScreen
import app.paybak.paybak.service.HapticKind
import app.paybak.paybak.service.rememberHaptics
import app.paybak.paybak.ui.components.PbAlert
import app.paybak.paybak.ui.components.PbAlertAction
import app.paybak.paybak.ui.components.PbAmountEditor
import app.paybak.paybak.ui.components.PbCard
import app.paybak.paybak.ui.components.PbPersonRow
import app.paybak.paybak.ui.components.PbPersonRowSize
import app.paybak.paybak.ui.components.PbPersonTrailing
import app.paybak.paybak.ui.components.PbSectionHeader
import app.paybak.paybak.ui.components.PbSegmentedControl
import app.paybak.paybak.ui.components.PbSettingRow
import app.paybak.paybak.ui.components.PbSettingTrailing
import app.paybak.paybak.ui.components.PbTextField
import app.paybak.paybak.ui.components.amountInput
import app.paybak.paybak.ui.components.groups.PbPushedPage
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbLayout
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles

private val ContributionEditSaver: Saver<ContributionEdit, String> =
    Saver(
        save = { LedgerJson.encodeToString(ContributionEdit.serializer(), it) },
        restore = { LedgerJson.decodeFromString(ContributionEdit.serializer(), it) },
    )

/**
 * The `projectSettings` route (screens-projects §6): the contribution rule with each member's share
 * (Equal read-only; Percent and Fixed editable, applied once they add up), Add member, the budget,
 * Collect money upfront and Close project, which asks first. Every change saves as it's made.
 */
@Composable
fun ProjectSettingsScreen(route: Route.ProjectSettings) {
    val navigator = LocalMainNavigator.current
    val ledger = LocalLedger.current
    val snapshot by ledger.collectSnapshot()
    val view = snapshot.view
    val projectId = route.groupId
    val project = view.group(projectId)?.takeIf { it.isProject }
    val haptics = rememberHaptics()
    val start = rememberDebugStartScreen("projectCloseAlert")
    var closing by rememberSaveable { mutableStateOf(start != null) }
    var budget by rememberSaveable {
        mutableStateOf(AmountEntry.text(project?.project?.budget ?: 0, project?.currency ?: "INR"))
    }
    val peopleRequest = rememberSaveable { newId() }
    val addMembersTitle = stringResource(R.string.projects_add_members)
    RouteResultEffect(peopleRequest) { result ->
        if (result is RouteResult.People) ledger.addMembers(projectId, result.personIds - ME)
    }

    PbPushedPage(
        id = "projectSettings",
        onBack = { navigator.back() },
        title = stringResource(R.string.projects_settings),
    ) {
        val info = project?.project ?: return@PbPushedPage
        Column(verticalArrangement = Arrangement.spacedBy(PbLayout.SectionGap)) {
            ContributionSection(
                project,
                view,
                onSave = { ledger.setContribution(projectId, it) },
                onAddMember = {
                    navigator.open(
                        Route.PickPeople(
                            PickRequest(peopleRequest),
                            mode = PickMode.Multi,
                            selected = project.memberIds - ME,
                            title = addMembersTitle,
                            includesYou = false,
                        )
                    )
                },
            )
            PbTextField(
                if (budget.isEmpty()) "" else AmountEntry.display(budget, project.currency),
                { typed ->
                    val decimals = AmountEntry.allowsDecimals(project.currency)
                    AmountEntry.accept(amountInput(typed, decimals))?.let { text ->
                        budget = text
                        val minor = AmountEntry.minor(text, project.currency)
                        ledger.setBudget(projectId, minor.takeIf { it > 0 })
                    }
                },
                label = stringResource(R.string.projects_budget),
                placeholder = AmountEntry.placeholder(project.currency),
                helper = stringResource(R.string.projects_budget_helper),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                fieldModifier = Modifier.testTag("projectSettings.budget"),
            )
            Column(verticalArrangement = Arrangement.spacedBy(PbSpace.S8)) {
                PbCard {
                    PbSettingRow(
                        stringResource(R.string.projects_pool),
                        Modifier.testTag("projectSettings.pool"),
                        trailing =
                            PbSettingTrailing.Toggle(info.pool) { ledger.setPool(projectId, it) },
                        icon = PbIcon.Wallet,
                        showDivider = false,
                    )
                }
                Text(
                    stringResource(R.string.projects_pool_helper),
                    style = PbTextStyles.Footnote,
                    color = PbColors.Text.Secondary,
                )
            }
            if (info.status == ProjectStatus.Active) {
                PbCard {
                    PbSettingRow(
                        stringResource(R.string.projects_close),
                        Modifier.testTag("projectSettings.close"),
                        trailing = PbSettingTrailing.None,
                        onClick = { closing = true },
                        icon = PbIcon.Lock,
                        showDivider = false,
                    )
                }
            }
        }
    }

    if (closing && project != null) {
        PbAlert(
            title = stringResource(R.string.projects_close_title, project.name),
            message = stringResource(R.string.projects_close_message),
            cancelLabel = stringResource(R.string.projects_cancel),
            actionLabel = stringResource(R.string.projects_close),
            onCancel = { closing = false },
            onAction = {
                closing = false
                haptics.perform(HapticKind.Success)
                ledger.closeProject(projectId)
                navigator.back()
            },
            action = PbAlertAction.Primary,
            testTag = "projectSettings.closeAlert",
        )
    }
}

/**
 * Contribution rule: the segments, the helper (red with what's left while Percent or Fixed don't
 * add up) and the members card with each share and Add member. A rule that adds up is saved through
 * [onSave] at once; until then the saved one stays in force.
 */
@Composable
private fun ContributionSection(
    project: Group,
    view: LedgerView,
    onSave: (Contribution) -> Unit,
    onAddMember: () -> Unit,
) {
    val haptics = rememberHaptics()
    val info = project.project ?: return
    val members = project.memberIds
    val currency = project.currency
    var edit by
        rememberSaveable(stateSaver = ContributionEditSaver) {
            mutableStateOf(ContributionEdit.of(info.contribution, members, currency))
        }
    val current = edit
    val check = current.check(members, info.budget, currency)
    fun update(next: ContributionEdit) {
        edit = next
        if (next.check(members, info.budget, currency).valid) {
            onSave(next.contribution(members, currency))
        }
    }
    val rules = ContributionRule.entries
    val avatars = rememberAvatars(view, members)
    Column(verticalArrangement = Arrangement.spacedBy(PbSpace.S12)) {
        Column(verticalArrangement = Arrangement.spacedBy(PbSpace.S8)) {
            PbSectionHeader(stringResource(R.string.projects_contribution_rule))
            PbSegmentedControl(
                options = rules.map { stringResource(it.labelRes()) },
                selectedIndex = rules.indexOf(current.rule),
                onSelect = { index ->
                    val rule = rules[index]
                    if (rule == current.rule) return@PbSegmentedControl
                    haptics.perform(HapticKind.Selection)
                    update(
                        if (rule == info.contribution.rule) {
                            ContributionEdit.of(info.contribution, members, currency)
                        } else {
                            ContributionEdit.prefill(
                                rule,
                                members,
                                info.budget,
                                view.projectSpent(project.id),
                                currency,
                            )
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                segmentTags = rules.map { "projectSettings.rule.${it.name.lowercase()}" },
            )
            Text(
                check.helper,
                Modifier.testTag("projectSettings.ruleHelper"),
                style = PbTextStyles.Footnote,
                color = if (check.valid) PbColors.Text.Secondary else PbColors.Text.Destructive,
            )
        }
        PbCard {
            members.forEachIndexed { index, id ->
                PbPersonRow(
                    name = view.first(id),
                    avatar = avatars[index],
                    modifier = Modifier.testTag("projectSettings.member.$id"),
                    trailing =
                        when (current.rule) {
                            ContributionRule.Equal ->
                                PbPersonTrailing.Amount(ContributionDraft.equalShare(members.size))
                            else ->
                                PbPersonTrailing.Field(
                                    PbAmountEditor(
                                        value = current.typed[id].orEmpty(),
                                        onValueChange = { text ->
                                            AmountEntry.accept(amountInput(text, true))?.let {
                                                update(
                                                    current.copy(typed = current.typed + (id to it))
                                                )
                                            }
                                        },
                                        prefix =
                                            if (current.rule == ContributionRule.Fixed) {
                                                AmountEntry.prefix(currency)
                                            } else {
                                                ""
                                            },
                                        suffix =
                                            if (current.rule == ContributionRule.Percent) "%"
                                            else "",
                                        decimal = true,
                                    ),
                                    testTag = "projectSettings.member.$id.value",
                                )
                        },
                    size = PbPersonRowSize.Compact,
                )
            }
            PbSettingRow(
                stringResource(R.string.projects_add_member),
                Modifier.testTag("projectSettings.addMember"),
                onClick = onAddMember,
                icon = PbIcon.UserAdd,
                showDivider = false,
            )
        }
    }
}

private fun ContributionRule.labelRes(): Int =
    when (this) {
        ContributionRule.Equal -> R.string.projects_rule_equal
        ContributionRule.Percent -> R.string.projects_rule_percent
        ContributionRule.Fixed -> R.string.projects_rule_fixed
    }
