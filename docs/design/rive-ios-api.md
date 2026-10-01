# rive-ios 6.28.0: API reference and SwiftUI wrapper for Paybak (checked against source and the shipped binary)

> **Status:** written before the iOS app was built, as the integration guide. The shipped wrapper is `ios/paybak/paybak/Rive/PaybakRive.swift` (with `Rive/RiveAssets.swift`), and the package edits of §9 are applied in `ios/paybak/paybak.xcodeproj`. Where the app's code differs from the listings below, the app wins; the API facts and gotchas still hold.

Scope: everything the iOS implementer needs to show the six Paybak `.riv` files (see `rive.md`) in SwiftUI with **rive-ios 6.28.0**. It covers loading, touch handling, data binding (auto-bind, number/bool/trigger, trigger listeners), playback lifecycle, Swift 6 and MainActor concerns, a complete wrapper (§8), and the exact `project.pbxproj` edits (§9).

Unless a path says otherwise, line references are to the source at tag 6.28.0, commit `b9966d5e` (a local clone; build-session scratch, not kept). The C++ runtime that 6.28.0 pins (submodule `rive-runtime` @ `43aa1025`) was sparse-cloned the same way.

---

## 0. TL;DR

1. **Read this first.** All six Paybak `.riv` files carry Rive's export watermark. With 6.28.0, every new artboard instance first plays a Rive pre-roll: a **black box (`0xFF000000`) with the white "RIVE" wordmark, for about 2.0–2.25 s**. The illustration starts only after that. It happens in the real app on the iOS 27 simulator (confirmed with simulator screenshots during the build; not kept), and it plays again on every `restart()`/`reset()`. App code can't and shouldn't turn it off. The fix is to **re-export the files without the watermark** from a Rive account/plan that allows it. Details in §1.
2. Use the **legacy `RiveViewModel` API** (synchronous, stable): `RiveFile(data:loadCdn:)` → `RiveModel(riveFile:)` → `RiveViewModel(_:stateMachineName:fit:alignment:autoPlay:artboardName:)` → `RiveViewRepresentable(viewModel:)`. Then `riveModel.enableAutoBind { instance in … }`.
3. **Auto-bind works on all six files.** It binds instance index 0, whether that instance is named `Default` or `Instance`. The callback runs synchronously, **once** if you enable it after `RiveViewModel.init`, **twice** if before, and **twice more on every `reset()`/`stop()`**. Each run hands you a **new instance object**.
4. Number/bool: `instance.numberProperty(fromPath:)?.value = Float`, `instance.booleanProperty(fromPath:)?.value = Bool`. Trigger: `instance.triggerProperty(fromPath:)?.trigger()`. Listen: `triggerProperty(fromPath:)?.addListener { } -> UUID`, and `removeListener(uuid)`. Listeners run **on the main thread, synchronously, during the next advance**. They never run inside `trigger()` itself.
5. **Touches go to the state machine by default.** The file's tap listeners fire on **pointer down**, and the "something was tapped" trigger fires **exactly once per tap**. So observe `personTapped` / `bellTapped` / `characterTapped` / `tapped` for haptics, and don't fire tap triggers yourself.
6. After changing a property on a view that isn't playing, call `riveViewModel.play()`. The legacy view doesn't advance by itself (issue #383). The wrapper does this for you.
7. The wrapper compiles with **zero warnings** under the target's exact settings (Swift 5 + default MainActor isolation + approachable concurrency + MemberImportVisibility), and also in Swift 6 language mode. It needs `import Combine` because of MemberImportVisibility.
8. pbxproj: 1 PBXBuildFile + Frameworks phase entry + `packageProductDependencies` + `packageReferences` + `XCRemoteSwiftPackageReference` (`exactVersion 6.28.0`) + `XCSwiftPackageProductDependency` (`RiveRuntime`). This was checked on a copy of the project: `xcodebuild -resolvePackageDependencies` and the Debug/Release simulator builds succeed, and `RiveRuntime.framework` is embedded automatically.

---

## 1. BLOCKER: the files are watermarked, so each illustration opens with a ~2 s black Rive pre-roll

**What the runtime does** (rive-runtime @ `43aa1025`, which is what 6.28.0 ships):
- `File::artboardNamed/artboardAt/artboardDefault` call `attachWatermark(...)` on every artboard instance they create, when the file's manifest has the watermark flag (`src/file.cpp:1342-1411`). The watermark artboard is the file's own `Watermark` artboard, played through its state machine.
- `StateMachineInstance::advanceAndApply` first advances the watermark. While it plays, the host artboard is advanced by **0 s**, which freezes it (`src/animation/state_machine_instance.cpp:2898-2910`).
- `Artboard::draw` draws the watermark instead of the artboard (`src/artboard.cpp:2248-2259`). `Watermark::draw` fills the whole artboard box with **opaque black `0xFF000000`**, then draws the watermark artboard with contain/center fit (`src/watermark.cpp:35,142-180`).
- The pre-roll ends when its state machine settles, or after at most 10 s (`src/watermark.cpp:19,119-140`). It is clocked on **wall time**, so it can't be skipped by advancing faster (`clampElapsed`, `src/watermark.cpp:62-117`). It plays **once per artboard instance**, so `RiveModel.setArtboard`, `RiveViewModel.reset()`/`stop()` (both rebuild the artboard) and every new controller replay it.

**What was measured with the 6.28.0 binary and these files:**
- macOS harness (a wall-clock probe; build-session scratch, not kept): the host state machine can't advance before **2.0–2.25 s** of real time have passed since the first advance, for `paybak-onboarding.riv` and `paybak-homefirstday.riv`. In a tight loop with no real time passing, the host never leaves its first frame.
- iOS 27.0 simulator, iPhone 17 Pro, with the wrapper below: at t≈1.6 s both Rive views are **solid black with the white "RIVE" wordmark**. At t≈3 s the Paybak illustration intro starts. After `restart()` the Get Started view goes **black again**.
- rive-ios issue #461 ("Main artboard renders as opaque black…", closed as "the rive file is to blame") matches this symptom.

**Consequences for the product:** Welcome, Get Started, Setup 4, All set and both Home empty states would each show a black "RIVE" box for about 2 s before the illustration. Touches during the pre-roll hit a frozen host. `reduceMotion` doesn't affect the pre-roll.

**Action (a decision for the owner, not the implementer):** re-export the six `.riv` files without the watermark, from a Rive workspace/plan that exports without it. Don't try to work around it in code: the watermark is Rive's licensing mechanism. Nothing else in this document changes once clean files arrive. The wrapper is file-agnostic. To check new files, confirm that the first frame of a fresh view shows the illustration and not a black box.

> **Editor's note (verified 2026-09-30):** Android does **not** show the pre-roll with rive-android 11.12.1. I ran a separate Android harness APK (`app.paybak.rivecheck`, since uninstalled) on the Android emulator, and the Get Started illustration was already drawing about 1.1 s after the activity started, with no black frame. Its bundled runtime evidently predates or lacks the watermark path. The web runtime @rive-app/canvas-advanced 2.43 **does** show it: a headless render stays solid black until about 2.5 s of wall time. So until clean files arrive, the ~2 s black box affects **iOS only**.

---

## 2. Package facts (Package.swift)

`Package.swift:1-18` (the whole file):
- `// swift-tools-version:5.10`, package name `RiveRuntime`.
- **Binary target**, not source: `.binaryTarget(name: "RiveRuntime", url: "https://github.com/rive-app/rive-ios/releases/download/6.28.0/RiveRuntime.xcframework.zip", checksum: "b571537569a087d3637f26d416502dbdbb6f0bdc7b8f329379b40f696b093406")`. I downloaded the zip and its SHA-256 matches.
- Platforms: `.iOS("14.0"), .visionOS("1.0"), .tvOS("16.0"), .macOS("13.1"), .macCatalyst("14.0")`.
- Product: `.library(name: "RiveRuntime", targets: ["RiveRuntime"])`. **Product name = `RiveRuntime`, module = `RiveRuntime`.**
- The xcframework is a **dynamic** framework (`Mach-O … dynamically linked shared library`). Xcode embeds it automatically for SPM products; the build put it at `paybak.app/Frameworks/RiveRuntime.framework`. It was built with Swift 6.3.2 in `-swift-version 5` mode with `-enable-library-evolution`, SDK 26.5, `MinimumOSVersion 14.0` (from its `.swiftinterface` and Info.plist). Slices: ios-arm64, ios-arm64_x86_64-simulator, maccatalyst, macos, tvos(+sim), xros(+sim).
- Latest tags: …6.26.0, 6.27.0, **6.28.0** (released 2026-09-30, not a prerelease). 6.28.0 is the newest stable tag.

---

## 3. Loading a .riv from the bundle with a specific artboard + state machine, contain/center

### 3.1 Real initializers
`RiveViewModel` (`Source/RiveViewModel.swift`) is `@objc open class RiveViewModel: NSObject, ObservableObject, RiveFileDelegate, RiveStateMachineDelegate, RivePlayerDelegate` (line 41). It is **not** `@MainActor`.

| Initializer | Line | Notes |
|---|---|---|
| `init(_ model: RiveModel, stateMachineName: String?, fit: RiveFit = .contain, alignment: RiveAlignment = .center, autoPlay: Bool = true, artboardName: String? = nil)` | 49-63 | **Used by the wrapper.** |
| `init(fileName: String, extension: String = ".riv", in bundle: Bundle = .main, stateMachineName: String?, fit: RiveFit = .contain, alignment: RiveAlignment = .center, autoPlay: Bool = true, artboardName: String? = nil, loadCdn: Bool = true, customLoader: LoadAsset? = nil)` | 81-99 | Convenient, but it calls `try! RiveModel(fileName:…)` (line 97), and `RiveFile.getData` calls `fatalError` if the resource is missing (`Source/Utils/RiveFile+Extensions.swift:29-43`). |
| `init(_ model:, animationName: …)` / `init(fileName:…, animationName: …)` | 65-79 / 101-120 | Animation variants. The `stateMachineName:` label (no default) picks the state machine overload. |

Every initializer ends in `sharedInit`, which runs `try! configureModel(artboardName:stateMachineName:animationName:)` (lines 156-166, 302-337). **A wrong artboard or state machine name crashes.** That's why the wrapper checks the names first with the throwing `RiveModel` API:
- `RiveModel.init(riveFile: RiveFile)`: `Source/RiveModel.swift:24-26`.
- `RiveModel.setArtboard(_ name: String) throws`: `RiveModel.swift:60-70` (creates a new artboard instance from the file).
- `RiveModel.setStateMachine(_ name: String) throws`: `RiveModel.swift:104-115`.
- `RiveFile(data: Data, loadCdn: Bool) throws` (ObjC `initWithData:loadCdn:error:`, `Source/Renderer/include/RiveFile.h:62-64`).

`RiveFit` is `fill, contain, cover, fitHeight, fitWidth, scaleDown, noFit, layout` and `RiveAlignment` is `topLeft … center … bottomRight` (`Source/Renderer/include/Rive.h:60-84`). Use `.contain` and `.center`, which are the defaults.

### 3.2 Bundle lookup
The `.riv` files go in `ios/paybak/paybak/Resources/Rive/`. The file-system-synchronized group **flattens them into the bundle root**: the built `paybak.app/` contains `paybak-onboarding.riv` and the rest directly. So use `Bundle.main.url(forResource: "paybak-onboarding", withExtension: "riv")`. None of the six files references image, font or audio assets (checked with a `customAssetLoader` probe), so `loadCdn: false` is safe and avoids any network access.

### 3.3 Several controllers per file
You can share one `RiveFile` between several `RiveModel`s. Each `setArtboard` creates its own artboard instance. The harness created three models from one `RiveFile` object without problems. The wrapper caches the parsed `RiveFile` per file name.

---

## 4. SwiftUI integration and touches

- `RiveViewModel.view() -> AnyView` wraps `RiveViewRepresentable(viewModel: self)` (`RiveViewModel.swift:601-603`). `public struct RiveViewRepresentable: UIViewRepresentable` has a public `init(viewModel:)` (lines 656-691). In the shipped interface it is `@MainActor @preconcurrency`. The wrapper uses `RiveViewRepresentable` directly, without `AnyView`.
  - `makeUIView` → `viewModel.createRiveView()` → `RiveView(model:autoPlay:)` → `setModel` → `play()`, which starts a CADisplayLink (lines 664-666, 550-562, `RiveView.swift:223-241`). The first advance happens on the first display-link tick, not synchronously.
  - `updateUIView` → `viewModel.update(view:)` re-applies fit/alignment/layoutScaleFactor/forwardsListenerEvents (lines 573-578).
  - `dismantleUIView` → `view.stop()` (the RiveView's, **not** the RiveViewModel's, so the model is not reset) and then `deregisterView()` if it's the current view (lines 672-677).
  - One `RiveViewModel` drives **one** `RiveView`. `registerView` replaces `riveView` (lines 585-593). Never show the same view model or controller in two places at once.
- **Touches go to the state machine by default.** `RiveView` (a UIView subclass, `@MainActor`) overrides `touchesBegan/Moved/Ended/Cancelled` (`RiveView.swift:546-640`). Each converts the point into artboard space for the current fit/alignment, calls `stateMachine.touchBegan/…(atLocation:touchID:)`, advances the state machine by 0 at once for began/ended/cancelled (`advanceStateMachine(by: 0)`, lines 820-841), and calls `play()` (line 669). A touch therefore also restarts a paused view. Multitouch is supported: an `IDPool` of 10 touch IDs (line 113).
  - `forwardsListenerEvents` (default **false**, `RiveViewModel.swift:202-204`, `RiveView.swift:53`) only decides whether the touch is **also** passed up the responder chain (`super.touches…`). It doesn't affect listener handling.
  - Measured with the real files after the pre-roll (a macOS touch-scan harness, 16×12 grid): every listener fires on **pointer down**. `personTapped` fires exactly once per tap, even where two person hit areas overlap (All set: `tapLeft`+`tapMiddle` fire together at x≈147, and `personTapped` still fires once). Hit areas in artboard pt (bounding boxes of the grid points that hit): Get Started has `tapLeft` x 60-132 / y 130-248, `tapMiddle` x 156-229 / y 106-248 and `tapRight` x 253-325 / y 130-248. Notifications `bellTapped` is x 180-349 / y 40-121. All set has `tapBadge` at x 169-192 / y 12-37 and the persons across x 79-282 / y 87-287. First Day `tapCharacter` is x 7-232 / y 37-157. AllSquare `tapped` is x 24-239 / y 25-178.
  - Onboarding has no listeners. Put `.allowsHitTesting(false)` on it (the wrapper does this through `PaybakRiveAsset.isInteractive`) so the Welcome horizontal-swipe gesture gets the touches.
- Offscreen: `RiveView.offscreenBehavior` defaults to `.playAndNoDraw`, which keeps advancing but skips drawing (`RiveView.swift:56`, `isOnscreen()` in `Source/Extensions/View+Extensions.swift:28-63`). `drawOptimization` defaults to `.drawOnChanged` (line 60). Neither is exposed on `RiveViewModel`; use `riveViewModel.riveView?.offscreenBehavior` if you need them.

---

## 5. Data binding

### 5.1 Default view model and instance, auto-bind semantics
- `RiveModel.enableAutoBind(_ callback: @escaping (RiveDataBindingViewModel.Instance) -> Void)` (`RiveModel.swift:13,181-186`) and `disableAutoBind()` (189-192).
- `autoBind()` (194-209): if enabled and an artboard exists, it runs `riveFile.defaultViewModel(for: artboard)` → `viewModel.createDefaultInstance()` → `stateMachine.bind(viewModelInstance:)`, or `artboard.bind(viewModelInstance:)` when there's no state machine yet, and then calls your callback **synchronously**.
- It runs from `enableAutoBind` itself **and** from every `setArtboard`/`setStateMachine` (lines 67, 81, 94, 108, 137). Measured on all six files:
  - enable **after** `RiveViewModel.init` → **1** callback, instance already bound to the state machine (`instance === stateMachine.viewModelInstance` is true). This is what the wrapper does.
  - enable **before** `RiveViewModel.init` → **2** callbacks (artboard-only bind, then state machine bind).
  - `riveViewModel.reset()` or `stop()` → `resetCurrentModel` → `configureModel` (`RiveViewModel.swift:340-351`) → **2 more callbacks with a NEW instance object**. Values you set on the old instance are lost, so re-apply them and re-attach listeners in the callback. The wrapper's `didBind` does this.
- "Default instance" means **instance index 0 of the artboard's default view model**, whatever its name. `ViewModelRuntime::createDefaultInstance` → `File::createDefaultViewModelInstance(viewModel)` → `viewModel->instance(0)` (rive-runtime `src/viewmodel/runtime/viewmodel_runtime.cpp:167-178`, `src/file.cpp:1854-1863`). Measured: onboarding/getstarted/notifications/home-allset bind `Default`, allset/homefirstday bind `Instance`. All properties resolve.
- Manual alternative (not needed): `let vm = riveFile.defaultViewModel(for: artboard)` (`RiveFile.h:153-154`), then `vm.createDefaultInstance()` / `createInstance(fromName:)` / `createInstance(fromIndex:)` (`RiveDataBindingViewModel.h:47-90`), then `stateMachine.bind(viewModelInstance:)` (`RiveStateMachineInstance.h:151-152`, which also binds the artboard). **Keep a strong reference to the instance** (header notes at `RiveDataBindingViewModelInstance.h:28-31`).

Properties measured per file (`instance.properties`):
| File | Instance | Properties |
|---|---|---|
| paybak-onboarding | Default | `reduceMotion`: boolean, `step`: number (value 1.0) |
| paybak-getstarted | Default | `personTapped`, `tapLeft`, `tapMiddle`, `tapRight`: trigger; `reduceMotion`: boolean |
| paybak-notifications | Default | `bellTapped`: trigger; `reduceMotion`: boolean |
| paybak-allset | Instance | `personTapped`, `tapBadge`, `tapLeft`, `tapMiddle`, `tapRight`: trigger; `reduceMotion`: boolean |
| paybak-homefirstday | Instance | `characterTapped`, `tapCharacter`: trigger; `reduceMotion`: boolean |
| paybak-home-allset | Default | `tapped`: trigger; `reduceMotion`: boolean |

### 5.2 Setting number and boolean, firing a trigger
Swift names come from the ObjC headers and were compiled:
```swift
instance.numberProperty(fromPath: "step")?.value = 2            // Float  (RiveDataBindingViewModelInstance.h:82-83, Property.h:88)
instance.booleanProperty(fromPath: "reduceMotion")?.value = true // Bool   (Instance.h:95-96, Property.h:119)
instance.triggerProperty(fromPath: "bellTapped")?.trigger()      //        (Instance.h:162-163, Property.h:248)
```
Types: `RiveDataBindingViewModel.Instance` plus `.NumberProperty`, `.BooleanProperty` and `.TriggerProperty` (`NS_SWIFT_NAME`s in `RiveDataBindingViewModelInstance.h:32`, `…Property.h:83,114,241`). Property objects are cached per path, so fetching the same path again returns the same object (`RiveDataBindingViewModelInstance.mm:130-162, 282-314`). You don't need to keep them.

Measured on Onboarding: `step` 1→2 moves to `Welcome 1 · Off Left`, `Welcome 1 · Hold`, `Welcome 2 · Shown`, `Welcome 2 · Enter`. 2→3 moves to `Welcome 2 · Hold`, `Welcome 3 · Shown`, `Welcome 3 · Enter`. 3→2 moves back. `reduceMotion = true` moves to `Still` (Get Started also shows `Moment Off`). If you set it before the first advance, the intro uses `Fade In` instead of `Intro`, and Onboarding shows `Welcome 1 · Settled`.

### 5.3 Listening to a trigger (and to any property)
```swift
let id: UUID = instance.triggerProperty(fromPath: "personTapped")!.addListener { /* () -> Void */ }
instance.triggerProperty(fromPath: "personTapped")!.removeListener(id)
```
- `addListener` returns an `NSUUID`, bridged to `UUID` (`…Property.h:260-261`). `removeListener(_:)` is on the base `Property` (`…Property.h:41`, impl `.mm:94-103`). Number, bool, string, color and enum have typed listeners too, e.g. the number one is `(Float) -> Void` (`…Property.h:79,103-104`).
- **When and on which thread:** listeners never run from `trigger()` or a value setter. They run from `RiveDataBindingViewModelInstance.updateListeners()` (`…Instance.mm:421-446`), which calls each cached property's `handleListeners` when its `hasChanged` flag is set (`…Property.mm:544-554`) and then clears it. `RiveView` calls `stateMachine.viewModelInstance?.updateListeners()` right after every state machine advance: `RiveView.advance(delta:)` line 410, driven by the CADisplayLink added to `.main` (`RiveDisplayLink.swift:74-78`), and `advanceStateMachine(by:)` line 840, called during touch handling. **So listeners always run on the main thread, synchronously.** A tap's trigger listener runs inside `touchesBegan`. Verified on the simulator (`Thread.isMainThread == true`) and in the harness: after `trigger()` the count is still 0, and it becomes 1 after the next advance.
- `hasChanged` is set by any change to the underlying value, whether from code, a Rive listener or the state machine (rive-runtime `viewmodel_instance_value_runtime.cpp:29-32`). A trigger fires and is reset to 0 inside the same `advanceAndApply` (`viewmodel_instance_trigger.cpp:22-28`, `state_machine_instance.cpp:2966`), so **one callback per fire**. Measured: never more than one callback per trigger per tap.
- The listener block is stored with `copy` on the property (`…Property.mm:83-92`). Property → instance → state machine → model → view model means a strong `self` capture from the controller forms a cycle. **Capture `[weak self]`.** The same applies to the `enableAutoBind` closure.

### 5.4 Does a property change need `play()`/`advance()`?
**Yes, when the view isn't playing.** In the legacy runtime nothing advances unless the display link is running (`isPlaying`) or a touch arrives (`RiveView.swift:354-443`). `RiveViewModel.setInput` calls `play()` for you (lines 365-401). Data-binding setters don't. Rive's own example says "Manually advance the Rive view since it is not playing" and calls `riveView?.advance(delta: 0)` (`Example-iOS/Source/Examples/SwiftUI/DataBindingView.swift:100-102`). Issue #383 (open) says the same: call `play()` after a trigger. Prefer `play()` to `advance(delta: 0)`, because a transition animation needs continuous advancing. `play()` is idempotent: `startTimer` is guarded by `displaySync == nil` (`RiveView.swift:326`).

Also measured: **none of the six state machines ever settles.** Even `Still` keeps `advance` returning true for more than 12 s. So a visible view never stops its display link by itself, and `play()` really matters only after you've called `pause()`.

---

## 6. Playback lifecycle

| Need | Call | Behaviour (source) |
|---|---|---|
| Pause | `riveViewModel.pause()` | `RiveView.pause()` sets `isPlaying = false`, and the display link stops on the next tick (`RiveViewModel.swift:280-283`, `RiveView.swift:279-288`). State is kept. |
| Resume | `riveViewModel.play()` | Starts the display link and continues from the current state (`RiveViewModel.swift:251-277`, `RiveView.swift:267-276`). Any touch also resumes. |
| Restart from frame 0 | `riveViewModel.reset()` then `play()` | `resetCurrentModel()` rebuilds the artboard and state machine (a new artboard instance, which **replays the watermark pre-roll**), auto-bind binds a **new** instance, then `riveView?.reset()` (`RiveViewModel.swift:293-297, 340-351`). `stop()` does the same and also stops the view (286-290). |
| Remove from screen | nothing | SwiftUI `dismantleUIView` stops the RiveView and deregisters it (`RiveViewModel.swift:672-677`). `RiveView.deinit` stops the timer (`RiveView.swift:182-200`). The model and instance live as long as your controller. |
| Several views at once | one controller per view | Each `RiveView` has its own CADisplayLink and Metal drawable. Verified with Onboarding and Get Started on screen together. |
| Reset when a view reappears | (a) own the controller in the screen's `@StateObject`; a new screen instance gets a fresh controller and animation; (b) for a kept-alive controller, call `controller.restart()` in `.onAppear` | Both replay the watermark until the files are re-exported. |

Known issue: **#427 (open)**. Repeatedly creating and destroying a `RiveViewModel` that has `enableAutoBind` leaks the artboard instance (their example: ~150 MB after 10 cycles with a complex file). Paybak creates only a handful of views per session and the files are small, so it's acceptable. Don't create controllers inside frequently re-created views such as list cells, and don't call `restart()` in a loop.

---

## 7. Swift 6, MainActor and approachable concurrency

The target builds with `-swift-version 5 -default-isolation=MainActor` plus the upcoming features `DisableOutwardActorInference`, `InferSendableFromCaptures`, `GlobalActorIsolatedTypesUsability`, `InferIsolatedConformances`, `NonisolatedNonsendingByDefault` and `MemberImportVisibility` (from the actual xcodebuild command line). Findings:
- **The wrapper compiles with no errors and no warnings** in Debug and Release. With `SWIFT_VERSION=6` as a probe it also compiles cleanly. **`@preconcurrency import RiveRuntime` is not needed.**
- **MemberImportVisibility:** every file that uses a Rive member needs its own `import RiveRuntime`. Otherwise you get errors like `property 'name' is not available due to missing import of defining module 'RiveRuntime'`, and I hit this in the test view. `ObservableObject`/`@Published` also need **`import Combine`**, or you get `type … does not conform to protocol 'ObservableObject'`. Keep Rive types inside `PaybakRive.swift` so screens only need `import SwiftUI`.
- `RiveViewModel`, `RiveModel`, `RiveFile` and the data-binding classes are non-isolated and non-Sendable. `RiveView` and `RiveViewRepresentable` are `@MainActor`. Our code is all main-actor, so no values cross isolation domains. Calling the non-isolated synchronous methods from MainActor code is fine.
- The closures passed to `enableAutoBind` and `addListener` are non-`@Sendable`, so they inherit MainActor isolation from where they're written. They may touch controller state directly. Rive always calls them on the main thread (§5.3), so Swift 6's dynamic isolation checks would pass too.
- **Don't subclass `RiveViewModel`** (Rive's example does, `DataBindingView.swift:12`). Under default MainActor isolation, a subclass and its overrides of nonisolated `@objc` delegate methods (`player(playedWithModel:)` and the rest) run into actor-isolation mismatch errors. Use composition, as the wrapper does. If you must override, mark the overrides `nonisolated`.
- Avoid conforming our classes to `RiveStateMachineDelegate`/`RivePlayerDelegate`. With InferIsolatedConformances the conformance becomes MainActor-isolated. We don't need them.
- A `deinit` in a MainActor class is nonisolated. The wrapper doesn't use one: weak captures break the cycles, and listeners die with the instance.
- Error enums: `nonisolated enum PaybakRiveError: Error` compiles and keeps the type usable outside the main actor.

