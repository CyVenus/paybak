package app.paybak.paybak.feature.friends

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import app.paybak.paybak.R
import app.paybak.paybak.data.ledger.actions.addFriend
import app.paybak.paybak.data.ledger.actions.addGuest
import app.paybak.paybak.data.ledger.collectSnapshot
import app.paybak.paybak.navigation.LocalLedger
import app.paybak.paybak.navigation.LocalMainNavigator
import app.paybak.paybak.navigation.LocalProfileStore
import app.paybak.paybak.navigation.Route
import app.paybak.paybak.navigation.rememberDebugStartScreen
import app.paybak.paybak.service.contacts.AddFriendLists
import app.paybak.paybak.service.contacts.ContactsDirectory
import app.paybak.paybak.service.contacts.DeviceContact
import app.paybak.paybak.service.contacts.Directory
import app.paybak.paybak.service.contacts.addFriendLists
import app.paybak.paybak.service.contacts.contactKey
import app.paybak.paybak.service.qr.QrScan
import app.paybak.paybak.service.qr.QrScanner
import app.paybak.paybak.ui.components.PbAvatarContent
import app.paybak.paybak.ui.components.PbCard
import app.paybak.paybak.ui.components.PbPersonRow
import app.paybak.paybak.ui.components.PbPersonTrailing
import app.paybak.paybak.ui.components.PbSectionHeader
import app.paybak.paybak.ui.components.PbSettingRow
import app.paybak.paybak.ui.components.PbTextField
import app.paybak.paybak.ui.components.avatarContent
import app.paybak.paybak.ui.components.groups.PbPushedPage
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles
import kotlinx.coroutines.launch

/**
 * The `addFriend` route (screens-groups §7): search, Invite with a link, Scan QR code, My QR code
 * (a local sheet, `myQrCode`), your contacts on Paybak (Added / Add) and the ones to invite, who
 * join right away as guest friends.
 */
@Composable
fun AddFriendScreen(route: Route.AddFriend) {
    val navigator = LocalMainNavigator.current
    val ledger = LocalLedger.current
    val snapshot by ledger.collectSnapshot()
    val context = LocalContext.current
    val focus = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    val shareInvite = rememberInviteShare()
    val start = rememberDebugStartScreen("myQrCode")
    var showQr by rememberSaveable { mutableStateOf(start != null) }
    var query by rememberSaveable { mutableStateOf("") }
    val people = snapshot.ledger.people
    val contacts by
        produceState(emptyList<DeviceContact>()) { value = ContactsDirectory.contacts(context) }
    val directory by
        produceState<Directory?>(null, people) {
            value = ContactsDirectory.directory(context, people)
        }
    val lists =
        remember(directory, contacts, query) { directory?.addFriendLists(contacts, query) }
    val profile by LocalProfileStore.current.profile.collectAsState()
    val ownCode = stringResource(R.string.groups_own_code)
    val notACode = stringResource(R.string.groups_not_a_code)
    val unavailable = stringResource(R.string.groups_scan_unavailable)

    PbPushedPage(
        id = "addFriend",
        onBack = { navigator.back() },
        title = stringResource(R.string.groups_add_friend),
        overlay = { if (showQr) MyQrCodeSheet(onDismiss = { showQr = false }) },
    ) {
        PbTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = stringResource(R.string.groups_search_placeholder),
            leadingIcon = PbIcon.Search,
            onClear = { query = "" },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { focus.clearFocus() }),
            fieldModifier = Modifier.testTag("addFriend.search"),
        )
        Spacer(Modifier.height(PbSpace.S16))
        PbCard {
            PbSettingRow(
                stringResource(R.string.groups_invite_link),
                Modifier.testTag("addFriend.inviteLink"),
                icon = PbIcon.Link,
                onClick = shareInvite,
            )
            PbSettingRow(
                stringResource(R.string.groups_scan_qr),
                Modifier.testTag("addFriend.scanQr"),
                icon = PbIcon.Scan,
                onClick = {
                    scope.launch {
                        when (val scan = QrScanner.scan(context)) {
                            is QrScan.Scanned -> {
                                val own = profile.username
                                val code =
                                    directory?.let {
                                        addFriendFromCode(scan.text, own, it, ledger, navigator)
                                    }
                                when (code) {
                                    is ScannedCode.User -> Unit
                                    ScannedCode.Own -> navigator.toast(ownCode)
                                    ScannedCode.NotPaybak,
                                    null -> navigator.toast(notACode)
                                }
                            }
                            QrScan.Unavailable -> navigator.toast(unavailable)
                            QrScan.Cancelled -> Unit
                        }
                    }
                },
            )
            PbSettingRow(
                stringResource(R.string.groups_my_qr),
                Modifier.testTag("addFriend.myQr"),
                icon = PbIcon.QrCode,
                showDivider = false,
                onClick = {
                    focus.clearFocus()
                    showQr = true
                },
            )
        }
        if (lists != null) People(lists, query)
    }
}

