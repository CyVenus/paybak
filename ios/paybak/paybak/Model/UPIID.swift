import Foundation

/// The UPI ID on Setup 3: optional, but when it's there it must look like `name@bank`.
enum UPIID {
    /// A trimmed UPI ID such as "arjun@okaxis": at least two of letters, digits, `.`, `_` or `-`,
    /// then `@` and a handle of at least two letters or digits.
    static func isValid(_ text: String) -> Bool {
        text.wholeMatch(of: #/[A-Za-z0-9._\-]{2,}@[A-Za-z0-9]{2,}/#) != nil
    }
}
