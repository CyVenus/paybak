import SwiftUI

/// Row / Person (Figma 127:2252): the one people row (split pickers, balances, breakdowns, contacts,
/// chat answers). Avatar, Headline name with an optional "Guest" pill beside it, Subheadline
/// subtitle, a trailing element and a divider that starts at the name.
///
/// Regular (64 pt, 40 pt avatar) is for white surfaces; set `isOnCard` inside a #F5F5F5 card to turn
/// the avatar white. Compact (56 pt, 32 pt white avatar, On Card pill and button) is drawn for cards.
/// The whole row is the tap target when `action` is set; Remove and Button keep their own targets.
struct PBPersonRow: View {
    enum Size {
        case regular
        case compact
    }

    enum Trailing {
        /// Owed "+₹700" / Owe "−₹700" (Amount/Medium) or, with no direction, a plain value ("₹700",
        /// "25%", Headline). An optional Footnote label ("Due Sun 4 Oct") and red status pill
        /// ("Overdue 3 days") stack under it.
        case amount(String, direction: PBAmountDirection? = nil, label: String? = nil, overdue: String? = nil)
        /// Muted status: "Settled", "Added", "No balance".
        case status(String)
        /// Single select (Paid by).
        case check
        /// Multi select: the black ticked circle or the empty ring.
        case select(Bool)
        case remove(() -> Void)
        /// "Invite" (Secondary) on Regular, "Remind" (On Card) on Compact.
        case button(String, () -> Void)
        case none
    }

    let name: String
    let avatar: PBAvatar.Content
    var subtitle: String?
    /// The Muted pill next to the name ("Guest").
    var tag: String?
    var size: Size = .regular
    var isOnCard = false
    var trailing: Trailing = .none
    var showsDivider = true
    var action: (() -> Void)?

    private var isCompact: Bool { size == .compact }
    private var surfaceIsCard: Bool { isCompact || isOnCard }

    var body: some View {
        Group {
            if let action {
                Button(action: action) {
                    // A control with its own target sits over the row; this copy only keeps its place.
                    row(trailing: trailingView.opacity(hasOwnTarget ? 0 : 1).accessibilityHidden(hasOwnTarget))
                }
                .buttonStyle(PBRowButtonStyle(surface: surfaceIsCard ? .card : .white))
                .overlay(alignment: .trailing) {
                    if hasOwnTarget {
                        trailingView.padding(.trailing, PBSpace.s16)
                    }
                }
            } else {
                row(trailing: trailingView)
            }
        }
        .overlay(alignment: .bottom) {
            if showsDivider {
                PBDivider().padding(.leading, isCompact ? 60 : 68)
            }
        }
    }

    private var hasOwnTarget: Bool {
        switch trailing {
        case .remove, .button: true
        default: false
        }
    }

    private func row(trailing: some View) -> some View {
        HStack(spacing: PBSpace.s12) {
            PBAvatar(avatar, diameter: isCompact ? PBSize.avatarSm : PBSize.avatarMd, isOnCard: surfaceIsCard)
            VStack(alignment: .leading, spacing: PBSpace.s2) {
                HStack(spacing: PBSpace.s8) {
                    // At most 200 pt (180 on Compact), so a tag beside a long name stays in view.
                    Text(name)
                        .textStyle(.headline)
                        .foregroundStyle(PBColor.textPrimary)
                        .lineLimit(1)
                        .pbMaxWidth(isCompact ? 180 : 200)
                    if let tag {
                        PBBadge(tag, style: surfaceIsCard ? .onCard : .muted)
                    }
                }
                if let subtitle {
                    Text(subtitle)
                        .textStyle(.subheadline)
                        .foregroundStyle(PBColor.textSecondary)
                        .lineLimit(1)
                }
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            trailing
        }
        .padding(.vertical, isCompact ? PBSpace.s6 : PBSpace.s8)
        .padding(.horizontal, PBSpace.s16)
        .frame(minHeight: isCompact ? 56 : 64)
        .contentShape(.rect)
        // Remove and Button stay their own VoiceOver elements when they sit inside the row.
        .accessibilityElement(children: hasOwnTarget && action == nil ? .contain : .combine)
        .accessibilityAddTraits(isSelected ? .isSelected : [])
    }

    private var isSelected: Bool {
        switch trailing {
        case .check: true
        case .select(let isOn): isOn
        default: false
        }
    }

    @ViewBuilder
    private var trailingView: some View {
        switch trailing {
        case .amount(let amount, let direction, let label, let overdue):
            VStack(alignment: .trailing, spacing: PBSpace.s2) {
                if let direction {
                    PBSignedAmount(amount, direction: direction)
                } else {
                    Text(amount)
                        .textStyle(.headline)
                        .foregroundStyle(PBColor.textPrimary)
                        .lineLimit(1)
                }
                if let label {
                    Text(label)
                        .textStyle(.footnote)
                        .foregroundStyle(PBColor.textTertiary)
                        .lineLimit(1)
                }
                if let overdue {
                    PBBadge(overdue, style: .overdue)
                }
            }
        case .status(let status):
            Text(status)
                .textStyle(.subheadline)
                .foregroundStyle(PBColor.textSecondary)
                .lineLimit(1)
        case .check:
            PBIconView(.check)
                .foregroundStyle(PBColor.iconPrimary)
        case .select(let isOn):
            PBSelectCircle(isOn: isOn)
        case .remove(let remove):
            Button(action: remove) {
                PBIconView(.close, size: PBSize.iconMd)
                    .foregroundStyle(PBColor.iconSecondary)
                    .frame(width: PBSize.tap, height: PBSize.tap)
                    .contentShape(.rect)
            }
            .buttonStyle(PBDimButtonStyle())
            .padding(.horizontal, -(PBSize.tap - PBSize.iconMd) / 2)
            .accessibilityLabel("Remove \(name)")
        case .button(let label, let action):
            PBButton(label, style: surfaceIsCard ? .onCard : .secondary, size: .small, action: action)
        case .none:
            EmptyView()
        }
    }
}

#Preview("PBPersonRow") {
    ScrollView {
        VStack(spacing: 0) {
            PBPersonRow(name: "Priya", avatar: .art(.priya), subtitle: "Dinner at Olive Garden", trailing: .amount("₹700", direction: .owed, label: "Due Sun 4 Oct"))
            PBPersonRow(name: "Priya", avatar: .art(.priya), subtitle: "Dinner at Olive Garden", trailing: .amount("₹700", direction: .owe, label: "Due Sun 4 Oct", overdue: "Overdue 3 days"))
            PBPersonRow(name: "Priya", avatar: .art(.priya), subtitle: "Dinner at Olive Garden", trailing: .status("Settled"))
            PBPersonRow(name: "Priya", avatar: .art(.priya), subtitle: "Dinner at Olive Garden", trailing: .select(true)) {}
            PBPersonRow(name: "Arjun R", avatar: .initials("AR"), subtitle: "Not on Paybak", tag: "Guest", trailing: .button("Invite") {})
            PBPersonRow(name: "Priya", avatar: .art(.priya), subtitle: "Dinner at Olive Garden", trailing: .remove {}) {}
        }
        VStack(spacing: 0) {
            PBPersonRow(name: "Priya", avatar: .art(.priya), subtitle: "Dinner at Olive Garden", size: .compact, trailing: .amount("₹700"))
            PBPersonRow(name: "Rohan", avatar: .art(.rohan), subtitle: "Movie tickets", size: .compact, trailing: .button("Remind") {}, showsDivider: false)
        }
        .pbCard(padding: 0)
        .padding(PBLayout.screenMargin)
    }
}
