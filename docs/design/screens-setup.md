# Setup screens: `setup1`, `setup2`, `setup3`, `setup4`, `allSet`

Source: Figma file `2SPNUpHlG8bCO62YfwRuRi`, page "03 Launch & Onboarding" (3:4), section "Sign in & Setup" (39:353).
All numbers were read with the Plugin API (read-only). Nothing in Figma was changed.

| Screen id | Figma frame | 2x reference (804×1748) | Node JSON (`.figma-cache/`, regenerate with `tools/fetch_figma.py`) |
|---|---|---|---|
| `setup1` | Setup 1 — Name & photo (42:665) | `ref/setup1.png` | `nodes/3-4.json` |
| `setup2` | Setup 2 — Currency (44:938) | `ref/setup2.png` | `nodes/3-4.json` |
| `setup3` | Setup 3 — Payment (46:1021) | `ref/setup3.png` | `nodes/3-4.json` |
| `setup4` | Setup 4 — Notifications (46:1148) | `ref/setup4.png` | `nodes/3-4.json` |
| `allSet` | All set (46:1223) | `ref/allSet.png` | `nodes/3-4.json` |

The reference PNGs include the iOS status bar, keyboard (setup1 only) and home indicator. Those are system UI.

Designer notes placed under the frames on the canvas (verbatim):
- Section: "Email or phone → code → 4 setup steps → All set. Apple and Google sign-in skip the code and start at Step 1."
- 42:1064 (setup1): "Step 1 of 4. Pick a line-art avatar or upload a photo; initials are the fallback."
- 44:1061 (setup2): "Step 2 of 4. INR is preselected from your region; search covers every currency."
- 46:1147 (setup3): "Step 3 of 4 · optional. Your UPI ID shows when friends settle up with you."
- 46:1222 (setup4): "Step 4 of 4 · optional. Explains reminders before the iOS permission prompt."
- 46:1295 (allSet): "Setup complete → Home (first day)."

## Conventions used in this file
- Coordinates are pt (iOS) = dp (Android) in the 402 × 874 frame, written `(x, y, w, h)`. **safe-y** = y − 62, measured from the top safe-area edge. The bottom safe-area edge is frame y **840** (874 − 34). "bottom gap" = distance from an element's bottom to y 840.
- Colours are token names from `tokens.md` with hex. Text styles are names from `tokens.md`. Letter spacing in pt: Title/1 −0.64, Title/3 −0.2, Headline −0.04, Button/Large −0.085, Button/Small −0.0375, Caption/1 +0.12; Body, Subheadline and Footnote 0.
- Status bar (kit), Keyboard (kit) and Home indicator (kit) are iOS system UI. **Do not draw them.**
- `Hotspot — Back` (44×44 at (20, 62)) and `Hotspot — Skip` (44×44 at (338, 62)) frames are invisible prototype hotspots. **Do not render them.** They tell you the intended tap targets.
- Every frame is a vertical auto-layout: padding top 62 (`layout/status-bar`), bottom 34 (`layout/home-indicator`), left/right 20 (`layout/screen-margin`), background `color/bg/primary` #FFFFFF. setup2/3/4 and allSet use **space-between**: a top-anchored `Content` column and a bottom-anchored `Footer`. In code: lay out from the safe area with 20 pt side margins; don't hard-code 62/34.
- Gaps between blocks are fixed spacer frames bound to spacing tokens (named `gap` in Figma). They're listed as "gap" rows below.
- **Motion:** `get_motion_context` returned no animated nodes for all five frames. The only motion in Figma is the prototype transitions (§0.12). Illustrations on setup4 and allSet are Rive files (§0.11).
- "Proposal" marks behaviour that is not designed in Figma. Everything else is read from Figma or `flow.md`.

---

## 0. Shared building blocks (exact component data from page 02 Components)

### 0.1 `Navigation / Setup Header` (component set 36:666)
Description: "First-run setup header: back, optional Skip, 4-segment progress and step count. SwiftUI: PBSetupHeader".
Properties: `Step` = 1 | 2 | 3 | 4 (variant), `Show Skip` (boolean, default false).
Usage: setup1 Step=1 Skip off, setup2 Step=2 Skip off, setup3 Step=3 **Skip on**, setup4 Step=4 **Skip on**. allSet has no header.

Geometry (identical on all four screens, frame coordinates):

| Part | Frame (x, y, w, h) | safe-y | Spec |
|---|---|---|---|
| Header block | (20, 62, 362, 82) | 0 | Vertical stack, gap 8 (`space/8`), no padding, fills the content width, hugs height |
| `top` row | (20, 62, 362, 44) | 0 | Horizontal, no padding, children vertically centred: `back` + `spacer` (fills) + `skip` (when shown) |
| `back` | (20, 62, 44, 44) | 0 | `Button / Icon`, Style=Plain (§0.4). Icon `Icon / Chevron Left` 24×24 at (30, 72), `color/icon/primary` #0A0A0A, stroke 1.5, round caps/joins (vector bounds 6×12 at (39, 78)). Accessibility label "Back" |
| `skip` (setup3, setup4) | (351, 62, 31, 44) | 0 | `Button / Text`, Style=Secondary (§0.3), label "Skip". Label box (351, 74, 31, 20). Right edge = content edge x 382. **Tap target:** at least 44×44, i.e. extend it leftwards to x 338 (the Figma hotspot is (338, 62, 44, 44)) |
| `progress` | (20, 114, 362, 4) | 52 | Horizontal, 4 equal segments (each `fill` width), gap 4 (`space/4`), height 4 |
| segment 1…4 | x 20 / 111.5 / 203 / 294.5, y 114, w 87.5, h 4 | 52 | Rectangles, corner radius `radius/full` (a 4 pt tall capsule, i.e. 2 pt radius). **Filled** = `color/bg/inverse` #0A0A0A. **Empty** = `color/bg/indicator` #D1D1D1 |
| `step` caption | (20, 126, hug, 18) | 64 | "Step N of 4". **Footnote** (Manrope Medium 13/18), `color/text/secondary` #6B6B6B, left-aligned, 1 line. Widths in Figma: 62 / 65 / 64 / 65 |

Segments filled per step: Step 1 → segment 1 black, 2–4 grey. Step 2 → 1–2 black. Step 3 → 1–3 black. Step 4 → all four black. Segment width on other screen widths: (contentWidth − 12) / 4.