/** "On Paybak" (Added, or Add) and "Invite" with its footnote; a search with no match says so. */
@Composable
private fun People(lists: AddFriendLists, query: String) {
    val navigator = LocalMainNavigator.current
    val ledger = LocalLedger.current
    if (lists.isEmpty) {
        if (query.isNotBlank()) {
            Spacer(Modifier.height(PbSpace.S24))
            Text(
                stringResource(R.string.groups_no_match, query.trim()),
                Modifier.testTag("addFriend.noMatch"),
                style = PbTextStyles.Footnote,
                color = PbColors.Text.Secondary,
            )
        }
        return
    }
    val addLabel = stringResource(R.string.groups_add)
    val inviteLabel = stringResource(R.string.groups_invite)
    if (lists.onPaybak.isNotEmpty()) {
        Spacer(Modifier.height(PbSpace.S24))
        Column(verticalArrangement = Arrangement.spacedBy(PbSpace.S4)) {
            PbSectionHeader(stringResource(R.string.groups_on_paybak))
            Column {
                lists.onPaybak.forEachIndexed { index, row ->
                    val person = row.person
                    val add = {
                        ledger.addFriend(person.copy(addedAt = ledger.clock.now()))
                    }
                    PbPersonRow(
                        name = person.name,
                        avatar = person.avatarContent(),
                        modifier = Modifier.testTag("addFriend.onPaybak.${person.id}"),
                        subtitle = row.handle,
                        trailing =
                            if (row.added) {
                                PbPersonTrailing.Status(stringResource(R.string.groups_added))
                            } else {
                                PbPersonTrailing.Action(addLabel, add)
                            },
                        onClick =
                            if (row.added) ({ navigator.open(Route.Friend(person.id)) }) else add,
                        showDivider = index < lists.onPaybak.lastIndex,
                        sidePadding = 0.dp,
                    )
                }
            }
        }
    }
    if (lists.invite.isNotEmpty()) {
        Spacer(Modifier.height(PbSpace.S24))
        Column(verticalArrangement = Arrangement.spacedBy(PbSpace.S16)) {
            Column(verticalArrangement = Arrangement.spacedBy(PbSpace.S4)) {
                PbSectionHeader(stringResource(R.string.groups_invite))
                Column {
                    lists.invite.forEachIndexed { index, row ->
                        val invite = {
                            val id = ledger.addGuest(row.name, row.contact)
                            navigator.open(Route.Friend(id))
                        }
                        PbPersonRow(
                            name = row.name,
                            avatar = PbAvatarContent.Initials(row.initials),
                            modifier =
                                Modifier.testTag(
                                    "addFriend.invite.${contactKey(row.contact) ?: index}"
                                ),
                            subtitle = stringResource(R.string.groups_not_on_paybak),
                            trailing = PbPersonTrailing.Action(inviteLabel, invite),
                            onClick = invite,
                            showDivider = index < lists.invite.lastIndex,
                            sidePadding = 0.dp,
                        )
                    }
                }
            }
            Text(
                stringResource(R.string.groups_guests_note),
                style = PbTextStyles.Footnote,
                color = PbColors.Text.Secondary,
            )
        }
    }
}
