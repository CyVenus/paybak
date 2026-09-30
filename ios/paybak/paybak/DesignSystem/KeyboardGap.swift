import SwiftUI
import UIKit

extension View {
    /// For a bottom CTA that rides the keyboard (Sign in, Setup 1): the view sits on the bottom
    /// safe-area edge, and while the keyboard is up it keeps a 12 pt gap above the keyboard's top
    /// edge (Figma: CTA bottom y 555, keyboard top y 567). SwiftUI's keyboard avoidance does the lift.
    func keyboardGap(_ gap: CGFloat = PBSpace.s12) -> some View {
        modifier(KeyboardGap(gap: gap))
    }
}

private struct KeyboardGap: ViewModifier {
    let gap: CGFloat

    @State private var isKeyboardVisible = false

    func body(content: Content) -> some View {
        content
            .padding(.bottom, isKeyboardVisible ? gap : 0)
            .onReceive(NotificationCenter.default.publisher(for: UIResponder.keyboardWillShowNotification)) {
                setKeyboardVisible(true, notification: $0)
            }
            .onReceive(NotificationCenter.default.publisher(for: UIResponder.keyboardWillHideNotification)) {
                setKeyboardVisible(false, notification: $0)
            }
    }

    /// Moves in step with the keyboard, which animates over the duration in the notification.
    private func setKeyboardVisible(_ isVisible: Bool, notification: Notification) {
        guard isVisible != isKeyboardVisible else { return }
        let duration = notification.userInfo?[UIResponder.keyboardAnimationDurationUserInfoKey] as? Double ?? 0.25
        withAnimation(.smooth(duration: duration)) {
            isKeyboardVisible = isVisible
        }
    }
}
