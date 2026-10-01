import SwiftUI

/// Overlay / Alert (Figma 102:1115): the iOS 27 alert card, 300 pt wide with 34 pt corners. A
/// centred Headline title and Subheadline message over two small pills sharing the width: a
/// secondary cancel ("Keep editing") and the action, red when something is destroyed ("Discard")
/// or black otherwise ("Settle up"). Present it with `.pbAlert(…)`, which centres it over the 40 %
/// scrim; tapping outside does nothing, because an alert needs an explicit choice.
/// Test ids: `<prefix>.cancel`, `<prefix>.action`.
struct PBAlert: View {
    enum Role {
        /// Button / Destructive: Discard, Delete.
        case destructive
        /// Button / Primary: nothing is destroyed.
        case primary
    }

    let title: String
    var message: String?
    let cancelLabel: String
    let actionLabel: String
    var role: Role = .destructive
    var testIDPrefix: String?
    let onCancel: () -> Void
    let onAction: () -> Void

    var body: some View {
        VStack(spacing: PBSpace.s20) {
            VStack(spacing: PBSpace.s4) {
                Text(title)
                    .textStyle(.headline)
                    .foregroundStyle(PBColor.textPrimary)
                    .accessibilityAddTraits(.isHeader)
                if let message {
                    Text(message)
                        .textStyle(.subheadline)
                        .foregroundStyle(PBColor.textSecondary)
                }
            }
            .multilineTextAlignment(.center)
            .frame(maxWidth: .infinity)
            HStack(spacing: PBSpace.s8) {
                PBButton(cancelLabel, style: .secondary, size: .small, fillsWidth: true, action: onCancel)
                    .accessibilityIdentifier(testID("cancel"))
                PBButton(actionLabel, style: role == .destructive ? .destructive : .primary, size: .small, fillsWidth: true, action: onAction)
                    .accessibilityIdentifier(testID("action"))
            }
        }
        .padding(PBSpace.s20)
        .frame(width: 300)
        .background(PBColor.bgPrimary, in: .rect(cornerRadius: 34))
        .shadow(color: PBPalette.gray900.opacity(0.10), radius: 16, y: 8)
        .accessibilityElement(children: .contain)
        .accessibilityAddTraits(.isModal)
        .accessibilityAction(.escape, onCancel)
    }

    private func testID(_ element: String) -> String {
        testIDPrefix.map { "\($0).\(element)" } ?? ""
    }
}

extension View {
    /// Shows a `PBAlert` over this view while `isPresented` is true. Either button dismisses it;
    /// `onAction` runs after the action button.
    func pbAlert(
        isPresented: Binding<Bool>,
        title: String,
        message: String? = nil,
        cancelLabel: String,
        actionLabel: String,
        role: PBAlert.Role = .destructive,
        testIDPrefix: String? = nil,
        onAction: @escaping () -> Void
    ) -> some View {
        overlay {
            AlertPresenter(isPresented: isPresented) {
                PBAlert(
                    title: title,
                    message: message,
                    cancelLabel: cancelLabel,
                    actionLabel: actionLabel,
                    role: role,
                    testIDPrefix: testIDPrefix,
                    onCancel: { isPresented.wrappedValue = false },
                    onAction: {
                        isPresented.wrappedValue = false
                        onAction()
                    }
                )
            }
        }
    }
}

extension View {
    /// `pbAlert` for a view that can't cover the whole screen itself: a tab root (under the tab bar)
    /// or a sheet. It draws in a clear full-screen cover above them, so the scrim dims everything.
    /// `onAction` runs once the alert has gone.
    func pbFullScreenAlert(
        isPresented: Binding<Bool>,
        title: String,
        message: String? = nil,
        cancelLabel: String,
        actionLabel: String,
        role: PBAlert.Role = .destructive,
        testIDPrefix: String? = nil,
        onAction: @escaping () -> Void
    ) -> some View {
        fullScreenCover(isPresented: isPresented) {
            AlertCover(isPresented: isPresented) { dismiss in
                PBAlert(
                    title: title,
                    message: message,
                    cancelLabel: cancelLabel,
                    actionLabel: actionLabel,
                    role: role,
                    testIDPrefix: testIDPrefix,
                    onCancel: { dismiss(nil) },
                    onAction: { dismiss(onAction) }
                )
            }
            .presentationBackground(.clear)
        }
        // The cover appears and goes without its slide; the alert fades in and out inside it.
        .transaction(value: isPresented.wrappedValue) { $0.disablesAnimations = true }
    }
}

/// The alert inside its full-screen cover: shown on appear, faded out before the cover goes, and
/// then the chosen action runs.
private struct AlertCover<Alert: View>: View {
    @Binding var isPresented: Bool
    let alert: (_ dismiss: @escaping (_ then: (() -> Void)?) -> Void) -> Alert

    @State private var isShown = false

    var body: some View {
        AlertPresenter(isPresented: $isShown) {
            alert(dismiss)
        }
        .onAppear { isShown = true }
    }

    private func dismiss(then action: (() -> Void)?) {
        isShown = false
        Task {
            try? await Task.sleep(for: .milliseconds(250))
            isPresented = false
            action?()
        }
    }
}

/// Fades the scrim and pops the card in like a system alert (scale 1.1 → 1, 0.25 s). The card is
/// centred on the screen, not the safe area (Figma 102:1115 sits at y 356 of 874).
private struct AlertPresenter<Alert: View>: View {
    @Binding var isPresented: Bool
    @ViewBuilder let alert: Alert

    @Environment(\.accessibilityReduceMotion) private var reduceMotion

    var body: some View {
        ZStack {
            if isPresented {
                PBColor.bgScrim
                    .ignoresSafeArea()
                    .transition(.opacity)
                    .accessibilityHidden(true)
                alert
                    .transition(.opacity.combined(with: .scale(scale: reduceMotion ? 1 : 1.1)))
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .ignoresSafeArea()
        .animation(.easeOut(duration: 0.25), value: isPresented)
    }
}

#Preview("PBAlert") {
    @Previewable @State var isPresented = true
    VStack(spacing: PBSpace.s24) {
        PBAlert(title: "Discard changes?", message: "Your avatar edits won’t be saved.", cancelLabel: "Keep editing", actionLabel: "Discard", onCancel: {}, onAction: {})
        PBAlert(title: "Settle up with Rohan?", message: "Record ₹800 as paid.", cancelLabel: "Not now", actionLabel: "Settle up", role: .primary, onCancel: {}, onAction: {})
        PBButton("Show alert") { isPresented = true }
    }
    .frame(maxWidth: .infinity, maxHeight: .infinity)
    .background(PBColor.bgCard)
    .pbAlert(isPresented: $isPresented, title: "Discard changes?", message: "Your avatar edits won’t be saved.", cancelLabel: "Keep editing", actionLabel: "Discard") {}
}
