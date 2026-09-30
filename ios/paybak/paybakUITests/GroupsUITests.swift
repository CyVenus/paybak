import XCTest

/// Groups & Friends (app-architecture §6.3, M4): segments, a group's balances and the leave block,
/// the friend page's Remind, inviting a guest, the My QR code sheet and the empty account.
final class GroupsUITests: XCTestCase {
    override func setUp() {
        continueAfterFailure = false
    }

    @MainActor
    func testSegmentsSwitchBetweenGroupsAndFriends() {
        let app = XCUIApplication.launchPaybak(startScreen: .groupsList)
        XCTAssertTrue(app.element("groups.row.g-goa").waitForExistence(timeout: 6))
        XCTAssertTrue(app.element("groups.archived").exists)
        app.element("groups.segment.friends").tap()
        XCTAssertTrue(app.element("friends.row.p-rohan").waitForExistence(timeout: 3))
        XCTAssertEqual(app.element("friends.summary.owed").label, "You’re owed, +₹2,900")
        XCTAssertEqual(app.element("friends.summary.owe").label, "You owe, −₹1,850")
        XCTAssertTrue(app.element("groups.row.g-goa").waitForNonExistence(timeout: 3))
        app.element("groups.segment.groups").tap()
        XCTAssertTrue(app.element("groups.row.g-goa").waitForExistence(timeout: 3))
    }

    @MainActor
    func testGoaTripBalancesThenLeaveIsBlockedAndSettleUpOpensRecordPayment() {
        let app = XCUIApplication.launchPaybak(startScreen: .groupsList)
        app.element("groups.row.g-goa").tap()
        XCTAssertTrue(app.element("screen.group").waitForExistence(timeout: 3))
        XCTAssertTrue(app.element("group.balances.row.p-kabir").exists)
        XCTAssertTrue(app.element("group.simplifyNote").exists)
        app.element("group.settings").tap()
        XCTAssertTrue(app.element("screen.groupSettings").waitForExistence(timeout: 3))
        app.element("groupSettings.leave").tap()
        let settleUp = app.element("groupSettings.leaveBlocked.action")
        XCTAssertTrue(settleUp.waitForExistence(timeout: 3))
        XCTAssertTrue(app.staticTexts["You owe ₹1,400 in Goa Trip. Settle up with Kabir first, then you can leave."].exists)
        settleUp.tap()
        XCTAssertTrue(app.element("screen.recordPayment").waitForExistence(timeout: 3))
    }

    @MainActor
    func testRohanRemindOpensTheRemindSheet() {
        let app = XCUIApplication.launchPaybak(startScreen: .friendsList)
        app.element("friends.row.p-rohan").tap()
        XCTAssertTrue(app.element("screen.friend").waitForExistence(timeout: 3))
        XCTAssertTrue(app.element("friend.lastReminder").exists)
        app.element("friend.remind").tap()
        XCTAssertTrue(app.element("screen.remind").waitForExistence(timeout: 3))
    }

    @MainActor
    func testInviteAnanyaOpensHerGuestPage() {
        let app = XCUIApplication.launchPaybak(startScreen: .friendsList)
        app.element("groups.action").tap()
        XCTAssertTrue(app.element("screen.addFriend").waitForExistence(timeout: 3))
        app.element("addFriend.invite.p-ananya").buttons["Invite"].tap()
        XCTAssertTrue(app.element("friend.invite").waitForExistence(timeout: 3))
        XCTAssertTrue(app.element("friend.noBalance").exists)
    }

    @MainActor
    func testMyQRCodeCopiesTheLink() {
        let app = XCUIApplication.launchPaybak(startScreen: .addFriend)
        app.element("addFriend.myQr").tap()
        XCTAssertTrue(app.element("myQr.code").waitForExistence(timeout: 3))
        XCTAssertEqual(app.element("myQr.link").label, "paybak.app/i/arjun")
        app.element("myQr.copy").tap()
        XCTAssertTrue(app.element(label: "Link copied").waitForExistence(timeout: 3))
        app.element("myQr.close").tap()
        XCTAssertTrue(app.element("myQr.code").waitForNonExistence(timeout: 3))
    }

    @MainActor
    func testEmptyAccountShowsGroupsEmptyThenNewGroup() {
        let app = XCUIApplication.launchPaybak(startScreen: .groupsEmpty)
        XCTAssertTrue(app.element("groups.empty").waitForExistence(timeout: 6))
        app.element("groups.empty.newGroup").tap()
        XCTAssertTrue(app.element("screen.newGroup").waitForExistence(timeout: 3))
    }
}
