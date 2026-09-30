import SwiftUI

/// Control / Category Chip (Figma 64:4185): a 36 pt filter or people chip. Selected = black fill with
/// white text. Leading: nothing, a 16 pt icon (the "Add" chip uses Plus) or a 24 pt avatar, which
/// sits in a white circle on the grey chip so the face reads. `onRemove` adds a trailing 16 pt ✕ that
/// removes the chip. The screen decides single or multi select. The hit area reaches 44 pt tall.
struct PBCategoryChip: View {
    enum Leading {
        case none
        case icon(PBIcon)
        case avatar(PBAvatar.Content)
    }

    let label: String
    var leading: Leading = .none
    var isSelected = false
    var onRemove: (() -> Void)?
    let action: () -> Void

    init(
        _ label: String,
        leading: Leading = .none,
        isSelected: Bool = false,
        onRemove: (() -> Void)? = nil,
        action: @escaping () -> Void
    ) {
        self.label = label
        self.leading = leading
        self.isSelected = isSelected
        self.onRemove = onRemove
        self.action = action
    }

    var body: some View {
        Button(action: action) {
            HStack(spacing: gap) {
                leadingView
                Text(label)
                    .textStyle(.buttonSmall)
                    .lineLimit(1)
                if onRemove != nil {
                    // The ✕ itself is the overlay button below; this keeps its place in the chip.
                    Color.clear.frame(width: PBSize.iconSm, height: PBSize.iconSm)
                }
            }
            .padding(.leading, leadingPadding)
            .padding(.trailing, PBSpace.s16)
        }
        .buttonStyle(ChipStyle(isSelected: isSelected))
        .overlay(alignment: .trailing) {
            if let onRemove {
                removeButton(onRemove)
            }
        }
        .accessibilityAddTraits(isSelected ? .isSelected : [])
    }

    @ViewBuilder
    private var leadingView: some View {
        switch leading {
        case .none:
            EmptyView()
        case .icon(let icon):
            PBIconView(icon, size: PBSize.iconSm)
        case .avatar(let content):
            PBAvatar(content, diameter: PBSize.avatarXs, isOnCard: !isSelected)
        }
    }

    private func removeButton(_ onRemove: @escaping () -> Void) -> some View {
        Button(action: onRemove) {
            PBIconView(.close, size: PBSize.iconSm)
                .foregroundStyle(isSelected ? PBColor.iconInverse : PBColor.iconPrimary)
                .frame(width: PBSize.tap, height: PBSize.tap)
                .contentShape(.rect)
        }
        .buttonStyle(.plain)
        // The 16 pt icon keeps its place 16 pt from the chip's end; the 44 pt target overhangs.
        .padding(.trailing, PBSpace.s16 - (PBSize.tap - PBSize.iconSm) / 2)
        .accessibilityLabel("Remove \(label)")
    }

    private var gap: CGFloat {
        if case .avatar = leading { return PBSpace.s8 }
        return PBSpace.s6
    }

    private var leadingPadding: CGFloat {
        switch leading {
        case .none: PBSpace.s16
        case .icon: PBSpace.s12
        case .avatar: PBSpace.s6
        }
    }
}

private struct ChipStyle: ButtonStyle {
    let isSelected: Bool

    @Environment(\.pbPreviewInteraction) private var previewInteraction

    private let hitSlop = (PBSize.tap - PBSize.buttonSm) / 2

    func makeBody(configuration: Configuration) -> some View {
        let isPressed = configuration.isPressed || previewInteraction == .pressed
        configuration.label
            .foregroundStyle(isSelected ? PBColor.textInverse : PBColor.textPrimary)
            .frame(height: PBSize.buttonSm)
            .background(fill(isPressed: isPressed), in: .capsule)
            .animation(.easeOut(duration: 0.1), value: isPressed)
            .padding(.vertical, hitSlop)
            .contentShape(.capsule)
            .padding(.vertical, -hitSlop)
    }

    private func fill(isPressed: Bool) -> Color {
        switch (isSelected, isPressed) {
        case (false, false): PBColor.bgCard
        case (false, true): PBColor.bgCardPressed
        case (true, false): PBColor.bgInverse
        case (true, true): PBColor.bgInversePressed
        }
    }
}

#Preview("PBCategoryChip") {
    @Previewable @State var selected = true
    VStack(alignment: .leading, spacing: PBSpace.s12) {
        HStack {
            PBCategoryChip("Hair") {}
            PBCategoryChip("Hair", isSelected: true) {}
        }
        HStack {
            PBCategoryChip("Hair", leading: .icon(.plus)) {}
            PBCategoryChip("Hair", leading: .icon(.plus), isSelected: true) {}
        }
        HStack {
            PBCategoryChip("Hair", leading: .avatar(.art(.priya))) {}
            PBCategoryChip("Hair", leading: .avatar(.art(.priya)), isSelected: selected) { selected.toggle() }
        }
        HStack {
            PBCategoryChip("Priya", leading: .avatar(.art(.priya)), onRemove: {}) {}
            PBCategoryChip("Food", isSelected: true, onRemove: {}) {}
        }
    }
    .padding(PBLayout.screenMargin)
}
