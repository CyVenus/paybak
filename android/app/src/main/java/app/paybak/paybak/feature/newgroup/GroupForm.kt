package app.paybak.paybak.feature.newgroup

import androidx.compose.runtime.saveable.Saver
import app.paybak.paybak.domain.addrecord.AmountEntry
import app.paybak.paybak.domain.addrecord.ContributionDraft
import app.paybak.paybak.domain.model.ContributionRule
import app.paybak.paybak.domain.model.GroupDraft
import app.paybak.paybak.domain.model.GroupKind
import app.paybak.paybak.domain.model.GroupType
import app.paybak.paybak.domain.model.LedgerJson
import app.paybak.paybak.domain.model.ME
import app.paybak.paybak.domain.model.ProjectInfo
import kotlinx.serialization.Serializable

/**
 * One draft behind both segments of New group (record-lend-group §6): switching keeps the name,
 * members and currency. [members] are the others; you're always the first member.
 *
 * @param shares What was typed per member (you included) for a Percent or Fixed contribution.
 */
@Serializable
data class GroupForm(
    val project: Boolean = false,
    val name: String = "",
    val type: GroupType? = null,
    val members: List<String> = emptyList(),
    val currency: String,
    val simplify: Boolean = true,
    val description: String = "",
    val cover: String? = null,
    val budget: String = "",
    val rule: ContributionRule = ContributionRule.Equal,
    val shares: Map<String, String> = emptyMap(),
) {
    /** Everyone in it: you, then the others as added. */
    val everyone: List<String>
        get() = listOf(ME) + members

    val contributionValid: Boolean
        get() = ContributionDraft.isValid(rule, shares, everyone)

    val canCreate: Boolean
        get() = name.isNotBlank() && (!project || contributionValid)

    fun toDraft(): GroupDraft =
        GroupDraft(
            name = name.trim(),
            kind = if (project) GroupKind.Project else GroupKind.Group,
            type = if (project) null else type ?: GroupType.Other,
            currency = currency,
            memberIds = everyone,
            simplifyDebts = simplify,
            project =
                if (project) {
                    ProjectInfo(
                        description = description.trim().ifEmpty { null },
                        coverPhoto = cover,
                        budget = AmountEntry.minor(budget, currency).takeIf { it > 0 },
                        contribution =
                            ContributionDraft.contribution(rule, shares, everyone, currency),
                    )
                } else null,
        )

    companion object {
        val Saver: Saver<GroupForm, String> =
            Saver(
                save = { LedgerJson.encodeToString(serializer(), it) },
                restore = { LedgerJson.decodeFromString(serializer(), it) },
            )
    }
}
