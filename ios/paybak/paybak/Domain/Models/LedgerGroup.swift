import Foundation

/// A group or a project (domain.md §1.3). Named `LedgerGroup` so it doesn't shadow SwiftUI's `Group`.
nonisolated struct LedgerGroup: Codable, Hashable, Identifiable, Sendable {
    enum Kind: String, Codable, Sendable {
        case group
        case project
    }

    enum GroupType: String, Codable, Sendable, CaseIterable {
        case trip
        case home
        case friends
        case other

        /// The icon key for the type (trip → plane, home → home, friends → people, other → tag).
        var icon: String {
            switch self {
            case .trip: "plane"
            case .home: "home"
            case .friends: "people"
            case .other: "tag"
            }
        }
    }

    var id: GroupID
    var kind: Kind
    /// Groups only.
    var type: GroupType?
    /// An icon key (`PBIcon` raw value).
    var icon: String
    var name: String
    /// All amounts in the group are in this currency.
    var currency: String
    /// Ordered; includes `Person.me` while you're a member. Order = display order and simplify tie-break.
    var memberIds: [PersonID]
    var simplifyDebts = true
    /// "Settle by": the due date of every debt in the group's plan.
    var settleBy: LocalDay?
    var createdAt: Date
    var createdBy: PersonID
    /// Projects only.
    var project: ProjectInfo?

    // lane fields: add optional fields below with a default.

    var isProject: Bool { kind == .project }
    var isArchived: Bool { project?.status == .archived }
}

/// The project part of a group (domain.md §1.3, §8).
nonisolated struct ProjectInfo: Codable, Hashable, Sendable {
    enum Status: String, Codable, Sendable {
        case active
        case closed
        case archived
    }

    var description: String?
    /// A photo file name in the photos folder.
    var coverPhoto: String?
    /// Minor units; nil = no budget.
    var budget: Int64?
    var contribution: Contribution
    /// "Collect money upfront".
    var pool = false
    var status: Status = .active
    var closedAt: Date?
    var archivedAt: Date?
}

/// How project spending is shared (domain.md §8.2).
nonisolated struct Contribution: Codable, Hashable, Sendable {
    enum Rule: String, Codable, Sendable {
        case equal
        /// `values` in basis points.
        case percent
        /// `values` in minor units; shares are proportional to them.
        case fixed
    }

    var rule: Rule = .equal
    var values: [PersonID: Int64] = [:]
}
