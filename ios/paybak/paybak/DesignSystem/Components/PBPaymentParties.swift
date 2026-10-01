import SwiftUI

/// Control / Payment Parties (Figma 125:1085): who paid whom on Record payment. A 96 pt #F5F5F5 card
/// with a From tile, a gray arrow and a To tile; each tile is a white 56 pt avatar with a Footnote
/// label over a Headline name, and opens the person picker. Test ids: `<prefix>.from`, `<prefix>.to`.
struct PBPaymentParties: View {
    struct Party {
        let name: String
        let avatar: PBAvatar.Content
    }

    let from: Party
    let to: Party
    var testIDPrefix: String?
    var onFromTap: () -> Void = {}
    var onToTap: () -> Void = {}

    var body: some View {
        HStack(spacing: PBSpace.s8) {
            tile(label: "From", party: from, action: onFromTap)
                .accessibilityIdentifier(testID("from"))
            PBIconView(.arrowRight, size: PBSize.iconMd)
                .foregroundStyle(PBColor.iconTertiary)
            tile(label: "To", party: to, action: onToTap)
                .accessibilityIdentifier(testID("to"))
        }
        .padding(.vertical, PBSpace.s20)
        .padding(.horizontal, PBSpace.s16)
        .background(PBColor.bgCard, in: .rect(cornerRadius: PBRadius.card))
    }

    private func tile(label: String, party: Party, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            HStack(spacing: PBSpace.s12) {
                PBAvatar(party.avatar, diameter: PBSize.avatarLg, isOnCard: true)
                VStack(alignment: .leading, spacing: 0) {
                    Text(label)
                        .textStyle(.footnote)
                        .foregroundStyle(PBColor.textTertiary)
                    Text(party.name)
                        .textStyle(.headline)
                        .foregroundStyle(PBColor.textPrimary)
                        .lineLimit(1)
                }
                .frame(maxWidth: .infinity, alignment: .leading)
            }
            .contentShape(.rect)
        }
        // Pressed lays the 6 % overlay over the tile, in its 14 pt corners.
        .buttonStyle(PBRowButtonStyle(surface: .card))
        .clipShape(.rect(cornerRadius: PBRadius.tile))
        .accessibilityElement(children: .combine)
        .accessibilityHint("Choose a person")
    }

    private func testID(_ element: String) -> String {
        testIDPrefix.map { "\($0).\(element)" } ?? ""
    }
}

#Preview("PBPaymentParties") {
    PBPaymentParties(from: .init(name: "You", avatar: .art(.arjun)), to: .init(name: "Meera", avatar: .art(.meera)))
        .padding(PBLayout.screenMargin)
}
