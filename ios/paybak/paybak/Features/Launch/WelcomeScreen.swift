import SwiftUI

/// Welcome 1–3 (screens-launch.md §2): one screen whose step drives the onboarding Rive, the copy,
/// the page dots and the CTA. Continue or a left swipe goes to the next step, a right swipe back to
/// the previous one (VoiceOver adjusts the page dots instead); "Get started" (step 3) and Skip
/// (steps 1–2) go to Get Started. The step lives in the router, so Back from Get Started returns to
/// the step the user left from.
struct WelcomeScreen: View {
    @Environment(AppRouter.self) private var router

    /// One artboard for all three steps: setting `step` plays the file's own slide transition.
    @StateObject private var illustration = PaybakRiveController(.onboarding)
    /// When the step last changed. Taps and swipes that land mid-transition are ignored, so a double
    /// tap can't skip a step.
    @State private var lastStepChange = Date.distantPast

    private var step: Int { router.welcomeStep }

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            PBOnboardingTopBar(onSkip: isLastStep ? nil : { skip() }, testIDPrefix: "welcome")
            // Centred in the content width, like Android's, on phones wider than its 362 pt slot.
            PaybakRiveView(controller: illustration)
                .frame(maxWidth: .infinity)
                .padding(.top, PBSpace.s8)
            WelcomeText(step: step)
                .padding(.top, PBSpace.s32)
            Spacer(minLength: PBSpace.s24)
            footer
        }
        .padding(.horizontal, PBLayout.screenMargin)
        .padding(.bottom, PBSpace.s16)
        .phoneContentWidth()
        .background(PBColor.bgPrimary)
        .contentShape(.rect)
        .gesture(swipe)
        .screenIdentifier(screenID)
        .onChange(of: step, initial: true) {
            illustration.setNumber("step", to: Float(step))
        }
    }

    /// Page dots over the CTA, whose label crossfades to "Get started" on the last step.
    private var footer: some View {
        VStack(alignment: .leading, spacing: PBSpace.s24) {
            PBPageDots(count: WelcomeCopy.all.count, active: step)
                // VoiceOver can't make the swipes, and Welcome is the stack's root with no back, so
                // the dots are adjustable like UIPageControl: swipe up or down to change the step.
                .accessibilityAdjustableAction { direction in
                    switch direction {
                    case .increment: showNextStep()
                    case .decrement: showPreviousStep()
                    @unknown default: break
                    }
                }
                .accessibilityIdentifier("welcome.dots")
            PBButton(isLastStep ? "Get started" : "Continue", fillsWidth: true, action: next)
                .contentTransition(.opacity)
                .animation(.easeInOut(duration: 0.2), value: isLastStep)
                .accessibilityIdentifier("welcome.continue")
        }
    }

    private var screenID: ScreenID {
        [.welcome1, .welcome2, .welcome3][step - 1]
    }

    private var isLastStep: Bool { step == WelcomeCopy.all.count }

    /// Horizontal swipes change the step, like the CTA: left = next, right = previous. The art can't
    /// be scrubbed, so the content doesn't follow the finger; the step changes when the swipe ends.
    private var swipe: some Gesture {
        DragGesture(minimumDistance: 20)
            .onEnded { value in
                let dx = value.translation.width
                guard abs(dx) > abs(value.translation.height),
                      abs(dx) > 50 || abs(value.velocity.width) > 300
                else { return }
                if dx < 0 {
                    showNextStep()
                } else {
                    showPreviousStep()
                }
            }
    }

    /// A left swipe or a VoiceOver increment. The last step ignores it: only "Get started" leaves
    /// the pager.
    private func showNextStep() {
        guard !isLastStep else { return }
        changeStep(to: step + 1)
    }

    /// A right swipe or a VoiceOver decrement.
    private func showPreviousStep() {
        guard step > 1 else { return }
        changeStep(to: step - 1)
    }

    private func next() {
        if isLastStep {
            guard isSettled else { return }
            router.push(.getStarted)
        } else {
            changeStep(to: step + 1)
        }
    }

    /// Skip dissolves to Get Started (300 ms); "Get started" pushes it.
    private func skip() {
        withDissolve(duration: 0.3) {
            router.push(.getStarted)
        }
    }

    private func changeStep(to newStep: Int) {
        guard isSettled else { return }
        lastStepChange = .now
        router.welcomeStep = newStep
    }

    /// The text and the Rive take about 0.3 s to change steps; input during that is ignored.
    private var isSettled: Bool {
        Date.now.timeIntervalSince(lastStepChange) >= 0.3
    }
}

