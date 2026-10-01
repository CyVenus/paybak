# Designer notes (verbatim), per page and section

Every TEXT note placed directly in a Figma section on pages 02 and 04–12 of file `2SPNUpHlG8bCO62YfwRuRi`, copied verbatim from the REST export of 30 Sep 2026. `[section note]` = a note for the whole section; `[under «frame» (id)]` = the caption under that frame. Page 03 (Launch & Onboarding) notes are quoted in `screens-launch.md`, `screens-signin.md` and `screens-setup.md`. `tools/fetch_figma.py` writes a fresh copy to `.figma-cache/notes.md` (its frame attribution is a geometric guess, so compare by text).

## Icons (5:2)
- [section note] Icons
- [section note] HugeIcons stroke-rounded (MIT) · 24pt grid · 1.5 stroke · colour bound to color/icon/primary. Apple (filled), the 4-colour Google G and the WhatsApp logo (share sheet only) are brand exceptions.
- [section note] Home
- [section note] Groups
- [section note] Plus
- [section note] Activity
- [section note] Profile
- [section note] Bell
- [section note] Chevron Right
- [section note] Chevron Left
- [section note] Settings
- [section note] Mail
- [section note] Receipt
- [section note] Food
- [section note] Bolt
- [section note] Wallet
- [section note] Money In
- [section note] Money Out
- [section note] Exchange
- [section note] Lend
- [section note] Calendar
- [section note] Check
- [section note] Check Circle
- [section note] Close
- [section note] Alert
- [section note] User Add
- [section note] People
- [section note] Apple
- [section note] Google
- [section note] Search
- [section note] Camera
- [section note] Copy
- [section note] Car
- [section note] Bed
- [section note] Ticket
- [section note] Shopping Bag
- [section note] Tag
- [section note] Split
- [section note] Note
- [section note] Arrow Right
- [section note] Arrow Up
- [section note] Sparkles
- [section note] Plane
- [section note] Drone
- [section note] Package
- [section note] QR Code
- [section note] Scan
- [section note] Link
- [section note] Share
- [section note] Logout
- [section note] Repeat
- [section note] Flag
- [section note] Delete
- [section note] Restore
- [section note] Chart
- [section note] Mic
- [section note] Image
- [section note] Flame
- [section note] Wi-Fi
- [section note] Crown
- [section note] Bank
- [section note] Download
- [section note] Star
- [section note] WhatsApp

## Illustrations & Art (7:2)
- [section note] Illustrations & Art
- [section note] Open Peeps + Open Doodles by Pablo Stanley (CC0). Recoloured to the illustration tokens: line #0A0A0A, fill white, tint #EBEBEB. Compose scenes from these components — never draw new people.
- [under «Art / Peep Head / Arjun» (7:5)] Arjun
- [under «Art / Peep Head / Priya» (7:22)] Priya
- [under «Art / Peep Head / Rohan» (7:37)] Rohan
- [under «Art / Peep Head / Esha» (7:52)] Esha
- [under «Art / Peep Head / Dev» (7:67)] Dev
- [under «Art / Peep Head / Kabir» (84:667)] Kabir
- [under «Art / Peep Head / Meera» (84:669)] Meera

## Brand (8:7)
- [section note] Brand
- [section note] Geometric “P” monogram, white on a black squircle (22.37% radius, 60% corner smoothing). Wordmark: Manrope ExtraBold, −3% tracking.

## Buttons (9:9)
- [section note] Buttons
- [section note] Pill buttons: Large 52pt (one primary per screen) and Small 36pt (in rows and cards). Primary = black. Secondary = gray on white. On Card = white on #F5F5F5 cards. Destructive = red, only for delete and sign out.
- [section note] Primary
- [under «Button / Primary» (9:36)] Secondary
- [under «Button / Primary» (9:36)] On Card
- [under «Button / Primary» (9:36)] Destructive
- [under «Button / Primary» (9:36)] Text
- [under «Button / Primary» (9:36)] Icon
- [section note] Add

## Badges & Avatars (11:19)
- [section note] Badges & Avatars
- [section note] Badges are 24pt pills in Caption/1 Bold. Red Overdue is the only coloured badge. Avatars use Open Peeps heads on #F5F5F5, with initials or an icon as fallbacks.
- [section note] Badge
- [under «Badge / Pill» (11:46)] Avatar
- [section note] Avatar stack

## Controls (12:215)
- [section note] Controls
- [section note] Page dots for onboarding, a segmented control for filters (Groups | Friends), and input fields on #F5F5F5 with a black focus ring. Errors use the red accent.
- [section note] Page dots
- [section note] Segmented
- [section note] Divider
- [under «Control / Page Dots» (12:230)] Input field

## Cards & Rows (13:220)
- [section note] Cards & Rows
- [section note] Cards are #F5F5F5, 20pt radius, no borders, no shadows. On a card, nested pills and avatars switch to white. Owed amounts are bold black (+₹), amounts you owe are gray (−₹), and only overdue uses red.
- [section note] Section header
- [section note] Balance card
- [section note] Balance summary
- [under «Row / Section Header» (13:223)] Attention row
- [under «Card / Balance» (13:269)] Activity row
- [section note] Empty state

