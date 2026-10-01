# Paybak app component library, part 3 (components-app.md)

Scope: Figma page **"02 Components" (3:3)**, every component set that `components-core.md` and `components-home.md` don't cover. That's six sections: **Shared (from Profile) 90:667**, **Forms & Money 115:849**, **Lists & Detail 116:872**, **Progress & Charts 116:1060**, **Assistant & Scan 117:958** and the new pieces of **Feedback & Overlays 118:962**, plus the new **Art / Receipt** in Illustrations & Art (7:2). §8 lists what changed in the components the first spec already covered.

Sources (all read-only, file `2SPNUpHlG8bCO62YfwRuRi`, read on 30 Sep–1 Oct 2026):
- Plugin API dumps, decoded, one per section (Shared, Forms & Money, Lists & Detail, Progress & Charts, Assistant & Scan, Feedback, Art / Receipt), plus a lean re-dump of the old sets used for §8. Every number in this file comes from those dumps. They weren't kept; the same values are in the REST node JSON below.
- 2× renders of every set: `.figma-cache/components/<id with - for :>.png` (for example `.figma-cache/components/127-2252.png` = Row / Person; regenerate with `tools/fetch_figma.py`). Keep the render open next to the section you implement. REST node JSON for the whole page: `.figma-cache/nodes/3-3-a.json`, `3-3-b.json`.
- Designer notes on the canvas (captions under each set): `designer-notes.md`, copied verbatim in §9.

Related: `tokens.md` (token values), `foundations-rules.md` (usage rules), `components-core.md` (buttons, badge, avatars, input field, divider, segmented control), `components-home.md` (rows, cards, tab bar, sheets, materials). Screen specs say where each component is placed.

## 0. Conventions and cross-cutting rules

