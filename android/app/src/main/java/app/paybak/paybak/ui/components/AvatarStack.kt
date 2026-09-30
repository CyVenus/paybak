package app.paybak.paybak.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.paybak.paybak.ui.theme.PbColors

private val RingWidth = 2.dp
private val Step = 24.dp

/**
 * `Avatar / Stack` (`PBAvatarStack`): 2–4 overlapping 32 dp heads, 8 dp overlap, each with a 2 dp
 * white ring outside it. Later heads sit on top. Decorative for TalkBack.
 */
@Composable
@JvmName("PbAvatarStackOfHeads")
fun PbAvatarStack(heads: List<PbPeepHead>, modifier: Modifier = Modifier) =
    PbAvatarStack(heads.map(PbAvatarContent::Art), modifier)

/**
 * [PbAvatarStack] of any avatars: the user's photo or character, a guest's initials, a Peep head.
 */
@Composable
fun PbAvatarStack(avatars: List<PbAvatarContent>, modifier: Modifier = Modifier) {
    require(avatars.size in 2..4) { "Avatar / Stack shows 2 to 4 heads" }
    val diameter = PbAvatarSize.Sm.diameter
    Box(
        modifier
            .size(width = diameter + Step * (avatars.size - 1), height = diameter)
            .clearAndSetSemantics {}
    ) {
        avatars.forEachIndexed { index, avatar ->
            PbAvatar(
                content = avatar,
                size = PbAvatarSize.Sm,
                modifier =
                    Modifier.offset(x = Step * index).drawBehind {
                        drawCircle(
                            PbColors.Bg.Primary,
                            radius = size.minDimension / 2 + RingWidth.toPx(),
                        )
                    },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PbAvatarStackPreview() {
    PbAvatarStack(listOf(PbPeepHead.Arjun, PbPeepHead.Priya, PbPeepHead.Rohan, PbPeepHead.Esha))
}
