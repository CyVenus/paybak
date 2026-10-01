package app.paybak.paybak.service.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Build
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import app.paybak.paybak.R
import kotlin.coroutines.resume
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

/**
 * The receipt camera's feed (app-architecture §4): CameraX on devices; on emulators (or with the
 * debug toggle) the bundled Leopold Cafe receipt stands in, so the real reading path still runs.
 */
object CameraSource {
    /** True on emulators by default; the debug menu flips it. */
    var isSimulated: Boolean =
        Build.FINGERPRINT.contains("generic") || Build.PRODUCT.contains("sdk")

    /** The receipt the simulated camera shows and captures. */
    suspend fun simulatedPhoto(context: Context): Bitmap =
        withContext(Dispatchers.IO) {
            BitmapFactory.decodeResource(context.resources, R.drawable.art_receipt_full)
        }
}

/** The live preview, filling [modifier]; [torch] lights the flash. Returns what takes photos. */
@Composable
fun CameraPreview(torch: Boolean, modifier: Modifier = Modifier): LifecycleCameraController {
    val context = LocalContext.current
    val owner = LocalLifecycleOwner.current
    val controller = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(CameraController.IMAGE_CAPTURE)
        }
    }
    DisposableEffect(owner) {
        controller.bindToLifecycle(owner)
        onDispose { controller.unbind() }
    }
    LaunchedEffect(torch) { controller.enableTorch(torch) }
    AndroidView(
        factory = {
            PreviewView(it).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
                this.controller = controller
            }
        },
        modifier = modifier,
    )
    return controller
}

/** Takes a photo, upright; null if the camera failed. */
suspend fun LifecycleCameraController.capture(context: Context): Bitmap? =
    suspendCancellableCoroutine { continuation ->
        takePicture(
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    val degrees = image.imageInfo.rotationDegrees
                    val bitmap = image.toBitmap()
                    image.close()
                    continuation.resume(bitmap.rotated(degrees))
                }

                override fun onError(exception: ImageCaptureException) {
                    continuation.resume(null)
                }
            },
        )
    }

private fun Bitmap.rotated(degrees: Int): Bitmap =
    if (degrees == 0) this
    else
        Bitmap.createBitmap(
            this,
            0,
            0,
            width,
            height,
            Matrix().apply { postRotate(degrees.toFloat()) },
            true,
        )
