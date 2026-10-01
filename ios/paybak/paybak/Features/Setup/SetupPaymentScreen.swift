import SwiftUI
import UIKit

/// Setup 3 — Payment (screens-setup.md §3), optional: a UPI ID and a live preview of what friends
/// see, with a copy button ("UPI ID copied" toast). Continue saves the UPI ID (or none) and goes to
/// Setup 4; text that isn't a UPI ID shows the field's error until it's edited. Skip goes on without
/// saving. The keyboard covers the footer so the preview stays visible while typing.
struct SetupPaymentScreen: View {
    @Environment(AppRouter.self) private var router
    @Environment(ProfileStore.self) private var profileStore

    @State private var upiID = ""
    @State private var isInvalid = false
    @State private var toast: PBToastMessage?
    @FocusState private var isFieldFocused: Bool

    private var trimmedUPIID: String { upiID.trimmingCharacters(in: .whitespacesAndNewlines) }

    var body: some View {
        VStack(spacing: 0) {
            PBSetupHeader(step: 3, onBack: router.pop, onSkip: skip)
                .padding(.horizontal, PBLayout.screenMargin)
            ScrollView {
                content
                    .padding(.horizontal, PBLayout.screenMargin)
                    .padding(.bottom, PBSpace.s24)
            }
            .scrollBounceBehavior(.basedOnSize)
            .scrollDismissesKeyboard(.interactively)
            .safeAreaInset(edge: .bottom, spacing: 0) {
                PBButton("Continue", fillsWidth: true, action: next)
                    .accessibilityIdentifier("setup3.continue")
                    .padding(.horizontal, PBLayout.screenMargin)
            }
        }
        // 16 pt above Continue (the Toast's "50 above the bottom edge" would cover it).
        .pbToast($toast, bottomPadding: PBSize.buttonLg + PBSpace.s16)
        .phoneContentWidth()
        .background(PBColor.bgPrimary)
        // Tapping empty space dismisses the keyboard.
        .contentShape(.rect)
        .onTapGesture { isFieldFocused = false }
        .ignoresSafeArea(.keyboard, edges: .bottom)
        .sensoryFeedback(.success, trigger: toast) { _, toast in toast != nil }
        .screenIdentifier(.setup3)
        .onChange(of: upiID) { isInvalid = false }
        .onAppear(perform: loadProfile)
    }

    private var content: some View {
        VStack(alignment: .leading, spacing: 0) {
            VStack(alignment: .leading, spacing: PBSpace.s12) {
                Text("How should friends\npay you?")
                    .textStyle(.title1)
                    .foregroundStyle(PBColor.textPrimary)
                    .accessibilityAddTraits(.isHeader)
                Text("Add your UPI ID. Friends see it when they settle up — Paybak never moves money.")
                    .textStyle(.body)
                    .foregroundStyle(PBColor.textSecondary)
            }
            .padding(.top, PBSpace.s24)
            PBTextField(
                "UPI ID",
                text: $upiID,
                prompt: PBPaymentPreview.upiPlaceholder,
                helper: "Only people you share expenses with can see it.",
                error: isInvalid ? "Enter a UPI ID like name@bank." : nil,
                focus: $isFieldFocused
            )
            .keyboardType(.emailAddress)
            .textInputAutocapitalization(.never)
            .autocorrectionDisabled()
            .submitLabel(.done)
            .accessibilityIdentifier("setup3.upi")
            .padding(.top, PBSpace.s24)
            PBPaymentPreview(
                avatar: profileStore.avatarContent,
                name: profileStore.profile.name,
                upiID: trimmedUPIID,
                onCopy: copy,
                testIDPrefix: "setup3"
            )
            .padding(.top, PBSpace.s24)
        }
    }

    /// Starts from the saved UPI ID on every arrival (the debug seed, or Back from Setup 4, which
    /// drops text that Skip didn't save), as on Android.
    private func loadProfile() {
        upiID = profileStore.profile.upiID
        isInvalid = false
    }

    /// Copying ends editing, so the keyboard doesn't hide the toast.
    private func copy() {
        isFieldFocused = false
        UIPasteboard.general.string = trimmedUPIID
        toast = PBToastMessage("UPI ID copied")
    }

    private func next() {
        guard trimmedUPIID.isEmpty || UPIID.isValid(trimmedUPIID) else {
            isInvalid = true
            return
        }
        profileStore.update { $0.upiID = trimmedUPIID }
        isFieldFocused = false
        router.push(.setup4)
    }

    /// Goes on without saving what was typed; a UPI ID saved earlier stays.
    private func skip() {
        isFieldFocused = false
        router.push(.setup4)
    }
}

#Preview("SetupPaymentScreen") {
    SetupPaymentScreen()
        .environment(AppRouter())
        .environment(ProfileStore())
}
