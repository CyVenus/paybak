import SwiftUI

/// Card / Empty State (Figma 13:541, components-home §11): a #F5F5F5 card with a 240 × 180 Rive
/// illustration, a centred Title/2 title and body, and up to two actions (First day). All settled is
/// the fixed calm message with the AllSquare character and no actions. Reused by Activity empty,
/// Groups empty and Group created with their own copy.
struct PBEmptyState: View {
    struct Action {
        let title: String
        let icon: PBIcon
        var testID: String?
        let perform: () -> Void
    }

    var illustration: PaybakRiveAsset = .homeFirstDay
    var title = "Nothing here yet."
    var message = "Add your first expense or invite a friend to get started."
    /// Black, full width.
    var primary: Action?
    /// White (On Card), full width.
    var secondary: Action?

    /// Home — All settled: "You’re all square."
    static let allSettled = PBEmptyState(illustration: .homeAllSquare, title: "You’re all square.",
                                         message: "No one owes anyone right now.")

    var body: some View {
        VStack(spacing: PBSpace.s20) {
            PaybakRiveIllustration(illustration)
                .frame(width: 240, height: 180)
            VStack(spacing: PBSpace.s8) {
                Text(title)
                    .textStyle(.title2)
                    .foregroundStyle(PBColor.textPrimary)
                    .accessibilityAddTraits(.isHeader)
                Text(message)
                    .textStyle(.body)
                    .foregroundStyle(PBColor.textSecondary)
            }
            .multilineTextAlignment(.center)
            .frame(maxWidth: .infinity)
            if primary != nil || secondary != nil {
                VStack(spacing: PBSpace.s12) {
                    if let primary {
                        PBButton(primary.title, icon: primary.icon, fillsWidth: true, action: primary.perform)
                            .accessibilityIdentifier(primary.testID ?? "")
                    }
                    if let secondary {
                        PBButton(secondary.title, style: .onCard, icon: secondary.icon, fillsWidth: true, action: secondary.perform)
                            .accessibilityIdentifier(secondary.testID ?? "")
                    }
                }
            }
        }
        .padding(PBSpace.s24)
        .frame(maxWidth: .infinity)
        .background(PBColor.bgCard, in: .rect(cornerRadius: PBRadius.card))
    }
}

#Preview("PBEmptyState") {
    ScrollView {
        VStack(spacing: PBSpace.s16) {
            PBEmptyState(primary: .init(title: "Add expense", icon: .plus) {},
                         secondary: .init(title: "Invite friends", icon: .userAdd) {})
            PBEmptyState.allSettled
        }
        .padding(PBLayout.screenMargin)
    }
}
