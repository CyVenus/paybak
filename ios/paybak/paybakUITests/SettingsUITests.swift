import XCTest

/// Settings and Pro (screens-settings, app-architecture §6.4 "M8 UI tests").
final class SettingsUITests: XCTestCase {
    override func setUp() {
        continueAfterFailure = false
    }

    /// Free plan: Export records opens the RevenueCat paywall (the purchase itself isn't automated).
    @MainActor
    func testExportOnTheFreePlanOpensThePaywall() {
        let app = XCUIApplication.launchPaybak(startScreen: .privacyData, pro: false)
        XCTAssertTrue(app.element("screen.privacyData").waitForExistence(timeout: 5))
        app.buttons["privacyData.export"].tap()
        XCTAssertTrue(app.element("screen.paywall").waitForExistence(timeout: 3))
        XCTAssertFalse(app.element("screen.privacyExport").exists)
    }

    /// Pro: Export records opens Export, last month and the busiest group preselected.
    @MainActor
    func testExportWithProOpensExport() {
        let app = XCUIApplication.launchPaybak(startScreen: .privacyData, pro: true)
        XCTAssertTrue(app.element("screen.privacyData").waitForExistence(timeout: 5))
        app.buttons["privacyData.export"].tap()
        XCTAssertTrue(app.element("screen.privacyExport").waitForExistence(timeout: 3))
        XCTAssertEqual(app.element("privacyExport.rangeLabel").label, "1 Sep – 30 Sep 2026")
        XCTAssertTrue(app.buttons["privacyExport.group.g-goa"].isSelected)
        XCTAssertFalse(app.buttons["privacyExport.group.g-college"].isSelected)
    }

    @MainActor
    func testExportRangesAndSelectAll() {
        let app = XCUIApplication.launchPaybak(startScreen: .privacyExport)
        XCTAssertTrue(app.element("screen.privacyExport").waitForExistence(timeout: 5))
        app.buttons["privacyExport.range.last3Months"].tap()
        XCTAssertEqual(app.element("privacyExport.rangeLabel").label, "1 Jul – 30 Sep 2026")
        app.buttons["privacyExport.selectAll"].tap()
        XCTAssertTrue(app.buttons["privacyExport.group.g-dubai"].isSelected)
        app.buttons["privacyExport.export"].tap()
        let shareSheet = app.otherElements["ActivityListView"]
        XCTAssertTrue(shareSheet.waitForExistence(timeout: 5), "Export opens the share sheet")
    }

    /// A UPI ID without "@" keeps the sheet open with the error; a valid one adds the row.
    @MainActor
    func testAddUPIErrorThenValid() {
        let app = XCUIApplication.launchPaybak(startScreen: .paymentDetails)
        XCTAssertTrue(app.element("screen.paymentDetails").waitForExistence(timeout: 5))
        app.buttons["paymentDetails.add"].tap()
        let field = app.textFields["paymentAddUpi.field"]
        XCTAssertTrue(field.waitForExistence(timeout: 3))
        XCTAssertEqual(field.value as? String, "arjun@okhdfcbank")

        field.typeText(String(repeating: XCUIKeyboardKey.delete.rawValue, count: 20) + "arjunokhdfcbank")
        app.buttons["paymentAddUpi.save"].tap()
        XCTAssertTrue(field.waitForExistence(timeout: 2), "The sheet stays up with the error")
        XCTAssertFalse(app.buttons["paymentDetails.method.2"].exists)

        field.typeText(String(repeating: XCUIKeyboardKey.delete.rawValue, count: 20) + "arjun@okhdfcbank")
        app.buttons["paymentAddUpi.save"].tap()
        XCTAssertTrue(field.waitForNonExistence(timeout: 3))
        XCTAssertTrue(app.buttons["paymentDetails.method.2"].waitForExistence(timeout: 3))
        XCTAssertTrue(app.buttons["paymentDetails.method.2"].label.contains("arjun@okhdfcbank"))
    }

    @MainActor
    func testDeleteAccountBlockedGoesToSettleUp() {
        let app = XCUIApplication.launchPaybak(startScreen: .privacyData)
        XCTAssertTrue(app.element("screen.privacyData").waitForExistence(timeout: 5))
        app.buttons["privacyData.deleteAccount"].tap()
        XCTAssertTrue(app.staticTexts["You still owe ₹1,850 and are owed ₹2,900. Settle every balance before deleting your account."]
            .waitForExistence(timeout: 2))
        app.buttons["privacyDeleteBlocked.action"].tap()
        XCTAssertTrue(app.element("screen.settleUp").waitForExistence(timeout: 3))
    }

    @MainActor
    func testNotificationSettingsPersistAcrossRelaunch() {
        var app = XCUIApplication.launchPaybak(startScreen: .settingsNotifications)
        XCTAssertTrue(app.element("screen.settingsNotifications").waitForExistence(timeout: 5))
        app.switches["settingsNotifications.push.reminders"].tap()
        app.buttons["settingsNotifications.schedule.onDueDate"].tap()
        XCTAssertEqual(app.switches["settingsNotifications.push.reminders"].value as? String, "0")
        XCTAssertFalse(app.buttons["settingsNotifications.schedule.onDueDate"].isSelected)
        app.terminate()

        app = XCUIApplication.launchPaybak(resetOnboarding: false)
        XCTAssertTrue(app.buttons["home.tab.profile"].waitForExistence(timeout: 5))
        app.buttons["home.tab.profile"].tap()
        app.buttons["profile.row.notifications"].tap()
        XCTAssertTrue(app.element("screen.settingsNotifications").waitForExistence(timeout: 3))
        XCTAssertEqual(app.switches["settingsNotifications.push.reminders"].value as? String, "0")
        XCTAssertTrue(app.switches["settingsNotifications.push.overdueAlerts"].value as? String == "1")
        XCTAssertFalse(app.buttons["settingsNotifications.schedule.onDueDate"].isSelected)
        XCTAssertTrue(app.buttons["settingsNotifications.schedule.twoDaysBefore"].isSelected)
    }

    @MainActor
    func testHelpAnswers() {
        let app = XCUIApplication.launchPaybak(startScreen: .helpFeedback)
        XCTAssertTrue(app.element("screen.helpFeedback").waitForExistence(timeout: 5))
        XCTAssertEqual(app.element("helpFeedback.version").label, "Paybak 1.0 (1)")
        app.buttons["helpFeedback.faq.0"].tap()
        XCTAssertTrue(app.element("helpAnswer.body").waitForExistence(timeout: 3))
        XCTAssertTrue(app.element("helpAnswer.body").label.hasPrefix("No. Paybak only records who paid"))
    }
}
