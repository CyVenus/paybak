import Foundation

extension ProfileStore {
    /// Sign out (screens-profile §2.3): forgets how the user signed in and that onboarding finished,
    /// but keeps the profile and the ledger on the device.
    func signOut() {
        update { profile in
            profile.signInMethod = nil
            profile.contact = nil
            profile.onboardingComplete = false
        }
    }
}

extension UserProfile {
    /// The line under the name on Profile (§2.2): the primary UPI ID, else the sign-in email or phone;
    /// nil hides the line (as on Android: a UPI ID that isn't primary doesn't show).
    var handle: String? {
        if !upiID.isEmpty {
            return upiID
        }
        return contact.flatMap { $0.isEmpty ? nil : $0 }
    }
}
