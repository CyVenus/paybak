import SwiftUI

/// Chat / Draft Expense (Figma 147:2315): the assistant's draft expense in Ask Paybak. A white
/// category icon, the Headline title and Amount/Medium amount, a divider, then the paid-by line, the
/// split line with the members' avatars and the per-person line. Pending: a wide black Save and a
/// white Edit. Saved: a check, "Expense added" and a View text button. Saving swaps the states in
/// place (250 ms ease-out); there is no toast and no navigation.
/// Test ids: `<prefix>.save`, `<prefix>.edit`, `<prefix>.view`.
struct PBDraftExpenseCard: View {
    let icon: PBIcon
    let title: String
    let amount: String
    /// "Paid by you · Today".
    let paidLine: String
    /// "Split equally with Esha and Dev".
    let splitLine: String
    let members: [PBPeepHead]
    /// "₹200 each".
    let eachLine: String
    let isSaved: Bool
    var testIDPrefix: String?
    var onSave: () -> Void = {}
    var onEdit: () -> Void = {}
    var onView: () -> Void = {}

    @Environment(\.accessibilityReduceMotion) private var reduceMotion

    var body: some View {
        VStack(alignment: .leading, spacing: PBSpace.s12) {
            HStack(spacing: PBSpace.s12) {
                PBAvatar(.icon(icon), isOnCard: true)
                Text(title)
                    .textStyle(.headline)
                    .foregroundStyle(PBColor.textPrimary)
                    .lineLimit(1)
                    .frame(maxWidth: .infinity, alignment: .leading)
                Text(amount)
                    .textStyle(.amountMedium)
                    .foregroundStyle(PBColor.textPrimary)
            }
            PBDivider()
            VStack(alignment: .leading, spacing: PBSpace.s8) {
                Text(paidLine)
                    .textStyle(.subheadline)
                    .foregroundStyle(PBColor.textSecondary)
                HStack(spacing: PBSpace.s8) {
                    Text(splitLine)
                        .textStyle(.subheadline)
                        .foregroundStyle(PBColor.textSecondary)
                        .frame(maxWidth: .infinity, alignment: .leading)
                    PBAvatarStack(heads: Array(members.prefix(4)))
                        .accessibilityHidden(true)
                }
                Text(eachLine)
                    .textStyle(.subheadline)
                    .foregroundStyle(PBColor.textPrimary)
            }
            .accessibilityElement(children: .combine)
            ZStack {
                if isSaved {
                    saved.transition(.opacity)
                } else {
                    actions.transition(.opacity)
                }
            }
        }
        .padding(PBLayout.cardPadding)
        .background(PBColor.bgCard, in: .rect(cornerRadius: PBRadius.card))
        .animation(reduceMotion ? nil : .easeOut(duration: 0.25), value: isSaved)
    }

    private var actions: some View {
        HStack(spacing: PBSpace.s8) {
            PBButton("Save", size: .small, fillsWidth: true, action: onSave)
                .accessibilityIdentifier(testID("save"))
            PBButton("Edit", style: .onCard, size: .small, action: onEdit)
                .accessibilityIdentifier(testID("edit"))
        }
    }

    private var saved: some View {
        HStack(spacing: PBSpace.s8) {
            PBIconView(.checkCircle, size: PBSize.iconMd)
                .foregroundStyle(PBColor.iconPrimary)
            Text("Expense added")
                .textStyle(.headline)
                .foregroundStyle(PBColor.textPrimary)
                .lineLimit(1)
                .frame(maxWidth: .infinity, alignment: .leading)
            PBTextButton("View", action: onView)
                .accessibilityIdentifier(testID("view"))
        }
    }

    private func testID(_ element: String) -> String {
        testIDPrefix.map { "\($0).\(element)" } ?? ""
    }
}

#Preview("PBDraftExpenseCard") {
    @Previewable @State var isSaved = false
    VStack(spacing: PBSpace.s16) {
        PBDraftExpenseCard(
            icon: .car, title: "Cab", amount: "₹600", paidLine: "Paid by you · Today",
            splitLine: "Split equally with Esha and Dev", members: [.arjun, .esha, .dev],
            eachLine: "₹200 each", isSaved: isSaved, onSave: { isSaved = true }
        )
        PBButton("Reset", style: .secondary, size: .small) { isSaved = false }
    }
    .padding(PBLayout.screenMargin)
}
