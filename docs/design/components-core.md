# Paybak core component library (components-core.md)

Scope: Figma page **"02 Components" (3:3)**, sections **Brand (8:7)**, **Buttons (9:9)**, **Badges & Avatars (11:19)**, **Controls (12:215)** and **Sign-in & Setup (36:590)**. Everything below was read with the Plugin API (exact values, bound variables and text styles). The React reference code and screenshots read for 9-36, 10-79, 12-296, 36-666, 36-702, 37-655, 37-673, 8-31 and 7-5 weren't kept; the same values are in the node JSON (`.figma-cache/nodes/3-3-a.json`, `3-3-b.json`) and the 2× renders are `.figma-cache/components/<id>.png` (regenerate with `tools/fetch_figma.py`).
Related: `tokens.md` (token values), `foundations-rules.md` (usage rules), `assets/icons/INDEX.md`, `assets/brand/INDEX.md`, `assets/avatars/INDEX.md`. Home-specific components (rows, cards, tab bar, sheet) are in `components-home.md`.

## 0. Conventions
- Units are **pt** (iOS) = **dp** (Android). Coordinates `(x, y)` are **relative to the component's top-left**. Components are placed on screens by the screen specs; this file doesn't repeat screen positions (the 402 × 874 frame, 62-pt top safe area and 34-pt bottom are handled there).
- Colours: **token name** (`color/` prefix dropped) + hex. Text styles: names from `tokens.md` (e.g. `Button/Large` = Manrope SemiBold 17/22, −0.5 % → −0.085 pt).
- "Hug" = size to content; "Fill" = take the remaining width; "fixed" = exact size. `radius/full` (999) = capsule/circle.
- Auto-layout notation: **H**/**V** = horizontal/vertical stack; padding `[top, right, bottom, left]`; alignment `main/cross`.
- Strokes marked **inside** are drawn inside the bounds. In Figma these components have `strokesIncludedInLayout = true`, so an inside ring also pushes auto-layout content inwards by the stroke width (noted where it matters).
- Icon files: `assets/icons/<name>.svg` (24 × 24, scale to the slot size; the stroke scales with it).
- **Interactions/motion**: none of the nodes in these sections has a prototype reaction, and `get_motion_context` reports no animations. States are static variants. Implement "Pressed" while touch is down (instant or ≤ 100 ms fade; Figma specifies no transition). "Disabled" = not tappable.
- SwiftUI names come from the Figma component descriptions (e.g. `PBButton(.primary)`). Use the same names in Kotlin (`PbButton(style = Primary)`) for parity.

---

## 1. Brand (8:7)
Full details and file list: `assets/brand/INDEX.md`. Summary:

### 1.1 Brand / App Mark (8:22): `PBAppMark(size:)`
Property **Size = 160 | 96 | 40 | 28** (square). Description: "Paybak app mark. Sizes: 160 (cover), 96 (splash), 40, 28 (header lockup)."
| Size | Node | Corner radius (22.37 %, **corner smoothing 60 %**) | Glyph centre-line bbox (x, y, w × h) | Glyph stroke |
|---|---|---|---|---|
| 160 | 8:10 | 35.792 | (55, 37.5), 53.75 × 85 | 17.5 |
| 96 | 8:13 | 21.4752 | (33, 22.5), 32.25 × 51 | 10.5 |
| 40 | 8:16 | 8.948 | (13.75, 9.375), 13.44 × 21.25 | 4.375 |
| 28 | 8:19 | 6.2636 | (9.625, 6.5625), 9.41 × 14.88 | 3.0625 |
- Tile fill `bg/inverse` #0A0A0A, clip content, no stroke, no shadow.
- Glyph: one **stroked vector path** (not text), stroke `icon/inverse` #FFFFFF, **round cap + round join**, width 0.109375 × S. Centre-line (× S): M (0.34375, 0.765625) → L (0.34375, 0.234375) → L (0.5078125, 0.234375) → clockwise half-circle, centre (0.5078125, 0.40625), r 0.171875 → (0.5078125, 0.578125) → L (0.34375, 0.578125).
- Draw natively (SwiftUI `RoundedRectangle(cornerRadius: 0.2237*S, style: .continuous)`; Compose path from `assets/brand/app-mark.svg`) or use `assets/brand/app-mark.svg` (true smoothing) / `app-mark-{160,96,40,28}.svg` (Figma exports, plain `rx`, no smoothing).

### 1.2 Brand / Logo (8:31): `PBLogo(layout:)`
Property **Layout = Horizontal | Stacked**. The wordmark **"Paybak" is live text** (Manrope ExtraBold 800, `text/primary` #0A0A0A), not outlines.
| | Horizontal (8:23), 104 × 28 | Stacked (8:27), 135 × 156 |
|---|---|---|
| Layout | H, hug, cross-axis **center**, padding 0, gap **8** (`space/8`) | V, hug, cross-axis **center**, padding 0, gap **16** (`space/16`) |
| Mark | App Mark Size=28 at (0, 0) | App Mark Size=96 at (19.5, 0) |
| Wordmark | `Brand/Wordmark S` (20/24, −3 % = −0.6 pt), text box 68 × 24 at (36, 2) | `Brand/Wordmark L` (40/44, −3 % = −1.2 pt), 135 × 44 at (0, 112) |
| Used | Get Started top logo; Home header (long-press opens the debug menu) | Splash |
Files: `assets/brand/logo-horizontal.svg`, `logo-stacked.svg` (text outlined by the exporter; in-app use real text).

---

## 2. Buttons (9:9)
Section rule (verbatim): “Pill buttons: Large 52pt (one primary per screen) and Small 36pt (in rows and cards). Primary = black. Secondary = gray on white. On Card = white on #F5F5F5 cards. Destructive = red, only for delete and sign out.”

### 2.1 Pill buttons: Button / Primary (9:36), Secondary (9:62), On Card (9:88), Destructive (9:114)
All four sets share one anatomy. SwiftUI: `PBButton(.primary | .secondary | .onCard | .destructive)`.

**Properties** (same on all four)
| Property | Type | Default |
|---|---|---|
| `Label` | text | "Continue" (Destructive: "Delete") |
| `Leading icon` | boolean | false (icon hidden) |
| `Icon` | instance swap | Icon / Plus (5:13) |
| `State` | variant | Default · Pressed · Disabled |
| `Size` | variant | Large · Small |

**Anatomy / layout**
| | Size=Large | Size=Small |
|---|---|---|
| Container | H auto-layout, **height 52** fixed (`size/button-lg`), width **hug**, align center/center, padding [0, **24**, 0, **24**] (`space/24`), radius **full** (`radius/full`) | height **36** (`size/button-sm`), padding [0, **16**, 0, **16**] (`space/16`), radius full |
| Gap icon → label | **8** (`space/8`) | **6** (`space/6`) |
| Leading icon slot | **20 × 20** (24-grid icon scaled → stroke 1.25), vertically centred, before the label; hidden unless `Leading icon` | **16 × 16** (stroke 1.0) |
| Label | `Button/Large`: Manrope SemiBold 17 / line 22 / −0.085 pt, single line, no wrap | `Button/Small`: SemiBold 15 / 20 / −0.0375 pt |
| Label position (no icon) | (24, 15) | (16, 8) |
| Hug size with default label | Primary/Secondary/On Card "Continue": **124 × 52** (label 76 × 22); Destructive "Delete": **102 × 52** | "Continue" **99 × 36** (label 67 × 20); "Delete" **79 × 36** |
| Stroke / shadow | none | none |
When a screen stretches a Large button to the content width it is **362 × 52** with the label (and icon) centred as a group (e.g. Get Started).

**Colours per style × state** (icon tint = label colour)
| Style | Default: fill / label | Pressed: fill / label | Disabled: fill / label |
|---|---|---|---|
| **Primary** | `bg/inverse` #0A0A0A / `text/inverse` #FFFFFF (icon `icon/inverse`) | `bg/inverse-pressed` #2B2B2B / `text/inverse` #FFFFFF | `bg/disabled` #E0E0E0 / `text/disabled` #A3A3A3 (icon `icon/tertiary` #A3A3A3) |
| **Secondary** | `bg/card` #F5F5F5 / `text/primary` #0A0A0A (icon `icon/primary`) | `bg/card-pressed` #EBEBEB / `text/primary` | `bg/card` #F5F5F5 / `text/disabled` #A3A3A3 |
| **On Card** (use on #F5F5F5 cards) | `bg/primary` #FFFFFF / `text/primary` #0A0A0A | `bg/card-pressed` #EBEBEB / `text/primary` | `bg/primary` #FFFFFF / `text/disabled` #A3A3A3 |
| **Destructive** (delete/sign out only) | `bg/destructive` #C93636 / `text/inverse` #FFFFFF | `bg/destructive-pressed` #A92E2E / `text/inverse` | `bg/disabled` #E0E0E0 / `text/disabled` #A3A3A3 |
Pressed changes **only the fill** (no scale, no opacity) in Figma.

**Descriptions (verbatim)**: Primary "Main action — black pill. One Large primary per screen. SwiftUI: PBButton(.primary)" · Secondary "Secondary action on white backgrounds — #F5F5F5 pill. SwiftUI: PBButton(.secondary)" · On Card "Action placed on #F5F5F5 cards — white pill. SwiftUI: PBButton(.onCard)" · Destructive "Delete / Sign out only — muted red. SwiftUI: PBButton(.destructive)".

**Verified usages**
- Get Started "Continue with Apple" (22:328): Primary, Large, `Leading icon` = true, Icon = **Icon / Apple** (20 × 20, overridden to **white**), label "Continue with Apple", 362 × 52. Prototype: ON_CLICK → push to Setup 1 (42:665), 350 ms ease-in-out.
- Get Started "Continue with Google" (22:333): **Secondary**, Large, Icon = **Icon / Google** (20 × 20, official 4 colours, never tinted), label "Continue with Google", 362 × 52, 12 pt below the Apple button (y 64 vs 0 inside their stack). ON_CLICK → push to Setup 1.
- Files: `assets/brand/apple-logo.svg` (= `icons/apple.svg`), `assets/brand/google-g.svg` (= `icons/google.svg`).

**Implementation**
- SwiftUI: a `ButtonStyle` reading `configuration.isPressed` for the Pressed fill and `isEnabled` for Disabled; `Capsule()` background; `.frame(height: 52)`, `.padding(.horizontal, 24)`; `.frame(maxWidth: .infinity)` when the screen stretches it.
- Compose: `Box`/`Row` with `clip(CircleShape)` (capsule via `RoundedCornerShape(50)`), `interactionSource.collectIsPressedAsState()`, `height(52.dp)`, horizontal padding 24.dp, `indication = null` (no ripple; the pressed fill *is* the feedback).

### 2.2 Button / Text (10:45): `PBTextButton`
Description: "Text-only action (See all, Skip, Continue with email or phone). Pressed = 50% opacity."
**Properties**: `Label` text (default "See all"), `Trailing chevron` boolean (false), `Style` = Primary · Secondary · Destructive, `State` = Default · Pressed · Disabled.
**Layout**: H auto-layout, **height 44** fixed (`size/tap`), width hug, padding 0, gap **2** (`space/2`), align start/center. Label `Button/Small` (SemiBold 15/20/−0.0375 pt) at (0, 12). Optional trailing **Icon / Chevron Right 16 × 16** (stroke 1.0) after the label (hidden by default). Default "See all" = **47 × 44**.
| Style | Default label (chevron) | Pressed | Disabled |
|---|---|---|---|
| Primary | `text/primary` #0A0A0A (`icon/primary`) | whole button **opacity 0.5** | `text/disabled` #A3A3A3 |
| Secondary | `text/secondary` #6B6B6B (`icon/secondary`) | opacity 0.5 | `text/disabled` #A3A3A3 |
| Destructive | `text/destructive` #C93636 (chevron: `icon/destructive`, inferred) | opacity 0.5 | `text/disabled` #A3A3A3 |
No fill, no stroke. Keep the 44-pt height as the hit area (and make it at least 44 wide for short labels like "Skip").
**Verified usages**: "Continue with email or phone" on Get Started (22:342) = Primary, 209 × 44, ON_CLICK → push to Sign in (39:356) 350 ms ease-in-out. "Skip" in Navigation / Setup Header = Secondary, 31 × 44.

### 2.3 Button / Icon (10:79): `PBIconButton`
Description: "Circular 44pt icon button. Glass = Liquid Glass for floating toolbar buttons (Home bell). Badge = small black unread dot."
**Properties**: `Icon` instance swap (default Icon / Bell 5:26), `Badge` boolean (false), `Style` = Plain · Filled · Glass · Inverse, `State` = Default · Pressed.
**Layout**: **44 × 44** fixed (`size/tap`), radius full, auto-layout centred; icon **24 × 24** at (10, 10), stroke 1.5.
**Badge** (when `Badge` = true): ellipse **10 × 10**, absolutely positioned at **(26, 9)** (top-right), with a **2-pt OUTSIDE stroke** (ring), so the visible dot is 14 × 14 spanning (24, 7)–(38, 21).
| Style | Default | Pressed | Icon | Badge |
|---|---|---|---|---|
| Plain | no fill | fill `bg/selected` (#0A0A0A @ 6 %) | `icon/primary` #0A0A0A | fill `bg/inverse` #0A0A0A, ring `bg/primary` #FFFFFF |
| Filled | `bg/card` #F5F5F5 | `bg/card-pressed` #EBEBEB | `icon/primary` | same as Plain |
| Glass | `bg/glass` (#FFFFFF @ 72 %) + **1-pt inside stroke** `border/glass-highlight` (#FFFFFF @ 60 %) + effect **Material/Glass Small** (drop shadow 0, 4, blur 16, #0A0A0A @ 8 %; GLASS radius 6) | fill `bg/card` #F5F5F5 (opaque) + the same stroke + effect | `icon/primary` | same as Plain |
| Inverse | `bg/inverse` #0A0A0A | `bg/inverse-pressed` #2B2B2B | `icon/inverse` #FFFFFF | fill `bg/primary` #FFFFFF, ring `bg/inverse` #0A0A0A |
**Glass on each platform**: iOS 26+ `Image(...).frame(44,44).glassEffect(.regular.interactive(), in: .circle)` (the system supplies the pressed response). Android: circle fill #FFFFFFB8 (72 %), 1 dp inside border #FFFFFF99, shadow y 4 dp blur 16 dp #0A0A0A14, pressed fill #F5F5F5.
**Verified usages**: back button of Navigation / Setup Header = Plain + Icon / Chevron Left. Home header bell = Glass + Bell (+ Badge when unread; see components-home.md).

### 2.4 Button / Add (10:87): `PBAddButton`
Description: "Center ＋ in the tab bar. Opens the Add sheet (Add expense · Record payment · Lend money · New group)."
**Property**: `State` = Default · Pressed. **52 × 52** (`size/add-button`), radius full, fill `bg/inverse` #0A0A0A (Pressed `bg/inverse-pressed` #2B2B2B). Icon / Plus 24 × 24 at (14, 14), `icon/inverse` #FFFFFF, **stroke width 2** (the plus vector is 16 × 16 at (4, 4) inside the icon). This is the one icon drawn with a heavier stroke: use `plus.svg` with `stroke-width="2"` (= `assets/images/add-button-plus.svg`). No shadow of its own (it sits in the glass tab bar).

---

## 3. Badges & Avatars (11:19)
Section rule: “Badges are 24pt pills in Caption/1 Bold. Red Overdue is the only coloured badge. Avatars use Open Peeps heads on #F5F5F5, with initials or an icon as fallbacks.”

### 3.1 Badge / Pill (11:46): `PBBadge`
Description: "Status pill. Overdue (red) is reserved for overdue items. On Card = white pill for use on #F5F5F5 cards."
**Properties**: `Label` text (default "Due Fri"), `Show icon` boolean (false), `Icon` instance swap (default Icon / Calendar 5:88), `Style` = Muted · On Card · Inverse · Overdue.
**Layout**: H auto-layout, **height 24** fixed, width hug, padding [0, **10**, 0, **10**] (10 is a literal, not a token), gap **4** (`space/4`), align start/center, radius full. Optional icon **14 × 14** (stroke 0.875) at (10, 5) before the label; label `Caption/1` (Manrope **Bold 12 / 16, +1 % = +0.12 pt**) at (10, 4) without icon, (28, 4) with icon. Default "Due Fri" = **61 × 24** (label 41 × 16).
| Style | Fill | Label | Icon |
|---|---|---|---|
| Muted (on white) | `bg/card` #F5F5F5 | `text/secondary` #6B6B6B | `icon/secondary` #6B6B6B |
| On Card (on #F5F5F5 cards) | `bg/primary` #FFFFFF | `text/secondary` #6B6B6B | `icon/secondary` |
| Inverse | `bg/inverse` #0A0A0A | `text/inverse` #FFFFFF | `icon/inverse` #FFFFFF |
| Overdue (only coloured badge) | `bg/destructive` #C93636 | `text/inverse` #FFFFFF | `icon/inverse` |
Not interactive.

### 3.2 Avatar / Circle (11:136): `PBAvatar`
Description: "Circular avatar on #F5F5F5. Art = Open Peeps head (swap per person). Initials = fallback. Icon = category or group icon in a #F5F5F5 circle, for white screens. Icon On Card = the same icon in a white circle, for use inside #F5F5F5 cards."
**Properties**: `Art` instance swap (default Art / Peep Head / Arjun 7:5), `Initials` text (default "AK"), `Icon` instance swap (default Icon / Groups 5:10), `Size` = 24 · 32 · 40 · 56 (`size/avatar-xs|sm|md|lg`), `Type` = Art · Initials · Icon · Icon On Card.
All types: a **circle S × S**, radius full, **clips content**, no stroke.
| Type | Fill | Content |
|---|---|---|
| Art | `bg/card` #F5F5F5 | the Peep head (`assets/avatars/avatar-N.svg`, transparent) scaled **uniformly to S × S at (0, 0)**, clipped by the circle |
| Initials | `bg/card` #F5F5F5 | initials centred, `text/primary` #0A0A0A, style by size (below) |
| Icon | `bg/card` #F5F5F5 | icon centred, `icon/primary` #0A0A0A, size by avatar size (below) |
| Icon On Card | `bg/primary` #FFFFFF | same icon rules |
| Size | Initials style (text box) | Icon size (stroke) | Icon origin |
|---|---|---|---|
| 24 | `Caption/2` SemiBold 11/13 +1 % (15 × 13) | 14 (0.875) | (5, 5) |
| 32 | `Caption/1` Bold 12/16 +1 % (16 × 16) | 16 (1.0) | (8, 8) |
| 40 | `Headline` SemiBold 16/22 −0.25 % (21 × 22) | 20 (1.25) | (10, 10) |
| 56 | `Title/3` Bold 20/26 −1 % (27 × 26) | 24 (1.5) | (16, 16) |
Initials content: use the first letters of first + last name, uppercase (Figma sample "AK"). **Verified usages (Home — Active)**:
- Overdue row "Rohan": Size=40 Art = Rohan (`avatar-3`).
- "Goa Trip — due Fri": Size=40 **Icon** with Icon / Groups.
- Recent-activity row: Size=40 Art = Priya (`avatar-2`).
- **Fill override (editor, verified in Figma):** the two `Row / Attention` avatars sit on a #F5F5F5 card, so they are filled **`bg/primary` #FFFFFF**, for both Art and Icon types. The `Row / Activity` avatar sits on white and keeps `bg/card` #F5F5F5. General rule: circle fill = `bg/card` on white surfaces, `bg/primary` on #F5F5F5 cards. Setup 3 "What friends see" preview (inside Card / Payment Preview 37:675): Size=40 Art (Arjun in the frame; the user's choice in the app). Inside that card the circle is drawn on white (`bg/primary`), per screens-setup.md. The transparent art works on either fill.

### 3.3 Avatar / Stack (11:417): `PBAvatarStack`
Description: "Overlapping 32pt avatars with a 2pt white ring. Used for split participants."
**Property**: `Count` = 2 · 3 · 4. H auto-layout, align start/start, padding 0, **gap −8** (8 pt overlap), hug. Each item = Avatar / Circle **Size=32, Type=Art** plus a **2-pt OUTSIDE stroke `bg/primary` #FFFFFF** (white ring; visual diameter 36). Items at x = 0, 24, 48, 72; later items are drawn **on top** of earlier ones. Sizes (without ring overflow): Count=2 **56 × 32**, 3 **80 × 32**, 4 **104 × 32**. Default art order: Arjun, Priya, Rohan, Esha. Not used on the in-scope screens.

---

## 4. Controls (12:215)
Section rule: “Page dots for onboarding, a segmented control for filters (Groups | Friends), and input fields on #F5F5F5 with a black focus ring. Errors use the red accent.”

### 4.1 Control / Page Dots (12:230): `PBPageDots(count:active:)`
Description: "Onboarding pager. Active dot is a 24×8 black pill."
**Property**: `Active` = 1 · 2 · 3. H auto-layout, hug → **52 × 8**, gap **6** (`space/6`), align start/center. Active dot **24 × 8** pill `bg/inverse` #0A0A0A; inactive dots **8 × 8** circles `bg/indicator` #D1D1D1; all radius full.
| Active | dot 1 | dot 2 | dot 3 |
|---|---|---|---|
| 1 | x 0, **24 wide** | x 30 | x 44 |
| 2 | x 0 | x 14, **24 wide** | x 44 |
| 3 | x 0 | x 14 | x 28, **24 wide** |
Not interactive (swipe/CTA change the step). Figma defines no transition. Animating width and colour when `step` changes (about 250 ms, ease-in-out) matches flow.md's "animate text change", but that part is a suggestion.

### 4.2 Control / Segment (12:236): `PBSegment`
Description: "Building block of Control / Segmented. Selected = black pill."
**Properties**: `Label` text (default "Groups"), `Selected` = True · False. **117 × 30** fixed (width is set by the parent Segmented: (container − 6) / n), radius full, label `Button/Small` (SemiBold 15/20/−0.0375 pt) centred.
Selected: fill `bg/inverse` #0A0A0A, label `text/inverse` #FFFFFF. Not selected: **no fill**, label `text/secondary` #6B6B6B.

### 4.3 Control / Segmented (12:249): `PBSegmentedControl`
Description: "Pill segmented control on #F5F5F5 with a black selected segment. Options=2: Groups | Friends, Timeline | Insights. Options=3: filters. Options=4: the split editor (Equally · Exact · % · Shares)."
**Property**: `Options` = 2 · 3 · 4. Container: H auto-layout, **height 36** fixed, **padding 3 on all sides**, gap 0, fill `bg/card` #F5F5F5, radius full.
| Options | Container | Segment width × height | Segment x positions | Default labels (first selected) |
|---|---|---|---|---|
| 2 | **240 × 36** | 117 × 30 | 3, 120 | "Groups", "Friends" |
| 3 | **330 × 36** | 108 × 30 | 3, 111, 219 | "All", "Upcoming", "Overdue" |
| 4 | **362 × 36** | 89 × 30 | 3, 92, 181, 270 | "Equally", "Exact", "%", "Shares" |
Tapping a segment selects it (single selection). A sliding black pill (matchedGeometryEffect / animated offset) is a suggestion; Figma has no motion. Not used on the in-scope screens.

### 4.4 Control / Input Field (12:296): `PBTextField`
Description: "52pt field on #F5F5F5, 14pt radius. Focused = 1.5pt black ring. Error = red ring + red helper."
**Properties**: `Label` text ("Email"), `Value` text ("you@example.com"), `Helper` text ("We’ll send a 6-digit code.", curly apostrophe), `Show label` boolean (true), `Show helper` boolean (true), `Leading icon` boolean (false), `Icon` instance swap (Icon / Mail 5:42), `State` = Default · Focused · Filled · Error · Disabled.
**Layout**: V auto-layout, width **362** fixed, height hug (**106** with label and helper), gap **8** (`space/8`), align start.
1. `label` at (0, 0): `Subheadline` (Medium 14/20), `text/secondary` #6B6B6B (Disabled: `text/disabled` #A3A3A3).
2. `field` at (0, 28), **362 × 52** (fill width, height 52): H auto-layout, padding [0, **16**, 0, **16**] (`space/16`), gap **12** (`space/12`), align start/center, fill `bg/card` #F5F5F5, radius **14** (`radius/input`).
   - optional leading icon **20 × 20** (stroke 1.25), `icon/secondary` #6B6B6B, at (16, 16)
   - `value` text: `Body` (Regular 16/24), fills the remaining width, single line; at (16, 14)
3. `helper` at (0, 88): `Footnote` (Medium 13/18), `text/tertiary` #A3A3A3.

| State | Field ring | Value text colour | Helper colour |
|---|---|---|---|
| Default (empty; Value shows the **placeholder**) | none | `text/tertiary` #A3A3A3 (placeholder) | `text/tertiary` #A3A3A3 |
| Focused | **1.5-pt inside** stroke `border/strong` #0A0A0A | `text/primary` #0A0A0A | `text/tertiary` |
| Filled (has text, not focused) | none | `text/primary` #0A0A0A | `text/tertiary` |
| Error | **1.5-pt inside** stroke `border/destructive` #C93636 | `text/primary` #0A0A0A | **`text/destructive` #C93636** (helper carries the error message) |
| Disabled | none | `text/disabled` #A3A3A3 | `text/disabled` #A3A3A3 (label too) |
- Note: because Figma includes strokes in layout, the value text moves from x 16 → **17.5** when the ring appears (Focused/Error). Recommended: draw the ring as an **overlay** so the text doesn't jump (the difference is 1.5 pt; either is acceptable).
- Caret/selection colour: `text/primary` #0A0A0A (Figma's Code Digit caret is #0A0A0A; use the same tint for text fields).
- iOS: `TextField` with `.focused(...)`; Android: `BasicTextField` with custom decoration. Placeholder = the `Value` string in `text/tertiary` when empty.

### 4.5 Divider / Line (12:302): `PBDivider`
Description: "1pt hairline in #EBEBEB. Leading inset aligns with row text after a 40pt avatar. Use sparingly — prefer white space."
**Property**: `Inset` = None · Leading. Height **1** (`stroke/hairline`). Colour `border/subtle` #EBEBEB.
- None: line spans the full width (362 in the component).
- Leading: left padding **52** (40 avatar + 12 gap), line 310 wide starting at x 52.
On a 3× screen 1 pt = 3 px; don't use a 1-px hairline.

---

## 5. Sign-in & Setup (36:590)
Section rule: “Components for sign in and first-run setup: step header with progress, 6-digit code input, avatar picker option, currency row and payment preview.” (Card / Payment Preview 37:675 is specced in screens-setup.md.)

### 5.1 Navigation / Setup Header (36:666): `PBSetupHeader`
Description: "First-run setup header: back, optional Skip, 4-segment progress and step count."
**Properties**: `Show Skip` boolean (false), `Step` = 1 · 2 · 3 · 4.
**Layout**: V auto-layout, width **362**, height **82**, gap **8** (`space/8`), align start.
1. `top` row at (0, 0), **362 × 44**, H auto-layout, align start/center, clips:
   - `back`: **Button / Icon, Style=Plain**, 44 × 44 at (0, 0), Icon = **Icon / Chevron Left** (24 × 24 at (10, 10) inside, `icon/primary`). Pressed = `bg/selected` 6 % circle.
   - `spacer`: fills.
   - `skip`: **Button / Text, Style=Secondary, Label "Skip"**, 31 × 44, right-aligned at x 331. **Only visible when `Show Skip` = true** (Setup 3 and Setup 4 per flow.md).
2. `progress` at (0, 52), **362 × 4**: H auto-layout, gap **4** (`space/4`); **4 equal segments**, each **87.5 × 4**, radius full, at x 0, 91.5, 183, 274.5. Segment *i* is `bg/inverse` #0A0A0A when *i* ≤ Step, else `bg/indicator` #D1D1D1.
3. `step` text at (0, 64): "Step N of 4" (e.g. "Step 1 of 4"), `Footnote` (Medium 13/18), `text/secondary` #6B6B6B. Width hug (62–65).
Not interactive apart from back/skip. Filling the next segment when moving between steps may be animated (suggestion, no Figma motion).

### 5.2 Control / Code Digit (36:675): `PBCodeDigit`
Description: "One box of the 6-digit code."
**Properties**: `Digit` text (default "4"), `State` = Empty · Focused · Filled · Error.
**48 × 56** fixed, fill `bg/card` #F5F5F5, radius **14** (`radius/input`), content centred.
| State | Ring | Content |
|---|---|---|
| Empty | none | nothing |
| Focused | **1.5-pt inside** stroke `border/strong` #0A0A0A | **caret**: 2 × 24 rectangle, `bg/inverse` #0A0A0A, at (23, 16) (centred) |
| Filled | none | digit, `Title/2` (Manrope **Bold 24/30, −1.5 % = −0.36 pt**), `text/primary` #0A0A0A, centred (text box 15 × 30 at (16.5, 13) for "4") |
| Error | **1.5-pt inside** stroke `border/destructive` #C93636 | digit still `text/primary` #0A0A0A |
Caret blink isn't specified in Figma; a standard ~1 s blink is fine.

### 5.3 Control / Code Input (36:702): `PBCodeField`
Description: "6-digit code field. Auto-advances and verifies on the 6th digit."
**Property**: `State` = Typing · Error. H auto-layout, **348 × 56** in the component (fixed width), justify **space-between**, gap **12** (`space/12`, the minimum), align start. Six Code Digits at x = 0, 60, 120, 180, 240, 300.
- **On screens it is stretched** (editor, verified in Figma): both instances (verify 39:551 and verifyWrong 39:661) use `layoutSizingHorizontal = FILL`, so they are **362 × 56** with space-between. The actual gap is (362 − 6 × 48) / 5 = **14.8**, and the boxes sit at x 0, 62.8, 125.6, 188.4, 251.2, 314 (frame x 20 … 334). Implement the row as "fill the content width, spread the boxes, minimum gap 12". The numbers are in `screens-signin.md` §0.6.
- Typing (figure): digits 1–4 Filled "4", "8", "2", "9"; digit 5 **Focused** (caret); digit 6 Empty. The focused box is the next empty position.
- Error: **all six** Error boxes (red rings) with "4", "8", "2", "9", "1", "7".
Behaviour from flow.md and the Figma description: typing fills left to right and focus auto-advances. When the 6th digit is entered it verifies (`000000` = correct). Wrong → Error state + the red message shown by the screen. Editing a digit clears the error.
Implementation suggestion (not in Figma): back the six boxes with one hidden text input (number pad, iOS `.textContentType(.oneTimeCode)`, Android `KeyboardType.NumberPassword` + SMS autofill); Backspace clears the last filled box; tapping the row focuses the input.

### 5.4 Control / Avatar Option (37:655): `PBAvatarOption`
Description: "Avatar picker option for setup. Selected = black ring. Swap the head with the Art property (any Art / Peep Head)."
**Properties**: `Art` instance swap (default Art / Peep Head / Arjun 7:5), `Type` = Art · Upload, `Selected` = True · False. Variants that exist: Art+True, Art+False, **Upload+False** (there's no Upload+True; a chosen photo becomes the selected Art-style circle, see below).
**Outer**: **56 × 56** circle (radius full), auto-layout centred, no fill.
| Variant | Outer ring | Inner (46 × 46 circle at (5, 5), clips) |
|---|---|---|
| Art, Selected=True | **2.5-pt inside** stroke `border/strong` #0A0A0A (leaves a 2.5-pt white gap around the inner circle) | fill `bg/card` #F5F5F5 + head art scaled to **46 × 46** (`assets/avatars/avatar-N.svg`) |
| Art, Selected=False | none | same |
| Upload, Selected=False | none | fill `bg/card` #F5F5F5 + **Icon / Camera 24 × 24** centred at (11, 11) in inner-circle coordinates (= (16, 16) in the 56 × 56 option, as screens-setup.md gives it), `icon/primary` #0A0A0A |
Setup 1 uses five Art options (Arjun, Priya, Rohan, Esha, Dev = `avatar-1…5`, presets 0–4) and the Upload option (camera → photo picker). Tap = select (single selection). When the user picks a photo, show it in the 46 circle (aspect-fill, clipped) with the 2.5-pt selected ring (the same visuals as Art+Selected).

### 5.5 Row / Currency (37:673): `PBCurrencyRow`
Description: "Currency option with radio."
**Properties**: `Symbol` text ("₹"), `Title` text ("Indian Rupee"), `Subtitle` text ("INR"), `Selected` = True · False.
**Layout**: H auto-layout, **362 × 56**, gap **12** (`space/12`), align start/center.
1. `symbol tile` **40 × 40** circle at (0, 8), fill `bg/card` #F5F5F5, clips; symbol centred, `Headline` (SemiBold 16/22, −0.25 % = −0.04 pt), `text/primary` #0A0A0A.
2. `text` column **fills** (276 wide) at (52, 6), V auto-layout gap **2** (`space/2`), clips: `title` `Headline` `text/primary` (276 × 22); `subtitle` `Subheadline` (Medium 14/20) `text/secondary` #6B6B6B (276 × 20). Truncate to one line each.
3. `radio` **22 × 22** circle at (340, 17):
   - Selected: fill `bg/inverse` #0A0A0A + **Icon / Check 14 × 14** (stroke 0.875) at (4, 4), `icon/inverse` #FFFFFF.
   - Not selected: no fill, **1.5-pt inside** stroke `bg/indicator` #D1D1D1.
The whole 56-pt row is the tap target; selection is single-choice. No pressed state is defined in Figma. Rows sit directly on white; any dividers/section headers belong to the screen spec (screens-setup.md).

---

## 6. Token cross-reference used above
| Token | Value | | Token | Value |
|---|---|---|---|---|
| bg/primary | #FFFFFF | | text/primary | #0A0A0A |
| bg/card | #F5F5F5 | | text/secondary | #6B6B6B |
| bg/card-pressed | #EBEBEB | | text/tertiary / text/disabled | #A3A3A3 |
| bg/selected | #0A0A0A @ 6 % | | text/inverse | #FFFFFF |
| bg/inverse | #0A0A0A | | text/destructive | #C93636 |
| bg/inverse-pressed | #2B2B2B | | icon/primary · secondary · tertiary · inverse · destructive | #0A0A0A · #6B6B6B · #A3A3A3 · #FFFFFF · #C93636 |
| bg/disabled | #E0E0E0 | | border/subtle | #EBEBEB |
| bg/destructive / -pressed | #C93636 / #A92E2E | | border/strong | #0A0A0A |
| bg/glass | #FFFFFF @ 72 % | | border/destructive | #C93636 |
| bg/indicator | #D1D1D1 | | border/glass-highlight | #FFFFFF @ 60 % |
| space/2 · 4 · 6 · 8 · 12 · 16 · 24 | 2 · 4 · 6 · 8 · 12 · 16 · 24 | | radius/input · card · full | 14 · 20 · 999 |
| size/tap · button-lg · button-sm · add-button | 44 · 52 · 36 · 52 | | size/avatar-xs · sm · md · lg | 24 · 32 · 40 · 56 |

Text styles used: Button/Large, Button/Small, Headline, Body, Subheadline, Footnote, Caption/1, Caption/2, Title/2, Title/3, Brand/Wordmark S, Brand/Wordmark L (values in tokens.md).
