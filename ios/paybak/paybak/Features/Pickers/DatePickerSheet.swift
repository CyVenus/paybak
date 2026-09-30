import SwiftUI

/// The Due date / Date sheet (add-expense §10): the system inline calendar tinted black, the chosen
/// day with how far away it is, the reminder schedule hint (due dates only), then "Set due date" /
/// "Set date" and, when allowed, "No due date". Answers `.day(day)` or `.day(nil)`; ✕ changes
/// nothing.
struct DatePickerSheet: View {
    let request: DatePickRequest

    @Environment(AppRouter.self) private var router
    @Environment(LedgerStore.self) private var store
    @State private var selection = Date()
    @State private var didLoad = false

    private var isDue: Bool { request.kind == .dueDate }
    private var prefix: String { isDue ? "dueDate" : "date" }
    private var calendar: Calendar { store.clock.calendar }
    private var today: LocalDay { store.clock.today }
    private var day: LocalDay { LocalDay(selection, calendar: calendar) }

    var body: some View {
        PBSheet(title: isDue ? "Due date" : "Date", testIDPrefix: prefix, onClose: router.dismissSheet) {
            VStack(spacing: PBSpace.s16) {
                DatePicker("", selection: $selection, in: range, displayedComponents: .date)
                    .datePickerStyle(.graphical)
                    .labelsHidden()
                    .tint(PBColor.bgInverse)
                    .environment(\.calendar, calendar)
                    .padding(.horizontal, PBSpace.s8)
                    .accessibilityIdentifier("\(prefix).calendar")
                VStack(alignment: .leading, spacing: PBSpace.s4) {
                    Text(summary)
                        .textStyle(.headline)
                        .foregroundStyle(PBColor.textPrimary)
                        .accessibilityIdentifier("\(prefix).summary")
                    if isDue {
                        Text(Self.reminderHint(store.ledger.settings.reminderSchedule))
                            .textStyle(.footnote)
                            .foregroundStyle(PBColor.textSecondary)
                            .fixedSize(horizontal: false, vertical: true)
                    }
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                VStack(spacing: PBSpace.s8) {
                    PBButton(isDue ? "Set due date" : "Set date", fillsWidth: true) {
                        Haptics.selection()
                        router.complete(request.id, with: .day(day))
                    }
                    .accessibilityIdentifier("\(prefix).set")
                    if request.allowsNone {
                        PBTextButton("No due date") { router.complete(request.id, with: .day(nil)) }
                            .accessibilityIdentifier("\(prefix).none")
                    }
                }
            }
        }
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier("\(prefix).sheet")
        .onAppear {
            guard !didLoad else { return }
            didLoad = true
            selection = (request.selected ?? today).moment(hour: 12, minute: 0, in: calendar)
        }
    }

    /// "Sun 4 Oct · in 4 days" / "Wed 30 Sep · Today" / "Mon 28 Sep · 2 days ago".
    private var summary: String {
        let relative = Format.relativeDays(day, today: today)
        return "\(Format.dayWithYear(day, today: today)) · \(isDue ? relative : relative.capitalizedFirst)"
    }

    private var range: ClosedRange<Date> {
        let lower = request.earliest.map { $0.start(in: calendar) } ?? .distantPast
        let upper = request.latest.map { $0.moment(hour: 23, minute: 59, in: calendar) } ?? .distantFuture
        return lower...upper
    }

    /// Settings' reminder schedule in words: "Paybak reminds them 2 days before, on the day, and every
    /// 3 days if it’s overdue."
    static func reminderHint(_ schedule: LedgerSettings.ReminderSchedule) -> String {
        let parts = [
            schedule.twoDaysBefore ? "2 days before" : nil,
            schedule.onDueDate ? "on the day" : nil,
            schedule.overdueEvery3Days ? "every 3 days if it’s overdue" : nil,
        ].compactMap(\.self)
        switch parts.count {
        case 0: return "Paybak won’t send them reminders. Change this in Settings."
        case 1: return "Paybak reminds them \(parts[0])."
        case 2: return "Paybak reminds them \(parts[0]) and \(parts[1])."
        default: return "Paybak reminds them \(parts.dropLast().joined(separator: ", ")), and \(parts[parts.count - 1])."
        }
    }
}

private extension String {
    var capitalizedFirst: String { prefix(1).uppercased() + dropFirst() }
}
