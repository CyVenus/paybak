import SwiftUI

/// A friend (screens-groups §6): the one net between you with why it's owed and the actions that
/// settle it, then your shared history, groups together and their automatic reminders. A guest gets
/// the invite notice; someone you share nothing with yet gets "No balance yet" and Add expense.
struct FriendScreen: View {
    let personId: PersonID

    @Environment(AppRouter.self) private var router
    @Environment(LedgerStore.self) private var ledgerStore
    @Environment(ProfileStore.self) private var profileStore
    @State private var share: ShareItem?

    /// History shows this many rows, then "See all".
    private static let historyLimit = 10

    var body: some View {
        let books = ledgerStore.books
        ScrollView {
            if let person = ledgerStore.ledger.person(personId), let page = books.friendPage(personId) {
                content(person, page: page, books: books)
                    .pbPushContent()
            }
        }
        .pbPinnedHeader {
            PBPushHeader(testIDPrefix: "friend", onBack: router.back)
        }
        .systemShare(item: $share)
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier("screen.friend")
    }

    private func content(_ person: Person, page: FriendPage, books: Books) -> some View {
        let balance = books.friendBalanceCopy(page)
        let title = PBTitleHeader(title: person.name, leading: person.avatarContent, subtitle: subtitle(person),
                                  tag: person.isGuest ? "Guest" : nil)
            .accessibilityElement(children: .combine)
            .accessibilityIdentifier("friend.title")
        return VStack(alignment: .leading, spacing: PBSpace.s24) {
            if person.isGuest {
                title
                VStack(spacing: balance == nil ? PBSpace.s32 : PBSpace.s16) {
                    inviteNotice(person)
                    if let balance {
                        balanceBlock(balance, person: person, page: page, books: books)
                    } else {
                        noBalance(person)
                    }
                }
            } else {
                VStack(alignment: .leading, spacing: PBSpace.s16) {
                    title
                    if let balance {
                        balanceBlock(balance, person: person, page: page, books: books)
                    }
                }
                if balance == nil {
                    noBalance(person)
                }
            }
            if balance != nil {
                history(page, books: books)
                groupsTogether(page)
                reminders(person, books: books)
            }
        }
    }

    /// The UPI ID, else "@username"; nothing for a guest.
    private func subtitle(_ person: Person) -> String? {
        guard !person.isGuest else { return nil }
        return person.upi ?? person.username.map { "@\($0)" }
    }

    // MARK: Balance and actions

    private func balanceBlock(_ copy: FriendBalanceCopy, person: Person, page: FriendPage, books: Books) -> some View {
        VStack(alignment: .leading, spacing: PBSpace.s8) {
            VStack(spacing: PBSpace.s12) {
                PBBalanceCard(kind: kind(copy.tone), label: copy.label, amount: copy.amount, caption: copy.caption,
                              badge: copy.overdue.map { ($0, .overdue) }, testIDPrefix: "friend")
                actions(copy, person: person, net: page.balance.net)
            }
            if copy.tone == .owed, let reminder = page.lastReminder {
                Text(books.lastReminderText(reminder) + ".")
                    .textStyle(.footnote)
                    .foregroundStyle(PBColor.textTertiary)
                    .accessibilityIdentifier("friend.lastReminder")
            }
        }
    }

    @ViewBuilder
    private func actions(_ copy: FriendBalanceCopy, person: Person, net: Int64) -> some View {
        let amount = abs(net)
        switch copy.tone {
        case .owed:
            HStack(spacing: PBSpace.s12) {
                PBButton("Remind", icon: .bell, fillsWidth: true) {
                    router.open(.remind(personId: person.id, context: copy.lead.map(reminderContext)))
                }
                .accessibilityIdentifier("friend.remind")
                PBButton("Record payment", style: .secondary, fillsWidth: true) {
                    router.open(.recordPayment(RecordPaymentArgs(from: person.id, to: Person.me, amount: amount,
                                                                 currency: currency, context: copy.lead.map(paymentContext))))
                }
                .accessibilityIdentifier("friend.recordPayment")
            }
        case .owe:
            PBButton("Settle up", fillsWidth: true) {
                router.open(.recordPayment(RecordPaymentArgs(from: Person.me, to: person.id, amount: amount,
                                                             currency: currency, context: copy.lead.map(paymentContext))))
            }
            .accessibilityIdentifier("friend.settleUp")
        case .settled:
            EmptyView()
        }
    }

    private var currency: String { profileStore.profile.defaultCurrency }

    private func reminderContext(_ item: Obligation) -> ReminderContext {
        switch item.kind {
        case .direct: .expense(item.ref)
        case .group, .project: .group(item.ref)
        case .loan: .loan(item.ref)
        }
    }

    private func paymentContext(_ item: Obligation) -> PaymentContext {
        switch item.kind {
        case .direct: .expense(item.ref)
        case .group, .project: .group(item.ref)
        case .loan: .loan(item.ref)
        }
    }

    // MARK: Guest and no balance

