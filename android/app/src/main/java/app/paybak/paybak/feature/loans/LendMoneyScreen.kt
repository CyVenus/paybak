package app.paybak.paybak.feature.loans

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import app.paybak.paybak.data.ledger.actions.addLoan
import app.paybak.paybak.data.ledger.actions.updateLoan
import app.paybak.paybak.data.ledger.collectSnapshot
import app.paybak.paybak.domain.actions.LedgerRuleException
import app.paybak.paybak.domain.addrecord.AmountEntry
import app.paybak.paybak.domain.addrecord.DueChip
import app.paybak.paybak.domain.addrecord.LoanSchedule
import app.paybak.paybak.domain.format.Dates
import app.paybak.paybak.domain.model.Frequency
import app.paybak.paybak.domain.model.newId
import app.paybak.paybak.feature.pickers.rememberPeopleDirectory
import app.paybak.paybak.navigation.DateKind
import app.paybak.paybak.navigation.LendDirection
import app.paybak.paybak.navigation.LocalAppClock
import app.paybak.paybak.navigation.LocalLedger
import app.paybak.paybak.navigation.LocalMainNavigator
import app.paybak.paybak.navigation.LocalProfileStore
import app.paybak.paybak.navigation.PickMode
import app.paybak.paybak.navigation.PickRequest
import app.paybak.paybak.navigation.Route
import app.paybak.paybak.navigation.RouteResult
import app.paybak.paybak.navigation.RouteResultEffect
import app.paybak.paybak.navigation.rememberDebugStartScreen
import app.paybak.paybak.service.HapticKind
import app.paybak.paybak.service.rememberHaptics
import app.paybak.paybak.ui.components.PbAlert
import app.paybak.paybak.ui.components.PbAmountField
import app.paybak.paybak.ui.components.PbButton
import app.paybak.paybak.ui.components.PbCard
import app.paybak.paybak.ui.components.PbCategoryChip
import app.paybak.paybak.ui.components.PbModalHeader
import app.paybak.paybak.ui.components.PbSegmentedControl
import app.paybak.paybak.ui.components.PbSettingRow
import app.paybak.paybak.ui.components.PbSettingTrailing
import app.paybak.paybak.ui.components.PbSheet
import app.paybak.paybak.ui.components.PbTextField
import app.paybak.paybak.ui.components.addrecord.PbPinnedHeaderScreen
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles
import java.time.LocalDate

/** The Repeats choices (record-lend-group §4.4; only Monthly is designed). */
private val Repeats = listOf(Frequency.Weekly, Frequency.Biweekly, Frequency.Monthly)

