import Contacts
import Foundation

/// People from the device's contacts, matched to Paybak users by phone or email (app-architecture §4).
/// The contacts are real (read only once access is granted, which Privacy › Contacts sync asks for);
/// the Paybak directory is simulated: nobody in a real address book is on Paybak yet. Debug builds with
/// the demo loaded use the demo's address book instead (Kabir and Meera on Paybak, Ananya to invite).
enum ContactsDirectory {
    nonisolated struct Contact: Hashable, Identifiable, Sendable {
        var id: String
        var name: String
        /// Phone or email.
        var contact: String
        /// A Paybak user already (Add), or someone to invite (Invite).
        var isOnPaybak: Bool
        /// Paybak users only (without "@").
        var username: String?
        /// A peep-head asset key for Paybak users who have one.
        var avatar: String?
        var upi: String?
    }

    /// Asks for access the first time; false when refused.
    static func requestAccess() async -> Bool {
        (try? await CNContactStore().requestAccess(for: .contacts)) ?? false
    }

    static var isAuthorized: Bool {
        switch CNContactStore.authorizationStatus(for: .contacts) {
        case .authorized, .limited: true
        default: false
        }
    }

    /// The address book, sorted by name (empty without access).
    static func contacts() async -> [Contact] {
        #if DEBUG
        if DebugState.demoAnchor != nil { return demoAddressBook }
        #endif
        guard isAuthorized else { return [] }
        return await Task.detached(priority: .userInitiated) { readContacts() }.value
    }

    nonisolated private static func readContacts() -> [Contact] {
        let keys = [CNContactGivenNameKey, CNContactFamilyNameKey, CNContactPhoneNumbersKey, CNContactEmailAddressesKey] as [CNKeyDescriptor]
        var result: [Contact] = []
        try? CNContactStore().enumerateContacts(with: CNContactFetchRequest(keysToFetch: keys)) { person, _ in
            let name = [person.givenName, person.familyName].filter { !$0.isEmpty }.joined(separator: " ")
            let reach = person.phoneNumbers.first?.value.stringValue ?? (person.emailAddresses.first?.value as String?)
            guard !name.isEmpty, let reach else { return }
            result.append(Contact(id: person.identifier, name: name, contact: reach, isOnPaybak: false))
        }
        return result.sorted { $0.name.localizedCaseInsensitiveCompare($1.name) == .orderedAscending }
    }

    #if DEBUG
    /// The demo's address book (screens-groups §7.4).
    static let demoAddressBook = [
        Contact(id: "c-kabir", name: "Kabir Singh", contact: "+91 98200 11223", isOnPaybak: true, username: "kabir", avatar: "avatar-6",
                upi: "kabir@okaxis"),
        Contact(id: "c-meera", name: "Meera Iyer", contact: "+91 98200 44556", isOnPaybak: true, username: "meera", avatar: "avatar-7",
                upi: "meera@okhdfcbank"),
        Contact(id: "c-ananya", name: "Ananya Rao", contact: "+91 98765 43210", isOnPaybak: false),
    ]
    #endif
}
