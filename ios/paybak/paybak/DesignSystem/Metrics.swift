import CoreGraphics

/// Spacing tokens: `PBSpace.s16` is Figma's `space/16`.
enum PBSpace {
    static let s0: CGFloat = 0
    static let s2: CGFloat = 2
    static let s4: CGFloat = 4
    static let s6: CGFloat = 6
    static let s8: CGFloat = 8
    static let s12: CGFloat = 12
    static let s16: CGFloat = 16
    static let s20: CGFloat = 20
    static let s24: CGFloat = 24
    static let s28: CGFloat = 28
    static let s32: CGFloat = 32
    static let s40: CGFloat = 40
    static let s48: CGFloat = 48
    static let s64: CGFloat = 64
    static let s96: CGFloat = 96
}

/// Layout tokens (`layout/*`). Figma's `layout/status-bar` (62) and `layout/home-indicator` (34) are
/// deliberately missing: screens lay out from the safe area instead of hard-coding the insets.
enum PBLayout {
    static let screenMargin: CGFloat = 20
    static let cardPadding: CGFloat = 16
    static let sectionGap: CGFloat = 24
    /// Phone layouts stay centred at this width on wider screens (flow.md "Resolved decisions").
    static let maxContentWidth: CGFloat = 430
}

/// Corner radius tokens (`radius/*`). `radius/full` (999) is drawn with `Capsule`/`Circle`.
enum PBRadius {
    static let xs: CGFloat = 6
    static let sm: CGFloat = 10
    static let input: CGFloat = 14
    static let tile: CGFloat = 14
    static let card: CGFloat = 20
    static let sheet: CGFloat = 40
    static let full: CGFloat = 999
}

/// Size tokens (`size/*` and `stroke/hairline`).
enum PBSize {
    static let buttonLg: CGFloat = 52
    static let buttonSm: CGFloat = 36
    static let tap: CGFloat = 44
    static let iconSm: CGFloat = 16
    static let iconMd: CGFloat = 20
    static let iconLg: CGFloat = 24
    static let avatarXs: CGFloat = 24
    static let avatarSm: CGFloat = 32
    static let avatarMd: CGFloat = 40
    static let avatarLg: CGFloat = 56
    static let tabbar: CGFloat = 62
    static let addButton: CGFloat = 52
    static let hairline: CGFloat = 1
}
