import SwiftUI
import Vision
import VisionKit

/// Scans a friend's QR code with VisionKit's data scanner (app-architecture §4).
enum QRScanner {
    /// Whether this device can scan (else the toast "Scanning isn’t available on this device").
    static var isAvailable: Bool {
        DataScannerViewController.isSupported && DataScannerViewController.isAvailable
    }

    /// The invite link's username, e.g. "meera" from `https://paybak.app/i/meera`.
    static func username(fromInviteLink text: String) -> String? {
        let link = text.hasPrefix("paybak.app/") ? "https://" + text : text
        guard let url = URL(string: link), url.host() == "paybak.app" else { return nil }
        let parts = url.pathComponents.filter { $0 != "/" }
        return parts.count == 2 && parts[0] == "i" && !parts[1].isEmpty ? parts[1].lowercased() : nil
    }
}

/// The full-screen camera scanner: reports the first QR code it reads, or nil on Cancel.
struct QRScannerView: View {
    let onFinish: (String?) -> Void

    var body: some View {
        DataScanner(onScan: { onFinish($0) })
            .ignoresSafeArea()
            .overlay(alignment: .topLeading) {
                PBIconButton(.close, accessibilityLabel: "Cancel", style: .glass) { onFinish(nil) }
                    .padding(.horizontal, PBLayout.screenMargin)
                    .accessibilityIdentifier("addFriend.scanner.close")
            }
            .background(PBColor.bgCamera)
    }
}

private struct DataScanner: UIViewControllerRepresentable {
    let onScan: (String) -> Void

    func makeUIViewController(context: Context) -> DataScannerViewController {
        let scanner = DataScannerViewController(recognizedDataTypes: [.barcode(symbologies: [.qr])], qualityLevel: .balanced,
                                                isHighlightingEnabled: true)
        scanner.delegate = context.coordinator
        try? scanner.startScanning()
        return scanner
    }

    func updateUIViewController(_ scanner: DataScannerViewController, context: Context) {}

    func makeCoordinator() -> Coordinator { Coordinator(onScan: onScan) }

    final class Coordinator: NSObject, DataScannerViewControllerDelegate {
        let onScan: (String) -> Void
        private var didScan = false

        init(onScan: @escaping (String) -> Void) {
            self.onScan = onScan
        }

        func dataScanner(_ scanner: DataScannerViewController, didAdd items: [RecognizedItem], allItems: [RecognizedItem]) {
            for case .barcode(let code) in items {
                guard !didScan, let text = code.payloadStringValue else { continue }
                didScan = true
                scanner.stopScanning()
                onScan(text)
            }
        }
    }
}
