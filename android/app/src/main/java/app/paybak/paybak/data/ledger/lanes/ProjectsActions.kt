package app.paybak.paybak.data.ledger.lanes

import app.paybak.paybak.data.ledger.LedgerRepository
import app.paybak.paybak.domain.actions.updateGroup
import app.paybak.paybak.domain.model.Contribution
import app.paybak.paybak.domain.model.ProjectInfo
import app.paybak.paybak.domain.projects.ComponentForm
import app.paybak.paybak.domain.projects.addComponent
import app.paybak.paybak.domain.projects.archiveIfSettled
import app.paybak.paybak.domain.projects.deleteComponent
import app.paybak.paybak.domain.projects.editComponent

/** Lane B's store actions for Projects (app-architecture §3.4, §7.1; screens-projects §1). */

/** Adds a part from the Add component sheet and returns its id. */
fun LedgerRepository.addComponent(projectId: String, form: ComponentForm): String =
    mutateReturning { ledger ->
        val currency = ledger.group(projectId)?.currency ?: defaultCurrency
        ledger.addComponent(projectId, form, currency, context())
    }

/** Saves the Edit component sheet over part [id]. */
fun LedgerRepository.editComponent(id: String, form: ComponentForm) = mutate { ledger ->
    val currency =
        ledger.component(id)?.let { ledger.group(it.projectId) }?.currency ?: defaultCurrency
    ledger.editComponent(id, form, currency, context())
}

fun LedgerRepository.deleteComponent(id: String) = mutate { it.deleteComponent(id) }

/** A valid contribution rule from Project settings; shares follow it at once. */
fun LedgerRepository.setContribution(projectId: String, contribution: Contribution) =
    updateProject(projectId) { it.copy(contribution = contribution) }

/** The budget, or none ([budget] null): the dashboard drops the bar and the projection. */
fun LedgerRepository.setBudget(projectId: String, budget: Long?) =
    updateProject(projectId) { it.copy(budget = budget) }

/** "Collect money upfront". */
fun LedgerRepository.setPool(projectId: String, pool: Boolean) =
    updateProject(projectId) { it.copy(pool = pool) }

/** Archives a closed project once its final plan is paid (screens-projects §1.6). */
fun LedgerRepository.archiveIfSettled(projectId: String) = mutate {
    it.archiveIfSettled(projectId, context())
}

private fun LedgerRepository.updateProject(id: String, change: (ProjectInfo) -> ProjectInfo) =
    mutate { ledger ->
        ledger.updateGroup(id) { group -> group.copy(project = group.project?.let(change)) }
    }
