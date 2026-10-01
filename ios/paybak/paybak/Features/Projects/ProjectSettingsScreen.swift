import SwiftUI

/// Project settings (screens-projects §6): the contribution rule with each member's share, Add
/// member, the budget, "Collect money upfront" and Close project. Every change saves as it's made;
/// a Percent or Fixed rule applies only once it adds up. Closing asks first, then shows the project
/// in its Closed state.
struct ProjectSettingsScreen: View {
    let groupId: GroupID

    @Environment(AppRouter.self) private var router
    @Environment(LedgerStore.self) private var ledgerStore

    /// The rule as edited; nil shows the saved one.
    @State private var edit: ContributionEdit?
    /// The budget's raw digits; nil shows the saved budget.
    @State private var budgetText: String?
    @State private var isCloseShown = false
    @State private var membersRequest = PeoplePickRequest(title: "Add members", showsYou: false)
    @FocusState private var isBudgetFocused: Bool

    var body: some View {
        let project = ledgerStore.ledger.group(groupId)
        ScrollView {
            if let project, let info = project.project {
                content(project, info: info)
                    .pbPushContent()
            }
        }
        .scrollDismissesKeyboard(.interactively)
        .pbPinnedHeader {
            PBPushHeader("Project settings", testIDPrefix: "projectSettings", onBack: router.back)
        }
        .pbAlert(isPresented: $isCloseShown, title: "Close \(project?.name ?? "project")?",
                 message: "Components lock and everyone sees the final plan.", cancelLabel: "Cancel",
                 actionLabel: "Close project", role: .primary, testIDPrefix: "projectSettings.closeAlert", onAction: closeProject)
        .onRouteResult(membersRequest.id) { result in
            guard case .people(let ids) = result, let project else { return }
            try? ledgerStore.addMembers(ids.filter { !project.memberIds.contains($0) }, to: groupId)
        }
        .onChange(of: isBudgetFocused) { _, isFocused in
            if !isFocused { saveBudget() }
        }
        .onDisappear(perform: saveBudget)
        .onStartScreen([.projectCloseAlert]) { _ in
            isCloseShown = true
        }
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier("screen.projectSettings")
    }

    private func content(_ project: LedgerGroup, info: ProjectInfo) -> some View {
        VStack(alignment: .leading, spacing: PBLayout.sectionGap) {
            contribution(project, info: info)
            budget(project, info: info)
            VStack(alignment: .leading, spacing: PBSpace.s8) {
                PBSettingRow("Collect money upfront", icon: .wallet, trailing: .toggle(pool(info)), showsDivider: false)
                    .pbCard(padding: 0)
                    .accessibilityIdentifier("projectSettings.pool")
                footnote("When on, members pay into a pool first and purchases come out of it.")
            }
            PBSettingRow("Close project", icon: .lock, trailing: .none, showsDivider: false) {
                isCloseShown = true
            }
            .pbCard(padding: 0)
            .accessibilityIdentifier("projectSettings.close")
        }
    }

    // MARK: Contribution rule

    private func contribution(_ project: LedgerGroup, info: ProjectInfo) -> some View {
        let members = project.memberIds
        let current = edit ?? ContributionEdit(info.contribution, members: members, currency: project.currency)
        let check = current.check(members: members, budget: info.budget, currency: project.currency)
        return VStack(alignment: .leading, spacing: PBSpace.s12) {
            VStack(alignment: .leading, spacing: PBSpace.s8) {
                PBSectionHeader("Contribution rule")
                PBSegmentedControl(options: ["Equal", "Percent", "Fixed"], selection: Binding {
                    Self.rules.firstIndex(of: current.rule) ?? 0
                } set: { index in
                    guard Self.rules[index] != current.rule else { return }
                    Haptics.selection()
                    let spent = ledgerStore.books.projectSpent(groupId)
                    apply(.prefill(Self.rules[index], members: members, budget: info.budget, spent: spent, currency: project.currency))
                }, testIDPrefix: "projectSettings.rule")
                footnote(check.helper, isError: !check.isValid)
                    .accessibilityIdentifier("projectSettings.ruleHelper")
            }
            VStack(spacing: 0) {
                ForEach(members, id: \.self) { id in
                    memberRow(id, edit: current, count: members.count, currency: project.currency)
                        .accessibilityElement(children: .contain)
                        .accessibilityIdentifier("projectSettings.member.\(id)")
                }
                PBSettingRow("Add member", icon: .userAdd, showsDivider: false) {
                    membersRequest = PeoplePickRequest(selected: members.filter { $0 != Person.me }, title: "Add members", showsYou: false)
                    router.open(.pickPeople(membersRequest))
                }
                .accessibilityIdentifier("projectSettings.addMember")
            }
            .pbCard(padding: 0)
        }
    }

