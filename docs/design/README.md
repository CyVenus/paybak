# Paybak design docs

**Paybak** is a native app for splitting shared expenses with friends, groups and project teams: who paid, who owes whom, when it's due, and settling up (UPI, bank, cash). It only **records** money; it never moves it. The iOS app (`ios/`, SwiftUI) and the Android app (`android/`, Jetpack Compose) are built from this folder. They have no backend yet: everything runs on-device against a local store with the real business logic, and a demo dataset reproduces every number in Figma. (`web/` is the marketing site and isn't covered here.)

This folder is the complete design spec the apps were built from: product rules, architecture, business logic, tokens, components, one spec per Figma page, 2× reference renders and the source art. It was read from Figma file `2SPNUpHlG8bCO62YfwRuRi` ("Paybak-iOS") with read-only tools between 30 Sep and 1 Oct 2026, and every number in the specs comes from the Figma node tree.

Quick facts:
- Canvas: iPhone 17 Pro, 402 × 874. Everything is at 1× in **pt (iOS) = dp (Android)**. The top safe area is 62 and the bottom 34, so the bottom safe-area edge is at frame y 840.
- Light theme only. Font: Manrope (400–800). Icons: HugeIcons stroke-rounded. Illustrations: Open Peeps / Open Doodles, mostly as Rive files.
- Figma pages: 01 Foundations · 02 Components · 03 Launch & Onboarding · 04 Home · 05 Profile · 06 Add & Record · 07 Groups & Friends · 08 Settle Up · 09 Activity · 10 Projects · 11 Insights & AI · 12 Settings & Pro.

---

## 1. What's in this folder

| Path | What it is |
|---|---|
| `README.md` | This page: what to read, how the specs map to the code, the cross-cutting rules, and what's still open. |
| `flow.md` | Product rules and user decisions: screens and ids of the onboarding flow, persistence, debug hooks, platform notes, device testing, "Resolved decisions", UI test ids and the **FULL APP SCOPE**. Overrides everything else. |
| `app-architecture.md` | The architecture and build plan for the full app: every screen id with its route, container, spec, ref and demo seed (§1), routes and navigation (§2), the data layer and debug hooks (§3), real vs simulated features (§4), the component kit (§5), per-module scope, acceptance and UI tests (§6), file ownership and cross-cutting UI rules (§7), decisions (§8). |
| `domain.md` | Business logic: entities, invariants, every algorithm (balances, splits and fair rotation, simplify debts, payments, loans, projects, recurring, reminders, insights, the assistant) with worked examples, the demo dataset and its JSON schema. |
| `spec-digest.md` | The per-page reports from the spec-writing pass: what each spec covers, the screen list and its open issues. |
| `tokens.md` | Colour, spacing, radius, size and text-style tokens, plus the material/effect styles. |
| `foundations-rules.md` | The designer's usage rules from page 01 and the page 02 captions, verbatim, with implementation notes. |
| `components-core.md` | Brand, buttons, badge, avatars, page dots, segmented control, input field, divider and the sign-in/setup components. |
| `components-home.md` | Nav header, icon/glass buttons, tab bar, balance cards, section header, attention/activity rows, empty state, sheet rows, Add sheet, sheet container, materials. |
| `components-app.md` | The 36 component sets added for the full app (Shared, Forms & Money, Lists & Detail, Progress & Charts, Assistant & Scan, Feedback, Art / Receipt), plus changes to the older components (§8). |
| `screens-launch.md` | Splash, Welcome 1–3, Get Started. |
| `screens-signin.md` | Sign in, Verify, Wrong code. |
| `screens-setup.md` | Setup 1–4 and All set. |
| `screens-home.md` | Home: Active, First day, All settled, ＋ Add sheet. |
| `screens-home-v2.md` | Home changes for the full app: the change report for pages 03–04, Home as the tab root (navigation map), and `homeConfirmPayment`. Wins over `screens-home.md` where they differ. |
| `screens-profile.md` | Profile tab and the avatar editor, including the avatar parts system. |
| `screens-add-expense.md` | The Add expense flow: form, people picker, payer sheet, split editor, category/currency/due-date sheets, Expense added. |
| `screens-record-lend-group.md` | Record payment, Lend money (IOU) and the loan detail, New group / project. |
| `screens-groups.md` | Groups and Friends tab, group detail and settings, friend page, Add friend, My QR code. |
| `screens-settle.md` | Balance breakdowns, Settle up plan, Remind and Not received sheets, payment confirmation. |
| `screens-activity.md` | Activity timeline, expense detail (comments, history, delete, disputed), Recently deleted, Notifications inbox, lock-screen notifications. |
| `screens-projects.md` | Project dashboard in its four states, Add component, Project settings. |
| `screens-insights-ai.md` | Insights, Ask Paybak (the on-device assistant), Scan receipt, Recurring expenses. |
| `screens-settings.md` | Paybak Pro paywall and welcome, Payment details, Currency, Notifications & reminders, Privacy & data, Export, Delete account, Help & feedback. |
| `rive.md` | The six `.riv` files: artboards, state machines, view models, triggers, the slot-vs-view sizes and the watermark finding. |
| `rive-ios-api.md` | rive-ios 6.28.0 reference: loading, touches, data binding, lifecycle, the original SwiftUI wrapper and the Xcode package setup. |
| `rive-android-api.md` | rive-android 11.12.1 reference: Gradle and manifest setup, the Compose API, data binding, the original Compose wrapper, what was verified. |
| `designer-notes.md` | Every designer note on the Figma canvas (pages 02, 04–12), verbatim, per section and frame. |
| `figma-index.md` | Every frame and component on pages 02, 04–12 with its node id (find a node here, then its render or JSON in `.figma-cache/`). |
| `seed/demo.json` | The demo dataset the apps load (dates relative to the load day; named scenarios). Generated; don't edit by hand. |
| `seed/build_demo.py` | Writes `demo.json`. Edit the records here. |
| `seed/verify.py` | The reference implementation of `domain.md`: loads the demo with the clock pinned to the Figma moment and asserts 135 Figma numbers, strings and rules. Plain Python 3.9+, no dependencies. |
| `seed/rates.json` | Bundled "today's rates" (INR per unit) for new foreign-currency records. |
| `ref/<id>.png` | 2× (804 × 1748; tall frames taller) Figma renders of every screen id, named by screen id. Some ids also have a `_1x` crop. They include the iOS status bar, keyboard and home indicator, which the app doesn't draw. |
| `ref/v2/<id>.png` | Re-renders of pages 03–04 from 1 Oct (onboarding unchanged; Home with the curly ’). Use these for the Home ids. |
| `assets/icons/` | 65 icon SVGs (24 × 24, stroke 1.5, #0A0A0A) + `INDEX.md` (component → file → HugeIcons source → usage, sizing and tint rules) and `INDEX-v2.md` (full-app check). |
| `assets/brand/` | App mark, logo lockups, P glyph, app-icon sources and the 1024 px iOS icon + `INDEX.md` (geometry and icon recipes). |
| `assets/avatars/` | The seven peep heads (`avatar-1…7`, SVG + @3x PNG) + `INDEX.md` (who is who, presets, drawing rules). |
| `assets/images/` | Static fallbacks for the Rive illustrations, the tab-bar ＋ glyph, receipt art + `INDEX-home.md`, `INDEX-v2.md`. |
| `assets/avatar-parts/` | The avatar editor's part layers (`boy/`, `girl/`, 84 SVGs in the 772 × 842 rig), `manifest.json` (categories, options, layer order, crops, rules) and `check/` (Figma-vs-native proof renders; don't ship). |
| `tools/figma_rest.py` | Figma REST helper (node JSON, renders, SVG exports) with a built-in rate limiter. |
| `tools/fetch_figma.py` | Re-downloads the whole Figma file into `.figma-cache/`. |
| `.figma-cache/` | Not in git (`.gitignore`). Created by `tools/fetch_figma.py`: page structure, node JSON per page, 2× renders of every screen and component, SVG exports, a fresh frame index and designer notes. The specs point here for node data. |

