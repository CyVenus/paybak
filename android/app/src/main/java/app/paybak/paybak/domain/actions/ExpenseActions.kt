package app.paybak.paybak.domain.actions

import app.paybak.paybak.domain.calc.SplitResult
import app.paybak.paybak.domain.calc.Splits
import app.paybak.paybak.domain.model.Category
import app.paybak.paybak.domain.model.Comment
import app.paybak.paybak.domain.model.Expense
import app.paybak.paybak.domain.model.ExpenseDraft
import app.paybak.paybak.domain.model.Flag
import app.paybak.paybak.domain.model.HistoryEntry
import app.paybak.paybak.domain.model.HistoryKind
import app.paybak.paybak.domain.model.Ledger
import app.paybak.paybak.domain.model.ME
import app.paybak.paybak.domain.model.Payer
import app.paybak.paybak.domain.model.RecurringRule
import app.paybak.paybak.domain.model.RuleSplit
import app.paybak.paybak.domain.model.Split
import app.paybak.paybak.domain.model.SplitMode
import app.paybak.paybak.domain.model.newId
import app.paybak.paybak.domain.model.replacing
import kotlinx.serialization.json.JsonPrimitive

/** Expense actions (domain.md §11). Each returns the new ledger; `add…` also the new id. */
const val PERCENT_TOTAL_BPS = 10_000L

/**
 * Adds an expense: validates it, splits it with the fair rotation and logs `created`. [by] is who
 * added it (a friend's expense arrives through the simulated sync).
 */
fun Ledger.addExpense(
    draft: ExpenseDraft,
    ctx: ActionContext,
    by: String = ME,
): Pair<Ledger, String> {
    val group = draft.groupId?.let(::group)
    val currency = group?.currency ?: draft.currency ?: ctx.defaultCurrency
    val computed = computeSplit(draft, currency)
    val id = newId()
    val expense =
        Expense(
            id = id,
            groupId = draft.groupId,
            title = draft.title.trim().ifEmpty { Category.of(draft.category).label },
            category = draft.category,
            amount = draft.amount,
            currency = currency,
            rate = draft.rate.takeIf { currency != ctx.defaultCurrency },
            date = draft.date ?: ctx.today,
            dueDate = draft.dueDate,
            payers = computed.payers,
            split = computed.split,
            itemized = draft.itemized,
            notes = draft.notes,
            receipt = draft.receipt,
            createdAt = ctx.at,
            createdBy = by,
            history = listOf(HistoryEntry(HistoryKind.Created, ctx.at, by)),
        )
    var next =
        copy(
            expenses = expenses + expense,
            rotation = rotation + (computed.key to computed.counter),
        )
    draft.repeat?.let { repeat ->
        val rule =
            RecurringRule(
                id = newId(),
                groupId = draft.groupId,
                title = expense.title,
                category = draft.category,
                amount = draft.amount.takeIf { !repeat.variable },
                currency = currency,
                variable = repeat.variable,
                frequency = repeat.frequency,
                anchorDate = repeat.anchorDate,
                startDate = expense.date,
                lastOccurrence = expense.date,
                payerId = computed.payers.first().personId,
                split =
                    RuleSplit(
                        SplitMode.Equal,
                        computed.split.rows.filter { it.included }.map { it.personId },
                    ),
                createdAt = ctx.at,
            )
        next = next.copy(recurringRules = next.recurringRules + rule)
    }
    return next to id
}

/**
 * Edits an expense: shares are recomputed only when the amount, people or split changed; one
 * history entry per changed field; any flag is cleared.
 */
