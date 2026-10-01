# Launch & Onboarding screens: `splash`, `welcome1`–`welcome3`, `getStarted`

Source: Figma file `2SPNUpHlG8bCO62YfwRuRi`, page "03 Launch & Onboarding" (3:4), section "Launch & Onboarding" (22:29). All numbers below were read from the node tree with the Plugin API (read-only), not estimated from pictures.

| Screen id | Figma frame | 2x reference (804×1748) | 1x reference (402×874) | Node JSON (`.figma-cache/`, regenerate with `tools/fetch_figma.py`) |
|---|---|---|---|---|
| `splash` | Splash (22:32) | `ref/splash.png` | `ref/splash_1x.png` | `nodes/3-4.json` |
| `welcome1` | Welcome 1 — Split (22:55) | `ref/welcome1.png` | `ref/welcome1_1x.png` | `nodes/3-4.json` |
| `welcome2` | Welcome 2 — Track (22:133) | `ref/welcome2.png` | `ref/welcome2_1x.png` | `nodes/3-4.json` |
| `welcome3` | Welcome 3 — Settle (22:200) | `ref/welcome3.png` | `ref/welcome3_1x.png` | `nodes/3-4.json` |
| `getStarted` | Get Started (22:272) | `ref/getStarted.png` | `ref/getStarted_1x.png` | `nodes/3-4.json` |

The reference PNGs include the iOS status bar and home indicator (kit instances). Don't draw those. In the references the illustrations are the static Figma art; in the app they are Rive files, so the art can differ slightly (idle animation poses).

**`welcome1`, `welcome2` and `welcome3` are ONE screen (one view/composable) with a `step` state (1…3).** They are not three destinations. The debug ids `welcome1/2/3` only choose the starting step.

## Conventions in this file
- Coordinates are pt/dp in the 402 × 874 frame, written `(x, y, w, h)`. **safe-y** = y − 62 is measured from the top safe-area edge. The bottom safe-area edge is at frame y = 840 (874 − 34). **bottom-gap** = the distance from an element's bottom edge to y = 840.
- Colours are token names from `tokens.md`, with hex values. Text styles are the style names in `tokens.md`. Letter spacing in pt: Title/1 −0.64, Body 0, Button/Large −0.085, Button/Small −0.0375, Footnote 0, Brand/Wordmark L −1.2, Brand/Wordmark S −0.6.
- Status bar and home indicator in the frames are iOS kit instances (system UI). **Do not draw them.** Use dark status-bar content (light theme only).
- Lay out from the safe area with 20 pt side margins (`layout/screen-margin`). Don't hard-code the 62/34 insets.

## Designer notes on the canvas (verbatim)
- 22:31 (section subtitle): "Splash · Welcome 1–3 · Get Started. Sign in and setup follow in Phase 2 on this page. iPhone 17 Pro, 402 × 874."
- 22:33 (under Splash): "Launch. Mark and wordmark on white — nothing else. Auto-advances to Welcome 1 after 1.5 s."
- 22:56 (under Welcome 1): "Onboarding 1 of 3. Continue → Welcome 2 · Skip → Get Started."
- 22:134 (under Welcome 2): "Onboarding 2 of 3. Continue → Welcome 3 · Skip → Get Started."
- 22:201 (under Welcome 3): "Onboarding 3 of 3. Skip is hidden on the last slide. Get started → Get Started."
- 22:273 (under Get Started): "Sign-in choice. Apple (black) · Google (official G) · email or phone. The email/phone flow and setup are Phase 2."
- Annotation on Splash (22:32): "Auto-advances to Welcome 1 after 1.5 s (dissolve)."
- Annotation on Welcome 3 "Top bar" (22:203): "Skip hidden on slide 3."
- Annotation on "Continue with Google" (22:333): "Official 4-colour Google G — required by Google sign-in branding. Only colour exception."

## Prototype interactions (read from `node.reactions`)
The page's flow starting point "Onboarding" is Splash (22:32). A second flow, "Sign in & Setup", starts at 39:356.

| Frame | Node | Trigger | Destination | Transition |
|---|---|---|---|---|
| Splash | frame 22:32 | AFTER_TIMEOUT 1500 ms | Welcome 1 (22:55) | DISSOLVE, 400 ms, EASE_OUT |
| Welcome 1 | CTA "Continue" (22:112) | ON_CLICK | Welcome 2 (22:133) | PUSH, direction LEFT, 350 ms, EASE_IN_AND_OUT |
| Welcome 1 | Skip hotspot (26:258) | ON_CLICK | Get Started (22:272) | DISSOLVE, 300 ms, EASE_IN_AND_OUT |
| Welcome 2 | CTA "Continue" (22:180) | ON_CLICK | Welcome 3 (22:200) | PUSH LEFT, 350 ms, EASE_IN_AND_OUT |
| Welcome 2 | Skip hotspot (26:261) | ON_CLICK | Get Started (22:272) | DISSOLVE, 300 ms, EASE_IN_AND_OUT |
| Welcome 3 | CTA "Get started" (22:252) | ON_CLICK | Get Started (22:272) | PUSH LEFT, 350 ms, EASE_IN_AND_OUT |
| Get Started | "Continue with Apple" (22:328) | ON_CLICK | Setup 1 — Name & photo (42:665) | PUSH LEFT, 350 ms, EASE_IN_AND_OUT |
| Get Started | "Continue with Google" (22:333) | ON_CLICK | Setup 1 — Name & photo (42:665) | PUSH LEFT, 350 ms, EASE_IN_AND_OUT |
| Get Started | "Continue with email or phone" (22:342) | ON_CLICK | Sign in — Email or phone (39:356) | PUSH LEFT, 350 ms, EASE_IN_AND_OUT |

