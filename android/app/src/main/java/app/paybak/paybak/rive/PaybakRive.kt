package app.paybak.paybak.rive

import android.util.Log
import androidx.annotation.RawRes
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.semantics.clearAndSetSemantics
import app.paybak.paybak.ui.theme.LocalReduceMotion
import app.rive.Artboard
import app.rive.Fit
import app.rive.Result
import app.rive.Rive
import app.rive.RiveFile
import app.rive.RiveFileSource
import app.rive.RivePointerInputMode
import app.rive.RiveResourceClosedException
import app.rive.StateMachine
import app.rive.ViewModelInstance
import app.rive.ViewModelSource
import app.rive.core.RiveWorker
import app.rive.rememberRiveWorkerOrNull
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch

private const val TAG = "PaybakRive"

/** View-model bool present in every Paybak .riv file: true shows the static `Still` pose. */
private const val REDUCE_MOTION_PROPERTY = "reduceMotion"

/**
 * The app-wide [RiveWorker]: one native thread and GPU context shared by every Rive view. Null when
 * it could not be created; [PaybakRive] then keeps its slot empty instead of crashing.
 */
val LocalRiveWorker = staticCompositionLocalOf<RiveWorker?> { null }

/**
 * Provides one [RiveWorker] for the whole app. Call once at the root of `setContent`, so it is
 * ready (and Welcome's file loads fast) by the time Splash finishes. Needs `Rive.init` first, which
 * [app.paybak.paybak.PaybakApplication] does.
 */
@Composable
fun RiveHost(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalRiveWorker provides rememberRiveWorkerOrNull(), content = content)
}

/**
 * Handle to the view-model instance bound to one [PaybakRive]. Valid while that view is composed;
 * afterwards every call is a silent no-op. Call from the main thread.
 */
@Stable
class PaybakRiveController internal constructor(private val instance: ViewModelInstance) {
    fun setNumber(property: String, value: Float) = guard { instance.setNumber(property, value) }

    fun setBool(property: String, value: Boolean) = guard { instance.setBoolean(property, value) }

    /**
     * Fires a view-model trigger, e.g. to replay an animation. Never use it to forward taps: the
     * files' own listeners already fire their tap triggers.
     */
    fun fire(trigger: String) = guard { instance.fireTrigger(trigger) }

    /** Emits each time [trigger] fires, including from the file's own tap listeners. */
    fun observe(trigger: String): Flow<Unit> =
        if (instance.closed) emptyFlow() else instance.getTriggerFlow(trigger)

    private inline fun guard(block: () -> Unit) {
        if (instance.closed) return
        try {
            block()
        } catch (_: RiveResourceClosedException) {
            // Disposed between the check and the call: nothing left to drive.
        }
    }
}

/**
 * Renders the [artboard] of the raw `.riv` [resId], driven by its [stateMachine], with the
 * artboard's default view-model instance bound (auto-bind). Loading, binding and the first values
 * all happen before the first frame, and everything is closed when it leaves composition. It draws
 * only while the Activity is resumed. Decorative for TalkBack.
 *
 * @param modifier Size it to the artboard (see [PaybakRiveAsset.viewSize]).
 * @param pointerInputMode Touches always reach the file's listeners. [RivePointerInputMode.Consume]
 *   also blocks parent gestures; use [RivePointerInputMode.Observe] inside swipes and scrolls.
 * @param reduceMotion Written to `reduceMotion`; follows the system setting by default.
 * @param numbers Number properties kept in sync, e.g. `mapOf("step" to 2f)`.
 * @param observedTriggers Triggers to observe; each fire calls [onTrigger] with its name.
 * @param onReady Receives the controller once per load, before the first frame.
 */
