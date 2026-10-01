import SwiftUI

/// Setup 4 — Notifications (screens-setup.md §4), optional: explains reminders before the OS
/// prompt. "Turn on notifications" asks for permission (or reuses an earlier answer) and goes to All
/// set whatever the answer; "Not now" and Skip go there without asking. The bell card rings when
/// tapped (the Rive file's own listener).
struct SetupNotificationsScreen: View {
    @Environment(AppRouter.self) private var router
    @Environment(ProfileStore.self) private var profileStore

    @State private var isRequesting = false

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            PBSetupHeader(step: 4, onBack: router.pop, onSkip: notNow)
            // The artboard is the 362 × 300 slot plus 12 pt of bleed on each side, centred in the
            // content width on wider phones.
            PaybakRiveIllustration(.notifications)
                .frame(maxWidth: .infinity)
                .padding(.top, PBSpace.s24)
            VStack(alignment: .leading, spacing: PBSpace.s12) {
                Text("Get gentle reminders")
                    .textStyle(.title1)
                    .foregroundStyle(PBColor.textPrimary)
                    .accessibilityAddTraits(.isHeader)
                Text("We’ll nudge you before something’s due and tell you when a friend pays you back.")
                    .textStyle(.body)
                    .foregroundStyle(PBColor.textSecondary)
            }
            .padding(.top, PBSpace.s32)
            Spacer(minLength: PBSpace.s24)
            VStack(spacing: PBSpace.s8) {
                PBButton("Turn on notifications", fillsWidth: true, action: turnOn)
                    .accessibilityIdentifier("setup4.enable")
                PBTextButton("Not now", style: .secondary, action: notNow)
                    .accessibilityIdentifier("setup4.notNow")
            }
        }
        .padding(.horizontal, PBLayout.screenMargin)
        .phoneContentWidth()
        .background(PBColor.bgPrimary)
        .screenIdentifier(.setup4)
    }

    /// Repeat taps while the system prompt is up are ignored.
    private func turnOn() {
        guard !isRequesting else { return }
        isRequesting = true
        Task {
            let choice = await NotificationPermission.request()
            finish(with: choice)
            isRequesting = false
        }
    }

    private func notNow() {
        guard !isRequesting else { return }
        finish(with: .notNow)
    }

    private func finish(with choice: UserProfile.NotificationsChoice) {
        profileStore.update { $0.notifications = choice }
        router.push(.allSet)
    }
}

#Preview("SetupNotificationsScreen") {
    SetupNotificationsScreen()
        .environment(AppRouter())
        .environment(ProfileStore())
}
