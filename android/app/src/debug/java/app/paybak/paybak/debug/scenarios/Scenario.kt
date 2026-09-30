package app.paybak.paybak.debug.scenarios

import app.paybak.paybak.domain.model.AvatarLook
import app.paybak.paybak.navigation.ActivitySegment
import app.paybak.paybak.navigation.GroupsSegment
import app.paybak.paybak.navigation.MainState
import app.paybak.paybak.navigation.NavEntry
import app.paybak.paybak.navigation.Route
import app.paybak.paybak.navigation.Tab

/**
 * How a debug start screen opens (app-architecture §3.10): the demo seed scenarios to load (at
 * Figma parity), the tab, the routes on the stack (pushes and modals in order), the route sheet on
 * top and the toast it shows. In-screen states (prefilled forms, local sheets, alerts) are applied
 * by the owning screen from the start id.
 */
data class Scenario(
    val seed: List<String>,
    val tab: Tab = Tab.Home,
    val stack: List<Route> = emptyList(),
    val sheet: Route? = null,
    val groupsSegment: GroupsSegment = GroupsSegment.Groups,
    val activitySegment: ActivitySegment = ActivitySegment.Timeline,
    val avatar: AvatarLook? = null,
    val toast: String? = null,
) {
    fun mainState(): MainState =
        MainState(
            entries = MainState().entries + (stack + listOfNotNull(sheet)).map { NavEntry(it) },
            selectedTab = tab,
            groupsSegment = groupsSegment,
            activitySegment = activitySegment,
            toast = toast,
        )
}

/** `E`: an empty account (the demo profile, no records). */
internal val E = listOf("empty")

/** `D` (+ x): the demo with Esha's pending claim, plus scenarios [extra]. */
internal fun demo(vararg extra: String) = listOf("eshaClaimsPayment") + extra

/** `D−claim` (+ x): the base records alone, plus scenarios [extra]. */
internal fun base(vararg extra: String) = extra.toList()

/** `P`: Pro on top (a yearly trial ending in 7 days). */
internal fun List<String>.pro() = this + "pro"
