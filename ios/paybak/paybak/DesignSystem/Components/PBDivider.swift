import SwiftUI

/// Divider / Line (Figma 12:302): a 1 pt `border/subtle` hairline. Leading inset 52 lines up with
/// row text after a 40 pt avatar. Use sparingly; prefer white space.
struct PBDivider: View {
    enum Inset {
        case none
        case leading
    }

    var inset: Inset = .none

    var body: some View {
        Rectangle()
            .fill(PBColor.borderSubtle)
            .frame(height: PBSize.hairline)
            .padding(.leading, inset == .leading ? PBSize.avatarMd + PBSpace.s12 : 0)
            .accessibilityHidden(true)
    }
}

#Preview("PBDivider") {
    VStack(spacing: PBSpace.s24) {
        PBDivider()
        PBDivider(inset: .leading)
    }
    .padding(PBLayout.screenMargin)
}
