#if DEBUG
import SwiftUI

// MARK: - Headers, alerts, sheets, settings

struct GalleryNavigationPage: View {
    @State private var alertRole: PBAlert.Role = .destructive
    @State private var showsAlert = false
    @State private var query = ""
    @State private var category = 0
    @State private var sheetDetent: PBSheetDetent = .fitted
    @State private var showsSheet = false
    @State private var selectedChips: Set<String> = ["Food"]
    @State private var people: [PBPeepHead] = [.priya, .rohan]
    @State private var isOn = true
    @State private var count = 2

    private static let categories: [(name: String, icon: PBIcon)] = [("Food", .food), ("Travel", .car), ("Stays", .bed)]
    private static let tones: [(String, PBSettingRow.Tone)] = [("Default", .default), ("Destructive", .destructive)]

    var body: some View {
        GalleryPageScroll {
            GallerySection("Navigation / Push Header: Text · Icon · None · Wide Text") {
                PBPushHeader("Edit avatar", trailing: .text("Save") {}) {}
                PBPushHeader("Edit avatar", trailing: .icon(.settings, accessibilityLabel: "Settings") {}) {}
                PBPushHeader("Edit avatar") {}
                PBPushHeader("Edit avatar", trailing: .wideText("Save") {}) {}
                GalleryLabel("Wide Text truncates the title and a long action")
                PBPushHeader("Notifications and reminders", trailing: .wideText("Mark all read") {}) {}
            }
            GallerySection("Navigation / Modal Header: Enabled · Disabled · None") {
                PBModalHeader("Add expense", actionLabel: "Save", onClose: {})
                PBModalHeader("Add expense", actionLabel: "Save", isActionEnabled: false, onClose: {})
                PBModalHeader("Add expense", onClose: {})
            }
            GallerySection("Overlay / Alert: Destructive · Primary") {
                onScrim {
                    PBAlert(title: "Discard changes?", message: "Your avatar edits won’t be saved.", cancelLabel: "Keep editing",
                            actionLabel: "Discard", onCancel: {}, onAction: {})
                }
                onScrim {
                    PBAlert(title: "Discard changes?", message: "Your avatar edits won’t be saved.", cancelLabel: "Not now",
                            actionLabel: "Settle up", role: .primary, onCancel: {}, onAction: {})
                }
                HStack(spacing: PBSpace.s8) {
                    PBButton("Show Destructive", style: .secondary, size: .small) { presentAlert(.destructive) }
                    PBButton("Show Primary", style: .secondary, size: .small) { presentAlert(.primary) }
                }
            }
            GallerySection("Sheet / Container") {
                GalleryLabel("Detent=Medium with search (the picker pattern)")
                GallerySheetContainer {
                    PBSheet(title: "Category", search: $query, searchPrompt: "Search categories", onClose: {}) {
                        categoryRows(testIDPrefix: nil) { category = $0 }
                    }
                }
                .padding(PBSpace.s8)
                .background(PBColor.bgScrim, in: .rect(cornerRadius: PBRadius.card))
                GalleryLabel("No title and no close: the content starts 20 pt down")
                GallerySheetContainer {
                    PBSheet {
                        PBButton("Not received", fillsWidth: true) {}
                    }
                }
                .padding(PBSpace.s8)
                .background(PBColor.bgScrim, in: .rect(cornerRadius: PBRadius.card))
                HStack(spacing: PBSpace.s8) {
                    PBButton("Open Medium", style: .secondary, size: .small) { presentSheet(.fitted) }
                    PBButton("Open Large", style: .secondary, size: .small) { presentSheet(.large) }
                }
            }
            GallerySection("Control / Category Chip (tap to select)") {
                chips
            }
            GallerySection("Row / Setting: Default · Destructive") {
                settingRows
            }
        }
        .pbFullScreenAlert(
            isPresented: $showsAlert,
            title: alertRole == .destructive ? "Discard changes?" : "Settle up with Rohan?",
            message: alertRole == .destructive ? "Your avatar edits won’t be saved." : "This records ₹800 as paid.",
            cancelLabel: alertRole == .destructive ? "Keep editing" : "Not now",
            actionLabel: alertRole == .destructive ? "Discard" : "Settle up",
            role: alertRole,
            testIDPrefix: "gallery.alert"
        ) {}
        .pbSheet(isPresented: $showsSheet, detent: sheetDetent) {
            PBSheet(title: sheetDetent == .large ? "Paid by" : "Category", search: $query, searchPrompt: "Search categories",
                    testIDPrefix: "gallery.sheet", onClose: { showsSheet = false }) {
                categoryRows(testIDPrefix: "gallery.sheet") { index in
                    category = index
                    showsSheet = false
                }
            }
        }
    }

    private func presentAlert(_ role: PBAlert.Role) {
        alertRole = role
        showsAlert = true
    }

