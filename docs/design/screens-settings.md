# Paybak: Settings & Pro screens spec (Figma page "12 Settings & Pro", 77:106)

Sections on the page (all read-only from Figma file `2SPNUpHlG8bCO62YfwRuRi`):
Paybak Pro (167:12467) · Payment details (167:12470) · Preferences (176:17770) · Privacy & data (176:19717) · Help (177:24770).
There are **no "Overlay helpers" / "↳ … (overlay)" prototype-only frames** on this page. The sheet (Add UPI ID) and the alert (Delete account — Blocked) are drawn *inside* full frames over their parent screen.

| Screen id (debug `startScreen`) | Figma frame (node) | Container | Spec § | 2× reference | Node JSON (`.figma-cache/`) |
|---|---|---|---|---|---|
| `paywall` | Paybak Pro — Paywall (167:13003) | full-screen modal (slides up) | §2 | `ref/paywall.png` | `nodes/77-106.json` |
| `proWelcome` | Paybak Pro — Welcome (167:13888) | same modal, next step (dissolve) | §3 | `ref/proWelcome.png` | `nodes/77-106.json` |
| `paymentDetails` | Payment details (167:14684) | pushed screen (from Profile) | §4 | `ref/paymentDetails.png` | `nodes/77-106.json` |
| `paymentAddUpi` | Add UPI ID (167:15962) | Medium sheet over Payment details, keyboard up | §5 | `ref/paymentAddUpi.png` | `nodes/77-106.json` |
| `paymentAddUpiError` | Add UPI ID — Error (167:18527) | state of §5 | §5.6 | `ref/paymentAddUpiError.png` | `nodes/77-106.json` |
| `settingsCurrency` | Currency (176:17773) | pushed screen (from Profile) | §6 | `ref/settingsCurrency.png` | `nodes/77-106.json` |
| `settingsNotifications` | Notifications & reminders (176:18371) | pushed screen, scrolls | §7 | `ref/settingsNotifications.png` | `nodes/77-106.json` |
| `privacyData` | Privacy & data (176:19720) | pushed screen (from Profile) | §8 | `ref/privacyData.png` | `nodes/77-106.json` |
| `privacyExport` | Export records (176:20773) | pushed screen (Pro) | §9 | `ref/privacyExport.png` | `nodes/77-106.json` |
| `privacyDeleteBlocked` | Delete account — Blocked (177:24246) | alert over Privacy & data | §10 | `ref/privacyDeleteBlocked.png` | `nodes/77-106.json` |
| `helpFeedback` | Help & feedback (177:24773) | pushed screen (from Profile) | §11 | `ref/helpFeedback.png` | `nodes/77-106.json` |

Read with: `tokens.md` (tokens/text styles), `components-core.md` and `components-home.md` (existing components), `flow.md` (FULL APP SCOPE: Pro is a mock entitlement, exports use the share sheet, data is local). §1 below specs every component that is **new** on this page (Row / Setting, Push Header, Modal Header, Card / Plan, Category Chip, Alert, kit Toggle), because the implementers of this page need them and they are not in the existing component specs.

Source of every number: a read-only Plugin-API dump of all 11 frames (geometry, auto-layout, bound tokens, text styles, component variants/properties, reactions), in two parts, dump A (Pro + Payment) and dump B (Preferences + Privacy + Help). The dumps weren't kept; the same node data is in `.figma-cache/nodes/77-106.json` (regenerate with `tools/fetch_figma.py`). Format of the dumps, for reading the values quoted below: `TYPE "name"[id] x,y,WxH` in **frame coordinates**, `<Component{Variant}> {props}`, `|H/V gap pTop,Right,Bottom,Left main/cross|`, `sXY` = horizontal/vertical sizing, first letter of FILL / HUG / FIXED (so `F` is ambiguous between FILL and FIXED; the widths and this spec disambiguate), `f:` fill token, `s:` stroke, `r` radius, `fx:` effect, text = `Style c:colour "characters"`, `>>{…}` = prototype reactions.

---

## 0. Conventions (same as screens-home.md)

- Frame 402 × 874 pt (iPhone 17 Pro). All `x, y` are **frame coordinates**. Top safe area 62, bottom 34 (bottom safe-area edge y 840). Status bar (0–62) and home indicator (840–874) are kit instances: **system UI, don't draw them**. The iOS keyboard (kit "Keyboard (kit) — Email", 402 × 307 at y 567) is system UI too.
- Every frame is a vertical auto-layout column: padding **62 / 20 / 34 / 20**, gap **24** (`layout/section-gap`) between sections (the paywall uses gap 20; the Welcome frame uses gap 0 with its own inner gaps). Background `bg/primary` #FFFFFF.
- Colours are tokens.md names (the `color/` prefix dropped) with hex. Text styles are tokens.md names. Icons are `assets/icons/<name>.svg` (24-grid, scale the SVG so the stroke scales: 24 → 1.5, 20 → 1.25, 16 → 1.0, 14 → 0.875).
- Copy is **verbatim**: curly ’ (U+2019) in "You’re", "don’t", "can’t", "friends’", "person’s"; `·` U+00B7; `–` U+2013 in "1 Sep – 30 Sep 2026" (with spaces); `···· 4821` is four U+00B7 middle dots; ₹ U+20B9; − U+2212 only where noted. The alert message uses plain "₹1,850" / "₹2,900" (no sign).
- "Section" = `Row / Section Header` (components-home.md §8: 362 × 32, Title/3) + content, stacked with gap **8**. A **section footer** (Footnote, `text/secondary`, left-aligned, fill width, wraps) sits 8 below the card.
- "Settings card" = a frame with fill `bg/card` #F5F5F5, radius **20** (`radius/card`), clip, vertical, gap 0, no padding, containing `Row / Setting` rows (§1.1) at fill width (362).
- Pressed states are not drawn in Figma. Use the README rules: rows/cards → `bg/card-pressed` #EBEBEB while pressed; text buttons → 50 % opacity; pill buttons → their Pressed fill.
- Every pushed screen uses `Navigation / Push Header` (§1.2) with the glass back button. Back = pop (iOS edge swipe / Android system back do the same).

---

## 1. Components used on this page

### Reuse map (existing specs)
| Figma component | Where | Spec |
|---|---|---|
| Row / Section Header (13:223) | every section title; "Groups" with action "Select all" | components-home.md §8 |
| Sheet / Action Row (17:641) | paywall feature list (Show chevron = false, height 56, padding 0); payment method rows (height 64, padding 0) | components-home.md §13 (geometry overrides in §2 / §4) |
| Sheet / Container, Detent=Medium (118:1017) | "Add payment method" sheet | components-home.md §15 |
| Button / Primary Large (9:36) | Start 7-day free trial, Done, Save, Export | components-core.md §2.1 |
| Button / Primary Small, Button / Secondary Small | alert actions | components-core.md §2.1 |
| Button / Text, Style=Secondary (10:45) | paywall links (label overridden to Footnote / `text/tertiary`, 18 tall); "Select all" | components-core.md §2.2 |
| Button / Icon, Style=Glass (10:79) | back button (inside Push Header) | components-core.md §2.3 |
| Button / Icon, Style=Plain + Icon / Copy | payment preview copy | components-core.md §2.3 |
| Badge / Pill: On Card "Save 33%", Inverse "Pro" | plan card, Export records row | components-core.md §3.1 |
| Avatar / Circle 40 Art (Arjun) | payment preview | components-core.md §3.2 (`assets/avatars/avatar-1.svg`, circle fill `bg/primary` on the card) |
| Control / Segmented Options=2 (12:249) | "UPI ID / Bank account", "PDF / CSV" | components-core.md §4.3 (stretched: see below) |
| Control / Input Field (12:296), Leading icon = Wallet | UPI ID field (Focused / Error) | components-core.md §4.4 |
| Divider / Line, Inset=Leading (12:302) | row dividers inside Row / Setting | components-core.md §4.5 (inset overridden, §1.1) |
| Row / Currency (37:673), Selected=True | default currency | components-core.md §5.5 (on a card: tile fill `bg/primary`) |
| Card / Payment Preview (37:675) | "What friends see" | screens-setup.md (setup3) + §4 item 4 below |
| Brand / App Mark Size=96 (8:13) | paywall hero | components-core.md §1.1 (`assets/brand/app-mark.svg` / `app-mark-96.svg`) |
| Illustration / All set | Pro Welcome | rive.md → **`paybak-allset.riv`** (see §3, Content › Illustration) |

Segmented stretch: the component is 240 wide for 2 options; on this page the instances are FILL: **354** in the sheet (segments 174 × 30 at x 3 / 177) and **362** on Export (segments 178 × 30 at x 3 / 181). Rule: segment width = (container − 6) / n.

### 1.1 Row / Setting (97:996), NEW. SwiftUI `PBSettingRow`
Figma description (verbatim): “Settings list row for #F5F5F5 card groups, 56 tall (grows with a subtitle). Trailing: Chevron (push), Toggle (exposed kit switch; On track tinted color/bg/inverse), Stepper (exposed kit stepper), Check (black tick, single select), Unchecked (empty 24 slot), None. Tone=Destructive: text/destructive + icon/destructive, never a chevron. Optional icon (Show icon off moves the title and the divider inset to 16), value, subtitle, and an exposed Badge / Pill (Inverse “Pro” / “Try free”). Turn Show value off on Toggle, Stepper, Check and Unchecked rows. SwiftUI: PBSettingRow”

Properties: `Title` (text, default "Payment details") · `Value` (text, "UPI") · `Show value` (bool, true) · `Icon` (instance swap, default Icon / Wallet) · `Show icon` (true) · `Subtitle` (text, "Paid back in parts") · `Show subtitle` (false) · `Show badge` (false) · `Show divider` (true) · `Trailing` = Chevron | Toggle | Stepper | Check | Unchecked | None · `Tone` = Default | Destructive.

