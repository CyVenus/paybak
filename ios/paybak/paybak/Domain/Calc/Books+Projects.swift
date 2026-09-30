import Foundation

/// A project's budget card and fair shares (domain.md §8).
nonisolated struct ProjectReport: Sendable {
    var spent: Int64
    var budget: Int64?
    /// Σ estimated cost of planned parts.
    var plannedExtra: Int64
    /// Rounded half up.
    var percentUsed: Int?
    var paid: [PersonID: Int64]
    var share: [PersonID: Int64]
    var nets: [PersonID: Int64]
    /// Members by net descending, ties by member order.
    var memberOrder: [PersonID]
    /// Who owes whom, amount descending.
    var plan: [Transfer]
    /// Planned first, then bought, then done; newest status change first inside each.
    var components: [ProjectComponent]

    var projection: Int64 { spent + plannedExtra }
    var isOverBudget: Bool { budget.map { spent > $0 } ?? false }
}

nonisolated extension Books {
    func projectParts(_ projectId: GroupID) -> [ProjectComponent] {
        ledger.components.filter { $0.projectId == projectId }
    }

    /// Σ actual cost of bought and done parts.
    func projectSpent(_ projectId: GroupID) -> Int64 {
        projectParts(projectId).filter(\.status.isSpent).reduce(0) { $0 + ($1.actualCost ?? 0) }
    }

    /// What each member paid for bought/done parts, and their share of the spend by the contribution
    /// rule (§8.2).
    func projectPaidShare(_ projectId: GroupID) -> (paid: [PersonID: Int64], share: [PersonID: Int64]) {
        guard let group = ledger.group(projectId) else { return ([:], [:]) }
        let members = group.memberIds
        var paid = Dictionary(uniqueKeysWithValues: members.map { ($0, Int64(0)) })
        for part in projectParts(projectId) where part.status.isSpent {
            paid[part.paidBy, default: 0] += part.actualCost ?? 0
        }
        let spent = paid.values.reduce(0, +)
        let counter = ledger.rotation[projectId, default: 0]
        let contribution = group.project?.contribution ?? Contribution()
        let share = switch contribution.rule {
        case .equal: Splits.equal(spent, among: members, counter: counter).shares
        case .percent, .fixed: Splits.weighted(spent, weights: contribution.values, order: members, counter: counter).shares
        }
        return (paid, share)
    }

    func projectReport(_ projectId: GroupID) -> ProjectReport {
        let group = ledger.group(projectId)
        let members = group?.memberIds ?? []
        let parts = projectParts(projectId)
        let spent = projectSpent(projectId)
        let budget = group?.project?.budget
        let (paid, share) = projectPaidShare(projectId)
        let nets = groupNets(projectId)
        let order = members.enumerated()
            .sorted { lhs, rhs in
                let (a, b) = (nets[lhs.element, default: 0], nets[rhs.element, default: 0])
                return a != b ? a > b : lhs.offset < rhs.offset
            }
            .map(\.element)
        let statusOrder: [ProjectComponent.Status] = [.planned, .bought, .done]
        let listed = parts.enumerated()
            .sorted { lhs, rhs in
                let (a, b) = (statusOrder.firstIndex(of: lhs.element.status)!, statusOrder.firstIndex(of: rhs.element.status)!)
                if a != b { return a < b }
                if lhs.element.statusChangedAt != rhs.element.statusChangedAt {
                    return lhs.element.statusChangedAt > rhs.element.statusChangedAt
                }
                return lhs.offset < rhs.offset
            }
            .map(\.element)
        let plan = groupPlan(projectId).enumerated()
            .sorted { $0.element.amount != $1.element.amount ? $0.element.amount > $1.element.amount : $0.offset < $1.offset }
            .map(\.element)
        return ProjectReport(
            spent: spent,
            budget: budget,
            plannedExtra: parts.filter { $0.status == .planned }.reduce(0) { $0 + ($1.estimatedCost ?? 0) },
            percentUsed: budget.flatMap { $0 > 0 ? Int((spent * 100 * 2 + $0) / (2 * $0)) : nil },
            paid: paid,
            share: share,
            nets: nets,
            memberOrder: order,
            plan: plan,
            components: listed
        )
    }
}
