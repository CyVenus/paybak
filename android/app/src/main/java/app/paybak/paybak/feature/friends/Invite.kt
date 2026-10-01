package app.paybak.paybak.feature.friends

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import app.paybak.paybak.R
import app.paybak.paybak.navigation.LocalProfileStore
import app.paybak.paybak.service.SystemShare

/**
 * Opens the share sheet with your invite: "Join me on Paybak so we can split expenses:
 * https://paybak.app/i/arjun" (Invite with a link, Send invite; screens-groups §2.11, §6.7).
 */
@Composable
internal fun rememberInviteShare(): () -> Unit {
    val context = LocalContext.current
    val profile by LocalProfileStore.current.profile.collectAsState()
    val text = stringResource(R.string.groups_invite_message, profile.inviteLink)
    return { SystemShare.shareText(context, text) }
}
