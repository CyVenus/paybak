# Paybak native build: flow and requirements (source of truth)

Repo: the Paybak monorepo (this repo). The app is built natively in both:
- iOS: `ios/paybak/paybak.xcodeproj` with SwiftUI. Deployment target iOS 27.0, Xcode 27. If `xcode-select` points at the CommandLineTools rather than Xcode, **prefix every Xcode command with `DEVELOPER_DIR=/Applications/Xcode.app/Contents/Developer`**. The project uses file-system synchronized groups (objectVersion 77/110), so files placed under `ios/paybak/paybak/` are included in the target automatically. There's no pbxproj edit for sources/resources; package dependencies DO need pbxproj edits. `SWIFT_DEFAULT_ACTOR_ISOLATION = MainActor`, Swift 5 mode, approachable concurrency.
- Android: `android/` with Kotlin + Jetpack Compose, package `app.paybak.paybak`, AGP 9.4.1, Kotlin 2.2.10, compileSdk/targetSdk 37, minSdk 24, Compose BOM 2026.02.01, JDK 25, Gradle 9.6. Version catalog: `android/gradle/libs.versions.toml`. Build with `cd android && ./gradlew assembleDebug`.

Figma file key: `2SPNUpHlG8bCO62YfwRuRi`. Pages: 01 Foundations (3:2), 02 Components (3:3), 03 Launch & Onboarding (3:4), 04 Home (3:5). Canvas is iPhone 17 Pro, 402 × 874 pt. One light theme only. Font is Manrope. Icons are HugeIcons stroke-rounded; illustrations are Open Peeps / Open Doodles, **most illustrations are Rive files (see rive.md)**.

## Screens and navigation

| id (debug start screen) | Figma frame (node) | Notes |
|---|---|---|
| `splash` | Splash (22:32) | Mark + wordmark on white. **Animated** (native, our own; no .riv for this). Auto-advance after ~1.5 s with a 0.4 s dissolve to Welcome, or to Home if onboarding is already complete. |
| `welcome1`, `welcome2`, `welcome3` | Welcome 1 — Split (22:55), Welcome 2 — Track (22:133), Welcome 3 — Settle (22:200) | ONE screen with `step` 1…3. Illustration slot = `paybak-onboarding.riv` (it animates the slide transitions itself when `step` changes). Headline/body/page dots/CTA change per step (animate text change). Continue → next step; step 3 CTA "Get started" → Get Started. "Skip" (top-right, hidden on step 3) → Get Started. Horizontal swipe also changes step. Back from step N>1 → step N-1. |
| `getStarted` | Get Started (22:272) | Illustration card = `paybak-getstarted.riv`. **Continue with Apple / Continue with Google → go straight to Setup step 1 ("What's your name?")**, no code (backend not built). "Continue with email or phone" → Sign in. Terms / Privacy Policy links do nothing (no URLs yet). |
| `signIn` | Sign in — Email or phone (39:356) | One field that takes email or phone. "Send code" is enabled only for a plausible email (`x@y.z`) or phone (≥ 7 digits, may start with +, spaces/dashes allowed). → Verify. |
| `verify` | Sign in — Verify code (39:534) | 6 digit boxes. "Sent to {contact} · Change" (Change → back to Sign in). Auto-verifies when the 6th digit is entered. **Correct code is `000000`** (stub, no backend) → Setup step 1. Anything else → wrong-code state. "Resend code" unlocks after 30 s (show countdown until then); resend clears the boxes and restarts the timer. |
| `verifyWrong` | Sign in — Wrong code (39:644) | Same screen in error state: red rings + "That code didn't match. Check it and try again." Editing a digit clears the error. |
| `setup1` | Setup 1 — Name & photo (42:665) | Step 1 of 4. Pick a line-art avatar (5 options) or the camera option (opens the system photo picker; show the chosen photo in the circle). Initials are the fallback. Name field; Continue enabled when name is non-empty. Back → previous screen (Verify or Get Started). |
| `setup2` | Setup 2 — Currency (44:938) | Step 2 of 4. Search field filters ALL ISO currencies (name or code). "Suggested" = device-region currency ("· Based on your region"), fallback INR. "Popular" = USD, EUR, GBP, AED, SGD (minus the suggested one). Suggested is preselected. Continue. |
| `setup3` | Setup 3 — Payment (46:1021) | Step 3 of 4, optional ("Skip" in header). UPI ID field; "What friends see" preview card shows avatar + name + UPI, with a copy button (copies UPI to clipboard). Continue / Skip → step 4. |
| `setup4` | Setup 4 — Notifications (46:1148) | Step 4 of 4, optional ("Skip" in header). Illustration = `paybak-notifications.riv`. "Turn on notifications" → request the OS notification permission (iOS UNUserNotificationCenter; Android 13+ POST_NOTIFICATIONS), then All set whatever the answer. "Not now" / Skip → All set. |
| `allSet` | All set (46:1223) | Illustration = `paybak-allset.riv`. "You're all set, {first name}." "Go to Home" → Home (first day). Marks onboarding complete. |
| `homeFirstDay` | Home — First day (24:326) | Default Home after setup. Empty-state card illustration = `paybak-homefirstday.riv`. "Add expense" / "Invite friends" buttons do nothing yet (or open the Add sheet for Add expense). |
| `homeActive` | Home — Active (24:5) | Sample data exactly as in Figma. Balance cards, Settle up, Due soon rows, Recent activity. Content scrolls under the glass tab bar. |
| `homeAllSettled` | Home — All settled (24:414) | Empty-state illustration = `paybak-home-allset.riv` ("You're all square."). |
| `homeAddSheet` | Home — ＋ Action sheet (24:520) / Overlay — Add sheet (24:808) | ＋ in the tab bar opens a floating sheet over a 40 % scrim; ✕ or tapping the scrim closes it. Rows do nothing yet (close the sheet). |

