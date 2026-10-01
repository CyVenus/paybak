import SwiftUI

/// Row / Bar (Figma 143:2156): a share bar row in Insights (by category, by group) and "Paid vs fair
/// share" on a project. 56 pt, no side padding. A 40 pt leading (a category icon tile on white, or a
/// person in a white circle inside a card), the Headline title with a Footnote caption ("51%",
/// "Paid ₹25,500"), the amount on the right (plain Headline, or signed Owed/Owe) and a small bar
/// underneath. People rows usually mark the fair share on the bar.
struct PBBarRow: View {
    let leading: PBAvatar.Content
    let title: String
    var caption: String?
    let amount: String
    /// Nil shows the amount as a plain Headline value ("₹12,000", "Settled").
    var direction: PBAmountDirection?
    /// The bar's fill, 0…1.
    let progress: Double
    var mark: Double?
    var isOnCard = false

    var body: some View {
        HStack(spacing: PBSpace.s12) {
            PBAvatar(leading, isOnCard: isOnCard)
            VStack(spacing: PBSpace.s8) {
                HStack(spacing: PBSpace.s8) {
                    HStack(spacing: PBSpace.s8) {
                        Text(title)
                            .textStyle(.headline)
                            .foregroundStyle(PBColor.textPrimary)
                        if let caption {
                            // A long title truncates; the caption keeps its width.
                            Text(caption)
                                .textStyle(.footnote)
                                .foregroundStyle(PBColor.textSecondary)
                                .layoutPriority(1)
                        }
                    }
                    .lineLimit(1)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    if let direction {
                        PBSignedAmount(amount, direction: direction)
                    } else {
                        Text(amount)
                            .textStyle(.headline)
                            .foregroundStyle(PBColor.textPrimary)
                            .lineLimit(1)
                    }
                }
                PBProgressBar(value: progress, mark: mark, accessibilityLabel: title)
            }
        }
        .padding(.vertical, PBSpace.s8)
        .accessibilityElement(children: .combine)
    }
}

#Preview("PBBarRow") {
    VStack(spacing: PBSpace.s16) {
        PBBarRow(leading: .icon(.home), title: "Rent", caption: "51%", amount: "₹12,000", progress: 0.51)
        PBBarRow(leading: .icon(.food), title: "Food", caption: "22%", amount: "₹5,200", progress: 0.22)
        VStack(spacing: 0) {
            PBBarRow(leading: .art(.dev), title: "Dev", caption: "Paid ₹25,500", amount: "₹12,000", direction: .owed, progress: 1, mark: 0.51, isOnCard: true)
            PBBarRow(leading: .art(.rohan), title: "Rohan", caption: "Paid ₹9,000", amount: "₹6,000", direction: .owe, progress: 0.35, mark: 0.51, isOnCard: true)
        }
        .pbCard()
    }
    .padding(PBLayout.screenMargin)
}
