package app.paybak.paybak.domain.calc

import app.paybak.paybak.domain.format.Dates
import app.paybak.paybak.domain.format.Money
import app.paybak.paybak.domain.model.Expense
import app.paybak.paybak.domain.model.Group
import app.paybak.paybak.domain.model.ME

/** A member's paid, share and net in the group currency (§5.2). */
data class MemberBalance(val personId: String, val paid: Long, val share: Long, val net: Long)

/** The group detail's numbers (§5.2–5.4). Foreign groups add the "≈ ₹" lines. */
data class GroupSheet(
    val group: Group,
    val spent: Long,
    val members: List<MemberBalance>,
    val plan: List<Transfer>,
    val expenses: List<Expense>,
    /** "21–25 Sep · 5 members · ₹39,500 spent". */
    val titleRow: String,
    /** "Simplify debts is on. You, Priya, Esha and Dev each pay Kabir directly." */
    val footnote: String?,
    /** Per expense id: "≈ ₹21,936 · ₹22.85 per AED". */
    val rateLines: Map<String, String>,
    /** "Total AED 1,800 · ≈ ₹41,118 at saved rates". */
    val totalLine: String?,
    /** Your net in the group currency. */
    val myNet: Long,
    /** "You paid Kabir AED 60 on 14 Mar" once you're settled after paying. */
    val settledCaption: String?,
)

fun LedgerView.groupSheet(groupId: String): GroupSheet? {
    val group = group(groupId) ?: return null
    val (paid, share) = groupPaidShare(groupId)
    val nets = groupNets(groupId)
    val plan = groupPlan(groupId)
    val expenses = liveExpenses().filter { it.groupId == groupId }.sortedByDescending { it.date }
    val spent = paid.values.sum()
    val range =
        expenses
            .map { it.date }
            .let { dates ->
                if (dates.isEmpty()) null else Dates.range(dates.min(), dates.max())
            }
    val titleRow =
        listOfNotNull(
                range,
                membersLabel(group.memberIds.size),
                "${Money.format(spent, group.currency)} spent",
            )
            .joinToString(" · ")
    val foreign = group.currency != defaultCurrency
    val rateLines =
        if (!foreign) emptyMap()
        else
            expenses
                .filter { it.rate != null }
                .associate {
                    it.id to Money.approxLine(it.amount, it.rate!!.value, it.currency, it.rate.to)
                }
    val totalLine =
        if (!foreign || expenses.isEmpty()) null
        else {
            val converted = expenses.sumOf { toDefault(it.amount, it.currency, it.rate) }
            "Total ${Money.format(spent, group.currency)} · ≈ ${Money.format(converted, defaultCurrency)} at saved rates"
        }
    val myNet = nets[ME] ?: 0
    val lastMine =
        confirmedPayments()
            .filter { it.groupId == groupId && ME in setOf(it.fromId, it.toId) }
            .maxByOrNull { it.confirmedAt!! }
    val settledCaption =
        if (myNet != 0L || lastMine == null) null
        else if (lastMine.fromId == ME) {
            "You paid ${first(lastMine.toId)} ${Money.format(lastMine.amount, lastMine.currency)} on ${Dates.short(lastMine.date)}"
        } else {
            "${first(lastMine.fromId)} paid you ${Money.format(lastMine.amount, lastMine.currency)} on ${Dates.short(lastMine.date)}"
        }
    return GroupSheet(
        group = group,
        spent = spent,
        members =
            group.memberIds.map { MemberBalance(it, paid[it] ?: 0, share[it] ?: 0, nets[it] ?: 0) },
        plan = plan,
        expenses = expenses,
        titleRow = titleRow,
        footnote = simplifyFootnote(group, plan),
        rateLines = rateLines,
        totalLine = totalLine,
        myNet = myNet,
        settledCaption = settledCaption,
    )
}

/**
 * "Simplify debts is on. You, Priya, Esha and Dev each pay Kabir directly." with one creditor,
 * "Everyone settles in {n} payments." with several (§5.3).
 */
fun LedgerView.simplifyFootnote(group: Group, plan: List<Transfer>): String? {
    if (!group.simplifyDebts || plan.isEmpty()) return null
    val creditors = plan.map { it.creditorId }.distinct()
    if (creditors.size > 1)
        return "Simplify debts is on. Everyone settles in ${plan.size} payments."
    val debtors =
        group.memberIds.filter { member -> plan.any { it.debtorId == member } }.map(::first)
    val who = joinNames(debtors)
    val each = if (debtors.size > 1) "each pay" else if (debtors.first() == "You") "pay" else "pays"
    return "Simplify debts is on. $who $each ${first(creditors.single())} directly."
}

/** "1 member", "5 members". */
fun membersLabel(count: Int): String = "$count member" + if (count == 1) "" else "s"

/** "A", "A and B", "A, B and C". */
fun joinNames(names: List<String>): String =
    if (names.size <= 1) names.firstOrNull().orEmpty()
    else names.dropLast(1).joinToString(", ") + " and " + names.last()
