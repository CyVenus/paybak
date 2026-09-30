import SwiftUI

/// Card / Balance (Figma 13:269, components-home §6): a balance total on a #F5F5F5 card. Owed = black
/// +₹, Owe = gray −₹, Settled = gray ₹0. Tapping it opens the per-person breakdown. The top-right
/// holds either the chevron or a badge; an optional small "Settle up" sits bottom-right, 24 pt above
/// the card's bottom edge. Detail pages override the label ("Your balance", "Rohan owes you") and may
/// drop the caption (a new group's 96 pt card).
struct PBBalanceCard: View {
    enum Kind {
        case owed
        case owe
        case settled

        var icon: PBIcon {
            switch self {
            case .owed: .moneyIn
            case .owe: .moneyOut
            case .settled: .checkCircle
            }
        }

        var label: String {
            switch self {
            case .owed: "You’re owed"
            case .owe: "You owe"
            case .settled: "All settled"
            }
        }

        var amountColor: Color {
            switch self {
            case .owed: PBColor.textPrimary
            case .owe: PBColor.textSecondary
            case .settled: PBColor.textTertiary
            }
        }
    }

    let kind: Kind
    /// Replaces the kind's label ("Your balance", "Rohan owes you").
    var label: String?
    /// Formatted with its sign ("+₹2,900", "−₹1,850", "₹0").
    let amount: String
    /// nil hides the caption line.
    let caption: String?
    /// A status pill in the top-right instead of the chevron.
    var badge: (text: String, style: PBBadge.Style)?
    /// Shows the trailing small "Settle up".
    var onSettleUp: (() -> Void)?
    /// A disabled "Settle up" (nothing to settle yet).
    var isSettleUpEnabled = true
    var onTap: (() -> Void)?
    /// Test ids `<prefix>.balance` (the card) and `<prefix>.settleUp`.
    var testIDPrefix: String?

    var body: some View {
        Button {
            onTap?()
        } label: {
            VStack(alignment: .leading, spacing: PBSpace.s12) {
                HStack(spacing: PBSpace.s6) {
                    PBIconView(kind.icon, size: PBSize.iconSm)
                        .foregroundStyle(PBColor.iconSecondary)
                    Text(label ?? kind.label)
                        .textStyle(.subheadline)
                        .foregroundStyle(PBColor.textSecondary)
                        .lineLimit(1)
                        .frame(maxWidth: .infinity, alignment: .leading)
                    if let badge {
                        PBBadge(badge.text, style: badge.style)
                    } else if onTap != nil {
                        PBIconView(.chevronRight, size: PBSize.iconSm)
                            .foregroundStyle(PBColor.iconTertiary)
                    }
                }
                // A badge (24 pt) makes the top row taller.
                .frame(height: badge == nil ? PBSpace.s20 : PBSpace.s24)
                HStack(alignment: .bottom, spacing: PBSpace.s12) {
                    VStack(alignment: .leading, spacing: PBSpace.s2) {
                        Text(amount)
                            .textStyle(.amountLarge)
                            .foregroundStyle(kind.amountColor)
                            .lineLimit(1)
                            // The exact 32 pt line is shorter than the glyphs; without a fixed height the
                            // text treats that as not fitting and shrinks or clips itself.
                            .fixedSize(horizontal: false, vertical: true)
                            .minimumScaleFactor(0.6)
                        if let caption {
                            Text(caption)
                                .textStyle(.footnote)
                                .foregroundStyle(PBColor.textTertiary)
                                .lineLimit(1)
                        }
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)
                    if onSettleUp != nil {
                        // Keeps the text clear of the button, which floats over the card.
                        settleUpButton.hidden().frame(height: 0)
                    }
                }
            }
            .padding(PBLayout.cardPadding)
            .frame(maxWidth: .infinity, alignment: .leading)
        }
        .buttonStyle(PBBalanceCardStyle())
        .disabled(onTap == nil)
        .accessibilityElement(children: .combine)
        .accessibilityIdentifier(testIDPrefix.map { "\($0).balance" } ?? "")
        // Figma pins "Settle up" 16 from the right and 24 from the bottom, over the layout.
        .overlay(alignment: .bottomTrailing) {
            if onSettleUp != nil {
                settleUpButton
                    .disabled(!isSettleUpEnabled)
                    .padding(.trailing, PBLayout.cardPadding)
                    .padding(.bottom, PBSpace.s24)
                    .accessibilityIdentifier(testIDPrefix.map { "\($0).settleUp" } ?? "")
            }
        }
    }

