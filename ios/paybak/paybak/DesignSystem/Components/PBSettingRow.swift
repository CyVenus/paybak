import SwiftUI

/// Row / Setting (Figma 97:996): a settings or picker row inside a #F5F5F5 group. At least 56 pt
/// (taller with a subtitle), 12/16 padding: an optional 24 pt icon, Headline title and Footnote
/// subtitle, an optional inverse badge ("Pro", "Try free"), an optional Body value, then the trailing
/// control. Destructive rows are red and never show a chevron. The divider starts at the title
/// (x 52, or 16 without an icon); hide it on a group's last row.
///
/// Stack rows with no gap in `VStack(spacing: 0) { … }.pbCard(padding: 0)`, which clips the pressed
/// overlay to the group's corners.
struct PBSettingRow: View {
    enum Trailing {
        /// Pushes a screen.
        case chevron
        /// The system switch, its On track tinted black. Tapping the row flips it too.
        case toggle(Binding<Bool>)
        /// The system stepper.
        case stepper(Binding<Int>, in: ClosedRange<Int>)
        /// Single-select lists: the chosen row.
        case check
        /// Single-select lists: an empty slot so titles line up with the checked row.
        case unchecked
        case none
    }

    enum Tone {
        case `default`
        case destructive
    }

    let title: String
    var subtitle: String?
    var value: String?
    var icon: PBIcon?
    var badge: String?
    var trailing: Trailing = .chevron
    var tone: Tone = .default
    var showsDivider = true
    var action: (() -> Void)?

    init(
        _ title: String,
        subtitle: String? = nil,
        value: String? = nil,
        icon: PBIcon? = nil,
        badge: String? = nil,
        trailing: Trailing = .chevron,
        tone: Tone = .default,
        showsDivider: Bool = true,
        action: (() -> Void)? = nil
    ) {
        self.title = title
        self.subtitle = subtitle
        self.value = value
        self.icon = icon
        self.badge = badge
        self.trailing = trailing
        self.tone = tone
        self.showsDivider = showsDivider
        self.action = action
    }

    var body: some View {
        switch trailing {
        case .toggle(let isOn):
            Button { isOn.wrappedValue.toggle() } label: { row }
                .buttonStyle(PBRowButtonStyle(surface: .card))
                .accessibilityRepresentation { Toggle(title, isOn: isOn) }
        case .stepper(let count, let range):
            row
                .accessibilityRepresentation {
                    Stepper(title, value: count, in: range)
                        .accessibilityValue(value ?? "\(count.wrappedValue)")
                }
        case .chevron, .check, .unchecked, .none:
            if let action {
                Button(action: action) { row }
                    .buttonStyle(PBRowButtonStyle(surface: .card))
                    .accessibilityElement(children: .combine)
                    .accessibilityAddTraits(isChecked ? .isSelected : [])
            } else {
                row
                    .accessibilityElement(children: .combine)
            }
        }
    }

    private var isChecked: Bool {
        if case .check = trailing { return true }
        return false
    }

    private var row: some View {
        HStack(spacing: PBSpace.s12) {
            if let icon {
                PBIconView(icon)
                    .foregroundStyle(tone == .destructive ? PBColor.iconDestructive : PBColor.iconPrimary)
            }
            VStack(alignment: .leading, spacing: PBSpace.s2) {
                Text(title)
                    .textStyle(.headline)
                    .foregroundStyle(tone == .destructive ? PBColor.textDestructive : PBColor.textPrimary)
                    .lineLimit(1)
                if let subtitle {
                    Text(subtitle)
                        .textStyle(.footnote)
                        .foregroundStyle(PBColor.textSecondary)
                }
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            if let badge {
                PBBadge(badge, style: .inverse)
            }
            if let value {
                // The value keeps its width; a long title truncates first.
                Text(value)
                    .textStyle(.body)
                    .foregroundStyle(PBColor.textSecondary)
                    .lineLimit(1)
                    .layoutPriority(1)
            }
            trailingView
        }
        .padding(.vertical, PBSpace.s12)
        .padding(.horizontal, PBSpace.s16)
        .frame(minHeight: 56)
        .overlay(alignment: .bottom) {
            if showsDivider {
                PBDivider()
                    .padding(.leading, icon == nil ? PBSpace.s16 : PBSpace.s16 + PBSize.iconLg + PBSpace.s12)
            }
        }
        .contentShape(.rect)
    }

    @ViewBuilder
    private var trailingView: some View {
        switch trailing {
        case .chevron:
            if tone == .default {
                PBIconView(.chevronRight, size: PBSize.iconMd)
                    .foregroundStyle(PBColor.iconTertiary)
            }
        case .toggle(let isOn):
            // The row's button flips it, so a tap on the switch itself doesn't flip it twice.
            Toggle(title, isOn: isOn)
                .labelsHidden()
                .tint(PBColor.bgInverse)
                .allowsHitTesting(false)
        case .stepper(let count, let range):
            Stepper(title, value: count, in: range)
                .labelsHidden()
        case .check:
            PBIconView(.check)
                .foregroundStyle(PBColor.iconPrimary)
        case .unchecked:
            Color.clear.frame(width: PBSize.iconLg, height: PBSize.iconLg)
        case .none:
            EmptyView()
        }
    }
}

#Preview("PBSettingRow") {
    @Previewable @State var isOn = true
    @Previewable @State var count = 2
    ScrollView {
        VStack(spacing: PBSpace.s24) {
            VStack(spacing: 0) {
                PBSettingRow("Payment details", value: "UPI", icon: .wallet) {}
                PBSettingRow("Reminders", icon: .bell, trailing: .toggle($isOn))
                PBSettingRow("Remind after", value: "\(count) days", icon: .calendar, trailing: .stepper($count, in: 1...14))
                PBSettingRow("Paybak Pro", icon: .crown, badge: "Try free") {}
                PBSettingRow("Sign out", icon: .logout, trailing: .none, tone: .destructive, showsDivider: false) {}
            }
            .pbCard(padding: 0)
            VStack(spacing: 0) {
                PBSettingRow("Food", icon: .food, trailing: .check) {}
                PBSettingRow("Travel", icon: .car, trailing: .unchecked) {}
                PBSettingRow("Paid back in parts", subtitle: "Paid back in parts", trailing: .unchecked, showsDivider: false) {}
            }
            .pbCard(padding: 0)
        }
        .padding(PBLayout.screenMargin)
    }
}
