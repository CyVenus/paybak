import SwiftUI

/// All set (screens-setup.md §5): greets the user by first name. Onboarding is saved as complete
/// as soon as it appears, so a relaunch lands on Home; there's no way back into setup (no back
/// button, no edge swipe). "Go to Home" dissolves to Home (first day), replacing the stack.
struct AllSetScreen: View {
    @Environment(AppRouter.self) private var router
    @Environment(ProfileStore.self) private var profileStore

    private var headline: String {
        let firstName = profileStore.profile.firstName
        return firstName.isEmpty ? "You’re all set." : "You’re all set, \(firstName)."
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            // Figma keeps the 44 pt a header would take. Centred in the content width on wider phones.
            PaybakRiveIllustration(.allSet)
                .frame(maxWidth: .infinity)
                .padding(.top, PBSize.tap)
            VStack(alignment: .leading, spacing: PBSpace.s12) {
                Text(headline)
                    .textStyle(.title1)
                    .foregroundStyle(PBColor.textPrimary)
                    .accessibilityAddTraits(.isHeader)
                    .accessibilityIdentifier("allSet.headline")
                Text("Add your first expense or\ninvite friends to start splitting.")
                    .textStyle(.body)
                    .foregroundStyle(PBColor.textSecondary)
            }
            .padding(.top, PBSpace.s32)
            Spacer(minLength: PBSpace.s24)
            PBButton("Go to Home", fillsWidth: true, action: router.finishOnboarding)
                .accessibilityIdentifier("allSet.goHome")
        }
        .padding(.horizontal, PBLayout.screenMargin)
        .phoneContentWidth()
        .background(PBColor.bgPrimary)
        .screenIdentifier(.allSet)
        .navigationBarBackButtonHidden()
        .onAppear {
            profileStore.update { $0.onboardingComplete = true }
        }
    }
}

#Preview("AllSetScreen") {
    AllSetScreen()
        .environment(AppRouter())
        .environment(ProfileStore())
}
