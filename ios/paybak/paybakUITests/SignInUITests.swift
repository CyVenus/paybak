import XCTest

/// Sign in and Verify (screens-signin.md): Send code is enabled only for a plausible email or phone
/// number, 000000 verifies, anything else shows the wrong-code state until a digit is edited, and
/// "Change" goes back to Sign in with the contact kept.
final class SignInUITests: XCTestCase {
    override func setUpWithError() throws {
        continueAfterFailure = false
    }

    @MainActor
    func testSendCodeNeedsAnEmailOrPhoneNumber() {
        let app = XCUIApplication.launchPaybak(startScreen: .getStarted)
        app.element("getStarted.email").tap()
        let field = app.textFields["signIn.field"]
        let sendCode = app.buttons["signIn.sendCode"]

        XCTAssertTrue(field.waitForExistence(timeout: 3))
        XCTAssertFalse(sendCode.isEnabled)
        field.typeText("arjun@example")
        XCTAssertFalse(sendCode.isEnabled)
        field.typeText(".com")
        XCTAssertTrue(sendCode.isEnabled)

        field.clearText()
        field.typeText("98765 43210")
        XCTAssertTrue(sendCode.isEnabled)
        sendCode.tap()

        XCTAssertTrue(app.screen(.verify).waitForExistence(timeout: 3))
        let sentTo = app.staticTexts.matching(NSPredicate(format: "label BEGINSWITH %@", "Sent to +91 98765 43210")).firstMatch
        XCTAssertTrue(sentTo.exists)
    }

    @MainActor
    func testWrongCodeShowsTheErrorUntilADigitIsEdited() {
        let app = XCUIApplication.launchPaybak(startScreen: .signIn)
        XCTAssertTrue(app.buttons["signIn.sendCode"].waitForExistence(timeout: 3))
        app.buttons["signIn.sendCode"].tap()
        XCTAssertTrue(app.screen(.verify).waitForExistence(timeout: 3))

        app.typeText("482917")

        XCTAssertTrue(app.screen(.verifyWrong).waitForExistence(timeout: 3))
        XCTAssertEqual(app.element("verify.error").label, "That code didn’t match. Check it and try again.")
        XCTAssertEqual(app.element("verify.code").value as? String, "4 8 2 9 1 7")

        app.typeText(XCUIKeyboardKey.delete.rawValue)

        XCTAssertTrue(app.screen(.verify).waitForExistence(timeout: 3))
        XCTAssertFalse(app.element("verify.error").exists)
        XCTAssertEqual(app.element("verify.code").value as? String, "4 8 2 9 1")

        app.typeText("0")
        XCTAssertTrue(app.screen(.verifyWrong).waitForExistence(timeout: 3))

        // Typing after an error starts over from the first box.
        app.typeText("0")
        XCTAssertTrue(app.screen(.verify).waitForExistence(timeout: 3))
        XCTAssertEqual(app.element("verify.code").value as? String, "0")
        app.typeText("00000")
        XCTAssertTrue(app.screen(.setup1).waitForExistence(timeout: 3))
    }

    @MainActor
    func testResendClearsTheCodeAndRestartsTheTimer() {
        let app = XCUIApplication.launchPaybak(startScreen: .verifyWrong)

        XCTAssertTrue(app.element("verify.error").waitForExistence(timeout: 3))
        app.buttons["verify.resend"].tap()

        XCTAssertTrue(app.screen(.verify).waitForExistence(timeout: 3))
        XCTAssertEqual(app.element("verify.code").value as? String, "Empty")
        XCTAssertTrue(app.element("verify.countdown").label.hasPrefix("Resend code in 0:"))
        XCTAssertFalse(app.buttons["verify.resend"].exists)
    }

    @MainActor
    func testChangeReturnsToSignInWithTheContact() {
        let app = XCUIApplication.launchPaybak(startScreen: .verify)

        XCTAssertTrue(app.buttons["verify.change"].waitForExistence(timeout: 3))
        app.buttons["verify.change"].tap()

        XCTAssertTrue(app.screen(.signIn).waitForExistence(timeout: 3))
        XCTAssertEqual(app.textFields["signIn.field"].value as? String, "arjun@example.com")
    }
}

extension XCUIElement {
    /// Deletes the text in a focused text field.
    func clearText() {
        guard let text = value as? String, !text.isEmpty else { return }
        typeText(String(repeating: XCUIKeyboardKey.delete.rawValue, count: text.count))
    }
}
