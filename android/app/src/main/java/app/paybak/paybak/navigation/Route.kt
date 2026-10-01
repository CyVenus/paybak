package app.paybak.paybak.navigation

import app.paybak.paybak.domain.model.Day
import app.paybak.paybak.domain.model.ReminderContext
import app.paybak.paybak.domain.model.RepeatRule as RepeatRuleValue
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** How a route is shown (app-architecture §2.1). */
enum class Presentation {
    /** A tab root inside the tab shell. */
    Tab,

    /** Pushed onto the top layer; hides the tab bar. */
    Push,

    /** A full-screen modal layer with its own push stack. */
    Modal,

    /** A floating sheet over the top layer (one at a time). */
    Sheet,
}

/** Who builds a route's screen (app-architecture §6, §7.1). */
enum class Owner {
    M2,
    LaneA,
    LaneB,
    LaneC,
    LaneD,
}

/** A route's id (its screen root is tagged `screen.<id>`), presentation, owner and spec. */
data class RouteInfo(
    val id: String,
    val presentation: Presentation,
    val owner: Owner,
    val spec: String,
)

private fun tab(id: String, owner: Owner, spec: String) =
    RouteInfo(id, Presentation.Tab, owner, spec)

private fun push(id: String, owner: Owner, spec: String) =
    RouteInfo(id, Presentation.Push, owner, spec)

private fun modal(id: String, owner: Owner, spec: String) =
    RouteInfo(id, Presentation.Modal, owner, spec)

private fun sheet(id: String, owner: Owner, spec: String) =
    RouteInfo(id, Presentation.Sheet, owner, spec)

/**
 * Every route of the app (app-architecture §2.2), identical on iOS. Each has exactly one screen
 * file, `@Composable fun <Name>Screen(route: Route.<Name>)`, wired in [RouteContent]. Routes are
 * serializable so the back stack survives process death.
 *
 * A lane that truly needs a new route adds it inside its own section below (and in
 * RouteContent.kt); the parameters listed here are the contract.
 */
@Serializable
sealed interface Route {
    val info: RouteInfo

    // MARK: M2

    /** The tab shell: entry 0 of the main stack. */
    @Serializable
    @SerialName("tabs")
    data object Tabs : Route {
        override val info
            get() = tab("tabs", Owner.M2, "app-architecture §2.1")
    }

    @Serializable
    @SerialName("addSheet")
    data object AddSheet : Route {
        override val info
            get() = sheet("addSheet", Owner.M2, "screens-home §5, home-v2 §2.4")
    }

    @Serializable
    @SerialName("activity")
    data object Activity : Route {
        override val info
            get() = tab("activity", Owner.M2, "screens-activity §3")
    }

    @Serializable
    @SerialName("debugMenu")
    data object DebugMenu : Route {
        override val info
            get() = sheet("debugMenu", Owner.M2, "app-architecture §3.10")
    }

    // end M2

    // MARK: Lane A

    @Serializable
    @SerialName("notifications")
    data object Notifications : Route {
        override val info
            get() = push("notifications", Owner.LaneA, "screens-activity §6")
    }

    @Serializable
    @SerialName("expense")
    data class Expense(val expenseId: String, val toast: String? = null) : Route {
        override val info
            get() = push("expense", Owner.LaneA, "screens-activity §4, add-expense §11")
    }

    @Serializable
    @SerialName("payment")
    data class Payment(val paymentId: String) : Route {
        override val info
            get() = push("payment", Owner.LaneA, "record-lend-group §3, settle §5")
    }

    @Serializable
    @SerialName("loan")
    data class Loan(val loanId: String) : Route {
        override val info
            get() = push("loan", Owner.LaneA, "record-lend-group §5")
    }

    @Serializable
    @SerialName("recentlyDeleted")
    data object RecentlyDeleted : Route {
        override val info
            get() = push("recentlyDeleted", Owner.LaneA, "screens-activity §5")
    }

    @Serializable
    @SerialName("activityLog")
    data class ActivityLog(val filter: ActivityFilter) : Route {
        override val info
            get() = push("activityLog", Owner.LaneA, "screens-activity §3.9")
    }

    /** [includesYou] lists "You" (Split with, Record payment); off where you're always in. */
    @Serializable
    @SerialName("pickPeople")
    data class PickPeople(
        val request: PickRequest,
        val mode: PickMode = PickMode.Multi,
        val selected: List<String> = emptyList(),
        val title: String? = null,
        val allowsGuests: Boolean = true,
        val includesYou: Boolean = true,
    ) : Route {
        override val info
            get() = push("pickPeople", Owner.LaneA, "add-expense §5")
    }

