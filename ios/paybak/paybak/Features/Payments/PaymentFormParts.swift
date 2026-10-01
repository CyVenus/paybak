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

/// What a payment is for (proposal): the groups and loans you share with the person, then "No
/// group" (directly between you).
struct PaymentForSheet: View {
    let contexts: [PaymentFor]
    let selected: PaymentFor
    let onClose: () -> Void
    let onSelect: (PaymentFor) -> Void

    @Environment(LedgerStore.self) private var store

    var body: some View {
        PBSheet(title: "For", testIDPrefix: "paymentFor", onClose: onClose) {
            VStack(spacing: 0) {
                ForEach(contexts, id: \.self) { context in
                    PBSettingRow(title(context), icon: icon(context), trailing: context == selected ? .check : .unchecked,
                                 showsDivider: context != contexts.last) {
                        Haptics.selection()
                        onSelect(context)
                    }
                }
            }
            .pbCard(padding: 0)
        }
    }

    private func title(_ context: PaymentFor) -> String {
        switch context {
        case .direct: "No group"
        case .group(let id): store.ledger.group(id)?.name ?? "Group"
        case .loan(let id): "Loan · \(store.ledger.loan(id)?.title ?? "")"
        }
    }

    private func icon(_ context: PaymentFor) -> PBIcon {
        switch context {
        case .direct: .people
        case .group(let id): store.ledger.group(id)?.pbIcon ?? .groups
        case .loan: .lend
        }
    }
}

#Preview("UPIPayeeCard") {
    UPIPayeeCard(name: "Kabir Singh", avatar: .art(.kabir), upi: "kabir@okaxis") {}
        .padding(PBLayout.screenMargin)
}