    private static let rules: [Contribution.Rule] = [.equal, .percent, .fixed]

    /// Equal shows the read-only "25%"; Percent and Fixed put a field in its place.
    private func memberRow(_ id: PersonID, edit current: ContributionEdit, count: Int, currency: String) -> some View {
        PBPersonRow(name: ledgerStore.books.firstName(id), avatar: ledgerStore.memberAvatar(id), size: .compact,
                    trailing: current.rule == .equal ? .amount(ContributionEdit.equalShareText(count: count)) : .none)
            .overlay(alignment: .trailing) {
                if current.rule != .equal {
                    let isPercent = current.rule == .percent
                    PBInlineField(
                        text: Binding {
                            current.typed[id] ?? ""
                        } set: { text in
                            var next = current
                            next.typed[id] = PBAmountField.sanitize(text, allowsDecimals: isPercent || Money.info(currency).exponent > 0)
                            apply(next)
                        },
                        accessibilityLabel: "\(ledgerStore.books.firstName(id))’s share",
                        prefix: isPercent ? nil : Money.info(currency).symbol,
                        suffix: isPercent ? "%" : nil
                    )
                    .padding(.trailing, PBSpace.s16)
                    .accessibilityIdentifier("projectSettings.share.\(id)")
                }
            }
    }

    /// Keeps what was typed and saves it once it adds up; until then the saved rule stays in force.
    private func apply(_ next: ContributionEdit) {
        edit = next
        guard let project = ledgerStore.ledger.group(groupId), let info = project.project else { return }
        let members = project.memberIds
        guard next.check(members: members, budget: info.budget, currency: project.currency).isValid else { return }
        let contribution = next.contribution(members: members, currency: project.currency)
        if contribution != info.contribution {
            try? ledgerStore.setContribution(contribution, of: groupId)
        }
    }

    // MARK: Budget and pool

    private func budget(_ project: LedgerGroup, info: ProjectInfo) -> some View {
        let currency = Currency(code: project.currency)
        let raw = budgetText ?? info.budget.map { MoneyInput.text($0, currency: project.currency) } ?? ""
        return PBTextField("Budget", text: Binding {
            raw.isEmpty ? "" : PBAmountField.display(raw, currency: currency)
        } set: {
            budgetText = PBAmountField.sanitize($0, allowsDecimals: Money.info(project.currency).exponent > 0)
        }, prompt: "\(currency.symbol)0", helper: "You’ll see a warning if spending goes over.", focus: $isBudgetFocused)
            .keyboardType(.decimalPad)
            .accessibilityIdentifier("projectSettings.budget")
    }

    /// Saves the typed budget when the field loses focus; empty means no budget.
    private func saveBudget() {
        guard let budgetText, let project = ledgerStore.ledger.group(groupId), project.project?.status == .active else { return }
        let budget = MoneyInput.minor(budgetText, currency: project.currency)
        let value = budget > 0 ? budget : nil
        if value != project.project?.budget {
            try? ledgerStore.setBudget(value, of: groupId)
        }
        self.budgetText = nil
    }

    private func pool(_ info: ProjectInfo) -> Binding<Bool> {
        Binding {
            info.pool
        } set: { isOn in
            try? ledgerStore.setPool(isOn, of: groupId)
        }
    }

    // MARK: Close

    private func closeProject() {
        do {
            try ledgerStore.closeProject(groupId)
            Haptics.success()
            router.back()
        } catch {
            Haptics.warning()
        }
    }

    private func footnote(_ text: String, isError: Bool = false) -> some View {
        Text(text)
            .textStyle(.footnote)
            .foregroundStyle(isError ? PBColor.textDestructive : PBColor.textSecondary)
            .fixedSize(horizontal: false, vertical: true)
    }

}

#if DEBUG
#Preview("ProjectSettingsScreen") {
    GroupsPreview {
        ProjectSettingsScreen(groupId: "pj-drone")
    }
}
#endif
