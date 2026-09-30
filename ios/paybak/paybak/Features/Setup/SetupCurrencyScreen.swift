import SwiftUI

// TODO(Setup phase): replace this placeholder with the real Setup 2 — Currency (screens-setup.md §2).
/// Setup 2: Continue → Setup 3; Back → Setup 1.
struct SetupCurrencyScreen: View {
    @Environment(AppRouter.self) private var router

    var body: some View {
        ScreenPlaceholder(
            screen: .setup2,
            spec: "screens-setup.md §2",
            onBack: router.pop,
            actions: [.init("Continue") { router.push(.setup3) }]
        )
    }
}
