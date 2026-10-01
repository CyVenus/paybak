# Paybak: Projects screens spec (Figma page "10 Projects" 77:104)

Screens (one **project detail** screen in four states, plus a sheet and a settings screen):

| Screen id (debug `-startScreen`) | Figma frame (node) | Figma no. | What it is | Container | Spec | 2× reference | Node JSON (`.figma-cache/`) |
|---|---|---|---|---|---|---|---|
| `projectDrone` | Project — Build a Drone (167:12476), **402 × 1749** | 10-01 | Project detail, state **Active / on track** (the full tall dashboard) | pushed screen | §3 | `ref/projectDrone.png` (804 × 3498; 1× `ref/projectDrone_1x.png`) | `nodes/77-104.json` |
| `projectOverBudget` | Project — Over budget (167:20507) | 10-03 | Same screen, state **Active / over budget** | pushed screen | §4 | `ref/projectOverBudget.png` (1× `ref/projectOverBudget_1x.png`) | `nodes/77-104.json` |
| `projectAddComponent` | Add component (167:21585) | 10-02 | **Add component** sheet over `projectDrone` | sheet (Medium) over the dashboard | §5 | `ref/projectAddComponent.png` | `nodes/77-104.json` |
| `projectSettings` | Project settings — Members & rules (177:25664) | 10-04 | Project settings (+ the Close project alert) | pushed screen | §6 | `ref/projectSettings.png` | `nodes/77-104.json` |
| `projectClosed` | Project — Closed (177:26392) | 10-05 | Project detail, state **Closed** (final settle-up plan) | pushed screen | §7 | `ref/projectClosed.png` | `nodes/77-104.json` |
| `projectArchived` | Project — Archived (177:27619) | 10-06 | Project detail, state **Archived** (Hackathon Kit) | pushed screen | §8 | `ref/projectArchived.png` | `nodes/77-104.json` |

