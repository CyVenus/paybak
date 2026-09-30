package app.paybak.paybak.debug.scenarios

import app.paybak.paybak.navigation.ActivityFilter
import app.paybak.paybak.navigation.Route
import app.paybak.paybak.navigation.Tab

/** Activity, expense detail and notifications (lane A, M6; app-architecture §1.6). */
internal val ActivityScenarios: Map<String, Scenario> =
    mapOf(
        "activityTimeline" to Scenario(demo(), tab = Tab.Activity),
        "activityEmpty" to Scenario(E, tab = Tab.Activity),
        "expenseVilla" to
            Scenario(demo(), tab = Tab.Activity, stack = listOf(Route.Expense("e-goa-villa"))),
        "expenseComment" to
            Scenario(demo(), tab = Tab.Activity, stack = listOf(Route.Expense("e-goa-villa"))),
        "expenseDelete" to
            Scenario(demo(), tab = Tab.Activity, stack = listOf(Route.Expense("e-goa-villa"))),
        "expenseDisputed" to
            Scenario(
                demo("eshaFlagsSeafood"),
                tab = Tab.Activity,
                stack = listOf(Route.Expense("e-goa-seafood")),
            ),
        "recentlyDeleted" to
            Scenario(demo(), tab = Tab.Activity, stack = listOf(Route.RecentlyDeleted)),
        "notifications" to Scenario(demo(), stack = listOf(Route.Notifications)),
        "activityLog" to
            Scenario(
                demo(),
                tab = Tab.Groups,
                stack =
                    listOf(
                        Route.Project("pj-drone"),
                        Route.ActivityLog(ActivityFilter.Project("pj-drone")),
                    ),
            ),
        "lockConfirmRequest" to Scenario(demo()),
        "lockReminder" to Scenario(demo()),
    )
