import XCTest

/// The whole first run, end to end: Welcome → Get Started → Sign in → Verify → Setup 1–4 → All set
/// → Home, and the Apple shortcut that skips the code.
final class OnboardingFlowUITests: XCTestCase {
    override func setUpWithError() throws {
        continueAfterFailure = false
    }

    @MainActor
    func testEmailSignInThroughSetupToHome() {
        let app = XCUIApplication.launchPaybak()

        XCTAssertTrue(app.screen(.welcome1).waitForExistence(timeout: 5))
        app.element("welcome.skip").tap()
        XCTAssertTrue(app.screen(.getStarted).waitForExistence(timeout: 3))
        app.element("getStarted.email").tap()

        // Sign in
        let field = app.textFields["signIn.field"]
        XCTAssertTrue(field.waitForExistence(timeout: 3))
        field.typeText("arjun@example.com")
        app.buttons["signIn.sendCode"].tap()

        // Verify: a wrong code first, then 000000 (typing after the error starts over)
        XCTAssertTrue(app.screen(.verify).waitForExistence(timeout: 3))
        app.typeText("123456")
        XCTAssertTrue(app.screen(.verifyWrong).waitForExistence(timeout: 3))
        XCTAssertTrue(app.element("verify.error").exists)
        app.typeText("000000")

        // Setup 1: name and avatar
        XCTAssertTrue(app.screen(.setup1).waitForExistence(timeout: 3))
        XCTAssertEqual(app.element("setup.progress").label, "Step 1 of 4")
        let continueName = app.buttons["setup1.continue"]
        XCTAssertFalse(continueName.isEnabled)
        // VoiceOver numbers the presets rather than naming them after the sample people.
        XCTAssertEqual(app.buttons["setup1.avatar.0"].label, "Avatar 1")
        XCTAssertEqual(app.buttons["setup1.avatar.4"].label, "Avatar 5")
        XCTAssertEqual(app.buttons["setup1.camera"].label, "Choose a photo")
        app.buttons["setup1.avatar.0"].tap()
        XCTAssertTrue(app.buttons["setup1.avatar.0"].isSelected)
        let name = app.textFields["setup1.name"]
        name.tap()
        name.typeText("Arjun Mehta")
        XCTAssertTrue(continueName.isEnabled)
        continueName.tap()

        // Setup 2: currency, found by search
        XCTAssertTrue(app.screen(.setup2).waitForExistence(timeout: 3))
        let search = app.textFields["setup2.search"]
        search.tap()
        search.typeText("rupee")
        let rupee = app.buttons["setup2.row.INR"]
        XCTAssertTrue(rupee.waitForExistence(timeout: 2))
        rupee.tap()
        XCTAssertTrue(rupee.isSelected)
        app.buttons["setup2.continue"].tap()

        // Setup 3: UPI ID, previewed and copied
        XCTAssertTrue(app.screen(.setup3).waitForExistence(timeout: 3))
        XCTAssertFalse(app.buttons["setup3.copy"].exists)
        let upi = app.textFields["setup3.upi"]
        upi.tap()
        upi.typeText("arjun@okaxis")
        app.buttons["setup3.copy"].tap()
        XCTAssertTrue(app.element(label: "UPI ID copied").waitForExistence(timeout: 2))
        // Copying ends editing, so Continue is no longer under the keyboard.
        app.buttons["setup3.continue"].tap()

        // Setup 4: the OS prompt, whatever the answer
        XCTAssertTrue(app.screen(.setup4).waitForExistence(timeout: 3))
        app.buttons["setup4.enable"].tap()
        allowNotificationsIfAsked()

        // All set → Home
        XCTAssertTrue(app.screen(.allSet).waitForExistence(timeout: 5))
        XCTAssertEqual(app.element("allSet.headline").label, "You’re all set, Arjun.")
        app.buttons["allSet.goHome"].tap()
        XCTAssertTrue(app.screen(.homeFirstDay).waitForExistence(timeout: 3))

        // Onboarding is saved: a relaunch goes from the splash to Home.
        app.terminate()
        let relaunched = XCUIApplication.launchPaybak(resetOnboarding: false)
        XCTAssertTrue(relaunched.screen(.homeFirstDay).waitForExistence(timeout: 5))
    }

