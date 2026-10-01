# Paybak: Activity screens spec (Figma page "09 Activity", 77:103)

Screens: the **Activity tab** (`activityTimeline` 167:14361, `activityEmpty` 167:16311), the **Expense detail template** (`expenseVilla` 167:17847 and its states `expenseComment` 167:20257, `expenseDelete` 177:28846, `expenseDisputed` 177:29389), **Recently deleted** (`recentlyDeleted` 177:29968), the **Notifications inbox** (`notifications` 177:30749) and the two **lock-screen pushes** (`lockConfirmRequest` 186:6896, `lockReminder` 186:26798), which become real local notifications.

Read with: `tokens.md` (tokens and text styles), `components-core.md` (buttons, badge, avatar, segmented control, divider), `components-home.md` (nav header, tab bar, section header, `Row / Activity`, empty-state card, sheets), and `components-app.md` (written in parallel with this spec: `Row / Setting`, `Navigation / Push Header`, `Overlay / Alert`, `Control / Composer`, `Overlay / Toast` and the other new components). Where a component isn't in those files yet, this file describes it fully from the instances on these frames (section 9).

**Reference PNGs:** all ten are in `ref/` at exactly 2× (804 × 1748; `expenseVilla.png` is 804 × 3196 because the frame is 402 × 1598). They were cropped from 2× exports of the three sections (`download_assets` at scale 2; the export adds 40 pt of padding, which the crop removes), so they're pixel-identical to per-frame screenshots. They include the iOS status bar, keyboard, home indicator and (for the lock screens) the whole system lock screen, which you don't draw. They show the static Figma illustration where the app uses Rive (`activityEmpty`).

---

## 0. Conventions and sources

- Frame = 402 × 874 pt (the Villa detail frame is a tall 402 × 1598 "whole scroll content" frame). All `x, y` are **frame coordinates** (0,0 = the frame's top-left, which on device is the screen's top-left). Top safe area 62, bottom 34. The status bar, the home indicator, the keyboard and the whole lock screen are **system UI: don't draw them**.
- Colours are token names from `tokens.md` (the `color/` prefix is dropped) with hex. Text styles are `tokens.md` names (Manrope). Radii: `radius/card` 20, `radius/input` 14, `radius/sm` 10, `radius/full` capsule.
- Icons are `assets/icons/<name>.svg` (24 × 24, stroke 1.5, #0A0A0A; tint them and scale the whole SVG: 20 pt → stroke 1.25). **Every icon used on this page already exists** in `assets/icons/`; no new icon was needed.
- Text is verbatim: `₹` U+20B9, `−` U+2212 in negative amounts, `·` U+00B7 in subtitles, curly `’` U+2019 (Britto’s, It’s, You’re), curly quotes `“ ”` U+201C/U+201D around the dispute note. Amounts use Indian grouping (`₹18,000`, `₹1,05,000` if it ever reaches a lakh) and no decimals when the paise are zero.
- Sources, all read-only: a full Plugin-API node-tree dump of each frame (depth 7: geometry, auto-layout, bound tokens, text styles, component properties, reactions, annotations). **`get_design_context` couldn't be called for these frames** (the Figma MCP quota was exhausted; see this page's issues in `spec-digest.md`), so the spec comes from those dumps, not generated React code. They carry more exact data than the React output would. The dumps weren't kept; the same node data is in `.figma-cache/nodes/77-103.json` (regenerate with `tools/fetch_figma.py`).
- Prototype motion found on this page: push `PUSH` from the right (Figma direction LEFT) **350 ms ease-in-out**; state swaps `DISSOLVE` **300 ms ease-out**; overlay sheets `MOVE_IN` from the bottom **300 ms ease-out**; the Confirm card `SMART_ANIMATE` **250 ms ease-out**. Nothing else animates in Figma.
- Test IDs follow flow.md (`<screen>.<element>`); root containers are `screen.<screenId>`. The IDs proposed below are new and must be the same on both platforms.

## 1. Screen map

| screenId | Figma frame (node) | Container | 2× reference | Node JSON (`.figma-cache/`) | Spec |
|---|---|---|---|---|---|
| `activityTimeline` | Activity — Timeline (167:14361) | **Tab root** (Activity tab, Timeline segment) | `ref/activityTimeline.png` | `nodes/77-103.json` | §3 |
| `activityEmpty` | Activity — Empty (167:16311) | State of `activityTimeline` (no activity yet) | `ref/activityEmpty.png` | `nodes/77-103.json` | §3.8 |
| `expenseVilla` | Expense — Villa (167:17847), tall 402×1598 | **Pushed screen**: the Expense detail template | `ref/expenseVilla.png` (804×3196) | `nodes/77-103.json` | §4 |
| `expenseComment` | Expense — Comment (167:20257) | State of the Expense detail (composer focused, keyboard up) | `ref/expenseComment.png` | `nodes/77-103.json` | §4.6 |
| `expenseDelete` | Expense — Delete (177:28846) | **Alert** over the Expense detail | `ref/expenseDelete.png` | `nodes/77-103.json` | §4.7 |
| `expenseDisputed` | Expense — Disputed (177:29389) | State of the Expense detail (someone flagged it) | `ref/expenseDisputed.png` | `nodes/77-103.json` | §4.8 |
| `recentlyDeleted` | Recently deleted (177:29968) | **Pushed screen** | `ref/recentlyDeleted.png` | `nodes/77-103.json` | §5 |
| `notifications` | Notifications (177:30749) | **Pushed screen** from the Home bell | `ref/notifications.png` | `nodes/77-103.json` | §6 |
| `lockConfirmRequest` | Lock screen — Confirm request (186:6896) | **System push** (local notification), not an app screen | `ref/lockConfirmRequest.png` | `nodes/77-103.json` | §7.1 |
| `lockReminder` | Lock screen — Reminder (186:26798) | **System push** (local notification) | `ref/lockReminder.png` | `nodes/77-103.json` | §7.2 |

Not screens (section "Overlay helpers" 190:27695, prototype-only copies that let "Open overlay" work on this page):
- `↳ Not received sheet (overlay)` (190:27698) → the real sheet is page 08's **Not received sheet** (component `_Sheet / Not received` 177:24988, shown on the frame "Not received" 177:25006, 08-10), spec in `screens-settle.md`. Contents of the copy: scrim 402×874; `Not received sheet` (PBSheet Detent=Medium) at (8, 408) 386 × 458; hotspots "Send" (24, 722) 354 × 52 and "Cancel" (24, 786) 354 × 52, both close it.
- `↳ Remind sheet (overlay)` (190:27725) → the real sheet is page 08's **Remind sheet** (component `_Sheet / Remind Rohan` 176:20970, shown on the frame "Remind — Rohan" 177:21203, 08-07; `screens-settle.md`). Copy: scrim; `Remind sheet` Detent=Medium at (8, 286) 386 × 580; hotspots ✕ (328, 306) 50 × 50 and "Send in Paybak" (24, 722) 354 × 52.
- `↳ Add sheet (overlay)` (216:28703) → the real sheet is the **Home ＋ Add sheet** (`Sheet / Action Sheet`, 24:808; `screens-home.md` §5). Its row hotspots open Add expense — Empty (176:17454), Record payment — form (177:29750), Lend money — form (185:25810) and New group — Group (190:8356), all on page 06.

## 2. Section-level designer notes (verbatim)

- Section "Timeline" (167:14358). Title: “Timeline”. Subtitle: “Everything that changed, newest first, grouped by day, with the Timeline | Insights switch and an empty state.”
- Section "Expense detail" (167:17844). Title: “Expense detail”. Subtitle: “The shared expense detail template (06-10 reuses it): split, receipt, comments, history, delete, dispute and restore.”
- Section "Notifications" (177:30746). Title: “Notifications”. Subtitle: “The Home bell opens an inbox grouped Today and Earlier, with payments to confirm handled inline; two lock-screen pushes show a payment to confirm and an automatic reminder.”
- Section "Overlay helpers" (190:27695). Title: “Overlay helpers”. Subtitle: “Prototype-only copies of the page 08 sheets and the Home ＋ sheet, so Open overlay works on this page. They aren’t screens.”
  - Caption under ↳ Not received sheet (overlay) (190:27704): “Prototype helper, not a screen: a linked copy of 08-10s Not received sheet (page 08). Not received on 09-01 and 09-08 opens it with Open overlay, so the timeline or the inbox stays visible under the scrim. It is full-screen with its own scrim; the scrim, Send and Cancel close it.”
  - Caption under ↳ Remind sheet (overlay) (190:27731): “Prototype helper, not a screen: a linked copy of 08-07s Remind sheet (page 08). Rohan’s Payment overdue row on 09-08 opens it with Open overlay, so the inbox stays visible under the scrim. It is full-screen with its own scrim; the scrim, ✕ and Send in Paybak close it.”
  - Caption under ↳ Add sheet (overlay) (216:28770): “Prototype helper, not a screen: a copy of Phase 1’s Overlay — Add sheet (page 04). The tab bar ＋ on 09-01 and 09-02 opens it with Open overlay; its rows open Add expense, Record payment, Lend money (IOU) and New group on page 06, and the scrim or ✕ closes it.”

(The outer quotes above are mine; everything inside them is verbatim.)

---

## 3. Activity tab: `activityTimeline` (167:14361) and `activityEmpty` (167:16311)

**Purpose:** everything that changed, newest first and grouped by day, with the Timeline | Insights switch. It's the root of the Activity tab. The Insights segment is page 11 (`screens-insights-ai.md`, frame "Insights — September" 167:17585): same header, same segmented control with "Insights" selected, and the tab bar still on Activity (verified in `ref/insightsSeptember.png`).

**Designer notes (verbatim):**
- 09-01, under Activity — Timeline (167:14683): “Everything that changed, newest first, grouped by day; a payment waiting for you sits on top with Confirm and Not received, the Restore button opens Recently deleted, and the Fuel share is ₹2,500 ÷ 5. Rohan’s automatic reminders follow the default schedule (2 days before, on the due date, then every 3 days while overdue), so they went out Fri 25 Sep, Sun 27 Sep and tonight at 9:00 pm.”
- 09-02, under Activity — Empty (167:16387): “State: a new account with no activity yet. The segmented control stays, so Insights is still one tap away.”
- Dev-mode annotation on "List (scrolls)" (167:14377): “Vertical scroll container (overflow: vertical). Sun 27, Sat 26 and Fri 25 Sep are built below the fold.”
- Dev-mode annotation on the Confirm card (167:14385): “Confirm → State=Confirmed (interactive component, smart animate 250 ms). Not received → ↳ Not received sheet (overlay).”

