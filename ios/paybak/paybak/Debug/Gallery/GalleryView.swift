#if DEBUG
import SwiftUI

/// Debug-only design-system gallery for checking the foundations against Figma:
/// `-startScreen gallery -galleryPage <n>` (0-based). Swipe or use the chevrons to change pages.
/// Each page fits one iPhone 18 Pro screen, so every page can be screenshotted as is.
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
