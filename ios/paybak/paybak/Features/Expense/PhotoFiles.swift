import SwiftUI
import UIKit
import os

/// Receipt and proof photos (app-architecture §3.7): JPEGs in `Application Support/photos/`,
/// referenced from records by file name. Demo records name a bundled image instead.
enum PhotoFiles {
    private static let log = Logger(subsystem: "app.paybak.paybak", category: "Photos")

    static var folder: URL {
        URL.applicationSupportDirectory.appending(path: "photos", directoryHint: .isDirectory)
    }

    /// Saves a picked image as a JPEG (longest side 2048 pt) and returns its file name.
    static func save(_ image: UIImage) throws -> String {
        try FileManager.default.createDirectory(at: folder, withIntermediateDirectories: true)
        let name = RecordID.make() + ".jpg"
        guard let data = scaled(image).jpegData(compressionQuality: 0.8) else { throw CocoaError(.fileWriteUnknown) }
        try data.write(to: folder.appending(path: name), options: .atomic)
        return name
    }

    /// Loads a picked photo; nil (logged) if it can't be read or saved.
    static func save(_ data: Data) -> String? {
        guard let image = UIImage(data: data) else { return nil }
        do {
            return try save(image)
        } catch {
            log.error("Could not save a photo: \(String(describing: error), privacy: .public)")
            return nil
        }
    }

    static func image(_ ref: PhotoRef) -> Image? {
        switch ref {
        case .asset(let name):
            return Image(name)
        case .file(let name):
            return UIImage(contentsOfFile: folder.appending(path: name).path()).map(Image.init(uiImage:))
        }
    }

    private static func scaled(_ image: UIImage) -> UIImage {
        let longest = max(image.size.width, image.size.height)
        guard longest > 2048 else { return image }
        let scale = 2048 / longest
        let size = CGSize(width: image.size.width * scale, height: image.size.height * scale)
        return UIGraphicsImageRenderer(size: size).image { _ in image.draw(in: CGRect(origin: .zero, size: size)) }
    }
}

extension Receipt {
    /// The photo to show: the saved file, else the bundled demo image.
    var photoRef: PhotoRef? {
        if let photo { return .file(photo) }
        return asset.map { .asset($0 == "receipt-thumb" ? "art-receipt-full" : $0) }
    }
}

/// A receipt's picture, falling back to the receipt line art.
struct ReceiptImage: View {
    let receipt: Receipt?

    var body: some View {
        if let image = receipt?.photo.flatMap({ PhotoFiles.image(.file($0)) }) {
            image.resizable().scaledToFill()
        } else {
            Image("art-receipt-thumb").resizable()
        }
    }
}
