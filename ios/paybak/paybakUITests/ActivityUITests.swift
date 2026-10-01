import XCTest

/// Activity's flows (app-architecture §6.2, M6): the timeline and its claim card, deleting and
/// restoring, comments, resolving a flag, the inbox and notification links.
final class ActivityUITests: XCTestCase {
    override func setUp() {
        continueAfterFailure = false
    }

    @MainActor
    func testTimelineIsNewestFirstWithTheClaimOnTop() {
        let app = XCUIApplication.launchPaybak(startScreen: .activityTimeline)
        let confirm = app.buttons["activity.claim.pay-esha-olive.confirm"]
        XCTAssertTrue(confirm.waitForExistence(timeout: 5))
        let reminder = app.element("activity.row.reminder:rem-e-movie-20260930")
        let dinner = app.element("activity.row.expense:e-olive")
        let yesterday = app.element("activity.day.2026-09-29")
        XCTAssertTrue(app.element("activity.day.2026-09-30").exists)
        XCTAssertLessThan(confirm.frame.minY, reminder.frame.minY)
        XCTAssertLessThan(reminder.frame.minY, dinner.frame.minY)
        XCTAssertLessThan(dinner.frame.minY, yesterday.frame.minY)
        XCTAssertTrue(dinner.label.contains("₹2,800"))
        // Rows open what they're about.
        app.descendants(matching: .any)
            .matching(NSPredicate(format: "identifier BEGINSWITH %@", "activity.row.edit:e-goa-villa")).firstMatch.tap()
        XCTAssertTrue(app.element("screen.expense").waitForExistence(timeout: 3))
        XCTAssertTrue(app.element(label: "Villa (3 nights)").exists)
    }

    @MainActor
    func testConfirmingFromActivityUpdatesHome() {
        let app = XCUIApplication.launchPaybak(startScreen: .activityTimeline)
        let confirm = app.buttons["activity.claim.pay-esha-olive.confirm"]
        XCTAssertTrue(confirm.waitForExistence(timeout: 5))
        confirm.tap()
        // The toast follows the card's Confirmed state and its 1.05 s hold.
        XCTAssertTrue(app.element("toast").waitForExistence(timeout: 4))
        XCTAssertEqual(app.element("toast").label, "Payment confirmed")
        XCTAssertTrue(app.element("activity.row.payment:pay-esha-olive").waitForExistence(timeout: 4))
        XCTAssertFalse(confirm.exists)
        app.buttons["home.tab.home"].tap()
        XCTAssertTrue(app.element("home.balance.owed").waitForExistence(timeout: 3))
        XCTAssertFalse(app.buttons["home.confirmCard.confirm"].exists)
        XCTAssertTrue(app.element("home.balance.owed").label.contains("+₹2,200"))
    }

    @MainActor
    func testDeleteThenRestoreFromRecentlyDeleted() {
        let app = XCUIApplication.launchPaybak(startScreen: .expenseVilla)
        let delete = app.buttons["expense.delete"]
        XCTAssertTrue(app.element("screen.expense").waitForExistence(timeout: 5))
        app.swipeUp()
        app.swipeUp()
        delete.tap()
        XCTAssertTrue(app.buttons["expense.alert.action"].waitForExistence(timeout: 3))
        app.buttons["expense.alert.action"].tap()
        XCTAssertTrue(app.element("toast").waitForExistence(timeout: 3))
        XCTAssertEqual(app.element("toast").label, "Expense deleted")

        app.buttons["activity.recentlyDeleted"].tap()
        let row = app.element("recentlyDeleted.row.e-goa-villa")
        XCTAssertTrue(row.waitForExistence(timeout: 3))
        // "30 days left" is held together with no-break spaces, as on Android.
        XCTAssertTrue(row.staticTexts.matching(NSPredicate(format: "label CONTAINS %@", "30\u{00A0}days\u{00A0}left")).firstMatch.exists)
        row.buttons["Restore"].tap()
        XCTAssertTrue(app.element(label: "Expense restored").waitForExistence(timeout: 3))
        XCTAssertFalse(row.exists)

        // It counts in Goa Trip again.
        app.buttons["recentlyDeleted.back"].tap()
        app.buttons["home.tab.groups"].tap()
        XCTAssertTrue(app.element("groups.row.g-goa").waitForExistence(timeout: 3))
        app.element("groups.row.g-goa").tap()
        XCTAssertTrue(app.element("group.expense.e-goa-villa").waitForExistence(timeout: 3))
    }