    private func inviteNotice(_ person: Person) -> some View {
        let first = person.firstName
        let (object, subject, possessive) = switch person.pronoun {
        case .she: ("her", "she", "her")
        case .he: ("him", "he", "his")
        case .they: ("them", "they", "their")
        }
        return PBNoticeCard(
            icon: .mail,
            title: "Invite \(first) to Paybak",
            message: "\(first) isn’t on Paybak yet. You can still split with \(object). When \(subject) join\(person.pronoun == .they ? "" : "s") "
                + "with the same phone or email, \(possessive) history moves to \(possessive) account.",
            primary: .init("Send invite", icon: .share) {
                share = ShareItem(text: profileStore.profile.inviteMessage)
            }
        )
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier("friend.invite")
    }

    private func noBalance(_ person: Person) -> some View {
        VStack(spacing: PBSpace.s16) {
            VStack(spacing: PBSpace.s4) {
                Text("No balance yet")
                    .textStyle(.headline)
                    .foregroundStyle(PBColor.textPrimary)
                Text("Expenses you share with \(person.firstName) will show here.")
                    .textStyle(.footnote)
                    .foregroundStyle(PBColor.textSecondary)
            }
            .multilineTextAlignment(.center)
            .accessibilityElement(children: .combine)
            .accessibilityIdentifier("friend.noBalance")
            PBButton("Add expense", style: .secondary, size: .small, icon: .plus) {
                let draft = ExpenseDraft(currency: currency, date: ledgerStore.clock.today, payers: [Payer(personId: Person.me, amount: 0)],
                                         rows: [SplitRow(personId: Person.me), SplitRow(personId: person.id)])
                router.open(.addExpense(AddExpenseArgs(draft: draft)))
            }
            .accessibilityIdentifier("friend.addExpense")
        }
        .frame(maxWidth: .infinity)
    }

    // MARK: History, groups together, reminders

    @ViewBuilder
    private func history(_ page: FriendPage, books: Books) -> some View {
        let rows = books.friendHistoryRows(page)
        if !rows.isEmpty {
            VStack(alignment: .leading, spacing: PBSpace.s4) {
                if rows.count > Self.historyLimit {
                    PBSectionHeader("History") { router.open(.activityLog(.person(personId))) }
                } else {
                    PBSectionHeader("History")
                }
                VStack(spacing: 0) {
                    ForEach(rows.prefix(Self.historyLimit)) { row in
                        historyRow(row)
                    }
                }
            }
        }
    }

    private func historyRow(_ row: FriendHistoryRow) -> some View {
        let leading: PBActivityRow.Leading = switch row.leading {
        case .icon(let icon): .icon(PBIcon(rawValue: icon) ?? .receipt)
        case .person(let id): .avatar(id == Person.me ? profileStore.avatarContent : ledgerStore.ledger.person(id)?.avatarContent ?? .icon(.profile))
        }
        return Button {
            switch row.target {
            case .expense(let id): router.open(.expense(id))
            case .payment(let id): router.open(Route.payment(id))
            case .loan(let id): router.open(.loan(id))
            }
        } label: {
            PBActivityRow(leading: leading, title: row.title, subtitle: row.subtitle,
                          trailing: .amount(row.amount, date: row.date, isIncoming: row.isOpen))
                .contentShape(.rect)
        }
        .buttonStyle(PBRowButtonStyle())
        .accessibilityIdentifier("friend.history.\(row.id)")
    }

    @ViewBuilder
    private func groupsTogether(_ page: FriendPage) -> some View {
        let groups = ledgerStore.snapshot.groups
        let summaries = page.groupsTogether.compactMap { group in groups.first { $0.id == group.id } }
        if !summaries.isEmpty {
            VStack(alignment: .leading, spacing: PBSpace.s4) {
                PBSectionHeader("Groups together")
                VStack(spacing: 0) {
                    ForEach(summaries) { summary in
                        GroupSummaryRow(summary: summary, showsBudget: false, showsDivider: summary.id != summaries.last?.id)
                            .accessibilityIdentifier("friend.group.\(summary.id)")
                    }
                }
            }
        }
    }

    private func reminders(_ person: Person, books: Books) -> some View {
        let isOn = Binding {
            !person.remindersMuted
        } set: { isOn in
            try? ledgerStore.setRemindersMuted(person.id, !isOn)
        }
        return VStack(alignment: .leading, spacing: PBSpace.s8) {
            PBSettingRow("Automatic reminders", icon: .bell, trailing: .toggle(isOn), showsDivider: false)
                .pbCard(padding: 0)
                .accessibilityIdentifier("friend.autoReminders")
            Text(books.autoRemindersFootnote(person.id))
                .textStyle(.footnote)
                .foregroundStyle(PBColor.textSecondary)
        }
    }

    private func kind(_ tone: BalanceTone) -> PBBalanceCard.Kind {
        switch tone {
        case .owed: .owed
        case .owe: .owe
        case .settled: .settled
        }
    }
}

#if DEBUG
#Preview("FriendScreen · Rohan") {
    GroupsPreview {
        FriendScreen(personId: "p-rohan")
    }
}

#Preview("FriendScreen · Ananya (guest)") {
    GroupsPreview {
        FriendScreen(personId: "p-ananya")
    }
}
#endif
