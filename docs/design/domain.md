# Paybak domain model, algorithms and demo dataset

The business logic both apps port, and the demo dataset whose numbers must match Figma. Paybak only **records** who paid and who owes; it never moves money.

**Files**
- `domain.md` (this file): entities, invariants, algorithms with worked examples, the demo dataset and its JSON schema, Figma inconsistencies.
- `seed/demo.json`: the demo dataset (what the apps bundle and load). Generated; don't edit by hand.
- `seed/build_demo.py`: writes `demo.json`. Edit the records here, then re-run it and `verify.py`.
- `seed/verify.py`: the **reference implementation**. It loads `demo.json` with the clock pinned to the Figma moment, runs every algorithm below and asserts 135 Figma numbers, strings and rules. When this file and `verify.py` disagree, `verify.py` is right. Section numbers (§) in its comments point here. The unit tests of M2 (both platforms) port its checks.
- `seed/rates.json`: bundled "today's rates" (INR per unit) for new foreign-currency records.

Run (from the repo root): `cd docs/design/seed && python3 build_demo.py && python3 verify.py` → `All 135 checks pass.` (`python3 docs/design/seed/verify.py` alone checks without regenerating.)

Precedence: flow.md > app-architecture.md (structure, ids, routes, ownership, the data-layer shape) > this file (business rules, field names, numbers) > the screen specs. §1.15 lists where this file renames a field sketched in app-architecture §3.2.

---

## 0. Conventions

- **Money** is an integer count of the currency's minor unit (`Int64` / `Long`): paise for INR, fils for AED, whole yen for JPY. Never floating point. Every amount field is named `amount`, `share`, `paid`, `budget`, `estimatedCost`, `actualCost` and is minor units in the record's `currency`.
- **Percent** values are basis points (`2500` = 25 %). **Shares** are whole numbers.
- **Rates** are decimal strings (`"22.85"`) = units of the *to* currency per 1 unit of the record currency, stored as `{ "value": "22.85", "to": "INR" }`. Use `Decimal` / `BigDecimal`.
- **Calendar dates** (an expense's date, due dates, settle-by, installment dues, occurrence dates) are local days (`yyyy-MM-dd` in `ledger.json`). **Moments** (`createdAt`, `confirmedAt`, `sentAt`, `at`, …) are instants (ISO-8601 UTC in `ledger.json`), shown in the device time zone.
- **Ids** are strings. Demo ids are readable (`p-rohan`, `g-goa`, `e-olive`); new ids are lowercase UUIDs. The user is always the person id `"me"`; their name and avatar come from the profile.
- **"You"**: every read model is from the user's point of view. A positive net means *they owe you*.
- **Signs in copy**: `+` for owed to you, U+2212 `−` for what you owe.
- Every time-dependent function takes `now` from `AppClock` (app-architecture §3.8). Nothing reads the system clock directly.

---

## 1. Entities

The ledger is one JSON document (app-architecture §3.2) plus the profile (ProfileStore). The demo file has the same shape plus `anchor`, `profile` and `scenarios` (§7).

### 1.1 Profile (ProfileStore; identity, not ledger)
Existing M1 fields (`name`, `avatar`, `currencyCode`, `upiID`, `notifications`, `signInMethod`, `contact`, `onboardingComplete`) plus, from M2:
| Field | Type | Notes |
|---|---|---|
| `currencyCode` | ISO code | **The default currency** for totals, new groups and new expenses (Settings › Currency edits it). |
| `username` | string | `"arjun"`; shown as "@arjun"; invite link `https://paybak.app/i/{username}`. Default = lowercase first name; collision → append a digit. |
| `pronoun` | `she` \| `he` \| `they` | For copy about the user on friends' devices (simulated). |
| `paymentMethods` | `[PaymentMethod]` | `{id, kind: upi|bank, value? (UPI ID), bankName?, last4?, primary}`. Exactly one primary when non-empty. `upiID` mirrors the primary UPI method (migration: a non-empty `upiID` with no methods becomes the primary method). |
| `showPaymentToFriends` | bool | Default true. Friends see only the primary method, only while on. |
| `avatar` | preset(i) \| photo \| character(look) | The demo uses `preset(0)` (= `avatar-1`). |

### 1.2 Person (a friend or a guest; the user is `"me"`)
| Field | Type | Invariants / notes |
|---|---|---|
| `id` | string | |
| `name` | string | Full name ("Rohan Verma"). The first word is the display name ("Rohan"). |
| `avatar` | string? | Peep-head asset key `avatar-2` … `avatar-7`; `null` = initials of first + last word ("AR"). |
| `upi` | string? | Their primary UPI ID (shown in Record payment and member rows). |
| `username` | string? | Without "@"; `null` for guests. |
| `pronoun` | `she`\|`he`\|`they` | Default `they`. "Esha says **she** paid you". |
| `isGuest` | bool | Not on Paybak. Everything works locally; the Guest tag is gray. "Guest joins" (debug) flips it off. |
| `contact` | string? | Phone or email (guests are identified by it). |
| `remindersMuted` | bool | Mutes **automatic** reminders to this friend only; manual Remind still works. Listed in Settings › Muted friends. |
| `addedAt` | moment | Order friends were added = tie-break order in lists. |

A person is a friend once added (Add friend, Invite → guest, or by being a member of a shared group). People are never hard-deleted while any record references them.

### 1.3 Group (`kind: group`) and Project (`kind: project`)
| Field | Type | Notes |
|---|---|---|
| `id`, `name` | string | |
| `kind` | `group` \| `project` | |
| `type` | `trip`\|`home`\|`friends`\|`other` \| null | Groups only. Icon: trip → `plane`, home → `home`, friends → `people`, other → `tag`. |
| `icon` | icon key | Stored (projects pick `drone`, `package`, …). |
| `currency` | ISO code | Default = profile currency. All amounts in the group are in it. |
| `memberIds` | `[id]` | Ordered; includes `"me"` while you're a member. Order = display order and the simplify tie-break. |
| `simplifyDebts` | bool | Default **true**. Projects always simplify. |
| `settleBy` | date? | "Settle by" (groups). The due date of every debt in the group's plan. |
| `createdAt`, `createdBy` | moment, id | |
| `project` | object? | Projects only: `{description?, coverPhoto?, budget? (minor), contribution: {rule: equal|percent|fixed, values: {personId: bps or minor}}, pool: bool ("Collect money upfront"), status: active|closed|archived, closedAt?, archivedAt?}` |

Invariants: a member can't be removed (and you can't leave) while their net in the group ≠ 0. Archived projects are read-only. Changing the group currency is only allowed while the group has no records.

### 1.4 Expense
| Field | Type | Notes |
|---|---|---|
| `id`, `title` | string | Empty title on save → the category name. |
| `groupId` | id? | `null` = direct, between the people on it. Never a project (projects use components). |
| `category` | category id | §1.12. Default `other`. |
| `amount` | minor | Total, > 0, in `currency`. |
| `currency` | ISO code | Group currency inside a group, else the default currency or any other. |
| `rate` | `{value, to}`? | Required when `currency` ≠ default; the rate on the expense's date, saved once, never recomputed. |
| `date` | date | When it happened (not in the future). |
| `dueDate` | date? | When the others should pay the payer back. Inside a group the effective due date is `dueDate ?? group.settleBy`. |
| `payers` | `[{personId, amount}]` | Σ amount = `amount`. One payer in every designed case. |
| `split` | `{mode, rows}` | mode `equal`\|`exact`\|`percent`\|`shares`\|`itemized`. Row `{personId, included, value, share}`: `value` = exact minor / bps / shares / null; **`share` is the saved result** (minor). Σ share over included rows = `amount`; excluded rows have share 0. §4. |
| `itemized` | object? | Receipt splits: `{items: [{label, amount, personIds}], lines: [{label, amount}] (GST, tip), subtotal}`. |
| `notes` | string? | ≤ 500 characters. |
| `receipt` | `{photo? or asset, addedBy, addedAt}`? | A local JPEG file name (demo: the `receipt-thumb` asset). |
| `recurringRuleId`, `occurrenceDate` | id?, date? | Set when a rule created it. |
| `createdAt`, `createdBy` | moment, id | |
| `history` | `[{kind, at, by, …}]` | Oldest first. Kinds: `created`, `amountChanged {old,new}`, `titleChanged {old,new}`, `dateChanged`, `splitChanged`, `payersChanged`, `categoryChanged`, `receiptAdded`, `flagged`, `flagRemoved`, `flagResolved`, `deleted`, `restored`. The expense's History list = this, newest first. |
| `comments` | `[{id, by, at, text}]` | Oldest first. |
| `flag` | `{by, note, at}`? | "Disputed". A flagged expense **still counts** everywhere. Cleared by Resolve (history `flagResolved`), by any edit, or by the flagger removing it. |
| `deletedAt`, `deletedBy` | moment?, id? | Soft delete: excluded from every calculation; purged 30 days after `deletedAt` (§10). Restore clears both and logs `restored`. |

