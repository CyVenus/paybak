#if DEBUG
import Foundation
import SwiftUI
import os

/// Debug-only launch hooks (flow.md "Debug-only hooks", app-architecture §3.10), compiled out of
/// release builds:
///
///     -resetOnboarding YES       clear the profile and the ledger before starting
///     -startScreen <id>          any screen id (ScreenID): an app screen loads its demo scenario at
///                                Figma parity and opens its tab, stack and sheet; onboarding ids past
///                                Get Started seed the sample profile. `gallery` opens the gallery
///     -galleryPage <n>           with `-startScreen gallery`: open page n (1-based)
///     -demoData YES              load the demo (`D`) at Figma parity without changing the start screen
///                                (only without `-startScreen`)
///     -now <yyyy-MM-ddTHH:mm>    pin the clock (local time)
///     -scenario <a>[,<b>…]       then apply seed scenarios to today's ledger
///     -pro YES|NO                override the plan last
///     -link <paybak://…>         open a deep link after launch, as a notification tap would
///     -autoApprove YES|NO        whether friends confirm your new payments after 5 s, for this launch
///                                only (`DebugState.autoApprovesPayments`; UI tests pass NO)
///     -autoApproveAfter <s>      how many seconds they take instead of 5
///
/// e.g. `xcrun simctl launch --terminate-running-process <udid> app.paybak.paybak -startScreen groupGoaTrip`.
struct DebugLaunchOptions {
    enum StartScreen {
        case screen(ScreenID)
        /// 1-based.
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
            startScreen = .gallery(page: defaults.object(forKey: "galleryPage") == nil ? 1 : defaults.integer(forKey: "galleryPage"))
        case let id?:
            if let screen = ScreenID(rawValue: id) {
                startScreen = .screen(screen)
            } else {
                Self.log.warning("Unknown startScreen \"\(id, privacy: .public)\"; starting normally")
                startScreen = nil
            }
        }
        now = defaults.string(forKey: "now").flatMap { Self.parseMoment($0, calendar: calendar) }
        pro = defaults.object(forKey: "pro") == nil ? nil : defaults.bool(forKey: "pro")
        loadsDemo = defaults.bool(forKey: "demoData")
        scenarios = defaults.string(forKey: "scenario")?
            .split(separator: ",")
            .map { $0.trimmingCharacters(in: .whitespaces) }
            .filter { !$0.isEmpty } ?? []
        link = defaults.string(forKey: "link").flatMap { DeepLink($0) }
    }

    /// The gallery starts on its own, without the data overrides. Otherwise: the start screen (with
    /// its scenario), or the demo when asked for and there's no start screen; then the clock, the
    /// extra scenarios on top and the plan, in that order.
    func apply(to profileStore: ProfileStore, ledgerStore: LedgerStore, router: AppRouter) {
        if resetsOnboarding {
            DebugStart.resetEverything(profileStore: profileStore, ledgerStore: ledgerStore)
        }
        switch startScreen {
        case .gallery(let page):
            router.showGallery(page: page)
            return
        case .screen(let screen):
            DebugStart.show(screen, postsLockScreen: false, profileStore: profileStore, ledgerStore: ledgerStore, router: router)
        case nil:
            if loadsDemo {
                do {
                    try ledgerStore.loadDemo(scenarios: Scenario.demo)
                } catch {
                    Self.log.error("Could not load the demo: \(String(describing: error), privacy: .public)")
                }
            }
        }
        if let now {
            ledgerStore.setClock(now)
        }
        for name in scenarios {
            do {
                try ledgerStore.applyScenario(name)
            } catch {
                Self.log.error("Could not apply \(name, privacy: .public): \(String(describing: error), privacy: .public)")
            }
        }
        if let pro {
            ledgerStore.setPro(pro)
        }
        if case .screen(let screen) = startScreen {
            DebugStart.postLockScreenNotification(for: screen, ledgerStore: ledgerStore)
        }
        router.pendingLink = link
    }

    /// `yyyy-MM-ddTHH:mm[:ss]` in local time.
    private static func parseMoment(_ text: String, calendar: Calendar) -> Date? {
        let parts = text.split(separator: "T")
        guard parts.count == 2, let day = LocalDay(string: String(parts[0])) else { return nil }
        let time = parts[1].split(separator: ":").compactMap { Int($0) }
        guard time.count == 2 || time.count == 3 else { return nil }
        let seconds = time.count == 3 ? TimeInterval(time[2]) : 0
        return day.moment(hour: time[0], minute: time[1], in: calendar).addingTimeInterval(seconds)
    }
}

/// Starting any screen id (launch hook and the debug menu's "Load screen").
enum DebugStart {
    private static let log = Logger(subsystem: "app.paybak.paybak", category: "Debug")

    /// Onboarding ids seed the sample profile when mid-flow; main-app ids load their scenario's data,
    /// then set up the tab, stacks, sheets and modals, and hand the id to its screen. The lock-screen
    /// ids post their notification unless `postsLockScreen` is false (the launch hook posts it after
    /// its overrides).
    static func show(_ screen: ScreenID, postsLockScreen: Bool = true, profileStore: ProfileStore,
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
            try ledgerStore.loadDemo(scenarios: scenario.seeds)
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
        if postsLockScreen {
            postLockScreenNotification(for: screen, ledgerStore: ledgerStore)
        }
    }

    /// `lockConfirmRequest` posts Esha's claim and `lockReminder` tonight's Kabir reminder, so they
    /// show over Home (screens-activity §7).
    static func postLockScreenNotification(for screen: ScreenID, ledgerStore: LedgerStore) {
        let snapshot = ledgerStore.snapshot
        switch screen {
        case .lockConfirmRequest:
            guard let claim = snapshot.home.pendingClaims.first else { return }
            Task { await NotificationService.postClaim(claim) }
        case .lockReminder:
            let reminders = snapshot.inbox.filter { $0.item.type == .paymentReminder }
            guard let row = reminders.max(by: { $0.item.createdAt < $1.item.createdAt }) else { return }
            Task { await NotificationService.post(row, link: DeepLink(row.item, in: ledgerStore.ledger)) }
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
