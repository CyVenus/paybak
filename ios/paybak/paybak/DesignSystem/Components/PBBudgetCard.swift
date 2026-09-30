import SwiftUI

/// Card / Budget (Figma 145:2106): budget vs spent on a project. "Spent", the Title/1 amount with
/// "of ₹60,000" on its baseline, a large bar, then "87% used" and "₹8,000 left", and optionally a
/// divider with the planned-items line.
/// - On track: the bar shows the planned total as a gray projection.
/// - Over budget: the bar is black up to the budget (marked) and red after it; the stats row becomes
///   the red "₹1,500 over budget" warning, the only red.
/// - Closed: a plain bar and no planned line.
struct PBBudgetCard: View {
    enum Status {
        /// `projected` = (spent + planned) ÷ budget.
        case onTrack(projected: Double?)
        case overBudget(warning: String)
        case closed
    }

    /// "₹52,000".
    let spent: String
    /// "of ₹60,000".
    let budget: String
    /// The black fill: spent ÷ budget, or budget ÷ spent when over budget.
    let progress: Double
    let status: Status
    /// "87% used".
    var percentLabel = ""
    /// "₹8,000 left", or "₹8,000 under budget" once the project is closed.
    var leftLabel = ""
    /// "Planned items bring it to ₹58,000" (not shown when closed).
    var plannedText: String?

    var body: some View {
        VStack(alignment: .leading, spacing: PBSpace.s12) {
            VStack(alignment: .leading, spacing: PBSpace.s2) {
                Text("Spent")
                    .textStyle(.footnote)
                    .foregroundStyle(PBColor.textSecondary)
                HStack(alignment: .firstTextBaseline, spacing: PBSpace.s8) {
                    Text(spent)
                        .textStyle(.title1)
                        .foregroundStyle(PBColor.textPrimary)
                    Text(budget)
                        .textStyle(.headline)
                        .foregroundStyle(PBColor.textSecondary)
                }
                .lineLimit(1)
            }
            bar
            stats
            if let plannedText, !isClosed {
                PBDivider()
                Text(plannedText)
                    .textStyle(.footnote)
                    .foregroundStyle(PBColor.textSecondary)
            }
        }
        .padding(PBLayout.cardPadding)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(PBColor.bgCard, in: .rect(cornerRadius: PBRadius.card))
        .accessibilityElement(children: .combine)
    }

    private var isClosed: Bool {
        if case .closed = status { return true }
        return false
    }

    @ViewBuilder
    private var bar: some View {
        switch status {
        case .onTrack(let projected):
            PBProgressBar(value: progress, projected: projected, size: .large, accessibilityLabel: "Budget used")
        case .overBudget:
            PBProgressBar(value: progress, overFrom: progress, mark: progress, size: .large, accessibilityLabel: "Budget used")
        case .closed:
            PBProgressBar(value: progress, size: .large, accessibilityLabel: "Budget used")
        }
    }

    @ViewBuilder
    private var stats: some View {
        if case .overBudget(let warning) = status {
            HStack(spacing: PBSpace.s6) {
                PBIconView(.alert, size: PBSize.iconSm)
                    .foregroundStyle(PBColor.iconDestructive)
                Text(warning)
                    .textStyle(.subheadline)
                    .foregroundStyle(PBColor.textDestructive)
            }
        } else {
            HStack {
                Text(percentLabel)
                    .textStyle(.subheadline)
                    .foregroundStyle(PBColor.textSecondary)
                Spacer(minLength: PBSpace.s8)
                Text(leftLabel)
                    .textStyle(.subheadline)
                    .foregroundStyle(PBColor.textPrimary)
            }
        }
    }
}

#Preview("PBBudgetCard") {
    ScrollView {
        VStack(spacing: PBSpace.s16) {
            PBBudgetCard(spent: "₹52,000", budget: "of ₹60,000", progress: 52.0 / 60, status: .onTrack(projected: 58.0 / 60), percentLabel: "87% used", leftLabel: "₹8,000 left", plannedText: "Planned items bring it to ₹58,000")
            PBBudgetCard(spent: "₹61,500", budget: "of ₹60,000", progress: 60 / 61.5, status: .overBudget(warning: "₹1,500 over budget"), plannedText: "All planned items are bought.")
            PBBudgetCard(spent: "₹52,000", budget: "of ₹60,000", progress: 52.0 / 60, status: .closed, percentLabel: "87% used", leftLabel: "₹8,000 under budget")
        }
        .padding(PBLayout.screenMargin)
    }
}
