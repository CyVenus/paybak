import XCTest

/// Add & Record (app-architecture §6.2 M3 UI tests): add an expense end to end, the Exact split
/// error, the discard alert, record a payment and cancel it, lend money in installments and create
/// a group.
final class AddRecordUITests: XCTestCase {
    override func setUp() {
        continueAfterFailure = false
    }

    @MainActor
    func testAddExpenseFromThePlusSheet() {
        let app = XCUIApplication.launchPaybak(startScreen: .homeActive)
        XCTAssertTrue(app.screen(.homeActive).waitForExistence(timeout: 5))
        app.buttons["home.tab.add"].tap()
        app.buttons["home.addSheet.expense"].tap()
        XCTAssertTrue(app.element("screen.addExpense").waitForExistence(timeout: 3))
        let save = app.buttons["addExpense.action"]
        XCTAssertFalse(save.isEnabled, "Save waits for an amount and someone else")

        app.typeText("2800")
        XCTAssertFalse(save.isEnabled, "Still nobody else on it")
        app.element("addExpense.addPeople").tap()
        XCTAssertTrue(app.element("screen.pickPeople").waitForExistence(timeout: 3))
        for friend in ["p-priya", "p-esha", "p-dev"] {
            app.buttons["splitWith.friend.\(friend)"].tap()
        }
        app.buttons["splitWith.done"].tap()
        XCTAssertTrue(app.element("addExpense.person.p-dev").waitForExistence(timeout: 3))
        XCTAssertTrue(save.isEnabled)

        app.element("addExpense.title").tap()
        app.typeText("Dinner at Olive Garden\n")
        app.buttons["addExpense.row.category"].tap()
        app.buttons["category.row.food"].tap()
        XCTAssertTrue(app.element("category.sheet").waitForNonExistence(timeout: 3))
        app.buttons["addExpense.due.weekend"].tap()
        XCTAssertTrue(app.buttons["addExpense.row.split"].label.contains("Equally · ₹700 each"))
        save.tap()

        XCTAssertTrue(app.toast("Expense added"))
        XCTAssertTrue(app.element("screen.expense").waitForExistence(timeout: 3))
        for person in ["me", "p-priya", "p-esha", "p-dev"] {
            XCTAssertTrue(app.element("expense.split.\(person)").label.contains("₹700"), "\(person) owes ₹700")
        }
        XCTAssertTrue(app.element("expense.due").label.contains("Sun 4 Oct"))
    }

    @MainActor
    func testExactSplitMustAddUp() {
        let app = XCUIApplication.launchPaybak(startScreen: .addExpenseSplitEqually)
        XCTAssertTrue(app.element("screen.splitEditor").waitForExistence(timeout: 5))
        app.buttons["Exact"].tap()
        let devField = app.element(label: "Dev’s amount")
        devField.tap()
        app.typeText(String(repeating: XCUIKeyboardKey.delete.rawValue, count: 3) + "550")
        XCTAssertTrue(app.element("split.total").label.contains("₹150 left"))
        XCTAssertFalse(app.buttons["split.done"].isEnabled)

        app.typeText(String(repeating: XCUIKeyboardKey.delete.rawValue, count: 3) + "700")
        XCTAssertTrue(app.element("split.total").label.contains("₹0 left"))
        XCTAssertTrue(app.buttons["split.done"].isEnabled)
        app.buttons["split.done"].tap()
        XCTAssertTrue(app.buttons["addExpense.row.split"].waitForExistence(timeout: 3))
        XCTAssertTrue(app.buttons["addExpense.row.split"].label.contains("Exact · 4 people"))
    }

    @MainActor
    func testDiscardAlertOnAChangedForm() {
        let app = XCUIApplication.launchPaybak(startScreen: .addExpenseFilled)
        XCTAssertTrue(app.element("screen.addExpense").waitForExistence(timeout: 5))
        app.buttons["addExpense.close"].tap()
        XCTAssertTrue(app.buttons["addExpense.discardAlert.cancel"].waitForExistence(timeout: 3))
        app.buttons["addExpense.discardAlert.cancel"].tap()
        XCTAssertTrue(app.element("screen.addExpense").exists)
        app.buttons["addExpense.close"].tap()
        app.buttons["addExpense.discardAlert.action"].tap()
        XCTAssertTrue(app.element("screen.addExpense").waitForNonExistence(timeout: 3))
        XCTAssertTrue(app.screen(.homeConfirmPayment).waitForExistence(timeout: 3))
    }

