package app.paybak.paybak.feature.addexpense

import androidx.compose.runtime.saveable.Saver
import app.paybak.paybak.domain.addrecord.AmountEntry
import app.paybak.paybak.domain.addrecord.SplitDraft
import app.paybak.paybak.domain.addrecord.SplitPreview
import app.paybak.paybak.domain.calc.Rates
import app.paybak.paybak.domain.calc.Splits
import app.paybak.paybak.domain.model.Category
import app.paybak.paybak.domain.model.Day
import app.paybak.paybak.domain.model.Expense
import app.paybak.paybak.domain.model.ExpenseDraft
import app.paybak.paybak.domain.model.Itemized
import app.paybak.paybak.domain.model.LedgerJson
import app.paybak.paybak.domain.model.ME
import app.paybak.paybak.domain.model.Payer
import app.paybak.paybak.domain.model.Rate
import app.paybak.paybak.domain.model.Receipt
import app.paybak.paybak.domain.model.RepeatRule
import app.paybak.paybak.domain.model.Split
import app.paybak.paybak.domain.model.SplitMode
import app.paybak.paybak.navigation.RouteResult
import java.time.Instant
import kotlinx.serialization.Serializable

/**
 * Everything the Add expense form holds (add-expense §3): the typed amount, the people (you first,
 * then the others as added), who paid, the split and the optional rows. It saves as JSON, so it
 * survives rotation and process death.
 *
 * @param payerAmounts What each person paid, typed in the payer editor ("Multiple people"); null
 *   while one person ([payerId]) paid it all.
 * @param category Null until one is chosen ("Choose").
 */
