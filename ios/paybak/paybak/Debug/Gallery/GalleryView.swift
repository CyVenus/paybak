#if DEBUG
import SwiftUI

/// Debug-only design-system gallery for checking the foundations against Figma:
/// `-startScreen gallery -galleryPage <n>` (0-based). Swipe or use the chevrons to change pages.
/// Most pages fit one iPhone 18 Pro screen; the longer component pages scroll.
struct GalleryView: View {
    @State private var page: GalleryPage

    init(initialPage: Int) {
        _page = State(initialValue: GalleryPage(rawValue: initialPage) ?? .coloursPrimitives)
    }

    var body: some View {
        VStack(spacing: 0) {
            header
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
        HStack(spacing: PBSpace.s8) {
            PBIconButton(.chevronLeft, accessibilityLabel: "Previous page") { move(by: -1) }
                .disabled(page.rawValue == 0)
            VStack(spacing: 0) {
                Text("Gallery \(page.rawValue) / \(GalleryPage.allCases.count - 1)")
                    .textStyle(.footnote)
                    .foregroundStyle(PBColor.textTertiary)
                Text(page.title)
                    .textStyle(.headline)
                    .foregroundStyle(PBColor.textPrimary)
                    .lineLimit(1)
                    .minimumScaleFactor(0.8)
            }
            .frame(maxWidth: .infinity)
            PBIconButton(.chevronRight, accessibilityLabel: "Next page") { move(by: 1) }
                .disabled(page.rawValue == GalleryPage.allCases.count - 1)
        }
        .padding(.horizontal, PBSpace.s12)
    }

    private func move(by offset: Int) {
        guard let next = GalleryPage(rawValue: page.rawValue + offset) else { return }
        withAnimation { page = next }
    }
}

enum GalleryPage: Int, CaseIterable, Identifiable {
    case coloursPrimitives
    case coloursSemantic
    case textStylesLarge
    case textStylesSmall
    case metrics
    case materials
    case icons
    case brand
    case pillButtons
    case pillButtonsOnCard
    case otherButtons
    case badgesAndAvatars
    case controls
    case inputs
    case code
    case navigation
    case pickers
    case riveWelcome
    case riveSetup
    case riveHome
    case feedback
    // components-app.md: the shared components of the full app.
    case chipsSettings
    case headersAlert
    case textInputs
    case amountEntry
    case splitRows
    case planPairs
    case personRows
    case transferHeaders
    case notices
    case confirmQR
    case groupRows
    case progress
    case chartCards
    case assistant
    case scan
    case sheetToast

    var id: Int { rawValue }

    var title: String {
        switch self {
        case .coloursPrimitives: "Colour primitives · bg"
        case .coloursSemantic: "Colour: text, icon, border…"
        case .textStylesLarge: "Text styles: titles, amounts"
        case .textStylesSmall: "Text styles: controls, brand"
        case .metrics: "Spacing, radius, size"
        case .materials: "Materials"
        case .icons: "Icons (65)"
        case .brand: "Brand and avatar art"
        case .pillButtons: "Button: Primary, Secondary"
        case .pillButtonsOnCard: "Button: On Card, Destructive"
        case .otherButtons: "Button: text, icon, add"
        case .badgesAndAvatars: "Badge and avatars"
        case .controls: "Page dots, segmented, divider"
        case .inputs: "Input field"
        case .code: "Code digit and code input"
        case .navigation: "Top bar, setup header"
        case .pickers: "Avatar option, currency row"
        case .riveWelcome: "Rive: Welcome, Get Started"
        case .riveSetup: "Rive: Setup 4, All set"
        case .riveHome: "Rive: Home empty states"
        case .feedback: "Section header, preview, toast"
        case .chipsSettings: "Category chip, setting row"
        case .headersAlert: "Push, modal header, alert"
        case .textInputs: "Text area, composer"
        case .amountEntry: "Amount, parties, split total"
        case .splitRows: "Split person rows"
        case .planPairs: "Plan, pair, comment, history"
        case .personRows: "Person rows"
        case .transferHeaders: "Transfer, title, amount hero"
        case .notices: "Notice cards"
        case .confirmQR: "Confirm payment, QR code"
        case .groupRows: "Group rows"
        case .progress: "Progress bars, bar rows"
        case .chartCards: "Chart, budget, loan"
        case .assistant: "Chat bubble, draft expense"
        case .scan: "Receipt, assign, shutter"
        case .sheetToast: "Toast, sheet container"
        }
    }

