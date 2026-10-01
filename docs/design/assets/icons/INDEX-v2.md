# Icons, v2 check (for components-app.md)

**Result: no icon is missing, and nothing new was exported.**
- On 1 Oct 2026 a read-only Plugin API scan of page "02 Components" (3:3) found **65** components named `Icon / …`: 62 in the Icons section (5:2) plus Shuffle (64:3957), Lock (64:3963) and Help (64:3969) in Shared (from Profile). The Icons section hasn't grown since the first spec.
- All 65 map to existing files in this folder under the same rule (name without "Icon / ", kebab-case). `INDEX.md` stays the complete index (component → file → HugeIcons source).
- Sizing and tint rules are unchanged (INDEX.md). One finding: in the new components, nested 16/20-pt icons keep a 1.5 stroke instead of scaling. See `components-app.md` §0 and §11 #1. The recommendation is to keep scaling the whole icon.

## Icons used by the new components (components-app.md)
| Icon file | Where (component → layer) | Drawn size | Tint |
|---|---|---|---|
| `plus.svg` | Control / Category Chip Leading=Icon (default, swappable) | 16 | `icon/primary` / `icon/inverse` when selected |
| `close.svg` | Category Chip `remove` · Row / Person Trailing=Remove · Modal Header ✕ on Android (the kit xmark on iOS) | 16 · 20 · 20 | label colour · `icon/secondary` · #1A1A1A |
| `wallet.svg` | Row / Setting `icon` (default, swappable) | 24 | `icon/primary` (Destructive: `icon/destructive`) |
| `chevron-right.svg` | Row / Setting Trailing=Chevron | 20 | `icon/tertiary` |
| `check.svg` | Row / Setting Trailing=Check · Row / Person Check (24) and Select On (16 on black) · Row / Split Person select (16 on black) | 24 / 16 | `icon/primary` / `icon/inverse` |
| `chevron-left.svg` | Navigation / Push Header back (glass 44) | 24 | `icon/primary` |
| `settings.svg` | Push Header Trailing=Icon (default) | 24 | `icon/primary` |
| `mic.svg` | Control / Composer, Empty | 24 | `icon/secondary` |
| `arrow-up.svg` | Composer send (36 black circle, icon at (6, 6)) | 24 | `icon/inverse` |
| `arrow-right.svg` | Control / Payment Parties (20) · Avatar / Pair (16 at Size=32, 20 at Size=56) | 20 / 16 | `icon/tertiary` |
| `alert.svg` | Card / Split Total Error (20) · Card / Budget Over budget (16) | 20 / 16 | `icon/destructive` |
| `plane.svg`, `home.svg`, `people.svg`, `tag.svg`, `drone.svg`, `package.svg` | Header / Title Row tile (24 in a 56 circle) · Row / Group tile (20 in a 40 circle) | 24 / 20 | `icon/primary` |
| `food.svg`, `bed.svg` | Header / Amount Hero Leading=Icon (24 in 56) · sheet example rows (24) | 24 | `icon/primary` |
| `food.svg`, `car.svg`, `bed.svg`, `ticket.svg`, `home.svg`, `bolt.svg`, `shopping-bag.svg`, `tag.svg`, `plane.svg`, `people.svg` | Row / Bar Leading=Icon (category and group map, 20 in 40) | 20 | `icon/primary` |
| `activity.svg`, `shuffle.svg`, `mail.svg`, `flag.svg`, `lock.svg`, `check-circle.svg` | Card / Notice icon circle (20 in 40 Leading, 24 in 56 Centered) | 20 / 24 | `icon/primary` |
| `check-circle.svg` | Card / Confirm Payment Confirmed (24) · Card / Loan Progress Paid back (16) · Chat / Draft Expense Saved (20) · Card / Person Totals (16) · Overlay / Toast (20, white) | 24/16/20/16/20 | `icon/primary`; Toast `icon/inverse` |
| `sparkles.svg` | Chat / Bubble Assistant avatar (14 in 24) | 14 | `icon/primary` |
| `car.svg` | Chat / Draft Expense icon circle (default) | 20 | `icon/primary` |
| `search.svg` | Sheet / Container search field | 20 | `icon/secondary` |
| `copy.svg` | Card / Payment Preview (existing) | 24 / 16 | `icon/primary` |
