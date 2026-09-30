package app.paybak.paybak.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class GroupKind {
    @SerialName("group") Group,
    @SerialName("project") Project,
}

/** A group's type; each has its icon (domain.md §1.3). */
@Serializable
enum class GroupType(val icon: String) {
    @SerialName("trip") Trip("plane"),
    @SerialName("home") Home("home"),
    @SerialName("friends") Friends("people"),
    @SerialName("other") Other("tag"),
}

/** A group or a project (domain.md §1.3). Amounts inside it are in [currency]. */
@Serializable
data class Group(
    val id: String,
    val kind: GroupKind = GroupKind.Group,
    val type: GroupType? = null,
    val icon: String = "people",
    val name: String,
    val currency: String,
    /** Display order and the simplify tie-break; includes [ME] while you're a member. */
    val memberIds: List<String>,
    val simplifyDebts: Boolean = true,
    val settleBy: Day? = null,
    val createdAt: Moment,
    val createdBy: String = ME,
    val project: ProjectInfo? = null,
) {
    val isProject: Boolean
        get() = kind == GroupKind.Project

    val isArchived: Boolean
        get() = project?.status == ProjectStatus.Archived
}

@Serializable
enum class ProjectStatus {
    @SerialName("active") Active,
    @SerialName("closed") Closed,
    @SerialName("archived") Archived,
}

@Serializable
enum class ContributionRule {
    @SerialName("equal") Equal,
    @SerialName("percent") Percent,
    @SerialName("fixed") Fixed,
}

/** How project spending is shared: basis points (percent) or minor units (fixed) per member. */
@Serializable
data class Contribution(
    val rule: ContributionRule = ContributionRule.Equal,
    val values: Map<String, Long> = emptyMap(),
)

/** The project part of a [Group] (domain.md §1.3, §8). */
@Serializable
data class ProjectInfo(
    val description: String? = null,
    val coverPhoto: String? = null,
    val budget: Long? = null,
    val contribution: Contribution = Contribution(),
    /** "Collect money upfront". */
    val pool: Boolean = false,
    val status: ProjectStatus = ProjectStatus.Active,
    val closedAt: Moment? = null,
    val archivedAt: Moment? = null,
)
