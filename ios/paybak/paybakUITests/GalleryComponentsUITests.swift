import XCTest

/// The interactive shared components (components-app.md), driven through the debug gallery pages:
/// the sheet opens, picks and closes; the alert presents and cancels; Confirm collapses the card.
final class GalleryComponentsUITests: XCTestCase {
    /// "Headers, alerts, sheets, settings".
    private static let navigationPage = 8
    /// "Lists and detail".
    private static let listsPage = 10

    override func setUpWithError() throws {
        continueAfterFailure = false
    }

    @MainActor
    func testSheetPicksACategoryAndCloses() {
        let app = XCUIApplication.launchGallery(page: Self.navigationPage)
        let open = app.buttons["Open Medium"]
        XCTAssertTrue(open.waitForExistence(timeout: 5))
        app.scrollUntilHittable(open)
        open.tap()

        let travel = app.buttons["gallery.sheet.Travel"]
        XCTAssertTrue(travel.waitForExistence(timeout: 3))
        XCTAssertTrue(app.buttons["gallery.sheet.Food"].isSelected)
        XCTAssertFalse(travel.isSelected)
        // Picking a row closes the sheet.
        travel.tap()
        XCTAssertTrue(travel.waitForNonExistence(timeout: 3))

        open.tap()
        XCTAssertTrue(travel.waitForExistence(timeout: 3))
        XCTAssertTrue(travel.isSelected)
        XCTAssertFalse(app.buttons["gallery.sheet.Food"].isSelected)
        app.buttons["gallery.sheet.close"].tap()
        XCTAssertTrue(travel.waitForNonExistence(timeout: 3))
    }

    @MainActor
    func testAlertPresentsOverTheScreenAndCancels() {
        let app = XCUIApplication.launchGallery(page: Self.navigationPage)
        let show = app.buttons["Show Destructive"]
        XCTAssertTrue(show.waitForExistence(timeout: 5))
        let cancel = app.buttons["gallery.alert.cancel"]
        XCTAssertFalse(cancel.exists)

        app.scrollUntilHittable(show)
        show.tap()
        XCTAssertTrue(cancel.waitForExistence(timeout: 3))
        XCTAssertTrue(app.buttons["gallery.alert.action"].exists)

        cancel.tap()
        XCTAssertTrue(cancel.waitForNonExistence(timeout: 3))
    }

    @MainActor
    func testConfirmCollapsesThePaymentCard() {
        let app = XCUIApplication.launchGallery(page: Self.listsPage)
        let confirm = app.buttons["gallery.confirm.confirm"]
        XCTAssertTrue(confirm.waitForExistence(timeout: 5))
        let confirmed = app.descendants(matching: .any).matching(NSPredicate(format: "label BEGINSWITH %@", "Esha paid you ₹700"))
        let confirmedBefore = confirmed.count

        app.scrollUntilHittable(confirm)
        confirm.tap()

        XCTAssertTrue(confirm.waitForNonExistence(timeout: 3))
        XCTAssertFalse(app.buttons["gallery.confirm.notReceived"].exists)
        XCTAssertGreaterThan(confirmed.count, confirmedBefore)
        XCTAssertTrue(app.buttons["Reset"].exists)
    }
}

private extension XCUIApplication {
    /// Opens the debug design-system gallery on `page` (`-startScreen gallery -galleryPage n`, 1-based).
    static func launchGallery(page: Int) -> XCUIApplication {
        let app = XCUIApplication()
        app.launchArguments += ["-startScreen", "gallery", "-galleryPage", String(page)]
        app.launch()
        return app
    }

    /// Scrolls the gallery page down, a little at a time, until `element` can be tapped.
    func scrollUntilHittable(_ element: XCUIElement, maxSwipes: Int = 30) {
        var swipes = 0
        while !element.isHittable, swipes < maxSwipes {
            scrollViews.firstMatch.swipeUp(velocity: .slow)
            swipes += 1
        }
    }
}
