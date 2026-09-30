import SwiftUI

// TODO(Sign-in phase): replace this placeholder with the real Sign in (screens-signin.md §1).
/// Sign in: one email-or-phone field; Send code → Verify.
struct SignInScreen: View {
    @Environment(AppRouter.self) private var router

    var body: some View {
        ScreenPlaceholder(
            screen: .signIn,
            spec: "screens-signin.md §1",
            onBack: router.pop,
            actions: [.init("Send code") { router.push(.verify(showsError: false)) }]
        )
    }
}
