# Paybak: Settle Up screens spec (Figma page "08 Settle Up", 77:102)

Screens: `settleOwedBreakdown` (167:11705), `settleOweBreakdown` (167:11957), `settleUp` (167:12113), `settleRecordKabir` (167:13635), `settlePaymentPending` (167:17304), `settleRemind` (177:21203, plus overlay-only copy 177:21801), `settleRemindShare` (177:23342), `settleNotReceived` (177:25006, plus overlay-only copy 177:25168), `settlePaymentConfirmed` (177:25303). Two local sheet components: `_Sheet / Remind Rohan` (176:20970) and `_Sheet / Not received` (177:24988).

Everything here was read from Figma with read-only Plugin API dumps: node trees, auto-layout, tokens, text styles, instance props, reactions and every caption. The combined dump was the exact source for every number below (it wasn't kept; the same node data is in `.figma-cache/nodes/77-102.json`, regenerate with `tools/fetch_figma.py`). The 2× references were exported per section with `download_assets` (scale 2) and cropped to each frame; they include the iOS status bar and home indicator, which you don't draw.

Related specs: `components-core.md` (buttons, badge, avatar, segmented control, input field, divider), `components-home.md` (nav header, tab bar, balance cards, section header, attention/activity rows, sheet container, materials), `screens-home.md` (Home Active), `screens-setup.md` (Card / Payment Preview on Setup 3). The NEW components used here are described in full in §0.4, because they weren't in the two component files when this was written (`components-app.md` was written in parallel; if it disagrees on a geometry, trust Figma).

---

## 0. Conventions, shared rules and new components

### 0.1 Conventions
- Frame = 402 × 874 pt. All `x, y` are **frame coordinates**. Top safe area 62, bottom 34 (home indicator from y 840). Don't draw the status bar, home indicator, kit share sheet chrome you get from the OS, or the invisible `Hotspot — …` frames.
- Colours are tokens from `tokens.md` (e.g. `text/secondary` = #6B6B6B). Text styles are the `tokens.md` names (Manrope).
- Text is verbatim. The minus sign in amounts is U+2212 "−", the rupee U+20B9 "₹", separators U+00B7 "·", apostrophes are the curly U+2019 "’" in every string on this page (including **"You’re owed"**, see §9.2), the ellipsis in "Share…" is U+2026.
- Icons: `assets/icons/<name>.svg`, scaled from 24 (24 → stroke 1.5, 20 → 1.25, 16 → 1.0).
- Screen numbering in the designer's notes: 08-01 You’re owed breakdown, 08-02 You owe breakdown, 08-03 Settle up, 08-04 Record payment — Kabir, 08-06 Payment pending, 08-07 Remind — Rohan, 08-07s Remind sheet (overlay), 08-08 Remind — Share, 08-10 Not received, 08-10s Not received sheet (overlay), 08-11 Payment confirmed. (08-05 and 08-09 don't exist on this page; 04-06 is "Home — Confirm payment" on page 04, specced in `screens-home-v2.md`.)

### 0.2 Page and section notes (verbatim)
- Section "Balance breakdowns" (167:11699), subtitle: "The screens behind the two Home balance cards: who owes you, and who you owe."
- Section "Settle up & record" (167:11702), subtitle: "The simplified suggestion list, Record payment prefilled for Kabir (UPI, with Copy), and the pending detail the payer sees."
- Section "Remind" (176:20967), subtitle: "The reminder sheet over Home, the same sheet as an overlay-only frame, and the iOS share sheet."
- Section "Receiver confirmation" (177:24985), subtitle: "The Not received sheet over Home, the same sheet as an overlay-only frame, and Home after Confirm."
- Section "Overlay helpers" (216:29119), subtitle: "Copies of overlays from other pages." Caption of "↳ Add sheet (overlay)" (216:29122): "Prototype helper, not a screen: a copy of Overlay — Add sheet (page 04). ＋ on 08-11 opens it with Open overlay. Its rows open the page 06 forms by URL; the scrim and ✕ close it."
  - That helper is the Home Add sheet (`screens-home.md` §5). Its rows link to page 06: Add expense → "Add expense — Empty" (176:17454), Record payment → "Record payment — form" (177:29750), Lend money (IOU) → "Lend money — form" (185:25810), New group → "New group — Group" (190:8356).

### 0.3 The money rules on this page (from the notes; they are requirements)
1. **Paybak never moves money.** Settling = the user pays in their own UPI/bank/cash app, then records it here.
2. **A recorded payment is "Pending confirmation" until the receiver confirms.** Balances do not change while it's pending ("Home keeps −₹1,850, until Kabir confirms").
3. **The receiver confirms or says "Not received".** Confirm settles that amount and updates both sides. Not received sends the payer an editable note; the debt stays ("Her ₹700 stays owed until a payment is confirmed").
4. **Red is only for overdue** (Rohan’s "Overdue 3 days" badge). "Nothing is red, because nothing is overdue" on the You owe breakdown. Money you owe is gray (`text/secondary`) with "−"; money you're owed is black (`text/primary`) with "+".
5. **Simplified debts per group.** Goa Trip uses simplified debts, so Arjun pays Kabir the whole ₹1,400 directly (see §3.1 for the algorithm and the demo numbers).
6. **Reminders change nothing.** "The balances don’t change."

### 0.4 NEW components used on this page (not in components-core.md / components-home.md)
Descriptions are the Figma component descriptions, quoted. Geometry is from the instances on this page.

**A. Navigation / Push Header (set 97:1082). SwiftUI `PBPushHeader`.** Variants `Trailing` = Text | Icon | None | Wide Text; props `Title`, `Action` ("Save"), `Show title`.
> "Pushed-screen header, 44 tall: glass back button, centred Headline title (Show title), and a trailing action. Trailing=Text: glass capsule text action (Save). Trailing=Icon: exposed glass Button / Icon (default Settings; gear on 10-01). Trailing=Wide Text: for long text actions (“Mark all read”): the capsule has 12 side padding and a 122 max width, and the centred title box is 102 wide and truncates, so at least 8 pt always separates title and action. Trailing=None: back only. Tall scrolling frames set the instance to fix position and give it a color/bg/primary fill. Built with .navigationTitle and a toolbar. SwiftUI: PBPushHeader"
- 362 × 44 at (20, 62). Horizontal, space-between, centred.
- `Back`: `Button / Icon` Style=Glass, 44 × 44 at (20, 62): fill `bg/glass` (#FFFFFF @72 %), 1 pt inside stroke `border/glass-highlight`, effect Material/Glass Small, icon `chevron-left.svg` 24 at (30, 72) `icon/primary`. (components-core §2.3.)
- `title`: absolute text box 200 × 22 at (101, 73), **Headline**, `text/primary`, centred, 1 line.
- Trailing=Text `Action`: glass capsule 44 tall, padding 0/16, hugs its label (Edit = 64 × 44 at (318, 62)), same fill/stroke/effect as the back button, label **Headline** `text/primary`. Pressed: fill `bg/card` (like Glass Pressed).
- iOS: a native `NavigationStack` push with `.navigationTitle` inline + toolbar items styled as glass (`.glassEffect`); Android: a custom 44-dp row.

**B. Navigation / Modal Header (set 115:886). SwiftUI `PBModalHeader`.** Variants `Action` = Enabled | Disabled | None; props `Title`, `Show title`.
> "PBModalHeader — Toolbar for full-screen modals (Add expense, Record payment, Lend money, New group, Ask Paybak, paywall): the kit glass xmark (cancel) on the LEFT, a centred Headline title and the confirmation pill on the right.
> Action=Enabled (black Button / Primary Small) · Disabled (until the form is valid) · None (Ask Paybak, paywall). The pill is the exposed nested instance “action”: set its Label#9:0 to Save / Create / Add. 362×44, placed at y 62 inside the 20pt margins. No fill, no shadow; sheets keep their xmark on the right.
> SwiftUI: PBModalHeader"
- `close` 44 × 44 at (20, 62): the iOS kit "Button - Liquid Glass - Symbol" (xmark, radius 1000). Draw it like the Add-sheet close (components-home §14): a white glass disc with the xmark glyph (iOS: `Image(systemName: "xmark")` in a `.glassEffect(.regular.interactive(), in: .circle)` 44 frame; Android: `close.svg` 20–24 dp tinted #1A1A1A on a white disc with a light hairline ring).
- `title` 200 × 22 at (101, 73), **Headline**, centred, 1 line.
- `action` = `Button / Primary` Size=Small, 36 tall, padding 0/16, hugs ("Save" = 67 × 36 at (315, 66)), `bg/inverse`, label **Button/Small** `text/inverse`. Disabled: `bg/disabled` + `text/disabled`.

**C. Row / Person (set 127:2252). SwiftUI `PBPersonRow`.** Variants `Size` = Regular | Compact × `Trailing` = Owed | Owe | Value | Muted | Check | Select On | Select Off | Remove | Button | None. Props: `Name`, `Subtitle`, `Show subtitle`, `Show tag`, `Amount`, `Amount label`, `Show amount label`, `Show status badge`, `Status`, `Show divider`.
> "PBPersonRow — the one people row (split pickers, balances, breakdowns, contacts, expense splits, chat answers). Exposed Avatar / Circle (40 Regular / 32 Compact), Name (Headline) + Show tag (exposed Badge / Pill Muted “Guest”, always next to the name), Subtitle (Subheadline), trailing, Show divider (inset to the name).
> Size=Regular: 64, avatar on color/bg/card for white surfaces (inside a #F5F5F5 card, override the avatar fill to color/bg/primary). Size=Compact: 56, avatar 32 in a white circle and an On Card tag, for use inside a card.
> Trailing: Owed = “+” + Amount (Amount/Medium, text/primary) · Owe = “−” + Amount (Amount/Medium, text/secondary) · Value = Amount (Headline, e.g. ₹700 or 25%) — these three carry Amount label (Footnote, tertiary) and Show status badge (exposed Badge / Pill Overdue “Overdue 3 days”, always under the amount) · Muted = Status (Subheadline, secondary: Settled / Added / No balance) · Check = black tick (single select, e.g. Paid by) · Select On / Select Off = 24 black circle with a white tick / empty border/strong ring (multi-select) · Remove = Icon / Close 20 (icon/secondary) · Button = exposed Button / Secondary Small “Invite” (Regular) or Button / On Card Small “Remind” (Compact) · None.
> Amount excludes the sign: the variant adds “+” or “−”. Guests: Avatar Type=Initials “AR” + Show tag.
> SwiftUI: PBPersonRow"
- Regular (as used here): 362 wide (354 inside a sheet), min height 64, horizontal, padding 8 top/bottom, 16 left/right, gap 12, centred.
  - avatar: `Avatar / Circle` 40 Art (fill `bg/primary` white on these #F5F5F5 cards), at row (16, 12).
  - text column (row x 68), fills, V gap 2: name line (H gap 8: `name` **Headline** `text/primary`, max width 200, 1 line, truncate; optional tag) + `subtitle` **Subheadline** `text/secondary`, 1 line, truncate.
  - trailing column, hug, right-aligned, V gap 2: `amount` row = `sign` + `value`, both **Amount/Medium** (Owed: "+" `text/primary`; Owe: "−" `text/secondary`), then either `amount label` (**Footnote** `text/tertiary`, e.g. "Due Sun 4 Oct") or `status badge` (`Badge / Pill` Overdue, 24 tall, e.g. "Overdue 3 days" 110 wide).
  - `divider` (Show divider): `Divider / Line` Inset=None, absolute at the row bottom (y+63), 1 pt `border/subtle` #EBEBEB, from the name's x (row x 68) to the row's right edge (294 wide in a 362 row). The last row in a card has no divider.

**D. Card / Notice (set 129:1976). SwiftUI `PBNoticeCard`.** Variants `Layout` = Leading | Centered × `Actions` = None | One | Two; props `Title`, `Show title`, `Body`, `Show badge`.
> "PBNoticeCard — an in-flow notice on #F5F5F5 r20 (06-12 · 07-11 · 08-03 · 08-06 · 09-06 · 10-05 · 10-06 · 11-03): pending confirmation, invite a guest, simplified debts, a dispute, a read-only project, a Pro lock.
> Layout=Leading: exposed icon circle Avatar / Circle 40 Type=Icon On Card (set its Icon: Activity, Shuffle, Mail, Flag, Lock, Check Circle), Title (Headline) with Show badge (exposed Badge / Pill, default Inverse “Pro”) on the title line, Body (Subheadline, secondary, wraps). Layout=Centered: 56 icon circle, badge, Title (Title/3), Body, all centred (11-03).
> Actions=None · One = exposed Button / Primary Large “primary”, full width · Two = exposed Button / Primary Small “primary” + Button / On Card Small “secondary”, both FILL, gap 8. Set the button labels on the nested buttons (Label#9:0 / Label#9:42).
> Show title off hides the whole title line (and the badge with it in Layout=Leading), so the body sits level with the icon.
> All gray and black: never red.
> SwiftUI: PBNoticeCard"
- Leading / None (used here): 362 wide, hugs height, fill `bg/card`, radius 20, padding 16, V gap 16. `content` H gap 12: `icon` = 40 white circle (`bg/primary`) with a 20 icon `icon/primary` centred (10, 10); `text` fills, V gap 2: `title line` (**Headline** `text/primary`) + `body` (**Subheadline** `text/secondary`, wraps).
- Heights: with title + 1-line body = 76; Show title off with a 3-line body = 92.

**E. Header / Amount Hero (set 128:2004). SwiftUI `PBAmountHero`.** Variants `Leading` = Icon | Avatar | Pair; props `Title`, `Amount`, `Meta`, `Show chips`, `Show chip 2`, `Show chip 3`.
> "PBAmountHero — the left-aligned hero of an expense, payment or loan detail (06-10/12/14/15/16 · 08-06 · 09-03/05/06). A 56 leading, then Title (Title/2, wraps), Amount (Title/1), Meta (Footnote, secondary) and up to 3 chips.
> Leading=Icon: exposed Avatar / Circle 56 Type=Icon (the category icon: Food, Bed…). Leading=Avatar: exposed Avatar / Circle 56 Art (the other person on a loan). Leading=Pair: exposed Avatar / Pair 56 (payer → receiver).
> Show chips + exposed Badge / Pill “chip 1” (Muted, group), “chip 2” (Muted, category; Show chip 2) and “chip 3” (Inverse, status such as “Disputed” or “Paid back”; Show chip 3). Status chips are black or gray, never red.
> SwiftUI: PBAmountHero"
- 362 wide, V gap 12: leading (56 tall) then `text` V gap 2: `title` **Title/2** `text/primary` (wraps), `amount` **Title/1** `text/primary`, `meta` **Footnote** `text/secondary`. Chips row hidden on this page (Show chips = false) → 158 tall.

**F. Avatar / Pair (set 116:1005). SwiftUI `PBAvatarPair`.** Variants Size=32 (88 × 32) | 56 (148 × 56).
> "PBAvatarPair — “From → To” pair for transfers and payments: two nested Avatar / Circle (exposed as “from” and “to”: set Art / Type / Initials on each) with Icon / Arrow Right in icon/tertiary between them.
> Size=32 (16 arrow, gap 4) for Row / Transfer · Size=56 (20 arrow, gap 8) for Header / Amount Hero Leading=Pair (06-12, 08-06). Default: Arjun → Kabir.
> SwiftUI: PBAvatarPair"
- Size 56: H gap 8, centred: `from` 56 circle (`bg/card` fill on white) · `arrow` = `arrow-right.svg` 20, `icon/tertiary` · `to` 56 circle.

**G. Control / Payment Parties (125:1085). SwiftUI `PBPaymentParties`.**
> "PBPaymentParties — who paid whom on Record payment (06-11, 08-04): a #F5F5F5 r20 card, 96 tall, with a From tile, Icon / Arrow Right (icon/tertiary) and a To tile. Each tile is an exposed Avatar / Circle 56 (“from avatar” / “to avatar”, white circle on the card) with a Footnote label and a Headline name, and opens a person picker. Default: You (Arjun) → Meera.
> SwiftUI: PBPaymentParties"
- 362 × 96, fill `bg/card`, radius 20, horizontal, padding 20 top/bottom, 16 left/right, gap 8, centred. Props `From label` "From", `From name`, `To label` "To", `To name`.
- Each tile (fills, 147 wide) H gap 12: avatar 56 white (`bg/primary`) + text V gap 0: `label` **Footnote** `text/tertiary` (18) and `name` **Headline** `text/primary` (22, 1 line).
- `arrow` = `arrow-right.svg` 20 between the tiles, `icon/tertiary`.
- Tapping a tile opens a person picker (not drawn on this page; see the Record payment form spec on page 06).

**H. Control / Amount Display (set 125:1084). SwiftUI `PBAmountField`.** Variants `State` = Empty | Focused | Filled; props `Amount`, `Helper`, `Show helper`, `Show date chip`.
> "PBAmountField — the amount-first entry at the top of Add expense, Record payment, Lend money and the receipt review (06-01/02/11/13 · 08-04 · 11-10 · 11-13).
> Two nested Control / Category Chip instances (exposed): “currency” (INR, opens the currency sheet) and “date” (Today, opens the Date sheet). Show date chip: on for 06-01/02 and 11-10; off for 06-11, 06-13, 08-04 and 11-13.
> The amount uses Amount/Display (Manrope ExtraBold 56/64). Empty = the “₹0” placeholder in text/tertiary with the 2×56 caret (not bound to Amount); Focused = the typed Amount + caret; Filled = Amount, no caret. Helper (Footnote, secondary) e.g. “You owe Meera ₹450 in Flat 302”.
> Amount entry always uses the kit keyboard (Number Pad: the kit has no Decimal Pad), never a custom keypad.
> SwiftUI: PBAmountField"
- Filled (used here): 362 × 138, V gap 12, padding 4 top/bottom, centred. `chips` row (H gap 8, centred): currency chip. `value` (V, centred): `amount` **Amount/Display** `text/primary`, centred; `helper` **Footnote** `text/secondary`, centred.
- Keyboard: the kit shows the Number Pad because Figma has no decimal pad; on device use the **decimal** pad (iOS `.decimalPad`, Android `KeyboardType.Decimal`), matching screens-add-expense.md.

**I. Control / Category Chip (set 64:4185). SwiftUI `PBCategoryChip`.** Variants `Selected` × `Leading` = None | Icon | Avatar; props `Label`, `Icon`, `Show remove`.
> "Filter and people chip, 36 tall. Selected = inverse fill. Leading=None: text only (avatar categories, currency, date). Leading=Icon: 16 icon swap (the “Add” chip uses Plus). Leading=Avatar: exposed Avatar / Circle 24 for people pickers (Selected=False gives it a white avatar circle so the face reads on the #F5F5F5 chip). Show remove adds a trailing Close 16. SwiftUI: PBCategoryChip"
- 36 tall, hugs, capsule, padding 0/16, gap 6, label **Button/Small**. Not selected: fill `bg/card`, label `text/primary`. Selected: fill `bg/inverse`, label `text/inverse`. Used here as the currency chip ("INR") and the five method chips.

**J. Card / Payment Preview (37:675), extended. SwiftUI `PBPaymentPreview`.** Already specced for Setup 3 (screens-setup.md §3). New props:
> "Shows how your UPI ID appears to friends. Show copy: Button / On Card Small “Copy” with the Copy icon (exposed), for 06-11 and 08-04. Show copy icon: the original plain copy icon button (default on; turn it off when Show copy is on). SwiftUI: PBPaymentPreview"
- Here: 362 × 76, fill `bg/card`, radius 20, padding 16. Row (H gap 12, centred): avatar 40 white + text (V gap 2: `name` **Headline**, `upi` **Subheadline** `text/secondary`) + `copy button` = `Button / On Card` Small with leading `copy.svg` 16: 92 × 36, label "Copy".

**K. Row / Setting (set 97:996). SwiftUI `PBSettingRow`.** Variants `Trailing` = Chevron | Toggle | Stepper | Check | Unchecked | None × `Tone` = Default | Destructive; props `Title`, `Value`, `Show value`, `Icon`, `Show icon`, `Subtitle`, `Show subtitle`, `Show badge`, `Show divider`.
> "Settings list row for #F5F5F5 card groups, 56 tall (grows with a subtitle). Trailing: Chevron (push), Toggle (exposed kit switch; On track tinted color/bg/inverse), Stepper (exposed kit stepper), Check (black tick, single select), Unchecked (empty 24 slot), None. Tone=Destructive: text/destructive + icon/destructive, never a chevron. Optional icon (Show icon off moves the title and the divider inset to 16), value, subtitle, and an exposed Badge / Pill (Inverse “Pro” / “Try free”). Turn Show value off on Toggle, Stepper, Check and Unchecked rows. SwiftUI: PBSettingRow"
- 362 wide, min height 56, horizontal, padding 12/16, gap 12, centred. No fill of its own (the parent card is `bg/card` r20, clips).
- `icon` 24 `icon/primary` (Destructive: `icon/destructive`) · `title` **Headline** `text/primary` (Destructive: `text/destructive`), fills, 1 line · `value` **Body** `text/secondary`, hugs · `chevron` `chevron-right.svg` 20 `icon/tertiary`.
- Divider (Show divider): absolute 1 pt `border/subtle` at the bottom, starting at the title's x (x 72 with an icon: 16 + 24 + 12 + … → line from frame x 72; without an icon: from x 36) to the right edge of the card.

**L. Overlay / Toast (118:965). SwiftUI `PBToast`.**
> "PBToast — Short confirmation after a save (“Expense added”, “Payment recorded”, “Loan added”, “Group created”, “Payment confirmed”, “UPI ID copied”). A bg/inverse capsule, 44 tall, hugging its Label (Button/Small, text/inverse) with an optional 20 icon (Show icon; Icon swap, default Check Circle, icon/inverse). No shadow.
> Place it absolute and centred: 50 above the bottom edge, or 16 above the tab bar on tab screens. It fades after 2 s and is never a link. Ask Paybak does not use it (the draft card shows the save)."
- 44 tall, capsule, fill `bg/inverse`, horizontal, padding left 16 / right 20, gap 8, centred: `check-circle.svg` 20 `icon/inverse` + label **Button/Small** `text/inverse`. Widths on this page: "Payment recorded" 195, "Payment confirmed" 203.
- Position: centred horizontally; **y = 874 − 50 − 44 = 780** on non-tab screens (bottom edge 50 above the screen bottom); **y = 791 − 16 − 44 = 731** on tab screens (16 above the tab bar's top).
- Motion (proposal, not in Figma): fade + 8 pt rise in 200 ms, hold 2 s, fade out 200 ms. Not tappable; announce it for accessibility.

**M. Control / Text Area (set 115:9936). SwiftUI `PBTextArea`.** Variants `State` = Default | Focused; props `Label`, `Show label`, `Value`, `Helper`, `Show helper`.
> "PBTextArea — Multi-line text input (TextEditor) for the Remind message (08-07) and the Not received note (08-10). Control / Input Field styling: #F5F5F5, radius 14, padding 16, Body text that wraps (3–4 lines; min height 104, hugs taller).
> State=Default · Focused (1.5 border/strong ring + caret). The “caret” layer is absolute: after overriding Value on a Focused instance, move it to the end of the last line. Label (Subheadline, secondary) and Helper (Footnote, tertiary) are optional.
> SwiftUI: PBTextArea"
- 354 wide in a sheet, V gap 8: `label` **Subheadline** `text/secondary` (20) · `field` fill `bg/card`, radius 14 (`radius/input`), padding 16, **min height 104**, hugs taller: `value` **Body** `text/primary`, wraps · `helper` **Footnote** `text/tertiary`. Height with 3 value lines = 158. Focused: 1.5 pt inside ring `border/strong` (draw as an overlay, README §3 rule 6), caret `text/primary`.

**N. Card / Confirm Payment (set 129:2060). SwiftUI `PBConfirmPaymentCard`.** Variants `State` = Pending (362 × 120) | Confirmed (362 × 72); props `Title`, `Detail`, `Confirmed title`, `Confirmed detail`.
> "PBConfirmPaymentCard — The receiver’s confirm card at the top of Home and in Activity (04-06 · 08-10 under the scrim · 09-01 · 09-08), #F5F5F5 r20, padding 16, gap 12.
> State=Pending (362×120): exposed Avatar / Circle 40 (the payer, white circle), Title (Headline) “Esha says she paid you ₹700”, Detail (Footnote) “Dinner at Olive Garden · UPI · 9:12 pm”, then two exposed buttons, both FILL, gap 8: Button / Primary Small “Confirm” (layer “confirm”) and Button / On Card Small “Not received” (layer “not received”).
> Button labels: set Label#9:0 on “confirm” and Label#9:42 on “not received” (they show in this card’s property panel as nested instances). Figma can’t bind a card-level text property to text inside a nested button, so there is no separate Confirm label / Secondary label property.
> State=Confirmed (≈72): Confirmed title “Esha paid you ₹700”, Confirmed detail “… · Confirmed”, Icon / Check Circle, no buttons. The Confirmed texts are separate properties so they survive the variant change.
> Interactive: Confirm → Change to State=Confirmed (on click, smart animate, ease out, 250 ms). A frame can override the Confirm button with its own link (04-06 → 08-11); Not received opens the 08-10 sheet on the frame.
> SwiftUI: PBConfirmPaymentCard"
- Pending geometry: fill `bg/card`, radius 20, padding 16, V gap 12. `top` (H gap 12, centred): avatar 40 white (`bg/primary`) + text (V gap 0): `title` **Headline** `text/primary` 1 line truncate, `detail` **Footnote** `text/secondary` 1 line truncate. `actions` (H gap 8): `confirm` = Button / Primary Small, FILL (161 × 36); `not received` = Button / On Card Small, FILL (161 × 36).

**O. Local sheet contents.** `_Sheet / Remind Rohan` (176:20970, SwiftUI `PBRemindSheet`) and `_Sheet / Not received` (177:24988, SwiftUI `PBNotReceivedSheet`) are specced with their screens (§6 and §8). Both sit in the Content slot of `Sheet / Container` Detent=Medium (components-home §15).

---

## 1. `settleOwedBreakdown`: You’re owed — Breakdown (167:11705, 08-01)

**Purpose:** the screen behind Home's "You’re owed" balance card: every person who owes you, overdue first. **Container:** pushed screen (from Home). **Ref:** `ref/settleOwedBreakdown.png`.

### 1.1 Layout (frame auto-layout: vertical, padding 62/20/34/20, gap 24, `bg/primary`, clips)
| # | Element | Rect (x, y, w, h) | Component / style | Content |
|---|---|---|---|---|
| 1 | Push header | 20, 62, 362, 44 | Navigation / Push Header, Trailing=None | Back (glass, chevron-left) + title "You’re owed" (Headline, centred) |
| 2 | Hero | 20, 130, 362, 66 | frame V gap 4 | `amount` "+₹2,900" **Title/1** `text/primary` (127 × 38) · `count` "from 4 people" **Body** `text/secondary` (y 172) |
| 3 | Who owes you | 20, 220, 362, 296 | frame V gap 8 | Section header + People card |
| 3a | Section header | 20, 220, 362, 32 | Row / Section Header, Show action=false | "Who owes you" (Title/3) |
| 3b | People | 20, 260, 362, 256 | frame V gap 0, fill `bg/card`, radius 20, clips | 4 × Row / Person Regular, Trailing=Owed (64 each) |
| 4 | Settle up | 20, 788, 362, 52 | Button / Primary Large, absolute (bottom = 840, the bottom safe-area edge) | "Settle up" |

People rows (y = 260, 324, 388, 452; avatar 40 white at x 36; name at x 88):
| Row | Avatar (asset) | Name | Subtitle | Trailing amount | Under the amount | Divider |
|---|---|---|---|---|---|---|
| Rohan | Rohan `avatars/avatar-3` | Rohan | Movie tickets | +₹800 | Badge / Pill **Overdue** "Overdue 3 days" (110 × 24 at 256, 292) | yes |
| Priya | Priya `avatar-2` | Priya | Dinner at Olive Garden | +₹700 | amount label "Due Sun 4 Oct" (Footnote, tertiary) | yes |
| Esha | Esha `avatar-4` | Esha | Dinner at Olive Garden | +₹700 | "Due Sun 4 Oct" | yes |
| Dev | Dev `avatar-5` | Dev | Dinner at Olive Garden | +₹700 | "Due Sun 4 Oct" | no (last) |

### 1.2 Behaviour and data
- Hero amount = the sum of what every friend owes you (net per person, positive only), formatted "+₹2,900"; count = number of people with a positive net, "from 4 people" ("from 1 person" when singular). These are the same numbers as the Home "You’re owed" card and must come from the same calculation.
- Sorting: **overdue first** (most overdue first), then by due date ascending, then keep a stable order (Figma: Priya, Esha, Dev, the order of the Dinner split). Rows without a due date go last.
- Row subtitle = what the debt is for: the expense title for a single non-group debt ("Movie tickets", "Dinner at Olive Garden"), else the group name. If a person owes you for several things, proposal: the group or expense with the earliest due date, plus " +1" style is NOT designed; use "{n} expenses" (proposal).
- Under the amount: the red Overdue badge if any part is overdue ("Overdue {n} days", "Overdue 1 day"), else "Due {Ddd d Mmm}" (e.g. "Due Sun 4 Oct"), else nothing.
- Scroll: Figma doesn't scroll (content fits). Implement a vertical scroll with the Settle up button pinned 0 pt above the bottom safe-area edge (y 788–840) and a bottom content inset of 52 + 24 so the last row can clear it (proposal).
- Empty (nobody owes you): not designed. Proposal: Home's card shows ₹0 and still opens this screen; show the hero "₹0" in `text/tertiary`, "Nobody owes you right now" as the count, and hide the section.

### 1.3 Navigation
| Element | Action |
|---|---|
| Back (and Android system back / iOS edge swipe) | Pop to Home (reaction: back hotspot → Home — Active). |
| A person row | Push that friend's page (reaction on Rohan: → "Friend — Rohan" 177:25988 on page 07, `screens-groups.md`). "In the app every row opens that friend’s page; only Rohan’s (07-08) is drawn." |
| Settle up | Push `settleUp` (PUSH from the right, 350 ms ease-in-out). |

### 1.4 Designer note (verbatim)
> "Tapping “You’re owed” on Home lists every person who owes you, overdue first. Only Rohan’s badge is red. In the app every row opens that friend’s page; only Rohan’s (07-08) is drawn."

### 1.5 Reuse map
Push header → NEW §0.4-A · Back → `Button / Icon` Glass (components-core §2.3) · Section header → components-home §8 · People card → a `bg/card` r20 container · rows → NEW Row / Person §0.4-C · avatar → `Avatar / Circle` 40 Art (components-core §3.2, white fill on the card) · Overdue badge → `Badge / Pill` Overdue (components-core §3.1) · Divider → `Divider / Line` (components-core §4.5) · Settle up → `Button / Primary` Large (components-core §2.1).

---

## 2. `settleOweBreakdown`: You owe — Breakdown (167:11957, 08-02)

Same screen template as §1 with the "owe" direction. **Ref:** `ref/settleOweBreakdown.png`.

### 2.1 Differences from §1
| Element | Rect | Content |
|---|---|---|
| Push header title | — | "You owe" |
| Hero amount | 20, 130, 118 × 38 | "−₹1,850" **Title/1** in **`text/secondary`** (gray, money you owe) |
| Hero count | 20, 172 | "across 2 groups" **Body** `text/secondary` |
| Section | 20, 220, 362, 194 | header "Who you owe"; People card 20, 260, 362 × 128; then `Footnote` text at 20, 396, 362 × 18 |
| Rows | — | Row / Person Regular, **Trailing=Owe** ("−" + amount, both `text/secondary`) |
| Footnote | 20, 396 | "Goa Trip uses simplified debts, so you pay Kabir directly." **Footnote** `text/secondary` |
| Settle up | 20, 788, 362 × 52 | same button → `settleUp` |

| Row | Avatar | Name | Subtitle | Amount | Amount label | Divider |
|---|---|---|---|---|---|---|
| Kabir | Kabir `avatars/avatar-6` | Kabir | Goa Trip | −₹1,400 | Due Fri 2 Oct | yes |
| Meera | Meera `avatars/avatar-7` | Meera | Flat 302 | −₹450 | Due Mon 5 Oct | no |

### 2.2 Behaviour
- Hero = the sum of what you owe (net per person, negative only), "−₹1,850"; the caption counts **groups** ("across 2 groups"), matching the Home "You owe" card. Proposal for other cases: "across 1 group", "to 1 person" when none of the debts is in a group, "across 2 groups and 1 friend" when mixed (not designed; keep the Home card and this hero identical).
- Subtitle = the group name (or the expense title for non-group debts).
- The footnote appears when at least one listed payment comes from a group with simplified debts **and** the payee isn't the person you originally owed on each expense. Template (proposal, generalised from the one sample): "{Group} uses simplified debts, so you pay {Name} directly." One line per such group.
- Nothing is red here (nothing is overdue). If a debt you owe becomes overdue, the same red Overdue badge replaces the amount label (Row / Person supports it).

### 2.3 Navigation: Back → Home · a row → that friend's page (not wired in Figma; same rule as §1) · Settle up → `settleUp` (push, 350 ms ease-in-out).

### 2.4 Designer note (verbatim)
> "The hero amount is gray because it’s money you owe. Nothing is red, because nothing is overdue. The footnote explains why you pay Kabir the whole ₹1,400 for Goa Trip."

### 2.5 Reuse map: as §1.5 (Row / Person Trailing=Owe).

---

## 3. `settleUp`: Settle up (167:12113, 08-03)

**Purpose:** the fewest-payments plan: payments you make and people who pay you, each with its action. **Container:** pushed screen (from Home's "Settle up" button and from both breakdowns). **Scrolls vertically** (Figma `overflowDirection` VERTICAL) under a **fixed white header**. **Ref:** `ref/settleUp.png`.

### 3.1 The plan: which payments and why
Demo data on Wed 30 Sep 2026 (the numbers must come out of the real calculation on the demo dataset):

| Who | Direction | Amount | For | Due | Why this amount |
|---|---|---|---|---|---|
| Kabir | you pay | ₹1,400 | Goa Trip | Fri 2 Oct ("Due Fri") | Your net in Goa Trip is −₹1,400. Goa Trip has **simplify debts** on, so the group's debts are re-routed into the fewest transfers and yours becomes one payment straight to Kabir, "the whole ₹1,400". |
| Meera | you pay | ₹450 | Flat 302 | Mon 5 Oct ("Due Mon") | Your share of the Flat 302 electricity bill (Home: "Electricity bill · Flat 302 · You owe −₹450"). |
| Rohan | pays you | ₹800 | Movie tickets | overdue 3 days | Non-group expense (movie tickets on 20 Sep) you paid for. |
| Priya | pays you | ₹700 | Dinner at Olive Garden | Sun 4 Oct ("Due Sun") | Dinner ₹2,800 split equally by 4 (you, Priya, Esha, Dev) = ₹700 each; you paid. |
| Esha | pays you | ₹700 | Dinner at Olive Garden | Sun 4 Oct | same |
| Dev | pays you | ₹700 | Dinner at Olive Garden | Sun 4 Oct | same |

Totals: you pay 2 payments = ₹1,850 (= Home "−₹1,850 across 2 groups"); 4 people pay you ₹2,900 (= Home "+₹2,900 from 4 people").

**Algorithm (what the implementation must do; it reproduces the table above):**
1. For each **group with simplify debts on**: compute each member's net balance inside the group (paid − share, minus confirmed payments), then produce the minimum set of transfers with the standard greedy match (repeatedly settle the largest debtor against the largest creditor; ties broken by a stable member order). Keep only the transfers that involve the current user. Goa Trip gives exactly one: you → Kabir ₹1,400.
2. For **groups without simplify debts** and **non-group expenses**: pairwise debts (each participant owes the payer their share).
3. **Net per person** across everything: add up the user's transfers with each friend (you→X positive, X→you negative) into one net amount per friend. A friend with net 0 disappears. Pending (unconfirmed) payments are **not** subtracted (rule 0.3-2).
4. Split into "payments to make" (net < 0) and "people who owe you" (net > 0). Paybak doesn't route money between third parties across unrelated groups/friends (e.g. it never suggests "Priya pays Kabir"): the plan only contains the user's own payments. That is what "Everyone ends up in the same place" means.
5. Each row's context ("Goa Trip", "Flat 302", "Movie tickets", "Dinner at Olive Garden") and due date come from the underlying debt (the group, or the expense). With several debts to one person: the earliest due date wins, and the context is the group/expense with that date (proposal).
6. Order: payments to make by due date ascending (Kabir Fri, Meera Mon); people who owe you **overdue first**, then due date ascending, then stable order (Rohan, Priya, Esha, Dev).

### 3.2 Layout (frame: vertical auto-layout, padding 62/20/34/20, gap 24; content height 960 → scrolls 86 pt)
Fixed children (don't scroll): "Header background (fixed)" white rect 0, 0, 402 × 106 (`bg/primary`) + the Push header at 20, 62 (Trailing=None, title "Settle up"). In the flow, "Header space" 20, 62, 362 × 44 reserves the header's space.

| # | Element | Rect | Component | Content |
|---|---|---|---|---|
| 1 | Notice | 20, 130, 362, 92 | Card / Notice, Layout=Leading, Actions=None, **Show title=false** | icon circle 40 white at (36, 146) with `shuffle.svg` 20 at (46, 156); body at (88, 146) 278 × 60, Subheadline `text/secondary`, 3 lines: "Paybak simplifies balances into the fewest payments. Everyone ends up in the same place." |
| 2 | Payments to make | 20, 246, 362, 228 | frame V gap 8 | Section header "2 payments to make" (Title/3, no action) at y 246; Rows frame at y 286, **V gap 12** |
| 2a | Kabir | 20, 286, 362, 88 | Row / Attention, State=Due soon, with an **Art** avatar | avatar Kabir (white circle) at (36, 310); title "Kabir" + detail "Goa Trip" at y 304; badge **On Card** "Due Fri" (61 × 24 at 88, 332); amount "₹1,400" (Amount/Medium `text/primary`, right-aligned, y 298); button `Button / On Card` Small "Settle" 76 × 36 at (290, 326) |
| 2b | Meera | 20, 386, 362, 88 | same | avatar Meera; "Meera" + "Flat 302"; badge "Due Mon" (72 × 24); "₹450"; "Settle" at (290, 426) |
| 3 | People who owe you | 20, 498, 362, 428 | frame V gap 8 | Section header "4 people owe you" at y 498; Rows at y 538, V gap 12 |
| 3a | Rohan | 20, 538, 362, 88 | Row / Attention, State=**Overdue** | avatar Rohan; "Rohan" + "Movie tickets"; badge **Overdue** "Overdue 3 days" (110 × 24 at 88, 584); "₹800"; button "Remind" 86 × 36 at (280, 578) |
| 3b | Priya | 20, 638, 362, 88 | Row / Attention, State=Due soon (Art) | "Priya" + "Dinner at Olive Garden" (truncates to "Dinner at Olive Gar…" in the 180-wide text column); badge On Card "Due Sun" (70 × 24); "₹700"; "Remind" 86 × 36 at (280, 678) |
| 3c | Esha | 20, 738, 362, 88 | same | "Esha" + "Dinner at Olive Garden"; "Due Sun"; "₹700"; "Remind" at (280, 778) |
| 3d | Dev | 20, 838, 362, 88 | same | "Dev" + "Dinner at Olive Garden"; "Due Sun"; "₹700"; "Remind" at (280, 878) (below the fold) |

- Amounts on this screen have **no sign** and are all `text/primary` (Row / Attention's style); the section tells the direction.
- Section-header copy: "{n} payments to make" / "1 payment to make"; "{n} people owe you" / "1 person owes you" (singulars are proposals).
- Badge label rule (Row / Attention): "Due {Ddd}" (weekday short: "Due Fri", "Due Mon", "Due Sun") when the due date is within the next 6 days; proposal for later dates: "Due {d Mmm}" ("Due 12 Oct"); overdue: red "Overdue {n} days".
- Scroll: content scrolls under the fixed white header (402 × 106 white; no blur, no divider in Figma). Bottom inset 34.
- Empty sections: hide a section that has no rows. Both empty = all settled; proposal: show the Home "You’re all square." empty-state card (Card / Empty State, All settled, `paybak-home-allset.riv`) under the notice.

### 3.3 Navigation (reactions)
| Element | Destination |
|---|---|
| Back | Pop (Figma back hotspot → Home — Active; in the app pop to wherever it came from: Home or a breakdown). |
| Kabir "Settle" | `settleRecordKabir`: Record payment prefilled for Kabir, **full-screen modal** sliding up (MOVE_IN from the bottom, 300 ms ease-out). |
| Meera "Settle" | The same Record payment modal prefilled for Meera (Figma links it to "Record payment — form" 177:29750 on page 06, which is that form with Meera's data; `screens-record-lend-group.md`). |
| Rohan "Remind" | The Remind sheet as an **overlay over this list** (→ "Remind sheet (overlay)" 177:21801, MOVE_IN from the bottom, 300 ms ease-out). Same sheet as §6. |
| Priya / Esha / Dev "Remind" | The same Remind sheet for that person (not wired in Figma). |
| A row body (outside the button) | Not wired. Proposal: open that friend's page. |

### 3.4 Designer note (verbatim)
> "This is the fewest-payments plan. You make 2 payments and 4 people pay you, with Goa Trip simplified so that you pay Kabir directly. Settle opens Record payment prefilled, and Remind opens the reminder sheet over this list."

### 3.5 States not designed (proposals)
- A payment you recorded that's still pending: keep the row, replace the due badge with an On Card badge "Pending" and hide "Settle"; tapping the row opens its Payment detail (§5). It leaves the list once confirmed.
- A person who's already been reminded today: keep "Remind" enabled (reminding again is allowed; the notes don't limit it).

### 3.6 Reuse map
Push header → NEW §0.4-A · Notice → NEW Card / Notice §0.4-D (icon circle = `Avatar / Circle` 40 Type=Icon On Card, Icon=Shuffle) · Section headers → components-home §8 · rows → `Row / Attention` (components-home §9) with two overrides: Due-soon rows use **Type=Art** avatars (people) instead of the Groups icon, and the Settle/Remind buttons as designed · badges → `Badge / Pill` On Card / Overdue · buttons → `Button / On Card` Small.

---

## 4. `settleRecordKabir`: Record payment — Kabir (167:13635, 08-04)

**Purpose:** record a payment you made, prefilled from the suggestion. **Container:** full-screen modal (slides up from Settle up). Same form as page 06 "Record payment — form" (06-11) with Kabir's data. **Ref:** `ref/settleRecordKabir.png`. No scroll in Figma (content fits, bottom 792).

### 4.1 Layout (frame: vertical, padding 62/20/34/20, gap 16; `Form` V gap 24 from y 122)
| # | Element | Rect | Component | Content / style |
|---|---|---|---|---|
| 1 | Modal header | 20, 62, 362, 44 | Navigation / Modal Header, Action=Enabled | close (kit glass xmark) 44 × 44 at (20, 62); title "Record payment" (Headline, centred, 200 × 22 at 101, 73); `Save` = Button / Primary Small 67 × 36 at (315, 66) |
| 2 | Payment parties | 20, 122, 362, 96 | Control / Payment Parties | From: avatar Arjun (`avatar-1`, the user's avatar) 56 at (36, 142), label "From" (Footnote tertiary, 104, 150), name "You" (Headline, 104, 168) · arrow `arrow-right.svg` 20 at (191, 160) `icon/tertiary` · To: avatar Kabir 56 at (219, 142), "To" / "Kabir" at x 287 |
| 3 | Amount display | 20, 238, 362, 138 | Control / Amount Display, State=Filled, Show date chip=false | currency chip "INR" (Category Chip, not selected, 57 × 36 at 172.5, 242) · amount "₹1,400" **Amount/Display** `text/primary`, centred (181 × 64 at 110.5, 290) · helper "You owe Kabir ₹1,400 in Goa Trip" Footnote `text/secondary`, centred (104.5, 354) |
| 4 | Method label | 20, 400, 52 × 20 | text | "Method" **Subheadline** `text/secondary` |
| 5 | Method chips | 20, 424, 362, 36 | H gap 6 | Category Chips (Leading=None): "Cash" 69 (x 20) · **"UPI" 57 (x 95) Selected** (`bg/inverse`, `text/inverse`) · "Bank" 67 (x 158) · "Card" 67 (x 231) · "Other" 74 (x 304) |
| 6 | Payment preview | 20, 476, 362, 76 | Card / Payment Preview, Show copy=true, Show copy icon=false | avatar Kabir 40 white (36, 494); name "Kabir Singh" (Headline, 88, 492); upi "kabir@okaxis" (Subheadline secondary, 88, 516); "Copy" button (On Card Small + copy icon 16) 92 × 36 at (274, 496) |
| 7 | Details rows | 20, 576, 362, 168 | `bg/card` r20 card, 3 × Row / Setting Trailing=Chevron | For: `groups.svg` 24, "For", value "Goa Trip", chevron · Date: `calendar.svg`, "Date", "Wed 30 Sep", chevron · Proof: `camera.svg`, "Proof", "Add photo (optional)", chevron (no divider) |
| 8 | Summary | 20, 756, 362, 36 | text | **Footnote** `text/secondary`, 2 lines: "You paid Kabir ₹1,400 by UPI for Goa Trip." / "Kabir will be asked to confirm. Paybak never moves money." |

### 4.2 Behaviour
- **Prefill from the suggestion:** From = You, To = the payee, Amount = the suggested amount, currency = the group/debt currency (INR), For = the group or expense ("Goa Trip"), Date = today ("Wed 30 Sep", format `EEE d MMM`), Proof empty. Method: **UPI is preselected** when the payee has a UPI ID (Kabir does); proposal: otherwise Cash.
- Helper template: "You owe {Name} {amount} in {For}" (from the component default "You owe Meera ₹450 in Flat 302"). It updates if the amount is edited (proposal: when the amount differs, "Paying {amount} of {owed}"; overpayment is covered by the page-06 rules in `screens-record-lend-group.md`).
- **Method chips:** single select. The payment preview shows only for methods with an ID the payee has shared: UPI → the UPI ID; Bank → the bank details (proposal); hidden for Cash, Card, Other. "“Paid to” appears only for methods with an ID (UPI, Bank)" (note on 08-06).
- **Copy** puts the UPI ID ("kabir@okaxis") on the clipboard and shows the toast **"UPI ID copied"** (Check Circle), centred 50 above the bottom (y 780), fading after 2 s.
- Amount: tap to edit with the decimal pad; the currency chip opens the currency sheet; For / Date / Proof rows open the page-06 pickers (group picker, Date sheet titled "Date", photo picker for proof). These pickers are specced in `screens-add-expense.md` / `screens-record-lend-group.md`.
- Summary template: "You paid {Name} {amount} by {Method} for {For}.\n{Name} will be asked to confirm. Paybak never moves money." (Method as the chip label: "UPI", "Cash", "Bank", "Card", "Other".)
- **Save** is enabled when there's an amount > 0 and a payee (Modal Header Action=Disabled otherwise). Save creates a payment with status **pending**, then goes to `settlePaymentPending` (DISSOLVE 200 ms ease-out) with the toast "Payment recorded". Balances don't change yet.
- Keyboard: not shown in Figma. When the amount is focused the decimal pad covers the lower form; nothing needs to ride above it (proposal).

### 4.3 Navigation
| Element | Action |
|---|---|
| Close (xmark) | Dismiss the modal (reaction BACK) without saving. |
| Save | → `settlePaymentPending` (dissolve 200 ms), toast "Payment recorded". |
| From / To tiles | Person picker (page 06). |
| INR chip | Currency sheet (page 06). |
| Copy | Clipboard + toast "UPI ID copied". |
| For / Date / Proof | Group picker / Date sheet / photo picker (page 06). |

### 4.4 Designer note (verbatim)
> "Everything is prefilled from the suggestion. With UPI selected, Kabir’s UPI ID shows with Copy. Copy puts kabir@okaxis on the clipboard and shows Overlay / Toast “UPI ID copied” (Check Circle), centred 50 above the bottom, which fades after 2 s. Paybak never moves money: you pay in your own UPI app and save the record here."

### 4.5 Reuse map
Modal header → NEW §0.4-B · Payment parties → NEW §0.4-G · Amount display → NEW §0.4-H · chips → NEW Category Chip §0.4-I · Payment preview → `Card / Payment Preview` (screens-setup.md §3) with Show copy §0.4-J · Copy button → `Button / On Card` Small with leading icon (components-core §2.1) · Details rows → NEW Row / Setting §0.4-K · toast → NEW Overlay / Toast §0.4-L.

---

## 5. `settlePaymentPending`: Payment pending (167:17304, 08-06)

**Purpose:** the payment detail the payer sees after saving, until the receiver confirms. **Container:** pushed screen (it replaces the Record payment modal; Back returns to Settle up). **Scrolls vertically** under a fixed white header (Figma: `overflowDirection` VERTICAL, 7 fixed children). **Ref:** `ref/settlePaymentPending.png`.

### 5.1 Layout (frame: vertical, padding 62/20/34/20, gap 24; content bottom 910 + 34 → scrolls 70 pt)
Fixed: "Header background (fixed)" 0, 0, 402 × 106 `bg/primary`; Push header at 20, 62, **Trailing=Text**: back (glass) + title "Payment" + glass capsule action "Edit" (64 × 44 at 318, 62, label Headline); the toast.

| # | Element | Rect | Component | Content |
|---|---|---|---|---|
| 1 | Amount hero | 20, 130, 362, 158 | Header / Amount Hero, Leading=Pair, Show chips=false | pair: from Arjun 56 (`bg/card` circle) at (20, 130), `arrow-right.svg` 20 `icon/tertiary` at (84, 148), to Kabir 56 at (112, 130) · title "You paid Kabir" **Title/2** (20, 198) · amount "₹1,400" **Title/1** (20, 230) · meta "UPI · Today · Goa Trip" **Footnote** `text/secondary` (20, 270) |
| 2 | Status | 20, 312, 362, 76 | Card / Notice, Leading, Actions=None, Show title=true | icon circle 40 white with `activity.svg` (clock) 20 · title "Pending confirmation" (Headline) · body "Waiting for Kabir to confirm" (Subheadline secondary) |
| 3 | Details card | 20, 412, 362, 392 | `bg/card` r20 card, 7 × Row / Setting, **Trailing=None, Show icon=false** | rows (56 each, title Headline left, value Body `text/secondary` right, dividers inset 16): From / You · To / Kabir · Method / UPI · Paid to / kabir@okaxis · Date / Wed 30 Sep · For / Goa Trip · Proof / None (last, no divider) |
| 4 | Footnote | 20, 812, 362, 18 | text | "Your balance updates once Kabir confirms." **Footnote** `text/secondary` |
| 5 | Actions card | 20, 854, 362, 56 | `bg/card` r20 card, Row / Setting **Tone=Destructive**, Trailing=None | `delete.svg` 24 `icon/destructive` + "Cancel payment" Headline `text/destructive` (below the fold) |
| 6 | Toast | 104, 780, 195, 44 | Overlay / Toast | check-circle + "Payment recorded" (fixed, 50 above the bottom, fades after 2 s) |

### 5.2 Behaviour
- Meta template: "{Method} · {relative date} · {For}" ("Today", "Yesterday", else "d MMM").
- Rows: "Paid to" only for methods with an ID (UPI → the UPI ID; Bank → the account, proposal), so a Cash payment has 6 rows. "Proof": "None" or a thumbnail/“1 photo” (proposal; tapping opens the photo).
- Status card text: "Pending confirmation" / "Waiting for {Name} to confirm". When the receiver confirms (simulated from the debug menu, flow.md), proposal: the card becomes Title "Confirmed", body "{Name} confirmed on {date}", icon Check Circle; the Cancel row disappears; balances update. When the receiver taps Not received, proposal: title "Not received", body "{Name} says they haven’t received it", icon Flag, plus their note; the payment stays unconfirmed and can be edited or cancelled.
- **Edit** re-opens Record payment (modal, MOVE_IN from the bottom 300 ms) with the saved values; Save updates the same pending payment.
- **Cancel payment** (no reaction in Figma). Proposal: a destructive confirmation (iOS `confirmationDialog`, Android `AlertDialog`): title "Cancel this payment?", message "Kabir won’t be asked to confirm it.", buttons "Cancel payment" (destructive) and "Keep". On confirm, delete the pending payment and pop to Settle up.
- Home keeps −₹1,850 while pending (rule 0.3-2).

### 5.3 Navigation
| Element | Action |
|---|---|
| Back | → `settleUp` (reaction: PUSH from the left, 350 ms ease-in-out, i.e. a pop). |
| Edit | → Record payment prefilled with this payment (MOVE_IN from the bottom, 300 ms ease-out). |
| Cancel payment | Confirmation, then delete (proposal). |

### 5.4 Designer note (verbatim)
> "After Save you land on the payment with a “Payment recorded” toast. It stays “Pending confirmation”, and Home keeps −₹1,850, until Kabir confirms. “Paid to” appears only for methods with an ID (UPI, Bank), so the Cash payment on 06-12 has 6 rows."

### 5.5 Reuse map
Push header Trailing=Text → NEW §0.4-A · hero → NEW Header / Amount Hero §0.4-E with Avatar / Pair §0.4-F · status → NEW Card / Notice §0.4-D (Icon=Activity) · detail rows and Cancel → NEW Row / Setting §0.4-K · toast → NEW §0.4-L.

---

## 6. `settleRemind`: Remind — Rohan (177:21203, 08-07) and the Remind sheet

**Purpose:** send a polite pre-written reminder. **Container:** sheet over Home (Home — Active content under a 40 % scrim). The same sheet opens as an overlay over other screens (Settle up, friend page, chat, notifications): frame "Remind sheet (overlay)" (177:21801, 08-07s) is that overlay alone (scrim + sheet), used by Settle up's Remind (§3.3); its scrim, ✕ and Send in Paybak all CLOSE the overlay. **Refs:** `ref/settleRemind.png`, `ref/settleRemindOverlay.png`, `ref/settleRemindSheet.png` (the local component alone).

### 6.1 Background (under the scrim)
Identical to `homeActive` (screens-home.md §2): header "Good evening, Arjun", balance summary +₹2,900 / from 4 people and −₹1,850 / across 2 groups with Settle up, Due soon (Rohan ₹800 Overdue 3 days · Remind; Goa Trip ₹1,400 Due Fri · Settle), Recent activity (Dinner at Olive Garden ₹2,800 Today; Priya paid you ₹1,050 Yesterday; Electricity bill −₹450 26 Sep), the fade and the tab bar. Scrim: 0, 0, 402 × 874, `bg/scrim` (#0A0A0A @40 %).

### 6.2 The sheet: `Sheet / Container` Detent=Medium, title "Remind Rohan"
- Container 8, 286, 386 × 580 (bottom 8 above the screen bottom), fill `bg/primary`, radius 40, padding 8/16/28/16, gap 8 (components-home §15): grabber 60 × 4 at (171, 294); header at (24, 306) 354 × 50: title "Remind Rohan" **Title/3**, glass ✕ 50 × 50 at (328, 306).
- Content (`_Sheet / Remind Rohan`, 354 × 474) at (24, 364), **V gap 24**:

| # | Element | Rect (frame) | Component | Content |
|---|---|---|---|---|
| 1 | person card | 24, 364, 354, 64 | `bg/card` r20 card with Row / Person Regular, Trailing=Owed, Show status badge=true, no divider | avatar Rohan 40 white · "Rohan" / "Movie tickets" · "+₹800" + Badge Overdue "Overdue 3 days" |
| 2 | tone label | 24, 452, 33 × 20 | text | "Tone" **Subheadline** `text/secondary` |
| 3 | tone switch | 24, 480, 354, 36 | Control / Segmented, Options=2 (components-core §4.3) | "Friendly" (**selected**, black pill) · "Neutral"; segments (354 − 6) / 2 = 174 wide |
| 4 | message | 24, 540, 354, 158 | Control / Text Area, State=Default | label "Message"; field 354 × 104 at y 568 (`bg/card` r14, padding 16) with the message (Body, 3 lines); helper "You can edit this message." (Footnote tertiary, y 680) |
| 5 | Send in Paybak | 24, 722, 354, 52 | Button / Primary Large | "Send in Paybak" |
| 6 | Share… | 24, 786, 354, 52 | Button / **Secondary** Large (`bg/card`) | "Share…" |

### 6.3 Message templates (VERBATIM where Figma has them)
- **Friendly** (the designed, default tone; verbatim): "Hi Rohan! Just a gentle reminder about ₹800 for the movie tickets on 20 Sep. You can pay me on UPI at arjun@okaxis. Thanks."
  - Template derived from it: "Hi {first name}! Just a gentle reminder about {amount} for the {what} on {expense date d MMM}. You can pay me on UPI at {my UPI ID}. Thanks." where {what} is the expense title with its first letter lower-cased ("Movie tickets" → "movie tickets"). Drop the UPI sentence when the user has no UPI ID. For a group balance or several expenses, proposal: "…about {amount} for {Group}. …".
- **Neutral: NOT IN FIGMA.** The design only shows the Friendly text (the Segmented control has Friendly selected, and the Text Area's default Value is the Friendly message). No Neutral copy was found on page 08, in the component defaults, or in any other page dump the team has read so far. Proposal (needs product sign-off): "Hi {first name}, this is a reminder that {amount} for {what} ({date}) is still due. You can pay me on UPI at {my UPI ID}." → "Hi Rohan, this is a reminder that ₹800 for movie tickets (20 Sep) is still due. You can pay me on UPI at arjun@okaxis."
- Switching tone replaces the message with that tone's template **unless the user has edited it** (proposal: if edited, ask nothing and keep the edit; the helper stays "You can edit this message."). The field is a real multi-line editor.
- Default tone: Friendly (proposal: remember the last tone used; Settings may add a default tone, see `screens-settings.md`).

### 6.4 Send in Paybak vs Share…
- **Send in Paybak**: sends the message as a Paybak notification to the friend (simulated: there's no backend; record a "reminder sent" event on the debt/timeline for Activity), closes the sheet and shows the toast **"Reminder sent to Rohan"** for 2 s: **16 above the tab bar on Home** (y 731) and **50 above the bottom elsewhere** (y 780). Balances don't change.
- **Share…**: hands the same message (the current, possibly edited text) to the **system share sheet** (iOS `ShareLink`/`UIActivityViewController`, Android `Intent.ACTION_SEND` chooser), so it can go to WhatsApp, Messages, Mail or Copy. The Remind sheet stays open underneath (§7). Proposal: after a successful share, close the Remind sheet silently (no toast), because Paybak can't know it was sent.

### 6.5 Navigation (reactions)
| Element | Action |
|---|---|
| Scrim tap, ✕, swipe down | Close the sheet (Figma: scrim / ✕ → Home — Active). |
| Send in Paybak | Close + toast "Reminder sent to Rohan" (Figma → Home — Active). |
| Share… | System share sheet over the Remind sheet (Figma → `settleRemindShare`, smart animate 300 ms ease-out). |
| Opened from | Home Due soon "Remind" (Rohan row), Settle up "Remind" (overlay), friend page / chat / notifications (overlay copies on pages 07, 09, 11). Motion: move in from the bottom, 300 ms ease-out, scrim fades in (same as the Add sheet). |

Keyboard: tapping the message focuses the Text Area (Focused state: 1.5 black ring + caret). Proposal: the sheet rises with the keyboard (iOS `.presentationDetents` does it; Android `imePadding` on the sheet) so the field stays visible; the buttons may go under the keyboard.

### 6.6 Designer notes (verbatim)
- Remind — Rohan: "Remind opens a pre-written message that you can edit, in a Friendly or Neutral tone. Send it as a Paybak notification, or share it through any app. “Send in Paybak” closes the sheet and shows Overlay / Toast “Reminder sent to Rohan” for 2 s (16 above the tab bar on Home, 50 above the bottom elsewhere). The balances don’t change."
- Remind sheet (overlay): "The Remind sheet on its own. Pages that aren’t Home open it as an overlay over the current screen, so the chat or friend page stays visible under the scrim."
- _Sheet / Remind Rohan (caption): "Local component (unpublished). It fills the Sheet / Container Content slot on 08-07, 08-07s and 08-08, and on the helper copies “↳ Remind sheet (overlay)” that the integrator pastes onto pages 07, 09 and 11."
- _Sheet / Remind Rohan (component description): "Local, unpublished content for the Sheet / Container Content slot on 08-07, 08-07s and 08-08 (and the helper copies “↳ Remind sheet (overlay)” on pages 07, 09 and 11): Rohan’s row, the Friendly | Neutral tone switch, the editable message, then Send in Paybak and Share…. SwiftUI: PBRemindSheet"

### 6.7 Reuse map
Scrim + sheet shell → `Sheet / Container` Medium (components-home §15) with the kit grabber and glass ✕ (components-home §14) · person row → NEW Row / Person §0.4-C · tone → `Control / Segmented` Options=2 (components-core §4.3) · message → NEW Control / Text Area §0.4-M · buttons → `Button / Primary` + `Button / Secondary` Large (components-core §2.1) · toast → NEW §0.4-L · background → Home Active (screens-home.md §2).

---

## 7. `settleRemindShare`: Remind — Share (177:23342, 08-08)

**Purpose:** "Share…" hands the message to the iOS share sheet. **Container:** system share sheet over the Remind sheet over Home. **Ref:** `ref/settleRemindShare.png`.

- Layers: Home Active background → Scrim (`bg/scrim`) → the Remind sheet (same as §6, unchanged) → "Scrim 2" (a second 40 % `bg/scrim`) → **"Share sheet (kit)"** = the iOS kit "Activity View - iPhone" (Mode=Light) with its sheet top at y 550 (instance at 0, 488; sheet starts 62 lower).
- Kit content (system UI, **don't draw it**; the OS share sheet replaces it): header with the message preview "Hi Rohan! Just a gentle reminder about ₹800 for the movie…" and a ✕; an app row **WhatsApp · Messages · Mail**; an action list with **Copy**. Android shows its own chooser (Sharesheet).
- Dismissing the share sheet (✕ at 342, 566 or tapping Scrim 2) returns to `settleRemind` with the Remind sheet still open (Figma: smart animate 300 ms ease-out back to 177:21203).
- Share payload: plain text = the current message. Proposal: no subject/URL; on iOS also set the subject "Payment reminder" for Mail.

Designer note (verbatim): "“Share…” hands the same message to the iOS share sheet, so you can send it on WhatsApp, Messages or Mail, or copy it."

Reuse map: everything is the Remind sheet (§6) plus the platform share sheet. Art: the share-sheet app icons (WhatsApp, Messages, Mail) are system UI; nothing to export.

---

## 8. `settleNotReceived`: Not received (177:25006, 08-10) and the Not received sheet

**Purpose:** the receiver (Arjun) tells the payer (Esha) that her claimed payment hasn't arrived, with an editable note, instead of silently rejecting it. **Container:** sheet over **Home — Confirm payment** (04-06, `screens-home-v2.md`); the overlay-only copy "Not received sheet (overlay)" (177:25168, 08-10s) is the same sheet for Activity and the Notifications inbox (its scrim, Send and Cancel all CLOSE). **Refs:** `ref/settleNotReceived.png`, `ref/settleNotReceivedOverlay.png`, `ref/settleNotReceivedSheet.png`.

### 8.1 Background (Home with a pending confirmation, under the scrim)
Home Active shifted down by the confirm card (all from Figma):
| Element | Rect | Content |
|---|---|---|
| Nav header (Home) | 20, 62, 362, 94 | "Paybak", sparkles + bell (badge), "Good evening, Arjun" |
| Card / Confirm Payment, State=Pending | 20, 180, 362, 120 | avatar Esha 40 white at (36, 196) · title "Esha says she paid you ₹700" (Headline, 88, 196) · detail "Dinner at Olive Garden · UPI · 9:12 pm" (Footnote secondary, 88, 218) · "Confirm" (Primary Small, 36, 248, 161 × 36) · "Not received" (On Card Small, 205, 248, 161 × 36) |
| Balance summary | 20, 324, 362, 180 | +₹2,900 from 4 people · −₹1,850 across 2 groups · Settle up |
| Due soon | 20, 528, 362, 228 | Rohan ₹800 Overdue 3 days Remind · Goa Trip ₹1,400 Due Fri Settle |
| Recent activity | 20, 780, … | (below the fold) same three rows as Home Active |
| Scroll edge fade | 0, 756, 402 × 118 | white gradient (0 % → 85 % at 0.4 → 100 %) |
| Tab bar | 20, 791 | Home active |

### 8.2 The sheet: `Sheet / Container` Detent=Medium, **Show header=false** (no title, no ✕), grabber on
- Container 8, 408, 386 × 458, `bg/primary`, radius 40, padding 8/16/28/16, gap 8; grabber 60 × 4 at (171, 416). Content (`_Sheet / Not received`, 354 × 410) at (24, 428), V gap 24, **padding-top 8**:

| # | Element | Rect (frame) | Style / component | Content |
|---|---|---|---|---|
| 1 | title | 24, 436, 354, 52 | **Title/3** `text/primary`, wraps (2 lines) | "Let Esha know you haven’t received ₹700?" |
| 2 | context | 24, 496, 354, 20 | **Subheadline** `text/secondary` | "Dinner at Olive Garden · UPI · 9:12 pm" |
| 3 | note | 24, 540, 354, 158 | Control / Text Area, Default | label "Note"; field (y 568, 354 × 104): "Hi Esha, I haven’t received ₹700 for Dinner at Olive Garden yet. Could you check your UPI app?"; helper "Esha still owes you ₹700 until a payment is confirmed." |
| 4 | Send | 24, 722, 354, 52 | Button / Primary Large | "Send" |
| 5 | Cancel | 24, 786, 354, 52 | Button / Secondary Large | "Cancel" |

### 8.3 Templates (verbatim sample → template)
- Title: "Let {Name} know you haven’t received {amount}?"
- Context: "{expense or For} · {Method} · {time h:mm a, lower-case am/pm}" (same as the confirm card's detail).
- Note (editable): "Hi {Name}, I haven’t received {amount} for {expense} yet. Could you check your {Method} app?" Proposal for methods without an app: Cash → "Could you check with me?" is NOT designed; use "Could you check?" for Cash/Other, "your bank app" for Bank, "your card app" for Card.
- Helper: "{Name} still owes you {amount} until a payment is confirmed."

### 8.4 Behaviour
- **Send**: sends the note to the payer (simulated: record it on the payment and in the payer's side of Activity/Notifications), marks that payment claim as **Not received** (it stays unconfirmed; it never counted), removes the confirm card from Home, and closes the sheet. Balances don't change: Esha's ₹700 stays owed ("+₹2,900 from 4 people"). Figma: Send → Home — Active. No toast in Figma; proposal: none (the card leaving is the feedback) or "Note sent to Esha" if QA wants one (not designed).
- **Cancel**, scrim tap, swipe down: close the sheet and keep the confirm card (Figma → Home — Confirm payment).
- Keyboard: as §6.5.

### 8.5 Designer notes (verbatim)
- Not received: "Not received sends Esha an editable note instead of silently rejecting her payment. Her ₹700 stays owed until a payment is confirmed."
- Not received sheet (overlay): "The Not received sheet on its own. Activity and the Notifications inbox open it over themselves, so the timeline stays visible under the scrim."
- _Sheet / Not received (caption): "Local component (unpublished). It fills the Sheet / Container Content slot on 08-10 and 08-10s, and on the helper copy “↳ Not received sheet (overlay)” that the integrator pastes onto page 09."
- _Sheet / Not received (component description): "Local, unpublished content for the Sheet / Container Content slot on 08-10 and 08-10s (and the helper copy “↳ Not received sheet (overlay)” on page 09): the question, the payment context, an editable note to Esha, then Send and Cancel. SwiftUI: PBNotReceivedSheet"

### 8.6 Reuse map
Sheet → `Sheet / Container` Medium, Show header=false (components-home §15: with the header hidden, content starts 20 below the sheet top: 8 padding + 4 grabber + 8 gap) · note → NEW Control / Text Area §0.4-M · buttons → `Button / Primary` / `Button / Secondary` Large · confirm card behind → NEW Card / Confirm Payment §0.4-N.

---

## 9. `settlePaymentConfirmed`: Payment confirmed (177:25303, 08-11) = Home after Confirm

**Purpose:** Home after the receiver taps **Confirm** on Esha's card. **Container:** tab root (Home). **Ref:** `ref/settlePaymentConfirmed.png`.

### 9.1 How Home changes after Confirm (vs Home — Confirm payment / Home Active)
| Element | Before (04-06) | After (08-11) |
|---|---|---|
| Confirm card | "Esha says she paid you ₹700" with Confirm / Not received | **Gone** (the card animates to its Confirmed state, then leaves; see 9.3) |
| You’re owed card | +₹2,900 · from 4 people | **+₹2,200 · from 3 people** (Rohan ₹800, Priya ₹700, Dev ₹700) |
| You owe card | −₹1,850 · across 2 groups | unchanged |
| Due soon | Rohan (Overdue 3 days, ₹800, Remind) · Goa Trip (Due Fri, ₹1,400, Settle) | unchanged |
| Recent activity | 3 rows | **4 rows**; new first row: Row / Activity Type=Payment, Direction=In: avatar Esha, "Esha paid you", "UPI", "₹700", "Today" |
| Toast | – | "Payment confirmed" (check-circle), at 100, 731, 203 × 44: 16 above the tab bar, fades after 2 s |

Recent activity after confirm (y 672, 740, 808, 876): Esha paid you · UPI · ₹700 · Today / Dinner at Olive Garden · You paid · 4 people · ₹2,800 · Today / Priya paid you · UPI · ₹1,050 · Yesterday / Electricity bill · Flat 302 · You owe · −₹450 · 26 Sep. The section grows to 304 (the fourth row sits under the tab bar and fade; it's visible once scrolled).

Also, off this screen (derived, must follow from the data): Esha's row leaves the You’re owed breakdown and the Settle up "people who owe you" list (3 people); Esha's side sees her payment confirmed; the payment's status becomes Confirmed.

### 9.2 Layout: exactly Home Active (screens-home.md §2) with the values above
Header 20, 62 · Balance summary 20, 180 · Due soon 20, 384 · Recent activity 20, 636 (362 × 304) · fade 0, 724 · tab bar 20, 791 · toast 100, 731.
- Note on "You’re owed": on this page (and in the Remind / Not received backgrounds) the Balance card label reads **"You’re owed" with a curly ’**, and the breakdown title too. `screens-home.md` / README said Home uses a straight ' ; the component text appears to have changed. Follow the curly ’ everywhere (flagged in Issues; the Home v2 change report should confirm).

### 9.3 Behaviour of Confirm (from the component + frames)
1. Tap **Confirm** on Card / Confirm Payment: the card changes to State=Confirmed with a smart animate, **250 ms ease-out**: title "Esha paid you ₹700", detail "Dinner at Olive Garden · UPI · Confirmed", Check Circle icon, no buttons, height 120 → ≈72.
2. The payment is marked confirmed; balances recompute (you’re owed −₹700); the activity row is added; the toast "Payment confirmed" shows for 2 s.
3. The card then leaves Home (08-11 has no card). Proposal: keep the Confirmed card ~1.5 s, then collapse it (height → 0, 250 ms ease-out) while the content below moves up.
- The same confirm flow runs from Activity and the Notifications inbox (inline Confirm / Not received; `screens-activity.md`) and from the lock-screen push actions.

### 9.4 Navigation (reactions)
| Element | Destination |
|---|---|
| ＋ | Add sheet overlay ("↳ Add sheet (overlay)" 216:29122; MOVE_IN from the bottom 300 ms ease-out), rows → page 06 forms. |
| Groups tab | Groups (167:14881, page 07). |
| Activity tab | Activity — Timeline (167:14361, page 09). |
| Profile tab | Profile (64:4316, page 05). |
| Everything else | As Home Active (screens-home.md / screens-home-v2.md): balance cards → §1 / §2, Settle up → §3, Rohan Remind → §6, Goa Trip Settle → Record payment for Kabir (§4), See all → Activity. |

### 9.5 Designer note (verbatim)
> "After Confirm, Esha’s ₹700 is settled. “You’re owed” drops to +₹2,200 from 3 people (Rohan ₹800, Priya ₹700, Dev ₹700), and her payment tops Recent activity."

### 9.6 Reuse map
Everything is Home (components-home.md: Nav Header, Card / Balance Summary, Row / Section Header, Row / Attention, Row / Activity Payment In with Esha art, Tab Bar) + NEW Overlay / Toast §0.4-L.

---

## 10. Sample data (verbatim, all frames)

People and avatars (all art exists in `assets/avatars/`): Arjun (you) `avatar-1` · Priya `avatar-2` · Rohan `avatar-3` · Esha `avatar-4` · Dev `avatar-5` · Kabir `avatar-6` (full name **"Kabir Singh"**, UPI **"kabir@okaxis"**, Payment Preview initials fallback "KS") · Meera `avatar-7`. The user's UPI (in the Friendly message): **"arjun@okaxis"**.

| Where | Strings |
|---|---|
| You’re owed breakdown | "You’re owed", "+₹2,900", "from 4 people", "Who owes you", Rohan "Movie tickets" "+₹800" "Overdue 3 days"; Priya / Esha / Dev "Dinner at Olive Garden" "+₹700" "Due Sun 4 Oct"; "Settle up" |
| You owe breakdown | "You owe", "−₹1,850", "across 2 groups", "Who you owe", Kabir "Goa Trip" "−₹1,400" "Due Fri 2 Oct"; Meera "Flat 302" "−₹450" "Due Mon 5 Oct"; "Goa Trip uses simplified debts, so you pay Kabir directly."; "Settle up" |
| Settle up | "Settle up", "Paybak simplifies balances into the fewest payments. Everyone ends up in the same place.", "2 payments to make", Kabir "Goa Trip" "Due Fri" "₹1,400" "Settle"; Meera "Flat 302" "Due Mon" "₹450" "Settle"; "4 people owe you", Rohan "Movie tickets" "Overdue 3 days" "₹800" "Remind"; Priya / Esha / Dev "Dinner at Olive Garden" "Due Sun" "₹700" "Remind" |
| Record payment | "Record payment", "Save", "From" "You", "To" "Kabir", "INR", "₹1,400", "You owe Kabir ₹1,400 in Goa Trip", "Method", "Cash" "UPI" "Bank" "Card" "Other", "Kabir Singh", "kabir@okaxis", "Copy", "For" "Goa Trip", "Date" "Wed 30 Sep", "Proof" "Add photo (optional)", "You paid Kabir ₹1,400 by UPI for Goa Trip.\nKabir will be asked to confirm. Paybak never moves money." |
| Payment pending | "Payment", "Edit", "You paid Kabir", "₹1,400", "UPI · Today · Goa Trip", "Pending confirmation", "Waiting for Kabir to confirm", "From" "You", "To" "Kabir", "Method" "UPI", "Paid to" "kabir@okaxis", "Date" "Wed 30 Sep", "For" "Goa Trip", "Proof" "None", "Your balance updates once Kabir confirms.", "Cancel payment", toast "Payment recorded" |
| Remind sheet | "Remind Rohan", "Rohan", "Movie tickets", "+₹800", "Overdue 3 days", "Tone", "Friendly", "Neutral", "Message", "Hi Rohan! Just a gentle reminder about ₹800 for the movie tickets on 20 Sep. You can pay me on UPI at arjun@okaxis. Thanks.", "You can edit this message.", "Send in Paybak", "Share…"; toast "Reminder sent to Rohan" |
| Share sheet (kit) | "Hi Rohan! Just a gentle reminder about ₹800 for the movie…", "WhatsApp", "Messages", "Mail", "Copy" |
| Confirm card | "Esha says she paid you ₹700", "Dinner at Olive Garden · UPI · 9:12 pm", "Confirm", "Not received"; Confirmed: "Esha paid you ₹700", "Dinner at Olive Garden · UPI · Confirmed" |
| Not received sheet | "Let Esha know you haven’t received ₹700?", "Dinner at Olive Garden · UPI · 9:12 pm", "Note", "Hi Esha, I haven’t received ₹700 for Dinner at Olive Garden yet. Could you check your UPI app?", "Esha still owes you ₹700 until a payment is confirmed.", "Send", "Cancel" |
| Payment confirmed (Home) | "Good evening, Arjun", "You’re owed" "+₹2,200" "from 3 people", "You owe" "−₹1,850" "across 2 groups", "Settle up", "Due soon", Rohan "Movie tickets" "Overdue 3 days" "₹800" "Remind", "Goa Trip" "Your share" "Due Fri" "₹1,400" "Settle", "Recent activity" "See all", "Esha paid you" "UPI" "₹700" "Today", "Dinner at Olive Garden" "You paid · 4 people" "₹2,800" "Today", "Priya paid you" "UPI" "₹1,050" "Yesterday", "Electricity bill" "Flat 302 · You owe" "−₹450" "26 Sep"; toast "Payment confirmed" |
| Other toast | "UPI ID copied" (Record payment Copy) |

Demo facts implied (input to `domain.md` / `seed/`): today Wed 30 Sep 2026; Goa Trip simplify debts on, Arjun's net −₹1,400, simplified payee Kabir, due Fri 2 Oct; Flat 302 electricity bill (dated 26 Sep), Arjun owes Meera ₹450, due Mon 5 Oct; Movie tickets (20 Sep), Rohan owes Arjun ₹800, due 27 Sep (→ "Overdue 3 days" on 30 Sep); Dinner at Olive Garden (Today), ₹2,800 paid by Arjun, split equally among Arjun, Priya, Esha, Dev (₹700 each), due Sun 4 Oct ("This weekend"); Esha's UPI payment claim of ₹700 at 9:12 pm today (pending receiver confirmation, drives the Home confirm card); Priya paid you ₹1,050 (UPI) yesterday (already confirmed).

---

## 11. Assets

- **Icons used** (all already in `assets/icons/`, nothing new exported): `chevron-left`, `chevron-right`, `shuffle`, `arrow-right`, `copy`, `groups`, `calendar`, `camera`, `activity`, `delete`, `check-circle`, `sparkles`, `bell`, `money-in`, `money-out`, `food`, `bolt`, `home`, `plus`, `profile`. The kit xmark (Modal Header close, sheet ✕) is the SF Symbol `xmark` on iOS and `close.svg` on Android (components-home §14).
- **Avatars**: `assets/avatars/avatar-1…7` (Arjun, Priya, Rohan, Esha, Dev, Kabir, Meera). Kabir and Meera were "unused" in the first build; they're used now.
- **Illustrations / Rive**: none on this page. No frame here uses a `.riv` or an illustration, so nothing was exported. (Only the proposed all-settled state of Settle up, §3.2, would reuse `paybak-home-allset.riv`.)
- **System art not exported**: the iOS share sheet's WhatsApp / Messages / Mail icons (kit "Activity View - iPhone") are drawn by the OS.
- **New files written by this spec**: `ref/settleOwedBreakdown.png`, `ref/settleOweBreakdown.png`, `ref/settleUp.png`, `ref/settleRecordKabir.png`, `ref/settlePaymentPending.png`, `ref/settleRemind.png`, `ref/settleRemindOverlay.png`, `ref/settleRemindShare.png`, `ref/settleRemindSheet.png` (708 × 948, the local component alone), `ref/settleNotReceived.png`, `ref/settleNotReceivedOverlay.png`, `ref/settleNotReceivedSheet.png` (708 × 820), `ref/settlePaymentConfirmed.png` (all 2×, 804 × 1748 for frames). The node-tree dumps weren't kept.

## 12. Test IDs (flow.md format `<screen>.<element>`, same on both platforms)
`screen.settleOwedBreakdown`, `owedBreakdown.row.<friendId>`, `owedBreakdown.settleUp` · `screen.settleOweBreakdown`, `oweBreakdown.row.<friendId>`, `oweBreakdown.settleUp` · `screen.settleUp`, `settleUp.pay.<friendId>` (Settle), `settleUp.remind.<friendId>` · `screen.recordPayment`, `recordPayment.close`, `recordPayment.save`, `recordPayment.amount`, `recordPayment.method.<cash|upi|bank|card|other>`, `recordPayment.copy`, `recordPayment.for`, `recordPayment.date`, `recordPayment.proof` · `screen.paymentDetail`, `paymentDetail.edit`, `paymentDetail.cancel`, `paymentDetail.status` · `remind.sheet`, `remind.tone.friendly`, `remind.tone.neutral`, `remind.message`, `remind.send`, `remind.share`, `remind.close` · `notReceived.sheet`, `notReceived.note`, `notReceived.send`, `notReceived.cancel` · `home.confirmCard`, `home.confirmCard.confirm`, `home.confirmCard.notReceived` · `toast` (text = the toast label).

## 13. Open questions / proposals collected
1. **Neutral reminder copy is not in Figma** (§6.3). Proposal given; needs sign-off.
2. Not received: no toast designed after Send (§8.4).
3. Cancel payment: no confirmation designed (§5.2 proposal).
4. Pending payment on Settle up and payment-detail states after Confirm / Not received are not designed (§3.5, §5.2).
5. Singular/plural and other caption variants ("1 person owes you", "across 1 group") are proposals (§1.2, §2.2, §3.2).
6. "You’re owed" now uses a curly ’ in these frames (§9.2); older specs say straight '.
7. Share success can't be detected; proposal closes the sheet after the share sheet returns (§6.4).
