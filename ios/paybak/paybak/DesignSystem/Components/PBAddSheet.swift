import SwiftUI

/// Sheet / Action Sheet (Figma 17:643, components-home §14): the ＋ Add sheet. "Add" in Title/3 with
/// the glass ✕, then four action rows. Present it with `.pbSheet` (fitted): the system draws the
/// grabber, the white 40 pt sheet and the scrim. Test ids: `home.addSheet`, `home.addSheet.close`,
/// `home.addSheet.<expense|payment|lend|group>`.
struct PBAddSheet: View {
    enum Action: String, CaseIterable, Identifiable {
        case expense
        case payment
        case lend
        case group

        var id: String { rawValue }

        var title: String {
            switch self {
            case .expense: "Add expense"
            case .payment: "Record payment"
            case .lend: "Lend money (IOU)"
            case .group: "New group"
            }
        }

        var subtitle: String {
            switch self {
            case .expense: "Split a bill with friends or a group"
            case .payment: "Log money you paid or received"
            case .lend: "Track a loan and when it’s due"
            case .group: "Flatmates, a trip or a project"
            }
        }

        var icon: PBIcon {
            switch self {
            case .expense: .receipt
            case .payment: .exchange
            case .lend: .lend
            case .group: .groups
            }
        }
    }

    let onClose: () -> Void
    let onSelect: (Action) -> Void

    var body: some View {
        PBSheet(title: "Add", testIDPrefix: "home.addSheet", onClose: onClose) {
            VStack(spacing: 0) {
                ForEach(Action.allCases) { action in
                    PBSheetRow(title: action.title, subtitle: action.subtitle, icon: action.icon) { onSelect(action) }
                        .accessibilityIdentifier("home.addSheet.\(action.rawValue)")
                }
            }
        }
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier("home.addSheet")
    }
}

/// Sheet / Action Row (Figma 17:641, components-home §13): a 72 pt row with a 44 pt icon tile, a
/// title and subtitle, and a chevron. Pressed fills the row `bg/card` and turns the tile white.
struct PBSheetRow: View {
    let title: String
    let subtitle: String
    let icon: PBIcon
    var showsChevron = true
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            EmptyView()
        }
        .buttonStyle(PBSheetRowStyle(title: title, subtitle: subtitle, icon: icon, showsChevron: showsChevron))
        .accessibilityLabel(title)
        .accessibilityHint(subtitle)
    }
}

private struct PBSheetRowStyle: ButtonStyle {
    let title: String
    let subtitle: String
    let icon: PBIcon
    let showsChevron: Bool

    @Environment(\.pbPreviewInteraction) private var previewInteraction

    func makeBody(configuration: Configuration) -> some View {
        let isPressed = configuration.isPressed || previewInteraction == .pressed
        HStack(spacing: PBSpace.s12) {
            PBIconView(icon)
                .foregroundStyle(PBColor.iconPrimary)
                .frame(width: PBSize.tap, height: PBSize.tap)
                .background(isPressed ? PBColor.bgPrimary : PBColor.bgCard, in: .rect(cornerRadius: PBRadius.tile))
            VStack(alignment: .leading, spacing: PBSpace.s2) {
                Text(title)
                    .textStyle(.headline)
                    .foregroundStyle(PBColor.textPrimary)
                Text(subtitle)
                    .textStyle(.subheadline)
                    .foregroundStyle(PBColor.textSecondary)
            }
            .lineLimit(1)
            .frame(maxWidth: .infinity, alignment: .leading)
            if showsChevron {
                PBIconView(.chevronRight, size: PBSize.iconMd)
                    .foregroundStyle(PBColor.iconTertiary)
            }
        }
        .padding(.horizontal, PBSpace.s12)
        .frame(height: 72)
        .background(isPressed ? PBColor.bgCard : .clear, in: .rect(cornerRadius: PBRadius.card))
        .contentShape(.rect)
        .animation(.easeOut(duration: 0.1), value: isPressed)
    }
}

#Preview("PBAddSheet") {
    @Previewable @State var isPresented = true
    PBButton("Open the Add sheet") { isPresented = true }
        .pbSheet(isPresented: $isPresented) {
            PBAddSheet(onClose: { isPresented = false }, onSelect: { _ in isPresented = false })
        }
}

#Preview("PBSheetRow") {
    VStack(spacing: 0) {
        PBSheetRow(title: "Add expense", subtitle: "Split a bill with friends or a group", icon: .receipt) {}
        PBSheetRow(title: "Record payment", subtitle: "Log money you paid or received", icon: .exchange) {}
            .pbPreviewInteraction(.pressed)
    }
    .padding(PBSpace.s16)
}
