import XCTest

/// Welcome 1–3 (screens-launch.md §2): one screen whose step changes with Continue and horizontal
/// swipes; "Get started" and Skip lead to Get Started, and Back from there returns to the same step.
final class WelcomeUITests: XCTestCase {
    /// The headlines as displayed, with the line break where Figma wraps them.
    private let headlines = [
        "Split any bill in\nseconds.",
        "Know who owes what,\nand by when.",
        "Settle up without the\nawkward chat.",
    ]

    override func setUpWithError() throws {
        continueAfterFailure = false
    }

    @MainActor
    func testContinueWalksThroughTheStepsToGetStarted() {
        let app = XCUIApplication.launchPaybak(startScreen: .welcome1)
        let headline = app.element("welcome.headline")
        let cta = app.element("welcome.continue")

        XCTAssertTrue(headline.waitForLabel(headlines[0]))
        XCTAssertEqual(cta.label, "Continue")
        cta.tap()
        XCTAssertTrue(app.screen(.welcome2).waitForExistence(timeout: 2))
        XCTAssertTrue(headline.waitForLabel(headlines[1]))
        cta.tap()
        XCTAssertTrue(app.screen(.welcome3).waitForExistence(timeout: 2))
        XCTAssertTrue(headline.waitForLabel(headlines[2]))
        XCTAssertTrue(cta.waitForLabel("Get started"))
        XCTAssertFalse(app.element("welcome.skip").exists)

        cta.tap()
        XCTAssertTrue(app.screen(.getStarted).waitForExistence(timeout: 3))
    }

    @MainActor
    func testSkipGoesToGetStarted() {
        let app = XCUIApplication.launchPaybak(startScreen: .welcome1)

        app.element("welcome.skip").tap()

        XCTAssertTrue(app.screen(.getStarted).waitForExistence(timeout: 3))
    }

    @MainActor
    func testBackFromGetStartedReturnsToTheStepLeft() {
        let app = XCUIApplication.launchPaybak(startScreen: .welcome2)

        app.element("welcome.skip").tap()
        XCTAssertTrue(app.screen(.getStarted).waitForExistence(timeout: 3))
        app.swipeBackFromLeftEdge()
        XCTAssertTrue(app.screen(.welcome2).waitForExistence(timeout: 3))
        XCTAssertTrue(app.element("welcome.headline").waitForLabel(headlines[1]))

        app.element("welcome.continue").tap()
        XCTAssertTrue(app.screen(.welcome3).waitForExistence(timeout: 2))
        app.element("welcome.continue").tap()
        XCTAssertTrue(app.screen(.getStarted).waitForExistence(timeout: 3))
        app.swipeBackFromLeftEdge()
        XCTAssertTrue(app.screen(.welcome3).waitForExistence(timeout: 3))
    }

    @MainActor
    func testSwipesChangeTheStepBothWays() {
        let app = XCUIApplication.launchPaybak(startScreen: .welcome1)

        app.screen(.welcome1).swipeLeft()
        XCTAssertTrue(app.screen(.welcome2).waitForExistence(timeout: 2))
        app.screen(.welcome2).swipeLeft()
        XCTAssertTrue(app.screen(.welcome3).waitForExistence(timeout: 2))

        // Only "Get started" leaves the last step.
        app.screen(.welcome3).swipeLeft()
        XCTAssertTrue(app.screen(.welcome3).waitForExistence(timeout: 2))
        XCTAssertFalse(app.screen(.getStarted).exists)

        app.screen(.welcome3).swipeRight()
        XCTAssertTrue(app.screen(.welcome2).waitForExistence(timeout: 2))
        XCTAssertTrue(app.element("welcome.headline").waitForLabel(headlines[1]))
    }
}
