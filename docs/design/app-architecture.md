# Paybak full app (v2): app architecture and build plan

Written 1 Oct 2026 as the architecture and build plan for everything after M1 (onboarding). The build ran as **M2 Data layer** on each platform, then **four parallel lanes per platform** (A, B, C, D), merged into `main` one at a time, and the app is now built from it. The module and lane labels (M1–M9, lanes A–D) are kept because the code is organised the same way (`// MARK: Lane A` … `Lane D` sections in `Route.swift` / `Route.kt`, `Data/Lanes/` / `data/ledger/lanes/`, per-module debug scenarios and menus). §1–§5, §7.2 and §8 describe the app as built; §6 and §7.1 record how the work was split and what each module had to deliver.

**Precedence.** flow.md ("Testing on devices", "Resolved decisions", "UI tests and test IDs", "FULL APP SCOPE") > this file for structure, ids, routes, ownership and data-layer shape > `domain.md` + `seed/` for business logic and numbers > the screen specs `screens-*.md` for layout, copy and per-screen behaviour > `components-app.md` / `components-home.md` / `components-core.md` > `tokens.md`. When this file and `domain.md` disagree about a calculation or a demo value, `domain.md` wins; when they disagree about file layout, ownership or API shape, this file wins.

Contents
1. Screen inventory (every id, container, spec, ref, owner, seed)
2. Routes and navigation map
3. Data layer (domain, store, persistence, demo seed, clock, debug hooks)
4. Real vs simulated, per feature (APIs and dependencies)
5. Components: shared kit (M2) and lane-owned components
6. Modules: scope, screens, acceptance, UI tests, commit subjects (M2, lanes A–D)
7. Conflict-avoidance rules and cross-cutting UI rules
8. Decisions taken here (for the record) and open items

Conventions: iOS paths are under `ios/paybak/paybak/`, Android paths under `android/app/src/main/java/app/paybak/paybak/` (`src/debug/…` / `src/release/…` for build-type source sets). "Figma parity" means the demo dataset loaded with the clock pinned to the demo anchor (§3.8).

---

## 1. Screen inventory

Every screen, sheet, alert and designed state has a unique **screen id**. The ids are the values of the debug start-screen hook (`-startScreen <id>` on iOS, `--es startScreen <id>` on Android) and the names of the refs. Ids that are *states* of one route say so in the Route column (`route · state`). Refs are 2× (804 × 1748) in `docs/design/ref/<id>.png`; the Home ids use the current renders in `docs/design/ref/v2/<id>.png` (curly ’ in "You’re owed"). All 93 Figma ids have a ref (checked; nothing needed copying from the REST renders). Ids marked *(proposal)* aren't drawn in Figma; they exist so the undrawn state can be started and tested.

**Seed column** (what the debug start hook loads before showing the screen; §3.10 has the mechanics):
- `E` = empty account: sample profile, empty ledger (a new user after onboarding).
- `D` = the demo at Figma parity (load day Wed 30 Sep 2026, clock pinned to 21:15, §3.8), free plan: the base records of `seed/demo.json` **plus the `eshaClaimsPayment` scenario**, so Esha's ₹700 claim is pending as on Activity, Notifications and the lock screen (domain.md §7.4 mapping). "Load demo data" in the debug menu loads the same.
- `D−claim` = the base records alone (the Home — Active moment).
- `D+x` / `D−claim+x` = the same plus the seed scenario `x` from `demo.json` → `scenarios` (e.g. `lendDevOverdue`), run through the store's actions (§3.8).
- `P` = the seed scenario `pro` (yearly trial, ends D+7) on top (`D P`).
- `UI: …` = in-screen state the **owning screen** applies itself when started with this id (form prefill, open local sheet, alert shown, scroll offset, focused field). Everything else (data, clock, Pro, route stack, route-level sheets and modals) is applied by the M2 scenario table.

### 1.1 Launch and onboarding (M1, done; unchanged)
| id | Route | Container | Spec | Owner | Seed |
|---|---|---|---|---|---|
| `splash` | onboarding root | root | screens-launch §1 | M1 | – |
| `welcome1` `welcome2` `welcome3` | onboarding · step | stack root | screens-launch §2 | M1 | – |
| `getStarted` | onboarding | push | screens-launch §3 | M1 | – |
| `signIn` | onboarding | push | screens-signin §1 | M1 | sample profile |
| `verify` / `verifyWrong` | onboarding · error | push | screens-signin §2–3 | M1 | sample profile |
| `setup1`…`setup4` | onboarding | push (one Setup screen on Android) | screens-setup §1–4 | M1 | sample profile |
| `allSet` | onboarding | push | screens-setup §5 | M1 | sample profile |

### 1.2 Home (lane D)
| id | Route · state | Container | Spec | Ref | Owner | Seed |
|---|---|---|---|---|---|---|
| `homeFirstDay` | `home` · first day | tab root | screens-home §3, home-v2 §2.3 | v2/homeFirstDay | D | E |
| `homeActive` | `home` · active | tab root | screens-home §2, home-v2 §2.2 | v2/homeActive | D | D−claim |
| `homeAllSettled` | `home` · all settled | tab root | screens-home §4 | v2/homeAllSettled | D | D−claim+allSettled |
| `homeConfirmPayment` | `home` · pending claim | tab root | home-v2 §3 | homeConfirmPayment | D | D |
| `settlePaymentConfirmed` | `home` · after Confirm | tab root + toast | settle §9, home-v2 §3.9 | settlePaymentConfirmed | D | D−claim+eshaPaymentConfirmed; toast "Payment confirmed" |
| `homeAddSheet` | `addSheet` over `home` | sheet (action sheet) | screens-home §5, home-v2 §2.4 | v2/homeAddSheet | M2 | D−claim |
| `debugMenu` | `debugMenu` over `home` | sheet (debug builds) | flow.md debug hooks, §3.10 | – | M2 | D |

### 1.3 Add & Record (lane A, M3)
| id | Route · state | Container | Spec | Owner | Seed |
|---|---|---|---|---|---|
| `addExpenseEmpty` | `addExpense` · empty | full-screen modal | add-expense §4 | A | D; UI: amount focused |
| `addExpenseFilled` | `addExpense` · filled | modal | add-expense §4.7 | A | D; UI: Olive Garden draft (§12 of the spec), keyboard down |
| `addExpenseSplitWith` | `pickPeople` pushed in the addExpense modal | push (in modal) | add-expense §5 | A | D; UI: filled draft |
| `addExpensePaidBy` | `addExpense` · Paid by sheet | local sheet Medium | add-expense §6 | A | D; UI: filled draft + sheet |
| `addExpensePayers` *(proposal)* | `addExpense` · payer editor page | local push | add-expense §6.3 | A | D; UI |
| `addExpenseSplitEqually` | `addExpense` · split editor page | local push (in modal) | add-expense §7.1 | A | D; UI: filled draft |
| `addExpenseSplitExactError` | `addExpense` · split editor, Exact | local push | add-expense §7.2 | A | D; UI: Exact, Dev = 550 focused |
| `addExpenseCategory` | `addExpense` · Category sheet | local sheet Large | add-expense §8 | A | D; UI |
| `addExpenseCurrency` | `pickCurrency` over addExpense | route sheet Large | add-expense §9 | A | D; UI: filled draft |
| `addExpenseDueDate` | `pickDate` (due) over addExpense | route sheet Medium | add-expense §10 | A | D; UI: Sun 4 Oct |
| `addExpenseDate` *(proposal)* | `pickDate` (date) | route sheet Medium | add-expense §10.3 | A | D |
| `addExpenseDiscard` *(proposal)* | `addExpense` · discard alert | alert | add-expense §3.15 | A | D; UI |
| `expenseAdded` | `expense(e-olive)` + toast | push on the Home stack | add-expense §11 | A | D; toast "Expense added" (opens the existing expense, no duplicate) |
| `recordPayment` | `recordPayment` · new | modal | record-lend-group §2 | A | D; args: You → Meera ₹450, Flat 302 |
| `settleRecordKabir` | `recordPayment` · prefilled | modal | settle §4 | A (form) / B (entry) | D; args: You → Kabir ₹1,400, UPI, Goa Trip |
| `paymentRecorded` | `payment(id)` · pending, payer | push | record-lend-group §3 | A | D+paymentToMeeraPending; toast "Payment recorded" |
| `settlePaymentPending` | `payment(id)` · pending (UPI, 7 rows) | push over `settleUp` | settle §5 | A (screen) / B (entry) | D+paymentToKabirPending; toast |
| `paymentCancelAlert` *(proposal)* | `payment` · cancel alert | alert | record-lend-group §3.4 | A | as paymentRecorded; UI |
| `lendMoney` | `lendMoney` | modal | record-lend-group §4 | A | D; UI: Figma prefill |
| `loanAdded` | `loan(l-dev-laptop)` · active | push + toast | record-lend-group §5.2 | A | D+lendDev; toast "Loan added" |
| `loanPaidBack` | `loan(l-kabir-bike)` · paid back | push | record-lend-group §5.3 | A | D |
| `loanOverdue` | `loan(l-dev-laptop)` · overdue | push | record-lend-group §5.4 | A | D+lendDevOverdue (clock D+34T10:00 = Tue 3 Nov) |
| `newGroup` | `newGroup` · group | modal | record-lend-group §6.2 | A | D; UI: Figma prefill |
| `newGroupProject` | `newGroup` · project | modal | record-lend-group §6.3 | A | D; UI: Figma prefill |
| `newGroupCreated` | `group(g-trek)` · empty | push on the Groups tab + toast | record-lend-group §7 | **B** (group detail file); A owns the flow into it | D+weekendTrek; toast "Group created" |

### 1.4 Groups & Friends (lane B, M4)
| id | Route · state | Container | Spec | Owner | Seed |
|---|---|---|---|---|---|
| `groupsList` | `groups` · Groups segment | tab root | groups §3.2 | B | D |
| `friendsList` | `groups` · Friends segment | tab root | groups §3.3 | B | D |
| `groupsEmpty` | `groups` · empty | tab root | groups §3.4 | B | E |
| `friendsEmpty` *(proposal)* | `groups` · Friends, none | tab root | groups §13 | B | E |
| `groupGoaTrip` | `group(g-goa)` | push | groups §4 | B | D |
| `groupDubaiWeekend` | `group(g-dubai)` · foreign currency, settled | push | groups §4.8 | B | D |
| `groupSettings` | `groupSettings(g-goa)` | push | groups §5 | B | D |
| `groupLeaveBlocked` | `groupSettings` · leave alert | alert | groups §5.6 | B | D; UI: scrolled 26, alert |
| `friendRohan` | `friend(p-rohan)` · owes you, overdue | push (from Friends) | groups §6 | B | D |
| `friendAnanyaGuest` | `friend(p-ananya)` · guest | push | groups §6.7 | B | D |
| `addFriend` | `addFriend` | push | groups §7 | B | D |
| `myQrCode` | `addFriend` · My QR code sheet | local sheet Medium | groups §7.6 | B | D; UI: sheet |

