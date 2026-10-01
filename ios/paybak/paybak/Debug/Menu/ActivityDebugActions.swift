#if DEBUG
import Foundation

/// Lane A's Activity section of the debug menu (app-architecture §3.10): post the local
/// notifications now instead of waiting for 9:00 pm, the month's last evening or the day after a due
/// date. Each posts the latest inbox item of its kind (a demo has them all).
enum ActivityDebugActions {
    /// Long enough to lock the device first.
    private static let delay: TimeInterval = 5

    static func actions(_ ledger: Ledger) -> [DebugAction] {
        [
            postLatest("Fire the Kabir reminder now", detail: "The latest Payment reminder", type: .paymentReminder),
            postLatest("Post the monthly summary now", type: .monthlySummary),
            postLatest("Post Rohan’s overdue alert", detail: "The latest Payment overdue", type: .paymentOverdue),
            DebugAction(title: "Deliver the next notification in 5 s", detail: "A pending claim to you, else the latest inbox item") { context in
                let store = context.ledgerStore
                // Pending claims come newest first.
                if let claim = store.snapshot.home.pendingClaims.first {
                    Task { await NotificationService.postClaim(claim, after: delay) }
                } else if let row = store.snapshot.inbox.max(by: { $0.item.createdAt < $1.item.createdAt }) {
                    Task { await NotificationService.post(row, link: DeepLink(row.item, in: store.ledger), after: delay) }
                }
            },
        ]
    }

    /// Posts the newest inbox item of `type`.
    private static func postLatest(_ title: String, detail: String? = nil, type: InboxItem.Kind) -> DebugAction {
        DebugAction(title: title, detail: detail) { context in
            let store = context.ledgerStore
            let rows = store.snapshot.inbox.filter { $0.item.type == type }
            guard let row = rows.max(by: { $0.item.createdAt < $1.item.createdAt }) else { return }
            Task { await NotificationService.post(row, link: DeepLink(row.item, in: store.ledger)) }
        }
    }
}
#endif
