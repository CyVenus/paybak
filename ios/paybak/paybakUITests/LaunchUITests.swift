import XCTest

/// Splash (screens-launch.md §1): a fresh launch shows the splash, which dissolves to Welcome step 1
/// after about 1.5 s.
final class LaunchUITests: XCTestCase {
    override func setUpWithError() throws {
        continueAfterFailure = false
    }

    @MainActor
    func testFreshLaunchShowsSplashThenWelcomeStepOne() {
        let app = XCUIApplication.launchPaybak()

        // launch() returns about 1.2 s in, once the entrance has settled. Check at once:
        // waitForExistence polls only after a second, when the splash is already dissolving.
        XCTAssertTrue(app.screen(.splash).exists)
        XCTAssertTrue(app.screen(.welcome1).waitForExistence(timeout: 5))
        XCTAssertFalse(app.screen(.splash).exists)
    }
}
