#if DEBUG
import Foundation

/// The friend's side while there's no backend: 5 s after you record a payment to a friend
/// (`DebugState.autoApproveDelay`), they confirm it, so the payment-approved scene plays over
/// whatever screen you've moved to. Skipped when the payment is no longer pending, or when it's
/// turned off (debug menu, Friend's side; UI tests launch with `-autoApprove NO`).
enum DebugAutoApprover {
    static func install(on store: LedgerStore) {
        store.observeChanges { [weak store] old, new in
            for payment in PaymentApprovals.recorded(from: old, to: new) {
                Task {
                    try? await Task.sleep(for: DebugState.autoApproveDelay)
                    guard DebugState.autoApprovesPayments, let store,
                          store.ledger.payment(payment.id)?.status == .pending else { return }
                    try? store.confirmPayment(payment.id)
                }
            }
        }
    }
}
#endif
