import SwiftUI

/// Group settings (screens-groups §5): name and Settle by, members (+ Add), currency, Simplify debts
/// and recurring rules, then Leave group, which only works once your balance in the group is zero.
struct GroupSettingsScreen: View {
    let groupId: GroupID

    @Environment(AppRouter.self) private var router
    @Environment(LedgerStore.self) private var ledgerStore
    @Environment(ProfileStore.self) private var profileStore

    /// "You can’t leave yet": its message and where its Settle up goes.
    @State private var block: (message: String, settle: SettleTarget)?
    @State private var isBlockShown = false
    @State private var isLeaveConfirmShown = false
    @State private var isRenaming = false
    @State private var newName = ""
    @State private var scrollPosition = ScrollPosition()
    /// One request id per picker, so each result lands in the right row.
    @State private var settleByRequest = DatePickRequest(kind: .dueDate, allowsNone: true)
    @State private var currencyRequest = CurrencyPickRequest()
    @State private var membersRequest = PeoplePickRequest(title: "Add members", showsYou: false)

    var body: some View {
        ScrollView {
            if let group = ledgerStore.ledger.group(groupId) {
                content(group)
                    .pbPushContent()
            }
        }
        .scrollPosition($scrollPosition)
        .pbPinnedHeader {
            PBPushHeader("Group settings", testIDPrefix: "groupSettings", onBack: router.back)
        }
        .pbAlert(isPresented: $isBlockShown, title: "You can’t leave yet", message: block?.message, cancelLabel: "Not now",
                 actionLabel: "Settle up", role: .primary, testIDPrefix: "groupSettings.leaveBlocked") {
            if let settle = block?.settle { router.open(settle.route(in: ledgerStore.ledger)) }
        }
        .pbAlert(isPresented: $isLeaveConfirmShown, title: "Leave \(ledgerStore.ledger.group(groupId)?.name ?? "group")?",
                 message: "You’ll stop seeing this group. Its history stays with the other members.",
                 cancelLabel: "Cancel", actionLabel: "Leave", testIDPrefix: "groupSettings.leaveConfirm", onAction: leaveGroup)
        .alert("Rename group", isPresented: $isRenaming) {
            TextField("Name", text: $newName)
            Button("Cancel", role: .cancel) {}
            Button("Save", action: rename)
        }
        .onRouteResult(settleByRequest.id) { result in
            guard case .day(let day) = result else { return }
            try? ledgerStore.updateGroup(groupId) { $0.settleBy = day }
        }
        .onRouteResult(currencyRequest.id) { result in
            guard case .currency(let code) = result else { return }
            try? ledgerStore.updateGroup(groupId) { $0.currency = code }
        }
        .onRouteResult(membersRequest.id) { result in
            guard case .people(let ids) = result, let group = ledgerStore.ledger.group(groupId) else { return }
            try? ledgerStore.addMembers(ids.filter { !group.memberIds.contains($0) }, to: groupId)
        }
        .onStartScreen([.groupLeaveBlocked]) { _ in
            scrollPosition.scrollTo(y: 26)
            leave()
        }
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier("screen.groupSettings")
    }

