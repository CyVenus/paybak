# Icons: `assets/icons/`

Source: Figma page "02 Components" (3:3). The section is **Icons** (5:2). Three more icons (Shuffle, Lock, Help) live in the section **Shared (from Profile)** (90:667) and are included too.
Every file is the **verbatim** output of `node.exportAsync({format:'SVG_STRING'})` on the icon component. Each file's length and djb2 hash were checked against the value computed inside Figma: **65/65 match**.

## Common facts (all icons unless noted)
- **Box**: 24 × 24 (`viewBox="0 0 24 24"`). Every icon component is 24 × 24; no icon has a different size.
- **Style**: HugeIcons **stroke-rounded** (MIT). Monochrome **stroke** paths, `fill="none"`, `stroke="#0A0A0A"`, `stroke-width="1.5"`, mostly `stroke-linecap="round"` / `stroke-linejoin="round"` (a few sub-paths use butt caps or miter joins exactly as HugeIcons draws them; keep the files as they are).
- **Colour**: bound in Figma to `color/icon/primary` = **#0A0A0A**. **Tint at runtime** (template image / `tint`/`ColorFilter`). Colour tokens used on icons: `icon/primary` #0A0A0A · `icon/secondary` #6B6B6B · `icon/tertiary` #A3A3A3 · `icon/inverse` #FFFFFF · `icon/destructive` #C93636.
- **Drawn sizes** (Foundations rule): 24 pt default, **20 pt in tiles and inside Large buttons/fields**, **16 pt in Small buttons/cards/badges**, 14 pt in 24-pt avatars, badges and radio checks. In Figma the stroke scales with the icon (24 → 1.5, 20 → 1.25, 16 → 1.0, 14 → 0.875). You get this automatically by scaling the 24 × 24 SVG. **Don't re-stroke at 1.5.**
- **Exception, the tab-bar ＋ (Button / Add 10:87)**: that instance uses **stroke 2.0** at 24 pt, in white. Use `plus.svg` with `stroke-width` 2 (identical to `assets/images/add-button-plus.svg` made for the Home spec).
- **Variants**: **none**. No icon is a component set and there are **no filled/outline pairs**. Tab-bar icons use the same stroke icon for active and inactive; only the tint changes (active `icon/primary` #0A0A0A, inactive `icon/secondary` #6B6B6B). Verified on Navigation / Tab Bar Item 17:504.
- **Brand exceptions** (never tint):
  - `apple.svg`: **filled** Apple logo, fill #0A0A0A. On the black Primary button it is overridden to **white** (`icon/inverse`). So tinting Apple is allowed and expected (black or white only).
  - `google.svg`: official 4-colour Google "G" (#4285F4, #34A853, #FBBC05, #EB4335). **Never tint.**
  - `whatsapp.svg`: official WhatsApp logo (green linear gradient #1FAF38→#60D669 plus white). Share-sheet mock only and never tinted. Not needed in this build (share sheet is out of scope and is drawn by the system).
- SwiftUI names in the Figma descriptions are `PBIcon.<camelCase>` (e.g. `PBIcon.chevronLeft`). Android suggestion: `R.drawable.ic_<snake_case>` (e.g. `ic_chevron_left`).
- Android VectorDrawable conversion: all files are plain paths (WhatsApp uses gradients, which need API 24+ `aapt:attr` gradients; minSdk is 24, so that's fine).

## Index (component → file)
| Component (node id) | File | HugeIcons source | Box | Stroke | Colour | Notes / where used in this build |
|---|---|---|---|---|---|---|
| Icon / Home (5:7) | `home.svg` | home-01 | 24 | 1.5 | #0A0A0A | Tab bar |
| Icon / Groups (5:10) | `groups.svg` | user-group | 24 | 1.5 | #0A0A0A | Tab bar; default Icon of Avatar / Circle (Type=Icon, e.g. "Goa Trip" row) |
| Icon / Plus (5:13) | `plus.svg` | plus-sign | 24 | 1.5 (2.0 in Button / Add) | #0A0A0A | Default leading icon of all pill buttons; tab-bar ＋ |
| Icon / Activity (5:18) | `activity.svg` | clock-01 | 24 | 1.5 | #0A0A0A | Tab bar |
| Icon / Profile (5:23) | `profile.svg` | user-circle | 24 | 1.5 | #0A0A0A | Tab bar |
| Icon / Bell (5:26) | `bell.svg` | notification-01 | 24 | 1.5 | #0A0A0A | Home header glass button; default Icon of Button / Icon |
| Icon / Chevron Right (5:29) | `chevron-right.svg` | arrow-right-01 | 24 | 1.5 | #0A0A0A | Row chevrons; Button / Text trailing chevron (16 pt) |
| Icon / Chevron Left (5:32) | `chevron-left.svg` | arrow-left-01 | 24 | 1.5 | #0A0A0A | Back button (Onboarding top bar, Setup header) |
| Icon / Settings (5:37) | `settings.svg` | settings-01 | 24 | 1.5 | #0A0A0A | |
| Icon / Mail (5:42) | `mail.svg` | mail-01 | 24 | 1.5 | #0A0A0A | Default leading icon of Control / Input Field (20 pt, `icon/secondary`) |
| Icon / Receipt (5:45) | `receipt.svg` | invoice-01 | 24 | 1.5 | #0A0A0A | Add sheet "Add expense" |
| Icon / Food (5:50) | `food.svg` | restaurant-01 | 24 | 1.5 | #0A0A0A | Home activity rows |
| Icon / Bolt (5:53) | `bolt.svg` | flash | 24 | 1.5 | #0A0A0A | Home rows |
| Icon / Wallet (5:59) | `wallet.svg` | wallet-01 | 24 | 1.5 | #0A0A0A | |
| Icon / Money In (5:65) | `money-in.svg` | money-receive-01 | 24 | 1.5 | #0A0A0A | Home payment rows |
| Icon / Money Out (5:71) | `money-out.svg` | money-send-01 | 24 | 1.5 | #0A0A0A | Home payment rows |
| Icon / Exchange (5:77) | `exchange.svg` | money-exchange-01 | 24 | 1.5 | #0A0A0A | Add sheet "Record payment" |
| Icon / Lend (5:83) | `lend.svg` | hand-coins | 24 | 1.5 | #0A0A0A | Add sheet "Lend money" |
| Icon / Calendar (5:88) | `calendar.svg` | calendar-03 | 24 | 1.5 | #0A0A0A | Default icon of Badge / Pill (14 pt) |
| Icon / Check (5:91) | `check.svg` | tick-02 | 24 | 1.5 | #0A0A0A | Row / Currency radio check (14 pt, white) |
| Icon / Check Circle (5:96) | `check-circle.svg` | checkmark-circle-02 | 24 | 1.5 | #0A0A0A | Toasts |
| Icon / Close (5:99) | `close.svg` | cancel-01 | 24 | 1.5 | #0A0A0A | Sheet close (Android) |
| Icon / Alert (5:104) | `alert.svg` | alert-circle | 24 | 1.5 | #0A0A0A | |
| Icon / User Add (5:109) | `user-add.svg` | user-add-01 | 24 | 1.5 | #0A0A0A | "Invite friends" |
| Icon / People (5:112) | `people.svg` | user-multiple | 24 | 1.5 | #0A0A0A | |
| Icon / Apple (5:115) | `apple.svg` (copy: `../brand/apple-logo.svg`) | Apple logo, **filled** | 24 | — (fill) | #0A0A0A fill; white on the black button | Get Started "Continue with Apple" (20 pt, white) |
| Icon / Google (5:121) | `google.svg` (copy: `../brand/google-g.svg`) | Google "G", 4 colours | 24 | — (fill) | #4285F4 / #34A853 / #FBBC05 / #EB4335, **never tint** | Get Started "Continue with Google" (20 pt) |
| Icon / Search (34:590) | `search.svg` | search-01 | 24 | 1.5 | #0A0A0A | Setup 2 currency search (20 pt, `icon/secondary`) |
| Icon / Camera (34:597) | `camera.svg` | camera-01 | 24 | 1.5 | #0A0A0A | Control / Avatar Option, Type=Upload |
| Icon / Copy (34:603) | `copy.svg` | copy-01 | 24 | 1.5 | #0A0A0A | Setup 3 payment preview copy button |
| Icon / Car (81:671) | `car.svg` | car-01 | 24 | 1.5 | #0A0A0A | |
| Icon / Bed (81:678) | `bed.svg` | bed-double | 24 | 1.5 | #0A0A0A | |
| Icon / Ticket (81:684) | `ticket.svg` | ticket-01 | 24 | 1.5 | #0A0A0A | |
| Icon / Shopping Bag (81:690) | `shopping-bag.svg` | shopping-bag-01 | 24 | 1.5 | #0A0A0A | |
| Icon / Tag (81:697) | `tag.svg` | tag-01 | 24 | 1.5 | #0A0A0A | |
| Icon / Split (81:703) | `split.svg` | divide-sign | 24 | 1.5 | #0A0A0A | |
| Icon / Note (81:707) | `note.svg` | note-edit | 24 | 1.5 | #0A0A0A | |
| Icon / Arrow Right (81:711) | `arrow-right.svg` | arrow-right-02 | 24 | 1.5 | #0A0A0A | |
| Icon / Arrow Up (81:715) | `arrow-up.svg` | arrow-up-02 | 24 | 1.5 | #0A0A0A | |
| Icon / Sparkles (81:719) | `sparkles.svg` | sparkles | 24 | 1.5 | #0A0A0A | Home |
| Icon / Plane (81:723) | `plane.svg` | airplane-01 | 24 | 1.5 | #0A0A0A | |
| Icon / Drone (81:727) | `drone.svg` | drone | 24 | 1.5 | #0A0A0A | |
| Icon / Package (81:731) | `package.svg` | package | 24 | 1.5 | #0A0A0A | |
| Icon / QR Code (81:739) | `qr-code.svg` | qr-code | 24 | 1.5 | #0A0A0A | |
| Icon / Scan (81:743) | `scan.svg` | qr-code-scan | 24 | 1.5 | #0A0A0A | |
| Icon / Link (81:749) | `link.svg` | link-01 | 24 | 1.5 | #0A0A0A | |
| Icon / Share (82:669) | `share.svg` | share-08 | 24 | 1.5 | #0A0A0A | |
| Icon / Logout (82:675) | `logout.svg` | logout-03 | 24 | 1.5 | #0A0A0A | |
| Icon / Repeat (82:679) | `repeat.svg` | repeat | 24 | 1.5 | #0A0A0A | |
| Icon / Flag (82:683) | `flag.svg` | flag-02 | 24 | 1.5 | #0A0A0A | |
| Icon / Delete (82:687) | `delete.svg` | delete-02 | 24 | 1.5 | #0A0A0A | |
| Icon / Restore (82:691) | `restore.svg` | delete-put-back | 24 | 1.5 | #0A0A0A | |
| Icon / Chart (82:697) | `chart.svg` | analytics-01 | 24 | 1.5 | #0A0A0A | |
| Icon / Mic (82:701) | `mic.svg` | mic-01 | 24 | 1.5 | #0A0A0A | |
| Icon / Image (82:708) | `image.svg` | image-01 | 24 | 1.5 | #0A0A0A | |
| Icon / Flame (82:712) | `flame.svg` | fire | 24 | 1.5 | #0A0A0A | |
| Icon / Wi-Fi (82:719) | `wi-fi.svg` | wifi-01 | 24 | 1.5 | #0A0A0A | |
| Icon / Crown (82:725) | `crown.svg` | crown | 24 | 1.5 | #0A0A0A | |
| Icon / Bank (82:731) | `bank.svg` | bank | 24 | 1.5 | #0A0A0A | |
| Icon / Download (82:735) | `download.svg` | download-04 | 24 | 1.5 | #0A0A0A | |
| Icon / Star (82:739) | `star.svg` | star | 24 | 1.5 | #0A0A0A | |
| Icon / WhatsApp (82:745) | `whatsapp.svg` | Iconify logos:whatsapp-icon | 24 | — (fill, gradient) | official green + white, **never tint** | Share sheet mock only (out of scope) |
| Icon / Shuffle (64:3957) | `shuffle.svg` | shuffle | 24 | 1.5 | #0A0A0A | (Shared from Profile) |
| Icon / Lock (64:3963) | `lock.svg` | square-lock-02 | 24 | 1.5 | #0A0A0A | (Shared from Profile) |
| Icon / Help (64:3969) | `help.svg` | help-circle | 24 | 1.5 | #0A0A0A | (Shared from Profile) |

Section caption in Figma (verbatim): "HugeIcons stroke-rounded (MIT) · 24pt grid · 1.5 stroke · colour bound to color/icon/primary. Apple (filled), the 4-colour Google G and the WhatsApp logo (share sheet only) are brand exceptions."
