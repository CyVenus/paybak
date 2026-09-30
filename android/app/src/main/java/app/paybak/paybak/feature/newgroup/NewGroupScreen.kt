package app.paybak.paybak.feature.newgroup

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.paybak.paybak.R
import app.paybak.paybak.data.ledger.actions.addGroup
import app.paybak.paybak.domain.actions.LedgerRuleException
import app.paybak.paybak.domain.addrecord.AmountEntry
import app.paybak.paybak.domain.addrecord.ContributionDraft
import app.paybak.paybak.domain.format.Money
import app.paybak.paybak.domain.model.ContributionRule
import app.paybak.paybak.domain.model.GroupType
import app.paybak.paybak.domain.model.ME
import app.paybak.paybak.domain.model.newId
import app.paybak.paybak.feature.pickers.PeopleDirectory
import app.paybak.paybak.feature.pickers.rememberLedgerPhoto
import app.paybak.paybak.feature.pickers.rememberPeopleDirectory
import app.paybak.paybak.feature.pickers.rememberPhotoPicker
import app.paybak.paybak.navigation.LocalLedger
import app.paybak.paybak.navigation.LocalMainNavigator
import app.paybak.paybak.navigation.LocalProfileStore
import app.paybak.paybak.navigation.NewGroupMode
import app.paybak.paybak.navigation.PickRequest
import app.paybak.paybak.navigation.Route
import app.paybak.paybak.navigation.RouteResult
import app.paybak.paybak.navigation.RouteResultEffect
import app.paybak.paybak.navigation.rememberDebugStartScreen
import app.paybak.paybak.service.HapticKind
import app.paybak.paybak.service.rememberHaptics
import app.paybak.paybak.ui.components.InlineAmountField
import app.paybak.paybak.ui.components.PbAlert
import app.paybak.paybak.ui.components.PbAmountEditor
import app.paybak.paybak.ui.components.PbCard
import app.paybak.paybak.ui.components.PbCategoryChip
import app.paybak.paybak.ui.components.PbModalHeader
import app.paybak.paybak.ui.components.PbPersonRow
import app.paybak.paybak.ui.components.PbPersonRowSize
import app.paybak.paybak.ui.components.PbPersonTrailing
import app.paybak.paybak.ui.components.PbReceiptThumbnail
import app.paybak.paybak.ui.components.PbSectionHeader
import app.paybak.paybak.ui.components.PbSegmentedControl
import app.paybak.paybak.ui.components.PbSettingRow
import app.paybak.paybak.ui.components.PbSettingTrailing
import app.paybak.paybak.ui.components.PbTextField
import app.paybak.paybak.ui.components.addrecord.PbPinnedHeaderScreen
import app.paybak.paybak.ui.components.amountInput
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.icons.PbIconImage
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbShapes
import app.paybak.paybak.ui.theme.PbSize
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles

/** Group types in chip order, with their labels and tags (record-lend-group §6.2). */
private val Types =
    listOf(
        Triple(GroupType.Trip, R.string.add_type_trip, "trip"),
        Triple(GroupType.Home, R.string.add_type_home, "home"),
        Triple(GroupType.Friends, R.string.add_type_friends, "friends"),
        Triple(GroupType.Other, R.string.add_type_other, "other"),
    )
private val Rules = listOf(ContributionRule.Equal, ContributionRule.Percent, ContributionRule.Fixed)

/**
 * The `newGroup` route (`newGroup`, `newGroupProject`; record-lend-group §6): one draft behind the
 * Group | Project switch. A group has a type, members, currency and Simplify debts (on); a project
 * adds a description, cover photo, budget and contribution rule. Create opens it on the Groups tab
 * with "Group created" / "Project created".
 */
