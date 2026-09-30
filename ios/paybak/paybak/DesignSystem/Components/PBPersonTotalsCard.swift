import SwiftUI

/// Card / Person Totals (Figma 147:2533): the live per-person totals pinned above Continue on
/// Assign items. A status row (a check with "All items assigned", or "3 items left to assign"
/// without it) with the right-aligned note ("Includes GST and tip"), then one column per person:
/// a white 32 pt avatar beside the Footnote name over the Amount/Medium total.
struct PBPersonTotalsCard: View {
    struct Total: Identifiable {
        let id: String
        let name: String
        let avatar: PBAvatar.Content
        let amount: String
    }

    let status: String
    /// Shows the check before the status once every item is assigned.
    var isComplete = true
    var note: String?
    let totals: [Total]

    var body: some View {
        VStack(alignment: .leading, spacing: PBSpace.s6) {
            HStack(spacing: PBSpace.s6) {
                if isComplete {
                    PBIconView(.checkCircle, size: PBSize.iconSm)
                        .foregroundStyle(PBColor.iconPrimary)
                }
                Text(status)
                    .textStyle(.footnote)
                    .foregroundStyle(PBColor.textPrimary)
                    .fixedSize()
                if let note {
                    Text(note)
                        .textStyle(.footnote)
                        .foregroundStyle(PBColor.textSecondary)
                        .lineLimit(1)
                        .frame(maxWidth: .infinity, alignment: .trailing)
                }
            }
            HStack(spacing: PBSpace.s12) {
                ForEach(totals) { total in
                    HStack(spacing: PBSpace.s8) {
                        PBAvatar(total.avatar, diameter: PBSize.avatarSm, isOnCard: true)
                        VStack(alignment: .leading, spacing: 0) {
                            Text(total.name)
                                .textStyle(.footnote)
                                .foregroundStyle(PBColor.textSecondary)
                            Text(total.amount)
                                .textStyle(.amountMedium)
                                .foregroundStyle(PBColor.textPrimary)
                        }
                        .lineLimit(1)
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .accessibilityElement(children: .combine)
                }
            }
        }
        .padding(PBLayout.cardPadding)
        .background(PBColor.bgCard, in: .rect(cornerRadius: PBRadius.card))
    }
}

#Preview("PBPersonTotalsCard") {
    let totals: [PBPersonTotalsCard.Total] = [
        .init(id: "you", name: "You", avatar: .art(.arjun), amount: "₹989"),
        .init(id: "esha", name: "Esha", avatar: .art(.esha), amount: "₹621"),
        .init(id: "dev", name: "Dev", avatar: .art(.dev), amount: "₹690"),
    ]
    VStack(spacing: PBSpace.s16) {
        PBPersonTotalsCard(status: "All items assigned", note: "Includes GST and tip", totals: totals)
        PBPersonTotalsCard(status: "3 items left to assign", isComplete: false, note: "Includes GST and tip", totals: totals)
    }
    .padding(PBLayout.screenMargin)
}