Geometry (row-relative; the row is always FILL width = 362 inside a settings card):
- Horizontal auto-layout, padding **12 top / 16 right / 12 bottom / 16 left**, gap **12**, items centred vertically. **Min height 56**; hugs taller content (84 with a 2-line subtitle, 68 with a 2-line title). No fill of its own (the card is `bg/card`).
- `icon` (when Show icon): 24 × 24 at (16, 16), `icon/primary` #0A0A0A (Destructive: `icon/destructive` #C93636).
- `text` column: fills the remaining width, vertical gap 2. Starts at x **52** with an icon, x **16** without.
  - `title`: **Headline** (SemiBold 16/22), `text/primary` (Destructive: `text/destructive` #C93636). One line, truncate at the end; rows whose title needs two lines wrap to 2 lines (Help §11 does this at 262 wide).
  - `subtitle` (Show subtitle): **Footnote** (Medium 13/18), `text/secondary` #6B6B6B, wraps.
- `value` (Show value): **Body** (Regular 16/24), `text/secondary` #6B6B6B, hug, right before the chevron (12 gap).
- `badge` (Show badge): Badge / Pill Style=Inverse, label "Pro" (40 × 24; fill `bg/inverse`, label Caption/1 `text/inverse`), right before the chevron.
- Trailing slot (right edge at row x 346 = 16 from the right):
  | Trailing | Drawn | Size / position (row-relative) | Colour |
  |---|---|---|---|
  | Chevron | Icon / Chevron Right | 20 × 20 at (326, 18) | `icon/tertiary` #A3A3A3 (verified on the render) |
  | Toggle | kit "Toggle - Switch" (78:1085) | 64 × 28 at (282, 14) | On: track `bg/inverse` #0A0A0A, knob white 38 × 24 at the right (x 306); Off: track kit "Labels/Tertiary" at 30 % (renders **#BEBEC0** on the #F5F5F5 card, measured), knob at the left (x 284) |
  | Check | Icon / Check | 24 × 24 at (322, 16) | `icon/primary` #0A0A0A ("black tick") |
  | Unchecked | empty 24 × 24 slot | (322, 16) | – |
  | Stepper | kit stepper | not used on this page | – |
  | None | nothing | – | – |
  Destructive rows never show a chevron (Trailing=None).
- `divider row` (Show divider): absolutely positioned at the row's bottom (y = height − 1), 362 × 1: a `Divider / Line` hairline `border/subtle` #EBEBEB from x **52** to 362 with an icon (310 wide), from x **16** with no icon (346 wide). The last row of every card has Show divider = false.

Behaviour: the whole row is the tap target. Chevron rows push; Toggle rows flip the switch (tap anywhere on the row, and the switch itself); Check / Unchecked rows toggle their check (multi-select on this page; see each screen); None rows perform their action (Delete account).
- iOS: a `Button` row with `PBSettingRow` content; Toggle = SwiftUI `Toggle` with `.tint(Color("bg/inverse"))` and `.labelsHidden()` (the system iOS 26+ switch is the 64 × 28 capsule with the pill knob that the kit draws). Don't draw the switch yourself on iOS.
- Android: draw the switch to match the kit: 64 × 28 capsule, On = #0A0A0A track + white 38 × 24 pill knob inset 2 at the right; Off = track #BEBEC0 (the kit's 30 % label gray as rendered on the #F5F5F5 card; use it opaque) + knob at the left; knob shadow: none visible at this size; animate the knob 200 ms (proposal). Use `Modifier.toggleable` on the whole row with `Role.Switch`.

### 1.2 Navigation / Push Header (97:1082), NEW. SwiftUI `PBPushHeader`
Description (verbatim): “Pushed-screen header, 44 tall: glass back button, centred Headline title (Show title), and a trailing action. Trailing=Text: glass capsule text action (Save). Trailing=Icon: exposed glass Button / Icon (default Settings; gear on 10-01). Trailing=Wide Text: for long text actions (“Mark all read”): the capsule has 12 side padding and a 122 max width, and the centred title box is 102 wide and truncates, so at least 8 pt always separates title and action. Trailing=None: back only. Tall scrolling frames set the instance to fix position and give it a color/bg/primary fill. Built with .navigationTitle and a toolbar. SwiftUI: PBPushHeader”

On this page every instance is **Trailing=None** (back only; the `Action` text prop "Save" is unused). Geometry (frame coords, x 20 y 62, 362 × 44, horizontal, space-between, centred):
- `Back`: Button / Icon Style=Glass, 44 × 44 at (20, 62), Icon / Chevron Left 24 at (30, 72), `icon/primary`. Glass = Material/Glass Small (components-core.md §2.3).
- `title`: absolutely positioned text box (101, 73) 200 × 22, **Headline**, `text/primary`, centred, one line (truncate).
- iOS: `.navigationTitle(title)` + `.navigationBarTitleDisplayMode(.inline)` with a custom glass back button (hide the system back button) or the system toolbar with Liquid Glass. Android: a 44-tall Box: back button left, title centred.
- Scrolling frames (only Notifications on this page): the header is fixed and a `bg/primary` rectangle 402 × 106 (y 0–106) sits behind it (see §7).

### 1.3 Navigation / Modal Header (115:886), NEW. SwiftUI `PBModalHeader`
Description (verbatim): “PBModalHeader — Toolbar for full-screen modals (Add expense, Record payment, Lend money, New group, Ask Paybak, paywall): the kit glass xmark (cancel) on the LEFT, a centred Headline title and the confirmation pill on the right.
Action=Enabled (black Button / Primary Small) · Disabled (until the form is valid) · None (Ask Paybak, paywall). The pill is the exposed nested instance “action”: set its Label#9:0 to Save / Create / Add. 362×44, placed at y 62 inside the 20pt margins. No fill, no shadow; sheets keep their xmark on the right.
SwiftUI: PBModalHeader”

Paywall instance: **Action=None, Show title=false** → only the close button:
- `close`: kit "Button - Liquid Glass - Symbol" Style=Glass, **44 × 44** at (20, 62) (the sheet close is 50 × 50; this one is 44). Glyph = SF Symbol `xmark` (U+100184), **Semibold 19 pt**, kit colour "Labels - Vibrant - Controls/Primary" (renders ≈ #1A1A1A), glyph box 26 × 22 centred. Kit glass layers are the same as the sheet ✕ (components-home.md §14): fills #000 25 % + #FFF 25 % + #444 60 % (linear dodge) + #F8F8F8 20 % (luminosity); shadows (0, 8) blur 15 black 2 %, 0.5 ring #E8E8E8, side hairlines ±1.25 #D0D0D0; GLASS 6. On white it reads as a white disc with a light hairline and soft shadow.
- iOS: a toolbar `Button(role: .cancel) { dismiss() } label: { Image(systemName: "xmark") }` placed `.topBarLeading` (Liquid Glass by default), or `Image(systemName:"xmark").font(.system(size: 19, weight: .semibold)).frame(44,44).glassEffect(.regular.interactive(), in: .circle)`. Android: 44 dp white circle, 0.5 dp #E8E8E8 ring, shadow (0, 8, blur 15, black 2 %), `close.svg` at 19–20 dp tinted #1A1A1A (same recipe as the Android sheet ✕ in screens-home.md §5.2).

### 1.4 Card / Plan (125:1198), NEW. SwiftUI `PBPlanCard`
Description (verbatim): “PBPlanCard — a paywall plan option (12-01), 171×104, radius 20, no border. Selected=True = color/bg/inverse with inverse text (the detail line at 72% opacity); Selected=False = color/bg/card. Period (Headline), Price (Amount/Medium), Detail (Footnote), Show badge + nested Badge / Pill On Card (exposed, “Save 33%”).
Yearly (₹799/year · ₹67/month · Save 33%) is selected by default and is the only plan with the 7-day trial; Monthly = Selected=False, “₹99/month” · “Billed monthly”, Show badge=false.
SwiftUI: PBPlanCard”

Properties: `Period` (text) · `Price` (text) · `Detail` (text) · `Show badge` (bool) · `Selected` = True | False.
Geometry (card-relative): **171 × 104** (on screen FILL: two cards in a row with gap 20), radius 20, vertical, padding **16** all sides, **space-between** (top row at the top, price block at the bottom), no stroke, no shadow.
- `top` row (16, 16) 139 wide, horizontal gap 8, centred: `period` **Headline**, fills, 1 line truncate · `badge` Badge / Pill Style=**On Card** (white pill, label Caption/1 `text/secondary`), "Save 33%" 77 × 24 (only when Show badge). Row height 24 with the badge, 22 without.
- `price block` (16, 48) 139 × 40, vertical gap 0: `price` **Amount/Medium** (Bold 17/22) · `detail` **Footnote** (Medium 13/18).
| Selected | Fill | Period / price | Detail |
|---|---|---|---|
| True | `bg/inverse` #0A0A0A | `text/inverse` #FFFFFF | `text/inverse` at **72 % opacity** |
| False | `bg/card` #F5F5F5 | `text/primary` #0A0A0A | `text/secondary` #6B6B6B |
Tap selects (single choice between the two cards). Suggested: 200 ms cross-fade of fill/text colours; accessibility: `.isSelected` trait / `Role.RadioButton`.

### 1.5 Control / Category Chip (64:4185), NEW here. SwiftUI `PBCategoryChip`
Description (verbatim): “Filter and people chip, 36 tall. Selected = inverse fill. Leading=None: text only (avatar categories, currency, date). Leading=Icon: 16 icon swap (the “Add” chip uses Plus). Leading=Avatar: exposed Avatar / Circle 24 for people pickers (Selected=False gives it a white avatar circle so the face reads on the #F5F5F5 chip). Show remove adds a trailing Close 16. SwiftUI: PBCategoryChip”
Used here with Leading=None, Show remove=false: height **36**, hug width, capsule, horizontal padding **16**, gap 6, label **Button/Small** (SemiBold 15/20). Selected: fill `bg/inverse`, label `text/inverse`. Not selected: fill `bg/card` #F5F5F5, label `text/primary` #0A0A0A. (If another page's spec also defines this chip, it's the same component.)

### 1.6 Overlay / Alert (102:1115), NEW here. SwiftUI `PBAlert`
Description (verbatim): “iOS 27 alert card, 300 wide, radius 34: Headline title, Subheadline message, two 36pt capsules sharing the width (both exposed). Action=Destructive: Button / Secondary + Button / Destructive (Discard, Delete). Action=Primary: Button / Secondary + Button / Primary when nothing is destroyed (Settle up). Built with .alert: a .cancel button plus a .destructive or default button. SwiftUI: PBAlert”
Geometry: **300** wide, hug height (182 in §10), fill `bg/primary`, radius **34**, effect **Material/Glass** (shadow (0, 8) blur 32 #0A0A0A @10 % + glass), vertical, padding **20**, gap **20**, centred.
- `Text` block 260 wide, vertical gap 4, centred: `title` **Headline** `text/primary` centre-aligned · `message` **Subheadline** (Medium 14/20) `text/secondary` centre-aligned, wraps.
- `Actions` row 260 × 36, horizontal gap 8, both buttons FILL (126 × 36 each): left `cancel` = Button / Secondary Small (fill `bg/card`, label `text/primary`), right `action` = Button / Primary Small (fill `bg/inverse`, label `text/inverse`) for Action=Primary, or Button / Destructive Small for Action=Destructive.
- Presented centred on screen over a **40 % scrim** (`bg/scrim`).
- iOS: `.alert(title, isPresented:) { Button("Not now", role: .cancel) {}; Button("Settle up") {…} } message: { Text(msg) }`. The system iOS 27 alert matches this card closely; that is what the description asks for. Android: a custom Dialog with exactly the geometry above (don't use the Material AlertDialog look).

### 1.7 Kit Grabber / sheet ✕
Same as components-home.md §14/§15 (grabber 60 × 4 #CCCCCC-ish; ✕ 50 × 50 kit glass). Nothing new.

---

## 2. `paywall`: Paybak Pro — Paywall (167:13003)

> **Shipped implementation:** both apps now sell Pro through the RevenueCat SDK. This route shows a RevenueCat Paywall for the `default` offering, and the entitlement is `paybak_pro` (Test Store in debug builds). The Figma layout below was the starting point for the dashboard paywall. See [`../revenuecat.md`](../revenuecat.md).

**Purpose.** Sell Paybak Pro (originally a mock subscription; flow.md: "Pro purchase (mock trial/purchase flow that flips a local entitlement; no store products)"). **Container:** full-screen modal. Entry points: the Profile "Paybak Pro" row, and every locked feature (Export records, the Add expense Repeat row, Insights; plus the other Pro features listed on the paywall when they are opened on the free plan). Prototype entry from Privacy › Export records: `ON_CLICK → NAVIGATE paywall, MOVE_IN direction TOP (slides up from the bottom), 300 ms, EASE_OUT`. No tab bar, no push header.

### Designer notes (verbatim)
- Section title: "Paybak Pro"
- Section subtitle: "The paywall opened from Profile or any locked feature, then the welcome screen once the Yearly trial starts."
- Caption 12-01: "Paywall, opened from the Profile “Paybak Pro” row or any locked feature (Export records, the Add expense Repeat row, Insights); Arjun is on the free plan and the core ledger stays free. Yearly is selected by default and is the only plan with the 7-day trial, so picking Monthly changes the CTA to “Subscribe for ₹99/month”."
- Card / Plan description (verbatim in §1.4): "Yearly (₹799/year · ₹67/month · Save 33%) is selected by default and is the only plan with the 7-day trial; Monthly = Selected=False, “₹99/month” · “Billed monthly”, Show badge=false."

### Plans and prices (verbatim, the whole business model)
| Plan | Card Period | Price | Detail | Badge | Trial | CTA label | Small print |
|---|---|---|---|---|---|---|---|
| Yearly (default, selected) | Yearly | ₹799/year | ₹67/month | Save 33% | **7-day free trial** (the only plan with one) | Start 7-day free trial | Then ₹799/year. Cancel anytime in Settings. |
| Monthly | Monthly | ₹99/month | Billed monthly | none | none | **Subscribe for ₹99/month** (caption 12-01) | not designed. Proposal: "Billed monthly. Cancel anytime in Settings." |
Maths behind the copy (for reference, keep the strings literal): 799 / 12 = 66.6 → "₹67/month"; 99 × 12 = 1,188 → 1 − 799/1,188 = 32.7 % → "Save 33%". Prices are always shown in ₹ as designed (mock store; not converted to the user's default currency).

**What's Pro-gated** (the feature list, in order): AI assistant (Ask Paybak) · Receipt scanning · Insights · Recurring expenses (the Add expense "Repeat" row) · PDF/CSV export (Privacy › Export records). "The core ledger stays free" (expenses, splits, balances, payments, loans, groups, projects, reminders).

Related designer notes on other pages that define the gating (verbatim, from `designer-notes.md`):
- 06-02 Add expense — Filled: "Repeat turns the expense into a recurring rule; it’s a Pro feature and Arjun is on the free plan, so the row carries a black Pro badge and opens the paywall."
- Recurring — Repeat: "For Pro members, the Repeat row in Add expense opens this sheet; free users get the paywall from that row."
- Insights — Locked: "State: Arjun, on the free plan, opens Insights. The charts stay blurred behind the lock, “See Pro” opens the paywall (12-01), and Timeline still works."
- Scan receipt — Camera: "Opens from “Add receipt” in Add expense. Free users can still take or upload the photo and attach it. Reading the items (Check receipt, Assign items) is Pro and opens the paywall. Shown as a Pro member."
- Forms & Money (component section note): "Modal header, amount entry, payment parties, split rows and the split total bar, paywall plan cards, the multi-line text area and the composer (comments and Ask Paybak). Amounts use Amount/Display; red appears only on errors."
So: Receipt scanning = *reading* the items is Pro (attaching a photo is free); Insights = the charts are Pro (Timeline stays free).

### Layout (frame auto-layout: vertical, padding 62/20/34/20, **gap 20**, bg `bg/primary`)
1. **Top** (20, 62) 362 × 226, vertical gap 4:
   - `Modal header` = Navigation / Modal Header, Action=None, Show title=false (§1.3): only `close` 44 × 44 at (20, 62) (kit glass xmark). Prototype hotspot (20, 62, 44, 44): `ON_CLICK → BACK` (dismiss the modal).
   - `Hero` (20, 110) 362 × 178, vertical gap 16, centred:
     - `App mark` = Brand / App Mark Size=96 at (153, 110): 96 × 96, `bg/inverse`, radius 21.4752 with 60 % corner smoothing, white "P" (components-core.md §1.1; file `assets/brand/app-mark.svg`).
     - `Title block` (20, 222) 362 × 66, vertical gap 4, centred: `title` "Paybak Pro", **Title/1** (ExtraBold 32/38), `text/primary`, centre-aligned (text box 168 × 38 at 117, 222) · `subtitle` "Smart tools on top of your free ledger.", **Body** (16/24), `text/secondary`, centre-aligned (277 × 24 at 62.5, 264).
2. **Features** (20, 308) 362 × 280, vertical gap 0: five `Sheet / Action Row` (State=Default, **Show chevron=false**) at FILL width, **56 tall** each (the component is 72; here the instances are 56 and have **padding 0**), rows at y 308 / 364 / 420 / 476 / 532. Per row (row y = Y): `tile` 44 × 44 at (20, Y+6), radius 14, fill `bg/card`, icon 24 × 24 centred at (30, Y+16), `icon/primary` · `text` column at (76, Y+6) 306 wide, gap 2: `title` **Headline** `text/primary`, `subtitle` **Subheadline** `text/secondary`, both one line. Not tappable (informational list, no chevron, no reaction).
   | # | Icon | Title | Subtitle |
   |---|---|---|---|
   | 1 | `sparkles.svg` | AI assistant | Ask about balances or add expenses by chat |
   | 2 | `camera.svg` | Receipt scanning | Snap a bill and split it item by item |
   | 3 | `chart.svg` | Insights | See where shared money goes each month |
   | 4 | `repeat.svg` | Recurring expenses | Rent and bills that add themselves |
   | 5 | `download.svg` | PDF/CSV export | Keep a clean copy of your records |
3. **Purchase** (20, 608) 362 × 224, vertical gap 16:
   - `Plans` (20, 608) 362 × 104, horizontal gap **20**: `Plan — Yearly` = Card / Plan Selected=True at (20, 608) 171 × 104 (period "Yearly" at 36, 625; badge "Save 33%" 77 × 24 at 98, 624; price "₹799/year" at 36, 656; detail "₹67/month" at 36, 678, white 72 %) · `Plan — Monthly` = Card / Plan Selected=False at (211, 608) 171 × 104 (period "Monthly" at 227, 624; price "₹99/month" at 227, 656; detail "Billed monthly" at 227, 678). Tap a card → select it (single choice) and update the CTA label and small print.
   - `CTA + legal` (20, 728) 362 × 104, vertical gap 10:
     - `CTA — Start trial` = Button / Primary Large, 362 × 52 at (20, 728), label "Start 7-day free trial" (Button/Large, `text/inverse`, 161 wide, centred). Prototype: `ON_CLICK → NAVIGATE Paybak Pro — Welcome, DISSOLVE 300 ms EASE_OUT`.
     - `Legal` (20, 790) 362 × 42, vertical gap 6, centred:
       - `small print` "Then ₹799/year. Cancel anytime in Settings.", **Footnote** `text/secondary`, centre-aligned (268 × 18 at 67, 790).
       - `Links` (78, 814) 246 × 18, horizontal gap **24**, centred: three `Button / Text` Style=Secondary instances whose labels are **Footnote** in **`text/tertiary` #A3A3A3** (overridden from Button/Small; each 18 tall): "Restore purchases" (78, 814, 116 × 18) · "Terms" (218, 814, 38 × 18) · "Privacy" (280, 814, 44 × 18). Pressed = 50 % opacity. Give each a ≥ 44-pt-tall hit area (extend vertically; don't change the visuals).
   - The purchase block ends at y 832, 8 above the bottom safe-area edge.

### States
- **Yearly selected (default, drawn).**
- **Monthly selected:** Monthly card Selected=True (black), Yearly Selected=False (gray, its "Save 33%" badge stays, on the gray card the On Card badge is white); CTA "Subscribe for ₹99/month"; small print per the table (proposal).
- **Already Pro** (paywall opened from the Profile "Paybak Pro" row while entitled): not designed. Proposal: open `proWelcome` in its "status" form instead of the paywall (same layout, body "Your free trial ends {date}. Then ₹799/year." during a trial, "Renews {EEE d MMM}. ₹799/year." / "Renews {date}. ₹99/month." afterwards, CTA "Done").

### Navigation
| Element | Action |
|---|---|
| ✕ close | Dismiss the modal (slide down 300 ms ease-in) back to where it was opened. Android system back = close. |
| Yearly / Monthly card | Select plan (no navigation). |
| Start 7-day free trial / Subscribe for ₹99/month | **Mock purchase** (flow.md): set the local entitlement `{isPro: true, plan: yearly or monthly, startedAt: now, trialEndsAt: yearly ? startOfDay(now) + 7 days : nil}` and persist it, then cross-fade (300 ms ease-out) to `proWelcome` inside the same modal. No fake App Store / Play sheet (don't imitate store UI). |
| Restore purchases | No reaction in Figma. Proposal: if a saved entitlement exists, re-apply it and go to `proWelcome`; otherwise show a native alert "No purchases to restore." with "OK". |
| Terms, Privacy | No reaction, no URLs yet (same rule as Get Started): tappable, do nothing. |
Debug menu: "Toggle Pro" flips the entitlement (flow.md).

### Scroll / keyboard
Fits the 874 frame exactly (no scroll in Figma). On shorter screens wrap the column below the close button in a vertical scroll view (close button stays pinned at the top-left); keep the Purchase block last. No keyboard.

### Test IDs
`screen.paywall`, `paywall.close`, `paywall.plan.yearly`, `paywall.plan.monthly`, `paywall.cta`, `paywall.smallPrint`, `paywall.restore`, `paywall.terms`, `paywall.privacy`.

### Sample data
All copy above is static (no user data on this screen).

---

## 3. `proWelcome`: Paybak Pro — Welcome (167:13888)

**Purpose.** Confirmation after the trial starts. **Container:** the same full-screen modal as the paywall (cross-fade from it). No header, no close button: the only way out is Done.

### Designer notes (verbatim)
- Caption 12-02: "Confirmation after the Yearly trial starts: today (Wed 30 Sep) plus 7 days is Wed 7 Oct. From here Arjun is a Pro member, and “Done” returns to the feature he tapped (Export records in the prototype) or to Profile."

### Layout (frame: vertical, padding 62/20/34/20, gap 0, bg `bg/primary`)
1. **Content** (20, 62) 362 × 666, vertical, **padding-top 24**, **gap 24**, clips:
   - `Illustration` slot (20, 86) **362 × 200**, content centred, clips. Inside: `Illustration / All set` at (80.33, 86) **241.33 × 200**. **Art: this is exactly the All set trio + check** (compared pixel-by-pixel with `ref/allSet.png`: same drawing at 2/3 scale). Use **`paybak-allset.riv`** (artboard `All Set` 362 × 300, state machine `All Set`, view model `AllSet`/`Instance`, bind `reduceMotion`), rendered at **241.33 × 200** (scale 2/3, fit contain), centred in the 362 × 200 slot. The artboard has no bleed (rive.md), so the Rive view rect = (80.33, 86, 241.33, 200). Taps on the people/badge are handled by the file's own listeners (don't fire triggers yourself). Static fallback: `assets/images/illustration-all-set.svg` scaled to 241.33 × 200.
   - `Text` (20, 310) 362 × 74, vertical gap **12**: `title` "You’re on Paybak Pro" (curly ’), **Title/1**, `text/primary`, left-aligned, 362 wide, wraps · `body` "Your free trial ends Wed 7 Oct. Then ₹799/year.", **Body**, `text/secondary`, left-aligned (20, 360) 362 × 24.
   - `Now unlocked` (20, 408) 362 × 320, vertical gap 8: `Row / Section Header` "Now unlocked" (Show action=false) · `Card` (20, 448) 362 × 280, fill `bg/card`, radius 20: five **Row / Setting** (Trailing=**Check**, Show icon=true, no subtitle/value/badge), 56 tall each at y 448 / 504 / 560 / 616 / 672, dividers on the first four (line x 72 → 382 at row y + 55):
     | Icon | Title |
     |---|---|
     | `sparkles.svg` | AI assistant |
     | `camera.svg` | Receipt scanning |
     | `chart.svg` | Insights |
     | `repeat.svg` | Recurring expenses |
     | `download.svg` | PDF/CSV export |
     The check is Icon / Check 24 at (342, row y + 16), `icon/primary`. Rows are **not** interactive here.
2. **CTA — Done**: Button / Primary Large, absolutely positioned at **(20, 772) 362 × 52** (bottom 824 = 16 above the safe-area bottom), label "Done". Prototype: `ON_CLICK → NAVIGATE Export records, DISSOLVE 300 ms EASE_OUT`.

### Dynamic text
- Yearly trial: body = "Your free trial ends {trialEndsAt as `EEE d MMM`}. Then ₹799/year." Date = start of today + 7 days (Wed 30 Sep 2026 → "Wed 7 Oct"). Format with the en-GB pattern `EEE d MMM` (no comma, no year).
- Monthly (not designed; proposal): "Your subscription renews {EEE d MMM, one month from today}. ₹99/month."

### Navigation
| Element | Action |
|---|---|
| Done | Dismiss the modal and continue to **the feature that opened the paywall** (e.g. push Export records onto the Privacy stack; open Insights; enable the Repeat row) — or back to Profile when it was opened from the Profile "Paybak Pro" row. The prototype shows the Export records case. |
| System back (Android) | Same as Done. iOS: no swipe-to-dismiss (`.interactiveDismissDisabled()`). |

### Motion (proposal)
Entrance from the paywall is the prototype's 300 ms dissolve. Optional: a success haptic when Welcome appears.

### Test IDs
`screen.proWelcome`, `proWelcome.title`, `proWelcome.body`, `proWelcome.done`.

---

## 4. `paymentDetails`: Payment details (167:14684)

**Purpose.** "Where friends learn how to pay you": the user's payment methods, the "Show to friends" switch and a preview of what friends see. **Container:** pushed screen from the Profile "Payment details" row (Profile is page 05, prototype node 64:4316). Back → Profile.

### Designer notes (verbatim)
- Section title: "Payment details"
- Section subtitle: "Where friends learn how to pay you: your methods, what friends see, and adding a UPI ID (with its error state)."
- Caption 12-03: "Where friends learn how to pay you. Only the primary method is shown to friends, and only while “Show to friends” is on."

### Layout (frame: vertical, padding 62/20/34/20, gap 24)
1. **Push header** (§1.2) (20, 62) 362 × 44: back + title "Payment details". Hotspot (20, 62, 44, 44) → Profile (prototype URL to page 05 node 64:4316).
2. **Your methods** (20, 130) 362 × 232, vertical gap 8:
   - `Row / Section Header` "Your methods" (no action).
   - `Methods` (20, 170) 362 × 192, vertical gap 0: three **Sheet / Action Row** (State=Default, **Show chevron=true**) at FILL width, **64 tall**, **padding 0** (the component's 12 side padding is removed here), gap 12. Per row (row y = Y): `tile` 44 × 44 at (20, Y+10), radius 14, `bg/card`, icon 24 at (30, Y+20) `icon/primary` · `text` (76, Y+10) 274 wide, gap 2: title **Headline** `text/primary`, subtitle **Subheadline** `text/secondary` · `chevron` 20 × 20 at (362, Y+22), `icon/tertiary` #A3A3A3.
     | Y | Icon | Title | Subtitle | Tap |
     |---|---|---|---|---|
     | 170 | `wallet.svg` | arjun@okaxis | UPI · Primary | method actions (proposal below) |
     | 234 | `bank.svg` | HDFC Bank ···· 4821 | Bank transfer | method actions (proposal below) |
     | 298 | `plus.svg` | Add payment method | UPI ID or bank account | **opens the Add payment method sheet (§5)**. Prototype: `ON_CLICK → NAVIGATE Add UPI ID, DISSOLVE 300 ms EASE_OUT`. |
3. **Show to friends card** (20, 386) 362 × 84: settings card (`bg/card`, r 20) holding one **Row / Setting** Trailing=**Toggle** (On), Show icon=false, Show subtitle=true, no divider: title "Show to friends" (Headline) at (36, 398); subtitle "Friends see your primary method when they settle up with you." (**Footnote** `text/secondary`, wraps to 2 lines in 254 wide, 36 tall at 36, 422); toggle 64 × 28 at (302, 414). Row height 84 = 12 + 60 + 12.
4. **What friends see** (20, 494) 362 × 116, vertical gap 8:
   - `Row / Section Header` "What friends see".
   - **Card / Payment Preview** (20, 534) 362 × 76 (Show copy=false, Show copy icon=true): fill `bg/card`, radius 20, padding 16, vertical gap 12. `row` (36, 550) 330 × 44, horizontal gap 12, centred: `avatar` Avatar / Circle Size=40 Type=Art at (36, 552), **Arjun** (`assets/avatars/avatar-1.svg`), circle fill **`bg/primary`** (white, it sits on the card) · `text` (88, 550) 222 wide, gap 2: `name` "Arjun Mehta" **Headline** `text/primary`, `upi` "arjun@okaxis" **Subheadline** `text/secondary` · `copy` Button / Icon Style=Plain 44 × 44 at (322, 550) with `copy.svg` 24 at (332, 560), `icon/primary`. Same component as setup3 (screens-setup.md).
5. **Info** (20, 634) 362 × 36, horizontal gap 8: `icon slot` 16 × 18 holding `lock.svg` 16 × 16 at (20, 635), tint `icon/secondary` #6B6B6B (verified) · `text` (44, 634) 338 × 36, **Footnote** `text/secondary`: "Paybak never moves money. Friends copy these details and pay you in their own app."
Content ends at y 670; no scroll in Figma. Implement as a vertical scroll view anyway (bottom inset 34 + 24).

### Behaviour / business rules
- **Primary method**: exactly one method is primary (subtitle gets " · Primary": "UPI · Primary"). Friends only ever see the primary method, and only while "Show to friends" is on (caption 12-03). The first method a user adds (setup3's UPI ID) becomes primary.
- **Method subtitles**: UPI → "UPI" (+ " · Primary"); bank account → "Bank transfer" (+ " · Primary"). Bank titles: "{Bank name} ···· {last 4 digits}" (four U+00B7 dots).
- **Show to friends** toggle (persisted, default On). **The preview card** shows the user's avatar, full name and the primary method's value. Off state not designed. Proposal: keep the "What friends see" header and replace the card with a Footnote line "Friends don’t see a payment method while this is off." (`text/secondary`).
- **Copy** button copies the primary method's value (UPI ID) to the clipboard and shows the "UPI ID copied" toast (same as setup3).
- **Tap an existing method** (not designed). Proposal: a native action sheet (iOS `confirmationDialog`, Android bottom sheet using Sheet / Container + Sheet / Action Rows) with "Make primary" (hidden for the primary), "Copy", "Remove" (destructive; removing the primary makes the next method primary; confirm with Overlay / Alert Action=Destructive, title "Remove payment method?", message "Friends won’t see it any more.", buttons "Cancel" / "Remove").

### Sample data (demo dataset)
Methods: `arjun@okaxis` (UPI, primary) · `HDFC Bank ···· 4821` (bank transfer). Show to friends = On. Preview: Arjun Mehta, avatar preset 0 (avatar-1), arjun@okaxis. A fresh account shows the UPI from setup3 (or only "Add payment method" if setup3 was skipped; then the preview card is replaced by the Footnote "Add a payment method so friends know how to pay you." (proposal)).

### Navigation
| Element | Destination |
|---|---|
| Back (glass chevron) / edge swipe / Android back | Pop to Profile |
| arjun@okaxis, HDFC Bank ···· 4821 rows | Method actions (proposal above) |
| Add payment method | Sheet §5 over this screen |
| Show to friends (row or switch) | Toggle, persist |
| Copy (preview) | Clipboard + toast |

### Test IDs
`screen.paymentDetails`, `paymentDetails.back`, `paymentDetails.method.<index>`, `paymentDetails.add`, `paymentDetails.showToFriends`, `paymentDetails.preview`, `paymentDetails.copy`.

---

## 5. `paymentAddUpi`: Add UPI ID (167:15962), sheet "Add payment method"

**Purpose.** Add a UPI ID (tab "UPI ID") or a bank account (tab "Bank account", not drawn). **Container:** `Sheet / Container` Detent=Medium over Payment details, with the keyboard up.

### Designer notes (verbatim)
- Caption 12-04: "The sheet adds a second UPI ID from his HDFC account. The value arjun@okhdfcbank is derived from the HDFC Bank ···· 4821 method, and the “Bank account” tab reuses this sheet with bank fields (not drawn)."

### Layout
- Background: the Payment details screen exactly as §4 (static, not interactive while the sheet is up).
- **Scrim** (0, 0) 402 × 874, `bg/scrim` (#0A0A0A @ 40 %).
- **Sheet** "Sheet — Add payment method" = Sheet / Container Detent=Medium (components-home.md §15) at **(8, 223) 386 × 344**, fill `bg/primary`, radius **40** all corners, vertical, padding **8 / 16 / 28 / 16**, gap 8:
  - `grabber` 60 × 4 at (171, 231) (kit Grabber).
  - `header` (24, 243) 354 × 50: `title` "Add payment method", **Title/3**, `text/primary`, 1 line truncate, (24, 255) 304 × 26 · `close` kit glass ✕ **50 × 50** at (328, 243) (xmark Semibold 19, #1A1A1A-ish; recipe in components-home.md §14).
  - `Content` slot (24, 301) 354 × 238 → `body`, vertical, **padding-top 8**, **gap 16**:
    1. **Control / Segmented** Options=2 (24, 309) **354 × 36** (padding 3): `UPI ID` Selected (27, 312) 174 × 30 (black pill, white label) · `Bank account` (201, 312) 174 × 30 (label `text/secondary`). Button/Small labels.
    2. `Form` (24, 361) 354 × 178, vertical gap **20**:
       - **Control / Input Field** State=**Focused**, Leading icon=true (Icon / **Wallet**), Show label, Show helper, 354 × 106 at (24, 361): label "UPI ID" (Subheadline `text/secondary`, 24, 361) · field (24, 389) 354 × 52, `bg/card`, radius 14, **1.5 inside ring `border/strong`** · leading icon `wallet.svg` 20 × 20 at (41.5, 405), `icon/secondary` · value "arjun@okhdfcbank" **Body** `text/primary` at (73.5, 403) (draw the text at x 72 with the ring as an overlay, README rule 6) · caret 2 × 22 `text/primary` radius 1 right after the text (drawn at 214, 404) · helper "Friends copy this to pay you in their UPI app." (**Footnote** `text/tertiary`, 24, 449).
       - **CTA — Save**: Button / Primary Large (24, 487) **354 × 52**, label "Save".
- **Keyboard**: iOS kit keyboard, **Type=Email** (has "@" and "." keys), 402 × 307 at y 567, no suggestions bar. The sheet's bottom edge (223 + 344 = 567) sits **exactly on the keyboard's top** (no gap), i.e. the Medium sheet rides the keyboard.

### Behaviour
- Opening: from the Payment details "Add payment method" row. Motion: the sheet slides up from the bottom with the scrim fading in (300 ms ease-out, same as the ＋ Add sheet, components-home.md §15); the prototype uses a 300 ms dissolve only because frames are separate. The UPI field is **focused on open** (keyboard up; `.focused` on appear / `FocusRequester`).
- Keyboard: iOS `.keyboardType(.emailAddress)`, `.textInputAutocapitalization(.never)`, `.autocorrectionDisabled()`, `.textContentType(.none)`; Android `KeyboardOptions(keyboardType = KeyboardType.Email, capitalization = None, autoCorrect = false, imeAction = Done)`; IME Done = Save. Keep the sheet anchored to the IME top (Android `imePadding()`), with the 8 pt side insets.
- **Prefill** (caption 12-04: "derived from the HDFC Bank ···· 4821 method"). Proposal for the rule: when the UPI tab opens and the user has a bank method that has no matching UPI ID yet, prefill `{local part of the primary UPI ID}@{bank UPI handle}` (HDFC → `okhdfcbank`, ICICI → `okicici`, SBI → `oksbi`, Axis → `okaxis`); otherwise start empty with the placeholder "yourname@bank" (README §5.1 #6) and the helper line.
- **Save** (always enabled while the field is non-empty; the caption for 12-05 says "Save stays enabled" in the error state; proposal: disabled while empty):
  - Valid (contains "@" with text on both sides; proposal regex `^[A-Za-z0-9._-]{2,}@[A-Za-z][A-Za-z0-9.-]+$`) → add a UPI method (not primary unless it's the first), dismiss the sheet (slide down + scrim fade), the new row appears above "Add payment method". Proposal: toast "UPI ID added".
  - No "@" (the designed case) or otherwise invalid → **error state §5.6**, keep focus.
  - Duplicate (proposal) → same error style with "You’ve already added this UPI ID".
- **Dismiss**: ✕, tap on the scrim above the sheet (hotspot 0, 0, 402 × 223), swipe down on the sheet, or Android back → close without saving (prototype: scrim/✕/Save hotspots all `BACK`).
- **Bank account tab** (not drawn; caption: "reuses this sheet with bank fields"). Proposal: same sheet, the segmented control on "Bank account", two Control / Input Fields stacked with gap 20: "Bank name" (leading icon `bank.svg`, placeholder "HDFC Bank") and "Account number" (number pad, placeholder "Account number", helper "Friends see the bank and the last 4 digits."), then Save. Saved as "{Bank name} ···· {last 4}" / "Bank transfer". The sheet grows to fit (Medium detent hugs its content).

### 5.6 `paymentAddUpiError`: Add UPI ID — Error (167:18527)
Designer note (verbatim), caption 12-05: "State: Save was tapped with an ID that has no “@”. The inline error clears as soon as the user edits the field, and Save stays enabled."
Differences from §5 (everything else identical, same geometry):
- Value "arjunokhdfcbank" (no "@"); caret after it (drawn at 200, 404); still focused, keyboard up.
- Input Field State=**Error**, **Show helper=false**: the field ring becomes **1.5 inside `border/destructive` #C93636**; value stays `text/primary`.
- In place of the helper, an `Error` row (24, 449) 354 × 18, horizontal gap **4**, centred: `alert.svg` **16 × 16** at (24, 450), tint `icon/destructive` #C93636 (verified) + "Enter a UPI ID like name@bank" (**no final period**), **Caption/1** (Bold 12/16, +1 %), `text/destructive` #C93636, at (44, 450). The "Field + error" wrapper is 106 tall, the same as the field with its helper, so nothing below moves.
- Editing the field (any change) → back to Focused with the helper; the error row disappears.
- Accessibility: announce the error (`UIAccessibility.post(.announcement)` / `LiveRegion`). Error haptic (proposal, as on Verify).
This supersedes the README §5.1 #6 proposal ("Enter a UPI ID like name@bank." with a period): Figma has no period and uses Caption/1 + alert icon.

### Test IDs
`screen.paymentAddUpi`, `paymentAddUpi.sheet`, `paymentAddUpi.close`, `paymentAddUpi.segment.upi`, `paymentAddUpi.segment.bank`, `paymentAddUpi.field`, `paymentAddUpi.helper`, `paymentAddUpi.error`, `paymentAddUpi.save`. Debug start screen `paymentAddUpiError` opens the sheet with "arjunokhdfcbank" and the error shown.

---

## 6. `settingsCurrency`: Currency (176:17773)

**Purpose.** Default currency for totals, the "keep balances per currency" switch, and how exchange rates work. **Container:** pushed screen from the Profile "Currency" row. Back → Profile (prototype URL to page 05 node 64:4316).

### Designer notes (verbatim)
- Section title: "Preferences"
- Section subtitle: "Currency and Notifications, behind the Profile rows: default currency, per-currency balances and reminders."
- Caption 12-06: "INR is the default for totals. Keeping balances per currency is off, so AED amounts convert at the rate saved with each expense."

### Layout (frame: vertical, padding 62/20/34/20, gap 24)
1. **Push header** "Currency" (§1.2).
2. **Default currency** (20, 130) 362 × 122, vertical gap 8:
   - `Row / Section Header` "Default currency".
   - `Default currency card` (20, 170) 362 × 56: fill `bg/card`, radius 20, **padding 0 / 16** (left/right), holding one **Row / Currency** Selected=True at FILL (36, 170) **330 × 56**: `symbol tile` 40 × 40 at (36, 178), fill **`bg/primary`** (white on the card; components-core.md §5.5 uses `bg/card` on white) with "₹" (Headline, centred) · `text` (88, 176) 244 wide, gap 2: title **"INR"** (Headline) over subtitle **"Indian rupee"** (Subheadline `text/secondary`; lower-case "r"). Note the order is the reverse of Setup 2 (there: name over code) · `radio` 22 × 22 at (344, 187): `bg/inverse` circle with a white Icon / Check 14.
   - `footer` (20, 234) "Home totals and new groups use this currency." (Footnote `text/secondary`).
3. **Balances** (20, 276) 362 × 158, vertical gap 8:
   - `Row / Section Header` "Balances".
   - settings card (20, 316) 362 × 56 with one **Row / Setting** Trailing=**Toggle**, **Off**, Show icon=false, no divider: title "Keep balances per currency" at (36, 333); switch at (302, 330).
   - `footer` (20, 380) 362 × 54, Footnote `text/secondary`, **two paragraphs with a hard line break** (`\n`): "Show what you owe in each currency instead of converting it." + "\n" + "e.g. You owe Kabir AED 60 and ₹1,400" (the first sentence wraps to 2 lines at 362, so 3 lines total).
4. **Exchange rates** (20, 458) 362 × 76, vertical gap 8:
   - `Row / Section Header` "Exchange rates".
   - `Info` (20, 498) 362 × 36, horizontal gap 8: 16 × 18 icon slot with `exchange.svg` 16 at (20, 499), tint `icon/secondary` #6B6B6B (verified) · Footnote `text/secondary` (44, 498) 338 × 36: "Rates are saved when an expense is added. Balances don’t change when rates move."
No scroll needed (content ends at y 534); use a scroll view anyway.

### Behaviour / business rules
- **Default currency** (persisted; set in Setup 2): used for Home totals, balance summaries and as the default for new groups and new expenses. Tapping the default currency card (not wired in Figma) opens the currency picker. Proposal: a `Sheet / Container` Detent=Large titled "Default currency" with Show search (the same list as Setup 2 §2 / the Add expense "Currency" sheet: ISO list, Suggested, Popular), selecting a row updates the card and dismisses. Changing it doesn't rewrite stored expenses; only totals re-convert.
- **Keep balances per currency** (persisted, **default Off**):
  - Off (drawn): every foreign-currency amount converts into the default currency at **the rate saved with that expense** (Add expense stores the rate when the currency differs; caption 06-08). E.g. Dubai Weekend AED expenses count as ₹ in all balances.
  - On: balances are kept separately per currency and shown per currency, e.g. "You owe Kabir AED 60 and ₹1,400" (the footer example). Settle-up suggestions then settle each currency separately.
- **Exchange rates**: informational only. Rates never change after saving: "Balances don’t change when rates move."

### Sample data
INR "₹" "Indian rupee" selected; Keep balances per currency Off; example text "Kabir AED 60 and ₹1,400".

### Navigation
| Element | Destination |
|---|---|
| Back | Pop to Profile |
| Default currency card | Currency picker sheet (proposal) |
| Keep balances per currency (row or switch) | Toggle, persist; balances recompute |

### Test IDs
`screen.settingsCurrency`, `settingsCurrency.back`, `settingsCurrency.default`, `settingsCurrency.perCurrency`.

---

## 7. `settingsNotifications`: Notifications & reminders (176:18371)

**Purpose.** Per-push-type switches, the default reminder schedule, and muted friends. **Container:** pushed screen from the Profile "Notifications" row; **vertical scroll** (the only scrolling frame on this page: `overflowDirection = VERTICAL`, 5 fixed children). Header title is "Notifications" (the frame is named "Notifications & reminders").

### Designer notes (verbatim)
- Caption 12-07: "Every push type can be switched off separately. This default schedule applies to anything with a due date, including loan installments, so Rohan’s Movie tickets (due Sun 27 Sep) got automatic reminders on Fri 25 Sep, Sun 27 Sep and today, Wed 30 Sep."

### Fixed layer (doesn't scroll)
- `Header background (fixed)`: rectangle **(0, 0) 402 × 106**, fill `bg/primary` (solid white behind the status bar and header; content scrolls under it).
- `Push header` "Notifications" (§1.2) at (20, 62), fixed (absolute).
- Back hotspot → Profile.

### Scrolling content (frame column: padding 62/20/34/20, gap 24; the first child is a 44-tall `Header space` spacer at y 62 so content starts at y 130)
1. **Push notifications** (20, 130) 362 × 376, vertical gap 8:
   - `Row / Section Header` "Push notifications".
   - settings card (20, 170) 362 × 336: **six Row / Setting**, Trailing=**Toggle**, **all On**, Show icon=false (dividers from x 36 to 382), rows 56 tall at y 170 / 226 / 282 / 338 / 394 / 450; last row no divider:
     | # | Push type (title, verbatim) | Default | What it controls (implementation) |
     |---|---|---|---|
     | 1 | Added to an expense | On | Someone adds you to an expense (simulated friend actions + local events). |
     | 2 | Payments to confirm | On | A friend recorded a payment to you that needs confirming (the lock-screen "confirm payment" push with actions, flow.md). |
     | 3 | Reminders | On | Automatic reminders from the schedule below (things you owe and are owed that have a due date). |
     | 4 | Overdue alerts | On | Items that just became overdue. |
     | 5 | Project updates | On | Budget / member updates in projects. |
     | 6 | Monthly summary | On | A monthly recap (links to Insights). |
     The "What it controls" column is our interpretation of the titles (not in Figma).
2. **Reminder schedule** (20, 530) 362 × 252, vertical gap 8:
   - `Row / Section Header` "Reminder schedule".
   - settings card (20, 570) 362 × 168: **three Row / Setting**, Trailing=**Check** (all checked), Show icon=false, rows at y 570 / 626 / 682:
     | Title (verbatim) | Default | Rule |
     |---|---|---|
     | 2 days before | ✓ | a reminder at 09:00 local time (proposal) on due date − 2 days |
     | On the due date | ✓ | a reminder on the due date |
     | When overdue, every 3 days | ✓ | while unpaid: due date + 3, + 6, + 9 … days |
     These are **independent checkboxes** (multi-select, all three on by default), even though the component description says "single select" for Check rows.
   - `footer` (20, 746) 362 × 36: "For anything with a due date. Friends who owe you get a gentle push." (Footnote `text/secondary`, 2 lines).
3. **Muted friends** (20, 806) 362 × 82, vertical gap 8 (no section header):
   - settings card (20, 806) 362 × 56: **Row / Setting** Trailing=**Chevron**, Show icon (Icon / **Bell** `bell.svg`), Show value "None": title "Muted friends" (72, 823) · value "None" (**Body** `text/secondary`, 294, 822) · chevron (346, 824).
   - `footer` (20, 870) "Muted friends don’t get automatic reminders." (Footnote `text/secondary`).
   The content ends at **y 888**, 14 pt below the frame bottom: in Figma this footer is clipped, which proves the screen scrolls. Bottom content inset: 34 + 24.

### Reminder schedule rules (from caption 12-07; implement exactly)
- Applies to **anything with a due date**: expenses with a due date, loans (IOUs) and **each loan installment**, project contributions with a due date.
- Worked example (demo data, today = Wed 30 Sep 2026): Rohan owes Arjun for "Movie tickets", **due Sun 27 Sep** → reminders **Fri 25 Sep** (2 days before), **Sun 27 Sep** (on the due date), **Wed 30 Sep** (overdue +3 days, "today"); the next would be Sat 3 Oct.
- "Friends who owe you get a gentle push": for money owed **to** you, the reminder goes to the friend (simulated; it appears in the activity/notifications inbox as "Reminder sent"), and for money **you** owe, you get a local notification. Local notifications are real (flow.md): schedule them with UNUserNotificationCenter / AlarmManager+NotificationManager, respecting the "Reminders" and "Overdue alerts" switches and the OS permission from Setup 4.
- Muted friends are skipped by automatic reminders (manual "Remind" still works).

### Muted friends
Where muting happens (verbatim note under «Friend — Rohan» on page 07): "Automatic reminders follow the default schedule, so Rohan was reminded Fri 25 Sep, on the due date (Sun 27 Sep) and today. The toggle mutes them for Rohan only, and Settings › Muted friends (12-07) lists anyone turned off here."
So muting is set per friend on the friend's page; this row lists them. The list screen itself is not designed. Proposal: push "Muted friends" (Push Header) with one settings card listing the muted friends (Row / Setting with the friend's Avatar / Circle 32 in the icon slot, Trailing=Toggle On = muted; turning it off un-mutes and removes the row after the screen closes); empty state = Footnote "No muted friends. Mute a friend from their page." The value here shows "None", "1 friend" or "{n} friends".

### Navigation
| Element | Destination |
|---|---|
| Back | Pop to Profile |
| Any push-type row / switch | Toggle, persist |
| Schedule rows | Toggle the check, persist, reschedule local notifications |
| Muted friends | Muted friends screen (proposal) |

### Test IDs
`screen.settingsNotifications`, `settingsNotifications.back`, `settingsNotifications.push.addedToExpense|paymentsToConfirm|reminders|overdueAlerts|projectUpdates|monthlySummary`, `settingsNotifications.schedule.twoDaysBefore|onDueDate|overdueEvery3Days`, `settingsNotifications.muted`.

---

## 8. `privacyData`: Privacy & data (176:19720)

**Purpose.** Discovery settings and data controls: Export records (Pro), Recently deleted, Delete account. **Container:** pushed screen from the Profile "Privacy" row; header title "Privacy". Back → Profile.

### Designer notes (verbatim)
- Section title: "Privacy & data"
- Section subtitle: "Discovery and data controls behind the Profile “Privacy” row: Export records (Pro), Recently deleted, and the blocked Delete account state."
- Caption 12-08: "Discovery and data controls on the free plan: Export is a Pro feature marked with a black “Pro” badge, and Recently deleted opens the same screen (09-07) as the Activity header icon. Red appears only on the destructive Delete account row."

### Layout (frame: vertical, padding 62/20/34/20, gap 24)
1. **Push header** "Privacy".
2. **Discovery** (20, 130) 362 × 178, vertical gap 8:
   - `Row / Section Header` "Discovery".
   - settings card (20, 170) 362 × 112, two **Row / Setting** Trailing=**Toggle** (On), Show icon=true:
     - "Find me by phone or email", icon `search.svg`, divider x 72 → 382 (row y 170).
     - "Contacts sync", icon `people.svg`, no divider (row y 226).
   - `footer` (20, 290) "Contacts are only used to find friends already on Paybak."
3. **Your data** (20, 332) 362 × 178, vertical gap 8:
   - `Row / Section Header` "Your data".
   - settings card (20, 372) 362 × 112:
     - **Export records**: Row / Setting Trailing=Chevron, icon `download.svg`, **Show badge=true** → Badge / Pill **Inverse "Pro"** (40 × 24 at 294, 388; black pill, white Caption/1 label), chevron (346, 390), divider. Prototype: `ON_CLICK → NAVIGATE Paybak Pro — Paywall, MOVE_IN (from the bottom) 300 ms EASE_OUT`.
     - **Recently deleted**: Trailing=Chevron, icon `restore.svg`, **value "1 item"** (Body `text/secondary`, 292, 444), no divider. Prototype: URL → page 09 (Activity, 77:103), node 177:29968 = the Recently deleted screen (09-07).
   - `footer` (20, 492) "Deleted items can be restored for 30 days."
4. **Delete account** (20, 534) 362 × 100, vertical gap 8 (no section header):
   - settings card (20, 534) 362 × 56: **Row / Setting** Trailing=**None**, **Tone=Destructive**, icon `delete.svg` tinted `icon/destructive` #C93636 (verified), title "Delete account" `text/destructive` #C93636.
   - `footer` (20, 598) 362 × 36: "Your past records stay in friends’ groups, shown as a former member." (2 lines).
Content ends at y 634 (no scroll needed; use a scroll view).

### Behaviour / business rules
- **Find me by phone or email** (persisted, default On): whether others can find this account by its sign-in contact. Local-only today (no backend); store the flag.
- **Contacts sync** (persisted, default On): permission-gated. Proposal: turning it on requests the OS contacts permission (iOS `CNContactStore.requestAccess`, Android `READ_CONTACTS`); if denied, flip back Off and show a native alert explaining how to enable it in Settings. Used only to suggest friends in "Add friend" pickers ("Contacts are only used to find friends already on Paybak.").
- **Export records**: on the free plan → the paywall (with "return to Export records" as the post-purchase target, §3); on Pro → push `privacyExport` (§9). On Pro the "Pro" badge is hidden (proposal; Figma only shows the free state).
- **Recently deleted**: push the Recently deleted screen (spec'd with page 09 Activity, 09-07). The value shows the number of deleted items still restorable ("1 item", "{n} items"; hide the value when 0, or "None" (proposal)). Note under «Recently deleted» on page 09 (verbatim): "Deleted expenses wait here for 30 days, and anyone in the group can restore them. “24 days left” counts from 30 Sep to 24 Oct; the screen opens from the Activity header icon and from Settings › Privacy & data (12-08)."
- **Delete account**: if **any balance is open** (anything owed to or by the user, in any group or 1:1) → show the blocked alert (§10). If everything is settled (not designed; proposal): an Overlay / Alert **Action=Destructive** "Delete account?" / "This removes your profile and settings from this device. Your past records stay in friends’ groups, shown as a former member." / buttons "Cancel" + "Delete"; Delete clears the local store and returns to Welcome (like "Reset onboarding").

### Navigation
| Element | Destination |
|---|---|
| Back | Pop to Profile |
| Find me by phone or email / Contacts sync | Toggle (see rules) |
| Export records | Free: paywall (full-screen modal, slide up 300 ms ease-out). Pro: push `privacyExport`. |
| Recently deleted | Push Recently deleted (09-07) |
| Delete account | Open balances → alert §10; none → destructive confirm (proposal) |

### Sample data
Free plan (Arjun). Find me = On, Contacts sync = On, "1 item" in Recently deleted (the demo's deleted item from page 09), badge "Pro".

### Test IDs
`screen.privacyData`, `privacyData.back`, `privacyData.findMe`, `privacyData.contactsSync`, `privacyData.export`, `privacyData.recentlyDeleted`, `privacyData.deleteAccount`.

---

## 9. `privacyExport`: Export records (176:20773)

**Purpose.** Export records as PDF or CSV for a date range and a set of groups (Pro). **Container:** pushed screen on the Privacy stack (from Export records when Pro, or after the paywall's Done). Back → Privacy & data (prototype: `NAVIGATE Privacy & data, PUSH direction RIGHT 350 ms EASE_IN_AND_OUT` = a normal pop).

### Designer notes (verbatim)
- Caption 12-09: "An export of September’s records: College Gang (12 Mar) and Dubai Weekend (6–8 Mar) are unticked because they have nothing in this range, and “Without a group” covers the dinner, the movie tickets and the groceries, plus Priya’s ₹1,050 payment. Export opens the iOS share sheet. Shown as a Pro member."

### Layout (frame: vertical, padding 62/20/34/20, gap 24)
1. **Push header** "Export records".
2. **Format** (20, 130) 362 × 76, vertical gap 8: `Row / Section Header` "Format" · **Control / Segmented** Options=2 at FILL **362 × 36** (20, 170): "PDF" **selected** (23, 173) 178 × 30 · "CSV" (201, 173) 178 × 30.
3. **Range** (20, 230) 362 × 102, vertical gap 8:
   - `Row / Section Header` "Range".
   - `Range chips` (20, 270), horizontal gap **8**: three **Control / Category Chip** (§1.5, Leading=None): "This month" **Selected** (20, 270) 113 × 36 · "Last 3 months" (141, 270) 133 × 36 · "All time" (282, 270) 85 × 36. Single select.
   - `footer` (20, 314) "1 Sep – 30 Sep 2026" (Footnote `text/secondary`; en dash U+2013 with spaces).
4. **Groups block** (20, 356) 362 × 420, vertical gap 8:
   - `Row / Section Header` "Groups" with **Show action=true**: Button / Text Secondary "Select all" (hit area 66 × 44 at 316, 350; label Button/Small `text/secondary`).
   - settings card (20, 396) 362 × 336: six **Row / Setting**, Show icon=false, Trailing **Check** (ticked) or **Unchecked** (empty 24 slot), dividers x 36 → 382 except the last:
     | Row y | Title (verbatim) | Drawn state | Why (caption) |
     |---|---|---|---|
     | 396 | Goa Trip | ✓ | has records in September |
     | 452 | Flat 302 | ✓ | has records in September |
     | 508 | College Gang | unticked | nothing in range (its records are from 12 Mar) |
     | 564 | Dubai Weekend | unticked | nothing in range (6–8 Mar) |
     | 620 | Build a Drone | ✓ | (a project) has records in September |
     | 676 | Without a group | ✓ | the dinner, the movie tickets and the groceries, plus Priya’s ₹1,050 payment |
   - `note` (20, 740) 362 × 36: "Includes expenses, payments and loans, with each person’s share." (Footnote `text/secondary`, 2 lines).
5. **CTA — Export**: Button / Primary Large, absolutely positioned at **(20, 788) 362 × 52**, label "Export"; its bottom (840) sits on the bottom safe-area edge.

### Scroll
Content ends at y 776 (12 above the CTA). With more groups the list scrolls under the pinned CTA: make the content scrollable with a bottom inset of (874 − 788) + 12 = 98, and put a solid `bg/primary` strip behind the CTA from y 776 to the screen bottom (proposal, so rows don't show through the gaps around the pill).

### Behaviour / business rules
- **Pro only.** Reached only when entitled (free users are sent to the paywall from Privacy). If the entitlement lapses while on this screen, the Export button opens the paywall (proposal).
- **Format**: PDF (default) | CSV.
- **Range** (default "This month"). Footer shows the resolved range, en-GB dates:
  - This month → 1st … last day of the current month: "1 Sep – 30 Sep 2026" (demo "today" = Wed 30 Sep 2026).
  - Last 3 months (proposal) → 1st of the month two months back … end of this month: "1 Jul – 30 Sep 2026".
  - All time (proposal) → date of the earliest record … today, e.g. "12 Mar – 30 Sep 2026". If the two dates are in different years: "d MMM yyyy – d MMM yyyy".
- **Groups list** = every group and project the user belongs to (Goa Trip, Flat 302, College Gang, Dubai Weekend, Build a Drone in the demo; archived ones included) + **"Without a group"** (1:1 expenses, payments and loans outside any group).
  - **Default ticks**: a row is ticked **iff it has at least one record in the selected range**; groups with nothing in range start unticked (caption). Recompute the default ticks whenever the range changes. The user can tick/untick any row (multi-select).
  - **Select all** ticks every row. Proposal: when all rows are ticked the action reads "Deselect all".
- **Export** (proposal: disabled when no row is ticked): build the file on-device, then open the **system share sheet** with it (iOS `ShareLink` / `UIActivityViewController`; Android `Intent.createChooser(ACTION_SEND)` via a `FileProvider` URI). File name (proposal): `Paybak records 1 Sep – 30 Sep 2026.pdf` / `.csv`.
- **Contents**: "Includes expenses, payments and loans, with each person’s share." Proposal:
  - CSV (UTF-8 with BOM, comma-separated, one row per record): `Date, Group, Type (Expense|Payment|Loan|Installment), Title, Category, Paid by, Amount, Currency, Rate, Amount (default currency), Shares` where Shares = "Arjun ₹700; Priya ₹700; …". Amounts as plain numbers with 2 decimals.
  - PDF (A4, rendered natively: iOS `UIGraphicsPDFRenderer` / `ImageRenderer`; Android `PdfDocument`): title "Paybak records", the range line, then one section per ticked group with a table (Date · Title · Paid by · Amount · Each person's share) and the group total; footer "Paybak never moves money."

### Sample data (demo dataset, Pro member)
Format PDF; range This month (1 Sep – 30 Sep 2026); ticked: Goa Trip, Flat 302, Build a Drone, Without a group; unticked: College Gang (12 Mar), Dubai Weekend (6–8 Mar). "Without a group" in September = the Olive Garden dinner, Rohan's Movie tickets, the groceries, and Priya's ₹1,050 payment.

### Navigation
| Element | Destination |
|---|---|
| Back | Pop to Privacy & data |
| PDF / CSV | Select format |
| Range chips | Select range; footer + default ticks update |
| Select all | Tick all rows |
| Group rows | Toggle tick |
| Export | Generate file → system share sheet |

### Test IDs
`screen.privacyExport`, `privacyExport.back`, `privacyExport.format.pdf`, `privacyExport.format.csv`, `privacyExport.range.thisMonth|last3Months|allTime`, `privacyExport.rangeLabel`, `privacyExport.selectAll`, `privacyExport.group.<id>` (`withoutGroup` for the last row), `privacyExport.export`.

---

## 10. `privacyDeleteBlocked`: Delete account — Blocked (177:24246)

**Purpose.** The state after tapping Delete account while balances are open. **Container:** alert over Privacy & data (the background is §8 unchanged).

### Designer notes (verbatim)
- Caption 12-10: "State: Delete account was tapped while balances are open, using the Home totals of −₹1,850 and +₹2,900. Deleting is blocked, and “Settle up” opens the settle-up suggestions (08-03)."

### Layout
- Background: §8 Privacy & data, identical.
- `Scrim` (0, 0) 402 × 874, `bg/scrim` 40 %.
- **Alert — Settle up first** = Overlay / Alert **Action=Primary** (§1.6) at **(51, 346) 300 × 182** (centred on the full screen), fill `bg/primary`, radius 34, Material/Glass shadow, padding 20, gap 20:
  - `title` "Settle up first" (Headline, centred) at (71, 366) 260 × 22.
  - `message` "You still owe ₹1,850 and are owed ₹2,900. Settle every balance before deleting your account." (Subheadline `text/secondary`, centred, 3 lines, 260 × 60 at 71, 392).
  - `Actions` (71, 472) 260 × 36, gap 8: `cancel` Button / Secondary Small "Not now" (71, 472) 126 × 36 · `action` Button / Primary Small "Settle up" (205, 472) 126 × 36.

### Rules
- Show this alert when the user has **any open balance**. Message template: "You still owe {total you owe} and are owed {total owed to you}. Settle every balance before deleting your account." The two amounts are the Home balance totals (Home shows −₹1,850 and +₹2,900 for the demo), printed **without signs**, formatted in the default currency. Proposals when one side is zero: "You still owe ₹1,850. Settle every balance before deleting your account." / "You’re still owed ₹2,900. Settle every balance before deleting your account."

### Navigation
| Element | Destination |
|---|---|
| Not now | Dismiss the alert (stay on Privacy & data) |
| Settle up | Dismiss, then open the **settle-up suggestions** screen (page 08 Settle Up, 08-03, node 167:12113) the same way Home's "Settle up" does |
| Scrim tap / Android back | Same as Not now (proposal) |
iOS: native `.alert` (§1.6). Android: custom dialog matching §1.6.

### Test IDs
`screen.privacyDeleteBlocked` (debug start: Privacy with the alert open), `privacyDeleteBlocked.alert`, `privacyDeleteBlocked.message`, `privacyDeleteBlocked.notNow`, `privacyDeleteBlocked.settleUp`.

---

## 11. `helpFeedback`: Help & feedback (177:24773)

**Purpose.** "Short answers to the questions people ask most, plus a way to contact the team and rate the app." **Container:** pushed screen from the Profile "Help & feedback" row. Back → Profile.

### Designer notes (verbatim)
- Section title: "Help"
- Section subtitle: "Common questions, contact and rating."
- Caption 12-11: "Short answers to the questions people ask most, plus a way to contact the team and rate the app. The version number is a placeholder that will come from the build."

### Layout (frame: vertical, padding 62/20/34/20, gap 24)
1. **Push header** "Help & feedback".
2. **Common questions** (20, 130) 362 × 344, vertical gap 8:
   - `Row / Section Header` "Common questions".
   - settings card (20, 170) 362 × 304: five **Row / Setting**, Trailing=**Chevron**, Show icon (`help.svg`), dividers x 72 → 382 except the last. Title column 262 wide; **two titles wrap to 2 lines** (rows 68 tall, icon and chevron stay vertically centred):
     | Row y | Height | FAQ question (verbatim) |
     |---|---|---|
     | 170 | 56 | Does Paybak move money? |
     | 226 | 68 | How do payment confirmations work? |
     | 294 | 56 | How does simplify debts work? |
     | 350 | 68 | Can I track more than one currency? |
     | 418 | 56 | Why can’t I leave a group? |
3. **Get in touch** (20, 498) 362 × 152, vertical gap 8:
   - `Row / Section Header` "Get in touch".
   - settings card (20, 538) 362 × 112: "Contact us" (icon `mail.svg`, chevron, divider) · "Rate Paybak" (icon `star.svg`, chevron).
4. `Version` (20, 674) 362 × 18: "Paybak 1.0 (1)", **Footnote** `text/tertiary` #A3A3A3, **centred**. Build it from the app: iOS "Paybak \(CFBundleShortVersionString) (\(CFBundleVersion))", Android "Paybak ${versionName} (${versionCode})".
No scroll needed (content ends at y 692); use a scroll view.

### FAQ answers
**Not designed**: the Figma rows only carry the questions (chevron = push). Proposal: each row pushes a simple answer screen (Push Header with the question as the title, truncating; then the answer in **Body** `text/primary`, 362 wide, 24 below the header). Proposed answers, written from the designer's own captions on other pages (replace if product provides copy):
1. Does Paybak move money? → "No. Paybak only records who paid and who owes. Friends pay each other in their own apps, by UPI, bank transfer or cash, and Paybak keeps the ledger." (from the Payment details info line "Paybak never moves money. Friends copy these details and pay you in their own app.")
2. How do payment confirmations work? → "When a friend records a payment to you, it stays pending until you confirm you got it. Confirm it and the balance updates, or mark it Not received and nothing changes."
3. How does simplify debts work? → "Paybak nets out the balances in a group so fewer payments settle everyone. Nobody pays or gets more in total; only who pays whom changes."
4. Can I track more than one currency? → "Yes. Each expense can use its own currency, and the rate is saved with it. By default everything converts to your default currency; turn on Keep balances per currency in Currency to see each currency separately."
5. Why can’t I leave a group? → "You can leave a group once your balance in it is settled. Settle up first, then leave."

### Navigation
| Element | Destination |
|---|---|
| Back | Pop to Profile |
| FAQ rows | Push the answer screen (proposal) |
| Contact us | Proposal: the system mail composer (iOS `MFMailComposeViewController` / `mailto:`; Android `ACTION_SENDTO mailto:`) with subject "Paybak feedback" and the version line in the body. **No support address exists in Figma; needs a decision** (keep it in one constant). If no mail app, show "Couldn’t open Mail." |
| Rate Paybak | iOS `@Environment(\.requestReview)` (StoreKit review prompt); Android Play In-App Review API, falling back to the Play Store listing. (Both may no-op in debug/sim builds; that's fine.) |

### Test IDs
`screen.helpFeedback`, `helpFeedback.back`, `helpFeedback.faq.0…4`, `helpFeedback.contact`, `helpFeedback.rate`, `helpFeedback.version`.

---

## 12. State, persistence and logic for this page (input to domain.md and app-architecture.md)

All local (flow.md: no backend, persisted across launches).

### 12.1 Pro entitlement
```
ProEntitlement { isPro: Bool, plan: .yearly | .monthly | nil, startedAt: Date?, trialEndsAt: Date? }
```
- Default: free (`isPro = false`), like Arjun in the Figma frames.
- Start trial (Yearly): `startedAt = now`, `trialEndsAt = startOfDay(now) + 7 days` → Welcome shows "Your free trial ends {EEE d MMM}. Then ₹799/year." (Wed 30 Sep → Wed 7 Oct).
- Subscribe (Monthly): no trial.
- Pro-gated features (each opens the paywall on the free plan and remembers where to continue after "Done"): Ask Paybak (AI assistant), Receipt scanning, Insights, Recurring expenses (Add expense Repeat row), Export records (PDF/CSV).
- Debug menu: "Toggle Pro" (flow.md).

### 12.2 Payment methods
```
PaymentMethod { id, kind: .upi | .bank, value: String /* "arjun@okaxis" */, bankName: String?, last4: String?, isPrimary: Bool }
Profile.showPaymentToFriends: Bool = true
```
Exactly one primary. Setup 3's UPI ID becomes the first (primary) method. Subtitles: "UPI" / "Bank transfer" (+ " · Primary"). Bank title "{bankName} ···· {last4}".
Demo dataset: `arjun@okaxis` (UPI, primary), `HDFC Bank ···· 4821` (bank). Add sheet prefill for the demo: "arjun@okhdfcbank".

### 12.3 Preferences
```
defaultCurrency = "INR"                // from Setup 2
keepBalancesPerCurrency = false
push = { addedToExpense: true, paymentsToConfirm: true, reminders: true, overdueAlerts: true, projectUpdates: true, monthlySummary: true }
reminderSchedule = { twoDaysBefore: true, onDueDate: true, overdueEvery3Days: true }
mutedFriendIds = []                    // value "None"
discovery = { findMeByContact: true, contactsSync: true }
```
- Balance maths with `keepBalancesPerCurrency = false`: convert every amount to the default currency with **the rate stored on that expense/payment** (never a live rate). With `true`: keep per-currency ledgers; don't convert.
- Reminder dates for a due date D (only while the item is unpaid and the friend is not muted): D − 2 days; D; D + 3k days (k = 1, 2, …). Applies to expenses with due dates, loans and **each loan installment**. Demo check: Movie tickets due Sun 27 Sep → Fri 25 Sep, Sun 27 Sep, Wed 30 Sep.

### 12.4 Delete account
Blocked while `totalOwed != 0 || totalOwe != 0` (the Home totals). Message amounts = Home totals without sign (demo: owe ₹1,850, owed ₹2,900).

### 12.5 Export
Range → default ticks → build PDF/CSV → share sheet (§9). Demo "This month": 1 Sep – 30 Sep 2026; ticked Goa Trip, Flat 302, Build a Drone, Without a group; unticked College Gang, Dubai Weekend.

### 12.6 Recently deleted
Items restorable for 30 days ("Deleted items can be restored for 30 days."). Value on Privacy = count ("1 item" in the demo).

### 12.7 Debug start screens (flow.md hooks; seed the demo dataset, "today" = Wed 30 Sep 2026)
| `startScreen` | Seed / state to show |
|---|---|
| `paywall` | Free plan; paywall presented over Profile (continue target = Profile). |
| `proWelcome` | Yearly trial started today (trialEndsAt = Wed 7 Oct); continue target = Export records. |
| `paymentDetails` | Demo methods (arjun@okaxis primary, HDFC Bank ···· 4821), Show to friends On. |
| `paymentAddUpi` | Payment details + sheet open on "UPI ID", field focused with "arjun@okhdfcbank". |
| `paymentAddUpiError` | Same, field "arjunokhdfcbank", error shown. |
| `settingsCurrency` | INR default, per-currency Off. |
| `settingsNotifications` | All six push types On, all three schedule checks On, no muted friends. |
| `privacyData` | Free plan (Pro badge visible), 1 item in Recently deleted. |
| `privacyExport` | **Pro** (toggle the entitlement on), range This month, default ticks. |
| `privacyDeleteBlocked` | Privacy & data with the "Settle up first" alert open (demo balances open). |
| `helpFeedback` | Static. |

---

## 13. Art and assets

### Art (illustrations / pictures)
| Frame | Art in Figma | Verdict |
|---|---|---|
| Paybak Pro — Welcome | `Illustration / All set` (241.33 × 200) | **Same art as `paybak-allset.riv`** (the All set trio + check; overlaid on `ref/allSet.png` at 2/3 scale: identical). Reuse `paybak-allset.riv` at 241.33 × 200 (§3). No export. |
| Paywall | Brand / App Mark 96 | Existing: `assets/brand/app-mark.svg` (or `app-mark-96.svg`). |
| Payment details / Add UPI sheet (background) | Art / Peep Head / Arjun in Avatar / Circle 40 | Existing: `assets/avatars/avatar-1.svg` (preset 0). |
No other illustrations or raster images on this page. **No new art files were exported.**

### Icons
Every icon used on this page already exists in `assets/icons/` (checked against `assets/icons/INDEX.md`): `sparkles`, `camera`, `chart`, `repeat`, `download`, `check`, `chevron-left`, `chevron-right`, `wallet`, `bank`, `plus`, `copy`, `lock`, `alert`, `exchange`, `bell`, `search`, `people`, `restore`, `delete`, `help`, `mail`, `star`. **No new icons were exported.** Kit glyphs (xmark ✕) are SF Symbols on iOS; on Android use `close.svg` (components-home.md §14).

### Files added by this spec
- `screens-settings.md` (this file).
- `ref/paywall.png`, `ref/proWelcome.png`, `ref/paymentDetails.png`, `ref/paymentAddUpi.png`, `ref/paymentAddUpiError.png`, `ref/settingsCurrency.png`, `ref/settingsNotifications.png`, `ref/privacyData.png`, `ref/privacyExport.png`, `ref/privacyDeleteBlocked.png`, `ref/helpFeedback.png`: 804 × 1748 (2×) crops of each frame from a 2× export of its Figma section (crop edges verified pixel-exact against the section background).

---

## 14. Open questions / decisions (implement the proposals unless product says otherwise)
1. **Not designed, proposed here**: Monthly small print and Welcome body; the "already Pro" paywall state; Restore purchases behaviour; tapping an existing payment method (Make primary / Copy / Remove); the "Show to friends" Off preview; the Bank account tab fields; UPI validation beyond "missing @"; the currency picker from Currency; the Muted friends screen; Last 3 months / All time ranges; Export file formats and contents; Delete account when nothing is owed; FAQ answers; Contact us address.
2. **Support email** for "Contact us" doesn't exist in Figma: needs a real address (kept in one constant).
3. **Check rows** are described as "single select" in the component, but every Check list on this page is multi-select (reminder schedule, export groups). Implemented as multi-select.
4. **Paywall prices** were fixed ₹ strings (mock store). Now that RevenueCat powers the paywall, prices come from the store products (see `docs/revenuecat.md`).
5. **Legal links** on the paywall ("Terms", "Privacy") have no URLs yet (same as Get Started).
6. The README §5.1 #6 proposal for the UPI error ("Enter a UPI ID like name@bank." with a period) is superseded by Figma: "Enter a UPI ID like name@bank" (no period, Caption/1, red alert icon).
7. `Row / Currency` on the Currency screen shows the **code as the title** ("INR") and the **name as the subtitle** ("Indian rupee", lower-case r), the reverse of Setup 2 ("Indian Rupee" / "INR"). Follow each screen's copy.

---

## 15. How Figma was read
The Figma MCP was heavily throttled during this read (Education plan), so reads were batched:
- 1 × `use_figma` page overview (sections, frames, captions verbatim).
- 2 × `use_figma` compressed node dumps covering all 11 frames (dumps A and B; everything in §1–§11 comes from these).
- 1 × `get_design_context` on the Paybak Pro section (Figma returns sparse metadata for sections).
- 1 × `get_screenshot` of the Paybak Pro section (1×) and `download_assets` section exports at scale 2 for the reference crops (one call per section; the Help ref was copied from the REST render of 177:24773). All 11 refs were cross-checked against the REST renders (`.figma-cache/renders/<id>.png`): mean pixel difference ≤ 0.08/255 (anti-aliasing only).
- Designer notes were cross-checked against `designer-notes.md` (identical).
- **No per-frame `get_design_context` output was saved** for these 11 frames: the per-frame calls were skipped in favour of the complete Plugin-API dumps, which carry more exact data than the generated React (exact frame coordinates, token names, text styles, component variants and properties, reactions). The dumps weren't kept; `.figma-cache/nodes/77-106.json` (regenerate with `tools/fetch_figma.py`) has the same node data.
