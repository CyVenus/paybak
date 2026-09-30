import Foundation

/// What the Sign in field accepts (flow.md): a plausible email (`x@y.z`) or phone number (at least
/// 7 digits, an optional leading `+`, spaces and dashes allowed), and how it is shown and saved.
struct SignInContact: Equatable {
    /// `.email` or `.phone`.
    let method: UserProfile.SignInMethod
    /// The trimmed email, or the phone number as typed with "+91 " in front when it has no
    /// country code (Figma: "phone numbers get +91"). Shown in "Sent to …" and saved in the profile.
    let value: String

    static let defaultCountryCode = "+91"
    private static let minimumPhoneDigits = 7

    /// nil when the input is neither a plausible email nor a plausible phone number.
    init?(_ input: String) {
        let text = input.trimmingCharacters(in: .whitespacesAndNewlines)
        if text.wholeMatch(of: #/[^@\s]+@[^@\s]+\.[^@\s]+/#) != nil {
            method = .email
            value = text
        } else if text.wholeMatch(of: #/\+?[0-9 \-]+/#) != nil,
                  text.count(where: \.isASCIIDigit) >= Self.minimumPhoneDigits {
            method = .phone
            value = text.hasPrefix("+") ? text : "\(Self.defaultCountryCode) \(text)"
        } else {
            return nil
        }
    }
}

private extension Character {
    var isASCIIDigit: Bool { isASCII && isNumber }
}
