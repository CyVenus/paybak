import SwiftUI

/// Sign in — Email or phone (screens-signin.md §1): one field that takes an email or a phone
/// number. "Send code" (or Return) is enabled for a plausible contact; it saves the sign-in method and
/// contact and pushes Verify. The field is focused on appear and the CTA rides 12 pt above the
/// keyboard. Back returns to Get Started; coming back from Verify keeps what was typed.
struct SignInScreen: View {
    @Environment(AppRouter.self) private var router
    @Environment(ProfileStore.self) private var profileStore

    @State private var input = ""
    @State private var hasPrefilled = false
    @FocusState private var isFieldFocused: Bool

    private var contact: SignInContact? { SignInContact(input) }

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            PBOnboardingTopBar(onBack: router.pop, testIDPrefix: "signIn")
            VStack(alignment: .leading, spacing: PBSpace.s12) {
                // Figma's break: the system would otherwise pull "or" down so "phone?" isn't alone.
                Text("What’s your email or\nphone?")
                    .textStyle(.title1)
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

    /// Starts from the saved email or phone (the debug seed, or a second visit after a relaunch).
    private func prefillSavedContact() {
        guard !hasPrefilled else { return }
        hasPrefilled = true
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
