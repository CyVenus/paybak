# Rive assets: verified facts

The six source files live in the platform projects (there is no other copy in the repo):
- iOS: `ios/paybak/paybak/Resources/Rive/` (original file names). The synchronized group bundles them automatically.
- Android: `android/app/src/main/res/raw/` renamed with underscores (`paybak_onboarding.riv`, `paybak_getstarted.riv`, `paybak_notifications.riv`, `paybak_allset.riv`, `paybak_homefirstday.riv`, `paybak_home_allset.riv`).

Runtimes (latest stable, Sep 2026): **rive-ios 6.28.0** (SPM: https://github.com/rive-app/rive-ios, product `RiveRuntime`) and **rive-android 11.12.1** (Maven: `app.rive:rive-android`).

Every file also has three unrelated artboards (`Watermark`, `NuRiveBrandmark01`, `NuRiveWordmark01`, which are Rive's own logo). **Always load the main artboard by name.**

I inspected these with the official Rive WASM runtime (2.43) and headless Chrome. Every main artboard has one state machine with the same name, plus a view model that is the artboard's default and has one instance.

| File | Artboard (size) | State machine | View model (instance) | Properties | Figma slot |
|---|---|---|---|---|---|
| paybak-onboarding.riv | `Onboarding` (362×340) | `Onboarding` | `Onboarding` (`Default`) | `step`: number (default 1), `reduceMotion`: bool | Welcome 1–3 "Illustration" (362×340 at x20 y114) |
| paybak-getstarted.riv | `Get Started` (386×284) | `Get Started` | `GetStarted` (`Default`) | `personTapped`, `tapLeft`, `tapMiddle`, `tapRight`: trigger; `reduceMotion`: bool | Get Started illustration card (includes the grey rounded card) |
| paybak-notifications.riv | `Notifications` (386×324) | `Notifications` | `Notifications` (`Default`) | `bellTapped`: trigger; `reduceMotion`: bool | Setup 4 illustration |
| paybak-allset.riv | `All Set` (362×300) | `All Set` | `AllSet` (`Instance`) | `personTapped`, `tapBadge`, `tapLeft`, `tapMiddle`, `tapRight`: trigger; `reduceMotion`: bool | All set illustration |
| paybak-homefirstday.riv | `First Day` (240×180) | `First Day` | `HomeFirstDay` (`Instance`) | `characterTapped`, `tapCharacter`: trigger; `reduceMotion`: bool | Home first-day empty-state illustration |
| paybak-home-allset.riv | `AllSquare` (264×204) | `AllSquare` | `AllSquare` (`Default`) | `tapped`: trigger; `reduceMotion`: bool | Home all-settled empty-state illustration (slot 240×180; the artboard adds 12 pt bleed, see Fit below) |

## Behaviour (verified)
- **Data binding is required**: bind the default view-model instance to the state machine (auto-bind is fine). With nothing bound, `step` changes and triggers do nothing.
- Onboarding: starts at `Welcome 1 · Enter` → `Welcome 1 · Idle`. Setting `step`=2 plays `Welcome 1 · Off Left` + `Welcome 2 · Enter`; `step`=3 → Welcome 3; going back (3→2, 2→1) also animates. No listeners, so no touch handling is needed. Keep one Rive view for all three welcome steps and just change `step`.
- Get Started / All set / First Day / AllSquare / Notifications: each starts with an Intro/Enter animation, then idles in a loop. **The files have their own tap listeners**: tapping a person fires `tapLeft|tapMiddle|tapRight` (+ `personTapped`), tapping the badge fires `tapBadge` (+ `personTapped`), tapping the character fires `tapCharacter` (+ `characterTapped`), tapping the AllSquare character fires `tapped`, tapping the bell card fires `bellTapped`. Each plays a jump/boing/ring animation. **So the app only has to pass touches through to the state machine** (native runtimes do this by default). Don't fire tap triggers yourself on tap, or you'll double-fire. `personTapped`/`characterTapped` are "something was tapped" signals; they drive no state. Use them (or the specific trigger) for a light haptic if the runtime lets you observe trigger properties.
- `reduceMotion` = true switches to a `Still` state (static pose). User: keep it false in normal use. **Bind it to the OS Reduce Motion accessibility setting** (iOS `UIAccessibility.isReduceMotionEnabled` / SwiftUI `accessibilityReduceMotion`; Android: `Settings.Global.ANIMATOR_DURATION_SCALE == 0f`). That's false by default.
- Fit: use `contain`, center alignment, and give the **Rive view the artboard size** (keep aspect ratio if the width changes). **Correction (editor, verified):** three artboards are NOT the Figma slot size. Each is the Figma slot plus **12 pt of bleed on every side**, and it draws its art at the same scale as Figma. So the layout reserves the Figma slot, and the Rive view is centred on that slot at its native size, overflowing it by 12 pt on each side, with no clipping and no scaling down:

| Artboard | Artboard size | Figma layout slot (frame coords) | Rive view rect (frame coords) | How it was verified |
|---|---|---|---|---|
| `Onboarding` | 362×340 | (20, 114, 362, 340) | same as slot | size match |
| `Get Started` | 386×284 | card (20, 146, 362, 260) | **(8, 134, 386, 284)** | pixel measurement of the card in the render (screens-launch.md §3.5) |
| `Notifications` | 386×324 | (20, 168, 362, 300) | **(8, 156, 386, 324)** | ink bounding box of the `Still` render vs `assets/images/illustration-reminders.svg`: same size (334×278), offset exactly (+12, +12) |
| `All Set` | 362×300 | (20, 106, 362, 300) | same as slot | ink box identical to `illustration-all-set.svg` |
| `First Day` | 240×180 | (81, 204, 240, 180) | same as slot | ink box identical to `empty-first-day.svg` |
| `AllSquare` | 264×204 | (81, 204, 240, 180) | **(69, 192, 264, 204)** | ink box vs `empty-all-square.svg`: same size (202×154), offset exactly (+12, +12) |

  The ink-box checks were rendered with `@rive-app/canvas-advanced` 2.43 (build-session scratch scripts, not kept).
- Background: artboards are transparent/white. The Get Started artboard draws its own grey rounded card, so don't draw a native card behind it.

## Watermark pre-roll (editor, verified; see rive-ios-api.md §1)
All six files carry Rive's export watermark flag. Newer runtimes play a pre-roll of about 2 s on every new artboard instance: an opaque black box with a white "RIVE" wordmark. It runs on wall-clock time, and the illustration is frozen underneath until it ends.
- **rive-ios 6.28.0: shows it** (confirmed with iOS 27 simulator screenshots during the build; not kept).
- **@rive-app/canvas-advanced 2.43 (web/WASM): shows it** (headless render stays solid black until about 2.5 s of real time have passed).
- **rive-android 11.12.1: does NOT show it.** Checked on the Android emulator with a separate harness APK: the Get Started illustration was already drawing about 1.1 s after the activity started, with no black frame.
- Fix: re-export the six files without the watermark. That's a decision for the asset owner, and nothing in code changes once clean files are in place.

## Payment scene (`paybak-payment.riv`, 2026-10-01)
The payer's "payment approved" moment: when a friend confirms a payment you made, the scene plays full screen over whatever is open (iOS `PaymentApprovalPresenter`, Android `PaymentApprovedHost`). The files are `ios/paybak/paybak/Resources/Rive/paybak-payment.riv` and `android/app/src/main/res/raw/paybak_payment.riv`. Facts below come from parsing the file against the rive-runtime headers, then checking on the iOS 27 simulator.
- **Artboard `main`** (402×874). It's built with **Rive layouts**: an opaque white fill, plus a layout that fills the artboard and holds the nested artboard `Palm Scene` (920×980), contained and bottom-aligned. Draw it with **Layout fit** at the view's size in points or dp. iOS `.layout` uses the automatic scale factor; Android needs `Fit.Layout(density)`, because its default scale factor is 1 px.
- **State machine `State Machine 1`.** It plays the nested `Palm Scene`. There are **no inputs, view models, events or listeners**, so nothing to bind and no `reduceMotion`. The scene plays with Reduce Motion on too, by the user's choice.
- **No watermark**, so there's no pre-roll.
- **The coin sequence** runs hand-rise (126 frames), coin-drop (104), hand-grab (66), coin-shatter (88) and hand-lower (76): 460 frames at 60 fps ≈ **7.67 s**. It then loops; an idle bounce runs alongside it. The overlay closes after one pass.
- **The coin-shatter** sends burst lines across the whole screen and flashes the scene black for a moment. The native headline therefore sits on a white rounded backing, which only shows during that flash.
