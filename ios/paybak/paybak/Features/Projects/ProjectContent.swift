import SwiftUI

/// The project dashboard's sections in the order each state draws them (screens-projects §10):
/// Active: summary, components, paid vs fair share, history, who owes whom. Closed: summary with the
/// read-only notice, the final plan, then the locked components and history. Archived: the same with
/// its members after the plan.
struct ProjectContent: View {
    let page: ProjectPage
    /// Opens the Edit component sheet (active projects only).
    let onEdit: (ComponentID) -> Void

    @Environment(AppRouter.self) private var router
    @Environment(LedgerStore.self) private var ledgerStore

    var body: some View {
        VStack(alignment: .leading, spacing: PBLayout.sectionGap) {
            summary
            switch page.state {
            case .active, .over:
                components
                if let rule = page.shareRule {
                    fairShare(rule)
                }
                history
                if page.showsPlan {
                    plan(title: "Who owes whom")
                }
            case .closed:
                plan(title: "Final settle-up plan")
                components
                history
            case .archived:
                plan(title: "Final settle-up plan")
                if !page.members.isEmpty {
                    members
                }
                components
                history
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }

    // MARK: Summary

    private var summary: some View {
        VStack(spacing: PBSpace.s16) {
            PBTitleHeader(title: page.project.name, leading: .icon(page.project.pbIcon), subtitle: page.subtitle,
                          tag: page.state == .archived ? "Archived" : nil, memberAvatars: memberAvatars)
                .accessibilityElement(children: .combine)
                .accessibilityIdentifier("project.title")
            if let notice = page.notice {
                PBNoticeCard(icon: .lock, title: notice.title, message: notice.message)
                    .accessibilityElement(children: .combine)
                    .accessibilityIdentifier("project.notice")
            }
            budgetCard
                .accessibilityIdentifier("project.budget")
        }
    }

    /// Every member's avatar, yours included and guests by their initials; the header stacks the
    /// first four, and none for a one-member project.
    private var memberAvatars: [PBAvatarStack.Member] {
        page.project.memberIds.map { id in
            id == Person.me ? PBAvatarStack.Member.user : .content(ledgerStore.memberAvatar(id))
        }
    }

    private var budgetCard: some View {
        let budget = page.budget
        // Over budget wins over closed: the red bar and warning stay once the project is locked.
        let status: PBBudgetCard.Status = if budget.isOver {
            .overBudget(warning: budget.left ?? "")
        } else if !page.isEditable {
            .closed
        } else {
            .onTrack(projected: budget.projected)
        }
        return PBBudgetCard(spent: budget.spent, budget: budget.budget, progress: budget.progress, status: status,
                            percentLabel: budget.percent ?? "", leftLabel: budget.left ?? "", plannedText: budget.planned)
    }

    // MARK: Components

    @ViewBuilder
    private var components: some View {
        if page.isEditable || !page.components.isEmpty {
            VStack(alignment: .leading, spacing: PBSpace.s8) {
                PBSectionHeader("Components")
                VStack(spacing: 0) {
                    if page.components.isEmpty {
                        Text("No components yet. Add the first part below.")
                            .textStyle(.subheadline)
                            .foregroundStyle(PBColor.textSecondary)
                            .multilineTextAlignment(.center)
                            .padding(PBLayout.cardPadding)
                            .frame(maxWidth: .infinity, minHeight: 64)
                            .accessibilityIdentifier("project.components.empty")
                    }
                    ForEach(page.components) { row in
                        componentRow(row, showsDivider: row.id != page.components.last?.id)
                    }
                }
                .pbCard(padding: 0)
            }
        }
    }

    @ViewBuilder
    private func componentRow(_ row: ProjectPage.ComponentRow, showsDivider: Bool) -> some View {
        let content = PBActivityRow(
            leading: row.payerId.map { .avatar(ledgerStore.memberAvatar($0)) } ?? .icon(.tag),
            title: row.part.name,
            subtitle: row.subtitle,
            trailing: .amountBadge(row.amount, badge: row.part.status.title, style: row.part.status.badgeStyle,
                                   isPlaceholder: !row.part.status.isSpent),
            surface: .onCard,
            titleLines: 2,
            subtitleLines: 3,
            showsDivider: showsDivider
        )
        .padding(.horizontal, PBLayout.cardPadding)
        if page.isEditable {
            Button { onEdit(row.id) } label: { content.contentShape(.rect) }
                .buttonStyle(PBRowButtonStyle(surface: .card))
                .accessibilityHint("Edit component")
                .accessibilityIdentifier("project.component.\(row.id)")
        } else {
            content
                .accessibilityIdentifier("project.component.\(row.id)")
        }
    }

    // MARK: Paid vs fair share

    private func fairShare(_ rule: String) -> some View {
        VStack(alignment: .leading, spacing: PBSpace.s8) {
            PBSectionHeader("Paid vs fair share")
            VStack(alignment: .leading, spacing: PBSpace.s8) {
                Text(rule)
                    .textStyle(.footnote)
                    .foregroundStyle(PBColor.textSecondary)
                    .accessibilityIdentifier("project.shareRule")
                VStack(spacing: 0) {
                    ForEach(page.shares) { row in
                        PBBarRow(leading: ledgerStore.memberAvatar(row.id), title: row.name, caption: row.caption, amount: row.value,
                                 direction: direction(row.tone), progress: row.fill, mark: row.mark, isOnCard: true)
                            .accessibilityIdentifier("project.share.\(row.id)")
                    }
                }
            }
            .pbCard()
        }
    }

    private func direction(_ tone: BalanceTone) -> PBAmountDirection? {
        switch tone {
        case .owed: .owed
        case .owe: .owe
        case .settled: nil
        }
    }

    // MARK: History

    private var history: some View {
        PBSettingRow("History", icon: .activity, showsDivider: false) {
            router.open(.activityLog(.project(page.project.id)))
        }
        .pbCard(padding: 0)
        .accessibilityIdentifier("project.history")
    }

    // MARK: Plan

    private func plan(title: String) -> some View {
        VStack(alignment: .leading, spacing: PBSpace.s8) {
            PBSectionHeader(title)
            if let notice = page.planNotice {
                PBNoticeCard(icon: .checkCircle, title: notice.title, message: notice.message)
                    .accessibilityElement(children: .combine)
                    .accessibilityIdentifier("project.planNotice")
            } else if !page.plan.isEmpty {
                VStack(spacing: 0) {
                    ForEach(Array(page.plan.enumerated()), id: \.element.id) { index, row in
                        PBTransferRow(from: ledgerStore.memberAvatar(row.transfer.from), to: ledgerStore.memberAvatar(row.transfer.to), title: row.title,
                                      amount: row.amount, showsDivider: index < page.plan.count - 1, action: action(row))
                            .accessibilityIdentifier("project.transfer.\(index)")
                    }
                }
                .pbCard(padding: 0)
            }
            if let footnote = page.footnote {
                Text(footnote)
                    .textStyle(.footnote)
                    .foregroundStyle(PBColor.textSecondary)
                    .fixedSize(horizontal: false, vertical: true)
                    .accessibilityIdentifier("project.footnote")
            }
        }
    }

    /// You pay → Record payment prefilled with the project; you receive → Remind the payer; a debt
    /// between two others is theirs to record (§3.8).
    private func action(_ row: ProjectPage.PlanRow) -> (() -> Void)? {
        let projectId = page.project.id
        switch row.role {
        case .youPay:
            return {
                router.open(.recordPayment(.paying(row.transfer.to, amount: row.transfer.amount, currency: page.project.currency,
                                                   context: .group(projectId), in: ledgerStore.ledger)))
            }
        case .youReceive:
            return { router.open(.remind(personId: row.transfer.from, context: .group(projectId))) }
        case .others:
            return nil
        }
    }

    // MARK: Members

    private var members: some View {
        VStack(alignment: .leading, spacing: PBSpace.s8) {
            PBSectionHeader("Members")
            VStack(spacing: 0) {
                ForEach(page.members) { member in
                    PBPersonRow(name: member.name, avatar: ledgerStore.memberAvatar(member.id), size: .compact, trailing: .status(member.status),
                                showsDivider: member.id != page.members.last?.id)
                        .accessibilityIdentifier("project.members.\(member.id)")
                }
            }
            .pbCard(padding: 0)
        }
    }

}

extension ProjectComponent.Status {
    /// "Planned", "Bought", "Done".
    var title: String { rawValue.capitalized }

    /// Planned is the quiet gray pill, Bought white on the card, Done black.
    var badgeStyle: PBBadge.Style {
        switch self {
        case .planned: .mutedOnCard
        case .bought: .onCard
        case .done: .inverse
        }
    }
}

extension LedgerStore {
    /// A member's circle on the project screens: your own avatar, a friend's art or their initials
    /// (the first letter of their name when they're gone from the ledger).
    func memberAvatar(_ id: PersonID) -> PBAvatar.Content {
        id == Person.me ? profileStore.avatarContent
            : ledger.person(id)?.avatarContent ?? .initials(String(books.firstName(id).prefix(1)))
    }
}
