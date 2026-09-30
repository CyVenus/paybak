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
        // The system alert, so the scrim covers the tab bar too (a tab root sits under the bar).
        .alert("Sign out?", isPresented: $isSignOutAlertPresented) {
            Button("Cancel", role: .cancel) {}
            Button("Sign out", role: .destructive, action: signOut)
        } message: {
            Text("Your records stay on this device.")
        }
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier("screen.profile")
        .onStartScreen([.profileSignOut]) { _ in isSignOutAlertPresented = true }
    }

    private var header: some View {
        VStack(spacing: PBSpace.s16) {
            Button { router.open(.editAvatar) } label: {
                PBUserAvatar(diameter: 120)
            }
            .buttonStyle(.plain)
            .accessibilityLabel("Edit avatar")
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
                        .accessibilityIdentifier("profile.handle")
                }
            }
            .lineLimit(1)
            PBButton("Edit avatar", style: .secondary, size: .small) { router.open(.editAvatar) }
                .accessibilityIdentifier("profile.editAvatar")
        }
        .frame(maxWidth: .infinity)
    }

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

#Preview("ProfileScreen") {
    let profileStore = ProfileStore(defaults: UserDefaults(suiteName: "preview-profile")!)
    profileStore.replace(with: .sample)
    profileStore.update { $0.avatar = .character(.defaultBoy) }
    return ProfileScreen()
        .environment(AppRouter())
        .environment(profileStore)
        .environment(LedgerStore(file: LedgerFile(url: .temporaryDirectory.appending(path: "preview-ledger.json")), profileStore: profileStore))
}
