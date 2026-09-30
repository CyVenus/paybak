import Foundation

/// The due-date quick chips of Add expense and Lend money (add-expense §3.7, domain.md §12 #14).
nonisolated enum QuickDue: String, CaseIterable, Sendable {
    case tomorrow
    case weekend
    case nextWeek

    var title: String {
        switch self {
        case .tomorrow: "Tomorrow"
        case .weekend: "This weekend"
        case .nextWeek: "Next week"
        }
    }

    /// Tomorrow = today + 1; This weekend = the coming Sunday (tomorrow on a Saturday, today on a
    /// Sunday); Next week = today + 7.
    func day(from today: LocalDay) -> LocalDay {
        switch self {
        case .tomorrow: today.adding(days: 1)
        case .weekend: today.adding(days: 6 - today.weekday)
        case .nextWeek: today.adding(days: 7)
        }
    }

    /// The chip a date matches, if any (a picked date shows no chip unless it equals one).
    static func matching(_ day: LocalDay?, today: LocalDay) -> QuickDue? {
        guard let day else { return nil }
        return allCases.first { $0.day(from: today) == day }
    }
}

nonisolated extension Format {
    /// The date sheets' relative part: "today", "tomorrow", "in 4 days", "yesterday", "2 days ago".
    static func relativeDays(_ day: LocalDay, today: LocalDay) -> String {
        switch today.days(to: day) {
        case 0: "today"
        case 1: "tomorrow"
        case -1: "yesterday"
        case let days where days > 1: "in \(days) days"
        case let days: "\(-days) days ago"
        }
    }

    /// The form's date chip: "Today", "Yesterday" or "Mon 28 Sep".
    static func dateChip(_ day: LocalDay, today: LocalDay) -> String {
        if day == today { return "Today" }
        if day == today.adding(days: -1) { return "Yesterday" }
        return day.year == today.year ? Self.day(day) : "\(Self.day(day)) \(day.year)"
    }

    /// "Sun 4 Oct", with the year when it isn't this year.
    static func dayWithYear(_ day: LocalDay, today: LocalDay) -> String {
        day.year == today.year ? Self.day(day) : "\(Self.day(day)) \(day.year)"
    }
}
