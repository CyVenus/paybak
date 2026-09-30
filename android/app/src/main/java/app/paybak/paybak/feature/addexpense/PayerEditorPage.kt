package app.paybak.paybak.feature.addexpense

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.paybak.paybak.domain.addrecord.AmountEntry
import app.paybak.paybak.domain.format.Money
import app.paybak.paybak.feature.pickers.PeopleDirectory
import app.paybak.paybak.ui.components.PbAmountEditor
import app.paybak.paybak.ui.components.PbCard
import app.paybak.paybak.ui.components.PbHeaderAction
import app.paybak.paybak.ui.components.PbPushHeader
import app.paybak.paybak.ui.components.PbSplitMode
import app.paybak.paybak.ui.components.PbSplitRow
import app.paybak.paybak.ui.components.PbSplitTotalBar
import app.paybak.paybak.ui.components.addrecord.PbPinnedHeaderScreen
import app.paybak.paybak.ui.components.amountInput
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbTextStyles

/**
 * The payer editor, "Multiple people" (`addExpensePayers`; add-expense §6.3): the split editor's
 * Exact rows for what each person paid, with the live total. A tick means "paid something"; ticking
 * someone focuses their field. Done hands back the typed amounts once they add up.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PayerEditorPage(
    form: ExpenseForm,
    people: PeopleDirectory,
    onDone: (Map<String, String>) -> Unit,
    onBack: () -> Unit,
) {
    var amounts by rememberSaveable { mutableStateOf(ExpenseForm.startingPayers(form)) }
    var focusOn by remember { mutableStateOf<String?>(null) }
    val choices = form.payerChoices
    val focusers = remember(choices) { choices.associateWith { FocusRequester() } }
    focusOn?.let { id -> LaunchedEffect(id) { focusers[id]?.requestFocus() } }
    val status = ExpenseForm.payerStatus(amounts, form.total, form.currency)
    val balanced = status.remaining == 0L && amounts.isNotEmpty()
    BackHandler(onBack = onBack)

    PbPinnedHeaderScreen(
        testTag = "payers.page",
        header = {
            PbPushHeader(
                "Paid by",
                onBack = onBack,
                action = PbHeaderAction.Text("Done", { onDone(amounts) }, enabled = balanced),
                testTag = "payers",
                actionTag = "done",
            )
        },
        footer = {
            PbSplitTotalBar(
                status.left,
                status.detail,
                Modifier.testTag("payers.total")
                    .padding(bottom = if (WindowInsets.isImeVisible) 8.dp else 0.dp),
                isError = !balanced,
            )
        },
    ) {
        Text(
            listOf(form.title.trim(), Money.format(form.total, form.currency))
                .filter { it.isNotEmpty() }
                .joinToString(" · "),
            Modifier.fillMaxWidth(),
            style = PbTextStyles.Footnote,
            color = PbColors.Text.Secondary,
            textAlign = TextAlign.Center,
        )
        PbCard {
            choices.forEachIndexed { index, id ->
                val paid = id in amounts
                PbSplitRow(
                    people.first(id),
                    people.avatar(id),
                    included = paid,
                    onIncludedChange = { tick ->
                        amounts = if (tick) amounts + (id to "0") else amounts - id
                        focusOn = id.takeIf { tick }
                    },
                    mode =
                        PbSplitMode.Exact(
                            PbAmountEditor(
                                value = amounts[id] ?: "0",
                                onValueChange = { typed ->
                                    val kept =
                                        amountInput(
                                            typed,
                                            AmountEntry.allowsDecimals(form.currency),
                                        )
                                    AmountEntry.accept(kept)?.let { amounts = amounts + (id to it) }
                                },
                                prefix = AmountEntry.prefix(form.currency),
                                decimal = AmountEntry.allowsDecimals(form.currency),
                                focusRequester = focusers[id],
                            )
                        ),
                    modifier = Modifier.testTag("payers.row.$id"),
                    showDivider = index < choices.lastIndex,
                )
            }
        }
        Text(
            "Enter how much each person paid.",
            style = PbTextStyles.Footnote,
            color = PbColors.Text.Secondary,
        )
    }
}
