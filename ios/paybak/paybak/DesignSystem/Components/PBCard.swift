import SwiftUI

extension View {
    /// The Paybak card surface: `bg/card` #F5F5F5 with 20 pt corners (`radius/card`) and the card
    /// padding. Pass `padding: 0` for groups of rows that bring their own insets (settings groups,
    /// split rows, transfer rows), which also clips the rows' pressed fill to the card's corners.
    func pbCard(padding: CGFloat = PBLayout.cardPadding) -> some View {
        self.padding(padding)
            .background(PBColor.bgCard)
            .clipShape(.rect(cornerRadius: PBRadius.card))
    }
}

/// Pressed feedback for tappable rows and cards (README §3 rule 11): the fill swaps while the finger
/// is down. Rows on white turn `bg/card-pressed` #EBEBEB; rows inside a #F5F5F5 card get the 6 %
/// `bg/selected` overlay.
struct PBRowButtonStyle: ButtonStyle {
    enum Surface {
        case white
        case card
    }

    var surface: Surface = .white

    @Environment(\.pbPreviewInteraction) private var previewInteraction

    func makeBody(configuration: Configuration) -> some View {
        let isPressed = configuration.isPressed || previewInteraction == .pressed
        configuration.label
            .contentShape(.rect)
            .background(isPressed ? pressedFill : .clear)
            .animation(.easeOut(duration: 0.1), value: isPressed)
    }

    private var pressedFill: Color {
        surface == .white ? PBColor.bgCardPressed : PBColor.bgSelected
    }
}

#Preview("pbCard") {
    VStack(spacing: PBSpace.s16) {
        Text("Card content").textStyle(.headline)
            .frame(maxWidth: .infinity, alignment: .leading)
            .pbCard()
        Button {} label: {
            Text("Pressed row in a card").textStyle(.headline)
                .frame(maxWidth: .infinity, minHeight: 56, alignment: .leading)
                .padding(.horizontal, PBSpace.s16)
        }
        .buttonStyle(PBRowButtonStyle(surface: .card))
        .pbPreviewInteraction(.pressed)
        .pbCard(padding: 0)
    }
    .padding(PBLayout.screenMargin)
}