No other node in these five frames has reactions. There are no drag/swipe reactions, no back reactions, and no hotspot on Welcome 3.

**In the app:** Welcome 1 → 2 → 3 is a change of `step` inside one screen, NOT a push (see §2.5). Skip, Get started, and the three Get Started buttons are real navigations. Use the platform's standard push (iOS NavigationStack push; Android slide-in-from-right, 350 ms ease-in-out) for PUSH LEFT, and a 300 ms crossfade for Skip → Get Started. Splash → next is a 400 ms crossfade.

## Motion
- `get_motion_context` (recursive) returned **no animated nodes** for Splash (22:32), for the Brand section on 02 Components (8:7, which contains Brand / App Mark 8:22 and Brand / Logo 8:31), and for the whole Launch & Onboarding section (22:29). No motion is designed in Figma. The splash animation in §1.4 and the text transitions in §2.5 are **suggestions** that fit the brand.
- The Cover page (0:1) frame "Phone — Splash" (28:487) is only a static picture of this Splash in a phone mock-up (a 257.2 × 540.4 black frame with radius 44 and an image-filled screen with radius 33). It gives no motion hints. The cover copy sets the tone: "Split it. Track it. Settle it." and "A calm shared ledger for friends, flatmates and trips. Paybak records who paid and who owes — it never moves money." So keep motion calm and minimal.
- Rive: `paybak-onboarding.riv` (Welcome) and `paybak-getstarted.riv` (Get Started) animate themselves. See `rive.md`.

---

## 0. Shared building blocks (exact component data from page 02 Components)

### 0.1 `Brand / App Mark` (8:22)
Description: "Paybak app mark. Sizes: 160 (cover), 96 (splash), 40, 28 (header lockup). SwiftUI: PBAppMark(size:)". These screens use Size=96 (8:13, on Splash) and Size=28 (8:19, on Get Started).

The mark is a black rounded square with a white stroked "P". All geometry scales linearly with the size S:

| Part | Formula | S = 96 | S = 28 |
|---|---|---|---|
| Tile | S × S, fill `color/bg/inverse` #0A0A0A, clips content | 96 × 96 | 28 × 28 |
| Corner radius | 0.2237 × S, **corner smoothing 0.6** (iOS "continuous" squircle) | 21.4752 | 6.2636 |
| Glyph stroke | 0.109375 × S, `color/icon/inverse` #FFFFFF, round caps, round joins, centred on the path | 10.5 | 3.0625 |
| Glyph path bounds (path, not ink) | origin (0.34375 S, 0.234375 S), size (0.3359 S × 0.53125 S) | (33, 22.5), 32.25 × 51 | (9.625, 6.5625), 9.406 × 14.875 |

Glyph path at S = 96, in tile coordinates (from the SVG export):
`M33 73.5 V22.5 H48.75 C53.1261 22.5 57.3229 24.2384 60.4173 27.3327 C63.5116 30.4271 65.25 34.6239 65.25 39 C65.25 43.3761 63.5116 47.5729 60.4173 50.6673 C57.3229 53.7616 53.1261 55.5 48.75 55.5 H33`
In words: a stem from (33, 73.5) up to (33, 22.5), a line right to (48.75, 22.5), a half-circle of radius 16.5 centred at (48.75, 39) round the right side down to (48.75, 55.5), then a line left back to the stem at (33, 55.5). The path is open (it ends on the stem).

Implementation: **draw it natively** (SwiftUI `RoundedRectangle(cornerRadius: 0.2237*S, style: .continuous)` + a stroked `Path`; Compose `Canvas` with the same path, or a vector drawable). Native drawing keeps it sharp at every size and lets the splash animate the tile. If you prefer a file: `assets/images/brand-app-mark-96.svg` and `assets/images/brand-app-mark-28.svg` are the exact Figma exports. **The SVGs use a plain `rx`: SVG has no corner smoothing.** On iOS the continuous corner is the closer match. On Android a plain rounded corner is fine; the difference is under 1 px at 96 pt. (The copies in `assets/brand/` are the same art.)

### 0.2 `Brand / Logo` (8:31)
Description: "Mark + wordmark lockup. Horizontal: Home header. Stacked: Splash. SwiftUI: PBLogo(layout:)".
- **Layout=Stacked** (8:27), used on Splash. Vertical stack, gap 16 (`space/16`), items centred horizontally. 135 × 156. The mark (Size=96) sits at (19.5, 0) in the lockup. The wordmark "Paybak" uses Brand/Wordmark L (Manrope ExtraBold 40/44, −3 % = −1.2 pt), `color/text/primary` #0A0A0A, and hugs its text (135 × 44 at (0, 112)).
- **Layout=Horizontal** (8:23), used on Get Started. Horizontal stack, gap 8 (`space/8`), items centred vertically. 104 × 28. The mark (Size=28) is at (0, 0). The wordmark "Paybak" uses Brand/Wordmark S (Manrope ExtraBold 20/24, −3 % = −0.6 pt), `color/text/primary`, 68 × 24 at (36, 2).
- The wordmark is live text in Manrope (not outlined). Use `Manrope-ExtraBold`.

### 0.3 `Button / Primary` (9:36), Size=Large
Description: "Main action — black pill. One Large primary per screen. SwiftUI: PBButton(.primary)".
- Height 52 (`size/button-lg`). Corner radius `radius/full` (a pill). Horizontal padding 24 (`space/24`). Horizontal stack, gap 8 (`space/8`), content centred on both axes. On these screens the button fills the width (362).
- Optional leading icon slot: 20 × 20 (hidden by default; shown on "Continue with Apple").
- Label: Button/Large (Manrope SemiBold 17/22, −0.085 pt), one line, centred.

