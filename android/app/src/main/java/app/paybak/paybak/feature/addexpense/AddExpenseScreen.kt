package app.paybak.paybak.feature.addexpense

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.platform.testTag
import app.paybak.paybak.data.ledger.actions.addExpense
import app.paybak.paybak.data.ledger.actions.updateExpense
import app.paybak.paybak.data.ledger.collectSnapshot
import app.paybak.paybak.domain.actions.LedgerRuleException
import app.paybak.paybak.domain.addrecord.DueChip
import app.paybak.paybak.domain.addrecord.SplitDraft
import app.paybak.paybak.domain.model.Receipt
import app.paybak.paybak.domain.model.SplitMode
import app.paybak.paybak.domain.model.newId
import app.paybak.paybak.feature.pickers.rememberLedgerPhoto
import app.paybak.paybak.feature.pickers.rememberPeopleDirectory
import app.paybak.paybak.feature.pickers.rememberPhotoPicker
import app.paybak.paybak.navigation.DateKind
import app.paybak.paybak.navigation.LocalAppClock
import app.paybak.paybak.navigation.LocalLedger
import app.paybak.paybak.navigation.LocalMainNavigator
import app.paybak.paybak.navigation.LocalProfileStore
import app.paybak.paybak.navigation.PhotoRef
import app.paybak.paybak.navigation.PickRequest
import app.paybak.paybak.navigation.Route
import app.paybak.paybak.navigation.RouteResult
import app.paybak.paybak.navigation.RouteResultEffect
import app.paybak.paybak.navigation.popTransition
import app.paybak.paybak.navigation.pushTransition
import app.paybak.paybak.navigation.rememberDebugStartScreen
import app.paybak.paybak.service.HapticKind
import app.paybak.paybak.service.rememberHaptics
import app.paybak.paybak.ui.components.PbAlert
import app.paybak.paybak.ui.components.PbModalHeader
import app.paybak.paybak.ui.components.addrecord.PbPinnedHeaderScreen
import app.paybak.paybak.ui.theme.LocalReduceMotion
import app.paybak.paybak.ui.theme.PbMotion
import app.paybak.paybak.ui.theme.PbSpace

/** The form and the pages it pushes inside the modal (add-expense §6.3, §7). */
private enum class Page {
    Form,
    Split,
    Payers,
}

/** The form's own sheets (add-expense §6, §8, §3.13). */
private enum class LocalSheet {
    PaidBy,
    Category,
    Notes,
}

/** The debug start states this screen applies itself (app-architecture §1.3). */
private val StartStates =
    arrayOf(
        "addExpensePaidBy",
        "addExpensePayers",
        "addExpenseSplitEqually",
        "addExpenseSplitExactError",
        "addExpenseCategory",
        "addExpenseDiscard",
    )

/**
 * The `addExpense` route (add-expense §3–§10): the amount-first form in a full-screen modal, new
 * (optionally prefilled by a draft: a group's members, Ask Paybak, a receipt) or editing an
 * expense. Save lands on the new expense with "Expense added"; ✕ with changes asks first.
 */
