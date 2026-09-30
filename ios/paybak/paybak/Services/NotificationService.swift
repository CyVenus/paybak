import UserNotifications
import os

// Lane A fills this service (scheduling from the store's `revision`, the reminder schedule, the
// 60-item cap, permission status) and keeps these names. M2 posts right away, for the debug menu and
// the lock-screen start screens.
/// Local notifications: reminders at the schedule's time, claims with Confirm / Not received,
/// overdue alerts, the monthly summary and recurring drafts. Each carries a deep link in `link`.
enum NotificationService {
    static let linkKey = "link"
    static let confirmCategory = "PAYMENT_CONFIRM"
    static let confirmAction = "CONFIRM"
    static let notReceivedAction = "NOT_RECEIVED"

    private static let log = Logger(subsystem: "app.paybak.paybak", category: "Notifications")

    /// "Payment to confirm" with Confirm (background) and Not received (opens the app).
    static var categories: Set<UNNotificationCategory> {
        let confirm = UNNotificationAction(identifier: confirmAction, title: "Confirm", options: [])
        let notReceived = UNNotificationAction(identifier: notReceivedAction, title: "Not received", options: [.foreground])
        return [UNNotificationCategory(identifier: confirmCategory, actions: [confirm, notReceived], intentIdentifiers: [])]
    }

    /// Re-schedules every upcoming notification for the ledger's open items (after each change).
    static func reschedule(for books: Books) async {}

    /// Posts "Payment to confirm" for a friend's claim (screens-activity §7.1).
    static func postClaim(_ claim: PendingClaim, after delay: TimeInterval = 1) async {
        let content = UNMutableNotificationContent()
        content.title = "Payment to confirm"
        content.body = "\(claim.title) for \(claim.purpose)."
        content.categoryIdentifier = confirmCategory
        content.userInfo = [linkKey: DeepLink.claim(claim.payment.id, notReceived: false).text]
        await post(content, id: "claim-\(claim.payment.id)", after: delay)
    }

    /// Posts an inbox item as an OS notification (screens-activity §7.2–7.3).
    static func post(_ row: InboxRow, link: DeepLink?, after delay: TimeInterval = 1) async {
        let content = UNMutableNotificationContent()
        content.title = row.title
        content.body = row.body
        if let link { content.userInfo = [linkKey: link.text] }
        await post(content, id: row.id, after: delay)
    }

    private static func post(_ content: UNMutableNotificationContent, id: String, after delay: TimeInterval) async {
        content.sound = .default
        let trigger = UNTimeIntervalNotificationTrigger(timeInterval: max(delay, 1), repeats: false)
        do {
            try await UNUserNotificationCenter.current().add(UNNotificationRequest(identifier: id, content: content, trigger: trigger))
        } catch {
            log.error("Could not post \(id, privacy: .public): \(String(describing: error), privacy: .public)")
        }
    }
}

extension DeepLink {
    /// Where an inbox item leads (domain.md §6.8).
    init?(_ item: InboxItem) {
        let p = item.params
        switch item.type {
        case .paymentReminder:
            guard let person = p.personId else { return nil }
            self = .recordPayment(to: person, amount: p.amount, context: p.groupId.map { .group($0) })
        case .monthlySummary:
            self = .insights(p.year.flatMap { year in p.month.map { YearMonth(year: year, month: $0) } })
        case .paymentConfirmed, .paymentNotReceived:
            guard let id = p.paymentId else { return nil }
            self = .payment(id)
        case .paymentOverdue:
            guard let person = p.personId else { return nil }
            self = .remind(person)
        case .newExpenseInGroup, .expenseFlagged, .flagResolved:
            guard let id = p.expenseId else { return nil }
            self = .expense(id)
        }
    }
}
