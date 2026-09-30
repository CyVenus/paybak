import Foundation

/// The Home greeting: "Good morning/afternoon/evening, {first name}" from the local time
/// (05:00–11:59 morning, 12:00–16:59 afternoon, otherwise evening).
enum Greeting {
    static func text(firstName: String, at date: Date = .now, calendar: Calendar = .current) -> String {
        let salutation = "Good \(partOfDay(at: date, calendar: calendar))"
        return firstName.isEmpty ? salutation : "\(salutation), \(firstName)"
    }

    private static func partOfDay(at date: Date, calendar: Calendar) -> String {
        switch calendar.component(.hour, from: date) {
        case 5..<12: "morning"
        case 12..<17: "afternoon"
        default: "evening"
        }
    }
}
