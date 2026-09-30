package app.paybak.paybak.debug.scenarios

import app.paybak.paybak.navigation.Route
import app.paybak.paybak.navigation.Tab

private val drone = Route.Project("pj-drone")

/** Projects (lane B, M7; app-architecture §1.7). */
internal val ProjectsScenarios: Map<String, Scenario> =
    mapOf(
        "projectDrone" to Scenario(demo(), tab = Tab.Groups, stack = listOf(drone)),
        "projectOverBudget" to
            Scenario(demo("devBuysGps"), tab = Tab.Groups, stack = listOf(drone)),
        "projectAddComponent" to Scenario(demo(), tab = Tab.Groups, stack = listOf(drone)),
        "projectSettings" to
            Scenario(
                demo(),
                tab = Tab.Groups,
                stack = listOf(drone, Route.ProjectSettings("pj-drone")),
            ),
        "projectCloseAlert" to
            Scenario(
                demo(),
                tab = Tab.Groups,
                stack = listOf(drone, Route.ProjectSettings("pj-drone")),
            ),
        "projectClosed" to Scenario(demo("closeDrone"), tab = Tab.Groups, stack = listOf(drone)),
        "projectArchived" to
            Scenario(demo(), tab = Tab.Groups, stack = listOf(Route.Project("pj-hackathon"))),
    )
