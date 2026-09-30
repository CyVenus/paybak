import Foundation

/// Every destination of the main app (app-architecture §2.2). Each route has exactly one screen file
/// (`RouteView` maps them) and a fixed presentation. Lanes add routes only inside their own section,
/// and the same section of `presentation`, `id` and `RouteView`.
indirect enum Route: Hashable, Codable, Identifiable {
    // MARK: M2 (the shell)
    case home
    case groups
    case activity
    case profile
    case addSheet
    case debugMenu
    // end M2

    // MARK: Lane A
    case notifications
    case expense(ExpenseID, toast: String? = nil)
    case payment(PaymentID)
    case loan(LoanID)
    case recentlyDeleted
    case activityLog(ActivityFilter)
    case pickPeople(PeoplePickRequest)
    case pickCurrency(CurrencyPickRequest)
    case pickDate(DatePickRequest)
    case pickGroup(GroupPickRequest)
    case photoViewer(PhotoRef)
    case addExpense(AddExpenseArgs)
    case recordPayment(RecordPaymentArgs)
    case lendMoney(LendMoneyArgs)
    case newGroup(NewGroupMode)
    // end Lane A

    // MARK: Lane B
    case owedBreakdown
    case oweBreakdown
    case settleUp(groupId: GroupID?)
    case remind(personId: PersonID, context: ReminderContext?)
    case notReceived(PaymentID)
    case friend(PersonID)
    case addFriend
    case group(GroupID)
    case groupSettings(GroupID)
    case project(GroupID)
    case projectSettings(GroupID)
    // end Lane B

    // MARK: Lane C
    case recurring(GroupID)
    case repeatRule(RepeatRuleRequest)
    case enterDraftAmount(DraftID)
    case ask
    case scanReceipt(ScanRequest)
    case paywall(continueTo: Route?)
    case editAvatar
    case paymentDetails
    case settingsCurrency
    case settingsNotifications
    case mutedFriends
    case privacyData
    case privacyExport
    case helpFeedback
    case helpAnswer(index: Int)
    // end Lane C

    // MARK: Lane D
    // end Lane D

    var id: Self { self }

    /// How the route is shown (§2.1).
    enum Presentation: Equatable {
        /// A tab root inside the shell.
        case tab(Tab)
        /// Pushed on the top layer's stack.
        case push
        /// A full-screen modal layer with its own stack and sheet.
        case modal
        /// The top layer's route sheet.
        case sheet(PBSheetDetent)
    }

    var presentation: Presentation {
        switch self {
        // M2
        case .home: .tab(.home)
        case .groups: .tab(.groups)
        case .activity: .tab(.activity)
        case .profile: .tab(.profile)
        case .addSheet: .sheet(.fitted)
        case .debugMenu: .sheet(.large)

        // Lane A
        case .notifications, .expense, .payment, .loan, .recentlyDeleted, .activityLog, .pickPeople: .push
        case .pickCurrency: .sheet(.large)
        case .pickDate, .pickGroup: .sheet(.fitted)
        case .photoViewer, .addExpense, .recordPayment, .lendMoney, .newGroup: .modal

        // Lane B
        case .owedBreakdown, .oweBreakdown, .settleUp, .friend, .addFriend, .group, .groupSettings, .project, .projectSettings: .push
        case .remind, .notReceived: .sheet(.fitted)

        // Lane C
        case .recurring, .editAvatar, .paymentDetails, .settingsCurrency, .settingsNotifications, .mutedFriends,
             .privacyData, .privacyExport, .helpFeedback, .helpAnswer: .push
        case .repeatRule: .sheet(.fitted)
        case .enterDraftAmount, .ask, .scanReceipt, .paywall: .modal

        // Lane D
        }
    }

    /// The route id (§2.2): the screen's test-id root is `screen.<routeId>`.
    var routeID: String {
        switch self {
        // M2
        case .home: "home"
        case .groups: "groups"
        case .activity: "activity"
        case .profile: "profile"
        case .addSheet: "addSheet"
        case .debugMenu: "debugMenu"

        // Lane A
        case .notifications: "notifications"
        case .expense: "expense"
        case .payment: "payment"
        case .loan: "loan"
        case .recentlyDeleted: "recentlyDeleted"
        case .activityLog: "activityLog"
        case .pickPeople: "pickPeople"
        case .pickCurrency: "pickCurrency"
        case .pickDate: "pickDate"
        case .pickGroup: "pickGroup"
        case .photoViewer: "photoViewer"
        case .addExpense: "addExpense"
        case .recordPayment: "recordPayment"
        case .lendMoney: "lendMoney"
        case .newGroup: "newGroup"

        // Lane B
        case .owedBreakdown: "owedBreakdown"
        case .oweBreakdown: "oweBreakdown"
        case .settleUp: "settleUp"
        case .remind: "remind"
        case .notReceived: "notReceived"
        case .friend: "friend"
        case .addFriend: "addFriend"
        case .group: "group"
        case .groupSettings: "groupSettings"
        case .project: "project"
        case .projectSettings: "projectSettings"

        // Lane C
        case .recurring: "recurring"
        case .repeatRule: "repeatRule"
        case .enterDraftAmount: "enterDraftAmount"
        case .ask: "ask"
        case .scanReceipt: "scanReceipt"
        case .paywall: "paywall"
        case .editAvatar: "editAvatar"
        case .paymentDetails: "paymentDetails"
        case .settingsCurrency: "settingsCurrency"
        case .settingsNotifications: "settingsNotifications"
        case .mutedFriends: "mutedFriends"
        case .privacyData: "privacyData"
        case .privacyExport: "privacyExport"
        case .helpFeedback: "helpFeedback"
        case .helpAnswer: "helpAnswer"

        // Lane D
        }
    }
}

/// The four tabs of the shell (＋ is an action, not a tab).
enum Tab: String, Codable, CaseIterable {
    case home
    case groups
    case activity
    case profile

    var route: Route {
        switch self {
        case .home: .home
        case .groups: .groups
        case .activity: .activity
        case .profile: .profile
        }
    }
}
