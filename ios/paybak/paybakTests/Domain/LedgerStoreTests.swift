import Foundation
import Testing
@testable import paybak

/// The store: persistence, round trips, actions' side effects and the snapshot's cost.
@MainActor
struct LedgerStoreTests {
    private let directory = FileManager.default.temporaryDirectory.appending(path: "LedgerStoreTests.\(UUID().uuidString)")

    private func makeStore() -> LedgerStore {
        let clock = AppClock(pinned: DemoFixture.figmaNow, calendar: DemoFixture.calendar)
        let suite = "LedgerStoreTests.\(UUID().uuidString)"
        let profile = ProfileStore(defaults: UserDefaults(suiteName: suite)!, directory: directory)
        return LedgerStore(file: LedgerFile(url: directory.appending(path: "ledger.json")), clock: clock, profileStore: profile)
    }

    @Test func jsonRoundTrip() throws {
        let ledger = DemoFixture.load("eshaClaimsPayment", "lendDev").ledger
        let data = try LedgerFile.encoder().encode(ledger)
        #expect(try LedgerFile.decoder().decode(Ledger.self, from: data) == ledger)
    }

    @Test func toleratesMissingAndUnknownKeys() throws {
        let data = Data(#"{"schemaVersion": 1, "people": [], "somethingNew": true}"#.utf8)
        let ledger = try LedgerFile.decoder().decode(Ledger.self, from: data)
        #expect(ledger.isEmpty)
        #expect(ledger.settings.reminderSchedule.time == "21:00")
    }

    @Test func persistsAcrossLaunches() throws {
        let store = makeStore()
        store.replace(with: DemoFixture.load().ledger)
        try store.confirmPaymentAfterClaim()
        store.flush()
        let reopened = LedgerStore(file: LedgerFile(url: directory.appending(path: "ledger.json")), clock: store.clock,
                                   profileStore: store.profileStore)
        #expect(reopened.ledger == store.ledger)
        #expect(reopened.snapshot.home.totals.owed == rupees(2200))
    }

    @Test func unreadableFileStartsEmpty() throws {
        let url = directory.appending(path: "ledger.json")
        try FileManager.default.createDirectory(at: directory, withIntermediateDirectories: true)
        try Data("not json".utf8).write(to: url)
        let store = LedgerStore(file: LedgerFile(url: url), clock: AppClock(), profileStore: makeStore().profileStore)
        #expect(store.ledger.isEmpty)
        #expect(try FileManager.default.contentsOfDirectory(atPath: directory.path()).contains { $0.hasPrefix("ledger.corrupt-") })
    }

    @Test func addExpenseRotatesAndLogs() throws {
        let store = makeStore()
        store.replace(with: DemoFixture.load("empty").ledger)
        store.addFriend(Person(id: "p-a", name: "Asha Rao", addedAt: store.clock.now))
        store.addFriend(Person(id: "p-b", name: "Bina Das", addedAt: store.clock.now))
        let rows = [Person.me, "p-a", "p-b"].map { SplitRow(personId: $0) }
        let draft = ExpenseDraft(amount: rupees(1000), currency: "INR", date: store.clock.today, rows: rows)
        let first = try store.addExpense(draft)
        let second = try store.addExpense(draft)
        #expect(store.ledger.expense(first)!.split.rows.map(\.share) == [33_334, 33_333, 33_333])
        #expect(store.ledger.expense(second)!.split.rows.map(\.share) == [33_333, 33_334, 33_333])
        #expect(store.ledger.expense(first)!.history.map(\.kind) == [.created])
        #expect(store.snapshot.home.totals.owed == 33_333 * 4 + 1)
        #expect(throws: LedgerError.needsSomeoneElse) {
            try store.addExpense(ExpenseDraft(amount: 100, currency: "INR", date: store.clock.today, rows: [SplitRow(personId: Person.me)]))
        }
    }

    @Test func deleteRestoreAndFlag() throws {
        let store = makeStore()
        store.replace(with: DemoFixture.load().ledger)
        try store.deleteExpense("e-goa-villa")
        #expect(store.books.groupNets("g-goa")[Person.me] == -rupees(1400) + rupees(3600))
        #expect(store.snapshot.recentlyDeleted.first?.expense.id == "e-goa-villa")
        try store.restoreExpense("e-goa-villa")
        #expect(store.ledger.expense("e-goa-villa")!.history.suffix(2).map(\.kind) == [.deleted, .restored])
        try store.flagExpense("e-goa-seafood", by: "p-esha", note: "Check it")
        try store.resolveFlag("e-goa-seafood")
        #expect(store.ledger.expense("e-goa-seafood")!.flag == nil)
        #expect(store.ledger.expense("e-goa-seafood")!.history.last?.kind == .flagResolved)
    }

    @Test func recordPaymentStatusRule() throws {
        let store = makeStore()
        store.replace(with: DemoFixture.load().ledger)
        let toMeera = try store.recordPayment(PaymentDraft(fromId: Person.me, toId: "p-meera", amount: rupees(450), currency: "INR",
                                                            date: store.clock.today, groupId: "g-flat302"))
        #expect(store.ledger.payment(toMeera)?.status == .pending)
        let fromRohan = try store.recordPayment(PaymentDraft(fromId: "p-rohan", toId: Person.me, amount: rupees(800), currency: "INR",
                                                              date: store.clock.today, expenseId: "e-movie", recordedBy: Person.me))
        // Recorded by its receiver: confirmed at once.
        #expect(store.ledger.payment(fromRohan)?.status == .confirmed)
        #expect(store.snapshot.home.totals.owed == rupees(2100))
        try store.cancelPayment(toMeera)
        #expect(store.ledger.payment(toMeera)?.status == .cancelled)
        #expect(throws: LedgerError.self) { try store.leaveGroup("g-goa") }
    }

    @Test func snapshotIsQuick() {
        let books = DemoFixture.load("eshaClaimsPayment")
        let clock = ContinuousClock()
        let elapsed = clock.measure { _ = LedgerSnapshot(books) }
        // Debug builds are slow; the budget is ~5 ms in release.
        #expect(elapsed < .milliseconds(250))
    }
}

private extension LedgerStore {
    func confirmPaymentAfterClaim() throws {
        try mutate { books in
            try books.recordPayment(PaymentDraft(id: "pay-esha", fromId: "p-esha", toId: Person.me, amount: rupees(700), currency: "INR",
                                                 date: books.today, expenseId: "e-olive", recordedBy: "p-esha"), at: books.now)
        }
        try confirmPayment("pay-esha")
    }
}
