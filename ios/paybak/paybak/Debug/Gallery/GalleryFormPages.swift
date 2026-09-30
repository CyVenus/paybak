#if DEBUG
import SwiftUI

// Gallery pages for components-app.md §1–2: Shared (from Profile) and Forms & Money.

struct GalleryChipsSettingsPage: View {
    @State private var chip = true
    @State private var isOn = true
    @State private var count = 2

    var body: some View {
        GalleryPageScroll {
            GallerySection("Control / Category Chip: None · Icon · Avatar × Selected False · True") {
                HStack(spacing: PBSpace.s8) {
                    PBCategoryChip("Hair") {}
                    PBCategoryChip("Hair", isSelected: true) {}
                    PBCategoryChip("Hair", leading: .icon(.plus)) {}
                    PBCategoryChip("Hair", leading: .icon(.plus), isSelected: true) {}
                }
                HStack(spacing: PBSpace.s8) {
                    PBCategoryChip("Hair", leading: .avatar(.art(.priya))) {}
                    PBCategoryChip("Hair", leading: .avatar(.art(.priya)), isSelected: chip) { chip.toggle() }
                    PBCategoryChip("Priya", leading: .avatar(.art(.priya)), onRemove: {}) {}
                }
                HStack(spacing: PBSpace.s8) {
                    PBCategoryChip("Hair") {}.pbPreviewInteraction(.pressed)
                    PBCategoryChip("Hair", isSelected: true) {}.pbPreviewInteraction(.pressed)
                    PBCategoryChip("Food", isSelected: true, onRemove: {}) {}
                }
            }
            GallerySection("Row / Setting: Chevron · Toggle · Stepper · Check · Unchecked · None") {
                VStack(spacing: 0) {
                    PBSettingRow("Payment details", value: "UPI", icon: .wallet) {}
                    PBSettingRow("Payment details", value: "UPI", icon: .wallet, trailing: .toggle($isOn))
                    PBSettingRow("Payment details", value: "\(count)", icon: .wallet, trailing: .stepper($count, in: 0...9))
                    PBSettingRow("Payment details", value: "UPI", icon: .wallet, trailing: .check) {}
                    PBSettingRow("Payment details", value: "UPI", icon: .wallet, trailing: .unchecked) {}
                    PBSettingRow("Payment details", value: "UPI", icon: .wallet, trailing: .none, showsDivider: false)
                }
                .pbCard(padding: 0)
            }
            GallerySection("Destructive · no icon · subtitle · badge · pressed") {
                VStack(spacing: 0) {
                    PBSettingRow("Sign out", icon: .logout, trailing: .none, tone: .destructive) {}
                    PBSettingRow("Currency", value: "INR") {}
                    PBSettingRow("Instalments", subtitle: "Paid back in parts", icon: .calendar, trailing: .toggle($isOn))
                    PBSettingRow("Paybak Pro", icon: .crown, badge: "Try free") {}
                    PBSettingRow("Help & feedback", icon: .help, showsDivider: false) {}
                        .pbPreviewInteraction(.pressed)
                }
                .pbCard(padding: 0)
            }
        }
    }
}

struct GalleryHeadersAlertPage: View {
    @State private var showsAlert = false

    var body: some View {
        GalleryPageScroll {
            GallerySection("Navigation / Push Header: Text · Icon · None · Wide Text") {
                PBPushHeader("Edit avatar", trailing: .text("Save") {}) {}
                PBPushHeader("Edit avatar", trailing: .icon(.settings, accessibilityLabel: "Settings") {}) {}
                PBPushHeader("Edit avatar") {}
                PBPushHeader("Notifications", trailing: .wideText("Mark all read") {}) {}
            }
            GallerySection("Navigation / Modal Header: Enabled · Disabled · None") {
                PBModalHeader("Add expense", actionLabel: "Save", onClose: {})
                PBModalHeader("Add expense", actionLabel: "Save", isActionEnabled: false, onClose: {})
                PBModalHeader("Add expense", onClose: {})
            }
            GallerySection("Overlay / Alert: Destructive · Primary (tap to present)") {
                PBAlert(title: "Discard changes?", message: "Your avatar edits won’t be saved.", cancelLabel: "Keep editing", actionLabel: "Discard", onCancel: {}, onAction: { showsAlert = true })
                    .frame(maxWidth: .infinity)
                PBAlert(title: "Discard changes?", message: "Your avatar edits won’t be saved.", cancelLabel: "Not now", actionLabel: "Settle up", role: .primary, onCancel: {}, onAction: { showsAlert = true })
                    .frame(maxWidth: .infinity)
            }
        }
        .pbAlert(isPresented: $showsAlert, title: "Discard changes?", message: "Your avatar edits won’t be saved.", cancelLabel: "Keep editing", actionLabel: "Discard", testIDPrefix: "gallery.alert") {}
    }
}

