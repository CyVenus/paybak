package app.paybak.paybak.domain.projects

import app.paybak.paybak.domain.actions.ActionContext
import app.paybak.paybak.domain.actions.LedgerRuleException
import app.paybak.paybak.domain.actions.addComponent
import app.paybak.paybak.domain.addrecord.AmountEntry
import app.paybak.paybak.domain.model.Component
import app.paybak.paybak.domain.model.ComponentDraft
import app.paybak.paybak.domain.model.ComponentEvent
import app.paybak.paybak.domain.model.ComponentStatus
import app.paybak.paybak.domain.model.Ledger
import app.paybak.paybak.domain.model.ME
import app.paybak.paybak.domain.model.Receipt
import app.paybak.paybak.domain.model.replacing
import kotlinx.serialization.Serializable

/**
 * The Add / Edit component sheet as typed (screens-projects §5, §1.5): costs are the digits on the
 * decimal pad ("6000"), [receipt] a saved photo's file name. New parts start Planned with you as
 * the payer.
 */
@Serializable
data class ComponentForm(
    val name: String = "",
    val estimate: String = "",
    val actual: String = "",
    val status: ComponentStatus = ComponentStatus.Planned,
    val paidBy: String = ME,
    val receipt: String? = null,
) {
    /** A name, and an actual cost once the part is bought or done. */
    fun canSave(currency: String): Boolean =
        name.isNotBlank() &&
            (status == ComponentStatus.Planned || AmountEntry.minor(actual, currency) > 0)

    /**
     * Typing an actual cost buys a planned part; clearing it plans a bought one again (proposal
     * §5.3). Done keeps its status either way.
     */
    fun withActual(text: String): ComponentForm {
        val next = copy(actual = text)
        return when {
            text.isNotEmpty() && status == ComponentStatus.Planned ->
                next.copy(status = ComponentStatus.Bought)
            text.isEmpty() && status == ComponentStatus.Bought ->
                next.copy(status = ComponentStatus.Planned)
            else -> next
        }
    }

    companion object {
        /** The sheet prefilled to edit [part]. */
        fun of(part: Component, currency: String) =
            ComponentForm(
                name = part.name,
                estimate = AmountEntry.text(part.estimatedCost ?: 0, currency),
                actual = AmountEntry.text(part.actualCost ?: 0, currency),
                status = part.status,
                paidBy = part.paidBy,
                receipt = part.receipt?.photo,
            )
    }
}

/**
 * Adds a part from the sheet and returns its id. A planned part keeps no actual cost; Done is
 * logged after Bought, as if it was fitted right away.
 */
fun Ledger.addComponent(
    projectId: String,
    form: ComponentForm,
    currency: String,
    ctx: ActionContext,
): Pair<Ledger, String> {
    checkSavable(form, currency)
    val planned = form.status == ComponentStatus.Planned
    val draft =
        ComponentDraft(
            projectId = projectId,
            name = form.name,
            estimatedCost = minorOrNull(form.estimate, currency),
            actualCost = if (planned) null else minorOrNull(form.actual, currency),
            paidBy = form.paidBy,
            receipt = form.receipt?.let { Receipt(photo = it, addedAt = ctx.at) },
        )
    val (added, id) = addComponent(draft, ctx)
    if (form.status != ComponentStatus.Done) return added to id
    return added.copy(
        components =
            added.components.replacing({ it.id == id }) {
                it.copy(
                    status = ComponentStatus.Done,
                    history = it.history + ComponentEvent("done", ctx.at, ME),
                )
            }
    ) to id
}

/**
 * Saves the sheet over part [id]: every field as typed (an emptied estimate goes), a status change
 * logged and moved to the top of its section.
 */
fun Ledger.editComponent(
    id: String,
    form: ComponentForm,
    currency: String,
    ctx: ActionContext,
): Ledger {
    checkSavable(form, currency)
    val old = requireNotNull(component(id)) { "No component $id" }
    val changed = form.status != old.status
    val receipt =
        when (form.receipt) {
            old.receipt?.photo -> old.receipt
            null -> null
            else -> Receipt(photo = form.receipt, addedAt = ctx.at)
        }
    val updated =
        old.copy(
            name = form.name.trim(),
            status = form.status,
            estimatedCost = minorOrNull(form.estimate, currency),
            actualCost =
                if (form.status == ComponentStatus.Planned) null
                else minorOrNull(form.actual, currency),
            paidBy = form.paidBy,
            receipt = receipt,
            statusChangedAt = if (changed) ctx.at else old.statusChangedAt,
            history =
                if (changed) old.history + ComponentEvent(form.status.name.lowercase(), ctx.at, ME)
                else old.history,
        )
    return copy(components = components.replacing({ it.id == id }) { updated })
}

/** Removes a part; its cost comes off the project. */
fun Ledger.deleteComponent(id: String): Ledger =
    copy(components = components.filterNot { it.id == id })

private fun checkSavable(form: ComponentForm, currency: String) {
    if (!form.canSave(currency)) {
        throw LedgerRuleException(
            if (form.name.isBlank()) "Name the part." else "Add what it cost."
        )
    }
}

private fun minorOrNull(text: String, currency: String): Long? =
    AmountEntry.minor(text, currency).takeIf { it > 0 }
