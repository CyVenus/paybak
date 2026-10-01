# Paybak: Profile and avatar editor spec (Figma page "05 Profile" 53:892)

Sections: **Customize Avatar** (53:893), **Avatar Parts** (53:896) and **Overlay helpers** (216:24574, prototype-only).

| Screen id | Figma frame (node) | Container | 2× reference (1× crop) | Node JSON (`.figma-cache/`) |
|---|---|---|---|---|
| `profile` | Profile (64:4316) | **tab root** (Profile tab of the glass tab bar) | `ref/profile.png` (`ref/profile_1x.png`) | `nodes/53-892.json` |
| `editAvatarBoyHair` | Edit avatar — Boy · Hair (64:4503) | **pushed screen** from Profile, no tab bar | `ref/editAvatarBoyHair.png` (`ref/editAvatarBoyHair_1x.png`) | `nodes/53-892.json` |
| `editAvatarBoyBeard` | Edit avatar — Boy · Beard (64:4963) | same screen, state | `ref/editAvatarBoyBeard.png` (`ref/editAvatarBoyBeard_1x.png`) | `nodes/53-892.json` |
| `editAvatarBoyEyewear` | Edit avatar — Boy · Eyewear (64:5384) | same screen, state | `ref/editAvatarBoyEyewear.png` (`ref/editAvatarBoyEyewear_1x.png`) | `nodes/53-892.json` |
| `editAvatarBoyOutfit` | Edit avatar — Boy · Outfit (64:5931) | same screen, state | `ref/editAvatarBoyOutfit.png` (`ref/editAvatarBoyOutfit_1x.png`) | `nodes/53-892.json` |
| `editAvatarGirlHair` | Edit avatar — Girl · Hair (64:6721) | same screen, state | `ref/editAvatarGirlHair.png` (`ref/editAvatarGirlHair_1x.png`) | `nodes/53-892.json` |
| `editAvatarGirlAccessory` | Edit avatar — Girl · Accessory (64:7535) | same screen, state | `ref/editAvatarGirlAccessory.png` (`ref/editAvatarGirlAccessory_1x.png`) | `nodes/53-892.json` |
| `editAvatarGirlOutfit` | Edit avatar — Girl · Outfit (64:8052) | same screen, state | `ref/editAvatarGirlOutfit.png` (`ref/editAvatarGirlOutfit_1x.png`) | `nodes/53-892.json` |
| `editAvatarDiscard` | Discard alert (64:8823) | **alert** over the editor (Boy · Outfit state) | `ref/editAvatarDiscard.png` (`ref/editAvatarDiscard_1x.png`) | `nodes/53-892.json` |

The seven "Edit avatar — …" frames are **one screen** (`EditAvatarScreen`) in seven states. It is written once in §3, and §3.7 lists the differences per state.

**Skipped: prototype-only copy.** Section **"Overlay helpers" (216:24574)** holds **"↳ Add sheet (overlay)" (216:24577)**, which is not a screen. It's a copy of the Home Add sheet (`screens-home.md` §5, Overlay — Add sheet 24:808 / `Sheet / Action Sheet` 17:643) that the Profile ＋ opens as an overlay, so Profile stays visible under the scrim.
- Its hotspots: scrim → CLOSE; ✕ → CLOSE; the four rows → page 06 by URL:
  - Add expense → "Add expense — Empty" 176:17454.
  - Record payment → "Record payment — form" 177:29750.
  - Lend money (IOU) → "Lend money — form" 185:25810.
  - New group → "New group — Group" 190:8356.
- Notes (verbatim):
  - section "Overlay helpers": "Prototype-only copy of the Phase 1 ＋ sheet (page 04), so Open overlay works on this page. It isn’t a screen."
  - caption: "Prototype helper, not a screen: a copy of Overlay — Add sheet 24:808 (page 04). ＋ on Profile opens it with Open overlay, so Profile stays visible under the scrim. Add expense, Record payment, Lend money (IOU) and New group jump to their page 06 flows; the scrim and ✕ close it."

Related specs:
- `components-core.md`: buttons, badge, avatars, segmented control, divider.
- `components-home.md`: Nav Header, Button / Icon, tab bar, materials.
- `tokens.md`, `foundations-rules.md`.
- The "Shared (from Profile)" components (Row / Setting, Navigation / Push Header, Control / Category Chip, Overlay / Alert) moved to "02 Components". Their used variants are described in full in §5.4, so this file stands alone.
- **Avatar assets:** `assets/avatar-parts/manifest.json` and `assets/avatar-parts/{boy,girl}/…` (§1 and §8).

---

## 0. Conventions

