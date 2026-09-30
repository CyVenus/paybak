import CoreImage
import CoreImage.CIFilterBuiltins
import SwiftUI

/// Card / QR Code (Figma 130:1912): a real, scannable QR code of the user's invite link on a white
/// 240 pt card with 20 pt corners. The matrix is 198 pt square (a 21 pt quiet zone) in
/// `text/primary`, error correction H, so the app mark in its 52 pt white backing in the centre
/// still scans. Generated with CoreImage, never shipped as an image.
struct PBQRCodeCard: View {
    /// e.g. "https://paybak.app/i/arjun".
    let link: String
    var showsMark = true
    private let matrix: CGImage?

    init(link: String, showsMark: Bool = true) {
        self.link = link
        self.showsMark = showsMark
        matrix = QRMatrix.image(for: link)
    }

    var body: some View {
        ZStack {
            if let matrix {
                Image(decorative: matrix, scale: 1)
                    .interpolation(.none)
                    .resizable()
                    .frame(width: 198, height: 198)
            }
            if showsMark {
                PBAppMark(size: 40)
                    .frame(width: 52, height: 52)
                    .background(PBColor.bgPrimary, in: .rect(cornerRadius: PBRadius.input))
            }
        }
        .frame(width: 240, height: 240)
        .background(PBColor.bgPrimary, in: .rect(cornerRadius: PBRadius.card))
        .accessibilityElement()
        .accessibilityLabel("QR code for \(link)")
        .accessibilityAddTraits(.isImage)
    }
}

/// Builds the QR matrix image: one pixel per module, no quiet zone, `text/primary` on white.
private enum QRMatrix {
    private static let context = CIContext()

    static func image(for text: String) -> CGImage? {
        let generator = CIFilter.qrCodeGenerator()
        generator.message = Data(text.utf8)
        generator.correctionLevel = "H"
        let colours = CIFilter.falseColor()
        colours.inputImage = generator.outputImage
        colours.color0 = CIColor(red: 10 / 255, green: 10 / 255, blue: 10 / 255)
        colours.color1 = CIColor(red: 1, green: 1, blue: 1)
        guard let output = colours.outputImage,
              let image = context.createCGImage(output, from: output.extent.integral)
        else { return nil }
        // CoreImage adds the same white margin on every side; crop it (the card draws the quiet zone).
        let margin = quietZone(of: image)
        let side = image.width - 2 * margin
        return image.cropping(to: CGRect(x: margin, y: margin, width: side, height: side))
    }

    /// The width of the white border: the first column, from the left, with a dark module.
    private static func quietZone(of image: CGImage) -> Int {
        let width = image.width
        let height = image.height
        var pixels = [UInt8](repeating: 255, count: width * height)
        let drawn = pixels.withUnsafeMutableBytes { buffer -> Bool in
            guard let bitmap = CGContext(
                data: buffer.baseAddress,
                width: width,
                height: height,
                bitsPerComponent: 8,
                bytesPerRow: width,
                space: CGColorSpaceCreateDeviceGray(),
                bitmapInfo: CGImageAlphaInfo.none.rawValue
            ) else { return false }
            bitmap.interpolationQuality = .none
            bitmap.draw(image, in: CGRect(x: 0, y: 0, width: width, height: height))
            return true
        }
        guard drawn else { return 0 }
        for x in 0..<width where (0..<height).contains(where: { pixels[$0 * width + x] < 128 }) {
            return x
        }
        return 0
    }
}

#Preview("PBQRCodeCard") {
    VStack(spacing: PBSpace.s24) {
        PBQRCodeCard(link: "https://paybak.app/i/arjun")
        PBQRCodeCard(link: "https://paybak.app/i/arjun", showsMark: false)
    }
    .padding(PBSpace.s24)
    .background(PBColor.bgCamera)
}
