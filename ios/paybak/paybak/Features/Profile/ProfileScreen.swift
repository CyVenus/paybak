import SwiftUI

/// The Profile tab (screens-profile §2): the user's avatar, name and handle, Edit avatar, the settings
/// card (Pro first) and Sign out. The large title collapses into the inline bar once it scrolls under
/// the status bar; the content scrolls clear of the tab bar.
struct ProfileScreen: View {
    @Environment(AppRouter.self) private var router
    @Environment(ProfileStore.self) private var profileStore
    @Environment(LedgerStore.self) private var ledgerStore

    @State private var isTitleCollapsed = false
    @State private var isSignOutAlertPresented = false

    private var profile: UserProfile { profileStore.profile }

    var body: some View {
        ScrollView {
            VStack(spacing: PBLayout.sectionGap) {
                PBNavHeader(title: "Profile")
                header
                settings
                PBTextButton("Sign out") { isSignOutAlertPresented = true }
                    .frame(height: PBSize.tap)
                    .accessibilityIdentifier("profile.signOut")
            }
            .padding(.horizontal, PBLayout.screenMargin)
            .padding(.bottom, PBTabBar.contentInset)
            .phoneContentWidth()
        }
        .onScrollGeometryChange(for: Bool.self) { geometry in
            geometry.contentOffset.y + geometry.contentInsets.top > PBSize.tap
        } action: { _, isCollapsed in
            isTitleCollapsed = isCollapsed
        }
        .pbCollapsingTitle("Profile", isCollapsed: isTitleCollapsed)
        .background(PBColor.bgPrimary)
        // Full screen, so the scrim covers the tab bar too (a tab root sits under the bar).
        .pbFullScreenAlert(isPresented: $isSignOutAlertPresented, title: "Sign out?", message: "Your records stay on this device.",
                           cancelLabel: "Cancel", actionLabel: "Sign out", testIDPrefix: "profile.signOutAlert", onAction: signOut)
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier("screen.profile")
        .onStartScreen([.profileSignOut]) { _ in isSignOutAlertPresented = true }
    }

    private var header: some View {
        VStack(spacing: PBSpace.s16) {
            Button { router.open(.editAvatar) } label: {
                avatar
            }
            .buttonStyle(AvatarButtonStyle())
            .accessibilityLabel("Your avatar. Edit avatar")
            .accessibilityIdentifier("profile.avatar")
            VStack(spacing: PBSpace.s2) {
                Text(profile.name)
                    .textStyle(.title2)
                    .foregroundStyle(PBColor.textPrimary)
                    .accessibilityIdentifier("profile.name")
                if let handle = profile.handle {
                    Text(handle)
                        .textStyle(.subheadline)
                        .foregroundStyle(PBColor.textSecondary)
                        .truncationMode(.middle)
                        .accessibilityIdentifier("profile.handle")
                }
            }
            .lineLimit(1)
            .multilineTextAlignment(.center)
            PBButton("Edit avatar", style: .secondary, size: .small) { router.open(.editAvatar) }
                .accessibilityIdentifier("profile.editAvatar")
        }
        .frame(maxWidth: .infinity)
    }

    /// The 120 pt circle (initials in Title/1).
    private var avatar: some View {
        PBUserAvatar(diameter: Self.avatarSize)
    }

    private static let avatarSize: CGFloat = 120

    private var settings: some View {
        VStack(spacing: 0) {
            PBSettingRow(
                "Paybak Pro",
                value: ledgerStore.isPro ? "Active" : nil,
                icon: .crown,
                badge: ledgerStore.isPro ? nil : "Try free"
            ) { router.open(.paywall(continueTo: nil)) }
                .accessibilityIdentifier("profile.row.pro")
            PBSettingRow("Payment details", value: profile.primaryPaymentMethod?.shortLabel, icon: .wallet) {
                router.open(.paymentDetails)
            }
            .accessibilityIdentifier("profile.row.payment")
            PBSettingRow("Currency", value: currencyValue, icon: .exchange) { router.open(.settingsCurrency) }
                .accessibilityIdentifier("profile.row.currency")
            PBSettingRow("Notifications", icon: .bell) { router.open(.settingsNotifications) }
                .accessibilityIdentifier("profile.row.notifications")
            PBSettingRow("Privacy", icon: .lock) { router.open(.privacyData) }
                .accessibilityIdentifier("profile.row.privacy")
            PBSettingRow("Help & feedback", icon: .help, showsDivider: false) { router.open(.helpFeedback) }
                .accessibilityIdentifier("profile.row.help")
        }
        .pbCard(padding: 0)
    }

    /// "INR ₹": the default currency's code and symbol.
    private var currencyValue: String {
        let currency = Currency(code: profile.defaultCurrency)
        return "\(currency.code) \(currency.symbol)"
    }

    /// Keeps the profile and the ledger, forgets the session and shows Get Started.
    private func signOut() {
        profileStore.signOut()
        router.signOut()
    }
}

/// The avatar circle opens the editor without a pressed look.
private struct AvatarButtonStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .contentShape(.circle)
    }
}

#Preview("ProfileScreen") {
    let profileStore = ProfileStore(defaults: UserDefaults(suiteName: "preview-profile")!)
    profileStore.replace(with: .sample)
    profileStore.update { $0.avatar = .character(.defaultBoy) }
    return ProfileScreen()
        .environment(AppRouter())
        .environment(profileStore)
        .environment(LedgerStore(file: LedgerFile(url: .temporaryDirectory.appending(path: "preview-ledger.json")), profileStore: profileStore))
}
