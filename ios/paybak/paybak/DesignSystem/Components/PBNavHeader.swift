import SwiftUI

/// Navigation / Nav Header, Type=Large Title (Figma 17:486, components-home §1): a tab root's Title/1
/// title with an optional glass action on the right (bell, plus, user-add or restore). 44 tall at
/// the screen margins from the top safe area. Test id of the action: `<testIDPrefix>.action` unless
/// the caller sets its own.
struct PBNavHeader: View {
    struct Action {
        let icon: PBIcon
        let accessibilityLabel: String
        var showsBadge = false
        var testID: String?
        let perform: () -> Void
    }

    let title: String
    var action: Action?

    var body: some View {
        HStack(spacing: 0) {
            Text(title)
                .textStyle(.title1)
                .foregroundStyle(PBColor.textPrimary)
                .lineLimit(1)
                .accessibilityAddTraits(.isHeader)
            Spacer(minLength: PBSpace.s8)
            if let action {
                PBIconButton(action.icon, accessibilityLabel: action.accessibilityLabel, style: .glass,
                             showsBadge: action.showsBadge, action: action.perform)
                    .accessibilityIdentifier(action.testID ?? "")
            }
        }
        .frame(height: PBSize.tap)
    }
}

/// Navigation / Nav Header, Type=Inline (Figma 98:873): the 44 pt collapsed bar of a scrolled tab
/// root, a centred Headline on white at 90 % with a background blur, full-bleed at the top safe area.
struct PBInlineNavHeader: View {
    let title: String

    var body: some View {
        Text(title)
            .textStyle(.headline)
            .foregroundStyle(PBColor.textPrimary)
            .lineLimit(1)
            .frame(maxWidth: .infinity)
            .frame(height: PBSize.tap)
            .background {
                Rectangle()
                    .fill(.ultraThinMaterial)
                    .overlay(PBColor.bgPrimary.opacity(0.9))
                    .ignoresSafeArea(edges: .top)
            }
            .accessibilityAddTraits(.isHeader)
    }
}

extension View {
    /// Shows the inline bar over the top of a scrolling tab root once its large title has scrolled
    /// away (`isCollapsed`), fading 0.2 s.
    func pbCollapsingTitle(_ title: String, isCollapsed: Bool) -> some View {
        overlay(alignment: .top) {
            if isCollapsed {
                PBInlineNavHeader(title: title)
                    .transition(.opacity)
            }
        }
        .animation(.easeOut(duration: 0.2), value: isCollapsed)
    }
}

#Preview("PBNavHeader") {
    VStack(spacing: PBSpace.s24) {
        PBNavHeader(title: "Activity", action: .init(icon: .restore, accessibilityLabel: "Recently deleted") {})
        PBNavHeader(title: "Groups", action: .init(icon: .plus, accessibilityLabel: "New group") {})
        PBNavHeader(title: "Profile")
        PBInlineNavHeader(title: "Activity")
    }
    .padding(.horizontal, PBLayout.screenMargin)
}
