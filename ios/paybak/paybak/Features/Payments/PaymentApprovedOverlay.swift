import SwiftUI

/// The payer's "payment approved" moment: `paybak-payment.riv` fills the screen (its `main` artboard
/// brings its own white) under a headline such as "Meera confirmed ₹450". It fades in with a success
/// haptic, plays the coin sequence once and fades out; a tap closes it sooner. It plays with Reduce
/// Motion too. `PaymentApprovalPresenter` shows it in a window of its own, over everything.
struct PaymentApprovedOverlay: View {
    let headline: String
    /// Called once the overlay has faded out.
    let onFinish: () -> Void

    /// One pass of the coin sequence: hand-rise, coin-drop, hand-grab, coin-shatter and hand-lower
    /// are 460 frames at 60 fps, after which the file loops.
    static let sequence: Duration = .milliseconds(7_667)
    static let fadeIn: TimeInterval = 0.25
    static let fadeOut: TimeInterval = 0.3

    @State private var isShown = false
    @State private var isClosing = false

    var body: some View {
        ZStack(alignment: .top) {
            PBColor.bgPrimary
                .ignoresSafeArea()
            PaybakRiveScene(.payment)
            Text(headline)
                .textStyle(.title2)
                .foregroundStyle(PBColor.textPrimary)
                .multilineTextAlignment(.center)
                // White on the scene's white, so it only shows when the coin bursts: its lines cross
                // the top and the scene flashes black for a moment.
                .padding(.horizontal, PBSpace.s16)
                .padding(.vertical, PBSpace.s8)
                .background(PBColor.bgPrimary, in: RoundedRectangle(cornerRadius: PBRadius.card, style: .continuous))
                .frame(maxWidth: .infinity)
                .padding(.horizontal, PBLayout.screenMargin)
                .padding(.top, PBSpace.s16)
        }
        .opacity(isShown && !isClosing ? 1 : 0)
        .contentShape(Rectangle())
        .onTapGesture(perform: close)
        .dynamicTypeSize(...DynamicTypeSize.xxxLarge)
        .accessibilityElement(children: .ignore)
        .accessibilityLabel(headline)
        .accessibilityHint("Double-tap to close.")
        .accessibilityAddTraits([.isModal, .isButton])
        .accessibilityAction(.escape, close)
        .accessibilityIdentifier("paymentApproved")
        .task {
            withAnimation(.easeOut(duration: Self.fadeIn)) { isShown = true }
            Haptics.success()
            // Cancelled when the presenter takes the window down (the app went to the background).
            guard (try? await Task.sleep(for: Self.sequence)) != nil else { return }
            close()
        }
    }

    private func close() {
        guard !isClosing else { return }
        withAnimation(.easeIn(duration: Self.fadeOut)) {
            isClosing = true
        } completion: {
            onFinish()
        }
    }
}

#Preview("PaymentApprovedOverlay") {
    PaymentApprovedOverlay(headline: "Meera confirmed ₹450") {}
}
