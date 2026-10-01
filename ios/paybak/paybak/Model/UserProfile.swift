import Foundation

/// Everything onboarding saves locally (flow.md "Persistence"). There is no backend.
struct UserProfile: Codable, Equatable {
    enum Avatar: Codable, Equatable {
        /// Index into the five Setup 1 presets (`PBPeepHead.presets`).
        case preset(Int)
        /// The photo saved by `ProfileStore.savePhoto(_:)`.
        case photo
        /// The custom character saved from Edit avatar.
        case character(AvatarLook)
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
    /// ISO 4217 code, e.g. "INR": the default currency for totals, new groups and new expenses.
    var currencyCode: String?
    /// Mirrors the primary UPI method (Setup 3 writes it; `paymentMethods` is the source from M2 on).
    var upiID = ""
    var notifications: NotificationsChoice?
    var signInMethod: SignInMethod?
    /// The email or phone number for the email/phone sign-in.
    var contact: String?
    var onboardingComplete = false
    /// Without "@" ("arjun"); the invite link is `https://paybak.app/i/{username}`.
    var username: String?
    /// For copy about the user on friends' devices (simulated).
    var pronoun: Pronoun?
    var paymentMethods: [PaymentMethod] = []
    /// Friends see the primary method only while this is on.
    var showPaymentToFriends = true

    /// The default currency, INR until onboarding picks one.
    var defaultCurrency: String { currencyCode ?? Currency.fallbackCode }

    /// The method friends see; nil when none is primary (Setup 3 cleared a primary UPI ID that sat
    /// beside other methods), as on Android.
    var primaryPaymentMethod: PaymentMethod? { paymentMethods.first(where: \.primary) }

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
    private enum CodingKeys: String, CodingKey {
        case name, avatar, currencyCode, upiID, notifications, signInMethod, contact, onboardingComplete, username,
             pronoun, paymentMethods, showPaymentToFriends
    }

    /// Tolerates profiles saved before a field existed (every key is optional), and migrates an M1
    /// profile: a UPI ID with no payment methods becomes the primary method.
    init(from decoder: Decoder) throws {
        let c = try decoder.container(keyedBy: CodingKeys.self)
        name = try c.decodeIfPresent(String.self, forKey: .name) ?? ""
        avatar = try c.decodeIfPresent(Avatar.self, forKey: .avatar)
        currencyCode = try c.decodeIfPresent(String.self, forKey: .currencyCode)
        upiID = try c.decodeIfPresent(String.self, forKey: .upiID) ?? ""
        notifications = try c.decodeIfPresent(NotificationsChoice.self, forKey: .notifications)
        signInMethod = try c.decodeIfPresent(SignInMethod.self, forKey: .signInMethod)
        contact = try c.decodeIfPresent(String.self, forKey: .contact)
        onboardingComplete = try c.decodeIfPresent(Bool.self, forKey: .onboardingComplete) ?? false
        username = try c.decodeIfPresent(String.self, forKey: .username)
        pronoun = try c.decodeIfPresent(Pronoun.self, forKey: .pronoun)
        paymentMethods = try c.decodeIfPresent([PaymentMethod].self, forKey: .paymentMethods) ?? []
        showPaymentToFriends = try c.decodeIfPresent(Bool.self, forKey: .showPaymentToFriends) ?? true
        migrateUPI()
    }

    /// A UPI ID with no payment methods becomes the primary UPI method.
    mutating func migrateUPI() {
        let upi = upiID.trimmingCharacters(in: .whitespaces)
        if paymentMethods.isEmpty, !upi.isEmpty {
            paymentMethods = [PaymentMethod(id: "pm-upi", kind: .upi, value: upi, primary: true)]
        }
    }

    /// Setup 3 edits `upiID`: the primary UPI method follows it (removed when it's cleared). With no
    /// primary UPI method, a new UPI ID goes first as the primary method and the others step down, as
    /// on Android.
    mutating func syncPrimaryUPI() {
        let upi = upiID.trimmingCharacters(in: .whitespaces)
        if let index = paymentMethods.firstIndex(where: { $0.primary && $0.kind == .upi }) {
            if upi.isEmpty {
                paymentMethods.remove(at: index)
            } else {
                paymentMethods[index].value = upi
            }
        } else if !upi.isEmpty {
            for index in paymentMethods.indices {
                paymentMethods[index].primary = false
            }
            let id = paymentMethods.contains { $0.id == "pm-upi" } ? RecordID.make() : "pm-upi"
            paymentMethods.insert(PaymentMethod(id: id, kind: .upi, value: upi, primary: true), at: 0)
        }
    }

    /// The default username: the lowercase first name ("arjun").
    static func defaultUsername(for name: String) -> String {
        let first = name.split(whereSeparator: \.isWhitespace).first.map(String.init) ?? ""
        return first.lowercased().filter { $0.isLetter || $0.isNumber }
    }

    /// The debug seed that makes mid-flow screens render like Figma (flow.md "Debug-only hooks").
    static let sample = UserProfile(
        name: "Arjun Mehta",
        avatar: .preset(0),
        currencyCode: "INR",
        upiID: "arjun@okaxis",
        signInMethod: .email,
        contact: "arjun@example.com",
        username: "arjun",
        pronoun: .he,
        paymentMethods: [PaymentMethod(id: "pm-upi", kind: .upi, value: "arjun@okaxis", primary: true)]
    )
}
