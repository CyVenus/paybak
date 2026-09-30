import SwiftUI

/// Card / Confirm Payment (Figma 129:2060): the receiver's confirm card at the top of Home and in
/// Activity. Pending: the payer's white 40 pt avatar, "Esha says she paid you ₹700", the Footnote
/// detail and two small buttons sharing the width, Confirm and Not received. Confirmed: the
/// confirmed texts and a check, no buttons. Confirming collapses the card (120 → 72 pt) with the
/// texts cross-fading, 250 ms ease-out (instant with Reduce Motion).
/// Test ids: `<prefix>.confirm`, `<prefix>.notReceived`.
struct PBConfirmPaymentCard: View {
    let avatar: PBAvatar.Content
    let title: String
    let detail: String
    let confirmedTitle: String
    let confirmedDetail: String
    let isConfirmed: Bool
    var testIDPrefix: String?
    var onConfirm: () -> Void = {}
    var onNotReceived: () -> Void = {}

    @Environment(\.accessibilityReduceMotion) private var reduceMotion

    var body: some View {
        VStack(spacing: PBSpace.s12) {
            HStack(spacing: PBSpace.s12) {
                PBAvatar(avatar, diameter: PBSize.avatarMd, isOnCard: true)
                ZStack(alignment: .leading) {
                    if isConfirmed {
                        texts(confirmedTitle, confirmedDetail)
                            .transition(.opacity)
                    } else {
                        texts(title, detail)
                            .transition(.opacity)
                    }
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                if isConfirmed {
                    PBIconView(.checkCircle)
                        .foregroundStyle(PBColor.iconPrimary)
                        .transition(.opacity)
                }
            }
            .accessibilityElement(children: .combine)
            if !isConfirmed {
                HStack(spacing: PBSpace.s8) {
                    PBButton("Confirm", size: .small, fillsWidth: true, action: onConfirm)
                        .accessibilityIdentifier(testID("confirm"))
                    PBButton("Not received", style: .onCard, size: .small, fillsWidth: true, action: onNotReceived)
                        .accessibilityIdentifier(testID("notReceived"))
                }
                .transition(.opacity)
            }
        }
        .padding(PBLayout.cardPadding)
        .background(PBColor.bgCard, in: .rect(cornerRadius: PBRadius.card))
        .clipped()
        .animation(reduceMotion ? nil : .easeOut(duration: 0.25), value: isConfirmed)
    }

    private func texts(_ title: String, _ detail: String) -> some View {
        VStack(alignment: .leading, spacing: 0) {
            Text(title)
                .textStyle(.headline)
                .foregroundStyle(PBColor.textPrimary)
            Text(detail)
                .textStyle(.footnote)
                .foregroundStyle(PBColor.textSecondary)
        }
        .lineLimit(1)
    }

    private func testID(_ element: String) -> String {
        testIDPrefix.map { "\($0).\(element)" } ?? ""
    }
}

#Preview("PBConfirmPaymentCard") {
    @Previewable @State var isConfirmed = false
    VStack(spacing: PBSpace.s16) {
        PBConfirmPaymentCard(
            avatar: .art(.esha),
            title: "Esha says she paid you ₹700",
            detail: "Dinner at Olive Garden · UPI · 9:12 pm",
            confirmedTitle: "Esha paid you ₹700",
            confirmedDetail: "Dinner at Olive Garden · UPI · Confirmed",
            isConfirmed: isConfirmed,
            onConfirm: { isConfirmed = true }
        )
        PBButton("Reset", style: .secondary, size: .small) { isConfirmed = false }
    }
    .padding(PBLayout.screenMargin)
}
