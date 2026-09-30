import SwiftUI

// TODO(Setup phase): replace this placeholder with the real Setup 4 — Notifications (screens-setup.md §4).
/// Setup 4 (optional): "Turn on notifications" (asks the OS), "Not now" or Skip → All set.
struct SetupNotificationsScreen: View {
    @Environment(AppRouter.self) private var router

    var body: some View {
        ScreenPlaceholder(
            screen: .setup4,
            spec: "screens-setup.md §4",
            onBack: router.pop,
            onSkip: { router.push(.allSet) },
            actions: [
                .init("Turn on notifications") { router.push(.allSet) },
                .init("Not now") { router.push(.allSet) },
            ]
        )
    }
}
