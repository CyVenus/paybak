package app.paybak.paybak.data

/** How the user signed in (Get Started or the email/phone flow). */
enum class SignInMethod {
    Apple,
    Google,
    Email,
    Phone,
}

/** The user's answer on Setup 4. */
enum class NotificationsChoice {
    /** Not asked yet, or skipped with "Not now" / Skip. */
    Undecided,
    Granted,
    Denied,
}

/** The avatar picked on Setup 1. With [None] the app shows initials. */
sealed interface AvatarChoice {
    data object None : AvatarChoice

    /** Index into `PbPeepHead.Presets` (0 = Arjun … 4 = Dev). */
    data class Preset(val index: Int) : AvatarChoice

    /** A photo saved with the profile; see [ProfileStore.photoFile]. */
    data class Photo(val fileName: String) : AvatarChoice
}

/** Everything onboarding saves locally (flow.md "Persistence"). */
data class UserProfile(
    val name: String = "",
    val avatar: AvatarChoice = AvatarChoice.None,
    val currencyCode: String? = null,
    val upiId: String = "",
    val notifications: NotificationsChoice = NotificationsChoice.Undecided,
    val signInMethod: SignInMethod? = null,
    val contact: String = "",
    val onboardingComplete: Boolean = false,
) {
    /** "Arjun" for "Arjun Mehta": used by All set and the Home greeting. */
    val firstName: String
        get() = name.trim().substringBefore(' ')

    /** "AM" for "Arjun Mehta", "A" for "Arjun": the avatar fallback. */
    val initials: String
        get() {
            val words = name.trim().split(Regex("\\s+")).filter(String::isNotEmpty)
            return listOfNotNull(words.firstOrNull(), words.drop(1).lastOrNull()).joinToString("") {
                it.first().uppercase()
            }
        }
}
