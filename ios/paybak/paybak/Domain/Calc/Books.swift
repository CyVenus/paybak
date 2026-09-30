import Foundation

/// The ledger at a moment: the records plus the clock, calendar and default currency every
/// calculation needs (the `Ledger` class of verify.py). Read models live in the `Books+…` extensions,
/// the store actions in `Books+Actions`, the scheduler in `Books+Tick`. Every time-dependent result
/// takes `now` from here; "as of t" variants only count what existed at t (domain.md §5.1).
nonisolated struct Books: Sendable {
    var ledger: Ledger
    /// The clock's moment (`AppClock.now`).
    var now: Date
    /// The device calendar and time zone: moments become local days and times through it.
    var calendar: Calendar
    /// The profile currency: totals, new groups and new expenses use it.
    var defaultCurrency: String

    init(ledger: Ledger, now: Date, calendar: Calendar, defaultCurrency: String) {
        self.ledger = ledger
        self.now = now
        self.calendar = calendar
        self.defaultCurrency = defaultCurrency
    }

    var today: LocalDay { day(of: now) }

    func day(of moment: Date) -> LocalDay {
        LocalDay(moment, calendar: calendar)
    }

    // MARK: Names

    /// "You" for the user, else the first name.
    func firstName(_ id: PersonID) -> String {
        id == Person.me ? "You" : ledger.person(id)?.firstName ?? "Someone"
    }

    /// The first name, with "you" in the middle of a sentence.
    func name(_ id: PersonID) -> String {
        id == Person.me ? "you" : firstName(id)
    }

    func groupName(_ id: GroupID?) -> String {
        id.flatMap { ledger.group($0)?.name } ?? ""
    }

    // MARK: Record filters (§5.1)

    /// Expenses that existed and weren't deleted at `asOf` (default now).
    func liveExpenses(asOf: Date? = nil) -> [Expense] {
        let moment = asOf ?? now
        return ledger.expenses.filter { expense in
            expense.createdAt <= moment && !(expense.deletedAt.map { $0 <= moment } ?? false)
        }
    }

    /// Payments confirmed at or before `asOf` (default now). Only these change balances.
    func confirmedPayments(asOf: Date? = nil) -> [Payment] {
        let moment = asOf ?? now
        return ledger.payments.filter { payment in
            payment.status == .confirmed && (payment.confirmedAt.map { $0 <= moment } ?? false)
        }
    }

    /// An amount in the default currency.
    func toDefault(_ minor: Int64, currency: String, rate: Rate?) -> Int64 {
        Money.toDefault(minor, currency: currency, rate: rate, defaultCurrency: defaultCurrency)
    }

    /// What a payment was for: the expense title, group name or loan reason ("Payment" otherwise).
    func paymentFor(_ payment: Payment) -> String {
        if let id = payment.expenseId, let expense = ledger.expense(id) { return expense.title }
        if let id = payment.groupId, let group = ledger.group(id) { return group.name }
        if let id = payment.loanId, let loan = ledger.loan(id) { return loan.title }
        return "Payment"
    }
}
