# Paybak: Insights & AI screens (Figma page "11 Insights & AI", 77:105)

Sections covered: **Insights** (167:12997), **Ask Paybak** (167:13000), **Scan receipt** (177:25565), **Recurring** (177:29221). The page's fifth section, **Overlay helpers** (216:24639), holds prototype-only copies of other pages' sheets and is not specced (see §0.3).

| Screen id | Figma frame (node) | What it is | Spec § | 2× reference | Node JSON (`.figma-cache/`) |
|---|---|---|---|---|---|
| `insightsSeptember` | Insights — September (167:17585) | Activity tab, **Insights** segment, top of the September report (Pro) | §2 | `ref/insightsSeptember.png` | `nodes/77-105.json` |
| `insightsScrolled` | Insights — Scrolled (167:20007) | Same screen scrolled to the end; large title collapsed to the inline bar | §2.4 | `ref/insightsScrolled.png` | `nodes/77-105.json` |
| `insightsLocked` | Insights — Locked (167:21336) | Same screen for a **free** user: report blurred behind a Pro notice | §2.5 | `ref/insightsLocked.png` | `nodes/77-105.json` |
| `askStart` | Ask Paybak — Start (167:13148) | Full-screen chat modal from the Home sparkle, empty state with suggested prompts | §3.2 | `ref/askStart.png` | `nodes/77-105.json` |
| `askAnswer` | Ask Paybak — Answer (167:14107) | Same screen after "Who owes me money?" | §3.3 | `ref/askAnswer.png` | `nodes/77-105.json` |
| `askConfirm` | Ask Paybak — Confirm (167:15071) | Same screen after "Add ₹600 for a cab…": drafted expense card waiting for Save | §3.4 | `ref/askConfirm.png` | `nodes/77-105.json` |
| `scanCamera` | Scan receipt — Camera (177:25568) | Full-screen camera over Add expense | §4.2 | `ref/scanCamera.png` | `nodes/77-105.json` |
| `scanReview` | Scan receipt — Review (177:26256) | Pushed "Check receipt": what was read, editable (Pro) | §4.3 | `ref/scanReview.png` | `nodes/77-105.json` |
| `scanAssign` | Scan receipt — Assign items (177:26997) | Pushed "Assign items": who had each item, live per-person totals (Pro) | §4.4 | `ref/scanAssign.png` | `nodes/77-105.json` |
| `scanAddExpense` | Scan receipt — Add expense (177:28338) | The normal Add expense form, prefilled from the scan | §4.5 | `ref/scanAddExpense.png` | `nodes/77-105.json` |
| `recurringFlat302` | Recurring — Flat 302 (177:29224) | Pushed list of Flat 302's recurring rules + the pending draft (Pro) | §5.2 | `ref/recurringFlat302.png` | `nodes/77-105.json` |
| `recurringRepeat` | Recurring — Repeat (177:30291) | Add expense (Cooking gas) with the **Repeat** sheet open | §5.3 | `ref/recurringRepeat.png` | `nodes/77-105.json` |
| `recurringEnterAmount` | Recurring — Enter amount (177:30957) | Full-screen modal that turns the Cooking gas draft into an expense | §5.4 | `ref/recurringEnterAmount.png` | `nodes/77-105.json` |

Also dumped (fully expanded) while writing this spec: the Repeat sheet body 177:30605, the Cooking-gas draft row 177:29243, the Rent rule row 177:29280 and the **Saved** variant 147:2213 of Chat / Draft Expense.

Business rules are in §2.6 (Insights aggregation), §2.7 (chart), §3.6 (assistant), §4.6 (scan math), §5.5 (recurring rules). Sample data is in each screen's "Sample data" block and summarised in §6. Components: §1. Art/Rive: §7. Assets: §8. Open questions: §9.

How this was read: read-only Plugin-API dumps of every frame (depth 7: geometry, auto-layout, bound variables → token names, text styles, component + variant + property values, component descriptions, reactions) plus section notes. **`get_design_context` was not run** for these 13 frames: the shared Figma account was rate-limited to roughly one call per minute during this phase, so the budget went to the node-tree dumps and the 2× references (see §9.1). The node-tree dumps replace the usual React reference; every number below comes from them. They weren't kept; the same node data is in `.figma-cache/nodes/77-105.json` (regenerate with `tools/fetch_figma.py`).

---

## 0. Conventions

- Frame = 402 × 874 pt (iPhone 17 Pro); every frame on this page is exactly 402 × 874 (none is tall). All `x, y` are **frame coordinates** (0,0 = frame top-left) unless a line says "component-relative" or "sheet-relative". pt (iOS) = dp (Android). Top safe area 62, bottom 34 (bottom safe edge y 840). All frames use the standard frame auto-layout: vertical, padding 62 / 20 / 34 / 20 (`layout/status-bar`, `layout/screen-margin`, `layout/home-indicator`), background `bg/primary` #FFFFFF, except the Camera (`bg/camera` #2B2B2B).
- Status bar (kit, 402×62 at y0) and home indicator (kit, 402×34 at y840) are **system UI: don't draw them**. The Camera frame uses their light (white) style.
- Invisible prototype frames named `… hotspot` are **not drawn**; their reactions are listed under "Navigation".
- Colours: `tokens.md` names (the `color/` prefix dropped) with hex. Text styles: `tokens.md` names (Manrope). `ls` = letter spacing.
- Text is verbatim. Keep `₹` U+20B9, `−` U+2212, `·` U+00B7, `×` U+00D7 ("Fresh lime soda ×3", "₹240 ÷ 3"), `÷` U+00F7, `’` U+2019 ("tonight’s", "Here’s", "I’ll", "don’t", "aren’t"), `“ ”` U+201C/U+201D in notes, `…` U+2026, and the `\n` hard line breaks shown in sample data.
- Icons: `assets/icons/<name>.svg` (24-grid; scale the whole SVG: 24 → stroke 1.5, 20 → 1.25, 16 → 1.0). **Every icon on these screens already exists** (§8).
- Avatars: `assets/avatars/avatar-N.svg` (Arjun 1, Priya 2, Rohan 3, Esha 4, Dev 5, Kabir 6, Meera 7). Circle fill rule (components-core.md §3.2): `bg/card` #F5F5F5 on white, `bg/primary` #FFFFFF inside #F5F5F5 cards and on black chips.
- Pressed states: README §3 rule 11 (pill = fill change, text button = 50 % opacity, glass icon button = `bg/card`). Rows with no pressed variant: `bg/card-pressed` #EBEBEB clipped to the row/card shape (suggestion).
- Motion: no keyframe motion on this page. Only the prototype transitions listed per screen.
- Test IDs (flow.md format `<screen>.<element>`) are proposed per screen. Root containers: `screen.<id>` (one container per real screen, e.g. `screen.insights` for the three Insights frames; the frame ids above are start-screen/debug ids).

