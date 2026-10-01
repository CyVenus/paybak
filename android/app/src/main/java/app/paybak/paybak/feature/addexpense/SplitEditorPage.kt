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
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.paybak.paybak.R
import app.paybak.paybak.domain.addrecord.AmountEntry
import app.paybak.paybak.domain.addrecord.SplitDraft
import app.paybak.paybak.domain.format.Money
import app.paybak.paybak.domain.model.LedgerJson
import app.paybak.paybak.domain.model.SplitMode
import app.paybak.paybak.feature.pickers.PeopleDirectory
import app.paybak.paybak.service.HapticKind
import app.paybak.paybak.service.rememberHaptics
import app.paybak.paybak.ui.components.PbAmountEditor
import app.paybak.paybak.ui.components.PbCard
import app.paybak.paybak.ui.components.PbHeaderAction
import app.paybak.paybak.ui.components.PbPushHeader
import app.paybak.paybak.ui.components.PbSegmentedControl
import app.paybak.paybak.ui.components.PbSplitMode
import app.paybak.paybak.ui.components.PbSplitRow
import app.paybak.paybak.ui.components.PbSplitTotalBar
import app.paybak.paybak.ui.components.addrecord.PbPinnedHeaderScreen
import app.paybak.paybak.ui.components.amountInput
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbTextStyles

/** The editor's modes in segment order: Equally · Exact · % · Shares. */
private val Modes = listOf(SplitMode.Equal, SplitMode.Exact, SplitMode.Percent, SplitMode.Shares)
private val ModeTags = listOf("equally", "exact", "percent", "shares")

private val DraftSaver: Saver<SplitDraft, String> =
    Saver(
        save = { LedgerJson.encodeToString(SplitDraft.serializer(), it) },
        restore = { LedgerJson.decodeFromString(SplitDraft.serializer(), it) },
    )

