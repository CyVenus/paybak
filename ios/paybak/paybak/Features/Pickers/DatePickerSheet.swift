import SwiftUI

/// The Due date / Date sheet (add-expense §10): the system inline calendar tinted black, the chosen
/// day with how far away it is, the reminder schedule hint (due dates only), then "Set due date" /
/// "Set date" and, when allowed, "No due date". A due date runs from tomorrow on and opens on
/// tomorrow; a date is today or earlier and opens on today. Answers `.day(day)` or `.day(nil)`; ✕
/// changes nothing.
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
                        Text(Format.reminderHint(store.ledger.settings.reminderSchedule))
                            .textStyle(.footnote)
                            .foregroundStyle(PBColor.textSecondary)
                            .fixedSize(horizontal: false, vertical: true)
                    }
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                VStack(spacing: PBSpace.s8) {
                    PBButton(isDue ? "Set due date" : "Set date", fillsWidth: true) {
                        router.complete(request.id, with: .day(day))
                    }
                    .disabled(!isInRange)
                    .accessibilityIdentifier("\(prefix).set")
                    if isDue, request.allowsNone {
                        PBTextButton("No due date") { router.complete(request.id, with: .day(nil)) }
                            .accessibilityIdentifier("\(prefix).none")
                    }
                }
            }
        }
        .routeTestRoot("pickDate")
        .onAppear {
            guard !didLoad else { return }
            didLoad = true
            selection = (request.selected ?? (isDue ? today.adding(days: 1) : today)).moment(hour: 12, minute: 0, in: calendar)
        }
    }

    /// "Sun 4 Oct · in 4 days" / "Wed 30 Sep · Today" / "Mon 28 Sep · 2 days ago" ("Today" and
    /// "Yesterday" capitalised only on the Date sheet).
    private var summary: String {
        let relative = Format.relativeDays(day, today: today)
        let days = today.days(to: day)
        return "\(Format.dayWithYear(day, today: today)) · \(!isDue && (days == 0 || days == -1) ? relative.capitalizedFirst : relative)"
    }

    /// Due dates from tomorrow, dates up to today, unless the request says otherwise.
    private var earliest: LocalDay? { request.earliest ?? (isDue ? today.adding(days: 1) : nil) }
    private var latest: LocalDay? { request.latest ?? (isDue ? nil : today) }

    private var isInRange: Bool {
        (earliest.map { day >= $0 } ?? true) && (latest.map { day <= $0 } ?? true)
    }

    private var range: ClosedRange<Date> {
        let lower = earliest.map { $0.start(in: calendar) } ?? .distantPast
        let upper = latest.map { $0.moment(hour: 23, minute: 59, in: calendar) } ?? .distantFuture
        return lower...upper
    }
}

private extension String {
    var capitalizedFirst: String { prefix(1).uppercased() + dropFirst() }
}
