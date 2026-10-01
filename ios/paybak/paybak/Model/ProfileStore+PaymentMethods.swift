import Foundation

/// Payment methods (screens-settings §4, §12.2): exactly one is primary, friends only see the primary
/// one, and `upiID` mirrors the primary UPI ID for the screens that read it.
extension ProfileStore {
    enum PaymentMethodError: Error, Equatable {
        /// Not `name@bank`.
        case invalidUPI
        case duplicateUPI
        case missingBankName
        /// Fewer than 4 digits.
        case invalidAccountNumber
    }

    /// Adds a UPI ID; the first method becomes primary.
    @discardableResult
    func addUPIMethod(_ text: String) throws(PaymentMethodError) -> PaymentMethod {
        let upi = text.trimmingCharacters(in: .whitespacesAndNewlines)
        guard UPIID.isValid(upi) else { throw .invalidUPI }
        guard !profile.paymentMethods.contains(where: { $0.kind == .upi && $0.value?.caseInsensitiveCompare(upi) == .orderedSame }) else {
            throw .duplicateUPI
        }
        let method = PaymentMethod.upi(upi, primary: profile.paymentMethods.isEmpty)
        add(method)
        return method
    }

    /// Adds a bank account, shown as "{bank} ···· {last 4}"; the first method becomes primary.
    @discardableResult
    func addBankMethod(bankName: String, accountNumber: String) throws(PaymentMethodError) -> PaymentMethod {
        let name = bankName.trimmingCharacters(in: .whitespacesAndNewlines)
        let digits = accountNumber.filter(\.isNumber)
        guard !name.isEmpty else { throw .missingBankName }
        guard digits.count >= 4 else { throw .invalidAccountNumber }
        let method = PaymentMethod(id: RecordID.make(), kind: .bank, value: nil, bankName: name,
                                   last4: String(digits.suffix(4)), primary: profile.paymentMethods.isEmpty)
        add(method)
        return method
    }

    func makePrimary(_ id: String) {
        update { profile in
            for index in profile.paymentMethods.indices {
                profile.paymentMethods[index].primary = profile.paymentMethods[index].id == id
            }
            profile.mirrorPrimaryUPI()
        }
    }

    /// Removes a method; removing the primary makes the next one primary.
    func removePaymentMethod(_ id: String) {
        update { profile in
            let wasPrimary = profile.paymentMethods.first { $0.id == id }?.primary ?? false
            profile.paymentMethods.removeAll { $0.id == id }
            if wasPrimary, !profile.paymentMethods.isEmpty {
                profile.paymentMethods[0].primary = true
            }
            profile.mirrorPrimaryUPI()
        }
    }

    func setShowPaymentToFriends(_ isOn: Bool) {
        update { $0.showPaymentToFriends = isOn }
    }

    private func add(_ method: PaymentMethod) {
        update { profile in
            profile.paymentMethods.append(method)
            profile.mirrorPrimaryUPI()
        }
    }
}

extension UserProfile {
    /// `upiID` follows the primary method when it's a UPI ID ("" otherwise), so Setup 3 and the
    /// migration keep agreeing with the methods list.
    mutating func mirrorPrimaryUPI() {
        upiID = primaryPaymentMethod.flatMap { $0.kind == .upi ? $0.value : nil } ?? ""
    }

    /// The Add sheet's prefill (§5 caption 12-04): the primary UPI ID's name at the handle of a bank
    /// that has no UPI ID yet ("arjun" + HDFC → "arjun@okhdfcbank"); "" when there's nothing to derive.
    var suggestedUPIID: String {
        guard let name = paymentMethods.first(where: { $0.kind == .upi })?.value?.split(separator: "@").first else { return "" }
        let taken = Set(paymentMethods.compactMap { $0.kind == .upi ? $0.value?.lowercased() : nil })
        for bank in paymentMethods where bank.kind == .bank {
            guard let handle = bank.bankName.flatMap(PaymentMethod.upiHandle(forBank:)) else { continue }
            let candidate = "\(name)@\(handle)"
            if !taken.contains(candidate.lowercased()) {
                return candidate
            }
        }
        return ""
    }
}

nonisolated extension PaymentMethod {
    /// "arjun@okaxis" / "HDFC Bank ···· 4821".
    var title: String {
        switch kind {
        case .upi: value ?? ""
        case .bank: "\(bankName ?? "Bank") ···· \(last4 ?? "")"
        }
    }

    /// "UPI · Primary" / "Bank transfer".
    var subtitle: String {
        let kindLabel = kind == .upi ? "UPI" : "Bank transfer"
        return primary ? "\(kindLabel) · Primary" : kindLabel
    }

    /// The Profile row's value: "UPI" or "Bank".
    var shortLabel: String { kind == .upi ? "UPI" : "Bank" }

    /// What friends copy: the UPI ID, or the bank and last 4 digits.
    var shareValue: String { title }

    /// The UPI handle of a bank's own app (HDFC → okhdfcbank), for the Add sheet's prefill.
    static func upiHandle(forBank name: String) -> String? {
        let folded = name.lowercased()
        let handles = [("hdfc", "okhdfcbank"), ("icici", "okicici"), ("sbi", "oksbi"), ("state bank", "oksbi"), ("axis", "okaxis")]
        return handles.first { folded.contains($0.0) }?.1
    }
}
