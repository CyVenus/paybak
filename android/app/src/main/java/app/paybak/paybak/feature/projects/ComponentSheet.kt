package app.paybak.paybak.feature.projects

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import app.paybak.paybak.R
import app.paybak.paybak.domain.addrecord.AmountEntry
import app.paybak.paybak.domain.model.ComponentStatus
import app.paybak.paybak.domain.model.LedgerJson
import app.paybak.paybak.domain.model.ME
import app.paybak.paybak.domain.projects.ComponentForm
import app.paybak.paybak.feature.pickers.PeopleDirectory
import app.paybak.paybak.feature.pickers.rememberPhotoPicker
import app.paybak.paybak.ui.components.PbButton
import app.paybak.paybak.ui.components.PbCard
import app.paybak.paybak.ui.components.PbPersonRow
import app.paybak.paybak.ui.components.PbPersonTrailing
import app.paybak.paybak.ui.components.PbSegmentedControl
import app.paybak.paybak.ui.components.PbSettingRow
import app.paybak.paybak.ui.components.PbSheet
import app.paybak.paybak.ui.components.PbTextButton
import app.paybak.paybak.ui.components.PbTextButtonStyle
import app.paybak.paybak.ui.components.PbTextField
import app.paybak.paybak.ui.components.amountInput
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles
import kotlinx.serialization.Serializable

/** Where the Add / Edit component flow is: the sheet, or one of its alerts over the dashboard. */
@Serializable
internal enum class ComponentStep {
    Editing,
    Discarding,
    Deleting,
}

/**
 * The open component flow: the part being edited ([editingId], null adds one), what's typed, what
 * it started as (to tell whether closing loses anything) and the [step].
 */
@Serializable
internal data class ComponentFlow(
    val editingId: String? = null,
    val form: ComponentForm = ComponentForm(),
    val initial: ComponentForm = form,
    val step: ComponentStep = ComponentStep.Editing,
) {
    val dirty: Boolean
        get() = form != initial

    companion object {
        val Saver: Saver<ComponentFlow?, String> =
            Saver(
                save = { flow -> flow?.let { LedgerJson.encodeToString(serializer(), it) } },
                restore = { LedgerJson.decodeFromString(serializer(), it) },
            )
    }
}

/**
 * The Add / Edit component sheet (screens-projects §5, §1.5 proposal): name, estimated and actual
 * cost, status, payer and receipt. Add enables once there's a name (and an actual cost for a
 * bought or done part); typing an actual cost buys a planned part. Editing adds Delete component.
 *
 * @param nameOf "You" or a first name, for the Paid by row.
 * @param onSave Runs once the sheet has slid away; [onDismiss] after ✕, the scrim or back.
 */
@Composable
internal fun ComponentSheet(
    flow: ComponentFlow,
    currency: String,
    members: List<String>,
    people: PeopleDirectory,
    nameOf: (String) -> String,
    onChange: (ComponentForm) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    val form = flow.form
    var after by remember { mutableStateOf<(() -> Unit)?>(null) }
    var pickingPayer by rememberSaveable { mutableStateOf(false) }
    val pickReceipt = rememberPhotoPicker { onChange(form.copy(receipt = it)) }
    val editing = flow.editingId != null
    PbSheet(
        onDismiss = { (after ?: onDismiss)() },
        // With the keyboard up the form is taller than the space left: stop below the status bar.
        modifier = Modifier.statusBarsPadding(),
        title =
            stringResource(
                if (editing) R.string.projects_edit_component else R.string.projects_add_component
            ),
        testTag = "addComponent",
    ) { dismiss ->
        Column(
            Modifier.testTag("screen.projectAddComponent")
                .verticalScroll(rememberScrollState())
                .padding(horizontal = PbSpace.S4),
            verticalArrangement = Arrangement.spacedBy(PbSpace.S16),
        ) {
            PbTextField(
                form.name,
                { onChange(form.copy(name = it)) },
                label = stringResource(R.string.projects_name),
                placeholder = stringResource(R.string.projects_name_placeholder),
                keyboardOptions =
                    KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Next,
                    ),
                fieldModifier = Modifier.testTag("addComponent.name"),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(PbSpace.S8)) {
                CostField(
                    stringResource(R.string.projects_estimated_cost),
                    form.estimate,
                    currency,
                    { onChange(form.copy(estimate = it)) },
                    Modifier.weight(1f),
                    testTag = "addComponent.estimate",
                )
                CostField(
                    stringResource(R.string.projects_actual_cost),
                    form.actual,
                    currency,
                    { onChange(form.withActual(it)) },
                    Modifier.weight(1f),
                    helper = stringResource(R.string.projects_actual_helper),
                    testTag = "addComponent.actual",
                )
            }
            StatusPicker(form.status) { onChange(form.copy(status = it)) }
            PbCard {
                PbSettingRow(
                    stringResource(R.string.projects_paid_by),
                    Modifier.testTag("addComponent.paidBy"),
                    onClick = { pickingPayer = true },
                    icon = PbIcon.Wallet,
                    value = nameOf(form.paidBy),
                )
                PbSettingRow(
                    stringResource(R.string.projects_add_receipt),
                    Modifier.testTag("addComponent.receipt"),
                    onClick = pickReceipt,
                    icon = PbIcon.Camera,
                    value = form.receipt?.let { stringResource(R.string.projects_photo_added) },
                    showDivider = false,
                )
            }
            Text(
                stringResource(R.string.projects_shares_footnote),
                style = PbTextStyles.Footnote,
                color = PbColors.Text.Secondary,
            )
            PbButton(
                stringResource(
                    if (editing) R.string.projects_save_changes
                    else R.string.projects_add_component
                ),
                onClick = {
                    after = onSave
                    dismiss()
                },
                modifier = Modifier.fillMaxWidth().testTag("addComponent.add"),
                enabled = form.canSave(currency) && (!editing || flow.dirty),
            )
            if (editing) {
                PbTextButton(
                    stringResource(R.string.projects_delete_component),
                    onClick = {
                        after = onDelete
                        dismiss()
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                        .testTag("addComponent.delete"),
                    style = PbTextButtonStyle.Destructive,
                )
            }
        }
    }
    if (pickingPayer) {
        PayerSheet(
            members,
            form.paidBy,
            people,
            onPick = { onChange(form.copy(paidBy = it)) },
            onDismiss = { pickingPayer = false },
        )
    }
}

