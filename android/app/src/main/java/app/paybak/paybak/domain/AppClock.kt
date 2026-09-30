package app.paybak.paybak.domain

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * The app's only source of "now" (app-architecture §3.8): real by default, or pinned to a fixed
 * moment by the debug hooks and scenarios (the Figma parity clock, Wed 30 Sep 2026 21:15). Nothing
 * else reads the system clock.
 */
class AppClock(private val zoneProvider: () -> ZoneId = ZoneId::systemDefault) {
    private val pinnedState = MutableStateFlow<Instant?>(null)

    /** The pinned moment, or null for real time. */
    val pinned: StateFlow<Instant?> = pinnedState.asStateFlow()

    val zone: ZoneId
        get() = zoneProvider()

    fun now(): Instant = pinnedState.value ?: Instant.now()

    fun today(): LocalDate = now().atZone(zone).toLocalDate()

    /** Pins the clock at [moment]; null goes back to real time. */
    fun pin(moment: Instant?) {
        pinnedState.value = moment
    }
}