---

## 8. Recommended wrapper (complete, compiled, run on the iOS 27 simulator)

File to add: `ios/paybak/paybak/Rive/PaybakRive.swift` (now in the app; the app's version is the source of truth, and the listing below is the original wrapper). Put the six `.riv` files in `ios/paybak/paybak/Resources/Rive/` with their original names.

API summary:
- `PaybakRiveAsset`: `.onboarding`, `.getStarted`, `.notifications`, `.allSet`, `.homeFirstDay`, `.homeAllSquare`, with `fileName`, `artboardName`, `stateMachineName`, `artboardSize`, `aspectRatio`, `tapTrigger` and `isInteractive`.
- `PaybakRiveController(_ asset:)`, an `ObservableObject`, with `setNumber(name:value:)`, `setBool(name:value:)`, `fire(trigger:)`, `onTrigger(_:perform:)`, `pause()`, `resume()`, `restart()`, `@Published tapCount`, `riveViewModel`, `boundInstance`.
- `PaybakRiveView(controller:isInteractive:)` draws with contain/center at the artboard's aspect ratio. It keeps `reduceMotion` in sync with `@Environment(\.accessibilityReduceMotion)` (seeded from `UIAccessibility.isReduceMotionEnabled` before the first advance), pauses on disappear, resumes on appear, and is hidden from accessibility.
- `PaybakRiveIllustration(_ asset:)` owns its controller (`@StateObject`) and plays `.sensoryFeedback(.impact(weight: .light), trigger: controller.tapCount)`.

```swift
//
//  PaybakRive.swift
//  paybak
//
//  SwiftUI wrapper around rive-ios 6.28.0 (SPM product `RiveRuntime`), legacy `RiveViewModel` API
//  plus data binding. Written for this target's settings: Swift 5 mode,
//  SWIFT_DEFAULT_ACTOR_ISOLATION = MainActor, SWIFT_APPROACHABLE_CONCURRENCY = YES,
//  SWIFT_UPCOMING_FEATURE_MEMBER_IMPORT_VISIBILITY = YES (so every file that touches a Rive
//  type must `import RiveRuntime` itself; screens that only use this wrapper don't need to).
//
//  Everything here is main-actor isolated by default. Rive calls every callback used below
//  (auto-bind, trigger listeners) synchronously on the main thread.
//

import SwiftUI
import Combine
import UIKit
import os
import RiveRuntime

// MARK: - Assets

/// Every Paybak .riv file. Artboard, state machine, view model and property names were checked
/// against the files with the 6.28.0 runtime. Each file also contains the artboards "Watermark",
/// "NuRiveBrandmark01" and "NuRiveWordmark01", so always load the main artboard by name.
enum PaybakRiveAsset: String, CaseIterable {
    case onboarding
    case getStarted
    case notifications
    case allSet
    case homeFirstDay
    case homeAllSquare

    /// Bundle resource name without the extension (files live in Resources/Rive/, which the
    /// synchronized group copies into the bundle root).
    var fileName: String {
        switch self {
        case .onboarding: "paybak-onboarding"
        case .getStarted: "paybak-getstarted"
        case .notifications: "paybak-notifications"
        case .allSet: "paybak-allset"
        case .homeFirstDay: "paybak-homefirstday"
        case .homeAllSquare: "paybak-home-allset"
        }
    }

    var artboardName: String {
        switch self {
        case .onboarding: "Onboarding"
        case .getStarted: "Get Started"
        case .notifications: "Notifications"
        case .allSet: "All Set"
        case .homeFirstDay: "First Day"
        case .homeAllSquare: "AllSquare"
        }
    }

    /// Every main artboard has exactly one state machine with the artboard's name.
    var stateMachineName: String { artboardName }

    /// Artboard size in pt = the Rive view size. getStarted, notifications and homeAllSquare are the Figma
    /// slot plus 12 pt of bleed per side: reserve the slot in layout and overlay this view centred on it (rive.md).
    var artboardSize: CGSize {
        switch self {
        case .onboarding: CGSize(width: 362, height: 340)
        case .getStarted: CGSize(width: 386, height: 284)
        case .notifications: CGSize(width: 386, height: 324)
        case .allSet: CGSize(width: 362, height: 300)
        case .homeFirstDay: CGSize(width: 240, height: 180)
        case .homeAllSquare: CGSize(width: 264, height: 204)
        }
    }

    var aspectRatio: CGFloat { artboardSize.width / artboardSize.height }

    /// The "something was tapped" view-model trigger that the file's own listeners fire.
    /// Observe only this one per file: the specific ones (tapLeft/tapBadge/...) fire in the same
    /// frame, so listening to both would give two haptics per tap. nil means the file has no listeners.
    var tapTrigger: String? {
        switch self {
        case .onboarding: nil
        case .getStarted, .allSet: "personTapped"
        case .notifications: "bellTapped"
        case .homeFirstDay: "characterTapped"
        case .homeAllSquare: "tapped"
        }
    }

    /// Onboarding has no listeners. Keep it non-interactive so a parent swipe gesture works over it.
    var isInteractive: Bool { tapTrigger != nil }
}

nonisolated enum PaybakRiveError: Error {
    case missingResource(String)
}

// MARK: - Controller

/// Owns one artboard instance, its state machine and the view-model instance bound to it.
///
/// Use one controller per on-screen Rive view: a `RiveViewModel` drives a single `RiveView`
/// (`riveView` is replaced by the most recent `makeUIView`), so never show the same controller
/// in two places at once.
final class PaybakRiveController: ObservableObject {
    static let reduceMotionProperty = "reduceMotion"

    let asset: PaybakRiveAsset

    /// Goes up by one each time the file's own tap listener fires `asset.tapTrigger`.
    /// Drive haptics from it with `.sensoryFeedback(_, trigger:)`.
    @Published private(set) var tapCount = 0

    /// nil when the file, artboard or state machine could not be loaded. The view then draws nothing.
    let riveViewModel: RiveViewModel?

    /// The instance currently bound to the state machine. auto-bind replaces it on every
    /// (re)configuration, for example `restart()`.
    private(set) var boundInstance: RiveDataBindingViewModel.Instance?

    // Values and handlers are stored so they can be re-applied whenever a new instance is bound.
    private var numberValues: [String: Float] = [:]
    private var boolValues: [String: Bool] = [:]
    private var triggerHandlers: [String: () -> Void] = [:]
    private var listenerTokens: [(property: RiveDataBindingViewModel.Instance.TriggerProperty, id: UUID)] = []
    private var isSuspended = false

    private static let log = Logger(subsystem: "app.paybak.paybak", category: "Rive")
    private static var fileCache: [String: RiveFile] = [:]

    init(_ asset: PaybakRiveAsset, bundle: Bundle = .main) {
        self.asset = asset
        self.riveViewModel = Self.makeRiveViewModel(for: asset, bundle: bundle)
        // Seed before the first bind, so the very first advance already uses the right mode.
        // PaybakRiveView keeps it in sync with @Environment(\.accessibilityReduceMotion).
        boolValues[Self.reduceMotionProperty] = UIAccessibility.isReduceMotionEnabled
        // enableAutoBind runs the callback synchronously right now (the artboard and state machine
        // are already set), then again on every reset()/configureModel (RiveModel.swift:181-209).
        riveViewModel?.riveModel?.enableAutoBind { [weak self] instance in
            self?.didBind(instance)
        }
    }

    // MARK: Data binding

    /// Sets a number property, e.g. `setNumber(name: "step", value: 2)`.
    func setNumber(name: String, value: Float) {
        numberValues[name] = value
        guard let property = boundInstance?.numberProperty(fromPath: name) else {
            Self.log.error("\(self.asset.fileName, privacy: .public): no number property '\(name, privacy: .public)'")
            return
        }
        guard property.value != value else { return }
        property.value = value
        kick()
    }

    /// Sets a boolean property, e.g. `setBool(name: "reduceMotion", value: true)`.
    func setBool(name: String, value: Bool) {
        boolValues[name] = value
        guard let property = boundInstance?.booleanProperty(fromPath: name) else {
            Self.log.error("\(self.asset.fileName, privacy: .public): no boolean property '\(name, privacy: .public)'")
            return
        }
        guard property.value != value else { return }
        property.value = value
        kick()
    }

    /// Fires a view-model trigger from code. Don't use this to mirror taps: the files' own
    /// listeners already fire their tap triggers, so doing it again would double-fire.
    func fire(trigger name: String) {
        guard let property = boundInstance?.triggerProperty(fromPath: name) else {
            Self.log.error("\(self.asset.fileName, privacy: .public): no trigger property '\(name, privacy: .public)'")
            return
        }
        property.trigger()
        kick()
    }

    /// Runs `action` (on the main thread) whenever the trigger fires, whether a Rive listener,
    /// the state machine or `fire(trigger:)` fired it. One handler per trigger name; calling
    /// this again replaces the handler, so it is safe to call from `.onAppear`.
    func onTrigger(_ name: String, perform action: @escaping () -> Void) {
        let needsListener = triggerHandlers[name] == nil && name != asset.tapTrigger
        triggerHandlers[name] = action
        if needsListener, let boundInstance {
            attachListener(trigger: name, on: boundInstance)
        }
    }

    // MARK: Playback

    /// Stops the display link. A touch on the view resumes it (RiveView.handleTouch calls play()).
    func pause() {
        isSuspended = true
        riveViewModel?.pause()
    }

    func resume() {
        isSuspended = false
        riveViewModel?.play()
    }

    /// Recreates the artboard and state machine from the file, so the animation starts again from
    /// its first frame. auto-bind then binds a new instance, and didBind re-applies the stored values
    /// and trigger listeners. Note: a new artboard instance also replays the file's watermark pre-roll.
    func restart() {
        riveViewModel?.reset()
        if !isSuspended {
            riveViewModel?.play()
        }
    }

    // MARK: Private

    /// Called by auto-bind: once during init, then twice per reset() (artboard first, then state machine).
    private func didBind(_ instance: RiveDataBindingViewModel.Instance) {
        for token in listenerTokens {
            token.property.removeListener(token.id)
        }
        listenerTokens.removeAll()
        boundInstance = instance

        for (name, value) in numberValues {
            instance.numberProperty(fromPath: name)?.value = value
        }
        for (name, value) in boolValues {
            instance.booleanProperty(fromPath: name)?.value = value
        }
        var triggers = Set(triggerHandlers.keys)
        if let tap = asset.tapTrigger {
            triggers.insert(tap)
        }
        for name in triggers {
            attachListener(trigger: name, on: instance)
        }
    }

    private func attachListener(trigger name: String, on instance: RiveDataBindingViewModel.Instance) {
        guard let property = instance.triggerProperty(fromPath: name) else {
            Self.log.error("\(self.asset.fileName, privacy: .public): no trigger property '\(name, privacy: .public)'")
            return
        }
        // Rive calls this synchronously on the main thread inside
        // RiveView.advance / touch handling -> viewModelInstance.updateListeners().
        let id = property.addListener { [weak self] in
            self?.handleTrigger(name)
        }
        listenerTokens.append((property: property, id: id))
    }

    private func handleTrigger(_ name: String) {
        if name == asset.tapTrigger {
            tapCount += 1
        }
        triggerHandlers[name]?()
    }

    /// The legacy RiveView only advances while its display link runs or on a touch. If the view is
    /// stopped, it won't pick up a property change until something calls play()
    /// (RiveView.swift:387-443, rive-ios issue #383).
    private func kick() {
        guard !isSuspended else { return }
        riveViewModel?.play()
    }

    private static func makeRiveViewModel(for asset: PaybakRiveAsset, bundle: Bundle) -> RiveViewModel? {
        do {
            let file = try loadFile(named: asset.fileName, bundle: bundle)
            let model = RiveModel(riveFile: file)
            // Check the names with the throwing API first: RiveViewModel.init uses `try!`
            // internally (RiveViewModel.swift:157), so a typo there would crash, not throw.
            try model.setArtboard(asset.artboardName)
            try model.setStateMachine(asset.stateMachineName)
            return RiveViewModel(
                model,
                stateMachineName: asset.stateMachineName,
                fit: .contain,
                alignment: .center,
                autoPlay: true,
                artboardName: asset.artboardName
            )
        } catch {
            log.error("Rive load failed for \(asset.fileName, privacy: .public): \(String(describing: error), privacy: .public)")
            assertionFailure("Rive load failed for \(asset.fileName): \(error)")
            return nil
        }
    }

    /// Parses each .riv once and shares the RiveFile. Each controller still gets its own
    /// artboard and state machine instances.
    private static func loadFile(named name: String, bundle: Bundle) throws -> RiveFile {
        if let cached = fileCache[name] {
            return cached
        }
        guard let url = bundle.url(forResource: name, withExtension: "riv") else {
            throw PaybakRiveError.missingResource("\(name).riv")
        }
        // loadCdn: false. None of the Paybak files reference CDN or out-of-band assets.
        let file = try RiveFile(data: Data(contentsOf: url), loadCdn: false)
        fileCache[name] = file
        return file
    }
}

// MARK: - Views

/// Draws a controller's artboard with contain fit and center alignment, at the artboard's aspect
/// ratio. Give it the Figma slot width (the height follows) or an explicit frame.
struct PaybakRiveView: View {
    @ObservedObject var controller: PaybakRiveController
    /// false lets touches through to parent gestures (the Welcome swipe). true passes them to the
    /// state machine, which the files with tap listeners need.
    var isInteractive: Bool

    @Environment(\.accessibilityReduceMotion) private var reduceMotion

    init(controller: PaybakRiveController, isInteractive: Bool? = nil) {
        self.controller = controller
        self.isInteractive = isInteractive ?? controller.asset.isInteractive
    }

    var body: some View {
        content
            .aspectRatio(controller.asset.aspectRatio, contentMode: .fit)
            .allowsHitTesting(isInteractive)
            .accessibilityHidden(true)
            .onChange(of: reduceMotion, initial: true) { _, newValue in
                controller.setBool(name: PaybakRiveController.reduceMotionProperty, value: newValue)
            }
            .onAppear { controller.resume() }
            .onDisappear { controller.pause() }
    }

    @ViewBuilder
    private var content: some View {
        if let riveViewModel = controller.riveViewModel {
            RiveViewRepresentable(viewModel: riveViewModel)
        } else {
            Color.clear
        }
    }
}

/// Owns its own controller and plays a light haptic when the file's tap trigger fires. Use it for
/// Get Started, Setup 4, All set and the Home empty states. For Welcome, keep the controller in
/// the screen (@StateObject) and call setNumber(name: "step", ...).
struct PaybakRiveIllustration: View {
    @StateObject private var controller: PaybakRiveController

    init(_ asset: PaybakRiveAsset) {
        _controller = StateObject(wrappedValue: PaybakRiveController(asset))
    }

    var body: some View {
        PaybakRiveView(controller: controller)
            .sensoryFeedback(.impact(weight: .light), trigger: controller.tapCount)
    }
}
```

### 8.1 Usage per screen
```swift
// Welcome 1–3: ONE view for all three steps; only `step` changes (the file animates the transition).
struct WelcomeScreen: View {
    @StateObject private var illustration = PaybakRiveController(.onboarding)
    @State private var step = 1
    var body: some View {
        VStack {
            PaybakRiveView(controller: illustration)   // non-interactive (no listeners), swipe passes through
                .frame(width: 362, height: 340)          // Figma slot; or .frame(maxWidth: 362) and let aspectRatio size it
            // … text, dots, CTA …
        }
        .onChange(of: step, initial: true) { _, newStep in
            illustration.setNumber(name: "step", value: Float(newStep))   // 1, 2, 3 (going back animates too)
        }
    }
}

// Get Started / Setup 4 / All set / Home empty states: self-contained, with a light haptic on taps.
PaybakRiveIllustration(.getStarted).frame(width: 386, height: 284)
PaybakRiveIllustration(.notifications).frame(width: 386, height: 324)
PaybakRiveIllustration(.allSet).frame(width: 362, height: 300)
PaybakRiveIllustration(.homeFirstDay).frame(width: 240, height: 180)
PaybakRiveIllustration(.homeAllSquare).frame(width: 264, height: 204)

// For a custom reaction, keep the controller yourself:
@StateObject private var bell = PaybakRiveController(.notifications)
PaybakRiveView(controller: bell)
    .sensoryFeedback(.impact(weight: .light), trigger: bell.tapCount)
    .onAppear { bell.onTrigger("bellTapped") { /* extra side effect; runs on main */ } }
```
Don't call `fire(trigger:)` for taps; the file already fires them. `fire` is for programmatic effects only.

> **Editor's note (layout):** `.getStarted` (386×284), `.notifications` (386×324) and `.homeAllSquare` (264×204) are the Figma slot plus 12 pt of bleed on every side (rive.md). Don't put the 386/264-wide frame straight into the content stack, because it would take 24 pt of extra height and push the text down. Reserve the slot and overlay the view: `Color.clear.frame(width: 362, height: 260).overlay { PaybakRiveIllustration(.getStarted).frame(width: 386, height: 284) }` (Setup 4: 362×300 slot / 386×324 view; Home All settled: 240×180 slot / 264×204 view). Don't clip it.

### 8.2 What the simulator run showed (iPhone 17 Pro, iOS 27.0, Debug build of a scratch copy of the project)
Console output from the harness view (a test `ContentView.swift`; not kept):
```
[RIVE-VERIFY] bound onboarding=Default getStarted=Default
[RIVE-VERIFY] step -> 2
[RIVE-VERIFY] step -> 3
[RIVE-VERIFY] fired personTapped, tapCount=0          <- listener not synchronous with trigger()
[RIVE-VERIFY] personTapped handler main=true          <- delivered on next advance, main thread
[RIVE-VERIFY] paused + reduceMotion true, playing=false
[RIVE-VERIFY] resumed, playing=true
[RIVE-VERIFY] restart -> bound=Default                <- new instance re-bound, values re-applied
[RIVE-VERIFY] fired tapLeft after restart, tapCount=1
[RIVE-VERIFY] tapLeft handler main=true               <- listeners re-attached after restart
```
Screenshots were taken at t≈1.6 s (pre-roll), t≈8.5 s (Welcome 3 + Get Started) and t≈12.5 s (the pre-roll replays after `restart()`); they weren't kept.

---

## 9. project.pbxproj edits (checked on a copy first; now applied in `ios/paybak/paybak.xcodeproj`)

Target file: `ios/paybak/paybak.xcodeproj/project.pbxproj` (objectVersion 110, `PBXFileSystemSynchronizedRootGroup`). New IDs (24 hex characters, unused in the file):
- `9864C1DD082A0F5107BD3C6D`: PBXBuildFile "RiveRuntime in Frameworks"
- `D28600CA87F881AB9C261502`: XCRemoteSwiftPackageReference "rive-ios"
- `2020BBCB7A14F5E98F58F85B`: XCSwiftPackageProductDependency "RiveRuntime"

Existing IDs used: Frameworks phase `4F6137C8306D291A00F755CE`, target `4F6137CA306D291A00F755CE`, project `4F6137C3306D291A00F755CE`.

Edits, in order:
1. Add a **PBXBuildFile section** as the first section inside `objects = {`, before `/* Begin PBXFileReference section */`:
```
/* Begin PBXBuildFile section */
		9864C1DD082A0F5107BD3C6D /* RiveRuntime in Frameworks */ = {isa = PBXBuildFile; productRef = 2020BBCB7A14F5E98F58F85B /* RiveRuntime */; };
/* End PBXBuildFile section */
```
2. In `4F6137C8306D291A00F755CE /* Frameworks */` add `9864C1DD082A0F5107BD3C6D /* RiveRuntime in Frameworks */,` to `files = ( … );`.
3. In the PBXNativeTarget `4F6137CA306D291A00F755CE /* paybak */`, add between `name = paybak;` and `productName = paybak;`:
```
			packageProductDependencies = (
				2020BBCB7A14F5E98F58F85B /* RiveRuntime */,
			);
```
4. In PBXProject `4F6137C3306D291A00F755CE`, add between `minimizedProjectReferenceProxies = 1;` and `preferredProjectObjectVersion = 77;`:
```
			packageReferences = (
				D28600CA87F881AB9C261502 /* XCRemoteSwiftPackageReference "rive-ios" */,
			);
```
5. After `/* End XCConfigurationList section */`, before the closing `};` of `objects`, add:
```
/* Begin XCRemoteSwiftPackageReference section */
		D28600CA87F881AB9C261502 /* XCRemoteSwiftPackageReference "rive-ios" */ = {
			isa = XCRemoteSwiftPackageReference;
			repositoryURL = "https://github.com/rive-app/rive-ios";
			requirement = {
				kind = exactVersion;
				version = 6.28.0;
			};
		};
/* End XCRemoteSwiftPackageReference section */

/* Begin XCSwiftPackageProductDependency section */
		2020BBCB7A14F5E98F58F85B /* RiveRuntime */ = {
			isa = XCSwiftPackageProductDependency;
			package = D28600CA87F881AB9C261502 /* XCRemoteSwiftPackageReference "rive-ios" */;
			productName = RiveRuntime;
		};
/* End XCSwiftPackageProductDependency section */
```
To allow patch and minor updates instead, use `requirement = { kind = upToNextMajorVersion; minimumVersion = 6.28.0; };`. `exactVersion` is recommended so both the watermark behaviour and the API stay fixed.

The full unified diff (`project.pbxproj.diff`, build-session scratch, not kept) passed `git apply --check -p1` from the repo root on the untouched file. After editing:
```
cd ios/paybak && DEVELOPER_DIR=/Applications/Xcode.app/Contents/Developer xcodebuild -project paybak.xcodeproj -scheme paybak -resolvePackageDependencies
```
This produced `paybak.xcodeproj/project.xcworkspace/xcshareddata/swiftpm/Package.resolved` (pin `rive-ios` 6.28.0, revision `b9966d5effb9aece78c70934f0bd245523e47a18`). **Commit it.** Then `xcodebuild … -destination 'generic/platform=iOS Simulator' build` succeeds in Debug and Release. Note that `-derivedDataPath` needs `-scheme paybak`.

---

## 10. Release notes, CHANGELOG, issues, Xcode 27 / iOS 27

- 6.28.0 (2026-09-30): adds global view models, font binding and blob binding to the **Concurrency** API, plus `Artboard.createStateMachine(binding:globals:)` and `StateMachine.bindViewModelInstances(main:globals:)`. It deprecates `Rive(file:…dataBind:)`, `Rive.viewModelInstance` and `StateMachine.bindViewModelInstance(_:)`. **No changes to the legacy data-binding API we use.**
- 6.27.0: framework 24–26 % smaller, and internal symbols are now hidden. Side effect: the public header declares `RiveRenderer`, but its class symbol isn't exported (it fails to link), so don't use it.
- 6.20.6: "advance state machine after pointer down/up events", the reason tap listeners respond straight away. 6.12.0: multitouch.
- 6.15.0 added the command-queue ("Concurrency") runtime, marked experimental until spring 2026. A Rive engineer in #383 calls it "just moved out of experimental". See the appendix.
- Relevant open issues: **#383** (a data-binding trigger doesn't advance a settled legacy view until `play()`; handled by the wrapper) and **#427** (auto-bind leak on repeated create and destroy). Closed: #461 (black artboard, blamed on the file, which matches the watermark).
- **Xcode 27 / iOS 27:** searching the issues for "Xcode 27" and "iOS 27" finds nothing. The binary was built with Swift 6.3.2 and the 26.5 SDK with library evolution. It resolves, builds and runs with Xcode 27.0 (27A266a) on the iOS 27.0 simulator (24A434). **A real-device build and run was not tested.** The ios-arm64 slice is present.
- Minor finding: `stateChanges()` returns non-ASCII state names mis-decoded (`Welcome 1 · Idle` arrives as `Welcome 1 ¬∑ Idle`). Don't match on state names that contain `·`. We don't need state names anyway.

---

## 11. What was verified, and how

| Claim | How |
|---|---|
| Every signature in the wrapper and this doc | Compiled against the shipped 6.28.0 xcframework in a copy of the project with the exact target settings (Debug, Release, and a Swift 6 probe). |
| Names of artboards, state machines, view models, instances and properties; auto-bind callback counts; new instance on reset; step and reduceMotion transitions; trigger listener semantics; removeListener; hit areas; no-settle | macOS harness on the macOS slice of the same binary with the real `.riv` files (`main.swift`, `touchscan.swift`, `realtime.swift`, `wmprobe.swift`, `assets.swift`; build-session scratch, not kept). |
| Watermark pre-roll | C++ source + wall-clock probe + iOS simulator screenshots. |
| Listener thread, `play()`/`pause()`, restart re-binding, two views at once, bundle flattening, framework embedding, Package.resolved | iOS 27.0 simulator run of the wrapper plus inspection of the build output. |
| **Not verified** | A real finger tap through `RiveView` on iOS: taps were exercised through the same state machine calls in the macOS harness, not with UIKit touches. How the Welcome swipe gesture and `allowsHitTesting(false)` interact at runtime. A real-device build. Memory growth (#427) with our files. The appendix API was not compiled. |

---

## Appendix A: the newer "Concurrency" API (considered, not recommended for this phase)

Signatures come from the source and weren't compiled. `Worker()` (async, `Source/Concurrency/Worker/Worker.swift:124-127`) → `File(source: .local("paybak-onboarding", .main), worker:)` (async; `File.swift:55-56`; `Source.local(String, Bundle?)` appends the `riv` extension itself, `FileLoader.swift:14-31,114-133`) → `file.createArtboard("Onboarding")` (`File.swift:136-137`) → `file.createViewModelInstance(.viewModelDefault(from: .artboardDefault(artboard)))` (`File.swift:172-173`, `ViewModelInstance.swift:13-28`) → `artboard.createStateMachine("Onboarding", binding: vmi)` (`Artboard.swift:121-135`) → `Rive(file:artboard:stateMachine:fit: .contain(alignment: .center))` (async, `@MainActor`, `Rive.swift:168-181`) → `RiveUIViewRepresentable(rive:)` with `.paused(_:)` (`RiveUIViewRepresentable.swift:17-70`) or `AsyncRiveUIViewRepresentable { … }` (109-123). Data: `vmi.setValue(of: NumberProperty(path: "step"), to: 2)` (`ViewModelInstance.swift:249`), `vmi.setValue(of: BoolProperty(path: "reduceMotion"), to: true)` (285), `vmi.fire(trigger: TriggerProperty(path: "bellTapped"))` (372), `for try await _ in vmi.stream(of: TriggerProperty(path: "bellTapped")) { … }` (386). Touches go to the state machine in `RiveUIView.touchesBegan/…` (`RiveUIView.swift:566-593`). According to Rive (#383), property sets mark the view dirty, so no `play()` is needed.

Why it isn't chosen:
- Everything is `async`, so each screen needs a loading state and draws an empty frame first.
- The API is still changing: 6.28.0 deprecates its `dataBind:` initializer and `viewModelInstance`.
- Triggers arrive through a command queue on a worker thread and then an `AsyncThrowingStream`. #446 (closed) reported delayed batched trigger bursts in 6.19.2.
- The legacy path is proven end to end above.

The watermark affects both APIs.

## Appendix B: gotchas checklist
- The files are watermarked, so each illustration opens with a ~2 s black pre-roll (§1). **Get clean exports before shipping.**
- Always pass the artboard name. The files also contain `Watermark`, `NuRiveBrandmark01` and `NuRiveWordmark01`, and the default artboard isn't guaranteed to be the one we want.
- `RiveViewModel(fileName:…)` crashes (`try!`/`fatalError`) on a missing file or a wrong name. The wrapper checks first and returns nil.
- Auto-bind hands you a new instance after every `reset()`/`stop()`. Re-apply values and re-attach listeners, as `didBind` does.
- Listeners run on the next advance, never inside `trigger()`. Setters on a paused view need `play()`.
- Observe only the "something was tapped" trigger per file, to get one haptic per tap.
- One controller per visible view. `RiveViewModel` drives only the most recently created `RiveView`.
- `import RiveRuntime` and `import Combine` wherever needed (MemberImportVisibility).
- `.allowsHitTesting(false)` on Onboarding, so swipes work over it.
