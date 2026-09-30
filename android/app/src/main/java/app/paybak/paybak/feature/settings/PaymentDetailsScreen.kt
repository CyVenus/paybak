package app.paybak.paybak.feature.settings

import android.content.res.Resources
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.paybak.paybak.R
import app.paybak.paybak.data.UserProfile
import app.paybak.paybak.domain.model.SavedMethodKind
import app.paybak.paybak.domain.model.SavedPaymentMethod
import app.paybak.paybak.navigation.LocalMainNavigator
import app.paybak.paybak.navigation.LocalProfileStore
import app.paybak.paybak.navigation.Route
import app.paybak.paybak.navigation.rememberDebugStartScreen
import app.paybak.paybak.ui.components.PbAlert
import app.paybak.paybak.ui.components.PbCard
import app.paybak.paybak.ui.components.PbPaymentPreview
import app.paybak.paybak.ui.components.PbSettingRow
import app.paybak.paybak.ui.components.PbSettingTone
import app.paybak.paybak.ui.components.PbSettingTrailing
import app.paybak.paybak.ui.components.PbSheet
import app.paybak.paybak.ui.components.rememberUserAvatar
import app.paybak.paybak.ui.icons.PbIcon

/** The debug start ids that open the Add sheet, with its prefill (screens-settings §12.7). */
private val AddSheetStates =
    mapOf(
        "paymentAddUpi" to AddMethodStart("arjun@okhdfcbank"),
        "paymentAddUpiError" to AddMethodStart("arjunokhdfcbank", showError = true),
    )

/**
 * The `paymentDetails` route (screens-settings §4–5): the user's payment methods, the Show to
 * friends switch and the "What friends see" preview of the primary method. Methods are added from
 * the Add payment method sheet; tapping one offers Make primary, Copy and Remove.
 */
@Composable
fun PaymentDetailsScreen(route: Route.PaymentDetails) {
    val navigator = LocalMainNavigator.current
    val profileStore = LocalProfileStore.current
    val profile by profileStore.profile.collectAsState()
    val designed = rememberDebugStartScreen(*AddSheetStates.keys.toTypedArray())
    var adding by rememberSaveable { mutableStateOf(designed != null) }
    var actionsFor by rememberSaveable { mutableStateOf<String?>(null) }
    var removing by rememberSaveable { mutableStateOf<String?>(null) }
    val copy = rememberCopier()
    val upiCopied = stringResource(R.string.settings_upi_copied)
    val detailsCopied = stringResource(R.string.settings_details_copied)
    val resources = LocalResources.current
    val copyMethod = { method: SavedPaymentMethod ->
        when (method.kind) {
            SavedMethodKind.Upi -> copy(method.value.orEmpty(), upiCopied)
            SavedMethodKind.Bank -> copy(resources.bankTitle(method), detailsCopied)
        }
    }

    SettingsPage(route.info.id, stringResource(R.string.settings_payment_title)) {
        SettingsSection(stringResource(R.string.settings_payment_methods)) {
            Column {
                profile.paymentMethods.forEachIndexed { index, method ->
                    TileRow(
                        icon = if (method.kind == SavedMethodKind.Upi) PbIcon.Wallet else PbIcon.Bank,
                        title = method.title(),
                        subtitle = method.subtitle(),
                        modifier = Modifier.testTag("paymentDetails.method.$index"),
                        height = MethodRowHeight,
                        onClick = { actionsFor = method.id },
                    )
                }
                TileRow(
                    icon = PbIcon.Plus,
                    title = stringResource(R.string.settings_payment_add),
                    subtitle = stringResource(R.string.settings_payment_add_detail),
                    modifier = Modifier.testTag("paymentDetails.add"),
                    height = MethodRowHeight,
                    onClick = { adding = true },
                )
            }
        }
        PbCard {
            ToggleRow(
                stringResource(R.string.settings_payment_show),
                checked = profile.showPaymentToFriends,
                onCheckedChange = { on ->
                    profileStore.update { it.copy(showPaymentToFriends = on) }
                },
                modifier = Modifier.testTag("paymentDetails.showToFriends"),
                subtitle = stringResource(R.string.settings_payment_show_detail),
                showDivider = false,
            )
        }
        SettingsSection(stringResource(R.string.settings_payment_preview)) {
            FriendsPreview(profile, onCopy = copyMethod)
        }
        SettingsInfoLine(PbIcon.Lock, stringResource(R.string.settings_payment_info))
    }

    if (adding) {
        AddPaymentMethodSheet(
            profile,
            start = AddSheetStates[designed] ?: AddMethodStart(profile.suggestedUpi()),
            onAdded = { updated, toast ->
                profileStore.update { updated }
                navigator.toast(toast)
            },
            onDismiss = { adding = false },
        )
    }
    profile.paymentMethods.firstOrNull { it.id == actionsFor }?.let { method ->
        MethodActionsSheet(
            method,
            onMakePrimary = { profileStore.update { it.makingPrimary(method.id) } },
            onCopy = { copyMethod(method) },
            onRemove = { removing = method.id },
            onDismiss = { actionsFor = null },
        )
    }
    removing?.let { id ->
        PbAlert(
            title = stringResource(R.string.settings_payment_remove_title),
            message = stringResource(R.string.settings_payment_remove_message),
            cancelLabel = stringResource(R.string.settings_cancel),
            actionLabel = stringResource(R.string.settings_payment_remove),
            onCancel = { removing = null },
            onAction = {
                removing = null
                profileStore.update { it.removingMethod(id) }
            },
            testTag = "paymentDetails.removeAlert",
        )
    }
}

