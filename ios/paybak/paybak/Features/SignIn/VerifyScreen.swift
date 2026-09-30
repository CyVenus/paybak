import SwiftUI

// TODO(Sign-in phase): replace this placeholder with the real Verify code (screens-signin.md §2–3).
/// Verify: 000000 → Setup 1; any other code → the wrong-code state (the verifyWrong id).
/// "Change" → back to Sign in.
struct VerifyScreen: View {
    @State private var showsError: Bool
    @Environment(AppRouter.self) private var router

    init(showsError: Bool) {
        _showsError = State(initialValue: showsError)
    }

    var body: some View {
        ScreenPlaceholder(
            screen: showsError ? .verifyWrong : .verify,
            spec: "screens-signin.md §2–3",
            onBack: router.pop,
            actions: [
                .init("Enter 000000") { router.push(.setup1) },
                .init("Enter a wrong code") { showsError = true },
                .init("Change") { router.pop() },
            ]
        )
    }
}
