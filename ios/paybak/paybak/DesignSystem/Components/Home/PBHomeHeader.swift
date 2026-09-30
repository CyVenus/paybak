import SwiftUI

/// Navigation / Nav Header, Type=Home (Figma 17:474, components-home §1): the Paybak logo, the glass
/// sparkle (Ask Paybak) and bell (with the unread dot) in a 44 pt toolbar, and the Title/1 greeting
/// 12 below. It scrolls with Home's content. Long-pressing the logo runs `onLogoLongPress` (the debug
/// menu in debug builds). Test ids: `home.logo`, `home.assistant`, `home.bell`, `home.greeting`.
struct PBHomeHeader: View {
    let greeting: String
    var hasUnread = false
    var onAssistant: () -> Void = {}
    var onBell: () -> Void = {}
    var onLogoLongPress: (() -> Void)?

    var body: some View {
        VStack(alignment: .leading, spacing: PBSpace.s12) {
            HStack(spacing: PBSpace.s8) {
                logo
                Spacer(minLength: 0)
                PBIconButton(.sparkles, accessibilityLabel: "Ask Paybak", style: .glass, action: onAssistant)
                    .accessibilityIdentifier("home.assistant")
                PBIconButton(.bell, accessibilityLabel: "Notifications", style: .glass, showsBadge: hasUnread, action: onBell)
                    .accessibilityValue(hasUnread ? "Unread" : "")
                    .accessibilityIdentifier("home.bell")
            }
            .frame(height: PBSize.tap)
            Text(greeting)
                .textStyle(.title1)
                .foregroundStyle(PBColor.textPrimary)
                .frame(maxWidth: .infinity, alignment: .leading)
                .fixedSize(horizontal: false, vertical: true)
                .accessibilityAddTraits(.isHeader)
                .accessibilityIdentifier("home.greeting")
        }
    }

    @ViewBuilder
    private var logo: some View {
        let logo = PBLogo()
            .frame(height: PBSize.tap)
            .contentShape(.rect)
            .accessibilityIdentifier("home.logo")
        if let onLogoLongPress {
            logo.onLongPressGesture(perform: onLogoLongPress)
        } else {
            logo
        }
    }
}

#Preview("PBHomeHeader") {
    VStack(spacing: PBSpace.s32) {
        PBHomeHeader(greeting: "Good evening, Arjun", hasUnread: true)
        PBHomeHeader(greeting: "Good morning, Alexandra Konstantinopoulou")
    }
    .padding(.horizontal, PBLayout.screenMargin)
}
