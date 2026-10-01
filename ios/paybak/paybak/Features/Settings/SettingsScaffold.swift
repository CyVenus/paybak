import SwiftUI

/// The pushed settings screens' frame (screens-settings §0): the push header pinned at the top, and a
/// scrolling column of sections 24 pt apart that starts 24 pt below the header. The test root is
/// `screen.<testIDPrefix>`; the back button is `<testIDPrefix>.back`.
struct SettingsScaffold<Content: View, Bottom: View>: View {
    let title: String
    let testIDPrefix: String
    let content: Content
    /// A pinned bottom bar (Export's CTA), drawn over a white strip.
    let bottom: Bottom

    @Environment(AppRouter.self) private var router

    init(title: String, testIDPrefix: String, @ViewBuilder content: () -> Content, @ViewBuilder bottom: () -> Bottom) {
        self.title = title
        self.testIDPrefix = testIDPrefix
        self.content = content()
        self.bottom = bottom()
    }

    var body: some View {
        VStack(spacing: 0) {
            PBPushHeader(title, testIDPrefix: testIDPrefix, onBack: router.back)
                .padding(.horizontal, PBLayout.screenMargin)
            ScrollView {
                VStack(alignment: .leading, spacing: PBLayout.sectionGap) {
                    content
                }
                .padding(.horizontal, PBLayout.screenMargin)
                .padding(.vertical, PBSpace.s24)
            }
            .scrollBounceBehavior(.basedOnSize)
            .safeAreaInset(edge: .bottom, spacing: 0) {
                bottom
            }
        }
        .phoneContentWidth()
        .background(PBColor.bgPrimary)
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier("screen.\(testIDPrefix)")
    }
}

extension SettingsScaffold where Bottom == EmptyView {
    init(title: String, testIDPrefix: String, @ViewBuilder content: () -> Content) {
        self.init(title: title, testIDPrefix: testIDPrefix, content: content) { EmptyView() }
    }
}

/// A titled settings section: the Title/3 header, the content (usually a settings card) 8 pt below,
/// and an optional Footnote footer 8 pt below that.
struct SettingsSection<Content: View>: View {
    var title: String?
    var footer: String?
    @ViewBuilder var content: Content

    var body: some View {
        VStack(alignment: .leading, spacing: PBSpace.s8) {
            if let title {
                PBSectionHeader(title)
            }
            content
            if let footer {
                SettingsFooter(footer)
            }
        }
    }
}

/// Footnote in `text/secondary` under a card. Copy that Figma ends with a lone word carries a `\n`
/// at Figma's break and sets `wrapsLikeFigma` (FigmaWrappedText); other `\n`s are paragraph breaks.
struct SettingsFooter: View {
    let text: String
    var wrapsLikeFigma = false

    init(_ text: String, wrapsLikeFigma: Bool = false) {
        self.text = text
        self.wrapsLikeFigma = wrapsLikeFigma
    }

    var body: some View {
        Group {
            if wrapsLikeFigma {
                FigmaWrappedText(text, style: .footnote)
            } else {
                Text(text)
                    .textStyle(.footnote)
            }
        }
        .foregroundStyle(PBColor.textSecondary)
        .frame(maxWidth: .infinity, alignment: .leading)
        .fixedSize(horizontal: false, vertical: true)
    }
}

/// A 16 pt icon in `icon/secondary` beside a Footnote (Payment details' lock line, Currency's rates).
struct SettingsInfoLine: View {
    let icon: PBIcon
    let text: String

    var body: some View {
        HStack(alignment: .top, spacing: PBSpace.s8) {
            PBIconView(icon, size: PBSize.iconSm)
                .foregroundStyle(PBColor.iconSecondary)
                .frame(height: 18)
            SettingsFooter(text)
        }
        .accessibilityElement(children: .combine)
    }
}

#Preview("SettingsScaffold") {
    SettingsScaffold(title: "Privacy", testIDPrefix: "preview") {
        SettingsSection(title: "Discovery", footer: "Contacts are only used to find friends already on Paybak.") {
            VStack(spacing: 0) {
                PBSettingRow("Find me by phone or email", icon: .search, trailing: .toggle(.constant(true)))
                PBSettingRow("Contacts sync", icon: .people, trailing: .toggle(.constant(false)), showsDivider: false)
            }
            .pbCard(padding: 0)
        }
        SettingsInfoLine(icon: .lock, text: "Paybak never moves money. Friends copy these details and pay you in their own app.")
    }
    .environment(AppRouter())
}