| State | Fill | Label | Figma variant |
|---|---|---|---|
| Default | `color/bg/inverse` #0A0A0A | `color/text/inverse` #FFFFFF | 9:12 |
| **Pressed** | `color/bg/inverse-pressed` #2B2B2B | `color/text/inverse` #FFFFFF | 9:20 |
| Disabled | `color/bg/disabled` #E0E0E0 | `color/text/disabled` #A3A3A3 | 9:28 (not used on these screens) |

The pressed fill changes instantly in Figma (no animation is designed). A 0.1 s colour fade is fine.

### 0.4 `Button / Secondary` (9:62), Size=Large
Description: "Secondary action on white backgrounds — #F5F5F5 pill. SwiftUI: PBButton(.secondary)". Same geometry as §0.3.

| State | Fill | Label | Figma variant |
|---|---|---|---|
| Default | `color/bg/card` #F5F5F5 | `color/text/primary` #0A0A0A | 9:38 |
| **Pressed** | `color/bg/card-pressed` #EBEBEB | `color/text/primary` #0A0A0A | 9:46 |
| Disabled | `color/bg/card` #F5F5F5 | `color/text/disabled` #A3A3A3 | 9:54 (not used) |

### 0.5 `Button / Text` (10:45)
Description: "Text-only action (See all, Skip, Continue with email or phone). Pressed = 50% opacity. SwiftUI: PBTextButton".
- Height 44 (`size/tap`). The width hugs the label. Horizontal stack, gap 2 (`space/2`), content centred vertically. Label is Button/Small (Manrope SemiBold 15/20, −0.0375 pt), one line, 12 pt from the top of the 44 pt box. An optional trailing `Icon / Chevron Right` (16 × 16) is hidden on these screens.
- **Style=Secondary** (10:21): label `color/text/secondary` #6B6B6B. Used for "Skip".
- **Style=Primary** (10:9): label `color/text/primary` #0A0A0A. Used for "Continue with email or phone".
- **Pressed** (10:13 / 10:25): the whole button at **50 % opacity**. **Disabled** (10:17 / 10:29): label `color/text/disabled` #A3A3A3 (not used here).

### 0.6 `Navigation / Onboarding Top Bar` (17:461)
Description: "Onboarding top bar: optional back chevron, Skip on the right (slides 1–2 only). SwiftUI: PBOnboardingTopBar".
- 362 × 44, horizontal stack, no padding, children centred vertically: [`back` (Button / Icon, Plain, 44 × 44, with `Icon / Chevron Left`)] + `spacer` (fills) + [`skip` (Button / Text, Style=Secondary, label "Skip")].
- On the Welcome screens: **Show back = false** on every step. **Show Skip = true** on steps 1 and 2, **false** on step 3. The bar keeps its 44 pt height when Skip is hidden, so nothing below moves.
- (The Sign-in screens use the same bar with back shown. See `screens-signin.md` §0.2 for the back button.)

### 0.7 `Control / Page Dots` (12:230)
Description: "Onboarding pager. Active dot is a 24×8 black pill. SwiftUI: PBPageDots(count:active:)".
- Horizontal stack, gap 6 (`space/6`), items centred vertically. 3 items. Total width 52, height 8.
- Active item: 24 × 8 pill, fill `color/bg/inverse` #0A0A0A, radius `radius/full`.
- Inactive item: 8 × 8 circle, fill `color/bg/indicator` #D1D1D1, radius `radius/full`.
- x offsets inside the control:

| Active | dot 1 | dot 2 | dot 3 | Figma variant |
|---|---|---|---|---|
| 1 | pill 0…24 | circle 30…38 | circle 44…52 | 12:218 |
| 2 | circle 0…8 | pill 14…38 | circle 44…52 | 12:222 |
| 3 | circle 0…8 | circle 14…22 | pill 28…52 | 12:226 |

- Not interactive in Figma. Keep it decorative: tapping the dots does nothing. Accessibility value "Page N of 3".

### 0.8 Icons used here (from `assets/icons/`, 24 × 24 viewBox, drawn at 20 × 20 in the buttons)
- `apple.svg` = `Icon / Apple` (5:115), "Apple logo (filled) for Sign in with Apple. Override to color/icon/inverse on black buttons." The file's fill is #0A0A0A (`color/icon/primary`). **Tint it `color/icon/inverse` #FFFFFF on the black Apple button.** Inside the 20 × 20 box the glyph's bounds are (1.854, 0, 16.292, 20).
- `google.svg` = `Icon / Google` (5:121), "Official Google G (brand colours required by Google sign-in branding). Do not recolour." Four fills: #4285F4, #34A853, #FBBC05, #EB4335. **Never tint it.** On Android, load it as a multi-colour vector drawable (no `tint`). On iOS, use it as an original-rendering image (not a template).

---

## 1. Splash (`splash`, 22:32)

### 1.1 Purpose
Launch screen. It shows the brand mark and the wordmark on white, then moves on after 1.5 s: to Welcome (step 1) if onboarding isn't complete, otherwise to Home (see `flow.md`).

### 1.2 Frame
- Fill `color/bg/primary` #FFFFFF, edge to edge (the status-bar and home-indicator areas are white as well).
- Vertical auto-layout, `main=CENTER`, `cross=CENTER`, padding left/right 20 (`layout/screen-margin`), top/bottom 0. **The lockup is centred in the full 874 pt screen, not in the safe area.** Its centre is at y = 437, which is 14 pt above the safe-area centre (451). In code, centre it in a container that ignores the safe areas.

