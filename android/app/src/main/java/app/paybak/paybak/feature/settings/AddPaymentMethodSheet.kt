package app.paybak.paybak.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.paybak.paybak.R
import app.paybak.paybak.data.UserProfile
import app.paybak.paybak.service.HapticKind
import app.paybak.paybak.service.rememberHaptics
import app.paybak.paybak.ui.components.PbButton
import app.paybak.paybak.ui.components.PbSegmentedControl
import app.paybak.paybak.ui.components.PbSheet
import app.paybak.paybak.ui.components.PbSheetContainer
import app.paybak.paybak.ui.components.PbTextField
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.icons.PbIconImage
import app.paybak.paybak.ui.theme.PaybakTheme
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbSize
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles

/** The sheet's two tabs. */
internal enum class MethodKindTab {
    Upi,
    Bank,
}

/** What the sheet starts with: the prefilled UPI ID and whether its error shows (debug states). */
internal data class AddMethodStart(val upi: String, val showError: Boolean = false)

/**
 * "Add payment method" (screens-settings §5): a Medium sheet over Payment details, riding the
 * keyboard, with UPI ID | Bank account tabs. Save validates on the spot: an ID without "@" shows
 * the inline error until the field is edited; a valid one is added and the sheet closes.
 *
 * @param onAdded Called with the updated profile and the toast to show.
 */
@Composable
internal fun AddPaymentMethodSheet(
    profile: UserProfile,
    start: AddMethodStart,
    onAdded: (UserProfile, toast: String) -> Unit,
    onDismiss: () -> Unit,
) {
    PbSheet(
        onDismiss = onDismiss,
        title = stringResource(R.string.settings_payment_add),
        testTag = "paymentAddUpi",
    ) { dismiss ->
        AddPaymentMethodForm(profile, start, onSave = { updated, toast ->
            onAdded(updated, toast)
            dismiss()
        })
    }
}

@Composable
private fun AddPaymentMethodForm(
    profile: UserProfile,
    start: AddMethodStart,
    onSave: (UserProfile, toast: String) -> Unit,
) {
    val haptics = rememberHaptics()
    var tab by rememberSaveable { mutableStateOf(MethodKindTab.Upi) }
    var upi by rememberSaveable { mutableStateOf(start.upi) }
    var bankName by rememberSaveable { mutableStateOf("") }
    var account by rememberSaveable { mutableStateOf("") }
    var problem by rememberSaveable { mutableStateOf(if (start.showError) UpiProblem.Invalid else null) }
    var bankInvalid by rememberSaveable { mutableStateOf(false) }
    val upiAdded = stringResource(R.string.settings_upi_added)
    val bankAdded = stringResource(R.string.settings_bank_added)

    val save: () -> Unit = {
        when (tab) {
            MethodKindTab.Upi -> {
                problem = profile.upiProblem(upi)
                if (problem == null) onSave(profile.addingUpi(upi), upiAdded)
                else haptics.perform(HapticKind.Warning)
            }
            MethodKindTab.Bank -> {
                bankInvalid = !isValidBankAccount(bankName, account)
                if (!bankInvalid) onSave(profile.addingBank(bankName, account), bankAdded)
                else haptics.perform(HapticKind.Warning)
            }
        }
    }
    val done = KeyboardActions(onDone = { save() })

    Column(
        Modifier.padding(top = PbSpace.S8).testTag("paymentAddUpi.sheet"),
        verticalArrangement = Arrangement.spacedBy(PbSpace.S16),
    ) {
        PbSegmentedControl(
            options =
                listOf(
                    stringResource(R.string.settings_add_upi_tab),
                    stringResource(R.string.settings_add_bank_tab),
                ),
            selectedIndex = tab.ordinal,
            onSelect = { tab = MethodKindTab.entries[it] },
            modifier = Modifier.fillMaxWidth(),
            segmentTags = listOf("paymentAddUpi.segment.upi", "paymentAddUpi.segment.bank"),
        )
        Column(verticalArrangement = Arrangement.spacedBy(PbSpace.S20)) {
            when (tab) {
                MethodKindTab.Upi ->
                    UpiField(
                        upi,
                        onChange = {
                            upi = it
                            problem = null
                        },
                        problem = problem,
                        actions = done,
                    )
                MethodKindTab.Bank ->
                    BankFields(
                        bankName,
                        onBankName = {
                            bankName = it
                            bankInvalid = false
                        },
                        account,
                        onAccount = {
                            account = it
                            bankInvalid = false
                        },
                        invalid = bankInvalid,
                        actions = done,
                    )
            }
            PbButton(
                stringResource(R.string.pb_save),
                onClick = save,
                modifier = Modifier.fillMaxWidth().testTag("paymentAddUpi.save"),
                enabled = if (tab == MethodKindTab.Upi) upi.isNotBlank() else bankName.isNotBlank(),
            )
        }
    }
}

