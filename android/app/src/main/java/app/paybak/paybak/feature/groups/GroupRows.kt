package app.paybak.paybak.feature.groups

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import app.paybak.paybak.domain.calc.LedgerView
import app.paybak.paybak.domain.groups.GroupListRow
import app.paybak.paybak.domain.groups.Standing
import app.paybak.paybak.domain.model.Group
import app.paybak.paybak.domain.model.ME
import app.paybak.paybak.navigation.LocalProfileStore
import app.paybak.paybak.navigation.Route
import app.paybak.paybak.ui.components.PbAvatarContent
import app.paybak.paybak.ui.components.PbBalance
import app.paybak.paybak.ui.components.PbGroupBalance
import app.paybak.paybak.ui.components.PbGroupBudget
import app.paybak.paybak.ui.components.PbGroupRow
import app.paybak.paybak.ui.components.avatarContent
import app.paybak.paybak.ui.components.iconForKey
import app.paybak.paybak.ui.components.rememberUserAvatar

/** A Groups list or "Groups together" row; tagged `[tagPrefix].<groupId>`. */
@Composable
internal fun GroupRowItem(
    row: GroupListRow,
    tagPrefix: String,
    showDivider: Boolean,
    onClick: () -> Unit,
) {
    PbGroupRow(
        name = row.group.name,
        subtitle = row.subtitle,
        icon = iconForKey(row.group.icon),
        balance =
            when (row.standing) {
                Standing.Owe -> PbGroupBalance.Owe(row.amount.orEmpty())
                Standing.Owed -> PbGroupBalance.Owed(row.amount.orEmpty())
                Standing.Settled -> PbGroupBalance.Settled(row.status.orEmpty())
            },
        modifier = Modifier.testTag("$tagPrefix.${row.group.id}"),
        budget = row.budget?.let { PbGroupBudget(it.progress, it.spent, it.left, it.over) },
        archived = row.archived,
        showDivider = showDivider,
        onClick = onClick,
    )
}

/** Where a group row leads: a project's dashboard or the group's detail. */
internal fun Group.route(): Route = if (isProject) Route.Project(id) else Route.Group(id)

internal fun Standing.balance(): PbBalance? =
    when (this) {
        Standing.Owe -> PbBalance.Owe
        Standing.Owed -> PbBalance.Owed
        Standing.Settled -> null
    }

/** A member's avatar: the user's own (photo, character or preset), or the friend's head. */
@Composable
internal fun rememberAvatars(view: LedgerView, personIds: List<String>): List<PbAvatarContent> {
    val store = LocalProfileStore.current
    val profile by store.profile.collectAsState()
    val mine = rememberUserAvatar(profile, store)
    return personIds.map { id ->
        if (id == ME) mine
        else view.person(id)?.avatarContent() ?: PbAvatarContent.Initials(view.first(id).take(1))
    }
}
