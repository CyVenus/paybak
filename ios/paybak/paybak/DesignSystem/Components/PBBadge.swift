import SwiftUI

/// Badge / Pill (Figma 11:46): a 24 pt status pill in Caption/1 with an optional 14 pt icon.
/// Overdue (red) is reserved for overdue items. Not interactive.
struct PBBadge: View {
    enum Style {
        /// `bg/card` pill on white.
        case muted
        /// White pill on #F5F5F5 cards.
        case onCard
        case inverse
        /// The only coloured badge.
        case overdue
    }

    let title: String
    var style: Style = .muted
    var icon: PBIcon?

    init(_ title: String, style: Style = .muted, icon: PBIcon? = nil) {
        self.title = title
        self.style = style
        self.icon = icon
    }

    var body: some View {
        HStack(spacing: PBSpace.s4) {
            if let icon {
                PBIconView(icon, size: 14)
            }
            Text(title)
                .textStyle(.caption1)
                .lineLimit(1)
        }
        .foregroundStyle(style.content)
        .padding(.horizontal, 10)
        .frame(height: 24)
        .background(style.fill, in: .capsule)
    }
}

private extension PBBadge.Style {
    var fill: Color {
        switch self {
        case .muted: PBColor.bgCard
        case .onCard: PBColor.bgPrimary
        case .inverse: PBColor.bgInverse
        case .overdue: PBColor.bgDestructive
        }
    }

    /// Label and icon share the colour (`text/secondary` + `icon/secondary`, and so on).
    var content: Color {
        switch self {
        case .muted, .onCard: PBColor.textSecondary
        case .inverse, .overdue: PBColor.textInverse
        }
    }
}

#Preview("PBBadge") {
    VStack(spacing: PBSpace.s12) {
        HStack {
            PBBadge("Due Fri")
            PBBadge("Due Fri", icon: .calendar)
            PBBadge("Settled", style: .inverse)
            PBBadge("Overdue", style: .overdue)
        }
        PBBadge("Due Fri", style: .onCard, icon: .calendar)
            .padding(PBSpace.s16)
            .background(PBColor.bgCard, in: .rect(cornerRadius: PBRadius.card))
    }
}
