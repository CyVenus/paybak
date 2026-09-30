import SwiftUI

/// Header / Amount Hero (Figma 128:2004): the left-aligned hero of an expense, payment or loan
/// detail. A 56 pt leading (the category icon, the other person's avatar, or a payer → receiver
/// pair), then the Title/2 title (wraps), the Title/1 amount, the Footnote meta and up to three
/// pills: the group and the category (Muted) and a status (Inverse: "Disputed", "Paid back").
/// Status pills are black or gray, never red.
struct PBAmountHero: View {
    enum Leading {
        case avatar(PBAvatar.Content)
        case pair(from: PBAvatar.Content, to: PBAvatar.Content)
    }

    let leading: Leading
    let title: String
    let amount: String
    var meta: String?
    /// Muted pills: the group, then the category.
    var chips: [String] = []
    /// The Inverse status pill.
    var status: String?

    var body: some View {
        VStack(alignment: .leading, spacing: PBSpace.s12) {
            switch leading {
            case .avatar(let content):
                PBAvatar(content, diameter: PBSize.avatarLg)
            case .pair(let from, let to):
                PBAvatarPair(from: from, to: to, diameter: PBSize.avatarLg)
            }
            VStack(alignment: .leading, spacing: PBSpace.s2) {
                Text(title)
                    .textStyle(.title2)
                    .foregroundStyle(PBColor.textPrimary)
                    .fixedSize(horizontal: false, vertical: true)
                    .accessibilityAddTraits(.isHeader)
                Text(amount)
                    .textStyle(.title1)
                    .foregroundStyle(PBColor.textPrimary)
                    .lineLimit(1)
                    .minimumScaleFactor(0.6)
                if let meta {
                    Text(meta)
                        .textStyle(.footnote)
                        .foregroundStyle(PBColor.textSecondary)
                }
            }
            if !chips.isEmpty || status != nil {
                HStack(spacing: PBSpace.s8) {
                    ForEach(chips, id: \.self) { PBBadge($0) }
                    if let status {
                        PBBadge(status, style: .inverse)
                    }
                }
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }
}

#Preview("PBAmountHero") {
    ScrollView {
        VStack(alignment: .leading, spacing: PBSpace.s32) {
            PBAmountHero(leading: .avatar(.icon(.food)), title: "Seafood dinner at Britto’s", amount: "₹6,500", meta: "Paid by you · 22 Sep", chips: ["Goa Trip", "Food"])
            PBAmountHero(leading: .avatar(.art(.dev)), title: "Lent to Dev", amount: "₹6,000", meta: "Due 30 Oct", status: "Paid back")
            PBAmountHero(leading: .pair(from: .art(.arjun), to: .art(.kabir)), title: "You paid Kabir", amount: "₹4,500", meta: "UPI · 14 Sep", chips: ["Goa Trip"], status: "Disputed")
        }
        .padding(PBLayout.screenMargin)
    }
}
