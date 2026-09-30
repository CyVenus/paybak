package app.paybak.paybak.ui.components.avatar

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.paybak.paybak.domain.model.AvatarLook
import app.paybak.paybak.domain.model.BoyLook
import app.paybak.paybak.ui.components.rememberPressState
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbMotion
import app.paybak.paybak.ui.theme.PbShapes
import app.paybak.paybak.ui.theme.PbSpace

private val RingWidth = 2.dp
private val GapShape = RoundedCornerShape(18.dp)

/** A pressed tile shrinks a little (no pressed variant in Figma; screens-profile §5.3). */
private const val PRESSED_SCALE = 0.97f

/**
 * `Control / Avatar Part Tile` (`PBAvatarPartTile`): a square r20 tile showing [look], the draft
 * with this tile's option swapped in, framed by [crop] (Head, or Bust for outfits). Selected draws
 * a 2 dp black ring with a 2 dp white gap inside it. [label] is the option name TalkBack reads.
 */
@Composable
fun PbAvatarPartTile(
    look: AvatarLook,
    crop: AvatarCrop,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val press = rememberPressState(interactionSource = null)
    val scale by
        animateFloatAsState(
            if (press.isPressed) PRESSED_SCALE else 1f,
            tween(PbMotion.PRESS_MILLIS),
            label = "Avatar tile press",
        )
    Box(
        modifier
            .aspectRatio(1f)
            .scale(scale)
            .clip(PbShapes.Card)
            .background(PbColors.Bg.Card)
            .selectable(
                selected = selected,
                interactionSource = press.source,
                indication = null,
                role = Role.RadioButton,
                onClick = onClick,
            )
            .semantics { contentDescription = label }
    ) {
        PbAvatarCharacter(look, crop, Modifier.fillMaxSize())
        if (selected) {
            Box(
                Modifier.fillMaxSize()
                    .padding(RingWidth)
                    .border(RingWidth, PbColors.Bg.Primary, GapShape)
            )
            Box(Modifier.fillMaxSize().border(RingWidth, PbColors.Border.Strong, PbShapes.Card))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PbAvatarPartTilePreview() {
    Row(horizontalArrangement = Arrangement.spacedBy(PbSpace.S12)) {
        PbAvatarPartTile(
            AvatarLook(boy = BoyLook(hair = "quiff")),
            AvatarCrop.Head,
            "Quiff",
            selected = true,
            onClick = {},
            Modifier.size(112.dp),
        )
        PbAvatarPartTile(
            AvatarLook(boy = BoyLook(outfit = "jacket")),
            AvatarCrop.Bust,
            "Jacket",
            selected = false,
            onClick = {},
            Modifier.size(112.dp),
        )
    }
}