### 1.5 Settle up (lane B, M5)
| id | Route · state | Container | Spec | Owner | Seed |
|---|---|---|---|---|---|
| `settleOwedBreakdown` | `owedBreakdown` | push (Home) | settle §1 | B | D |
| `settleOweBreakdown` | `oweBreakdown` | push (Home) | settle §2 | B | D |
| `settleUp` | `settleUp()` | push (Home) | settle §3 | B | D |
| `settleRemind` | `remind(p-rohan)` over `home` | route sheet Medium | settle §6 | B | D−claim |
| `settleRemindShare` | `remind` + system share sheet | system UI | settle §7 | B | D−claim; UI: share sheet open |
| `settleNotReceived` | `notReceived(pay-esha-olive)` over `home` | route sheet Medium, no header | settle §8 | B | D |
| (`settleRecordKabir`, `settlePaymentPending`, `settlePaymentConfirmed` are listed with their screens' owners in §1.3 and §1.2) | | | | | |

### 1.6 Activity, expense detail, notifications (lane A, M6)
| id | Route · state | Container | Spec | Owner | Seed |
|---|---|---|---|---|---|
| `activityTimeline` | `activity` · Timeline segment | tab root | activity §3 | A (content), M2 (tab container) | D |
| `activityEmpty` | `activity` · Timeline, empty | tab root | activity §3.8 | A | E |
| `expenseVilla` | `expense(e-goa-villa)` | push (from Activity) | activity §4 | A | D |
| `expenseComment` | `expense(e-goa-villa)` · composing | push, keyboard up | activity §4.6 | A | D; UI: composer focused, text "Thanks, that works for me." |
| `expenseDelete` | `expense(e-goa-villa)` · delete alert | alert | activity §4.7 | A | D; UI: alert |
| `expenseDisputed` | `expense(e-goa-seafood)` · disputed | push | activity §4.8 | A | D+eshaFlagsSeafood |
| `recentlyDeleted` | `recentlyDeleted` | push | activity §5 | A | D |
| `notifications` | `notifications` | push (Home bell) | activity §6 | A | D |
| `activityLog` *(proposal)* | `activityLog(filter)` | push | activity §3.9, projects §3.10, groups §6.4 | A | D; filter project `pj-drone` |
| `lockConfirmRequest` | system notification (claim) | local notification | activity §7.1 | A | D; posts the notification on launch |
| `lockReminder` | system notification (reminder) | local notification | activity §7.2 | A | D; posts the Kabir reminder on launch |

### 1.7 Projects (lane B, M7)
| id | Route · state | Container | Spec | Owner | Seed |
|---|---|---|---|---|---|
| `projectDrone` | `project(pj-drone)` · active, on track | push (Groups) | projects §3 | B | D |
| `projectOverBudget` | `project(pj-drone)` · over budget | push | projects §4 | B | D+devBuysGps |
| `projectAddComponent` | `project` · Add component sheet | local sheet Medium | projects §5 | B | D; UI: sheet |
| `projectSettings` | `projectSettings(pj-drone)` | push | projects §6 | B | D |
| `projectCloseAlert` *(proposal)* | `projectSettings` · close alert | alert | projects §6.6 | B | D; UI |
| `projectClosed` | `project(pj-drone)` · closed | push | projects §7 | B | D+closeDrone |
| `projectArchived` | `project(pj-hackathon)` · archived | push | projects §8 | B | D |

### 1.8 Profile, Settings & Pro (lane C, M8)
| id | Route · state | Container | Spec | Owner | Seed |
|---|---|---|---|---|---|
| `profile` | `profile` | tab root | profile §2 | C | D + avatar = character(default Boy) |
| `profileSignOut` *(proposal)* | `profile` · sign-out alert | alert | profile §2.3 | C | as profile; UI |
| `editAvatarBoyHair` `editAvatarBoyBeard` `editAvatarBoyEyewear` `editAvatarBoyOutfit` `editAvatarGirlHair` `editAvatarGirlAccessory` `editAvatarGirlOutfit` | `editAvatar` · gender/category/draft | push (tab bar hidden) | profile §3, states §3.7 | C | as profile; UI: draft per profile §3.7 |
| `editAvatarDiscard` | `editAvatar` · discard alert | alert | profile §4 | C | as profile; UI: Boy · Outfit draft + alert |
| `paywall` | `paywall(continueTo: nil)` | modal (over Profile) | settings §2 | C | D (free) |
| `proWelcome` | `paywall(continueTo: privacyExport)` · welcome | modal | settings §3 | C | D P (trial started today) |
| `paymentDetails` | `paymentDetails` | push | settings §4 | C | D |
| `paymentAddUpi` | `paymentDetails` · Add sheet | local sheet Medium | settings §5 | C | D; UI: "arjun@okhdfcbank" focused |
| `paymentAddUpiError` | `paymentDetails` · Add sheet error | local sheet | settings §5.6 | C | D; UI: "arjunokhdfcbank" + error |
| `settingsCurrency` | `settingsCurrency` | push | settings §6 | C | D |
| `settingsNotifications` | `settingsNotifications` | push | settings §7 | C | D |
| `mutedFriends` *(proposal)* | `mutedFriends` | push | settings §7 "Muted friends" | C | D |
| `privacyData` | `privacyData` | push | settings §8 | C | D (free) |
| `privacyExport` | `privacyExport` | push (over privacyData) | settings §9 | C | D P |
| `privacyDeleteBlocked` | `privacyData` · blocked alert | alert | settings §10 | C | D; UI: alert |
| `helpFeedback` | `helpFeedback` | push | settings §11 | C | D |
| `helpAnswer` *(proposal)* | `helpAnswer(1)` | push | settings §11 FAQ | C | D |

### 1.9 Insights & AI (lane C, M9)
| id | Route · state | Container | Spec | Owner | Seed |
|---|---|---|---|---|---|
| `insightsSeptember` | `activity` · Insights segment, Sep 2026 | tab root | insights §2.2 | C (content), M2 (tab container) | D P |
| `insightsScrolled` | same · scrolled 676 | tab root | insights §2.4 | C | D P; UI: scroll offset 676 |
| `insightsLocked` | same · free plan | tab root | insights §2.5 | C | D |
| `askStart` | `ask` · start | modal (Home sparkle) | insights §3.2 | C | D P |
| `askAnswer` | `ask` · "Who owes me money?" | modal | insights §3.3 | C | D P; UI: prompt sent |
| `askConfirm` | `ask` · draft expense | modal | insights §3.4 | C | D P; UI: "Add ₹600 for a cab, split with Esha and Dev" sent |
| `scanCamera` | `scanReceipt` · camera, over addExpense | modal over modal | insights §4.2 | C | D P; simulated camera feed |
| `scanReview` | `scanReceipt` · Check receipt | local push in scan modal | insights §4.3 | C | D P; UI: sample receipt read |
| `scanAssign` | `scanReceipt` · Assign items | local push | insights §4.4 | C | D P; UI: drawn assignment |
| `scanAddExpense` | `addExpense` · prefilled from scan | modal | insights §4.5 | A (form) / C (result) | D P; args: Leopold itemized draft |
| `recurringFlat302` | `recurring(g-flat302)` | push (group › Recurring) | insights §5.2 | C | D P |
| `recurringRepeat` | `repeatRule` over `addExpense` (Cooking gas, Flat 302) | route sheet Medium | insights §5.3 | C (sheet) / A (form) | D P; args: Cooking gas draft |
| `recurringEnterAmount` | `enterDraftAmount(d-gas-09)` | modal | insights §5.4 | C | D P |

System UI that isn't an app screen (no id): the OS photo picker, share sheet, notification permission prompt, contacts permission, QR scanner UI, mail composer, review prompt.

---

## 2. Routes and the navigation map

### 2.1 Presentation model (both platforms)
The app has three roots, as today: **splash**, **onboarding** (the M1 stack, unchanged) and **main** (replaces today's `home` root). Main is one navigation model:

```
main
 ├─ TabShell (bottom of the main stack): Home | Groups | ＋ | Activity | Profile
 │     the glass tab bar is drawn ONLY here; every pushed or modal screen covers it (Figma: no pushed frame has a tab bar)
 ├─ main stack: pushes over the TabShell (tab bar hidden while anything is pushed)
 ├─ sheet slot of the main stack (Add sheet, Remind, Not received, pickers, debug menu)
 └─ modal layers [0…n]: each = root route + its own push stack + its own sheet slot
```
- **Push**: onto the top-most layer (the main stack, or the top modal's stack). Native push on iOS (350 ms); Android `pushTransition()` / `popTransition()` (existing, 350 ms ease-in-out).
- **Modal** (full-screen, Modal Header with ✕ on the left): a new layer. iOS `.fullScreenCover` per layer (nested covers for modal-over-modal); Android slide up 300 ms ease-out, slide down on dismiss.
- **Sheet** (Sheet / Container, Medium or Large, floating 8 pt from the edges, radius 40): the top layer's sheet slot. Custom overlay on both platforms (`PBSheet` / `PbSheet`, same motion as the Add sheet: MOVE_IN from the bottom 300 ms ease-out with the 40 % scrim fading in). One route sheet at a time per layer.
- **Local** sheets, pages and alerts (used by one owner only) stay inside the owning screen's files and are not routes (§2.4 marks them "local").
- **Tab switch**: `select(tab)` dismisses all modals and sheets, pops the main stack to the TabShell and selects the tab. Each tab root keeps its own UI state (scroll position, segment) while the app runs.
- There is one main stack, not one per tab: pushes hide the tab bar, so the user can't switch tabs while pushed; deep links and helpers call `select(tab)` first.

### 2.2 Route catalog
Every route is defined in M2 (iOS `Navigation/Route.swift` `enum Route: Hashable, Codable`; Android `navigation/Route.kt` `@Serializable sealed interface Route`). Ids, parameters and presentation are identical on both platforms. Each route has exactly **one screen file**, created by M2 as a placeholder and replaced by the owner. Screen files: iOS `Features/<Folder>/<Name>Screen.swift` (`struct <Name>Screen: View` with the listed initializer), Android `feature/<package>/<Name>Screen.kt` (`@Composable fun <Name>Screen(route: Route.<Name>)`).

| Route id | Parameters | Presentation | Owner | Screen file (iOS / Android) | Screen ids |
|---|---|---|---|---|---|
| `home` | – | tab | D | Home/HomeScreen · home/HomeScreen | homeFirstDay, homeActive, homeAllSettled, homeConfirmPayment, settlePaymentConfirmed |
| `groups` | – (segment in router state) | tab | B | Groups/GroupsTabScreen · groups/GroupsTabScreen | groupsList, friendsList, groupsEmpty |
| `activity` | – (segment + month in router state) | tab | M2 container; A timeline; C insights | Activity/ActivityTabScreen (M2) + Activity/ActivityTimelineView (A) + Insights/InsightsView (C); Android same names in activity/ and insights/ | activityTimeline, activityEmpty, insights* |
| `profile` | – | tab | C | Profile/ProfileScreen · profile/ProfileScreen | profile |
| `addSheet` | – | sheet | M2 | Navigation/AddSheet · navigation/AddSheet | homeAddSheet |
| `notifications` | – | push | A | Notifications/NotificationsScreen | notifications |
| `owedBreakdown` | – | push | B | Settle/OwedBreakdownScreen | settleOwedBreakdown |
| `oweBreakdown` | – | push | B | Settle/OweBreakdownScreen | settleOweBreakdown |
| `settleUp` | `groupId: GroupID?` (nil = everyone) | push | B | Settle/SettleUpScreen | settleUp |
| `remind` | `personId`, `context: ReminderContext?` (expense/group/loan the reminder is about) | sheet Medium | B | Settle/RemindSheet | settleRemind, settleRemindShare |
| `notReceived` | `paymentId` | sheet Medium (no header) | B | Settle/NotReceivedSheet | settleNotReceived |
| `friend` | `personId` | push | B | Friends/FriendScreen | friendRohan, friendAnanyaGuest |
| `addFriend` | – | push | B | Friends/AddFriendScreen | addFriend, myQrCode |
| `group` | `groupId` | push | B | Groups/GroupDetailScreen | groupGoaTrip, groupDubaiWeekend, newGroupCreated |
| `groupSettings` | `groupId` | push | B | Groups/GroupSettingsScreen | groupSettings, groupLeaveBlocked |
| `project` | `groupId` | push | B | Projects/ProjectScreen | projectDrone, projectOverBudget, projectAddComponent, projectClosed, projectArchived |
| `projectSettings` | `groupId` | push | B | Projects/ProjectSettingsScreen | projectSettings, projectCloseAlert |
| `expense` | `expenseId`, `toast: String?` | push | A | Expense/ExpenseDetailScreen | expenseVilla, expenseComment, expenseDelete, expenseDisputed, expenseAdded |
| `payment` | `paymentId` | push | A | Payments/PaymentDetailScreen | paymentRecorded, settlePaymentPending, paymentCancelAlert |
| `loan` | `loanId` | push | A | Loans/LoanScreen | loanAdded, loanPaidBack, loanOverdue |
| `recentlyDeleted` | – | push | A | Activity/RecentlyDeletedScreen | recentlyDeleted |
| `activityLog` | `filter: ActivityFilter` (person / group / project / category+month) | push | A | Activity/ActivityLogScreen | activityLog |
| `pickPeople` | `request: PickRequest` (+ `mode: multi|single`, `selected`, `title`, `allowsGuests`) | push | A | Pickers/PeoplePickerScreen | addExpenseSplitWith |
| `pickCurrency` | `request` (+ `selected`, `title`) | sheet Large | A | Pickers/CurrencyPickerSheet | addExpenseCurrency |
| `pickDate` | `request` (+ `kind: date|dueDate`, `selected`, `allowsNone`, `range`) | sheet Medium | A | Pickers/DatePickerSheet | addExpenseDueDate, addExpenseDate |
| `pickGroup` | `request` (+ `selected`) | sheet Medium | A | Pickers/GroupPickerSheet | – |
| `photoViewer` | `photo: PhotoRef` | modal | A | Expense/PhotoViewerScreen | – |
| `addExpense` | `args: AddExpenseArgs` (`editing: ExpenseID?`, `draft: ExpenseDraft?`, `focusAmount: Bool`) | modal | A | AddExpense/AddExpenseScreen | addExpense*, scanAddExpense |
| `recordPayment` | `args: RecordPaymentArgs` (`editing`, `from`, `to`, `amount`, `currency`, `method`, `context`) | modal | A | Payments/RecordPaymentScreen | recordPayment, settleRecordKabir |
| `lendMoney` | `args: LendMoneyArgs` (`editing: LoanID?`, `person`, `direction`) | modal | A | Loans/LendMoneyScreen | lendMoney |
| `newGroup` | `mode: group|project` | modal | A | NewGroup/NewGroupScreen | newGroup, newGroupProject |
| `recurring` | `groupId` | push | C | Recurring/RecurringScreen | recurringFlat302 |
| `repeatRule` | `request` (+ `current: RepeatRule?`, `startDate`) | sheet Medium | C | Recurring/RepeatSheet | recurringRepeat |
| `enterDraftAmount` | `draftId` | modal | C | Recurring/EnterAmountScreen | recurringEnterAmount |
| `ask` | – | modal | C | Ask/AskScreen | askStart, askAnswer, askConfirm |
| `scanReceipt` | `request` | modal | C | Scan/ScanReceiptScreen | scanCamera, scanReview, scanAssign |
| `paywall` | `continueTo: Route?` | modal | C | Pro/PaywallScreen | paywall, proWelcome |
| `editAvatar` | – | push | C | Avatar/EditAvatarScreen | editAvatar*, editAvatarDiscard |
| `paymentDetails` | – | push | C | Settings/PaymentDetailsScreen | paymentDetails, paymentAddUpi, paymentAddUpiError |
| `settingsCurrency` | – | push | C | Settings/CurrencySettingsScreen | settingsCurrency |
| `settingsNotifications` | – | push | C | Settings/NotificationSettingsScreen | settingsNotifications |
| `mutedFriends` | – | push | C | Settings/MutedFriendsScreen | mutedFriends |
| `privacyData` | – | push | C | Settings/PrivacyScreen | privacyData, privacyDeleteBlocked |
| `privacyExport` | – | push | C | Settings/ExportScreen | privacyExport |
| `helpFeedback` | – | push | C | Settings/HelpScreen | helpFeedback |
| `helpAnswer` | `index: Int` | push | C | Settings/HelpAnswerScreen | helpAnswer |
| `debugMenu` | – | sheet Large (debug builds only) | M2 | Debug/DebugMenu (iOS) · src/debug …/debug/DebugMenu.kt | debugMenu |

Test-ID root of every route screen: `screen.<routeId>` (e.g. `screen.expense`, `screen.project`, `screen.pickPeople`). Designed states are visible in content; where a UI test needs the state explicitly, the screen adds `<routeId>.state.<state>` on a hidden element. **Exceptions kept for compatibility:** onboarding and Home keep the M1 roots `screen.<screenId>` (`screen.homeActive`, …) and `home.state.<firstDay|active|allSettled|confirmPayment>`; the tab bar keeps `home.tab.<home|groups|add|activity|profile>` on every tab.

### 2.3 Tab bar, ＋ sheet and the Home header
- **Tab bar** (M2 `PBTabBar`): Home · Groups · ＋ · Activity · Profile, floating glass at x20, 21 above the bottom edge. Tapping the selected tab while it's already selected scrolls its root to the top (proposal; no other effect). Test ids `home.tab.*`.
- **＋ from every tab** opens `addSheet` over the current tab root (MOVE_IN 300 ms). Rows: Add expense → `addExpense(new)`, Record payment → `recordPayment(new)`, Lend money (IOU) → `lendMoney(new)`, New group → `newGroup(.group)`. A row tap dismisses the sheet, then presents the modal (`router.replaceSheet(with:)` helper). Scrim / ✕ dismiss. Test ids `home.addSheet`, `home.addSheet.close`, `home.addSheet.<expense|payment|lend|group>`.
- **Tab root content** scrolls under the tab bar with a bottom content inset of 107 pt (tab bar 62 + 21 + 24) and Home's scroll-edge fade where the spec draws it.

**Home header and cards** (screens-home-v2 §2; lane D wires them):
| Element | Destination | Presentation |
|---|---|---|
| Logo long-press | `debugMenu` (debug builds) | sheet |
| Sparkle (`home.assistant`) | `requirePro(.ask)`: Pro → `ask`; free → `paywall(continueTo: .ask)` | modal |
| Bell (`home.bell`, unread dot = any unread inbox item) | `notifications` | push |
| You’re owed card | `owedBreakdown` | push |
| You owe card | `oweBreakdown` | push |
| Settle up | `settleUp(nil)` | push |
| Due soon row "Remind" (a friend owes you) | `remind(personId, context)` | sheet |
| Due soon row "Settle" (you owe, e.g. Goa Trip) | `recordPayment(prefill from the settle plan: to the simplified payee, amount, context)` | modal |
| Due soon row body | friend → `friend(id)`; group → `group(id)` | push |
| See all | `select(.activity)` + Timeline segment | tab switch |
| Recent activity row | expense → `expense(id)`; payment → `payment(id)`; loan → `loan(id)` | push |
| Confirm (card) | `store.confirmPayment(id)` in place + toast "Payment confirmed" | in place |
| Not received (card) | `notReceived(paymentId)` | sheet |
| First day "Add expense" | `addExpense(new)` | modal |
| First day "Invite friends" | `addFriend` | push |

### 2.4 Navigation map: every edge by screen
Presentation keys: **P** push · **M** modal · **S** route sheet · **L** local (sheet/page/alert inside the owner's files) · **T** tab switch · **A** action in place (toast where named). Back/✕ edges are in §2.5.

| From | Tap | → Destination (params) | Pres. |
|---|---|---|---|
| Groups tab (Groups segment) | header ＋ | `newGroup(.group)` | M |
| | group row | `group(id)` (project rows → `project(id)`; archived → `project(id)` read-only) | P |
| | empty: New group / Invite friends | `newGroup(.group)` / `addFriend` | M / P |
| Groups tab (Friends segment) | header user-add | `addFriend` | P |
| | friend row | `friend(id)` | P |
| Group detail | gear | `groupSettings(id)` | P |
| | Settle up (card) | one transfer in the group plan → `recordPayment(prefill)`; several → `settleUp(groupId)` | M / P |
| | expense row | `expense(id)` | P |
| | Add expense (empty state) | `addExpense(draft: group + members)` | M |
| | Recurring (from settings) | `requirePro(.recurring(groupId))` | P / M |
| Group settings | Members Add | `pickPeople(multi)` → result adds members | P |
| | Currency | `pickCurrency` | S |
| | Recurring expenses | `requirePro(.recurring(groupId))` | P |
| | Leave group | balance ≠ 0 → alert "You can’t leave yet" (Settle up → `recordPayment(prefill)`); balance 0 → confirm alert (proposal) → leave, pop to Groups | L |
| Friend page | Remind | `remind(id, context)` | S |
| | Record payment | `recordPayment(from: friend, to: me, amount: net)`; if you owe: Settle up → `recordPayment(from: me, to: friend)` | M |
| | history row | `expense` / `payment` / `loan` | P |
| | group row (Groups together) | `group(id)` / `project(id)` | P |
| | guest: Send invite / Add expense | share sheet (invite link) / `addExpense(draft: people [guest])` | system / M |
| | Automatic reminders toggle | `store.setRemindersMuted(id, !on)` | A |
| Add friend | Invite with a link | share sheet (`paybak.app/i/<username>`) | system |
| | Scan QR code | QR scanner (§4) → add friend → `friend(id)` | system → P |
| | My QR code | local sheet (Copy → clipboard + toast "Link copied"; Share link → share sheet) | L |
| | contact "Add" / "Invite" | `store.addFriend(id)` / `store.addGuest(name, contact)` then `friend(id)` | A / P |
| Owed / Owe breakdown | person row | `friend(id)` | P |
| | Settle up | `settleUp(nil)` | P |
| Settle up | Settle (you pay) | `recordPayment(prefill: to, amount, context)` | M |
| | Remind (they owe) | `remind(id, context)` | S |
| | row body | `friend(id)`; a pending payment row → `payment(id)` | P |
| Remind sheet | Send in Paybak | `store.sendReminder(…)`, dismiss, toast "Reminder sent to {first name}" | A |
| | Share… | share sheet with the message (sheet stays; closes silently after a completed share) | system |
| Not received sheet | Send / Cancel | `store.markNotReceived(id, note)` + dismiss / dismiss | A |
| Record payment | From / To tiles | `pickPeople(single)` | P |
| | currency chip | `pickCurrency` | S |
| | For | `pickGroup` (groups + open loans + "None"; proposal) | S |
| | Date | `pickDate(.date)` | S |
| | Proof | system photo picker | system |
| | Copy (UPI preview) | clipboard + toast "UPI ID copied" | A |
| | Save | `store.recordPayment` → `router.didSave(.payment(id), toast: "Payment recorded")` (§2.7) | M→P |
| Payment detail | Edit | `recordPayment(editing: id)` | M |
| | Cancel payment | alert → `store.cancelPayment(id)` → pop | L |
| | Proof thumbnail | `photoViewer` | M |
| Lend money | Lent to | `pickPeople(single)` | P |
| | currency / Date, First due, Pick date / Repeats | `pickCurrency` / `pickDate` / local sheet | S / S / L |
| | Save | `store.addLoan` → `didSave(.loan(id), toast: "Loan added")` | M→P |
| Loan detail | Edit | `lendMoney(editing: id)` | M |
| | Record repayment | `recordPayment(from: borrower, to: me, amount: next installment, context: loan)` | M |
| | Remind {name} | `remind(borrower, context: loan)` | S |
| New group | Add people | `pickPeople(multi)` | P |
| | Currency | `pickCurrency` | S |
| | cover photo | system photo picker | system |
| | Create | `store.addGroup` → `didCreateGroup(id)`: dismiss, `select(.groups)`, push `group(id)` / `project(id)`, toast "Group created" / "Project created" | M→T→P |
| Add expense | Add people (+) / person chips | `pickPeople(multi)` | P |
| | currency chip | `pickCurrency` | S |
| | Today chip | `pickDate(.date)` | S |
| | Due row / Pick date | `pickDate(.dueDate)` | S |
| | Paid by, Category, Notes | local sheets; Multiple people → local payer page | L |
| | Split row | local split-editor page | L |
| | Group row | `pickGroup` | S |
| | Repeat row | `requirePro(.repeatRule(request))` (free → `paywall(continueTo: nil)`, Done returns to the form) | S / M |
| | Add receipt | free: system photo picker (attach only) — Pro: `scanReceipt(request)` | system / M |
| | Save | `store.addExpense` → `didSave(.expense(id), toast: "Expense added")`; edit mode → dismiss back to the detail | M→P |
| Expense detail | Edit | `addExpense(editing: id)` | M |
| | group chip / "Your {group} balance" row | `group(id)` | P |
| | receipt | `photoViewer` | M |
| | Flag an issue | local sheet (note) → `store.flagExpense` | L |
| | Resolve / Edit expense (disputed notice) | `store.resolveFlag` / `addExpense(editing:)` | A / M |
| | Delete expense | alert → `store.deleteExpense` → pop, toast "Expense deleted" | L |
| | composer send | `store.addComment` | A |
| Activity (Timeline) | Restore (header) | `recentlyDeleted` | P |
| | Insights segment | segment switch (router state) | A |
| | confirm card | `store.confirmPayment` / `notReceived(id)` | A / S |
| | expense / payment / loan / reminder rows | `expense` / `payment` / `loan` / the item it's about | P |
| | recurring-draft row | `recurring(groupId)` | P |
| Recently deleted | Restore | `store.restoreExpense(id)` + toast "Expense restored" | A |
| Notifications | Mark all read | `store.markAllInboxRead()` | A |
| | confirm card | as Activity | A / S |
| | Payment reminder row | `recordPayment(prefill)` | M |
| | Monthly summary row | `select(.activity)` + Insights segment, month | T |
| | Payment overdue row | `remind(id, context)` | S |
| | New expense / comment / flag rows | `expense(id)` | P |
| | Payment confirmed row | `payment(id)` | P |
| Insights | Restore | `recentlyDeleted` | P |
| | month ‹ › | month state | A |
| | category / group / friend bar row | `activityLog(filter)` | P |
| | loan row | `loan(id)` | P |
| | locked: See Pro | `paywall(continueTo: nil)`; entitlement flips, Insights unlocks in place | M |
| Ask Paybak | prompt / composer send | assistant answer (§4.3) | A |
| | Remind {name} chip | `remind(id)` over the chat | S |
| | See Insights / Open {group} / Settle up chips | `select(.activity)`+Insights / `group(id)` / `settleUp(groupId)` (dismiss the chat first) | T / P |
| | Draft Save / Edit / View | `store.addExpense` in place / `addExpense(draft:)` over the chat / dismiss chat + `expense(id)` | A / M / P |
| | Mic | speech input (§4) | system |
| Scan camera | shutter / Upload photo | read the image (§4) → local Review page | L |
| | Review "Looks right" → Assign → Continue | `router.complete(request, .receipt(result))`: dismiss scan modal, the Add expense form applies the draft | M→M |
| Recurring (group) | draft "Enter amount" | `enterDraftAmount(draftId)` | M |
| | rule row | `addExpense(editing template)` (proposal: edits the rule) | M |
| Profile | avatar / Edit avatar | `editAvatar` | P |
| | Paybak Pro row | free → `paywall(nil)`; Pro → `paywall(nil)` in its member/status form | M |
| | Payment details, Currency, Notifications, Privacy, Help rows | `paymentDetails`, `settingsCurrency`, `settingsNotifications`, `privacyData`, `helpFeedback` | P |
| | Sign out | alert → clear session → onboarding at Get Started | L |
| Edit avatar | Back (dirty) / Save | Discard alert / `store`… profile update, pop | L / A |
| Payment details | Add payment method | local sheet (UPI / Bank) | L |
| | method row | local action sheet (Make primary / Copy / Remove) | L |
| Currency settings | default currency card | `pickCurrency(title: "Default currency")` | S |
| Notification settings | Muted friends | `mutedFriends` | P |
| Privacy & data | Export records | `requirePro(.privacyExport)` | P / M |
| | Recently deleted | `recentlyDeleted` | P |
| | Delete account | open balances → alert "Settle up first" (Settle up → `settleUp(nil)` on the current stack); else confirm alert → wipe → Welcome | L |
| Export | Export | build file → share sheet | system |
| Help | FAQ row / Contact us / Rate Paybak | `helpAnswer(i)` / mail composer / review prompt | P / system |
| Project detail | gear | `projectSettings(id)` | P |
| | Add component / component row | local sheet (add / edit) | L |
| | History | `activityLog(.project(id))` | P |
| | transfer row involving you | you pay → `recordPayment(prefill)`; they owe you → `remind(id)` | M / S |
| Project settings | Add member | `pickPeople(multi)` | P |
| | Close project | alert → `store.closeProject(id)` → pop to the project (Closed) | L |

### 2.5 Back and dismiss behaviour
- Pushed screens: glass back chevron = `router.back()` = pop the top layer's stack; iOS edge swipe stays enabled (reuse `navigationBarHiddenKeepingSwipeBack()`); a screen with unsaved changes disables the swipe (`navigationBarBackButtonHidden` pattern) and shows its alert on back.
- Modals: ✕ = `router.dismissModal()` (the top layer). Forms with changes ask first (Discard alert); iOS covers can't be swiped down, and Android system back on a form = ✕. Paywall: ✕ = dismiss; Welcome's Done = `router.finishPaywall()` (dismiss, then open `continueTo` if any).
- Sheets: scrim, ✕, swipe down on the grabber, Android back = dismiss (`router.dismissSheet()`); a sheet with a focused field dismisses the keyboard first.
- Alerts: never dismissed by the scrim; Android back = the cancel action.
- **Android system back** order: local alert → local sheet/page (the screen's own `BackHandler`) → route sheet → push in the top layer → top modal (through its ✕ logic) → main push → on a tab root other than Home: `select(.home)` → Home: leave the app (Home is the task root, flow.md).
- "Back" targets in Figma that point at Home — Active mean "pop to where you came from" (all specs agree). Never pop into onboarding from main.

### 2.6 Deep links (notifications and internal links)
Every notification carries one internal link string (iOS `userInfo["link"]`, Android intent extra `link`); no URL scheme or intent filter is registered. `DeepLink.parse(_:)` (M2) → `router.open(link)`; when the app is on onboarding, links are ignored.

| Link | Opens | From |
|---|---|---|
| `paybak://activity?claim=<paymentId>` | `select(.activity)`, Timeline (claim card on top) | tap on "Payment to confirm" (lockConfirmRequest) |
| `paybak://activity?claim=<id>&action=notReceived` | as above + `notReceived(id)` sheet | "Not received" notification action (foreground) |
| (background action) `CONFIRM` | `store.confirmPayment(id)` without opening the UI; removes the notification | "Confirm" action |
| `paybak://record-payment?to=<personId>&amount=<minor>&context=<group:id>` | `select(.home)` + `recordPayment(prefill)` | "Payment reminder" (lockReminder), inbox row |
| `paybak://insights?month=2026-09` | `select(.activity)`, Insights, that month | monthly summary |
| `paybak://remind?person=<id>` | `select(.home)` + `remind(id)` | overdue alert |
| `paybak://expense/<id>` | `select(.activity)` + push `expense(id)` | new expense / comment / flag |
| `paybak://recurring-draft/<draftId>` | `enterDraftAmount(id)` | variable-rule draft reminder |
| `paybak://payment/<id>` | `select(.activity)` + push `payment(id)` | payment confirmed / not received (domain.md §6.8) |

### 2.7 Router helpers (M2; both platforms, same names)
- `open(_ route)` — decides by `route.presentation` (§2.1).
- `back()`, `dismissModal()`, `dismissSheet()`, `select(_ tab)`, `popToRoot()`.
- `replaceSheet(with route)` — dismiss the current sheet, then open `route` (Add sheet rows).
- `didSave(_ detail: Route, toast: String)` — the modal's Save: push `detail` without animation on the layer **below** the modal, then dismiss the modal (so the modal slides away revealing the detail), then show the toast. Used by Add expense, Record payment, Lend money. Edit-mode saves just dismiss.
- `didCreateGroup(_ id, isProject)` — dismiss all modals, `select(.groups)`, push `group`/`project`, toast.
- `requirePro(_ route, feature)` — Pro → `open(route)`; free → `open(.paywall(continueTo: route))`. The paywall's Done calls `finishPaywall()`.
- `toast(_ text, icon: .checkCircle)` — one app-level toast host above every layer (§7.2).
- Result channel for cross-lane pickers: `PickRequest { id: String, … }` travels in the route; the picker calls `router.complete(requestId, with: RouteResult)` (which also pops/dismisses the picker); the caller receives it with iOS `.onRouteResult(requestId) { result in … }` / Android `RouteResultEffect(requestId) { result -> … }`. `RouteResult` (M2): `.people([PersonID])`, `.person(PersonID)`, `.currency(String)`, `.day(LocalDay?)`, `.group(GroupID?)`, `.repeatRule(RepeatRule?)`, `.receipt(ReceiptResult)`. Results are Codable/serializable so Android can save them.
- `open(_ link: DeepLink)`.

### 2.8 Platform implementation of the router
**iOS** (M2 edits `App/AppRouter.swift`, adds `Navigation/*`):
- `AppRouter.Root` gains `.main`, replacing `.home`; `finishOnboarding()` / `finishSplash` go to `.main` with `selectedTab = .home`. `homeState` and `isAddSheetPresented` go away (Home's state now comes from data; the debug menu loads scenarios instead).
- Main state: `selectedTab: Tab`, `groupsSegment`, `activitySegment`, `insightsMonth`, `mainPath: [Route]`, `mainSheet: Route?`, `modals: [ModalLayer]` (`ModalLayer: Identifiable { id, root: Route, path: [Route], sheet: Route? }`), `results: [String: RouteResult]`, `toast: PBToastMessage?`.
- `MainView` = `NavigationStack(path: $router.mainPath) { TabShell() }.navigationDestination(for: Route.self) { RouteView(route: $0) }`, overlaid by `SheetHost(layer: .main)`; `ModalHost(level: 0)` attaches `.fullScreenCover(item:)` recursively (each cover hosts `NavigationStack(path:)` + `RouteView(root)` + its own `SheetHost` + `ModalHost(level + 1)`).
- `Navigation/RouteView.swift` is the single `switch route` → screen view, grouped in lane sections (§7.1).

**Android** (M2 edits `navigation/*`):
- Keep `Destination` for splash and onboarding; replace `Destination.Home` with `Destination.Main`, whose content is `MainHost(navigator: MainNavigator)`.
- `MainNavigator` (`@Stable`): `entries: SnapshotStateList<NavEntry>` (`NavEntry(key: String, route: Route)`; entry 0 is always `Route.Tabs`), `selectedTab`, `groupsSegment`, `activitySegment`, `insightsMonth`, `results: SnapshotStateMap<String, RouteResult>`, `toast`. Presentation comes from `route.presentation`; layers are implicit (a Modal entry starts a layer; later Push entries belong to it; Sheet entries overlay the entry below).
- `MainHost` renders the last non-sheet entry with `AnimatedContent` (transition from the entry's presentation: push/pop, modal slide up/down, dissolve for `didSave`), keeps the entry below a sheet composed underneath it, and draws sheet entries in `SheetHost` above. `SaveableStateHolder` keeps each entry's and each tab root's state.
- Saved across process death with `rememberSaveable` + kotlinx.serialization JSON of `entries` and the main state.
- `navigation/RouteContent.kt` is the single `when (route)` → screen composable, grouped in lane sections (§7.1).
- `LocalMainNavigator`, `LocalLedger`, `LocalAppClock` CompositionLocals (M2) give screens access without parameter threading.

---

## 3. Data layer

`domain.md` (with `seed/verify.py` as its reference implementation) is the source for entities, field names, algorithms and numbers. This section fixes how the apps hold, expose and persist them.

### 3.1 Overview
```
Domain (pure, no UI, no I/O)          ← unit-tested; a straight port of seed/verify.py / domain.md
  models (Codable / @Serializable), money, days, formatting, calculations, tick jobs, assistant parser, receipt math
Store (LedgerStore / LedgerRepository) ← one observable owner of the Ledger document
  read models: snapshot (recomputed on every change) + parametrised queries
  actions: mutate(…) + named domain actions (M2, domain.md §11) + lane extension files
Persistence: ledger.json (JSON, atomic write) · ProfileStore (existing, UserDefaults / SharedPreferences)
Seed: demo.json + rates.json bundled; DemoSeed materialises the demo for a load day and runs its named scenarios (debug only)
Clock: AppClock (real, or pinned by the debug hooks)
Services (stubs from M2, filled by lanes): notifications, share, haptics, QR, contacts, camera, receipt reading, speech, export
```

**Two stores, one owner each** (domain.md §1.1, §1.14):
- `ProfileStore` (M1, extended by M2): identity and onboarding, as today, plus the domain.md §1.1 fields: `currencyCode` (**the default currency**), `username`, `pronoun`, `paymentMethods` (`upiID` mirrors the primary UPI method; migration: a non-empty `upiID` with no methods becomes the primary method), `showPaymentToFriends`, and the new avatar case `character(AvatarLook)`. demo.json's `profile` block loads into it.
- `LedgerStore` / `LedgerRepository` (M2, new): everything else, including `settings` (with the `entitlement`), `rotation` and `scheduler`.
- "Reset onboarding" and "Delete account" clear both stores; Sign out clears only the session fields of the profile.

### 3.2 The Ledger document (both platforms, one JSON shape)
**`ledger.json` is demo.json without its `anchor`, `profile` and `scenarios` blocks, with absolute dates** (domain.md §7.3). Field names are camelCase and identical on iOS and Android (domain.md §1 is the field list), so one file decodes on both:

```
Ledger {
  schemaVersion: 1
  settings:       { keepBalancesPerCurrency, push {…6}, reminderSchedule {twoDaysBefore, onDueDate, overdueEvery3Days, time "21:00"},
                    discovery {findMeByContact, contactsSync}, entitlement {plan free|pro, period, trialEndsAt, since} }
  people:         [Person  §1.2  {id, name, avatar, upi, username, pronoun, isGuest, contact, remindersMuted, addedAt}]
  groups:         [Group   §1.3  {id, kind, type, icon, name, currency, memberIds, simplifyDebts, settleBy, createdAt, createdBy, project?}]
  expenses:       [Expense §1.4  {…, payers, split {mode, rows [{personId, included, value, share}]}, itemized?, receipt?, history, comments,
                                   flag?, deletedAt?, deletedBy?}]
  payments:       [Payment §1.5  {id, fromId, toId, amount, currency, rate?, method, date, groupId?, loanId?, expenseId?, status, …}]
  loans:          [Loan    §1.6]
  components:     [Component §1.7 {…, estimatedCost?, actualCost?, paidBy, …}]
  recurringRules: [Rule §1.8]      drafts: [Draft §1.8]
  reminders:      [Reminder §1.9]  inbox:  [InboxItem §1.10 {id, type, createdAt, read, params (snapshot)}]
  rotation:       { contextKey: counter }        // fair leftover-paise rotation (§4.1)
  scheduler:      { cursor }                     // tick(now) has run up to here (§10)
  // lane anchors: a lane may append optional fields below its own marker (§7.1)
}
```
- `me` is the user's person id. Demo ids are readable and stable (`p-rohan`, `g-goa`, `e-olive`, `pay-esha-olive`, `l-kabir-bike`, `pj-drone`, `c-drone-gps`, `r-gas`, `d-gas-09`); UI tests address rows by them. New ids are lowercase UUIDs.
- Money is integer minor units (`Int64` / `Long`) plus an ISO code; percent in basis points; rates are decimal strings (`{value: "22.85", to: "INR"}`) handled with `Decimal` / `BigDecimal` (domain.md §0).
- Calendar dates are local days `yyyy-MM-dd`; moments are instants, ISO-8601 UTC in `ledger.json`, shown in the device zone (domain.md §0). The seed's relative local moments (`"D-9T15:30"`) become instants in the device zone at load.
- **No stored event log**: the Activity timeline, expense History, Recent activity and inbox copy are derived from the records (domain.md §1.11, §6.6).
- Receipts and proofs: `photo` is a JPEG file name in the app's photos folder; `asset` names a bundled image (demo only).

### 3.3 Read models (M2 provides; pure functions in Domain, cached by the store)
Port domain.md §5–§6 / verify.py's read models one to one: names may become idiomatic, results and rounding may not change. `LedgerSnapshot` is recomputed after every change (under ~5 ms for the demo; a unit test measures it):
- `home: HomeSummary` — state (firstDay / active / allSettled), owed total + people count, owe total + caption ("across 2 groups"), `dueSoon` (domain.md §12 #4 rule), `recent` (top 3), `pendingClaims` (payments to you with status pending, newest first; §6.7).
- `friends: [FriendBalance]` — one net per friend (§5.5), order, overdue, subtitle facts.
- `groups: [GroupSummary]` — your net, open/settled, project budget facts, archived flag, list order (§6.5).
- `owedBreakdown`, `oweBreakdown`, `settlePlan` (§6.2–6.3).
- `timeline: [TimelineDay]` (§6.6), `inbox` + `unreadCount` (§6.8), `recentlyDeleted` (+ days left), `openBalances (owe, owed)`, `recentCurrencies`.

Parametrised queries: `groupSheet(id)` (§5.2–5.4), `friendPage(id)`, `expenseDetail(id)`, `paymentDetail(id)`, `loanDetail(id)` (§9), `projectReport(id)` (§8), `insights(month)` (§6.4), `recurring(groupId)`, `reminderDates(for:)` (§10), `exportRecords(range, groups)`, `inboxText(item)` (§6.8), `assistantAnswer(prompt)` building blocks (§6.9).
Presentation that isn't a business rule (row view models, copy for undesigned UI, layout) belongs to the lane that draws it, in its own files.

### 3.4 Actions (M2 provides every action in domain.md §11)
Names and side effects are domain.md §11's; every action validates, takes `at = clock.now()` (scenario steps pass theirs), goes through `mutate`, appends history / reminder / inbox entries and persists:
`addExpense`, `updateExpense`, `deleteExpense`, `restoreExpense`, `addComment`, `flagExpense`, `resolveFlag`, `enterDraftAmount(draftId, amount)`, `recordPayment`, `cancelPayment`, `confirmPayment`, `markNotReceived`, `addLoan`, `addGroup` (group or project), `updateGroup`, `addMembers`, `removeMember`, `leaveGroup` (throws when the net ≠ 0), `addComponent`, `updateComponent`, `closeProject`, `sendReminder`, `setRemindersMuted`, `markInboxRead`, `markAllInboxRead`, `addRecurringRule`, `updateRecurringRule`, `deleteRecurringRule`, `startTrial`, `subscribe`, `restorePurchases`, `setPro`, `updateSettings`, `tick(now)`, `clear()`, `loadDemo(anchor, scenarios)` (debug), plus `addFriend(person)` and `addGuest(name, contact)` for Add friend and Split with (domain.md §1.2). Profile-side actions (payment methods, default currency, avatar, username) are `ProfileStore` methods.
- `tick(now)` (domain.md §10) runs at launch, on foreground, after the clock changes and from the debug menu: recurring 09:00, overdue alerts 09:00, monthly summary 20:00 on the last day, reminders at `reminderSchedule.time`, then the 30-day purge and the archive of settled closed projects; idempotent.
- Seed scenario steps name some actions differently; the scenario runner maps them: `createGroup` → `addGroup`, `setEntitlement` → the entitlement update (`startTrial`/`subscribe`/`setPro`), `clearLedger` → `clear()`, `setClock` → pin `AppClock` and `tick`. The others (`recordPayment`, `confirmPayment`, `markNotReceived`, `flagExpense`, `addLoan`, `updateComponent`, `closeProject`) are the store actions of the same name.
- Lane-only actions (avatar save, export) live in the lane's own extension file.

### 3.5 iOS
- `Domain/` (M2): value types, `nonisolated` + `Sendable` + `Codable` (the target defaults to MainActor isolation; domain types opt out so they can be encoded off the main actor). Files: `Ids.swift`, `Money.swift`, `LocalDay.swift` (encodes as `yyyy-MM-dd`; moments are `Date`, encoded ISO-8601 UTC), `AppClock.swift`, `Formatting.swift` (a port of verify.py §2–§3: money with Indian grouping, `+`/U+2212 signs, en-GB `EEE d MMM`, `d MMM`, `h:mm a` lowercase, relative Today/Yesterday, ranges), `Models/*.swift`, `Calc/*.swift` (Balances, Simplify, Splits + rotation, DueDates + reminder schedule, Installments, Projects, Insights, Recurring, ReceiptMath, Rates), `Snapshot.swift`.
- `Data/LedgerStore.swift` (M2 core):
```swift
@Observable final class LedgerStore {
    private(set) var ledger: Ledger
    private(set) var snapshot: LedgerSnapshot
    private(set) var revision = 0            // observers (notification scheduler) react to changes
    let clock: AppClock
    init(file: LedgerFile = .standard, clock: AppClock = .system)
    /// The only write path: applies `change`, recomputes the snapshot, saves, bumps `revision`.
    func mutate(_ change: (inout Ledger) throws -> Void) rethrows
}
```
  Shared actions in `Data/LedgerStore+Actions.swift`, shared queries in `Data/LedgerStore+Queries.swift` (M2). Lanes add `Data/Lanes/LedgerStore+<Module>.swift`. Injected with `.environment(ledgerStore)` next to `ProfileStore` and `AppRouter` in `PaybakApp`.
- `Data/LedgerFile.swift`: `Application Support/ledger.json`, `JSONEncoder` (`.sortedKeys`, `.iso8601` dates for moments; `LocalDay` encodes itself as `yyyy-MM-dd`), written with `.atomic` from a serial background task (the store encodes on the main actor, hands `Data` over). Decode failure → move the file to `ledger.corrupt-<timestamp>.json`, log, start empty.
- Resources: `Resources/Seed/demo.json` (copied from `docs/design/seed/demo.json`), `Resources/Rates/rates.json` (copied from `docs/design/seed/rates.json`: INR per unit, "today's rate"). The loader `Debug/DemoSeed.swift` is `#if DEBUG`.
- Unit tests: `paybakTests/Domain/*Tests.swift` (Swift Testing, already the target's framework). They load the bundled `demo.json` through the app host and assert **every number `seed/verify.py` asserts**, plus formatting vectors and the rotation/rounding cases.

### 3.6 Android
- `domain/` (M2): pure Kotlin (no `android.*` imports), `@Serializable` data classes, `java.time` (**enable core library desugaring**: minSdk 24 lacks `java.time`; `coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")`, `isCoreLibraryDesugaringEnabled = true`). Days are `LocalDate` (`yyyy-MM-dd`), moments `Instant` (ISO-8601 UTC), each with a small custom serializer; only `DemoSeed` parses the seed's relative strings. Same file split as iOS: `domain/model`, `domain/calc`, `domain/format`, `domain/Snapshot.kt`.
- `data/ledger/LedgerRepository.kt` (M2 core):
```kotlin
class LedgerRepository(private val file: LedgerFile, val clock: AppClock, private val scope: CoroutineScope) {
    val ledger: StateFlow<Ledger>
    val snapshot: StateFlow<LedgerSnapshot>
    val revision: StateFlow<Int>
    /** The only write path: applies [change] on the main thread, recomputes, saves on IO. */
    fun mutate(change: (Ledger) -> Ledger)
}
```
  Shared actions as extension functions in `data/ledger/actions/*.kt`, shared queries in `data/ledger/Queries.kt` (M2); lanes add `data/ledger/lanes/<Module>Actions.kt`. Created lazily in `PaybakApplication` next to `profileStore`; provided to Compose with `LocalLedger`.
- `data/ledger/LedgerFile.kt`: `AtomicFile(filesDir/ledger.json)`, writes serialised on `Dispatchers.IO` through a conflated channel (last write wins, in order). Add `ledger.json` to `backup_rules.xml` / `data_extraction_rules.xml`.
- **JSON library: kotlinx.serialization** (`org.jetbrains.kotlin.plugin.serialization` at the Kotlin version + `org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0`). Why: about 15 nested entity types with sealed variants (avatar kinds, split modes, contexts, route params) would need hand-written, untested mappers with org.json; kotlinx.serialization generates them at compile time (no reflection, R8-safe), handles defaults for schema growth (`ignoreUnknownKeys`, default values), produces the same JSON shape as iOS Codable, and also serialises the `Route` back stack for process death.
- Seed: `src/debug/assets/seed/demo.json` (debug only) + `src/main/assets/rates.json` (copied from `docs/design/seed/rates.json`). JVM unit tests read the seed via `sourceSets["test"].resources.srcDir("src/debug/assets")`.
- Unit tests: `src/test/java/app/paybak/paybak/domain/*Test.kt` (JUnit 4), the same assertions and vectors as iOS.

### 3.7 Persistence rules
- Every action persists immediately (write-through, like the profile). Nothing is lost on process death.
- `schemaVersion` starts at 1; decoders ignore unknown keys and default missing ones, so lanes can add optional fields safely. A breaking change bumps the version with a migration function in `LedgerFile`.
- Photos (receipts, proofs, covers) are JPEGs in `Application Support/photos/` / `filesDir/photos/`, referenced by file name; deleting the owning record deletes the photo.
- Exports go to the cache directory and are shared through the share sheet (Android `FileProvider`, authority `app.paybak.paybak.files`).

### 3.8 Demo seed and the clock
- `seed/demo.json` and `seed/rates.json` are bundled unchanged. Relative values (domain.md §7.1): `D±n` = the **load day** ± n days, `D±nTHH:MM` = that day at that local time; seeded moments later than now clamp to now. `DemoSeed` resolves them (port verify.py `resolve`), loads `profile` into `ProfileStore` and the rest into the ledger (`scheduler.cursor` = the load day at 00:00), then runs `tick(now)`, which generates today's 20:00 summary and 21:00 reminders.
- **Figma parity** (every debug start screen; verify.py `FIGMA_NOW`): load day **Wed 30 Sep 2026**, clock **pinned to 21:15** local. Every date label, the "Good evening" greeting, the 9:00 pm reminder and the 8:00 pm summary then match Figma.
- **Scenarios** are `demo.json` → `scenarios` (domain.md §7.4): named lists of steps, each `{action, at, …}` or `{use: otherScenario}`. The runner (port verify.py `apply_scenario`) runs `tick(at)` before each step, then the store action (names mapped as in §3.4); `setClock` pins the clock. The debug start screens and the debug menu use them by name (`empty`, `eshaClaimsPayment`, `eshaPaymentConfirmed`, `eshaPaymentNotReceived`, `paymentToMeeraPending`, `paymentToMeeraConfirmed`, `paymentToKabirPending`, `eshaFlagsSeafood`, `lendDev`, `lendDevOverdue`, `devBuysGps`, `closeDrone`, `weekendTrek`, `allSettled`, `pro`).
- **Debug menu "Load demo data"** = base + `eshaClaimsPayment` (the `D` of §1), offered *at the Figma date* (above) or *around today* (load day = today, real clock: the same story on today's dates).
- `AppClock` (M2): `now()` (an instant), `today()` (the local day), the device zone and calendar. Real by default; pinned by `-now <yyyy-MM-ddTHH:mm>` / `--es now …`, by scenarios (`setClock`) or from the debug menu. Every time-dependent computation (greeting, Today/Yesterday, due/overdue, reminders, recurring, trial end, `tick`) takes the clock; nothing calls `Date()` / `System.currentTimeMillis()` / `LocalDateTime.now()` directly.
- A pinned clock persists in debug builds (UserDefaults / debug SharedPreferences) until "Use real time", so relaunching keeps the state; UI tests pass it on every launch.

### 3.9 Tests the data layer must pass (M2 acceptance)
- **Port every check of `seed/verify.py`** (135 on 1 Oct 02:34) as unit tests on both platforms, one test (or test file) per `check_*` function: `check_formatting`, `check_splits`, `check_home_and_friends` (incl. the Ask "Who owes me money?" text), `check_groups`, `check_projects`, `check_insights`, `check_loans`, `check_activity_and_inbox`, `check_payments`, `check_recurring_and_misc`. They load the bundled `demo.json` with verify.py's `load(*scenarios, now:, anchor:)` semantics.
- Round trip: demo → JSON → decode = equal; iOS and Android encode the same ledger to the same keys (a shared fixture file checked by both suites).
- `tick` idempotence; rotation counter behaviour; each action's side effects (history, inbox, reminders).
- Snapshot recompute time for the demo under ~5 ms (release-like build; skip the assertion on slow CI).

### 3.10 Debug hooks (M2 extends flow.md's hooks; debug builds only)
**Launch arguments** (iOS `-key value`; Android `--es/--ez key value`):
| Key | Effect |
|---|---|
| `startScreen <id>` | any id from §1: applies its scenario (profile + ledger seed + clock + Pro + route stack + route sheets/modals) and exposes the id to the owning screen for its `UI:` state |
| `resetOnboarding YES` | clears profile and ledger (existing key; now also the ledger) |
| `now <yyyy-MM-ddTHH:mm>` | pins the clock (local time) |
| `pro YES/NO` | overrides the entitlement after the scenario |
| `demoData YES` | loads the demo at Figma parity without changing the start screen |
| `scenario <name>[,<name>…]` | applies seed scenarios after the demo loads (UI tests combine them freely) |
| `link <paybak://…>` | opens a deep link after launch, exactly as a notification tap would (§2.6); UI tests use it for notification routing |
| `startScreen gallery` (+ `galleryPage`) | existing component gallery |

**Scenario table** (M2 writes all of it): `Debug/Scenarios/Scenarios+<Module>.swift` / `src/debug/…/debug/scenarios/<Module>Scenarios.kt`, one file per module (`Home`, `AddRecord`, `Groups`, `Settle`, `Activity`, `Projects`, `Profile`, `Settings`, `Insights`), each returning `[ScreenID: Scenario]`. `Scenario { seedScenarios: [String], clock?, tab, stack: [Route], sheet?: Route, modals: [Route], avatar?: AvatarLook }`; the Seed column of §1 is its data: `E` = `["empty"]` (demo profile kept), `D` = `["eshaClaimsPayment"]`, `D−claim` = `[]`, `D+x` = `["eshaClaimsPayment", "x"]`, `D−claim+x` = `["x"]`, `P` appends `"pro"`; all at Figma parity. The owning screen reads the start id from the environment (iOS `@Environment(\.debugStartScreen)`, Android `LocalDebugStartScreen`; nil in release and after first use) to apply its `UI:` state once. `ScreenID` lists every id of §1 (M2, `App/ScreenID.swift`; Android `debug/ScreenIds.kt` plus the existing `Destination` ids).

**Debug menu** (long-press the Home logo; M2 builds the sheet and the core sections; each module has its own section file `Debug/Menu/<Module>DebugActions.swift` / `debug/menu/<Module>DebugActions.kt` returning `[DebugAction]`):
- *Data* (M2): Load demo data at the Figma date · Load demo data around today · Start an empty account · Reset onboarding · Apply scenario… (every seed scenario by name) · Load screen… (every screen id).
- *Clock* (M2): Pin to 30 Sep 2026 21:15 · Use real time · +1 day · Run `tick` now.
- *Pro* (M2): Toggle Pro (shows the current plan) · Clear the entitlement.
- *Friend's side* (M2; each item runs the same store action a real sync would, "as" the friend): Esha says she paid ₹700 (the `eshaClaimsPayment` step at now; also posts the "Payment to confirm" notification once lane A's service exists) · {payee} confirms my latest pending payment · {payee} says Not received · Esha flags Seafood dinner (`eshaFlagsSeafood`) · Esha removes her flag · Priya comments on Villa · Meera adds an expense in Flat 302.
- *Notifications* (lane A): Fire the Kabir reminder now · Post the monthly summary now · Post Rohan's overdue alert · Deliver the next notification in 5 s (to lock the device first).
- *Projects* (lane B): Dev buys the GPS module (`devBuysGps`) · Close Build a Drone (`closeDrone`) · Rohan pays Dev ₹8,500 · Priya pays Dev ₹4,000 · Confirm pending project payments. *Friends* (lane B): Simulate a QR scan of paybak.app/i/meera.
- *Scan* (lane C): Simulated camera on/off (default on for the simulator and emulator).
- *Tools*: Component gallery.

---

## 4. Real vs simulated, per feature

Rule (flow.md): real platform capability where it's cheap and reliable; otherwise a clean seam with a simulation behind it. **iOS adds no package dependencies** (everything below is in the SDK). **Android adds exactly these** (M2 adds them all to `libs.versions.toml` / `build.gradle.kts`; lanes never touch Gradle files):

| Android dependency | Why this one |
|---|---|
| `org.jetbrains.kotlinx:kotlinx-serialization-json` + the Kotlin serialization plugin | ledger/seed JSON and route state (§3.6) |
| `com.android.tools:desugar_jdk_libs` (core library desugaring) | `java.time` on minSdk 24 for all date logic |
| `com.google.zxing:core` 3.5.3 | QR generation (ECC H) in pure Java, ~500 kB; Android has no QR encoder (already added by the components pass for `PbQrCodeCard`) |
| `androidx.camera:camera-camera2`, `camera-lifecycle`, `camera-view` (one version) | the designed in-app receipt camera (preview + capture); the standard, supported camera API |
| `com.google.mlkit:text-recognition` (bundled Latin model) | on-device receipt reading that works offline and deterministically on the emulator; unbundled/Play-services variants download the model on first use |
| `com.google.android.gms:play-services-code-scanner` | QR scanning with Google's scanner UI and no camera permission; avoids building a second camera screen |

| Feature | Real / simulated | iOS API | Android API | Owner |
|---|---|---|---|---|
| Photo picker (avatar, receipt attach, proof, cover) | real | PhotosUI `PhotosPicker` | `ActivityResultContracts.PickVisualMedia` (androidx.activity, present) | M1 done; lanes reuse |
| Receipt camera (scanCamera) | real on devices; **simulated feed** (the bundled Leopold receipt art) on the simulator/emulator or when the debug toggle is on | AVFoundation `AVCaptureSession` + `AVCapturePhotoOutput` in a `UIViewRepresentable`; flash via `AVCaptureDevice.torchMode` | CameraX `Preview` + `ImageCapture`; `CAMERA` permission | C |
| Receipt reading | **real OCR** + deterministic parser; failure → "We couldn’t read this receipt." (photo stays attached) | Vision `VNRecognizeTextRequest` (.accurate, en) | ML Kit Text Recognition (bundled) | C (parser math in Domain, M2 port of the scan math) |
| QR generate | real | CoreImage `CIFilter.qrCodeGenerator()` correction "H", app mark composited in the centre | ZXing `QRCodeWriter` → `Bitmap`, same composition | B |
| QR scan | real where supported; else toast "Scanning isn’t available on this device"; debug action simulates a scan | VisionKit `DataScannerViewController` (`.barcode(symbologies: [.qr])`) when `isSupported && isAvailable` | GMS code scanner `GmsBarcodeScanning` | B |
| Directory ("contacts on Paybak", invite, usernames) | **simulated**: people in the ledger with `onPaybak`/`isFriend` flags from the seed; contact matching by phone/email | Contacts `CNContactStore` (permission on "Contacts sync") | `READ_CONTACTS` + `ContactsContract` | B (Add friend), C (Privacy toggle) |
| Share sheet (remind, invite link, QR link, export) | real | `ShareLink` / `UIActivityViewController` via M2 `SystemShare` | `Intent.createChooser(ACTION_SEND)`, files via `FileProvider` | M2 helper; lanes call it |
| Clipboard (UPI ID, links) | real | `UIPasteboard.general` | `ClipboardManager` | lanes |
| Local notifications | **real**: scheduled reminders (21:00, the reminder schedule), claim notifications with Confirm / Not received actions, overdue, monthly summary, recurring drafts; posted locally when the debug menu simulates the friend | UserNotifications: category `PAYMENT_CONFIRM` (actions `CONFIRM` background, `NOT_RECEIVED` foreground), `UNCalendarNotificationTrigger`, identifiers per item; `UNUserNotificationCenterDelegate` on an `AppDelegate` (M2 adds `@UIApplicationDelegateAdaptor`); keep ≤ 60 pending, nearest first | channels "Payments to confirm" (high), "Reminders", "Summaries", "Activity"; `AlarmManager.setAndAllowWhileIdle` (inexact) + `BroadcastReceiver`s for alarms and actions, `BOOT_COMPLETED` reschedule; permission from Setup 4 | A (service), M2 (stubs, manifest, delegate) |
| CSV / PDF export | real | CSV built in Domain; PDF `UIGraphicsPDFRenderer` (A4) | CSV built in domain; PDF `android.graphics.pdf.PdfDocument` | C |
| Haptics | real | `.sensoryFeedback` / `UIImpactFeedbackGenerator` via M2 `Haptics` | `View.performHapticFeedback` (`CONFIRM`/`REJECT`/`CLOCK_TICK`, fallbacks) via M2 `Haptics` | M2 helper |
| Pro purchase | **simulated**: mock trial/subscribe flips the local entitlement; Restore re-applies a saved one; no store UI is imitated | – | – | C (paywall), M2 (`settings.entitlement`, `startTrial` / `subscribe` / `restorePurchases` / `setPro`) |
| Rate Paybak | real where available | StoreKit `requestReview` | Play Store listing intent (`market://details?id=app.paybak.paybak`, web fallback); no Play Review library | C |
| Contact us | real | `mailto:` (subject "Paybak feedback", version in the body) | `ACTION_SENDTO mailto:` | C (address constant `SupportContact.email`, **to confirm**, §8) |
| Ask Paybak mic | real dictation into the field | Speech `SFSpeechRecognizer` (on-device when supported) + AVAudioEngine; mic + speech permissions | `RecognizerIntent.ACTION_RECOGNIZE_SPEECH` | C |
| Ask Paybak answers | **simulated assistant, real data** (§4.3) | Domain `Assistant` | domain `Assistant` | C |
| Friend's side (confirm, not received, claims, flags, comments, their expenses) | **simulated** from the debug menu (§3.10); every action is the same store action a real sync would call | – | – | M2 + lanes |
| Money movement | never (Paybak only records) | – | – | – |

**Permissions and manifest entries (M2 declares all of them up front; lanes only fill the stub classes):**
- iOS (`INFOPLIST_KEY_*` build settings in the pbxproj, the only pbxproj edit in M2): `NSCameraUsageDescription` ("Paybak uses the camera to scan receipts and QR codes."), `NSContactsUsageDescription` ("Paybak uses your contacts to find friends already on Paybak."), `NSMicrophoneUsageDescription` and `NSSpeechRecognitionUsageDescription` ("…to dictate questions to Ask Paybak.").
- Android manifest: permissions `CAMERA`, `READ_CONTACTS`, `RECEIVE_BOOT_COMPLETED` (no `RECORD_AUDIO`: `RecognizerIntent` records in the system's own UI); receivers `service.NotificationActionReceiver`, `service.ReminderAlarmReceiver`, `service.BootReceiver`; `androidx.core.content.FileProvider` with `res/xml/file_paths.xml` (cache/exports); `<queries>` for `ACTION_SENDTO mailto` and `RecognizerIntent`.

### 4.1 Receipt reading decision (per platform)
Both platforms read for real (Vision / ML Kit) and share one parser: text lines (top to bottom, with x positions) → `ReceiptScan { merchant, date, time, items [label, amount], subtotal, taxes [label, rate, amount], tip, total }` following insights §4.6 (merchant = first prominent line; lines named Subtotal / GST x% / CGST / SGST / Service charge / Tip x% / Total; amounts are the right-most money token). The parser is pure Domain code, unit-tested with the Leopold Cafe line list. Because the simulator and emulator have no useful camera, the camera screen shows the bundled receipt image as its feed and the shutter captures that image, so the real OCR path runs end to end in tests. Free plan: the photo is attached, nothing is read.

### 4.2 Receipt allocation, itemized split
`ReceiptMath` (Domain, M2 port): even split of shared items with fair leftover rotation, tax and tip proportional to each person's items, largest-remainder rounding to the paisa so the parts add up to the total (860/540/600 × 1.15 → ₹989 / ₹621 / ₹690). The result is an `ExpenseDraft` with `split.mode = itemized` and per-person exact shares.

### 4.3 Ask Paybak: the on-device assistant (lane C; Domain `Assistant`, pure and unit-tested; answers per domain.md §6.9)
- **Input normalisation**: lower-case, trim, collapse spaces, strip a trailing "?" / ".", map curly quotes; names match friends by case-insensitive first name.
- **Supported intents** (checked in this order; the four suggested prompts are exact strings from insights §3.6.1 and always hit their intent):
  1. *Who owes me* — "who owes me (money)?", "who owes me", "what am i owed", "how much am i owed". Answer = insights §3.6.2 template from `snapshot.home` + `snapshot.friends` (the same numbers as Home: "4 people owe you ₹2,900: …"), card = Row / Person Compact per person, chips "Remind {name}" for each overdue person (max 2) → `remind`.
  2. *Spend on a category this month* — "how much did i spend on {category} (this month|in {month})". Answer from `insights(month)` ("You spent ₹3,850 on food in September — 17% of your ₹23,300 share."), chip "See Insights".
  3. *When is {group} due* — "when is {group} due", "when do i pay {group}". Answer from the settle plan / due dates ("Your Goa Trip share of ₹1,400 is due Fri 2 Oct."), chips "Settle up", "Open {group}".
  4. *Draft a reminder* — "draft a reminder for {name}", "remind {name}". Answer = the Remind sheet's Friendly template for that debt, chip "Remind {name}" (prefilled).
  5. *Draft an expense* — phrase grammar (insights §3.6.6): `[add|log|split]? {money} (for (a|an|the)? {what})? (, )?(split )?(with {names})? (in {group})?` where `{money}` = `₹600 | 600 | rs 600 | 600 rupees` (decimals allowed), `{names}` separated by `,` / `and` / `&`. Title = `{what}` capitalised; category by the keyword table in insights §3.6.6 (cab → Travel, …, else Other) with **groceries → Food** (domain.md §12 #9); paid by you, today, split equally with you + the matched people (fair rotation; "About ₹x each" when uneven); unknown names → "I couldn’t find {name}." and they're left out. Reply "Here’s what I’ll add. Nothing is saved until you tap Save." + Chat / Draft Expense card (Save → `store.addExpense` → Saved state; Edit → `addExpense(draft:)`; View → `expense(id)`).
  6. *Fallback* — "I can answer questions about your balances and due dates, or add an expense. Try “Who owes me money?”" + the suggestions card again.
- Suggested prompts for other accounts: substitute the soonest-due group and the most overdue debtor, hide a prompt with no data (insights §3.6.1).
- Answers are recomputed from live data at ask time; the chat lives for the session (cleared on relaunch). Nothing leaves the device.

---

## 5. Components

**How the kit was built.** A components pass built **every `components-app.md` set** on `main` before M2 (iOS `DesignSystem/Components/PB{Alert,AmountField,AmountHero,AssignItemRow,AvatarPair,BarRow,BudgetCard,Card,CategoryChip,ChatBubble,CommentRow,Composer,ConfirmPaymentCard,DraftExpenseCard,GroupRow,HistoryRow,InlineField,LoanProgressCard,ModalHeader,MonthlyBarChart,NoticeCard,PaymentParties,PersonRow,PersonTotalsCard,PlanCard,ProgressBar,PushHeader,QRCodeCard,ReceiptLineRow,ReceiptThumbnail,SelectCircle,SettingRow,Sheet,ShutterButton,SignedAmount,SplitRow,SplitTotalBar,TextArea,TitleHeader,TransferRow}.swift` + gallery pages; Android the same set in `ui/components/` + `debug/gallery/{Forms,Lists,Navigation}Page.kt`). **Those files are the kit: don't rebuild them.** They stay flat in `DesignSystem/Components/` / `ui/components/`. M2 started after that pass was committed and added only the missing shared pieces (★ below). "Owner" in §5.2 means the lane that may change a component (in a small, separate commit, §7.1); everyone may use every component.

### 5.1 Shared kit (used by two or more lanes; ★ = M2 builds it, the rest come from the components pass)
Each has a preview (`#Preview` / `@Preview`) and a gallery entry. Lanes use them as is; a lane that needs a variant adds a parameter in a minimal, separate commit (§7.1).

| Component (iOS / Android) | Figma | Spec | Used by |
|---|---|---|---|
| ★ `PBTabBar` + `PBTabItem` / `PbTabBar` | 17:618, 17:504 | components-home §3–5 | shell |
| ★ `PBAddSheet` + `PBSheetRow` / `PbAddSheet` | 17:643, 17:641 | components-home §13–14 | shell |
| `PBSheet` (Sheet / Container: Medium/Large, header on/off, search slot, grabber) / `PbSheet`; ★ the route sheet host | 118:1017 | components-home §15, components-app §6.2, §8.3 | all |
| ★ `PBNavHeader` (Large Title + Inline, collapse on scroll) / `PbNavHeader` | 17:494 | components-home §1, insights §2.4, profile §2.2 | B, C, A (tab roots) |
| ★ `ActivityHeader` (Large title "Activity", Restore button, Timeline/Insights segments) | 167:14363 | activity §3.2–3.3 | A, C |
| `PBPushHeader` (glass back, centred title, Text/Icon/Wide Text/None trailing, white band) / `PbPushHeader` | 97:1082 | components-app §1.3 | all |
| `PBModalHeader` / `PbModalHeader` | 115:886 | components-app §2.1 | A, C |
| `PBAlert` (Android custom dialog; iOS `.pbAlert` over native `.alert` with roles) | 102:1115 | components-app §1.4 | all |
| `PBSettingRow` + settings card group / `PbSettingRow` | 97:996 | components-app §1.2 | A, B, C |
| `PBCategoryChip` / `PbCategoryChip` | 64:4185 | components-app §1.1 | A, B, C |
| `PBPersonRow` (Regular/Compact × trailing types) / `PbPersonRow` | 127:2252 | components-app §3.4 | A, B, C |
| ★ `PBActivityRow` (updated: 2-line titles, 3-line notification bodies, unread dot, detail line, action) / `PbActivityRow` | 13:477 | components-home §10, activity §9 | D, A, C |
| `PBConfirmPaymentCard` (Pending → Confirmed, 250 ms) / `PbConfirmPaymentCard`; ★ `PendingClaimCard` wrapper: Confirm → `store.confirmPayment` + toast, Not received → `notReceived(id)`, collapse after confirm | 129:2060 | components-app §3.9, home-v2 §3.3/§3.9 | D, A, B |
| ★ `PBBalanceCard` + `PBBalanceSummary` (Show caption, badge in the top row, curly ’) / `PbBalanceCard` | 13:269, 13:271 | components-home §6–7, components-app §8.1 | D, B |
| ★ `PBEmptyState` (Card / Empty State with a Rive slot) / `PbEmptyState` | 13:541 | components-home §11 | D, A, B |
| `PBNoticeCard` (Leading/Centered × 0/1/2 actions) / `PbNoticeCard` | 129:1976 | components-app §3.8 | A, B, C |
| `PBAmountHero` / `PbAmountHero` | 128:2004 | components-app §3.7 | A, B |
| `PBAvatarPair` / `PbAvatarPair` | 116:1005 | components-app §3.1 | A, B |
| `PBTitleHeader` / `PbTitleHeader` | 128:1857 | components-app §3.6 | B, A |
| `PBProgressBar` (Small/Large × Default/Projected/Over, mark) / `PbProgressBar` | 116:1099 | components-app §4.1 | A, B, C |
| `PBBarRow` / `PbBarRow` | 143:2156 | components-app §4.2 | B, C |
| `PBAmountField` (Amount Display, system decimal pad) / `PbAmountField` | 125:1084 | components-app §2.4 | A, C |
| `PBTextArea` / `PbTextArea` | 115:9936 | components-app §2.2 | B, A |
| `PBComposer` (Empty/Typing, pinned bar riding the keyboard) / `PbComposer` | 115:907 | components-app §2.3 | A, C |
| `PBReceiptThumbnail` (photo or art) / `PbReceiptThumbnail` | 86:730 | components-app §7.1 | A, C |
| ★ `PBUserAvatar` (the current user in any size/surface: none/preset/photo/character) / `PbUserAvatar` | – | profile §1.7 | all; the `character` rendering calls `AvatarCharacterView` (M2 stub drawing initials; lane C replaces the stub file) |
| ★ Toast host (existing `PBToast` / `PbToast`, lifted to the app root, tab-bar-aware position) | 118:965 | components-app §6.1 | all |
| ★ `Haptics`, `SystemShare` helpers | – | §4 | all |

### 5.2 Lane-owned components (from the components pass unless marked NEW; NEW ones go in `DesignSystem/Components/<Module>/` / `ui/components/<module>/`)
- Lane A: `PBSplitRow` (126:1596), `PBSplitTotalBar` (125:1170), `PBPaymentParties` (125:1085), `PBCommentRow` (116:1007), `PBHistoryRow` (116:1058), `PBLoanProgressCard` (145:2155); NEW: the calendar used by `pickDate` (Android custom calendar per add-expense §10).
- Lane B: `PBGroupRow` (139:2052), `PBTransferRow` (128:1604), `PBQRCodeCard` (130:1912), `PBBudgetCard` (145:2106); NEW: the Remind and Not received sheet bodies.
- Lane C: `PBPlanCard` (125:1198), `PBMonthlyBarChart` (143:2157), `PBChatBubble` (117:971), `PBDraftExpenseCard` (147:2315), `PBReceiptLineRow` (117:993), `PBAssignItemRow` (147:2532), `PBPersonTotalsCard` (147:2533), `PBShutterButton` (117:1001); NEW: `AvatarCharacterView` + `PBAvatarStage` + `PBAvatarPartTile` (profile §5).
- Lane D: NEW `PBAttentionRow` (13:379, detail truncates, components-app §8.2) and the Home header (Nav Header Type=Home).

### 5.3 Assets
- Icons: all 65 Figma icons already exist on both platforms; none are added.
- Rive reuse (no new .riv): First day card → `paybak-homefirstday.riv` (Home first day, Activity empty, Group created); `paybak-getstarted.riv` (Groups empty, spec's 240 × 180 view rect); `paybak-home-allset.riv` (Home all settled); `paybak-allset.riv` (Pro Welcome at 2/3 scale).
- Lane C adds the avatar parts (`assets/avatar-parts`, 84 SVGs → iOS asset catalog folder `AvatarParts/` with preserved vector data; Android VectorDrawables `avatar_part_<gender>_<category>_<option>.xml`) and the receipt art (`receipt-leopold-cafe@3x.png`, `art-receipt-thumb.svg`). Kabir/Meera heads (`avatar-6`, `avatar-7`) already exist.

---

## 6. Modules

This section records how the build was split and what each module had to deliver; it stays useful as the acceptance and UI-test reference per area. Every module ended with the app installed and running on its device showing the new screens, each compared with its ref (flow.md "Testing on devices"), unit + UI tests green, and no new build warnings. Commits were made per finished part (subjects below; imperative, ≤ 72 characters).

### 6.1 M2 Data layer (per platform, before any lane starts)
Two tracks that can run in parallel on each platform (they meet at the scenario table):
- **M2-data**: Domain port (every calculation in `seed/verify.py` / `domain.md`), models, formatting, clock, `LedgerStore`/`LedgerRepository` with read models and every shared action (§3.4), JSON persistence, demo seed + `DemoSeed` with the scenario runner, `tick`, ProfileStore extensions (domain.md §1.1: character avatar case, username, pronoun, payment methods, Show to friends), unit tests (§3.9). Android: Gradle deps + desugaring + serialization plugin, manifest entries, backup rules.
- **M2-ui**: routes (§2.2), router + hosts (§2.7–2.8), tab shell with the glass tab bar, the Add sheet, toast host, the `activity` tab container with `ActivityHeader`, a placeholder file for **every** route (upgraded `ScreenPlaceholder` / `PlaceholderScreen`: route id, owner lane, spec section, parameters, back/close; tab roots draw inside the shell), the ★ pieces of the shared kit (§5.1; the components pass supplies the rest), service stubs (`NotificationService`, `ReceiptReader`, `CameraSource`, `QRCode`, `QRScanner`, `ContactsDirectory`, `SpeechInput`, `Exporter`, each with a do-nothing or minimal implementation and its final signature), iOS `AppDelegate` + Info.plist keys, the debug menu (core sections) and the full scenario table for every id in §1, launch-argument parsing (§3.10), per-module empty debug-action files, `ScreenID` for every id.
- Home placeholder computes `screen.home<State>` from `snapshot.home.state`, so M1's onboarding UI tests keep passing (All set → `screen.homeFirstDay`).

**Acceptance**
- Unit tests reproduce every Figma number; JSON round-trips; both platforms agree on the fixture.
- Every id in §1 starts via the hook and shows its route placeholder (or the finished M1 screen) with the right tab, stack and sheet; `screen.<routeId>` present.
- Tabs switch; ＋ opens the Add sheet on every tab and each row opens its modal placeholder; back / ✕ / scrim / Android back behave per §2.5; `didSave`, `didCreateGroup`, `requirePro`, result channel and deep links work against placeholders.
- Data persists across relaunch; Reset onboarding clears both stores; debug menu actions work.
- The ★ components render in the gallery and previews, matching their component refs in `.figma-cache/components/<id>.png` (regenerate with `tools/fetch_figma.py`).

**UI tests** (`paybakUITests/ShellUITests.swift`, `androidTest/…/ShellTest.kt`): tab switching from each tab; Add sheet from each tab → each modal → ✕; start-screen smoke test that launches every id in §1 and asserts its root id (batched to keep runtime sensible); persistence (load demo → relaunch → Home still shows data); onboarding → Home first day still green.

**Commit subjects** (iOS shown; Android identical with "Android:")
- `iOS: add the ledger domain model and its calculations`
- `iOS: add the ledger store with JSON persistence and demo seed`
- `iOS: add app routes, the tab shell and a placeholder per screen`
- `iOS: add the Add sheet, app toasts and the debug menu`
- `iOS: add the tab bar, nav headers and the Activity header`
- `iOS: add balance cards, activity rows, empty states and user avatars`
- `iOS: start any screen id with its demo scenario`
- Android only, first: `Android: add serialization, desugaring and full-app dependencies`

### 6.2 Lane A: M3 Add & Record → M6 Activity
**M3 scope**: `addExpense` (Empty/Filled, amount rules, people, title, every row: Paid by + payer editor, split editor with Equally/Exact/Percent/Shares + fair rotation + live footer, Category, Currency via `pickCurrency` + rate line, Group via `pickGroup`, Date/Due via `pickDate` + quick chips, Repeat → `repeatRule` (lane C; placeholder until merged) behind `requirePro`, Receipt → free photo attach / Pro `scanReceipt` result, Notes, discard alert, edit mode); the pickers `pickPeople` (Split with, with guests and "Add a new friend" → `addGuest`), `pickCurrency`, `pickDate`, `pickGroup`; `expense` detail **base** (hero, share card, split card, receipt, Edit → edit mode; comments/history/actions sections render existing data read-only); `recordPayment` (all methods, UPI preview + Copy, For/Date/Proof, summary line, prefill args incl. `settleRecordKabir`, edit mode); `payment` detail (pending payer view, 6/7 rows, Edit, Cancel alert; Confirmed / Not received states as proposed); `lendMoney` (I lent / I borrowed, installments + stepper, repeats, first due, schedule preview); `loan` detail (Active / Paid back / Overdue, Remind → `remind`, Record repayment → `recordPayment`); `newGroup` (Group / Project segments, types, members via `pickPeople`, currency, simplify, description, cover, budget, contribution) → `didCreateGroup`.
**M3 acceptance**: refs `addExpense*`, `expenseAdded`, `recordPayment`, `settleRecordKabir`, `paymentRecorded`, `settlePaymentPending`, `lendMoney`, `loan*`, `newGroup*` match; saving changes Home/Groups numbers through the store (e.g. adding the Olive Garden dinner to an account without it adds ₹2,100 owed); Save rules and split validation per add-expense §3.
**M3 UI tests**: the add-expense §13 flow (＋ → Save disabled → 2800 → Priya/Esha/Dev → Save enabled → title → Food → This weekend → Save → detail ₹700 × 4 + toast); Exact error (Dev 550 → Done disabled, "₹150 left"; 700 → enabled); discard alert; record payment to Meera → pending detail → Cancel payment; lend money with 3 installments → loan detail rows; new group → Create → Groups tab + group pushed + toast.
**M3 commits**: `iOS: add the Add expense form and its pickers` · `iOS: add the split editor and the payer editor` · `iOS: add the expense detail` · `iOS: add Record payment and the payment detail` · `iOS: add Lend money and the loan detail` · `iOS: add the New group form`.

**M6 scope**: `ActivityTimelineView` (day groups, event copy templates, pinned claim cards, row destinations, empty state with the First-day Rive), `activityLog(filter)`; expense detail extras (comments + pinned composer, History rail, Flag an issue sheet, Disputed notice + Resolve, Delete alert → soft delete + toast, `recentlyDeleted` + Restore + retention); `notifications` inbox (Today/Earlier, unread dots, Mark all read, inline confirm card, row destinations); `NotificationService` (permission status, categories/channels, scheduling from the store's `revision`, action handling, deep links, 60-item iOS cap), lock-screen states; the *Notifications* debug actions.
**M6 acceptance**: refs `activityTimeline`, `activityEmpty`, `expenseVilla`, `expenseComment`, `expenseDelete`, `expenseDisputed`, `recentlyDeleted`, `notifications` match; the two notifications show the designed title/body and their actions work (Confirm from the notification updates Home without opening the app; Not received opens the sheet); reminders are scheduled at 21:00 on the schedule and cancelled when settled.
**M6 UI tests**: timeline order and claim card on top; confirm from Activity → card gone on Home; delete Villa → Recently deleted shows it with "30 days left" → Restore → back in Goa Trip; comment send → appears + composer clears; Resolve on the disputed expense clears the chip and logs history; inbox Mark all read clears the bell dot; notification tap routing via launching with the deep link argument (a `link` debug launch key, M2).
**M6 commits**: `iOS: add the Activity timeline` · `iOS: add comments, flags and edit history to expenses` · `iOS: delete expenses and restore them from Recently deleted` · `iOS: add the notifications inbox` · `iOS: post local notifications for reminders and claims`.

### 6.3 Lane B: M4 Groups & Friends → M5 Settle up → M7 Projects
**M4 scope**: `groups` tab (Groups/Friends segments, list order, archived section, project budget bars, friends summary, empty states, header actions, large-title collapse), `group` detail (balances card, paid-vs-share, simplify footnote, foreign-currency lines, expenses by date, **empty state = `newGroupCreated`**), `groupSettings` (+ leave rules, blocked alert, members via `pickPeople`, currency, simplify toggle, recurring row → `requirePro(.recurring)`), `friend` (owes you / you owe / settled / guest variants, history, groups together, reminders toggle), `addFriend` (search, invite link share, QR scan, My QR code sheet with generated QR + Copy + Share, contacts on Paybak Add/Added, Invite → guest), QR services.
**M4 acceptance**: refs `groupsList`, `friendsList`, `groupsEmpty`, `groupGoaTrip`, `groupSettings`, `groupLeaveBlocked`, `groupDubaiWeekend`, `friendRohan`, `addFriend`, `myQrCode`, `friendAnanyaGuest`, `newGroupCreated` match; Goa nets and Dubai ₹41,118 come from the store.
**M4 UI tests**: Groups ↔ Friends segment; open Goa Trip → balances; gear → Leave → blocked alert → Settle up opens Record payment (placeholder until lane A merges); Friends → Rohan → Remind opens the sheet route; Add friend → Invite Ananya → guest page; My QR code sheet → Copy → toast; empty account shows Groups empty → New group.
**M4 commits**: `iOS: add the Groups tab with groups and friends` · `iOS: add the group detail and group settings` · `iOS: add the friend page` · `iOS: add Add friend and the My QR code sheet`.

**M5 scope**: `owedBreakdown`, `oweBreakdown`, `settleUp` (plan from the store, Settle → `recordPayment(prefill)`, Remind → `remind`, pending rows), `remind` sheet (tones, templates, editable message, Send in Paybak → `sendReminder` + toast, Share… → share sheet), `notReceived` sheet (note template → `markNotReceived`), the receiver confirm flow on every surface (the shared card from M2 wired to the store; Home collapse sequence handled by lane D), prefill rules for `settleRecordKabir` and the pending state data for `settlePaymentPending` (screens are lane A's; verify them once lane A's M3 is on main, merging `main` into the lane branch as needed).
**M5 acceptance**: refs `settleOwedBreakdown`, `settleOweBreakdown`, `settleUp`, `settleRemind`, `settleNotReceived` match; `settleRemindShare` shows the system sheet with the message; Confirm anywhere → Home +₹2,200 from 3 people; Not received keeps +₹2,900 and removes the card.
**M5 UI tests**: Home owed card → breakdown → Settle up → Kabir Settle opens the prefilled form; Rohan Remind → Send in Paybak → toast "Reminder sent to Rohan" and the timeline logs it; (demo `D`) Not received → Send → card gone, balance unchanged; Confirm from the notifications inbox → Home updated.
**M5 commits**: `iOS: add the balance breakdowns and the Settle up plan` · `iOS: add the Remind sheet with sharing` · `iOS: add the Not received sheet and wire payment confirmation`.

**M7 scope**: `project` detail (Active / Over budget / Closed / Archived, budget card, components list, paid vs fair share, history link, who owes whom, pinned Add component), Add/Edit component sheet (lifecycle Planned → Bought → Done), `projectSettings` (contribution Equal/Percent/Fixed, members, budget, pool toggle, Close alert), close/archive rules, *Projects* debug actions.
**M7 acceptance**: refs `projectDrone`, `projectOverBudget`, `projectAddComponent`, `projectSettings`, `projectClosed`, `projectArchived` match; the projects §11 test values hold.
**M7 UI tests**: projects §11 (₹52,000, 87 % used, ₹8,000 left, projection ₹58,000, 8 rows, Rohan owes Dev ₹8,500; Add component disabled until named → new Planned row first; actual cost changes spent/shares; Close → alert → Closed state).
**M7 commits**: `iOS: add the project dashboard` · `iOS: add project components and project settings` · `iOS: close and archive projects`.

### 6.4 Lane C: M8 Profile, Settings & Pro → M9 Insights & AI
**M8 scope**: `profile` tab (avatar circle, handle rule, Pro row free/Pro, rows with values, Sign out alert → session clear), avatar system (`AvatarCharacterView` compositing the part files in the manifest order and crops, bitmap cache for small sizes; replaces M2's stub so every "You" circle shows the character), `editAvatar` (draft, Boy/Girl keeping picks, chips, tiles previewing the look, Shuffle, Save, dirty rule, Discard alert), `paywall` (plans, mock purchase, Restore, member/status form) + Welcome (`finishPaywall` continuation), `paymentDetails` (methods, primary, Show to friends, preview, Copy, Add sheet UPI/Bank with validation + error, method actions), `settingsCurrency` (default via `pickCurrency`, per-currency toggle), `settingsNotifications` (six toggles, schedule checks → reschedule, Muted friends → `mutedFriends`), `privacyData` (discovery toggles + contacts permission, Export → `requirePro`, Recently deleted count → `recentlyDeleted`, Delete account blocked / confirm), `privacyExport` (format, ranges, default ticks, Select all, CSV/PDF + share), `helpFeedback` + `helpAnswer`, contact/rate.
**M8 acceptance**: refs `profile`, all `editAvatar*`, `editAvatarDiscard`, `paywall`, `proWelcome`, `paymentDetails`, `paymentAddUpi`, `paymentAddUpiError`, `settingsCurrency`, `settingsNotifications`, `privacyData`, `privacyExport`, `privacyDeleteBlocked`, `helpFeedback` match; the avatar renders match the manifest checks; the export file opens in the share sheet with the demo's September records.
**M8 UI tests**: profile §7 flow (Edit avatar → Quiff → Girl → Ponytail → Boy keeps Quiff → Back → alert → Keep editing → Save → Profile shows it; clean Back → no alert); paywall → Start trial → Welcome "Wed 7 Oct" → Done → returns to Export; Add UPI error then valid → new row; Delete account blocked alert → Settle up; notification toggles persist across relaunch.
**M8 commits**: `iOS: add the Profile tab` · `iOS: compose the custom avatar from its parts` · `iOS: add the avatar editor` · `iOS: add the Pro paywall and welcome` · `iOS: add payment details and payment methods` · `iOS: add currency and notification settings` · `iOS: add privacy settings, export and account deletion` · `iOS: add Help & feedback`.

**M9 scope**: `InsightsView` (month row + navigation, hero + trend, monthly bars, categories, who you spent with Groups/Friends, lent vs borrowed, collapse header, locked state with blur + notice), `ask` (start/answer/draft states, §4.3 assistant, chips, mic), `scanReceipt` (camera with simulated feed, flash, Upload photo, reading, Check receipt edit rows, Assign items with live totals, result to the form; free = attach only), `recurring` (needs-amount drafts, rules card, next dates), `repeatRule` sheet (frequencies, day of month, variable toggle, next draft), `enterDraftAmount`, the *Scan* debug toggle.
**M9 acceptance**: refs `insightsSeptember`, `insightsScrolled`, `insightsLocked`, `askStart`, `askAnswer`, `askConfirm`, `scanCamera`, `scanReview`, `scanAssign`, `scanAddExpense`, `recurringFlat302`, `recurringRepeat`, `recurringEnterAmount` match; every number from the store; OCR reads the bundled receipt to the designed items and totals on both platforms.
**M9 UI tests**: Insights free → locked → See Pro → trial → unlocked; month ‹ changes the report; Ask "Who owes me money?" answer text equals the Figma copy; draft "Add ₹600 for a cab, split with Esha and Dev" → Save → Saved → View opens the expense; scan with the simulated camera → review 6 items → assign → Continue → form shows ₹2,300 "Itemized · 3 people"; enter the Cooking gas amount → expense appears in Flat 302.
**M9 commits**: `iOS: add Insights` · `iOS: add the Ask Paybak assistant` · `iOS: add receipt scanning` · `iOS: add recurring expenses`.

### 6.5 Lane D: Home
**Scope**: `HomeScreen` from `snapshot.home` (First day / Active / All settled / pending claims stacked newest first), greeting from `AppClock`, header (logo long-press → `debugMenu`, sparkle → `requirePro(.ask)`, bell + unread dot → `notifications`), balance summary cards → breakdowns, Settle up, Due soon rows (Remind / Settle / row bodies), Recent activity (See all → Activity tab, rows → details), First day buttons, confirm card flow (Confirmed animation, hold, collapse, new activity row, toast 16 above the tab bar; Reduce Motion cross-fade), Not received → sheet, scroll under the tab bar with the fade, `PBAttentionRow`, the Home header.
**Acceptance**: `homeFirstDay`, `homeActive`, `homeAllSettled`, `homeConfirmPayment`, `settlePaymentConfirmed`, `homeAddSheet` match `ref/v2/…` / `ref/…`; every number from the store; every element leads somewhere (§2.3).
**UI tests**: fresh onboarding → First day → Add expense opens the modal; demo (`D`, claim pending) → Confirm → +₹2,200 from 3 people + "Esha paid you" row + toast; bell → Notifications; owed card → breakdown; Goa Settle → Record payment prefilled ₹1,400; See all → Activity tab; long-press logo → debug menu → Start an empty account → First day.
**Commits**: `iOS: build Home from the ledger` · `iOS: add the confirm payment card and its flow to Home` · `iOS: wire the Home header, cards and rows`.

---

## 7. Conflict avoidance and cross-cutting rules

### 7.1 File ownership (lanes edit only their own files)
| Owner | iOS | Android |
|---|---|---|
| M2 (core; no lane edits) | `App/*`, `Navigation/*` (except lane sections), `Domain/*` (lanes may add new files in `Domain/<Module>/`), `Data/LedgerStore*.swift`, `Data/LedgerFile.swift`, `Model/*`, `DesignSystem/*` existing + shared kit, `Services/*` stubs' signatures, `Debug/*` except per-module files, `Resources/*`, the pbxproj | `navigation/*` (except lane sections), `domain/*` (lanes may add `domain/<module>/`), `data/*`, `ui/theme/*`, `ui/components/*` shared kit, `service/*` signatures, `src/debug/…/debug/*` except per-module files, Gradle files, `AndroidManifest.xml`, `res/values/strings.xml`, `res/xml/*` |
| Lane A | `Features/{AddExpense,Pickers,Expense,Payments,Loans,NewGroup,Activity (timeline, log, recently deleted),Notifications}/`, `Services/NotificationService*.swift`, `DesignSystem/Components/AddRecord/`, `Data/Lanes/LedgerStore+AddRecord.swift`, `+Activity.swift`, `Debug/Scenarios/Scenarios+{AddRecord,Activity}.swift`, `Debug/Menu/ActivityDebugActions.swift`, `paybakTests/{AddRecord,Activity}*`, `paybakUITests/{AddRecord,Activity}*` | `feature/{addexpense,pickers,expense,payments,loans,newgroup,activity,notifications}/`, `service/notifications/`, `ui/components/addrecord/`, `res/values/strings_{add,activity}.xml`, matching debug/test files |
| Lane B | `Features/{Groups,Friends,Settle,Projects}/`, `Services/{QRCode,QRScanner,ContactsDirectory}*.swift`, `DesignSystem/Components/{Groups,Settle,Projects}/`, `Data/Lanes/LedgerStore+{Groups,Settle,Projects}.swift`, scenarios/menu/tests for those modules | `feature/{groups,friends,settle,projects}/`, `service/qr/`, `service/contacts/`, `ui/components/{groups,settle,projects}/`, `res/values/strings_{groups,settle,projects}.xml`, matching debug/test files |
| Lane C | `Features/{Profile,Avatar,Pro,Settings,Insights,Ask,Scan,Recurring}/`, `Services/{ReceiptReader,CameraSource,SpeechInput,Exporter}*.swift`, `DesignSystem/Components/{Avatar,Insights,Assistant}/`, `Assets.xcassets/{AvatarParts,Receipt}/`, `Data/Lanes/LedgerStore+{Settings,Insights,Recurring}.swift`, `Model/ProfileStore+Avatar.swift`, `+PaymentMethods.swift`, scenarios/menu/tests | `feature/{profile,avatar,pro,settings,insights,ask,scan,recurring}/`, `service/{receipt,camera,speech,export}/`, `ui/components/{avatar,insights,assistant}/`, `res/drawable/avatar_part_*`, `res/drawable/receipt_*`, `res/values/strings_{profile,settings,insights}.xml`, matching debug/test files |
| Lane D | `Features/Home/`, `DesignSystem/Components/Home/`, `Debug/Scenarios/Scenarios+Home.swift`, `Debug/Menu/HomeDebugActions.swift`, Home tests | `feature/home/`, `ui/components/home/`, `res/values/strings_home.xml`, matching debug/test files |

- **Split folders**: `Features/Activity/ActivityTabScreen.swift` + `ActivityHeader` (Android `feature/activity/ActivityTabScreen.kt`) are M2's; every other file in that folder is lane A's. `InsightsView` lives in lane C's `Features/Insights/`.
- **Routes**: all routes exist after M2. If a lane truly needs a new route or parameter, it adds it inside its own section of `Route.swift` / `Route.kt` and `RouteView.swift` / `RouteContent.kt`: the files have one `// MARK: Lane A` … `// MARK: Lane D` block each, separated by blank lines and ended by a `// end Lane X` line, so parallel additions merge cleanly. Screen initializers/composable signatures in §2.2 are the contract; don't change them.
- **Store**: never edit `LedgerStore.swift` / `LedgerRepository.kt` or the models' core. New stored fields go below the model's `// lane X fields` anchor with a default value (decoders tolerate them). New actions/queries go in the lane's `Lanes/` file and call `mutate`.
- **Strings**: Android: each lane's strings in `res/values/strings_<module>.xml`, names prefixed `<module>_` (`add_`, `activity_`, `groups_`, `settle_`, `projects_`, `profile_`, `settings_`, `insights_`, `home_`); M2 keeps `strings.xml` (M1 + shell). iOS: inline string literals in views, as M1 does; no String Catalog is added or edited during lanes.
- **Icons and assets**: every icon exists; new art only in the lane's asset folders (above). Don't edit `Icons.swift` / `PbIcon.kt`.
- **Shared components**: use as is. A needed fix or variant goes in its own small commit touching only that component (`iOS: add a trailing badge to the setting row`).
- **Gradle, pbxproj, manifest, Info.plist**: M2 only; lanes didn't edit them.
- **Tests**: per-lane test files only; `AppLaunch.swift` / `LaunchPaybak.kt` helpers are M2's (they take `startScreen`, `now`, `pro`, `link`, `resetOnboarding`).
- **Merging**: before its final merge each lane merged `main` into its branch (a merge commit, never a rebase) and ran all unit and UI tests; then it was merged into `main`. Lanes could merge `main` earlier to pick up another lane's screens.

### 7.2 Cross-cutting UI rules
- **Toasts**: `router.toast(text)`; black capsule `PBToast` with the check-circle icon, 2 s, fade; position 16 above the tab bar on tab roots and 50 above the bottom edge elsewhere (above the keyboard when it's up); a new toast replaces the current one; test id `toast` (label = text). Copy in use: "Expense added", "Expense deleted", "Expense restored", "Payment recorded", "Payment confirmed", "Loan added", "Group created", "Project created", "Reminder sent to {first name}", "UPI ID copied", "Link copied", "UPI ID added".
- **Alerts**: `PBAlert` rules: title + message + two actions (Cancel/Keep left, the action right; Destructive red only for destroying data), never dismissed by the scrim, Android back = cancel. Designed copy wins; undesigned copy follows the specs' proposals verbatim.
- **Sheets**: `PBSheet`; Medium sheets size to content, Large sheets reach 8 below the top safe area; header ✕ on the right, grabber on; keyboard: the sheet rises with the IME and its field stays visible.
- **Large titles**: tab roots use Nav Header Large Title in the scroll content; once the title scrolls under the status bar the Inline bar appears (white 90 % + blur 24, Headline centred, 44 tall at the top safe area). Timeline keeps its header fixed (activity §3.1).
- **Pushed headers**: `PBPushHeader` pinned with the white band 0–106 behind it; content scrolls under the band.
- **Keyboard**: forms focus their first field on appear where the spec says (amount, UPI); CTAs ride the keyboard (`KeyboardGap` on iOS, `imePadding` on Android); tapping a row that opens a picker dismisses the keyboard first; the pinned composer rides the keyboard.
- **Empty states**: `PBEmptyState` with the reused Rive art (§5.3); no dead ends: every empty state has its designed action or none by design ("A calm confirmation").
- **Formatting**: only through Domain `Format` (money: symbol + Indian grouping for INR, no decimals when whole, `+`/U+2212 signs where the spec shows them, `{CODE} {amount}` for other currencies in lists; dates en-GB `EEE d MMM`, `d MMM`, "Today"/"Yesterday", times `h:mm a` lowercase; ranges `1 Sep – 30 Sep 2026`). Never call a platform formatter in a view.
- **Colour rules**: red only for overdue and destructive actions/errors (foundations-rules); pending, disputed and not-received states are black/gray.
- **Motion**: push 350 ms ease-in-out; modal 300 ms ease-out up / down; sheets 300 ms ease-out + scrim fade; alerts 200 ms dissolve; component state changes per spec (250 ms smart animate). Reduce Motion: cross-fades only.
- **Haptics**: selection on chips/segments/tiles, success on Save/Confirm, warning on validation errors, light impact on Rive taps (existing).
- **Accessibility**: every interactive element has a label; money reads naturally ("owes you 800 rupees"); VoiceOver/TalkBack order follows the visual order; Dynamic Type up to xxxLarge as M1.
- **Test ids**: `<screen>.<element>` with the specs' names; roots per §2.2; rows `<screen>.row.<entityId>` with stable demo ids; sheets `<sheet>.sheet`; alerts `<screen>.alert.<action>`; Android `testTag` with `testTagsAsResourceId` (already on at the root).
- **Safe areas and width**: lay out from the safe area; phone content max width 430 centred (existing `phoneContentWidth()`); portrait only.

---

## 8. Decisions taken here, and open items

**Decided (implement as written):**
1. Pushed and modal screens hide the tab bar; one main stack over the tab shell (Figma draws no tab bar on pushed frames).
2. Ask Paybak is Pro-gated (the paywall's feature list names the AI assistant); the sparkle opens the paywall on the free plan and continues to the chat after the trial starts.
3. The demo (`D`) shows Esha's pending claim (base + `eshaClaimsPayment`); `homeActive`, `homeAddSheet`, `settleRemind`, `settleRemindShare`, `homeAllSettled` and `settlePaymentConfirmed` start from the base alone (`D−claim`), as domain.md §7.4 maps (home-v2 decision a).
4. Arjun stays `preset(0)` in the demo; only the Profile and Edit avatar scenarios seed the character (profile §9 #2).
5. Home state is derived from data; the debug menu loads scenarios instead of switching a Home flag.
6. The profile holds identity: name, avatar, the default currency (`currencyCode`), username, pronoun, payment methods and Show to friends (domain.md §1.1); the ledger holds everything else, including settings and the entitlement.
7. "Created" after New group is the group/project detail (lane B's file) in its empty state; lane A owns the flow into it. Record payment and the payment detail are lane A's files; lane B owns the settle-side entry points and the receiver's sheets.
8. The Repeat sheet is a lane C route used by lane A's form; the people/currency/date/group pickers are lane A routes used by everyone.
9. Receipt reading is real OCR on both platforms with a simulated camera feed on simulators/emulators.
10. Android keeps its own navigator (no navigation library); iOS keeps `NavigationStack` + nested full-screen covers; both sheets are custom overlays.

**Open (use the proposal until someone decides):**
- Support email for "Contact us": `SupportContact.email = "support@paybak.app"` (one constant; confirm).
- Neutral reminder template copy (settle §6.3 proposal).
- Everything the specs mark *proposal* is implemented as proposed.
