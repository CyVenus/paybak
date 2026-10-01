import SwiftUI

/// Card / Payment Preview with Show copy (record-lend-group §2.3, settle §4): who you're paying and
/// their UPI ID, with an On Card "Copy" button.
struct UPIPayeeCard: View {
    let name: String
    let avatar: PBAvatar.Content
    let upi: String
    let onCopy: () -> Void

    var body: some View {
        HStack(spacing: PBSpace.s12) {
            PBAvatar(avatar, diameter: PBSize.avatarMd, isOnCard: true)
            VStack(alignment: .leading, spacing: PBSpace.s2) {
                Text(name)
                    .textStyle(.headline)
                    .foregroundStyle(PBColor.textPrimary)
                Text(upi)
                    .textStyle(.subheadline)
                    .foregroundStyle(PBColor.textSecondary)
                    .truncationMode(.middle)
            }
            .lineLimit(1)
            .frame(maxWidth: .infinity, alignment: .leading)
            .accessibilityElement(children: .combine)
            PBButton("Copy", style: .onCard, size: .small, icon: .copy, action: onCopy)
                .accessibilityLabel("Copy UPI ID")
                .accessibilityIdentifier("recordPayment.preview.copy")
        }
        .padding(PBLayout.cardPadding)
        .background(PBColor.bgCard, in: .rect(cornerRadius: PBRadius.card))
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier("recordPayment.preview")
    }
}

/// What a payment is for (proposal), as Record payment's For: "None" (directly between you), the
/// groups and projects you share with the person, then your open loans with them. Before anyone is
/// chosen it's the Group picker: "No group", then your groups.
struct PaymentForSheet: View {
    let friend: PersonID?
    let selected: PaymentFor
    let onClose: () -> Void
    let onSelect: (PaymentFor) -> Void

    @Environment(LedgerStore.self) private var store

    private struct Choice: Identifiable {
        let id: String
        let context: PaymentFor
        let title: String
        let icon: PBIcon
    }

    var body: some View {
        let choices = self.choices
        PBSheet(title: friend == nil ? "Group" : "For", testIDPrefix: "paymentFor", onClose: onClose) {
            ScrollView {
                VStack(spacing: 0) {
                    ForEach(choices) { choice in
                        PBSettingRow(choice.title, icon: choice.icon, trailing: isSelected(choice.context) ? .check : .unchecked,
                                     showsDivider: choice.id != choices.last?.id) {
                            Haptics.selection()
                            onSelect(choice.context)
                        }
                        .accessibilityIdentifier("paymentFor.row.\(choice.id)")
                    }
                }
                .pbCard(padding: 0)
            }
            .scrollBounceBehavior(.basedOnSize)
            .frame(maxHeight: 56 * 7)
        }
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier("paymentFor.sheet")
    }

    private var choices: [Choice] {
        let ledger = store.ledger
        let none = Choice(id: "none", context: .direct(expense: nil), title: friend == nil ? "No group" : "None", icon: .groups)
        let groups = ledger.groups
            .filter { $0.memberIds.contains(Person.me) && !$0.isArchived }
            .filter { group in friend.map { group.memberIds.contains($0) } ?? !group.isProject }
            .map { Choice(id: $0.id, context: .group($0.id), title: $0.name, icon: $0.pbIcon) }
        guard let friend else { return [none] + groups }
        let books = store.books
        let loans = ledger.loans
            .filter { $0.friendId == friend && books.openBalance(with: friend, for: .loan($0.id)) != 0 }
            .map { Choice(id: $0.id, context: .loan($0.id), title: "Loan · \($0.title)", icon: .lend) }
        return [none] + groups + loans
    }

    /// "None" stands for anything directly between you.
    private func isSelected(_ context: PaymentFor) -> Bool {
        if case .direct = context, case .direct = selected { return true }
        return context == selected
    }
}

#Preview("UPIPayeeCard") {
    UPIPayeeCard(name: "Kabir Singh", avatar: .art(.kabir), upi: "kabir@okaxis") {}
        .padding(PBLayout.screenMargin)
}
