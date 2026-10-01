import Foundation

/// Lane B's Projects actions (app-architecture §7.1, screens-projects §1).
extension LedgerStore {
    /// Adds a part from the Add component sheet.
    @discardableResult
    func addComponent(to projectId: GroupID, _ form: ComponentForm) throws -> ComponentID {
        try mutate { try $0.addComponent(to: projectId, form, at: clock.now) }
    }

    /// Saves the Edit component sheet over part `id`.
    func editComponent(_ id: ComponentID, _ form: ComponentForm) throws {
        try mutate { try $0.editComponent(id, form, at: clock.now) }
    }

    func deleteComponent(_ id: ComponentID) throws {
        try mutate { try $0.deleteComponent(id) }
    }

    /// A rule that adds up; shares, bars and transfers follow it at once.
    func setContribution(_ contribution: Contribution, of projectId: GroupID) throws {
        try mutate { try $0.updateProject(projectId) { $0.contribution = contribution } }
    }

    /// The budget, or none: the dashboard then drops the bar and the projection.
    func setBudget(_ budget: Int64?, of projectId: GroupID) throws {
        try mutate { try $0.updateProject(projectId) { $0.budget = budget } }
    }

    /// "Collect money upfront".
    func setPool(_ isOn: Bool, of projectId: GroupID) throws {
        try mutate { try $0.updateProject(projectId) { $0.pool = isOn } }
    }

    /// Archives a closed project at once when its final plan is paid and confirmed (screens-projects
    /// §1.6), as the scheduler would on its next run. Returns whether it archived; writes nothing
    /// otherwise.
    @discardableResult
    func archiveIfSettled(_ projectId: GroupID) -> Bool {
        guard ledger.group(projectId)?.project?.status == .closed,
              books.groupNets(projectId).values.allSatisfy({ $0 == 0 }) else { return false }
        mutate { $0.archiveSettledProjects(at: clock.now) }
        return true
    }
}
