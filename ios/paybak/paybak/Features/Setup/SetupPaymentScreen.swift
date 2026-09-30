import SwiftUI

// TODO(Setup phase): replace this placeholder with the real Setup 3 — Payment (screens-setup.md §3).
/// Setup 3 (optional): Continue or Skip → Setup 4; Back → Setup 2.
struct SetupPaymentScreen: View {
    @Environment(AppRouter.self) private var router

    var body: some View {
        ScreenPlaceholder(
            screen: .setup3,
            spec: "screens-setup.md §3",
            onBack: router.pop,
            onSkip: { router.push(.setup4) },
            actions: [.init("Continue") { router.push(.setup4) }]
        )
    }
}
