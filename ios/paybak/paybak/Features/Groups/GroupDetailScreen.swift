import SwiftUI

/// A group (screens-groups §4): the title row with its members, your balance with Settle up, each
/// member's paid vs share with the simplify / saved-rate footnotes, then the expenses by date. A new
/// group shows the "No expenses yet." card instead (record-lend-group §7).
struct GroupDetailScreen: View {
    let groupId: GroupID

    @Environment(AppRouter.self) private var router
    @Environment(LedgerStore.self) private var ledgerStore
    @Environment(ProfileStore.self) private var profileStore

    var body: some View {
        let books = ledgerStore.books
        ScrollView {
            if let sheet = books.groupSheet(groupId) {
                content(sheet, books: books)
                    .pbPushContent()
            }
        }
        .pbPinnedHeader {
            PBPushHeader(testIDPrefix: "group", onBack: router.back)
                .overlay(alignment: .trailing) {
                    PBIconButton(.settings, accessibilityLabel: "Group settings", style: .glass) {
                        router.open(.groupSettings(groupId))
                    }
                    .accessibilityIdentifier("group.settings")
                }
        }
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier("screen.group")
    }

    private func content(_ sheet: GroupSheet, books: Books) -> some View {
        VStack(alignment: .leading, spacing: PBSpace.s24) {
            VStack(spacing: PBSpace.s16) {
                PBTitleHeader(title: sheet.group.name, leading: .icon(sheet.group.pbIcon), subtitle: books.groupHeaderSubtitle(sheet),
                              members: members(sheet.group))
                    .accessibilityElement(children: .combine)
                    .accessibilityIdentifier("group.title")
                balanceCard(books.groupBalanceCopy(sheet))
            }
            if sheet.expenses.isEmpty {
                PBEmptyState(
                    title: "No expenses yet.",
                    message: "Add the first expense and Paybak will split it for everyone.",
                    primary: .init(title: "Add expense", icon: .plus, testID: "group.addExpense") { addExpense(sheet.group) }
                )
                .accessibilityElement(children: .contain)
                .accessibilityIdentifier("group.empty")
            } else {
                balances(sheet, books: books)
                expenses(sheet, books: books)
            }
        }
    }

    // MARK: Header and balance

    /// Up to four member heads (Figma draws no "+N"); guests without art are left out.
    private func members(_ group: LedgerGroup) -> [PBPeepHead] {
        group.memberIds.compactMap { id in
            if id == Person.me {
                if case .art(let head) = profileStore.avatarContent { return head }
                return nil
            }
            return ledgerStore.ledger.person(id)?.avatar.flatMap(PBPeepHead.init(rawValue:))
        }
    }

    private func balanceCard(_ copy: GroupBalanceCopy) -> some View {
        PBBalanceCard(kind: kind(copy.tone), label: "Your balance", amount: copy.amount, caption: copy.caption,
                      onSettleUp: copy.showsSettleUp ? { settle(copy.settle) } : nil, isSettleUpEnabled: copy.settle != nil,
                      testIDPrefix: "group")
    }

    private func settle(_ target: SettleTarget?) {
        guard let target else { return }
        router.open(target.route(in: ledgerStore.ledger))
    }

    private func addExpense(_ group: LedgerGroup) {
        let draft = ExpenseDraft(groupId: group.id, currency: group.currency, date: ledgerStore.clock.today,
                                 payers: [Payer(personId: Person.me, amount: 0)],
                                 rows: group.memberIds.map { SplitRow(personId: $0) })
        router.open(.addExpense(AddExpenseArgs(draft: draft)))
    }

    // MARK: Balances

    private func balances(_ sheet: GroupSheet, books: Books) -> some View {
        let rows = books.memberBalances(sheet)
        let footnotes = [sheet.simplifyFootnote.map { ($0, "group.simplifyNote") }, books.groupTotalFootnote(sheet).map { ($0, "group.totalNote") }]
            .compactMap(\.self)
        return VStack(alignment: .leading, spacing: PBSpace.s4) {
            PBSectionHeader("Balances")
            VStack(alignment: .leading, spacing: PBSpace.s8) {
                VStack(spacing: 0) {
                    ForEach(rows) { row in
                        memberRow(row, showsDivider: row.id != rows.last?.id)
                    }
                }
                .pbCard(padding: 0)
                ForEach(footnotes, id: \.1) { text, id in
                    Text(text)
                        .textStyle(.footnote)
                        .foregroundStyle(PBColor.textSecondary)
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .accessibilityIdentifier(id)
                }
            }
        }
    }