Not kept from the spec-writing session: the per-frame node dumps and `get_design_context` output (the same data is in `.figma-cache/nodes/`), scratch scripts, the draft Rive wrapper sources (replaced by the apps' code) and the fonts (bundled in both apps).

**Figma tool names in the specs.** The specs say how each page was read. "Plugin-API dumps" (or "node dumps") are node trees read through the Figma Plugin API with the Figma MCP server's `use_figma` tool. `get_design_context` (React/Tailwind reference code), `get_screenshot` and `download_assets` are other Figma MCP tools. "REST" means the Figma REST API, which `tools/` uses. All of these read the same node data, and none of the outputs are needed to use the specs.

### 1.1 Asset notes worth knowing
- **Brand** (`assets/brand/`): `app-mark.svg` is the preferred mark (true 60 % corner smoothing rebuilt; scale freely). The `app-mark-{160,96,40,28}.svg` files are Figma exports with plain `rx` corners. `app-icon-1024.png` is the iOS app icon (1024 × 1024, opaque RGB). `app-icon-fullbleed.svg` is the source for the iOS icon and the Android adaptive icon; `p-glyph-160.svg` is the adaptive/monochrome foreground. `logo-horizontal.svg` / `logo-stacked.svg` have outlined wordmarks; in the app use live Manrope ExtraBold text. `apple-logo.svg` and `google-g.svg` are copies of the icons (never tint the G).
- **Avatars** (`assets/avatars/`): `avatar-1` Arjun (preset 0, the debug seed default), `avatar-2` Priya (preset 1), `avatar-3` Rohan (preset 2), `avatar-4` Esha (preset 3), `avatar-5` Dev (preset 4), `avatar-6` Kabir, `avatar-7` Meera. 120 × 120 SVG / 360 × 360 PNG, transparent.
- **Duplicates** (verified; ship one): `assets/images/brand-app-mark-{96,28}.svg` are byte-identical to `assets/brand/app-mark-{96,28}.svg`; `assets/images/peep-head-{rohan,priya}.svg` are the same art as `avatars/avatar-{3,2}.svg`; `assets/images/receipt-thumb.svg` and `art-receipt-thumb.svg` are both the Art / Receipt thumbnail.
- **Rive fallbacks**: `assets/images/empty-first-day.svg`, `empty-all-square.svg` (240 × 180), `illustration-reminders.svg`, `illustration-all-set.svg` (362 × 300) are static art used only if the `.riv` can't load.
- `assets/images/add-button-plus.svg` is `plus.svg` with **stroke 2** (the tab-bar ＋ only).

---

## 2. Reading order

### 2.1 For a designer
1. This README, then `foundations-rules.md` and `tokens.md`.
2. `components-core.md`, `components-home.md`, `components-app.md` (§8 lists what changed in the older components).
3. The screen spec for the page you're working on, with its `ref/<id>.png` open next to it (map in §2.5). Each spec ends with its open questions and the undesigned states it proposes; those are the design gaps.
4. `designer-notes.md` for the canvas captions, and `spec-digest.md` for the open issues per page.
5. Product context: `flow.md` "FULL APP SCOPE" and `app-architecture.md` §4 (what is real vs simulated).

### 2.2 For an iOS or Android engineer
1. This README, especially **§3 Rules you must not miss** and **§5 Known gaps**.
2. `flow.md`: product rules, debug hooks, test ids, device testing.
3. `app-architecture.md`: find your screen id in §1, its route in §2, the data layer in §3.
4. `tokens.md`, `foundations-rules.md`, then the component specs (`components-core.md`, `components-home.md`, `components-app.md`).
5. The screen spec for your area (§2.5), with its ref open. Compare your build against the ref at the same scale (the refs are 2×; a 3× simulator screenshot needs scaling by 2/3).
6. For Rive work: `rive.md`, then `rive-ios-api.md` or `rive-android-api.md`. The wrappers in the apps (`ios/paybak/paybak/Rive/`, `android/.../rive/`) are the source of truth; the API docs explain why they look the way they do.
7. §4 below maps every spec to the code.

### 2.3 For someone changing business logic
1. `domain.md`: the rules and the worked examples.
2. `seed/verify.py`: the reference implementation (when it and `domain.md` disagree, `verify.py` is right). Change the rule here first.
3. `seed/build_demo.py` if the demo records change, then regenerate and re-check:
   ```sh
   cd docs/design/seed && python3 build_demo.py && python3 verify.py   # → All 135 checks pass.
   python3 docs/design/seed/verify.py                                  # check only, from the repo root
   ```
   `python3 docs/design/seed/verify.py` must pass before you change the apps.
4. Port the change to both apps (§4.1: `Domain/` + `Data/` on iOS, `domain/` + `data/` on Android) and their unit tests, which port `verify.py`'s checks. If `demo.json` or `rates.json` changed, copy them into both apps (§4.2).

### 2.4 Source-of-truth order when files disagree
`flow.md` ("Testing on devices", "Resolved decisions", "UI tests and test IDs", "FULL APP SCOPE") > `app-architecture.md` (structure, ids, routes, ownership, data-layer shape) > `domain.md` + `seed/` (business rules and numbers) > the screen specs (layout, copy, per-screen behaviour; `screens-home-v2.md` over `screens-home.md`) > `components-app.md` / `components-home.md` / `components-core.md` > `tokens.md`. If you still find a conflict, trust Figma, then `flow.md`.

### 2.5 Screen → spec → reference map
Every screen id, with its route, container, spec section, ref and demo seed, is in `app-architecture.md` §1. By spec:

| Spec | Figma page | Screen ids | Module (lane) |
|---|---|---|---|
| `screens-launch.md` | 03 Launch & Onboarding | splash, welcome1–3, getStarted | M1 |
| `screens-signin.md` | 03 Launch & Onboarding | signIn, verify, verifyWrong | M1 |
| `screens-setup.md` | 03 Launch & Onboarding | setup1–4, allSet | M1 |
| `screens-home.md` + `screens-home-v2.md` (v2 wins where they differ) | 04 Home | homeFirstDay, homeActive, homeAllSettled, homeAddSheet, homeConfirmPayment; Home navigation | Lane D |
| `screens-profile.md` | 05 Profile | profile, editAvatar*, editAvatarDiscard; avatar parts system | M8 (lane C) |
| `screens-add-expense.md` | 06 Add & Record | addExpense*, expenseAdded | M3 (lane A) |
| `screens-record-lend-group.md` | 06 Add & Record | recordPayment, paymentRecorded, lendMoney, loan*, newGroup*, newGroupCreated | M3 (lane A) |
| `screens-groups.md` | 07 Groups & Friends | groupsList, friendsList, groupsEmpty, group*, friend*, addFriend, myQrCode | M4 (lane B) |
| `screens-settle.md` | 08 Settle Up | settle* | M5 (lane B) |
| `screens-activity.md` | 09 Activity | activity*, expense*, recentlyDeleted, notifications, lock* | M6 (lane A) |
| `screens-projects.md` | 10 Projects | project* | M7 (lane B) |
| `screens-insights-ai.md` | 11 Insights & AI | insights*, ask*, scan*, recurring* | M9 (lane C) |
| `screens-settings.md` | 12 Settings & Pro | paywall, proWelcome, payment*, settings*, privacy*, helpFeedback | M8 (lane C) |

Modules and lanes are how the build was split (`app-architecture.md` §6); the code keeps the same grouping. Each spec's first table lists its frames with node ids and refs.

The onboarding and first Home screens in detail (the node JSON is in `.figma-cache/nodes/` after `tools/fetch_figma.py`; page 03 = `3-4.json`, page 04 = `3-5.json`):

| Screen id (flow.md) | Figma node | Spec section | 2× reference | 1× reference |
|---|---|---|---|---|
| `splash` | 22:32 | screens-launch.md §1 | `ref/splash.png` | `ref/splash_1x.png` |
| `welcome1` / `welcome2` / `welcome3` (ONE screen, `step` 1…3) | 22:55 / 22:133 / 22:200 | screens-launch.md §2 | `ref/welcome1.png` / `welcome2.png` / `welcome3.png` | `ref/welcome{1,2,3}_1x.png` |
| `getStarted` | 22:272 | screens-launch.md §3 | `ref/getStarted.png` | `ref/getStarted_1x.png` |
| `signIn` | 39:356 | screens-signin.md §1 | `ref/signIn.png` | `ref/signIn_1x.png` |
| `verify` | 39:534 | screens-signin.md §2 | `ref/verify.png` | `ref/verify_1x.png` |
| `verifyWrong` (same screen as verify, error state) | 39:644 | screens-signin.md §3 | `ref/verifyWrong.png` | `ref/verifyWrong_1x.png` |
| `setup1` | 42:665 | screens-setup.md §1 | `ref/setup1.png` | — |
| `setup2` | 44:938 | screens-setup.md §2 | `ref/setup2.png` | — |
| `setup3` | 46:1021 | screens-setup.md §3 | `ref/setup3.png` | — |
| `setup4` | 46:1148 | screens-setup.md §4 | `ref/setup4.png` | — |
| `allSet` | 46:1223 | screens-setup.md §5 | `ref/allSet.png` | — |
| `homeFirstDay` | 24:326 | screens-home.md §3 | `ref/v2/homeFirstDay.png` (first pass: `ref/homeFirstDay.png`) | — |
| `homeActive` | 24:5 | screens-home.md §2 | `ref/v2/homeActive.png` (first pass: `ref/homeActive.png`) | — |
| `homeAllSettled` | 24:414 | screens-home.md §4 | `ref/v2/homeAllSettled.png` (first pass: `ref/homeAllSettled.png`) | — |
| `homeAddSheet` | 24:520 (+ overlay 24:808) | screens-home.md §5 | `ref/v2/homeAddSheet.png` (+ `ref/v2/homeAddSheet-overlay.png`) | — |
| `homeConfirmPayment` | 167:11424 | screens-home-v2.md §3 | `ref/homeConfirmPayment.png` | — |

The references show Figma's static illustrations, not the Rive art.

---

## 3. Rules you must not miss (cross-cutting)

1. **Layout from the safe area.** Never hard-code 62/34. Draw no status bar, keyboard, home indicator, or the invisible `Hotspot — …` frames. **Exception:** the Splash lockup is centred in the full screen, not the safe area (screens-launch.md §1.2).
2. **Light theme only.** Force light on both platforms. The screen background is `color/bg/primary` #FFFFFF everywhere, and status-bar content is dark.
3. **Fonts:** bundle `Manrope-{Regular,Medium,SemiBold,Bold,ExtraBold}.ttf` (in the apps: `ios/paybak/paybak/Resources/Fonts/`, `android/app/src/main/res/font/manrope_*.ttf`) and **reference them by PostScript name** (`Manrope-Regular` … `Manrope-ExtraBold`). The editor verified these names in the font `name` tables. The Medium, SemiBold and ExtraBold files each declare their own family name ("Manrope Medium" …), so on iOS use `Font.custom("Manrope-SemiBold", size:)`, not family + weight. On Android use one `FontFamily` with an explicit `FontWeight` per file (400/500/600/700/800). Letter spacing is a % of font size (tokens.md).
4. **Icons:** 24 × 24 stroke SVGs, #0A0A0A. Tint them at runtime and **scale the whole icon** (20 pt → stroke 1.25, 16 pt → 1.0, 14 pt → 0.875), exactly like Figma. Only the tab-bar ＋ uses stroke 2 (`assets/images/add-button-plus.svg`). Never tint the Google G. Tint the Apple logo only black or white.
5. **Text is verbatim.**
   - Curly ’ (U+2019) in most copy, but the **straight ' in "You're owed"** (Home). *(Superseded: Home now uses the curly ’ too, so it's curly everywhere; screens-home-v2 §1.3.)*
   - − (U+2212) in amounts, · (U+00B7) in subtitles, — (U+2014) on setup3, “” (U+201C/U+201D) where quoted.
   - U+2028 LINE SEPARATOR in the setup3 headline and the allSet body: implement it as `\n`.
   - Hard line breaks in the Get Started headline and body.
6. **Input field focus/error ring:** draw the 1.5 pt ring as an **inside overlay**, and keep the text at a constant 16 pt inset. Figma shifts the text to 17.5 because the stroke counts in layout. Don't copy that shift.
7. **Code input row:** it fills the 362 content width with space-between, so the real gap is 14.8 and the boxes sit at x 20 / 82.8 / 145.6 / 208.4 / 271.2 / 334. The component itself is 348 wide, but the screens stretch it.
8. **Avatar circle fill:** `bg/card` #F5F5F5 on white surfaces, `bg/primary` #FFFFFF inside #F5F5F5 cards (Home attention rows, the setup3 preview card). The head art is transparent, 120 × 120: scale it to the circle and clip.
9. **Rive layout:** three artboards are the Figma slot plus **12 pt of bleed per side**: Get Started 386×284 on a 362×260 slot, Notifications 386×324 on 362×300, AllSquare 264×204 on 240×180.
   - **Reserve the Figma slot** in layout and overlay the Rive view centred on it at native size.
   - Don't clip it, don't scale it down, and don't draw a native grey card behind Get Started.
   - The table is in `rive.md`, and each screen spec has the exact rects.
10. **Rive binding and taps:**
    - Always load the main artboard by name, and bind the default view-model instance.
    - Bind `reduceMotion` to the OS setting.
    - Never fire tap triggers yourself: the files' own listeners fire them.
    - For haptics, observe only the generic trigger (`personTapped` / `bellTapped` / `characterTapped` / `tapped`).
11. **Pressed states** are static Figma variants. Swap them while the finger is down, instantly or with a ≤ 100 ms fade:
    - Pill buttons change fill only.
    - Text buttons go to 50 % opacity.
    - Icon buttons fill with `bg/selected`.
    - Android has no ripple (`indication = null`).
12. **Motion:** Figma has **no keyframe motion anywhere** (`get_motion_context` is empty on every frame and component). The only designed motion is:
    - the prototype transitions: push 350 ms ease-in-out, splash dissolve 400 ms ease-out, Skip dissolve 300 ms, Add sheet move-in 300 ms ease-out;
    - the Rive files.

    Everything else in the specs marked "suggestion"/"proposal" (splash entrance, Welcome text slide, dot/progress animation, toasts) is ours to choose.
13. **One shared asset set:**
    - Brand: use `assets/brand/…`, and draw the mark natively with continuous corners on iOS.
    - Avatars: use `assets/avatars/avatar-1…7`.
    - `assets/images/brand-app-mark-*.svg` and `assets/images/peep-head-*.svg` are verified duplicates. Don't ship both.

The full-app cross-cutting UI rules (toasts, alerts, sheets, large titles, keyboard, formatting, colour, motion, haptics, accessibility, test ids) are in `app-architecture.md` §7.2.

---

## 4. From spec to app code

### 4.1 Where each spec lives in the code
iOS paths are under `ios/paybak/paybak/`, Android paths under `android/app/src/main/java/app/paybak/paybak/` unless they start with `src/` or `res/` (then under `android/app/`).

| Spec | iOS | Android |
|---|---|---|
| `tokens.md`, `foundations-rules.md` | `DesignSystem/{Colors,Typography,Fonts,Metrics,Materials}.swift` | `ui/theme/{Color,Type,Dimens,Materials,Motion,Theme}.kt` |
| `components-*.md` | `DesignSystem/Components/` (`PB…`; module folders `AddRecord/`, `Avatar/`, `Groups/`, `Home/`) | `ui/components/` (`Pb…`; same module folders) |
| Icons and art (`assets/`) | `DesignSystem/Icons.swift`; `Assets.xcassets/{Icons,Avatars,AvatarParts,Images,AppIcon}` | `ui/icons/PbIcon.kt`; `res/drawable/ic_*`, `avatar_part_*`; launcher icon in `res/mipmap-*` |
| `screens-launch.md`, `screens-signin.md`, `screens-setup.md` | `Features/{Launch,SignIn,Setup}/` | `feature/{launch,signin,setup}/` |
| `screens-home.md`, `screens-home-v2.md` | `Features/Home/` | `feature/home/` |
| `screens-profile.md` | `Features/{Profile,Avatar}/` | `feature/{profile,avatar}/` |
| `screens-add-expense.md` | `Features/{AddExpense,Pickers,Expense}/` | `feature/{addexpense,pickers,expense}/` |
| `screens-record-lend-group.md` | `Features/{Payments,Loans,NewGroup}/` | `feature/{payments,loans,newgroup}/` |
| `screens-groups.md` | `Features/{Groups,Friends}/` | `feature/{groups,friends}/` |
| `screens-settle.md` | `Features/Settle/` | `feature/settle/` |
| `screens-activity.md` | `Features/{Activity,Expense,Notifications}/`, `Services/` (notifications) | `feature/{activity,expense,notifications}/`, `service/notifications/` |
| `screens-projects.md` | `Features/Projects/` | `feature/projects/` |
| `screens-insights-ai.md` | `Features/{Insights,Ask,Scan,Recurring}/` | `feature/{insights,ask,scan,recurring}/` |
| `screens-settings.md` | `Features/{Pro,Settings}/` | `feature/{pro,settings}/` |
| `app-architecture.md` §2 (routes) | `Navigation/` (`Route.swift`, `RouteView.swift`, `AppRouter+Main.swift`) | `navigation/` (`Route.kt`, `RouteContent.kt`) |
| `app-architecture.md` §3, `domain.md` | `Domain/` (calculations, models), `Data/` (`LedgerStore`, persistence; `Data/Lanes/`), `Model/` (profile) | `domain/` (`calc/`, `model/`, …), `data/` (`ledger/`, `ledger/lanes/`) |
| `rive.md`, `rive-*-api.md` | `Rive/PaybakRive.swift`, `Rive/RiveAssets.swift`; files in `Resources/Rive/` | `rive/PaybakRive*.kt`; `PaybakApplication.kt` (`Rive.init`); files in `res/raw/` |
| Debug hooks (`flow.md`, `app-architecture.md` §3.10) | `App/DebugLaunchOptions.swift`, `App/ScreenID.swift`, `Debug/` (`Menu/`, `Scenarios/`, `Gallery/`, `DemoSeed.swift`) | `src/debug/java/app/paybak/paybak/debug/` (`DebugLaunch.kt`, `ScreenIds.kt`, `menu/`, `scenarios/`, `gallery/`) |
| Tests | `ios/paybak/paybakTests/`, `ios/paybak/paybakUITests/` | `src/test/`, `src/androidTest/` |

Test ids (`<screen>.<element>`, roots `screen.<id>`) are the same on both platforms (flow.md, app-architecture §7.2).

### 4.2 Debug start screens and demo data
Debug builds only (compiled out of release):

| | iOS (launch arguments) | Android (intent extras) |
|---|---|---|
| Start at a screen id | `-startScreen <id>` | `--es startScreen <id>` |
| Component gallery | `-startScreen gallery [-galleryPage <n>]` | `--es startScreen gallery [--ei galleryPage <n>]` |
| Reset (profile + ledger) | `-resetOnboarding YES` | `--ez resetOnboarding true` |
| Load the demo without changing the start screen | `-demoData YES` | `--ez demoData true` |
| Apply seed scenarios | `-scenario a,b` | `--es scenario a,b` |
| Pin the clock | `-now 2026-09-30T21:15` | `--es now 2026-09-30T21:15` |
| Override the plan | `-pro YES\|NO` | `--es pro YES\|NO` |
| Open a deep link | `-link paybak://…` | `--es link paybak://…` |
| Friends auto-approve your payments (default on; UI tests turn it off) | `-autoApprove YES\|NO` | `--ez autoApprove true\|false` |
| How long they take (default 5 s) | `-autoApproveAfter <s>` | `--ei autoApproveAfter <s>` |

Examples: `xcrun simctl launch --terminate-running-process <udid> app.paybak.paybak -startScreen groupGoaTrip`; `adb shell am start -S -n app.paybak.paybak/.MainActivity --es startScreen groupGoaTrip`. In the app, long-press the Paybak logo on Home for the debug menu: load demo data (at the Figma date or around today), start an empty account, reset onboarding, pin or advance the clock, toggle Pro, play the friend's side (claims, confirmations, flags, comments, and the auto-approve toggle: 5 s after you record a payment to a friend they confirm it and the payment-approved scene plays over whatever is open), post notifications, per-module scenarios, and the component gallery.

- **Screen ids:** every id in `app-architecture.md` §1 (all 93 Figma frames plus a few undrawn *proposal* states); the refs use the same names. Each id loads its seed before showing the screen: `E` = empty account, `D` = the demo at Figma parity (base records + `eshaClaimsPayment`), `D−claim` = the base records alone, `+x` = a named scenario on top, `P` = Pro.
- **Demo data:** `seed/demo.json` holds the base records with relative dates (`D±n`), anchored to the load day; Figma parity is load day Wed 30 Sep 2026 with the clock at 21:15. Scenarios: `empty`, `eshaClaimsPayment`, `eshaPaymentConfirmed`, `eshaPaymentNotReceived`, `paymentToMeeraPending`, `paymentToMeeraConfirmed`, `paymentToKabirPending`, `eshaFlagsSeafood`, `lendDev`, `lendDevOverdue`, `devBuysGps`, `closeDrone`, `weekendTrek`, `allSettled`, `pro`. Mapping and rules: `domain.md` §7, `app-architecture.md` §3.8.
- **Copies in the apps** (byte-identical to `seed/` today; copy again whenever you regenerate): `ios/paybak/paybak/Resources/Seed/demo.json`, `ios/paybak/paybak/Resources/Rates/rates.json`, `android/app/src/debug/assets/seed/demo.json` (debug only; the JVM unit tests read it too), `android/app/src/main/assets/rates.json`.
- A new account after onboarding starts empty (Home "First day"); only the debug hooks and the debug menu load the demo.

### 4.3 Refreshing from Figma
The Figma file is `2SPNUpHlG8bCO62YfwRuRi`. The specs and refs here are a snapshot of 30 Sep–1 Oct 2026; nothing regenerates them automatically. To get fresh data for a change:

1. Put a Figma personal access token that can read the file in `~/.config/paybak/figma_token` (one line; never commit it or paste it into a doc).
2. Run `python3 docs/design/tools/fetch_figma.py` from anywhere. It writes `docs/design/.figma-cache/` (gitignored, about 70 MB): `structure.json`, `nodes/<page>.json`, `renders/<id>.png` (2× screens), `components/<id>.png` (2× component sets), `svg/<id>.svg` (icons and art), `index.json`, `INDEX.md` (like `figma-index.md`) and `notes.md` (like `designer-notes.md`). Options: `--pages 3:5,77:101` for some pages only, `--skip nodes,components,svg` to skip steps, `--offline` to rebuild the index and notes from a cached `structure.json`. A full run is about 30 REST calls (a few minutes).
3. For single nodes: `python3 docs/design/tools/figma_rest.py nodes <id,…> --out x.json`, `… render <id,…> --outdir DIR [--scale 2] [--format png|svg]`, `… file --depth 2`.
4. Compare with the committed `ref/` and specs, update the spec by hand, and copy a new render to `ref/<screenId>.png` when a screen changed.

**Rate limits:** the account is on Figma's Education plan, whose **MCP is heavily throttled** (200 calls/day for the whole account, and about one call a minute once exceeded; this cut short several spec reads). The **REST API is the reliable path**, so prefer the tools above. `figma_rest.py` keeps every process on the machine under 8 REST calls a minute (its state lives in the system temp dir) and retries 429s after `Retry-After`. The REST node JSON includes the prototype links (`interactions`), dev-mode annotations and bound variables (as ids, not token names; match them against `tokens.md`).

---

## 5. Known gaps, decisions and open items

### Open items (as of 1 Oct 2026)
1. **Rive watermark on iOS.** All six `.riv` files carry Rive's export watermark. rive-ios 6.28.0 plays a ~2 s black "RIVE" pre-roll on every new illustration view (Welcome, Get Started, Setup 4, All set, both Home empty states, and the screens that reuse them); rive-android 11.12.1 doesn't. The fix is to **re-export the files without the watermark** and replace them in `ios/paybak/paybak/Resources/Rive/` and `android/app/src/main/res/raw/` (same names); no code changes. Don't work around it in code. (`rive.md`, `rive-ios-api.md` §1)
2. **"Keep balances per currency" isn't functional yet.** The toggle in Settings › Currency (screens-settings §6) is saved (`settings.keepBalancesPerCurrency`), but balances still convert everything to the default currency.
3. **Copy and contacts not in Figma:** the "Contact us" address is a placeholder (`support@paybak.app`, one constant per app); the Neutral reminder template is a proposal (screens-settle §6.3); Terms / Privacy Policy links have no URLs (Get Started, paywall).
4. **Simulated, by design for now** (flow.md FULL APP SCOPE, app-architecture §4): there's no backend, the friend's side (confirm, not received, claims) runs from the debug menu, Pro is a mock purchase, and the camera feed is simulated on simulators/emulators.
5. **Design questions:** nested 16/20 pt icons in the new components keep stroke 1.5 while the older components scale it (components-app §11 #1; the apps scale it); `Row / Activity` changes (2-line titles, 3-line notification bodies, unread dot) aren't listed in components-app §8 (screens-activity, spec-digest).
6. **Not verified:** a real-device build on either platform, a real finger tap through `RiveView` on iOS, Android API 24–28 devices (rive-ios-api §11, rive-android-api §10).
7. **Proposals awaiting confirmation:** everything marked *proposal* in the specs is implemented as written. Each screen spec ends with its open questions, and `spec-digest.md` lists them per page; `app-architecture.md` §8 lists the decisions taken.

### 5.1 Needs a decision from the user / asset owner (v1, onboarding + Home)
`flow.md` "Resolved decisions" (30 Sep) settles #2–#8, #10 and the orientation part of #14 (portrait only); #12 and #13 are superseded by the v2 specs (§5.5). #1 is still open (above).

1. **BLOCKER (iOS): the six `.riv` files are watermarked.**
   - With rive-ios 6.28.0, every new artboard instance first shows an opaque **black box with a white "RIVE" wordmark for about 2 s**. It replays after `restart()` and on every new view. This is confirmed from runtime source and simulator screenshots, and it affects Welcome, Get Started, Setup 4, All set and both Home empty states.
   - **The editor verified that rive-android 11.12.1 does not show it** (emulator run, illustration drawing about 1.1 s after launch). The web runtime 2.43 does show it.
   - **Fix:** re-export the files without the watermark. Don't work around it in code, because it's Rive's licensing mechanism. The code needs no change once clean files arrive.
2. **Back from Get Started** isn't designed (no back button, no reaction). Proposal (screens-launch.md): system back / edge swipe returns to Welcome at the step the user left from.
3. **Back on Welcome step 1 (Android)** exits the app, because Splash isn't on the back stack. That's an assumption.
4. **Left swipe on Welcome step 3:** proposal is to do nothing. Only the "Get started" CTA leaves the pager.
5. **Setup 1 initial avatar selection:** the frame shows Arjun selected, but the designer's note says "initials are the fallback". Proposal: a fresh profile has nothing selected, and continuing without a choice gives initials. A saved choice or the debug seed (avatar 0) shows as selected.
6. **Copy that isn't in Figma** (proposed; confirm or replace):
   - placeholders: email/phone "you@example.com" (the component default, which reads oddly for phone users), name "Your name", UPI "yourname@bank";
   - currency search empty state "No currencies match “{query}”";
   - UPI validation error "Enter a UPI ID like name@bank." (or skip validation);
   - toast "UPI ID copied" (this one comes from the Toast component's description).
7. **Phone normalisation:** the Figma annotation only says "phone numbers get +91". Proposal: if there's no leading "+", show "+91 " + the input as typed.
8. **`onboardingComplete` timing:** flow.md sets it on "Go to Home". Proposal: also persist it as soon as All set appears, and disable back on All set.
9. **Setup 3 toast position:** the Toast description says "50 above the bottom edge", which would overlap Continue. Proposal: 16 pt above the CTA.
10. **Terms / Privacy Policy** are **not bold** in Figma (Medium, #6B6B6B, underlined; the rest of the line is #A3A3A3). The original product brief said "bold". The spec follows Figma. The links do nothing (no URLs yet).
11. **Apple button** is the custom Figma pill, not `ASAuthorizationAppleIDButton`, since there's no real auth. Revisit Apple's button guidelines if real Sign in with Apple is added.
12. **Home First day "Add expense"** opens the Add sheet. flow.md allows "do nothing" as well.
13. **Out-of-scope Figma frames:** "Home — Confirm payment" (167:11424) is in the Home section but not in flow.md, so it isn't specced. Kabir/Meera avatars and the WhatsApp icon are exported but unused.
14. **Not designed at all** (implementer's choice; keep it plain and native):
    - the **debug menu** (long-press on the Home logo): iOS `Menu`/`confirmationDialog`, Android `DropdownMenu`;
    - **orientation**: Figma is portrait-only, so the editor recommends locking phones to portrait;
    - **Android adaptive icon** XML: the recipe is in `assets/brand/INDEX.md`;
    - loading and error states for everything, since there's no backend.

### 5.2 Resolved by the editor (files changed)
1. **Missing references:** `ref/signIn_1x.png`, `ref/verify_1x.png` and `ref/verifyWrong_1x.png` were referenced but missing. I exported them from Figma (`get_screenshot`, 402 × 874).
2. **Rive slot sizes:**
   - `rive.md` said "artboard sizes match the Figma slots", which is wrong for Get Started, Notifications and AllSquare. I replaced it with a slot-vs-view table.
   - I independently confirmed the 12 pt bleed by rendering the `Still` pose and comparing ink bounding boxes with the Figma fallback art. Notifications and AllSquare are the same size and offset exactly +12/+12. First Day and All Set are identical to their slots. Get Started had already been confirmed by pixel measurement.
   - The screen specs already had the right rects.
3. **Watermark on Android / web:** checked (see 5.1 #1). I added notes to `rive.md`, `rive-ios-api.md` §1 and `rive-android-api.md` §0.
4. **Wrapper comments** claimed "artboard size = Figma slot size". I corrected those comments only (no code changes, so the compile verification still holds) in the draft wrappers (since replaced by the apps' code) and the copies in both API docs. I also added layout notes to `rive-ios-api.md` §8.1 and `rive-android-api.md` §8.4: reserve the slot and overlay the view. `TappableIllustration`'s `fillMaxWidth()` would shrink the art by about 6 %.
5. **Code Input 348 vs 362:** both values are right. The component is 348 with fixed sizing, and the screen instances are FILL = 362, which gives the 14.8 gap. Verified in Figma; `components-core.md` §5.3 updated.
6. **Avatar fill on cards:** verified in Figma. `Row / Attention` avatars are white (`bg/primary`) and `Row / Activity` avatars are `bg/card`. Updated in `components-core.md` §3.2 and `assets/avatars/INDEX.md`.
7. **GLASS parameters:** `tokens.md` only had radius 6 for Glass Small, while `components-home.md` also gave refraction 0.4 and depth 8. I read the effect styles in Figma and completed `tokens.md` (Glass: refraction 0.7, depth 30, dispersion 0.2; Glass Small: refraction 0.4, depth 8, dispersion 0.1; light intensity 0.25 for both).
8. **Duplicate assets checked:**
   - `images/brand-app-mark-{96,28}.svg` are byte-identical to `brand/app-mark-{96,28}.svg`.
   - `images/peep-head-{rohan,priya}.svg` render the same as `avatars/avatar-{3,2}.svg`.
   - `images/add-button-plus.svg` is the `plus.svg` path with stroke 2.
   - I marked the canonical set here and in `assets/avatars/INDEX.md`.
9. **iOS app icon:** added `assets/brand/app-icon-1024.png` (opaque RGB) and listed it in `assets/brand/INDEX.md`.
10. **Fonts:** verified the PostScript names and weights, and found the per-file family names (§3 rule 3).
11. **Camera tile icon coordinates:** clarified inner-circle (11, 11) vs option (16, 16) in `components-core.md` §5.4. Both specs were right.
12. **Checks that passed with no change needed:**
    - All 93 SVGs are valid XML starting with `<svg`, and all PNGs are real PNGs.
    - Every file named in a spec exists.
    - Every screen id has a spec section and a 2× reference.
    - Component geometry agrees across `components-core.md`, `components-home.md` and the screen specs: page dots, setup header, buttons, text-button hit areas, badge, tab bar, header buttons and badge dot, balance cards, attention/activity rows, currency row, payment-preview card, avatar option.

### 5.3 Proposals already in the specs (not in Figma; implement as written unless product says otherwise)
- **Motion:**
  - Splash entrance: spring scale + wordmark fade.
  - Welcome text slide/fade (150 ms out, 300 ms in), timed to the Rive exit/enter measured on the **web** runtime 2.37. Verify on device.
  - Page-dot and progress-segment growth (0.3–0.35 s).
  - Numeric "Step N of 4" transition, caret blink (1 s), toast fade, Add sheet spring.
  - Reduce Motion variants for all of the above.
- **Verify screen:**
  - Auto-verify after about 250 ms; ignore Figma's prototype-only 2000 ms timeout and push to Setup 1.
  - "Resend code" resets in place: clear, restart the 30 s timer, keep focus. Don't navigate as the prototype does.
  - Undesigned resend layouts keep the label top at y 308 / 342.
  - Typing after an error starts over from box 1; backspace clears the error.
  - Error haptic.
- **Keyboard:**
  - signIn and setup1: the CTA rides 12 pt above the keyboard (designed).
  - setup2/3: the CTA stays pinned and the keyboard covers it (proposal), so the setup3 preview stays visible.
- **Setup 1 camera tile:** photo shown in the 46 circle with the selected ring. Tapping a selected tile re-opens the picker. The photo is saved as a 512 px JPEG.
- **Setup 2 search:** ISO 4217 list, ranking rules, clear button. Use Figma's names and symbols for the six designed currencies ("UAE Dirham", "S$"). Symbol tiles use Headline for symbols of 2 characters or fewer, otherwise Caption/1 with the code.
- **Home:**
  - The header scrolls with the content.
  - Bottom content inset about 107 pt; a solid white strip behind the status bar.
  - Tab bar bottom = max(21, bottomInset − 13) on Android.
  - Suggested pressed fill `bg/card-pressed` for cards and rows.
  - Custom overlay for the Add sheet (not `.sheet` / `ModalBottomSheet`), with swipe-down to dismiss.
  - Android ✕ = `close.svg` at 38 dp, tinted #1A1A1A.
- **Narrow Android screens** (360 dp):
  - Shrink code boxes to min(48, (w − 60)/6).
  - Shrink avatar options to min(56, (w − 20)/6).
  - Shrink illustration slots proportionally before other spacing.
- **Input field ring:** overlay, not layout (§3 rule 6).
- **Colours inferred** (not readable in Figma): the Destructive text-button chevron, and leading-icon tints in the hidden Pressed/Disabled button variants (icon colour = label colour).
- **Icon-size exceptions:** Badge / Pill and the currency radio check use 14 pt icons, although the Foundations text says "16pt in cards and badges". Follow the components.

### 5.4 Platform integration notes (from the Rive integration research; verified unless marked)
- **iOS:**
  - Use the legacy `RiveViewModel` API with `enableAutoBind`.
  - Every `reset()`/`stop()` binds a **new** instance, and the callback fires twice. Re-apply values and listeners, as the wrapper's `didBind` does.
  - Data-binding setters need `play()` on a paused view (issue #383).
  - Open issue #427 leaks an artboard when auto-bind controllers are churned, so don't create controllers in list cells.
  - Because of `MemberImportVisibility`, files need `import RiveRuntime` / `import Combine`.
  - Commit `Package.resolved`. `xcodebuild -derivedDataPath` needs `-scheme paybak`.
  - Don't match on `stateChanges()` names: `·` comes back mis-decoded.
- **Android:**
  - **Must** add `PaybakApplication` calling `Rive.init(this)` and register it in the manifest. Without it every illustration is silently blank.
  - Adding Rive bumps core-ktx to 1.17.0 and lifecycle to 2.9.4 (fine with compileSdk 37).
  - Name clashes: `app.rive.Rive` vs `app.rive.runtime.kotlin.core.Rive`, and `app.rive.Alignment` vs Compose `Alignment`.
  - Use `RivePointerInputMode.Observe` for Onboarding (parent swipe) and for the Home illustrations inside the scroll content.
  - Pin 11.12.1: the 12.0 API renames the factory functions.
- **Not verified yet:**
  - iOS: a real UIKit finger tap through `RiveView` (taps were tested through the state machine in a macOS harness); the Welcome swipe with `.allowsHitTesting(false)`; a real-device build; #427 memory with our files; the Concurrency API.
  - Android: the `RiveInitializer` manifest route; the legacy `RiveAnimationView`; API 24–28 devices; Vulkan; rotation; haptic feel on hardware.
  - Both: the AllSquare overhang on a device (the ink-box check was done in the web runtime).

### 5.5 Superseded v1 statements (the v2 files win)
- §3 rule 5: Home's "You’re owed" now uses the curly ’ (screens-home-v2 §1).
- §5.1 #6: the UPI validation copy is Figma's "Enter a UPI ID like name@bank" (no period; screens-settings §5.6).
- §5.1 #12: Home First day "Add expense" opens Add expense directly (screens-home-v2 §2.3).
- §5.1 #13: "Home — Confirm payment" is now in scope (`homeConfirmPayment`).
- flow.md's older "Profile is out of scope" and "other tabs are inert" lines: superseded by FULL APP SCOPE.
- `HomeState` debug switching and the Home placeholder: Home's state now comes from data (app-architecture §2.8, §3).
