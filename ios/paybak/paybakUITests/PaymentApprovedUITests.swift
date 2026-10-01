import XCTest

/// The payer's payment-approved scene: when a friend confirms a payment you made,
/// `paybak-payment.riv` takes over the screen, whatever is open, until it ends or you tap it.
final class PaymentApprovedUITests: XCTestCase {
    override func setUpWithError() throws {
        continueAfterFailure = false
    }

    @MainActor
    func testFriendConfirmingYourPaymentPlaysTheScene() {
        let app = XCUIApplication.launchPaybak(startScreen: .debugMenu, scenarios: ["paymentToMeeraPending"])
        XCTAssertTrue(app.element("screen.debugMenu").waitForExistence(timeout: 6))
        app.tapDebugRow("Meera confirms my latest pending payment")

        let scene = app.element("paymentApproved")
        XCTAssertTrue(scene.waitForExistence(timeout: 5))
        XCTAssertEqual(scene.label, "Meera confirmed ₹450")
        scene.tap()
        XCTAssertTrue(scene.waitForNonExistence(timeout: 3))

        // Back on Home, and the payment is no longer pending.
        let logo = app.element("home.logo")
        XCTAssertTrue(logo.isHittable)
        logo.press(forDuration: 1)
        XCTAssertTrue(app.element("screen.debugMenu").waitForExistence(timeout: 3))
        XCTAssertFalse(app.buttons["debugMenu.Meera confirms my latest pending payment"].exists)
    }

    @MainActor
    func testAutoApprovedPaymentCoversAnOpenAlert() {
        let app = XCUIApplication.launchPaybak(startScreen: .homeActive, autoApprove: true)
        XCTAssertTrue(app.screen(.homeActive).waitForExistence(timeout: 5))
        app.buttons["home.tab.add"].tap()
        app.buttons["home.addSheet.payment"].tap()
        XCTAssertTrue(app.element("screen.recordPayment").waitForExistence(timeout: 3))
        app.buttons["recordPayment.action"].tap()
        XCTAssertTrue(app.element("screen.payment").waitForExistence(timeout: 3))
        XCTAssertTrue(app.element("paymentRecorded.status").label.contains("Pending confirmation"))
        app.buttons["paymentRecorded.cancel"].tap()
        let alertAction = app.buttons["paymentRecorded.alert.action"]
        XCTAssertTrue(alertAction.waitForExistence(timeout: 3))

        // Meera confirms 5 s after the save, over the alert.
        let scene = app.element("paymentApproved")
        XCTAssertTrue(scene.waitForExistence(timeout: 10))
        XCTAssertEqual(scene.label, "Meera confirmed ₹450")
        scene.tap()
        XCTAssertTrue(scene.waitForNonExistence(timeout: 3))
        XCTAssertTrue(alertAction.exists)
    }
}
