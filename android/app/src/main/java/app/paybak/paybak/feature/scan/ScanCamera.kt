package app.paybak.paybak.feature.scan

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import app.paybak.paybak.R
import app.paybak.paybak.feature.pickers.LedgerPhotos
import app.paybak.paybak.service.camera.CameraPreview
import app.paybak.paybak.service.camera.CameraSource
import app.paybak.paybak.service.camera.capture
import app.paybak.paybak.service.receipt.ReceiptPhotos
import app.paybak.paybak.ui.components.PbShutterButton
import app.paybak.paybak.ui.icons.PbIcon
import app.paybak.paybak.ui.icons.PbIconImage
import app.paybak.paybak.ui.theme.PbColors
import app.paybak.paybak.ui.theme.PbLayout
import app.paybak.paybak.ui.theme.PbPalette
import app.paybak.paybak.ui.theme.PbShapes
import app.paybak.paybak.ui.theme.PbSize
import app.paybak.paybak.ui.theme.PbSpace
import app.paybak.paybak.ui.theme.PbTextStyles
import kotlinx.coroutines.launch

/** The receipt guides: 300 × 460, 64 below the top bar (Figma 170 − 106). */
private val GuidesWidth = 300.dp
private val GuidesHeight = 460.dp
private val GuidesTop = 64.dp
private val GuideArm = 36.dp
private val GuideStroke = 4.dp
private val GuideRadius = 12.dp

/** The simulated feed: the receipt art centred in the guides, turned 4° clockwise (Figma). */
private val ArtWidth = 257.dp
private val ArtHeight = 392.4.dp
private const val ART_TURN = 4f

/** The kit glass glyphs (SF Symbols at Semibold 19) are drawn on a 28 dp grid. */
private val GlyphSize = 28.dp

/** The glass discs on the camera read as mid grey (white 40 % over the dark backdrop). */
private val ControlDisc = PbPalette.Gray0.copy(alpha = 0.4f)

/**
 * The camera page (scanCamera; insights §4.2): a dark full screen with the live preview, ✕ and the
 * flash, the receipt guides, the hint, Upload photo and the shutter. [onPhoto] gets the picture
 * (and its saved file when it came from the photo picker); while [reading], a progress line
 * replaces the hint. Without camera access the hint says how to allow it and Upload still works.
 */
@Composable
internal fun ScanCamera(reading: Boolean, onClose: () -> Unit, onPhoto: (Bitmap, String?) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val simulated = CameraSource.isSimulated
    var torch by rememberSaveable { mutableStateOf(false) }
    var allowed by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    var asked by rememberSaveable { mutableStateOf(false) }
    val permission =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
            allowed = it
            asked = true
        }
    LaunchedEffect(Unit) {
        if (!simulated && !allowed && !asked) permission.launch(Manifest.permission.CAMERA)
    }
    val upload =
        rememberLauncherForActivityResult(PickVisualMedia()) { uri ->
            if (uri != null) {
                scope.launch {
                    val name = LedgerPhotos.save(context, uri) ?: return@launch
                    ReceiptPhotos.load(context, name)?.let { onPhoto(it, name) }
                }
            }
        }
    LightSystemBars()
    Box(Modifier.fillMaxSize().background(PbColors.Bg.Camera).testTag("screen.scanCamera")) {
        val camera =
            if (!simulated && allowed) CameraPreview(torch, Modifier.fillMaxSize()) else null
        Column(Modifier.fillMaxSize().systemBarsPadding()) {
            TopBar(torch = torch, onClose = onClose, onFlash = { torch = !torch })
            Box(
                Modifier.align(Alignment.CenterHorizontally)
                    .padding(top = GuidesTop)
                    .size(GuidesWidth, GuidesHeight)
            ) {
                if (simulated) {
                    Image(
                        painterResource(R.drawable.art_receipt_full),
                        contentDescription = null,
                        modifier =
                            Modifier.align(Alignment.Center)
                                .size(ArtWidth, ArtHeight)
                                .rotate(ART_TURN),
                    )
                }
                Guides(Modifier.fillMaxSize())
            }
            Hint(
                when {
                    reading -> null
                    !simulated && !allowed -> stringResource(R.string.insights_scan_allow_camera)
                    else -> stringResource(R.string.insights_scan_hint)
                },
                Modifier.align(Alignment.CenterHorizontally).padding(top = PbSpace.S16),
            )
            Spacer(Modifier.weight(1f))
            Box(
                Modifier.fillMaxWidth()
                    .padding(horizontal = PbLayout.ScreenMargin)
                    .padding(bottom = PbSpace.S24)
            ) {
                UploadButton(
                    onClick = {
                        if (!reading)
                            upload.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
                    },
                    modifier = Modifier.align(Alignment.CenterStart),
                )
                PbShutterButton(
                    onClick = {
                        if (reading) return@PbShutterButton
                        scope.launch {
                            val photo =
                                when {
                                    simulated -> CameraSource.simulatedPhoto(context)
                                    else -> camera?.capture(context)
                                }
                            photo?.let { onPhoto(it, null) }
                        }
                    },
                    modifier = Modifier.align(Alignment.Center).testTag("scan.shutter"),
                )
            }
        }
    }
}