    private var settleUpButton: some View {
        PBButton("Settle up", size: .small) { onSettleUp?() }
    }
}

/// The card is tappable: pressed swaps the fill to `bg/card-pressed`.
private struct PBBalanceCardStyle: ButtonStyle {
    @Environment(\.pbPreviewInteraction) private var previewInteraction

    func makeBody(configuration: Configuration) -> some View {
        let isPressed = configuration.isPressed || previewInteraction == .pressed
        configuration.label
            .background(isPressed ? PBColor.bgCardPressed : PBColor.bgCard, in: .rect(cornerRadius: PBRadius.card))
            .contentShape(.rect(cornerRadius: PBRadius.card))
            .animation(.easeOut(duration: 0.1), value: isPressed)
    }
}

/// Card / Balance Summary (Figma 13:271, components-home §7): the Owed and Owe cards side by side
/// (12 apart, equal widths) and the full-width "Settle up". Test ids: `home.balance.owed`,
/// `home.balance.owe`, `home.settleUp`.
struct PBBalanceSummary: View {
    let totals: HomeTotals
    var currency = "INR"
    var onOwed: () -> Void = {}
    var onOwe: () -> Void = {}
    /// nil hides the Settle up button.
    var onSettleUp: (() -> Void)?

    var body: some View {
        VStack(spacing: PBSpace.s12) {
            HStack(alignment: .top, spacing: PBSpace.s12) {
                PBBalanceCard(kind: .owed, amount: Money.format(totals.owed, currency, sign: .signed),
                              caption: totals.owed > 0 ? totals.owedCaption : "Nothing pending", onTap: onOwed)
                    .accessibilityIdentifier("home.balance.owed")
                PBBalanceCard(kind: .owe, amount: Money.format(-totals.owe, currency, sign: .signed),
                              caption: totals.owe > 0 ? totals.oweCaption : "Nothing to pay", onTap: onOwe)
                    .accessibilityIdentifier("home.balance.owe")
            }
            .fixedSize(horizontal: false, vertical: true)
            if let onSettleUp {
                PBButton("Settle up", fillsWidth: true, action: onSettleUp)
                    .accessibilityIdentifier("home.settleUp")
            }
        }
    }
}

#Preview("PBBalanceCard") {
    VStack(spacing: PBSpace.s12) {
        PBBalanceSummary(totals: HomeTotals(owed: 290_000, owedPeople: 4, owe: 185_000, oweGroups: 2), onSettleUp: {})
        PBBalanceCard(kind: .settled, amount: "₹0", caption: "Nothing pending")
        PBBalanceCard(kind: .owed, amount: "+₹800", caption: "Movie tickets", badge: ("Overdue 3 days", .overdue))
        PBBalanceCard(kind: .owe, amount: "−₹1,400", caption: "Goa Trip", onSettleUp: {}, onTap: {})
        PBBalanceCard(kind: .owe, label: "Your balance", amount: "−₹1,400", caption: "You owe Kabir · Due Fri 2 Oct", onSettleUp: {})
        PBBalanceCard(kind: .settled, label: "Your balance", amount: "₹0", caption: nil, onSettleUp: {}, isSettleUpEnabled: false)
    }
    .padding(PBLayout.screenMargin)
}
