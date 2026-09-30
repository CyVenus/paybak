import SwiftUI

/// The root: Splash, the onboarding stack, the main app, or (debug builds) the design-system gallery.
/// Root changes cross-dissolve. It fills the whole screen; each screen centres its own content with
/// `phoneContentWidth()`, so full-bleed layers still reach the edges on wider screens.
struct AppFlowView: View {
    @Environment(AppRouter.self) private var router

    var body: some View {
        ZStack {
            switch router.root {
            case .splash:
                SplashScreen()
                    .transition(.opacity)
            case .onboarding:
                OnboardingFlow()
                    .transition(.opacity)
            case .main:
                MainView()
                    .transition(.opacity)
            #if DEBUG
            case .gallery(let page):
                GalleryView(initialPage: page)
            #endif
            }
        }
        .background(PBColor.bgPrimary)
        // Match Figma at the default size; stop before the accessibility sizes break fixed layouts.
        .dynamicTypeSize(...DynamicTypeSize.xxxLarge)
    }
}

/// Welcome is the stack's root; everything up to All set is pushed over it. Every screen draws its
/// own top bar, and the edge swipe goes back wherever the screen allows it.
private struct OnboardingFlow: View {
    @Environment(AppRouter.self) private var router

    var body: some View {
        @Bindable var router = router
        NavigationStack(path: $router.path) {
            WelcomeScreen()
                .navigationBarHiddenKeepingSwipeBack()
                .navigationDestination(for: OnboardingRoute.self) { route in
                    screen(for: route)
                        .navigationBarHiddenKeepingSwipeBack()
                }
        }
    }

    @ViewBuilder
    private func screen(for route: OnboardingRoute) -> some View {
        switch route {
        case .getStarted: GetStartedScreen()
        case .signIn: SignInScreen()
        case .verify(let showsError): VerifyScreen(showsError: showsError)
        case .setup1: SetupNameScreen()
        case .setup2: SetupCurrencyScreen()
        case .setup3: SetupPaymentScreen()
        case .setup4: SetupNotificationsScreen()
        case .allSet: AllSetScreen()
        }
    }
}
