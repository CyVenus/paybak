# Sign-in screens: `signIn`, `verify`, `verifyWrong`

Source: Figma file `2SPNUpHlG8bCO62YfwRuRi`, page "03 Launch & Onboarding" (3:4), section "Sign in & Setup" (39:353).

| Screen id | Figma frame | 2x reference (804×1748) | 1x reference | Node JSON (`.figma-cache/`, regenerate with `tools/fetch_figma.py`) |
|---|---|---|---|---|
| `signIn` | Sign in — Email or phone (39:356) | `ref/signIn.png` | `ref/signIn_1x.png` | `nodes/3-4.json` |
| `verify` | Sign in — Verify code (39:534) | `ref/verify.png` | `ref/verify_1x.png` | `nodes/3-4.json` |
| `verifyWrong` | Sign in — Wrong code (39:644) | `ref/verifyWrong.png` | `ref/verifyWrong_1x.png` | `nodes/3-4.json` |

`verify` and `verifyWrong` are **one screen** (one view/composable) with an `error` state. They are not two destinations.

Designer notes placed next to the frames on the canvas (verbatim):
- Section: "Email or phone → code → 4 setup steps → All set. Apple and Google sign-in skip the code and start at Step 1."
- 39:358 (next to signIn): "Email or phone in one field. Send code → Verify."
- 39:536 (next to verify): "6-digit code. Auto-verifies on the last digit; resend unlocks after 30 s."
- 39:646 (next to verifyWrong): "Wrong code: red rings and message (red is allowed for errors). Resend is available."

Conventions in this file:
- Coordinates are in pt/dp in the 402 × 874 frame: `(x, y, w, h)`. **safe-y** = y − 62, measured from the top safe-area edge. The bottom safe-area edge is at frame y = 840 (874 − 34).
- Colours are token names from `tokens.md`, with hex values. Text styles are the names in `tokens.md`. Letter spacing in pt: Title/1 −0.64, Title/2 −0.36, Button/Large −0.085, Button/Small −0.0375, and 0 for Body, Subheadline and Footnote.
- Status bar, keyboard and home indicator in the frames are iOS kit instances (system UI). **Do not draw them.**
- The `Hotspot — Back` frames (49:2900, 49:2903, 49:2906; 44×44 at (20, 62)) are invisible prototype hotspots over the back button. **Do not render them.** The back button itself handles taps.
- No Rive on these screens (none of them is listed in `rive.md`). No motion: `get_motion_context` returned no animated nodes for any of the three frames.

---

## 0. Shared building blocks (exact component data from page 02 Components)

### 0.1 Frame / layout shell (all three screens)
- Frame 402 × 874, fill `color/bg/primary` #FFFFFF, vertical auto-layout, padding top 62 (`layout/status-bar`), bottom 34 (`layout/home-indicator`), left/right 20 (`layout/screen-margin`). Children are top-aligned (`MIN`).
- A single `Content` column (x 20, w 362, fill width, hugs height) holds everything except the bottom CTA. **Content is top-anchored**: it starts at the top safe-area edge and does not move when the keyboard appears.
- In code: lay out from the safe area with 20 pt side margins. Don't hard-code the 62/34 insets.