    private func memberRow(_ row: MemberBalanceCopy, showsDivider: Bool) -> some View {
        let isMe = row.id == Person.me
        let avatar = isMe ? profileStore.avatarContent : ledgerStore.ledger.person(row.id)?.avatarContent ?? .icon(.profile)
        let trailing: PBPersonRow.Trailing = switch row.trailing {
        case .owe(let amount, let label): .amount(amount, direction: .owe, label: label)
        case .owed(let amount, let label): .amount(amount, direction: .owed, label: label)
        case .status(let status): .status(status)
        }
        return PBPersonRow(name: row.name, avatar: avatar, subtitle: row.subtitle, size: .compact, trailing: trailing,
                           showsDivider: showsDivider, action: isMe ? nil : { router.open(.friend(row.id)) })
            .accessibilityIdentifier("group.balances.row.\(row.id)")
    }

    // MARK: Expenses

    private func expenses(_ sheet: GroupSheet, books: Books) -> some View {
        let days = Dictionary(grouping: sheet.expenses, by: \.date)
        let order = sheet.expenses.map(\.date).reduce(into: [LocalDay]()) { if $0.last != $1 { $0.append($1) } }
        return VStack(alignment: .leading, spacing: PBSpace.s4) {
            PBSectionHeader("Expenses")
            VStack(alignment: .leading, spacing: PBSpace.s8) {
                ForEach(order, id: \.self) { day in
                    VStack(alignment: .leading, spacing: PBSpace.s4) {
                        Text(books.expenseDayLabel(day))
                            .textStyle(.footnote)
                            .foregroundStyle(PBColor.textSecondary)
                        VStack(spacing: 0) {
                            ForEach(days[day] ?? []) { expense in
                                expenseRow(expense, books: books)
                            }
                        }
                    }
                }
            }
        }
    }

    private func expenseRow(_ expense: Expense, books: Books) -> some View {
        Button {
            router.open(.expense(expense.id))
        } label: {
            PBActivityRow(leading: .icon(expense.category.pbIcon), title: expense.title, subtitle: books.groupExpenseSubtitle(expense),
                          detail: expense.rate.map { Money.approximateLine(expense.amount, currency: expense.currency, rate: $0) },
                          trailing: .amount(Money.format(expense.amount, expense.currency), date: nil, isIncoming: true))
                .contentShape(.rect)
        }
        .buttonStyle(PBRowButtonStyle())
        .accessibilityIdentifier("group.expense.\(expense.id)")
    }

    private func kind(_ tone: BalanceTone) -> PBBalanceCard.Kind {
        switch tone {
        case .owed: .owed
        case .owe: .owe
        case .settled: .settled
        }
    }
}

extension SettleTarget {
    /// Record payment prefilled with the one transfer (by UPI when you pay someone with an ID), or the
    /// group's Settle up plan.
    func route(in ledger: Ledger) -> Route {
        switch self {
        case .pay(let transfer, let currency, let groupId) where transfer.from == Person.me:
            .recordPayment(.paying(transfer.to, amount: transfer.amount, currency: currency, context: .group(groupId), in: ledger))
        case .pay(let transfer, let currency, let groupId):
            .recordPayment(RecordPaymentArgs(from: transfer.from, to: transfer.to, amount: transfer.amount, currency: currency,
                                             context: .group(groupId)))
        case .plan(let groupId):
            .settleUp(groupId: groupId)
        }
    }
}

#if DEBUG
#Preview("GroupDetailScreen · Goa Trip") {
    GroupsPreview {
        GroupDetailScreen(groupId: "g-goa")
    }
}

#Preview("GroupDetailScreen · Dubai Weekend") {
    GroupsPreview {
        GroupDetailScreen(groupId: "g-dubai")
    }
}
#endif
