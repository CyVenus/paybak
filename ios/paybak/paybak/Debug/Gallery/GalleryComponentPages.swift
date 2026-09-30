#if DEBUG
import SwiftUI

struct GalleryOtherButtonsPage: View {
    private let textStyles: [(String, PBTextButton.Style)] = [("Skip", .primary), ("Skip", .secondary), ("Delete", .destructive)]
    private let iconStyles: [(String, PBIconButton.Style)] = [("Plain", .plain), ("Filled", .filled), ("Glass", .glass), ("Inverse", .inverse)]

    var body: some View {
        GalleryPageScroll {
            GallerySection("Button / Text Primary · Secondary · Destructive: Default · Pressed · Disabled · chevron") {
                ForEach(textStyles, id: \.1) { name, style in
                    HStack(spacing: PBSpace.s20) {
                        PBTextButton(name, style: style) {}
                        PBTextButton(name, style: style) {}.pbPreviewInteraction(.pressed)
                        PBTextButton(name, style: style) {}.disabled(true)
                        PBTextButton("See all", style: style, showsChevron: true) {}
                    }
                }
            }
            GallerySection("Button / Icon: Default · Pressed · Badge") {
                ForEach(iconStyles, id: \.0) { name, style in
                    HStack(spacing: PBSpace.s16) {
                        PBIconButton(.bell, accessibilityLabel: "Notifications", style: style) {}
                        PBIconButton(.bell, accessibilityLabel: "Notifications", style: style) {}.pbPreviewInteraction(.pressed)
                        PBIconButton(.bell, accessibilityLabel: "Notifications", style: style, showsBadge: true) {}
                        PBIconButton(.chevronLeft, accessibilityLabel: "Back", style: style) {}
                        Text(name).textStyle(.footnote).foregroundStyle(PBColor.textSecondary)
                    }
                    .padding(.vertical, 2)
                    .background(style == .glass ? PBColor.bgCardPressed : .clear)
                }
            }
            GallerySection("Button / Add: Default · Pressed") {
                HStack(spacing: PBSpace.s16) {
                    PBAddButton {}
                    PBAddButton {}.pbPreviewInteraction(.pressed)
                }
            }
        }
    }
}

struct GalleryBadgesAvatarsPage: View {
    private let diameters = [PBSize.avatarXs, PBSize.avatarSm, PBSize.avatarMd, PBSize.avatarLg]

    var body: some View {
        GalleryPageScroll {
            GallerySection("Badge / Pill: Muted · On Card · Inverse · Overdue") {
                HStack(spacing: PBSpace.s8) {
                    PBBadge("Due Fri")
                    PBBadge("Due Fri", icon: .calendar)
                    PBBadge("Paid", style: .inverse)
                    PBBadge("Overdue", style: .overdue, icon: .calendar)
                }
                HStack(spacing: PBSpace.s8) {
                    PBBadge("Due Fri", style: .onCard)
                    PBBadge("Due Fri", style: .onCard, icon: .calendar)
                }
                .padding(PBSpace.s12)
                .background(PBColor.bgCard, in: .rect(cornerRadius: PBRadius.card))
            }
            GallerySection("Avatar / Circle 24 · 32 · 40 · 56: Art · Initials · Icon · Icon On Card") {
                HStack(alignment: .top, spacing: PBSpace.s16) {
                    avatarColumn { PBAvatar(.art(.arjun), diameter: $0) }
                    avatarColumn { PBAvatar(.initials("AK"), diameter: $0) }
                    avatarColumn { PBAvatar(.icon(.groups), diameter: $0) }
                    avatarColumn { PBAvatar(.icon(.groups), diameter: $0, isOnCard: true) }
                        .padding(PBSpace.s8)
                        .background(PBColor.bgCard, in: .rect(cornerRadius: PBRadius.card))
                }
            }
            GallerySection("Avatar / Stack: 2 · 3 · 4") {
                HStack(spacing: PBSpace.s24) {
                    PBAvatarStack(heads: [.arjun, .priya])
                    PBAvatarStack(heads: [.arjun, .priya, .rohan])
                    PBAvatarStack(heads: [.arjun, .priya, .rohan, .esha])
                }
            }
        }
    }

    private func avatarColumn(@ViewBuilder _ avatar: @escaping (CGFloat) -> some View) -> some View {
        VStack(spacing: PBSpace.s8) {
            ForEach(diameters, id: \.self) { avatar($0) }
        }
    }
}

struct GalleryControlsPage: View {
    @State private var activeDot = 1
    @State private var two = 0
    @State private var three = 1
    @State private var four = 2

    var body: some View {
        GalleryPageScroll {
            GallerySection("Control / Page Dots: Active 1 · 2 · 3, then live") {
                HStack(spacing: PBSpace.s24) {
                    PBPageDots(active: 1)
                    PBPageDots(active: 2)
                    PBPageDots(active: 3)
                }
                HStack(spacing: PBSpace.s24) {
                    PBPageDots(active: activeDot)
                    PBButton("Next dot", style: .secondary, size: .small) { activeDot = activeDot % 3 + 1 }
                }
            }
            GallerySection("Control / Segmented: 2 · 3 · 4 options (tap to slide)") {
                PBSegmentedControl(options: ["Groups", "Friends"], selection: $two).frame(width: 240)
                PBSegmentedControl(options: ["All", "Upcoming", "Overdue"], selection: $three).frame(width: 330)
                PBSegmentedControl(options: ["Equally", "Exact", "%", "Shares"], selection: $four)
            }
            GallerySection("Divider / Line: None · Leading") {
                PBDivider()
                HStack(spacing: PBSpace.s12) {
                    PBAvatar(.art(.priya))
                    Text("Row text").textStyle(.headline)
                }
                PBDivider(inset: .leading)
            }
        }
    }
}

