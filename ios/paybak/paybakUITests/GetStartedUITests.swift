import XCTest

/// Get Started (screens-launch.md §3): Apple and Google go straight to Setup 1, email or phone to
/// Sign in.
final class GetStartedUITests: XCTestCase {
    override func setUpWithError() throws {
        continueAfterFailure = false
    }

    @MainActor
    func testShowsTheHeadline() {
        let app = XCUIApplication.launchPaybak(startScreen: .getStarted)

        XCTAssertTrue(app.element("getStarted.headline").waitForLabel("Shared money,\nkept clear."))
    }

    @MainActor
    func testAppleGoesToSetupOne() {
        let app = XCUIApplication.launchPaybak(startScreen: .getStarted)

        app.element("getStarted.apple").tap()

        XCTAssertTrue(app.screen(.setup1).waitForExistence(timeout: 3))
    }

    @MainActor
    func testGoogleGoesToSetupOne() {
        let app = XCUIApplication.launchPaybak(startScreen: .getStarted)

        app.element("getStarted.google").tap()

        XCTAssertTrue(app.screen(.setup1).waitForExistence(timeout: 3))
    }

    @MainActor
    func testEmailOrPhoneGoesToSignIn() {
        let app = XCUIApplication.launchPaybak(startScreen: .getStarted)

        app.element("getStarted.email").tap()

        XCTAssertTrue(app.screen(.signIn).waitForExistence(timeout: 3))
    }
}
