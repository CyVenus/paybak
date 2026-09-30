import Foundation

/// One of the user's payment methods (domain.md §1.1): a UPI ID or a bank account. Exactly one is
/// primary when there are any; friends only ever see the primary one.
nonisolated struct PaymentMethod: Codable, Hashable, Identifiable, Sendable {
    enum Kind: String, Codable, Sendable {
        case upi
        case bank
    }

    var id: String
    var kind: Kind
    /// The UPI ID.
    var value: String?
    var bankName: String?
    var last4: String?
    var primary: Bool

    static func upi(_ id: String, primary: Bool = true) -> PaymentMethod {
        PaymentMethod(id: RecordID.make(), kind: .upi, value: id, bankName: nil, last4: nil, primary: primary)
    }
}
