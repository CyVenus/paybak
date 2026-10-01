# Spec digest: reports from the full-app spec pass (pages 02–12)

Each block is one spec's report from the spec-writing pass (30 Sep–1 Oct 2026), kept for its findings and open issues: the files written, the screens (screenId → Figma node → purpose), a summary and the open issues. Many issues were settled later in `app-architecture.md` §8 and in the specs themselves; where they disagree, the specs win. The Figma MCP was heavily rate-limited during this pass (Education plan), which explains the gaps in `get_design_context` output noted below. The per-frame node dumps the reports mention weren't kept; the same node data is in `.figma-cache/` (regenerate with `tools/fetch_figma.py`).

## spec:components-v2
**Files:** components-app.md, assets/icons/INDEX-v2.md, assets/images/INDEX-v2.md, assets/images/art-receipt-full.svg, assets/images/art-receipt-full@3x.png, assets/images/art-receipt-thumb.svg (plus the node dumps used while writing, not kept)
**Screens:**
- PBCategoryChip → 64:4185 Control / Category Chip → filter/people chip (None/Icon/Avatar leading, selected = black)
- PBSettingRow → 97:996 Row / Setting → settings list row in #F5F5F5 groups (Chevron/Toggle/Stepper/Check/Unchecked/None, Destructive tone)
- PBPushHeader → 97:1082 Navigation / Push Header → pushed-screen header: glass back, centred title, Text/Icon/Wide Text/None trailing
- PBAlert → 102:1115 Overlay / Alert → 300-wide iOS 27 alert card, Destructive or Primary action pair
- PBModalHeader → 115:886 Navigation / Modal Header → full-screen modal toolbar: kit xmark left, title, Save pill Enabled/Disabled/None
- PBTextArea → 115:9936 Control / Text Area → multi-line #F5F5F5 input (Remind message, Not received note)
- PBComposer → 115:907 Control / Composer → comment/Ask Paybak input, Empty/Typing, Pinned keyboard bar 402x68
- PBAmountField → 125:1084 Control / Amount Display → amount-first entry, Amount/Display 56 with currency/date chips
- PBPaymentParties → 125:1085 Control / Payment Parties → From → To card on Record payment
- PBSplitRow → 126:1596 Row / Split Person → split editor row, Equally/Exact/Percent/Shares × Default/Focused/Excluded
- PBSplitTotalBar → 125:1170 Card / Split Total → split editor footer, Balanced/Error
- PBPlanCard → 125:1198 Card / Plan → paywall plan option (Yearly selected / Monthly)
- PBAvatarPair → 116:1005 Avatar / Pair → from → to avatars, Size 32/56
- PBCommentRow → 116:1007 Row / Comment → expense comment row
- PBHistoryRow → 116:1058 Row / History → edit-history timeline entry, Middle/Last
- PBPersonRow → 127:2252 Row / Person → universal people row, Regular/Compact × 10 trailing types
- PBTransferRow → 128:1604 Row / Transfer → settle-up plan row with Avatar / Pair
- PBTitleHeader → 128:1857 Header / Title Row → group/friend/project detail header, Tile/Avatar + members
- PBAmountHero → 128:2004 Header / Amount Hero → expense/payment/loan detail hero, Icon/Avatar/Pair + chips
- PBNoticeCard → 129:1976 Card / Notice → in-flow notice, Leading/Centered × None/One/Two actions
- PBConfirmPaymentCard → 129:2060 Card / Confirm Payment → receiver confirm card, Pending → Confirmed (smart animate 250 ms)
- PBQRCodeCard → 130:1912 Card / QR Code → 240 white QR card with app-mark centre (generate natively, ECC H)
- PBGroupRow → 139:2052 Row / Group → group/project list row, Group/Project/Archived × Owe/Owed/Settled
- PBProgressBar → 116:1099 Control / Progress Bar → Small/Large × Default/Projected/Over, optional mark (+ example 116:1101)
- PBBarRow → 143:2156 Row / Bar → insights/fair-share bar row, Icon/Avatar × Neutral/Owed/Owe
- PBMonthlyBarChart → 143:2157 Chart / Monthly Bars → six-month bar chart, current month black
- PBBudgetCard → 145:2106 Card / Budget → project budget card, On track/Over budget/Closed (+ example 159:11440)
- PBLoanProgressCard → 145:2155 Card / Loan Progress → IOU progress, Active/Paid back (+ example 145:2156)
- PBChatBubble → 117:971 Chat / Bubble → Ask Paybak message, User (black bubble) / Assistant (plain + sparkles avatar)
- PBDraftExpenseCard → 147:2315 Chat / Draft Expense → assistant draft expense, Pending → Saved (smart animate 250 ms)
- PBReceiptLineRow → 117:993 Row / Receipt Line → scanned receipt line, Default/Total × Default/Editing
- PBAssignItemRow → 147:2532 Row / Assign Item → receipt item with You/Esha/Dev avatar chips, Shared False/True
- PBPersonTotalsCard → 147:2533 Card / Person Totals → live per-person totals (₹989/₹621/₹690)
- PBShutterButton → 117:1001 Control / Shutter → camera shutter Default/Pressed on bg/camera
- PBToast → 118:965 Overlay / Toast → black capsule save confirmation, 2 s
- PBSheet example → 118:10155 Sheet / Container populated slot → Category picker sheet pattern (search + Row / Setting card)
- PBReceiptThumbnail → 86:730 Art / Receipt → Leopold Cafe receipt art Full 300x458 / Thumb 56x72 (exported)
**Summary:** Wrote components-app.md (814 lines), the implementation-ready spec for all 36 component sets and examples on page 02 that the first spec didn't cover: Shared (from Profile) (4 sets), Forms & Money (8), Lists & Detail (11), Progress & Charts (5 + 2 examples), Assistant & Scan (6), the new Overlay / Toast and the populated Sheet / Container example, and Art / Receipt. Each entry has the verbatim description with its SwiftUI name, properties and defaults (the sample data), exact geometry, tokens, text styles, states, prototype reactions (only Confirm Payment and Draft Expense have them, both CHANGE_TO with smart animate 250 ms ease-out), reuse of nested components, and iOS/Android notes.

Change check: I re-dumped all 36 sets covered by components-core.md and components-home.md and compared them value by value. Only three changed:
- Card / Balance: new Show caption option (96 tall without the caption), the badge now sits at the end of the top row (row 24, card 120), and "You’re owed" uses the curly ’.
- Row / Attention: the detail text now fills its line and truncates with "…", so the row stays 88 tall.
- Sheet / Container: new Show header option (content starts 20 from the top when off), and the header lost its 4 pt left padding so the title starts at x 16.
The old files were not edited.

Icons: page 02 has 65 icon components, and all 65 are already in assets/icons. Nothing was exported; icons/INDEX-v2.md records this and maps the icons the new components use.

Art: exported Art / Receipt as images/art-receipt-full.svg (live Manrope text), art-receipt-full@3x.png (900x1374) and art-receipt-thumb.svg, indexed in images/INDEX-v2.md. None of the new components has scene art to map to the six .riv files.

