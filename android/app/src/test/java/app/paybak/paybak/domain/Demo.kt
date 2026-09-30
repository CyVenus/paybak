package app.paybak.paybak.domain

import app.paybak.paybak.domain.calc.LedgerView
import app.paybak.paybak.domain.seed.DemoSeed
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

/** The bundled demo, loaded like verify.py's `load(*scenarios, now=, anchor=)`. */
object Demo {
    val zone: ZoneId = ZoneId.of("Asia/Kolkata")
    val figmaDay: LocalDate = LocalDate.of(2026, 9, 30)
    val figmaNow: LocalDateTime = LocalDateTime.of(2026, 9, 30, 21, 15)

    val seed: DemoSeed by lazy {
        DemoSeed(Demo::class.java.classLoader!!.getResource("seed/demo.json")!!.readText())
    }

    fun load(
        vararg scenarios: String,
        now: LocalDateTime = figmaNow,
        anchor: LocalDate = figmaDay,
    ): LedgerView {
        val loaded = seed.load(anchor, now.atZone(zone).toInstant(), zone, scenarios.toList())
        return LedgerView(loaded.ledger, loaded.profile.currencyCode, loaded.now, zone)
    }
}

/** Rupees to paise. */
fun rupee(amount: Number): Long = Math.round(amount.toDouble() * 100)
