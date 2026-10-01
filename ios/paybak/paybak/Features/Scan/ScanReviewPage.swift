import SwiftUI

/// Check receipt (screens-insights-ai §4.3): what was read, every amount editable in place, the items
/// checked against the subtotal, Add item, then "Looks right". When nothing could be read it offers
/// Retake and Attach photo instead.
struct ScanReviewPage: View {
    @Binding var review: ReceiptReview?
    let photo: UIImage?
    let isUnreadable: Bool
    let onRetake: () -> Void
    let onAttach: () -> Void
    let onConfirm: () -> Void

    @Environment(\.dismiss) private var dismiss
    @Environment(AppRouter.self) private var router
    @Environment(LedgerStore.self) private var ledgerStore
    @State private var dateRequest = RecordID.make()
    @State private var isRenaming = false
    @State private var isAddingItem = false
    @State private var newName = ""
    @State private var newAmount = ""

    private var currency: String { ledgerStore.books.defaultCurrency }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 0) {
                if let review, !isUnreadable {
                    content(review)
                } else {
                    unreadable
                }
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(.horizontal, PBLayout.screenMargin)
            .padding(.bottom, 100)
            .phoneContentWidth()
        }
        .scrollDismissesKeyboard(.interactively)
        .pbPinnedHeader {
            PBPushHeader("Check receipt", testIDPrefix: "scanReview") { dismiss() }
        }
        .safeAreaInset(edge: .bottom, spacing: 0) {
            if let review, !isUnreadable {
                ScanBottomBar {
                    PBButton("Looks right", fillsWidth: true, action: onConfirm)
                        .disabled(!review.addsUp)
                        .accessibilityIdentifier("scanReview.confirm")
                }
            }
        }
        .toolbarVisibility(.hidden, for: .navigationBar)
        .onRouteResult(dateRequest) { result in
            if case .day(let day?) = result { review?.date = day }
        }
        .alert("Merchant", isPresented: $isRenaming) {
            TextField("Merchant", text: $newName)
            Button("Cancel", role: .cancel) {}
            Button("Save") { rename() }
        }
        .alert("Add item", isPresented: $isAddingItem) {
            TextField("Item", text: $newName)
            TextField("Amount", text: $newAmount)
                .keyboardType(.decimalPad)
            Button("Cancel", role: .cancel) {}
            Button("Add") { addItem() }
        }
        .scanState("review")
    }

    @ViewBuilder
    private func content(_ review: ReceiptReview) -> some View {
        HStack(spacing: PBSpace.s16) {
            PBReceiptThumbnail(photo: photo.map(Image.init(uiImage:)))
            VStack(alignment: .leading, spacing: PBSpace.s2) {
                Text(review.foundLine)
                    .textStyle(.headline)
                    .foregroundStyle(PBColor.textPrimary)
                Text("Tap any value to fix it.")
                    .textStyle(.footnote)
                    .foregroundStyle(PBColor.textSecondary)
            }
        }
        .padding(.top, PBSpace.s16)

        VStack(spacing: 0) {
            PBSettingRow("Merchant", value: review.merchant, icon: .receipt, trailing: .none) {
                newName = review.merchant
                isRenaming = true
            }
            .accessibilityIdentifier("scanReview.merchant")
            PBSettingRow("Date", value: review.dateLine, icon: .calendar, trailing: .none, showsDivider: false) {
                dateRequest = RecordID.make()
                router.open(.pickDate(DatePickRequest(id: dateRequest, selected: review.date, latest: ledgerStore.books.today)))
            }
            .accessibilityIdentifier("scanReview.date")
        }
        .pbCard(padding: 0)
        .padding(.top, PBSpace.s24)

        VStack(spacing: 0) {
            ReceiptAmountRow(label: "Subtotal", amount: binding(\.subtotal), currency: currency)
                .accessibilityIdentifier("scanReview.subtotal")
            ForEach(review.charges.indices, id: \.self) { index in
                ReceiptAmountRow(label: review.charges[index].label, amount: chargeBinding(index), currency: currency)
                    .accessibilityIdentifier(Self.chargeID(review.charges[index].label, index: index))
            }
            ReceiptAmountRow(label: "Total", amount: .constant(review.total), currency: currency, isTotal: true)
                .accessibilityIdentifier("scanReview.total")
        }
        .pbCard(padding: 0)
        .padding(.top, PBSpace.s16)

        PBSectionHeader("Items", actionTitle: "Add item") {
            newName = ""
            newAmount = ""
            isAddingItem = true
        }
        .accessibilityIdentifier("scanReview.addItem")
        .padding(.top, PBSpace.s24)

        VStack(spacing: 0) {
            ForEach(review.items.indices, id: \.self) { index in
                ReceiptAmountRow(label: review.items[index].label, amount: itemBinding(index), currency: currency)
                    .accessibilityIdentifier("scanReview.item.\(index)")
            }
        }
        .pbCard(padding: 0)
        .padding(.top, PBSpace.s8)

        if let mismatch = review.mismatchLine(currency: currency) {
            Text(mismatch)
                .textStyle(.footnote)
                .foregroundStyle(PBColor.textDestructive)
                .padding(.top, PBSpace.s8)
                .accessibilityIdentifier("scanReview.mismatch")
        }
    }

    private var unreadable: some View {
        PBNoticeCard(
            icon: .receipt, title: "Couldn’t read this receipt",
            message: "Try again in better light, or attach the photo to the expense as it is.",
            primary: .init("Retake", testID: "scanReview.retake", action: onRetake),
            secondary: .init("Attach photo", testID: "scanReview.attach", action: onAttach)
        )
        .padding(.top, PBSpace.s16)
    }

    /// `scanReview.tip`, or `scanReview.tax.<n>` for GST, service and the like.
    private static func chargeID(_ label: String, index: Int) -> String {
        let name = label.lowercased()
        return name.hasPrefix("tip") || name.hasPrefix("gratuity") ? "scanReview.tip" : "scanReview.tax.\(index)"
    }

    // MARK: Editing

    private func binding(_ keyPath: WritableKeyPath<ReceiptReview, Int64>) -> Binding<Int64> {
        Binding { review?[keyPath: keyPath] ?? 0 } set: { review?[keyPath: keyPath] = $0 }
    }

    private func chargeBinding(_ index: Int) -> Binding<Int64> {
        Binding { review?.charges[safe: index]?.amount ?? 0 } set: { review?.charges[index].amount = $0 }
    }

    private func itemBinding(_ index: Int) -> Binding<Int64> {
        Binding { review?.items[safe: index]?.amount ?? 0 } set: { review?.items[index].amount = $0 }
    }

    private func rename() {
        let name = newName.trimmingCharacters(in: .whitespaces)
        if !name.isEmpty { review?.merchant = name }
    }

    private func addItem() {
        let name = newName.trimmingCharacters(in: .whitespaces)
        let amount = MoneyInput.minor(newAmount, currency: currency)
        guard !name.isEmpty, amount > 0 else { return }
        review?.addItem(name, amount: amount)
    }
}

