import SwiftUI
import UIKit

/// The custom character's Head crop drawn once into a bitmap, for circles that take an image (rows,
/// previews and stacks; screens-profile §1.7 "Performance"). Cached by look and size, so a list of
/// "You" avatars composes the 12 vector layers only once.
enum AvatarBitmap {
    private static let cache = NSCache<NSString, UIImage>()

    /// The Head crop of `look` in a transparent square of `side` points at 3× (enough for every circle
    /// up to the Profile header).
    static func head(_ look: AvatarLook, side: CGFloat = 120) -> UIImage? {
        let key = "\(side)|\(look)" as NSString
        if let image = cache.object(forKey: key) {
            return image
        }
        let renderer = ImageRenderer(content: AvatarCharacterView(look: look, crop: .head).frame(width: side, height: side))
        renderer.scale = 3
        guard let image = renderer.uiImage else { return nil }
        cache.setObject(image, forKey: key)
        return image
    }
}
