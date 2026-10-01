# Paybak design rules (from Figma "01 Foundations" + "02 Components" captions)

Read-only extraction from file `2SPNUpHlG8bCO62YfwRuRi`. The **quoted text is verbatim** (curly quotes, en/em dashes, "−" minus signs and "₹" kept exactly). The "Implementation notes" under each group are mine; they translate the rule into what native code must do. Token names/values are in `tokens.md`.

## 1. Colour: frame "Rules" (19:8), inside "Colour"
1. “Background is always white. Text, icons and controls use shades of black.”
2. “Cards are #F5F5F5 — never add borders or shadows. On a card, nested pills and avatars turn white.”
3. “Red #C93636 is only for overdue, errors, over budget and delete. Brand exceptions: the official Google “G” and the WhatsApp logo (share sheet only).”
4. “You’re owed = bold black “+₹”. You owe = gray “−₹”. Settled = light gray “₹0”.”
5. “Charts stay gray: track #EBEBEB, bars #D1D1D1, current value #0A0A0A. Only an over-budget segment is red. #2B2B2B is the camera backdrop only.”

Implementation notes
- Screen background = `color/bg/primary` #FFFFFF everywhere (one light theme; ignore system dark mode, i.e. force light on both platforms).
- Cards = `color/bg/card` #F5F5F5, radius `radius/card` 20, **no border, no shadow**.
- "On a card … turn white": use the **On Card** variants (Button / On Card, Badge / Pill Style=On Card, Avatar / Circle Type=Icon On Card) when the parent is a #F5F5F5 card.
- Red (`color/*/destructive` #C93636, pressed #A92E2E, subtle #FBEBEB) only for: overdue badge/rows, error states (input/code), over-budget, delete/sign-out. Nothing else.
- Money: owed `+₹…` bold black (`text/primary`); owe `−₹…` (U+2212 minus) gray (`text/secondary`); settled `₹0` light gray (`text/tertiary`).

## 2. Typography: frame "Rules" (19:265), inside "Typography"
1. “Manrope for all app UI. Apple kit parts (status bar, grabber, glass close) keep SF Pro.”
2. “Title/1 once per screen. Amounts use the Amount styles so numbers line up.”
3. “Body copy is #6B6B6B; captions and dates #A3A3A3 (never for essential text).”

Implementation notes
- Bundle Manrope 400/500/600/700/800 (bundled in the apps: iOS `ios/paybak/paybak/Resources/Fonts/`, Android `android/app/src/main/res/font/`). PostScript names `Manrope-Regular/-Medium/-SemiBold/-Bold/-ExtraBold`. System-drawn parts (status bar, sheet grabber, iOS glass close button, keyboard) stay SF Pro / system.
- Use the text styles exactly (size, line height, tracking). Tracking is a % of font size (`tokens.md`), e.g. Button/Large −0.5 % × 17 = −0.085 pt.
- Amounts: use `Amount/*` styles. Consider tabular digits (`.monospacedDigit()` / `fontFeatureSettings = "tnum"`) so numbers line up (the rule's intent; Figma doesn't set a feature flag).
- Body copy colour `text/secondary` #6B6B6B; captions/dates `text/tertiary` #A3A3A3, never for essential text.

## 3. Spacing, radius & size: frame "Rules" (19:352), inside "Spacing, radius & size"
1. “20pt screen margins · 24pt between sections · 16pt card padding · 12pt between cards.”
2. “Radius: pills are full, cards 20, inputs and tiles 14, sheets 40.”
3. “Minimum tap target 44pt. Primary buttons are 52pt tall.”

Implementation notes
- `layout/screen-margin` 20 → content width on the 402-pt canvas = **362**. `layout/section-gap` 24. `layout/card-padding` 16. Card-to-card 12.
- Pills (buttons, badges, segments, page dots, progress segments) = fully rounded (`radius/full` 999 → use Capsule / `CircleShape`/50 %). Cards 20. Inputs, code digits, tiles 14. Sheets 40.
- Every tappable thing is at least 44 × 44 (`size/tap`). Text buttons are 44 tall even though the label is 20. Large buttons 52 (`size/button-lg`), Small 36 (`size/button-sm`; in rows/cards; extend the hit area to 44).

## 4. Materials: frame "Rules" (19:436), inside "Materials"
1. “Liquid Glass only on floating chrome: the tab bar and toolbar buttons. Never on cards.”
2. “The glass shadow is the only shadow in the app.”

Implementation notes
- Glass is used only by: the floating tab bar (`Material/Glass`) and toolbar/icon buttons with Style=Glass (`Material/Glass Small`, e.g. the Home bell).
  - `Material/Glass`: drop shadow x 0, y 8, blur 32, #0A0A0A @ 10 % + GLASS (refraction 0.7, depth 30, radius 16).
  - `Material/Glass Small`: drop shadow x 0, y 4, blur 16, #0A0A0A @ 8 % + GLASS (radius 6).
  - iOS 26+: `.glassEffect()` (e.g. `.glassEffect(.regular.interactive(), in: .circle)` for the 44-pt button). Android: fill `bg/glass` (#FFFFFF @ 72 %) + that drop shadow + 1 px inside border `border/glass-highlight` (#FFFFFF @ 60 %); backdrop blur optional.
- No other element has a shadow (not cards, not sheets' content, not buttons).

## 5. Icons: frame "Rules" (19:593), inside "Icons"
1. “HugeIcons stroke-rounded, 24pt grid, 1.5pt stroke. Use 20pt in tiles and 16pt in cards and badges.”
2. “Active tab icons are black; inactive are #6B6B6B.”
3. “Brand logos keep their official colours: Apple, the Google “G” and WhatsApp. WhatsApp appears only in the share sheet and is never offered in icon pickers.”

Implementation notes
- Files and details: `assets/icons/INDEX.md`. Scale the 24 × 24 SVG. The stroke scales with it (20 pt → 1.25, 16 pt → 1.0, 14 pt → 0.875), which is exactly what Figma does.
- Component reality check: Badge / Pill's icon is **14 pt** and the Currency radio check is 14 pt (the component beats the rule text).
- Tab bar: active icon + label `icon/primary`/`text/primary` #0A0A0A; inactive #6B6B6B (`icon/secondary`). Same stroke icon for both; there are **no filled variants**.
- Apple logo: black, or white on the black button. Google "G": never tinted.

## 6. Illustrations: frame "Rules" (19:747), inside "Illustrations"
1. “Open Peeps + Open Doodles by Pablo Stanley (CC0). Line #0A0A0A, fill white, tint #EBEBEB.”
2. “One illustration per screen, at the top. Never recolour with the red accent.”

Implementation notes
- Scene illustrations are **Rive** files in this build (see `rive.md`). Static avatar heads are SVG/PNG in `assets/avatars/`.
- Tokens: `illustration/line` #0A0A0A, `illustration/fill` #FFFFFF, `illustration/tint` #EBEBEB.

## 7. Section captions on "02 Components" (usage rules for the core components, verbatim)
- **Icons (5:2)**: “HugeIcons stroke-rounded (MIT) · 24pt grid · 1.5 stroke · colour bound to color/icon/primary. Apple (filled), the 4-colour Google G and the WhatsApp logo (share sheet only) are brand exceptions.”
- **Illustrations & Art (7:2)**: “Open Peeps + Open Doodles by Pablo Stanley (CC0). Recoloured to the illustration tokens: line #0A0A0A, fill white, tint #EBEBEB. Compose scenes from these components — never draw new people.”
- **Brand (8:7)**: “Geometric “P” monogram, white on a black squircle (22.37% radius, 60% corner smoothing). Wordmark: Manrope ExtraBold, −3% tracking.”
- **Buttons (9:9)**: “Pill buttons: Large 52pt (one primary per screen) and Small 36pt (in rows and cards). Primary = black. Secondary = gray on white. On Card = white on #F5F5F5 cards. Destructive = red, only for delete and sign out.”
- **Badges & Avatars (11:19)**: “Badges are 24pt pills in Caption/1 Bold. Red Overdue is the only coloured badge. Avatars use Open Peeps heads on #F5F5F5, with initials or an icon as fallbacks.”
- **Controls (12:215)**: “Page dots for onboarding, a segmented control for filters (Groups | Friends), and input fields on #F5F5F5 with a black focus ring. Errors use the red accent.”
- **Cards & Rows (13:220)**: “Cards are #F5F5F5, 20pt radius, no borders, no shadows. On a card, nested pills and avatars switch to white. Owed amounts are bold black (+₹), amounts you owe are gray (−₹), and only overdue uses red.”
- **Navigation (17:458)**: “Floating Liquid Glass tab bar: Home · Groups · ＋ · Activity · Profile. The active tab gets a soft 6% pill, inactive tabs are gray. The Home header pairs the logo with a glass bell (black unread dot).”
- **Sheets (17:620)**: “Floating sheet inset 8pt, 40pt corners, white, on a 40% scrim. The grabber and glass close button are Apple iOS 27 kit instances. Rows: 44pt icon tile, title and subtitle, chevron.”
- **Sign-in & Setup (36:590)**: “Components for sign in and first-run setup: step header with progress, 6-digit code input, avatar picker option, currency row and payment preview.”

## 8. Interaction / motion facts for the core components
- **No prototype reactions** exist on any node in the Icons, Brand, Buttons, Badges & Avatars, Controls or Sign-in & Setup sections (checked all nodes). `get_motion_context` returns **no animations** for these sections.
- Figma states are static variants (`State=Default|Pressed|Disabled`, `Selected=True|False`, …). Implement pressed as the "Pressed" variant's colours while the finger is down. Figma defines no transition, so use an instant swap or a short (≤ 100 ms) fade. Text buttons' pressed state is **50 % opacity** of the whole button.
- The only transitions in the prototype are on screens (e.g. Get Started buttons: ON_CLICK → PUSH, 350 ms, ease-in-and-out). Those are documented by the screen specs.
