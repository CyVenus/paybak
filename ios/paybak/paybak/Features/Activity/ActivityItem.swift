import SwiftUI

/// A timeline row ready to draw (screens-activity §3.5): the event's copy, its leading art, the
/// unsigned amount (black when you paid or were paid, gray otherwise) or a badge, and where a tap goes.
struct ActivityItem: Identifiable {
    let id: String
    let at: Date
    let leading: PBActivityRow.Leading
    let title: String
    let subtitle: String
    let trailing: PBActivityRow.Trailing
    /// nil when the row has nowhere to go.
    let route: Route?
}

/// A day group of rows: "Today", "Yesterday", then "Mon 28 Sep".
struct ActivityDay: Identifiable {
    let day: LocalDay
    let header: String
    var items: [ActivityItem]

    var id: LocalDay { day }
}

extension Books {
    /// The timeline's day groups as rows (§3.4).
    func activityDays(_ timeline: [TimelineDay]) -> [ActivityDay] {
        timeline.map { day in
            ActivityDay(day: day.day, header: day.header, items: day.events.map(activityItem))
        }
    }

    /// The timeline narrowed to a person, a group, a project (with its parts' history) or a
    /// category's expenses in a month (activity §3.9, projects §3.7, insights drill-downs).
    func activityLog(_ filter: ActivityFilter) -> [ActivityDay] {
        let items = timeline().filter { matches($0, filter) }.map(activityItem)
        var days: [ActivityDay] = []
        for item in items {
            let day = day(of: item.at)
            if days.last?.day == day {
                days[days.count - 1].items.append(item)
            } else {
                days.append(ActivityDay(day: day, header: Format.dayHeader(day, today: today), items: [item]))
            }
        }
        return days
    }

    /// The log's header: "Rohan Verma · History", "Build a Drone · History", "Food · September".
    func activityLogTitle(_ filter: ActivityFilter) -> String {
        switch filter {
        case .person(let id): "\(ledger.person(id)?.name ?? firstName(id)) · History"
        case .group(let id), .project(let id): "\(groupName(id)) · History"
        case .category(let category, let month): "\(category.name) · \(Format.month(month.month))"
        }
    }

    func activityItem(_ event: TimelineEvent) -> ActivityItem {
        let trailing: PBActivityRow.Trailing = if let amount = event.amount {
            .amount(amount, date: nil, isIncoming: event.isPrimary)
        } else if let badge = event.badge {
            .badge(badge)
        } else {
            .none
        }
        let (leading, route) = presentation(of: event.kind)
        return ActivityItem(id: event.id, at: event.at, leading: leading, title: event.title, subtitle: event.subtitle,
                            trailing: trailing, route: route)
    }

    // MARK: Presentation

    /// Expense rows show the category, payment and loan rows the friend; reminders open the debt they
    /// were about (an expense, a loan, else the group or project), drafts their group's recurring
    /// rules, parts their project (with its icon).
    private func presentation(of kind: TimelineEvent.Kind) -> (PBActivityRow.Leading, Route?) {
        switch kind {
        case .expenseAdded(let id), .expenseEdited(let id):
            return (.icon((ledger.expense(id)?.category ?? .other).pbIcon), .expense(id))
        case .payment(let id):
            return (avatar(ledger.payment(id)?.otherPartyId), .payment(id))
        case .reminderSent(let id):
            let reminder = ledger.reminders.first { $0.id == id }
            let route: Route? = if let expenseId = reminder?.expenseId {
                .expense(expenseId)
            } else if let loanId = reminder?.loanId {
                .loan(loanId)
            } else if let groupId = reminder?.groupId {
                ledger.group(groupId)?.isProject == true ? Route.project(groupId) : Route.group(groupId)
            } else {
                nil
            }
            return (.icon(.bell), route)
        case .draftCreated(let id):
            let rule = ledger.draft(id).flatMap { ledger.rule($0.ruleId) }
            return (.icon(rule.map(icon) ?? ExpenseCategory.other.pbIcon), rule?.groupId.map { .recurring($0) })
        case .loanAdded(let id):
            return (avatar(ledger.loan(id)?.friendId), .loan(id))
        case .componentChanged(let id, _):
            let project = ledger.component(id).flatMap { ledger.group($0.projectId) }
            return (.icon(project?.pbIcon ?? .package), project.map { .project($0.id) })
        }
    }

    private func avatar(_ person: PersonID?) -> PBActivityRow.Leading {
        .avatar(person.flatMap { ledger.person($0)?.avatarContent } ?? .icon(.profile))
    }

    /// A recurring rule's icon: its utility where a word of the title names one (Cooking gas →
    /// flame; Wi-Fi, internet, broadband), else its category's.
    private func icon(_ rule: RecurringRule) -> PBIcon {
        let words = Set(rule.title.lowercased()
            .split { !(($0 >= "a" && $0 <= "z") || $0 == "-") }
            .map(String.init))
        if words.contains("gas") { return .flame }
        if !words.isDisjoint(with: ["wi-fi", "wifi", "internet", "broadband"]) { return .wiFi }
        return rule.category.pbIcon
    }

    // MARK: Filters

    private func matches(_ event: TimelineEvent, _ filter: ActivityFilter) -> Bool {
        switch filter {
        case .person(let person): people(in: event.kind).contains(person)
        case .group(let id), .project(let id): groupId(of: event.kind) == id
        case .category(let category, let month):
            switch event.kind {
            case .expenseAdded(let id), .expenseEdited(let id):
                if let expense = ledger.expense(id) {
                    expense.category == category && YearMonth(expense.date) == month
                } else {
                    false
                }
            default:
                false
            }
        }
    }

    private func people(in kind: TimelineEvent.Kind) -> Set<PersonID> {
        switch kind {
        case .expenseAdded(let id), .expenseEdited(let id):
            guard let expense = ledger.expense(id) else { return [] }
            return Set(expense.participantIds + expense.payers.map(\.personId))
        case .payment(let id): return Set(ledger.payment(id).map { [$0.otherPartyId] } ?? [])
        case .reminderSent(let id): return Set(ledger.reminders.filter { $0.id == id }.map(\.toId))
        case .draftCreated: return []
        case .loanAdded(let id): return Set(ledger.loan(id).map { [$0.friendId] } ?? [])
        case .componentChanged(_, let by): return [by]
        }
    }

    private func groupId(of kind: TimelineEvent.Kind) -> GroupID? {
        switch kind {
        case .expenseAdded(let id), .expenseEdited(let id): return ledger.expense(id)?.groupId
        case .payment(let id): return ledger.payment(id)?.groupId
        case .reminderSent(let id):
            let reminder = ledger.reminders.first { $0.id == id }
            return reminder?.groupId ?? reminder?.expenseId.flatMap { ledger.expense($0)?.groupId }
        case .draftCreated(let id): return ledger.draft(id).flatMap { ledger.rule($0.ruleId)?.groupId }
        case .loanAdded: return nil
        case .componentChanged(let id, _): return ledger.component(id)?.projectId
        }
    }
}
