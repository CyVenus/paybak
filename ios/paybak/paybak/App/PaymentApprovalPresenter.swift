import SwiftUI
import UIKit

/// Shows `PaymentApprovedOverlay` when friends confirm payments you made (`PaymentApprovals`). The
/// overlay gets a window of its own above the app's, so it covers whatever is on screen: pushes,
/// full-screen covers, sheets and alerts; the keyboard is put away first.
///
/// Approvals wait while the app isn't active or isn't in the main app (splash, onboarding, the
/// gallery). Those that arrive together, or while one plays, are shown as one.
final class PaymentApprovalPresenter {
    private let ledgerStore: LedgerStore
    private let router: AppRouter
    private var waiting: [Payment] = []
    private var isFlushScheduled = false
    private var window: UIWindow?
    private weak var previousKeyWindow: UIWindow?

    init(ledgerStore: LedgerStore, router: AppRouter) {
        self.ledgerStore = ledgerStore
        self.router = router
        ledgerStore.observeChanges { [weak self] old, new in
            self?.enqueue(PaymentApprovals.approved(from: old, to: new))
        }
    }

    /// Shows what's waiting, if the app can show it now. Call it when the app becomes active and
    /// when the main app appears.
    func presentIfReady() {
        guard window == nil, !waiting.isEmpty, router.root == .main,
              let scene = activeScene() else { return }
        let headline = PaymentApprovals.headline(for: waiting, in: ledgerStore.ledger)
        waiting.removeAll()

        // A window below the keyboard's can't cover it.
        scene.windows.forEach { $0.endEditing(true) }
        let host = UIHostingController(rootView: PaymentApprovedOverlay(headline: headline) { [weak self] in
            self?.finish()
        })
        host.view.backgroundColor = .clear
        let window = UIWindow(windowScene: scene)
        window.windowLevel = .alert + 1
        window.backgroundColor = .clear
        window.rootViewController = host
        previousKeyWindow = scene.keyWindow
        window.makeKeyAndVisible()
        self.window = window
        UIAccessibility.post(notification: .screenChanged, argument: host.view)
    }

    /// Takes the overlay down at once (the app is going to the background); the approval was shown.
    func finishNow() {
        finish()
    }

    /// One presentation per run-loop turn, so a batch of confirms makes one scene.
    private func enqueue(_ payments: [Payment]) {
        guard !payments.isEmpty else { return }
        waiting.append(contentsOf: payments)
        guard !isFlushScheduled else { return }
        isFlushScheduled = true
        Task { [weak self] in
            guard let self else { return }
            isFlushScheduled = false
            presentIfReady()
        }
    }

    private func finish() {
        guard let window else { return }
        window.isHidden = true
        self.window = nil
        if let previousKeyWindow, !previousKeyWindow.isHidden {
            previousKeyWindow.makeKey()
        }
        previousKeyWindow = nil
        // Anything that arrived meanwhile plays next.
        presentIfReady()
    }

    private func activeScene() -> UIWindowScene? {
        UIApplication.shared.connectedScenes
            .compactMap { $0 as? UIWindowScene }
            .first { $0.activationState == .foregroundActive && $0.keyWindow != nil }
    }
}
