import SwiftUI

/// Overlay / Toast (Figma 118:965): a short confirmation after a save ("Expense added", "Payment
/// recorded", "UPI ID copied"). A 44 pt black capsule that hugs an optional 20 pt icon and its label.
/// No shadow, never a link. Show it with `.pbToast(_:bottomPadding:)`: 50 pt above the bottom edge,
/// 16 above the tab bar on tab screens, or 16 above a pinned CTA.
struct PBToast: View {
    let message: String
    var icon: PBIcon? = .checkCircle

    init(_ message: String, icon: PBIcon? = .checkCircle) {
        self.message = message
        self.icon = icon
    }

    var body: some View {
        HStack(spacing: PBSpace.s8) {
            if let icon {
                PBIconView(icon, size: PBSize.iconMd)
                    .foregroundStyle(PBColor.iconInverse)
            }
            Text(message)
                .textStyle(.buttonSmall)
                .foregroundStyle(PBColor.textInverse)
                .lineLimit(1)
        }
        // 16 pt before the icon or the label (Figma keeps it when Show icon is off), 20 pt after.
        .padding(.leading, PBSpace.s16)
        .padding(.trailing, PBSpace.s20)
        .frame(height: PBSize.tap)
        .background(PBColor.bgInverse, in: .capsule)
        .accessibilityElement(children: .combine)
    }
}

/// One showing of a toast. Each value is new, so showing the same text again restarts the timer.
struct PBToastMessage: Equatable, Identifiable {
    let id = UUID()
    let text: String

    init(_ text: String) {
        self.text = text
    }
}

extension View {
    /// Shows `toast` centred `bottomPadding` above this view's bottom edge: it fades in rising 8 pt
    /// (0.2 s), stays 2 s, fades out, then sets `toast` back to nil. VoiceOver reads it out.
    func pbToast(_ toast: Binding<PBToastMessage?>, bottomPadding: CGFloat) -> some View {
        modifier(ToastPresenter(toast: toast, bottomPadding: bottomPadding))
    }
}

private struct ToastPresenter: ViewModifier {
    @Binding var toast: PBToastMessage?
    let bottomPadding: CGFloat

    @Environment(\.accessibilityReduceMotion) private var reduceMotion

    private static let visibleDuration: Duration = .seconds(2)

    func body(content: Content) -> some View {
        content.overlay(alignment: .bottom) {
            ZStack {
                if let toast {
                    PBToast(toast.text)
                        .accessibilityIdentifier("toast")
                        .id(toast.id)
                        .transition(.opacity.combined(with: .offset(y: reduceMotion ? 0 : 8)))
                }
            }
            .padding(.bottom, bottomPadding)
            .animation(.easeOut(duration: 0.2), value: toast)
            .allowsHitTesting(false)
            .task(id: toast?.id) {
                guard let shown = toast else { return }
                AccessibilityNotification.Announcement(shown.text).post()
                try? await Task.sleep(for: Self.visibleDuration)
                guard !Task.isCancelled, toast?.id == shown.id else { return }
                toast = nil
            }
        }
    }
}

#Preview("PBToast") {
    @Previewable @State var toast: PBToastMessage?
    VStack(spacing: PBSpace.s16) {
        PBToast("UPI ID copied")
        PBToast("Payment recorded", icon: nil)
        PBButton("Show a toast") { toast = PBToastMessage("UPI ID copied") }
    }
    .frame(maxWidth: .infinity, maxHeight: .infinity)
    .pbToast($toast, bottomPadding: PBSpace.s48)
}
