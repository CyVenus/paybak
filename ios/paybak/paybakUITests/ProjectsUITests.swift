import XCTest

/// Projects (app-architecture §6.3, M7; screens-projects §11): the Build a Drone figures, adding a
/// planned and a bought part, closing the project and its archive once the plan is paid.
final class ProjectsUITests: XCTestCase {
    override func setUp() {
        continueAfterFailure = false
    }

    @MainActor
    func testBuildADroneShowsItsBudgetPartsAndPlan() {
        let app = XCUIApplication.launchPaybak(startScreen: .groupsList)
        app.element("groups.row.pj-drone").tap()
        XCTAssertTrue(app.element("project.state.active").waitForExistence(timeout: 3))
        let budget = app.element("project.budget").label
        for text in ["₹52,000", "87% used", "₹8,000 left", "Planned items bring it to ₹58,000"] {
            XCTAssertTrue(budget.contains(text), "\(text) in \(budget)")
        }
        let parts = ["gps", "camera", "transmitter", "escs", "battery", "fc", "motors", "frame"]
        XCTAssertTrue(parts.allSatisfy { app.element("project.component.c-drone-\($0)").exists })
        let transfer = app.element("project.transfer.0").label
        XCTAssertTrue(transfer.contains("Rohan owes Dev") && transfer.contains("₹8,500"), transfer)
        XCTAssertEqual(app.element("project.footnote").label, "You’re settled in this project.")
    }

    @MainActor
    func testAPlannedPartFeedsOnlyTheProjection() {
        let app = XCUIApplication.launchPaybak(startScreen: .projectDrone)
        app.element("project.addComponent").tap()
        let add = app.buttons["addComponent.add"]
        XCTAssertTrue(add.waitForExistence(timeout: 3))
        XCTAssertFalse(add.isEnabled)
        app.textFields["addComponent.name"].tap()
        app.typeText("Spare propellers")
        XCTAssertTrue(add.isEnabled)
        app.textFields["addComponent.estimate"].tap()
        app.typeText("1500")
        XCTAssertTrue(app.element("addComponent.status.planned").isSelected)
        app.revealSheetButton(add)
        XCTAssertTrue(app.element(label: "Component added").waitForExistence(timeout: 3))
        let budget = app.element("project.budget").label
        XCTAssertTrue(budget.contains("₹52,000") && budget.contains("Planned items bring it to ₹59,500"), budget)
        XCTAssertTrue(app.staticTexts["Spare propellers"].exists)
    }

    @MainActor
    func testAnActualCostChangesSpentAndShares() {
        let app = XCUIApplication.launchPaybak(startScreen: .projectAddComponent)
        let name = app.textFields["addComponent.name"]
        XCTAssertTrue(name.waitForExistence(timeout: 3))
        name.tap()
        app.typeText("Gimbal")
        app.textFields["addComponent.actual"].tap()
        app.typeText("4000")
        XCTAssertTrue(app.element("addComponent.status.bought").isSelected)
        app.revealSheetButton(app.buttons["addComponent.add"])
        XCTAssertTrue(app.element("project.budget").waitForLabel(containing: "₹56,000"))
        XCTAssertEqual(app.element("project.shareRule").label, "Equal split · ₹14,000 each so far")
    }

    @MainActor
    func testClosingShowsTheFinalPlan() {
        let app = XCUIApplication.launchPaybak(startScreen: .projectDrone)
        app.element("project.settings").tap()
        XCTAssertTrue(app.element("screen.projectSettings").waitForExistence(timeout: 3))
        app.element("projectSettings.close").tap()
        let confirm = app.element("projectSettings.closeAlert.action")
        XCTAssertTrue(confirm.waitForExistence(timeout: 3))
        XCTAssertTrue(app.staticTexts["Close Build a Drone?"].exists)
        confirm.tap()
        XCTAssertTrue(app.element("project.state.closed").waitForExistence(timeout: 3))
        XCTAssertTrue(app.element("project.notice").label.contains("Closed · Read-only"))
        XCTAssertTrue(app.element("project.transfer.0").label.contains("Rohan pays Dev"))
        XCTAssertFalse(app.element("project.addComponent").exists)
        XCTAssertFalse(app.element("project.settings").exists)
    }

    @MainActor
    func testConfirmedPaymentsArchiveTheClosedProject() {
        let app = XCUIApplication.launchPaybak(startScreen: .debugMenu, scenarios: ["closeDrone"])
        XCTAssertTrue(app.element("screen.debugMenu").waitForExistence(timeout: 6))
        app.buttons["debugMenu.Members record their Build a Drone payments"].tap()
        XCTAssertTrue(app.element("screen.debugMenu").waitForNonExistence(timeout: 3))
        app.element("home.logo").press(forDuration: 1)
        XCTAssertTrue(app.element("screen.debugMenu").waitForExistence(timeout: 3))
        app.buttons["debugMenu.Confirm pending Build a Drone payments"].tap()
        XCTAssertTrue(app.element("screen.debugMenu").waitForNonExistence(timeout: 3))
        app.element("home.tab.groups").tap()
        app.element("groups.row.pj-drone").tap()
        XCTAssertTrue(app.element("project.state.archived").waitForExistence(timeout: 3))
        XCTAssertTrue(app.element("project.planNotice").label.contains("Everyone is settled"))
        XCTAssertTrue(app.element("project.members.p-rohan").label.contains("Settled"))
    }
}

private extension XCUIApplication {
    /// The keyboard covers the sheet's button: scroll the sheet up to it, then tap it.
    func revealSheetButton(_ button: XCUIElement) {
        element("screen.projectAddComponent").swipeUp()
        button.tap()
    }
}

private extension XCUIElement {
    /// Waits until the element's label contains `text` (figures update after the sheet closes).
    func waitForLabel(containing text: String, timeout: TimeInterval = 3) -> Bool {
        let predicate = NSPredicate(format: "label CONTAINS %@", text)
        return XCTWaiter().wait(for: [XCTNSPredicateExpectation(predicate: predicate, object: self)], timeout: timeout) == .completed
    }
}
