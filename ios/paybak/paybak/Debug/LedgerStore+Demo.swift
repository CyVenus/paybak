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

    /// Runs one more seed scenario on today's ledger ("Apply <name>", `-scenario`): its `D` dates
    /// resolve against the clock's today.
    func applyScenario(_ name: String) throws {
        let seed = try DemoSeed.bundled()
        var books = books
        try seed.apply(name, to: &books, anchor: clock.today)
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

/// Debug state that survives relaunches: the pinned clock, the demo's load day and the friends'
/// auto-approve.
enum DebugState {
    private static let clockKey = "debug.pinnedClock"
    private static let anchorKey = "debug.demoAnchor"
    /// Also a launch argument: `-autoApprove NO` turns it off for that launch (UI tests).
    private static let autoApproveKey = "autoApprove"
    /// Only a launch argument: `-autoApproveAfter 12` waits 12 s instead of 5.
    private static let autoApproveAfterKey = "autoApproveAfter"

    /// Friends confirm the payments you record to them after 5 s (`DebugAutoApprover`). On until
    /// turned off in the debug menu. A launch argument arrives as the string "YES" or "NO", which
    /// `bool(forKey:)` reads.
    static var autoApprovesPayments: Bool {
        get {
            UserDefaults.standard.object(forKey: autoApproveKey) == nil
                || UserDefaults.standard.bool(forKey: autoApproveKey)
        }
        set { UserDefaults.standard.set(newValue, forKey: autoApproveKey) }
    }

    /// How long a friend takes to confirm: 5 s, or the `-autoApproveAfter` launch argument.
    static var autoApproveDelay: Duration {
        let seconds = UserDefaults.standard.double(forKey: autoApproveAfterKey)
        return seconds > 0 ? .seconds(seconds) : .seconds(5)
    }

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
