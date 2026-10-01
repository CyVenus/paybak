import Foundation

/// Something the scheduler will tell you later (domain.md §10.1): an inbox item `tick` will create
/// (a payment reminder, an overdue alert, the month-end summary) or the draft of a variable rule,
/// at the moment it's due. The app schedules each as an OS notification ahead of time, because it's
/// usually closed when the moment comes.
nonisolated struct UpcomingAlert: Hashable, Sendable, Identifiable {
    enum Content: Hashable, Sendable {
        case inbox(InboxItem)
        case draft(RecurringDraft)
    }

    /// Stable across reschedules: the id of the record it announces.
    var id: String
    var fireAt: Date
    var content: Content
}

nonisolated extension Books {
    /// Alerts due after now and within `days`, nearest first, at most `limit` (iOS keeps 64 pending
    /// requests). Worked out by running `tick` on a copy of the books, so a notification says exactly
    /// what its inbox row will. The push toggles in Settings filter them.
    func upcomingAlerts(days: Int = 21, limit: Int = 60) -> [UpcomingAlert] {
        guard let until = calendar.date(byAdding: .day, value: days, to: now) else { return [] }
        var future = self
        future.ledger.scheduler.cursor = now
        future.tick(until: until)

        let push = ledger.settings.push
        let knownItems = Set(ledger.inbox.map(\.id))
        let items = future.ledger.inbox.filter { item in
            guard !knownItems.contains(item.id), item.createdAt > now else { return false }
            return switch item.type {
            case .paymentReminder: push.reminders
            case .paymentOverdue: push.overdueAlerts
            case .monthlySummary: push.monthlySummary
            default: false
            }
        }
        let knownDrafts = Set(ledger.drafts.map(\.id))
        let drafts = push.reminders ? future.ledger.drafts.filter { !knownDrafts.contains($0.id) && $0.createdAt > now } : []

        let alerts = items.map { UpcomingAlert(id: "alert-\($0.id)", fireAt: $0.createdAt, content: .inbox($0)) }
            + drafts.map { UpcomingAlert(id: "alert-\($0.id)", fireAt: $0.createdAt, content: .draft($0)) }
        return Array(alerts.sorted { $0.fireAt != $1.fireAt ? $0.fireAt < $1.fireAt : $0.id < $1.id }.prefix(limit))
    }

    /// A variable rule's draft as a notification: "Cooking gas needs an amount" /
    /// "Enter this month’s amount for Flat 302." (proposal; the inbox has no draft row).
    func draftAlertText(_ draft: RecurringDraft) -> (title: String, body: String) {
        guard let rule = ledger.rule(draft.ruleId) else { return ("A recurring expense needs an amount", "") }
        let place = rule.groupId.map { " for \(groupName($0))" } ?? ""
        return ("\(rule.title) needs an amount", "Enter this \(Self.periodWord(rule.frequency))’s amount\(place).")
    }

    private static func periodWord(_ frequency: RecurrenceFrequency) -> String {
        switch frequency {
        case .weekly: "week"
        case .monthly: "month"
        case .yearly: "year"
        }
    }
}
