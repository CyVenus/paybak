package app.paybak.paybak.feature.friends

import app.paybak.paybak.data.ledger.LedgerRepository
import app.paybak.paybak.data.ledger.actions.addFriend
import app.paybak.paybak.navigation.MainNavigator
import app.paybak.paybak.navigation.Route
import app.paybak.paybak.service.contacts.Directory
import app.paybak.paybak.service.qr.QrCode

/**
 * A scanned code (screens-groups §7.3): a Paybak invite link adds its owner as a friend (if they
 * aren't one yet) and opens their page. Returns false for anything else, which the caller reports.
 */
fun addFriendFromCode(
    text: String,
    directory: Directory,
    ledger: LedgerRepository,
    navigator: MainNavigator,
): Boolean {
    val person = QrCode.usernameFrom(text)?.let(directory::byUsername) ?: return false
    if (!directory.isFriend(person)) ledger.addFriend(person.copy(addedAt = ledger.clock.now()))
    navigator.open(Route.Friend(person.id))
    return true
}
