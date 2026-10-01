import Foundation
import Observation
import UIKit
import os

/// Loads and saves the `UserProfile` in UserDefaults, and the custom avatar photo in
/// Application Support. Every change is written straight away.
@Observable
final class ProfileStore {
    private(set) var profile: UserProfile
    /// The avatar photo, while the profile uses one.
    private(set) var photo: UIImage?

    /// Where the avatar photo is saved.
    @ObservationIgnored let photoURL: URL
    @ObservationIgnored private let defaults: UserDefaults

    private static let profileKey = "userProfile"
    /// Setup 1 saves photos as square JPEGs of at most this many pixels a side.
    static let photoMaxPixels: CGFloat = 512
    private static let log = Logger(subsystem: "app.paybak.paybak", category: "ProfileStore")

    /// - Parameters:
    ///   - defaults: Where the profile is saved.
    ///   - directory: Where the avatar photo is saved.
    init(defaults: UserDefaults = .standard, directory: URL = .applicationSupportDirectory) {
        self.defaults = defaults
        photoURL = directory.appending(path: "avatar-photo.jpg")
        profile = defaults.data(forKey: Self.profileKey)
            .flatMap { try? JSONDecoder().decode(UserProfile.self, from: $0) } ?? UserProfile()
        if profile.avatar == .photo {
            photo = UIImage(contentsOfFile: photoURL.path(percentEncoded: false))
        }
    }

    /// Applies `change` to the profile and saves it. Choosing another avatar deletes the photo, so the
    /// user's picture doesn't stay on the device once nothing shows it.
    func update(_ change: (inout UserProfile) -> Void) {
        let hadPhoto = profile.avatar == .photo
        let oldUPI = profile.upiID
        change(&profile)
        if profile.upiID != oldUPI {
            profile.syncPrimaryUPI()
        }
        // A named user without a username gets their lowercase first name, kept from then on (as on
        // Android), so a later rename doesn't change the invite link.
        if (profile.username ?? "").isEmpty, !profile.firstName.isEmpty {
            profile.username = UserProfile.defaultUsername(for: profile.name)
        }
        if hadPhoto, profile.avatar != .photo {
            photo = nil
            try? FileManager.default.removeItem(at: photoURL)
        }
        save()
    }

    /// Replaces the whole profile (the debug seed) and saves it.
    func replace(with profile: UserProfile) {
        update { $0 = profile }
    }

    /// Clears the saved profile and photo ("Reset onboarding").
    func reset() {
        profile = UserProfile()
        photo = nil
        defaults.removeObject(forKey: Self.profileKey)
        try? FileManager.default.removeItem(at: photoURL)
    }

    // MARK: Photo

    /// Crops the photo to a centred square of at most 512 px, saves it as a JPEG and makes it the
    /// avatar.
    func savePhoto(_ image: UIImage) throws {
        guard let data = Self.avatarPhoto(from: image).jpegData(compressionQuality: 0.85) else {
            throw CocoaError(.fileWriteUnknown)
        }
        try FileManager.default.createDirectory(at: photoURL.deletingLastPathComponent(), withIntermediateDirectories: true)
        try data.write(to: photoURL, options: .atomic)
        update { $0.avatar = .photo }
        photo = UIImage(data: data)
    }

    /// The centred square of `image`, scaled down to at most `photoMaxPixels` a side (never up),
    /// at scale 1, upright.
    static func avatarPhoto(from image: UIImage) -> UIImage {
        let side = min(image.size.width, image.size.height)
        let pixels = min(photoMaxPixels, (side * image.scale).rounded(.down))
        let scale = pixels / side
        let drawn = CGSize(width: image.size.width * scale, height: image.size.height * scale)
        let format = UIGraphicsImageRendererFormat()
        format.scale = 1
        return UIGraphicsImageRenderer(size: CGSize(width: pixels, height: pixels), format: format).image { _ in
            image.draw(in: CGRect(
                x: (pixels - drawn.width) / 2,
                y: (pixels - drawn.height) / 2,
                width: drawn.width,
                height: drawn.height
            ))
        }
    }

    // MARK: Private

    private func save() {
        do {
            defaults.set(try JSONEncoder().encode(profile), forKey: Self.profileKey)
        } catch {
            Self.log.error("Could not save the profile: \(String(describing: error), privacy: .public)")
        }
    }
}
