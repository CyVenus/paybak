package app.paybak.paybak.navigation

import androidx.compose.runtime.Composable
import app.paybak.paybak.debug.DebugMenu
import app.paybak.paybak.feature.activity.ActivityLogScreen
import app.paybak.paybak.feature.activity.ActivityTabScreen
import app.paybak.paybak.feature.activity.RecentlyDeletedScreen
import app.paybak.paybak.feature.addexpense.AddExpenseScreen
import app.paybak.paybak.feature.ask.AskScreen
import app.paybak.paybak.feature.avatar.EditAvatarScreen
import app.paybak.paybak.feature.expense.ExpenseDetailScreen
import app.paybak.paybak.feature.expense.PhotoViewerScreen
import app.paybak.paybak.feature.friends.AddFriendScreen
import app.paybak.paybak.feature.friends.FriendScreen
import app.paybak.paybak.feature.groups.GroupDetailScreen
import app.paybak.paybak.feature.groups.GroupSettingsScreen
import app.paybak.paybak.feature.groups.GroupsTabScreen
import app.paybak.paybak.feature.home.HomeScreen
import app.paybak.paybak.feature.loans.LendMoneyScreen
import app.paybak.paybak.feature.loans.LoanScreen
import app.paybak.paybak.feature.newgroup.NewGroupScreen
import app.paybak.paybak.feature.notifications.NotificationsScreen
import app.paybak.paybak.feature.payments.PaymentDetailScreen
import app.paybak.paybak.feature.payments.RecordPaymentScreen
import app.paybak.paybak.feature.pickers.CurrencyPickerSheet
import app.paybak.paybak.feature.pickers.DatePickerSheet
import app.paybak.paybak.feature.pickers.GroupPickerSheet
import app.paybak.paybak.feature.pickers.PeoplePickerScreen
import app.paybak.paybak.feature.pro.PaywallScreen
import app.paybak.paybak.feature.profile.ProfileScreen
import app.paybak.paybak.feature.projects.ProjectScreen
import app.paybak.paybak.feature.projects.ProjectSettingsScreen
import app.paybak.paybak.feature.recurring.EnterAmountScreen
import app.paybak.paybak.feature.recurring.RecurringScreen
import app.paybak.paybak.feature.recurring.RepeatSheet
import app.paybak.paybak.feature.scan.ScanReceiptScreen
import app.paybak.paybak.feature.settings.CurrencySettingsScreen
import app.paybak.paybak.feature.settings.ExportScreen
import app.paybak.paybak.feature.settings.HelpAnswerScreen
import app.paybak.paybak.feature.settings.HelpScreen
import app.paybak.paybak.feature.settings.MutedFriendsScreen
import app.paybak.paybak.feature.settings.NotificationSettingsScreen
import app.paybak.paybak.feature.settings.PaymentDetailsScreen
import app.paybak.paybak.feature.settings.PrivacyScreen
import app.paybak.paybak.feature.settle.NotReceivedSheet
import app.paybak.paybak.feature.settle.OweBreakdownScreen
import app.paybak.paybak.feature.settle.OwedBreakdownScreen
import app.paybak.paybak.feature.settle.RemindSheet
import app.paybak.paybak.feature.settle.SettleUpScreen

/** The one place a route becomes its screen (app-architecture §2.8), in lane sections. */
@Composable
fun RouteContent(route: Route) {
    when (route) {
        // MARK: M2
        Route.Tabs -> error("The tab shell is drawn by MainHost")
        is Route.AddSheet -> AddSheet(route)
        is Route.Activity -> ActivityTabScreen(route)
        is Route.DebugMenu -> DebugMenu(route)
        // end M2

        // MARK: Lane A
        is Route.Notifications -> NotificationsScreen(route)
        is Route.Expense -> ExpenseDetailScreen(route)
        is Route.Payment -> PaymentDetailScreen(route)
        is Route.Loan -> LoanScreen(route)
        is Route.RecentlyDeleted -> RecentlyDeletedScreen(route)
        is Route.ActivityLog -> ActivityLogScreen(route)
        is Route.PickPeople -> PeoplePickerScreen(route)
        is Route.PickCurrency -> CurrencyPickerSheet(route)
        is Route.PickDate -> DatePickerSheet(route)
        is Route.PickGroup -> GroupPickerSheet(route)
        is Route.PhotoViewer -> PhotoViewerScreen(route)
        is Route.AddExpense -> AddExpenseScreen(route)
        is Route.RecordPayment -> RecordPaymentScreen(route)
        is Route.LendMoney -> LendMoneyScreen(route)
        is Route.NewGroup -> NewGroupScreen(route)
        // end Lane A

        // MARK: Lane B
        is Route.Groups -> GroupsTabScreen(route)
        is Route.OwedBreakdown -> OwedBreakdownScreen(route)
        is Route.OweBreakdown -> OweBreakdownScreen(route)
        is Route.SettleUp -> SettleUpScreen(route)
        is Route.Remind -> RemindSheet(route)
        is Route.NotReceived -> NotReceivedSheet(route)
        is Route.Friend -> FriendScreen(route)
        is Route.AddFriend -> AddFriendScreen(route)
        is Route.Group -> GroupDetailScreen(route)
        is Route.GroupSettings -> GroupSettingsScreen(route)
        is Route.Project -> ProjectScreen(route)
        is Route.ProjectSettings -> ProjectSettingsScreen(route)
        // end Lane B

        // MARK: Lane C
        is Route.Profile -> ProfileScreen(route)
        is Route.Recurring -> RecurringScreen(route)
        is Route.RepeatRule -> RepeatSheet(route)
        is Route.EnterDraftAmount -> EnterAmountScreen(route)
        is Route.Ask -> AskScreen(route)
        is Route.ScanReceipt -> ScanReceiptScreen(route)
        is Route.Paywall -> PaywallScreen(route)
        is Route.EditAvatar -> EditAvatarScreen(route)
        is Route.PaymentDetails -> PaymentDetailsScreen(route)
        is Route.SettingsCurrency -> CurrencySettingsScreen(route)
        is Route.SettingsNotifications -> NotificationSettingsScreen(route)
        is Route.MutedFriends -> MutedFriendsScreen(route)
        is Route.PrivacyData -> PrivacyScreen(route)
        is Route.PrivacyExport -> ExportScreen(route)
        is Route.HelpFeedback -> HelpScreen(route)
        is Route.HelpAnswer -> HelpAnswerScreen(route)
        // end Lane C

        // MARK: Lane D
        is Route.Home -> HomeScreen(route)
    // end Lane D
    }
}
