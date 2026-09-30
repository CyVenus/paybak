import Foundation

/// The two lists of Add friend (screens-groups §7.4–7.5): contacts on Paybak ("Added" once they're
/// friends, else "Add") and people to invite (not on Paybak, including guest friends), filtered by
/// the search by name, phone, email or @username. While searching, friends who aren't in the address
/// book show too; a full phone, email or @username that matches nobody becomes one Invite row.
struct AddFriendLists: Equatable {
    struct OnPaybakRow: Identifiable, Equatable {
        var id: String
        var name: String
        var username: String
        var avatar: String?
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
        var seen = Set<PersonID>()
        for contact in contacts {
            let person = Self.person(matching: contact, in: people)
            if let person { seen.insert(person.id) }
            if let person, !person.isGuest {
                onPaybak.append(OnPaybakRow(id: person.id, name: contact.name, username: person.username ?? contact.username ?? "",
                                            avatar: person.avatar ?? contact.avatar, friendId: person.id))
            } else if contact.isOnPaybak, person == nil {
                onPaybak.append(OnPaybakRow(id: contact.id, name: contact.name, username: contact.username ?? "", avatar: contact.avatar,
                                            contact: contact))
            } else {
                invite.append(InviteRow(id: person?.id ?? contact.id, name: contact.name, reach: contact.contact, guestId: person?.id))
            }
        }
        let query = query.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !query.isEmpty else { return }
        for person in people where !seen.contains(person.id) {
            if person.isGuest {
                invite.append(InviteRow(id: person.id, name: person.name, reach: person.contact, guestId: person.id))
            } else {
                onPaybak.append(OnPaybakRow(id: person.id, name: person.name, username: person.username ?? "", avatar: person.avatar,
                                            friendId: person.id))
            }
        }
        onPaybak = onPaybak.filter { Self.matches(query, name: $0.name, reach: $0.contact?.contact, username: $0.username) }
        invite = invite.filter { Self.matches(query, name: $0.name, reach: $0.reach, username: nil) }
        if onPaybak.isEmpty, invite.isEmpty, Self.isFullAddress(query) {
            invite = [InviteRow(id: "query", name: query, reach: query.hasPrefix("@") ? nil : query)]
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

    /// Case- and diacritic-insensitive: the name contains the query, a phone contains its digits, an
    /// email contains it, or the username starts with it (with or without "@").
    static func matches(_ query: String, name: String, reach: String?, username: String?) -> Bool {
        let options: String.CompareOptions = [.caseInsensitive, .diacriticInsensitive]
        if name.range(of: query, options: options) != nil { return true }
        let handle = query.hasPrefix("@") ? String(query.dropFirst()) : query
        if let username, !handle.isEmpty, username.range(of: handle, options: options.union(.anchored)) != nil { return true }
        guard let reach else { return false }
        let digits = query.filter(\.isNumber)
        if digits.count >= 3, reach.filter(\.isNumber).contains(digits) { return true }
        return reach.contains("@") && reach.range(of: query, options: options) != nil
    }

    /// A whole phone number, email or @username (worth inviting when nobody matches).
    static func isFullAddress(_ query: String) -> Bool {
        if query.wholeMatch(of: #/@[A-Za-z0-9._]{2,}/#) != nil { return true }
        return SignInContact(query) != nil
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
