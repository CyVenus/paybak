import SwiftUI
import UIKit

/// My QR code (screens-groups §7.6): your avatar, name and handle, a scannable QR of your invite
/// link with the Paybak mark, the link with Copy (toast "Link copied") and Share link.
struct MyQRCodeSheet: View {
    let onClose: () -> Void

    @Environment(ProfileStore.self) private var profileStore
    @State private var toast: PBToastMessage?
    @State private var share: ShareItem?

    var body: some View {
        let profile = profileStore.profile
        PBSheet(title: "My QR code", testIDPrefix: "myQr", onClose: onClose) {
            VStack(spacing: PBSpace.s16) {
                VStack(spacing: PBSpace.s8) {
                    PBUserAvatar(diameter: PBSize.avatarLg)
                    VStack(spacing: 0) {
                        Text(profile.name)
                            .textStyle(.headline)
                            .foregroundStyle(PBColor.textPrimary)
                        Text("@\(profile.inviteUsername)")
                            .textStyle(.subheadline)
                            .foregroundStyle(PBColor.textSecondary)
                            .accessibilityIdentifier("myQr.handle")
                    }
                    .lineLimit(1)
                }
                VStack(spacing: PBSpace.s12) {
                    PBQRCodeCard(link: profile.inviteLink.absoluteString)
                        .accessibilityIdentifier("myQr.code")
                    Text("Friends can scan this to add you.")
                        .textStyle(.footnote)
                        .foregroundStyle(PBColor.textSecondary)
                }
                linkField(profile)
                PBButton("Share link", icon: .share, fillsWidth: true) {
                    share = ShareItem(url: profile.inviteLink)
                }
                .accessibilityIdentifier("myQr.share")
            }
            .frame(maxWidth: .infinity)
        }
        .pbToast($toast, bottomPadding: PBSpace.s96)
        .systemShare(item: $share)
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier("myQr.sheet")
    }

    private func linkField(_ profile: UserProfile) -> some View {
        HStack(spacing: PBSpace.s8) {
            Text(profile.inviteLinkText)
                .textStyle(.body)
                .foregroundStyle(PBColor.textPrimary)
                .lineLimit(1)
                .truncationMode(.middle)
                .frame(maxWidth: .infinity, alignment: .leading)
                .accessibilityIdentifier("myQr.link")
            PBIconButton(.copy, accessibilityLabel: "Copy link", style: .plain) {
                UIPasteboard.general.string = profile.inviteLink.absoluteString
                toast = PBToastMessage("Link copied")
            }
            .accessibilityIdentifier("myQr.copy")
        }
        .padding(.leading, PBSpace.s16)
        .padding(.trailing, PBSpace.s4)
        .frame(height: 48)
        .background(PBColor.bgCard, in: .rect(cornerRadius: PBRadius.input))
    }
}

#if DEBUG
#Preview("MyQRCodeSheet") {
    @Previewable @State var isPresented = true
    GroupsPreview {
        Color.clear.pbSheet(isPresented: $isPresented) {
            MyQRCodeSheet { isPresented = false }
        }
    }
}
#endif
