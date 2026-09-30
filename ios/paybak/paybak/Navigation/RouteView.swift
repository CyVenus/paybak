import SwiftUI

/// The single map from a route to its screen (app-architecture §2.2). Screen initializers are the
/// contract between lanes; each lane edits only its own section.
struct RouteView: View {
    let route: Route

    var body: some View {
        switch route {
        // MARK: M2
        case .home: HomeScreen()
        case .groups: GroupsTabScreen()
        case .activity: ActivityTabScreen()
        case .profile: ProfileScreen()
        case .addSheet: AddSheet()
        case .debugMenu: debugMenu
        // end M2

        // MARK: Lane A
        case .notifications: NotificationsScreen()
        case .expense(let id, let toast): ExpenseDetailScreen(expenseId: id, toast: toast)
        case .payment(let id): PaymentDetailScreen(paymentId: id)
        case .loan(let id): LoanScreen(loanId: id)
        case .recentlyDeleted: RecentlyDeletedScreen()
        case .activityLog(let filter): ActivityLogScreen(filter: filter)
        case .pickPeople(let request): PeoplePickerScreen(request: request)
        case .pickCurrency(let request): CurrencyPickerSheet(request: request)
        case .pickDate(let request): DatePickerSheet(request: request)
        case .pickGroup(let request): GroupPickerSheet(request: request)
        case .photoViewer(let photo): PhotoViewerScreen(photo: photo)
        case .addExpense(let args): AddExpenseScreen(args: args)
        case .recordPayment(let args): RecordPaymentScreen(args: args)
        case .lendMoney(let args): LendMoneyScreen(args: args)
        case .newGroup(let mode): NewGroupScreen(mode: mode)
        // end Lane A

        // MARK: Lane B
        case .owedBreakdown: OwedBreakdownScreen()
        case .oweBreakdown: OweBreakdownScreen()
        case .settleUp(let groupId): SettleUpScreen(groupId: groupId)
        case .remind(let personId, let context): RemindSheet(personId: personId, context: context)
        case .notReceived(let id): NotReceivedSheet(paymentId: id)
        case .friend(let id): FriendScreen(personId: id)
        case .addFriend: AddFriendScreen()
        case .group(let id): GroupDetailScreen(groupId: id)
        case .groupSettings(let id): GroupSettingsScreen(groupId: id)
        case .project(let id): ProjectScreen(groupId: id)
        case .projectSettings(let id): ProjectSettingsScreen(groupId: id)
        // end Lane B

        // MARK: Lane C
        case .recurring(let id): RecurringScreen(groupId: id)
        case .repeatRule(let request): RepeatSheet(request: request)
        case .enterDraftAmount(let id): EnterAmountScreen(draftId: id)
        case .ask: AskScreen()
        case .scanReceipt(let request): ScanReceiptScreen(request: request)
        case .paywall(let continueTo): PaywallScreen(continueTo: continueTo)
        case .editAvatar: EditAvatarScreen()
        case .paymentDetails: PaymentDetailsScreen()
        case .settingsCurrency: CurrencySettingsScreen()
        case .settingsNotifications: NotificationSettingsScreen()
        case .mutedFriends: MutedFriendsScreen()
        case .privacyData: PrivacyScreen()
        case .privacyExport: ExportScreen()
        case .helpFeedback: HelpScreen()
        case .helpAnswer(let index): HelpAnswerScreen(index: index)
        // end Lane C

        // MARK: Lane D
        // end Lane D
        }
    }

    @ViewBuilder
    private var debugMenu: some View {
        #if DEBUG
        DebugMenu()
        #endif
    }
}
