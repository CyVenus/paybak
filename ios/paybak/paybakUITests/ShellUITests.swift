import XCTest

/// The app shell (app-architecture §6.1): tabs, the ＋ Add sheet on every tab, every start screen id,
/// deep links and persistence.
final class ShellUITests: XCTestCase {
    override func setUp() {
        continueAfterFailure = false
    }

    @MainActor
    func testTabsSwitchFromEveryTab() {
        let app = XCUIApplication.launchPaybak(startScreen: .homeActive)
        XCTAssertTrue(app.screen(.homeActive).waitForExistence(timeout: 5))
        let roots = ["groups": "screen.groups", "activity": "screen.activity", "profile": "screen.profile", "home": "screen.homeActive"]
        for from in ["home", "groups", "activity", "profile"] {
            app.buttons["home.tab.\(from)"].tap()
            XCTAssertTrue(app.element(roots[from]!).waitForExistence(timeout: 3), "\(from) root")
            for to in ["home", "groups", "activity", "profile"] where to != from {
                app.buttons["home.tab.\(to)"].tap()
                XCTAssertTrue(app.element(roots[to]!).waitForExistence(timeout: 3), "\(from) → \(to)")
                app.buttons["home.tab.\(from)"].tap()
            }
        }
    }

    @MainActor
    func testAddSheetOpensFromEveryTabAndEachRowOpensItsModal() {
        let app = XCUIApplication.launchPaybak(startScreen: .homeActive)
        XCTAssertTrue(app.screen(.homeActive).waitForExistence(timeout: 5))
        for tab in ["home", "groups", "activity", "profile"] {
            app.buttons["home.tab.\(tab)"].tap()
            app.buttons["home.tab.add"].tap()
            XCTAssertTrue(app.element("home.addSheet").waitForExistence(timeout: 3), "Add sheet over \(tab)")
            app.buttons["home.addSheet.close"].tap()
            XCTAssertTrue(app.element("home.addSheet").waitForNonExistence(timeout: 3))
        }
        app.buttons["home.tab.home"].tap()
        for (row, route) in [("expense", "addExpense"), ("payment", "recordPayment"), ("lend", "lendMoney"), ("group", "newGroup")] {
            app.buttons["home.tab.add"].tap()
            XCTAssertTrue(app.element("home.addSheet.\(row)").waitForExistence(timeout: 3))
            app.buttons["home.addSheet.\(row)"].tap()
            XCTAssertTrue(app.element("screen.\(route)").waitForExistence(timeout: 3), "\(row) opens \(route)")
            app.buttons["\(route).close"].tap()
            XCTAssertTrue(app.screen(.homeActive).waitForExistence(timeout: 3), "✕ closes \(route)")
        }
    }

    @MainActor
    func testPushAndBack() {
        let app = XCUIApplication.launchPaybak(startScreen: .groupSettings)
        XCTAssertTrue(app.element("screen.groupSettings").waitForExistence(timeout: 5))
        app.buttons["groupSettings.back"].tap()
        XCTAssertTrue(app.element("screen.group").waitForExistence(timeout: 3))
        app.buttons["group.back"].tap()
        XCTAssertTrue(app.element("screen.groups").waitForExistence(timeout: 3))
        XCTAssertTrue(app.buttons["home.tab.groups"].exists)
    }

    @MainActor
    func testDeepLinkOpensTheExpense() {
        let app = XCUIApplication.launchPaybak(startScreen: .homeActive, link: "paybak://expense/e-goa-villa")
        XCTAssertTrue(app.element("screen.expense").waitForExistence(timeout: 5))
        app.buttons["expense.back"].tap()
        XCTAssertTrue(app.element("screen.activity").waitForExistence(timeout: 3))
    }

    @MainActor
    func testDemoDataPersistsAcrossLaunches() {
        let app = XCUIApplication.launchPaybak(startScreen: .homeActive)
        XCTAssertTrue(app.screen(.homeActive).waitForExistence(timeout: 5))
        app.terminate()
        let relaunched = XCUIApplication.launchPaybak(resetOnboarding: false)
        XCTAssertTrue(relaunched.screen(.homeActive).waitForExistence(timeout: 6))
    }

    @MainActor
    func testDebugMenuStartsAnEmptyAccount() {
        let app = XCUIApplication.launchPaybak(startScreen: .homeActive)
        XCTAssertTrue(app.screen(.homeActive).waitForExistence(timeout: 5))
        app.element("home.logo").press(forDuration: 1)
        XCTAssertTrue(app.element("screen.debugMenu").waitForExistence(timeout: 3))
        app.buttons["debugMenu.Start an empty account"].tap()
        XCTAssertTrue(app.screen(.homeFirstDay).waitForExistence(timeout: 5))
    }

    // MARK: Every start screen id shows its route (batched per module)

    @MainActor func testStartScreensHome() { assertStartScreens(Self.home) }
    @MainActor func testStartScreensAddRecord() { assertStartScreens(Self.addRecord) }
    @MainActor func testStartScreensGroupsAndSettle() { assertStartScreens(Self.groupsAndSettle) }
    @MainActor func testStartScreensActivityAndProjects() { assertStartScreens(Self.activityAndProjects) }
    @MainActor func testStartScreensProfileAndSettings() { assertStartScreens(Self.profileAndSettings) }
    @MainActor func testStartScreensInsights() { assertStartScreens(Self.insights) }

    @MainActor
    private func assertStartScreens(_ table: [(ScreenID, String)]) {
        for (id, root) in table {
            let app = XCUIApplication.launchPaybak(startScreen: id)
            XCTAssertTrue(app.element(root).waitForExistence(timeout: 6), "\(id.rawValue) shows \(root)")
        }
    }

