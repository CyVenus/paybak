package app.paybak.paybak.feature.scan

import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import app.paybak.paybak.domain.model.ME
import app.paybak.paybak.domain.model.ReceiptResult
import app.paybak.paybak.domain.scan.ReceiptSplit
import app.paybak.paybak.navigation.LocalAppClock
import app.paybak.paybak.navigation.LocalMainNavigator
import app.paybak.paybak.navigation.Route
import app.paybak.paybak.navigation.RouteResult
import app.paybak.paybak.navigation.popTransition
import app.paybak.paybak.navigation.pushTransition
import app.paybak.paybak.navigation.rememberDebugStartScreen
import app.paybak.paybak.service.camera.CameraSource
import app.paybak.paybak.service.receipt.ReceiptPhotos
import app.paybak.paybak.service.receipt.ReceiptReader
import app.paybak.paybak.ui.theme.LocalReduceMotion
import app.paybak.paybak.ui.theme.PbMotion
import kotlinx.coroutines.launch

/**
 * The `scanReceipt` route (scanCamera, scanReview, scanAssign; insights §4): take or upload a photo
 * of the bill, check what was read (Check receipt), tap who had each item (Assign items), then hand
 * the Add expense form that opened it a prefilled itemized expense (`RouteResult.Receipt`) and
 * close. Check receipt and Assign items are pages pushed inside this modal; back pops them. Tagged
 * `screen.scanReceipt`.
 */
@Composable
fun ScanReceiptScreen(route: Route.ScanReceipt) {
    val navigator = LocalMainNavigator.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val today = LocalAppClock.current.today()
    var state by
        rememberSaveable(stateSaver = ScanState.Saver) {
            mutableStateOf(ScanState(people = listOf(ME) + route.personIds.filter { it != ME }))
        }
    var reading by remember { mutableStateOf(false) }

    fun read(photo: Bitmap, saved: String?, then: (ScanState) -> ScanState = { it }) {
        reading = true
        scope.launch {
            val name = saved ?: ReceiptPhotos.save(context, photo)
            val scan = ReceiptReader.read(photo)
            reading = false
            state = then(state.read(name, sample = saved == null && CameraSource.isSimulated, scan))
        }
    }

    val debugStart = rememberDebugStartScreen("scanReview", "scanAssign")
    LaunchedEffect(debugStart) {
        if (debugStart == null) return@LaunchedEffect
        // The designed states: the sample receipt read, and on Assign items its drawn assignment.
        read(CameraSource.simulatedPhoto(context), saved = null) { next ->
            if (debugStart == "scanAssign" && next.scan != null) drawnAssignment(next) else next
        }
    }

    BackHandler(enabled = state.step != ScanStep.Camera) {
        state = state.copy(step = ScanStep.entries[state.step.ordinal - 1])
    }
    val reduceMotion = LocalReduceMotion.current
    Box(Modifier.fillMaxSize().testTag("screen.scanReceipt")) {
        AnimatedContent(
            targetState = state.step,
            transitionSpec = {
                when {
                    reduceMotion ->
                        fadeIn(tween(PbMotion.SWAP_MILLIS)) togetherWith
                            fadeOut(tween(PbMotion.SWAP_MILLIS))
                    targetState.ordinal > initialState.ordinal -> pushTransition()
                    else -> popTransition()
                }
            },
            label = "Scan pages",
        ) { step ->
            when (step) {
                ScanStep.Camera ->
                    ScanCamera(
                        reading = reading,
                        onClose = navigator::dismissModal,
                        onPhoto = { photo, saved -> read(photo, saved) },
                    )
                ScanStep.Review ->
                    ScanReview(
                        state = state,
                        onChange = { state = it },
                        onBack = { state = state.copy(step = ScanStep.Camera) },
                        onConfirm = { state = state.copy(step = ScanStep.Assign) },
                        onAttachOnly = {
                            navigator.complete(
                                route.request.id,
                                RouteResult.Receipt(
                                    ReceiptResult(
                                        ReceiptSplit.attachOnly(state.people),
                                        photo = state.photo,
                                    )
                                ),
                            )
                        },
                    )
                ScanStep.Assign ->
                    ScanAssign(
                        state = state,
                        onChange = { state = it },
                        onBack = { state = state.copy(step = ScanStep.Review) },
                        onContinue = {
                            val scan = state.scan ?: return@ScanAssign
                            navigator.complete(
                                route.request.id,
                                RouteResult.Receipt(
                                    ReceiptResult(
                                        ReceiptSplit.draft(
                                            scan,
                                            state.assigned,
                                            state.people,
                                            today,
                                        ),
                                        scan = scan,
                                        photo = state.photo,
                                    )
                                ),
                            )
                        },
                    )
            }
        }
    }
}

/**
 * Figma's Assign items: Chicken biryani Dev, Paneer tikka Esha, Fish and chips and Chocolate
 * brownie you, the fries and the sodas shared by all three (the demo receipt only).
 */
private fun drawnAssignment(state: ScanState): ScanState {
    val drawn = listOf(listOf("p-dev"), listOf("p-esha"), listOf(ME), listOf(ME))
    val withPeople = state.withPeople(listOf("p-esha", "p-dev"))
    val items = state.scan?.items.orEmpty()
    return withPeople.copy(
        step = ScanStep.Assign,
        assigned = items.indices.map { drawn.getOrNull(it) ?: withPeople.people },
    )
}
