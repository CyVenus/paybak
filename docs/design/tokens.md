# Paybak design tokens (read with the Plugin API from Figma "01 Foundations"; exact values)

Each variable has its Swift name set in Figma as iOS code syntax. Verify these with `get_variable_defs` if you need them. Names below are the Figma variable names.

## Color: primitives
gray/0 #FFFFFF · gray/50 #F5F5F5 · gray/100 #EBEBEB · gray/200 #E0E0E0 · gray/300 #D1D1D1 · gray/400 #A3A3A3 · gray/600 #6B6B6B · gray/800 #2B2B2B · gray/900 #0A0A0A · red/50 #FBEBEB · red/500 #C93636 · red/600 #A92E2E · alpha/black-40 #0A0A0A @40% · alpha/black-06 #0A0A0A @6% · alpha/white-72 #FFFFFF @72% · alpha/white-60 #FFFFFF @60% · device/black #000000

## Color: semantic (mode "Light")
bg/primary → gray/0 · bg/card → gray/50 · bg/card-pressed → gray/100 · bg/selected → black-06 · bg/inverse → gray/900 · bg/inverse-pressed → gray/800 · bg/disabled → gray/200 · bg/destructive → red/500 · bg/destructive-pressed → red/600 · bg/destructive-subtle → red/50 · bg/scrim → black-40 · bg/glass → white-72 · bg/indicator → gray/300 · bg/device → black · bg/camera → gray/800
text/primary → gray/900 · text/secondary → gray/600 · text/tertiary → gray/400 · text/inverse → gray/0 · text/disabled → gray/400 · text/destructive → red/500
icon/primary → gray/900 · icon/secondary → gray/600 · icon/tertiary → gray/400 · icon/inverse → gray/0 · icon/destructive → red/500
border/subtle → gray/100 · border/strong → gray/900 · border/destructive → red/500 · border/glass-highlight → white-60
illustration/line → gray/900 · illustration/tint → gray/100 · illustration/fill → gray/0
chart/track → gray/100 · chart/bar → gray/300 · chart/fill → gray/900 · chart/over → red/500

## Spacing
space/0 0 · 2 · 4 · 6 · 8 · 12 · 16 · 20 · 24 · 28 · 32 · 40 · 48 · 64 · 96
layout/screen-margin → 20 · layout/card-padding → 16 · layout/section-gap → 24 · layout/status-bar 62 · layout/home-indicator 34

## Radius
radius/xs 6 · radius/sm 10 · radius/input 14 · radius/tile 14 · radius/card 20 · radius/sheet 40 · radius/full 999

## Size
size/button-lg 52 · size/button-sm 36 · size/tap 44 · size/icon-sm 16 · size/icon-md 20 · size/icon-lg 24 · size/avatar-xs 24 · size/avatar-sm 32 · size/avatar-md 40 · size/avatar-lg 56 · size/tabbar 62 · size/add-button 52 · stroke/hairline 1

## Text styles (Manrope; line height in px; letter spacing in % of the font size)
| Style | Weight | Size | Line height | Letter spacing |
|---|---|---|---|---|
| Title/1 | ExtraBold (800) | 32 | 38 | -2% |
| Title/2 | Bold (700) | 24 | 30 | -1.5% |
| Title/3 | Bold (700) | 20 | 26 | -1% |
| Amount/Display | ExtraBold (800) | 56 | 64 | -2% |
| Amount/Large | ExtraBold (800) | 26 | 32 | -2% |
| Amount/Medium | Bold (700) | 17 | 22 | -0.5% |
| Headline | SemiBold (600) | 16 | 22 | -0.25% |
| Body | Regular (400) | 16 | 24 | 0 |
| Button/Large | SemiBold (600) | 17 | 22 | -0.5% |
| Button/Small | SemiBold (600) | 15 | 20 | -0.25% |
| Subheadline | Medium (500) | 14 | 20 | 0 |
| Footnote | Medium (500) | 13 | 18 | 0 |
| Caption/1 | Bold (700) | 12 | 16 | +1% |
| Caption/2 | SemiBold (600) | 11 | 13 | +1% |
| Brand/Wordmark S | ExtraBold (800) | 20 | 24 | -3% |
| Brand/Wordmark L | ExtraBold (800) | 40 | 44 | -3% |
(Styles starting with `_Doc/` are only for documenting the Figma file. Ignore them.)
Letter spacing in points = size × percent / 100 (e.g. Title/1: 32 × -0.02 = -0.64 pt). Compose: `letterSpacing = (-0.02).em`.

## Effects (materials)
- Material/Glass: drop shadow 0,8 blur 32 #0A0A0A @10% + GLASS (refraction 0.7, depth 30, radius 16, dispersion 0.2, light intensity 0.25, light angle 0). iOS: `.glassEffect()`. Android: glass fallback.
- Material/Glass Small: drop shadow 0,4 blur 16 #0A0A0A @8% + GLASS (radius 6, refraction 0.4, depth 8, dispersion 0.1, light intensity 0.25, light angle 0). (GLASS parameters verified from the effect styles by the editor.)
- Material/Frosted: drop shadow 0,8 blur 32 @10% + background blur 24.
