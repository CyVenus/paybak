import SwiftUI

/// Row / Transfer (Figma 128:1604): one payment in a settle-up plan, 64 pt inside a #F5F5F5 card.
/// The white from → to avatar pair, the Headline title ("Rohan owes Dev") and the Amount/Medium
/// amount. The divider starts at the title; hide it on the last row.
struct PBTransferRow: View {
    let from: PBAvatar.Content
    let to: PBAvatar.Content
    let title: String
    let amount: String
    var showsDivider = true
    var action: (() -> Void)?

    var body: some View {
        Group {
            if let action {
                Button(action: action) { row }
                    .buttonStyle(PBRowButtonStyle(surface: .card))
            } else {
                row
            }
        }
        .overlay(alignment: .bottom) {
            if showsDivider {
                PBDivider().padding(.leading, 116)
            }
        }
    }

    private var row: some View {
        HStack(spacing: PBSpace.s12) {
            PBAvatarPair(from: from, to: to, isOnCard: true)
            Text(title)
                .textStyle(.headline)
                .foregroundStyle(PBColor.textPrimary)
                .lineLimit(1)
                .frame(maxWidth: .infinity, alignment: .leading)
            Text(amount)
                .textStyle(.amountMedium)
                .foregroundStyle(PBColor.textPrimary)
                .lineLimit(1)
        }
        .padding(.vertical, PBSpace.s8)
        .padding(.horizontal, PBSpace.s16)
        .frame(minHeight: 64)
        .contentShape(.rect)
        .accessibilityElement(children: .combine)
    }
}

#Preview("PBTransferRow") {
    VStack(spacing: 0) {
        PBTransferRow(from: .art(.rohan), to: .art(.dev), title: "Rohan owes Dev", amount: "₹8,500")
        PBTransferRow(from: .art(.priya), to: .art(.arjun), title: "Priya pays you", amount: "₹1,200", showsDivider: false)
    }
    .pbCard(padding: 0)
    .padding(PBLayout.screenMargin)
}
