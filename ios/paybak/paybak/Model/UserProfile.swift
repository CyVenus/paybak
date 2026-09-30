import Foundation

/// Everything onboarding saves locally (flow.md "Persistence"). There is no backend.
struct UserProfile: Codable, Equatable {
    enum Avatar: Codable, Equatable {
        /// Index into the five Setup 1 presets (`PBPeepHead.presets`).
        case preset(Int)
        /// The photo saved by `ProfileStore.savePhoto(_:)`.
        case photo
    }

    enum SignInMethod: String, Codable {
        case apple
        case google
        case email
        case phone
    }

    enum NotificationsChoice: String, Codable {
        /// The user tapped "Turn on notifications" and the OS permission was granted.
        case allowed
        /// The user tapped "Turn on notifications" and the OS permission was denied.
        case denied
        /// "Not now" or Skip: the permission was never requested.
        case notNow
    }

    var name = ""
    /// nil: no choice yet; initials are the fallback everywhere an avatar shows.
    var avatar: Avatar?
    /// ISO 4217 code, e.g. "INR".
    var currencyCode: String?
    var upiID = ""
    var notifications: NotificationsChoice?
    var signInMethod: SignInMethod?
    /// The email or phone number for the email/phone sign-in.
    var contact: String?
    var onboardingComplete = false

    /// The first whitespace-separated word of the trimmed name ("Arjun Mehta" → "Arjun").
    var firstName: String {
        name.split(whereSeparator: \.isWhitespace).first.map(String.init) ?? ""
    }

    /// First letter of the first word + first letter of the last word, uppercased ("AM");
    /// one word gives one letter.
    var initials: String {
        let words = name.split(whereSeparator: \.isWhitespace)
        guard let first = words.first?.first else { return "" }
        guard words.count > 1, let last = words.last?.first else { return String(first).uppercased() }
        return "\(first)\(last)".uppercased()
    }
}

extension UserProfile {
    /// The debug seed that makes mid-flow screens render like Figma (flow.md "Debug-only hooks").
    static let sample = UserProfile(
        name: "Arjun Mehta",
        avatar: .preset(0),
        currencyCode: "INR",
        upiID: "arjun@okaxis",
        signInMethod: .email,
        contact: "arjun@example.com"
    )
}