### 0.2 Header: `Navigation / Onboarding Top Bar` (17:461), a simple nav header
**It is NOT `Navigation / Setup Header` (36:666, the one with the step progress), and NOT `Navigation / Push Header`.** It is the Onboarding Top Bar instance with properties `Show back = true`, `Show Skip = false`. Component description: "Onboarding top bar: optional back chevron, Skip on the right (slides 1–2 only). SwiftUI: PBOnboardingTopBar".
- Bar: (20, 62, 362, 44), safe-y 0. Horizontal auto-layout, no padding, children vertically centred: `back` + `spacer` (fill). Skip is hidden, so nothing is on the right.
- `back` = `Button / Icon`, Style=Plain, State=Default (10:47): (20, 62, 44, 44) (`size/tap`), corner radius `radius/full` (circle), no fill, content centred. Badge hidden.
  - Icon `Icon / Chevron Left` (5:32, "HugeIcons arrow-left-01 (stroke-rounded)"), 24 × 24 at (30, 72), which is a 10 pt inset. Stroke 1.5 pt, round caps and joins, `color/icon/primary` #0A0A0A. The drawn vector's bounds are 6 × 12 at (39, 78).
  - **Pressed** (Button / Icon, Style=Plain, State=Pressed, 10:51): the 44 × 44 circle is filled with `color/bg/selected` (#0A0A0A at 6 %). The icon doesn't change.
  - Accessibility label "Back".
- The frame gives no visible back affordance on the right, and there's no title.

### 0.3 `Control / Input Field` (12:296), used on signIn
Description: "52pt field on #F5F5F5, 14pt radius. Focused = 1.5pt black ring. Error = red ring + red helper. SwiftUI: PBTextField".
Structure: vertical stack, gap 8 (`space/8`), full width 362, total height 106:
1. `label`: Subheadline (Manrope Medium 14/20), height 20.
2. `field`: height 52 (`size/button-lg`), fill `color/bg/card` #F5F5F5, radius `radius/input` 14, horizontal padding 16 (`space/16`), gap 12 (`space/12`, used only if the leading icon is shown; it's hidden here), content vertically centred. Holds `value`, which is Body (Manrope Regular 16/24), fills the width, one line.
3. `helper`: Footnote (Manrope Medium 13/18), height 18.

| State | Field ring | Value text | Placeholder | Label | Helper |
|---|---|---|---|---|---|
| Default (empty, unfocused) | none | n/a | `color/text/tertiary` #A3A3A3 | `color/text/secondary` #6B6B6B | `color/text/tertiary` #A3A3A3 |
| **Focused** (as shown on signIn) | 1.5 pt **inside** stroke `color/border/strong` #0A0A0A | `color/text/primary` #0A0A0A | `color/text/tertiary` #A3A3A3 (when empty) | `color/text/secondary` | `color/text/tertiary` |
| Filled (unfocused, has value) | none | `color/text/primary` | n/a | `color/text/secondary` | `color/text/tertiary` |
| Error | 1.5 pt inside stroke `color/border/destructive` #C93636 | `color/text/primary` | `color/text/tertiary` | `color/text/secondary` | `color/text/destructive` #C93636 |
| Disabled | none | `color/text/disabled` #A3A3A3 | `color/text/disabled` | `color/text/disabled` | `color/text/disabled` |

- In the Default/Filled variants the value text starts 16 pt from the field edge. In the Focused/Error variants Figma counts the stroke in the layout, so the text starts at 17.5 pt. **Recommendation:** keep the text at 16 pt in every state and draw the 1.5 pt ring as an inside overlay (strokeBorder), so the text doesn't jump 1.5 pt when the field gains focus.
- The ring appears and disappears immediately (no animation is designed). A 0.15 s fade is acceptable.
- Caret (system text cursor) colour: `color/text/primary` #0A0A0A (iOS `.tint`, Android `cursorBrush = SolidColor(#0A0A0A)`). Not specified in Figma; this matches the black code-digit caret.

### 0.4 `Button / Primary`, Size=Large (9:36), used for "Send code"
Description: "Main action — black pill. One Large primary per screen. SwiftUI: PBButton(.primary)".
- Height 52 (`size/button-lg`), radius `radius/full` (pill), horizontal padding 24 (`space/24`), gap 8 (`space/8`, for the optional leading icon, which is hidden). Label is Button/Large (Manrope SemiBold 17/22, −0.085 pt), centred. Here the width is stretched to the full content width, 362.

| State | Fill | Label |
|---|---|---|
| Default (enabled) | `color/bg/inverse` #0A0A0A | `color/text/inverse` #FFFFFF |
| Pressed | `color/bg/inverse-pressed` #2B2B2B | `color/text/inverse` #FFFFFF |
| Disabled | `color/bg/disabled` #E0E0E0 | `color/text/disabled` #A3A3A3 |

### 0.5 `Button / Text` (10:45), used for "Resend code"
Description: "Text-only action (See all, Skip, Continue with email or phone). Pressed = 50% opacity. SwiftUI: PBTextButton".
- Height 44 (`size/tap`), horizontal auto-layout, gap 2 (`space/2`), no padding, content vertically centred, width hugs the label. Label is Button/Small (Manrope SemiBold 15/20, −0.0375 pt), sitting 12 pt below the button's top edge. The trailing chevron is hidden.
- The designs use **Style=Primary**, so the label is `color/text/primary` #0A0A0A.

| State | Look |
|---|---|
| Default | label `color/text/primary` #0A0A0A |
| Pressed | the whole button at 50 % opacity |
| Disabled | label `color/text/disabled` #A3A3A3 |

### 0.6 `Control / Code Digit` (36:675) and `Control / Code Input` (36:702)
Code Digit description: "One box of the 6-digit code. SwiftUI: PBCodeDigit". Code Input description: "6-digit code field. Auto-advances and verifies on the 6th digit. SwiftUI: PBCodeField".

**Code Digit box:** 48 × 56, radius `radius/input` 14, fill `color/bg/card` #F5F5F5, content centred.

| State | Stroke | Content |
|---|---|---|
| Empty (36:668) | none | nothing |
| Focused (36:669) | 1.5 pt **inside** `color/border/strong` #0A0A0A | **caret**: a 2 × 24 rectangle, fill `color/bg/inverse` #0A0A0A, square corners, centred (box-relative x 23, y 16) |
| Filled (36:671) | none | the digit in Title/2 (Manrope Bold 24/30, −0.36 pt), `color/text/primary` #0A0A0A, centred horizontally. The 30 pt line box sits 13 pt below the box top, which centres it vertically |
| Error (36:673) | 1.5 pt inside `color/border/destructive` #C93636 | the digit, same as Filled: Title/2, `color/text/primary` #0A0A0A. **The digit stays black. Only the ring is red.** |

- Caret blink: not designed (no motion data). Proposal: blink with a 1.0 s period (0.5 s visible, 0.5 s hidden), like a system caret. Don't blink under Reduce Motion.
- Box-state changes are instant in Figma. A 0.12 s ease-out cross-fade of the ring or digit is acceptable. Don't add scaling or shake effects (not designed).

**Code Input row:** horizontal, height 56, **fills the content width (362)**, `itemSpacing` 12 (`space/12`) with **space-between** distribution, items top-aligned. Variants: State=Typing (36:677) and State=Error (36:689).
- On a 402-wide screen: 6 × 48 = 288, so the actual gap is (362 − 288) / 5 = **14.8 pt**. Box x positions in the frame: **20, 82.8, 145.6, 208.4, 271.2, 334**.
- Proposal for narrow screens (not designed): keep the minimum gap at 12 and the height at 56, and shrink the box width to `min(48, (contentWidth − 60) / 6)`. For example, 43.3 pt on a 360 dp Android phone.

### 0.7 Keyboard rules (both platforms)
- The keyboard is up in all three frames. The field or code input **auto-focuses when the screen appears**, so the keyboard is already up.
- Content is always **top-anchored** and never moves with the keyboard.
- Only `signIn` has a bottom CTA. It **rides the keyboard**: see §1 for the numbers. Verify and Wrong code have no bottom CTA, so nothing reacts to the keyboard there.
- Android: edge-to-edge with `WindowInsets.ime` (for example `Modifier.imePadding()` on the CTA container only). iOS: SwiftUI's keyboard avoidance through `.safeAreaInset(edge: .bottom)`.

---

## 1. `signIn`: Sign in — Email or phone (39:356)

**Purpose:** collect an email address or phone number in one field, then send a 6-digit code. Background `color/bg/primary` #FFFFFF.
**Keyboard in the frame:** iOS kit `Keyboard`, Type=Email, no suggestion bar. It covers (0, 567, 402, 307), i.e. from y 567 to the frame bottom, including the home-indicator zone.

### Elements, top to bottom

| # | Element | Frame (x, y, w, h) | safe-y | Spec |
|---|---|---|---|---|
| 1 | Top bar | (20, 62, 362, 44) | 0 | §0.2: Onboarding Top Bar, back chevron only |
| — | gap | y 106 → 130 | | 24 (`space/24`) |
| 2 | Headline | (20, 130, 362, 76) | 68 | "What’s your email or phone?" (U+2019 apostrophe). **Title/1** (Manrope ExtraBold 32/38, −0.64 pt), `color/text/primary` #0A0A0A, left-aligned, fills the width. Wraps to 2 lines: "What’s your email or" / "phone?" |
| — | gap | y 206 → 218 | | 12 (`space/12`, the text stack's itemSpacing) |
| 3 | Body | (20, 218, 362, 24) | 156 | "We’ll send a 6-digit code. No password needed." (U+2019). **Body** (Manrope Regular 16/24), `color/text/secondary` #6B6B6B, left-aligned, fills the width, 1 line |
| — | gap | y 242 → 266 | | 24 (`space/24`) |
| 4 | Email or phone field | (20, 266, 362, 106) | 204 | `Control / Input Field`, **State=Focused** (§0.3). Props: Show label = true, Show helper = true, Leading icon = false |
| 4a | label | (20, 266, hug ≈150, 20) | 204 | "Email or phone number". Subheadline, `color/text/secondary` #6B6B6B |
| 4b | field | (20, 294, 362, 52) | 232 | `color/bg/card` #F5F5F5, radius 14, focused ring 1.5 pt inside `color/border/strong` #0A0A0A |
| 4c | value | (37.5, 308, 327, 24) | 246 | Value in the frame: "arjun@example.com" (the typed text). Body, `color/text/primary` #0A0A0A. Keep a 16 pt inset in code (§0.3 note) |
| 4d | helper | (20, 354, hug ≈181, 18) | 292 | "We’ll only use it to sign you in." (U+2019). Footnote (Manrope Medium 13/18), `color/text/tertiary` #A3A3A3 |
| — | free space | y 372 → 503 | | Flexible. Not a fixed gap |
| 5 | CTA "Send code" | (20, 503, 362, 52) | 441 | `Button / Primary`, Size=Large (§0.4). Label "Send code" |
| — | keyboard | y 567 → 874 | | system |

**CTA position (keyboard layout):** in Figma the CTA is absolutely positioned and pinned to the bottom with stretched width. Its bottom edge (y 555) is **exactly 12 pt above the keyboard's top edge** (y 567).
- Keyboard visible: CTA bottom = keyboard top − 12 pt. Horizontal insets are 20/20.
- Keyboard hidden (not designed): put the CTA's bottom on the bottom safe-area edge (frame y 788–840). This matches Setup 2, Setup 3 and All set, where the CTA is also at y 788 with a 34 pt bottom gap. Setup 1 (42:665) uses the same "12 pt above the keyboard" rule as this screen.
- iOS: `.safeAreaInset(edge: .bottom) { SendCode.padding(.horizontal, 20).padding(.bottom, keyboardVisible ? 12 : 0) }`. Android: CTA container with `.imePadding()` or `.windowInsetsPadding(WindowInsets.ime.union(WindowInsets.navigationBars))`, plus 12 dp while the IME is visible.

### Field behaviour
- **Label:** "Email or phone number".
- **Placeholder:** the frame only shows a typed value, and no placeholder is set on this instance. The only placeholder copy in the file is the component default, "you@example.com". Use that, in `color/text/tertiary` #A3A3A3, Body. (Open item: see the notes at the end.)
- **Helper:** "We’ll only use it to sign you in." is always shown.
- **Focus:** auto-focus on appear. The ring is shown while focused, whether the field is empty or has text.
- **Keyboard type:** iOS `.keyboardType(.emailAddress)`, `.textContentType(.username)`, `.textInputAutocapitalization(.never)`, `.autocorrectionDisabled()`, `.submitLabel(.send)`. Android `KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Send, autoCorrectEnabled = false, capitalization = KeyboardCapitalization.None)`. The IME action or Return key triggers Send code, but only when it's enabled.
- **Single line.** Long input scrolls horizontally inside the field.
- **Error state:** it exists in the component, but **no error copy is designed for this screen**, so don't use it. Invalid input just keeps Send code disabled.
- Figma annotation on the field (verbatim): "One smart field: detects email or phone; phone numbers get +91."

### "Send code" enabled / disabled (from flow.md)
- **Enabled** (Default look: black pill, white label) when the trimmed input is:
  - a plausible email (`x@y.z`: something @ something . something, no spaces), or
  - a plausible phone number: at least 7 digits, optional leading `+`, spaces and dashes allowed, no other characters.
- **Disabled** (look: `color/bg/disabled` #E0E0E0 pill, label `color/text/disabled` #A3A3A3) otherwise, including an empty field. Disabled buttons don't respond to taps.
- **Pressed:** `color/bg/inverse-pressed` #2B2B2B.
- **On tap:** normalise the contact, then push `verify`. The Verify screen starts a new 30 s resend timer.
  - Email: trim it.
  - Phone (per the "+91" annotation; exact formatting is a proposal): if it starts with `+`, keep it as typed (trimmed). Otherwise prefix `+91 `. For example, "98765 43210" becomes "+91 98765 43210".
  - Store the sign-in method (`email`/`phone`) and the contact in the profile (see flow.md Persistence).

### Interactions (prototype, read from `node.reactions`)
| Trigger | Figma action | App behaviour |
|---|---|---|
| Tap "Send code" (39:389) | Navigate to Sign in — Verify code (39:534), **Push, direction LEFT, 350 ms, ease-in-out** | Push `verify` (platform push: the new screen slides in from the right) |
| Tap back (hotspot 49:2900) | Navigate to Get Started (22:272), **Push, direction RIGHT, 350 ms, ease-in-out** | Pop to Get Started. Android system back does the same |

### Assets used
- `Icon / Chevron Left` (component 5:32). See "Assets" at the end for the file name and the SVG.

---

## 2. `verify`: Sign in — Verify code (39:534)

**Purpose:** enter the 6-digit code that was sent to the contact. Background `color/bg/primary` #FFFFFF.
**Keyboard in the frame:** iOS kit `Keyboard`, Type=**Number Pad**, with the suggestions strip. It covers (0, 566, 402, 308).

### Elements, top to bottom

| # | Element | Frame (x, y, w, h) | safe-y | Spec |
|---|---|---|---|---|
| 1 | Top bar | (20, 62, 362, 44) | 0 | §0.2: Onboarding Top Bar, back chevron only |
| — | gap | y 106 → 130 | | 24 (`space/24`) |
| 2 | Headline | (20, 130, 362, 38) | 68 | "Enter the code". **Title/1**, `color/text/primary` #0A0A0A, 1 line |
| — | gap | y 168 → 180 | | 12 (`space/12`) |
| 3 | Sent-to line | (20, 180, 362, 24) | 118 | Verbatim: "Sent to arjun@example.com · Change". The character before "Change" is a space, a **U+00B7 MIDDLE DOT**, and a space. Text style **Body** (Regular 16/24) for the whole run. Two segments: **"Sent to {contact} · "** in `color/text/secondary` #6B6B6B with no decoration, then **"Change"** in `color/text/primary` #0A0A0A with **underline** (single, solid, font's default underline position and thickness). The line wraps if the contact is long (fixed width, height grows). "Change" stays inline at the end |
| — | gap | y 204 → 236 | | **32** (`space/32`) |
| 4 | Code input | (20, 236, 362, 56) | 174 | `Control / Code Input`, State=Typing (§0.6). Boxes at x 20 / 82.8 / 145.6 / 208.4 / 271.2 / 334, each 48 × 56. In the frame: boxes 1–4 **Filled** with "4", "8", "2", "9"; box 5 **Focused** (black ring and caret; caret at (294.2, 252, 2, 24)); box 6 **Empty**. Figma annotation (verbatim): "Verifies automatically on the 6th digit." |
| — | gap | y 292 → 308 | | 16 (`space/16`) |
| 5 | Resend countdown | (20, 308, 362, 18) | 246 | "Resend code in 0:24" (as shown). **Footnote** (Medium 13/18), `color/text/tertiary` #A3A3A3, left-aligned, fills the width. Plain text, not a button and not tappable |
| — | free space | y 326 → keyboard | | Nothing else. No bottom CTA |

### The "Change" link
- Style as above: Body, `color/text/primary`, underlined. It isn't a separate component.
- Figma has no reaction on it, because it's a text segment. flow.md says **Change goes back to Sign in**: pop to `signIn` with the field pre-filled with what the user typed and focused.
- Hit area: make "Change" tappable with at least a 44 pt tall target. Pad it invisibly around the word; don't change the visible layout.
  - iOS: build the line as `Text` concatenation plus an overlay button, or use `AttributedString` with a link and a custom `OpenURLAction`.
  - Android: `buildAnnotatedString` with `LinkAnnotation.Clickable`.
- Pressed state (not designed): proposal is 50 % opacity on "Change", the same as Button / Text.

### Resend: countdown and enabled variants
- **Countdown (designed, this frame):** "Resend code in 0:SS", Footnote, `color/text/tertiary` #A3A3A3. Format `m:ss`. It starts at **"Resend code in 0:30"** as soon as the screen appears (the code has just been sent), and ticks every second down to "Resend code in 0:01".
- **Unlocked (at 0:00):** the countdown text is replaced by the `Button / Text` Style=Primary "Resend code" (§0.5, designed in `verifyWrong`). Placement when there's no error is not designed. Proposal: code row, then a **4 pt** gap, then the 44 pt button at (20, 296, hug ≈92, 44). Its label then sits at y 308, the same top as the countdown text, so the swap doesn't shift anything visually.
- **Tapping Resend code:** clear all six boxes, clear any error, restart the 30 s countdown, keep focus in box 1 (keyboard stays up). There's no backend, so nothing is sent.

### Verify behaviour (from flow.md and annotations)
- **One hidden input** drives the six boxes.
  - iOS: a `TextField` with `.keyboardType(.numberPad)` and `.textContentType(.oneTimeCode)`, so SMS code AutoFill appears in the suggestions strip.
  - Android: `BasicTextField` with `KeyboardOptions(keyboardType = KeyboardType.NumberPassword)` (no masking) and the SMS-OTP autofill content type.
  - Accept digits only, up to 6. Pasting a 6-digit string fills all boxes.
- **Tap targets:** tapping anywhere on the code row focuses the input and shows the keyboard. There's no per-box cursor placement; input always appends or deletes at the end.
- **Box state for index i** (0-based), with `n` = digits entered:
  - error → **Error**, showing the digit.
  - i < n → **Filled**.
  - i == n, the input is focused and n < 6 → **Focused**, with ring and caret.
  - otherwise → **Empty**.
  - If the keyboard is dismissed, no box is Focused.
- **Auto-verify when the 6th digit arrives.** No button. Proposal: wait about 250 ms first so the 6th digit visibly fills.
  - `000000` is correct. Go to **Setup step 1** (`setup1`), and push it onto the stack so Setup 1's back returns here.
  - Any other code shows the **wrong-code state** (§3).
- The Figma prototype stands in for auto-verify with a **frame-level `AFTER_TIMEOUT` of 2000 ms**: navigate to Setup 1 — Name & photo (42:665), **Dissolve 300 ms ease-in-out**. The 2 s delay is a prototype artefact; don't copy it. Push navigation is recommended because Setup 1 needs Back. If you want to match Figma's look, use a 300 ms cross-fade.
- **Back** (chevron or system back) pops to `signIn`, keeping the contact in the field. Figma hotspot 49:2903: Navigate to Sign in — Email or phone (39:356), **Push RIGHT 350 ms ease-in-out**.
- Accessibility: expose the code input as one text field labelled "Verification code, 6 digits" with its current value. The countdown is a live region, but announce it only every 10 s or at 0 to avoid chatter.

### Assets used
- `Icon / Chevron Left` (5:32).

---

## 3. `verifyWrong`: Sign in — Wrong code (39:644)

This is the same screen as `verify` in its **error** state. Everything above the code row is identical, pixel for pixel: top bar, headline "Enter the code", and the sent-to line with its segments and underline. Keyboard: Number Pad, (0, 566, 402, 308).

### Exact differences from `verify`

| Area | `verify` (39:534) | `verifyWrong` (39:644) |
|---|---|---|
| Code input variant | `Control / Code Input` State=Typing | `Control / Code Input` **State=Error** |
| Boxes | 4 Filled ("4","8","2","9") + 1 Focused (caret) + 1 Empty | **all 6 = Code Digit State=Error**: "4","8","2","9","1","7". Each has a 1.5 pt inside ring `color/border/destructive` #C93636, fill `color/bg/card` #F5F5F5, digit Title/2 `color/text/primary` #0A0A0A. **No caret, no Focused box** |
| Below the row (gap 16, y 292 → 308) | "Resend code in 0:24" (Footnote, `color/text/tertiary`) at (20, 308, 362, 18) | **Error message** at (20, 308, 362, 18): "That code didn’t match. Check it and try again." (U+2019 apostrophe). **Footnote** (Medium 13/18), **`color/text/destructive` #C93636**, left-aligned, fills the width |
| Then | nothing | gap **4** (`space/4`), y 326 → 330. Then **"Resend code"**: `Button / Text`, Style=**Primary**, State=Default at (20, 330, 92, 44), hugging its width. Label "Resend code", Button/Small (SemiBold 15/20, −0.0375 pt), `color/text/primary` #0A0A0A, at (20, 342, 92, 20). Pressed = 50 % opacity |
| Content bottom | y 326 | y 374 |
| Frame-level reaction | AFTER_TIMEOUT 2000 ms → Setup 1 (Dissolve 300 ms) | none |

In this frame the countdown has finished, so Resend is shown as enabled ("Resend is available" per the designer note).

### Interactions (prototype)
| Trigger | Figma action | App behaviour |
|---|---|---|
| Tap "Resend code" (39:683) | Navigate to Sign in — Verify code (39:534), Push LEFT 350 ms ease-in-out | **Don't navigate.** Reset in place: clear boxes, clear error, restart the 30 s countdown, keep focus |
| Tap back (hotspot 49:2906) | Navigate to Sign in — Email or phone (39:356), Push RIGHT 350 ms ease-in-out | Pop to `signIn` |

### Leaving the error state (flow.md: "Editing a digit clears the error")
- **Backspace:** delete the last digit and clear the error. The boxes go back to Filled ×5 plus a Focused box 6.
- **Typing a digit while all 6 are shown in error:** proposal. Clear the whole code, put the new digit in box 1 and clear the error. The user starts over; there's no "replace the 6th digit" behaviour.
- **Resend code:** as above.
- Proposal, not designed: fire an error haptic when the error appears (iOS `.sensoryFeedback(.error, trigger:)`, Android `HapticFeedbackType.Reject`, or `performHapticFeedback(REJECT)` on API 30+). Nothing else is animated.

### Error plus a running countdown (not designed; proposal)
If a wrong code is entered before 30 s have passed, show: code row → 16 → the error message (y 308–326) → **16** → the countdown text "Resend code in 0:SS" (Footnote, `color/text/tertiary`) at (20, 342, 362, 18). Its top then lines up with where the "Resend code" label sits in the designed error frame (y 342). When the countdown reaches 0, swap it for the designed 4 pt gap + 44 pt "Resend code" button.

**Summary of the resend-row layouts** (y values assume one-line error and contact texts):

| error | timer | Below the code row (row ends at y 292) |
|---|---|---|
| no | running | 16 gap → countdown text at y 308 **(designed)** |
| no | done | 4 gap → Resend button, 44 tall, at y 296 (label at y 308) *(proposal)* |
| yes | done | 16 gap → error text at y 308 → 4 gap → Resend button at y 330 (label at y 342) **(designed)** |
| yes | running | 16 gap → error text at y 308 → 16 gap → countdown text at y 342 *(proposal)* |

### Assets used
- `Icon / Chevron Left` (5:32).

---

## 4. Navigation summary for these screens

| From | Action | To | Figma transition |
|---|---|---|---|
| Get Started | "Continue with email or phone" | `signIn` | (defined on Get Started) |
| `signIn` | back | Get Started (pop) | Push RIGHT, 350 ms, ease-in-out |
| `signIn` | Send code (enabled) | `verify` (push) | Push LEFT, 350 ms, ease-in-out |
| `verify` / `verifyWrong` | back or "Change" | `signIn` (pop, contact kept) | Push RIGHT, 350 ms, ease-in-out |
| `verify` | 6th digit, code `000000` | `setup1` (push) | Figma: Dissolve 300 ms ease-in-out after a 2000 ms timeout (prototype stand-in) |
| `verify` | 6th digit, other code | same screen, error state | none (instant) |
| `verifyWrong` | Resend code | same screen, reset | Figma: Push LEFT 350 ms (use an in-place reset instead) |

Use the platform's standard push/pop: iOS `NavigationStack` with the system nav bar hidden (keep the interactive swipe-back), Android slide-in/out over 350 ms with ease-in-out. System back on Android equals the in-app chevron.

Debug start screens: `-startScreen signIn|verify|verifyWrong` (iOS) and `--es startScreen …` (Android).
- `verify`: seed the contact as "arjun@example.com" so the sent-to line matches Figma.
- `verifyWrong`: open the screen in the error state with the digits "482917" and the countdown finished, so it matches the reference PNG.

---

## Assets

| Asset | Where it's used | File |
|---|---|---|
| `Icon / Chevron Left` (component 5:32, HugeIcons arrow-left-01 stroke-rounded, 24 × 24) | Back button in the Onboarding Top Bar on all three screens | **`assets/icons/chevron-left.svg`** (component-icon export, verified present). SwiftUI name per the component description: `PBIcon.chevronLeft` |

SVG exported from Figma (`exportAsync SVG_STRING` of 5:32), for reference or as a fallback:
```svg
<svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
<path d="M15 6C15 6 9 10.419 9 12C9 13.581 15 18 15 18" stroke="#0A0A0A" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
</svg>
```
Tint it with `color/icon/primary` #0A0A0A.

No other images, illustrations or Rive files appear on these screens, and nothing was exported to `assets/images/`. The status bar, keyboard and home indicator are system UI.

---

## Open items and proposals (not in Figma)
1. **Placeholder** for the email/phone field: not set on the instance. The spec uses the component default, "you@example.com". Change it if product wants something phone-friendly.
2. **Phone formatting** for "Sent to …": the "+91" annotation defines the prefix only. The rule proposed here is "+91 " + the input as typed when there's no leading "+".
3. **Resend states that aren't designed:** unlocked without an error, and error while the timer is still running. The layouts above keep the label's top aligned with the designed positions.
4. **Typing after an error:** start over from box 1 (proposal). Backspace deletes the last digit and clears the error (per flow.md "editing clears the error").
5. **Verify delay:** 250 ms before checking the code (proposal). Figma's 2 s timeout is only a prototype device.
6. **Caret blink:** 1 s period (proposal). The design shows a static caret.
7. **Keyboard-hidden CTA position on signIn:** bottom safe-area edge (from Setup 2/3 and All set). 12 pt above the keyboard when it's visible (designed).