### 1.5 Payment (a settlement recorded in Paybak; the money moved elsewhere)
| Field | Type | Notes |
|---|---|---|
| `id`, `fromId`, `toId` | string | Payer → receiver. |
| `amount`, `currency`, `rate` | minor, code, `{value,to}`? | Inside a group: the group currency. Other currency → rate saved like expenses. |
| `method` | `cash`\|`upi`\|`bank`\|`card`\|`other` | Labels Cash, UPI, Bank, Card, Other. |
| `date` | date | When it was paid. |
| `groupId` / `loanId` / `expenseId` | id? | The context: `groupId` → counts in that group (or project); `loanId` → a loan repayment; neither → a direct payment between the two people. `expenseId` is only the "for" label ("for Weekend groceries"). |
| `status` | `pending`\|`confirmed`\|`notReceived`\|`cancelled` | **Only `confirmed` changes balances.** |
| `recordedBy` | id | |
| `createdAt`, `confirmedAt` | moment, moment? | |
| `note`, `proof`, `notReceivedNote` | string?, photo?, string? | |

Status rules: a new payment is `pending` unless the person recording it is its receiver (you record that Rohan paid you → `confirmed` at once). The receiver confirms (`confirmed`) or taps Not received (`notReceived` + note; the debt stays). The recorder may cancel a pending payment (`cancelled`; kept for history, excluded everywhere). Overpaying leaves a balance the other way; underpaying leaves the rest owed.

### 1.6 Loan (IOU)
`{id, lenderId, borrowerId, amount, currency, rate?, reason?, date, installments: {count, frequency: weekly|biweekly|monthly, firstDue}?, dueDate?, createdAt, createdBy}`. One of them is `"me"`. Repayments are Payments with `loanId` (borrower → lender). §9.

### 1.7 Project component
`{id, projectId, name, status: planned|bought|done, estimatedCost?, actualCost?, paidBy, receipt?, createdAt, statusChangedAt, history}`. `bought`/`done` require `actualCost > 0`. New parts start `planned` with `paidBy = "me"`. §8.

### 1.8 Recurring rule and draft
Rule: `{id, groupId?, title, category, amount? (null when variable), currency, variable, frequency: weekly|monthly|yearly, anchorDate, startDate, lastOccurrence?, payerId, split: {mode: equal, personIds}, createdAt, createdBy, active}`. The schedule's day comes from `anchorDate` (day of month / weekday / day-month). Draft: `{id, ruleId, occurrenceDate, createdAt, expenseId?}`; Enter amount creates the expense (dated `occurrenceDate`, split as the rule says) and sets `expenseId`. Drafts never count anywhere. §10.

### 1.9 Reminder (log of reminders Paybak sent to friends)
`{id, toId, fromId: "me", amount, currency, expenseId? | groupId? | loanId?, installment?, sentAt, automatic, message?, tone?, via?: paybak|share}`. Automatic ones come from the schedule (§10), manual ones from the Remind sheet. They show on the timeline and drive "Last reminder sent …". Reminders change no balance.

### 1.10 Inbox item (things that happened **to you**; the Notifications screen)
`{id, type, createdAt, read, params}`. `params` is a **snapshot** (amounts and titles at creation) so the text never changes later. Types, params and copy in §6.8. Reminders Paybak sends *for* you are not inbox items (timeline only). Pending claims are not stored as inbox items: the inbox shows a Confirm card for every pending payment to you (derived).

### 1.11 ActivityEvent: **derived, not stored**
The timeline is computed from the records (§6.6): expense `createdAt` and `history`, payments' `confirmedAt`, the reminder log, drafts, loans, component history. Why: one source of truth (a delete, restore or edit can't leave a stale timeline row); no dual writes in lane actions; the demo needs no duplicate event list; the expense History list and the timeline read the same `history` array. What *must* be stored is stored: history entries, reminder log, inbox (read state + snapshot). This replaces the `events` array sketched in app-architecture §3.2.

### 1.12 Category (fixed list, picker order)
| id | Name | Icon |
|---|---|---|
| `food` | Food | `food` |
| `travel` | Travel | `car` |
| `stays` | Stays | `bed` |
| `fun` | Fun | `ticket` |
| `rent` | Rent | `home` |
| `bills` | Bills | `bolt` |
| `shopping` | Shopping | `shopping-bag` |
| `other` | Other | `tag` |

### 1.13 Currency
ISO 4217 code, symbol, minor-unit exponent, name (the existing `Currency` type). Symbols used in copy: ₹ INR · AED AED · A$ AUD · £ GBP · C$ CAD · € EUR · ¥ JPY (exponent 0) · S$ SGD · $ USD. New foreign-currency records take their rate from `rates.json` (INR per unit; cross rate a→b = inr[a] ÷ inr[b]); the user may edit it before saving.

### 1.14 Settings and entitlement (ledger)
`{keepBalancesPerCurrency: false, push: {addedToExpense, paymentsToConfirm, reminders, overdueAlerts, projectUpdates, monthlySummary} (all true), reminderSchedule: {twoDaysBefore, onDueDate, overdueEvery3Days (all true), time: "21:00"}, discovery: {findMeByContact, contactsSync} (true), entitlement: {plan: free|pro, period: yearly|monthly|null, trialEndsAt: date?, since: moment?}}` plus `rotation: {contextKey: counter}` (§4.1) and `scheduler: {cursor}` (§10) at the top level of the ledger.
- Free by default. Pro-gated: Ask Paybak, reading receipts (attaching a photo is free), Insights, Recurring (the Repeat row), PDF/CSV export. The core ledger is free.
- Trial (Yearly only): `trialEndsAt = today + 7 days` ("Your free trial ends Wed 7 Oct."). Mock purchase flips `plan` to `pro`.

### 1.15 Names that differ from the app-architecture §3.2 sketch
| Sketch | This file / demo.json |
|---|---|
| `events` (stored) | not stored; derived (§1.11) |
| Payment `from`, `to`, `context?` | `fromId`, `toId`, `groupId` / `loanId` / `expenseId` |
| Expense `isDraft` | separate `drafts` collection (§1.8) |
| Component `estimatedCost`, `actualCost`, `paidBy` | same |
| Loan `lender`, `borrower`, `period`, `lastReminderAt` | `lenderId`, `borrowerId`, `installments.frequency`; last reminder = latest Reminder with that `loanId` |
| Group `dueDate` | `settleBy` |
| Person `isFriend`, `onPaybak`, `phone`, `email`, `upiId` | every person in `people` is a friend; `onPaybak = !isGuest`; `contact`; `upi` |
| `reminders [{personId, context, at, …}]` | §1.9 fields |
| RecurringRule `template`, `anchor`, `lastGenerated` | flat fields, `anchorDate`, `lastOccurrence` |

---

## 2. Money and formatting

### 2.1 Arithmetic
All sums in minor units. Conversion at a saved rate: `to_minor = round_half_up(from_minor × rate × 10^(exp_to − exp_from))`. Example: AED 960 at 22.85 → 96,000 fils × 22.85 = 2,193,600 paise = **₹21,936**.

### 2.2 Formatting (`money(minor, code, sign)`)
- Symbol of ≤ 2 characters → prefixed without a space (`₹2,800`, `€1,200`, `$1,200`, `S$40`); longer → code and a space (`AED 1,800`).
- Grouping: **Indian for INR** (last 3 digits, then pairs: `₹2,900`, `₹12,500`, `₹1,00,000`, `₹99,99,999.50`); thousands for everything else (`AED 1,800`).
- Decimals only when the minor part isn't 0 (`₹1,234.50`, `₹333.34`); JPY never has decimals.
- `sign: signed` → `+₹2,900` / `−₹1,850` (U+2212); `debit` → `−₹450` only for negatives; `none` (timeline, settle rows, chat) → unsigned.
- "≈" lines (foreign groups, the Add expense rate line): `≈ ₹{converted, rounded to whole rupees} · ₹{rate with 2 decimals} per {CODE}` → "≈ ₹21,936 · ₹22.85 per AED".
- Big amount entry (Amount Display) echoes what's typed and formats on blur; cap ₹99,99,99,999.

---