### 3.1 Scaffold
Frame: vertical auto-layout, padding 62 / 20 / 34 / 20 (`layout/status-bar`, `layout/screen-margin`, `layout/home-indicator`), gap 24 (`layout/section-gap`), background `bg/primary` #FFFFFF, clips.
```
ZStack / Box (bg/primary)
 ├─ Column
 │   ├─ "Top" (FIXED, does not scroll): Nav Header Large Title + Timeline | Insights      y 62–158
 │   └─ "List (scrolls)" vertical scroll, clipped at its top edge y 182                   y 182–874
 ├─ floating glass tab bar (Active = Activity)                                             y 791–853
 └─ overlays: Not received sheet / Add sheet (scrim + sheet)
```
- "Top" is outside the scroll container in Figma, so the header and the segmented control **stay put** while the list scrolls under nothing (the list is clipped at y 182). Keep a solid `bg/primary` behind the status bar and the Top block.
- The list runs to the bottom edge (y 874) and **scrolls under the glass tab bar**, like Home. Bottom content inset (proposal, same as Home): 107 pt from the screen bottom, so the last row clears the tab bar. Add Home's bottom scroll-edge fade (screens-home.md §2.6) when the content reaches it (proposal; Figma draws no fade here).

### 3.2 Header: `Navigation / Nav Header` Type=Large Title, Show action = true (167:14363), x20 y62 362×44
- Title "Activity": **Title/1** (ExtraBold 32/38, −2 %), `text/primary`, at (20, 65) 120 × 38.
- Action: `Button / Icon` **Style=Glass**, State=Default, Badge=false, Icon = **Icon / Restore** (`restore.svg`), 44 × 44 at (338, 62), icon 24 at (348, 72) `icon/primary`. Glass = `bg/glass` #FFFFFF @72 % + 1 pt inside stroke `border/glass-highlight` + Material/Glass Small (components-home.md §2). Hotspot 44 × 44 at (338, 62): **push "Recently deleted"** (§5), 350 ms. Test ID `activity.recentlyDeleted`, accessibility label "Recently deleted".
- `activityEmpty` has Show action = **false** (no Restore button). Rule: show the button only when Recently deleted has at least one item (that's true in both frames).

### 3.3 Segmented control "Timeline | Insights" (167:14372), x20 y122 **362 × 36**
`Control / Segmented` Options=2 (components-core.md §4.3), stretched to the full content width: fill `bg/card` #F5F5F5, capsule, padding 3; two segments **178 × 30** (= (362 − 6) / 2) at x 23 and x 201, y 125.
- Segment 1 "Timeline", **Selected**: fill `bg/inverse` #0A0A0A, label **Button/Small** (SemiBold 15/20) `text/inverse`, label box (81.5, 130) 61 × 20.
- Segment 2 "Insights", not selected: no fill, label `text/secondary` #6B6B6B at (261, 130) 58 × 20.
- Tap "Insights" (hotspot 178 × 30 at (201, 125)) → switch the tab's content to Insights (page 11). In-app this is **one screen with two segments**, not a push: keep the header and the segmented control, and swap the content below (a sliding black pill is a suggestion). Test IDs `activity.segment.timeline` / `activity.segment.insights`. The segmented control stays in the empty state too (09-02).
- The selected segment is remembered for the session (proposal). Coming back to the tab keeps it.

### 3.4 List: "List (scrolls)" (167:14377), x20 y182 362 × 692, vertical scroll, clipped
Vertical stack, **gap 24** between day groups. Each **day group** is a vertical stack with **gap 8**: a `Row / Section Header` (Show action = false; title **Title/3** Bold 20/26, `text/primary`; 362 × 32), then (Today only) the pending-payment cards, then a "Rows" stack with **gap 0** of `Row / Activity` rows (Surface=Plain, 362 wide, min height 64). The rows have no dividers, no date caption (the day header gives the date) and no unread dot.

Day-group titles: **"Today"**, **"Yesterday"**, then **`EEE d MMM`** in English ("Mon 28 Sep", "Sun 27 Sep", "Sat 26 Sep", "Fri 25 Sep"). Proposal for older items: add the year when it isn't the current year ("Fri 25 Sep 2025"). Groups are newest first, and so are the rows inside a group.

Frame positions in the reference (unscrolled; everything from y 874 down is below the fold):

| Group (node) | Group rect | Header y | Content |
|---|---|---|---|
| Today (167:14378) | y 182, h 314 | 182 | Confirm card y 222–342; rows y 350 (64) and 414 (82) |
| Yesterday (167:14453) | y 520, h 104 | 520 | row y 560 (64) |
| Mon 28 Sep (167:14488) | y 648, h 168 | 648 | rows y 688, 752 |
| Sun 27 Sep (167:14545) | y 840, h 104 | 840 | row y 880 |
| Sat 26 Sep (167:14571) | y 968, h 104 | 968 | row y 1008 |
| Fri 25 Sep (167:14597) | y 1096, h 168 | 1096 | rows y 1136, 1200 (content ends y 1264) |

### 3.5 Timeline rows (sample data, verbatim) and their event types
`Row / Activity` (components-home.md §10) geometry on this screen: padding 8 / 0, gap 12, items centred vertically; 40 × 40 leading circle at x 20; text column at x 72 (title **Headline** `text/primary`; subtitle **Subheadline** `text/secondary`, gap 2); trailing amount **Amount/Medium** right-aligned to x 382. Expense rows show the category icon (20 pt, `icon/primary`) in a `bg/card` #F5F5F5 circle; payment rows show the person's avatar (Peep head) on `bg/card`.

**Change vs components-home.md:** the title now **wraps to 2 lines** ("You added Dinner at Olive Garden" is 239 × 44 and the row grows to 82). Allow 2 lines for the title and don't truncate the subtitle before 3 lines (the Notifications rows wrap to 3). Keep the min height 64, and keep the leading circle and the trailing column vertically centred.

| # | Group | Event type | Leading | Title | Subtitle | Trailing | Row rect | Tap |
|---|---|---|---|---|---|---|---|---|
| – | Today | Payment claim waiting for you | Card / Confirm Payment (§3.6) | Esha says she paid you ₹700 | Dinner at Olive Garden · UPI · 9:12 pm | Confirm / Not received | y 222, 362×120 | §3.6 |
| 1 | Today | Automatic reminder sent (to someone who owes you) | icon **Bell** | Reminder sent to Rohan | Movie tickets · ₹800 · Sent automatically | none (Show amount off) | y 350, h 64 | not linked (proposal: the expense detail, Movie tickets) |
| 2 | Today | You added an expense | icon **Food** | You added Dinner at Olive Garden (2 lines) | You paid · 4 people | **₹2,800** `text/primary` (at 323,444) | y 414, h 82 | not linked (proposal: its Expense detail; this is 06-10 "Expense added") |
| 3 | Yesterday | Payment received and confirmed | avatar **Priya** (`avatar-2`) | Priya paid you | Weekend groceries · UPI · Confirmed | **₹1,050** `text/primary` (at 327,581) | y 560, h 64 | not linked (proposal: payment detail, page 08) |
| 4 | Mon 28 Sep | Someone edited an expense | icon **Bed** | Kabir changed Villa (3 nights) | Goa Trip · Was ₹17,500 | **₹18,000** `text/secondary` (at 315,709) | y 688, h 64 | **push Expense — Villa** (167:17847), 350 ms ease-in-out |
| 5 | Mon 28 Sep | Recurring draft created | icon **Flame** | Cooking gas draft created | Flat 302 · Needs an amount | `Badge / Pill` **Muted** "Draft" 51×24 at (331,772), in place of the amount | y 752, h 64 | **→ "Recurring — Flat 302" (177:29224, page 11)** (`screens-insights-ai.md`) |
| 6 | Sun 27 Sep | Automatic reminder sent | icon **Bell** | Reminder sent to Rohan | Movie tickets · ₹800 · Sent automatically | none | y 880, h 64 | as #1 |
| 7 | Sat 26 Sep | Someone added an expense (you owe) | icon **Bolt** | Meera added Electricity bill | Flat 302 · You owe ₹450 | **₹1,350** `text/secondary` (at 328,1029) | y 1008, h 64 | not linked (proposal: its Expense detail) |
| 8 | Fri 25 Sep | Automatic reminder sent | icon **Bell** | Reminder sent to Rohan | Movie tickets · ₹800 · Sent automatically | none | y 1136, h 64 | as #1 |
| 9 | Fri 25 Sep | Someone added an expense (your share) | icon **Car** | Dev added Fuel | Goa Trip · Your share ₹500 | **₹2,500** `text/secondary` (at 324,1221) | y 1200, h 64 | not linked (proposal: its Expense detail) |

Amount rules on the timeline (derived from the rows above):
- The amount is the **expense total** (or the payment amount), **never signed** (no "−" here, unlike Home's "−₹450").
- Colour: **`text/primary` #0A0A0A** when you paid the expense or received the payment (Direction=In), **`text/secondary` #6B6B6B** when someone else paid (Direction=Out). Reminder rows and draft rows have no amount.
- Your part goes in the subtitle: "You owe ₹450" / "Your share ₹500" / "You paid · 4 people".

**Event types and copy templates** (the data model must emit these; `{…}` are fields; the Figma examples are the source):
| Event | Title | Subtitle | Leading | Amount |
|---|---|---|---|---|
| `expenseAddedByYou` | `You added {expense.title}` | outside a group: `You paid · {participantCount} people`; in a group (proposal): `{group} · You paid` | category icon | total, primary |
| `expenseAddedByOther` | `{payer} added {expense.title}` | `{group} · You owe {yourShare}`, or `{group} · Your share {yourShare}` (see the note below) | category icon | total, secondary |
| `expenseEdited` | `{editor} changed {expense.title}` | `{group} · Was {oldTotal}` (amount edits; proposal for other edits: `{group} · Edited`) | category icon | new total, primary if you paid it, else secondary |
| `paymentReceived` (confirmed) | `{payer} paid you` | `{expense or reason} · {method} · Confirmed` | payer avatar | amount, primary |
| `paymentSent` (proposal, mirror of Home's "You paid Rohan") | `You paid {name}` | `{method} · {group}` | payee avatar | amount, secondary |
| `reminderSentAutomatically` | `Reminder sent to {debtor}` | `{expense} · {amount} · Sent automatically` | icon Bell | none |
| `reminderSentByYou` (proposal) | `Reminder sent to {debtor}` | `{expense} · {amount} · Sent by you` | icon Bell | none |
| `recurringDraftCreated` | `{rule.title} draft created` | `{group} · Needs an amount` | category icon | Muted badge "Draft" |
| `paymentClaimPending` | (card, §3.6) `{payer} says she paid you {amount}` | `{expense} · {method} · {h:mm a}` | Confirm card | – |
| `expenseDeleted` / `expenseRestored` / `expenseFlagged` / `flagResolved` / `commentAdded` | not designed. Proposal: `{actor} deleted {title}` (no amount, icon Delete), `{actor} restored {title}`, `{actor} flagged {title}` (icon Flag), `You resolved {name}’s flag`, `{actor} commented on {title}` | `{group}` | category or action icon | none |

- "You owe ₹450" vs "Your share ₹500" (both Figma): proposal: use **"You owe {share}"** when your share is a debt to that payer that's still open, and **"Your share {share}"** when the group simplifies debts (Goa Trip), because there you don't owe the payer directly (Goa Trip nets to "you owe Kabir ₹1,400"). Confirm with the domain spec.
- Pronoun: Figma writes "Esha says **she** paid you". The data model needs a per-person pronoun for this copy (proposal: `she` / `he` / `they`, default `they` → "says they paid you"; the demo data sets Esha, Priya, Meera = she; Rohan, Kabir, Dev = he).
- Times use `h:mm a` in lowercase with a space ("9:12 pm", "9:00 pm", "8:00 pm").
- What goes on the timeline vs the inbox (09-08): reminders Paybak sends **for you** (the automatic ones to Rohan) are logged **on the timeline, not in the inbox**. The inbox holds things that happened **to you** (§6).
- Reminder schedule (09-01 and Settings): automatic reminders go out **2 days before the due date, on the due date, then every 3 days while overdue**, at **9:00 pm** local time. Rohan's Movie tickets were due Sun 27 Sep, so the reminders went out Fri 25 Sep, Sun 27 Sep and Wed 30 Sep ("tonight") at 9:00 pm: three `reminderSentAutomatically` events. Since there's no backend, create these events on-device when each scheduled time passes (and at launch for times that passed while the app was closed).

### 3.6 `Card / Confirm Payment` (167:14385), State=Pending, x20 y222 362 × 120
SwiftUI `PBConfirmPaymentCard`. Description (verbatim, from the component 129:2060): "The receiver’s confirm card at the top of Home and in Activity (04-06 · 08-10 under the scrim · 09-01 · 09-08), #F5F5F5 r20, padding 16, gap 12." (the full description is in components-app.md.)
- Card: fill `bg/card` #F5F5F5, radius 20, padding 16 (`layout/card-padding`), vertical gap 12.
- `top` (36, 238) 330 × 40, horizontal gap 12, centred: avatar `Avatar / Circle` Size=40 Type=Art **Esha** (`avatar-4`) on a **white** circle (`bg/primary`, because it's on a card) at (36, 238); text column (88, 238) 278 wide, gap 0: title **"Esha says she paid you ₹700"** Headline `text/primary`, 1 line, truncate end (88, 238, 278 × 22); detail **"Dinner at Olive Garden · UPI · 9:12 pm"** Footnote `text/secondary`, 1 line (88, 260, 278 × 18).
- `actions` (36, 290) 330 × 36, horizontal gap 8, both buttons FILL (161 × 36): **"Confirm"** = `Button / Primary` Small (black, label `text/inverse`, at (36, 290)); **"Not received"** = `Button / On Card` Small (white, label `text/primary`, at (205, 290)).
- **Confirm** → the card changes to **State=Confirmed** in place (smart animate, ease-out, 250 ms). Confirmed card = **362 × 72**: same padding; `top` row only: avatar (Esha), text column 242 wide: title **"Esha paid you ₹700"**, detail **"Dinner at Olive Garden · UPI · Confirmed"**, and a trailing **Icon / Check Circle** 24 × 24 (`check-circle.svg`, `icon/primary`) at card-relative (322, 24). No buttons. The rows below slide up 48 pt with the height change.
  Data effect: the claim becomes a confirmed payment (Esha's debt to you drops by ₹700; Home's "You're owed" drops from +₹2,900 to +₹2,200); show the toast "Payment confirmed" (`Overlay / Toast`, components-app.md; 16 above the tab bar); the claimant is notified (simulated). Proposal: on the next visit the claim shows as a normal `paymentReceived` row ("Esha paid you" · "Dinner at Olive Garden · UPI · Confirmed" · ₹700) in its day group, and the Confirmed card is not shown again.
- **Not received** → opens the **Not received sheet** (page 08, 08-10; component `_Sheet / Not received` 177:24988; `screens-settle.md`) as an overlay over this screen with its own 40 % scrim (MOVE_IN from the bottom, 300 ms ease-out). Its Send / Cancel / scrim close it. Send marks the claim as disputed-not-received and notifies Esha (simulated); the card disappears (spec in screens-settle.md).
- Placement rule: **pending claims sit on top of the list** (first children of the first group, above the rows) regardless of their time, newest first, one card per claim.
- Test IDs: `activity.confirm.<claimId>`, `activity.notReceived.<claimId>`.

### 3.7 Tab bar (167:14657)
`Navigation / Tab Bar` **Active = Activity** (components-home.md §5; screens-home.md §1.2): x20 y791 362 × 62, Activity item selected (bg/selected pill, black icon and label), the others inactive.
Hotspots in Figma: Home (26, 796) → **Home — Active** (24:5); Groups (100.5, 796) → **Groups** (167:14881, page 07); ＋ (175, 796) → **Add sheet** overlay (MOVE_IN from the bottom, 300 ms ease-out); Profile (308, 796) → **Profile** (64:4316, page 05). Activity = selected, no-op (proposal: tapping it again scrolls the list to the top).

### 3.8 `activityEmpty` (167:16311): state differences
Purpose (09-02): a new account with no activity yet; the segmented control stays so Insights is one tap away.
- Frame gap 0 (not 24). Top block identical except **no Restore button** (header Show action = false).
- "Content" (167:16326) x20 y158 362 × 632, vertical, **centres its child** on both axes. The child is `Card / Empty State` Type=First day (167:16327), **Show actions = false** → **362 × 334** at **(20, 307)**:
  - fill `bg/card`, radius 20, padding 24, gap 20, children centred;
  - illustration slot **(81, 331) 240 × 180** = `Illustration / Empty — First day` (7:216), **the same component as Home "First day"** → on device use **`paybak-homefirstday.riv`** (artboard `First Day` 240×180, state machine `First Day`, view model `HomeFirstDay`/`Instance`, `reduceMotion` ← OS setting, its own tap listener; see rive.md). Rive view rect = the slot (81, 331, 240, 180). Static fallback `assets/images/empty-first-day.svg`;
  - title **"No activity yet."** Title/2 `text/primary`, centred, at (44, 531) 314 × 30;
  - body **"Expenses, payments and changes will show up here."** Body `text/secondary`, centred, 2 lines, (44, 569) 314 × 48;
  - no buttons (the tab-bar ＋ is the action).
- Tab bar and hotspots as §3.7. The Insights hotspot (201, 125) still switches to Insights.
- When the account has activity but Insights is selected, nothing changes here (page 11).

### 3.9 Navigation (Activity tab)
| Element | Action | Destination | Transition |
|---|---|---|---|
| Restore glass button (338, 62) | tap | `recentlyDeleted` (§5) | push, 350 ms ease-in-out |
| "Insights" segment | tap | Insights content (page 11, "Insights — September" 167:17585) | in-place segment switch |
| Confirm (card) | tap | card → Confirmed (§3.6) | smart animate 250 ms ease-out |
| Not received (card) | tap | Not received sheet (08-10; `_Sheet / Not received` 177:24988) | overlay, move in from the bottom 300 ms ease-out |
| Row "Kabir changed Villa (3 nights)" | tap | `expenseVilla` | push 350 ms ease-in-out |
| Row "Cooking gas draft created" | tap | Recurring — Flat 302 (177:29224, page 11) | push (proposal) |
| Other expense rows | tap | their Expense detail (proposal; the template covers every expense) | push |
| Payment rows | tap | payment detail on page 08 (proposal) | push |
| Reminder rows | tap | the expense the reminder is about (proposal) | push |
| Tab bar Home / Groups / ＋ / Profile | tap | Home / Groups tab / Add sheet overlay / Profile tab | tab switch / overlay |

Pull to refresh: not designed and not needed (no backend).

---

## 4. Expense detail template: `expenseVilla` (167:17847) + states

**Purpose:** every expense opens this detail: amount, who paid, the split, the receipt, comments and a full history of edits (09-03). It is **the shared template for every expense**: 06-10 "Expense added" (177:24360, `screens-add-expense.md`) uses it, and so do the expense rows on Home, Groups, Friends and Activity.

**Container:** pushed screen (push 350 ms ease-in-out) with the **tab bar hidden** (none of these frames has one). The Villa frame is the whole scroll content (402 × 1598, "Tall frame: height = content"). On device it's one vertical scroll view with a fixed header.

**Designer notes (verbatim):**
- 09-03, under Expense — Villa (167:18399): “Every expense opens this detail: the amount, who paid, the split, the receipt, comments and a full history of edits. Edit opens Add expense in edit mode, prefilled (not linked in the prototype); the comment dates are derived, with Priya asking on 27 Sep and Kabir replying with his 28 Sep edit.”
- Dev-mode annotation on the Push header (167:18375): “Fixed when scrolling (with the status bar, header backing and home indicator). Tall frame: height = content.”
- Section subtitle: “The shared expense detail template (06-10 reuses it): split, receipt, comments, history, delete, dispute and restore.”

### 4.1 Scaffold
Frame: vertical auto-layout, padding 62 / 20 / 34 / 20, **gap 24**, `bg/primary`, vertical scroll.
- **Fixed layer** (absolute, doesn't scroll): "Header backing" rectangle **(0, 0) 402 × 106**, fill `bg/primary` (solid white, no blur), then the Push header at (20, 62). Content scrolls **under** the backing. The first child of the scroll content is a 44-tall transparent "Header space" at y 62, so at rest the hero starts at y 122.
- Bottom: the content ends at y 1564, plus the 34 bottom padding (the home-indicator inset). No tab bar.
- Scroll edge: nothing designed. Proposal: none (the white backing is enough).

### 4.2 Push header: `Navigation / Push Header` Trailing=Text (167:18375), x20 y62 362 × 44, fill `bg/primary`
(components-app.md "Navigation / Push Header".)
- **Back**: `Button / Icon` Style=Glass, Icon = Chevron Left, 44 × 44 at (20, 62) (glass: `bg/glass` @72 %, 1 pt `border/glass-highlight` inside, Material/Glass Small). Tap = pop (Figma `BACK`; hotspot (20, 62) 44 × 44). System back / edge swipe do the same. Test ID `expense.back`.
- **Title** "Expense": **Headline** `text/primary`, centred in a 200-wide box at (101, 73) (centre x 201).
- **Action "Edit"**: glass capsule 64 × 44 at (318, 62), padding 0 / 16, label **Headline** `text/primary` at (335, 73). Tap → **Add expense in edit mode, prefilled** (full-screen modal from page 06, `screens-add-expense.md`; "not linked in the prototype"). Saving the edit updates the expense, adds a History entry, notifies everyone on the expense and clears a dispute (§4.8). Proposal for the modal: title "Edit expense", confirmation "Save". Test ID `expense.edit`. Proposal: only people on the expense can edit; anyone in the group can view.

### 4.3 Content, top to bottom (Villa sample data)

**A. Hero: `Header / Amount Hero` Leading=Icon (167:17850), x20 y122 362 × 194**, vertical, gap 12 (SwiftUI `PBAmountHero`; the full description is in components-app.md: "the left-aligned hero of an expense, payment or loan detail … A 56 leading, then Title (Title/2, wraps), Amount (Title/1), Meta (Footnote, secondary) and up to 3 chips … Status chips are black or gray, never red.")
1. Leading: `Avatar / Circle` Size=56 **Type=Icon**, fill `bg/card` #F5F5F5, category icon **Bed** 24 × 24 `icon/primary`, at (20, 122) 56 × 56.
2. Text block (20, 190) 362 wide, vertical gap 2:
   - title **"Villa (3 nights)"**: **Title/2** (Bold 24/30, −1.5 %), `text/primary`, wraps (20, 190, 362 × 30);
   - amount **"₹18,000"**: **Title/1** (ExtraBold 32/38, −2 %), `text/primary` (20, 222, 126 × 38). The expense total in the expense's currency (proposal for foreign currency: "AED 1,200" plus a Footnote "≈ ₹27,000 at 22.5" under the meta line; see screens-groups.md for Dubai Weekend);
   - meta **"Paid by Kabir · 21 Sep"**: **Footnote** `text/secondary` (20, 262, 362 × 18). Template `Paid by {payer | "you"} · {d MMM}` (the expense date). Several payers (proposal): "Paid by Kabir and you · 21 Sep", or "Paid by 3 people · 21 Sep" for more than 2.
3. Chips (20, 292), horizontal gap 8, each a `Badge / Pill` (24 tall, padding 0 / 10, Caption/1, no icon):
   - chip 1 **"Goa Trip"** Muted (`bg/card`, `text/secondary`) 69 × 24 at (20, 292): the group. **Tap → Group — Goa Trip** (167:18792, page 07) (hotspot 69 × 24 at (20, 292)). Hidden when the expense isn't in a group (06-10).
   - chip 2 **"Stays"** Muted 54 × 24 at (97, 292): the category name (Bed icon = Stays). Not tappable.
   - chip 3 (hidden here): **Inverse** status chip, e.g. "Disputed" (§4.8); proposal "Recurring" or "Settled" when relevant.

**B. Share card (167:17894), x20 y340 362 × 168**: fill `bg/card`, radius 20, clips; three `Row / Setting` rows (SwiftUI `PBSettingRow`, components-app.md), each **362 × 56**, padding 12 / 16, gap 12, icon 24 at x 36, title **Headline** `text/primary` at x 72, value **Body** `text/secondary` right-aligned to x 366, divider = a 1 pt `border/subtle` #EBEBEB line inset to the title (from x 72 to x 382) at the row's bottom.
| Row | y | Icon | Title | Value | Trailing | Divider | Tap |
|---|---|---|---|---|---|---|---|
| Your share | 340 | Wallet | Your share | **₹3,600** (313, 356) | none | yes (y 395) | none |
| Due | 396 | Calendar | Due | **Fri 2 Oct** (306, 412) | none | yes (y 451) | none (proposal: the payer can change it from Edit) |
| Your Goa Trip balance | 452 | Groups | Your Goa Trip balance | **−₹1,400** (276, 468) | Chevron Right 20 `icon/tertiary` at (346, 470) | no | **→ Group — Goa Trip** (167:18792) |
Rules:
- **Your share** = your part of this expense under its split (₹18,000 ÷ 5 = ₹3,600). If you're not on the expense (proposal): hide the row. The hidden subtitle "Paid back in parts" exists on the component. Proposal: show it (Footnote, secondary) when you've partly paid this share back.
- **Due** = the expense's due date, `EEE d MMM` ("Fri 2 Oct"). Hide the row when there's no due date (proposal).
- **Your {group} balance** = your **net** balance in the whole group after simplification, signed: "−₹1,400" in `text/secondary` when you owe, "+₹…" in `text/primary` when you're owed (proposal; same colour rule as Home), "₹0" `text/tertiary` when settled. Row title template `Your {group.name} balance`. Hidden for expenses outside a group (06-10 note).
- There's **no "You're owed" row** in the template (06-10 note). For an expense you paid, the rows are the same (the Disputed frame shows "Your share ₹1,300").
- The card's height hugs its visible rows. The last visible row has no divider.

**C. Split (167:17959), x20 y532, vertical gap 8**
- `Row / Section Header` Title **"Split equally · 5 people"** (Title/3), no action, (20, 532) 362 × 32. Template `Split {mode} · {n} people` with mode "equally" (Figma). Proposal for the other editor modes (page 06: Equally · Exact · % · Shares): "Split by exact amounts", "Split by percentages", "Split by shares".
- Split card (167:17966) x20 y572 **362 × 280**: fill `bg/card`, radius 20; one `Row / Person` **Size=Compact, Trailing=Value** per participant (SwiftUI `PBPersonRow`: "the one people row …"; components-app.md), each **56** tall, padding 6 / 16, gap 12, items centred:
  - avatar `Avatar / Circle` **Size=32** Type=Art on a **white** circle (`bg/primary`) at x 36;
  - text column at x 80: name **Headline** `text/primary` (max width 180, 1 line); subtitle **Subheadline** `text/secondary` (1 line) shown **only on the payer's row**: "Paid ₹18,000";
  - trailing value: **Headline** `text/primary`, right-aligned to x 366: the person's share;
  - divider (all rows except the last): 1 pt `border/subtle` from x 80 to x 382 at the row bottom (`Divider / Line` Inset=None placed at the name's x).
  | Row | y | Avatar | Name | Subtitle | Value |
  |---|---|---|---|---|---|
  | 1 | 572 | Kabir (`avatar-6`) | Kabir | Paid ₹18,000 | ₹3,600 |
  | 2 | 628 | Arjun (`avatar-1`, the user's avatar) | You | – | ₹3,600 |
  | 3 | 684 | Priya (`avatar-2`) | Priya | – | ₹3,600 |
  | 4 | 740 | Esha (`avatar-4`) | Esha | – | ₹3,600 |
  | 5 | 796 | Dev (`avatar-5`) | Dev | – | ₹3,600 |
  **Order:** the payer first, then "You" (if you're not the payer), then everyone else in group-member order (Kabir, Priya, Esha, Dev). The Disputed frame confirms it: You (payer), Kabir, Priya, Esha, Dev. Several payers (proposal): every payer's row shows "Paid ₹…". Guests: Initials avatar + a Muted "Guest" tag next to the name (Row / Person supports it).
  Remainders: an equal split that doesn't divide evenly rotates the leftover paise fairly (06-05 note); show each person's exact share.
  The user's own avatar = their chosen preset/photo/initials (flow.md). Name "You".

**D. Receipt (167:18211), x20 y876, vertical gap 8**
- Section header **"Receipt"** (20, 876).
- Receipt card (167:18218) x20 y916 **362 × 96**: horizontal, gap 12, padding 12, items centred, fill `bg/card`, radius 20.
  - Thumbnail `Art / Receipt` **Size=Thumb** (167:18219) **56 × 72** at (32, 928), radius 10 (`radius/sm`), fill `illustration/tint` #EBEBEB. In the app **show the real receipt photo** (aspect-fill, clipped to the 56 × 72 rounded rect). The Figma art ("text drawn as line bars so it also stands for other receipts") is exported as `assets/images/receipt-thumb.svg`: use it for the demo data and as the placeholder when no photo is available.
  - Text (100, 943) 238 wide, gap 2: title **"Receipt photo"** Headline `text/primary`; subtitle **"Added by Kabir · 21 Sep"** Footnote `text/secondary`. Template `Added by {name | "you"} · {d MMM}`.
  - Chevron Right **20 × 20** at (350, 954), `icon/tertiary`.
  - Tap (not linked in the prototype): proposal: full-screen photo viewer (black background, pinch-zoom, Share and Close). Test ID `expense.receipt`.
  - No receipt (proposal, not designed): keep the section and show a Row / Setting-style card "Add receipt" (Icon / Receipt, chevron) that opens Scan receipt (page 11) or the photo picker/camera.

**E. Comments (167:18242), x20 y1036, vertical gap 8**
- Section header **"Comments"** (20, 1036).
- Body (20, 1076), vertical gap 12: the comment rows (gap 0), then the composer.
- `Row / Comment` (new component; see §9): 362 wide, horizontal gap 12, padding 8 / 0, items top-aligned:
  - avatar `Avatar / Circle` Size=32 Type=Art on `bg/card` #F5F5F5 (it's on white) at (20, y+8);
  - content (64, y+8) 318 wide, vertical gap 2: meta line (horizontal, gap 8, **baseline-aligned**): name **Headline** `text/primary` + date **Footnote** `text/tertiary`; then the comment text **Body** (Regular 16/24) `text/primary`, wraps (no limit).
  | y | Avatar | Name | Date | Text |
  |---|---|---|---|---|
  | 1076 | Priya | Priya | 27 Sep | Was breakfast included? |
  | 1140 | Kabir | Kabir | 28 Sep | Yes, all three days. Updated the total. |
  Oldest first (chronological, like a chat), unlike the History list. Date format `d MMM` (proposal for recent ones: "Today" / "Yesterday", to match the rows; and your own comments show the name "You").
- Composer `Control / Composer` **State=Empty, Pinned=False** (167:18306), x20 y1216 **362 × 52**: field fill `bg/card`, radius 14 (`radius/input`), padding 0 / 16, placeholder **"Add a comment"** Body `text/tertiary` at (36, 1230), no mic here (Show mic off). **Tap → focus → `expenseComment` state** (§4.6; Figma DISSOLVE 300 ms). Test ID `expense.composer`.
- No comments yet (proposal): hide the rows and keep the header + composer.

**F. History (167:18311), x20 y1292, vertical gap 8**
- Section header **"History"** (20, 1292).
- Rows (20, 1332), gap 0, **newest first**. `Row / History` (new; §9), horizontal gap 12:
  - rail 8 wide: vertical, gap 4, padding-top 6: a **dot 8 × 8** circle `icon/tertiary` #A3A3A3; for Position=Middle a **1 pt line** `border/subtle` #EBEBEB centred under the dot (x 23.5) that fills the rest of the row height; Position=Last has no line;
  - content (x 40) 342 wide, vertical gap 2, padding-bottom **16** (Middle) / 0 (Last): text **Subheadline** `text/primary`, wraps; date **Footnote** `text/tertiary`.
  | y | Position | Text | Date |
  |---|---|---|---|
  | 1332 (h 56) | Middle | Kabir changed the amount from ₹17,500 to ₹18,000 | 28 Sep |
  | 1388 (h 40) | Last | Kabir added this | 21 Sep |
  History copy templates: `{actor} added this` (creation; always the last row), `{actor} changed the amount from {old} to {new}` (Figma), `You resolved {name}’s flag` (09-06), and proposals: `{actor} changed the title to “{new}”`, `{actor} changed the date to {d MMM}`, `{actor} changed the split`, `{actor} changed who paid`, `{actor} changed the category to {category}`, `{actor} added a receipt`, `{name} flagged this`, `{name} removed their flag`, `{actor} deleted this` / `{actor} restored this`. `{actor}` is a first name or "You".

**G. Actions card (167:18333), x20 y1452 362 × 112**: fill `bg/card`, radius 20; two `Row / Setting` rows:
| Row | y | Icon | Title | Tone | Trailing | Divider | Tap |
|---|---|---|---|---|---|---|---|
| Flag an issue | 1452 | Flag (`icon/primary`) | Flag an issue | Default | Chevron Right 20 `icon/tertiary` (346, 1470) | yes (y 1507) | not linked (proposal below) |
| Delete expense | 1508 | Delete (**`icon/destructive` #C93636**) | Delete expense (**`text/destructive` #C93636**) | **Destructive** (no chevron) | none | no | **→ `expenseDelete` alert** (Figma DISSOLVE 300 ms ease-out) |
- "Flag an issue" (proposal; not designed): opens a `PBSheet` Medium titled "Flag an issue" with a `Control / Text Area` ("What looks wrong?") and a black "Flag expense" button. Sending sets the dispute (§4.8) and notifies everyone on the expense. Hide the row for the expense's payer (you can Edit instead) and while you already have an open flag on it; show "Remove my flag" then (proposal).
- Only red on this screen: the Delete row (Destructive tone is reserved for delete/sign out).

### 4.4 Sample data (Villa, verbatim, including below-the-fold content)
Title "Villa (3 nights)" · amount "₹18,000" · meta "Paid by Kabir · 21 Sep" · chips "Goa Trip", "Stays" · Your share "₹3,600" · Due "Fri 2 Oct" · Your Goa Trip balance "−₹1,400" · split header "Split equally · 5 people" · Kabir "Paid ₹18,000" ₹3,600 · You ₹3,600 · Priya ₹3,600 · Esha ₹3,600 · Dev ₹3,600 · Receipt "Receipt photo" / "Added by Kabir · 21 Sep" · Comments: Priya · 27 Sep "Was breakfast included?"; Kabir · 28 Sep "Yes, all three days. Updated the total." · composer placeholder "Add a comment" · History: "Kabir changed the amount from ₹17,500 to ₹18,000" 28 Sep; "Kabir added this" 21 Sep · actions "Flag an issue", "Delete expense" · header "Expense" / "Edit".
Hidden component texts that aren't content (don't show them): Row / Setting subtitle "Paid back in parts", Row / Person "Due Sun 4 Oct" amount label, and the hidden subtitle "Dinner at Olive Garden" on non-payer rows.
Consistency (the demo dataset must produce this): Goa Trip's members are Arjun (You), Kabir, Priya, Esha, Dev. Arjun paid only "Seafood dinner at Britto’s" ₹6,500 and his Goa Trip share is ₹7,900 (₹39,500 ÷ 5, screens-groups.md), so his balance is **₹6,500 − ₹7,900 = −₹1,400**. Simplified, that's "you owe Kabir ₹1,400", due Fri 2 Oct (Home "Goa Trip · Your share · Due Fri · ₹1,400").

### 4.5 Navigation (Expense detail)
| Element | Destination | Transition |
|---|---|---|
| Back (glass) / system back | pop to the previous screen | pop |
| Edit | Add expense (page 06) in edit mode, prefilled | full-screen modal (proposal) |
| Chip "Goa Trip" | Group — Goa Trip (167:18792) | push |
| Row "Your Goa Trip balance" | Group — Goa Trip (167:18792) | push |
| Receipt card | photo viewer (proposal) | full-screen modal |
| Composer | `expenseComment` state | keyboard (Figma dissolve 300 ms) |
| Flag an issue | Flag sheet (proposal) | sheet |
| Delete expense | `expenseDelete` alert | alert fades in over a 40 % scrim |

### 4.6 State `expenseComment` (167:20257): composing a comment
Designer note 09-04 (167:20506): “Tapping the comment field raises the keyboard and pins the composer above it. Send posts the comment and notifies everyone on the expense.”
- Keyboard up (system, 332 tall in the frame, y 542–874). The screen keeps the Push header (in the layout here, at (20, 62)) and the content area **(20, 106) 362 × 436** (clipped between the header bottom y 106 and the composer top y 474), scrolled so that **Receipt, Comments and History** are visible (Receipt section top at y 42 under the header; Comments header y 202, rows y 242 and y 306; History header y 394, rows y 434 and y 490). Proposal: when the composer is focused, scroll so the last comment sits just above the composer.
- The in-content composer is replaced by the **pinned composer** `Control / Composer` **State=Typing, Pinned=True** (167:20493) at **(0, 474) 402 × 68**: fill `bg/primary`, top divider 1 pt `border/subtle` full width at y 474, padding 8 / 20. Field (20, 482) 362 × 52, `bg/card`, radius 14, padding 0 / 8 / 0 / 16: typed text **"Thanks, that works for me."** Body `text/primary` (36, 496), caret 2 × 20 `bg/inverse` right after the text (231, 498), then the **send** button = `Button / Icon` **Style=Inverse**, Icon = **Arrow Up**, **36 × 36** at (338, 490) (black circle, white arrow, 20 pt glyph recommended). Position rule: **y = screen height − keyboard height − 68** (it rides the keyboard). iOS: `.safeAreaInset(edge: .bottom)` / `.toolbar(.keyboard)`; Android: `imePadding()`.
- **Send** (hotspot (338, 490) 36 × 36): posts the comment (author = You, date = now), appends it to Comments, clears the field, **notifies everyone on the expense** (simulated inbox/push for the others), dismisses the keyboard (Figma goes back to `expenseVilla` with a 300 ms dissolve). Send shows only when the trimmed text isn't empty (State=Empty has no send button). Return key = newline? Proposal: single line, return = send.
- **Tap outside** the composer (the clipped content area has "tap outside → 09-03") dismisses the keyboard and returns to the normal layout. Proposal: keep the draft text in the in-content composer (shown as State=Typing, Pinned=False, with its send button).
- Test IDs `expense.composer`, `expense.send`.

### 4.7 State `expenseDelete` (177:28846): delete confirmation alert
Designer note 09-05 (177:28927): “Delete always asks first. The expense can be restored from Recently deleted for 30 days.”
- Underneath: the Villa detail at rest (same content as §4.3, clipped at the screen bottom).
- Scrim: (0, 0) 402 × 874, `bg/scrim` #0A0A0A @40 %, covering everything including the header.
- Alert: `Overlay / Alert` **Action=Destructive** (177:28911), **300 × 182** at **(51, 346)**, centred on the screen; fill `bg/primary`, **radius 34**, padding 20, gap 20, effect Material/Glass (shadow 0, 8, blur 32, #0A0A0A @10 %). (components-app.md "Overlay / Alert"; SwiftUI `.alert` with `.cancel` + `.destructive`.)
  - Text (71, 366) 260 wide, gap 4: title **"Delete this expense?"** Headline `text/primary` centred (260 × 22); message **"Villa (3 nights) moves to Recently deleted for 30 days. Goa Trip balances update for everyone."** Subheadline `text/secondary` centred, 3 lines (260 × 60).
  - Actions (71, 472) 260 × 36, gap 8: **"Cancel"** `Button / Secondary` Small (fill `bg/card`) 126 × 36 at (71, 472); **"Delete"** `Button / Destructive` Small (fill `bg/destructive` #C93636, white label) 126 × 36 at (205, 472).
  - Message template: `{expense.title} moves to Recently deleted for 30 days. {group} balances update for everyone.` Outside a group (proposal): `{title} moves to Recently deleted for 30 days. Balances update for everyone on it.`
- **Cancel** → dismiss (back to `expenseVilla`; Figma dissolve 300 ms). **Delete** → soft-delete the expense (it stops counting in every balance at once; `deletedAt = now`, `deletedBy = you`), log history, notify the others (simulated), dismiss the alert and **pop back to the previous screen** (Figma: → Activity — Timeline, dissolve 300 ms). Proposal: toast "Expense deleted" (PBToast). Tapping the scrim does nothing (alerts need a choice); Android back = Cancel.
- Test IDs `expense.delete`, `expense.alert.cancel`, `expense.alert.delete`.

### 4.8 State `expenseDisputed` (177:29389): someone flagged the expense
Designer note 09-06 (177:29749): “State: Esha flagged your seafood dinner, so it shows as Disputed in black and gray, never red. Resolve keeps the amount, clears the badge, logs “You resolved Esha’s flag” in History and notifies Esha; editing the expense or Esha removing her flag also clears it.”
Dev-mode annotation on its Push header: “Fixed when scrolling (with the status bar and header backing).”
Differences from Villa (everything else is the template):
- **Hero** (20, 122): Leading icon **Food**; title **"Seafood dinner at Britto’s"** (curly ’); amount **"₹6,500"** (20, 222, 109 × 38); meta **"Paid by you · 22 Sep"**; chips: "Goa Trip" Muted 69 × 24 at (20, 292), **"Food"** Muted 49 × 24 at (97, 292), **"Disputed"** `Badge / Pill` **Inverse** (`bg/inverse` #0A0A0A, `text/inverse`) 74 × 24 at (154, 292). **Never red.**
- **New notice card** right under the hero, inside the "Top" stack (gap 16): `Card / Notice` **Layout=Leading, Actions=Two** (177:29485) at **(20, 332) 362 × 148** (SwiftUI `PBNoticeCard`; "All gray and black: never red."): fill `bg/card`, radius 20, padding 16, gap 16.
  - content row (36, 348) 330 wide, gap 12: icon circle `Avatar / Circle` Size=40 **Type=Icon On Card** (white circle) with **Icon / Flag** 20 at (36, 348); text column (88, 348) 278 wide, gap 2: title **"Esha flagged this expense"** Headline `text/primary`; body **"“I left before dessert. Can we check the bill?”"** Subheadline `text/secondary`, wraps (2 lines, 278 × 40), with curly double quotes around the note.
  - actions (36, 428) 330 × 36, gap 8, both FILL 161 × 36: **"Edit expense"** `Button / Primary` Small (black) at (36, 428); **"Resolve"** `Button / On Card` Small (white) at (205, 428).
  - Templates: title `{flagger} flagged this expense`; body `“{note}”`.
- Everything below moves down by 164: Share card at y 504 (Your share **₹1,300** at (318, 520); Due **Fri 2 Oct**; Your Goa Trip balance **−₹1,400** + chevron); Split header "Split equally · 5 people" at y 696; split card at y 736: **You** "Paid ₹6,500" **₹1,300**, **Kabir** ₹1,300, **Priya** ₹1,300, **Esha** ₹1,300, **Dev** ₹1,300 (y 736, 792, 848, 904, 960). Receipt, Comments, History and the Actions card follow as in the template (below the frame's visible area).
- Behaviour (09-06):
  - **Resolve** keeps the amount, clears the Disputed chip and the notice card, adds History "You resolved Esha’s flag", and notifies Esha (simulated). Test ID `expense.resolve`.
  - **Edit expense** opens the edit modal (as the header "Edit"). Saving any edit also clears the flag (and logs the edit).
  - **Esha removing her flag** (other side, simulated from the debug menu) also clears it.
  - A disputed expense **still counts** in all balances (Resolve "keeps the amount").
  - Who sees the notice: everyone on the expense. Proposal: the flagger sees the notice with the actions "Remove flag" (primary) only.
- Sample data: title "Seafood dinner at Britto’s", ₹6,500, Paid by you · 22 Sep, Goa Trip, Food, Disputed; flagger Esha; note "I left before dessert. Can we check the bill?"; shares ₹1,300 × 5.

### 4.9 How 06-10 "Expense added" uses this template (from screens-add-expense.md's note, verbatim there)
"Dinner at Olive Garden" ₹2,800, you paid, 4 people, ₹700 each, not in a group: hero chip 1 (group) hidden, "Your … balance" row hidden, share card = Your share ₹700 + Due (Sun 4 Oct, from "This weekend"); after Save the modal closes onto this detail with the toast "Expense added". There's no "You're owed ₹2,100" row.

---

## 5. `recentlyDeleted` (177:29968)
**Purpose:** deleted expenses wait here for 30 days, and anyone in the group can restore them. Pushed from the Activity header's Restore button and from **Settings › Privacy & data (12-08)** (`screens-settings.md`). No tab bar.

Designer note 09-07 (177:30022): “Deleted expenses wait here for 30 days, and anyone in the group can restore them. “24 days left” counts from 30 Sep to 24 Oct; the screen opens from the Activity header icon and from Settings › Privacy & data (12-08).”

Frame: vertical auto-layout, padding 62 / 20 / 34 / 20, **gap 16**, `bg/primary`.
1. `Navigation / Push Header` **Trailing=None** (177:29969) (20, 62) 362 × 44: glass Back (Figma `BACK`; hotspot (20, 62)) + centred title **"Recently deleted"** Headline (box (101, 73) 200 × 22). The hidden action text "Save" isn't used.
2. Helper **"Deleted items are kept for 30 days."** Footnote `text/secondary`, (20, 122) 362 × 18.
3. Deleted card (177:29976) **(20, 156) 362 × 114**: fill `bg/card`, radius 20, padding **8 / 16 / 8 / 16**, vertical, gap 0. One `Row / Activity` per deleted expense: **Type=Expense, Surface=On Card, Show detail line, Show action**, amount and date off (330 wide):
   - leading 40 circle **white** (On Card) with the category icon (Food) at (36, 193);
   - title **"Snacks"** Headline (88, 172); subtitle **"₹300 · Goa Trip"** Subheadline `text/secondary` (88, 196); detail **"Deleted by Priya on 24 Sep · 24 days left"** Footnote `text/tertiary`, wraps to 2 lines (88, 218, 177 × 36);
   - trailing **"Restore"** = `Button / On Card` Small (white) **89 × 36** at about (277, 195) (label at (293, 203)), vertically centred in the 98-tall row.
   - Templates: subtitle `{amount} · {group}` (outside a group, proposal: `{amount} · {first other person}`); detail `Deleted by {name | "you"} on {d MMM} · {n} days left` ("1 day left"; proposal "Deletes today" on the last day).
   - Several items (proposal): stack the rows in the same card, newest deletion first, with `Row / Activity` Show divider (leading-inset hairline) between rows.
4. **Restore** (not linked in the prototype): un-delete. The expense counts again in every balance, the row leaves this list, History gets "{you} restored this", and the others are notified (simulated). Proposal: toast "Expense restored" (PBToast; the copy isn't in Figma). Who can restore: anyone in the expense's group (09-07). Test ID `recentlyDeleted.restore.<expenseId>`.
5. Retention: purge items **30 days after `deletedAt`**. "24 days left" = whole calendar days from today (Wed 30 Sep) to the purge date (deleted Thu 24 Sep + 30 days = **Sat 24 Oct**). Purge on launch/foreground.
6. Empty (proposal, not designed): keep the helper line and replace the card with a Body `text/secondary` "Nothing here." centred below it. The Activity header's Restore button hides when the list is empty (§3.2).
- Sample data: "Snacks", "₹300 · Goa Trip", "Deleted by Priya on 24 Sep · 24 days left", icon Food, "Restore".

---

## 6. `notifications` (177:30749): the inbox
**Purpose:** the Home bell opens this inbox, grouped **Today** and **Earlier**; payments to confirm are handled inline. Pushed from the Home header bell (screens-home.md §1.1; test ID proposal `home.bell`). No tab bar.

Designer note 09-08 (177:30956): “The bell opens this inbox, grouped Today and Earlier; payments to confirm can be handled right here, while reminders Paybak sends for you (like tonight’s to Rohan) are logged on the timeline, not here. Times are derived: the Kabir reminder goes out at 9:00 pm, two days before Goa Trip is due, Rohan’s overdue alert came on Mon 28 Sep, and the 8:00 pm summary comes after tonight’s dinner was added, so it counts ₹2,900.”

Frame: vertical, padding 62 / 20 / 34 / 20, gap 16, `bg/primary`.
1. `Navigation / Push Header` **Trailing=Wide Text** (177:30750) (20, 62) 362 × 44:
   - glass Back (20, 62): **pop to Home** (Figma links "Home — Active" 24:5);
   - title **"Notifications"** Headline, **102-wide box at (150, 73), 1 line, truncate end**;
   - action glass capsule **"Mark all read"** at (260, 62) **122 × 44** (padding 0 / 12, max width 122), label Headline `text/primary` (273, 73) 96 × 22. Tap → every item's unread flag clears (the dots disappear) and the Home bell's badge dot hides. Proposal: disable it (50 % opacity, no action) when nothing is unread. Test ID `notifications.markAllRead`.
2. Content (177:30758) (20, 122) 362 wide, vertical, **gap 24** between groups, scrolls when taller than the screen (proposal; Figma's content exactly fills to y 774).
   - **Today** (20, 122), gap 8: `Row / Section Header` "Today"; then pending **Card / Confirm Payment** cards (same component and behaviour as §3.6: at (20, 162) 362 × 120, "Esha says she paid you ₹700" / "Dinner at Olive Garden · UPI · 9:12 pm" / Confirm (36, 230) / Not received (205, 230) → Not received sheet overlay); then rows (gap 0).
   - **Earlier** (20, 494): section header "Earlier" and rows.
   Grouping rule: **Today** = received today (local), **Earlier** = everything older, newest first in both. Proposal: keep 60 days of history.
3. Rows: `Row / Activity` Surface=Plain with **Show date** on (the trailing caption is the time for Today, "Yesterday" / `d MMM` for Earlier; Footnote `text/tertiary`, vertically centred) and **Unread** for unread items: an **8 × 8 `bg/inverse` dot** at the row's trailing edge (x 374, vertically centred), with the date moved left to end at x 362. Titles are 1 line; **bodies wrap (2–3 lines; don't truncate)**.

| # | Group | Type | Leading | Title | Body (subtitle) | Trailing | Unread | Row rect | Tap |
|---|---|---|---|---|---|---|---|---|---|
| – | Today | Payment to confirm (card) | Esha avatar | Esha says she paid you ₹700 | Dinner at Olive Garden · UPI · 9:12 pm | Confirm / Not received | – | (20,162) 362×120 | §3.6 |
| 1 | Today | Payment reminder (you owe) | icon **Calendar** | Payment reminder | You owe Kabir ₹1,400 for Goa Trip. It’s due Friday. | "9:00 pm" (311,321) | **yes** | y 290, h 80 | **→ Record payment — Kabir** (167:13635, page 08, `screens-settle.md`) |
| 2 | Today | Monthly summary | icon **Chart** | Monthly summary | September: you spent ₹23,300 on shared expenses. You’re owed ₹2,900. (3 lines) | "8:00 pm" (312,411) | **yes** | y 370, h 100 | **→ Insights — September** (167:17585, page 11: the Activity tab with Insights selected, month = that month) |
| 3 | Earlier | Payment confirmed | avatar **Priya** | Payment confirmed | Priya paid you ₹1,050 for Weekend groceries by UPI. | "Yesterday" (321,565) | no | y 534, h 80 | not linked (proposal: the payment detail, page 08) |
| 4 | Earlier | Payment overdue (owed to you) | avatar **Rohan** (`avatar-3`) | Payment overdue | Rohan owes you ₹800 for Movie tickets. It was due on 27 Sep. | `Badge / Pill` **Overdue** (red, `bg/destructive`, label "Overdue", 71 × 24 at (311, 642)) instead of the date | no | y 614, h 80 | **→ Remind sheet** (08-07; `_Sheet / Remind Rohan` 176:20970) as an overlay (move in from the bottom 300 ms ease-out; its ✕, scrim and "Send in Paybak" close it) |
| 5 | Earlier | New expense in a group | icon **Bolt** | New expense in Flat 302 | Meera added Electricity bill, ₹1,350. Your share is ₹450. | "26 Sep" (340,725) | no | y 694, h 80 | not linked (proposal: that expense's detail) |

**Inbox copy templates** (Figma examples → templates):
| Type | Title | Body | When it's created |
|---|---|---|---|
| `paymentToConfirm` | (card) `{payer} says {pronoun} paid you {amount}` | `{expense} · {method} · {h:mm a}` | a friend records a payment to you (simulated from the debug menu) |
| `paymentReminder` | `Payment reminder` | `You owe {payee} {amount} for {group or expense}. It’s due {weekday}.` ("Friday"); proposal: "It’s due today." / "It’s due tomorrow." / overdue: "It was due on {d MMM}." | your own debts, on the default reminder schedule at **9:00 pm** (the first one is 2 days before the due date: Goa Trip due Fri 2 Oct → Wed 30 Sep 9:00 pm) |
| `monthlySummary` | `Monthly summary` | `{Month}: you spent {yourShareTotal} on shared expenses. You’re owed {net}.` (proposal when you owe: `You owe {net}.`; settled: `You’re all square.`) | the **last day of the month at 8:00 pm** (it counts everything added before then: "comes after tonight’s dinner was added, so it counts ₹2,900") |
| `paymentConfirmed` | `Payment confirmed` | `{payer} paid you {amount} for {expense} by {method}.` | you confirmed (or a payment you recorded was confirmed) |
| `paymentOverdue` | `Payment overdue` | `{debtor} owes you {amount} for {expense}. It was due on {d MMM}.` | the day after the due date (Rohan: due Sun 27 Sep → alert Mon 28 Sep) |
| `newExpenseInGroup` | `New expense in {group}` | `{payer} added {expense}, {total}. Your share is {share}.` | someone else adds an expense you're on |
| `commentAdded` (proposal) | `New comment on {expense}` | `{name}: {text}` | someone comments on an expense you're on (09-04: "notifies everyone on the expense") |
| `expenseFlagged` / `flagResolved` (proposal) | `{name} flagged {expense}` / `{name} resolved your flag` | the note / `{expense}` | 09-06 |

Reminders Paybak sends **to other people** on your behalf are **not** inbox items (timeline only). Every inbox item can also be posted as a local notification if its push type is on in Settings › Notifications & reminders (`screens-settings.md`).

Sample data (verbatim): "Notifications", "Mark all read", "Today", "Earlier", the card texts above, "Payment reminder" / "You owe Kabir ₹1,400 for Goa Trip. It’s due Friday." / "9:00 pm", "Monthly summary" / "September: you spent ₹23,300 on shared expenses. You’re owed ₹2,900." / "8:00 pm", "Payment confirmed" / "Priya paid you ₹1,050 for Weekend groceries by UPI." / "Yesterday", "Payment overdue" / "Rohan owes you ₹800 for Movie tickets. It was due on 27 Sep." / "Overdue", "New expense in Flat 302" / "Meera added Electricity bill, ₹1,350. Your share is ₹450." / "26 Sep".

Navigation: Back → Home; row 1 → Record payment — Kabir (push); row 2 → Activity tab with Insights (September) selected (switch tab, not push; proposal); row 4 → Remind sheet overlay; card Confirm → Confirmed in place (+ toast "Payment confirmed"); card Not received → Not received sheet overlay. Opening an unread row marks it read.

---

## 7. Lock-screen pushes → real local notifications
Both frames are the **iOS lock screen (system UI)**: a light wallpaper (linear gradient `#F5F5F5` → `#D1D1D1` top to bottom, with `Illustration / Cover — Crowd` along the bottom edge at (−75, 688) 471 × 186), the lock glyph (Icon / Lock 24 at (189, 52)), the date line ("Wednesday 30 September") and a big clock (SF Semibold 120, centred at y 103), and one **collapsed notification** (Apple kit "Notification - Collapsed", Stack=1) at **(8, 592) 386 × 81.5**, radius 24, Liquid Glass: app icon (the Paybak mark, 38 × 38) at (22, 614), title (SF Semibold 15/17), body (SF Regular 15/18, up to 2 lines), time "now" (#4D4D4D) top-right. **Nothing here is drawn by the app**: the app only supplies the notification's title, body, category/actions, thread and deep link. The wallpaper art isn't needed.

### 7.1 `lockConfirmRequest` (186:6896): "Payment to confirm"
Designer note 09-09 (186:7136): “State: Esha’s claim arrives as a push at 9:12 pm. Tapping it opens Activity; a long press offers Confirm and Not received.”
- Clock in the frame: **9:12** (Esha claims at 9:12 pm).
- **Title: "Payment to confirm"**. **Body: "Esha says she paid you ₹700 for Dinner at Olive Garden."** Template: `{payer} says {pronoun} paid you {amount} for {expense}.`
- Tap → open the app on the **Activity tab (Timeline)**, where the pending Confirm card is on top (§3.6).
- Long press (iOS) / expanded (Android) actions: **"Confirm"** and **"Not received"**.
  - iOS: `UNNotificationCategory(identifier: "PAYMENT_CONFIRM", actions: [UNNotificationAction("CONFIRM", title: "Confirm"), UNNotificationAction("NOT_RECEIVED", title: "Not received", options: [.foreground])])`. Confirm runs in the background (records the confirmation, updates balances, clears the card, removes the notification). Not received opens the app on Activity with the Not received sheet presented (it needs the note/confirmation UI).
  - Android: channel "Payments to confirm" (importance high), `NotificationCompat.Builder` with `setContentTitle/Text`, content intent → Activity tab, and two actions: "Confirm" (a BroadcastReceiver that confirms without opening the app) and "Not received" (an activity intent that opens the sheet). Request POST_NOTIFICATIONS on Android 13+ (the setup step already asks).
- Trigger (no backend): the debug menu action "Simulate: Esha says she paid ₹700" creates the pending claim (Dinner at Olive Garden, UPI, now) and posts this notification immediately (or after 5 s so you can lock the device; proposal).
- Test hooks: the notification's `userInfo`/extras carry `claimId`; the deep link is `paybak://activity?claim=<id>` (proposal).

### 7.2 `lockReminder` (186:26798): "Payment reminder"
Designer note 09-10 (186:26991): “State: the automatic reminder two days before Goa Trip is due (Fri 2 Oct), at 9:00 pm (derived). Tapping it opens Record payment to Kabir.”
- Clock in the frame: **9:00**.
- **Title: "Payment reminder"**. **Body: "You owe Kabir ₹1,400 for Goa Trip. It’s due Friday."** (same text as the inbox row, §6 row 1). Template: `You owe {payee} {amount} for {group or expense}. It’s due {weekday}.`
- Tap → open **Record payment to Kabir** (08, 167:13635; `screens-settle.md`) prefilled with Kabir and ₹1,400 (deep link `paybak://record-payment?to=<personId>&amount=<paise>`, proposal). No actions in Figma.
- Scheduling: a local notification per open debt you owe, on the **default reminder schedule** (2 days before the due date, on the due date, then every 3 days while overdue) at **9:00 pm** local time, using `UNCalendarNotificationTrigger` / Android `AlarmManager` (inexact is fine) + `WorkManager`. Reschedule whenever balances, due dates or the Settings schedule change; cancel when the debt is settled. The same moment also creates the inbox item (§6 row 1). Respect the per-type toggles and the quiet schedule in Settings › Notifications & reminders (`screens-settings.md`).
- Debug: "Fire the Kabir reminder now" posts it immediately.

### 7.3 Other pushes (proposal, same mechanism)
Monthly summary (last day of the month, 8:00 pm → Insights), Payment overdue (the day after a due date → the Remind sheet), New expense / New comment / Flag (→ the expense detail). Each maps to its inbox type in §6 and to a Settings toggle.

---

## 8. Keyboard, scroll and system-back summary
| Screen | Scroll | Keyboard | System back (Android) |
|---|---|---|---|
| activityTimeline / activityEmpty | list scrolls under the tab bar; Top fixed | none | tab root: back → Home tab (proposal), from Home → exit |
| expenseVilla (+ states) | whole content scrolls; header + white backing (0–106) fixed | composer → pinned bar rides the keyboard (§4.6) | pop (or dismiss the keyboard / alert first) |
| recentlyDeleted | scrolls if the list is long | none | pop |
| notifications | content scrolls if long | none | pop to Home |

## 9. Reuse map
| Element | Component | Where specced | Notes for this page |
|---|---|---|---|
| Activity header | `Navigation / Nav Header` Type=Large Title, Show action | components-home.md §1 | action icon swapped to **Restore**; hidden in the empty state |
| Timeline | Insights | `Control / Segmented` Options=2 | components-core.md §4.3 | stretched to 362 wide (segments 178 × 30) |
| Day header, section titles | `Row / Section Header` (Show action off) | components-home.md §8 | – |
| Timeline & inbox rows | `Row / Activity` | components-home.md §10 | **changes**: title up to 2 lines, subtitle up to 3; Unread dot + date; Muted "Draft" badge; Overdue badge; On Card + detail line + "Restore" action (Recently deleted) |
| Payment claim card | `Card / Confirm Payment` State=Pending / Confirmed | components-app.md (`PBConfirmPaymentCard`) | §3.6 has the full geometry |
| Empty state | `Card / Empty State` Type=First day, Show actions off | components-home.md §11 | Rive `paybak-homefirstday.riv` |
| Tab bar | `Navigation / Tab Bar` Active=Activity | components-home.md §5 | – |
| Pushed headers | `Navigation / Push Header` Trailing=Text ("Edit") / None / Wide Text ("Mark all read") | components-app.md (`PBPushHeader`) | Villa: fixed with a white backing |
| Expense hero | `Header / Amount Hero` Leading=Icon | components-app.md (`PBAmountHero`) | chips = `Badge / Pill` Muted / Inverse |
| Share card rows, actions card rows | `Row / Setting` Trailing=None / Chevron, Tone=Default / Destructive | components-app.md (`PBSettingRow`) | in `bg/card` r20 groups; divider inset to the title |
| Split rows | `Row / Person` Size=Compact Trailing=Value | components-app.md (`PBPersonRow`) | subtitle only on the payer row |
| Avatars | `Avatar / Circle` 32 / 40 / 56 (Art, Icon, Icon On Card) | components-core.md §3.2 | white circle inside cards |
| Chips | `Badge / Pill` Muted / Inverse / Overdue | components-core.md §3.1 | "Disputed" = Inverse (never red) |
| Receipt thumbnail | `Art / Receipt` Size=Thumb 56 × 72 | components-app.md (`PBReceiptThumbnail`) | `assets/images/receipt-thumb.svg` placeholder; real photo in the app |
| Comment row | `Row / Comment` | **NEW here** (not described elsewhere yet) | below |
| History row | `Row / History` Position=Middle / Last | **NEW here** | below |
| Composer | `Control / Composer` State=Empty/Typing, Pinned=False/True | components-app.md (`PBComposer`) | §4.6 |
| Delete alert | `Overlay / Alert` Action=Destructive | components-app.md (`PBAlert`) | 300 × 182 here (3-line message) |
| Dispute notice | `Card / Notice` Layout=Leading, Actions=Two | components-app.md (`PBNoticeCard`) | Flag icon |
| Toasts | `Overlay / Toast` | components-app.md (`PBToast`) | "Payment confirmed" (proposals: "Expense deleted", "Expense restored") |
| Sheets opened from here | Not received sheet, Remind sheet (page 08); Add sheet (page 04) | screens-settle.md; screens-home.md §5 | overlays with their own 40 % scrim |

**NEW component `Row / Comment`** (instances 167:18251, 167:18272; SwiftUI proposal `PBCommentRow`): width fill (362), horizontal auto-layout, **gap 12**, padding **8 top / 8 bottom / 0 sides**, items top-aligned, no fill, no divider. Children: `avatar` = `Avatar / Circle` Size=32 Type=Art (fill `bg/card` on white surfaces), at (0, 8); `content` (44, 8), fills, vertical gap **2**: `meta` = horizontal, gap **8**, **baseline-aligned**, hugs: `name` Headline `text/primary` (1 line) + `date` Footnote `text/tertiary`; then `text` Body `text/primary`, wraps (auto height). Height = 16 + 22 + 2 + text height (64 for one line). Properties seen: `Name`, `Date`, `Text`.

**NEW component `Row / History`** (instances 167:18319 Middle, 167:18326 Last; SwiftUI proposal `PBHistoryRow`): width fill (362), horizontal auto-layout, **gap 12**, no padding. Children: `rail` 8 wide, fills the row height, vertical gap **4**, padding-top **6**, centred: `dot` ellipse **8 × 8** fill `icon/tertiary` #A3A3A3; `line` (Middle only) rectangle **1 pt wide** (`stroke/hairline`) fill `border/subtle` #EBEBEB, fills the remaining height (it runs from 18 below the row top to the row bottom, so consecutive rows form a continuous rail). `content` fills, vertical gap **2**, padding-bottom **16** (Middle) / **0** (Last): `text` Subheadline `text/primary` wraps; `date` Footnote `text/tertiary`. Heights: Middle 56 and Last 40 for one-line text. Properties: `Text`, `Date`, `Position` = Middle | Last (use Middle for every row but the oldest).

## 10. Art and assets
| Art | Where | Decision | File |
|---|---|---|---|
| Illustration / Empty — First day (7:216), Open Doodles "laying" | `activityEmpty` empty card slot (81, 331) 240 × 180 | **Same art as Home first day → reuse `paybak-homefirstday.riv`** (artboard `First Day`, exact slot size). It's the same Figma component, so it doesn't need a visual check. | `paybak-homefirstday.riv`; fallback `assets/images/empty-first-day.svg` |
| Art / Receipt Size=Thumb (instance 167:18219 of set 86:730) | Expense detail receipt card (56 × 72) | Not Rive; **exported** (vector) | **`assets/images/receipt-thumb.svg`** (new, 56 × 72, rounded-10 #EBEBEB tile, white paper, line bars). The components spec may also export the Art / Receipt set; if both exist they're the same art: keep one. |
| Peep heads | avatars | existing | `assets/avatars/avatar-1` Arjun (You), `-2` Priya, `-3` Rohan, `-4` Esha, `-5` Dev, `-6` Kabir |
| Illustration / Cover — Crowd | lock-screen wallpaper only | system lock screen, **not needed** | – |
| Brand / App Mark 40 | notification app icon | the system uses the app icon | `assets/brand/app-icon-1024.png` (already in the apps) |

**Icons used** (all already in `assets/icons/`, none added): restore, bell, food, bed, flame, car, bolt, calendar, chart, wallet, groups, chevron-left, chevron-right, flag, delete, arrow-up, check-circle, mic (composer, optional), plus the tab-bar set (home, groups, plus/add-button-plus, activity, profile) and lock (system glyph, not drawn).

**Assets added by this spec:** `assets/images/receipt-thumb.svg`. (The per-frame Plugin-API dumps weren't kept.) Reference renders: `ref/activityTimeline.png`, `ref/activityEmpty.png`, `ref/expenseVilla.png`, `ref/expenseComment.png`, `ref/expenseDelete.png`, `ref/expenseDisputed.png`, `ref/recentlyDeleted.png`, `ref/notifications.png`, `ref/lockConfirmRequest.png`, `ref/lockReminder.png`.

## 11. Demo data this page needs (input to `domain.md` / `seed/`; every number must come from real calculations)
"Now" = **Wed 30 Sep 2026**, evening (after 9:12 pm). The user is **Arjun Mehta** ("You"), INR.
- **Goa Trip** (group; members You, Kabir, Priya, Esha, Dev; simplify debts on; due Fri 2 Oct): "Villa (3 nights)" ₹18,000 paid by Kabir on 21 Sep, category Stays, equal split 5 (₹3,600 each), receipt photo added by Kabir on 21 Sep, amount edited by Kabir on 28 Sep from ₹17,500 to ₹18,000, comments by Priya (27 Sep) and Kabir (28 Sep); "Seafood dinner at Britto’s" ₹6,500 paid by You on 22 Sep, Food, equal 5 (₹1,300), flagged by Esha: "I left before dessert. Can we check the bill?"; "Fuel" ₹2,500 paid by Dev on Fri 25 Sep, Car/Transport, equal 5 (₹500); "Snacks" ₹300 (Food), deleted by Priya on 24 Sep; the other Goa Trip expenses to total **₹39,500** (screens-groups.md). Your balance −₹1,400 → you owe Kabir ₹1,400.
- **Flat 302** (group): "Electricity bill" ₹1,350 added by Meera on Sat 26 Sep, your share ₹450; a monthly recurring rule "Cooking gas" (variable amount) that created a draft on Mon 28 Sep (needs an amount).
- **Rohan**: "Movie tickets", Rohan owes you ₹800, due Sun 27 Sep (overdue 3 days); automatic reminders sent Fri 25 Sep, Sun 27 Sep and Wed 30 Sep, each at 9:00 pm; overdue alert on Mon 28 Sep.
- **Priya**: paid you ₹1,050 for "Weekend groceries" by UPI, confirmed yesterday (Tue 29 Sep).
- **Dinner at Olive Garden**: ₹2,800, paid by You today (before 8:00 pm), 4 people equally (₹700 each), not in a group, due Sun 4 Oct; **Esha's pending claim** ₹700 by UPI at 9:12 pm today.
- Monthly summary at 8:00 pm today: September "you spent ₹23,300" (your share of all September shared expenses) and "You’re owed ₹2,900" (Home's +₹2,900).
- Inbox unread: the 9:00 pm Kabir reminder and the 8:00 pm summary (the Home bell shows its badge).

## 12. Open questions / proposals to confirm
1. Pronoun in "says she paid you" (the model needs a pronoun per person; default "they").
2. "You owe ₹X" vs "Your share ₹X" rule on timeline rows (§3.5).
3. Undesigned toasts: "Expense deleted", "Expense restored".
4. Undesigned flows: Flag an issue sheet, receipt photo viewer, Recently deleted empty state, tap targets for most timeline/inbox rows (§3.9, §6), "Remove flag" for the flagger.
5. Restore button visibility rule (only when Recently deleted isn't empty).
6. Comment dates "Today"/"Yesterday" vs `d MMM`; send on return key.
7. `get_design_context` output is missing for all ten frames (quota); the Plugin-API dumps replaced it.
