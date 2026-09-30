import SwiftUI

// TODO(Setup phase): replace this placeholder with the real Setup 1 — Name & photo (screens-setup.md §1).
/// Setup 1: Continue → Setup 2; Back → Verify or Get Started, whichever came before.
struct SetupNameScreen: View {
    @Environment(AppRouter.self) private var router

    var body: some View {
        ScreenPlaceholder(
            screen: .setup1,
            spec: "screens-setup.md §1",
            onBack: router.pop,
            actions: [.init("Continue") { router.push(.setup2) }]
        )
    }
}
