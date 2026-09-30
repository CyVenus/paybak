package app.paybak.paybak.domain.actions

import app.paybak.paybak.domain.calc.LedgerView
import app.paybak.paybak.domain.model.Ledger
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * What every action needs besides its arguments: the moment it happens ([at], from `AppClock` or a
 * scenario step), the device zone and the profile's default currency.
 */
data class ActionContext(val at: Instant, val zone: ZoneId, val defaultCurrency: String) {
    val today: LocalDate
        get() = at.atZone(zone).toLocalDate()

    fun view(ledger: Ledger) = LedgerView(ledger, defaultCurrency, at, zone)
}

/** An action refused by a ledger rule; [message] is user-facing copy. */
class LedgerRuleException(message: String) : IllegalStateException(message)

internal fun ensure(condition: Boolean, message: () -> String) {
    if (!condition) throw LedgerRuleException(message())
}
