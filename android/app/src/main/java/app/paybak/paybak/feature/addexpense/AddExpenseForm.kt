package app.paybak.paybak.feature.addexpense

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.paybak.paybak.R
import app.paybak.paybak.domain.addrecord.AmountEntry
import app.paybak.paybak.domain.addrecord.DateCopy
import app.paybak.paybak.domain.addrecord.DueChip
import app.paybak.paybak.domain.addrecord.SplitSummary
import app.paybak.paybak.domain.format.Dates
import app.paybak.paybak.domain.format.Money
import app.paybak.paybak.domain.model.Category
import app.paybak.paybak.domain.model.ME
import app.paybak.paybak.domain.model.SplitMode
import app.paybak.paybak.feature.pickers.PeopleDirectory
import app.paybak.paybak.ui.components.PbAmountField
import app.paybak.paybak.ui.components.PbCard
import app.paybak.paybak.ui.components.PbCategoryChip
import app.paybak.paybak.ui.components.PbChipLeading
import app.paybak.paybak.ui.components.PbDivider
import app.paybak.paybak.ui.components.PbDividerInset
import app.paybak.paybak.ui.components.PbReceiptThumbnail
import app.paybak.paybak.ui.components.PbSettingRow
import app.paybak.paybak.ui.components.PbTextField
import app.paybak.paybak.ui.components.pbIcon
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbLayout
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles
import java.time.LocalDate

/** The Add expense form's taps; each row that opens a picker drops the keyboard first. */
interface ExpenseFormActions {
    fun amount(text: String)

    fun title(text: String)

    fun currency()

    fun date()

    fun people()

    fun category()

    fun paidBy()

    fun split()

    fun group()

    fun due()

    fun dueChip(chip: DueChip)

    fun repeat()

    fun receipt()

    fun notes()
}

