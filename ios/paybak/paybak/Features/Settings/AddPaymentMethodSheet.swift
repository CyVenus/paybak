import SwiftUI

/// Add payment method (screens-settings §5): a UPI ID, or a bank account on the second tab. The UPI
/// field is focused on open and prefilled from a bank that has no UPI ID yet. Save adds the method;
/// an ID without "@" shows the inline error, which clears as soon as the field changes.
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
    @State private var error: String?
    @FocusState private var isUPIFocused: Bool
    @FocusState private var isBankFocused: Bool
    @FocusState private var isAccountFocused: Bool

    private static let upiError = "Enter a UPI ID like name@bank"

    init(start: Start, onClose: @escaping () -> Void) {
        self.onClose = onClose
        _upiID = State(initialValue: start.upiID)
        _error = State(initialValue: start.showsError ? Self.upiError : nil)
    }

    var body: some View {
        PBSheet(title: "Add payment method", testIDPrefix: "paymentAddUpi", onClose: onClose) {
            VStack(spacing: PBSpace.s16) {
                PBSegmentedControl(options: ["UPI ID", "Bank account"], selection: $tab,
                                   testIDs: ["paymentAddUpi.segment.upi", "paymentAddUpi.segment.bank"])
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
        .sensoryFeedback(.error, trigger: error) { _, new in new != nil }
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier("paymentAddUpi.sheet")
        .onAppear { isUPIFocused = true }
        .onChange(of: tab) { _, tab in
            error = nil
            if tab == 0 { isUPIFocused = true } else { isBankFocused = true }
        }
        .onChange(of: upiID) { error = nil }
        .onChange(of: bankName) { error = nil }
        .onChange(of: accountNumber) { error = nil }
    }

    private var upiForm: some View {
        PBTextField("UPI ID", text: $upiID, prompt: PBPaymentPreview.upiPlaceholder,
                    helper: "Friends copy this to pay you in their UPI app.", error: error, icon: .wallet,
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
                        helper: "Friends see the bank and the last 4 digits.", error: error,
                        errorShowsIcon: true, focus: $isAccountFocused)
                .keyboardType(.numberPad)
                .accessibilityIdentifier("paymentAddUpi.accountNumber")
        }
    }

    private var canSave: Bool {
        if tab == 0 { return !upiID.trimmingCharacters(in: .whitespaces).isEmpty }
        return !bankName.trimmingCharacters(in: .whitespaces).isEmpty && !accountNumber.isEmpty
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
            let message = switch error {
            case .invalidUPI: Self.upiError
            case .duplicateUPI: "You’ve already added this UPI ID"
            case .missingBankName: "Enter the bank’s name"
            case .invalidAccountNumber: "Enter the full account number"
            }
            self.error = message
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
