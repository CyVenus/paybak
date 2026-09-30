import SwiftUI

/// Every screen id (app-architecture §1): the values of the debug `-startScreen` argument and the
/// names of the refs. Some ids are states of one route (welcome1–3, verifyWrong, the Home states,
/// form states); the owning screen applies those with `onStartScreen`.
enum ScreenID: String, CaseIterable {
    // MARK: Launch and onboarding (M1)
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

    // MARK: Home (lane D, M2)
    case homeFirstDay
    case homeActive
    case homeAllSettled
    case homeConfirmPayment
    case settlePaymentConfirmed
    case homeAddSheet
    case debugMenu

    // MARK: Add & Record (lane A)
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

    // MARK: Groups & Friends (lane B)
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

    // MARK: Settle up (lane B)
    case settleOwedBreakdown
    case settleOweBreakdown
    case settleUp
    case settleRemind
    case settleRemindShare
    case settleNotReceived

    // MARK: Activity (lane A)
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

    // MARK: Projects (lane B)
    case projectDrone
    case projectOverBudget
    case projectAddComponent
    case projectSettings
    case projectCloseAlert
    case projectClosed
    case projectArchived

    // MARK: Profile, Settings & Pro (lane C)
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

    // MARK: Insights & AI (lane C)
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

    /// Splash and the onboarding stack (M1); everything else starts in the main app.
    var isOnboarding: Bool {
        switch self {
        case .splash, .welcome1, .welcome2, .welcome3, .getStarted, .signIn, .verify, .verifyWrong,
             .setup1, .setup2, .setup3, .setup4, .allSet: true
        default: false
        }
    }
}

extension View {
    /// Marks a screen's root container for UI tests as `screen.<id>` (flow.md "UI tests and test
    /// IDs"). The container keeps its children's own identifiers.
    func screenIdentifier(_ screen: ScreenID) -> some View {
        accessibilityElement(children: .contain)
            .accessibilityIdentifier("screen.\(screen.rawValue)")
    }

    /// Debug builds: when the app was started with one of `screens` (`-startScreen`), runs `apply`
    /// once with it so the screen can show its designed in-screen state (a prefilled form, an open
    /// local sheet, an alert). Release builds never call it.
    func onStartScreen(_ screens: Set<ScreenID>, perform apply: @escaping (ScreenID) -> Void) -> some View {
        modifier(StartScreenApplier(screens: screens, apply: apply))
    }
}

private struct StartScreenApplier: ViewModifier {
    let screens: Set<ScreenID>
    let apply: (ScreenID) -> Void
    @Environment(AppRouter.self) private var router

    func body(content: Content) -> some View {
        #if DEBUG
        content.task {
            if let screen = router.takeStartScreen(in: screens) {
                apply(screen)
            }
        }
        #else
        content
        #endif
    }
}

#if DEBUG
extension ScreenID {
    /// Screens after the sign-in choice. Starting on one of them in a debug build seeds the sample
    /// profile so it renders like Figma.
    var isMidFlow: Bool {
        switch self {
        case .splash, .welcome1, .welcome2, .welcome3, .getStarted: false
        default: true
        }
    }
}
#endif
