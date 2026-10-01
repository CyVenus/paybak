package app.paybak.paybak.feature.expense

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import app.paybak.paybak.R
import app.paybak.paybak.feature.pickers.LedgerPhotos
import app.paybak.paybak.navigation.LocalMainNavigator
import app.paybak.paybak.navigation.Route
import app.paybak.paybak.ui.components.PbGlassCloseButton
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbLayout
import app.paybak.paybak.ui.theme.PbSize
import app.paybak.paybak.ui.theme.PbTextStyles

private const val MAX_ZOOM = 4f

/** A photo file once read: its image, or null when it can't be loaded. */
private class LoadedPhoto(val image: ImageBitmap?)

/**
 * The `photoViewer` route (activity §4.3-D, proposal): a receipt or proof full screen on black,
 * pinch to zoom, ✕ to close. Demo records show the bundled receipt art; a photo file that can't
 * be loaded says so.
 */
@Composable
fun PhotoViewerScreen(route: Route.PhotoViewer) {
    val navigator = LocalMainNavigator.current
    val context = LocalContext.current
    val file = route.photo.file
    // Null while the file is read.
    val loaded by
        produceState<LoadedPhoto?>(null, file) {
            value = file?.let { LoadedPhoto(LedgerPhotos.load(context, it)) }
        }
    val photo = loaded?.image
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val zoom = rememberTransformableState { change, pan, _ ->
        scale = (scale * change).coerceIn(1f, MAX_ZOOM)
        offset = if (scale == 1f) Offset.Zero else offset + pan
    }
    Box(
        Modifier.fillMaxSize().background(PbColors.Bg.Device).testTag("screen.photoViewer"),
        contentAlignment = Alignment.Center,
    ) {
        val imageModifier =
            Modifier.fillMaxSize().transformable(zoom).graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationX = offset.x
                translationY = offset.y
            }
        when {
            photo != null ->
                Image(
                    photo,
                    stringResource(R.string.add_receipt_photo),
                    imageModifier,
                    contentScale = ContentScale.Fit,
                )
            file == null ->
                Image(
                    painterResource(R.drawable.art_receipt_full),
                    stringResource(R.string.add_receipt),
                    imageModifier.padding(PbLayout.ScreenMargin),
                    contentScale = ContentScale.Fit,
                )
            loaded != null ->
                Text(
                    stringResource(R.string.add_photo_unavailable),
                    Modifier.padding(PbLayout.ScreenMargin).testTag("photoViewer.unavailable"),
                    style = PbTextStyles.Body,
                    color = PbColors.Text.Inverse,
                )
        }
        PbGlassCloseButton(
            onClick = navigator::dismissModal,
            modifier =
                Modifier.align(Alignment.TopStart)
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(PbLayout.ScreenMargin)
                    .testTag("photoViewer.close"),
            diameter = PbSize.Tap,
        )
    }
}
