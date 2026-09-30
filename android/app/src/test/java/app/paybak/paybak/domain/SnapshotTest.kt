package app.paybak.paybak.domain

import app.paybak.paybak.domain.model.Ledger
import app.paybak.paybak.domain.model.LedgerJson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** The snapshot's cost and the ledger's JSON round trip (app-architecture §3.9). */
class SnapshotTest {
    private val demo = Demo.load("eshaClaimsPayment")

    @Test
    fun theSnapshotIsQuickToRecompute() {
        repeat(WARM_UP) { LedgerSnapshot.of(demo.ledger, "INR", demo.now, Demo.zone) }
        val start = System.nanoTime()
        repeat(RUNS) { LedgerSnapshot.of(demo.ledger, "INR", demo.now, Demo.zone) }
        val millis = (System.nanoTime() - start) / 1e6 / RUNS
        println("Snapshot of the demo: %.2f ms".format(millis))
        // ~5 ms on a release-like build; the JVM test build gets headroom.
        assertTrue("snapshot took $millis ms", millis < 50)
    }

    @Test
    fun theLedgerRoundTripsThroughJson() {
        val json = LedgerJson.encodeToString(Ledger.serializer(), demo.ledger)
        assertEquals(demo.ledger, LedgerJson.decodeFromString(Ledger.serializer(), json))
    }

    @Test
    fun theSnapshotMatchesTheViewItCameFrom() {
        val snapshot = LedgerSnapshot.of(demo.ledger, "INR", demo.now, Demo.zone)
        assertEquals(1, snapshot.home.pendingClaims.size)
        assertEquals(2, snapshot.unreadCount)
        assertEquals("Today", snapshot.timeline.first().header)
        assertEquals(
            listOf("Kabir", "Meera"),
            snapshot.oweBreakdown.map { snapshot.view.first(it.friendId) },
        )
    }

    private companion object {
        const val WARM_UP = 20
        const val RUNS = 50
    }
}
