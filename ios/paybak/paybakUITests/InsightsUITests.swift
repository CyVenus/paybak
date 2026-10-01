import XCTest

/// Insights (screens-insights-ai §2, app-architecture §6.4 "M9 UI tests").
final class InsightsUITests: XCTestCase {
    override func setUp() {
        continueAfterFailure = false
    }

    /// Free plan: the report is locked; See Pro → trial → Done unlocks it in place.
    @MainActor
    func testLockedInsightsUnlockWithTheTrial() {
        let app = XCUIApplication.launchPaybak(startScreen: .insightsLocked)
        XCTAssertTrue(app.element("insights.locked").waitForExistence(timeout: 5))
        app.buttons["insights.seePro"].tap()
        XCTAssertTrue(app.element("screen.paywall").waitForExistence(timeout: 3))
        app.buttons["paywall.cta"].tap()
        XCTAssertTrue(app.element("paywall.state.welcome").waitForExistence(timeout: 3))
        app.buttons["proWelcome.done"].tap()
        XCTAssertTrue(app.element("insights.total").waitForLabel("₹23,300"))
        XCTAssertFalse(app.element("insights.locked").exists)
    }

    /// Month ‹ recomputes the report for August; › comes back to September.
    @MainActor
    func testMonthNavigationChangesTheReport() {
        let app = XCUIApplication.launchPaybak(startScreen: .insightsSeptember)
        XCTAssertTrue(app.element("insights.month").waitForLabel("September 2026", timeout: 5))
        XCTAssertFalse(app.buttons["insights.monthNext"].isEnabled)
        XCTAssertEqual(app.element("insights.trend").label, "Up 5% from August")
        app.buttons["insights.monthPrev"].tap()
        XCTAssertTrue(app.element("insights.month").waitForLabel("August 2026"))
        XCTAssertTrue(app.element("insights.total").waitForLabel("₹22,200"))
        XCTAssertEqual(app.element("insights.trend").label, "Up 8% from July")
        app.buttons["insights.monthNext"].tap()
        XCTAssertTrue(app.element("insights.month").waitForLabel("September 2026"))
    }

    /// Who you spent with: Friends lists people; a category row opens its activity log.
    @MainActor
    func testFriendsAndCategoryRows() {
        let app = XCUIApplication.launchPaybak(startScreen: .insightsScrolled)
        let friends = app.buttons["insights.who.friends"]
        XCTAssertTrue(friends.waitForExistence(timeout: 5))
        friends.tap()
        XCTAssertTrue(app.element("insights.friend.p-esha").waitForExistence(timeout: 3))
        app.buttons["insights.category.travel"].tap()
        XCTAssertTrue(app.element("screen.activityLog").waitForExistence(timeout: 3))
    }
}
