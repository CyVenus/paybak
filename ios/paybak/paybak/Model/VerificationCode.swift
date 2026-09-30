import Foundation

/// The sign-in code. There's no backend yet, so nothing is sent and `000000` is the one correct
/// code (flow.md).
enum VerificationCode {
    /// "Resend code" unlocks this long after a code was sent.
    static let resendDelay: TimeInterval = 30

    static func isCorrect(_ code: String) -> Bool {
        code == "000000"
    }
}

/// The Verify screen's resend timer: "Resend code in 0:30" … "0:01", then the Resend button.
struct ResendCountdown: Equatable {
    /// When "Resend code" becomes available.
    let unlocksAt: Date

    /// A countdown that starts now (a code was just sent).
    static func started(at date: Date = .now) -> ResendCountdown {
        ResendCountdown(unlocksAt: date.addingTimeInterval(VerificationCode.resendDelay))
    }

    /// Whole seconds left, rounded up, so it reads 30 right after sending and reaches 0 exactly
    /// at `unlocksAt`.
    func secondsLeft(at date: Date) -> Int {
        max(0, Int(unlocksAt.timeIntervalSince(date).rounded(.up)))
    }

    /// When the shown number next changes, or nil once the countdown has finished.
    func nextTick(after date: Date) -> Date? {
        let left = secondsLeft(at: date)
        guard left > 0 else { return nil }
        return unlocksAt.addingTimeInterval(-TimeInterval(left - 1))
    }

    /// "Resend code in 0:24" (m:ss).
    static func label(secondsLeft: Int) -> String {
        let seconds = String(format: "%02d", secondsLeft % 60)
        return "Resend code in \(secondsLeft / 60):\(seconds)"
    }
}