fun Ledger.updateExpense(id: String, draft: ExpenseDraft, ctx: ActionContext): Ledger {
    val old = requireNotNull(expense(id)) { "No expense $id" }
    val currency = old.groupId?.let(::group)?.currency ?: draft.currency ?: old.currency
    // Who is on it and how it's split, apart from the amounts that follow the total.
    val shapeChanged =
        draft.split.mode != old.split.mode ||
            draft.split.rows.map { Triple(it.personId, it.included, it.value) } !=
                old.split.rows.map { Triple(it.personId, it.included, it.value) }
    val splitChanged =
        draft.amount != old.amount ||
            shapeChanged ||
            (draft.payers.isNotEmpty() && draft.payers != old.payers)
    // No payers in the draft keeps who paid (one payer now paying the new amount).
    val payers =
        draft.payers.ifEmpty {
            if (old.payers.size == 1) listOf(Payer(old.payerId, draft.amount)) else old.payers
        }
    val computed =
        if (splitChanged) {
            computeSplit(draft.copy(groupId = old.groupId, payers = payers), currency)
        } else {
            null
        }
    val history = old.history.toMutableList()
    fun log(kind: HistoryKind, oldValue: JsonPrimitive? = null, newValue: JsonPrimitive? = null) {
        history += HistoryEntry(kind, ctx.at, ME, oldValue, newValue)
    }
    val title = draft.title.trim().ifEmpty { old.title }
    if (draft.amount != old.amount)
        log(HistoryKind.AmountChanged, JsonPrimitive(old.amount), JsonPrimitive(draft.amount))
    if (title != old.title)
        log(HistoryKind.TitleChanged, JsonPrimitive(old.title), JsonPrimitive(title))
    val date = draft.date ?: old.date
    if (date != old.date)
        log(
            HistoryKind.DateChanged,
            JsonPrimitive(old.date.toString()),
            JsonPrimitive(date.toString()),
        )
    if (draft.category != old.category)
        log(HistoryKind.CategoryChanged, JsonPrimitive(old.category), JsonPrimitive(draft.category))
    if (shapeChanged) log(HistoryKind.SplitChanged)
    // A single payer whose amount just follows the new total isn't a payer change.
    val payersChanged =
        computed != null &&
            (computed.payers.map { it.personId } != old.payers.map { it.personId } ||
                (old.payers.size > 1 && computed.payers != old.payers))
    if (payersChanged) log(HistoryKind.PayersChanged)
    if (draft.receipt != null && old.receipt == null) log(HistoryKind.ReceiptAdded)
    val updated =
        old.copy(
            title = title,
            category = draft.category,
            amount = draft.amount,
            currency = currency,
            rate = draft.rate ?: old.rate,
            date = date,
            dueDate = draft.dueDate,
            payers = computed?.payers ?: old.payers,
            split = computed?.split ?: old.split,
            itemized = draft.itemized ?: old.itemized,
            notes = draft.notes,
            receipt = draft.receipt ?: old.receipt,
            history = history,
            flag = null,
        )
    val next = copy(expenses = expenses.replacing({ it.id == id }) { updated })
    return if (computed == null) next
    else next.copy(rotation = next.rotation + (computed.key to computed.counter))
}

/** Soft delete: kept in Recently deleted for 30 days. */
fun Ledger.deleteExpense(id: String, ctx: ActionContext, by: String = ME): Ledger =
    editExpense(id) {
        it.copy(
            deletedAt = ctx.at,
            deletedBy = by,
            history = it.history + HistoryEntry(HistoryKind.Deleted, ctx.at, by),
        )
    }

fun Ledger.restoreExpense(id: String, ctx: ActionContext, by: String = ME): Ledger =
    editExpense(id) {
        it.copy(
            deletedAt = null,
            deletedBy = null,
            history = it.history + HistoryEntry(HistoryKind.Restored, ctx.at, by),
        )
    }

fun Ledger.addComment(
    expenseId: String,
    text: String,
    ctx: ActionContext,
    by: String = ME,
): Ledger {
    ensure(text.isNotBlank()) { "Write a comment first." }
    return editExpense(expenseId) {
        it.copy(comments = it.comments + Comment(newId(), by, ctx.at, text.trim()))
    }
}

/** "Flag an issue": the expense is Disputed but still counts. */
fun Ledger.flagExpense(id: String, by: String, note: String, ctx: ActionContext): Ledger =
    editExpense(id) {
        it.copy(
            flag = Flag(by, note, ctx.at),
            history = it.history + HistoryEntry(HistoryKind.Flagged, ctx.at, by),
        )
    }

/** The flagger takes their flag back. */
fun Ledger.removeFlag(id: String, ctx: ActionContext, by: String = ME): Ledger =
    editExpense(id) {
        it.copy(
            flag = null,
            history = it.history + HistoryEntry(HistoryKind.FlagRemoved, ctx.at, by),
        )
    }

