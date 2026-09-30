import SwiftUI

/// Chart / Monthly Bars (Figma 143:2157): six months of your share. 32 pt columns spread across the
/// width: bars grow up from a zero baseline in a 120 pt plot (the tallest month fills it), with
/// round tops, over a Footnote month label. The current (last) month is black with a
/// `text/primary` label; the others are `chart/bar` gray. No axes, no gridlines.
struct PBMonthlyBarChart: View {
    struct Month: Identifiable {
        /// "Apr".
        let label: String
        let value: Double
        /// Spoken with the label ("Apr, ₹18,400").
        var accessibilityValue: String?

        var id: String { label }
    }

    /// Oldest first; the last one is the current month.
    let months: [Month]

    private let plotHeight: CGFloat = 120
    private let barWidth: CGFloat = 32

    var body: some View {
        let peak = months.map(\.value).max() ?? 0
        HStack(alignment: .bottom, spacing: 0) {
            ForEach(Array(months.enumerated()), id: \.element.id) { index, month in
                if index > 0 { Spacer(minLength: PBSpace.s4) }
                column(month, height: peak > 0 ? plotHeight * month.value / peak : 0, isCurrent: index == months.count - 1)
            }
        }
    }

    private func column(_ month: Month, height: CGFloat, isCurrent: Bool) -> some View {
        VStack(spacing: 0) {
            UnevenRoundedRectangle(topLeadingRadius: barWidth / 2, topTrailingRadius: barWidth / 2)
                .fill(isCurrent ? PBColor.chartFill : PBColor.chartBar)
                .frame(width: barWidth, height: max(0, height))
                .frame(height: plotHeight, alignment: .bottom)
                .clipped()
            Text(month.label)
                .textStyle(.footnote)
                .foregroundStyle(isCurrent ? PBColor.textPrimary : PBColor.textSecondary)
                .lineLimit(1)
                .fixedSize()
                .frame(width: barWidth, height: 20)
        }
        .accessibilityElement(children: .ignore)
        .accessibilityLabel(month.label)
        .accessibilityValue(month.accessibilityValue ?? "")
    }
}

#Preview("PBMonthlyBarChart") {
    PBMonthlyBarChart(months: [
        .init(label: "Apr", value: 18_400),
        .init(label: "May", value: 21_800),
        .init(label: "Jun", value: 19_600),
        .init(label: "Jul", value: 20_500),
        .init(label: "Aug", value: 22_100),
        .init(label: "Sep", value: 23_300),
    ])
    .padding(PBSpace.s20)
    .pbCard(padding: 0)
    .padding(PBLayout.screenMargin)
}