    @MainActor
    func testRecordPaymentToMeeraThenCancelIt() {
        let app = XCUIApplication.launchPaybak(startScreen: .homeActive)
        XCTAssertTrue(app.screen(.homeActive).waitForExistence(timeout: 5))
        app.buttons["home.tab.add"].tap()
        app.buttons["home.addSheet.payment"].tap()
        XCTAssertTrue(app.element("screen.recordPayment").waitForExistence(timeout: 3))
        XCTAssertTrue(app.element("recordPayment.summary").label.contains("You paid Meera ₹450 in cash for Flat 302."))
        app.buttons["recordPayment.method.upi"].tap()
        XCTAssertTrue(app.element("recordPayment.preview").waitForExistence(timeout: 2))
        app.buttons["recordPayment.action"].tap()

        XCTAssertTrue(app.toast("Payment recorded"))
        XCTAssertTrue(app.element("screen.payment").waitForExistence(timeout: 3))
        XCTAssertTrue(app.element("paymentRecorded.status").label.contains("Pending confirmation"))
        app.buttons["paymentRecorded.cancel"].tap()
        XCTAssertTrue(app.buttons["paymentRecorded.alert.action"].waitForExistence(timeout: 3))
        app.buttons["paymentRecorded.alert.action"].tap()
        XCTAssertTrue(app.screen(.homeActive).waitForExistence(timeout: 3))
    }

    @MainActor
    func testLendMoneyInThreeInstallments() {
        let app = XCUIApplication.launchPaybak(startScreen: .homeActive)
        XCTAssertTrue(app.screen(.homeActive).waitForExistence(timeout: 5))
        app.buttons["home.tab.add"].tap()
        app.buttons["home.addSheet.lend"].tap()
        XCTAssertTrue(app.element("screen.lendMoney").waitForExistence(timeout: 3))
        app.typeText("6000")
        app.buttons["lendMoney.person"].tap()
        app.buttons["pickPerson.friend.p-dev"].tap()
        XCTAssertTrue(app.element("lendMoney.schedule").waitForExistence(timeout: 3))
        XCTAssertEqual(app.element("lendMoney.schedule").label, "3 × ₹2,000 · Fri 30 Oct, Mon 30 Nov and Wed 30 Dec")
        app.buttons["lendMoney.action"].tap()

        XCTAssertTrue(app.element("screen.loan").waitForExistence(timeout: 3))
        XCTAssertTrue(app.element("loan.installment.1").label.contains("Due Fri 30 Oct"))
        XCTAssertTrue(app.element("loan.installment.3").label.contains("Due Wed 30 Dec"))
        XCTAssertTrue(app.buttons["loan.recordRepayment"].exists)
    }

    @MainActor
    func testNewGroupOpensOnTheGroupsTab() {
        let app = XCUIApplication.launchPaybak(startScreen: .homeActive)
        XCTAssertTrue(app.screen(.homeActive).waitForExistence(timeout: 5))
        app.buttons["home.tab.add"].tap()
        app.buttons["home.addSheet.group"].tap()
        XCTAssertTrue(app.element("screen.newGroup").waitForExistence(timeout: 3))
        XCTAssertFalse(app.buttons["newGroup.action"].isEnabled)
        app.element("newGroup.name").tap()
        app.typeText("Weekend Trek\n")
        app.buttons["newGroup.type.trip"].tap()
        app.buttons["newGroup.addPeople"].tap()
        app.buttons["splitWith.friend.p-esha"].tap()
        app.buttons["splitWith.done"].tap()
        XCTAssertTrue(app.element("newGroup.member.1").waitForExistence(timeout: 3))
        app.buttons["newGroup.action"].tap()

        XCTAssertTrue(app.toast("Group created"))
        XCTAssertTrue(app.element("screen.group").waitForExistence(timeout: 3))
        app.buttons["group.back"].tap()
        XCTAssertTrue(app.element("screen.groups").waitForExistence(timeout: 3))
    }
}

private extension XCUIApplication {
    /// Whether the app toast shows `text` (it fades out after 2 s).
    func toast(_ text: String) -> Bool {
        let toast = element("toast")
        return toast.waitForExistence(timeout: 3) && toast.label.contains(text)
    }
}
