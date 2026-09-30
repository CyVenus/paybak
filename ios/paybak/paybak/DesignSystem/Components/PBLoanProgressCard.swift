import SwiftUI

/// Card / Loan Progress (Figma 145:2155): an IOU's progress on the loan detail. Three columns
/// (Original, Paid, Remaining: Footnote labels over Amount/Medium values), a divider, a large bar
/// filled with the share paid back, and the caption ("0% paid back"). Paid back adds a check before
/// the caption ("Paid back on 14 Sep"). Never red: overdue installments show on their own rows.
struct PBLoanProgressCard: View {
    let original: String
    let paid: String
    let remaining: String
    /// Paid ÷ original, 0…1.
    let progress: Double
    let caption: String
    var isPaidBack = false

    var body: some View {
        VStack(alignment: .leading, spacing: PBSpace.s12) {
            HStack(spacing: PBSpace.s12) {
                stat("Original", original)
                stat("Paid", paid)
                stat("Remaining", remaining)
            }
            PBDivider()
            PBProgressBar(value: progress, size: .large, accessibilityLabel: "Paid back")
            HStack(spacing: PBSpace.s6) {
                if isPaidBack {
                    PBIconView(.checkCircle, size: PBSize.iconSm)
                        .foregroundStyle(PBColor.iconPrimary)
                }
                Text(caption)
                    .textStyle(.footnote)
                    .foregroundStyle(PBColor.textSecondary)
            }
        }
        .padding(PBLayout.cardPadding)
        .background(PBColor.bgCard, in: .rect(cornerRadius: PBRadius.card))
        .accessibilityElement(children: .combine)
    }

    private func stat(_ label: String, _ value: String) -> some View {
        VStack(alignment: .leading, spacing: PBSpace.s2) {
            Text(label)
                .textStyle(.footnote)
                .foregroundStyle(PBColor.textSecondary)
            Text(value)
                .textStyle(.amountMedium)
                .foregroundStyle(PBColor.textPrimary)
        }
        .lineLimit(1)
        .frame(maxWidth: .infinity, alignment: .leading)
    }
}

#Preview("PBLoanProgressCard") {
    VStack(spacing: PBSpace.s16) {
        PBLoanProgressCard(original: "₹6,000", paid: "₹0", remaining: "₹6,000", progress: 0, caption: "0% paid back")
        PBLoanProgressCard(original: "₹4,500", paid: "₹4,500", remaining: "₹0", progress: 1, caption: "Paid back on 14 Sep", isPaidBack: true)
    }
    .padding(PBLayout.screenMargin)
}
