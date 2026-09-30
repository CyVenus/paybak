import Foundation

/// A calendar date without a time (an expense's date, due dates, settle-by, installment dues). It is
/// the same day wherever the device is, and encodes as `yyyy-MM-dd` (domain.md §0). Day arithmetic is
/// proleptic Gregorian, so it needs no calendar; only converting to and from moments does.
nonisolated struct LocalDay: Hashable, Comparable, Sendable, CustomStringConvertible {
    let year: Int
    let month: Int
    let day: Int

    init(year: Int, month: Int, day: Int) {
        self.year = year
        self.month = month
        self.day = day
    }

    /// The local day of a moment.
    init(_ date: Date, calendar: Calendar) {
        let parts = calendar.dateComponents([.year, .month, .day], from: date)
        self.init(year: parts.year ?? 1970, month: parts.month ?? 1, day: parts.day ?? 1)
    }

    /// Parses `yyyy-MM-dd`.
    init?(string: String) {
        let parts = string.split(separator: "-").compactMap { Int($0) }
        guard parts.count == 3, (1...12).contains(parts[1]), (1...31).contains(parts[2]) else { return nil }
        self.init(year: parts[0], month: parts[1], day: parts[2])
    }

    /// `yyyy-MM-dd`.
    var description: String {
        String(format: "%04d-%02d-%02d", year, month, day)
    }

    /// `yyyyMMdd`, for deterministic record ids.
    var compact: String {
        String(format: "%04d%02d%02d", year, month, day)
    }

    static func < (lhs: LocalDay, rhs: LocalDay) -> Bool {
        (lhs.year, lhs.month, lhs.day) < (rhs.year, rhs.month, rhs.day)
    }

    // MARK: Arithmetic

    func adding(days: Int) -> LocalDay {
        LocalDay(ordinal: ordinal + days)
    }

    /// Whole days from `self` to `other` (positive when `other` is later).
    func days(to other: LocalDay) -> Int {
        other.ordinal - ordinal
    }

    /// Monday = 0 … Sunday = 6 (Python's `weekday()`).
    var weekday: Int {
        // 1970-01-01 was a Thursday (3).
        ((ordinal % 7) + 7 + 3) % 7
    }

    var firstOfMonth: LocalDay { LocalDay(year: year, month: month, day: 1) }

    var lastOfMonth: LocalDay { LocalDay(year: year, month: month, day: Self.daysIn(month: month, year: year)) }

    /// The same `day` of the month `months` later, clamped to that month's last day (31 Jan + 1 → 28/29
    /// Feb). Always pass the original anchor day so later months get it back (domain.md §3 addMonths).
    func adding(months: Int, day wantedDay: Int? = nil) -> LocalDay {
        let index = year * 12 + (month - 1) + months
        let newYear = Int((Double(index) / 12).rounded(.down))
        let newMonth = index - newYear * 12 + 1
        let clamped = min(wantedDay ?? day, Self.daysIn(month: newMonth, year: newYear))
        return LocalDay(year: newYear, month: newMonth, day: clamped)
    }

    /// This date in another year, clamped (29 Feb → 28 Feb).
    func replacing(year newYear: Int) -> LocalDay {
        LocalDay(year: newYear, month: month, day: min(day, Self.daysIn(month: month, year: newYear)))
    }

    // MARK: Moments

    /// The moment this day starts in `calendar`'s time zone.
    func start(in calendar: Calendar) -> Date {
        moment(hour: 0, minute: 0, in: calendar)
    }

    /// This day at a local wall-clock time.
    func moment(hour: Int, minute: Int, in calendar: Calendar) -> Date {
        let parts = DateComponents(year: year, month: month, day: day, hour: hour, minute: minute)
        return calendar.date(from: parts) ?? .distantPast
    }

    // MARK: Private

    static func daysIn(month: Int, year: Int) -> Int {
        switch month {
        case 2: isLeap(year) ? 29 : 28
        case 4, 6, 9, 11: 30
        default: 31
        }
    }

    private static func isLeap(_ year: Int) -> Bool {
        (year % 4 == 0 && year % 100 != 0) || year % 400 == 0
    }

    /// Days since 1970-01-01 (Howard Hinnant's days_from_civil).
    private var ordinal: Int {
        let y = month <= 2 ? year - 1 : year
        let era = (y >= 0 ? y : y - 399) / 400
        let yearOfEra = y - era * 400
        let dayOfYear = (153 * (month + (month > 2 ? -3 : 9)) + 2) / 5 + day - 1
        let dayOfEra = yearOfEra * 365 + yearOfEra / 4 - yearOfEra / 100 + dayOfYear
        return era * 146_097 + dayOfEra - 719_468
    }

    /// civil_from_days.
    private init(ordinal: Int) {
        let z = ordinal + 719_468
        let era = (z >= 0 ? z : z - 146_096) / 146_097
        let dayOfEra = z - era * 146_097
        let yearOfEra = (dayOfEra - dayOfEra / 1460 + dayOfEra / 36524 - dayOfEra / 146_096) / 365
        let dayOfYear = dayOfEra - (365 * yearOfEra + yearOfEra / 4 - yearOfEra / 100)
        let mp = (5 * dayOfYear + 2) / 153
        let day = dayOfYear - (153 * mp + 2) / 5 + 1
        let month = mp < 10 ? mp + 3 : mp - 9
        self.init(year: yearOfEra + era * 400 + (month <= 2 ? 1 : 0), month: month, day: day)
    }
}

nonisolated extension LocalDay: Codable {
    init(from decoder: Decoder) throws {
        let container = try decoder.singleValueContainer()
        let text = try container.decode(String.self)
        guard let day = LocalDay(string: text) else {
            throw DecodingError.dataCorruptedError(in: container, debugDescription: "Expected yyyy-MM-dd, got \(text)")
        }
        self = day
    }

    func encode(to encoder: Encoder) throws {
        var container = encoder.singleValueContainer()
        try container.encode(description)
    }
}
