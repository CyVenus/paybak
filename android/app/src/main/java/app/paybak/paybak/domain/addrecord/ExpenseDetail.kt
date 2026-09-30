package app.paybak.paybak.domain.addrecord

import app.paybak.paybak.domain.calc.LedgerView
import app.paybak.paybak.domain.format.Dates
import app.paybak.paybak.domain.format.Money
import app.paybak.paybak.domain.format.MoneySign
import app.paybak.paybak.domain.model.Category
import app.paybak.paybak.domain.model.Expense
import app.paybak.paybak.domain.model.HistoryEntry
import app.paybak.paybak.domain.model.HistoryKind
import app.paybak.paybak.domain.model.ME
import app.paybak.paybak.domain.model.SplitMode

/** One person in the detail's split card: the payer first ("Paid ₹2,800"), then you. */
data class SplitLine(
    val personId: String,
    val name: String,
    val share: String,
    val paid: String?,
    val isGuest: Boolean,
)

/** "Your Goa Trip balance": signed, and how it reads (owed black, owe grey, settled light). */
data class GroupBalanceLine(
    val groupId: String,
    val title: String,
    val value: String,
    val net: Long,
)

data class CommentLine(val personId: String, val name: String, val date: String, val text: String)

data class HistoryLine(val text: String, val date: String)

/** The expense detail template (activity §4, add-expense §11), ready to draw. */
data class ExpenseDetail(
    val expense: Expense,
    val category: Category,
    val title: String,
    val amount: String,
    val meta: String,
    val tags: List<String>,
    val status: String?,
    val yourShare: String?,
    val due: String?,
    val groupBalance: GroupBalanceLine?,
    val splitHeader: String,
    val split: List<SplitLine>,
    val receiptLine: String?,
    val comments: List<CommentLine>,
    val history: List<HistoryLine>,
    val flagTitle: String?,
    val flagBody: String?,
    val canFlag: Boolean,
)

fun LedgerView.expenseDetail(expenseId: String): ExpenseDetail? {
    val expense = expense(expenseId) ?: return null
    val currency = expense.currency
    val group = groupOf(expense)
    val category = Category.of(expense.category)
    val payers = expense.payers.map { it.personId }
    val day = Dates.rowDate(expense.date, today)
    val paidBy =
        when {
            payers.size == 1 -> if (payers[0] == ME) "you" else first(payers[0])
            payers.size == 2 && ME in payers -> "${first(payers.first { it != ME })} and you"
            payers.size == 2 -> "${first(payers[0])} and ${first(payers[1])}"
            else -> "${payers.size} people"
        }
    val approx = expense.rate?.let { Money.approxLine(expense.amount, it.value, currency, it.to) }
    val meta = listOfNotNull("Paid by $paidBy · $day", approx).joinToString("\n")
    val mine = expense.split.rows.firstOrNull { it.personId == ME && it.included }
    val balance = group?.let {
        val net = groupNets(it.id)[ME] ?: 0
        GroupBalanceLine(
            it.id,
            "Your ${it.name} balance",
            Money.format(net, it.currency, MoneySign.Signed),
            net,
        )
    }
    val sharers = expense.split.rows.filter { it.included }
    val memberOrder = group?.memberIds ?: expense.split.rows.map { it.personId }
    val order =
        (payers + listOf(ME) + memberOrder + sharers.map { it.personId }).distinct().filter { id ->
            id in payers || sharers.any { it.personId == id }
        }
    val split = order.map { id ->
        SplitLine(
            personId = id,
            name = first(id),
            share = Money.format(expense.shareOf(id), currency),
            paid =
                expense.paidBy(id).takeIf { it > 0 }?.let { "Paid ${Money.format(it, currency)}" },
            isGuest = person(id)?.isGuest == true,
        )
    }
    val modeText =
        when (expense.split.mode) {
            SplitMode.Equal -> "equally"
            SplitMode.Exact -> "by exact amounts"
            SplitMode.Percent -> "by percentages"
            SplitMode.Shares -> "by shares"
            SplitMode.Itemized -> "by items"
        }
    val receipt =
        expense.receipt?.let {
            val by = if (it.addedBy == ME) "you" else first(it.addedBy)
            "Added by $by · ${Dates.rowDate(localDate(it.addedAt), today)}"
        }
    val flag = expense.flag
    return ExpenseDetail(
        expense = expense,
        category = category,
        title = expense.title,
        amount = Money.format(expense.amount, currency),
        meta = meta,
        tags = listOfNotNull(group?.name, category.label),
        status = if (flag != null) "Disputed" else null,
        yourShare = mine?.let { Money.format(it.share, currency) },
        due = (expense.dueDate ?: group?.settleBy)?.let(Dates::day),
        groupBalance = balance,
        splitHeader =
            "Split $modeText · ${sharers.size} ${if (sharers.size == 1) "person" else "people"}",
        split = split,
        receiptLine = receipt,
        comments =
            expense.comments.map {
                CommentLine(it.by, first(it.by), Dates.rowDate(localDate(it.at), today), it.text)
            },
        history = expense.history.asReversed().map { historyLine(it, expense) },
        flagTitle = flag?.let { "${first(it.by)} flagged this expense" },
        flagBody = flag?.let { "“${it.note}”" },
        canFlag = ME !in payers && flag == null,
    )
}

/** History copy (activity §4.3-F): "Kabir changed the amount from ₹17,500 to ₹18,000". */
private fun LedgerView.historyLine(entry: HistoryEntry, expense: Expense): HistoryLine {
    val actor = first(entry.by)
    fun money(amount: Long?) = amount?.let { Money.format(it, expense.currency) }.orEmpty()
    val text =
        when (entry.kind) {
            HistoryKind.Created -> "$actor added this"
            HistoryKind.AmountChanged ->
                "$actor changed the amount from ${money(entry.oldAmount)} to ${money(entry.newAmount)}"
            HistoryKind.TitleChanged ->
                "$actor changed the title to “${entry.new?.content.orEmpty()}”"
            HistoryKind.DateChanged ->
                "$actor changed the date to " +
                    (entry.new
                        ?.content
                        ?.let { Dates.short(java.time.LocalDate.parse(it)) }
                        .orEmpty())
            HistoryKind.SplitChanged -> "$actor changed the split"
            HistoryKind.PayersChanged -> "$actor changed who paid"
            HistoryKind.CategoryChanged ->
                "$actor changed the category to ${Category.of(entry.new?.content.orEmpty()).label}"
            HistoryKind.ReceiptAdded -> "$actor added a receipt"
            HistoryKind.Flagged -> "$actor flagged this"
            HistoryKind.FlagRemoved -> "$actor removed their flag"
            HistoryKind.FlagResolved -> {
                val flagger =
                    expense.history
                        .lastOrNull { it.kind == HistoryKind.Flagged && it.at <= entry.at }
                        ?.by
                "$actor resolved ${flagger?.let { first(it) + "’s" } ?: "the"} flag"
            }
            HistoryKind.Deleted -> "$actor deleted this"
            HistoryKind.Restored -> "$actor restored this"
        }
    return HistoryLine(text, Dates.rowDate(localDate(entry.at), today))
}
