import SwiftUI

/// Navigation / Push Header (Figma 97:1082): the 44 pt header of a pushed screen. A glass back
/// button, a centred Headline title (200 pt box) and a trailing action: a glass text capsule
/// ("Save"), a glass icon button (Settings) or nothing. `.wideText` is for long actions ("Mark all
/// read"): the capsule caps at 122 pt with 12 pt padding and the title box shrinks to 102 pt, so
/// they never touch. Place it at the screen margins from the top safe area; on long scrolling
/// screens pin it and give it a `bg/primary` fill. Test ids: `<prefix>.back`, `<prefix>.action`.
struct PBPushHeader: View {
    enum Trailing {
        case none
        case text(String, action: () -> Void)
        case wideText(String, action: () -> Void)
        case icon(PBIcon, accessibilityLabel: String, action: () -> Void)
    }

    var title: String?
    var trailing: Trailing = .none
    var testIDPrefix: String?
    let onBack: () -> Void

    init(_ title: String? = nil, trailing: Trailing = .none, testIDPrefix: String? = nil, onBack: @escaping () -> Void) {
        self.title = title
        self.trailing = trailing
        self.testIDPrefix = testIDPrefix
        self.onBack = onBack
    }

    var body: some View {
        HStack(spacing: 0) {
            PBIconButton(.chevronLeft, accessibilityLabel: "Back", style: .glass, action: onBack)
                .accessibilityIdentifier(testID("back"))
            Spacer(minLength: 0)
            trailingView
                .accessibilityIdentifier(testID("action"))
        }
        .frame(height: PBSize.tap)
        .overlay {
            if let title {
                Text(title)
                    .textStyle(.headline)
                    .foregroundStyle(PBColor.textPrimary)
                    .multilineTextAlignment(.center)
                    .lineLimit(1)
                    .frame(width: isWide ? 102 : 200)
                    .accessibilityAddTraits(.isHeader)
            }
        }
    }

    private var isWide: Bool {
        if case .wideText = trailing { return true }
        return false
    }

    @ViewBuilder
    private var trailingView: some View {
        switch trailing {
        case .none:
            EmptyView()
        case .text(let label, let action):
            PBGlassTextButton(label, action: action)
        case .wideText(let label, let action):
            PBGlassTextButton(label, horizontalPadding: PBSpace.s12, maxWidth: 122, action: action)
        case .icon(let icon, let label, let action):
            PBIconButton(icon, accessibilityLabel: label, style: .glass, action: action)
        }
    }

    private func testID(_ element: String) -> String {
        testIDPrefix.map { "\($0).\(element)" } ?? ""
    }
}

/// The glass text capsule of Push Header: 44 pt tall, at least 44 wide, Headline label.
struct PBGlassTextButton: View {
    let label: String
    var horizontalPadding: CGFloat = PBSpace.s16
    var maxWidth: CGFloat?
    let action: () -> Void

    @Environment(\.isEnabled) private var isEnabled

    init(_ label: String, horizontalPadding: CGFloat = PBSpace.s16, maxWidth: CGFloat? = nil, action: @escaping () -> Void) {
        self.label = label
        self.horizontalPadding = horizontalPadding
        self.maxWidth = maxWidth
        self.action = action
    }

    var body: some View {
        Button(action: action) {
            Text(label)
                .textStyle(.headline)
                .foregroundStyle(isEnabled ? PBColor.textPrimary : PBColor.textDisabled)
                .lineLimit(1)
                .padding(.horizontal, horizontalPadding)
                .frame(minWidth: PBSize.tap, maxWidth: maxWidth)
                .frame(height: PBSize.tap)
                .contentShape(.capsule)
                .pbMaterial(.glassSmall, in: .capsule, isInteractive: true)
        }
        .buttonStyle(.plain)
    }
}

#Preview("PBPushHeader") {
    VStack(spacing: PBSpace.s24) {
        PBPushHeader("Edit avatar", trailing: .text("Save") {}) {}
        PBPushHeader("Edit avatar", trailing: .icon(.settings, accessibilityLabel: "Settings") {}) {}
        PBPushHeader("Edit avatar") {}
        PBPushHeader("Notifications", trailing: .wideText("Mark all read") {}) {}
    }
    .padding(PBLayout.screenMargin)
}