    private static let home: [(ScreenID, String)] = [
        (.homeFirstDay, "screen.homeFirstDay"), (.homeActive, "screen.homeActive"), (.homeAllSettled, "screen.homeAllSettled"),
        (.homeConfirmPayment, "screen.homeConfirmPayment"), (.settlePaymentConfirmed, "screen.homeActive"),
        (.homeAddSheet, "home.addSheet"), (.debugMenu, "screen.debugMenu"),
    ]

    private static let addRecord: [(ScreenID, String)] = [
        (.addExpenseEmpty, "screen.addExpense"), (.addExpenseFilled, "screen.addExpense"), (.addExpenseSplitWith, "screen.pickPeople"),
        (.addExpensePaidBy, "screen.addExpense"), (.addExpensePayers, "screen.addExpense"), (.addExpenseSplitEqually, "screen.addExpense"),
        (.addExpenseSplitExactError, "screen.addExpense"), (.addExpenseCategory, "screen.addExpense"),
        (.addExpenseCurrency, "screen.pickCurrency"), (.addExpenseDueDate, "screen.pickDate"), (.addExpenseDate, "screen.pickDate"),
        (.addExpenseDiscard, "screen.addExpense"), (.expenseAdded, "screen.expense"), (.recordPayment, "screen.recordPayment"),
        (.settleRecordKabir, "screen.recordPayment"), (.paymentRecorded, "screen.payment"), (.settlePaymentPending, "screen.payment"),
        (.paymentCancelAlert, "screen.payment"), (.lendMoney, "screen.lendMoney"), (.loanAdded, "screen.loan"),
        (.loanPaidBack, "screen.loan"), (.loanOverdue, "screen.loan"), (.newGroup, "screen.newGroup"),
        (.newGroupProject, "screen.newGroup"), (.newGroupCreated, "screen.group"),
    ]

    private static let groupsAndSettle: [(ScreenID, String)] = [
        (.groupsList, "screen.groups"), (.friendsList, "screen.groups"), (.groupsEmpty, "screen.groups"), (.friendsEmpty, "screen.groups"),
        (.groupGoaTrip, "screen.group"), (.groupDubaiWeekend, "screen.group"), (.groupSettings, "screen.groupSettings"),
        (.groupLeaveBlocked, "screen.groupSettings"), (.friendRohan, "screen.friend"), (.friendAnanyaGuest, "screen.friend"),
        (.addFriend, "screen.addFriend"), (.myQrCode, "screen.addFriend"),
        (.settleOwedBreakdown, "screen.owedBreakdown"), (.settleOweBreakdown, "screen.oweBreakdown"), (.settleUp, "screen.settleUp"),
        (.settleRemind, "screen.remind"), (.settleRemindShare, "ActivityListView"), (.settleNotReceived, "screen.notReceived"),
    ]

    private static let activityAndProjects: [(ScreenID, String)] = [
        (.activityTimeline, "screen.activity"), (.activityEmpty, "screen.activity"), (.expenseVilla, "screen.expense"),
        (.expenseComment, "screen.expense"), (.expenseDelete, "screen.expense"), (.expenseDisputed, "screen.expense"),
        (.recentlyDeleted, "screen.recentlyDeleted"), (.notifications, "screen.notifications"), (.activityLog, "screen.activityLog"),
        (.lockConfirmRequest, "screen.homeConfirmPayment"), (.lockReminder, "screen.homeConfirmPayment"),
        (.projectDrone, "screen.project"), (.projectOverBudget, "screen.project"), (.projectAddComponent, "screen.project"),
        (.projectSettings, "screen.projectSettings"), (.projectCloseAlert, "screen.projectSettings"), (.projectClosed, "screen.project"),
        (.projectArchived, "screen.project"),
    ]

    private static let profileAndSettings: [(ScreenID, String)] = [
        (.profile, "screen.profile"), (.profileSignOut, "screen.profile"), (.editAvatarBoyHair, "screen.editAvatar"),
        (.editAvatarBoyBeard, "screen.editAvatar"), (.editAvatarBoyEyewear, "screen.editAvatar"), (.editAvatarBoyOutfit, "screen.editAvatar"),
        (.editAvatarGirlHair, "screen.editAvatar"), (.editAvatarGirlAccessory, "screen.editAvatar"),
        (.editAvatarGirlOutfit, "screen.editAvatar"), (.editAvatarDiscard, "screen.editAvatar"), (.paywall, "screen.paywall"),
        (.proWelcome, "screen.paywall"), (.paymentDetails, "screen.paymentDetails"), (.paymentAddUpi, "screen.paymentDetails"),
        (.paymentAddUpiError, "screen.paymentDetails"), (.settingsCurrency, "screen.settingsCurrency"),
        (.settingsNotifications, "screen.settingsNotifications"), (.mutedFriends, "screen.mutedFriends"),
        (.privacyData, "screen.privacyData"), (.privacyExport, "screen.privacyExport"), (.privacyDeleteBlocked, "screen.privacyData"),
        (.helpFeedback, "screen.helpFeedback"), (.helpAnswer, "screen.helpAnswer"),
    ]

    private static let insights: [(ScreenID, String)] = [
        (.insightsSeptember, "screen.activity"), (.insightsScrolled, "screen.activity"), (.insightsLocked, "screen.activity"),
        (.askStart, "screen.ask"), (.askAnswer, "screen.ask"), (.askConfirm, "screen.ask"), (.scanCamera, "screen.scanReceipt"),
        (.scanReview, "screen.scanReceipt"), (.scanAssign, "screen.scanReceipt"), (.scanAddExpense, "screen.addExpense"),
        (.recurringFlat302, "screen.recurring"), (.recurringRepeat, "screen.repeatRule"), (.recurringEnterAmount, "screen.enterDraftAmount"),
    ]
}
