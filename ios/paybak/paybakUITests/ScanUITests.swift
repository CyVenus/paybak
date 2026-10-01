import XCTest

/// Scan receipt (screens-insights-ai §4, app-architecture §6.4 "M9 UI tests").
final class ScanUITests: XCTestCase {
    override func setUp() {
        continueAfterFailure = false
    }

    /// Add expense with Esha and Dev → Add receipt → the simulated camera's shot is read (6 items) →
    /// assign as drawn → Continue fills the form: ₹2,300, "Itemized · 3 people".
    @MainActor
    func testScanFillsTheExpenseForm() {
        let app = XCUIApplication.launchPaybak(startScreen: .addExpenseEmpty, pro: true)
        XCTAssertTrue(app.element("screen.addExpense").waitForExistence(timeout: 5))
        app.element("addExpense.addPeople").tap()
        XCTAssertTrue(app.element("screen.pickPeople").waitForExistence(timeout: 3))
        app.buttons["splitWith.friend.p-esha"].tap()
        app.buttons["splitWith.friend.p-dev"].tap()
        app.buttons["splitWith.done"].tap()

        // Put the keyboard away so the Receipt row is in reach.
        app.element("addExpense.title").tap()
        app.typeText("\n")
        app.buttons["addExpense.row.receipt"].tap()
        XCTAssertTrue(app.element("scanReceipt.state.camera").waitForExistence(timeout: 3))
        app.buttons["scan.shutter"].tap()
        XCTAssertTrue(app.element("scanReceipt.state.review").waitForExistence(timeout: 10))
        XCTAssertTrue(app.staticTexts["We found 6 items."].exists)
        app.buttons["scanReview.confirm"].tap()

        XCTAssertTrue(app.element("scanReceipt.state.assign").waitForExistence(timeout: 3))
        XCTAssertFalse(app.buttons["scanAssign.continue"].isEnabled)
        let drawn = [["Dev"], ["Esha"], ["You"], ["You"], ["You", "Esha", "Dev"], ["You", "Esha", "Dev"]]
        for (index, people) in drawn.enumerated() {
            for person in people {
                app.element("scanAssign.item.\(index)").buttons[person].tap()
            }
        }
        XCTAssertTrue(app.element("scanAssign.status").staticTexts["All items assigned"].exists)
        app.buttons["scanAssign.continue"].tap()

        XCTAssertTrue(app.element("screen.addExpense").waitForExistence(timeout: 3))
        XCTAssertTrue(app.buttons["addExpense.row.split"].waitForExistence(timeout: 3))
        XCTAssertTrue(app.buttons["addExpense.row.split"].label.contains("Itemized · 3 people"))
        XCTAssertEqual(app.element("addExpense.amount").value as? String, "₹2,300")
        XCTAssertTrue(app.staticTexts["From receipt · 6 items"].exists)
    }

    /// A scanned expense's Split row goes back to Assign items with the assignment kept; giving the
    /// brownie to Esha instead moves ₹276 of the split to her.
    @MainActor
    func testSplitReopensAssignItems() {
        let app = XCUIApplication.launchPaybak(startScreen: .scanAddExpense, pro: true)
        let split = app.buttons["addExpense.row.split"]
        XCTAssertTrue(split.waitForExistence(timeout: 5))
        split.tap()
        XCTAssertTrue(app.element("scanReceipt.state.assign").waitForExistence(timeout: 3))
        XCTAssertTrue(app.element("scanAssign.status").staticTexts["All items assigned"].exists)
        XCTAssertTrue(app.element("scanAssign.status").staticTexts["₹690"].exists)
        app.element("scanAssign.item.3").buttons["You"].tap()
        app.element("scanAssign.item.3").buttons["Esha"].tap()
        XCTAssertTrue(app.element("scanAssign.status").staticTexts["₹713"].exists)
        XCTAssertTrue(app.element("scanAssign.status").staticTexts["₹897"].exists)
        app.buttons["scanAssign.continue"].tap()

        XCTAssertTrue(app.element("screen.addExpense").waitForExistence(timeout: 3))
        XCTAssertTrue(app.element("addExpense.title").waitForExistence(timeout: 3))
        XCTAssertEqual(app.element("addExpense.title").value as? String, "Lunch at Leopold Cafe")
        XCTAssertTrue(app.buttons["addExpense.row.split"].label.contains("Itemized · 3 people"))
        XCTAssertTrue(app.buttons["addExpense.row.receipt"].label.contains("Attached"))
    }

    /// Free plan: the shutter hands the photo back to the form without reading it. (The form itself
    /// sends free users to the photo picker; the debug start screen is the only way here.)
    @MainActor
    func testFreePlanDoesNotRead() {
        let app = XCUIApplication.launchPaybak(startScreen: .scanCamera, pro: false)
        let shutter = app.buttons["scan.shutter"]
        XCTAssertTrue(shutter.waitForExistence(timeout: 5))
        shutter.tap()
        XCTAssertTrue(app.element("screen.addExpense").waitForExistence(timeout: 5))
        XCTAssertFalse(app.element("scanReceipt.state.review").exists)
        XCTAssertFalse(app.staticTexts["From receipt · 6 items"].exists)
    }
}
