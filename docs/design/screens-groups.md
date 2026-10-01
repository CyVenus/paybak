# Paybak: Groups & Friends screens spec (Figma page "07 Groups & Friends", 77:101)

Sections covered: **Groups tab** (167:13142), **Group detail** (167:13145), **Friends & add friend** (177:25985). The fourth section, **Overlay helpers** (216:28950), holds prototype-only copies (§9).

| Screen id | Figma frame (node) | Designer no. | What it is | Container | Spec § | 2× reference |
|---|---|---|---|---|---|---|
| `groupsList` | Groups (167:14881) | 07-01 | Groups tab, segment **Groups**: groups + projects list, Archived | tab root | §3.2 | `ref/groupsList.png` |
| `friendsList` | Friends (167:15557) | 07-02 | Groups tab, segment **Friends**: one net per friend | tab root (same screen, other segment) | §3.3 | `ref/friendsList.png` |
| `groupsEmpty` | Groups — Empty (167:16388) | 07-03 | Groups tab, Groups segment, no groups yet | tab root (state) | §3.4 | `ref/groupsEmpty.png` |
| `groupGoaTrip` | Group — Goa Trip (167:18792) | 07-04 | Group detail, INR, open balance, simplify on | pushed screen | §4 | `ref/groupGoaTrip.png` |
| `groupSettings` | Group settings — Goa Trip (176:18633) | 07-05 | Group settings | pushed screen | §5 | `ref/groupSettings.png` |
| `groupLeaveBlocked` | Leave group — Blocked (176:18995) | 07-06 | Group settings (scrolled 26) + blocking alert | alert over Group settings | §5.6 | `ref/groupLeaveBlocked.png` |
| `groupDubaiWeekend` | Group — Dubai Weekend (176:20314) | 07-07 | Group detail, AED, settled | pushed screen (state of §4) | §4.8 | `ref/groupDubaiWeekend.png` |
| `friendRohan` | Friend — Rohan (177:25988) | 07-08 | Friend page, overdue friend | pushed screen | §6 | `ref/friendRohan.png` |
| `addFriend` | Add friend (177:26746) | 07-09 | Add friend: search, link, QR, contacts | pushed screen | §7 | `ref/addFriend.png` |
| `myQrCode` | My QR code (177:28130) | 07-10 | My QR code sheet over Add friend | sheet (Medium) over `addFriend` | §7.6 | `ref/myQrCode.png` (+ `ref/myQrCodeSheet.png`, the sheet body component 177:28072 alone, 708 × 1048) |
| `friendAnanyaGuest` | Friend — Ananya (Guest) (177:28684) | 07-11 | Friend page, guest with no balance | pushed screen (state of §6) | §6.7 | `ref/friendAnanyaGuest.png` |