    @MainActor
    func testAppleSkipsTheCodeAndBackReturnsToGetStarted() {
        let app = XCUIApplication.launchPaybak(startScreen: .getStarted)

        app.element("getStarted.apple").tap()

        XCTAssertTrue(app.screen(.setup1).waitForExistence(timeout: 3))
        app.buttons["setup.back"].tap()
        XCTAssertTrue(app.screen(.getStarted).waitForExistence(timeout: 3))

        app.element("getStarted.apple").tap()
        let name = app.textFields["setup1.name"]
        XCTAssertTrue(name.waitForExistence(timeout: 3))
        name.tap()
        name.typeText("Priya")
        app.buttons["setup1.continue"].tap()
        XCTAssertTrue(app.screen(.setup2).waitForExistence(timeout: 3))
        app.buttons["setup2.continue"].tap()
        XCTAssertTrue(app.screen(.setup3).waitForExistence(timeout: 3))
        app.buttons["setup.skip"].tap()
        XCTAssertTrue(app.screen(.setup4).waitForExistence(timeout: 3))
        app.buttons["setup4.notNow"].tap()

        XCTAssertTrue(app.screen(.allSet).waitForExistence(timeout: 3))
        XCTAssertEqual(app.element("allSet.headline").label, "You’re all set, Priya.")
        // No way back into setup from All set.
        app.swipeBackFromLeftEdge()
        XCTAssertTrue(app.screen(.allSet).exists)
    }

    @MainActor
    func testContinueNeedsAValidUPIIDOrNone() {
        let app = XCUIApplication.launchPaybak(startScreen: .setup3)
        let upi = app.textFields["setup3.upi"]
        XCTAssertTrue(upi.waitForExistence(timeout: 3))
        XCTAssertEqual(upi.value as? String, "arjun@okaxis")

        upi.tap()
        upi.typeText(" x\n")
        app.buttons["setup3.continue"].tap()
        XCTAssertFalse(app.screen(.setup4).waitForExistence(timeout: 1))
        XCTAssertTrue(app.screen(.setup3).exists)

        upi.tap()
        upi.clearText()
        XCTAssertFalse(app.buttons["setup3.copy"].exists)
        upi.typeText("\n")
        app.buttons["setup3.continue"].tap()
        XCTAssertTrue(app.screen(.setup4).waitForExistence(timeout: 3))
    }

    @MainActor
    func testContinueIsReachableAfterComingBackToTheNameStep() {
        let app = XCUIApplication.launchPaybak(startScreen: .setup1)
        let continueName = app.buttons["setup1.continue"]
        XCTAssertTrue(continueName.waitForExistence(timeout: 3))
        continueName.tap()
        XCTAssertTrue(app.screen(.setup2).waitForExistence(timeout: 3))
        app.buttons["setup.back"].tap()
        XCTAssertTrue(app.screen(.setup1).waitForExistence(timeout: 3))

        // Refocusing the name while Back revealed the screen left Continue under the keyboard.
        XCTAssertTrue(continueName.isHittable)
        app.textFields["setup1.name"].tap()
        continueName.tap()
        XCTAssertTrue(app.screen(.setup2).waitForExistence(timeout: 3))
    }

    @MainActor
    func testNameStepScrollsInsteadOfCuttingItsCopyWithLargeText() {
        let app = XCUIApplication.launchPaybak(startScreen: .setup1, textSize: .extraExtraExtraLarge)
        let name = app.textFields["setup1.name"]
        XCTAssertTrue(name.waitForExistence(timeout: 3))
        let body = app.staticTexts.matching(NSPredicate(format: "label BEGINSWITH %@", "Friends see your name")).firstMatch

        // Squeezed between the header and the keyboard, the copy was cut to one line
        // ("…and picture o…"); it needs at least two lines at this size.
        XCTAssertGreaterThanOrEqual(body.frame.height, 2 * 24)
        XCTAssertLessThanOrEqual(name.frame.maxY, app.buttons["setup1.continue"].frame.minY)
    }

    /// Taps Allow on the system notification prompt when it appears (it doesn't once answered).
    private func allowNotificationsIfAsked() {
        let springboard = XCUIApplication(bundleIdentifier: "com.apple.springboard")
        let allow = springboard.buttons["Allow"]
        if allow.waitForExistence(timeout: 3) {
            allow.tap()
        }
    }
}
