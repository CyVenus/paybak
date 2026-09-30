package app.paybak.paybak.domain.actions

import app.paybak.paybak.domain.model.Component
import app.paybak.paybak.domain.model.ComponentDraft
import app.paybak.paybak.domain.model.ComponentEvent
import app.paybak.paybak.domain.model.ComponentStatus
import app.paybak.paybak.domain.model.Ledger
import app.paybak.paybak.domain.model.ME
import app.paybak.paybak.domain.model.ProjectStatus
import app.paybak.paybak.domain.model.newId
import app.paybak.paybak.domain.model.replacing

/** Project parts and closing (domain.md §8.3). */

/** A new part starts Planned, or Bought when it already has an actual cost. */
fun Ledger.addComponent(draft: ComponentDraft, ctx: ActionContext): Pair<Ledger, String> {
    ensure(draft.name.isNotBlank()) { "Name the part." }
    val bought = (draft.actualCost ?: 0) > 0
    val id = newId()
    val component =
        Component(
            id = id,
            projectId = draft.projectId,
            name = draft.name.trim(),
            status = if (bought) ComponentStatus.Bought else ComponentStatus.Planned,
            estimatedCost = draft.estimatedCost,
            actualCost = draft.actualCost,
            paidBy = draft.paidBy,
            receipt = draft.receipt,
            createdAt = ctx.at,
            statusChangedAt = ctx.at,
            history =
                listOfNotNull(
                    ComponentEvent("added", ctx.at, ME),
                    ComponentEvent("bought", ctx.at, ME).takeIf { bought },
                ),
        )
    return copy(components = components + component) to id
}

/**
 * Edits a part. Bought and Done need an actual cost; a status change is logged and moves the part
 * to the top of its section.
 */
fun Ledger.updateComponent(
    id: String,
    ctx: ActionContext,
    status: ComponentStatus? = null,
    actualCost: Long? = null,
    paidBy: String? = null,
    name: String? = null,
    estimatedCost: Long? = null,
    by: String = ME,
): Ledger {
    val old = requireNotNull(component(id)) { "No component $id" }
    val newStatus = status ?: old.status
    val cost = actualCost ?: old.actualCost
    ensure(newStatus == ComponentStatus.Planned || (cost ?: 0) > 0) { "Add what it cost." }
    val changed = newStatus != old.status
    val updated =
        old.copy(
            name = name?.trim()?.ifEmpty { null } ?: old.name,
            status = newStatus,
            estimatedCost = estimatedCost ?: old.estimatedCost,
            actualCost = cost,
            paidBy = paidBy ?: old.paidBy,
            statusChangedAt = if (changed) ctx.at else old.statusChangedAt,
            history =
                if (changed) old.history + ComponentEvent(newStatus.name.lowercase(), ctx.at, by)
                else old.history,
        )
    return copy(components = components.replacing({ it.id == id }) { updated })
}

/** Closes a project; it archives at once when everyone is already square. */
fun Ledger.closeProject(id: String, ctx: ActionContext): Ledger {
    val closed =
        updateGroup(id) {
            it.copy(project = it.project!!.copy(status = ProjectStatus.Closed, closedAt = ctx.at))
        }
    val settled = ctx.view(closed).groupNets(id).values.all { it == 0L }
    return if (!settled) closed
    else
        closed.updateGroup(id) {
            it.copy(
                project = it.project!!.copy(status = ProjectStatus.Archived, archivedAt = ctx.at)
            )
        }
}
