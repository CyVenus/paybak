#if DEBUG
import SwiftUI

// MARK: - Buttons

struct GalleryButtonsPage: View {
    private static let styles: [(String, PBButton.Style)] = [
        ("Primary", .primary), ("Secondary", .secondary), ("OnCard", .onCard), ("Destructive", .destructive),
    ]
    private static let textStyles: [PBTextButton.Style] = [.primary, .secondary, .destructive]
    private static let iconStyles: [(String, PBIconButton.Style)] = [
        ("Plain", .plain), ("Filled", .filled), ("Glass", .glass), ("Inverse", .inverse),
    ]

    var body: some View {
        GalleryPageScroll {
            ForEach(Self.styles, id: \.0) { name, style in
                GallerySection("Button / \(name)") {
                    if style == .onCard {
                        GalleryOnCard { states(style) }
                    } else {
                        states(style)
                    }
                }
            }
            GallerySection("Stretched with brand icons (Get Started)") {
                PBButton("Continue with Apple", icon: .apple, fillsWidth: true) {}
                PBButton("Continue with Google", style: .secondary, icon: .google, fillsWidth: true) {}
            }
            GallerySection("Button / Text") {
                ForEach(Self.textStyles, id: \.self) { style in
                    GalleryStateRow { _ in
                        PBTextButton(style == .secondary ? "Skip" : "See all", style: style, showsChevron: style != .secondary) {}
                    }
                }
            }
            GallerySection("Button / Icon: default · pressed · badge") {
                ForEach(Self.iconStyles, id: \.0) { name, style in
                    if style == .glass {
                        ZStack(alignment: .leading) {
                            GalleryStripes()
                            iconRow(name, style)
                                .padding(.horizontal, PBSpace.s12)
                        }
                        .frame(maxWidth: .infinity)
                        .frame(height: 76)
                    } else {
                        iconRow(name, style)
                    }
                }
            }
            GallerySection("Button / Add: default · pressed") {
                HStack(spacing: PBSpace.s16) {
                    PBAddButton {}
                    PBAddButton {}
                        .pbPreviewInteraction(.pressed)
                }
            }
        }
    }

    /// Large and Small × Default · Pressed · Disabled, then Small with a leading icon.
    @ViewBuilder
    private func states(_ style: PBButton.Style) -> some View {
        let label = style == .destructive ? "Delete" : "Continue"
        ForEach([PBButton.Size.large, .small], id: \.self) { size in
            GalleryStateRow { _ in
                PBButton(label, style: style, size: size) {}
            }
        }
        GalleryStateRow { _ in
            PBButton(label, style: style, size: .small, icon: .plus) {}
        }
    }

    private func iconRow(_ name: String, _ style: PBIconButton.Style) -> some View {
        HStack(spacing: PBSpace.s16) {
            PBIconButton(.bell, accessibilityLabel: "Notifications", style: style) {}
            PBIconButton(.bell, accessibilityLabel: "Notifications", style: style) {}
                .pbPreviewInteraction(.pressed)
            PBIconButton(.bell, accessibilityLabel: "Notifications", style: style, showsBadge: true) {}
            GalleryLabel(name)
        }
    }
}

// MARK: - Badges, avatars, controls

struct GalleryControlsPage: View {
    @State private var activeDot = 1
    @State private var segments = [0, 0, 0]

    private static let badgeStyles: [(String, PBBadge.Style)] = [
        ("Muted", .muted), ("OnCard", .onCard), ("MutedOnCard", .mutedOnCard), ("Inverse", .inverse), ("Overdue", .overdue),
    ]
    private static let diameters: [CGFloat] = [PBSize.avatarXs, PBSize.avatarSm, PBSize.avatarMd, PBSize.avatarLg]
    private static let segmentOptions: [(options: [String], width: CGFloat)] = [
        (["Groups", "Friends"], 240),
        (["All", "Upcoming", "Overdue"], 330),
        (["Equally", "Exact", "%", "Shares"], 362),
    ]