    @Serializable
    @SerialName("pickCurrency")
    data class PickCurrency(
        val request: PickRequest,
        val selected: String? = null,
        val title: String? = null,
    ) : Route {
        override val info
            get() = sheet("pickCurrency", Owner.LaneA, "add-expense §9")
    }

    @Serializable
    @SerialName("pickDate")
    data class PickDate(
        val request: PickRequest,
        val kind: DateKind = DateKind.Date,
        val selected: Day? = null,
        val allowsNone: Boolean = false,
        val earliest: Day? = null,
        val latest: Day? = null,
    ) : Route {
        override val info
            get() = sheet("pickDate", Owner.LaneA, "add-expense §10")
    }

    /**
     * With [personId] (Record payment's For) it lists what you share with them: "None", your groups
     * with them and your open loans with them; a loan answers with its id.
     */
    @Serializable
    @SerialName("pickGroup")
    data class PickGroup(
        val request: PickRequest,
        val selected: String? = null,
        val personId: String? = null,
    ) : Route {
        override val info
            get() = sheet("pickGroup", Owner.LaneA, "record-lend-group §2 (For)")
    }

    @Serializable
    @SerialName("photoViewer")
    data class PhotoViewer(val photo: PhotoRef) : Route {
        override val info
            get() = modal("photoViewer", Owner.LaneA, "screens-activity §4")
    }

    @Serializable
    @SerialName("addExpense")
    data class AddExpense(val args: AddExpenseArgs = AddExpenseArgs()) : Route {
        override val info
            get() = modal("addExpense", Owner.LaneA, "screens-add-expense §4")
    }

    @Serializable
    @SerialName("recordPayment")
    data class RecordPayment(val args: RecordPaymentArgs = RecordPaymentArgs()) : Route {
        override val info
            get() = modal("recordPayment", Owner.LaneA, "record-lend-group §2, settle §4")
    }

    @Serializable
    @SerialName("lendMoney")
    data class LendMoney(val args: LendMoneyArgs = LendMoneyArgs()) : Route {
        override val info
            get() = modal("lendMoney", Owner.LaneA, "record-lend-group §4")
    }

    @Serializable
    @SerialName("newGroup")
    data class NewGroup(val mode: NewGroupMode = NewGroupMode.Group) : Route {
        override val info
            get() = modal("newGroup", Owner.LaneA, "record-lend-group §6")
    }

    // end Lane A

    // MARK: Lane B

    @Serializable
    @SerialName("groups")
    data object Groups : Route {
        override val info
            get() = tab("groups", Owner.LaneB, "screens-groups §3")
    }

    @Serializable
    @SerialName("owedBreakdown")
    data object OwedBreakdown : Route {
        override val info
            get() = push("owedBreakdown", Owner.LaneB, "screens-settle §1")
    }

    @Serializable
    @SerialName("oweBreakdown")
    data object OweBreakdown : Route {
        override val info
            get() = push("oweBreakdown", Owner.LaneB, "screens-settle §2")
    }

    /** Settle up for everyone, or for one group. */
    @Serializable
    @SerialName("settleUp")
    data class SettleUp(val groupId: String? = null) : Route {
        override val info
            get() = push("settleUp", Owner.LaneB, "screens-settle §3")
    }

    @Serializable
    @SerialName("remind")
    data class Remind(val personId: String, val context: ReminderContext? = null) : Route {
        override val info
            get() = sheet("remind", Owner.LaneB, "screens-settle §6–7")
    }

    @Serializable
    @SerialName("notReceived")
    data class NotReceived(val paymentId: String) : Route {
        override val info
            get() = sheet("notReceived", Owner.LaneB, "screens-settle §8")
    }

    @Serializable
    @SerialName("friend")
    data class Friend(val personId: String) : Route {
        override val info
            get() = push("friend", Owner.LaneB, "screens-groups §6")
    }

    @Serializable
    @SerialName("addFriend")
    data object AddFriend : Route {
        override val info
            get() = push("addFriend", Owner.LaneB, "screens-groups §7")
    }

    @Serializable
    @SerialName("group")
    data class Group(val groupId: String) : Route {
        override val info
            get() = push("group", Owner.LaneB, "screens-groups §4, record-lend-group §7")
    }

    @Serializable
    @SerialName("groupSettings")
    data class GroupSettings(val groupId: String) : Route {
        override val info
            get() = push("groupSettings", Owner.LaneB, "screens-groups §5")
    }

