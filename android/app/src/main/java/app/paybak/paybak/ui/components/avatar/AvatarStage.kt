package app.paybak.paybak.ui.components.avatar

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.paybak.paybak.domain.model.AvatarLook
import app.paybak.paybak.ui.components.PbIconButton
import app.paybak.paybak.ui.components.PbIconButtonStyle
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbMotion
import app.paybak.paybak.ui.theme.PbShapes
import app.paybak.paybak.ui.theme.PbSpace

/** The stage's Figma height; the character's scale follows it (Stage crop). */
private val StageHeight = 300.dp

/**
 * `Avatar / Stage` (`PBAvatarStage`): the live preview of the editor. The whole character, framed
 * by the Stage crop, on a #F5F5F5 r20 card that fills the width, with the glass Shuffle button 12
 * dp in from the bottom-right corner. A new look cross-fades in.
 *
 * @param previewLabel What TalkBack reads for the preview ("Avatar preview").
 * @param shuffleModifier Applied to the Shuffle button, e.g. its test tag.
 */
@Composable
fun PbAvatarStage(
    look: AvatarLook,
    onShuffle: () -> Unit,
    shuffleLabel: String,
    modifier: Modifier = Modifier,
    previewLabel: String? = null,
    shuffleModifier: Modifier = Modifier,
) {
    Box(
        modifier
            .fillMaxWidth()
            .height(StageHeight)
            .clip(PbShapes.Card)
            .background(PbColors.Bg.Card)
    ) {
        Crossfade(
            targetState = look,
            modifier =
                Modifier.fillMaxSize().semantics {
                    previewLabel?.let { contentDescription = it }
                },
            animationSpec = tween(PbMotion.FADE_MILLIS),
            label = "Avatar stage",
        ) {
            PbAvatarCharacter(it, AvatarCrop.Stage, Modifier.fillMaxSize())
        }
        PbIconButton(
            PbIcon.Shuffle,
            contentDescription = shuffleLabel,
            onClick = onShuffle,
            modifier = shuffleModifier.align(Alignment.BottomEnd).padding(PbSpace.S12),
            style = PbIconButtonStyle.Glass,
        )
    }
}

@Preview(showBackground = true, widthDp = 362)
@Composable
private fun PbAvatarStagePreview() {
    PbAvatarStage(AvatarLook.DefaultBoy, onShuffle = {}, shuffleLabel = "Shuffle avatar")
}
