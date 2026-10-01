#if DEBUG
import Foundation

/// Lane A's Notifications section of the debug menu (app-architecture §3.10): posts real local
/// notifications now (lock the device within 5 s to see them on the lock screen).
enum ActivityDebugActions {
    static func actions(_ ledger: Ledger) -> [DebugAction] {
        [
            DebugAction(title: "Post the claim notification", detail: "Esha’s pending ₹700, in 5 s") { context in
                guard let claim = context.ledgerStore.snapshot.home.pendingClaims.first else { return }
                Task { await NotificationService.postClaim(claim, after: 5) }
            },
            post("Fire the Kabir reminder now", type: .paymentReminder),
            post("Post the monthly summary now", type: .monthlySummary),
            post("Post Rohan’s overdue alert", type: .paymentOverdue),
            DebugAction(title: "Deliver the next notification in 5 s", detail: "The nearest scheduled reminder or alert") { context in
                let books = context.ledgerStore.books
                guard let alert = books.upcomingAlerts(limit: 1).first else { return }
                Task { await NotificationService.postEarly(alert, in: books, after: 5) }
            },
        ]
    }

    /// Posts the newest inbox item of `type`.
    private static func post(_ title: String, type: InboxItem.Kind) -> DebugAction {
        DebugAction(title: title) { context in
            guard let row = context.ledgerStore.snapshot.inbox.first(where: { $0.item.type == type }) else { return }
            Task { await NotificationService.post(row, link: DeepLink(row.item, in: context.ledgerStore.ledger)) }
        }
    }
}
#endif
