import XCTest

/// Settle up (app-architecture §6.3, M5): the breakdowns lead to the plan, Settle opens Record payment,
/// Remind sends or shares a reminder, and Not received keeps the balance.
final class SettleUITests: XCTestCase {
    override func setUp() {
        continueAfterFailure = false
    }

    @MainActor
    func testHomeOwedCardLeadsToThePlanAndSettleOpensRecordPayment() {
        let app = XCUIApplication.launchPaybak(startScreen: .homeActive)
        XCTAssertTrue(app.screen(.homeActive).waitForExistence(timeout: 6))
        app.buttons["home.balance.owed"].tap()
        XCTAssertTrue(app.element("owedBreakdown.row.p-rohan").waitForExistence(timeout: 3))
        XCTAssertEqual(app.element("owedBreakdown.total").label, "+₹2,900, from 4 people")
        app.element("owedBreakdown.settleUp").tap()
        XCTAssertTrue(app.element("screen.settleUp").waitForExistence(timeout: 3))
        XCTAssertTrue(app.staticTexts["2 payments to make"].exists)
        XCTAssertTrue(app.staticTexts["4 people owe you"].exists)
        app.element("settleUp.pay.p-kabir").tap()
        XCTAssertTrue(app.element("screen.recordPayment").waitForExistence(timeout: 3))
    }

    @MainActor
    func testOweBreakdownExplainsSimplifiedDebts() {
        let app = XCUIApplication.launchPaybak(startScreen: .settleOweBreakdown)
        XCTAssertTrue(app.element("oweBreakdown.row.p-kabir").waitForExistence(timeout: 6))
        XCTAssertEqual(app.element("oweBreakdown.footnote").label, "Goa Trip uses simplified debts, so you pay Kabir directly.")
        app.element("oweBreakdown.row.p-meera").tap()
        XCTAssertTrue(app.element("screen.friend").waitForExistence(timeout: 3))
    }

    @MainActor
    func testRemindSwitchesToneThenSendsInPaybak() {
        let app = XCUIApplication.launchPaybak(startScreen: .settleUp)
        app.element("settleUp.remind.p-rohan").tap()
        XCTAssertTrue(app.element("remind.message").waitForExistence(timeout: 3))
        XCTAssertTrue(app.staticTexts["Remind Rohan"].exists)
        XCTAssertTrue(messageText(app).hasPrefix("Hi Rohan! Just a gentle reminder about ₹800"))
        app.element("remind.tone.neutral").tap()
        XCTAssertTrue(messageText(app).hasPrefix("Hi Rohan, this is a reminder that ₹800"))
        app.element("remind.send").tap()
        XCTAssertTrue(app.element(label: "Reminder sent to Rohan").waitForExistence(timeout: 3))
        XCTAssertTrue(app.element("screen.remind").waitForNonExistence(timeout: 3))
        XCTAssertTrue(app.element("screen.settleUp").exists)
    }

    @MainActor
    func testShareHandsTheMessageToTheShareSheet() {
        let app = XCUIApplication.launchPaybak(startScreen: .settleRemind)
        XCTAssertTrue(app.element("remind.share").waitForExistence(timeout: 6))
        app.element("remind.share").tap()
        XCTAssertTrue(app.otherElements["ActivityListView"].waitForExistence(timeout: 5))
    }

    /// Not received sends the note and removes Esha's claim from Home; what she owes stays.
    @MainActor
    func testNotReceivedKeepsTheBalance() {
        let app = XCUIApplication.launchPaybak(startScreen: .homeConfirmPayment)
        XCTAssertTrue(app.screen(.homeConfirmPayment).waitForExistence(timeout: 6))
        app.buttons["home.confirmCard.notReceived"].tap()
        XCTAssertTrue(app.element("notReceived.send").waitForExistence(timeout: 3))
        XCTAssertTrue(app.staticTexts["Let Esha know you haven’t received ₹700?"].exists)
        XCTAssertTrue(app.staticTexts["Esha still owes you ₹700 until a payment is confirmed."].exists)
        app.element("notReceived.send").tap()
        XCTAssertTrue(app.element("screen.notReceived").waitForNonExistence(timeout: 3))
        XCTAssertTrue(app.screen(.homeActive).waitForExistence(timeout: 3))
        XCTAssertFalse(app.buttons["home.confirmCard.confirm"].exists)
        XCTAssertTrue(app.element("home.balance.owed").label.contains("+₹2,900"))
        XCTAssertTrue(app.element("home.balance.owed").label.contains("from 4 people"))
    }

    /// Remind from Home's Due soon row: the toast sits over the tab root.
    @MainActor
    func testRemindFromHomeSendsInPaybak() {
        let app = XCUIApplication.launchPaybak(startScreen: .homeActive)
        XCTAssertTrue(app.screen(.homeActive).waitForExistence(timeout: 6))
        app.buttons["home.due.p-rohan.action"].tap()
        XCTAssertTrue(app.element("remind.send").waitForExistence(timeout: 3))
        app.element("remind.send").tap()
        XCTAssertTrue(app.element(label: "Reminder sent to Rohan").waitForExistence(timeout: 3))
        XCTAssertTrue(app.screen(.homeActive).exists)
    }

    /// The Remind sheet's editable message (a multi-line text field).
    private func messageText(_ app: XCUIApplication) -> String {
        let editor = app.textViews.matching(identifier: "remind.message").firstMatch
        let field = editor.exists ? editor : app.textFields.matching(identifier: "remind.message").firstMatch
        return (field.value as? String) ?? ""
    }
}
