package app.paybak.paybak.domain.groups

import app.paybak.paybak.domain.calc.FriendBalance
import app.paybak.paybak.domain.calc.GroupSummary
import app.paybak.paybak.domain.calc.LedgerView
import app.paybak.paybak.domain.calc.ObligationKind
import app.paybak.paybak.domain.calc.friendPage
import app.paybak.paybak.domain.calc.projectReport
import app.paybak.paybak.domain.format.Dates
import app.paybak.paybak.domain.format.Money
import app.paybak.paybak.domain.format.MoneySign
import app.paybak.paybak.domain.model.Group
import app.paybak.paybak.domain.model.ME

/** Where the user stands: they owe, they're owed, or square. */
enum class Standing {
    Owe,
    Owed,
    Settled,
}

/** A project's budget line: spent ÷ budget (or the budget point when over) and both captions. */
data class BudgetLine(val progress: Float, val spent: String, val left: String, val over: Boolean)

/**
 * One row of the Groups list or of "Groups together" (screens-groups §2.5–2.6). [amount] is
 * unsigned ("₹1,400") when you owe or are owed; [status] is "Settled", "You’re settled" (a project
 * where others still owe) or, archived, "Read-only".
 */
data class GroupListRow(
    val group: Group,
    val subtitle: String,
    val standing: Standing,
    val amount: String?,
    val status: String?,
    val budget: BudgetLine?,
) {
    val archived: Boolean
        get() = group.isArchived
}

/** The Groups segment: groups and projects by open balance then name, and the Archived ones. */
data class GroupsList(val rows: List<GroupListRow>, val archived: List<GroupListRow>)

fun LedgerView.groupsList(summaries: List<GroupSummary>): GroupsList {
    val rows = summaries.map(::groupListRow)
    return GroupsList(rows.filterNot { it.archived }, rows.filter { it.archived })
}

fun LedgerView.groupListRow(summary: GroupSummary): GroupListRow {
    val group = summary.group
    val net = summary.net
    val standing = standingOf(net)
    val status =
        when {
            group.isArchived -> "Read-only"
            net != 0L -> null
            group.isProject && groupNets(group.id).values.any { it != 0L } -> "You’re settled"
            else -> "Settled"
        }
    return GroupListRow(
        group = group,
        subtitle = groupSubtitle(group, open = net != 0L),
        standing = standing,
        amount = if (net == 0L) null else Money.format(net, group.currency),
        status = status,
        budget = if (group.isArchived) null else budgetLine(group),
    )
}

/**
 * "5 members · Due Fri 2 Oct", "3 members · AED", "Project · 4 members", "Project · Closed 30 Aug".
 * The due date shows while your balance there is open.
 */
fun LedgerView.groupSubtitle(group: Group, open: Boolean): String {
    val members = "${group.memberIds.size} members"
    if (group.isProject) {
        val closed = group.project?.closedAt?.takeIf { group.isArchived }
        return if (closed != null) "Project · Closed ${Dates.short(localDate(closed))}"
        else "Project · $members"
    }
    return listOfNotNull(
            members,
            group.currency.takeIf { it != defaultCurrency },
            group.settleBy?.takeIf { open }?.let(Dates::dueLabel),
        )
        .joinToString(" · ")
}

/** The budget bar of a project row; null for groups and projects without a budget. */
fun LedgerView.budgetLine(group: Group): BudgetLine? {
    if (!group.isProject) return null
    val report = projectReport(group.id) ?: return null
    val budget = report.budget?.takeIf { it > 0 } ?: return null
    val spent = report.spent
    val over = spent > budget
    return BudgetLine(
        progress = if (over) budget.toFloat() / spent else spent.toFloat() / budget,
        spent = "${Money.format(spent, group.currency)} of ${Money.format(budget, group.currency)}",
        left = report.budgetLine.orEmpty(),
        over = over,
    )
}

/**
 * One row of the Friends list (§2.4): the first name, a Guest tag, the lead item as subtitle, and
 * the net with "Owes you" / "You owe" or the red overdue badge; "No balance" until anything is
 * shared, "Settled" after.
 */
data class FriendListRow(
    val balance: FriendBalance,
    val name: String,
    val guest: Boolean,
    val subtitle: String?,
    val standing: Standing,
    val amount: String?,
    val label: String?,
    val overdue: String?,
    val status: String?,
) {
    val personId: String
        get() = balance.person.id
}

/** The Friends summary line: "+₹2,900" owed and "−₹1,850" owe (the Home totals). */
data class FriendsSummary(val owed: Long, val owe: Long, val currency: String) {
    val owedText: String
        get() = Money.format(owed, currency, MoneySign.Signed)

    val oweText: String
        get() = Money.format(-owe, currency, MoneySign.Signed)
}

fun LedgerView.friendsSummary(friends: List<FriendBalance>) =
    FriendsSummary(
        owed = friends.filter { it.net > 0 }.sumOf { it.net },
        owe = -friends.filter { it.net < 0 }.sumOf { it.net },
        currency = defaultCurrency,
    )

fun LedgerView.friendListRow(balance: FriendBalance): FriendListRow {
    val net = balance.net
    val lead = balance.lead
    val subtitle =
        when {
            lead == null -> null
            net > 0 && balance.overdue -> lead.title
            else -> leadCaption(lead.kind, lead.title, lead.due)
        }
    return FriendListRow(
        balance = balance,
        name = balance.person.firstName,
        guest = balance.person.isGuest,
        subtitle = subtitle,
        standing = standingOf(net),
        amount = if (net == 0L) null else Money.format(net, defaultCurrency),
        label =
            when {
                net > 0 && !balance.overdue -> "Owes you"
                net < 0 -> "You owe"
                else -> null
            },
        overdue = balance.badge?.takeIf { net > 0 && balance.overdue },
        status =
            when {
                net != 0L -> null
                sharesAnything(balance.person.id) -> "Settled"
                else -> "No balance"
            },
    )
}

/**
 * The subtitle of an open item: a direct expense is just "Due Sun 4 Oct"; a group, project or loan
 * names itself first ("Goa Trip · Due Fri 2 Oct"); with no due date, the title alone.
 */
internal fun leadCaption(kind: ObligationKind, title: String, due: java.time.LocalDate?): String =
    when {
        due == null -> title
        kind == ObligationKind.Direct -> Dates.dueLabel(due)
        else -> "$title · ${Dates.dueLabel(due)}"
    }

/** Anything recorded with this person: an expense, a payment, a loan or a group together. */
fun LedgerView.sharesAnything(personId: String): Boolean {
    val page = friendPage(personId) ?: return false
    return page.history.isNotEmpty() || page.groupsTogether.isNotEmpty()
}

internal fun standingOf(net: Long): Standing =
    when {
        net < 0 -> Standing.Owe
        net > 0 -> Standing.Owed
        else -> Standing.Settled
    }

/** The members you share [group] with, you first. */
internal fun Group.membersYouFirst(): List<String> =
    listOfNotNull(ME.takeIf { it in memberIds }) + memberIds.filter { it != ME }
