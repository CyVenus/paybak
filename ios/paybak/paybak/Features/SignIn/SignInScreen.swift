import SwiftUI

/// Sign in — Email or phone (screens-signin.md §1): one field that takes an email or a phone
/// number. "Send code" (or Return) is enabled for a plausible contact; it saves the sign-in method and
/// contact and pushes Verify. The field is focused on appear and the CTA rides 12 pt above the
/// keyboard. Back returns to Get Started; coming back from Verify ("Change") shows the contact the
/// code went to, as Android does (a phone number with its "+91 ").
struct SignInScreen: View {
    @Environment(AppRouter.self) private var router
    @Environment(ProfileStore.self) private var profileStore

    @State private var input = ""
    @FocusState private var isFieldFocused: Bool

    private var contact: SignInContact? { SignInContact(input) }

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            PBOnboardingTopBar(onBack: router.pop, testIDPrefix: "signIn")
            VStack(alignment: .leading, spacing: PBSpace.s12) {
                FigmaWrappedText("What’s your email or\nphone?", style: .title1)
                    .foregroundStyle(PBColor.textPrimary)
                    .accessibilityAddTraits(.isHeader)
                Text("We’ll send a 6-digit code. No password needed.")
                    .textStyle(.body)
                    .foregroundStyle(PBColor.textSecondary)
            }
            .padding(.top, PBSpace.s24)
            PBTextField(
                "Email or phone number",
                text: $input,
                prompt: "you@example.com",
                helper: "We’ll only use it to sign you in.",
                focus: $isFieldFocused
            )
            .keyboardType(.emailAddress)
            .textContentType(.username)
            .textInputAutocapitalization(.never)
            .autocorrectionDisabled()
            .submitLabel(.send)
            .onSubmit(sendCode)
            .accessibilityIdentifier("signIn.field")
            .padding(.top, PBSpace.s24)
            Spacer(minLength: PBSpace.s24)
            PBButton("Send code", fillsWidth: true, action: sendCode)
                .disabled(contact == nil)
                .accessibilityIdentifier("signIn.sendCode")
                .keyboardGap()
        }
        .padding(.horizontal, PBLayout.screenMargin)
        .phoneContentWidth()
        .background(PBColor.bgPrimary)
        .screenIdentifier(.signIn)
        .onAppear {
            prefillSavedContact()
            isFieldFocused = true
        }
    }

    /// Starts from the saved email or phone on every arrival: the debug seed, a second visit after a
    /// relaunch, or the contact the code last went to when "Change" or Back returns from Verify.
    private func prefillSavedContact() {
        let profile = profileStore.profile
        if profile.signInMethod == .email || profile.signInMethod == .phone, let saved = profile.contact {
            input = saved
        }
    }

    private func sendCode() {
        guard let contact else {
            // Return with nothing sendable keeps the keyboard up.
            isFieldFocused = true
            return
        }
        profileStore.update {
            $0.signInMethod = contact.method
            $0.contact = contact.value
        }
        router.push(.verify(showsError: false))
    }
}

#Preview("SignInScreen") {
    SignInScreen()
        .environment(AppRouter())
        .environment(ProfileStore())
}
