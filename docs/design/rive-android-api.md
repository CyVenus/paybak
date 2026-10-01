# rive-android 11.12.1: verified API and integration spec (Paybak Android)

> **Status:** written before the Android app was built, as the integration guide. The shipped code is `android/app/src/main/java/app/paybak/paybak/PaybakApplication.kt` (calls `Rive.init`) and `.../rive/PaybakRive.kt`, `PaybakRiveAssets.kt`, `PaybakRiveIllustration.kt` (package `rive/`, not `ui/rive/`). Where the app's code differs from the listings below, the app wins; the API facts and gotchas still hold.

Everything below was checked against the published Maven Central artifact `app.rive:rive-android:11.12.1` (AAR, `-sources.jar`, POM, Gradle `.module`; SHA-1s matched Maven's). The recommended wrapper was **compiled with this project's exact toolchain** (AGP 9.4.1 built-in Kotlin 2.2.10, Compose BOM 2026.02.01, compileSdk 37, minSdk 24, Gradle 9.6, JDK 25) in a scratch copy of `android/`. It was also **run on the arm64 16 KB-page emulator** (`sdk_gphone16k_arm64`) with all six Paybak `.riv` files, in both a debug build and an R8-minified release build. The repo itself was not modified.

Sources of truth for the Rive files themselves: `rive.md` (artboards, state machines, view models, triggers). This file only covers the Android runtime.

---

## 0. Decisions (read this first)

| Topic | Decision |
|---|---|
| Version | `app.rive:rive-android:11.12.1`. **Works with Kotlin 2.2.10 as is** (the classes carry Kotlin metadata 1.9.0). Don't change the project's Kotlin version. |
| API | Use the **Compose-native API in package `app.rive`** (`Rive(...)` composable, `RiveFile`, `Artboard`, `StateMachine`, `ViewModelInstance`). The classic `RiveAnimationView` is `@Deprecated` in 11.x and frees its native file only when the Activity is destroyed. Don't use it. |
| Init | **Must call `app.rive.runtime.kotlin.core.Rive.init(context)` once** (Application.onCreate). The androidx.startup initializer is **not** merged automatically (the AAR's own manifest removes it). Without init, every illustration silently stays blank (verified). |
| Worker | **One app-wide `RiveWorker`** (native thread + GL context), provided at the root with `RiveHost { … }` / `LocalRiveWorker`. |
| Wrapper | `PaybakRive(resId, artboard, stateMachine, modifier, …)` in `ui/rive/PaybakRive.kt` (§8). It loads the file, creates the named artboard and state machine, creates and binds the artboard's **default view-model instance**, and writes `reduceMotion` (system "Remove animations") and any numbers before the first frame. It observes triggers for haptics and closes everything when it leaves composition. |
| Data binding | `ViewModelInstance.create(file, ViewModelSource.DefaultForArtboard(artboard).defaultInstance())`, then pass it as `Rive(viewModelInstance = …)`. Setters: `setNumber`, `setBoolean`, `fireTrigger`. Observe: `getTriggerFlow(name): Flow<Unit>`. Verified on all six files, including the ones whose only instance is named `Instance`. |
| Touch | Forwarded to the state machine **by default** (verified: taps fire the files' own listeners and their triggers). `pointerInputMode = Consume` (default) or `Observe` (when a parent must also scroll or swipe). |
| Fit | `app.rive.Fit.Contain(app.rive.Alignment.Center)`, which is also the library default. Size the composable to the **artboard** size. For GetStarted, Notifications and HomeAllSquare the artboard is the Figma slot + 12 dp per side: reserve the slot and draw the view centred on it with `requiredSize` (editor's note, see rive.md and the screen specs). |
| Watermark | (Editor, verified on the Android emulator.) 11.12.1 does **not** play the ~2 s black "RIVE" pre-roll that rive-ios 6.28.0 plays for these watermarked files. The Get Started art was drawing about 1.1 s after launch. Re-exported clean files are still needed for iOS. |
| R8 | No app rules needed (the AAR ships `-keep class app.rive.** { *;}` as consumer rules; the minified release build was verified at runtime). |

---

## 1. Artifact facts

Downloaded (build-session scratch, not kept): `rive-android-11.12.1.{pom,module,aar}` and `rive-android-11.12.1-sources.jar`, unpacked into `aar/`, `classes/` and `src/`. Published 2026-09-16. Version 11.12.1 is the latest (the metadata lists 11.0.0 … 11.12.1).

### 1.1 Dependencies (from the Gradle `.module`; the POM says the same)
API scope (on the app's compile classpath): only `org.jetbrains.kotlin:kotlin-stdlib:1.9.25`.
Runtime scope (packaged, **not** on your compile classpath):

| Dependency | Version | Note |
|---|---|---|
| `androidx.compose:compose-bom` (platform) | 2025.11.01 | Our BOM 2026.02.01 wins. Resolved Compose ui/runtime = **1.10.4**. |
| `androidx.compose.runtime:runtime`, `androidx.compose.ui:ui`, `ui-android` | from BOM | |
| `androidx.core:core-ktx` | 1.17.0 | **Upgrades the project's core-ktx 1.10.1 → 1.17.0** (needs compileSdk ≥ 36; ours is 37, and checkAarMetadata passed). |
| `androidx.lifecycle:lifecycle-runtime-ktx` | 2.9.4 | Upgrades the project's 2.6.1 → 2.9.4. Rive uses kotlinx-coroutines but doesn't declare it. It arrives transitively (lifecycle/Compose), so nothing needs adding. |
| `androidx.lifecycle:lifecycle-runtime-compose` | 2.9.4 | Runtime only. If **app** code imports `androidx.lifecycle.compose.*`, add it explicitly. The wrapper doesn't need it. |
| `androidx.customview:customview` | 1.1.0 | |
| `androidx.startup:startup-runtime` | 1.2.0 | Only for the optional `RiveInitializer` (§3). |
| `com.getkeepsafe.relinker:relinker` | 1.4.5 | Native-load fallback below API 23 only. |
| `com.android.volley:volley` | 1.2.1 | Legacy URL loading only. |

Resolved in our build: `kotlin-stdlib 1.9.25 -> 2.2.10`, `core-ktx 1.10.1 -> 1.17.0`, `lifecycle-runtime-ktx 2.6.1 -> 2.9.4`.
Optional tidy-up: set `coreKtx = "1.17.0"` and `lifecycleRuntimeKtx = "2.9.4"` in the catalog so the declared versions match the resolved ones. That changes nothing at runtime.

### 1.2 AAR manifest (verbatim)
```xml
<manifest package="app.rive.runtime.kotlin">
    <uses-sdk android:minSdkVersion="21" />
    <application>
        <provider android:name="androidx.startup.InitializationProvider"
            android:authorities="${applicationId}.androidx-startup"
            android:exported="false" tools:node="merge">
            <meta-data android:name="app.rive.runtime.kotlin.RiveInitializer"
                android:value="androidx.startup" tools:node="remove" />
        </provider>
    </application>
</manifest>
```
- **minSdk 21**, which is below ours (24). OK.
- The `RiveInitializer` meta-data is marked **`tools:node="remove"`**, so it is **not** in the merged manifest. That's verified in `processDebugMainManifest`: the startup provider contains only EmojiCompat, ProcessLifecycle and ProfileInstaller. Manifest merge prints a harmless warning: `meta-data#app.rive.runtime.kotlin.RiveInitializer was tagged at AndroidManifest.xml:0 to remove other declarations but no other declaration present`.
- There are no permissions.

### 1.3 Consumer ProGuard/R8 rules (`proguard.txt` in the AAR, verbatim)
```
-keep class app.rive.** { *;}
```
AGP applies this automatically. **No app-side keep rules are needed.** Verified: the release build with `optimization { enable = true }` passed `minifyReleaseWithR8` and ran correctly on device (load, bind, tap triggers).

### 1.4 Native libraries (`jni/` in the AAR)
| ABI | librive-android.so | libc++_shared.so | ELF LOAD alignment |
|---|---|---|---|
| arm64-v8a | 5,336,624 B | 1,292,904 B | **16 KB** |
| x86_64 | 5,712,008 B | 1,252,080 B | **16 KB** |
| armeabi-v7a | 5,088,632 B | 872,872 B | 4 KB (32-bit, 16 KB not required) |
| x86 | 5,690,568 B | 1,254,988 B | 4 KB (32-bit) |

- Android 15+ 16 KB page-size devices: compatible (verified running on a 16 KB arm64 emulator).
- APK size: a universal APK gains ≈ 27 MB of uncompressed native code across 4 ABIs (our debug APK was 39 MB; the R8 release APK was 28 MB). An AAB delivered through Play ships one ABI (≈ 6.6 MB on arm64).
- The build prints `Unable to strip the following libraries, packaging them as they are: libandroidx.graphics.path.so, libc++_shared.so, librive-android.so`. That's harmless.

---

## 2. Kotlin metadata compatibility (Kotlin 2.2.10)

- `META-INF/kotlin_release.kotlin_module` header = metadata version **1.9.0**.
- `javap -v` on **all 927 classes** in `classes.jar`: every one has `kotlin.Metadata mv=[1,9,0]` and class-file **major version 55 (Java 11)**.
- A Kotlin 2.2.10 compiler reads any metadata ≤ 2.3.x, so 1.9.0 is fine. **No compatibility problem. Don't bump Kotlin or downgrade Rive.** Verified by compiling the wrapper with the project's AGP 9.4.1 built-in Kotlin 2.2.10: `BUILD SUCCESSFUL` with **zero warnings** (no deprecation warnings either, because only non-deprecated APIs are used).
- The bytecode targets Java 11, which matches the project's `sourceCompatibility/targetCompatibility = VERSION_11`.
- Compose: the library was built against BOM 2025.11.01. We resolve Compose 1.10.4 from BOM 2026.02.01, which is binary compatible (verified at runtime).

---

## 3. Gradle setup (exact edits)

`android/gradle/libs.versions.toml`:
```toml
[versions]
# … existing …
rive = "11.12.1"

[libraries]
# … existing …
rive-android = { group = "app.rive", name = "rive-android", version.ref = "rive" }
```

`android/app/build.gradle.kts`, in `dependencies { … }`:
```kotlin
implementation(libs.rive.android)
```
The repositories already include `mavenCentral()` (settings.gradle.kts). Nothing else is required.

- **packaging / jniLibs**: nothing required. Add `packaging { jniLibs { pickFirsts += "**/libc++_shared.so" } }` **only if** another native dependency is added later that also ships `libc++_shared.so` (the error would be `2 files found with path 'lib/<abi>/libc++_shared.so'`). Leave `useLegacyPackaging` at its default (false). With minSdk 24 the libs are stored uncompressed and page-aligned.
- **ABI filters**: optional. Don't filter release (32-bit devices need armeabi-v7a). To speed up local debug installs only:
  ```kotlin
  buildTypes { debug { ndk { abiFilters += listOf("arm64-v8a", "x86_64") } } }
  ```
- **R8**: no rules (§1.3). The project's release build currently has `optimization { enable = false }`. Enabling it later needs no Rive changes.

### Manifest / Application (required)
There is no `Application` class yet. Add `PaybakApplication` (§8.2) and register it:
```xml
<application
    android:name=".PaybakApplication"
    … existing attributes … >
```

---

## 4. Initialization

`object app.rive.runtime.kotlin.core.Rive` (not deprecated):
```kotlin
fun init(context: Context, defaultRenderer: RendererType = RendererType.Rive)   // throws UnsatisfiedLinkError
@JvmStatic fun initializeCppEnvironment()                                     // only if you load the .so yourself
```
`init` loads `rive-android` (API ≥ 23 uses `System.loadLibrary`; the lib's `DT_NEEDED` pulls in `libc++_shared`). Then it calls `cppInitialize()` to set up JNI.

- **Nothing in the Compose API (`app.rive.*`) calls it.** Only `app/rive/runtime/kotlin/core/Rive.class` contains `loadLibrary` (checked in the bytecode).
- Verified failure mode without `Rive.init`: `E/Rive/Worker: Failed to create Rive worker: No implementation found for long app.rive.core.RenderContextGL.cppConstructor(long, long) … is the library loaded`. `rememberRiveWorkerOrNull()` returns null, so illustrations are blank and the app doesn't crash. `rememberRiveWorker()` (non-null variant) would throw `RiveInitializationException` instead.
- `androidx.startup` alternative: `class app.rive.runtime.kotlin.RiveInitializer : Initializer<Unit>` (`create = Rive.init(context)`, no dependencies). It is NOT auto-registered. You could add `<meta-data android:name="app.rive.runtime.kotlin.RiveInitializer" android:value="androidx.startup"/>` inside your own `InitializationProvider` entry (with `tools:node="merge"`), or call `AppInitializer.getInstance(ctx).initializeComponent(RiveInitializer::class.java)`. That path wasn't tested, and the Application approach is simpler. **Use `Rive.init(this)` in `Application.onCreate()`.**
- Name clash: the composable `app.rive.Rive(...)` and the object `app.rive.runtime.kotlin.core.Rive` share a simple name. Keep `Rive.init` in `PaybakApplication.kt` (which doesn't import the composable). Otherwise use `import app.rive.runtime.kotlin.core.Rive as RiveRuntime`.
- Logging: `app.rive.RiveLog.logger` defaults to a no-op. Set `RiveLog.logger = RiveLog.LogcatLogger()` in debuggable builds (done in §8.2). Tags: `Rive/File`, `Rive/Artboard`, `Rive/StateMachine`, `Rive/VMI`, `Rive/UI`, `Rive/CQ`, `Rive/Worker`.

---

## 5. Compose-native API (`package app.rive`): exact signatures

All resources are `CheckableAutoCloseable` (`val closed: Boolean`, `fun close()`; close is idempotent, and a second close only logs a warning). Resources belong to the `RiveWorker` that loaded the file and must not be mixed across workers.

### 5.1 RiveWorker (`typealias app.rive.core.RiveWorker = app.rive.core.CommandQueue`)
One worker = one native "command server" thread + one render context (OpenGL by default). All operations are async commands. Resource creation returns handles. It's ref-counted: files, surfaces and assets acquire it. Max 32 concurrent subscribers per worker (`MAX_CONCURRENT_SUBSCRIBERS`), which is plenty.
```kotlin
@Composable @Throws(RiveInitializationException::class)
fun rememberRiveWorker(autoPoll: Boolean = true, tracingEnabled: Boolean = false,
                       renderBackend: RenderBackend = RenderBackend.OpenGL): RiveWorker
@Composable
fun rememberRiveWorkerOrNull(errorState: MutableState<Throwable?> = mutableStateOf(null),
                             autoPoll: Boolean = true, tracingEnabled: Boolean = false,
                             renderBackend: RenderBackend = RenderBackend.OpenGL): RiveWorker?
enum class RenderBackend { Vulkan /* API ≥ 29, falls back to GL */, OpenGL }
```
The composables poll the worker once per frame while `LocalLifecycleOwner` is **RESUMED** (`beginPolling`), and release the worker's reference on dispose. Callbacks such as property and trigger updates are delivered on the main thread during polling.

### 5.2 RiveFile
```kotlin
@Stable class RiveFile : CheckableAutoCloseable {
  val fileHandle: FileHandle; val riveWorker: RiveWorker
  companion object {
    suspend fun load(source: RiveFileSource, riveWorker: RiveWorker): RiveFile   // throws RiveFileException, RiveResourceClosedException, Resources.NotFoundException, IOException
    @Deprecated suspend fun fromSource(source, riveWorker): Result<RiveFile>     // removed in 12.0
  }
  suspend fun getArtboardNames(): List<String>
  suspend fun getViewModelNames(): List<String>
  suspend fun getViewModelInstanceNames(viewModel: String): List<String>
  suspend fun getViewModelProperties(viewModel: String): List<ViewModel.Property>
  suspend fun getEnums(): List<File.Enum>
  suspend fun getFileAssets(): List<RiveFileAsset>
  suspend fun getDefaultViewModelInfo(artboard: Artboard): app.rive.core.DefaultViewModelInfo  // data class(viewModelName, instanceName)
}
sealed interface RiveFileSource {
  @JvmInline value class Bytes(val data: ByteArray)
  data class RawRes(@RawRes val resId: Int, val resources: Resources) {
    companion object { @Composable fun from(@RawRes resId: Int): RawRes }   // uses LocalContext
  }
}
@Composable fun rememberRiveFile(source: RiveFileSource, riveWorker: RiveWorker): Result<RiveFile>  // closes on dispose
```
Raw resources are read on `Dispatchers.IO`.

### 5.3 Artboard
```kotlin
class Artboard : CheckableAutoCloseable {
  val artboardHandle: ArtboardHandle; val name: String?
  companion object { suspend fun create(file: RiveFile, artboardName: String? = null): Artboard }  // null = default artboard
  suspend fun getStateMachineNames(): List<String>
  fun resizeArtboard(surface: RiveSurface, scaleFactor: Float = 1f)   // Fit.Layout only
  fun resetArtboardSize(); fun setVolume(volume: Float); suspend fun getVolume(): Float
}
@Composable fun rememberArtboardResult(file: RiveFile, artboardName: String? = null): Result<Artboard>
@Deprecated @Composable fun rememberArtboard(file, artboardName): Artboard   // don't use; removed in 12.0
```
**Always pass the artboard name.** Each Paybak file also contains the `Watermark`, `NuRiveBrandmark01` and `NuRiveWordmark01` artboards (see rive.md).

### 5.4 StateMachine
```kotlin
class StateMachine : CheckableAutoCloseable {
  val stateMachineHandle: StateMachineHandle; val name: String?
  companion object { suspend fun create(artboard: Artboard, stateMachineName: String? = null): StateMachine }
  fun advance(deltaTime: kotlin.time.Duration)     // manual advance; Rive() does this for you
}
@Composable fun rememberStateMachineResult(artboard: Artboard, stateMachineName: String? = null): Result<StateMachine>
```
Don't share one `StateMachine` between two `Rive()` composables. Each renderer needs its own artboard and state machine.

### 5.5 ViewModelInstance (data binding)
```kotlin
class ViewModelInstance : CheckableAutoCloseable {
  val instanceHandle: ViewModelInstanceHandle
  companion object {
    suspend fun create(file: RiveFile, source: ViewModelInstanceSource): ViewModelInstance  // throws RiveFileException, …
    @Deprecated fun fromFile(file, source): ViewModelInstance                                // removed in 12.0
  }
  suspend fun getViewModelName(): String; suspend fun getName(): String
  // setters: fire-and-forget; take effect on the next state-machine advance; also unsettle the bound Rive()
  fun setNumber(propertyPath: String, value: Float)
  fun setBoolean(propertyPath: String, value: Boolean)
  fun setString(propertyPath: String, value: String)
  fun setEnum(propertyPath: String, value: String)
  fun setColor(propertyPath: String, @ColorInt value: Int)
  fun fireTrigger(propertyPath: String)
  fun setImage(propertyPath: String, image: ImageAsset?)
  fun setArtboard(propertyPath: String, artboard: Artboard?)
  fun setViewModelInstance(propertyPath: String, instance: ViewModelInstance)
  // cold flows, cached per path; first emission = current value (not for triggers); distinctUntilChanged; complete when closed
  fun getNumberFlow(propertyPath: String): Flow<Float>
  fun getBooleanFlow(propertyPath: String): Flow<Boolean>
  fun getStringFlow(propertyPath: String): Flow<String>
  fun getEnumFlow(propertyPath: String): Flow<String>
  fun getColorFlow(propertyPath: String): Flow<Int>
  fun getTriggerFlow(propertyPath: String): Flow<Unit>        // one Unit per fire (buffer 32, drop oldest)
  // lists: getListSize, insertToListAtIndex, appendToList, removeFromListAtIndex, removeFromList, swapListItems
}
sealed interface ViewModelSource {                       // which view model
  @JvmInline value class Named(val viewModelName: String)
  @JvmInline value class DefaultForArtboard(val artboard: Artboard)
  fun blankInstance(): ViewModelInstanceSource            // zeroed values; DON'T use (step would be 0)
  fun defaultInstance(): ViewModelInstanceSource          // the instance marked default in the editor
  fun namedInstance(instanceName: String): ViewModelInstanceSource
}
sealed interface ViewModelInstanceSource { Blank, Default, Named(vmSource, instanceName), Reference(parent, path), ReferenceListItem(parent, pathToList, index) }
@Composable fun rememberViewModelInstanceResult(file: RiveFile, source: ViewModelInstanceSource? = null): Result<ViewModelInstance>
@Deprecated @Composable fun rememberViewModelInstance(file, source): ViewModelInstance   // removed in 12.0
```
Property paths are slash-delimited for nested properties. Paybak only uses top-level names: `step`, `reduceMotion`, and the triggers in rive.md. Errors from setters (wrong name or type) arrive asynchronously in the log and aren't thrown. Getters and flows throw `RiveViewModelInstanceException` for a bad path.

### 5.6 The `Rive` composable
```kotlin
@Composable
fun Rive(
    file: RiveFile,
    modifier: Modifier = Modifier,
    playing: Boolean = true,                       // false = don't advance with time (input still evaluated)
    artboard: Artboard? = null,                    // null = default artboard (created implicitly; DON'T rely on it)
    stateMachine: StateMachine? = null,            // must come from `artboard`; requires `artboard`
    viewModelInstance: ViewModelInstance? = null,  // null = Rive creates & binds the default instance itself, but you can't reach it
    fit: Fit = Fit.Contain(),                      // Alignment.Center
    backgroundColor: Int = Color.TRANSPARENT,
    pointerInputMode: RivePointerInputMode = RivePointerInputMode.Consume,
    frameRate: RiveFrameRate = RiveFrameRate.Unbounded,    // or RiveFrameRate.Capped(fps)
    semantics: RiveSemanticsMode = RiveSemanticsMode.Off,  // On/Automatic need @ExperimentalRiveSemantics
    onBitmapAvailable: ((getBitmap: () -> Bitmap) -> Unit)? = null,
)   // throws RiveResourceClosedException / RiveIncompatibleResourceException if resources are closed/mismatched
```
Behaviour, from the source and verified on device:
- It renders into a non-opaque `TextureView` (`isOpaque = false`), so the background is transparent. It measures like any single-child layout, so give it a size through `modifier`.
- It **always binds** the state machine. If you pass `viewModelInstance`, it calls `setMainViewModelInstance` and `bind`. It re-binds when the instance changes, and every setter call on that instance "unsettles" the state machine so the next frame applies it.
- It draws on `withFrameNanos` only while the lifecycle is **RESUMED** and the state machine is **not settled**. A settled machine (such as the `reduceMotion` "Still" state) costs nothing. Idle loops never settle, so they draw every frame while on screen.
- Before the artboard and state machine are ready, the surface is blank. There's no placeholder API; the wrapper shows an empty `Box` of the same size.
- `enum class RivePointerInputMode { Consume, Observe, PassThrough }` (see §7).
- `sealed class Fit { Layout(scaleFactor), Contain(alignment), ScaleDown(alignment), Cover(alignment), FitWidth(alignment), FitHeight(alignment), Fill, None(alignment) }`, all with `alignment: Alignment = Alignment.Center`.
- `enum class Alignment { TopLeft, TopCenter, TopRight, CenterLeft, Center, CenterRight, BottomLeft, BottomCenter, BottomRight }`.
- **Use `app.rive.Fit` / `app.rive.Alignment`**, not the legacy enums `app.rive.runtime.kotlin.core.Fit.CONTAIN` / `Alignment.CENTER`, which exist only for `RiveAnimationView`. Watch out for Compose's `androidx.compose.ui.Alignment` name clash: import `app.rive.Alignment` explicitly, or write `Fit.Contain()`, which already centres.

### 5.7 Result and errors
```kotlin
sealed interface Result<out T> { object Loading; data class Error(val throwable: Throwable); data class Success<T>(val value: T)
  @Composable fun <R> andThen(onSuccess: @Composable (T) -> Result<R>): Result<R>; fun <R> map(…); fun zip(…) }
```
Exceptions (package `app.rive`): `RiveInitializationException`, `RiveFileException`, `RiveArtboardException`, `RiveStateMachineException`, `RiveViewModelInstanceException`, `RiveResourceClosedException : IllegalStateException`, `RiveIncompatibleResourceException : IllegalArgumentException`, `RiveRenderException`, `RiveShutdownException`.

---

## 6. Data binding: verified behaviour on the six files

| Check (on device) | Result |
|---|---|
| `ViewModelInstance.create(file, DefaultForArtboard(artboard).defaultInstance())` for **all 6 files**, including All Set and First Day, whose only instance is named `Instance` | Created every time (`Created ViewModelInstanceHandle(n) from source: Default(...)`). The fallback in the wrapper never triggered. |
| Onboarding: `setNumber("step", 1f→2f→3f→1f)` every 4 s | The illustration changed per step. `getNumberFlow("step")` emitted `1.0, 2.0, 3.0`. |
| `setBoolean("reduceMotion", true)` before the first frame (All Set) | A static pose: 0 differing pixel rows between frames 1.5 s apart. With `false`: 854 differing rows (animating). |
| System "Remove animations" toggled live (`settings put global animator_duration_scale 0`) | `rememberSystemReduceMotion()` picked it up through its ContentObserver, and the illustration went static. The setting was restored afterwards. |
| Tapping people in Get Started (left, middle, right) | `getTriggerFlow` emitted `personTapped` + `tapLeft` / `tapMiddle` / `tapRight` for each tap. Tapping outside emitted nothing. |
| Background (Home) and resume | Polling and drawing restarted, and taps still worked. |
| Leaving composition (Back) | Closed in order: surface → VMI → state machine → artboard → file → worker shutdown. No double-close warnings, no crash. |
| Six `PaybakRive`s on one worker in a `verticalScroll` column (`Observe`) | All loaded and rendered. A swipe that started on an illustration still scrolled the column. |
| Cold start → first frame (emulator) | ≈ 195 ms from worker/EGL creation to the first draw (≈ 105 ms from file load to the first draw). |

**Haptics rule:** observe only the generic trigger (`personTapped`, `bellTapped`, `characterTapped`, `tapped`). A tap fires the generic trigger **and** the specific one, so observing both would double-buzz. **Never** call `fireTrigger` for taps yourself (the file's listeners already do). `fire()` is only for replaying an animation programmatically.

---

## 7. Touch

- **Pointer events are forwarded to the state machine by default.** `Rive()` installs a `PointerInputFilter` that sends `Press → pointerDown`, `Move → pointerMove`, `Release → pointerUp + pointerExit` and `Exit → pointerExit` for every pointer, mapped through `fit` into artboard space. No extra code is needed for the files' listeners.
- `RivePointerInputMode.Consume` (default): events are consumed, so parent `scroll`/`pointerInput` detectors don't get drags that start on the illustration. Use it for fixed, non-scrolling screens (Get Started, Setup 4, All set).
- `RivePointerInputMode.Observe`: Rive gets the events without consuming them, so the parent still scrolls or swipes. Use it for **Onboarding** (the parent handles the horizontal swipe; the file has no listeners anyway) and for **Home** illustrations inside the scrolling content. Trade-off: a drag that starts and ends on a character may also count as a tap for Rive.
- `RivePointerInputMode.PassThrough`: also shares events with siblings underneath. It isn't needed here.
- Legacy view equivalent: `RiveAnimationView.touchPassThrough` (default false, meaning it absorbs touches) and `multiTouchEnabled` (default false).

---

## 8. Recommended implementation (copy these files)

The verified sources (listed in §8.2–§8.4) were meant to be copied to the paths below. The app ended up with them in package `rive/` instead of `ui/rive/` (see the status note at the top):
- `android/app/src/main/java/app/paybak/paybak/PaybakApplication.kt` (and register it in the manifest, §3)
- `android/app/src/main/java/app/paybak/paybak/ui/rive/PaybakRive.kt`
- `android/app/src/main/java/app/paybak/paybak/ui/rive/PaybakRiveAssets.kt`
- `android/app/src/main/java/app/paybak/paybak/ui/rive/RiveSamples.kt` (optional; example usages)

and put the `.riv` files in `android/app/src/main/res/raw/` as `paybak_onboarding.riv`, `paybak_getstarted.riv`, `paybak_notifications.riv`, `paybak_allset.riv`, `paybak_homefirstday.riv`, `paybak_home_allset.riv` (see rive.md).

Then wrap the root content once:
```kotlin
setContent {
    RiveHost {            // one RiveWorker for the whole app; created during Splash, so Welcome's file loads fast
        PaybakTheme { /* NavHost … */ }
    }
}
```

### 8.1 Why a custom loader instead of chaining `rememberRiveFile` → `rememberArtboardResult` → …
Both approaches use the same public suspend APIs, and both compile (the helper chain is in §8.5). The wrapper's single `produceState` loader:
- creates **file → artboard → state machine → VM instance in one coroutine**, and writes `reduceMotion`/`step` **before** publishing the scene, so the first drawn frame is already correct (no one-frame flash of the animated or step-1 pose);
- falls back to the first authored instance if a file has no "Default" instance;
- closes everything in reverse order when it leaves composition (or when a key changes, returning `Loading` synchronously through `key(...)` so `Rive()` is never composed with closed resources);
- keeps the slot's size while loading or on error, logs the error, and never crashes the screen.

### 8.2 PaybakApplication.kt
```kotlin
package app.paybak.paybak

import android.app.Application
import android.content.pm.ApplicationInfo
import app.rive.RiveLog
import app.rive.runtime.kotlin.core.Rive

class PaybakApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Loads libc++_shared.so + librive-android.so and initialises JNI. REQUIRED before any
        // app.rive API: RiveWorker creation calls native code. It is NOT automatic: the AAR's
        // manifest removes its androidx.startup RiveInitializer entry. Without it,
        // rememberRiveWorkerOrNull() returns null and every PaybakRive stays blank.
        Rive.init(this)
        // Rive's own logging is a no-op by default; turn it on for debuggable builds only.
        if (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0) {
            RiveLog.logger = RiveLog.LogcatLogger()
        }
    }
}
```

### 8.3 PaybakRive.kt (complete)
```kotlin
package app.paybak.paybak.ui.rive

import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.annotation.RawRes
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import app.rive.Alignment
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

/** View-model property present in every Paybak .riv file. */
const val RIVE_REDUCE_MOTION = "reduceMotion"

// ---------------------------------------------------------------------------------------------
// App-wide worker
// ---------------------------------------------------------------------------------------------

/**
 * The single app-wide [RiveWorker] (one native thread + one GPU context for every Rive view).
 * Null while not provided or when the worker could not be created (e.g. no usable GPU); in that
 * case [PaybakRive] draws nothing but keeps its layout slot.
 */
val LocalRiveWorker = staticCompositionLocalOf<RiveWorker?> { null }

/**
 * Provide one [RiveWorker] for the whole app. Call once, directly inside `setContent { … }`
 * (above the NavHost), so every screen shares it. Requires `Rive.init(context)` to have run first
 * (see PaybakApplication).
 */
@Composable
fun RiveHost(content: @Composable () -> Unit) {
    val worker = rememberRiveWorkerOrNull()
    CompositionLocalProvider(LocalRiveWorker provides worker, content = content)
}

// ---------------------------------------------------------------------------------------------
// Controller
// ---------------------------------------------------------------------------------------------

/**
 * Imperative handle to the bound view-model instance of one [PaybakRive]. Valid while that
 * [PaybakRive] is in composition; afterwards every call is a silent no-op. Call from the main
 * thread.
 */
@Stable
class PaybakRiveController internal constructor(private val instance: ViewModelInstance) {
    /** False once the owning [PaybakRive] left composition. */
    val isActive: Boolean get() = !instance.closed

    fun setNumber(property: String, value: Float) = guard { instance.setNumber(property, value) }

    fun setBool(property: String, value: Boolean) = guard { instance.setBoolean(property, value) }

    /** Fires a view-model trigger (e.g. to replay a tap animation programmatically). */
    fun fire(trigger: String) = guard { instance.fireTrigger(trigger) }

    /**
     * Emits Unit every time [trigger] fires, including when the .riv's own listeners fire it on
     * tap. Completes when the instance is closed.
     */
    fun triggerFlow(trigger: String): Flow<Unit> =
        if (instance.closed) emptyFlow() else instance.getTriggerFlow(trigger)

    fun numberFlow(property: String): Flow<Float> =
        if (instance.closed) emptyFlow() else instance.getNumberFlow(property)

    fun boolFlow(property: String): Flow<Boolean> =
        if (instance.closed) emptyFlow() else instance.getBooleanFlow(property)

    private inline fun guard(block: () -> Unit) {
        if (instance.closed) return
        try {
            block()
        } catch (_: RiveResourceClosedException) {
            // Worker or instance was disposed between the check and the call; nothing to do.
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Composable
// ---------------------------------------------------------------------------------------------

/**
 * Renders [artboard] of the raw .riv resource [resId], driven by [stateMachine], with the
 * artboard's default view-model instance bound (data binding).
 *
 * @param modifier Give it the Figma slot size, e.g. `Modifier.size(362.dp, 340.dp)` (or
 *    `fillMaxWidth().aspectRatio(w / h)`).
 * @param fit Defaults to contain + center.
 * @param pointerInputMode [RivePointerInputMode.Consume] (default) gives taps to the .riv's own
 *    listeners and blocks parent gestures. Use [RivePointerInputMode.Observe] when a parent must
 *    also get the gesture (Onboarding swipe, illustrations inside a scrolling column).
 * @param reduceMotion Written to the `reduceMotion` bool before the first frame and on change.
 * @param numbers Number properties to keep in sync (e.g. `mapOf("step" to 2f)`); written before
 *    the first frame and whenever the map changes.
 * @param observedTriggers Trigger properties to observe; each emission calls [onTrigger].
 * @param viewModelInstanceName Null = the artboard's default view model + its "Default"
 *    instance (auto-bind). Pass a name only to force a specific authored instance.
 * @param contentDescription Null = decorative (hidden from TalkBack).
 * @param onReady Called once per load with the controller, before the first frame is drawn.
 * @param playing False pauses time-based advancement (input is still processed). Not needed for
 *    lifecycle: Rive already stops drawing when the Activity is not RESUMED.
 */
@Composable
fun PaybakRive(
    @RawRes resId: Int,
    artboard: String,
    stateMachine: String,
    modifier: Modifier = Modifier,
    fit: Fit = Fit.Contain(Alignment.Center),
    pointerInputMode: RivePointerInputMode = RivePointerInputMode.Consume,
    reduceMotion: Boolean = rememberSystemReduceMotion(),
    numbers: Map<String, Float> = emptyMap(),
    observedTriggers: List<String> = emptyList(),
    onTrigger: (trigger: String) -> Unit = {},
    viewModelInstanceName: String? = null,
    contentDescription: String? = null,
    onReady: (PaybakRiveController) -> Unit = {},
    playing: Boolean = true,
) {
    val a11y = if (contentDescription == null) {
        Modifier.clearAndSetSemantics {}
    } else {
        Modifier.clearAndSetSemantics {
            this.contentDescription = contentDescription
            role = Role.Image
        }
    }
    val worker = LocalRiveWorker.current
    if (worker == null) {
        Box(modifier.then(a11y))
        return
    }

    val currentReduceMotion by rememberUpdatedState(reduceMotion)
    val currentNumbers by rememberUpdatedState(numbers)
    val currentOnReady by rememberUpdatedState(onReady)

    val sceneResult = rememberRiveScene(
        worker = worker,
        resId = resId,
        artboardName = artboard,
        stateMachineName = stateMachine,
        viewModelInstanceName = viewModelInstanceName,
    ) { controller ->
        // Runs after creation, before the scene is published => before the first advance/draw.
        controller.setBool(RIVE_REDUCE_MOTION, currentReduceMotion)
        currentNumbers.forEach { (name, value) -> controller.setNumber(name, value) }
        currentOnReady(controller)
    }

    val scene = (sceneResult as? Result.Success)?.value
    if (scene == null) {
        // Loading or failed: keep the slot (transparent). Errors are logged in rememberRiveScene.
        Box(modifier.then(a11y))
        return
    }

    val controller = scene.controller
    if (controller != null) {
        LaunchedEffect(controller, reduceMotion) {
            controller.setBool(RIVE_REDUCE_MOTION, reduceMotion)
        }
        LaunchedEffect(controller, numbers) {
            numbers.forEach { (name, value) -> controller.setNumber(name, value) }
        }
        val currentOnTrigger by rememberUpdatedState(onTrigger)
        LaunchedEffect(controller, observedTriggers) {
            observedTriggers.forEach { name ->
                launch { controller.triggerFlow(name).collect { currentOnTrigger(name) } }
            }
        }
    }

    Rive(
        file = scene.file,
        modifier = modifier.then(a11y),
        playing = playing,
        artboard = scene.artboard,
        stateMachine = scene.stateMachine,
        viewModelInstance = scene.viewModelInstance,
        fit = fit,
        pointerInputMode = pointerInputMode,
    )
}

// ---------------------------------------------------------------------------------------------
// Loading
// ---------------------------------------------------------------------------------------------

/** Everything one [PaybakRive] owns. All of it is closed together when it leaves composition. */
private class RiveScene(
    val file: RiveFile,
    val artboard: Artboard,
    val stateMachine: StateMachine,
    /** Null only if no view-model instance could be created (then Rive binds its own default). */
    val viewModelInstance: ViewModelInstance?,
    val controller: PaybakRiveController?,
)

@Composable
private fun rememberRiveScene(
    worker: RiveWorker,
    @RawRes resId: Int,
    artboardName: String,
    stateMachineName: String,
    viewModelInstanceName: String?,
    onCreated: (PaybakRiveController) -> Unit,
): Result<RiveScene> {
    val resources = LocalContext.current.resources
    // key(...) makes a key change return Loading synchronously, so Rive() is never composed with
    // resources that the previous producer is about to close.
    return key(worker, resId, artboardName, stateMachineName, viewModelInstanceName) {
        produceState<Result<RiveScene>>(Result.Loading) {
            val owned = ArrayList<AutoCloseable>(4)
            try {
                val file = RiveFile.load(RiveFileSource.RawRes(resId, resources), worker)
                    .also(owned::add)
                val artboard = Artboard.create(file, artboardName).also(owned::add)
                val stateMachine = StateMachine.create(artboard, stateMachineName).also(owned::add)
                val instance = createViewModelInstance(file, artboard, viewModelInstanceName)
                    ?.also(owned::add)
                val controller = instance?.let(::PaybakRiveController)
                controller?.let(onCreated)
                value = Result.Success(RiveScene(file, artboard, stateMachine, instance, controller))
                awaitDispose { closeAll(owned) }
            } catch (ce: CancellationException) {
                closeAll(owned)
                throw ce
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load $artboardName/$stateMachineName ($resId)", e)
                closeAll(owned)
                value = Result.Error(e)
            }
        }.value
    }
}

/**
 * Default view model of [artboard] + its "Default" instance (what other runtimes call
 * auto-bind). If that fails, falls back to the first authored instance of that view model.
 */
private suspend fun createViewModelInstance(
    file: RiveFile,
    artboard: Artboard,
    instanceName: String?,
): ViewModelInstance? {
    val vmSource = ViewModelSource.DefaultForArtboard(artboard)
    if (instanceName != null) {
        return ViewModelInstance.create(file, vmSource.namedInstance(instanceName))
    }
    return try {
        ViewModelInstance.create(file, vmSource.defaultInstance())
    } catch (ce: CancellationException) {
        throw ce
    } catch (e: Exception) {
        Log.w(TAG, "No default VM instance for ${artboard.name}: ${e.message}; trying first")
        try {
            val vmName = file.getDefaultViewModelInfo(artboard).viewModelName
            val first = file.getViewModelInstanceNames(vmName).firstOrNull() ?: return null
            ViewModelInstance.create(file, ViewModelSource.Named(vmName).namedInstance(first))
        } catch (ce: CancellationException) {
            throw ce
        } catch (e2: Exception) {
            Log.e(TAG, "No view model instance for ${artboard.name}", e2)
            null
        }
    }
}

/** Closes in reverse creation order (instance, state machine, artboard, file) and clears. */
private fun closeAll(owned: MutableList<AutoCloseable>) {
    for (i in owned.indices.reversed()) {
        try {
            owned[i].close()
        } catch (e: Exception) {
            Log.w(TAG, "Close failed: ${e.message}")
        }
    }
    owned.clear()
}

// ---------------------------------------------------------------------------------------------
// Reduce motion
// ---------------------------------------------------------------------------------------------

/**
 * True when the system "Remove animations" setting is on
 * (`Settings.Global.ANIMATOR_DURATION_SCALE == 0f`). Updates live while composed.
 */
@Composable
fun rememberSystemReduceMotion(): Boolean {
    val resolver = LocalContext.current.applicationContext.contentResolver
    val read = remember(resolver) {
        {
            Settings.Global.getFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
        }
    }
    var reduce by remember(resolver) { mutableStateOf(read()) }
    DisposableEffect(resolver) {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                reduce = read()
            }
        }
        resolver.registerContentObserver(
            Settings.Global.getUriFor(Settings.Global.ANIMATOR_DURATION_SCALE),
            false,
            observer,
        )
        reduce = read()
        onDispose { resolver.unregisterContentObserver(observer) }
    }
    return reduce
}
```

### 8.4 PaybakRiveAssets.kt and example usages (RiveSamples.kt)
```kotlin
package app.paybak.paybak.ui.rive

import androidx.annotation.RawRes
import app.paybak.paybak.R

/**
 * The six Paybak illustrations. Artboard and state machine share the same name in every file.
 * [widthDp]/[heightDp] = artboard size = Rive view size (GetStarted, Notifications and HomeAllSquare are the
 * Figma slot + 12 dp bleed per side; reserve the slot and overlay the view centred on it, see rive.md). [tapTrigger] = the generic
 * "something was tapped" trigger to observe for a light haptic (null = no listeners).
 */
enum class PaybakRiveAsset(
    @param:RawRes val resId: Int,
    val artboard: String,
    val widthDp: Int,
    val heightDp: Int,
    val tapTrigger: String?,
) {
    Onboarding(R.raw.paybak_onboarding, "Onboarding", 362, 340, null),
    GetStarted(R.raw.paybak_getstarted, "Get Started", 386, 284, "personTapped"),
    Notifications(R.raw.paybak_notifications, "Notifications", 386, 324, "bellTapped"),
    AllSet(R.raw.paybak_allset, "All Set", 362, 300, "personTapped"),
    HomeFirstDay(R.raw.paybak_homefirstday, "First Day", 240, 180, "characterTapped"),
    HomeAllSquare(R.raw.paybak_home_allset, "AllSquare", 264, 204, "tapped");

    val stateMachine: String get() = artboard
}
```
```kotlin
package app.paybak.paybak.ui.rive

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import app.rive.RivePointerInputMode

/** Welcome 1–3: one view, `step` 1…3, no listeners; parent handles horizontal swipe. */
@Composable
fun OnboardingIllustration(step: Int, modifier: Modifier = Modifier) {
    val a = PaybakRiveAsset.Onboarding
    PaybakRive(
        resId = a.resId,
        artboard = a.artboard,
        stateMachine = a.stateMachine,
        modifier = modifier.size(a.widthDp.dp, a.heightDp.dp),
        pointerInputMode = RivePointerInputMode.Observe,
        numbers = mapOf("step" to step.toFloat()),
    )
}

/** Any tappable illustration: taps go to the .riv listeners; haptic on the generic trigger. */
@Composable
fun TappableIllustration(
    asset: PaybakRiveAsset,
    modifier: Modifier = Modifier,
    insideScrollingContainer: Boolean = false,
) {
    val haptics = LocalHapticFeedback.current
    PaybakRive(
        resId = asset.resId,
        artboard = asset.artboard,
        stateMachine = asset.stateMachine,
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(asset.widthDp.toFloat() / asset.heightDp),
        pointerInputMode = if (insideScrollingContainer) {
            RivePointerInputMode.Observe
        } else {
            RivePointerInputMode.Consume
        },
        observedTriggers = listOfNotNull(asset.tapTrigger),
        onTrigger = { haptics.performHapticFeedback(HapticFeedbackType.ContextClick) },
    )
}
```

Per-screen usage:

> **Editor's note:** the "Size" column is the **Rive view** size (= the artboard). For Get Started (slot 362×260), Setup 4 (slot 362×300) and Home All settled (slot 240×180), reserve the Figma slot in layout and draw the view centred on it: `Box(Modifier.size(slot), contentAlignment = Center) { PaybakRive(modifier = Modifier.requiredSize(view)) }`. Don't use `TappableIllustration`'s `fillMaxWidth().aspectRatio(...)` for these three: inside the 362 dp content column it would shrink the art by about 6 % and move everything below it.

| Screen | Asset | Size (dp) = Rive view (artboard) | pointerInputMode | numbers | observedTriggers → haptic |
|---|---|---|---|---|---|
| Welcome 1–3 (one composable for all steps) | `Onboarding` | 362 × 340 | `Observe` (parent swipe) | `mapOf("step" to step.toFloat())` | none (no listeners) |
| Get Started | `GetStarted` | 386 × 284 (draws its own grey card) | `Consume` | none | `personTapped` |
| Setup 4 — Notifications | `Notifications` | 386 × 324 | `Consume` | none | `bellTapped` |
| All set | `AllSet` | 362 × 300 | `Consume` | none | `personTapped` |
| Home — First day (in scroll content) | `HomeFirstDay` | 240 × 180 | `Observe` | none | `characterTapped` |
| Home — All settled (in scroll content) | `HomeAllSquare` | 264 × 204 | `Observe` | none | `tapped` |

Haptic: `LocalHapticFeedback.current.performHapticFeedback(HapticFeedbackType.ContextClick)` compiles with Compose 1.10.4 and is the closest to iOS "light impact". Alternative: `LocalView.current.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)`.

Imperative control if needed: `onReady = { controller -> … }` gives a `PaybakRiveController` with `setNumber`, `setBool`, `fire`, `triggerFlow`, `numberFlow`, `boolFlow` and `isActive`. It is valid until the composable leaves composition, and calls after that are no-ops. Prefer the declarative `numbers =` / `reduceMotion =` parameters.

### 8.5 Minimal version using only the library's remember-helpers (compiles; for reference)
```kotlin
@Composable
fun CanonicalOnboarding(step: Int, reduceMotion: Boolean) {
    val worker = rememberRiveWorker()
    val fileResult = rememberRiveFile(RiveFileSource.RawRes.from(R.raw.paybak_onboarding), worker)
    val file = (fileResult as? Result.Success)?.value ?: return
    val artboard = (rememberArtboardResult(file, "Onboarding") as? Result.Success)?.value ?: return
    val stateMachine =
        (rememberStateMachineResult(artboard, "Onboarding") as? Result.Success)?.value ?: return
    val vmSource = remember(artboard) { ViewModelSource.DefaultForArtboard(artboard).defaultInstance() }
    val vmi = (rememberViewModelInstanceResult(file, vmSource) as? Result.Success)?.value ?: return

    LaunchedEffect(vmi, step) { vmi.setNumber("step", step.toFloat()) }
    LaunchedEffect(vmi, reduceMotion) { vmi.setBoolean("reduceMotion", reduceMotion) }

    Rive(
        file = file,
        modifier = Modifier.size(362.dp, 340.dp),
        artboard = artboard,
        stateMachine = stateMachine,
        viewModelInstance = vmi,
        fit = Fit.Contain(Alignment.Center),
        pointerInputMode = RivePointerInputMode.Observe,
    )
}
// Trigger observation with the same helpers:
// LaunchedEffect(vmi) { vmi.getTriggerFlow("personTapped").collect { onTap() } }
```
Downside: the first frame can be drawn before the `LaunchedEffect` setters run (step 1 or the animated pose for a frame), and there's no instance-name fallback.

---

## 9. Classic view API (`RiveAnimationView`): documented, NOT recommended

`open class app.rive.runtime.kotlin.RiveAnimationView(context: Context, attrs: AttributeSet? = null) : RiveTextureView` is annotated **`@Deprecated("The legacy API is deprecated. Use the APIs in the app.rive package.", level = WARNING)`**. `RiveFileController.stateMachines` is also deprecated ("Multiple state machines are deprecated").
```kotlin
fun setRiveResource(
    @RawRes resId: Int,
    artboardName: String? = null,
    animationName: String? = null,        // deprecated concept (linear animations)
    stateMachineName: String? = null,
    autoplay: Boolean = controller.autoplay,
    autoBind: Boolean = false,            // true => file.defaultViewModelForArtboard(artboard).createDefaultInstance() bound to artboard + state machine
    fit: app.rive.runtime.kotlin.core.Fit = Fit.CONTAIN,
    alignment: app.rive.runtime.kotlin.core.Alignment = Alignment.CENTER,
    loop: Loop = Loop.AUTO,
)
// same parameter list: setRiveBytes(bytes, …), setRiveFile(file: File, …)
// legacy enums: Fit { FILL, CONTAIN, COVER, FIT_WIDTH, FIT_HEIGHT, NONE, SCALE_DOWN, LAYOUT }
//               Alignment { TOP_LEFT, TOP_CENTER, TOP_RIGHT, CENTER_LEFT, CENTER, CENTER_RIGHT, BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT }
var touchPassThrough: Boolean = false; var multiTouchEnabled: Boolean = false
```
Legacy data binding: `val vmi = view.controller.stateMachines.first().viewModelInstance` (or `view.controller.activeArtboard?.viewModelInstance`). Then `vmi.getNumberProperty("step").value = 2f`, `vmi.getBooleanProperty("reduceMotion").value = true`, `vmi.getTriggerProperty("x").trigger()`. To observe: `vmi.getTriggerProperty("personTapped").valueFlow` is a `StateFlow<TriggerUnit>` that emits its current value on collect, so use `.drop(1)`.
Compose usage would be `AndroidView(factory = { RiveAnimationView(it).apply { setRiveResource(R.raw.x, artboardName = "…", stateMachineName = "…", autoBind = true) } })`.

**Why not:** it produces deprecation warnings. The renderer is deleted on `onDetachedFromWindow`, but the file and controller are released **only on the host LifecycleOwner's `ON_DESTROY`** (`RiveViewLifecycleObserver`). In our single-Activity Compose app, every screen's file would stay in memory until the Activity dies, and `AndroidView.onRelease` has no public API to free it. It drives frames from its own `Choreographer` callback and a native renderer per view, outside Compose. None of this path was runtime-tested.

### Recommendation matrix
| Need | Compose `app.rive` (wrapper) | `RiveAnimationView` |
|---|---|---|
| Fixed artboard + state machine by name | ✅ `Artboard.create(file, name)` / `StateMachine.create(ab, name)` | ✅ |
| Data binding (set number/bool, fire, observe trigger) | ✅ verified on device | ⚠️ works via legacy properties; not verified |
| Touch → state machine listeners | ✅ default, with Consume/Observe control | ✅ (touchPassThrough) |
| Several animations on screen | ✅ one shared worker, verified with 6 views | ⚠️ one native renderer per view |
| Lifecycle and memory | ✅ closed on dispose; pauses when not RESUMED | ❌ file freed only at Activity destroy |
| Future-proof | ✅ (12.0 renames: `Artboard.create`/`ViewModelInstance.create` → `fromFile`, `StateMachine.create` → `fromArtboard`, `RiveFile.load` → `fromSource`, `remember*Result` → `remember*`) | ❌ scheduled for removal |

---

## 10. What was and wasn't verified

**Verified by compiling** (AGP 9.4.1 / Kotlin 2.2.10 / Compose BOM 2026.02.01, zero warnings): every file listed in §8.2–§8.4, and the §8.5 helper chain.
**Verified at runtime** (arm64 16 KB emulator, debug and R8 release): init requirement and its failure mode; loading all 6 files by artboard and state machine name; default VM instance creation and binding for all 6; `step` changes and `numberFlow`; `reduceMotion` static pose; live system-setting observation; tap forwarding and `getTriggerFlow` emissions for listener-fired triggers; background/resume; disposal order; six views on one worker in a scroll container; R8 with the bundled rules.
**Not verified:** the `RiveInitializer` manifest route (use `Rive.init` instead); the legacy `RiveAnimationView` path; the `viewModelInstanceName` override and the first-instance fallback (never needed); physical-device haptic feel; Vulkan backend (OpenGL is the default and was used); behaviour on API 24–28 devices (the emulator was a recent API level; the AAR claims minSdk 21); configuration-change (rotation) behaviour, where everything reloads with the Activity and the `step` from app state is re-applied through `numbers`.

The verification build (build-session scratch, not kept) was a copy of `android/` with a different applicationId (`app.paybak.rivecheck`), uninstalled from the emulator afterwards. The real `app.paybak.paybak` install was not touched.
