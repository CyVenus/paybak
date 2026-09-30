package app.paybak.paybak.feature.friends

import android.content.ClipData
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.paybak.paybak.R
import app.paybak.paybak.navigation.LocalMainNavigator
import app.paybak.paybak.navigation.LocalProfileStore
import app.paybak.paybak.service.SystemShare
import app.paybak.paybak.ui.components.PbAvatarSize
import app.paybak.paybak.ui.components.PbButton
import app.paybak.paybak.ui.components.PbIconButton
import app.paybak.paybak.ui.components.PbQrCodeCard
import app.paybak.paybak.ui.components.PbSheet
import app.paybak.paybak.ui.components.PbUserAvatar
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbShapes
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles
import kotlinx.coroutines.launch

private val LinkFieldHeight = 48.dp

/**
 * My QR code (screens-groups §7.6, `myQrCode`): your avatar, name and @handle, a scannable QR of
 * your invite link, the link with Copy (clipboard + "Link copied") and Share link.
 */
@Composable
internal fun MyQrCodeSheet(onDismiss: () -> Unit) {
    val navigator = LocalMainNavigator.current
    val context = LocalContext.current
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    val profile by LocalProfileStore.current.profile.collectAsState()
    val link = profile.inviteLink
    val copied = stringResource(R.string.groups_link_copied)
    PbSheet(
        onDismiss = onDismiss,
        title = stringResource(R.string.groups_my_qr),
        testTag = "myQr.sheet",
    ) {
        Column(
            Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(PbSpace.S16),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(PbSpace.S8),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                PbUserAvatar(size = PbAvatarSize.Lg)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(profile.name, style = PbTextStyles.Headline, color = PbColors.Text.Primary)
                    Text(
                        "@${profile.username}",
                        Modifier.testTag("myQr.handle"),
                        style = PbTextStyles.Subheadline,
                        color = PbColors.Text.Secondary,
                    )
                }
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(PbSpace.S12),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                PbQrCodeCard(link, Modifier.testTag("myQr.code"))
                Text(
                    stringResource(R.string.groups_qr_helper),
                    style = PbTextStyles.Footnote,
                    color = PbColors.Text.Secondary,
                )
            }
            Row(
                Modifier.fillMaxWidth()
                    .height(LinkFieldHeight)
                    .background(PbColors.Bg.Card, PbShapes.Input)
                    .padding(start = PbSpace.S16, end = PbSpace.S4),
                horizontalArrangement = Arrangement.spacedBy(PbSpace.S8),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    link.removePrefix("https://"),
                    Modifier.weight(1f).testTag("myQr.link"),
                    style = PbTextStyles.Body,
                    color = PbColors.Text.Primary,
                    maxLines = 1,
                    overflow = TextOverflow.MiddleEllipsis,
                )
                PbIconButton(
                    PbIcon.Copy,
                    contentDescription = stringResource(R.string.groups_copy_link),
                    onClick = {
                        scope.launch {
                            clipboard.setClipEntry(ClipEntry(ClipData.newPlainText("Paybak", link)))
                            navigator.toast(copied)
                        }
                    },
                    modifier = Modifier.testTag("myQr.copy"),
                )
            }
            PbButton(
                stringResource(R.string.groups_share_link),
                onClick = { SystemShare.shareText(context, link) },
                modifier = Modifier.fillMaxWidth().testTag("myQr.share"),
                leadingIcon = PbIcon.Share,
            )
        }
    }
}
