#if DEBUG
import Foundation
import os

/// Debug-only launch hooks (flow.md "Debug-only hooks"), compiled out of release builds:
///
///     -startScreen <id>       start on a flow.md screen id, or `gallery`
///     -galleryPage <n>        with `-startScreen gallery`: open page n (0-based)
///     -resetOnboarding YES    clear the saved profile before starting
///
/// e.g. `xcrun simctl launch --terminate-running-process <udid> app.paybak.paybak -startScreen setup3`.
/// Starting mid-flow (after Get Started) seeds the sample profile so screens render like Figma;
/// the Home ids also mark onboarding complete.
struct DebugLaunchOptions {
    enum StartScreen {
        case screen(ScreenID)
        case gallery(page: Int)
    }

    let startScreen: StartScreen?
    let resetsOnboarding: Bool

    /// Reads the arguments from UserDefaults' argument domain, where `-key value` pairs land.
    init(defaults: UserDefaults = .standard) {
        resetsOnboarding = defaults.bool(forKey: "resetOnboarding")
        switch defaults.string(forKey: "startScreen") {
        case nil:
            startScreen = nil
        case "gallery":
            startScreen = .gallery(page: defaults.integer(forKey: "galleryPage"))
        case let id?:
            if let screen = ScreenID(rawValue: id) {
                startScreen = .screen(screen)
            } else {
                Logger(subsystem: "app.paybak.paybak", category: "Debug").warning("Unknown -startScreen '\(id, privacy: .public)'")
                startScreen = nil
            }
        }
    }

    func apply(to store: ProfileStore, router: AppRouter) {
        if resetsOnboarding {
            store.reset()
        }
        switch startScreen {
        case .gallery(let page):
            router.showGallery(page: page)
        case .screen(let screen):
            if screen.isMidFlow {
                var sample = UserProfile.sample
                sample.onboardingComplete = screen.isHome
                store.replace(with: sample)
            }
            router.show(screen)
        case nil:
            break
        }
    }
}

private extension ScreenID {
    var isHome: Bool {
        switch self {
        case .homeFirstDay, .homeActive, .homeAllSettled, .homeAddSheet: true
        default: false
        }
    }
}
#endif