    private func content(_ group: LedgerGroup) -> some View {
        let books = ledgerStore.books
        return VStack(alignment: .leading, spacing: PBSpace.s24) {
            VStack(spacing: 0) {
                PBSettingRow("Name", value: group.name, icon: group.pbIcon) {
                    newName = group.name
                    isRenaming = true
                }
                .accessibilityIdentifier("groupSettings.name")
                PBSettingRow("Settle by", value: group.settleBy.map(Format.day) ?? "None", icon: .calendar, showsDivider: false) {
                    settleByRequest = DatePickRequest(kind: .dueDate, selected: group.settleBy, allowsNone: true)
                    router.open(.pickDate(settleByRequest))
                }
                .accessibilityIdentifier("groupSettings.settleBy")
            }
            .pbCard(padding: 0)
            members(group)
            VStack(alignment: .leading, spacing: PBSpace.s8) {
                VStack(spacing: 0) {
                    PBSettingRow("Currency", value: currencyValue(group.currency), icon: .exchange) {
                        currencyRequest = CurrencyPickRequest(selected: group.currency, title: "Currency")
                        router.open(.pickCurrency(currencyRequest))
                    }
                    .accessibilityIdentifier("groupSettings.currency")
                    PBSettingRow("Simplify debts", icon: .shuffle, trailing: .toggle(simplify(group)))
                        .accessibilityIdentifier("groupSettings.simplify")
                    PBSettingRow("Recurring expenses", value: books.recurringValue(group.id), icon: .repeat,
                                 badge: ledgerStore.isPro ? nil : "Pro", showsDivider: false) {
                        router.requirePro(.recurring(group.id))
                    }
                    .accessibilityIdentifier("groupSettings.recurring")
                }
                .pbCard(padding: 0)
                Text("Fewer payments to settle. Totals stay the same.")
                    .textStyle(.footnote)
                    .foregroundStyle(PBColor.textSecondary)
            }
            PBButton("Leave group", style: .destructive, icon: .logout, fillsWidth: true, action: leave)
                .accessibilityIdentifier("groupSettings.leave")
        }
    }

    private func members(_ group: LedgerGroup) -> some View {
        VStack(alignment: .leading, spacing: PBSpace.s4) {
            PBSectionHeader("Members", actionTitle: "Add") {
                membersRequest = PeoplePickRequest(selected: group.memberIds.filter { $0 != Person.me }, title: "Add members", showsYou: false)
                router.open(.pickPeople(membersRequest))
            }
            .accessibilityElement(children: .contain)
            .accessibilityIdentifier("groupSettings.addMember")
            VStack(spacing: 0) {
                ForEach(group.memberIds, id: \.self) { id in
                    memberRow(id, showsDivider: id != group.memberIds.last)
                        .accessibilityIdentifier("groupSettings.member.\(id)")
                }
            }
            .pbCard(padding: 0)
        }
    }

    @ViewBuilder
    private func memberRow(_ id: PersonID, showsDivider: Bool) -> some View {
        if id == Person.me {
            let profile = profileStore.profile
            PBPersonRow(name: "\(profile.name) (you)", avatar: profileStore.avatarContent,
                        subtitle: profile.upiID.isEmpty ? "@\(profile.inviteUsername)" : profile.upiID, size: .compact, showsDivider: showsDivider)
        } else if let person = ledgerStore.ledger.person(id) {
            PBPersonRow(name: person.name, avatar: person.avatarContent, subtitle: memberSubtitle(person), tag: person.isGuest ? "Guest" : nil,
                        size: .compact, showsDivider: showsDivider) {
                router.open(.friend(id))
            }
        }
    }

    /// The UPI ID, else "@username"; a guest's phone or email.
    private func memberSubtitle(_ person: Person) -> String? {
        if person.isGuest { return person.contact }
        return person.upi ?? person.username.map { "@\($0)" }
    }

    /// "INR ₹" · "AED" (a code whose symbol is the code shows once).
    private func currencyValue(_ code: String) -> String {
        let symbol = Money.info(code).symbol
        return symbol == code ? code : "\(code) \(symbol)"
    }

    private func simplify(_ group: LedgerGroup) -> Binding<Bool> {
        Binding {
            group.simplifyDebts
        } set: { isOn in
            try? ledgerStore.updateGroup(groupId) { $0.simplifyDebts = isOn }
        }
    }

    private func rename() {
        let name = newName.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !name.isEmpty else { return }
        try? ledgerStore.updateGroup(groupId) { $0.name = name }
    }

    // MARK: Leave

    /// Blocked while your balance isn't zero (screens-groups §2.9), else confirm first.
    private func leave() {
        switch ledgerStore.books.leaveCheck(groupId) {
        case .blocked(let message, let settle):
            block = (message, settle)
            isBlockShown = true
        case .allowed:
            isLeaveConfirmShown = true
        }
    }

    private func leaveGroup() {
        try? ledgerStore.leaveGroup(groupId)
        router.popToRoot()
    }
}

#if DEBUG
#Preview("GroupSettingsScreen") {
    GroupsPreview {
        GroupSettingsScreen(groupId: "g-goa")
    }
}
#endif
