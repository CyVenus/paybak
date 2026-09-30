package app.paybak.paybak.ui.components.avatar

import android.content.res.Resources
import android.util.LruCache
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.VectorGroup
import androidx.compose.ui.graphics.vector.VectorNode
import androidx.compose.ui.graphics.vector.VectorPath
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.paybak.paybak.domain.model.AvatarGender
import app.paybak.paybak.domain.model.AvatarLook
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbSpace
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** The rig every part is drawn in (screens-profile §1.1). */
private const val RIG_WIDTH = 772f
private const val RIG_HEIGHT = 842f

/** How the rig maps into a container (screens-profile §1.5): a rig rectangle scaled to fill it. */
enum class AvatarCrop {
    /** Rig (72, 60, 600, 600): avatar circles and the face-part tiles. */
    Head,

    /** Rig (61, 192, 650, 650): the Outfit tiles. */
    Bust,

    /** Rig y 60 → 842 fills the height, centred: `Avatar / Stage`. */
    Stage;

    /** The rig's scale and the rig origin's position inside a container of [size]. */
    internal fun placement(size: Size): Pair<Float, Offset> =
        when (this) {
            Head -> (size.width / 600f).let { it to Offset(-72f * it, -60f * it) }
            Bust -> (size.width / 650f).let { it to Offset(-61f * it, -192f * it) }
            Stage ->
                (size.height / 782f).let {
                    it to Offset((size.width - RIG_WIDTH * it) / 2f, -60f * it)
                }
        }
}

/**
 * `Avatar / Character` (`PBAvatarView`): the custom character of [look], its part layers stacked
 * in the rig and framed by [crop]. It draws no background; the container (circle, tile, stage)
 * gives the fill and the shape. [placeholder] shows while the look is first composed (off the main
 * thread; composed looks are cached, so this is rare).
 */
@Composable
fun PbAvatarCharacter(
    look: AvatarLook,
    crop: AvatarCrop,
    modifier: Modifier = Modifier,
    placeholder: @Composable () -> Unit = {},
) {
    val vector = rememberAvatarVector(look)
    Box(modifier.clipToBounds(), contentAlignment = Alignment.Center) {
        if (vector == null) {
            placeholder()
        } else {
            val painter = rememberVectorPainter(vector)
            Canvas(Modifier.fillMaxSize()) {
                val (scale, origin) = crop.placement(size)
                translate(origin.x, origin.y) {
                    with(painter) { draw(Size(RIG_WIDTH * scale, RIG_HEIGHT * scale)) }
                }
            }
        }
    }
}

/**
 * The Head crop of a custom character inside an avatar circle (screens-profile §1.5): every "You"
 * circle once the user saved a character. The [initials] show until the look is composed.
 */
@Composable
fun AvatarCharacterHead(
    look: AvatarLook,
    initials: String,
    initialsStyle: TextStyle,
    modifier: Modifier = Modifier,
) {
    PbAvatarCharacter(look, AvatarCrop.Head, modifier) {
        Text(initials, style = initialsStyle, color = PbColors.Text.Primary, maxLines = 1)
    }
}

/** [look] as one vector, from the cache at once or composed in the background (null until then). */
@Composable
private fun rememberAvatarVector(look: AvatarLook): ImageVector? {
    val resources = LocalResources.current
    val vector = remember(look) { mutableStateOf(AvatarVectors.cached(look)) }
    if (vector.value == null) {
        LaunchedEffect(look) {
            vector.value = withContext(Dispatchers.Default) { AvatarVectors.of(resources, look) }
        }
    }
    return vector.value
}

/**
 * Parsed part drawables and composed looks. A look becomes one [ImageVector] with every layer's
 * paths in order, so each avatar draws through a single vector painter (one cached bitmap at the
 * drawn size), which keeps lists of small circles cheap.
 */
private object AvatarVectors {
    private const val LOOKS_CACHED = 48

    private val parts = ConcurrentHashMap<Int, ImageVector>()
    private val looks = LruCache<AvatarLook, ImageVector>(LOOKS_CACHED)

    fun cached(look: AvatarLook): ImageVector? = looks.get(look)

    fun of(resources: Resources, look: AvatarLook): ImageVector =
        looks.get(look)
            ?: compose(AvatarLayers.of(look).map { part(resources, it) }).also {
                looks.put(look, it)
            }

    private fun part(resources: Resources, id: Int): ImageVector =
        parts.getOrPut(id) { ImageVector.vectorResource(res = resources, resId = id) }

    private fun compose(layers: List<ImageVector>): ImageVector =
        ImageVector.Builder(
                name = "Avatar",
                defaultWidth = RIG_WIDTH.dp,
                defaultHeight = RIG_HEIGHT.dp,
                viewportWidth = RIG_WIDTH,
                viewportHeight = RIG_HEIGHT,
            )
            .apply { layers.forEach { layer -> layer.root.forEach { addNode(it) } } }
            .build()

    private fun ImageVector.Builder.addNode(node: VectorNode) {
        when (node) {
            is VectorPath ->
                addPath(
                    pathData = node.pathData,
                    pathFillType = node.pathFillType,
                    fill = node.fill,
                    fillAlpha = node.fillAlpha,
                    stroke = node.stroke,
                    strokeAlpha = node.strokeAlpha,
                    strokeLineWidth = node.strokeLineWidth,
                    strokeLineCap = node.strokeLineCap,
                    strokeLineJoin = node.strokeLineJoin,
                    strokeLineMiter = node.strokeLineMiter,
                )
            is VectorGroup -> {
                addGroup(
                    rotate = node.rotation,
                    pivotX = node.pivotX,
                    pivotY = node.pivotY,
                    scaleX = node.scaleX,
                    scaleY = node.scaleY,
                    translationX = node.translationX,
                    translationY = node.translationY,
                    clipPathData = node.clipPathData,
                )
                node.forEach { addNode(it) }
                clearGroup()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PbAvatarCharacterPreview() {
    val girl = AvatarLook(gender = AvatarGender.Girl)
    Row(horizontalArrangement = Arrangement.spacedBy(PbSpace.S8)) {
        listOf(AvatarLook.DefaultBoy, girl).forEach { look ->
            PbAvatarCharacter(
                look,
                AvatarCrop.Head,
                Modifier.size(120.dp).clip(CircleShape).background(PbColors.Bg.Card),
            )
        }
    }
}