## Navigation (17:458)
- [section note] Navigation
- [section note] Floating Liquid Glass tab bar: Home · Groups · ＋ · Activity · Profile. The active tab gets a soft 6% pill, inactive tabs are gray. The Home header pairs the logo with a glass bell (black unread dot).
- [section note] Onboarding top bar
- [section note] Nav header
- [under «Navigation / Onboarding Top Bar» (17:461)] Tab bar item
- [under «Navigation / Nav Header» (17:494)] Tab bar (on #F5F5F5 to reveal the glass edge)

## Sheets (17:620)
- [section note] Sheets
- [section note] Floating sheet inset 8pt, 40pt corners, white, on a 40% scrim. The grabber and glass close button are Apple iOS 27 kit instances. Rows: 44pt icon tile, title and subtitle, chevron.
- [section note] Action row
- [section note] Action sheet (on #F5F5F5 to show its edge)

## Sign-in & Setup (36:590)
- [section note] Sign-in & Setup
- [section note] Components for sign in and first-run setup: step header with progress, 6-digit code input, avatar picker option, currency row and payment preview.
- [section note] Setup header
- [section note] Code digit
- [under «Control / Code Digit» (36:675)] Code input
- [under «Navigation / Setup Header» (36:666)] Avatar option
- [section note] Currency row
- [section note] Payment preview

## Shared (from Profile) (90:667)
- [section note] Shared (from Profile)
- [section note] Moved from 05 Profile with their node IDs kept, then extended additively. Page 05 instances stay linked and look the same.
- [section note] Settings row
- [section note] Push header
- [section note] Alert
- [under «Overlay / Alert» (102:1115)] Icons
- [under «Icon / Shuffle» (64:3957)] Shuffle
- [under «Icon / Lock» (64:3963)] Lock
- [under «Icon / Help» (64:3969)] Help
- [under «Navigation / Push Header» (97:1082)] Category chip

## Forms & Money (115:849)
- [section note] Forms & Money
- [section note] Modal header, amount entry, payment parties, split rows and the split total bar, paywall plan cards, the multi-line text area and the composer (comments and Ask Paybak). Amounts use Amount/Display; red appears only on errors.
- [section note] Modal header
- [section note] Text area
- [section note] Composer (Pinned=True: the white 402-wide keyboard bar with a top divider)
- [under «Navigation / Modal Header» (115:886)] Amount display
- [section note] Split total
- [section note] Payment parties
- [under «Navigation / Modal Header» (115:886)] Split person row
- [under «Card / Split Total» (125:1170)] Plan card

## Lists & Detail (116:872)
- [section note] Lists & Detail
- [section note] People, group and transfer rows, title and amount heroes, notices, the confirm payment and QR code cards, and the comment and history rows of the expense detail. Nested avatars, badges and buttons are exposed.
- [section note] Avatar pair
- [section note] Comment row
- [section note] History row
- [under «Avatar / Pair» (116:1005)] Person row (Compact variants are drawn for #F5F5F5 cards: their white avatar circles, pills and buttons are invisible on this white section)
- [under «Avatar / Pair» (116:1005)] Title row
- [under «Row / History» (116:1058)] Transfer row
- [section note] Confirm payment card (Confirm → Confirmed, smart animate 250 ms)
- [under «Avatar / Pair» (116:1005)] Amount hero
- [under «Card / Confirm Payment» (129:2060)] QR code card (scans: https://paybak.app/i/arjun)
- [under «Avatar / Pair» (116:1005)] Notice card
- [under «Card / QR Code» (130:1912)] Group row

## Progress & Charts (116:1060)
- [section note] Progress & Charts
- [section note] Progress bars in two heights (Small 6, Large 12), share-bar rows, budget and loan cards and the monthly chart, in chart grays. Red marks only over budget. Bar lengths and chart heights are padding overrides.
- [section note] Progress bar
- [section note] Show mark=true (examples)
- [under «Control / Progress Bar» (116:1099)] Bar row
- [under «_Example · Progress Bar marks» (116:1101)] Monthly bars chart
- [under «Control / Progress Bar» (116:1099)] Budget card
- [under «Chart / Monthly Bars» (143:2157)] Loan progress card
- [under «_Example · Progress Bar marks» (116:1101)] Example: paid back (Kabir, 06-15)
- [section note] Example: over budget (10-03)

## Assistant & Scan (117:958)
- [section note] Assistant & Scan
- [section note] Ask Paybak chat bubbles and the draft expense card, receipt review and item assignment rows, and the camera shutter (fallback: the iOS 27 kit has no camera chrome).
- [section note] Chat bubble
- [section note] Receipt line
- [section note] Shutter (fallback)
- [under «Chat / Bubble» (117:971)] Draft expense card (Save → Saved, smart animate 250 ms)
- [section note] Assign item row
- [under «Control / Shutter» (117:1001)] Person totals card

## Feedback & Overlays (118:962)
- [section note] Feedback & Overlays
- [section note] The toast (black capsule, no shadow) that confirms a save, and the sheet container: kit grabber, title, glass xmark on the right, optional search and a native Content slot.
- [section note] Toast
- [section note] Sheet container
- [section note] Example: populated Content slot (Show search=true)

## Home (24:2)
- [section note] Home
- [section note] Home: active · confirm payment · first day · all settled · ＋ Add sheet, plus the prototype overlay. Tab bar: Home · Groups · ＋ · Activity · Profile.
- [under «Home — Active» (24:5)] Owed and owing at a glance, what needs attention next, and recent activity. Content scrolls under the glass tab bar.
- [under «Home — Confirm payment» (167:11424)] When a friend records a payment to you, a confirm card appears above the balances, and Home keeps +₹2,900 until you confirm. The card pushes Recent activity under the glass tab bar.
- [under «Home — First day» (24:326)] New account. One clear next step: add an expense or invite friends.
- [under «Home — All settled» (24:414)] Nothing owed either way. A calm confirmation — no extra actions.
- [under «Home — ＋ Action sheet» (24:520)] Tap ＋ in the tab bar. Floating sheet over a 40% scrim; ✕ or the scrim closes it.
- [under «Overlay — Add sheet» (24:808)] Prototype overlay target: transparent frame with its own scrim, opened by ＋.

## Customize Avatar (53:893)
- [section note] Customize Avatar
- [section note] Profile → Edit avatar. Boy | Girl switch, category chips, 3-column part grid, live preview. Rive file `avatar.riv` drives the same parts (ViewModel `Avatar`). iPhone 17 Pro, 402 × 874.
- [under «Profile» (64:4316)] Profile tab. Tap the avatar or Edit avatar → Edit avatar (push). The Pro entry point is the top of the settings card, shown on the free plan. After subscribing, the badge is replaced by the value “Active”.
- [under «Edit avatar — Boy · Hair» (64:4503)] Boy | Girl switch keeps each character’s picks. Tiles preview the current look with each option.
- [under «Edit avatar — Boy · Beard» (64:4963)] Beard (boy only).
- [under «Edit avatar — Boy · Eyewear» (64:5384)] Eyewear. None removes glasses.
- [under «Edit avatar — Boy · Outfit» (64:5931)] Outfit. Chips scroll horizontally.
- [under «Edit avatar — Girl · Hair» (64:6721)] Girl has her own item set: hair, accessory, eyewear, eyes, mouth, outfit.
- [under «Edit avatar — Girl · Accessory» (64:7535)] Accessory (girl only). Buns show through the beanie by design.
- [under «Edit avatar — Girl · Outfit» (64:8052)] Save commits the look; Rive avatar.riv renders it in the app.
- [under «Discard alert» (64:8823)] Back with unsaved changes → Discard changes? Keep editing returns; Discard (red) leaves without saving.

## Avatar Parts (53:896)
- [section note] Avatar Parts
- [section note] Every option as a 772 × 842 component in Rive rig space (1:1 with the Rive `avatar` artboard). Characters combine them via instance-swap properties. Source SVGs: _design/avatar/parts.

## Overlay helpers (216:24574)
- [section note] Overlay helpers
- [section note] Prototype-only copy of the Phase 1 ＋ sheet (page 04), so Open overlay works on this page. It isn’t a screen.
- [under «↳ Add sheet (overlay)» (216:24577)] Prototype helper, not a screen: a copy of Overlay — Add sheet 24:808 (page 04). ＋ on Profile opens it with Open overlay, so Profile stays visible under the scrim. Add expense, Record payment, Lend money (IOU) and New group jump to their page 06 flows; the scrim and ✕ close it.

## Add expense (167:21224)
- [section note] Add expense
- [section note] Full-screen modal from the ＋ sheet: amount first on the decimal pad, then people, title and form rows. Pickers open as sheets, the split editor pushes, and Save lands on the new expense with a toast.
- [under «Add expense — Empty» (176:17454)] Amount first: the decimal pad opens with the cursor in ₹0, so a bill takes seconds. Save stays disabled until there is an amount and at least one other person, and the date defaults to Today.
- [under «Add expense — Filled» (176:18002)] The Olive Garden bill ready to save: ₹2,800 split equally between 4 people (₹700 each), and “This weekend” sets the due date to Sun 4 Oct. Repeat turns the expense into a recurring rule; it’s a Pro feature and Arjun is on the free plan, so the row carries a black Pro badge and opens the paywall.
- [under «Split with» (176:19238)] Pick who shares the bill. The people you pick show as chips here and on the form. Guests who aren’t on Paybak yet can be included, and they’re marked Guest.
- [under «Paid by» (176:19877)] Only people on this expense are listed, and picking one closes the sheet. Multiple people pushes a Paid by editor that reuses the split editor’s Exact rows and Card / Split Total.
- [under «Split — Equally» (176:21025)] An equal split of ₹2,800 between 4 people. The footer updates live and stays gray at ₹0 left. When an amount doesn’t divide evenly, the leftover paisa rotate fairly.
- [under «Split — Exact (error)» (177:21514)] State: exact amounts that don’t add up. Dev is mid-edit at ₹550, so the footer turns red with “₹150 left”, and Done stays disabled until the total is ₹2,800.
- [under «Category» (177:21884)] A searchable category list, with Food picked for Olive Garden. The same icons lead expense rows across the app (black line in a #F5F5F5 circle) and label the Insights categories on page 11.
- [under «Currency» (177:22329)] Reuses the Setup currency rows; AED is recent from Dubai Weekend (6–8 Mar). Picking a non-default currency adds a rate line to the form, and that rate is saved with the expense.
- [under «Due date» (177:23763)] The quick chips (Tomorrow · This weekend · Next week) cover most cases, and Pick date opens this calendar; the hint mirrors the default reminder schedule in Settings. The form’s “Today” chip opens the same sheet, titled “Date” with a “Set date” button and no hint, so you can back-date an expense.
- [under «Expense added» (177:24360)] After Save, the modal closes onto the new expense with a short toast; the screen uses the 09-03 Expense detail template. Your share is ₹700, so you’re owed ₹2,100 (₹2,800 − ₹700); the share card keeps the template’s rows with no “You’re owed” row, and the group chip and group balance row are hidden because the dinner isn’t in a group.

## Record payment (177:29215)
- [section note] Record payment
- [section note] Logs a payment made outside Paybak from the ＋ sheet. It stays pending until the other person confirms.
- [under «Record payment — form» (177:29750)] Logs a payment made outside Paybak, prefilled from the open ₹450 with Meera; picking UPI shows meera@okhdfcbank with Copy. A smaller amount records a partial payment and the rest stays owed. Paying more becomes a balance in your favour, and tapping INR shows the amount converted at today’s rate.
- [under «Payment recorded» (177:30023)] The payer’s view until Meera confirms. Pending is gray, not red, and nothing changes yet: Home still shows −₹1,850, and once she confirms you’re settled in Flat 302. If Meera taps Not received, this card switches to Not received with her note.

## Lend money (IOU) (177:29218)
- [section note] Lend money (IOU)
- [section note] Full-screen modal from the ＋ sheet for a direct loan with optional installments. Save lands on the loan: original, paid and remaining, then the schedule. Two states follow.
- [under «Lend money — form» (185:25810)] A direct loan, not a shared bill. Turning on Installments reveals the count, frequency and first due date, and previews the schedule; with it off, a single Due row with quick chips appears instead.
- [under «Loan added» (186:10325)] Original → paid so far → remaining, with a progress bar, then the schedule. Dev sees the same loan from his side and gets a reminder before each due date.
- [under «Loan — Paid back» (186:26377)] State: a loan repaid in full, reached from the loan in Kabir’s friend history (same layout as 07-08). The due dates are derived as monthly on the 12th (12 Jul, 12 Aug, 12 Sep), so the third payment was 2 days late; that’s noted in gray, because red is only for what is overdue now.
- [under «Loan — Installment overdue» (186:26608)] State: time-shifted to Tue 3 Nov. Installment 1 is overdue 4 days, and it’s the only red on screen. Reminders follow the default schedule (2 days before, on the due date, then every 3 days when overdue): Wed 28 Oct, Fri 30 Oct and Mon 2 Nov, so the last one went out Mon 2 Nov.

## New group (190:8353)
- [section note] New group
- [section note] Full-screen modal from the ＋ sheet for a new group or project, switched with a segmented control. Create lands on the new, empty group with a toast.
- [under «New group — Group» (190:8356)] A group for people who share often. Simplify debts is on by default, so settling up takes the fewest payments, and the currency defaults to your INR.
- [under «New group — Project» (190:12242)] The same draft switched to a Project, with an optional description, cover photo, budget and contribution rule. Equal gives each of the 4 members 25%, and Percent or Fixed turns those values into fields; components come after creation, and the budget is left empty to avoid inventing numbers.
- [under «Group created» (190:27177)] The new group opens empty with a toast, and its header, balance card and gear match 07-04 Group detail. The Balances card and the expense list appear after the first expense, and Settle up stays disabled until there’s something to settle.

## Groups tab (167:13142)
- [section note] Groups tab
- [section note] Groups tab: groups and projects list · friends list · empty state. Segmented control Groups | Friends; tab bar Active=Groups.
- [under «Groups» (167:14881)] Groups and projects are in one list, sorted by open balance first, and a project shows its budget bar. “You’re settled” means your part is square even though others still owe inside the project, and archived projects open read-only.
- [under «Friends» (167:15557)] Each friend shows one net across all groups and direct expenses, so this list adds up to the Home totals. Only the overdue badge is red, and guests show “No balance” until you share something with them.
- [under «Groups — Empty» (167:16388)] State: a new account with no groups yet. The same card pattern as Home “First day” points to creating a group or inviting friends.

## Group detail (167:13145)
- [section note] Group detail
- [section note] Group detail: Goa Trip (INR, open balance) · group settings · leave group blocked · Dubai Weekend (AED, settled).
- [under «Group — Goa Trip» (167:18792)] A group shows its total spend, your balance and its due date first, then each member’s paid vs share, then expenses by date. Every net traces back to the six expenses (₹39,500 ÷ 5 = ₹7,900 each), and with simplify debts on, everyone who owes pays Kabir directly.
- [under «Group settings — Goa Trip» (176:18633)] The settings cover members, the group currency, simplify debts (on) and recurring rules. Recurring shows in every group: Goa Trip reads “None”, and Flat 302 reads “3 rules” and opens 11-11 (reached in the prototype from the Cooking gas row on 09-01). Leave group is the only red element. Shown as a Pro member.
- [under «Leave group — Blocked» (176:18995)] State: you tapped Leave group while you still owe money in it. You can only leave once your balance is zero, so the alert offers Settle up (Record payment to Kabir, page 08) instead of a destructive action.
- [under «Group — Dubai Weekend» (176:20314)] A foreign-currency group keeps every amount in AED and shows the ₹ value at the rate saved on the day of each expense, so balances never shift with the market. The total is the sum of the three saved conversions (21,936 + 12,312 + 6,870).

## Friends & add friend (177:25985)
- [section note] Friends & add friend
- [section note] Friends & add friend: Rohan (overdue friend) · add friend · My QR code sheet · Ananya (guest friend).
- [under «_Sheet / My QR code» (177:28072)] _Sheet / My QR code · local component (unpublished) that fills the Content slot of Sheet / Container on 07-10.
- [under «Friend — Rohan» (177:25988)] A friend’s page shows the one net between you, why it’s owed, and the actions that settle it. In the app, Record payment opens the page 06/08 form prefilled “Rohan → You · ₹800” and Movie tickets opens the 09-03 expense template; neither is drawn. Automatic reminders follow the default schedule, so Rohan was reminded Fri 25 Sep, on the due date (Sun 27 Sep) and today. The toggle mutes them for Rohan only, and Settings › Muted friends (12-07) lists anyone turned off here.
- [under «Add friend» (177:26746)] One screen covers every way to add someone: search, an invite link, QR in both directions, and your contacts. Inviting someone who isn’t on Paybak adds them right away as a guest friend. Usernames follow the @arjun pattern (lowercase first name).
- [under «My QR code» (177:28130)] Your code and link add you on Paybak in one scan or tap. The QR is plain black on white so any camera reads it, with the Paybak mark in the middle.
- [under «Friend — Ananya (Guest)» (177:28684)] A guest is tracked like any friend but can’t see anything until they join. Their history links to their account automatically, so the invite is the only extra step. The tag is gray, not red, because nothing is wrong.
- [under «↳ My QR code sheet (overlay)» (189:6345)] Overlay target for 07-09 “My QR code”: a transparent copy of 07-10 without the Add friend screen, holding only the scrim and the sheet (8 from the bottom). 07-09 opens it with Open overlay, move in from bottom, 300 ms, so the page stays visible under it. ✕ or the scrim closes it. A prototype helper, not a screen.

## Overlay helpers (216:28950)
- [section note] Overlay helpers
- [section note] Prototype-only copies of the Home ＋ sheet and the page 08 Remind sheet. They aren’t screens.
- [under «↳ Add sheet (overlay)» (216:28953)] Prototype helper, not a screen: a copy of Phase 1’s Overlay — Add sheet (page 04). The tab bar ＋ on 07-01 and 07-02 opens it with Open overlay, move in from bottom, 300 ms. Its rows open Add expense, Record payment, Lend money (IOU) and New group on page 06; the scrim or ✕ closes it.
- [under «↳ Remind sheet (overlay)» (216:28961)] Prototype helper, not a screen: a copy of 08-07s Remind sheet (page 08) on a transparent 402×874 frame with its own scrim, the sheet 8 from the bottom. Remind on 07-08 opens it with Open overlay, move in from bottom, 300 ms, so Rohan’s page stays visible under it. ✕, Send in Paybak or the scrim closes it.

## Balance breakdowns (167:11699)
- [section note] Balance breakdowns
- [section note] The screens behind the two Home balance cards: who owes you, and who you owe.
- [under «You’re owed — Breakdown» (167:11705)] Tapping “You’re owed” on Home lists every person who owes you, overdue first. Only Rohan’s badge is red. In the app every row opens that friend’s page; only Rohan’s (07-08) is drawn.
- [under «You owe — Breakdown» (167:11957)] The hero amount is gray because it’s money you owe. Nothing is red, because nothing is overdue. The footnote explains why you pay Kabir the whole ₹1,400 for Goa Trip.

## Settle up & record (167:11702)
- [section note] Settle up & record
- [section note] The simplified suggestion list, Record payment prefilled for Kabir (UPI, with Copy), and the pending detail the payer sees.
- [under «Settle up» (167:12113)] This is the fewest-payments plan. You make 2 payments and 4 people pay you, with Goa Trip simplified so that you pay Kabir directly. Settle opens Record payment prefilled, and Remind opens the reminder sheet over this list.
- [under «Record payment — Kabir» (167:13635)] Everything is prefilled from the suggestion. With UPI selected, Kabir’s UPI ID shows with Copy. Copy puts kabir@okaxis on the clipboard and shows Overlay / Toast “UPI ID copied” (Check Circle), centred 50 above the bottom, which fades after 2 s. Paybak never moves money: you pay in your own UPI app and save the record here.
- [under «Payment pending» (167:17304)] After Save you land on the payment with a “Payment recorded” toast. It stays “Pending confirmation”, and Home keeps −₹1,850, until Kabir confirms. “Paid to” appears only for methods with an ID (UPI, Bank), so the Cash payment on 06-12 has 6 rows.

## Remind (176:20967)
- [section note] Remind
- [section note] The reminder sheet over Home, the same sheet as an overlay-only frame, and the iOS share sheet.
- [under «_Sheet / Remind Rohan» (176:20970)] Local component (unpublished). It fills the Sheet / Container Content slot on 08-07, 08-07s and 08-08, and on the helper copies “↳ Remind sheet (overlay)” that the integrator pastes onto pages 07, 09 and 11.
- [under «Remind — Rohan» (177:21203)] Remind opens a pre-written message that you can edit, in a Friendly or Neutral tone. Send it as a Paybak notification, or share it through any app. “Send in Paybak” closes the sheet and shows Overlay / Toast “Reminder sent to Rohan” for 2 s (16 above the tab bar on Home, 50 above the bottom elsewhere). The balances don’t change.
- [under «Remind sheet (overlay)» (177:21801)] The Remind sheet on its own. Pages that aren’t Home open it as an overlay over the current screen, so the chat or friend page stays visible under the scrim.
- [under «Remind — Share» (177:23342)] “Share…” hands the same message to the iOS share sheet, so you can send it on WhatsApp, Messages or Mail, or copy it.

## Receiver confirmation (177:24985)
- [section note] Receiver confirmation
- [section note] The Not received sheet over Home, the same sheet as an overlay-only frame, and Home after Confirm.
- [under «_Sheet / Not received» (177:24988)] Local component (unpublished). It fills the Sheet / Container Content slot on 08-10 and 08-10s, and on the helper copy “↳ Not received sheet (overlay)” that the integrator pastes onto page 09.
- [under «Not received» (177:25006)] Not received sends Esha an editable note instead of silently rejecting her payment. Her ₹700 stays owed until a payment is confirmed.
- [under «Not received sheet (overlay)» (177:25168)] The Not received sheet on its own. Activity and the Notifications inbox open it over themselves, so the timeline stays visible under the scrim.
- [under «Payment confirmed» (177:25303)] After Confirm, Esha’s ₹700 is settled. “You’re owed” drops to +₹2,200 from 3 people (Rohan ₹800, Priya ₹700, Dev ₹700), and her payment tops Recent activity.

## Overlay helpers (216:29119)
- [section note] Overlay helpers
- [section note] Copies of overlays from other pages.
- [under «↳ Add sheet (overlay)» (216:29122)] Prototype helper, not a screen: a copy of Overlay — Add sheet (page 04). ＋ on 08-11 opens it with Open overlay. Its rows open the page 06 forms by URL; the scrim and ✕ close it.

## Timeline (167:14358)
- [section note] Timeline
- [section note] Everything that changed, newest first, grouped by day, with the Timeline | Insights switch and an empty state.
- [under «Activity — Timeline» (167:14361)] Everything that changed, newest first, grouped by day; a payment waiting for you sits on top with Confirm and Not received, the Restore button opens Recently deleted, and the Fuel share is ₹2,500 ÷ 5. Rohan’s automatic reminders follow the default schedule (2 days before, on the due date, then every 3 days while overdue), so they went out Fri 25 Sep, Sun 27 Sep and tonight at 9:00 pm.
- [under «Activity — Empty» (167:16311)] State: a new account with no activity yet. The segmented control stays, so Insights is still one tap away.

## Expense detail (167:17844)
- [section note] Expense detail
- [section note] The shared expense detail template (06-10 reuses it): split, receipt, comments, history, delete, dispute and restore.
- [under «Expense — Comment» (167:20257)] Tapping the comment field raises the keyboard and pins the composer above it. Send posts the comment and notifies everyone on the expense.
- [under «Expense — Delete» (177:28846)] Delete always asks first. The expense can be restored from Recently deleted for 30 days.
- [under «Expense — Disputed» (177:29389)] State: Esha flagged your seafood dinner, so it shows as Disputed in black and gray, never red. Resolve keeps the amount, clears the badge, logs “You resolved Esha’s flag” in History and notifies Esha; editing the expense or Esha removing her flag also clears it.
- [under «Recently deleted» (177:29968)] Deleted expenses wait here for 30 days, and anyone in the group can restore them. “24 days left” counts from 30 Sep to 24 Oct; the screen opens from the Activity header icon and from Settings › Privacy & data (12-08).
- [under «Expense — Villa» (167:17847)] Every expense opens this detail: the amount, who paid, the split, the receipt, comments and a full history of edits. Edit opens Add expense in edit mode, prefilled (not linked in the prototype); the comment dates are derived, with Priya asking on 27 Sep and Kabir replying with his 28 Sep edit.

## Notifications (177:30746)
- [section note] Notifications
- [section note] The Home bell opens an inbox grouped Today and Earlier, with payments to confirm handled inline; two lock-screen pushes show a payment to confirm and an automatic reminder.
- [under «Notifications» (177:30749)] The bell opens this inbox, grouped Today and Earlier; payments to confirm can be handled right here, while reminders Paybak sends for you (like tonight’s to Rohan) are logged on the timeline, not here. Times are derived: the Kabir reminder goes out at 9:00 pm, two days before Goa Trip is due, Rohan’s overdue alert came on Mon 28 Sep, and the 8:00 pm summary comes after tonight’s dinner was added, so it counts ₹2,900.
- [under «Lock screen — Confirm request» (186:6896)] State: Esha’s claim arrives as a push at 9:12 pm. Tapping it opens Activity; a long press offers Confirm and Not received.
- [under «Lock screen — Reminder» (186:26798)] State: the automatic reminder two days before Goa Trip is due (Fri 2 Oct), at 9:00 pm (derived). Tapping it opens Record payment to Kabir.

## Overlay helpers (190:27695)
- [section note] Overlay helpers
- [section note] Prototype-only copies of the page 08 sheets and the Home ＋ sheet, so Open overlay works on this page. They aren’t screens.
- [under «↳ Not received sheet (overlay)» (190:27698)] Prototype helper, not a screen: a linked copy of 08-10s Not received sheet (page 08). Not received on 09-01 and 09-08 opens it with Open overlay, so the timeline or the inbox stays visible under the scrim. It is full-screen with its own scrim; the scrim, Send and Cancel close it.
- [under «↳ Remind sheet (overlay)» (190:27725)] Prototype helper, not a screen: a linked copy of 08-07s Remind sheet (page 08). Rohan’s Payment overdue row on 09-08 opens it with Open overlay, so the inbox stays visible under the scrim. It is full-screen with its own scrim; the scrim, ✕ and Send in Paybak close it.
- [under «↳ Add sheet (overlay)» (216:28703)] Prototype helper, not a screen: a copy of Phase 1’s Overlay — Add sheet (page 04). The tab bar ＋ on 09-01 and 09-02 opens it with Open overlay; its rows open Add expense, Record payment, Lend money (IOU) and New group on page 06, and the scrim or ✕ closes it.

## Project dashboard (167:12473)
- [section note] Project dashboard
- [section note] Build a Drone: the tall dashboard (budget, components, paid vs fair share, history, who owes whom) · Add component sheet · over-budget state.
- [under «Add component» (167:21585)] New parts start as Planned with you as the payer, and the button enables once a name is entered. Planned items feed only the projection; everyone’s share changes only when an actual cost is added.
- [under «Project — Over budget» (167:20507)] State: at a later point Dev buys the GPS module for ₹9,500, so spending reaches ₹61,500 of ₹60,000. Only the bar’s overflow and the warning turn red.
- [under «Project — Build a Drone» (167:12476)] The dashboard answers three things at a glance: how much of the budget is gone, what’s still planned, and who owes whom. Fair share uses actual costs only, planned items count only toward the ₹58,000 projection, History opens the Activity timeline filtered to this project (the prototype links to 09-01), and project spending stays out of Insights.

## Settings, closing and archive (177:25562)
- [section note] Settings, closing and archive
- [section note] Project settings with the contribution rule and the close confirmation · the closed project with its final settle-up plan · the archived Hackathon Kit.
- [under «Project settings — Members & rules» (177:25664)] With Equal, each share is read-only; Percent and Fixed turn the rows into editable fields that must add up, and the pool is off, so whoever buys a part pays for it and is paid back. Close project first shows an Overlay / Alert, “Close Build a Drone?” / “Components lock and everyone sees the final plan.”, with Cancel · Close project. Both buttons stay black, because closing isn’t destructive, and confirming leads to 10-05.
- [under «Project — Closed» (177:26392)] State: Build a Drone right after it’s closed. Components lock and the GPS module stays listed as Planned but counts toward nothing, so the budget shows ₹52,000 with no projection; Rohan and Priya can still record their payments to Dev, and once both are confirmed the project archives like Hackathon Kit (10-06).
- [under «Project — Archived» (177:27619)] An archived project is a permanent record: there is no gear and no Add component, and every figure is final. Coming in under budget keeps the bar black.

## Insights (167:12997)
- [section note] Insights
- [section note] The Activity tab with Insights selected: the September report, the rest of it after scrolling, and the locked state free users see.
- [under «Insights — September» (167:17585)] Your share of expenses in groups and with friends, month by month. The chart starts at zero, so a 5% change looks small on purpose. Projects are tracked on their own dashboards. Shown as a Pro member.
- [under «Insights — Scrolled» (167:20007)] The rest of the September report after scrolling. The large title collapses to an inline title, as in iOS. Friends lists each friend with the same bar rows, and every bar shows a share of ₹23,300 (55%, 34% and 11% for the groups). Shown as a Pro member.
- [under «Insights — Locked» (167:21336)] State: Arjun, on the free plan, opens Insights. The charts stay blurred behind the lock, “See Pro” opens the paywall (12-01), and Timeline still works.

## Ask Paybak (167:13000)
- [section note] Ask Paybak
- [section note] A full-screen chat from the Home sparkle button: suggested prompts, an answer from live balances, and a drafted expense that waits for Save.
- [under «Ask Paybak — Start» (167:13148)] Opens from the sparkle button in the Home header. Suggested prompts send in one tap, and the mic dictates into the field without sending. Shown as a Pro member.
- [under «Ask Paybak — Answer» (167:14107)] Answers use your live balances, and the numbers match Home (₹2,900 from 4 people). Action chips open the normal flows, so reminding Rohan uses the usual Remind sheet. Shown as a Pro member.
- [under «Ask Paybak — Confirm» (167:15071)] The assistant drafts the expense as a card (Travel, guessed from “cab”) and never saves without a tap. Unlike forms, which close onto the new item’s detail with a toast, Save keeps you in the chat and turns the card into “Expense added” with View, which opens the expense’s detail. Edit opens the full Add expense form, prefilled. Shown as a Pro member.

## Scan receipt (177:25565)
- [section note] Scan receipt
- [section note] Take a photo of the bill, check what was read, tap who had each item, then save it as a normal expense, prefilled.
- [under «Scan receipt — Camera» (177:25568)] Opens from “Add receipt” in Add expense. Free users can still take or upload the photo and attach it. Reading the items (Check receipt, Assign items) is Pro and opens the paywall. Shown as a Pro member.
- [under «Scan receipt — Review» (177:26256)] You check what was read before anything is split: tap any value to fix it in place, and the items add up to the ₹2,000 subtotal. If nothing can be read, a Card / Notice “Couldn’t read this receipt” offers Retake and Attach photo only. Shown as a Pro member.
- [under «Scan receipt — Assign items» (177:26997)] Tap who had each item. Shared items split evenly (₹240 ÷ 3 = ₹80, ₹270 ÷ 3 = ₹90). Tax and tip (₹300) follow each person’s items, so ₹860, ₹540 and ₹600 become ₹989, ₹621 and ₹690. Shown as a Pro member.
- [under «Scan receipt — Add expense» (177:28338)] The scan ends in the normal Add expense form, filled in, so saving works like any other expense and opens its detail with “Expense added”. Changing the split goes back to Assign items. Shown as a Pro member.

## Recurring (177:29221)
- [section note] Recurring
- [section note] Flat 302’s repeating expenses: fixed rules add themselves on schedule, and variable ones create a draft that waits for your amount.
- [under «Recurring — Flat 302» (177:29224)] Fixed rules add the expense on schedule. Variable rules create a draft that leaves balances alone until you enter the amount, and Cooking gas repeats on the 28th, so its next date is Wed 28 Oct. Shown as a Pro member.
- [under «Recurring — Repeat» (177:30291)] For Pro members, the Repeat row in Add expense opens this sheet; free users get the paywall from that row. Turning on “Amount changes each time” makes the rule create drafts instead of expenses. Shown as a Pro member.
- [under «Recurring — Enter amount» (177:30957)] The September draft Paybak created on Mon 28 Sep. The split follows the rule (equally between the 3 roommates), and nothing counts until you enter an amount. Shown as a Pro member.

## Overlay helpers (216:24639)
- [section note] Overlay helpers
- [section note] Prototype-only copies of the page 04 and 08 sheets, so Open overlay works on this page. They aren’t screens.
- [under «↳ Add sheet (overlay)» (216:24642)] Prototype helper, not a screen: a copy of Overlay — Add sheet (page 04). ＋ on 11-01 and 11-02 opens it with Open overlay. Its rows open Add expense, Record payment, Lend money and New group on page 06; the scrim and ✕ close it.
- [under «↳ Remind sheet (overlay)» (216:24704)] Prototype helper, not a screen: a copy of 08-07s Remind sheet (page 08). Remind Rohan on 11-05 opens it with Open overlay, so the chat stays visible under the scrim.

## Paybak Pro (167:12467)
- [section note] Paybak Pro
- [section note] The paywall opened from Profile or any locked feature, then the welcome screen once the Yearly trial starts.
- [under «Paybak Pro — Paywall» (167:13003)] Paywall, opened from the Profile “Paybak Pro” row or any locked feature (Export records, the Add expense Repeat row, Insights); Arjun is on the free plan and the core ledger stays free. Yearly is selected by default and is the only plan with the 7-day trial, so picking Monthly changes the CTA to “Subscribe for ₹99/month”.
- [under «Paybak Pro — Welcome» (167:13888)] Confirmation after the Yearly trial starts: today (Wed 30 Sep) plus 7 days is Wed 7 Oct. From here Arjun is a Pro member, and “Done” returns to the feature he tapped (Export records in the prototype) or to Profile.

## Payment details (167:12470)
- [section note] Payment details
- [section note] Where friends learn how to pay you: your methods, what friends see, and adding a UPI ID (with its error state).
- [under «Payment details» (167:14684)] Where friends learn how to pay you. Only the primary method is shown to friends, and only while “Show to friends” is on.
- [under «Add UPI ID» (167:15962)] The sheet adds a second UPI ID from his HDFC account. The value arjun@okhdfcbank is derived from the HDFC Bank ···· 4821 method, and the “Bank account” tab reuses this sheet with bank fields (not drawn).
- [under «Add UPI ID — Error» (167:18527)] State: Save was tapped with an ID that has no “@”. The inline error clears as soon as the user edits the field, and Save stays enabled.

## Preferences (176:17770)
- [section note] Preferences
- [section note] Currency and Notifications, behind the Profile rows: default currency, per-currency balances and reminders.
- [under «Currency» (176:17773)] INR is the default for totals. Keeping balances per currency is off, so AED amounts convert at the rate saved with each expense.
- [under «Notifications & reminders» (176:18371)] Every push type can be switched off separately. This default schedule applies to anything with a due date, including loan installments, so Rohan’s Movie tickets (due Sun 27 Sep) got automatic reminders on Fri 25 Sep, Sun 27 Sep and today, Wed 30 Sep.

## Privacy & data (176:19717)
- [section note] Privacy & data
- [section note] Discovery and data controls behind the Profile “Privacy” row: Export records (Pro), Recently deleted, and the blocked Delete account state.
- [under «Privacy & data» (176:19720)] Discovery and data controls on the free plan: Export is a Pro feature marked with a black “Pro” badge, and Recently deleted opens the same screen (09-07) as the Activity header icon. Red appears only on the destructive Delete account row.
- [under «Export records» (176:20773)] An export of September’s records: College Gang (12 Mar) and Dubai Weekend (6–8 Mar) are unticked because they have nothing in this range, and “Without a group” covers the dinner, the movie tickets and the groceries, plus Priya’s ₹1,050 payment. Export opens the iOS share sheet. Shown as a Pro member.
- [under «Delete account — Blocked» (177:24246)] State: Delete account was tapped while balances are open, using the Home totals of −₹1,850 and +₹2,900. Deleting is blocked, and “Settle up” opens the settle-up suggestions (08-03).

## Help (177:24770)
- [section note] Help
- [section note] Common questions, contact and rating.
- [under «Help & feedback» (177:24773)] Short answers to the questions people ask most, plus a way to contact the team and rate the app. The version number is a placeholder that will come from the build.
