import Foundation
import Observation
import UIKit
import os

/// Loads and saves the `UserProfile` in UserDefaults, and the custom avatar photo in
/// Application Support. Every change is written straight away.
@Observable
final class ProfileStore {
    private(set) var profile: UserProfile

    @ObservationIgnored private let defaults: UserDefaults
    @ObservationIgnored private let photoURL: URL

    private static let profileKey = "userProfile"
    /// Setup 1 saves photos as JPEGs of at most this many pixels on the long side.
    private static let photoMaxPixels: CGFloat = 512
    private static let log = Logger(subsystem: "app.paybak.paybak", category: "ProfileStore")

    init(defaults: UserDefaults = .standard) {
        self.defaults = defaults
        photoURL = URL.applicationSupportDirectory.appending(path: "avatar-photo.jpg")
        profile = defaults.data(forKey: Self.profileKey)
            .flatMap { try? JSONDecoder().decode(UserProfile.self, from: $0) } ?? UserProfile()
    }

    /// Applies `change` to the profile and saves it.
    func update(_ change: (inout UserProfile) -> Void) {
        change(&profile)
        save()
    }

    /// Replaces the whole profile (the debug seed) and saves it.
    func replace(with profile: UserProfile) {
        self.profile = profile
        save()
    }

    /// Clears the saved profile and photo ("Reset onboarding").
    func reset() {
        profile = UserProfile()
        defaults.removeObject(forKey: Self.profileKey)
        try? FileManager.default.removeItem(at: photoURL)
    }

    // MARK: Photo

    /// Scales the photo down to 512 px, saves it as a JPEG and makes it the avatar.
    func savePhoto(_ image: UIImage) throws {
        let scale = min(1, Self.photoMaxPixels / max(image.size.width * image.scale, image.size.height * image.scale))
        let size = CGSize(width: image.size.width * image.scale * scale, height: image.size.height * image.scale * scale)
        let format = UIGraphicsImageRendererFormat()
        format.scale = 1
        let resized = UIGraphicsImageRenderer(size: size, format: format).image { _ in
            image.draw(in: CGRect(origin: .zero, size: size))
        }
        guard let data = resized.jpegData(compressionQuality: 0.85) else {
            throw CocoaError(.fileWriteUnknown)
        }
        try FileManager.default.createDirectory(at: photoURL.deletingLastPathComponent(), withIntermediateDirectories: true)
        try data.write(to: photoURL, options: .atomic)
        update { $0.avatar = .photo }
    }

    /// The saved avatar photo, if the profile uses one.
    func loadPhoto() -> UIImage? {
        guard profile.avatar == .photo else { return nil }
        return UIImage(contentsOfFile: photoURL.path(percentEncoded: false))
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
