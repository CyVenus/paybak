package app.paybak.paybak.service.receipt

import android.graphics.Bitmap
import app.paybak.paybak.domain.model.ReceiptScan
import app.paybak.paybak.domain.scan.OcrLine
import app.paybak.paybak.domain.scan.ReceiptParser
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Reads a receipt photo with on-device text recognition (ML Kit, bundled Latin model; it works
 * offline) and parses the lines into a [ReceiptScan] (app-architecture §4.1).
 */
object ReceiptReader {
    /** Null when nothing could be read ("Couldn’t read this receipt"). */
    suspend fun read(image: Bitmap): ReceiptScan? = ReceiptParser.parse(recognize(image))

    private suspend fun recognize(image: Bitmap): List<OcrLine> =
        suspendCancellableCoroutine { continuation ->
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            continuation.invokeOnCancellation { recognizer.close() }
            recognizer
                .process(InputImage.fromBitmap(image, 0))
                .addOnSuccessListener { text ->
                    val lines =
                        text.textBlocks.flatMap { block ->
                            block.lines.mapNotNull { line ->
                                val box = line.boundingBox ?: return@mapNotNull null
                                OcrLine(
                                    line.text,
                                    box.left.toFloat(),
                                    box.top.toFloat(),
                                    box.right.toFloat(),
                                    box.bottom.toFloat(),
                                )
                            }
                        }
                    recognizer.close()
                    continuation.resume(lines)
                }
                .addOnFailureListener {
                    recognizer.close()
                    continuation.resume(emptyList())
                }
        }
}
