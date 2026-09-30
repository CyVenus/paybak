package app.paybak.paybak.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import app.paybak.paybak.domain.model.LedgerJson
import app.paybak.paybak.domain.model.ME
import app.paybak.paybak.domain.model.newId
import app.paybak.paybak.ui.components.PbToastState
import java.time.YearMonth
import kotlinx.serialization.Serializable

/** One screen on the main stack; [key] keeps its saved UI state apart from other entries. */
@Serializable data class NavEntry(val route: Route, val key: String = newId())

/** Everything [MainNavigator] saves across process death. */
@Serializable
data class MainState(
    val entries: List<NavEntry> = listOf(NavEntry(Route.Tabs, TABS_KEY)),
    val selectedTab: Tab = Tab.Home,
    val groupsSegment: GroupsSegment = GroupsSegment.Groups,
    val activitySegment: ActivitySegment = ActivitySegment.Timeline,
    val insightsMonth: String? = null,
    /** A toast to show once when the app opens (debug start screens such as `expenseAdded`). */
    val toast: String? = null,
) {
    fun encode(): String = LedgerJson.encodeToString(serializer(), this)

    companion object {
        const val TABS_KEY = "tabs"

        fun decode(json: String): MainState? = runCatching {
            LedgerJson.decodeFromString(serializer(), json)
        }.getOrNull()
    }
}

/** How the screen change animates. */
enum class NavMotion {
    None,
    Push,
    Pop,
    ModalUp,
    ModalDown,
}

/**
 * The main navigation state (app-architecture §2.1, §2.7): one stack over the tab shell. Entry 0 is
 * always [Route.Tabs]; a [Presentation.Modal] entry starts a layer, later pushes belong to it, and
 * a [Presentation.Sheet] entry floats over the entry below. Screens reach it through
 * `LocalMainNavigator`.
 *
 * @param isPro Whether Pro is on, for [requirePro].
 */
@Stable
class MainNavigator(state: MainState = MainState(), private val isPro: () -> Boolean = { false }) {
    val entries = mutableStateListOf<NavEntry>().apply { addAll(state.entries) }
    var selectedTab by mutableStateOf(state.selectedTab)
    var groupsSegment by mutableStateOf(state.groupsSegment)
    var activitySegment by mutableStateOf(state.activitySegment)

    /** The month Insights shows; null = the current month. */
    var insightsMonth by mutableStateOf(state.insightsMonth?.let(YearMonth::parse))

    /** Picker results waiting for their caller ([RouteResultEffect]). */
    val results = mutableStateMapOf<String, RouteResult>()

    /** The app's one toast host (every layer shows it). */
    val toasts = PbToastState().apply { state.toast?.let(::show) }

    var motion by mutableStateOf(NavMotion.None)
        private set

    /** What fills the screen: the top entry that isn't a sheet. */
    val screen: NavEntry
        get() = entries.last { it.route.presentation != Presentation.Sheet }

    /** The route sheet floating over [screen], if any. */
    val sheet: NavEntry?
        get() = entries.last().takeIf { it.route.presentation == Presentation.Sheet }

    val onTabRoot: Boolean
        get() = screen.route == Route.Tabs

    /** False on Home with nothing open: system back then leaves the app (flow.md). */
    val handlesBack: Boolean
        get() = entries.size > 1 || selectedTab != Tab.Home

    val state: MainState
        get() =
            MainState(
                entries.toList(),
                selectedTab,
                groupsSegment,
                activitySegment,
                insightsMonth?.toString(),
            )

    /** Replaces the whole state (the debug menu's demo loads). */
    fun reset(state: MainState) {
        motion = NavMotion.None
        entries.clear()
        entries.addAll(state.entries)
        selectedTab = state.selectedTab
        groupsSegment = state.groupsSegment
        activitySegment = state.activitySegment
        insightsMonth = state.insightsMonth?.let(YearMonth::parse)
        state.toast?.let(::toast)
    }

    /** Opens [route] as its presentation says. */
    fun open(route: Route) {
        when (route.presentation) {
            Presentation.Tab -> Tab.entries.firstOrNull { it.route == route }?.let(::select)
            Presentation.Push -> {
                removeSheet()
                motion = NavMotion.Push
                entries += NavEntry(route)
            }
            Presentation.Modal -> {
                removeSheet()
                motion = NavMotion.ModalUp
                entries += NavEntry(route)
            }
            Presentation.Sheet -> {
                removeSheet()
                entries += NavEntry(route)
            }
        }
    }

    /**
     * System back and the back chevrons: a sheet, then the top push or modal, then (on another tab)
     * Home. False when there is nothing to go back to.
     */
    fun back(): Boolean {
        val top = entries.last()
        when {
            top.route.presentation == Presentation.Sheet -> entries.removeAt(entries.lastIndex)
            entries.size > 1 -> {
                motion =
                    if (top.route.presentation == Presentation.Modal) NavMotion.ModalDown
                    else NavMotion.Pop
                entries.removeAt(entries.lastIndex)
            }
            selectedTab != Tab.Home -> select(Tab.Home)
            else -> return false
        }
        return true
    }

