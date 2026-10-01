import SwiftUI

/// Add payment method (screens-settings §5): a UPI ID, or a bank account on the second tab. The UPI
/// field is focused on open and prefilled from a bank that has no UPI ID yet. Save adds the method;
/// an ID without "@" shows the inline error, which clears as soon as the field changes. Each tab keeps
/// its own error.
struct AddPaymentMethodSheet: View {
    /// How the sheet opens (the debug start screens open it mid-edit).
    struct Start: Equatable {
        var upiID: String
        var showsError = false

        init(profile: UserProfile) {
            upiID = profile.suggestedUPIID
        }
    }

    let onClose: () -> Void

    @Environment(AppRouter.self) private var router
    @Environment(ProfileStore.self) private var profileStore

    @State private var tab = 0
    @State private var upiID: String
    @State private var bankName = ""
    @State private var accountNumber = ""
    @State private var upiError: String?
    @State private var bankError: String?
    @FocusState private var isUPIFocused: Bool
    @FocusState private var isBankFocused: Bool
    @FocusState private var isAccountFocused: Bool

    private static let upiMessage = "Enter a UPI ID like name@bank"
    private static let bankMessage = "Enter the bank name and at least 4 digits"

    init(start: Start, onClose: @escaping () -> Void) {
        self.onClose = onClose
        _upiID = State(initialValue: start.upiID)
        _upiError = State(initialValue: start.showsError ? Self.upiMessage : nil)
    }

    var body: some View {
        PBSheet(title: "Add payment method", testIDPrefix: "paymentAddUpi", onClose: onClose) {
            VStack(spacing: PBSpace.s16) {
                PBSegmentedControl(options: ["UPI ID", "Bank account"], selection: $tab, testIDPrefix: "paymentAddUpi.segment")
                VStack(spacing: PBSpace.s20) {
                    if tab == 0 {
                        upiForm
                    } else {
                        bankForm
                    }
                    PBButton("Save", fillsWidth: true, action: save)
                        .disabled(!canSave)
                        .accessibilityIdentifier("paymentAddUpi.save")
                }
            }
            .padding(.top, PBSpace.s8)
        }
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier("paymentAddUpi.sheet")
        .onAppear { isUPIFocused = true }
        .onChange(of: tab) { _, tab in
            if tab == 0 { isUPIFocused = true } else { isBankFocused = true }
        }
        .onChange(of: upiID) { upiError = nil }
        .onChange(of: bankName) { bankError = nil }
        .onChange(of: accountNumber) { _, number in
            // Digits only, typed or pasted.
            let digits = number.filter(\.isNumber)
            if digits != number {
                accountNumber = digits
            }
            bankError = nil
        }
    }

    private var upiForm: some View {
        PBTextField("UPI ID", text: $upiID, prompt: PBPaymentPreview.upiPlaceholder,
                    helper: "Friends copy this to pay you in their UPI app.", error: upiError, icon: .wallet,
                    errorShowsIcon: true, focus: $isUPIFocused)
            .keyboardType(.emailAddress)
            .textInputAutocapitalization(.never)
            .autocorrectionDisabled()
            .textContentType(.none)
            .submitLabel(.done)
            .onSubmit(save)
            .accessibilityIdentifier("paymentAddUpi.field")
    }

    private var bankForm: some View {
        VStack(spacing: PBSpace.s20) {
            PBTextField("Bank name", text: $bankName, prompt: "HDFC Bank", icon: .bank, focus: $isBankFocused)
                .textInputAutocapitalization(.words)
                .submitLabel(.next)
                .onSubmit { isAccountFocused = true }
                .accessibilityIdentifier("paymentAddUpi.bankName")
            PBTextField("Account number", text: $accountNumber, prompt: "Account number",
                        helper: "Friends see the bank and the last 4 digits.", error: bankError,
                        errorShowsIcon: true, focus: $isAccountFocused)
                .keyboardType(.numberPad)
                .accessibilityIdentifier("paymentAddUpi.accountNumber")
        }
    }

    /// UPI: anything typed. Bank: a bank name (the digits are checked on Save).
    private var canSave: Bool {
        if tab == 0 { return !upiID.trimmingCharacters(in: .whitespaces).isEmpty }
        return !bankName.trimmingCharacters(in: .whitespaces).isEmpty
    }

    private func save() {
        guard canSave else { return }
        do {
            if tab == 0 {
                try profileStore.addUPIMethod(upiID)
                router.toast("UPI ID added")
            } else {
                try profileStore.addBankMethod(bankName: bankName, accountNumber: accountNumber)
                router.toast("Bank account added")
            }
            onClose()
        } catch {
            let message: String
            switch error {
            case .invalidUPI:
                message = Self.upiMessage
                upiError = message
            case .duplicateUPI:
                message = "You’ve already added this UPI ID"
                upiError = message
            case .missingBankName, .invalidAccountNumber:
                message = Self.bankMessage
                bankError = message
            }
            Haptics.warning()
            AccessibilityNotification.Announcement(message).post()
        }
    }
}

#Preview("AddPaymentMethodSheet") {
    @Previewable @State var isPresented = true
    let profileStore = ProfileStore(defaults: UserDefaults(suiteName: "preview-add-method")!)
    PBButton("Add payment method") { isPresented = true }
        .pbSheet(isPresented: $isPresented) {
            AddPaymentMethodSheet(start: .init(profile: .sample)) { isPresented = false }
        }
        .environment(AppRouter())
        .environment(profileStore)
}
