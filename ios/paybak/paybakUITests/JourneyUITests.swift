import XCTest

/// The main journey on a new account, across every module: onboarding → the first expense → the
/// balance it leaves on Home → settling it from Settle up → the activity it all writes, kept
/// across a relaunch.
final class JourneyUITests: XCTestCase {
    override func setUp() {
        continueAfterFailure = false
    }

    @MainActor
    func testNewAccountAddsAnExpenseSettlesItAndSeesTheActivity() {
        let app = XCUIApplication.launchPaybak()
        onboard(app, as: "Maya Rao")

        // The first expense: ₹1,200 for dinner, split with a new guest.
        app.buttons["home.firstDay.addExpense"].tap()
        XCTAssertTrue(app.element("screen.addExpense").waitForExistence(timeout: 3))
        app.typeText("1200")
        app.element("addExpense.addPeople").tap()
        let search = app.textFields["splitWith.search"]
        XCTAssertTrue(search.waitForExistence(timeout: 3))
        search.tap()
        search.typeText("Sam")
        app.element("splitWith.addGuest").tap()
        app.buttons["splitWith.done"].tap()
        app.element("addExpense.title").tap()
        app.typeText("Dinner\n")
        XCTAssertTrue(app.buttons["addExpense.row.split"].label.contains("₹600 each"))
        app.buttons["addExpense.action"].tap()
        XCTAssertTrue(app.element(label: "Expense added").waitForExistence(timeout: 3))
        XCTAssertTrue(app.element("screen.expense").exists)
        app.buttons["expense.back"].tap()

        // Home shows what Sam owes, and Settle up asks Sam for it.
        XCTAssertTrue(app.screen(.homeActive).waitForExistence(timeout: 3))
        XCTAssertTrue(app.element("home.balance.owed").label.contains("+₹600"))
        app.buttons["home.settleUp"].tap()
        XCTAssertTrue(app.element("screen.settleUp").waitForExistence(timeout: 3))
        let samRow = app.descendants(matching: .any)
            .matching(NSPredicate(format: "identifier BEGINSWITH 'settleUp.row.'")).firstMatch
        XCTAssertTrue(samRow.label.contains("Sam"))
        XCTAssertTrue(samRow.label.contains("₹600"))

        // Sam pays it back: recorded from Sam's page, it lands confirmed.
        samRow.tap()
        XCTAssertTrue(app.element("screen.friend").waitForExistence(timeout: 3))
        app.buttons["friend.recordPayment"].tap()
        XCTAssertTrue(app.element("screen.recordPayment").waitForExistence(timeout: 3))
        app.buttons["recordPayment.action"].tap()
        XCTAssertTrue(app.element(label: "Payment recorded").waitForExistence(timeout: 3))
        XCTAssertTrue(app.element("screen.payment").exists)
        app.buttons["paymentRecorded.back"].tap()
        let balance = app.element("friend.balance")
        XCTAssertTrue(balance.waitForExistence(timeout: 3))
        XCTAssertTrue(balance.label.contains("Settled"))
        app.buttons["friend.back"].tap()
        XCTAssertTrue(app.element("settleUp.allSettled").waitForExistence(timeout: 3))
        app.buttons["settleUp.back"].tap()
        XCTAssertTrue(app.screen(.homeAllSettled).waitForExistence(timeout: 3))

        // Activity has both, newest first.
        app.buttons["home.tab.activity"].tap()
        let timeline = app.element("activity.timeline")
        XCTAssertTrue(timeline.waitForExistence(timeout: 3))
        let rows = app.descendants(matching: .any).matching(NSPredicate(format: "identifier BEGINSWITH 'activity.row.'"))
        XCTAssertEqual(rows.count, 2)
        XCTAssertTrue(rows.element(boundBy: 0).label.contains("Sam paid you"))
        XCTAssertTrue(rows.element(boundBy: 1).label.contains("Dinner"))

        // Everything is still there after a relaunch.
        app.terminate()
        let relaunched = XCUIApplication.launchPaybak(resetOnboarding: false)
        XCTAssertTrue(relaunched.screen(.homeAllSettled).waitForExistence(timeout: 5))
        relaunched.buttons["home.tab.activity"].tap()
        XCTAssertTrue(relaunched.element("activity.timeline").waitForExistence(timeout: 3))
        XCTAssertEqual(relaunched.descendants(matching: .any)
            .matching(NSPredicate(format: "identifier BEGINSWITH 'activity.row.'")).count, 2)
    }

    /// The short way through onboarding: Apple, a name, the suggested currency, no UPI ID and no
    /// notifications, onto Home's first day.
    @MainActor
    private func onboard(_ app: XCUIApplication, as name: String) {
        XCTAssertTrue(app.screen(.welcome1).waitForExistence(timeout: 5))
        app.element("welcome.skip").tap()
        XCTAssertTrue(app.screen(.getStarted).waitForExistence(timeout: 3))
        app.element("getStarted.apple").tap()
        let field = app.textFields["setup1.name"]
        XCTAssertTrue(field.waitForExistence(timeout: 3))
        field.tap()
        field.typeText(name)
        app.buttons["setup1.continue"].tap()
        XCTAssertTrue(app.buttons["setup2.continue"].waitForExistence(timeout: 3))
        app.buttons["setup2.continue"].tap()
        XCTAssertTrue(app.screen(.setup3).waitForExistence(timeout: 3))
        app.buttons["setup.skip"].tap()
        XCTAssertTrue(app.screen(.setup4).waitForExistence(timeout: 3))
        app.buttons["setup.skip"].tap()
        XCTAssertTrue(app.screen(.allSet).waitForExistence(timeout: 5))
        app.buttons["allSet.goHome"].tap()
        XCTAssertTrue(app.screen(.homeFirstDay).waitForExistence(timeout: 3))
    }
}