/// The headline and body of each step, verbatim from Figma. Step 1 breaks where Figma does in the
/// string itself: it measures 361.3 pt and would otherwise just fit on one line at 362 pt. The others
/// wrap by themselves within the 362 pt measure (a hard break would give them three lines on narrow
/// phones), as on Android.
private struct WelcomeCopy {
    let headline: String
    let body: String

    static let all = [
        WelcomeCopy(headline: "Split any bill in\nseconds.", body: "Add it once. Paybak does the math for everyone."),
        WelcomeCopy(headline: "Know who owes what, and by when.", body: "Clear balances and due dates, all in one place."),
        WelcomeCopy(headline: "Settle up without the awkward chat.", body: "Record payments and send gentle reminders."),
    ]
}

/// The step's headline and body. When the step changes, the old copy slides 24 pt out and fades
/// (150 ms, ease-in), then the new copy slides in from the other side (300 ms, ease-out), in time
/// with the Rive's exit and enter. With Reduce Motion the copy fades out and in without sliding.
private struct WelcomeText: View {
    let step: Int

    @Environment(\.accessibilityReduceMotion) private var reduceMotion
    @State private var shownStep: Int
    @State private var shift: CGFloat = 0
    @State private var opacity: Double = 1

    init(step: Int) {
        self.step = step
        _shownStep = State(initialValue: step)
    }

    /// Figma's 362 content width, so the headlines break where the designs do on wider phones.
    private static let measure: CGFloat = 362

    var body: some View {
        let copy = WelcomeCopy.all[shownStep - 1]
        VStack(alignment: .leading, spacing: PBSpace.s12) {
            // Every headline keeps at least two lines, so the body stays put between steps even if
            // one fits on a line (at a smaller text size, say).
            ZStack(alignment: .topLeading) {
                Text(verbatim: " \n ")
                    .textStyle(.title1)
                    .hidden()
                    .accessibilityHidden(true)
                Text(copy.headline)
                    .textStyle(.title1)
                    .foregroundStyle(PBColor.textPrimary)
                    .accessibilityAddTraits(.isHeader)
                    .accessibilityIdentifier("welcome.headline")
            }
            Text(copy.body)
                .textStyle(.body)
                .foregroundStyle(PBColor.textSecondary)
                .accessibilityIdentifier("welcome.body")
        }
        .frame(maxWidth: Self.measure, alignment: .leading)
        .frame(maxWidth: .infinity, alignment: .leading)
        .offset(x: shift)
        .opacity(opacity)
        .onChange(of: step) { oldStep, newStep in
            transition(to: newStep, forward: newStep > oldStep)
        }
    }

    private func transition(to newStep: Int, forward: Bool) {
        let distance: CGFloat = reduceMotion ? 0 : (forward ? 24 : -24)
        withAnimation(.easeIn(duration: reduceMotion ? 0.1 : 0.15)) {
            shift = -distance
            opacity = 0
        } completion: {
            shownStep = newStep
            shift = distance
            withAnimation(reduceMotion ? .easeOut(duration: 0.1) : .timingCurve(0.2, 0, 0, 1, duration: 0.3)) {
                shift = 0
                opacity = 1
            }
        }
    }
}

#Preview("WelcomeScreen") {
    WelcomeScreen()
        .environment(AppRouter())
}
