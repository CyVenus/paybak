package app.paybak.paybak.domain.model

import java.time.Instant
import java.time.LocalDate
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json

/** A calendar day in the device calendar, stored as `yyyy-MM-dd` (domain.md §0). */
typealias Day = @Serializable(with = DaySerializer::class) LocalDate

/** A moment in time, stored as ISO-8601 UTC and shown in the device time zone (domain.md §0). */
typealias Moment = @Serializable(with = MomentSerializer::class) Instant

object DaySerializer : KSerializer<LocalDate> {
    override val descriptor = PrimitiveSerialDescriptor("Day", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: LocalDate) =
        encoder.encodeString(value.toString())

    override fun deserialize(decoder: Decoder): LocalDate = LocalDate.parse(decoder.decodeString())
}

object MomentSerializer : KSerializer<Instant> {
    override val descriptor = PrimitiveSerialDescriptor("Moment", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Instant) =
        encoder.encodeString(value.toString())

    override fun deserialize(decoder: Decoder): Instant = Instant.parse(decoder.decodeString())
}

/**
 * The JSON shape of `ledger.json` and `demo.json`, identical to the iOS Codable shape: unknown keys
 * are ignored and missing ones take their defaults, so lanes can add optional fields safely
 * (app-architecture §3.7). Nulls are left out when encoding, as Swift's `encodeIfPresent` does.
 */
val LedgerJson = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
    encodeDefaults = true
    coerceInputValues = true
}
