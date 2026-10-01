import SwiftUI

/// Chat / Bubble (Figma 117:971): one message in Ask Paybak, at the full row width. The user's
/// messages are right-aligned black bubbles (20 pt corners, 12/16 padding, at most 280 pt wide);
/// the assistant's are plain Body text on the left (at most 320 pt with the 24 pt sparkles avatar).
/// Text is selectable. Assistant answers can carry other components under the text (`attachment`).
struct PBChatBubble<Attachment: View>: View {
    enum Role {
        case user
        case assistant
    }

    let role: Role
    let text: String
    var showsAvatar = true
    @ViewBuilder var attachment: Attachment

    var body: some View {
        switch role {
        case .user:
            // A short message hugs its text; one that wraps fills the 280 pt bubble, as Figma does.
            ViewThatFits(in: .horizontal) {
                userText.fixedSize()
                userText.frame(width: 248, alignment: .leading)
            }
            .padding(.vertical, PBSpace.s12)
            .padding(.horizontal, PBSpace.s16)
            .background(PBColor.bgInverse, in: .rect(cornerRadius: PBRadius.card))
            .frame(maxWidth: 280, alignment: .trailing)
            .frame(maxWidth: .infinity, alignment: .trailing)
        case .assistant:
            VStack(alignment: .leading, spacing: PBSpace.s12) {
                HStack(alignment: .top, spacing: PBSpace.s8) {
                    if showsAvatar {
                        PBAvatar(.icon(.sparkles), diameter: PBSize.avatarXs)
                    }
                    Text(text)
                        .textStyle(.body)
                        .foregroundStyle(PBColor.textPrimary)
                        .textSelection(.enabled)
                        .fixedSize(horizontal: false, vertical: true)
                }
                .frame(maxWidth: 320, alignment: .leading)
                attachment
            }
            .frame(maxWidth: .infinity, alignment: .leading)
        }
    }

    private var userText: some View {
        Text(text)
            .textStyle(.body)
            .foregroundStyle(PBColor.textInverse)
            .textSelection(.enabled)
            .fixedSize(horizontal: false, vertical: true)
    }
}

extension PBChatBubble where Attachment == EmptyView {
    init(role: Role, text: String, showsAvatar: Bool = true) {
        self.init(role: role, text: text, showsAvatar: showsAvatar) { EmptyView() }
    }
}

#Preview("PBChatBubble") {
    VStack(spacing: PBSpace.s24) {
        PBChatBubble(role: .user, text: "Who owes me money?")
        PBChatBubble(role: .assistant, text: "Four people owe you ₹2,900 in total. Rohan’s ₹800 is the oldest.")
        PBChatBubble(role: .user, text: "Add cab ₹600 with Esha and Dev, split equally")
    }
    .padding(PBLayout.screenMargin)
}
