package app.paybak.paybak.feature.payments

import android.content.ClipData
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import app.paybak.paybak.data.ledger.actions.recordPayment
import app.paybak.paybak.data.ledger.actions.updatePayment
import app.paybak.paybak.data.ledger.collectSnapshot
import app.paybak.paybak.domain.actions.LedgerRuleException
import app.paybak.paybak.domain.addrecord.AmountEntry
import app.paybak.paybak.domain.addrecord.PaymentCopy
import app.paybak.paybak.domain.addrecord.contextLabel
import app.paybak.paybak.domain.addrecord.currencyOf
import app.paybak.paybak.domain.addrecord.openBalance
import app.paybak.paybak.domain.format.Dates
import app.paybak.paybak.domain.format.Money
import app.paybak.paybak.domain.model.ME
import app.paybak.paybak.domain.model.PaymentMethod
import app.paybak.paybak.domain.model.newId
import app.paybak.paybak.feature.pickers.PeopleDirectory
import app.paybak.paybak.feature.pickers.rememberPeopleDirectory
import app.paybak.paybak.feature.pickers.rememberPhotoPicker
import app.paybak.paybak.navigation.DateKind
import app.paybak.paybak.navigation.LocalAppClock
import app.paybak.paybak.navigation.LocalLedger
import app.paybak.paybak.navigation.LocalMainNavigator
import app.paybak.paybak.navigation.LocalProfileStore
import app.paybak.paybak.navigation.PhotoRef
import app.paybak.paybak.navigation.PickMode
import app.paybak.paybak.navigation.PickRequest
import app.paybak.paybak.navigation.Route
import app.paybak.paybak.navigation.RouteResult
import app.paybak.paybak.navigation.RouteResultEffect
import app.paybak.paybak.service.HapticKind
import app.paybak.paybak.service.rememberHaptics
import app.paybak.paybak.ui.components.PbAlert
import app.paybak.paybak.ui.components.PbAmountField
import app.paybak.paybak.ui.components.PbAvatar
import app.paybak.paybak.ui.components.PbAvatarContent
import app.paybak.paybak.ui.components.PbButton
import app.paybak.paybak.ui.components.PbButtonSize
import app.paybak.paybak.ui.components.PbButtonStyle
import app.paybak.paybak.ui.components.PbCard
import app.paybak.paybak.ui.components.PbCategoryChip
import app.paybak.paybak.ui.components.PbModalHeader
import app.paybak.paybak.ui.components.PbPaymentParties
import app.paybak.paybak.ui.components.PbSettingRow
import app.paybak.paybak.ui.components.addrecord.PbPinnedHeaderScreen
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbLayout
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles
import kotlinx.coroutines.launch

/**
 * The `recordPayment` route (`recordPayment`, `settleRecordKabir`; record-lend-group §2, settle
 * §4): log a payment made outside Paybak between you and one person. It's prefilled from the open
 * balance; UPI shows the recipient's ID with Copy. Save records it pending (confirmed at once when
 * they paid you) and lands on its detail with "Payment recorded".
 */
