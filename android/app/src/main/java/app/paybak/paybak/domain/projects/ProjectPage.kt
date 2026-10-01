package app.paybak.paybak.domain.projects

import app.paybak.paybak.domain.calc.FairShareRow
import app.paybak.paybak.domain.calc.LedgerView
import app.paybak.paybak.domain.calc.ProjectReport
import app.paybak.paybak.domain.calc.Transfer
import app.paybak.paybak.domain.calc.projectReport
import app.paybak.paybak.domain.format.Dates
import app.paybak.paybak.domain.format.Money
import app.paybak.paybak.domain.format.MoneySign
import app.paybak.paybak.domain.groups.Standing
import app.paybak.paybak.domain.groups.standingOf
import app.paybak.paybak.domain.model.Component
import app.paybak.paybak.domain.model.ComponentStatus
import app.paybak.paybak.domain.model.ContributionRule
import app.paybak.paybak.domain.model.Group
import app.paybak.paybak.domain.model.ME
import app.paybak.paybak.domain.model.PaymentStatus
import app.paybak.paybak.domain.model.ProjectStatus
import java.time.Instant

/** The four designed states of the project detail (screens-projects §10). */
enum class ProjectState {
    Active,
    OverBudget,
    Closed,
    Archived,
}

/**
 * `Card / Budget`'s figures (§1.2). Without a budget only [spent] shows: [budget], [percent] and
 * [left] are null.
 *
 * @param progress Spent ÷ budget; over budget, budget ÷ spent (where the red starts).
 * @param projected (Spent + planned) ÷ budget while Active and under budget, capped at the track.
 * @param left "₹8,000 left", "₹8,000 under budget" or, with [over], "₹1,500 over budget".
 */
data class BudgetFigures(
    val spent: String,
    val budget: String?,
    val progress: Float,
    val projected: Float?,
    val percent: String?,
    val left: String?,
    val over: Boolean,
    val planned: String?,
)

/** One part in the Components card: "Dev · Est. ₹6,000", "₹9,500" or "—" while planned. */
data class ComponentRow(val component: Component, val subtitle: String, val amount: String) {
    /** Whose head leads the row; a planned part shows the tag icon instead. */
    val payerId: String?
        get() = component.paidBy.takeIf { component.counts }
}

/** One "Paid vs fair share" row: "Paid ₹25,500", the unsigned net (or "Settled") and its bar. */
data class ShareRow(
    val personId: String,
    val name: String,
    val caption: String,
    val value: String,
    val standing: Standing,
    val fill: Float,
    val mark: Float,
)

/** What tapping a transfer does: you pay it, you remind the payer, or nothing (others' debt). */
enum class TransferRole {
    YouPay,
    YouReceive,
    Others,
}

/** One transfer of "Who owes whom" or the final settle-up plan: "Rohan owes Dev" ₹8,500. */
data class PlanRow(
    val transfer: Transfer,
    val title: String,
    val amount: String,
    val role: TransferRole,
)

/** A grey card with a lock or a tick: the read-only notice and "Everyone is settled". */
data class ProjectNotice(val title: String, val body: String)

/** An archived project's member and where they ended up ("Settled"). */
data class MemberRow(val personId: String, val name: String, val status: String)

/**
 * The project detail screen (screens-projects §3–§8), from [ProjectReport]: every figure the four
 * states show, in display order.
 *
 * @param shareRule "Equal split · ₹13,000 each so far"; null hides Paid vs fair share (nothing
 *   spent yet, or closed).
 * @param showsPlan Whether the plan section shows at all (hidden while nothing is spent).
 * @param planNotice "Everyone is settled" in place of an empty final plan.
 */
data class ProjectPage(
    val project: Group,
    val state: ProjectState,
    val subtitle: String,
    val notice: ProjectNotice?,
    val budget: BudgetFigures,
    val components: List<ComponentRow>,
    val shareRule: String?,
    val shares: List<ShareRow>,
    val showsPlan: Boolean,
    val plan: List<PlanRow>,
    val planNotice: ProjectNotice?,
    val footnote: String?,
    val members: List<MemberRow>,
) {
    /** Active projects take new parts, edits and settings; closed and archived ones are locked. */
    val editable: Boolean
        get() = state == ProjectState.Active || state == ProjectState.OverBudget

    /** A closed project whose plan is done: the scheduler archives it on its next run. */
    val readyToArchive: Boolean
        get() = state == ProjectState.Closed && plan.isEmpty()
}

fun LedgerView.projectPage(projectId: String): ProjectPage? {
    val report = projectReport(projectId) ?: return null
    val project = report.project
    val info = project.project ?: return null
    val state =
        when (info.status) {
            ProjectStatus.Active -> if (report.overBudget) ProjectState.OverBudget
                else ProjectState.Active
            ProjectStatus.Closed -> ProjectState.Closed
            ProjectStatus.Archived -> ProjectState.Archived
        }
    val active = info.status == ProjectStatus.Active
    val currency = project.currency
    val nets = groupNets(projectId)
    val spentSomething = report.spent > 0
    val plan = report.plan.map { planRow(it, active, currency) }
    val planDone = !active && plan.isEmpty()
    return ProjectPage(
        project = project,
        state = state,
        subtitle = projectSubtitle(project),
        notice =
            when (state) {
                ProjectState.Closed ->
                    ProjectNotice(
                        "Closed · Read-only",
                        "Components are locked. Payments can still be recorded.",
                    )
                ProjectState.Archived -> ProjectNotice("Read-only", "Nothing here can be edited.")
                else -> null
            },
        budget = budgetFigures(report),
        components = report.parts.map { componentRow(it, currency) },
        shareRule = if (active && spentSomething) shareRule(report) else null,
        shares = if (active) report.fairShare.map { shareRow(it, currency) } else emptyList(),
        showsPlan = !active || spentSomething,
        plan = plan,
        planNotice =
            ProjectNotice("Everyone is settled", "No payments left in this project.")
                .takeIf { planDone },
        footnote =
            if (planDone || (active && !spentSomething)) null
            else footnote(projectId, active, plan.isEmpty(), nets[ME] ?: 0, currency),
        members =
            if (state == ProjectState.Archived) {
                project.memberIds.map { id ->
                    val net = nets[id] ?: 0
                    MemberRow(
                        id,
                        first(id),
                        if (net == 0L) "Settled" else Money.format(net, currency, MoneySign.Signed),
                    )
                }
            } else {
                emptyList()
            },
    )
}

