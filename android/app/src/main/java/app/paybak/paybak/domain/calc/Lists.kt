package app.paybak.paybak.domain.calc

import app.paybak.paybak.domain.format.Dates
import app.paybak.paybak.domain.format.Money
import app.paybak.paybak.domain.model.Group
import app.paybak.paybak.domain.model.ME
import app.paybak.paybak.domain.model.Person
import java.time.LocalDate

/**
 * A friend's overall balance (§5.5): [net] > 0 means they owe you. [lead] is the earliest-due open
 * item; [badge] its "Overdue 3 days" / "Due Fri".
 */
data class FriendBalance(
    val person: Person,
    val net: Long,
    val items: List<Obligation>,
    val lead: Obligation?,
    val due: LocalDate?,
    val overdue: Boolean,
    val badge: String?,
)

/**
 * A row of the Groups list (§6.5). [net] is your net in the group currency; [openItems] are your
 * open debts there (in the default currency); [due] the first one's due date.
 */
data class GroupSummary(
    val group: Group,
    val net: Long,
    val openItems: List<Obligation>,
    val due: LocalDate?,
    val memberCount: Int,
    val spent: Long?,
    val budget: Long?,
) {
    val archived: Boolean
        get() = group.isArchived

    /** Your net is 0 but others still owe each other ("You’re settled"). */
    val othersOpen: Boolean
        get() = openItems.isEmpty() && net == 0L
}

/** Friends: overdue owed, owed by due date, you owe by due date, then no balance (§6.5). */
fun LedgerView.friendBalances(): List<FriendBalance> {
    val nets = friendNets()
    val items = openItems()
    val order = ledger.people.map { it.id }
    val rows =
        ledger.people.map { person ->
            val net = nets[person.id] ?: 0
            val mine = items.filter { it.friendId == person.id }
            val lead = mine.filter { it.due != null }.minByOrNull { it.due!! } ?: mine.firstOrNull()
            val due = mine.mapNotNull { it.due }.minOrNull()
            FriendBalance(
                person,
                net,
                mine,
                lead,
                due,
                net > 0 && due != null && due.isBefore(today),
                due?.let { Dates.dueBadge(it, today) },
            )
        }
    return rows.sortedWith(
        compareBy(
            {
                when {
                    it.net > 0 && it.overdue -> 0
                    it.net > 0 -> 1
                    it.net < 0 -> 2
                    else -> 3
                }
            },
            { if (it.net == 0L) LocalDate.MAX else it.due ?: LocalDate.MAX },
            { -kotlin.math.abs(it.net) },
            { order.indexOf(it.person.id) },
        )
    )
}

/** Groups and projects you're in: open ones by due date, the rest by name, archived last. */
fun LedgerView.groupSummaries(): List<GroupSummary> {
    val items = openItems()
    return ledger.groups
        .filter { ME in it.memberIds }
        .map { group ->
            val mine = items.filter { it.ref == group.id }
            GroupSummary(
                group = group,
                net = groupNets(group.id)[ME] ?: 0,
                openItems = mine,
                due = mine.firstOrNull()?.due,
                memberCount = group.memberIds.size,
                spent = if (group.isProject) projectSpent(group.id) else null,
                budget = group.project?.budget,
            )
        }
        .sortedWith(
            compareBy(
                { it.archived },
                { it.openItems.isEmpty() },
                { it.due ?: LocalDate.MAX },
                { it.group.name },
            )
        )
}

/** A deleted expense still in Recently deleted: "Deleted by Priya on 24 Sep · 24 days left". */
data class DeletedRow(
    val expenseId: String,
    val title: String,
    val amount: String,
    val caption: String,
    val detail: String,
    val daysLeft: String,
)

const val RETENTION_DAYS = 30L

fun LedgerView.recentlyDeleted(): List<DeletedRow> =
    ledger.expenses
        .filter { it.deletedAt != null }
        .sortedByDescending { it.deletedAt }
        .map { expense ->
            val deletedOn = localDate(expense.deletedAt!!)
            val left = Dates.daysLeft(deletedOn.plusDays(RETENTION_DAYS), today)
            DeletedRow(
                expense.id,
                expense.title,
                Money.format(expense.amount, expense.currency),
                listOfNotNull(
                        Money.format(expense.amount, expense.currency),
                        groupOf(expense)?.name,
                    )
                    .joinToString(" · "),
                "Deleted by ${first(expense.deletedBy ?: ME)} on ${Dates.short(deletedOn)} · $left",
                left,
            )
        }

/** Currencies of your latest records, newest first (the currency picker's Recent section). */
fun LedgerView.recentCurrencies(limit: Int = 3): List<String> =
    (ledger.expenses.sortedByDescending { it.createdAt }.map { it.currency } +
            ledger.payments.sortedByDescending { it.createdAt }.map { it.currency })
        .filter { it != defaultCurrency }
        .distinct()
        .take(limit)
