import XCTest

/// The interactive shared components (components-app.md), driven through the debug gallery pages:
/// the sheet opens, picks and closes; the alert presents and cancels; Confirm collapses the card.
final class GalleryComponentsUITests: XCTestCase {
    override func setUpWithError() throws {
        continueAfterFailure = false
    }

    @MainActor
    func testSheetPicksACategoryAndCloses() {
        let app = XCUIApplication.launchGallery(page: 36)
        let preview = app.element("gallery.sheetPreview")
        XCTAssertTrue(preview.waitForExistence(timeout: 5))
        preview.tap()

        let travel = app.buttons["gallery.sheet.Travel"]
        XCTAssertTrue(travel.waitForExistence(timeout: 3))
        XCTAssertFalse(travel.isSelected)
        travel.tap()
        XCTAssertTrue(travel.isSelected)
        XCTAssertFalse(app.buttons["gallery.sheet.Food"].isSelected)

        app.buttons["gallery.sheet.close"].tap()
        XCTAssertTrue(travel.waitForNonExistence(timeout: 3))
    }

    @MainActor
    func testAlertPresentsOverTheScreenAndCancels() {
        let app = XCUIApplication.launchGallery(page: 22)
        let sampleDiscard = app.buttons["Discard"].firstMatch
        XCTAssertTrue(sampleDiscard.waitForExistence(timeout: 5))
        let cancel = app.buttons["gallery.alert.cancel"]
        XCTAssertFalse(cancel.exists)

        // The sample card's Discard presents the live alert over the page.
        sampleDiscard.tap()
        XCTAssertTrue(cancel.waitForExistence(timeout: 3))
        XCTAssertTrue(app.buttons["gallery.alert.action"].exists)

        cancel.tap()
        XCTAssertTrue(cancel.waitForNonExistence(timeout: 3))
    }

    @MainActor
    func testConfirmCollapsesThePaymentCard() {
        let app = XCUIApplication.launchGallery(page: 30)
        let confirm = app.buttons["gallery.confirm.confirm"]
        XCTAssertTrue(confirm.waitForExistence(timeout: 5))
        let confirmed = app.descendants(matching: .any).matching(NSPredicate(format: "label BEGINSWITH %@", "Esha paid you ₹700"))
        let confirmedBefore = confirmed.count

        confirm.tap()

        XCTAssertTrue(confirm.waitForNonExistence(timeout: 3))
        XCTAssertFalse(app.buttons["gallery.confirm.notReceived"].exists)
        XCTAssertGreaterThan(confirmed.count, confirmedBefore)
    }
}

private extension XCUIApplication {
    /// Opens the debug design-system gallery on `page` (`-startScreen gallery -galleryPage n`).
    static func launchGallery(page: Int) -> XCUIApplication {
        let app = XCUIApplication()
        app.launchArguments += ["-startScreen", "gallery", "-galleryPage", String(page)]
        app.launch()
        return app
    }
}