    private func presentSheet(_ detent: PBSheetDetent) {
        sheetDetent = detent
        showsSheet = true
    }

    /// A sample centred on the 40 % scrim.
    private func onScrim<Content: View>(@ViewBuilder _ content: () -> Content) -> some View {
        content()
            .frame(maxWidth: .infinity)
            .padding(PBSpace.s16)
            .background(PBColor.bgScrim, in: .rect(cornerRadius: PBRadius.card))
    }

    private func categoryRows(testIDPrefix: String?, onSelect: @escaping (Int) -> Void) -> some View {
        VStack(spacing: 0) {
            ForEach(Array(Self.categories.enumerated()), id: \.offset) { index, entry in
                PBSettingRow(entry.name, icon: entry.icon, trailing: category == index ? .check : .unchecked,
                             showsDivider: index < Self.categories.count - 1) {
                    onSelect(index)
                }
                .galleryTestID(testIDPrefix.map { "\($0).\(entry.name)" })
            }
        }
        .pbCard(padding: 0)
    }

    @ViewBuilder
    private var chips: some View {
        HStack(spacing: PBSpace.s8) {
            ForEach(["Hair", "Food"], id: \.self) { label in
                PBCategoryChip(label, isSelected: selectedChips.contains(label)) { toggle(label) }
            }
            PBCategoryChip("Add", leading: .icon(.plus), isSelected: selectedChips.contains("Add")) { toggle("Add") }
        }
        HStack(spacing: PBSpace.s8) {
            ForEach(["Hair", "Priya"], id: \.self) { label in
                PBCategoryChip(label, leading: .avatar(.art(.priya)), isSelected: selectedChips.contains(label)) { toggle(label) }
            }
        }
        GalleryLabel("Show remove: ✕ removes the chip")
        HStack(spacing: PBSpace.s8) {
            ForEach(people) { head in
                PBCategoryChip(head.name, leading: .avatar(.art(head)), onRemove: { people.removeAll { $0 == head } }) {}
            }
        }
    }

    private func toggle(_ label: String) {
        if selectedChips.contains(label) {
            selectedChips.remove(label)
        } else {
            selectedChips.insert(label)
        }
    }

    @ViewBuilder
    private var settingRows: some View {
        ForEach(Self.tones, id: \.0) { _, tone in
            VStack(spacing: 0) {
                PBSettingRow("Payment details", value: "UPI", icon: .wallet, tone: tone) {}
                PBSettingRow("Payment details", icon: .wallet, trailing: .toggle($isOn), tone: tone)
                PBSettingRow("Payment details", value: "\(count)", icon: .wallet, trailing: .stepper($count, in: 0...99), tone: tone)
                PBSettingRow("Payment details", icon: .wallet, trailing: .check, tone: tone) {}
                PBSettingRow("Payment details", icon: .wallet, trailing: .unchecked, tone: tone) {}
                PBSettingRow("Payment details", value: "UPI", icon: .wallet, trailing: .none, tone: tone, showsDivider: false)
            }
            .pbCard(padding: 0)
        }
        GalleryLabel("Subtitle, badge, no icon")
        VStack(spacing: 0) {
            PBSettingRow("Loan to Dev", subtitle: "Paid back in parts", badge: "Pro") {}
            PBSettingRow("Insights", icon: .chart, badge: "Try free", showsDivider: false) {}
        }
        .pbCard(padding: 0)
    }
}

// MARK: - Forms and money

struct GalleryFormsPage: View {
    @State private var note = ""
    @State private var message = ""
    @State private var sent = ""
    @State private var amount = ""
    @State private var included: Set<PBPeepHead> = [.arjun, .priya, .rohan, .esha]
    @State private var shares: [PBPeepHead: Int] = [.arjun: 1, .priya: 1, .rohan: 1, .esha: 1]
    @State private var yearly = true

    private static let reminder = "Hi Rohan! Just a gentle reminder about ₹800 for the movie tickets on 20 Sep. You can pay me on UPI at arjun@okaxis. Thanks."
    private static let people: [PBPeepHead] = [.arjun, .priya, .rohan, .esha]
    private static let splitTotal = 2_800
    private let inr = Currency(code: "INR")

