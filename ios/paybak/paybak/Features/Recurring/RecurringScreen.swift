import SwiftUI

/// A group's recurring expenses (screens-insights-ai §5.2): the drafts that need an amount, then the
/// rules with their schedule, payer, next date and amount. Add starts a monthly expense for the
/// group; a rule opens the Repeat sheet to change its schedule or stop it.
struct RecurringScreen: View {
    let groupId: GroupID

    @Environment(AppRouter.self) private var router
    @Environment(LedgerStore.self) private var ledgerStore
    @State private var editRequest = RecordID.make()
    @State private var editing: RuleID?
    @State private var stopping: RecurringRule?

    var body: some View {
        let page = ledgerStore.books.recurringPage(groupId)
        ScrollView {
            VStack(alignment: .leading, spacing: 0) {
                Text(page.intro)
                    .textStyle(.footnote)
                    .foregroundStyle(PBColor.textSecondary)
                if !page.drafts.isEmpty {
                    drafts(page.drafts)
                        .padding(.top, PBSpace.s24)
                }
                if !page.rules.isEmpty {
                    rules(page.rules)
                        .padding(.top, PBSpace.s24)
                }
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(.horizontal, PBLayout.screenMargin)
            .padding(.top, PBSpace.s8)
            .padding(.bottom, PBSpace.s32)
            .phoneContentWidth()
        }
        .pbPinnedHeader {
            PBPushHeader("Recurring", trailing: .text("Add", action: add), testIDPrefix: "recurring", onBack: router.back)
        }
        .onRouteResult(editRequest) { result in
            if case .repeatRule(let rule) = result { update(with: rule) }
        }
        .alert("Stop repeating \(stopping?.title ?? "")?", isPresented: isStopAlertPresented, presenting: stopping) { rule in
            Button("Keep", role: .cancel) {}
            Button("Stop", role: .destructive) { try? ledgerStore.deleteRecurringRule(rule.id) }
        } message: { rule in
            Text("Paybak won’t add \(rule.title) to \(ledgerStore.books.groupName(groupId)) any more. Expenses it already added stay.")
        }
        .routeTestRoot("recurring")
    }

    // MARK: Sections

    private func drafts(_ drafts: [RecurringPage.DraftRow]) -> some View {
        VStack(alignment: .leading, spacing: 0) {
            PBSectionHeader("Needs your amount")
            VStack(spacing: PBSpace.s8) {
                ForEach(drafts) { draft in
                    DraftRowView(draft: draft) { router.open(.enterDraftAmount(draft.id)) }
                }
            }
            .padding(.top, PBSpace.s8)
            Text("Drafts don’t affect balances.")
                .textStyle(.footnote)
                .foregroundStyle(PBColor.textSecondary)
                .padding(.top, PBSpace.s8)
        }
    }

    private func rules(_ rules: [RecurringPage.RuleRow]) -> some View {
        VStack(alignment: .leading, spacing: PBSpace.s8) {
            PBSectionHeader("Rules")
            VStack(spacing: 0) {
                ForEach(rules) { rule in
                    Button { edit(rule.id) } label: {
                        PBActivityRow(
                            leading: .icon(PBIcon(rawValue: rule.icon) ?? .repeat), title: rule.title, subtitle: rule.subtitle,
                            detail: rule.detail, trailing: .amount(rule.amount, date: nil, isIncoming: true), surface: .onCard,
                            showsDivider: rule.id != rules.last?.id
                        )
                        .padding(.horizontal, PBSpace.s16)
                    }
                    .buttonStyle(PBRowButtonStyle(surface: .card))
                    .accessibilityHint("Change how it repeats")
                    .accessibilityIdentifier("recurring.rule.\(rule.id)")
                }
            }
            .padding(.vertical, PBSpace.s4)
            .pbCard(padding: 0)
        }
    }

    // MARK: Actions

    /// A new expense for the group that repeats monthly from today (proposal, §5.2).
    private func add() {
        let books = ledgerStore.books
        let members = books.ledger.group(groupId)?.memberIds ?? [Person.me]
        let draft = ExpenseDraft(
            groupId: groupId, currency: books.ledger.group(groupId)?.currency ?? books.defaultCurrency, date: books.today,
            rows: members.map { SplitRow(personId: $0) },
            repeatRule: RepeatRule(frequency: .monthly, anchorDate: books.today)
        )
        router.open(.addExpense(AddExpenseArgs(draft: draft)))
    }

    private func edit(_ id: RuleID) {
        guard let rule = ledgerStore.ledger.rule(id) else { return }
        editing = id
        editRequest = RecordID.make()
        router.open(.repeatRule(RepeatRuleRequest(id: editRequest, current: rule.repeatRule, startDate: ledgerStore.books.today)))
    }

    /// Never asks before stopping the rule; otherwise the schedule changes. A variable rule only turns
    /// fixed when it has an amount to repeat (its latest entered one).
    private func update(with repeatRule: RepeatRule?) {
        guard let id = editing, let rule = ledgerStore.ledger.rule(id) else { return }
        guard let repeatRule else {
            stopping = rule
            return
        }
        let amount = rule.amount ?? ledgerStore.ledger.expenses.last { $0.recurringRuleId == id && $0.deletedAt == nil }?.amount
        try? ledgerStore.updateRecurringRule(id) { rule in
            rule.frequency = repeatRule.frequency
            rule.anchorDate = repeatRule.anchorDate
            rule.variable = repeatRule.variable || amount == nil
            rule.amount = rule.variable ? nil : amount
        }
    }

    private var isStopAlertPresented: Binding<Bool> {
        Binding { stopping != nil } set: { if !$0 { stopping = nil } }
    }
}

/// "Cooking gas · September draft · 28 Sep · Draft" with Enter amount (Row / Attention, customised).
private struct DraftRowView: View {
    let draft: RecurringPage.DraftRow
    let onEnterAmount: () -> Void

    var body: some View {
        HStack(spacing: PBSpace.s8) {
            PBAvatar(.icon(PBIcon(rawValue: draft.icon) ?? .repeat), isOnCard: true)
            VStack(alignment: .leading, spacing: PBSpace.s6) {
                VStack(alignment: .leading, spacing: 0) {
                    Text(draft.title)
                        .textStyle(.headline)
                        .foregroundStyle(PBColor.textPrimary)
                    Text(draft.subtitle)
                        .textStyle(.footnote)
                        .foregroundStyle(PBColor.textSecondary)
                }
                .lineLimit(1)
                PBBadge("Draft", style: .onCard)
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .accessibilityElement(children: .combine)
            PBButton("Enter amount", style: .onCard, size: .small, action: onEnterAmount)
                .accessibilityIdentifier("recurring.draft.\(draft.id).enterAmount")
        }
        .padding(PBSpace.s12)
        .background(PBColor.bgCard, in: .rect(cornerRadius: PBRadius.card))
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier("recurring.draft.\(draft.id)")
    }
}

#if DEBUG
#Preview("RecurringScreen") {
    GroupsPreview(scenarios: Scenario.pro()) {
        RecurringScreen(groupId: "g-flat302")
    }
}
#endif
