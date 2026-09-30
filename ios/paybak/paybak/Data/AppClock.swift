import Foundation
import Observation

/// The app's clock (app-architecture §3.8). Real by default; debug hooks pin it (`-now`, scenarios,
/// the debug menu) so dates match Figma. Every time-dependent computation reads `now` / `today`
/// from here, never `Date()`.
@Observable
final class AppClock {
    /// The pinned moment, or nil for real time.
    private(set) var pinned: Date?
    let calendar: Calendar

    init(pinned: Date? = nil, calendar: Calendar = .autoupdatingCurrent) {
        self.pinned = pinned
        self.calendar = calendar
    }

    var now: Date { pinned ?? Date() }
    var today: LocalDay { LocalDay(now, calendar: calendar) }
    var isPinned: Bool { pinned != nil }

    /// Pins the clock (or returns to real time with nil).
    func pin(_ moment: Date?) {
        pinned = moment
    }
}
