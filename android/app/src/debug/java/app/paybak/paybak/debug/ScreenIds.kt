package app.paybak.paybak.debug

import app.paybak.paybak.debug.scenarios.ActivityScenarios
import app.paybak.paybak.debug.scenarios.AddRecordScenarios
import app.paybak.paybak.debug.scenarios.GroupsScenarios
import app.paybak.paybak.debug.scenarios.HomeScenarios
import app.paybak.paybak.debug.scenarios.InsightsScenarios
import app.paybak.paybak.debug.scenarios.ProfileScenarios
import app.paybak.paybak.debug.scenarios.ProjectsScenarios
import app.paybak.paybak.debug.scenarios.Scenario
import app.paybak.paybak.debug.scenarios.SettingsScenarios
import app.paybak.paybak.debug.scenarios.SettleScenarios
import app.paybak.paybak.navigation.Destination

/**
 * Every debug start-screen id (app-architecture §1): the onboarding roots and the app's screens.
 */
object ScreenIds {
    /** The app's screen ids and how each opens, module by module. */
    val scenarios: Map<String, Scenario> =
        HomeScenarios +
            AddRecordScenarios +
            GroupsScenarios +
            SettleScenarios +
            ActivityScenarios +
            ProjectsScenarios +
            ProfileScenarios +
            SettingsScenarios +
            InsightsScenarios

    /** The onboarding ids (M1), in flow order. */
    val onboarding: List<String> = Destination.all.filter { it != Destination.Main }.map { it.id }

    val all: List<String> = onboarding + scenarios.keys
}