/**
 * "Project · 4 members · Active since 10 Aug", "Project · 4 members" once closed, "Project · 4
 * members · Closed 30 Aug" when archived; dates outside this year carry the year.
 */
fun LedgerView.projectSubtitle(project: Group): String {
    val count = project.memberIds.size
    val members = "Project · $count ${if (count == 1) "member" else "members"}"
    val info = project.project ?: return members
    return when (info.status) {
        ProjectStatus.Active -> "$members · Active since ${dayLabel(project.createdAt)}"
        ProjectStatus.Closed -> members
        ProjectStatus.Archived ->
            info.closedAt?.let { "$members · Closed ${dayLabel(it)}" } ?: members
    }
}

private fun LedgerView.dayLabel(moment: Instant): String {
    val day = localDate(moment)
    return if (day.year == today.year) Dates.short(day) else "${Dates.short(day)} ${day.year}"
}

private fun budgetFigures(report: ProjectReport): BudgetFigures {
    val currency = report.project.currency
    val spent = report.spent
    val budget = report.budget?.takeIf { it > 0 }
    val spentText = Money.format(spent, currency)
    if (budget == null) {
        return BudgetFigures(spentText, null, 0f, null, null, null, over = false, planned = null)
    }
    val over = spent > budget
    return BudgetFigures(
        spent = spentText,
        budget = "of ${Money.format(budget, currency)}",
        progress = if (over) budget.toFloat() / spent else spent.toFloat() / budget,
        projected =
            report.projection?.takeIf { !over }?.let { (it.toFloat() / budget).coerceAtMost(1f) },
        percent = "${report.percentUsed}% used",
        left = report.budgetLine,
        over = over,
        planned = report.plannedLine,
    )
}

private fun LedgerView.componentRow(part: Component, currency: String): ComponentRow {
    val estimate = part.estimatedCost?.takeIf { it > 0 }?.let { "Est. ${Money.format(it, currency)}" }
    return if (part.status == ComponentStatus.Planned) {
        ComponentRow(part, estimate ?: "No estimate", Typography.mdash.toString())
    } else {
        ComponentRow(
            part,
            "${first(part.paidBy)} · ${estimate ?: "Unplanned"}",
            Money.format(part.actualCost ?: 0, currency),
        )
    }
}

private fun LedgerView.shareRule(report: ProjectReport): String {
    val rule = report.project.project?.contribution?.rule ?: ContributionRule.Equal
    return when (rule) {
        ContributionRule.Equal -> {
            val each = report.spent / report.project.memberIds.size.coerceAtLeast(1)
            "Equal split · ${Money.format(each, report.project.currency)} each so far"
        }
        ContributionRule.Percent -> "Percent split · shares follow each person’s %"
        ContributionRule.Fixed -> "Fixed amounts · shares follow each person’s amount"
    }
}

private fun LedgerView.shareRow(row: FairShareRow, currency: String): ShareRow {
    val standing = standingOf(row.net)
    return ShareRow(
        personId = row.personId,
        name = first(row.personId),
        caption = "Paid ${Money.format(row.paid, currency)}",
        value = if (standing == Standing.Settled) "Settled" else Money.format(row.net, currency),
        standing = standing,
        fill = row.paidFill,
        mark = row.shareMark,
    )
}

/** "Rohan owes Dev" while Active, "Rohan pays Dev" in the final plan; you are "You" / "you". */
private fun LedgerView.planRow(transfer: Transfer, active: Boolean, currency: String): PlanRow {
    val from = transfer.debtorId
    val to = transfer.creditorId
    val title =
        when {
            from == ME -> "You ${if (active) "owe" else "pay"} ${first(to)}"
            to == ME -> "${first(from)} ${if (active) "owes" else "pays"} you"
            else -> "${first(from)} ${if (active) "owes" else "pays"} ${first(to)}"
        }
    val role =
        when {
            from == ME -> TransferRole.YouPay
            to == ME -> TransferRole.YouReceive
            else -> TransferRole.Others
        }
    return PlanRow(transfer, title, Money.format(transfer.amount, currency), role)
}

/** The line under the plan: where you stand, and any payment still waiting for its receiver. */
private fun LedgerView.footnote(
    projectId: String,
    active: Boolean,
    noTransfers: Boolean,
    myNet: Long,
    currency: String,
): String {
    val standing =
        when {
            active && noTransfers -> "Everyone’s settled in this project."
            active && myNet == 0L -> "You’re settled in this project."
            active && myNet < 0 -> "You owe ${Money.format(myNet, currency)} in this project."
            active -> "You’re owed ${Money.format(myNet, currency)} in this project."
            myNet == 0L -> "You’re settled. It becomes a permanent record once everyone has paid."
            else -> "It becomes a permanent record once everyone has paid."
        }
    val pending =
        ledger.payments.count { it.groupId == projectId && it.status == PaymentStatus.Pending }
    return when (pending) {
        0 -> standing
        1 -> "$standing 1 payment is waiting for confirmation."
        else -> "$standing $pending payments are waiting for confirmation."
    }
}
