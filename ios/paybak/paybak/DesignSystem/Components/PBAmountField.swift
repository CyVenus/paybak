import SwiftUI

/// Control / Amount Display (Figma 125:1084): the amount-first entry at the top of Add expense,
/// Record payment, Lend money and the receipt review. A centred row of chips (the currency code,
/// which opens the currency sheet, and the optional date chip), then the amount in Amount/Display
/// (ExtraBold 56/64) with a 2 × 56 caret while focused, and an optional Footnote helper.
///
/// `text` is the raw input (digits and at most one "."); the field shows it grouped the currency's
/// way ("₹1,00,000"). Empty shows the "₹0" placeholder in gray. Input is the system number pad;
/// tapping the amount focuses it. Long amounts shrink instead of wrapping.
/// Test ids: `<prefix>.currency`, `<prefix>.date`, `<prefix>.amount`.
struct PBAmountField: View {
    @Binding var text: String
    let currency: Currency
    /// The date chip's label ("Today"); nil hides the chip.
    var date: String?
    var helper: String?
    var allowsDecimals = true
    var testIDPrefix: String?
    var onCurrencyTap: () -> Void = {}
    var onDateTap: () -> Void = {}

    private let externalFocus: FocusState<Bool>.Binding?
    @FocusState private var ownFocus: Bool
    @Environment(\.pbPreviewInteraction) private var previewInteraction

    init(
        text: Binding<String>,
        currency: Currency,
        date: String? = nil,
        helper: String? = nil,
        allowsDecimals: Bool = true,
        testIDPrefix: String? = nil,
        focus: FocusState<Bool>.Binding? = nil,
        onCurrencyTap: @escaping () -> Void = {},
        onDateTap: @escaping () -> Void = {}
    ) {
        _text = text
        self.currency = currency
        self.date = date
        self.helper = helper
        self.allowsDecimals = allowsDecimals
        self.testIDPrefix = testIDPrefix
        self.externalFocus = focus
        self.onCurrencyTap = onCurrencyTap
        self.onDateTap = onDateTap
    }

    private var focus: FocusState<Bool>.Binding { externalFocus ?? $ownFocus }
    private var isFocused: Bool { focus.wrappedValue || previewInteraction == .focused }

    var body: some View {
        VStack(spacing: PBSpace.s12) {
            HStack(spacing: PBSpace.s8) {
                PBCategoryChip(currency.code, action: onCurrencyTap)
                    .accessibilityLabel("Currency, \(currency.name)")
                    .accessibilityIdentifier(testID("currency"))
                if let date {
                    PBCategoryChip(date, action: onDateTap)
                        .accessibilityLabel("Date, \(date)")
                        .accessibilityIdentifier(testID("date"))
                }
            }
            VStack(spacing: 0) {
                amount
                if let helper {
                    Text(helper)
                        .textStyle(.footnote)
                        .foregroundStyle(PBColor.textSecondary)
                        .multilineTextAlignment(.center)
                }
            }
        }
        .padding(.vertical, PBSpace.s4)
        .frame(maxWidth: .infinity)
    }

    private var amount: some View {
        ZStack {
            TextField("", text: $text)
                .keyboardType(allowsDecimals ? .decimalPad : .numberPad)
                .focused(focus)
                .opacity(0)
                .accessibilityHidden(true)
            HStack(alignment: .top, spacing: PBSpace.s2) {
                Text(text.isEmpty ? currency.symbol + "0" : Self.display(text, currency: currency))
                    .textStyle(.amountDisplay)
                    .foregroundStyle(text.isEmpty ? PBColor.textTertiary : PBColor.textPrimary)
                    .lineLimit(1)
                    .minimumScaleFactor(0.5)
                if isFocused {
                    BlinkingCaret()
                        .padding(.top, PBSpace.s4)
                }
            }
            .padding(.horizontal, PBLayout.screenMargin)
        }
        .contentShape(.rect)
        .onTapGesture { focus.wrappedValue = true }
        .onChange(of: text) {
            let clean = Self.sanitize(text, allowsDecimals: allowsDecimals)
            if clean != text { text = clean }
        }
        .accessibilityElement(children: .ignore)
        .accessibilityLabel("Amount")
        .accessibilityValue(text.isEmpty ? "Empty" : Self.display(text, currency: currency))
        .accessibilityAddTraits(.isButton)
        .accessibilityAction { focus.wrappedValue = true }
        .accessibilityIdentifier(testID("amount"))
    }

    private func testID(_ element: String) -> String {
        testIDPrefix.map { "\($0).\(element)" } ?? ""
    }
}

extension PBAmountField {
    /// Keeps digits and one decimal point with at most two decimals, and at most nine whole digits.
    static func sanitize(_ raw: String, allowsDecimals: Bool) -> String {
        let allowed = raw.filter { $0.isASCII && ($0.isWholeNumber || (allowsDecimals && $0 == ".")) }
        let parts = allowed.split(separator: ".", maxSplits: 1, omittingEmptySubsequences: false)
        var whole = String(parts[0].prefix(9))
        // No leading zeros ("007" → "7").
        while whole.count > 1, whole.hasPrefix("0") { whole.removeFirst() }
        guard parts.count > 1 else { return whole }
        let fraction = parts[1].filter(\.isWholeNumber).prefix(2)
        return (whole.isEmpty ? "0" : whole) + "." + fraction
    }

    /// The raw input with the currency symbol and its grouping: Indian grouping for INR
    /// ("₹1,00,000"), thousands elsewhere ("$100,000"). Decimals show only as typed.
    static func display(_ raw: String, currency: Currency) -> String {
        let parts = raw.split(separator: ".", omittingEmptySubsequences: false)
        let whole = Int(parts.first ?? "") ?? 0
        let formatter = NumberFormatter()
        formatter.numberStyle = .decimal
        formatter.locale = Locale(identifier: currency.code == "INR" ? "en_IN" : "en_US")
        let grouped = formatter.string(from: NSNumber(value: whole)) ?? String(whole)
        let fraction = parts.count > 1 ? "." + parts[1] : ""
        return currency.symbol + grouped + fraction
    }
}

/// The 2 × 56 caret: blinks about once a second, steady with Reduce Motion.
private struct BlinkingCaret: View {
    @State private var isVisible = true
    @Environment(\.accessibilityReduceMotion) private var reduceMotion

    var body: some View {
        Rectangle()
            .fill(PBColor.textPrimary)
            .frame(width: 2, height: 56)
            .opacity(isVisible ? 1 : 0)
            .task {
                guard !reduceMotion else { return }
                while !Task.isCancelled {
                    try? await Task.sleep(for: .milliseconds(530))
                    isVisible.toggle()
                }
            }
    }
}

#Preview("PBAmountField") {
    @Previewable @State var empty = ""
    @Previewable @State var amount = "2800"
    VStack(spacing: PBSpace.s24) {
        PBAmountField(text: $empty, currency: Currency(code: "INR"), date: "Today").pbPreviewInteraction(.focused)
        PBAmountField(text: $amount, currency: Currency(code: "INR"), date: "Today").pbPreviewInteraction(.focused)
        PBAmountField(text: $amount, currency: Currency(code: "INR"), helper: "You owe Meera ₹450 in Flat 302")
    }
}
