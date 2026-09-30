package app.paybak.paybak.navigation

/**
 * The app's roots: splash, the onboarding screens of flow.md and [Main] (the tab shell with every
 * app screen, navigated by [MainNavigator]). [id] is the flow.md screen id (also the debug
 * start-screen id), and [savedKey] encodes the whole destination, so the back stack can be saved as
 * a list of keys.
 *
 * Welcome, Verify and Setup are each ONE screen with a state (step, error), so moving between their
 * states is not a push: they share a [screenKey].
 */
sealed interface Destination {
    /** The flow.md screen id. */
    val id: String

    /** Encodes the whole destination for a saved back stack; [fromSavedKey] decodes it. */
    val savedKey: String
        get() = id

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

    /** The code screen; in its error state it shows the [rejectedCode]. */
    data class Verify(val rejectedCode: String? = null) : Destination {
        override val id
            get() = if (rejectedCode == null) "verify" else WRONG_ID

        override val savedKey
            get() = rejectedCode?.let { "$WRONG_ID:$it" } ?: id

        companion object {
            private const val WRONG_ID = "verifyWrong"

            /** The wrong code in Figma's "Sign in — Wrong code" frame. */
            const val FIGMA_WRONG_CODE = "482917"

            fun fromSavedKey(key: String): Verify? =
                key.removePrefix("$WRONG_ID:").takeIf { it != key }?.let(::Verify)
        }
    }

    data class Setup(val step: Int) : Destination {
        override val id
            get() = "setup$step"
    }

    data object AllSet : Destination {
        override val id = "allSet"
    }

    /** The app after onboarding: the tab shell and everything above it. */
    data object Main : Destination {
        override val id = "main"
    }

    companion object {
        const val WELCOME_STEPS = 3
        const val SETUP_STEPS = 4

        /** Every root, in flow order. */
        val all: List<Destination> = buildList {
            add(Splash)
            (1..WELCOME_STEPS).forEach { add(Welcome(it)) }
            add(GetStarted)
            add(SignIn)
            add(Verify())
            add(Verify(rejectedCode = Verify.FIGMA_WRONG_CODE))
            (1..SETUP_STEPS).forEach { add(Setup(it)) }
            add(AllSet)
            add(Main)
        }

        fun fromId(id: String): Destination? = all.firstOrNull { it.id == id }

        fun fromSavedKey(key: String): Destination? =
            all.firstOrNull { it.savedKey == key } ?: Verify.fromSavedKey(key)
    }
}

/**
 * The back stack a user would have on reaching [destination] (via email sign-in for the setup
 * steps), so back works when the app starts mid-flow.
 */
fun canonicalBackStack(destination: Destination): List<Destination> {
    val toGetStarted =
        listOf(Destination.Welcome(Destination.WELCOME_STEPS), Destination.GetStarted)
    val toVerify = toGetStarted + Destination.SignIn + Destination.Verify()
    return when (destination) {
        Destination.Splash,
        is Destination.Welcome,
        Destination.AllSet,
        Destination.Main -> listOf(destination)
        Destination.GetStarted -> toGetStarted
        Destination.SignIn -> toGetStarted + Destination.SignIn
        is Destination.Verify -> toGetStarted + Destination.SignIn + destination
        is Destination.Setup -> toVerify + destination
    }
}
