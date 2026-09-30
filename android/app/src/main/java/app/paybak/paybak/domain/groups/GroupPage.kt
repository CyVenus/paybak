package app.paybak.paybak.domain.groups

import app.paybak.paybak.domain.calc.GroupSheet
import app.paybak.paybak.domain.calc.LedgerView
import app.paybak.paybak.domain.calc.groupSheet
import app.paybak.paybak.domain.calc.joinNames
import app.paybak.paybak.domain.calc.recurring
import app.paybak.paybak.domain.format.Dates
import app.paybak.paybak.domain.format.Money
import app.paybak.paybak.domain.format.MoneySign
import app.paybak.paybak.domain.model.Expense
import app.paybak.paybak.domain.model.Group
import app.paybak.paybak.domain.model.GroupType
import app.paybak.paybak.domain.model.ME
import java.time.LocalDate

/** What a group's Settle up does: pay the one person you owe there, or open its Settle up plan. */
sealed interface GroupSettle {
    data class Pay(val toId: String, val amount: Long, val currency: String, val groupId: String) :
        GroupSettle

    data class Plan(val groupId: String) : GroupSettle
}

/** The group's "Your balance" card (screens-groups §2.1, record-lend-group §7). */
data class GroupBalanceCard(
    val standing: Standing,
    /** "−₹1,400", "+₹600", "Settled", or "₹0" for a group with nothing in it yet. */
    val amount: String,
    val caption: String?,
    /** Null: no Settle up button. */
    val settle: GroupSettle?,
    /** A new group shows Settle up disabled until there's something to settle. */
    val showsDisabledAction: Boolean = false,
)

/** A member's paid vs share in the Balances card: "Paid ₹6,500 · Share ₹7,900", "−₹1,400". */
data class MemberBalanceRow(
    val personId: String,
    val name: String,
    val subtitle: String,
    val standing: Standing,
    val amount: String?,
    val label: String?,
)

/** A group expense: "Dev paid · Your share ₹500", the whole amount, and the ≈ ₹ line abroad. */
data class GroupExpenseRow(
    val expense: Expense,
    val subtitle: String,
    val amount: String,
    val detail: String?,
)

/** One date of the Expenses list: "Fri 25 Sep" and its expenses, newest first. */
data class ExpenseDay(val date: LocalDate, val label: String, val rows: List<GroupExpenseRow>)

/** Group detail (screens-groups §4); [isEmpty] is the new group's state (record-lend-group §7). */
data class GroupPage(
    val group: Group,
    val subtitle: String,
    /** Every member, you first: the header's avatar stack shows the first four. */
    val memberIds: List<String>,
    val balance: GroupBalanceCard,
    val members: List<MemberBalanceRow>,
    /** The simplify line, then the foreign-currency total line. */
    val notes: List<String>,
    val days: List<ExpenseDay>,
) {
    val isEmpty: Boolean
        get() = days.isEmpty()
}

fun LedgerView.groupPage(groupId: String): GroupPage? {
    val sheet = groupSheet(groupId) ?: return null
    val group = sheet.group
    val members = "${group.memberIds.size} members"
    val subtitle =
        when {
            sheet.expenses.isEmpty() ->
                listOfNotNull(group.type?.label, members, group.currency).joinToString(" · ")
            group.currency != defaultCurrency ->
                listOfNotNull(dateRange(sheet.expenses), members, group.currency)
                    .joinToString(" · ")
            else -> sheet.titleRow
        }
    return groupPage(subtitle, sheet)
}

private fun LedgerView.groupPage(subtitle: String, sheet: GroupSheet): GroupPage {
    val group = sheet.group
    val order = group.membersYouFirst()
    val byId = sheet.members.associateBy { it.personId }
    return GroupPage(
        group = group,
        subtitle = subtitle,
        memberIds = order,
        balance = balanceCard(sheet),
        members =
            order.mapNotNull(byId::get).map { member ->
                val you = member.personId == ME
                MemberBalanceRow(
                    personId = member.personId,
                    name = first(member.personId),
                    subtitle =
                        "Paid ${Money.format(member.paid, group.currency)} · " +
                            "Share ${Money.format(member.share, group.currency)}",
                    standing = standingOf(member.net),
                    amount =
                        if (member.net == 0L) null else Money.format(member.net, group.currency),
                    label =
                        when {
                            member.net < 0 -> if (you) "You owe" else "Owes"
                            member.net > 0 -> if (you) "You’re owed" else "Gets back"
                            else -> null
                        },
                )
            },
        notes = listOfNotNull(sheet.footnote, sheet.totalLine),
        days =
            sheet.expenses
                .groupBy { it.date }
                .toSortedMap(compareByDescending { it })
                .map { (date, expenses) ->
                    ExpenseDay(
                        date,
                        if (date.year == today.year) Dates.day(date)
                        else "${Dates.day(date)} ${date.year}",
                        expenses.sortedByDescending { it.createdAt }.map { expense ->
                            GroupExpenseRow(
                                expense = expense,
                                subtitle = expenseSubtitle(expense),
                                amount = Money.format(expense.amount, expense.currency),
                                detail = sheet.rateLines[expense.id],
                            )
                        },
                    )
                },
    )
}

/** "Dev paid · Your share ₹500"; "Kabir paid" when you have no share. */
private fun LedgerView.expenseSubtitle(expense: Expense): String {
    val payer = if (expense.payerId == ME) "You paid" else "${first(expense.payerId)} paid"
    val share = myShare(expense)
    return if (share == 0L) payer
    else "$payer · Your share ${Money.format(share, expense.currency)}"
}

