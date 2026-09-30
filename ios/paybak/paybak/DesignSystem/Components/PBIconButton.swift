import SwiftUI

/// Button / Icon (Figma 10:79): a circular 44 pt icon button with a 24 pt icon and an optional
/// unread badge. Glass is Liquid Glass for floating toolbar buttons (the Home bell). Some components
/// resize it (the composer's 36 pt send button); the icon stays 24 pt.
struct PBIconButton: View {
    enum Style {
        /// No fill; pressed fills the circle with `bg/selected`.
        case plain
        /// `bg/card` circle.
        case filled
        /// Liquid Glass (Material/Glass Small); the system supplies the pressed response.
        case glass
        /// Black circle, white icon.
        case inverse
    }

    let icon: PBIcon
    /// Spoken by VoiceOver, e.g. "Back" or "Notifications".
    let accessibilityLabel: String
    var style: Style = .plain
    var showsBadge = false
    var diameter: CGFloat = PBSize.tap
    let action: () -> Void

    init(
        _ icon: PBIcon,
        accessibilityLabel: String,
        style: Style = .plain,
        showsBadge: Bool = false,
        diameter: CGFloat = PBSize.tap,
        action: @escaping () -> Void
    ) {
        self.icon = icon
        self.accessibilityLabel = accessibilityLabel
        self.style = style
        self.showsBadge = showsBadge
        self.diameter = diameter
        self.action = action
    }

    var body: some View {
        Button(action: action) {
            PBIconView(icon)
                .frame(width: diameter, height: diameter)
                .overlay(alignment: .topLeading) {
                    if showsBadge {
                        badge
                    }
                }
        }
        .buttonStyle(PBIconButtonStyle(style: style))
        .accessibilityLabel(accessibilityLabel)
    }

    /// A 10 pt dot at (26, 9) with a 2 pt outside ring, so the visible dot spans (24, 7)–(38, 21).
    private var badge: some View {
        Circle()
            .fill(style == .inverse ? PBColor.bgPrimary : PBColor.bgInverse)
            .frame(width: 10, height: 10)
            .padding(2)
            .background(style == .inverse ? PBColor.bgInverse : PBColor.bgPrimary, in: .circle)
            .offset(x: 24, y: 7)
            .accessibilityHidden(true)
    }
}

private struct PBIconButtonStyle: ButtonStyle {
    let style: PBIconButton.Style

    @Environment(\.pbPreviewInteraction) private var previewInteraction

    func makeBody(configuration: Configuration) -> some View {
        let isPressed = configuration.isPressed || previewInteraction == .pressed
        let label = configuration.label
            .foregroundStyle(style == .inverse ? PBColor.iconInverse : PBColor.iconPrimary)
            .contentShape(.circle)
        switch style {
        case .glass:
            label.pbMaterial(.glassSmall, in: .circle, isInteractive: true)
        case .plain, .filled, .inverse:
            label
                .background(fill(isPressed: isPressed), in: .circle)
                .animation(.easeOut(duration: 0.1), value: isPressed)
        }
    }

    private func fill(isPressed: Bool) -> Color {
        switch style {
        case .plain: isPressed ? PBColor.bgSelected : .clear
        case .filled: isPressed ? PBColor.bgCardPressed : PBColor.bgCard
        case .inverse: isPressed ? PBColor.bgInversePressed : PBColor.bgInverse
        case .glass: .clear
        }
    }
}

#Preview("PBIconButton") {
    HStack(spacing: PBSpace.s16) {
        PBIconButton(.chevronLeft, accessibilityLabel: "Back") {}
        PBIconButton(.chevronLeft, accessibilityLabel: "Back") {}.pbPreviewInteraction(.pressed)
        PBIconButton(.bell, accessibilityLabel: "Notifications", style: .filled, showsBadge: true) {}
        PBIconButton(.bell, accessibilityLabel: "Notifications", style: .glass, showsBadge: true) {}
        PBIconButton(.bell, accessibilityLabel: "Notifications", style: .inverse, showsBadge: true) {}
    }
    .padding(PBLayout.screenMargin)
}