private val MethodRowHeight = 64.dp

/** The primary method as friends see it, a note while it's hidden, or a nudge with no method. */
@Composable
private fun FriendsPreview(profile: UserProfile, onCopy: (SavedPaymentMethod) -> Unit) {
    val primary = profile.primaryMethod
    when {
        primary == null -> SettingsFootnote(stringResource(R.string.settings_payment_none))
        !profile.showPaymentToFriends ->
            SettingsFootnote(stringResource(R.string.settings_payment_hidden))
        else ->
            PbPaymentPreview(
                name = profile.name,
                upiId = primary.title(),
                avatar = rememberUserAvatar(profile, LocalProfileStore.current),
                onCopy = { onCopy(primary) },
                modifier = Modifier.testTag("paymentDetails.preview"),
                copyButtonModifier = Modifier.testTag("paymentDetails.copy"),
                showCaption = false,
            )
    }
}

/** Make primary (not for the primary), Copy and Remove, in a Medium sheet (§4, proposal). */
@Composable
private fun MethodActionsSheet(
    method: SavedPaymentMethod,
    onMakePrimary: () -> Unit,
    onCopy: () -> Unit,
    onRemove: () -> Unit,
    onDismiss: () -> Unit,
) {
    PbSheet(onDismiss = onDismiss, title = method.title(), testTag = "paymentDetails.actions") {
        dismiss ->
        val then = { action: () -> Unit ->
            action()
            dismiss()
        }
        PbCard {
            if (!method.primary) {
                PbSettingRow(
                    stringResource(R.string.settings_payment_make_primary),
                    modifier = Modifier.testTag("paymentDetails.actions.primary"),
                    trailing = PbSettingTrailing.None,
                    onClick = { then(onMakePrimary) },
                    icon = PbIcon.Star,
                )
            }
            PbSettingRow(
                stringResource(R.string.settings_payment_copy),
                modifier = Modifier.testTag("paymentDetails.actions.copy"),
                trailing = PbSettingTrailing.None,
                onClick = { then(onCopy) },
                icon = PbIcon.Copy,
            )
            PbSettingRow(
                stringResource(R.string.settings_payment_remove),
                modifier = Modifier.testTag("paymentDetails.actions.remove"),
                trailing = PbSettingTrailing.None,
                onClick = { then(onRemove) },
                icon = PbIcon.Delete,
                tone = PbSettingTone.Destructive,
                showDivider = false,
            )
        }
    }
}

/** "arjun@okaxis", or "HDFC Bank ···· 4821". */
@Composable
private fun SavedPaymentMethod.title(): String =
    when (kind) {
        SavedMethodKind.Upi -> value.orEmpty()
        SavedMethodKind.Bank -> LocalResources.current.bankTitle(this)
    }

private fun Resources.bankTitle(method: SavedPaymentMethod): String =
    getString(
        R.string.settings_payment_bank_title,
        method.bankName.orEmpty(),
        method.last4.orEmpty(),
    )

/** "UPI · Primary", "Bank transfer". */
@Composable
private fun SavedPaymentMethod.subtitle(): String {
    val kind =
        stringResource(
            if (kind == SavedMethodKind.Upi) R.string.settings_payment_upi
            else R.string.settings_payment_bank
        )
    return if (primary) stringResource(R.string.settings_payment_primary, kind) else kind
}
