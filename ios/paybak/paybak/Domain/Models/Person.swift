import Foundation

/// A friend or a guest (domain.md §1.2). The user is `Person.me`; their name and avatar come from the
/// profile. Every person in the ledger is a friend.
nonisolated struct Person: Codable, Hashable, Identifiable, Sendable {
    static let me: PersonID = "me"

    var id: PersonID
    /// The full name ("Rohan Verma"); the first word is the display name.
    var name: String
    /// A peep-head asset key (`avatar-2` … `avatar-7`); nil = initials.
    var avatar: String?
    var upi: String?
    /// Without "@"; nil for guests.
    var username: String?
    var pronoun: Pronoun = .they
    /// Not on Paybak yet (the gray Guest tag).
    var isGuest = false
    /// Phone or email.
    var contact: String?
    /// Mutes automatic reminders to this friend only.
    var remindersMuted = false
    var addedAt: Date

    // lane fields: add optional fields below with a default.

    var firstName: String {
        name.split(whereSeparator: \.isWhitespace).first.map(String.init) ?? name
    }

    /// First letter of the first word + first letter of the last word ("AR").
    var initials: String {
        let words = name.split(whereSeparator: \.isWhitespace)
        guard let first = words.first?.first else { return "" }
        guard words.count > 1, let last = words.last?.first else { return String(first).uppercased() }
        return "\(first)\(last)".uppercased()
    }
}

nonisolated enum Pronoun: String, Codable, Sendable {
    case she
    case he
    case they
}
