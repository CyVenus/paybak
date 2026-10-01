#if DEBUG
import SwiftUI

/// Debug-only gallery of the design system: every token, text style and core component state, and
/// the six Rive illustrations at their Figma slot sizes. `-startScreen gallery -galleryPage <n>`
/// opens page n (1-based). Swipe or use the arrows to page.
struct GalleryView: View {
    @State private var page: GalleryPage

    init(initialPage: Int) {
        let index = min(max(initialPage - 1, 0), GalleryPage.allCases.count - 1)
        _page = State(initialValue: GalleryPage.allCases[index])
    }

    var body: some View {
        VStack(spacing: 0) {
            header
            PBDivider()
            TabView(selection: $page) {
                ForEach(GalleryPage.allCases) { page in
                    page.content
                        .tag(page)
                }
            }
            .tabViewStyle(.page(indexDisplayMode: .never))
        }
        .phoneContentWidth()
        .background(PBColor.bgPrimary)
    }

    private var header: some View {
        HStack(spacing: 0) {
            PBIconButton(.chevronLeft, accessibilityLabel: "Previous page") { move(by: -1) }
            VStack(alignment: .leading, spacing: 0) {
                Text("Gallery \(page.rawValue + 1)/\(GalleryPage.allCases.count)")
                    .textStyle(.footnote)
                    .foregroundStyle(PBColor.textTertiary)
                Text(page.title)
                    .textStyle(.headline)
                    .foregroundStyle(PBColor.textPrimary)
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(.leading, PBSpace.s4)
            PBIconButton(.chevronRight, accessibilityLabel: "Next page") { move(by: 1) }
        }
        .padding(.horizontal, PBSpace.s8)
    }

    /// Past either end the arrow does nothing.
    private func move(by offset: Int) {
        guard let next = GalleryPage(rawValue: page.rawValue + offset) else { return }
        withAnimation { page = next }
    }
}

/// The gallery pages, in order.
enum GalleryPage: Int, CaseIterable, Identifiable {
    case colours
    case type
    case layout
    case assets
    case buttons
    case controls
    case inputs
    case navigation
    case forms
    case lists
    case charts
    case assistant
    case shell
    case rive

    var id: Int { rawValue }

    var title: String {
        switch self {
        case .colours: "Colours"
        case .type: "Text styles"
        case .layout: "Spacing, radius, size, materials"
        case .assets: "Icons, brand, avatars"
        case .buttons: "Buttons"
        case .controls: "Badges, avatars, controls"
        case .inputs: "Inputs and setup"
        case .navigation: "Headers, alerts, sheets, settings"
        case .forms: "Forms and money"
        case .lists: "Lists and detail"
        case .charts: "Progress and charts"
        case .assistant: "Assistant and scan"
        case .shell: "Tab bar, Add sheet, Home cards"
        case .rive: "Rive"
        }
    }

    @ViewBuilder
    var content: some View {
        switch self {
        case .colours: GalleryColoursPage()
        case .type: GalleryTypePage()
        case .layout: GalleryLayoutPage()
        case .assets: GalleryAssetsPage()
        case .buttons: GalleryButtonsPage()
        case .controls: GalleryControlsPage()
        case .inputs: GalleryInputsPage()
        case .navigation: GalleryNavigationPage()
        case .forms: GalleryFormsPage()
        case .lists: GalleryListsPage()
        case .charts: GalleryChartsPage()
        case .assistant: GalleryAssistantPage()
        case .shell: GalleryShellPage()
        case .rive: GalleryRivePage()
        }
    }
}

// MARK: - Shared building blocks

/// A scrolling gallery page with the screen margins.
struct GalleryPageScroll<Content: View>: View {
    @ViewBuilder let content: Content

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: PBLayout.sectionGap) {
                content
            }
            .padding(.horizontal, PBLayout.screenMargin)
            .padding(.vertical, PBSpace.s16)
            .frame(maxWidth: .infinity, alignment: .leading)
        }
    }
}

/// A titled group of samples.
struct GallerySection<Content: View>: View {
    let title: String
    @ViewBuilder let content: Content

    init(_ title: String, @ViewBuilder content: () -> Content) {
        self.title = title
        self.content = content()
    }

    var body: some View {
        VStack(alignment: .leading, spacing: PBSpace.s12) {
            Text(title)
                .textStyle(.title3)
                .foregroundStyle(PBColor.textPrimary)
            content
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }
}

/// A small grey caption naming a sample.
struct GalleryLabel: View {
    let text: String

    init(_ text: String) {
        self.text = text
    }

    var body: some View {
        Text(text)
            .textStyle(.footnote)
            .foregroundStyle(PBColor.textTertiary)
    }
}

/// A #F5F5F5 card, for the On Card variants.
struct GalleryOnCard<Content: View>: View {
    @ViewBuilder let content: Content