/** "21–25 Sep", or null with no expenses. */
private fun dateRange(expenses: List<Expense>): String? {
    val dates = expenses.map { it.date }
    return if (dates.isEmpty()) null else Dates.range(dates.min(), dates.max())
}

private fun LedgerView.balanceCard(sheet: GroupSheet): GroupBalanceCard {
    val group = sheet.group
    val net = sheet.myNet
    val plan = sheet.plan
    val due = group.settleBy?.let { " · ${Dates.dueLabel(it)}" }.orEmpty()
    val amount = Money.format(net, group.currency, MoneySign.Signed)
    return when {
        net < 0 -> {
            val payees = plan.filter { it.debtorId == ME }.map { it.creditorId }
            GroupBalanceCard(
                Standing.Owe,
                amount,
                "You owe ${joinNames(payees.map(::first))}$due",
                groupSettle(group),
            )
        }
        net > 0 -> {
            val debtors = plan.filter { it.creditorId == ME }.map { it.debtorId }
            val who =
                if (debtors.size == 1) "${first(debtors.single())} owes you"
                else "${debtors.size} people owe you"
            GroupBalanceCard(Standing.Owed, amount, "$who$due", GroupSettle.Plan(group.id))
        }
        sheet.expenses.isEmpty() ->
            GroupBalanceCard(
                Standing.Settled,
                Money.format(0, group.currency),
                caption = null,
                settle = null,
                showsDisabledAction = true,
            )
        else ->
            GroupBalanceCard(
                Standing.Settled,
                "Settled",
                sheet.settledCaption ?: "Nothing pending",
                settle = null,
            )
    }
}

/**
 * Settle up in [group] from your side: the prefilled payment when you pay one person there, the
 * group's Settle up plan when you pay several or are owed; null when you're square.
 */
fun LedgerView.groupSettle(group: Group): GroupSettle? {
    val net = groupNets(group.id)[ME] ?: 0
    if (net == 0L) return null
    val mine = groupPlan(group.id).filter { it.debtorId == ME }
    val only = mine.singleOrNull()
    return if (net < 0 && only != null) {
        GroupSettle.Pay(only.creditorId, only.amount, group.currency, group.id)
    } else {
        GroupSettle.Plan(group.id)
    }
}

/** Leave group (§2.9): blocked while your balance there isn't 0, otherwise a confirmation. */
sealed interface LeaveCheck {
    data class Blocked(val message: String, val settle: GroupSettle) : LeaveCheck

    data class Confirm(val title: String, val message: String) : LeaveCheck
}

fun LedgerView.leaveCheck(groupId: String): LeaveCheck? {
    val group = group(groupId) ?: return null
    val net = groupNets(groupId)[ME] ?: 0
    val settle = groupSettle(group)
    if (net == 0L || settle == null) {
        return LeaveCheck.Confirm(
            "Leave ${group.name}?",
            "You’ll stop seeing this group. Its history stays with the other members.",
        )
    }
    val amount = Money.format(net, group.currency)
    val plan = groupPlan(groupId)
    val message =
        if (net < 0) {
            val payee = (settle as? GroupSettle.Pay)?.let { " with ${first(it.toId)}" }.orEmpty()
            "You owe $amount in ${group.name}. Settle up$payee first, then you can leave."
        } else {
            val debtor = plan.filter { it.creditorId == ME }.singleOrNull()
            val who = debtor?.let { "${first(it.debtorId)} owes you" } ?: "You’re owed"
            "$who $amount in ${group.name}. Settle up first, then you can leave."
        }
    return LeaveCheck.Blocked(message, settle)
}

/** Who you are in a members list: the profile isn't in the ledger. */
data class MemberIdentity(val name: String, val upi: String?, val username: String?)

/** A row of Group settings › Members: "Kabir Singh" · "kabir@okaxis". */
data class SettingsMember(
    val personId: String,
    val name: String,
    val subtitle: String?,
    val guest: Boolean,
)

/** Group settings (screens-groups §5). */
data class GroupSettingsPage(
    val group: Group,
    /** "Fri 2 Oct" or "None". */
    val settleBy: String,
    val members: List<SettingsMember>,
    /** "INR ₹", "AED". */
    val currency: String,
    /** "None", "1 rule", "3 rules". */
    val recurring: String,
)

fun LedgerView.groupSettingsPage(groupId: String, me: MemberIdentity): GroupSettingsPage? {
    val group = group(groupId) ?: return null
    val rules = recurring(groupId).rules.size
    return GroupSettingsPage(
        group = group,
        settleBy = group.settleBy?.let(Dates::day) ?: "None",
        members =
            group.membersYouFirst().map { id ->
                if (id == ME) {
                    val handle = me.upi ?: me.username?.let { "@$it" }
                    SettingsMember(ME, "${me.name} (you)", handle, guest = false)
                } else {
                    val person = person(id)
                    SettingsMember(
                        id,
                        person?.name ?: first(id),
                        person?.run { upi ?: username?.let { "@$it" } ?: contact },
                        person?.isGuest == true,
                    )
                }
            },
        currency = currencyLabel(group.currency),
        recurring =
            when (rules) {
                0 -> "None"
                1 -> "1 rule"
                else -> "$rules rules"
            },
    )
}

/** "INR ₹" ("{CODE} {symbol}"), or the code alone when it is its own symbol ("AED"). */
fun currencyLabel(code: String): String {
    val symbol = Money.currency(code).symbol
    return if (symbol == code) code else "$code $symbol"
}

/** The New group type chip names, the first word of an empty group's subtitle. */
val GroupType.label: String
    get() =
        when (this) {
            GroupType.Trip -> "Trip"
            GroupType.Home -> "Home"
            GroupType.Friends -> "Friends"
            GroupType.Other -> "Other"
        }
