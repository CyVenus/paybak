package app.paybak.paybak.navigation

import androidx.annotation.StringRes
import app.paybak.paybak.R
import app.paybak.paybak.ui.icons.PbIcon

/** The four tabs of the tab bar (＋ sits between Groups and Activity). */
enum class Tab(
    val id: String,
    @param:StringRes val label: Int,
    val icon: PbIcon,
    val route: Route,
) {
    Home("home", R.string.shell_tab_home, PbIcon.Home, Route.Home),
    Groups("groups", R.string.shell_tab_groups, PbIcon.Groups, Route.Groups),
    Activity("activity", R.string.shell_tab_activity, PbIcon.Activity, Route.Activity),
    Profile("profile", R.string.shell_tab_profile, PbIcon.Profile, Route.Profile),
}

/** The Groups tab's segments, kept in the navigator while the app runs. */
enum class GroupsSegment {
    Groups,
    Friends,
}

/** The Activity tab's segments. */
enum class ActivitySegment {
    Timeline,
    Insights,
}
