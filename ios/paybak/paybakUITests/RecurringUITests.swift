import XCTest

/// Recurring expenses (screens-insights-ai §5, app-architecture §6.4 "M9 UI tests").
final class RecurringUITests: XCTestCase {
    override func setUp() {
        continueAfterFailure = false
    }

    /// Enter the Cooking gas amount: the draft leaves the list and the expense shows in Flat 302.
    @MainActor
    func testEnteringTheDraftAmountAddsTheExpense() {
        let app = XCUIApplication.launchPaybak(startScreen: .recurringFlat302)
        let enterAmount = app.buttons["recurring.draft.d-gas-09.enterAmount"]
        XCTAssertTrue(enterAmount.waitForExistence(timeout: 5))
        enterAmount.tap()
        XCTAssertTrue(app.element("screen.enterDraftAmount").waitForExistence(timeout: 3))
        XCTAssertFalse(app.buttons["enterAmount.action"].isEnabled)
        app.typeText("900")
        app.buttons["enterAmount.action"].tap()
        XCTAssertTrue(app.element(label: "Expense added").waitForExistence(timeout: 3))
        XCTAssertFalse(app.element("recurring.draft.d-gas-09").exists)
        app.buttons["recurring.back"].tap()
        XCTAssertTrue(app.element("screen.group").waitForExistence(timeout: 3))
        let row = app.descendants(matching: .any).matching(NSPredicate(format: "label CONTAINS %@", "Cooking gas")).firstMatch
        XCTAssertTrue(row.waitForExistence(timeout: 3))
    }

    /// The Repeat sheet from the form's Repeat row: Weekly changes the schedule lines, and Done writes
    /// the rule into the form.
    @MainActor
    func testRepeatSheetWritesTheRuleIntoTheForm() {
        let app = XCUIApplication.launchPaybak(startScreen: .recurringRepeat)
        XCTAssertTrue(app.element("repeatSheet.nextDraft").waitForLabel("Next draft: Wed 28 Oct", timeout: 5))
        app.buttons["repeatSheet.close"].tap()
        let repeatRow = app.buttons["addExpense.row.repeat"]
        XCTAssertTrue(repeatRow.waitForExistence(timeout: 3))
        repeatRow.tap()
        XCTAssertTrue(app.buttons["repeatSheet.freq.monthly"].waitForExistence(timeout: 3))
        XCTAssertTrue(app.buttons["repeatSheet.freq.monthly"].isSelected)
        app.buttons["repeatSheet.freq.weekly"].tap()
        XCTAssertTrue(app.element("repeatSheet.nextDraft").waitForLabel("Next draft: Mon 5 Oct"))
        app.buttons["repeatSheet.done"].tap()
        XCTAssertTrue(repeatRow.waitForExistence(timeout: 3))
        XCTAssertTrue(repeatRow.label.contains("Weekly"), repeatRow.label)
    }

    /// A rule opens the Repeat sheet; Never asks before it stops the rule.
    @MainActor
    func testStoppingARule() {
        let app = XCUIApplication.launchPaybak(startScreen: .recurringFlat302)
        let wifi = app.buttons["recurring.rule.r-wifi"]
        XCTAssertTrue(wifi.waitForExistence(timeout: 5))
        wifi.tap()
        XCTAssertTrue(app.element("screen.repeatRule").waitForExistence(timeout: 3))
        app.buttons["repeatSheet.freq.never"].tap()
        app.buttons["repeatSheet.done"].tap()
        let stop = app.alerts.buttons["Stop"]
        XCTAssertTrue(stop.waitForExistence(timeout: 3))
        stop.tap()
        XCTAssertFalse(wifi.waitForExistence(timeout: 1))
        XCTAssertTrue(app.buttons["recurring.rule.r-rent"].exists)
    }
}
