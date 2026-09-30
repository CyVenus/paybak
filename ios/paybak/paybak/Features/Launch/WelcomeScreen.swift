import SwiftUI

// TODO(Launch phase): replace this placeholder with the real Welcome pager (screens-launch.md §2).
/// Welcome 1–3: one screen with a step. Continue → next step; step 3 "Get started" and Skip →
/// Get Started; Back → previous step.
struct WelcomeScreen: View {
    @Environment(AppRouter.self) private var router

    var body: some View {
        let step = router.welcomeStep
        ScreenPlaceholder(
            screen: [.welcome1, .welcome2, .welcome3][step - 1],
            spec: "screens-launch.md §2",
            onSkip: step < 3 ? { router.push(.getStarted) } : nil,
            actions: [
                .init(step < 3 ? "Continue" : "Get started") {
                    if step < 3 { router.welcomeStep += 1 } else { router.push(.getStarted) }
                },
            ] + (step > 1 ? [.init("Back") { router.welcomeStep -= 1 }] : [])
        )
    }
}
