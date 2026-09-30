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

enum HomeState {
    case firstDay
    case active
    case allSettled
}

/// The app's navigation state: which flow is on screen, the onboarding stack, and the state kept
/// across pushes (the Welcome step, the Home state).
@Observable
final class AppRouter {
    enum Root: Equatable {
        case splash
        case onboarding
        /// Home is the task root: back never returns into onboarding.
        case home
        #if DEBUG
        case gallery(page: Int)
        #endif
    }

    private(set) var root: Root = .splash
    var path: [OnboardingRoute] = []
    /// Welcome's step (1…3). Kept here so Back from Get Started returns to the step the user left.
    var welcomeStep = 1
    var homeState: HomeState = .firstDay
    var isAddSheetPresented = false

    /// The 0.4 s ease-out dissolve used by Splash → next and All set → Home.
    private static let dissolve = Animation.easeOut(duration: 0.4)

    /// Splash → Home if onboarding is complete, otherwise Welcome step 1.
    func finishSplash(onboardingComplete: Bool) {
        withAnimation(Self.dissolve) {
            root = onboardingComplete ? .home : .onboarding
        }
    }

    func push(_ route: OnboardingRoute) {
        path.append(route)
    }

    func pop() {
        guard !path.isEmpty else { return }
        path.removeLast()
    }

    /// All set → Home (first day), replacing the whole onboarding stack.
    func finishOnboarding() {
        withAnimation(Self.dissolve) {
            homeState = .firstDay
            isAddSheetPresented = false
            root = .home
        }
        path = []
    }

    /// Back to a fresh Welcome step 1 (after the profile was cleared).
    func restartOnboarding() {
        path = []
        welcomeStep = 1
        isAddSheetPresented = false
        root = .onboarding
    }
}

#if DEBUG
// Debug start screens (DebugLaunchOptions); compiled out of release builds.
extension AppRouter {
    /// Jumps straight to a screen with a plausible back stack (debug start screens).
    func show(_ screen: ScreenID) {
        isAddSheetPresented = false
        switch screen {
        case .splash:
            root = .splash
        case .welcome1, .welcome2, .welcome3:
            welcomeStep = screen == .welcome1 ? 1 : screen == .welcome2 ? 2 : 3
            path = []
            root = .onboarding
        case .homeFirstDay, .homeActive, .homeAllSettled, .homeAddSheet:
            homeState = switch screen {
            case .homeFirstDay: .firstDay
            case .homeAllSettled: .allSettled
            default: .active
            }
            isAddSheetPresented = screen == .homeAddSheet
            root = .home
        default:
            welcomeStep = 3
            path = Self.emailSignInPath(to: screen)
            root = .onboarding
        }
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