/** Resolve on a disputed expense ("You resolved Esha’s flag"). */
fun Ledger.resolveFlag(id: String, ctx: ActionContext, by: String = ME): Ledger =
    editExpense(id) {
        it.copy(
            flag = null,
            history = it.history + HistoryEntry(HistoryKind.FlagResolved, ctx.at, by),
        )
    }

/** Creates the expense of a variable rule's draft (dated the occurrence) and links the draft. */
fun Ledger.enterDraftAmount(
    draftId: String,
    amount: Long,
    ctx: ActionContext,
): Pair<Ledger, String> {
    val draft = requireNotNull(draft(draftId)) { "No draft $draftId" }
    val rule = requireNotNull(rule(draft.ruleId)) { "No rule ${draft.ruleId}" }
    ensure(amount > 0) { "Enter an amount." }
    val (next, expenseId) =
        addExpense(
            ExpenseDraft.equal(rule.split.personIds)
                .copy(
                    title = rule.title,
                    amount = amount,
                    currency = rule.currency,
                    groupId = rule.groupId,
                    category = rule.category,
                    date = draft.occurrenceDate,
                    payers = listOf(Payer(rule.payerId, amount)),
                ),
            ctx,
        )
    val linked =
        next.copy(
            expenses =
                next.expenses.replacing({ it.id == expenseId }) {
                    it.copy(recurringRuleId = rule.id, occurrenceDate = draft.occurrenceDate)
                },
            drafts = next.drafts.replacing({ it.id == draftId }) { it.copy(expenseId = expenseId) },
        )
    return linked to expenseId
}

private fun Ledger.editExpense(id: String, change: (Expense) -> Expense): Ledger {
    requireNotNull(expense(id)) { "No expense $id" }
    return copy(expenses = expenses.replacing({ it.id == id }, change))
}

private data class ComputedSplit(
    val payers: List<Payer>,
    val split: Split,
    val key: String,
    val counter: Int,
)

/** Validates a draft and computes its shares with the context's rotation counter (§4). */
private fun Ledger.computeSplit(draft: ExpenseDraft, currency: String): ComputedSplit {
    ensure(draft.amount > 0) { "Enter an amount." }
    val included = draft.split.rows.filter { it.included }.map { it.personId }
    ensure(included.any { it != ME } || draft.payers.any { it.personId != ME }) {
        "Add someone to split with."
    }
    val payers = draft.payers.ifEmpty { listOf(Payer(ME, draft.amount)) }
    ensure(payers.sumOf { it.amount } == draft.amount) {
        "The payers’ amounts must add up to the total."
    }
    val group = draft.groupId?.let(::group)
    val order =
        if (group != null) {
            group.memberIds.filter { it in included } + included.filter { it !in group.memberIds }
        } else {
            included.filter { it == ME } + included.filter { it != ME }
        }
    val key = draft.groupId ?: order.sorted().joinToString("+")
    val counter = rotation[key] ?: 0
    val values =
        draft.split.rows.filter { it.included }.associate { it.personId to (it.value ?: 0L) }
    val result: SplitResult =
        when (draft.split.mode) {
            SplitMode.Equal -> Splits.equal(draft.amount, order, counter)
            SplitMode.Exact -> {
                val status = Splits.exactStatus(draft.amount, values.values, currency)
                ensure(status.remaining == 0L) { status.left }
                SplitResult(values, counter)
            }
            SplitMode.Percent -> {
                ensure(values.values.sum() == PERCENT_TOTAL_BPS) {
                    "The percentages must add up to 100%."
                }
                Splits.weighted(draft.amount, values, order, counter)
            }
            SplitMode.Shares -> {
                ensure(values.values.all { it >= 1 }) { "Everyone needs at least 1 share." }
                Splits.weighted(draft.amount, values, order, counter)
            }
            SplitMode.Itemized -> {
                val items = requireNotNull(draft.itemized) { "An itemized split needs its items" }
                Splits.itemized(
                        items.items.map { it.amount to it.personIds },
                        draft.amount,
                        order,
                        counter,
                    )
                    .first
            }
        }
    val rows =
        draft.split.rows.map {
            it.copy(share = if (it.included) result.shares[it.personId] ?: 0 else 0)
        }
    return ComputedSplit(payers, Split(draft.split.mode, rows), key, result.counter)
}
