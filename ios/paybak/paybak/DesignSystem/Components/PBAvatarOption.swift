import SwiftUI

/// Control / Avatar Option (Figma 37:655): a 56 pt picker option with a 46 pt avatar circle.
/// Selected = a 2.5 pt black ring inside the 56 circle, leaving a 2.5 pt white gap. The Upload option
/// shows the camera icon until the user picks a photo; the photo then shows with the selected ring.
/// Narrow screens can pass a smaller `diameter`; ring and gap stay 2.5 pt.
struct PBAvatarOption: View {
    enum Kind {
        case art(PBPeepHead)
        /// The camera tile, before a photo is chosen.
        case upload
        /// A chosen photo, drawn like Art (aspect-fill, clipped).
        case photo(Image)
    }

    let kind: Kind
    let isSelected: Bool
    var diameter: CGFloat = PBSize.avatarLg
    let action: () -> Void

    init(kind: Kind, isSelected: Bool, diameter: CGFloat = PBSize.avatarLg, action: @escaping () -> Void) {
        self.kind = kind
        self.isSelected = isSelected
        self.diameter = diameter
        self.action = action
    }

    @Environment(\.accessibilityReduceMotion) private var reduceMotion

    /// Ring and gap, 2.5 pt each, around the avatar.
    private var avatarDiameter: CGFloat { diameter - 10 }

    var body: some View {
        Button(action: action) {
            ZStack {
                if isSelected {
                    Circle()
                        .strokeBorder(PBColor.borderStrong, lineWidth: 2.5)
                        .transition(.opacity)
                }
                inner
            }
            .frame(width: diameter, height: diameter)
            .contentShape(.circle)
        }
        .buttonStyle(.plain)
        .animation(reduceMotion ? nil : .easeOut(duration: 0.15), value: isSelected)
        .accessibilityLabel(accessibilityLabel)
        .accessibilityAddTraits(isSelected ? .isSelected : [])
    }

    @ViewBuilder
    private var inner: some View {
        switch kind {
        case .art(let head):
            PBAvatar(.art(head), diameter: avatarDiameter)
        case .photo(let image):
            PBAvatar(.photo(image), diameter: avatarDiameter)
        case .upload:
            PBIconView(.camera)
                .foregroundStyle(PBColor.iconPrimary)
                .frame(width: avatarDiameter, height: avatarDiameter)
                .background(PBColor.bgCard, in: .circle)
        }
    }

    private var accessibilityLabel: String {
        switch kind {
        case .art(let head): "Avatar \(head.name)"
        case .upload: "Choose a photo"
        case .photo: "Your photo"
        }
    }
}

#Preview("PBAvatarOption") {
    @Previewable @State var selected = 0
    HStack(spacing: 5) {
        ForEach(Array(PBPeepHead.presets.enumerated()), id: \.offset) { index, head in
            PBAvatarOption(kind: .art(head), isSelected: selected == index) { selected = index }
        }
        PBAvatarOption(kind: .upload, isSelected: false) {}
    }
}
