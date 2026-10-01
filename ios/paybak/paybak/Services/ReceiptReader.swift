import UIKit
import Vision

/// Reads a receipt photo on the device (app-architecture §4.1): Vision's accurate text recognition,
/// then `ReceiptParser`. Throws when nothing useful was read.
enum ReceiptReader {
    enum ReadError: Error {
        case unreadable
    }

    static func read(_ image: UIImage, currency: String) async throws -> ReceiptScan {
        guard let cgImage = image.cgImage else { throw ReadError.unreadable }
        let texts = try await recognize(cgImage, orientation: CGImagePropertyOrientation(image.imageOrientation))
        guard let scan = ReceiptParser.parse(texts, currency: currency) else { throw ReadError.unreadable }
        return scan
    }

    /// Vision works off the main thread; its boxes have y growing upwards, so they're flipped here.
    private nonisolated static func recognize(_ image: CGImage, orientation: CGImagePropertyOrientation) async throws -> [ReceiptText] {
        try await Task.detached(priority: .userInitiated) {
            let request = VNRecognizeTextRequest()
            request.recognitionLevel = .accurate
            request.recognitionLanguages = ["en-US"]
            request.usesLanguageCorrection = false
            try VNImageRequestHandler(cgImage: image, orientation: orientation).perform([request])
            return (request.results ?? []).compactMap { observation in
                guard let text = observation.topCandidates(1).first?.string else { return nil }
                let box = observation.boundingBox
                return ReceiptText(text: text, minX: box.minX, maxX: box.maxX, minY: 1 - box.maxY, maxY: 1 - box.minY)
            }
        }.value
    }
}

private extension CGImagePropertyOrientation {
    nonisolated init(_ orientation: UIImage.Orientation) {
        self = switch orientation {
        case .up: .up
        case .down: .down
        case .left: .left
        case .right: .right
        case .upMirrored: .upMirrored
        case .downMirrored: .downMirrored
        case .leftMirrored: .leftMirrored
        case .rightMirrored: .rightMirrored
        @unknown default: .up
        }
    }
}
