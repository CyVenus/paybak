import Foundation
import Testing
@testable import paybak

/// M1 profiles load into the M2 profile: new fields default, the UPI ID becomes the primary method.
@MainActor
struct UserProfileMigrationTests {
    @Test func m1ProfileGainsItsPrimaryUPIMethod() throws {
        let saved = Data(#"{"name":"Arjun Mehta","avatar":{"preset":{"_0":0}},"currencyCode":"INR","upiID":"arjun@okaxis","signInMethod":"email","contact":"arjun@example.com","onboardingComplete":true}"#.utf8)
        let profile = try JSONDecoder().decode(UserProfile.self, from: saved)
        #expect(profile.name == "Arjun Mehta")
        #expect(profile.avatar == .preset(0))
        #expect(profile.onboardingComplete)
        #expect(profile.showPaymentToFriends)
        #expect(profile.paymentMethods == [PaymentMethod(id: "pm-upi", kind: .upi, value: "arjun@okaxis", primary: true)])
        #expect(profile.defaultCurrency == "INR")
    }

    @Test func characterAvatarRoundTrips() throws {
        var profile = UserProfile.sample
        profile.avatar = .character(.defaultBoy)
        let decoded = try JSONDecoder().decode(UserProfile.self, from: JSONEncoder().encode(profile))
        #expect(decoded == profile)
    }

    @Test func editingTheUPIIDMovesThePrimaryMethod() {
        var profile = UserProfile()
        profile.upiID = "a@okaxis"
        profile.syncPrimaryUPI()
        profile.upiID = "b@okaxis"
        profile.syncPrimaryUPI()
        #expect(profile.primaryPaymentMethod?.value == "b@okaxis")
        #expect(profile.paymentMethods.count == 1)
    }
}