struct GalleryTextInputsPage: View {
    @State private var message = "Hi Rohan! Just a gentle reminder about ₹800 for the movie tickets on 20 Sep. You can pay me on UPI at arjun@okaxis. Thanks."
    @State private var empty = ""
    @State private var typing = "Add a comment"
    @State private var sent = "Type and send (live)"

    var body: some View {
        GalleryPageScroll {
            GallerySection("Control / Text Area: Default · Focused") {
                PBTextArea("Message", text: $message, helper: "You can edit this message.")
                PBTextArea("Message", text: $message, helper: "You can edit this message.")
                    .pbPreviewInteraction(.focused)
            }
            GallerySection("Control / Composer: Empty · Typing (Pinned=False)") {
                PBComposer(text: $empty, placeholder: "Add a comment", onMic: {}) { _ in }
                PBComposer(text: $typing, placeholder: "Add a comment") { _ in }
            }
            GallerySection("Composer Pinned=True (402 wide bar), then live") {
                VStack(spacing: PBSpace.s16) {
                    PBComposer(text: $empty, placeholder: "Add a comment", isPinned: true, onMic: {}) { _ in }
                    PBComposer(text: $typing, placeholder: "Add a comment", isPinned: true) { _ in }
                }
                .padding(.horizontal, -PBLayout.screenMargin)
                Text(sent).textStyle(.footnote).foregroundStyle(PBColor.textSecondary)
                PBComposer(text: $empty, placeholder: "Ask or add an expense") { sent = "Sent: \($0)" }
            }
        }
    }
}

struct GalleryAmountEntryPage: View {
    @State private var empty = ""
    @State private var amount = "2800"
    @State private var long = "123456789.50"
    @State private var live = ""

    var body: some View {
        GalleryPageScroll {
            GallerySection("Control / Amount Display: Empty · Focused · Filled + helper") {
                PBAmountField(text: $empty, currency: Currency(code: "INR"), date: "Today")
                    .pbPreviewInteraction(.focused)
                PBAmountField(text: $amount, currency: Currency(code: "INR"), date: "Today")
                    .pbPreviewInteraction(.focused)
                PBAmountField(text: $amount, currency: Currency(code: "INR"), helper: "You owe Meera ₹450 in Flat 302")
            }
            GallerySection("Live: tap the amount (Indian grouping) · a long amount shrinks") {
                PBAmountField(text: $live, currency: Currency(code: "INR"), date: "Today")
                PBAmountField(text: $long, currency: Currency(code: "USD"))
            }
            GallerySection("Control / Payment Parties") {
                PBPaymentParties(from: .init(name: "You", avatar: .art(.arjun)), to: .init(name: "Meera", avatar: .art(.meera)))
            }
            GallerySection("Card / Split Total: Balanced · Error") {
                PBSplitTotalBar(left: "₹0 left", detail: "₹2,800 of ₹2,800")
                PBSplitTotalBar(left: "₹150 left", detail: "₹2,650 of ₹2,800", isError: true)
            }
        }
    }
}

struct GallerySplitRowsPage: View {
    @State private var included = true
    @State private var excluded = false
    @State private var exact = "700"
    @State private var percent = "25"
    @State private var shares = 1

    var body: some View {
        GalleryPageScroll {
            ForEach(Array(zip(["Equally", "Exact", "Percent", "Shares"], modes).enumerated()), id: \.offset) { _, entry in
                GallerySection("Row / Split Person, Mode=\(entry.0): Default · Focused · Excluded") {
                    VStack(spacing: 0) {
                        row(entry.1, included: $included)
                        row(entry.1, included: $included).pbPreviewInteraction(entry.0 == "Equally" ? .pressed : .focused)
                        row(entry.1, included: $excluded, showsDivider: false)
                    }
                    .pbCard(padding: 0)
                }
            }
        }
    }

    private var modes: [PBSplitRow.Mode] {
        [.equally, .exact($exact), .percent($percent), .shares($shares)]
    }

    private func row(_ mode: PBSplitRow.Mode, included: Binding<Bool>, showsDivider: Bool = true) -> PBSplitRow {
        PBSplitRow(name: "Priya", avatar: .art(.priya), mode: mode, amount: "₹700", isIncluded: included, showsDivider: showsDivider)
    }
}
#endif
