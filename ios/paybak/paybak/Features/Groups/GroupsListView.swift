import SwiftUI

/// The Groups segment (screens-groups §3.2, §3.4): groups and projects in one list (open balances
/// first), then the read-only Archived section; the Get Started empty state when there are none.
struct GroupsListView: View {
    @Environment(AppRouter.self) private var router
    @Environment(LedgerStore.self) private var ledgerStore

    var body: some View {
        let summaries = ledgerStore.snapshot.groups
        let active = summaries.filter { !$0.group.isArchived }
        let archived = summaries.filter(\.group.isArchived)
        if summaries.isEmpty {
            empty
        } else {
            VStack(alignment: .leading, spacing: PBSpace.s24) {
                rows(active)
                if !archived.isEmpty {
                    VStack(alignment: .leading, spacing: PBSpace.s4) {
                        PBSectionHeader("Archived")
                        rows(archived)
                    }
                    .accessibilityElement(children: .contain)
                    .accessibilityIdentifier("groups.archived")
                }
            }
        }
    }

    private func rows(_ summaries: [GroupSummary]) -> some View {
        VStack(spacing: 0) {
            ForEach(summaries) { summary in
                GroupSummaryRow(summary: summary, showsDivider: summary.id != summaries.last?.id)
                    .accessibilityIdentifier("groups.row.\(summary.id)")
            }
        }
    }

    private var empty: some View {
        PBEmptyState(
            illustration: .getStarted,
            title: "No groups yet.",
            message: "Start one for a trip, your flat or a project.",
            primary: .init(title: "New group", icon: .plus, testID: "groups.empty.newGroup") { router.open(.newGroup(.group)) },
            secondary: .init(title: "Invite friends", icon: .userAdd, testID: "groups.empty.inviteFriends") { router.open(.addFriend) }
        )
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier("groups.empty")
    }
}

/// A Row / Group for a group or project: opens its page (projects, archived ones read-only, open the
/// project page). `showsBudget` is off in a friend's "Groups together".
struct GroupSummaryRow: View {
    let summary: GroupSummary
    var showsBudget = true
    var showsDivider = true

    @Environment(AppRouter.self) private var router
    @Environment(LedgerStore.self) private var ledgerStore

    var body: some View {
        let copy = ledgerStore.books.groupRowCopy(summary)
        PBGroupRow(name: summary.group.name, subtitle: copy.subtitle, icon: summary.group.pbIcon, balance: balance(copy.trailing),
                   budget: showsBudget ? copy.budget.map(budget) : nil, isArchived: copy.trailing == .readOnly,
                   showsDivider: showsDivider) {
            router.open(summary.group.isProject ? .project(summary.id) : .group(summary.id))
        }
    }

    private func balance(_ trailing: GroupRowCopy.Trailing) -> PBGroupRow.Balance {
        switch trailing {
        case .owe(let amount): .owe(amount)
        case .owed(let amount): .owed(amount)
        case .status(let status): .settled(status)
        case .readOnly: .settled()
        }
    }

    private func budget(_ budget: GroupRowCopy.Budget) -> PBGroupRow.Budget {
        PBGroupRow.Budget(progress: budget.progress, spent: budget.spent, left: budget.left, isOver: budget.isOver)
    }
}

#if DEBUG
#Preview("GroupsListView") {
    GroupsPreview {
        ScrollView {
            GroupsListView().padding(PBLayout.screenMargin)
        }
    }
}

#Preview("GroupsListView · empty") {
    GroupsPreview(scenarios: Scenario.empty) {
        GroupsListView().padding(PBLayout.screenMargin)
    }
}
#endif
