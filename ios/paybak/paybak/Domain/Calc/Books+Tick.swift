import Foundation

/// The scheduler (domain.md §10): recurring occurrences, overdue alerts, the month-end summary and
/// reminders for every moment between the cursor and `until`, then the purge and project archiving.
/// Ids are deterministic and past moments never re-run, so it's idempotent.
nonisolated extension Books {
    mutating func tick(until: Date) {
        let cursor = ledger.scheduler.cursor ?? until
        let schedule = ledger.settings.reminderSchedule
        let reminderTime = schedule.hourMinute
        var day = self.day(of: cursor)
        let lastDay = self.day(of: until)
        while day <= lastDay {
            let jobs: [(hour: Int, minute: Int, job: Job)] = [
                (9, 0, .recurring), (9, 0, .overdue), (20, 0, .summary), (reminderTime.hour, reminderTime.minute, .reminders),
            ]
            for entry in jobs {
                let moment = day.moment(hour: entry.hour, minute: entry.minute, in: calendar)
                guard cursor < moment, moment <= until else { continue }
                switch entry.job {
                case .recurring: recurringJob(at: moment)
                case .overdue: overdueJob(at: moment)
                case .summary: summaryJob(at: moment)
                case .reminders: reminderJob(at: moment)
                }
            }
            day = day.adding(days: 1)
        }
        purgeDeleted(until: until)
        archiveSettledProjects(at: until)
        ledger.scheduler.cursor = max(until, cursor)
    }

    /// The next occurrence of a rule after `after` (§10): the anchor's weekday, day of month
    /// (clamped) or day and month. Every 2 weeks steps 14 days from the anchor, so it lands on the
    /// anchor's weekday every other week.
    static func nextOccurrence(_ rule: RecurringRule, after: LocalDay) -> LocalDay {
        let anchor = rule.anchorDate
        switch rule.frequency {
        case .weekly:
            let delta = ((anchor.weekday - after.weekday - 1) % 7 + 7) % 7 + 1
            return after.adding(days: delta)
        case .biweekly:
            let offset = (anchor.days(to: after) % 14 + 14) % 14
            return after.adding(days: 14 - offset)
        case .yearly:
            let candidate = anchor.replacing(year: after.year)
            return candidate > after ? candidate : anchor.replacing(year: after.year + 1)
        case .monthly:
            let candidate = after.firstOfMonth.adding(months: 0, day: anchor.day)
            return candidate > after ? candidate : after.firstOfMonth.adding(months: 1, day: anchor.day)
        }
    }

    /// The day a rule last produced something (the day before its start for a new rule).
    static func lastOccurrence(of rule: RecurringRule) -> LocalDay {
        rule.lastOccurrence ?? rule.startDate.adding(days: -1)
    }

    // MARK: Jobs

    private enum Job {
        case recurring
        case overdue
        case summary
        case reminders
    }

    /// 09:00: fixed rules add their expense, variable ones a draft.
    private mutating func recurringJob(at moment: Date) {
        let today = day(of: moment)
        for index in ledger.recurringRules.indices where ledger.recurringRules[index].active {
            let rule = ledger.recurringRules[index]
            let occurrence = Self.nextOccurrence(rule, after: Self.lastOccurrence(of: rule))
            guard occurrence == today else { continue }
            ledger.recurringRules[index].lastOccurrence = occurrence
            if rule.variable || rule.amount == nil {
                let id = "d-\(rule.id)-\(occurrence)"
                guard ledger.draft(id) == nil else { continue }
                ledger.drafts.append(RecurringDraft(id: id, ruleId: rule.id, occurrenceDate: occurrence, createdAt: moment))
            } else {
                let id = "e-\(rule.id)-\(occurrence)"
                guard ledger.expense(id) == nil else { continue }
                appendExpense(from: rule, id: id, amount: rule.amount ?? 0, occurrence: occurrence, createdBy: rule.createdBy, at: moment)
            }
        }
    }

    /// 09:00: each open item owed to you that was due yesterday.
    private mutating func overdueJob(at moment: Date) {
        let yesterday = day(of: moment).adding(days: -1)
        for item in openItems(asOf: moment) where item.isOwedToMe && item.due == yesterday {
            let id = "n-overdue-\(item.ref)-\(day(of: moment).compact)"
            guard !ledger.inbox.contains(where: { $0.id == id }) else { continue }
            ledger.inbox.append(InboxItem(
                id: id, type: .paymentOverdue, createdAt: moment, read: false,
                params: InboxParams(personId: item.friend, expenseId: item.kind == .direct ? item.ref : nil,
                                    title: item.title, amount: item.amount, currency: defaultCurrency, dueDate: item.due)
            ))
        }
    }

    /// 20:00 on the month's last day: the Insights total and Home totals as of that moment.
    private mutating func summaryJob(at moment: Date) {
        let today = day(of: moment)
        guard today == today.lastOfMonth else { return }
        let id = "n-summary-\(YearMonth(today).key.replacingOccurrences(of: "-", with: ""))"
        guard !ledger.inbox.contains(where: { $0.id == id }) else { return }
        let report = insights(YearMonth(today), asOf: moment)
        let totals = homeTotals(asOf: moment)
        ledger.inbox.append(InboxItem(
            id: id, type: .monthlySummary, createdAt: moment, read: false,
            params: InboxParams(year: today.year, month: today.month, spent: report.total, owed: totals.owed, owe: totals.owe)
        ))
    }

    /// The reminder time: owed to you → an automatic reminder (unless muted); you owe → an inbox
    /// "Payment reminder".
    private mutating func reminderJob(at moment: Date) {
        let today = day(of: moment)
        let schedule = ledger.settings.reminderSchedule
        for item in openItems(asOf: moment) {
            guard let due = item.due, schedule.fires(due: due, on: today) else { continue }
            if item.isOwedToMe {
                guard ledger.person(item.friend)?.remindersMuted != true else { continue }
                let id = "rem-\(item.ref)-\(today.compact)"
                guard !ledger.reminders.contains(where: { $0.id == id }) else { continue }
                ledger.reminders.append(Reminder(
                    id: id, toId: item.friend, amount: item.amount, currency: defaultCurrency,
                    expenseId: item.kind == .direct ? item.ref : nil, groupId: item.kind == .group ? item.ref : nil,
                    loanId: item.kind == .loan ? item.ref : nil, installment: item.installment, sentAt: moment, automatic: true
                ))
            } else {
                let id = "n-reminder-\(item.ref)-\(today.compact)"
                guard !ledger.inbox.contains(where: { $0.id == id }) else { continue }
                ledger.inbox.append(InboxItem(
                    id: id, type: .paymentReminder, createdAt: moment, read: false,
                    params: InboxParams(personId: item.friend, groupId: item.kind == .group ? item.ref : nil,
                                        title: item.title, amount: item.amount, currency: defaultCurrency, dueDate: due)
                ))
            }
        }
    }

    /// Recently deleted keeps 30 days.
    private mutating func purgeDeleted(until: Date) {
        ledger.expenses.removeAll { expense in
            guard let deletedAt = expense.deletedAt,
                  let purgeAt = calendar.date(byAdding: .day, value: Ledger.deletedRetentionDays, to: deletedAt) else { return false }
            return purgeAt <= until
        }
    }

    /// A closed project archives once everyone is square.
    mutating func archiveSettledProjects(at moment: Date) {
        for index in ledger.groups.indices where ledger.groups[index].project?.status == .closed {
            if groupNets(ledger.groups[index].id, asOf: moment).values.allSatisfy({ $0 == 0 }) {
                ledger.groups[index].project?.status = .archived
                ledger.groups[index].project?.archivedAt = moment
            }
        }
    }

    /// The expense a rule creates for an occurrence (split equally with the fair rotation), and the
    /// rotation counter to save.
    func expense(from rule: RecurringRule, id: ExpenseID, amount: Int64, occurrence: LocalDay, createdBy: PersonID,
                 at moment: Date) -> (expense: Expense, counter: Int) {
        let order = rule.split.personIds
        let key = rule.groupId ?? Books.rotationKey(order)
        let (shares, counter) = Splits.equal(amount, among: order, counter: ledger.rotation[key, default: 0])
        let expense = Expense(
            id: id, groupId: rule.groupId, title: rule.title, category: rule.category, amount: amount,
            currency: rule.currency, rate: nil, date: occurrence, dueDate: nil,
            payers: [Payer(personId: rule.payerId, amount: amount)],
            split: Split(mode: .equal, rows: order.map { SplitRow(personId: $0, included: true, value: nil, share: shares[$0, default: 0]) }),
            itemized: nil, notes: nil, receipt: nil, recurringRuleId: rule.id, occurrenceDate: occurrence,
            createdAt: moment, createdBy: createdBy,
            history: [HistoryEntry(kind: .created, at: moment, by: createdBy)], comments: [], flag: nil,
            deletedAt: nil, deletedBy: nil
        )
        return (expense, counter)
    }

    /// The rotation context outside a group: the sorted participant ids joined by "+" (§4.1).
    static func rotationKey(_ people: [PersonID]) -> String {
        people.sorted().joined(separator: "+")
    }
}