### 1.3 Elements (exact geometry)
| Element | Node | Frame rect (x, y, w, h) | safe-y | Details |
|---|---|---|---|---|
| Logo lockup | `Brand / Logo`, Layout=Stacked (instance 22:34 of 8:27) | (133.5, 359, 135, 156) | 297 | Vertical stack, gap 16, centred. |
| Mark | `Brand / App Mark`, Size=96 (I22:34;8:28) | (153, 359, 96, 96) | 297 | Fill `color/bg/inverse` #0A0A0A, radius 21.4752, smoothing 0.6. |
| P glyph (path bounds) | vector "glyph" (I22:34;8:28;8:15) | (186, 381.5, 32.25, 51) | 319.5 | Stroke 10.5 `color/icon/inverse` #FFFFFF, round caps and joins. With the stroke, the ink spans x 180.75…223.5 and y 376.25…437.75. |
| Wordmark | text "wordmark" (I22:34;8:30) | (133.5, 471, 135, 44) | 409 | "Paybak", Brand/Wordmark L (Manrope ExtraBold 40/44, −1.2 pt), `color/text/primary` #0A0A0A, centred. It sits 16 pt below the mark (359 + 96 + 16 = 471). |

Nothing else is on the screen: no status-bar tint, no loader, no version text.

### 1.4 Suggested splash animation (nothing is designed; calm, minimal, about 1.2 s)
The Figma prototype is AFTER_TIMEOUT 1500 ms → DISSOLVE 400 ms EASE_OUT to Welcome 1. The suggestion keeps exactly that timing and adds a short entrance.

| Time (from the first frame the splash is on screen) | What happens |
|---|---|
| 0.00 s | White screen. Mark at scale 0.86, opacity 0 (scale about its own centre). Wordmark at opacity 0, offset y +8 pt. |
| 0.00 → ~0.60 s | **Mark springs in**: scale 0.86 → 1.0 with a gentle spring (iOS `.spring(response: 0.55, dampingFraction: 0.72)`; Compose `spring(dampingRatio = 0.72f, stiffness = 130f)`), plus opacity 0 → 1 over 0.30 s ease-out. At most a tiny overshoot (about 1–2 %), no bounce. |
| 0.35 → 0.85 s | **Wordmark fades and slides up**: opacity 0 → 1 and y +8 → 0 over 0.50 s, ease-out (cubic-bezier 0.22, 1, 0.36, 1; Compose `CubicBezierEasing(0.22f, 1f, 0.36f, 1f)`). |
| 0.85 → 1.50 s | Hold. Everything is still. |
| 1.50 → 1.90 s | **Dissolve** (crossfade) to the next screen (Welcome step 1, or Home when onboarding is complete): 0.40 s, ease-out. The next screen fades in over the splash. There is no slide. |

- Start the 1.5 s timer when the splash first appears. Don't restart it on recomposition or re-render.
- The Welcome screen's Rive (`paybak-onboarding.riv`) starts its own "Welcome 1 · Enter" animation when it loads. Start the Rive view when it appears (during the dissolve), so its entrance plays as the splash fades out.
- **Reduce Motion** (iOS `accessibilityReduceMotion`; Android animator duration scale 0): no scale or slide. Show the lockup at once (or fade it in over 0.2 s), keep the 1.5 s hold, and keep the 0.4 s crossfade (a crossfade is fine under Reduce Motion).
- Native launch screen, so there's no double splash or flash: **iOS**: plain white `UILaunchScreen` (background white; force light appearance, because the app is light-only). **Android 12+**: the system SplashScreen API draws first. Give it a white `windowSplashScreenBackground` and a transparent/empty `windowSplashScreenAnimatedIcon` (or install `androidx.core:core-splashscreen` with the same settings) and dismiss it on the first frame, so the in-app Splash above is the only branded splash. Keep the Android window background white.

### 1.5 Assets used
- Brand / App Mark 96: draw natively (§0.1), or `assets/images/brand-app-mark-96.svg`.
- Font `Manrope-ExtraBold.ttf` (PostScript name `Manrope-ExtraBold`; bundled in the apps, see README).

---

## 2. Welcome (`welcome1` / `welcome2` / `welcome3`: one screen, `step` 1…3)

### 2.1 Purpose
Three-page intro to Paybak. It's one screen whose `step` drives the Rive illustration, the headline, the body text, the page dots, the CTA label and whether Skip is visible. Continue advances the step. "Get started" (step 3) and Skip go to Get Started.

### 2.2 Frame (identical on all three steps; verified node by node)
- Fill `color/bg/primary` #FFFFFF.
- Vertical auto-layout, `main=SPACE_BETWEEN`, `cross=MIN` (leading). Padding: top 62 (`layout/status-bar`), left/right 20 (`layout/screen-margin`), **bottom 50** (a raw value = 34 home indicator + 16). Two children: **Content** (top-anchored) and **Footer** (bottom-anchored). The space between them flexes (142 pt on this 874 pt frame).
- In code: `VStack { Content; Spacer(minLength: 24); Footer }` inside the safe area, with 20 pt side margins and 16 pt bottom padding above the bottom safe-area edge.

