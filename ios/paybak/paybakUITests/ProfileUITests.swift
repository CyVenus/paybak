import XCTest

/// Profile and the avatar editor (screens-profile §7, app-architecture §6.4 "M8 UI tests").
final class ProfileUITests: XCTestCase {
    override func setUp() {
        continueAfterFailure = false
    }

    /// Quiff → Girl → Ponytail → Boy keeps Quiff → Back asks → Keep editing → Save → the saved look
    /// reopens; Back with nothing changed leaves without asking.
    @MainActor
    func testEditAvatarKeepsPicksAndSaves() {
        let app = XCUIApplication.launchPaybak(startScreen: .profile)
        XCTAssertTrue(app.screen(.profile).waitForExistence(timeout: 5))
        app.buttons["profile.editAvatar"].tap()
        XCTAssertTrue(app.element("screen.editAvatar").waitForExistence(timeout: 3))

        app.buttons["editAvatar.option.quiff"].tap()
        XCTAssertTrue(app.buttons["editAvatar.option.quiff"].isSelected)
        app.buttons["editAvatar.gender.girl"].tap()
        app.buttons["editAvatar.option.ponytail"].tap()
        XCTAssertTrue(app.buttons["editAvatar.option.ponytail"].isSelected)
        app.buttons["editAvatar.gender.boy"].tap()
        XCTAssertTrue(app.buttons["editAvatar.option.quiff"].waitForExistence(timeout: 2))
        XCTAssertTrue(app.buttons["editAvatar.option.quiff"].isSelected)

        app.buttons["editAvatar.back"].tap()
        XCTAssertTrue(app.buttons["editAvatar.discard.cancel"].waitForExistence(timeout: 2))
        app.buttons["editAvatar.discard.cancel"].tap()
        XCTAssertTrue(app.element("screen.editAvatar").exists)
        app.buttons["editAvatar.action"].tap()
        XCTAssertTrue(app.screen(.profile).waitForExistence(timeout: 3))

        app.buttons["profile.avatar"].tap()
        XCTAssertTrue(app.buttons["editAvatar.option.quiff"].waitForExistence(timeout: 3))
        XCTAssertTrue(app.buttons["editAvatar.option.quiff"].isSelected)
        app.buttons["editAvatar.back"].tap()
        XCTAssertTrue(app.screen(.profile).waitForExistence(timeout: 3))
        XCTAssertFalse(app.buttons["editAvatar.discard.cancel"].exists)
    }

    @MainActor
    func testDiscardLeavesTheSavedLook() {
        let app = XCUIApplication.launchPaybak(startScreen: .editAvatarDiscard)
        XCTAssertTrue(app.buttons["editAvatar.discard.action"].waitForExistence(timeout: 5))
        app.buttons["editAvatar.discard.action"].tap()
        XCTAssertTrue(app.screen(.profile).waitForExistence(timeout: 3))
        app.buttons["profile.editAvatar"].tap()
        XCTAssertTrue(app.buttons["editAvatar.option.curly"].waitForExistence(timeout: 3))
        XCTAssertTrue(app.buttons["editAvatar.option.curly"].isSelected)
    }

    @MainActor
    func testShuffleChangesTheLook() {
        let app = XCUIApplication.launchPaybak(startScreen: .editAvatarBoyHair)
        XCTAssertTrue(app.buttons["editAvatar.shuffle"].waitForExistence(timeout: 5))
        app.buttons["editAvatar.category.eyes"].tap()
        XCTAssertTrue(app.buttons["editAvatar.option.dots"].waitForExistence(timeout: 2))
        for _ in 0..<3 { app.buttons["editAvatar.shuffle"].tap() }
        XCTAssertTrue(app.buttons["editAvatar.category.eyes"].isSelected, "Shuffle keeps the chip")
    }

    @MainActor
    func testEveryRowOpensItsScreen() {
        let app = XCUIApplication.launchPaybak(startScreen: .profile)
        XCTAssertTrue(app.screen(.profile).waitForExistence(timeout: 5))
        XCTAssertEqual(app.element("profile.handle").label, "arjun@okaxis")
        let rows = ["payment": "paymentDetails", "currency": "settingsCurrency", "notifications": "settingsNotifications",
                    "privacy": "privacyData", "help": "helpFeedback"]
        for (row, screen) in rows {
            app.buttons["profile.row.\(row)"].tap()
            XCTAssertTrue(app.element("screen.\(screen)").waitForExistence(timeout: 3), "\(row) opens \(screen)")
            app.buttons["\(screen).back"].tap()
            XCTAssertTrue(app.screen(.profile).waitForExistence(timeout: 3))
        }
        app.buttons["profile.row.pro"].tap()
        XCTAssertTrue(app.element("screen.paywall").waitForExistence(timeout: 3))
        app.buttons["paywall.close"].tap()
        XCTAssertTrue(app.screen(.profile).waitForExistence(timeout: 3))
    }

    @MainActor
    func testSignOutKeepsDataAndShowsGetStarted() {
        let app = XCUIApplication.launchPaybak(startScreen: .profile)
        XCTAssertTrue(app.screen(.profile).waitForExistence(timeout: 5))
        app.buttons["profile.signOut"].tap()
        XCTAssertTrue(app.buttons["profile.signOutAlert.cancel"].waitForExistence(timeout: 2))
        app.buttons["profile.signOutAlert.cancel"].tap()
        XCTAssertTrue(app.buttons["profile.signOutAlert.cancel"].waitForNonExistence(timeout: 2))
        app.buttons["profile.signOut"].tap()
        XCTAssertTrue(app.buttons["profile.signOutAlert.action"].waitForExistence(timeout: 2))
        app.buttons["profile.signOutAlert.action"].tap()
        XCTAssertTrue(app.screen(.getStarted).waitForExistence(timeout: 3))
    }
}
