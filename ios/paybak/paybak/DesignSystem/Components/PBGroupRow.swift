import SwiftUI

/// Row / Group (Figma 139:2052): a group or project in the Groups list and in "Groups together" on
/// a friend page. On white with no side padding (it lives in the screen margins): a 40 pt icon tile
/// (Plane, Home, People, Tag; Drone or Package for projects), the Headline name and Footnote
/// subtitle, and the balance on the right: "−₹1,400 / You owe", "+₹1,400 / You’re owed" or a status
/// ("Settled", "You’re settled"). Projects add a small budget bar with "₹52,000 of ₹60,000" and
/// "₹8,000 left" under the text (red after the budget point when over budget). Archived groups are
/// gray and read "Read-only" whatever the balance. The divider starts at 52.
struct PBGroupRow: View {
    enum Balance {
        case owe(String)
        case owed(String)
        case settled(String = "Settled")
    }

    struct Budget {
        /// Spent ÷ budget, 0…1; over budget, budget ÷ spent (where the red starts).
        let progress: Double
        /// "₹52,000 of ₹60,000".
        let spent: String
        /// "₹8,000 left", or "₹2,000 over".
        let left: String
        /// Over budget: the fill stops square at `progress` and red runs to the end.
        var isOver = false
    }

    let name: String
    let subtitle: String
    let icon: PBIcon
    var balance: Balance = .settled()
    var budget: Budget?
    var isArchived = false
    var showsDivider = true
    var action: (() -> Void)?

    var body: some View {
        Group {
            if let action {
                Button(action: action) { row }
                    .buttonStyle(PBRowButtonStyle(surface: .white))
            } else {
                row
            }
        }
        .overlay(alignment: .bottom) {
            if showsDivider {
                PBDivider(inset: .leading)
            }
        }
    }

    private var row: some View {
        VStack(spacing: PBSpace.s12) {
            HStack(spacing: PBSpace.s12) {
                PBAvatar(.icon(icon), iconTint: isArchived ? PBColor.iconTertiary : PBColor.iconPrimary)
                VStack(alignment: .leading, spacing: 0) {
                    Text(name)
                        .textStyle(.headline)
                        .foregroundStyle(isArchived ? PBColor.textTertiary : PBColor.textPrimary)
                    Text(subtitle)
                        .textStyle(.footnote)
                        .foregroundStyle(PBColor.textSecondary)
                }
                .lineLimit(1)
                .frame(maxWidth: .infinity, alignment: .leading)
                trailing
            }
            if let budget {
                HStack(spacing: PBSpace.s12) {
                    Color.clear.frame(width: PBSize.avatarMd, height: 0)
                    VStack(spacing: PBSpace.s8) {
                        PBProgressBar(value: budget.progress, overFrom: budget.isOver ? budget.progress : nil,
                                      accessibilityLabel: "Budget used")
                        HStack {
                            Text(budget.spent).textStyle(.footnote)
                            Spacer(minLength: PBSpace.s8)
                            Text(budget.left).textStyle(.footnote)
                        }
                        .foregroundStyle(PBColor.textSecondary)
                        .lineLimit(1)
                    }
                }
            }
        }
        .padding(.vertical, PBSpace.s16)
        .contentShape(.rect)
        .accessibilityElement(children: .combine)
    }

    @ViewBuilder
    private var trailing: some View {
        if isArchived {
            Text("Read-only")
                .textStyle(.subheadline)
                .foregroundStyle(PBColor.textTertiary)
        } else {
            switch balance {
            case .owe(let amount):
                amountColumn(PBSignedAmount(amount, direction: .owe), label: "You owe")
            case .owed(let amount):
                amountColumn(PBSignedAmount(amount, direction: .owed), label: "You’re owed")
            case .settled(let status):
                Text(status)
                    .textStyle(.subheadline)
                    .foregroundStyle(PBColor.textSecondary)
            }
        }
    }

    private func amountColumn(_ amount: PBSignedAmount, label: String) -> some View {
        VStack(alignment: .trailing, spacing: 0) {
            amount
            Text(label)
                .textStyle(.footnote)
                .foregroundStyle(PBColor.textSecondary)
        }
    }
}

#Preview("PBGroupRow") {
    let budget = PBGroupRow.Budget(progress: 52_000.0 / 60_000, spent: "₹52,000 of ₹60,000", left: "₹8,000 left")
    ScrollView {
        VStack(spacing: 0) {
            PBGroupRow(name: "Goa Trip", subtitle: "5 members · Due Fri 2 Oct", icon: .plane, balance: .owe("₹1,400")) {}
            PBGroupRow(name: "Flat 302", subtitle: "3 members", icon: .home, balance: .owed("₹1,400")) {}
            PBGroupRow(name: "Drone build", subtitle: "4 members", icon: .drone, balance: .owe("₹1,400"), budget: budget) {}
            PBGroupRow(name: "Drone build", subtitle: "4 members", icon: .drone, balance: .settled("You’re settled"), budget: budget) {}
            PBGroupRow(name: "Old flat", subtitle: "Archived 12 Aug", icon: .package, isArchived: true, showsDivider: false) {}
        }
        .padding(PBLayout.screenMargin)
    }
}
