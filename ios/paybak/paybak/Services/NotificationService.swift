import UserNotifications
import os

/// Local notifications (screens-activity §7, domain.md §10.1): payment reminders at the schedule's
/// time, overdue alerts, the month-end summary and recurring drafts are scheduled ahead after every
/// ledger change (`reschedule`); a friend's claim posts "Payment to confirm" with Confirm / Not
/// received at once. Each carries its deep link in `link`; `AppDelegate` handles taps and actions.
enum NotificationService {
    static let linkKey = "link"
    static let confirmCategory = "PAYMENT_CONFIRM"
    static let confirmAction = "CONFIRM"
    static let notReceivedAction = "NOT_RECEIVED"

    private static let log = Logger(subsystem: "app.paybak.paybak", category: "Notifications")
    /// Scheduled requests carry this prefix (`UpcomingAlert.id`), so stale ones can be told apart.
    private static let alertPrefix = "alert-"

    /// "Payment to confirm" with Confirm (background) and Not received (opens the app).
    static var categories: Set<UNNotificationCategory> {
        let confirm = UNNotificationAction(identifier: confirmAction, title: "Confirm", options: [])
        let notReceived = UNNotificationAction(identifier: notReceivedAction, title: "Not received", options: [.foreground])
        return [UNNotificationCategory(identifier: confirmCategory, actions: [confirm, notReceived], intentIdentifiers: [])]
    }

    /// Whether the system lets Paybak post (the Setup 4 or Settings permission).
    static func isAuthorized() async -> Bool {
        switch await UNUserNotificationCenter.current().notificationSettings().authorizationStatus {
        case .authorized, .provisional, .ephemeral: true
        default: false
        }
    }

    /// Re-schedules every upcoming alert for the ledger as it is now (after each change): adds or
    /// replaces the nearest 60 and removes the ones that no longer apply (a debt was settled, a toggle
    /// was turned off).
    static func reschedule(for books: Books) async {
        let center = UNUserNotificationCenter.current()
        let alerts = await Task.detached(priority: .utility) { books.upcomingAlerts() }.value
        let wanted = Set(alerts.map(\.id))
        let stale = await center.pendingNotificationRequests().map(\.identifier)
            .filter { $0.hasPrefix(alertPrefix) && !wanted.contains($0) }
        center.removePendingNotificationRequests(withIdentifiers: stale)
        guard !Task.isCancelled, await isAuthorized() else { return }
        for alert in alerts {
            let components = books.calendar.dateComponents([.year, .month, .day, .hour, .minute], from: alert.fireAt)
            let trigger = UNCalendarNotificationTrigger(dateMatching: components, repeats: false)
            do {
                try await center.add(UNNotificationRequest(identifier: alert.id, content: content(for: alert, in: books), trigger: trigger))
            } catch {
                log.error("Could not schedule \(alert.id, privacy: .public): \(String(describing: error), privacy: .public)")
            }
        }
    }

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

    /// Posts an upcoming alert now instead of at its moment (debug "Deliver the next notification").
    static func postEarly(_ alert: UpcomingAlert, in books: Books, after delay: TimeInterval) async {
        await post(content(for: alert, in: books), id: "early-\(alert.id)", after: delay)
    }

    private static func content(for alert: UpcomingAlert, in books: Books) -> UNMutableNotificationContent {
        let content = UNMutableNotificationContent()
        let link: DeepLink?
        switch alert.content {
        case .inbox(let item):
            (content.title, content.body) = books.inboxText(item)
            link = DeepLink(item)
        case .draft(let draft):
            (content.title, content.body) = books.draftAlertText(draft)
            link = .recurringDraft(draft.id)
        }
        if let link { content.userInfo = [linkKey: link.text] }
        content.sound = .default
        return content
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
