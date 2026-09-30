package app.paybak.paybak.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.paybak.paybak.R
import app.paybak.paybak.data.ledger.actions.setRemindersMuted
import app.paybak.paybak.data.ledger.collectSnapshot
import app.paybak.paybak.domain.model.Person
import app.paybak.paybak.navigation.LocalLedger
import app.paybak.paybak.navigation.Route
import app.paybak.paybak.ui.components.PbAvatar
import app.paybak.paybak.ui.components.PbAvatarSize
import app.paybak.paybak.ui.components.PbCard
import app.paybak.paybak.ui.components.PbDivider
import app.paybak.paybak.ui.components.PbSwitch
import app.paybak.paybak.ui.components.avatarContent
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles

/**
 * The `mutedFriends` route (screens-settings §7, proposal): the friends muted from automatic
 * reminders on their pages. Switching one off unmutes them; the row stays until the screen closes,
 * so a mistaken tap can be undone.
 */
@Composable
fun MutedFriendsScreen(route: Route.MutedFriends) {
    val ledger = LocalLedger.current
    val people = ledger.collectSnapshot().value.ledger.people
    val shown = rememberSaveable { people.filter { it.remindersMuted }.map { it.id } }
    val rows = shown.mapNotNull { id -> people.firstOrNull { it.id == id } }

    SettingsPage(route, stringResource(R.string.settings_muted)) {
        if (rows.isEmpty()) {
            SettingsFootnote(stringResource(R.string.settings_muted_empty))
        } else {
            SettingsSection(title = null, footer = stringResource(R.string.settings_muted_detail)) {
                PbCard {
                    rows.forEach { person ->
                        MutedFriendRow(
                            person,
                            onCheckedChange = { ledger.setRemindersMuted(person.id, it) },
                            showDivider = person != rows.last(),
                        )
                    }
                }
            }
        }
    }
}

/** A friend's avatar and name with the switch; On = muted. */
@Composable
private fun MutedFriendRow(
    person: Person,
    onCheckedChange: (Boolean) -> Unit,
    showDivider: Boolean,
) {
    Row(
        Modifier.fillMaxWidth()
            .heightIn(min = 56.dp)
            .toggleable(
                value = person.remindersMuted,
                interactionSource = null,
                indication = null,
                role = Role.Switch,
                onValueChange = onCheckedChange,
            )
            .testTag("mutedFriends.row.${person.id}")
            .padding(horizontal = PbSpace.S16, vertical = PbSpace.S12),
        horizontalArrangement = Arrangement.spacedBy(PbSpace.S12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PbAvatar(person.avatarContent(), size = PbAvatarSize.Sm, onCard = true)
        Text(
            person.name,
            Modifier.weight(1f),
            style = PbTextStyles.Headline,
            color = PbColors.Text.Primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        PbSwitch(person.remindersMuted, onCheckedChange = null)
    }
    if (showDivider) PbDivider(Modifier.padding(start = 60.dp))
}
