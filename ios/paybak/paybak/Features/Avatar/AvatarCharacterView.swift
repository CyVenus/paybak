import SwiftUI

// STUB (app-architecture §5.1): Lane C replaces this file with the composed character (the part
// layers in manifest order, the crops) and keeps this initializer.
/// A custom character drawn from its look, in one of the rig crops (screens-profile §1.5).
struct AvatarCharacterView: View {
    enum Crop {
        case head
        case bust
        case stage
        case full
    }

    let look: AvatarLook
    var crop: Crop = .head

    var body: some View {
        GeometryReader { proxy in
            Image(systemName: look.gender == .boy ? "person.crop.circle" : "person.crop.circle.fill")
                .resizable()
                .scaledToFit()
                .foregroundStyle(PBColor.iconSecondary)
                .padding(proxy.size.width * 0.12)
        }
    }
}
