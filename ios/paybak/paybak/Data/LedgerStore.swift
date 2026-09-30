import Foundation
import Observation

/// The one owner of the ledger (app-architecture §3.5). Views read `snapshot` (recomputed after every
/// change) and the `books` queries; every write goes through `mutate`, which recomputes, saves
/// straight away and bumps `revision`. Shared actions are in `LedgerStore+Actions`; lanes add theirs
/// in `Data/Lanes/LedgerStore+<Module>.swift`.
@Observable
final class LedgerStore {
    private(set) var ledger: Ledger
    private(set) var snapshot: LedgerSnapshot
    /// Bumped on every change (the notification scheduler observes it).
    private(set) var revision = 0

    let clock: AppClock
    @ObservationIgnored let profileStore: ProfileStore
    @ObservationIgnored private let file: LedgerFile

    init(file: LedgerFile = .standard, clock: AppClock = AppClock(), profileStore: ProfileStore) {
        self.file = file
        self.clock = clock
        self.profileStore = profileStore
        let ledger = file.load() ?? Ledger()
        self.ledger = ledger
        snapshot = LedgerSnapshot(Books(ledger: ledger, now: clock.now, calendar: clock.calendar,
                                        defaultCurrency: profileStore.profile.defaultCurrency))
    }

    /// The ledger at the clock's now, for queries (`store.books.groupNets(id)`, `projectReport(id)` …).
    var books: Books {
        Books(ledger: ledger, now: clock.now, calendar: clock.calendar, defaultCurrency: profileStore.profile.defaultCurrency)
    }

    var isPro: Bool { ledger.settings.entitlement.isPro }

    /// The only write path: applies `change` to the books at the clock's now; when it doesn't throw,
    /// keeps the result, recomputes the snapshot, saves and bumps `revision`.
    @discardableResult
    func mutate<Result>(_ change: (inout Books) throws -> Result) rethrows -> Result {
        var books = books
        let result = try change(&books)
        commit(books.ledger)
        return result
    }

    /// Runs the scheduler up to now (launch, foreground, after the clock moves).
    func tick() {
        mutate { $0.tick(until: clock.now) }
    }

    /// Recomputes the snapshot without a change (the clock moved, or the default currency changed).
    func refresh() {
        snapshot = LedgerSnapshot(books)
    }

    /// Replaces the whole ledger (demo loading, an empty account).
    func replace(with ledger: Ledger) {
        commit(ledger)
    }

    /// Clears the ledger and deletes its file ("Reset onboarding", "Delete account").
    func reset() {
        ledger = Ledger()
        file.delete()
        snapshot = LedgerSnapshot(books)
        revision += 1
    }

    /// Waits for pending writes.
    func flush() {
        file.flush()
    }

    private func commit(_ ledger: Ledger) {
        self.ledger = ledger
        snapshot = LedgerSnapshot(books)
        revision += 1
        do {
            file.write(try LedgerFile.encoder().encode(ledger))
        } catch {
            assertionFailure("The ledger must always encode: \(error)")
        }
    }
}
