import CoreText
import Foundation
import UIKit
import os

/// The five bundled Manrope files, by PostScript name.
///
/// The Medium, SemiBold and ExtraBold files each declare their own family name ("Manrope Medium", …),
/// so family + weight lookups are unreliable. Always load a face by its PostScript name.
enum PBFont: String, CaseIterable {
    case regular = "Manrope-Regular"
    case medium = "Manrope-Medium"
    case semiBold = "Manrope-SemiBold"
    case bold = "Manrope-Bold"
    case extraBold = "Manrope-ExtraBold"

    var postScriptName: String { rawValue }

    /// The UIKit face, used to read the font's ascender and descender for Figma line boxes.
    func uiFont(size: CGFloat) -> UIFont {
        UIFont(name: postScriptName, size: size) ?? .systemFont(ofSize: size)
    }

    /// Registers the TTFs in Resources/Fonts (copied flat into the bundle) for this process.
    /// Call once at launch, before the first view renders.
    static func registerAll(bundle: Bundle = .main) {
        for font in allCases {
            guard let url = bundle.url(forResource: font.postScriptName, withExtension: "ttf") else {
                assertionFailure("Missing \(font.postScriptName).ttf in the app bundle")
                continue
            }
            var error: Unmanaged<CFError>?
            if !CTFontManagerRegisterFontsForURL(url as CFURL, .process, &error) {
                let message = error?.takeRetainedValue().localizedDescription ?? "unknown error"
                Logger.fonts.error("Could not register \(font.postScriptName, privacy: .public): \(message, privacy: .public)")
            }
        }
    }
}

private extension Logger {
    static let fonts = Logger(subsystem: "app.paybak.paybak", category: "Fonts")
}
