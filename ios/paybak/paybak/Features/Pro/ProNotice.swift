import SwiftUI

/// A one-button notice in the Overlay / Alert card ("No purchases to restore." · OK): the native
/// single-action alert the spec asks for, drawn in the kit's style. 300 pt wide with 34 pt corners,
/// the message in Headline over a full-width secondary OK. Tapping outside dismisses it too.
struct ProNotice: View {
    let message: String
    let onDismiss: () -> Void

    var body: some View {
        VStack(spacing: PBSpace.s20) {
            Text(message)
                .textStyle(.headline)
                .foregroundStyle(PBColor.textPrimary)
                .multilineTextAlignment(.center)
                .frame(maxWidth: .infinity)
                .accessibilityAddTraits(.isHeader)
            PBButton("OK", style: .secondary, size: .small, fillsWidth: true, action: onDismiss)
                .accessibilityIdentifier("paywall.notice.ok")
        }
        .padding(PBSpace.s20)
        .frame(width: 300)
        .background(PBColor.bgPrimary, in: .rect(cornerRadius: 34))
        .shadow(color: PBPalette.gray900.opacity(0.10), radius: 16, y: 8)
        .accessibilityElement(children: .contain)
        .accessibilityAddTraits(.isModal)
        .accessibilityAction(.escape, onDismiss)
        .accessibilityIdentifier("paywall.notice")
    }
}

extension View {
    /// Shows a `ProNotice` centred over the 40 % scrim while `isPresented` is true.
    func proNotice(_ message: String, isPresented: Binding<Bool>) -> some View {
        overlay {
            ProNoticePresenter(message: message, isPresented: isPresented)
        }
    }
}

/// Fades the scrim in and pops the card like the app's alerts (scale 1.1 → 1, 0.25 s).
private struct ProNoticePresenter: View {
    let message: String
    @Binding var isPresented: Bool

    @Environment(\.accessibilityReduceMotion) private var reduceMotion

    var body: some View {
        ZStack {
            if isPresented {
                PBColor.bgScrim
                    .ignoresSafeArea()
                    .contentShape(.rect)
                    .onTapGesture { isPresented = false }
                    .transition(.opacity)
                    .accessibilityHidden(true)
                ProNotice(message: message) { isPresented = false }
                    .transition(.opacity.combined(with: .scale(scale: reduceMotion ? 1 : 1.1)))
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .ignoresSafeArea()
        .animation(.easeOut(duration: 0.25), value: isPresented)
    }
}

#Preview("ProNotice") {
    @Previewable @State var isPresented = true
    PBButton("Restore purchases") { isPresented = true }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .proNotice("No purchases to restore.", isPresented: $isPresented)
}
