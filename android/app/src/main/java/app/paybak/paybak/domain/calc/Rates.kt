package app.paybak.paybak.domain.calc

import app.paybak.paybak.domain.model.LedgerJson
import app.paybak.paybak.domain.model.Rate
import java.math.BigDecimal
import java.math.RoundingMode
import kotlinx.serialization.Serializable

/**
 * The bundled "today's rates" (`rates.json`, INR per unit) for new foreign-currency records; the
 * rate is then saved on the record and never recomputed (domain.md §1.13).
 */
class Rates(private val inrPerUnit: Map<String, BigDecimal>) {
    /** Units of [to] per one [from]: inr[from] ÷ inr[to]. Null for an unknown currency. */
    fun rate(from: String, to: String): Rate? {
        val a = inrPerUnit[from] ?: return null
        val b = inrPerUnit[to] ?: return null
        val value = a.divide(b, RATE_SCALE, RoundingMode.HALF_UP).stripTrailingZeros()
        return Rate(value.setScale(maxOf(2, value.scale())).toPlainString(), to)
    }

    companion object {
        private const val RATE_SCALE = 4

        fun parse(json: String): Rates {
            val file = LedgerJson.decodeFromString(RatesFile.serializer(), json)
            return Rates(file.inrPerUnit.mapValues { BigDecimal(it.value) })
        }
    }

    @Serializable private data class RatesFile(val inrPerUnit: Map<String, String>)
}
