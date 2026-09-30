package app.paybak.paybak.service.contacts

import app.paybak.paybak.domain.model.Person
import java.text.Normalizer
import kotlinx.serialization.Serializable

/** A person from the phone's contacts: a name and a phone number or email. */
@Serializable data class DeviceContact(val name: String, val contact: String)

/** A Paybak account the simulated directory knows: who a username or a contact belongs to. */
@Serializable
data class DirectoryAccount(
    val id: String,
    val name: String,
    val username: String,
    val contact: String,
    val avatar: String? = null,
    val upi: String? = null,
)

/**
 * Who is on Paybak (app-architecture §4: simulated, there is no server). Your friends are, unless
 * they're guests; so is anyone in [accounts], the simulated directory of other people's accounts.
 * Phone numbers match on their last ten digits, emails without case.
 */
class Directory(private val people: List<Person>, private val accounts: List<DirectoryAccount>) {
    /** The friend or account with this username (without "@"). */
    fun byUsername(username: String): Person? {
        val name = username.trim().removePrefix("@").lowercase()
        if (name.isEmpty()) return null
        return people.firstOrNull { !it.isGuest && it.username.equals(name, ignoreCase = true) }
            ?: accounts.firstOrNull { it.username.equals(name, ignoreCase = true) }?.toPerson()
    }

    /** The friend (or guest) or account behind a phone number or email. */
    fun byContact(contact: String): Person? {
        val key = contactKey(contact) ?: return null
        return people.firstOrNull { it.contact?.let(::contactKey) == key }
            ?: accounts.firstOrNull { contactKey(it.contact) == key }?.toPerson()
    }

    fun isFriend(person: Person): Boolean = people.any { it.id == person.id }

    private fun DirectoryAccount.toPerson() =
        people.firstOrNull { it.id == id }
            ?: Person(
                id = id,
                name = name,
                avatar = avatar,
                upi = upi,
                username = username,
                contact = contact,
                addedAt = java.time.Instant.EPOCH,
            )
}

/** A row of Add friend's "On Paybak": [added] once they're your friend. */
data class OnPaybakRow(val person: Person, val added: Boolean) {
    /** "@kabir". */
    val handle: String?
        get() = person.username?.let { "@$it" }
}

/** A row of Add friend's "Invite": someone not on Paybak; Invite adds them as a guest. */
data class InviteRow(val name: String, val contact: String) {
    val initials: String
        get() = Person(id = "", name = name, addedAt = java.time.Instant.EPOCH).initials
}

/** Add friend's lists for a search [query] (screens-groups §7.4–7.5). */
data class AddFriendLists(val onPaybak: List<OnPaybakRow>, val invite: List<InviteRow>) {
    val isEmpty: Boolean
        get() = onPaybak.isEmpty() && invite.isEmpty()
}

/**
 * Your [contacts] split into those on Paybak and those to invite, filtered by [query] (name,
 * phone, email or @username; case and accents don't matter). A full @username finds that account
 * even outside your contacts; a full phone number or email that matches nobody becomes an Invite
 * row of its own.
 */
fun Directory.addFriendLists(contacts: List<DeviceContact>, query: String): AddFriendLists {
    val onPaybak = LinkedHashMap<String, OnPaybakRow>()
    val invite = LinkedHashMap<String, InviteRow>()
    for (contact in contacts) {
        val key = contactKey(contact.contact) ?: continue
        val person = byContact(contact.contact)
        if (person != null && !person.isGuest) {
            onPaybak.getOrPut(person.id) { OnPaybakRow(person, isFriend(person)) }
        } else {
            invite.getOrPut(key) { InviteRow(person?.name ?: contact.name, contact.contact) }
        }
    }
    val q = fold(query)
    if (q.isEmpty()) return AddFriendLists(onPaybak.values.toList(), invite.values.toList())
    val found = onPaybak.values.filter { it.person.matches(q) }.toMutableList()
    val invites =
        invite.values.filter { fold(it.name).contains(q) || contactMatches(it.contact, q) }
    if (query.trim().startsWith("@")) {
        byUsername(query)?.takeIf { !it.isGuest && found.none { row -> row.person.id == it.id } }
            ?.let { found += OnPaybakRow(it, isFriend(it)) }
    }
    if (found.isEmpty() && invites.isEmpty() && contactKey(query) != null) {
        val person = byContact(query)
        return if (person != null && !person.isGuest) {
            AddFriendLists(listOf(OnPaybakRow(person, isFriend(person))), emptyList())
        } else {
            val contact = query.trim()
            AddFriendLists(emptyList(), listOf(InviteRow(person?.name ?: contact, contact)))
        }
    }
    return AddFriendLists(found, invites)
}

private fun Person.matches(q: String): Boolean =
    fold(name).contains(q) ||
        username?.let { fold("@$it").contains(q) } == true ||
        contact?.let { contactMatches(it, q) } == true

private fun contactMatches(contact: String, q: String): Boolean {
    val digits = q.filter(Char::isDigit)
    return fold(contact).contains(q) ||
        (digits.length >= 3 && contact.filter(Char::isDigit).contains(digits))
}

/** Lower case without accents: "Ananya" matches "ānanya". */
private fun fold(text: String): String =
    Normalizer.normalize(text.trim(), Normalizer.Form.NFD)
        .replace(Regex("\\p{M}+"), "")
        .lowercase()

/**
 * The comparable form of a phone number (its last ten digits, at least seven) or an email (lower
 * case); null when the text is neither.
 */
fun contactKey(text: String): String? {
    val trimmed = text.trim()
    if (EMAIL.matches(trimmed)) return trimmed.lowercase()
    if (!PHONE.matches(trimmed)) return null
    val digits = trimmed.filter(Char::isDigit)
    return if (digits.length >= 7) digits.takeLast(10) else null
}

private val EMAIL = Regex("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")
private val PHONE = Regex("\\+?[0-9][0-9 ()-]*")
