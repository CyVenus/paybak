# Paybak: Home screens spec (Figma page "04 Home" 3:5, section "Home" 24:2)

Screens: `homeActive` (24:5), `homeFirstDay` (24:326), `homeAllSettled` (24:414), `homeAddSheet` (24:520 + overlay 24:808).
Components used here are specced in `components-home.md` (Home-specific) and `components-core.md` (buttons, badge, avatar, brand; a separate spec).
Tokens, text styles and materials: `tokens.md`. Rive facts: `rive.md`. Screen ids and navigation: `flow.md`.

Reference PNGs (2x, 804×1748): `ref/homeActive.png`, `ref/homeFirstDay.png`, `ref/homeAllSettled.png`, `ref/homeAddSheet.png` (24:520, the sheet over Home Active), `ref/homeAddSheet-overlay.png` (24:808, the prototype overlay by itself: scrim + sheet on a transparent frame).
Node JSON for 24:5, 24:326, 24:414, 24:520 and 24:808: `.figma-cache/nodes/3-5.json` (regenerate with `tools/fetch_figma.py`). The `get_design_context` React/Tailwind output read while writing this spec wasn't kept.

## 0. Conventions used in this file

- Frame = 402 × 874 pt (iPhone 17 Pro). All `x, y` are **frame coordinates** (top-left of the frame is 0,0). Top safe-area inset = 62 (status bar), bottom inset = 34 (home indicator). Where it matters, I also give "SA+n" = n pt below the top safe-area edge (y − 62) and "↑n from bottom" = distance from the bottom screen edge (874 − y).
- The status bar (kit instance, 402×62 at y 0) and home indicator (kit instance, 402×34 at y 840; its 5 pt bar sits 8 pt above the screen bottom) are **system UI. Don't draw them.**
- Colours are token names from tokens.md with hex in brackets. Text styles are the tokens.md style names (Manrope). `ls` = letter spacing.
- Icons: `assets/icons/<name>.svg` (24×24 viewBox, stroke 1.5, colour #0A0A0A; tint them). HugeIcons stroke width scales with the drawn size: at 24 pt the stroke is 1.5, at 20 pt 1.25 and at 16 pt 1.0. That's exactly what you get by scaling the 24×24 SVG, so don't change stroke widths. **Only exception: the ＋ in the tab-bar add button uses stroke 2 at 24 pt**. Use `assets/images/add-button-plus.svg` (white, stroke 2) for that one.
- Sample data (Active) is Figma's data verbatim. The minus sign in amounts is **U+2212 "−"** (not a hyphen), the rupee is U+20B9 "₹", the separator in subtitles is U+00B7 "·". "You're owed" uses a **straight** apostrophe U+0027 in Figma; "You’re all square." and "it’s" use the curly U+2019. Keep all of these exactly.

---

## 1. Shared Home scaffold (all four states)

Frame auto-layout: vertical column, padding top 62 (`layout/status-bar`), left/right 20 (`layout/screen-margin`), bottom 34 (`layout/home-indicator`), gap 24 (`layout/section-gap`) between sections. Background `color/bg/primary` (#FFFFFF). The frame clips its content.

Implementation shape (both platforms):
```
ZStack / Box (fill screen, bg/primary)
 ├─ vertical ScrollView / LazyColumn (content column: side padding 20, top = safe-area top, section gap 24)
 │    ├─ Home header (Nav Header Type=Home)            ← scrolls with content
 │    └─ state content (Active sections | Empty-state card)
 ├─ bottom scroll-edge fade (Active only; see 2.6)       ← not interactive, above content
 ├─ floating glass tab bar                                ← above content, fixed
 └─ Add-sheet overlay (scrim + floating sheet) when open  ← above everything
```
- Content **scrolls under the glass tab bar** (section caption in Figma: "Content scrolls under the glass tab bar."). Figma defines no prototype scrolling (`overflowDirection = NONE`), so the scroll behaviour below is our choice.
- Bottom content inset (not in Figma, recommended): enough that the last row can scroll fully above the tab bar: (screen bottom − tab-bar top) + 24 = 83 + 24 = **107 pt from the screen bottom** (= bottom safe inset 34 + 73). The status bar area: Figma doesn't define a scrolled state for Home. Recommended: keep a solid `bg/primary` backdrop behind the status bar (0–62) so scrolled rows don't collide with the clock. Don't add the "Inline" nav bar variant on Home.
- The header is **part of the scroll content** (in Figma it's the first child of the auto-layout column, not pinned).

### 1.1 Home header: `Navigation / Nav Header`, Type=Home (instance "Header"), x20 y62 w362 h94 (SA+0)
Vertical stack, gap 12 (`space/12`), width = screen − 40.
1. **Toolbar row**, x20 y62 w362 h44, horizontal, gap 8 (`space/8`), items vertically centred:
   - **Logo** (`Brand / Logo`, Layout=Horizontal), x20 y70 104×28 (vertically centred in the 44 row), horizontal gap 8:
     - App mark (`Brand / App Mark` Size=28): x20 y70 28×28, fill `color/bg/inverse` (#0A0A0A), corner radius 6.26 (6.2636), white "P" glyph. File `assets/brand/app-mark-28.svg` (whole lockup: `assets/brand/logo-horizontal.svg`).
     - Wordmark text "Paybak", x56 y72 68×24, style **Brand/Wordmark S** (ExtraBold 20/24, ls −3 % = −0.6 pt), `color/text/primary` (#0A0A0A). Live text, not an image.
     - **Debug-only**: long-press on the logo opens the debug menu (flow.md).
   - Spacer (fills).
   - **AI / "Ask Paybak" button** (`Button / Icon`, Style=Glass, State=Default, Badge=false): x286 y62, 44×44 (`size/tap`), circle (`radius/full`). Icon `sparkles.svg` 24×24 at x296 y72 (centred), `color/icon/primary` (#0A0A0A). Material/Glass Small (see 1.3). Tap: nothing yet (the Figma description says it "opens Ask Paybak", not built).
   - **Bell button** (`Button / Icon`, Style=Glass, State=Default, **Badge=true**): x338 y62, 44×44, circle. Icon `bell.svg` 24×24 at x348 y72, `color/icon/primary`. **Unread dot**: ellipse 10×10 at x364 y71 (button-relative x26 y9, absolutely positioned; it overlaps the top-right of the bell glyph), fill `color/bg/inverse` (#0A0A0A), **2 pt stroke OUTSIDE** in `color/bg/primary` (#FFFFFF), so the visible disc incl. ring is 14×14 at x362 y69. Tap: nothing yet.
   - Gap between the two glass buttons: 8. The right edge of the bell = x382 (screen margin 20).
2. **Greeting** text, x20 y118 w362 h38 (SA+56), style **Title/1** (ExtraBold 32/38, ls −2 % = −0.64 pt), `color/text/primary`, left aligned, fills the width, wraps (auto height) if the name is long. Figma text: "Good evening, Arjun". Runtime: "Good morning/afternoon/evening, {first name}" per flow.md (5–12 morning, 12–17 afternoon, else evening).

Header bottom = y156. The next section starts at y180 (gap 24).

### 1.2 Floating tab bar: `Navigation / Tab Bar`, Active=Home (instance "Tab bar")
- Frame: **x20 y791 w362 h62** (`size/tabbar`), absolutely positioned (not in the scroll column). Horizontally centred with 20 pt side margins. **Bottom edge y853 = 21 pt above the screen bottom** = 13 pt *below* the bottom safe-area edge (840). The home-indicator bar (≈ y 861–866) sits in the 21 pt gap under the capsule, clear of it.
  - Positioning rule to use in code: `bottom = 21 pt from the physical screen bottom` on iPhones with a 34 pt inset. General: `bottom = max(21, bottomInset − 13)` (Android 3-button nav, 48 dp inset → 35 dp; gesture nav ≈ 24 dp → 21 dp).
- Shape: capsule (`radius/full`), fill `color/bg/glass` (#FFFFFF @72 %), 1 pt **inside** stroke `color/border/glass-highlight` (#FFFFFF @60 %), effect style **Material/Glass**: drop shadow x0 y8 blur 32 spread 0 #0A0A0A @10 % + GLASS (refraction 0.7, depth 30, radius 16).
  - iOS: `.glassEffect(.regular, in: .capsule)` (+ optional shadow `.shadow(color: #0A0A0A.opacity(0.10), radius: 16, y: 8)`; SwiftUI radius ≈ Figma blur/2).
  - Android: fill white 72 %, 1 dp white-60 % border, drop shadow (0, 8 dp, blur 32 dp, #0A0A0A 10 %); a real backdrop blur is optional.
- Layout: horizontal, padding 5 (+1 pt stroke counted inside → content inset 6 pt left/right), `SPACE_BETWEEN`, items centred vertically. Five children, 6.5 pt apart:
  | # | Child | Frame rect (x, y, w, h) | Content |
  |---|---|---|---|
  | 1 | home, `Tab Bar Item` State=**Active** | 26, 796, 68, 52 | icon `home.svg` 24 at x48 y802.5, label "Home" at y828.5 |
  | 2 | groups, State=Inactive | 100.5, 796, 68, 52 | icon `groups.svg` at x122.5 y802.5, label "Groups" |
  | 3 | add, `Button / Add` State=Default | 175, 796, 52, 52 | black circle, `add-button-plus.svg` 24 at x189 y810 |
  | 4 | activity, State=Inactive | 233.5, 796, 68, 52 | icon `activity.svg` at x255.5 y802.5, label "Activity" |
  | 5 | profile, State=Inactive | 308, 796, 68, 52 | icon `profile.svg` at x330 y802.5, label "Profile" |
- Tab item (68×52, capsule): vertical stack, gap 2 (`space/2`), centred: icon 24×24 then label (**Caption/2**: SemiBold 11/13, ls +1 % = 0.11 pt). The stack is 39 tall, so the icon's top is 6.5 pt below the item top.
  - Active: background `color/bg/selected` (#0A0A0A @6 %) pill = the whole 68×52 capsule; icon `color/icon/primary` (#0A0A0A); label `color/text/primary` (#0A0A0A).
  - Inactive: no background; icon `color/icon/secondary` (#6B6B6B); label `color/text/secondary` (#6B6B6B).
  - The **same outline icon files** are used for selected and unselected; only the tint changes (no filled variants in Figma).
- Add button: 52×52 (`size/add-button`), circle, fill `color/bg/inverse` (#0A0A0A); pressed `color/bg/inverse-pressed` (#2B2B2B). Icon ＋ 24 pt white (`color/icon/inverse`), stroke 2.
- Interaction:
  - ＋ (the Figma hotspot is 52×52 at x175 y796, exactly the add button) → opens the Add sheet overlay (section 5). Prototype: `ON_CLICK → OPEN OVERLAY 24:808`, transition MOVE_IN, direction TOP (i.e. moving up from the bottom), EASE_OUT, 300 ms.
  - Home: selected, no-op. Groups / Activity / Profile: inert (flow.md). No pressed state is defined for tab items.
- Accessibility: label each item ("Home", "Groups", "Add", "Activity", "Profile"); mark Home selected.

### 1.3 Glass materials (tokens.md + exact Figma values)
- **Material/Glass** (tab bar): shadow (0, 8) blur 32 #0A0A0A @10 %; GLASS radius 16, refraction 0.7, depth 30.
- **Material/Glass Small** (header 44 pt buttons): shadow (0, 4) blur 16 #0A0A0A @8 %; GLASS radius 6, refraction 0.4, depth 8. Fill `color/bg/glass` #FFFFFF @72 %, 1 pt inside stroke `color/border/glass-highlight` #FFFFFF @60 %.
  - iOS: `.glassEffect(.regular.interactive(), in: .circle)` on a 44×44 frame.
  - Android: white 72 % circle + 1 dp white-60 % border + shadow (0, 4 dp, blur 16 dp, #0A0A0A 8 %). Pressed (Figma `Button / Icon` Style=Glass State=Pressed): fill becomes `color/bg/card` (#F5F5F5), same stroke and shadow.
  - On the white Home background the glass buttons read as white discs with a soft shadow (see `ref/homeActive.png`).

---

## 2. `homeActive`: Home — Active (24:5)

Purpose: "Owed and owing at a glance, what needs attention next, and recent activity. Content scrolls under the glass tab bar." (Figma caption). The default screen for a user with data. Sample data below is static and verbatim.

Background `color/bg/primary`. Top-to-bottom (frame y, then safe-area offset):

### 2.1 Header: y62–156 (SA+0). See 1.1.

### 2.2 Balance summary: `Card / Balance Summary` (Show Settle up = true), x20 y180 w362 h180 (SA+118)
Vertical stack, gap 12.
- **Cards row** y180 h116: horizontal, gap 12, two `Card / Balance` instances each **FILL** width → 175×116 each.
  - **Left card: Type=Owed**, x20 y180 175×116. Fill `color/bg/card` (#F5F5F5), radius 20 (`radius/card`), padding 16 (`layout/card-padding`) all sides, vertical gap 12. No border, no shadow.
    - Top row (x36 y196 w143 h20): horizontal, gap 6, centred: icon `money-in.svg` **16×16** at x36 y198, tint `color/icon/secondary` (#6B6B6B); label "You're owed" (Subheadline: Medium 14/20, `color/text/secondary` #6B6B6B), fills the remaining width; chevron `chevron-right.svg` **16×16** at x163 y198, tint `color/icon/tertiary` (#A3A3A3).
    - Amount block (x36 y228), vertical gap 2: amount "+₹2,900" (**Amount/Large**: ExtraBold 26/32, ls −2 % = −0.52 pt), `color/text/primary` (#0A0A0A), 104×32; caption "from 4 people" (Footnote: Medium 13/18), `color/text/tertiary` (#A3A3A3), at y262.
  - **Right card: Type=Owe**, x207 y180 175×116. Same geometry. Icon `money-out.svg` 16 (icon/secondary) at x223 y198; label "You owe"; chevron at x350 y198; amount "−₹1,850" in `color/text/secondary` (#6B6B6B) (gray because it's money you owe); caption "across 2 groups" (text/tertiary).
  - Tap either card → per-person breakdown (**later phase**; do nothing now). Figma defines no pressed state for the card; recommended pressed fill `color/bg/card-pressed` (#EBEBEB).
- **Settle up** button (`Button / Primary` Size=Large, State=Default, no icon): x20 y308 w362 h52 (`size/button-lg`), full width, capsule, fill `color/bg/inverse` (#0A0A0A), label "Settle up" (**Button/Large**: SemiBold 17/22, ls −0.5 %), `color/text/inverse` (#FFFFFF), centred (label at x164.5 y323). Pressed: `color/bg/inverse-pressed` (#2B2B2B). Tap → Settle Up flow (**later phase**; do nothing).
- Annotation: "Tap a balance card → per-person breakdown (later phase). Settle up → Settle Up flow (later phase)."

### 2.3 "Due soon" section: x20 y384 w362 h228 (SA+322), vertical gap 12
- **Section header** (`Row / Section Header`, Show action = **false**): x20 y384 362×32. Title "Due soon" (**Title/3**: Bold 20/26, ls −1 % = −0.2 pt), `color/text/primary`, at y387 (vertically centred in 32).
- **Rows** stack y428, vertical gap 8:
  1. **Rohan (overdue)**: `Row / Attention` State=Overdue, x20 y428 w362 h88. Fill `color/bg/card`, radius 20, padding 12 top/bottom, 16 left/right, horizontal gap 12, items vertically centred.
     - Avatar (`Avatar / Circle` Size=40 Type=Art): 40×40 circle at x36 y452, fill `color/bg/primary` (**white**, because it sits on a card), clips the art. Art = "Art / Peep Head / Rohan" (Open Peeps peep-21) → `assets/images/peep-head-rohan.svg` (120×120 art, scale to 40×40 and clip to the circle; the art has a transparent background).
     - Text column (fills, x88): vertical gap 6.
       - Title row (y446, h22), horizontal gap 6, centred: "Rohan" (Headline: SemiBold 16/22, ls −0.25 %, `color/text/primary`) + "Movie tickets" (Subheadline, `color/text/secondary`) at x143.
       - Badge (`Badge / Pill` Style=**Overdue**), x88 y474 110×24: capsule, fill `color/bg/destructive` (#C93636), padding 0/10, label "Overdue 3 days" (**Caption/1**: Bold 12/16, ls +1 % = 0.12 pt), `color/text/inverse` (#FFFFFF). The only red element on Home.
     - Right column (hug, right-aligned), vertical gap 6, x280 y440: amount "₹800" (**Amount/Medium**: Bold 17/22, ls −0.5 %), `color/text/primary`, right-aligned at x322; action button (`Button / On Card` Size=Small): x280 y468 86×36 (`size/button-sm`), capsule, fill `color/bg/primary` (white), padding 0/16, label "Remind" (**Button/Small**: SemiBold 15/20, ls −0.25 %), `color/text/primary`. Pressed fill `color/bg/card-pressed` (#EBEBEB). Tap: nothing yet (annotation: "Remind sends a polite pre-written nudge"; later phase).
     - Annotation: "Red = overdue only. Remind sends a polite pre-written nudge."
  2. **Goa Trip (due Fri)**: `Row / Attention` State=Due soon, x20 y524 w362 h88. Same card geometry.
     - Avatar (`Avatar / Circle` Size=40 Type=Icon): 40×40 circle at x36 y548, fill `color/bg/primary` (white), icon `groups.svg` **20×20** centred (x46 y558), tint `color/icon/primary`.
     - Title row (y542): "Goa Trip" (Headline, primary) + "Your share" (Subheadline, secondary) at x157.
     - Badge (`Badge / Pill` Style=**On Card**): x88 y570 61×24, fill `color/bg/primary` (white), label "Due Fri" (Caption/1, `color/text/secondary` #6B6B6B).
     - Right column x290 y536: amount "₹1,400" (Amount/Medium, **text/primary**); button "Settle" (`Button / On Card` Small, white) x290 y564 76×36. Tap: nothing yet.

### 2.4 "Recent activity" section: x20 y636 w362 h236 (SA+574), vertical gap 4
- **Section header** (`Row / Section Header`, Show action = **true**): x20 y636 362×32. Title "Recent activity" (Title/3) at y639. Trailing text button "See all" (`Button / Text` Style=Secondary): 47×44 hit area at x335 y630 (the 44 pt button overflows the 32 pt row by 6 above and below, centred on it), label Button/Small `color/text/secondary` (#6B6B6B) at y642, no chevron. Pressed = 50 % opacity. Tap: nothing (Activity tab is inert).
- **Rows** (`Row / Activity`, Surface=Plain), each full width, min height 64, padding 8 top/bottom, 0 left/right, horizontal gap 12, vertically centred. Text column: title (Headline, `color/text/primary`, 1 line, truncate with "…") above subtitle (Subheadline, `color/text/secondary`, max 2 lines, truncate), gap 2. Trailing column (hug, right-aligned), gap 2: amount (Amount/Medium) above date (Footnote, `color/text/tertiary` #A3A3A3).
  | # | Frame y | Leading (40×40 circle at x20, y+12) | Title | Subtitle | Amount (colour) | Date |
  |---|---|---|---|---|---|---|
  | 1 | 672–736 | Type=Expense: circle `color/bg/card` (#F5F5F5) + `food.svg` 20×20 (fork & knife), `color/icon/primary`, at x30 y694 | Dinner at Olive Garden | You paid · 4 people | ₹2,800 (`text/primary`, Direction=In) | Today |
  | 2 | 740–804 | Type=Payment: `Avatar / Circle` 40 Art, circle `color/bg/card` (#F5F5F5) + Priya art `assets/images/peep-head-priya.svg` (Open Peeps peep-93) | Priya paid you | UPI | ₹1,050 (`text/primary`, In) | Yesterday |
  | 3 | 808–872 | Type=Expense: circle `color/bg/card` + `bolt.svg` 20×20, `color/icon/primary`, at x30 y830 | Electricity bill | Flat 302 · You owe | −₹450 (`text/secondary`, Direction=Out) | 26 Sep |
  - Row 3 lies **under the tab bar and the fade** (tab bar y791–853, fade from y724). It is only partly visible in the reference, and fully visible once scrolled.
  - Text column x72 (= 20 + 40 + 12); title at row-y+10, subtitle at row-y+34; amount at row-y+11, date at row-y+35.
  - Rows are not tappable in this phase. No dividers (Show divider = false), no unread dot, no badge.

### 2.5 Tab bar: see 1.2 (Active=Home). Annotation: "＋ opens the Add sheet as an overlay."

### 2.6 Scroll-edge fade ("Scroll edge (fade)", rectangle 25:795)
- x0 y724 w402 h150 (the bottom 150 pt of the screen, down to the bottom edge), full width, drawn **above the content and below the tab bar**, not interactive.
- Vertical linear gradient (top → bottom): #FFFFFF @0 % at 0 % → #FFFFFF @85 % at 45 % (≈ y791.5, the tab-bar top) → #FFFFFF @100 % at 100 % (y874).
- Present in Active and in the Action-sheet background (same content). Not in First day / All settled (nothing reaches it). White on white is invisible, so it's safe to keep it always.

### 2.7 Assets used (Active)
`assets/brand/app-mark-28.svg` (or `logo-horizontal.svg`), `assets/icons/sparkles.svg`, `bell.svg`, `money-in.svg`, `money-out.svg`, `chevron-right.svg`, `groups.svg`, `food.svg`, `bolt.svg`, `home.svg`, `activity.svg`, `profile.svg`, `assets/images/add-button-plus.svg`, `assets/images/peep-head-rohan.svg`, `assets/images/peep-head-priya.svg`.

---

## 3. `homeFirstDay`: Home — First day (24:326)

Purpose: "New account. One clear next step: add an expense or invite friends." Default Home after setup.

Frame children: Header, Empty state, Tab bar, ＋ hotspot (no balance, sections or fade).
- **Header**: identical to 1.1 (y62–156).
- **Empty-state card** (`Card / Empty State` Type=First day; Show actions / Show primary action / Show secondary action = true): **x20 y180 w362 h470** (SA+118; bottom at y650). Fill `color/bg/card` (#F5F5F5), radius 20, padding 24 (`space/24`) all sides, vertical stack, gap 20 (`space/20`), children centred horizontally.
  1. **Illustration slot**: **x81 y204 w240 h180** (card-relative x61 y24; horizontally centred). Figma: "Illustration / Empty — First day" (Open Doodles "laying"). **Replace with Rive**: `paybak-homefirstday.riv`, artboard **`First Day`** (240×180, exact match), state machine **`First Day`**, view model `HomeFirstDay` (instance `Instance`), auto-bind the default instance. Fit `contain`, alignment centre, view size 240×180. `reduceMotion` ← OS Reduce Motion. The file handles its own tap (tapCharacter/characterTapped); pass touches through, don't fire triggers yourself; optional light haptic on `characterTapped`. Static fallback / preview art: `assets/images/empty-first-day.svg` (240×180).
  2. **Text** (x44 y404 w314, gap 8, centred): title "Nothing here yet." (**Title/2**: Bold 24/30, ls −1.5 % = −0.36 pt), `color/text/primary`, centre-aligned, h30; body "Add your first expense or invite a friend to get started." (**Body**: Regular 16/24), `color/text/secondary` (#6B6B6B), centre-aligned, wraps to **2 lines** at 314 width (h48, y442–490).
  3. **Actions** (x44 y510 w314, vertical gap 12, full inner width):
     - **"Add expense"** (`Button / Primary` Large, Leading icon = true): x44 y510 314×52, capsule, fill `color/bg/inverse` (#0A0A0A), padding 0/24, content centred with gap 8: `plus.svg` **20×20** tinted `color/icon/inverse` (#FFFFFF) at x135 y526, label "Add expense" (Button/Large, `color/text/inverse`) at x163 y525. Pressed `color/bg/inverse-pressed` (#2B2B2B). Tap: open the Add sheet (section 5) per flow.md ("do nothing yet, or open the Add sheet").
     - **"Invite friends"** (`Button / On Card` Large, Leading icon = true): x44 y574 314×52, capsule, fill `color/bg/primary` (white), padding 0/24, gap 8: `user-add.svg` 20×20 tinted `color/icon/primary` at x134.5 y590, label "Invite friends" (Button/Large, `color/text/primary`) at x162.5 y589. Pressed `color/bg/card-pressed` (#EBEBEB). Tap: nothing yet.
- **Tab bar**: 1.2 (Home active). ＋ hotspot same as Active (opens the Add sheet).
- White space from y650 to the tab bar. Nothing scrolls under the tab bar at default size; keep the scroll container anyway (Dynamic Type).

Assets: header and tab-bar assets (1.1/1.2), `plus.svg`, `user-add.svg`, `paybak-homefirstday.riv` (fallback `assets/images/empty-first-day.svg`).

---

## 4. `homeAllSettled`: Home — All settled (24:414)

Purpose: "Nothing owed either way. A calm confirmation — no extra actions."

- **Header**: 1.1.
- **Empty-state card** (`Card / Empty State` Type=**All settled**): **x20 y180 w362 h310** (bottom y490). Same card styling as First day (bg/card, radius 20, padding 24, gap 20, centred). **No buttons.**
  1. **Illustration slot** (layout): **x81 y204 w240 h180** (card-relative x61 y24). Figma: "Illustration / Empty — All square" (Open Doodles "meditating").
     **Rive**: `paybak-home-allset.riv`, artboard **`AllSquare`**, state machine **`AllSquare`**, view model `AllSquare` (instance `Default`), trigger `tapped` handled by the file's own listener; `reduceMotion` ← OS setting.
     **Size mismatch**: the artboard is **264×204**, not 240×180. I rendered it and it draws the meditating character at the **same scale** as the Figma art (≈202 pt wide), with about 12 pt of extra room on every side for sparkles and a floor shadow. So **keep the 240×180 layout slot** (card stays 362×310) and draw the Rive view at its native **264×204, centred on the slot**: frame rect **x69 y192 w264 h204** (card-relative x49 y12), overflowing the slot by 12 on each side. Don't clip it to the slot. It stays inside the card (12 pt below the card top, 8 pt above the title at y404; 49 pt from the card's left edge). Fit `contain`, centre. Don't scale it down into 240×180, because the character would shrink to ~91 %.
     Static fallback art: `assets/images/empty-all-square.svg` (240×180).
  2. **Text** (x44 y404 w314, gap 8, centred): title "You’re all square." (Title/2, `color/text/primary`, centred, h30; **curly ’**); body "No one owes anyone right now." (Body, `color/text/secondary`, centred, 1 line, y442 h24).
- **Tab bar**: 1.2. ＋ hotspot → Add sheet.

Differences vs First day: card height 310 (vs 470), different illustration/Rive file, different title and body, **no action buttons**, the text body is 1 line.

---

## 5. `homeAddSheet`: Home — ＋ Action sheet (24:520) and Overlay — Add sheet (24:808)

Purpose: "Tap ＋ in the tab bar. Floating sheet over a 40% scrim; ✕ or the scrim closes it."
24:520 = the Home Active screen (identical content, incl. the fade and the tab bar; the ＋ hotspot is absent) with the scrim and sheet on top. 24:808 = the prototype overlay itself (transparent 402×874 frame containing the scrim, the sheet and a ✕ hotspot). Use 24:520 as the visual target and 24:808 for the interactions.
The debug start screen `homeAddSheet` = Home Active with the sheet already open.

### 5.1 Scrim
Rectangle x0 y0 w402 h874 (full screen, also under the status bar), fill `color/bg/scrim` (#0A0A0A @40 %). Covers the tab bar and all content. Tap on the scrim → close the sheet (Figma: `ON_CLICK → CLOSE` overlay).

### 5.2 Floating sheet (`Sheet / Action Sheet`, instance "Add sheet")
- Rect: **x8 y472 w386 h394** → left/right inset 8, **bottom edge y866 = 8 pt above the screen bottom** (26 pt below the bottom safe-area edge; the home-indicator bar is drawn by the system over the sheet's bottom area). Height hugs the content (394). Constraints: stretch horizontally, pinned to the bottom.
- Fill `color/bg/primary` (#FFFFFF), **radius 40 on all four corners** (`radius/sheet`), no border, no shadow (the scrim provides separation).
- Auto-layout: vertical, padding **top 8, left 16, right 16, bottom 28**, gap 8 (`space/8`), children centred horizontally.
  1. **Grabber** (Apple kit "Grabber", Mode=Light): 60×4 at x171 y480 (centred; SA-independent), radius 100 (capsule), fill #CCCCCC (kit colour "Fills - Vibrant/Primary", LINEAR_BURN blend; on white it renders as solid #CCCCCC). Closest token: `color/bg/indicator` (#D1D1D1). It's decorative (Figma has no drag-to-dismiss). Recommended: also support a swipe-down dismiss.
  2. **Header row**: x24 y492 w354 h50, horizontal, padding-left 4, items centred.
     - Title "Add" (**Title/3**: Bold 20/26, ls −0.2 pt), `color/text/primary`, at x28 y504.
     - Spacer.
     - **Close button** (Apple kit "Button - Liquid Glass - Symbol", Style=Glass, enabled): **50×50 circle at x328 y492** (right edge x378 = sheet right − 16). Glyph: SF Symbol **`xmark`**, SF Pro Semibold 19 pt (line 22), colour #1A1A1A (kit "Labels - Vibrant - Controls/Primary"). Measured in the reference: the ✕ glyph is **19×19 pt**, centred, stroke ≈ 2 pt. The button reads as a white disc with a hairline light-gray ring (#E8E8E8, 0.5 pt) and a very faint shadow (0, 8, blur 15, black 2 %).
       - iOS: `Button { dismiss } label: { Image(systemName: "xmark").font(.system(size: 19, weight: .semibold)).foregroundStyle(Color(hex: 0x1A1A1A)) }.frame(width: 50, height: 50).glassEffect(.regular.interactive(), in: .circle)` (or `.buttonStyle(.glass)` with a circle shape).
       - Android: 50 dp circle, fill white, 0.5 dp #E8E8E8 border, shadow (0, 8 dp, blur 15 dp, black 2 %); icon `assets/icons/close.svg` drawn at **38×38 dp** (its 12-unit glyph becomes 19 dp, stroke 2.375) tinted #1A1A1A, centred. Ripple/pressed: fill `color/bg/card` (#F5F5F5).
       - Tap → close the sheet (Figma: "✕ hotspot" 50×50 at x328 y492, `ON_CLICK → CLOSE`). Accessibility label "Close".
  3. **Rows** (vertical, gap 0): x24 y550 w354, four `Sheet / Action Row` rows, **72 pt each** (y550, 622, 694, 766; bottom y838; plus the sheet's 28 bottom padding = 866). Row: horizontal, padding 0/12, gap 12, items centred, radius 20 (`radius/card`, only visible when pressed).
     - Icon tile: 44×44, radius 14 (`radius/tile`), fill `color/bg/card` (#F5F5F5), at row-x+12 (x36), row-y+14; icon 24×24 centred (x46), tint `color/icon/primary` (#0A0A0A), stroke 1.5.
     - Text (fills, x92): title (Headline: SemiBold 16/22, `color/text/primary`) at row-y+14; subtitle (Subheadline: Medium 14/20, `color/text/secondary`) at row-y+38; gap 2; both single line.
     - Chevron `chevron-right.svg` **20×20** at x346 (row right − 12), row-y+26, tint `color/icon/tertiary` (#A3A3A3).
     - **Pressed** (component State=Pressed): row fill `color/bg/card` (#F5F5F5) with radius 20 and the tile turns `color/bg/primary` (white).
     | Row | y | Icon file | Title | Subtitle |
     |---|---|---|---|---|
     | 1 | 550 | `receipt.svg` (Icon / Receipt, HugeIcons invoice-01) | Add expense | Split a bill with friends or a group |
     | 2 | 622 | `exchange.svg` (Icon / Exchange, money-exchange-01) | Record payment | Log money you paid or received |
     | 3 | 694 | `lend.svg` (Icon / Lend, hand-coins) | Lend money (IOU) | Track a loan and when it’s due |
     | 4 | 766 | `groups.svg` (Icon / Groups, user-group) | New group | Flatmates, a trip or a project |
     (Row 3 subtitle has a **curly ’**.) Row tap: this phase does nothing except close the sheet (flow.md).
     Note: the `get_design_context` output for 24:520 and 24:808 showed the same image constant for rows 1–3. That's a code-gen dedup artefact. The node tree confirms the four different icons above.
- Annotation on the sheet: "✕ or tapping the scrim closes the sheet."

### 5.3 Motion (open/close)
- Figma prototype: ＋ → OPEN OVERLAY, **MOVE_IN from the bottom (direction TOP), EASE_OUT, 300 ms**, overlay position CENTER (full-frame overlay), no overlay background (the scrim is part of the overlay), background interaction NONE. The scrim and ✕ both `CLOSE`. No keyframe animation is defined (`get_motion_context` returns nothing for all Home frames).
- Native recommendation (matches the prototype, feels native):
  - Open: the scrim fades 0 → 40 % over 250–300 ms (ease-out) while the sheet slides up from off-screen (translateY = sheet height + 8 → 0) with ease-out 300 ms, **or** a spring (iOS `.spring(response: 0.35, dampingFraction: 0.85)`; Compose `spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow)`).
  - Close (✕, scrim tap, Android back, swipe down): reverse, ~250 ms ease-in; the scrim fades out.
  - Reduce Motion: cross-fade only (no slide).
- iOS option: a native `.sheet` with detents would give a system glass sheet, not this white inset card with a custom 60×4 grabber. **A custom overlay (ZStack) is recommended** to match Figma exactly. Android: a custom overlay composable (not ModalBottomSheet, whose shape and insets differ) + `BackHandler` to close.
- While open: the content underneath doesn't scroll; the status bar stays dark text on the dimmed background.

### 5.4 Assets used (sheet)
`assets/icons/receipt.svg`, `exchange.svg`, `lend.svg`, `groups.svg`, `chevron-right.svg`, `close.svg` (Android close glyph; iOS uses SF Symbol `xmark`).

---

## 6. State differences at a glance

| | Active (24:5) | First day (24:326) | All settled (24:414) | ＋ sheet (24:520) |
|---|---|---|---|---|
| Header | same | same | same | same (under the scrim) |
| Body | Balance summary (y180–360), Due soon (y384–612), Recent activity (y636–872, overflows) | Empty card First day 362×470 at y180, Rive `First Day` 240×180 at x81 y204, 2 buttons | Empty card All settled 362×310 at y180, Rive `AllSquare` 264×204 at x69 y192 (slot 240×180 at x81 y204), no buttons | = Active |
| Scroll-edge fade | yes (y724–874) | no | no | yes |
| Tab bar | Home active | Home active | Home active | Home active, under the scrim |
| ＋ hotspot | yes → overlay | yes → overlay | yes → overlay | n/a (sheet open) |
| Overlay | – | – | – | scrim 40 % + sheet x8 y472 386×394 |

Choosing the state at runtime: First day = no groups/expenses yet (after onboarding); Active = sample data; All settled = has history but zero balances. For now the state comes from the debug menu / `-startScreen` (flow.md). The default after onboarding is First day.

## 7. Other frames in the section (not in scope)
- "Home — Confirm payment" (167:11424) is also in the Home section and is a prototype flow starting point ("When a friend records a payment to you, a confirm card appears above the balances, and Home keeps +₹2,900 until you confirm. The card pushes Recent activity under the glass tab bar."). flow.md doesn't list it, so I haven't specced it.
