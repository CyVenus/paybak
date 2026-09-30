import Foundation

// STUB (app-architecture §4): Lane B fills this with VisionKit's DataScanner, keeping these names.
/// Scans a friend's QR code.
enum QRScanner {
    /// Whether this device can scan (else the toast "Scanning isn’t available on this device").
    static var isAvailable: Bool { false }

    /// The invite link's username, e.g. "meera" from `https://paybak.app/i/meera`.
    static func username(fromInviteLink text: String) -> String? {
        guard let url = URL(string: text), url.host() == "paybak.app" else { return nil }
        let parts = url.pathComponents.filter { $0 != "/" }
        return parts.count == 2 && parts[0] == "i" ? parts[1] : nil
    }
}
