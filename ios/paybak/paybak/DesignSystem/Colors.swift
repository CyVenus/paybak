import SwiftUI

/// Colour primitives from Figma "01 Foundations" (`gray/50`, `red/500`, `alpha/black-40`, …).
/// Views use the semantic tokens in `PBColor`; the primitives only define them.
enum PBPalette {
    static let gray0 = Color(hex: 0xFFFFFF)
    static let gray50 = Color(hex: 0xF5F5F5)
    static let gray100 = Color(hex: 0xEBEBEB)
    static let gray200 = Color(hex: 0xE0E0E0)
    static let gray300 = Color(hex: 0xD1D1D1)
    static let gray400 = Color(hex: 0xA3A3A3)
    static let gray600 = Color(hex: 0x6B6B6B)
    static let gray800 = Color(hex: 0x2B2B2B)
    static let gray900 = Color(hex: 0x0A0A0A)
    static let red50 = Color(hex: 0xFBEBEB)
    static let red500 = Color(hex: 0xC93636)
    static let red600 = Color(hex: 0xA92E2E)
    static let black40 = Color(hex: 0x0A0A0A, opacity: 0.40)
    static let black06 = Color(hex: 0x0A0A0A, opacity: 0.06)
    static let white72 = Color(hex: 0xFFFFFF, opacity: 0.72)
    static let white60 = Color(hex: 0xFFFFFF, opacity: 0.60)
    static let deviceBlack = Color(hex: 0x000000)
}

/// Semantic colour tokens (Figma mode "Light"; the app is light-only).
/// `PBColor.bgCard` is Figma's `color/bg/card`, `PBColor.textSecondary` is `color/text/secondary`, and so on.
enum PBColor {
    // MARK: Background
    static let bgPrimary = PBPalette.gray0
    static let bgCard = PBPalette.gray50
    static let bgCardPressed = PBPalette.gray100
    static let bgSelected = PBPalette.black06
    static let bgInverse = PBPalette.gray900
    static let bgInversePressed = PBPalette.gray800
    static let bgDisabled = PBPalette.gray200
    static let bgDestructive = PBPalette.red500
    static let bgDestructivePressed = PBPalette.red600
    static let bgDestructiveSubtle = PBPalette.red50
    static let bgScrim = PBPalette.black40
    static let bgGlass = PBPalette.white72
    static let bgIndicator = PBPalette.gray300
    static let bgDevice = PBPalette.deviceBlack
    static let bgCamera = PBPalette.gray800

    // MARK: Text
    static let textPrimary = PBPalette.gray900
    static let textSecondary = PBPalette.gray600
    static let textTertiary = PBPalette.gray400
    static let textInverse = PBPalette.gray0
    static let textDisabled = PBPalette.gray400
    static let textDestructive = PBPalette.red500

    // MARK: Icon
    static let iconPrimary = PBPalette.gray900
    static let iconSecondary = PBPalette.gray600
    static let iconTertiary = PBPalette.gray400
    static let iconInverse = PBPalette.gray0
    static let iconDestructive = PBPalette.red500

    // MARK: Border
    static let borderSubtle = PBPalette.gray100
    static let borderStrong = PBPalette.gray900
    static let borderDestructive = PBPalette.red500
    static let borderGlassHighlight = PBPalette.white60

    // MARK: Illustration
    static let illustrationLine = PBPalette.gray900
    static let illustrationTint = PBPalette.gray100
    static let illustrationFill = PBPalette.gray0

    // MARK: Chart
    static let chartTrack = PBPalette.gray100
    static let chartBar = PBPalette.gray300
    static let chartFill = PBPalette.gray900
    static let chartOver = PBPalette.red500
}

extension Color {
    /// An sRGB colour from a 0xRRGGBB literal, as the hex values appear in Figma.
    init(hex: UInt32, opacity: Double = 1) {
        self.init(
            .sRGB,
            red: Double((hex >> 16) & 0xFF) / 255,
            green: Double((hex >> 8) & 0xFF) / 255,
            blue: Double(hex & 0xFF) / 255,
            opacity: opacity
        )
    }
}
