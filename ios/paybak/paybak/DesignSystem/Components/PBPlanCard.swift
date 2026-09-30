import SwiftUI

/// Card / Plan (Figma 125:1198): a paywall plan option, 104 pt tall with 20 pt corners; two share the
/// content width 20 pt apart. Selected = black with white text (the detail at 72 %); otherwise
/// #F5F5F5. Headline period with an optional white "Save 33%" pill, the Amount/Medium price and a
/// Footnote detail. Tap selects (single choice).
struct PBPlanCard: View {
    let period: String
    let price: String
    let detail: String
    var badge: String?
    let isSelected: Bool
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            VStack(alignment: .leading, spacing: 0) {
                HStack(spacing: PBSpace.s8) {
                    Text(period)
                        .textStyle(.headline)
                        .lineLimit(1)
                        .frame(maxWidth: .infinity, alignment: .leading)
                    if let badge {
                        PBBadge(badge, style: .onCard)
                            .fixedSize()
                    }
                }
                Spacer(minLength: PBSpace.s8)
                Text(price)
                    .textStyle(.amountMedium)
                    .lineLimit(1)
                Text(detail)
                    .textStyle(.footnote)
                    .foregroundStyle(isSelected ? PBColor.textInverse.opacity(0.72) : PBColor.textSecondary)
                    .lineLimit(1)
            }
            .foregroundStyle(isSelected ? PBColor.textInverse : PBColor.textPrimary)
            .padding(PBLayout.cardPadding)
            .frame(maxWidth: .infinity, minHeight: 104, alignment: .leading)
            .background(isSelected ? PBColor.bgInverse : PBColor.bgCard, in: .rect(cornerRadius: PBRadius.card))
            .contentShape(.rect(cornerRadius: PBRadius.card))
        }
        .buttonStyle(.plain)
        .animation(.easeOut(duration: 0.15), value: isSelected)
        .accessibilityElement(children: .combine)
        .accessibilityAddTraits(isSelected ? [.isButton, .isSelected] : .isButton)
    }
}

#Preview("PBPlanCard") {
    @Previewable @State var yearly = true
    HStack(spacing: PBSpace.s20) {
        PBPlanCard(period: "Yearly", price: "₹799/year", detail: "₹67/month", badge: "Save 33%", isSelected: yearly) { yearly = true }
        PBPlanCard(period: "Monthly", price: "₹99/month", detail: "Billed monthly", isSelected: !yearly) { yearly = false }
    }
    .padding(PBLayout.screenMargin)
}
