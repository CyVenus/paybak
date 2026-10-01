# Avatar art: `assets/avatars/`

Static line-art heads (Open Peeps by Pablo Stanley, CC0, recoloured to the illustration tokens: line #0A0A0A, fill white). They are **not** Rive. Source: `Art / Peep Head / *` components in "02 Components" → **Illustrations & Art** (7:2). Each component is **120 × 120, clips content, transparent background**. The Open Peeps bust (144 × ~194) is offset inside it and cropped by the 120 box.

| File (SVG + PNG @3x) | Component (node) | Open Peeps id | Preset index (flow.md "avatar choice") | Where it's used |
|---|---|---|---|---|
| `avatar-1.svg`, `avatar-1@3x.png` | Art / Peep Head / **Arjun** (7:5) | peep-43 | **0** (selected by default) | Setup 1 option 1 (`Control / Avatar Option` Art, Selected); default Art of `Avatar / Circle` and `Control / Avatar Option`; Setup 3 "What friends see" preview (Avatar / Circle 40, Art); Avatar / Stack slot 1; debug sample profile (avatar 0) |
| `avatar-2.svg`, `avatar-2@3x.png` | Art / Peep Head / **Priya** (7:22) | peep-93 | **1** | Setup 1 option 2; **Home — Active: "Recent activity" Row / Activity avatar (Avatar / Circle Size=40, Art)**; Avatar / Stack slot 2 |
| `avatar-3.svg`, `avatar-3@3x.png` | Art / Peep Head / **Rohan** (7:37) | peep-21 | **2** | Setup 1 option 3; **Home — Active: "Rohan — overdue" attention row (Avatar / Circle Size=40, Art)**, also in Home — ＋ Action sheet backdrop; Avatar / Stack slot 3 |
| `avatar-4.svg`, `avatar-4@3x.png` | Art / Peep Head / **Esha** (7:52) | peep-96 | **3** | Setup 1 option 4; Avatar / Stack slot 4 |
| `avatar-5.svg`, `avatar-5@3x.png` | Art / Peep Head / **Dev** (7:67) | peep-73 | **4** | Setup 1 option 5 |
| `avatar-6.svg`, `avatar-6@3x.png` | Art / Peep Head / **Kabir** (84:667) | peep-5 | (not a preset) | Not used on any screen in this build (Figma: other people in groups). Included for completeness |
| `avatar-7.svg`, `avatar-7@3x.png` | Art / Peep Head / **Meera** (84:669) | peep-27 | (not a preset) | Not used on any screen in this build. Included for completeness |

Figma descriptions (e.g. 7:5): "Open Peeps bust (peep-43) cropped for avatars. Used inside Avatar / Circle. SwiftUI: PBPeepHead.arjun" (the others follow the same pattern: `PBPeepHead.priya`, `.rohan`, `.esha`, `.dev`, `.kabir`, `.meera`). Android suggestion: `R.drawable.avatar_1` … `avatar_7`.

## How to draw them (exactly like Figma)
- The head is scaled **uniformly** so its 120 × 120 box equals the circle's diameter, placed at the circle's origin, and **clipped to the circle**. There is no extra inset. The background colour comes from the container, **not** the art (the art is transparent):
  - `Avatar / Circle` Type=Art (24/32/40/56): circle fill `color/bg/card` #F5F5F5, art S × S.
  - `Control / Avatar Option` (Setup 1): 46 × 46 inner circle, fill `color/bg/card` #F5F5F5, art 46 × 46 (the 56 × 56 outer ring is separate; see components-core.md).
  - Setup 3 preview card: the avatar sits on a white circle (`color/bg/primary`). Transparency is why this works.
  - Home `Row / Attention` cards (the "Rohan — overdue" row): white circle (`color/bg/primary`), verified in Figma. Home `Row / Activity` on white ("Priya paid you"): `color/bg/card` #F5F5F5. Rule: `bg/card` on white surfaces, `bg/primary` inside #F5F5F5 cards.
- `assets/images/peep-head-rohan.svg` and `peep-head-priya.svg` (exported for the Home spec) are the same art as `avatar-3.svg` and `avatar-2.svg`. **Ship one set: use `avatar-1…7`.**
- The line colour (#0A0A0A) and white fills are baked into the art; **don't tint**.
- Custom photo (Setup 1 camera option) replaces the art: fill the same circle with the photo, aspect-fill, clipped. Initials fallback: `Avatar / Circle` Type=Initials.

## How the files were made / verified
- **SVG**: `avatar-1` … `avatar-5` are clean exports of the components (120 × 120, `<g clip-path>` wrapper, no background). `avatar-6`/`avatar-7` are the Figma MCP export of 84:667/84:669 with the section's canvas rect and section-outline paths removed; the art content is unchanged. **All seven were rasterized and compared with Figma's own isolated render (`get_screenshot`, contentsOnly) of the same component**: mean channel difference 0.6–1.1/255, at most 3 pixels over 64/255 (antialiasing). Corners are fully transparent in both.
- **PNG @3x**: 360 × 360 RGBA (transparent), rendered from the verified SVGs with Skia (`@napi-rs/canvas`) at 3×. Figma's MCP raster export includes the section background, so it wasn't usable. For @1x/@2x, scale the SVG (preferred) or the @3x PNG.
- Largest on-screen use is 56 pt (`size/avatar-lg`), i.e. 168 px @3x, so the 360 px PNGs have plenty of headroom.
