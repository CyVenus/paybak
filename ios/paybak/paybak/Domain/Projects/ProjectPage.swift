import Foundation

/// The project detail screen (screens-projects §3–§8) as it reads: every figure of the four states
/// (Active, over budget, Closed, Archived), from `projectReport`, in display order.
nonisolated struct ProjectPage: Sendable {
    enum State: String, Sendable {
        case active
        case over
        case closed
        case archived
    }

    /// Card / Budget's figures (§1.2). Without a budget only `spent` shows.
    struct Budget: Hashable, Sendable {
        /// "₹52,000".
        var spent: String
        /// "of ₹60,000".
        var budget: String?
        /// Spent ÷ budget; over budget, budget ÷ spent (where the red starts).
        var progress: Double
        /// (Spent + planned) ÷ budget while Active and under budget, capped at the track.
        var projected: Double?
        /// "87% used".
        var percent: String?
        /// "₹8,000 left", "₹8,000 under budget" or "₹1,500 over budget".
        var left: String?
        var isOver: Bool
        /// "Planned items bring it to ₹58,000" or "All planned items are bought." (Active only).
        var planned: String?
    }

    /// One part in the Components card.
    struct ComponentRow: Hashable, Sendable, Identifiable {
        var part: ProjectComponent
        /// "Dev · Est. ₹6,000", "Dev · Unplanned", "Est. ₹6,000" while planned.
        var subtitle: String
        /// "₹9,500", or "—" while planned.
        var amount: String

        var id: ComponentID { part.id }
        /// Whose head leads the row; a planned part shows the tag icon instead.
        var payerId: PersonID? { part.status.isSpent ? part.paidBy : nil }
    }

    /// One "Paid vs fair share" row.
    struct ShareRow: Hashable, Sendable, Identifiable {
        var id: PersonID
        /// "You", "Dev".
        var name: String
        /// "Paid ₹25,500".
        var caption: String
        /// The unsigned net ("₹12,500"), or "Settled".
        var value: String
        var tone: BalanceTone
        /// Paid ÷ scale.
        var fill: Double
        /// Fair share ÷ scale.
        var mark: Double
    }

    /// What tapping a transfer does: you pay it, you remind the payer, or nothing (others' debt).
    enum TransferRole: Hashable, Sendable {
        case youPay
        case youReceive
        case others
    }

    /// One transfer of "Who owes whom" or the final settle-up plan.
    struct PlanRow: Hashable, Sendable, Identifiable {
        var transfer: Transfer
        /// "Rohan owes Dev" while Active, "Rohan pays Dev" once closed; you are "You" / "you".
        var title: String
        var amount: String
        var role: TransferRole

        var id: String { "\(transfer.from)→\(transfer.to)" }
    }

    /// A gray notice with a lock or a tick: the read-only notice, "Everyone is settled".
    struct Notice: Hashable, Sendable {
        var title: String
        var message: String
    }

    /// An archived project's member and where they ended up.
    struct MemberRow: Hashable, Sendable, Identifiable {
        var id: PersonID
        var name: String
        /// "Settled", or a signed amount.
        var status: String
    }

    var project: LedgerGroup
    var state: State
    /// "Project · 4 members · Active since 10 Aug".
    var subtitle: String
    var notice: Notice?
    var budget: Budget
    var components: [ComponentRow]
    /// "Equal split · ₹13,000 each so far"; nil hides Paid vs fair share (nothing spent, or closed).
    var shareRule: String?
    var shares: [ShareRow]
    /// Whether the plan section shows at all (hidden while nothing is spent).
    var showsPlan: Bool
    var plan: [PlanRow]
    /// "Everyone is settled" in place of a paid-off final plan.
    var planNotice: Notice?
    var footnote: String?
    /// Archived projects list their members.
    var members: [MemberRow]

    /// Active projects take new parts, edits and settings; closed and archived ones are locked.
    var isEditable: Bool { state == .active || state == .over }
}

