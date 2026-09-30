import Foundation

// STUB (app-architecture §4): Lane B (Add friend) and lane C (the Privacy toggle) fill this with the
// Contacts framework and the simulated directory, keeping these names.
/// People from the device's contacts, matched to Paybak users by phone or email (simulated).
enum ContactsDirectory {
    struct Contact: Hashable, Identifiable {
        var id: String
        var name: String
        var contact: String
        /// A Paybak user already (Add), or someone to invite (Invite).
        var isOnPaybak: Bool
    }

    /// Asks for access the first time; false when refused.
    static func requestAccess() async -> Bool { false }

    static func contacts() async -> [Contact] { [] }
}
