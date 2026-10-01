# Paybak: Record payment, Lend money (IOU) and New group (Figma page "06 Add & Record" 77:100)

Sections covered: **Record payment** (177:29215), **Lend money (IOU)** (177:29218), **New group** (190:8353). The fourth section on this page, "Add expense" (167:21224), is specced in `screens-add-expense.md`.

| Screen id | Figma frame (node) | What it is | Spec § | 2× reference | Node JSON (`.figma-cache/`) |
|---|---|---|---|---|---|
| `recordPayment` | Record payment — form (177:29750) | Full-screen modal from the ＋ sheet | §2 | `ref/recordPayment.png` | `nodes/77-100.json` |
| `paymentRecorded` | Payment recorded (177:30023) | Pushed payment detail, payer's view while pending | §3 | `ref/paymentRecorded.png` | `nodes/77-100.json` |
| `lendMoney` | Lend money — form (185:25810) | Full-screen modal from the ＋ sheet | §4 | `ref/lendMoney.png` | `nodes/77-100.json` |
| `loanAdded` | Loan added (186:10325) | Loan detail right after Save (state Active) | §5 | `ref/loanAdded.png` | `nodes/77-100.json` |
| `loanPaidBack` | Loan — Paid back (186:26377) | Same screen, state Paid back (Kabir) | §5.3 | `ref/loanPaidBack.png` | `nodes/77-100.json` |
| `loanOverdue` | Loan — Installment overdue (186:26608) | Same screen, state Overdue (time-shifted to Tue 3 Nov) | §5.4 | `ref/loanOverdue.png` | `nodes/77-100.json` |
| `newGroup` | New group — Group (190:8356) | Full-screen modal from the ＋ sheet, segment Group | §6 | `ref/newGroup.png` | `nodes/77-100.json` |
| `newGroupProject` | New group — Project (190:12242) | Same modal, segment Project (scrolls) | §6.3 | `ref/newGroupProject.png` | `nodes/77-100.json` |
| `newGroupCreated` | Group created (190:27177) | The new, empty group detail with a toast | §7 | `ref/newGroupCreated.png` | `nodes/77-100.json` |

There are **no** "Overlay helpers" or "↳ … (overlay)" frames in these three sections.

Business rules (payments, loans, installments, reminders, groups, projects) are collected in **§8**, sample data in **§9**, the reuse map in **§1**, assets in **§10**, open questions in **§11**.

How this was read: a read-only Plugin-API dump of all nine frames to full depth (geometry, auto-layout, bound variables → token names, text styles, component + variant + property values, annotations, reactions) plus the section texts; the dumps weren't kept (see §11.1). Component descriptions not quoted in the dump were taken from the REST node JSON (`.figma-cache/nodes/77-100.json`, regenerate with `tools/fetch_figma.py`) or from `screens-settle.md` / `screens-groups.md`, which quote the same Figma descriptions. The 2× references were exported per section with `download_assets` (scale 2) and cropped to each frame; they match the REST renders `.figma-cache/renders/<node>.png` (mean difference < 0.02/255). They show the iOS status bar and home indicator, which you don't draw.

---

## 0. Conventions

- Frame = 402 × 874 pt (iPhone 17 Pro). All `x, y` are **frame coordinates** (0,0 = frame top-left) unless a line says "component-relative". pt (iOS) = dp (Android). Top safe area 62, bottom 34 (bottom safe edge y 840).
- Status bar (kit, 402×62 at y0) and home indicator (kit, 402×34 at y840) are **system UI: don't draw them**. Every frame here has them.
- "Hotspot" frames (`✕ hotspot`, `Save hotspot`, `Back hotspot`, `… segment hotspot`, `Add expense hotspot`) are **invisible prototype helpers**: don't draw them; their reactions are listed under "Navigation".
- Colours are token names from `tokens.md` (the `color/` prefix dropped) with hex. Text styles are `tokens.md` names (Manrope).
- Text is verbatim. Keep: `₹` U+20B9, `−` U+2212 (none on these screens), `·` U+00B7, `×` U+00D7 (schedule preview), `’` U+2019 curly apostrophe (e.g. "What’s it for?", "what’s spent", "won’t", "You’ll"), `“ ”` in alert copy, and the `\n` hard line break in the Record payment summary.
- Icons: `assets/icons/<name>.svg` (24-grid; scale the whole SVG: 24 → stroke 1.5, 20 → 1.25, 16 → 1.0). All icons on these screens already exist (§10).
- Avatars: `assets/avatars/avatar-N.svg` (Arjun 1, Esha 4, Dev 5, Kabir 6, Meera 7). Circle fill rule: `bg/card` #F5F5F5 on white, `bg/primary` #FFFFFF inside #F5F5F5 cards.
- Pressed states: as README §3 rule 11 (pill = fill change, text button = 50 % opacity, glass icon button = `bg/card`). For rows with no pressed variant, use `bg/card-pressed` #EBEBEB clipped to the card shape (suggestion).
- Test IDs (flow.md format `<screen>.<element>`): given per screen below. Root containers: `screen.<id>`.

---

## 1. Components used on these screens (reuse map)

### 1.1 Already specced (reuse as-is)
| Figma component | Spec | Used here as |
|---|---|---|
| Button / Primary (9:36) Large + Small | components-core.md §2.1 | "Record repayment" (Large, 362×52); "Add expense" (Large, leading Plus, inside the empty-state card); the Modal Header "Save"/"Create" pill (Small); "Settle up" Small **Disabled** on the balance card |
| Button / Secondary (9:62) Large | components-core.md §2.1 | "Remind Dev" (362×52) |
| Button / Icon (10:79) Style=Glass | components-core.md §2.3 / components-home.md §2 | back button and gear inside Navigation / Push Header |
| Badge / Pill (11:46) Inverse, Overdue | components-core.md §3.1 | "Paid back" chip (Inverse) in the loan hero; "Overdue 4 days" (Overdue, red) in an installment row |
| Avatar / Circle (11:136) Art 32/56, Icon 56, Icon On Card 40 | components-core.md §3.2 | parties, hero avatars, member rows, group tile, notice icon |
| Avatar / Stack (11:417) Count=4 | components-core.md §3.3 | members under the new group's title |
| Control / Segmented (12:249) Options=2 and 3, stretched to 362 | components-core.md §4.3 | "I lent · I borrowed", "Group · Project", "Equal · Percent · Fixed" |
| Control / Input Field (12:296) Default/Filled, no icon | components-core.md §4.4 | Name, Description, Budget |
| Divider / Line (12:302) | components-core.md §4.5 | inside rows and the loan card |
| Row / Section Header (13:223) Show action=false | components-home.md §8 | "3 monthly installments", "Members" |
| Row / Activity (13:477) Type=Expense, Surface=On Card | components-home.md §10 | the installment rows |
| Card / Balance (13:269) Type=Settled | components-home.md §6 | "Your balance ₹0" card on the new group (new properties, see §7) |
| Card / Empty State (13:541) Type=First day | components-home.md §11 | "No expenses yet." on the new group |
| Sheet / Action Row (17:641) | components-home.md §13 | "Add people" row inside the Members card (resized to 362 × 56, padding 0/16/0/8, gap 8, subtitle hidden, **tile white** on the card; see §6.2) |
| Card / Payment Preview (37:675) | screens-setup.md §0.10 | UPI preview on Record payment (hidden in the frame; variant Show copy=true) |
| Overlay / Toast (118:965) | screens-setup.md §0.11 (and §1.2.13 below) | "Payment recorded", "Loan added", "Group created" |

### 1.2 NEW components (not in components-core.md / components-home.md)
`components-app.md` is the canonical spec for page "02 Components" sections "Shared (from Profile)" (90:667), "Forms & Money" (115:849), "Lists & Detail" (116:872), "Progress & Charts" (116:1060) and "Feedback & Overlays" (118:962). It was written in parallel with this file and wins where they differ. The same components are also described in `screens-settle.md` §0.4 and `screens-groups.md` §1 (consistent with the values here). What follows is complete enough to build these screens: geometry from the instance node trees on these frames, and the component descriptions quoted verbatim where they were read.

