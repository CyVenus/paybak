import SwiftUI

/// Button / Text (Figma 10:45): a text-only action ("See all", "Skip", "Continue with email or phone").
/// 44 pt tall, width hugs the label; pressed = the whole button at 50 % opacity.
struct PBTextButton: View {
    enum Style {
        case primary
        case secondary
        case destructive
    }

    let title: String
    var style: Style = .primary
    var showsChevron = false
    let action: () -> Void

    init(_ title: String, style: Style = .primary, showsChevron: Bool = false, action: @escaping () -> Void) {
        self.title = title
        self.style = style
        self.showsChevron = showsChevron
        self.action = action
    }

    var body: some View {
        Button(action: action) {
            HStack(spacing: PBSpace.s2) {
                Text(title)
                    .textStyle(.buttonSmall)
                    .lineLimit(1)
                if showsChevron {
                    PBIconView(.chevronRight, size: PBSize.iconSm)
                }
            }
        }
        .buttonStyle(PBTextButtonStyle(style: style))
    }
}

private struct PBTextButtonStyle: ButtonStyle {
    let style: PBTextButton.Style

    @Environment(\.isEnabled) private var isEnabled
    @Environment(\.pbPreviewInteraction) private var previewInteraction

    /// Widens the tap target past short labels ("Skip" is 31 pt wide) without moving the label.
    private let horizontalHitSlop: CGFloat = PBSpace.s8

    func makeBody(configuration: Configuration) -> some View {
        let isPressed = configuration.isPressed || previewInteraction == .pressed
        configuration.label
            .foregroundStyle(isEnabled ? style.color : PBColor.textDisabled)
            .frame(height: PBSize.tap)
            .opacity(isPressed ? 0.5 : 1)
            .animation(.easeOut(duration: 0.1), value: isPressed)
            .padding(.horizontal, horizontalHitSlop)
            .contentShape(.rect)
            .padding(.horizontal, -horizontalHitSlop)
    }
}

private extension PBTextButton.Style {
    var color: Color {
        switch self {
        case .primary: PBColor.textPrimary
        case .secondary: PBColor.textSecondary
        case .destructive: PBColor.textDestructive
        }
    }
}

#Preview("PBTextButton") {
    VStack(alignment: .leading) {
        PBTextButton("Continue with email or phone") {}
        PBTextButton("Skip", style: .secondary) {}
        PBTextButton("See all", showsChevron: true) {}
        PBTextButton("See all", showsChevron: true) {}.pbPreviewInteraction(.pressed)
        PBTextButton("Delete", style: .destructive, showsChevron: true) {}
        PBTextButton("See all", showsChevron: true) {}.disabled(true)
    }
    .padding(PBLayout.screenMargin)
}
