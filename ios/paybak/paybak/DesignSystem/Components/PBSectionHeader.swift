import SwiftUI

/// Row / Section Header (Figma 13:223): a Title/3 section title in a 32 pt row, with an optional
/// "See all" text button on the right (its 44 pt tap target overhangs the row by 6 pt each way).
struct PBSectionHeader: View {
    let title: String
    var actionTitle = "See all"
    var action: (() -> Void)?

    init(_ title: String, actionTitle: String = "See all", action: (() -> Void)? = nil) {
        self.title = title
        self.actionTitle = actionTitle
        self.action = action
    }

    var body: some View {
        HStack(spacing: PBSpace.s12) {
            Text(title)
                .textStyle(.title3)
                .foregroundStyle(PBColor.textPrimary)
                .lineLimit(1)
                .accessibilityAddTraits(.isHeader)
            Spacer(minLength: 0)
            if let action {
                PBTextButton(actionTitle, style: .secondary, action: action)
                    .frame(height: 32)
            }
        }
        .frame(height: 32)
    }
}

#Preview("PBSectionHeader") {
    VStack(spacing: PBSpace.s16) {
        PBSectionHeader("Suggested")
        PBSectionHeader("Recent activity") {}
    }
    .padding(PBLayout.screenMargin)
}
