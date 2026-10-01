import XCTest

/// Home's flows (app-architecture §6.5): every state, the confirm card, and where each element leads.
final class HomeUITests: XCTestCase {
    override func setUp() {
        continueAfterFailure = false
    }

    @MainActor
    func testOnboardingLandsOnFirstDayAndAddExpenseOpensTheForm() {
        let app = XCUIApplication.launchPaybak(startScreen: .allSet)
        app.buttons["allSet.goHome"].tap()
        XCTAssertTrue(app.screen(.homeFirstDay).waitForExistence(timeout: 5))
        XCTAssertTrue(app.element(label: "Nothing here yet.").exists)
        app.buttons["home.firstDay.addExpense"].tap()
        XCTAssertTrue(app.element("screen.addExpense").waitForExistence(timeout: 3))
    }

    @MainActor
    func testFirstDayInviteOpensAddFriend() {
        let app = XCUIApplication.launchPaybak(startScreen: .homeFirstDay)
        XCTAssertTrue(app.screen(.homeFirstDay).waitForExistence(timeout: 5))
        app.buttons["home.firstDay.invite"].tap()
        XCTAssertTrue(app.element("screen.addFriend").waitForExistence(timeout: 3))
    }

    @MainActor
    func testActiveShowsTheLedgerNumbers() {
        let app = XCUIApplication.launchPaybak(startScreen: .homeActive)
        XCTAssertTrue(app.screen(.homeActive).waitForExistence(timeout: 5))
        XCTAssertTrue(app.element("home.greeting").label.hasPrefix("Good evening, Arjun"))
        XCTAssertTrue(app.element("home.balance.owed").label.contains("+₹2,900"))
        XCTAssertTrue(app.element("home.balance.owed").label.contains("from 4 people"))
        XCTAssertTrue(app.element("home.balance.owe").label.contains("−₹1,850"))
        XCTAssertTrue(app.element("home.balance.owe").label.contains("across 2 groups"))
        XCTAssertTrue(app.element("home.due.p-rohan").label.contains("Overdue 3 days"))
        XCTAssertTrue(app.element("home.due.g-goa").label.contains("Due Fri"))
        XCTAssertTrue(app.element("home.activity.e-olive").exists)
    }

    @MainActor
    func testConfirmingTheClaimUpdatesHome() {
        let app = XCUIApplication.launchPaybak(startScreen: .homeConfirmPayment)
        XCTAssertTrue(app.screen(.homeConfirmPayment).waitForExistence(timeout: 5))
        XCTAssertTrue(app.element(label: "Esha says she paid you ₹700").exists)
        XCTAssertTrue(app.element("home.balance.owed").label.contains("+₹2,900"))
        app.buttons["home.confirmCard.confirm"].tap()
        // The toast follows the card's Confirmed state and its 1.05 s hold.
        XCTAssertTrue(app.element("toast").waitForExistence(timeout: 4))
        XCTAssertEqual(app.element("toast").label, "Payment confirmed")
        XCTAssertTrue(app.screen(.homeActive).waitForExistence(timeout: 4))
        XCTAssertFalse(app.buttons["home.confirmCard.confirm"].exists)
        XCTAssertTrue(app.element("home.balance.owed").label.contains("+₹2,200"))
        XCTAssertTrue(app.element("home.balance.owed").label.contains("from 3 people"))
        XCTAssertTrue(app.element("home.activity.pay-esha-olive").label.contains("Esha paid you"))
    }

    @MainActor
    func testNotReceivedOpensItsSheet() {
        let app = XCUIApplication.launchPaybak(startScreen: .homeConfirmPayment)
        XCTAssertTrue(app.screen(.homeConfirmPayment).waitForExistence(timeout: 5))
        app.buttons["home.confirmCard.notReceived"].tap()
        XCTAssertTrue(app.element("screen.notReceived").waitForExistence(timeout: 3))
    }

    @MainActor
    func testHeaderLinks() {
        let app = XCUIApplication.launchPaybak(startScreen: .homeActive)
        XCTAssertTrue(app.screen(.homeActive).waitForExistence(timeout: 5))
        app.buttons["home.bell"].tap()
        XCTAssertTrue(app.element("screen.notifications").waitForExistence(timeout: 3))
        goBack(to: "home.assistant", in: app)
        // Free plan: the sparkle opens the paywall on its way to Ask Paybak.
        app.buttons["home.assistant"].tap()
        XCTAssertTrue(app.element("screen.paywall").waitForExistence(timeout: 3))
    }