@Composable
fun RecordPaymentScreen(route: Route.RecordPayment) {
    val navigator = LocalMainNavigator.current
    val ledger = LocalLedger.current
    val snapshot by ledger.collectSnapshot()
    val profile by LocalProfileStore.current.profile.collectAsState()
    val today = LocalAppClock.current.today()
    val people = rememberPeopleDirectory()
    val haptics = rememberHaptics()
    val view = snapshot.view
    val editing = route.args.editing?.let { snapshot.ledger.payment(it) }
    val initial =
        rememberSaveable(stateSaver = PaymentForm.Saver) {
            mutableStateOf(
                editing?.let(PaymentForm::of) ?: PaymentForm.new(route.args, view, today)
            )
        }
    var form by rememberSaveable(stateSaver = PaymentForm.Saver) { mutableStateOf(initial.value) }
    var discarding by rememberSaveable { mutableStateOf(false) }
    val requestId = rememberSaveable { newId() }
    var picking by rememberSaveable { mutableStateOf("") }
    val amountFocus = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    val canSave = form.total > 0 && form.friendId != null

    RouteResultEffect("$requestId.person") { result ->
        val picked = (result as? RouteResult.Person)?.personId ?: return@RouteResultEffect
        val tappedFrom = picking == "from"
        form =
            when {
                picked == ME -> form.copy(youPaid = tappedFrom)
                else -> {
                    val sameFriend = picked == form.friendId
                    form.copy(
                        friendId = picked,
                        youPaid = !tappedFrom,
                        groupId = form.groupId.takeIf { sameFriend },
                        loanId = form.loanId.takeIf { sameFriend },
                    )
                }
            }.refilled(view)
    }
    RouteResultEffect("$requestId.currency") { result ->
        (result as? RouteResult.Currency)?.let {
            form = form.copy(currency = it.code, groupId = null, loanId = null, amountEdited = true)
        }
    }
    RouteResultEffect("$requestId.for") { result ->
        (result as? RouteResult.Group)?.let { picked ->
            val loan = picked.groupId?.let { snapshot.ledger.loan(it) }
            form =
                form
                    .copy(
                        groupId = picked.groupId.takeIf { loan == null },
                        loanId = loan?.id,
                        expenseId = null,
                    )
                    .refilled(view)
        }
    }
    RouteResultEffect("$requestId.date") { result ->
        (result as? RouteResult.Day)?.day?.let { form = form.copy(date = it) }
    }
    val pickProof = rememberPhotoPicker { form = form.copy(proof = it) }
    if (form.friendId == null && editing == null) {
        LaunchedEffect(Unit) { amountFocus.requestFocus() }
    }

    fun close() {
        if (form != initial.value) discarding = true else navigator.dismissModal()
    }
    fun pickPerson(side: String) {
        focusManager.clearFocus()
        picking = side
        navigator.open(
            Route.PickPeople(
                PickRequest("$requestId.person"),
                PickMode.Single,
                title = "Choose someone",
            )
        )
    }
    fun save() {
        try {
            val draft = form.toDraft(ledger.rates, profile.defaultCurrency)
            if (editing != null) {
                ledger.updatePayment(editing.id, draft)
                navigator.dismissModal()
            } else {
                val id = ledger.recordPayment(draft)
                navigator.didSave(Route.Payment(id), "Payment recorded")
            }
            haptics.perform(HapticKind.Success)
        } catch (error: LedgerRuleException) {
            haptics.perform(HapticKind.Warning)
            navigator.toast(error.message.orEmpty())
        }
    }
    // A route sheet over the form (currency, For, date) closes first.
    BackHandler(enabled = navigator.sheet == null, onBack = ::close)

    val friend = form.friendId
    val friendName = friend?.let(people::first)
    val forLabel =
        view.contextLabel(form.context).takeIf { form.groupId != null || form.loanId != null }
    PbPinnedHeaderScreen(
        testTag = "screen.recordPayment",
        header = {
            PbModalHeader(
                if (editing != null) "Edit payment" else "Record payment",
                onClose = ::close,
                action = "Save",
                actionEnabled = canSave,
                onAction = ::save,
                testTag = "recordPayment",
                actionTag = "save",
            )
        },
        gap = PbSpace.S24,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(PbSpace.S20)) {
            PbPaymentParties(
                fromName = form.fromId?.let(people::first) ?: "Choose",
                fromAvatar = party(people, form.fromId),
                toName = form.toId?.let(people::first) ?: "Choose",
                toAvatar = party(people, form.toId),
                onFromClick = { pickPerson("from") },
                onToClick = { pickPerson("to") },
                testTag = "recordPayment",
            )
            val balance = friend?.let { view.openBalance(it, form.context) } ?: 0
            val contextCurrency = view.currencyOf(form.context)
            PbAmountField(
                value = form.amount,
                onValueChange = { text ->
                    AmountEntry.accept(text)?.let {
                        form = form.copy(amount = it, amountEdited = true)
                    }
                },
                formatted = AmountEntry.display(form.amount, form.currency),
                placeholder = AmountEntry.placeholder(form.currency),
                currency = form.currency,
                onCurrencyClick = {
                    focusManager.clearFocus()
                    navigator.open(
                        Route.PickCurrency(
                            PickRequest("$requestId.currency"),
                            selected = form.currency,
                        )
                    )
                },
                helper =
                    if (form.currency != contextCurrency && form.total > 0) {
                        ledger.rates.rate(form.currency, contextCurrency)?.let {
                            Money.approxLine(form.total, it.value, form.currency, contextCurrency)
                        }
                    } else {
                        friendName?.let {
                            PaymentCopy.helper(
                                it,
                                balance,
                                form.currency,
                                forLabel?.removePrefix("Loan · "),
                                loan = form.loanId != null,
                            )
                        }
                    },
                allowDecimals = AmountEntry.allowsDecimals(form.currency),
                fieldModifier = Modifier.focusRequester(amountFocus),
                testTag = "recordPayment",
            )
        }
        MethodPicker(form.method) {
            haptics.perform(HapticKind.Selection)
            form = form.copy(method = it)
        }
        val recipientUpi =
            if (form.youPaid) friend?.let { people.person(it)?.upi }
            else profile.upiId.ifBlank { null }
        if (form.method == PaymentMethod.Upi && recipientUpi != null) {
            val recipient = form.toId ?: ME
            UpiPreview(people.full(recipient), recipientUpi, people, recipient) {
                scope.launch {
                    clipboard.setClipEntry(ClipEntry(ClipData.newPlainText("UPI ID", recipientUpi)))
                }
                navigator.toast("UPI ID copied")
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(PbSpace.S12)) {
            PbCard {
                PbSettingRow(
                    "For",
                    Modifier.testTag("recordPayment.for"),
                    icon = PbIcon.Groups,
                    value = forLabel ?: "None",
                    onClick = {
                        focusManager.clearFocus()
                        navigator.open(
                            Route.PickGroup(
                                PickRequest("$requestId.for"),
                                selected = form.groupId ?: form.loanId,
                                personId = friend,
                            )
                        )
                    },
                )
                PbSettingRow(
                    "Date",
                    Modifier.testTag("recordPayment.date"),
                    icon = PbIcon.Calendar,
                    value = Dates.day(form.date),
                    onClick = {
                        focusManager.clearFocus()
                        navigator.open(
                            Route.PickDate(
                                PickRequest("$requestId.date"),
                                DateKind.Date,
                                selected = form.date,
                            )
                        )
                    },
                )
                PbSettingRow(
                    "Proof",
                    Modifier.testTag("recordPayment.proof"),
                    icon = PbIcon.Camera,
                    value = if (form.proof != null) "1 photo" else "Add photo (optional)",
                    onClick = {
                        val proof = form.proof
                        if (proof != null) navigator.open(Route.PhotoViewer(PhotoRef(file = proof)))
                        else pickProof()
                    },
                    showDivider = false,
                )
            }
            if (friendName != null && form.total > 0) {
                Text(
                    PaymentCopy.summary(
                        friendName,
                        form.youPaid,
                        Money.format(form.total, form.currency),
                        form.method,
                        forLabel,
                    ),
                    Modifier.testTag("recordPayment.summary"),
                    style = PbTextStyles.Footnote,
                    color = PbColors.Text.Secondary,
                )
            }
        }
    }
    if (discarding) {
        PbAlert(
            title = if (editing != null) "Discard changes?" else "Discard this payment?",
            message = "Your changes won’t be saved.",
            cancelLabel = "Keep editing",
            actionLabel = "Discard",
            onCancel = { discarding = false },
            onAction = {
                discarding = false
                navigator.dismissModal()
            },
            testTag = "recordPayment.discardAlert",
        )
    }
}