    @Serializable
    @SerialName("project")
    data class Project(val groupId: String) : Route {
        override val info
            get() = push("project", Owner.LaneB, "screens-projects §3–8")
    }

    @Serializable
    @SerialName("projectSettings")
    data class ProjectSettings(val groupId: String) : Route {
        override val info
            get() = push("projectSettings", Owner.LaneB, "screens-projects §6")
    }

    // end Lane B

    // MARK: Lane C

    @Serializable
    @SerialName("profile")
    data object Profile : Route {
        override val info
            get() = tab("profile", Owner.LaneC, "screens-profile §2")
    }

    @Serializable
    @SerialName("recurring")
    data class Recurring(val groupId: String) : Route {
        override val info
            get() = push("recurring", Owner.LaneC, "screens-insights-ai §5.2")
    }

    @Serializable
    @SerialName("repeatRule")
    data class RepeatRule(
        val request: PickRequest,
        val current: RepeatRuleValue? = null,
        val startDate: Day? = null,
    ) : Route {
        override val info
            get() = sheet("repeatRule", Owner.LaneC, "screens-insights-ai §5.3")
    }

    @Serializable
    @SerialName("enterDraftAmount")
    data class EnterDraftAmount(val draftId: String) : Route {
        override val info
            get() = modal("enterDraftAmount", Owner.LaneC, "screens-insights-ai §5.4")
    }

    @Serializable
    @SerialName("ask")
    data object Ask : Route {
        override val info
            get() = modal("ask", Owner.LaneC, "screens-insights-ai §3")
    }

    /**
     * Scan receipt for the Add expense form that opened it; [personIds] are the people on that
     * expense, who the items get assigned to (empty: just you, and Assign items lets you add more).
     */
    @Serializable
    @SerialName("scanReceipt")
    data class ScanReceipt(val request: PickRequest, val personIds: List<String> = emptyList()) :
        Route {
        override val info
            get() = modal("scanReceipt", Owner.LaneC, "screens-insights-ai §4")
    }

    /** The paywall; after a trial or purchase, Done continues to [continueTo]. */
    @Serializable
    @SerialName("paywall")
    data class Paywall(val continueTo: Route? = null) : Route {
        override val info
            get() = modal("paywall", Owner.LaneC, "screens-settings §2–3")
    }

    @Serializable
    @SerialName("editAvatar")
    data object EditAvatar : Route {
        override val info
            get() = push("editAvatar", Owner.LaneC, "screens-profile §3–4")
    }

    @Serializable
    @SerialName("paymentDetails")
    data object PaymentDetails : Route {
        override val info
            get() = push("paymentDetails", Owner.LaneC, "screens-settings §4–5")
    }

    @Serializable
    @SerialName("settingsCurrency")
    data object SettingsCurrency : Route {
        override val info
            get() = push("settingsCurrency", Owner.LaneC, "screens-settings §6")
    }

    @Serializable
    @SerialName("settingsNotifications")
    data object SettingsNotifications : Route {
        override val info
            get() = push("settingsNotifications", Owner.LaneC, "screens-settings §7")
    }

    @Serializable
    @SerialName("mutedFriends")
    data object MutedFriends : Route {
        override val info
            get() = push("mutedFriends", Owner.LaneC, "screens-settings §7")
    }

    @Serializable
    @SerialName("privacyData")
    data object PrivacyData : Route {
        override val info
            get() = push("privacyData", Owner.LaneC, "screens-settings §8, §10")
    }

    @Serializable
    @SerialName("privacyExport")
    data object PrivacyExport : Route {
        override val info
            get() = push("privacyExport", Owner.LaneC, "screens-settings §9")
    }

    @Serializable
    @SerialName("helpFeedback")
    data object HelpFeedback : Route {
        override val info
            get() = push("helpFeedback", Owner.LaneC, "screens-settings §11")
    }

    @Serializable
    @SerialName("helpAnswer")
    data class HelpAnswer(val index: Int) : Route {
        override val info
            get() = push("helpAnswer", Owner.LaneC, "screens-settings §11")
    }

    // end Lane C

    // MARK: Lane D

    @Serializable
    @SerialName("home")
    data object Home : Route {
        override val info
            get() = tab("home", Owner.LaneD, "screens-home, screens-home-v2 §2")
    }

    // end Lane D
}

val Route.id: String
    get() = info.id

val Route.presentation: Presentation
    get() = info.presentation