    @ViewBuilder
    var content: some View {
        switch self {
        case .coloursPrimitives: GalleryColourPrimitivesPage()
        case .coloursSemantic: GalleryColourSemanticPage()
        case .textStylesLarge: GalleryTextStylesPage(styles: Array(PBTextStyle.all.prefix(8)))
        case .textStylesSmall: GalleryTextStylesPage(styles: Array(PBTextStyle.all.dropFirst(8)))
        case .metrics: GalleryMetricsPage()
        case .materials: GalleryMaterialsPage()
        case .icons: GalleryIconsPage()
        case .brand: GalleryBrandPage()
        case .pillButtons: GalleryPillButtonsPage(styles: [.primary, .secondary])
        case .pillButtonsOnCard: GalleryPillButtonsPage(styles: [.onCard, .destructive], showsCTAs: true)
        case .otherButtons: GalleryOtherButtonsPage()
        case .badgesAndAvatars: GalleryBadgesAvatarsPage()
        case .controls: GalleryControlsPage()
        case .inputs: GalleryInputsPage()
        case .code: GalleryCodePage()
        case .navigation: GalleryNavigationPage()
        case .pickers: GalleryPickersPage()
        case .riveWelcome: GalleryRivePage(assets: [.onboarding, .getStarted])
        case .riveSetup: GalleryRivePage(assets: [.notifications, .allSet])
        case .riveHome: GalleryRivePage(assets: [.homeFirstDay, .homeAllSquare])
        case .feedback: GalleryFeedbackPage()
        case .chipsSettings: GalleryChipsSettingsPage()
        case .headersAlert: GalleryHeadersAlertPage()
        case .textInputs: GalleryTextInputsPage()
        case .amountEntry: GalleryAmountEntryPage()
        case .splitRows: GallerySplitRowsPage()
        case .planPairs: GalleryPlanPairsPage()
        case .personRows: GalleryPersonRowsPage()
        case .transferHeaders: GalleryTransferHeadersPage()
        case .notices: GalleryNoticesPage()
        case .confirmQR: GalleryConfirmQRPage()
        case .groupRows: GalleryGroupRowsPage()
        case .progress: GalleryProgressPage()
        case .chartCards: GalleryChartCardsPage()
        case .assistant: GalleryAssistantPage()
        case .scan: GalleryScanPage()
        case .sheetToast: GallerySheetToastPage()
        }
    }
}

// MARK: - Shared building blocks

/// A scrolling page with the screen margins.
struct GalleryPageScroll<Content: View>: View {
    @ViewBuilder let content: Content

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: PBLayout.sectionGap) {
                content
            }
            .padding(.horizontal, PBLayout.screenMargin)
            .padding(.top, PBSpace.s8)
            .padding(.bottom, PBSpace.s32)
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
        VStack(alignment: .leading, spacing: PBSpace.s8) {
            Text(title)
                .textStyle(.caption1)
                .foregroundStyle(PBColor.textTertiary)
                .textCase(.uppercase)
            content
        }
    }
}

/// A sample with a small caption under it (the Figma variant name).
struct GalleryItem<Content: View>: View {
    let caption: String
    @ViewBuilder let content: Content

    init(_ caption: String, @ViewBuilder content: () -> Content) {
        self.caption = caption
        self.content = content()
    }

    var body: some View {
        VStack(alignment: .leading, spacing: PBSpace.s4) {
            content
            Text(caption)
                .textStyle(.caption2)
                .foregroundStyle(PBColor.textTertiary)
        }
    }
}

#Preview("Gallery") {
    GalleryView(initialPage: 0)
}
#endif
