import SwiftUI

/// The Repeat sheet (screens-insights-ai §5.3): Never · Weekly · Monthly · Yearly · Custom, the day
/// the schedule follows, "Amount changes each time" (variable rules make drafts), the helper and the
/// next date. Custom repeats every other week (`.biweekly`), as on Android. The day row opens chips
/// under it: Mon–Sun for a weekly or every-2-weeks rule, 1–31 for a monthly one; a yearly rule falls
/// on the expense's date. Done (or ✕) answers `.repeatRule` — nil for Never.
struct RepeatSheet: View {
    let request: RepeatRuleRequest

    @Environment(AppRouter.self) private var router
    @Environment(LedgerStore.self) private var ledgerStore
    @State private var choice: Choice
    @State private var anchor: LocalDay
    @State private var variable: Bool
    @State private var isPickingDay = false

    /// The frequency chips; Custom repeats every other week.
    enum Choice: String, CaseIterable {
        case never, weekly, monthly, yearly, custom

        var title: String { rawValue.capitalized }

        var frequency: RecurrenceFrequency? {
            switch self {
            case .never: nil
            case .weekly: .weekly
            case .monthly: .monthly
            case .yearly: .yearly
            case .custom: .biweekly
            }
        }

        init(_ frequency: RecurrenceFrequency?) {
            self = Self.allCases.first { $0.frequency == frequency } ?? .never
        }
    }

    init(request: RepeatRuleRequest) {
        self.request = request
        _choice = State(initialValue: Choice(request.current?.frequency))
        _anchor = State(initialValue: request.current?.anchorDate ?? request.startDate)
        _variable = State(initialValue: request.current?.variable ?? false)
    }

    private var rule: RepeatRule? {
        choice.frequency.map { RepeatRule(frequency: $0, anchorDate: anchor, variable: variable) }
    }

    var body: some View {
        PBSheet(title: "Repeat", testIDPrefix: "repeatSheet", onClose: done) {
            VStack(alignment: .leading, spacing: 0) {
                PBFlowLayout {
                    ForEach(Choice.allCases, id: \.self) { option in
                        PBCategoryChip(option.title, isSelected: choice == option) { select(option) }
                            .accessibilityIdentifier("repeatSheet.freq.\(option.rawValue)")
                    }
                }
                if let rule {
                    settings(rule)
                        .padding(.top, PBSpace.s16)
                    Text(rule.helper)
                        .textStyle(.footnote)
                        .foregroundStyle(PBColor.textSecondary)
                        .fixedSize(horizontal: false, vertical: true)
                        .padding(.top, PBSpace.s12)
                    HStack(spacing: PBSpace.s6) {
                        PBIconView(.calendar, size: PBSize.iconSm)
                            .foregroundStyle(PBColor.iconSecondary)
                        Text(nextLine(rule))
                            .textStyle(.footnote)
                            .foregroundStyle(PBColor.textPrimary)
                    }
                    .accessibilityElement(children: .combine)
                    .accessibilityIdentifier("repeatSheet.nextDraft")
                    .padding(.top, PBSpace.s12)
                }
                PBButton("Done", fillsWidth: true, action: done)
                    .accessibilityIdentifier("repeatSheet.done")
                    .padding(.top, PBSpace.s24)
            }
            .animation(.easeOut(duration: 0.2), value: choice)
            .animation(.easeOut(duration: 0.2), value: isPickingDay)
        }
        .routeTestRoot("repeatRule")
    }

    // MARK: Settings card

    private func settings(_ rule: RepeatRule) -> some View {
        VStack(spacing: 0) {
            dayRow(rule)
            if isPickingDay {
                dayPicker(rule)
            }
            PBSettingRow("Amount changes each time", icon: .wallet, trailing: .toggle($variable), showsDivider: false)
                .accessibilityIdentifier("repeatSheet.variable")
        }
        .pbCard(padding: 0)
    }

    /// "Day of week" Monday · "Day of month" 28th · "Date" 28 Sep (fixed: the expense's own date).
    @ViewBuilder
    private func dayRow(_ rule: RepeatRule) -> some View {
        switch rule.frequency {
        case .weekly, .biweekly:
            PBSettingRow("Day of week", value: Format.weekday(anchor), icon: .calendar) { isPickingDay.toggle() }
                .accessibilityIdentifier("repeatSheet.dayOfMonth")
        case .monthly:
            PBSettingRow("Day of month", value: Format.ordinal(anchor.day), icon: .calendar) { isPickingDay.toggle() }
                .accessibilityIdentifier("repeatSheet.dayOfMonth")
        case .yearly:
            PBSettingRow("Date", value: Format.short(anchor), icon: .calendar, trailing: .none)
                .accessibilityIdentifier("repeatSheet.dayOfMonth")
        }
    }

