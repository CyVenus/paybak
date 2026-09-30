package app.paybak.paybak.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

/** How the next screen arrives (the Figma prototype transitions). */
enum class NavTransition {
    /** PUSH LEFT, 350 ms ease-in-out. */
    Push,

    /** The reverse of [Push]. */
    Pop,

    /** 400 ms ease-out dissolve: Splash → next, All set → Home. */
    Dissolve,

    /** 300 ms crossfade: Skip → Get Started. */
    Crossfade,
}

/**
 * The app's back stack. It is saved as [Destination.savedKey]s, so it survives recreation and
 * process death. Moving between states of one screen (a Welcome or Setup step, the Add sheet)
 * replaces the top entry and the screen animates the change itself.
 */
@Stable
class AppNavigator(initialStack: List<Destination>) {
    private val stack = mutableStateListOf<Destination>().apply { addAll(initialStack) }

    val current: Destination
        get() = stack.last()

    var lastTransition by mutableStateOf(NavTransition.Push)
        private set

    fun push(destination: Destination, transition: NavTransition = NavTransition.Push) {
        lastTransition = transition
        stack.add(destination)
    }

    /** Swaps the top entry for another state of the same screen. */
    fun replace(destination: Destination) {
        stack[stack.lastIndex] = destination
    }

    /** Clears the stack: nothing before [destination] is reachable with back any more. */
    fun resetTo(destination: Destination, transition: NavTransition) {
        lastTransition = transition
        stack.clear()
        stack.add(destination)
    }

    /**
     * Runs [navigation] only while [screen] is on top. A screen that is transitioning out stays
     * composed, and tappable, until the transition ends, so a quick second tap on it must not
     * navigate again (or, after a [replace], change the wrong screen).
     */
    fun from(screen: Destination, navigation: AppNavigator.() -> Unit) {
        if (current == screen) navigation()
    }

    /** False where system back should leave the app: Welcome step 1, Home, or an empty stack. */
    val handlesBack: Boolean
        get() =
            when (val top = current) {
                is Destination.Welcome -> top.step > 1 || stack.size > 1
                is Destination.Home -> top.addSheetOpen
                Destination.AllSet -> true
                else -> stack.size > 1
            }

    /** System back and the in-app back chevrons (flow.md "Resolved decisions"). */
    fun back() {
        when (val top = current) {
            is Destination.Welcome if top.step > 1 -> replace(top.copy(step = top.step - 1))
            is Destination.Setup if top.step > 1 -> replace(top.copy(step = top.step - 1))
            is Destination.Home if top.addSheetOpen -> replace(top.copy(addSheetOpen = false))
            // All set can't go back into onboarding.
            Destination.AllSet -> Unit
            else ->
                if (stack.size > 1) {
                    lastTransition = NavTransition.Pop
                    stack.removeAt(stack.lastIndex)
                }
        }
    }

    companion object {
        val Saver =
            listSaver<AppNavigator, String>(
                save = { navigator -> navigator.stack.map(Destination::savedKey) },
                restore = { keys ->
                    AppNavigator(
                        keys.mapNotNull(Destination::fromSavedKey).ifEmpty {
                            listOf(Destination.Splash)
                        }
                    )
                },
            )
    }
}

@Composable
fun rememberAppNavigator(initialStack: List<Destination>): AppNavigator =
    rememberSaveable(saver = AppNavigator.Saver) { AppNavigator(initialStack) }
