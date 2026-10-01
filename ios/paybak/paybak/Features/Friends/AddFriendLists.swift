import Foundation

/// The two lists of Add friend (screens-groups §7.4–7.5): contacts on Paybak ("Added" once they're
/// friends, else "Add") and people to invite (not on Paybak, including guest friends), filtered by
/// the search by name, phone, email or @username (case and accents don't matter). A full @username
/// finds that friend even outside your contacts; a full phone number or email that matches nobody
/// becomes one Invite row of its own.
struct AddFriendLists: Equatable {
    struct OnPaybakRow: Identifiable, Equatable {
        var id: String
        var name: String
        var username: String
        var avatar: String?
        /// Their phone or email, which the search also matches.
        var reach: String?
        /// Already a friend ("Added").
        var friendId: PersonID?
        /// The contact to add as a friend ("Add").
        var contact: ContactsDirectory.Contact?
    }

    struct InviteRow: Identifiable, Equatable {
        var id: String
        var name: String
        /// The phone or email the guest is added with.
        var reach: String?
        /// Already a guest friend: Invite opens their page.
        var guestId: PersonID?
    }

    var onPaybak: [OnPaybakRow] = []
    var invite: [InviteRow] = []

    init(contacts: [ContactsDirectory.Contact], people: [Person], query: String) {
        var seenOnPaybak = Set<String>()
        var seenInvites = Set<String>()
        for contact in contacts {
            guard let key = Self.contactKey(contact.contact) else { continue }
            let person = Self.person(matching: contact, in: people)
            if let person, !person.isGuest {
                guard seenOnPaybak.insert(person.id).inserted else { continue }
                onPaybak.append(OnPaybakRow(id: person.id, name: person.name, username: person.username ?? contact.username ?? "",
                                            avatar: person.avatar ?? contact.avatar, reach: person.contact ?? contact.contact,
                                            friendId: person.id))
            } else if contact.isOnPaybak, person == nil {
                guard seenOnPaybak.insert(contact.id).inserted else { continue }
                onPaybak.append(OnPaybakRow(id: contact.id, name: contact.name, username: contact.username ?? "", avatar: contact.avatar,
                                            reach: contact.contact, contact: contact))
            } else {
                guard seenInvites.insert(key).inserted else { continue }
                invite.append(InviteRow(id: person?.id ?? contact.id, name: person?.name ?? contact.name, reach: contact.contact,
                                        guestId: person?.id))
            }
        }
        let query = query.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !query.isEmpty else { return }
        onPaybak = onPaybak.filter { Self.matches(query, name: $0.name, reach: $0.reach, username: $0.username) }
        invite = invite.filter { Self.matches(query, name: $0.name, reach: $0.reach, username: nil) }
        // A whole @username finds that friend, in your contacts or not.
        if query.hasPrefix("@") {
            let handle = query.dropFirst().lowercased()
            if !handle.isEmpty,
               let friend = people.first(where: { !$0.isGuest && $0.username?.lowercased() == handle }),
               !onPaybak.contains(where: { $0.friendId == friend.id }) {
                onPaybak.append(OnPaybakRow(id: friend.id, name: friend.name, username: friend.username ?? "", avatar: friend.avatar,
                                            reach: friend.contact, friendId: friend.id))
            }
        }
        // A whole phone number or email: whoever it belongs to, or a new guest to invite.
        guard onPaybak.isEmpty, invite.isEmpty, Self.contactKey(query) != nil else { return }
        if let person = people.first(where: { $0.contact.map { Self.sameReach($0, query) } ?? false }) {
            if person.isGuest {
                invite = [InviteRow(id: person.id, name: person.name, reach: query, guestId: person.id)]
            } else {
                onPaybak = [OnPaybakRow(id: person.id, name: person.name, username: person.username ?? "", avatar: person.avatar,
                                        reach: person.contact, friendId: person.id)]
            }
        } else if let account = contacts.first(where: { $0.isOnPaybak && Self.sameReach($0.contact, query) }) {
            onPaybak = [OnPaybakRow(id: account.id, name: account.name, username: account.username ?? "", avatar: account.avatar,
                                    reach: account.contact, contact: account)]
        } else {
            invite = [InviteRow(id: "query", name: query, reach: query)]
        }
    }

    var isEmpty: Bool { onPaybak.isEmpty && invite.isEmpty }

    /// The person a contact already is: the same username, the same phone or email, or (a guest added
    /// by name) the same name.
    static func person(matching contact: ContactsDirectory.Contact, in people: [Person]) -> Person? {
        people.first { person in
            if let username = contact.username, person.username == username { return true }
            if let reach = person.contact, sameReach(reach, contact.contact) { return true }
            return person.isGuest && person.name.caseInsensitiveCompare(contact.name) == .orderedSame
        }
    }

    /// Case- and accent-insensitive: the name contains the query, "@username" contains it, the phone
    /// or email contains it, or a phone contains its digits (three or more).
    static func matches(_ query: String, name: String, reach: String?, username: String?) -> Bool {
        let folded = fold(query)
        if fold(name).contains(folded) { return true }
        if let username, !username.isEmpty, fold("@" + username).contains(folded) { return true }
        guard let reach else { return false }
        if fold(reach).contains(folded) { return true }
        let digits = query.filter(\.isNumber)
        return digits.count >= 3 && reach.filter(\.isNumber).contains(digits)
    }

    /// The comparable form of a phone number (its last ten digits, at least seven) or an email (lower
    /// case); nil when the text is neither.
    static func contactKey(_ text: String) -> String? {
        let trimmed = text.trimmingCharacters(in: .whitespacesAndNewlines)
        if trimmed.wholeMatch(of: #/[^@\s]+@[^@\s]+\.[^@\s]+/#) != nil { return trimmed.lowercased() }
        guard trimmed.wholeMatch(of: #/\+?[0-9][0-9\s()\-]*/#) != nil else { return nil }
        let digits = trimmed.filter(\.isNumber)
        return digits.count >= 7 ? String(digits.suffix(10)) : nil
    }

    /// Lower case without accents: "ānanya" matches "Ananya".
    private static func fold(_ text: String) -> String {
        text.trimmingCharacters(in: .whitespacesAndNewlines)
            .folding(options: [.caseInsensitive, .diacriticInsensitive], locale: nil)
            .lowercased()
    }

    /// Phone numbers compare by their last ten digits, emails case-insensitively.
    private static func sameReach(_ a: String, _ b: String) -> Bool {
        if a.contains("@") || b.contains("@") { return a.caseInsensitiveCompare(b) == .orderedSame }
        let (left, right) = (a.filter(\.isNumber).suffix(10), b.filter(\.isNumber).suffix(10))
        return !left.isEmpty && left == right
    }
}

extension Person {
    /// "AR" for someone who isn't in the ledger yet (a contact), by the same rule as `initials`.
    static func initials(of name: String) -> String {
        Person(id: "", name: name, addedAt: .distantPast).initials
    }
}
