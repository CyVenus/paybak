import SwiftUI

/// A Settle up plan row (settle §3.2): Row / Attention with a person's art. Their name and what it's
/// for on one line (the detail truncates), the due pill under it (red only when overdue, "Pending"
/// while your payment waits), then the amount over its small white action. A pending payment has no
/// action. Tapping the row outside the action opens the person, or the pending payment.
/// Test ids: `testID` for the row, the action's own id for its button.
struct SettlePlanRow: View {
    struct Action {
        let title: String
        let testID: String
        let perform: () -> Void
    }

    let avatar: PBAvatar.Content
    let title: String
    let detail: String
    let amount: String
    var badge: String?
    var isOverdue = false
    var action: Action?
    var testID: String?
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            HStack(spacing: PBSpace.s12) {
                PBAvatar(avatar, diameter: PBSize.avatarMd, isOnCard: true)
                VStack(alignment: .leading, spacing: PBSpace.s6) {
                    HStack(spacing: PBSpace.s6) {
                        Text(title)
                            .textStyle(.headline)
                            .foregroundStyle(PBColor.textPrimary)
                            .layoutPriority(1)
                        Text(detail)
                            .textStyle(.subheadline)
                            .foregroundStyle(PBColor.textSecondary)
                    }
                    .lineLimit(1)
                    if let badge {
                        PBBadge(badge, style: isOverdue ? .overdue : .onCard)
                    }
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                VStack(alignment: .trailing, spacing: PBSpace.s6) {
                    Text(amount)
                        .textStyle(.amountMedium)
                        .foregroundStyle(PBColor.textPrimary)
                        .lineLimit(1)
                    // Keeps the action's place, so the amount stays at the top even without one; the
                    // live button sits on top (a button can't live inside another button's label).
                    if let action {
                        actionButton(action).hidden()
                    } else {
                        Color.clear.frame(width: 0, height: PBSize.buttonSm)
                    }
                }
                .fixedSize()
            }
            .padding(.vertical, PBSpace.s12)
            .padding(.horizontal, PBLayout.cardPadding)
            .frame(minHeight: 88)
        }
        .buttonStyle(PBRowButtonStyle(surface: .card))
        .background(PBColor.bgCard)
        .clipShape(.rect(cornerRadius: PBRadius.card))
        .accessibilityElement(children: .combine)
        .accessibilityIdentifier(testID ?? "")
        .overlay(alignment: .bottomTrailing) {
            if let action {
                actionButton(action)
                    .accessibilityIdentifier(action.testID)
                    .accessibilityLabel("\(action.title) \(title)")
                    .padding(.bottom, PBSpace.s12)
                    .padding(.trailing, PBLayout.cardPadding)
            }
        }
    }

    private func actionButton(_ action: Action) -> some View {
        PBButton(action.title, style: .onCard, size: .small, action: action.perform)
    }
}

#Preview("SettlePlanRow") {
    VStack(spacing: PBSpace.s12) {
        SettlePlanRow(avatar: .art(.kabir), title: "Kabir", detail: "Goa Trip", amount: "₹1,400", badge: "Due Fri",
                      action: .init(title: "Settle", testID: "settle") {}) {}
        SettlePlanRow(avatar: .art(.kabir), title: "Kabir", detail: "Goa Trip", amount: "₹1,400", badge: "Pending") {}
        SettlePlanRow(avatar: .art(.rohan), title: "Rohan", detail: "Movie tickets", amount: "₹800", badge: "Overdue 3 days", isOverdue: true,
                      action: .init(title: "Remind", testID: "remind") {}) {}
        SettlePlanRow(avatar: .art(.priya), title: "Priya", detail: "Dinner at Olive Garden", amount: "₹700", badge: "Due Sun",
                      action: .init(title: "Remind", testID: "remind") {}) {}
            .pbPreviewInteraction(.pressed)
    }
    .padding(PBLayout.screenMargin)
}
