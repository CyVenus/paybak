# Paybak: Home v2 — change report (pages 03 + 04), `homeConfirmPayment`, and Home as the tab-app root

Figma file `2SPNUpHlG8bCO62YfwRuRi`, pages "03 Launch & Onboarding" (3:4) and "04 Home" (3:5). Read on 1 Oct 2026, 00:20–00:40 IST, read-only.

This file **adds to** `screens-home.md` (it doesn't replace it). The first Home spec is still right for everything this file doesn't mention. Where the two disagree, this file wins, because it reflects the current Figma.

Contents:
1. Change report: every page-03/04 frame that has a reference was re-rendered at 2× and pixel-diffed against `ref/`.
2. Home is now the root of a tab app: where every Home element navigates (from the new prototype reactions and from the notes on pages 06, 07, 08, 09, 11 and 12).
3. `homeConfirmPayment`: full spec of the new frame "Home — Confirm payment" (167:11424), plus the states around it that are drawn on page 08.
4. Designer notes, verbatim: the Home section, per frame, instance annotations, and the notes on other pages that define Home behaviour.
5. Assets, files written, and open issues.

Conventions are the same as `screens-home.md` §0: frame = 402 × 874 pt, all `x, y` are frame coordinates, colours are tokens from `tokens.md`, text styles are the `tokens.md` names, the status bar (0–62) and home indicator (840–874) are system UI. Minus in amounts is U+2212 "−", rupee U+20B9, separator U+00B7 "·".

---

## 1. Change report (pages 03 and 04)

### 1.1 Method
- **New renders:** each section was exported once at 2× with `download_assets` (`defaultScale 2`, PNG), the same export path the original refs came from: "Launch & Onboarding" (22:29), "Sign in & Setup" (39:353) and "Home" (24:2). The exports carry an 80 px (40 pt) margin around the section. I cropped every frame at `(frameX·2 + 80, frameY·2 + 80, 804 × 1748)`. A ±3 px offset search found the best alignment at offset 0 for all 18 frames, so the crops are exact frame renders.
- **Diff:** alpha is flattened on white, then per-pixel max channel difference. A pixel counts as changed above 24/255. The changed pixels are clustered into boxes (reported in pt). Everything below 24/255 was also checked: those are isolated anti-aliasing and glass-edge pixels (≤ 16/255, at most a few dozen pixels per frame).
- **New renders:** `ref/v2/<id>.png` (804 × 1748, RGBA). The old refs are untouched.
- **Non-visual changes** (reactions, component properties, notes) came from a read-only Plugin API dump of the Home frames and a verbatim comparison of every section note with the existing specs. Figma dumps from the first spec pass (30 Sep 15:30 UTC) served as the "before" state.

### 1.2 Pixel diff results

| Screen id | Figma frame | Changed px (>24/255) | Max diff | Verdict |
|---|---|---|---|---|
| `splash` | Splash 22:32 | 0 | 0 | unchanged |
| `welcome1` | Welcome 1 — Split 22:55 | 0 | 16 | unchanged (2 AA px) |
| `welcome2` | Welcome 2 — Track 22:133 | 0 | 16 | unchanged |
| `welcome3` | Welcome 3 — Settle 22:200 | 0 | 16 | unchanged |
| `getStarted` | Get Started 22:272 | 0 | 16 | unchanged |
| `signIn` | Sign in — Email or phone 39:356 | 0 | 16 | unchanged |
| `verify` | Sign in — Verify code 39:534 | 0 | 2 | unchanged |
| `verifyWrong` | Sign in — Wrong code 39:644 | 0 | 15 | unchanged |
| `setup1` | Setup 1 — Name & photo 42:665 | 0 | 2 | unchanged |
| `setup2` | Setup 2 — Currency 44:938 | 0 | 15 | unchanged |
| `setup3` | Setup 3 — Payment 46:1021 | 0 | 15 | unchanged |
| `setup4` | Setup 4 — Notifications 46:1148 | 0 | 16 | unchanged |
| `allSet` | All set 46:1223 | 0 | 16 | unchanged |
| `homeActive` | Home — Active 24:5 | **359** | 138 | **changed**: one box at x82.5 y201 w53.5 h10 (see 1.3 #1) |
| `homeFirstDay` | Home — First day 24:326 | 0 | 16 | unchanged (glass-button edge AA only) |
| `homeAllSettled` | Home — All settled 24:414 | 0 | 16 | unchanged |
| `homeAddSheet` | Home — ＋ Action sheet 24:520 | **243** | 83 | **changed**: the same box under the scrim (see 1.3 #1) |
| `homeAddSheet-overlay` | Overlay — Add sheet 24:808 | 0 | 15 | unchanged. Note: the v2 crop includes the section background behind the transparent overlay frame, so compare it flattened on white. |

**Page 03 has no visible change at all.** Its 14 notes (section titles, subtitles, per-frame captions) are character-for-character identical to the ones quoted in `screens-launch.md`, `screens-signin.md` and `screens-setup.md`. Its flow starting points are unchanged ("Onboarding" → 22:32, "Sign in & Setup" → 39:356). Page 03's prototype reactions could not be re-read (see §5.3). The renders are identical, so nothing an implementer builds from the existing specs changes.

### 1.3 Every change found on page 04 (element, old → new)

1. **"You're owed" label: straight apostrophe → curly apostrophe.** This is the only pixel change.
   - Where: `Card / Balance` Type=Owed (component 13:231, text layer "label" 13:238). It's a component change, so every instance picks it up: Home — Active (24:5, x58 y196), the dimmed background of Home — ＋ Action sheet (24:520), Home — Confirm payment (167:11424, x58 y340), and the page 08 frames that reuse Home.
   - Old: `You're owed` (U+0027). Verified in the bytes of the first pass's `get_design_context` output for 24:5 and in the 15:30 UTC dump.
   - New: `You’re owed` (U+2019).
   - The glyph change moves "re owed" by a fraction of a point, which is the diff box (x82.5–136, y201–211).
   - **Supersedes:** README §3 rule 5 ("the straight ' in 'You're owed'"), `screens-home.md` §0 and §2.2, and `components-home.md` §6 ("You're owed (straight ')"). **All copy now uses the curly ’ everywhere, with no exception.**
2. **`Card / Balance` gained a boolean property `Show caption`.** It's true on every Home instance, so there's no visual change. The first spec listed only `Type`, `Show chevron`, `Show action` and `Show badge`. With it false, the "from 4 people" / "across 2 groups" footnote is hidden. See `components-app.md` for the component's current description.
3. **New frame "Home — Confirm payment" (167:11424)** at section x602 y200 (between Active and First day), plus a new flow starting point with the same name. Page 04 flows are now "Home" → 24:5 and "Home — Confirm payment" → 167:11424. Full spec in §3.
4. **The Home section subtitle (24:4)** now reads: "Home: active · confirm payment · first day · all settled · ＋ Add sheet, plus the prototype overlay. Tab bar: Home · Groups · ＋ · Activity · Profile." (The first spec didn't quote the old subtitle.) The per-frame captions of the five original frames are unchanged. The new frame has its own caption (§4.2).
5. **Prototype hotspots were added to every Home frame.** On 30 Sep 15:30 UTC, the only reactions on page 04 were the ＋ hotspots (→ Overlay 24:808) and the overlay's scrim/✕ `CLOSE`. Now there are transparent `…hotspot` frames linking to the other pages by prototype URL. The `216:*` and `224:*` ids are all new. The full map is in §2. As a result, these statements in `screens-home.md` are **obsolete**:
   - "Tap either card → per-person breakdown (later phase; do nothing now)."
   - "Settle up … (later phase; do nothing)."
   - "Remind … Tap: nothing yet."
   - "Settle … Tap: nothing yet."
   - "See all … Tap: nothing (Activity tab is inert)."
   - "Rows are not tappable in this phase."
   - "Groups / Activity / Profile: inert."
   - The header buttons "Tap: nothing yet".
   - First day "Add expense" → Add sheet (README §5.1 #12). It now opens **Add expense — Empty** directly.
   - "Invite friends … Tap: nothing yet."
6. **Home — ＋ Action sheet (24:520) got its own reactions.** Before, 24:520 was a static picture, and the interactions lived only on the overlay 24:808.
   - Its Scrim (24:704) and a new "✕ hotspot" (216:28918, 328,492 50×50) now `NAVIGATE → Home — Active` with `DISSOLVE 200 ms EASE_OUT`.
   - A new "Add expense hotspot" (216:28920, 24,550 354×72) links by URL to page 06. I couldn't read the rest of its target, nor any row hotspots on 24:808 (§5.3). The overlay helpers on pages 07, 08, 09 and 11 are copies of 24:808 and link the four rows as listed in §2.4, so implement those.
   - In the app, this changes nothing: close = dismiss the overlay, as specced.
7. **The instance annotations are unchanged.** They still say "(later phase)" for the balance cards and Settle up (§4.3). The new hotspots supersede them.
8. **The `Scroll edge (fade)` differs per frame:**
   - Active: x0 y724 402×150 (unchanged).
   - Confirm payment: **x0 y756 402×118**, starting exactly at the bottom of the Goa Trip card, so the fade doesn't wash over it.
   - The "Payment confirmed" Home state on page 08: y724 ×150.
   - The gradient stops are the same everywhere. See §3.8.

No other element on page 04 changed: geometry, colours, text and component variants of Active / First day / All settled / ＋ sheet / overlay all match `screens-home.md`.

---

## 2. Home as the root of the tab app: navigation map

**Sources.** Frame-level hotspots and instance reactions on page 04 (ids below). Where a hotspot links by prototype URL to another page, the destination is resolved to the frame name. The presentation style comes from the destination frame's own structure and notes on its page (the destination specs own the details).

**Presentation vocabulary:**
- **tab switch**: select that tab. The tab keeps its own stack. No animation beyond the tab-bar state change.
- **push**: native push on the Home tab's stack. The tab bar stays visible, as in Figma, where pushed frames draw the tab bar. The destination has a glass back button whose "Hotspot — Back" returns to Home — Active.
- **full-screen modal**: a Navigation / Modal Header screen with the glass ✕ on the LEFT. It slides up from the bottom (the prototype uses MOVE_IN TOP 300 ms EASE_OUT for modals).
- **sheet over Home**: a `Sheet / Container` Detent=Medium over the 40 % scrim, same motion as the Add sheet (`screens-home.md` §5.3).

### 2.1 Every Home state (Active 24:5, Confirm payment 167:11424, First day 24:326, All settled 24:414)

| Element (Home rect in Active → in Confirm payment) | Destination | Figma node / page / code | Presentation | Source |
|---|---|---|---|---|
| Sparkle glass button (286,62 44×44) | **Ask Paybak — Start** | 167:13148, page 11, "11-04" | full-screen modal (chat; Modal Header Action=None) | Sparkle hotspot 216:28852 / 216:28874 / 216:28898 / 216:28908 (Active / Confirm / First day / All settled). Note 11-04: "Opens from the sparkle button in the Home header." |
| Bell glass button (338,62 44×44) | **Notifications** (inbox) | 177:30749, page 09, "09-08" | push (its "Hotspot — Back" → Home — Active) | Bell hotspot 216:28854 / 216:28876 / 216:28900 / 216:28910. Section note: "The Home bell opens an inbox …" |
| Logo long-press | debug menu (debug builds only) | — | menu | flow.md (unchanged) |
| Tab: Home | stays on Home (selected). From another tab, it returns to the Home root. | 24:5 | tab switch | "Hotspot — Tab Home" on 09-01/09-02 → 24:5 |
| Tab: Groups (100.5,796 68×52) | **Groups** | 167:14881, page 07, "07-01" | tab switch | Tab Groups hotspot 216:28868 / 216:28892 / 216:28902 / 216:28912 |
| ＋ (175,796 52×52) | **Add sheet** (unchanged) | overlay 24:808 | sheet over Home, MOVE_IN TOP 300 ms EASE_OUT | ＋ hotspot 24:309 / 167:11439 / 24:397 / 24:503 |
| Tab: Activity (233.5,796 68×52) | **Activity — Timeline** | 167:14361, page 09, "09-01" | tab switch | Tab Activity hotspot 216:28870 / 216:28894 / 216:28904 / 216:28914 |
| Tab: Profile (308,796 68×52) | **Profile** | 64:4316, page 05 | tab switch | Tab Profile hotspot 216:28872 / 216:28896 / 216:28906 / 216:28916 |

### 2.2 Active and Confirm payment (the data states)

| Element | Rect in Active → in Confirm payment | Destination | Figma node / page / code | Presentation | Source |
|---|---|---|---|---|---|
| "You’re owed" balance card | 20,180 175×116 → 20,324 | **You’re owed — Breakdown** | 167:11705, page 08, "08-01" | push (back → Home) | Owed card hotspot 216:28856 / 216:28882 |
| "You owe" balance card | 207,180 175×116 → 207,324 | **You owe — Breakdown** | 167:11957, page 08, "08-02" | push | Owe card hotspot 216:28858 / 216:28884 |
| Settle up (Primary Large) | 20,308 362×52 → 20,452 | **Settle up** (fewest-payments plan) | 167:12113, page 08, "08-03" | push (back → Home) | Settle up hotspot 216:28860 / 216:28886 |
| Rohan row "Remind" (On Card Small) | 280,468 86×36 → 280,612 | **Remind — Rohan**: the Remind sheet over Home | 177:21203, page 08, "08-07" | sheet over Home (Sheet / Container Medium "Remind Rohan", 386×580 at 8,286) | Rohan Remind hotspot 216:28862 / 216:28888 |
| Goa Trip row "Settle" (On Card Small) | 290,564 76×36 → 290,708 | **Record payment — Kabir** (prefilled; Goa Trip is simplified, so you pay Kabir ₹1,400 directly) | 167:13635, page 08, "08-04" | full-screen modal | Goa Trip Settle hotspot 216:28864 / 216:28890 |
| "See all" (Recent activity header) | 335,630 47×44 → 335,774 | **Activity — Timeline** | 167:14361, page 09 | tab switch to Activity (not a push) | See all hotspot 216:28866 / 224:25540 |
| Recent activity row "Dinner at Olive Garden" | 20,672 362×64 → 20,816 | **the expense's detail**. The prototype links to "Expense added" (177:24360, page 06, "06-10"), the Olive Garden expense drawn on the 09-03 Expense detail template. | 177:24360 | push, **without** the "Expense added" toast (that toast only follows Save) | Instance reaction on Row / Activity 24:197 / 167:11434 |
| Recent activity rows "Priya paid you", "Electricity bill" | 20,740 / 20,808 → 20,884 / 20,952 | no reaction in Figma. **Proposal:** open that item's detail (a payment → the payment detail used on 08-05/06-12, an expense → Expense detail 09-03), like the Dinner row. | — | push | proposal (flow.md: no dead ends) |
| Due soon row bodies (outside the Remind/Settle buttons) | Rohan 20,428 362×88; Goa Trip 20,524 → +144 | no reaction. **Proposal:** Rohan → Friend — Rohan (177:25988, "07-08"); Goa Trip → Group — Goa Trip (167:18792, page 07). | — | push | proposal |
| Confirm (card, Confirm payment only) | — → 36,248 161×36 | Confirm the payment. Home then becomes the **"Payment confirmed"** state (177:25303, page 08, "08-11"). | §3.9 | in place + toast | Confirm hotspot 216:28878 (URL → 177:25303), on top of the component's own CHANGE_TO State=Confirmed (250 ms smart animate) |
| Not received (card, Confirm payment only) | — → 205,248 161×36 | **Not received** sheet over Home | 177:25006, page 08, "08-10" | sheet over Home (Sheet / Container Medium, no title/close, 386×458 at 8,408) | Not received hotspot 216:28880 |

### 2.3 First day (24:326)

| Element | Rect | Destination | Figma node / page / code | Presentation | Source |
|---|---|---|---|---|---|
| "Add expense" (Primary Large, plus icon) | 44,510 314×52 | **Add expense — Empty** | 176:17454, page 06, "06-01" | full-screen modal | Add expense hotspot 224:25542. **Changed:** it used to open the Add sheet. |
| "Invite friends" (On Card Large, user-add icon) | 44,574 314×52 | **Add friend** | 177:26746, page 07, "07-09" | push | Invite friends hotspot 224:25544 |
| Empty-state illustration | 81,204 240×180 | none (Rive tap animation only) | — | — | unchanged |

All settled (24:414) has only the §2.1 links. Its card has no actions: "A calm confirmation — no extra actions."

### 2.4 The ＋ Add sheet rows (overlay 24:808; same on 24:520)
From the page-07/08/09/11 helper copies of this overlay (I couldn't read the page-04 row hotspots; §5.3):

| Row | Destination | Node / page | Presentation |
|---|---|---|---|
| Add expense | Add expense — Empty | 176:17454, page 06 ("06-01") | dismiss the sheet, then full-screen modal |
| Record payment | Record payment — form | 177:29750, page 06 | same |
| Lend money (IOU) | Lend money — form | 185:25810, page 06 | same |
| New group | New group — Group | 190:8356, page 06 | same |

The scrim and ✕ close the sheet (overlay 24:808: `CLOSE`; 24:520: NAVIGATE → Home — Active, DISSOLVE 200 ms).

### 2.5 Back and return behaviour
- Home is the root of the Home tab. System back on Home exits the app (flow.md resolved decisions). **Proposal:** Android system back on another tab's root selects the Home tab first (Material guidance). iOS has no back there.
- Every pushed destination returns to Home with its back button or edge swipe. Their "Hotspot — Back" all target 24:5 Home — Active, which in the app means "pop to Home, in whatever state the data now puts it".
- The page 08 sheets over Home close back to the same Home state:
  - Remind: ✕ / scrim / "Send in Paybak" → Home (toast "Reminder sent to Rohan"); "Share…" → the iOS share sheet (177:23342).
  - Not received: "Cancel" or the scrim → Home — Confirm payment (the card is still there); "Send" → Home without the card (§3.10).

---

## 3. `homeConfirmPayment`: Home — Confirm payment (167:11424)

**Purpose (designer caption):** "When a friend records a payment to you, a confirm card appears above the balances, and Home keeps +₹2,900 until you confirm. The card pushes Recent activity under the glass tab bar."

**Container:** Home tab root, the same scaffold as Home — Active (`screens-home.md` §1): the header scrolls with the content, the glass tab bar floats, content scrolls under it. It's a **state of Home**, not a separate screen. Show it whenever at least one incoming payment claim is pending your confirmation. The card sits between the header and the balance summary, and everything below moves down by **144** (card 120 + section gap 24).

**References:**
- 2× render: `ref/homeConfirmPayment.png` (804 × 1748, cropped from the 2× export of section 24:2, the same pipeline as the other refs; `get_screenshot` wasn't available, §5.3).
- Node tree: node 167:11424 in `.figma-cache/nodes/3-5.json` (regenerate with `tools/fetch_figma.py`). The Plugin API dumps this spec was written from weren't kept (§5.3).

**Debug start screen id (proposal):** `homeConfirmPayment` = demo data **with** Esha's pending ₹700 claim. For `homeActive`, seed the same demo data **without** that claim. The Activity timeline (09-01), the Notifications inbox (09-08) and the lock-screen push (09-09) all show this same claim as pending "tonight at 9:12 pm". So the full demo dataset has it pending, and plain Home — Active is the demo minus the claim. This needed confirming in the architecture plan (`app-architecture.md`).

### 3.1 Frame
Vertical auto-layout, padding 62 / 20 / 34 / 20 (`layout/status-bar`, `layout/screen-margin`, `layout/home-indicator`), gap 24 (`layout/section-gap`), fill `color/bg/primary`, clips. Children in order: Header, **Card / Confirm Payment**, Balance summary, Due soon, Recent activity. Absolutely positioned on top: Scroll edge (fade), Tab bar, hotspots, kit bars.

### 3.2 Header: y62–156
Identical to `screens-home.md` §1.1 (Nav Header Type=Home, 17:474): logo at 20,70; sparkle glass button at 286,62; bell glass button with the unread dot at 338,62 (dot 364,71 10×10); greeting "Good evening, Arjun" (Title/1) at 20,118 362×38.

### 3.3 Card / Confirm Payment (instance 167:11442, main component 129:1977 `State=Pending` of set 129:2060): **NEW component**
**x20 y180 w362 h120** (bottom y300). SwiftUI name from the Figma description: `PBConfirmPaymentCard`. The full description is quoted in §4.4.

Card: fill `color/bg/card` #F5F5F5, radius 20 (`radius/card`), padding 16 all sides (`layout/card-padding`), vertical auto-layout, gap 12 (`space/12`), width FILL, height HUG. No stroke, no shadow.

1. **"top" row**: x36 y196 w330 h40. Horizontal, gap 12 (`space/12`), items centred vertically.
   - **Avatar**: `Avatar / Circle` Size=40 Type=Art, x36 y196 40×40. Circle fill **`color/bg/primary` (white, because it sits on a card)**, clips. Art = **Art / Peep Head / Esha** (7:52) → `assets/avatars/avatar-4.svg` (preset 3), scaled to 40×40.
   - **"text" column**: x88 y196 w278 h40. Vertical, **gap 0** (`space/0`), width FILL.
     - **title**: x88 y196 w278 h22, **Headline** (Manrope SemiBold 16/22, ls −0.25 %), `color/text/primary` #0A0A0A, left, **1 line, truncate at the end**. Text = the `Title` property: **"Esha says she paid you ₹700"**.
     - **detail**: x88 y218 w278 h18, **Footnote** (Medium 13/18), `color/text/secondary` #6B6B6B, left, 1 line, truncate at the end. Text = the `Detail` property: **"Dinner at Olive Garden · UPI · 9:12 pm"** (expense title · method · time the claim was made).
2. **"actions" row**: x36 y248 w330 h36. Horizontal, gap 8 (`space/8`). Both buttons are **FILL**, so each is 161 wide.
   - **confirm**: `Button / Primary` Size=Small, State=Default, no icon. x36 y248 161×36 (`size/button-sm`), capsule, fill `color/bg/inverse` #0A0A0A, padding 0/16, gap 6. Label **"Confirm"** (Button/Small: SemiBold 15/20, ls −0.25 %), `color/text/inverse`, centred (label box x87.5 y256 58×20). Pressed: `color/bg/inverse-pressed` #2B2B2B.
   - **not received**: `Button / On Card` Size=Small, State=Default, no icon. x205 y248 161×36, capsule, fill `color/bg/primary` #FFFFFF, padding 0/16. Label **"Not received"** (Button/Small), `color/text/primary`, centred (label box x240 y256 91×20). Pressed: `color/bg/card-pressed` #EBEBEB.

**Component properties** (set 129:2060):
- `Title` (text, default "Esha says she paid you ₹700")
- `Detail` (text, default "Dinner at Olive Garden · UPI · 9:12 pm")
- `Confirmed title` (text, default "Esha paid you ₹700")
- `Confirmed detail` (text, default "Dinner at Olive Garden · UPI · Confirmed")
- `State` = Pending | Confirmed

The button labels are set on the nested buttons ("Confirm" / "Not received").

**Variant State=Confirmed (129:2021), 362 × 72.** From a Plugin API dump made for the Activity spec (not kept). Component-relative coordinates:
- Same card: fill `bg/card`, radius 20, padding 16, gap 12.
- **Only the top row**, no actions, so the height is 16 + 40 + 16 = 72:
  - avatar 40 at (16,16), white circle, Esha art;
  - text column at (68,16), **242** wide:
    - title (68,16) 242×22, Headline, text/primary, 1 line: **"Esha paid you ₹700"**;
    - detail (68,38) 242×18, Footnote, text/secondary, 1 line: **"Dinner at Olive Garden · UPI · Confirmed"**;
  - **status icon**: `Icon / Check Circle` 24×24 at (322,24) → `assets/icons/check-circle.svg`. The tint wasn't in the dump. Use `color/icon/primary` #0A0A0A (Paybak's check marks are black; never green).
- Interaction built into the component: "confirm" `ON_CLICK → CHANGE_TO State=Confirmed`, `SMART_ANIMATE`, `EASE_OUT`, **250 ms**. The buttons fade out, the card shrinks 120 → 72 and the texts cross-fade.

### 3.4 Balance summary: `Card / Balance Summary` (13:271, Show Settle up = true), x20 y324 w362 h180
Same as `screens-home.md` §2.2, moved down 144:
- **Owed card**: x20 y324 175×116. Icon money-in 16 at 36,342; label "**You’re owed**" (curly ’, §1.3 #1) x58 y340 w99; chevron at 163,342; amount "**+₹2,900**" (Amount/Large, text/primary) x36 y372 104×32; caption "**from 4 people**" (Footnote, text/tertiary) x36 y406.
- **Owe card**: x207 y324 175×116. Money-out icon at 223,342; label "You owe" x245 y340; chevron 350,342; amount "**−₹1,850**" (Amount/Large, **text/secondary**) x223 y372 96×32; caption "**across 2 groups**" x223 y406.
- **Settle up**: x20 y452 362×52, label "Settle up" at x164.5 y467.
- The balances are the **same as on Home — Active**. A pending claim doesn't change them ("Home keeps +₹2,900 until you confirm").

### 3.5 Due soon: x20 y528 w362 h228
Header "Due soon" (Row / Section Header, Show action = false) y528 (title x20 y531 90×26). Rows stack at y572, gap 8:
1. **Rohan** (Row / Attention State=Overdue), x20 y572 362×88:
   - avatar Rohan (`avatar-3.svg`, white circle) at 36,596;
   - "Rohan" (Headline) x88 y590 49×22, then "Movie tickets" (Subheadline, secondary) x143 y591;
   - badge Overdue "**Overdue 3 days**" x88 y618 110×24 (label x98 y622);
   - "**₹800**" (Amount/Medium) x322 y584 44×22;
   - "**Remind**" (On Card Small) x280 y612 86×36 (label x296 y620).
2. **Goa Trip** (State=Due soon), x20 y668 362×88:
   - Icon avatar (groups 20 at 46,702, white circle) at 36,692;
   - "Goa Trip" x88 y686 63×22, then "Your share" x157 y687;
   - badge On Card "**Due Fri**" x88 y714 61×24 (label x98 y718);
   - "**₹1,400**" x311 y680 55×22;
   - "**Settle**" x290 y708 76×36 (label x306 y716).

### 3.6 Recent activity: x20 y780 w362 h236 (from y780 down, under the fade and the tab bar at rest)
- Header "Recent activity" (Show action = true) y780. Title x20 y783 145×26. "See all" (Button / Text Secondary, no chevron) 47×44 at x335 y774.
- Rows (Row / Activity, Surface=Plain, min height 64, padding 8/0, gap 12):

| # | Row y | Leading | Title (Headline, 1 line) | Subtitle (Subheadline, ≤ 2 lines) | Amount (Amount/Medium) | Date (Footnote, tertiary) |
|---|---|---|---|---|---|---|
| 1 | 816–880 | Expense tile: #F5F5F5 circle at 20,828 + `food.svg` 20 at 30,838 | Dinner at Olive Garden (x72 y826, 239 wide) | You paid · 4 people (y850) | ₹2,800 (text/primary; x323 y827) | Today (x345 y851) |
| 2 | 884–948 | Avatar Art Priya (`avatar-2.svg`, #F5F5F5 circle) at 20,896 | Priya paid you (x72 y894) | UPI | ₹1,050 (text/primary; x327 y895) | Yesterday (x321 y919) |
| 3 | 952–1016 | Expense tile + `bolt.svg` at 30,974 | Electricity bill (x72 y962) | Flat 302 · You owe | −₹450 (**text/secondary**, Direction=Out; x331 y963) | 26 Sep (x340 y987) |

At rest, only the section header shows (from y780, under the fade and the glass tab bar, which starts at y791). The rows are reached by scrolling: "The card pushes Recent activity under the glass tab bar."

### 3.7 Tab bar
`Navigation / Tab Bar` Active=Home, x20 y791 362×62, identical to `screens-home.md` §1.2. Annotation: "＋ opens the Add sheet as an overlay."

### 3.8 Scroll edge (fade), and scrolling
- Fade (rectangle 167:11437): **x0 y756 w402 h118**, absolutely positioned, above the content and below the tab bar, not interactive. Linear gradient from top to bottom: #FFFFFF at 0 % opacity (0 %) → #FFFFFF at 85 % (45 %, ≈ y809) → #FFFFFF at 100 % (100 %, y874).
- Home — Active uses the same gradient over **y724 h150**. To match Figma in both states, use a fixed bottom overlay 150 tall, or 118 tall while the confirm card is shown. Either way it's white on white over the bottom edge, and the only visible effect is how early the Goa Trip card starts to fade.
- Scroll: the same vertical scroll as Home — Active. Content height at default type = 1016 (last row bottom), plus the recommended bottom inset of 107 (`screens-home.md` §1). There's no keyboard on this screen.

### 3.9 Confirm: behaviour and the resulting Home ("Payment confirmed", 177:25303, page 08 "08-11")
On tap **Confirm**:
1. **Animate the card to State=Confirmed** (250 ms smart animate, ease-out, §3.3): "Esha paid you ₹700" / "Dinner at Olive Garden · UPI · Confirmed" with the check-circle icon.
2. Record the payment as **confirmed**. Esha's ₹700 is now settled.
3. Home re-renders to the **"Payment confirmed" state**, which is Home — Active with new numbers:
   - **The confirm card is gone.** The page-08 frame has no card, and the balance summary is back at y180.
   - "You’re owed" = **+₹2,200**, caption "**from 3 people**" (Rohan ₹800, Priya ₹700, Dev ₹700). "You owe" is unchanged: −₹1,850 across 2 groups.
   - Due soon is unchanged (Rohan ₹800 overdue, Goa Trip ₹1,400 due Fri).
   - **Recent activity gets a new first row**: Row / Activity Type=Payment, Direction=In, avatar **Esha** (`avatar-4.svg`, #F5F5F5 circle), title "**Esha paid you**", subtitle "**UPI**", amount "**₹700**" (text/primary), date "**Today**". It sits above "Dinner at Olive Garden". The Recent activity block then has 4 rows (y672 / 740 / 808 / 876), 304 tall.
   - Fade y724 h150.
   - **Toast**: `Overlay / Toast` with the check-circle icon, label "**Payment confirmed**", at **x100 y731 203×44** (centred, **16 above the tab bar**, whose top is y791). It uses the Toast component spec (`screens-setup.md` §0.11 "Overlay / Toast (118:965)"): fill `bg/inverse`, capsule, padding right 20 / left 16, gap 8. It shows for 2 s, then fades.
4. **Sequencing proposal** (Figma shows only the start and end states): play the 250 ms Confirmed animation, hold it about 0.8 s so the confirmation reads, then collapse the card (height 72 → 0 plus fade, about 250 ms, ease-out) while the content below moves up 96 (72 + 24 gap), and insert the Esha row at the top of Recent activity. Show the toast when the collapse starts. Reduce Motion: cross-fade only.
5. The same claim is also confirmable from Activity (09-01), from the Notifications inbox (09-08) and from the lock-screen push actions (09-09). All of them must update one shared model, so Home drops the card whichever surface confirmed it.

### 3.10 Not received: behaviour (177:25006, page 08 "08-10"; full spec in `screens-settle.md`)
- On tap **Not received**, a **sheet opens over Home** (Home — Confirm payment stays visible under a 40 % scrim that covers the tab bar):
  - `Sheet / Container` Detent=Medium, x8 y408 386×458, radius 40, grabber shown, **no title and no close button**.
  - Content (`_Sheet / Not received`, 177:24988):
    - title "Let Esha know you haven’t received ₹700?" (Title/3);
    - context "Dinner at Olive Garden · UPI · 9:12 pm" (Subheadline, secondary);
    - `Control / Text Area` labelled "Note", prefilled with "Hi Esha, I haven’t received ₹700 for Dinner at Olive Garden yet. Could you check your UPI app?", helper "Esha still owes you ₹700 until a payment is confirmed.";
    - **Send** (Primary Large, 354×52 at 24,722);
    - **Cancel** (Secondary Large at 24,786).
- **Send**: the sheet closes, the note goes to Esha (simulated, flow.md), the claim is marked **not received**, and Home returns to **Home — Active**: the card is gone and the balances are unchanged (+₹2,900 from 4 people; "Her ₹700 stays owed until a payment is confirmed"). Prototype: Send → 24:5 Home — Active.
- **Cancel** or scrim tap: the sheet closes and Home — Confirm payment stays unchanged (the card is still pending). Prototype: Cancel / Scrim → 167:11424.
- **Proposal:** show a toast "Note sent to Esha" after Send. Figma doesn't draw one, so it's optional. Keep Paybak calm: no red anywhere in this flow.

### 3.11 Business rules (from the designer notes; §4 has them verbatim)
- A payment a friend records **to you** is **pending** until you confirm. While it's pending, Home balances don't change ("Home keeps +₹2,900 until you confirm").
- **Confirm** settles that amount. "You’re owed" drops by it (₹2,900 → ₹2,200), the "from N people" count drops if that person is now square (4 → 3), and the payment tops Recent activity.
- **Not received** sends the payer an editable note instead of silently rejecting. The amount stays owed.
- Nothing about a pending or rejected payment is red. Red stays reserved for overdue items (Rohan's badge).
- The mirror case (you recorded a payment to someone, and they must confirm) is the payer's "Payment pending" view (167:17304, 08-05): "Pending is gray, not red, and nothing changes yet". It's not a Home state.
- **Several pending claims** aren't designed. **Proposal:** one card per claim, newest first, stacked with a 12 pt gap above the balance summary, each confirmed or rejected independently.

### 3.12 Sample data (verbatim, everything in the frame including off-screen content)
- Header: "Paybak", "Good evening, Arjun". Bell unread dot shown.
- Confirm card: avatar Esha (Art / Peep Head / Esha 7:52). "Esha says she paid you ₹700", "Dinner at Olive Garden · UPI · 9:12 pm", "Confirm", "Not received". Confirmed-state texts: "Esha paid you ₹700", "Dinner at Olive Garden · UPI · Confirmed".
- Balances: "You’re owed", "+₹2,900", "from 4 people", "You owe", "−₹1,850", "across 2 groups", "Settle up".
- Due soon: "Rohan", "Movie tickets", "Overdue 3 days", "₹800", "Remind"; "Goa Trip", "Your share", "Due Fri", "₹1,400", "Settle".
- Recent activity: "Recent activity", "See all"; "Dinner at Olive Garden", "You paid · 4 people", "₹2,800", "Today" (food icon); "Priya paid you", "UPI", "₹1,050", "Yesterday" (Priya art); "Electricity bill", "Flat 302 · You owe", "−₹450", "26 Sep" (bolt icon).
- Hidden instance defaults (not shown; don't render): Row / Activity `Detail` = "Next · Thu 1 Oct"; Avatar / Circle `Initials` = "AK".
- Tab bar: "Home" (active), "Groups", ＋, "Activity", "Profile". Status bar time "9:41" is system UI.
- After Confirm (08-11): "+₹2,200", "from 3 people"; new row "Esha paid you", "UPI", "₹700", "Today"; toast "Payment confirmed".

### 3.13 Reuse map
| Element | Spec component | Status |
|---|---|---|
| Header | Navigation / Nav Header Type=Home (`components-home.md` §1) | reuse |
| Sparkle / bell | Button / Icon Style=Glass (+Badge) (`components-home.md` §2) | reuse |
| **Confirm card** | **Card / Confirm Payment** (129:2060) `PBConfirmPaymentCard` | **NEW**. Fully described in §3.3; also covered by `components-app.md`. |
| Card avatar | Avatar / Circle 40 Art, white fill on a card (`components-core.md` §3.2) | reuse |
| Confirm / Not received | Button / Primary Small, Button / On Card Small (`components-core.md` §2.1), stretched to FILL | reuse |
| Balance summary | Card / Balance Summary + Card / Balance (`components-home.md` §6–7) | reuse (label now "You’re owed"; new `Show caption` prop) |
| Due soon | Row / Section Header, Row / Attention, Badge / Pill, Button / On Card (`components-home.md` §8, 9, 12) | reuse |
| Recent activity | Row / Section Header (+ Button / Text), Row / Activity (`components-home.md` §8, 10) | reuse |
| Fade | Scroll edge (fade) (`screens-home.md` §2.6) | reuse, height 118 here |
| Tab bar | Navigation / Tab Bar + Tab Bar Item + Button / Add (`components-home.md` §3–5) | reuse |
| Toast after Confirm (08-11) | Overlay / Toast 118:965 (`screens-setup.md` §0.11) | reuse |
| Not received sheet (08-10) | Sheet / Container Medium + `_Sheet / Not received` (`screens-settle.md`) | reuse from page 08 |

### 3.14 Art
- **No illustration** on this frame, so no Rive. The only art is the people's heads: Esha → `assets/avatars/avatar-4.svg`, Rohan → `avatar-3.svg`, Priya → `avatar-2.svg`. These are static line art, not Rive (`assets/avatars/INDEX.md`).
- Icons: sparkles, bell, money-in, money-out, chevron-right, groups, food, bolt, home, activity, profile, check-circle (Confirmed variant and toast), all already in `assets/icons/`. ＋ = `assets/images/add-button-plus.svg`. **No new icons or images were needed.**

### 3.15 Test IDs (proposal, `<screen>.<element>` per flow.md)
`screen.homeConfirmPayment`, `home.confirmCard`, `home.confirmCard.confirm`, `home.confirmCard.notReceived`, `home.confirmCard.title`.

Add these for the new Home links:
- `home.assistant` (sparkle), `home.bell`
- `home.balance.owed`, `home.balance.owe`, `home.settleUp`
- `home.due.rohan.remind`, `home.due.goaTrip.settle`
- `home.seeAll`, `home.activity.row.<n>`
- `home.firstDay.addExpense`, `home.firstDay.invite`
- `home.notReceived.send`, `home.notReceived.cancel`

---

## 4. Designer notes (verbatim)

### 4.1 Section "Home" (24:2), page 04
- Title (24:3): "Home"
- Subtitle (24:4): "Home: active · confirm payment · first day · all settled · ＋ Add sheet, plus the prototype overlay. Tab bar: Home · Groups · ＋ · Activity · Profile."

### 4.2 Per-frame captions (text under each frame)
- Home — Active (24:6): "Owed and owing at a glance, what needs attention next, and recent activity. Content scrolls under the glass tab bar."
- **Home — Confirm payment (167:11524)**: "When a friend records a payment to you, a confirm card appears above the balances, and Home keeps +₹2,900 until you confirm. The card pushes Recent activity under the glass tab bar."
- Home — First day (24:327): "New account. One clear next step: add an expense or invite friends."
- Home — All settled (24:415): "Nothing owed either way. A calm confirmation — no extra actions."
- Home — ＋ Action sheet (24:807): "Tap ＋ in the tab bar. Floating sheet over a 40% scrim; ✕ or the scrim closes it."
- Overlay — Add sheet (24:864): "Prototype overlay target: transparent frame with its own scrim, opened by ＋."

### 4.3 Dev-mode annotations on instances (Home — Confirm payment; the same ones are on Home — Active)
- Balance summary (167:11426): "Tap a balance card → per-person breakdown (later phase). Settle up → Settle Up flow (later phase)." The page-08 links now implement both.
- Rohan — overdue (167:11430): "Red = overdue only. Remind sends a polite pre-written nudge."
- Tab bar (167:11438): "＋ opens the Add sheet as an overlay."
- (On Activity 09-01's instance of the same card, 167:14385: "Confirm → State=Confirmed (interactive component, smart animate 250 ms). Not received → ↳ Not received sheet (overlay).")

### 4.4 Component `Card / Confirm Payment` (129:2060, page 02): description and caption
Description, as read from Figma for the Settle Up spec:
> PBConfirmPaymentCard — The receiver’s confirm card at the top of Home and in Activity (04-06 · 08-10 under the scrim · 09-01 · 09-08), #F5F5F5 r20, padding 16, gap 12.
> State=Pending (362×120): exposed Avatar / Circle 40 (the payer, white circle), Title (Headline) “Esha says she paid you ₹700”, Detail (Footnote) “Dinner at Olive Garden · UPI · 9:12 pm”, then two exposed buttons, both FILL, gap 8: Button / Primary Small “Confirm” (layer “confirm”) and Button / On Card Small “Not received” (layer “not received”).
> Button labels: set Label#9:0 on “confirm” and Label#9:42 on “not received” (they show in this card’s property panel as nested instances). Figma can’t bind a card-level text property to text inside a nested button, so there is no separate Confirm label / Secondary label property.
> State=Confirmed (≈72): Confirmed title “Esha paid you ₹700”, Confirmed detail “… · Confirmed”, Icon / Check Circle, no buttons. The Confirmed texts are separate properties so they survive the variant change.
> Interactive: Confirm → Change to State=Confirmed (on click, smart animate, ease out, 250 ms). A frame can override the Confirm button with its own link (04-06 → 08-11); Not received opens the 08-10 sheet on the frame.
> SwiftUI: PBConfirmPaymentCard

Page-02 caption (131:1921): "Confirm payment card (Confirm → Confirmed, smart animate 250 ms)". (So Home — Confirm payment is frame "04-06" in the designer's numbering.)

### 4.5 Notes on other pages that define Home behaviour (verbatim)
**Page 08 Settle Up**
- Section "Balance breakdowns", subtitle (167:11701): "The screens behind the two Home balance cards: who owes you, and who you owe."
- 08-01 You’re owed — Breakdown (167:11706): "Tapping “You’re owed” on Home lists every person who owes you, overdue first. Only Rohan’s badge is red. In the app every row opens that friend’s page; only Rohan’s (07-08) is drawn."
- 08-02 You owe — Breakdown (167:11958): "The hero amount is gray because it’s money you owe. Nothing is red, because nothing is overdue. The footnote explains why you pay Kabir the whole ₹1,400 for Goa Trip."
- Section "Settle up & record", subtitle (167:11704): "The simplified suggestion list, Record payment prefilled for Kabir (UPI, with Copy), and the pending detail the payer sees."
- 08-03 Settle up (167:12114): "This is the fewest-payments plan. You make 2 payments and 4 people pay you, with Goa Trip simplified so that you pay Kabir directly. Settle opens Record payment prefilled, and Remind opens the reminder sheet over this list."
- 08-04 Record payment — Kabir (167:13636): "Everything is prefilled from the suggestion. With UPI selected, Kabir’s UPI ID shows with Copy. Copy puts kabir@okaxis on the clipboard and shows Overlay / Toast “UPI ID copied” (Check Circle), centred 50 above the bottom, which fades after 2 s. Paybak never moves money: you pay in your own UPI app and save the record here."
- 08-05 Payment pending (167:17305): "After Save you land on the payment with a “Payment recorded” toast. It stays “Pending confirmation”, and Home keeps −₹1,850, until Kabir confirms. “Paid to” appears only for methods with an ID (UPI, Bank), so the Cash payment on 06-12 has 6 rows."
- Section "Remind", subtitle (176:20969): "The reminder sheet over Home, the same sheet as an overlay-only frame, and the iOS share sheet."
- 08-07 Remind — Rohan (177:24981): "Remind opens a pre-written message that you can edit, in a Friendly or Neutral tone. Send it as a Paybak notification, or share it through any app. “Send in Paybak” closes the sheet and shows Overlay / Toast “Reminder sent to Rohan” for 2 s (16 above the tab bar on Home, 50 above the bottom elsewhere). The balances don’t change."
- 08-07s Remind sheet (overlay) (177:24982): "The Remind sheet on its own. Pages that aren’t Home open it as an overlay over the current screen, so the chat or friend page stays visible under the scrim."
- 08-08 Remind — Share (177:24983): "“Share…” hands the same message to the iOS share sheet, so you can send it on WhatsApp, Messages or Mail, or copy it."
- Section "Receiver confirmation", subtitle (177:24987): "The Not received sheet over Home, the same sheet as an overlay-only frame, and Home after Confirm."
- 08-10 Not received (177:25557): "Not received sends Esha an editable note instead of silently rejecting her payment. Her ₹700 stays owed until a payment is confirmed."
- 08-10s Not received sheet (overlay) (177:25558): "The Not received sheet on its own. Activity and the Notifications inbox open it over themselves, so the timeline stays visible under the scrim."
- 08-11 Payment confirmed (177:25559): "After Confirm, Esha’s ₹700 is settled. “You’re owed” drops to +₹2,200 from 3 people (Rohan ₹800, Priya ₹700, Dev ₹700), and her payment tops Recent activity."
- Overlay helper (216:29189): "Prototype helper, not a screen: a copy of Overlay — Add sheet (page 04). ＋ on 08-11 opens it with Open overlay. Its rows open the page 06 forms by URL; the scrim and ✕ close it."

**Page 09 Activity**
- 09-01 Activity — Timeline (167:14683): "Everything that changed, newest first, grouped by day; a payment waiting for you sits on top with Confirm and Not received, the Restore button opens Recently deleted, and the Fuel share is ₹2,500 ÷ 5. Rohan’s automatic reminders follow the default schedule (2 days before, on the due date, then every 3 days while overdue), so they went out Fri 25 Sep, Sun 27 Sep and tonight at 9:00 pm."
- Section "Notifications" subtitle (177:30748): "The Home bell opens an inbox grouped Today and Earlier, with payments to confirm handled inline; two lock-screen pushes show a payment to confirm and an automatic reminder."
- 09-08 Notifications (177:30956): "The bell opens this inbox, grouped Today and Earlier; payments to confirm can be handled right here, while reminders Paybak sends for you (like tonight’s to Rohan) are logged on the timeline, not here. Times are derived: the Kabir reminder goes out at 9:00 pm, two days before Goa Trip is due, Rohan’s overdue alert came on Mon 28 Sep, and the 8:00 pm summary comes after tonight’s dinner was added, so it counts ₹2,900."
- 09-09 Lock screen — Confirm request (186:7136): "State: Esha’s claim arrives as a push at 9:12 pm. Tapping it opens Activity; a long press offers Confirm and Not received."
- Overlay helper (216:28770): "Prototype helper, not a screen: a copy of Phase 1’s Overlay — Add sheet (page 04). The tab bar ＋ on 09-01 and 09-02 opens it with Open overlay; its rows open Add expense, Record payment, Lend money (IOU) and New group on page 06, and the scrim or ✕ closes it."

**Page 11 Insights & AI**
- Section "Ask Paybak" subtitle (167:13002): "A full-screen chat from the Home sparkle button: suggested prompts, an answer from live balances, and a drafted expense that waits for Save."
- 11-04 (167:13284): "Opens from the sparkle button in the Home header. Suggested prompts send in one tap, and the mic dictates into the field without sending. Shown as a Pro member."
- 11-05 (167:14357): "Answers use your live balances, and the numbers match Home (₹2,900 from 4 people). Action chips open the normal flows, so reminding Rohan uses the usual Remind sheet. Shown as a Pro member."

**Page 07 Groups & Friends**
- 07-02 Friends (167:15961): "Each friend shows one net across all groups and direct expenses, so this list adds up to the Home totals. Only the overdue badge is red, and guests show “No balance” until you share something with them."
- 07-03 Groups — Empty (167:16506): "State: a new account with no groups yet. The same card pattern as Home “First day” points to creating a group or inviting friends."
- 07-09 Add friend (177:26996): "One screen covers every way to add someone: search, an invite link, QR in both directions, and your contacts. Inviting someone who isn’t on Paybak adds them right away as a guest friend. Usernames follow the @arjun pattern (lowercase first name)."
- Overlay helper (216:29031): "Prototype helper, not a screen: a copy of Phase 1’s Overlay — Add sheet (page 04). The tab bar ＋ on 07-01 and 07-02 opens it with Open overlay, move in from bottom, 300 ms. Its rows open Add expense, Record payment, Lend money (IOU) and New group on page 06; the scrim or ✕ closes it."

**Page 06 Add & Record** (the Home First-day and Dinner-row targets)
- 06-01 Add expense — Empty (176:17769): "Amount first: the decimal pad opens with the cursor in ₹0, so a bill takes seconds. Save stays disabled until there is an amount and at least one other person, and the date defaults to Today."
- 06-10 Expense added (177:24769): "After Save, the modal closes onto the new expense with a short toast; the screen uses the 09-03 Expense detail template. Your share is ₹700, so you’re owed ₹2,100 (₹2,800 − ₹700); the share card keeps the template’s rows with no “You’re owed” row, and the group chip and group balance row are hidden because the dinner isn’t in a group."

**Page 12 Settings & Pro**
- 12-01 Paywall (caption): "Paywall, opened from the Profile “Paybak Pro” row or any locked feature (Export records, the Add expense Repeat row, Insights); Arjun is on the free plan and the core ledger stays free. Yearly is selected by default and is the only plan with the 7-day trial, so picking Monthly changes the CTA to “Subscribe for ₹99/month”."
- Privacy & data, Delete account state (quoted in the components spec): "State: Delete account was tapped while balances are open, using the Home totals of −₹1,850 and +₹2,900. Deleting is blocked, and “Settle up” opens the settle-up suggestions (08-03)."

**Page 05 Profile**
- Profile (caption, quoted from another spec's Figma dump): "Profile tab. Tap the avatar or Edit avatar → Edit avatar (push). The Pro entry point is the top of the settings card, shown on the free plan. After subscribing, the badge is replaced by the value “Active”."

---

## 5. Assets, files, open issues

### 5.1 Assets
**No new icons or images.** Everything on the new frame already exists (§3.14). I checked `assets/icons/INDEX.md` for check-circle, sparkles, bell, money-in/out, chevron-right, groups, food, bolt, home, activity, profile and plus, and `assets/avatars/INDEX.md` for Esha = `avatar-4`. The frame has no illustration, so no Rive and no export.

### 5.2 Files written for this spec
- `screens-home-v2.md` (this file)
- `ref/homeConfirmPayment.png`: 2× reference for the new frame (804 × 1748)
- `ref/v2/<id>.png` for the 18 re-rendered refs: splash, welcome1–3, getStarted, signIn, verify, verifyWrong, setup1–4, allSet, homeActive, homeFirstDay, homeAllSettled, homeAddSheet, homeAddSheet-overlay
- The node-tree dumps of 167:11424 (Plugin API, not get_design_context; §5.3) and the diff images weren't kept. The node JSON is in `.figma-cache/nodes/3-5.json` (regenerate with `tools/fetch_figma.py`).

### 5.3 Open issues / not verified
The Figma MCP's daily limit (Education plan, 200 calls/day for the whole account) cut this read short at about 00:40 IST, after 7 successful calls. These items are still open:
1. **`get_design_context` for 167:11424 was not run.** Plugin API node dumps were used instead. They have all geometry, tokens, text styles and props, so the spec is complete without the React reference. `ref/homeConfirmPayment.png` comes from the 2× section export, not from `get_screenshot`, but it's pixel-equivalent (same export path as every other ref).
2. **The row hotspots on 24:808 / 24:520** ("Add expense hotspot" 216:28920 and presumably Record payment / Lend money / New group) were cut off by the 20 kB result limit. §2.4 uses the identical helper copies on pages 07/08/09, whose rows link to 176:17454, 177:29750, 185:25810 and 190:8356.
3. **Page 03 reactions were not re-read.** The renders and notes are identical, and nothing on page 03 changed after the first spec, so no change is expected.
4. **The Card / Balance and Card / Confirm Payment component descriptions** weren't re-read for this spec. §4.4 quotes the Settle Up spec's read of the same file. The `Show caption` property's exact description is in `components-app.md`.
5. **The tint of the Confirmed variant's check-circle icon** wasn't in any dump. Recommended: `color/icon/primary`.
6. **Pro gating of Ask Paybak** isn't stated for Home. The 11-04/05/06 frames are "Shown as a Pro member", and the paywall caption (12-01) lists Export records, Repeat and Insights (11-07 adds receipt reading), not Ask Paybak. `screens-insights-ai.md` owns the decision. Until it decides, the sparkle opens Ask Paybak for everyone.
7. **Open decisions (for the architecture plan):**
   - whether the demo dataset's default Home is Confirm payment (with Esha's pending claim) and `homeActive` is seeded without it (§3, proposal);
   - several pending claims (§3.11, proposal);
   - taps on rows without reactions (§2.2, proposals);
   - the collapse animation after Confirm (§3.9, proposal).
