import SwiftUI

/// Sheet / Container (Figma 118:1017, components-app.md §8.3): the body of every picker and form
/// sheet. The system draws the sheet and its grabber (`.pbSheet(…)`); this lays out the header, a
/// Title/3 title at x 16 with the 50 pt glass ✕ on the right, an optional search field, and the
/// content 8 pt below. A sheet with neither title nor ✕ drops the header and starts its content
/// 20 pt below the top. Test ids: `<prefix>.close`, `<prefix>.search`.
struct PBSheet<Content: View>: View {
    var title: String?
    /// Shows the search field bound to this text.
    var search: Binding<String>?
    var searchPrompt = "Search"
    var testIDPrefix: String?
    /// Nil hides the ✕.
    var onClose: (() -> Void)?
    @ViewBuilder var content: Content

    var body: some View {
        VStack(alignment: .leading, spacing: PBSpace.s8) {
            if title != nil || onClose != nil {
                HStack(spacing: PBSpace.s8) {
                    Text(title ?? "")
                        .textStyle(.title3)
                        .foregroundStyle(PBColor.textPrimary)
                        .lineLimit(1)
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .accessibilityAddTraits(.isHeader)
                    if let onClose {
                        PBGlassCloseButton(diameter: 50, action: onClose)
                            .accessibilityIdentifier(testID("close"))
                    }
                }
                .frame(height: 50)
            }
            if let search {
                PBTextField(nil, text: search, prompt: searchPrompt, icon: .search, showsClearButton: true)
                    .accessibilityIdentifier(testID("search"))
            }
            content
        }
        // The system grabber sits in the top 12 pt (8 padding + 4 grabber), then the usual 8 pt gap.
        .padding(.top, PBSpace.s20)
        .padding(.horizontal, PBSpace.s16)
        .frame(maxWidth: .infinity, alignment: .topLeading)
    }

    private func testID(_ element: String) -> String {
        testIDPrefix.map { "\($0).\(element)" } ?? ""
    }
}

/// How tall a `.pbSheet` opens.
enum PBSheetDetent {
    /// Detent=Medium: as tall as its content, floating 8 pt from the screen edges.
    case fitted
    /// Detent=Large: the tall sheet whose content fills the height and scrolls.
    case large
}

extension View {
    /// Presents `content` (usually a `PBSheet`) as a system sheet with the grabber, white fill and
    /// 40 pt corners over the dimmed screen. `.fitted` hugs the content's height.
    func pbSheet<Content: View>(
        isPresented: Binding<Bool>,
        detent: PBSheetDetent = .fitted,
        @ViewBuilder content: @escaping () -> Content
    ) -> some View {
        sheet(isPresented: isPresented) {
            PBSheetPresentation(detent: detent, content: content)
        }
    }
}

/// The system sheet's look for a `.pbSheet`: the detent (fitted to the content, or large), the
/// grabber, white fill and 40 pt corners. Route sheets use it directly inside `.sheet(item:)`.
struct PBSheetPresentation<Content: View>: View {
    let detent: PBSheetDetent
    let content: () -> Content

    /// The fitted height, measured from the content; a sensible start before the first layout.
    @State private var height: CGFloat = 320
    /// The floating sheet's own bottom inset (the home-indicator area it keeps clear below the
    /// detent). Medium sheets end 28 pt below their content, counting this inset. It keeps the
    /// largest value seen: while another sheet stacks on top the inset passes through in-between
    /// values, and following them would feed the height back into itself without end.
    @State private var bottomInset: CGFloat = 0

    var body: some View {
        Group {
            switch detent {
            case .fitted:
                content()
                    .padding(.bottom, max(0, PBSpace.s28 - bottomInset))
                    .fixedSize(horizontal: false, vertical: true)
                    .onGeometryChange(for: CGFloat.self) { $0.size.height } action: { height = $0 }
                    .frame(maxHeight: .infinity, alignment: .top)
                    .onGeometryChange(for: CGFloat.self) { $0.safeAreaInsets.bottom } action: { bottomInset = max(bottomInset, $0) }
                    .presentationDetents([.height(height)])
            case .large:
                content()
                    .frame(maxHeight: .infinity, alignment: .top)
                    .presentationDetents([.large])
            }
        }
        .presentationDragIndicator(.visible)
        .presentationBackground(PBColor.bgPrimary)
        .presentationCornerRadius(PBRadius.sheet)
    }
}

#Preview("PBSheet") {
    @Previewable @State var isPresented = true
    @Previewable @State var query = ""
    @Previewable @State var category = "Food"
    PBButton("Choose category") { isPresented = true }
        .pbSheet(isPresented: $isPresented) {
            PBSheet(title: "Category", search: $query, searchPrompt: "Search categories", onClose: { isPresented = false }) {
                VStack(spacing: 0) {
                    ForEach([("Food", PBIcon.food), ("Travel", .car), ("Stays", .bed)], id: \.0) { name, icon in
                        PBSettingRow(name, icon: icon, trailing: category == name ? .check : .unchecked, showsDivider: name != "Stays") {
                            category = name
                        }
                    }
                }
                .pbCard(padding: 0)
            }
        }
}
