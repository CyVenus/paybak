import SwiftUI

/// Art / Receipt (Figma 86:730). Thumb (56 × 72, 10 pt corners) shows the receipt photo, or the
/// line-bar receipt art when there is none (the demo expenses). Full (300 × 458) is the Leopold Cafe
/// receipt: the demo receipt in the Scan receipt camera scene and the one the simulated scanner
/// reads.
struct PBReceiptThumbnail: View {
    enum Size {
        case thumb
        case full
    }

    var photo: Image?
    var size: Size = .thumb

    var body: some View {
        switch size {
        case .thumb:
            Group {
                if let photo {
                    photo.resizable().scaledToFill()
                } else {
                    Image("art-receipt-thumb").resizable()
                }
            }
            .frame(width: 56, height: 72)
            .clipShape(.rect(cornerRadius: PBRadius.sm))
            .accessibilityLabel("Receipt")
            .accessibilityAddTraits(.isImage)
        case .full:
            Image("art-receipt-full")
                .resizable()
                .frame(width: 300, height: 458)
                .accessibilityLabel("Receipt from Leopold Cafe, ₹2,300")
        }
    }
}

#Preview("PBReceiptThumbnail") {
    ScrollView {
        VStack(spacing: PBSpace.s24) {
            HStack(spacing: PBSpace.s16) {
                PBReceiptThumbnail()
                PBReceiptThumbnail(photo: PBPeepHead.meera.image)
            }
            PBReceiptThumbnail(size: .full)
        }
        .padding(PBLayout.screenMargin)
    }
}