/**
 * The split editor (`addExpenseSplitEqually`, `addExpenseSplitExactError`; add-expense §7), a page
 * pushed inside the Add expense modal. It edits a copy of the form's split: Done (only while it
 * adds up) or Back on a balanced split hands it back; Back on an unbalanced one keeps the last
 * valid split.
 *
 * @param order The ledger's rotation order for the people, so shares match what Save computes.
 * @param focus Starts in Exact with this person's field focused, holding this value (the
 *   `addExpenseSplitExactError` start state: Dev at ₹550).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SplitEditorPage(
    form: ExpenseForm,
    people: PeopleDirectory,
    order: List<String>,
    counter: Int,
    onDone: (SplitDraft) -> Unit,
    onBack: () -> Unit,
    focus: Pair<String, String>? = null,
) {
    val haptics = rememberHaptics()
    val initial = remember {
        val start =
            if (form.split.mode == SplitMode.Itemized) toExact(form, order, counter) else form.split
        if (focus == null) start
        else
            start
                .switchTo(SplitMode.Exact, form.preview(order, counter), form.people, form.currency)
                .withValue(focus.first, focus.second)
    }
    var draft by rememberSaveable(stateSaver = DraftSaver) { mutableStateOf(initial) }
    val total = form.total
    val currency = form.currency
    val preview = draft.preview(total, form.people, currency, order, counter, form.itemized)
    val focusers = remember(form.people) { form.people.associateWith { FocusRequester() } }
    focus?.let { (person, _) -> LaunchedEffect(Unit) { focusers[person]?.requestFocus() } }
    val leave = { if (preview.balanced) onDone(draft) else onBack() }
    BackHandler(onBack = leave)

    PbPinnedHeaderScreen(
        testTag = "split.page",
        header = {
            PbPushHeader(
                stringResource(R.string.add_split),
                onBack = leave,
                action =
                    PbHeaderAction.Text(
                        stringResource(R.string.add_done),
                        { onDone(draft) },
                        enabled = preview.balanced,
                    ),
                testTag = "split",
                actionTag = "done",
            )
        },
        footer = {
            PbSplitTotalBar(
                preview.left,
                preview.detail,
                Modifier.testTag("split.total")
                    .padding(bottom = if (WindowInsets.isImeVisible) 8.dp else 0.dp),
                isError = !preview.balanced,
            )
        },
    ) {
        Text(
            listOf(form.title.trim(), Money.format(total, currency))
                .filter { it.isNotEmpty() }
                .joinToString(" · "),
            Modifier.fillMaxWidth(),
            style = PbTextStyles.Footnote,
            color = PbColors.Text.Secondary,
            textAlign = TextAlign.Center,
        )
        PbSegmentedControl(
            listOf(
                stringResource(R.string.add_split_equally),
                stringResource(R.string.add_split_exact),
                stringResource(R.string.add_split_percent),
                stringResource(R.string.add_split_shares),
            ),
            selectedIndex = Modes.indexOf(draft.mode).coerceAtLeast(0),
            onSelect = { index ->
                haptics.perform(HapticKind.Selection)
                draft = draft.switchTo(Modes[index], preview, form.people, currency)
            },
            modifier = Modifier.fillMaxWidth(),
            segmentTags = ModeTags.map { "split.mode.$it" },
        )
        PbCard {
            form.people.forEachIndexed { index, id ->
                val included = id !in draft.excluded
                val share = preview.shares[id] ?: 0
                fun edit(decimals: Boolean) =
                    PbAmountEditor(
                        value = if (included) draft.value(id) else "0",
                        onValueChange = { typed ->
                            draft = draft.withValue(id, clean(typed, draft.mode, decimals))
                        },
                        prefix =
                            if (draft.mode == SplitMode.Exact) AmountEntry.prefix(currency) else "",
                        suffix = if (draft.mode == SplitMode.Percent) "%" else "",
                        currency = currency.takeIf { draft.mode == SplitMode.Exact },
                        decimal = decimals,
                        focusRequester = focusers[id],
                    )
                val mode =
                    when (draft.mode) {
                        SplitMode.Exact ->
                            PbSplitMode.Exact(edit(AmountEntry.allowsDecimals(currency)))
                        SplitMode.Percent ->
                            PbSplitMode.Percent(edit(true), Money.format(share, currency))
                        SplitMode.Shares -> {
                            val count = draft.value(id).toIntOrNull() ?: 0
                            PbSplitMode.Shares(
                                edit(false),
                                Money.format(share, currency),
                                onDecrement = {
                                    draft =
                                        draft.withValue(id, (count - 1).coerceAtLeast(1).toString())
                                },
                                onIncrement = {
                                    draft =
                                        draft.withValue(
                                            id,
                                            (count + 1).coerceAtMost(MAX_SHARES).toString(),
                                        )
                                },
                            )
                        }
                        else -> PbSplitMode.Equally(Money.format(share, currency))
                    }
                PbSplitRow(
                    people.first(id),
                    people.avatar(id),
                    included = included,
                    onIncludedChange = {
                        haptics.perform(HapticKind.Selection)
                        draft = draft.toggle(id, form.people)
                    },
                    mode = mode,
                    modifier = Modifier.testTag("split.row.$id"),
                    showDivider = index < form.people.lastIndex,
                )
            }
        }
        if (draft.mode == SplitMode.Equal || draft.mode == SplitMode.Shares) {
            Text(
                stringResource(R.string.add_split_hint),
                Modifier.testTag("split.hint"),
                style = PbTextStyles.Footnote,
                color = PbColors.Text.Secondary,
            )
        }
    }
}

/** An itemized receipt split opens as the exact amounts it came to. */
private fun toExact(form: ExpenseForm, order: List<String>, counter: Int): SplitDraft {
    val preview = form.preview(order, counter)
    return SplitDraft(
        mode = SplitMode.Exact,
        excluded = form.split.excluded,
        values =
            preview.shares.mapValues { AmountEntry.text(it.value, form.currency).ifEmpty { "0" } },
    )
}

/** Keeps what a split field may hold: amounts and percents with 2 decimals, whole shares. */
private fun clean(typed: String, mode: SplitMode, decimals: Boolean): String {
    val kept = amountInput(typed, allowDecimals = decimals)
    return when (mode) {
        SplitMode.Shares -> kept.take(2)
        SplitMode.Percent ->
            if ((kept.substringBefore('.').toIntOrNull() ?: 0) > 100) "100" else kept
        else -> AmountEntry.accept(kept) ?: kept.dropLast(1)
    }
}

private const val MAX_SHARES = 99
