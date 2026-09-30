import Foundation
@testable import paybak

/// Loads the bundled demo like verify.py's `load(*scenarios, now:, anchor:)`: Figma parity by default
/// (Wed 30 Sep 2026, 21:15), in a fixed time zone so the tests don't depend on the machine's.
enum DemoFixture {
    static let calendar: Calendar = {
        var calendar = Calendar(identifier: .gregorian)
        calendar.timeZone = TimeZone(identifier: "Asia/Kolkata")!
        return calendar
    }()

    static let seed: DemoSeed = {
        do {
            return try DemoSeed.bundled()
        } catch {
            fatalError("The demo must be bundled: \(error)")
        }
    }()

    static let figmaDay = DemoSeed.figmaDay
    static let figmaNow = DemoSeed.figmaNow(calendar: calendar)

    static func moment(_ year: Int, _ month: Int, _ day: Int, _ hour: Int = 0, _ minute: Int = 0) -> Date {
        LocalDay(year: year, month: month, day: day).moment(hour: hour, minute: minute, in: calendar)
    }

    static func day(_ year: Int, _ month: Int, _ day: Int) -> LocalDay {
        LocalDay(year: year, month: month, day: day)
    }

    static func load(_ scenarios: String..., now: Date = figmaNow, anchor: LocalDay = figmaDay) -> Books {
        do {
            return try seed.load(anchor: anchor, now: now, calendar: calendar, scenarios: scenarios).books
        } catch {
            fatalError("The demo must load: \(error)")
        }
    }
}

/// Whole rupees → paise.
func rupees(_ value: Double) -> Int64 {
    Int64((value * 100).rounded())
}

extension Books {
    func firstNames(_ nets: [PersonID: Int64]) -> [String: Int64] {
        Dictionary(uniqueKeysWithValues: nets.map { (firstName($0.key), $0.value / 100) })
    }
}
