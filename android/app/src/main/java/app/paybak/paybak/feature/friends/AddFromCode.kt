package app.paybak.paybak.feature.friends

import app.paybak.paybak.data.ledger.LedgerRepository
import app.paybak.paybak.data.ledger.actions.addFriend
import app.paybak.paybak.domain.model.Person
import app.paybak.paybak.navigation.MainNavigator
import app.paybak.paybak.navigation.Route
import app.paybak.paybak.service.contacts.Directory
import app.paybak.paybak.service.qr.QrCode

/** What a scanned code names (screens-groups §7.3). */
sealed interface ScannedCode {
    /** A Paybak user: a friend already, or someone to add. */
    data class User(val person: Person) : ScannedCode

    /** Your own invite link. */
    data object Own : ScannedCode

    /** Anything that isn't a Paybak invite link. */
    data object NotPaybak : ScannedCode
}

/** Reads a scanned [text] against the directory; [ownUsername] is yours. */
fun Directory.scannedCode(text: String, ownUsername: String): ScannedCode {
    val username = QrCode.usernameFrom(text) ?: return ScannedCode.NotPaybak
    if (username.equals(ownUsername, ignoreCase = true)) return ScannedCode.Own
    return byUsername(username)?.let(ScannedCode::User) ?: ScannedCode.NotPaybak
}

/**
 * A scanned code (screens-groups §7.3): a Paybak invite link adds its owner as a friend (if they
 * aren't one yet) and opens their page. Returns what the code named, so the caller reports your own
 * code or anything else.
 */
fun addFriendFromCode(
    text: String,
    ownUsername: String,
    directory: Directory,
    ledger: LedgerRepository,
    navigator: MainNavigator,
): ScannedCode {
    val code = directory.scannedCode(text, ownUsername)
    if (code is ScannedCode.User) {
        val person = code.person
        if (!directory.isFriend(person)) ledger.addFriend(person.copy(addedAt = ledger.clock.now()))
        navigator.open(Route.Friend(person.id))
    }
    return code
}