/**
 * The form body of `addExpenseEmpty` / `addExpenseFilled` (add-expense §4.2–4.5): the amount with
 * its currency and date chips, the people strip, the title and the rows card with the due chips.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddExpenseFields(
    form: ExpenseForm,
    people: PeopleDirectory,
    today: LocalDate,
    splitSummary: SplitSummary,
    groupName: String?,
    isPro: Boolean,
    receiptPhoto: ImageBitmap?,
    amountFocus: FocusRequester,
    actions: ExpenseFormActions,
) {
    val focusManager = LocalFocusManager.current
    fun tap(action: () -> Unit): () -> Unit = {
        focusManager.clearFocus()
        action()
    }
    PbAmountField(
        value = form.amount,
        onValueChange = actions::amount,
        formatted = AmountEntry.display(form.amount, form.currency),
        placeholder = AmountEntry.placeholder(form.currency),
        currency = form.currency,
        onCurrencyClick = tap(actions::currency),
        date = DateCopy.label(form.date, today),
        onDateClick = tap(actions::date),
        helper =
            form.rate
                ?.takeIf { form.total > 0 }
                ?.let { Money.approxLine(form.total, it.value, form.currency, it.to) }
                ?: form.itemized
                    ?.takeIf { form.split.mode == SplitMode.Itemized }
                    ?.let {
                        pluralStringResource(
                            R.plurals.add_from_receipt,
                            it.items.size,
                            it.items.size,
                        )
                    },
        allowDecimals = AmountEntry.allowsDecimals(form.currency),
        fieldModifier = Modifier.focusRequester(amountFocus),
        testTag = "addExpense",
    )
    PeopleStrip(form, people, onClick = tap(actions::people))
    PbTextField(
        value = form.title,
        onValueChange = { actions.title(it.take(MAX_TITLE)) },
        placeholder = stringResource(R.string.add_expense_title_placeholder),
        keyboardOptions =
            KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Done,
            ),
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
        fieldModifier = Modifier.testTag("addExpense.title"),
    )
    val category = form.category?.let(Category::of)
    PbCard {
        PbSettingRow(
            stringResource(R.string.add_category),
            Modifier.testTag("addExpense.row.category"),
            icon = category?.pbIcon ?: PbIcon.Tag,
            value = category?.label ?: stringResource(R.string.add_choose),
            onClick = tap(actions::category),
        )
        PbSettingRow(
            stringResource(R.string.add_paid_by),
            Modifier.testTag("addExpense.row.paidBy"),
            icon = PbIcon.Wallet,
            value = paidByValue(form, people),
            onClick = tap(actions::paidBy),
        )
        PbSettingRow(
            stringResource(R.string.add_split),
            Modifier.testTag("addExpense.row.split"),
            icon = PbIcon.Split,
            value = splitSummary.text,
            valueColor =
                if (splitSummary.error) PbColors.Text.Destructive else PbColors.Text.Secondary,
            onClick = tap(actions::split),
        )
        PbSettingRow(
            stringResource(R.string.add_group),
            Modifier.testTag("addExpense.row.group"),
            icon = PbIcon.Groups,
            value = groupName ?: stringResource(R.string.add_no_group),
            onClick = tap(actions::group),
        )
        PbSettingRow(
            stringResource(R.string.add_due),
            Modifier.testTag("addExpense.row.due"),
            icon = PbIcon.Calendar,
            value = form.dueDate?.let(Dates::day) ?: stringResource(R.string.add_none),
            onClick = tap(actions::due),
            showDivider = false,
        )
        DueChips(form.dueDate, today, onChip = actions::dueChip, onPick = tap(actions::due))
        PbDivider(inset = PbDividerInset.Leading, modifier = Modifier.padding(start = 36.dp))
        PbSettingRow(
            stringResource(R.string.add_repeat),
            Modifier.testTag("addExpense.row.repeat"),
            icon = PbIcon.Repeat,
            badge = if (isPro) null else stringResource(R.string.add_pro),
            value = form.repeat?.frequency?.label ?: stringResource(R.string.add_never),
            onClick = tap(actions::repeat),
        )
        PbSettingRow(
            if (form.receipt != null) stringResource(R.string.add_receipt)
            else stringResource(R.string.add_add_receipt),
            Modifier.testTag("addExpense.row.receipt"),
            icon = PbIcon.Camera,
            value = if (form.receipt != null) stringResource(R.string.add_attached) else null,
            valueLeading =
                form.receipt?.let {
                    { PbReceiptThumbnail(Modifier.size(25.dp, 32.dp), photo = receiptPhoto) }
                },
            onClick = tap(actions::receipt),
        )
        PbSettingRow(
            stringResource(R.string.add_notes),
            Modifier.testTag("addExpense.row.notes"),
            icon = PbIcon.Note,
            value =
                form.notes.lineSequence().firstOrNull()?.takeIf { it.isNotBlank() }
                    ?: stringResource(R.string.add_optional),
            onClick = tap(actions::notes),
            showDivider = false,
        )
    }
}

/** "With you and" + a chip per person, then Add; it scrolls sideways to the screen's edge. */
@Composable
private fun PeopleStrip(form: ExpenseForm, people: PeopleDirectory, onClick: () -> Unit) {
    Row(
        Modifier.bleedEnd(PbLayout.ScreenMargin).horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(PbSpace.S8),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            if (ME in form.people) stringResource(R.string.add_with_you_and)
            else stringResource(R.string.add_with),
            style = PbTextStyles.Subheadline,
            color = PbColors.Text.Secondary,
        )
        form.others.forEach { id ->
            PbCategoryChip(
                people.first(id),
                Modifier.testTag("addExpense.person.$id"),
                onClick = onClick,
                leading = PbChipLeading.Avatar(people.avatar(id)),
            )
        }
        PbCategoryChip(
            if (form.others.isEmpty()) stringResource(R.string.add_add_people)
            else stringResource(R.string.add_add),
            Modifier.testTag("addExpense.addPeople").padding(end = PbLayout.ScreenMargin),
            onClick = onClick,
            leading = PbChipLeading.Icon(PbIcon.Plus),
        )
    }
}

/** Widens the content by [bleed] past its right edge: the strip scrolls to the screen's edge. */
private fun Modifier.bleedEnd(bleed: Dp): Modifier = layout { measurable, constraints ->
    val width = constraints.maxWidth + bleed.roundToPx()
    val placeable = measurable.measure(constraints.copy(minWidth = width, maxWidth = width))
    layout(constraints.maxWidth, placeable.height) { placeable.place(0, 0) }
}

/** Tomorrow · This weekend · Next week · Pick date, under the Due row (white on the card). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DueChips(
    due: LocalDate?,
    today: LocalDate,
    onChip: (DueChip) -> Unit,
    onPick: () -> Unit,
) {
    val selected = DueChip.matching(due, today)
    FlowRow(
        Modifier.fillMaxWidth().padding(start = 52.dp, end = PbSpace.S16, bottom = PbSpace.S12),
        horizontalArrangement = Arrangement.spacedBy(PbSpace.S8),
        verticalArrangement = Arrangement.spacedBy(PbSpace.S8),
    ) {
        DueChip.entries.forEach { chip ->
            PbCategoryChip(
                chip.label,
                Modifier.testTag("addExpense.due.${chip.tag}"),
                selected = chip == selected,
                onClick = { onChip(chip) },
                onCard = true,
            )
        }
        PbCategoryChip(
            stringResource(R.string.add_pick_date),
            Modifier.testTag("addExpense.due.pick"),
            onClick = onPick,
            onCard = true,
        )
    }
}

private fun paidByValue(form: ExpenseForm, people: PeopleDirectory): String {
    val payers = form.payers()
    return if (payers.size > 1) "${payers.size} people"
    else people.first(payers.firstOrNull()?.personId ?: form.payerId)
}

private const val MAX_TITLE = 60
