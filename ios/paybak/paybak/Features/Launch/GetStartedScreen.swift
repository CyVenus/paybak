import SwiftUI

/// Get Started (screens-launch.md §3): the sign-in choice. Apple and Google record the method and go
/// straight to Setup 1 (there's no backend yet); email or phone goes to Sign in. Figma has no back
/// button here: the edge swipe returns to Welcome at the step the user left from.
struct GetStartedScreen: View {
    @Environment(AppRouter.self) private var router
    @Environment(ProfileStore.self) private var profileStore

    var body: some View {
        VStack(spacing: 0) {
            PBLogo(layout: .horizontal)
                .padding(.top, PBSpace.s24)
            // The artboard draws the grey card itself and overhangs the 362 × 260 slot by 12 pt.
            PaybakRiveIllustration(.getStarted)
                .padding(.top, PBSpace.s32)
            VStack(spacing: PBSpace.s12) {
                Text("Shared money,\nkept clear.")
                    .textStyle(.title1)
                    .foregroundStyle(PBColor.textPrimary)
                    .accessibilityAddTraits(.isHeader)
                    .accessibilityIdentifier("getStarted.headline")
                Text("Paybak keeps the record.\nYou pay however you like.")
                    .textStyle(.body)
                    .foregroundStyle(PBColor.textSecondary)
            }
            .multilineTextAlignment(.center)
            .padding(.top, PBSpace.s32)
            Spacer(minLength: PBSpace.s24)
            VStack(spacing: PBSpace.s12) {
                PBButton("Continue with Apple", icon: .apple, fillsWidth: true) { signIn(with: .apple) }
                    .accessibilityIdentifier("getStarted.apple")
                PBButton("Continue with Google", style: .secondary, icon: .google, fillsWidth: true) {
                    signIn(with: .google)
                }
                .accessibilityIdentifier("getStarted.google")
                PBTextButton("Continue with email or phone") { router.push(.signIn) }
                    .accessibilityIdentifier("getStarted.email")
                Text(Self.legalNotice)
                    .textStyle(.footnote)
                    .multilineTextAlignment(.center)
                    .accessibilityIdentifier("getStarted.legal")
            }
        }
        .padding(.horizontal, PBLayout.screenMargin)
        .phoneContentWidth()
        .background(PBColor.bgPrimary)
        .screenIdentifier(.getStarted)
    }

    /// "By continuing, you agree to our Terms and Privacy Policy." in `text/tertiary`, with the two
    /// policy names in `text/secondary` and underlined, at the same Medium weight (not bold). They
    /// aren't links until the policies have URLs.
    private static let legalNotice: AttributedString = {
        func run(_ text: String, isPolicy: Bool = false) -> AttributedString {
            var run = AttributedString(text)
            run.swiftUI.foregroundColor = isPolicy ? PBColor.textSecondary : PBColor.textTertiary
            if isPolicy {
                run.swiftUI.underlineStyle = .single
            }
            return run
        }
        return run("By continuing, you agree to our ") + run("Terms", isPolicy: true) + run(" and ")
            + run("Privacy Policy", isPolicy: true) + run(".")
    }()

    private func signIn(with method: UserProfile.SignInMethod) {
        profileStore.update {
            $0.signInMethod = method
            $0.contact = nil
        }
        router.push(.setup1)
    }
}

#Preview("GetStartedScreen") {
    GetStartedScreen()
        .environment(AppRouter())
        .environment(ProfileStore())
}