@Composable
fun NewGroupScreen(route: Route.NewGroup) {
    val navigator = LocalMainNavigator.current
    val ledger = LocalLedger.current
    val profile by LocalProfileStore.current.profile.collectAsState()
    val people = rememberPeopleDirectory()
    val haptics = rememberHaptics()
    val focusManager = LocalFocusManager.current
    val start = rememberDebugStartScreen("newGroup", "newGroupProject")
    val initial =
        rememberSaveable(stateSaver = GroupForm.Saver) {
            val blank =
                GroupForm(
                    project = route.mode == NewGroupMode.Project,
                    currency = profile.defaultCurrency,
                )
            mutableStateOf(
                if (start != null) {
                    // The Figma prefill (06-17, 06-18): Weekend Trek with Esha, Dev and Kabir.
                    blank.copy(
                        name = "Weekend Trek",
                        type = GroupType.Trip,
                        members = listOf("p-esha", "p-dev", "p-kabir"),
                    )
                } else {
                    blank
                }
            )
        }
    var form by rememberSaveable(stateSaver = GroupForm.Saver) { mutableStateOf(initial.value) }
    var discarding by rememberSaveable { mutableStateOf(false) }
    val addPeople = stringResource(R.string.add_add_people)
    val requestId = rememberSaveable { newId() }
    RouteResultEffect("$requestId.people") { result ->
        (result as? RouteResult.People)?.let { picked ->
            form = form.copy(members = picked.personIds.filter { it != ME })
        }
    }
    RouteResultEffect("$requestId.currency") { result ->
        (result as? RouteResult.Currency)?.let { form = form.copy(currency = it.code) }
    }
    val pickCover = rememberPhotoPicker { form = form.copy(cover = it) }
    val cover = rememberLedgerPhoto(form.cover)

    fun close() {
        if (form != initial.value) discarding = true else navigator.dismissModal()
    }
    fun create() {
        try {
            val id = ledger.addGroup(form.toDraft())
            haptics.perform(HapticKind.Success)
            navigator.didCreateGroup(id, form.project)
        } catch (error: LedgerRuleException) {
            haptics.perform(HapticKind.Warning)
            navigator.toast(error.message.orEmpty())
        }
    }
    // A route sheet over the form (currency) closes first.
    BackHandler(enabled = navigator.sheet == null, onBack = ::close)

    PbPinnedHeaderScreen(
        testTag = "screen.newGroup",
        header = {
            PbModalHeader(
                stringResource(R.string.add_new_group),
                onClose = ::close,
                action = stringResource(R.string.add_create),
                actionEnabled = form.canCreate,
                onAction = ::create,
                testTag = "newGroup",
                actionTag = "create",
            )
        },
    ) {
        PbSegmentedControl(
            listOf(
                stringResource(R.string.add_mode_group),
                stringResource(R.string.add_mode_project),
            ),
            selectedIndex = if (form.project) 1 else 0,
            onSelect = {
                haptics.perform(HapticKind.Selection)
                form = form.copy(project = it == 1)
            },
            modifier = Modifier.fillMaxWidth(),
            segmentTags = listOf("newGroup.mode.group", "newGroup.mode.project"),
        )
        Column(verticalArrangement = Arrangement.spacedBy(PbSpace.S24)) {
            Column(verticalArrangement = Arrangement.spacedBy(PbSpace.S16)) {
                PbTextField(
                    form.name,
                    { form = form.copy(name = it.take(MAX_NAME)) },
                    label = stringResource(R.string.add_name),
                    placeholder =
                        if (form.project) stringResource(R.string.add_project_name_placeholder)
                        else stringResource(R.string.add_group_name_placeholder),
                    keyboardOptions =
                        KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Done,
                        ),
                    fieldModifier = Modifier.testTag("newGroup.name"),
                )
                if (form.project) {
                    PbTextField(
                        form.description,
                        { form = form.copy(description = it.take(MAX_DESCRIPTION)) },
                        label = stringResource(R.string.add_description),
                        placeholder = stringResource(R.string.add_whats_it_for),
                        keyboardOptions =
                            KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                        fieldModifier = Modifier.testTag("newGroup.description"),
                    )
                    PbCard {
                        PbSettingRow(
                            if (form.cover != null) stringResource(R.string.add_cover_photo)
                            else stringResource(R.string.add_add_cover_photo),
                            Modifier.testTag("newGroup.cover"),
                            icon = PbIcon.Camera,
                            value =
                                if (form.cover != null) stringResource(R.string.add_added)
                                else null,
                            valueLeading =
                                form.cover?.let {
                                    { PbReceiptThumbnail(Modifier.size(32.dp), photo = cover) }
                                },
                            onClick = pickCover,
                            showDivider = false,
                        )
                    }
                } else {
                    TypePicker(form.type) {
                        haptics.perform(HapticKind.Selection)
                        form = form.copy(type = it)
                    }
                }
            }
            if (form.project) {
                PbTextField(
                    form.budget,
                    { text ->
                        AmountEntry.accept(amountInput(text, true))?.let {
                            form = form.copy(budget = it)
                        }
                    },
                    label = stringResource(R.string.add_budget),
                    placeholder = AmountEntry.placeholder(form.currency),
                    helper = stringResource(R.string.add_budget_helper),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    fieldModifier = Modifier.testTag("newGroup.budget"),
                )
                ContributionPicker(form.rule, form.contributionValid) {
                    haptics.perform(HapticKind.Selection)
                    form = form.copy(rule = it, shares = emptyMap())
                }
            }
            Members(
                form,
                people,
                onRemove = { id -> form = form.copy(members = form.members - id) },
                onShare = { id, text -> form = form.copy(shares = form.shares + (id to text)) },
                onAdd = {
                    focusManager.clearFocus()
                    navigator.open(
                        Route.PickPeople(
                            PickRequest("$requestId.people"),
                            selected = form.members,
                            title = addPeople,
                            includesYou = false,
                        )
                    )
                },
            )
            PbCard {
                PbSettingRow(
                    stringResource(R.string.add_currency),
                    Modifier.testTag("newGroup.currency"),
                    icon = PbIcon.Exchange,
                    value = "${form.currency} ${Money.currency(form.currency).symbol}",
                    onClick = {
                        focusManager.clearFocus()
                        navigator.open(
                            Route.PickCurrency(
                                PickRequest("$requestId.currency"),
                                selected = form.currency,
                            )
                        )
                    },
                    showDivider = !form.project,
                )
                if (!form.project) {
                    PbSettingRow(
                        stringResource(R.string.add_simplify_debts),
                        Modifier.testTag("newGroup.simplify"),
                        icon = PbIcon.Shuffle,
                        subtitle = stringResource(R.string.add_simplify_debts_subtitle),
                        trailing =
                            PbSettingTrailing.Toggle(form.simplify) {
                                form = form.copy(simplify = it)
                            },
                        showDivider = false,
                    )
                }
            }
        }
    }
    if (discarding) {
        PbAlert(
            title =
                if (form.project) stringResource(R.string.add_project_discard_title)
                else stringResource(R.string.add_group_discard_title),
            message = stringResource(R.string.add_discard_message),
            cancelLabel = stringResource(R.string.add_keep_editing),
            actionLabel = stringResource(R.string.add_discard),
            onCancel = { discarding = false },
            onAction = {
                discarding = false
                navigator.dismissModal()
            },
            testTag = "newGroup.discardAlert",
        )
    }
}