/**
 * The `lendMoney` route (`lendMoney`; record-lend-group §4): a direct loan, not a shared bill. I
 * lent / I borrowed, the amount, who, why and when, then installments (count, repeats, first due
 * and the schedule preview) or a single due date with quick chips. Save lands on the loan with
 * "Loan added".
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LendMoneyScreen(route: Route.LendMoney) {
    val navigator = LocalMainNavigator.current
    val ledger = LocalLedger.current
    val snapshot by ledger.collectSnapshot()
    val profile by LocalProfileStore.current.profile.collectAsState()
    val today = LocalAppClock.current.today()
    val people = rememberPeopleDirectory()
    val haptics = rememberHaptics()
    val focusManager = LocalFocusManager.current
    val start = rememberDebugStartScreen("lendMoney")
    val editing = route.args.editing?.let { snapshot.ledger.loan(it) }
    val initial =
        rememberSaveable(stateSaver = LoanForm.Saver) {
            mutableStateOf(
                editing?.let(LoanForm::of)
                    ?: LoanForm(
                        lent = route.args.direction == LendDirection.Lent,
                        currency = profile.defaultCurrency,
                        friendId = route.args.personId,
                        date = today,
                    )
            )
        }
    var form by
        rememberSaveable(stateSaver = LoanForm.Saver) {
            mutableStateOf(
                if (start != null) {
                    // The Figma prefill (06-13): ₹6,000 lent to Dev for a laptop repair.
                    initial.value.copy(
                        amount = "6000",
                        friendId = "p-dev",
                        reason = "Laptop repair",
                    )
                } else {
                    initial.value
                }
            )
        }
    var sheet by rememberSaveable { mutableStateOf<String?>(null) }
    var discarding by rememberSaveable { mutableStateOf(false) }
    val requestId = rememberSaveable { newId() }
    fun request(kind: String) = PickRequest("$requestId.$kind")
    val amountFocus = remember { FocusRequester() }
    if (editing == null && start == null) LaunchedEffect(Unit) { amountFocus.requestFocus() }

    RouteResultEffect("$requestId.person") { result ->
        (result as? RouteResult.Person)?.let { form = form.copy(friendId = it.personId) }
    }
    RouteResultEffect("$requestId.currency") { result ->
        (result as? RouteResult.Currency)?.let { form = form.copy(currency = it.code) }
    }
    RouteResultEffect("$requestId.date") { result ->
        (result as? RouteResult.Day)?.day?.let { form = form.copy(date = it) }
    }
    RouteResultEffect("$requestId.firstDue") { result ->
        (result as? RouteResult.Day)?.day?.let { form = form.copy(firstDue = it) }
    }
    RouteResultEffect("$requestId.due") { result ->
        (result as? RouteResult.Day)?.let { form = form.copy(due = it.day) }
    }

    val canSave = form.total > 0 && form.friendId != null
    fun close() {
        if (form != initial.value) discarding = true else navigator.dismissModal()
    }
    fun open(route: Route) {
        focusManager.clearFocus()
        navigator.open(route)
    }
    fun save() {
        try {
            val draft = form.toDraft(ledger.rates, profile.defaultCurrency)
            if (editing != null) {
                ledger.updateLoan(editing.id, draft)
                navigator.dismissModal()
            } else {
                navigator.didSave(Route.Loan(ledger.addLoan(draft)), "Loan added")
            }
            haptics.perform(HapticKind.Success)
        } catch (error: LedgerRuleException) {
            haptics.perform(HapticKind.Warning)
            navigator.toast(error.message.orEmpty())
        }
    }
    BackHandler(onBack = ::close)

    PbPinnedHeaderScreen(
        testTag = "screen.lendMoney",
        header = {
            PbModalHeader(
                if (editing != null) "Edit loan" else "Lend money",
                onClose = ::close,
                action = "Save",
                actionEnabled = canSave,
                onAction = ::save,
                testTag = "lendMoney",
                actionTag = "save",
            )
        },
    ) {
        PbSegmentedControl(
            listOf("I lent", "I borrowed"),
            selectedIndex = if (form.lent) 0 else 1,
            onSelect = {
                haptics.perform(HapticKind.Selection)
                form = form.copy(lent = it == 0)
            },
            modifier = Modifier.fillMaxWidth(),
            segmentTags = listOf("lendMoney.direction.lent", "lendMoney.direction.borrowed"),
        )
        PbAmountField(
            value = form.amount,
            onValueChange = { text ->
                AmountEntry.accept(text)?.let { form = form.copy(amount = it) }
            },
            formatted = AmountEntry.display(form.amount, form.currency),
            placeholder = AmountEntry.placeholder(form.currency),
            currency = form.currency,
            onCurrencyClick = {
                open(Route.PickCurrency(request("currency"), selected = form.currency))
            },
            allowDecimals = AmountEntry.allowsDecimals(form.currency),
            fieldModifier = Modifier.focusRequester(amountFocus),
            testTag = "lendMoney",
        )
        Column(verticalArrangement = Arrangement.spacedBy(PbSpace.S24)) {
            PbCard {
                PbSettingRow(
                    if (form.lent) "Lent to" else "Borrowed from",
                    Modifier.testTag("lendMoney.person"),
                    icon = PbIcon.Profile,
                    value = form.friendId?.let(people::first) ?: "Choose",
                    onClick = {
                        open(
                            Route.PickPeople(
                                request("person"),
                                PickMode.Single,
                                title = if (form.lent) "Lent to" else "Borrowed from",
                                includesYou = false,
                            )
                        )
                    },
                )
                PbSettingRow(
                    "Reason",
                    Modifier.testTag("lendMoney.reason"),
                    icon = PbIcon.Receipt,
                    value = form.reason.ifBlank { "Optional" },
                    onClick = {
                        focusManager.clearFocus()
                        sheet = "reason"
                    },
                )
                PbSettingRow(
                    "Date",
                    Modifier.testTag("lendMoney.date"),
                    icon = PbIcon.Calendar,
                    value = Dates.day(form.date),
                    onClick = {
                        open(Route.PickDate(request("date"), DateKind.Date, selected = form.date))
                    },
                    showDivider = false,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(PbSpace.S12)) {
                PbCard {
                    PbSettingRow(
                        "Installments",
                        Modifier.testTag("lendMoney.installments"),
                        icon = PbIcon.Lend,
                        subtitle = "Paid back in parts",
                        trailing =
                            PbSettingTrailing.Toggle(form.installments) {
                                form = form.copy(installments = it)
                            },
                    )
                    if (form.installments) {
                        PbSettingRow(
                            "Number of installments",
                            Modifier.testTag("lendMoney.count"),
                            icon = PbIcon.Split,
                            value = form.count.toString(),
                            trailing =
                                PbSettingTrailing.Stepper(
                                    onDecrement = { form = form.copy(count = form.count - 1) },
                                    onIncrement = { form = form.copy(count = form.count + 1) },
                                    canDecrement = form.count > LoanSchedule.MIN_COUNT,
                                    canIncrement = form.count < LoanSchedule.MAX_COUNT,
                                ),
                        )
                        PbSettingRow(
                            "Repeats",
                            Modifier.testTag("lendMoney.repeats"),
                            icon = PbIcon.Repeat,
                            value = form.frequency.label,
                            onClick = { sheet = "repeats" },
                        )
                        PbSettingRow(
                            "First due",
                            Modifier.testTag("lendMoney.firstDue"),
                            icon = PbIcon.Calendar,
                            value = Dates.day(form.effectiveFirstDue),
                            onClick = {
                                open(
                                    Route.PickDate(
                                        request("firstDue"),
                                        DateKind.DueDate,
                                        selected = form.effectiveFirstDue,
                                        earliest = form.date.plusDays(1),
                                    )
                                )
                            },
                            showDivider = false,
                        )
                    } else {
                        PbSettingRow(
                            "Due",
                            Modifier.testTag("lendMoney.due"),
                            icon = PbIcon.Calendar,
                            value = form.due?.let(Dates::day) ?: "None",
                            onClick = { open(dueRoute(request("due"), form.due)) },
                            showDivider = false,
                        )
                        DueChips(form.due, today) { chip ->
                            if (chip == null) open(dueRoute(request("due"), form.due))
                            else {
                                val date = chip.date(today)
                                form = form.copy(due = date.takeIf { it != form.due })
                            }
                        }
                    }
                }
                if (form.installments && form.total > 0) {
                    Text(
                        LoanSchedule.preview(
                            form.total,
                            form.currency,
                            form.count,
                            form.frequency,
                            form.effectiveFirstDue,
                        ),
                        Modifier.testTag("lendMoney.schedule"),
                        style = PbTextStyles.Footnote,
                        color = PbColors.Text.Secondary,
                    )
                }
            }
        }
    }

    when (sheet) {
        "reason" ->
            ReasonSheet(
                form.reason,
                onDone = { form = form.copy(reason = it) },
                onDismiss = { sheet = null },
            )
        "repeats" ->
            RepeatsSheet(
                form.frequency,
                onPick = { form = form.copy(frequency = it, firstDue = null) },
                onDismiss = { sheet = null },
            )
    }
    if (discarding) {
        PbAlert(
            title = if (editing != null) "Discard changes?" else "Discard this loan?",
            message = "Your changes won’t be saved.",
            cancelLabel = "Keep editing",
            actionLabel = "Discard",
            onCancel = { discarding = false },
            onAction = {
                discarding = false
                navigator.dismissModal()
            },
            testTag = "lendMoney.discardAlert",
        )
    }
}

private fun dueRoute(request: PickRequest, due: LocalDate?) =
    Route.PickDate(request, DateKind.DueDate, selected = due, allowsNone = true)

/** Tomorrow · This weekend · Next week · Pick date (null), white on the card. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DueChips(due: LocalDate?, today: LocalDate, onChip: (DueChip?) -> Unit) {
    val selected = DueChip.matching(due, today)
    FlowRow(
        Modifier.fillMaxWidth().padding(start = 52.dp, end = PbSpace.S16, bottom = PbSpace.S12),
        horizontalArrangement = Arrangement.spacedBy(PbSpace.S8),
        verticalArrangement = Arrangement.spacedBy(PbSpace.S8),
    ) {
        DueChip.entries.forEach { chip ->
            PbCategoryChip(
                chip.label,
                Modifier.testTag("lendMoney.due.${chip.tag}"),
                selected = chip == selected,
                onClick = { onChip(chip) },
                onCard = true,
            )
        }
        PbCategoryChip(
            "Pick date",
            Modifier.testTag("lendMoney.due.pick"),
            onClick = { onChip(null) },
            onCard = true,
        )
    }
}

/** The Reason sheet: a one-line field and Done. */
@Composable
private fun ReasonSheet(reason: String, onDone: (String) -> Unit, onDismiss: () -> Unit) {
    var text by rememberSaveable { mutableStateOf(reason) }
    var save by remember { mutableStateOf(false) }
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { focus.requestFocus() }
    PbSheet(
        onDismiss = {
            if (save) onDone(text.trim())
            onDismiss()
        },
        title = "Reason",
        testTag = "reason.sheet",
    ) { dismiss ->
        val done = {
            save = true
            dismiss()
        }
        Column(verticalArrangement = Arrangement.spacedBy(PbSpace.S16)) {
            PbTextField(
                text,
                { text = it.take(MAX_REASON) },
                placeholder = "What’s it for?",
                keyboardOptions =
                    KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Done,
                    ),
                keyboardActions = KeyboardActions(onDone = { done() }),
                fieldModifier = Modifier.focusRequester(focus).testTag("reason.field"),
            )
            PbButton(
                "Done",
                onClick = done,
                modifier = Modifier.fillMaxWidth().testTag("reason.done"),
            )
        }
    }
}

/** The Repeats sheet: weekly, every 2 weeks or monthly. */
@Composable
private fun RepeatsSheet(selected: Frequency, onPick: (Frequency) -> Unit, onDismiss: () -> Unit) {
    var picked by remember { mutableStateOf<Frequency?>(null) }
    PbSheet(
        onDismiss = {
            picked?.let(onPick)
            onDismiss()
        },
        title = "Repeats",
        testTag = "repeats.sheet",
    ) { dismiss ->
        PbCard {
            Repeats.forEachIndexed { index, frequency ->
                PbSettingRow(
                    frequency.label,
                    Modifier.testTag("repeats.row.${frequency.name.lowercase()}"),
                    trailing = PbSettingTrailing.Check(frequency == selected),
                    onClick = {
                        picked = frequency
                        dismiss()
                    },
                    showDivider = index < Repeats.lastIndex,
                )
            }
        }
    }
}

private const val MAX_REASON = 60