    @MainActor
    func testSendingACommentPostsItAndClearsTheComposer() {
        let app = XCUIApplication.launchPaybak(startScreen: .expenseVilla)
        let field = app.element("expense.composer.field")
        XCTAssertTrue(field.waitForExistence(timeout: 5))
        app.swipeUp()
        field.tap()
        app.typeText("See you at the villa")
        app.buttons["expense.composer.send"].tap()
        XCTAssertTrue(app.element(label: "See you at the villa").waitForExistence(timeout: 3))
        XCTAssertFalse(app.buttons["expense.composer.send"].exists)
        XCTAssertEqual(app.element("expense.composer.field").label, "Add a comment")
    }

    @MainActor
    func testResolvingAFlagClearsTheDispute() {
        let app = XCUIApplication.launchPaybak(startScreen: .expenseDisputed)
        XCTAssertTrue(app.element("expense.flagNotice").waitForExistence(timeout: 5))
        XCTAssertTrue(app.staticTexts["Disputed"].exists)
        app.buttons["Resolve"].tap()
        XCTAssertFalse(app.element("expense.flagNotice").waitForExistence(timeout: 1))
        XCTAssertFalse(app.staticTexts["Disputed"].exists)
        app.swipeUp()
        app.swipeUp()
        XCTAssertTrue(app.element(label: "You resolved Esha’s flag").waitForExistence(timeout: 3))
    }

    @MainActor
    func testMarkAllReadClearsTheBellDot() {
        let app = XCUIApplication.launchPaybak(startScreen: .homeActive)
        let bell = app.buttons["home.bell"]
        XCTAssertTrue(bell.waitForExistence(timeout: 5))
        XCTAssertEqual(bell.value as? String, "Unread")
        bell.tap()
        XCTAssertTrue(app.element("screen.notifications").waitForExistence(timeout: 3))
        let markAllRead = app.buttons["notifications.action"]
        XCTAssertTrue(markAllRead.isEnabled)
        markAllRead.tap()
        XCTAssertFalse(markAllRead.isEnabled, "Nothing left to mark")
        app.buttons["notifications.back"].tap()
        XCTAssertTrue(bell.waitForExistence(timeout: 3))
        XCTAssertNotEqual(bell.value as? String, "Unread")
    }

    @MainActor
    func testInboxRowsOpenTheirFlows() {
        let app = XCUIApplication.launchPaybak(startScreen: .notifications)
        let reminder = app.element("notifications.row.n-reminder-g-goa-20260930")
        XCTAssertTrue(reminder.waitForExistence(timeout: 5))
        reminder.tap()
        XCTAssertTrue(app.element("screen.recordPayment").waitForExistence(timeout: 3))
    }

    @MainActor
    func testNotificationLinksOpenTheirScreens() {
        var app = XCUIApplication.launchPaybak(startScreen: .homeConfirmPayment, link: "paybak://activity?claim=pay-esha-olive")
        XCTAssertTrue(app.buttons["activity.claim.pay-esha-olive.confirm"].waitForExistence(timeout: 5))
        app.terminate()

        app = XCUIApplication.launchPaybak(startScreen: .homeConfirmPayment,
                                           link: "paybak://activity?claim=pay-esha-olive&action=notReceived")
        XCTAssertTrue(app.element("screen.notReceived").waitForExistence(timeout: 5))
        app.terminate()

        app = XCUIApplication.launchPaybak(startScreen: .homeActive, link: "paybak://record-payment?to=p-kabir&amount=140000&context=group:g-goa")
        XCTAssertTrue(app.element("screen.recordPayment").waitForExistence(timeout: 5))
    }
}
