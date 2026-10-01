import SwiftUI

/// The Repeat sheet (screens-insights-ai §5.3): Never · Weekly · Monthly · Yearly · Custom, the day
/// the schedule follows, "Amount changes each time" (variable rules make drafts), the helper and the
/// next date. Done (or ✕) answers `.repeatRule` — nil for Never. Custom picks the unit and the first
/// date (a proposal; Figma doesn't draw it).
struct RepeatSheet: View {
    let request: RepeatRuleRequest

    @Environment(AppRouter.self) private var router
    @Environment(LedgerStore.self) private var ledgerStore
    @State private var choice: Choice
    @State private var anchor: LocalDay
    @State private var variable: Bool
    @State private var customUnit: RecurrenceFrequency = .monthly
    @State private var openPicker: OpenPicker?

    enum Choice: String, CaseIterable {
        case never, weekly, monthly, yearly, custom

        var title: String { rawValue.capitalized }
    }

    private enum OpenPicker {
        case day
        case unit
    }

    init(request: RepeatRuleRequest) {
        self.request = request
        _choice = State(initialValue: request.current.map { Choice(rawValue: $0.frequency.rawValue) ?? .monthly } ?? .never)
        _anchor = State(initialValue: request.current?.anchorDate ?? request.startDate)
        _variable = State(initialValue: request.current?.variable ?? false)
    }

    private var rule: RepeatRule? {
        let frequency: RecurrenceFrequency? = switch choice {
        case .never: nil
        case .weekly: .weekly
        case .monthly: .monthly
        case .yearly: .yearly
        case .custom: customUnit
        }
        return frequency.map { RepeatRule(frequency: $0, anchorDate: anchor, variable: variable) }
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
                        Text(rule.nextLine(after: request.startDate))
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
            .animation(.easeOut(duration: 0.2), value: openPicker)
        }
        .routeTestRoot("repeatRule")
    }

    // MARK: Settings card

    private func settings(_ rule: RepeatRule) -> some View {
        VStack(spacing: 0) {
            if choice == .custom {
                PBSettingRow("Every", value: unitName(customUnit), icon: .repeat) { toggle(.unit) }
                    .accessibilityIdentifier("repeatSheet.unit")
                if openPicker == .unit {
                    Picker("Every", selection: $customUnit) {
                        ForEach(RecurrenceFrequency.allCases, id: \.self) { Text(unitName($0)).tag($0) }
                    }
                    .pickerStyle(.wheel)
                    .frame(height: 140)
                }
            }
            dayRow(rule)
            if openPicker == .day {
                dayPicker
            }
            PBSettingRow("Amount changes each time", icon: .wallet, trailing: .toggle($variable), showsDivider: false)
                .accessibilityIdentifier("repeatSheet.variable")
        }
        .pbCard(padding: 0)
    }

    @ViewBuilder
    private func dayRow(_ rule: RepeatRule) -> some View {
        let (title, value): (String, String) = switch (choice, rule.frequency) {
        case (.custom, _): ("Starts", Format.day(anchor))
        case (_, .weekly): ("Day of week", Format.weekday(anchor))
        case (_, .monthly): ("Day of month", rule.dayOfMonthValue)
        case (_, .yearly): ("Date", Format.short(anchor))
        }
        PBSettingRow(title, value: value, icon: .calendar) { toggle(.day) }
            .accessibilityIdentifier("repeatSheet.dayOfMonth")
    }

    @ViewBuilder
    private var dayPicker: some View {
        switch choice {
        case .weekly:
            Picker("Day of week", selection: weekdayBinding) {
                ForEach(0..<7, id: \.self) { Text(Format.weekdayNames[$0]).tag($0) }
            }
            .pickerStyle(.wheel)
            .frame(height: 140)
        case .monthly:
            Picker("Day of month", selection: dayOfMonthBinding) {
                ForEach(1...28, id: \.self) { Text(Format.ordinal($0)).tag($0) }
                Text("Last day").tag(31)
            }
            .pickerStyle(.wheel)
            .frame(height: 140)
        case .yearly, .custom:
            DatePicker("Date", selection: dateBinding, displayedComponents: .date)
                .datePickerStyle(.wheel)
                .labelsHidden()
                .frame(height: 140)
                .clipped()
        case .never:
            EmptyView()
        }
    }

    // MARK: Bindings

    private var calendar: Calendar { ledgerStore.clock.calendar }

    /// A weekday: the first such day on or after the start.
    private var weekdayBinding: Binding<Int> {
        Binding { anchor.weekday } set: { weekday in
            let start = request.startDate
            anchor = start.adding(days: (weekday - start.weekday + 7) % 7)
        }
    }

    /// A day of the month; "Last day" is the 31st (the scheduler clamps it to shorter months), so the
    /// anchor moves to the next month that has one.
    private var dayOfMonthBinding: Binding<Int> {
        Binding { anchor.day } set: { day in
            var month = request.startDate.firstOfMonth
            while LocalDay.daysIn(month: month.month, year: month.year) < day {
                month = month.adding(months: 1, day: 1)
            }
            anchor = LocalDay(year: month.year, month: month.month, day: day)
        }
    }

    private var dateBinding: Binding<Date> {
        Binding { anchor.start(in: calendar) } set: { anchor = LocalDay($0, calendar: calendar) }
    }

    // MARK: Actions

    private func select(_ option: Choice) {
        choice = option
        openPicker = nil
        Haptics.selection()
    }

    private func toggle(_ picker: OpenPicker) {
        openPicker = openPicker == picker ? nil : picker
    }

    private func done() {
        router.complete(request.id, with: .repeatRule(rule))
    }

    private func unitName(_ unit: RecurrenceFrequency) -> String {
        switch unit {
        case .weekly: "Week"
        case .monthly: "Month"
        case .yearly: "Year"
        }
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
