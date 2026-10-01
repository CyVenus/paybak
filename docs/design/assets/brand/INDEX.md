# Brand assets: `assets/brand/`

Figma: page "02 Components" → section **Brand** (8:7). Caption (verbatim): "Geometric “P” monogram, white on a black squircle (22.37% radius, 60% corner smoothing). Wordmark: Manrope ExtraBold, −3% tracking."

## Files
| File | What | Source / how it was made |
|---|---|---|
| `app-mark.svg` | **The mark on its own, with true 60 % corner smoothing** (160 × 160 viewBox; scale freely). **Use this one when you need a file.** | Derived: Figma's smoothed-squircle geometry (radius 35.792 = 22.37 %, smoothing 0.6) computed with the published Figma squircle algorithm, plus the exact glyph path from Figma. Checked against Figma's own 1024 px render: 71 of 1,048,576 pixels differ by more than 64/255 (antialiasing only). |
| `app-mark-1024.png` | 1024 × 1024 RGBA PNG of the mark, transparent outside the squircle | Figma `exportAsync({format:'PNG', constraint:{type:'WIDTH', value:1024}})` of `Brand / App Mark` Size=160 (8:10), moved as base64 and hash-checked in 20 chunks. 21,210 bytes. |
| `app-mark-160.svg`, `app-mark-96.svg`, `app-mark-40.svg`, `app-mark-28.svg` | Verbatim Figma SVG exports of each Size variant (8:10, 8:13, 8:16, 8:19) | `exportAsync SVG_STRING`, hash-checked. **Figma's SVG export drops corner smoothing**: these use a plain `<rect rx>` (a circular corner). The difference is at most about 1 pt at 96 pt and invisible at 28 pt. Prefer `app-mark.svg` or native drawing. |
| `p-glyph.svg` | The white "P" glyph only (Figma vector node 8:12 from the 160 mark), 72 × 103 export box | `exportAsync SVG_STRING` of the glyph VECTOR (a single stroked path, **not** outlined text) |
| `p-glyph-160.svg` | Same glyph on a transparent 160 × 160 canvas at its exact position in the mark | Derived from `app-mark-160.svg` by removing the background rect. For icon foregrounds (e.g. an Android adaptive-icon foreground) |
| `app-icon-fullbleed.svg` | 1024 × 1024 **square** (no rounding) #0A0A0A background + glyph at the mark's proportions | Derived. Source for the **iOS app icon** (iOS applies its own squircle mask) and the Android adaptive-icon background/foreground. |
| `app-icon-1024.png` | **iOS app icon, ready to drop into `AppIcon.appiconset`**: 1024 × 1024, 8-bit **RGB, no alpha** (App Store requirement), square full-bleed #0A0A0A + white glyph | Derived by the editor: `app-icon-fullbleed.svg` rasterised with @napi-rs/canvas and written as an opaque RGB PNG (build-session script, not kept). iOS applies the squircle mask. |
| `logo-horizontal.svg` | Brand / Logo, Layout=Horizontal (8:23), 104 × 28 | Verbatim Figma export; **wordmark outlined to paths by the SVG exporter** |
| `logo-stacked.svg` | Brand / Logo, Layout=Stacked (8:27), exported box 136 × 159 (node is 135 × 156; the "y" descender extends the export box) | Verbatim Figma export; wordmark outlined |
| `google-g.svg` | Official 4-colour Google "G" (Icon / Google 5:121), 24 × 24 | Copy of `../icons/google.svg`. **Never recolour** |
| `apple-logo.svg` | Apple logo, filled (Icon / Apple 5:115), 24 × 24, fill #0A0A0A | Copy of `../icons/apple.svg`. Tinted **white** on the black "Continue with Apple" button |

## Brand / App Mark (8:22): exact geometry
Variants: `Size = 160 | 96 | 40 | 28`. Figma description: "Paybak app mark. Sizes: 160 (cover), 96 (splash), 40, 28 (header lockup). SwiftUI: PBAppMark(size:)".

