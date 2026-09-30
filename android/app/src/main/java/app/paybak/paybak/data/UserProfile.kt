package app.paybak.paybak.data

import app.paybak.paybak.domain.model.AvatarLook
import app.paybak.paybak.domain.model.Pronoun
import app.paybak.paybak.domain.model.SavedMethodKind
import app.paybak.paybak.domain.model.SavedPaymentMethod

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

/**
 * The user's avatar (screens-profile §1.7): picked on Setup 1, or a custom character saved from
 * Edit avatar. With [None] the app shows initials.
 */
sealed interface AvatarChoice {
    data object None : AvatarChoice

    /** Index into `PbPeepHead.Presets` (0 = Arjun … 4 = Dev). */
    data class Preset(val index: Int) : AvatarChoice

    /** A photo saved with the profile; see [ProfileStore.photoFile]. */
    data class Photo(val fileName: String) : AvatarChoice

    /** The character from Edit avatar. */
    data class Character(val look: AvatarLook) : AvatarChoice
}

/**
 * The user's identity (flow.md "Persistence", domain.md §1.1): what onboarding saves, plus the
 * default currency ([currencyCode]), username, pronoun and payment methods. [upiId] mirrors the
 * primary UPI method (see [ProfileStore.update]).
 */
data class UserProfile(
    val name: String = "",
    val avatar: AvatarChoice = AvatarChoice.None,
    val currencyCode: String? = null,
    val upiId: String = "",
    val notifications: NotificationsChoice = NotificationsChoice.Undecided,
    val signInMethod: SignInMethod? = null,
    val contact: String = "",
    val onboardingComplete: Boolean = false,
    val username: String = "",
    val pronoun: Pronoun = Pronoun.They,
    val paymentMethods: List<SavedPaymentMethod> = emptyList(),
    val showPaymentToFriends: Boolean = true,
) {
    /** The currency for totals, new groups and new expenses. */
    val defaultCurrency: String
        get() = currencyCode ?: DEFAULT_CURRENCY

    /** The primary method, the one friends see. */
    val primaryMethod: SavedPaymentMethod?
        get() = paymentMethods.firstOrNull { it.primary }

    /** Friends' invite link: `https://paybak.app/i/{username}`. */
    val inviteLink: String
        get() = "https://paybak.app/i/$username"

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

    /**
     * Keeps the derived fields consistent after [previous] changed into this: a new UPI ID on Setup
     * 3 updates the primary UPI method, a new primary method updates [upiId], and a named user
     * without a username gets their lowercase first name.
     */
    fun normalized(previous: UserProfile = UserProfile()): UserProfile {
        var next =
            when {
                upiId != previous.upiId ->
                    copy(paymentMethods = syncedMethods(paymentMethods, upiId))
                // Migration from M1: a saved UPI ID with no methods becomes the primary method.
                paymentMethods.isEmpty() && upiId.isNotEmpty() ->
                    copy(paymentMethods = syncedMethods(emptyList(), upiId))
                paymentMethods != previous.paymentMethods ->
                    copy(
                        upiId =
                            paymentMethods
                                .firstOrNull { it.primary && it.kind == SavedMethodKind.Upi }
                                ?.value
                                .orEmpty()
                    )
                else -> this
            }
        if (next.username.isEmpty() && next.firstName.isNotEmpty()) {
            next = next.copy(username = next.firstName.lowercase().filter(Char::isLetterOrDigit))
        }
        return next
    }

    companion object {
        const val DEFAULT_CURRENCY = "INR"
        private const val SETUP_UPI_ID = "pm-upi"

        /** [methods] with the primary UPI method set to [upi] (added, or removed when blank). */
        private fun syncedMethods(
            methods: List<SavedPaymentMethod>,
            upi: String,
        ): List<SavedPaymentMethod> {
            val primaryUpi = methods.firstOrNull { it.primary && it.kind == SavedMethodKind.Upi }
            return when {
                upi.isEmpty() -> methods.filter { it != primaryUpi }
                primaryUpi != null ->
                    methods.map { if (it == primaryUpi) it.copy(value = upi) else it }
                else ->
                    listOf(
                        SavedPaymentMethod(
                            SETUP_UPI_ID,
                            SavedMethodKind.Upi,
                            value = upi,
                            primary = true,
                        )
                    ) + methods.map { it.copy(primary = false) }
            }
        }
    }
}
