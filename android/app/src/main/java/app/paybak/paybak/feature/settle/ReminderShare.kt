package app.paybak.paybak.feature.settle

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

/** The broadcast the share sheet sends once an app has been picked for the reminder. */
private const val SHARED_ACTION = "app.paybak.paybak.action.REMINDER_SHARED"

/**
 * Hands a reminder message to the system share sheet (screens-settle §6.4, §7). Paybak can't know
 * whether the message went out, so [onShared] runs once an app has been picked: the closest sign of
 * a completed share. Backing out of the share sheet changes nothing.
 */
@Composable
internal fun rememberReminderShare(onShared: () -> Unit): (String) -> Unit {
    val context = LocalContext.current
    val latest by rememberUpdatedState(onShared)
    DisposableEffect(context) {
        val receiver =
            object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) = latest()
            }
        ContextCompat.registerReceiver(
            context,
            receiver,
            IntentFilter(SHARED_ACTION),
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )
        onDispose { context.unregisterReceiver(receiver) }
    }
    return { text ->
        val picked =
            PendingIntent.getBroadcast(
                context,
                0,
                Intent(SHARED_ACTION).setPackage(context.packageName),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        val send =
            Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text)
        context.startActivity(Intent.createChooser(send, null, picked.intentSender))
    }
}
