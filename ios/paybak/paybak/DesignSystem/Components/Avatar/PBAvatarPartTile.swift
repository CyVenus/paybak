import SwiftUI

/// Control / Avatar Part Tile (Figma 64:4176): one option in the editor grid, a square `bg/card` tile
/// with 20 pt corners previewing the look with that option (Head crop, or Bust for outfits). Selected
/// = a 2 pt black ring at the edge with a 2 pt white gap inside it, drawn over the art. The tile shows
/// no text; `name` is its accessibility label.
struct PBAvatarPartTile: View {
    let look: AvatarLook
    var crop: AvatarCrop = .head
    let name: String
    let isSelected: Bool
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            AvatarCharacterView(look: look, crop: crop)
                .aspectRatio(1, contentMode: .fit)
                .background(PBColor.bgCard)
                .clipShape(.rect(cornerRadius: PBRadius.card))
                .overlay {
                    if isSelected {
                        RoundedRectangle(cornerRadius: 18)
                            .strokeBorder(PBColor.bgPrimary, lineWidth: 2)
                            .padding(2)
                        RoundedRectangle(cornerRadius: PBRadius.card)
                            .strokeBorder(PBColor.borderStrong, lineWidth: 2)
                    }
                }
                .contentShape(.rect(cornerRadius: PBRadius.card))
        }
        .buttonStyle(TileStyle())
        .accessibilityLabel(name)
        .accessibilityAddTraits(isSelected ? .isSelected : [])
    }
}

/// Pressed: the tile shrinks a little (no pressed variant in Figma).
private struct TileStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .scaleEffect(configuration.isPressed ? 0.97 : 1)
            .animation(.easeOut(duration: 0.1), value: configuration.isPressed)
    }
}

#Preview("PBAvatarPartTile") {
    let look = AvatarLook.defaultBoy
    HStack(spacing: 13) {
        PBAvatarPartTile(look: look, name: "Curly", isSelected: true) {}
        PBAvatarPartTile(look: look.setting("quiff", for: "hair"), name: "Quiff", isSelected: false) {}
        PBAvatarPartTile(look: look.setting("jacket", for: "outfit"), crop: .bust, name: "Jacket", isSelected: false) {}
    }
    .frame(width: 362)
}