struct GalleryInputsPage: View {
    @State private var empty = ""
    @State private var email = "you@example.com"
    @State private var live = ""

    private let helper = "We’ll send a 6-digit code."

    var body: some View {
        GalleryPageScroll {
            GalleryItem("Default (placeholder) · Focused") {
                VStack(spacing: PBSpace.s12) {
                    PBTextField("Email", text: $empty, prompt: "you@example.com", helper: helper)
                    PBTextField("Email", text: $email, prompt: "you@example.com", helper: helper)
                        .pbPreviewInteraction(.focused)
                }
            }
            GalleryItem("Filled · Error · Disabled") {
                VStack(spacing: PBSpace.s12) {
                    PBTextField("Email", text: $email, prompt: "you@example.com", helper: helper)
                    PBTextField("Email", text: $email, prompt: "you@example.com", error: helper)
                    PBTextField("Email", text: $empty, prompt: "you@example.com", helper: helper)
                        .disabled(true)
                }
            }
            GalleryItem("No label or helper, leading icon (live: tap to type)") {
                PBTextField(nil, text: $live, prompt: "Search currencies", icon: .search)
            }
        }
    }
}

struct GalleryCodePage: View {
    @State private var typing = "4829"
    @State private var wrong = "482917"
    @State private var live = ""

    var body: some View {
        GalleryPageScroll {
            GallerySection("Control / Code Digit: Empty · Focused · Filled · Error") {
                HStack(spacing: PBSpace.s12) {
                    PBCodeDigit(digit: nil, state: .empty)
                    PBCodeDigit(digit: nil, state: .focused)
                    PBCodeDigit(digit: "4", state: .filled)
                    PBCodeDigit(digit: "4", state: .error)
                }
            }
            GallerySection("Control / Code Input: Typing (362 wide, 14.8 gaps)") {
                PBCodeField(code: $typing).pbPreviewInteraction(.focused)
            }
            GallerySection("Control / Code Input: Error") {
                PBCodeField(code: $wrong, isError: true)
            }
            GallerySection("Live (tap to type; 000000 is right)") {
                PBCodeField(code: $live, isError: live.count == PBCodeField.length && live != "000000")
                PBCodeField(code: $live)
                    .frame(width: 320)
                Text("Narrow row: boxes shrink to keep 12 pt gaps")
                    .textStyle(.caption2)
                    .foregroundStyle(PBColor.textTertiary)
            }
        }
    }
}

struct GalleryNavigationPage: View {
    @State private var step = 2

    var body: some View {
        GalleryPageScroll {
            GallerySection("Navigation / Onboarding Top Bar") {
                PBOnboardingTopBar(onSkip: {})
                PBOnboardingTopBar(onBack: {})
                PBOnboardingTopBar(onBack: {})
                    .pbPreviewInteraction(.pressed)
            }
            GallerySection("Navigation / Setup Header: Step 1 · 4 (Skip)") {
                PBSetupHeader(step: 1, onBack: {})
                PBSetupHeader(step: 4, onBack: {}, onSkip: {})
            }
            GallerySection("Live: animated progress and step number") {
                PBSetupHeader(step: step, onBack: { step = max(1, step - 1) }, onSkip: step >= 3 ? {} : nil)
                PBButton("Next step", style: .secondary, size: .small) { step = step % PBSetupHeader.stepCount + 1 }
            }
        }
    }
}

struct GalleryPickersPage: View {
    @State private var avatar = 0
    @State private var currency = "USD"

    var body: some View {
        GalleryPageScroll {
            GallerySection("Control / Avatar Option: Art selected · Art · Upload · Photo") {
                HStack(spacing: 5) {
                    ForEach(Array(PBPeepHead.presets.enumerated()), id: \.offset) { index, head in
                        PBAvatarOption(kind: .art(head), isSelected: avatar == index) { avatar = index }
                    }
                    PBAvatarOption(kind: .upload, isSelected: false) {}
                }
                PBAvatarOption(kind: .photo(PBPeepHead.meera.image), isSelected: true) {}
            }
            GallerySection("Row / Currency: Selected · Not selected (Figma defaults)") {
                VStack(spacing: 0) {
                    PBCurrencyRow(symbol: "₹", title: "Indian Rupee", subtitle: "INR", isSelected: true) {}
                    PBCurrencyRow(symbol: "₹", title: "Indian Rupee", subtitle: "INR", isSelected: false) {}
                }
            }
            GallerySection("Currency model") {
                Text(currencySummary)
                    .textStyle(.footnote)
                    .foregroundStyle(PBColor.textSecondary)
            }
            GallerySection("Symbol tile: ≤ 2 characters in Headline, else the code in Caption/1 (tap)") {
                VStack(spacing: 0) {
                    ForEach(["USD", "AED", "SGD", "CHF"].map { Currency(code: $0) }) { item in
                        PBCurrencyRow(symbol: item.tileText, title: item.name, subtitle: item.code, isSelected: currency == item.code) {
                            currency = item.code
                        }
                    }
                }
            }
        }
    }

    private var currencySummary: String {
        let suggestion = Currency.suggested()
        let source = suggestion.isFromRegion ? "from this region" : "fallback"
        let popular = Currency.popular(excluding: suggestion.currency.code).map(\.code).joined(separator: ", ")
        return "\(Currency.all().count) ISO currencies · suggested \(suggestion.currency.code) (\(source)) · popular \(popular)"
    }
}
#endif
