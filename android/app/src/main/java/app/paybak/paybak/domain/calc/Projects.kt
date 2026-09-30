package app.paybak.paybak.domain.calc

import app.paybak.paybak.domain.format.Money
import app.paybak.paybak.domain.model.Component
import app.paybak.paybak.domain.model.ComponentStatus
import app.paybak.paybak.domain.model.Group
import app.paybak.paybak.domain.model.ProjectStatus
import java.math.BigDecimal
import java.math.RoundingMode

/** One member's row of "Paid vs fair share" (§8.2); fills are 0–1 of the shared scale. */
data class FairShareRow(
    val personId: String,
    val paid: Long,
    val share: Long,
    val net: Long,
    val paidFill: Float,
    val shareMark: Float,
)

/** The project dashboard's numbers (§8). Budget lines are null without a budget. */
data class ProjectReport(
    val project: Group,
    val spent: Long,
    val budget: Long?,
    val percentUsed: Int?,
    val projection: Long?,
    val budgetLine: String?,
    val plannedLine: String?,
    val overBudget: Boolean,
    val parts: List<Component>,
    val fairShare: List<FairShareRow>,
    val plan: List<Transfer>,
)

fun LedgerView.projectReport(projectId: String): ProjectReport? {
    val project = group(projectId)?.takeIf { it.isProject } ?: return null
    val info = project.project!!
    val spent = projectSpent(projectId)
    val parts = projectParts(projectId)
    val planned =
        parts.filter { it.status == ComponentStatus.Planned }.sumOf { it.estimatedCost ?: 0 }
    val active = info.status == ProjectStatus.Active
    val budget = info.budget
    val currency = project.currency
    val budgetLine = budget?.let {
        when {
            spent > it -> "${Money.format(spent - it, currency)} over budget"
            active -> "${Money.format(it - spent, currency)} left"
            else -> "${Money.format(it - spent, currency)} under budget"
        }
    }
    val plannedLine =
        if (!active || budget == null) null
        else if (planned > 0) "Planned items bring it to ${Money.format(spent + planned, currency)}"
        else "All planned items are bought."
    val (paid, share) = projectPaidShare(projectId)
    val nets = groupNets(projectId)
    val scale = maxOf(paid.values.maxOrNull() ?: 0, share.values.maxOrNull() ?: 0).coerceAtLeast(1)
    val members = project.memberIds
    val fairShare =
        members
            .map { m ->
                FairShareRow(
                    m,
                    paid[m] ?: 0,
                    share[m] ?: 0,
                    nets[m] ?: 0,
                    (paid[m] ?: 0).toFloat() / scale,
                    (share[m] ?: 0).toFloat() / scale,
                )
            }
            .sortedWith(compareBy({ -it.net }, { members.indexOf(it.personId) }))
    return ProjectReport(
        project = project,
        spent = spent,
        budget = budget,
        percentUsed =
            budget
                ?.takeIf { it > 0 }
                ?.let {
                    BigDecimal(spent * 100).divide(BigDecimal(it), 0, RoundingMode.HALF_UP).toInt()
                },
        projection = if (active) spent + planned else null,
        budgetLine = budgetLine,
        plannedLine = plannedLine,
        overBudget = budget != null && spent > budget,
        parts =
            parts.sortedWith(
                compareBy({ it.status.ordinal }, { -it.statusChangedAt.toEpochMilli() })
            ),
        fairShare = fairShare,
        plan = groupPlan(projectId).sortedByDescending { it.amount },
    )
}
