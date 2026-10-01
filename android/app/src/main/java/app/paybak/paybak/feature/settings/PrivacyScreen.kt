package app.paybak.paybak.feature.settings

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import app.paybak.paybak.PaybakApplication
import app.paybak.paybak.R
import app.paybak.paybak.data.ledger.actions.updateSettings
import app.paybak.paybak.data.ledger.collectSnapshot
import app.paybak.paybak.domain.calc.HomeTotals
import app.paybak.paybak.domain.format.Money
import app.paybak.paybak.domain.model.Discovery
import app.paybak.paybak.feature.profile.restartIntoOnboarding
import app.paybak.paybak.navigation.Destination
import app.paybak.paybak.navigation.LocalLedger
import app.paybak.paybak.navigation.LocalMainNavigator
import app.paybak.paybak.navigation.Route
import app.paybak.paybak.navigation.rememberDebugStartScreen
import app.paybak.paybak.ui.components.PbAlert
import app.paybak.paybak.ui.components.PbAlertAction
import app.paybak.paybak.ui.components.PbCard
import app.paybak.paybak.ui.components.PbSettingRow
import app.paybak.paybak.ui.components.PbSettingTone
import app.paybak.paybak.ui.components.PbSettingTrailing
import app.paybak.paybak.ui.icons.PbIcon

/** The alert Delete account shows. */
private enum class DeleteStep {
    /** "Settle up first": a balance is still open. */
    Blocked,

    /** "Delete account?": everything is settled. */
    Confirm,
}

/**
 * The `privacyData` route (screens-settings §8, §10): discovery switches (Contacts sync asks for
 * the contacts permission), Export records (Pro), Recently deleted and Delete account, which is
 * blocked while any balance is open and otherwise wipes this device and starts over.
 */