#### 1.2.1 Navigation / Modal Header (115:886). SwiftUI `PBModalHeader`
> “PBModalHeader — Toolbar for full-screen modals (Add expense, Record payment, Lend money, New group, Ask Paybak, paywall): the kit glass xmark (cancel) on the LEFT, a centred Headline title and the confirmation pill on the right. Action=Enabled (black Button / Primary Small) · Disabled (until the form is valid) · None (Ask Paybak, paywall). The pill is the exposed nested instance “action”: set its Label#9:0 to Save / Create / Add. 362×44, placed at y 62 inside the 20pt margins. No fill, no shadow; sheets keep their xmark on the right. SwiftUI: PBModalHeader”
- 362 × 44 at (20, 62), H, space-between, centred. No fill.
- `close`: iOS 27 kit "Button - Liquid Glass - Symbol" (Style=Glass, enabled), **44 × 44** circle at (20, 62), SF Symbol `xmark` (U+100184 "􀆄", SF Pro Semibold 19, colour kit Labels-Vibrant-Controls/Primary ≈ #1A1A1A). iOS: `ToolbarItem(placement: .topBarLeading)` with `Image(systemName: "xmark")` (system glass). Android: 44 dp circle, glass fallback (fill #FFFFFF @72 %, 1 dp inside border #FFFFFF @60 %, shadow y4 blur16 #0A0A0A @8 %) and `close.svg` drawn at 38 × 38 dp (its 12-unit glyph becomes the same 19 dp ✕ as the SF Symbol, as in screens-home.md §5.2), tinted #1A1A1A, centred.
- `title`: Headline, `text/primary`, centred, box 200 × 22 at (101, 73), 1 line, truncate end.
- `action`: Button / Primary **Small** (36 tall, padding 0/16, capsule), right-aligned, top y 66. "Save" = 67 × 36 at (315, 66); "Create" = 81 × 36 at (301, 66). Enabled = `bg/inverse` + `text/inverse`; Disabled = `bg/disabled` #E0E0E0 + `text/disabled` #A3A3A3, not tappable.

#### 1.2.2 Navigation / Push Header (97:1082). SwiftUI `PBPushHeader`
> “Pushed-screen header, 44 tall: glass back button, centred Headline title (Show title), and a trailing action. Trailing=Text: glass capsule text action (Save). Trailing=Icon: exposed glass Button / Icon (default Settings; gear on 10-01). Trailing=Wide Text: … Trailing=None: back only. Tall scrolling frames set the instance to fix position and give it a color/bg/primary fill. Built with .navigationTitle and a toolbar. SwiftUI: PBPushHeader”
- 362 × 44 at (20, 62), H, space-between, centred.
- `Back`: Button / Icon Style=Glass + `chevron-left.svg` 24, 44 × 44 at (20, 62). Tap = pop.
- `title`: Headline `text/primary`, centred, 200 × 22 box at (101, 73). Hidden when Show title = false.
- Trailing=Text: glass capsule, 44 tall, hugs, min width 44, padding 0/16, fill `bg/glass` (#FFFFFF @72 %) + 1 pt inside stroke `border/glass-highlight` + `Material/Glass Small`; label Headline `text/primary` ("Edit" = 64 × 44 at (318, 62)). Pressed: fill `bg/card`.
- Trailing=Icon: Button / Icon Style=Glass, 44 × 44 at (338, 62) (gear = `settings.svg` on the new group).
- Trailing=None: back + title only.
- iOS: `NavigationStack` + `.navigationTitle` + `.navigationBarTitleDisplayMode(.inline)` + `ToolbarItem(.topBarTrailing)` gives these glass buttons on iOS 26+. Android: custom 44 dp row with the glass fallback; `BackHandler` = back.

#### 1.2.3 Control / Amount Display (125:1084). SwiftUI `PBAmountField`
> “PBAmountField — the amount-first entry at the top of Add expense, Record payment, Lend money and the receipt review (06-01/02/11/13 · 08-04 · 11-10 · 11-13). Two nested Control / Category Chip instances (exposed): “currency” (INR, opens the currency sheet) and “date” (Today, opens the Date sheet). Show date chip: on for 06-01/02 and 11-10; off for 06-11, 06-13, 08-04 and 11-13. The amount uses Amount/Display (Manrope ExtraBold 56/64). Empty = the “₹0” placeholder in text/tertiary with the 2×56 caret (not bound to Amount); Focused = the typed Amount + caret; Filled = Amount, no caret. Helper (Footnote, secondary) e.g. “You owe Meera ₹450 in Flat 302”. Amount entry always uses the kit keyboard (Number Pad: the kit has no Decimal Pad), never a custom keypad. SwiftUI: PBAmountField”
- 362 wide, V, padding 4 top/bottom (`space/4`), gap 12 (`space/12`), children centred. Height 120, or 138 with the helper.
- `chips` row (H gap 8), 36 tall, centred: `currency` chip = Control / Category Chip Selected=False Leading=None, label = currency code ("INR", 57 × 36). The `date` chip is hidden on all screens in this file.
- `value` column (centred, gap 0): `amount row` (H gap 2): `amount` **Amount/Display** (ExtraBold 56/64, −2 %), centred, hug; State Filled = `text/primary`, no caret. `helper` (Show helper) **Footnote** `text/secondary`, centred, hug width.
- Input: the **system decimal pad** (iOS `.keyboardType(.decimalPad)`; Android `KeyboardType.Decimal`), never a custom keypad. Figma draws the kit Number Pad only because "the kit has no Decimal Pad"; the Add expense note 06-01 says "the decimal pad opens". Allow at most 2 decimals (the currency's minor unit). Indian digit grouping for INR (₹6,000; ₹1,00,000). For very long amounts shrink the font (min scale ≈ 0.5), never wrap.

#### 1.2.4 Control / Payment Parties (125:1085). SwiftUI `PBPaymentParties`
> “PBPaymentParties — who paid whom on Record payment (06-11, 08-04): a #F5F5F5 r20 card, 96 tall, with a From tile, Icon / Arrow Right (icon/tertiary) and a To tile. Each tile is an exposed Avatar / Circle 56 (“from avatar” / “to avatar”, white circle on the card) with a Footnote label and a Headline name, and opens a person picker. Default: You (Arjun) → Meera. SwiftUI: PBPaymentParties”
- 362 × 96, H, padding 20 top/bottom (`space/20`), 16 sides (`layout/card-padding`), gap 8 (`space/8`), items centred, fill `bg/card`, radius 20 (`radius/card`).
- `from` tile fills (147 × 56), H gap 12: Avatar / Circle 56 Art, **fill `bg/primary`** · text column (gap 0): label **Footnote** `text/tertiary` + name **Headline** `text/primary` (1 line, truncate).
- `arrow`: `arrow-right.svg` 20 × 20, `icon/tertiary` #A3A3A3.
- `to` tile: same as from.

#### 1.2.5 Control / Category Chip (64:4185). SwiftUI `PBCategoryChip`
> “Filter and people chip, 36 tall. Selected = inverse fill. Leading=None: text only (avatar categories, currency, date). Leading=Icon: 16 icon swap (the “Add” chip uses Plus). Leading=Avatar: exposed Avatar / Circle 24 for people pickers (Selected=False gives it a white avatar circle so the face reads on the #F5F5F5 chip). Show remove adds a trailing Close 16. SwiftUI: PBCategoryChip”
- Used here only as Leading=None: H, **height 36** (`size/button-sm`), hug width, padding 0/16 (`space/16`), gap 6, capsule (`radius/full`). Label **Button/Small** (SemiBold 15/20, −0.25 %), label at (16, 8) chip-relative.
- Selected=False: fill `bg/card` #F5F5F5, label `text/primary`. Selected=True: fill `bg/inverse` #0A0A0A, label `text/inverse`.
- No pressed variant (suggested: `bg/card-pressed` / `bg/inverse-pressed`). Extend the hit area to 44 tall.

#### 1.2.6 Row / Setting (97:996). SwiftUI `PBSettingRow`
> “Settings list row for #F5F5F5 card groups, 56 tall (grows with a subtitle). Trailing: Chevron (push), Toggle (exposed kit switch; On track tinted color/bg/inverse), Stepper (exposed kit stepper), Check (black tick, single select), Unchecked (empty 24 slot), None. Tone=Destructive: text/destructive + icon/destructive, never a chevron. Optional icon (Show icon off moves the title and the divider inset to 16), value, subtitle, and an exposed Badge / Pill (Inverse “Pro” / “Try free”). Turn Show value off on Toggle, Stepper, Check and Unchecked rows. SwiftUI: PBSettingRow”
- 362 wide, **min height 56**, no fill (the screen draws the `bg/card` radius-20 group; rows stack with 0 gap and the group clips), H, padding 12 top/bottom (`space/12`), 16 sides (`space/16`), gap 12 (`space/12`), items centred.
- `icon` 24 × 24 at row (16, 16): `icon/primary` (Default) / `icon/destructive` #C93636 (Destructive). Show icon=false → text starts at row x 16.
- `text` column fills (V gap 2): `title` **Headline** `text/primary` (Destructive: `text/destructive`), truncate; `subtitle` (Show subtitle) **Footnote** `text/secondary`, wraps → row 66 tall with one subtitle line.
- `value` (Show value) **Body** `text/secondary`, hug, 1 line, right before the trailing element.
- Trailing Chevron: `chevron-right.svg` **20 × 20**, `icon/tertiary`, right edge at row x 346 (frame x 366).
- Trailing Toggle: kit **Toggle - Switch** 64 × 28, right-aligned (frame x 302), On track tinted **`bg/inverse` #0A0A0A**, white knob 38 × 24 (inset 2, on the right when on). iOS: `Toggle("", isOn:).labelsHidden().tint(Color.bgInverse)`. Android: custom 64 × 28 dp switch (on track #0A0A0A, off track #E0E0E0 per iOS look, white 38 × 24 knob, radius 100), don't use a default Material Switch as-is.
- Trailing Stepper: kit **Stepper** 92 × 32 (radius 100): two 46 × 32 halves, fill kit "Fills/Tertiary" (≈ #767680 @12 %), a 1 × 24 separator (kit "Labels/Tertiary" ≈ #3C3C43 @30 %), SF Symbols `minus` / `plus` (SF Pro Semibold 17, kit Labels/Primary #000). iOS: native `Stepper` (labels hidden). Android: a 92 × 32 dp capsule with − | + drawn the same way.
- Trailing None: nothing.
- `divider row` (absolute, row bottom, 362 × 1): 36-wide inset when the icon shows + Divider / Line Inset=Leading (its own 16 inset) → hairline `border/subtle` #EBEBEB from **row x 52 → 362** (frame x 72 → 382) with an icon, **row x 16 → 362** (frame x 36 → 382) without. Hidden on the last row of a group (Show divider=false).

#### 1.2.7 Header / Amount Hero (128:2004), Avatar / Pair (116:1005). SwiftUI `PBAmountHero`, `PBAvatarPair`
> “PBAmountHero — the left-aligned hero of an expense, payment or loan detail (06-10/12/14/15/16 · 08-06 · 09-03/05/06). A 56 leading, then Title (Title/2, wraps), Amount (Title/1), Meta (Footnote, secondary) and up to 3 chips. Leading=Icon: exposed Avatar / Circle 56 Type=Icon (the category icon: Food, Bed…). Leading=Avatar: exposed Avatar / Circle 56 Art (the other person on a loan). Leading=Pair: exposed Avatar / Pair 56 (payer → receiver). Show chips + exposed Badge / Pill “chip 1” (Muted, group), “chip 2” (Muted, category; Show chip 2) and “chip 3” (Inverse, status such as “Disputed” or “Paid back”; Show chip 3). Status chips are black or gray, never red. SwiftUI: PBAmountHero”

> “PBAvatarPair — “From → To” pair for transfers and payments: two nested Avatar / Circle (exposed as “from” and “to”: set Art / Type / Initials on each) with Icon / Arrow Right in icon/tertiary between them. Size=32 (16 arrow, gap 4) for Row / Transfer · Size=56 (20 arrow, gap 8) for Header / Amount Hero Leading=Pair (06-12, 08-06). Default: Arjun → Kabir. SwiftUI: PBAvatarPair”

(Quoted via `screens-settle.md` §0.4, which read them from Figma.) Variants used here: Leading=Pair (paymentRecorded) and Leading=Avatar (loans). On `loanPaidBack` the instance shows only "chip 1", overridden to Style=**Inverse** "Paid back" (chip 2 and chip 3 hidden).
- 362 wide, V, gap 12 (`space/12`), no fill. Height 158 without chips, 194 with chips.
  1. Leading (56 tall):
     - Leading=Avatar: Avatar / Circle 56 Art at (20, 122), fill `bg/card` (on white).
     - Leading=Pair: **Avatar / Pair** Size=56 = H gap 8, centred: Avatar / Circle 56 Art (payer) · `arrow-right.svg` 20 × 20 `icon/tertiary` · Avatar / Circle 56 Art (payee); 148 × 56 → avatars at x 20 and x 112, arrow at x 84, y 140. Circle fills `bg/card`.
  2. `text` column (fills, V gap 2): `title` **Title/2** `text/primary` (fills, wraps) · `amount` **Title/1** `text/primary` (hug, 38 tall) · `meta` **Footnote** `text/secondary` (fills).
  3. `chips` (Show chips): H gap 8, 24 tall, up to 3 Badge / Pill (Show chip 2 / Show chip 3). Used once: "Paid back" Style=Inverse.
- Frame positions (top at y 122): title y 190 (h30), amount y 222 (h38), meta y 262 (h18), chips y 292 (h24).

#### 1.2.8 Card / Notice (129:1976), Layout=Leading, Actions=None. SwiftUI `PBNoticeCard`
> “PBNoticeCard — an in-flow notice on #F5F5F5 r20 (06-12 · 07-11 · 08-03 · 08-06 · 09-06 · 10-05 · 10-06 · 11-03): pending confirmation, invite a guest, simplified debts, a dispute, a read-only project, a Pro lock. Layout=Leading: exposed icon circle Avatar / Circle 40 Type=Icon On Card (set its Icon: Activity, Shuffle, Mail, Flag, Lock, Check Circle), Title (Headline) with Show badge (exposed Badge / Pill, default Inverse “Pro”) on the title line, Body (Subheadline, secondary, wraps). Layout=Centered: … Actions=None · One · Two … Show title off hides the whole title line … All gray and black: never red. SwiftUI: PBNoticeCard” (full text in `screens-settle.md` §0.4-D)
- 362 wide, fill `bg/card`, radius 20, padding 16 all sides (`layout/card-padding`), V gap 16. Height 76 with one body line.
- `content` H gap 12: `icon` = Avatar / Circle **40 Type=Icon On Card** (white circle `bg/primary`, icon 20 × 20 `icon/primary`) · `text` column (fills, V gap 2): `title line` (H gap 8: `title` **Headline** `text/primary` + optional Badge / Pill, hidden here) + `body` **Subheadline** `text/secondary` (wraps).
- Other variants (other Layouts / Actions) exist but are not used on these screens.

#### 1.2.9 Card / Loan Progress (145:2155), State = Active | Paid back; Control / Progress Bar (116:1099)
Section description (Progress & Charts): “Progress bars in two heights (Small 6, Large 12), share-bar rows, budget and loan cards and the monthly chart, in chart grays. Red marks only over budget. Bar lengths and chart heights are padding overrides.”
Loan Progress description (verbatim, from the REST node JSON `.figma-cache/nodes/77-100.json`): “PBLoanProgressCard — an IOU’s progress on the loan detail (06-14 · 06-15 · 06-16). #F5F5F5 r20, padding 16, gap 12; 362 wide (≈141 tall). Three stat columns (Footnote label over Amount/Medium value): Original → Paid → Remaining, a divider, an exposed Control / Progress Bar Large (layer “bar”, fill = share paid back), then Caption (Footnote, secondary). State=Active: bar at 0% (“fill” hidden; set bar › track › fill.paddingRight = W×(1−p) and show it for partial repayments), Caption “0% paid back”. State=Paid back: bar at 100% and Icon / Check Circle 16 before the Caption (“Paid back on 14 Sep”). No red here: an overdue installment shows its red badge on its Row / Activity. Text props share one default (the Dev loan, ₹6,000 · ₹0 · ₹6,000): a Paid back instance sets them, e.g. Kabir ₹4,500 · ₹4,500 · ₹0 · “Paid back on 14 Sep” (see the example beside the set). SwiftUI: PBLoanProgressCard”
Progress Bar description (quoted via `screens-groups.md` §1.3): “PBProgressBar — Budget, loan and share bars. Size=Small (6 tall, inside Row / Group and Row / Bar) · Large (12 tall, Card / Budget and Card / Loan Progress). Full-radius color/chart/track track; use the instance at FILL width. State=Default (fill only) · Projected … · Over … Percentages are PADDING overrides … For 0% hide “fill”. …” Loans only use Size=Large, State=Default, Show mark=false.
- Card: 362 × 141, fill `bg/card`, radius 20, padding 16 all sides, V gap 12 (`space/12`).
  1. `stats` 330 × 42, H gap 12, three equal columns (each 102 wide, fill): `label` **Footnote** `text/secondary` (18) + `value` **Amount/Medium** `text/primary` (22), gap 2. Columns: Original · Paid · Remaining (x 36, 150, 264 in the frame).
  2. Divider / Line Inset=None, 330 × 1, `border/subtle`.
  3. `bar` = Control / Progress Bar **Size=Large**, 330 × **12**: track `chart/track` #EBEBEB, capsule, clips; fill segment `chart/fill` #0A0A0A capsule, width = 330 × paid / original (0 % → no fill segment at all; 100 % → full width). Show mark = false.
  4. `caption` row H gap 6, 18 tall: State=Paid back adds `check-circle.svg` **16 × 16** `icon/primary` before the text; text **Footnote** `text/secondary`.
- State Active caption: "{pct}% paid back" (e.g. "0% paid back"). State Paid back caption: "Paid back on {d MMM}" with the check icon; bar full.
- Values stay `text/primary` in both states (Figma: Remaining "₹0" is **not** grey here).

#### 1.2.10 Row / Person (127:2252), Size=Compact. SwiftUI `PBPersonRow`
> “PBPersonRow — the one people row (split pickers, balances, breakdowns, contacts, expense splits, chat answers). Exposed Avatar / Circle (40 Regular / 32 Compact), Name (Headline) + Show tag (exposed Badge / Pill Muted “Guest”, always next to the name), Subtitle (Subheadline), trailing, Show divider (inset to the name). … Size=Compact: 56, avatar 32 in a white circle and an On Card tag, for use inside a card. Trailing: … Value = Amount (Headline, e.g. ₹700 or 25%) … Remove = Icon / Close 20 (icon/secondary) … None. … SwiftUI: PBPersonRow” (full text in `screens-settle.md` §0.4-C)
- 362 × 56 in a `bg/card` group, H, padding 6 top/bottom (`space/6`), 16 sides (`layout/card-padding`), gap 12, centred.
- `avatar` Avatar / Circle **32** Art, fill `bg/primary` (on the card), at row (16, 12).
- `text` column (fills, V gap 2): `name line` (H gap 8: `name` **Headline** `text/primary`, 1 line, hug + optional tag, hidden here) + `subtitle` (Show subtitle) **Subheadline** `text/secondary`, 1 line.
- Trailing:
  - None (the "You" row on Group).
  - Remove: `close.svg` **20 × 20** `icon/secondary` #6B6B6B at row x 326 (frame x 346). Tap = remove the member (min 44 hit area).
  - Value: right-aligned column (V gap 2): `value` **Headline** `text/primary` ("25%"), optional amount label hidden.
- `divider` (absolute, row bottom): Divider / Line Inset=None, from row x 60 to the right edge (frame x 80 → 382, 302 wide). Show divider=true on every person row here (the Add people row follows).
- Hidden component defaults you'll see in Figma props (not shown on these screens): Amount "₹700", Amount label "Due Sun 4 Oct", Subtitle "Dinner at Olive Garden", Status "Settled".

#### 1.2.11 Header / Title Row (128:1857), Leading=Tile. SwiftUI `PBTitleHeader`
> “PBTitleHeader — the content header under a Push Header on group, friend and project detail (06-19 · 07-04/07/08/11 · 10-01 · 10-06), always 16 below the header. Left-aligned: a 56 leading, then Title (Title/2) with Show tag (exposed Badge / Pill Muted: “Guest”, “Archived”) and Subtitle (Subheadline, secondary; Show subtitle off on 07-11). Leading=Tile: exposed Avatar / Circle 56 Type=Icon (group type: Plane, Home, People, Tag; projects: Drone, Package). Leading=Avatar: exposed Avatar / Circle 56 (Art, or Initials “AR” for a guest). Show members: exposed Avatar / Stack (Count 2–4) 8 below the meta, so the row is 96 tall with members and 56 without; a title-only meta is centred on the 56 leading. SwiftUI: PBTitleHeader” (quoted via `screens-groups.md` §1.5)
- 362 wide, H gap 16 (`space/16`), top-aligned. Height 96 with the member stack.
- `tile` Avatar / Circle **56 Type=Icon**, fill `bg/card`, icon 24 × 24 `icon/primary` centred.
- `content` (fills, V gap 8): `meta` (56 tall, V gap 2, vertically centred): `title line` (H gap 8: `title` **Title/2** `text/primary` + optional tag, hidden) + `subtitle` **Subheadline** `text/secondary`; `members` = Avatar / Stack Count=n (32 pt avatars, −8 overlap, 2-pt white outside ring).

#### 1.2.12 Overlay / Alert (102:1115). SwiftUI `PBAlert` (used by "Cancel payment", not drawn in these frames)
> “iOS 27 alert card, 300 wide, radius 34: Headline title, Subheadline message, two 36pt capsules sharing the width (both exposed). Action=Destructive: Button / Secondary + Button / Destructive (Discard, Delete). Action=Primary: Button / Secondary + Button / Primary when nothing is destroyed (Settle up). Built with .alert: a .cancel button plus a .destructive or default button. SwiftUI: PBAlert”
- 300 wide, hugs (142 with one-line texts), padding 20, gap 20, radius 34, fill `bg/primary`, effect Material/Glass; title Headline centred, message Subheadline `text/secondary` centred (gap 4); two Small buttons (126 × 36 each, gap 8). Centred over the 40 % scrim. iOS: the real `.alert` with `.cancel` + `.destructive`; Android: a custom Dialog drawing this card, not dismissible by tapping outside.

#### 1.2.13 Overlay / Toast (118:965), placement on these screens
Black capsule 44 tall, hugs; padding 16 left / 20 right, gap 8; `check-circle.svg` 20 `icon/inverse` + label **Button/Small** `text/inverse`; no shadow. Fades in, stays 2 s, fades out; never tappable. Placement on these screens:
- `paymentRecorded`, `newGroupCreated`: bottom edge **50 above the screen bottom** (frame y 780–824), centred.
- `loanAdded`: bottom edge **12 above the pinned "Record repayment" button** (frame y 732–776), centred.

---

## 2. `recordPayment`: Record payment — form (177:29750)

**Purpose:** log a payment made outside Paybak (cash, UPI, bank, card, other) between you and one person, usually to clear an open balance. It stays pending until the other person confirms.
**Container:** full-screen modal presented from the Home ＋ sheet row "Record payment" (Home prototype: sheet rows use MOVE_IN from the bottom; present with iOS `.fullScreenCover`, Android a full-screen destination sliding up). No tab bar.
**Background:** `bg/primary`. Frame auto-layout: V, padding 62/20/34/20, gap 16 (`space/16`). Figma overflow: none (fits). Build it as a vertical ScrollView under a fixed header anyway (Dynamic Type, the UPI preview adds 122 pt).

### 2.1 Section notes (verbatim)
- Section title (177:29216): "Record payment"
- Section subtitle (177:29217): "Logs a payment made outside Paybak from the ＋ sheet. It stays pending until the other person confirms."

### 2.2 Designer notes (verbatim)
- Caption 06-11 (177:29967): "Logs a payment made outside Paybak, prefilled from the open ₹450 with Meera; picking UPI shows meera@okhdfcbank with Copy. A smaller amount records a partial payment and the rest stays owed. Paying more becomes a balance in your favour, and tapping INR shows the amount converted at today’s rate."
- Annotation on the method chips (177:29827): "Picking UPI shows Card / Payment Preview under the chips: Meera Iyer · meera@okhdfcbank with Copy. It is hidden for Cash."

### 2.3 Elements, top to bottom
1. **Modal header** (Navigation / Modal Header, Action=Enabled), (20, 62, 362, 44): ✕ glass close (20, 62, 44, 44); title "Record payment"; action **"Save"** (315, 66, 67, 36).
2. **Form** column (20, 122, 362, hug 578), V gap **24** (`layout/section-gap`):
   1. **Payment** group (20, 122, 362, 254), V gap **20** (`space/20`):
      - **Payment parties** (Control / Payment Parties), (20, 122, 362, 96):
        - From tile (36, 142, 147, 56): avatar Arjun (`avatar-1`) 56 at (36, 142), white circle; label "From" (104, 150) Footnote `text/tertiary`; name "You" (104, 168) Headline `text/primary`.
        - Arrow (191, 160, 20, 20) `arrow-right.svg`, `icon/tertiary`.
        - To tile (219, 142, 147, 56): avatar Meera (`avatar-7`) 56 at (219, 142), white; label "To" (287, 150); name "Meera" (287, 168).
      - **Amount display** (Control / Amount Display, State=Filled, Show helper=true, Show date chip=false), (20, 238, 362, 138):
        - Currency chip "INR" (172.5, 242, 57, 36), unselected (`bg/card`), label at (188.5, 250).
        - Amount "₹450" (132.5, 290, 137, 64) Amount/Display `text/primary`, centred.
        - Helper "You owe Meera ₹450 in Flat 302" (106.5, 354, 189, 18) Footnote `text/secondary`, centred.
   2. **Method** group (20, 400, 362, 60; 182 with the UPI preview), V gap 16:
      - Method picker (V gap 4): label "Method" (20, 400) Subheadline `text/secondary`; chip row (20, 424, 362, 36), H gap **6** (`space/6`), clips:
        | Chip | Rect | State |
        |---|---|---|
        | Cash | (20, 424, 69, 36) | **Selected** (`bg/inverse`, white label) |
        | UPI | (95, 424, 57, 36) | unselected |
        | Bank | (158, 424, 67, 36) | unselected |
        | Card | (231, 424, 67, 36) | unselected |
        | Other | (304, 424, 74, 36) | unselected |
        Single select. Default Cash.
      - **UPI payment preview** (Card / Payment Preview, Show copy=true, Show copy icon=false), (20, 476, 362, 106), **hidden in the frame** (Cash is selected). Visible only when UPI is selected; it pushes everything below down by 122 (16 gap + 106). Content: caption "What friends see" is the component default; the card shows the **recipient**: avatar Meera, name "Meera Iyer", UPI "meera@okhdfcbank", and the exposed **Button / On Card Small "Copy"** with the Copy icon (instead of the plain icon button used on setup3). Copy → copies the UPI ID to the clipboard and shows the toast "UPI ID copied". If the recipient has no UPI ID saved, hide the card (proposal).
   3. **Details** group (20, 484, 362, 216), V gap 12:
      - Rows card (20, 484, 362, 168), `bg/card`, radius 20, clips; three Row / Setting, Trailing=Chevron, Show icon=true:
        | Row | Rect | Icon | Title | Value | Divider |
        |---|---|---|---|---|---|
        | For | (20, 484, 362, 56) | `groups.svg` | For | Flat 302 | yes |
        | Date | (20, 540, 362, 56) | `calendar.svg` | Date | Wed 30 Sep | yes |
        | Proof | (20, 596, 362, 56) | `camera.svg` | Proof | Add photo (optional) | no |
        Value text Body `text/secondary` right-aligned before the chevron (chevron at (346, row y + 18)).
      - **Summary** (20, 664, 362, 36) Footnote `text/secondary`, 2 lines with a hard break: "You paid Meera ₹450 in cash for Flat 302.\nMeera will be asked to confirm. Paybak never moves money."
3. Nothing below y 700 (white).

### 2.4 Behaviour
- **Prefill** (caption): opened from the ＋ sheet, From = You, To = the person you owe most recently / the open balance in context, Amount = that open balance, For = the group it belongs to. Demo: Meera, ₹450, Flat 302 (the "Electricity bill" −₹450 on Home). Helper = "You owe {To} {open balance} in {For}". If nothing is open with anyone, open with To empty and the amount Empty ("₹0" placeholder + caret, decimal pad up) (proposal).
- **Other entry points** prefill the same form: Settle up suggestion (08-04: "You → Kabir · ₹1,400", UPI preselected, `screens-settle.md` §4), a friend page (07-08 note: "Record payment opens the page 06/08 form prefilled “Rohan → You · ₹800”", i.e. From = the friend, To = You, when they owe you), and "Record repayment" on a loan (§5.5).
- **From / To tiles** → person picker sheet (Sheet / Container, list of friends; reuse the people picker from Add expense "Split with" 06-03). Choosing "You" on the To side swaps the direction (someone paid you). Both can't be the same person. Helper and summary update ("{Name} owes you ₹… in …" when they owe you; proposal).
- **Amount**: tap → decimal pad. Rules (caption): **less than the open balance = a partial payment; the rest stays owed. More than the open balance = the excess becomes a balance in your favour** (they now owe you the difference). Helper keeps showing the open balance.
- **Currency chip "INR"** → currency sheet (the Add expense "Currency" sheet, 06-08). Caption: "tapping INR shows the amount converted at today’s rate": when a currency other than the group/profile currency is chosen, show the converted value under the amount (e.g. helper line "≈ ₹450 at today’s rate"; proposal for the exact copy) and store the rate with the payment.
- **Method chips**: single select; UPI reveals the preview (above). Store the method.
- **For** → picker of the shared contexts with this person (groups you share + "No group"/direct). Changing it recomputes the prefill amount (the open balance in that context) only if the user hasn't edited the amount (proposal).
- **Date** → the Date sheet from Add expense (06-09 note: "The form’s “Today” chip opens the same sheet, titled “Date” with a “Set date” button and no hint, so you can back-date"). Default today; display "EEE d MMM" ("Wed 30 Sep"). No future dates (proposal).
- **Proof** → photo picker (camera or library; real on both platforms). After picking, the value shows "1 photo" (proposal) and the photo is stored with the payment. Optional.
- **Summary** template: "You paid {To} {amount} {method phrase} for {For}.\n{To} will be asked to confirm. Paybak never moves money." Method phrase: Cash **"in cash"** (06-11) and UPI **"by UPI"** (designed on page 08, 08-04: "You paid Kabir ₹1,400 by UPI for Goa Trip."); proposals: Bank "by bank transfer", Card "by card", Other: omit the phrase. Without a group: drop " for {For}". When someone paid you: "{From} paid you {amount} …" (proposal).
- **Method default**: Cash in this frame (Meera has a UPI ID but Cash is selected); page 08 preselects UPI when prefilled from a Settle up suggestion. Rule: from the ＋ sheet default Cash; from Settle up default UPI when the payee has one (`screens-settle.md` §4.2).
- **Save** enabled when amount > 0 and both parties are set (Action=Disabled otherwise). Save → creates a **pending** payment (§8.1), dismisses the modal and shows the new payment's detail (`paymentRecorded`) with the toast "Payment recorded". Prototype: `Save hotspot` ON_CLICK → NAVIGATE "Payment recorded" (177:30023), **DISSOLVE 300 ms EASE_OUT**.
- **✕** → dismiss without saving (if the user changed anything, confirm with Overlay / Alert Destructive "Discard changes?" per the Modal Header description; proposal copy "Discard this payment?" / "Keep editing" / "Discard"). Prototype: `✕ hotspot` → URL of the Home prototype (Home — Active, 24:5), i.e. back to Home.
- Keyboard: decimal pad for the amount only. The Save button stays in the header (no bottom CTA), so no keyboard avoidance is needed; tap outside or scroll to dismiss.

### 2.5 Navigation
| Element | Destination |
|---|---|
| ✕ (20, 62) | dismiss → previous screen (Home) |
| Save | `paymentRecorded` (dissolve 300 ms), toast "Payment recorded" |
| From / To tile | person picker sheet |
| INR chip | currency sheet (06-08) |
| Method chips | select in place (UPI → preview) |
| Copy (UPI preview) | clipboard + toast "UPI ID copied" |
| For | context picker sheet |
| Date | Date sheet (06-09 variant "Date" / "Set date") |
| Proof | system photo picker / camera |

Test IDs: `recordPayment.close`, `recordPayment.save`, `recordPayment.from`, `recordPayment.to`, `recordPayment.amount`, `recordPayment.currency`, `recordPayment.method.<cash|upi|bank|card|other>`, `recordPayment.preview`, `recordPayment.preview.copy`, `recordPayment.for`, `recordPayment.date`, `recordPayment.proof`, `recordPayment.summary`.

---

## 3. `paymentRecorded`: Payment recorded (177:30023)

**Purpose:** the payment detail after Save, from the payer's side, while it waits for the other person's confirmation.
**Container:** pushed detail screen (lands here when the Record payment modal closes). Push header with back + "Edit". No tab bar.
**Scroll:** the frame scrolls vertically (content 952 tall). Fixed (non-scrolling) layers: white strip "Scroll edge (top)" (0, 0, 402, 106) `bg/primary`, the Push header, and the toast. Content scrolls under the white strip.

### 3.1 Designer notes (verbatim)
- Caption 06-12 (177:30290): "The payer’s view until Meera confirms. Pending is gray, not red, and nothing changes yet: Home still shows −₹1,850, and once she confirms you’re settled in Flat 302. If Meera taps Not received, this card switches to Not received with her note."
- Cross-page note on 08-06 (page 08, quoted in `screens-settle.md` §5.4), which is about this frame: "After Save you land on the payment with a “Payment recorded” toast. It stays “Pending confirmation”, and Home keeps −₹1,850, until Kabir confirms. “Paid to” appears only for methods with an ID (UPI, Bank), so the Cash payment on 06-12 has 6 rows."
- Annotation on "Cancel payment" (177:30240): "In the app this opens Overlay / Alert (Action=Destructive): “Cancel this payment?” · “Meera won’t be asked to confirm. You’ll still owe her ₹450.” · “Keep” · “Cancel payment”. No prototype link."
- Annotation on "Scroll spacer" (224:25550): "Prototype only: scroll room so Cancel payment can scroll clear of the toast. In code, inset the scroll content by the toast’s height."

### 3.2 Elements (scroll content: V, padding 62/20/34/20, gap **24** `layout/section-gap`)
1. **Top** group (20, 62, 362, 218), V gap 16:
   - Header space 362 × 44 at y 62 (placeholder under the fixed Push header).
   - **Amount hero** (Header / Amount Hero, Leading=Pair, Show chips=false), (20, 122, 362, 158):
     - Pair (20, 122, 148, 56): Arjun (`avatar-1`) 56 at (20, 122) → `arrow-right.svg` 20 `icon/tertiary` at (84, 140) → Meera (`avatar-7`) 56 at (112, 122). Circle fills `bg/card`.
     - Title "You paid Meera" (20, 190, 362, 30) Title/2 `text/primary`.
     - Amount "₹450" (20, 222, 79, 38) Title/1 `text/primary`.
     - Meta "Cash · Today · Flat 302" (20, 262, 362, 18) Footnote `text/secondary`.
2. **Status** notice (Card / Notice, Layout=Leading, Actions=None), (20, 304, 362, 76): icon circle 40 white at (36, 320) with `activity.svg` 20 `icon/primary`; title "Pending confirmation" (88, 320) Headline `text/primary`; body "Waiting for Meera to confirm" (88, 344) Subheadline `text/secondary`. **Grey, never red** (caption).
3. **Details** group (20, 404, 362, 362), V gap 8:
   - Details card (20, 404, 362, 336) `bg/card` radius 20, six Row / Setting, **Trailing=None, Show icon=false** (title at x 36, value right-aligned ending x 366, dividers x 36 → 382):
     | Row | y | Title | Value | Divider |
     |---|---|---|---|---|
     | From | 404 | From | You | yes |
     | To | 460 | To | Meera | yes |
     | Method | 516 | Method | Cash | yes |
     | Date | 572 | Date | Wed 30 Sep | yes |
     | For | 628 | For | Flat 302 | yes |
     | Proof | 684 | Proof | None | no |
     For UPI or Bank a 7th row **"Paid to" / {UPI ID or account}** goes between Method and Date (page 08 note; 08-06 shows "Paid to / kabir@okaxis"), which makes the card 392 tall.
   - Footnote "Your balance updates once Meera confirms." (20, 748, 362, 18) Footnote `text/secondary`.
4. **Actions card** (20, 790, 362, 56) `bg/card` radius 20: Row / Setting **Tone=Destructive**, Trailing=None, icon `delete.svg` 24 `icon/destructive` at (36, 806), title "Cancel payment" (72, 807) Headline `text/destructive`.
5. Scroll spacer 362 × 48 at y 870 (prototype-only). In code: bottom content inset = toast height (44) + its 50 offset while the toast is visible, or simply bottom inset 48 + safe area (matches Figma).

Fixed layers:
- **Push header** (Navigation / Push Header, Trailing=Text), (20, 62, 362, 44): back (glass, chevron-left), title "Payment", trailing glass capsule "Edit".
- **Toast** "Payment recorded" (104, 780, 195, 44), check-circle icon. Shown once on arrival from Save.

### 3.3 States
- **Pending** (drawn): notice "Pending confirmation" / "Waiting for {name} to confirm" with the Activity icon; footnote "Your balance updates once {name} confirms."; Cancel payment row shown.
- **Not received** (caption, not drawn): "this card switches to Not received with her note". Proposal (kept identical to `screens-settle.md` §5.2): same Card / Notice, icon `flag.svg` in the white circle, title "Not received", body "{Name} says they haven’t received it" followed by her note on the next line (the note is the editable text she sent, e.g. page 08's template "Hi Arjun, I haven’t received ₹450 for Flat 302 yet. Could you check?"). Still grey, never red. The payment stays unconfirmed; Edit and Cancel payment stay available. Balances unchanged.
- **Confirmed** (not drawn): notice title "Confirmed" (proposal) with `check-circle.svg`, body "Meera confirmed on {date}"; the footnote and the Cancel payment row disappear; balances update (§8.1).
- Debug menu (flow.md "the other person's side"): "Meera confirms" / "Meera: Not received (with note)".

### 3.4 Navigation
| Element | Destination |
|---|---|
| Back (20, 62) | pop → where Record payment was opened from (Home). Prototype `Back hotspot` → URL Home — Active (24:5) |
| Edit | re-open the Record payment modal prefilled with this payment (edit mode; Save updates the same pending payment). No reaction on this frame; the same button on 08-06 is wired MOVE_IN from the bottom, 300 ms ease-out |
| Cancel payment | Overlay / Alert Destructive (copy from the annotation, **designed**): title "Cancel this payment?", message "{Name} won’t be asked to confirm. You’ll still owe {her/him/them} {amount}." (sample "Meera won’t be asked to confirm. You’ll still owe her ₹450."), buttons "Keep" (Button / Secondary Small) and "Cancel payment" (Button / Destructive Small). Confirm → delete the pending payment and pop back. No prototype link. Pronoun: we don't store gender, so use the name: "You’ll still owe Meera ₹450." unless a pronoun is known (proposal). The same alert applies to 08-06 (page 08) |

Test IDs: `paymentRecorded.back`, `paymentRecorded.edit`, `paymentRecorded.status`, `paymentRecorded.cancel`, `paymentRecorded.toast`.

---

## 4. `lendMoney`: Lend money — form (185:25810)

**Purpose:** record a direct loan (an IOU, not a shared bill) to or from one person, optionally paid back in installments.
**Container:** full-screen modal from the Home ＋ sheet row "Lend money (IOU)". Frame V, padding 62/20/34/20, gap **16**; overflow none (fits at y 778). Build as a ScrollView under the fixed Modal header.

### 4.1 Section notes (verbatim)
- Section title (177:29219): "Lend money (IOU)"
- Section subtitle (177:29220): "Full-screen modal from the ＋ sheet for a direct loan with optional installments. Save lands on the loan: original, paid and remaining, then the schedule. Two states follow."

### 4.2 Designer notes (verbatim)
- Caption 06-13 (185:26048): "A direct loan, not a shared bill. Turning on Installments reveals the count, frequency and first due date, and previews the schedule; with it off, a single Due row with quick chips appears instead."
- Annotation on the Installments row (185:25924): "Installments on: reveals Number of installments, Repeats and First due, plus the schedule preview. Off: a single Due row with quick chips (Tomorrow · This weekend · Next week · Pick date) instead."

### 4.3 Elements, top to bottom
1. **Modal header** (Action=Enabled): title "Lend money", action "Save" (315, 66, 67, 36).
2. **Direction** segmented control (Control / Segmented Options=2, stretched), (20, 122, 362, 36): segments (178 × 30 each) "I lent" (**selected**, black) and "I borrowed".
3. **Amount display** (State=Filled, Show helper=false, Show date chip=false), (20, 174, 362, 120): chip "INR" (172.5, 178, 57, 36); amount "₹6,000" (103, 226, 196, 64) Amount/Display `text/primary`.
4. **Form** (20, 310, 362, 468), V gap **24**:
   1. **Card A** (20, 310, 362, 168) `bg/card` r20, Row / Setting ×3, Trailing=Chevron:
      | Row | Rect | Icon | Title | Value | Divider |
      |---|---|---|---|---|---|
      | Lent to | (20, 310, 362, 56) | `profile.svg` | Lent to | Dev | yes |
      | Reason | (20, 366, 362, 56) | `receipt.svg` | Reason | Laptop repair | yes |
      | Date | (20, 422, 362, 56) | `calendar.svg` | Date | Wed 30 Sep | no |
   2. **Installments** group (20, 502, 362, 276), V gap 12:
      - **Card B** (20, 502, 362, 246) `bg/card` r20:
        | Row | Rect | Trailing | Icon | Title / subtitle | Value | Divider |
        |---|---|---|---|---|---|---|
        | Installments | (20, 502, 362, **66**) | **Toggle, on** (302, 521, 64, 28) | `lend.svg` | "Installments" / subtitle "Paid back in parts" (Footnote `text/secondary`) | – | yes |
        | Number of installments | (20, 568, 362, **68**) | **Stepper** (274, 586, 92, 32) | `split.svg` | "Number of installments" (wraps to 2 lines in its 169-wide column: "Number of / installments") | "3" (253, 590) Body `text/secondary` | yes |
        | Repeats | (20, 636, 362, 56) | Chevron | `repeat.svg` | Repeats | Monthly | yes |
        | First due | (20, 692, 362, 56) | Chevron | `calendar.svg` | First due | Fri 30 Oct | no |
      - **Schedule preview** (20, 760, 362, 18) Footnote `text/secondary`: "3 × ₹2,000 · Fri 30 Oct, Mon 30 Nov and Wed 30 Dec".

### 4.4 Behaviour
- **Direction**: "I lent" (you are owed) / "I borrowed" (you owe). With "I borrowed" the first row title becomes "Borrowed from" (proposal; not drawn) and the detail title "You borrowed from {name}".
- **Amount**: decimal pad; the form opens with the amount focused (Amount Display Empty: "₹0" placeholder `text/tertiary` + 2 × 56 caret, pad up), like Add expense 06-01 (proposal; the frame shows the Filled state).
- **Lent to** → person picker sheet (friends list; single person). **Reason** → text entry (a sheet with a text field or inline edit; proposal) — optional. **Date** → the Date sheet ("Date" / "Set date"), default today.
- **Currency chip** → currency sheet (06-08).
- **Installments toggle** (default in the frame: on):
  - On: shows Number of installments (stepper), Repeats (chevron → picker), First due (chevron → date sheet) and the schedule preview line.
  - Off: those three rows and the preview are replaced by a single **Due** row (Row / Setting, `calendar.svg`, title "Due", value = the chosen date or "None"; proposal) with a row of quick chips **Tomorrow · This weekend · Next week · Pick date** (Control / Category Chip Leading=None; "Pick date" opens the date sheet). Same chips and date rules as Add expense's Due date sheet (06-09).
- **Number of installments** stepper: min 2, max 24 (proposal); value shown as the row value. **Repeats** options: Weekly, Every 2 weeks, Monthly (proposal; only "Monthly" is designed). **First due**: default = loan date + one period (Wed 30 Sep + 1 month = Fri 30 Oct, matches Figma).
- **Schedule preview** = "{n} × {installment amount} · {date 1}, {date 2} … and {date n}" in "EEE d MMM". For n > 3 (proposal): "{n} × {amount} · {period adverb} from {first} to {last}" (e.g. "6 × ₹1,000 · monthly from Fri 30 Oct to Wed 31 Mar"). Derivation in §8.2.
- **Save** enabled when amount > 0 and a person is chosen. Save → creates the loan (§8.2), dismisses the modal, shows the loan detail `loanAdded` with the toast "Loan added". Prototype: `Save hotspot` → NAVIGATE "Loan added" (186:10325), **DISSOLVE 300 ms EASE_OUT**.
- **✕** → dismiss (confirm discard if dirty). Prototype: URL → Home — Active.

### 4.5 Navigation
| Element | Destination |
|---|---|
| ✕ | dismiss → Home |
| Save | `loanAdded` (dissolve 300 ms) + toast "Loan added" |
| I lent / I borrowed | toggles direction in place |
| INR chip | currency sheet |
| Lent to | person picker sheet |
| Reason | text entry |
| Date, First due, Pick date | date sheet |
| Repeats | frequency picker sheet (Sheet / Container with Row / Setting Trailing=Check rows) |
| Installments toggle, stepper | in place |

Test IDs: `lendMoney.close`, `lendMoney.save`, `lendMoney.direction.<lent|borrowed>`, `lendMoney.amount`, `lendMoney.currency`, `lendMoney.person`, `lendMoney.reason`, `lendMoney.date`, `lendMoney.installments` (toggle), `lendMoney.count` (stepper), `lendMoney.count.minus`, `lendMoney.count.plus`, `lendMoney.repeats`, `lendMoney.firstDue`, `lendMoney.schedule`, `lendMoney.due`, `lendMoney.due.<tomorrow|weekend|nextWeek|pick>`.

---

## 5. Loan detail: `loanAdded` (186:10325), `loanPaidBack` (186:26377), `loanOverdue` (186:26608)

One screen, three states. **Container:** pushed detail screen (after Save; also from friend history 07-08 and from Activity). Push header "Loan". Frame V, padding 62/20/34/20, gap **16**; Figma overflow none. Build as a ScrollView (more installments overflow) with the bottom buttons **pinned** above the bottom safe area and a bottom content inset of (button stack height + 16).

### 5.1 Designer notes (verbatim)
- Caption 06-14 (186:10542), Loan added: "Original → paid so far → remaining, with a progress bar, then the schedule. Dev sees the same loan from his side and gets a reminder before each due date."
- Caption 06-15 (186:26560), Loan — Paid back: "State: a loan repaid in full, reached from the loan in Kabir’s friend history (same layout as 07-08). The due dates are derived as monthly on the 12th (12 Jul, 12 Aug, 12 Sep), so the third payment was 2 days late; that’s noted in gray, because red is only for what is overdue now."
- Caption 06-16 (186:26792), Loan — Installment overdue: "State: time-shifted to Tue 3 Nov. Installment 1 is overdue 4 days, and it’s the only red on screen. Reminders follow the default schedule (2 days before, on the due date, then every 3 days when overdue): Wed 28 Oct, Fri 30 Oct and Mon 2 Nov, so the last one went out Mon 2 Nov."

### 5.2 `loanAdded` (state Active, just created), elements
1. **Push header** (Trailing=Text) (20, 62, 362, 44): back, title "Loan", trailing "Edit".
2. **Amount hero** (Leading=Avatar, no chips) (20, 122, 362, 158): avatar Dev (`avatar-5`) 56 at (20, 122), fill `bg/card`; title "You lent Dev" (20, 190); amount "₹6,000" (20, 222, 112, 38) Title/1; meta "Laptop repair · Today" (20, 262) Footnote `text/secondary`.
3. **Loan progress** (Card / Loan Progress, State=Active) (20, 296, 362, 141):
   - Original "₹6,000" (36, 312) · Paid "₹0" (150, 312) · Remaining "₹6,000" (264, 312).
   - Divider (36, 366, 330, 1).
   - Bar (36, 379, 330, 12): track only (0 % → no fill).
   - Caption "0% paid back" (36, 403) Footnote `text/secondary`.
4. **Installments** (20, 453, 362, 236), V gap 12:
   - Section header "3 monthly installments" (20, 453, 362, 32), Title/3, no action.
   - Installments card (20, 497, 362, 192) `bg/card` r20, padding 0/16 (`layout/card-padding` left/right), three Row / Activity (Type=Expense, Direction=In, Surface=On Card, Show date=false, Show amount=true, Icon=`calendar.svg`), each 330 × 64 at x 36; icon circle white 40 at (36, row y + 12), icon 20 at (46, row y + 22):
     | Row | y | Title | Subtitle | Amount | Divider |
     |---|---|---|---|---|---|
     | 1 | 497 | Installment 1 | Due Fri 30 Oct | ₹2,000 | yes (x 88 → 366) |
     | 2 | 561 | Installment 2 | Due Mon 30 Nov | ₹2,000 | yes |
     | 3 | 625 | Installment 3 | Due Wed 30 Dec | ₹2,000 | no |
     Amount Amount/Medium `text/primary`.
5. **Record repayment** (Button / Primary Large, no icon) (20, 788, 362, 52), pinned: bottom edge = bottom safe-area edge (y 840).
6. **Toast** "Loan added" (128, 732, 147, 44), 12 above the button.

### 5.3 `loanPaidBack` (state Paid back): differences from 5.2
- Push header **Trailing=None** (back + "Loan" only; no Edit).
- Hero **Show chips=true** → 194 tall: avatar **Kabir** (`avatar-6`); title "You lent Kabir"; amount "₹4,500" (108 × 38); meta "Bike service · 12 Jun"; chip row (20, 292): Badge / Pill **Inverse** "Paid back" (77 × 24, black, white Caption/1).
- Loan progress **State=Paid back** at (20, 332): Original "₹4,500" · Paid "₹4,500" · Remaining "₹0" (still `text/primary`); bar fully filled (`chart/fill` #0A0A0A, 330 wide); caption = `check-circle.svg` 16 `icon/primary` at (36, 440) + "Paid back on 14 Sep" (58, 439).
- Installments at y 489: header "3 monthly installments"; card (20, 533, 362, 192); rows use **`check-circle.svg`** instead of the calendar:
  | Row | y | Title | Subtitle | Amount |
  |---|---|---|---|---|
  | 1 | 533 | Installment 1 | Paid 10 Jul | ₹1,500 |
  | 2 | 597 | Installment 2 | Paid 12 Aug | ₹1,500 |
  | 3 | 661 | Installment 3 | Paid 14 Sep · 2 days late | ₹1,500 |
  "2 days late" stays **grey** (`text/secondary`, part of the subtitle): red is only for what's overdue now.
- **No bottom buttons, no toast.** No prototype reactions on this frame.

### 5.4 `loanOverdue` (state Active with an overdue installment; "now" = Tue 3 Nov): differences from 5.2
- Push header Trailing=Text "Edit" (same as loanAdded).
- Hero meta "Laptop repair · Wed 30 Sep" (the date instead of "Today", since it's no longer today).
- Loan progress identical to loanAdded (₹6,000 · ₹0 · ₹6,000, "0% paid back").
- Installments group (20, 453, 362, 262): header; list (20, 497, 362, 218) V gap 8 = card (192) + footnote.
  - Row 1 **Show badge=true**: Badge / Pill **Overdue** (red `bg/destructive` #C93636, white Caption/1) "Overdue 4 days" in the trailing column under "₹2,000" (the date slot). **The only red on screen.** Rows 2–3 unchanged ("Due Mon 30 Nov", "Due Wed 30 Dec").
  - Footnote "Last reminder sent Mon 2 Nov" (20, 697, 362, 18) Footnote `text/secondary`.
- Bottom buttons (pinned): **"Remind Dev"** Button / Secondary Large (20, 724, 362, 52) (`bg/card`, `text/primary`) and **"Record repayment"** Primary (20, 788, 362, 52); gap 12.
- No toast, no reactions.

### 5.5 Behaviour and navigation
| Element | Destination / action |
|---|---|
| Back | pop. After Save the stack is Home → loan detail, so back → Home (prototype `Back hotspot` on loanAdded → URL Home — Active 24:5). From friend history → back to it |
| Edit | re-open the Lend money modal prefilled (edit mode). Changing amount/count/dates re-derives the unpaid installments; paid ones stay. Proposal (not designed) |
| Record repayment | open Record payment (§2) prefilled: From = Dev, To = You, amount = the next unpaid installment (₹2,000), For = this loan (value "Loan · Laptop repair", proposal). Saving creates a pending repayment; once confirmed it is applied to installments in due-date order (§8.2) |
| Remind Dev | the Remind sheet from page 08 (`screens-settle.md` §6: Sheet / Container "Remind Dev", pre-written editable message, Friendly/Neutral tone, "Send in Paybak" or "Share…"), opened over this screen. 08-07 note: "“Send in Paybak” closes the sheet and shows Overlay / Toast “Reminder sent to Rohan” for 2 s (16 above the tab bar on Home, 50 above the bottom elsewhere). The balances don’t change." Here: toast "Reminder sent to Dev", placed 12 above the button stack (like "Loan added"), and the footnote becomes "Last reminder sent {today}". Shown only when an installment is overdue |
| Installment rows | not tappable (proposal) |

Test IDs: `loan.back`, `loan.edit`, `loan.hero`, `loan.progress`, `loan.installment.<n>`, `loan.installment.<n>.badge`, `loan.lastReminder`, `loan.remind`, `loan.recordRepayment`, `loan.toast`.

---

## 6. `newGroup` / `newGroupProject`: New group (190:8356 / 190:12242)

One modal with a segmented control **Group | Project**; both are the same draft (name and members carry over when switching).
**Container:** full-screen modal from the Home ＋ sheet row "New group". Modal header "New group", action **"Create"**.

### 6.1 Section and designer notes (verbatim)
- Section title (190:8354): "New group"
- Section subtitle (190:8355): "Full-screen modal from the ＋ sheet for a new group or project, switched with a segmented control. Create lands on the new, empty group with a toast."
- Caption 06-17 (190:8710), Group: "A group for people who share often. Simplify debts is on by default, so settling up takes the fewest payments, and the currency defaults to your INR."
- Caption 06-18 (190:12611), Project: "The same draft switched to a Project, with an optional description, cover photo, budget and contribution rule. Equal gives each of the 4 members 25%, and Percent or Fixed turns those values into fields; components come after creation, and the budget is left empty to avoid inventing numbers."
- Annotation on "Simplify debts" (190:8665): "On by default: settling up takes the fewest payments."

### 6.2 `newGroup` (segment Group), elements
Frame V, padding 62/20/34/20, gap **16**; overflow none (content ends y 816).
1. **Modal header** (20, 62, 362, 44): ✕, title "New group", action "Create" (301, 66, 81, 36).
2. **Segmented** (Options=2, stretched) (20, 122, 362, 36): "Group" (**selected**) · "Project". Segments 178 × 30 at x 23 and x 201.
3. **Form** (20, 174, 362, 642), V gap **24**:
   1. **Details** (20, 174, 362, 156), V gap 16:
      - **Name** (Control / Input Field, State=Filled, label on, helper off) (20, 174, 362, 80): label "Name" Subheadline `text/secondary`; field (20, 202, 362, 52) `bg/card` r14, value "Weekend Trek" Body `text/primary`.
      - **Type picker** (20, 270, 362, 60), V gap 4: label "Type" Subheadline `text/secondary`; chips (20, 294), H gap **8** (`space/8`): "Trip" (20, 294, 60, 36) **selected**, "Home" (88, 294, 74, 36), "Friends" (170, 294, 85, 36), "Other" (263, 294, 74, 36). Single select.
   2. **Members** (20, 354, 362, 316), V gap **4**:
      - Section header "Members" (20, 354, 362, 32), no action.
      - Members card (20, 390, 362, 280) `bg/card` r20, clips:
        | Row | Rect | Component | Avatar | Name | Subtitle | Trailing |
        |---|---|---|---|---|---|---|
        | You | (20, 390, 362, 56) | Row / Person Compact, Trailing=None, Show subtitle | Arjun 32 at (36, 402), white | "You" | "Arjun Mehta" | – |
        | Esha | (20, 446, 362, 56) | Trailing=Remove | Esha (`avatar-4`) at (36, 458) | "Esha Kapoor" | – | ✕ `close.svg` 20 `icon/secondary` at (346, 464) |
        | Dev | (20, 502, 362, 56) | Trailing=Remove | Dev (`avatar-5`) | "Dev Malhotra" | – | ✕ at (346, 520) |
        | Kabir | (20, 558, 362, 56) | Trailing=Remove | Kabir (`avatar-6`) | "Kabir Singh" | – | ✕ at (346, 576) |
        | Add people | (20, 614, 362, 56) | Sheet / Action Row (Default), resized: padding 0/16/0/8, gap 8 | – | "Add people" (Headline) | (hidden) | chevron 20 `icon/tertiary` |
        Person-row dividers from x 80 to 382 under each of the four person rows. The Add people row: icon tile 44 × 44 at (28, 620), radius 14, **white** (`bg/primary`; verified in the 2× render: the tile reads as a white rounded square on the grey card, per the "on a card, nested pills and avatars turn white" rule), `user-add.svg` 24 `icon/primary` centred at (38, 630); title "Add people" Headline at x 80 (aligned with the names), subtitle hidden; chevron `chevron-right.svg` 20 `icon/tertiary` at (346, 632). No divider.
   3. **Settings card** (20, 694, 362, 122) `bg/card` r20:
      - **Currency** Row / Setting Trailing=Chevron (20, 694, 362, 56): `exchange.svg`, title "Currency", value "INR ₹", divider.
      - **Simplify debts** Row / Setting Trailing=**Toggle, on** (20, 750, 362, 66): `shuffle.svg`, title "Simplify debts", subtitle "Fewer payments when settling up", no divider.

### 6.3 `newGroupProject` (segment Project): differences
Frame overflow **vertical** (content height 1120: form ends y 1086 + 34). Fixed layers: "Header background (fixed)" white rect (0, 0, 402, 106) `bg/primary`, the Modal header, the ✕ and segment hotspots. **The segmented control scrolls with the content.**
1. Modal header identical ("New group", "Create").
2. Segmented (20, 122): "Group" · "Project" (**selected**).
3. Form (20, 174, 362, 912), V gap 24:
   1. **Details** (20, 174, 362, 248), V gap 16:
      - Name (Filled) "Weekend Trek" (20, 174, 362, 80).
      - **Description** (Input Field, State=Default, label on, helper off) (20, 270, 362, 80): label "Description"; placeholder "What’s it for?" Body `text/tertiary`.
      - **Cover card** (20, 366, 362, 56) `bg/card` r20: Row / Setting Trailing=Chevron, `camera.svg`, title "Add cover photo", no value, no divider.
   2. **Budget** (Input Field, State=Default, label + helper) (20, 446, 362, 106): label "Budget"; placeholder "₹0" `text/tertiary` (currency symbol of the chosen currency); helper "Optional. Spending is tracked against it." Footnote `text/tertiary`. Decimal pad.
   3. **Contribution** (20, 576, 362, 90), V gap 8: label "Contribution" Subheadline `text/secondary`; Segmented **Options=3** stretched to 362 (segments ≈ 118.7 × 30): "Equal" (**selected**) · "Percent" · "Fixed"; helper "Everyone pays the same share of what’s spent." (20, 648) Footnote `text/secondary`.
   4. **Members** (20, 690, 362, 316): header "Members"; card (20, 726, 362, 280) with Row / Person Compact **Trailing=Value**: "You" (no subtitle here), "Esha Kapoor", "Dev Malhotra", "Kabir Singh", each value **"25%"** Headline `text/primary` right-aligned (x 332–366); dividers x 80 → 382; then "Add people" row (20, 950). No remove ✕ in this mode.
   5. **Currency card** (20, 1030, 362, 56): Currency "INR ₹" (no divider).
   - Project mode has **no Type chips and no Simplify debts row**.

### 6.4 Behaviour
- **Segment switch** keeps the draft (name, members, currency). Prototype: `Project segment hotspot` (201, 125, 178, 30) → NAVIGATE "New group — Project" (instant); `Group segment hotspot` (23, 125, 178, 30) → "New group — Group" (instant). Implement as an in-place switch (optionally cross-fade the form 200 ms; proposal).
- **Name**: required; Create stays disabled (Modal Header Action=Disabled) until the name is non-empty (proposal: trim whitespace). Keyboard: default text, Return = Done.
- **Type** (Group only): Trip · Home · Friends · Other. The type sets the group's tile icon and the first word of the subtitle. Header / Title Row's description lists the group tiles as "Plane, Home, People, Tag", so: Trip → `plane.svg` (confirmed on Group created), Home → `home.svg`, Friends → `people.svg`, Other → `tag.svg` (order-matched; `screens-groups.md` may confirm). A blank form has no type selected until the user picks one (proposal; Create doesn't require it, default Other); the frame shows Trip because the name is "Weekend Trek".
- **Members**: "You" is always first, not removable, subtitle = your full name. Remove ✕ removes a person from the draft. **Add people** → people picker sheet (multi-select friends and guests who aren't on Paybak yet, like Add expense 06-03 "Split with"; guests are marked Guest).
- **Currency**: defaults to your profile currency ("the currency defaults to your INR"); value format "{CODE} {symbol}" ("INR ₹"). Tap → currency sheet (06-08).
- **Simplify debts** (Group): toggle, **on by default**. Stored on the group; settle-up suggestions use the minimal-transfer algorithm when on, pairwise debts when off (§8.3).
- **Description / cover photo / budget** (Project): all optional. Cover → photo picker (camera/library); after picking show the photo (proposal: the row value "Photo added" or a 56-tall thumbnail; not designed). Budget empty by default ("left empty to avoid inventing numbers").
- **Contribution** (Project): Equal (default): every member's value = 100 / n %, shown read-only ("25%" for 4). **Percent** or **Fixed** "turns those values into fields": each member's trailing value becomes an editable field (Percent: "%" values that must add up to 100 %; Fixed: amounts in the project currency). Helper text per mode: Equal "Everyone pays the same share of what’s spent." (designed); proposals: Percent "Set each person’s share. Shares must add up to 100%.", Fixed "Set a fixed amount for each person." While the values don't add up, show the helper in `text/destructive` and keep Create disabled (proposal, mirrors the split editor's error rule).
- **Create** → creates the group/project (§8.3), dismisses the modal and pushes the new group's detail (`newGroupCreated`) with the toast "Group created". Prototype: `Create hotspot` (301, 66, 81, 36) → NAVIGATE "Group created" (190:27177), DISSOLVE 300 ms EASE_OUT. For a Project, land on the new project's detail (page 10 Projects, empty; toast "Project created" is a proposal since the Toast copy list only has "Group created").
- **✕** → dismiss (confirm if dirty). Prototype: URL → Home — Active.
- Keyboard (Project, scrolling form): let the ScrollView scroll the focused field above the keyboard (iOS default; Android `imePadding()` + `bringIntoViewRequester`).

### 6.5 Navigation
| Element | Destination |
|---|---|
| ✕ | dismiss → Home |
| Create | `newGroupCreated` (dissolve 300 ms) + toast "Group created" (Project → project detail) |
| Group / Project segments | switch mode in place |
| Type chips | select |
| Remove ✕ on a member | remove from draft |
| Add people | people picker sheet |
| Currency | currency sheet |
| Simplify debts | toggle |
| Add cover photo | photo picker |
| Equal / Percent / Fixed | contribution mode |

Test IDs: `newGroup.close`, `newGroup.create`, `newGroup.mode.<group|project>`, `newGroup.name`, `newGroup.type.<trip|home|friends|other>`, `newGroup.member.<n>`, `newGroup.member.<n>.remove`, `newGroup.addPeople`, `newGroup.currency`, `newGroup.simplify`, `newGroup.description`, `newGroup.cover`, `newGroup.budget`, `newGroup.contribution.<equal|percent|fixed>`, `newGroup.member.<n>.share`.

---

## 7. `newGroupCreated`: Group created (190:27177)

**Purpose:** the new, empty group right after Create.
**Container:** pushed group detail (the 07-04 Group detail template in its empty state), toast on arrival. Frame V, padding 62/20/34/20, gap **24**; overflow none.

### 7.1 Designer notes (verbatim)
- Caption 06-19 (190:27447): "The new group opens empty with a toast, and its header, balance card and gear match 07-04 Group detail. The Balances card and the expense list appear after the first expense, and Settle up stays disabled until there’s something to settle."

### 7.2 Elements
1. **Top** (20, 62, 362, 268), V gap 16:
   - **Push header** (Trailing=**Icon**, Show title=false) (20, 62, 362, 44): glass back (20, 62); glass gear `settings.svg` (338, 62). No title.
   - **Title block** (20, 122, 362, 96), V gap 6: Header / Title Row (Leading=Tile, Show subtitle, Show members):
     - Tile (20, 122, 56, 56) Avatar / Circle Type=Icon, fill `bg/card`, `plane.svg` 24 `icon/primary` at (36, 138).
     - Title "Weekend Trek" (92, 124, 159, 30) Title/2 `text/primary`.
     - Subtitle "Trip · 4 members · INR" (92, 156, 290, 20) Subheadline `text/secondary`.
     - Members: Avatar / Stack Count=4 (92, 186, 104, 32): Arjun, Esha, Dev, Kabir (32 pt, 2-pt white outside rings, 8 overlap; later ones on top).
   - **Balance card** (Card / Balance, Type=Settled, Show chevron=false, **Show caption=false**, **Show action=true**, Show badge=false) (20, 234, 362, 96). Current component description (REST JSON; these properties are newer than components-home.md §6): “… Show caption (default on): turn it off for a 96-tall card with no caption line (06-19); with the caption the card is 116. Show action: trailing Button / Primary Small “Settle up” (exposed, bottom-right). … set the instance to FILL width for full-width cards (06-19, 07-04/07/08). …” Contents: `check-circle.svg` 16 `icon/secondary` at (36, 252); label **"Your balance"** (58, 250) Subheadline `text/secondary` (overrides the Settled default "All settled"); amount "₹0" (36, 282, 34, 32) Amount/Large **`text/tertiary`**; action **"Settle up"** Button / Primary Small **State=Disabled** (269, 270, 97, 36): `bg/disabled` #E0E0E0, `text/disabled` #A3A3A3, absolute bottom 24 / right 16. Card height 96 = 16 + 20 + 12 + 32 + 16 (no caption line).
2. **Empty state** (Card / Empty State, Type=First day, Show primary action=true, Show secondary action=false) (20, 354, 362, 406), `bg/card` r20, padding 24, gap 20, centred:
   - Illustration slot (81, 378, 240, 180): Figma "Illustration / Empty — First day" (7:216), **the same component as Home First day** (pixel-identical in the 2× refs). On device: **`paybak-homefirstday.riv`**, artboard `First Day` 240 × 180 = the slot (no bleed), state machine `First Day`, view model `HomeFirstDay`/`Instance`, `reduceMotion` ← OS setting, own tap listener (see rive.md). Static fallback `assets/images/empty-first-day.svg`.
   - Title "No expenses yet." (44, 578, 314, 30) Title/2 `text/primary`, centred.
   - Body "Add the first expense and Paybak will split it for everyone." (44, 616, 314, 48) Body `text/secondary`, centred, 2 lines.
   - Primary action "Add expense" (Button / Primary Large, leading `plus.svg` 20 white) (44, 684, 314, 52). No secondary ("Invite friends") button.
3. **Toast** "Group created" (118, 780, 167, 44), 50 above the bottom.

### 7.3 Behaviour and navigation
| Element | Destination |
|---|---|
| Back | pop → the Groups tab list (prototype `Back hotspot` → URL Groups, 167:14881 = `groupsList` on page 07). Since the modal was opened from the ＋ sheet on any tab, implement Create as: dismiss the modal, switch to the Groups tab, push this group (proposal consistent with the prototype) |
| Gear | group settings (07-xx, `screens-groups.md`) |
| Settle up | disabled until someone owes something in this group |
| Add expense | Add expense modal (`addExpenseEmpty`) with this group preselected. Prototype `Add expense hotspot` → NAVIGATE "Add expense — Empty" (176:17454), **MOVE_IN direction TOP (slides up from the bottom), 300 ms EASE_OUT** |
| Balance card | not tappable while ₹0 (proposal) |

After the first expense the screen becomes the normal 07-04 Group detail (Balances card and expense list appear; caption). Test IDs: `group.back`, `group.settings`, `group.title`, `group.balance`, `group.settleUp`, `group.empty`, `group.addExpense`, `group.toast`.

---

## 8. Business rules (from the notes; code these in the on-device data layer)

### 8.1 Payments (Record payment)
- A payment = {id, from, to, amount, currency, rate (if not the context currency), method (cash|upi|bank|card|other), date, context (group id or direct/loan id), proof photo?, status, createdAt, confirmedAt?, note?}.
- **Status**: `pending` on save → `confirmed` when the recipient confirms, or `notReceived` (with the recipient's note) when they tap Not received; `cancelled` removes it (Cancel payment).
- **Pending payments don't change any balance** ("nothing changes yet: Home still shows −₹1,850"). Only `confirmed` payments reduce/increase balances. Pending is shown grey (Card / Notice, Muted badges), never red.
- **Partial payment**: amount < open balance in that context → after confirmation the remainder stays owed.
- **Overpayment**: amount > open balance → after confirmation the excess becomes a balance in the payer's favour (the recipient now owes the payer the difference).
- **Currency**: if a different currency is chosen, convert at "today’s rate" to the context currency and store the rate with the payment (same rule as expenses 06-08). Rates: a bundled table or the last known rates (no network requirement); for the demo, INR only.
- Demo check (06-12): after Arjun records ₹450 cash to Meera for Flat 302 → Home still shows You owe −₹1,850 (₹1,400 Goa Trip + ₹450 Flat 302). Once Meera confirms → Flat 302 between Arjun and Meera is settled (caption), so Home's You owe becomes −₹1,400 with the caption "across 1 group" (singular form is a proposal).
- The recipient's side (simulated, debug menu): a confirm request (Home "Confirm payment" card, local notification with Confirm / Not received actions per flow.md).

### 8.2 Loans (IOU) and installments
- A loan = {id, direction (lent|borrowed), counterparty, amount, currency, reason?, date, installments: on|off, count n, frequency (weekly|every 2 weeks|monthly), firstDue, due (when installments off), repayments[]}. A loan is a direct debt between two people, not a group expense.
- **Installment amount** = amount / n. If it doesn't divide evenly to the currency's minor unit, give the leftover minor units one each to the earliest installments (proposal, consistent with 06-05 "leftover paisa rotate fairly"). Demo: ₹6,000 / 3 = ₹2,000; ₹4,500 / 3 = ₹1,500.
- **Due-date derivation**: due(i) = firstDue + (i − 1) × period, i = 1…n. Monthly keeps the same day of month ("monthly on the 12th (12 Jul, 12 Aug, 12 Sep)"; "Fri 30 Oct, Mon 30 Nov and Wed 30 Dec"); if a month is shorter, use its last day (e.g. 31 Jan → 28/29 Feb → 31 Mar: always anchor on firstDue's day, not the previous due; proposal). Weekly = +7 days, every 2 weeks = +14 days. Default firstDue = loan date + 1 period.
- Installments off: a single due date (optional). Quick chips (share one implementation with Add expense's Due date sheet 06-09): Tomorrow = today + 1 day (Thu 1 Oct); This weekend = the coming Sunday (designed: 06-02 "“This weekend” sets the due date to Sun 4 Oct" on Wed 30 Sep); Next week = the next Monday (Mon 5 Oct; proposal); Pick date = the date sheet. On a Sunday, "This weekend" = today (proposal).
- **Repayments**: recorded via Record payment (pending → confirmed). Confirmed repayments are applied to installments in due-date order; an installment is paid when its amount is fully covered; its **paid date** is the date of the repayment that completed it. Partial coverage stays on the current installment; overpaying the whole loan becomes a balance in the payer's favour.
- **Display rules** (per installment row):
  - Unpaid, not overdue: icon `calendar.svg`, subtitle "Due {EEE d MMM}".
  - Paid: icon `check-circle.svg`, subtitle "Paid {d MMM}"; if paid after its due date: append " · {k} day(s) late" in the same **grey** subtitle (k = paid date − due date in days). Paid early/on time: nothing extra (Installment 1 paid 10 Jul for a 12 Jul due date shows just "Paid 10 Jul").
  - Overdue now (unpaid and today > due date): keep "Due {date}" and show Badge / Pill **Overdue** "Overdue {d} day(s)" (d = today − due date; Tue 3 Nov − Fri 30 Oct = 4). **Red only for overdue now.**
- **Loan card**: Original = amount; Paid = sum of confirmed repayments (capped at amount for the bar); Remaining = max(0, amount − paid); caption "{round(paid / amount × 100)}% paid back" while unpaid; when Remaining = 0: state Paid back, hero chip "Paid back", caption "Paid back on {date of the last repayment}" with the check icon, no bottom buttons, header without Edit.
- **Section header**: "{n} {frequency adjective} installments" ("3 monthly installments"; proposals "weekly", "fortnightly"). With installments off: a single row "Due {date}" under the header "Due" (proposal).
- **Hero meta**: "{reason} · {loan date}". Figma shows three forms: "Laptop repair · Today" (viewed the same day), "Laptop repair · Wed 30 Sep" (viewed 34 days later, on Tue 3 Nov) and "Bike service · 12 Jun" (viewed 110 days later). Rule that reproduces all three (proposal): "Today", "Yesterday", else "EEE d MMM" if less than 60 days ago, else "d MMM"; append the year when it isn't the current year. Without a reason: just the date.
- **Reminders** (default schedule from Settings): **2 days before the due date, on the due date, then every 3 days while overdue**. Example (06-16): due Fri 30 Oct → Wed 28 Oct, Fri 30 Oct, Mon 2 Nov (next would be Thu 5 Nov). The same schedule drives friend debts (07-08 Friend — Rohan note: "Rohan was reminded Fri 25 Sep, on the due date (Sun 27 Sep) and today"), and a per-friend toggle mutes automatic reminders (Settings › Muted friends, 12-07); a muted borrower gets no automatic loan reminders either (proposal). The borrower ("Dev … gets a reminder before each due date") is simulated; on this device schedule local notifications for the **lender** only if the user enabled reminders (proposal) and always record the reminder log so the footnote "Last reminder sent {EEE d MMM}" = the latest scheduled reminder time ≤ now. "Remind Dev" sends an extra manual nudge and records it (updates the footnote).
- Balances: an outstanding loan counts in the per-person balance and in Home's owed/owe totals (proposal). The Figma Home numbers (+₹2,900 from 4 people) exclude the Dev loan because it's created in this flow, so the Dev loan must **not** be in the base demo dataset (see §9.4).

### 8.3 Groups and projects
- Group = {id, name, kind (group|project), type (trip|home|friends|other; group only), members (you + friends/guests), currency (default = profile currency), simplifyDebts (default **true**), createdAt}. Project adds {description?, coverPhoto?, budget? (empty by default), contribution: equal|percent|fixed with per-member values}.
- **Simplify debts** on: settle-up suggestions minimise the number of transfers (net each member's balance, then greedily match the largest creditor with the largest debtor). Off: show pairwise debts as they arose.
- **Contribution** (project): Equal → each member's share of what's spent = 1/n (shown as "25%" with 4 members; rounding: show integer percent when exact, else one decimal, e.g. "33.3%", proposal). Percent → explicit percents summing to 100. Fixed → fixed amounts per member.
- Group created: balance card "Your balance ₹0" (Settled), Settle up disabled until something is owed; the Balances card and expense list appear after the first expense.
- Subtitle format: "{Type} · {n} members · {CURRENCY CODE}" ("Trip · 4 members · INR").

---

## 9. Sample data (verbatim, including hidden/clipped content)

### 9.1 recordPayment (177:29750)
- Header: "Record payment", "Save". Close glyph SF Symbol xmark "􀆄".
- Parties: From "From" / "You" (Arjun, `avatar-1`); To "To" / "Meera" (Meera, `avatar-7`).
- Amount "₹450"; currency chip "INR"; helper "You owe Meera ₹450 in Flat 302".
- Method label "Method"; chips "Cash" (selected), "UPI", "Bank", "Card", "Other".
- Hidden UPI preview: Name "Meera Iyer", UPI "meera@okhdfcbank", Copy button (Show copy), caption default "What friends see".
- Rows: "For" / "Flat 302" (groups icon); "Date" / "Wed 30 Sep" (calendar); "Proof" / "Add photo (optional)" (camera).
- Summary: "You paid Meera ₹450 in cash for Flat 302." + line break + "Meera will be asked to confirm. Paybak never moves money."

### 9.2 paymentRecorded (177:30023)
- Header "Payment", "Edit". Hero: pair Arjun → Meera, "You paid Meera", "₹450", "Cash · Today · Flat 302".
- Notice: "Pending confirmation" / "Waiting for Meera to confirm" (Activity icon).
- Details: From "You" · To "Meera" · Method "Cash" · Date "Wed 30 Sep" · For "Flat 302" · Proof "None".
- Footnote "Your balance updates once Meera confirms." Destructive row "Cancel payment".
- Toast "Payment recorded".
- Alert copy (annotation): "Cancel this payment?" · "Meera won’t be asked to confirm. You’ll still owe her ₹450." · "Keep" · "Cancel payment".

### 9.3 lendMoney (185:25810)
- Header "Lend money", "Save". Segments "I lent" (selected) / "I borrowed". Amount "₹6,000", chip "INR".
- "Lent to" / "Dev"; "Reason" / "Laptop repair"; "Date" / "Wed 30 Sep".
- "Installments" (on) / subtitle "Paid back in parts"; "Number of installments" / "3" (stepper); "Repeats" / "Monthly"; "First due" / "Fri 30 Oct".
- Preview "3 × ₹2,000 · Fri 30 Oct, Mon 30 Nov and Wed 30 Dec".

### 9.4 Loans (186:10325 / 186:26377 / 186:26608)
- **Dev loan** (created in the flow; seed it only for the debug start screens `loanAdded`/`loanOverdue`): I lent, ₹6,000, "Laptop repair", date Wed 30 Sep 2026, 3 × monthly ₹2,000 due Fri 30 Oct, Mon 30 Nov, Wed 30 Dec 2026; nothing repaid.
  - loanAdded: header "Loan"/"Edit"; "You lent Dev", "₹6,000", "Laptop repair · Today"; Original "₹6,000", Paid "₹0", Remaining "₹6,000", "0% paid back"; "3 monthly installments"; "Installment 1" "Due Fri 30 Oct" "₹2,000"; "Installment 2" "Due Mon 30 Nov" "₹2,000"; "Installment 3" "Due Wed 30 Dec" "₹2,000"; button "Record repayment"; toast "Loan added".
  - loanOverdue (clock = **Tue 3 Nov 2026**; debug hook needs a time override): meta "Laptop repair · Wed 30 Sep"; row 1 badge "Overdue 4 days"; footnote "Last reminder sent Mon 2 Nov"; buttons "Remind Dev", "Record repayment". Reminder log: Wed 28 Oct, Fri 30 Oct, Mon 2 Nov 2026.
- **Kabir loan** (belongs in the base demo dataset; Kabir's friend history 07-08): I lent, ₹4,500, "Bike service", date 12 Jun 2026, 3 × monthly ₹1,500 due 12 Jul, 12 Aug, 12 Sep 2026; repayments (confirmed) ₹1,500 on 10 Jul, ₹1,500 on 12 Aug, ₹1,500 on 14 Sep 2026. Shows: header "Loan" (no action); "You lent Kabir", "₹4,500", "Bike service · 12 Jun", chip "Paid back"; Original "₹4,500", Paid "₹4,500", Remaining "₹0", "Paid back on 14 Sep"; rows "Paid 10 Jul", "Paid 12 Aug", "Paid 14 Sep · 2 days late", each "₹1,500".

### 9.5 New group (190:8356 / 190:12242 / 190:27177)
- Header "New group", "Create". Segments "Group" / "Project". Name "Weekend Trek". Type label "Type": "Trip" (selected), "Home", "Friends", "Other".
- Members header "Members": "You" (subtitle "Arjun Mehta"), "Esha Kapoor", "Dev Malhotra", "Kabir Singh", "Add people".
- Currency "INR ₹"; "Simplify debts" (on) / "Fewer payments when settling up".
- Project: "Description" placeholder "What’s it for?"; "Add cover photo"; "Budget" placeholder "₹0", helper "Optional. Spending is tracked against it."; "Contribution": "Equal" (selected), "Percent", "Fixed"; helper "Everyone pays the same share of what’s spent."; each member "25%".
- Group created: "Weekend Trek", "Trip · 4 members · INR", plane tile, stack Arjun/Esha/Dev/Kabir; "Your balance" "₹0", "Settle up" (disabled); "No expenses yet." / "Add the first expense and Paybak will split it for everyone." / "Add expense"; toast "Group created".
- Full names used: Arjun Mehta (you), Meera Iyer, Esha Kapoor, Dev Malhotra, Kabir Singh.
- The "Weekend Trek" group is created in the flow; it's **not** part of the base demo dataset (seed it only for the `newGroupCreated` debug start screen).

### 9.6 Debug start screens
`recordPayment` (prefilled Meera ₹450 Flat 302 from the demo data), `paymentRecorded` (seed the pending ₹450 cash payment), `lendMoney` (form prefilled as Figma), `loanAdded`, `loanPaidBack` (open Kabir's loan), `loanOverdue` (Dev loan + clock override Tue 3 Nov 2026 10:00 local), `newGroup`, `newGroupProject` (draft prefilled as Figma), `newGroupCreated` (seed Weekend Trek).

---

## 10. Assets

- **Icons**: all already in `assets/icons/` (no new exports): `arrow-right`, `groups`, `chevron-right`, `calendar`, `camera`, `activity`, `delete`, `chevron-left`, `check-circle`, `profile`, `receipt`, `lend`, `split`, `repeat`, `close`, `user-add`, `exchange`, `shuffle`, `settings`, `plane`, `plus` (+ `copy` inside the hidden UPI preview's Copy button).
- **Avatars**: `assets/avatars/avatar-1` (Arjun), `avatar-4` (Esha), `avatar-5` (Dev), `avatar-6` (Kabir), `avatar-7` (Meera). No new art.
- **Illustration**: only one, on `newGroupCreated`: Figma "Illustration / Empty — First day" (7:216) = the identical component used on Home First day → **reuse `paybak-homefirstday.riv`** (artboard `First Day`, 240 × 180, no bleed). Verified visually: the 480 × 360 slot crop of `ref/newGroupCreated.png` vs `ref/homeFirstDay.png` differs by a mean of 0.002/255 (max 15/255), i.e. the same art. Fallback `assets/images/empty-first-day.svg`. Nothing exported to `assets/images/`.
- **Kit parts** (system, don't ship art): SF Symbol `xmark` (modal close), iOS Toggle and Stepper.

---

## 11. Gaps and open questions

### 11.1 Reference renders and node data
- All nine 2× references exist (`ref/<screenId>.png`, 804 × 1748). Extra 1× crops exist for `recordPayment`, `lendMoney`, `loanAdded`, `loanPaidBack`, `loanOverdue` (`ref/<screenId>_1x.png`).
- The nine frames were read from **Plugin-API node dumps, not `get_design_context` output**: the Figma MCP quota (Education plan) was exhausted, so the REST data was used instead ("get_design_context adds little on top of the node JSON"). The dumps hold every value the React reference would (and more: token names, variants, annotations, reactions). They weren't kept; `.figma-cache/nodes/77-100.json` has the same node data.

### 11.2 Not designed (proposals written above; confirm or replace)
1. "Not received" and "Confirmed" states of the payment detail (only described in the caption).
2. Edit on payment and loan detail (the "Edit" action has no target frame).
3. The Due row + quick chips of Lend money with Installments off (only described in the annotation).
4. "I borrowed" copy ("Borrowed from", "You borrowed from {name}").
5. Repeats options other than Monthly; stepper min/max; schedule preview for n > 3; month-end clamping.
6. Where "Record repayment" goes (proposed: Record payment prefilled with the next installment).
7. Type → tile icon mapping beyond Trip → plane (derived from the Title Row description's order "Plane, Home, People, Tag").
8. Percent / Fixed helper texts and validation; the Project's landing screen and toast copy after Create.
9. Summary sentence for Bank / Card / Other (Cash "in cash" and UPI "by UPI" are designed); the currency-conversion line copy.
11. `screens-settle.md` §5.2 proposes its own Cancel-payment alert copy for 08-06; the 06-12 annotation (§3.4 here) is the designed copy and should be used on both pages.
10. Whether loans count in Home's owed/owe totals (proposal: yes; the Figma Home numbers stay correct because the Dev loan isn't in the base demo data and Kabir's is fully repaid).
