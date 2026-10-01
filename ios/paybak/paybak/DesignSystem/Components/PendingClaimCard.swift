import SwiftUI

/// A friend's claim that they paid you, wired to the ledger (home-v2 §3.3, components-app §3.9):
/// Confirm confirms the payment and turns the card Confirmed; "Payment confirmed" shows once that
/// has had its moment to read (the 250 ms animation and a hold, 1.05 s; at once with Reduce Motion).
/// Not received opens its sheet. Home adds its own collapse sequence around it.
/// Test ids: `<testIDPrefix>.confirm`, `.notReceived`.
struct PendingClaimCard: View {
    let claim: PendingClaim
    var testIDPrefix = "claim"
    /// Runs after a successful confirm (Home collapses the card and adds the activity row).
    var onConfirmed: () -> Void = {}

    @Environment(LedgerStore.self) private var ledgerStore
    @Environment(AppRouter.self) private var router
    @Environment(\.accessibilityReduceMotion) private var reduceMotion
    @State private var isConfirmed = false

    /// The Confirmed animation (250 ms) plus the hold that lets it read before the toast.
    private static let confirmedHold: Duration = .milliseconds(1050)

    var body: some View {
        PBConfirmPaymentCard(
            avatar: ledgerStore.ledger.person(claim.payment.fromId)?.avatarContent ?? .initials("?"),
            title: claim.title,
            detail: claim.detail,
            confirmedTitle: "\(ledgerStore.books.firstName(claim.payment.fromId)) paid you \(Money.format(claim.payment.amount, claim.payment.currency))",
            confirmedDetail: "\(claim.purpose) · \(claim.payment.method.label) · Confirmed",
            isConfirmed: isConfirmed,
            testIDPrefix: testIDPrefix,
            onConfirm: confirm,
            onNotReceived: { router.open(.notReceived(claim.payment.id)) }
        )
        .sensoryFeedback(.success, trigger: isConfirmed)
    }

    private func confirm() {
        do {
            try ledgerStore.confirmPayment(claim.payment.id)
            isConfirmed = true
            onConfirmed()
            let router = router
            let hold = reduceMotion ? Duration.zero : Self.confirmedHold
            Task {
                try? await Task.sleep(for: hold)
                router.toast("Payment confirmed")
            }
        } catch {
            router.toast(error.localizedDescription)
        }
    }
}