@Composable
private fun UpiField(
    value: String,
    onChange: (String) -> Unit,
    problem: UpiProblem?,
    actions: KeyboardActions,
) {
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { focus.requestFocus() }
    Column(verticalArrangement = Arrangement.spacedBy(PbSpace.S8)) {
        PbTextField(
            value = value,
            onValueChange = onChange,
            label = stringResource(R.string.settings_add_upi_label),
            placeholder = stringResource(R.string.settings_add_upi_placeholder),
            helper = if (problem == null) stringResource(R.string.settings_add_upi_helper) else null,
            isError = problem != null,
            leadingIcon = PbIcon.Wallet,
            keyboardOptions =
                KeyboardOptions(
                    capitalization = KeyboardCapitalization.None,
                    autoCorrectEnabled = false,
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Done,
                ),
            keyboardActions = actions,
            fieldModifier = Modifier.focusRequester(focus).testTag("paymentAddUpi.field"),
        )
        problem?.let {
            FieldError(
                stringResource(
                    when (it) {
                        UpiProblem.Invalid -> R.string.settings_add_upi_error
                        UpiProblem.Duplicate -> R.string.settings_add_upi_duplicate
                    }
                )
            )
        }
    }
}

/** The bank tab (not drawn in Figma): the bank's name and the account number. */
@Composable
private fun BankFields(
    bankName: String,
    onBankName: (String) -> Unit,
    account: String,
    onAccount: (String) -> Unit,
    invalid: Boolean,
    actions: KeyboardActions,
) {
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { focus.requestFocus() }
    PbTextField(
        value = bankName,
        onValueChange = onBankName,
        label = stringResource(R.string.settings_add_bank_name),
        placeholder = stringResource(R.string.settings_add_bank_name_placeholder),
        leadingIcon = PbIcon.Bank,
        keyboardOptions =
            KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
        fieldModifier = Modifier.focusRequester(focus).testTag("paymentAddUpi.bankName"),
    )
    Column(verticalArrangement = Arrangement.spacedBy(PbSpace.S8)) {
        PbTextField(
            value = account,
            onValueChange = { text -> onAccount(text.filter(Char::isDigit)) },
            label = stringResource(R.string.settings_add_bank_account),
            placeholder = stringResource(R.string.settings_add_bank_account),
            helper =
                if (invalid) null else stringResource(R.string.settings_add_bank_account_helper),
            isError = invalid,
            keyboardOptions =
                KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
            keyboardActions = actions,
            fieldModifier = Modifier.testTag("paymentAddUpi.account"),
        )
        if (invalid) FieldError(stringResource(R.string.settings_add_bank_error))
    }
}

/** Figma's inline error: a red 16 dp alert icon and Caption/1 text, announced to TalkBack. */
@Composable
private fun FieldError(text: String) {
    Row(
        Modifier.height(18.dp)
            .testTag("paymentAddUpi.error")
            .semantics { liveRegion = LiveRegionMode.Polite },
        horizontalArrangement = Arrangement.spacedBy(PbSpace.S4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PbIconImage(
            PbIcon.Alert,
            contentDescription = null,
            size = PbSize.IconSm,
            tint = PbColors.Icon.Destructive,
        )
        Text(text, style = PbTextStyles.Caption1, color = PbColors.Text.Destructive)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF9E9E9E, widthDp = 402)
@Composable
private fun AddPaymentMethodFormPreview() {
    PaybakTheme {
        PbSheetContainer(
            Modifier.padding(8.dp),
            title = "Add payment method",
            onClose = {},
        ) {
            AddPaymentMethodForm(
                UserProfile(name = "Arjun Mehta"),
                AddMethodStart("arjunokhdfcbank", showError = true),
                onSave = { _, _ -> },
            )
        }
    }
}
