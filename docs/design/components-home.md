# Paybak: Home components spec (Figma page "02 Components", 3:3)

Covers: Cards & Rows (13:220): Card / Balance 13:269, Card / Balance Summary 13:271, Row / Section Header 13:223, Row / Attention 13:379, Row / Activity 13:477, Card / Empty State 13:541 · Navigation (17:458): Nav Header 17:494, Tab Bar Item 17:504, Tab Bar 17:618 · Button / Icon 10:79 and Button / Add 10:87 (as used on Home) · Sheets (17:620): Sheet / Action Row 17:641, Sheet / Action Sheet 17:643 · Feedback & Overlays (118:962): Sheet / Container 118:1017 · Badge / Pill 11:46.
Generic buttons, avatars and brand are in `components-core.md` (a separate spec). Their Home-relevant variants are summarised here so this file stands alone.

Conventions: sizes in pt (dp on Android). Coordinates are **component-relative** (x, y from the component's top-left). Colours are tokens (tokens.md) with hex. Text styles are tokens.md names. Icons: `assets/icons/<kebab-name>.svg`, 24×24 viewBox, stroke 1.5 (the stroke scales with size: 20 pt → 1.25, 16 pt → 1.0; that's automatic when you scale the SVG). "Hug" = size to content; "Fill" = take the remaining space.
Each component's Figma description is quoted, and it names the intended SwiftUI type (e.g. `PBBalanceCard`). Use those names on iOS and matching Composables on Android (`PBBalanceCard(...)`).

---

## 1. Navigation / Nav Header (17:494). SwiftUI: `PBNavHeader`
> "Home: logo, glass sparkle (opens Ask Paybak) and glass bell (unread dot), greeting in Title/1. Large Title: tab title + optional exposed glass action (Show action; Bell, Plus, User Add or Restore). Inline: the 44-tall collapsed bar for scrolled tab screens, centred Headline on white at 90% with a background blur and no shadow; place it full-bleed at y 62."

Properties: `Type` = Home | Large Title | Inline (default Home) · `Show action` boolean (default true; used by Large Title).

### Type=Home (17:474): 362 × 94. Used on all Home screens.
Vertical, gap 12 (`space/12`), width fills the screen minus 2×20.
- `toolbar` (0,0) 362×44: horizontal, gap 8 (`space/8`), items centred vertically.
  - `logo`: `Brand / Logo` Layout=Horizontal, (0,8) 104×28: horizontal, gap 8.
    - `mark`: `Brand / App Mark` Size=28, (0,8) 28×28, fill `color/bg/inverse` #0A0A0A, radius 6.2636, clip, white "P" glyph vector 9.4×14.9 at (9.63, 6.56) inside the mark. File: `assets/brand/app-mark-28.svg`.
    - `wordmark` TEXT "Paybak", (36,10) 68×24, **Brand/Wordmark S** (Manrope ExtraBold 20 / 24, ls −3 %), `color/text/primary`.
  - `spacer` (fill).
  - `assistant`: `Button / Icon` Style=Glass, State=Default, Badge=false, Icon = Icon / Sparkles. (266,0) 44×44. Icon (276,10) 24×24, `color/icon/primary`, stroke 1.5.
  - `bell`: `Button / Icon` Style=Glass, State=Default, **Badge=true**, Icon = Icon / Bell. (318,0) 44×44. Icon (328,10) 24. `badge` ellipse (344,9) 10×10 (absolute), fill `color/bg/inverse`, stroke 2 **outside** `color/bg/primary`.
- `greeting` TEXT (0,56) 362×38, **Title/1** (ExtraBold 32 / 38, ls −2 %), `color/text/primary`, left, fills the width, wraps. Default text "Good evening, Arjun".

### Type=Large Title (17:486): 362 × 44 (other tabs; not used on Home)
- `toolbar` 362×44 horizontal, gap 0: `title` TEXT (0,3) "Activity", **Title/1**, `color/text/primary`; spacer; `action` = `Button / Icon` Style=Glass (Bell by default, Badge=false) 44×44 at (318,0), visible only when `Show action` = true.

