import Foundation
import Testing
@testable import paybak

/// Payment methods and the Pro status line (screens-settings §4–5, §3, §12).
@MainActor
struct PaymentMethodsTests {
    private let store: ProfileStore

    init() throws {
        let suite = "PaymentMethodsTests.\(UUID().uuidString)"
        store = ProfileStore(defaults: try #require(UserDefaults(suiteName: suite)),
                             directory: FileManager.default.temporaryDirectory.appending(path: suite))
    }

    /// The demo's methods: arjun@okaxis (primary) and HDFC Bank ···· 4821.
    private func loadDemoMethods() {
        store.replace(with: .sample)
        store.update { profile in
            profile.paymentMethods.append(PaymentMethod(id: "pm-hdfc", kind: .bank, value: nil, bankName: "HDFC Bank",
                                                        last4: "4821", primary: false))
        }
    }

    @Test func rowCopy() {
        loadDemoMethods()
        let methods = store.profile.paymentMethods
        #expect(methods.map(\.title) == ["arjun@okaxis", "HDFC Bank ···· 4821"])
        #expect(methods.map(\.subtitle) == ["UPI · Primary", "Bank transfer"])
        #expect(store.profile.primaryPaymentMethod?.shortLabel == "UPI")
        #expect(store.profile.handle == "arjun@okaxis")
    }

    @Test func prefillComesFromTheBank() throws {
        loadDemoMethods()
        #expect(store.profile.suggestedUPIID == "arjun@okhdfcbank")
        try store.addUPIMethod("arjun@okhdfcbank")
        #expect(store.profile.suggestedUPIID == "")
        #expect(store.profile.paymentMethods.last?.primary == false)
    }

    @Test func validation() {
        loadDemoMethods()
        #expect(throws: ProfileStore.PaymentMethodError.invalidUPI) { try store.addUPIMethod("arjunokhdfcbank") }
        #expect(throws: ProfileStore.PaymentMethodError.duplicateUPI) { try store.addUPIMethod(" ARJUN@okaxis ") }
        #expect(throws: ProfileStore.PaymentMethodError.invalidAccountNumber) {
            try store.addBankMethod(bankName: "ICICI Bank", accountNumber: "12")
        }
        #expect(store.profile.paymentMethods.count == 2)
    }

    @Test func firstMethodIsPrimary() throws {
        try store.addBankMethod(bankName: "SBI", accountNumber: "0000 1234 5678")
        #expect(store.profile.paymentMethods.map(\.title) == ["SBI ···· 5678"])
        #expect(store.profile.paymentMethods[0].primary)
        #expect(store.profile.upiID == "")
    }

    @Test func primaryMovesAndUPIFollows() throws {
        loadDemoMethods()
        store.makePrimary("pm-hdfc")
        #expect(store.profile.primaryPaymentMethod?.id == "pm-hdfc")
        #expect(store.profile.upiID == "")
        // A UPI ID that isn't primary doesn't show under the name (as on Android).
        #expect(store.profile.handle == "arjun@example.com")
        store.removePaymentMethod("pm-hdfc")
        #expect(store.profile.paymentMethods.map(\.primary) == [true])
        #expect(store.profile.upiID == "arjun@okaxis")
        store.removePaymentMethod("pm-upi")
        #expect(store.profile.paymentMethods.isEmpty)
        #expect(store.profile.handle == "arjun@example.com")
    }

    @Test func signOutKeepsTheProfile() {
        loadDemoMethods()
        store.update { $0.onboardingComplete = true }
        store.signOut()
        #expect(!store.profile.onboardingComplete)
        #expect(store.profile.signInMethod == nil)
        #expect(store.profile.name == "Arjun Mehta")
        #expect(store.profile.paymentMethods.count == 2)
    }

    @Test func proStatusLine() {
        let calendar = DemoFixture.calendar
        let today = DemoFixture.figmaDay
        let trial = Entitlement(plan: .pro, period: .yearly, trialEndsAt: today.adding(days: 7), since: DemoFixture.figmaNow)
        #expect(trial.statusLine(today: today, calendar: calendar) == "Your free trial ends Wed 7 Oct. Then ₹799/year.")
        let monthly = Entitlement(plan: .pro, period: .monthly, trialEndsAt: nil, since: DemoFixture.figmaNow)
        #expect(monthly.statusLine(today: today, calendar: calendar) == "Your subscription renews Fri 30 Oct. ₹99/month.")
        // After the trial, the yearly plan renews a year after it ended.
        #expect(trial.statusLine(today: today.adding(days: 8), calendar: calendar)
            == "Renews \(Format.day(DemoFixture.day(2027, 10, 7))). ₹799/year.")
        #expect(Entitlement().statusLine(today: today, calendar: calendar) == nil)
    }

    @Test func currencySubtitle() {
        #expect(CurrencySettingsScreen.sentenceCase("Indian Rupee") == "Indian rupee")
        #expect(CurrencySettingsScreen.sentenceCase("Euro") == "Euro")
        #expect(CurrencySettingsScreen.sentenceCase("US Dollar") == "US dollar")
        #expect(CurrencySettingsScreen.sentenceCase("United Arab Emirates Dirham") == "United arab emirates dirham")
    }
}
