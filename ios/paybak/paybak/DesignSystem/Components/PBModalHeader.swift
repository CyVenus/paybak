import SwiftUI

/// Navigation / Modal Header (Figma 115:886): the toolbar of full-screen modals (Add expense, Record
/// payment, New group, Ask Paybak, the paywall). The kit glass ✕ on the left, a centred Headline
/// title (200 pt box, truncates) and the confirmation pill on the right, a small primary button
/// ("Save", "Create", "Add") that stays disabled until the form is valid. Without `actionLabel` there
/// is no pill. 44 pt tall at the screen margins from the top safe area; no fill, no shadow.
/// Test ids: `<prefix>.close`, `<prefix>.action`.
struct PBModalHeader: View {
    var title: String?
    var actionLabel: String?
    var isActionEnabled = true
    var testIDPrefix: String?
    let onClose: () -> Void
    var onAction: () -> Void = {}

    init(
        _ title: String? = nil,
        actionLabel: String? = nil,
        isActionEnabled: Bool = true,
        testIDPrefix: String? = nil,
        onClose: @escaping () -> Void,
        onAction: @escaping () -> Void = {}
    ) {
        self.title = title
        self.actionLabel = actionLabel
        self.isActionEnabled = isActionEnabled
        self.testIDPrefix = testIDPrefix
        self.onClose = onClose
        self.onAction = onAction
    }

    var body: some View {
        HStack(spacing: 0) {
            PBGlassCloseButton(action: onClose)
                .accessibilityIdentifier(testID("close"))
            Spacer(minLength: 0)
            if let actionLabel {
                PBButton(actionLabel, size: .small, action: onAction)
                    .disabled(!isActionEnabled)
                    .accessibilityIdentifier(testID("action"))
            }
        }
        .frame(height: PBSize.tap)
        .overlay {
            if let title {
                Text(title)
                    .textStyle(.headline)
                    .foregroundStyle(PBColor.textPrimary)
                    .multilineTextAlignment(.center)
                    .lineLimit(1)
                    .frame(width: 200)
                    .accessibilityAddTraits(.isHeader)
            }
        }
    }

    private func testID(_ element: String) -> String {
        testIDPrefix.map { "\($0).\(element)" } ?? ""
    }
}

/// The kit's Liquid Glass ✕ ("Button - Liquid Glass - Symbol"): the SF Symbol xmark in a glass
/// circle, 44 pt on modal headers and 50 pt on sheets.
struct PBGlassCloseButton: View {
    var diameter: CGFloat = PBSize.tap
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Image(systemName: "xmark")
                .font(.system(size: 25, weight: .medium))
                .foregroundStyle(PBColor.iconPrimary)
                .frame(width: diameter, height: diameter)
                .contentShape(.circle)
                .pbMaterial(.glassSmall, in: .circle, isInteractive: true)
        }
        .buttonStyle(.plain)
        .accessibilityLabel("Close")
    }
}

#Preview("PBModalHeader") {
    VStack(spacing: PBSpace.s24) {
        PBModalHeader("Add expense", actionLabel: "Save", onClose: {})
        PBModalHeader("Add expense", actionLabel: "Save", isActionEnabled: false, onClose: {})
        PBModalHeader("Add expense", onClose: {})
        PBGlassCloseButton(diameter: 50) {}
    }
    .padding(PBLayout.screenMargin)
}
