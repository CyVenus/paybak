import Combine
import SwiftUI
import UIKit

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
    /// Like `.fitted`, but the content scrolls when the keyboard leaves less room than it needs (a
    /// form taller than the space above the keyboard), so the focused field stays in view.
    case fittedScrolling
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
        .pbSheetScrim(isShown: isPresented.wrappedValue)
    }

    /// Deepens the dimming behind a presented sheet to the Figma scrim (`color/bg/scrim`, 40 %).
    /// iOS dims the presenting screen by 20 % black; this layer adds the rest (24 % of #0A0A0A).
    /// Attach it to the screen that presents the sheet.
    func pbSheetScrim(isShown: Bool) -> some View {
        overlay {
            PBPalette.gray900
                .opacity(isShown ? 0.24 : 0)
                .ignoresSafeArea()
                .allowsHitTesting(false)
                .accessibilityHidden(true)
                .animation(isShown ? .easeOut(duration: 0.3) : .easeIn(duration: 0.25), value: isShown)
        }
    }
}

/// The system sheet's look for a `.pbSheet`: the detent (fitted to the content, or large), the
/// grabber, white fill and 40 pt corners. Route sheets use it directly inside `.sheet(item:)`.
///
/// iOS lays a floating (Medium) sheet out at the full screen width, then scales it down into its
/// frame 8 pt in from each edge, which would draw every row, text and control 4 % small. Fitted
/// content is laid out at the floating frame's width instead and scaled back up by the same factor,
/// so it lands at Figma's size. It runs into the sheet's home-indicator inset, so the sheet ends
/// 28 pt below the content as in Figma. While the keyboard is up iOS attaches the sheet to the
/// screen edges at full size, so the content goes back to its own size.
struct PBSheetPresentation<Content: View>: View {
    let detent: PBSheetDetent
    let content: () -> Content

    /// The fitted height, measured from the content; a sensible start before the first layout.
    @State private var height: CGFloat = 320
    /// The sheet's layout size (the screen width; the height left above the keyboard).
    @State private var size = CGSize(width: 402, height: 874)
    @State private var isKeyboardShown = false

    /// The system's floating scale on iPhone: the inset frame's width over the layout width (1
    /// while the keyboard has attached the sheet to the edges, and on iPad's form sheets).
    private var scale: CGFloat {
        guard !isKeyboardShown, size.width > 0, UIDevice.current.userInterfaceIdiom == .phone else { return 1 }
        return (size.width - 2 * PBSpace.s8) / size.width
    }

    /// The detent that draws the sheet exactly as tall as the content, at `scale`. iOS adds the
    /// home-indicator inset below a floating sheet's detent; the content already covers it. The
    /// inset is the window's, which a keyboard or a stacked sheet never changes.
    private var fittedDetent: PresentationDetent {
        guard !isKeyboardShown else { return .height(height) }
        let homeIndicator = UIApplication.shared.connectedScenes
            .compactMap { ($0 as? UIWindowScene)?.keyWindow?.safeAreaInsets.bottom }
            .first ?? 0
        return .height(max(1, height / scale - homeIndicator))
    }

    var body: some View {
        Group {
            switch detent {
            case .fitted:
                content()
                    .padding(.bottom, PBSpace.s28)
                    .fixedSize(horizontal: false, vertical: true)
                    .frame(width: size.width * scale)
                    .ignoresSafeArea(.container, edges: .bottom)
                    .onGeometryChange(for: CGFloat.self) { $0.size.height } action: { height = $0 }
                    .scaleEffect(1 / scale, anchor: .top)
                    .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .top)
                    .measuringSheetLayout(size: $size)
                    .presentationDetents([fittedDetent])
            case .fittedScrolling:
                ScrollView {
                    content()
                        .padding(.bottom, PBSpace.s28)
                        .onGeometryChange(for: CGFloat.self) { $0.size.height } action: { height = $0 }
                }
                .scrollBounceBehavior(.basedOnSize)
                .scrollDismissesKeyboard(.interactively)
                .frame(width: size.width * scale, height: size.height * scale)
                .scaleEffect(1 / scale, anchor: .top)
                .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .top)
                .measuringSheetLayout(size: $size)
                .presentationDetents([fittedDetent])
            case .large:
                content()
                    .frame(maxHeight: .infinity, alignment: .top)
                    .presentationDetents([.custom(LargeSheetDetent.self)])
            }
        }
        .presentationDragIndicator(.visible)
        .presentationBackground(PBColor.bgPrimary)
        .presentationCornerRadius(PBRadius.sheet)
        .onReceive(NotificationCenter.default.publisher(for: UIResponder.keyboardWillShowNotification)) { _ in
            isKeyboardShown = true
        }
        .onReceive(NotificationCenter.default.publisher(for: UIResponder.keyboardWillHideNotification)) { _ in
            isKeyboardShown = false
        }
    }
}

/// Detent=Large: 8 pt below the top safe area (app-architecture §7.2), where the system's large
/// detent reaches it.
private struct LargeSheetDetent: CustomPresentationDetent {
    static func height(in context: Context) -> CGFloat? {
        context.maxDetentValue - PBSpace.s8
    }
}

private extension View {
    /// Fills the sheet down to its bottom edge (over the home-indicator inset, above the keyboard)
    /// and reports that size.
    func measuringSheetLayout(size: Binding<CGSize>) -> some View {
        ignoresSafeArea(.container, edges: .bottom)
            .onGeometryChange(for: CGSize.self) { $0.size } action: { size.wrappedValue = $0 }
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
