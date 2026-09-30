package app.paybak.paybak.feature.groups

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.paybak.paybak.R
import app.paybak.paybak.data.ledger.actions.addMembers
import app.paybak.paybak.data.ledger.actions.leaveGroup
import app.paybak.paybak.data.ledger.actions.updateGroup
import app.paybak.paybak.data.ledger.collectSnapshot
import app.paybak.paybak.domain.groups.GroupSettingsPage
import app.paybak.paybak.domain.groups.LeaveCheck
import app.paybak.paybak.domain.groups.MemberIdentity
import app.paybak.paybak.domain.groups.groupSettingsPage
import app.paybak.paybak.domain.groups.leaveCheck
import app.paybak.paybak.domain.model.ME
import app.paybak.paybak.domain.model.newId
import app.paybak.paybak.navigation.DateKind
import app.paybak.paybak.navigation.LocalLedger
import app.paybak.paybak.navigation.LocalMainNavigator
import app.paybak.paybak.navigation.LocalProfileStore
import app.paybak.paybak.navigation.PickMode
import app.paybak.paybak.navigation.PickRequest
import app.paybak.paybak.navigation.Route
import app.paybak.paybak.navigation.RouteResult
import app.paybak.paybak.navigation.RouteResultEffect
import app.paybak.paybak.navigation.rememberDebugStartScreen
import app.paybak.paybak.ui.components.PbAlert
import app.paybak.paybak.ui.components.PbAlertAction
import app.paybak.paybak.ui.components.PbAvatarContent
import app.paybak.paybak.ui.components.PbButton
import app.paybak.paybak.ui.components.PbButtonStyle
import app.paybak.paybak.ui.components.PbCard
import app.paybak.paybak.ui.components.PbPersonRow
import app.paybak.paybak.ui.components.PbPersonRowSize
import app.paybak.paybak.ui.components.PbSectionHeader
import app.paybak.paybak.ui.components.PbSettingRow
import app.paybak.paybak.ui.components.PbSettingTrailing
import app.paybak.paybak.ui.components.groups.PbPushedPage
import app.paybak.paybak.ui.components.iconForKey
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles

/** How far `groupLeaveBlocked` scrolls the page before its alert, as Figma draws it (§5.6). */
private val LeaveBlockedScroll = 26.dp

/**
 * The `groupSettings` route (screens-groups §5): name and Settle by, the members (+ Add through
 * the people picker), currency, Simplify debts, Recurring expenses (Pro) and Leave group, which is
 * blocked while your balance there isn't 0 (§5.6) and asks first otherwise.
 */
