import SwiftUI

// TODO(Setup phase): replace this placeholder with the real All set (screens-setup.md §5).
/// All set: onboarding is saved as complete when it appears, back is disabled, and
/// "Go to Home" replaces the stack with Home (first day).
struct AllSetScreen: View {
    @Environment(AppRouter.self) private var router
    @Environment(ProfileStore.self) private var profileStore

    var body: some View {
        ScreenPlaceholder(
            screen: .allSet,
            spec: "screens-setup.md §5",
            actions: [.init("Go to Home", perform: router.finishOnboarding)]
        )
        .navigationBarBackButtonHidden()
        .onAppear {
            profileStore.update { $0.onboardingComplete = true }
        }
    }
}
