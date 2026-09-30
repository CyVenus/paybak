import SwiftUI

/// The root: Splash, the onboarding stack, Home, or (debug builds) the design-system gallery.
/// Root changes cross-dissolve. Phone layouts stay centred at 430 pt on wider screens.
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
            case .home:
                HomeScreen()
                    .transition(.opacity)
            #if DEBUG
            case .gallery(let page):
                GalleryView(initialPage: page)
            #endif
            }
        }
        .frame(maxWidth: PBLayout.maxContentWidth)
        .frame(maxWidth: .infinity)
        .background(PBColor.bgPrimary)
        // Match Figma at the default size; stop before the accessibility sizes break fixed layouts.
        .dynamicTypeSize(...DynamicTypeSize.xxxLarge)
    }
}

/// Welcome is the stack's root; everything up to All set is pushed over it.
private struct OnboardingFlow: View {
    @Environment(AppRouter.self) private var router

    var body: some View {
        @Bindable var router = router
        NavigationStack(path: $router.path) {
            WelcomeScreen()
                .navigationDestination(for: OnboardingRoute.self) { route in
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
    }
}