## 3. Dates and relative formatting
All in the device calendar and time zone, en-GB month/weekday names.
| Function | Rule | Examples |
|---|---|---|
| `rowDate(d)` | Today · Yesterday · `d MMM` · `d MMM yyyy` (other year) | "Today", "26 Sep", "12 Mar" |
| `dayHeader(d)` (timeline groups) | Today · Yesterday · `EEE d MMM` (+ year if other) | "Mon 28 Sep" |
| `dueBadge(due)` (Row / Attention, settle rows) | overdue → "Overdue {n} day(s)" (n = today − due in calendar days); due within 0–6 days → "Due {EEE}"; later → "Due {d MMM}" | "Overdue 3 days", "Due Fri", "Due 12 Oct" |
| `dueLabel(due)` (lists, detail rows) | "Due {EEE d MMM}" | "Due Sun 4 Oct" |
| `time(t)` | `h:mm a`, lowercase am/pm | "9:12 pm" |
| `loanMetaDate(d)` | Today · Yesterday · `EEE d MMM` if < 60 days ago · `d MMM`; + year if other | "Laptop repair · Today", "· Wed 30 Sep" (34 days later), "· 12 Jun" (110 days) |
| `dateRange(a, b)` | same month "21–25 Sep" (en dash, no spaces); otherwise "28 Sep – 2 Oct" | "6–8 Mar" |
| `daysLeft` (Recently deleted) | whole days from today to `deletedAt + 30 days` | "24 days left", "1 day left" |
| inbox time | Today → `time`; otherwise `rowDate` | "9:00 pm", "Yesterday" |
| reminder body weekday | due in 2–6 days → "It’s due {EEEE}."; 1 → "tomorrow"; 0 → "today"; later → "on {d MMM}"; past → "It was due on {d MMM}." | "It’s due Friday." |
| `addMonths(d, n, day)` | same day of month, clamped to the month's last day (31 Jan → 28/29 Feb → 31 Mar: always anchor on the original day) | |
| greeting | 5–12 morning, 12–17 afternoon, else evening (M1) | "Good evening" |

---

## 4. Splits and rounding

### 4.1 Fair rotation of leftover minor units
When a total doesn't divide evenly, the r leftover units go one each to r people, rotating over time:
1. Order the included people by the context's member order (group `memberIds`; outside a group: `"me"` first, then the order they were added to the expense).
2. `rotation[contextKey]` (default 0). contextKey = the group or project id; outside a group the sorted participant ids joined by `+`.
3. Give +1 to people at indices `counter mod n, …` (wrapping), then `counter += r`. Save the resulting shares on the record. Only a change of amount, people or split recomputes them.

Example: ₹1,000 among Arjun, Priya, Esha, counter 0 → ₹333.34 / ₹333.33 / ₹333.33 (counter → 1); the next ₹1,000 in the same context gives Priya the extra paisa.

### 4.2 Modes (T = total in minor units, P = included people)
- **equal**: ⌊T/n⌋ each + rotation. ₹2,800 ÷ 4 = ₹700 each.
- **exact**: each row's `value` is its amount; valid iff Σ = T. Footer: "{T−Σ} left" / "{Σ−T} over" and "{Σ} of {T}". Dev mid-edit at ₹550: "₹150 left", "₹2,650 of ₹2,800", Done disabled.
- **percent**: `value` in bps, Σ must be 10,000. Money = ⌊T × bps / 10,000⌋, then the leftover by **largest remainder**, ties by the rotation order.
- **shares**: `value` = whole shares ≥ 1; money = T × s ÷ Σs with largest remainder + rotation. Always valid. 2:1:1 of ₹2,800 → ₹1,400 / ₹700 / ₹700.
- **itemized** (receipt): each item's price split equally among its people (rotation), giving per-person subtotals; then the **total** (subtotal + tax + tip) is allocated in proportion to the subtotals with largest remainder so the parts add up exactly. Leopold Cafe: subtotals You ₹860 · Esha ₹540 · Dev ₹600 (₹240 ÷ 3 = ₹80, ₹270 ÷ 3 = ₹90); × 2,300/2,000 → **₹989 · ₹621 · ₹690**.
- Switching modes starts from the current money split (Equally → Exact prefills ₹700 each).
- Form Split row value: "Equally · ₹700 each" (even) · "Equally · 3 people" (leftover) · "Exact · 4 people" · "Percent · 4 people" · "Shares · 4 people" · "Itemized · 3 people".

### 4.3 Other rounding
- Loan installments: `amount ÷ count`, leftover units to the **earliest** installments (₹6,000 / 3 = ₹2,000).
- Project shares: same as expense splits of `spent` (rotation key = project id).
- Percentages shown to people (Insights captions, fair-share %): largest remainder so displayed parts add up to 100 (§6.4).

---

## 5. Balances

### 5.1 What counts
Balances use **live expenses** (not deleted, not drafts), **project components** with status bought/done, **confirmed payments** and **loans**. Pending, not-received and cancelled payments count nowhere. A flagged expense counts. "As of t" (the scheduler, §10) = only records with `createdAt ≤ t` and payments with `confirmedAt ≤ t` (edits apply retroactively).

### 5.2 Net per member inside a group (group currency)
`net[m] = Σ paid by m − Σ share of m + Σ confirmed group payments m sent − Σ m received`. Nets sum to 0.

**Goa Trip** (6 live expenses, each ÷ 5; Snacks ₹300 is deleted):
| Expense (date) | Paid by | Amount | Share each |
|---|---|---|---|
| Villa (3 nights) (Mon 21 Sep) | Kabir | ₹18,000 | ₹3,600 |
| Scooter rentals (Mon 21 Sep) | Priya | ₹3,500 | ₹700 |
| Seafood dinner at Britto’s (Tue 22 Sep) | You | ₹6,500 | ₹1,300 |
| Parasailing (Wed 23 Sep) | Esha | ₹5,000 | ₹1,000 |
| Beach shack lunch (Thu 24 Sep) | Dev | ₹4,000 | ₹800 |
| Fuel (Fri 25 Sep) | Dev | ₹2,500 | ₹500 |
| **Total** | | **₹39,500** | **₹7,900** |

Paid vs share → nets: You 6,500 − 7,900 = **−₹1,400** · Kabir 18,000 − 7,900 = **+₹10,100** · Priya 3,500 − 7,900 = **−₹4,400** · Esha 5,000 − 7,900 = **−₹2,900** · Dev 6,500 − 7,900 = **−₹1,400**. Title row "21–25 Sep · 5 members · ₹39,500 spent" (range of expense dates).

### 5.3 The plan (who pays whom)
- **Simplify on** (default; always for projects): repeat { debtor = most negative net, creditor = most positive net (ties: earlier in `memberIds`); transfer min(|debt|, credit) } until no debtor or creditor is left. At most n − 1 transfers.
  Goa Trip: Priya → Kabir ₹4,400 · Esha → Kabir ₹2,900 · You → Kabir ₹1,400 · Dev → Kabir ₹1,400. Footnote: "Simplify debts is on. You, Priya, Esha and Dev each pay Kabir directly." (debtors in member order, "You" first, joined "A, B and C"; several creditors: "Everyone settles in {n} payments.").
  Flat 302: nets You −₹450 · Meera +₹900 · Kabir −₹450 → You → Meera ₹450, Kabir → Meera ₹450.
- **Simplify off**: pairwise. For each expense, each person owes each payer `share × paid_by_payer ÷ amount`; group payments reduce the pair; netted per pair.
- The nets never depend on the mode, only the transfers do.

### 5.4 Foreign-currency groups
Group amounts stay in the group currency, and so do its nets and plan ("Paid AED 540 · Share AED 600"). Each expense's saved rate gives its "≈ ₹" line; the group total footnote sums the per-expense conversions (never total × one rate).

**Dubai Weekend** (AED; You, Kabir, Meera; AED 600 each):
| Expense | Date | Paid by | Amount | Your share | Rate | ≈ ₹ |
|---|---|---|---|---|---|---|
| Hotel | Fri 6 Mar | Kabir | AED 960 | AED 320 | 22.85 | 21,936 |
| Desert safari | Sat 7 Mar | You | AED 540 | AED 180 | 22.80 | 12,312 |
| Dinner at the Marina | Sun 8 Mar | Meera | AED 300 | AED 100 | 22.90 | 6,870 |
"Total AED 1,800 · ≈ ₹41,118 at saved rates". Nets You −60, Kabir +360, Meera −300 AED; payments You → Kabir AED 60 and Meera → Kabir AED 300 on 14 Mar make everyone 0 → "Settled", card caption "You paid Kabir AED 60 on 14 Mar".

**Into the default currency** (Home, Friends, Settle up; "Keep balances per currency" **off**): a group-currency transfer converts at the group's **latest saved rate** (the rate of its newest expense or payment). A settled foreign group is exactly ₹0 in every view. Direct (no group) foreign-currency expenses and payments convert each at their own saved rate. With the setting **on**, keep one balance per currency everywhere (a friend net becomes a map, "You owe Kabir AED 60 and ₹1,400") and the settle plan settles each currency separately.

