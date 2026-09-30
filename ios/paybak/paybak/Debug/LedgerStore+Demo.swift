#if DEBUG
import Foundation

/// Demo loading for the debug hooks and menu (app-architecture §3.8, §3.10): the base records plus
/// named scenarios, at the Figma date (clock pinned to Wed 30 Sep 2026 21:15) or around today.
extension LedgerStore {
    /// Loads the demo profile and ledger, then applies `scenarios`. At the Figma date the clock is
    /// pinned (a `setClock` scenario may move it further).
    func loadDemo(atFigmaDate: Bool = true, scenarios: [String] = []) throws {
        let seed = try DemoSeed.bundled()
        let calendar = clock.calendar
        let now = atFigmaDate ? DemoSeed.figmaNow(calendar: calendar) : Date()
        let anchor = LocalDay(now, calendar: calendar)
        let loaded = try seed.load(anchor: anchor, now: now, calendar: calendar, scenarios: scenarios)
        DebugState.demoAnchor = anchor
        DebugState.pinnedClock = atFigmaDate || loaded.books.now != now ? loaded.books.now : nil
        clock.pin(DebugState.pinnedClock)
        profileStore.applyDemo(loaded.profile)
        replace(with: loaded.books.ledger)
    }

    /// Applies one more seed scenario to the current data, as of the clock's now.
    func applyScenario(_ name: String) throws {
        let seed = try DemoSeed.bundled()
        var books = books
        try seed.apply(name, to: &books, anchor: DebugState.demoAnchor ?? clock.today)
        if books.now != clock.now {
            DebugState.pinnedClock = books.now
            clock.pin(books.now)
        }
        replace(with: books.ledger)
    }

    /// Every scenario the bundled demo defines.
    static var scenarioNames: [String] {
        (try? DemoSeed.bundled().scenarioNames) ?? []
    }

    /// Pins the clock (nil = real time), remembers it across launches, and runs `tick`.
    func setClock(_ moment: Date?) {
        DebugState.pinnedClock = moment
        clock.pin(moment)
        tick()
    }
}

extension ProfileStore {
    /// The demo profile (Arjun Mehta, INR, arjun@okaxis + HDFC ···· 4821, @arjun), onboarding complete.
    func applyDemo(_ demo: DemoProfile) {
        update { profile in
            profile.name = demo.name
            profile.avatar = demo.avatar.kind == "preset" ? .preset(demo.avatar.index ?? 0) : nil
            profile.currencyCode = demo.currencyCode
            profile.upiID = demo.upiID
            profile.username = demo.username
            profile.pronoun = demo.pronoun
            profile.signInMethod = demo.signInMethod.flatMap(UserProfile.SignInMethod.init(rawValue:))
            profile.contact = demo.contact
            profile.onboardingComplete = true
            profile.showPaymentToFriends = demo.showPaymentToFriends ?? true
            profile.paymentMethods = demo.paymentMethods
        }
    }
}

/// Debug state that survives relaunches: the pinned clock and the demo's load day.
enum DebugState {
    private static let clockKey = "debug.pinnedClock"
    private static let anchorKey = "debug.demoAnchor"

    static var pinnedClock: Date? {
        get { UserDefaults.standard.object(forKey: clockKey) as? Date }
        set { UserDefaults.standard.set(newValue, forKey: clockKey) }
    }

    static var demoAnchor: LocalDay? {
        get { UserDefaults.standard.string(forKey: anchorKey).flatMap(LocalDay.init(string:)) }
        set { UserDefaults.standard.set(newValue?.description, forKey: anchorKey) }
    }
}
#endif
