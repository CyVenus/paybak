import Observation
import SwiftUI

/// Screens pushed over Welcome in the onboarding `NavigationStack` (flow.md navigation table).
enum OnboardingRoute: Hashable {
    case getStarted
    case signIn
    /// `showsError` opens the screen in its wrong-code state (the `verifyWrong` id).
    case verify(showsError: Bool)
    case setup1
    case setup2
    case setup3
    case setup4
    case allSet
}

/// The app's navigation state: which flow is on screen, the onboarding stack, and the main app's
/// tabs, stacks, modal layers and sheets (app-architecture §2). The main-app helpers are in
/// `Navigation/AppRouter+Main.swift`.
@Observable
final class AppRouter {
    enum Root: Equatable {
        case splash
        case onboarding
        /// The tab shell and everything over it. It's the task root: back never returns into onboarding.
        case main
        #if DEBUG
        case gallery(page: Int)
        #endif
    }

    private(set) var root: Root = .splash
    var path: [OnboardingRoute] = []
    /// Welcome's step (1…3). Kept here so Back from Get Started returns to the step the user left.
    var welcomeStep = 1

    // MARK: Main

    var selectedTab: Tab = .home
    var groupsSegment: GroupsSegment = .groups
    var activitySegment: ActivitySegment = .timeline
    /// The Insights month; nil = the clock's current month.
    var insightsMonth: YearMonth?
    /// Pushes over the tab shell (the tab bar hides while anything is pushed).
    var mainPath: [Route] = []
    /// The main stack's route sheet.
    var mainSheet: Route?
    /// Full-screen modal layers, bottom first; each has its own stack and sheet.
    var modals: [ModalLayer] = []
    /// Picker results waiting for the screen that asked (`onRouteResult`).
    var results: [String: RouteResult] = [:]
    var toast: PBToastMessage?
    /// The top screen's pinned bottom buttons (`pinnedFooter()`): the toast then sits 12 pt above
    /// them instead of 50 pt above the bottom edge.
    var pinnedFooter: PinnedFooterMark?
    /// Opened once the current sheet has gone (`replaceSheet(with:)`).
    @ObservationIgnored var routeAfterSheet: Route?
    /// Whether the user has Pro (`requirePro`); set by the app from the ledger store.
    @ObservationIgnored var isPro: () -> Bool = { false }
    /// A deep link to open once the main app shows (the debug `-link` argument).
    @ObservationIgnored var pendingLink: DeepLink?
    #if DEBUG
    /// The debug start screen whose in-screen state its owner hasn't applied yet (`onStartScreen`).
    @ObservationIgnored var startScreen: ScreenID?
    #endif

    /// The 0.4 s ease-out dissolve used by Splash → next and All set → Home.
    private static let dissolve = Animation.easeOut(duration: 0.4)

    /// Splash → Home if onboarding is complete, otherwise Welcome step 1 (dropping any link waiting
    /// for the app: links are ignored during onboarding, as on Android).
    func finishSplash(onboardingComplete: Bool) {
        if !onboardingComplete {
            pendingLink = nil
        }
        withAnimation(Self.dissolve) {
            root = onboardingComplete ? .main : .onboarding
        }
    }

    /// Pushes `route` unless it's already on top: a quick double tap lands both taps before the push
    /// covers the button, and would otherwise open the same screen twice.
    func push(_ route: OnboardingRoute) {
        guard path.last != route else { return }
        path.append(route)
    }

    func pop() {
        guard !path.isEmpty else { return }
        path.removeLast()
    }

    /// All set → Home (first day), replacing the whole onboarding stack.
    func finishOnboarding() {
        resetMain()
        withAnimation(Self.dissolve) {
            root = .main
        }
        path = []
    }

    /// Back to a fresh Welcome step 1 (after the profile was cleared).
    func restartOnboarding() {
        resetMain()
        path = []
        welcomeStep = 1
        root = .onboarding
    }

    /// Back to Get Started (Sign out keeps the data on the device).
    func signOut() {
        resetMain()
        welcomeStep = 3
        path = [.getStarted]
        root = .onboarding
    }

    /// Home, with nothing pushed or presented.
    func resetMain() {
        modals = []
        mainSheet = nil
        mainPath = []
        selectedTab = .home
        groupsSegment = .groups
        activitySegment = .timeline
        insightsMonth = nil
        results = [:]
        routeAfterSheet = nil
        pinnedFooter = nil
    }

    /// Shows the main app on its current state (debug start screens, deep links).
    func showMain() {
        path = []
        root = .main
    }
}

enum GroupsSegment: String, Codable {
    case groups
    case friends
}

enum ActivitySegment: String, Codable {
    case timeline
    case insights
}

/// A full-screen modal with its own push stack and route sheet.
struct ModalLayer: Identifiable, Hashable {
    let id = UUID()
    var root: Route
    var path: [Route] = []
    var sheet: Route?
}

#if DEBUG
// Debug start screens (DebugLaunchOptions); compiled out of release builds.
extension AppRouter {
    /// Jumps straight to an onboarding screen with a plausible back stack (debug start screens).
    func showOnboarding(_ screen: ScreenID) {
        resetMain()
        switch screen {
        case .splash:
            root = .splash
        case .welcome1, .welcome2, .welcome3:
            welcomeStep = screen == .welcome1 ? 1 : screen == .welcome2 ? 2 : 3
            path = []
            root = .onboarding
        default:
            welcomeStep = 3
            path = Self.emailSignInPath(to: screen)
            root = .onboarding
        }
    }

    /// Hands the start screen to the first screen that owns it, once.
    func takeStartScreen(in screens: Set<ScreenID>) -> ScreenID? {
        guard let screen = startScreen, screens.contains(screen) else { return nil }
        startScreen = nil
        return screen
    }

    func showGallery(page: Int) {
        root = .gallery(page: page)
    }

    /// The stack for the email/phone sign-in, cut after `screen`.
    private static func emailSignInPath(to screen: ScreenID) -> [OnboardingRoute] {
        let steps: [(ScreenID, OnboardingRoute)] = [
            (.getStarted, .getStarted),
            (.signIn, .signIn),
            (.verify, .verify(showsError: false)),
            (.setup1, .setup1),
            (.setup2, .setup2),
            (.setup3, .setup3),
            (.setup4, .setup4),
            (.allSet, .allSet),
        ]
        if screen == .verifyWrong {
            return [.getStarted, .signIn, .verify(showsError: true)]
        }
        guard let end = steps.firstIndex(where: { $0.0 == screen }) else { return [] }
        return steps[...end].map(\.1)
    }
}
#endif