### Type=Inline (98:873): 402 × 44 (collapsed bar for scrolled tab screens; not used on Home)
Full-bleed (402 wide, place at y 62), height 44 (`size/tap`), fill `color/bg/primary` @90 % + BACKGROUND_BLUR radius 24, no shadow. Centred `title` "Activity" **Headline**, `color/text/primary`.

---

## 2. Button / Icon (10:79). SwiftUI: `PBIconButton`. Header buttons on Home.
> "Circular 44pt icon button. Glass = Liquid Glass for floating toolbar buttons (Home bell). Badge = small black unread dot."

Properties: `Icon` (instance swap, default Icon / Bell) · `Badge` boolean (default false) · `Style` = Plain | Filled | Glass | Inverse · `State` = Default | Pressed.
Common: 44×44 (`size/tap`), circle (`radius/full`), icon 24×24 centred at (10,10), `badge` ellipse (26,9) 10×10 with a 2 pt **outside** stroke, visible when Badge=true.

| Style / State | Fill | Stroke | Effect | Icon tint | Badge fill / ring |
|---|---|---|---|---|---|
| Plain / Default | none | – | – | icon/primary #0A0A0A | bg/inverse / bg/primary |
| Plain / Pressed | bg/selected #0A0A0A @6 % | – | – | icon/primary | same |
| Filled / Default | bg/card #F5F5F5 | – | – | icon/primary | same |
| Filled / Pressed | bg/card-pressed #EBEBEB | – | – | icon/primary | same |
| **Glass / Default** (Home) | **bg/glass #FFFFFF @72 %** | **1 inside, border/glass-highlight #FFFFFF @60 %** | **Material/Glass Small**: drop shadow (0,4) blur 16 #0A0A0A @8 %; GLASS radius 6, refraction 0.4, depth 8 | icon/primary | bg/inverse #0A0A0A / ring bg/primary #FFFFFF |
| Glass / Pressed | bg/card #F5F5F5 | same | same | icon/primary | same |
| Inverse / Default | bg/inverse #0A0A0A | – | – | icon/inverse #FFFFFF | bg/primary / ring bg/inverse |
| Inverse / Pressed | bg/inverse-pressed #2B2B2B | – | – | icon/inverse | same |

