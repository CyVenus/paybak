import Foundation
import Observation

/// New group's draft (record-lend-group §6.4): one draft for both segments, so the name, members
/// and currency carry over between Group and Project.
@Observable
final class GroupForm {
    /// Everything the form holds, to tell whether it changed since it opened (Discard asks first).
    struct Snapshot: Equatable {
        var mode: NewGroupMode
        var name: String
        var type: LedgerGroup.GroupType?
        var members: [PersonID]
        var currency: String
        var simplifyDebts: Bool
        var details: String
        var coverPhoto: String?
        var budgetText: String
        var rule: Contribution.Rule
        var shares: [PersonID: String]
    }

    /// The longest name and description the fields take.
    static let maxName = 40
    static let maxDescription = 120

    var mode: NewGroupMode
    var name = ""
    var type: LedgerGroup.GroupType?
    /// Everyone but you, in the order the people picker gives them.
    var members: [PersonID] = []
    var currency: String
    var simplifyDebts = true
    var details = ""
    var coverPhoto: String?
    var budgetText = ""
    var rule: Contribution.Rule = .equal
    /// Percent or Fixed values as typed, per person (you included).
    var shares: [PersonID: String] = [:]

    @ObservationIgnored private var initial: Snapshot?

    init(mode: NewGroupMode, currency: String) {
        self.mode = mode
        self.currency = currency
        markUnchanged()
    }

    /// The state Discard compares against: as opened (or as a debug start screen prefilled it).
    func markUnchanged() {
        initial = snapshot
    }

    var snapshot: Snapshot {
        Snapshot(mode: mode, name: name, type: type, members: members, currency: currency, simplifyDebts: simplifyDebts,
                 details: details, coverPhoto: coverPhoto, budgetText: budgetText, rule: rule, shares: shares)
    }

    var isProject: Bool { mode == .project }

    /// You first, then the others.
    var everyone: [PersonID] { [Person.me] + members }

    var trimmedName: String { name.trimmingCharacters(in: .whitespacesAndNewlines) }

    /// Anything changed since the form opened, the Group | Project switch included.
    var isDirty: Bool { snapshot != initial }

    /// Percent shares must add up to 100 %; Fixed needs an amount for everyone.
    var contributionAddsUp: Bool {
        switch rule {
        case .equal: true
        case .percent: everyone.reduce(0) { $0 + MoneyInput.basisPoints(shares[$1] ?? "") } == 10_000
        case .fixed: everyone.allSatisfy { MoneyInput.minor(shares[$0] ?? "", currency: currency) > 0 }
        }
    }

    var canCreate: Bool { !trimmedName.isEmpty && (!isProject || contributionAddsUp) }

    /// Equal: "25%", or one decimal when it doesn't divide ("33.3%").
    var equalShareText: String {
        let count = everyone.count
        let (whole, tenths) = ((1000 + count / 2) / count).quotientAndRemainder(dividingBy: 10)
        return tenths == 0 ? "\(whole)%" : "\(whole).\(tenths)%"
    }

    /// The helper under Contribution.
    var contributionHelper: String {
        switch rule {
        case .equal: "Everyone pays the same share of what’s spent."
        case .percent: "Set each person’s share. Shares must add up to 100%."
        case .fixed: "Set a fixed amount for each person."
        }
    }

    /// The people picked, in the picker's order (you're always first, so you're left out here).
    func setMembers(_ ids: [PersonID]) {
        members = ids.filter { $0 != Person.me }
    }

    /// A new rule starts with empty fields.
    func setRule(_ newRule: Contribution.Rule) {
        guard newRule != rule else { return }
        rule = newRule
        shares = [:]
    }

    var draft: GroupDraft {
        let type = type ?? .other
        return GroupDraft(
            kind: isProject ? .project : .group,
            type: isProject ? nil : type,
            icon: isProject ? PBIcon.package.rawValue : type.icon,
            name: trimmedName,
            currency: currency,
            memberIds: everyone,
            simplifyDebts: simplifyDebts,
            project: isProject ? ProjectInfo(
                description: trimmedDetails.isEmpty ? nil : trimmedDetails,
                coverPhoto: coverPhoto,
                budget: budget,
                contribution: contribution
            ) : nil
        )
    }

    private var trimmedDetails: String { details.trimmingCharacters(in: .whitespacesAndNewlines) }

    /// No budget unless it's more than zero.
    private var budget: Int64? {
        let minor = MoneyInput.minor(budgetText, currency: currency)
        return minor > 0 ? minor : nil
    }

    private var contribution: Contribution {
        switch rule {
        case .equal: Contribution()
        case .percent: Contribution(rule: .percent, values: Dictionary(uniqueKeysWithValues: everyone.map { ($0, MoneyInput.basisPoints(shares[$0] ?? "")) }))
        case .fixed: Contribution(rule: .fixed, values: Dictionary(uniqueKeysWithValues: everyone.map { ($0, MoneyInput.minor(shares[$0] ?? "", currency: currency)) }))
        }
    }
}

extension LedgerGroup.GroupType {
    var title: String {
        switch self {
        case .trip: "Trip"
        case .home: "Home"
        case .friends: "Friends"
        case .other: "Other"
        }
    }
}