### 2.3 Elements, top to bottom (constant layout)
| # | Element | Node (Welcome 1 ids; Welcome 2 / 3 use the same structure) | Frame rect (x, y, w, h) | safe-y / bottom-gap | Details |
|---|---|---|---|---|---|
| 1 | **Content** column | 22:57 | (20, 62, 362, 536) | safe-y 0 | Vertical stack, gap 0, leading-aligned; fills the width and hugs its height. Holds items 2–5 with explicit spacer frames. |
| 2 | Top bar | `Navigation / Onboarding Top Bar` instance 22:58 (Show back=false, Show Skip=true on steps 1–2) | (20, 62, 362, 44) | safe-y 0 | §0.6. |
| 2a | Skip button | `Button / Text`, Style=Secondary, State=Default, label "Skip" (I22:58;17:468) | (351, 62, 31, 44); label (351, 74, 31, 20) | safe-y 0 | Button/Small, `color/text/secondary` #6B6B6B. The right edge sits on the 20 pt margin (x = 382). Pressed = 50 % opacity. |
| 2b | Skip **hit area** | "Skip hotspot" 26:258 (Welcome 1), 26:261 (Welcome 2); invisible frame, no fill | (343, 62, 47, 44) | safe-y 0 | The tap target extends 8 pt past the label on both sides (x 343…390). Make the whole 47 × 44 area tappable (e.g. 8 pt horizontal padding on the Skip button with the label kept at x 351). → Get Started (crossfade 300 ms). **Don't render the hotspot.** |
| 3 | Spacer | "gap" 22:66 | h 8 | | 8 pt between the top bar and the illustration. |
| 4 | **Illustration slot = Rive** | "Illustration" instance 22:67 (`Illustration / Welcome 1 — Split` 7:82; 7:117 on step 2; 7:142 on step 3) | **(20, 114, 362, 340)** | safe-y 52 | Replaced by **`paybak-onboarding.riv`**: artboard `Onboarding` (362 × 340), state machine `Onboarding`, view model `Onboarding`, instance `Default` (auto-bind). Fit `contain`, alignment centre. The view is exactly 362 × 340 and transparent (no fill, no card). Figma clips the slot; the Rive art stays inside its artboard. See §2.6. |
| 5 | Spacer | "gap" 22:103 | h 32 | | 32 pt between the illustration and the text. |
| 6 | **Text** block | 22:104 | (20, 486, 362, 112) | safe-y 424 | Vertical stack, gap 12 (`space/12`), leading-aligned, fills the width. |
| 6a | Headline | 22:105 | (20, 486, 362, 76) | safe-y 424 | Title/1 (Manrope ExtraBold 32/38, −0.64 pt), `color/text/primary` #0A0A0A, left-aligned, wraps (2 lines on every step). |
| 6b | Body | 22:106 | (20, 574, 362, 24) | safe-y 512 | Body (Manrope Regular 16/24, 0), `color/text/secondary` #6B6B6B, left-aligned, wraps (1 line on every step). |
| 7 | **Footer** | 22:107 | (20, 740, 362, 84) | bottom-gap 16 | Vertical stack, gap 24 (`space/24`), leading-aligned, fills the width. |
| 7a | Page dots | `Control / Page Dots`, Active=step (22:108) | (20, 740, 52, 8) | | §0.7. Left-aligned at x = 20. |
| 7b | CTA | `Button / Primary`, State=Default, Size=Large, Leading icon=false (22:112) | (20, 772, 362, 52) | bottom-gap 16 | §0.3. Label centred: "Continue" is 76 × 22 at (163, 787); "Get started" is 92 × 22 at (155, 787). |

Accessibility: the Rive view is decorative (hide it from VoiceOver/TalkBack). Read the headline as a heading. Skip label "Skip". The dots are "Page N of 3".