    var body: some View {
        GalleryPageScroll {
            GallerySection("Badge / Pill") {
                ForEach(Self.badgeStyles, id: \.0) { name, style in
                    if style == .onCard {
                        GalleryOnCard { badgeRow(name, style) }
                    } else {
                        badgeRow(name, style)
                    }
                }
            }
            GallerySection("Avatar / Circle: Art · Initials · Icon at 24 · 32 · 40 · 56") {
                ForEach(Self.diameters, id: \.self) { diameter in
                    HStack(spacing: PBSpace.s12) {
                        PBAvatar(.art(.arjun), diameter: diameter)
                        PBAvatar(.initials("AM"), diameter: diameter)
                        PBAvatar(.icon(.groups), diameter: diameter)
                        GalleryLabel("\(Int(diameter))")
                    }
                }
                GalleryOnCard {
                    HStack(spacing: PBSpace.s12) {
                        PBAvatar(.art(.rohan), isOnCard: true)
                        PBAvatar(.initials("AM"), isOnCard: true)
                        PBAvatar(.icon(.groups), isOnCard: true)
                        GalleryLabel("On card (Icon On Card)")
                    }
                }
            }
            GallerySection("Avatar / Stack: 2 · 3 · 4") {
                HStack(spacing: PBSpace.s24) {
                    ForEach(2...4, id: \.self) { count in
                        PBAvatarStack(heads: Array(PBPeepHead.presets.prefix(count)))
                    }
                }
            }
            GallerySection("Control / Page Dots") {
                ForEach(1...3, id: \.self) { active in
                    PBPageDots(active: active)
                }
                HStack(spacing: PBSpace.s16) {
                    PBPageDots(active: activeDot)
                    PBButton("Next dot", style: .secondary, size: .small) { activeDot = activeDot % 3 + 1 }
                }
            }
            GallerySection("Control / Segmented (tap to select)") {
                ForEach(Self.segmentOptions.indices, id: \.self) { index in
                    PBSegmentedControl(options: Self.segmentOptions[index].options, selection: $segments[index])
                        .frame(width: Self.segmentOptions[index].width)
                }
            }
            GallerySection("Divider / Line: None · Leading") {
                PBDivider()
                PBDivider(inset: .leading)
            }
        }
    }

    private func badgeRow(_ name: String, _ style: PBBadge.Style) -> some View {
        HStack(spacing: PBSpace.s12) {
            PBBadge("Due Fri", style: style)
            PBBadge("Due Fri", style: style, icon: .calendar)
            GalleryLabel(name)
        }
    }
}

// MARK: - Inputs and setup

struct GalleryInputsPage: View {
    @State private var live = ""
    @State private var code = ""
    @State private var step = 1
    @State private var avatar = 0
    @State private var currency: String
    @State private var toast: PBToastMessage?

    private let suggested: (currency: Currency, isFromRegion: Bool)
    private let popular: [Currency]
    private let available: Int

    private static let helper = "We’ll send a 6-digit code."

    init() {
        let suggested = Currency.suggested()
        self.suggested = suggested
        popular = Currency.popular(excluding: suggested.currency.code)
        available = Currency.all().count
        _currency = State(initialValue: suggested.currency.code)
    }

