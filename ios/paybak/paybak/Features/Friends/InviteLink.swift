import Foundation

/// The user's Paybak invite (screens-groups §2.11): "@" + the username, and the link a QR code or a
/// share carries.
extension UserProfile {
    /// "arjun": the saved username, else the lowercase first name.
    var inviteUsername: String {
        username ?? Self.defaultUsername(for: name)
    }

    /// "https://paybak.app/i/arjun".
    var inviteLink: URL {
        URL(string: "https://paybak.app/i/\(inviteUsername)")!
    }

    /// "paybak.app/i/arjun": the link as the field shows it, without the scheme.
    var inviteLinkText: String {
        "paybak.app/i/\(inviteUsername)"
    }

    /// The text that goes with a shared invite link.
    var inviteMessage: String {
        "Join me on Paybak so we can split expenses: \(inviteLink.absoluteString)"
    }
}
