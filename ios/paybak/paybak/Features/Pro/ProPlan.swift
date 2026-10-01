import Foundation

/// The two Pro plans and their fixed ₹ copy (screens-settings §2 "Plans and prices"; a mock store, so
/// the strings are literal). Yearly is the default and the only plan with the 7-day trial.
enum ProPlan: CaseIterable {
    case yearly
    case monthly

    var period: Entitlement.Period {
        self == .yearly ? .yearly : .monthly
    }

    var name: String { self == .yearly ? "Yearly" : "Monthly" }
    var price: String { self == .yearly ? "₹799/year" : "₹99/month" }
    var detail: String { self == .yearly ? "₹67/month" : "Billed monthly" }
    var badge: String? { self == .yearly ? "Save 33%" : nil }
    var callToAction: String { self == .yearly ? "Start 7-day free trial" : "Subscribe for ₹99/month" }
    var smallPrint: String {
        self == .yearly ? "Then ₹799/year. Cancel anytime in Settings." : "Billed monthly. Cancel anytime in Settings."
    }
}

/// What Pro unlocks, in the paywall's order.
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

extension Entitlement {
    /// The Welcome / status line (§3 "Dynamic text"), nil on the free plan: the trial's end while it
    /// runs, otherwise the next renewal after `today` with the plan's price ("Renews Thu 7 Oct 2027.
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
