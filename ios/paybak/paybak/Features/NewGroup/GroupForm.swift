import Foundation
import Observation

/// New group's draft (record-lend-group §6.4): one draft for both segments, so the name, members
/// and currency carry over between Group and Project.
@Observable
final class GroupForm {
    var mode: NewGroupMode
    var name = ""
    var type: LedgerGroup.GroupType?
    /// Everyone but you, in the order they were added.
    var members: [PersonID] = []
    var currency: String
    var simplifyDebts = true
    var details = ""
    var coverPhoto: String?
    var budgetText = ""
    var rule: Contribution.Rule = .equal
    /// Percent or Fixed values as typed, per person (you included).
    var shares: [PersonID: String] = [:]

    init(mode: NewGroupMode, currency: String) {
        self.mode = mode
        self.currency = currency
    }

    var isProject: Bool { mode == .project }

    /// You first, then the others.
    var everyone: [PersonID] { [Person.me] + members }

    var trimmedName: String { name.trimmingCharacters(in: .whitespacesAndNewlines) }

    var isDirty: Bool { !trimmedName.isEmpty || !members.isEmpty || type != nil || !details.isEmpty || coverPhoto != nil || !budgetText.isEmpty }

    /// Percent shares must add up to 100 % (Fixed and Equal always work).
    var contributionAddsUp: Bool {
        rule != .percent || everyone.reduce(0) { $0 + MoneyInput.basisPoints(shares[$1] ?? "") } == 10_000
    }

    var canCreate: Bool { !trimmedName.isEmpty && (!isProject || contributionAddsUp) }

    /// Equal: "25%", or one decimal when it doesn't divide ("33.3%").
    var equalShareText: String {
        let count = everyone.count
        let (whole, tenths) = ((1000 + count / 2) / count).quotientAndRemainder(dividingBy: 10)
        return 1000 % count == 0 && tenths == 0 ? "\(whole)%" : "\(whole).\(tenths)%"
    }

    /// The helper under Contribution.
    var contributionHelper: String {
        switch rule {
        case .equal: "Everyone pays the same share of what’s spent."
        case .percent: "Set each person’s share. Shares must add up to 100%."
        case .fixed: "Set a fixed amount for each person."
        }
    }

    func setMembers(_ ids: [PersonID]) {
        let others = ids.filter { $0 != Person.me }
        members = members.filter(others.contains) + others.filter { !members.contains($0) }
    }

    /// Switching to Percent or Fixed starts from an equal split so the fields add up.
    func setRule(_ newRule: Contribution.Rule) {
        guard newRule != rule else { return }
        rule = newRule
        shares = [:]
        guard newRule == .percent else { return }
        let (each, extra) = Int64(10_000).quotientAndRemainder(dividingBy: Int64(everyone.count))
        for (index, person) in everyone.enumerated() {
            shares[person] = MoneyInput.percentText(each + (Int64(index) < extra ? 1 : 0))
        }
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
            simplifyDebts: isProject ? true : simplifyDebts,
            project: isProject ? ProjectInfo(
                description: details.isEmpty ? nil : details,
                coverPhoto: coverPhoto,
                budget: budgetText.isEmpty ? nil : MoneyInput.minor(budgetText, currency: currency),
                contribution: contribution
            ) : nil
        )
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