iOS Glass: `.glassEffect(.regular.interactive(), in: .circle)` on the 44×44 frame (the system handles the press). Android Glass: the table values (pressed = #F5F5F5 fill).

---

## 3. Navigation / Tab Bar Item (17:504). SwiftUI: `PBTabItem`
> "Tab item. Active = black icon/label on a 6% pill. Inactive = gray."

Properties: `Label` text (default "Home") · `Icon` instance swap (default Icon / Home) · `State` = Active | Inactive.
Geometry (both states): 68 × 52, capsule (`radius/full`), vertical stack, gap 2 (`space/2`), content centred on both axes: `icon` 24×24 at (22, 6.5); `label` at (centred, 32.5), **Caption/2** (SemiBold 11 / 13, ls +1 %), hug width, single line.

| State | Background | Icon tint | Label colour |
|---|---|---|---|
| Active (17:496) | `color/bg/selected` #0A0A0A @6 % (the full 68×52 capsule) | `color/icon/primary` #0A0A0A | `color/text/primary` #0A0A0A |
| Inactive (17:500) | none | `color/icon/secondary` #6B6B6B | `color/text/secondary` #6B6B6B |

Same outline icon in both states (no filled variants). No pressed state in Figma. Suggestion: a light press feedback (opacity 0.6 or bg/selected flash).

## 4. Button / Add (10:87). SwiftUI: `PBAddButton`
> "Center ＋ in the tab bar. Opens the Add sheet (Add expense · Record payment · Lend money · New group)."

`State` = Default | Pressed. 52×52 (`size/add-button`), circle, fill **Default `color/bg/inverse` #0A0A0A / Pressed `color/bg/inverse-pressed` #2B2B2B**. Icon Icon / Plus 24×24 at (14,14), tint `color/icon/inverse` #FFFFFF, **stroke width 2** (override; the other icons use 1.5). Asset: `assets/images/add-button-plus.svg` (white, stroke 2). Accessibility label "Add".

## 5. Navigation / Tab Bar (17:618). SwiftUI: `PBTabBar`
> "Floating Liquid Glass capsule (Material/Glass, 72% white, white 60% hairline). Sits 20pt from the sides, 21pt above the bottom edge. Center ＋ opens the Add sheet."

Property: `Active` = Home | Groups | Activity | Profile (which tab item has State=Active; the rest are Inactive).
- Container: 362 × 62 (`size/tabbar`); on screen it's full width minus 2×20 (fixed 362 on the 402 canvas), capsule, fill `color/bg/glass` #FFFFFF @72 %, **1 pt inside stroke** `color/border/glass-highlight` #FFFFFF @60 %, effect **Material/Glass**: drop shadow (0,8) blur 32 spread 0 #0A0A0A @10 % + GLASS radius 16, refraction 0.7, depth 30.
- Auto-layout: horizontal, padding 5 on all sides (the 1 pt stroke is also inside, so the effective content inset is 6), `SPACE_BETWEEN`, items centred vertically → equal gaps of **6.5**.
- Children (x, y inside the bar; all y 5):
  | Child | x | Size | Component | Icon | Label |
  |---|---|---|---|---|---|
  | home | 6 | 68×52 | Tab Bar Item | `home.svg` (Icon / Home, home-01) | Home |
  | groups | 80.5 | 68×52 | Tab Bar Item | `groups.svg` (Icon / Groups, user-group) | Groups |
  | add | 155 | 52×52 | Button / Add | `add-button-plus.svg` | – |
  | activity | 213.5 | 68×52 | Tab Bar Item | `activity.svg` (Icon / Activity, clock-01) | Activity |
  | profile | 288 | 68×52 | Tab Bar Item | `profile.svg` (Icon / Profile, user-circle) | Profile |
- Placement on screen: x 20, **bottom 21 pt above the screen's bottom edge** (y791 on the 874 canvas; 13 pt below the bottom safe-area edge). Content scrolls under it.
- iOS: `.glassEffect(.regular, in: .capsule)`. Android: the fill, stroke and shadow values above; backdrop blur optional.

---

## 6. Card / Balance (13:269). SwiftUI: `PBBalanceCard`
> "Balance total. Owed = bold black +₹ · Owe = gray −₹ · Settled = ₹0. Tap opens the per-person breakdown. Show chevron (default on). Show action: trailing Button / Primary Small “Settle up” (exposed). Show badge: Badge / Pill top-right (exposed, default Overdue). Both off by default; set the instance to FILL width for full-width cards (06-19, 07-04/07/08). Turn Show chevron off when Show badge is on (both sit top-right)."

Properties: `Type` = Owed | Owe | Settled (default Owed) · `Show chevron` (true) · `Show action` (false) · `Show badge` (false).
Geometry (all types): default 175 × 116 (width fills in use; height hugs). Fill `color/bg/card` #F5F5F5, radius 20 (`radius/card`), padding 16 (`layout/card-padding`) all sides, vertical gap 12, no border, no shadow.
- `top` row (16,16) fill-width × 20: horizontal, gap 6 (`space/6`), centred: `icon` 16×16 at (16,18) tinted `color/icon/secondary` #6B6B6B · `label` (38,16) **Subheadline** (Medium 14/20) `color/text/secondary`, fills the width · `chevron` Icon / Chevron Right 16×16 at (w−32, 18) tinted `color/icon/tertiary` #A3A3A3 (visible when Show chevron).
- `amount block` (16,48): vertical, gap 2 (`space/2`), hug: `amount` **Amount/Large** (ExtraBold 26/32, ls −2 %), 32 tall · `caption` (16,82) **Footnote** (Medium 13/18) `color/text/tertiary` #A3A3A3.
- `action` (hidden by default, absolute, bottom 24 / right 16): `Button / Primary` Small "Settle up": 36 tall (`size/button-sm`), padding 0/16, capsule, `color/bg/inverse`, label Button/Small `color/text/inverse`.
- `badge` (hidden by default, absolute, top 16 / right 16): `Badge / Pill` Style=Overdue "Overdue 3 days" (see 13).

| Type | Icon | Label | Amount text (colour) | Caption |
|---|---|---|---|---|
| Owed (13:231) | `money-in.svg` | You're owed (straight ') | +₹2,900 (`text/primary` #0A0A0A) | from 4 people |
| Owe (13:244) | `money-out.svg` | You owe | −₹1,850 (`text/secondary` #6B6B6B; U+2212 minus) | across 2 groups |
| Settled (13:257) | `check-circle.svg` | All settled | ₹0 (`text/tertiary` #A3A3A3) | Nothing pending |

States: no pressed variant. Recommended pressed fill `color/bg/card-pressed` #EBEBEB (it's tappable).

## 7. Card / Balance Summary (13:271). SwiftUI: `PBBalanceSummary`
> "Home balance summary: Owed + Owe cards and one primary Settle up."

Property: `Show Settle up` (true). 362 × 180 (hugs), vertical, gap 12.
- `cards` row 362×116: horizontal, gap 12; two Card / Balance instances (Owed, Owe), each **Fill** → 175 wide. Owe card at x187.
- `settle up` (0,128) 362×52: `Button / Primary` Large (`size/button-lg` 52), fill width, capsule, `color/bg/inverse`, padding 0/24, label "Settle up" **Button/Large** (SemiBold 17/22, ls −0.5 %) `color/text/inverse`, centred. Visible when Show Settle up.

## 8. Row / Section Header (13:223). SwiftUI: `PBSectionHeader`
> "Section title (Title/3) with optional “See all” text button."

Properties: `Title` (default "Recent activity") · `Show action` (true).
362 × **32** fixed height, horizontal, `SPACE_BETWEEN`, centred. `title` (0,3) **Title/3** (Bold 20/26, ls −1 %) `color/text/primary`, hug. `action` (right-aligned) = `Button / Text` Style=Secondary "See all": hit area 47×44 (`size/tap`) at (315,−6) (overflows the 32 row by 6 above and below), label **Button/Small** (SemiBold 15/20, ls −0.25 %) `color/text/secondary`, no chevron. Button / Text states: Pressed = the whole button at **50 % opacity**; Disabled = `color/text/disabled` #A3A3A3.

## 9. Row / Attention (13:379). SwiftUI: `PBAttentionRow`
> "Due soon / overdue item on Home. Overdue = red badge + Remind (money owed to you). Due soon = white badge + Settle (money you owe)."

Property: `State` = Overdue | Due soon. (Title, detail, amount, badge label and button label are plain text overrides in instances.)
Geometry: 362 × 88 (height hugs: 12 + 64 + 12), fill `color/bg/card` #F5F5F5, radius 20, padding **12 top/bottom, 16 left/right**, horizontal, gap 12, items centred vertically.
- `avatar` (16,24) 40×40 circle (`Avatar / Circle` Size=40), fill **`color/bg/primary` (white)** because it's on a card, clips content.
  - Overdue: Type=Art: person art (default Art / Peep Head / Rohan → `assets/images/peep-head-rohan.svg`, scaled to 40×40).
  - Due soon: Type=Icon: `groups.svg` 20×20 centred (26,34), `color/icon/primary`, stroke 1.25.
- `text` column (68, 18), fills the width, vertical gap 6:
  - `title row` hug, horizontal gap 6, centred: `title` **Headline** (SemiBold 16/22, ls −0.25 %) `color/text/primary` + `detail` **Subheadline** `color/text/secondary`.
  - `badge` 24 tall: Overdue → `Badge / Pill` Style=**Overdue** (red); Due soon → Style=**On Card** (white).
- `amount + action` column (right edge 346), hug, vertical gap 6, **right-aligned**: `amount` **Amount/Medium** (Bold 17/22, ls −0.5 %) `color/text/primary` (both states) · `action` `Button / On Card` Size=Small: 36 tall, padding 0/16, capsule, fill `color/bg/primary` (white), label **Button/Small** `color/text/primary`. Pressed fill `color/bg/card-pressed` #EBEBEB; disabled label `color/text/disabled`.

| State | Avatar | Title | Detail | Badge (style, label) | Amount | Button |
|---|---|---|---|---|---|---|
| Overdue (13:305) | Art (Rohan) | Rohan | Movie tickets | Overdue, "Overdue 3 days" (110 wide) | ₹800 | Remind (86 wide) |
| Due soon (13:356) | Icon (Groups) | Goa Trip | Your share | On Card, "Due Fri" (61 wide) | ₹1,400 | Settle (76 wide) |

## 10. Row / Activity (13:477). SwiftUI: `PBActivityRow`
> "Activity and timeline row, 64 tall (grows when the subtitle wraps to 2 lines or the detail line shows). Type=Expense: category icon in a circle; Payment: person avatar. Direction: In = black amount, Out = gray −₹. Surface=Plain on white (#F5F5F5 circle); On Card inside #F5F5F5 cards (white circle). Show amount / Show date: the trailing column; Show badge puts an exposed Badge / Pill in the date slot (status chips; Muted on Plain, On Card on On Card). Show detail line + Detail: a 3rd Footnote line (rate, next date, deleted by). Show action: exposed small button (Secondary on Plain, On Card on On Card), turn Show amount and Show date off with it. Unread: 8pt black dot. Show divider: leading-inset hairline for rows in a card."

Properties: `Type` = Expense | Payment · `Direction` = In | Out · `Surface` = Plain | On Card · `Icon` instance swap (default Icon / Food) · `Show amount` (true) · `Show date` (true) · `Show badge` (false) · `Show detail line` (false) · `Detail` text (default "Next · Thu 1 Oct") · `Show action` (false) · `Unread` (false) · `Show divider` (false).
Geometry: width fills (362), **min height 64**, padding 8 top/bottom, 0 left/right, horizontal, gap 12, items centred vertically.
- Leading 40×40 circle at (0,12):
  - Expense: `tile` frame, circle, fill **Plain `color/bg/card` #F5F5F5 / On Card `color/bg/primary` white**, category icon 20×20 centred (10,22), `color/icon/primary`, stroke 1.25.
  - Payment: `avatar` = `Avatar / Circle` Size=40 Type=Art, fill Plain #F5F5F5 / On Card white; person art (Priya = `peep-head-priya.svg`, Rohan = `peep-head-rohan.svg`).
- `text` column (52,10), fills the width, vertical gap 2: `title` **Headline** `color/text/primary`, **1 line, truncate end** · `subtitle` **Subheadline** `color/text/secondary`, **max 2 lines, truncate end** · `detail` (hidden unless Show detail line) **Footnote** `color/text/tertiary`, text = `Detail`.
- Trailing `amount` column (hug, right-aligned), vertical gap 2: `amount` **Amount/Medium** (colour: **In → `color/text/primary` #0A0A0A; Out → `color/text/secondary` #6B6B6B**, and Out amounts start with "−") · `caption` (date) **Footnote** `color/text/tertiary` #A3A3A3 · `badge` (hidden; Show badge): `Badge / Pill` **Muted** on Plain / **On Card** on On Card, label "Pending" · `action` (hidden; Show action): small button "Restore" (Plain: `Button / Secondary` Small, fill `color/bg/card`; On Card: `Button / On Card` Small, white), 36 tall, padding 0/16.
- `unread` (hidden; Unread): 8×8 circle `color/bg/inverse` at the trailing edge (354,28).
- `divider` (hidden; Show divider): `Divider / Line` Inset=Leading, absolute at the bottom (0,63) 362×1: a 1 pt line `color/border/subtle` #EBEBEB starting at x52 (leading inset 52).

Default contents of the variants (Plain shown; On Card variants are identical except the circle is white and the badge/action use the On Card styles):
| Variant | Leading | Title | Subtitle | Amount | Date |
|---|---|---|---|---|---|
| Expense, In (13:381) | Food icon | Dinner at Olive Garden | You paid · 4 people | ₹2,800 (primary) | Today |
| Expense, Out (13:393) | Food icon (Home instance swaps to Bolt) | Electricity bill | Flat 302 · You owe | −₹450 (secondary) | 26 Sep |
| Payment, In (13:403) | Priya art | Priya paid you | UPI | ₹1,050 (primary) | Yesterday |
| Payment, Out (13:440) | Rohan art | You paid Rohan | UPI · Goa Trip | −₹1,400 (secondary) | Today |
On Card variant ids: Expense/In 102:953, Expense/Out 102:975, Payment/In 102:996, Payment/Out 102:1027.
No pressed state in Figma. Suggestion if rows become tappable: `color/bg/card-pressed`.

## 11. Card / Empty State (13:541). SwiftUI: `PBEmptyState`
> "Empty states on a #F5F5F5 card. First day: illustration, title, body, a primary (Add expense) and a secondary (Invite friends) action. Title, Body, Illustration, Show primary action and Show secondary action customise First day for other screens (06-19, 07-03, 09-02; hide both actions there when the tab bar ＋ is the action). All settled: the fixed calm message; the new props apply to First day only."

Properties: `Type` = First day | All settled · `Title` (default "Nothing here yet.") · `Body` (default "Add your first expense or invite a friend to get started.") · `Illustration` instance swap (default Illustration / Empty — First day 7:216) · `Show actions` / `Show primary action` / `Show secondary action` (all true). The text/illustration/action props apply to First day only.
Common geometry: width 362 (fills), height hugs; fill `color/bg/card` #F5F5F5, radius 20, **padding 24** (`space/24`), vertical, **gap 20** (`space/20`), children centred horizontally.
- `illustration` (61,24) **240×180** fixed, clips. On device this is a Rive slot (see below).
- `text` (24,224) fill (314), vertical gap 8, centred: `title` **Title/2** (Bold 24/30, ls −1.5 %) `color/text/primary`, centre-aligned, 30 tall · `body` **Body** (Regular 16/24) `color/text/secondary`, centre-aligned, wraps.
- `actions` (First day only; Show actions) (24,330) fill width, vertical gap 12:
  - `Button / Primary` Large, Leading icon = true, Icon / Plus: fill width × 52, capsule, `color/bg/inverse`, padding 0/24, content centred with gap 8: icon 20×20 `color/icon/inverse` + label **Button/Large** `color/text/inverse` "Add expense". Pressed `color/bg/inverse-pressed` #2B2B2B; Disabled `color/bg/disabled` #E0E0E0 + `color/text/disabled` #A3A3A3.
  - `Button / On Card` Large, Leading icon = true, Icon / User Add: fill width × 52, capsule, `color/bg/primary` white, padding 0/24, gap 8: icon 20×20 `color/icon/primary` + label Button/Large `color/text/primary` "Invite friends". Pressed `color/bg/card-pressed` #EBEBEB; Disabled: white fill + `color/text/disabled`.

| Type | Size | Illustration (Figma) | Rive on device | Title | Body | Actions |
|---|---|---|---|---|---|---|
| First day (13:479) | 362×470 | Illustration / Empty — First day (Open Doodles "laying"), 240×180 | `paybak-homefirstday.riv`, artboard `First Day` 240×180, SM `First Day`, VM `HomeFirstDay`/`Instance`: view = the slot (61,24,240,180) | Nothing here yet. | Add your first expense or invite a friend to get started. | Add expense (plus) + Invite friends (user-add) |
| All settled (13:501) | 362×310 | Illustration / Empty — All square (Open Doodles "meditating"), 240×180 | `paybak-home-allset.riv`, artboard `AllSquare` **264×204**, SM `AllSquare`, VM `AllSquare`/`Default`: view **centred on the slot at native size**, card-relative (49,12,264,204), not clipped | You’re all square. (curly ’) | No one owes anyone right now. | none |

Static fallbacks (vector, 240×180): `assets/images/empty-first-day.svg`, `assets/images/empty-all-square.svg`. Illustration colours: line `color/illustration/line` #0A0A0A, accent `color/illustration/tint` #EBEBEB.

## 12. Badge / Pill (11:46). SwiftUI: `PBBadge`
> "Status pill. Overdue (red) is reserved for overdue items. On Card = white pill for use on #F5F5F5 cards."

Properties: `Label` (default "Due Fri") · `Show icon` (false) · `Icon` instance swap (default Icon / Calendar) · `Style` = Muted | On Card | Inverse | Overdue.
Geometry: height **24** fixed, width hugs, capsule (`radius/full`), horizontal, padding 0 top/bottom, **10 left/right**, gap 4 (`space/4`), centred. Optional `icon` 14×14 at (10,5) before the label (hidden by default). `label` **Caption/1** (Manrope Bold 12 / 16, ls +1 % = 0.12 pt), at y4.

| Style | Fill | Label colour | Used on Home |
|---|---|---|---|
| Muted (11:22) | `color/bg/card` #F5F5F5 | `color/text/secondary` #6B6B6B | – (activity-row status chips on white) |
| On Card (11:28) | `color/bg/primary` #FFFFFF | `color/text/secondary` #6B6B6B | "Due Fri" (Goa Trip row) |
| Inverse (11:34) | `color/bg/inverse` #0A0A0A | `color/text/inverse` #FFFFFF | – |
| **Overdue** (11:40) | `color/bg/destructive` #C93636 | `color/text/inverse` #FFFFFF | **"Overdue 3 days"** (Rohan row): 110×24, text 90×16 at (10,4) |

Icon tint when shown: same as the label colour.

---

## 13. Sheet / Action Row (17:641). SwiftUI: `PBSheetRow`
> "Row in the Add sheet: 44pt icon tile + title/subtitle + chevron."

Properties: `Title` (default "Add expense") · `Subtitle` (default "Split a bill with friends or a group") · `Icon` instance swap (default Icon / Receipt) · `Show chevron` (true) · `State` = Default | Pressed.
Geometry: 354 × **72** fixed height, horizontal, padding 0 top/bottom, **12 left/right**, gap 12, items centred, radius 20 (`radius/card`).
- `tile` (12,14) **44×44**, radius **14** (`radius/tile`), clip, icon 24×24 centred at (22,24), `color/icon/primary`, stroke 1.5.
- `text` (68,14) fills, vertical gap 2: `title` **Headline** `color/text/primary` · `subtitle` (68,38) **Subheadline** `color/text/secondary`. Both 1 line.
- `chevron` Icon / Chevron Right **20×20** at (322,26), `color/icon/tertiary` #A3A3A3, stroke 1.25. Visible when Show chevron.

| State | Row fill | Tile fill |
|---|---|---|
| Default (17:623) | none (transparent) | `color/bg/card` #F5F5F5 |
| Pressed (17:632) | `color/bg/card` #F5F5F5 (radius 20) | `color/bg/primary` #FFFFFF |

## 14. Sheet / Action Sheet (17:643). SwiftUI: `PBAddSheet`
> "The ＋ Add sheet: floating white sheet with the kit grabber and glass close, and 4 actions." (Section caption: "Floating sheet inset 8pt, 40pt corners, white, on a 40% scrim. The grabber and glass close button are Apple iOS 27 kit instances. Rows: 44pt icon tile, title and subtitle, chevron.")

No properties. 386 × 394 (hugs its height). Placed with 8 pt insets left/right/bottom from the screen edges (x8, bottom 8).
- Fill `color/bg/primary` #FFFFFF, **radius 40** (`radius/sheet`) on all four corners, no stroke, no shadow.
- Vertical auto-layout, padding **8 top / 16 right / 28 bottom / 16 left**, gap 8, children centred.
  1. `grabber (kit)` (163,8) **60×4**, capsule, #CCCCCC (kit "Fills - Vibrant/Primary", LINEAR_BURN; nearest token `color/bg/indicator` #D1D1D1).
  2. `header` (16,20) 354×50 (hug height), horizontal, padding-left 4, centred: `title` "Add" (20,32) **Title/3** `color/text/primary` · spacer · `close (kit)` (320,20) **50×50** "Button - Liquid Glass - Symbol" Style=Glass: SF Symbol `xmark` Semibold 19 pt, #1A1A1A, glyph 19×19 centred. Kit glass layers: fills #000 25 % + #FFF 25 % + #444 60 % (linear dodge) + #F8F8F8 20 % (luminosity), shadows (0,8) blur 15 black 2 %, 0.5 pt ring #E8E8E8, side hairlines (±1.25, 0) spread −0.75 #D0D0D0, GLASS radius 6 (refraction 0.7, depth 30). On white it renders as a white disc with a light gray hairline ring. See screens-home.md 5.2 for iOS/Android recipes.
  3. `rows` (16,78) 354×288, vertical, gap 0: four Sheet / Action Row (Default):
     | Row | Icon | Title | Subtitle |
     |---|---|---|---|
     | Add expense | Icon / Receipt `receipt.svg` | Add expense | Split a bill with friends or a group |
     | Record payment | Icon / Exchange `exchange.svg` | Record payment | Log money you paid or received |
     | Lend money (IOU) | Icon / Lend `lend.svg` | Lend money (IOU) | Track a loan and when it’s due |
     | New group | Icon / Groups `groups.svg` | New group | Flatmates, a trip or a project |
- Height check: 8 + 4 + 8 + 50 + 8 + 288 + 28 = 394.

## 15. Sheet / Container (118:1017). SwiftUI: `PBSheet` (generic sheet shell; not used on Home, but it's the same visual language)
> "PBSheet (.presentationDetents) — Container for every picker and form sheet: kit grabber, a Title/3 title on the left and the kit glass xmark on the right, an optional search field (Show search; nested Control / Input Field exposed as “search”: set its Value#12:9 to “Search categories” etc.) and the native Content slot. Detent=Medium: 386 wide (inset 8, anchored 8 from the bottom), radius 40, hugs its content; the slot is 354 wide. Detent=Large: 402×804 from y 70, top radius 40, the slot fills the height and clips (lists scroll under the bottom edge); the slot is 370 wide. Content is a native SLOT: append the sheet body to the “Content” slot of the instance (or a local _Sheet / frame name instance). Never detach. Booleans: Show title, Show close, Show grabber, Show search. With Show title and Show close both off, the empty header still takes its two 8 gaps, so the content starts 28 below the top (08-10). SwiftUI: PBSheet"

Properties: `Title` (default "Paid by") · `Show title` / `Show close` / `Show grabber` (true) · `Show search` (false) · `Content` (slot) · `Detent` = Medium | Large.
| | Detent=Medium (118:972) | Detent=Large (118:996) |
|---|---|---|
| Size / placement | 386 wide, hugs height (e.g. 206 with a 100-tall slot); inset 8 left/right/bottom | 402 × 804, placed at y 70, full width |
| Corners | radius 40 all corners | radius 40 top-left/top-right, 0 bottom |
| Padding | 8 top, 16 left/right, **28 bottom** | 8 top, 16 left/right, **34 bottom** (`layout/home-indicator`); clips |
| Gap | 8 | 8 |
| Grabber | 60×4 at (163,8) | 60×4 at (171,8) |
| Header | 354×50 at (16,20): title Title/3 fills (hug height, clips), close 50×50 at (320,20); header justify MAX (end), padding-left 4 | 370×50 at (16,20), close at (336,20) |
| Search (optional) | Control / Input Field (Default, leading search icon, value "Search", no label/helper) 354×52 at (16,78) | 370×52 |
| Content slot | (16,78) 354 wide, hugs, clips | (16,78) 370 × fills the remaining height, clips |
Fill `color/bg/primary`. The same scrim (`color/bg/scrim` 40 %) and open/close motion as the Add sheet.

---

## 16. Materials summary (for implementers)
| Name | Where | Fill | Stroke | Shadow | Glass |
|---|---|---|---|---|---|
| Material/Glass | Tab bar | bg/glass #FFF @72 % | 1 inside, border/glass-highlight #FFF @60 % | (0,8) blur 32 #0A0A0A @10 % | radius 16, refraction 0.7, depth 30 |
| Material/Glass Small | Header 44 pt buttons | bg/glass | same | (0,4) blur 16 #0A0A0A @8 % | radius 6, refraction 0.4, depth 8 |
| Kit Liquid Glass Regular Small | Sheet ✕ (50 pt) | see 14 | 0.5 ring #E8E8E8 | (0,8) blur 15 black @2 % | radius 6, refraction 0.7, depth 30 |
| Scrim | Behind sheets | bg/scrim #0A0A0A @40 % | – | – | – |
SwiftUI shadow radius ≈ Figma blur ÷ 2. Compose `Modifier.dropShadow(shape, Shadow(radius = <Figma blur>.dp, offset = DpOffset(0.dp, y.dp), color = …))` takes the Figma blur directly.
