package app.paybak.paybak.debug

import app.paybak.paybak.PaybakApplication
import app.paybak.paybak.data.ledger.LedgerRepository
import kotlinx.coroutines.CoroutineScope

/** Release builds have no stand-in friends: a payment waits for the friend's own confirm. */
object AutoApprove {
    fun install(app: PaybakApplication, ledger: LedgerRepository, scope: CoroutineScope) = Unit
}
