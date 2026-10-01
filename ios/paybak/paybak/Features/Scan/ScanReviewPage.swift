import SwiftUI

/// Check receipt (screens-insights-ai §4.3): what was read (the merchant and date as read), every
/// amount editable in place, the items checked against the subtotal, Add item (a "New item" line
/// whose amount is typed in place; an item set to 0 goes), then "Looks right". When nothing could be
/// read it offers Retake and Attach photo instead.
struct ScanReviewPage: View {
    @Binding var review: ReceiptReview?
    let photo: UIImage?
    let isUnreadable: Bool
    let onRetake: () -> Void
    let onAttach: () -> Void
    let onConfirm: () -> Void

    @Environment(\.dismiss) private var dismiss
    @Environment(LedgerStore.self) private var ledgerStore
    /// The line Add item just appended, opened for its amount.
    @State private var newItem: Int?

    private var currency: String { ledgerStore.books.defaultCurrency }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 0) {
                if let review, !isUnreadable {
                    content(review)
                } else {
                    intro(found: "We couldn’t find any items.", showsHint: false)
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
        .scanState("review")
    }

    /// The photo, "We found 6 items." (or that none were) and, once read, "Tap any value to fix it."
    private func intro(found: String, showsHint: Bool) -> some View {
        HStack(spacing: PBSpace.s16) {
            PBReceiptThumbnail(photo: photo.map(Image.init(uiImage:)))
            VStack(alignment: .leading, spacing: PBSpace.s2) {
                Text(found)
                    .textStyle(.headline)
                    .foregroundStyle(PBColor.textPrimary)
                if showsHint {
                    Text("Tap any value to fix it.")
                        .textStyle(.footnote)
                        .foregroundStyle(PBColor.textSecondary)
                }
            }
        }
        .padding(.top, PBSpace.s16)
    }

    @ViewBuilder
    private func content(_ review: ReceiptReview) -> some View {
        intro(found: review.foundLine, showsHint: true)

        VStack(spacing: 0) {
            PBSettingRow("Merchant", value: review.merchantLine, icon: .receipt, trailing: .none)
                .accessibilityIdentifier("scanReview.merchant")
            PBSettingRow("Date", value: review.dateLine, icon: .calendar, trailing: .none, showsDivider: false)
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

        PBSectionHeader("Items", actionTitle: "Add item", action: addItem)
            .accessibilityIdentifier("scanReview.addItem")
            .padding(.top, PBSpace.s24)

        VStack(spacing: 0) {
            ForEach(review.items.indices, id: \.self) { index in
                ReceiptAmountRow(label: review.items[index].label, amount: itemBinding(index), currency: currency,
                                 startsEditing: index == newItem) { finishItem(index) }
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
        .padding(.top, PBSpace.s24)
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

    /// "New item" at ₹0, its amount open for typing.
    private func addItem() {
        guard review != nil else { return }
        review?.addItem("New item", amount: 0)
        newItem = (review?.items.count ?? 1) - 1
    }

    /// An item left at 0 is taken off the receipt.
    private func finishItem(_ index: Int) {
        if newItem == index { newItem = nil }
        guard review?.items[safe: index]?.amount == 0 else { return }
        review?.items.remove(at: index)
        review?.assignment.remove(at: index)
    }
}

/// A receipt line whose amount is fixed in place: grouped ("₹2,000") at rest, raw while typing (empty
/// for ₹0), written back on every keystroke so the total follows. `onCommit` runs when typing ends.
private struct ReceiptAmountRow: View {
    let label: String
    @Binding var amount: Int64
    let currency: String
    var isTotal = false
    var startsEditing = false
    var onCommit: () -> Void = {}

    @State private var text = ""
    @State private var isEditing = false

    var body: some View {
        PBReceiptLineRow(label: label, amount: $text, currency: currency, isTotal: isTotal,
                         isEditing: $isEditing)
            .allowsHitTesting(!isTotal)
            .onAppear {
                text = grouped
                if startsEditing { isEditing = true }
            }
            .onChange(of: amount) { if !isEditing { text = grouped } }
            .onChange(of: isEditing) {
                text = isEditing ? (amount == 0 ? "" : MoneyInput.text(amount, currency: currency)) : grouped
                if !isEditing { onCommit() }
            }
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
