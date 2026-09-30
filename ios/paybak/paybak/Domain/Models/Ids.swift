import Foundation

// Record ids are strings (domain.md §0): readable in the demo (`p-rohan`, `g-goa`, `e-olive`),
// lowercase UUIDs for new records. The user is always the person id `Person.me`.
typealias PersonID = String
typealias GroupID = String
typealias ExpenseID = String
typealias PaymentID = String
typealias LoanID = String
typealias ComponentID = String
typealias RuleID = String
typealias DraftID = String
typealias ReminderID = String
typealias InboxItemID = String

nonisolated enum RecordID {
    /// A new record id: a lowercase UUID.
    static func make() -> String {
        UUID().uuidString.lowercased()
    }
}
