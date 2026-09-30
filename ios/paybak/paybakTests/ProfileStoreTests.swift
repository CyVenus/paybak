import Foundation
import Testing
import UIKit
@testable import paybak

/// Each test gets its own UserDefaults suite and photo directory.
@MainActor
struct ProfileStoreTests {
    private let suiteName = "ProfileStoreTests.\(UUID().uuidString)"
    private let defaults: UserDefaults
    private let directory: URL

    init() throws {
        defaults = try #require(UserDefaults(suiteName: suiteName))
        directory = FileManager.default.temporaryDirectory.appending(path: suiteName)
    }

    private func makeStore() -> ProfileStore {
        ProfileStore(defaults: defaults, directory: directory)
    }

    @Test func startsEmpty() {
        let store = makeStore()
        #expect(store.profile == UserProfile())
        #expect(store.photo == nil)
    }

    @Test func savesEveryChange() {
        let store = makeStore()
        store.update {
            $0.name = "Arjun Mehta"
            $0.avatar = .preset(2)
            $0.currencyCode = "INR"
            $0.upiID = "arjun@okaxis"
            $0.notifications = .allowed
            $0.signInMethod = .phone
            $0.contact = "+91 98765 43210"
            $0.onboardingComplete = true
        }

        #expect(makeStore().profile == store.profile)
    }

    @Test func resetClearsTheProfileAndPhoto() throws {
        let store = makeStore()
        try store.savePhoto(image(width: 40, height: 40))
        store.update { $0.name = "Arjun" }

        store.reset()

        #expect(store.profile == UserProfile())
        #expect(store.photo == nil)
        #expect(makeStore().profile == UserProfile())
    }

    @Test func savedPhotoBecomesTheAvatarAndIsReloaded() throws {
        let store = makeStore()
        try store.savePhoto(image(width: 1200, height: 800))

        #expect(store.profile.avatar == .photo)
        #expect(store.photo != nil)
        let reloaded = try #require(makeStore().photo)
        #expect(reloaded.size.width * reloaded.scale == ProfileStore.photoMaxPixels)
    }

    @Test func choosingArtDropsThePhoto() throws {
        let store = makeStore()
        try store.savePhoto(image(width: 40, height: 40))
        store.update { $0.avatar = .preset(0) }
        #expect(store.photo == nil)
    }

    @Test func avatarPhotoIsACentredSquareOfAtMost512Pixels() {
        let large = ProfileStore.avatarPhoto(from: image(width: 1200, height: 800))
        #expect(large.size == CGSize(width: 512, height: 512))
        #expect(large.scale == 1)

        let small = ProfileStore.avatarPhoto(from: image(width: 300, height: 200))
        #expect(small.size == CGSize(width: 200, height: 200))
    }

    private func image(width: CGFloat, height: CGFloat) -> UIImage {
        let format = UIGraphicsImageRendererFormat()
        format.scale = 1
        return UIGraphicsImageRenderer(size: CGSize(width: width, height: height), format: format).image { context in
            UIColor.systemTeal.setFill()
            context.fill(CGRect(x: 0, y: 0, width: width, height: height))
        }
    }
}
