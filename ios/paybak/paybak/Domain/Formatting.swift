import Foundation

/// Date and time copy (domain.md §3, verify.py §3). English (en-GB) names from fixed tables, so labels
/// never depend on the device language. Views format through this, never a platform formatter.
nonisolated enum Format {
    static let monthNames = ["January", "February", "March", "April", "May", "June", "July", "August",
                             "September", "October", "November", "December"]
    /// Monday first, matching `LocalDay.weekday`.
    static let weekdayNames = ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]

    static func shortMonth(_ month: Int) -> String { String(monthNames[month - 1].prefix(3)) }
    static func shortWeekday(_ day: LocalDay) -> String { String(weekdayNames[day.weekday].prefix(3)) }
    static func weekday(_ day: LocalDay) -> String { weekdayNames[day.weekday] }
    static func month(_ month: Int) -> String { monthNames[month - 1] }

    /// "Mon 28 Sep".
    static func day(_ day: LocalDay) -> String {
        "\(shortWeekday(day)) \(day.day) \(shortMonth(day.month))"
    }

    /// "26 Sep".
    static func short(_ day: LocalDay) -> String {
        "\(day.day) \(shortMonth(day.month))"
    }

    /// "9:12 pm".
    static func time(_ moment: Date, calendar: Calendar) -> String {
        let parts = calendar.dateComponents([.hour, .minute], from: moment)
        let hour = parts.hour ?? 0
        let minute = parts.minute ?? 0
        let clock = hour % 12 == 0 ? 12 : hour % 12
        return "\(clock):\(minute < 10 ? "0" : "")\(minute) \(hour < 12 ? "am" : "pm")"
    }

    /// List rows: Today · Yesterday · 26 Sep · 26 Sep 2025.
    static func rowDate(_ day: LocalDay, today: LocalDay) -> String {
        if day == today { return "Today" }
        if day == today.adding(days: -1) { return "Yesterday" }
        return day.year == today.year ? short(day) : "\(short(day)) \(day.year)"
    }

    /// Timeline day groups: Today · Yesterday · Mon 28 Sep.
    static func dayHeader(_ day: LocalDay, today: LocalDay) -> String {
        if day == today || day == today.adding(days: -1) { return rowDate(day, today: today) }
        return day.year == today.year ? Self.day(day) : "\(Self.day(day)) \(day.year)"
    }

    /// Due Fri (within 6 days) · Due 12 Oct · Overdue 3 days · Overdue 1 day.
    static func dueBadge(_ due: LocalDay, today: LocalDay) -> String {
        let days = today.days(to: due)
        if days < 0 { return "Overdue \(-days) day" + (days == -1 ? "" : "s") }
        return days <= 6 ? "Due \(shortWeekday(due))" : "Due \(short(due))"
    }

    /// "Due Sun 4 Oct" (lists and detail rows).
    static func dueLabel(_ due: LocalDay) -> String {
        "Due \(day(due))"
    }

    /// Loan meta: Today · Yesterday · `EEE d MMM` under 60 days · `d MMM`; + year when it differs.
    static func loanMetaDate(_ day: LocalDay, today: LocalDay) -> String {
        let age = day.days(to: today)
        if age == 0 || age == 1 { return rowDate(day, today: today) }
        let text = age < 60 ? Self.day(day) : short(day)
        return day.year == today.year ? text : "\(text) \(day.year)"
    }

    /// 21–25 Sep · 28 Sep – 2 Oct.
    static func dateRange(_ start: LocalDay, _ end: LocalDay) -> String {
        if start == end { return short(start) }
        if start.year == end.year, start.month == end.month {
            return "\(start.day)–\(end.day) \(shortMonth(end.month))"
        }
        return "\(short(start)) – \(short(end))"
    }

    /// "1 Sep – 30 Sep 2026" (the Export range line).
    static func fullRange(_ start: LocalDay, _ end: LocalDay) -> String {
        "\(short(start)) – \(short(end)) \(end.year)"
    }

    /// Recently deleted: whole days left of the 30-day retention ("24 days left", "1 day left").
    static func daysLeft(deletedOn day: LocalDay, today: LocalDay) -> String {
        let left = max(0, today.days(to: day.adding(days: Ledger.deletedRetentionDays)))
        return "\(left) day\(left == 1 ? "" : "s") left"
    }

    /// Inbox rows: the time today, otherwise the row date.
    static func inboxTime(_ moment: Date, today: LocalDay, calendar: Calendar) -> String {
        let day = LocalDay(moment, calendar: calendar)
        return day == today ? time(moment, calendar: calendar) : rowDate(day, today: today)
    }

    /// "A", "A and B", "A, B and C".
    static func joinedNames(_ names: [String]) -> String {
        guard names.count > 1 else { return names.first ?? "" }
        return names.dropLast().joined(separator: ", ") + " and " + names[names.count - 1]
    }

    /// "1st", "2nd", "28th" (recurring rule subtitles).
    static func ordinal(_ number: Int) -> String {
        let suffix = switch (number % 10, number % 100) {
        case (_, 11...13): "th"
        case (1, _): "st"
        case (2, _): "nd"
        case (3, _): "rd"
        default: "th"
        }
        return "\(number)\(suffix)"
    }
}