### 2.4 Per-step differences (everything else is identical)
| | Step 1 (22:55 "Welcome 1 — Split") | Step 2 (22:133 "Welcome 2 — Track") | Step 3 (22:200 "Welcome 3 — Settle") |
|---|---|---|---|
| Rive `step` | 1 | 2 | 3 |
| Figma art (reference only) | Two friends sitting, a receipt icon in a circle between them | A person pointing at a ledger card with a calendar | Two friends settling up, a check in a circle between them |
| Headline (verbatim) | Split any bill in seconds. | Know who owes what, and by when. | Settle up without the awkward chat. |
| Headline wrap at 362 pt (Figma) | "Split any bill in / seconds." | "Know who owes what, / and by when." | "Settle up without the / awkward chat." |
| Body (verbatim) | Add it once. Paybak does the math for everyone. | Clear balances and due dates, all in one place. | Record payments and send gentle reminders. |
| Page dots | Active=1 | Active=2 | Active=3 |
| CTA label | Continue | Continue | Get started |
| CTA action | step → 2 | step → 3 | → Get Started (push) |
| Skip | visible, → Get Started | visible, → Get Started | **hidden** (Show Skip=false; no hotspot). The bar stays 44 pt tall. |
| Back (system back / edge swipe) | leaves Welcome (Android: back exits the app, since Splash isn't on the back stack) | step → 1 | step → 2 |

Swipes (from `flow.md`; not in the Figma prototype): swipe left (finger moves right to left) = next step; swipe right = previous step. On step 1 a right swipe does nothing. On step 3 a left swipe does nothing (only the CTA leaves the pager); this is my suggestion, see Issues. Trigger on gesture end: horizontal translation > 50 pt, or velocity > 300 pt/s, and more horizontal than vertical. The content doesn't have to follow the finger, because the Rive can't be scrubbed.

### 2.5 Suggested transitions between steps (nothing is designed)
The Figma prototype pushes whole frames (PUSH LEFT, 350 ms, ease-in-out). In the app, only the parts that change animate. The Rive, the top-bar height, the text-block position and the footer stay in place.

Rive timing, measured in headless Chrome with the web runtime: on `step` 1 → 2 the old art leaves within ~150 ms ("Welcome 1 · Off Left"), and the new art enters from ~160 ms and settles by ~700 ms ("Welcome 2 · Enter", 60 frames at 60 fps; the state machine reaches "Welcome 2 · Idle" at ~1.0 s). Going back (2 → 1) mirrors this ("Welcome 2 · Off Right" + "Welcome 1 · Enter"). The text transition below is timed to match.

- **Illustration**: set the view-model `step` number as soon as the step changes. Rive animates it; do nothing else.
- **Headline + body** (animate them as one unit, or give the body a 40 ms stagger):
  - Forward (n → n+1): the outgoing text fades 1 → 0 and moves x 0 → −24 pt over 150 ms, ease-in. Then the incoming text fades 0 → 1 and moves x +24 → 0 over 300 ms, ease-out (cubic-bezier 0.2, 0, 0, 1). Total ≈ 450 ms, in step with the Rive exit/enter.
  - Backward (n → n−1): the same, mirrored (out to +24, in from −24).
  - Keep the text block's frame fixed (leading, top at safe-y 424). If a headline had a different line count it would push the body down; all three are 2 lines at 362 pt, so nothing jumps.
- **Page dots**: animate the widths (8 ↔ 24) and the fills (`color/bg/indicator` #D1D1D1 ↔ `color/bg/inverse` #0A0A0A) together, over 300 ms ease-in-out (or a spring with response 0.35 and damping 0.85). The control stays 52 pt wide the whole time.
- **CTA label**: crossfade "Continue" ↔ "Get started" over 200 ms. The button stays 362 wide.
- **Skip**: fade out over 200 ms when entering step 3 (and stop it taking touches at once). Fade it back in over 200 ms when returning to step 2.
- While a transition runs, ignore extra CTA taps or swipes for ~300 ms, or let the new target replace the old one smoothly. Don't queue several steps.
- **Reduce Motion**: no slide, just a 200 ms crossfade of the text. The dots and label change with a 200 ms fade. Set the Rive `reduceMotion` = true (per `rive.md`).

### 2.6 Rive slot summary
- File `paybak-onboarding.riv` (iOS `Resources/Rive/paybak-onboarding.riv`; Android `res/raw/paybak_onboarding.riv`). Load artboard **`Onboarding`** by name, state machine **`Onboarding`**, and bind the default view-model instance (`Onboarding` / `Default`). Data binding is required.
- Slot: **(20, 114, 362, 340)**, safe-y 52, fit `contain`, alignment centre, transparent background. Keep ONE Rive view for all three steps and only change `step` (number: 1, 2, 3). The file has no listeners, so taps on the art do nothing and no haptics are needed.
- `reduceMotion` (bool) ← the OS Reduce Motion setting.
- When the screen starts at `welcome2`/`welcome3` (debug start screen), set `step` before or right after load. The Rive will play that step's Enter.

### 2.7 Assets used
- `paybak-onboarding.riv` (artboard `Onboarding`, state machine `Onboarding`).
- No icons (Skip is text). Fonts: `Manrope-ExtraBold` (Title/1), `Manrope-Regular` (Body), `Manrope-SemiBold` (Button/Large, Button/Small).
- The Figma illustration components (7:82, 7:117, 7:142) are **not** shipped. The Rive replaces them.

---

## 3. Get Started (`getStarted`, 22:272)

### 3.1 Purpose
Sign-in choice. Apple and Google go straight to Setup 1 ("What's your name?"; there's no backend). "Continue with email or phone" goes to Sign in. The Terms and Privacy Policy links do nothing yet.

### 3.2 Frame
- Fill `color/bg/primary` #FFFFFF.
- Vertical auto-layout, `main=SPACE_BETWEEN`, `cross=MIN`. Padding: top 62 (`layout/status-bar`), left/right 20 (`layout/screen-margin`), **bottom 34** (the legal line ends exactly on the bottom safe-area edge). Two children: **Content** (top-anchored) and **Actions** (bottom-anchored). The gap between them flexes (64 pt on this frame).
- In code: `VStack { Content; Spacer(minLength: 24); Actions }` inside the safe area, with 20 pt side margins and 0 bottom padding above the bottom safe-area edge.

### 3.3 Elements, top to bottom
| # | Element | Node | Frame rect (x, y, w, h) | safe-y / bottom-gap | Details |
|---|---|---|---|---|---|
| 1 | **Content** column | 22:274 | (20, 62, 362, 512) | safe-y 0 | Vertical stack, gap 0, **items centred horizontally**, **top padding 24** (raw value), fills the width. |
| 2 | Logo | `Brand / Logo`, Layout=Horizontal (instance 22:275) | (149, 86, 104, 28) | safe-y 24 | §0.2. Centred (centre x = 201). |
| 2a | Mark | `Brand / App Mark`, Size=28 (I22:275;8:24) | (149, 86, 28, 28) | | Radius 6.2636, smoothing 0.6. The glyph path bounds are (158.63, 92.56, 9.41, 14.88), stroke 3.0625 white. |
| 2b | Wordmark | "Paybak" (I22:275;8:26) | (185, 88, 68, 24) | | Brand/Wordmark S (Manrope ExtraBold 20/24, −0.6 pt), `color/text/primary`. 8 pt right of the mark. |
| 3 | Spacer | "gap" 22:280 | h 32 | | |
| 4 | **Illustration card** (Figma) | "Illustration" instance 22:281 of `Illustration / Get Started — People` (7:174) | **card (20, 146, 362, 260)** | safe-y 84 | Figma: fill `color/illustration/tint` #EBEBEB, corner radius **28** (raw; no token; smoothing 0), clips content. Three Open Peeps busts. **Replaced by the Rive; see 4R.** |
| 4R | **Rive slot** | `paybak-getstarted.riv` | **view (8, 134, 386, 284)** | safe-y 72 | Artboard `Get Started` (386 × 284), state machine `Get Started`, view model `GetStarted` / `Default`. Fit `contain`, alignment centre. **The artboard draws its own grey card**, inset 12 pt on every side, so centre the 386 × 284 view on the 362 × 260 card slot (it overhangs the slot by 12 pt all round, 8 pt from the screen edges). **Don't draw a native card behind it.** Details in §3.5. |
| 5 | Spacer | "gap" 22:323 | h 32 | | 32 pt below the card slot (not below the Rive view's overhang). |
| 6 | **Text** block | 22:324 | (20, 438, 362, 136) | safe-y 376 | Vertical stack, gap 12 (`space/12`), centred, fills the width. |
| 6a | Headline | 22:325 | (20, 438, 362, 76) | safe-y 376 | "Shared money,⏎kept clear." (**hard line break** after the comma). Title/1 (Manrope ExtraBold 32/38, −0.64 pt), `color/text/primary` #0A0A0A, **centre-aligned**. |
| 6b | Body | 22:326 | (20, 526, 362, 48) | safe-y 464 | "Paybak keeps the record.⏎You pay however you like." (**hard line break** after the full stop). Body (Manrope Regular 16/24), `color/text/secondary` #6B6B6B, **centre-aligned**. |
| 7 | **Actions** column | 22:327 | (20, 638, 362, 202) | bottom-gap 0 | Vertical stack, gap 12 (`space/12`), **items centred horizontally**, fills the width. |
| 7a | Continue with Apple | `Button / Primary`, State=Default, Size=Large, **Leading icon=true**, Icon=`Icon / Apple` (22:328) | (20, 638, 362, 52) | | §0.3. Icon 20 × 20 at (105, 654), fill **`color/icon/inverse` #FFFFFF** (overridden). Label "Continue with Apple", 164 × 22 at (133, 653), `color/text/inverse`. Icon and label are centred as a group, 8 pt apart. Pressed: fill #2B2B2B. → Setup 1 (push). |
| 7b | Continue with Google | `Button / Secondary`, State=Default, Size=Large, **Leading icon=true**, Icon=`Icon / Google` (22:333) | (20, 702, 362, 52) | | §0.4. Fill `color/bg/card` #F5F5F5. Icon 20 × 20 at (99.5, 718), **official 4-colour G, not tinted**. Label "Continue with Google", 175 × 22 at (127.5, 717), `color/text/primary` #0A0A0A. Pressed: fill `color/bg/card-pressed` #EBEBEB. → Setup 1 (push). |
| 7c | Continue with email or phone | `Button / Text`, Style=**Primary**, State=Default (22:342) | (96.5, 766, 209, 44); label (96.5, 778, 209, 20) | | §0.5. Button/Small (Manrope SemiBold 15/20, −0.0375 pt), `color/text/primary` #0A0A0A, centred in the column. Pressed = 50 % opacity. → Sign in (push). |
| 7d | Legal footnote | text "Legal" 22:347 | (20, 822, 362, 18) | bottom-gap 0 | Footnote (Manrope **Medium** 13/18, letter spacing 0), **centre-aligned**, fills the width, one line. Mixed runs; see below. |

Vertical rhythm of the Actions column: Apple 638…690 · 12 · Google 702…754 · 12 · text button 766…810 · 12 · legal 822…840.

**Legal footnote runs (exact, from `getStyledTextSegments`).** Every run uses the Footnote style (Manrope Medium 500, 13 pt, line height 18 px, letter spacing 0 %):

| # | Characters (verbatim, spaces included) | Fill | Decoration |
|---|---|---|---|
| 1 | `By continuing, you agree to our ` | `color/text/tertiary` #A3A3A3 | none |
| 2 | `Terms` | `color/text/secondary` #6B6B6B | **underline**, solid, thickness AUTO, offset AUTO, underline colour AUTO (= the text colour #6B6B6B) |
| 3 | ` and ` | `color/text/tertiary` #A3A3A3 | none |
| 4 | `Privacy Policy` | `color/text/secondary` #6B6B6B | **underline**, solid, AUTO (as run 2) |
| 5 | `.` | `color/text/tertiary` #A3A3A3 | none |

**"Terms" and "Privacy Policy" are NOT bold.** They are the same Medium 500 weight; only the colour (#6B6B6B instead of #A3A3A3) and the underline change. Full string: `By continuing, you agree to our Terms and Privacy Policy.` iOS: an `AttributedString` with `.underlineStyle = .single` and `.foregroundColor` per run. Compose: `buildAnnotatedString` with `SpanStyle(color = …, textDecoration = TextDecoration.Underline)`. Make the two link runs tappable (`LinkAnnotation` / `.link`) with **no action for now** (no URLs yet), so URLs can be added later. Don't add a pressed highlight that suggests something happens.

Accessibility: the Rive view is decorative, except that its tap listeners are playful. Hide it from VoiceOver/TalkBack. The button labels are their visible text. The headline is a heading.

### 3.4 Interactive states on this screen
- Apple button: Default #0A0A0A / Pressed `color/bg/inverse-pressed` #2B2B2B. The label and icon stay white.
- Google button: Default #F5F5F5 / Pressed `color/bg/card-pressed` #EBEBEB. The label stays #0A0A0A and the G keeps its colours.
- Email-or-phone text button: Pressed = 50 % opacity.
- No disabled states on this screen. No loading state is designed. Since there's no backend, navigate at once on tap.
- Implement the Apple button as the custom Figma pill (Manrope label, Apple logo), not `ASAuthorizationAppleIDButton`. No real Apple or Google auth for now (see `flow.md`). Record the sign-in method as `apple`/`google` in the profile.

### 3.5 Rive slot: exact rect and card match
- File `paybak-getstarted.riv` (iOS `Resources/Rive/paybak-getstarted.riv`; Android `res/raw/paybak_getstarted.riv`). Artboard **`Get Started`**, state machine **`Get Started`**, view model `GetStarted` / instance `Default` (auto-bind). Data binding is required.
- **Figma card vs .riv card (measured by rendering the artboard at 1:1 in headless Chrome with @rive-app/canvas 2.37 and reading the pixels):**
  - The artboard is 386 × 284 and transparent outside the card. Corner pixels have alpha 0.
  - The .riv card spans x 12…373 and y 12…271 in the artboard: **362 × 260 at (12, 12)**, a 12 pt inset on every side.
  - The card fill is rgb(235, 235, 235) = **#EBEBEB = `color/illustration/tint`**.
  - The corner radius is **≈ 28**. I measured 28.3–28.7 from the missing-alpha area in each corner, which includes anti-aliasing. All four corners are the same.
  - **→ The .riv card matches the Figma card exactly in size, colour and radius.** Nothing is drawn outside the card: 0 non-transparent pixels outside it at 0.05 s, 0.6 s and 4 s, and after tapping the middle person.
  - The measurement page and a 1:1 render (`measure.html`, `getstarted-riv-4s.png`) were build-session scratch and weren't kept.
- **Placement:** Rive view **(8, 134, 386, 284)** in frame coordinates (safe-y 72), which is the Figma card rect (20, 146, 362, 260) grown by 12 pt on each side. In a stack, reserve a 362 × 260 slot in the Content column and draw the 386 × 284 Rive view centred on it without affecting layout. SwiftUI: `Color.clear.frame(width: 362, height: 260).overlay { RiveView().frame(width: 386, height: 284) }`. Compose: `Box(Modifier.size(362.dp, 260.dp), contentAlignment = Center) { RiveView(Modifier.requiredSize(386.dp, 284.dp)) }`. Fit `contain`, alignment centre.
- Don't clip the Rive view to the 362 × 260 card (clipping to its own 386 × 284 bounds is fine). Don't draw any native background behind it.
- Behaviour (from `rive.md`): the "Intro" animation (84 frames at 60 fps = 1.4 s) plays when it loads, then it idles (loops "Groove"/"Phone Moment"). **The file has its own tap listeners**: tapping a person fires `tapLeft`/`tapMiddle`/`tapRight` (+ `personTapped`) and plays a jump. Pass touches through to the Rive view and don't fire triggers yourself. Optionally play a light haptic on `personTapped`, if the runtime lets you observe it. `reduceMotion` ← the OS setting.
- If the screen is shorter than 874 pt and the column must shrink, scale the slot and the Rive view together (keep 362:260 for the card and 386:284 for the view, with the same 12:362 inset ratio).

### 3.6 Assets used
- `paybak-getstarted.riv` (artboard `Get Started`, state machine `Get Started`).
- `assets/icons/apple.svg` (tint white on the Apple button), `assets/icons/google.svg` (never tint).
- Brand / App Mark 28: draw natively (§0.1), or `assets/images/brand-app-mark-28.svg`.
- Fonts: `Manrope-ExtraBold` (wordmark, Title/1), `Manrope-Regular` (Body), `Manrope-SemiBold` (Button/Large, Button/Small), `Manrope-Medium` (Footnote).
- The Figma illustration `Illustration / Get Started — People` (7:174) is **not** shipped. The Rive replaces it, grey card included.

---

## 4. Navigation summary for this section
| From | Action | To | Transition |
|---|---|---|---|
| Splash | 1.5 s timer | Welcome, step 1 (or Home when `onboardingComplete`) | 0.4 s crossfade, ease-out |
| Welcome step 1/2 | Continue, or swipe left | same screen, step + 1 | in-place transition (§2.5) |
| Welcome step 2/3 | system back / swipe right | same screen, step − 1 | in-place transition (§2.5), reversed |
| Welcome step 1/2 | Skip (47 × 44 hit area) | Get Started | 0.3 s crossfade |
| Welcome step 3 | Get started | Get Started | push (slide from the right), 0.35 s ease-in-out |
| Get Started | Continue with Apple / Continue with Google | Setup 1 (42:665) | push, 0.35 s |
| Get Started | Continue with email or phone | Sign in (39:356) | push, 0.35 s |
| Get Started | Terms / Privacy Policy | nothing | n/a |

Splash is never on the back stack. Where "back" from Get Started goes is not in Figma (see Issues). The suggestion: back to the Welcome screen at the step the user left from.

## 5. Small-screen behaviour (not designed; suggestion)
On the 874 pt frame, Welcome has 142 pt of flexible space between Content and Footer, and Get Started has 64 pt. On shorter screens, keep the minimum gap at 24 pt and shrink the illustration slot proportionally (Welcome: 362:340; Get Started: card 362:260, Rive view 386:284) before anything else moves. With larger Dynamic Type or font scale, the headlines may wrap to 3 lines. Let the text block grow and take the space from the flexible gap.