    var body: some View {
        VStack(alignment: .leading, spacing: PBSpace.s12) {
            content
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(PBSpace.s16)
        .background(PBColor.bgCard, in: .rect(cornerRadius: PBRadius.card))
    }
}

/// Black diagonal stripes, a busy backdrop that shows translucent materials.
struct GalleryStripes: View {
    var body: some View {
        Canvas { context, size in
            var path = Path()
            var x = -size.height
            while x < size.width {
                path.move(to: CGPoint(x: x, y: size.height))
                path.addLine(to: CGPoint(x: x + size.height, y: 0))
                x += 12
            }
            context.stroke(path, with: .color(PBColor.bgInverse), lineWidth: 3)
        }
        .clipped()
        .accessibilityHidden(true)
    }
}

/// The white body of a sheet drawn in place: the grabber, then the `PBSheet` header and content,
/// and the 28 pt bottom inset of Detent=Medium.
struct GallerySheetContainer<Content: View>: View {
    @ViewBuilder let content: Content

    var body: some View {
        content
            .padding(.bottom, PBSpace.s28)
            .background(PBColor.bgPrimary, in: .rect(cornerRadius: PBRadius.sheet))
            .overlay(alignment: .top) {
                Capsule()
                    .fill(PBColor.bgIndicator)
                    .frame(width: 60, height: 4)
                    .padding(.top, PBSpace.s8)
            }
    }
}

/// Lays children out left to right, top-aligned, and wraps them onto new rows.
struct GalleryFlow: Layout {
    var spacing: CGFloat
    var rowSpacing: CGFloat

    func sizeThatFits(proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) -> CGSize {
        let rows = rows(width: proposal.width ?? .infinity, subviews: subviews)
        let height = rows.map(\.height).reduce(0, +) + rowSpacing * CGFloat(max(rows.count - 1, 0))
        let width = rows.map(\.width).max() ?? 0
        return CGSize(width: proposal.width ?? width, height: height)
    }

    func placeSubviews(in bounds: CGRect, proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) {
        var y = bounds.minY
        for row in rows(width: bounds.width, subviews: subviews) {
            var x = bounds.minX
            for index in row.indices {
                let size = subviews[index].sizeThatFits(.unspecified)
                subviews[index].place(at: CGPoint(x: x, y: y), proposal: ProposedViewSize(size))
                x += size.width + spacing
            }
            y += row.height + rowSpacing
        }
    }

    private struct Row {
        var indices: [Int] = []
        var width: CGFloat = 0
        var height: CGFloat = 0
    }

    private func rows(width: CGFloat, subviews: Subviews) -> [Row] {
        var rows: [Row] = []
        var current = Row()
        for index in subviews.indices {
            let size = subviews[index].sizeThatFits(.unspecified)
            let needed = current.indices.isEmpty ? size.width : current.width + spacing + size.width
            if needed > width, !current.indices.isEmpty {
                rows.append(current)
                current = Row()
            }
            current.width = current.indices.isEmpty ? size.width : current.width + spacing + size.width
            current.height = max(current.height, size.height)
            current.indices.append(index)
        }
        if !current.indices.isEmpty {
            rows.append(current)
        }
        return rows
    }
}

/// The states a control sample is drawn in.
enum GalleryControlState: String, CaseIterable {
    case `default` = "Default"
    case pressed = "Pressed"
    case disabled = "Disabled"
}

/// One labelled sample per `GalleryControlState`; wraps when the samples are wide.
struct GalleryStateRow<Sample: View>: View {
    @ViewBuilder let sample: (GalleryControlState) -> Sample

    var body: some View {
        GalleryFlow(spacing: PBSpace.s12, rowSpacing: PBSpace.s8) {
            ForEach(GalleryControlState.allCases, id: \.self) { state in
                VStack(spacing: 0) {
                    sample(state)
                        .galleryState(state)
                    GalleryLabel(state.rawValue)
                }
            }
        }
    }
}

extension View {
    /// Draws a control Pressed or Disabled without touching it.
    @ViewBuilder
    func galleryState(_ state: GalleryControlState) -> some View {
        switch state {
        case .default: self
        case .pressed: pbPreviewInteraction(.pressed)
        case .disabled: disabled(true)
        }
    }

    /// Sets the accessibility identifier only when there is one.
    @ViewBuilder
    func galleryTestID(_ id: String?) -> some View {
        if let id {
            accessibilityIdentifier(id)
        } else {
            self
        }
    }
}

/// "2800" → "2,800", "100000" → "1,00,000": Indian digit grouping, for the gallery's samples.
func galleryGroupIndian(_ digits: String) -> String {
    let parts = digits.split(separator: ".", maxSplits: 1, omittingEmptySubsequences: false)
    let whole = parts.first.map(String.init).flatMap { $0.isEmpty ? nil : $0 } ?? "0"
    let last3 = String(whole.suffix(3))
    let restDigits = Array(whole.dropLast(3))
    var groups: [String] = []
    var end = restDigits.count
    while end > 0 {
        let start = max(0, end - 2)
        groups.insert(String(restDigits[start..<end]), at: 0)
        end = start
    }
    let grouped = groups.isEmpty ? last3 : groups.joined(separator: ",") + "," + last3
    return parts.count > 1 ? "\(grouped).\(parts[1])" : grouped
}

#Preview("Gallery") {
    GalleryView(initialPage: 1)
}
#endif
