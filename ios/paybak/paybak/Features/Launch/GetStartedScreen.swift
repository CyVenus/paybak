import SwiftUI

// TODO(Launch phase): replace this placeholder with the real Get Started (screens-launch.md §3).
/// Get Started: Apple / Google → Setup 1 (no backend yet); email or phone → Sign in.
/// Back returns to Welcome at the step the user left from.
struct GetStartedScreen: View {
    @Environment(AppRouter.self) private var router
    @Environment(ProfileStore.self) private var profileStore

    var body: some View {
        ScreenPlaceholder(
            screen: .getStarted,
            spec: "screens-launch.md §3",
            onBack: router.pop,
            actions: [
                .init("Continue with Apple") { signIn(with: .apple) },
                .init("Continue with Google") { signIn(with: .google) },
                .init("Continue with email or phone") { router.push(.signIn) },
            ]
        )
    }

    private func signIn(with method: UserProfile.SignInMethod) {
        profileStore.update {
            $0.signInMethod = method
            $0.contact = nil
        }
        router.push(.setup1)
    }
}