- Frame = 402 × 874 pt (iPhone 17 Pro). All `x, y` are **frame coordinates** unless marked "(component-relative)". The top safe area is 62 and the bottom is 34 (the home indicator starts at y 840). The status bar (kit, 402 × 62), the home indicator (kit, 402 × 34) and the invisible `… hotspot` frames are **not drawn**.
- Colours are `tokens.md` names (`color/` dropped) with hex. Text styles are the `tokens.md` names (Manrope).
- Text is verbatim. Curly ’ (U+2019) appears in "won’t", "character’s" and "today’s". The ₹ is U+20B9.
- Icons: `assets/icons/<name>.svg` (24 × 24, stroke 1.5, #0A0A0A). Tint them and scale the whole icon (20 pt → stroke 1.25).
- **Where the values come from:**
  - Read-only Plugin API dumps of the node trees: auto-layout, bound variables → token names, text styles, instance properties incl. nested avatar swaps, and reactions (not kept).
  - The REST node JSON of the page (`.figma-cache/nodes/53-892.json`, regenerate with `tools/fetch_figma.py`).
  - **`get_design_context` was not run.** The Figma MCP allowance was exhausted, so the REST data was used instead. The dumps and REST JSON carry everything it would add.
- **References:**
  - `ref/<id>.png` are the REST 2× renders (804 × 1748).
  - `ref/<id>_1x.png` are 1× crops of an MCP screenshot of the whole section.
- No `get_motion_context` animation exists here. The only motion is the prototype transitions quoted in each Navigation table.

---

## 1. The avatar system (read this first)

### 1.1 There is no `avatar.riv`
The Figma notes say "Rive file `avatar.riv` drives the same parts (ViewModel `Avatar`)" and "Save commits the look; Rive avatar.riv renders it in the app". **That file doesn't exist** (flow.md: the six .riv files are all the Rive assets). **So the app composes the avatar natively:**
- It stacks full-canvas SVG part layers, exported from Figma, in the fixed layer order of the `Avatar / Character` components.
- The canvas is the **772 × 842 rig space**. The Figma section note says every part is "a 772 × 842 component in Rive rig space (1:1 with the Rive `avatar` artboard)".
- **None of the six existing .riv files contains this character.** The onboarding people, the Get Started trio, the notifications person + bell, the All-set trio + check, the First-day lying character and the All-square meditating character are all different art.
- The Figma descriptions name the view types: `PBAvatarView(gender: .male/.female)`, `PBAvatarStage` and `PBAvatarPartTile`. Use those names on iOS and matching Composables on Android.

### 1.2 Rig and layer order
Each gender is one component with fixed layers. Every layer is a 772 × 842 instance at (0, 0).
- The swappable layers are **instance-swap properties**.
- The base layers are fixed: body, shadow, face and nose.

Draw the layers **bottom → top** in this order (read from the component children):

| # | Boy: `Avatar / Character / Boy` (61:100) | Source | Girl: `Avatar / Character / Girl` (61:145) | Source |
|---|---|---|---|---|
| 1 | `outfit-back` | **Outfit (back)** swap (61:6) | `hair-back` | **Hair (back)** swap (61:8) |
| 2 | `body` | Base / Body (fixed) | `outfit-back` | **Outfit (back)** swap (61:14) |
| 3 | `outfit-front` | **Outfit** swap (61:5) | `body` | Base / Body |
| 4 | `shadow` | Base / Shadow | `outfit-front` | **Outfit** swap (61:13) |
| 5 | `face` | Base / Face | `shadow` | Base / Shadow |
| 6 | `beard` | **Beard** swap (61:1) | `face` | Base / Face |
| 7 | `hair-front` | **Hair** swap (61:0) | `hair-front` | **Hair** swap (61:7) |
| 8 | `mouth` | **Mouth** swap (61:4) | `accessory` | **Accessory** swap (61:9) |
| 9 | `nose` | Base / Nose | `mouth` | **Mouth** swap (61:12) |
| 10 | `eyes` | **Eyes** swap (61:3) | `nose` | Base / Nose |
| 11 | `eyewear` | **Eyewear** swap (61:2) | `eyes` | **Eyes** swap (61:11) |
| 12 | – | | `eyewear` | **Eyewear** swap (61:10) |

- The Boy has no hair-back layer. Man bun is drawn entirely in hair-front.
- Component descriptions (verbatim):
  - Boy: "Boy avatar. Swap Hair, Beard, Eyewear, Eyes, Mouth, Outfit. When changing Outfit also set Outfit (back) to the same style. Mirrors Rive avatar.riv ViewModel Avatar.male. SwiftUI: PBAvatarView(gender: .male)."
  - Girl: "Girl avatar. Swap Hair, Accessory, Eyewear, Eyes, Mouth, Outfit. When changing Hair/Outfit also set Hair (back)/Outfit (back) to the same style. Mirrors Rive avatar.riv ViewModel Avatar.female. SwiftUI: PBAvatarView(gender: .female)."
  - Set `Avatar / Character` (61:272, property `Gender` = Boy | Girl): "Customizable avatar. Gender switches between the Boy and Girl characters; each exposes its own part slots (instance swaps). Same layers and options as the Rive `avatar` artboard."
- **One user choice drives two layers.**
  - Outfit sets `outfit-back` and `outfit-front` together.
  - Girl Hair sets `hair-back` and `hair-front` together.
  - The editor never exposes "back" layers.

### 1.3 Categories and options (UI order = Figma parts-sheet order = editor tile order; ★ = default)
**Boy** (category chips in this order): Hair · Beard · Eyewear · Eyes · Mouth · Outfit

| Category | Options (id) in UI order | Default | "None" option |
|---|---|---|---|
| Hair | Curly (`curly`) ★, Quiff (`quiff`), Side part (`side-part`), Spiky (`spiky`), Man bun (`man-bun`), Crew cut (`crew-cut`) | curly | – |
| Beard (boy only) | None (`none`) ★, Stubble, Goatee, Full, Mustache, Handlebar | none | **None** = no beard layer |
| Eyewear | None (`none`), Round ★, Square, Rimless, Shades, Round shades | round | **None** "removes glasses" |
| Eyes | Dots ★, Happy, Wink, Squint, Wide, Sleepy | dots | – |
| Mouth | Smile ★, Grin, Neutral, Smirk, Open, Tongue | smile | – |
| Outfit | Hoodie ★, T-shirt, Polo, Sweater, Jacket, Shirt | hoodie | – |

**Girl** (category chips in this order): Hair · Accessory · Eyewear · Eyes · Mouth · Outfit

| Category | Options (id) in UI order | Default | "None" option |
|---|---|---|---|
| Hair (front + back) | Long wavy ★, Long straight, Bob, Top bun, Ponytail, Messy bun | long-wavy | – |
| Accessory (girl only) | None ★, Headband, Bow, Flower clip, Hair clips, Beanie | none | **None** = no accessory layer |
| Eyewear | None, Round ★, Square, Shades, Heart shades, Cat-eye | round | **None** |
| Eyes | Dots ★, Happy, Wink, Squint, Wide, Sleepy | dots | – |
| Mouth | Smile ★, Grin, Neutral, Smirk, Open, Tongue | smile | – |
| Outfit | T-shirt ★, Hoodie, Collar shirt, Sweater, Blazer, Striped tee | t-shirt | – |

- **Ids:** option ids are the kebab-case of the Figma option name. Display names are the Figma names verbatim, and they are the tile accessibility labels. (Tiles show no text.)
- **Defaults:** they come from the instance-swap defaults of 61:100 / 61:145 and the ★ in the parts sheets ("★ = default (today’s look)").
- **Empty layers:** these components are **empty in Figma** (0 children), so they have no file and draw nothing (`file`/`backFile` = null in the manifest):
  - Boy: Beard/None, Eyewear/None, and the Outfit Back of T-shirt, Polo, Sweater, Shirt and Jacket.
  - Girl: Accessory/None, Eyewear/None, and the Outfit Back of T-shirt, Collar shirt, Sweater, Blazer and Striped tee.
  - Only **Hoodie** has an outfit-back layer, in both genders. **Every Girl hair style has a hair-back layer.**
- **Special rule: "Buns show through the beanie by design."** Both bun styles draw the bun in the **hair-back** layer.
  - Top bun: hair-back reaches up to rig y 85. Messy bun: to y 76.
  - The Beanie accessory (layer 8) starts at y 112. So the bun sits behind everything and still pokes out above the beanie.
  - **Don't hide or mask anything** when Beanie is picked. The plain layer order produces exactly this (verified: `assets/avatar-parts/check/girl-topbun-beanie-full.png`, `girl-messybun-beanie-full.png`).

### 1.4 The part files (`assets/avatar-parts/`)
- **84 SVGs**, one per non-empty part component: 46 Boy components − 7 empty = 39, and 52 Girl − 7 empty = 45.
  - Path: `assets/avatar-parts/<gender>/<category>/<option>.svg`.
  - Categories: `base`, `hair`, `hair-back` (girl), `beard` (boy), `accessory` (girl), `eyewear`, `eyes`, `mouth`, `outfit`, `outfit-back`.
- **Every file is the full rig:**
  - `width="772" height="842" viewBox="0 0 772 842"`, no transforms, plain absolute `<path>`s only.
  - Stack them at the same origin and scale. **No per-part offsets.**
- **Colours are baked in. Never tint.**
  - Line #0A0A0A.
  - Fill white or #0A0A0A.
  - Tint #D9D8D9: shadow, outfit shading, tongue.
  - Hair highlight stroke #D6D5D6.
  - These greys are not tokens (the nearest is `illustration/tint` #EBEBEB). Keep the file colours.
- **Stroke widths** are rig units: 5–17, mostly 10. They scale with the drawing (0.2 × in a 120 circle → 2 pt).
- **Fill rules:** two files use `fill-rule="evenodd"`: `boy/beard/goatee.svg`, `boy/beard/full.svg`. Android VectorDrawable: `android:fillType="evenOdd"`.
- **Overflow:** some outfit and hair-back art runs past y 842 in Figma (e.g. Girl T-shirt to y 882). The files are clipped to the 842 viewBox, like the character frame. No crop used by the app reaches past y 842.
- **How they were made:**
  - Each non-empty part component (ids in `manifest.json` → `figmaNode` / `backFigmaNode`) was exported **1:1 with the Figma REST images API** (`format=svg`, `scale=1`) through the REST helper (now `tools/figma_rest.py`). Each export is the component frame itself, anchored at (0, 0).
  - The only edit: 12 exports that overflow the bottom (heights 843–883) got their root set to `height="842" viewBox="0 0 772 842"`.
  - The files contain plain paths only: no groups, transforms, clip paths, masks or filters.
  - **Earlier pass, now replaced.** The frames were first exported through MCP `download_assets` at a forced 0.476 scale and re-based with a scratch script. That pass gave the same renders. The REST 1:1 files replaced it because their coordinates are exact.
  - Stroke widths match the node data, e.g. face 10, Girl nose 8.8, eyewear 11.
  - The scripts that built the manifest (`make_manifest.py`) and the composites and checks (`compose_look.mjs`, `render_regions.mjs`, `sheetcmp.mjs`) were build-session scratch, not kept.
- **`manifest.json`** is the machine-readable source for the app:
  - per gender: `categories` in UI order, each option `{id, name, file, backFile?, figmaNode, none?}`, `default`, `hasNone`, `tileCrop`;
  - `base` files; `layers` (bottom → top, each layer names its base file or category + `file`/`backFile`); `defaults`;
  - global `crops` and `rules`.
  - Bundle it and drive the editor from it, so option order and ids live in one place.
- **Platform import:**
  - **iOS:** add the SVGs to an asset catalog folder `Avatar/` (namespace the folder: `avatar/boy/hair/curly`) with **Preserve Vector Data** on and **Single Scale**. Draw with `Image(name).resizable().frame(width: 772*s, height: 842*s)` inside a clipped container.
  - **Android:** convert each SVG to a VectorDrawable, `res/drawable/avatar_<gender>_<category>_<option>.xml` (Android Studio › Vector Asset, or `vd-tool`), keeping `viewportWidth=772 viewportHeight=842`. Draw with `Image(painterResource(…), modifier = Modifier.requiredSize((772*s).dp, (842*s).dp))`.
  - The long Stubble path (≈ 12 KB) triggers the "VectorPath too long" lint warning. It still renders fine; suppress the warning for these files.

### 1.5 Crops (how the rig maps into a container)
A crop is a rectangle in rig space that is scaled to fill the container. Scale `s` = container size ÷ crop size. The rig origin is drawn at (−cropX·s, −cropY·s) inside the container, and the container **clips** its content (circle / r20 tile / r20 stage).

| Crop | Rig rect (x, y, w, h) | Used by | Worked numbers |
|---|---|---|---|
| **Head** | (72, 60, 600, 600) | avatar circles (the Profile 120 circle and every `Avatar / Circle` showing the user); editor tiles for Hair, Beard, Eyewear, Eyes, Mouth, Accessory | tile 112 → s **0.186667**, rig at (−13.44, −11.2), rig drawn 144.11 × 157.17. Circle 120 → s **0.2**, rig at (−14.4, −12), 154.4 × 168.4. Circle 40 → s 0.066667, rig at (−4.8, −4) |
| **Bust** | (61, 192, 650, 650) | editor tiles for Outfit | tile 112 → s **0.172308**, rig at (−10.51, −33.08), 133.02 × 145.08 |
| **Stage** | height 782 from rig y 60 to the rig bottom (842); centred horizontally | `Avatar / Stage` 362 × 300 | s = 300 / 782 = **0.383632**, rig at (32.92, −23.02), 296.16 × 323.02. The rig bottom sits exactly on the stage bottom |

- All numbers are the Figma instance geometry. The UI-components note gives the same tile crops ("Crop: Head (box 600², ×0.1867) for face parts, Bust (box 650², ×0.1723) for outfits").
- **Conflict:** the note for the stage says "character box x0 y40 → x772 y680 at ×0.4689". The actual `Avatar / Stage` component (64:4189) and every screen instance use ×0.3836 with the rig bottom on the stage bottom. **Follow the instances** (they are what `ref/*.png` shows).

### 1.6 Composite check (my native layering vs Figma renders)
I composed the avatars from the exported part files with a small script (`compose_look.mjs`, `render_regions.mjs`, `sheetcmp.mjs`; Skia via `@napi-rs/canvas`; build-session scratch, not kept). It uses exactly the manifest's layer order and the crops above. I then diffed the results against Figma's own 2× renders. Differences are per-pixel max-channel |Δ| over the region (0–255):

| Check | Regions | Mean Δ | Pixels with Δ > 64 |
|---|---|---|---|
| Full characters vs `Avatar / Character / Boy` (61:100) and `/ Girl` (61:145), default looks, 1544 × 1684 | 2 | **0.12 / 0.11** | 0.01 % / 0.00 % |
| Every cell of the Figma parts sheets (62:243, 62:2148): all 72 options on the default look, Head crop and (Outfit) Bust crop, 320 × 320 | 72 | avg **2.49**, max 4.33 (Girl · Striped tee) | avg 1.2 %, max 2.4 % |
| The 7 editor states: stage (Stage crop, shuffle button masked) | 7 | 0.46–0.64 | ≤ 0.05 % |
| The 7 editor states: 42 option tiles (Head/Bust crop, selected rings drawn natively) | 42 | 1.2–3.6 | ≤ 1.0 % |
| Profile header circle (Head crop at s 0.2) | 1 | 3.61 | 2.1 % |

- A search over ±3 px shifts always found the best alignment at (0, 0). The remaining Δ is anti-aliasing along the 1–2 px outlines. Visually the renders are identical (the `…-figma-vs-native` images below).
- **This proves four things:**
  - the layer order (§1.2);
  - the front/back pairing: every Figma instance uses the same style for Outfit and Outfit (back), and for Hair and Hair (back);
  - the default looks;
  - the crop maths (§1.5).
- The beanie + bun rule needs no special case.
- Proof files in `assets/avatar-parts/check/` (not for shipping):
  - `boy-character-figma-vs-native.png`, `girl-character-figma-vs-native.png` (Figma left, native right).
  - `boy-parts-sheet-figma-vs-native.png`, `girl-parts-sheet-figma-vs-native.png`: each cell is Figma on the left, native on the right. That's all 72 options.
  - `<screenId>-figma-vs-native-1x.png` for `profile` and the 7 editor states: Figma | native | inverted diff.
  - `boy-options-sheet.png`, `girl-options-sheet.png`: native contact sheets of every option on the default look, labelled, ★ = default.
  - `boy-default-full.png`, `girl-default-full.png`, `girl-topbun-beanie-full.png`, `girl-messybun-beanie-full.png`: full 772 × 842 composites.

### 1.7 The user's avatar everywhere (data model; presets vs custom)
Figma shows the custom character in the Profile header. Every other page that shows "You" (Arjun) uses the Setup 1 preset head `assets/avatars/avatar-1.svg`, for example Record payment "From You" and the Setup 3 preview. So the app needs **one avatar value** with four kinds:

```
UserAvatar = none                      // initials fallback ("AM")
           | preset(index 0…4)         // Setup 1 line-art heads = assets/avatars/avatar-1…5
           | photo(file)               // Setup 1 camera option
           | character(AvatarLook)     // saved from Edit avatar
AvatarLook = { gender: boy|girl,
               boy:  {hair, beard, eyewear, eyes, mouth, outfit},        // option ids (§1.3)
               girl: {hair, accessory, eyewear, eyes, mouth, outfit} }   // both kept ("keeps each character’s picks")
```
- **Where it comes from:**
  - Setup 1 writes `none`, `preset(i)` or `photo`.
  - **Edit avatar → Save** writes `character(look)`. From then on the character *is* the user's avatar.
  - There is no other way back to a preset after onboarding. None is designed, so don't add one.
- **Where it's drawn:** the **Profile header** (120 circle) and **every circle that shows the current user**, through one renderer, `PBUserAvatar(size:, surface:)`:
  - Setup 3 "What friends see" (40).
  - Settings › Payment details preview.
  - Record payment "From / You" (56).
  - Group member lists and `Avatar / Stack` slots for "You".
  - Anywhere else a "You" avatar appears.
  - Rendering per kind:
    - `preset`: the head art scaled to the circle (existing rule, `assets/avatars/INDEX.md`).
    - `photo`: aspect-fill.
    - `character`: the **Head crop** (§1.5) of the saved look.
    - `none`: initials in `Avatar / Circle` Type=Initials.
  - Circle fill follows the existing rule: `bg/card` #F5F5F5 on white surfaces, `bg/primary` #FFFFFF inside #F5F5F5 cards.
- **Opening the editor** (Profile avatar or "Edit avatar"):
  - With a `character` avatar, the draft = the saved look (both genders' picks; the selected gender = the saved gender).
  - Otherwise the draft = the defaults (§1.3), with the starting gender guessed from the preset: Arjun, Rohan, Dev (0, 2, 4) → **Boy**; Priya, Esha (1, 3) → **Girl**; photo / none → **Boy**. This is a proposal; Figma shows Boy.
  - The **category chip always starts at Hair**, and the chip row is scrolled to the start.
- **Demo data and debug seeds:**
  - "Load demo data" keeps Arjun as `preset(0)`, so every other page matches Figma (they all draw `avatar-1` for Arjun).
  - The `profile` and `editAvatar*` start screens **additionally** seed `character(default Boy look)`, so `ref/profile.png` matches. Default Boy look: Curly, Beard none, Round, Dots, Smile, Hoodie.
  - Each `editAvatar*` start screen also seeds its draft (§3.7).
  - **Open decision:** alternatively, seed the character in the demo data everywhere and accept that the other pages show the character head for Arjun.
- **Performance:**
  - A composed avatar is up to 12 vector layers.
  - In lists (group members, stacks), render the Head crop once into a bitmap cache keyed by (look, size, scale).
  - In the editor, the 6 tiles and the stage can draw live vectors.

---

## 2. `profile`: Profile tab (64:4316)

**Purpose:** the user's identity (custom avatar, name, UPI handle), the way into the avatar editor, the settings list (Pro first), and Sign out.
**Container:** tab root. The glass tab bar has **Profile** active. No back button.

Background `bg/primary` #FFFFFF. The frame is a vertical auto-layout: padding 62 top (`layout/status-bar`), 20 left/right (`layout/screen-margin`), 34 bottom (`layout/home-indicator`), gap **24** (`layout/section-gap`).

### 2.1 Elements (top → bottom)
| # | Element | Rect (x, y, w, h) | Component / variant | Content, style, tokens | Tap |
|---|---|---|---|---|---|
| 1 | **Header** | 20, 62, 362, 44 | `Navigation / Nav Header` **Type=Large Title**, Show action = **false** (components-home §1) | toolbar row (H, centred): title **"Profile"**, **Title/1** (ExtraBold 32/38, −2 %), `text/primary`, text box 102 × 38 at (20, 65); spacer. No glass action button | – |
| 2 | **Profile header** | 20, 130, 362, 240 | frame, V auto-layout, gap **16** (`space/16`), children centred horizontally, clips | – | – |
| 2a | Avatar circle | 141, 130, 120, 120 | frame, circle (`radius/full`), fill **`bg/card` #F5F5F5**, clips | the **user's avatar** (§1.7). Figma: `Avatar / Character` Gender=Boy at rig scale **0.2**, rig origin (126.6, 118) = (−14.4, −12) in the circle, i.e. the **Head crop**. Look: Hair Curly, Beard None, Eyewear Round, Eyes Dots, Mouth Smile, Outfit Hoodie (+ Outfit back Hoodie) = the default Boy look | → **Edit avatar** (push) |
| 2b | Identity | 131.5, 266, 139, 52 | V, gap 2 (`space/2`), centred, hug | **name** "Arjun Mehta" **Title/2** (Bold 24/30, −1.5 %) `text/primary`, 139 × 30 at (131.5, 266) · **handle** "arjun@okaxis" **Subheadline** (Medium 14/20) `text/secondary` #6B6B6B, 89 × 20 at (156.5, 298). Both centred, single line | – |
| 2c | **Edit avatar** button | 147, 334, 108, 36 | `Button / Secondary` **Size=Small**, State=Default, no leading icon | fill `bg/card` #F5F5F5, capsule, padding 0/16, height 36 (`size/button-sm`); label "Edit avatar" **Button/Small** (SemiBold 15/20) `text/primary`, 76 × 20 at (163, 342). Pressed fill `bg/card-pressed` #EBEBEB | → **Edit avatar** (push) |
| 3 | **Settings card** | 20, 394, 362, 336 | frame, V, gap 0, fill **`bg/card` #F5F5F5**, radius **20** (`radius/card`), clips | 6 × `Row / Setting` **Trailing=Chevron, Tone=Default**, 56 each (§5.4.1) | rows below |
| 3.1 | Paybak Pro | 20, 394, 362, 56 | Row / Setting, Show icon, **Show badge = true**, Show value = false, Show divider | icon `crown.svg` 24 at (36, 410); title "Paybak Pro" (Headline) at (72, 411), text column 185 wide; **badge** `Badge / Pill` **Style=Inverse** "Try free": 65 × 24 at (269, 410), fill `bg/inverse` #0A0A0A, label Caption/1 `text/inverse` at (279, 414); chevron 20 at (346, 412); divider at y 449 | → Paywall |
| 3.2 | Payment details | 20, 450, 362, 56 | Row / Setting, **Show value**, Show divider | icon `wallet.svg` at (36, 466); title "Payment details" (72, 467); value **"UPI"** **Body** (Regular 16/24) `text/secondary`, 25 × 24 at (309, 466); chevron (346, 468); divider y 505 | → Payment details |
| 3.3 | Currency | 20, 506, 362, 56 | Row / Setting, Show value, Show divider | icon `exchange.svg` at (36, 522); title "Currency" (72, 523); value **"INR ₹"** Body `text/secondary`, 38 × 24 at (296, 522); chevron (346, 524); divider y 561 | → Currency |
| 3.4 | Notifications | 20, 562, 362, 56 | Row / Setting, no value, Show divider | icon `bell.svg` at (36, 578); title "Notifications" (72, 579); chevron (346, 580); divider y 617 | → Notifications & reminders |
| 3.5 | Privacy | 20, 618, 362, 56 | Row / Setting, no value, Show divider | icon `lock.svg` at (36, 634); title "Privacy" (72, 635); chevron (346, 636); divider y 673 | → Privacy & data |
| 3.6 | Help & feedback | 20, 674, 362, 56 | Row / Setting, no value, **Show divider = false** (last row) | icon `help.svg` at (36, 690); title "Help & feedback" (72, 691); chevron (346, 692) | → Help & feedback |
| 4 | **Sign out** | 20, 754, 362, 44 (row); button 171, 754, 60, 44 | frame H, centred, clips; `Button / Text` **Style=Primary**, State=Default, no chevron | label "Sign out" **Button/Small** `text/primary` #0A0A0A (black, **not** red), 60 × 20 at (171, 766). Pressed = whole button at 50 % opacity | → sign-out confirm (§2.3, proposal) |
| 5 | **Tab bar** | 20, 791, 362, 62 | `Navigation / Tab Bar` **Active=Profile** (components-home §5) | identical to Home except: **profile** item (308, 796, 68 × 52) = Active (pill `bg/selected` #0A0A0A @6 %, icon/label `text/primary`); **home** item Inactive (icon `icon/secondary`, label `text/secondary`) | tabs below |

Row details (all rows, from `Row / Setting`):
- **Icon:** 24 at row-relative (16, 16), `icon/primary`.
- **Title:** Headline (SemiBold 16/22, −0.25 %) `text/primary`, at x 72, one line, truncated at the end.
- **Value:** Body `text/secondary`, right-aligned before the chevron.
- **Chevron:** `chevron-right.svg` 20 × 20, `icon/tertiary` #A3A3A3.
- **Divider:** a `Divider / Line` Inset=Leading hairline, `border/subtle` #EBEBEB, 1 pt, from x 72 to 382, at the row's bottom (row-relative y 55). The last row has none.
- **Pressed:** no variant exists. Use a `bg/card-pressed` #EBEBEB row fill, clipped to the card's r20 shape on the first and last rows.

### 2.2 Data and states (behaviour)
- **Name:** the profile name (Setup 1).
- **Handle:** Figma shows "arjun@okaxis", which is Arjun's UPI ID (flow.md seed). Rule (proposal): show the **primary UPI ID** from Payment details; if there is none, the sign-in contact (email or phone as entered); if neither, hide the line.
- **Avatar circle:** see §1.7. A preset or photo shows in the same 120 circle. For `none`, Figma has no 120-pt initials style. Proposal: initials in **Title/1** (e.g. "AM"), centred, `text/primary`, on `bg/card`.
- **Paybak Pro row** (designer note: "The Pro entry point is the top of the settings card, shown on the free plan. After subscribing, the badge is replaced by the value “Active”."):
  - **Free:** badge "Try free" (Inverse), no value.
  - **Pro:** no badge, value **"Active"** (Body `text/secondary`), chevron kept. Tapping opens the Pro screen. Proposal: the Paywall screen in its "member" state, or the Welcome screen. Follow the Settings & Pro spec.
- **Payment details value:**
  - "UPI" when the primary method is a UPI ID; "Bank" when it's a bank account (proposal).
  - Hidden when there's no method.
- **Currency value:** "{ISO code} {symbol}" of the default currency, e.g. "INR ₹" (Setup 2 or Settings › Currency).
- **Notifications / Privacy / Help:** no value.
- **Scroll (proposal; Figma doesn't scroll):**
  - The content reaches y 798, and the Sign out hit area overlaps the tab-bar top (y 791) by 7 pt. So make the page a vertical scroll view, like Home, with the header in the scroll content and a bottom content inset of ≈ 107 pt (tab bar + 24), so Sign out can scroll clear of the bar.
  - When the large title scrolls under the status bar, show the collapsed **Nav Header Type=Inline** bar: 402 × 44 at y 62, `bg/primary` @90 % + background blur 24, centred Headline "Profile" (components-home §1).
  - No scroll-edge fade is drawn on this frame.
- **Keyboard:** none.

### 2.3 Navigation (reactions from Figma + proposals)
| Element | Destination | Figma | Presentation |
|---|---|---|---|
| Avatar circle (141, 130, 120 × 120) | **Edit avatar** (`EditAvatarScreen`, opens at Hair) | `ON_CLICK → NAVIGATE 64:4503`, **PUSH, 350 ms, EASE_OUT, direction LEFT** (new screen slides in from the right) | push; the tab bar hides |
| "Edit avatar" button | same | same reaction | push |
| Paybak Pro row | **Paybak Pro — Paywall** (page 12, "12-01", 167:13003) | `ON_CLICK → URL` (prototype link to node 167-13003 on page 77:106) | per the Settings & Pro spec (full-screen paywall) |
| Payment details row | **Payment details** (12-03, 167:14684) | URL → 167-14684 | push |
| Currency row | **Currency** (12-06, 176:17773) | URL → 176-17773 | push |
| Notifications row | **Notifications & reminders** (12-07, 176:18371) | URL → 176-18371 | push |
| Privacy row | **Privacy & data** (12-08, 176:19720) | URL → 176-19720 | push |
| Help & feedback row | **Help & feedback** (12-11, 177:24773) | URL → 177-24773 | push |
| Sign out | **not wired in Figma.** Proposal: `Overlay / Alert` Action=Destructive, title "Sign out?", message "Your records stay on this device.", buttons "Cancel" (Secondary) + "Sign out" (Destructive). Confirm clears the session (sign-in method, contact, `onboardingComplete`) but **keeps** the profile and ledger on the device, then shows Get Started. After signing in again, skip Setup when a profile exists and go to Home | – | alert |
| Tab: Home | **Home** (Home — Active 24:5 in the prototype; the Home tab root in the app) | "Tab Home hotspot" 216:24829 → URL page 3:5, node 24-5 | tab switch |
| Tab: Groups | **Groups** (07-01, 167:14881) | "Tab Groups hotspot" 216:24831 → URL 167-14881 | tab switch |
| ＋ | **Add sheet** over Profile | "＋ hotspot" 216:24833 → `OPEN_OVERLAY 216:24577` ("↳ Add sheet (overlay)", a prototype copy of Overlay — Add sheet 24:808), **MOVE_IN, 300 ms, EASE_OUT, direction TOP** | sheet over the current tab (screens-home §5) |
| Tab: Activity | **Activity — Timeline** (09-01, 167:14361) | "Tab Activity hotspot" 216:24835 → URL 167-14361 | tab switch |
| Tab: Profile | selected (no hotspot) | – | – |

### 2.4 Designer notes (verbatim)
- Caption under the frame (64:4357): "Profile tab. Tap the avatar or Edit avatar → Edit avatar (push). The Pro entry point is the top of the settings card, shown on the free plan. After subscribing, the badge is replaced by the value “Active”."

### 2.5 Sample data (verbatim, everything in the frame)
- Title "Profile". Name "Arjun Mehta". Handle "arjun@okaxis". Button "Edit avatar".
- Avatar: `Avatar / Character` Boy, look {Hair Curly, Beard None, Eyewear Round, Eyes Dots, Mouth Smile, Outfit Hoodie, Outfit (back) Hoodie}.
- Rows: "Paybak Pro" + badge "Try free" · "Payment details" + "UPI" · "Currency" + "INR ₹" · "Notifications" · "Privacy" · "Help & feedback".
- Hidden row properties carry the component defaults: Subtitle "Paid back in parts" (Show subtitle = false everywhere) and Value "UPI" (hidden on Pro, Notifications, Privacy, Help). **Don't render them.**
- "Sign out". Tab labels "Home", "Groups", "Activity", "Profile". Status bar "9:41" (system).

### 2.6 Reuse map
Header → `Navigation / Nav Header` Large Title (components-home §1) · avatar circle → NEW `PBUserAvatar` at 120 (§1.7; circle rules from `Avatar / Circle`, components-core §3.2) · Edit avatar → `Button / Secondary` Small (components-core §2.1) · settings rows → `Row / Setting` (§5.4.1) · Try free → `Badge / Pill` Inverse (components-core §3.1) · divider → `Divider / Line` Leading (components-core §4.5) · Sign out → `Button / Text` Primary (components-core §2.2) · tab bar → `Navigation / Tab Bar` Active=Profile (components-home §5). **No art besides the avatar** (no Rive).

---

## 3. Edit avatar (64:4503 and six more states)

**Purpose:** build the custom avatar. Pick Boy or Girl, a category, then an option. The stage previews the whole look live, the tiles preview "the current look with each option", Shuffle randomises, Save commits.
**Container:** pushed screen over the Profile tab. It is full height with **no tab bar** and a `Navigation / Push Header`. Back = pop (with the discard check, §3.5).

The frame is a vertical auto-layout: padding 62 / 20 / 34 / 20, gap **16** (`space/16`), `bg/primary`.

### 3.1 Elements (top → bottom; values from 64:4503)
| # | Element | Rect | Component / variant | Details |
|---|---|---|---|---|
| 1 | **Push header** | 20, 62, 362, 44 | `Navigation / Push Header` **Trailing=Text**, Title "Edit avatar", Action "Save", Show title | H, space-between, centred. **Back**: `Button / Icon` Style=Glass, Icon=Chevron Left, 44 × 44 at (20, 62): fill `bg/glass` #FFFFFF @72 %, 1 pt inside stroke `border/glass-highlight` #FFFFFF @60 %, effect **Material/Glass Small**; icon `chevron-left.svg` 24 at (30, 72) `icon/primary`. **Title** "Edit avatar" **Headline** `text/primary`, centre-aligned, fixed box 200 × 22 at (101, 73) (absolute, centred on the screen). **Save**: glass capsule 71 × 44 at (311, 62), padding 0/16, radius full, same glass fill/stroke/effect as Back; label "Save" **Headline** `text/primary` 37 × 22 at (328, 73) |
| 2 | **Stage** | 20, 122, 362, 300 | `Avatar / Stage` (64:4189) | fill `bg/card` #F5F5F5, radius **20** (`radius/card`), clips. **Avatar**: the draft look with the **Stage crop** (§1.5): rig drawn 296.16 × 323.02 at stage-relative (32.92, −23.02) (frame 52.92, 98.98). **Shuffle**: `Button / Icon` Style=Glass, Icon = `shuffle.svg`, 44 × 44 at stage-relative (306, 244) = frame (326, 366), i.e. **12 pt inset** from the stage's right and bottom edges; icon 24 at (336, 376) |
| 3 | **Boy \| Girl** | 20, 438, 362, 36 | `Control / Segmented` **Options=2**, stretched to **FILL** (components-core §4.3) | container fill `bg/card`, capsule, padding 3; two `Control / Segment`s **178 × 30** at (23, 441) and (201, 441). Selected: fill `bg/inverse`, label **Button/Small** `text/inverse`; not selected: no fill, label `text/secondary`. Labels "Boy" (27 × 20 at 98.5, 446), "Girl" (25 × 20 at 277.5, 446) |
| 4 | **Category chips** | 20, 490, 362, 36 (viewport, clips) | frame `Categories` containing `Track` (H, gap **8** `space/8`, hug) | `Control / Category Chip` **Leading=None** (§5.4.3): 36 tall, padding 0/16, capsule, label **Button/Small**. Selected: fill `bg/inverse`, label `text/inverse`; not selected: fill `bg/card`, label `text/primary`. **Boy** track 487 wide: Hair 61 (x 0) · Beard 74 (69) · Eyewear 92 (151) · Eyes 66 (251) · Mouth 79 (325) · Outfit 75 (412). **Girl** track 520: Hair 61 (0) · Accessory 107 (69) · Eyewear 92 (184) · Eyes 66 (284) · Mouth 79 (358) · Outfit 75 (445). **Scrolls horizontally** ("Chips scroll horizontally."); no scroll indicator |
| 5 | **Options grid** | 20, 542, 362, 237 | frame `Options`, H **wrap**, gap **13**, row gap **13** | six `Control / Avatar Part Tile` (§5.3), **112 × 112**, 3 columns at x 20 / 145 / 270, rows at y 542 / 667. Tile = r20 `bg/card` clip + the draft look with that option swapped in (Head crop, or Bust crop for Outfit). Selected tile = 2 pt black ring (`border/strong`, inside) + 2 pt white gap ring (`bg/primary`, 108 × 108 r18 at (2, 2), inside) |
| – | (space) | y 779–840 | – | nothing; content ends 61 pt above the home indicator |

### 3.2 Behaviour
- **Draft model.** The editor edits a `draft: AvatarLook` (§1.7), initialised when the screen opens. Everything on screen renders from the draft. Nothing persists until **Save**.
- **Boy | Girl** ("Boy | Girl switch keeps each character’s picks."):
  - Switching changes `draft.gender` only. Each gender keeps its own picks, so switching back restores them.
  - The category chips switch to that gender's set. The selected chip resets to **Hair** and the chip row scrolls to the start. That's what the prototype shows: Girl → "Girl · Hair" and Boy → "Boy · Hair".
  - Transition: DISSOLVE 200 ms EASE_OUT. Cross-fade the stage and the grid. Move the black segment pill with a 200 ms ease-out slide (suggestion).
- **Category chips:**
  - Single selection. Tapping a chip shows that category's six tiles.
  - When the selected chip is partly off-screen, scroll the track so the chip is fully visible. The Outfit states show the track scrolled to its end: Boy −125, Girl −158, so the right edge of Outfit is at the viewport's right edge.
  - Transition: SMART_ANIMATE 250 ms EASE_OUT. The chip fill cross-fades, and the tiles cross-fade to the new category.
  - **Eyes** and **Mouth** have no prototype frame and no reaction. They work exactly like the others: Head crop and the six options from §1.3.
- **Tiles:**
  - A tile shows the **current draft look with that option** ("Tiles preview the current look with each option."). Outfit tiles swap front and back layers together; Girl Hair tiles swap front and back hair.
  - Tap = `draft[gender][category] = option`. The stage updates at once, and the selected ring moves. No confirmation.
  - The prototype doesn't wire tile taps. Suggestion: a light haptic and a 150 ms cross-fade on the stage.
  - A "None" tile shows the look without that layer.
- **Shuffle** ("Glass Shuffle button randomises."):
  - Pick a random option in **every** category of the current gender. "None" options are included. Retry until the result differs from the current draft.
  - Leave the other gender and the selected chip alone.
  - Suggestion: a light haptic and a 200 ms stage cross-fade.
- **Save** ("Save commits the look; Rive avatar.riv renders it in the app."):
  - Write `UserAvatar = character(draft)`. Store both genders' picks; the saved gender is the one showing.
  - Persist, then pop to Profile. The Profile avatar and every "You" circle now show the character (§1.7).
  - Save is always enabled. With no changes it just pops.
- **Back and the discard check:** see §3.5.
- **Scroll:** the content fits the 402 × 874 frame (it ends at y 779), so the page does not scroll.
  - Smaller screens (proposal): wrap the column in a vertical scroll view. Keep tiles square at `(width − 40 − 26) / 3`.
  - Tall screens: keep the layout top-aligned.
- **Keyboard:** none.
- **Accessibility:**
  - Stage: "Avatar preview".
  - Shuffle: "Shuffle avatar".
  - Segments: "Boy" / "Girl", with the selected trait.
  - Chips: their labels, with the selected trait.
  - Tiles: the option name, e.g. "Quiff", "None", with the selected trait.

### 3.3 Navigation (reactions from Figma)
| Element | Destination | Figma reaction |
|---|---|---|
| Back (glass chevron) | **Profile**, no save, when the draft is unchanged; otherwise the **Discard alert** (§4) | Boy · Hair, Beard, Eyewear, Girl · Hair, Accessory, Outfit: `NAVIGATE 64:4316 PUSH 350 ms EASE_OUT RIGHT`. **Boy · Outfit: `NAVIGATE 64:8823` (Discard alert) DISSOLVE 200 ms EASE_OUT.** Implement the rule, not the per-frame wiring |
| Save (glass capsule) | commit + **Profile** | all states: `NAVIGATE 64:4316 PUSH 350 ms EASE_OUT RIGHT` (a pop) |
| Segment "Girl" (on Boy states) | Girl · Hair | `NAVIGATE 64:6721 DISSOLVE 200 ms EASE_OUT` |
| Segment "Boy" (on Girl states) | Boy · Hair | `NAVIGATE 64:4503 DISSOLVE 200 ms EASE_OUT` |
| Chip Hair / Beard / Eyewear / Outfit (Boy) | that category | `NAVIGATE 64:4503 / 64:4963 / 64:5384 / 64:5931`, **SMART_ANIMATE 250 ms EASE_OUT** |
| Chip Hair / Accessory / Outfit (Girl) | that category | `NAVIGATE 64:6721 / 64:7535 / 64:8052`, SMART_ANIMATE 250 ms EASE_OUT |
| Chip Eyes / Mouth | that category (not drawn) | none in Figma |
| Tiles | select the option | none in Figma |
| Shuffle | randomise | none in Figma |
| Android system back / iOS edge swipe | same as Back | – (flow.md: system back = in-app back) |

iOS: when the draft is dirty, disable the interactive pop gesture (or intercept it) so the alert can show.

### 3.4 Transitions summary
- Push in from Profile: 350 ms ease-out, sliding in from the right. Pop: 350 ms ease-out.
- Gender switch: 200 ms dissolve. Category switch: 250 ms smart-animate / cross-fade.
- Discard alert in and out: 200 ms dissolve.
- Figma has no other motion.

### 3.5 Unsaved changes rule
- **Dirty** = the draft differs from what the editor opened with: gender, Boy picks or Girl picks.
- Switching gender and back with no picks changed is **not** dirty.
- Back when dirty → **Discard alert** (§4). Back when clean → pop.
- Designer note: "Back with unsaved changes → Discard changes? Keep editing returns; Discard (red) leaves without saving."

### 3.6 Designer notes (verbatim)
- Section title (53:894): "Customize Avatar"
- Section subtitle (53:895): "Profile → Edit avatar. Boy | Girl switch, category chips, 3-column part grid, live preview. Rive file `avatar.riv` drives the same parts (ViewModel `Avatar`). iPhone 17 Pro, 402 × 874."
- Caption, Boy · Hair (64:4588): "Boy | Girl switch keeps each character’s picks. Tiles preview the current look with each option."
- Caption, Boy · Beard (64:5383): "Beard (boy only)."
- Caption, Boy · Eyewear (64:5930): "Eyewear. None removes glasses."
- Caption, Boy · Outfit (64:6720): "Outfit. Chips scroll horizontally."
- Caption, Girl · Hair (64:7534): "Girl has her own item set: hair, accessory, eyewear, eyes, mouth, outfit."
- Caption, Girl · Accessory (64:8051): "Accessory (girl only). Buns show through the beanie by design."
- Caption, Girl · Outfit (64:8822): "Save commits the look; Rive avatar.riv renders it in the app." (There is no avatar.riv; compose natively, §1.1.)
- Caption, Discard alert (64:8936): "Back with unsaved changes → Discard changes? Keep editing returns; Discard (red) leaves without saving."

### 3.7 The seven states (sample data, verbatim) and debug seeds
The header, stage box, segmented control, chip row and grid geometry are identical in every state. Only these change:

| Screen id (frame) | Gender (segment) | Selected chip | Chip track offset | **Stage look** (draft) | Tiles in order (★ = selected) | Tile crop | Back wired to |
|---|---|---|---|---|---|---|---|
| `editAvatarBoyHair` (64:4503) | **Boy** | Hair | 0 | Hair **Quiff**, Beard None, Eyewear Round, Eyes Dots, Mouth Smile, Outfit Hoodie | Curly · **Quiff ★** · Side part · Spiky · Man bun · Crew cut | Head | Profile |
| `editAvatarBoyBeard` (64:4963) | Boy | Beard | 0 | Quiff, Beard **Stubble**, Round, Dots, Smile, Hoodie | None · **Stubble ★** · Goatee · Full · Mustache · Handlebar | Head | Profile |
| `editAvatarBoyEyewear` (64:5384) | Boy | Eyewear | 0 | Quiff, Stubble, Eyewear **Square**, Dots, Smile, Hoodie | None · Round · **Square ★** · Rimless · Shades · Round shades | Head | Profile |
| `editAvatarBoyOutfit` (64:5931) | Boy | Outfit | **−125** | Quiff, Stubble, Square, Dots, Smile, Outfit **Jacket** (back Jacket = empty) | Hoodie · T-shirt · Polo · Sweater · **Jacket ★** · Shirt | **Bust** | **Discard alert** |
| `editAvatarGirlHair` (64:6721) | **Girl** | Hair | 0 (Girl track) | Hair **Ponytail** (+ back Ponytail), Accessory None, Eyewear Round, Eyes Dots, Mouth Smile, Outfit T-shirt | Long wavy · Long straight · Bob · Top bun · **Ponytail ★** · Messy bun | Head | Profile |
| `editAvatarGirlAccessory` (64:7535) | Girl | Accessory | 0 | Ponytail, Accessory **Bow**, Round, Dots, Smile, T-shirt | None · Headband · **Bow ★** · Flower clip · Hair clips · Beanie | Head | Profile |
| `editAvatarGirlOutfit` (64:8052) | Girl | Outfit | **−158** | Ponytail, Bow, Round, Dots, Smile, Outfit **Striped tee** | T-shirt · Hoodie · Collar shirt · Sweater · Blazer · **Striped tee ★** | **Bust** | Profile |

- Every tile shows the stage look with only its own option changed. Outfit tiles change the front and back outfit together; Girl hair tiles change front and back hair together.
- The prototype story is: Quiff → Stubble → Square → Jacket (Boy), then Girl: Ponytail → Bow → Striped tee. The Girl picks never overwrite the Boy picks.
- **Debug seeds** (flow.md start-screen hook), for screenshot parity:
  - The saved avatar = `character(default Boy look)`.
  - The draft = the row's stage look. The Boy states keep the Girl defaults; the Girl states keep the Boy picks Quiff / Stubble / Square / Jacket.
  - The selected chip and gender come from the row, and the chip row is scrolled so the selected chip is visible.
  - `editAvatarBoyOutfit` is dirty, so Back shows the alert.

### 3.8 Reuse map
- Push header → `Navigation / Push Header` Trailing=Text (§5.4.2).
- Stage → NEW `Avatar / Stage` (§5.2) with NEW `Avatar / Character` (§5.1) and `Button / Icon` Glass (components-core §2.3).
- Boy | Girl → `Control / Segmented` Options=2, stretched (components-core §4.3).
- Chips → `Control / Category Chip` Leading=None (§5.4.3).
- Tiles → NEW `Control / Avatar Part Tile` (§5.3).
- Icons: `chevron-left.svg`, `shuffle.svg`. No new icons.
- Art: the avatar parts only (§1). No Rive, no other illustration.

---

## 4. `editAvatarDiscard`: Discard alert (64:8823)

**Purpose:** confirm leaving the editor with unsaved changes.
**Container:** an **alert** over the Edit avatar screen. The frame's background is the Boy · Outfit state, pixel-identical to 64:5931: stage look Quiff / Stubble / Square / Dots / Smile / Jacket, chip Outfit selected, tiles Hoodie · T-shirt · Polo · Sweater · **Jacket ★** · Shirt (Bust).

| # | Element | Rect | Component | Details |
|---|---|---|---|---|
| 1 | Edit avatar (background) | full frame | §3 | not interactive while the alert shows |
| 2 | **Scrim** | 0, 0, 402, 874 | rectangle | `bg/scrim` = #0A0A0A @40 %, **covers the whole screen including the status bar** |
| 3 | **Alert** | 51, 366, 300, 142 | `Overlay / Alert` **Action=Destructive** (§5.4.4) | centred on the screen (centre y 437). Card: fill `bg/primary` #FFFFFF, radius **34**, padding 20, V gap 20, effect **Material/Glass** (drop shadow 0, 8, blur 32, #0A0A0A @10 %) |
| 3a | Title | 71, 386, 260, 22 | text | "Discard changes?" **Headline** `text/primary`, centred |
| 3b | Message | 71, 412, 260, 20 | text | "Your avatar edits won’t be saved." **Subheadline** `text/secondary`, centred (curly ’). Title → message gap 4 |
| 3c | **Keep editing** | 71, 452, 126, 36 | `Button / Secondary` Small (fill `bg/card`, label `text/primary`) | label at (89.5, 460) |
| 3d | **Discard** | 205, 452, 126, 36 | `Button / Destructive` Small (fill `bg/destructive` #C93636, label `text/inverse`) | label at (241, 460). The only red on the screen |

Navigation:
| Element | Destination | Figma reaction |
|---|---|---|
| Keep editing | dismiss the alert; stay in the editor with the draft intact | `NAVIGATE 64:5931 DISSOLVE 200 ms EASE_OUT` |
| Discard | drop the draft and pop to **Profile** | `NAVIGATE 64:4316 PUSH 350 ms EASE_OUT RIGHT` |
| Tap on the scrim | nothing (an alert needs an explicit choice) | – |
| Android back while the alert is up | = Keep editing (proposal) | – |

- **iOS:** use `.alert("Discard changes?", isPresented:)`. Put **Keep editing** under `role: .cancel` and **Discard** under `role: .destructive`, with the message text. On iOS 26+ this renders as the glass card with capsule buttons (the component description: "Built with .alert: a .cancel button plus a .destructive or default button").
- **Android:** a custom `Dialog` that draws the card above, with `dismissOnClickOutside = false`.

Designer note (verbatim, 64:8936): "Back with unsaved changes → Discard changes? Keep editing returns; Discard (red) leaves without saving."

Sample data: "Discard changes?", "Your avatar edits won’t be saved.", "Keep editing", "Discard"; the background has the Boy · Outfit data (§3.7).

Reuse: `Overlay / Alert` (§5.4.4) = `Button / Secondary` Small + `Button / Destructive` Small (components-core §2.1) and the `bg/scrim` token.

---

## 5. Components

### 5.1 `Avatar / Character` (set 61:272; Boy 61:100, Girl 61:145). SwiftUI `PBAvatarView`. NEW
- Set: property `Gender` = Boy | Girl. Variants 772 × 842 each: Gender=Boy 61:185 and Gender=Girl 61:231, each wrapping an instance of 61:100 / 61:145.
- Layers and swap properties: §1.2. Options: §1.3. Files: §1.4.
- Swap-property definitions (for reference):
  - Boy: `Hair#61:0` (default Curly), `Beard#61:1` (None), `Eyewear#61:2` (Round), `Eyes#61:3` (Dots), `Mouth#61:4` (Smile), `Outfit#61:5` (Hoodie), `Outfit (back)#61:6` (Hoodie).
  - Girl: `Hair#61:7` (Long wavy), `Hair (back)#61:8` (Long wavy), `Accessory#61:9` (None), `Eyewear#61:10` (Round), `Eyes#61:11` (Dots), `Mouth#61:12` (Smile), `Outfit#61:13` (T-shirt), `Outfit (back)#61:14` (T-shirt).
  - Each property has 6 preferred values: the six options of that slot.
- **API proposal:** `PBAvatarView(look: AvatarLook, crop: .head | .bust | .stage | .full)` (Compose: `PbAvatar(look, crop, modifier)`).
  - It draws the manifest layers inside a container, scaled and offset per §1.5.
  - It **does not** draw the background or clip. The parent (circle, tile, stage) does both.
- Section notes (verbatim):
  - Title (53:897): "Avatar Parts".
  - Subtitle (53:898): "Every option as a 772 × 842 component in Rive rig space (1:1 with the Rive `avatar` artboard). Characters combine them via instance-swap properties. Source SVGs: _design/avatar/parts."
  - Part description (every part): "Avatar part — <Gender> · <Category> · <Option>. 772×842 rig space, identical to the Rive `avatar` artboard. Source: _design/avatar/parts."
  - (`_design/avatar/parts` isn't available to us. The exported files in `assets/avatar-parts/` replace it.)

### 5.2 `Avatar / Stage` (64:4189). SwiftUI `PBAvatarStage`. NEW
Description: "Live avatar preview on the Edit avatar screen. Nested Avatar / Character shows the current look (set Gender + parts). Glass Shuffle button randomises. SwiftUI: PBAvatarStage (Rive avatar.riv)"
- 362 × 300 (fills the width), fill `bg/card` #F5F5F5, radius 20 (`radius/card`), clips.
- `Avatar`: the Stage crop, s = height / 782, rig bottom on the stage bottom, centred horizontally (§1.5).
- `Shuffle`: `Button / Icon` Style=Glass, Icon=Shuffle, 44 × 44, 12 pt from the right and bottom edges. Glass Pressed = fill `bg/card` #F5F5F5. On iOS use `.glassEffect(.regular.interactive(), in: .circle)`.
- UI-components note (verbatim): "362 × 300 live preview · character box x0 y40 → x772 y680 at ×0.4689 · glass Shuffle button, 12 pt inset". The framing numbers conflict with the instances; use §1.5.

### 5.3 `Control / Avatar Part Tile` (set 64:4176). SwiftUI `PBAvatarPartTile`. NEW
Description: "Avatar option tile. Selected = black ring. Nested Avatar / Character: set Gender and the slot being shown. SwiftUI: PBAvatarPartTile"
UI-components note (verbatim): "112 × 112 · Selected = 2 pt black ring with a 2 pt white gap · Crop: Head (box 600², ×0.1867) for face parts, Bust (box 650², ×0.1723) for outfits"
Properties: `Selected` = False | True · `Crop` = Head | Bust.

| Variant (node) | Box | Avatar (component-relative) | Ring |
|---|---|---|---|
| Selected=False, Crop=Head (64:3984) | 112 × 112, fill `bg/card`, radius 20, clips | rig 144.11 × 157.17 at (−13.44, −11.2) | none (the "Ring gap" rect is hidden) |
| Selected=True, Crop=Head (64:4032) | same + **2 pt inside stroke `border/strong` #0A0A0A** | same | + "Ring gap": 108 × 108 at (2, 2), radius 18, **2 pt inside stroke `bg/primary` #FFFFFF**, drawn **above** the avatar |
| Selected=False, Crop=Bust (64:4080) | as False/Head | rig 133.02 × 145.08 at (−10.51, −33.08) | none |
| Selected=True, Crop=Bust (64:4128) | as True/Head | as Bust | as True/Head |

- **Drawing order:** background → avatar → white gap ring → black ring. Result: a 2 pt black outline at the tile edge, a 2 pt white band inside it, then the art (clipped by the r20 tile; the band's inner radius is 16).
- **Tap:** select. No pressed variant; suggested pressed = scale 0.97 or a `bg/card-pressed` fill for the ≤ 100 ms press.
- **Width:** 112 on the 402 frame. Keep the tile square when the grid width changes; the crop scales with the tile size.

### 5.4 Shared components used here ("02 Components · Shared (from Profile)", node ids unchanged)
The page note (verbatim, 162:11424): "Row / Setting, Navigation / Push Header, Control / Category Chip, Overlay / Alert and Icon / Shuffle, Lock and Help now live on 02 Components · Shared (from Profile), with the same node IDs."

#### 5.4.1 `Row / Setting` (97:996), SwiftUI `PBSettingRow`: Trailing=Chevron, Tone=Default
- 362 wide, min height 56, no fill (it sits in a `bg/card` r20 group).
- H layout, padding [12, 16, 12, 16], gap 12, centred.
- Icon 24 (`icon/primary`). Text column fills: title **Headline** `text/primary`, one line, truncates.
- Optional `Badge / Pill` Inverse. Optional value **Body** `text/secondary`.
- Chevron **20** `icon/tertiary`.
- Absolute divider row at the bottom: a 36-wide spacer (when Show icon) + `Divider / Line` Inset=Leading → hairline from x 52 (row-relative), 1 pt `border/subtle`.
- UI-components caption (verbatim): "56 pt list row for the Profile card · icon · Headline title · optional value · chevron · leading-inset divider".

#### 5.4.2 `Navigation / Push Header` (97:1082), SwiftUI `PBPushHeader`: Trailing=Text
- 362 × 44, space-between.
- Glass back button 44. Centred Headline title in a 200 box at (81, 11), component-relative.
- Glass text capsule: height 44, padding 0/16, min width 44, Headline label.
- iOS: `NavigationStack` + `.navigationTitle` (inline) + a `ToolbarItem(placement: .topBarTrailing) { Button("Save") }`. The iOS 26+ toolbar draws the glass capsules.
- Android: a custom 44-dp row with the glass fallback: fill #FFFFFFB8, 1 dp #FFFFFF99 inside border, shadow y 4 blur 16 #0A0A0A @8 %.

#### 5.4.3 `Control / Category Chip` (64:4185), SwiftUI `PBCategoryChip`: Leading=None
- 36 tall, hug width, padding 0/16, gap 6, capsule, label Button/Small.
- Selected=True: `bg/inverse` / `text/inverse`. Selected=False: `bg/card` / `text/primary`.
- No pressed variant. Suggested pressed fill: `bg/card-pressed` (unselected), `bg/inverse-pressed` (selected).
- Give it a 44-pt hit height (4 pt above and below).

#### 5.4.4 `Overlay / Alert` (102:1115), SwiftUI `PBAlert`: Action=Destructive
- 300 × hug (142), padding 20, gap 20, radius 34, `bg/primary`, Material/Glass.
- Text block: Headline title + Subheadline message, gap 4, centred.
- Actions row: gap 8, two Small pills that fill the width (126 each): Secondary cancel + Destructive action.
- Presented centred over a full-screen `bg/scrim` @40 %.

### 5.5 Existing components reused (unchanged)
- `Navigation / Nav Header` Large Title and Inline (components-home §1).
- `Navigation / Tab Bar` Active=Profile (components-home §5).
- `Button / Secondary` Small, `Button / Destructive` Small (components-core §2.1).
- `Button / Text` Primary (§2.2).
- `Button / Icon` Glass (§2.3).
- `Badge / Pill` Inverse (§3.1).
- `Control / Segmented` Options=2 (§4.3), used FILL at 362: segments are (362 − 6) / 2 = **178** wide.
- `Divider / Line` Leading (§4.5).
- `Avatar / Circle` fill rules (§3.2).

---

## 6. Avatar Parts section: sheets and component frames (reference only, nothing to build)
- **Parts sheet — Boy** (62:243, 1408 × 1560): "Boy — 36 options" / "Each option shown on the default look. ★ = default (today’s look). Checkpoint 1: approve before Rive."
  - Rows, in the order used for §1.3: Hair · Beard · Eyewear · Eyes · Mouth · Outfit.
  - Each row has six framed previews with labels: "Curly ★", "Quiff", …, "None ★" (Beard), "Round ★" (Eyewear), "Dots ★", "Smile ★", "Hoodie ★".
- **Parts sheet — Girl** (62:2148): "Girl — 36 options" / the same subtitle.
  - Rows Hair · Accessory · Eyewear · Eyes · Mouth · Outfit.
  - Stars: "Long wavy ★", "None ★" (Accessory), "Round ★", "Dots ★", "Smile ★", "T-shirt ★".
- **Boy parts** (53:899) and **Girl parts** (53:900): grids of the 46 + 52 part components, each 772 × 842 on an 820 × 962 pitch. All of them are exported (§1.4).
- **UI components** (64:3948) texts (verbatim):
  - "UI components — Customize Avatar"
  - "Local components for Profile → Edit avatar. Built on Color / Radius / Spacing variables and Manrope text styles. Avatar art comes from the nested Avatar / Character instance."
  - "Control / Avatar Part Tile"
  - "112 × 112 · Selected = 2 pt black ring with a 2 pt white gap · Crop: Head (box 600², ×0.1867) for face parts, Bust (box 650², ×0.1723) for outfits"
  - "Row / Setting"
  - "56 pt list row for the Profile card · icon · Headline title · optional value · chevron · leading-inset divider"
  - "Notifications" (the sample row label)
  - "Avatar / Stage"
  - "362 × 300 live preview · character box x0 y40 → x772 y680 at ×0.4689 · glass Shuffle button, 12 pt inset"
  - The moved-components note is quoted in §5.4.

---

## 7. Persistence, ids and debug hooks
- **Persist** (with the rest of the profile, flow.md): `avatar.kind` ∈ none | preset | photo | character, plus `avatar.presetIndex` and `avatar.photoFile`.
  - For a character, store `avatar.look` as JSON, for example `{"gender":"boy","boy":{"hair":"quiff","beard":"stubble","eyewear":"square","eyes":"dots","mouth":"smile","outfit":"jacket"},"girl":{"hair":"ponytail","accessory":"bow","eyewear":"round","eyes":"dots","mouth":"smile","outfit":"striped-tee"}}`.
  - Unknown ids (e.g. after an asset update) fall back to the category default.
- **Start-screen ids:** `profile`, `editAvatarBoyHair`, `editAvatarBoyBeard`, `editAvatarBoyEyewear`, `editAvatarBoyOutfit`, `editAvatarGirlHair`, `editAvatarGirlAccessory`, `editAvatarGirlOutfit`, `editAvatarDiscard`. Seeds are in §1.7 and §3.7. `editAvatarDiscard` = the Boy · Outfit state with the alert showing.
- **Test ids** (`<screen>.<element>`):
  - Screen roots: `screen.profile`, `screen.editAvatar`.
  - Profile: `profile.avatar`, `profile.editAvatar`, `profile.name`, `profile.handle`, `profile.row.pro`, `profile.row.payment`, `profile.row.currency`, `profile.row.notifications`, `profile.row.privacy`, `profile.row.help`, `profile.signOut`, `home.tab.*` (shared tab bar).
  - Editor: `editAvatar.back`, `editAvatar.save`, `editAvatar.stage`, `editAvatar.shuffle`, `editAvatar.gender.boy`, `editAvatar.gender.girl`, `editAvatar.category.<id>`, `editAvatar.option.<id>`.
  - Discard alert: `editAvatar.discard.keep`, `editAvatar.discard.discard`.
- **UI test flow to cover:**
  1. Profile → Edit avatar.
  2. Pick Quiff; the stage changes.
  3. Girl → Ponytail → Boy; Quiff is still selected.
  4. Back → the alert → Keep editing → Save.
  5. The Profile avatar shows Quiff. Re-open: the draft = saved.
  6. Back when clean → Profile with no alert.

---

## 8. Assets
**Added by this spec:**
- `assets/avatar-parts/manifest.json`: categories, options, UI order, defaults, none options, layer order, crops, rules (§1.3–1.5).
- `assets/avatar-parts/boy/…` (39 SVGs):
  - `base/{body,face,nose,shadow}`
  - `hair/{curly,quiff,side-part,spiky,man-bun,crew-cut}`
  - `beard/{stubble,goatee,full,mustache,handlebar}`
  - `eyewear/{round,square,rimless,shades,round-shades}`
  - `eyes/{dots,happy,wink,squint,wide,sleepy}`
  - `mouth/{smile,grin,neutral,smirk,open,tongue}`
  - `outfit/{hoodie,t-shirt,polo,sweater,jacket,shirt}`
  - `outfit-back/hoodie`
- `assets/avatar-parts/girl/…` (45 SVGs):
  - `base/{body,face,nose,shadow}`
  - `hair/{long-wavy,long-straight,bob,top-bun,ponytail,messy-bun}`
  - `hair-back/` (the same six)
  - `accessory/{headband,bow,flower-clip,hair-clips,beanie}`
  - `eyewear/{round,square,shades,heart-shades,cat-eye}`
  - `eyes/…` (six), `mouth/…` (six)
  - `outfit/{t-shirt,hoodie,collar-shirt,sweater,blazer,striped-tee}`
  - `outfit-back/hoodie`
- `assets/avatar-parts/check/*.png`: composite renders from the part files, for verification only; don't ship them (§1.6).
- References: `ref/profile.png`, `ref/editAvatar*.png` (2× REST renders), plus `ref/…_1x.png` crops (§0 table).
- Node data: the Plugin API dumps and per-frame REST subtrees used for this spec weren't kept. The REST node JSON for the 9 frames, UI components 64:3948, `Avatar / Character` 61:272 and the Add-sheet helper 216:24577 is in `.figma-cache/nodes/53-892.json` (regenerate with `tools/fetch_figma.py`).

**Reused:**
- Icons: `crown`, `wallet`, `exchange`, `bell`, `lock`, `help`, `chevron-right`, `chevron-left`, `shuffle`, plus the tab-bar icons (`home`, `groups`, `activity`, `profile`) and `assets/images/add-button-plus.svg`. All already exist in `assets/icons/` (checked against `INDEX.md`). **No new icons.**
- `assets/avatars/avatar-1…5` (Setup 1 presets, §1.7).

**No Rive** on this page. No new illustrations besides the avatar parts.

---

## 9. Open questions and decisions (proposals in the text; please confirm)
1. **No avatar.riv.** The Figma notes assume Rive drives the avatar. Native layer composition from the exported parts replaces it (§1). If a Rive file is made later, it must use the same ids and the same 772 × 842 rig.
2. **Arjun's avatar in demo data.**
   - Only the Profile header shows the character; every other page shows the preset head `avatar-1`.
   - Proposal: the demo = `preset(0)`, and the `profile`/`editAvatar*` start screens seed the character (§1.7).
   - Alternative: the character everywhere.
3. **Editor starting gender for preset users:** mapped from the preset head (§1.7). Figma only shows Boy first.
4. **Stage framing:** the note says ×0.4689 (y40→680); the instances use ×0.3836 (y60→842). The spec follows the instances.
5. **Sign out** isn't wired in Figma. The proposed alert and session-only sign-out are in §2.3.
6. **Profile scroll / Inline header / bottom inset:** Figma doesn't scroll, but Sign out overlaps the tab bar by 7 pt. Scrolling is proposed (§2.2).
7. **Handle line and Payment details value** when no UPI ID is set: proposed rules in §2.2.
8. **Pro member row:** value "Active" (from the note). The destination when Pro is up to the Settings & Pro spec.
9. **Shuffle rules** (all categories, "None" allowed, must differ) and **tile-tap feedback** are proposals. Figma wires neither.
