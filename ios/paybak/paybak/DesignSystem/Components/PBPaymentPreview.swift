import SwiftUI

/// Card / Payment Preview (Figma 37:675), with the plain copy icon (Setup 3): how your UPI ID
/// appears to friends. "What friends see", your avatar on a white circle, your name and UPI ID, and a
/// copy button. Without a UPI ID the line shows the "yourname@bank" hint and the copy button hides.
/// Screens that title the card with a section header (Payment details) turn the caption off.
struct PBPaymentPreview: View {
    let avatar: PBAvatar.Content
    let name: String
    let upiID: String
    let onCopy: () -> Void
    /// The screen part of the copy button's test id (flow.md): `<prefix>.copy`.
    var testIDPrefix: String?
    var showsCaption = true

    static let upiPlaceholder = "yourname@bank"

    var body: some View {
        VStack(alignment: .leading, spacing: PBSpace.s12) {
            if showsCaption {
                Text("What friends see")
                    .textStyle(.footnote)
                    .foregroundStyle(PBColor.textTertiary)
            }
            HStack(spacing: PBSpace.s12) {
                PBAvatar(avatar, diameter: PBSize.avatarMd, isOnCard: true)
                VStack(alignment: .leading, spacing: PBSpace.s2) {
                    Text(name)
                        .textStyle(.headline)
                        .foregroundStyle(PBColor.textPrimary)
                    Text(upiID.isEmpty ? Self.upiPlaceholder : upiID)
                        .textStyle(.subheadline)
                        .foregroundStyle(upiID.isEmpty ? PBColor.textTertiary : PBColor.textSecondary)
                        .truncationMode(.middle)
                }
                .lineLimit(1)
                .frame(maxWidth: .infinity, alignment: .leading)
                .accessibilityElement(children: .combine)
                if !upiID.isEmpty {
                    PBIconButton(.copy, accessibilityLabel: "Copy UPI ID", action: onCopy)
                        .accessibilityIdentifier(testIDPrefix.map { "\($0).copy" } ?? "")
                        .transition(.opacity)
                }
            }
            .frame(height: PBSize.tap)
            .animation(.easeOut(duration: 0.15), value: upiID.isEmpty)
        }
        .padding(PBLayout.cardPadding)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(PBColor.bgCard, in: .rect(cornerRadius: PBRadius.card))
    }
}

#Preview("PBPaymentPreview") {
    VStack(spacing: PBSpace.s16) {
        PBPaymentPreview(avatar: .art(.arjun), name: "Arjun Mehta", upiID: "arjun@okaxis") {}
        PBPaymentPreview(avatar: .initials("PS"), name: "Priya Shah", upiID: "") {}
        PBPaymentPreview(avatar: .art(.arjun), name: "Arjun Mehta", upiID: "arjun@okaxis", onCopy: {}, showsCaption: false)
    }
    .padding(PBLayout.screenMargin)
}
