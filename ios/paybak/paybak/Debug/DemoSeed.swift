#if DEBUG
import Foundation

/// The bundled demo dataset (`Resources/Seed/demo.json`, domain.md §7): relative dates resolved
/// against a load day, the base records, the demo profile and the named scenarios. Loading at Figma
/// parity (Wed 30 Sep 2026, clock 21:15) reproduces every Figma number from real records.
nonisolated struct DemoSeed {
    /// Wed 30 Sep 2026, the Figma day.
    static let figmaDay = LocalDay(year: 2026, month: 9, day: 30)
    /// The Figma clock: 21:15 local.
    static let figmaTime = (hour: 21, minute: 15)

    /// The demo materialised for a load day.
    struct Loaded {
        var books: Books
        var profile: DemoProfile
    }

    private let root: [String: Any]
    private let scenarios: [String: [[String: Any]]]

    /// Every scenario name, in file order.
    let scenarioNames: [String]

    init(data: Data) throws {
        guard let root = try JSONSerialization.jsonObject(with: data) as? [String: Any] else {
            throw CocoaError(.fileReadCorruptFile)
        }
        self.root = root
        scenarios = root["scenarios"] as? [String: [[String: Any]]] ?? [:]
        // JSONSerialization loses key order; read it from the text so menus list scenarios as written.
        let text = String(decoding: data, as: UTF8.self)
        let names = scenarios.keys.compactMap { name in text.range(of: "\"\(name)\": [").map { (name, $0.lowerBound) } }
        scenarioNames = names.sorted { $0.1 < $1.1 }.map(\.0)
    }

    /// The copy bundled with the app.
    static func bundled() throws -> DemoSeed {
        guard let url = Bundle.main.url(forResource: "demo", withExtension: "json") else {
            throw CocoaError(.fileNoSuchFile)
        }
        return try DemoSeed(data: Data(contentsOf: url))
    }

    /// The Figma moment in `calendar`'s time zone.
    static func figmaNow(calendar: Calendar) -> Date {
        figmaDay.moment(hour: figmaTime.hour, minute: figmaTime.minute, in: calendar)
    }

    /// Resolves the demo for `anchor` with the clock at `now`, runs `tick(now)` (today's 20:00 summary
    /// and 21:00 reminders), then applies `scenarios` in order (verify.py `load`).
    func load(anchor: LocalDay, now: Date, calendar: Calendar, scenarios names: [String] = []) throws -> Loaded {
        var base = root
        for key in ["anchor", "profile", "scenarios"] { base[key] = nil }
        let ledger = try decode(Ledger.self, from: resolve(base, anchor: anchor, now: now, calendar: calendar))
        let profile = try decode(DemoProfile.self, from: root["profile"] ?? [:])
        var books = Books(ledger: ledger, now: now, calendar: calendar, defaultCurrency: profile.currencyCode)
        books.tick(until: now)
        for name in names {
            try apply(name, to: &books, anchor: anchor)
        }
        return Loaded(books: books, profile: profile)
    }

    /// Runs a named scenario's steps (verify.py `apply_scenario`): `tick(at)` before each step, then the
    /// store action it names. Step moments clamp to now, except `setClock`, which moves the clock.
    func apply(_ name: String, to books: inout Books, anchor: LocalDay) throws {
        guard let steps = scenarios[name] else { throw SeedError.unknownScenario(name) }
        for raw in steps {
            if let use = raw["use"] as? String {
                try apply(use, to: &books, anchor: anchor)
                continue
            }
            let movesClock = raw["action"] as? String == "setClock"
            let resolved = resolve(raw, anchor: anchor, now: movesClock ? nil : books.now, calendar: books.calendar)
            let step = try decode(ScenarioStep.self, from: resolved)
            let at = step.at ?? books.now
            if let stepAt = step.at, stepAt > (books.ledger.scheduler.cursor ?? .distantPast) {
                books.tick(until: stepAt)
            }
            try perform(step, at: at, on: &books)
        }
    }

    // MARK: Private

    enum SeedError: Error {
        case unknownScenario(String)
        case incompleteStep(String)
    }

    private func perform(_ step: ScenarioStep, at: Date, on books: inout Books) throws {
        func require<T>(_ value: T?) throws -> T {
            guard let value else { throw SeedError.incompleteStep(step.action ?? "?") }
            return value
        }
        switch step.action {
        case "recordPayment":
            try books.recordPayment(require(step.payment), at: at)
        case "confirmPayment":
            try books.confirmPayment(require(step.paymentId), at: at)
        case "markNotReceived":
            try books.markNotReceived(require(step.paymentId), note: step.note ?? "", at: at)
        case "flagExpense":
            try books.flagExpense(require(step.expenseId), by: require(step.by), note: step.note ?? "", at: at)
        case "addLoan":
            try books.addLoan(require(step.loan), at: at)
        case "updateComponent":
            try books.updateComponent(require(step.componentId), status: require(step.status), actualCost: step.actualCost,
                                      paidBy: require(step.paidBy), at: at)
        case "closeProject":
            try books.closeProject(require(step.projectId), at: at)
        case "createGroup":
            books.addGroup(try require(step.group), at: at)
        case "setClock":
            books.tick(until: at)
            books.now = at
        case "setEntitlement":
            books.ledger.settings.entitlement = Entitlement(plan: step.plan ?? .free, period: step.period,
                                                            trialEndsAt: step.trialEndsAt, since: books.now)
        case "clearLedger":
            books.clear()
        default:
            throw SeedError.incompleteStep(step.action ?? "?")
        }
    }

    private func decode<T: Decodable>(_ type: T.Type, from object: Any) throws -> T {
        let decoder = JSONDecoder()
        decoder.dateDecodingStrategy = .iso8601
        return try decoder.decode(type, from: JSONSerialization.data(withJSONObject: object))
    }

    /// `D±n` → the load day ± n (`yyyy-MM-dd`); `D±nTHH:MM` → that local moment (ISO-8601 UTC), clamped
    /// to `now` when given (verify.py `resolve`).
    private func resolve(_ value: Any, anchor: LocalDay, now: Date?, calendar: Calendar) -> Any {
        switch value {
        case let object as [String: Any]:
            return object.mapValues { resolve($0, anchor: anchor, now: now, calendar: calendar) }
        case let array as [Any]:
            return array.map { resolve($0, anchor: anchor, now: now, calendar: calendar) }
        case let text as String:
            guard let match = text.wholeMatch(of: #/D([+-]?\d+)(?:T(\d\d):(\d\d))?/#), let offset = Int(match.1) else {
                return text
            }
            let day = anchor.adding(days: offset)
            guard let hour = match.2.flatMap({ Int($0) }), let minute = match.3.flatMap({ Int($0) }) else {
                return day.description
            }
            let moment = day.moment(hour: hour, minute: minute, in: calendar)
            return (now.map { min(moment, $0) } ?? moment).formatted(.iso8601)
        default:
            return value
        }
    }
}

/// A scenario step as written in demo.json (domain.md §7.4), after its dates are resolved.
nonisolated struct ScenarioStep: Decodable {
    var action: String?
    var at: Date?
    var payment: PaymentDraft?
    var paymentId: PaymentID?
    var note: String?
    var expenseId: ExpenseID?
    var by: PersonID?
    var loan: LoanDraft?
    var componentId: ComponentID?
    var status: ProjectComponent.Status?
    var actualCost: Int64?
    var paidBy: PersonID?
    var projectId: GroupID?
    var group: GroupDraft?
    var plan: Entitlement.Plan?
    var period: Entitlement.Period?
    var trialEndsAt: LocalDay?
}

/// demo.json's `profile` block (loads into `ProfileStore`).
nonisolated struct DemoProfile: Decodable {
    struct Avatar: Decodable {
        var kind: String
        var index: Int?
    }

    var name: String
    var avatar: Avatar
    var currencyCode: String
    var upiID: String
    var username: String?
    var pronoun: Pronoun?
    var signInMethod: String?
    var contact: String?
    var showPaymentToFriends: Bool?
    var paymentMethods: [PaymentMethod]
}
#endif
