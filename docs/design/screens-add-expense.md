# Paybak: Add expense (Figma page "06 Add & Record" 77:100, section "Add expense" 167:21224)

This is the whole Add expense flow: the amount-first form, the people picker, the payer sheet, the split editor (Equally · Exact · % · Shares), the category, currency and due-date sheets, and the new expense's detail after Save. The other three sections of page 06 (Record payment, Lend money, New group) are in `screens-record-lend-group.md`.

| Screen id | Figma frame (node) | Container | Spec § | 2× reference | 1× reference | Node JSON (`.figma-cache/`) |
|---|---|---|---|---|---|---|
| `addExpenseEmpty` | Add expense — Empty (176:17454), caption 06-01 | full-screen modal (from the ＋ sheet) | §4 | `ref/addExpenseEmpty.png` | `ref/addExpenseEmpty_1x.png` | `nodes/77-100.json` |
| `addExpenseFilled` | Add expense — Filled (176:18002), 06-02 | same screen, filled state | §4 | `ref/addExpenseFilled.png` | `ref/addExpenseFilled_1x.png` | `nodes/77-100.json` |
| `addExpenseSplitWith` | Split with (176:19238), 06-03 | pushed screen inside the modal | §5 | `ref/addExpenseSplitWith.png` | `ref/addExpenseSplitWith_1x.png` | `nodes/77-100.json` |
| `addExpensePaidBy` | Paid by (176:19877), 06-04 | sheet (Medium) over the form | §6 | `ref/addExpensePaidBy.png` | `ref/addExpensePaidBy_1x.png` | `nodes/77-100.json` |
| `addExpenseSplitEqually` | Split — Equally (176:21025), 06-05 | pushed screen inside the modal | §7 | `ref/addExpenseSplitEqually.png` | `ref/addExpenseSplitEqually_1x.png` | `nodes/77-100.json` |
| `addExpenseSplitExactError` | Split — Exact (error) (177:21514), 06-06 | same screen, Exact mode, error state | §7 | `ref/addExpenseSplitExactError.png` | `ref/addExpenseSplitExactError_1x.png` | `nodes/77-100.json` |
| `addExpenseCategory` | Category (177:21884), 06-07 | sheet (Large) over the form | §8 | `ref/addExpenseCategory.png` | `ref/addExpenseCategory_1x.png` | `nodes/77-100.json` |
| `addExpenseCurrency` | Currency (177:22329), 06-08 | sheet (Large) over the form | §9 | `ref/addExpenseCurrency.png` | `ref/addExpenseCurrency_1x.png` | `nodes/77-100.json` |
| `addExpenseDueDate` | Due date (177:23763), 06-09 | sheet (Medium) over the form | §10 | `ref/addExpenseDueDate.png` | `ref/addExpenseDueDate_1x.png` | `nodes/77-100.json` |
| `expenseAdded` | Expense added (177:24360), 06-10 | pushed detail (09-03 Expense detail template) + toast | §11 | `ref/expenseAdded.png` | `ref/expenseAdded_1x.png` | `nodes/77-100.json` |

