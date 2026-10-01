import Foundation
import RevenueCat

/// What Pro unlocks, in the order Welcome lists it.
struct ProFeature: Identifiable {
    let icon: PBIcon
    let title: String
    let subtitle: String

    var id: String { title }

    static let all = [
        ProFeature(icon: .sparkles, title: "AI assistant", subtitle: "Ask about balances or add expenses by chat"),
        ProFeature(icon: .camera, title: "Receipt scanning", subtitle: "Snap a bill and split it item by item"),
        ProFeature(icon: .chart, title: "Insights", subtitle: "See where shared money goes each month"),
        ProFeature(icon: .repeat, title: "Recurring expenses", subtitle: "Rent and bills that add themselves"),
        ProFeature(icon: .download, title: "PDF/CSV export", subtitle: "Keep a clean copy of your records"),
    ]
}

extension EntitlementInfo {
    /// The Welcome status line (§3 "Dynamic text") for a store subscription: the trial's end, the next
    /// renewal, or the last day of a cancelled plan. Nil for a lifetime unlock (no expiry).
    func statusLine(calendar: Calendar) -> String? {
        guard let expirationDate else { return nil }
        let day = Format.day(LocalDay(expirationDate, calendar: calendar))
        if periodType == .trial {
            return "Your free trial ends \(day)."
        }
        return willRenew ? "Renews \(day)." : "Pro until \(day)."
    }
}

extension Entitlement {
    /// The debug mock entitlement's status line, nil on the free plan: the trial's end while it runs,
    /// otherwise the next renewal after `today` with the plan's price ("Renews Thu 7 Oct 2027.
    /// ₹799/year.", "Your subscription renews Fri 30 Oct. ₹99/month.").
    func statusLine(today: LocalDay, calendar: Calendar) -> String? {
        guard isPro else { return nil }
        if let trialEndsAt, trialEndsAt >= today {
            return "Your free trial ends \(Format.day(trialEndsAt)). Then ₹799/year."
        }
        let isMonthly = period == .monthly
        let step = isMonthly ? 1 : 12
        var renewal = trialEndsAt ?? since.map { LocalDay($0, calendar: calendar) } ?? today
        var months = 0
        while renewal.adding(months: months) <= today {
            months += step
        }
        renewal = renewal.adding(months: months)
        return isMonthly
            ? "Your subscription renews \(Format.day(renewal)). ₹99/month."
            : "Renews \(Format.day(renewal)). ₹799/year."
    }
}
