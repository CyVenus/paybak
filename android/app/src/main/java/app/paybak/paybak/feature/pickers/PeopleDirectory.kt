package app.paybak.paybak.feature.pickers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import app.paybak.paybak.data.ledger.collectSnapshot
import app.paybak.paybak.domain.model.Ledger
import app.paybak.paybak.domain.model.ME
import app.paybak.paybak.domain.model.Person
import app.paybak.paybak.navigation.LocalLedger
import app.paybak.paybak.navigation.LocalProfileStore
import app.paybak.paybak.ui.components.PbAvatarContent
import app.paybak.paybak.ui.components.avatarContent
import app.paybak.paybak.ui.components.rememberUserAvatar

/**
 * Names and avatars for the people on a form or detail: "You" with the user's own avatar for [ME],
 * a friend's first or full name and Peep head (initials for guests) otherwise.
 */
class PeopleDirectory(
    private val ledger: Ledger,
    private val myAvatar: PbAvatarContent,
    private val myName: String,
) {
    /** Friends and guests, in the order they were added. */
    val friends: List<Person>
        get() = ledger.people

    fun person(id: String): Person? = ledger.person(id)

    /** "You", "Priya". */
    fun first(id: String): String = if (id == ME) "You" else person(id)?.firstName ?: "Someone"

    /** "Arjun Mehta", "Priya Sharma". */
    fun full(id: String): String = if (id == ME) myName else person(id)?.name ?: "Someone"

    fun avatar(id: String): PbAvatarContent =
        if (id == ME) myAvatar else person(id)?.avatarContent() ?: PbAvatarContent.Initials("?")

    fun isGuest(id: String): Boolean = person(id)?.isGuest == true
}

@Composable
fun rememberPeopleDirectory(): PeopleDirectory {
    val store = LocalProfileStore.current
    val profile by store.profile.collectAsState()
    val snapshot by LocalLedger.current.collectSnapshot()
    val avatar = rememberUserAvatar(profile, store)
    return remember(snapshot.ledger, avatar, profile.name) {
        PeopleDirectory(snapshot.ledger, avatar, profile.name)
    }
}