/** A cost on the decimal pad, shown with the currency symbol and grouping ("₹6,000"). */
@Composable
private fun CostField(
    label: String,
    text: String,
    currency: String,
    onChange: (String) -> Unit,
    modifier: Modifier,
    helper: String? = null,
    testTag: String,
) {
    val decimals = AmountEntry.allowsDecimals(currency)
    PbTextField(
        if (text.isEmpty()) "" else AmountEntry.display(text, currency),
        { typed -> AmountEntry.accept(amountInput(typed, decimals))?.let(onChange) },
        modifier,
        label = label,
        placeholder = AmountEntry.placeholder(currency),
        helper = helper,
        keyboardOptions =
            KeyboardOptions(
                keyboardType = if (decimals) KeyboardType.Decimal else KeyboardType.Number,
                imeAction = ImeAction.Next,
            ),
        fieldModifier = Modifier.testTag(testTag),
    )
}

@Composable
private fun StatusPicker(status: ComponentStatus, onPick: (ComponentStatus) -> Unit) {
    val statuses = ComponentStatus.entries
    Column(verticalArrangement = Arrangement.spacedBy(PbSpace.S8)) {
        Text(
            stringResource(R.string.projects_status),
            style = PbTextStyles.Subheadline,
            color = PbColors.Text.Secondary,
        )
        PbSegmentedControl(
            options = statuses.map { stringResource(it.label()) },
            selectedIndex = statuses.indexOf(status),
            onSelect = { onPick(statuses[it]) },
            modifier = Modifier.fillMaxWidth(),
            segmentTags = statuses.map { "addComponent.status.${it.name.lowercase()}" },
        )
    }
}

/** Paid by: the project's members, you first as "You"; a pick closes the sheet. */
@Composable
private fun PayerSheet(
    members: List<String>,
    selected: String,
    people: PeopleDirectory,
    onPick: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var picked by remember { mutableStateOf<String?>(null) }
    PbSheet(
        onDismiss = {
            picked?.let(onPick)
            onDismiss()
        },
        title = stringResource(R.string.projects_paid_by),
        testTag = "addComponent.payerSheet",
    ) { dismiss ->
        Column(Modifier.fillMaxWidth()) {
            members.forEachIndexed { index, id ->
                PbPersonRow(
                    if (id == ME) stringResource(R.string.projects_you) else people.full(id),
                    people.avatar(id),
                    Modifier.testTag("addComponent.payer.$id"),
                    subtitle = people.full(id).takeIf { id == ME },
                    trailing = if (id == selected) PbPersonTrailing.Check else null,
                    onClick = {
                        picked = id
                        dismiss()
                    },
                    showDivider = index < members.lastIndex,
                )
            }
        }
    }
}
