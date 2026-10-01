package app.paybak.paybak.data.ledger

import app.paybak.paybak.data.UserProfile
import app.paybak.paybak.domain.AppClock
import app.paybak.paybak.domain.LedgerSnapshot
import app.paybak.paybak.domain.actions.ActionContext
import app.paybak.paybak.domain.actions.tick
import app.paybak.paybak.domain.calc.Rates
import app.paybak.paybak.domain.model.Ledger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch

/**
 * The one owner of the [Ledger] (app-architecture §3.6). Screens read [snapshot] (every read model,
 * recomputed after each change) and change the ledger only through the actions in `actions/` and
 * the lanes' files in `lanes/`, which all go through [mutate]. Every change is saved at once.
 *
 * @param profile The profile: its currency is the default currency of every total.
 * @param storePro Whether the store's Pro entitlement is active, for [LedgerSnapshot.isPro].
 */
class LedgerRepository(
    private val file: LedgerFile,
    val clock: AppClock,
    private val profile: StateFlow<UserProfile>,
    val rates: Rates,
    private val storePro: StateFlow<Boolean> = MutableStateFlow(false),
    scope: CoroutineScope,
) {
    private val ledgerState = MutableStateFlow(file.read())
    private val snapshotState = MutableStateFlow(snapshotOf(ledgerState.value))
    private val revisionState = MutableStateFlow(0)
    private val changesFlow = MutableSharedFlow<LedgerChange>(extraBufferCapacity = CHANGE_BUFFER)

    val ledger: StateFlow<Ledger> = ledgerState.asStateFlow()
    val snapshot: StateFlow<LedgerSnapshot> = snapshotState.asStateFlow()

    /** Bumps after every change: observers such as the notification scheduler react to it. */
    val revision: StateFlow<Int> = revisionState.asStateFlow()

    /**
     * Every change [mutate] makes, with the ledger before and after (payment approvals, the debug
     * auto-approver). [replace] swaps the whole ledger and isn't reported. Nothing is replayed, so
     * collect it from the start, on a scope that lives as long as the app.
     */
    val changes: SharedFlow<LedgerChange> = changesFlow.asSharedFlow()

    val defaultCurrency: String
        get() = profile.value.defaultCurrency

    init {
        // A new default currency or a moved clock changes every total and date label; a purchase,
        // restore or expiry changes what Pro unlocks.
        scope.launch {
            merge(
                    profile.map { it.defaultCurrency }.distinctUntilChanged().drop(1),
                    clock.pinned.drop(1),
                    storePro.drop(1),
                )
                .collect { tick() }
        }
    }

    /** The context actions run in: now, the device zone and the default currency. */
    fun context(): ActionContext = ActionContext(clock.now(), clock.zone, defaultCurrency)

    /**
     * The only write path: applies [change], saves, recomputes the snapshot and bumps [revision]. A
     * change that changes nothing does nothing.
     */
    @Synchronized
    fun mutate(change: (Ledger) -> Ledger) {
        val before = ledgerState.value
        val next = change(before)
        if (next != before) {
            commit(next)
            changesFlow.tryEmit(LedgerChange(before, next))
        }
    }

    /** [mutate] for actions that also return something, such as a new record's id. */
    fun <T> mutateReturning(change: (Ledger) -> Pair<Ledger, T>): T {
        var outcome: Pair<Ledger, T>? = null
        mutate { ledger -> change(ledger).also { outcome = it }.first }
        return checkNotNull(outcome).second
    }

    /** Swaps the whole ledger: demo loads, "Start an empty account", reset. Not in [changes]. */
    @Synchronized
    fun replace(ledger: Ledger) {
        if (ledger != ledgerState.value) commit(ledger)
    }

    /**
     * Runs the scheduler up to now (launch, foreground, a moved clock, a new default currency) and
     * recomputes the read models, whose dates and totals depend on both.
     */
    @Synchronized
    fun tick() {
        val current = ledgerState.value
        val next = current.tick(context())
        if (next != current) commit(next) else snapshotState.value = snapshotOf(current)
    }

    private fun commit(next: Ledger) {
        ledgerState.value = next
        file.write(next)
        snapshotState.value = snapshotOf(next)
        revisionState.value += 1
    }

    private fun snapshotOf(ledger: Ledger) =
        LedgerSnapshot.of(ledger, defaultCurrency, clock.now(), clock.zone, storePro.value)
}

/** A change [LedgerRepository.mutate] made: the ledger [before] and [after] it. */
data class LedgerChange(val before: Ledger, val after: Ledger)

/** Changes a slow collector can fall behind by before the newest are dropped. */
private const val CHANGE_BUFFER = 64