@Serializable
data class ExpenseForm(
    val amount: String = "",
    val currency: String,
    val rate: Rate? = null,
    val title: String = "",
    val people: List<String> = listOf(ME),
    val payerId: String = ME,
    val payerAmounts: Map<String, String>? = null,
    val split: SplitDraft = SplitDraft(),
    val category: String? = null,
    val groupId: String? = null,
    val date: Day,
    val dueDate: Day? = null,
    val repeat: RepeatRule? = null,
    val receipt: Receipt? = null,
    val notes: String = "",
    val itemized: Itemized? = null,
) {
    val total: Long
        get() = AmountEntry.minor(amount, currency)

    /** The people on it besides you: the form's chips. */
    val others: List<String>
        get() = people.filter { it != ME }

    /** Who can have paid: you, then the others on the expense. */
    val payerChoices: List<String>
        get() = listOf(ME) + others

    /** Sets who is on the expense, keeping the split, payers and values of those who stay. */
    fun withPeople(next: List<String>): ExpenseForm {
        val ordered = next.filter { it == ME } + next.filter { it != ME }.distinct()
        val choices = listOf(ME) + ordered.filter { it != ME }
        return copy(
            people = ordered,
            split = split.keepOnly(ordered),
            payerId = payerId.takeIf { it in choices } ?: ME,
            payerAmounts = payerAmounts?.filterKeys { it in choices },
            itemized = itemized.takeIf { split.mode == SplitMode.Itemized && ordered == people },
        )
    }

    /** The split preview for the current total, in the ledger's rotation order. */
    fun preview(order: List<String>, counter: Int): SplitPreview =
        split.preview(total, people, currency, order, counter, itemized)

    /** The payers as saved: one person paying it all, or each typed amount above 0. */
    fun payers(): List<Payer> =
        payerAmounts
            ?.map { (id, text) -> Payer(id, AmountEntry.minor(text, currency)) }
            ?.filter { it.amount > 0 } ?: listOf(Payer(payerId, total))

    /** Whether the payers' amounts add up to the total. */
    val payersBalanced: Boolean
        get() = payerAmounts == null || payers().sumOf { it.amount } == total

    /** Saves today's rate for a currency other than [defaultCurrency] (add-expense §3.9). */
    fun withRate(rates: Rates, defaultCurrency: String): ExpenseForm =
        copy(
            rate =
                if (currency == defaultCurrency) null
                else rate ?: rates.rate(currency, defaultCurrency)
        )

    /** A receipt scan's result: its amount, items and split, and the photo attached. */
    fun applying(result: RouteResult.Receipt, now: Instant): ExpenseForm {
        val photo = result.result.photo?.let { Receipt(photo = it, addedAt = now) } ?: receipt
        // An unreadable receipt ("Attach photo") only attaches the photo; the typed form stays.
        if (result.result.scan == null) return copy(receipt = photo)
        val scanned = new(result.result.draft, currency, date)
        return scanned.copy(
            groupId = groupId,
            dueDate = dueDate,
            notes = notes,
            repeat = repeat,
            title = scanned.title.ifEmpty { title },
            receipt = photo,
        )
    }

    fun toDraft(): ExpenseDraft =
        ExpenseDraft(
            title = title.trim(),
            amount = total,
            currency = currency,
            rate = rate,
            groupId = groupId,
            category = category ?: Category.Other.id,
            date = date,
            dueDate = dueDate,
            payers = payers(),
            split = Split(split.mode, split.rows(people, currency)),
            itemized = itemized.takeIf { split.mode == SplitMode.Itemized },
            notes = notes.trim().ifEmpty { null },
            receipt = receipt,
            repeat = repeat,
        )

    companion object {
        /** A new expense, prefilled by [draft] when it has one (a group's members, Ask Paybak). */
        fun new(draft: ExpenseDraft?, currency: String, today: Day): ExpenseForm {
            if (draft == null) return ExpenseForm(currency = currency, date = today)
            val drafted = draft.currency ?: currency
            return of(draft, drafted, today)
        }

        /** Edit mode: the form holding [expense]. */
        fun of(expense: Expense): ExpenseForm =
            of(
                ExpenseDraft(
                    title = expense.title,
                    amount = expense.amount,
                    currency = expense.currency,
                    rate = expense.rate,
                    groupId = expense.groupId,
                    category = expense.category,
                    date = expense.date,
                    dueDate = expense.dueDate,
                    payers = expense.payers,
                    split = expense.split,
                    itemized = expense.itemized,
                    notes = expense.notes,
                    receipt = expense.receipt,
                ),
                expense.currency,
                expense.date,
            )

        private fun of(draft: ExpenseDraft, currency: String, today: Day): ExpenseForm {
            val people = draft.personIds.ifEmpty { listOf(ME) }
            val payers = draft.payers
            return ExpenseForm(
                amount = AmountEntry.text(draft.amount, currency),
                currency = currency,
                rate = draft.rate,
                title = draft.title,
                people = people.filter { it == ME } + people.filter { it != ME },
                payerId = payers.singleOrNull()?.personId ?: ME,
                payerAmounts =
                    payers
                        .takeIf { it.size > 1 }
                        ?.associate { it.personId to AmountEntry.text(it.amount, currency) },
                split = SplitDraft.of(draft.split, currency),
                category = draft.category.takeIf { draft.amount > 0 || it != Category.Other.id },
                groupId = draft.groupId,
                date = draft.date ?: today,
                dueDate = draft.dueDate,
                repeat = draft.repeat,
                receipt = draft.receipt,
                notes = draft.notes.orEmpty(),
                itemized = draft.itemized,
            )
        }

        val Saver: Saver<ExpenseForm, String> =
            Saver(
                save = { LedgerJson.encodeToString(serializer(), it) },
                restore = { LedgerJson.decodeFromString(serializer(), it) },
            )

        /** Equal payer amounts to start the payer editor with: you paying it all. */
        fun startingPayers(form: ExpenseForm): Map<String, String> =
            form.payerAmounts
                ?: mapOf(
                    form.payerId to AmountEntry.text(form.total, form.currency).ifEmpty { "0" }
                )

        /** The typed total of [amounts] against [total]: the payer editor's footer. */
        fun payerStatus(amounts: Map<String, String>, total: Long, currency: String) =
            Splits.exactStatus(
                total,
                amounts.values.map { AmountEntry.minor(it, currency) },
                currency,
            )
    }
}
