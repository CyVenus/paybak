import SwiftUI

/// Control / Segmented (Figma 12:249): a 36 pt `bg/card` pill with 3 pt padding and equal-width
/// Control / Segment items (12:236). The selected segment is a black pill that slides between items.
/// Figma widths: 2 options 240, 3 options 330, 4 options 362; set them with `.frame(width:)`.
/// `testIDs` names the segments for UI tests, one per option.
struct PBSegmentedControl: View {
    let options: [String]
    @Binding var selection: Int
    var testIDs: [String] = []

    @Namespace private var selectedPill
    @Environment(\.accessibilityReduceMotion) private var reduceMotion

    var body: some View {
        HStack(spacing: 0) {
            ForEach(options.indices, id: \.self) { index in
                PBSegment(title: options[index], isSelected: index == selection) {
                    selection = index
                }
                .accessibilityIdentifier(testIDs.indices.contains(index) ? testIDs[index] : "")
                .background {
                    if index == selection {
                        Capsule()
                            .fill(PBColor.bgInverse)
                            .matchedGeometryEffect(id: "selectedPill", in: selectedPill)
                    }
                }
            }
        }
        .padding(3)
        .frame(height: 36)
        .background(PBColor.bgCard, in: .capsule)
        // The slide is a suggestion; Figma swaps variants instantly.
        .animation(reduceMotion ? nil : .snappy(duration: 0.25), value: selection)
    }
}

/// Control / Segment (Figma 12:236): one 30 pt item. Selected = white label on the black pill,
/// otherwise a `text/secondary` label with no fill.
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
                .contentShape(.capsule)
        }
        .buttonStyle(.plain)
        .accessibilityAddTraits(isSelected ? .isSelected : [])
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
