import SwiftUI

/// Control / Segmented (Figma 12:249): a 36 pt `bg/card` pill with 3 pt padding and equal-width
/// Control / Segment items (12:236). Choosing a segment fades its black pill in and the previous
/// one's out (0.15 s). Figma widths: 2 options 240, 3 options 330, 4 options 362; set them with
/// `.frame(width:)`. With a `testIDPrefix`, each segment's test id is
/// `<prefix>.<option in lowercase>`.
struct PBSegmentedControl: View {
    let options: [String]
    @Binding var selection: Int
    var testIDPrefix: String?

    var body: some View {
        HStack(spacing: 0) {
            ForEach(options.indices, id: \.self) { index in
                PBSegment(title: options[index], isSelected: index == selection) {
                    selection = index
                }
                .accessibilityIdentifier(testIDPrefix.map { "\($0).\(options[index].lowercased())" } ?? "")
            }
        }
        .padding(3)
        .frame(height: 36)
        .background(PBColor.bgCard, in: .capsule)
    }
}

/// Control / Segment (Figma 12:236): one 30 pt item. Selected = white label on the black pill,
/// otherwise a `text/secondary` label with no fill. The fill and label colour fade (0.15 s).
private struct PBSegment: View {
    let title: String
    let isSelected: Bool
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Text(title)
                .textStyle(.buttonSmall)
                .lineLimit(1)
                .foregroundStyle(isSelected ? PBColor.textInverse : PBColor.textSecondary)
                .frame(maxWidth: .infinity)
                .frame(height: 30)
                .background(isSelected ? PBColor.bgInverse : .clear, in: .capsule)
                .contentShape(.capsule)
                .animation(.easeInOut(duration: 0.15), value: isSelected)
        }
        .buttonStyle(PBSegmentStyle())
        .accessibilityAddTraits(isSelected ? .isSelected : [])
    }
}

/// No pressed look: the selection change is the feedback.
private struct PBSegmentStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
    }
}

#Preview("PBSegmentedControl") {
    @Previewable @State var two = 0
    @Previewable @State var three = 1
    @Previewable @State var four = 3
    VStack(spacing: PBSpace.s16) {
        PBSegmentedControl(options: ["Groups", "Friends"], selection: $two).frame(width: 240)
        PBSegmentedControl(options: ["All", "Upcoming", "Overdue"], selection: $three).frame(width: 330)
        PBSegmentedControl(options: ["Equally", "Exact", "%", "Shares"], selection: $four).frame(width: 362)
    }
}
