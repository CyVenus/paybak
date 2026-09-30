package app.paybak.paybak.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.paybak.paybak.R
import app.paybak.paybak.ui.theme.PbRadius
import app.paybak.paybak.ui.theme.PbSpace

/** `Size` of `Art / Receipt`. */
enum class PbReceiptSize {
    /** The Leopold Cafe receipt, 300 × 458: the demo camera scene and the simulated scan. */
    Full,

    /** 56 × 72, radius 10: the receipt slot of an expense. */
    Thumb,
}

/**
 * `Art / Receipt` (`PBReceiptThumbnail`). Thumb shows the real receipt [photo] (cropped to fill)
 * when there is one, and the drawn placeholder receipt otherwise. Full is the exported Figma art.
 */
@Composable
fun PbReceiptThumbnail(
    modifier: Modifier = Modifier,
    size: PbReceiptSize = PbReceiptSize.Thumb,
    photo: ImageBitmap? = null,
    contentDescription: String? = null,
) {
    when {
        size == PbReceiptSize.Full ->
            Image(
                painter = painterResource(R.drawable.art_receipt_full),
                contentDescription = contentDescription,
                modifier = modifier.size(300.dp, 458.dp),
            )
        photo != null ->
            Image(
                bitmap = photo,
                contentDescription = contentDescription,
                modifier = modifier.size(56.dp, 72.dp).clip(RoundedCornerShape(PbRadius.Sm)),
                contentScale = ContentScale.Crop,
            )
        else ->
            Image(
                painter = painterResource(R.drawable.art_receipt_thumb),
                contentDescription = contentDescription,
                modifier = modifier.size(56.dp, 72.dp),
            )
    }
}

@Preview(showBackground = true)
@Composable
private fun PbReceiptThumbnailPreview() {
    Row(horizontalArrangement = Arrangement.spacedBy(PbSpace.S16)) {
        PbReceiptThumbnail(size = PbReceiptSize.Full)
        PbReceiptThumbnail()
    }
}
