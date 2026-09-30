package app.paybak.paybak.data.ledger.actions

import app.paybak.paybak.data.ledger.LedgerRepository
import app.paybak.paybak.domain.actions.addComponent
import app.paybak.paybak.domain.actions.addFriend
import app.paybak.paybak.domain.actions.addGroup
import app.paybak.paybak.domain.actions.addGuest
import app.paybak.paybak.domain.actions.addMembers
import app.paybak.paybak.domain.actions.closeProject
import app.paybak.paybak.domain.actions.leaveGroup
import app.paybak.paybak.domain.actions.removeMember
import app.paybak.paybak.domain.actions.setRemindersMuted
import app.paybak.paybak.domain.actions.updateComponent
import app.paybak.paybak.domain.actions.updateGroup
import app.paybak.paybak.domain.model.ComponentDraft
import app.paybak.paybak.domain.model.ComponentStatus
import app.paybak.paybak.domain.model.Group
import app.paybak.paybak.domain.model.GroupDraft
import app.paybak.paybak.domain.model.Person

/** Group, project and friend actions of the store (domain.md §11). */

/** Creates a group or project and returns its id. */
fun LedgerRepository.addGroup(draft: GroupDraft): String = mutateReturning {
    it.addGroup(draft, context())
}

fun LedgerRepository.updateGroup(id: String, change: (Group) -> Group) = mutate {
    it.updateGroup(id, change)
}

fun LedgerRepository.addMembers(groupId: String, personIds: List<String>) = mutate {
    it.addMembers(groupId, personIds)
}

/** Throws `LedgerRuleException` while the member's net isn't 0. */
fun LedgerRepository.removeMember(groupId: String, personId: String) = mutate {
    it.removeMember(groupId, personId, context())
}

/** Throws `LedgerRuleException` ("You owe ₹1,400 in Goa Trip. …") while your net isn't 0. */
fun LedgerRepository.leaveGroup(groupId: String) = mutate { it.leaveGroup(groupId, context()) }

fun LedgerRepository.addFriend(person: Person) = mutate { it.addFriend(person) }

/** Adds a guest (not on Paybak) and returns their id; an existing contact is reused. */
fun LedgerRepository.addGuest(name: String, contact: String?): String = mutateReturning {
    it.addGuest(name, contact, context())
}

fun LedgerRepository.setRemindersMuted(personId: String, muted: Boolean) = mutate {
    it.setRemindersMuted(personId, muted)
}

/** Adds a project part and returns its id. */
fun LedgerRepository.addComponent(draft: ComponentDraft): String = mutateReturning {
    it.addComponent(draft, context())
}

fun LedgerRepository.updateComponent(
    id: String,
    status: ComponentStatus? = null,
    actualCost: Long? = null,
    paidBy: String? = null,
    name: String? = null,
    estimatedCost: Long? = null,
) = mutate { it.updateComponent(id, context(), status, actualCost, paidBy, name, estimatedCost) }

fun LedgerRepository.closeProject(id: String) = mutate { it.closeProject(id, context()) }