/** "Type": Trip · Home · Friends · Other, single choice; it sets the group's tile icon. */
@Composable
private fun TypePicker(selected: GroupType?, onPick: (GroupType) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(PbSpace.S4)) {
        Text(
            stringResource(R.string.add_type),
            style = PbTextStyles.Subheadline,
            color = PbColors.Text.Secondary,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(PbSpace.S8)) {
            Types.forEach { (type, label, tag) ->
                PbCategoryChip(
                    stringResource(label),
                    Modifier.testTag("newGroup.type.$tag"),
                    selected = type == selected,
                    onClick = { onPick(type) },
                )
            }
        }
    }
}

/** "Contribution": Equal · Percent · Fixed with its helper, red while the values don't add up. */
@Composable
private fun ContributionPicker(
    rule: ContributionRule,
    valid: Boolean,
    onPick: (ContributionRule) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(PbSpace.S8)) {
        Text(
            stringResource(R.string.add_contribution),
            style = PbTextStyles.Subheadline,
            color = PbColors.Text.Secondary,
        )
        PbSegmentedControl(
            listOf(
                stringResource(R.string.add_contribution_equal),
                stringResource(R.string.add_contribution_percent),
                stringResource(R.string.add_contribution_fixed),
            ),
            selectedIndex = Rules.indexOf(rule),
            onSelect = { onPick(Rules[it]) },
            modifier = Modifier.fillMaxWidth(),
            segmentTags = Rules.map { "newGroup.contribution.${it.name.lowercase()}" },
        )
        Text(
            ContributionDraft.helper(rule),
            style = PbTextStyles.Footnote,
            color = if (valid) PbColors.Text.Secondary else PbColors.Text.Destructive,
        )
    }
}

