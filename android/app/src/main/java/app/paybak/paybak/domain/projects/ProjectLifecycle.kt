package app.paybak.paybak.domain.projects

import app.paybak.paybak.domain.actions.ActionContext
import app.paybak.paybak.domain.actions.updateGroup
import app.paybak.paybak.domain.model.Ledger
import app.paybak.paybak.domain.model.ProjectStatus

/**
 * A closed project whose final plan is all paid and confirmed becomes a permanent record at once
 * (screens-projects §1.6), as the scheduler would on its next run.
 */
fun Ledger.archiveIfSettled(projectId: String, ctx: ActionContext): Ledger {
    val info = group(projectId)?.project ?: return this
    if (info.status != ProjectStatus.Closed) return this
    if (ctx.view(this).groupNets(projectId).values.any { it != 0L }) return this
    return updateGroup(projectId) {
        it.copy(project = info.copy(status = ProjectStatus.Archived, archivedAt = ctx.at))
    }
}
