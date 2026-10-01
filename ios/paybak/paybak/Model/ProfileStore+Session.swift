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
    /// The line under the name on Profile (§2.2): the primary UPI ID (or any UPI ID), else the sign-in
    /// email or phone; nil hides the line.
    var handle: String? {
        let upi = [primaryPaymentMethod].compactMap(\.self).filter { $0.kind == .upi } + paymentMethods.filter { $0.kind == .upi }
        if let value = upi.first?.value, !value.isEmpty {
            return value
        }
        return contact.flatMap { $0.isEmpty ? nil : $0 }
    }
}
