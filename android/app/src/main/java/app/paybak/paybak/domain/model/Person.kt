package app.paybak.paybak.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class Pronoun(val subject: String) {
    @SerialName("she") She("she"),
    @SerialName("he") He("he"),
    @SerialName("they") They("they"),
}

/** A friend or a guest (domain.md §1.2). The user is not a Person; they are [ME]. */
@Serializable
data class Person(
    val id: String,
    val name: String,
    /** Peep-head asset key `avatar-2` … `avatar-7`; null = initials. */
    val avatar: String? = null,
    val upi: String? = null,
    val username: String? = null,
    val pronoun: Pronoun = Pronoun.They,
    val isGuest: Boolean = false,
    val contact: String? = null,
    val remindersMuted: Boolean = false,
    val addedAt: Moment,
) {
    /** "Rohan" for "Rohan Verma". */
    val firstName: String
        get() = name.trim().substringBefore(' ')

    /** "RV" for "Rohan Verma" (first and last word). */
    val initials: String
        get() {
            val words = name.trim().split(Regex("\\s+")).filter(String::isNotEmpty)
            return listOfNotNull(words.firstOrNull(), words.drop(1).lastOrNull()).joinToString("") {
                it.first().uppercase()
            }
        }
}
