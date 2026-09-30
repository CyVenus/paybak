#if DEBUG
import Foundation
import SwiftUI
import os

/// Debug-only launch hooks (flow.md "Debug-only hooks", app-architecture §3.10), compiled out of
/// release builds:
///
///     -startScreen <id>          any screen id (ScreenID), or `gallery`
///     -galleryPage <n>           with `-startScreen gallery`: open page n (0-based)
///     -resetOnboarding YES       clear the profile and the ledger before starting
///     -now <yyyy-MM-ddTHH:mm>    pin the clock (local time)
///     -pro YES|NO                override the entitlement after the scenario
///     -demoData YES              load the demo at Figma parity without changing the start screen
///     -scenario <a>[,<b>…]       apply seed scenarios after the demo loads
///     -link <paybak://…>         open a deep link after launch, as a notification tap would
///
/// e.g. `xcrun simctl launch --terminate-running-process <udid> app.paybak.paybak -startScreen groupGoaTrip`.
struct DebugLaunchOptions {
    enum StartScreen {
        case screen(ScreenID)
        case gallery(page: Int)
    }

    let startScreen: StartScreen?
    let resetsOnboarding: Bool
    let now: Date?
    let pro: Bool?
    let loadsDemo: Bool
    let scenarios: [String]
    let link: DeepLink?

    private static let log = Logger(subsystem: "app.paybak.paybak", category: "Debug")

    /// Reads the arguments from UserDefaults' argument domain, where `-key value` pairs land.
    init(defaults: UserDefaults = .standard, calendar: Calendar = .autoupdatingCurrent) {
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
                Self.log.warning("Unknown -startScreen '\(id, privacy: .public)'")
                startScreen = nil
            }
        }
        now = defaults.string(forKey: "now").flatMap { Self.parseMoment($0, calendar: calendar) }
        pro = defaults.object(forKey: "pro") == nil ? nil : defaults.bool(forKey: "pro")
        loadsDemo = defaults.bool(forKey: "demoData")
        scenarios = defaults.string(forKey: "scenario")?.split(separator: ",").map { $0.trimmingCharacters(in: .whitespaces) } ?? []
        link = defaults.string(forKey: "link").flatMap { DeepLink($0) }
    }

    func apply(to profileStore: ProfileStore, ledgerStore: LedgerStore, router: AppRouter) {
        if resetsOnboarding {
            DebugStart.resetEverything(profileStore: profileStore, ledgerStore: ledgerStore)
        }
        if loadsDemo || !scenarios.isEmpty {
            do {
                try ledgerStore.loadDemo(scenarios: loadsDemo ? Scenario.demo + scenarios : scenarios)
            } catch {
                Self.log.error("Could not load the demo: \(String(describing: error), privacy: .public)")
            }
        }
        switch startScreen {
        case .gallery(let page):
            router.showGallery(page: page)
        case .screen(let screen):
            DebugStart.show(screen, extraScenarios: loadsDemo ? [] : scenarios, profileStore: profileStore,
                            ledgerStore: ledgerStore, router: router)
        case nil:
            break
        }
        if let now {
            ledgerStore.setClock(now)
        }
        if let pro {
            ledgerStore.setPro(pro)
        }
        router.pendingLink = link
    }

    /// `yyyy-MM-ddTHH:mm` in local time.
    private static func parseMoment(_ text: String, calendar: Calendar) -> Date? {
        let parts = text.split(separator: "T")
        guard parts.count == 2, let day = LocalDay(string: String(parts[0])) else { return nil }
        let time = parts[1].split(separator: ":").compactMap { Int($0) }
        guard time.count == 2 else { return nil }
        return day.moment(hour: time[0], minute: time[1], in: calendar)
    }
}

/// Starting any screen id (launch hook and the debug menu's "Load screen…").
enum DebugStart {
    private static let log = Logger(subsystem: "app.paybak.paybak", category: "Debug")

    /// Onboarding ids seed the sample profile when mid-flow; main-app ids load their scenario's data,
    /// then set up the tab, stacks, sheets and modals, and hand the id to its screen.
    static func show(_ screen: ScreenID, extraScenarios: [String] = [], profileStore: ProfileStore,
                     ledgerStore: LedgerStore, router: AppRouter) {
        if screen.isOnboarding {
            if screen.isMidFlow {
                profileStore.replace(with: .sample)
            }
            router.showOnboarding(screen)
            return
        }
        guard let scenario = Scenario.all[screen] else {
            log.warning("No scenario for \(screen.rawValue, privacy: .public)")
            return
        }
        do {
            try ledgerStore.loadDemo(scenarios: scenario.seeds + extraScenarios)
        } catch {
            log.error("Could not load the demo: \(String(describing: error), privacy: .public)")
        }
        if let avatar = scenario.avatar {
            profileStore.update { $0.avatar = .character(avatar) }
        }
        var transaction = Transaction()
        transaction.disablesAnimations = true
        withTransaction(transaction) {
            router.resetMain()
            router.showMain()
            router.selectedTab = scenario.tab
            router.groupsSegment = scenario.groupsSegment ?? .groups
            router.activitySegment = scenario.activitySegment ?? .timeline
            router.mainPath = scenario.stack
            router.mainSheet = scenario.sheet
            router.modals = scenario.modals.map { ModalLayer(root: $0.root, path: $0.path, sheet: $0.sheet) }
            router.startScreen = screen
        }
        if let toast = scenario.toast {
            router.toast(toast)
        }
        postLockScreenNotification(for: screen, ledgerStore: ledgerStore)
    }

    /// The lock-screen ids post their notification on launch (screens-activity §7).
    private static func postLockScreenNotification(for screen: ScreenID, ledgerStore: LedgerStore) {
        let snapshot = ledgerStore.snapshot
        switch screen {
        case .lockConfirmRequest:
            guard let claim = snapshot.home.pendingClaims.first else { return }
            Task { await NotificationService.postClaim(claim) }
        case .lockReminder:
            guard let row = snapshot.inbox.first(where: { $0.item.type == .paymentReminder }) else { return }
            Task { await NotificationService.post(row, link: DeepLink(row.item)) }
        default:
            break
        }
    }

    /// "Reset onboarding": clears the profile, the ledger and the debug clock.
    static func resetEverything(profileStore: ProfileStore, ledgerStore: LedgerStore) {
        profileStore.reset()
        ledgerStore.reset()
        DebugState.pinnedClock = nil
        DebugState.demoAnchor = nil
        ledgerStore.clock.pin(nil)
    }
}
#endif
