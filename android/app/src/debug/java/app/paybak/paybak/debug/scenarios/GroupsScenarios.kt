package app.paybak.paybak.debug.scenarios

import app.paybak.paybak.navigation.GroupsSegment
import app.paybak.paybak.navigation.Route
import app.paybak.paybak.navigation.Tab

/** Groups & Friends (lane B, M4; app-architecture §1.4). */
internal val GroupsScenarios: Map<String, Scenario> =
    mapOf(
        "groupsList" to Scenario(demo(), tab = Tab.Groups),
        "friendsList" to Scenario(demo(), tab = Tab.Groups, groupsSegment = GroupsSegment.Friends),
        "groupsEmpty" to Scenario(E, tab = Tab.Groups),
        "friendsEmpty" to Scenario(E, tab = Tab.Groups, groupsSegment = GroupsSegment.Friends),
        "groupGoaTrip" to Scenario(demo(), tab = Tab.Groups, stack = listOf(Route.Group("g-goa"))),
        "groupDubaiWeekend" to
            Scenario(demo(), tab = Tab.Groups, stack = listOf(Route.Group("g-dubai"))),
        "groupSettings" to
            Scenario(
                demo(),
                tab = Tab.Groups,
                stack = listOf(Route.Group("g-goa"), Route.GroupSettings("g-goa")),
            ),
        "groupLeaveBlocked" to
            Scenario(
                demo(),
                tab = Tab.Groups,
                stack = listOf(Route.Group("g-goa"), Route.GroupSettings("g-goa")),
            ),
        "friendRohan" to
            Scenario(
                demo(),
                tab = Tab.Groups,
                groupsSegment = GroupsSegment.Friends,
                stack = listOf(Route.Friend("p-rohan")),
            ),
        "friendAnanyaGuest" to
            Scenario(
                demo(),
                tab = Tab.Groups,
                groupsSegment = GroupsSegment.Friends,
                stack = listOf(Route.Friend("p-ananya")),
            ),
        "addFriend" to
            Scenario(
                demo(),
                tab = Tab.Groups,
                groupsSegment = GroupsSegment.Friends,
                stack = listOf(Route.AddFriend),
            ),
        "myQrCode" to
            Scenario(
                demo(),
                tab = Tab.Groups,
                groupsSegment = GroupsSegment.Friends,
                stack = listOf(Route.AddFriend),
            ),
    )