### 5.5 Contexts and obligations: everything between you and one friend
For each friend F, sum these **contexts** (amount > 0 = F owes you):
1. **Each group or project you're in**: the plan transfers between you and F (§5.3), converted (§5.4). Due date = `settleBy` (groups), none (projects). Title = the group name.
2. **Direct**: every live no-group expense with both of you on it contributes `pairDebt` (what F owes you on it: F's share × your paid ÷ amount − your share × F's paid ÷ amount), converted at its rate; direct payments between you (no `groupId`, no `loanId`) reduce it. Payments pay **the oldest debts first**, so the **open items** are the newest obligations in the net's direction, taken until they cover |net| (the last one partly). Each open item keeps its expense title and due date.
3. **Each loan with F**: remaining = amount − confirmed repayments; open items = unpaid installments (§9).

`friendNet[F] = Σ contexts`. **Open items** of F = the obligations of its contexts in the direction of `friendNet[F]` (these drive due dates, overdue, reminders and row subtitles).

Demo (Wed 30 Sep, 21:15):
| Friend | Contexts | Net | Open item (due) |
|---|---|---|---|
| Rohan | direct: Movie tickets ₹1,600, you paid, ₹800 each | **+₹800** | Movie tickets ₹800 (Sun 27 Sep → overdue 3 days) |
| Priya | direct: Weekend groceries ₹2,100 (₹1,050 each) paid back yesterday + Dinner at Olive Garden ₹700; Goa Trip plan: Priya pays Kabir, not you | **+₹700** | Dinner at Olive Garden ₹700 (Sun 4 Oct) |
| Esha | direct: Olive Garden ₹700 | **+₹700** | Olive Garden (Sun 4 Oct) |
| Dev | direct: Olive Garden ₹700; Build a Drone: Rohan and Priya pay Dev, not you | **+₹700** | Olive Garden (Sun 4 Oct) |
| Kabir | Goa Trip plan You → Kabir ₹1,400; Bike service loan paid back; Dubai, College Gang, Hackathon Kit settled | **−₹1,400** | Goa Trip (Fri 2 Oct) |
| Meera | Flat 302 plan You → Meera ₹450; Dubai settled | **−₹450** | Flat 302 (Mon 5 Oct) |
| Ananya | guest, nothing shared | **0** ("No balance") | – |

Payments across contexts: a payment is filed under one context (the Record payment "For" row). The friend net is the sum over contexts, so a payment filed under one context settles the friend net even if it leaves offsetting balances in two contexts (rare; the group screen then shows them).

---

## 6. Read models (worked on the demo at Figma parity)

### 6.1 Home
- **You’re owed** = Σ positive friend nets; caption "from {n} people" ("from 1 person"). Demo **+₹2,900 from 4 people** (Rohan 800 + Priya 700 + Esha 700 + Dev 700).
- **You owe** = Σ |negative friend nets|; caption from the open items you owe: g = distinct groups/projects, p = distinct people owed outside groups → "across {g} group(s)" · "to {p} person/people" · "across {g} groups and {p} people". Demo **−₹1,850 across 2 groups** (Kabir/Goa Trip 1,400 + Meera/Flat 302 450).
- **State**: `firstDay` when the ledger has no people and no records; `allSettled` when both totals are 0 and no claim is pending; otherwise `active`. Pending claims (§6.7) show as Confirm cards above the balances and **don't change the totals** ("Home keeps +₹2,900 until you confirm").
- **Due soon**: open items that are overdue or due within **2 days** (today + 2, the same lead as the first reminder), max 3, sorted overdue first (most overdue), then due date. A debt **you** owe inside a group shows the group ("Goa Trip · Your share", Settle); everything else shows the person with the item title (owed to you → Remind; direct debt you owe → Settle). Demo: Rohan · Movie tickets · ₹800 · "Overdue 3 days" · Remind; Goa Trip · Your share · ₹1,400 · "Due Fri" · Settle. (Olive Garden is due in 4 days and Flat 302 in 5, so they're not listed.)
- **Recent activity**: the 3 newest timeline events of kinds expense added and payment confirmed (§6.6). Row copy on Home: expense you paid → title, "You paid · {n} people", total (primary); expense someone else paid → title, "{group or payer} · You owe" / "· Your share" (§6.6 rule), "−{your share}" (secondary); payment to you → "{name} paid you", "{method}", amount (primary); payment by you → "You paid {name}", "{method}", "−{amount}" (secondary). Demo: Dinner at Olive Garden / You paid · 4 people / ₹2,800 / Today · Priya paid you / UPI / ₹1,050 / Yesterday · Electricity bill / Flat 302 · You owe / −₹450 / 26 Sep.
- After confirming Esha's ₹700: **+₹2,200 from 3 people**, "Esha paid you · UPI · ₹700 · Today" tops Recent activity. After Meera confirms your ₹450: **−₹1,400 across 1 group**.

### 6.2 Breakdowns
- You’re owed: one row per positive friend, amount = friend net, subtitle = the lead open item's title (the earliest-due one; several items → proposal "{n} expenses"), under it the red Overdue badge or "Due {EEE d MMM}". Order: overdue first (most overdue), due date ascending, then friend order. Demo: Rohan +₹800 Movie tickets Overdue 3 days · Priya / Esha / Dev +₹700 Dinner at Olive Garden Due Sun 4 Oct.
- You owe: Kabir −₹1,400 Goa Trip Due Fri 2 Oct · Meera −₹450 Flat 302 Due Mon 5 Oct.
- **Simplified-debts footnote** ("Goa Trip uses simplified debts, so you pay Kabir directly."): for each simplify-on group where you pay, take the payers of the expenses you had a share in **since your group net was last 0**; show the line when that set of people ≠ the people you pay in the plan. Goa Trip: since the trip began you owe on expenses paid by Kabir, Priya, Esha and Dev, but you pay only Kabir → shown. Flat 302: after the 7 Sep settle-up you were at 0; since then only Meera's electricity → you pay Meera → not shown.

### 6.3 Settle up (the fewest-payments plan, from your side)
One row per friend with a non-zero net (§5.5): net < 0 → "payments to make", net > 0 → "people who owe you". Context and due = the friend's earliest-due open item. Order: overdue first, then due date, then friend order. Paybak never suggests transfers between two other people across unrelated contexts. Demo: **2 payments to make**: Kabir ₹1,400 Goa Trip Due Fri · Meera ₹450 Flat 302 Due Mon. **4 people owe you**: Rohan ₹800 Movie tickets Overdue 3 days · Priya, Esha, Dev ₹700 Dinner at Olive Garden Due Sun. Record payment from a row prefills friend, amount and context (group / expense). A pending payment you recorded keeps the row with a Pending marker until confirmed.

### 6.4 Insights (month M)
1. Scope: for every live expense **dated** in M with a share for you, in a group (not a project) or with friends outside groups: your share, converted at its saved rate. Excluded: projects, payments, loans, drafts, deleted.
2. **Total** = Σ. September 2026 = **₹23,300**.
3. **Trend** vs the previous month: pct = round(|cur − prev| ÷ prev × 100); "Up {pct}% from {Month}" / "Down …" / "Same as {Month}"; hidden when prev = 0. (23,300 − 22,200) ÷ 22,200 = 4.95 % → **"Up 5% from August"**.
4. **Chart**: the month and the 5 before; height = round(120 × month ÷ max) pt, axis from zero. Apr ₹18,400 · May ₹21,950 · Jun ₹19,600 · Jul ₹20,600 · Aug ₹22,200 · Sep ₹23,300 → **95 · 113 · 101 · 106 · 114 · 120**.
5. **By category**, amount descending, ₹0 omitted; captions by **largest remainder** so they add to 100; bar = 310 × caption %. September: Rent ₹12,000 **51%** (51.50 %) · Food ₹3,850 **17%** (16.52) · Stays ₹3,600 **15%** · Fun ₹1,800 **8%** (7.73) · Travel ₹1,200 **5%** · Bills ₹850 **4%** (3.65). (Floors 97; the 3 extra points go to Fun .73, Bills .65, Food .52; Rent's .50 loses.)
6. **Groups**: per group, no-group expenses as "Without a group": Flat 302 ₹12,850 **55%** · Goa Trip ₹7,900 **34%** · Without a group ₹2,550 **11%**. (Flat 302 = Rent 12,000 + Wi-Fi 400 + Electricity 450; Without a group = Olive Garden 700 + Movie tickets 800 + Weekend groceries 1,050.)
7. **Friends** (not designed; proposal): split each of your shares evenly across the other people on that expense (fair rotation), so the bars add up to the total.
8. **Lent vs borrowed since {first chart month}**: Σ loan amounts created in the window where you lend / borrow: **Lent ₹4,500 · Borrowed ₹0** (Kabir · Bike service · Paid back).

### 6.5 Lists
- **Friends list**: summary "+₹{Σ owed}" / "−₹{Σ owe}" (= Home). Order: 1) owed and overdue (most overdue first), 2) owed, by due date then amount descending, 3) you owe, by due date then amount descending, 4) settled / no balance; ties by `addedAt`. Demo: Rohan, Priya, Esha, Dev, Kabir, Meera, Ananya.
- **Groups list** (groups and projects together): 1) where your net ≠ 0, by due date then |amount| descending; 2) the rest alphabetically; archived projects under "Archived". Demo: Goa Trip (−₹1,400, "5 members · Due Fri 2 Oct"), Flat 302 (−₹450, "3 members · Due Mon 5 Oct"), Build a Drone ("Project · 4 members", "You’re settled": your net 0 but others open, "₹52,000 of ₹60,000", "₹8,000 left"), College Gang ("6 members", Settled), Dubai Weekend ("3 members · AED", Settled); Archived: Hackathon Kit ("Project · Closed 30 Aug", Read-only).
- **Group settings** "Recurring expenses": "None" / "1 rule" / "{n} rules" (Flat 302 "3 rules").
- **Leave group**: allowed only when your net in the group is exactly 0; otherwise "You owe ₹1,400 in Goa Trip. Settle up with Kabir first, then you can leave."
- **Friend page**: net + lead item + due / overdue; "Last reminder sent today" (latest Reminder to them: today / yesterday / on {EEE d MMM}); History = non-project expenses you share with them + payments and loans between you, newest first (Rohan: Movie tickets ₹1,600 20 Sep "You paid · Rohan owes ₹800"; Farewell dinner ₹9,000 12 Mar "College Gang · Settled"); Groups together = groups and projects with both of you (Rohan: College Gang, Build a Drone).
- **Expense detail**: Your share = your split share; Due = `dueDate ?? group.settleBy`; "Your {group} balance" = your group net (signed); split rows: payer(s) first, then You, then the others in member order. Villa: ₹3,600 · Fri 2 Oct · −₹1,400; Olive Garden (no group): Your share ₹700 · Due Sun 4 Oct, no group row.
- **Recently deleted**: deleted expenses newest deletion first; "Deleted by Priya on 24 Sep · 24 days left" (Snacks, ₹300, Goa Trip; purge Sat 24 Oct).
- **Export**: a group row starts ticked iff it has any record (expense date, payment involving you, component status change) in the range; "Without a group" covers direct expenses, payments and loans. September: Goa Trip, Flat 302, Build a Drone, Without a group ticked; College Gang, Dubai Weekend (and Hackathon Kit) unticked. Range line "1 Sep – 30 Sep 2026".
- **Delete account** is blocked while either Home total ≠ 0: "You still owe ₹1,850 and are owed ₹2,900."

