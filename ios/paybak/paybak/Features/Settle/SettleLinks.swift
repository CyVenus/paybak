import Foundation

extension RecordPaymentArgs {
    /// Record payment prefilled from a suggestion (settle §4.2): you pay `payee` the amount for the
    /// group or expense, by UPI when they've shared a UPI ID, else cash.
    static func paying(_ payee: PersonID, amount: Int64, currency: String, context: PaymentContext?, in ledger: Ledger) -> RecordPaymentArgs {
        RecordPaymentArgs(from: Person.me, to: payee, amount: amount, currency: currency,
                          method: ledger.person(payee)?.upi == nil ? .cash : .upi, context: context)
    }
}
