import Foundation

/// The ledger document (app-architecture §3.2): everything except the profile. Saved as
/// `ledger.json`, the shape of `demo.json` without `anchor`, `profile` and `scenarios`, with absolute
/// dates. Arrays keep insertion order, which is the tie-break order everywhere (people = added order).
nonisolated struct Ledger: Codable, Hashable, Sendable {
    static let currentSchemaVersion = 1
    /// Recently deleted keeps expenses this many days.
    static let deletedRetentionDays = 30

    var schemaVersion = Ledger.currentSchemaVersion
    var settings = LedgerSettings()
    var people: [Person] = []
    var groups: [LedgerGroup] = []
    var expenses: [Expense] = []
    var payments: [Payment] = []
    var loans: [Loan] = []
    var components: [ProjectComponent] = []
    var recurringRules: [RecurringRule] = []
    var drafts: [RecurringDraft] = []
    var reminders: [Reminder] = []
    var inbox: [InboxItem] = []
    /// Fair leftover-paise rotation counters per context (domain.md §4.1).
    var rotation: [String: Int] = [:]
    var scheduler = SchedulerState()

    // lane fields: add optional fields below with a default.

    init() {}

    /// True when there's nothing yet: a new account (Home "First day").
    var isEmpty: Bool {
        people.isEmpty && groups.isEmpty && expenses.isEmpty && payments.isEmpty && loans.isEmpty
    }

    // MARK: Lookup

    func person(_ id: PersonID) -> Person? { people.first { $0.id == id } }
    func group(_ id: GroupID) -> LedgerGroup? { groups.first { $0.id == id } }
    func expense(_ id: ExpenseID) -> Expense? { expenses.first { $0.id == id } }
    func payment(_ id: PaymentID) -> Payment? { payments.first { $0.id == id } }
    func loan(_ id: LoanID) -> Loan? { loans.first { $0.id == id } }
    func component(_ id: ComponentID) -> ProjectComponent? { components.first { $0.id == id } }
    func rule(_ id: RuleID) -> RecurringRule? { recurringRules.first { $0.id == id } }
    func draft(_ id: DraftID) -> RecurringDraft? { drafts.first { $0.id == id } }

    /// How a payment reminder pays `payee`: by UPI when they have a UPI ID, else the form's default.
    func reminderMethod(paying payee: PersonID) -> PaymentMethodKind? { person(payee)?.upi == nil ? nil : .upi }

    // MARK: Decoding (tolerant: missing keys take their defaults, unknown keys are ignored)

    private enum CodingKeys: String, CodingKey {
        case schemaVersion, settings, people, groups, expenses, payments, loans, components, recurringRules,
             drafts, reminders, inbox, rotation, scheduler
    }

    init(from decoder: Decoder) throws {
        let c = try decoder.container(keyedBy: CodingKeys.self)
        schemaVersion = try c.decodeIfPresent(Int.self, forKey: .schemaVersion) ?? Self.currentSchemaVersion
        settings = try c.decodeIfPresent(LedgerSettings.self, forKey: .settings) ?? LedgerSettings()
        people = try c.decodeIfPresent([Person].self, forKey: .people) ?? []
        groups = try c.decodeIfPresent([LedgerGroup].self, forKey: .groups) ?? []
        expenses = try c.decodeIfPresent([Expense].self, forKey: .expenses) ?? []
        payments = try c.decodeIfPresent([Payment].self, forKey: .payments) ?? []
        loans = try c.decodeIfPresent([Loan].self, forKey: .loans) ?? []
        components = try c.decodeIfPresent([ProjectComponent].self, forKey: .components) ?? []
        recurringRules = try c.decodeIfPresent([RecurringRule].self, forKey: .recurringRules) ?? []
        drafts = try c.decodeIfPresent([RecurringDraft].self, forKey: .drafts) ?? []
        reminders = try c.decodeIfPresent([Reminder].self, forKey: .reminders) ?? []
        inbox = try c.decodeIfPresent([InboxItem].self, forKey: .inbox) ?? []
        rotation = try c.decodeIfPresent([String: Int].self, forKey: .rotation) ?? [:]
        scheduler = try c.decodeIfPresent(SchedulerState.self, forKey: .scheduler) ?? SchedulerState()
    }
}

/// Where `tick` has run up to (domain.md §10).
nonisolated struct SchedulerState: Codable, Hashable, Sendable {
    var cursor: Date?
}

/// Ledger settings and the Pro entitlement (domain.md §1.14).
nonisolated struct LedgerSettings: Codable, Hashable, Sendable {
    struct Push: Codable, Hashable, Sendable {
        var addedToExpense = true
        var paymentsToConfirm = true
        var reminders = true
        var overdueAlerts = true
        var projectUpdates = true
        var monthlySummary = true
    }

    struct ReminderSchedule: Codable, Hashable, Sendable {
        var twoDaysBefore = true
        var onDueDate = true
        var overdueEvery3Days = true
        /// Local "HH:mm".
        var time = "21:00"

        var hourMinute: (hour: Int, minute: Int) {
            let parts = time.split(separator: ":").compactMap { Int($0) }
            return parts.count == 2 ? (parts[0], parts[1]) : (21, 0)
        }

        /// Whether a reminder fires for `due` on `day` (domain.md §10).
        func fires(due: LocalDay, on day: LocalDay) -> Bool {
            if twoDaysBefore, day == due.adding(days: -2) { return true }
            if onDueDate, day == due { return true }
            return overdueEvery3Days && day > due && due.days(to: day) % 3 == 0
        }
    }

    struct Discovery: Codable, Hashable, Sendable {
        var findMeByContact = true
        var contactsSync = true
    }

    var keepBalancesPerCurrency = false
    var push = Push()
    var reminderSchedule = ReminderSchedule()
    var discovery = Discovery()
    var entitlement = Entitlement()

    // lane fields: add optional fields below with a default.
}

/// The Pro plan (simulated purchase; domain.md §1.14).
nonisolated struct Entitlement: Codable, Hashable, Sendable {
    enum Plan: String, Codable, Sendable {
        case free
        case pro
    }

    enum Period: String, Codable, Sendable {
        case yearly
        case monthly
    }

    var plan: Plan = .free
    var period: Period?
    var trialEndsAt: LocalDay?
    var since: Date?

    var isPro: Bool { plan == .pro }
}
