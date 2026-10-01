import SwiftUI

/// Pill buttons: Button / Primary, Secondary, On Card and Destructive (Figma 9:36, 9:62, 9:88, 9:114).
/// Large is 52 pt (one Large primary per screen); Small is 36 pt, for rows and cards.
struct PBButton: View {
    enum Style {
        /// Black pill, the main action.
        case primary
        /// #F5F5F5 pill on white backgrounds.
        case secondary
        /// White pill on #F5F5F5 cards.
        case onCard
        /// Red pill, for delete and sign out only.
        case destructive
    }

    enum Size {
        case large
        case small
    }

    let title: String
    var style: Style = .primary
    var size: Size = .large
    /// Optional leading icon, tinted like the label (the Google G keeps its colours).
    var icon: PBIcon?
    /// Stretches the pill to the available width with the label centred (e.g. the 362 pt CTAs).
    var fillsWidth = false
    let action: () -> Void

    init(
        _ title: String,
        style: Style = .primary,
        size: Size = .large,
        icon: PBIcon? = nil,
        fillsWidth: Bool = false,
        action: @escaping () -> Void
    ) {
        self.title = title
        self.style = style
        self.size = size
        self.icon = icon
        self.fillsWidth = fillsWidth
        self.action = action
    }

    var body: some View {
        Button(action: action) {
            HStack(spacing: size.iconGap) {
                if let icon {
                    PBIconView(icon, size: size.iconSize)
                }
                Text(title)
                    .textStyle(size.textStyle)
                    .lineLimit(1)
                    // A label too long for its pill (an alert's half-width "Close project") shrinks,
                    // down to 11 pt, before it clips.
                    .minimumScaleFactor(Self.minimumLabelSize / size.textStyle.size)
                    // A new label ("Continue" → "Get started") crossfades in place.
                    .contentTransition(.opacity)
            }
            .animation(.easeInOut(duration: 0.2), value: title)
        }
        .buttonStyle(PBPillButtonStyle(style: style, size: size, fillsWidth: fillsWidth))
    }

    private static let minimumLabelSize: CGFloat = 11
}

/// The pill shape, fills and label colours per style and state. Pressed changes only the fill.
private struct PBPillButtonStyle: ButtonStyle {
    let style: PBButton.Style
    let size: PBButton.Size
    let fillsWidth: Bool

    @Environment(\.isEnabled) private var isEnabled
    @Environment(\.pbPreviewInteraction) private var previewInteraction

    func makeBody(configuration: Configuration) -> some View {
        let isPressed = configuration.isPressed || previewInteraction == .pressed
        let colors = style.colors(isPressed: isPressed, isEnabled: isEnabled)
        configuration.label
            .foregroundStyle(colors.label)
            .padding(.horizontal, size.horizontalPadding)
            .frame(maxWidth: fillsWidth ? .infinity : nil)
            .frame(height: size.height)
            .background(colors.fill, in: .capsule)
            .animation(.easeOut(duration: 0.1), value: isPressed)
            // Small buttons keep a 44 pt tall hit area (size/tap) without changing their layout.
            .padding(.vertical, size.hitSlop)
            .contentShape(.capsule)
            .padding(.vertical, -size.hitSlop)
    }
}

private extension PBButton.Size {
    var height: CGFloat { self == .large ? PBSize.buttonLg : PBSize.buttonSm }
    var horizontalPadding: CGFloat { self == .large ? PBSpace.s24 : PBSpace.s16 }
    var iconGap: CGFloat { self == .large ? PBSpace.s8 : PBSpace.s6 }
    var iconSize: CGFloat { self == .large ? PBSize.iconMd : PBSize.iconSm }
    var textStyle: PBTextStyle { self == .large ? .buttonLarge : .buttonSmall }
    var hitSlop: CGFloat { max(0, (PBSize.tap - height) / 2) }
}

private extension PBButton.Style {
    func colors(isPressed: Bool, isEnabled: Bool) -> (fill: Color, label: Color) {
        switch (self, isEnabled) {
        case (.primary, true): (isPressed ? PBColor.bgInversePressed : PBColor.bgInverse, PBColor.textInverse)
        case (.primary, false): (PBColor.bgDisabled, PBColor.textDisabled)
        case (.secondary, true): (isPressed ? PBColor.bgCardPressed : PBColor.bgCard, PBColor.textPrimary)
        case (.secondary, false): (PBColor.bgCard, PBColor.textDisabled)
        case (.onCard, true): (isPressed ? PBColor.bgCardPressed : PBColor.bgPrimary, PBColor.textPrimary)
        case (.onCard, false): (PBColor.bgPrimary, PBColor.textDisabled)
        case (.destructive, true): (isPressed ? PBColor.bgDestructivePressed : PBColor.bgDestructive, PBColor.textInverse)
        case (.destructive, false): (PBColor.bgDisabled, PBColor.textDisabled)
        }
    }
}

#Preview("PBButton") {
    VStack(spacing: PBSpace.s12) {
        PBButton("Continue", fillsWidth: true) {}
        PBButton("Continue with Apple", icon: .apple, fillsWidth: true) {}
        PBButton("Continue with Google", style: .secondary, icon: .google, fillsWidth: true) {}
        HStack {
            PBButton("Continue", size: .small) {}
            PBButton("Continue", size: .small) {}.pbPreviewInteraction(.pressed)
            PBButton("Continue", size: .small) {}.disabled(true)
        }
        HStack {
            PBButton("Delete", style: .destructive) {}
            PBButton("Add", style: .onCard, size: .small, icon: .plus) {}
                .padding(PBSpace.s16)
                .background(PBColor.bgCard, in: .rect(cornerRadius: PBRadius.card))
        }
    }
    .padding(PBLayout.screenMargin)
}