/**
 * "Members": you first, then each person with ✕ (a group) or their share (a project: "25%" for an
 * equal split, a field for Percent and Fixed), then Add people.
 */
@Composable
private fun Members(
    form: GroupForm,
    people: PeopleDirectory,
    onRemove: (String) -> Unit,
    onShare: (String, String) -> Unit,
    onAdd: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(PbSpace.S4)) {
        PbSectionHeader(stringResource(R.string.add_members))
        PbCard {
            form.everyone.forEachIndexed { index, id ->
                val you = id == ME
                val trailing =
                    when {
                        form.project && form.rule == ContributionRule.Equal ->
                            PbPersonTrailing.Amount(
                                ContributionDraft.equalShare(form.everyone.size)
                            )
                        form.project -> null
                        you -> null
                        else ->
                            PbPersonTrailing.Remove(
                                stringResource(R.string.add_remove, people.first(id))
                            ) {
                                onRemove(id)
                            }
                    }
                Box(Modifier.testTag("newGroup.member.$index")) {
                    PbPersonRow(
                        if (you) stringResource(R.string.add_you) else people.full(id),
                        people.avatar(id),
                        subtitle = people.full(ME).takeIf { you && !form.project },
                        tag = stringResource(R.string.add_guest).takeIf { people.isGuest(id) },
                        trailing = trailing,
                        size = PbPersonRowSize.Compact,
                    )
                    if (form.project && form.rule != ContributionRule.Equal) {
                        val percent = form.rule == ContributionRule.Percent
                        InlineAmountField(
                            PbAmountEditor(
                                value = form.shares[id] ?: "",
                                onValueChange = { onShare(id, amountInput(it, true)) },
                                prefix = if (percent) "" else AmountEntry.prefix(form.currency),
                                suffix = if (percent) "%" else "",
                                decimal = true,
                            ),
                            PbTextStyles.Headline,
                            Modifier.align(Alignment.CenterEnd)
                                .padding(end = PbSpace.S16)
                                .testTag("newGroup.member.$index.share"),
                        )
                    }
                }
            }
            AddPeopleRow(onAdd)
        }
    }
}

/** The Add people row inside the members card: a white tile, the title and a chevron. */
@Composable
private fun AddPeopleRow(onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth()
            .height(56.dp)
            .clickable(onClick = onClick)
            .padding(start = PbSpace.S8, end = PbSpace.S16)
            .testTag("newGroup.addPeople"),
        horizontalArrangement = Arrangement.spacedBy(PbSpace.S8),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(PbSize.Tap).background(PbColors.Bg.Primary, PbShapes.Tile),
            contentAlignment = Alignment.Center,
        ) {
            PbIconImage(PbIcon.UserAdd, contentDescription = null)
        }
        Text(
            stringResource(R.string.add_add_people),
            Modifier.weight(1f),
            style = PbTextStyles.Headline,
            color = PbColors.Text.Primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        PbIconImage(
            PbIcon.ChevronRight,
            contentDescription = null,
            size = PbSize.IconMd,
            tint = PbColors.Icon.Tertiary,
        )
    }
}

private const val MAX_NAME = 40
private const val MAX_DESCRIPTION = 120