- Tile: fill `color/bg/inverse` **#0A0A0A**, corner radius **0.2237 × S**, **corner smoothing 60 %**, clips content, no stroke, no shadow.
- Glyph: a single **stroked** open path (not a font glyph): stroke `color/icon/inverse` **#FFFFFF**, **round caps, round joins**, stroke width **0.109375 × S**.
- Glyph centre-line in unit coordinates (multiply by S):
  1. Move to (0.34375, 0.765625): bottom of the stem
  2. Line to (0.34375, 0.234375): top-left corner
  3. Line to (0.5078125, 0.234375): start of the bowl
  4. **Clockwise half-circle** centred at (0.5078125, 0.40625), radius 0.171875, from the top point to the bottom point (0.5078125, 0.578125). Figma stores it as two cubic Béziers; a true arc is visually identical.
  5. Line to (0.34375, 0.578125): end of the bowl, back at the stem
- Per size (from Figma):

| Size S | Corner radius | Glyph vector bbox (x, y, w × h), centre-line | Stroke |
|---|---|---|---|
| 160 | 35.792 | (55, 37.5), 53.75 × 85 | 17.5 |
| 96 | 21.4752 | (33, 22.5), 32.25 × 51 | 10.5 |
| 40 | 8.948 | (13.75, 9.375), 13.44 × 21.25 | 4.375 |
| 28 | 6.2636 | (9.625, 6.5625), 9.41 × 14.88 | 3.0625 |

- Native drawing (recommended, keeps it sharp and animatable for the Splash):
  - SwiftUI: `RoundedRectangle(cornerRadius: 0.2237 * S, style: .continuous)` (Apple's continuous corner ≈ Figma 60 % smoothing) filled #0A0A0A, plus a `Path` with the points above, `.stroke(.white, style: StrokeStyle(lineWidth: 0.109375 * S, lineCap: .round, lineJoin: .round))`.
  - Compose: draw the squircle from `app-mark.svg`'s path (scaled by S/160) or use `RoundedCornerShape(22.37 %)`, which is circular and within 1 pt of the design. Glyph via `Path` + `drawPath(style = Stroke(width = 0.109375f * S, cap = StrokeCap.Round, join = StrokeJoin.Round))`.
- App icons: iOS 1024 icon = `app-icon-fullbleed.svg` rendered to PNG (square, iOS masks the corners). Android adaptive icon: background = solid #0A0A0A; foreground = `p-glyph-160.svg` scaled so the 160 canvas maps to the 108 dp foreground layer's inner 72 dp safe zone (glyph ≈ 32 × 46 dp, slightly right of centre, exactly as in the mark). Monochrome/themed icon: same foreground.

## Brand / Logo (8:31): how the lockup is built
Variants: `Layout = Horizontal | Stacked`. Description: "Mark + wordmark lockup. Horizontal: Home header. Stacked: Splash. SwiftUI: PBLogo(layout:)".
**The wordmark "Paybak" is a live TEXT layer, not outlined vectors**: characters `Paybak`, Manrope **ExtraBold (800)**, text colour `color/text/primary` #0A0A0A, text style `Brand/Wordmark S` or `Brand/Wordmark L`. (Only the SVG exporter outlines it; in the app render real text with the bundled `Manrope-ExtraBold`.)

| | Horizontal (8:23) | Stacked (8:27) |
|---|---|---|
| Auto-layout | horizontal, hug × hug, align items **center** (cross axis), padding 0 | vertical, hug × hug, align items **center** (horizontal centring), padding 0 |
| Gap | **8** (`space/8`) | **16** (`space/16`) |
| Mark | Brand / App Mark **Size=28**, at (0, 0) | Brand / App Mark **Size=96**, at (19.5, 0) (centred over the text) |
| Wordmark | `Brand/Wordmark S`: 20 / 24, letter spacing −3 % = **−0.6 pt**; box 68 × 24 at (36, 2) (vertically centred on the 28 mark) | `Brand/Wordmark L`: 40 / 44, −3 % = **−1.2 pt**; box 135 × 44 at (0, 112) |
| Total size | **104 × 28** | **135 × 156** |
| Used in | Get Started (logo at top), Home header toolbar (long-press = debug menu) | Splash (animated natively) |
