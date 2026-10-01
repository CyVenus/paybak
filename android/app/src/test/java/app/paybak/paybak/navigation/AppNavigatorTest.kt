package app.paybak.paybak.navigation

import androidx.compose.runtime.saveable.SaverScope
import java.io.File
import java.net.URLClassLoader
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
                "main",
            )
        assertEquals(ids, Destination.all.map(Destination::id))
        ids.forEach { id -> assertEquals(id, Destination.fromId(id)?.id) }
    }

    /**
     * A normal launch touches [Destination.Splash] before anything asks for [Destination.all]; the
     * list must still hold Splash, or restoring a saved stack after a configuration change crashes.
     * A fresh class loader makes Splash the first class to load, whatever ran before.
     */
    @Test
    fun everyRootIsListedWhenSplashLoadsFirst() {
        val classPath =
            System.getProperty("java.class.path").split(File.pathSeparator).map {
                File(it).toURI().toURL()
            }
        URLClassLoader(classPath.toTypedArray(), null).use { fresh ->
            val name = Destination::class.java.name
            Class.forName("$name\$Splash", true, fresh)
            val companion = Class.forName(name, true, fresh).getField("Companion").get(null)
            val all = companion.javaClass.getMethod("getAll").invoke(companion) as List<*>
            assertFalse(null in all)
        }
    }

    @Test
    fun aSavedStackRestoresEveryDestinationExactly() {
        Destination.all.forEach { destination ->
            assertEquals(destination, AppNavigator(listOf(destination)).savedAndRestored().current)
        }
    }

    @Test
    fun anyRejectedCodeSurvivesASavedStack() {
        val wrong = Destination.Verify(rejectedCode = "123456")
        assertEquals("verifyWrong", wrong.id)
        assertEquals(wrong, AppNavigator(listOf(wrong)).savedAndRestored().current)
    }

    @Test
    fun aRestoredStackKeepsItsHistory() {
        val navigator = AppNavigator(canonicalBackStack(Destination.Setup(3))).savedAndRestored()
        navigator.back()
        assertEquals(Destination.Setup(2), navigator.current)
        navigator.back()
        navigator.back()
        assertEquals(Destination.Verify(), navigator.current)
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
        assertEquals(Destination.Verify(), navigator.current)
    }

    @Test
    fun allSetAndTheAppDoNotGoBackIntoOnboarding() {
        val navigator = AppNavigator(canonicalBackStack(Destination.Setup(4)))
        navigator.push(Destination.AllSet)
        navigator.back()
        assertEquals(Destination.AllSet, navigator.current)
        navigator.resetTo(Destination.Main, NavTransition.Dissolve)
        // The app handles back with its own navigator; the root never pops out of it.
        assertFalse(navigator.handlesBack)
        navigator.back()
        assertEquals(Destination.Main, navigator.current)
    }
}

/** Saves and restores the navigator the way `rememberSaveable` does across process death. */
private fun AppNavigator.savedAndRestored(): AppNavigator {
    val saved = with(AppNavigator.Saver) { SaverScope { true }.save(this@savedAndRestored) }
    return AppNavigator.Saver.restore(checkNotNull(saved)) ?: error("Nothing restored")
}
