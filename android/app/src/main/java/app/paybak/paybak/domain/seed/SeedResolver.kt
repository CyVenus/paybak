package app.paybak.paybak.domain.seed

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/**
 * Resolves the seed's relative values (domain.md §7.1): `"D-9"` is the load day − 9 and
 * `"D-9T15:30"` that day at 15:30 local. Moments later than [now] clamp to it (they already
 * happened); days never clamp. A port of verify.py `resolve`.
 */
object SeedResolver {
    private val relative = Regex("""^D([+-]?\d+)(?:T(\d\d):(\d\d))?$""")

    fun resolve(value: JsonElement, anchor: LocalDate, now: Instant?, zone: ZoneId): JsonElement =
        when (value) {
            is JsonObject -> JsonObject(value.mapValues { resolve(it.value, anchor, now, zone) })
            is JsonArray -> JsonArray(value.map { resolve(it, anchor, now, zone) })
            is JsonPrimitive -> resolvePrimitive(value, anchor, now, zone)
        }

    private fun resolvePrimitive(
        value: JsonPrimitive,
        anchor: LocalDate,
        now: Instant?,
        zone: ZoneId,
    ): JsonElement {
        if (!value.isString) return value
        val match = relative.matchEntire(value.content) ?: return value
        val day = anchor.plusDays(match.groupValues[1].toLong())
        if (match.groupValues[2].isEmpty()) return JsonPrimitive(day.toString())
        val time = LocalTime.of(match.groupValues[2].toInt(), match.groupValues[3].toInt())
        val moment = day.atTime(time).atZone(zone).toInstant()
        return JsonPrimitive((if (now != null && moment > now) now else moment).toString())
    }
}
