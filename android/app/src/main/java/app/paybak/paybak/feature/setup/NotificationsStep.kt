package app.paybak.paybak.feature.setup

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import app.paybak.paybak.R
import app.paybak.paybak.data.NotificationsChoice
import app.paybak.paybak.data.ProfileStore
import app.paybak.paybak.rive.PaybakRiveAsset
import app.paybak.paybak.rive.PaybakRiveIllustration
import app.paybak.paybak.ui.components.PbButton
import app.paybak.paybak.ui.components.PbScreenBody
import app.paybak.paybak.ui.components.PbTextButton
import app.paybak.paybak.ui.components.PbTextButtonStyle
import app.paybak.paybak.ui.components.PbTitleBlock
import app.paybak.paybak.ui.theme.PbLayout
import app.paybak.paybak.ui.theme.PbSpace

/**
 * `setup4`: reminders (screens-setup.md §4), optional. "Turn on notifications" asks for the OS
 * permission (Android 13+; granted at install before that), records the answer and moves on
 * whatever it is. "Not now" moves on without asking.
 */
@Composable
internal fun NotificationsStep(profileStore: ProfileStore, onDone: () -> Unit) {
    val context = LocalContext.current
    val finish = { choice: NotificationsChoice ->
        profileStore.update { it.copy(notifications = choice) }
        onDone()
    }
    var asking by remember { mutableStateOf(false) }
    val permission =
        rememberLauncherForActivityResult(RequestPermission()) { granted ->
            asking = false
            finish(if (granted) NotificationsChoice.Granted else NotificationsChoice.Denied)
        }
    val turnOn = {
        when {
            asking -> Unit
            needsPermissionPrompt(context) -> {
                asking = true
                permission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            NotificationManagerCompat.from(context).areNotificationsEnabled() ->
                finish(NotificationsChoice.Granted)
            else -> finish(NotificationsChoice.Denied)
        }
    }

    PbScreenBody(
        modifier = Modifier.padding(horizontal = PbLayout.ScreenMargin),
        footer = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(PbSpace.S8),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                PbButton(
                    label = stringResource(R.string.setup4_turn_on),
                    onClick = turnOn,
                    modifier = Modifier.fillMaxWidth().testTag("setup4.enable"),
                )
                PbTextButton(
                    label = stringResource(R.string.setup4_not_now),
                    onClick = onDone,
                    modifier = Modifier.testTag("setup4.notNow"),
                    style = PbTextButtonStyle.Secondary,
                )
            }
        },
    ) {
        Spacer(Modifier.height(PbSpace.S24))
        PaybakRiveIllustration(
            PaybakRiveAsset.Notifications,
            Modifier.align(Alignment.CenterHorizontally).weight(1f, fill = false),
        )
        Spacer(Modifier.height(PbSpace.S32))
        PbTitleBlock(
            title = stringResource(R.string.setup4_headline),
            body = stringResource(R.string.setup4_body),
        )
    }
}

/** Android 13+ asks at runtime, unless the permission is already granted. */
private fun needsPermissionPrompt(context: Context): Boolean =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
