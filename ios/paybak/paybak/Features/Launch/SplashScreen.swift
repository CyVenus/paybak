import SwiftUI

// TODO(Launch phase): replace this placeholder with the real Splash (screens-launch.md §1).
/// Splash: auto-advances after 1.5 s to Home if onboarding is complete, otherwise to Welcome.
struct SplashScreen: View {
    @Environment(AppRouter.self) private var router
    @Environment(ProfileStore.self) private var profileStore

    var body: some View {
        ScreenPlaceholder(
            screen: .splash,
            spec: "screens-launch.md §1",
            actions: [.init("Next", perform: advance)]
        )
        .task {
            try? await Task.sleep(for: .seconds(1.5))
            advance()
        }
    }

    private func advance() {
        guard router.root == .splash else { return }
        router.finishSplash(onboardingComplete: profileStore.profile.onboardingComplete)
    }
}