    @MainActor
    func testBalanceCardsAndSettleUp() {
        let app = XCUIApplication.launchPaybak(startScreen: .homeActive)
        XCTAssertTrue(app.screen(.homeActive).waitForExistence(timeout: 5))
        app.buttons["home.balance.owed"].tap()
        XCTAssertTrue(app.element("screen.owedBreakdown").waitForExistence(timeout: 3))
        goBack(to: "home.balance.owe", in: app)
        app.buttons["home.balance.owe"].tap()
        XCTAssertTrue(app.element("screen.oweBreakdown").waitForExistence(timeout: 3))
        goBack(to: "home.settleUp", in: app)
        app.buttons["home.settleUp"].tap()
        XCTAssertTrue(app.element("screen.settleUp").waitForExistence(timeout: 3))
    }

    @MainActor
    func testDueSoonActionsAndRows() {
        let app = XCUIApplication.launchPaybak(startScreen: .homeActive)
        XCTAssertTrue(app.screen(.homeActive).waitForExistence(timeout: 5))
        app.buttons["home.due.g-goa.action"].tap()
        XCTAssertTrue(app.element("screen.recordPayment").waitForExistence(timeout: 3))

        let relaunched = XCUIApplication.launchPaybak(startScreen: .homeActive)
        XCTAssertTrue(relaunched.screen(.homeActive).waitForExistence(timeout: 5))
        relaunched.buttons["home.due.p-rohan.action"].tap()
        XCTAssertTrue(relaunched.element("screen.remind").waitForExistence(timeout: 3))

        let again = XCUIApplication.launchPaybak(startScreen: .homeActive)
        XCTAssertTrue(again.screen(.homeActive).waitForExistence(timeout: 5))
        again.buttons["home.due.p-rohan"].tap()
        XCTAssertTrue(again.element("screen.friend").waitForExistence(timeout: 3))
        goBack(to: "home.due.g-goa", in: again)
        again.buttons["home.due.g-goa"].tap()
        XCTAssertTrue(again.element("screen.group").waitForExistence(timeout: 3))
    }

    @MainActor
    func testRecentActivityRowsAndSeeAll() {
        let app = XCUIApplication.launchPaybak(startScreen: .homeActive)
        XCTAssertTrue(app.screen(.homeActive).waitForExistence(timeout: 5))
        app.buttons["home.activity.e-olive"].tap()
        XCTAssertTrue(app.element("screen.expense").waitForExistence(timeout: 3))
        goBack(to: "home.seeAll", in: app)
        app.buttons["home.seeAll"].tap()
        XCTAssertTrue(app.element("screen.activity").waitForExistence(timeout: 3))
    }

    @MainActor
    func testDebugMenuSwitchesHomeStates() {
        let app = XCUIApplication.launchPaybak(startScreen: .homeActive)
        XCTAssertTrue(app.screen(.homeActive).waitForExistence(timeout: 5))
        app.element("home.logo").press(forDuration: 1)
        XCTAssertTrue(app.element("screen.debugMenu").waitForExistence(timeout: 3))
        tapDebugRow("Home: All settled", in: app)
        XCTAssertTrue(app.screen(.homeAllSettled).waitForExistence(timeout: 5))
        XCTAssertTrue(app.element(label: "You’re all square.").exists)
    }

    /// Swipes back from a pushed screen until Home's `button` can be tapped again.
    @MainActor
    private func goBack(to button: String, in app: XCUIApplication) {
        for _ in 0..<2 {
            app.swipeBackFromLeftEdge()
            if app.buttons[button].waitForExistence(timeout: 3), app.buttons[button].isHittable { return }
        }
        XCTFail("Swiping back didn't return to Home (\(button))")
    }

    /// Scrolls the debug menu down to the row titled `title` (the module sections follow the long
    /// Scenarios and Load screen lists), then taps it.
    @MainActor
    private func tapDebugRow(_ title: String, in app: XCUIApplication) {
        let row = app.buttons["debugMenu.\(title)"]
        var swipes = 0
        while !(row.exists && row.isHittable), swipes < 60 {
            app.element("screen.debugMenu").swipeUp(velocity: .slow)
            swipes += 1
        }
        row.tap()
    }
}