private fun party(people: PeopleDirectory, id: String?) =
    id?.let(people::avatar) ?: PbAvatarContent.Symbol(PbIcon.UserAdd)

/** "Method": Cash · UPI · Bank · Card · Other, single choice. */
@Composable
private fun MethodPicker(selected: PaymentMethod, onPick: (PaymentMethod) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(PbSpace.S4)) {
        Text("Method", style = PbTextStyles.Subheadline, color = PbColors.Text.Secondary)
        Row(
            Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(PbSpace.S6),
        ) {
            PaymentMethod.entries.forEach { method ->
                PbCategoryChip(
                    method.label,
                    Modifier.testTag("recordPayment.method.${method.name.lowercase()}"),
                    selected = method == selected,
                    onClick = { onPick(method) },
                )
            }
        }
    }
}

/** The recipient's UPI ID with Copy, under the method chips when UPI is picked (08-04). */
@Composable
private fun UpiPreview(
    name: String,
    upi: String,
    people: PeopleDirectory,
    personId: String,
    onCopy: () -> Unit,
) {
    PbCard(Modifier.testTag("recordPayment.preview")) {
        Row(
            Modifier.fillMaxWidth().padding(PbLayout.CardPadding),
            horizontalArrangement = Arrangement.spacedBy(PbSpace.S12),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PbAvatar(people.avatar(personId), onCard = true)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(PbSpace.S2)) {
                Text(
                    name,
                    style = PbTextStyles.Headline,
                    color = PbColors.Text.Primary,
                    maxLines = 1,
                )
                Text(
                    upi,
                    style = PbTextStyles.Subheadline,
                    color = PbColors.Text.Secondary,
                    maxLines = 1,
                )
            }
            PbButton(
                "Copy",
                onClick = onCopy,
                modifier = Modifier.testTag("recordPayment.preview.copy"),
                style = PbButtonStyle.OnCard,
                size = PbButtonSize.Small,
                leadingIcon = PbIcon.Copy,
            )
        }
    }
}
