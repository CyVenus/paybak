package app.paybak.paybak.feature.scan

import androidx.compose.runtime.saveable.Saver
import app.paybak.paybak.domain.model.LedgerJson
import app.paybak.paybak.domain.model.ME
import app.paybak.paybak.domain.model.ReceiptScan
import app.paybak.paybak.domain.model.ScanItem
import kotlinx.serialization.Serializable

/** The scan modal's pages, in push order. */
@Serializable
enum class ScanStep {
    Camera,
    Review,
    Assign,
}

/**
 * Where the scan flow is: the saved [photo] (a file in the photos folder; [sample] when it is the
 * simulated camera's receipt), what was read from it ([scan]; null with a photo = nothing
 * readable), who had each item ([assigned], one list per item) and the people on the expense (you
 * first).
 */
@Serializable
data class ScanState(
    val people: List<String>,
    val step: ScanStep = ScanStep.Camera,
    val photo: String? = null,
    val sample: Boolean = false,
    val scan: ReceiptScan? = null,
    val assigned: List<List<String>> = emptyList(),
) {
    /** A new reading: everything unassigned. */
    fun read(photo: String?, sample: Boolean, scan: ReceiptScan?) =
        copy(
            step = ScanStep.Review,
            photo = photo,
            sample = sample,
            scan = scan,
            assigned = scan?.items?.map { emptyList<String>() }.orEmpty(),
        )

    fun editScan(change: (ReceiptScan) -> ReceiptScan): ScanState {
        val current = scan ?: return this
        val next = change(current)
        // Items added or removed keep the others' assignment.
        val assigned = next.items.indices.map { this.assigned.getOrNull(it).orEmpty() }
        return copy(scan = next, assigned = assigned)
    }

    fun editItem(index: Int, change: (ScanItem) -> ScanItem) = editScan { scan ->
        scan.copy(
            items = scan.items.mapIndexed { i, item -> if (i == index) change(item) else item }
        )
    }

    /** Ticks or unticks [personId] on item [index]; the list keeps the expense's people order. */
    fun toggle(index: Int, personId: String): ScanState {
        val current = assigned.getOrNull(index) ?: return this
        val next = if (personId in current) current - personId else current + personId
        return copy(
            assigned =
                assigned.mapIndexed { i, who ->
                    if (i == index) people.filter { it in next } else who
                }
        )
    }

    /** You and [others] are on the expense now (Assign items' "Add"); items lose anyone dropped. */
    fun withPeople(others: List<String>): ScanState {
        val next = listOf(ME) + others.filter { it != ME }.distinct()
        return copy(people = next, assigned = assigned.map { who -> who.filter { it in next } })
    }

    companion object {
        val Saver: Saver<ScanState, String> =
            Saver(
                save = { LedgerJson.encodeToString(serializer(), it) },
                restore = { LedgerJson.decodeFromString(serializer(), it) },
            )
    }
}