### 0.1 Pro gating on this page (flow.md: free by default; debug menu toggles Pro)
| Feature | Free user | Pro user | Source |
|---|---|---|---|
| Insights (Activity › Insights segment) | Locked state §2.5; "See Pro" → paywall (12-01) | Full report | caption 11-03 |
| Scan receipt | Camera works: take/upload a photo and **attach** it to the expense. "Check receipt" and "Assign items" (reading) → paywall | Full flow | caption 11-07 |
| Recurring (Repeat row in Add expense) | Repeat row → paywall (the row carries a black "Pro" badge, per 06-02) | Repeat sheet §5.3 | caption 11-12, Add expense note 06-02 |
| Ask Paybak | **Not stated.** Every frame is "Shown as a Pro member". Default here: treat as Pro (sparkle → paywall for free users); confirm against the paywall feature list (§9.2 #1) | Full chat | captions 11-04…06 |

### 0.2 Section notes (verbatim)
- Insights, title: "Insights" · subtitle: "The Activity tab with Insights selected: the September report, the rest of it after scrolling, and the locked state free users see."
- Ask Paybak, title: "Ask Paybak" · subtitle: "A full-screen chat from the Home sparkle button: suggested prompts, an answer from live balances, and a drafted expense that waits for Save."
- Scan receipt, title: "Scan receipt" · subtitle: "Take a photo of the bill, check what was read, tap who had each item, then save it as a normal expense, prefilled."
- Recurring, title: "Recurring" · subtitle: "Flat 302’s repeating expenses: fixed rules add themselves on schedule, and variable ones create a draft that waits for your amount."

### 0.3 Overlay helpers (216:24639): not screens
Section subtitle (verbatim): "Prototype-only copies of the page 04 and 08 sheets, so Open overlay works on this page. They aren’t screens."
| Helper frame | Real sheet it copies | Used by | Caption (verbatim) |
|---|---|---|---|
| ↳ Add sheet (overlay) (216:24642) | Overlay — Add sheet (24:808) = `homeAddSheet`, screens-home.md §5 | ＋ on `insightsSeptember` / `insightsScrolled` | "Prototype helper, not a screen: a copy of Overlay — Add sheet (page 04). ＋ on 11-01 and 11-02 opens it with Open overlay. Its rows open Add expense, Record payment, Lend money and New group on page 06; the scrim and ✕ close it." |
| ↳ Remind sheet (overlay) (216:24704) | 08-07 Remind sheet (page "08 Settle Up"; specced in `screens-settle.md`) | "Remind Rohan" chip on `askAnswer` | "Prototype helper, not a screen: a copy of 08-07s Remind sheet (page 08). Remind Rohan on 11-05 opens it with Open overlay, so the chat stays visible under the scrim." |

---

## 1. Components (reuse map)

### 1.1 Already specced (reuse as-is)
| Figma component | Spec | Used here as |
|---|---|---|
| Navigation / Nav Header (17:494) Type=Large Title, Show action | components-home.md §1 | Insights header "Activity" + glass **Restore** button (Recently deleted) |
| Navigation / Nav Header Type=Inline | components-home.md §1 | Collapsed "Activity" bar on `insightsScrolled` |
| Navigation / Tab Bar (17:618) Active=Activity | components-home.md §5 | All three Insights frames |
| Button / Icon (10:79) Style=Glass | components-core.md §2.3 | Month prev/next chevrons (Insights) |
| Control / Segmented (12:249) Options=2, stretched to 362 | components-core.md §4.3 | "Timeline · Insights", "Groups · Friends" |
| Row / Section Header (13:223) | components-home.md §8 | "By category", "Who you spent with", "Lent vs borrowed since April", "Items" (with action "Add item"), "Needs your amount", "Rules" |
| Badge / Pill (11:46) On Card / Inverse / Overdue / Muted | components-core.md §3.1 | "Up 5% from August" (On Card), "Paid back" + "Pro" (Inverse), "Overdue 3 days" (Overdue), "Draft" (On Card on the draft row, **Muted** on Enter amount) |
| Avatar / Circle (11:136) Art 24/32, Icon 24/40/56, Icon On Card 40/56 | components-core.md §3.2 | assistant sparkle, category/group tiles, people, lock circle, category icon on the draft card |
| Avatar / Stack (11:417) Count=3 | components-core.md §3.3 | Arjun · Esha · Dev on the draft expense card |
| Button / Primary (9:36) Large / Small | components-core.md §2.1 | "See Pro", "Looks right", "Continue", "Done" (Large); "Save" on the draft card (Small) |
| Button / Secondary (9:62) Small + leading icon | components-core.md §2.1 | "Remind Rohan" (Bell) |
| Button / On Card (9:88) Small | components-core.md §2.1 | "Edit" (draft card), "Enter amount" (draft row) |
| Button / Text (10:45) Primary | components-core.md §2.2 | "View" on the saved draft card; "Add item" (section header action, Secondary) |
| Divider / Line (12:302) | components-core.md §4.5 | inside cards/rows |
| Row / Activity (13:477) Type=Expense, Surface=On Card, Show detail line | components-home.md §10 | Recurring rule rows (§5.2) |
| Row / Attention (13:379) State=Due soon | components-home.md §9 | "Cooking gas" draft row (customised, §5.2) |
| Sheet / Container (118:1017) Detent=Medium | components-home.md §15 | Repeat sheet |
| Control / Input Field (12:296) State=Filled, no label/helper | components-core.md §4.4 | title field on the Add expense forms |
| Button - Liquid Glass - Symbol (kit, 17:433) Style=Glass | components-home.md §14 (kit glass ✕) | Camera ✕ and flash (44×44 here), sheet ✕ (50×50) |
| Keyboard (kit) Type=Number Pad | system keyboard | Enter amount |

### 1.2 NEW components specced in `components-app.md` (reuse that spec; the summary here is what these screens need)
| Figma component | SwiftUI name (Figma description) | Used here as |
|---|---|---|
| Navigation / Modal Header (115:886) Action=None / Enabled / Disabled | `PBModalHeader` | Ask Paybak (✕ left, centred "Ask Paybak", no action); Add expense ("Save", Enabled on `scanAddExpense`, Disabled on `recurringRepeat`); Enter amount ("Add", Disabled) |
| Navigation / Push Header (97:1082) Trailing=None / Text | `PBPushHeader` | "Check receipt", "Assign items" (back only); "Recurring" (back + text action "Add") |
| Row / Setting (97:996) Trailing=Chevron / None / Toggle | `PBSettingRow` | Suggested prompts; Merchant/Date; Add expense form rows; Enter amount details; Repeat sheet rows |
| Control / Category Chip (64:4185) Leading=None / Avatar / Icon | `PBCategoryChip` | person chips (Assign items, "With you and"), INR/Today, due quick chips, Repeat frequency chips |
| Control / Composer (115:907) State=Empty, Pinned=False | `PBComposer` | Ask Paybak input |
| Control / Amount Display (125:1084) State=Filled / Empty | `PBAmountField` | "₹2,300" (scan), "₹0" (Repeat background, Enter amount) |

Key facts from their descriptions (verbatim quotes):
- Control / Composer: "PBComposer — Single-line input for expense comments (09-03/04) and Ask Paybak (11-04/05/06). Input Field styling: #F5F5F5, radius 14 (input radius, not a pill), 52 tall. ⏎ State=Empty: placeholder in text/tertiary + optional mic (Show mic; Empty only, iOS dictation fills the field and never sends). State=Typing: text/primary + caret + black send (Button / Icon Inverse, Icon / Arrow Up, 36). ⏎ Pinned=False: the bare 362×52 field for in-content use. Pinned=True: the bar pinned on the keyboard — 402 wide (place at x 0), white (color/bg/primary), a full-width Divider / Line on top, 8 top/bottom and 20 side padding, 68 tall; y = 874 − keyboard height − 68 (09-04). ⏎ Text = placeholder (“Add a comment”, “Ask or add an expense”) or typed text. ⏎ SwiftUI: PBComposer"
- Control / Amount Display: "…The amount uses Amount/Display (Manrope ExtraBold 56/64). Empty = the “₹0” placeholder in text/tertiary with the 2×56 caret (not bound to Amount); Focused = the typed Amount + caret; Filled = Amount, no caret. Helper (Footnote, secondary) e.g. “You owe Meera ₹450 in Flat 302”. ⏎ Amount entry always uses the kit keyboard (Number Pad: the kit has no Decimal Pad), never a custom keypad. ⏎ SwiftUI: PBAmountField"
- Row / Setting: "Settings list row for #F5F5F5 card groups, 56 tall (grows with a subtitle). Trailing: Chevron (push), Toggle (exposed kit switch; On track tinted color/bg/inverse), Stepper (exposed kit stepper), Check (black tick, single select), Unchecked (empty 24 slot), None. Tone=Destructive: text/destructive + icon/destructive, never a chevron. Optional icon (Show icon off moves the title and the divider inset to 16), value, subtitle, and an exposed Badge / Pill (Inverse “Pro” / “Try free”). Turn Show value off on Toggle, Stepper, Check and Unchecked rows. SwiftUI: PBSettingRow"
- Row / Setting geometry (components spec §1.2, confirmed by these dumps): 362 wide (354 in the Repeat sheet), min height 56, no fill (sits in a `bg/card` radius-20 group), padding 12 / 16, gap 12; icon 24 at row x 16 (`icon/primary`); title `Headline` `text/primary` (1 line in the component; the Ask prompt "How much did I spend on food this month?" wraps to 2 lines → row 76 tall, so allow wrapping for prompts); value `Body` `text/secondary`, hug; chevron 20 `icon/tertiary` at row x 326; Toggle = kit switch 64 × 28 at row x 282 (On track `bg/inverse`); divider from row x 52 to the group's right edge.
- Control / Category Chip: "Filter and people chip, 36 tall. Selected = inverse fill. Leading=None: text only (avatar categories, currency, date). Leading=Icon: 16 icon swap (the “Add” chip uses Plus). Leading=Avatar: exposed Avatar / Circle 24 for people pickers (Selected=False gives it a white avatar circle so the face reads on the #F5F5F5 chip). Show remove adds a trailing Close 16. SwiftUI: PBCategoryChip". Geometry seen here: Leading=None padding 0/16, gap 6; Leading=Avatar padding 0/16/0/6, gap 8, avatar 24; Leading=Icon padding 0/16/0/12, gap 6, icon 16. Label `Button/Small`. Unselected fill `bg/card`, label `text/primary`; Selected fill `bg/inverse`, label `text/inverse`.

### 1.3 NEW components specced here (not in components-core/home; if `components-app.md` also documents them, it is canonical and should match this)

#### 1.3.1 Chart / Monthly Bars (143:2157). SwiftUI `PBMonthlyBarChart`. NEW
> "PBMonthlyBarChart — six months of your share (11-01 hero card; 11-03 with a layer blur on the instance). 322×140 for the 362 card with 20 padding; columns spread with space-between, so FILL works.
> Each column: “bar-1…6” (32×120 plot, bars grow up from a zero baseline) + “label-1…6” (Footnote, 20 tall; Month 1–6 props, Apr–Sep). The current month (bar-6) uses color/chart/fill and a text/primary label; the others color/chart/bar and text/secondary. Pill tops (radius/full on the top corners).
> Height h (0–120) is a PADDING override: bar-N.paddingTop = 120 − h (size overrides don’t apply to instance sublayers). Defaults = Apr–Sep 2026: 95 · 113 · 101 · 106 · 114 · 120 (₹18,400 … ₹23,300 on a zero baseline).
> Built with Swift Charts.
> SwiftUI: PBMonthlyBarChart"

Properties: `Month 1…6` text (Apr, May, Jun, Jul, Aug, Sep). Geometry (component-relative, 322 × 140, H auto-layout, `SPACE_BETWEEN`, padding 0): 6 columns, each **32 × 140** (V, gap 0) at x = 0, 58, 116, 174, 232, 290 (pitch 58, gap 26):
- `bar-N` 32 × 120 plot (no fill); inside it `fill` = rectangle 32 wide × h, bottom-aligned (paddingTop = 120 − h), corner radius **999 on the top-left/top-right only** (bottom corners square), fill `chart/bar` #D1D1D1 for months 1–5, **`chart/fill` #0A0A0A for month 6** (the selected month).
- `label-N` 32 × 20 at y 120, `Footnote` (Medium 13/18), centred, `text/secondary` #6B6B6B (months 1–5) / **`text/primary` #0A0A0A** (month 6). Fixed width 32 (labels are 3-letter month abbreviations).
- No axis lines, gridlines, values or tooltips. Not interactive in Figma (see §2.7 for the proposed tap behaviour).
- Implementation: iOS may use Swift Charts (`BarMark`, `.clipShape` top-rounded) or plain shapes; Android draws in `Canvas`/Boxes. Either way reproduce the exact geometry above.

#### 1.3.2 Row / Bar (143:2156). SwiftUI `PBBarRow`. NEW
> "PBBarRow — a share bar row: Insights by category and by group (11-01 · 11-02 · 11-03) and “Paid vs fair share” on the project dashboard (10-01). 362×56, no side padding (use FILL inside a card).
> Line 1: exposed leading Avatar / Circle 40, Title (Headline), Caption (Footnote, secondary: “51%” / “Paid ₹25,500”; Show caption), Amount on the right. Line 2: exposed Control / Progress Bar Small (layer “bar”).
> Leading=Icon: Type=Icon on color/bg/card for white surfaces (set its Icon; category map Food · Car · Bed · Ticket · Home · Bolt · Shopping Bag · Tag; groups Home · Plane · People). Leading=Avatar: Type=Art in a white circle for #F5F5F5 cards (set Art).
> Value=Neutral: Amount (Headline, text/primary: “₹12,000”, “Settled”) · Owed: “+” + Amount (Amount/Medium, text/primary) · Owe: “−” + Amount (Amount/Medium, text/secondary). Amount excludes the sign.
> Bar: set the nested bar’s paddings after sizing (see Control / Progress Bar): bar › track › fill.paddingRight = W×(1−p); Avatar variants show the fair-share mark (mark.paddingLeft = W×0.51 − 1). Defaults: Icon 51% (Rent); Avatar Neutral 51% · Owed 100% · Owe 35%.
> SwiftUI: PBBarRow"

Properties: `Leading` = Icon | Avatar, `Value` = Neutral | Owed | Owe, `Title`, `Caption`, `Show caption`, `Amount`. Used here: **Leading=Icon, Value=Neutral** only.
Geometry (component-relative, 362 × 56): H auto-layout, padding 8 / 0 / 8 / 0 (`space/8`, `space/0`), gap 12 (`space/12`), items centred.
1. `leading` Avatar / Circle Size=40 Type=Icon at (0, 8): circle `bg/card` #F5F5F5, icon 20 `icon/primary`.
2. `content` (52, 10) 310 × 36, V, gap 8:
   - `line` 310 × 22, H gap 8, centred, space-between: `label` (H gap 8, centred) = `title` `Headline` (SemiBold 16/22, ls −0.25 %) `text/primary` + `caption` `Footnote` (Medium 13/18) `text/secondary` (baseline-centred: caption box is 2 pt lower); `value` = `amount` `Headline` `text/primary`, right-aligned, hug.
   - `bar` Control / Progress Bar Size=Small, 310 × 6 at (52, 40).
Row height stays 56 (titles are single line; truncate the title with "…" if it would collide with the amount).

#### 1.3.3 Control / Progress Bar (116:1099) Size=Small. SwiftUI `PBProgressBar`. NEW (only Small/Default used here)
> "PBProgressBar — Budget, loan and share bars. Size=Small (6 tall, inside Row / Group and Row / Bar) · Large (12 tall, Card / Budget and Card / Loan Progress). Full-radius color/chart/track track; use the instance at FILL width.
> State=Default (fill only) · Projected (fill + the projected segment in color/chart/bar) · Over (fill up to the budget, then the red color/chart/over segment to the end).
> Percentages are PADDING overrides (Figma ignores size and position overrides on instance sublayers). Inside “track”, the layers “fill”, “projected” and “over” are full-width auto-layout frames holding a “segment”: fill.paddingRight = W×(1−p) · projected.paddingRight = W×(1−p) · over.paddingLeft = W×p (the budget point). For 0% hide “fill”. Show mark shows “mark” (a full-width frame holding the 2pt “tick”, color/chart/fill with a 1pt white outline, overhanging by 3): mark.paddingLeft = W×p − 1. W = the instance width: set the paddings AFTER the bar has its final width (they are pixels, not percentages).
> “over” and “mark” are left-anchored (constraint Left, 362 wide; the track clips “over”), so their paddings always count from the bar’s left edge.
> Drawn defaults: Default fill 60% (mark 51%) · Projected fill 87%, projected 97% (mark 90%) · Over fill 97.6%, over from 97.6% (mark 97.6%).
> SwiftUI: PBProgressBar"

Small/Default as used here: `track` W × 6, corner radius 999 (`radius/full`), fill `chart/track` #EBEBEB, clips; `segment` left-anchored, height 6, width = W × p, fill `chart/fill` #0A0A0A, pill ends (the track clips it). p = 0 → draw no segment. Show mark = false.

#### 1.3.4 Card / Notice (129:1976) Layout=Centered, Actions=One. SwiftUI `PBNoticeCard`. NEW (Centered layout used here)
> "PBNoticeCard — an in-flow notice on #F5F5F5 r20 (06-12 · 07-11 · 08-03 · 08-06 · 09-06 · 10-05 · 10-06 · 11-03): pending confirmation, invite a guest, simplified debts, a dispute, a read-only project, a Pro lock.
> Layout=Leading: exposed icon circle Avatar / Circle 40 Type=Icon On Card (set its Icon: Activity, Shuffle, Mail, Flag, Lock, Check Circle), Title (Headline) with Show badge (exposed Badge / Pill, default Inverse “Pro”) on the title line, Body (Subheadline, secondary, wraps). Layout=Centered: 56 icon circle, badge, Title (Title/3), Body, all centred (11-03).
> Actions=None · One = exposed Button / Primary Large “primary”, full width · Two = exposed Button / Primary Small “primary” + Button / On Card Small “secondary”, both FILL, gap 8. Set the button labels on the nested buttons (Label#9:0 / Label#9:42).
> Show title off hides the whole title line (and the badge with it in Layout=Leading), so the body sits level with the icon.
> All gray and black: never red.
> SwiftUI: PBNoticeCard"

Centered / One geometry (component-relative, 362 wide, hug height = 302 here): fill `bg/card` #F5F5F5, radius 20 (`radius/card`), V auto-layout, padding 24 (`space/24`), gap 12 (`space/12`), children centred.
1. `icon` Avatar / Circle Size=56 Type=Icon On Card: 56 circle `bg/primary` #FFFFFF, icon 24 `icon/primary` (Lock here) at (153, 24).
2. `badge` Badge / Pill Inverse "Pro" (40 × 24) at (161, 92).
3. `text` (24, 128) 314 wide, V gap 8, centred: `title` `Title/3` (Bold 20/26, ls −1 %) `text/primary`, centre-aligned, wraps; `body` `Subheadline` (Medium 14/20) `text/secondary`, centre-aligned, wraps.
4. `actions` (24, 214) 314 wide, padding-top 12: `primary` Button / Primary Large, FILL width (314 × 52).

#### 1.3.5 Chat / Bubble (117:971) Role=User | Assistant. SwiftUI `PBChatBubble`. NEW
> "PBChatBubble — One message in Ask Paybak (11-05, 11-06). The row is 362 wide; use it at FILL.
> Role=User: right-aligned black bubble (bg/inverse, radius 20, padding 12/16) with text/inverse Body, max 280 wide, hugging short text. Role=Assistant: left-aligned plain Body with no bubble, max 320 wide including the 24 Sparkles avatar (Avatar / Circle Type=Icon, exposed; Show avatar, Assistant only).
> Text = the message.
> SwiftUI: PBChatBubble"

Geometry: row 362 wide (H). **User**: `bubble` right-aligned, H, padding 12 / 16 (`space/12`, `space/16`), radius 20 (`radius/card`), fill `bg/inverse` #0A0A0A; `text` `Body` (Regular 16/24) `text/inverse`, left-aligned inside, wraps at a bubble max width of 280 (text max 248). One line = 48 tall ("Who owes me money?" → 196 × 48); two lines = 72. **Assistant**: `message` H gap 8 (`space/8`), max 320 wide: `avatar` Avatar / Circle Size=24 Type=Icon (Sparkles 14, circle `bg/card` #F5F5F5 on white) top-aligned, then `text` `Body` `text/primary` (288 max), wraps; no bubble.

#### 1.3.6 Chat / Draft Expense (147:2315) State=Pending | Saved. SwiftUI `PBDraftExpenseCard`. NEW
> "PBDraftExpenseCard — the assistant’s draft expense in Ask Paybak (11-06). #F5F5F5 r20, padding 16, gap 12, 362 wide.
> Top row: exposed icon circle Avatar / Circle 40 Type=Icon On Card (category icon, default Car), Title (Headline), Amount (Amount/Medium). A divider, then Paid line, Split line with an exposed Avatar / Stack (3: You, Esha, Dev) and Each line (Subheadline).
> State=Pending: exposed Button / Primary Small “save” (FILL) + Button / On Card Small “edit”. State=Saved: Icon / Check Circle 20 + “Expense added” + exposed Button / Text “view” (opens the expense detail, the 09-03 template).
> Interactive: Save → CHANGE_TO State=Saved (On click, Smart animate, ease out, 250 ms). You stay in the chat: no toast and no navigation (the intended exception to “detail + toast”). Text props are shared by both states, so the data survives the change. Nothing is saved without a tap.
> SwiftUI: PBDraftExpenseCard"

Properties: `Title` ("Cab"), `Amount` ("₹600"), `Paid line` ("Paid by you · Today"), `Split line` ("Split equally with Esha and Dev"), `Each line` ("₹200 each"), `State`.
Geometry (component-relative; Pending 362 × 233, Saved 362 × 241): fill `bg/card`, radius 20, V, padding 16 (`layout/card-padding`), gap 12.
1. `top` (16, 16) 330 × 40, H gap 12, centred: `icon` Avatar / Circle 40 Type=Icon On Card (white circle, category icon 20); `title` `Headline` `text/primary`, fills (222), 1 line truncate; `amount` `Amount/Medium` (Bold 17/22, ls −0.5 %) `text/primary`, hug.
2. `divider` Divider / Line Inset=None, 330 × 1 at (16, 68), `border/subtle` #EBEBEB.
3. `details` (16, 81) 330 × 88, V gap 8: `paid` `Subheadline` `text/secondary` (20); `split row` 330 × 32, H gap 8, centred: `split` `Subheadline` `text/secondary` (fills 242) + `members` Avatar / Stack Count=3 (80 × 32; You/Arjun, Esha, Dev; 2 pt white rings); `each` `Subheadline` **`text/primary`** (20).
4. Pending: `actions` (16, 181) 330 × 36, H gap 8: `save` Button / Primary Small "Save", **FILL** (261 × 36); `edit` Button / On Card Small "Edit" (61 × 36, white pill).
   Saved: `status` (16, 181) 330 × 44, H gap 8, centred: `check` Icon / Check Circle **20** (`icon/primary`); `saved label` "Expense added" `Headline` `text/primary`, fills, 1 line; `view` Button / Text Style=Primary "View" (34 × 44, hug).
- State change: Save → Saved in place with a 250 ms ease-out cross-fade/"smart animate" (height grows 233 → 241).

#### 1.3.7 Row / Person (127:2252) Size=Compact, Trailing=Value. SwiftUI `PBPersonRow`. NEW (only this variant here)
> "PBPersonRow — the one people row (split pickers, balances, breakdowns, contacts, expense splits, chat answers). Exposed Avatar / Circle (40 Regular / 32 Compact), Name (Headline) + Show tag (exposed Badge / Pill Muted “Guest”, always next to the name), Subtitle (Subheadline), trailing, Show divider (inset to the name).
> Size=Regular: 64, avatar on color/bg/card for white surfaces (inside a #F5F5F5 card, override the avatar fill to color/bg/primary). Size=Compact: 56, avatar 32 in a white circle and an On Card tag, for use inside a card.
> Trailing: Owed = “+” + Amount (Amount/Medium, text/primary) · Owe = “−” + Amount (Amount/Medium, text/secondary) · Value = Amount (Headline, e.g. ₹700 or 25%) — these three carry Amount label (Footnote, tertiary) and Show status badge (exposed Badge / Pill Overdue “Overdue 3 days”, always under the amount) · Muted = Status (Subheadline, secondary: Settled / Added / No balance) · Check = black tick (single select, e.g. Paid by) · Select On / Select Off = 24 black circle with a white tick / empty border/strong ring (multi-select) · Remove = Icon / Close 20 (icon/secondary) · Button = exposed Button / Secondary Small “Invite” (Regular) or Button / On Card Small “Remind” (Compact) · None.
> Amount excludes the sign: the variant adds “+” or “−”. Guests: Avatar Type=Initials “AR” + Show tag.
> SwiftUI: PBPersonRow"

Compact/Value geometry used here: 362 wide, H, padding 6 / 16 (`space/6`, `layout/card-padding`), gap 12, centred; height 56 (60 when the status badge shows). `avatar` 32 Art, white circle, at (16, 12); `text` column (60, …) fills: `name` `Headline` `text/primary`, 1 line truncate (subtitle and tag hidden here); `trailing` right-aligned column, gap 2: `amount` `Headline` `text/primary` ("₹800"), optional `status badge` Badge / Pill Overdue under it ("Overdue 3 days", 110 × 24); `divider` Divider / Line at the row bottom from x 60 to the right edge (302 wide), hidden on the last row.

#### 1.3.8 Row / Receipt Line (117:993) Style=Default | Total, State=Default | Editing. SwiftUI `PBReceiptLineRow`. NEW
> "PBReceiptLineRow — One line of a scanned receipt (11-08), 362×44 inside a #F5F5F5 card, 16 side padding: Label on the left, Amount on the right.
> Style=Default (Body) · Total (Headline, with a Divider / Line above it). State=Editing turns the amount into an inline white field (bg/primary, radius 14, 36 tall, min 96, focus ring and caret) so a misread value can be fixed in place.
> SwiftUI: PBReceiptLineRow"

Geometry: 362 × 44, H, padding 0 / 16, gap 12, centred. `label` fills, 1 line truncate; `amount` hug, right. Default: both `Body` (Regular 16/24) `text/primary`. Total: both `Headline` (SemiBold 16/22) `text/primary` + `divider` Divider / Line Inset=None 330 × 1 at the row top (x 16…346), absolute. Editing (not drawn on this page; from the description): the amount becomes an inline field `bg/primary`, radius 14 (`radius/input`), height 36, min width 96, 1.5 pt `border/strong` ring + caret, number keyboard.

#### 1.3.9 Row / Assign Item (147:2532) Shared=False | True. SwiftUI `PBAssignItemRow`. NEW
> "PBAssignItemRow — one receipt item on Assign items (11-09): tap who had it. Row on white, no side padding, full-width divider (Show divider).
> Line 1: Item (Headline, truncates) and Price (Amount/Medium). Line 2: three exposed Control / Category Chip Leading=Avatar, 36 tall, named you · esha · dev (Label You/Esha/Dev, Avatar / Circle 24 Art). A chip is Selected=True (black) when that person had the item; switch the chip’s Selected per row.
> Shared=False (90): default Dev selected (Chicken biryani). Shared=True (108): adds Shared caption (Footnote, secondary: “Shared by 3 · ₹80 each”) under the item, all three chips selected.
> Text props share one default (Chicken biryani ₹430): override Item/Price per row.
> SwiftUI: PBAssignItemRow"

Geometry (362 wide, on white): V, padding 12 / 0 / 12 / 0, gap 8.
1. `line` 362 wide, H gap 12: `text` column fills (308), V gap 0: `item` `Headline` `text/primary` 1 line truncate; `shared caption` (only when more than one person) `Footnote` `text/secondary` 1 line truncate; `price` `Amount/Medium` `text/primary`, hug, right.
2. `chips` H gap 8, hug: one Control / Category Chip Leading=Avatar per person on the expense (here You 80 × 36, Esha 89 × 36, Dev 82 × 36). Unselected: fill `bg/card`, avatar circle white, label `text/primary`. Selected: fill `bg/inverse`, label `text/inverse` (avatar circle stays white so the face reads).
3. `divider` Divider / Line Inset=None, full 362 at the row bottom; hidden on the last row.
Heights: 90 (one line of text) / 108 (with the shared caption).
Tap a chip = toggle that person on the item (multi-select). The caption shows when ≥ 2 people are selected: "Shared by {n} · {₹each} each".

#### 1.3.10 Card / Person Totals (147:2533). SwiftUI `PBPersonTotalsCard`. NEW
> "PBPersonTotalsCard — the live per-person totals pinned above Continue on Assign items (11-09). #F5F5F5 r20, padding 16, gap 6, 362×96.
> Status row: Icon / Check Circle 16 + Status line (“All items assigned”) and Note (“Includes GST and tip”, right-aligned, secondary). Then three columns: exposed Avatar / Circle 32 Art (white circle), Name (Footnote, secondary) over Amount (Amount/Medium): You ₹989 · Esha ₹621 · Dev ₹690 (tax and tip split in proportion; they sum to ₹2,300).
> SwiftUI: PBPersonTotalsCard"

Properties: `Status line`, `Note`, `Name 1…3`, `Amount 1…3`. Geometry: 362 × 96, fill `bg/card`, radius 20, V, padding 16, gap 6.
1. `status` 330 × 18, H gap 6, centred: `check` Icon / Check Circle 16 (`icon/primary`); `status line` `Footnote` `text/primary`, hug; `note` `Footnote` `text/secondary`, right-aligned, fills, truncate.
2. `people` 330 × 40, H gap 12: three equal columns (102 each), each H gap 8 centred: avatar 32 Art (white circle) + V text: `name` `Footnote` `text/secondary` (18) over `amount` `Amount/Medium` `text/primary` (22).
For more than 3 people (not designed): proposal: keep equal columns up to 4; beyond that make the people row horizontally scrollable.

#### 1.3.11 Control / Shutter (117:1001) State=Default | Pressed. SwiftUI `PBShutterButton`. NEW
> "PBShutterButton — Camera shutter for Scan receipt (11-07). Fallback: the iOS 27 kit has no camera chrome. A 76 white ring (4pt, bg/primary) around a 62 white disc; Pressed shrinks the disc to 56 in bg/card-pressed. Always sits on the dark color/bg/camera backdrop (the set background shows it).
> SwiftUI: PBShutterButton"

76 × 76: `ring` ellipse 76 with a 4 pt **inside** stroke `bg/primary` #FFFFFF, no fill; `disc` ellipse 62 at (7, 7), fill `bg/primary`. Pressed: disc 56 (centred) fill `bg/card-pressed` #EBEBEB.

#### 1.3.12 Art / Receipt (86:730) Size=Full | Thumb. NEW art (see §7, §8)
Size=Full (86:667) 300 × 458: white paper with a 2 pt #EBEBEB outline and a zig-zag torn bottom (`assets/images/receipt-paper.svg`), dotted separators (`#0A0A0A`, dash 2 / gap 4, 260 wide), and the Leopold Cafe receipt text. Rendered: `assets/images/receipt-leopold-cafe@3x.png` (900 × 1374). Size=Thumb (86:712) 56 × 72: a miniature receipt on #EBEBEB, radius 10: `assets/images/receipt-thumb.svg` (already exported for another spec; matches this variant).

---

## 2. Insights (Activity tab, "Insights" segment): `insightsSeptember`, `insightsScrolled`, `insightsLocked`

**Container:** tab root. It is the **Activity** tab (tab bar Active=Activity) with the segmented control on **Insights**; "Timeline" is the other segment (Activity › Timeline, specced in `screens-activity.md`, frame 167:14361). One screen, three states: top of the report (§2.2–2.3), scrolled (§2.4), locked for free users (§2.5). Pro members see the report; free users see Locked.

### 2.1 Scaffold (all three states)
- Frame auto-layout as §0; background `bg/primary`.
- **Scroll view** = everything below the status bar: header, segmented control, month row and report scroll together (in Figma the frame's ON_DRAG reaction swaps September ↔ Scrolled: SMART_ANIMATE 350 ms ease-in-out, i.e. a simulated scroll). Content scrolls **under the glass tab bar**; bottom content inset as Home (screens-home.md §1: ≈ 107 pt from the screen bottom).
- **Floating glass tab bar** (Navigation / Tab Bar, Active=**Activity**) at x20 y791 362×62, identical to Home (components-home.md §5): Activity item selected (`bg/selected` pill, black icon/label).
- ＋ in the tab bar opens the Add sheet (`homeAddSheet`, prototype: OPEN OVERLAY ↳ Add sheet, MOVE_IN from bottom, EASE_OUT 300 ms).
- The collapsed **inline bar** appears on scroll (§2.4).

### 2.2 `insightsSeptember` (167:17585): element list, top to bottom
Designer notes (caption 11-01, verbatim): "Your share of expenses in groups and with friends, month by month. The chart starts at zero, so a 5% change looks small on purpose. Projects are tracked on their own dashboards. Shown as a Pro member."

1. **Top block** x20 y62 w362 h96, V gap 16 (part of the scroll content):
   - **Header** Navigation / Nav Header Type=Large Title, Show action=true: 362 × 44 at (20, 62). Title "Activity" `Title/1` (ExtraBold 32/38, ls −2 %) `text/primary` at (20, 65). Action = Button / Icon Style=Glass with **Icon / Restore** (`restore.svg`, 24) at (338, 62) 44 × 44 = **Recently deleted** (`screens-activity.md`, 177:29968).
   - **Segmented** Control / Segmented Options=2 at (20, 122), stretched to **362 × 36** (container padding 3, segments 178 × 30): "Timeline" (unselected, `text/secondary`) | "**Insights**" (selected: `bg/inverse` pill, `text/inverse`). Tap Timeline → Activity › Timeline (same tab, segment switch; no push).
2. **Report** (scroll content) x20 y158 w362, V, padding-top 8, gap 16:
   - **Month row** (20, 166) 362 × 44, H space-between, centred:
     - Previous month: Button / Icon Glass + **Icon / Chevron Left** (20, 166) 44 × 44.
     - Month label "September 2026" `Headline` `text/primary`, centred at (137.5, 177) (127 × 22). Format `MMMM yyyy` (e.g. "August 2026").
     - Next month: Button / Icon Glass + **Icon / Chevron Right** (338, 166) 44 × 44 at **opacity 0.3** = disabled (you are on the current month). Enabled (opacity 1) when the selected month is before the current month.
   - **Hero card** (20, 226) 362 × 280: fill `bg/card` #F5F5F5, radius 20 (`radius/card`), padding 20 (`space/20`), V, **space-between** (summary at the top, chart at the bottom).
     - `Summary` (40, 246) 322 × 62, V gap 4:
       - label "Your share of shared expenses" `Subheadline` `text/secondary` (40, 246).
       - Amount row (40, 270) H gap 8, centred: amount "₹23,300" `Title/1` `text/primary` (127 × 38); **trend badge** Badge / Pill Style=On Card "Up 5% from August" (175, 277) 133 × 24 (white pill, `Caption/1` `text/secondary`).
     - **Chart / Monthly Bars** (40, 346) 322 × 140 (§1.3.1): columns at x 40, 98, 156, 214, 272, 330; bar fills (bottom at y 466): Apr h95 (y371), May h113 (y353), Jun h101 (y365), Jul h106 (y360), Aug h114 (y352), **Sep h120 (y346, black)**; labels Apr May Jun Jul Aug **Sep** at y 466–486.
   - **By category** (20, 522) 362 × 376, V, padding-top 8, gap 0:
     - Row / Section Header "By category" (Show action=false) (20, 530) 362 × 32.
     - Six **Row / Bar** (Leading=Icon, Value=Neutral, Show caption=true), 362 × 56 each, at y 562, 618, 674, 730, 786, 842 (the last two sit under the tab bar and scroll into view):
       | Title | Caption | Amount | Leading icon | Bar fill (of 310) |
       |---|---|---|---|---|
       | Rent | 51% | ₹12,000 | Home (`home.svg`) | 158.1 |
       | Food | 17% | ₹3,850 | Food (`food.svg`) | 52.7 |
       | Stays | 15% | ₹3,600 | Bed (`bed.svg`) | 46.5 |
       | Fun | 8% | ₹1,800 | Ticket (`ticket.svg`) | 24.8 |
       | Travel | 5% | ₹1,200 | Car (`car.svg`) | 15.5 |
       | Bills | 4% | ₹850 | Bolt (`bolt.svg`) | 12.4 |
3. **Tab bar** + ＋ as §2.1.
- **Scroll behaviour:** the whole column scrolls; the report continues with §2.4's sections. No pull-to-refresh (local data).
- **Keyboard:** none.

### 2.3 Navigation (Insights)
| Element | Action | Destination | Transition |
|---|---|---|---|
| Restore glass button | tap | Recently deleted (`screens-activity.md`, 177:29968) | push (Figma: URL link to that frame) |
| "Timeline" segment | tap | Activity › Timeline (167:14361) — same tab, segment switch | none (cross-fade optional) |
| Month ‹ | tap | previous month's report (all numbers recomputed) | none (suggestion: 200 ms cross-fade of the report, bars animate height) |
| Month › | tap (only when enabled) | next month | same |
| Row / Bar (category or group) | tap | **not designed**. Proposal: push the Activity timeline filtered to that category/group for the month | push |
| "Groups · Friends" segment | tap | switch the "Who you spent with" list | none |
| Loan row (Kabir · Bike service) | tap | proposal: loan detail (`loanPaidBack`, screens-record-lend-group.md §5.3) | push |
| Tab bar items | tap | Home (`homeActive`), Groups (167:14881), Profile (64:4316); ＋ → `homeAddSheet` | tab switch / overlay |
| Frame drag (prototype only) | drag | `insightsScrolled` | simulated scroll |

### 2.4 `insightsScrolled` (167:20007): the rest of the report, large title collapsed
Designer notes (caption 11-02, verbatim): "The rest of the September report after scrolling. The large title collapses to an inline title, as in iOS. Friends lists each friend with the same bar rows, and every bar shows a share of ₹23,300 (55%, 34% and 11% for the groups). Shown as a Pro member."

Same scroll content, scrolled by **676 pt** (Travel's row moved from y786 to y110). Differences from §2.2:
- **Inline bar** Navigation / Nav Header Type=**Inline** at (0, 62) **402 × 44** (full-bleed, absolute, pinned below the status bar): fill `bg/primary` @ 90 % + **background blur 24**, no shadow; title "Activity" `Headline` `text/primary`, centred. No glass action in the inline bar. Content scrolls under it (the "Fun" row is visible blurred through it; the rows above the viewport are clipped by the status-bar strip).
  - **Collapse rule (iOS "large title" behaviour):** show the inline bar once the large title has scrolled under the top safe-area edge, i.e. when scroll offset ≥ 44 (the header height); cross-fade it in over the next ~10 pt (or a 150 ms fade). Above that offset only the large header shows. The segmented control and month row simply scroll away (they are not pinned). iOS: a custom scroll-offset reader (not `.navigationTitle`, because the header has a custom glass button and the segmented control); Android: `LazyColumn` + `derivedStateOf { firstVisibleItemIndex > 0 || offset ≥ 44.dp }`.
  - Status-bar strip: keep a solid `bg/primary` backdrop behind the status bar (0–62) as on Home, so the blur bar reads as one surface with it (the ref shows the bar's blur starting right below the status bar).
- Visible content (frame y):
  1. Row / Bar "Fun" partly under the inline bar (row top y54, clipped at y110).
  2. Row / Bar **Travel** 5% ₹1,200 (Car) at y110; **Bills** 4% ₹850 (Bolt) at y166.
  3. **Who you spent with** (20, 222) 362 × 276, V, padding-top 24, gap 8:
     - Row / Section Header "Who you spent with" (20, 246).
     - Control / Segmented Options=2 (20, 286) 362 × 36: "**Groups**" (selected) | "Friends".
     - Group rows (Row / Bar Leading=Icon Neutral) at y 330, 386, 442:
       | Title | Caption | Amount | Icon | Bar fill (of 310) |
       |---|---|---|---|---|
       | Flat 302 | 55% | ₹12,850 | Home | 170.5 |
       | Goa Trip | 34% | ₹7,900 | Plane (`plane.svg`) | 105.4 |
       | Without a group | 11% | ₹2,550 | People (`people.svg`) | 34.1 |
     - **Friends** (not drawn; caption: "Friends lists each friend with the same bar rows, and every bar shows a share of ₹23,300"): Row / Bar with the friend's Peep head. On white, use Leading=Icon styling with the Art head inside the 40 `bg/card` circle (Leading=Avatar is the white-circle variant for cards). Rule in §2.6.
  4. **Lent vs borrowed** (20, 498) 362 × 232, V, padding-top 24, gap 8:
     - Row / Section Header "Lent vs borrowed since April" (20, 522). "April" = the first month of the chart window (§2.6).
     - Card + footnote (20, 562) V gap 12:
       - **Lent vs borrowed card** (20, 562) 362 × 120: `bg/card`, radius 20, padding 16 (`layout/card-padding`), V gap 12.
         - Columns (36, 578) 330 × 52, H gap 16, two equal columns (157): **Lent**: "Lent" `Footnote` `text/secondary` (36, 578) over "₹4,500" `Amount/Large` (ExtraBold 26/32) `text/primary` (36, 598). **Borrowed** at x 209: "Borrowed" over "₹0" (same styles; ₹0 stays `text/primary` in Figma).
         - Loan row (36, 642) 330 × 24, H gap 8, centred: Avatar / Circle 24 Art **Kabir** (`avatar-6`, white circle); "Kabir · Bike service" `Footnote` `text/secondary`, fills; status Badge / Pill **Inverse** "Paid back" (289, 642) 77 × 24.
         - More loans (not drawn): one loan row per loan in the window, newest first, gap 8; badge: "Paid back" (Inverse), "Overdue" (Overdue red) or the next due date (On Card, e.g. "Due 3 Nov") — proposal matching the loan screens (screens-record-lend-group.md).
       - Footnote "Totals are your share of expenses in groups and with friends. Projects, payments and loans aren’t counted." `Footnote` `text/tertiary`, (20, 694) 362 × 36 (2 lines). End of content (y 730).
- Tab bar as §2.1. The frame's ON_DRAG swaps back to `insightsSeptember`.

### 2.5 `insightsLocked` (167:21336): free plan
Designer notes (caption 11-03, verbatim): "State: Arjun, on the free plan, opens Insights. The charts stay blurred behind the lock, “See Pro” opens the paywall (12-01), and Timeline still works."

Same layout as §2.2 with the **real report rendered underneath but unreadable**:
- Header, Restore button, segmented control, month row: **unchanged and not blurred** (Timeline still works; propose the month chevrons are inert while locked).
- Blurred parts (each: **opacity 0.4 + layer blur 16**): hero `Summary`, `Chart / Monthly Bars`, the "By category" section header and all six Row / Bar rows. The hero card's own `bg/card` fill is **not** blurred.
  - iOS: `.blur(radius: 8)` (SwiftUI blur radius ≈ Figma blur ÷ 2) + `.opacity(0.4)`; Android 12+: `Modifier.blur(16.dp)` + `alpha(0.4f)`; below API 31 draw the rows at alpha 0.15 without blur (no RenderEffect).
- **Lock overlay**: rectangle (0, 226) 402 × 648 (from the hero card's top to the screen bottom), fill `alpha/white-60` #FFFFFF @ 60 %, absolute, above the report, below the notice card and the tab bar. Not interactive (blocks taps on the report).
- **Pro notice** Card / Notice Layout=Centered, Actions=One (§1.3.4) at (20, 358) 362 × 302, absolute (fixed, doesn't scroll): lock circle (173, 382) 56 with `lock.svg`; "Pro" Inverse badge (181, 450); title "Insights are part of Paybak Pro" (44, 486) `Title/3`; body "See monthly trends, spending by category, group and friend, and lent vs borrowed." (44, 520, 2 lines) `Subheadline` `text/secondary`; **"See Pro"** Button / Primary Large (44, 584) 314 × 52.
- Scroll: disabled while locked (the frame is static).
- Tab bar as §2.1.
- Navigation: **See Pro → paywall (12-01)**, presented as `screens-settings.md` specifies (full-screen modal). After a successful (mock) purchase the entitlement flips and Insights shows the full report. Timeline segment → Activity › Timeline. Restore → Recently deleted.

### 2.6 Insights aggregation rules (what every number means)
All amounts are in the user's default currency (INR here); expenses in other currencies use the rate saved with the expense (Add expense note 06-08).
1. **Scope ("your share of shared expenses")**: for each **expense** in a **group** (not a project) or **with friends outside any group**, take the **user's share** (their split amount, whoever paid). **Excluded:** projects ("Projects are tracked on their own dashboards"), **payments/settlements**, **loans (IOUs)**, **drafts** from variable recurring rules until their amount is entered (§5.5), and **deleted** expenses. Guests' shares don't matter; only the user's own share counts.
2. **Month** = calendar month in the device time zone, by the expense date (not the created date). Label `MMMM yyyy`.
3. **Hero total** = Σ user's share for the selected month ("₹23,300"). Format with the Indian grouping (₹1,00,000 for lakhs), no decimals when the value is a whole rupee (proposal: show paise only when non-zero, e.g. "₹1,234.50").
4. **Trend badge** vs the previous calendar month: pct = round(|cur − prev| ÷ prev × 100). Text: "Up {pct}% from {Month}" when cur > prev, "Down {pct}% from {Month}" when cur < prev, "Same as {Month}" when pct = 0 (the last two are proposals; only "Up" is drawn). Hide the badge when prev = 0. `{Month}` = full month name ("August").
5. **Chart window** = the selected month and the 5 before it (6 columns, oldest left). Heights in §2.7.
6. **By category** = Σ user's share per expense category for the month; rows sorted by amount descending; categories with ₹0 are omitted. Caption = percentage of the hero total, **rounded with the largest-remainder method so the captions add up to 100 %** (Figma: 12,000 / 23,300 = 51.50 % shows **51%** while Food 16.52 % → 17%, Fun 7.73 % → 8%, Bills 3.65 % → 4%; plain rounding would give 101 %). Bar fill = W × (caption % ÷ 100) (Figma sets the bar from the displayed percentage: 0.51 × 310 = 158.1). Category → icon: Rent → Home, Food → Food, Stays → Bed, Fun → Ticket, Travel → Car, Bills → Bolt, Shopping → Shopping Bag, Other → Tag (the category list and icons come from the Add expense Category sheet, 06-07).
7. **Who you spent with → Groups** = Σ user's share per group; expenses with friends outside any group are one row "**Without a group**" (People icon). Sorted by amount descending, same percentage and bar rules. Group icon = the group's icon (Flat 302 → Home, Goa Trip → Plane).
8. **Who you spent with → Friends** (not drawn): one row per friend. To keep "every bar shows a share of ₹23,300", **split the user's share of each expense evenly across the other people on that expense** (e.g. my ₹700 share of a dinner with Priya, Esha and Dev counts ₹233.33 to each of them; distribute leftover paise with the same fair rotation as splits); rows sorted by amount, percentages by largest remainder. This is a proposal (§9.2 #3).
9. **Lent vs borrowed since {first month of the window}**: Lent = Σ principal of loans where the user is the lender, created in the chart window (Apr–Sep here); Borrowed = the same where the user is the borrower. The list shows each of those loans ("{other person} · {loan note}") with its status badge. Loans don't count toward any other number on this screen.
10. **Empty states (not designed; proposals):** a month with no shared expenses shows the hero "₹0" with no badge, a chart of the available months, and instead of the category/group lists a single `Footnote` `text/tertiary` line "No shared expenses in {Month}."; the Lent vs borrowed card shows "₹0 / ₹0" and no loan rows.

### 2.7 Chart spec (Chart / Monthly Bars)
- **Axis from zero** ("The chart starts at zero, so a 5% change looks small on purpose."): bar height h = round(120 × amount ÷ max(amounts in the 6-month window)); the tallest month is 120 pt. Never start the axis at the minimum.
- Month with ₹0 → h = 0 (no bar, label still shown). A future month never appears (the window ends at the selected month).
- Colour: selected month `chart/fill` #0A0A0A + `text/primary` label; the other 5 `chart/bar` #D1D1D1 + `text/secondary` labels.
- Labels = 3-letter month (`MMM`, "Apr"…"Sep").
- Interaction (not designed; proposal): tapping a column selects that month (same as using the chevrons). Accessibility: the chart is one element with the label "Your share by month: April ₹18,400, …, September ₹23,300".
- Motion (proposal): bars grow from the baseline over 300 ms ease-out when the month changes; Reduce Motion → no animation.

### 2.8 Sample data (Insights, verbatim; every number must come out of the demo dataset's real calculations)
- Month: "September 2026" (next disabled).
- Hero: "Your share of shared expenses" · "₹23,300" · "Up 5% from August".
- Chart: Apr May Jun Jul Aug Sep with heights 95 · 113 · 101 · 106 · 114 · 120; the component description gives ₹18,400 (Apr) … ₹23,300 (Sep). Monthly totals that reproduce the heights **and** "Up 5%" (proposal for the demo data): **Apr ₹18,400 · May ₹21,950 · Jun ₹19,600 · Jul ₹20,600 · Aug ₹22,200 · Sep ₹23,300** (→ 94.8, 113.0, 100.9, 106.1, 114.3, 120 → rounded exactly as drawn; (23,300 − 22,200) ÷ 22,200 = 4.95 % → "5%").
- By category: Rent 51% ₹12,000 · Food 17% ₹3,850 · Stays 15% ₹3,600 · Fun 8% ₹1,800 · Travel 5% ₹1,200 · Bills 4% ₹850 (sum ₹23,300).
- Groups: Flat 302 55% ₹12,850 · Goa Trip 34% ₹7,900 · Without a group 11% ₹2,550 (sum ₹23,300).
  - Consistency with other pages: Flat 302 = Rent ₹12,000 (₹36,000 ÷ 3, §5) + Electricity ₹450 (Home "Electricity bill · Flat 302 · You owe −₹450") + Wi-Fi ₹400 (₹1,200 ÷ 3, §5) = ₹12,850, and Bills = ₹450 + ₹400 = ₹850. The Cooking gas draft (no amount) counts ₹0. Goa Trip ₹7,900 and Without a group ₹2,550 cover Stays ₹3,600, Travel ₹1,200, Food ₹3,850 and Fun ₹1,800; the Olive Garden dinner share ₹700 (Food) and the movie tickets share (Fun) are "without a group". The exact expense list is the demo-data owner's call as long as all the totals above come out.
- Lent vs borrowed since April: Lent ₹4,500 · Borrowed ₹0 · "Kabir · Bike service" · "Paid back".
- Footnote: "Totals are your share of expenses in groups and with friends. Projects, payments and loans aren’t counted."
- Locked: "Pro" · "Insights are part of Paybak Pro" · "See monthly trends, spending by category, group and friend, and lent vs borrowed." · "See Pro".

### 2.9 Test IDs
`screen.insights`, `activity.segment.timeline`, `activity.segment.insights`, `activity.recentlyDeleted`, `insights.monthPrev`, `insights.monthNext`, `insights.month`, `insights.total`, `insights.trend`, `insights.chart`, `insights.category.<name>`, `insights.who.groups`, `insights.who.friends`, `insights.group.<name>`, `insights.friend.<name>`, `insights.lent`, `insights.borrowed`, `insights.loan.<id>`, `insights.locked`, `insights.seePro`, `activity.inlineHeader`.

---

## 3. Ask Paybak: `askStart`, `askAnswer`, `askConfirm`

**Container:** full-screen modal (slides up) from the **Home sparkle** glass button (Home header "assistant", screens-home.md §1.1; also reachable from the debug start screen). One screen, a chat that grows; the three frames are its states. Close (✕) dismisses back to where it was opened (Home). Pro gating: §0.1.

### 3.1 Shared chrome (all three frames)
- **Header** Navigation / Modal Header Action=None, Show title (20, 62) 362 × 44: **✕** close button at the left (kit glass xmark style, 44 × 44, white disc with a soft shadow in the ref) at (20, 62); title "Ask Paybak" `Headline` `text/primary`, centred; no trailing action. The header is pinned (not scrolling); on `askConfirm` a white **scroll-edge fade** sits behind it (§3.4).
- **Composer** Control / Composer State=Empty, Pinned=False, Show mic=true at (20, 788) 362 × 52 (bottom = the safe-area edge): `bg/card` #F5F5F5, radius 14 (`radius/input`), padding 0 / 16, gap 8; placeholder "Ask or add an expense" `Body` `text/tertiary` (36, 802); **mic** Icon / Mic 24 at (342, 802), `icon/secondary`.
  - Typing (State=Typing, from the component): text `text/primary` + caret; the mic is replaced by a **36 black send button** (Button / Icon Inverse with Icon / **Arrow Up**, `arrow-up.svg`). Send on tap or on the keyboard's Send key; empty/whitespace can't be sent.
  - Mic: dictation fills the field and **never sends** (caption 11-04). iOS: rely on the system keyboard dictation, or `SFSpeechRecognizer` into the field; Android: `RecognizerIntent.ACTION_RECOGNIZE_SPEECH` into the field. The user taps send.
  - **Keyboard:** focusing the composer switches it to Pinned=True: a 402-wide white bar with a full-width hairline on top, padding 8 / 20, 68 tall, riding on the keyboard (y = 874 − keyboard height − 68). The chat scrolls so the newest message stays visible above the bar. Dismiss the keyboard by dragging the chat (iOS `.scrollDismissesKeyboard(.interactively)`; Android `imePadding()` + tap outside).
- Status bar, home indicator: system.

### 3.2 `askStart` (167:13148): empty chat with suggested prompts
Designer notes (caption 11-04, verbatim): "Opens from the sparkle button in the Home header. Suggested prompts send in one tap, and the mic dictates into the field without sending. Shown as a Pro member."

Content column (20, 106) 362 wide, V, padding-top 64, gap 32:
1. **Greeting** (20, 170) 362 × 158, V gap 16:
   - Assistant avatar Avatar / Circle Size=56 Type=Icon: 56 circle `bg/card`, **Icon / Sparkles** 24 `icon/primary`, at (20, 170).
   - Text (20, 242) V gap 8: "What can I help with, Arjun?" `Title/2` (Bold 24/30) `text/primary` (runtime: "What can I help with, {first name}?"); "Ask about balances and due dates, or add an expense in plain words." `Body` `text/secondary` (2 lines, 362 wide).
2. **Try asking** (20, 360) 362 × 270, V gap 8:
   - label "Try asking" `Footnote` `text/tertiary` (20, 360).
   - **Suggestions card** (20, 386) 362 × 244: `bg/card`, radius 20, V gap 0, four Row / Setting (Trailing=Chevron, Show icon, no value):
     | y | Height | Icon | Title (Headline, wraps) | Divider |
     |---|---|---|---|---|
     | 386 | 56 | People (`people.svg`) | Who owes me money? | yes |
     | 442 | 76 | Food (`food.svg`) | How much did I spend on food this month? (2 lines) | yes |
     | 518 | 56 | Calendar (`calendar.svg`) | When is Goa Trip due? | yes |
     | 574 | 56 | Bell (`bell.svg`) | Draft a reminder for Rohan | no |
     Icon 24 at x 36, title from x 72, chevron 20 at x 346 (`icon/tertiary`), dividers from x 72 to 382.
3. **Privacy note** (absolute, centred) (92, 756) 218 × 18, H gap 6, centred: Icon / Lock 16 (`icon/tertiary`) + "Paybak only sees your own data." `Footnote` `text/tertiary`. Hidden once the chat has messages (it isn't in the Answer/Confirm frames).
4. Composer (§3.1).
- Tap a prompt = send it immediately (adds the user bubble, then the answer). Prototype: the first prompt → `askAnswer` (DISSOLVE 300 ms ease-out).
- Scroll: the start content doesn't scroll unless Dynamic Type makes it taller than the space above the composer.

### 3.3 `askAnswer` (167:14107): "Who owes me money?"
Designer notes (caption 11-05, verbatim): "Answers use your live balances, and the numbers match Home (₹2,900 from 4 people). Action chips open the normal flows, so reminding Rohan uses the usual Remind sheet. Shown as a Pro member."

**Chat** (20, 106) 362 wide, V, padding-top 16, gap 12 (scrolls between the header and the composer; newest at the bottom):
1. **Exchange** (20, 122) V gap 16:
   - User message Chat / Bubble Role=User: bubble (186, 122) 196 × 48, "Who owes me money?".
   - Assistant message Role=Assistant (20, 186) 362 × 72: sparkle avatar 24 at (20, 186); text (52, 186) 288 × 72 (3 lines): "4 people owe you ₹2,900: Rohan ₹800 (overdue since 27 Sep), and Priya, Esha and Dev ₹700 each for tonight’s dinner."
2. **Answer card** (20, 270) 362 × 228: `bg/card`, radius 20, V gap 0, four Row / Person Size=Compact Trailing=Value (§1.3.7):
   | y | Height | Person (avatar) | Amount | Status badge | Divider |
   |---|---|---|---|---|---|
   | 270 | 60 | Rohan (`avatar-3`) | ₹800 | Overdue "Overdue 3 days" (256, 300) 110 × 24 | yes |
   | 330 | 56 | Priya (`avatar-2`) | ₹700 | – | yes |
   | 386 | 56 | Esha (`avatar-4`) | ₹700 | – | yes |
   | 442 | 56 | Dev (`avatar-5`) | ₹700 | – | no |
3. **Action chips** (20, 510) 362 × 36, H gap 8 (wraps if needed): "**Remind Rohan**" Button / Secondary Small, leading **Icon / Bell** 16, 157 × 36.
4. Composer (§3.1). The privacy note is gone.
- Navigation: **Remind Rohan → the usual Remind sheet** (08-07, `screens-settle.md`) over this chat (prototype: OPEN OVERLAY ↳ Remind sheet, MOVE_IN from bottom, EASE_OUT 300 ms); the chat stays visible under the scrim. Tapping a person row (not designed; proposal) → that friend's detail (`screens-groups.md`). Composer (prototype) → `askConfirm` (DISSOLVE 300 ms ease-out) to stand in for typing.

### 3.4 `askConfirm` (167:15071): drafted expense waiting for Save
Designer notes (caption 11-06, verbatim): "The assistant drafts the expense as a card (Travel, guessed from “cab”) and never saves without a tap. Unlike forms, which close onto the new item’s detail with a toast, Save keeps you in the chat and turns the card into “Expense added” with View, which opens the expense’s detail. Edit opens the full Add expense form, prefilled. Shown as a Pro member."

The chat has scrolled up (content in frame coords; `Chat (scrolled)` starts at y91 under the header):
1. Answer card (20, 91) (its top is under the header/fade) and "Remind Rohan" chip (20, 331): as §3.3.
2. **New exchange** (20, 379) V, padding-top 12, gap 12:
   - Exchange (20, 391) V gap 16: User bubble (102, 391) **280 × 72** (2 lines, max width reached): "Add ₹600 for a cab, split with Esha and Dev"; Assistant (20, 479) 362 × 48 (2 lines): "Here’s what I’ll add. Nothing is saved until you tap Save."
   - **Draft expense** Chat / Draft Expense State=Pending (§1.3.6) at (20, 539) 362 × 233: Car icon (white circle 40), "Cab", "₹600"; divider; "Paid by you · Today"; "Split equally with Esha and Dev" + Avatar / Stack (Arjun, Esha, Dev); "₹200 each"; **Save** (36, 720) 261 × 36 + **Edit** (305, 720) 61 × 36.
3. **Scroll-edge fade** (0, 0) 402 × 150, above the chat and below the header: vertical gradient white 100 % at 0 → white 100 % at 60 % (y 90) → white 0 % at 100 % (y 150). It hides content scrolling up under the ✕/title.
4. Composer (§3.1).
- **Save** → the card changes in place to **State=Saved** (250 ms ease-out): the actions row becomes check-circle 20 + "Expense added" + "View" (text button). The expense is created for real (balances update: Esha and Dev each owe ₹200). **No toast, no navigation.**
- **View** → the new expense's detail (09-03 Expense detail template, `screens-activity.md`), pushed.
- **Edit** → the full **Add expense** form (06-02 template, `screens-add-expense.md`) as a full-screen modal, prefilled (amount ₹600, "Cab", Travel, paid by you, today, split equally with Esha and Dev). Saving there follows the normal form flow (closes onto the expense detail with "Expense added" toast); when the form is dismissed, the chat card changes to Saved if the expense was saved, otherwise stays Pending.
- After Saved, the draft can't be saved twice (the Save button is gone).

### 3.5 Navigation (Ask Paybak)
| Element | Action | Destination | Transition |
|---|---|---|---|
| Home sparkle (glass button) | tap | this screen (Start, or the last conversation state) | full-screen modal, slide up (system) |
| ✕ | tap | dismiss to Home (prototype: link to `homeActive`) | modal dismiss |
| Suggested prompt row | tap | sends it; answer appears | none (answer fades in, suggestion 200 ms) |
| Composer | focus / type / send | pinned composer on keyboard; sends message | keyboard |
| Mic | tap | dictation into the field (no send) | system |
| Remind {name} chip | tap | Remind sheet (08-07) over the chat | sheet |
| Draft **Save** | tap | card → Saved, expense created | 250 ms ease-out in place |
| Draft **Edit** | tap | Add expense form, prefilled | full-screen modal |
| Draft **View** (Saved) | tap | Expense detail (09-03) | push |

### 3.6 Assistant behaviour (deterministic, on-device; flow.md FULL APP SCOPE)
The assistant never calls a server. It answers the four suggested prompts and a few simple question shapes **from live data**, and drafts expenses from simple phrases. Everything it shows is recomputed at the time of asking.
1. **Suggested prompts** (verbatim, in this order): "Who owes me money?" · "How much did I spend on food this month?" · "When is Goa Trip due?" · "Draft a reminder for Rohan". They are fixed in Figma. Proposal: keep these four exactly for the demo account; for a user without Goa Trip/Rohan, substitute the soonest-due group ("When is {group} due?") and the most overdue debtor ("Draft a reminder for {name}"); hide a prompt that has no data.
2. **"Who owes me money?"** (drawn):
   - Text template: "{n} people owe you {total}: {parts}." where n and total match Home's "You're owed" card (+₹2,900 from 4 people). Parts: overdue people first, each "{name} {amount} (overdue since {d MMM})"; then people owed for the **same expense with the same amount** grouped as "{A}, {B} and {C} {amount} each for {expense}"; other people "{name} {amount}". Join the parts with ", " and put "and " before the last part. `{expense}` = "tonight’s dinner" for an expense dated today in category Food whose title starts with "Dinner" (demo: "Dinner at Olive Garden"); otherwise "today’s {title lowercased}" when dated today, else the title ("Movie tickets"). With exactly 1 person: "{name} owes you {amount}…". With none: "No one owes you anything right now." and no card.
   - Card: one Row / Person Compact/Value per person, same order, amount = what they owe you, Overdue badge ("Overdue {n} days") when overdue.
   - Chips: "Remind {name}" (bell) for **each overdue person** (max 2 chips), opening the Remind sheet for that person.
3. **"How much did I spend on food this month?"** (not drawn; proposal): text "You spent {amount} on food in {Month} — {pct}% of your {total} share." (Insights numbers, §2.6; demo: "You spent ₹3,850 on food in September — 17% of your ₹23,300 share."). Card: a single Row / Bar (Food) inside a `bg/card` card (use Leading=Avatar white-circle styling). Chip: "See Insights" (chart icon) → Activity › Insights. Generalises to "…spend on {category}…".
4. **"When is Goa Trip due?"** (not drawn; proposal): "Your Goa Trip share of {amount} is due {EEE d MMM}." (demo: Home shows Goa Trip · Your share · ₹1,400 · Due Fri → "Your Goa Trip share of ₹1,400 is due Fri 2 Oct."). If nothing is due: "Nothing is due in Goa Trip." Chips: "Settle up" (→ Settle Up flow for that group) and "Open Goa Trip" (→ group detail).
5. **"Draft a reminder for Rohan"** (not drawn; proposal): "Here’s a reminder for Rohan. Nothing is sent until you tap Send." followed by the drafted message as an assistant message, then chip "Remind Rohan" → the Remind sheet (08-07) prefilled with the same text. Use the Remind sheet's own template (its Figma sample: "Hi Rohan! Just a gentle reminder about ₹800 for the movie tickets on 20 Sep. You can pay me on UPI at arjun@okaxis. Thanks." — see the components spec, Control / Text Area) so both places say the same thing.
6. **Drafting an expense** (drawn): phrases like "Add ₹600 for a cab, split with Esha and Dev". Parser (case-insensitive):
   - amount: first money token (`₹600`, `600`, `rs 600`, `600 rupees`, decimals allowed);
   - what: the words after "for (a|an|the)?" up to the next comma/"split"/"with" → title = that noun phrase with the first letter capitalised ("Cab");
   - people: names after "split with"/"with" separated by ",", "and", "&", matched against the user's friends (case-insensitive first names); unknown names → the draft's Split line says "Split equally with {known names}" and the assistant adds "I couldn’t find {name}." (proposal);
   - category by keyword (the caption: "Travel, guessed from “cab”"): cab/taxi/uber/ola/auto/metro/train/bus/flight/fuel/petrol/parking → **Travel** (Car icon); dinner/lunch/breakfast/food/coffee/snacks/pizza/biryani/restaurant → Food; rent → Rent (Home); electricity/wifi/wi-fi/internet/gas/water/bill → Bills (Bolt); movie/tickets/concert/game → Fun (Ticket); hotel/stay/hostel/airbnb/villa → Stays (Bed); groceries/shopping → Shopping (Shopping Bag); else **Other** (Tag);
   - paid by = you; date = today; split = equally among you + the people (₹600 ÷ 3 = "₹200 each"; uneven amounts use the same fair leftover-paise rotation as Add expense 06-05, and the Each line then reads "About ₹{x} each" — proposal); group = none unless the phrase says "in {group}".
   - Reply text (verbatim): "Here’s what I’ll add. Nothing is saved until you tap Save."
   - Lines: Paid line "Paid by you · Today"; Split line "Split equally with {names joined with “, ” and “ and ”}"; Each line "{₹each} each".
7. **Fallback** (proposal): anything else → "I can answer questions about your balances and due dates, or add an expense. Try “Who owes me money?”" and show the suggestion card again below it.
8. Conversation persistence (proposal): keep the chat for the session; clear it when the app is relaunched. Nothing is sent anywhere.

### 3.7 Sample data (Ask Paybak, verbatim)
- Start: "Ask Paybak" · "What can I help with, Arjun?" · "Ask about balances and due dates, or add an expense in plain words." · "Try asking" · the four prompts (§3.6.1) · "Paybak only sees your own data." · "Ask or add an expense".
- Answer: "Who owes me money?" · "4 people owe you ₹2,900: Rohan ₹800 (overdue since 27 Sep), and Priya, Esha and Dev ₹700 each for tonight’s dinner." · Rohan ₹800 "Overdue 3 days" · Priya ₹700 · Esha ₹700 · Dev ₹700 · "Remind Rohan".
- Confirm: "Add ₹600 for a cab, split with Esha and Dev" · "Here’s what I’ll add. Nothing is saved until you tap Save." · "Cab" · "₹600" · "Paid by you · Today" · "Split equally with Esha and Dev" · avatars Arjun, Esha, Dev · "₹200 each" · "Save" · "Edit"; Saved state: "Expense added" · "View".
- Consistency: ₹2,900 = Rohan ₹800 (movie tickets, overdue since 27 Sep → "Overdue 3 days" on Wed 30 Sep) + 3 × ₹700 (Dinner at Olive Garden ₹2,800 ÷ 4). Matches Home "+₹2,900 from 4 people".

### 3.8 Test IDs
`screen.ask`, `ask.close`, `ask.prompt.0…3`, `ask.composer`, `ask.mic`, `ask.send`, `ask.message.<n>`, `ask.answerCard`, `ask.chip.remind.<name>`, `ask.draft`, `ask.draft.save`, `ask.draft.edit`, `ask.draft.view`, `ask.draft.saved`.

---

## 4. Scan receipt: `scanCamera`, `scanReview`, `scanAssign`, `scanAddExpense`

**Flow:** Add expense form row **"Add receipt"** (camera icon; screens-add-expense.md) → Camera (full-screen modal over the form) → Check receipt (push) → Assign items (push) → back to the **Add expense** form, prefilled (the scan's end state). Pro gating: §0.1 (free users: the photo is only attached; the Review/Assign steps open the paywall).

### 4.1 Designer notes (verbatim)
- 11-07 (Camera): "Opens from “Add receipt” in Add expense. Free users can still take or upload the photo and attach it. Reading the items (Check receipt, Assign items) is Pro and opens the paywall. Shown as a Pro member."
- 11-08 (Review): "You check what was read before anything is split: tap any value to fix it in place, and the items add up to the ₹2,000 subtotal. If nothing can be read, a Card / Notice “Couldn’t read this receipt” offers Retake and Attach photo only. Shown as a Pro member."
- 11-09 (Assign items): "Tap who had each item. Shared items split evenly (₹240 ÷ 3 = ₹80, ₹270 ÷ 3 = ₹90). Tax and tip (₹300) follow each person’s items, so ₹860, ₹540 and ₹600 become ₹989, ₹621 and ₹690. Shown as a Pro member."
- 11-10 (Add expense): "The scan ends in the normal Add expense form, filled in, so saving works like any other expense and opens its detail with “Expense added”. Changing the split goes back to Assign items. Shown as a Pro member."

### 4.2 `scanCamera` (177:25568)
**Container:** full-screen modal over Add expense; dark (`bg/camera` #2B2B2B) behind a **live camera preview** that fills the screen (aspect-fill). Status bar and home indicator in their light style.
1. **Top bar** (20, 62) 362 × 44, H space-between, centred:
   - **Close** kit glass ✕ button 44 × 44 at (20, 62) (on the dark backdrop it renders as a translucent grey disc with a white ✕). iOS: `.glassEffect` circle with `xmark` 17 pt semibold white; Android: circle #FFFFFF @ 20 % + `close.svg` 24 white.
   - Title "Scan receipt" `Headline` **`text/inverse`** #FFFFFF, centred (153, 73).
   - **Flash** kit glass button 44 × 44 at (338, 62) with SF Symbol bolt (Android `bolt.svg` white). Toggles the torch (off by default; proposal: filled/black-on-white glass when on).
2. **Receipt guides** (51, 170) 300 × 460 (absolute): four corner brackets, each an **L of two 36-pt arms**, stroke **4 pt white** (`icon/inverse`), **round caps**, inner corner radius ≈ 12 (measured on the render), at the guide's corners: top-left arms start (53, 172), top-right (313, 172), bottom-right (313, 592), bottom-left (53, 592) (36 × 36 boxes). Not interactive.
3. Sample content (Figma only): **Art / Receipt Size=Full** at (59.1, 195.3) 257 × 392.4, **rotated −4°** = the paper in view. In the app this is the live camera image; on the iOS Simulator / Android emulator without a camera, show `assets/images/receipt-leopold-cafe@3x.png` in that rect (aspect-fit, −4°) as the simulated preview so the demo flow works (§9.2 #6).
4. **Hint pill** (78, 646) 247 × 36 (centred): H, padding 0 / 16, gap 6, radius full, fill `bg/scrim` #0A0A0A @ 40 %, effect Material/Glass Small; "Fit the whole receipt in the frame" `Subheadline` `text/inverse`.
5. **Upload photo** (20, 760) 127 × 36: H, padding 0 / 12, gap 4, radius full, fill `bg/scrim` @ 40 % + Material/Glass Small; Icon / **Image** 16 (`icon/inverse`, `image.svg`) + "Upload photo" `Footnote` `text/inverse`. → system photo picker (PHPicker / Android Photo Picker), single image.
6. **Shutter** Control / Shutter (§1.3.11) at (163, 740) 76 × 76 (centred, bottom 816). → capture.
- Navigation: ✕ → back to the Add expense form (unchanged). Shutter / Upload photo → **Pro:** `scanReview` (prototype DISSOLVE 300 ms ease-out; in the app push after the read finishes, with a brief "Reading…" progress state — proposal: a small `Footnote` spinner row replacing the hint pill); **free:** attach the photo to the expense and return to the form ("Receipt · Attached") without reading.
- Permissions: camera permission on first open; if denied, show the dark screen with the hint text "Allow camera access in Settings to scan receipts." and keep "Upload photo" (proposal).

### 4.3 `scanReview` (177:26256): "Check receipt"
**Container:** pushed on the camera's modal stack (white). Header Navigation / Push Header Trailing=None, Title "Check receipt" (20, 62) 362 × 44: back chevron (glass) at the left, title centred. Back → Camera (prototype PUSH RIGHT 350 ms ease-in-out).
Content scrolls; the CTA is pinned.
1. **Intro** (20, 106) padding-top 16 → row (20, 122) 362 × 72, H gap 16, centred:
   - Receipt thumb (the captured photo; Figma Art / Receipt Size=Thumb) 56 × 72, radius 10, clipped, aspect-fill. Demo/simulator: `assets/images/receipt-thumb.svg`.
   - Text (92, 137) V gap 2: "We found 6 items." `Headline` `text/primary` ("We found {n} items." / "We found 1 item."); "Tap any value to fix it." `Footnote` `text/secondary`.
2. **Details** (20, 194) padding-top 24 → card (20, 218) 362 × 112 (`bg/card`, radius 20): Row / Setting Trailing=None, Show value:
   - "Merchant" · value "Leopold Cafe" · Icon / Receipt · divider.
   - "Date" · value "Wed 30 Sep · 1:15 pm" (`EEE d MMM · h:mm a`, lowercase am/pm) · Icon / Calendar.
   Tap a row → edit in place (merchant: inline text field; date: the Date sheet from Add expense 06-09). Proposal.
3. **Totals** (20, 330) padding-top 16 → card (20, 346) 362 × 176: Row / Receipt Line ×4 (44 each): "Subtotal" ₹2,000 · "GST 5%" ₹100 · "Tip 10%" ₹200 · **Total** (Style=Total, divider above) "Total" ₹2,300.
4. **Items header** (20, 522) padding-top 24 → Row / Section Header "Items" with action "**Add item**" (Button / Text Secondary) at (20, 546). Add item → appends an editable line "New item" ₹0 in Editing state (proposal).
5. **Items** (20, 578) padding-top 8 → card (20, 586) 362 × 264: Row / Receipt Line ×6 (44 each) at y 586, 630, 674, 718, 762, 806: Chicken biryani ₹430 · Paneer tikka ₹370 · Fish and chips ₹450 · Chocolate brownie ₹240 · Masala fries ₹240 · Fresh lime soda ×3 ₹270. The card continues under the fade/CTA and scrolls.
6. **Bottom fade** (0, 764) 402 × 110 (absolute, above content, below the CTA): white 0 % at 0 → white 100 % at 20 % (y 786) → 100 % at the bottom.
7. **"Looks right"** Button / Primary Large (20, 788) 362 × 52, pinned. → `scanAssign` (PUSH LEFT 350 ms ease-in-out).
- **Editing** any value: Row / Receipt Line State=Editing (inline white field, number pad for amounts, text keyboard for labels). The keyboard pushes the CTA up (it rides 12 pt above the keyboard, like signIn) and the edited row scrolls into view.
- **Validation (proposal consistent with the caption):** items must add up to the Subtotal. When they don't, show a `Footnote` `text/destructive` line under the Items card: "Items add up to {Σ}, the subtotal is {subtotal}." and disable "Looks right"; Total = Subtotal + tax lines + tip lines, recomputed on every edit (the Total row itself is not editable).
- **Nothing readable** (caption): replace sections 2–5 with a Card / Notice (Layout=Leading, icon Receipt/Alert, title "Couldn’t read this receipt", Actions=Two) offering **"Retake"** (→ Camera) and **"Attach photo"** (attach and return to the form) only. Body copy isn't in Figma; proposal: "Try again in better light, or attach the photo to the expense as it is."

### 4.4 `scanAssign` (177:26997): "Assign items"
Header Navigation / Push Header Trailing=None, "Assign items" (back → Review, PUSH RIGHT 350 ms).
1. **Helper** (20, 106) padding-top 8: "Tap who had each item. Tax and tip are split in proportion." `Footnote` `text/secondary` (20, 114).
2. **Items** (20, 132) padding-top 12, V gap 0: Row / Assign Item (§1.3.9) per receipt item, people = **the people on the expense** (the Add expense "With you and" people plus you; demo: You, Esha, Dev):
   | Row y | Height | Item | Price | Selected chips | Caption |
   |---|---|---|---|---|---|
   | 144 | 90 | Chicken biryani | ₹430 | Dev | – |
   | 234 | 90 | Paneer tikka | ₹370 | Esha | – |
   | 324 | 90 | Fish and chips | ₹450 | You | – |
   | 414 | 90 | Chocolate brownie | ₹240 | You | – |
   | 504 | 108 | Masala fries | ₹240 | You, Esha, Dev | "Shared by 3 · ₹80 each" |
   | 612 | 108 | Fresh lime soda ×3 | ₹270 | You, Esha, Dev | "Shared by 3 · ₹90 each" (no divider) |
   The list scrolls under the fade and the totals card.
3. **Bottom fade** (0, 652) 402 × 222: white 0 % → 100 % at 10 % (y 674) → 100 %.
4. **Person totals** Card / Person Totals (§1.3.10) pinned at (20, 676) 362 × 96: check-circle 16 + "All items assigned" + "Includes GST and tip"; You ₹989 · Esha ₹621 · Dev ₹690 (avatars Arjun, Esha, Dev).
   - Unassigned items (proposal): the status row shows Icon / Alert 16 (`icon/secondary`) + "{n} items left" and the amounts show only assigned items; **Continue is disabled** until every item has at least one person.
5. **Continue** Button / Primary Large (20, 788) pinned → `scanAddExpense` (PUSH LEFT 350 ms ease-in-out). In the app: dismiss the scan screens and land on the Add expense form the user started from, now prefilled (the frame shows that form; Figma animates it as a push-left). The form's Split row ("Itemized · 3 people") reopens Assign items with the current assignment (Figma: push-right = back).
- Default state when arriving (proposal): nothing selected (the Figma frame shows the finished assignment); the demo/debug start screen seeds the drawn assignment.

### 4.5 `scanAddExpense` (177:28338): the Add expense form, prefilled
This is **the normal Add expense screen** (screens-add-expense.md, 06-02 layout), not a new screen. Only the prefilled values differ:
- Header Navigation / Modal Header **Action=Enabled**: ✕, "Add expense", **Save** (black pill, enabled).
- Control / Amount Display State=Filled (20, 122) 362 × 138: chips "INR" and "Today" (131, 126); amount "**₹2,300**" `Amount/Display` centred (106, 174); helper "**From receipt · 6 items**" `Footnote` `text/secondary` (136, 238).
- People strip (20, 276): "With you and" `Subheadline` `text/secondary` + chips **Esha**, **Dev** (Leading=Avatar, unselected) + "**Add**" (Leading=Icon Plus).
- Title field (20, 328) 362 × 52, Filled: "**Lunch at Leopold Cafe**".
- Form card (20, 396) 362 × 540, `bg/card`, radius 20 — Row / Setting (Trailing=Chevron) rows:
  | y | Title | Value | Icon | Note |
  |---|---|---|---|---|
  | 396 | Category | Food | Food | |
  | 452 | Paid by | You | Wallet | |
  | 508 | Split | **Itemized · 3 people** | Split | tap → back to Assign items (prototype PUSH RIGHT 350 ms) |
  | 564 | Group | No group | Groups | |
  | 620 | Due | None | Calendar | followed by the due quick chips row (20, 676) 362 × 92: "Tomorrow", "This weekend", "Next week", "Pick date" (Category Chips, wrap, padding 0/16/12/52, gap 8) + leading-inset divider |
  | 768 | Repeat | Never | Repeat | Pro badge rules per 06-02 |
  | 824 | Receipt | **Attached** | Camera | a 24.9 × 32 receipt thumb (radius 4.4) sits at (232, 836) just left of the value |
  | 880 | Notes | Optional | Note | no divider |
- ✕ (prototype: link to Home) → discard confirmation per screens-add-expense.md; Save → the new expense's detail with the "Expense added" toast (09-03), exactly like any expense.
- The "Receipt" row (not drawn as tappable here): proposal → a full-screen photo viewer with "Retake" and "Remove".

### 4.6 Scan math and rules
1. **Reading** (flow.md: real where cheap): iOS: Vision `VNRecognizeTextRequest` (accurate) on the photo; Android: ML Kit Text Recognition v2 (on-device) if the dependency is acceptable, otherwise a **demo parse** behind the same interface that returns the Leopold Cafe receipt for the sample image (and for any photo on the simulator/emulator). Parse: merchant = first prominent line; date/time; item lines "label … ₹amount"; lines named Subtotal / GST x% / CGST / SGST / Service charge / Tip x% / Total.
2. **Check**: Σ items must equal Subtotal (₹430 + ₹370 + ₹450 + ₹240 + ₹240 + ₹270 = ₹2,000). Total = Subtotal + tax + tip = ₹2,000 + ₹100 + ₹200 = ₹2,300.
3. **Assign**: each item's price is split evenly among the people selected for it: ₹240 ÷ 3 = ₹80, ₹270 ÷ 3 = ₹90. When it doesn't divide evenly, split to the paisa and rotate the leftover paise fairly (same rule as Add expense 06-05).
4. **Tax and tip follow each person's items** (proportional allocation): person total = person's item subtotal × (Total ÷ Subtotal). Demo: You ₹450 + ₹240 + ₹80 + ₹90 = **₹860**; Esha ₹370 + ₹80 + ₹90 = **₹540**; Dev ₹430 + ₹80 + ₹90 = **₹600** (Σ ₹2,000). Factor 2,300 ÷ 2,000 = 1.15 → **₹989, ₹621, ₹690** (Σ ₹2,300). Rounding: compute in paise, round each to the paisa, then give any leftover paise to the people with the largest remainders so the parts add up to the Total exactly. Display whole rupees when the paise are 0.
5. **Result → Add expense**: amount = Total; title = "{meal} at {merchant}" where meal comes from the receipt time for Food receipts (05:00–10:59 Breakfast, 11:00–15:59 **Lunch**, 16:00–18:59 Snacks, 19:00–04:59 Dinner) — proposal matching "Lunch at Leopold Cafe" at 1:15 pm; for other categories just the merchant name. Category = Food for restaurants/cafes (merchant keywords cafe, restaurant, dhaba, bar, bistro, kitchen, or ≥ 50 % food item keywords), else Other. Date = the receipt date ("Today" chip when it is today). Paid by = you. Split = **Itemized** with the per-person totals (value "Itemized · {n} people"). Receipt = Attached. Helper = "From receipt · {n} items".
6. Free users: steps 2–4 are skipped; the photo is attached and the amount is not filled.

### 4.7 Sample data (Scan receipt, verbatim)
- Camera: "Scan receipt" · "Fit the whole receipt in the frame" · "Upload photo".
- The receipt (art, 86:667): "Leopold Cafe" · "Colaba Causeway, Mumbai" · "Wed 30 Sep 2026 · 1:15 pm" · the six items with prices · "Subtotal ₹2,000" · "GST 5% ₹100" · "Tip 10% ₹200" · "Total ₹2,300" · "Thank you. Visit again."
- Review: "Check receipt" · "We found 6 items." · "Tap any value to fix it." · "Merchant" "Leopold Cafe" · "Date" "Wed 30 Sep · 1:15 pm" · "Subtotal" ₹2,000 · "GST 5%" ₹100 · "Tip 10%" ₹200 · "Total" ₹2,300 · "Items" · "Add item" · Chicken biryani ₹430 · Paneer tikka ₹370 · Fish and chips ₹450 · Chocolate brownie ₹240 · Masala fries ₹240 · Fresh lime soda ×3 ₹270 · "Looks right".
- Assign: "Assign items" · "Tap who had each item. Tax and tip are split in proportion." · chips "You", "Esha", "Dev" · "Shared by 3 · ₹80 each" · "Shared by 3 · ₹90 each" · "All items assigned" · "Includes GST and tip" · You ₹989 · Esha ₹621 · Dev ₹690 · "Continue".
- Add expense: "Add expense" · "Save" · "INR" · "Today" · "₹2,300" · "From receipt · 6 items" · "With you and" · Esha · Dev · "Add" · "Lunch at Leopold Cafe" · Category "Food" · Paid by "You" · Split "Itemized · 3 people" · Group "No group" · Due "None" · "Tomorrow" · "This weekend" · "Next week" · "Pick date" · Repeat "Never" · Receipt "Attached" · Notes "Optional".

### 4.8 Test IDs
`screen.scanCamera`, `scan.close`, `scan.flash`, `scan.shutter`, `scan.upload`; `screen.scanReview`, `scanReview.back`, `scanReview.merchant`, `scanReview.date`, `scanReview.subtotal`, `scanReview.tax.<n>`, `scanReview.tip`, `scanReview.total`, `scanReview.item.<n>`, `scanReview.addItem`, `scanReview.confirm`; `screen.scanAssign`, `scanAssign.back`, `scanAssign.item.<n>.person.<you|esha|dev>`, `scanAssign.total.<person>`, `scanAssign.status`, `scanAssign.continue`. The prefilled form uses the Add expense IDs.

---

## 5. Recurring: `recurringFlat302`, `recurringRepeat`, `recurringEnterAmount`

### 5.1 Designer notes (verbatim)
- 11-11 (Flat 302 list): "Fixed rules add the expense on schedule. Variable rules create a draft that leaves balances alone until you enter the amount, and Cooking gas repeats on the 28th, so its next date is Wed 28 Oct. Shown as a Pro member."
- 11-12 (Repeat sheet): "For Pro members, the Repeat row in Add expense opens this sheet; free users get the paywall from that row. Turning on “Amount changes each time” makes the rule create drafts instead of expenses. Shown as a Pro member."
- 11-13 (Enter amount): "The September draft Paybak created on Mon 28 Sep. The split follows the rule (equally between the 3 roommates), and nothing counts until you enter an amount. Shown as a Pro member."

### 5.2 `recurringFlat302` (177:29224): "Recurring" for Flat 302
**Container:** pushed screen (white). Entry point is not on this page (prototype back → Activity › Timeline 167:14361). Proposal: pushed from the Flat 302 group detail / group settings "Recurring" row (`screens-groups.md`), and from a draft notification. Pro only (free users get the paywall from the entry row).
1. **Header** Navigation / Push Header **Trailing=Text**, Title "Recurring", Action "**Add**" (20, 62) 362 × 44: glass back chevron left, centred title, glass pill "Add" right. Add → a new Add expense form for this group with Repeat preset to Monthly (proposal) — i.e. the same form as `recurringRepeat` with Group = Flat 302.
2. **Intro** (20, 106) padding-top 8: "Paybak adds these to Flat 302 on schedule." `Footnote` `text/secondary` (20, 114). Runtime: "Paybak adds these to {group} on schedule."
3. **Needs your amount** (20, 132) padding-top 24 → Row / Section Header "Needs your amount" (20, 156). Section hidden when there are no drafts.
4. **Draft** (20, 188) padding-top 8 → **Cooking gas draft** = Row / Attention State=Due soon, customised (20, 196) 362 × 94: fill `bg/card`, radius 20, **padding 12 all sides, gap 8**, centred:
   - avatar Avatar / Circle 40 Type=Icon **Flame** (`flame.svg`) at (32, 223) — the circle is white on the card.
   - text column (80, 208) 153 wide, V gap 6: one text node with two styled lines: "Cooking gas" (`Headline` `text/primary`) + line break + "September draft · 28 Sep" (`Footnote` `text/secondary`) (150 × 40); then Badge / Pill **On Card** "Draft" (80, 254) 51 × 24.
   - trailing (241, 225): Button / On Card Small "**Enter amount**" 129 × 36 (white pill). → `recurringEnterAmount` (MOVE_IN from bottom, EASE_OUT 300 ms; hotspot 241,225 129×36).
   - Row text runtime: "{rule title}\n{Month} draft · {d MMM}".
5. **Draft note** (20, 290) padding-top 8: "Drafts don’t affect balances." `Footnote` `text/secondary` (20, 298).
6. **Rules** (20, 316) padding-top 24 → Row / Section Header "Rules" (20, 340).
7. **Rules card** (20, 380) 362 × 308: `bg/card`, radius 20, **padding 4 / 16 / 4 / 16**, V gap 0; Row / Activity Type=Expense, Surface=On Card, Show detail line, Show amount, **Show date=false**, 330 × 100 each:
   | y | Icon | Title | Subtitle (2 lines) | Detail | Amount | Divider |
   |---|---|---|---|---|---|---|
   | 384 | Home | Rent | "Monthly on the 1st\nPaid by you" | "Next Thu 1 Oct" | ₹36,000 | yes |
   | 484 | Wi-Fi (`wi-fi.svg`) | Wi-Fi | "Monthly on the 5th\nPaid by Kabir" | "Next Mon 5 Oct" | ₹1,200 | yes |
   | 584 | Flame | Cooking gas | "Monthly on the 28th\nPaid by you" | "Next Wed 28 Oct" | **Varies** | no |
   Row geometry (component-relative, from the Rent dump): padding 8 / 0, gap 12, centred; tile 40 circle `bg/primary` (white on card) at (0, 30) with the 20 icon; text column (52, 8) 196 wide, V gap 2: title `Headline` `text/primary` (22), subtitle `Subheadline` `text/secondary` **2 lines** (40), detail `Footnote` `text/tertiary` (18); amount column right (260, 39): `Amount/Medium` `text/primary` (Direction=In → black, no sign; "Varies" uses the same style); divider Inset=Leading (from x 52) at the row bottom.
   Tap a rule → edit it: the Add expense form in "edit rule" mode with its Repeat sheet (prototype: Cooking gas → `recurringRepeat`, DISSOLVE 300 ms ease-out). Proposal: swipe/long-press to delete a rule ("Stop repeating").
8. Scroll: the column scrolls if it outgrows the screen (it doesn't in Figma).
- Navigation: back → previous screen (prototype: Activity › Timeline); Add → new recurring expense form; Enter amount → `recurringEnterAmount`; rule row → edit (the form + Repeat sheet).

### 5.3 `recurringRepeat` (177:30291): Add expense with the Repeat sheet
Background = **the Add expense form** (screens-add-expense.md) for the Cooking gas rule, drawn under a **scrim** (0, 0) 402 × 874 `bg/scrim` #0A0A0A @ 40 %:
- Modal Header **Action=Disabled** "Add expense" (Save disabled: no amount yet); Amount Display State=**Empty** "₹0" with chips "INR", "Today", no helper; People strip "With you and" **Meera** (`avatar-7`), **Kabir** (`avatar-6`), "Add"; title "Cooking gas"; form rows: Category **Bills** (Bolt) · Paid by **You** (Wallet) · Split **Equally · 3 people** · Group **Flat 302** · Due **None** + quick chips · Repeat **Monthly** · **Add receipt** (Camera, no value) · Notes **Optional**.
  - A variable rule has no amount: when "Amount changes each time" is on, the form's Save is enabled with ₹0 (proposal: Save creates the rule; the amount field shows the helper "Paybak asks for the amount each time." — not in Figma, §9.2 #7). In the frame Save is disabled because the sheet is open.

**Repeat sheet** Sheet / Container Detent=Medium, Title "Repeat", Show close (§ components-home.md §15) at (8, 398) 386 × 468 (bottom 8 from the screen edge), radius 40, `bg/primary`, padding 8 / 16 / 28 / 16, gap 8. Sheet-relative → frame: x + 8, y + 398.
1. grabber 60 × 4 at (163, 8); header (16, 20) 354 × 50: "Repeat" `Title/3` + kit glass ✕ 50 × 50 at (320, 20).
2. Content slot (16, 78) 354 × 362, V gap 0:
   - **Frequency chips** 354 × 80, H **wrap**, gap 8 (row gap 8): Control / Category Chip Leading=None: "Never" 74 × 36 (16, 78), "Weekly" 84 (98, 78), "**Monthly**" 91 (190, 78) **Selected** (black), "Yearly" 75 (289, 78), "Custom" 89 (16, 122). Single choice.
   - **Settings** padding-top 16 → card (16, 174) 354 × 112, `bg/card`, radius 20:
     - Row / Setting Trailing=Chevron: "Day of month" · value "**28th**" · Icon / Calendar · divider (302 wide). Tap → day picker (1st…28th, plus "Last day"; proposal: a wheel picker in a nested sheet). For Weekly this row becomes "Day of week" (e.g. "Monday"); Yearly "Date" ("28 Sep"); Never hides the card; Custom not designed (proposal: "Every N days/weeks/months" stepper).
     - Row / Setting **Trailing=Toggle**: "**Amount changes each time**" · Icon / Wallet · toggle **On** (kit switch 64 × 28, white knob, On track `bg/inverse` #0A0A0A). No value.
   - **Helper** padding-top 12: "Paybak adds a draft on the 28th and asks you for the amount." `Footnote` `text/secondary` (16, 298), 2 lines. Toggle off → "Paybak adds this expense on the 28th of every month." (proposal; not in Figma).
   - **Next draft** padding-top 12: Icon / Calendar 16 (`icon/secondary`, measured on the render) + "Next draft: Wed 28 Oct" `Footnote` `text/primary`, gap 6 (16, 346). Toggle off → "Next: Wed 28 Oct" (proposal).
   - **Actions** padding-top 24: **Done** Button / Primary Large (16, 388) 354 × 52 (frame (24, 786)).
- ✕ / Done / scrim tap → close the sheet and write the choice into the form's Repeat row ("Monthly"; "Never" hides the rule). Prototype: ✕ and Done → `recurringFlat302` (DISSOLVE 300 ms) as a shortcut for "save the rule and return".
- Motion: the sheet slides up over the scrim like the Add sheet (MOVE_IN 300 ms ease-out) — reuse the Sheet / Container behaviour.

### 5.4 `recurringEnterAmount` (177:30957): "Cooking gas" draft → expense
**Container:** full-screen modal (slides up from the bottom, MOVE_IN 300 ms ease-out); ✕ → back to `recurringFlat302` (MOVE_OUT to the bottom, EASE_IN_AND_OUT 300 ms).
1. Header Navigation / Modal Header **Action=Disabled**: ✕, title "**Cooking gas**", "**Add**" pill disabled until the amount is > 0.
2. **Draft meta** (20, 106) padding-top 8 → row (20, 114) H gap 8, centred: Badge / Pill **Muted** "Draft" 51 × 24 + "Flat 302 · September" `Footnote` `text/secondary`.
3. **Amount** (20, 138) padding-top 16 → Control / Amount Display State=**Empty** (20, 154) 362 × 138, **Show date chip=false**: chip "INR" (172.5, 158) 57 × 36; value "₹0" `Amount/Display` `text/tertiary` centred (163, 206) with the **caret** 2 × 56 `text/primary` at (237, 210) (focused); helper "Drafts don’t count until you add the amount." `Footnote` `text/secondary` (67, 270).
4. **Details** (20, 292) padding-top 16 → card (20, 308) 362 × 168, `bg/card`, radius 20: Row / Setting **Trailing=None** (read-only here): "Paid by" "You" (Wallet) · "Split" "Equally · 3 people" (Split) · "Date" "Mon 28 Sep" (Calendar, no divider).
5. **Keyboard**: the amount is focused on open. Figma draws the kit **Number Pad** (0, 566) 402 × 308 with a "." added on the empty bottom-left key (8, 761); i.e. a decimal pad. iOS `.keyboardType(.decimalPad)`; Android `KeyboardType.Decimal`. The content above doesn't move (it fits above y 566).
- **Add** → the draft becomes a real expense in Flat 302 dated **Mon 28 Sep**, paid by you, split equally between the 3 roommates (Arjun, Meera, Kabir), category Bills, title "Cooking gas"; balances update; the "Needs your amount" section loses the row. Proposal: dismiss back to the Recurring list with the toast "Expense added" (Overlay / Toast).

### 5.5 Recurring rules
1. A **rule** = a template expense (title, category, group, people, paid by, split, currency, amount or *variable*) + a **schedule** (Never / Weekly / Monthly / Yearly / Custom) + an anchor (day of month, weekday or date) + the start date. Rules belong to a group (or to a set of friends). Creating one: the **Repeat** row in Add expense (Pro; free → paywall).
2. **Fixed rule** ("Amount changes each time" off): on each occurrence date Paybak **adds the expense automatically** (a normal expense, dated the occurrence, counted in balances and Insights), and notifies the group (proposal). Rent (₹36,000, monthly on the 1st, paid by you) and Wi-Fi (₹1,200, monthly on the 5th, paid by Kabir) are fixed.
3. **Variable rule** (toggle on): on each occurrence Paybak creates a **draft** instead ("{Month} draft · {d MMM}", e.g. "September draft · 28 Sep" created **Mon 28 Sep**). A draft **leaves balances alone** ("Drafts don’t affect balances.", "Drafts don’t count until you add the amount.") and is excluded from Insights until someone enters the amount. The rule's list amount shows "**Varies**". Proposal: send a local notification on the draft date "Cooking gas: add September’s amount" that opens `recurringEnterAmount`.
4. **Enter amount** turns the draft into a normal expense dated the occurrence date (not today), split **as the rule says** ("The split follows the rule (equally between the 3 roommates)").
5. **Next date** = the first occurrence **after today** (today = Wed 30 Sep 2026 in the demo): Rent "Monthly on the 1st" → **Thu 1 Oct**; Wi-Fi "on the 5th" → **Mon 5 Oct**; Cooking gas "on the 28th" → 28 Sep has passed (its September draft exists) → **Wed 28 Oct**. Format "Next {EEE d MMM}". Day-of-month larger than the month's length → the month's last day (proposal). Weekly: next same weekday after today; Yearly: next same day-month.
6. Occurrences are generated when the app launches/foregrounds (no backend): for every rule, create all occurrences whose date ≤ today and that don't exist yet (catch-up), in date order.
7. Subtitle format: "{Frequency} on the {ordinal}\nPaid by {you|name}" (Weekly: "Weekly on {Weekday}s"; Yearly: "Yearly on {d MMM}" — proposals). Ordinals: 1st, 2nd, 3rd, 4th…21st, 22nd, 23rd, 28th, 31st.
8. Sheet copy: helper "Paybak adds a draft on the {ordinal} and asks you for the amount."; "Next draft: {EEE d MMM}".

### 5.6 Sample data (Recurring, verbatim)
- List: "Recurring" · "Add" · "Paybak adds these to Flat 302 on schedule." · "Needs your amount" · "Cooking gas" / "September draft · 28 Sep" / "Draft" / "Enter amount" · "Drafts don’t affect balances." · "Rules" · Rent · "Monthly on the 1st" "Paid by you" · "Next Thu 1 Oct" · ₹36,000 · Wi-Fi · "Monthly on the 5th" "Paid by Kabir" · "Next Mon 5 Oct" · ₹1,200 · Cooking gas · "Monthly on the 28th" "Paid by you" · "Next Wed 28 Oct" · "Varies".
- Repeat: background form "Add expense" · "Save" · "INR" · "Today" · "₹0" · "With you and" · Meera · Kabir · "Add" · "Cooking gas" · Category "Bills" · Paid by "You" · Split "Equally · 3 people" · Group "Flat 302" · Due "None" · quick chips · Repeat "Monthly" · "Add receipt" · Notes "Optional"; sheet "Repeat" · "Never" · "Weekly" · "Monthly" · "Yearly" · "Custom" · "Day of month" "28th" · "Amount changes each time" · "Paybak adds a draft on the 28th and asks you for the amount." · "Next draft: Wed 28 Oct" · "Done".
- Enter amount: "Cooking gas" · "Add" · "Draft" · "Flat 302 · September" · "INR" · "₹0" · "Drafts don’t count until you add the amount." · "Paid by" "You" · "Split" "Equally · 3 people" · "Date" "Mon 28 Sep".
- Flat 302 roommates: Arjun (you), Meera, Kabir.

### 5.7 Test IDs
`screen.recurring`, `recurring.back`, `recurring.add`, `recurring.draft.<ruleId>`, `recurring.draft.<ruleId>.enterAmount`, `recurring.rule.<ruleId>`; `repeatSheet`, `repeatSheet.close`, `repeatSheet.freq.never|weekly|monthly|yearly|custom`, `repeatSheet.dayOfMonth`, `repeatSheet.variable`, `repeatSheet.nextDraft`, `repeatSheet.done`; `screen.enterAmount`, `enterAmount.close`, `enterAmount.add`, `enterAmount.amount`.

---

## 6. Sample-data summary for the demo dataset (all "now" = Wed 30 Sep 2026)
| Area | Must come out of real calculations |
|---|---|
| Insights, September 2026 | total ₹23,300; categories Rent 12,000 · Food 3,850 · Stays 3,600 · Fun 1,800 · Travel 1,200 · Bills 850; groups Flat 302 12,850 · Goa Trip 7,900 · Without a group 2,550; Aug ₹22,200 (→ "Up 5% from August"); chart Apr 18,400 · May 21,950 · Jun 19,600 · Jul 20,600 · Aug 22,200 (proposed values reproducing the drawn heights) |
| Lent vs borrowed since April | Lent ₹4,500 (Kabir · Bike service, Paid back), Borrowed ₹0 |
| Ask Paybak | owed ₹2,900 from 4 people: Rohan ₹800 overdue since 27 Sep (movie tickets), Priya/Esha/Dev ₹700 each (Dinner at Olive Garden ₹2,800 today, evening) |
| Flat 302 rules | Rent ₹36,000 monthly 1st paid by you; Wi-Fi ₹1,200 monthly 5th paid by Kabir; Cooking gas variable monthly 28th paid by you; September Cooking gas draft created Mon 28 Sep; members Arjun, Meera, Kabir; split equally |
| Scan demo receipt | Leopold Cafe, Colaba Causeway, Mumbai, Wed 30 Sep 2026 1:15 pm, 6 items, GST 5 %, Tip 10 %, Total ₹2,300; people You, Esha, Dev (not saved in the dataset: the scan is a flow) |

## 7. Art and Rive
- **No frame on this page uses Rive art.** None of the six `.riv` illustrations (onboarding people, get-started trio, notifications person + bell, all-set trio + check, first-day lying character, all-square meditating character) appears here; there are no Open Peeps/Open Doodles scene illustrations on page 11.
- People are **Peep heads** in avatars (`assets/avatars/avatar-1…7`).
- The only artwork is **Art / Receipt** (vector, 86:730): exported as `assets/images/receipt-leopold-cafe@3x.png` (Size=Full) + `assets/images/receipt-paper.svg` (its paper shape); Size=Thumb = existing `assets/images/receipt-thumb.svg`. In the real app the receipt image is the user's photo; these files are the simulator/demo stand-ins (camera preview, Review thumb, Add expense "Attached" thumb).
- The Camera's corner guides, shutter and hint pill are drawn natively (no assets).

## 8. Assets
Added for this spec (none overwritten):
| File | Source | Size | Use |
|---|---|---|---|
| `assets/images/receipt-leopold-cafe@3x.png` | Art / Receipt Size=Full (86:667), `download_assets` PNG @3x | 900 × 1374 px (300 × 458 pt) | Simulated camera preview on the simulator/emulator; demo "captured photo" for the Leopold Cafe flow |
| `assets/images/receipt-paper.svg` | the paper vector inside 86:667 (SVG) | 300 × 458 | Optional: rebuild the receipt natively (paper outline #EBEBEB 2 pt, white fill, zig-zag bottom) |

Reused (already present): `assets/images/receipt-thumb.svg` (Art / Receipt Size=Thumb, 56 × 72; exported earlier for another spec); icons `restore`, `chevron-left`, `chevron-right`, `home`, `food`, `bed`, `ticket`, `car`, `bolt`, `plane`, `people`, `groups`, `plus`, `activity`, `profile`, `lock`, `sparkles`, `calendar`, `bell`, `mic`, `arrow-up` (composer send, Typing state), `image`, `receipt`, `check-circle`, `wallet`, `split`, `repeat`, `camera`, `note`, `flame`, `wi-fi`, `close` (Android ✕), `alert` (proposed unassigned status); avatars 1–7. **No new icons** were needed (all 30 icons used on the 13 frames exist in `assets/icons/`).
Kit SF Symbols used by Figma: `xmark` (✕), `bolt` (flash), `delete.left` (keypad). Android equivalents: `close.svg`, `bolt.svg`, the system keyboard.
References: `ref/insightsSeptember.png`, `ref/insightsScrolled.png`, `ref/insightsLocked.png`, `ref/askStart.png`, `ref/askAnswer.png`, `ref/askConfirm.png`, `ref/scanCamera.png`, `ref/scanReview.png`, `ref/scanAssign.png`, `ref/scanAddExpense.png`, `ref/recurringFlat302.png`, `ref/recurringRepeat.png`, `ref/recurringEnterAmount.png` — all 804 × 1748 (2×), cropped from 2× `download_assets` exports of each section (frames at section offset +40 pt).

## 9. Open questions and gaps
### 9.1 Not done because of the Figma rate limit
- `get_design_context` (React/Tailwind reference + inline screenshot) was **not** fetched for the 13 frames. The Plugin-API node trees used instead hold the same numbers (and more: tokens, reactions, component properties).
- The 2× refs come from section exports, not per-frame `get_screenshot` (which returns 1× for these frame sizes). They were checked for alignment (frame edges vs the section background).

### 9.2 Decisions for product / the editor
1. **Is Ask Paybak Pro-only?** Not stated; default here = Pro (§0.1). Check the paywall (12-01) feature list.
2. **Insights month range:** how far back can the user go? Proposal: to the month of the first expense.
3. **Friends breakdown rule** (§2.6 #8): even split of each expense share across the other people (keeps 100 %). Alternative: per friend, the user's share of expenses shared with that friend (percentages then exceed 100 % in total).
4. **Assistant answers other than "Who owes me money?"** are proposals (§3.6 #3–5, #7).
5. **Recurring list entry point** (group detail row?) and the "Add" action are not designed on this page.
6. **Camera on the simulator/emulator:** proposal = simulated preview with the Leopold receipt art, and the demo parse returns the Leopold data.
7. **Variable rule with ₹0 in Add expense:** Save rule enabled at ₹0 when the toggle is on (not drawn).
8. Unassigned items state on Assign items, the "nothing readable" notice body text, Weekly/Yearly/Custom repeat details, Insights empty states: proposals in §2.6, §4.3–4.4, §5.3.
9. The Figma prototype links some back/close buttons to unrelated frames (Ask ✕ and Scan Add-expense ✕ → Home, Recurring back → Activity timeline). The app should dismiss/pop to the actual previous screen.
