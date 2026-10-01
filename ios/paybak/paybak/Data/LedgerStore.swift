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
    @ObservationIgnored private var changeObservers: [(_ old: Ledger, _ new: Ledger) -> Void] = []

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

    /// The active RevenueCat `paybak_pro` entitlement, mirrored from `SubscriptionStore`.
    var hasStoreEntitlement = false

    /// Whether Pro is unlocked. Debug builds also honour the ledger's mock entitlement (the debug menu,
    /// `-pro`, seed scenarios), so the gates can be tested without a purchase.
    var isPro: Bool {
        #if DEBUG
        hasStoreEntitlement || ledger.settings.entitlement.isPro
        #else
        hasStoreEntitlement
        #endif
    }

    /// The only write path: applies `change` to the books at the clock's now; when it doesn't throw,
    /// keeps the result, recomputes the snapshot, saves and bumps `revision`, then tells the change
    /// observers.
    @discardableResult
    func mutate<Result>(_ change: (inout Books) throws -> Result) rethrows -> Result {
        let old = ledger
        var books = books
        let result = try change(&books)
        commit(books.ledger)
        for observer in changeObservers {
            observer(old, ledger)
        }
        return result
    }

    /// Calls `observer` after every `mutate` with the ledger before and after (payment approvals,
    /// the debug auto-approver). `replace` and `reset` swap the whole ledger (demo loading, a new
    /// account) and aren't reported.
    func observeChanges(_ observer: @escaping (_ old: Ledger, _ new: Ledger) -> Void) {
        changeObservers.append(observer)
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