- There are **no "Overlay helpers" / "↳ … (overlay)" frames** in this section. The four sheet frames (Paid by, Category, Currency, Due date) are whole 402 × 874 frames: the form in the background, the 40 % scrim and the sheet. The prototype opens them with `OPEN OVERLAY`.
- The **Date** sheet (opened by the form's "Today" chip), the **Multiple people** payer editor, the **Percent** and **Shares** split modes and the Repeat/Group/Notes pickers aren't drawn as frames in this section. §6.3, §7.3, §10.3 and §3 spell them out from the component variants and the designer notes. Repeat (Pro) is drawn on page 11 (`screens-insights-ai.md` §5.3), and the paywall on page 12 (`screens-settings.md` §2).
- Business rules (amounts, validation, split maths with the fair rotation of leftover paise, due dates and reminders, currency and rate, balances after Save) are in **§3**. Sample data is in **§12**, test IDs and debug start screens are in **§13**, and assets are in **§14**. Open questions and proposals are in **§15**.

**How this was read.** The Figma MCP allowance ran out while this was written (see §15.1). The data comes from:
- `get_design_context` for Empty, Filled and Split with;
- one read-only `use_figma` script that listed every prototype reaction, scroll setting and component definition in the ten frames;
- the REST export of the whole page (now `.figma-cache/nodes/77-100.json` and `.figma-cache/nodes/3-3-b.json`, regenerate with `tools/fetch_figma.py`), turned into a readable node tree with token names for the ten frames and for every variant of Row / Split Person and Card / Split Total.

These intermediate dumps weren't kept. Bound variables were resolved to token names by matching the REST variable ids with the design-context output and the token order in `tokens.md`. The 2× references are the REST renders (`.figma-cache/renders/<id>.png`), copied to `ref/`. The 1× references were cropped from one section screenshot.

---

## 0. Conventions

- Frame = 402 × 874 pt (iPhone 17 Pro). All `x, y` are **frame coordinates** (0,0 = the frame's top-left) unless a line says "row-relative" or "sheet-relative". pt (iOS) = dp (Android). Top safe area 62, bottom 34 (bottom safe edge y 840).
- The status bar (kit, 402×62 at y 0), the home indicator (kit, 402×34 at y 840) and the **kit keyboard** (402×308 at y 566) are **system UI: don't draw them**. Use the real system decimal pad (§3.1).
- **Hotspot frames** (`✕ hotspot`, `Save hotspot`, `INR hotspot`, `Today hotspot`, `Back hotspot`, `Done hotspot`, `<row> hotspot`) are **invisible prototype helpers**. Don't draw them; their reactions are listed under "Navigation".
- **"Scroll edge (top)"** (a white `bg/primary` rectangle 402 × 106 at (0, 0)) and **"Header space"** (a 44-tall empty frame at y 62 in the column) mean: the header is fixed, the content scrolls under a solid white band behind the status bar and header, and the content starts at y 122 at rest.
- Colours are `tokens.md` names (the `color/` prefix dropped) with hex. Text styles are `tokens.md` names (Manrope). "kit" = an Apple iOS 27 UI-kit instance (system component).
- Text is verbatim. Keep `₹` U+20B9, `·` U+00B7, `’` U+2019 (e.g. "it’s", "aren’t"), `“ ”` U+201C/U+201D, `−` U+2212, `–` U+2013 ("6–8 Mar").
- Icons: `assets/icons/<name>.svg` (24-grid stroke icons; scale the whole SVG: 24 → stroke 1.5, 20 → 1.25, 16 → 1.0). All icons used here already exist (§14).
- Avatars: `assets/avatars/avatar-N.svg` (Arjun 1, Priya 2, Rohan 3, Esha 4, Dev 5, Kabir 6, Meera 7). Circle fill: `bg/card` #F5F5F5 on white, `bg/primary` #FFFFFF inside #F5F5F5 cards and chips.
- Pressed states: README §3 rule 11 (pills change fill only, text buttons go to 50 % opacity, glass icon buttons use `bg/card`). Rows without a pressed variant: `bg/card-pressed` #EBEBEB clipped to the card shape (suggestion). Row / Split Person has a designed pressed look (§2.2).
- Test IDs follow flow.md (`<screen>.<element>`), listed in §13.

---

## 1. Designer notes (verbatim)

### 1.1 Section notes (Add expense, 167:21224)
- Title (167:21225): "Add expense"
- Subtitle (167:21226): "Full-screen modal from the ＋ sheet: amount first on the decimal pad, then people, title and form rows. Pickers open as sheets, the split editor pushes, and Save lands on the new expense with a toast."

### 1.2 Captions under each frame
- **06-01**, under Add expense — Empty (176:17769): "Amount first: the decimal pad opens with the cursor in ₹0, so a bill takes seconds. Save stays disabled until there is an amount and at least one other person, and the date defaults to Today."
- **06-02**, under Add expense — Filled (176:18356): "The Olive Garden bill ready to save: ₹2,800 split equally between 4 people (₹700 each), and “This weekend” sets the due date to Sun 4 Oct. Repeat turns the expense into a recurring rule; it’s a Pro feature and Arjun is on the free plan, so the row carries a black Pro badge and opens the paywall."
- **06-03**, under Split with (176:19716): "Pick who shares the bill. The people you pick show as chips here and on the form. Guests who aren’t on Paybak yet can be included, and they’re marked Guest."
- **06-04**, under Paid by (176:20149): "Only people on this expense are listed, and picking one closes the sheet. Multiple people pushes a Paid by editor that reuses the split editor’s Exact rows and Card / Split Total."
- **06-05**, under Split — Equally (176:21202): "An equal split of ₹2,800 between 4 people. The footer updates live and stays gray at ₹0 left. When an amount doesn’t divide evenly, the leftover paisa rotate fairly."
- **06-06**, under Split — Exact (error) (177:21800): "State: exact amounts that don’t add up. Dev is mid-edit at ₹550, so the footer turns red with “₹150 left”, and Done stays disabled until the total is ₹2,800."
- **06-07**, under Category (177:22164): "A searchable category list, with Food picked for Olive Garden. The same icons lead expense rows across the app (black line in a #F5F5F5 circle) and label the Insights categories on page 11."
- **06-08**, under Currency (177:22510): "Reuses the Setup currency rows; AED is recent from Dubai Weekend (6–8 Mar). Picking a non-default currency adds a rate line to the form, and that rate is saved with the expense."
- **06-09**, under Due date (177:24082): "The quick chips (Tomorrow · This weekend · Next week) cover most cases, and Pick date opens this calendar; the hint mirrors the default reminder schedule in Settings. The form’s “Today” chip opens the same sheet, titled “Date” with a “Set date” button and no hint, so you can back-date an expense."
- **06-10**, under Expense added (177:24769): "After Save, the modal closes onto the new expense with a short toast; the screen uses the 09-03 Expense detail template. Your share is ₹700, so you’re owed ₹2,100 (₹2,800 − ₹700); the share card keeps the template’s rows with no “You’re owed” row, and the group chip and group balance row are hidden because the dinner isn’t in a group."

### 1.3 Dev-mode annotations on nodes (verbatim)
- Keyboard (kit Number Pad) 176:17715 on Empty: "Kit Number Pad stands in for .keyboardType(.decimalPad): the iOS 27 kit has no Decimal Pad. It shows while the amount is focused; tapping a row dismisses it."
- People 176:18005 on Filled: "Scrolls horizontally; the Add chip (→ 06-03) follows Dev."
- Toast 177:24747 on Expense added: "Toast: fixed position, auto-dismisses after 2 s. No link."

### 1.4 Notes on other pages that define Add expense behaviour (verbatim)
- Scan receipt — Camera (page 11, 177:25568): "Opens from “Add receipt” in Add expense. Free users can still take or upload the photo and attach it. Reading the items (Check receipt, Assign items) is Pro and opens the paywall. Shown as a Pro member."
- Scan receipt — Add expense (page 11, 177:28338): "The scan ends in the normal Add expense form, filled in, so saving works like any other expense and opens its detail with “Expense added”. Changing the split goes back to Assign items. Shown as a Pro member."
- Recurring — Repeat (page 11, 177:30291): "For Pro members, the Repeat row in Add expense opens this sheet; free users get the paywall from that row. Turning on “Amount changes each time” makes the rule create drafts instead of expenses. Shown as a Pro member."
- Paybak Pro — Paywall (page 12, 167:13003): "Paywall, opened from the Profile “Paybak Pro” row or any locked feature (Export records, the Add expense Repeat row, Insights); Arjun is on the free plan and the core ledger stays free. …"
- Expense — Villa (page 09, 167:17847): "… Edit opens Add expense in edit mode, prefilled (not linked in the prototype); …"
- Ask Paybak — Confirm (page 11, 167:15071): "… Edit opens the full Add expense form, prefilled. …"

---

## 2. Components (reuse map and NEW components)

### 2.1 Reuse map (every component in these frames)
| Figma component | Where it's specced | Used here as |
|---|---|---|
| Navigation / Modal Header (115:886), Action=Disabled / Enabled | `screens-record-lend-group.md` §1.2.1 | form header: kit glass ✕ left, "Add expense", "Save" pill |
| Control / Amount Display (125:1084), State=Empty / Filled | `screens-record-lend-group.md` §1.2.3 (+ §2.3 here: Show date chip = **true**) | amount + "INR" and "Today" chips |
| Control / Category Chip (64:4185), Leading=None / Icon / Avatar, Selected, Show remove | `screens-record-lend-group.md` §1.2.5 (+ the Avatar and remove variants below) | currency/date chips, people chips, "Add people"/"Add", due quick chips, selected-people chips |
| Control / Input Field (12:296), Default / Filled, leading Search icon | `components-core.md` §4.4 | title field; search fields in Split with and the Category and Currency sheets |
| Row / Setting (97:996), Trailing=Chevron / Check / Unchecked / None, Tone=Default / Destructive, Show badge | `screens-record-lend-group.md` §1.2.6 | form rows, category rows, detail share rows, Delete expense |
| Divider / Line (12:302), Inset=Leading / None | `components-core.md` §4.5 | under the due chips; in rows |
| Badge / Pill (11:46), Inverse "Pro", On Card "Guest", Muted "Food" | `components-core.md` §3.1 | Repeat row; guest tag; category chip in the detail hero |
| Avatar / Circle (11:136), 24 / 32 / 40 / 56, Art / Initials / Icon | `components-core.md` §3.2 | chips, person rows, split rows, hero icon |
| Navigation / Push Header (97:1082), Trailing=Text | `screens-record-lend-group.md` §1.2.2 | "Split with" · "Done"; "Split" · "Done"; "Expense" · "Edit" |
| Row / Person (127:2252), Regular (Select On / Select Off / Check / None), Compact (Value) | `screens-record-lend-group.md` §1.2.10, `screens-settle.md` §0.4-C (full description in §2.4 here) | Split with list, Paid by sheet, detail split card |
| Sheet / Action Row (17:641) | `components-home.md` §13 | "Add a new friend" (no subtitle), "Multiple people" |
| Row / Section Header (13:223), Show action=false | `components-home.md` §8 | "Friends", "Recent", "All currencies", detail sections |
| Control / Segmented (12:249) Options=4 + Control / Segment (12:236) | `components-core.md` §4.2–4.3 | split mode: Equally · Exact · % · Shares (362 × 36, segments 89 × 30) |
| Sheet / Container (118:1017), Detent=Medium / Large, Show search | `components-home.md` §15 (the Figma description has since changed, see §2.5) | Paid by, Due date (Medium); Category, Currency (Large + search) |
| Row / Currency (37:673) | `components-core.md` §5.5 | currency rows, stretched to 370 |
| Button / Primary (9:36) Large + Small; Button / Text (10:45) Primary | `components-core.md` §2.1–2.2 | "Set due date", the "Save" pill, "No due date" |
| Header / Amount Hero (128:2004) Leading=Icon | `screens-record-lend-group.md` §1.2.7, `screens-activity.md` §4.3-A | detail hero |
| Control / Composer (115:907), Row / History (116:1058), Overlay / Toast (118:965) | `screens-activity.md` §4.3/§9, `screens-insights-ai.md` §1.2, `screens-record-lend-group.md` §1.2.13 | detail comments, history, "Expense added" toast |
| **Row / Split Person (126:1596)** | **NEW, §2.2** | split editor rows (and the Multiple people payer editor) |
| **Card / Split Total (125:1170)** | **NEW, §2.3** | live split footer |
| kit: Button - Liquid Glass - Symbol (xmark), Grabber, Keyboard (Number Pad), Date and time - Pickers (Inline), Stepper, Status bar, Home indicator | system | ✕ buttons, sheet grabber, decimal pad, calendar, shares stepper |

### 2.2 Row / Split Person (126:1596). SwiftUI `PBSplitRow` (NEW)
> "PBSplitRow — one person in the split editor (06-05, 06-06; 10-04 Percent/Fixed editing; the Multiple people payer editor uses Exact). Row 64, inside a #F5F5F5 card: select circle (24, black with a white tick; an empty border/strong ring when Excluded), exposed Avatar / Circle 32 (white circle), Name, trailing value, inset divider.
> Mode=Equally: the Amount (Headline). Exact: an inline 96-wide color/bg/primary r14 field with the Amount. Percent: the field shows Percent; Amount is the Footnote under the name. Shares: a 48 field with Shares + the kit Stepper; Amount under the name.
> State=Focused: the field gets a 1.5 border/strong ring and the caret (Equally has no field, so Focused shows the pressed row, color/bg/card-pressed). State=Excluded: unticked, name and ₹0 / 0% / 0 in text/tertiary (fixed values, not bound to the props).
> SwiftUI: PBSplitRow"

Properties: `Name` ("Priya"), `Amount` ("₹700"), `Percent` ("25%"), `Shares` ("1"), `Show divider` (true), `Mode` = Equally | Exact | Percent | Shares, `State` = Default | Focused | Excluded.

Geometry (all 12 variants, row-relative): **362 × 64** (min height 64), H auto-layout, padding **8 top/bottom, 16 left/right** (`layout/card-padding`), gap **12**, items centred vertically, no fill of its own (the screen draws the `bg/card` radius-20 group; rows stack with 0 gap).
1. `select` **24 × 24** circle at (16, 20):
   - ticked (Default, Focused): fill `bg/inverse` #0A0A0A + `check.svg` **16 × 16** at (4, 4) tinted `icon/inverse` #FFFFFF;
   - Excluded: no fill, **1.5 pt inside ring `border/strong` #0A0A0A**.
   - The select circle is the toggle: tap = include/exclude this person (min 44 × 44 hit area).
2. `avatar` Avatar / Circle **32** Type=Art, fill **`bg/primary`** (white, on the card), at (52, 16).
3. `text` column (starts at x 96), V gap 0:
   - `name` **Headline** `text/primary` (Excluded `text/tertiary`), 1 line, truncate end;
   - Percent and Shares only: `amount` **Footnote** `text/secondary` (Excluded `text/tertiary`, fixed "₹0") under the name. The column is then 40 tall (22 + 18).
   - Column width: Equally 199 (fills), Exact/Percent 142, Shares 90.
4. Trailing, by Mode:
   - **Equally**: `amount` **Headline** `text/primary`, hug, right-aligned to x 346 (frame x 366). Excluded: "₹0" `text/tertiary`.
   - **Exact**: `trailing` (hug) → `field` **96 × 36** at (250, 14), fill `bg/primary` #FFFFFF, radius **14** (`radius/input`), H, padding 0/12, gap 2, content **right-aligned**; `value` **Headline** `text/primary` ("₹700"). Excluded: "₹0" `text/tertiary`.
   - **Percent**: same 96 × 36 field with the Percent ("25%"); Excluded "0%" `text/tertiary`.
   - **Shares**: `trailing` 148 wide, H gap 8: `field` **48 × 36** (content **centred**, value "1") + kit **Stepper** 92 × 32 (radius 100, two 46-wide halves with `minus`/`plus`; spec in `screens-record-lend-group.md` §1.2.6 "Trailing Stepper"). Excluded: value "0" `text/tertiary`, stepper at **40 % opacity** (disabled).
5. State **Focused**:
   - Exact/Percent/Shares: the field gets a **1.5 pt inside ring `border/strong`** and a **caret** 2 × 20 `text/primary` right after the value (gap 2). The system decimal pad is up (§7.4).
   - Equally: the whole row fill becomes **`bg/card-pressed`** #EBEBEB (the pressed look).
6. `divider` (absolute, row bottom, Show divider): Divider / Line Inset=None, **from row x 96 to the right edge** (266 wide), `border/subtle` #EBEBEB. Hidden on the last row.

### 2.3 Card / Split Total (125:1170). SwiftUI `PBSplitTotalBar` (NEW)
> "PBSplitTotalBar — the live footer of the split editor (06-05, 06-06) and the Multiple people payer editor: a #F5F5F5 r20 bar, 56 tall. Left (Amount/Medium) + Detail (Subheadline, right-aligned).
> Balanced: “₹0 left” stays gray. Error: Icon / Alert + Left in text/destructive (Bold), e.g. Left “₹150 left”, Detail “₹2,650 of ₹2,800”; Done stays disabled. Text props share one default, so set Left/Detail per instance.
> SwiftUI: PBSplitTotalBar"

Properties: `Left` ("₹0 left"), `Detail` ("₹2,800 of ₹2,800"), `State` = Balanced | Error.
- **362 × 56**, H auto-layout, padding **0 top/bottom, 16 left/right** (`layout/card-padding`), gap **8**, items centred, fill `bg/card` #F5F5F5, radius **20** (`radius/card`). No stroke, no shadow.
- **Balanced**: `left` **Amount/Medium** (Bold 17/22, −0.5 %) **`text/secondary`** #6B6B6B, hug, at (16, 17) · `detail` **Subheadline** `text/secondary`, fills, **right-aligned**, at (78, 18).
- **Error**: `icon` `alert.svg` **20 × 20** tinted **`icon/destructive`** #C93636 at (16, 18) · `left` Amount/Medium **`text/destructive`** #C93636 · `detail` Subheadline `text/secondary` (not red), right-aligned.
- It's red **only** while the split doesn't add up (foundations: red is for errors/overdue).
- Placement on the split editor: absolute, **bottom = the bottom safe-area edge (y 784–840)** when no keyboard is up; **8 pt above the keyboard** when a field is focused (y 502–558 with the 308-tall kit keyboard at y 566). iOS: `.safeAreaInset(edge: .bottom)` with 0 spacing (it rides the keyboard). Android: `Modifier.imePadding()` on the bottom bar + 8 dp.

### 2.4 Row / Person (127:2252): the variants used here
Full description (verbatim):
> "PBPersonRow — the one people row (split pickers, balances, breakdowns, contacts, expense splits, chat answers). Exposed Avatar / Circle (40 Regular / 32 Compact), Name (Headline) + Show tag (exposed Badge / Pill Muted “Guest”, always next to the name), Subtitle (Subheadline), trailing, Show divider (inset to the name).
> Size=Regular: 64, avatar on color/bg/card for white surfaces (inside a #F5F5F5 card, override the avatar fill to color/bg/primary). Size=Compact: 56, avatar 32 in a white circle and an On Card tag, for use inside a card.
> Trailing: Owed = “+” + Amount (Amount/Medium, text/primary) · Owe = “−” + Amount (Amount/Medium, text/secondary) · Value = Amount (Headline, e.g. ₹700 or 25%) — these three carry Amount label (Footnote, tertiary) and Show status badge (exposed Badge / Pill Overdue “Overdue 3 days”, always under the amount) · Muted = Status (Subheadline, secondary: Settled / Added / No balance) · Check = black tick (single select, e.g. Paid by) · Select On / Select Off = 24 black circle with a white tick / empty border/strong ring (multi-select) · Remove = Icon / Close 20 (icon/secondary) · Button = exposed Button / Secondary Small “Invite” (Regular) or Button / On Card Small “Remind” (Compact) · None.
> Amount excludes the sign: the variant adds “+” or “−”. Guests: Avatar Type=Initials “AR” + Show tag.
> SwiftUI: PBPersonRow"

**Size=Regular** (Split with and Paid by): 64 min height (row-relative), H, padding 8/16, gap 12, centred. `avatar` 40 at (16, 12) (fill `bg/primary` inside a card, `bg/card` on the white sheet). `text` at x 68 (V gap 2): `name line` (H gap 8: `name` **Headline** `text/primary`, **max width 200**, 1 line + optional `tag`), `subtitle` **Subheadline** `text/secondary` 1 line (Show subtitle). Trailing:
- **Select On**: 24 × 24 circle `bg/inverse` with `check.svg` 16 `icon/inverse` at (4, 4); **Select Off**: 24 × 24, 1.5 pt inside ring `border/strong`. Right edge at row x 346.
- **Check**: `check.svg` **24 × 24** tinted `icon/primary` (a plain black tick, no circle), right edge at row x 338 (the row is 354 wide in the sheet).
- **None**: nothing.
- `tag` (Show tag): Badge / Pill **On Card** style in the Figma instance (white `bg/primary` pill, Caption/1 `text/secondary`) "Guest": the description says Muted on white surfaces and On Card inside a card; the Split with list is a card, so **On Card**.
- `divider` (absolute, row bottom): Divider / Line Inset=None from **row x 68** to the right edge.

**Size=Compact, Trailing=Value** (detail split card): 56 min, padding **6**/16, gap 12. Avatar **32** white at (16, 12). Text at x 60 (name max width 180). Trailing `value` **Headline** `text/primary`, right-aligned to row x 346. Divider from row x 60.

### 2.5 Sheet / Container (118:1017): current description (verbatim, supersedes `components-home.md` §15's copy)
> "PBSheet (.presentationDetents) — Container for every picker and form sheet: kit grabber, a Title/3 title on the left and the kit glass xmark on the right, an optional search field (Show search; nested Control / Input Field exposed as “search”: set its Value#12:9 to “Search categories” etc.) and the native Content slot.
> Detent=Medium: 386 wide (inset 8, anchored 8 from the bottom), radius 40, hugs its content; the slot is 354 wide. Detent=Large: 402×804 from y 70, top radius 40, the slot fills the height and clips (lists scroll under the bottom edge); the slot is 370 wide.
> Content is a native SLOT: append the sheet body to the “Content” slot of the instance (or a local _Sheet / frame name instance). Never detach. Booleans: Show title, Show close, Show grabber, Show search, Show header. The title starts at 16, level with the slot content. For a sheet with no title and no close (08-10), turn Show header off (not just the two inner booleans), so the content starts 20 below the top.
> SwiftUI: PBSheet"

Measured on these frames (sheet-relative):
| | Medium (Paid by, Due date) | Large (Category, Currency) |
|---|---|---|
| Frame | x 8, width 386, bottom at y 866 (8 above the screen bottom), height hugs | (0, 70) 402 × 804 |
| Corners / fill | radius 40 all corners, `bg/primary` | radius 40 top-left/top-right, 0 bottom, `bg/primary`, clips |
| Padding / gap | 8 top, 16 sides, **28** bottom · gap 8 | 8 top, 16 sides, **34** bottom (`layout/home-indicator`) · gap 8 |
| Grabber (kit) | 60 × 4 at (163, 8), #CCCCCC, radius 100 | 60 × 4 at (171, 8) |
| Header | (16, 20) 354 × 50, H, end-aligned: `title` **Title/3** `text/primary` (fills, 1 line) at (16, 32); kit glass **close 50 × 50** at (320, 20) | (16, 20) 370 × 50; title at (16, 32) 320 wide; close at (336, 20) |
| Search (Show search) | – | Control / Input Field Default, leading `search.svg` 20 `icon/secondary`, (16, 78) 370 × 52 |
| Content slot | (16, 78) 354 wide, hugs | (16, 138) 370 wide, fills to the bottom padding (632 tall), clips; the list scrolls inside |

Scrim: full-screen `bg/scrim` #0A0A0A @ 40 %. Scrim tap, the ✕ and a row pick all close the sheet (Figma `CLOSE`). Figma opens these overlays with DISSOLVE 250 ms ease-out. On device, use the native sheet: iOS `.sheet` + `.presentationDetents([.medium])` or `[.large]` (iOS 26+ draws the floating inset Medium sheet and the glass ✕ itself; `.presentationDragIndicator(.visible)`). Android: the app's shared `PBSheet` (`components-home.md` §15: custom overlay with scrim, slide-up 300 ms ease-out, swipe-down to dismiss; ✕ = `close.svg` 38 dp tinted #1A1A1A in a white 50 dp circle).

---

## 3. Business rules (code these in the on-device data layer; the numbers on screen must come out of them)

"Now" for the demo is **Wed 30 Sep 2026** (flow.md). The user is **Arjun Mehta** ("You"), free plan, default currency **INR**.

### 3.1 Amount entry (amount first, decimal pad)
- The form opens with the **amount focused** and the **system decimal pad** up: iOS `TextField(...).keyboardType(.decimalPad)` + `@FocusState` set on appear; Android `KeyboardOptions(keyboardType = KeyboardType.Decimal)` + `FocusRequester.requestFocus()` in a `LaunchedEffect`. Never a custom keypad (Amount Display description). Figma draws the kit Number Pad only because "the iOS 27 kit has no Decimal Pad" (§1.3).
- **Display** (Control / Amount Display, `Amount/Display` ExtraBold 56/64, centred):
  - empty: the placeholder **"₹0"** in `text/tertiary` #A3A3A3, followed by the **caret** (2 × 56, `text/primary`, gap 2);
  - typing: the formatted amount in `text/primary` + caret (State=Focused);
  - not focused: the amount in `text/primary`, no caret (State=Filled).
- Store money as **integer minor units** (paise for INR). Format for display with the currency's symbol and grouping. For INR: "₹" + **Indian grouping** (₹2,800 · ₹12,500 · ₹1,00,000). Show no decimals when the amount is whole. While typing, echo what's typed (`₹2,800.5` stays as typed until blur, then `₹2,800.50`). Other currencies: see §3.9.
- Input rules: digits and one decimal separator (accept both "." and the locale's ","); **max 2 decimals** (the currency's minor unit; 0 for JPY); strip leading zeros; cap at ₹99,99,99,999 (proposal). If a long amount doesn't fit the 362 width, shrink the font (min scale 0.5); never wrap.
- **Focus rules** (annotation): tapping the amount (or its caret area) focuses it. Tapping **any form row dismisses the keyboard** (and opens that row's picker). Tapping the title field moves focus there (text keyboard). The keyboard's own dismiss also blurs.

### 3.2 Save rule (Modal Header action)
- **Save is enabled only when the amount is > 0 AND at least one other person (not you) is on the expense** (06-01), **and** the split is valid (§3.5: Exact/Percent add up). Otherwise the pill shows `Action=Disabled` (`bg/disabled` #E0E0E0 + `text/disabled` #A3A3A3) and isn't tappable.
- The date always has a value (defaults to **Today**). Category, title, group, due date, repeat, receipt and notes are optional.
- Empty title on Save (proposal, not in Figma): use the category name ("Food"), or "Expense" when there's no category.
- Default category when none is picked (proposal): **Other**. The expense row icon is then `tag.svg`.

### 3.3 People ("With you and …")
- The people on the expense are **you + everyone picked in Split with** (§5). The form lists the others as chips after the label "With you and".
- Picking/removing people updates the split: new people join the current split (Equally recomputes; for Exact/Percent they join with ₹0 / 0 %, so the split must be fixed before Save; for Shares they join with 1 share).
- If the payer is removed from the people, the payer resets to **You** (proposal).
- Guests (friends not on Paybak, e.g. Ananya Rao) can be included. Their avatar is initials, they carry a "Guest" tag in lists, and balances are kept locally like anyone else's (flow.md: no backend).

### 3.4 Paid by
- Default payer: **You**. The Paid by sheet lists **only people on this expense**: you first (name "You", subtitle your full name), then the others in the order they were added, each with their full name. Picking one sets the single payer and closes the sheet (06-04). The form row value = "You" or the person's first name.
- **Multiple people** pushes the payer editor (§6.3): each payer's paid amount, which must add up to the total. The form row value is then "{n} people" (proposal; e.g. "2 people"). Each person's net for the expense = paid − share.

### 3.5 Split modes and maths (split editor, §7)
Everyone on the expense has a row. A row can be **excluded** (unticked): an excluded person has share 0 but stays on the expense (proposal: excluding makes sense for "they were there but didn't eat"; to remove someone entirely, use Split with). At least one row must stay ticked (proposal: the last ticked row can't be unticked).

Let T = total in minor units, P = the ticked people (n = |P|).
- **Equally**: every ticked person gets ⌊T / n⌋. The leftover r = T − n·⌊T / n⌋ (0 ≤ r < n) minor units are given **one each to r people, rotating fairly** (below). Example: ₹2,800 / 4 = ₹700 each, r = 0. ₹1,000 / 3 = ₹333.33 × 3 + 1 paisa left over → one person gets ₹333.34.
- **Exact**: each ticked person has an amount field. **Σ must equal T.** Remaining = T − Σ. Excluded rows are fixed at ₹0.
- **Percent (%)**: each ticked person has a percent field (0–100, up to 2 decimals). **Σ must equal 100 %.** Shares in money = T × pct / 100, rounded to minor units with the largest-remainder method; ties are broken by the fair rotation. Default when switching to % from Equally: 100 / n each (25 % for 4). The Footnote under the name shows the money value ("₹700").
- **Shares**: each ticked person has a whole number of shares ≥ 1 (the stepper; the field also accepts typing, 1–99). Money = T × sᵢ / Σs, rounded by largest remainder + fair rotation. Always adds up, so it never shows an error. Default: 1 share each. Excluded = 0 shares, stepper disabled.
- **Switching modes** keeps the current money split as the starting point: Equally → Exact prefills each field with the equal amount (the 06-06 frame starts from ₹700 each); Exact → % converts amounts to percentages; any mode → Equally recomputes (proposal).
- **Fair rotation of leftover paise** (06-05: "the leftover paisa rotate fairly"). Give the r extra minor units to r different people, and over time to everyone equally:
  1. Order the ticked people by a stable key (the member's creation order, or the user id).
  2. Keep a rotation counter per "context": the group id, or the sorted set of participant ids for expenses outside a group. Store it with the data.
  3. For each new split with a leftover r > 0, start at index `counter mod n`, give +1 minor unit to the next r people in order (wrapping), then do `counter += r`.
  4. Save the resulting exact shares on the expense, so editing unrelated fields never reshuffles them. Recompute only when the amount, people or split changes.
  Example with the counter at 0: ₹1,000 among Arjun, Priya, Esha → Arjun ₹333.34, Priya ₹333.33, Esha ₹333.33; the next ₹1,000 split in the same context gives Priya the extra paisa.
- **Validation / footer (Card / Split Total)**:
  - Exact: Left = "{remaining} left" when remaining > 0, "{−remaining} over" when Σ > T (proposal, not in Figma), "₹0 left" when equal. Detail = "{Σ} of {T}" ("₹2,650 of ₹2,800").
  - Percent: Left = "{100 − Σ}% left" / "{Σ − 100}% over"; Detail = "{Σ}% of 100%" (proposal).
  - Equally and Shares: always "₹0 left", "{T} of {T}".
  - State=Balanced (gray) iff remaining = 0; otherwise **State=Error** (red icon and red Left) and **Done is disabled** (06-06). The footer **updates live on every keystroke** (06-05).
- **Form Split row value**: Equally with no leftover → "Equally · ₹700 each"; Equally with a leftover → "Equally · 3 people"; Exact → "Exact · 4 people"; Percent → "Percent · 4 people"; Shares → "Shares · 4 people"; from Scan receipt → "Itemized · 3 people" (`screens-insights-ai.md` §4.5). Excluded people aren't counted. Only the "Equally · ₹700 each" form is in Figma; the others are proposals following the "Equally · 3 people" pattern used on page 11.
- If the **amount changes after an Exact split** was set and no longer adds up, the Split row value shows **"Doesn't add up"** in `text/destructive`, and Save is disabled until it's fixed (proposal). Percent and Shares rescale automatically; Equally recomputes.

### 3.6 Date (when it happened) and the Date sheet
- Defaults to **Today** (06-01). The "Today" chip shows "Today", "Yesterday", or `EEE d MMM` ("Mon 28 Sep") for other dates.
- The chip opens the **Date** sheet: the Due date sheet titled "Date", with a "Set date" button and no hint (06-09); §10.3. Future dates are disabled (proposal: "back-date" only).

### 3.7 Due date (when others should pay you back)
- Default **None**. The row value shows "None" or the due date as `EEE d MMM` ("Sun 4 Oct").
- **Quick chips** under the Due row (single choice, the selected one is `Selected=True`, black):
  - **Tomorrow** = today + 1 day (Thu 1 Oct);
  - **This weekend** = the **coming Sunday** (Sun 4 Oct from Wed 30 Sep; on a Saturday → tomorrow; on a Sunday → today);
  - **Next week** = today + 7 days (Wed 7 Oct) (proposal: Figma doesn't define it);
  - **Pick date** opens the Due date sheet (§10). After a date is picked there, no quick chip is selected unless the date equals one of them.
  - Tapping the selected chip again clears the due date (proposal). The Due row itself (chevron) also opens the Due date sheet.
- The due date applies to **what the others owe the payer** (your share has nothing to pay back when you paid). It drives the "Due soon" and "Overdue" states on Home and in the lists.
- **Reminders**: the sheet's hint mirrors the default reminder schedule in **Settings** (`screens-settings.md`): 2 days before, on the day, then every 3 days while overdue. Build the hint from the user's current Settings values; the Figma copy is the default: "Paybak reminds them 2 days before, on the day, and every 3 days if it’s overdue." On Save, create the reminder schedule for each person who owes (the other person's side is simulated, flow.md). Local notifications are only for debts **you** owe (`screens-activity.md` §7).

### 3.8 Category
- The fixed list (in order): **Food** (`food.svg`), **Travel** (`car.svg`), **Stays** (`bed.svg`), **Fun** (`ticket.svg`), **Rent** (`home.svg`), **Bills** (`bolt.svg`), **Shopping** (`shopping-bag.svg`), **Other** (`tag.svg`). Search filters by name (case- and accent-insensitive, contains).
- The form's Category row shows the **chosen category's icon** as its leading icon and the name as its value. With nothing chosen: `tag.svg` + "Choose" (06-01 → 06-02 swaps Tag → Food).
- The same icon leads the expense everywhere: a black line icon in a #F5F5F5 circle on white (Row / Activity Type=Expense), a white circle inside cards, and the Insights categories (06-07).

### 3.9 Currency and rate
- Default = the profile currency (**INR**). In a group, the default is the group's currency (e.g. AED for Dubai Weekend, `screens-groups.md` §2.3).
- The chip label is the ISO code ("INR", "AED").
- **Recent** = currencies used on your recent expenses and groups, newest first, with the default currency on top. Demo: INR, then **AED** (recent from Dubai Weekend, 6–8 Mar). **All currencies** = the full ISO 4217 list sorted by English name, using the **sentence-case names** shown here ("Australian dollar", "British pound", …; note these differ in case from Setup 2's "Indian Rupee"; use the sentence-case names in this sheet) and the symbol tiles from Setup (§9.3).
- Picking a **non-default currency adds a rate line** to the form, and **that rate is saved with the expense** (06-08). The line isn't drawn in Figma. Proposal: show it as the Amount Display helper (`Show helper = true`, Footnote `text/secondary`, centred under the amount, 8 below it: the block grows from 120 to 138), with the text `≈ ₹{amount × rate} · ₹{rate} per {CODE}`, e.g. "≈ ₹27,420 · ₹22.85 per AED", rounded to whole rupees. The format matches the Dubai Weekend detail lines. Tapping the helper lets you edit the rate (proposal: a small sheet with a decimal field "₹ per AED").
- Rates: an on-device table (bundled, "today's rate"; no network needed). Save the rate on the expense and never recompute it, because balances must not shift with the market.
- Amount formatting for non-INR: `{CODE} {amount}` with grouping ("AED 1,200") in lists (Groups spec). In the big Amount Display, use the currency symbol when it's 1–2 characters (€1,200, $1,200, £1,200, ¥1,200), otherwise the code + a space ("AED 1,200") (proposal).

### 3.10 Group
- Default **"No group"**. The row opens a group picker sheet (not designed; proposal): Sheet / Container Medium titled "Group", with Row / Setting rows (Trailing=Check / Unchecked, `groups.svg` or the group's type icon): "No group" first, then your active groups (Goa Trip, Flat 302, Dubai Weekend, …). Picking a group sets its currency and offers its members. Proposal: add all members to the people if none are picked yet. Opening Add expense from inside a group presets the group and its members.
- The expense then counts in that group's balances (and simplify-debts); "No group" expenses are direct between you and the friends involved.

### 3.11 Repeat (Pro)
- **Free plan** (Arjun in Figma): the row shows a black **"Pro"** badge (Badge / Pill Inverse) and value "Never". Tapping it opens the **Paywall** (page 12, `screens-settings.md` §2; Figma link to 167:13003).
- **Pro**: hide the badge (proposal). The row opens the **Repeat** sheet (page 11 177:30291, `screens-insights-ai.md` §5.3), and the value shows the rule ("Monthly", "Weekly", …; "Never" = no rule). Save then creates a **recurring rule** as well as this first expense (`screens-insights-ai.md` §5.5).

### 3.12 Receipt
- "Add receipt" opens **Scan receipt — Camera** (page 11, 177:25568; `screens-insights-ai.md` §4.2). Free users can take or upload the photo and attach it. Reading the items is Pro (paywall).
- When a photo is attached, the row becomes "Receipt" · "Attached" with a 24.9 × 32 receipt thumbnail (radius 4.4) left of the value (as drawn on the page-11 prefilled form, `screens-insights-ai.md` §4.5). Tap it for the photo viewer (proposal).

### 3.13 Notes
- Optional free text, value "Optional" when empty, otherwise the first line (1 line, truncated, `text/secondary`). The row opens a notes sheet (not designed; proposal: Sheet / Container Medium "Notes" with a multi-line field (`bg/card`, radius 14, min 120 tall, Body) and a "Done" Button / Primary Large; max 500 characters).

### 3.14 What Save does
1. Creates the expense `{id, title, amount (minor units), currency, rate?, date, categoryId, payers [{personId, paid}], split {mode, rows [{personId, included, exactAmount | percent | shares, share}]}, groupId?, dueDate?, repeatRuleId?, receiptPhoto?, notes?, createdBy, createdAt, history: ["You added this"]}`.
2. Updates balances: for each person, net = paid − share. Demo: you paid ₹2,800 and your share is ₹700, so you're owed **₹2,100** (Priya, Esha and Dev each owe you ₹700). Home's "You're owed" total and the per-friend nets change accordingly (06-10 note). **In the demo dataset this expense already exists** (Home's Recent activity shows "Dinner at Olive Garden · You paid · 4 people · ₹2,800 · Today"), and its ₹2,100 is part of Home's **+₹2,900 from 4 people** (Rohan 800 + Priya 700 + Esha 700 + Dev 700, `screens-groups.md` §2.4).
3. Adds an Activity item ("You added Dinner at Olive Garden", `screens-activity.md`), notifies the other people (simulated), and schedules reminders for the due date (§3.7).
4. **Navigation**: the modal closes, the **new expense's detail** (§11) is pushed on the stack the modal was opened from (e.g. Home), and the **"Expense added" toast** shows for 2 s. Back returns there.

### 3.15 Edit mode and discarding
- **Edit** (from the Expense detail's "Edit") opens the same form **prefilled** with the expense (as a full-screen modal; Figma: MOVE_IN from the bottom, 300 ms ease-out). Proposal: title "Edit expense", the pill still says "Save", amount not focused on open. Saving updates the expense, adds a History entry ("You changed the amount from ₹2,800 to ₹3,000", …; templates in `screens-activity.md` §4.3-F) and returns to the detail.
- **✕ (close)**: if nothing was entered, close immediately. If anything changed, confirm with **Overlay / Alert Action=Destructive** (iOS `.alert`, `screens-record-lend-group.md` §1.2.12). Copy (proposal, consistent with the other forms): title **"Discard this expense?"**, message "Your changes won’t be saved.", buttons **"Keep editing"** (cancel) and **"Discard"** (destructive). In edit mode: "Discard changes?". Figma links ✕ straight to Home (24:5).
- iOS interactive swipe-down on the modal = ✕ (with the same check; `interactiveDismissDisabled(isDirty)` + an alert). Android system back on the form = ✕.

---

## 4. The form: `addExpenseEmpty` (176:17454) and `addExpenseFilled` (176:18002)

**Purpose:** record a shared bill in seconds: amount first, then who shares it, a title, and optional details. It's **one screen** with two designed states: Empty (just opened, amount focused, decimal pad up, Save disabled) and Filled (the Olive Garden bill ready to save, keyboard down, Save enabled).

**Container:** full-screen modal (no tab bar), opened from the ＋ Add sheet's "Add expense" row on every tab (`screens-home-v2.md` §2.4: dismiss the Add sheet, then present). iOS `.fullScreenCover` with its own `NavigationStack`, because Split with and the split editor push inside the modal. Android: a full-screen destination with its own nested NavHost, entering with a slide-up (300 ms ease-out) and leaving with a slide-down. Also opened prefilled by: Expense detail "Edit" (edit mode), Scan receipt (page 11), Ask Paybak "Edit" (page 11), Recurring (page 11), and a group's "Add expense" (group preset).

**Scaffold:** vertical column, padding 62 / 20 / 34 / 20 (`layout/status-bar`, `layout/screen-margin`, `layout/home-indicator`), **gap 16** (`space/16`), fill `bg/primary` #FFFFFF.
- **Filled** (the scroll-ready layout, use it for both states): the Modal Header is **fixed** at (20, 62) above a white **"Scroll edge (top)"** band (0, 0) 402 × 106 `bg/primary`. The column starts with a 44-tall **Header space** at y 62, so the content starts at y 122 at rest and scrolls vertically **under** the band (frame marked vertical scrolling). Content ends at y 919 + 34 bottom padding, so it scrolls by about 79 pt on this device (the Notes row is below the fold).
- **Empty**: Figma puts the header in the flow (same position). The kit keyboard covers y 566–874 and the form continues underneath it. On device: one scroll view. While the keyboard is up, its bottom inset = the keyboard height, so every row can be scrolled into view above it.

### 4.1 Modal header: `Navigation / Modal Header` (20, 62) 362 × 44 (`screens-record-lend-group.md` §1.2.1)
- **✕** kit glass xmark, 44 × 44 at (20, 62). → Close with the discard check (§3.15). Figma: link to Home — Active (24:5).
- **Title** "Add expense": Headline `text/primary`, centred, box 200 × 22 at (101, 73).
- **Save** Button / Primary **Small** 67 × 36 at (315, 66), label "Save" (Button/Small) at (331, 74).
  - Empty: **Action=Disabled**: fill `bg/disabled` #E0E0E0, label `text/disabled` #A3A3A3, not tappable.
  - Filled: **Action=Enabled**: fill `bg/inverse` #0A0A0A, label `text/inverse`; pressed `bg/inverse-pressed` #2B2B2B. Enable rule §3.2. Tap → Save (§3.14). Figma: Save hotspot → Expense added (DISSOLVE 300 ms ease-out).

### 4.2 Amount: `Control / Amount Display` (20, 122) 362 × 120 (State Empty / Filled)
V auto-layout, padding 4 top/bottom (`space/4`), gap 12 (`space/12`), children centred horizontally. `screens-record-lend-group.md` §1.2.3 has the component. Here **Show date chip = true** and **Show helper = false** (the helper appears for the rate line, §3.9).
1. **Chips** row (131, 126) 140 × 36, H gap 8 (`space/8`), centred:
   - `currency` Control / Category Chip Leading=None, Selected=False: **"INR"** 57 × 36 at (131, 126). Fill `bg/card` #F5F5F5, capsule, padding 0/16, label **Button/Small** `text/primary` at (147, 134). → **Currency** sheet (§9). Figma: INR hotspot → OPEN OVERLAY Currency, DISSOLVE 250 ms.
   - `date` Category Chip Leading=None: **"Today"** 75 × 36 at (196, 126), label at (212, 134). → **Date** sheet (§10.3). Figma: Today hotspot → OPEN OVERLAY Due date (the prototype reuses the Due date frame).
   - Min hit area 44 tall for both (suggestion).
2. **Value** (centred, gap 0) → `amount row` (H gap 2):
   - **Empty**: "₹0" **Amount/Display** (ExtraBold 56/64, −2 % = −1.12 pt) **`text/tertiary`** #A3A3A3, 72 × 64 at (163, 174), + **caret** rectangle **2 × 56** `text/primary` at (237, 178) (4 below the text top). Blink the caret at about 1 s (suggestion).
   - **Filled**: "₹2,800" Amount/Display **`text/primary`** #0A0A0A, 192 × 64 at (105, 174), no caret.
   - Tap → focus the amount (decimal pad). Figma: the Amount block (and the keyboard) → Add expense — Filled, DISSOLVE 300 ms ease-out, standing in for typing.

### 4.3 People (20, 258) 36 tall: "With you and" + chips
- `label` "With you and": **Subheadline** (Medium 14/20) `text/secondary` #6B6B6B, 86 × 20 at (20, 266).
- **Empty**: the row hugs (225 × 36, gap 8): **"Add people"** chip = Category Chip **Leading=Icon** (Icon / Plus), 131 × 36 at (114, 258): fill `bg/card`, capsule, padding **0 / 16 / 0 / 12**, gap 6, `plus.svg` **16 × 16** `icon/primary` at (126, 268), label "Add people" Button/Small `text/primary` at (148, 266). → **Split with** (§5). Figma: PUSH from the right (direction LEFT), 350 ms ease-in-out.
- **Filled**: the row is a **horizontal scroll strip**, 382 wide (x 20 → the right screen edge), clips, gap 8. Annotation: "Scrolls horizontally; the Add chip (→ 06-03) follows Dev."
  | Chip | Frame | Component | Content |
  |---|---|---|---|
  | Priya | (114, 258) 90 × 36 | Category Chip **Leading=Avatar**, Selected=False | padding 0 / 16 / 0 / **6**, gap **8**; Avatar / Circle **24** Art (Priya, `avatar-2`) on a **white** `bg/primary` circle at (120, 264); label "Priya" Button/Small `text/primary` at (152, 266) |
  | Esha | (212, 258) 89 × 36 | same | avatar-4, "Esha" |
  | Dev | (309, 258) 82 × 36 | same | avatar-5, "Dev" |
  | Add | (399, 258) 79 × 36 (off-screen until scrolled) | Leading=Icon | `plus.svg` 16 + "Add" |
  Chip labels are the **first names**. Order = the order people were added. Tap any chip → **Split with** (push). Scroll content inset: 0 left (the label is inside the strip), 20 right after the last chip (proposal).

### 4.4 Title field: `Control / Input Field` (20, 310) 362 × 52 (no label, no helper, no icon)
- Field 362 × 52, fill `bg/card`, radius 14 (`radius/input`), padding 0 / 16; text **Body** (Regular 16/24) at (36, 324), 1 line.
- **Empty (State=Default)**: placeholder **"What was it for?"** `text/tertiary`. **Filled (State=Filled)**: "Dinner at Olive Garden" `text/primary`.
- Focused: 1.5 pt inside ring `border/strong` (draw as an overlay; README §3 rule 6), caret `text/primary`. Keyboard: text, sentence capitalisation, return key "Done" (dismisses). Max 60 characters (proposal).

### 4.5 Form card "Form" (20, 378) 362 × 541
Fill `bg/card` #F5F5F5, radius **20** (`radius/card`), clips, V gap 0. Rows are `Row / Setting` **Trailing=Chevron, Tone=Default**, 362 × 56 (padding 12/16, gap 12): icon 24 at (36, y+16) `icon/primary`; title **Headline** `text/primary` at (72, y+17); value **Body** `text/secondary` right-aligned, ending at x 334; chevron `chevron-right.svg` **20 × 20** `icon/tertiary` at (346, y+18); divider = 1 pt `border/subtle` from x 72 to x 382 at the row bottom.

| y | Row | Icon | Value: Empty | Value: Filled | Divider | Tap → |
|---|---|---|---|---|---|---|
| 378 | Category | Empty `tag.svg` · Filled **`food.svg`** (the chosen category's icon, §3.8) | Choose | Food | yes | **Category** sheet (§8). Figma: OPEN OVERLAY Category, DISSOLVE 250 ms |
| 434 | Paid by | `wallet.svg` | You | You | yes | **Paid by** sheet (§6). Figma: OPEN OVERLAY Paid by, DISSOLVE 250 ms |
| 490 | Split | `split.svg` | Equally | **Equally · ₹700 each** | yes | **Split editor** (§7). Figma: NAVIGATE Split — Equally, PUSH from the right, 350 ms ease-in-out |
| 546 | Group | `groups.svg` | No group | No group | yes | group picker sheet (proposal, §3.10). Not linked in Figma |
| 602 | Due | `calendar.svg` | None | **Sun 4 Oct** | **no** (the chips follow) | **Due date** sheet (proposal: the row opens it too; in Figma only "Pick date" is linked) |
| 658 | *Due quick chips* | – | – | – | – | see below |
| 750 | *Divider / Line* Inset=Leading | 362 × 1, padding-left 52 → line x 72–382 | | | | |
| 751 | Repeat | `repeat.svg` | Never + **Pro** badge | Never + **Pro** badge | yes | free → **Paywall** (page 12, 167:13003); Pro → Repeat sheet (§3.11). Figma: URL → Paybak Pro — Paywall |
| 807 | Add receipt | `camera.svg` | (no value) | (no value) | yes | **Scan receipt — Camera** (page 11, 177:25568). Figma: URL → that frame |
| 863 | Notes | `note.svg` | Optional | Optional | no | notes sheet (proposal, §3.13). Not linked |

- **Due quick chips** "Due quick chips" (20, 658) 362 × 92: H **wrap**, padding **0 / 16 / 12 / 52** (the left inset aligns the chips with the row titles at x 72), gap 8, row gap 8, clips. Four Category Chips Leading=None, 36 tall, padding 0/16, label Button/Small:
  | Chip | Frame | Empty | Filled |
  |---|---|---|---|
  | Tomorrow | (72, 658) 104 × 36 | white `bg/primary`, `text/primary` | same |
  | This weekend | (184, 658) 130 × 36 | white | **Selected=True: `bg/inverse` #0A0A0A, label `text/inverse`** |
  | Next week | (72, 702) 106 × 36 | white | white |
  | Pick date | (186, 702) 98 × 36 | white | white · Figma: OPEN OVERLAY Due date, DISSOLVE 250 ms |
  Unselected chips on the card are **white** (`bg/primary`), not #F5F5F5. The rules are in §3.7. Pressed (suggestion): white → `bg/card-pressed`, black → `bg/inverse-pressed`.
- **Pro badge** on Repeat: Badge / Pill **Inverse** "Pro", 40 × 24 at (239, 767), fill `bg/inverse`, label **Caption/1** `text/inverse` at (249, 771), 12 left of the value "Never" (291, 767). Row order: icon · title (fills) · badge · value · chevron, gap 12.
- **Add receipt** has no value (Show value = false). Once a photo is attached: "Receipt" · "Attached" + thumbnail (§3.12).

### 4.6 Keyboard (Empty)
The system **decimal pad**, shown while the amount is focused. Figma's kit Number Pad sits at (0, 566) 402 × 308 with rounded top corners 27. Its bottom-left key is empty; the real decimal pad has "." there. The content doesn't move: everything down to the Split row (y 546) is above the keyboard. Tapping a row dismisses the keyboard and opens that row's picker (annotation §1.3).

### 4.7 State differences (Empty → Filled)
| Element | Empty (06-01) | Filled (06-02) |
|---|---|---|
| Save | Disabled (gray) | Enabled (black) |
| Amount | "₹0" tertiary + caret, decimal pad up | "₹2,800" primary, no keyboard |
| People | "With you and" + "Add people" | "With you and" + Priya, Esha, Dev + "Add" (scrolls) |
| Title | placeholder "What was it for?" | "Dinner at Olive Garden" |
| Category | Tag icon, "Choose" | Food icon, "Food" |
| Split | "Equally" | "Equally · ₹700 each" |
| Due | "None", no chip selected | "Sun 4 Oct", "This weekend" selected |
| Everything else | Paid by You · Group No group · Repeat Pro/Never · Add receipt · Notes Optional | same |

### 4.8 Navigation (the form)
| Element | Destination | Presentation |
|---|---|---|
| ✕ | close (discard check) → back to where it was opened (Home in Figma) | modal dismiss (slide down) |
| Save (enabled) | new expense detail `expenseAdded` (§11) + toast | dismiss the modal, then push the detail on the origin stack (Figma DISSOLVE 300 ms) |
| INR chip | Currency sheet (§9) | sheet, Large |
| Today chip | Date sheet (§10.3) | sheet, Medium |
| Amount | focus amount (decimal pad) | – |
| Add people / person chip / Add | Split with (§5) | push inside the modal (350 ms) |
| Title field | focus (text keyboard) | – |
| Category | Category sheet (§8) | sheet, Large |
| Paid by | Paid by sheet (§6) | sheet, Medium |
| Split | Split editor (§7) | push (350 ms) |
| Group | Group picker (proposal) | sheet, Medium |
| Due row / Pick date | Due date sheet (§10) | sheet, Medium |
| Tomorrow / This weekend / Next week | set the due date in place (§3.7) | – |
| Repeat | Paywall (free) / Repeat sheet (Pro) | paywall = full-screen modal over this modal (`screens-settings.md` §2); Repeat = sheet |
| Add receipt | Scan receipt — Camera (page 11) | full-screen (camera), returns to this form with the photo attached |
| Notes | Notes sheet (proposal) | sheet, Medium |
| Android system back / iOS swipe down | = ✕ | – |

---

## 5. `addExpenseSplitWith`: Split with (176:19238)

**Purpose:** pick who shares the bill (multi-select): search, selected chips, you, add a new friend, the friends list.

**Container:** pushed screen inside the Add expense modal (from "Add people", any person chip or "Add"). Figma: PUSH from the right, 350 ms ease-in-out; Back and Done → back to the form (PUSH to the right, 350 ms). No tab bar.

**Scaffold:** V column, padding 62 / 20 / 34 / 20, gap **16**, `bg/primary`, vertical scroll (content ends at y 898 + 34). The **Push Header is fixed** at (20, 62) over the white "Scroll edge (top)" band (0, 0) 402 × 106; the first child is a 44-tall Header space.

### 5.1 Push header: `Navigation / Push Header` Trailing=Text (20, 62) 362 × 44
- **Back**: Button / Icon Style=Glass (fill `bg/glass` #FFFFFF @72 %, 1 pt inside `border/glass-highlight`, `Material/Glass Small`), 44 × 44 at (20, 62), `chevron-left.svg` 24 `icon/primary` at (30, 72). → back to the form, **keeping the selection** (Figma: Back and Done go to the same place).
- **Title** "Split with": Headline `text/primary`, centred, box 200 × 22 at (101, 73).
- **Done**: glass capsule 74 × 44 at (308, 62), padding 0/16, min width 44, label "Done" **Headline** `text/primary` at (325, 73). → back to the form with the selection. Pressed: fill `bg/card`.

### 5.2 Elements, top to bottom
1. **Search** Control / Input Field **Default**, Leading icon = true (`search.svg` 20 × 20 `icon/secondary` at (36, 138)), (20, 122) 362 × 52, fill `bg/card`, radius 14; placeholder **"Name, phone, email or @username"** Body `text/tertiary`. Filters the Friends list live by name, phone, email or username (case-insensitive, contains). No match (proposal): a single row "Add “{query}” as a guest" (Sheet / Action Row, `user-add.svg`) that creates a guest friend (initials from the text) and ticks them; if the query looks like an email/phone, the row reads "Invite {query}".
2. **Selected** chips (20, 190), H gap 8, 36 tall, clips (proposal: make it a horizontal scroll strip, like the form, when it overflows): Category Chip **Leading=Avatar, Show remove = true**, fill `bg/card`, padding 0 / 16 / 0 / 6, gap 8: avatar 24 (white circle) · first name Button/Small `text/primary` · **`close.svg` 16 × 16 `icon/secondary`**.
   | Chip | Frame | ✕ at |
   |---|---|---|
   | Priya | (20, 190) 114 × 36 | (102, 200) |
   | Esha | (142, 190) 113 × 36 | (223, 200) |
   | Dev | (263, 190) 106 × 36 | (337, 200) |
   Tap a chip (or its ✕) → remove that person (untick them in the list). Hide the row when nobody is selected (proposal).
3. **You** card (20, 242) 362 × 64: `bg/card`, radius 20, one **Row / Person Regular, Trailing=Select On**: avatar 40 Art (Arjun, `avatar-1`, white circle) at (36, 254); name **"You"** Headline at (88, 252); subtitle **"Arjun Mehta"** Subheadline `text/secondary` at (88, 276); select circle 24 (black, white tick) at (342, 262). You are on the expense by default. Tapping toggles you (proposal: you can leave yourself out, e.g. a gift you only paid for, as long as someone else is on it).
4. **"Add a new friend"** Sheet / Action Row (Default, **no subtitle**), (20, 322) 362 × 72, radius 20, no fill: tile 44 × 44 `bg/card` radius 14 at (32, 336) with `user-add.svg` 24 `icon/primary`; title Headline at (88, 347); chevron 20 `icon/tertiary` at (350, 348). → **Add friend** (page 07, 177:26746, `screens-groups.md` §7), pushed. When it returns with a new friend, tick them (proposal). Figma: URL → Add friend.
5. **Friends** (20, 410), V gap 8:
   - Row / Section Header **"Friends"** (Title/3), no action, (20, 410) 362 × 32.
   - List card (20, 450) 362 × 448, `bg/card`, radius 20, clips: **Row / Person Regular** rows, 64 each, avatar 40 on a **white** circle, name only (no subtitle), trailing **Select On** (ticked) / **Select Off** (empty 1.5 pt ring `border/strong`), divider from x 88 to x 382 (not on the last row):
     | y | Name | Avatar | Trailing |
     |---|---|---|---|
     | 450 | Priya Sharma | avatar-2 | Select On |
     | 514 | Esha Kapoor | avatar-4 | Select On |
     | 578 | Dev Malhotra | avatar-5 | Select On |
     | 642 | Rohan Verma | avatar-3 | Select Off |
     | 706 | Kabir Singh | avatar-6 | Select Off |
     | 770 | Meera Iyer | avatar-7 | Select Off |
     | 834 | Ananya Rao **Guest** | Initials **"AR"** (Headline) in a white circle | Select Off, no divider |
   - The "Guest" tag is Badge / Pill (white `bg/primary` pill, **Caption/1** `text/secondary`), 8 right of the name, vertically centred on the name line.
   - Tap a row → toggle. Order (proposal): the order in Figma for the demo. Generally: selected first, then the most recently used friends, then alphabetical. Guests sort with everyone.

### 5.3 Behaviour
- Changes apply live. Back and Done both keep them (Figma links both to the form). The form's chips, the split and the Paid by list update when you return.
- Done/Back are always enabled. Save on the form handles "at least one other person".
- Keyboard: focusing the search shows the text keyboard. The list scrolls under it (bottom inset = keyboard height). Scrolling dismisses it (`scrollDismissesKeyboard(.interactively)`).

### 5.4 Navigation
| Element | Destination |
|---|---|
| Back / Done / system back | form (§4), pop 350 ms |
| Search | focus, filter |
| Chip ✕ / chip | remove person |
| You row, friend rows | toggle |
| Add a new friend | Add friend (page 07) push; returns here |

---

## 6. `addExpensePaidBy`: Paid by sheet (176:19877)

**Purpose:** choose who paid. Single tap, then close. "Multiple people" opens the payer editor.

**Container:** sheet (Sheet / Container **Detent=Medium**, Title "Paid by", no search) over the form, with the 40 % scrim. The background is the Filled form (Save enabled). Figma: OPEN OVERLAY, DISSOLVE 250 ms ease-out; scrim, ✕ and every person row → CLOSE.

### 6.1 Sheet (frame coordinates)
- Sheet (8, 431) **386 × 435**, radius 40, `bg/primary`, padding 8 / 16 / 28 / 16, gap 8. Bottom at y 866.
- Grabber 60 × 4 at (171, 439). Header (24, 451) 354 × 50: "Paid by" **Title/3** at (24, 463); kit glass ✕ **50 × 50** at (328, 451).
- Content (24, 509) 354 × 329, V gap 0, directly on the white sheet (no card):
  | y | Row | Component | Content |
  |---|---|---|---|
  | 509 | You | Row / Person **Regular, Trailing=Check**, 354 × 64 | avatar 40 Art Arjun on **`bg/card`** (white surface) at (40, 521); "You" Headline (92, 519); "Arjun Mehta" Subheadline `text/secondary` (92, 543); **`check.svg` 24 × 24 `icon/primary`** at (338, 529); divider x 92–378 at y 572 |
  | 573 | Priya Sharma | Row / Person Regular, **Trailing=None** | avatar-2 at (40, 585); name (92, 594); divider y 636 |
  | 637 | Esha Kapoor | same | avatar-4; divider y 700 |
  | 701 | Dev Malhotra | same, **no divider** | avatar-5 |
  | 765 | – | Divider / Line **Inset=None**, 354 × 1 full width | `border/subtle` |
  | 766 | Multiple people | **Sheet / Action Row** Default, 354 × 72 | tile 44 × 44 `bg/card` radius 14 at (36, 780) with **`people.svg`** 24; "Multiple people" Headline (92, 780); **"Enter how much each person paid"** Subheadline `text/secondary` (92, 804); chevron 20 `icon/tertiary` (346, 792) |
- The check marks the current payer: one row has it, or none when several people paid.

### 6.2 Behaviour
- Rows = you + the people on this expense (§3.4), in that order, full names. Tap → set the single payer, close the sheet (spring/slide down), and the form's Paid by value updates ("You", "Priya").
- **Multiple people** → close the sheet and push the **payer editor** (§6.3) on the modal's stack (06-04). Not linked in Figma.
- With more people than fit, the Medium sheet grows up to the Large detent and the list scrolls (proposal).

### 6.3 Payer editor ("Multiple people"), not drawn: build it from the split editor (06-04 note)
Same scaffold and components as the split editor (§7), with these differences:
- Push Header Trailing=Text: title **"Paid by"**, action **"Done"**.
- Summary line (Footnote `text/secondary`, centred): "{title} · {total}" ("Dinner at Olive Garden · ₹2,800").
- **No segmented control** (payers are always exact amounts).
- Card of **Row / Split Person Mode=Exact** rows, one per person on the expense. The tick means "paid something". Default: you ticked with the full amount, everyone else unticked (Excluded, ₹0). Ticking someone focuses their field.
- Hint (Footnote `text/secondary`): "Enter how much each person paid." (proposal; the subtitle's wording).
- Footer **Card / Split Total**: Left "{T − Σ paid} left" (or "… over"), Detail "{Σ paid} of {T}". Red Error while it doesn't add up. **Done disabled** until Σ = T.
- Done → back to the form. The Paid by value becomes "{n} people" (or the name when only one person paid). The expense stores every payer's amount (§3.4).

---

## 7. Split editor: `addExpenseSplitEqually` (176:21025) and `addExpenseSplitExactError` (177:21514)

**Purpose:** decide how the total is shared: Equally, Exact amounts, Percent or Shares. People can be left out, and a live footer shows what's left.

**Container:** pushed screen inside the Add expense modal (from the form's Split row). Figma: PUSH from the right, 350 ms ease-in-out; Back and Done → the form (PUSH to the right, 350 ms). No tab bar.

**Scaffold:** V column, padding 62 / 20 / 34 / 20, gap **16**, `bg/primary`. The Push Header is **in the flow** here: the content fits on screen with 4 people. With more people the list scrolls: make the header fixed with the white 106-tall band, as on Split with (proposal). The footer is pinned to the bottom (§2.3).

### 7.1 Elements (Split — Equally, 06-05)
1. **Push Header** Trailing=Text (20, 62) 362 × 44: glass Back (20, 62); title **"Split"** Headline centred (101, 73); glass capsule **"Done"** 74 × 44 at (308, 62), label Headline `text/primary` at (325, 73).
2. **Summary** (20, 122) 362 × 18: **"Dinner at Olive Garden · ₹2,800"** **Footnote** `text/secondary`, centred. Template `{title} · {total}`; only `{total}` when there's no title (proposal).
3. **Split mode** Control / Segmented **Options=4** (20, 156) **362 × 36**, fill `bg/card`, capsule, padding 3: four Control / Segment **89 × 30** at x 23, 112, 201, 290 (y 159), labels **"Equally"**, **"Exact"**, **"%"**, **"Shares"** (Button/Small). Selected: `bg/inverse` pill + `text/inverse`; others: no fill, `text/secondary`. Tap → switch mode (§3.5). Suggestion: slide the black pill (matchedGeometryEffect / animated offset, 250 ms ease-in-out).
4. **Split rows** card (20, 208) 362 × 256 (= 4 × 64), `bg/card`, radius 20, clips: Row / Split Person **Mode=Equally, State=Default** (§2.2):
   | y | Name | Avatar | Amount | Divider |
   |---|---|---|---|---|
   | 208 | You | Arjun (avatar-1) at (72, 224) | ₹700 | y 271 (x 116–382) |
   | 272 | Priya | avatar-2 | ₹700 | y 335 |
   | 336 | Esha | avatar-4 | ₹700 | y 399 |
   | 400 | Dev | avatar-5 | ₹700 | none |
   Select circle at (36, y+20), name Headline at (116, y+21), amount Headline right-aligned to x 366 (e.g. "₹700" at (327, 229)). Rows use first names and "You".
5. **Hint** (20, 480) 362 × 18: **"Uncheck someone to leave them out."** **Footnote** `text/secondary`, left-aligned, 16 below the card. It's drawn in Equally only (the Exact frame doesn't have it). Proposal: show it in every mode while the keyboard is down.
6. **Split total** Card / Split Total **State=Balanced** (20, 784) **362 × 56**: "₹0 left" (Amount/Medium `text/secondary`) at (36, 801) · "₹2,800 of ₹2,800" (Subheadline `text/secondary`, right-aligned) ending at x 366. Its bottom sits on the bottom safe-area edge (y 840).

### 7.2 State: Exact, not adding up (06-06)
Differences from 7.1:
- Header **Done disabled**: the glass capsule stays, the label turns **`text/tertiary`** #A3A3A3, and it isn't tappable.
- Segmented: **"Exact"** selected (segment 2).
- Rows = Row / Split Person **Mode=Exact**: a white field **96 × 36** at (270, y+14), radius 14, padding 0/12, right-aligned value (Headline `text/primary`):
  | y | Name | Field | State |
  |---|---|---|---|
  | 208 | You | ₹700 (value at (315, 229)) | Default |
  | 272 | Priya | ₹700 | Default |
  | 336 | Esha | ₹700 | Default |
  | 400 | Dev | **₹550** (value at (311, 421)) + **caret** 2 × 20 at (352, 422) + **1.5 pt `border/strong` ring** | **Focused** |
- No hint.
- **Split total State=Error** at **(20, 502)** 362 × 56, i.e. **8 above the keyboard** (top y 566): `alert.svg` 20 `icon/destructive` at (36, 520) · **"₹150 left"** Amount/Medium **`text/destructive`** #C93636 at (64, 519) · **"₹2,650 of ₹2,800"** Subheadline `text/secondary`, right-aligned (143–366).
- System **decimal pad** up (kit Number Pad at (0, 566) 402 × 308 in Figma). Nothing else moves.
- Maths: 700 + 700 + 700 + 550 = 2,650; 2,800 − 2,650 = **150 left**. Done becomes enabled (and the footer gray "₹0 left", "₹2,800 of ₹2,800") as soon as Dev's field reads ₹700 (06-06).

### 7.3 Percent and Shares (not drawn as frames; build them from the Row / Split Person variants, §2.2)
- **%**: rows Mode=Percent: name + money Footnote under it ("₹700"), and a 96 × 36 field with "25%". Footer per §3.5 ("0% left", "100% of 100%"). Decimal pad.
- **Shares**: rows Mode=Shares: name + money Footnote, a 48 × 36 centred field ("1") + the kit Stepper 92 × 32. Footer always balanced ("₹0 left", "₹2,800 of ₹2,800"). Integer keypad (iOS `.numberPad`, Android `KeyboardType.Number`). Stepper min 1 when ticked; unticked = 0 with the stepper disabled (40 % opacity).
- Excluded rows (any mode): open 1.5 pt ring, name and value in `text/tertiary`, value "₹0" / "0%" / "0" (§2.2).

### 7.4 Behaviour and keyboard
- Tap a **select circle** → include/exclude (the last ticked person can't be excluded). Equally recomputes instantly. In Exact/Percent, excluding sets that value to 0 and the footer updates.
- Tap a **field** → focus it (State=Focused ring + caret) with the decimal pad (integer pad for Shares) and select its content so typing replaces it (proposal). The footer rides 8 above the keyboard. The rows scroll if a focused field would be hidden.
- iOS: add a keyboard toolbar with **"Next"** (moves to the next ticked row's field) and **"Done"** (dismisses) (proposal, because the decimal pad has no return key). Android: `ImeAction.Next` / `Done`.
- In Equally, a tapped row shows the pressed look (`bg/card-pressed`) while the finger is down (State=Focused). Tapping the row body toggles the tick (proposal).
- **Done** (enabled only when balanced) → pop to the form with the new split. The Split row value updates (§3.5).
- **Back** / system back: if balanced, same as Done; if not, pop and **keep the last valid split** (the unbalanced edits are dropped, proposal).

### 7.5 Navigation
| Element | Destination |
|---|---|
| Back | form (pop 350 ms), see 7.4 |
| Done (balanced only) | form (pop 350 ms) with the split applied |
| Segments | switch mode in place |
| Select circle / field / stepper | edit in place |

---

## 8. `addExpenseCategory`: Category sheet (177:21884)

**Purpose:** a searchable category list. The pick sets the form's Category row (icon + name).

**Container:** Sheet / Container **Detent=Large**, **Show search**, Title "Category", over the Filled form and the scrim. Figma: OPEN OVERLAY DISSOLVE 250 ms; scrim, ✕ and every row → CLOSE.

- Sheet (0, 70) **402 × 804**, top corners 40, padding 8 / 16 / 34 / 16, gap 8, clips. Grabber (171, 78). Header (16, 90) 370 × 50: **"Category"** Title/3 at (16, 102); kit glass ✕ 50 × 50 at (336, 90).
- **Search** Control / Input Field Default (16, 148) 370 × 52: `search.svg` 20 `icon/secondary` at (32, 164), placeholder **"Search categories"** Body `text/tertiary`. Filters live (§3.8). No match (proposal): a centred Footnote `text/secondary` "No categories match “{query}”" 24 below the search.
- Content (16, 208): card **"Categories"** 370 × 448, `bg/card`, radius 20, clips. 8 × **Row / Setting** 370 × 56 (padding 12/16, gap 12): icon 24 `icon/primary` at (32, y+16), title **Headline** `text/primary` at (68, y+17), trailing 24 × 24 slot at (346, y+16), divider x 68–386 (none on the last row):
  | y | Title | Icon | Trailing |
  |---|---|---|---|
  | 208 | Food | `food.svg` | **Trailing=Check**: `check.svg` 24 `icon/primary` |
  | 264 | Travel | `car.svg` | Trailing=Unchecked (empty 24 slot) |
  | 320 | Stays | `bed.svg` | Unchecked |
  | 376 | Fun | `ticket.svg` | Unchecked |
  | 432 | Rent | `home.svg` | Unchecked |
  | 488 | Bills | `bolt.svg` | Unchecked |
  | 544 | Shopping | `shopping-bag.svg` | Unchecked |
  | 600 | Other | `tag.svg` | Unchecked, no divider |
- Tap a row → select it (check), close the sheet, and set the form's Category row to that icon + name. Opening the sheet focuses nothing (no keyboard). Tapping the search shows the keyboard.

---

## 9. `addExpenseCurrency`: Currency sheet (177:22329)

**Purpose:** pick the expense's currency. Recent ones first, then all. It reuses the Setup currency rows.

**Container:** Sheet / Container **Detent=Large**, **Show search**, Title "Currency", over the Filled form and the scrim (opened from the "INR" chip). Figma: OPEN OVERLAY DISSOLVE 250 ms; scrim, ✕ and every row → CLOSE.

### 9.1 Elements (frame coordinates; the sheet is at (0, 70))
- Header **"Currency"** (16, 102); ✕ (336, 90). Search (16, 148) 370 × 52, placeholder **"Search currencies"**.
- Content (16, 208), V gap **24** (`space/24`); each group V gap 4:
  - **Recent** group: Row / Section Header **"Recent"** (Title/3) (16, 208) 370 × 32, then Row / Currency rows **370 × 56**:
    | y | Symbol tile | Title | Subtitle | Radio |
    |---|---|---|---|---|
    | 244 | ₹ | Indian rupee | INR | **Selected** |
    | 304 | AED | UAE dirham | AED | off |
  - **All currencies** group at (16, 384): header **"All currencies"**, then:
    | y | Symbol | Title | Code |
    |---|---|---|---|
    | 420 | A$ | Australian dollar | AUD |
    | 480 | £ | British pound | GBP |
    | 540 | C$ | Canadian dollar | CAD |
    | 600 | € | Euro | EUR |
    | 660 | ¥ | Japanese yen | JPY |
    | 720 | S$ | Singapore dollar | SGD |
    | 780 | $ | US dollar | USD |
- **Row / Currency** at 370 wide (`components-core.md` §5.5): H gap 12, centred. Symbol tile **40 × 40** circle `bg/card` at (16, y+8) with the symbol in **Headline** `text/primary` centred (3-letter "AED" is also Headline here). Text column (68, y+6) 284 wide: title **Headline** `text/primary` + code **Subheadline** `text/secondary`. Radio **22 × 22** at (364, y+17): selected = `bg/inverse` + `check.svg` 14 `icon/inverse`; off = 1.5 pt inside ring **`bg/indicator`** #D1D1D1. No dividers.
- The list scrolls inside the sheet (the slot clips at the sheet's bottom padding).

### 9.2 Behaviour
- Tap a row → select, close, and the chip shows the code. A non-default currency adds the **rate line** and saves the rate (§3.9, 06-08).
- Search matches the name or the code across **all ISO 4217 currencies** (Setup 2's list and matching rules, `screens-setup.md` §2). While searching, hide the section headers and show one flat list (proposal).
- "All currencies" (proposal; Figma shows exactly these 7, alphabetical): the major currencies drawn here first, then every other ISO currency sorted by name. Currencies already under Recent aren't repeated.
- Names are **sentence case** here ("Indian rupee", "UAE dirham"), unlike Setup ("Indian Rupee"). Keep them verbatim per screen.

### 9.3 Symbol table used here (verbatim)
₹ INR · AED AED · A$ AUD · £ GBP · C$ CAD · € EUR · ¥ JPY · S$ SGD · $ USD.

---

## 10. `addExpenseDueDate`: Due date sheet (177:23763), and the Date sheet

**Purpose:** pick when the others should pay you back, on a calendar, with the reminder schedule spelled out.

**Container:** Sheet / Container **Detent=Medium**, Title "Due date", no search, over the Filled form and the scrim (opened from "Pick date" and the Due row). Figma: OPEN OVERLAY DISSOLVE 250 ms; scrim, ✕, "Set due date" and "No due date" → CLOSE.

### 10.1 Elements (frame coordinates)
- Sheet (8, 237) **386 × 629**, radius 40, padding 8 / 16 / 28 / 16, gap 8, bottom at y 866. Grabber (171, 245). Header (24, 257) 354 × 50: **"Due date"** Title/3 at (24, 269); kit glass ✕ 50 × 50 at (328, 257).
- Content "_Sheet / 06-09 Due date" (24, 315) 354 × 523, V gap **16**:
  1. **Calendar**: kit "Date and time - Pickers" **Style=Inline** (date only), (24, 315) **354 × 325**, white, padding 0 / 8 (grid 338 wide):
     - Header row 40 tall: **"October 2026"** (SF Pro Semibold 17, #000) + a disclosure "›" (SF Pro Bold 13, `text/primary`) = month/year picker; **‹** previous and **›** next month chevrons at the right (SF Pro Medium 20, tinted **`text/primary` #0A0A0A**, not the iOS blue).
     - Weekday row: **S M T W T F S** (SF Pro Semibold 13, #3C3C43 @ 30 %), 7 columns 50 apart, cells 38 wide.
     - Days: 38 × 38 cells on a 50-pt grid, SF Pro Regular 20 #000; October 2026 starts on **Thu 1**. Five week rows: 1–3 / 4–10 / 11–17 / 18–24 / 25–31.
     - **Selected day "4"**: a **black circle** (`bg/inverse`) with the number in **white, SF Pro Semibold 20**.
     - iOS: `DatePicker("", selection: $due, in: tomorrow..., displayedComponents: .date).datePickerStyle(.graphical).labelsHidden().tint(Color.pbBgInverse)` in a 338-wide frame. The system draws everything above; the black tint gives the black selection circle. Android: build a custom month grid with this geometry (Material 3's DatePicker looks different): header "October 2026 ›" (the disclosure opens a month/year wheel, proposal), ‹ › arrows, 7 × 38 dp cells, selected = #0A0A0A circle with a white SemiBold label. The iOS type is SF Pro; on Android use Manrope with the same sizes and weights (proposal).
     - Due dates **before today are disabled** (`text/tertiary`, not tappable; proposal). Today = "Wed 30 Sep" in the demo, so all of October is enabled.
  2. **Selected date** (24, 656) 354 × 62, V gap 4:
     - **"Sun 4 Oct · in 4 days"**: **Headline** `text/primary`. Template `{EEE d MMM} · {relative}` with relative = "today", "tomorrow", "in {n} days" (proposal for past dates on the Date sheet: "{n} days ago", "yesterday").
     - Hint (24, 682) 354 × 36 (2 lines): **"Paybak reminds them 2 days before, on the day, and every 3 days if it’s overdue."** **Footnote** `text/secondary`. Built from the Settings reminder schedule (§3.7).
  3. **Actions** (24, 734) 354 × 104, V gap 8, centred:
     - **"Set due date"**: Button / Primary **Large**, 354 × 52 at (24, 734), label Button/Large `text/inverse` at (149.5, 749). → apply the date, close, and the form's Due row shows "Sun 4 Oct". The quick chip "This weekend" shows as selected when the date equals it (§3.7).
     - **"No due date"**: Button / Text **Primary**, 86 × 44 at (158, 794), label Button/Small `text/primary` at (158, 806). → clear the due date ("None", no chip), close.
- ✕ / scrim / swipe down: close **without** changing anything.

### 10.2 Behaviour
- Opens on the current due date (or **tomorrow** when there's none; proposal), in that month. The summary line and "in N days" update live as you tap days.
- Figma selects **Sun 4 Oct** (the "This weekend" value) because the form already has it.

### 10.3 The **Date** sheet (the form's "Today" chip, 06-09 note; not drawn)
The same sheet with these differences:
- Title **"Date"**. The calendar selects the expense date (default **today**). **Future dates are disabled** (proposal; the note says "so you can back-date an expense").
- Summary line: `{EEE d MMM} · {relative}` e.g. "Wed 30 Sep · Today", "Mon 28 Sep · 2 days ago" (proposal).
- **No hint.**
- Button **"Set date"** (Primary Large). **No "No due date"** text button (the date is required; proposal).
- The sheet hugs its shorter content (about 537 tall, top at about y 329).
- On "Set date" the chip shows "Today" / "Yesterday" / "Mon 28 Sep" (§3.6).

---

## 11. `expenseAdded`: Expense added (177:24360)

**Purpose:** after Save, land on the new expense with a short confirmation. It's the **09-03 Expense detail template** (`screens-activity.md` §4, screen `expenseVilla`), filled with the Olive Garden data. Only the differences and the exact content are listed here; build it with the same view.

**Container:** pushed detail (no tab bar) on the stack the modal was opened from. The modal dismisses first, then the detail appears with the toast (Figma: Save → DISSOLVE 300 ms ease-out). **Back** → where Add expense was opened (Figma: link to Home — Active 24:5). **Edit** → Add expense in **edit mode** (Figma: NAVIGATE Add expense — Filled, MOVE_IN from the bottom, ease-out 300 ms).

**Scaffold:** V column, padding 62 / 20 / 34 / 20, **gap 24** (`layout/section-gap`), vertical scroll (content ends at y 1160 + 34). Fixed Push Header over the white band (0, 0) 402 × 106. The first child "Top" (20, 62) 362 × 254 (V gap 16) holds a 44-tall header space and the hero.

### 11.1 Elements, top to bottom (content verbatim)
1. **Push Header** Trailing=Text (20, 62): glass Back (20, 62) · **"Expense"** Headline centred (101, 73) · glass capsule **"Edit"** 64 × 44 at (318, 62), label at (335, 73).
2. **Header / Amount Hero** Leading=Icon (20, 122) 362 × 194, V gap 12:
   - Avatar / Circle **56 Type=Icon** `bg/card` with **`food.svg`** 24 `icon/primary` (36, 138), at (20, 122).
   - Text (20, 190), V gap 2: **"Dinner at Olive Garden"** Title/2 `text/primary` (20, 190) · **"₹2,800"** Title/1 `text/primary` (20, 222) 110 × 38 · **"Paid by you · Today"** Footnote `text/secondary` (20, 262).
   - Chips (20, 292): only **chip 1 = "Food"** Badge / Pill **Muted** (`bg/card`, Caption/1 `text/secondary`) 49 × 24. The group chip is **hidden** (no group), so the category chip moves into the first slot (chip 2 and chip 3 off). Rule: chips = [group?, category], left to right.
3. **Share card** (20, 340) 362 × 112, `bg/card` radius 20: Row / Setting **Trailing=None**:
   - **"Your share"** `wallet.svg` · value **"₹700"** (328, 356) · divider (x 72–382, y 395), at y 340;
   - **"Due"** `calendar.svg` · value **"Sun 4 Oct"** (294, 412) · no divider, at y 396.
   - **No "Your {group} balance" row** (not in a group) and **no "You’re owed" row** (06-10 note).
4. **Split** (20, 476), V gap 8: Row / Section Header **"Split equally · 4 people"** (Title/3) · card (20, 516) 362 × 224 `bg/card` radius 20 with **Row / Person Compact, Trailing=Value** (56 each, avatar 32 white):
   | y | Name | Subtitle | Value |
   |---|---|---|---|
   | 516 | You (avatar-1) | **Paid ₹2,800** | ₹700 |
   | 572 | Priya (avatar-2) | – | ₹700 |
   | 628 | Esha (avatar-4) | – | ₹700 |
   | 684 | Dev (avatar-5) | – | ₹700 (no divider) |
   The payer is first. Dividers run from x 80 to x 382.
5. **Receipt** (20, 764), V gap 8: header **"Receipt"** · card (20, 804) 362 × 56 `bg/card` radius 20 with Row / Setting Trailing=Chevron **"Add receipt"** (`camera.svg`, no value, chevron at (346, 822)). → Scan receipt — Camera (page 11). With a receipt, the template's thumbnail card replaces it (`screens-activity.md` §4.3-D).
6. **Comments** (20, 884), V gap 8: header **"Comments"** · Control / Composer **State=Empty, Pinned=False**, **Show mic = false** (20, 924) 362 × 52: `bg/card` radius 14, placeholder **"Add a comment"** Body `text/tertiary` at (36, 938). No comments yet. Tap → the comment state (`screens-activity.md` §4.6).
7. **History** (20, 1000), V gap 8: header **"History"** · Row / History **Position=Last** (20, 1040) 362 × 40: dot 8 × 8 `icon/tertiary` at (20, 1046); **"You added this"** Subheadline `text/primary` (40, 1040); **"Today"** Footnote `text/tertiary` (40, 1062).
8. **Actions** card (20, 1104) 362 × 56 `bg/card` radius 20: Row / Setting **Tone=Destructive, Trailing=None**: `delete.svg` `icon/destructive` + **"Delete expense"** Headline `text/destructive`. → the delete alert (`screens-activity.md` §4.7). There's **no "Flag an issue" row**, because you're the payer (matches `screens-activity.md` §4.3-G: hidden for the payer).
9. **Toast** Overlay / Toast (115, 780) **173 × 44**, fixed (not scrolling), centred horizontally, **bottom edge 50 above the screen bottom** (y 824): `bg/inverse` capsule, padding 0 / 20 / 0 / 16, gap 8, `check-circle.svg` 20 `icon/inverse` at (131, 792) + **"Expense added"** Button/Small `text/inverse` at (159, 792). Annotation: "Toast: fixed position, auto-dismisses after 2 s. No link." Fade in about 200 ms, stay 2 s, fade out about 200 ms (suggestion). Not tappable, and it doesn't block touches (proposal).

### 11.2 Numbers (must come from the data, §3.14)
Total ₹2,800; paid by you; 4 people × ₹700; your share ₹700 → you're owed ₹2,100: Priya, Esha and Dev each owe you ₹700, due Sun 4 Oct. In the demo dataset this is the existing Olive Garden expense (dated today, before 8:00 pm), and these ₹2,100 are part of Home's +₹2,900 (with Rohan's ₹800). Saving a *new* expense adds its amounts on top.

### 11.3 Navigation
| Element | Destination |
|---|---|
| Back / system back | pop to the origin (Home in Figma) |
| Edit | Add expense (edit mode, prefilled), full-screen modal from the bottom 300 ms |
| Add receipt | Scan receipt — Camera (page 11) |
| Composer | comment state (keyboard) |
| Delete expense | delete alert |
| Person rows | none in Figma. Proposal: friend detail (page 07) |

---

## 12. Sample data (verbatim, including clipped and off-screen content)

- **People** (full name → chip/row name → avatar): Arjun Mehta → "You" → avatar-1 · Priya Sharma → "Priya" → avatar-2 · Esha Kapoor → "Esha" → avatar-4 · Dev Malhotra → "Dev" → avatar-5 · Rohan Verma → avatar-3 · Kabir Singh → avatar-6 · Meera Iyer → avatar-7 · **Ananya Rao** → initials **"AR"**, tag **"Guest"**.
- **Form (Empty)**: "Add expense", "Save" (disabled), "INR", "Today", "₹0", "With you and", "Add people", "What was it for?", rows: Category "Choose" · Paid by "You" · Split "Equally" · Group "No group" · Due "None" · chips "Tomorrow", "This weekend", "Next week", "Pick date" · Repeat "Pro" "Never" · "Add receipt" · Notes "Optional".
- **Form (Filled)**: "₹2,800"; chips Priya, Esha, Dev, "Add" (the Add chip is off-screen right); "Dinner at Olive Garden"; Category **"Food"** (Food icon); Paid by "You"; Split **"Equally · ₹700 each"**; Group "No group"; Due **"Sun 4 Oct"** with **"This weekend"** selected; Repeat "Pro" "Never"; "Add receipt"; Notes "Optional" (below the fold).
- **Split with**: search "Name, phone, email or @username"; chips Priya, Esha, Dev (each with ✕); "You" / "Arjun Mehta" (ticked); "Add a new friend"; "Friends": Priya Sharma ✓, Esha Kapoor ✓, Dev Malhotra ✓, Rohan Verma, Kabir Singh, Meera Iyer, Ananya Rao "Guest" (the last row sits under the home indicator; reachable by scrolling).
- **Paid by**: "Paid by"; You / Arjun Mehta (✓); Priya Sharma; Esha Kapoor; Dev Malhotra; "Multiple people" / "Enter how much each person paid".
- **Split — Equally**: "Split", "Done", "Dinner at Olive Garden · ₹2,800", "Equally" "Exact" "%" "Shares"; You/Priya/Esha/Dev ₹700 each; "Uncheck someone to leave them out."; "₹0 left" / "₹2,800 of ₹2,800".
- **Split — Exact (error)**: fields ₹700, ₹700, ₹700, **₹550** (focused); "₹150 left" / "₹2,650 of ₹2,800"; Done disabled.
- **Category**: "Category", "Search categories", Food ✓, Travel, Stays, Fun, Rent, Bills, Shopping, Other.
- **Currency**: "Currency", "Search currencies", "Recent": ₹ Indian rupee INR (selected), AED UAE dirham AED; "All currencies": A$ Australian dollar AUD, £ British pound GBP, C$ Canadian dollar CAD, € Euro EUR, ¥ Japanese yen JPY, S$ Singapore dollar SGD, $ US dollar USD.
- **Due date**: "Due date", "October 2026", S M T W T F S, days 1–31 (Oct 1 = Thursday), selected 4; "Sun 4 Oct · in 4 days"; "Paybak reminds them 2 days before, on the day, and every 3 days if it’s overdue."; "Set due date"; "No due date".
- **Expense added**: "Expense", "Edit"; "Dinner at Olive Garden", "₹2,800", "Paid by you · Today", chip "Food"; "Your share" "₹700"; "Due" "Sun 4 Oct"; "Split equally · 4 people": You "Paid ₹2,800" ₹700, Priya ₹700, Esha ₹700, Dev ₹700; "Receipt" / "Add receipt"; "Comments" / "Add a comment"; "History": "You added this" "Today"; "Delete expense"; toast "Expense added".
- **Hidden component defaults you'll see in Figma props; don't render them**: Amount Display Helper "You owe Meera ₹450 in Flat 302"; Input Field Label "Email" / Helper "We’ll send a 6-digit code."; Row / Setting Subtitle "Paid back in parts" and Value "UPI" (hidden); Row / Person Amount "₹700" / Amount label "Due Sun 4 Oct" / Status "Settled" on rows whose trailing isn't Value; Row / Split Person Percent "25%" / Shares "1" in Equally/Exact rows; Sheet / Action Row Subtitle "Split a bill with friends or a group" on "Add a new friend" (subtitle hidden).
- **Demo-data needs** (input to `domain.md` / `seed/`): the 7 friends above, with Ananya Rao as a guest; the Dubai Weekend group in AED (so AED is "Recent"); Arjun on the free plan; the reminder schedule default (2 days before, on the day, every 3 days). The **Olive Garden expense is part of the base demo dataset** (₹2,800, today, you paid, split equally with Priya, Esha and Dev, not in a group, due Sun 4 Oct, category Food, no receipt, no comments, history "You added this · Today"). It's what Home's first Recent activity row, the Friends list ("Due Sun 4 Oct" for Priya/Esha/Dev) and the +₹2,900 total are computed from (`screens-groups.md` §2.4 and demo notes, `screens-activity.md` §11). Esha's pending ₹700 claim on it (9:12 pm) is also demo data (Activity/Home "Confirm payment"). The debug start screen `expenseAdded` opens this existing expense's detail with the toast. It doesn't create a second copy (§13).

---

## 13. Test IDs and debug start screens

**Debug start screens** (flow.md `-startScreen <id>` / `--es startScreen <id>`, with the demo seed):
| id | Opens |
|---|---|
| `addExpenseEmpty` | Home with the Add expense modal presented, empty, amount focused |
| `addExpenseFilled` | the modal prefilled with the Olive Garden draft (₹2,800; Priya, Esha, Dev; "Dinner at Olive Garden"; Food; due This weekend), keyboard down |
| `addExpenseSplitWith` | the Filled draft with Split with pushed |
| `addExpensePaidBy` | the Filled draft with the Paid by sheet open |
| `addExpenseSplitEqually` | the Filled draft with the split editor pushed (Equally) |
| `addExpenseSplitExactError` | the split editor in Exact with Dev = ₹550 focused |
| `addExpenseCategory` / `addExpenseCurrency` / `addExpenseDueDate` | the Filled draft with that sheet open (Due date on Sun 4 Oct) |
| `expenseAdded` | the demo's existing Olive Garden expense detail, pushed over Home, with the "Expense added" toast (don't create a duplicate) |
| (note) | The `addExpenseFilled…` start screens only prefill a **draft**. Saving it in the demo creates a second Olive Garden expense (fine for testing, but the Home numbers then change). |

**Test IDs** (`<screen>.<element>`, same on both platforms; roots `screen.<id>`):
- Form: `addExpense.close`, `addExpense.save`, `addExpense.amount`, `addExpense.currency`, `addExpense.date`, `addExpense.addPeople`, `addExpense.person.<personId>`, `addExpense.title`, `addExpense.row.category`, `addExpense.row.paidBy`, `addExpense.row.split`, `addExpense.row.group`, `addExpense.row.due`, `addExpense.due.tomorrow`, `addExpense.due.weekend`, `addExpense.due.nextWeek`, `addExpense.due.pick`, `addExpense.row.repeat`, `addExpense.row.receipt`, `addExpense.row.notes`, `addExpense.discardAlert`.
- Split with: `splitWith.back`, `splitWith.done`, `splitWith.search`, `splitWith.chip.<personId>`, `splitWith.you`, `splitWith.addFriend`, `splitWith.friend.<personId>`.
- Paid by: `paidBy.sheet`, `paidBy.close`, `paidBy.row.<personId|you>`, `paidBy.multiple`; payer editor `payers.done`, `payers.row.<personId>`, `payers.total`.
- Split editor: `split.back`, `split.done`, `split.mode.equally|exact|percent|shares`, `split.row.<personId>`, `split.row.<personId>.select`, `split.row.<personId>.field`, `split.row.<personId>.stepper`, `split.total`, `split.hint`.
- Sheets: `category.sheet`, `category.search`, `category.row.<categoryId>`; `currency.sheet`, `currency.search`, `currency.row.<CODE>`; `dueDate.sheet`, `dueDate.calendar`, `dueDate.summary`, `dueDate.set`, `dueDate.none`, `dueDate.close` (the Date variant: `date.sheet`, `date.set`).
- Detail: as `screens-activity.md` (`expense.back`, `expense.edit`, …) + `toast`.
- **UI-test flows** to cover: open from ＋ → Save disabled → type 2800 → add Priya/Esha/Dev → Save enabled → title → Category Food → This weekend → Save → detail shows ₹700 × 4 and the toast; the Exact error (Dev 550 → Done disabled, "₹150 left"; 700 → enabled); the fair-rotation unit test (₹1,000 / 3 twice → the extra paisa moves).

---

## 14. Art and assets

- **Illustrations: none.** These frames have no Open Doodles/Rive art, so no `.riv` is reused and nothing new was exported. People art = the existing Open Peeps avatars (`assets/avatars/avatar-1…7`), plus initials for guests.
- **Icons: nothing new.** Every icon used already exists in `assets/icons/`: `plus`, `tag`, `food`, `wallet`, `split`, `groups`, `calendar`, `repeat`, `camera`, `note`, `chevron-right`, `chevron-left`, `search`, `close`, `check`, `user-add`, `people`, `alert`, `car`, `bed`, `ticket`, `home`, `bolt`, `shopping-bag`, `delete`, `check-circle`, and `mail` (the hidden default icon of the input fields).
- **System (don't ship assets)**: kit xmark (SF Symbol `xmark`, 19 pt Semibold; Android `close.svg`), grabber, decimal pad, graphical DatePicker, Stepper (SF `minus`/`plus`), status bar, home indicator.
- **References added for this spec**: `ref/addExpenseEmpty.png`, `ref/addExpenseFilled.png`, `ref/addExpenseSplitWith.png`, `ref/addExpensePaidBy.png`, `ref/addExpenseSplitEqually.png`, `ref/addExpenseSplitExactError.png`, `ref/addExpenseCategory.png`, `ref/addExpenseCurrency.png`, `ref/addExpenseDueDate.png`, `ref/expenseAdded.png` (2×, 804 × 1748, from the REST renders) and the same ids with `_1x` (402 × 874, cropped from one section screenshot).

---

## 15. Gaps, proposals and open questions

### 15.1 How the Figma read went
The Figma MCP allowance (Education plan: 200 calls/day, 10/min) was exhausted while this was written. Three `get_design_context` calls (Empty, Filled, Split with), one reactions/components `use_figma` and one section screenshot succeeded. The other seven frames were read from the REST export (now `.figma-cache/nodes/77-100.json`), which has the full node trees (geometry, auto-layout, fills, text, component properties, annotations and interactions). So **no information is missing**: the REST node trees stand in for the missing design-context output. Kit-component internals (calendar, keyboard, stepper) are system UI and were read only as far as needed.

### 15.2 Not designed: proposals written above (confirm or replace)
1. Discard alert on ✕ ("Discard this expense?", "Your changes won’t be saved.", Keep editing / Discard) (§3.15).
2. Default title (category name or "Expense") and default category Other (§3.2).
3. "Next week" = today + 7; tapping the selected due chip clears it; the Due row itself opens the sheet (§3.7).
4. The Split row value formats for Exact/%/Shares and for uneven Equally; "Doesn't add up" (§3.5).
5. Exact "… over" and Percent footers (§3.5).
6. Back on an unbalanced split keeps the last valid split (§7.4).
7. The payer editor ("Multiple people") layout and the "{n} people" value (§6.3).
8. The rate line as the Amount Display helper: "≈ ₹27,420 · ₹22.85 per AED" (§3.9).
9. The "All currencies" ordering (Figma shows exactly 7 major currencies) (§9.2).
10. Group picker, Notes sheet, guest creation from search, the Date sheet summary copy, future/past date limits (§3.10, §3.13, §5.2, §10).
11. Pro members: the Repeat badge is hidden (§3.11).
12. Android calendar: a custom grid matching the iOS kit layout (§10.1).

### 15.3 Open questions
- Should **you** be removable from the split (a bill you paid only for others)? This spec allows it (§5.2) as long as someone else is on the expense.
- Figma's Paid by sheet is 435 tall for 4 people. With many people, grow to Large (proposal) or keep Medium and scroll?
- Currency rates source: a bundled table only (proposal), or a user-editable rate per expense?
