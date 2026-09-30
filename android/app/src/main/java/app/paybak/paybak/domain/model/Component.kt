package app.paybak.paybak.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ComponentStatus(val label: String) {
    @SerialName("planned") Planned("Planned"),
    @SerialName("bought") Bought("Bought"),
    @SerialName("done") Done("Done"),
}

@Serializable data class ComponentEvent(val kind: String, val at: Moment, val by: String)

/** A project part (domain.md §1.7, §8). Bought and done parts count at [actualCost]. */
@Serializable
data class Component(
    val id: String,
    val projectId: String,
    val name: String,
    val status: ComponentStatus = ComponentStatus.Planned,
    val estimatedCost: Long? = null,
    val actualCost: Long? = null,
    val paidBy: String = ME,
    val receipt: Receipt? = null,
    val createdAt: Moment,
    val statusChangedAt: Moment,
    val history: List<ComponentEvent> = emptyList(),
) {
    val counts: Boolean
        get() = status != ComponentStatus.Planned
}