- Units are **pt** (iOS) = **dp** (Android). Coordinates `(x, y)` are **relative to the component's (variant's) top-left**. Screens place the components.
- Colours: token names without the `color/` prefix, with hex (see `tokens.md`). Text styles: `tokens.md` names. For example `Headline` = Manrope SemiBold 16/22, −0.25 %.
- Auto-layout: **H**/**V** stack, padding `[top, right, bottom, left]`, "hug" = size to content, "fill" = take the remaining space. `radius/full` (999) = capsule/circle.
- **Designer's screen codes.** Descriptions refer to screens as `NN-MM` = Figma page NN, frame MM (e.g. `06-05` = page 06 Add & Record, the 5th frame, the split editor). The screen specs use the same codes next to their screen ids.
- **SwiftUI names** come from each description (`SwiftUI: PBPersonRow`). Use the same name for the Composable (`PBPersonRow(...)`) so both platforms match.
- **Nothing in these sections has a Figma pressed state** unless a `State=Pressed/Focused` variant is listed. Two components have prototype reactions (Card / Confirm Payment, Chat / Draft Expense: `ON_CLICK → CHANGE_TO …, SMART_ANIMATE 250 ms EASE_OUT`). No other node in these sections has a reaction. Tappable rows/cards: use the README §3 rule 11 pressed feedback. The suggested fill is `bg/card-pressed` #EBEBEB (rows on white) or a 6 % `bg/selected` overlay (rows inside #F5F5F5 cards), swapped while the finger is down.
- **"White on a card".** Inside a #F5F5F5 card, nested avatar circles, pills, chips, fields and buttons switch to white (`bg/primary`), exactly as the Cards & Rows caption says. Where a component has both surfaces, its variants encode it (e.g. Row / Person Size=Compact). Otherwise the screen overrides the nested fill, and each section below says so.
- **Signs are never part of an amount property.** Variants with a sign draw a separate text: "+" (U+002B, Owed) or "−" (**U+2212**, Owe) in the same style and colour, gap 0, before the value. Keep `₹` (U+20B9), `·` (U+00B7) and curly ’ (U+2019) exactly as quoted.
- **Bar lengths and chart heights are drawn as padding overrides** in Figma: fixed pixel paddings on full-width frames. In code, compute them from a fraction `p` (0…1) and the live width: `fillWidth = W × p`. Never hard-code the pixel values from the sample.
- **Icon strokes (finding, see §11 #1).** In these new components the nested 16 pt and 20 pt icon instances keep **stroke 1.5**. Examples: the 16 pt checks in Row / Person Select On and Row / Split Person, the 16 pt Plus in Category Chip, the 20 pt Close in Row / Person Remove, Arrow Right in Payment Parties, Alert in Card / Split Total. The older components scale the stroke with the icon (16 pt → 1.0, 20 pt → 1.25, e.g. Card / Balance, Avatar / Circle, Button / Primary). Until the designer answers, follow the README rule (scale the 24 × 24 SVG, so 16 pt → 1.0) everywhere. The difference is 0.5 pt at 16 pt.
- **Kit parts** (Apple iOS 27 UI kit instances in Figma): `Toggle - Switch`, `Stepper`, `Button - Liquid Glass - Symbol` (the glass xmark), `Grabber`. On iOS use the real system control. On Android draw the look described here. SF Symbols glyphs in the kit (􀅽 minus, 􀅼 plus, 􀆄 xmark) are system glyphs, not Manrope text.

## 0.1 Index: every component covered here

| § | Component (set id) | Variants / key properties | SwiftUI name | 2× render in `.figma-cache/components/` |
|---|---|---|---|---|
| 1.1 | Control / Category Chip (64:4185) | Selected False·True × Leading None·Icon·Avatar; Label, Icon, Show remove | `PBCategoryChip` | `64-4185.png` |
| 1.2 | Row / Setting (97:996) | Trailing Chevron·Toggle·Stepper·Check·Unchecked·None × Tone Default·Destructive; Title, Value, Subtitle, Icon, Show icon/value/subtitle/badge/divider | `PBSettingRow` | `97-996.png` |
| 1.3 | Navigation / Push Header (97:1082) | Trailing Text·Icon·None·Wide Text; Title, Action, Show title | `PBPushHeader` | `97-1082.png` |
| 1.4 | Overlay / Alert (102:1115) | Action Destructive·Primary; Title, Message | `PBAlert` | `102-1115.png` |
| 2.1 | Navigation / Modal Header (115:886) | Action Enabled·Disabled·None; Title, Show title | `PBModalHeader` | `115-886.png` |
| 2.2 | Control / Text Area (115:9936) | State Default·Focused; Label, Value, Helper, Show label/helper | `PBTextArea` | `115-9936.png` |
| 2.3 | Control / Composer (115:907) | State Empty·Typing × Pinned False·True; Text, Show mic | `PBComposer` | `115-907.png` |
| 2.4 | Control / Amount Display (125:1084) | State Empty·Focused·Filled; Amount, Helper, Show helper, Show date chip | `PBAmountField` | `125-1084.png` |
| 2.5 | Control / Payment Parties (125:1085) | (single) From/To label + name | `PBPaymentParties` | `125-1085.png` |
| 2.6 | Row / Split Person (126:1596) | Mode Equally·Exact·Percent·Shares × State Default·Focused·Excluded; Name, Amount, Percent, Shares, Show divider | `PBSplitRow` | `126-1596.png` |
| 2.7 | Card / Split Total (125:1170) | State Balanced·Error; Left, Detail | `PBSplitTotalBar` | `125-1170.png` |
| 2.8 | Card / Plan (125:1198) | Selected True·False; Period, Price, Detail, Show badge | `PBPlanCard` | `125-1198.png` |
| 3.1 | Avatar / Pair (116:1005) | Size 32·56 | `PBAvatarPair` | `116-1005.png` |
| 3.2 | Row / Comment (116:1007) | (single) Name, Date, Text | `PBCommentRow` | `116-1007.png` |
| 3.3 | Row / History (116:1058) | Position Middle·Last; Text, Date | `PBHistoryRow` | `116-1058.png` |
| 3.4 | Row / Person (127:2252) | Size Regular·Compact × Trailing Owed·Owe·Value·Muted·Check·Select On·Select Off·Remove·Button·None (20) | `PBPersonRow` | `127-2252.png` |
| 3.5 | Row / Transfer (128:1604) | (single) Title, Amount, Show divider | `PBTransferRow` | `128-1604.png` |
| 3.6 | Header / Title Row (128:1857) | Leading Tile·Avatar; Title, Subtitle, Show subtitle/tag/members | `PBTitleHeader` | `128-1857.png` |
| 3.7 | Header / Amount Hero (128:2004) | Leading Icon·Avatar·Pair; Title, Amount, Meta, Show chips/chip 2/chip 3 | `PBAmountHero` | `128-2004.png` |
| 3.8 | Card / Notice (129:1976) | Layout Leading·Centered × Actions None·One·Two; Title, Body, Show title/badge | `PBNoticeCard` | `129-1976.png` |
| 3.9 | Card / Confirm Payment (129:2060) | State Pending·Confirmed (prototype: Confirm → Confirmed) | `PBConfirmPaymentCard` | `129-2060.png` |
| 3.10 | Card / QR Code (130:1912) | (single) Show mark | `PBQRCodeCard` | `130-1912.png` |
| 3.11 | Row / Group (139:2052) | Type Group·Project·Archived × Balance Owe·Owed·Settled (7) | `PBGroupRow` | `139-2052.png` |
| 4.1 | Control / Progress Bar (116:1099) | Size Small·Large × State Default·Projected·Over; Show mark | `PBProgressBar` | `116-1099.png`, marks `116-1101.png` |
| 4.2 | Row / Bar (143:2156) | Leading Icon·Avatar × Value Neutral·Owed·Owe | `PBBarRow` | `143-2156.png` |
| 4.3 | Chart / Monthly Bars (143:2157) | (single) Month 1–6 | `PBMonthlyBarChart` | `143-2157.png` |
| 4.4 | Card / Budget (145:2106) | State On track·Over budget·Closed | `PBBudgetCard` | `145-2106.png`, example `159-11440.png` |
| 4.5 | Card / Loan Progress (145:2155) | State Active·Paid back | `PBLoanProgressCard` | `145-2155.png`, example `145-2156.png` |
| 5.1 | Chat / Bubble (117:971) | Role User·Assistant; Text, Show avatar | `PBChatBubble` | `117-971.png` |
| 5.2 | Chat / Draft Expense (147:2315) | State Pending·Saved (prototype: Save → Saved) | `PBDraftExpenseCard` | `147-2315.png` |
| 5.3 | Row / Receipt Line (117:993) | Style Default·Total × State Default·Editing | `PBReceiptLineRow` | `117-993.png` |
| 5.4 | Row / Assign Item (147:2532) | Shared False·True | `PBAssignItemRow` | `147-2532.png` |
| 5.5 | Card / Person Totals (147:2533) | (single) | `PBPersonTotalsCard` | `147-2533.png` |
| 5.6 | Control / Shutter (117:1001) | State Default·Pressed | `PBShutterButton` | `117-1001.png` |
| 6.1 | Overlay / Toast (118:965) | (single) Label, Icon, Show icon | `PBToast` | `118-965.png` |
| 6.2 | Sheet / Container (118:1017), populated example | changed, see §8.3 | `PBSheet` | `118-1017.png`, `118-10155.png` |
| 7.1 | Art / Receipt (86:730) | Size Full·Thumb | `PBReceiptThumbnail` | `86-730.png` |

---

## 1. Shared (from Profile) (90:667)
Section note (verbatim): “Moved from 05 Profile with their node IDs kept, then extended additively. Page 05 instances stay linked and look the same.” Captions: “Settings row”, “Push header”, “Category chip”, “Alert”, “Icons”. The three icons in this section (Shuffle 64:3957, Lock 64:3963, Help 64:3969) are already exported as `assets/icons/shuffle.svg`, `lock.svg`, `help.svg`.

### 1.1 Control / Category Chip (64:4185). SwiftUI: `PBCategoryChip`. NEW
> “Filter and people chip, 36 tall. Selected = inverse fill. Leading=None: text only (avatar categories, currency, date). Leading=Icon: 16 icon swap (the “Add” chip uses Plus). Leading=Avatar: exposed Avatar / Circle 24 for people pickers (Selected=False gives it a white avatar circle so the face reads on the #F5F5F5 chip). Show remove adds a trailing Close 16. SwiftUI: PBCategoryChip”

Properties: `Label` text ("Hair") · `Icon` instance swap (default Icon / Plus; the preferred values are the whole 64-icon set) · `Show remove` boolean (false) · `Selected` = False | True · `Leading` = None | Icon | Avatar (6 variants).

Container (all variants): H auto-layout, **height 36** (`size/button-sm`), width hugs, capsule (`radius/full`), items centred on both axes, no stroke, no shadow.
| Leading | Padding [t, r, b, l] | Gap | Default size ("Hair") | Leading element |
|---|---|---|---|---|
| None (64:4181 / 64:4183) | [0, 16, 0, 16] (`space/16`) | 6 (`space/6`) | 61 × 36 | none; label at (16, 8) |
| Icon (95:733 / 95:772) | [0, 16, 0, **12**] (`space/12` left) | 6 | 79 × 36 | `icon` 16 × 16 (`size/icon-sm`) at (12, 10); label at (34, 8) |
| Avatar (95:739 / 95:778) | [0, 16, 0, **6**] (`space/6` left) | **8** (`space/8`) | 83 × 36 | `avatar` = `Avatar / Circle` Size=24 Type=Art (default art Priya) at (6, 6); label at (38, 8) |

| Selected | Fill | Label (`Button/Small`, SemiBold 15/20, −0.25 %) | Icon tint | Avatar circle fill (override) |
|---|---|---|---|---|
| False | `bg/card` #F5F5F5 | `text/primary` #0A0A0A | `icon/primary` #0A0A0A | **`bg/primary` #FFFFFF** (the face reads on the grey chip) |
| True | `bg/inverse` #0A0A0A | `text/inverse` #FFFFFF | `icon/inverse` #FFFFFF | `bg/card` #F5F5F5 |

- `remove`: `Icon / Close` 16 × 16 after the label (same gap), hidden unless `Show remove`. Tint = the label colour (inferred, because the hidden layer has no readable tint). With it shown the chip grows by 22 (None/Icon) or 24 (Avatar). Tapping ✕ removes the chip.
- Tap toggles `Selected`. The screen decides single- or multi-select (e.g. Row / Assign Item uses three independent chips). There's no pressed variant. Suggested pressed fill: `bg/card-pressed` #EBEBEB when unselected, `bg/inverse-pressed` #2B2B2B when selected.
- Chips sit in horizontal rows; the screen specs say whether they wrap or scroll. The 36-pt chip is below the 44-pt minimum, so extend the hit area by 4 above and below.
- Nested uses: Control / Amount Display ("INR", "Today" chips, Leading=None), Row / Assign Item (Leading=Avatar "You"/"Esha"/"Dev").

### 1.2 Row / Setting (97:996). SwiftUI: `PBSettingRow`. NEW
> “Settings list row for #F5F5F5 card groups, 56 tall (grows with a subtitle). Trailing: Chevron (push), Toggle (exposed kit switch; On track tinted color/bg/inverse), Stepper (exposed kit stepper), Check (black tick, single select), Unchecked (empty 24 slot), None. Tone=Destructive: text/destructive + icon/destructive, never a chevron. Optional icon (Show icon off moves the title and the divider inset to 16), value, subtitle, and an exposed Badge / Pill (Inverse “Pro” / “Try free”). Turn Show value off on Toggle, Stepper, Check and Unchecked rows. SwiftUI: PBSettingRow”

Each variant also carries the older description “Settings list row inside a bg/card group (Profile). Icon swap, Headline title, optional secondary value, tertiary chevron, divider inset to the title. SwiftUI: PBSettingsRow”. Use one name: `PBSettingRow`.

Properties: `Title` ("Payment details") · `Value` ("UPI") · `Show value` (true) · `Icon` instance swap (default **Icon / Wallet**, 64 preferred) · `Show icon` (true) · `Subtitle` ("Paid back in parts") · `Show subtitle` (false) · `Show badge` (false) · `Show divider` (true) · `Trailing` = Chevron | Toggle | Stepper | Check | Unchecked | None · `Tone` = Default | Destructive. That's 12 variants. Destructive+Chevron exists in Figma, but the rule says a destructive row never shows a chevron.

Geometry (all variants): **362 wide, min height 56** (hugs taller with a subtitle), **no fill** (the row sits inside a `bg/card` #F5F5F5 radius-20 group that the screen draws), H auto-layout, padding **[12, 16, 12, 16]**, gap **12**, items centred vertically.
1. `icon` 24 × 24 at (16, 16), `Icon` swap. Tint `icon/primary` (Default) or **`icon/destructive` #C93636** (Destructive). Hidden when `Show icon` = false, and the text then starts at x 16.
2. `text` column at (52, 17), **fills**, V gap 2, clips:
   - `title` **Headline**, `text/primary` (Default) or **`text/destructive`** (Destructive), 1 line, truncates at the end.
   - `subtitle` at (52, 41) **Footnote**, `text/secondary`, wraps. Hidden unless `Show subtitle`. One subtitle line makes the row 12 + 22 + 2 + 18 + 12 = 66 tall.
3. `badge` (hidden; `Show badge`): `Badge / Pill` **Style=Inverse**, label "Pro" (or "Try free"), 40 × 24, before the value.
4. `value` **Body** (Regular 16/24), `text/secondary`, hugs, 1 line; hidden unless `Show value` ("UPI" is 25 × 24).
5. Trailing element (right edge x 346):
   | Trailing | Element |
   |---|---|
   | Chevron (64:4288) | `Icon / Chevron Right` **20 × 20** at (326, 18), `icon/tertiary` #A3A3A3 |
   | Toggle (97:803) | kit **`Toggle - Switch`** (State=Idle, Is On=True, Enabled), **64 × 28** at (282, 14), capsule; **the On track is tinted `bg/inverse` #0A0A0A**, knob white. iOS: `Toggle("", isOn:).labelsHidden().tint(Color.bgInverse)`. Android: a custom 64 × 28 dp switch (track #0A0A0A on / #E9E9EA off, white knob with a soft shadow) or Material3 `Switch` recoloured, keeping the 64 × 28 footprint. |
   | Stepper (97:822) | kit **`Stepper`** 92 × 32 at (254, 12), capsule (radius 100, smoothing 0.6), − and + halves split by a hairline. iOS: `Stepper`. Android: a 92 × 32 dp capsule, fill #767680 @ 12 % (the iOS tertiary fill), two 46-wide halves with a 1 × 18 divider, − / + glyphs 17 pt #0A0A0A. |
   | Check (97:845) | `Icon / Check` **24 × 24** at (322, 16), `icon/primary` (single-select lists) |
   | Unchecked (97:862) | an empty 24 × 24 slot, so titles line up with checked rows |
   | None (97:878) | nothing; the text column runs to x 309 (with the value shown) |
   The text column width left over is: Chevron 225 · Toggle 181 · Stepper 153 · Check/Unchecked 221 · None 257 (with "UPI" shown).
6. `divider row` (absolute, bottom, at (0, 55) 362 × 1): an `icon inset` spacer 36 wide (only when `Show icon`) + `Divider / Line` Inset=Leading (inset overridden to 16), so the `border/subtle` #EBEBEB hairline starts at **x 52** with an icon and **x 16** without. Hide it on the last row of a group (`Show divider` = false).
- The whole row is the tap target. Chevron pushes; Check/Unchecked selects; Toggle flips the switch; Destructive asks for confirmation with `Overlay / Alert`. There's no pressed variant: use the 6 % overlay, clipped to the group's rounded shape on the first and last rows.
- Groups: rows stack with no gap inside one `bg/card` radius-20 container. The screen specs give the group headers. The Sheet / Container example (§6.2) shows three rows in a card: Food (Check, Icon / Food), Travel (Unchecked, Icon / Car), Stays (Unchecked, Icon / Bed, no divider).

### 1.3 Navigation / Push Header (97:1082). SwiftUI: `PBPushHeader`. NEW
> “Pushed-screen header, 44 tall: glass back button, centred Headline title (Show title), and a trailing action. Trailing=Text: glass capsule text action (Save). Trailing=Icon: exposed glass Button / Icon (default Settings; gear on 10-01). Trailing=Wide Text: for long text actions (“Mark all read”): the capsule has 12 side padding and a 122 max width, and the centred title box is 102 wide and truncates, so at least 8 pt always separates title and action. Trailing=None: back only. Tall scrolling frames set the instance to fix position and give it a color/bg/primary fill. Built with .navigationTitle and a toolbar. SwiftUI: PBPushHeader”

Variant description (all four): “Pushed-screen header: glass back button, centred title, glass capsule text action (Save). SwiftUI: .navigationTitle + toolbar(Save)”.

Properties: `Title` ("Edit avatar") · `Action` ("Save") · `Show title` (true) · `Trailing` = Text | Icon | None | Wide Text.
Container: **362 × 44**, H auto-layout, `SPACE_BETWEEN`, items centred vertically, no fill (on long scrolling screens the screen pins it and gives it a `bg/primary` fill). Place it at x 20, y = the top safe area, like every header.
- `Back` (all variants): `Button / Icon` **Style=Glass**, Icon = `Icon / Chevron Left`, 44 × 44 at (0, 0): `bg/glass` #FFFFFF @ 72 % + 1-pt inside stroke `border/glass-highlight` #FFFFFF @ 60 % + `Material/Glass Small`. Tap = pop (same as system back / edge swipe).
- `title` (absolute): **Headline**, `text/primary`, centre-aligned. Box **200 wide at (81, 11)** (centre x 181 = the header centre) for Text / Icon / None; **102 wide at (130, 11), 1 line, truncates** for Wide Text. Visible when `Show title`.
- Trailing:
  | Trailing | Element | Geometry |
  |---|---|---|
  | Text (64:4245) | glass capsule `Action`: H, **height 44**, width hugs, **min width 44**, padding [0, 16, 0, 16], `radius/full`, fill `bg/glass` @ 72 %, 1-pt inside stroke `border/glass-highlight`, `Material/Glass Small`; `label` **Headline** `text/primary` ("Save") | "Save" → 71 × 44 at (291, 0); label at (308, 11) |
  | Icon (97:1059) | `Button / Icon` Style=Glass, Icon = `Icon / Settings` (swap per screen) | 44 × 44 at (318, 0) |
  | None (97:1074) | back + title only | – |
  | Wide Text (213:28488) | same capsule with padding [0, **12**, 0, **12**], **max width 122**, label 1 line, truncates | "Save" → 63 × 44 at (299, 0) |
- iOS: `NavigationStack` + `.navigationTitle(title)` + `.navigationBarTitleDisplayMode(.inline)` + `ToolbarItem(placement: .topBarTrailing)`. The iOS 26+ toolbar draws the glass capsules and the glass back button itself. If you draw it yourself, use `.glassEffect(.regular.interactive(), in: .capsule)`. Android: a custom 44-dp row with the glass fallback (fill #FFFFFFB8, 1 dp #FFFFFF99 inside border, shadow y 4 blur 16 #0A0A0A @ 8 %). `BackHandler` = back button.
- Pressed: the glass button pressed state = `bg/card` #F5F5F5 (from Button / Icon). Use the same for the text capsule (no variant in Figma).

### 1.4 Overlay / Alert (102:1115). SwiftUI: `PBAlert`. NEW
> “iOS 27 alert card, 300 wide, radius 34: Headline title, Subheadline message, two 36pt capsules sharing the width (both exposed). Action=Destructive: Button / Secondary + Button / Destructive (Discard, Delete). Action=Primary: Button / Secondary + Button / Primary when nothing is destroyed (Settle up). Built with .alert: a .cancel button plus a .destructive or default button. SwiftUI: PBAlert”

Variant description (both): “iOS 27 alert card (Discard changes?). Keep editing = Button / Secondary, Discard = Button / Destructive; both exposed. SwiftUI: .alert with .cancel + .destructive roles”.

Properties: `Title` ("Discard changes?") · `Message` ("Your avatar edits won’t be saved.", curly ’) · `Action` = Destructive | Primary.
Geometry (both variants, 64:4257 / 102:1098): **300 × 142** (height hugs), V auto-layout, padding **20** all round, gap **20**, children centred horizontally, **radius 34** (a literal, not a token), fill `bg/primary` #FFFFFF, effect **`Material/Glass`** (drop shadow 0, 8, blur 32, #0A0A0A @ 10 % + GLASS radius 16). No stroke.
1. `Text` at (20, 20), 260 × 46 (fills the width, hugs its height), V gap 4:
   - `title` 260 × 22, **Headline**, `text/primary`, centred, wraps.
   - `message` at (20, 46) 260 × 20, **Subheadline**, `text/secondary`, centred, wraps (grows).
2. `Actions` at (20, 86), 260 × 36, H gap 8. Two **Small** pill buttons, each **fills → 126 × 36**:
   | Action | `cancel` (left) | `action` (right) |
   |---|---|---|
   | Destructive | `Button / Secondary` Small "Keep editing" (`bg/card`) | `Button / Destructive` Small "Discard" (`bg/destructive` #C93636, white label) |
   | Primary | `Button / Secondary` Small "Not now" | `Button / Primary` Small "Settle up" (black) |
- Presentation (not in the component): centred on screen over the `bg/scrim` #0A0A0A @ 40 % scrim. Tapping outside does nothing, because an alert needs an explicit choice.
- iOS: the real `.alert(title, isPresented:) { Button(role: .cancel) …; Button(role: .destructive) … } message: { … }`, as the description says. On iOS 26+ it renders as this glass card with capsule buttons. Android: a custom `Dialog` that draws exactly this card (Material `AlertDialog` looks different), with `dismissOnClickOutside = false`.

---

## 2. Forms & Money (115:849)
Section note (verbatim): “Modal header, amount entry, payment parties, split rows and the split total bar, paywall plan cards, the multi-line text area and the composer (comments and Ask Paybak). Amounts use Amount/Display; red appears only on errors.”

### 2.1 Navigation / Modal Header (115:886). SwiftUI: `PBModalHeader`. NEW
> “PBModalHeader — Toolbar for full-screen modals (Add expense, Record payment, Lend money, New group, Ask Paybak, paywall): the kit glass xmark (cancel) on the LEFT, a centred Headline title and the confirmation pill on the right.
> Action=Enabled (black Button / Primary Small) · Disabled (until the form is valid) · None (Ask Paybak, paywall). The pill is the exposed nested instance “action”: set its Label#9:0 to Save / Create / Add. 362×44, placed at y 62 inside the 20pt margins. No fill, no shadow; sheets keep their xmark on the right.
> SwiftUI: PBModalHeader”

Properties: `Title` ("Add expense") · `Show title` (true) · `Action` = Enabled | Disabled | None.
Container: **362 × 44** (`size/tap` tall), H auto-layout, `SPACE_BETWEEN`, centred vertically, **no fill, no shadow**. Place at x 20, y = the top safe area (62 on the 402 × 874 frame).
- `close` at (0, 0), **44 × 44**: the kit glass xmark (**“Button - Liquid Glass - Symbol”**, Style=Glass, Enabled, not destructive), SF Symbol `xmark` (U+100184 􀆄), a circle (radius 1000, smoothing 0.6). It's the same kit button as the Add sheet close (components-home.md §14), but **44** instead of 50.
  - iOS: `Button { dismiss() } label: { Image(systemName: "xmark") }` in `ToolbarItem(placement: .topBarLeading)`; the system draws the glass circle.
  - Android: a 44-dp circle with the glass fallback (fill #FFFFFFB8, 1 dp #FFFFFF99 inside border, shadow y 4 blur 16 #0A0A0A @ 8 %) and `close.svg` at 20 dp tinted #1A1A1A, as screens-home.md recommends for the sheet ✕.
- `title` (absolute) at (81, 11), **200 × 22**, **Headline**, `text/primary`, centred, 1 line, truncates. Visible when `Show title`.
- `action` (right-aligned, y 4): `Button / Primary` **Size=Small** (36 tall, padding 0/16, capsule), label "Save" → **67 × 36 at (295, 4)**.
  - Enabled (115:852): State=Default, fill `bg/inverse` #0A0A0A, label `text/inverse`.
  - Disabled (115:865): State=Disabled, fill `bg/disabled` #E0E0E0, label `text/disabled` #A3A3A3. Not tappable until the form is valid.
  - None (115:878): no action (Ask Paybak, paywall).
  Labels used: Save / Create / Add, per screen.
- Close dismisses the modal. If the form is dirty, ask first with `Overlay / Alert` "Discard changes?" (per the screen specs). Present these screens full-screen: iOS `.fullScreenCover`, Android a full-screen destination with a slide-up transition.

### 2.2 Control / Text Area (115:9936). SwiftUI: `PBTextArea`. NEW
> “PBTextArea — Multi-line text input (TextEditor) for the Remind message (08-07) and the Not received note (08-10). Control / Input Field styling: #F5F5F5, radius 14, padding 16, Body text that wraps (3–4 lines; min height 104, hugs taller).
> State=Default · Focused (1.5 border/strong ring + caret). The “caret” layer is absolute: after overriding Value on a Focused instance, move it to the end of the last line. Label (Subheadline, secondary) and Helper (Footnote, tertiary) are optional.
> SwiftUI: PBTextArea”

Properties: `Label` ("Message") · `Show label` (true) · `Value` ("Hi Rohan! Just a gentle reminder about ₹800 for the movie tickets on 20 Sep. You can pay me on UPI at arjun@okaxis. Thanks.") · `Helper` ("You can edit this message.") · `Show helper` (true) · `State` = Default | Focused.
Geometry: **362 × 158** with label and helper (V auto-layout, gap 8):
1. `label` at (0, 0), **Subheadline**, `text/secondary`.
2. `field` at (0, 28), **362 wide, min height 104** (hugs taller), V auto-layout, padding **16** all round, fill `bg/card` #F5F5F5, radius **14** (`radius/input`), clips. `value` at (16, 16) inside, 330 wide, **Body**, `text/primary`, wraps (the sample is 3 lines = 72 tall).
3. `helper` at (0, 140), **Footnote**, `text/tertiary`.
- Focused (115:9930): a 1.5-pt **inside** stroke `border/strong` #0A0A0A on the field. Unlike Input Field, this ring does **not** push the content inwards (`strokesIncludedInLayout` is off here). The caret is `bg/inverse` 2 × 20 at the end of the text.
- Empty: show the placeholder in `text/tertiary`, as Input Field does. No Error state is designed. If one is needed, reuse the Input Field error (red ring + red helper).
- iOS: `TextField(axis: .vertical)` with `.lineLimit(3...)`, or `TextEditor` with `.scrollContentBackground(.hidden)`. Android: a multi-line `BasicTextField` in the same decoration box.

### 2.3 Control / Composer (115:907). SwiftUI: `PBComposer`. NEW
> “PBComposer — Single-line input for expense comments (09-03/04) and Ask Paybak (11-04/05/06). Input Field styling: #F5F5F5, radius 14 (input radius, not a pill), 52 tall.
> State=Empty: placeholder in text/tertiary + optional mic (Show mic; Empty only, iOS dictation fills the field and never sends). State=Typing: text/primary + caret + black send (Button / Icon Inverse, Icon / Arrow Up, 36).
> Pinned=False: the bare 362×52 field for in-content use. Pinned=True: the bar pinned on the keyboard — 402 wide (place at x 0), white (color/bg/primary), a full-width Divider / Line on top, 8 top/bottom and 20 side padding, 68 tall; y = 874 − keyboard height − 68 (09-04).
> Text = placeholder (“Add a comment”, “Ask or add an expense”) or typed text.
> SwiftUI: PBComposer”
Caption: “Composer (Pinned=True: the white 402-wide keyboard bar with a top divider)”.

Properties: `Text` ("Add a comment") · `Show mic` (true) · `State` = Empty | Typing · `Pinned` = False | True.
- **Field** (all variants): **362 × 52** (`size/button-lg`), H auto-layout, gap 8, items centred vertically, fill `bg/card` #F5F5F5, radius **14** (`radius/input`), clips.
  - Empty (115:888): padding [0, 16, 0, 16]. `text` at (16, 14), 298 × 24, **Body**, `text/tertiary` (placeholder), 1 line, truncates. `mic` = `Icon / Mic` **24 × 24** at (322, 14), `icon/secondary` #6B6B6B, visible when `Show mic` (Empty only).
  - Typing (115:895): padding [0, **8**, 0, 16]. `input` row at (16, 14), 294 × 24 (fills, H gap 2, clips): the typed `text` in **Body** `text/primary` (hugs) + `caret` 2 × 20 `bg/inverse`. `send` = `Button / Icon` **Style=Inverse** resized to **36 × 36** at (318, 8): a black `bg/inverse` circle with `Icon / Arrow Up` **24 × 24 at (6, 6)**, `icon/inverse`. Tap sends/submits, then clears the field.
- **Pinned=False** (115:888 / 115:895): just the 362 × 52 field (inside a V wrapper filled `bg/primary`).
- **Pinned=True** (159:2379 / 159:2384): a **402 × 68** bar, fill `bg/primary` #FFFFFF, V auto-layout, padding **[8, 20, 8, 20]** (`space/8`, `layout/screen-margin`), field at (20, 8). `top divider` = `Divider / Line` Inset=None, absolute at (0, 0), 402 × 1 (`border/subtle` #EBEBEB). Place it at x 0, anchored to the keyboard top: y = 874 − keyboard height − 68. With the keyboard hidden it sits above the home indicator (bottom safe area).
- Mic: on iOS, start dictation into the field; it never sends by itself. Android: `RecognizerIntent` speech-to-text into the field, or hide the mic when it's unavailable.
- iOS: `TextField` + `.safeAreaInset(edge: .bottom)` for the pinned bar (it rides the keyboard automatically). Android: `Modifier.imePadding()` on the pinned bar.

### 2.4 Control / Amount Display (125:1084). SwiftUI: `PBAmountField`. NEW
> “PBAmountField — the amount-first entry at the top of Add expense, Record payment, Lend money and the receipt review (06-01/02/11/13 · 08-04 · 11-10 · 11-13).
> Two nested Control / Category Chip instances (exposed): “currency” (INR, opens the currency sheet) and “date” (Today, opens the Date sheet). Show date chip: on for 06-01/02 and 11-10; off for 06-11, 06-13, 08-04 and 11-13.
> The amount uses Amount/Display (Manrope ExtraBold 56/64). Empty = the “₹0” placeholder in text/tertiary with the 2×56 caret (not bound to Amount); Focused = the typed Amount + caret; Filled = Amount, no caret. Helper (Footnote, secondary) e.g. “You owe Meera ₹450 in Flat 302”.
> Amount entry always uses the kit keyboard (Number Pad: the kit has no Decimal Pad), never a custom keypad.
> SwiftUI: PBAmountField”

Properties: `Amount` ("₹2,800") · `Helper` ("You owe Meera ₹450 in Flat 302") · `Show helper` (false) · `Show date chip` (true) · `State` = Empty | Focused | Filled.
Geometry: **362 × 120** (hugs; 18 taller with the helper), V auto-layout, padding [4, 0, 4, 0], gap **12**, children **centred horizontally**. No fill.
1. `chips` row (centred), H gap 8, 36 tall: `currency` = `Control / Category Chip` Selected=False, Leading=None, **"INR"** (57 × 36) → opens the currency sheet. `date` = the same chip, **"Today"** (75 × 36) → opens the date sheet; visible when `Show date chip`. With both chips the row is 140 wide at x 111.
2. `value` (at y 52), V, centred. `amount row` = H gap 2, centred:
   - `amount` **Amount/Display** (Manrope ExtraBold 56/64, −2 %), centre-aligned, hugs.
     - Empty (125:1039): the placeholder "**₹0**" in `text/tertiary` #A3A3A3 (72 × 64). It's a fixed text, not bound to Amount; use the chosen currency's symbol.
     - Focused (125:1055): the typed amount in `text/primary` ("₹2,800" = 192 × 64).
     - Filled (125:1070): the amount in `text/primary`, no caret.
   - `caret` **2 × 56**, 4 below the row top, fill `text/primary` #0A0A0A (Empty and Focused only). Blink about once a second.
   - `helper` (y 116) **Footnote**, `text/secondary`, centred; visible when `Show helper`.
- Formatting: group digits by the currency's locale (₹ uses Indian grouping: 1,00,000), put the symbol before the digits, and show decimals only if the user typed them. Input: the system **number pad** (iOS `.keyboardType(.numberPad)`, or `.decimalPad` if decimals are allowed; Android `KeyboardType.Number`), never a custom keypad. Tapping the amount focuses it.
- Shrink-to-fit isn't designed. For very long amounts, scale the font down (`minimumScaleFactor` ≈ 0.5) instead of wrapping.

### 2.5 Control / Payment Parties (125:1085). SwiftUI: `PBPaymentParties`. NEW
> “PBPaymentParties — who paid whom on Record payment (06-11, 08-04): a #F5F5F5 r20 card, 96 tall, with a From tile, Icon / Arrow Right (icon/tertiary) and a To tile. Each tile is an exposed Avatar / Circle 56 (“from avatar” / “to avatar”, white circle on the card) with a Footnote label and a Headline name, and opens a person picker. Default: You (Arjun) → Meera.
> SwiftUI: PBPaymentParties”

Properties: `From label` ("From") · `From name` ("You") · `To label` ("To") · `To name` ("Meera").
Geometry: **362 × 96**, H auto-layout, padding **[20, 16, 20, 16]**, gap **8**, items centred vertically, fill `bg/card` #F5F5F5, radius **20** (`radius/card`).
- `from` tile at (16, 20), **fills** (147 × 56), H gap 12, centred: `from avatar` = `Avatar / Circle` **Size=56 Type=Art** (Arjun, `avatar-1`), circle fill **`bg/primary`** (white); `text` column, V gap 0, fills (79 wide): `label` **Footnote** `text/tertiary` ("From"), `name` **Headline** `text/primary` ("You"), 1 line, truncates.
- `arrow` = `Icon / Arrow Right` **20 × 20** at (171, 38), `icon/tertiary` #A3A3A3.
- `to` tile at (199, 20), fills (147 × 56): `to avatar` Size=56 Art (**Meera**, `avatar-7`), white circle; `label` "To", `name` "Meera".
- Each tile is tappable and opens the person picker sheet. A swap gesture isn't designed.

### 2.6 Row / Split Person (126:1596). SwiftUI: `PBSplitRow`. NEW
> “PBSplitRow — one person in the split editor (06-05, 06-06; 10-04 Percent/Fixed editing; the Multiple people payer editor uses Exact). Row 64, inside a #F5F5F5 card: select circle (24, black with a white tick; an empty border/strong ring when Excluded), exposed Avatar / Circle 32 (white circle), Name, trailing value, inset divider.
> Mode=Equally: the Amount (Headline). Exact: an inline 96-wide color/bg/primary r14 field with the Amount. Percent: the field shows Percent; Amount is the Footnote under the name. Shares: a 48 field with Shares + the kit Stepper; Amount under the name.
> State=Focused: the field gets a 1.5 border/strong ring and the caret (Equally has no field, so Focused shows the pressed row, color/bg/card-pressed). State=Excluded: unticked, name and ₹0 / 0% / 0 in text/tertiary (fixed values, not bound to the props).
> SwiftUI: PBSplitRow”

Properties: `Name` ("Priya") · `Amount` ("₹700") · `Percent` ("25%") · `Shares` ("1") · `Show divider` (true) · `Mode` = Equally | Exact | Percent | Shares · `State` = Default | Focused | Excluded (12 variants).
Row (all variants): **362 × 64** (min height 64), H auto-layout, padding **[8, 16, 8, 16]** (`space/8`, `layout/card-padding`), gap **12**, items centred vertically, no fill (it sits in a `bg/card` card). The Equally/Focused variant is filled `bg/card-pressed` #EBEBEB.
1. `select` **24 × 24** circle at (16, 20):
   - Default/Focused: fill `bg/inverse` #0A0A0A + `Icon / Check` **16 × 16** at (4, 4), `icon/inverse` (white).
   - Excluded: no fill, **1.5-pt inside** stroke `border/strong` #0A0A0A, no tick.
2. `avatar` = `Avatar / Circle` **Size=32 Type=Art** (Priya) at (52, 16), circle fill **`bg/primary`** (white).
3. `text` column at x 96, fills, V gap 0: `name` **Headline**, `text/primary` (Excluded: `text/tertiary`), 1 line, truncates. In **Percent** and **Shares** a second line `amount` in **Footnote** `text/secondary` ("₹700") sits under the name (Excluded: "₹0" in `text/tertiary`), and the column starts at y 12 instead of 21.
4. Trailing, by Mode:
   | Mode | Trailing | Default | Focused | Excluded |
   |---|---|---|---|---|
   | Equally | `amount` **Headline** `text/primary` "₹700", hugs, right edge 346 | – | row fill `bg/card-pressed` | "₹0" `text/tertiary` |
   | Exact | `field` **96 × 36** at (250, 14): H, padding [0, 12, 0, 12], gap 2, **right-aligned** content, radius **14** (`radius/input`), fill `bg/primary`, height `size/button-sm`; `value` **Headline** `text/primary` "₹700" | – | + 1.5-pt inside ring `border/strong` + caret 2 × 20 `text/primary` after the value | value "₹0" `text/tertiary` |
   | Percent | the same 96 × 36 field showing `Percent` ("25%") | – | ring + caret | "0%" `text/tertiary` |
   | Shares | `trailing` 148 × 36 at (198, 14), H gap 8: a **48 × 36** field (same style, **centred** content) showing `Shares` ("1") + the kit **Stepper** 92 × 32 at (254, 16) | – | ring + caret in the 48 field | value "0" `text/tertiary`, Stepper at **40 % opacity** |
   Name column width: Equally 199 (Excluded 217), Exact/Percent 142, Shares 90.
5. `divider` = `Divider / Line` Inset=None, absolute at (96, 63), 266 × 1: the hairline starts at the name (x 96). Hide it on the last row.
- Behaviour: tapping the select circle toggles inclusion. Excluded people get ₹0 and are left out of the split. In Exact and Percent, typing edits the value (number pad), and the Card / Split Total footer shows what's left. In Shares, the stepper adds or removes shares (minimum 0). Equally recomputes amounts from the total ÷ included people (see the screen spec for rounding).
- Stepper on Android: a 92 × 32 dp capsule, #767680 @ 12 % fill, − | + halves with a hairline divider (as in Row / Setting).

### 2.7 Card / Split Total (125:1170). SwiftUI: `PBSplitTotalBar`. NEW
> “PBSplitTotalBar — the live footer of the split editor (06-05, 06-06) and the Multiple people payer editor: a #F5F5F5 r20 bar, 56 tall. Left (Amount/Medium) + Detail (Subheadline, right-aligned).
> Balanced: “₹0 left” stays gray. Error: Icon / Alert + Left in text/destructive (Bold), e.g. Left “₹150 left”, Detail “₹2,650 of ₹2,800”; Done stays disabled. Text props share one default, so set Left/Detail per instance.
> SwiftUI: PBSplitTotalBar”

Properties: `Left` ("₹0 left") · `Detail` ("₹2,800 of ₹2,800") · `State` = Balanced | Error.
Geometry: **362 × 56**, H auto-layout, padding [0, 16, 0, 16] (`layout/card-padding`), gap 8, items centred vertically, fill `bg/card` #F5F5F5, radius 20.
- Balanced (125:1160): `left` at (16, 17), **Amount/Medium** (Bold 17/22), **`text/secondary`** #6B6B6B · `detail` fills (at (78, 18), 268 wide), **Subheadline** `text/secondary`, **right-aligned**.
- Error (125:1163): `icon` = `Icon / Alert` **20 × 20** at (16, 18), **`icon/destructive`** #C93636 · `left` at (44, 17), **`text/destructive`** #C93636 · `detail` at (106, 18), 240 wide, `text/secondary`, right-aligned. The screen's Done/Save stays disabled while this shows.
- Logic: Left = total − sum(assigned) (Exact / Percent / Shares). Balanced shows "₹0 left". Detail = "{sum assigned} of {total}". Over-assigning isn't designed: show it in the same Error style (proposal: "₹150 over").

### 2.8 Card / Plan (125:1198). SwiftUI: `PBPlanCard`. NEW
> “PBPlanCard — a paywall plan option (12-01), 171×104, radius 20, no border. Selected=True = color/bg/inverse with inverse text (the detail line at 72% opacity); Selected=False = color/bg/card. Period (Headline), Price (Amount/Medium), Detail (Footnote), Show badge + nested Badge / Pill On Card (exposed, “Save 33%”).
> Yearly (₹799/year · ₹67/month · Save 33%) is selected by default and is the only plan with the 7-day trial; Monthly = Selected=False, “₹99/month” · “Billed monthly”, Show badge=false.
> SwiftUI: PBPlanCard”

Properties: `Period` ("Yearly") · `Price` ("₹799/year") · `Detail` ("₹67/month") · `Show badge` (true) · `Selected` = True | False.
Geometry: **171 × 104**. Two cards side by side with a **20** gap fill the 362 content width: (362 − 20) / 2 = 171. V auto-layout, padding **16** all round, `SPACE_BETWEEN`, radius **20**, no border, no shadow.
1. `top` row at (16, 16), 139 × 24, H gap 8: `period` **Headline** (fills, 1 line, truncates) + `badge` = `Badge / Pill` **Style=On Card** "Save 33%" (77 × 24, white pill, `text/secondary` label), visible when `Show badge`.
2. `price block` at (16, 48), V gap 0: `price` **Amount/Medium** (Bold 17/22) + `detail` **Footnote**.
| Selected | Fill | Period / Price | Detail | Badge |
|---|---|---|---|---|
| True (125:1171) | `bg/inverse` #0A0A0A | `text/inverse` #FFFFFF | `text/inverse` at **72 % layer opacity** | white On Card pill |
| False (125:1186) | `bg/card` #F5F5F5 | `text/primary` | `text/secondary` | white On Card pill |
Sample data: Yearly = "Yearly" · "₹799/year" · "₹67/month" · badge "Save 33%" (selected by default; the only plan with the 7-day trial). Monthly = "Monthly" · "₹99/month" · "Billed monthly", no badge. Tap selects (single choice).

---

## 3. Lists & Detail (116:872)
Section note (verbatim): “People, group and transfer rows, title and amount heroes, notices, the confirm payment and QR code cards, and the comment and history rows of the expense detail. Nested avatars, badges and buttons are exposed.” Person-row caption (verbatim): “Person row (Compact variants are drawn for #F5F5F5 cards: their white avatar circles, pills and buttons are invisible on this white section)”.

### 3.1 Avatar / Pair (116:1005). SwiftUI: `PBAvatarPair`. NEW
> “PBAvatarPair — “From → To” pair for transfers and payments: two nested Avatar / Circle (exposed as “from” and “to”: set Art / Type / Initials on each) with Icon / Arrow Right in icon/tertiary between them.
> Size=32 (16 arrow, gap 4) for Row / Transfer · Size=56 (20 arrow, gap 8) for Header / Amount Hero Leading=Pair (06-12, 08-06). Default: Arjun → Kabir.
> SwiftUI: PBAvatarPair”

Property: `Size` = 32 | 56. H auto-layout, hugs, items centred vertically.
| Size | Box | Gap | `from` | `arrow` (`Icon / Arrow Right`, `icon/tertiary` #A3A3A3) | `to` |
|---|---|---|---|---|---|
| 32 (116:875) | **88 × 32** | 4 | Avatar / Circle 32 Art (Arjun, `avatar-1`) at (0, 0) | **16 × 16** at (36, 8) | Avatar / Circle 32 Art (**Kabir**, `avatar-6`) at (56, 0) |
| 56 (116:940) | **148 × 56** | 8 | Avatar / Circle 56 Art (Arjun) at (0, 0) | **20 × 20** at (64, 18) | Avatar / Circle 56 Art (Kabir) at (92, 0) |
The circles default to `bg/card` #F5F5F5 (for white surfaces). **Inside a #F5F5F5 card, set both to `bg/primary`**, as Row / Transfer does (verified in the REST JSON: its pair shows Rohan → Dev on white circles). Not interactive by itself.

### 3.2 Row / Comment (116:1007). SwiftUI: `PBCommentRow`. NEW
> “PBCommentRow — One comment on an expense (09-03, 09-04): nested Avatar / Circle 32 (exposed “avatar”), Name (Headline) with the Date (Footnote, tertiary) beside it, and the comment Text (Body) that wraps. 362 wide, 8 top and bottom padding, hugs its height; stack rows with no gap.
> SwiftUI: PBCommentRow”

Properties: `Name` ("Priya") · `Date` ("27 Sep") · `Text` ("Was breakfast included?").
Geometry: **362 wide**, height hugs (**64** with one line of text), H auto-layout, padding [8, 0, 8, 0], gap **12**, **top-aligned**.
- `avatar` = Avatar / Circle **32** Type=Art (Priya, `avatar-2`) at (0, 8), fill `bg/card` #F5F5F5.
- `content` at (44, 8), fills (318), V gap 2:
  - `meta` row, H gap **8**, **baseline-aligned**: `name` **Headline** `text/primary` ("Priya") + `date` **Footnote** `text/tertiary` ("27 Sep", 3 pt lower so the baselines match).
  - `text` **Body** `text/primary`, wraps (hugs its height).
- Height check: 8 + 22 + 2 + 24 + 8 = 64. Rows stack with no gap and no divider. Not interactive.

### 3.3 Row / History (116:1058). SwiftUI: `PBHistoryRow`. NEW
> “PBHistoryRow — One entry in an expense’s edit history (09-03; 06-10 “You added this · Today”): an 8pt dot (icon/tertiary) with a 1pt connecting line (border/subtle) down to the next entry, then Text (Subheadline, wraps) and Date (Footnote, tertiary).
> Position=Middle keeps the line and 16 bottom padding · Last hides the line and the padding. Stack rows with no gap, newest first.
> SwiftUI: PBHistoryRow”

Properties: `Text` ("Kabir changed the amount from ₹17,500 to ₹18,000") · `Date` ("28 Sep") · `Position` = Middle | Last.
Geometry: **362 wide**, H auto-layout, gap **12**, top-aligned.
- `rail` **8 wide**, the full row height, V auto-layout, padding-top **6**, gap **4**, centred horizontally:
  - `dot` = ellipse **8 × 8** at (0, 6), fill `icon/tertiary` #A3A3A3.
  - `line` = rectangle **1 wide** (`stroke/hairline`) at x 3.5, from y 18 to the bottom of the row (38 tall in Middle), fill `border/subtle` #EBEBEB. It continues into the next row's dot area.
- `content` at (20, 0), fills (342), V gap 2: `text` **Subheadline** (Medium 14/20) **`text/primary`**, wraps · `date` **Footnote** `text/tertiary`.
| Position | Size | `line` | content bottom padding |
|---|---|---|---|
| Middle (116:1044) | 362 × **56** | visible | **16** |
| Last (116:1051) | 362 × **40** | hidden | 0 |
Not interactive. Newest entry first.

### 3.4 Row / Person (127:2252). SwiftUI: `PBPersonRow`. NEW
> “PBPersonRow — the one people row (split pickers, balances, breakdowns, contacts, expense splits, chat answers). Exposed Avatar / Circle (40 Regular / 32 Compact), Name (Headline) + Show tag (exposed Badge / Pill Muted “Guest”, always next to the name), Subtitle (Subheadline), trailing, Show divider (inset to the name).
> Size=Regular: 64, avatar on color/bg/card for white surfaces (inside a #F5F5F5 card, override the avatar fill to color/bg/primary). Size=Compact: 56, avatar 32 in a white circle and an On Card tag, for use inside a card.
> Trailing: Owed = “+” + Amount (Amount/Medium, text/primary) · Owe = “−” + Amount (Amount/Medium, text/secondary) · Value = Amount (Headline, e.g. ₹700 or 25%) — these three carry Amount label (Footnote, tertiary) and Show status badge (exposed Badge / Pill Overdue “Overdue 3 days”, always under the amount) · Muted = Status (Subheadline, secondary: Settled / Added / No balance) · Check = black tick (single select, e.g. Paid by) · Select On / Select Off = 24 black circle with a white tick / empty border/strong ring (multi-select) · Remove = Icon / Close 20 (icon/secondary) · Button = exposed Button / Secondary Small “Invite” (Regular) or Button / On Card Small “Remind” (Compact) · None.
> Amount excludes the sign: the variant adds “+” or “−”. Guests: Avatar Type=Initials “AR” + Show tag.
> SwiftUI: PBPersonRow”

Properties: `Name` ("Priya") · `Subtitle` ("Dinner at Olive Garden") · `Show subtitle` (true) · `Show tag` (false) · `Amount` ("₹700") · `Amount label` ("Due Sun 4 Oct") · `Show amount label` (true) · `Show status badge` (false) · `Status` ("Settled") · `Show divider` (true) · `Size` = Regular | Compact · `Trailing` = Owed | Owe | Value | Muted | Check | Select On | Select Off | Remove | Button | None (20 variants).

Layout (both sizes): H auto-layout, gap **12**, items centred vertically, no fill.
| | Size=Regular (for white surfaces) | Size=Compact (inside #F5F5F5 cards) |
|---|---|---|
| Row | 362 × **64** (min height 64), padding [8, 16, 8, 16] | 362 × **56** (min 56), padding [**6**, 16, **6**, 16] |
| `avatar` | Avatar / Circle **40** Art (Priya) at (16, 12), fill **`bg/card`** (override to `bg/primary` inside a card) | Avatar / Circle **32** Art at (16, 12), fill **`bg/primary`** |
| `text` column | at (68, 10), fills, V gap 2 | at (60, 6), fills, V gap 2 |
| `name line` | H gap 8, centred: `name` **Headline** `text/primary`, **max width 200**, 1 line, truncates · `tag` = Badge / Pill **Muted** "Guest" (56 × 24), hidden unless `Show tag` | same, name max width **180** · `tag` = Badge / Pill **On Card** (white) |
| `subtitle` | **Subheadline** `text/secondary`, 1 line, truncates; `Show subtitle` | same |
| `divider` | Divider / Line Inset=None, absolute at (**68**, 63), 294 × 1 (starts at the name) | at (**60**, 55), 302 × 1 |
| Button trailing | `Button / Secondary` Small "Invite" (72 × 36 at (274, 14), `bg/card`) | `Button / On Card` Small "Remind" (86 × 36 at (260, 10), white) |

Trailing by variant (right edge x 346):
| Trailing | Element | Text column width (Regular) |
|---|---|---|
| Owed | `trailing` V gap 2, **right-aligned**: `amount` row = "+" + value in **Amount/Medium** `text/primary` ("+₹700") · `amount label` **Footnote** `text/tertiary` ("Due Sun 4 Oct"; `Show amount label`) · `status badge` Badge / Pill **Overdue** "Overdue 3 days" (110 × 24, hidden; `Show status badge`), under the label | 180 |
| Owe | same, "−" (U+2212) + value in **Amount/Medium `text/secondary`** ("−₹700") | 180 |
| Value | same column, value **Headline** `text/primary` ("₹700" or "25%"), no sign | 180 |
| Muted | `status` **Subheadline** `text/secondary` ("Settled"; also "Added", "No balance") | 218 |
| Check | `Icon / Check` 24 × 24, `icon/primary` (single select) | 242 |
| Select On | **24 × 24** circle `bg/inverse` + `Icon / Check` 16 × 16 at (4, 4), `icon/inverse` | 242 |
| Select Off | 24 × 24 circle, **1.5-pt inside** stroke `border/strong` #0A0A0A, no fill | 242 |
| Remove | `Icon / Close` **20 × 20**, `icon/secondary` #6B6B6B | 246 |
| Button | the exposed small button (see above) | 194 |
| None | nothing | 278 |
- With the status badge shown the row grows: 8 + (22 + 2 + 18 + 2 + 24) + 8 = 84 (Regular).
- Guests: Avatar Type=Initials ("AR") + `Show tag` with "Guest".
- Tap: the whole row (push to the person, or select, per the screen). Remove/Button have their own tap targets (make the 20-pt ✕ a 44 × 44 hit area).

### 3.5 Row / Transfer (128:1604). SwiftUI: `PBTransferRow`. NEW
> “PBTransferRow — one payment in a settle-up plan (10-01 “Who owes whom”, 10-05 final plan): exposed Avatar / Pair 32 (from → to, white circles for a #F5F5F5 card), Title (Headline, “Rohan owes Dev” / “Rohan pays Dev”), Amount (Amount/Medium), Show divider (inset to the title; off on the last row). 64 tall.
> SwiftUI: PBTransferRow”

Properties: `Title` ("Rohan owes Dev") · `Amount` ("₹8,500") · `Show divider` (true).
Geometry: **362 × 64** (min 64), H auto-layout, padding [8, 16, 8, 16], gap **12**, centred vertically, no fill (it sits in a #F5F5F5 card).
- `avatars` = `Avatar / Pair` Size=32 (88 × 32) at (16, 16); from = Rohan, to = Dev, **both circles `bg/primary`**.
- `title` at (116, 21), fills (160), **Headline** `text/primary`, 1 line, truncates.
- `amount` at (288, 21), hugs, **Amount/Medium** `text/primary` ("₹8,500").
- `divider` = Divider / Line Inset=None, absolute at (116, 63), 246 × 1 (starts at the title). Off on the last row.

### 3.6 Header / Title Row (128:1857). SwiftUI: `PBTitleHeader`. NEW
> “PBTitleHeader — the content header under a Push Header on group, friend and project detail (06-19 · 07-04/07/08/11 · 10-01 · 10-06), always 16 below the header. Left-aligned: a 56 leading, then Title (Title/2) with Show tag (exposed Badge / Pill Muted: “Guest”, “Archived”) and Subtitle (Subheadline, secondary; Show subtitle off on 07-11).
> Leading=Tile: exposed Avatar / Circle 56 Type=Icon (group type: Plane, Home, People, Tag; projects: Drone, Package). Leading=Avatar: exposed Avatar / Circle 56 (Art, or Initials “AR” for a guest).
> Show members: exposed Avatar / Stack (Count 2–4) 8 below the meta, so the row is 96 tall with members and 56 without; a title-only meta is centred on the 56 leading.
> SwiftUI: PBTitleHeader”

Properties: `Title` ("Goa Trip") · `Subtitle` ("21–25 Sep · 5 members · ₹39,500 spent", en dash U+2013 and · U+00B7) · `Show subtitle` (true) · `Show tag` (false) · `Show members` (true) · `Leading` = Tile | Avatar.
Geometry: **362 × 96** (with members) / **362 × 56** (without), H auto-layout, gap **16**, top-aligned. Place it 16 below the Push Header.
- Leading (at (0, 0), 56 × 56): Tile (128:1670) = `Avatar / Circle` **56 Type=Icon**, Icon = **Plane** (24 pt icon at (16, 16)), fill `bg/card`. Avatar (128:1752) = `Avatar / Circle` **56 Type=Art** (Rohan), fill `bg/card`.
- `content` at (72, 0), fills (290), V gap **8**:
  - `meta` **min height 56**, V gap 2, content **vertically centred** (a title-only meta sits centred on the 56 leading):
    - `title line` H gap 8, centred: `title` **Title/2** (Bold 24/30, −1.5 %) `text/primary`, **max width 260**, 1 line, truncates · `tag` = Badge / Pill **Muted** "Guest" (hidden; `Show tag`; also "Archived").
    - `subtitle` **Subheadline** `text/secondary`, 1 line, truncates; `Show subtitle`.
  - `members` = `Avatar / Stack` **Count=4** (104 × 32; Arjun, Priya, Rohan, Esha) at (72, 64); `Show members`. The screen sets Count to 2–4.
- Not interactive (the Push Header carries the actions). Tapping the members stack may open the member list (screen spec).

### 3.7 Header / Amount Hero (128:2004). SwiftUI: `PBAmountHero`. NEW
> “PBAmountHero — the left-aligned hero of an expense, payment or loan detail (06-10/12/14/15/16 · 08-06 · 09-03/05/06). A 56 leading, then Title (Title/2, wraps), Amount (Title/1), Meta (Footnote, secondary) and up to 3 chips.
> Leading=Icon: exposed Avatar / Circle 56 Type=Icon (the category icon: Food, Bed…). Leading=Avatar: exposed Avatar / Circle 56 Art (the other person on a loan). Leading=Pair: exposed Avatar / Pair 56 (payer → receiver).
> Show chips + exposed Badge / Pill “chip 1” (Muted, group), “chip 2” (Muted, category; Show chip 2) and “chip 3” (Inverse, status such as “Disputed” or “Paid back”; Show chip 3). Status chips are black or gray, never red.
> SwiftUI: PBAmountHero”

Properties: `Title` ("Seafood dinner at Britto’s", curly ’) · `Amount` ("₹6,500") · `Meta` ("Paid by you · 22 Sep") · `Show chips` (true) · `Show chip 2` (true) · `Show chip 3` (false) · `Leading` = Icon | Avatar | Pair.
Geometry: **362 × 194** (hugs), V auto-layout, gap **12**, left-aligned.
1. Leading at (0, 0): Icon (128:1858) = `Avatar / Circle` **56 Type=Icon**, Icon = **Food**, fill `bg/card`. Avatar (128:1891) = `Avatar / Circle` **56 Type=Art** (**Dev**, `avatar-5`). Pair (128:1945) = `Avatar / Pair` **Size=56** (148 × 56, Arjun → Kabir).
2. `text` at (0, 68), fills, V gap 2: `title` **Title/2** `text/primary`, **wraps** · `amount` **Title/1** (ExtraBold 32/38, −2 %) `text/primary` ("₹6,500", hugs). Note it's Title/1, not an Amount style · `meta` **Footnote** `text/secondary`.
3. `chips` at (0, 170), H gap 8, 24 tall (`Show chips`): `chip 1` Badge / Pill **Muted** "Goa Trip" (69 × 24) · `chip 2` Muted "Food" (49 × 24; `Show chip 2`) · `chip 3` **Inverse** "Disputed" (74 × 24; `Show chip 3`; also "Paid back").
- Height check: 56 + 12 + 90 + 12 + 24 = 194. Not interactive. Chips are labels, not buttons.

### 3.8 Card / Notice (129:1976). SwiftUI: `PBNoticeCard`. NEW
> “PBNoticeCard — an in-flow notice on #F5F5F5 r20 (06-12 · 07-11 · 08-03 · 08-06 · 09-06 · 10-05 · 10-06 · 11-03): pending confirmation, invite a guest, simplified debts, a dispute, a read-only project, a Pro lock.
> Layout=Leading: exposed icon circle Avatar / Circle 40 Type=Icon On Card (set its Icon: Activity, Shuffle, Mail, Flag, Lock, Check Circle), Title (Headline) with Show badge (exposed Badge / Pill, default Inverse “Pro”) on the title line, Body (Subheadline, secondary, wraps). Layout=Centered: 56 icon circle, badge, Title (Title/3), Body, all centred (11-03).
> Actions=None · One = exposed Button / Primary Large “primary”, full width · Two = exposed Button / Primary Small “primary” + Button / On Card Small “secondary”, both FILL, gap 8. Set the button labels on the nested buttons (Label#9:0 / Label#9:42).
> Show title off hides the whole title line (and the badge with it in Layout=Leading), so the body sits level with the icon.
> All gray and black: never red.
> SwiftUI: PBNoticeCard”

Properties: `Title` ("Pending confirmation") · `Show title` (true) · `Body` ("Waiting for Meera to confirm") · `Show badge` (false) · `Layout` = Leading | Centered · `Actions` = None | One | Two (6 variants).
Card (all): fill `bg/card` #F5F5F5, radius **20**, 362 wide, height hugs, V auto-layout. No stroke, no shadow.
**Layout=Leading** (padding **16**, gap **16**):
- `content` H gap 12, top-aligned: `icon` = `Avatar / Circle` **40 Type=Icon On Card** (white circle, default Icon / **Activity**, 20 pt icon) · `text` fills, V gap 2: `title line` (H gap 8, centred; hidden with `Show title`): `title` **Headline** `text/primary` (fills) + `badge` Badge / Pill **Inverse** "Pro" (40 × 24; `Show badge`) · `body` **Subheadline** `text/secondary`, wraps.
- `actions` row (H gap 8, at y 76): One = `Button / Primary` **Large** "Send invite", full width (330 × 52) → card **362 × 144**. Two = `Button / Primary` **Small** "Edit expense" + `Button / On Card` **Small** "Resolve", both fill (161 × 36 each) → card **362 × 128**. None → card **362 × 76**.
**Layout=Centered** (padding **24**, gap **12**, all centred):
- `icon` = `Avatar / Circle` **56 Type=Icon On Card** (default Icon / **Lock**) · `badge` Inverse "Pro" (hidden; `Show badge`) · `text` V gap 8, centred: `title` **Title/3** (Bold 20/26) `text/primary`, centre-aligned · `body` **Subheadline** `text/secondary`, centre-aligned.
- `actions` (padding-top 12, so 24 below the text): One = Primary **Large** "See Pro" (314 × 52) → **362 × 246**. Two = Primary Small "See Pro" + On Card Small "Not now" (153 × 36 each) → **362 × 230**. None → **362 × 170**.
- Icons used on screens (description): Activity (pending), Shuffle (simplified debts), Mail (invite a guest), Flag (dispute), Lock (read-only / Pro lock), Check Circle. Never red.

### 3.9 Card / Confirm Payment (129:2060). SwiftUI: `PBConfirmPaymentCard`. NEW
> “PBConfirmPaymentCard — The receiver’s confirm card at the top of Home and in Activity (04-06 · 08-10 under the scrim · 09-01 · 09-08), #F5F5F5 r20, padding 16, gap 12.
> State=Pending (362×120): exposed Avatar / Circle 40 (the payer, white circle), Title (Headline) “Esha says she paid you ₹700”, Detail (Footnote) “Dinner at Olive Garden · UPI · 9:12 pm”, then two exposed buttons, both FILL, gap 8: Button / Primary Small “Confirm” (layer “confirm”) and Button / On Card Small “Not received” (layer “not received”).
> Button labels: set Label#9:0 on “confirm” and Label#9:42 on “not received” (they show in this card’s property panel as nested instances). Figma can’t bind a card-level text property to text inside a nested button, so there is no separate Confirm label / Secondary label property.
> State=Confirmed (≈72): Confirmed title “Esha paid you ₹700”, Confirmed detail “… · Confirmed”, Icon / Check Circle, no buttons. The Confirmed texts are separate properties so they survive the variant change.
> Interactive: Confirm → Change to State=Confirmed (on click, smart animate, ease out, 250 ms). A frame can override the Confirm button with its own link (04-06 → 08-11); Not received opens the 08-10 sheet on the frame.
> SwiftUI: PBConfirmPaymentCard”

Properties: `Title` ("Esha says she paid you ₹700") · `Detail` ("Dinner at Olive Garden · UPI · 9:12 pm") · `Confirmed title` ("Esha paid you ₹700") · `Confirmed detail` ("Dinner at Olive Garden · UPI · Confirmed") · `State` = Pending | Confirmed.
Card: fill `bg/card` #F5F5F5, radius **20**, padding **16**, V gap **12**, 362 wide.
- **Pending (129:1977), 362 × 120:**
  - `top` row (H gap 12, centred): `avatar` = `Avatar / Circle` **40 Art** (**Esha**, `avatar-4`), fill **`bg/primary`** · `text` fills (278), V gap 0: `title` **Headline** `text/primary`, 1 line, truncates · `detail` **Footnote** `text/secondary`, 1 line, truncates.
  - `actions` (H gap 8): `confirm` = `Button / Primary` **Small** "Confirm" (fills, 161 × 36) · `not received` = `Button / On Card` **Small** "Not received" (fills, 161 × 36).
  - **Reaction on `confirm`:** ON_CLICK → CHANGE_TO State=Confirmed (129:2021), **SMART_ANIMATE, 250 ms, EASE_OUT**.
- **Confirmed (129:2021), 362 × 72:** the same top row with `title` = Confirmed title, `detail` = Confirmed detail (text column 242 wide) + `status` = `Icon / Check Circle` **24 × 24** at (322, 24), `icon/primary`. No actions.
- Motion: animate the height from 120 to 72 (the buttons fade and collapse), then the texts cross-fade and the check icon fades in, all in 250 ms ease-out. With Reduce Motion, swap instantly. Confirm marks the payment confirmed in the data store. Not received opens the "Not received" sheet (08-10). Both are specced by the Settle Up / Home screen specs.

### 3.10 Card / QR Code (130:1912). SwiftUI: `PBQRCodeCard`. NEW
> “PBQRCodeCard — “My QR code” (07-10): a real, scannable QR vector for https://paybak.app/i/arjun (version 4, 33×33 modules, error correction H, 6 pt modules = 198 pt) in color/text/primary on a white (color/bg/primary) r20 card, 240 square, quiet zone 21. Show mark: Brand / App Mark 40 on a 52 white rounded backing in the centre (≈7% of the modules; level H recovers 30%, so it still scans).
> The matrix was generated by api.qrserver.com (ecc=H) and verified to decode with CoreImage (CIDetector) before and after import. In the app it is rendered by CoreImage (CIQRCodeGenerator, correction level H) from the user’s invite link, never an image asset.
> SwiftUI: PBQRCodeCard”

Property: `Show mark` (true). Geometry: **240 × 240**, fill **`bg/primary` #FFFFFF**, radius **20**, content centred.
- `modules`: the QR matrix, **198 × 198** at (21, 21) (quiet zone 21), fill `text/primary` #0A0A0A, square modules of **6 pt** (33 × 33, version 4, ECC **H**).
- `mark` (`Show mark`): a **52 × 52** white (`bg/primary`) square, radius **14** (`radius/input`), centred at (94, 94), holding `Brand / App Mark` **Size=40** at (100, 100) (radius 8.95, smoothing 0.6).
- Rendering: iOS `CIFilter.qrCodeGenerator()` with `correctionLevel = "H"`, scaled with nearest-neighbour (`.interpolation(.none)`) to exactly 198 pt. Android: `com.google.zxing:core` `QRCodeWriter` with `EncodeHintType.ERROR_CORRECTION = H` and `MARGIN = 0`, drawn as 6-dp modules on a Canvas. Encode the user's invite link (`https://paybak.app/i/<handle>`; the demo user Arjun → `https://paybak.app/i/arjun`). Never ship the QR as an image asset.
- The white card is only visible where the screen puts it on a darker surface. The screen spec (07-10 My QR code) gives the placement.

### 3.11 Row / Group (139:2052). SwiftUI: `PBGroupRow`. NEW
> “PBGroupRow — a group or project in the Groups list (07-01) and in “Groups together” on a friend page (07-08). Row on white, no side padding, inset divider (Divider / Line Inset=Leading, 52).
> Exposed tile = Avatar / Circle 40 Type=Icon (set its Icon: Plane trip · Home · People friends · Tag other · Drone / Package projects). Name (Headline, truncates), Subtitle (Footnote, secondary), Show divider.
> Balance=Owe: “−” + Amount (Amount/Medium, text/secondary) over Amount label “You owe”. Balance=Owed: “+” + Amount (Amount/Medium, text/primary) over Owed label “You’re owed”. Balance=Settled: Status (Subheadline, secondary: “Settled”, or “You’re settled” for a project).
> Type=Group 72 · Type=Project 116: adds an exposed Control / Progress Bar Small (layer “bar”: set bar › track › fill.paddingRight = W×(1−p), default 87%) with Budget “₹52,000 of ₹60,000” and Left “₹8,000 left”, indented under the text · Type=Archived (Settled only) 72: tile icon, name and the fixed status “Read-only” in tertiary gray.
> Amount excludes the sign. Text props share one default across variants (Goa Trip): override Name/Subtitle per instance.
> SwiftUI: PBGroupRow”

Properties: `Name` ("Goa Trip") · `Subtitle` ("5 members · Due Fri 2 Oct") · `Amount` ("₹1,400") · `Amount label` ("You owe") · `Owed label` ("You’re owed", curly ’) · `Status` ("Settled") · `Budget` ("₹52,000 of ₹60,000") · `Left` ("₹8,000 left") · `Show divider` (true) · `Type` = Group | Project | Archived · `Balance` = Owe | Owed | Settled (7 variants; Archived exists only with Settled).
Geometry: **362 wide**, V auto-layout, padding [**16**, 0, **16**, 0] (no side padding: the row lives in the 20-pt screen margins), gap **12**, no fill (on white).
- `main` row at (0, 16), 362 × 40, H gap 12, centred:
  - `tile` = `Avatar / Circle` **40 Type=Icon**, fill `bg/card`; Icon: **Plane** (Group), **Drone** (Project), **Package** (Archived). Use Home / People / Tag per group type.
  - `text` fills, V gap 0: `name` **Headline** `text/primary` (Archived: **`text/tertiary`**), 1 line, truncates · `subtitle` **Footnote** `text/secondary`, 1 line, truncates.
  - `trailing` V gap 0, **right-aligned**:
    - Owe: "−" + `Amount` in **Amount/Medium `text/secondary`** over `Amount label` **Footnote `text/secondary`** ("You owe").
    - Owed: "+" + `Amount` in **Amount/Medium `text/primary`** over `Owed label` **Footnote `text/secondary`** ("You’re owed").
    - Settled: `status` **Subheadline** `text/secondary` ("Settled"; "You’re settled" for a project).
    - Archived: the fixed text "Read-only", **Subheadline `text/tertiary`**.
- **Project only**, `budget row` at (0, 68), 362 × 32, H gap 12: an `inset` spacer **40** wide (`size/avatar-md`) + `budget` column (310 wide, V gap 8): `bar` = `Control / Progress Bar` **Size=Small, State=Default** (310 × 6; fill = spent ÷ budget, default 87 %) · `caption` row (`SPACE_BETWEEN`): `budget` **Footnote** `text/secondary` ("₹52,000 of ₹60,000") and `left` **Footnote** `text/secondary` ("₹8,000 left").
- `divider` = `Divider / Line` **Inset=Leading** (the line starts at x 52), absolute at the bottom (y 71 for 72-tall rows, 115 for Project). `Show divider`: off on the last row.
| Variant | Height | Text column width |
|---|---|---|
| Group, Owe (139:1913) | 72 | 233 |
| Group, Owed (139:1929) | 72 | 224 |
| Group, Settled (139:1945) | 72 | 250 |
| Project, Owe / Owed / Settled (139:1958 / 139:1986 / 139:2014) | **116** | 233 / 224 / 250 |
| Archived, Settled (139:2039) | 72 | 230 |
- Tap: the whole row pushes to the group/project.

---

## 4. Progress & Charts (116:1060)
Section note (verbatim): “Progress bars in two heights (Small 6, Large 12), share-bar rows, budget and loan cards and the monthly chart, in chart grays. Red marks only over budget. Bar lengths and chart heights are padding overrides.”
Chart colours (tokens.md, Foundations rule): track `chart/track` #EBEBEB · bars `chart/bar` #D1D1D1 · current value `chart/fill` #0A0A0A · over budget `chart/over` #C93636 (the only red).

### 4.1 Control / Progress Bar (116:1099). SwiftUI: `PBProgressBar`. NEW
> “PBProgressBar — Budget, loan and share bars. Size=Small (6 tall, inside Row / Group and Row / Bar) · Large (12 tall, Card / Budget and Card / Loan Progress). Full-radius color/chart/track track; use the instance at FILL width.
> State=Default (fill only) · Projected (fill + the projected segment in color/chart/bar) · Over (fill up to the budget, then the red color/chart/over segment to the end).
> Percentages are PADDING overrides (Figma ignores size and position overrides on instance sublayers). Inside “track”, the layers “fill”, “projected” and “over” are full-width auto-layout frames holding a “segment”: fill.paddingRight = W×(1−p) · projected.paddingRight = W×(1−p) · over.paddingLeft = W×p (the budget point). For 0% hide “fill”. Show mark shows “mark” (a full-width frame holding the 2pt “tick”, color/chart/fill with a 1pt white outline, overhanging by 3): mark.paddingLeft = W×p − 1. W = the instance width: set the paddings AFTER the bar has its final width (they are pixels, not percentages).
> “over” and “mark” are left-anchored (constraint Left, 362 wide; the track clips “over”), so their paddings always count from the bar’s left edge.
> Drawn defaults: Default fill 60% (mark 51%) · Projected fill 87%, projected 97% (mark 90%) · Over fill 97.6%, over from 97.6% (mark 97.6%).
> SwiftUI: PBProgressBar”

Properties: `Show mark` (false) · `Size` = Small | Large · `State` = Default | Projected | Over (6 variants). A **fill-width** bar: **H = 6** (Small) or **12** (Large); 362 is only the component's default width.

Implement it as one view with inputs `fill p` (0…1), `projected q` (optional, q ≥ p), `budget point b` (Over only), `mark m` (optional) and `size`. Layers, bottom to top:
1. `track`: full width × H, capsule (`radius/full`), fill `chart/track` #EBEBEB, **clips** its children.
2. `projected` (Projected only): segment from x 0 to W × q, capsule, fill `chart/bar` #D1D1D1.
3. `over` (Over only): segment from x W × b to the right end, fill `chart/over` #C93636. It has no radius of its own; the track's round right end clips it.
4. `fill`: segment from x 0 to W × p, fill `chart/fill` #0A0A0A. Capsule in Default/Projected; in **Over** only the **left** corners are round, so the black meets the red with a straight edge at the budget point.
5. `mark` (`Show mark`): a **2-pt wide tick** at x = W × m − 1, **3 pt taller than the track at the top and at the bottom** (12 tall on Small at y −3, 18 on Large), fill `chart/fill` #0A0A0A with a **1-pt OUTSIDE stroke `bg/primary`** (white outline). It isn't clipped by the track.
| Variant | Size | Figma sample (W = 362) |
|---|---|---|
| Small / Default (116:1063) | 362 × 6 | fill 60 % (217.2), mark (hidden) at 51 % |
| Small / Projected (116:1069) | 362 × 6 | fill 87 % (314.94), projected 97 % (351.14), mark 90 % |
| Small / Over (116:1075) | 362 × 6 | fill 97.6 % (353.31), over 97.6 → 100 %, mark 97.6 % |
| Large / Default (116:1081) | 362 × 12 | as Small |
| Large / Projected (116:1087) | 362 × 12 | as Small |
| Large / Over (116:1093) | 362 × 12 | as Small |
- 0 %: draw no fill segment (Figma hides "fill"). 100 %: the fill is the full capsule.
- Example frame `_Example · Progress Bar marks` (116:1101), caption “Show mark=true (examples)”: a Small Default bar with its mark at 51 %, and a Large Over bar with its mark at the budget point (render `.figma-cache/components/116-1101.png`).
- Animate width changes with ~300 ms ease-out when values change (suggestion; Figma has no motion). Accessibility: expose it as a progress value ("87 percent used").

### 4.2 Row / Bar (143:2156). SwiftUI: `PBBarRow`. NEW
> “PBBarRow — a share bar row: Insights by category and by group (11-01 · 11-02 · 11-03) and “Paid vs fair share” on the project dashboard (10-01). 362×56, no side padding (use FILL inside a card).
> Line 1: exposed leading Avatar / Circle 40, Title (Headline), Caption (Footnote, secondary: “51%” / “Paid ₹25,500”; Show caption), Amount on the right. Line 2: exposed Control / Progress Bar Small (layer “bar”).
> Leading=Icon: Type=Icon on color/bg/card for white surfaces (set its Icon; category map Food · Car · Bed · Ticket · Home · Bolt · Shopping Bag · Tag; groups Home · Plane · People). Leading=Avatar: Type=Art in a white circle for #F5F5F5 cards (set Art).
> Value=Neutral: Amount (Headline, text/primary: “₹12,000”, “Settled”) · Owed: “+” + Amount (Amount/Medium, text/primary) · Owe: “−” + Amount (Amount/Medium, text/secondary). Amount excludes the sign.
> Bar: set the nested bar’s paddings after sizing (see Control / Progress Bar): bar › track › fill.paddingRight = W×(1−p); Avatar variants show the fair-share mark (mark.paddingLeft = W×0.51 − 1). Defaults: Icon 51% (Rent); Avatar Neutral 51% · Owed 100% · Owe 35%.
> SwiftUI: PBBarRow”

Properties: `Title` ("Rent") · `Caption` ("51%") · `Show caption` (true) · `Amount` ("₹12,000") · `Leading` = Icon | Avatar · `Value` = Neutral | Owed | Owe (6 variants).
Geometry: **362 × 56**, H auto-layout, padding [8, 0, 8, 0], gap **12**, items centred vertically, no fill.
- `leading` at (0, 8): Icon = `Avatar / Circle` **40 Type=Icon**, Icon = **Home**, fill `bg/card` (for white surfaces). Avatar = `Avatar / Circle` **40 Type=Art** (**Dev**), fill **`bg/primary`** (for #F5F5F5 cards).
- `content` at (52, 10), fills (310), V gap **8**:
  - `line` H gap 8, centred: `label` (fills, H gap 8, centred) = `title` **Headline** `text/primary` ("Rent", hugs) + `caption` **Footnote** `text/secondary` ("51%"; `Show caption`) · `value`, right-aligned:
    - Neutral: `amount` **Headline** `text/primary` ("₹12,000" or "Settled").
    - Owed: "+" + amount in **Amount/Medium `text/primary`**.
    - Owe: "−" + amount in **Amount/Medium `text/secondary`**.
  - `bar` = `Control / Progress Bar` **Small, State=Default**, 310 × 6 at (52, 40). **Avatar variants show the mark** (`Show mark` = true) at the fair share (51 % in the sample).
- Sample bar fills: Icon 51 % (Rent). Avatar: Neutral 51 %, Owed 100 %, Owe 35 %.
- Tap: the screen decides (e.g. open the category). No pressed variant.

### 4.3 Chart / Monthly Bars (143:2157). SwiftUI: `PBMonthlyBarChart`. NEW
> “PBMonthlyBarChart — six months of your share (11-01 hero card; 11-03 with a layer blur on the instance). 322×140 for the 362 card with 20 padding; columns spread with space-between, so FILL works.
> Each column: “bar-1…6” (32×120 plot, bars grow up from a zero baseline) + “label-1…6” (Footnote, 20 tall; Month 1–6 props, Apr–Sep). The current month (bar-6) uses color/chart/fill and a text/primary label; the others color/chart/bar and text/secondary. Pill tops (radius/full on the top corners).
> Height h (0–120) is a PADDING override: bar-N.paddingTop = 120 − h (size overrides don’t apply to instance sublayers). Defaults = Apr–Sep 2026: 95 · 113 · 101 · 106 · 114 · 120 (₹18,400 … ₹23,300 on a zero baseline).
> Built with Swift Charts.
> SwiftUI: PBMonthlyBarChart”

Properties: `Month 1` … `Month 6` ("Apr", "May", "Jun", "Jul", "Aug", "Sep").
Geometry: **322 × 140** (fills the 362 card minus 2 × 20 padding), H auto-layout, `SPACE_BETWEEN`, bottom-aligned. Six columns, each **32 × 140**, at x 0, 58, 116, 174, 232, 290 (26 apart at 322 wide):
- `bar-N`: a **32 × 120** plot area (clips) with a bar that grows up from the zero baseline at the bottom: height **h = 120 × value ÷ max(values)**, width 32, **top corners fully round** (radius 16 on a 32-wide bar; bottom corners square). Fill `chart/bar` #D1D1D1 for months 1–5 and **`chart/fill` #0A0A0A for month 6** (the current month).
- `label-N` 32 × 20 under the plot: **Footnote**, centred, `text/secondary` (months 1–5) or **`text/primary`** (month 6).
- Sample heights (Apr → Sep 2026): **95, 113, 101, 106, 114, 120**, i.e. ₹18,400 … ₹23,300 on a zero baseline (23,300 → 120). The live values come from the insights aggregation (screen spec).
- iOS: Swift Charts `BarMark` with `.cornerRadius` on the top only (or a custom `UnevenRoundedRectangle`), no axes and no gridlines. Android: a Canvas drawing the six bars. On 11-03 (locked Insights) the whole chart has a **layer blur** (screen spec).

### 4.4 Card / Budget (145:2106). SwiftUI: `PBBudgetCard`. NEW
> “PBBudgetCard — budget vs spent on a project (10-01 · 10-03 · 10-05 · 10-06). #F5F5F5 r20, padding 16, gap 12; 362 wide, hugs (189 with the planned line, 146 without).
> “Spent” label, Spent (Title/1) + Budget (Headline, secondary, “of ₹60,000”) on one baseline, an exposed Control / Progress Bar Large (layer “bar”), then Percent label (“87% used”) and Left label (“₹8,000 left” / “₹8,000 under budget”), then Show planned line: a divider + Planned text (Footnote).
> State=On track: bar State=Projected (fill 87%, projected 97% = ₹58,000). State=Over budget: bar State=Over + mark at the budget (fill 97.6% = ₹60,000 of ₹61,500), and the stats row becomes Icon / Alert 16 + Warning (Subheadline, “₹1,500 over budget”) in text/destructive: the only red, with the over segment. State=Closed: bar State=Default, no projection, no planned line (planned components count toward nothing).
> Set percentages with the nested bar’s paddings (see Control / Progress Bar). Text props share one default (the 10-01 numbers): 10-03 sets Spent “₹61,500” and Planned text “All planned items are bought.” (see _Example · Over budget beside the set); 10-05/10-06 set Left label “… under budget”.
> SwiftUI: PBBudgetCard”

Properties: `Spent` ("₹52,000") · `Budget` ("of ₹60,000") · `Percent label` ("87% used") · `Left label` ("₹8,000 left") · `Warning` ("₹1,500 over budget") · `Show planned line` (true) · `Planned text` ("Planned items bring it to ₹58,000") · `State` = On track | Over budget | Closed.
Card: fill `bg/card` #F5F5F5, radius **20**, padding **16**, V gap **12**, 362 wide, hugs.
1. `head` (V gap 2): `label` "Spent" **Footnote** `text/secondary` · `amount row` (H gap 8, **baseline-aligned**): `spent` **Title/1** `text/primary` ("₹52,000") + `budget` **Headline** `text/secondary` ("of ₹60,000").
2. `bar` = `Control / Progress Bar` **Large**, 330 × 12:
   - On track: State=**Projected**, fill = spent ÷ budget (87 %), projected = (spent + planned) ÷ budget (97 % = ₹58,000).
   - Over budget: State=**Over**, **Show mark** at the budget point. Fill = budget ÷ spent (97.6 % = ₹60,000 of ₹61,500), and the red runs from there to the end.
   - Closed: State=**Default**, no projection, no mark.
3. `stats` row (`SPACE_BETWEEN`): `percent` **Subheadline** `text/secondary` ("87% used") · `left` **Subheadline `text/primary`** ("₹8,000 left", or "₹8,000 under budget" once the project is closed/finished).
   Over budget replaces it with `warning` (H gap 6, centred): `Icon / Alert` **16 × 16** `icon/destructive` + `warning text` **Subheadline `text/destructive`** ("₹1,500 over budget").
4. `planned` (V gap 12; `Show planned line`; not in Closed): `Divider / Line` Inset=None (330) + `planned text` **Footnote** `text/secondary`.
| State | Size |
|---|---|
| On track (145:2044) | **362 × 189** (16 + 58 + 12 + 12 + 12 + 20 + 12 + 31 + 16) |
| Over budget (145:2065) | 362 × 189 |
| Closed (145:2090) | **362 × 146** |
Example `_Example · Over budget (10-03)` (159:11440): State=Over budget, Spent "₹61,500", Budget "of ₹60,000", Warning "₹1,500 over budget", Planned text "All planned items are bought." (render `.figma-cache/components/159-11440.png`).

### 4.5 Card / Loan Progress (145:2155). SwiftUI: `PBLoanProgressCard`. NEW
> “PBLoanProgressCard — an IOU’s progress on the loan detail (06-14 · 06-15 · 06-16). #F5F5F5 r20, padding 16, gap 12; 362 wide (≈141 tall).
> Three stat columns (Footnote label over Amount/Medium value): Original → Paid → Remaining, a divider, an exposed Control / Progress Bar Large (layer “bar”, fill = share paid back), then Caption (Footnote, secondary).
> State=Active: bar at 0% (“fill” hidden; set bar › track › fill.paddingRight = W×(1−p) and show it for partial repayments), Caption “0% paid back”. State=Paid back: bar at 100% and Icon / Check Circle 16 before the Caption (“Paid back on 14 Sep”). No red here: an overdue installment shows its red badge on its Row / Activity.
> Text props share one default (the Dev loan, ₹6,000 · ₹0 · ₹6,000): a Paid back instance sets them, e.g. Kabir ₹4,500 · ₹4,500 · ₹0 · “Paid back on 14 Sep” (see the example beside the set).
> SwiftUI: PBLoanProgressCard”

Properties: `Original` ("₹6,000") · `Paid` ("₹0") · `Remaining` ("₹6,000") · `Caption` ("0% paid back") · `State` = Active | Paid back.
Card: fill `bg/card`, radius **20**, padding **16**, V gap **12**, **362 × 141**.
1. `stats` row, H gap 12: three columns, each fills (102 wide), V gap 2: `label` **Footnote** `text/secondary` ("Original" / "Paid" / "Remaining") over `value` **Amount/Medium** `text/primary`.
2. `divider` = `Divider / Line` Inset=None, 330 wide.
3. `bar` = `Control / Progress Bar` **Large, Default**, 330 × 12, fill = paid ÷ original (Active sample 0 %: no fill; Paid back 100 %).
4. `caption` row, H gap 6, centred: Paid back only: `Icon / Check Circle` **16 × 16** `icon/primary` · `caption text` **Footnote** `text/secondary` ("0% paid back" / "Paid back on 14 Sep").
- Example `_Example · Loan paid back` (145:2156), caption “Example: paid back (Kabir, 06-15)”: Original "₹4,500", Paid "₹4,500", Remaining "₹0", Caption "Paid back on 14 Sep" (render `.figma-cache/components/145-2156.png`).
- Caption copy for partial repayments isn't designed. Proposal: "{n}% paid back", n = round(paid ÷ original × 100).

---

## 5. Assistant & Scan (117:958)
Section note (verbatim): “Ask Paybak chat bubbles and the draft expense card, receipt review and item assignment rows, and the camera shutter (fallback: the iOS 27 kit has no camera chrome).”

### 5.1 Chat / Bubble (117:971). SwiftUI: `PBChatBubble`. NEW
> “PBChatBubble — One message in Ask Paybak (11-05, 11-06). The row is 362 wide; use it at FILL.
> Role=User: right-aligned black bubble (bg/inverse, radius 20, padding 12/16) with text/inverse Body, max 280 wide, hugging short text. Role=Assistant: left-aligned plain Body with no bubble, max 320 wide including the 24 Sparkles avatar (Avatar / Circle Type=Icon, exposed; Show avatar, Assistant only).
> Text = the message.
> SwiftUI: PBChatBubble”

Properties: `Text` ("Who owes me money?") · `Show avatar` (true) · `Role` = User | Assistant.
- **User** (117:961), row 362 wide, content **right-aligned**: `bubble` hugs, **max width 280**, padding [**12**, **16**, 12, 16], radius **20** (`radius/card`), fill `bg/inverse` #0A0A0A, clips; `text` **Body** (Regular 16/24) `text/inverse`, wraps. The sample is 196 × 48.
- **Assistant** (117:964), row 362 wide, **left-aligned**, no bubble: `message` (max 320 wide), H gap **8**, top-aligned: `avatar` = `Avatar / Circle` **24 Type=Icon**, Icon = **Sparkles** (14 pt), fill `bg/card` #F5F5F5 (`Show avatar`) + `text` **Body** `text/primary`, wraps (288 wide). The sample is 362 × 24.
- Vertical spacing between messages is set by the screen spec (Ask Paybak). Text is selectable (long-press copy). Assistant answers can embed other components under the text (e.g. Row / Person rows, Chat / Draft Expense); see the Ask Paybak screen spec.

### 5.2 Chat / Draft Expense (147:2315). SwiftUI: `PBDraftExpenseCard`. NEW
> “PBDraftExpenseCard — the assistant’s draft expense in Ask Paybak (11-06). #F5F5F5 r20, padding 16, gap 12, 362 wide.
> Top row: exposed icon circle Avatar / Circle 40 Type=Icon On Card (category icon, default Car), Title (Headline), Amount (Amount/Medium). A divider, then Paid line, Split line with an exposed Avatar / Stack (3: You, Esha, Dev) and Each line (Subheadline).
> State=Pending: exposed Button / Primary Small “save” (FILL) + Button / On Card Small “edit”. State=Saved: Icon / Check Circle 20 + “Expense added” + exposed Button / Text “view” (opens the expense detail, the 09-03 template).
> Interactive: Save → CHANGE_TO State=Saved (On click, Smart animate, ease out, 250 ms). You stay in the chat: no toast and no navigation (the intended exception to “detail + toast”). Text props are shared by both states, so the data survives the change. Nothing is saved without a tap.
> SwiftUI: PBDraftExpenseCard”

Properties: `Title` ("Cab") · `Amount` ("₹600") · `Paid line` ("Paid by you · Today") · `Split line` ("Split equally with Esha and Dev") · `Each line` ("₹200 each") · `State` = Pending | Saved.
Card: fill `bg/card` #F5F5F5, radius **20**, padding **16**, V gap **12**, 362 wide.
1. `top` row (H gap 12, centred, 40 tall): `icon` = `Avatar / Circle` **40 Type=Icon On Card** (white), Icon = **Car** · `title` **Headline** `text/primary`, fills, 1 line, truncates · `amount` **Amount/Medium** `text/primary` ("₹600").
2. `divider` = `Divider / Line` Inset=None (330).
3. `details` (V gap 8): `paid` **Subheadline** `text/secondary` · `split row` (H gap 8, centred): `split` **Subheadline** `text/secondary` (fills) + `members` = `Avatar / Stack` **Count=3** (80 × 32; Arjun, Esha, Dev; circles `bg/card` with the stack's 2-pt white ring) · `each` **Subheadline `text/primary`** ("₹200 each").
4. Pending (147:2111, **362 × 233**): `actions` (H gap 8): `save` = `Button / Primary` **Small** "Save" (**fills**, 261 × 36) + `edit` = `Button / On Card` **Small** "Edit" (hugs, 61 × 36).
   Saved (147:2213, **362 × 241**): `status` row (H gap 8, centred, 44 tall): `Icon / Check Circle` **20 × 20** `icon/primary` + `saved label` "Expense added" **Headline** `text/primary` (fills, 1 line) + `view` = `Button / Text` Style=Primary "View" (34 × 44) → opens the saved expense's detail (the 09-03 template).
- **Reaction on `save`:** ON_CLICK → CHANGE_TO State=Saved (147:2213), **SMART_ANIMATE 250 ms EASE_OUT**. Saving creates the expense in the data store. No toast, no navigation. Edit opens the Add expense modal prefilled with the draft.

### 5.3 Row / Receipt Line (117:993). SwiftUI: `PBReceiptLineRow`. NEW
> “PBReceiptLineRow — One line of a scanned receipt (11-08), 362×44 inside a #F5F5F5 card, 16 side padding: Label on the left, Amount on the right.
> Style=Default (Body) · Total (Headline, with a Divider / Line above it). State=Editing turns the amount into an inline white field (bg/primary, radius 14, 36 tall, min 96, focus ring and caret) so a misread value can be fixed in place.
> SwiftUI: PBReceiptLineRow”

Properties: `Label` ("Chicken biryani") · `Amount` ("₹430") · `Style` = Default | Total · `State` = Default | Editing.
Geometry: **362 × 44** (`size/tap`), H auto-layout, padding [0, 16, 0, 16], gap **12**, centred vertically, no fill (inside a `bg/card` card).
| Variant | `label` | `amount` | Extra |
|---|---|---|---|
| Default, Default (117:973) | **Body** `text/primary`, fills, 1 line, truncates | **Body** `text/primary`, hugs | – |
| Default, Editing (117:976) | Body, fills (222 wide) | inside `field` | `field` **96 × 36** (min width 96, grows) at (250, 4): H gap 2, padding [0, 12, 0, 12], content **right-aligned**, radius **14**, fill `bg/primary`, **1.5-pt inside** stroke `border/strong`; amount Body `text/primary` + `caret` 2 × 20 `bg/inverse` |
| Total, Default (117:981) | **Headline** `text/primary` | **Headline** `text/primary` | `divider` = Divider / Line Inset=None, absolute at (16, 0), 330 × 1 (on top) |
| Total, Editing (117:986) | Headline | Headline inside the field | the field + the divider |
- Tap the amount to edit (number pad). Done / tapping outside commits. Sample lines come from Art / Receipt (§7.1).

### 5.4 Row / Assign Item (147:2532). SwiftUI: `PBAssignItemRow`. NEW
> “PBAssignItemRow — one receipt item on Assign items (11-09): tap who had it. Row on white, no side padding, full-width divider (Show divider).
> Line 1: Item (Headline, truncates) and Price (Amount/Medium). Line 2: three exposed Control / Category Chip Leading=Avatar, 36 tall, named you · esha · dev (Label You/Esha/Dev, Avatar / Circle 24 Art). A chip is Selected=True (black) when that person had the item; switch the chip’s Selected per row.
> Shared=False (90): default Dev selected (Chicken biryani). Shared=True (108): adds Shared caption (Footnote, secondary: “Shared by 3 · ₹80 each”) under the item, all three chips selected.
> Text props share one default (Chicken biryani ₹430): override Item/Price per row.
> SwiftUI: PBAssignItemRow”

Properties: `Item` ("Chicken biryani") · `Price` ("₹430") · `Shared caption` ("Shared by 3 · ₹80 each") · `Show divider` (true) · `Shared` = False | True.
Geometry: **362 wide**, V auto-layout, padding [**12**, 0, **12**, 0], gap **8**, no fill (on white).
1. `line` (H gap 12, top-aligned): `text` (fills, 308) V gap 0: `item` **Headline** `text/primary`, 1 line, truncates · (Shared=True) `shared caption` **Footnote** `text/secondary`, 1 line · `price` **Amount/Medium** `text/primary`.
2. `chips` (H gap 8): three `Control / Category Chip` **Leading=Avatar** chips, 36 tall: "You" (Arjun, 80 × 36), "Esha" (Esha, 89 × 36), "Dev" (Dev, 82 × 36). Selected=True (black, avatar circle `bg/card`) when that person had the item; Selected=False (grey, avatar circle white).
3. `divider` = `Divider / Line` **Inset=None**, absolute at the bottom, full 362 width (`Show divider`).
| Shared | Size | Sample |
|---|---|---|
| False (147:2317) | **362 × 90** | only "Dev" selected |
| True (147:2425) | **362 × 108** | caption "Shared by 3 · ₹80 each", all three selected |
- Behaviour: tapping a chip toggles that person for the item. With several people selected, the item is split equally and the caption shows "Shared by N · ₹X each" (screen spec). Card / Person Totals updates live.

### 5.5 Card / Person Totals (147:2533). SwiftUI: `PBPersonTotalsCard`. NEW
> “PBPersonTotalsCard — the live per-person totals pinned above Continue on Assign items (11-09). #F5F5F5 r20, padding 16, gap 6, 362×96.
> Status row: Icon / Check Circle 16 + Status line (“All items assigned”) and Note (“Includes GST and tip”, right-aligned, secondary). Then three columns: exposed Avatar / Circle 32 Art (white circle), Name (Footnote, secondary) over Amount (Amount/Medium): You ₹989 · Esha ₹621 · Dev ₹690 (tax and tip split in proportion; they sum to ₹2,300).
> SwiftUI: PBPersonTotalsCard”

Properties: `Status line` ("All items assigned") · `Note` ("Includes GST and tip") · `Name 1` ("You") · `Amount 1` ("₹989") · `Name 2` ("Esha") · `Amount 2` ("₹621") · `Name 3` ("Dev") · `Amount 3` ("₹690").
Card: **362 × 96**, fill `bg/card`, radius **20**, padding **16**, V gap **6**.
1. `status` row (H gap 6, centred, 18 tall): `Icon / Check Circle` **16 × 16** `icon/primary` · `status line` **Footnote `text/primary`** (hugs) · `note` **Footnote** `text/secondary`, **right-aligned**, fills, 1 line, truncates.
2. `people` row (H gap 12): three columns, each fills (102), H gap 8, centred: `avatar` = `Avatar / Circle` **32 Art** (Arjun / Esha / Dev), fill **`bg/primary`** · `text` (V gap 0): `name` **Footnote** `text/secondary` over `amount` **Amount/Medium** `text/primary`.
- Logic (description): each person's share of the items, plus tax and tip split **in proportion** to their item subtotal. The three amounts sum to the receipt total (₹989 + ₹621 + ₹690 = ₹2,300). Until every item is assigned, the status line should say so (copy not designed; proposal: "3 items left to assign", with no check icon).

### 5.6 Control / Shutter (117:1001). SwiftUI: `PBShutterButton`. NEW
> “PBShutterButton — Camera shutter for Scan receipt (11-07). Fallback: the iOS 27 kit has no camera chrome. A 76 white ring (4pt, bg/primary) around a 62 white disc; Pressed shrinks the disc to 56 in bg/card-pressed. Always sits on the dark color/bg/camera backdrop (the set background shows it).
> SwiftUI: PBShutterButton”

Property: `State` = Default | Pressed. **76 × 76**.
- `ring`: a 76 × 76 circle, **4-pt INSIDE stroke** `bg/primary` #FFFFFF, no fill.
- `disc`: Default = **62 × 62** at (7, 7), fill `bg/primary` #FFFFFF. Pressed = **56 × 56** at (10, 10), fill `bg/card-pressed` #EBEBEB.
- It always sits on `bg/camera` #2B2B2B (the camera backdrop). Tap = capture (light haptic). Animate the disc shrink over ~100 ms (suggestion).

---

## 6. Feedback & Overlays (118:962): new pieces
Section note (verbatim): “The toast (black capsule, no shadow) that confirms a save, and the sheet container: kit grabber, title, glass xmark on the right, optional search and a native Content slot.”

### 6.1 Overlay / Toast (118:965). SwiftUI: `PBToast`. NEW
> “PBToast — Short confirmation after a save (“Expense added”, “Payment recorded”, “Loan added”, “Group created”, “Payment confirmed”, “UPI ID copied”). A bg/inverse capsule, 44 tall, hugging its Label (Button/Small, text/inverse) with an optional 20 icon (Show icon; Icon swap, default Check Circle, icon/inverse). No shadow.
> Place it absolute and centred: 50 above the bottom edge, or 16 above the tab bar on tab screens. It fades after 2 s and is never a link. Ask Paybak does not use it (the draft card shows the save).
> SwiftUI: PBToast”

Properties: `Label` ("Expense added") · `Show icon` (true) · `Icon` instance swap (default Icon / Check Circle).
Geometry: "Expense added" → **173 × 44**. H auto-layout, **height 44**, width hugs, padding **[0, 20, 0, 16]** (16 left, 20 right), gap **8**, items centred vertically, capsule, fill **`bg/inverse` #0A0A0A**, **no shadow**, no stroke.
- `icon` at (16, 12), **20 × 20**, `icon/inverse` #FFFFFF, default `check-circle.svg`. Hidden when `Show icon` = false; the left padding stays 16.
- `label` at (44, 12), **Button/Small** (SemiBold 15/20, −0.25 %), `text/inverse`, 1 line.
- Placement: horizontally centred. The **bottom edge sits 50 pt above the screen's bottom edge** (y 780 on the 874 frame) on screens without a tab bar, or **16 above the tab bar's top** on tab screens (tab bar top y 791 → toast bottom y 775). Where a pinned bottom CTA would be covered, the README §5.1 #9 proposal applies: 16 above the CTA.
- Motion: fade in (~200 ms), stay **2 s**, fade out (~200 ms). Not tappable, not a link. One toast at a time; a new one replaces the current one. VoiceOver/TalkBack announce the label.
- Copy used across the app: “Expense added”, “Payment recorded”, “Loan added”, “Group created”, “Payment confirmed”, “UPI ID copied”.

### 6.2 Sheet / Container (118:1017): populated example (and changes)
The container itself is specced in components-home.md §15, and **it changed**: see §8.3 (new `Show header`, and the title now starts at x 16). The section adds an example, caption “Example: populated Content slot (Show search=true)” (`_Example backdrop` 118:10155, render `.figma-cache/components/118-10155.png`):
- A Detent=Medium sheet, **386 × 334**, `Title` "Category", `Show search` = true with the nested search field's Value "Search categories" (Input Field, leading `Icon / Search`, no label, no helper), and the Content slot (354 × 168) holding one **`bg/card` #F5F5F5 radius-20 card** with three **Row / Setting** rows (354 wide, 56 tall): "Food" (Icon / Food, **Trailing=Check**) · "Travel" (Icon / Car, Trailing=Unchecked) · "Stays" (Icon / Bed, Trailing=Unchecked, Show divider = false). Show value is off on all three.
- Height check: 8 + 4 + 8 + 50 + 8 + 52 + 8 + 168 + 28 = 334.
- This is the pattern for every picker sheet (category, paid by, currency, date…): a search field + a card of `Row / Setting` rows with Check/Unchecked.

---

## 7. Illustrations & Art (7:2): additions and Rive mapping
Section note (verbatim): “Open Peeps + Open Doodles by Pablo Stanley (CC0). Recoloured to the illustration tokens: line #0A0A0A, fill white, tint #EBEBEB. Compose scenes from these components — never draw new people.”

### 7.1 Art / Receipt (86:730). SwiftUI: `PBReceiptThumbnail`. NEW (art)
> “Leopold Cafe receipt art (₹2,300 lunch: 6 items, subtotal ₹2,000, GST 5% ₹100, tip 10% ₹200). Size=Full ≈300×458 white paper with torn edge and tint border, Manrope text; used rotated in the 11-07 camera scene. Size=Thumb 56×72 r10, text drawn as line bars so it also stands for other receipts (Villa); used on 09-03, 11-08, 11-10. The app shows the real photo. SwiftUI: PBReceiptThumbnail”

Property: `Size` = Full | Thumb.
- **Full (86:667), 300 × 458.** `paper` vector (the torn bottom edge is a zig-zag path), fill `illustration/fill` #FFFFFF, **2-pt inside** stroke `illustration/tint` #EBEBEB. `content` (V gap 12, padding [24, 20, 20, 20], clips):
  - `header` (centred, V gap 2): "Leopold Cafe" **Title/3** `text/primary` · "Colaba Causeway, Mumbai" **Footnote** `text/secondary` · "Wed 30 Sep 2026 · 1:15 pm" **Footnote** `text/secondary`.
  - a dashed line (260 wide, 1-pt stroke `illustration/line` #0A0A0A, dash 2, gap 4).
  - `items` (V gap 6; each row `SPACE_BETWEEN`, **Subheadline** `text/primary`): "Chicken biryani" ₹430 · "Paneer tikka" ₹370 · "Fish and chips" ₹450 · "Chocolate brownie" ₹240 · "Masala fries" ₹240 · "Fresh lime soda ×3" ₹270 (× is U+00D7).
  - a dashed line; `subtotal` (V gap 4, **Footnote** `text/secondary`): "Subtotal" ₹2,000 · "GST 5%" ₹100 · "Tip 10%" ₹200.
  - a dashed line; `total`: "Total" **Headline** `text/primary` · "₹2,300" **Amount/Medium** `text/primary`.
  - `footer` (padding-top 4, centred): "Thank you. Visit again." **Footnote** `text/tertiary`.
  - Check: 430 + 370 + 450 + 240 + 240 + 270 = 2,000; + 100 + 200 = 2,300.
- **Thumb (86:712), 56 × 72**, radius **10** (`radius/sm`), fill `illustration/tint` #EBEBEB, clips; a white `paper` rect 40 × 72 at (8, 8), radius 2 (it runs off the bottom); "text" drawn as bars in `illustration/line` #0A0A0A: title 20 × 3 at (18, 14) r1.5 · meta 14 × 1.5 at (21, 20) · six item bars 1.5 tall at x 12 (widths 16, 13, 15, 18, 12, 17) with 6 × 1.5 amount bars at x 39, rows at y 26, 31, 36, 41, 46, 51 · total label 12 × 2.5 at (12, 59) and total 9 × 2.5 at (36, 59), r1.25.
- In the app: the thumbnail slot shows the **real receipt photo** (aspect-fill, clipped to the 56 × 72 r10 shape). Use the Thumb art as the placeholder when there's no photo, or for the demo data (the Goa "Villa" and Leopold Cafe expenses). The Full art is only for the demo camera scene (Scan receipt, 11-07, where it's shown rotated) and as the demo receipt the simulated scanner "reads".
- Files (new; §10): `assets/images/art-receipt-full.svg` (300 × 458, **live Manrope text**, 8.5 kB; needs Manrope installed to render, so use the PNG where text fidelity matters), `assets/images/art-receipt-full@3x.png` (900 × 1374, from Figma at scale 3), `assets/images/art-receipt-thumb.svg` (56 × 72, pure shapes).

### 7.2 Other art on the page
- `Art / Peep Head / Kabir` (84:667) and `Meera` (84:669) are already exported (`assets/avatars/avatar-6`, `avatar-7`). The new components now use them: Kabir in Avatar / Pair, Meera in Control / Payment Parties. The `Art` property of Avatar / Circle and Avatar Option lists **7 preferred heads** (Arjun, Priya, Rohan, Esha, Dev, Kabir, Meera).
- `Illustration / Cover — Crowd` (7:245, 760 × 300) is the Figma cover-page hero. It isn't used in the app, so don't export or ship it.
- **Rive mapping:** none of the components in this file contains scene art, so there's nothing to map to the six `.riv` files. Screens with empty states reuse `Card / Empty State` (components-home.md §11), whose illustrations are the First Day / AllSquare Rive files; the screen specs decide which one.

---

## 8. Changes to existing components (since components-core.md / components-home.md)
Method: I re-dumped all 36 sets those two files cover (variants, sizes, layout, fills/strokes as tokens, text styles, texts, nested instances and properties, descriptions and property definitions), and compared them value by value with the two specs and with the page structure saved at the start of the first spec. Only three sets changed. **Don't edit the old files; apply these deltas on top of them.**

### 8.1 Card / Balance (13:269): new property, badge moved, curly apostrophe
New description (verbatim): “Balance total. Owed = bold black +₹ · Owe = gray −₹ · Settled = ₹0. Tap opens the per-person breakdown. Show chevron (default on). Show caption (default on): turn it off for a 96-tall card with no caption line (06-19); with the caption the card is 116. Show action: trailing Button / Primary Small “Settle up” (exposed, bottom-right). Show badge: Badge / Pill (exposed, default Overdue) at the end of the top row, so the label stops (and wraps) before it; the top row grows to 24, the card to 120. Both off by default; set the instance to FILL width for full-width cards (06-19, 07-04/07/08). Turn Show chevron off when Show badge is on. The Owed label reads “You’re owed”; Card / Balance Summary keeps the Phase 1 label (straight apostrophe) as a text override on its nested Owed card, so the Home frames don’t change until the curly apostrophe is approved there. SwiftUI: PBBalanceCard”
| What | components-home.md §6 said | Figma now |
|---|---|---|
| Properties | Type, Show chevron, Show action, Show badge | + **`Show caption`** (boolean, default **true**). Off = no caption line; the card is **96** tall instead of 116 (used on 06-19). |
| Badge position | absolute, top 16 / right 16 | **a flow child at the end of the `top` row** (after the label; hug width, 24 tall). The label (fills, wraps) stops before it. With the badge shown, the top row is **24** tall and the card **120**. Still: turn Show chevron off when Show badge is on. |
| Owed label | "You're owed" (straight ' U+0027) | **"You’re owed" (curly ’ U+2019)** in the component. The description says Card / Balance Summary keeps the straight apostrophe as an override so the Home frames don't change, but the re-dump shows **"You’re owed" with U+2019 inside Card / Balance Summary too** (13:271). See §11 #2. |
| Label sizing | fills | fills, **auto height** (wraps when the badge narrows it) |
Unchanged: 175 × 116 default, padding 16, gap 12, radius 20, `bg/card`, icons 16 pt (`money-in`/`money-out`/`check-circle`, `icon/secondary`, stroke 1.0), chevron 16 `icon/tertiary`, amount `Amount/Large`, caption `Footnote` `text/tertiary`, texts "+₹2,900" / "from 4 people", "−₹1,850" / "across 2 groups", "₹0" / "Nothing pending", and the action button absolute bottom-right (97 × 36 at (62, 56) = right 16 / bottom 24).

### 8.2 Row / Attention (13:379): detail truncates
New description (verbatim): “Due soon / overdue item on Home. Overdue = red badge + Remind (money owed to you). Due soon = white badge + Settle (money you owe). The detail after the title truncates with … when the column is full, so the row stays 88 tall. SwiftUI: PBAttentionRow”
| What | components-home.md §9 said | Figma now |
|---|---|---|
| `title row` | hugs | **fills the text column** (Overdue 180 wide, Due soon 190) |
| `detail` ("Movie tickets" / "Your share") | Subheadline `text/secondary`, hug | Subheadline `text/secondary`, **fills the rest of the title row, 1 line, truncates with …** (Overdue 125 wide at x 123, Due soon 121 at x 137). The title keeps its natural width. |
Unchanged: 362 × 88, padding 12/16, gap 12, radius 20, avatar 40 white, badge (Overdue "Overdue 3 days" / On Card "Due Fri"), amount Amount/Medium, On Card Small button (Remind 86 wide / Settle 76 wide).

### 8.3 Sheet / Container (118:1017): new `Show header`, title inset 16
New description (verbatim): “PBSheet (.presentationDetents) — Container for every picker and form sheet: kit grabber, a Title/3 title on the left and the kit glass xmark on the right, an optional search field (Show search; nested Control / Input Field exposed as “search”: set its Value#12:9 to “Search categories” etc.) and the native Content slot.
Detent=Medium: 386 wide (inset 8, anchored 8 from the bottom), radius 40, hugs its content; the slot is 354 wide. Detent=Large: 402×804 from y 70, top radius 40, the slot fills the height and clips (lists scroll under the bottom edge); the slot is 370 wide.
Content is a native SLOT: append the sheet body to the “Content” slot of the instance (or a local _Sheet / frame name instance). Never detach. Booleans: Show title, Show close, Show grabber, Show search, Show header. The title starts at 16, level with the slot content. For a sheet with no title and no close (08-10), turn Show header off (not just the two inner booleans), so the content starts 20 below the top.
SwiftUI: PBSheet”
| What | components-home.md §15 said | Figma now |
|---|---|---|
| Properties | Title, Show title, Show close, Show grabber, Show search, Content, Detent | + **`Show header`** (boolean, default **true**) |
| Header padding / title x | header padding-left 4, so the title sat at x 20 | **no left padding**: `title` at **(16, 32)**, level with the slot content. It fills (**304** wide Medium / **320** Large), 1 line, truncates. |
| Sheet with no title and no close (08-10) | "the empty header still takes its two 8 gaps, so the content starts 28 below the top" | **turn `Show header` off**: the header is removed and the content starts **20** below the sheet top (8 top padding + 4 grabber + 8 gap) |
Unchanged: Medium 386 wide (206 with a 100-tall slot), radius 40, padding 8/16/28/16; Large 402 × 804, top radius 40, bottom padding 34, clips; grabber 60 × 4; close 50 × 50 at (320, 20) / (336, 20); search Input Field 354/370 × 52; slot at (16, 78). **Sheet / Action Sheet (17:643) did NOT change**: its header still has padding-left 4 and "Add" at x 20.

### 8.4 Verified unchanged
Brand / App Mark, Brand / Logo, Button / Primary, Secondary, On Card, Destructive, Text, Icon, Add, Badge / Pill, Avatar / Circle, Avatar / Stack, Control / Page Dots, Segment, Segmented, Input Field, Divider / Line, Row / Section Header, Card / Balance Summary (except the apostrophe in 8.1), Row / Activity, Card / Empty State, Navigation / Onboarding Top Bar, Nav Header, Tab Bar Item, Tab Bar, Sheet / Action Row, Sheet / Action Sheet, Navigation / Setup Header, Control / Code Digit, Code Input, Avatar Option, Row / Currency, Card / Payment Preview. All their descriptions, properties, variant sizes, layouts, tokens and texts match the specs.
Two details the old specs don't state, both unchanged in Figma:
- `Avatar / Circle` and `Control / Avatar Option`: the `Art` swap lists **7 preferred heads** (adds Kabir, Meera). `Card / Empty State`: the `Illustration` swap lists 9 preferred illustrations.
- `Card / Payment Preview` has `Show copy` / `Show copy icon` (already covered in screens-setup.md §0.10).

---

## 9. Designer notes (verbatim, from `designer-notes.md`)
Section-level notes and captions on page 02 for the sections in this file. Component descriptions are quoted in each section above.
- **Shared (from Profile) (90:667):** “Shared (from Profile)” · “Moved from 05 Profile with their node IDs kept, then extended additively. Page 05 instances stay linked and look the same.” · captions “Settings row”, “Push header”, “Alert”, “Icons”, “Shuffle”, “Lock”, “Help”, “Category chip”.
- **Forms & Money (115:849):** “Forms & Money” · “Modal header, amount entry, payment parties, split rows and the split total bar, paywall plan cards, the multi-line text area and the composer (comments and Ask Paybak). Amounts use Amount/Display; red appears only on errors.” · captions “Modal header”, “Text area”, “Composer (Pinned=True: the white 402-wide keyboard bar with a top divider)”, “Amount display”, “Split total”, “Payment parties”, “Split person row”, “Plan card”.
- **Lists & Detail (116:872):** “Lists & Detail” · “People, group and transfer rows, title and amount heroes, notices, the confirm payment and QR code cards, and the comment and history rows of the expense detail. Nested avatars, badges and buttons are exposed.” · captions “Avatar pair”, “Comment row”, “History row”, “Person row (Compact variants are drawn for #F5F5F5 cards: their white avatar circles, pills and buttons are invisible on this white section)”, “Title row”, “Transfer row”, “Confirm payment card (Confirm → Confirmed, smart animate 250 ms)”, “Amount hero”, “QR code card (scans: https://paybak.app/i/arjun)”, “Notice card”, “Group row”.
- **Progress & Charts (116:1060):** “Progress & Charts” · “Progress bars in two heights (Small 6, Large 12), share-bar rows, budget and loan cards and the monthly chart, in chart grays. Red marks only over budget. Bar lengths and chart heights are padding overrides.” · captions “Progress bar”, “Show mark=true (examples)”, “Bar row”, “Monthly bars chart”, “Budget card”, “Loan progress card”, “Example: paid back (Kabir, 06-15)”, “Example: over budget (10-03)”.
- **Assistant & Scan (117:958):** “Assistant & Scan” · “Ask Paybak chat bubbles and the draft expense card, receipt review and item assignment rows, and the camera shutter (fallback: the iOS 27 kit has no camera chrome).” · captions “Chat bubble”, “Receipt line”, “Shutter (fallback)”, “Draft expense card (Save → Saved, smart animate 250 ms)”, “Assign item row”, “Person totals card”.
- **Feedback & Overlays (118:962):** “Feedback & Overlays” · “The toast (black capsule, no shadow) that confirms a save, and the sheet container: kit grabber, title, glass xmark on the right, optional search and a native Content slot.” · captions “Toast”, “Sheet container”, “Example: populated Content slot (Show search=true)”.
- **Illustrations & Art (7:2):** “Open Peeps + Open Doodles by Pablo Stanley (CC0). Recoloured to the illustration tokens: line #0A0A0A, fill white, tint #EBEBEB. Compose scenes from these components — never draw new people.” · name captions “Arjun”, “Priya”, “Rohan”, “Esha”, “Dev”, “Kabir”, “Meera”.
- The other section notes on page 02 (Icons, Brand, Buttons, Badges & Avatars, Controls, Cards & Rows, Navigation, Sheets, Sign-in & Setup) are unchanged and quoted in `foundations-rules.md` §7.

---

## 10. Assets
- **Icons: nothing new.** Page 02 has **65** `Icon / …` components (62 in Icons 5:2 + Shuffle, Lock, Help in Shared), and all 65 already exist in `assets/icons/` under the same kebab-case names. The Icons section hasn't grown. `assets/icons/INDEX-v2.md` records this check and lists which icons the new components use, at what size and tint.
- **Images added** (listed in `assets/images/INDEX-v2.md`):
  | File | Source | Size | Use |
  |---|---|---|---|
  | `assets/images/art-receipt-full.svg` | Art / Receipt, Size=Full (86:667), `exportAsync SVG_STRING` with live text (`svgOutlineText: false`) | 300 × 458 | demo receipt in the Scan receipt camera scene (11-07) and the simulated scanner. Text is Manrope `<text>`, so it needs the font. |
  | `assets/images/art-receipt-full@3x.png` | the same node, PNG at scale 3 | 900 × 1374 | the raster to ship for the camera scene (exact text) |
  | `assets/images/art-receipt-thumb.svg` | Art / Receipt, Size=Thumb (86:712), SVG (pure shapes) | 56 × 72 | receipt thumbnail placeholder (09-03, 11-08, 11-10) when there's no real photo |
  The outlined-text SVG of the Full receipt is 194 kB and wasn't exported; use the PNG.
- **Node data** (exact values for every component in this file): the decoded Plugin API dumps per section and the §8 re-dump weren't kept. No `get_design_context` output was saved for these sets; the Figma MCP allowance was exhausted, and the REST node JSON `.figma-cache/nodes/3-3-*.json` (regenerate with `tools/fetch_figma.py`) holds the same values.
- **References:** 2× renders of every set in `.figma-cache/components/<id>.png` (REST renders). §0.1 lists the file for each component.

---

## 11. Open questions and decisions (none block implementation)
1. **Icon stroke in nested 16/20-pt icons.** The new components keep stroke 1.5 at 16 and 20 pt; the older components scale it (16 → 1.0, 20 → 1.25). Recommendation: **one rule app-wide, scale with size (README §3 rule 4)**. It's simpler for both platforms, where SVG template images / VectorDrawables scale their strokes, and it matches the Home screens already built. Ask the designer whether the heavier 1.5 at 16 pt was intended.
2. **"You’re owed" apostrophe.** The Card / Balance component and Card / Balance Summary now show the curly ’. The description says the Home frames keep the straight ' until the curly one is approved, and screens-home.md specifies the straight one for Home. Keep Home as its screen spec says; use the curly ’ on every other screen (06-19, 07-04/07/08). If the Home frames now read curly too, switch Home to curly for consistency.
3. **Toast vs pinned CTAs.** The description places the toast 50 above the bottom edge (no tab bar), which can overlap a pinned 52-pt CTA. Keep README §5.1 #9: 16 above the CTA when one is pinned.
4. **Copy not in Figma** (proposals): Split total over-assignment "₹150 over" (Error style) · Person Totals before everything is assigned: "3 items left to assign" without the check icon · Loan partial caption "{n}% paid back".
5. **Android look of the kit Toggle/Stepper.** Proposed above: a custom 64 × 28 switch (black on-track) and a 92 × 32 capsule stepper with #767680 @ 12 % fill. Material components restyled to these footprints are fine too.
6. **QR on Android** needs an encoder. `com.google.zxing:core` (Apache-2.0, ~500 kB) is the usual choice; it's a new Gradle dependency, so it needed approval (the app now uses `com.google.zxing:core` 3.5.4). iOS uses CoreImage (no dependency).
7. **Prototype-only behaviour.** The only designed motion here is the two `CHANGE_TO … SMART_ANIMATE 250 ms ease-out` reactions (Confirm payment, Draft expense save). Everything else (bar/chart growth, toast fade, shutter press) is a suggestion (README §3 rule 12).
