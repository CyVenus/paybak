import XCTest

/// Ask Paybak (screens-insights-ai §3, app-architecture §6.4 "M9 UI tests").
final class AskUITests: XCTestCase {
    override func setUp() {
        continueAfterFailure = false
    }

    /// The first suggested prompt answers with Home's numbers; Remind Rohan opens the Remind sheet.
    @MainActor
    func testWhoOwesMeAnswersFromLiveBalances() {
        let app = XCUIApplication.launchPaybak(startScreen: .askStart)
        let prompt = app.buttons["ask.prompt.0"]
        XCTAssertTrue(prompt.waitForExistence(timeout: 5))
        prompt.tap()
        let answer = "4 people owe you ₹2,900: Rohan ₹800 (overdue since 27 Sep), and Priya, Esha and Dev ₹700 each for tonight’s dinner."
        XCTAssertTrue(app.staticTexts[answer].waitForExistence(timeout: 3))
        XCTAssertTrue(app.element("ask.answerCard").exists)
        app.buttons["ask.chip.remind.rohan"].tap()
        XCTAssertTrue(app.element("screen.remind").waitForExistence(timeout: 3))
    }

    /// A typed expense is drafted, saved in place, and View opens it.
    @MainActor
    func testDraftedExpenseSavesAndOpens() {
        let app = XCUIApplication.launchPaybak(startScreen: .askStart)
        let field = app.textFields["ask.field"]
        XCTAssertTrue(field.waitForExistence(timeout: 5))
        field.tap()
        field.typeText("Add ₹600 for a cab, split with Esha and Dev")
        app.buttons["ask.send"].tap()
        let save = app.buttons["ask.draft.save"]
        XCTAssertTrue(save.waitForExistence(timeout: 3))
        XCTAssertTrue(app.staticTexts["₹200 each"].exists)
        save.tap()
        XCTAssertTrue(app.element("ask.draft.saved").waitForExistence(timeout: 3))
        XCTAssertTrue(app.staticTexts["Expense added"].exists)
        app.buttons["ask.draft.view"].tap()
        XCTAssertTrue(app.element("screen.expense").waitForExistence(timeout: 3))
        XCTAssertFalse(app.element("screen.ask").exists)
    }

    /// Anything else gets the fallback and the suggestions again.
    @MainActor
    func testFallbackOffersTheSuggestions() {
        let app = XCUIApplication.launchPaybak(startScreen: .askStart)
        let field = app.textFields["ask.field"]
        XCTAssertTrue(field.waitForExistence(timeout: 5))
        field.tap()
        field.typeText("What’s the weather?\n")
        XCTAssertTrue(app.staticTexts["I can answer questions about your balances and due dates, or add an expense. Try “Who owes me money?”"]
            .waitForExistence(timeout: 3))
        XCTAssertTrue(app.buttons["ask.prompt.2"].exists)
    }
}
