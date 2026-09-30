import UIKit
import XCTest

/// Every screen id (app-architecture §1): the values of the debug `-startScreen` argument. Keep in
/// step with the app's `ScreenID`.
enum ScreenID: String, CaseIterable {
    case splash
    case welcome1
    case welcome2
    case welcome3
    case getStarted
    case signIn
    case verify
    case verifyWrong
    case setup1
    case setup2
    case setup3
    case setup4
    case allSet
    case homeFirstDay
    case homeActive
    case homeAllSettled
    case homeConfirmPayment
    case settlePaymentConfirmed
    case homeAddSheet
    case debugMenu
    case addExpenseEmpty
    case addExpenseFilled
    case addExpenseSplitWith
    case addExpensePaidBy
    case addExpensePayers
    case addExpenseSplitEqually
    case addExpenseSplitExactError
    case addExpenseCategory
    case addExpenseCurrency
    case addExpenseDueDate
    case addExpenseDate
    case addExpenseDiscard
    case expenseAdded
    case recordPayment
    case settleRecordKabir
    case paymentRecorded
    case settlePaymentPending
    case paymentCancelAlert
    case lendMoney
    case loanAdded
    case loanPaidBack
    case loanOverdue
    case newGroup
    case newGroupProject
    case newGroupCreated
    case groupsList
    case friendsList
    case groupsEmpty
    case friendsEmpty
    case groupGoaTrip
    case groupDubaiWeekend
    case groupSettings
    case groupLeaveBlocked
    case friendRohan
    case friendAnanyaGuest
    case addFriend
    case myQrCode
    case settleOwedBreakdown
    case settleOweBreakdown
    case settleUp
    case settleRemind
    case settleRemindShare
    case settleNotReceived
    case activityTimeline
    case activityEmpty
    case expenseVilla
    case expenseComment
    case expenseDelete
    case expenseDisputed
    case recentlyDeleted
    case notifications
    case activityLog
    case lockConfirmRequest
    case lockReminder
    case projectDrone
    case projectOverBudget
    case projectAddComponent
    case projectSettings
    case projectCloseAlert
    case projectClosed
    case projectArchived
    case profile
    case profileSignOut
    case editAvatarBoyHair
    case editAvatarBoyBeard
    case editAvatarBoyEyewear
    case editAvatarBoyOutfit
    case editAvatarGirlHair
    case editAvatarGirlAccessory
    case editAvatarGirlOutfit
    case editAvatarDiscard
    case paywall
    case proWelcome
    case paymentDetails
    case paymentAddUpi
    case paymentAddUpiError
    case settingsCurrency
    case settingsNotifications
    case mutedFriends
    case privacyData
    case privacyExport
    case privacyDeleteBlocked
    case helpFeedback
    case helpAnswer
    case insightsSeptember
    case insightsScrolled
    case insightsLocked
    case askStart
    case askAnswer
    case askConfirm
    case scanCamera
    case scanReview
    case scanAssign
    case scanAddExpense
    case recurringFlat302
    case recurringRepeat
    case recurringEnterAmount
}

extension XCUIApplication {
    /// Launches Paybak with its debug launch hooks (flow.md "Debug-only hooks", app-architecture
    /// §3.10): `-resetOnboarding` clears the profile and ledger, `-startScreen` opens a screen with
    /// its demo scenario, `-now` pins the clock, `-pro` overrides the plan, `-scenario` applies seed
    /// scenarios and `-link` opens a deep link. `textSize` sets the app's Dynamic Type size without
    /// touching the device's settings.
    static func launchPaybak(
        startScreen: ScreenID? = nil,
        resetOnboarding: Bool = true,
        textSize: UIContentSizeCategory? = nil,
        now: String? = nil,
        pro: Bool? = nil,
        scenarios: [String] = [],
        link: String? = nil
    ) -> XCUIApplication {
        let app = XCUIApplication()
        if resetOnboarding {
            app.launchArguments += ["-resetOnboarding", "YES"]
        }
        if let startScreen {
            app.launchArguments += ["-startScreen", startScreen.rawValue]
        }
        if let textSize {
            app.launchArguments += ["-UIPreferredContentSizeCategoryName", textSize.rawValue]
        }
        if let now {
            app.launchArguments += ["-now", now]
        }
        if let pro {
            app.launchArguments += ["-pro", pro ? "YES" : "NO"]
        }
        if !scenarios.isEmpty {
            app.launchArguments += ["-scenario", scenarios.joined(separator: ",")]
        }
        if let link {
            app.launchArguments += ["-link", link]
        }
        app.launch()
        return app
    }

    /// The root container of a screen (`screen.<id>`).
    func screen(_ id: ScreenID) -> XCUIElement {
        element("screen.\(id.rawValue)")
    }

    /// The element with a flow.md test id such as `welcome.continue`, whatever its type.
    func element(_ identifier: String) -> XCUIElement {
        descendants(matching: .any).matching(identifier: identifier).firstMatch
    }

    /// The first element labelled `label`, whatever its type (e.g. a toast).
    func element(label: String) -> XCUIElement {
        descendants(matching: .any).matching(NSPredicate(format: "label == %@", label)).firstMatch
    }

    /// The iOS back gesture: a drag from the left screen edge (onboarding screens have no nav bar).
    func swipeBackFromLeftEdge() {
        let start = coordinate(withNormalizedOffset: CGVector(dx: 0.01, dy: 0.5))
        start.press(forDuration: 0.05, thenDragTo: coordinate(withNormalizedOffset: CGVector(dx: 0.8, dy: 0.5)))
    }
}

extension XCUIElement {
    /// Waits until the element's label is `label` (text that animates in changes after a moment).
    @discardableResult
    func waitForLabel(_ label: String, timeout: TimeInterval = 3) -> Bool {
        let predicate = NSPredicate(format: "label == %@", label)
        let expectation = XCTNSPredicateExpectation(predicate: predicate, object: self)
        return XCTWaiter().wait(for: [expectation], timeout: timeout) == .completed
    }
}
