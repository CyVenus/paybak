package app.paybak.paybak.navigation

import androidx.compose.runtime.saveable.SaverScope
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppNavigatorTest {
    @Test
    fun everyFlowScreenIdRoundTrips() {
        val ids =
            listOf(
                "splash",
                "welcome1",
                "welcome2",
                "welcome3",
                "getStarted",
                "signIn",
                "verify",
                "verifyWrong",
                "setup1",
                "setup2",
                "setup3",
                "setup4",
                "allSet",
                "homeFirstDay",
                "homeActive",
                "homeAllSettled",
                "homeAddSheet",
            )
        assertEquals(ids, Destination.all.map(Destination::id))
        ids.forEach { id -> assertEquals(id, Destination.fromId(id)?.id) }
    }

    @Test
    fun aSavedStackRestoresEveryDestinationExactly() {
        val destinations =
            Destination.all + HomeState.entries.map { Destination.Home(it, addSheetOpen = true) }
        destinations.forEach { destination ->
            assertEquals(destination, AppNavigator(listOf(destination)).savedAndRestored().current)
        }
    }

    @Test
    fun aRestoredStackKeepsItsHistory() {
        val navigator = AppNavigator(canonicalBackStack(Destination.Setup(3))).savedAndRestored()
        navigator.back()
        assertEquals(Destination.Setup(2), navigator.current)
        navigator.back()
        navigator.back()
        assertEquals(Destination.Verify(wrongCode = false), navigator.current)
    }

    @Test
    fun backStepsThroughWelcomeThenLeavesTheApp() {
        val navigator = AppNavigator(listOf(Destination.Welcome(3)))
        navigator.back()
        assertEquals(Destination.Welcome(2), navigator.current)
        navigator.back()
        assertEquals(Destination.Welcome(1), navigator.current)
        assertFalse(navigator.handlesBack)
    }

    @Test
    fun backFromGetStartedReturnsToTheWelcomeStepLeftFrom() {
        val navigator = AppNavigator(listOf(Destination.Welcome(2)))
        navigator.push(Destination.GetStarted, NavTransition.Crossfade)
        navigator.back()
        assertEquals(Destination.Welcome(2), navigator.current)
    }

    @Test
    fun onlyTheScreenOnTopNavigates() {
        val navigator = AppNavigator(listOf(Destination.Welcome(3)))
        repeat(2) { navigator.from(Destination.Welcome(3)) { push(Destination.GetStarted) } }
        navigator.from(Destination.Welcome(3)) { replace(Destination.Welcome(2)) }
        assertEquals(Destination.GetStarted, navigator.current)
        navigator.back()
        assertEquals(Destination.Welcome(3), navigator.current)
    }

    @Test
    fun setupStepsGoBackOneAtATimeThenPop() {
        val navigator = AppNavigator(canonicalBackStack(Destination.Setup(2)))
        navigator.back()
        assertEquals(Destination.Setup(1), navigator.current)
        navigator.back()
        assertEquals(Destination.Verify(wrongCode = false), navigator.current)
    }

    @Test
    fun allSetAndHomeDoNotGoBackIntoOnboarding() {
        val navigator = AppNavigator(canonicalBackStack(Destination.Setup(4)))
        navigator.push(Destination.AllSet)
        navigator.back()
        assertEquals(Destination.AllSet, navigator.current)
        navigator.resetTo(Destination.Home(HomeState.FirstDay), NavTransition.Dissolve)
        assertFalse(navigator.handlesBack)
    }

    @Test
    fun backClosesTheAddSheetFirst() {
        val navigator =
            AppNavigator(listOf(Destination.Home(HomeState.Active, addSheetOpen = true)))
        assertTrue(navigator.handlesBack)
        navigator.back()
        assertEquals(Destination.Home(HomeState.Active), navigator.current)
    }
}

/** Saves and restores the navigator the way `rememberSaveable` does across process death. */
private fun AppNavigator.savedAndRestored(): AppNavigator {
    val saved = with(AppNavigator.Saver) { SaverScope { true }.save(this@savedAndRestored) }
    return AppNavigator.Saver.restore(checkNotNull(saved)) ?: error("Nothing restored")
}