@Composable
fun PrivacyScreen(route: Route.PrivacyData) {
    val navigator = LocalMainNavigator.current
    val ledger = LocalLedger.current
    val snapshot by ledger.collectSnapshot()
    val discovery = snapshot.ledger.settings.discovery
    val totals = snapshot.home.totals
    val defaultCurrency = snapshot.view.defaultCurrency
    val context = LocalContext.current
    val designed = rememberDebugStartScreen("privacyDeleteBlocked")
    var deleting by rememberSaveable { mutableStateOf(designed?.let { DeleteStep.Blocked }) }
    var contactsDenied by rememberSaveable { mutableStateOf(false) }
    val setDiscovery = { change: (Discovery) -> Discovery ->
        ledger.updateSettings { it.copy(discovery = change(it.discovery)) }
    }
    val contactsPermission =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            setDiscovery { it.copy(contactsSync = granted) }
            contactsDenied = !granted
        }

    SettingsPage(route, stringResource(R.string.settings_privacy_title)) {
        SettingsSection(
            stringResource(R.string.settings_discovery),
            footer = stringResource(R.string.settings_discovery_footer),
        ) {
            PbCard {
                ToggleRow(
                    stringResource(R.string.settings_find_me),
                    checked = discovery.findMeByContact,
                    onCheckedChange = { on -> setDiscovery { it.copy(findMeByContact = on) } },
                    modifier = Modifier.testTag("privacyData.findMe"),
                    icon = PbIcon.Search,
                )
                ToggleRow(
                    stringResource(R.string.settings_contacts_sync),
                    checked = discovery.contactsSync,
                    onCheckedChange = { on ->
                        if (on && !context.canReadContacts()) {
                            contactsPermission.launch(Manifest.permission.READ_CONTACTS)
                        } else {
                            setDiscovery { it.copy(contactsSync = on) }
                        }
                    },
                    modifier = Modifier.testTag("privacyData.contactsSync"),
                    icon = PbIcon.People,
                    showDivider = false,
                )
            }
        }
        SettingsSection(
            stringResource(R.string.settings_your_data),
            footer = stringResource(R.string.settings_your_data_footer),
        ) {
            val deleted = snapshot.recentlyDeleted.size
            PbCard {
                PbSettingRow(
                    stringResource(R.string.settings_export_records),
                    modifier = Modifier.testTag("privacyData.export"),
                    onClick = { navigator.openFrom(route, Route.PrivacyExport, pro = true) },
                    icon = PbIcon.Download,
                    badge =
                        if (snapshot.isPro) null else stringResource(R.string.settings_pro_badge),
                )
                PbSettingRow(
                    stringResource(R.string.settings_recently_deleted),
                    modifier = Modifier.testTag("privacyData.recentlyDeleted"),
                    onClick = { navigator.openFrom(route, Route.RecentlyDeleted) },
                    icon = PbIcon.Restore,
                    value =
                        if (deleted == 0) null
                        else pluralStringResource(R.plurals.settings_items, deleted, deleted),
                    showDivider = false,
                )
            }
        }
        SettingsSection(title = null, footer = stringResource(R.string.settings_delete_footer)) {
            PbCard {
                PbSettingRow(
                    stringResource(R.string.settings_delete_account),
                    modifier = Modifier.testTag("privacyData.deleteAccount"),
                    trailing = PbSettingTrailing.None,
                    onClick = {
                        deleting = if (totals.isOpen) DeleteStep.Blocked else DeleteStep.Confirm
                    },
                    icon = PbIcon.Delete,
                    tone = PbSettingTone.Destructive,
                    showDivider = false,
                )
            }
        }
    }

    when (deleting) {
        DeleteStep.Blocked ->
            PbAlert(
                title = stringResource(R.string.settings_delete_blocked_title),
                message = blockedMessage(totals, defaultCurrency),
                cancelLabel = stringResource(R.string.settings_not_now),
                actionLabel = stringResource(R.string.settings_settle_up),
                onCancel = { deleting = null },
                onAction = {
                    deleting = null
                    navigator.openFrom(route, Route.SettleUp())
                },
                action = PbAlertAction.Primary,
                testTag = "privacyDeleteBlocked",
            )
        DeleteStep.Confirm ->
            PbAlert(
                title = stringResource(R.string.settings_delete_title),
                message = stringResource(R.string.settings_delete_message),
                cancelLabel = stringResource(R.string.settings_cancel),
                actionLabel = stringResource(R.string.settings_delete),
                onCancel = { deleting = null },
                onAction = {
                    deleting = null
                    (context.applicationContext as PaybakApplication).resetAccount()
                    context.restartIntoOnboarding(Destination.Welcome(1))
                },
                testTag = "privacyData.deleteAlert",
            )
        null -> Unit
    }
    if (contactsDenied) {
        PbAlert(
            title = stringResource(R.string.settings_contacts_denied_title),
            message = stringResource(R.string.settings_contacts_denied_message),
            cancelLabel = stringResource(R.string.settings_cancel),
            actionLabel = stringResource(R.string.settings_open_settings),
            onCancel = { contactsDenied = false },
            onAction = {
                contactsDenied = false
                context.openAppSettings()
            },
            action = PbAlertAction.Primary,
            testTag = "privacyData.contactsAlert",
        )
    }
}

/** Anything owed to or by the user, anywhere (§12.4). */
private val HomeTotals.isOpen: Boolean
    get() = owe != 0L || owed != 0L

/** The Home totals without signs: "You still owe ₹1,850 and are owed ₹2,900. …" */
@Composable
private fun blockedMessage(totals: HomeTotals, currency: String): String {
    val owe = Money.format(totals.owe, currency)
    val owed = Money.format(totals.owed, currency)
    return when {
        totals.owe != 0L && totals.owed != 0L ->
            stringResource(R.string.settings_delete_blocked_both, owe, owed)
        totals.owe != 0L -> stringResource(R.string.settings_delete_blocked_owe, owe)
        else -> stringResource(R.string.settings_delete_blocked_owed, owed)
    }
}

private fun Context.canReadContacts(): Boolean =
    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) ==
        PackageManager.PERMISSION_GRANTED

private fun Context.openAppSettings() {
    startActivity(
        Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", packageName, null),
            )
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )
}