@Composable
fun PaybakRive(
    @RawRes resId: Int,
    artboard: String,
    stateMachine: String,
    modifier: Modifier = Modifier,
    pointerInputMode: RivePointerInputMode = RivePointerInputMode.Consume,
    reduceMotion: Boolean = LocalReduceMotion.current,
    numbers: Map<String, Float> = emptyMap(),
    observedTriggers: List<String> = emptyList(),
    onTrigger: (trigger: String) -> Unit = {},
    onReady: (PaybakRiveController) -> Unit = {},
) {
    val decorative = modifier.clearAndSetSemantics {}
    val worker = LocalRiveWorker.current
    if (worker == null) {
        Box(decorative)
        return
    }

    val currentReduceMotion by rememberUpdatedState(reduceMotion)
    val currentNumbers by rememberUpdatedState(numbers)
    val currentOnReady by rememberUpdatedState(onReady)
    val sceneResult =
        rememberRiveScene(worker, resId, artboard, stateMachine) { controller ->
            controller.setBool(REDUCE_MOTION_PROPERTY, currentReduceMotion)
            currentNumbers.forEach { (name, value) -> controller.setNumber(name, value) }
            currentOnReady(controller)
        }
    val scene = (sceneResult as? Result.Success)?.value
    if (scene == null) {
        // Loading or failed: keep the slot transparent. Failures are logged by rememberRiveScene.
        Box(decorative)
        return
    }

    scene.controller?.let { controller ->
        LaunchedEffect(controller, reduceMotion) {
            controller.setBool(REDUCE_MOTION_PROPERTY, reduceMotion)
        }
        LaunchedEffect(controller, numbers) {
            numbers.forEach { (name, value) -> controller.setNumber(name, value) }
        }
        val currentOnTrigger by rememberUpdatedState(onTrigger)
        LaunchedEffect(controller, observedTriggers) {
            observedTriggers.forEach { name ->
                launch { controller.observe(name).collect { currentOnTrigger(name) } }
            }
        }
    }

    Rive(
        file = scene.file,
        modifier = decorative,
        artboard = scene.artboard,
        stateMachine = scene.stateMachine,
        viewModelInstance = scene.viewModelInstance,
        fit = Fit.Contain(),
        pointerInputMode = pointerInputMode,
    )
}

/** Everything one [PaybakRive] owns; closed together when it leaves composition. */
private class RiveScene(
    val file: RiveFile,
    val artboard: Artboard,
    val stateMachine: StateMachine,
    /** Null only if no instance could be created; Rive then binds its own default. */
    val viewModelInstance: ViewModelInstance?,
    val controller: PaybakRiveController?,
)

/**
 * Loads file → artboard → state machine → view-model instance in one coroutine and calls
 * [onCreated] before publishing, so the first frame already has the right values. A key change
 * returns Loading synchronously, so `Rive()` never sees resources that are about to close.
 */
@Composable
private fun rememberRiveScene(
    worker: RiveWorker,
    @RawRes resId: Int,
    artboardName: String,
    stateMachineName: String,
    onCreated: (PaybakRiveController) -> Unit,
): Result<RiveScene> {
    val resources = LocalResources.current
    return key(worker, resId, artboardName, stateMachineName) {
        produceState<Result<RiveScene>>(Result.Loading) {
                val owned = ArrayList<AutoCloseable>(4)
                try {
                    val file =
                        RiveFile.load(RiveFileSource.RawRes(resId, resources), worker)
                            .also(owned::add)
                    val artboard = Artboard.create(file, artboardName).also(owned::add)
                    val stateMachine =
                        StateMachine.create(artboard, stateMachineName).also(owned::add)
                    val instance = createDefaultViewModelInstance(file, artboard)?.also(owned::add)
                    val controller = instance?.let(::PaybakRiveController)
                    controller?.let(onCreated)
                    value =
                        Result.Success(
                            RiveScene(file, artboard, stateMachine, instance, controller)
                        )
                    awaitDispose { closeAll(owned) }
                } catch (ce: CancellationException) {
                    closeAll(owned)
                    throw ce
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to load $artboardName/$stateMachineName ($resId)", e)
                    closeAll(owned)
                    value = Result.Error(e)
                }
            }
            .value
    }
}

/**
 * The artboard's default view model and its default instance (what other runtimes call auto-bind),
 * falling back to the first authored instance.
 */
private suspend fun createDefaultViewModelInstance(
    file: RiveFile,
    artboard: Artboard,
): ViewModelInstance? {
    val viewModel = ViewModelSource.DefaultForArtboard(artboard)
    return try {
        ViewModelInstance.create(file, viewModel.defaultInstance())
    } catch (ce: CancellationException) {
        throw ce
    } catch (e: Exception) {
        Log.w(
            TAG,
            "No default view-model instance for ${artboard.name}: ${e.message}; trying the first",
        )
        try {
            val viewModelName = file.getDefaultViewModelInfo(artboard).viewModelName
            val first = file.getViewModelInstanceNames(viewModelName).firstOrNull() ?: return null
            ViewModelInstance.create(
                file,
                ViewModelSource.Named(viewModelName).namedInstance(first),
            )
        } catch (ce: CancellationException) {
            throw ce
        } catch (e2: Exception) {
            Log.e(TAG, "No view-model instance for ${artboard.name}", e2)
            null
        }
    }
}

/** Closes in reverse creation order (instance, state machine, artboard, file). */
private fun closeAll(owned: MutableList<AutoCloseable>) {
    for (index in owned.indices.reversed()) {
        try {
            owned[index].close()
        } catch (e: Exception) {
            Log.w(TAG, "Close failed: ${e.message}")
        }
    }
    owned.clear()
}
