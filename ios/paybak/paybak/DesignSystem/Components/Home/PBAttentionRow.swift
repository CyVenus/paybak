import SwiftUI

/// Row / Attention (Figma 13:379, components-home §9, components-app §8.2): a Home "Due soon" item on a
/// #F5F5F5 card, 88 tall. The person's art (or a group icon) in a white 40 pt circle; the title with
/// its detail, which truncates so the row keeps its height; a status pill (red only when overdue);
/// then the amount over a small white action ("Remind" for money owed to you, "Settle" for money you
/// owe). Tapping the row outside the action opens what it's about. Settle up drops the action on a
/// payment waiting for its receiver, and the pill when there's no due date.
/// Test ids: `<testID>` for the row, `actionTestID` (default `<testID>.action`) for the button.
struct PBAttentionRow: View {
    let avatar: PBAvatar.Content
    let title: String
    let detail: String
    let amount: String
    let badge: String?
    var isOverdue = false
    /// Nil: no action (the amount keeps its place).
    let actionTitle: String?
    var testID: String?
    var actionTestID: String?
    let onAction: () -> Void
    var onTap: () -> Void = {}

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
                    // Reserves the action's place; the live button sits on top (a button can't
                    // live inside another button's label).
                    if let actionTitle {
                        actionButton(actionTitle)
                            .hidden()
                    } else {
                        Color.clear.frame(width: 0, height: PBSize.buttonSm)
                    }
                }
                .fixedSize()
            }
            .padding(.vertical, PBSpace.s12)
            .padding(.horizontal, PBLayout.cardPadding)
        }
        .buttonStyle(PBAttentionRowStyle())
        .accessibilityElement(children: .combine)
        .accessibilityIdentifier(testID ?? "")
        .overlay(alignment: .bottomTrailing) {
            if let actionTitle {
                actionButton(actionTitle)
                    .accessibilityIdentifier(actionTestID ?? testID.map { "\($0).action" } ?? "")
                    .accessibilityLabel("\(actionTitle) \(title)")
                    .padding(.bottom, PBSpace.s12)
                    .padding(.trailing, PBLayout.cardPadding)
            }
        }
    }

    private func actionButton(_ title: String) -> some View {
        PBButton(title, style: .onCard, size: .small, action: onAction)
    }
}

/// The card fill, `bg/card-pressed` while the row is pressed.
private struct PBAttentionRowStyle: ButtonStyle {
    @Environment(\.pbPreviewInteraction) private var previewInteraction

    func makeBody(configuration: Configuration) -> some View {
        let isPressed = configuration.isPressed || previewInteraction == .pressed
        configuration.label
            .background(isPressed ? PBColor.bgCardPressed : PBColor.bgCard, in: .rect(cornerRadius: PBRadius.card))
            .contentShape(.rect(cornerRadius: PBRadius.card))
            .animation(.easeOut(duration: 0.1), value: isPressed)
    }
}

#Preview("PBAttentionRow") {
    VStack(spacing: PBSpace.s8) {
        PBAttentionRow(avatar: .art(.rohan), title: "Rohan", detail: "Movie tickets", amount: "₹800",
                       badge: "Overdue 3 days", isOverdue: true, actionTitle: "Remind", onAction: {})
        PBAttentionRow(avatar: .icon(.groups), title: "Goa Trip", detail: "Your share", amount: "₹1,400",
                       badge: "Due Fri", actionTitle: "Settle", onAction: {})
        PBAttentionRow(avatar: .art(.dev), title: "Dev", detail: "Groceries for the whole weekend trip", amount: "₹12,700",
                       badge: "Due tomorrow", actionTitle: "Remind", onAction: {})
            .pbPreviewInteraction(.pressed)
        PBAttentionRow(avatar: .art(.kabir), title: "Kabir", detail: "Goa Trip", amount: "₹1,400",
                       badge: "Pending", actionTitle: nil, onAction: {})
    }
    .padding(PBLayout.screenMargin)
}
