package app.paybak.paybak.ui.components.home

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import app.paybak.paybak.R
import app.paybak.paybak.ui.components.PbIconButton
import app.paybak.paybak.ui.components.PbIconButtonStyle
import app.paybak.paybak.ui.components.PbLogo
import app.paybak.paybak.ui.components.PbLogoLayout
import app.paybak.paybak.ui.components.partTag
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbSize
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles

/**
 * `Navigation / Nav Header` Type=Home: the logo, the glass sparkle (Ask Paybak) and the glass bell
 * with its unread dot on a 44 dp toolbar, then the Title/1 greeting, which wraps when the name is
 * long. [onLogoLongPress] opens the debug menu in debug builds. Parts are tagged "[testTag].logo",
 * ".assistant", ".bell" and ".greeting".
 */
@Composable
fun PbHomeHeader(
    greeting: String,
    unread: Boolean,
    onAssistant: () -> Unit,
    onNotifications: () -> Unit,
    modifier: Modifier = Modifier,
    onLogoLongPress: (() -> Unit)? = null,
    testTag: String? = null,
) {
    val longPress by rememberUpdatedState(onLogoLongPress)
    val brand = stringResource(R.string.app_name)
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(PbSpace.S12)) {
        Row(
            Modifier.fillMaxWidth().height(PbSize.Tap),
            horizontalArrangement = Arrangement.spacedBy(PbSpace.S8),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PbLogo(
                PbLogoLayout.Horizontal,
                Modifier.partTag(testTag, "logo")
                    .semantics(mergeDescendants = true) { contentDescription = brand }
                    .pointerInput(Unit) {
                        detectTapGestures(onLongPress = { longPress?.invoke() })
                    },
            )
            Spacer(Modifier.weight(1f))
            PbIconButton(
                PbIcon.Sparkles,
                stringResource(R.string.home_ask_paybak),
                onClick = onAssistant,
                modifier = Modifier.partTag(testTag, "assistant"),
                style = PbIconButtonStyle.Glass,
            )
            PbIconButton(
                PbIcon.Bell,
                stringResource(
                    if (unread) R.string.home_notifications_unread else R.string.home_notifications
                ),
                onClick = onNotifications,
                modifier = Modifier.partTag(testTag, "bell"),
                style = PbIconButtonStyle.Glass,
                badge = unread,
            )
        }
        Text(
            greeting,
            Modifier.fillMaxWidth().partTag(testTag, "greeting").semantics { heading() },
            style = PbTextStyles.Title1,
            color = PbColors.Text.Primary,
        )
    }
}

@Preview(showBackground = true, widthDp = 402)
@Composable
private fun PbHomeHeaderPreview() {
    Column(
        Modifier.padding(PbSpace.S20),
        verticalArrangement = Arrangement.spacedBy(PbSpace.S24),
    ) {
        PbHomeHeader("Good evening, Arjun", unread = true, onAssistant = {}, onNotifications = {})
        PbHomeHeader(
            "Good afternoon, Alexandria-Rose",
            unread = false,
            onAssistant = {},
            onNotifications = {},
        )
    }
}
