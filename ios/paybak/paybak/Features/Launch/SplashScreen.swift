import SwiftUI

/// Splash (screens-launch.md §1): the stacked logo on white, centred in the full screen. The mark
/// springs in and the wordmark rises after it (about 0.85 s); after 1.5 s it dissolves to Home if
/// onboarding is complete, otherwise to Welcome. The generated launch screen is plain white, so the
/// first frame (nothing visible yet) continues it seamlessly.
struct SplashScreen: View {
    @Environment(AppRouter.self) private var router
    @Environment(ProfileStore.self) private var profileStore
    @Environment(\.accessibilityReduceMotion) private var reduceMotion

    @State private var isShown = false

    var body: some View {
        // The Brand / Logo stacked lockup, built from its parts so each part has its own entrance.
        VStack(spacing: PBSpace.s16) {
            PBAppMark(size: 96)
                .scaleEffect(isShown || reduceMotion ? 1 : 0.86)
                .animation(.spring(response: 0.55, dampingFraction: 0.72), value: isShown)
                .opacity(isShown ? 1 : 0)
                .animation(.easeOut(duration: reduceMotion ? 0.2 : 0.3), value: isShown)
            PBWordmark(size: .large)
                .offset(y: isShown || reduceMotion ? 0 : 8)
                .opacity(isShown ? 1 : 0)
                .animation(reduceMotion ? .easeOut(duration: 0.2) : Self.wordmarkRise, value: isShown)
        }
        .accessibilityElement(children: .ignore)
        .accessibilityLabel("Paybak")
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .ignoresSafeArea()
        .background(PBColor.bgPrimary)
        .screenIdentifier(.splash)
        .onAppear { isShown = true }
        .task {
            try? await Task.sleep(for: .seconds(1.5))
            advance()
        }
    }

    /// The wordmark fades in and rises 8 pt from 0.35 s to 0.85 s, ease-out (0.22, 1, 0.36, 1).
    /// With Reduce Motion the whole lockup just fades in over 0.2 s, without scale or slide.
    private static let wordmarkRise = Animation.timingCurve(0.22, 1, 0.36, 1, duration: 0.5).delay(0.35)

    private func advance() {
        guard router.root == .splash else { return }
        router.finishSplash(onboardingComplete: profileStore.profile.onboardingComplete)
    }
}

#Preview("SplashScreen") {
    SplashScreen()
        .environment(AppRouter())
        .environment(ProfileStore())
}