@Composable
fun GroupSettingsScreen(route: Route.GroupSettings) {
    val navigator = LocalMainNavigator.current
    val ledger = LocalLedger.current
    val snapshot by ledger.collectSnapshot()
    val profile by LocalProfileStore.current.profile.collectAsState()
    val view = snapshot.view
    val groupId = route.groupId
    val me = MemberIdentity(profile.name, profile.upiId.ifEmpty { null }, profile.username)
    val page = remember(snapshot, me) { view.groupSettingsPage(groupId, me) }
    val start = rememberDebugStartScreen("groupLeaveBlocked")
    val scroll = rememberScrollState()
    var renaming by rememberSaveable { mutableStateOf(false) }
    var leaving by rememberSaveable { mutableStateOf(start != null) }
    val scrollOffset = with(LocalDensity.current) { LeaveBlockedScroll.roundToPx() }
    LaunchedEffect(start) { if (start != null) scroll.scrollTo(scrollOffset) }

    val addMembersTitle = stringResource(R.string.groups_add_members)
    val currencyTitle = stringResource(R.string.groups_group_currency)
    val dateRequest = rememberSaveable { newId() }
    val peopleRequest = rememberSaveable { newId() }
    val currencyRequest = rememberSaveable { newId() }
    RouteResultEffect(dateRequest) { result ->
        if (result is RouteResult.Day) ledger.updateGroup(groupId) { it.copy(settleBy = result.day) }
    }
    RouteResultEffect(peopleRequest) { result ->
        if (result is RouteResult.People) ledger.addMembers(groupId, result.personIds - ME)
    }
    RouteResultEffect(currencyRequest) { result ->
        if (result is RouteResult.Currency) ledger.updateGroup(groupId) { it.copy(currency = result.code) }
    }

    PbPushedPage(
        id = "groupSettings",
        onBack = { navigator.back() },
        title = stringResource(R.string.groups_settings),
        scrollState = scroll,
        overlay = {
            if (page != null && renaming) {
                RenameGroupSheet(
                    page.group.name,
                    onSave = { name -> ledger.updateGroup(groupId) { it.copy(name = name) } },
                    onDismiss = { renaming = false },
                )
            }
        },
    ) {
        if (page == null) return@PbPushedPage
        NameCard(
            page,
            onName = { renaming = true },
            onSettleBy = {
                navigator.open(
                    Route.PickDate(
                        PickRequest(dateRequest),
                        kind = DateKind.DueDate,
                        selected = page.group.settleBy,
                        allowsNone = true,
                    )
                )
            },
        )
        Spacer(Modifier.height(PbSpace.S24))
        Members(
            page,
            rememberAvatars(view, page.members.map { it.personId }),
            onAdd = {
                navigator.open(
                    Route.PickPeople(
                        PickRequest(peopleRequest),
                        mode = PickMode.Multi,
                        selected = page.group.memberIds - ME,
                        title = addMembersTitle,
                    )
                )
            },
        )
        Spacer(Modifier.height(PbSpace.S24))
        Column(verticalArrangement = Arrangement.spacedBy(PbSpace.S8)) {
            PbCard {
                PbSettingRow(
                    stringResource(R.string.groups_currency),
                    Modifier.testTag("groupSettings.currency"),
                    icon = PbIcon.Exchange,
                    value = page.currency,
                    onClick = {
                        navigator.open(
                            Route.PickCurrency(
                                PickRequest(currencyRequest),
                                selected = page.group.currency,
                                title = currencyTitle,
                            )
                        )
                    },
                )
                PbSettingRow(
                    stringResource(R.string.groups_simplify),
                    Modifier.testTag("groupSettings.simplify"),
                    trailing =
                        PbSettingTrailing.Toggle(page.group.simplifyDebts) { on ->
                            ledger.updateGroup(groupId) { it.copy(simplifyDebts = on) }
                        },
                    icon = PbIcon.Shuffle,
                )
                PbSettingRow(
                    stringResource(R.string.groups_recurring),
                    Modifier.testTag("groupSettings.recurring"),
                    icon = PbIcon.Repeat,
                    value = page.recurring,
                    badge = if (snapshot.isPro) null else stringResource(R.string.groups_pro),
                    showDivider = false,
                    onClick = { navigator.requirePro(Route.Recurring(groupId)) },
                )
            }
            Text(
                stringResource(R.string.groups_simplify_helper),
                style = PbTextStyles.Footnote,
                color = PbColors.Text.Secondary,
            )
        }
        Spacer(Modifier.height(PbSpace.S24))
        PbButton(
            stringResource(R.string.groups_leave),
            onClick = { leaving = true },
            modifier = Modifier.fillMaxWidth().testTag("groupSettings.leave"),
            style = PbButtonStyle.Destructive,
            leadingIcon = PbIcon.Logout,
        )
    }

    if (leaving) {
        when (val check = view.leaveCheck(groupId)) {
            is LeaveCheck.Blocked ->
                PbAlert(
                    title = stringResource(R.string.groups_leave_blocked_title),
                    message = check.message,
                    cancelLabel = stringResource(R.string.groups_not_now),
                    actionLabel = stringResource(R.string.pb_settle_up),
                    onCancel = { leaving = false },
                    onAction = {
                        leaving = false
                        navigator.settle(view, check.settle)
                    },
                    action = PbAlertAction.Primary,
                    testTag = "groupSettings.leaveBlocked",
                )
            is LeaveCheck.Confirm ->
                PbAlert(
                    title = check.title,
                    message = check.message,
                    cancelLabel = stringResource(R.string.groups_cancel),
                    actionLabel = stringResource(R.string.groups_leave_action),
                    onCancel = { leaving = false },
                    onAction = {
                        leaving = false
                        ledger.leaveGroup(groupId)
                        navigator.popToRoot()
                    },
                    testTag = "groupSettings.leaveConfirm",
                )
            null -> leaving = false
        }
    }
}

@Composable
private fun NameCard(page: GroupSettingsPage, onName: () -> Unit, onSettleBy: () -> Unit) {
    PbCard {
        PbSettingRow(
            stringResource(R.string.groups_name),
            Modifier.testTag("groupSettings.name"),
            icon = iconForKey(page.group.icon),
            value = page.group.name,
            onClick = onName,
        )
        PbSettingRow(
            stringResource(R.string.groups_settle_by),
            Modifier.testTag("groupSettings.settleBy"),
            icon = PbIcon.Calendar,
            value = page.settleBy,
            showDivider = false,
            onClick = onSettleBy,
        )
    }
}

@Composable
private fun Members(page: GroupSettingsPage, avatars: List<PbAvatarContent>, onAdd: () -> Unit) {
    val navigator = LocalMainNavigator.current
    Column(verticalArrangement = Arrangement.spacedBy(PbSpace.S4)) {
        PbSectionHeader(
            stringResource(R.string.groups_members),
            Modifier.testTag("groupSettings.members"),
            action = stringResource(R.string.groups_add),
            onAction = onAdd,
        )
        PbCard {
            page.members.forEachIndexed { index, member ->
                PbPersonRow(
                    name = member.name,
                    avatar = avatars[index],
                    modifier = Modifier.testTag("groupSettings.member.${member.personId}"),
                    subtitle = member.subtitle,
                    tag = if (member.guest) stringResource(R.string.groups_guest) else null,
                    size = PbPersonRowSize.Compact,
                    onClick =
                        if (member.personId == ME) null
                        else ({ navigator.open(Route.Friend(member.personId)) }),
                    showDivider = index < page.members.lastIndex,
                )
            }
        }
    }
}