### 6.6 Timeline (derived; Activity tab and "See all")
Events for you, newest first by moment, grouped by `dayHeader`. Pending claims to you sit on top as Confirm cards (§6.7).
| Kind | Source (moment) | Title | Subtitle | Amount (unsigned) |
|---|---|---|---|---|
| expense added | expense `createdAt` (not deleted) | "You added {title}" / "{actor} added {title}" | you paid: "You paid · {n} people" (in a group "{group} · You paid"); else "{group or payer} · You owe {share}" or "· Your share {share}" | total; primary if you paid, else secondary |
| expense edited | history `amountChanged` etc. | "{actor} changed {title}" | "{group} · Was {old}" (other edits "{group} · Edited") | new total |
| expense deleted / restored | `deletedAt` / history | "{actor} deleted {title}" / "restored" | "{group}" | – |
| payment | payment `confirmedAt` (you're payer or receiver) | "{name} paid you" / "You paid {name}" | "{for} · {method} · Confirmed" | amount; primary when to you |
| reminder sent | Reminder `sentAt` | "Reminder sent to {name}" | "{item} · {amount} · Sent automatically" / "Sent by you" | – |
| draft created | draft `createdAt` | "{rule} draft created" | "{group} · Needs an amount" | badge "Draft" |
| loan added | loan `createdAt` | "You lent {name}" / "{name} lent you" | reason | amount |
| component events | component history | "{actor} bought {part}" etc. (proposal) | project name | actual cost |
Comments and group creation are **not** timeline events (they would add rows on Sun 27 and Mon 28 Sep that Figma doesn't draw).

**"You owe" vs "Your share"**: "You owe" when the payer is someone you currently pay (a plan transfer to them in that group, or an open direct item on that expense); otherwise "Your share". Electricity bill (Meera; you pay Meera) → "Flat 302 · You owe ₹450"; Fuel (Dev; you pay Kabir) → "Goa Trip · Your share ₹500".

Demo timeline (with Esha's claim applied): **Today**: [Confirm card: "Esha says she paid you ₹700" · "Dinner at Olive Garden · UPI · 9:12 pm"] · Reminder sent to Rohan · Movie tickets · ₹800 · Sent automatically · You added Dinner at Olive Garden · You paid · 4 people · ₹2,800. **Yesterday**: Priya paid you · Weekend groceries · UPI · Confirmed · ₹1,050. **Mon 28 Sep**: Kabir changed Villa (3 nights) · Goa Trip · Was ₹17,500 · ₹18,000 · Cooking gas draft created · Flat 302 · Needs an amount. **Sun 27 Sep**: Reminder sent to Rohan. **Sat 26 Sep**: Meera added Electricity bill · Flat 302 · You owe ₹450 · ₹1,350. **Fri 25 Sep**: Reminder sent to Rohan · Dev added Fuel · Goa Trip · Your share ₹500 · ₹2,500.

### 6.7 Pending claims
Every payment with `toId = "me"` and `status = pending`, newest first: card title "{name} says {pronoun} paid you {amount}", detail "{for} · {method} · {h:mm a}". Confirm → `confirmed` (+ inbox "Payment confirmed", read), Not received → `notReceived` with the note. The same claim is on Home, the timeline, the inbox and the lock-screen push; all of them act on the one payment.

### 6.8 Inbox types (params are snapshots)
| Type | Params | Title / body | Created | Link |
|---|---|---|---|---|
| `paymentReminder` | personId, amount, currency, title, dueDate, groupId? | "Payment reminder" / "You owe {name} {amount} for {title}. {weekday phrase}" | schedule, for debts you owe (§10), 21:00 | `record-payment?to=…&amount=…&context=…` |
| `monthlySummary` | year, month, spent, owed, owe | "Monthly summary" / "{Month}: you spent {spent} on shared expenses. You’re owed {owed}." (owed = 0 → "You owe {owe}."; both 0 → "You’re all square.") | last day of the month, 20:00 | `insights?month=yyyy-MM` |
| `paymentConfirmed` | paymentId, personId, amount, currency, title, method | "Payment confirmed" / "{name} paid you {amount} for {title} by {method}." | a payment to you is confirmed | payment detail |
| `paymentOverdue` | personId, amount, currency, title, dueDate, expenseId?/groupId?/loanId? | "Payment overdue" / "{name} owes you {amount} for {title}. It was due on {d MMM}." (trailing: red "Overdue" badge) | the day after a due date, 09:00, if still open | `remind?person=…` |
| `newExpenseInGroup` | expenseId, groupId, actorId, title, total, share, currency | "New expense in {group}" / "{name} added {title}, {total}. Your share is {share}." | someone else adds a group expense you're on | `expense/{id}` |
| `paymentNotReceived` (proposal) | paymentId, personId, note | "{name} hasn’t received it" / the note | the receiver taps Not received on your payment | payment detail |
| `expenseFlagged`, `flagResolved` (proposal) | expenseId, personId, note | "{name} flagged {title}" / note; "{name} resolved your flag" / title | 09-06 | `expense/{id}` |
Comments create no inbox item. Grouping: Today (created today) / Earlier; newest first. Unread = the dot; opening an item or "Mark all read" clears it; the Home bell dot = any unread. Every item may also post an OS notification if its push toggle is on (§10.3).

Demo inbox at 21:15: **Today** (unread): Payment reminder · "You owe Kabir ₹1,400 for Goa Trip. It’s due Friday." · 9:00 pm; Monthly summary · "September: you spent ₹23,300 on shared expenses. You’re owed ₹2,900." · 8:00 pm. **Earlier** (read): Payment confirmed · "Priya paid you ₹1,050 for Weekend groceries by UPI." · Yesterday; Payment overdue · "Rohan owes you ₹800 for Movie tickets. It was due on 27 Sep."; New expense in Flat 302 · "Meera added Electricity bill, ₹1,350. Your share is ₹450." · 26 Sep; then the Goa Trip expenses of 21–25 Sep.

### 6.9 Ask Paybak answers (from the same read models)
- "Who owes me money?" → "{n} people owe you {total}: {parts}." Parts: overdue people first "{name} {amount} (overdue since {d MMM})"; then people owed the same amount for the same single expense grouped "{A}, {B} and {C} {amount} each for {what}"; others "{name} {amount}"; joined with ", " and ", and " before the last. {what} = "tonight’s dinner" for a Food expense dated today whose title starts with "Dinner", "today’s {title lower}" for another today's expense, else the title. Demo: **"4 people owe you ₹2,900: Rohan ₹800 (overdue since 27 Sep), and Priya, Esha and Dev ₹700 each for tonight’s dinner."**
- "How much did I spend on food this month?" → "You spent ₹3,850 on food in September — 17% of your ₹23,300 share."
- "When is Goa Trip due?" → "Your Goa Trip share of ₹1,400 is due Fri 2 Oct."
- "Draft a reminder for Rohan" → the Remind sheet's Friendly template: "Hi {first}! Just a gentle reminder about {amount} for the {title lower-first} on {expense date d MMM}. You can pay me on UPI at {your UPI}. Thanks."
- Drafting "Add ₹600 for a cab, split with Esha and Dev" → Cab, Travel, ₹600, paid by you today, equal among you + Esha + Dev → "₹200 each". Keyword table: insights §3.6.6, except **"groceries" → Food** (§12 #9).

---

## 7. The demo dataset (`seed/demo.json`)

### 7.1 Relative dates and the anchor
- Dates are written `"D-9"` (a local day: the load day − 9) and moments `"D-9T15:30"` (that day at 15:30 local). `"D0"` is the load day; `"D+2"` two days later. Regex: `^D([+-]?\d+)(T(\d\d):(\d\d))?$`.
- **Figma parity**: load with the anchor day **Wed 30 Sep 2026** and the clock pinned to **Wed 30 Sep 2026 21:15 local** (after the 9:00 pm reminders and the 9:12 pm claim). Every number and label then matches Figma.
- **Around today**: anchor day = today, real clock. The same story shifts by whole days: "Overdue 3 days", "Today", "Yesterday" stay; weekday names and month buckets follow the real calendar (Goa Trip due "Sat" on a Thursday; Insights months move).
- **Clamp**: a seeded moment later than `now` becomes `now` (loaded at 9 am, tonight's 19:40 dinner is created "now"). Dates never clamp. Scenario steps clamp the same way except `setClock`.
- Materialising = resolve every relative string, then run `tick(now)` from `scheduler.cursor` (`"D0T00:00"`): that generates **today's** 20:00 summary and 21:00 reminders (when `now` is past them), exactly as live use would. Everything before the cursor is seeded.

### 7.2 Contents
Profile: Arjun Mehta (`preset(0)` = avatar-1), INR, `arjun@okaxis` (primary UPI) + HDFC Bank ···· 4821, @arjun, free plan.

People (seed order = `addedAt` order): Rohan Verma (avatar-3, rohan@ybl, he) · Priya Sharma (avatar-2, priya@okhdfcbank, she) · Esha Kapoor (avatar-4, esha@okicici, she) · Dev Malhotra (avatar-5, dev@oksbi, he) · Kabir Singh (avatar-6, kabir@okaxis, he) · Meera Iyer (avatar-7, meera@okhdfcbank, she) · Ananya Rao (guest, initials "AR", +91 98765 43210, she).

| Id | Group | Members | Records |
|---|---|---|---|
| `g-goa` | Goa Trip (trip, INR, settle by D+2 = Fri 2 Oct) | me, Kabir, Priya, Esha, Dev | the 6 expenses of §5.2 + Snacks ₹300 (Priya, 23 Sep, deleted by Priya 24 Sep 10:00); Villa added at ₹17,500 and changed to ₹18,000 by Kabir on 28 Sep 18:20, receipt by Kabir, comments Priya 27 Sep / Kabir 28 Sep |
| `g-flat302` | Flat 302 (home, INR, settle by D+5 = Mon 5 Oct) | me, Meera, Kabir | Apr–Sep: Rent ₹36,000 (you, 1st, rule `r-rent`), Wi-Fi ₹1,200 (Kabir, 5th, rule `r-wifi`), Electricity bill (Meera, 26th: 1,500 / 2,100 / 2,400 / 1,950 / 1,650 / **1,350**); settle-up payments on the 7th of each month (the simplified plan of what was open, all to you); Cooking gas rule `r-gas` (variable, 28th, you) with the September draft (created Mon 28 Sep 09:00). Result: You −₹450, Meera +₹900, Kabir −₹450 |
| `g-college` | College Gang (friends, INR) | me, Rohan, Priya, Esha, Dev, Kabir | Farewell dinner ₹9,000, Kabir, 12 Mar; everyone paid Kabir ₹1,500 on 14 Mar → settled |
| `g-dubai` | Dubai Weekend (trip, AED) | me, Kabir, Meera | §5.4, settled 14 Mar |
| `pj-drone` | Build a Drone (project, drone, INR, created 10 Aug, budget ₹60,000, Equal, active) | me, Dev, Priya, Rohan | 8 components (§8.4) |
| `pj-hackathon` | Hackathon Kit (project, package, budget ₹20,000, Equal, closed = archived 30 Aug) | me, Esha, Dev, Kabir | Raspberry Pi kits ₹7,200 (Esha) · Sensors ₹4,600 (Dev) · Display ₹3,400 (you) · Cables and adapters ₹3,200 (Kabir); you → Esha ₹1,200 (29 Aug), Kabir → Esha ₹1,400 (30 Aug) |

Direct expenses: Weekend groceries ₹2,100 (you, Sat 19 Sep, you + Priya, Food; Priya paid you ₹1,050 by UPI Tue 29 Sep 18:40, confirmed 19:02) · Movie tickets ₹1,600 (you, Sun 20 Sep, you + Rohan, Fun, due Sun 27 Sep) · Dinner at Olive Garden ₹2,800 (you, today 19:40, you + Priya + Esha + Dev, Food, due Sun 4 Oct) · April–August outings, all squared up on the 28th of their month (they give the Insights chart its months): Dinner at Bombay Canteen, IPL match tickets, Concert tickets, Lonavala villa, Brunch at Farmers’ Café, Dinner at Toit, Cricket match tickets, Pizza night, Stand-up show tickets, Alibaug stay, Dinner at Bastian, Movie night, Coorg homestay.

Loan: you lent Kabir ₹4,500 for "Bike service" on 12 Jun, 3 monthly installments from 12 Jul; Kabir repaid ₹1,500 on 10 Jul, 12 Aug and 14 Sep (→ "Paid 14 Sep · 2 days late", "Paid back on 14 Sep").

Reminder log: Rohan (Movie tickets ₹800) Fri 25 and Sun 27 Sep 21:00; Kabir's loan 10 Aug, 10 Sep, 12 Sep 21:00. Today's 21:00 reminder to Rohan is generated by `tick`.

Inbox (all read): New expense in Goa Trip × 5 (21–25 Sep), New expense in Flat 302 (26 Sep 20:15), Payment overdue for Rohan (Mon 28 Sep 09:00), Payment confirmed from Priya (Tue 29 Sep 19:02). Today's Payment reminder (21:00) and Monthly summary (20:00) are generated by `tick`, unread.

Not in the base (scenarios, §7.4): Esha's ₹700 claim, the Dev loan, Weekend Trek, pending payments to Meera / Kabir, the Seafood flag, the GPS purchase, the drone's closing.

### 7.3 Schema (top level of demo.json)
```
{
  schemaVersion: 1,
  anchor: { figmaDate: "2026-09-30", pinnedTime: "21:15", note },
  profile:  { name, avatar: {kind: "preset", index}, currencyCode, upiID, username, pronoun, signInMethod,
              contact, onboardingComplete, showPaymentToFriends, paymentMethods: [PaymentMethod] },   → ProfileStore
  settings: { keepBalancesPerCurrency, push {…6}, reminderSchedule {twoDaysBefore, onDueDate, overdueEvery3Days, time},
              discovery {findMeByContact, contactsSync}, entitlement {plan, period, trialEndsAt, since} },
  people: [Person §1.2], groups: [Group §1.3], expenses: [Expense §1.4], payments: [Payment §1.5],
  loans: [Loan §1.6], components: [Component §1.7], recurringRules: [Rule §1.8], drafts: [Draft §1.8],
  reminders: [Reminder §1.9], inbox: [InboxItem §1.10], rotation: {}, scheduler: { cursor: "D0T00:00" },
  scenarios: { name: [Step] }                      // §7.4, debug only
}
```
Everything except `anchor`, `profile` and `scenarios` is the Ledger document as stored in `ledger.json` (with absolute dates). Unknown keys are ignored; missing optional keys default to null / empty.

### 7.4 Scenarios (scripted transitions, applied after the base is materialised)
A scenario is a list of steps. `{use: "otherScenario"}` inlines another scenario. Every other step is a **store action** (§11) with its arguments and `at` (a relative moment, clamped to now). The loader runs `tick(at)` before each step, then the action. `setClock` moves the pinned clock (and runs `tick` up to it).
Step shapes (exactly as in demo.json): `recordPayment {at, payment: {id, fromId, toId, amount, currency, method, date, groupId, loanId, expenseId, recordedBy}}` (the action sets status, createdAt, confirmedAt) · `confirmPayment {at, paymentId}` · `markNotReceived {at, paymentId, note}` · `flagExpense {at, expenseId, by, note}` · `addLoan {at, loan: {id, lenderId, borrowerId, amount, currency, reason, date, installments, dueDate}}` · `updateComponent {at, componentId, status, actualCost, paidBy}` · `closeProject {at, projectId}` · `createGroup {at, group: {id, kind, type, icon, name, currency, memberIds, simplifyDebts, settleBy}}` · `setClock {at}` · `setEntitlement {plan, period, trialEndsAt}` · `clearLedger {}`. `verify.py` (`apply_scenario`, `act_*`) is the reference.

| Scenario | Steps | Result |
|---|---|---|
| `eshaClaimsPayment` | recordPayment Esha → me ₹700 UPI, for `e-olive`, recordedBy Esha, at D0T21:12 (id `pay-esha-olive`) | pending claim; Confirm card on Home / Activity / inbox; totals unchanged |
| `eshaPaymentConfirmed` | eshaClaimsPayment + confirmPayment at D0T21:15 | +₹2,200 from 3 people; "Esha paid you" tops Recent |
| `eshaPaymentNotReceived` | eshaClaimsPayment + markNotReceived (note "Hi Esha, I haven’t received ₹700 …") | card gone, +₹2,900 unchanged |
| `paymentToMeeraPending` | recordPayment me → Meera ₹450 cash, Flat 302 (`pay-me-meera`) | pending; −₹1,850 unchanged |
| `paymentToMeeraConfirmed` | + confirmPayment | −₹1,400 across 1 group |
| `paymentToKabirPending` | recordPayment me → Kabir ₹1,400 UPI, Goa Trip (`pay-me-kabir`) | pending; −₹1,850 unchanged |
| `eshaFlagsSeafood` | flagExpense `e-goa-seafood` by Esha, note "I left before dessert. Can we check the bill?" | Disputed; balances unchanged |
| `lendDev` | addLoan me → Dev ₹6,000 "Laptop repair", D0, 3 monthly from D+30 (`l-dev-laptop`) | Fri 30 Oct / Mon 30 Nov / Wed 30 Dec × ₹2,000; Dev +₹6,700 |
| `lendDevOverdue` | lendDev + setClock D+34T10:00 (Tue 3 Nov 2026 10:00) | installment 1 "Overdue 4 days"; reminders Wed 28 Oct, Fri 30 Oct, Mon 2 Nov |
| `devBuysGps` | updateComponent `c-drone-gps` → bought, ₹9,500, paid by Dev | ₹61,500 of ₹60,000, "₹1,500 over budget", shares ₹15,375 |
| `closeDrone` | closeProject `pj-drone` | Closed; "Rohan pays Dev ₹8,500", "Priya pays Dev ₹4,000", "₹8,000 under budget" |
| `weekendTrek` | createGroup Weekend Trek (trip, INR, me, Esha, Dev, Kabir) (`g-trek`) | "Trip · 4 members · INR", balance ₹0 |
| `allSettled` | confirmed payments for every plan row (me → Kabir ₹1,400, me → Meera ₹450, Rohan ₹800, Priya / Esha / Dev ₹700 → me) | Home all settled |
| `empty` | clearLedger | a new account (profile kept) |
| `pro` | setEntitlement pro, yearly, trial ends D+7 | Pro screens |

**Mapping to app-architecture's Seed column** (§1 there): `E` = `empty` · `D` = base + `eshaClaimsPayment` (architecture decision 3: the demo shows the pending claim; "Load demo data" applies it too) · `D−claim` = base alone (`homeActive`, `homeAddSheet`, `settleRemind`) · `D+settleAll` = base + `allSettled` (**without** the claim, or its card would remain) · `D+confirm Esha's claim` = base + `eshaPaymentConfirmed` · `D+Esha flags it` = base + `eshaClaimsPayment` + `eshaFlagsSeafood` · `D+Dev loan` = … + `lendDev` (`loanOverdue` adds `lendDevOverdue`'s clock) · `D P` = … + `pro` · New group created = `weekendTrek` · project states = `devBuysGps` / `closeDrone`. Screen-level UI state (prefilled forms, open sheets) stays with the owning screen.

---

## 8. Projects

### 8.1 Budget card
`spent` = Σ `actualCost` of bought/done parts. `plannedExtra` = Σ `estimatedCost` of planned parts. Projection = spent + plannedExtra (Active only). `% used` = round half up (spent ÷ budget × 100). Active & spent ≤ budget → "₹{budget − spent} left"; spent > budget → "₹{spent − budget} over budget" (red only for that and the bar overflow). Closed / archived → "₹{budget − spent} under budget" (or "… over budget"). Planned line: "Planned items bring it to ₹{projection}" or "All planned items are bought.". No budget → only "Spent".
Build a Drone: spent **₹52,000** of ₹60,000 → **87% used**, **₹8,000 left**, "Planned items bring it to **₹58,000**" (+ GPS module ₹6,000). Hackathon Kit: ₹18,400 of ₹20,000, **92%**, "₹1,600 under budget".

### 8.2 Fair share and nets
`paid[m]` = Σ actual of bought/done parts m paid; `share[m]` = split of `spent` by the contribution rule: equal (§4.2 equal), percent (bps), fixed (proportional to each member's fixed amount). `net[m] = paid − share + project payments sent − received`. Rows by net descending, ties by member order. Mini bars: scale = max(max paid, max share); fill = paid ÷ scale, mark at share ÷ scale.
Build a Drone (equal, ₹13,000 each): Dev 25,500 → **+₹12,500** · You 13,000 → **0** · Priya 9,000 → **−₹4,000** · Rohan 4,500 → **−₹8,500**; fills Dev 100 %, You 51 %, Priya 35 %, Rohan 18 %, mark 51 %.
Who owes whom = simplify (§5.3), amount descending: **Rohan owes Dev ₹8,500 · Priya owes Dev ₹4,000** ("pays" once closed). Footnote "You’re settled in this project." when your net is 0.
Project debts are real debts between people and count in friend nets (yours is 0 in the demo, so Home is unaffected). Project spending is excluded from Insights.

### 8.3 Lifecycle
Components: new → planned (paidBy you); an actual cost makes it bought (counts from then); done = finished, same money. List order: planned, bought, done; inside each, newest `statusChangedAt` first. Close project → status closed, components locked, projection hidden, plan frozen from the nets; `tick` archives it when every net is 0 (immediately if already 0). Archived = read-only permanent record ("Project · Closed {d MMM}").
Over budget (`devBuysGps`): spent ₹61,500 → shares ₹15,375 → Dev +₹19,625, You −₹2,375, Priya −₹6,375, Rohan −₹10,875.

### 8.4 Build a Drone parts (list order)
GPS module (planned, est. ₹6,000, you, 20 Sep) · Camera (bought ₹7,500, Dev, unplanned, 18 Sep) · Transmitter (₹5,000, you, 9 Sep) · ESCs and propellers (₹8,000, you, 2 Sep) · Battery (est. ₹5,000, actual ₹4,500, Rohan, 27 Aug) · Flight controller (₹9,000, Priya, 21 Aug) · Motors ×4 (done ₹12,000, Dev, 16 Aug) · Frame (done ₹6,000, Dev, 12 Aug).

---

## 9. Loans and installments
- Installment i (0-based) due = `firstDue + i periods`: weekly +7 d, biweekly +14 d, monthly = same day of month as `firstDue` (clamped). Default firstDue = loan date + 1 period. Amount = §4.3.
- Confirmed repayments fill installments **in due order**; an installment's paid date = the date of the repayment that completed it. Paid after its due → "Paid {d MMM} · {k} days late" (gray); unpaid and past due → red "Overdue {n} days"; else "Due {EEE d MMM}".
- Card: Original = amount; Paid = Σ confirmed repayments (bar capped at 100 %); Remaining = max(0, amount − paid); "{round(paid ÷ amount × 100)}% paid back"; Remaining 0 → "Paid back on {date of the last repayment}", chip "Paid back". Section "{n} monthly installments".
- A loan's remaining counts in the friend net and Home (Dev loan: Dev +₹6,700).
- Kabir: 12 Jul / 12 Aug / 12 Sep × ₹1,500, repaid 10 Jul / 12 Aug / 14 Sep → "Paid 10 Jul", "Paid 12 Aug", "Paid 14 Sep · 2 days late". Dev (`lendDevOverdue`, Tue 3 Nov): "Overdue 4 days", reminders Wed 28 Oct, Fri 30 Oct, Mon 2 Nov → "Last reminder sent Mon 2 Nov".

---

## 10. The scheduler: `tick(now)`
Runs at launch, on foreground, after `setClock`, and from the debug menu. It walks each day from `scheduler.cursor` to `now` and runs, for every moment in `(cursor, now]`, in this order:
| Time | Job |
|---|---|
| 09:00 | **Recurring**: for each active rule whose next occurrence after `lastOccurrence` is this day: fixed → create the expense (dated the occurrence, `createdBy` = rule creator, split per rule, rotation applied); variable → create a draft. Set `lastOccurrence`. |
| 09:00 | **Overdue alerts**: each open item owed to you whose due date was yesterday → inbox `paymentOverdue`. |
| 20:00 | **Monthly summary** on the month's last day → inbox `monthlySummary` with the Insights total and Home totals **as of that moment** (so tonight's 19:40 dinner counts: ₹23,300 / ₹2,900). |
| 21:00 (`reminderSchedule.time`) | **Reminders**: for each open item with a due date where the day is a fire day (2 days before if on; the due date if on; every 3 days after it while overdue if on): owed to you → a Reminder (automatic) to that friend unless muted; you owe → inbox `paymentReminder`. |
Then: purge expenses deleted ≥ 30 days ago, archive closed projects whose nets are all 0, set `cursor = now`. Open items are evaluated **as of** each moment (§5.1). Ids are deterministic (`rem-{ref}-{yyyyMMdd}`, `n-summary-{yyyyMM}` …) and past moments never re-run, so `tick` is idempotent.

Next occurrence: monthly = the anchor's day of month in the month after `lastOccurrence` (clamped); weekly = the anchor's weekday; yearly = the anchor's day and month. Demo: Rent "Next Thu 1 Oct", Wi-Fi "Next Mon 5 Oct", Cooking gas "Next Wed 28 Oct" (its September draft exists). Rule subtitle "Monthly on the {ordinal}".

Worked (Rohan, due Sun 27 Sep): Fri 25 (−2), Sun 27 (due), Wed 30 (+3), next Sat 3 Oct. Kabir / Goa Trip (due Fri 2 Oct): Wed 30 Sep 21:00 → "You owe Kabir ₹1,400 for Goa Trip. It’s due Friday.".

### 10.1 Real OS notifications
The inbox item is created by `tick` when its moment passes; the **OS notification** must be scheduled ahead (the app is usually closed at 21:00): after every ledger change, (re)schedule local notifications for the next fire times of your own debts (Reminders toggle), the month-end summary (Monthly summary toggle) and overdue alerts (Overdue alerts toggle), and cancel ones whose debt is settled. A claim (a friend's payment to you) posts "Payment to confirm" with Confirm / Not received at once (Payments to confirm toggle). Copy and links: screens-activity §7, app-architecture §2.6.

---

## 11. Actions (the stable store API; semantics and side effects)
Names as in app-architecture §3.4. Every action validates, updates records, appends history / reminder / inbox entries, and never touches derived data.
- `addExpense(draft)`: validates (amount > 0, someone else on it, split valid); computes shares with rotation; `history: [created]`; inbox `newExpenseInGroup` for the others is simulated (not stored on your device); creates the recurring rule when Repeat is set.
- `updateExpense(id, draft)`: recomputes shares only if amount/people/split changed; appends one history entry per changed field; clears `flag`.
- `deleteExpense(id)` / `restoreExpense(id)`: soft delete with `deletedBy`; history `deleted` / `restored`.
- `addComment(expenseId, text, by)`; `flagExpense(id, by, note)` (history `flagged`); `resolveFlag(id)` (history `flagResolved`, "You resolved Esha’s flag").
- `enterDraftAmount(draftId, amount)`: creates the expense from the rule (dated the occurrence), links the draft.
- `recordPayment(draft)`: status per §1.5; `cancelPayment(id)`; `confirmPayment(id)` (+ inbox `paymentConfirmed` when it's to you); `markNotReceived(id, note)`.
- `addLoan(draft)`; repayments go through `recordPayment` with `loanId`.
- `addGroup(draft)` (group or project; `simplifyDebts` true, currency = profile currency by default); `updateGroup`; `addMembers`; `removeMember` / `leaveGroup` throw when the net ≠ 0.
- `addComponent`, `updateComponent`, `closeProject` (archives at once if every net is 0).
- `sendReminder(personId, context, tone, message, via)` → Reminder (automatic = false). `setRemindersMuted(personId, bool)`.
- `markInboxRead(id)`, `markAllInboxRead()`.
- `addRecurringRule`, `updateRecurringRule`, `deleteRecurringRule` (sets `active = false`; generated expenses stay).
- `startTrial(plan)` / `subscribe(plan)` / `setPro(bool)`; `updateSettings`.
- `tick(now)` (§10); `clear()`; `loadDemo(anchor, scenarios)` (debug).

---

## 12. Figma inconsistencies and how they're resolved
1. **Demo base with or without Esha's claim.** Home — Active has no claim; Activity 09-01, Notifications 09-08 and the lock screen 09-09 show it pending. → The base records are the Home — Active moment; the claim is the `eshaClaimsPayment` scenario. app-architecture decision 3 ("D includes the claim") = base + that scenario (§7.4 mapping).
2. **Reminder time.** The Settings spec proposes 09:00; Figma derives 9:00 pm (lock screen at 9:00, inbox "9:00 pm", "tonight’s to Rohan"). → 21:00 (`reminderSchedule.time`).
3. **"You owe ₹450" vs "Your share ₹500"** on timeline rows; the activity spec's proposal ("Your share" whenever the group simplifies) would print "Your share" for Flat 302, which Figma writes as "You owe". → Rule in §6.6 (depends on who you pay), reproduces both rows.
4. **Home "Due soon" has no stated rule.** → Overdue or due within 2 days (the first reminder's lead time), which lists Rohan and Goa Trip and leaves out Olive Garden (4 days) and Flat 302 (5 days), as drawn.
5. **Monthly summary "You’re owed ₹2,900"** while Arjun also owes ₹1,850: the body names the owed total, not a net. → "You’re owed {owed}." when owed > 0, else "You owe {owe}.", else "You’re all square."
6. **Converting a foreign group at "the rate saved with each expense"** can't keep a settled AED group at exactly ₹0 when payments were saved at other rates. → Group balances live in the group currency; an open one converts at the group's latest saved rate (§5.4).
7. **Export caption** lists "the dinner, the movie tickets and the groceries, plus Priya’s ₹1,050 payment" for September, but Kabir's last loan repayment (14 Sep) is also a September record outside groups. → It's included in the export; the tick states are the same. The "All time" example "12 Mar – …" contradicts Dubai's 6 Mar records → the range starts at the earliest record (6 Mar in the demo).
8. **Ananya** is a guest friend on Friends (07-02) and Split with (06-03) but an un-added contact with "Invite" on Add friend (07-09). → She's a guest friend in the base; Add friend lists guests who aren't on Paybak with "Invite" (sends the invite link).
9. **Groceries' category.** The assistant keyword table maps groceries → Shopping, but Insights needs Weekend groceries in Food (Food ₹3,850). → The expense is Food; move "groceries" to Food in the keyword table.
10. **Villa's "Due Fri 2 Oct"** although group expenses have no due date. → Effective due = `dueDate ?? group.settleBy`.
11. **Comments on Sun 27 / Mon 28 Sep** would add timeline rows and inbox items that Figma doesn't draw. → Comments create neither (they notify only as a proposal push, not stored).
12. **Home Recent activity vs the timeline** show different amounts for the same expense (−₹450 your share vs ₹1,350 total). → Both are kept; each surface has its row rule (§6.1, §6.6).
13. **Figma leaves out**: Flat 302's third member (Kabir, from page 11), the College Gang members (Arjun, Rohan, Priya, Esha, Dev, Kabir, so no unseen friend appears in the Friends list), the Farewell dinner's payer (Kabir, so Rohan's history stays two rows), Snacks' payer (Priya), Meera's UPI (meera@okhdfcbank from page 06), the Weekend groceries expense (₹2,100, Sat 19 Sep, split with Priya, from the ₹1,050 payment, Insights and Export), Hackathon Kit's parts and payments (projects spec proposal), the April–August history behind the Insights chart (its monthly totals were a spec proposal; the seed produces them from real expenses).
14. **"Next week" chip**: today + 7 (add-expense spec) vs next Monday (record-lend spec). → today + 7; one implementation for both forms.
15. **Kabir's loan reminders**: Figma shows none; the default schedule would have sent them (10 Aug, 10 Sep, 12 Sep), so the log has them. They're only visible as "Last reminder sent" on an unpaid loan, which this one isn't.
