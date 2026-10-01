import SwiftUI
import UIKit

/// Payment details (screens-settings §4): the user's methods (the primary one marked), Show to friends,
/// a preview of what friends see and the "never moves money" line. Add payment method opens the local
/// sheet (§5); tapping a method offers Make primary, Copy and Remove.
struct PaymentDetailsScreen: View {
    @Environment(AppRouter.self) private var router
    @Environment(ProfileStore.self) private var profileStore

    @State private var addSheet: AddPaymentMethodSheet.Start?
    @State private var actionsFor: PaymentMethod?
    @State private var removing: PaymentMethod?

    private var profile: UserProfile { profileStore.profile }

    var body: some View {
        SettingsScaffold(title: "Payment details", testIDPrefix: "paymentDetails") {
            VStack(alignment: .leading, spacing: PBSpace.s8) {
                PBSectionHeader("Your methods")
                VStack(spacing: 0) {
                    ForEach(Array(profile.paymentMethods.enumerated()), id: \.element.id) { index, method in
                        PBSheetRow(title: method.title, subtitle: method.subtitle, icon: method.kind == .upi ? .wallet : .bank,
                                   height: 64, horizontalPadding: 0) { actionsFor = method }
                            .accessibilityIdentifier("paymentDetails.method.\(index)")
                    }
                    PBSheetRow(title: "Add payment method", subtitle: "UPI ID or bank account", icon: .plus,
                               height: 64, horizontalPadding: 0) { addSheet = AddPaymentMethodSheet.Start(profile: profile) }
                        .accessibilityIdentifier("paymentDetails.add")
                }
            }
            PBSettingRow(
                "Show to friends",
                subtitle: "Friends see your primary method when they settle up with you.",
                trailing: .toggle(Binding { profile.showPaymentToFriends } set: { profileStore.setShowPaymentToFriends($0) }),
                showsDivider: false
            )
            .pbCard(padding: 0)
            .accessibilityIdentifier("paymentDetails.showToFriends")
            VStack(alignment: .leading, spacing: PBSpace.s8) {
                PBSectionHeader("What friends see")
                preview
            }
            SettingsInfoLine(icon: .lock, text: "Paybak never moves money. Friends copy these details and pay you in their own app.")
        }
        .pbSheet(isPresented: isPresented($actionsFor)) {
            if let actionsFor {
                actionsSheet(actionsFor)
            }
        }
        .pbAlert(
            isPresented: isPresented($removing),
            title: "Remove payment method?",
            message: "Friends won’t see it any more.",
            cancelLabel: "Cancel",
            actionLabel: "Remove",
            testIDPrefix: "paymentDetails.removeAlert"
        ) {
            if let removing { profileStore.removePaymentMethod(removing.id) }
        }
        .pbSheet(isPresented: isPresented($addSheet)) {
            if let addSheet {
                AddPaymentMethodSheet(start: addSheet) { self.addSheet = nil }
            }
        }
        .onStartScreen([.paymentAddUpi, .paymentAddUpiError]) { screen in
            var start = AddPaymentMethodSheet.Start(profile: profile)
            if screen == .paymentAddUpiError {
                start.upiID = "arjunokhdfcbank"
                start.showsError = true
            }
            addSheet = start
        }
    }

    /// The primary method with the user's avatar and name, while Show to friends is on.
    @ViewBuilder
    private var preview: some View {
        if let primary = profile.primaryPaymentMethod, profile.showPaymentToFriends {
            PBPaymentPreview(avatar: profileStore.avatarContent, name: profile.name, upiID: primary.title,
                             onCopy: { copy(primary) }, testIDPrefix: "paymentDetails", showsCaption: false)
                .accessibilityIdentifier("paymentDetails.preview")
        } else {
            Text(profile.paymentMethods.isEmpty
                 ? "Add a payment method so friends know how to pay you."
                 : "Friends don’t see a payment method while this is off.")
                .textStyle(.footnote)
                .foregroundStyle(PBColor.textSecondary)
                .accessibilityIdentifier("paymentDetails.preview")
        }
    }

    /// Make primary (not for the primary), Copy and Remove in a card, in a Medium sheet titled with the
    /// method (§4, proposal). Each closes the sheet.
    private func actionsSheet(_ method: PaymentMethod) -> some View {
        PBSheet(title: method.title, testIDPrefix: "paymentDetails.actions", onClose: { actionsFor = nil }) {
            VStack(spacing: 0) {
                if !method.primary {
                    PBSettingRow("Make primary", icon: .star, trailing: .none) {
                        actionsFor = nil
                        profileStore.makePrimary(method.id)
                    }
                    .accessibilityIdentifier("paymentDetails.actions.primary")
                }
                PBSettingRow("Copy", icon: .copy, trailing: .none) {
                    actionsFor = nil
                    copy(method)
                }
                .accessibilityIdentifier("paymentDetails.actions.copy")
                PBSettingRow("Remove", icon: .delete, trailing: .none, tone: .destructive, showsDivider: false) {
                    actionsFor = nil
                    removing = method
                }
                .accessibilityIdentifier("paymentDetails.actions.remove")
            }
            .pbCard(padding: 0)
        }
    }

    /// Copies the UPI ID, or the bank and last 4 digits, with a success tick and the toast.
    private func copy(_ method: PaymentMethod) {
        UIPasteboard.general.string = method.title
        Haptics.success()
        router.toast(method.kind == .upi ? "UPI ID copied" : "Details copied")
    }

    private func isPresented<Value>(_ item: Binding<Value?>) -> Binding<Bool> {
        Binding { item.wrappedValue != nil } set: { if !$0 { item.wrappedValue = nil } }
    }
}

#Preview("PaymentDetailsScreen") {
    let profileStore = ProfileStore(defaults: UserDefaults(suiteName: "preview-payment")!)
    profileStore.replace(with: .sample)
    return PaymentDetailsScreen()
        .environment(AppRouter())
        .environment(profileStore)
}