    @ViewBuilder
    private func dayPicker(_ rule: RepeatRule) -> some View {
        switch rule.frequency {
        case .weekly, .biweekly:
            dayChips {
                ForEach(0..<7, id: \.self) { weekday in
                    DayChip(title: String(Format.weekdayNames[weekday].prefix(3)), isSelected: anchor.weekday == weekday) {
                        pickWeekday(weekday)
                    }
                    .accessibilityIdentifier("repeatSheet.weekday.\(weekday + 1)")
                }
            }
        case .monthly:
            dayChips {
                ForEach(1...31, id: \.self) { day in
                    DayChip(title: "\(day)", isSelected: anchor.day == day) { pickDayOfMonth(day) }
                        .accessibilityIdentifier("repeatSheet.day.\(day)")
                }
            }
        case .yearly:
            EmptyView()
        }
    }

    /// The chips under the day row, white on the card, wrapping.
    private func dayChips(@ViewBuilder _ chips: () -> some View) -> some View {
        PBFlowLayout {
            chips()
        }
        .padding(.horizontal, PBSpace.s16)
        .padding(.bottom, PBSpace.s12)
        .frame(maxWidth: .infinity, alignment: .leading)
    }

    // MARK: Actions

    /// A yearly rule falls on the expense's own date.
    private func select(_ option: Choice) {
        Haptics.selection()
        if option == .yearly { anchor = request.startDate }
        choice = option
        isPickingDay = false
    }

    /// The anchor moves to that weekday in its own week or the next.
    private func pickWeekday(_ weekday: Int) {
        Haptics.selection()
        anchor = anchor.adding(days: (weekday - anchor.weekday + 7) % 7)
        isPickingDay = false
    }

    /// The anchor moves to that day in the first month from its own that has it (the 31st in
    /// a 31-day month).
    private func pickDayOfMonth(_ day: Int) {
        Haptics.selection()
        var month = anchor.firstOfMonth
        while LocalDay.daysIn(month: month.month, year: month.year) < day {
            month = month.adding(months: 1, day: 1)
        }
        anchor = LocalDay(year: month.year, month: month.month, day: day)
        isPickingDay = false
    }

    /// "Next draft: Wed 28 Oct" (variable) or "Next: Wed 28 Oct": the first occurrence after the
    /// expense's date that's still to come.
    private func nextLine(_ rule: RepeatRule) -> String {
        let next = rule.nextOccurrence(after: max(request.startDate, ledgerStore.books.today))
        return "\(rule.variable ? "Next draft" : "Next"): \(Format.day(next))"
    }

    private func done() {
        router.complete(request.id, with: .repeatRule(rule))
    }
}

/// A day chip on the #F5F5F5 card: white, black when chosen (Category Chip on a card).
private struct DayChip: View {
    let title: String
    let isSelected: Bool
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Text(title)
                .textStyle(.buttonSmall)
                .lineLimit(1)
                .padding(.horizontal, PBSpace.s16)
        }
        .buttonStyle(DayChipStyle(isSelected: isSelected))
        .accessibilityAddTraits(isSelected ? .isSelected : [])
    }
}

private struct DayChipStyle: ButtonStyle {
    let isSelected: Bool

    func makeBody(configuration: Configuration) -> some View {
        let fill: Color = switch (isSelected, configuration.isPressed) {
        case (false, false): PBColor.bgPrimary
        case (false, true): PBColor.bgCardPressed
        case (true, false): PBColor.bgInverse
        case (true, true): PBColor.bgInversePressed
        }
        configuration.label
            .foregroundStyle(isSelected ? PBColor.textInverse : PBColor.textPrimary)
            .frame(height: PBSize.buttonSm)
            .background(fill, in: .capsule)
            .padding(.vertical, (PBSize.tap - PBSize.buttonSm) / 2)
            .contentShape(.capsule)
            .padding(.vertical, -(PBSize.tap - PBSize.buttonSm) / 2)
            .animation(.easeOut(duration: 0.1), value: configuration.isPressed)
    }
}

#if DEBUG
#Preview("RepeatSheet") {
    GroupsPreview(scenarios: Scenario.pro()) {
        RepeatSheet(request: RepeatRuleRequest(
            current: RepeatRule(frequency: .monthly, anchorDate: DemoSeed.figmaDay.adding(days: -2), variable: true),
            startDate: DemoSeed.figmaDay
        ))
    }
}
#endif