The decoded plugin dumps weren't kept. 2x references are the REST renders in .figma-cache/components/<id>.png.
**Issues:**
- The Figma MCP allowance was exhausted: about 11 Figma calls succeeded and roughly 20 were rate-limited. clientStorage turned out to be unsupported (getAsync is not a supported API), so chunked caching didn't work; I switched to small self-contained compressed dumps, then to the REST data.
- No get_design_context output was saved for these sets; nearly every attempt was rate-limited. The decoded Plugin API dumps (not kept) and the REST node JSON (.figma-cache/nodes/3-3-*.json) hold the same values.
- No new 2x PNGs were written to ref/ for the components. The spec points to the 2x REST renders in .figma-cache/components/<id>.png, all 41 of which exist.
- Finding: in the new components, nested 16/20 pt icons keep stroke 1.5, while the older components scale the stroke (16 → 1.0, 20 → 1.25). The spec recommends keeping the README rule (scale the whole icon) and asking the designer (§11 #1).
- Finding: Card / Balance and Card / Balance Summary now render the curly ’ in 'You’re owed', although the description says the Summary keeps the straight ' and screens-home.md specifies the straight one for Home. Flagged in components-app.md §8.1 and §11 #2.
- Art / Receipt Full was exported as an SVG with live Manrope text (the 194 kB outlined version wasn't saved) plus a PNG @3x. Ship the PNG where text fidelity matters.
- QR rendering on Android needs a new Gradle dependency (e.g. com.google.zxing:core), which needed approval (the app now uses com.google.zxing:core 3.5.4).
- Copy that isn't in Figma is proposed and marked as a proposal: split over-assignment 'X over', Person Totals before all items are assigned, and the partial-repayment loan caption.

## spec:changes-03-04
**Files:** screens-home-v2.md, ref/homeConfirmPayment.png, ref/v2/splash.png, ref/v2/welcome1.png, ref/v2/welcome2.png, ref/v2/welcome3.png, ref/v2/getStarted.png, ref/v2/signIn.png, ref/v2/verify.png, ref/v2/verifyWrong.png, ref/v2/setup1.png, ref/v2/setup2.png, ref/v2/setup3.png, ref/v2/setup4.png, ref/v2/allSet.png, ref/v2/homeActive.png, ref/v2/homeFirstDay.png, ref/v2/homeAllSettled.png, ref/v2/homeAddSheet.png, ref/v2/homeAddSheet-overlay.png (plus the node dumps used while writing, not kept)
**Screens:**
- homeConfirmPayment → 167:11424 "Home — Confirm payment" → NEW Home state: pending incoming payment claim card (Card / Confirm Payment, Esha ₹700) above unchanged balances; Confirm → Payment confirmed state (08-11, +₹2,200 from 3 people, toast), Not received → sheet over Home (08-10)
- homeActive → 24:5 "Home — Active" → CHANGED: "You're owed" now "You’re owed" (curly U+2019, Card / Balance component); new hotspots link cards, Settle up, Remind, Settle, See all, Dinner row, sparkle, bell and tabs to pages 05/06/07/08/09/11
- homeFirstDay → 24:326 "Home — First day" → visually unchanged; Add expense now → Add expense — Empty (176:17454) directly, Invite friends → Add friend (177:26746), header/tab hotspots added
- homeAllSettled → 24:414 "Home — All settled" → visually unchanged; header/tab hotspots added
- homeAddSheet → 24:520 "Home — ＋ Action sheet" (+ overlay 24:808) → changed only via the curly apostrophe under the scrim; scrim/✕ now NAVIGATE → Home — Active (dissolve 200 ms); rows link to page 06 forms
- splash, welcome1-3, getStarted, signIn, verify, verifyWrong, setup1-4, allSet → page 03 frames 22:32, 22:55, 22:133, 22:200, 22:272, 39:356, 39:534, 39:644, 42:665, 44:938, 46:1021, 46:1148, 46:1223 → re-rendered at 2x: no visible change; all 14 notes are verbatim-identical
**Summary:** I re-rendered all 18 page 03/04 refs at 2× by exporting each of the 3 sections once with download_assets at scale 2 and cropping at 80 px + 2·frame position (the best alignment offset was 0 for all 18). I saved the renders to ref/v2/ and pixel-diffed them against the old refs.

Page 03 has no visible change. Every note is verbatim-identical to the existing specs, and the flow starting points haven't changed. Its prototype reactions weren't re-read because of the limit (issue 3).

Page 04 changes:
1. The only pixel change: "You're owed" (U+0027) → "You’re owed" (U+2019), in the Card / Balance component. It shows on homeActive and under the homeAddSheet scrim (box x82.5 y201 w53.5 h10). This supersedes README rule 5 and the straight-apostrophe notes in screens-home.md and components-home.md.
2. Card / Balance gained a `Show caption` property.
3. The section subtitle now mentions "confirm payment".
4. New frame "Home — Confirm payment" (167:11424), plus a new flow starting point with the same name.
5. Prototype hotspots were added to every Home frame. The "later phase / inert" statements in screens-home.md are obsolete, and First day "Add expense" now opens Add expense — Empty directly instead of the Add sheet.
6. On 24:520 the scrim and ✕ now go back to Home — Active.
7. The confirm frame uses a shorter fade (y756 h118) than Active (y724 h150).

screens-home-v2.md holds:
- the change report;
- the full navigation map of Home as the tab root, with every destination resolved to its frame name and page code;
- the full homeConfirmPayment spec: geometry, the NEW Card / Confirm Payment component in both its Pending and Confirmed states, the Confirm result ("Payment confirmed" 08-11: +₹2,200 from 3 people, the new "Esha paid you" row, the toast 16 above the tab bar), the Not received sheet flow, business rules, all sample data, the reuse map, art (Esha = avatars/avatar-4.svg, no Rive), and proposed test IDs;
- every Home designer note verbatim, plus the notes on pages 05, 06, 07, 08, 09, 11 and 12 that define Home behaviour.

No new icons or images were needed. I made 7 successful Figma calls. The Figma Education-plan daily limit (200 calls/day) then blocked further calls, so some details come from Figma dumps saved for other specs: the Confirmed variant, the page 07–11 captions and the destination frame names. Several details are still unverified (see issues).
**Issues:**
- The Figma MCP daily limit (Education plan, 200/day) blocked all calls from about 00:40 IST, after 7 successful calls.
- I couldn't run get_design_context for 167:11424. Two Plugin API node-tree dumps were used instead (not kept). ref/homeConfirmPayment.png is cropped from the 2× section export rather than taken with get_screenshot; it's pixel-equivalent, the same pipeline as the other refs.
- The Add-sheet row hotspots on 24:808/24:520 were cut off by the 20 kB result limit. The row destinations (176:17454, 177:29750, 185:25810, 190:8356) come from the identical overlay-helper copies on pages 07, 08 and 09.
- Page 03 reactions were not re-read. The renders and notes are identical, so no change is expected.
- The Card / Confirm Payment description and the State=Confirmed variant geometry come from Figma dumps made for the Settle Up and Activity specs, not from my own reads. The tint of the Confirmed variant's check-circle icon is unknown; I recommend icon/primary.
- Open decisions for the architecture plan: (a) The demo dataset has Esha's ₹700 claim pending (09-01/09-08/09-09), so the default demo Home is Confirm payment. I propose seeding the homeActive debug screen without the claim. (b) Several pending claims aren't designed; I propose stacking the cards. (c) Rows with no reaction (Priya/Electricity activity rows, the Due soon row bodies) get proposed destinations. (d) The collapse animation after Confirm is a proposal. (e) Whether Ask Paybak is Pro-gated isn't stated for Home.
- The old README §5.1 #12 (First day Add expense → Add sheet) and README §3 rule 5 (straight ' in You're owed) are now wrong. screens-home-v2.md documents this, and the README lists both as superseded.

## spec:profile-avatar
**Files:** screens-profile.md, assets/avatar-parts/manifest.json, assets/avatar-parts/boy/ (39 SVGs: base, hair, beard, eyewear, eyes, mouth, outfit, outfit-back), assets/avatar-parts/girl/ (45 SVGs: base, hair, hair-back, accessory, eyewear, eyes, mouth, outfit, outfit-back), assets/avatar-parts/check/ (18 PNGs: Figma-vs-native comparisons for the characters, both parts sheets and all 8 screens; option contact sheets; full composites incl. beanie+bun), ref/profile.png, ref/editAvatarBoyHair.png, ref/editAvatarBoyBeard.png, ref/editAvatarBoyEyewear.png, ref/editAvatarBoyOutfit.png, ref/editAvatarGirlHair.png, ref/editAvatarGirlAccessory.png, ref/editAvatarGirlOutfit.png, ref/editAvatarDiscard.png, ref/<same 9 ids>_1x.png (1x crops of an MCP section screenshot) (plus the node dumps used while writing, not kept)
**Screens:**
- profile → 64:4316 Profile → Profile tab root: 120 pt avatar circle (the custom character, Head crop), name and UPI handle, Edit avatar button, settings card (Paybak Pro 'Try free' → Paywall 167:13003; Payment details 167:14684; Currency 176:17773; Notifications 176:18371; Privacy 176:19720; Help & feedback 177:24773), Sign out, glass tab bar with Profile active
- editAvatarBoyHair → 64:4503 Edit avatar — Boy · Hair → pushed avatar editor: Push header (Back / Save), 362×300 stage with Shuffle, Boy|Girl segmented control, scrolling category chips, 3-column 112 pt tiles (Hair; Quiff selected)
- editAvatarBoyBeard → 64:4963 Edit avatar — Boy · Beard → editor state: Beard chip, Stubble selected
- editAvatarBoyEyewear → 64:5384 Edit avatar — Boy · Eyewear → editor state: Eyewear chip, Square selected, None removes glasses
- editAvatarBoyOutfit → 64:5931 Edit avatar — Boy · Outfit → editor state: Outfit chip (chip row scrolled −125), Bust-crop tiles, Jacket selected; Back is dirty, so it opens the Discard alert
- editAvatarGirlHair → 64:6721 Edit avatar — Girl · Hair → editor state: Girl set, Ponytail selected (front and back hair layers)
- editAvatarGirlAccessory → 64:7535 Edit avatar — Girl · Accessory → editor state: Bow selected; buns show through the beanie
- editAvatarGirlOutfit → 64:8052 Edit avatar — Girl · Outfit → editor state: chip row scrolled −158, Striped tee selected
- editAvatarDiscard → 64:8823 Discard alert → alert over Boy · Outfit: 40 % scrim, Overlay / Alert Destructive 'Discard changes?' with Keep editing and Discard
- (reference only) 61:100 / 61:145 / 61:272 Avatar / Character, 53:899 / 53:900 part grids, 62:243 / 62:2148 parts sheets, 64:3948 UI components (Avatar Part Tile, Avatar / Stage); 216:24577 ↳ Add sheet (overlay) is a prototype-only copy of the Home Add sheet (24:808)
**Summary:** I wrote screens-profile.md for page 05 Profile, exported every avatar part, and built a manifest. Figma has no avatar.riv, so the app must stack the part SVGs natively. A test render of those layers matches Figma's own renders. get_design_context was not run, and a few behaviours Figma doesn't define are written as proposals.

**Avatar parts**
- There are 98 part components. 84 have art and are now SVG files under assets/avatar-parts/<gender>/<category>/<option>.svg. The other 14 are empty in Figma (e.g. Beard None), so they have no file; the manifest marks them as null ("draw nothing").
- Every file is the full 772×842 rig with plain paths only, so the layers stack 1:1 with no offsets. They were exported 1:1 through the REST helper (now tools/figma_rest.py). A few art pieces that ran past the bottom are clipped to 842; nothing on screen reaches that area.
- **Manifest** (manifest.json):
  - UI order of categories and options, with the Figma node for each option.
  - Defaults, which options are "none", and each category's tile crop.
  - Layer order from bottom to top.
  - The rules:
    - Outfit sets the front and back layers together.
    - Girl hair sets front and back hair together.
    - Boy | Girl keeps each character's picks.
    - Buns show through the beanie by design, because the bun sits in the back hair layer and pokes above it. No special handling is needed.
- **Crops, from the Figma instance sizes:**
  - Head: the rig window (72, 60, 600, 600). Used for avatar circles and most editor tiles.
  - Bust: (61, 192, 650, 650). Used for Outfit tiles.
  - Stage: scale 300/782, with the rig bottom on the stage bottom.
  - The UI-components note gives different stage numbers (×0.4689). The spec follows the actual instances.

**Proof the layering is right.** I stacked the files in the manifest order and compared them with Figma's own 2× renders:
- Full characters: mean difference about 0.1/255.
- All 72 options in both parts sheets: average 2.5, worst 4.3.
- The 7 editor stages: 0.5–0.6. The 42 editor tiles: 1.2–3.6. The Profile circle: 3.6.
- The best alignment is always at zero offset; what's left is edge anti-aliasing. Side-by-side proof images are in assets/avatar-parts/check/.

**What the spec covers**
- The Profile tab: every row with its destination, the Pro badge ("Try free" on the free plan, the value "Active" after subscribing), tab links, sample data and notes.
- The editor as one screen with seven states: draft, Boy | Girl, chips, tiles, Shuffle, Save, the unsaved-changes rule and the Discard alert.
- The four shared components used here and the three new local ones (Avatar / Character, Avatar / Stage, Avatar Part Tile).
- Persistence, debug seeds, test ids, the verbatim notes and the assets list. No new icons or other images were needed.

**How the user's avatar shows elsewhere:** one value with four kinds (none, Setup 1 preset, photo, custom character). Setup 1 writes preset, photo or none. Saving in the editor makes the character the user's avatar in the Profile header and every "You" circle, using the Head crop.

**Figma data sources**
- The Figma MCP was heavily rate-limited, so most reads came from the REST export (now .figma-cache/).
- My own read-only Plugin API node dumps weren't kept.
- Per-frame REST node data stood in for get_design_context output.
- The 2× references are the REST renders copied to ref/<screenId>.png; I also saved 1× crops.
- The "Overlay helpers" section on this page only holds a copy of the Home Add sheet, so I noted it and did not spec it.
**Issues:**
- get_design_context was not run for any frame. The MCP allowance ran out, so the REST data was used instead: per-frame node JSON plus Plugin API dumps.
- The 2× references are the REST renders (ref/<id>.png), not my own get_screenshot calls. My section screenshot came back at 1× only, so I also saved ref/<id>_1x.png crops.
- Open decision (demo data): only the Profile header shows the custom character; every other page draws Arjun as preset head avatar-1. Proposal: the demo dataset uses preset 0, and only the profile/editAvatar* start screens seed the character. The alternative is the character everywhere.
- Stage framing conflict: the Figma UI-components note says ×0.4689 (rig y40→680), but the actual Avatar / Stage component and every screen instance use ×0.3836 (rig y60→842). The spec follows the instances.
- Not wired in Figma, so these are proposals: Sign out (confirm alert, then clear only the session), the Profile scroll with the collapsed header (Sign out overlaps the tab bar by 7 pt), the handle and Payment-details value rules when no UPI ID is set, Shuffle rules, tile-tap feedback, and the editor's starting gender for users on a preset.
- No avatar.riv exists, although the Figma notes assume one. The app must compose the avatar natively from the exported part layers, as the spec describes.

## spec:add-expense
**Files:** screens-add-expense.md, ref/addExpenseEmpty.png, ref/addExpenseFilled.png, ref/addExpenseSplitWith.png, ref/addExpensePaidBy.png, ref/addExpenseSplitEqually.png, ref/addExpenseSplitExactError.png, ref/addExpenseCategory.png, ref/addExpenseCurrency.png, ref/addExpenseDueDate.png, ref/expenseAdded.png, ref/addExpenseEmpty_1x.png, ref/addExpenseFilled_1x.png, ref/addExpenseSplitWith_1x.png, ref/addExpensePaidBy_1x.png, ref/addExpenseSplitEqually_1x.png, ref/addExpenseSplitExactError_1x.png, ref/addExpenseCategory_1x.png, ref/addExpenseCurrency_1x.png, ref/addExpenseDueDate_1x.png, ref/expenseAdded_1x.png (plus the node dumps used while writing, not kept)
**Screens:**
- addExpenseEmpty → 176:17454 → the Add expense form (full-screen modal from ＋) just opened: amount focused on the decimal pad, ₹0 placeholder, Save disabled
- addExpenseFilled → 176:18002 → the same form filled (Olive Garden ₹2,800, Priya/Esha/Dev, Food, Equally · ₹700 each, due This weekend = Sun 4 Oct), Save enabled, scrolls under a fixed header
- addExpenseSplitWith → 176:19238 → pushed multi-select people picker: search, removable chips, You, Add a new friend, Friends list including guest Ananya Rao
- addExpensePaidBy → 176:19877 → Medium sheet: pick the single payer from the people on the expense, or Multiple people (payer editor, specced as a proposal)
- addExpenseSplitEqually → 176:21025 → pushed split editor in Equally mode (4 × ₹700) with the segmented control Equally · Exact · % · Shares and the gray live footer ₹0 left
- addExpenseSplitExactError → 177:21514 → split editor in Exact mode, Dev mid-edit at ₹550: red footer ₹150 left / ₹2,650 of ₹2,800, Done disabled, decimal pad
- addExpenseCategory → 177:21884 → Large sheet with search: 8 categories (Food ✓, Travel, Stays, Fun, Rent, Bills, Shopping, Other) with their icons
- addExpenseCurrency → 177:22329 → Large sheet with search: Recent (INR selected, AED) and All currencies rows (Setup Row / Currency); a non-default currency adds a saved rate line
- addExpenseDueDate → 177:23763 → Medium sheet: kit inline calendar (Sun 4 Oct), 'Sun 4 Oct · in 4 days', reminder-schedule hint, Set due date / No due date; the Date variant (Today chip) is specced from the note
- expenseAdded → 177:24360 → after Save: the 09-03 Expense detail template with Olive Garden data (no group chip or group balance row, payer first, no Flag row) and the 'Expense added' toast
**Summary:** I wrote screens-add-expense.md (about 96 KB), the implementation spec for all 10 frames of the "Add expense" section. Implementers don't need Figma to use it.

- **Business rules (§3):** amount entry on the system decimal pad, in paise with Indian grouping. Save is enabled only when the amount is above 0, at least one other person is on the expense, and the split adds up. The section also covers people, payer(s), all four split modes, the fair-rotation algorithm for leftover paise, the live footer states, the Split row value formats, the date and due-date rules (quick chips, reminder schedule), categories and their icons, currency with a saved rate line, group, Repeat (paywall on the free plan, Repeat sheet for Pro), receipt, notes, what Save does, edit mode and the discard alert.
- **Per-screen sections (§4–§11):** exact geometry, component variants, text styles, colour tokens, icons, states, scroll and keyboard behaviour. Each has a Navigation table taken from the real prototype reactions.
- **New components specced in full:** Row / Split Person (all 12 Mode × State variants) and Card / Split Total. I also quote the current descriptions of Row / Person and Sheet / Container. Other components are mapped to their existing specs.
- **Notes and data:** all section notes, frame captions, dev-mode annotations and related notes from other pages are copied verbatim. §12 has the verbatim sample data, including off-screen content and hidden component defaults not to render. §13 has debug start screens, test IDs and UI-test flows.
- **Undrawn states:** the Date sheet, the payer editor, and the Percent/Shares modes are derived from the notes and component variants, and marked as proposals where Figma is silent.

**Sources:** 3 get_design_context outputs, one reactions/components use_figma call, one section screenshot, and the REST export. I mapped REST variable IDs to token names myself. The 2× refs come from the REST renders, and I cropped 1× refs from the section screenshot. There is no illustration art in this section and no icons were missing, so no assets were exported.
**Issues:**
- The Figma MCP allowance was used up: about 30 attempts hit the Education-plan limit. Only 5 MCP reads succeeded: get_design_context for Empty, Filled and Split with, one use_figma listing reactions and components, and one section screenshot. The other 7 frames were read from the REST export, which has the full node trees, text, annotations and interactions. So no information is missing; design-context output exists only for 176-17454, 176-18002 and 176-19238, and a REST node-tree dump (not kept) stood in for the rest.
- The 2× refs are the REST renders, not get_screenshot output. The *_1x refs were cropped from a 1× section screenshot: the MCP returned the section at 1×, not 2×.
- The REST JSON has variable IDs, not names. I rebuilt the ID→token map by matching the design-context CSS variables and the token order in tokens.md (a scratch map, not kept). The kit (remote) variables are only labelled 'kit'.
- Correction for the domain model (domain.md): the Olive Garden expense (₹2,800, today, due Sun 4 Oct) is part of the base demo dataset and is inside Home's +₹2,900. The expenseAdded debug start screen should open the existing expense, not create a duplicate.
- Not designed; proposals written in the spec: the discard alert copy on ✕, default title/category, 'Next week' = today + 7, the Split row value formats for Exact/%/Shares/uneven Equally, the 'over' footer and the % footer, Back on an unbalanced split, the Multiple-people payer editor, the rate-line copy ('≈ ₹27,420 · ₹22.85 per AED'), the group picker, the Notes sheet, guest creation from search, the Date-sheet summary and date limits, and the Android custom calendar.
- Open questions for product: can 'You' be left out of a split? Should the Paid by sheet grow to Large with many people? Figma's 'All currencies' shows exactly 7 major currencies, not the full ISO list; the spec proposes majors first, then all ISO currencies. What is the rates source (bundled table vs a user-editable rate)?
- The README screen map didn't list these 10 screen ids at the time; app-architecture.md §1 lists every screen id.

## spec:record-lend-group
**Files:** screens-record-lend-group.md, ref/recordPayment.png, ref/paymentRecorded.png, ref/lendMoney.png, ref/loanAdded.png, ref/loanPaidBack.png, ref/loanOverdue.png, ref/newGroup.png, ref/newGroupProject.png, ref/newGroupCreated.png, ref/recordPayment_1x.png, ref/lendMoney_1x.png, ref/loanAdded_1x.png, ref/loanPaidBack_1x.png, ref/loanOverdue_1x.png (plus the node dumps used while writing, not kept)
**Screens:**
- recordPayment → 177:29750 (Record payment — form) → full-screen modal from the ＋ sheet: log a payment made outside Paybak (parties, amount, method chips + UPI preview, For/Date/Proof, summary); Save → paymentRecorded
- paymentRecorded → 177:30023 (Payment recorded) → pushed payment detail, payer's view while pending (pair hero, grey 'Pending confirmation' notice, 6 detail rows, Cancel payment alert, toast); scrolls under a fixed header
- lendMoney → 185:25810 (Lend money — form) → full-screen modal for a direct IOU: I lent/I borrowed, amount, Lent to/Reason/Date, Installments toggle with count stepper, Repeats, First due and a schedule preview
- loanAdded → 186:10325 (Loan added) → loan detail, state Active: hero, Card / Loan Progress (Original/Paid/Remaining + bar), 3 monthly installment rows, pinned Record repayment, toast 'Loan added'
- loanPaidBack → 186:26377 (Loan — Paid back) → same loan screen, state Paid back (Kabir ₹4,500, 'Paid back' chip, full bar, paid rows incl. '2 days late' in grey, no buttons)
- loanOverdue → 186:26608 (Loan — Installment overdue) → same loan screen time-shifted to Tue 3 Nov: red 'Overdue 4 days' badge on installment 1, 'Last reminder sent Mon 2 Nov', Remind Dev + Record repayment
- newGroup → 190:8356 (New group — Group) → full-screen modal, segment Group: name, Type chips, Members card with remove + Add people, Currency, Simplify debts (on)
- newGroupProject → 190:12242 (New group — Project) → same modal, segment Project (scrolls): name, description, cover photo, budget, Contribution Equal/Percent/Fixed, members at 25%, currency
- newGroupCreated → 190:27177 (Group created) → pushed empty group detail: title row with plane tile + member stack, 'Your balance ₹0' card with disabled Settle up, 'No expenses yet.' empty state (paybak-homefirstday.riv) with Add expense, toast
**Summary:** I wrote screens-record-lend-group.md for all nine frames in the Record payment, Lend money (IOU) and New group sections of page 06. The file stays at the same level of detail as screens-home.md. It contains:
- **Per frame:** container type, header, exact frame-coordinate geometry for every element (component, variant, text style and colour token), scroll and keyboard behaviour, a Navigation table built from the prototype reactions (checked against the REST JSON), and test IDs. The three loan frames are written once as one screen with three states; the two New group frames are written as one modal with two segments.
- **Designer notes:** every section note, frame caption and on-canvas annotation, verbatim. Examples: the Cancel payment alert copy, the UPI preview rule, the Installments on/off rule, the Simplify debts default and the scroll-spacer note. I also added the relevant page 07/08 notes that describe these frames.
- **Business rules (§8):**
  - Payments: pending until confirmed, pending never changes balances, partial payments, overpayment becoming a balance in your favour, currency conversion at today's rate, and the Not received and cancel flows.
  - Installments: amount / count, due(i) = first due + (i−1) × period with same-day-of-month for monthly, default first due = loan date + one period, and repayments applied in due-date order.
  - Late and overdue display: "· N days late" stays grey; the Overdue badge is red only for what is overdue now (Tue 3 Nov − Fri 30 Oct = 4 days).
  - Reminder schedule: 2 days before, on the due date, then every 3 days when overdue. Worked example: Wed 28 Oct, Fri 30 Oct, Mon 2 Nov.
  - Groups: Simplify debts on by default, currency defaults to the profile currency, and the "{Type} · {n} members · {CODE}" subtitle.
  - Projects: optional description, cover photo and budget; contribution Equal (100/n %, "25%" with 4 members), Percent or Fixed.
- **Sample data (§9):** everything verbatim, including hidden content such as the UPI preview (Meera Iyer, meera@okhdfcbank). It also covers the demo dataset: the Kabir loan belongs in the base data, while the Dev loan and the Weekend Trek group are seeded only for their debug start screens. The Dev loan stays out of the base data so the Home totals still come out as in Figma.
- **Reuse map (§1):** already-specced components with their section numbers, and the 13 new ones with the Figma descriptions quoted and full geometry.
- **Art:** the only illustration, on Group created, is the same Figma component as Home First day. A pixel diff against ref/homeFirstDay.png confirms it, so reuse paybak-homefirstday.riv. No new icons or images were needed; all 21 icons and the 5 avatars already exist.

I saved all nine 2× references (804×1748) by exporting each section at scale 2 and cropping. They match the REST renders, with a mean difference under 0.02/255.
**Issues:**
- Figma's MCP quota (Education plan) was exhausted for most of the read, so the REST data was used instead.
- The nine frames were read from Plugin-API node dumps (not kept), not get_design_context output. The dumps have full depth, token names, variants, annotations and reactions. They come from an earlier compressed dump (all frames except the tail of 190:27177) plus a separate dump of 190:27177.
- No new icons or images were exported: every icon and avatar used here already exists, and the only illustration reuses paybak-homefirstday.riv.
- Cross-spec conflict: screens-settle.md §5.2 proposes its own 'Cancel payment' alert copy for 08-06. The designed copy is the 06-12 annotation: 'Cancel this payment?' · 'Meera won’t be asked to confirm. You’ll still owe her ₹450.' · 'Keep' · 'Cancel payment'. Both pages should use it. It's recorded in §3.4 and §11.2 #11.
- The Figma component descriptions for Amount Hero, Avatar Pair, Card / Notice, Row / Person, Title Row and Progress Bar are quoted from screens-settle.md and screens-groups.md, which read them from Figma. Card / Loan Progress and Card / Balance are quoted from the REST node JSON (.figma-cache/nodes/77-100.json). components-app.md did not exist yet; the spec says it wins once it does.
- Not designed, so the spec gives proposals: the Not received and Confirmed payment states, and where Edit goes on the payment and loan screens. Also: the Installments-off Due row and chips, the 'I borrowed' copy, and Repeats options other than Monthly. Also: stepper limits, month-end clamping, the schedule preview when there are more than 3 installments, and where Record repayment goes. Also: Type→icon mapping (Home→home, Friends→people, Other→tag, inferred from the Title Row description), the Percent/Fixed helper text and validation, the project landing screen after Create, summary wording for Bank/Card/Other, and whether loans count toward the Home totals.

## spec:groups-friends
**Files:** screens-groups.md, ref/groupsList.png, ref/friendsList.png, ref/groupsEmpty.png, ref/groupGoaTrip.png, ref/groupSettings.png, ref/groupLeaveBlocked.png, ref/groupDubaiWeekend.png, ref/friendRohan.png, ref/addFriend.png, ref/myQrCode.png, ref/myQrCodeSheet.png, ref/friendAnanyaGuest.png (plus the node dumps used while writing, not kept)
**Screens:**
- groupsList → 167:14881 (07-01) → Groups tab, Groups segment. Groups and projects in one list with the open-balance ones first. Includes the project budget bar, "You’re settled" and a read-only Archived section. Tab root, tab bar Active=Groups.
- friendsList → 167:15557 (07-02) → Groups tab, Friends segment. One net per friend across all groups and direct expenses. The summary line (+₹2,900 / −₹1,850) matches the Home totals. Only the overdue badge is red, and the guest shows "No balance".
- groupsEmpty → 167:16388 (07-03) → Groups segment with no groups yet. Uses the Card / Empty State with the Get Started trio (reuses paybak-getstarted.riv), plus New group and Invite friends buttons.
- groupGoaTrip → 167:18792 (07-04) → Group detail (INR, open balance). Title row with the member stack, balance card −₹1,400 with Settle up (goes to settleRecordKabir), Balances card (paid vs share, ₹39,500 ÷ 5 = ₹7,900), simplify-debts footnote, and expenses grouped by date.
- groupSettings → 176:18633 (07-05) → Group settings. Name, Settle by, Members + Add, Currency, Simplify debts toggle, Recurring expenses (None) and the red Leave group button.
- groupLeaveBlocked → 176:18995 (07-06) → Group settings scrolled 26 pt with the Overlay / Alert "You can’t leave yet" (Not now / Settle up → Record payment to Kabir).
- groupDubaiWeekend → 176:20314 (07-07) → The same group template in AED and settled. Each expense shows its saved-rate ₹ line, and the total footnote reads AED 1,800 ≈ ₹41,118 (21,936 + 12,312 + 6,870).
- friendRohan → 177:25988 (07-08) → Friend page for an overdue friend. Balance card +₹800 with the Overdue badge, Remind (opens the page 08 Remind sheet overlay) and Record payment, history, Groups together, and the Automatic reminders toggle.
- addFriend → 177:26746 (07-09) → Add friend. Search, invite link, Scan QR, My QR code, contacts on Paybak (Added), and Invite for Ananya, who becomes a guest friend.
- myQrCode → 177:28130 (07-10) → My QR code sheet (Sheet / Container Medium) over Add friend. Contains the avatar, name and @arjun handle, a generated QR (ECC H, with the app mark), the paybak.app/i/arjun link with Copy, and Share link. The 177:28072 sheet body is also in ref/myQrCodeSheet.png.
- friendAnanyaGuest → 177:28684 (07-11) → Guest friend page. Initials avatar and Guest tag, the Card / Notice invite with Send invite, and "No balance yet" with Add expense.
- (prototype-only, not built) 189:6345 → the myQrCode sheet; 216:28953 → the Home ＋ Add sheet (screens-home.md §5); 216:28961 → the page 08 Remind sheet (screens-settle.md, settleRemind).
**Summary:** I wrote screens-groups.md, the implementation-ready spec for page "07 Groups & Friends". It covers 11 screens in 3 sections, at the same level of detail as screens-home.md.

For each screen it gives:
- the container type, pinned header and scroll behaviour
- a top-to-bottom element list with exact frame coordinates, component variants, text styles and colour tokens
- navigation for every tappable element, from the Figma reactions or from the designer notes
- test IDs and debug start-screen ids
- the designer's notes copied word for word, plus the section-level notes

A reuse map points every element to components-core.md or components-home.md, or marks it NEW. The new components are fully described with their Figma descriptions quoted: Row / Group, Progress Bar Small, Row / Person Regular and Compact, Header / Title Row, Push Header (with the fixed white band behind it), Row / Setting, Overlay / Alert, Card / Notice, and the Sheet / Container + _Sheet / My QR code + Card / QR Code set.

Section 2 writes the business rules down as requirements:
- **Group balances:** each member's net is what they paid minus their share. Goa Trip's six expenses total ₹39,500, so each share is ₹7,900; the nets are You −1,400, Kabir +10,100, Priya −4,400, Esha −2,900, Dev −1,400. Only confirmed payments count.
- **Simplify debts:** an explicit greedy algorithm, and the template for the footnote under the Balances card.
- **Foreign-currency groups:** amounts stay in AED and each expense keeps the rate saved on its date. The total is the sum of each expense's own conversion (21,936 + 12,312 + 6,870 = ₹41,118).
- **Per-friend net:** one net per friend across groups (using the simplified plan) and direct expenses. It adds up to Home's +₹2,900 from 4 people and −₹1,850 across 2 groups, with the sort order and subtitle rules spelled out.
- **Groups list order:** open balances first, then alphabetical (inferred from the frame). It also covers the project budget bar at 87 %, "You’re settled", archived projects being read-only, and the subtitle and icon rules.
- **The rest:** due dates and overdue, the automatic reminder schedule (25 Sep, due date 27 Sep, and today) with the per-friend mute, guests, the leave-group block, recurring rules (a Pro feature), usernames, invite links and the QR spec for CoreImage and ZXing.

All sample data is recorded word for word, including a people/groups table for the demo dataset. The only illustration is the Get Started trio, so it reuses paybak-getstarted.riv; the spec gives the exact view rect for the 240 × 180 slot. No new icons or images were needed; every icon already exists.

I saved 12 2× references (11 screens plus the sheet body alone). The combined node-tree dump wasn't kept. Reading the page took 9 successful Figma calls: the page overview, the dump in 2 compressed chunks, 3 section screenshots at 1×, and 3 section exports at 2× that I cropped per frame.
**Issues:**
- The Figma account was at its Education-plan MCP limit (about 1 successful call per minute, and use_figma output is cut off at 20 KB), so get_design_context wasn't called per frame. A complete Plugin-API node-tree dump with tokens, styles, props and reactions replaces it; get_design_context output exists only for Groups.
- get_screenshot never renders sections above 1×, whatever maxDimension is set to. The 2× references come from download_assets (PNG, scale 2) of each section, cropped with the 80 px padding.
- The groups-list order is inferred: open balances first by due date, then alphabetical, which is the only order that fits the frame. The friends-list order is also inferred: overdue first, then owed, then owe, then no balance, with ties in the order friends were added.
- Designer inconsistency: in 07-10 the Add friend rows under the scrim use 16/16 padding, so the avatars sit at x 36. In 07-09 and 07-02 they sit at x 20. The spec follows 07-09.
- Row / Person Regular is 378 wide at x 4 in Figma. The spec builds it 362 wide with the avatar on the content edge.
- The Friends summary amount "+₹2,900" uses raw Manrope Bold 14/20 with no text style.
- The Get Started illustration is squeezed unevenly (0.663 × 0.692) into the 240 × 180 empty-state slot. The spec scales the Rive artboard evenly by 0.663, so the grey card comes out 3.8 pt shorter top and bottom.
- Many destinations have no prototype link: the Flat 302 and College Gang rows, the Priya, Esha, Dev, Kabir and Meera rows, the Dubai gear, the group-settings rows, Scan QR, Invite with a link, Record payment on the friend page, and the member rows. Undesigned states have no design either: owed-state balance cards, the leave confirmation when your balance is zero, empty Friends, the Pro badge for free users on Recurring, and the contacts permission. All of these carry proposals, marked in the spec.
- Some demo data isn't visible on page 07: Flat 302's third member, Meera's UPI ID, the College Gang members beyond Arjun and Rohan, and who paid for the Farewell dinner. The seed has to take these from other pages or choose them consistently.

## spec:settle
**Files:** screens-settle.md, ref/settleOwedBreakdown.png, ref/settleOweBreakdown.png, ref/settleUp.png, ref/settleRecordKabir.png, ref/settlePaymentPending.png, ref/settleRemind.png, ref/settleRemindOverlay.png, ref/settleRemindShare.png, ref/settleRemindSheet.png, ref/settleNotReceived.png, ref/settleNotReceivedOverlay.png, ref/settleNotReceivedSheet.png, ref/settlePaymentConfirmed.png (plus the node dumps used while writing, not kept)
**Screens:**
- settleOwedBreakdown → 167:11705 (08-01) → pushed screen behind Home's You’re owed card: +₹2,900 from 4 people, overdue first (Rohan red), Settle up button
- settleOweBreakdown → 167:11957 (08-02) → pushed screen behind the You owe card: gray −₹1,850 across 2 groups (Kabir ₹1,400 Goa Trip, Meera ₹450 Flat 302) + simplified-debts footnote
- settleUp → 167:12113 (08-03) → fewest-payments plan: 2 payments to make (Settle → Record payment) and 4 people owe you (Remind → Remind sheet overlay); scrolls under a fixed header
- settleRecordKabir → 167:13635 (08-04) → full-screen modal Record payment prefilled for Kabir (₹1,400, UPI selected, kabir@okaxis with Copy → toast UPI ID copied); Save → pending
- settlePaymentPending → 167:17304 (08-06) → payer's payment detail: Pending confirmation, 7 detail rows (Paid to only for UPI/Bank), Edit, Cancel payment, toast Payment recorded; balances unchanged
- settleRemind → 177:21203 (08-07; overlay-only copy 177:21801 = 08-07s) → Remind sheet over Home: Rohan row, Friendly|Neutral tone, editable message, Send in Paybak (toast Reminder sent to Rohan) / Share…
- settleRemindShare → 177:23342 (08-08) → Share… hands the message to the system share sheet (WhatsApp, Messages, Mail, Copy) over the Remind sheet
- settleNotReceived → 177:25006 (08-10; overlay-only copy 177:25168 = 08-10s) → receiver's Not received sheet over Home — Confirm payment: editable note to Esha, Send / Cancel; Esha's ₹700 stays owed
- settlePaymentConfirmed → 177:25303 (08-11) → Home after Confirm: +₹2,200 from 3 people, Esha paid you ₹700 tops Recent activity, toast Payment confirmed 16 above the tab bar
- (component) _Sheet / Remind Rohan → 176:20970 → local sheet content PBRemindSheet (ref/settleRemindSheet.png)
- (component) _Sheet / Not received → 177:24988 → local sheet content PBNotReceivedSheet (ref/settleNotReceivedSheet.png)
**Summary:** I wrote screens-settle.md for page 08 Settle Up, covering all 9 real frames plus the 2 local sheet components. Each frame has exact geometry, tokens, text styles and component variants, its states, scroll and keyboard behaviour, a navigation table built from the prototype reactions, the designer notes copied verbatim, and a reuse map. The page and section notes are in §0.2. The file also documents 14 new components in full (§0.4), using their Figma descriptions and the geometry seen on this page. Those are Push Header, Modal Header, Row / Person, Card / Notice, Header / Amount Hero, Avatar / Pair, Payment Parties, Amount Display, Category Chip, Row / Setting, Overlay / Toast, Text Area and Card / Confirm Payment, plus the Show copy extension of Card / Payment Preview.

The fewest-payments plan (§3.1) spells out the algorithm and reproduces the Figma numbers. Goa Trip has simplify debts on, so Arjun pays Kabir the whole ₹1,400. Meera gets ₹450 for Flat 302. Rohan owes ₹800 (overdue 3 days), and Priya, Esha and Dev owe ₹700 each for the Olive Garden dinner (₹2,800 split 4 ways). Balances are netted per person, pending payments don't count, and the sort order is given.

- **Reminder templates:** the Friendly message is copied verbatim and turned into a template. No Neutral copy exists in Figma, so I wrote a proposal and flagged it.
- **Send in Paybak vs Share…:** both are covered, with the share sheet contents.
- **Toast texts:** Payment recorded, UPI ID copied, Reminder sent to Rohan and Payment confirmed, each with its exact position (50 above the bottom edge, or 16 above the tab bar on tab screens) and the 2 s fade.
- **Not received:** the sheet copy and templates are included.
- **Home after Confirm:** the card animates to Confirmed in 250 ms and is then gone, You're owed drops to +₹2,200 from 3 people, and "Esha paid you ₹700" is the new first activity row.

§10 lists every sample string. No new icons or art were needed: every icon and avatar already exists, and no frame uses a .riv or an illustration. I exported 13 refs at 2× (download_assets at scale 2, cropped per frame) and dumped the node trees (not kept).
**Issues:**
- Figma rate limit: the account was past the Education plan's 200 MCP calls/day and got roughly one successful call a minute, so most MCP calls were rejected.
- To save calls, the page metadata, the verbatim notes and the dumps of the breakdowns and Settle up came from an earlier, partly rate-limited read and weren't read again.
- I skipped get_design_context for all 9 frames because of the rate limit. The full Plugin API node-tree dump (not kept) replaced it and is more exact.
- get_screenshot rendered sections at 1× no matter what maxDimension was (4416 and 3372 both came back at 1×), so the 2× refs come from download_assets at defaultScale 2, cropped per frame. The export adds a 40 pt margin around the section, which the crop removes.
- Needs a product decision: the Neutral reminder template is not in Figma. Only the Friendly text exists. screens-settle.md §6.3 has a proposal.
- Copy change: in these frames the balance card label and the breakdown title read 'You’re owed' with a curly ’. README §3 rule 5 and screens-home.md say Home uses a straight ' (the Home v2 change report should confirm).
- Not designed, proposals written into the spec: a toast after Not received → Send; a confirmation dialog for Cancel payment; what a pending payment looks like on Settle up; the payment detail after the receiver confirms or says not received; singular/plural captions; the Settle up all-settled state.
- The Meera 'Settle' button links to page 06 'Record payment — form' (177:29750), not to a frame on page 08. The Rohan breakdown row links to 'Friend — Rohan' (177:25988, page 07). Not received Send/Cancel and the Remind scrim link to Home frames on page 04, including 'Home — Confirm payment' 167:11424, which screens-home-v2.md specs.
- The Amount Display description says the Figma kit only has a Number Pad. The spec asks for the decimal pad on device, matching screens-add-expense.md.
- Screen numbers 08-05 and 08-09 don't exist on page 08. The numbering was inferred from the cross-references in the component descriptions.

## spec:activity
**Files:** screens-activity.md, assets/images/receipt-thumb.svg, ref/activityTimeline.png, ref/activityEmpty.png, ref/expenseVilla.png, ref/expenseComment.png, ref/expenseDelete.png, ref/expenseDisputed.png, ref/recentlyDeleted.png, ref/notifications.png, ref/lockConfirmRequest.png, ref/lockReminder.png (plus the node dumps used while writing, not kept)
**Screens:**
- activityTimeline → 167:14361 Activity — Timeline → Activity tab root: day-grouped timeline (Today/Yesterday/EEE d MMM), a pending Confirm Payment card on top, the Timeline | Insights segmented switch, and a glass Restore button that opens Recently deleted
- activityEmpty → 167:16311 Activity — Empty → state of the Activity tab for a new account: an empty card with no actions, reusing paybak-homefirstday.riv, the segmented control still shown and the Restore button hidden
- expenseVilla → 167:17847 Expense — Villa (402×1598) → pushed Expense detail template shared by every expense and by 06-10: Amount Hero, share card (Your share / Due / group balance), split card, receipt, comments with composer, History rail, and Flag/Delete actions; fixed Push Header with Edit
- expenseComment → 167:20257 Expense — Comment → Expense detail state with the keyboard up and the pinned composer (Typing + send) riding the keyboard; Send posts the comment and notifies everyone
- expenseDelete → 177:28846 Expense — Delete → destructive Overlay / Alert (300×182, radius 34) over the detail; Delete soft-deletes for 30 days and pops, Cancel dismisses
- expenseDisputed → 177:29389 Expense — Disputed → Expense detail state: Inverse 'Disputed' chip plus a Card / Notice (Esha flagged…) with Edit expense / Resolve; black and gray, never red
- recentlyDeleted → 177:29968 Recently deleted → pushed list of soft-deleted expenses in a card of On Card Row / Activity rows with a detail line ('Deleted by Priya on 24 Sep · 24 days left') and a Restore button; purged after 30 days
- notifications → 177:30749 Notifications → pushed inbox from the Home bell, grouped Today/Earlier, with an inline Confirm Payment card, unread dots, Mark all read, and rows linking to Record payment, Insights and the Remind sheet
- lockConfirmRequest → 186:6896 Lock screen — Confirm request → system push 'Payment to confirm' / 'Esha says she paid you ₹700 for Dinner at Olive Garden.': tap opens Activity, actions Confirm / Not received (real local notification category)
- lockReminder → 186:26798 Lock screen — Reminder → system push 'Payment reminder' / 'You owe Kabir ₹1,400 for Goa Trip. It’s due Friday.' at 9:00 pm, two days before the due date: tap opens Record payment to Kabir
**Summary:** The full spec for page 09 Activity is written to screens-activity.md. It covers all 10 frames at the same depth as screens-home.md.

What it contains:
- **Layout:** frame-coordinate geometry for every element, with tokens and text styles.
- **Designer notes:** every canvas caption, section title and subtitle, and dev-mode annotation, copied programmatically so the text is exact.
- **Sample data:** all of it, including content below the fold (Sun 27, Sat 26 and Fri 25 Sep rows; the Villa receipt, comments, history and actions).
- **Navigation:** every Figma link. URL links were resolved to their real targets:
  - Cooking gas draft → Recurring — Flat 302 (177:29224)
  - Insights segment and Monthly summary → Insights — September (167:17585)
  - Goa Trip chip and balance row → Group — Goa Trip (167:18792)
  - Payment reminder → Record payment — Kabir (167:13635)
  - Tab bar → Home, Groups and Profile
  - The overlay-helper frames are mapped to the real page 08 Not received and Remind sheets and the Home Add sheet.
- **Timeline events and copy templates:** each event type with its title, subtitle and amount rules. Amounts are the unsigned total, black if you paid it, gray if someone else did. Also covered: day grouping, pending claims pinned on top, the reminder schedule (2 days before the due date, on it, then every 3 days while overdue, at 9:00 pm), and what goes on the timeline versus the inbox.
- **Expense detail template:**
  - share-card row rules and split ordering (payer first, then You, then the other members)
  - the receipt, comment and history-rail specs, with history copy templates
  - the three states: comment (pinned composer), delete (30-day soft delete) and disputed (Resolve / Edit, never red)
  - how 06-10 "Expense added" reuses it
- **Recently deleted:** the 30-day retention math.
- **Notifications inbox:** the Today/Earlier groups, unread and Mark all read, the inline Confirm card and its Confirmed state, and inbox copy templates.
- **Lock-screen pushes:** turned into real local-notification specs, with iOS notification categories and Android channels, actions, deep links, scheduling and debug triggers.
- **Also:** a reuse map, full specs for two new components (Row / Comment and Row / History), and a demo-data section. The data checks out: the Goa Trip balance of −₹1,400 equals ₹6,500 paid minus the ₹7,900 share.

Art:
- The Activity empty state is the same Figma component as Home's first-day card, so it reuses paybak-homefirstday.riv (confirmed visually too).
- The Art / Receipt thumbnail is exported as assets/images/receipt-thumb.svg.
- No new icons were needed; every icon on the page already exists.

References:
- All 10 refs are exact 2× crops of section exports (download_assets at scale 2; the export's 40 pt padding is cropped out).
- Each frame was read from a full Plugin-API node-tree dump (hash-verified; not kept).
**Issues:**
- get_design_context was NOT called for any of the 10 frames. The account hit the Figma MCP Education-plan quota, so the spec comes from read-only use_figma node-tree dumps (depth 7, with bound tokens, text styles, component properties, reactions and annotations), not generated React. They're more exact.
- The 2x refs were made from download_assets exports of the three sections at scale 2 and cropped to each frame, not from per-frame get_screenshot calls. That cut the call count from 10 to 3. The sizes are verified (804×1748; Villa 804×3196).
- use_figma return values are truncated at about 20 KB. The dump was LZ-compressed and base64-chunked across two calls, and the decoded hash matched. The first call's regex mangled URL-reaction targets; a second call resolved them properly.
- Row / Activity has changed since components-home.md: titles wrap to 2 lines, notification subtitles wrap to 3, and an Unread dot can sit after the date (the date then ends at x362). components-app.md should list this under 'Changes to existing components'.
- New components Row / Comment and Row / History aren't described anywhere else, so I specced them in screens-activity.md §9. Card / Confirm Payment, Header / Amount Hero, Row / Person, Card / Notice, Row / Setting, Push Header, Alert, Composer and Toast rely on components-app.md, which was written in parallel. §9 summarises the geometry used on these frames in case that file is incomplete.
- assets/images/receipt-thumb.svg (Art / Receipt Size=Thumb) may duplicate the Art / Receipt export made for components-app.md (INDEX-v2.md). Keep one.
- Open product questions (the spec marks these as proposals): the pronoun in 'Esha says she paid you' (the data model needs a per-person pronoun, default 'they'); the rule for 'You owe ₹X' vs 'Your share ₹X' on timeline rows; toasts that aren't designed ('Expense deleted', 'Expense restored'); undesigned flows (the Flag an issue sheet, a receipt viewer, the Recently deleted empty state, 'Remove flag' for the flagger, tap targets for most timeline and inbox rows); and showing the Restore header button only when Recently deleted isn't empty (inferred from the Empty frame).
- The component descriptions for PBAmountHero, PBConfirmPaymentCard, PBPersonRow, PBNoticeCard and PBReceiptThumbnail came from Figma dumps made for other specs. The Figma file was only read.

## spec:projects
**Files:** screens-projects.md, ref/projectDrone.png, ref/projectOverBudget.png, ref/projectAddComponent.png, ref/projectSettings.png, ref/projectClosed.png, ref/projectArchived.png, ref/projectDrone_1x.png, ref/projectOverBudget_1x.png (plus the node dumps used while writing, not kept)
**Screens:**
- projectDrone → 167:12476 (402×1749) → project detail, Active/on track: title row, budget card with projection, components list (Planned/Bought/Done), paid vs fair share, History, who owes whom, pinned Add component
- projectOverBudget → 167:20507 → same screen over budget: Dev bought the GPS module for ₹9,500, spent ₹61,500 of ₹60,000, bar State=Over with a budget mark, red alert and '₹1,500 over budget'
- projectAddComponent → 167:21585 → Add component sheet (Sheet / Container Medium) over the dashboard: Name, Estimated/Actual cost, Status Planned/Bought/Done, Paid by, Add receipt, disabled button until a name is entered
- projectSettings → 177:25664 → Project settings: contribution rule Equal/Percent/Fixed, members at 25%, Add member, Budget, Collect money upfront toggle, Close project (opens the Overlay / Alert 'Close Build a Drone?')
- projectClosed → 177:26392 → same detail screen, Closed: read-only notice, budget '₹8,000 under budget', final settle-up plan 'Rohan pays Dev ₹8,500' / 'Priya pays Dev ₹4,000'
- projectArchived → 177:27619 → same detail screen, Archived (Hackathon Kit): 'Archived' tag, Read-only notice, ₹18,400 of ₹20,000 (92%), 'Everyone is settled', Members all Settled
**Summary:** I wrote screens-projects.md for page 10 Projects (77:104). The six frames are one project detail screen shown in four states (Active on track, Active over budget, Closed, Archived), plus the Add component sheet and Project settings. There are no Overlay helper frames on this page, and the Close project alert isn't drawn as a frame, so it's specified from the designer note and annotation (§6.6).

What the spec covers:
- **Business rules (§1), turning the designer notes into formulas:**
  - Spent counts only Bought and Done parts. The projection adds Planned estimates (₹52,000 + ₹6,000 = ₹58,000).
  - Percent used and "left" / "under budget" / "over budget" come from spent against the budget. Only the bar's overflow segment and the warning are red.
  - Fair share uses actual costs only. Equal gives ₹13,000 each; Percent and Fixed are covered too, with a paise-rotation rounding rule.
  - Each person's net is what they paid minus their share, adjusted by confirmed payments. The share rows are ordered by net, and the mini-bar and fair-share mark maths is spelled out.
  - Who owes whom uses the simplify-debts routine: Rohan → Dev ₹8,500 and Priya → Dev ₹4,000, with "owes" while Active and "pays" once Closed.
  - Component lifecycle: new parts start Planned with you as payer, then Bought, then Done, with the row styling for each and the list order.
  - Close and archive: Close project shows an alert that stays black (not destructive). Closing locks components and drops the projection. The project archives automatically once every transfer in the final plan is confirmed.
- **Demo dataset (§2):** it reproduces every Figma figure by calculation, with dates relative to Wed 30 Sep 2026. It covers Build a Drone plus its over-budget and closed variants, and Hackathon Kit. Hackathon Kit's components and payments aren't in Figma, so those records are my proposal.
- **Per-screen specs (§3–§8):** exact frame rectangles, components and variants, text styles, colour tokens and icons. Each screen also has designer notes copied verbatim (captions, section notes and the two annotations), sample data, scroll and keyboard behaviour, and navigation built from the prototype links.
  - Dashboard back and archived back go to groupsList (167:14881).
  - The gear pushes Project settings (350 ms).
  - Add component slides up from the bottom (300 ms); the scrim and ✕ slide it back down (300 ms).
  - Settings back returns to the dashboard.
  - History goes to activityTimeline filtered to the project.
- **Reuse map and component specs (§9):** a reuse map for every element, and full specs, with their Figma descriptions quoted, for the components that aren't in components-core/home: Push Header, Title Row, Card / Budget, Progress Bar (including the over-budget and fair-share mark rules), Row / Setting, Row / Bar, Row / Transfer, Avatar / Pair, Card / Notice and Row / Person.
- **Also included:**
  - A state-difference table (§10).
  - Debug hooks and UI tests (§11).
  - Test IDs (§12).
  - Assets (§13): there's no illustration or Rive art on this page. Every icon and avatar needed already exists, so I exported nothing new.
  - Open questions (§14).
**Issues:**
- The Figma MCP allowance (Education plan) ran out partway through. Once the REST data was available (now .figma-cache/nodes/77-104.json and .figma-cache/renders/), it was used instead.
- get_design_context succeeded only for 167:12476. The server cut its response at about 25k tokens, inside the Top bar, and dropped the screenshot. That output, with the complete REST node tree appended, was the source for 167:12476. The other five frames (167-20507, 167-21585, 177-25664, 177-26392, 177-27619) were read from REST node-tree dumps (not kept), not get_design_context output. Variable IDs were resolved to token names, except the Apple kit variables.
- get_screenshot won't upscale past 1×: maxDimension 3498 still returned 402×1749. The 2× refs in ref/project*.png are the REST 2× renders. The 1× MCP screenshots are kept as projectDrone_1x.png and projectOverBudget_1x.png.
- Design gaps, all marked as proposals in the spec: (1) What 'Done' means compared with 'Bought' is inferred as fitted/finished, with the same money. (2) The Closed and Archived frames draw no Components list or History, but note 10-05 says the GPS module 'stays listed', so I propose showing both read-only. (3) The maths for the Fixed rule and for the pool ('Collect money upfront') isn't designed. (4) Planned rows show no payer, although new parts get 'you as the payer'. (5) Copy is missing for several cases: the Percent/Fixed helpers, footnotes when you're not settled, the empty component list, and the edit/delete component flow.
- Two Figma inconsistencies, resolved as follows: the over-budget warning is drawn in Manrope Bold 14/20, while the Card / Budget description says Subheadline (Medium). The spec follows the drawing. The bar widths on 10-01 use rounded percents (287.1 and 320.1), while 10-05 and 10-06 use exact ratios. The spec uses exact ratios everywhere.

## spec:insights-ai
**Files:** screens-insights-ai.md, ref/insightsSeptember.png, ref/insightsScrolled.png, ref/insightsLocked.png, ref/askStart.png, ref/askAnswer.png, ref/askConfirm.png, ref/scanCamera.png, ref/scanReview.png, ref/scanAssign.png, ref/scanAddExpense.png, ref/recurringFlat302.png, ref/recurringRepeat.png, ref/recurringEnterAmount.png, assets/images/receipt-leopold-cafe@3x.png, assets/images/receipt-paper.svg (plus the node dumps used while writing, not kept)
**Screens:**
- insightsSeptember → 167:17585 → Activity tab, Insights segment (Pro): month row, hero card (your share ₹23,300, trend badge, 6-month bar chart from zero), By category share-bar rows; spec §2.2
- insightsScrolled → 167:20007 → same screen scrolled 676 pt: large title collapsed to the blurred inline 'Activity' bar, Who you spent with (Groups | Friends bars), Lent vs borrowed since April card, footnote; spec §2.4
- insightsLocked → 167:21336 → free-plan state: report at opacity 0.4 + blur 16 under a white 60% overlay, centred Card / Notice 'Insights are part of Paybak Pro' with See Pro → paywall 12-01; spec §2.5
- askStart → 167:13148 → Ask Paybak full-screen chat modal (from Home sparkle): greeting, 4 suggested prompts card, privacy note, composer with mic; spec §3.2
- askAnswer → 167:14107 → answer to 'Who owes me money?': user bubble, assistant text, Row / Person answer card (Rohan overdue + Priya/Esha/Dev), 'Remind Rohan' chip → Remind sheet 08-07; spec §3.3
- askConfirm → 167:15071 → drafted expense 'Cab ₹600' card (Pending: Save/Edit; Saved: Expense added + View), top scroll-edge fade; spec §3.4
- scanCamera → 177:25568 → dark full-screen camera over Add expense: glass ✕ and flash, corner guides, hint pill, Upload photo, shutter; spec §4.2
- scanReview → 177:26256 → pushed 'Check receipt' (Pro): thumb + 'We found 6 items.', Merchant/Date, Subtotal/GST/Tip/Total, 6 editable items, 'Looks right'; spec §4.3
- scanAssign → 177:26997 → pushed 'Assign items' (Pro): per-item person chips, shared captions, pinned Card / Person Totals (₹989/₹621/₹690, tax+tip proportional), Continue; spec §4.4
- scanAddExpense → 177:28338 → the normal Add expense form prefilled from the scan (₹2,300, Lunch at Leopold Cafe, Split Itemized · 3 people, Receipt Attached); spec §4.5
- recurringFlat302 → 177:29224 → pushed 'Recurring' list for Flat 302: Needs your amount (Cooking gas September draft, Enter amount), Rules card (Rent, Wi-Fi fixed; Cooking gas Varies) with next dates; spec §5.2
- recurringRepeat → 177:30291 → Add expense (Cooking gas, Flat 302) with the Repeat sheet: frequency chips, Day of month 28th, 'Amount changes each time' toggle, next draft Wed 28 Oct, Done; spec §5.3
- recurringEnterAmount → 177:30957 → full-screen modal turning the Cooking gas draft into an expense: Draft badge, ₹0 amount with caret and decimal pad, Paid by/Split/Date (Mon 28 Sep), Add disabled until amount; spec §5.4
**Summary:** Wrote screens-insights-ai.md (≈106 KB, 666 lines) covering all 13 frames of page 11 (Insights ×3, Ask Paybak ×3, Scan receipt ×4, Recurring ×3). For each frame it gives the purpose and container type, a top-to-bottom element list with exact frame geometry, component + variant, text styles and colour tokens, states, scroll and keyboard behaviour, a navigation table (every Figma reaction), verbatim designer notes (per frame and per section, inserted programmatically from the Figma text), and verbatim sample data.

The spec also covers:
- **Pro gating** for each feature (§0.1).
- **Overlay helpers**: which real sheet each helper copies (§0.3).
- **Reuse map** (§1): existing components, the NEW page-02 components specced in components-app.md (Modal/Push Header, Row / Setting, Category Chip, Composer, Amount Display), and 12 NEW components fully described here with their Figma descriptions and geometry: Chart / Monthly Bars, Row / Bar, Progress Bar Small, Card / Notice Centered, Chat / Bubble, Chat / Draft Expense (Pending and Saved), Row / Person Compact, Row / Receipt Line, Row / Assign Item, Card / Person Totals, Control / Shutter, Art / Receipt.
- **Business rules**:
  - Insights aggregation (§2.6): scope, exclusions, month, trend badge, largest-remainder percentages that reproduce Figma's 51%/17%/8%/4%, bar fill from the displayed percentage, groups and Without a group, a Friends rule proposal, lent vs borrowed window.
  - Chart spec (§2.7): axis from zero, tallest month = 120 pt.
  - Collapsing large title (§2.4) and lock/blur recipe (§2.5).
  - Deterministic assistant (§3.6): prompts, the answer template that reproduces the Figma copy verbatim, the expense-draft parser with category keywords, the Save → Saved behaviour.
  - Scan math (§4.6): even split of shared items, tax and tip proportional (860/540/600 × 1.15 → 989/621/690), paise rounding.
  - Recurring rules (§5.5): fixed vs variable drafts, drafts don't count toward balances or Insights, next-date derivation (Thu 1 Oct, Mon 5 Oct, Wed 28 Oct), catch-up generation.
- **Demo-data targets** (§6), including chart months that reproduce the drawn bar heights and 'Up 5%', and the cross-page check Flat 302 = Rent 12,000 + Electricity 450 + Wi-Fi 400.

Refs and assets:
- 13 × 2× references (804×1748) in ref/, cropped from 2× section exports and alignment-checked.
- 14 node-tree dumps (not kept).
- New art: assets/images/receipt-leopold-cafe@3x.png (Art / Receipt Full, 900×1374) and assets/images/receipt-paper.svg. The existing receipt-thumb.svg is reused for the Thumb variant.
- No new icons were needed: all 30 icons on these frames already exist.
- No frame uses Rive art.

**Issues:**
- get_design_context was NOT run for any of the 13 frames. Figma was throttled to about 1 successful call per minute (the Education plan limit), so compact node-tree dumps compressed inside use_figma were used instead (LZW, because output is truncated at 20 KB). They hold all the geometry, tokens, variants and reactions (not kept).
- The 2× refs come from section download_assets exports at scale 2, cropped per frame. get_screenshot returns 1× for these sizes, so it wasn't usable for 2×. The three Insights refs come from an earlier, interrupted read of this page and were checked against Figma.
- Decision needed: is Ask Paybak Pro-only? It isn't stated in Figma. The spec defaults to Pro (the sparkle opens the paywall for free users) because every Ask frame says 'Shown as a Pro member'. Check this against the paywall (12-01) feature list.
- Not designed, so the spec proposes it: the Insights Friends breakdown rule (each expense share split evenly across the other participants so bars sum to 100%), empty and month-range behaviour, assistant answers for the three other prompts plus the fallback, the unassigned-items and 'Couldn’t read this receipt' copy, Weekly/Yearly/Custom repeat details, the Save state of a variable rule at ₹0, and the entry point to the Recurring list.
- Some Figma prototype links point at unrelated frames: Ask ✕ and the Scan Add-expense ✕ link to Home, and the Recurring back button links to Activity › Timeline 167:14361. The spec says to dismiss or pop to the real previous screen.
- The monthly chart values for May to August are proposals (₹21,950 / ₹19,600 / ₹20,600 / ₹22,200). Figma only states Apr ₹18,400 and Sep ₹23,300 plus the bar heights. The chosen values reproduce the drawn heights and the 'Up 5% from August' badge exactly.
- Several components used here (Row / Person, Card / Notice, Progress Bar, Chat components, receipt rows) are also documented in components-app.md. My file describes them fully and says components-app.md is canonical where both exist.

## spec:settings-pro
**Files:** screens-settings.md, ref/paywall.png, ref/paywall_1x.png, ref/proWelcome.png, ref/proWelcome_1x.png, ref/paymentDetails.png, ref/paymentAddUpi.png, ref/paymentAddUpiError.png, ref/settingsCurrency.png, ref/settingsNotifications.png, ref/privacyData.png, ref/privacyExport.png, ref/privacyDeleteBlocked.png, ref/helpFeedback.png (plus the node dumps used while writing, not kept)
**Screens:**
- paywall → 167:13003 → Full-screen Pro paywall: 5 Pro features, Yearly ₹799/year (₹67/month, Save 33%, only plan with the 7-day trial, selected by default) vs Monthly ₹99/month (Billed monthly, CTA 'Subscribe for ₹99/month'); the CTA sets a mock entitlement
- proWelcome → 167:13888 → Trial-started confirmation ('Your free trial ends Wed 7 Oct. Then ₹799/year.'), 'Now unlocked' check list, art = paybak-allset.riv at 241.33×200 (2/3 scale); Done returns to the gated feature or Profile
- paymentDetails → 167:14684 → Pushed from Profile: methods (arjun@okaxis 'UPI · Primary', HDFC Bank ···· 4821 'Bank transfer', Add payment method), 'Show to friends' toggle, the 'What friends see' preview card, the 'Paybak never moves money' info line
- paymentAddUpi → 167:15962 → Medium sheet 'Add payment method' (UPI ID | Bank account) over Payment details, focused UPI field prefilled 'arjun@okhdfcbank', Email keyboard, Save
- paymentAddUpiError → 167:18527 → Error state of the same sheet: 'arjunokhdfcbank', red ring, alert icon + 'Enter a UPI ID like name@bank' (Caption/1, no period); the error clears on edit and Save stays enabled
- settingsCurrency → 176:17773 → Pushed: default currency (INR / 'Indian rupee'), 'Keep balances per currency' toggle (Off: convert at the rate saved with each expense), exchange-rate info
- settingsNotifications → 176:18371 → Scrolling pushed screen: 6 push-type toggles (all On), reminder schedule checks (2 days before / On the due date / When overdue, every 3 days), Muted friends 'None'
- privacyData → 176:19720 → Pushed: Discovery toggles (Find me by phone or email, Contacts sync), Export records (black Pro badge → paywall on the free plan), Recently deleted '1 item' (→ 09-07), red Delete account
- privacyExport → 176:20773 → Pro export: PDF|CSV, range chips (This month = '1 Sep – 30 Sep 2026' / Last 3 months / All time), group checklist ticked only if it has records in range, Select all, Export → share sheet
- privacyDeleteBlocked → 177:24246 → Alert 'Settle up first' over Privacy: 'You still owe ₹1,850 and are owed ₹2,900. Settle every balance before deleting your account.' Not now / Settle up (→ 08-03)
- helpFeedback → 177:24773 → Pushed: 5 FAQ rows (questions verbatim; answers not designed, proposals given), Contact us, Rate Paybak, version 'Paybak 1.0 (1)' taken from the build
**Summary:** I've written screens-settings.md, the implementation spec for all 11 frames on page "12 Settings & Pro", plus a 2× reference image for each frame. No new icons or art were needed.

**How the spec is built.** Every number comes from two read-only node dumps of all 11 frames (layout, colour tokens, text styles, component variants, prototype links, component descriptions). (Dumps A and B; not kept.) The 2× refs are crops of 2× section exports. All 11 match the REST renders (mean difference ≤ 0.08/255).

**What the spec contains, per frame:** container type, header, a top-to-bottom element list with geometry, reuse map, states, navigation, scroll and keyboard behaviour, test IDs, sample data and debug start screens. It also covers:
- **Designer notes:** every page-12 note verbatim, checked against designer-notes.md. Related notes from other pages are also quoted (verbatim): what is Pro-gated, muting a friend from their page, Recently deleted.
- **New components:** full definitions for the six components not yet in the existing specs (Row / Setting, Push Header, Modal Header, Card / Plan, Category Chip, Overlay / Alert), plus the kit toggle.
- **Plans and prices:**
  - Yearly: ₹799/year, "₹67/month", "Save 33%", selected by default, the only plan with the 7-day trial.
  - Monthly: ₹99/month, "Billed monthly", CTA "Subscribe for ₹99/month".
- **Pro-gated features:** AI assistant, Receipt scanning (reading the items; attaching a photo stays free), Insights charts, Recurring expenses, PDF/CSV export.
- **Trial:** ends start of today + 7 days (Wed 30 Sep → Wed 7 Oct). Done returns to the feature that opened the paywall, or to Profile.
- **Payment methods:** one primary method; friends see only the primary, and only while "Show to friends" is on.
- **Notifications:** six push-type toggles, all On by default. Default reminder schedule: 2 days before, on the due date, then every 3 days while overdue. It applies to anything with a due date, including loan installments (checked against Rohan's Movie tickets: Fri 25 Sep, Sun 27 Sep, Wed 30 Sep).
- **Export:** PDF or CSV; This month / Last 3 months / All time; a group starts ticked only if it has records in the range; Export opens the share sheet.
- **Delete account:** blocked while any balance is open, using the Home totals.
- **FAQ:** the five questions verbatim.

**Art:** the Welcome illustration is the All-set art at exactly 2/3 scale, so it reuses paybak-allset.riv (checked against ref/allSet.png).
**Issues:**
- The Figma MCP was throttled to roughly one successful call per minute (Education plan), so this page was read mostly through batched node dumps.
- No per-frame get_design_context output was saved for these 11 frames; it adds little over a thorough use_figma dump and the node JSON. Dumps A and B served as the raw reference; the only get_design_context output was the sparse section metadata of 167:12467.
- Not designed in Figma; the spec proposes behaviour for each (marked 'proposal'): Monthly small print and Welcome text, the paywall when already Pro, Restore purchases, tapping an existing payment method, the 'Show to friends' Off preview, the Bank account tab fields, UPI validation beyond the missing '@', a currency picker from Currency, the Muted friends list, Last 3 months / All time ranges, PDF/CSV contents, deleting when nothing is owed, the FAQ answers.
- Needs a decision: 'Contact us' has no support email address in Figma.
- The Row / Setting description says Check rows are single-select, but both check lists on this page (reminder schedule, export groups) are multi-select; the spec treats them as multi-select.
- Figma's UPI error copy is 'Enter a UPI ID like name@bank' (no period, Caption/1 with a red alert icon). It replaces the README §5.1 #6 proposal, which had a period (the README lists it as superseded).
- Row / Currency on the Currency screen shows the code as the title ('INR') and 'Indian rupee' as the subtitle, the reverse of Setup 2 ('Indian Rupee' / 'INR'). Each screen follows its own copy.
- The Off toggle track is an iOS kit colour at 30 %; the spec gives the measured on-card colour #BEBEC0 for Android.
- The paywall_1x.png and proWelcome_1x.png refs are extra 1× fallbacks.