nonisolated extension Books {
    func projectPage(_ projectId: GroupID) -> ProjectPage? {
        guard let project = ledger.group(projectId), let info = project.project else { return nil }
        let report = projectReport(projectId)
        let state: ProjectPage.State = switch info.status {
        case .active: report.isOverBudget ? .over : .active
        case .closed: .closed
        case .archived: .archived
        }
        let isActive = info.status == .active
        let hasSpent = report.spent > 0
        let plan = report.plan.map { planRow($0, isActive: isActive, currency: project.currency) }
        let isPaidOff = !isActive && plan.isEmpty
        let notice: ProjectPage.Notice? = switch state {
        case .closed: .init(title: "Closed · Read-only", message: "Components are locked. Payments can still be recorded.")
        case .archived: .init(title: "Read-only", message: "Nothing here can be edited.")
        case .active, .over: nil
        }
        return ProjectPage(
            project: project,
            state: state,
            subtitle: projectSubtitle(project),
            notice: notice,
            budget: budgetFigures(report, isActive: isActive, currency: project.currency),
            components: report.components.map { componentRow($0, currency: project.currency) },
            shareRule: isActive && hasSpent ? shareRule(project, spent: report.spent) : nil,
            shares: isActive && hasSpent ? shareRows(report, currency: project.currency) : [],
            showsPlan: !isActive || hasSpent,
            plan: plan,
            planNotice: isPaidOff ? .init(title: "Everyone is settled", message: "No payments left in this project.") : nil,
            footnote: isPaidOff || (isActive && !hasSpent)
                ? nil
                : planFootnote(projectId, isActive: isActive, hasTransfers: !plan.isEmpty, myNet: report.nets[Person.me, default: 0],
                               currency: project.currency),
            members: state == .archived ? project.memberIds.map { memberRow($0, net: report.nets[$0, default: 0], currency: project.currency) } : []
        )
    }

    /// "Project · 4 members · Active since 10 Aug", "Project · 4 members" once closed, "Project · 4
    /// members · Closed 30 Aug" when archived; a date outside this year carries the year.
    func projectSubtitle(_ project: LedgerGroup) -> String {
        let count = project.memberIds.count
        let members = "Project · \(count) \(count == 1 ? "member" : "members")"
        switch project.project?.status {
        case .active: return "\(members) · Active since \(dayLabel(project.createdAt))"
        case .archived: return project.project?.closedAt.map { "\(members) · Closed \(dayLabel($0))" } ?? members
        case .closed, nil: return members
        }
    }

    private func dayLabel(_ moment: Date) -> String {
        let day = day(of: moment)
        return day.year == today.year ? Format.short(day) : "\(Format.short(day)) \(day.year)"
    }

    private func budgetFigures(_ report: ProjectReport, isActive: Bool, currency: String) -> ProjectPage.Budget {
        let spent = Money.format(report.spent, currency)
        guard let budget = report.budget, budget > 0 else {
            return .init(spent: spent, progress: 0, isOver: false)
        }
        let over = report.spent - budget
        let left = switch (isActive, over > 0) {
        case (true, false): "\(Money.format(-over, currency)) left"
        case (false, false): "\(Money.format(-over, currency)) under budget"
        case (_, true): "\(Money.format(over, currency)) over budget"
        }
        let planned = report.plannedExtra > 0
            ? "Planned items bring it to \(Money.format(report.projection, currency))"
            : "All planned items are bought."
        return .init(
            spent: spent,
            budget: "of \(Money.format(budget, currency))",
            progress: over > 0 ? Double(budget) / Double(report.spent) : Double(report.spent) / Double(budget),
            projected: isActive && over <= 0 ? min(Double(report.projection) / Double(budget), 1) : nil,
            percent: report.percentUsed.map { "\($0)% used" },
            left: left,
            isOver: over > 0,
            planned: isActive ? planned : nil
        )
    }

    private func componentRow(_ part: ProjectComponent, currency: String) -> ProjectPage.ComponentRow {
        let estimate = part.estimatedCost.flatMap { $0 > 0 ? "Est. \(Money.format($0, currency))" : nil }
        guard part.status.isSpent else {
            return .init(part: part, subtitle: estimate ?? "No estimate", amount: "—")
        }
        return .init(part: part, subtitle: "\(firstName(part.paidBy)) · \(estimate ?? "Unplanned")",
                     amount: Money.format(part.actualCost ?? 0, currency))
    }

    private func shareRule(_ project: LedgerGroup, spent: Int64) -> String {
        switch project.project?.contribution.rule ?? .equal {
        case .equal:
            let each = spent / Int64(max(project.memberIds.count, 1))
            return "Equal split · \(Money.format(each, project.currency)) each so far"
        case .percent: return "Percent split · shares follow each person’s %"
        case .fixed: return "Fixed amounts · shares follow each person’s amount"
        }
    }

    /// Mini bars share one scale, the larger of the biggest payment and the biggest share.
    private func shareRows(_ report: ProjectReport, currency: String) -> [ProjectPage.ShareRow] {
        let scale = Double(max(report.paid.values.max() ?? 0, report.share.values.max() ?? 0, 1))
        return report.memberOrder.map { id in
            let net = report.nets[id, default: 0]
            return .init(
                id: id,
                name: firstName(id),
                caption: "Paid \(Money.format(report.paid[id, default: 0], currency))",
                value: net == 0 ? "Settled" : Money.format(abs(net), currency),
                tone: net > 0 ? .owed : net < 0 ? .owe : .settled,
                fill: Double(report.paid[id, default: 0]) / scale,
                mark: Double(report.share[id, default: 0]) / scale
            )
        }
    }

    private func planRow(_ transfer: Transfer, isActive: Bool, currency: String) -> ProjectPage.PlanRow {
        let (from, to) = (transfer.from, transfer.to)
        let verb = isActive ? "owe" : "pay"
        let title = if from == Person.me {
            "You \(verb) \(firstName(to))"
        } else if to == Person.me {
            "\(firstName(from)) \(verb)s you"
        } else {
            "\(firstName(from)) \(verb)s \(firstName(to))"
        }
        let role: ProjectPage.TransferRole = from == Person.me ? .youPay : to == Person.me ? .youReceive : .others
        return .init(transfer: transfer, title: title, amount: Money.format(transfer.amount, currency), role: role)
    }

    /// The line under the plan: where you stand, then any payment still waiting for its receiver.
    private func planFootnote(_ projectId: GroupID, isActive: Bool, hasTransfers: Bool, myNet: Int64, currency: String) -> String {
        let standing = switch (isActive, myNet.signum()) {
        case (true, _) where !hasTransfers: "Everyone’s settled in this project."
        case (true, 0): "You’re settled in this project."
        case (true, -1): "You owe \(Money.format(-myNet, currency)) in this project."
        case (true, _): "You’re owed \(Money.format(myNet, currency)) in this project."
        case (false, 0): "You’re settled. It becomes a permanent record once everyone has paid."
        case (false, _): "It becomes a permanent record once everyone has paid."
        }
        let pending = ledger.payments.count(where: { $0.groupId == projectId && $0.status == .pending })
        return switch pending {
        case 0: standing
        case 1: "\(standing) 1 payment is waiting for confirmation."
        default: "\(standing) \(pending) payments are waiting for confirmation."
        }
    }

    private func memberRow(_ id: PersonID, net: Int64, currency: String) -> ProjectPage.MemberRow {
        .init(id: id, name: firstName(id), status: net == 0 ? "Settled" : Money.format(net, currency, sign: .signed))
    }
}
