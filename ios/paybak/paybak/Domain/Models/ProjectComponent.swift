import Foundation

/// A part of a project (domain.md §1.7, §8). Bought and done parts count at `actualCost`.
nonisolated struct ProjectComponent: Codable, Hashable, Identifiable, Sendable {
    enum Status: String, Codable, Sendable, CaseIterable {
        case planned
        case bought
        case done

        /// Bought and done parts count as spent.
        var isSpent: Bool { self != .planned }
    }

    struct HistoryEntry: Codable, Hashable, Sendable {
        /// `added`, or the status it moved to.
        var kind: String
        var at: Date
        var by: PersonID
    }

    var id: ComponentID
    var projectId: GroupID
    var name: String
    var status: Status
    var estimatedCost: Int64?
    var actualCost: Int64?
    var paidBy: PersonID
    var receipt: Receipt?
    var createdAt: Date
    var statusChangedAt: Date
    var history: [HistoryEntry]

    // lane fields: add optional fields below with a default.
}
