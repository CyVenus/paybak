import Foundation

/// What a scanned or opened invite link led to.
enum InviteLinkResult: Equatable {
    /// The friend it names (added first when they weren't one yet).
    case friend(PersonID)
    /// The user's own code.
    case ownCode
    /// Not a Paybak invite link.
    case notPaybak
}

/// Lane B's Groups & Friends actions (app-architecture §7.1).
extension LedgerStore {
    /// A Paybak invite link (a scanned QR code): the friend with that username, added first when they
    /// aren't a friend yet. The Paybak directory is simulated, so any well-formed username is a user.
    func openInviteLink(_ text: String) -> InviteLinkResult {
        guard let username = QRScanner.username(fromInviteLink: text) else { return .notPaybak }
        if username == profileStore.profile.inviteUsername { return .ownCode }
        if let friend = ledger.people.first(where: { $0.username == username }) { return .friend(friend.id) }
        let name = username.prefix(1).uppercased() + username.dropFirst()
        return .friend(addFriend(Person(id: RecordID.make(), name: name, username: username, addedAt: clock.now)))
    }

    /// Adds a contact who is on Paybak as a friend ("Add" on Add friend).
    @discardableResult
    func addFriend(_ contact: ContactsDirectory.Contact) -> PersonID {
        addFriend(Person(id: RecordID.make(), name: contact.name, avatar: contact.avatar, upi: contact.upi, username: contact.username,
                         contact: contact.contact, addedAt: clock.now))
    }
}
