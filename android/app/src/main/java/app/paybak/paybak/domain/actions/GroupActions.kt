package app.paybak.paybak.domain.actions

import app.paybak.paybak.domain.format.Money
import app.paybak.paybak.domain.model.Group
import app.paybak.paybak.domain.model.GroupDraft
import app.paybak.paybak.domain.model.GroupKind
import app.paybak.paybak.domain.model.Ledger
import app.paybak.paybak.domain.model.ME
import app.paybak.paybak.domain.model.Person
import app.paybak.paybak.domain.model.ProjectInfo
import app.paybak.paybak.domain.model.newId
import app.paybak.paybak.domain.model.replacing

/** Groups, projects and friends (domain.md §11). */

/** Creates a group or project; you're always its first member. */
fun Ledger.addGroup(draft: GroupDraft, ctx: ActionContext): Pair<Ledger, String> {
    ensure(draft.name.isNotBlank()) { "Give it a name." }
    val id = draft.id ?: newId()
    val project = draft.kind == GroupKind.Project
    val group =
        Group(
            id = id,
            kind = draft.kind,
            type = draft.type.takeIf { !project },
            icon = draft.icon ?: draft.type?.icon ?: if (project) "package" else "people",
            name = draft.name.trim(),
            currency = draft.currency ?: ctx.defaultCurrency,
            memberIds = listOf(ME) + draft.memberIds.filter { it != ME }.distinct(),
            simplifyDebts = project || draft.simplifyDebts,
            settleBy = draft.settleBy.takeIf { !project },
            createdAt = ctx.at,
            createdBy = ME,
            project = if (project) draft.project ?: ProjectInfo() else null,
        )
    return copy(groups = groups + group) to id
}

/** Any settings change of a group (name, currency, simplify, settle by, project fields). */
fun Ledger.updateGroup(id: String, change: (Group) -> Group): Ledger {
    requireNotNull(group(id)) { "No group $id" }
    return copy(groups = groups.replacing({ it.id == id }, change))
}

fun Ledger.addMembers(groupId: String, personIds: List<String>): Ledger =
    updateGroup(groupId) { group ->
        group.copy(memberIds = (group.memberIds + personIds).distinct())
    }

/** Removes a member whose net in the group is exactly 0. */
fun Ledger.removeMember(groupId: String, personId: String, ctx: ActionContext): Ledger {
    val net = ctx.view(this).groupNets(groupId)[personId] ?: 0
    val group = group(groupId)!!
    ensure(net == 0L) {
        "${person(personId)?.firstName ?: "They"} still has a balance of " +
            "${Money.format(kotlin.math.abs(net), group.currency)} in ${group.name}."
    }
    return updateGroup(groupId) { it.copy(memberIds = it.memberIds - personId) }
}

/**
 * Leaves a group; only allowed at a net of exactly 0 (§6.5), otherwise "You owe ₹1,400 in Goa Trip.
 * Settle up with Kabir first, then you can leave."
 */
fun Ledger.leaveGroup(groupId: String, ctx: ActionContext): Ledger {
    val view = ctx.view(this)
    val group = requireNotNull(group(groupId)) { "No group $groupId" }
    val net = view.groupNets(groupId)[ME] ?: 0
    ensure(net == 0L) {
        val amount = Money.format(kotlin.math.abs(net), group.currency)
        val plan = view.groupPlan(groupId)
        if (net < 0) {
            val payees = plan.filter { it.debtorId == ME }.map { view.first(it.creditorId) }
            "You owe $amount in ${group.name}. Settle up with ${payees.joinToString(" and ")} first, then you can leave."
        } else {
            "You’re owed $amount in ${group.name}. Settle up first, then you can leave."
        }
    }
    return updateGroup(groupId) { it.copy(memberIds = it.memberIds - ME) }
}

/** Adds a friend (Add friend "Add", a QR scan). A person already there is kept. */
fun Ledger.addFriend(person: Person): Ledger =
    if (person(person.id) != null) this else copy(people = people + person)

/** Invites someone not on Paybak: a guest friend identified by [contact]. */
fun Ledger.addGuest(name: String, contact: String?, ctx: ActionContext): Pair<Ledger, String> {
    ensure(name.isNotBlank()) { "Add a name." }
    people
        .firstOrNull { contact != null && it.contact == contact }
        ?.let {
            return this to it.id
        }
    val id = newId()
    val guest =
        Person(id = id, name = name.trim(), isGuest = true, contact = contact, addedAt = ctx.at)
    return copy(people = people + guest) to id
}

/** Mutes automatic reminders to one friend (manual Remind still works). */
fun Ledger.setRemindersMuted(personId: String, muted: Boolean): Ledger =
    copy(people = people.replacing({ it.id == personId }) { it.copy(remindersMuted = muted) })
