package app.paybak.paybak.navigation

/** Home's three designed states (flow.md). */
enum class HomeState(val id: String) {
    FirstDay("homeFirstDay"),
    Active("homeActive"),
    AllSettled("homeAllSettled"),
}

/**
 * Every screen in flow.md. [id] is the flow.md screen id (also the debug start-screen id), and it
 * encodes the whole destination, so the back stack can be saved as a list of ids.
 *
 * Welcome, Verify, Setup and Home are each ONE screen with a state (step, error, sheet), so moving
 * between their states is not a push: they share a [screenKey].
 */
sealed interface Destination {
    val id: String

    /** Destinations with the same key are the same screen in a different state. */
    val screenKey: String
        get() = this::class.java.simpleName

    data object Splash : Destination {
        override val id = "splash"
    }

    data class Welcome(val step: Int) : Destination {
        override val id
            get() = "welcome$step"
    }

    data object GetStarted : Destination {
        override val id = "getStarted"
    }

    data object SignIn : Destination {
        override val id = "signIn"
    }

    data class Verify(val wrongCode: Boolean) : Destination {
        override val id
            get() = if (wrongCode) "verifyWrong" else "verify"
    }

    data class Setup(val step: Int) : Destination {
        override val id
            get() = "setup$step"
    }

    data object AllSet : Destination {
        override val id = "allSet"
    }

    data class Home(val state: HomeState, val addSheetOpen: Boolean = false) : Destination {
        override val id
            get() = if (addSheetOpen) "homeAddSheet" else state.id
    }

    companion object {
        const val WELCOME_STEPS = 3
        const val SETUP_STEPS = 4

        /** Every flow.md screen id, in flow order. */
        val all: List<Destination> = buildList {
            add(Splash)
            (1..WELCOME_STEPS).forEach { add(Welcome(it)) }
            add(GetStarted)
            add(SignIn)
            add(Verify(wrongCode = false))
            add(Verify(wrongCode = true))
            (1..SETUP_STEPS).forEach { add(Setup(it)) }
            add(AllSet)
            add(Home(HomeState.FirstDay))
            add(Home(HomeState.Active))
            add(Home(HomeState.AllSettled))
            // The Add sheet is drawn over Home — Active in Figma.
            add(Home(HomeState.Active, addSheetOpen = true))
        }

        fun fromId(id: String): Destination? = all.firstOrNull { it.id == id }
    }
}

/**
 * The back stack a user would have on reaching [destination] (via email sign-in for the setup
 * steps), so back works when the app starts mid-flow.
 */
fun canonicalBackStack(destination: Destination): List<Destination> {
    val toGetStarted =
        listOf(Destination.Welcome(Destination.WELCOME_STEPS), Destination.GetStarted)
    val toVerify = toGetStarted + Destination.SignIn + Destination.Verify(wrongCode = false)
    return when (destination) {
        Destination.Splash,
        is Destination.Welcome,
        Destination.AllSet,
        is Destination.Home -> listOf(destination)
        Destination.GetStarted -> toGetStarted
        Destination.SignIn -> toGetStarted + Destination.SignIn
        is Destination.Verify -> toGetStarted + Destination.SignIn + destination
        is Destination.Setup -> toVerify + destination
    }
}