/// A receipt line whose amount is fixed in place: grouped ("₹2,000") at rest, raw while typing,
/// written back on every keystroke so the total follows.
private struct ReceiptAmountRow: View {
    let label: String
    @Binding var amount: Int64
    let currency: String
    var isTotal = false

    @State private var text = ""
    @State private var isEditing = false

    var body: some View {
        PBReceiptLineRow(label: label, amount: $text, currency: currency, isTotal: isTotal,
                         isEditing: $isEditing)
            .allowsHitTesting(!isTotal)
            .onAppear { text = grouped }
            .onChange(of: amount) { if !isEditing { text = grouped } }
            .onChange(of: isEditing) { text = isEditing ? MoneyInput.text(amount, currency: currency) : grouped }
            .onChange(of: text) {
                guard isEditing else { return }
                amount = MoneyInput.minor(text.replacingOccurrences(of: ",", with: ""), currency: currency)
            }
    }

    /// "2,000" (the row adds the symbol).
    private var grouped: String {
        let formatted = Money.format(amount, currency)
        return String(formatted.dropFirst(Money.info(currency).symbol.count)).trimmingCharacters(in: .whitespaces)
    }
}

/// The pinned CTA over a white fade (Check receipt, Assign items).
struct ScanBottomBar<Content: View>: View {
    @ViewBuilder let content: Content

    var body: some View {
        VStack(spacing: PBSpace.s16) {
            content
        }
        .padding(.horizontal, PBLayout.screenMargin)
        .phoneContentWidth()
        .keyboardGap()
        .background(alignment: .top) {
            // Figma: the fade starts 24 pt above the bar and is solid white 22 pt later.
            VStack(spacing: 0) {
                LinearGradient(colors: [PBColor.bgPrimary.opacity(0), PBColor.bgPrimary], startPoint: .top, endPoint: .bottom)
                    .frame(height: 22)
                PBColor.bgPrimary
            }
            .padding(.top, -24)
            .ignoresSafeArea(edges: .bottom)
            .allowsHitTesting(false)
        }
    }
}

private extension Array {
    subscript(safe index: Int) -> Element? {
        indices.contains(index) ? self[index] : nil
    }
}