Sections: **Project dashboard** (167:12473) and **Settings, closing and archive** (177:25562). This page has **no "Overlay helpers" / "↳ … (overlay)" copies**. The Close project alert is not drawn as a frame (it's described by a designer note and an annotation; §6.6).

Components: existing ones are in `components-core.md` / `components-home.md`. Components that first appear on the later pages are also specced in other files (`screens-record-lend-group.md` §1.2, `screens-groups.md` §1, `screens-settle.md` §0.4). §9 of this file specs every component used here, in the variants used here, so this file stands alone. Tokens: `tokens.md`. App rules: `flow.md` (FULL APP SCOPE).

How this was read: Plugin-API node dumps (all six frames, reactions, dev annotations, component descriptions), `get_design_context` for 167:12476 (cut at ~25k tokens by the server), and the REST node JSON (now `.figma-cache/nodes/77-104.json`, regenerate with `tools/fetch_figma.py`) for the complete trees. The Figma MCP quota ran out mid-read, so the other five frames were read from REST node-tree dumps (same values). The dumps weren't kept.

---

## 0. Conventions used in this file

- Frame = 402 wide (iPhone 17 Pro). All `x, y` are **frame coordinates** (0,0 = frame top-left). Top safe-area inset 62, bottom inset 34 (bottom safe edge = y 840 on an 874 frame, y 1715 on the tall frame). The status bar and home indicator are kit instances: **system UI, don't draw them**.
- Colours are token names (tokens.md) with hex. Text styles are tokens.md names (Manrope). Icons: `assets/icons/<name>.svg` (24 × 24, stroke 1.5; scale the whole SVG, the stroke scales with it: 20 → 1.25, 16 → 1.0).
- Money formatting (verbatim from Figma): `₹` U+20B9, Indian grouping (`₹12,500`, `₹61,500`, `₹1,00,000`), no decimals for whole rupees, minus sign **U+2212 "−"**, separator **U+00B7 "·"**, curly **’ U+2019** ("You’re settled…", "what’s", "it’s", "You’ll"), em dash **"—" U+2014** as the amount of a Planned part, multiplication sign **"×" U+00D7** in "Motors ×4". Curly quotes “ ” (U+201C/U+201D) in the alert title.
- Every number shown must be **computed** from the demo dataset (§2) with the rules in §1. Figma's strings are the expected output.
- Avatars (don't tint; the circle fill comes from the container): Arjun = `assets/avatars/avatar-1.svg`, Priya = `avatar-2`, Rohan = `avatar-3`, Esha = `avatar-4`, Dev = `avatar-5`, Kabir = `avatar-6`. Circle fill: `bg/card` #F5F5F5 on white surfaces, **`bg/primary` #FFFFFF inside #F5F5F5 cards** (every avatar inside a card on this page is on white).
- Arjun is the current user and is always written **"You"** on this page.
- Test IDs follow flow.md (`<screen>.<element>`, root `screen.<id>`); the list is in §12.

---

## 1. Business rules (projects)

Designer notes are quoted verbatim per screen in §3–§8. This section turns them into rules. **(proposal)** marks what Figma doesn't define.

### 1.1 Entities
- **Project** (a group with `kind = project`, created from New group › Project, see `screens-record-lend-group.md` §6.3/§8.3): `id`, `name`, `icon` (project icons: `drone.svg`, `package.svg`; any icon from the set), `members` (ordered; Figma order: You, Dev, Priya, Rohan), `createdAt` (subtitle "Active since 10 Aug"), `budget` (optional ₹), `rule` (**Equal | Percent | Fixed**), `ruleValues` (per member %, or per member ₹; empty for Equal), `pool` (bool, "Collect money upfront"; off in the demo), `status` (**Active | Closed | Archived**), `closedAt`, `archivedAt`, `currency` (INR in the demo), optional description / cover photo (set at creation).
- **Component** (a part of the project): `id`, `name`, `estimate` (₹, optional), `actual` (₹, optional), `status` (**Planned | Bought | Done**), `payer` (member), `receipt` (optional photo), `createdAt`, `statusChangedAt`.
- **Project payment**: a normal recorded payment (Record payment / Settle Up flow, pending until the receiver confirms) with `projectId` set. Figma: "Rohan and Priya can still record their payments to Dev".

### 1.2 Budget, spending and projection (Card / Budget)
- `spent` = Σ `actual` of components whose status is **Bought or Done**. Planned components never count toward `spent` or anyone's share.
- `plannedExtra` = Σ `estimate` of **Planned** components (a Planned part with no estimate adds 0).
- `projection` = `spent + plannedExtra`. Demo: 52,000 + 6,000 (GPS module) = **₹58,000** → "Planned items bring it to ₹58,000".
- `percentUsed` = round(spent / budget × 100), half up → 52,000 / 60,000 = 86.67 → **"87% used"**; 18,400 / 20,000 → "92% used".
- Active, spent ≤ budget: right stat = "₹{budget − spent} left" ("₹8,000 left").
- Active, spent > budget (**over budget**): the stats row is replaced by the warning "₹{spent − budget} over budget" ("₹1,500 over budget") with Icon / Alert. Designer: "Only the bar’s overflow and the warning turn red."
- Closed / Archived: right stat = "₹{budget − spent} under budget" ("₹8,000 under budget", "₹1,600 under budget"); over budget at close **(proposal)**: "₹{spent − budget} over budget" in `text/destructive` in the same slot, and the bar stays State=Over.
- Planned line (Active only; `Show planned line`): plannedExtra > 0 → "Planned items bring it to ₹{projection}"; plannedExtra = 0 → "All planned items are bought." (verbatim, 10-03). Hidden when Closed/Archived ("no projection").
- **No budget (proposal):** hide "of ₹…", the bar, the stats row and the planned line; keep "Spent" + amount. (New projects start with an empty budget, per 06-18.)
- **Projection over the budget while spent is under it (proposal):** the gray projected segment is capped at the full track; no red (red = real overspend only).

### 1.3 Fair share ("Paid vs fair share")
Designer (10-01): "Fair share uses actual costs only, planned items count only toward the ₹58,000 projection". (10-02): "Planned items feed only the projection; everyone’s share changes only when an actual cost is added."
- `paid[m]` = Σ `actual` of Bought/Done components with payer m.
- `share[m]` (always a share of `spent`):
  - **Equal**: spent / n. Demo 52,000 / 4 = **₹13,000** → rule line "Equal split · ₹13,000 each so far".
  - **Percent**: spent × pct[m] / 100 (percents add up to 100).
  - **Fixed** **(proposal for the math)**: members' fixed amounts add up to the **budget** (or to what they agree to put in when there's no budget); share of what's spent so far = spent × fixed[m] / Σfixed. So shares always add up to `spent`, and at close each share is final.
  - Rounding: work in the currency's minor unit (paise); floor each share and give the leftover units one by one to members in member order, rotating the start member per project (same fairness rule as the split editor, 06-05 "the leftover paisa rotate fairly"). Display whole rupees when the value is whole, otherwise 2 decimals.
- `net[m]` = paid[m] − share[m] + (confirmed project payments m **sent**) − (confirmed project payments m **received**). Demo: Dev 25,500 − 13,000 = **+₹12,500**; You 13,000 − 13,000 = **0**; Priya 9,000 − 13,000 = **−₹4,000**; Rohan 4,500 − 13,000 = **−₹8,500**.
- Row value: net > 0 → Value=Owed ("+" + amount, `text/primary`); net < 0 → Value=Owe ("−" + amount, `text/secondary`); net = 0 → Value=Neutral, "Settled".
- Row order: **net descending** (Dev +12,500, You 0, Priya −4,000, Rohan −8,500 = Figma order). Ties: member order.
- Mini bars (Control / Progress Bar Small with the fair-share mark): `scale = max(max(paid), max(share))`; fill = paid[m] / scale; mark at share[m] / scale. Demo scale 25,500 → Dev 100 %, You 51 % (141.7 of 278), Priya 35 % (98.1), Rohan 18 % (49.1); mark at 13,000 / 25,500 = 51 % on every row (Equal). With Percent/Fixed the mark moves per row.
- Nothing spent yet (spent = 0) **(proposal)**: hide "Paid vs fair share" and "Who owes whom"; keep History.
- Project spending **stays out of Insights** (designer 10-01; Insights 11-01 note: "Projects are tracked on their own dashboards"). The debts that result (who owes whom) are real debts between people and **do** count in each person's balance and Settle Up **(proposal; flow.md says balances come from expenses and splits)**. In the demo the user's project net is 0, so Home's +₹2,900 / −₹1,850 are unaffected.

### 1.4 Who owes whom (simplified transfers)
- Run the app's simplify-debts routine on `net[]`: repeatedly pair the largest debtor with the largest creditor, transfer min(|debt|, credit), until all nets are 0 (at most n − 1 transfers). Demo: Rohan → Dev ₹8,500, Priya → Dev ₹4,000. Order: amount descending.
- Row title: Active "{from} owes {to}"; Closed plan "{from} pays {to}". The user is "You": **(proposal)** "You owe Dev" / "Dev owes you", "You pay Dev" / "Dev pays you".
- Footnote under the card:
  - Active, your net = 0: "You’re settled in this project." (verbatim). **(proposal)** net < 0: "You owe ₹{x} in this project."; net > 0: "You’re owed ₹{x} in this project."
  - Closed, your net = 0: "You’re settled. It becomes a permanent record once everyone has paid." (verbatim). **(proposal)** otherwise: "It becomes a permanent record once everyone has paid."
  - Nobody owes anything (Active) **(proposal)**: no transfers card; footnote "Everyone’s settled in this project."
- Pending payments don't change the plan until confirmed (same rule as elsewhere in the app). **(proposal)** Add a line to the footnote while any are pending: "1 payment is waiting for confirmation." / "{n} payments are waiting for confirmation."
- **Pool** ("Collect money upfront", off in the demo). Designer: "When on, members pay into a pool first and purchases come out of it." and (10-04) "the pool is off, so whoever buys a part pays for it and is paid back." **(proposal)** With the pool on: the project creator holds the pool; each member owes the holder their share of the **budget** (or of the projection when there's no budget) minus what they've paid in; purchases are recorded with payer = pool; Who owes whom shows each member's remaining contribution to the holder. Persist the toggle either way.

### 1.5 Component lifecycle
- **New** (Add component sheet, §5): status **Planned**, payer **You** (10-02: "New parts start as Planned with you as the payer"), estimate optional, actual empty. "the button enables once a name is entered".
- **Planned → Bought**: the part now has an `actual` cost (> 0) and a payer; from this moment it counts in `spent` and changes everyone's share ("everyone’s share changes only when an actual cost is added"; sheet footnote "Shares update as soon as an actual cost is added.").
- **Bought → Done** (meaning inferred from the sample: Motors ×4 and Frame, both Dev's): the part is fitted/finished. Money is identical to Bought; only the badge changes (Inverse). Done also requires an actual cost.
- Validation in the sheet **(proposal)**: Bought/Done need an actual cost > 0 (keep the button disabled and show the Actual cost helper as the hint); typing an actual cost while Planned switches the status to Bought; clearing it returns to Planned.
- Row presentation by status (exactly as Figma):

| Status | Leading (40, white circle) | Subtitle | Amount | Badge |
|---|---|---|---|---|
| Planned | Avatar / Circle **Type=Icon On Card**, `tag.svg` 20 `icon/primary` (no person) | "Est. ₹6,000" (**proposal** no estimate: "No estimate") | "—" (U+2014) Amount/Medium **`text/tertiary`** | Badge / Pill **Muted** "Planned", fill overridden to **`bg/card-pressed` #EBEBEB** |
| Bought | payer's art (`Avatar / Circle` 40 Art) | "{payer} · Est. ₹{estimate}", or "{payer} · Unplanned" when there was no estimate; payer "You" for the user | actual, Amount/Medium `text/primary` | Badge / Pill **On Card** "Bought" (white) |
| Done | payer's art | same as Bought | actual, `text/primary` | Badge / Pill **Inverse** "Done" (black, white label) |

  Over budget shows GPS module after Dev bought it: Dev art, "Dev · Est. ₹6,000", ₹9,500, "Bought".
- List order (matches Figma): Planned first, then Bought, then Done; inside a group, most recent status change first. The demo dates in §2 produce Figma's order.
- Edit **(proposal; no frame)**: tapping a component row (Active only) opens the same sheet titled "Edit component", prefilled, button "Save changes", plus `Button / Text` Destructive "Delete component" under the button (confirm with Overlay / Alert Destructive "Delete {name}?" · "Its cost comes off the project." · Cancel · Delete).
- Closed/Archived: components are locked (no Add component, rows not tappable). A Planned part "stays listed as Planned but counts toward nothing" (10-05).

### 1.6 Project lifecycle
- **Active → Closed**: Project settings › Close project → Overlay / Alert (§6.6) → confirm. Components lock, the projection disappears, the plan is frozen from the current nets, and the screen shows the Closed state (10-05). The gear disappears (no settings on a closed project: the Closed header has Trailing=None).
- **Closed → Archived** automatically when every transfer of the final plan is recorded **and confirmed** (10-05: "once both are confirmed the project archives like Hackathon Kit (10-06)"). If all nets are already 0 when closing, archive immediately **(proposal)**. Archived = "a permanent record: there is no gear and no Add component, and every figure is final." Subtitle becomes "Project · {n} members · Closed {d MMM}" and the title gets the "Archived" tag.
- Where projects appear: Groups tab list (`screens-groups.md` §3.2): Active/Closed projects in the main list (Row / Group Type=Project with the budget bar), Archived ones under "Archived" ("Project · Closed 30 Aug", "Read-only"). Reopening is not designed **(no reopen)**.
- History: the project emits timeline events (component added / bought / done, payment recorded / confirmed, project closed / archived) into the Activity timeline with the project attached; History opens `activityTimeline` filtered to the project (§3.10).

---

## 2. Demo dataset (projects part; dates relative to "now" = Wed 30 Sep 2026)

Load with the other demo data (flow.md "Data"). All figures on §3–§8 must come out of these records.

### 2.1 Build a Drone (Active)
- `id` "build-a-drone", icon `drone.svg`, currency INR, members **You (Arjun), Dev, Priya, Rohan** (this order), createdAt **10 Aug 2026**, budget **₹60,000**, rule **Equal**, pool **off**, status Active, no project payments.
- Components (status-change dates chosen to give Figma's list order):

| # (list order) | Name | Status | Estimate | Actual | Payer | Status changed |
|---|---|---|---|---|---|---|
| 1 | GPS module | Planned | ₹6,000 | — | You | 20 Sep |
| 2 | Camera | Bought | — (Unplanned) | ₹7,500 | Dev | 18 Sep |
| 3 | Transmitter | Bought | ₹5,000 | ₹5,000 | You | 9 Sep |
| 4 | ESCs and propellers | Bought | ₹8,000 | ₹8,000 | You | 2 Sep |
| 5 | Battery | Bought | ₹5,000 | ₹4,500 | Rohan | 27 Aug |
| 6 | Flight controller | Bought | ₹9,000 | ₹9,000 | Priya | 21 Aug |
| 7 | Motors ×4 | Done | ₹12,000 | ₹12,000 | Dev | 16 Aug |
| 8 | Frame | Done | ₹6,000 | ₹6,000 | Dev | 12 Aug |

- Check: spent 7,500 + 5,000 + 8,000 + 4,500 + 9,000 + 12,000 + 6,000 = **52,000**; projection **58,000**; 87 % used; ₹8,000 left. Paid: Dev 25,500, You 13,000, Priya 9,000, Rohan 4,500. Share 13,000. Nets +12,500 / 0 / −4,000 / −8,500. Transfers Rohan → Dev ₹8,500, Priya → Dev ₹4,000.
- Groups list row (from `screens-groups.md`): "Build a Drone", "Project · 4 members", "You’re settled", "₹52,000 of ₹60,000", "₹8,000 left".

### 2.2 Over-budget variant (debug only; `-startScreen projectOverBudget`)
Designer 10-03: "at a later point Dev buys the GPS module for ₹9,500". Apply to 2.1: GPS module → Bought, actual ₹9,500, payer Dev, changed "now". Result: spent **₹61,500** of ₹60,000 → "₹1,500 over budget"; plannedExtra 0 → "All planned items are bought."; fair share 15,375 each → Dev +19,625, You −2,375, Priya −6,375, Rohan −10,875 (Figma doesn't draw these sections in 10-03; they must still compute).

### 2.3 Closed variant (debug only; `-startScreen projectClosed`)
2.1 with status **Closed**, closedAt = now. GPS module stays Planned (counts toward nothing). Plan: "Rohan pays Dev ₹8,500", "Priya pays Dev ₹4,000".

### 2.4 Hackathon Kit (Archived; part of the normal demo data)
Figma shows: icon `package.svg`, members **You, Esha, Dev, Kabir**, "Closed 30 Aug", budget **₹20,000**, spent **₹18,400** ("92% used", "₹1,600 under budget"), everyone settled. Figma doesn't show its components or payments, so **(proposal, any records that sum the same are fine)**:
- createdAt 1 Aug 2026, rule Equal, closedAt = archivedAt = **30 Aug 2026**.
- Components (all Done): "Raspberry Pi kits" ₹7,200 (Esha) · "Sensors" ₹4,600 (Dev) · "Display" ₹3,400 (You) · "Cables and adapters" ₹3,200 (Kabir) = ₹18,400. Share ₹4,600 → nets Esha +2,600, Dev 0, You −1,200, Kabir −1,400.
- Confirmed project payments: You → Esha ₹1,200 (29 Aug), Kabir → Esha ₹1,400 (30 Aug). All nets 0 → archived.

---

## 3. `projectDrone`: Project — Build a Drone (167:12476), state Active / on track

**Purpose** (section subtitle, verbatim): "Build a Drone: the tall dashboard (budget, components, paid vs fair share, history, who owes whom) · Add component sheet · over-budget state."
**Container:** pushed screen (from the Groups list row, `groupsList`; also from Activity/notification rows that belong to the project). **No tab bar.** Background `bg/primary`.
**Scroll:** Figma overflow = VERTICAL. The content column scrolls; the **top bar is fixed** and the **Add component button is pinned** over a white fade (annotations below).

### 3.1 Designer notes (verbatim)
- Section title (167:12474): "Project dashboard"
- Section subtitle (167:12475): "Build a Drone: the tall dashboard (budget, components, paid vs fair share, history, who owes whom) · Add component sheet · over-budget state."
- Caption 10-01 (167:13634): "The dashboard answers three things at a glance: how much of the budget is gone, what’s still planned, and who owes whom. Fair share uses actual costs only, planned items count only toward the ₹58,000 projection, History opens the Activity timeline filtered to this project (the prototype links to 09-01), and project spending stays out of Insights."
- Annotation on "Top bar" (167:13599): "Status bar, header and gear stay fixed while the dashboard scrolls."
- Annotation on "Add component" (167:18523): "Pinned: fixed to the bottom of the viewport over a white fade."

### 3.2 Layout skeleton
Frame auto-layout: V, padding **118 top** (62 status + 44 header + 12), **20 sides** (`layout/screen-margin`), **110 bottom** (34 safe + 52 button + 24), gap **24** (`layout/section-gap`). Sections: Summary (y118), Components (y443), Paid vs fair share (y1019), History (y1365), Who owes whom (y1445–1639).
```
ZStack / Box (bg/primary)
 ├─ ScrollView (content column: side 20, top inset = safe top + 56 → content starts 12 below the header; bottom inset 110 incl. safe area)
 │    Summary → Components → Paid vs fair share → History → Who owes whom (+ footnote)
 ├─ Top bar (fixed): white 0…safeTop+44, Push header (back · gear)
 ├─ Fade (fixed, bottom 96, not interactive)
 └─ Add component button (fixed, bottom = safe-area bottom)
```

### 3.3 Top bar (fixed), "Top bar" 167:13599 (0, 0, 402, 106)
- Frame fill `bg/primary` #FFFFFF, padding top 62 (`layout/status-bar`), sides 20 (`layout/screen-margin`). No shadow, no divider, no blur (content simply scrolls under the opaque white bar).
- `Navigation / Push Header` **Trailing=Icon**, Show title = **false** (title "Build a Drone" is set but hidden), (20, 62, 362, 44), H space-between, centred:
  - **Back**: `Button / Icon` Style=Glass (44 × 44 at (20, 62); fill `bg/glass` #FFFFFF @72 %, 1 pt inside `border/glass-highlight`, effect Material/Glass Small), icon `chevron-left.svg` 24 at (30, 72), `icon/primary`. Tap → pop (prototype: URL to Groups 167:14881 = `groupsList`).
  - **Gear**: `Button / Icon` Style=Glass (338, 62, 44, 44), icon `settings.svg` 24 at (348, 72). Tap → push `projectSettings` (prototype: NAVIGATE 177:25664, **PUSH LEFT, EASE_IN_AND_OUT, 350 ms**).
  - iOS: `NavigationStack` toolbar (`.topBarLeading` back is the system glass back; `.topBarTrailing` gear) with `.toolbarBackground(.white)`; or a custom fixed bar reproducing the geometry. Android: custom 44 dp row with the glass fallback; system back = pop.
- **(proposal)** When the title row scrolls under the bar, fade in a centred Headline "Build a Drone" in the bar (Push Header Show title) over 150 ms. Figma doesn't show a scrolled state.

### 3.4 Summary (20, 118, 362, 301), V gap 16 (`space/16`)
**a) Title row** — `Header / Title Row` Leading=Tile, Show members = true, Show subtitle = true, Show tag = false; (20, 118, 362, 96), H gap 16 (`space/16`).
- `tile`: `Avatar / Circle` Size 56 Type=Icon, fill `bg/card` #F5F5F5, icon `drone.svg` 24 at (36, 134) `icon/primary`.
- `content` (92, 118, 290 fill, 96), V gap 8: `meta` (56 tall, V gap 2, vertically centred): title "Build a Drone" **Title/2** `text/primary` (92, 120, 148, 30), 1 line, truncates; subtitle "Project · 4 members · Active since 10 Aug" **Subheadline** `text/secondary` (92, 152, 290, 20), 1 line, truncates.
- `members`: `Avatar / Stack` Count=4 (92, 182, 104, 32): 32-pt Art avatars, fill `bg/card`, 2-pt **outside** white ring (`bg/primary`), −8 overlap, later ones on top, at x 92, 116, 140, 164: **Arjun, Dev, Priya, Rohan** (member order). More than 4 members **(proposal)**: show 3 + a 32-pt Initials-style circle "+{n−3}" (Caption/1).
- Subtitle format: "Project · {n} members · Active since {d MMM}" (Active), "Project · {n} members" (Closed), "Project · {n} members · Closed {d MMM}" (Archived). Add the year when it isn't the current year **(proposal)**.

**b) Budget card** — `Card / Budget` State=**On track** (20, 230, 362, 189): fill `bg/card`, radius 20 (`radius/card`), padding 16 (`layout/card-padding`), V gap 12 (`space/12`).
1. `head` (36, 246, 330, 58), V gap 2: "Spent" **Footnote** `text/secondary` (36, 246, 37, 18); `amount row` H gap 8, **baseline-aligned**: "₹52,000" **Title/1** `text/primary` (36, 266, 129, 38) + "of ₹60,000" **Headline** `text/secondary` (173, 280, 84, 22).
2. `bar`: `Control / Progress Bar` Size=**Large**, State=**Projected**, Show mark = false, (36, 316, 330, 12): track `chart/track` #EBEBEB capsule; **projected** segment `chart/bar` #D1D1D1 capsule, width 330 × projection / budget; **fill** segment `chart/fill` #0A0A0A capsule on top, width 330 × spent / budget. (Figma drew 87 % = 287.1 and 97 % = 320.1 from the rounded percents; compute exact ratios: 286.0 and 319.0.)
3. `stats` (36, 340, 330, 20), H space-between: "87% used" **Subheadline** `text/secondary` · "₹8,000 left" **Subheadline** `text/primary` (293, 340, 73, 20).
4. `planned` (36, 372, 330, 31), V gap 12: `Divider / Line` Inset=None 330 × 1 `border/subtle` #EBEBEB; "Planned items bring it to ₹58,000" **Footnote** `text/secondary` (36, 385, 330, 18), wraps.
- Not tappable (no reaction in Figma).

### 3.5 Components (20, 443, 362, 552), V gap 8 (`space/8`)
- `Row / Section Header` Show action = false, title "Components" **Title/3** (20, 446).
- **Components card** (20, 483, 362, 512): fill `bg/card`, radius 20, padding **0 top/bottom, 16 sides** (`layout/card-padding`), V gap 0, clips. Eight `Row / Activity` rows (Type=Payment, Direction=In, **Surface=On Card**, Show amount = true, **Show date = false, Show badge = true**, Show detail line = false, Unread = false, Show action = false, Show divider = true except the last), each **330 × 64** at x 36, y = 483 + 64 × i.
- Row internals (row-relative): H gap 12 (`space/12`), padding 8 top/bottom (`space/8`), items centred. Leading 40 × 40 at (0, 12), white circle. Text column at (52, 10), fills (≈197–206 wide), V gap 2: title **Headline** `text/primary` 1 line + subtitle **Subheadline** `text/secondary` max 2 lines. Trailing column (hug, right edge 330, **right-aligned**, V gap 2): amount **Amount/Medium** at row y + 8, badge (24 tall) at row y + 32. Divider: `Divider / Line` Inset=Leading, absolute at the row bottom (y + 63), hairline from row x 52 to 330 (frame 88 → 366), `border/subtle`.

| # | Frame y | Leading | Title | Subtitle | Amount | Badge (style, frame rect) |
|---|---|---|---|---|---|---|
| 1 | 483 | Icon On Card `tag.svg` 20 at (46, 505) | GPS module | Est. ₹6,000 | — (`text/tertiary`, 14 × 22 at (352, 491)) | Muted "Planned", fill **`bg/card-pressed` #EBEBEB** (297, 515, 69, 24) |
| 2 | 547 | Dev art | Camera | Dev · Unplanned | ₹7,500 | On Card "Bought" (302, 579, 64, 24) |
| 3 | 611 | Arjun art | Transmitter | You · Est. ₹5,000 | ₹5,000 | On Card "Bought" |
| 4 | 675 | Arjun art | ESCs and propellers | You · Est. ₹8,000 | ₹8,000 | On Card "Bought" |
| 5 | 739 | Rohan art | Battery | Rohan · Est. ₹5,000 | ₹4,500 | On Card "Bought" |
| 6 | 803 | Priya art | Flight controller | Priya · Est. ₹9,000 | ₹9,000 | On Card "Bought" |
| 7 | 867 | Dev art | Motors ×4 | Dev · Est. ₹12,000 | ₹12,000 | **Inverse** "Done" (315, 899, 51, 24) |
| 8 | 931 | Dev art | Frame | Dev · Est. ₹6,000 | ₹6,000 | Inverse "Done"; **no divider** |

- Badge geometry (Badge / Pill): 24 tall, padding 0/10, label **Caption/1**; Muted/On Card label `text/secondary`, Inverse label `text/inverse` on `bg/inverse`.
- Tap a row (Active) → Edit component sheet **(proposal, §1.5)**. Pressed fill **(proposal)**: `bg/card-pressed` over the row inside the card.
- Empty list **(proposal)**: a single 64-tall row-height message inside the card, Subheadline `text/secondary` centred: "No components yet. Add the first part below."

### 3.6 Paid vs fair share (20, 1019, 362, 322), V gap 8
- `Row / Section Header` "Paid vs fair share" (Title/3, (20, 1022)), no action.
- **Fair share card** (20, 1059, 362, 282): `bg/card`, radius 20, padding 16, V gap 8.
  - Rule line "Equal split · ₹13,000 each so far" **Footnote** `text/secondary` (36, 1075, 189, 18). Formats: Equal "Equal split · ₹{share} each so far" (verbatim pattern); **(proposal)** Percent "Percent split · shares follow each person’s %"; Fixed "Fixed amounts · shares follow each person’s amount".
  - `Bars` (36, 1101, 330, 224), V gap 0: four `Row / Bar` Leading=Avatar (§9.6), 330 × 56 at y 1101, 1157, 1213, 1269:

| Row | Avatar | Title | Caption | Value (variant) | Mini bar fill (of 278) | Mark |
|---|---|---|---|---|---|---|
| Dev | Dev | Dev | Paid ₹25,500 | "+" "₹12,500" (Owed, `text/primary`) | 278 (100 %) | 141.7 (51 %) |
| You | Arjun | You | Paid ₹13,000 | "Settled" (Neutral, Headline `text/primary`) | 141.7 (51 %) | 141.7 |
| Priya | Priya | Priya | Paid ₹9,000 | "−" "₹4,000" (Owe, `text/secondary`) | 98.1 (35 %) | 141.7 |
| Rohan | Rohan | Rohan | Paid ₹4,500 | "−" "₹8,500" (Owe) | 49.1 (18 %) | 141.7 |

  Mark x inside the bar = W × p_mark (the 2-pt tick is centred on it; Figma: padding-left 140.73 = W × 0.51 − 1). Not tappable.

### 3.7 History (20, 1365, 362, 56)
- Card: `bg/card`, radius 20, clips. One `Row / Setting` Trailing=Chevron, Tone=Default, Show icon = true, no value/subtitle/badge/divider: icon `activity.svg` 24 at (36, 1381) `icon/primary`; title "History" **Headline** `text/primary` (72, 1382, 262, 22); chevron `chevron-right.svg` 20 at (346, 1383) `icon/tertiary`.
- Tap → `activityTimeline` **filtered to this project** (prototype: URL to Activity — Timeline 167:14361, page 09). Push with the standard push transition. The Activity spec (`screens-activity.md` §3) owns the timeline; pass a project filter (show the project name as the pushed screen's title, **proposal**: Push Header title "Build a Drone · History", back returns here).

### 3.8 Who owes whom (20, 1445, 362, 194), V gap 8
- `Row / Section Header` "Who owes whom" (Title/3, (20, 1448)).
- **Transfers card** (20, 1485, 362, 128): `bg/card`, radius 20, padding 0, V gap 0, clips. Two `Row / Transfer` (§9.7), 362 × 64:
  1. (20, 1485): `Avatar / Pair` 32 Rohan → Dev at (36, 1501) (from 36, arrow `arrow-right.svg` 16 `icon/tertiary` at (72, 1509), to 92); title "Rohan owes Dev" **Headline** `text/primary` (136, 1506, 160, 22), 1 line; amount "₹8,500" **Amount/Medium** `text/primary` (308, 1506); divider from x 136 to 382 at y 1548.
  2. (20, 1549): Priya → Dev, "Priya owes Dev", "₹4,000", no divider.
- Footnote "You’re settled in this project." **Footnote** `text/secondary` (20, 1621, 174, 18), directly under the card (gap 8), outside the card.
- Tap **(proposal)**: a row where **you** pay → Record payment prefilled (to, amount, project attached; `screens-record-lend-group.md` §2); a row where **you** receive → the Remind sheet for that person (`screens-settle.md`); other rows aren't tappable (other people record their own payments; in the demo the debug menu simulates them, §11).

### 3.9 Pinned bottom: fade + Add component
- **Fade** "Fade" 167:13612, rectangle (0, 1653, 402, 96) = the bottom **96 pt of the viewport** (on an 874 screen: y 778 → 874), above the content, below the button, not interactive. Linear gradient top → bottom: #FFFFFF @0 % at 0 → #FFFFFF @85 % at 45 % → #FFFFFF @100 % at 100 %.
- **Add component**: `Button / Primary` Size=Large, Leading icon = true, Icon / Plus (20 × 20 `icon/inverse`): **(20, 1663, 362, 52)** = bottom edge on the bottom safe-area line (874-screen: y 788–840). Capsule, `bg/inverse`, padding 0/24 (`space/24`), gap 8, label "Add component" **Button/Large** `text/inverse` (151, 1678). Pressed `bg/inverse-pressed` #2B2B2B.
- Tap → Add component sheet (§5). Prototype: NAVIGATE 167:21585, **MOVE_IN from BOTTOM, EASE_IN_AND_OUT, 300 ms**.
- Android: pin above the navigation bar inset (`windowInsetsPadding(WindowInsets.navigationBars)`), fade height 96 dp from the screen bottom.

### 3.10 Navigation (projectDrone)
| Element | Destination |
|---|---|
| Back (glass) / system back / edge swipe | pop → previous screen (`groupsList` in the prototype) |
| Gear | push `projectSettings` (350 ms ease-in-out) |
| Component row | Edit component sheet **(proposal)** |
| History | push `activityTimeline` filtered to the project |
| Transfer row | Record payment / Remind **(proposal, §3.8)** |
| Add component | present Add component sheet (§5), 300 ms from the bottom |

### 3.11 Keyboard
None on this screen.

### 3.12 Assets
`drone.svg`, `chevron-left.svg`, `settings.svg`, `tag.svg`, `activity.svg`, `chevron-right.svg`, `arrow-right.svg`, `plus.svg`; avatars `avatar-1` (Arjun), `avatar-2` (Priya), `avatar-3` (Rohan), `avatar-5` (Dev). No illustration.

---

## 4. `projectOverBudget`: Project — Over budget (167:20507), state Active / over budget

Same screen as §3 with the over-budget data (§2.2). The Figma frame is a 402 × 874 crop (overflow NONE): it shows the top bar, Summary, the Components card (clipped at the bottom by the fade/button) and the pinned button. It **doesn't draw** Paid vs fair share, History or Who owes whom; on device those sections are present and computed (§2.2). The button has no prototype link in this frame.

### 4.1 Designer notes (verbatim)
- Caption 10-03 (167:20736): "State: at a later point Dev buys the GPS module for ₹9,500, so spending reaches ₹61,500 of ₹60,000. Only the bar’s overflow and the warning turn red."
- Component description (Card / Budget, excerpt): "State=Over budget: bar State=Over + mark at the budget (fill 97.6% = ₹60,000 of ₹61,500), and the stats row becomes Icon / Alert 16 + Warning (Subheadline, “₹1,500 over budget”) in text/destructive: the only red, with the over segment."

### 4.2 Differences from §3
1. **Budget card** `Card / Budget` State=**Over budget** (20, 230, 362, 189):
   - Spent "₹61,500" Title/1 (36, 266, 121, 38); "of ₹60,000" at (165, 280).
   - `bar`: Control / Progress Bar Large State=**Over**, Show mark = **true** (36, 316, 330, 12). Scale = spent. Black fill `chart/fill` from 0 to **330 × budget / spent = 321.95** (square right end; the track capsule rounds the left end); red `chart/over` #C93636 from 321.95 to 330 (the track capsule rounds its right end; Figma's segment is 40 wide and clipped by the track). **Mark** at the budget point: tick 2 × 18 at x 320.95…322.95 (bar-relative), y −3 (frame y 313), `chart/fill` #0A0A0A with a **1-pt outside white** (`bg/primary`) outline, drawn above the track (not clipped).
   - `warning` row replaces `stats` (36, 340, 330, 20), H gap 6 (`space/6`), centred: `alert.svg` **16 × 16** at (36, 342) `icon/destructive` #C93636; "₹1,500 over budget" at (58, 340, 130, 20) in `text/destructive` #C93636, **Manrope Bold 14 / line 20** (the text node has no style; the component description says Subheadline, which is Medium 14/20; follow the drawn **Bold**).
   - Planned line text "All planned items are bought." (plannedExtra = 0).
2. **GPS module row**: leading = **Dev art** (was the tag icon), subtitle "Dev · Est. ₹6,000", amount "₹9,500" `text/primary`, badge On Card "Bought" (302, 515, 64, 24).
3. Everything else (rows 2–8, header, top bar, fade at (0, 778, 402, 96), button at (20, 788, 362, 52)) is unchanged.
- Nothing else is red: amounts, fair-share values and the "Who owes whom" rows stay black/gray (foundations rule 3).

### 4.3 Reference render
`ref/projectOverBudget.png`: the black fill ends at the white-outlined tick; a short red cap follows to the track end; the red alert + warning line sits under the bar.

---

## 5. `projectAddComponent`: Add component (167:21585), sheet over the dashboard

**Purpose:** add a part to the project (usually Planned, with an estimate).
**Container:** `Sheet / Container` Detent=Medium over `projectDrone` (at its scroll position), 40 % scrim. Background frame = the dashboard (On track data, identical to §3's first 874 pt, including the pinned button and fade) under the scrim.

### 5.1 Designer notes (verbatim)
- Caption 10-02 (167:21771): "New parts start as Planned with you as the payer, and the button enables once a name is entered. Planned items feed only the projection; everyone’s share changes only when an actual cost is added."
- Sheet / Container description: see `components-home.md` §15 (verbatim there; this frame also uses the newer "Show header" boolean: "For a sheet with no title and no close (08-10), turn Show header off … so the content starts 20 below the top").

### 5.2 Scrim and sheet
- **Scrim** (0, 0, 402, 874): `bg/scrim` #0A0A0A @40 %, covers everything incl. the status bar area. Tap → close (prototype: NAVIGATE 167:12476, **MOVE_OUT to BOTTOM, EASE_IN_AND_OUT, 300 ms**).
- **Sheet** "Add component sheet" (8, 248, 386, 618): `bg/primary`, radius 40 (`radius/sheet`) all corners, **bottom edge y 866 (8 above the screen bottom)**, V auto-layout, padding 8 top / 16 sides / 28 bottom (`space/8`, `space/16`, `space/28`), gap 8, children centred.
  1. Grabber (kit, Mode=Light) 60 × 4 at (171, 256), capsule, #CCCCCC (≈ `bg/indicator`). **(proposal)** Swipe down dismisses.
  2. Header (24, 268, 354, 50), padding-left 4, justify end: title "Add component" **Title/3** `text/primary` (28, 280, 300, 26), 1 line; **close** = kit "Button - Liquid Glass - Symbol" Style=Glass, **50 × 50** at (328, 268), SF Symbol `xmark` Semibold 19 #1A1A1A (recipes: `screens-home.md` §5.2: iOS `.glassEffect(.regular.interactive(), in: .circle)`; Android white 50 dp disc + 0.5 dp #E8E8E8 ring + `close.svg` drawn 38 dp tinted #1A1A1A). Tap → close (hotspot 186:27046 → MOVE_OUT BOTTOM 300 ms).
  3. Content slot (24, 326, 354, 512) → form "Add component form", V gap **16** (`space/16`), padding 0/**4** (`space/4`) sides → field width **346** at x 28:

| Element | Rect | Component / style | Content |
|---|---|---|---|
| Name | (28, 326, 346, 80) | `Control / Input Field` State=Default, label on, helper off | label "Name" **Subheadline** `text/secondary` at (28, 326); field (28, 354, 346, 52) `bg/card` radius 14, padding 0/16; placeholder "e.g. Spare propellers" **Body** `text/tertiary` at (44, 368) |
| Costs row | (28, 422, 346, 106), H gap 8 | two Input Fields, each **169** wide | **Estimated cost**: label "Estimated cost" (28, 422), field (28, 450, 169, 52), placeholder "₹0" at (44, 464). **Actual cost**: label "Actual cost" (205, 422), field (205, 450, 169, 52), placeholder "₹0" at (221, 464), helper "Add it once it’s bought" **Footnote** `text/tertiary` (205, 510, 136, 18) |
| Status | (28, 544, 346, 64), V gap 8 | label + `Control / Segmented` Options=3 stretched | label "Status" **Subheadline** `text/secondary` (28, 544, 45, 20); segmented (28, 572, 346, 36), padding 3, `bg/card`, capsule; segments **113.3 × 30** at x 31, 144.3, 257.7: **"Planned" (selected: `bg/inverse`, `text/inverse`)**, "Bought", "Done" (`text/secondary`), labels **Button/Small** |
| Paid by card | (28, 624, 346, 112) | `bg/card` radius 20, V gap 0, clips | two `Row / Setting` (Trailing=Chevron), 346 × 56 |
| · Paid by | (28, 624, 346, 56) | Row / Setting, Show value, Show divider | icon `wallet.svg` 24 at (44, 640); title "Paid by" **Headline** (80, 641); value "You" **Body** `text/secondary` (299, 640, 27, 24); chevron 20 at (338, 642) `icon/tertiary`; divider hairline (80 → 374, y 679) |
| · Add receipt | (28, 680, 346, 56) | Row / Setting | icon `camera.svg` 24 at (44, 696); title "Add receipt" (80, 697); chevron (338, 698); no divider |
| Footnote | (28, 752, 346, 18) | **Footnote** `text/secondary` | "Shares update as soon as an actual cost is added." |
| Button | (28, 786, 346, 52) | `Button / Primary` Large, State=**Disabled**, no icon | "Add component": fill `bg/disabled` #E0E0E0, label **Button/Large** `text/disabled` #A3A3A3 (137, 801). Enabled: `bg/inverse` + `text/inverse` |

- Height check: 8 + 4 + 8 + 50 + 8 + 512 + 28 = 618.

### 5.3 Behaviour
- Opens with Status = **Planned**, Paid by = **You**, all fields empty. **(proposal)** Autofocus Name (keyboard up).
- Add component enables when Name is non-empty (trimmed) (designer). **(proposal)** plus the Bought/Done rule of §1.5 (needs Actual cost > 0).
- Estimated / Actual cost: currency fields (decimal pad; `₹` prefix from the project currency, Indian grouping as you type, max 2 decimals). Placeholder "₹0" in `text/tertiary`; filled text `text/primary`.
- Status segmented: single selection; **(proposal)** typing an actual cost switches Planned → Bought automatically; clearing it switches back to Planned.
- **Paid by** → a member picker sheet (reuse the Add expense **Paid by** sheet, 06-04, listing the project's members; picking one closes it and sets the value; "You" for the user). Hidden/disabled? No: always enabled, even while Planned (the payer is stored and used when it's bought).
- **Add receipt** → photo picker / camera (attach a photo; reading items is Pro and lives on the Scan receipt flow; here it only attaches). After attaching **(proposal)**: value "Photo added" on the row.
- **Add component** → create the component (§1.5), close the sheet (MOVE_OUT bottom 300 ms), and the new row appears at the top of the Components card. **(proposal)** Toast "Component added" (Overlay / Toast, 50 above the bottom… here 12 above the pinned button).
- ✕ / scrim / swipe down / Android back → dismiss without saving (**proposal**: if anything was typed, confirm with Overlay / Alert Destructive "Discard this component?" · Keep editing · Discard).

### 5.4 Keyboard
The sheet stays inset 8 from the sides; when the keyboard shows, the sheet's bottom sits **8 above the keyboard** and its content scrolls so the focused field is visible (the form is taller than the space left). iOS: a custom overlay sheet (like the Home Add sheet) with `.safeAreaInset`/keyboard avoidance; Android: `imePadding()` on the sheet + `bringIntoViewRequester`. Decimal pad for the two cost fields.

### 5.5 Motion
Open: scrim fades 0 → 40 % while the sheet slides up from off-screen, 300 ms ease-in-out (prototype MOVE_IN BOTTOM). Close: reverse, 300 ms (MOVE_OUT BOTTOM). Reduce Motion: cross-fade.

### 5.6 Navigation (projectAddComponent)
| Element | Destination |
|---|---|
| ✕, scrim, swipe down, back | close → `projectDrone` |
| Paid by | Paid by picker sheet (06-04 style) |
| Add receipt | system photo picker / camera |
| Add component | save, close → `projectDrone` with the new row |

### 5.7 Assets
`wallet.svg`, `camera.svg`, `chevron-right.svg`, `close.svg` (Android ✕; iOS SF Symbol `xmark`).

---

## 6. `projectSettings`: Project settings — Members & rules (177:25664)

**Purpose:** change the contribution rule, members, budget and pool; close the project.
**Container:** pushed screen from the dashboard gear (PUSH LEFT 350 ms). No tab bar. Background `bg/primary`.
**Scroll:** Figma overflow NONE, but the content ends at y 850 (+34 padding = 884 > 874), so build it as a **ScrollView** (it scrolls 10 pt at default size; more with Dynamic Type / the keyboard). Frame auto-layout: V, padding **62 / 20 / 34 / 20**, gap **24**.

### 6.1 Designer notes (verbatim)
- Section title (177:25563): "Settings, closing and archive"
- Section subtitle (177:25564): "Project settings with the contribution rule and the close confirmation · the closed project with its final settle-up plan · the archived Hackathon Kit."
- Caption 10-04 (177:25984): "With Equal, each share is read-only; Percent and Fixed turn the rows into editable fields that must add up, and the pool is off, so whoever buys a part pays for it and is paid back. Close project first shows an Overlay / Alert, “Close Build a Drone?” / “Components lock and everyone sees the final plan.”, with Cancel · Close project. Both buttons stay black, because closing isn’t destructive, and confirming leads to 10-05."
- Annotation on "Close project" (177:25947): "Opens Overlay / Alert (Action=Primary): “Close Build a Drone?” · Cancel · Close project. Confirming leads to 10-05."

### 6.2 Header and rule (20, 62, 362, 454), V gap 16
- `Navigation / Push Header` **Trailing=None**, Show title = **true**: back (Glass, `chevron-left.svg`) at (20, 62); title "Project settings" **Headline** `text/primary`, centred, box (101, 73, 200, 22). Back → pop to the dashboard (prototype: NAVIGATE 167:12476, PUSH RIGHT, EASE_IN_AND_OUT, 350 ms). **Fixed header (proposal)**: keep the header pinned with a white background while the list scrolls, like the dashboard.
- **Contribution rule** (20, 122, 362, 394), V gap 12:
  - `Rule controls` (20, 122, 362, 102), V gap 8: `Row / Section Header` "Contribution rule" (Title/3, (20, 125)); `Control / Segmented` Options=3 **stretched to 362** (20, 162, 362, 36): segments **118.7 × 30** at x 23, 141.7, 260.3: **"Equal" (selected)**, "Percent", "Fixed"; helper "Everyone pays the same share of what’s spent." **Footnote** `text/secondary` (20, 206, 362, 18).
  - **Members card** (20, 236, 362, 280): `bg/card`, radius 20, V gap 0, clips:
    - Four `Row / Person` Size=**Compact**, Trailing=**Value** (§9.9), 362 × 56 at y 236, 292, 348, 404: avatar 32 (white circle) at (36, y+12); name **Headline** `text/primary` at (80, y+17); value "25%" **Headline** `text/primary` right-aligned, right edge 366; divider hairline from x 80 to 382 at the row bottom.
    - Rows: **You (Arjun) 25%**, **Dev 25%**, **Priya 25%**, **Rohan 25%**.
    - `Row / Setting` Trailing=Chevron "Add member" (20, 460, 362, 56): icon `user-add.svg` 24 at (36, 476); title "Add member" **Headline** (72, 477); chevron 20 at (346, 478) `icon/tertiary`; no divider.

### 6.3 Budget (20, 540, 362, 106)
`Control / Input Field` State=**Filled**, label on, helper on: label "Budget" **Subheadline** `text/secondary` (20, 540); field (20, 568, 362, 52) `bg/card` radius 14; value "₹60,000" **Body** `text/primary` at (36, 582); helper "You’ll see a warning if spending goes over." **Footnote** `text/tertiary` (20, 628, 253, 18). Decimal pad; empty = no budget (placeholder "₹0" `text/tertiary`, **proposal**).

### 6.4 Pool (20, 670, 362, 100), V gap 8
- Card (20, 670, 362, 56) `bg/card` radius 20 with `Row / Setting` **Trailing=Toggle**: icon `wallet.svg` 24 at (36, 686); title "Collect money upfront" **Headline** (72, 687, 218, 22); kit **Toggle - Switch** 64 × 28 at (302, 684), **off** (track kit Labels/Tertiary #3C3C43 @30 %, white knob 38 × 24 at inset 2 on the left). On: track `bg/inverse` #0A0A0A, knob right. iOS `Toggle("", isOn:).labelsHidden().tint(.bgInverse)`; Android custom 64 × 28 dp switch (don't use a stock Material switch as-is).
- Helper "When on, members pay into a pool first and purchases come out of it." **Footnote** `text/secondary` (20, 734, 362, 36), 2 lines.

### 6.5 Close project (20, 794, 362, 56)
Card `bg/card` radius 20 with `Row / Setting` **Trailing=None**, **Tone=Default** (not destructive): icon `lock.svg` 24 at (36, 810) `icon/primary`; title "Close project" **Headline** `text/primary` (72, 811, 294, 22). Tap → alert §6.6. Hidden when the project isn't Active (the settings screen is only reachable while Active anyway).

### 6.6 Close confirmation: Overlay / Alert, Action=Primary (not drawn as a frame)
From the note and the annotation (verbatim copy): title **“Close Build a Drone?”** (with the curly quotes only as quoting in the note: the title text is `Close Build a Drone?`, pattern "Close {project name}?"), message **"Components lock and everyone sees the final plan."**, buttons **"Cancel"** · **"Close project"**. "Both buttons stay black, because closing isn’t destructive" → Overlay / Alert **Action=Primary** = Button / Secondary Small "Cancel" + Button / Primary Small "Close project" (no red).
- Component (`screens-record-lend-group.md` §1.2.12): card 300 wide, radius 34, `bg/primary`, padding 20, gap 20, Material/Glass; title **Headline** centred; message **Subheadline** `text/secondary` centred (gap 4); two Small buttons 126 × 36 sharing the width, gap 8; centred over the 40 % scrim; not dismissed by tapping outside.
- iOS: `.alert("Close Build a Drone?", isPresented:) { Button("Cancel", role: .cancel) {}; Button("Close project") { close() } } message: { Text("Components lock and everyone sees the final plan.") }` (default role, not `.destructive`). Android: custom Dialog drawing the card.
- Confirm → close the project (§1.6), pop settings and show the dashboard in the **Closed** state (10-05). Cancel → dismiss.

### 6.7 Behaviour
- **Rule segments** (designer): Equal → member values read-only ("{100/n}%": 25 % for 4; non-integer → one decimal, e.g. "33.3%", same rule as `screens-record-lend-group.md` §8.3). **Percent / Fixed** → each member's trailing value becomes an **editable field** ("must add up"):
  - **(proposal)** Field: 88 × 36 capsule-free inline field, `bg/primary` radius 10 (`radius/sm`), right-aligned **Headline** value with "%" suffix (Percent) or "₹" prefix (Fixed); number pad / decimal pad.
  - Prefill when switching: Percent → the equal split (25 % each); Fixed → budget / n each (₹15,000) or spent / n when there's no budget.
  - Helper copy **(proposal, same as the New group spec)**: Percent "Set each person’s share. Shares must add up to 100%."; Fixed "Set a fixed amount for each person." While the values don't add up, show the helper in `text/destructive` with the remainder ("25% left" / "₹5,000 left", **proposal**) and **don't apply** the new rule (the old rule stays in force). Leaving the screen with invalid values discards them.
- Rule changes apply immediately to the dashboard (shares, bars, transfers).
- **Add member** → the people picker used for New group › Add people (`screens-record-lend-group.md` §6), multi-select, existing members disabled. Equal redistributes automatically; Percent/Fixed need re-balancing (helper turns red). **(proposal)** Removing a member: swipe-to-delete on a member row, only if their paid and share are 0.
- **Budget**: saves on blur; empty = no budget.
- **Collect money upfront**: toggles `pool` (§1.4).
- No Save button (Trailing=None): every change saves as it's made.

### 6.8 Keyboard
Budget (and Percent/Fixed fields) raise the decimal/number pad; the ScrollView scrolls the focused field above the keyboard (iOS default; Android `imePadding()` + `bringIntoViewRequester`). Dismiss on scroll/tap outside.

### 6.9 Navigation (projectSettings)
| Element | Destination |
|---|---|
| Back / system back | pop → project detail (PUSH RIGHT 350 ms) |
| Equal / Percent / Fixed | switch rule in place |
| Add member | people picker (modal) |
| Collect money upfront | toggle |
| Close project | Overlay / Alert → confirm → project detail, Closed state |

### 6.10 Assets
`chevron-left.svg`, `user-add.svg`, `chevron-right.svg`, `wallet.svg`, `lock.svg`; avatars 1, 2, 3, 5.

---

## 7. `projectClosed`: Project — Closed (177:26392), state Closed

Same project detail screen in the **Closed** state (§1.6), data §2.3. Frame: V, padding 62 / 20 / 34 / 20, gap 24, overflow NONE (fits). The header is inside the flow in Figma; implement the same **fixed top bar** as §3.3 but **without the gear** (Push Header Trailing=None, no title). The frame has **no hotspots** (back is not linked in the prototype; it pops like §3).

### 7.1 Designer notes (verbatim)
- Caption 10-05 (177:26745): "State: Build a Drone right after it’s closed. Components lock and the GPS module stays listed as Planned but counts toward nothing, so the budget shows ₹52,000 with no projection; Rohan and Priya can still record their payments to Dev, and once both are confirmed the project archives like Hackathon Kit (10-06)."

### 7.2 Content top to bottom
1. Push header (20, 62, 362, 44): back only.
2. Summary (20, 118, 362, 370), V gap 16:
   - Title row as §3.4a with subtitle **"Project · 4 members"** (no "Active since").
   - **Read-only notice**: `Card / Notice` Layout=Leading, Actions=None, Show title = true, Show badge = false (20, 230, 362, **96**): `bg/card` radius 20 padding 16; icon circle `Avatar / Circle` 40 **Icon On Card** (white) with `lock.svg` 20 at (46, 256); title **"Closed · Read-only"** **Headline** `text/primary` (88, 246, 278, 22); body **"Components are locked. Payments can still be recorded."** **Subheadline** `text/secondary` (88, 270, 278, 40), 2 lines.
   - **Budget** `Card / Budget` State=**Closed** (20, 342, 362, **146**, no planned line): "Spent" (36, 358); "₹52,000" Title/1 (36, 378) + "of ₹60,000" (173, 392); bar Large State=**Default**, Show mark = false (36, 428, 330, 12), fill **286.0** (= 330 × 52,000 / 60,000, capsule); stats (36, 452): "87% used" `text/secondary` · **"₹8,000 under budget"** `text/primary` (226, 452, 140, 20).
3. **Final settle-up plan** (20, 512, 362, 212), V gap 8:
   - `Row / Section Header` "Final settle-up plan" (Title/3, (20, 515)).
   - Transfers card (20, 552, 362, 128), `bg/card` radius 20: `Row / Transfer` **"Rohan pays Dev"** "₹8,500" (Rohan → Dev, divider from x 136) at y 552; **"Priya pays Dev"** "₹4,000" (Priya → Dev) at y 616, no divider.
   - Footnote **"You’re settled. It becomes a permanent record once everyone has paid."** **Footnote** `text/secondary` (20, 688, 362, 36), 2 lines.
4. **(proposal, from the note "the GPS module stays listed as Planned")**: below the footnote, the **Components** section exactly as §3.5 but read-only (rows not tappable, no pressed state), then **History** (§3.7). The Figma frame doesn't draw them; the caption says the components stay listed. No Paid vs fair share section (the plan replaces it) and no Add component button, no fade.

### 7.3 Behaviour
- As payments are recorded (by you in Record payment, or simulated for others from the debug menu) and **confirmed**, recompute the plan: settled transfers disappear. When no transfer is left → archive automatically (§1.6) and the screen switches to the Archived layout (§8) (**proposal**: cross-fade 300 ms, toast "Project archived").
- Transfer row taps as §3.8 (you pay → Record payment prefilled with the project; you receive → Remind).

### 7.4 Navigation (projectClosed)
| Element | Destination |
|---|---|
| Back / system back | pop |
| Transfer row (you involved) | Record payment / Remind **(proposal)** |
| History (proposal section) | `activityTimeline` filtered |

---

## 8. `projectArchived`: Project — Archived (177:27619), Hackathon Kit

Same screen in the **Archived** state, data §2.4. Frame: V, padding 62 / 20 / 34 / 20, gap 24, **overflow VERTICAL** (content ends at y 896 → scrolls). Fixed top bar as §7 (back only, no gear, no title).

### 8.1 Designer notes (verbatim)
- Caption 10-06 (177:28071): "An archived project is a permanent record: there is no gear and no Add component, and every figure is final. Coming in under budget keeps the bar black."

### 8.2 Content top to bottom
1. Push header (20, 62): back only (hotspot → URL Groups 167:14881 = `groupsList`).
2. Summary (20, 118, 362, 350), V gap 16:
   - Title row: tile `package.svg` 24 in the 56 `bg/card` circle; title **"Hackathon Kit"** Title/2 (92, 120, 161, 30) + **tag** `Badge / Pill` Style=**Muted** **"Archived"** (261, 123, 73, 24) 8 pt after the title (Show tag = true); subtitle **"Project · 4 members · Closed 30 Aug"**; members stack **Arjun, Esha, Dev, Kabir** (`avatar-1`, `avatar-4`, `avatar-5`, `avatar-6`).
   - Read-only notice `Card / Notice` (20, 230, 362, **76**): `lock.svg` icon circle; title **"Read-only"**; body **"Nothing here can be edited."** (1 line).
   - Budget `Card / Budget` State=Closed (20, 322, 362, 146): **"₹18,400"** "of ₹20,000"; bar Default fill **303.6** (92 %); **"92% used"** · **"₹1,600 under budget"**. Under budget → black bar (designer).
3. **Final settle-up plan** (20, 492, 362, 116), V gap 8: header "Final settle-up plan"; **Plan notice** `Card / Notice` Layout=Leading (20, 532, 362, 76) with `check-circle.svg` 20 in the white icon circle, title **"Everyone is settled"**, body **"No payments left in this project."**
4. **Members** (20, 632, 362, 264), V gap 8: header "Members" (Title/3); card (20, 672, 362, 224) `bg/card` radius 20 with four `Row / Person` Size=Compact, **Trailing=Muted** (status **Subheadline** `text/secondary`, right edge 366), 56 each at y 672, 728, 784, 840: **You "Settled"**, **Esha "Settled"**, **Dev "Settled"**, **Kabir "Settled"** (dividers from x 80 except the last).
5. **(proposal)** Components (read-only, as §3.5) and History (§3.7) after Members, so the "permanent record" can still be read. Not drawn in Figma.

### 8.3 Navigation (projectArchived)
| Element | Destination |
|---|---|
| Back / system back | pop (prototype: `groupsList`) |
| History (proposal) | `activityTimeline` filtered |
Nothing else is tappable.

---

## 9. Components used on this page (reuse map + specs of the NEW ones)

| Component (Figma id) | Where | Status |
|---|---|---|
| Button / Icon Style=Glass (10:79) | back, gear | components-core §2.3 / components-home §2 |
| Button / Primary Large (9:36) (Leading icon Plus; Disabled) | Add component (dashboard, sheet) | components-core §2.1 |
| Button / Text | (hidden "See all" in section headers) | components-core §2.2 |
| Badge / Pill (11:46) Muted / On Card / Inverse | component badges, "Archived" tag | components-core §3.1 (**Muted fill override `bg/card-pressed` on the Planned badge**) |
| Avatar / Circle (11:136) 32 / 40 / 56, Art / Icon / Icon On Card | everywhere | components-core §3.2 |
| Avatar / Stack (11:417) Count=4 | title row | components-core §3.3 |
| Control / Segmented (12:249) Options=3, stretched | rule, status | components-core §4.3 (stretched: segment = (W − 6) / 3) |
| Control / Input Field (12:296) Default / Filled | sheet fields, Budget | components-core §4.4 |
| Divider / Line (12:302) | cards | components-core §4.5 |
| Row / Section Header (13:223) Show action=false | section titles | components-home §8 |
| Row / Activity (13:477) Type=Payment, Surface=On Card, Show badge, no date | component rows | components-home §10 (badge in the date slot) |
| Sheet / Container (118:1017) Detent=Medium | Add component sheet | components-home §15 |
| Navigation / Push Header (97:1082) Trailing=Icon / None | headers | **NEW here** → §9.1 (also `screens-record-lend-group.md` §1.2.2) |
| Header / Title Row (128:1857) Leading=Tile | title row | **NEW** → §9.2 (also `screens-record-lend-group.md` §1.2.11) |
| Card / Budget (145:2106) On track / Over budget / Closed | budget card | **NEW** → §9.3 |
| Control / Progress Bar (116:1099) Large/Small, Default/Projected/Over, mark | budget + share bars | **NEW** → §9.4 |
| Row / Setting (97:996) Chevron / Toggle / None | History, Paid by, Add receipt, Add member, pool, Close project | **NEW** → §9.5 (also `screens-record-lend-group.md` §1.2.6) |
| Row / Bar (143:2156) Leading=Avatar, Owed / Neutral / Owe | Paid vs fair share | **NEW** → §9.6 |
| Row / Transfer (128:1604) + Avatar / Pair (116:1005) Size=32 | Who owes whom, final plan | **NEW** → §9.7 |
| Card / Notice (129:1976) Layout=Leading, Actions=None | read-only notices, "Everyone is settled" | **NEW** → §9.8 (also `screens-record-lend-group.md` §1.2.8) |
| Row / Person (127:2252) Size=Compact, Trailing=Value / Muted | settings members, archived members | **NEW** → §9.9 (also `screens-record-lend-group.md` §1.2.10) |
| Toggle - Switch (Apple kit) | pool | kit → §6.4 |
| Overlay / Alert (102:1115) Action=Primary | close confirmation | **NEW here** → §6.6 (spec in `screens-record-lend-group.md` §1.2.12) |
| Grabber / glass xmark (Apple kit) | sheet | components-home §14/§15 |

### 9.1 Navigation / Push Header (97:1082). SwiftUI `PBPushHeader`
> "Pushed-screen header, 44 tall: glass back button, centred Headline title (Show title), and a trailing action. Trailing=Text: glass capsule text action (Save). Trailing=Icon: exposed glass Button / Icon (default Settings; gear on 10-01). Trailing=Wide Text: for long text actions (“Mark all read”): the capsule has 12 side padding and a 122 max width, and the centred title box is 102 wide and truncates, so at least 8 pt always separates title and action. Trailing=None: back only. Tall scrolling frames set the instance to fix position and give it a color/bg/primary fill. Built with .navigationTitle and a toolbar. SwiftUI: PBPushHeader"
- 362 × 44 at (20, 62), H space-between, centred. Back: Button / Icon Glass 44 at the left. Title (Show title): **Headline** `text/primary`, centred, box 200 × 22 at (101, 73), truncates. Trailing=Icon: Button / Icon Glass 44 at (338, 62). Trailing=None: nothing on the right.
- Used: dashboard Trailing=Icon (gear), Show title=false; settings Trailing=None, title "Project settings"; closed/archived Trailing=None, Show title=false.

### 9.2 Header / Title Row (128:1857), Leading=Tile. SwiftUI `PBTitleHeader`
> "PBTitleHeader — the content header under a Push Header on group, friend and project detail (06-19 · 07-04/07/08/11 · 10-01 · 10-06), always 16 below the header. Left-aligned: a 56 leading, then Title (Title/2) with Show tag (exposed Badge / Pill Muted: “Guest”, “Archived”) and Subtitle (Subheadline, secondary; Show subtitle off on 07-11).
> Leading=Tile: exposed Avatar / Circle 56 Type=Icon (group type: Plane, Home, People, Tag; projects: Drone, Package). Leading=Avatar: exposed Avatar / Circle 56 (Art, or Initials “AR” for a guest).
> Show members: exposed Avatar / Stack (Count 2–4) 8 below the meta, so the row is 96 tall with members and 56 without; a title-only meta is centred on the 56 leading.
> SwiftUI: PBTitleHeader"
- 362 wide, H gap 16, top-aligned; 96 tall with members. Tile 56 `bg/card` with a 24 icon. Content V gap 8: meta (56 tall, V gap 2, centred): title line (H gap 8: Title/2 + optional Muted tag 24 tall) + Subheadline subtitle; members stack (Count up to 4).

### 9.3 Card / Budget (145:2106). SwiftUI `PBBudgetCard`
> "PBBudgetCard — budget vs spent on a project (10-01 · 10-03 · 10-05 · 10-06). #F5F5F5 r20, padding 16, gap 12; 362 wide, hugs (189 with the planned line, 146 without).
> “Spent” label, Spent (Title/1) + Budget (Headline, secondary, “of ₹60,000”) on one baseline, an exposed Control / Progress Bar Large (layer “bar”), then Percent label (“87% used”) and Left label (“₹8,000 left” / “₹8,000 under budget”), then Show planned line: a divider + Planned text (Footnote).
> State=On track: bar State=Projected (fill 87%, projected 97% = ₹58,000). State=Over budget: bar State=Over + mark at the budget (fill 97.6% = ₹60,000 of ₹61,500), and the stats row becomes Icon / Alert 16 + Warning (Subheadline, “₹1,500 over budget”) in text/destructive: the only red, with the over segment. State=Closed: bar State=Default, no projection, no planned line (planned components count toward nothing).
> Set percentages with the nested bar’s paddings (see Control / Progress Bar). Text props share one default (the 10-01 numbers): 10-03 sets Spent “₹61,500” and Planned text “All planned items are bought.” (see _Example · Over budget beside the set); 10-05/10-06 set Left label “… under budget”.
> SwiftUI: PBBudgetCard"
- Card-relative geometry: `head` (16, 16, 330, 58) V gap 2 ("Spent" Footnote secondary 18 tall; amount row H gap 8 baseline: Title/1 38 tall + Headline secondary); `bar` (16, 86, 330, 12); `stats`/`warning` (16, 110, 330, 20); `planned` (16, 142, 330, 31: divider + 12 + Footnote). Height 189 / 146.
- Stats: left "{pct}% used" Subheadline `text/secondary`; right Left label Subheadline **`text/primary`**. Warning (Over): `alert.svg` 16 `icon/destructive` + gap 6 + text **Manrope Bold 14/20** `text/destructive` (drawn style; see §4.2).
- Not interactive.

### 9.4 Control / Progress Bar (116:1099). SwiftUI `PBProgressBar`
> "PBProgressBar — Budget, loan and share bars. Size=Small (6 tall, inside Row / Group and Row / Bar) · Large (12 tall, Card / Budget and Card / Loan Progress). Full-radius color/chart/track track; use the instance at FILL width.
> State=Default (fill only) · Projected (fill + the projected segment in color/chart/bar) · Over (fill up to the budget, then the red color/chart/over segment to the end).
> Percentages are PADDING overrides (Figma ignores size and position overrides on instance sublayers). Inside “track”, the layers “fill”, “projected” and “over” are full-width auto-layout frames holding a “segment”: fill.paddingRight = W×(1−p) · projected.paddingRight = W×(1−p) · over.paddingLeft = W×p (the budget point). For 0% hide “fill”. Show mark shows “mark” (a full-width frame holding the 2pt “tick”, color/chart/fill with a 1pt white outline, overhanging by 3): mark.paddingLeft = W×p − 1. W = the instance width: set the paddings AFTER the bar has its final width (they are pixels, not percentages).
> “over” and “mark” are left-anchored (constraint Left, 362 wide; the track clips “over”), so their paddings always count from the bar’s left edge.
> Drawn defaults: Default fill 60% (mark 51%) · Projected fill 87%, projected 97% (mark 90%) · Over fill 97.6%, over from 97.6% (mark 97.6%).
> SwiftUI: PBProgressBar"
- Native implementation (one view, params `height` 12|6, `fill` p, `projected` p?, `overFrom` p?, `mark` p?):
  - Track: capsule, `chart/track` #EBEBEB, clips its segments.
  - Projected (optional): capsule segment `chart/bar` #D1D1D1 from 0 to W × pProj (under the fill).
  - Fill: capsule segment `chart/fill` #0A0A0A from 0 to W × p. p = 0 → no segment. In State=Over the fill's right end is **square** and the red `chart/over` #C93636 segment runs from W × p to W (the track's capsule rounds both outer ends).
  - Mark (optional): rectangle 2 × (h + 6), centred on x = W × pMark, top −3, `chart/fill` + 1 pt **outside** white (`bg/primary`) outline; **not** clipped by the track.
- Colours stay gray/black; red only for the over segment (foundations rule 5).

### 9.5 Row / Setting (97:996). SwiftUI `PBSettingRow`
> "Settings list row for #F5F5F5 card groups, 56 tall (grows with a subtitle). Trailing: Chevron (push), Toggle (exposed kit switch; On track tinted color/bg/inverse), Stepper (exposed kit stepper), Check (black tick, single select), Unchecked (empty 24 slot), None. Tone=Destructive: text/destructive + icon/destructive, never a chevron. Optional icon (Show icon off moves the title and the divider inset to 16), value, subtitle, and an exposed Badge / Pill (Inverse “Pro” / “Try free”). Turn Show value off on Toggle, Stepper, Check and Unchecked rows. SwiftUI: PBSettingRow"
- W × 56 (362 on screens, 346 in the sheet), no fill (the card behind is `bg/card` radius 20), H gap 12, padding 12/16. Icon 24 `icon/primary` at (16, 16); title **Headline** `text/primary` fills; value **Body** `text/secondary`; chevron 20 `icon/tertiary` at the right (16 inset); toggle 64 × 28 kit switch. Divider (Show divider): hairline `border/subtle` from row x 52 to the row's right edge at the bottom. Pressed **(proposal)**: `bg/card-pressed` row fill.

### 9.6 Row / Bar (143:2156), Leading=Avatar. SwiftUI `PBBarRow`
> "PBBarRow — a share bar row: Insights by category and by group (11-01 · 11-02 · 11-03) and “Paid vs fair share” on the project dashboard (10-01). 362×56, no side padding (use FILL inside a card).
> Line 1: exposed leading Avatar / Circle 40, Title (Headline), Caption (Footnote, secondary: “51%” / “Paid ₹25,500”; Show caption), Amount on the right. Line 2: exposed Control / Progress Bar Small (layer “bar”).
> Leading=Icon: Type=Icon on color/bg/card for white surfaces (set its Icon; category map Food · Car · Bed · Ticket · Home · Bolt · Shopping Bag · Tag; groups Home · Plane · People). Leading=Avatar: Type=Art in a white circle for #F5F5F5 cards (set Art).
> Value=Neutral: Amount (Headline, text/primary: “₹12,000”, “Settled”) · Owed: “+” + Amount (Amount/Medium, text/primary) · Owe: “−” + Amount (Amount/Medium, text/secondary). Amount excludes the sign.
> Bar: set the nested bar’s paddings after sizing (see Control / Progress Bar): bar › track › fill.paddingRight = W×(1−p); Avatar variants show the fair-share mark (mark.paddingLeft = W×0.51 − 1). Defaults: Icon 51% (Rent); Avatar Neutral 51% · Owed 100% · Owe 35%.
> SwiftUI: PBBarRow"
- Here: 330 × 56 (FILL in the card), H gap 12, padding 8 top/bottom, 0 sides, centred. Avatar 40 Art, white circle, at (0, 8). Content (fills, V gap 8): line 1 (22 tall, H gap 8, centred): label group (H gap 8: title **Headline** `text/primary` + caption **Footnote** `text/secondary`, both hug) fills; value (H gap 0: sign + amount, right-aligned). Line 2: Progress Bar **Small** 6 tall, content width (278), Show mark = true.

### 9.7 Row / Transfer (128:1604) and Avatar / Pair (116:1005) Size=32
> "PBTransferRow — one payment in a settle-up plan (10-01 “Who owes whom”, 10-05 final plan): exposed Avatar / Pair 32 (from → to, white circles for a #F5F5F5 card), Title (Headline, “Rohan owes Dev” / “Rohan pays Dev”), Amount (Amount/Medium), Show divider (inset to the title; off on the last row). 64 tall.
> SwiftUI: PBTransferRow"
> "PBAvatarPair — “From → To” pair for transfers and payments: two nested Avatar / Circle (exposed as “from” and “to”: set Art / Type / Initials on each) with Icon / Arrow Right in icon/tertiary between them.
> Size=32 (16 arrow, gap 4) for Row / Transfer · Size=56 (20 arrow, gap 8) for Header / Amount Hero Leading=Pair (06-12, 08-06). Default: Arjun → Kabir.
> SwiftUI: PBAvatarPair"
- 362 × 64, H gap 12, padding 8 / 16, centred. Pair 88 × 32: from 32 (white circle) · gap 4 · `arrow-right.svg` 16 `icon/tertiary` · gap 4 · to 32. Title **Headline** `text/primary` fills, 1 line, truncates. Amount **Amount/Medium** `text/primary` (always black here, even for debts between others). Divider: absolute at the bottom from the title's x (row x 116) to the row's right edge (246 wide), `border/subtle`.

### 9.8 Card / Notice (129:1976), Layout=Leading, Actions=None. SwiftUI `PBNoticeCard`
> "PBNoticeCard — an in-flow notice on #F5F5F5 r20 (06-12 · 07-11 · 08-03 · 08-06 · 09-06 · 10-05 · 10-06 · 11-03): pending confirmation, invite a guest, simplified debts, a dispute, a read-only project, a Pro lock.
> Layout=Leading: exposed icon circle Avatar / Circle 40 Type=Icon On Card (set its Icon: Activity, Shuffle, Mail, Flag, Lock, Check Circle), Title (Headline) with Show badge (exposed Badge / Pill, default Inverse “Pro”) on the title line, Body (Subheadline, secondary, wraps). Layout=Centered: 56 icon circle, badge, Title (Title/3), Body, all centred (11-03).
> Actions=None · One = exposed Button / Primary Large “primary”, full width · Two = exposed Button / Primary Small “primary” + Button / On Card Small “secondary”, both FILL, gap 8. Set the button labels on the nested buttons (Label#9:0 / Label#9:42).
> Show title off hides the whole title line (and the badge with it in Layout=Leading), so the body sits level with the icon.
> All gray and black: never red.
> SwiftUI: PBNoticeCard"
- 362 wide, `bg/card` radius 20, padding 16, V gap 16; content H gap 12: icon circle 40 (white) with a 20 icon `icon/primary`; text column (fills, V gap 2): title line (Headline `text/primary`) + body (Subheadline `text/secondary`, wraps). Height 76 (1-line body) / 96 (2 lines). Not tappable.

### 9.9 Row / Person (127:2252), Size=Compact, Trailing=Value / Muted. SwiftUI `PBPersonRow`
> "PBPersonRow — the one people row (split pickers, balances, breakdowns, contacts, expense splits, chat answers). Exposed Avatar / Circle (40 Regular / 32 Compact), Name (Headline) + Show tag (exposed Badge / Pill Muted “Guest”, always next to the name), Subtitle (Subheadline), trailing, Show divider (inset to the name).
> Size=Regular: 64, avatar on color/bg/card for white surfaces (inside a #F5F5F5 card, override the avatar fill to color/bg/primary). Size=Compact: 56, avatar 32 in a white circle and an On Card tag, for use inside a card.
> Trailing: Owed = “+” + Amount (Amount/Medium, text/primary) · Owe = “−” + Amount (Amount/Medium, text/secondary) · Value = Amount (Headline, e.g. ₹700 or 25%) — these three carry Amount label (Footnote, tertiary) and Show status badge (exposed Badge / Pill Overdue “Overdue 3 days”, always under the amount) · Muted = Status (Subheadline, secondary: Settled / Added / No balance) · Check = black tick (single select, e.g. Paid by) · Select On / Select Off = 24 black circle with a white tick / empty border/strong ring (multi-select) · Remove = Icon / Close 20 (icon/secondary) · Button = exposed Button / Secondary Small “Invite” (Regular) or Button / On Card Small “Remind” (Compact) · None.
> Amount excludes the sign: the variant adds “+” or “−”. Guests: Avatar Type=Initials “AR” + Show tag.
> SwiftUI: PBPersonRow"
- 362 × 56, H gap 12, padding 6 / 16, centred. Avatar 32 Art (white circle) at (16, 12). Name **Headline** `text/primary` 1 line. Trailing (right-aligned): Value = **Headline** `text/primary` ("25%"); Muted = **Subheadline** `text/secondary` ("Settled"). Divider (Show divider): hairline from row x 60 to the right edge.

---

## 10. State differences at a glance (one project detail screen)

| | Active, on track (10-01) | Active, over budget (10-03) | Closed (10-05) | Archived (10-06) |
|---|---|---|---|---|
| Header | back + gear (fixed) | back + gear | back only | back only |
| Subtitle | "Project · 4 members · Active since 10 Aug" | same | "Project · 4 members" | "Project · 4 members · Closed 30 Aug" + tag "Archived" |
| Notice | — | — | Card / Notice lock "Closed · Read-only" / "Components are locked. Payments can still be recorded." | Card / Notice lock "Read-only" / "Nothing here can be edited." |
| Budget card | On track: bar Projected, "87% used" · "₹8,000 left", planned line "Planned items bring it to ₹58,000" (189 tall) | Over budget: bar Over + mark, red alert + "₹1,500 over budget", "All planned items are bought." | Closed: bar Default, "₹8,000 under budget", no planned line (146 tall) | Closed: "₹18,400" of "₹20,000", "92% used" · "₹1,600 under budget" |
| Components | editable list + Add component | same (GPS bought by Dev ₹9,500) | locked list (**proposal**; not drawn) | locked list (**proposal**; not drawn) |
| Paid vs fair share | yes | yes (not drawn) | — | — |
| History | yes | yes (not drawn) | proposal | proposal |
| Who owes whom / plan | "Who owes whom": "Rohan owes Dev ₹8,500", "Priya owes Dev ₹4,000" + "You’re settled in this project." | recomputed (not drawn) | "Final settle-up plan": "Rohan pays Dev", "Priya pays Dev" + "You’re settled. It becomes a permanent record once everyone has paid." | "Final settle-up plan": notice check-circle "Everyone is settled" / "No payments left in this project." |
| Members section | — | — | — | "Members": You / Esha / Dev / Kabir "Settled" |
| Pinned Add component + fade | yes | yes | no | no |

---

## 11. Debug hooks and tests

- `-startScreen` ids (seed the demo data first): `projectDrone`, `projectOverBudget` (applies §2.2), `projectAddComponent` (dashboard + sheet open), `projectSettings`, `projectClosed` (applies §2.3), `projectArchived` (opens Hackathon Kit). **(proposal)** `projectCloseAlert` = settings with the alert shown.
- Debug menu additions **(proposal)**: "Project: Dev buys GPS module (₹9,500)", "Project: close Build a Drone", "Project: Rohan pays Dev ₹8,500", "Project: Priya pays Dev ₹4,000", "Project: confirm pending payments" (to walk Closed → Archived without a second device), "Project: reset Build a Drone".
- UI tests (both platforms): open Groups › Build a Drone → assert "₹52,000", "87% used", "₹8,000 left", "Planned items bring it to ₹58,000", 8 component rows, "Rohan owes Dev" "₹8,500"; tap Add component → button disabled → type a name → enabled → add → new Planned row first, projection updates, spent unchanged; enter an actual cost on a new part → spent/shares update; gear → Close project → alert → Close project → Closed notice + "Rohan pays Dev"; debug-confirm both payments → Archived layout.

## 12. Test IDs

`screen.projectDrone` (the detail screen in any state; also `screen.projectOverBudget` / `screen.projectClosed` / `screen.projectArchived` when started there — **proposal**: one root id `screen.project` plus `project.state.<active|over|closed|archived>`), `project.back`, `project.settings`, `project.title`, `project.subtitle`, `project.notice`, `project.budget`, `project.budget.spent`, `project.budget.percent`, `project.budget.left`, `project.budget.warning`, `project.budget.planned`, `project.component.<id>` (e.g. `project.component.gps-module`), `project.share.<memberId>`, `project.shareRule`, `project.history`, `project.transfer.<index>`, `project.footnote`, `project.members.<memberId>`, `project.addComponent`; sheet `screen.projectAddComponent`, `addComponent.close`, `addComponent.name`, `addComponent.estimate`, `addComponent.actual`, `addComponent.status.<planned|bought|done>`, `addComponent.paidBy`, `addComponent.receipt`, `addComponent.add`; settings `screen.projectSettings`, `projectSettings.back`, `projectSettings.rule.<equal|percent|fixed>`, `projectSettings.member.<memberId>`, `projectSettings.addMember`, `projectSettings.budget`, `projectSettings.pool`, `projectSettings.close`, `projectSettings.closeAlert.cancel`, `projectSettings.closeAlert.confirm`.

---

## 13. Art and assets

- **No illustrations on this page**, so no Rive reuse and nothing to export. (Checked all six frames: the only art is avatar heads and icons.)
- Icons used (all already in `assets/icons/`, none missing): `chevron-left`, `settings`, `drone`, `package`, `tag`, `activity`, `chevron-right`, `arrow-right`, `plus`, `alert`, `wallet`, `camera`, `user-add`, `lock`, `check-circle`, `close` (Android sheet ✕). Tints: `icon/primary` everywhere except chevrons and the transfer arrow (`icon/tertiary`), the alert (`icon/destructive`) and the button plus (`icon/inverse`; `text/disabled` #A3A3A3 when disabled).
- Avatars used: `avatar-1` Arjun, `avatar-2` Priya, `avatar-3` Rohan, `avatar-4` Esha, `avatar-5` Dev, `avatar-6` Kabir.
- **New files written by this spec:** `ref/projectDrone.png` (804 × 3498), `ref/projectOverBudget.png`, `ref/projectAddComponent.png`, `ref/projectSettings.png`, `ref/projectClosed.png`, `ref/projectArchived.png` (804 × 1748, 2× REST renders), `ref/projectDrone_1x.png`, `ref/projectOverBudget_1x.png` (1× MCP screenshots). The node dumps weren't kept. No icons or images added.

## 14. Open questions / decisions for product

1. **Done vs Bought**: inferred as "fitted/finished" (money identical). Confirm.
2. **Planned rows show no payer** although new parts get "you as the payer": kept as drawn.
3. **Closed/Archived frames omit the components list and History**, but 10-05 says the GPS module "stays listed". Proposal: show them read-only below the plan/members.
4. **Fixed rule math** (what the fixed amounts add up to) and **pool** accounting are not designed; proposals in §1.3/§1.4.
5. **Over-budget warning style**: drawn Manrope Bold 14 vs the description's Subheadline (Medium 14). Spec follows the drawing.
6. Who records payments between two other members (Rohan → Dev)? Proposal: only the people involved; the demo simulates them from the debug menu.
7. Budget bar widths: Figma used rounded percents (287.1 / 320.1) on 10-01 but exact ratios elsewhere (286.0 on 10-05); spec uses exact ratios.
8. Project debts in Home balances/Settle Up: proposal says yes (they're real debts); Insights excludes project spending (designed).
