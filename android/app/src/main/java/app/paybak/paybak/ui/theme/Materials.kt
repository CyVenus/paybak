package app.paybak.paybak.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp

/**
 * The Figma effect styles. Android has no Liquid Glass, so every material uses the agreed fallback:
 * a translucent white fill (`bg/glass`), the style's drop shadow and a 1 dp inside highlight
 * (`border/glass-highlight`). Glass goes only on floating chrome (Materials rule 1).
 */
enum class PbMaterial(offsetY: Dp, blur: Dp, alpha: Float) {
    /** `Material/Glass`: the floating tab bar. */
    Glass(offsetY = 8.dp, blur = 32.dp, alpha = 0.10f),

    /** `Material/Glass Small`: 44 dp toolbar buttons (Home bell). */
    GlassSmall(offsetY = 4.dp, blur = 16.dp, alpha = 0.08f),

    /** `Material/Frosted`: the background blur is approximated by the translucent fill. */
    Frosted(offsetY = 8.dp, blur = 32.dp, alpha = 0.10f);

    val shadow =
        Shadow(
            radius = blur,
            color = PbPalette.Gray900,
            offset = DpOffset(0.dp, offsetY),
            alpha = alpha,
        )
}

/**
 * Draws [material] behind the content in [shape]. [fill] defaults to `bg/glass`; the Glass icon
 * button swaps it for `bg/card` while pressed. The shadow is cut out under the shape, as in Figma,
 * so it never greys the translucent fill.
 */
fun Modifier.pbMaterial(
    material: PbMaterial,
    shape: Shape,
    fill: Color = PbColors.Bg.Glass,
): Modifier =
    this.drawWithCache {
            val shadow = obtainShadowContext().createDropShadowPainter(shape, material.shadow)
            val body =
                Path().apply {
                    addOutline(shape.createOutline(size, layoutDirection, this@drawWithCache))
                }
            onDrawBehind {
                clipPath(body, ClipOp.Difference) {
                    with(shadow) { draw(size) }
                }
            }
        }
        .background(fill, shape)
        .border(PbSize.Hairline, PbColors.Border.GlassHighlight, shape)

/** `bg/scrim`: the 40 % black layer behind sheets. */
fun Modifier.pbScrim(): Modifier = background(PbColors.Bg.Scrim)