@Composable
fun AddExpenseScreen(route: Route.AddExpense) {
    val navigator = LocalMainNavigator.current
    val ledger = LocalLedger.current
    val snapshot by ledger.collectSnapshot()
    val profile by LocalProfileStore.current.profile.collectAsState()
    val today = LocalAppClock.current.today()
    val people = rememberPeopleDirectory()
    val haptics = rememberHaptics()
    val start = rememberDebugStartScreen(*StartStates)
    val editing = route.args.editing?.let { snapshot.ledger.expense(it) }
    val defaultCurrency = profile.defaultCurrency

    val initial =
        rememberSaveable(stateSaver = ExpenseForm.Saver) {
            mutableStateOf(
                editing?.let(ExpenseForm::of)
                    ?: ExpenseForm.new(route.args.draft, defaultCurrency, today)
                        .withRate(ledger.rates, defaultCurrency)
            )
        }
    var form by rememberSaveable(stateSaver = ExpenseForm.Saver) { mutableStateOf(initial.value) }
    var page by rememberSaveable {
        mutableStateOf(
            when (start) {
                "addExpenseSplitEqually",
                "addExpenseSplitExactError" -> Page.Split
                "addExpensePayers" -> Page.Payers
                else -> Page.Form
            }
        )
    }
    var sheet by rememberSaveable {
        mutableStateOf(
            when (start) {
                "addExpensePaidBy" -> LocalSheet.PaidBy
                "addExpenseCategory" -> LocalSheet.Category
                else -> null
            }
        )
    }
    var discarding by rememberSaveable { mutableStateOf(start == "addExpenseDiscard") }
    val requestId = rememberSaveable { newId() }
    fun request(kind: String) = PickRequest("$requestId.$kind")

    val group = form.groupId?.let { snapshot.ledger.group(it) }
    val order = SplitDraft.order(form.people, group?.memberIds)
    val sharers = order.filter { it !in form.split.excluded }
    val counter = snapshot.ledger.rotation[form.groupId ?: sharers.sorted().joinToString("+")] ?: 0
    val preview = form.preview(order, counter)
    val canSave =
        form.total > 0 && form.others.isNotEmpty() && preview.balanced && form.payersBalanced
    val dirty = form != initial.value

    RouteResultEffect("$requestId.people") { result ->
        (result as? RouteResult.People)?.let { form = form.withPeople(it.personIds) }
    }
    RouteResultEffect("$requestId.currency") { result ->
        (result as? RouteResult.Currency)?.let {
            val leavesGroup = group != null && it.code != group.currency
            form =
                form
                    .copy(
                        currency = it.code,
                        rate = null,
                        groupId = form.groupId.takeUnless { leavesGroup },
                    )
                    .withRate(ledger.rates, defaultCurrency)
        }
    }
    RouteResultEffect("$requestId.date") { result ->
        (result as? RouteResult.Day)?.day?.let { form = form.copy(date = it) }
    }
    RouteResultEffect("$requestId.due") { result ->
        (result as? RouteResult.Day)?.let { form = form.copy(dueDate = it.day) }
    }
    RouteResultEffect("$requestId.group") { result ->
        (result as? RouteResult.Group)?.let { picked ->
            val chosen = picked.groupId?.let { snapshot.ledger.group(it) }
            form =
                if (chosen == null) form.copy(groupId = null)
                else {
                    val joined =
                        if (form.others.isEmpty()) form.withPeople(chosen.memberIds) else form
                    joined
                        .copy(groupId = chosen.id, currency = chosen.currency, rate = null)
                        .withRate(ledger.rates, defaultCurrency)
                }
        }
    }
    RouteResultEffect("$requestId.repeat") { result ->
        (result as? RouteResult.Repeat)?.let { form = form.copy(repeat = it.rule) }
    }
    RouteResultEffect("$requestId.receipt") { result ->
        (result as? RouteResult.Receipt)?.let { form = form.applying(it, ledger.clock.now()) }
    }
    val pickPhoto = rememberPhotoPicker { file ->
        form = form.copy(receipt = Receipt(photo = file, addedAt = ledger.clock.now()))
    }
    val receiptPhoto = rememberLedgerPhoto(form.receipt?.photo)

    fun close() {
        if (dirty) discarding = true else navigator.dismissModal()
    }
    fun save() {
        try {
            if (editing != null) {
                ledger.updateExpense(editing.id, form.toDraft())
                haptics.perform(HapticKind.Success)
                navigator.dismissModal()
            } else {
                val id = ledger.addExpense(form.toDraft())
                haptics.perform(HapticKind.Success)
                navigator.didSave(Route.Expense(id), "Expense added")
            }
        } catch (error: LedgerRuleException) {
            haptics.perform(HapticKind.Warning)
            navigator.toast(error.message.orEmpty())
        }
    }
    val amountFocus = remember { FocusRequester() }
    if (route.args.focusAmount && editing == null && start == null) {
        LaunchedEffect(Unit) { amountFocus.requestFocus() }
    }
    BackHandler(enabled = page == Page.Form, onBack = ::close)

    val actions =
        object : ExpenseFormActions {
            override fun amount(text: String) {
                form = form.copy(amount = text)
            }

            override fun title(text: String) {
                form = form.copy(title = text)
            }

            override fun currency() =
                navigator.open(Route.PickCurrency(request("currency"), selected = form.currency))

            override fun date() =
                navigator.open(Route.PickDate(request("date"), DateKind.Date, selected = form.date))

            override fun people() =
                navigator.open(Route.PickPeople(request("people"), selected = form.people))

            override fun category() {
                sheet = LocalSheet.Category
            }

            override fun paidBy() {
                sheet = LocalSheet.PaidBy
            }

            override fun split() {
                page = Page.Split
            }

            override fun group() =
                navigator.open(Route.PickGroup(request("group"), selected = form.groupId))

            override fun due() =
                navigator.open(
                    Route.PickDate(
                        request("due"),
                        DateKind.DueDate,
                        selected = form.dueDate,
                        allowsNone = true,
                    )
                )

            override fun dueChip(chip: DueChip) {
                haptics.perform(HapticKind.Selection)
                val date = chip.date(today)
                form = form.copy(dueDate = date.takeIf { it != form.dueDate })
            }

            override fun repeat() =
                navigator.requirePro(
                    Route.RepeatRule(request("repeat"), form.repeat, startDate = form.date)
                )

            override fun receipt() {
                val receipt = form.receipt
                when {
                    receipt != null ->
                        navigator.open(Route.PhotoViewer(PhotoRef(receipt.photo, receipt.asset)))
                    snapshot.isPro -> navigator.open(Route.ScanReceipt(request("receipt")))
                    else -> pickPhoto()
                }
            }

            override fun notes() {
                sheet = LocalSheet.Notes
            }
        }

    Box(Modifier.fillMaxSize().testTag("screen.addExpense")) {
        val reduceMotion = LocalReduceMotion.current
        AnimatedContent(
            targetState = page,
            transitionSpec = {
                when {
                    reduceMotion ->
                        fadeIn(tween(PbMotion.SWAP_MILLIS)) togetherWith
                            fadeOut(tween(PbMotion.SWAP_MILLIS))
                    targetState.ordinal > initialState.ordinal -> pushTransition()
                    else -> popTransition()
                }
            },
            label = "AddExpense pages",
        ) { shown ->
            when (shown) {
                Page.Form ->
                    PbPinnedHeaderScreen(
                        testTag = "addExpense.form",
                        header = {
                            PbModalHeader(
                                if (editing != null) "Edit expense" else "Add expense",
                                onClose = ::close,
                                action = "Save",
                                actionEnabled = canSave,
                                onAction = ::save,
                                testTag = "addExpense",
                                actionTag = "save",
                            )
                        },
                        gap = PbSpace.S16,
                    ) {
                        AddExpenseFields(
                            form = form,
                            people = people,
                            today = today,
                            splitSummary =
                                form.split.summary(preview, form.people, form.total, form.currency),
                            groupName = group?.name,
                            isPro = snapshot.isPro,
                            receiptPhoto = receiptPhoto,
                            amountFocus = amountFocus,
                            actions = actions,
                        )
                    }
                Page.Split ->
                    SplitEditorPage(
                        form = form,
                        people = people,
                        order = order,
                        counter = counter,
                        onDone = { split ->
                            form =
                                form.copy(
                                    split = split,
                                    itemized =
                                        form.itemized.takeIf { split.mode == SplitMode.Itemized },
                                )
                            page = Page.Form
                        },
                        onBack = { page = Page.Form },
                        focus =
                            if (start == "addExpenseSplitExactError") "p-dev" to "550" else null,
                    )
                Page.Payers ->
                    PayerEditorPage(
                        form = form,
                        people = people,
                        onDone = { amounts ->
                            val paid = amounts.filterValues { it.isNotEmpty() && it != "0" }
                            form =
                                if (paid.size == 1)
                                    form.copy(payerId = paid.keys.first(), payerAmounts = null)
                                else form.copy(payerAmounts = paid)
                            page = Page.Form
                        },
                        onBack = { page = Page.Form },
                    )
            }
        }
    }

    when (sheet) {
        LocalSheet.PaidBy ->
            PaidBySheet(
                choices = form.payerChoices,
                payerId = form.payerId.takeIf { form.payerAmounts == null },
                people = people,
                onPick = { form = form.copy(payerId = it, payerAmounts = null) },
                onMultiple = { page = Page.Payers },
                onDismiss = { sheet = null },
            )
        LocalSheet.Category ->
            CategorySheet(
                selected = form.category,
                onPick = { form = form.copy(category = it.id) },
                onDismiss = { sheet = null },
            )
        LocalSheet.Notes ->
            NotesSheet(
                notes = form.notes,
                onDone = { form = form.copy(notes = it) },
                onDismiss = { sheet = null },
            )
        null -> Unit
    }
    if (discarding) {
        PbAlert(
            title = if (editing != null) "Discard changes?" else "Discard this expense?",
            message = "Your changes won’t be saved.",
            cancelLabel = "Keep editing",
            actionLabel = "Discard",
            onCancel = { discarding = false },
            onAction = {
                discarding = false
                navigator.dismissModal()
            },
            testTag = "addExpense.discardAlert",
        )
    }
}