Tab bar: Home · Groups · ＋ · Activity · Profile. Only Home is built; other tabs are inert (don't navigate, can show selected state or nothing). The Profile screen is out of scope. *(Onboarding-phase scope; superseded by "FULL APP SCOPE" below.)*

Greeting: "Good morning/afternoon/evening, {first name}" from the local time (5–12 morning, 12–17 afternoon, else evening).

## Persistence (user decision)
Save the profile locally: name, avatar choice (preset index or custom photo file), currency code, UPI ID, notifications choice, sign-in method (apple/google/email/phone) + contact, `onboardingComplete`. iOS: UserDefaults (photo in Application Support). Android: SharedPreferences (photo in filesDir). On launch: Splash → Home if complete, else Welcome.

## Debug-only hooks (both platforms, never in release)
- Start screen override. iOS launch argument `-startScreen <id>`; Android intent extra `--es startScreen <id>`. `<id>` is from the table above. When starting mid-flow, seed a sample profile (name "Arjun Mehta", avatar 0, INR, UPI "arjun@okaxis") so screens render like Figma.
- Reset: iOS launch argument `-resetOnboarding YES`; Android intent extra `--ez resetOnboarding true`. Clears the saved profile.
- Hidden gesture on Home: long-press the "Paybak" logo in the Home header → small debug menu: switch Home state (First day / Active / All settled) and "Reset onboarding".

## Platform notes
- Status bar and home indicator in Figma are system UI; don't draw them. Lay out content from the safe area (Figma y = 62 is the top safe-area inset, 34 bottom).
- iOS: use SwiftUI. Liquid Glass (`.glassEffect`) is available (iOS 26+). Use it for the tab bar / glass buttons where Figma uses Material/Glass.
- Android: mirror the design exactly (same layout, Manrope, custom components). Use edge-to-edge, system back (BackHandler) matching the in-app back chevrons, IME insets. Glass: translucent white (color/bg/glass = white 72 %) + shadow + 1 px white-60 % highlight border; a real backdrop blur is optional.
- Haptics: light impact when a Rive character/bell is tapped (observe the view-model trigger if the runtime supports it cleanly; otherwise skip).
- Respect Dynamic Type/font scale reasonably, but match Figma at default size.

## Testing on devices (test on BOTH the iOS simulator and the Android emulator)
- iOS: an "iPhone 18 Pro" simulator (402×874 pt @3x, the same size as the Figma frames). In Xcode 27 simulators show in the Device Hub (there's no standalone Simulator.app). `<udid>` below is the simulator's UDID (`xcrun simctl list devices`).
  - Build: `cd ios/paybak && xcodebuild -project paybak.xcodeproj -scheme paybak -destination 'platform=iOS Simulator,id=<udid>' -derivedDataPath <DerivedData> build`
  - Install: `xcrun simctl install <udid> <DerivedData>/Build/Products/Debug-iphonesimulator/paybak.app`
  - Launch into a screen: `xcrun simctl launch --terminate-running-process <udid> app.paybak.paybak -startScreen welcome2`
  - Screenshot: `xcrun simctl io <udid> screenshot out.png` (1206×2622; the Figma refs are 804×1748 at 2x, so scale to compare).
  - (Prefix xcrun/xcodebuild with `DEVELOPER_DIR=/Applications/Xcode.app/Contents/Developer` if `xcode-select` doesn't point at Xcode.)
- Android: a Pixel 10 Pro emulator (AVD Pixel_10_Pro, 1280×2856 @ 480 dpi ≈ 427×952 dp, API 37). `<serial>` below is its adb serial (`adb devices`, e.g. `emulator-5554`).
  - Build + install: `cd android && ./gradlew installDebug` (or `adb -s <serial> install -r app/build/outputs/apk/debug/app-debug.apk`).
  - Launch into a screen: `adb -s <serial> shell am start -S -n app.paybak.paybak/.MainActivity --es startScreen welcome2`
  - Screenshot: `adb -s <serial> exec-out screencap -p > out.png`. Taps: `adb -s <serial> shell input tap X Y` (px), text: `adb shell input text ...`. Use these to walk real flows end to end.
- The app was built and verified this way: every implementation step ended with the app installed and running on its device, and each screen was screenshotted and compared with `docs/design/ref/<id>.png`.

## Resolved decisions (lead engineer, 2026-09-30). These override "needs a decision" items in README §5.1.
- Back from Get Started → Welcome, at the step the user left from (step 3 if they came via "Get started", or the step they tapped Skip on).
- Android system back: Welcome step N>1 → step N−1; step 1 → exits the app (default). Swiping forward on step 3 does nothing; the CTA is the only way forward. Every other screen's system back = its in-app back chevron. From All set and from Home, back does NOT return into onboarding (Home is the task root; back exits).
- Setup 1 on a fresh profile: no avatar selected, and the Continue rule needs only a name (the avatar is optional; initials are the fallback everywhere an avatar shows). The debug seed profile has avatar 0 selected, to match Figma.
- Copy not in Figma: use the spec's proposals as written (placeholders "you@example.com" style etc., the currency-search empty state, the UPI validation message, the phone rule).
- Save `onboardingComplete` when All set appears (and disable back there). "Go to Home" goes to Home (first day).
- Terms / Privacy Policy follow Figma styling exactly; tapping them does nothing yet.
- Portrait only on phones (iOS iPhone orientations = portrait; Android `screenOrientation="portrait"` on the activity). Leave iPad support alone (iOS TARGETED_DEVICE_FAMILY stays), but keep phone layouts centred with a max content width of 430 pt on wider screens.
- Watermark: iOS shows Rive's ~2 s watermark pre-roll because the source files are watermarked. The files need re-exporting without the watermark (open item). DON'T work around it in code. For iOS screenshots of Rive screens, wait at least 4 s after the screen appears. Android doesn't show it.
- Implement the spec's "Suggested behaviour" and motion proposals (README §5.3) as written.

## UI tests and test IDs (both platforms, from the Onboarding phase on)
- `xcrun simctl` can't tap, so real end-to-end interaction on iOS goes through an **XCUITest UI-test target** (`paybakUITests`, folder `ios/paybak/paybakUITests/`, a synchronized group), runnable with `xcodebuild test -scheme paybak -only-testing:paybakUITests -destination 'platform=iOS Simulator,id=<udid>'`. The shared scheme `ios/paybak/paybak.xcodeproj/xcshareddata/xcschemes/paybak.xcscheme` includes it in its Test action. Android uses Compose UI instrumented tests in `app/src/androidTest` (`./gradlew connectedDebugAndroidTest` on the emulator), plus `adb shell input tap` for ad-hoc checks.
- Give every interactive element and key text a stable ID, named the same on both platforms: iOS `.accessibilityIdentifier("…")`, Android `Modifier.testTag("…")` (with `testTagsAsResourceId = true` at the root so adb/uiautomator can see them). Format `<screen>.<element>`, e.g. `welcome.continue`, `welcome.skip`, `welcome.headline`, `getStarted.apple`, `getStarted.google`, `getStarted.email`, `signIn.field`, `signIn.sendCode`, `verify.code` (hidden field), `verify.resend`, `verify.change`, `verify.error`, `setup.back`, `setup.skip`, `setup.progress`, `setup1.avatar.0…4`, `setup1.camera`, `setup1.name`, `setup1.continue`, `setup2.search`, `setup2.row.<CODE>`, `setup2.continue`, `setup3.upi`, `setup3.copy`, `setup3.continue`, `setup4.enable`, `setup4.notNow`, `allSet.goHome`, `home.greeting`, `home.state.<firstDay|active|allSettled>`, `home.tab.<home|groups|add|activity|profile>`, `home.addSheet`, `home.addSheet.close`, `home.logo` (debug long-press). Root screen containers: `screen.<id>` (ids from the table above).
- UI tests launch the app with the debug launch arguments (`-resetOnboarding YES`, `-startScreen …`) and must not depend on real time beyond sensible waits (splash ≈ 1.5 s; use waitForExistence / waitUntil).
- Each phase extends the tests to cover its screens' real flow (taps, typing, the expected next screen). Tests must pass before the phase's final commit (the same rule applies to later changes).

## FULL APP SCOPE (user request, 2026-09-30 evening). This supersedes "Profile is out of scope".
The user wants the WHOLE app from Figma built, working, beautiful and well crafted on both platforms: pages 03–12 (Launch & Onboarding, Home, Profile + avatar editor, Add & Record, Groups & Friends, Settle Up, Activity, Projects, Insights & AI, Settings & Pro). Every button should do something sensible; no dead ends.
- **Rive:** the six .riv files are ALL the Rive assets there are (no avatar.riv or anything else). Where a screen's art is the same as an existing .riv (e.g. an empty state that reuses the First Day or AllSquare character), reuse that .riv. Where art has no .riv, export it from Figma (SVG/PNG) and rebuild it natively (for the avatar editor, compose the exported part layers natively in the Figma rig space).
- **No backend:** everything runs on-device against a local data store with REAL logic (balances from expenses and splits, simplify debts, pending payments that need confirmation, loans with installments, project budgets, recurring rules, activity timeline, notifications inbox, insights aggregation) and persists across launches. Money never moves; Paybak only records it (per the Figma cover text).
- **Data:** a new account after onboarding starts EMPTY (Home "First day"; Groups/Activity empty states). A **demo dataset** that reproduces the Figma sample data exactly (Arjun and his friends, groups, projects, etc., with dates relative to "now" so it matches Figma on Wed 30 Sep 2026) is loaded by the debug start-screen hooks and from the debug menu ("Load demo data"). Every Figma number (e.g. Home +₹2,900 from 4 people / −₹1,850 across 2 groups) must come out of the real calculations on the demo data, not hard-coded strings.
- **Real vs simulated** (prefer the real platform capability when it's cheap and reliable; otherwise simulate behind a clean seam): real = photo picker, camera where available, share sheet, clipboard, QR generation, local notifications (reminders, the lock-screen "confirm payment" push with actions), CSV/PDF export via the share sheet, haptics. Simulated = the other person's side (a friend confirming or marking "Not received" can be triggered from the debug menu), Pro purchase (originally a mock that flipped a local entitlement; now real through the RevenueCat SDK, see `docs/revenuecat.md`), Ask Paybak answers (a deterministic on-device assistant that answers the suggested prompts and simple questions from live data and drafts expenses from simple phrases), and receipt reading (on-device text recognition if cheap, e.g. iOS Vision; otherwise a demo parse of the sample receipt behind the same interface).
- **Pro:** free plan by default (like Arjun on the paywall). The debug menu can toggle Pro so the Pro-only screens can be checked. Locked features open the paywall.