Prototype-only frames (not screens, don't build): "↳ My QR code sheet (overlay)" (189:6345) → the `myQrCode` sheet; "↳ Add sheet (overlay)" (216:28953) → Home ＋ Add sheet (`screens-home.md` §5); "↳ Remind sheet (overlay)" (216:28961) → page 08 Remind sheet (`screens-settle.md`, 08-07s, `settleRemind`). See §9.

Related specs: `components-core.md` (buttons, badge, avatar, avatar stack, segmented control, input field, divider), `components-home.md` (nav header, icon/glass button, tab bar, balance card, section header, activity row, empty state, sheet container, materials), `screens-home.md` (Home scaffold, tab bar placement, Add sheet), `screens-settle.md` (page 08: Record payment — Kabir `settleRecordKabir`, Remind sheet, Settle up; its §0.4 also describes Push Header, Row / Person, Card / Notice, Row / Setting), `screens-record-lend-group.md` (page 06: Record payment, Lend money, **New group** `newGroup`, "Group created" = the empty state of the §4 template; its §1.2 describes the same new components), `screens-add-expense.md` (page 06 Add expense). Tokens: `tokens.md`. Rive: `rive.md`.

---

## 0. How this was read, conventions

### 0.1 Sources
- Read-only Plugin-API dump of all 12 frames (geometry, auto-layout, bound variables resolved to token names, text styles, component + variant + property values, reactions) plus every caption on the page. **That dump is the source for every number below** (it wasn't kept; the same node data is in `.figma-cache/nodes/77-101.json`, regenerate with `tools/fetch_figma.py`). Only 167:14881 (Groups) also had a `get_design_context` React reference. The other frames have no `get_design_context` output: the Figma account hit its MCP rate/day limit, so the node-tree dump replaced it (it's more exact anyway).
- 2× references: `download_assets` (PNG, scale 2) of each section, cropped to each frame (804 × 1748). They include the iOS status bar and home indicator (system UI: don't draw them).

### 0.2 Conventions
- Frame = 402 × 874 pt (= dp). All `x, y` are **frame coordinates** (frame top-left = 0,0); rows inside cards also give "row-relative" values where useful. Top safe area 62, bottom 34 (home indicator from y 840). Content column x 20 → 382 (362 wide).
- Colours = `tokens.md` names without `color/` (e.g. `text/secondary` = #6B6B6B). Text styles = `tokens.md` names (Manrope).
- Icons: `assets/icons/<name>.svg` (24 × 24 source; drawn at 24 / 20 / 16; the stroke scales with it). **Every icon used on this page already exists** in `assets/icons/`: plus, plane, home, drone, people, package, groups, activity, profile, user-add, money-in, money-out, car, food, ticket, bed, chevron-left, chevron-right, settings, calendar, exchange, shuffle, repeat, logout, check-circle, bell, search, link, scan, qr-code, copy, share, mail.
- Avatars: `assets/avatars/avatar-N.svg` (Arjun 1, Priya 2, Rohan 3, Esha 4, Dev 5, **Kabir 6**, **Meera 7**). Kabir and Meera are now used (they were "unused" in the Home phase). Circle fill rule: `bg/card` #F5F5F5 on white, `bg/primary` #FFFFFF inside #F5F5F5 cards.
- Text is verbatim. Characters that matter: minus in amounts **U+2212 "−"**; plus is ASCII "+"; rupee "₹" U+20B9; separator "·" U+00B7; en dash "–" U+2013 in date ranges ("21–25 Sep"); "≈" U+2248; curly apostrophe "’" U+2019 in **every** string on this page, **including "You’re owed" in the Friends summary and "You’re settled"** (unlike Home's straight "You're owed"); curly quotes in captions only.
- "AED 300" uses a plain ASCII space between code and number.
- Pressed states: same rules as README §3 rule 11 (pill buttons change fill, text buttons 50 % opacity, icon buttons `bg/selected`, rows `bg/card-pressed` suggested). Android: no ripple.
- Motion: only the prototype transitions exist: push 350 ms ease-in-out (PUSH from the right; back = PUSH/RIGHT), overlays move in from the bottom 300 ms ease-out, sheet close via scrim/✕ = dissolve 300 ms ease-out (in the prototype). Segment switches are instant NAVIGATE in the prototype (proposal: animate the black pill 250 ms ease-in-out).

### 0.3 Section-level notes (verbatim)
- **Groups tab** (167:13142): title "Groups tab"; subtitle "Groups tab: groups and projects list · friends list · empty state. Segmented control Groups | Friends; tab bar Active=Groups."
- **Group detail** (167:13145): title "Group detail"; subtitle "Group detail: Goa Trip (INR, open balance) · group settings · leave group blocked · Dubai Weekend (AED, settled)."
- **Friends & add friend** (177:25985): title "Friends & add friend"; subtitle "Friends & add friend: Rohan (overdue friend) · add friend · My QR code sheet · Ananya (guest friend)."
- **Overlay helpers** (216:28950): title "Overlay helpers"; subtitle "Prototype-only copies of the Home ＋ sheet and the page 08 Remind sheet. They aren’t screens."

---

## 1. Reuse map and NEW components

### 1.1 Reuse map (every component on this page)
| Figma component | Where on this page | Spec |
|---|---|---|
| Navigation / Nav Header, Type=Large Title (17:494) | Groups tab header "Groups" + glass action (Plus / User Add) | `components-home.md` §1 (Large Title); geometry §3.1 below |
| Navigation / Nav Header, Type=Inline | not drawn; proposed collapsed bar when the Groups tab scrolls | `components-home.md` §1 (Inline) |
| Button / Icon Style=Glass (10:79) | header actions, back, gear | `components-core.md` §2.3, `components-home.md` §2 |
| Button / Icon Style=Plain | Copy in the QR sheet link field | `components-core.md` §2.3 |
| Control / Segmented Options=2 (12:249) | Groups \| Friends, **stretched to 362** (segments 178 × 30) | `components-core.md` §4.3 (+ §3.1 below) |
| Navigation / Tab Bar Active=**Groups** (17:618) | 07-01/02/03 | `components-home.md` §5, placement `screens-home.md` §1.2 |
| Row / Group (139:2052) | Groups list, "Groups together" | **NEW §1.2** |
| Control / Progress Bar Size=Small (116:1099) | project budget bar in Row / Group | **NEW §1.3** |
| Row / Person (127:2252) Regular: Owed / Owe / Muted / Button; Compact: Owe / Owed / Muted / None | Friends list, Add friend lists, group Balances card, group Members card | **NEW §1.4** (also `screens-settle.md` §0.4-C) |
| Row / Section Header (13:223) | "Archived", "Balances", "Expenses", "History", "Groups together", "On Paybak", "Invite", "Members" (+ "Add" action) | `components-home.md` §8 |
| Card / Empty State Type=First day (13:541) | Groups — Empty | `components-home.md` §11, customised §3.4 |
| Header / Title Row (128:1857) Leading=Tile / Avatar | group + friend headers | **NEW §1.5** |
| Navigation / Push Header (97:1082) Trailing=Icon / None | group, settings, friend, add friend | **NEW §1.6** |
| Card / Balance (13:269) Type=Owe (+action), Owed (+badge), Settled | group + friend balance cards | `components-home.md` §6, extended §1.7 |
| Button / Primary / Secondary / On Card / Destructive, Large + Small | many | `components-core.md` §2.1 |
| Badge / Pill Overdue / Muted | "Overdue 3 days", "Guest" tag | `components-core.md` §3.1 |
| Avatar / Circle 32/40/56 Art / Icon / Initials / Icon On Card | everywhere | `components-core.md` §3.2 |
| Avatar / Stack Count=3/4 (11:417) | group members under the title | `components-core.md` §3.3 |
| Row / Activity Surface=Plain (13:477) | group expenses (no date, detail line on AED), friend history (with date) | `components-home.md` §10 |
| Row / Setting (97:996) Trailing=Chevron / Toggle | group settings, add options, automatic reminders | **NEW §1.8** (also `screens-record-lend-group.md` §1.2.6) |
| Toggle - Switch (kit, 78:1085) | Simplify debts, Automatic reminders | inside Row / Setting §1.8 |
| Divider / Line None / Leading (12:302) | row dividers | `components-core.md` §4.5 |
| Overlay / Alert Action=Primary (102:1115) | "You can’t leave yet" | **NEW §1.9** |
| Card / Notice Layout=Leading, Actions=One (129:1976) | "Invite Ananya to Paybak" | **NEW §1.10** |
| Control / Input Field (12:296) Default, leading search icon, no label/helper | Add friend search | `components-core.md` §4.4 |
| Sheet / Container Detent=Medium (118:1017) | My QR code sheet | `components-home.md` §15 (+ §1.11) |
| _Sheet / My QR code (177:28072, local) + Card / QR Code (130:1912) + Brand / App Mark 40 | My QR code sheet body | **NEW §1.11** |
| Illustration / Get Started — People (7:174) | Groups — Empty illustration | **Rive `paybak-getstarted.riv`** (§11) |

### 1.2 Row / Group (139:2052). SwiftUI `PBGroupRow` (NEW)
Designer description (verbatim):
> "PBGroupRow — a group or project in the Groups list (07-01) and in “Groups together” on a friend page (07-08). Row on white, no side padding, inset divider (Divider / Line Inset=Leading, 52).
> Exposed tile = Avatar / Circle 40 Type=Icon (set its Icon: Plane trip · Home · People friends · Tag other · Drone / Package projects). Name (Headline, truncates), Subtitle (Footnote, secondary), Show divider.
> Balance=Owe: “−” + Amount (Amount/Medium, text/secondary) over Amount label “You owe”. Balance=Owed: “+” + Amount (Amount/Medium, text/primary) over Owed label “You’re owed”. Balance=Settled: Status (Subheadline, secondary: “Settled”, or “You’re settled” for a project).
> Type=Group 72 · Type=Project 116: adds an exposed Control / Progress Bar Small (layer “bar”: set bar › track › fill.paddingRight = W×(1−p), default 87%) with Budget “₹52,000 of ₹60,000” and Left “₹8,000 left”, indented under the text · Type=Archived (Settled only) 72: tile icon, name and the fixed status “Read-only” in tertiary gray.
> Amount excludes the sign. Text props share one default across variants (Goa Trip): override Name/Subtitle per instance.
> SwiftUI: PBGroupRow"

Props: `Name`, `Subtitle`, `Amount`, `Amount label` ("You owe"), `Owed label` ("You’re owed"), `Status` ("Settled"), `Budget`, `Left`, `Show divider`; variants `Type` = Group | Project | Archived × `Balance` = Owe | Owed | Settled.

Geometry (row-relative; the row is 362 wide, full content width, no fill):
- Row: V stack, padding **16 top / 16 bottom / 0 sides**, gap **12**. Height **72** (Group, Archived) / **116** (Project).
- `main` (0,16) 362 × 40: H, gap 12, items centred.
  - `tile` = Avatar / Circle 40 Type=Icon: circle 40 × 40 fill `bg/card`, icon **20 × 20** centred (10,10), `icon/primary` (Archived: `icon/tertiary` #A3A3A3).
  - `text` (52, 0) fills: V gap 0: `name` **Headline** `text/primary` (Archived: `text/tertiary`), 22 tall, 1 line, truncate end; `subtitle` **Footnote** `text/secondary`, 18 tall, 1 line, truncate end.
  - `trailing` hug, right-aligned, V gap 0:
    - Owe: `amount` row (H, gap 0) = `sign` "−" + `value` "₹1,400", both **Amount/Medium** `text/secondary`, 22 tall; `amount label` "You owe" **Footnote** `text/secondary`, 18 tall, right-aligned. (In Row / Group the label is `text/secondary`; in Row / Person it's `text/tertiary`.)
    - Owed: sign "+" and value **Amount/Medium** `text/primary`; label "You’re owed" Footnote `text/secondary` (not drawn on this page).
    - Settled: `status` **Subheadline** `text/secondary` ("Settled" / "You’re settled"), 20 tall, vertically centred in the 40 row (y +10).
    - Archived: `status` "Read-only" **Subheadline** `text/tertiary`.
- Project only: `budget row` (0,68) 362 × 32: H gap 12: a 40-wide empty `inset` (so the budget aligns with the text at x 52) + `budget` column (52, 68) 310 wide, V gap **8**:
  - `bar` = Control / Progress Bar Size=Small, 310 × **6** (§1.3), fill = spent / budget.
  - `caption` row 310 × 18, H space-between: `budget` "₹52,000 of ₹60,000" **Footnote** `text/secondary` · `left` "₹8,000 left" **Footnote** `text/secondary` (right).
- `divider` = Divider / Line **Inset=Leading**, absolute at the row bottom (y = h − 1), 362 × 1: hairline `border/subtle` #EBEBEB from row x **52** to 362 (frame x 72 → 382). Hidden on the last row of a list (Show divider=false).
- Tap target: the whole row. No pressed state in Figma (suggest `bg/card-pressed` behind the full 362 row).

### 1.3 Control / Progress Bar (116:1099), Size=Small. SwiftUI `PBProgressBar` (NEW)
Designer description (verbatim):
> "PBProgressBar — Budget, loan and share bars. Size=Small (6 tall, inside Row / Group and Row / Bar) · Large (12 tall, Card / Budget and Card / Loan Progress). Full-radius color/chart/track track; use the instance at FILL width.
> State=Default (fill only) · Projected (fill + the projected segment in color/chart/bar) · Over (fill up to the budget, then the red color/chart/over segment to the end).
> Percentages are PADDING overrides (Figma ignores size and position overrides on instance sublayers). Inside “track”, the layers “fill”, “projected” and “over” are full-width auto-layout frames holding a “segment”: fill.paddingRight = W×(1−p) · projected.paddingRight = W×(1−p) · over.paddingLeft = W×p (the budget point). For 0% hide “fill”. Show mark shows “mark” (a full-width frame holding the 2pt “tick”, color/chart/fill with a 1pt white outline, overhanging by 3): mark.paddingLeft = W×p − 1. W = the instance width: set the paddings AFTER the bar has its final width (they are pixels, not percentages).
> “over” and “mark” are left-anchored (constraint Left, 362 wide; the track clips “over”), so their paddings always count from the bar’s left edge.
> Drawn defaults: Default fill 60% (mark 51%) · Projected fill 87%, projected 97% (mark 90%) · Over fill 97.6%, over from 97.6% (mark 97.6%).
> SwiftUI: PBProgressBar"

Used here: Size=Small, State=Default, Show mark=false. Track 310 × 6, fill `chart/track` #EBEBEB, capsule, clips. Fill segment: capsule, `chart/fill` #0A0A0A, width = W × p (Build a Drone: fill.paddingRight 40.3 → segment **269.7** = 310 × 0.87; p = 52,000 / 60,000 = 0.867, drawn as 87 %). p = 0 → no fill segment. Over-budget projects use State=Over (red `chart/over` from the budget point); that's page 10's concern, but the row must support it (see `screens-projects` when written).

### 1.4 Row / Person (127:2252). SwiftUI `PBPersonRow` (NEW; the same component is specced in `screens-settle.md` §0.4-C and `screens-record-lend-group.md` §1.2.10)
Designer description (verbatim):
> "PBPersonRow — the one people row (split pickers, balances, breakdowns, contacts, expense splits, chat answers). Exposed Avatar / Circle (40 Regular / 32 Compact), Name (Headline) + Show tag (exposed Badge / Pill Muted “Guest”, always next to the name), Subtitle (Subheadline), trailing, Show divider (inset to the name).
> Size=Regular: 64, avatar on color/bg/card for white surfaces (inside a #F5F5F5 card, override the avatar fill to color/bg/primary). Size=Compact: 56, avatar 32 in a white circle and an On Card tag, for use inside a card.
> Trailing: Owed = “+” + Amount (Amount/Medium, text/primary) · Owe = “−” + Amount (Amount/Medium, text/secondary) · Value = Amount (Headline, e.g. ₹700 or 25%) — these three carry Amount label (Footnote, tertiary) and Show status badge (exposed Badge / Pill Overdue “Overdue 3 days”, always under the amount) · Muted = Status (Subheadline, secondary: Settled / Added / No balance) · Check = black tick (single select, e.g. Paid by) · Select On / Select Off = 24 black circle with a white tick / empty border/strong ring (multi-select) · Remove = Icon / Close 20 (icon/secondary) · Button = exposed Button / Secondary Small “Invite” (Regular) or Button / On Card Small “Remind” (Compact) · None.
> Amount excludes the sign: the variant adds “+” or “−”. Guests: Avatar Type=Initials “AR” + Show tag.
> SwiftUI: PBPersonRow"

Props: `Name`, `Subtitle`, `Show subtitle`, `Show tag`, `Amount`, `Amount label`, `Show amount label`, `Show status badge`, `Status`, `Show divider`; variants `Size` = Regular | Compact × `Trailing` = Owed | Owe | Value | Muted | Check | Select On | Select Off | Remove | Button | None.

**Size=Regular** (on white; Friends list, Add friend lists): 64 tall, H, gap **12**, items centred, padding 8 top/bottom.
- Implement it as: avatar at the content left edge (frame x 20), trailing flush with the right edge (x 382). (In Figma the instance box is 378 wide at x 4 with 16 left / 0 right padding, so the avatar lands at x 20. The 07-10 backdrop copy uses 16/16 padding at x 20 instead, which shifts the avatar to 36: treat that as a designer inconsistency and follow 07-09 / 07-02.)
- `avatar` Avatar / Circle **40** at row y 12, fill `bg/card` #F5F5F5 (Art, or Initials in `Headline` for guests).
- `text` column (x 72, fills), V gap **2**: `name line` (H gap **8**, centred): `name` **Headline** `text/primary`, 1 line, hug (truncates when long) + `tag` (Show tag) = Badge / Pill Muted "Guest" (24 tall, `bg/card` fill, Caption/1 `text/secondary`, padding 0/10) · `subtitle` (Show subtitle) **Subheadline** `text/secondary`, 20 tall, 1 line, truncate. With no subtitle the name line is vertically centred (Ananya).
- `trailing` column hug, V gap **2**, right-aligned, vertically centred:
  - Owed: `amount` row = `sign` "+" + `value`, **Amount/Medium** `text/primary` (22 tall) · then either `amount label` **Footnote** `text/tertiary` #A3A3A3 (18 tall, e.g. "Owes you") **or** (Show status badge) Badge / Pill **Overdue** 24 tall ("Overdue 3 days", `bg/destructive` #C93636, Caption/1 `text/inverse`), right-aligned.
  - Owe: sign "−" + value **Amount/Medium** `text/secondary`; label Footnote `text/tertiary` ("You owe").
  - Muted: `status` **Subheadline** `text/secondary` ("No balance", "Added", "Settled").
  - Button: Button / Secondary **Small** "Invite": 72 × 36, capsule, fill `bg/card`, padding 0/16, label Button/Small `text/primary`.
- `divider` Divider / Line Inset=None, absolute at the row bottom: hairline `border/subtle` from frame x **72** to 382 (inset to the name). Hidden on the last row.

**Size=Compact** (inside `bg/card` r20 cards; group Balances and Members): 56 tall, H gap 12, padding **6 top/bottom, 16 left/right**, items centred.
- `avatar` Avatar / Circle **32**, fill **`bg/primary` white** (on the card), at row (16, 12).
- `text` (row x 60 → frame x 80), V gap 2: name **Headline** + subtitle **Subheadline** `text/secondary` (1 line each, truncate).
- Trailing as Regular (Owe / Owed amount + Footnote `text/tertiary` label; Muted status Subheadline `text/secondary`; None = nothing), right edge at row x 346 (frame x 366).
- `divider` from frame x **80** to 382 (302 wide), at the row bottom; hidden on the last row.

### 1.5 Header / Title Row (128:1857). SwiftUI `PBTitleHeader` (NEW; also `screens-record-lend-group.md` §1.2.11)
Designer description (verbatim):
> "PBTitleHeader — the content header under a Push Header on group, friend and project detail (06-19 · 07-04/07/08/11 · 10-01 · 10-06), always 16 below the header. Left-aligned: a 56 leading, then Title (Title/2) with Show tag (exposed Badge / Pill Muted: “Guest”, “Archived”) and Subtitle (Subheadline, secondary; Show subtitle off on 07-11).
> Leading=Tile: exposed Avatar / Circle 56 Type=Icon (group type: Plane, Home, People, Tag; projects: Drone, Package). Leading=Avatar: exposed Avatar / Circle 56 (Art, or Initials “AR” for a guest).
> Show members: exposed Avatar / Stack (Count 2–4) 8 below the meta, so the row is 96 tall with members and 56 without; a title-only meta is centred on the 56 leading.
> SwiftUI: PBTitleHeader"

Geometry (frame coords as placed at y 122): H, gap **16**, top-aligned, 362 wide.
- Leading (20, 122) **56 × 56** circle, fill `bg/card`: Tile = icon **24 × 24** `icon/primary` at (36, 138); Avatar = art scaled to 56; Initials = "AR" in **Title/3** `text/primary` centred.
- `content` (92, 122) 290 wide, V gap **8**:
  - `meta` 290 × 56, V gap **2**, vertically centred on the 56 leading: `title line` (H gap 8, centred): `title` **Title/2** `text/primary` (30 tall, 1 line, truncate) + optional `tag` Badge / Pill Muted "Guest" (24 tall) · `subtitle` **Subheadline** `text/secondary` (20 tall, 1 line, truncate). With both lines the title is at y 124 and the subtitle at y 156; title only → title at y 135 (centred).
  - `members` (Show members) = Avatar / Stack Count=n at (92, 186): 32-pt Art avatars, fill `bg/card`, 8-pt overlap (gap −8), each with a 2-pt white outside ring, later ones on top. Count is capped at **4** (Goa Trip has 5 members and shows 4: Arjun, Kabir, Priya, Esha). No "+N" indicator in Figma.
- Height: 96 with members, 56 without.

### 1.6 Navigation / Push Header (97:1082). SwiftUI `PBPushHeader` (NEW; also `screens-record-lend-group.md` §1.2.2)
Designer description (verbatim):
> "Pushed-screen header, 44 tall: glass back button, centred Headline title (Show title), and a trailing action. Trailing=Text: glass capsule text action (Save). Trailing=Icon: exposed glass Button / Icon (default Settings; gear on 10-01). Trailing=Wide Text: for long text actions (“Mark all read”): the capsule has 12 side padding and a 122 max width, and the centred title box is 102 wide and truncates, so at least 8 pt always separates title and action. Trailing=None: back only. Tall scrolling frames set the instance to fix position and give it a color/bg/primary fill. Built with .navigationTitle and a toolbar. SwiftUI: PBPushHeader"

- 362 × 44 at (20, 62), H, space-between, centred. `Back` = Button / Icon Glass 44 × 44 at (20, 62) with `chevron-left.svg` 24 (`icon/primary`). `title` **Headline** `text/primary`, centred, 200 × 22 box at (101, 73), 1 line, truncate (hidden when Show title=false). Trailing=Icon: Button / Icon Glass 44 × 44 at (338, 62) with `settings.svg` (gear).
- **Fixed header on scrolling pages** (Goa Trip, Dubai Weekend, Group settings, Friend — Rohan): the Push Header is pinned, and a fixed rectangle "Header background (fixed)" **0,0 → 402 × 106**, fill `bg/primary` #FFFFFF, sits behind it and in front of the content, so content scrolls under a solid white band (status bar + header). The first content element starts under a 44-pt "Header space" at y 62, so the content begins at y 122 (16 below the header).
- On Add friend and Ananya (no scroll in Figma) the header is in the flow; build every pushed page the same way (pinned header + white band) so long content still works.
- iOS: `NavigationStack` with a custom toolbar (glass buttons are system on iOS 26+), or a custom pinned header. Android: custom 44-dp row, glass fallback (fill #FFFFFF @72 %, 1 dp #FFFFFF @60 % inside border, shadow y4 blur16 #0A0A0A @8 %); system back = Back.

### 1.7 Card / Balance (13:269) on this page (extends `components-home.md` §6)
The component now also has a `Show caption` boolean (true everywhere here). Instances are FILL width (362).
| Use | Type | Icon (16, `icon/secondary`) | Label | Amount (Amount/Large) | Caption (Footnote `text/tertiary`) | Trailing |
|---|---|---|---|---|---|---|
| Goa Trip (07-04) | Owe | `money-out.svg` | "Your balance" | "−₹1,400" `text/secondary` | "You owe Kabir · Due Fri 2 Oct" | **Show action**: Button / Primary **Small** "Settle up" 97 × 36 at (269, 290) = card right 16 / bottom 24, absolute |
| Dubai Weekend (07-07) | Settled | `check-circle.svg` | "Your balance" | "**Settled**" `text/tertiary` (text override of the default "₹0") | "You paid Kabir AED 60 on 14 Mar" | none |
| Rohan (07-08) | Owed | `money-in.svg` | "Rohan owes you" | "+₹800" `text/primary` | "Movie tickets · Due Sun 27 Sep" | **Show badge**: Badge / Pill Overdue "Overdue 3 days" 110 × 24 at (256, 210) = top right; Show chevron=false |
- Card: fill `bg/card`, radius 20, padding 16, V gap 12; 116 tall (120 on Rohan because the top row is 24 tall to fit the badge: icon at y 214, label y 212, amount y 246, caption y 280).
- Label box fills the width (308 without badge; 192 with the badge). Chevron hidden on all three.

### 1.8 Row / Setting (97:996). SwiftUI `PBSettingRow` (NEW; full spec in `screens-record-lend-group.md` §1.2.6)
Designer description (verbatim):
> "Settings list row for #F5F5F5 card groups, 56 tall (grows with a subtitle). Trailing: Chevron (push), Toggle (exposed kit switch; On track tinted color/bg/inverse), Stepper (exposed kit stepper), Check (black tick, single select), Unchecked (empty 24 slot), None. Tone=Destructive: text/destructive + icon/destructive, never a chevron. Optional icon (Show icon off moves the title and the divider inset to 16), value, subtitle, and an exposed Badge / Pill (Inverse “Pro” / “Try free”). Turn Show value off on Toggle, Stepper, Check and Unchecked rows. SwiftUI: PBSettingRow"

Used here (rows stack with no gap inside a `bg/card` r20 card that clips): 362 × 56, H gap 12, padding 12/16/12/16, centred. `icon` **24 × 24** `icon/primary` at row (16, 16) · `title` **Headline** `text/primary` (fills, truncate) · `value` **Body** `text/secondary` (hug) · Chevron: `chevron-right.svg` **20 × 20** `icon/tertiary`, right edge at frame x 366 · Toggle: kit switch **64 × 28** at frame x 302, On = track `bg/inverse` #0A0A0A with a white 38 × 24 knob (radius 100) on the right (Off: iOS default track; Android off track #E0E0E0 proposal) · divider: hairline from frame x **72** → 382 (icon rows), hidden on the last row.

### 1.9 Overlay / Alert (102:1115), Action=Primary. SwiftUI `PBAlert` (NEW)
Designer description (verbatim):
> "iOS 27 alert card, 300 wide, radius 34: Headline title, Subheadline message, two 36pt capsules sharing the width (both exposed). Action=Destructive: Button / Secondary + Button / Destructive (Discard, Delete). Action=Primary: Button / Secondary + Button / Primary when nothing is destroyed (Settle up). Built with .alert: a .cancel button plus a .destructive or default button. SwiftUI: PBAlert"

- Card 300 × 162 at (51, 356) (centred on the screen), fill `bg/primary`, radius **34**, effect **Material/Glass** (shadow 0,8 blur 32 #0A0A0A @10 %), V, padding **20**, gap **20**, centred.
- `Text` 260 wide, V gap **4**: `title` **Headline** `text/primary`, centred, 22 tall · `message` **Subheadline** `text/secondary`, centred, wraps (40 tall = 2 lines).
- `Actions` 260 × 36, H gap **8**: `cancel` Button / Secondary Small (fill `bg/card`, label Button/Small `text/primary`) 126 × 36 · `action` Button / Primary Small (fill `bg/inverse`, label `text/inverse`) 126 × 36. Both FILL (equal widths).
- Behind it: "Scrim" rectangle 0,0 402 × 874, `bg/scrim` (#0A0A0A @40 %), covering everything including the header.
- iOS: native `.alert(title, isPresented:)` with `Button("Not now", role: .cancel)` + `Button("Settle up")` (system look is acceptable and is what the description asks for). Android: custom `Dialog` drawing this exact card over the 40 % scrim; not dismissed by tapping outside (proposal: tapping the scrim = "Not now" is also fine; iOS alerts don't dismiss on outside tap, so keep parity: don't).

### 1.10 Card / Notice (129:1976), Layout=Leading, Actions=One. SwiftUI `PBNoticeCard` (NEW)
Designer description (verbatim):
> "PBNoticeCard — an in-flow notice on #F5F5F5 r20 (06-12 · 07-11 · 08-03 · 08-06 · 09-06 · 10-05 · 10-06 · 11-03): pending confirmation, invite a guest, simplified debts, a dispute, a read-only project, a Pro lock.
> Layout=Leading: exposed icon circle Avatar / Circle 40 Type=Icon On Card (set its Icon: Activity, Shuffle, Mail, Flag, Lock, Check Circle), Title (Headline) with Show badge (exposed Badge / Pill, default Inverse “Pro”) on the title line, Body (Subheadline, secondary, wraps). Layout=Centered: 56 icon circle, badge, Title (Title/3), Body, all centred (11-03).
> Actions=None · One = exposed Button / Primary Large “primary”, full width · Two = exposed Button / Primary Small “primary” + Button / On Card Small “secondary”, both FILL, gap 8. Set the button labels on the nested buttons (Label#9:0 / Label#9:42).
> Show title off hides the whole title line (and the badge with it in Layout=Leading), so the body sits level with the icon.
> All gray and black: never red.
> SwiftUI: PBNoticeCard"

Geometry on 07-11: card 362 × 204 at (20, 202), fill `bg/card`, radius 20, padding **16**, V gap **16**.
- `content` (36, 218) 330 wide, H gap 12: `icon` Avatar / Circle 40 **Icon On Card** (white circle, `mail.svg` 20 `icon/primary`) · `text` (88, 218) 278 wide, V gap 2: `title` **Headline** `text/primary` (22) · `body` **Subheadline** `text/secondary`, wraps (80 tall = 4 lines here).
- `actions` (36, 338) 330 × 52: Button / Primary **Large**, full width, leading icon `share.svg` 20 (white), label "Send invite".

### 1.11 My QR code sheet: Sheet / Container (118:1017) + _Sheet / My QR code (177:28072) + Card / QR Code (130:1912) (NEW)
Designer descriptions (verbatim):
> Sheet / Container: "PBSheet (.presentationDetents) — Container for every picker and form sheet: kit grabber, a Title/3 title on the left and the kit glass xmark on the right, an optional search field (Show search; nested Control / Input Field exposed as “search”: set its Value#12:9 to “Search categories” etc.) and the native Content slot.
> Detent=Medium: 386 wide (inset 8, anchored 8 from the bottom), radius 40, hugs its content; the slot is 354 wide. Detent=Large: 402×804 from y 70, top radius 40, the slot fills the height and clips (lists scroll under the bottom edge); the slot is 370 wide.
> Content is a native SLOT: append the sheet body to the “Content” slot of the instance (or a local _Sheet / frame name instance). Never detach. Booleans: Show title, Show close, Show grabber, Show search, Show header. The title starts at 16, level with the slot content. For a sheet with no title and no close (08-10), turn Show header off (not just the two inner booleans), so the content starts 20 below the top.
> SwiftUI: PBSheet"

> _Sheet / My QR code: "Content of the My QR code sheet (07-10), placed in the Content slot of Sheet / Container (Detent=Medium): Arjun’s avatar, name and handle, Card / QR Code, helper, link field with Copy, and Share link. Local and unpublished. SwiftUI: PBMyQRCodeSheet"

> Card / QR Code: "PBQRCodeCard — “My QR code” (07-10): a real, scannable QR vector for https://paybak.app/i/arjun (version 4, 33×33 modules, error correction H, 6 pt modules = 198 pt) in color/text/primary on a white (color/bg/primary) r20 card, 240 square, quiet zone 21. Show mark: Brand / App Mark 40 on a 52 white rounded backing in the centre (≈7% of the modules; level H recovers 30%, so it still scans).
> The matrix was generated by api.qrserver.com (ecc=H) and verified to decode with CoreImage (CIDetector) before and after import. In the app it is rendered by CoreImage (CIQRCodeGenerator, correction level H) from the user’s invite link, never an image asset.
> SwiftUI: PBQRCodeCard"

Geometry is in §7.6.

### 1.12 Other known components, used with new settings
- **Control / Segmented Options=2** is stretched to the full 362 width here (FILL): container 362 × 36, padding 3, `bg/card`, capsule; segments **178 × 30** at x 23 and x 201. Selected: `bg/inverse` pill + label Button/Small `text/inverse`; unselected: no fill + `text/secondary`. Labels centred ("Groups" 52 wide at x 86; "Friends" 53 wide at x 263.5).
- **Navigation / Tab Bar** Active=Groups: identical to Home (`screens-home.md` §1.2) except the Groups item is Active (`bg/selected` pill, `icon/primary`, `text/primary`) and Home is Inactive (`icon/secondary`, `text/secondary`).
- **Row / Activity**, Surface=Plain, used two ways: (a) group expenses: Show date=false, amount = the whole expense, always Direction=In styling (`text/primary`) even when someone else paid; with `Show detail line` on for foreign-currency expenses (80 tall: title / subtitle / detail Footnote `text/tertiary`); (b) friend history: Show date=true (Footnote `text/tertiary` under the amount), Direction=In for an open item (`text/primary`), **Direction=Out styling (`text/secondary`, no sign) for a settled item** ("Farewell dinner" ₹9,000).

---

## 2. Business rules and data (from the designer notes; they are requirements)

### 2.1 Group balances (07-04; must come out of the real data, never hard-coded)
- Every member's **Share** = the sum of their split shares across the group's expenses; **Paid** = the sum of what they paid on those expenses (settlement payments are not "Paid"). **Net = Paid − Share**, then adjusted by **confirmed** settlement payments (payer +x, receiver −x). A payment that is still "Pending confirmation" changes nothing (page 08 rule).
- Goa Trip check (the designer's note: "Every net traces back to the six expenses (₹39,500 ÷ 5 = ₹7,900 each)"):

  | Expense (date) | Paid by | Amount | Your share |
  |---|---|---|---|
  | Villa (3 nights) (Mon 21 Sep) | Kabir | ₹18,000 | ₹3,600 |
  | Scooter rentals (Mon 21 Sep) | Priya | ₹3,500 | ₹700 |
  | Seafood dinner at Britto’s (Tue 22 Sep) | You | ₹6,500 | ₹1,300 |
  | Parasailing (Wed 23 Sep) | Esha | ₹5,000 | ₹1,000 |
  | Beach shack lunch (Thu 24 Sep) | Dev | ₹4,000 | ₹800 |
  | Fuel (Fri 25 Sep) | Dev | ₹2,500 | ₹500 |
  | **Total** | | **₹39,500** | **₹7,900** |

  All split **equally between the 5 members** (share = amount ÷ 5). Nets: You 6,500 − 7,900 = **−₹1,400** · Kabir 18,000 − 7,900 = **+₹10,100** · Priya 3,500 − 7,900 = **−₹4,400** · Esha 5,000 − 7,900 = **−₹2,900** · Dev (2,500 + 4,000) 6,500 − 7,900 = **−₹1,400**. The nets sum to 0.
- Uneven divisions: "When an amount doesn’t divide evenly, the leftover paisa rotate fairly" (06-05 note, page 06). Keep money in integer paisa.
- Row labels: your own row uses "You owe" / "You’re owed" (proposal for the owed case); other members use **"Owes"** (negative) and **"Gets back"** (positive); zero = Muted "Settled".
- **Your balance card** (Card / Balance): net < 0 → Type=Owe, "−₹X", caption "You owe {payee} · Due {Settle by as EEE d MMM}", Settle up button. net > 0 → Type=Owed "+₹X" (not drawn; proposal caption "{n} people owe you · Due {date}", or "{Name} owes you · Due {date}" when one person). net = 0 → Type=Settled, amount text "Settled", caption = your most recent confirmed settlement in this group, "You paid {Name} {amount} on {d MMM}" / "{Name} paid you {amount} on {d MMM}" (proposal when there is none: "Nothing pending", the component default).

### 2.2 Simplify debts
- Group setting "Simplify debts" (on for Goa Trip). Helper (verbatim): "Fewer payments to settle. Totals stay the same."
- The nets never change; only the list of transfers does. **On**: minimum-transfer plan: repeatedly match the largest debtor with the largest creditor and transfer min(|debt|, credit) (greedy; at most n−1 transfers). Goa Trip: You → Kabir ₹1,400, Priya → Kabir ₹4,400, Esha → Kabir ₹2,900, Dev → Kabir ₹1,400. **Off**: pairwise debts per expense (each non-payer owes the payer their share; multiple payers pro rata), netted per pair.
- Footnote under the Balances card when on (verbatim for Goa Trip): "Simplify debts is on. You, Priya, Esha and Dev each pay Kabir directly." Template: "Simplify debts is on. {debtors, "You" first, joined "A, B and C"} each pay {creditor} directly." (single creditor). Several creditors (proposal): "Simplify debts is on. Everyone settles in {n} payments." When off: no footnote (proposal).
- Everything downstream uses the plan: "You owe Kabir" on the balance card, Kabir's −₹1,400 on the Friends list, Home "Due soon" Goa Trip row, page 08 Settle up ("2 payments to make").

### 2.3 Foreign-currency groups (07-07 Dubai Weekend)
Designer note (verbatim): "A foreign-currency group keeps every amount in AED and shows the ₹ value at the rate saved on the day of each expense, so balances never shift with the market. The total is the sum of the three saved conversions (21,936 + 12,312 + 6,870)."
- The group has a currency (Group settings › Currency). Every amount in the group is stored and shown in it: "AED 300", "Paid AED 540 · Share AED 600". Balances are computed in AED.
- Each expense saves the rate (user's currency per 1 unit of group currency) on its date. Its detail line (Row / Activity Show detail line, Footnote `text/tertiary`): "≈ ₹{amount × rate, rounded to whole rupees} · ₹{rate, 2 decimals} per AED":
  | Expense | Date | Paid by | Amount | Your share | Saved rate | ≈ ₹ |
  |---|---|---|---|---|---|---|
  | Hotel | Fri 6 Mar | Kabir | AED 960 | AED 320 | ₹22.85 | ₹21,936 |
  | Desert safari | Sat 7 Mar | You | AED 540 | AED 180 | ₹22.80 | ₹12,312 |
  | Dinner at the Marina | Sun 8 Mar | Meera | AED 300 | AED 100 | ₹22.90 | ₹6,870 |
- Balances footnote: "Total AED 1,800 · ≈ ₹41,118 at saved rates" = Σ amounts in AED · Σ of each expense's saved ₹ conversion (never total × today's rate).
- Nets (3 members, AED 600 share each): You 540 − 600 = −60, Kabir 960 − 600 = +360, Meera 300 − 600 = −300. All settled by payments: "You paid Kabir AED 60 on 14 Mar" (shown on the card); Meera must have paid Kabir AED 300 (not shown; seed it as confirmed, date proposal 14 Mar).
- Converting group balances into the user's currency (for Home totals and the Friends list): convert each share/payment at its own saved rate (proposal; Dubai is settled, so it doesn't affect the demo numbers).
- Amount formatting: "{CODE} {amount}" with an ASCII space, grouping by thousands (proposal: 2 decimals only when not whole). The group row subtitle adds " · AED" (see §2.6).

### 2.4 Per-friend net (07-02)
Designer note (verbatim): "Each friend shows one net across all groups and direct expenses, so this list adds up to the Home totals. Only the overdue badge is red, and guests show “No balance” until you share something with them."
- net(friend) = Σ direct (non-group) expenses between you + Σ open loans (page 06) + Σ over groups of the transfers between you and that friend **in that group's settlement plan** (simplified plan when Simplify is on; pairwise otherwise) − confirmed payments between you outside groups. Positive = they owe you (Trailing=Owed), negative = you owe them (Trailing=Owe), zero = "Settled" (Muted), a guest with nothing shared = "No balance" (Muted).
- Checks with the demo data: owed = Rohan 800 + Priya 700 + Esha 700 + Dev 700 = **+₹2,900** from 4 people; owe = Kabir 1,400 (Goa Trip, simplified) + Meera 450 (Flat 302) = **−₹1,850** across 2 groups. These are exactly Home's "+₹2,900 from 4 people" / "−₹1,850 across 2 groups" and the Friends summary line. In Goa Trip, Priya/Esha/Dev owe Kabir (not you) because of simplify, so Goa Trip adds nothing between you and them.
- Row subtitle / label rules (derived from Figma; proposal where marked):
  - Owed, not overdue: subtitle = "Due {EEE d MMM}" (earliest due item), label "Owes you". If the balance sits in a group: "{Group} · Due {date}" (proposal, by analogy with the Owe rows).
  - Owed and overdue: subtitle = the item's context (expense title "Movie tickets", or the group name), and the red Badge / Pill Overdue "Overdue {n} days" (n = whole days since the due date; "Overdue 1 day" singular, proposal) replaces the label.
  - Owe: subtitle "{Group} · Due {date}" ("Goa Trip · Due Fri 2 Oct"), label "You owe". Direct debt (proposal): "Due {date}".
  - No balance / Settled: Muted status, no subtitle for guests ("Ananya" + Guest tag).
- Sort (inferred from Figma; implement exactly this so the demo matches): 1) owed to you and overdue (most overdue first); 2) owed to you, by due date ascending, then amount descending; 3) you owe, by due date ascending, then amount descending; 4) settled / no balance. Ties keep the order the friend was added (demo seed order: Rohan, Priya, Esha, Dev, Kabir, Meera, Ananya), which gives Priya, Esha, Dev as drawn.
- Summary line above the list (07-02): "You’re owed" + "+₹{Σ positive}" and "You owe" + "−₹{Σ negative}" (§3.3). Show "+₹0" / "−₹0"? Proposal: when a side is zero show "₹0" in `text/tertiary`.

### 2.5 Groups list sorting and archived
Designer note (verbatim): "Groups and projects are in one list, sorted by open balance first, and a project shows its budget bar. “You’re settled” means your part is square even though others still owe inside the project, and archived projects open read-only."
- One list for groups and projects. Order (matches Figma): 1) items where **you** have an open balance, by due date ascending then |amount| descending (Goa Trip −₹1,400 due Fri 2 Oct, then Flat 302 −₹450 due Mon 5 Oct); 2) everything else alphabetically (Build a Drone, College Gang, Dubai Weekend). The second rule is inferred (alphabetical is the only order that fits); if product prefers "most recent activity", the demo order changes, so keep alphabetical.
- Project trailing: your net ≠ 0 → amount like a group; your net = 0 but others in the project still have open balances → Status **"You’re settled"**; everyone square → "Settled". Projects always show the budget bar (§1.2) when they have a budget.
- **Archived**: closed projects (Type=Archived, "Settled only") move to an "Archived" section (Row / Section Header, no action) under the list; row greyed (tile icon `icon/tertiary`, name `text/tertiary`), subtitle "Project · Closed {d MMM}", status "Read-only" `text/tertiary`; tap opens the project read-only (page 10). The section is hidden when empty.

### 2.6 Group row subtitles (Groups list, "Groups together")
- Open balance: "{n} members · Due {EEE d MMM}" (Settle by date). "5 members · Due Fri 2 Oct".
- Settled: "{n} members"; non-default group currency: "{n} members · {CODE}" ("3 members · AED"). Both (proposal): "{n} members · {CODE} · Due {date}".
- Project: "Project · {n} members". Archived project: "Project · Closed {d MMM}".
- Tile icon = group type: Trip → `plane`, Flat/Home → `home`, Friends → `people`, Other → `tag`; Project → `drone` (or `package`), chosen when the group is created (New group, page 06).

### 2.7 Due dates, overdue and reminders
- A group's due date is its **Settle by** setting (Goa Trip: Fri 2 Oct; Flat 302: Mon 5 Oct). A direct expense has its own due date (06-02: "This weekend" → Sun 4 Oct; Movie tickets: Sun 27 Sep).
- Overdue = today is after the due date and the item is still open. Red is only for overdue (the Overdue badge); everything else is gray/black.
- Automatic reminders (07-08 note, verbatim below): Rohan was reminded **Fri 25 Sep (2 days before), Sun 27 Sep (the due date) and Wed 30 Sep (today, 3 days after)**: that's the default schedule from Settings (page 12 owns the exact schedule). The friend-page toggle "Automatic reminders" mutes them **for that friend only**; muted friends are listed in Settings › Muted friends (12-07). Manual Remind still works when muted.
- "Last reminder sent today." = the latest reminder (automatic or manual) to this friend: "today" / "yesterday" / "on {EEE d MMM}" (proposal for the latter two). Hidden when none has been sent (proposal).

### 2.8 Guests (07-11, 07-09)
Designer notes (verbatim): "A guest is tracked like any friend but can’t see anything until they join. Their history links to their account automatically, so the invite is the only extra step. The tag is gray, not red, because nothing is wrong." and "Inviting someone who isn’t on Paybak adds them right away as a guest friend."
- Guest = a friend with no Paybak account (identified by phone/email). Everything works (split, balances, reminders) on your side; they see nothing until they join. When they join with the same phone or email, their history moves to their account (simulated: debug hook "guest joins" can flip `isGuest` off).
- UI: Avatar Type=Initials (first letters of first + last name, "AR"), Badge / Pill Muted "Guest" next to the name (never red), "No balance" when nothing is shared.
- Invite from Add friend (07-09 "Invite" button) adds the person immediately as a guest friend and opens their page (prototype: push to 07-11); "Send invite" on the guest page opens the system share sheet with the invite link.

### 2.9 Leave group (07-05 / 07-06)
Designer note (verbatim): "State: you tapped Leave group while you still owe money in it. You can only leave once your balance is zero, so the alert offers Settle up (Record payment to Kabir, page 08) instead of a destructive action."
- Leave is allowed only when **your net in the group is exactly 0** (pending payments don't count as paid). Otherwise show the blocking alert (§5.6) with "Settle up" → Record payment to the person you pay in the plan (Goa Trip: Kabir, `settleRecordKabir` prefilled ₹1,400).
- You're owed money in the group (proposal, not designed): same alert with title "You can’t leave yet" and message "{Name} owes you {amount} in {Group}. Settle up first, then you can leave." and "Settle up" → page 08 Settle up.
- Balance zero (proposal, not designed): Overlay / Alert **Action=Destructive**: title "Leave {Group}?", message "You’ll stop seeing this group. Its history stays with the other members.", buttons "Cancel" / "Leave" (Button / Destructive Small). Leave → remove you from the group, pop to the Groups list.

### 2.10 Recurring rules (07-05)
Designer note (verbatim, 07-05): "…Recurring shows in every group: Goa Trip reads “None”, and Flat 302 reads “3 rules” and opens 11-11 (reached in the prototype from the Cooking gas row on 09-01)… Shown as a Pro member."
- Row value = "None" or "{n} rules" ("1 rule" singular, proposal). Tap → the group's recurring rules list (page 11 screen 11-11, `recurringFlat302` in the page 11 spec).
- Recurring is a **Pro** feature (06-02 note). The frame is drawn "as a Pro member". For free users (the default) show the row's exposed Badge / Pill **Inverse "Pro"** and open the paywall on tap (proposal; the badge exists on Row / Setting for exactly this).

### 2.11 Usernames, invite links, QR
- Username = "@" + lowercase first name ("@arjun", "@kabir", "@meera") (07-09 note). Collisions (proposal): append a digit ("@arjun2").
- Invite link = `https://paybak.app/i/{username without @}`; shown without the scheme in the link field: "paybak.app/i/arjun". Copy puts the full `https://…` URL on the clipboard (proposal) with Overlay / Toast "Link copied" (proposal wording, Check Circle icon, 50 above the bottom, 2 s; the Toast component spec is in `screens-settle.md` §0.4-L).
- QR = the full invite URL, error correction **H**, drawn in `text/primary` on white, module area 198 × 198 pt inside a 240 white r20 card (quiet zone 21), with the app mark (40) on a 52 white r14 backing in the centre. iOS: `CIFilter.qrCodeGenerator()` (`correctionLevel = "H"`), scale with `.interpolation(.none)`. Android: ZXing core `QRCodeWriter` (`EncodeHintType.ERROR_CORRECTION = H`, `MARGIN = 0`) drawn module by module on a Canvas. "https://paybak.app/i/arjun" (26 bytes) encodes as version 4 (33 × 33) → 6 pt per module; other lengths may give another version, so always fit the matrix to 198 pt.

---

## 3. Groups tab: `groupsList` / `friendsList` / `groupsEmpty` (07-01, 07-02, 07-03)

ONE tab-root screen with a segmented control. Segment **Groups** shows the groups list (07-01) or its empty state (07-03); segment **Friends** shows the friends list (07-02). Tab bar Active=Groups. Remember the selected segment while the app runs (proposal: default Groups each launch).

### 3.1 Shared scaffold (all three)
Frame: V, padding 62 / 20 / 34 / 20, gap 24, fill `bg/primary`, clips; no prototype scrolling in Figma. Build it like Home (`screens-home.md` §1): one vertical scroll view holding header + segmented + content; the glass tab bar floats above (same placement, x 20, bottom 21 above the screen edge); content scrolls under it with a bottom inset of ~107. Proposal: when the large title scrolls away, show Navigation / Nav Header **Type=Inline** ("Groups", Headline, white @90 % + blur 24, full-bleed 402 × 44 at y 62) with a solid white strip behind the status bar; the segmented control scrolls with the content.
Top block (07-01/03: "Top" V gap 16; 07-02: "Content" V gap 16 continues with the summary and list):
1. **Header** = Navigation / Nav Header Type=Large Title, Show action=true: 362 × 44 at (20, 62). `title` "Groups" **Title/1** `text/primary` at (20, 65) 111 × 38 (same title on both segments). `action` Button / Icon Glass 44 × 44 at (338, 62): **Groups segment → `plus.svg`** (New group); **Friends segment → `user-add.svg`** (Add friend).
2. **Segmented** (20, 122) 362 × 36: "Groups" | "Friends" (see §1.12). Groups selected on 07-01/03, Friends on 07-02.
3. Content starts at y 182 (Groups, Empty: 24 below) or y 174 (Friends summary: 16 below).
4. **Tab bar** Active=Groups, (20, 791) 362 × 62.

Navigation (07-01/02/03):
| Element | Destination | Transition |
|---|---|---|
| Header ＋ (Groups segment) | **New group** full-screen modal (page 06, 190:8356, `newGroup` in `screens-record-lend-group.md` §6) | modal, move in from bottom |
| Header user-add (Friends segment) | **Add friend** (07-09) | push 350 ms ease-in-out |
| Segment "Friends" / "Groups" | switch segment in place | instant in Figma (animate pill, proposal) |
| Tab Home | Home (`homeActive`/current state) | tab switch |
| Tab ＋ | Add sheet overlay (Home ＋ sheet, `screens-home.md` §5) over this tab; rows: Add expense (176:17454), Record payment (177:29750), Lend money (185:25810), New group (190:8356) | overlay, move in from bottom 300 ms ease-out |
| Tab Activity | Activity tab (page 09, 167:14361) | tab switch |
| Tab Profile | Profile (page 05, 64:4316) | tab switch |

### 3.2 Groups segment: `groupsList` (07-01, 167:14881)
Designer notes (verbatim): "Groups and projects are in one list, sorted by open balance first, and a project shows its budget bar. “You’re settled” means your part is square even though others still owe inside the project, and archived projects open read-only."

Elements (top to bottom, after §3.1):
- **Groups list** (20, 182) 362 × 404: V gap 0, Row / Group instances (§1.2):
  | # | y | Row (variant) | Tile icon | Name | Subtitle | Trailing | Divider | Tap → |
  |---|---|---|---|---|---|---|---|---|
  | 1 | 182–254 | Group, Owe | `plane` | Goa Trip | 5 members · Due Fri 2 Oct | "−" "₹1,400" / "You owe" | yes | Group — Goa Trip (07-04), push |
  | 2 | 254–326 | Group, Owe | `home` | Flat 302 | 3 members · Due Mon 5 Oct | "−" "₹450" / "You owe" | yes | Flat 302 group detail (same template; not drawn; no prototype link) |
  | 3 | 326–442 | Project, Settled | `drone` | Build a Drone | Project · 4 members | "You’re settled"; bar 87 %; "₹52,000 of ₹60,000" · "₹8,000 left" | yes | Project — Build a Drone (page 10, 167:12476) |
  | 4 | 442–514 | Group, Settled | `people` | College Gang | 6 members | "Settled" | yes | College Gang detail (not drawn) |
  | 5 | 514–586 | Group, Settled | `plane` | Dubai Weekend | 3 members · AED | "Settled" | **no** | Group — Dubai Weekend (07-07), push |
  Exact trailing boxes: Goa amount 65 × 22 at (317, 198), label 50 × 18 at (332, 220); Flat 302 value at (340, 270); "You’re settled" 90 × 20 at (292, 352); "Settled" 48 × 20 at (334, 468 / 540). Budget row: bar (72, 394) 310 × 6 (fill 269.7); caption y 408.
- **Archived** section (20, 610) 362 × 108: V gap **4**:
  - Row / Section Header "Archived" (Show action=false), 32 tall; title Title/3 at (20, 613).
  - Row / Group Type=Archived (20, 646) 362 × 72: tile `package` (`icon/tertiary`), name "Hackathon Kit" (`text/tertiary`), subtitle "Project · Closed 30 Aug" (`text/secondary`), status "Read-only" (Subheadline `text/tertiary`, 68 × 20 at (314, 672)), no divider. Tap → archived project, read-only (page 10, 177:27619).
- Scroll: the list + archived section scroll as one; the last row must be able to scroll above the tab bar.
- Loading/empty: with no groups → §3.4. With groups but no archived items → hide the Archived section.

### 3.3 Friends segment: `friendsList` (07-02, 167:15557)
Designer notes (verbatim): "Each friend shows one net across all groups and direct expenses, so this list adds up to the Home totals. Only the overdue badge is red, and guests show “No balance” until you share something with them."

Elements (after §3.1; the "Content" stack continues with gap 16):
- **Summary** (20, 174) 362 × 20, H space-between, centred:
  - Left "Owed" (H gap 6): "You’re owed" **Subheadline** `text/secondary` (79 × 20 at x 20) + "+₹2,900" **Manrope Bold 14 / 20** (no text style; use Subheadline metrics with weight Bold) `text/primary` (57 × 20 at x 105).
  - Right "Owe" (H gap 6): "You owe" **Subheadline** `text/secondary` (x 271) + "−₹1,850" **Subheadline** `text/secondary` (x 331).
  - Values = §2.4 sums (same as Home).
- **Friends list** (20, 210) 362 × 448: V gap 0, seven Row / Person **Regular** rows (64 each):
  | # | y | Avatar (40, `bg/card`) | Name | Subtitle | Trailing | Divider | Tap → |
  |---|---|---|---|---|---|---|---|
  | 1 | 210 | Rohan art (avatar-3) | Rohan | Movie tickets | Owed: "+" "₹800" + Badge Overdue **"Overdue 3 days"** (110 × 24 at (272, 242)) | yes | Friend — Rohan (07-08), push |
  | 2 | 274 | Priya (avatar-2) | Priya | Due Sun 4 Oct | Owed: "+" "₹700" / "Owes you" | yes | Priya's friend page (not drawn) |
  | 3 | 338 | Esha (avatar-4) | Esha | Due Sun 4 Oct | Owed: "+" "₹700" / "Owes you" | yes | Esha's page |
  | 4 | 402 | Dev (avatar-5) | Dev | Due Sun 4 Oct | Owed: "+" "₹700" / "Owes you" | yes | Dev's page |
  | 5 | 466 | Kabir (avatar-6) | Kabir | Goa Trip · Due Fri 2 Oct | Owe: "−" "₹1,400" / "You owe" | yes | Kabir's page |
  | 6 | 530 | Meera (avatar-7) | Meera | Flat 302 · Due Mon 5 Oct | Owe: "−" "₹450" / "You owe" | yes | Meera's page |
  | 7 | 594 | Initials "AR" | Ananya + tag "Guest" (Badge Muted 56 × 24 at (137, 614)) | (none) | Muted: "No balance" (73 × 20 at (309, 616)) | **no** | Friend — Ananya (Guest) (07-11), push |
  Trailing geometry: amount rows right-aligned at x 382 (e.g. Priya "+" at x 330, "₹700" 42 wide at x 340, y +11); labels Footnote `text/tertiary` right-aligned (x 323, y +35). Names: Headline at row y +10; subtitles at y +34.
- Only the Overdue badge is red. Every row opens that friend's page (only Rohan and Ananya are drawn; the rest use the same §6 template).
- Empty Friends state (not designed; proposal): Card / Empty State First day with the same illustration as §3.4, title "No friends yet.", body "Add a friend to split with them.", primary "Add friend" (`user-add`), no secondary.

### 3.4 Groups segment, empty: `groupsEmpty` (07-03, 167:16388)
Designer notes (verbatim): "State: a new account with no groups yet. The same card pattern as Home “First day” points to creating a group or inviting friends."

Same as §3.1 (header with ＋, Groups selected, tab bar). Content:
- **Empty state** = Card / Empty State Type=First day (`components-home.md` §11), (20, 182) 362 × **446**, fill `bg/card`, radius 20, padding 24, V gap 20, centred:
  - `illustration` slot **240 × 180** at (81, 206) = Illustration / Get Started — People (the Get Started trio on its #EBEBEB rounded card) → **Rive `paybak-getstarted.riv`** (§11).
  - `title` "No groups yet." **Title/2** `text/primary`, centred, 314 wide at (44, 406).
  - `body` "Start one for a trip, your flat or a project." **Body** `text/secondary`, centred, at (44, 444).
  - Primary Large (44, 488) 314 × 52: `plus.svg` 20 white + "New group" → **New group** modal (page 06, 190:8356).
  - On Card Large (44, 552) 314 × 52: `user-add.svg` 20 + "Invite friends" → **Add friend** (07-09), push.
- No reactions in Figma on this frame (the destinations above are the obvious ones and match the ＋ header and Home "Invite friends").

Test IDs (§3): `screen.groupsList` / `screen.friendsList` / `screen.groupsEmpty` (root), `groups.title`, `groups.action` (＋ or user-add), `groups.segment.groups`, `groups.segment.friends`, `groups.row.<groupId>` (e.g. `groups.row.goa-trip`), `groups.archived`, `groups.empty`, `groups.empty.newGroup`, `groups.empty.inviteFriends`, `friends.summary.owed`, `friends.summary.owe`, `friends.row.<personId>` (e.g. `friends.row.rohan`), tab bar `home.tab.<home|groups|add|activity|profile>` (same ids as Home).

---

## 4. Group detail: `groupGoaTrip` (07-04, 167:18792) and `groupDubaiWeekend` (07-07, 176:20314)

ONE template (`PBGroupDetail`), pushed from the Groups list, a friend's "Groups together", Home "Due soon" (proposal) and New group → Create (page 06 `newGroupCreated` is its empty state).

### 4.1 Designer notes (verbatim)
- 07-04: "A group shows its total spend, your balance and its due date first, then each member’s paid vs share, then expenses by date. Every net traces back to the six expenses (₹39,500 ÷ 5 = ₹7,900 each), and with simplify debts on, everyone who owes pays Kabir directly."
- 07-07: "A foreign-currency group keeps every amount in AED and shows the ₹ value at the rate saved on the day of each expense, so balances never shift with the market. The total is the sum of the three saved conversions (21,936 + 12,312 + 6,870)."

### 4.2 Container, scroll, header
- Pushed screen. Frame V, padding 62 / 20 / 34 / 20, gap **24**, **overflow VERTICAL** (content 1320 tall on Goa Trip: it scrolls), 6 fixed children: status bar, home indicator, "Header background (fixed)" (0,0,402,106 `bg/primary`), Push Header, hotspots.
- **Push Header** Trailing=Icon, Show title=false: Back (20, 62) + gear `settings.svg` (338, 62), both glass 44. Pinned; content scrolls under the white 0–106 band (§1.6). No tab bar on pushed pages.
- Keyboard: none.

### 4.3 Top block (V gap 16): y 62–350
1. "Header space" 362 × 44 (under the fixed header).
2. **Title block** (20, 122) 362 × 96 → Header / Title Row Leading=Tile, Show members=true (§1.5):
   - Goa: tile `plane`; title "Goa Trip" (93 × 30); subtitle "21–25 Sep · 5 members · ₹39,500 spent"; members Avatar / Stack Count=4: Arjun (92,186), Kabir (116), Priya (140), Esha (164).
   - Dubai: tile `plane`; title "Dubai Weekend"; subtitle "6–8 Mar · 3 members · AED"; stack Count=3: Arjun, Kabir, Meera.
   - Subtitle rule: "{first–last expense date, en dash, month once when shared} · {n} members · {₹total} spent" for default-currency groups; foreign groups end with " · {CODE}" instead of the spend (the total is in the Balances footnote). Empty group (proposal): "{n} members".
3. **Balance card** (20, 234) 362 × 116 = Card / Balance (§1.7):
   - Goa: Owe, "Your balance", "−₹1,400", "You owe Kabir · Due Fri 2 Oct", **Settle up** (Primary Small) at (269, 290) 97 × 36.
   - Dubai: Settled, "Your balance", "Settled" (`text/tertiary`), "You paid Kabir AED 60 on 14 Mar", no button.

### 4.4 Balances section: (20, 374), V gap 4
- Row / Section Header "Balances" (no action), 32 tall.
- **Balances body** (20, 410), V gap **8**:
  - **Balances card** 362 wide, fill `bg/card`, radius 20, clips; Row / Person **Compact** rows (56 each), "You" first, then the other members in the group's member order:
    | Goa Trip row | Avatar (32, white) | Name | Subtitle | Trailing (label) |
    |---|---|---|---|---|
    | 410 | Arjun | You | Paid ₹6,500 · Share ₹7,900 | Owe "−" "₹1,400" ("You owe") |
    | 466 | Kabir | Kabir | Paid ₹18,000 · Share ₹7,900 | Owed "+" "₹10,100" ("Gets back") |
    | 522 | Priya | Priya | Paid ₹3,500 · Share ₹7,900 | Owe "−" "₹4,400" ("Owes") |
    | 578 | Esha | Esha | Paid ₹5,000 · Share ₹7,900 | Owe "−" "₹2,900" ("Owes") |
    | 634 | Dev | Dev | Paid ₹6,500 · Share ₹7,900 | Owe "−" "₹1,400" ("Owes"), no divider |
    | Dubai Weekend row | | | | |
    | 410 | Arjun | You | Paid AED 540 · Share AED 600 | Muted "Settled" |
    | 466 | Kabir | Kabir | Paid AED 960 · Share AED 600 | Muted "Settled" |
    | 522 | Meera | Meera | Paid AED 300 · Share AED 600 | Muted "Settled", no divider |
    Goa card height 280 (5 rows); Dubai 168 (3 rows).
  - **Footnote** Footnote `text/secondary`, fills 362, wraps:
    - Goa (20, 698, 2 lines, 36 tall): "Simplify debts is on. You, Priya, Esha and Dev each pay Kabir directly."
    - Dubai (20, 586, 1 line): "Total AED 1,800 · ≈ ₹41,118 at saved rates"
    - Rule: foreign-currency groups show the total line; simplified groups with open debts show the simplify line (both → show both lines, simplify first, proposal).
- Tap a member row (proposal): open that friend's page (not your own row).

### 4.5 Expenses section: V gap 4
- Row / Section Header "Expenses" (no action).
- **Expense list** V gap **8**: one block per date, newest date first; each block V gap 4: `Date label` **Footnote** `text/secondary` ("Fri 25 Sep", EEE d MMM, 18 tall) + rows (V gap 0), newest first within a day.
- Rows = Row / Activity Plain, Type=Expense, Show date=false, 64 tall (80 with the detail line), icon circle 40 `bg/card` + category icon 20; title Headline; subtitle Subheadline "{payer} paid · Your share {amount}" ("You paid · Your share ₹1,300"); amount Amount/Medium `text/primary` = the whole expense.
- Goa Trip (frame y): Fri 25 Sep (794): **Fuel** (`car`) "Dev paid · Your share ₹500" ₹2,500 · Thu 24 Sep (888): **Beach shack lunch** (`food`) "Dev paid · Your share ₹800" ₹4,000 · Wed 23 Sep (982): **Parasailing** (`ticket`) "Esha paid · Your share ₹1,000" ₹5,000 · Tue 22 Sep (1076): **Seafood dinner at Britto’s** (`food`) "You paid · Your share ₹1,300" ₹6,500 · Mon 21 Sep (1170): **Villa (3 nights)** (`bed`) "Kabir paid · Your share ₹3,600" ₹18,000; **Scooter rentals** (`car`) "Priya paid · Your share ₹700" ₹3,500. List ends at y 1320 (+34 bottom padding).
- Dubai Weekend (detail line on, Footnote `text/tertiary`): Sun 8 Mar (664): **Dinner at the Marina** (`food`) "Meera paid · Your share AED 100" / "≈ ₹6,870 · ₹22.90 per AED" / "AED 300" · Sat 7 Mar (774): **Desert safari** (`ticket`) "You paid · Your share AED 180" / "≈ ₹12,312 · ₹22.80 per AED" / "AED 540" · Fri 6 Mar (884): **Hotel** (`bed`) "Kabir paid · Your share AED 320" / "≈ ₹21,936 · ₹22.85 per AED" / "AED 960". Ends at y 986.
- Tap an expense → Expense detail (page 09 template; Villa links to 167:17847 = `expenseVilla` in the page 09 spec).
- A group with no expenses: the page 06 "Group created" empty state (`screens-record-lend-group.md` §7).

### 4.6 Navigation (07-04 / 07-07)
| Element | Destination | Transition / source |
|---|---|---|
| Back | pop to where it came from (Groups list in the prototype) | PUSH/RIGHT 350 ms ease-in-out |
| Gear | Group settings for this group (07-05) | push 350 ms (Goa); Dubai has no prototype link → same |
| Settle up (card) | **Record payment — Kabir** (`settleRecordKabir`, page 08, 167:13635) prefilled Kabir ₹1,400, full-screen modal | URL reaction; modal slides up (page 08 spec). If you pay several people in the plan → page 08 Settle up (`settleUp`) filtered to this group (proposal) |
| Villa (3 nights) row (and every expense) | Expense detail (page 09, 167:17847) | push |
| Member row (not "You") | friend page (proposal) | push |

### 4.7 Test IDs
`screen.groupGoaTrip` / `screen.groupDubaiWeekend` (debug ids; in the app `screen.group`), `group.back`, `group.settings`, `group.title`, `group.subtitle`, `group.members`, `group.balance`, `group.settleUp`, `group.balances.row.<personId>`, `group.simplifyNote`, `group.totalNote`, `group.expense.<expenseId>`, `group.empty`, `group.addExpense`, `group.toast` (the last three are the page 06 empty-state ids, `screens-record-lend-group.md` §7).

### 4.8 State differences: Dubai Weekend vs Goa Trip
Same template. Dubai: currency AED everywhere (§2.3); Balance card Settled (no Settle up); balance rows Muted "Settled"; footnote is the total line; expense rows 80 tall with the rate detail line; 3 members / stack of 3; subtitle ends with "AED"; content ends at y 986 (still scrolls a little).

---

## 5. Group settings: `groupSettings` (07-05, 176:18633) and `groupLeaveBlocked` (07-06, 176:18995)

### 5.1 Designer notes (verbatim)
- 07-05: "The settings cover members, the group currency, simplify debts (on) and recurring rules. Recurring shows in every group: Goa Trip reads “None”, and Flat 302 reads “3 rules” and opens 11-11 (reached in the prototype from the Cooking gas row on 09-01). Leave group is the only red element. Shown as a Pro member."
- 07-06: "State: you tapped Leave group while you still owe money in it. You can only leave once your balance is zero, so the alert offers Settle up (Record payment to Kabir, page 08) instead of a destructive action."

### 5.2 Container and header
Pushed from the group's gear. Frame V, padding 62/20/34/20, gap 24, **overflow VERTICAL** (content ends at y 868 + 34 → scrolls 28). Fixed white band 0–106 + **Push Header Trailing=None, title "Group settings"** (Headline, centred, 200 × 22 at (101, 73)). Back → Group — Goa Trip (PUSH/RIGHT).

### 5.3 Elements (07-05, top to bottom)
1. "Header space" 44 (y 62).
2. **Card A** (20, 122) 362 × 112, `bg/card` r20, clips; Row / Setting Chevron rows:
   - **Name** (y 122): icon = group type (`plane`), title "Name", value "Goa Trip" (Body `text/secondary`, at x 273), chevron, divider. Tap (proposal): edit the name and type (alert with a text field on iOS / dialog on Android, or a small form); renaming updates everywhere.
   - **Settle by** (y 178): `calendar`, "Settle by", value "Fri 2 Oct", chevron, no divider. Tap (proposal): the page 06 date sheet (06-09 calendar) titled "Settle by" with "Set date"; this date is the group's due date (§2.7). Empty value (proposal): "None".
3. **Members** section (20, 258), V gap 4:
   - Row / Section Header "Members" with Show action=true: Button / Text Secondary **"Add"** (29 × 44 at (353, 252); label Button/Small `text/secondary` at y 264). Tap (proposal): a people picker sheet (Sheet / Container Large with Row / Person Select On/Off, as in page 06 "Split with") listing friends and guests; add at the end of the member list. Adding a member doesn't change past splits.
   - **Members card** (20, 294) 362 × 280, `bg/card` r20: Row / Person **Compact, Trailing=None**, subtitle = the member's UPI ID:
     | y | Avatar | Name | Subtitle |
     |---|---|---|---|
     | 294 | Arjun | Arjun Mehta (you) | arjun@okaxis |
     | 350 | Kabir | Kabir Singh | kabir@okaxis |
     | 406 | Priya | Priya Sharma | priya@okhdfcbank |
     | 462 | Esha | Esha Kapoor | esha@okicici |
     | 518 | Dev | Dev Malhotra | dev@oksbi (no divider) |
     Member without a UPI ID (proposal): subtitle "@username", guests: "Guest" tag and phone/email.
4. **Card C group** (20, 598), V gap 8:
   - **Card C** 362 × 168, `bg/card` r20:
     - **Currency** (y 598): `exchange`, "Currency", value "INR ₹" ("{CODE} {symbol}", symbols from the Setup currency list), chevron, divider. Tap (proposal): the currency sheet (page 06 06-08, same rows as Setup 2). Existing expenses keep their own currency and saved rate; new expenses default to the new currency.
     - **Simplify debts** (y 654): `shuffle`, "Simplify debts", **Toggle on** (64 × 28 at (302, 668)), divider. Toggling recomputes the plan instantly (§2.2).
     - **Recurring expenses** (y 710): `repeat`, "Recurring expenses", value "None", chevron, no divider. Tap → recurring rules (page 11); free users → Pro badge + paywall (§2.10).
   - **Helper** (20, 774) Footnote `text/secondary`: "Fewer payments to settle. Totals stay the same."
5. **Leave group** (20, 816) 362 × 52: Button / **Destructive** Large, leading `logout.svg` 20 white (138, 832), label "Leave group" (Button/Large `text/inverse`), fill `bg/destructive` #C93636 (pressed #A92E2E). The only red element. Tap → §2.9 (blocked alert 07-06 when your balance isn't 0).

### 5.4 Scroll / keyboard
Content 868 + 34 → scrolls 28 pt; the Leave button sits under the home indicator until scrolled (as drawn). No keyboard.

### 5.5 Navigation
| Element | Destination |
|---|---|
| Back | Group — Goa Trip (pop) |
| Name / Settle by / Add / Currency / Recurring | see §5.3 (not linked in the prototype) |
| Simplify toggle | in place |
| Leave group | blocked alert (07-06) or leave confirmation (proposal §2.9) |

### 5.6 `groupLeaveBlocked` (07-06): the blocking alert
State of 07-05 after tapping Leave group while you owe money. The settings content is **scrolled up 26 pt** (Card A at y 96, Members at y 232, Card C at y 572, helper y 748, Leave button y 790) under the fixed header; then:
- **Scrim** 0,0 402 × 874 `bg/scrim` (#0A0A0A @40 %), over everything including the header.
- **Alert** = Overlay / Alert Action=Primary (§1.9), 300 × 162 at (51, 356):
  - title "You can’t leave yet" (Headline, centred, y 376)
  - message "You owe ₹1,400 in Goa Trip. Settle up with Kabir first, then you can leave." (Subheadline `text/secondary`, centred, 2 lines, y 402)
  - "Not now" (Button / Secondary Small, 126 × 36 at (71, 462)) → dismiss.
  - "Settle up" (Button / Primary Small, 126 × 36 at (205, 462)) → dismiss, then open **Record payment — Kabir** (`settleRecordKabir`, page 08) prefilled ₹1,400.
- Message template: "You owe {amount} in {Group}. Settle up with {payee} first, then you can leave." (payee = your transfer target in the plan; several payees (proposal): "Settle up first, then you can leave.").
- No reactions in Figma on this frame.
- Test IDs: `screen.groupSettings` / `screen.groupLeaveBlocked`, `groupSettings.back`, `groupSettings.name`, `groupSettings.settleBy`, `groupSettings.addMember`, `groupSettings.member.<personId>`, `groupSettings.currency`, `groupSettings.simplify`, `groupSettings.recurring`, `groupSettings.leave`, `groupSettings.leaveBlocked`, `groupSettings.leaveBlocked.notNow`, `groupSettings.leaveBlocked.settleUp`.

---

## 6. Friend page: `friendRohan` (07-08, 177:25988) and `friendAnanyaGuest` (07-11, 177:28684)

ONE template (`PBFriendDetail`). Opened from the Friends list, Home rows (proposal), page 08 breakdowns, Add friend (after Invite), group member rows (proposal).

### 6.1 Designer notes (verbatim)
- 07-08: "A friend’s page shows the one net between you, why it’s owed, and the actions that settle it. In the app, Record payment opens the page 06/08 form prefilled “Rohan → You · ₹800” and Movie tickets opens the 09-03 expense template; neither is drawn. Automatic reminders follow the default schedule, so Rohan was reminded Fri 25 Sep, on the due date (Sun 27 Sep) and today. The toggle mutes them for Rohan only, and Settings › Muted friends (12-07) lists anyone turned off here."
- 07-11: "A guest is tracked like any friend but can’t see anything until they join. Their history links to their account automatically, so the invite is the only extra step. The tag is gray, not red, because nothing is wrong."

### 6.2 Container and header (07-08)
Pushed. Frame V, padding 62/20/34/20, gap 24, **overflow VERTICAL** (content ends at y 902 + 34 → scrolls 62). Fixed white band 0–106 + **Push Header Trailing=None, Show title=false** (back only). Back → Friends list (PUSH/RIGHT).

### 6.3 Top block (V gap 16)
1. "Header space" 44.
2. **Title row** (20, 122) 362 × 56 = Header / Title Row **Leading=Avatar**, no members: avatar 56 Rohan art on `bg/card`; title "Rohan Verma" (Title/2, 150 × 30 at (92, 124)); subtitle "rohan@ybl" (Subheadline `text/secondary`, the friend's UPI ID; proposal: "@username" when there's no UPI ID).
3. **Balance block** (20, 194), V gap **8**:
   - "Balance and actions" V gap **12**:
     - **Balance card** 362 × 120 = Card / Balance Owed + Overdue badge (§1.7): "Rohan owes you" · badge "Overdue 3 days" · "+₹800" · "Movie tickets · Due Sun 27 Sep".
     - **Actions** (20, 326) 362 × 52, H gap 12, two FILL buttons (175 × 52 each):
       - **Remind**: Button / Primary Large, leading `bell.svg` 20 white (63, 342), label "Remind" → opens the **Remind sheet** (page 08, 08-07s, `settleRemind` sheet prefilled for Rohan) as an overlay over this page (move in from bottom 300 ms ease-out). Its ✕, "Send in Paybak" and the scrim close it.
       - **Record payment**: Button / Secondary Large (no icon), label "Record payment" → the Record payment form (page 06 `recordPayment` / page 08) prefilled **From Rohan → To You · ₹800** (full-screen modal).
   - **Footnote** (20, 386) Footnote **`text/tertiary`**: "Last reminder sent today." (§2.7).
   - Balance-dependent variants (proposal, from the rules): you owe the friend → Card / Balance Owe "You owe {Name}", "−₹X", caption "{context} · Due {date}", a single full-width Button / Primary Large "Settle up" (→ Record payment prefilled You → {Name} · amount), no Remind, no reminder footnote; settled → Card / Balance Settled "Settled" + the last payment as caption, no actions row, no footnote. The overdue badge shows only when the friend owes you and the item is past due.

### 6.4 History section (20, 428), V gap 4
- Row / Section Header "History".
- **History list** (20, 464), V gap 0, Row / Activity Plain **with date**, newest first:
  | y | Icon | Title | Subtitle | Amount | Date |
  |---|---|---|---|---|---|
  | 464 | `ticket` | Movie tickets | You paid · Rohan owes ₹800 | ₹1,600 (`text/primary`) | 20 Sep |
  | 528 | `food` | Farewell dinner | College Gang · Settled | ₹9,000 (**`text/secondary`**, no sign: settled) | 12 Mar |
- Content: every direct expense, group expense, payment and loan you share with this friend (proposal: cap at the 10 most recent + "See all" → Activity filtered by friend). Tap → the item's detail (Movie tickets → the 09-03 expense template; loans → page 06 loan detail, e.g. Kabir's "Bike service" loan).

### 6.5 Groups together (20, 616), V gap 4
- Row / Section Header "Groups together".
- **Groups list** (20, 652): Row / Group (§1.2), Type=Group (no budget bar here even for a project):
  - (652) `people` "College Gang" "6 members" · "Settled" · divider.
  - (724) `drone` "Build a Drone" "Project · 4 members" · "You’re settled" · no divider.
- Tap → the group / project page. Hidden when you share no groups (proposal).

### 6.6 Reminders (20, 820), V gap 8
- **Reminders card** 362 × 56 `bg/card` r20: Row / Setting Toggle: `bell` "Automatic reminders", toggle **on** (302, 834).
- **Footnote** (20, 884) Footnote `text/secondary`: "Turn off to stop Paybak nudging Rohan." (template "Turn off to stop Paybak nudging {first name}.").
- Toggle off → mute this friend (listed in Settings › Muted friends, 12-07). Shown only when the friend owes you (proposal: always shown; it only matters when they owe you).

### 6.7 `friendAnanyaGuest` (07-11): guest with no balance
Frame V, padding 62/20/34/20, gap 24, no scroll in Figma (build the same scrolling template).
1. **Top** (V gap 16): Push Header Trailing=None (back only; Back → Friends list), **Title row** Leading=Avatar **Initials "AR"** (56 circle `bg/card`, initials Title/3), Show subtitle=**false**, Show tag=**true**: title "Ananya Rao" (Title/2 at (92, 135), centred on the leading) + Badge / Pill **Muted "Guest"** 56 × 24 at (233, 138).
2. **Body** (20, 202), V gap **32**:
   - **Invite notice** = Card / Notice Leading, Actions=One (§1.10): icon `mail`; title "Invite Ananya to Paybak"; body "Ananya isn’t on Paybak yet. You can still split with her. When she joins with the same phone or email, her history moves to her account."; primary "Send invite" (`share` icon) → the system share sheet with the invite text + link (proposal text: "Join me on Paybak so we can split expenses: https://paybak.app/i/arjun"). Pronouns: the copy uses "her"; for other guests use the name without pronouns (proposal: "You can still split with them. When they join…").
   - **No balance** block (20, 438) 362 × 96, V gap 16, centred: text (V gap 4): "No balance yet" **Headline** `text/primary` centred; "Expenses you share with Ananya will show here." **Footnote** `text/secondary` centred · **Add expense** = Button / Secondary **Small** with leading `plus.svg` 16, 146 × 36 at (128, 498) → Add expense (page 06, 176:17454) full-screen modal, with Ananya preselected in "Split with" (proposal).
3. Once a guest has a balance: the §6.3 layout (balance card, actions), with the Guest tag kept and the invite notice above the balance (proposal).

### 6.8 Navigation (07-08 / 07-11)
| Element | Destination | Transition |
|---|---|---|
| Back | Friends list | PUSH/RIGHT 350 ms |
| Remind | Remind sheet overlay (page 08-07s) | overlay, move in from bottom 300 ms ease-out |
| Record payment | Record payment form, prefilled Rohan → You · ₹800 (page 06/08) | full-screen modal |
| History rows | item detail (09-03 expense template for Movie tickets) | push |
| Groups together rows | group / project page | push |
| Automatic reminders toggle | in place (mute) | — |
| Send invite (guest) | system share sheet | — |
| Add expense (guest) | Add expense modal (176:17454) | modal (URL in the prototype) |

Test IDs: `screen.friendRohan` / `screen.friendAnanyaGuest` (app: `screen.friend`), `friend.back`, `friend.title`, `friend.guestTag`, `friend.balance`, `friend.overdueBadge`, `friend.remind`, `friend.recordPayment`, `friend.lastReminder`, `friend.history.<itemId>`, `friend.group.<groupId>`, `friend.autoReminders`, `friend.invite` (notice), `friend.sendInvite`, `friend.noBalance`, `friend.addExpense`.

---

## 7. Add friend: `addFriend` (07-09, 177:26746) and the My QR code sheet `myQrCode` (07-10, 177:28130)

### 7.1 Designer notes (verbatim)
- 07-09: "One screen covers every way to add someone: search, an invite link, QR in both directions, and your contacts. Inviting someone who isn’t on Paybak adds them right away as a guest friend. Usernames follow the @arjun pattern (lowercase first name)."
- 07-10: "Your code and link add you on Paybak in one scan or tap. The QR is plain black on white so any camera reads it, with the Paybak mark in the middle."
- _Sheet / My QR code (177:28129): "_Sheet / My QR code · local component (unpublished) that fills the Content slot of Sheet / Container on 07-10."
- ↳ My QR code sheet (overlay) (189:6401): "Overlay target for 07-09 “My QR code”: a transparent copy of 07-10 without the Add friend screen, holding only the scrim and the sheet (8 from the bottom). 07-09 opens it with Open overlay, move in from bottom, 300 ms, so the page stays visible under it. ✕ or the scrim closes it. A prototype helper, not a screen."

### 7.2 Container
Pushed from the Friends header user-add, the Groups-empty "Invite friends" and Home "Invite friends". Frame V, padding 62/20/34/20, gap 24; no scroll in Figma (make it scroll; keyboard below).

### 7.3 Top block (V gap 16)
1. **Push Header** Trailing=None, title **"Add friend"** (in the flow; build pinned like the others). Back → pop (prototype BACK).
2. **Search** (20, 122) 362 × 52 = Control / Input Field State=Default, leading icon `search.svg` 20 `icon/secondary` (36, 138), no label, no helper; placeholder "Name, phone, email or @username" (Body `text/tertiary`, at (68, 136)).
3. **Add options card** (20, 190) 362 × 168, `bg/card` r20 — Row / Setting Chevron, no value:
   - (190) `link` "Invite with a link" → system share sheet with the invite link (§2.11) (proposal).
   - (246) `scan` "Scan QR code" → camera QR scanner (real: iOS `DataScannerViewController` / AVFoundation metadata output; Android CameraX + ML Kit barcode scanning or ZXing). A scanned `paybak.app/i/{username}` adds that person (simulated directory lookup) and opens their friend page; anything else → toast "That isn’t a Paybak code" (proposal). Not designed.
   - (302) `qr-code` "My QR code" → **My QR code sheet** (§7.6), overlay move in from bottom 300 ms ease-out.

### 7.4 Lists
- **On Paybak** (20, 382), V gap 4: Row / Section Header "On Paybak"; list (20, 418) of Row / Person **Regular Trailing=Muted**:
  - (418) Kabir art, "Kabir Singh", "@kabir", status "Added", divider.
  - (482) Meera art, "Meera Iyer", "@meera", status "Added", no divider.
  - = your contacts who are on Paybak. Already friends → "Added". Not yet friends (proposal): Trailing=Button with Button / Secondary Small **"Add"** → adds the friend, then the row shows "Added".
- **Invite block** (20, 570), V gap 16:
  - "Invite" section (V gap 4): Row / Section Header "Invite"; list (20, 606): Row / Person **Regular Trailing=Button**: Initials "AR", "Ananya Rao", "Not on Paybak yet", Button / Secondary Small **"Invite"** 72 × 36 at (310, 620). Tap Invite → add Ananya as a **guest friend** right away and push her page (07-11) (prototype: push).
  - **Footnote** (20, 686) Footnote `text/secondary`, 2 lines: "People who aren’t on Paybak join as guests. You can still split with them."
- Contacts source (proposal): device contacts (iOS `CNContactStore`, Android `ContactsContract`, asked on first open with a pre-prompt; if denied, only search/link/QR). "On Paybak" = contacts whose phone/email matches a (simulated, on-device) Paybak directory; "Invite" = the rest. The demo dataset supplies Kabir, Meera (on Paybak) and Ananya (not on Paybak) without touching real contacts.

### 7.5 Search and keyboard
- Typing filters both lists by name, phone, email or @username (case-insensitive, diacritics-insensitive). A full phone/email/@username that matches nobody shows one Row / Person with Trailing=Button "Invite" for that value (proposal). Empty results: Footnote `text/secondary` "No one matches “{query}”." (proposal).
- Keyboard: focus ring per Input Field (1.5 black inside overlay); the content scrolls, the keyboard covers the bottom; return key "Search" dismisses it.

### 7.6 `myQrCode` (07-10): My QR code sheet over Add friend
Container: sheet over Add friend: the 07-09 content stays underneath (the 07-10 backdrop copy has Row / Person insets of 16/16 — ignore, §1.4), covered by **Scrim** 0,0 402 × 874 `bg/scrim` 40 %. The sheet = Sheet / Container **Detent=Medium**, Title "My QR code", Show close/grabber/title = true, search off:
- Sheet box **(8, 236) 386 × 630** (8 from the bottom: bottom at y 866), fill `bg/primary`, radius **40** all corners, V, padding **8 top / 16 sides / 28 bottom**, gap **8**, centred. Height check: 8 + 4 + 8 + 50 + 8 + 524 + 28 = 630.
  - `grabber` 60 × 4 at (171, 244), capsule, kit colour #CCCCCC (nearest token `bg/indicator`).
  - `header` (24, 256) 354 × 50: `title` "My QR code" **Title/3** `text/primary` (24, 268), 1 line; `close` = kit Liquid Glass xmark **50 × 50** at (328, 256) (same recipe as the Home Add sheet ✕, `screens-home.md` §5.2).
  - `Content` slot (24, 314) 354 × 524 = **_Sheet / My QR code**: V gap **16**, centred, fill `bg/primary`:
    1. **Identity** (154.5, 314) 93 × 106, V gap 8, centred: Avatar / Circle **56** Art (the user's avatar; Arjun) at (173, 314), fill `bg/card`; name block (V gap 0, centred): "Arjun Mehta" **Headline** `text/primary` (y 378) · "@arjun" **Subheadline** `text/secondary` (y 400).
    2. **QR block** (81, 436) 240 × 270, V gap 12, centred:
       - **Card / QR Code** 240 × 240, fill `bg/primary` white, radius 20 (no stroke; it sits on the white sheet), content centred: `modules` vector **198 × 198** at (102, 457) (quiet zone 21), fill `text/primary` #0A0A0A; `mark` 52 × 52 at (175, 530), fill `bg/primary`, radius **14**, centred, holding **Brand / App Mark 40** (black squircle, radius 8.95, white P) at (181, 536). Content per §2.11.
       - **Helper** "Friends can scan this to add you." **Footnote** `text/secondary`, centred (102, 688).
    3. **Link field** (24, 722) 354 × 48, fill `bg/card`, radius **14**, H gap 8, padding 0 / 4 / 0 / 16, centred: `Link` "paybak.app/i/arjun" **Body** `text/primary` (fills, 1 line, truncate middle proposal) · **Copy** = Button / Icon **Plain** 44 × 44 at (330, 724) with `copy.svg` 24 `icon/primary` (pressed `bg/selected` circle) → copy the link + toast (§2.11).
    4. **Share link** (24, 786) 354 × 52 = Button / Primary Large, leading `share.svg` 20 white, label "Share link" → system share sheet with the full link (iOS `ShareLink` / `UIActivityViewController`; Android `Intent.ACTION_SEND` chooser).
- Open: move in from the bottom, 300 ms ease-out (like the Add sheet). Close: ✕, tapping the scrim, or swipe down (proposal) → back to Add friend (prototype: dissolve 300 ms ease-out). Brightness boost while the QR shows (proposal: no; keep calm).
- iOS: a custom overlay like the Home Add sheet (`screens-home.md` §5), or `.sheet` with a fixed detent of the sheet height if it reproduces the 8-pt inset/40 radius; Android: custom overlay (not `ModalBottomSheet`). Same choice as the other sheets in the app.
- Test IDs: `screen.addFriend` / `screen.myQrCode`, `addFriend.back`, `addFriend.search`, `addFriend.inviteLink`, `addFriend.scanQr`, `addFriend.myQr`, `addFriend.onPaybak.<personId>`, `addFriend.invite.<contactId>` (the Invite button), `myQr.sheet`, `myQr.close`, `myQr.code`, `myQr.handle`, `myQr.link`, `myQr.copy`, `myQr.share`, `myQr.toast`.

### 7.7 Navigation (07-09 / 07-10)
| Element | Destination | Transition |
|---|---|---|
| Back | pop (Friends list / wherever it came from) | BACK |
| Invite with a link | share sheet | — |
| Scan QR code | scanner (not designed) | full-screen modal (proposal) |
| My QR code | My QR code sheet | overlay, move in from bottom 300 ms ease-out |
| Kabir / Meera rows | their friend page (proposal) | push |
| Invite (Ananya) | Friend — Ananya (Guest) (07-11), after adding her as a guest | push 350 ms |
| Sheet ✕ / scrim | close the sheet | dissolve 300 ms (prototype) |
| Copy / Share link | clipboard + toast / share sheet | — |

---

## 8. Scroll summary
| Screen | Figma | Build |
|---|---|---|
| Groups tab (all segments) | no overflow | vertical scroll under the glass tab bar; bottom inset ≈ 107; Inline title bar on scroll (proposal) |
| Group detail (Goa / Dubai) | overflow VERTICAL, fixed header + white 0–106 band | scroll; header pinned |
| Group settings | overflow VERTICAL (28) | scroll; header pinned |
| Friend page (Rohan) | overflow VERTICAL (62) | scroll; header pinned |
| Friend page (guest), Add friend | no overflow | scroll anyway; header pinned |
| My QR code sheet | fixed | no scroll (content fits); on short screens let the sheet content scroll |

---

## 9. Overlay helpers (prototype-only; don't build as screens)
- **↳ My QR code sheet (overlay)** (189:6345): scrim + the 07-10 sheet at (8, 236); its scrim and ✕ CLOSE. → the real thing is the `myQrCode` sheet (§7.6).
- **↳ Add sheet (overlay)** (216:28953), note (verbatim): "Prototype helper, not a screen: a copy of Phase 1’s Overlay — Add sheet (page 04). The tab bar ＋ on 07-01 and 07-02 opens it with Open overlay, move in from bottom, 300 ms. Its rows open Add expense, Record payment, Lend money (IOU) and New group on page 06; the scrim or ✕ closes it." Sheet at (8, 472) 386 × 394; hotspots: Add expense (24, 550) → 176:17454; Record payment (24, 622) → 177:29750; Lend money (IOU) (24, 694) → 185:25810; New group (24, 766) → 190:8356; ✕ (328, 492) and scrim → CLOSE. → the Home ＋ Add sheet (`screens-home.md` §5).
- **↳ Remind sheet (overlay)** (216:28961), note (verbatim): "Prototype helper, not a screen: a copy of 08-07s Remind sheet (page 08) on a transparent 402×874 frame with its own scrim, the sheet 8 from the bottom. Remind on 07-08 opens it with Open overlay, move in from bottom, 300 ms, so Rohan’s page stays visible under it. ✕, Send in Paybak or the scrim closes it." Sheet at (8, 286) 386 × 580; ✕ (328, 306), "Send in Paybak" (24, 722, 354 × 52), scrim → CLOSE. → the page 08 Remind sheet (`screens-settle.md`, `settleRemind`).

---

## 10. Sample data (verbatim; the demo dataset must reproduce all of it from real calculations)

"Today" = **Wed 30 Sep 2026**. People (avatar file; UPI; username):
| Person | Full name | Avatar | UPI ID | Username | Notes |
|---|---|---|---|---|---|
| You | Arjun Mehta ("Arjun Mehta (you)" in members) | avatar-1 | arjun@okaxis | @arjun | invite link paybak.app/i/arjun |
| Rohan | Rohan Verma | avatar-3 | rohan@ybl | (@rohan) | friend page 07-08 |
| Priya | Priya Sharma | avatar-2 | priya@okhdfcbank | (@priya) | |
| Esha | Esha Kapoor | avatar-4 | esha@okicici | (@esha) | |
| Dev | Dev Malhotra | avatar-5 | dev@oksbi | (@dev) | |
| Kabir | Kabir Singh | avatar-6 | kabir@okaxis | @kabir | on Paybak, "Added" |
| Meera | Meera Iyer | avatar-7 | (not shown on page 07) | @meera | on Paybak, "Added" |
| Ananya | Ananya Rao | Initials "AR" | — | — | **guest**, "Not on Paybak yet" |

Groups and projects:
| Name | Type / icon | Currency | Members | Status for you | Other facts |
|---|---|---|---|---|---|
| Goa Trip | trip, `plane` | INR | 5: Arjun, Kabir, Priya, Esha, Dev | −₹1,400 (owe Kabir) | 21–25 Sep 2026; 6 expenses ₹39,500 (§2.1); Settle by Fri 2 Oct; Simplify on; Recurring "None" |
| Flat 302 | flat, `home` | INR | 3 (Arjun, Meera + one member not visible on this page) | −₹450 (owe Meera) | due Mon 5 Oct; "Electricity bill" 26 Sep (Home); Recurring "3 rules" (page 11) |
| Build a Drone | project, `drone` | INR | 4 (Arjun, Dev, Priya, Rohan per page 10) | You’re settled | budget ₹60,000, spent ₹52,000, "₹8,000 left", 87 % |
| College Gang | friends, `people` | INR | 6 (incl. Arjun and Rohan) | Settled | "Farewell dinner" ₹9,000 on 12 Mar 2026, settled |
| Dubai Weekend | trip, `plane` | AED | 3: Arjun, Kabir, Meera | Settled | 6–8 Mar 2026; 3 expenses (§2.3); "You paid Kabir AED 60 on 14 Mar" |
| Hackathon Kit | project, `package` | INR | — | archived, read-only | "Project · Closed 30 Aug" |

Direct items: **Movie tickets** ₹1,600, 20 Sep, you paid, split with Rohan (₹800 each), due Sun 27 Sep → "Overdue 3 days"; reminders sent Fri 25 Sep, Sun 27 Sep, Wed 30 Sep ("Last reminder sent today."). **Dinner at Olive Garden** ₹2,800, today, you paid, split equally with Priya, Esha, Dev (₹700 each), due Sun 4 Oct.

Every string on the page (verbatim, by screen):
- 07-01: "Groups", "Groups", "Friends"; "Goa Trip", "5 members · Due Fri 2 Oct", "−", "₹1,400", "You owe"; "Flat 302", "3 members · Due Mon 5 Oct", "−", "₹450", "You owe"; "Build a Drone", "Project · 4 members", "You’re settled", "₹52,000 of ₹60,000", "₹8,000 left"; "College Gang", "6 members", "Settled"; "Dubai Weekend", "3 members · AED", "Settled"; "Archived"; "Hackathon Kit", "Project · Closed 30 Aug", "Read-only"; tab labels "Home", "Groups", "Activity", "Profile".
- 07-02: "Groups", "Groups", "Friends", "You’re owed", "+₹2,900", "You owe", "−₹1,850"; "Rohan", "Movie tickets", "+", "₹800", "Overdue 3 days"; "Priya", "Due Sun 4 Oct", "+", "₹700", "Owes you"; "Esha", "Due Sun 4 Oct", "+", "₹700", "Owes you"; "Dev", "Due Sun 4 Oct", "+", "₹700", "Owes you"; "Kabir", "Goa Trip · Due Fri 2 Oct", "−", "₹1,400", "You owe"; "Meera", "Flat 302 · Due Mon 5 Oct", "−", "₹450", "You owe"; "AR", "Ananya", "Guest", "No balance".
- 07-03: "Groups", "Groups", "Friends", "No groups yet.", "Start one for a trip, your flat or a project.", "New group", "Invite friends".
- 07-04: "Goa Trip", "21–25 Sep · 5 members · ₹39,500 spent", "Your balance", "−₹1,400", "You owe Kabir · Due Fri 2 Oct", "Settle up"; "Balances"; "You", "Paid ₹6,500 · Share ₹7,900", "−", "₹1,400", "You owe"; "Kabir", "Paid ₹18,000 · Share ₹7,900", "+", "₹10,100", "Gets back"; "Priya", "Paid ₹3,500 · Share ₹7,900", "−", "₹4,400", "Owes"; "Esha", "Paid ₹5,000 · Share ₹7,900", "−", "₹2,900", "Owes"; "Dev", "Paid ₹6,500 · Share ₹7,900", "−", "₹1,400", "Owes"; "Simplify debts is on. You, Priya, Esha and Dev each pay Kabir directly."; "Expenses"; "Fri 25 Sep", "Fuel", "Dev paid · Your share ₹500", "₹2,500"; "Thu 24 Sep", "Beach shack lunch", "Dev paid · Your share ₹800", "₹4,000"; "Wed 23 Sep", "Parasailing", "Esha paid · Your share ₹1,000", "₹5,000"; "Tue 22 Sep", "Seafood dinner at Britto’s", "You paid · Your share ₹1,300", "₹6,500"; "Mon 21 Sep", "Villa (3 nights)", "Kabir paid · Your share ₹3,600", "₹18,000", "Scooter rentals", "Priya paid · Your share ₹700", "₹3,500".
- 07-05: "Group settings", "Name", "Goa Trip", "Settle by", "Fri 2 Oct", "Members", "Add", "Arjun Mehta (you)", "arjun@okaxis", "Kabir Singh", "kabir@okaxis", "Priya Sharma", "priya@okhdfcbank", "Esha Kapoor", "esha@okicici", "Dev Malhotra", "dev@oksbi", "Currency", "INR ₹", "Simplify debts", "Recurring expenses", "None", "Fewer payments to settle. Totals stay the same.", "Leave group".
- 07-06: as 07-05 + "You can’t leave yet", "You owe ₹1,400 in Goa Trip. Settle up with Kabir first, then you can leave.", "Not now", "Settle up".
- 07-07: "Dubai Weekend", "6–8 Mar · 3 members · AED", "Your balance", "Settled", "You paid Kabir AED 60 on 14 Mar", "Balances", "You", "Paid AED 540 · Share AED 600", "Settled", "Kabir", "Paid AED 960 · Share AED 600", "Settled", "Meera", "Paid AED 300 · Share AED 600", "Settled", "Total AED 1,800 · ≈ ₹41,118 at saved rates", "Expenses", "Sun 8 Mar", "Dinner at the Marina", "Meera paid · Your share AED 100", "≈ ₹6,870 · ₹22.90 per AED", "AED 300", "Sat 7 Mar", "Desert safari", "You paid · Your share AED 180", "≈ ₹12,312 · ₹22.80 per AED", "AED 540", "Fri 6 Mar", "Hotel", "Kabir paid · Your share AED 320", "≈ ₹21,936 · ₹22.85 per AED", "AED 960".
- 07-08: "Rohan Verma", "rohan@ybl", "Rohan owes you", "Overdue 3 days", "+₹800", "Movie tickets · Due Sun 27 Sep", "Remind", "Record payment", "Last reminder sent today.", "History", "Movie tickets", "You paid · Rohan owes ₹800", "₹1,600", "20 Sep", "Farewell dinner", "College Gang · Settled", "₹9,000", "12 Mar", "Groups together", "College Gang", "6 members", "Settled", "Build a Drone", "Project · 4 members", "You’re settled", "Automatic reminders", "Turn off to stop Paybak nudging Rohan."
- 07-09: "Add friend", "Name, phone, email or @username", "Invite with a link", "Scan QR code", "My QR code", "On Paybak", "Kabir Singh", "@kabir", "Added", "Meera Iyer", "@meera", "Added", "Invite", "AR", "Ananya Rao", "Not on Paybak yet", "Invite", "People who aren’t on Paybak join as guests. You can still split with them."
- 07-10 (sheet): "My QR code", "Arjun Mehta", "@arjun", "Friends can scan this to add you.", "paybak.app/i/arjun", "Share link".
- 07-11: "AR", "Ananya Rao", "Guest", "Invite Ananya to Paybak", "Ananya isn’t on Paybak yet. You can still split with her. When she joins with the same phone or email, her history moves to her account.", "Send invite", "No balance yet", "Expenses you share with Ananya will show here.", "Add expense".
- Hidden component defaults you'll see in the Figma props but that are NOT displayed (don't show them): Row / Group "₹52,000 of ₹60,000" / "₹8,000 left" / "Settled" on non-project rows; Row / Person "Due Sun 4 Oct" / "₹700" / "Settled" / "Dinner at Olive Garden" on rows where those parts are off; Push Header "Edit avatar" / "Save" when Show title is off; Input Field "Email" / "We’ll send a 6-digit code.".

Debug start screens (flow.md `-startScreen` / `--es startScreen`): `groupsList`, `friendsList`, `groupsEmpty` (loads an empty account on the Groups tab), `groupGoaTrip`, `groupSettings`, `groupLeaveBlocked` (opens settings and shows the alert), `groupDubaiWeekend`, `friendRohan`, `addFriend`, `myQrCode` (Add friend with the sheet open), `friendAnanyaGuest`. All but `groupsEmpty` load the demo dataset.

---

## 11. Art
| Figma art | Where | Same as an existing .riv? | Use |
|---|---|---|---|
| Illustration / Get Started — People (7:174, 362 × 260 component: the three people on an #EBEBEB rounded card), instance resized into the 240 × 180 empty-state slot | Groups — Empty (07-03), slot (81, 206, 240, 180) | **Yes: the Get Started trio** (compared with `ref/getStarted.png`: same three Open Peeps people, same card) | **`paybak-getstarted.riv`**, artboard `Get Started` (386 × 284 = the 362 × 260 card + 12 pt bleed), SM `Get Started`, VM `GetStarted`/`Default`. The Figma instance is squeezed non-uniformly (0.663 × 0.692); on device scale the artboard **uniformly by 240 / 362 = 0.663** (fit contain): Rive view **255.9 × 188.3**, centred on the slot → frame rect **(73.0, 201.9, 255.9, 188.3)** (card-relative (53.0, 19.9)); the grey card then draws 240 × 172.4, 3.8 pt shorter than Figma top and bottom. Don't draw a native grey card; don't clip. Taps on the people play the file's own jump animations (don't fire triggers yourself). Bind `reduceMotion`. The empty card is `bg/card` #F5F5F5; the Rive card is #EBEBEB, as in Figma. Static fallback: none exported (the Get Started art has no SVG in `assets/images/`; use the Rive `Still` pose). |
| Peep heads (Rohan, Priya, Esha, Dev, Kabir, Meera, Arjun) | avatars | not Rive | `assets/avatars/avatar-1…7` |
| QR code matrix (Card / QR Code "modules" vector) | My QR code | not art | generated at runtime (§2.11), never an asset |
| Brand / App Mark 40 | QR centre mark | — | `assets/brand/app-mark.svg` (or native squircle, `components-core.md` §1.1) |

No other illustrations on this page.

## 12. Assets
- **New icons: none.** Every icon on page 07 already exists in `assets/icons/` (checked against `assets/icons/INDEX.md`: plus, plane, home, drone, people, package, groups, activity, profile, user-add, money-in, money-out, car, food, ticket, bed, chevron-left, chevron-right, settings, calendar, exchange, shuffle, repeat, logout, check-circle, bell, search, link, scan, qr-code, copy, share, mail).
- **New images: none** (the only illustration reuses `paybak-getstarted.riv`; the QR is generated).
- Now used for the first time: `assets/avatars/avatar-6` (Kabir), `avatar-7` (Meera); icons `plane`, `drone`, `package`, `people`, `car`, `ticket`, `bed`, `settings`, `calendar`, `exchange`, `shuffle`, `repeat`, `logout`, `link`, `scan`, `qr-code`, `share`, `copy` (in these contexts).
- Files written by this spec: `screens-groups.md`; `ref/groupsList.png`, `ref/friendsList.png`, `ref/groupsEmpty.png`, `ref/groupGoaTrip.png`, `ref/groupSettings.png`, `ref/groupLeaveBlocked.png`, `ref/groupDubaiWeekend.png`, `ref/friendRohan.png`, `ref/addFriend.png`, `ref/myQrCode.png` (all 804 × 1748), `ref/myQrCodeSheet.png` (708 × 1048, the sheet body component 177:28072 alone). The node-tree dump and the `get_design_context` output of Groups weren't kept.

## 13. Open questions and proposals (not in Figma; implement as written unless product says otherwise)
1. Sort order of settled groups: alphabetical (only order matching Figma). Friends: §2.4 rules (inferred).
2. Destinations with no prototype link: Flat 302 / College Gang rows (same group template), Priya/Esha/Dev/Kabir/Meera rows (same friend template), Dubai gear, group settings rows (Name, Settle by, Add, Currency, Recurring), Scan QR code, Invite with a link, Record payment on the friend page, member rows. Proposals given inline.
3. Owed-state copy for group and friend balance cards, the "Leave?" confirmation when your balance is zero, and the "you're owed" leave block (§2.9).
4. Free users see a "Pro" badge on Recurring expenses (frame is drawn as a Pro member).
5. The Friends summary amount uses raw Manrope Bold 14/20 (no text style). Build it as Subheadline metrics at weight Bold.
6. Row / Person Regular in Figma is 378 wide at x 4 (16 left padding): build it at 362 with the avatar at the content edge (§1.4). The 07-10 backdrop's 16/16 padding is an inconsistency.
7. Empty Friends list, empty history, guest pronouns, "Overdue 1 day" singular, username collisions, QR-scan errors, contacts permission: proposals inline.
8. Flat 302's third member and Meera's UPI ID are not visible on page 07: take them from the other page specs / seed (keep Flat 302 "3 members").
9. The Get Started illustration is squeezed non-uniformly in Figma; the spec scales it uniformly (§11).
