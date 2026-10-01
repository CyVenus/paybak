import SwiftUI

/// Navigation / Tab Bar (Figma 17:618, components-home §5): the floating Liquid Glass capsule with
/// Home · Groups · ＋ · Activity · Profile. 62 tall, 20 from the sides and 21 above the bottom edge;
/// content scrolls under it. Test ids: `home.tab.<home|groups|add|activity|profile>`.
struct PBTabBar: View {
    let selection: Tab
    let onSelect: (Tab) -> Void
    let onAdd: () -> Void

    var body: some View {
        HStack(spacing: 0) {
            item(.home)
            Spacer(minLength: 0)
            item(.groups)
            Spacer(minLength: 0)
            PBAddButton(action: onAdd)
                .accessibilityIdentifier("home.tab.add")
            Spacer(minLength: 0)
            item(.activity)
            Spacer(minLength: 0)
            item(.profile)
        }
        // 5 pt padding plus the 1 pt inside hairline.
        .padding(PBSpace.s6)
        .frame(height: PBSize.tabbar)
        .overlay(Capsule().strokeBorder(PBColor.borderGlassHighlight, lineWidth: 1))
        .pbMaterial(.glass, in: .capsule)
    }

    private func item(_ tab: Tab) -> some View {
        PBTabItem(tab: tab, isActive: tab == selection) { onSelect(tab) }
            .accessibilityIdentifier("home.tab.\(tab.rawValue)")
    }
}

/// Navigation / Tab Bar Item (Figma 17:504): 68 × 52 capsule, 24 pt icon over a Caption/2 label.
/// Active = black on the 6 % pill; inactive = gray.
struct PBTabItem: View {
    let tab: Tab
    let isActive: Bool
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            VStack(spacing: PBSpace.s2) {
                PBIconView(tab.icon)
                    .foregroundStyle(isActive ? PBColor.iconPrimary : PBColor.iconSecondary)
                Text(tab.title)
                    .textStyle(.caption2)
                    .foregroundStyle(isActive ? PBColor.textPrimary : PBColor.textSecondary)
                    .lineLimit(1)
                    .fixedSize()
            }
            .frame(width: 68, height: 52)
            .background(isActive ? PBColor.bgSelected : .clear, in: .capsule)
            .animation(.easeOut(duration: 0.15), value: isActive)
            .contentShape(.capsule)
        }
        .buttonStyle(PBTabItemStyle())
        .accessibilityElement(children: .ignore)
        .accessibilityLabel(tab.title)
        .accessibilityAddTraits(isActive ? [.isButton, .isSelected] : .isButton)
    }
}

/// Figma has no pressed state; a light dip in opacity acknowledges the tap.
private struct PBTabItemStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .opacity(configuration.isPressed ? 0.6 : 1)
    }
}

extension Tab {
    var title: String {
        switch self {
        case .home: "Home"
        case .groups: "Groups"
        case .activity: "Activity"
        case .profile: "Profile"
        }
    }

    var icon: PBIcon {
        switch self {
        case .home: .home
        case .groups: .groups
        case .activity: .activity
        case .profile: .profile
        }
    }
}

extension PBTabBar {
    /// Space tab-root content leaves at the bottom so its end scrolls clear of the bar: 62 + 21 + 24.
    static let contentInset: CGFloat = 107
    /// The bar's distance from the bottom edge of the screen.
    static let bottomOffset: CGFloat = 21
}

#Preview("PBTabBar") {
    @Previewable @State var selection = Tab.home
    ZStack(alignment: .bottom) {
        LinearGradient(colors: [PBColor.bgCard, PBColor.bgInverse], startPoint: .top, endPoint: .bottom)
            .ignoresSafeArea()
        PBTabBar(selection: selection, onSelect: { selection = $0 }, onAdd: {})
            .padding(.horizontal, PBLayout.screenMargin)
    }
}