    var body: some View {
        GalleryPageScroll {
            GallerySection("Control / Text Area: Default · Focused · live") {
                PBTextArea("Message", text: .constant(Self.reminder), helper: "You can edit this message.")
                PBTextArea("Message", text: .constant(Self.reminder), helper: "You can edit this message.")
                    .pbPreviewInteraction(.focused)
                PBTextArea(nil, text: $note, prompt: "Add a note (optional)")
            }
            GallerySection("Control / Composer: Empty · Typing · Pinned") {
                PBComposer(text: .constant(""), placeholder: "Add a comment", onMic: {}) { _ in }
                PBComposer(text: .constant("Add a comment"), placeholder: "Add a comment") { _ in }
                GalleryLabel("Pinned: the keyboard bar (full width in a screen)")
                PBComposer(text: $message, placeholder: "Ask or add an expense", isPinned: true, onMic: {}) { text in
                    sent = text
                    message = ""
                }
                if !sent.isEmpty {
                    GalleryLabel("Sent: \(sent)")
                }
            }
            GallerySection("Control / Amount Display: Empty · Focused · Filled · live") {
                PBAmountField(text: .constant(""), currency: inr, date: "Today")
                    .pbPreviewInteraction(.focused)
                PBAmountField(text: .constant("2800"), currency: inr, date: "Today")
                    .pbPreviewInteraction(.focused)
                PBAmountField(text: .constant("2800"), currency: inr, date: "Today")
                GalleryLabel("Live, with helper and no date chip (tap the amount)")
                PBAmountField(text: $amount, currency: inr, helper: "You owe Meera ₹450 in Flat 302")
            }
            GallerySection("Control / Payment Parties") {
                PBPaymentParties(from: .init(name: "You", avatar: .art(.arjun)), to: .init(name: "Meera", avatar: .art(.meera)))
            }
            GallerySection("Row / Split Person: Default · Excluded") {
                ForEach([true, false], id: \.self) { isIncluded in
                    GalleryLabel(isIncluded ? "Default" : "Excluded")
                    splitModes(isIncluded: isIncluded)
                }
            }
            GallerySection("Row / Split Person, live: tap a row to exclude, a field to focus") {
                liveSplit
            }
            GallerySection("Card / Split Total: Balanced · Error") {
                PBSplitTotalBar(left: "₹0 left", detail: "₹2,800 of ₹2,800")
                PBSplitTotalBar(left: "₹150 left", detail: "₹2,650 of ₹2,800", isError: true)
            }
            GallerySection("Card / Plan (tap to select)") {
                HStack(spacing: PBSpace.s20) {
                    PBPlanCard(period: "Yearly", price: "₹799/year", detail: "₹67/month", badge: "Save 33%", isSelected: yearly) { yearly = true }
                        .frame(maxWidth: .infinity)
                    PBPlanCard(period: "Monthly", price: "₹99/month", detail: "Billed monthly", isSelected: !yearly) { yearly = false }
                        .frame(maxWidth: .infinity)
                }
            }
        }
    }

    /// Priya in each mode: Equally, Exact, % and Shares.
    private func splitModes(isIncluded: Bool) -> some View {
        let amount = isIncluded ? "₹700" : "₹0"
        let included = Binding.constant(isIncluded)
        return VStack(spacing: 0) {
            PBSplitRow(name: "Priya", avatar: .art(.priya), mode: .equally, amount: amount, isIncluded: included)
            PBSplitRow(name: "Priya", avatar: .art(.priya), mode: .exact(.constant(isIncluded ? "700" : "0")), amount: amount, isIncluded: included)
            PBSplitRow(name: "Priya", avatar: .art(.priya), mode: .percent(.constant(isIncluded ? "25" : "0")), amount: amount, isIncluded: included)
            PBSplitRow(name: "Priya", avatar: .art(.priya), mode: .shares(.constant(isIncluded ? 1 : 0)), amount: amount, isIncluded: included,
                       showsDivider: false)
        }
        .pbCard(padding: 0)
    }

    /// ₹2,800 by shares among four people: excluding someone or changing a count re-splits it.
    private var liveSplit: some View {
        VStack(spacing: 0) {
            ForEach(Array(Self.people.enumerated()), id: \.offset) { index, head in
                PBSplitRow(name: head.name, avatar: .art(head), mode: .shares(shareCount(head)),
                           amount: "₹" + galleryGroupIndian(String(share(of: head))), isIncluded: isIncluded(head),
                           showsDivider: index < Self.people.count - 1)
            }
        }
        .pbCard(padding: 0)
    }

    private func count(of head: PBPeepHead) -> Int {
        included.contains(head) ? shares[head] ?? 1 : 0
    }

    private func share(of head: PBPeepHead) -> Int {
        let total = Self.people.filter { included.contains($0) }.map { shares[$0] ?? 1 }.reduce(0, +)
        return total == 0 ? 0 : Self.splitTotal * count(of: head) / total
    }

    private func shareCount(_ head: PBPeepHead) -> Binding<Int> {
        Binding { count(of: head) } set: { shares[head] = max(0, $0) }
    }

    private func isIncluded(_ head: PBPeepHead) -> Binding<Bool> {
        Binding {
            included.contains(head)
        } set: { isOn in
            if isOn {
                included.insert(head)
            } else {
                included.remove(head)
            }
        }
    }
}
#endif
