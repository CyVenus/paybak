package app.paybak.paybak.service.notifications

import android.content.BroadcastReceiver
import android.content.Context
import app.paybak.paybak.PaybakApplication
import app.paybak.paybak.data.ledger.LedgerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Runs [work] for a broadcast: the ledger is read off the main thread (the process may have just
 * started for this broadcast), [work] runs on the main thread like every ledger change, and the
 * broadcast stays alive until it's done.
 */
internal fun BroadcastReceiver.withLedger(
    context: Context,
    work: (PaybakApplication, LedgerRepository) -> Unit,
) {
    val app = context.applicationContext as PaybakApplication
    val pending = goAsync()
    CoroutineScope(Dispatchers.Main).launch {
        try {
            val ledger = withContext(Dispatchers.Default) { app.ledger }
            work(app, ledger)
        } finally {
            pending.finish()
        }
    }
}

/**
 * Catches up with the scheduler (domain.md §10): `tick` creates what's due by now, and only that is
 * posted, then the next alarm is set.
 */
internal fun BroadcastReceiver.tickAndNotify(context: Context) =
    withLedger(context) { app, ledger ->
        app.notifications.sync(ledger.snapshot.value)
        ledger.tick()
        app.notifications.sync(ledger.snapshot.value)
    }
