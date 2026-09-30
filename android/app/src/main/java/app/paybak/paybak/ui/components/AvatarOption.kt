package app.paybak.paybak.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbMotion
import app.paybak.paybak.ui.theme.PbSize
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles

private val InnerDiameter = 46.dp
private val SelectedRingWidth = 2.5.dp

/**
 * `Control / Avatar Option` (`PBAvatarOption`): a 56 dp choice in the Setup 1 picker. The avatar
 * sits in a 46 dp #F5F5F5 circle; selected adds a 2.5 dp black ring with a white gap.
 *
 * @param content A head, the user's photo, or `Symbol(PbIcon.Camera)` for the Upload option.
 */
@Composable
fun PbAvatarOption(
    content: PbAvatarContent,
    selected: Boolean,
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    val ring by
        animateColorAsState(
            targetValue = if (selected) PbColors.Border.Strong else Color.Transparent,
            animationSpec = tween(PbMotion.FADE_MILLIS),
            label = "PbAvatarOption ring",
        )
    Box(
        modifier =
            modifier
                .size(PbSize.AvatarLg)
                .border(SelectedRingWidth, ring, CircleShape)
                .selectable(
                    selected = selected,
                    interactionSource = null,
                    indication = null,
                    role = Role.RadioButton,
                    onClick = onClick,
                )
                .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        AvatarCircle(
            content = content,
            diameter = InnerDiameter,
            fill = PbColors.Bg.Card,
            initialsStyle = PbTextStyles.Headline,
            iconSize = PbSize.IconLg,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PbAvatarOptionPreview() {
    Row(horizontalArrangement = Arrangement.spacedBy(PbSpace.S4)) {
        PbAvatarOption(
            PbAvatarContent.Art(PbPeepHead.Arjun),
            selected = true,
            onClick = {},
            "Arjun",
        )
        PbAvatarOption(
            PbAvatarContent.Art(PbPeepHead.Priya),
            selected = false,
            onClick = {},
            "Priya",
        )
        PbAvatarOption(
            PbAvatarContent.Symbol(PbIcon.Camera),
            selected = false,
            onClick = {},
            "Choose a photo",
        )
    }
}
