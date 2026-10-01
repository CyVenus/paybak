# assets/images: Home exports (for the Home spec)

All vector, exported read-only from Figma with `exportAsync({format:'SVG_STRING'})`. Path coordinates are rounded to 2 decimals and the files have transparent backgrounds.

| File | Figma source | Box | Used on | Notes |
|---|---|---|---|---|
| peep-head-rohan.svg | Art / Peep Head / Rohan (7:37), Open Peeps peep-21 | 120×120 (clip) | Home Active, "Rohan" overdue row (Avatar / Circle 40, white circle); Row / Activity Payment/Out | Scale to the avatar size (40) and clip to a circle; the circle fill comes from the avatar (white on cards, #F5F5F5 on white). |
| peep-head-priya.svg | Art / Peep Head / Priya (7:22), Open Peeps peep-93 | 120×120 (clip) | Home Active, "Priya paid you" activity row (circle #F5F5F5) | Same as above. |
| empty-first-day.svg | Illustration / Empty — First day (7:216), Open Doodles "laying" | 240×180 | Static fallback / preview for the First-day empty state | On device the slot uses `paybak-homefirstday.riv` (artboard `First Day`). |
| empty-all-square.svg | Illustration / Empty — All square (7:221), Open Doodles "meditating" | 240×180 | Static fallback for the All-settled empty state | On device: `paybak-home-allset.riv` (artboard `AllSquare`, 264×204, centred on the 240×180 slot). |
| add-button-plus.svg | Icon / Plus inside Button / Add (10:82) | 24×24 | Tab-bar ＋ button | White, **stroke 2** (the generic `icons/plus.svg` is stroke 1.5). |

Icons used by Home come from `assets/icons/` (exported with the component specs): sparkles, bell, money-in, money-out, chevron-right, groups, food, bolt, home, activity, profile, plus, user-add, receipt, exchange, lend, close, check-circle. The brand mark is `assets/brand/app-mark-28.svg` / `logo-horizontal.svg`.
