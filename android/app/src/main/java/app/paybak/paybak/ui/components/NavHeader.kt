package app.paybak.paybak.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbMotion
import app.paybak.paybak.ui.theme.PbSize
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles

/** The glass action of a Large Title header (Bell, Plus, User Add or Restore). */
data class PbNavAction(
    val icon: PbIcon,
    val contentDescription: String,
    val onClick: () -> Unit,
    val badge: Boolean = false,
    val testTag: String? = null,
)

/**
 * `Navigation / Nav Header` Type=Large Title (`PBNavHeader`): a tab's Title/1 title with an
 * optional glass action, 44 dp tall, inside the screen margins. Put it at the top of the scroll
 * content and show [PbNavHeaderInline] once it scrolls away.
 */
@Composable
fun PbNavHeader(title: String, modifier: Modifier = Modifier, action: PbNavAction? = null) {
    Row(
        modifier.fillMaxWidth().height(PbSize.Tap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f).semantics { heading() },
            style = PbTextStyles.Title1,
            color = PbColors.Text.Primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (action != null) {
            PbIconButton(
                icon = action.icon,
                contentDescription = action.contentDescription,
                onClick = action.onClick,
                modifier = Modifier.partTag(action.testTag),
                style = PbIconButtonStyle.Glass,
                badge = action.badge,
            )
        }
    }
}

/**
 * `Navigation / Nav Header` Type=Inline: the 44 dp collapsed bar of a scrolled tab root, full width
 * under the status bar, white at 90 % with a centred Headline. It fades in when [visible].
 */
@Composable
fun PbNavHeaderInline(title: String, visible: Boolean, modifier: Modifier = Modifier) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(tween(PbMotion.SWAP_MILLIS)),
        exit = fadeOut(tween(PbMotion.SWAP_MILLIS)),
    ) {
        Column(Modifier.fillMaxWidth().background(PbColors.Bg.Primary.copy(alpha = INLINE_ALPHA))) {
            Box(
                Modifier.fillMaxWidth().statusBarsPadding().height(PbSize.Tap),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    title,
                    style = PbTextStyles.Headline,
                    color = PbColors.Text.Primary,
                    maxLines = 1,
                )
            }
        }
    }
}

private const val INLINE_ALPHA = 0.9f

@Preview(showBackground = true, widthDp = 402)
@Composable
private fun PbNavHeaderPreview() {
    Column(Modifier.padding(horizontal = 20.dp, vertical = PbSpace.S16)) {
        PbNavHeader("Groups", action = PbNavAction(PbIcon.Plus, "New group", {}))
        PbNavHeader("Activity", action = PbNavAction(PbIcon.Restore, "Recently deleted", {}))
        PbNavHeaderInline("Activity", visible = true)
    }
}
