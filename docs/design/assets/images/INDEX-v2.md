# assets/images: v2 additions (for components-app.md)

Exported read-only from Figma page "02 Components" (3:3), Illustrations & Art (7:2) → **Art / Receipt** (86:730), on 1 Oct 2026. Nothing existing was overwritten. Spec: `components-app.md` §7.1.

| File | Figma source | Size | Format | Used on | Notes |
|---|---|---|---|---|---|
| `art-receipt-full.svg` | Art / Receipt, Size=Full (86:667). `exportAsync({format:'SVG_STRING', svgOutlineText:false})` | 300 × 458 | SVG, **live text** (`<text font-family="Manrope">`, weights 500/600/700) | Scan receipt camera scene (11-07, shown rotated); the demo receipt for the simulated scanner | The white paper has a torn (zig-zag) bottom edge, a 2-pt #EBEBEB inside border, and dashed separators. It needs Manrope to render correctly, so where exact text matters ship the PNG below. The outlined-text export is 194 kB and wasn't saved. |
| `art-receipt-full@3x.png` | the same node, PNG, scale 3 (`download_assets`) | 900 × 1374 (300 × 458 @3x) | PNG, transparent outside the paper | same as above | The raster to ship for the camera scene. |
| `art-receipt-thumb.svg` | Art / Receipt, Size=Thumb (86:712), SVG | 56 × 72 | SVG, shapes only (no text) | Receipt thumbnail placeholder: expense detail (09-03), receipt review (11-08, 11-10); also stands for other receipts (the Goa "Villa") | #EBEBEB r10 tile, a white paper strip and "text" drawn as #0A0A0A bars. In the app the slot shows the real photo (aspect-fill, clipped to 56 × 72 r10). |

Receipt content (sample data, verbatim): "Leopold Cafe" · "Colaba Causeway, Mumbai" · "Wed 30 Sep 2026 · 1:15 pm" · Chicken biryani ₹430 · Paneer tikka ₹370 · Fish and chips ₹450 · Chocolate brownie ₹240 · Masala fries ₹240 · Fresh lime soda ×3 ₹270 · Subtotal ₹2,000 · GST 5% ₹100 · Tip 10% ₹200 · Total ₹2,300 · "Thank you. Visit again."

Not exported (not used in the app): `Illustration / Cover — Crowd` (7:245), the Figma cover hero. The Kabir and Meera heads were already exported to `assets/avatars/avatar-6` / `avatar-7`.
