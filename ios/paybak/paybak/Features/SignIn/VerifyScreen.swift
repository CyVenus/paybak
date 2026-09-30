import SwiftUI

/// Sign in — Verify code and Wrong code (screens-signin.md §2–3): one screen. Six code boxes over a
/// hidden number-pad field, focused on appear. 250 ms after the sixth digit the code is checked:
/// `000000` pushes Setup 1; anything else shows the wrong-code state (red rings, the message and an
/// error haptic) until a digit is edited. "Resend code" unlocks 30 s after the code was sent and
/// resets the screen in place. Back or "Change" returns to Sign in with the contact kept.
struct VerifyScreen: View {
    @Environment(AppRouter.self) private var router
    @Environment(ProfileStore.self) private var profileStore

    @State private var code: String
    @State private var isWrong: Bool
    @State private var countdown: ResendCountdown
    @State private var secondsLeft: Int
    @State private var pendingCheck: Task<Void, Never>?
    @FocusState private var isCodeFocused: Bool

    /// - Parameter showsError: Opens in the wrong-code state with "482917" entered and the countdown
    ///   finished, like the Figma frame (the `verifyWrong` debug start screen).
    init(showsError: Bool) {
        let countdown = showsError ? ResendCountdown(unlocksAt: .now) : .started()
        _code = State(initialValue: showsError ? "482917" : "")
        _isWrong = State(initialValue: showsError)
        _countdown = State(initialValue: countdown)
        _secondsLeft = State(initialValue: countdown.secondsLeft(at: .now))
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            PBOnboardingTopBar(onBack: router.pop, testIDPrefix: "verify")
            VStack(alignment: .leading, spacing: PBSpace.s12) {
                Text("Enter the code")
                    .textStyle(.title1)
                    .foregroundStyle(PBColor.textPrimary)
                    .accessibilityAddTraits(.isHeader)
                SentToLine(contact: profileStore.profile.contact ?? "", onChange: router.pop)
            }
            .padding(.top, PBSpace.s24)
            PBCodeField(code: $code, isError: isWrong, focus: $isCodeFocused, onComplete: check)
                .accessibilityIdentifier("verify.code")
                .padding(.top, PBSpace.s32)
            resendArea
            Spacer(minLength: 0)
        }
        .padding(.horizontal, PBLayout.screenMargin)
        .phoneContentWidth()
        .background(PBColor.bgPrimary)
        .screenIdentifier(isWrong ? .verifyWrong : .verify)
        .sensoryFeedback(.error, trigger: isWrong) { _, isWrong in isWrong }
        .onChange(of: code) {
            // Editing a digit clears the error (PBCodeField starts over when a digit is typed).
            isWrong = false
        }
        .task(id: countdown) { await runCountdown() }
        .onAppear { isCodeFocused = true }
    }

    /// Under the code row (the designed layouts keep the label tops at y 308 / 342):
    /// the error message when the code was wrong, then the countdown or, once it's done, the
    /// Resend button (4 pt below, so its label lines up with where the countdown was).
    private var resendArea: some View {
        VStack(alignment: .leading, spacing: 0) {
            if isWrong {
                Text("That code didn’t match. Check it and try again.")
                    .textStyle(.footnote)
                    .foregroundStyle(PBColor.textDestructive)
                    .padding(.top, PBSpace.s16)
                    .accessibilityIdentifier("verify.error")
            }
            if secondsLeft > 0 {
                Text(ResendCountdown.label(secondsLeft: secondsLeft))
                    .textStyle(.footnote)
                    .foregroundStyle(PBColor.textTertiary)
                    .padding(.top, PBSpace.s16)
                    .accessibilityIdentifier("verify.countdown")
            } else {
                PBTextButton("Resend code", action: resend)
                    .padding(.top, PBSpace.s4)
                    .accessibilityIdentifier("verify.resend")
            }
        }
    }

    /// Checks the code 250 ms after the sixth digit, so it visibly fills first. Editing the code in
    /// the meantime drops the check.
    private func check(_ entered: String) {
        pendingCheck?.cancel()
        pendingCheck = Task {
            try? await Task.sleep(for: .milliseconds(250))
            guard !Task.isCancelled, code == entered else { return }
            if VerificationCode.isCorrect(entered) {
                router.push(.setup1)
            } else {
                isWrong = true
                AccessibilityNotification.Announcement("That code didn’t match. Check it and try again.").post()
            }
        }
    }

    /// Nothing is sent (no backend): clear the boxes and the error, restart the timer, keep focus.
    private func resend() {
        pendingCheck?.cancel()
        code = ""
        isWrong = false
        countdown = .started()
        isCodeFocused = true
    }

    /// Updates `secondsLeft` on each whole second until the countdown finishes. The timer runs off
    /// the unlock time, so it stays right after the screen was covered or the app was in the
    /// background.
    private func runCountdown() async {
        while true {
            secondsLeft = countdown.secondsLeft(at: .now)
            guard let next = countdown.nextTick(after: .now) else {
                break
            }
            try? await Task.sleep(for: .seconds(max(0, next.timeIntervalSinceNow)))
            if Task.isCancelled { return }
        }
    }
}

/// "Sent to {contact} · Change" in Body: the sentence in `text/secondary`, "Change" in
/// `text/primary`, underlined, with a 44 pt tall tap target. A contact too long for one line wraps
/// the whole sentence as text, and "Change" stays inline at its end as a link.
private struct SentToLine: View {
    let contact: String
    let onChange: () -> Void

    var body: some View {
        ViewThatFits(in: .horizontal) {
            HStack(spacing: 0) {
                Text(sentTo)
                    .textStyle(.body)
                    .foregroundStyle(PBColor.textSecondary)
                Button(action: onChange) {
                    Text("Change")
                        .textStyle(.body)
                        .underline()
                        .foregroundStyle(PBColor.textPrimary)
                }
                .buttonStyle(InlineLinkButtonStyle())
                .accessibilityIdentifier("verify.change")
            }
            Text(wrapped)
                .textStyle(.body)
                .tint(PBColor.textPrimary)
                .environment(\.openURL, OpenURLAction { _ in
                    onChange()
                    return .handled
                })
        }
    }

    private var sentTo: String { "Sent to \(contact) · " }

    private var wrapped: AttributedString {
        var sentence = AttributedString(sentTo)
        sentence.foregroundColor = PBColor.textSecondary
        var change = AttributedString("Change")
        change.foregroundColor = PBColor.textPrimary
        change.underlineStyle = .single
        change.link = URL(string: "paybak://sign-in/change-contact")
        return sentence + change
    }
}

/// An inline text link: 50 % opacity while pressed (like Button / Text), with its tap target
/// padded to 44 pt tall without moving the text.
private struct InlineLinkButtonStyle: ButtonStyle {
    private let verticalHitSlop = (PBSize.tap - PBTextStyle.body.lineHeight) / 2
    private let horizontalHitSlop = PBSpace.s8

    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .opacity(configuration.isPressed ? 0.5 : 1)
            .animation(.easeOut(duration: 0.1), value: configuration.isPressed)
            .padding(.vertical, verticalHitSlop)
            .padding(.horizontal, horizontalHitSlop)
            .contentShape(.rect)
            .padding(.vertical, -verticalHitSlop)
            .padding(.horizontal, -horizontalHitSlop)
    }
}

#Preview("VerifyScreen") {
    VerifyScreen(showsError: false)
        .environment(AppRouter())
        .environment(ProfileStore())
}

#Preview("VerifyScreen, wrong code") {
    VerifyScreen(showsError: true)
        .environment(AppRouter())
        .environment(ProfileStore())
}
