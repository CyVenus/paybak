package app.paybak.paybak.feature.addexpense

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import app.paybak.paybak.domain.model.Category
import app.paybak.paybak.domain.model.ME
import app.paybak.paybak.feature.pickers.PeopleDirectory
import app.paybak.paybak.feature.pickers.matchesSearch
import app.paybak.paybak.ui.components.PbButton
import app.paybak.paybak.ui.components.PbCard
import app.paybak.paybak.ui.components.PbDivider
import app.paybak.paybak.ui.components.PbPersonRow
import app.paybak.paybak.ui.components.PbPersonTrailing
import app.paybak.paybak.ui.components.PbSettingRow
import app.paybak.paybak.ui.components.PbSettingTrailing
import app.paybak.paybak.ui.components.PbSheet
import app.paybak.paybak.ui.components.PbSheetDetent
import app.paybak.paybak.ui.components.PbSheetRow
import app.paybak.paybak.ui.components.PbSheetSearch
import app.paybak.paybak.ui.components.PbTextArea
import app.paybak.paybak.ui.components.pbIcon
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles

/**
 * `addExpensePaidBy` (add-expense §6): you and the people on the expense; a pick sets the single
 * payer and closes the sheet. "Multiple people" opens the payer editor.
 *
 * @param payerId The current single payer; null when several people paid (no tick).
 */
@Composable
fun PaidBySheet(
    choices: List<String>,
    payerId: String?,
    people: PeopleDirectory,
    onPick: (String) -> Unit,
    onMultiple: () -> Unit,
    onDismiss: () -> Unit,
) {
    var picked by remember { mutableStateOf<() -> Unit>({}) }
    PbSheet(
        onDismiss = {
            picked()
            onDismiss()
        },
        title = "Paid by",
        testTag = "paidBy.sheet",
    ) { dismiss ->
        Column(Modifier.fillMaxWidth()) {
            choices.forEachIndexed { index, id ->
                PbPersonRow(
                    if (id == ME) "You" else people.full(id),
                    people.avatar(id),
                    Modifier.testTag("paidBy.row.$id"),
                    subtitle = people.full(id).takeIf { id == ME },
                    tag = "Guest".takeIf { people.isGuest(id) },
                    trailing = if (id == payerId) PbPersonTrailing.Check else null,
                    onClick = {
                        picked = { onPick(id) }
                        dismiss()
                    },
                    showDivider = index < choices.lastIndex,
                )
            }
            PbDivider()
            PbSheetRow(
                "Multiple people",
                "Enter how much each person paid",
                PbIcon.People,
                onClick = {
                    picked = onMultiple
                    dismiss()
                },
                modifier = Modifier.testTag("paidBy.multiple"),
            )
        }
    }
}

/** `addExpenseCategory` (add-expense §8): the 8 categories with their icons, searchable. */
@Composable
fun CategorySheet(selected: String?, onPick: (Category) -> Unit, onDismiss: () -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var picked by remember { mutableStateOf<Category?>(null) }
    PbSheet(
        onDismiss = {
            picked?.let(onPick)
            onDismiss()
        },
        title = "Category",
        detent = PbSheetDetent.Large,
        search = PbSheetSearch(query, { query = it }, "Search categories"),
        testTag = "category.sheet",
    ) { dismiss ->
        val shown = Category.entries.filter { matchesSearch(it.label, query = query) }
        Column(Modifier.verticalScroll(rememberScrollState())) {
            if (shown.isEmpty()) {
                Text(
                    "No categories match “$query”",
                    Modifier.fillMaxWidth().padding(top = PbSpace.S16),
                    style = PbTextStyles.Footnote,
                    color = PbColors.Text.Secondary,
                    textAlign = TextAlign.Center,
                )
            } else {
                PbCard {
                    shown.forEachIndexed { index, category ->
                        PbSettingRow(
                            category.label,
                            Modifier.testTag("category.row.${category.id}"),
                            icon = category.pbIcon,
                            trailing = PbSettingTrailing.Check(category.id == selected),
                            onClick = {
                                picked = category
                                dismiss()
                            },
                            showDivider = index < shown.lastIndex,
                        )
                    }
                }
            }
        }
    }
}

/** The Notes sheet (proposal, add-expense §3.13): a multi-line field and Done. */
@Composable
fun NotesSheet(notes: String, onDone: (String) -> Unit, onDismiss: () -> Unit) {
    var text by rememberSaveable { mutableStateOf(notes) }
    var save by remember { mutableStateOf(false) }
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { focus.requestFocus() }
    PbSheet(
        onDismiss = {
            if (save) onDone(text)
            onDismiss()
        },
        title = "Notes",
        testTag = "notes.sheet",
    ) { dismiss ->
        Column(verticalArrangement = Arrangement.spacedBy(PbSpace.S16)) {
            PbTextArea(
                text,
                { text = it.take(MAX_NOTES) },
                placeholder = "Anything to remember about it",
                fieldModifier = Modifier.focusRequester(focus).testTag("notes.field"),
            )
            PbButton(
                "Done",
                onClick = {
                    save = true
                    dismiss()
                },
                modifier = Modifier.fillMaxWidth().testTag("notes.done"),
            )
        }
    }
}

private const val MAX_NOTES = 500
