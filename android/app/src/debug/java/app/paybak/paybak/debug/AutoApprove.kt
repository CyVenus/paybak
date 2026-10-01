package app.paybak.paybak.debug

import android.content.Context
import androidx.core.content.edit
import app.paybak.paybak.PaybakApplication
import app.paybak.paybak.data.ledger.LedgerRepository
import app.paybak.paybak.data.ledger.actions.confirmPayment
import app.paybak.paybak.domain.model.PaymentStatus
import app.paybak.paybak.domain.settle.PaymentApprovals
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * The friend's side while there's no backend: 5 s after you record a payment to a friend, they
 * confirm it, so the payment-approved scene plays over whatever screen you've moved to. Skipped
 * when the payment is no longer pending, or when it's turned off (debug menu, Friend's side; UI
 * tests launch with `--ez autoApprove false`). Release builds have a copy that does nothing.
 */
object AutoApprove {
    private const val PREFS = "debug"
    private const val KEY_ON = "auto_approve"
    private const val DELAY_MILLIS = 5_000L

    /**
     * The last `autoApprove` launch extra (DebugLaunch), over the saved setting until the process
     * ends, so a relaunch without it (the debug menu's) keeps it.
     */
    @Volatile internal var launchOverride: Boolean? = null

    /** On unless turned off in the debug menu. */
    fun isOn(app: PaybakApplication): Boolean =
        launchOverride
            ?: app.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_ON, true)

    /** Saves the setting, which then also wins over this launch's extra. */
    fun setOn(app: PaybakApplication, on: Boolean) {
        app.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit { putBoolean(KEY_ON, on) }
        launchOverride = null
    }

    /** Watches [ledger] for payments you record, on [scope] (the app's). */
    fun install(app: PaybakApplication, ledger: LedgerRepository, scope: CoroutineScope) {
        scope.launch {
            ledger.changes.collect { change ->
                PaymentApprovals.recorded(change.before, change.after).forEach { payment ->
                    scope.launch {
                        delay(DELAY_MILLIS)
                        val current = ledger.ledger.value.payment(payment.id)
                        if (isOn(app) && current?.status == PaymentStatus.Pending) {
                            ledger.confirmPayment(payment.id)
                        }
                    }
                }
            }
        }
    }
}
