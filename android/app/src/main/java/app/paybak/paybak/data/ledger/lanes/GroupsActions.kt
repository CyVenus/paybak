package app.paybak.paybak.data.ledger.lanes

import app.paybak.paybak.data.ledger.LedgerRepository
import app.paybak.paybak.domain.model.replacing

/** Lane B's store actions for Groups & Friends (app-architecture §3.4, §7.1). */

/**
 * A guest joined Paybak with the phone or email you know them by (screens-groups §2.8, simulated
 * from the debug menu): they keep their history and lose the Guest tag, with the @username of
 * their first name.
 */
fun LedgerRepository.guestJoined(personId: String) = mutate { ledger ->
    ledger.copy(
        people =
            ledger.people.replacing({ it.id == personId && it.isGuest }) {
                it.copy(isGuest = false, username = it.username ?: it.firstName.lowercase())
            }
    )
}