Header states:
- Back: Default = transparent 44 circle. **Pressed** = the 44×44 circle filled `color/bg/selected` (#0A0A0A at 6 %); the icon doesn't change.
- Skip: Default label `color/text/secondary`. **Pressed** = whole button at 50 % opacity. (Disabled exists in the component but isn't used.)

**Progress animation between steps (proposal; Figma has instant variant swaps):**
- Best: keep ONE header for the whole setup flow (a setup container with a pinned header above a content area that pages between steps). Then only the content slides (§0.12) and the header animates in place.
- Draw each segment as a grey track (`color/bg/indicator`) with a black fill capsule (`color/bg/inverse`) on top, anchored to the segment's leading edge. Going forward N → N+1: the fill of segment N+1 grows from width 0 to full width, **0.35 s, ease-in-out** (cubic-bezier 0.42, 0, 0.58, 1, the same curve and duration as the Figma push). Going back N+1 → N: that fill shrinks back to 0 towards the leading edge, same timing.
- "Step N of 4" caption: change the number with a numeric text transition (iOS `.contentTransition(.numericText())` inside `withAnimation`; Android `AnimatedContent` with a short vertical slide + fade, 0.2 s). Or a 0.2 s cross-fade.
- Skip appearing (step 2 → 3) / disappearing (3 → 2): fade 0.2 s. Its space is on the right after a flexible spacer, so nothing else moves.
- If each step is instead its own pushed screen: render the new screen's header with the previous step's fill and start the fill animation ~0.1 s after the push begins, so it runs while the screen slides in.
- Reduce Motion: no growth animation; switch instantly (or a 0.15 s cross-fade).

### 0.2 `Button / Primary`, Size=Large (9:36)
Description: "Main action — black pill. One Large primary per screen. SwiftUI: PBButton(.primary)".
Height 52 (`size/button-lg`), radius `radius/full` (pill), horizontal padding 24 (`space/24`), gap 8 (the leading icon is hidden on all these screens). Label **Button/Large** (Manrope SemiBold 17/22, −0.085 pt), centred, 1 line. Width stretched to the content width (362).

| State | Fill | Label |
|---|---|---|
| Default | `color/bg/inverse` #0A0A0A | `color/text/inverse` #FFFFFF |
| Pressed | `color/bg/inverse-pressed` #2B2B2B | `color/text/inverse` #FFFFFF |
| Disabled | `color/bg/disabled` #E0E0E0 | `color/text/disabled` #A3A3A3 |

Disabled buttons ignore taps. The pressed swap is instant in Figma; a 0.1 s fade is fine.

### 0.3 `Button / Text`, Style=Secondary (10:21), used for "Skip" and "Not now"
Description: "Text-only action (See all, Skip, Continue with email or phone). Pressed = 50% opacity. SwiftUI: PBTextButton".
Height 44 (`size/tap`), horizontal, gap 2 (`space/2`), no padding, content vertically centred, width hugs the label. Label **Button/Small** (Manrope SemiBold 15/20, −0.0375 pt), `color/text/secondary` #6B6B6B, 12 pt below the button top. The trailing chevron is hidden.
States: Default as above. **Pressed** = the whole button at 50 % opacity. Disabled = label `color/text/disabled` #A3A3A3 (not used here).

### 0.4 `Button / Icon`, Style=Plain (10:47 Default, 10:51 Pressed), used for back and copy
Description: "Circular 44pt icon button. … SwiftUI: PBIconButton".
44×44 (`size/tap`), radius `radius/full`, no fill, a 24×24 icon centred (10 pt inset). Icon colour `color/icon/primary` #0A0A0A. The badge dot is hidden.
**Pressed** = the circle filled `color/bg/selected` (#0A0A0A at 6 %). Icon unchanged.

### 0.5 `Control / Input Field` (12:296), used for Name, Search and UPI ID
Description: "52pt field on #F5F5F5, 14pt radius. Focused = 1.5pt black ring. Error = red ring + red helper. SwiftUI: PBTextField". (Same component as `screens-signin.md` §0.3.)
Structure: vertical stack, gap 8 (`space/8`), width 362:
1. `label` (optional): **Subheadline** (Manrope Medium 14/20), height 20.
2. `field`: height 52 (`size/button-lg`), fill `color/bg/card` #F5F5F5, radius `radius/input` 14, horizontal padding 16 (`space/16`), gap 12 (`space/12`) between the optional leading 20×20 icon and the value, content vertically centred. `value` = **Body** (Manrope Regular 16/24), fills the width, 1 line.
3. `helper` (optional): **Footnote** (Manrope Medium 13/18), height 18.

| State | Ring (inside stroke) | Value text | Placeholder | Label | Helper |
|---|---|---|---|---|---|
| Default (empty, unfocused) | none | — | `color/text/tertiary` #A3A3A3 | `color/text/secondary` #6B6B6B | `color/text/tertiary` #A3A3A3 |
| Focused | 1.5 pt `color/border/strong` #0A0A0A | `color/text/primary` #0A0A0A | `color/text/tertiary` (when empty) | `color/text/secondary` | `color/text/tertiary` |
| Filled (unfocused, has text) | none | `color/text/primary` | — | `color/text/secondary` | `color/text/tertiary` |
| Error | 1.5 pt `color/border/destructive` #C93636 | `color/text/primary` | `color/text/tertiary` | `color/text/secondary` | `color/text/destructive` #C93636 |
| Disabled | none | `color/text/disabled` #A3A3A3 | `color/text/disabled` | `color/text/disabled` | `color/text/disabled` |

- Figma counts the 1.5 pt stroke in the layout for Focused/Error, so the value starts at 17.5 pt instead of 16. Keep it at 16 pt in every state and draw the ring as an overlay (`strokeBorder` / `border` drawn inside) so the text doesn't jump.
- Caret colour (proposal, matches sign-in): `color/text/primary` #0A0A0A.
- Leading icon, when shown: 20×20, drawn with the 24-grid SVG scaled to 20 (stroke becomes 1.25). The Search field overrides its colour to `color/icon/secondary` #6B6B6B.

### 0.6 `Control / Avatar Option` (component set 37:655)
Description: "Avatar picker option for setup. Selected = black ring. Swap the head with the Art property (any Art / Peep Head). SwiftUI: PBAvatarOption".
Variants: `Type=Art, Selected=True` (37:613), `Type=Art, Selected=False` (37:631), `Type=Upload, Selected=False` (37:649). Property `Art` (instance swap) picks the head.

All variants: a 56×56 circle container (`size/avatar-lg`), radius `radius/full`, content centred, no fill.
- **Art, unselected:** inner circle 46×46 at (5, 5), fill `color/bg/card` #F5F5F5, clips its content to the circle. Inside: the head art (§0.7) scaled to 46×46.
- **Art, selected:** same inner circle, plus a **2.5 pt stroke drawn inside the 56 circle**, `color/border/strong` #0A0A0A. So from outside in: 2.5 pt black ring (radius 28 → 25.5), a 2.5 pt white gap (the screen background shows through), then the 46 pt avatar. The avatar doesn't move or resize between states.
- **Upload (camera tile):** inner circle 46×46 at (5, 5), fill `color/bg/card` #F5F5F5, with `Icon / Camera` (HugeIcons camera-01) 24×24 centred at (16, 16), `color/icon/primary` #0A0A0A, stroke 1.5. No ring. There is no selected Upload variant in Figma (see setup1 for the proposal).
- Pressed: not designed. Proposal: no pressed styling; the ring appears on tap-up with a 0.15 s ease-out fade (optionally the ring scales 0.92 → 1). Reduce Motion: instant.

### 0.7 Avatars: art, photo, initials
**Art (the 5 presets).** Each preset is an `Art / Peep Head / …` component (120×120, clips content, transparent background, Open Peeps busts). Exported to `assets/avatars/`:

| Preset index (flow.md "avatar N") | Option in setup1 (left → right) | Figma component | File |
|---|---|---|---|
| 0 | 1st, "Avatar option — Arjun" | Art / Peep Head / Arjun (7:5), peep-43 | `assets/avatars/avatar-1.svg` |
| 1 | 2nd, "Avatar option — Priya" | Art / Peep Head / Priya (7:22), peep-93 | `assets/avatars/avatar-2.svg` |
| 2 | 3rd, "Avatar option — Rohan" | Art / Peep Head / Rohan (7:37), peep-21 | `assets/avatars/avatar-3.svg` |
| 3 | 4th, "Avatar option — Esha" | Art / Peep Head / Esha (7:52), peep-96 | `assets/avatars/avatar-4.svg` |
| 4 | 5th, "Avatar option — Dev" | Art / Peep Head / Dev (7:67), peep-73 | `assets/avatars/avatar-5.svg` |

- Each SVG is 120×120 (viewBox 0 0 120 120), transparent background, already cropped to its square. **Draw it scaled to the full circle size (46, 40, …) and clip to the circle.** The circle's background colour is drawn natively (see below). The body fills inside the art are white, the ink is #0A0A0A.
- The art is identical to `assets/images/peep-head-priya.svg` (= avatar-2) and `peep-head-rohan.svg` (= avatar-3) exported by the Home spec. One shared asset set is fine (e.g. SwiftUI `PBPeepHead.arjun…dev`, Android `R.drawable.avatar_1…5`).
- Circle background: `color/bg/card` #F5F5F5 on white screens (setup1 options, `Avatar / Circle` default). **`color/bg/primary` #FFFFFF when the avatar sits on a #F5F5F5 card** (the setup3 preview card overrides it to white).

**Photo (camera option).** A user photo fills the circle: aspect-fill, centred, clipped to the circle. No background needed.

**Initials (fallback).** `Avatar / Circle`, Type=Initials: circle `color/bg/card` #F5F5F5 (white on a card), text `color/text/primary` #0A0A0A, centred. Style by size: 32 → **Caption/1** (Bold 12/16, +0.12 pt); 40 → **Headline** (SemiBold 16/22, −0.04 pt); 56 → **Title/3** (Bold 20/26, −0.2 pt). For 46 (setup1 option size) use Headline. Initials = first letter of the first word + first letter of the last word, uppercased ("Arjun Mehta" → "AM"); one word → its first letter.

### 0.8 `Row / Section Header` (13:223)
Description: "Section title (Title/3) with optional “See all” text button. SwiftUI: PBSectionHeader".
Height 32, horizontal, space-between, vertically centred. Title **Title/3** (Manrope Bold 20/26, −0.2 pt), `color/text/primary` #0A0A0A, 3 pt below the row top. `Show action` = false on setup2 (no "See all").

### 0.9 `Row / Currency` (component set 37:673)
Description: "Currency option with radio. SwiftUI: PBCurrencyRow". Properties: `Symbol`, `Title`, `Subtitle` (text), `Selected` = True | False.
Row: height 56, full content width 362, horizontal, gap 12 (`space/12`), children vertically centred, no padding, no background, no divider. Row-relative geometry:

| Part | (x, y, w, h) in the row | Spec |
|---|---|---|
| symbol tile | (0, 8, 40, 40) | Circle, `color/bg/card` #F5F5F5, radius `radius/full`, symbol text centred |
| symbol text | centred | **Headline** (SemiBold 16/22, −0.04 pt), `color/text/primary` #0A0A0A, e.g. "₹", "$", "€", "£", "S$". **Exception:** when the symbol is a letter code ("AED") the text is **Caption/1** (Bold 12/16, +0.12 pt), `color/text/primary` |
| text column | (52, 6, 276, 44) | Vertical, gap 2 (`space/2`), fills the remaining width |
| title | (52, 6, 276, 22) | **Headline**, `color/text/primary` #0A0A0A, 1 line, truncate tail |
| subtitle | (52, 30, 276, 20) | **Subheadline** (Medium 14/20), `color/text/secondary` #6B6B6B, 1 line, truncate tail |
| radio | (340, 17, 22, 22) | See states |

| State | Radio |
|---|---|
| Selected=True | 22×22 circle filled `color/bg/inverse` #0A0A0A; `Icon / Check` (HugeIcons tick-02) 14×14 centred (at +4, +4) in `color/icon/inverse` #FFFFFF (the 24-grid icon scaled to 14, stroke becomes 0.875) |
| Selected=False | 22×22 circle, no fill, **1.5 pt inside stroke `color/bg/indicator` #D1D1D1** (yes, the stroke uses the bg/indicator token) |

- Whole row is the tap target (362×56). Exactly one row is selected across all sections.
- Pressed and selection animation: not designed. Proposal: no pressed highlight; the radio cross-fades between states in 0.15 s ease-out (check may scale 0.6 → 1). Reduce Motion: instant.
- Accessibility: one element per row, label "{Title}, {code}", selected trait/`selected` semantics when selected.

### 0.10 `Card / Payment Preview` (37:675)
Description: "Shows how your UPI ID appears to friends. Show copy: Button / On Card Small “Copy” with the Copy icon (exposed), for 06-11 and 08-04. Show copy icon: the original plain copy icon button (default on; turn it off when Show copy is on). SwiftUI: PBPaymentPreview".
Setup3 uses `Show copy = false`, `Show copy icon = true` (the plain icon button). The "Copy" pill variant is for other screens; don't use it here.
Card: width 362, fill `color/bg/card` #F5F5F5, radius `radius/card` 20, padding 16 all sides (`layout/card-padding`), vertical, gap 12 (`space/12`), height hugs (106).

| Part | Card-relative (x, y, w, h) | Spec |
|---|---|---|
| caption | (16, 16, hug 104, 18) | "What friends see". **Footnote** (Medium 13/18), `color/text/tertiary` #A3A3A3 |
| row | (16, 46, 330, 44) | Horizontal, gap 12, vertically centred |
| avatar | (16, 48, 40, 40) | `Avatar / Circle` Size=40 Type=Art, **fill `color/bg/primary` #FFFFFF** (on-card override), art = the user's chosen avatar (§0.7) scaled to 40 and clipped to the circle |
| text column | (68, 46, 222, 44) | Vertical, gap 2, fills width |
| name | (68, 46, 222, 22) | The user's full name. **Headline**, `color/text/primary` #0A0A0A, 1 line, truncate tail |
| upi | (68, 70, 222, 20) | The UPI ID. **Subheadline**, `color/text/secondary` #6B6B6B, 1 line, truncate middle or tail |
| copy | (302, 46, 44, 44) | `Button / Icon` Plain (§0.4) with `Icon / Copy` (HugeIcons copy-01) 24×24, `color/icon/primary`. Pressed = `color/bg/selected` circle. Accessibility label "Copy UPI ID" |

### 0.11 `Overlay / Toast` (118:965), for the copy confirmation
Description (verbatim): "PBToast — Short confirmation after a save (“Expense added”, “Payment recorded”, “Loan added”, “Group created”, “Payment confirmed”, “UPI ID copied”). A bg/inverse capsule, 44 tall, hugging its Label (Button/Small, text/inverse) with an optional 20 icon (Show icon; Icon swap, default Check Circle, icon/inverse). No shadow. Place it absolute and centred: 50 above the bottom edge, or 16 above the tab bar on tab screens. It fades after 2 s and is never a link. …"
- Capsule height 44, radius `radius/full`, fill `color/bg/inverse` #0A0A0A, padding left 16 / right 20, gap 8, content vertically centred. Icon `Icon / Check Circle` 20×20, `color/icon/inverse` #FFFFFF. Label **Button/Small**, `color/text/inverse` #FFFFFF. Width hugs; centred horizontally.
- setup3 uses it with the label **"UPI ID copied"** (text from the description). Position: see setup3.

### 0.12 Navigation and transitions (prototype reactions, read from `node.reactions`)
All forward/back links in the setup flow are **Navigate, Push, 0.35 s, EASE_IN_AND_OUT** (= cubic-bezier(0.42, 0, 0.58, 1); Compose `CubicBezierEasing(0.42f, 0f, 0.58f, 1f)`). Forward = direction LEFT (new screen comes in from the right). Back = direction RIGHT.

| From | Trigger | Figma destination | App behaviour |
|---|---|---|---|
| setup1 | Continue (42:1043) | Setup 2 (44:938), push LEFT | Save name + avatar, go to setup2 |
| setup1 | Back (hotspot 49:2909) | Get Started (22:272), push RIGHT | Pop to the previous screen: **Verify or Get Started**, whichever came before (flow.md) |
| setup2 | Continue (44:1040) | Setup 3 (46:1021), push LEFT | Save currency, go to setup3 |
| setup2 | Back (49:2912) | Setup 1 (42:665), push RIGHT | Back to setup1 |
| setup3 | Continue (46:1126) | Setup 4 (46:1148), push LEFT | Save UPI (may be empty), go to setup4 |
| setup3 | Skip (hotspot 49:2917) | Setup 4 (46:1148), push LEFT | Go to setup4 without saving the typed UPI |
| setup3 | Back (49:2915) | Setup 2 (44:938), push RIGHT | Back to setup2 |
| setup4 | Turn on notifications (46:1197) | All set (46:1223), push LEFT | Request OS permission, then allSet whatever the answer |
| setup4 | Not now (46:1201) | All set (46:1223), push LEFT | allSet, no prompt |
| setup4 | Skip (49:2923) | All set (46:1223), push LEFT | allSet, no prompt |
| setup4 | Back (49:2921) | Setup 3 (46:1021), push RIGHT | Back to setup3 |
| allSet | Go to Home (46:1275) | URL link to page 04 Home, node 24:326 "Home — First day" (no transition data) | Mark onboarding complete, replace the whole stack with `homeFirstDay` (proposal: 0.4 s cross-dissolve, like Splash) |

- iOS: `NavigationStack` pushes match (system push ≈ 0.35 s). Android: `slideInHorizontally { it }` + `slideOutHorizontally { -it / 3 }` (and the reverse for back), 350 ms, the easing above. Android system back = the in-app back chevron on setup1–4 (`BackHandler`).
- allSet has no back button. Proposal: disable back there (iOS: hide the back button and disable the swipe-back gesture; Android: consume system back and do nothing).

### 0.13 Rive slots on these screens (see `rive.md`)

| Screen | Figma illustration slot (layout box) | Rive view frame to draw | File / artboard / state machine / view model |
|---|---|---|---|
| setup4 | "Illustration" = `Illustration / Reminders` (35:588) at **(20, 168, 362, 300)**, safe-y 106 | **(8, 156, 386, 324)**, safe-y 94. The artboard is the Figma slot plus **12 pt of bleed on every side** (verified: the ground line renders at artboard (33…353, 308), which is Figma (21…341, 296) + 12) | `paybak-notifications.riv` (Android `R.raw.paybak_notifications`), artboard **`Notifications`** (386×324), state machine **`Notifications`**, view model `Notifications`, instance `Default`: `bellTapped` trigger, `reduceMotion` bool |
| allSet | "Illustration" = `Illustration / All set` (35:607) at **(20, 106, 362, 300)**, safe-y 44 | **(20, 106, 362, 300)**, the same rect (artboard size = slot size) | `paybak-allset.riv` (Android `R.raw.paybak_allset`), artboard **`All Set`** (362×300), state machine **`All Set`**, view model `AllSet`, instance `Instance`: `personTapped`, `tapBadge`, `tapLeft`, `tapMiddle`, `tapRight` triggers, `reduceMotion` bool |

Rules (from `rive.md`, repeated here so you don't miss them):
- Load the main artboard **by name** (each file also has Rive-logo artboards). Bind the default view-model instance to the state machine (auto-bind); without binding nothing reacts.
- Fit `contain`, alignment centre, view size = the "Rive view frame" above. Transparent background (the screen is white).
- **setup4 layout:** the layout box stays 362×300 (so the gaps above and below are exactly 24 and 32), but the Rive view is 386×324, centred on that box, overflowing it by 12 on each side. It reaches x 8…394 (8 pt from the screen edges). Parents must not clip it. SwiftUI: `RiveView.frame(width: 386, height: 324).frame(width: 362, height: 300)` (no `.clipped()`). Compose: `Box(Modifier.size(362.dp, 300.dp), contentAlignment = Alignment.Center) { Rive(Modifier.requiredSize(386.dp, 324.dp)) }`. Do not scale the artboard into 362×300; that would shrink the art by 7 %.
- The files have their own tap listeners (bell card on setup4; the three people and the badge on allSet). **Just pass touches through.** Don't fire triggers yourself. Optional light haptic when `bellTapped` / `personTapped` fires, if the runtime lets you observe it.
- `reduceMotion` ← OS Reduce Motion (iOS `accessibilityReduceMotion`; Android `ANIMATOR_DURATION_SCALE == 0`). False by default.
- The Rive views are decorative: hide them from accessibility (no label).
- Static fallbacks (use ONLY if the Rive file fails to load; not needed otherwise): `assets/images/illustration-reminders.svg` (362×300, draw in the 362×300 layout box, not the 386×324 rect) and `assets/images/illustration-all-set.svg` (362×300).

### 0.14 Keyboard rules for setup (proposal where not designed)
- **setup1:** the Name field is focused and the keyboard is up in Figma. Auto-focus the field on appear. The Continue button **rides the keyboard**: its bottom edge is **12 pt above the keyboard's top edge** (Figma: CTA bottom y 555, keyboard top y 567). With the keyboard hidden, the CTA sits on the bottom safe-area edge (y 788–840), like the other steps. iOS: put the CTA in `.safeAreaInset(edge: .bottom)` with `.padding(.bottom, keyboardVisible ? 12 : 0)`. Android: CTA container with `imePadding()` (or `windowInsetsPadding(WindowInsets.ime.union(WindowInsets.navigationBars))`) plus 12 dp while the IME is visible. Content above stays top-anchored and doesn't move.
- **setup2, setup3 (proposal, no keyboard in these frames):** the footer CTA stays pinned to the bottom safe area and is **not** lifted; the keyboard covers it while typing. Rationale: on setup3 the preview card ends at y 564, just above a standard keyboard (top ≈ 567), so the live preview stays visible while you type; lifting the CTA would cover it. Return key dismisses the keyboard (setup2 "Search", setup3 "Done"). Scrolling setup2's list dismisses the keyboard (iOS `.scrollDismissesKeyboard(.immediately)`; Android: clear focus on scroll). Tapping empty space dismisses it on setup3. iOS: exclude the footer from keyboard avoidance (`.ignoresSafeArea(.keyboard, edges: .bottom)` on the screen container).
- setup4 and allSet have no text input.

---

## 1. `setup1`: Setup 1 — Name & photo (42:665)

**Purpose:** step 1 of 4. The user picks a line-art avatar (5 presets) or a photo, and enters their name. Background `color/bg/primary` #FFFFFF.
**Layout:** frame auto-layout with children top-aligned (not space-between). `Content` column (20, 62, 362, 384). The CTA is **absolutely positioned** above the keyboard (§0.14). iOS kit keyboard, Type=Lower Case, no suggestion bar, at (0, 567, 402, 307).

### Elements, top to bottom

| # | Element | Frame (x, y, w, h) | safe-y | Spec |
|---|---|---|---|---|
| 1 | Setup header | (20, 62, 362, 82) | 0 | §0.1, **Step=1**, Skip hidden. Segment 1 black, 2–4 grey. Caption "Step 1 of 4" at (20, 126, 62, 18) |
| — | gap | y 144 → 168 | | 24 (`space/24`) |
| 2 | Headline | (20, 168, 362, 38) | 106 | "What’s your name?" (U+2019 apostrophe). **Title/1** (Manrope ExtraBold 32/38, −0.64 pt), `color/text/primary` #0A0A0A, left, fills width, 1 line |
| — | gap | y 206 → 218 | | 12 (`space/12`, text stack gap) |
| 3 | Body | (20, 218, 362, 48) | 156 | "Friends see your name and picture on shared expenses." **Body** (Manrope Regular 16/24), `color/text/secondary` #6B6B6B, left, fills width. Wraps to 2 lines: "Friends see your name and picture on shared" / "expenses." |
| — | gap | y 266 → 290 | | 24 (`space/24`) |
| 4 | Avatar options row | (20, 290, 362, 56) | 228 | Horizontal, **space-between**, vertically centred, 6 options of 56×56 (§0.6). Option x: **20, 81.2, 142.4, 203.6, 264.8, 326** (actual gap 5.2) |
| 4a | Option 1 | (20, 290, 56, 56) | | Art = avatar-1 (Arjun). **Selected** in the frame: 2.5 pt ring `color/border/strong` #0A0A0A. Inner 46 circle at (25, 295) |
| 4b | Option 2 | (81.2, 290, 56, 56) | | Art = avatar-2 (Priya), unselected |
| 4c | Option 3 | (142.4, 290, 56, 56) | | Art = avatar-3 (Rohan), unselected |
| 4d | Option 4 | (203.6, 290, 56, 56) | | Art = avatar-4 (Esha), unselected |
| 4e | Option 5 | (264.8, 290, 56, 56) | | Art = avatar-5 (Dev), unselected |
| 4f | Camera tile | (326, 290, 56, 56) | | Type=Upload: 46 circle `color/bg/card` at (331, 295), `Icon / Camera` 24×24 at (342, 306), `color/icon/primary` |
| — | gap | y 346 → 366 | | 20 (`space/20`) |
| 5 | Name field | (20, 366, 362, 80) | 304 | `Control / Input Field` **State=Focused** (§0.5). Show label = true, Show helper = **false**, Leading icon = false. Height 80 = label 20 + gap 8 + field 52 |
| 5a | label | (20, 366, hug 38, 20) | 304 | "Name". Subheadline, `color/text/secondary` #6B6B6B |
| 5b | field | (20, 394, 362, 52) | 332 | `color/bg/card` #F5F5F5, radius 14, focused ring 1.5 pt inside `color/border/strong` #0A0A0A |
| 5c | value | (37.5, 408, 327, 24) | 346 | Typed text in the frame: "Arjun Mehta". Body, `color/text/primary` #0A0A0A. Keep 16 pt inset in code (§0.5) |
| — | free space | y 446 → 503 | | Flexible, not a fixed gap |
| 6 | CTA "Continue" | (20, 503, 362, 52) | 441 | `Button / Primary` Large (§0.2), label "Continue". Absolute, left/right 20. **Bottom edge 12 pt above the keyboard top** (keyboard top y 567). Keyboard hidden: y 788–840 (bottom gap 0) |
| — | keyboard | y 567 → 874 | 505 | System |

### Behaviour
- **Name field:** label "Name". Placeholder (not designed; the component default "you@example.com" doesn't fit). Proposal: "Your name", Body, `color/text/tertiary` #A3A3A3. Single line. Auto-focus on appear; the focused ring shows while focused (empty or not); unfocused with text = Filled (no ring). iOS: `.textContentType(.name)`, `.textInputAutocapitalization(.words)`, `.autocorrectionDisabled()`, `.submitLabel(.continue)`. Android: `KeyboardOptions(capitalization = KeyboardCapitalization.Words, keyboardType = KeyboardType.Text, imeAction = ImeAction.Next or Done, autoCorrectEnabled = false)`. The return/IME action does the same as Continue when it's enabled. Error state isn't used.
- **Continue:** enabled only when the trimmed name is non-empty (flow.md). Disabled look: `color/bg/disabled` #E0E0E0 + `color/text/disabled` #A3A3A3. On tap: save the trimmed name and the avatar choice, go to setup2.
- **Avatar options:** single selection among the 5 presets and the photo. Tap an art option → it gets the ring; the previous one loses it. Tapping the selected option again keeps it selected. Accessibility: "Avatar 1" … "Avatar 5", selected trait; camera tile "Choose a photo".
- **Camera tile** (flow.md: opens the system photo picker and shows the chosen photo in the circle). Proposal for the details:
  - Tap → system photo picker, images only (iOS `PhotosPicker(selection:matching: .images)`; Android `ActivityResultContracts.PickVisualMedia(PickVisualMedia.ImageOnly)`). Cancel → nothing changes.
  - After a pick: the tile's 46 circle shows the photo (aspect-fill, clipped to the circle; the camera icon is hidden) and the tile gets the **same selected ring as art options** (2.5 pt `color/border/strong`, 2.5 pt gap). The tile keeps showing the photo when the user later selects an art option (then without the ring).
  - Tap the tile when it already shows a photo but is unselected → select it (no picker). Tap it when it's selected → open the picker again to replace the photo.
  - Save the photo downscaled (e.g. 512×512 JPEG, centre square crop) to Application Support (iOS) / `filesDir` (Android), per flow.md Persistence.
- **Initial selection** (open decision, see Issues): the frame shows option 1 selected, but that is the sample state (the debug seed has avatar 0). The designer note says "initials are the fallback". Proposal: a fresh profile starts with **no option selected**; if the user continues without choosing, the profile avatar is the initials fallback (§0.7). A saved choice (e.g. when coming back, or the debug seed) is shown selected.
- **Back:** pops to whatever came before (Verify, or Get Started for Apple/Google).

### Narrow screens (proposal)
6 × 56 = 336 pt must fit in the content width. On screens narrower than 376 pt (e.g. 360 dp Android: content 320) keep space-between with a minimum 4 pt gap and shrink each option to `min(56, (contentWidth − 20) / 6)`, keeping ring 2.5, gap 2.5 and inner circle = option − 10.

### Assets used
`assets/icons/chevron-left.svg`, `assets/icons/camera.svg`, `assets/avatars/avatar-1.svg` … `avatar-5.svg`.

---

## 2. `setup2`: Setup 2 — Currency (44:938)

**Purpose:** step 2 of 4. Pick the home currency. Background `color/bg/primary` #FFFFFF.
**Layout:** space-between. `Content` (20, 62, 362, 712) top-anchored; `Footer` (20, 788, 362, 52) bottom-anchored (bottom gap 0 to the safe area); a full-bleed `Scroll edge fade` above the footer. No keyboard in the frame.

### Elements, top to bottom

| # | Element | Frame (x, y, w, h) | safe-y | Spec |
|---|---|---|---|---|
| 1 | Setup header | (20, 62, 362, 82) | 0 | §0.1, **Step=2**, Skip hidden. Segments 1–2 black, 3–4 grey. "Step 2 of 4" (20, 126, 65, 18) |
| — | gap | y 144 → 168 | | 24 |
| 2 | Headline | (20, 168, 362, 38) | 106 | "Pick your currency". **Title/1**, `color/text/primary`, 1 line |
| — | gap | y 206 → 218 | | 12 |
| 3 | Body | (20, 218, 362, 48) | 156 | "Totals show in this currency. You can still add expenses in others." **Body**, `color/text/secondary` #6B6B6B. 2 lines: "Totals show in this currency. You can still add" / "expenses in others." |
| — | gap | y 266 → 286 | | 20 (`space/20`) |
| 4 | Search field | (20, 286, 362, 52) | 224 | `Control / Input Field` **State=Default**, Show label = false, Show helper = false, **Leading icon = true**, Icon = `Icon / Search` |
| 4a | leading icon | (36, 302, 20, 20) | 240 | `Icon / Search` (HugeIcons search-01) at 20 pt, stroke 1.25, **`color/icon/secondary` #6B6B6B** |
| 4b | placeholder | (68, 300, 298, 24) | 238 | "Search currencies". Body, `color/text/tertiary` #A3A3A3 |
| — | gap | y 338 → 358 | | 20 (`space/20`) |
| 5 | Section header "Suggested" | (20, 358, 362, 32) | 296 | `Row / Section Header` (§0.8), title "Suggested" at (20, 361, 105, 26), Show action = false |
| 6 | Row INR (selected) | (20, 390, 362, 56) | 328 | `Row / Currency` **Selected=True** (§0.9). Symbol "₹" (U+20B9, Headline), title "Indian Rupee", subtitle "INR · Based on your region" (space, U+00B7 MIDDLE DOT, space) |
| — | gap | y 446 → 462 | | 16 (`space/16`) |
| 7 | Section header "Popular" | (20, 462, 362, 32) | 400 | Title "Popular" at (20, 465, 74, 26) |
| 8 | Row USD | (20, 494, 362, 56) | 432 | Selected=False. Symbol "$" (Headline), "US Dollar", "USD" |
| 9 | Row EUR | (20, 550, 362, 56) | 488 | Selected=False. Symbol "€" (U+20AC, Headline), "Euro", "EUR" |
| 10 | Row GBP | (20, 606, 362, 56) | 544 | Selected=False. Symbol "£" (U+00A3, Headline), "British Pound", "GBP" |
| 11 | Row AED | (20, 662, 362, 56) | 600 | Selected=False. Symbol **"AED" in Caption/1** (Bold 12/16, +0.12 pt; text box (28, 682, 24, 16)), "UAE Dirham", "AED" |
| 12 | Row SGD | (20, 718, 362, 56) | 656 | Selected=False. Symbol "S$" (Headline), "Singapore Dollar", "SGD". Row bottom y 774 |
| — | space | y 774 → 788 | | 14 (space-between remainder; use it as the list's bottom padding) |
| 13 | Scroll edge fade | (0, 764, 402, 110) | 702 | Full-bleed rectangle, absolute, over the content. Linear gradient **top → bottom**: #FFFFFF at 0 % opacity (stop 0) → #FFFFFF 100 % (stop 22 %, i.e. y ≈ 788) → #FFFFFF 100 % (stop 100 %, y 874). So a 24 pt fade from transparent to white right above the CTA, solid white from the CTA top to the screen bottom |
| 14 | CTA "Continue" | (20, 788, 362, 52) | 726 | `Button / Primary` Large (§0.2), "Continue". Bottom = bottom safe-area edge (y 840). Always enabled (a currency is always selected) |

List spacing summary: search → 20 → section header (32) → rows (56 each, no gap, no dividers) → 16 → section header (32) → rows.

### Scrolling (flow.md: the list must scroll, Continue sticks to the bottom)
- Proposal: the Setup header stays pinned. Everything below it (headline, body, search field, sections and rows) is one vertical scroll view, from y 144 down to the footer top (y 788). Content keeps the Figma gaps; bottom content padding 14 so the last row ends at y 774 when scrolled to the end.
- The scroll edge fade and the footer are drawn above the scroll view and don't scroll. The fade's opaque part fills the area behind the CTA and home indicator (to the screen bottom).
- In Figma everything fits without scrolling on a 874-tall screen. Scrolling matters for search results, smaller screens and larger Dynamic Type.

### Data and behaviour (flow.md + proposals)
- **Suggested:** the device-region currency (iOS `Locale.current.currency?.identifier`; Android `Currency.getInstance(Locale.getDefault()).currencyCode`, in a try/catch). Fallback **INR**. Subtitle "{CODE} · Based on your region". Proposal: when the region is unknown and INR is only the fallback, show just "INR".
- **Popular:** USD, EUR, GBP, AED, SGD in that order, minus the suggested one.
- **Preselection:** the suggested currency is selected when the screen first opens. A previously saved currency (coming back, or the debug seed INR) is shown selected instead.
- **Names and symbols:** for the six designed currencies use the Figma strings exactly: INR "Indian Rupee" "₹", USD "US Dollar" "$", EUR "Euro" "€", GBP "British Pound" "£", AED "UAE Dirham" "AED", SGD "Singapore Dollar" "S$" (platform names differ, e.g. "United Arab Emirates Dirham"). For all other currencies use the platform display name (current locale) and the en-US symbol. Symbol style rule: symbol ≤ 2 characters → Headline; otherwise show the 3-letter ISO code in Caption/1 (as for AED).
- **Search** (behaviour not designed; proposal): filters ALL ISO 4217 currencies (iOS `Locale.commonISOCurrencyCodes`; Android `Currency.getAvailableCurrencies()`) by name or code, case- and diacritic-insensitive, on every keystroke. While the trimmed query is non-empty: hide both section headers and the Suggested/Popular rows, and show matching `Row / Currency` rows starting 20 pt below the search field (where "Suggested" was). Order: exact code match, then code prefix, then name prefix, then name contains; ties alphabetical by name. The selected currency keeps its check in the results. No matches: one line "No currencies match “{query}”" (Body, `color/text/secondary`, curly quotes U+201C/U+201D) 20 pt below the field. Clearing the query restores the sections. Focused field = 1.5 pt ring (§0.5). Show a clear button when there's text (iOS: native clear button; Android: trailing `Icon / Close` 20 pt, `color/icon/secondary`, 44 pt tap target). Keyboard: iOS `.submitLabel(.search)`, `.autocorrectionDisabled()`, `.textInputAutocapitalization(.never)`; Android `ImeAction.Search`. Return dismisses the keyboard.
- **Tap a row:** select it (single selection across all rows). Stay on the screen.
- **Continue:** save the selected currency code, go to setup3.

### Assets used
`assets/icons/chevron-left.svg`, `assets/icons/search.svg`, `assets/icons/check.svg` (radio, tinted white), `assets/icons/close.svg` (proposed clear button only).

---

## 3. `setup3`: Setup 3 — Payment (46:1021)

**Purpose:** step 3 of 4, optional. Add a UPI ID and preview how friends see it. Background `color/bg/primary` #FFFFFF.
**Layout:** space-between. `Content` (20, 62, 362, 502) top-anchored; `Footer` (20, 788, 362, 52). No keyboard in the frame (the field is shown Filled, unfocused).

### Elements, top to bottom

| # | Element | Frame (x, y, w, h) | safe-y | Spec |
|---|---|---|---|---|
| 1 | Setup header | (20, 62, 362, 82) | 0 | §0.1, **Step=3**, **Skip shown** at (351, 62, 31, 44). Segments 1–3 black, 4 grey. "Step 3 of 4" (20, 126, 64, 18) |
| — | gap | y 144 → 168 | | 24 |
| 2 | Headline | (20, 168, 362, 76) | 106 | "How should friends" **line break** "pay you?". The Figma text contains a U+2028 LINE SEPARATOR after "friends"; in code use `"How should friends\npay you?"`. **Title/1**, `color/text/primary`, 2 lines |
| — | gap | y 244 → 256 | | 12 |
| 3 | Body | (20, 256, 362, 48) | 194 | "Add your UPI ID. Friends see it when they settle up — Paybak never moves money." (space, U+2014 EM DASH, space). **Body**, `color/text/secondary`. 2 lines: "Add your UPI ID. Friends see it when they settle" / "up — Paybak never moves money." |
| — | gap | y 304 → 328 | | 24 |
| 4 | UPI field | (20, 328, 362, 106) | 266 | `Control / Input Field` **State=Filled** in the frame (§0.5). Show label = true, Show helper = true, Leading icon = false |
| 4a | label | (20, 328, hug 38, 20) | 266 | "UPI ID". Subheadline, `color/text/secondary` |
| 4b | field | (20, 356, 362, 52) | 294 | `color/bg/card`, radius 14. No ring when unfocused; 1.5 pt `color/border/strong` ring when focused |
| 4c | value | (36, 370, 330, 24) | 308 | Typed text in the frame: "arjun@okaxis". Body, `color/text/primary` |
| 4d | helper | (20, 416, hug 290, 18) | 354 | "Only people you share expenses with can see it." Footnote, `color/text/tertiary` #A3A3A3 |
| — | gap | y 434 → 458 | | 24 |
| 5 | Payment preview card | (20, 458, 362, 106) | 396 | `Card / Payment Preview` (§0.10), Show copy icon = true, Show copy = false. Ends y 564 |
| 5a | caption | (36, 474, 104, 18) | 412 | "What friends see". Footnote, `color/text/tertiary` |
| 5b | avatar | (36, 506, 40, 40) | 444 | The user's avatar from setup1, 40 pt, **white** circle `color/bg/primary`. Frame shows avatar-1 (Arjun) |
| 5c | name | (88, 504, 222, 22) | 442 | The user's name from setup1. Frame: "Arjun Mehta". Headline, `color/text/primary` |
| 5d | upi | (88, 528, 222, 20) | 466 | Live copy of the UPI field. Frame: "arjun@okaxis". Subheadline, `color/text/secondary` |
| 5e | copy button | (322, 504, 44, 44) | 442 | `Button / Icon` Plain + `Icon / Copy` 24 at (332, 514), `color/icon/primary`. Pressed = `color/bg/selected` circle |
| — | free space | y 564 → 788 | | Flexible |
| 6 | CTA "Continue" | (20, 788, 362, 52) | 726 | `Button / Primary` Large, "Continue". Bottom = bottom safe-area edge. Always enabled (the step is optional) |

### Behaviour
- **UPI field:** label "UPI ID", helper always shown. Placeholder (not designed; proposal): "yourname@bank", Body, `color/text/tertiary`. Don't auto-focus (the frame shows it unfocused). Keyboard: iOS `.keyboardType(.emailAddress)`, `.textInputAutocapitalization(.never)`, `.autocorrectionDisabled()`, `.submitLabel(.done)`; Android `KeyboardType.Email`, `ImeAction.Done`, no autocorrect, no capitalization. Done dismisses the keyboard. The footer CTA is not lifted (§0.14). Prefill with the saved UPI if there is one (debug seed "arjun@okaxis").
- **Validation** (not designed; proposal): Continue always works. Empty → save no UPI. Non-empty text that doesn't look like a UPI ID (`^[A-Za-z0-9._-]{2,}@[A-Za-z0-9]{2,}$`) → on Continue, show the field's **Error** state (red ring, helper in `color/text/destructive` #C93636) with helper "Enter a UPI ID like name@bank." and stay. Editing clears the error (helper goes back to the normal text). If you'd rather not invent copy, skip validation and save as typed.
- **Preview card** updates live: name = saved full name; avatar = saved choice (art / photo / initials, §0.7, on a white circle); upi = trimmed field text. Empty UPI (proposal): show "yourname@bank" in `color/text/tertiary` in the upi line and hide the copy button.
- **Copy button** (flow.md): copies the UPI ID to the clipboard (iOS `UIPasteboard.general.string`; Android `ClipboardManager.setPrimaryClip`). Feedback: the `Overlay / Toast` (§0.11) with **"UPI ID copied"** and the check-circle icon. The toast description says "50 above the bottom edge", but at that spot (y 780–824) it would sit on the Continue button, so proposal: centre it horizontally with its bottom **16 pt above the CTA** → (centred, 728, hug, 44). Fade + 8 pt upward move in 0.2 s, stays 2 s, fades out 0.2 s. Light haptic (optional). Android 13+ shows its own clipboard confirmation; still show the toast for consistency.
- **Skip** (header): go to setup4 without saving the typed text (keep any previously saved UPI unchanged). **Continue:** save the trimmed UPI (or none), go to setup4.

### Assets used
`assets/icons/chevron-left.svg`, `assets/icons/copy.svg`, `assets/icons/check-circle.svg` (toast), the chosen avatar (`assets/avatars/avatar-N.svg`, a saved photo, or initials).

---

## 4. `setup4`: Setup 4 — Notifications (46:1148)

**Purpose:** step 4 of 4, optional. Explain reminders before the OS permission prompt. Background `color/bg/primary` #FFFFFF.
**Layout:** space-between. `Content` (20, 62, 362, 536) top-anchored; `Footer` (20, 736, 362, 104) bottom-anchored: vertical, gap 8 (`space/8`), children **centred horizontally**.

### Elements, top to bottom

| # | Element | Frame (x, y, w, h) | safe-y | Spec |
|---|---|---|---|---|
| 1 | Setup header | (20, 62, 362, 82) | 0 | §0.1, **Step=4**, **Skip shown**. All 4 segments black. "Step 4 of 4" (20, 126, 65, 18) |
| — | gap | y 144 → 168 | | 24 |
| 2 | Illustration slot | layout box (20, 168, 362, 300) | 106 | **Rive**: `paybak-notifications.riv`, artboard `Notifications`, state machine `Notifications`. **Draw the Rive view at (8, 156, 386, 324)**, centred on the layout box, overflowing it by 12 on each side (§0.13). Figma placeholder = `Illustration / Reminders` (35:588): a sitting person, a notification card (190×84, radius 20, 2.5 pt `color/illustration/line` border, `color/illustration/fill` white, bell icon + two bars) and a ground line. Tapping the bell card rings it (Rive listener) |
| — | gap | y 468 → 500 | | 32 (`space/32`) |
| 3 | Headline | (20, 500, 362, 38) | 438 | "Get gentle reminders". **Title/1**, `color/text/primary`, 1 line |
| — | gap | y 538 → 550 | | 12 |
| 4 | Body | (20, 550, 362, 48) | 488 | "We’ll nudge you before something’s due and tell you when a friend pays you back." (two U+2019 apostrophes: "We’ll", "something’s"). **Body**, `color/text/secondary`. 2 lines: "We’ll nudge you before something’s due and tell" / "you when a friend pays you back." |
| — | free space | y 598 → 736 | | Flexible |
| 5 | CTA "Turn on notifications" | (20, 736, 362, 52) | 674 | `Button / Primary` Large, label "Turn on notifications" (label box (117, 751, 168, 22)). Figma annotation (verbatim): "Shows the iOS notification permission prompt." |
| — | gap | y 788 → 796 | | 8 (`space/8`) |
| 6 | "Not now" | (171.5, 796, 59, 44) | 734 | `Button / Text` **Style=Secondary** (§0.3), label "Not now" at (171.5, 808, 59, 20), `color/text/secondary` #6B6B6B, centred horizontally. Bottom = bottom safe-area edge (y 840). Pressed = 50 % opacity |

### Behaviour (flow.md)
- **Turn on notifications:** request the OS permission, then go to allSet **whatever the answer**.
  - iOS: `UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .badge, .sound])`. If the status is already determined (granted or denied), don't prompt; continue straight away.
  - Android 13+ (API 33): request `android.permission.POST_NOTIFICATIONS` (declare it in the manifest) with `ActivityResultContracts.RequestPermission()`. Below API 33 there's no runtime permission: continue straight away.
  - Save the notifications choice: granted → "enabled", refused → "denied". Ignore repeat taps while the prompt is up.
- **Not now** and header **Skip:** go to allSet without prompting. Save "skipped".
- **Back:** to setup3.

### Assets used
`assets/icons/chevron-left.svg`; Rive `paybak-notifications.riv` (iOS `Resources/Rive/paybak-notifications.riv`, Android `res/raw/paybak_notifications.riv`). Fallback only: `assets/images/illustration-reminders.svg`.

---

## 5. `allSet`: All set (46:1223)

**Purpose:** setup finished. Greet the user by first name and send them to Home. Background `color/bg/primary` #FFFFFF.
**Layout:** space-between. `Content` (20, 62, 362, 474) top-anchored; `Footer` (20, 788, 362, 52). **No header, no back button, no progress.**

### Elements, top to bottom

| # | Element | Frame (x, y, w, h) | safe-y | Spec |
|---|---|---|---|---|
| — | top gap | y 62 → 106 | | 44 (bound to `size/tap`, i.e. the height a header would take) |
| 1 | Illustration slot | (20, 106, 362, 300) | 44 | **Rive**: `paybak-allset.riv`, artboard `All Set` (362×300), state machine `All Set`. Draw the Rive view exactly in this rect (§0.13). Figma placeholder = `Illustration / All set` (35:607): three standing people and a round check badge (60×60 circle at slot (151, 0), 2.5 pt `color/illustration/line` ring, white fill, `Icon / Check` 36 pt). Tapping a person or the badge plays a jump (Rive listeners) |
| — | gap | y 406 → 438 | | 32 (`space/32`) |
| 2 | Headline | (20, 438, 362, 38) | 376 | "You’re all set, Arjun." (U+2019 apostrophe). **Dynamic:** `"You’re all set, \(firstName)."`, where firstName = the first whitespace-separated word of the saved trimmed name. **Title/1**, `color/text/primary`, left, fills width; 1 line in the frame, wraps if the name is long (height grows, everything below is top-anchored so nothing overlaps) |
| — | gap | y 476 → 488 | | 12 |
| 3 | Body | (20, 488, 362, 48) | 426 | "Add your first expense or" **line break** "invite friends to start splitting." The Figma text has a U+2028 LINE SEPARATOR after "or"; in code use `"Add your first expense or\ninvite friends to start splitting."`. **Body**, `color/text/secondary` #6B6B6B, 2 lines |
| — | free space | y 536 → 788 | | Flexible |
| 4 | CTA "Go to Home" | (20, 788, 362, 52) | 726 | `Button / Primary` Large, label "Go to Home" (label box (154, 803, 94, 22)). Bottom = bottom safe-area edge |

### Behaviour
- **Go to Home:** mark onboarding complete (`onboardingComplete = true`, flow.md), then replace the navigation stack with `homeFirstDay` (no way back into setup). Transition proposal: 0.4 s cross-dissolve. Proposal: also persist `onboardingComplete` as soon as allSet appears (all profile data is saved by then), so killing the app on this screen still lands on Home next launch. (flow.md ties it to "Go to Home"; see Issues.)
- **Back:** disabled (§0.12).
- Debug start `-startScreen allSet` / `--es startScreen allSet`: with the seed profile the headline reads "You’re all set, Arjun."

### Assets used
Rive `paybak-allset.riv` (iOS `Resources/Rive/paybak-allset.riv`, Android `res/raw/paybak_allset.riv`). Fallback only: `assets/images/illustration-all-set.svg`.

---

## 6. State differences between the four setup frames (quick diff)

| | setup1 | setup2 | setup3 | setup4 |
|---|---|---|---|---|
| Header `Step` | 1 | 2 | 3 | 4 |
| Black segments | 1 | 1–2 | 1–3 | 1–4 |
| Caption | "Step 1 of 4" | "Step 2 of 4" | "Step 3 of 4" | "Step 4 of 4" |
| Skip | hidden | hidden | shown | shown |
| Frame layout | top-aligned, CTA absolute above keyboard | space-between | space-between | space-between |
| Footer | "Continue" (y 503 above keyboard; 788 without) | "Continue" y 788 + scroll-edge fade | "Continue" y 788 | "Turn on notifications" y 736 + "Not now" y 796 |
| Continue enabled | name non-empty | always | always | n/a |
| Headline top | y 168 | y 168 | y 168 (2 lines) | y 500 (below the illustration) |

---

## 7. Assets (all paths relative to `docs/design/`)

| File | Used on | Notes |
|---|---|---|
| `assets/icons/chevron-left.svg` | setup1–4 back | 24×24, stroke 1.5, #0A0A0A (`color/icon/primary`) |
| `assets/icons/camera.svg` | setup1 camera tile | 24×24, `color/icon/primary` |
| `assets/icons/search.svg` | setup2 search field | draw at 20×20, tint `color/icon/secondary` #6B6B6B |
| `assets/icons/check.svg` | setup2 selected radio | draw at 14×14, tint `color/icon/inverse` #FFFFFF |
| `assets/icons/close.svg` | setup2 clear button (proposal only) | 20×20, `color/icon/secondary` |
| `assets/icons/copy.svg` | setup3 copy button | 24×24, `color/icon/primary` |
| `assets/icons/check-circle.svg` | setup3 "UPI ID copied" toast | 20×20, tint `color/icon/inverse` #FFFFFF |
| `assets/avatars/avatar-1.svg` … `avatar-5.svg` | setup1 options, setup3 preview avatar (and anywhere the profile avatar shows) | 120×120 art, transparent; scale to the circle and clip (§0.7). avatar-1 = Arjun (preset 0) … avatar-5 = Dev (preset 4) |
| `assets/images/illustration-reminders.svg` | setup4, **fallback only** | 362×300 static art of the Figma placeholder |
| `assets/images/illustration-all-set.svg` | allSet, **fallback only** | 362×300 static art of the Figma placeholder |
| `paybak-notifications.riv` (in the apps: iOS `Resources/Rive/`, Android `res/raw/paybak_notifications.riv`) | setup4 | see `rive.md` |
| `paybak-allset.riv` (in the apps: iOS `Resources/Rive/`, Android `res/raw/paybak_allset.riv`) | allSet | see `rive.md` |
| `Manrope-*.ttf` (in the apps: iOS `Resources/Fonts/`, Android `res/font/manrope_*.ttf`) | all | Regular, Medium, SemiBold, Bold, ExtraBold |

All icons are HugeIcons stroke-rounded on a 24 grid; the SVG strokes are #0A0A0A. Tint them (iOS template rendering / Android `tint`) where another colour token is listed. Scale the whole icon (stroke scales with it, as in Figma).