    var body: some View {
        GalleryPageScroll {
            GallerySection("Control / Input Field") {
                textFields
            }
            GallerySection("Control / Code Digit: Empty · Focused · Filled · Error") {
                HStack(spacing: PBSpace.s12) {
                    PBCodeDigit(digit: nil, state: .empty)
                    PBCodeDigit(digit: nil, state: .focused)
                    PBCodeDigit(digit: "4", state: .filled)
                    PBCodeDigit(digit: "4", state: .error)
                }
            }
            GallerySection("Control / Code Input: Typing · Error · live") {
                PBCodeField(code: .constant("4829"))
                    .pbPreviewInteraction(.focused)
                PBCodeField(code: .constant("482917"), isError: true)
                PBCodeField(code: $code, isError: code.count == PBCodeField.length && code != "000000")
                GalleryLabel("Live: 000000 is correct; any other six digits show the error.")
            }
            GallerySection("Navigation / Onboarding Top Bar") {
                PBOnboardingTopBar(onSkip: {})
                PBOnboardingTopBar(onBack: {})
                PBOnboardingTopBar(onBack: {}, onSkip: {})
            }
            GallerySection("Navigation / Setup Header") {
                ForEach(1...PBSetupHeader.stepCount, id: \.self) { index in
                    PBSetupHeader(step: index, onBack: {}, onSkip: index >= 3 ? {} : nil)
                }
                GalleryLabel("Live: the next segment fills, the number slides, Skip fades")
                PBSetupHeader(step: step, onBack: { step = max(1, step - 1) }, onSkip: step >= 3 ? {} : nil)
                PBButton("Next step", style: .secondary, size: .small) { step = step % PBSetupHeader.stepCount + 1 }
            }
            GallerySection("Control / Avatar Option (tap to select)") {
                HStack(spacing: 0) {
                    ForEach(Array(PBPeepHead.presets.enumerated()), id: \.offset) { index, head in
                        PBAvatarOption(kind: .art(head), isSelected: avatar == index) { avatar = index }
                        Spacer(minLength: 0)
                    }
                    PBAvatarOption(kind: .upload, isSelected: false) {}
                }
            }
            GallerySection("Row / Currency (tap to select)") {
                currencyRows
            }
            GallerySection("Card / Payment Preview · Overlay / Toast") {
                PBPaymentPreview(avatar: .art(.arjun), name: "Arjun Mehta", upiID: "arjun@okaxis") {
                    toast = PBToastMessage("UPI ID copied")
                }
                PBPaymentPreview(avatar: .initials("AM"), name: "Arjun Mehta", upiID: "") {}
                GalleryLabel("Copy shows the toast for 2 s")
                Color.clear
                    .frame(maxWidth: .infinity)
                    .frame(height: PBSize.tap)
                    .pbToast($toast, bottomPadding: 0)
                PBToast("UPI ID copied")
            }
        }
    }

    @ViewBuilder
    private var textFields: some View {
        GalleryLabel("Default")
        PBTextField("Email", text: .constant(""), prompt: "you@example.com", helper: Self.helper)
        GalleryLabel("Focused")
        PBTextField("Email", text: .constant("you@example.com"), prompt: "you@example.com", helper: Self.helper)
            .pbPreviewInteraction(.focused)
        GalleryLabel("Filled")
        PBTextField("Email", text: .constant("you@example.com"), prompt: "you@example.com", helper: Self.helper)
        GalleryLabel("Error")
        PBTextField("Email", text: .constant("you@example.com"), prompt: "you@example.com", error: Self.helper)
        GalleryLabel("Disabled")
        PBTextField("Email", text: .constant(""), prompt: "you@example.com", helper: Self.helper)
            .disabled(true)
        GalleryLabel("Leading icon, no label or helper")
        PBTextField(nil, text: .constant(""), prompt: "Search currencies", icon: .search)
        GalleryLabel("Live (type here)")
        PBTextField("Email or phone", text: $live, prompt: "you@example.com", helper: Self.helper)
            .keyboardType(.emailAddress)
            .textInputAutocapitalization(.never)
    }

    private var currencyRows: some View {
        VStack(alignment: .leading, spacing: 0) {
            PBSectionHeader("Suggested")
            let region = suggested.isFromRegion ? " · Based on your region" : ""
            PBCurrencyRow(symbol: suggested.currency.tileText, title: suggested.currency.name, subtitle: suggested.currency.code + region,
                          isSelected: currency == suggested.currency.code) {
                currency = suggested.currency.code
            }
            PBSectionHeader("Popular")
            ForEach(popular) { item in
                PBCurrencyRow(symbol: item.tileText, title: item.name, subtitle: item.code, isSelected: currency == item.code) {
                    currency = item.code
                }
            }
            GalleryLabel("\(available) ISO currencies in use today")
        }
    }
}
#endif
