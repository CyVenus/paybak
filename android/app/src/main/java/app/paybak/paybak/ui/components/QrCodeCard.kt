package app.paybak.paybak.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.paybak.paybak.R
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbShapes
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import kotlin.math.floor

/** The QR matrix fills this square inside the 240 dp card (a 21 dp quiet zone all round). */
private val MatrixSize = 198.dp

/**
 * `Card / QR Code` (`PBQRCodeCard`): a real, scannable QR code of [content] (the invite link, e.g.
 * https://paybak.app/i/arjun) in `text/primary` on a 240 dp white card, radius 20. It uses error
 * correction H, so the centred app mark on its white 52 dp backing ([showMark]) still scans. The
 * modules snap to whole pixels, so they never blur.
 */
@Composable
fun PbQrCodeCard(content: String, modifier: Modifier = Modifier, showMark: Boolean = true) {
    val modules = remember(content) { qrModules(content) }
    val description = stringResource(R.string.pb_qr_code, content)
    Box(
        modifier =
            modifier.size(240.dp).clip(PbShapes.Card).background(PbColors.Bg.Primary).semantics {
                contentDescription = description
                role = Role.Image
            },
        contentAlignment = Alignment.Center,
    ) {
        Spacer(
            Modifier.size(MatrixSize).drawWithCache {
                val count = modules.size
                val module = floor(size.width / count)
                val inset = (size.width - module * count) / 2
                val dark = Path()
                modules.forEachIndexed { y, row ->
                    row.forEachIndexed { x, on ->
                        if (on) {
                            val topLeft = Offset(inset + x * module, inset + y * module)
                            dark.addRect(Rect(topLeft, Size(module, module)))
                        }
                    }
                }
                onDrawBehind { drawPath(dark, PbColors.Text.Primary) }
            }
        )
        if (showMark) {
            Box(
                Modifier.size(52.dp).background(PbColors.Bg.Primary, PbShapes.Input),
                contentAlignment = Alignment.Center,
            ) {
                PbAppMark(size = 40.dp)
            }
        }
    }
}

/** The QR code of [content] at error correction H, as rows of dark (true) modules, no margin. */
internal fun qrModules(content: String): List<List<Boolean>> {
    val hints =
        mapOf(EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.H, EncodeHintType.MARGIN to 0)
    val matrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, 0, 0, hints)
    return List(matrix.height) { y -> List(matrix.width) { x -> matrix[x, y] } }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F5F5)
@Composable
private fun PbQrCodeCardPreview() {
    PbQrCodeCard("https://paybak.app/i/arjun")
}