    fun dismissSheet() = removeSheet()

    /** Closes the top modal layer with everything pushed in it. */
    fun dismissModal() {
        val index = entries.indexOfLast { it.route.presentation == Presentation.Modal }
        if (index <= 0) return
        motion = NavMotion.ModalDown
        removeFrom(index)
    }

    /** Pops the top layer back to its root (the tab shell or the modal's first screen). */
    fun popToRoot() {
        val root =
            entries.indexOfLast { it.route.presentation == Presentation.Modal }.coerceAtLeast(0)
        if (entries.size > root + 1) {
            motion = NavMotion.Pop
            removeFrom(root + 1)
        }
    }

    /** Closes everything and shows [tab]'s root. */
    fun select(tab: Tab) {
        if (entries.size > 1) {
            motion =
                if (screen.route.presentation == Presentation.Modal) NavMotion.ModalDown
                else NavMotion.Pop
            removeFrom(1)
        }
        selectedTab = tab
    }

    /** Closes the current sheet, then opens [route] (the Add sheet's rows). */
    fun replaceSheet(route: Route) {
        removeSheet()
        open(route)
    }

    /**
     * A modal's Save (Add expense, Record payment, Lend money): [detail] goes on the layer below
     * the modal, the modal slides away uncovering it, then [toast] shows.
     */
    fun didSave(detail: Route, toast: String) {
        val index = entries.indexOfLast { it.route.presentation == Presentation.Modal }
        if (index <= 0) {
            open(detail)
        } else {
            motion = NavMotion.ModalDown
            entries.add(index, NavEntry(detail))
            removeFrom(index + 1)
        }
        toast(toast)
    }

    /** New group's Create: closes the modals, shows the Groups tab with the new group pushed. */
    fun didCreateGroup(groupId: String, isProject: Boolean) {
        motion = NavMotion.ModalDown
        removeFrom(1)
        selectedTab = Tab.Groups
        entries += NavEntry(if (isProject) Route.Project(groupId) else Route.Group(groupId))
        toast(if (isProject) "Project created" else "Group created")
    }

    /** Opens [route] on Pro; on the free plan the paywall, which continues to it afterwards. */
    fun requirePro(route: Route) = open(if (isPro()) route else Route.Paywall(continueTo = route))

    /** The paywall's Done: closes it, then opens where it was going. */
    fun finishPaywall() {
        val paywall = entries.lastOrNull { it.route is Route.Paywall }?.route as? Route.Paywall
        dismissModal()
        paywall?.continueTo?.let(::open)
    }

    fun toast(text: String) = toasts.show(text)

    /** A picker's answer: stores [result] for [requestId] and closes the picker. */
    fun complete(requestId: String, result: RouteResult) {
        results[requestId] = result
        if (entries.last().route.presentation == Presentation.Modal) dismissModal() else back()
    }

    /** Opens an internal link as a notification tap would. */
    fun open(link: DeepLink) {
        when (link) {
            is DeepLink.Claim -> {
                select(Tab.Activity)
                activitySegment = ActivitySegment.Timeline
                if (link.notReceived) open(Route.NotReceived(link.paymentId))
            }
            is DeepLink.RecordPayment -> {
                select(Tab.Home)
                open(
                    Route.RecordPayment(
                        RecordPaymentArgs(
                            fromId = ME,
                            toId = link.toId,
                            amount = link.amount,
                            groupId = link.groupId,
                        )
                    )
                )
            }
            is DeepLink.Insights -> {
                select(Tab.Activity)
                activitySegment = ActivitySegment.Insights
                insightsMonth = link.month
            }
            is DeepLink.Remind -> {
                select(Tab.Home)
                open(Route.Remind(link.personId))
            }
            is DeepLink.Expense -> {
                select(Tab.Activity)
                open(Route.Expense(link.expenseId))
            }
            is DeepLink.Payment -> {
                select(Tab.Activity)
                open(Route.Payment(link.paymentId))
            }
            is DeepLink.RecurringDraft -> {
                select(selectedTab)
                open(Route.EnterDraftAmount(link.draftId))
            }
        }
    }

    private fun removeSheet() {
        if (entries.last().route.presentation == Presentation.Sheet)
            entries.removeAt(entries.lastIndex)
    }

    private fun removeFrom(index: Int) {
        while (entries.size > index) entries.removeAt(entries.lastIndex)
    }
}

/**
 * Delivers the result of the picker opened with [requestId] once, when this screen is back on top
 * (app-architecture §2.7).
 */
@Composable
fun RouteResultEffect(requestId: String, onResult: (RouteResult) -> Unit) {
    val navigator = LocalMainNavigator.current
    val result = navigator.results[requestId]
    val latest by rememberUpdatedState(onResult)
    LaunchedEffect(result) {
        if (result != null) {
            navigator.results.remove(requestId)
            latest(result)
        }
    }
}