@Composable
private fun TopBar(torch: Boolean, onClose: () -> Unit, onFlash: () -> Unit) {
    Box(Modifier.fillMaxWidth().height(PbSize.Tap).padding(horizontal = PbLayout.ScreenMargin)) {
        DiscButton(
            R.drawable.kit_xmark,
            stringResource(R.string.pb_close),
            onClose,
            Modifier.align(Alignment.CenterStart).testTag("scan.close"),
        )
        Text(
            stringResource(R.string.insights_scan_title),
            Modifier.align(Alignment.Center),
            style = PbTextStyles.Headline,
            color = PbColors.Text.Inverse,
        )
        DiscButton(
            R.drawable.kit_bolt_fill,
            stringResource(R.string.insights_scan_flash),
            onFlash,
            Modifier.align(Alignment.CenterEnd).testTag("scan.flash"),
            on = torch,
        )
    }
}

/** A 44 dp translucent disc with a white glyph; [on] turns it white with a black glyph (flash). */
@Composable
private fun DiscButton(
    @DrawableRes glyph: Int,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    on: Boolean = false,
) {
    Box(
        modifier
            .size(PbSize.Tap)
            .clip(CircleShape)
            .background(if (on) PbColors.Bg.Primary else ControlDisc)
            .clickable(onClick = onClick)
            .semantics {
                contentDescription = label
                role = Role.Button
            },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painterResource(glyph),
            contentDescription = null,
            modifier = Modifier.size(GlyphSize),
            tint = if (on) PbColors.Icon.Primary else PbColors.Icon.Inverse,
        )
    }
}

/** The four L brackets around the receipt: 36 dp arms, 4 dp white, round caps. */
@Composable
private fun Guides(modifier: Modifier) {
    Canvas(modifier) {
        val stroke = GuideStroke.toPx()
        val inset = stroke / 2
        val arm = GuideArm.toPx()
        val radius = GuideRadius.toPx()
        val corners =
            listOf(
                Offset(inset, inset) to Offset(1f, 1f),
                Offset(size.width - inset, inset) to Offset(-1f, 1f),
                Offset(size.width - inset, size.height - inset) to Offset(-1f, -1f),
                Offset(inset, size.height - inset) to Offset(1f, -1f),
            )
        corners.forEach { (corner, direction) ->
            val path =
                Path().apply {
                    moveTo(corner.x, corner.y + direction.y * arm)
                    lineTo(corner.x, corner.y + direction.y * radius)
                    arcTo(
                        Rect(
                            Offset(
                                minOf(corner.x, corner.x + direction.x * 2 * radius),
                                minOf(corner.y, corner.y + direction.y * 2 * radius),
                            ),
                            Size(2 * radius, 2 * radius),
                        ),
                        startAngleDegrees = if (direction.x > 0) 180f else 0f,
                        sweepAngleDegrees = if (direction.x * direction.y > 0) 90f else -90f,
                        forceMoveTo = false,
                    )
                    lineTo(corner.x + direction.x * arm, corner.y)
                }
            drawPath(path, PbColors.Icon.Inverse, style = Stroke(stroke, cap = StrokeCap.Round))
        }
    }
}

/** The hint pill; null shows the reading progress instead. */
@Composable
private fun Hint(text: String?, modifier: Modifier) {
    Row(
        modifier
            .height(PbSize.ButtonSm)
            .background(PbColors.Bg.Scrim, PbShapes.Pill)
            .padding(horizontal = PbSpace.S16)
            .testTag("scan.hint"),
        horizontalArrangement = Arrangement.spacedBy(PbSpace.S6),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (text == null) {
            CircularProgressIndicator(
                Modifier.size(PbSize.IconSm),
                color = PbColors.Icon.Inverse,
                strokeWidth = 2.dp,
            )
        }
        Text(
            text ?: stringResource(R.string.insights_scan_reading),
            style = PbTextStyles.Subheadline,
            color = PbColors.Text.Inverse,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun UploadButton(onClick: () -> Unit, modifier: Modifier) {
    Row(
        modifier
            .height(PbSize.ButtonSm)
            .clip(PbShapes.Pill)
            .background(PbColors.Bg.Scrim)
            .clickable(onClick = onClick)
            .padding(horizontal = PbSpace.S12)
            .testTag("scan.upload"),
        horizontalArrangement = Arrangement.spacedBy(PbSpace.S4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PbIconImage(
            PbIcon.Image,
            contentDescription = null,
            size = PbSize.IconSm,
            tint = PbColors.Icon.Inverse,
        )
        Text(
            stringResource(R.string.insights_scan_upload),
            style = PbTextStyles.Footnote,
            color = PbColors.Text.Inverse,
        )
    }
}

/** White status and navigation bar icons over the dark camera, restored on leaving. */
@Composable
private fun LightSystemBars() {
    val view = LocalView.current
    DisposableEffect(view) {
        val window = (view.context as? Activity)?.window
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }
        val status = controller?.isAppearanceLightStatusBars
        val navigation = controller?.isAppearanceLightNavigationBars
        controller?.isAppearanceLightStatusBars = false
        controller?.isAppearanceLightNavigationBars = false
        onDispose {
            status?.let { controller.isAppearanceLightStatusBars = it }
            navigation?.let { controller.isAppearanceLightNavigationBars = it }
        }
    }
}
