import SwiftUI

/// Setup 2 — Currency (screens-setup.md §2): the home currency. "Suggested" is the device-region
/// currency (INR when the region has none), "Popular" the five designed ones; the saved or suggested
/// currency starts selected. Searching filters every ISO currency by name or code. Everything below
/// the header scrolls under a pinned Continue with a scroll-edge fade; the keyboard covers the
/// footer instead of lifting it, and scrolling dismisses the keyboard.
struct SetupCurrencyScreen: View {
    @Environment(AppRouter.self) private var router
    @Environment(ProfileStore.self) private var profileStore

    @State private var query = ""
    @State private var selection: String?
    /// A currency chosen through search that neither section lists. It stays at the top of
    /// Suggested, so the choice is still on screen after the search is cleared.
    @State private var pinnedCode: String?
    @FocusState private var isSearchFocused: Bool

    private static let all = Currency.all()
    private static let suggestion = Currency.suggested()
    private static let popular = Currency.popular(excluding: suggestion.currency.code)

    var body: some View {
        VStack(spacing: 0) {
            PBSetupHeader(step: 2, onBack: router.pop)
                .padding(.horizontal, PBLayout.screenMargin)
            ScrollView {
                VStack(alignment: .leading, spacing: 0) {
                    VStack(alignment: .leading, spacing: PBSpace.s12) {
                        Text("Pick your currency")
                            .textStyle(.title1)
                            .foregroundStyle(PBColor.textPrimary)
                            .accessibilityAddTraits(.isHeader)
                        Text("Totals show in this currency. You can still add expenses in others.")
                            .textStyle(.body)
                            .foregroundStyle(PBColor.textSecondary)
                    }
                    .padding(.top, PBSpace.s24)
                    PBTextField(
                        nil,
                        text: $query,
                        prompt: "Search currencies",
                        icon: .search,
                        showsClearButton: true,
                        focus: $isSearchFocused
                    )
                    .textInputAutocapitalization(.never)
                    .autocorrectionDisabled()
                    .submitLabel(.search)
                    .accessibilityIdentifier("setup2.search")
                    .padding(.top, PBSpace.s20)
                    list
                        .padding(.top, PBSpace.s20)
                }
                .padding(.horizontal, PBLayout.screenMargin)
                // Figma's space-between remainder: the last row ends 14 pt above Continue.
                .padding(.bottom, 14)
            }
            .scrollDismissesKeyboard(.immediately)
            .safeAreaInset(edge: .bottom, spacing: 0) { footer }
        }
        .phoneContentWidth()
        .background(PBColor.bgPrimary)
        .ignoresSafeArea(.keyboard, edges: .bottom)
        .screenIdentifier(.setup2)
        .onAppear {
            guard selection == nil else { return }
            let saved = profileStore.profile.currencyCode
            select(saved ?? Self.suggestion.currency.code)
        }
    }

    @ViewBuilder
    private var list: some View {
        let trimmedQuery = query.trimmingCharacters(in: .whitespacesAndNewlines)
        if trimmedQuery.isEmpty {
            VStack(alignment: .leading, spacing: 0) {
                PBSectionHeader("Suggested")
                ForEach(suggestedRows) { row($0.currency, subtitle: $0.subtitle) }
                PBSectionHeader("Popular")
                    .padding(.top, PBSpace.s16)
                ForEach(Self.popular) { row($0) }
            }
        } else {
            let matches = Currency.search(trimmedQuery, in: Self.all)
            if matches.isEmpty {
                Text("No currencies match “\(trimmedQuery)”")
                    .textStyle(.body)
                    .foregroundStyle(PBColor.textSecondary)
                    .accessibilityIdentifier("setup2.noMatches")
            } else {
                LazyVStack(spacing: 0) {
                    ForEach(matches) { row($0) }
                }
            }
        }
    }

    /// The region's currency ("INR · Based on your region"; just the code for the INR fallback),
    /// after the pinned search choice if there is one.
    private var suggestedRows: [SuggestedRow] {
        let suggestion = Self.suggestion
        let code = suggestion.currency.code
        let subtitle = suggestion.isFromRegion ? "\(code) · Based on your region" : code
        var rows = [SuggestedRow(currency: suggestion.currency, subtitle: subtitle)]
        if let pinnedCode {
            rows.insert(SuggestedRow(currency: Currency(code: pinnedCode), subtitle: pinnedCode), at: 0)
        }
        return rows
    }

    private func row(_ currency: Currency, subtitle: String? = nil) -> some View {
        PBCurrencyRow(
            symbol: currency.tileText,
            title: currency.name,
            subtitle: subtitle ?? currency.code,
            isSelected: selection == currency.code
        ) {
            select(currency.code)
            // Picking a result ends the search, so Continue is no longer under the keyboard.
            isSearchFocused = false
        }
        .accessibilityIdentifier("setup2.row.\(currency.code)")
    }

    private func select(_ code: String) {
        selection = code
        let isListed = code == Self.suggestion.currency.code || Self.popular.contains { $0.code == code }
        if !isListed {
            pinnedCode = code
        }
    }

    /// Continue on the bottom safe-area edge, under a 24 pt fade from transparent to white that
    /// continues as solid white to the bottom of the screen (Figma "Scroll edge fade").
    private var footer: some View {
        PBButton("Continue", fillsWidth: true, action: next)
            .accessibilityIdentifier("setup2.continue")
            .padding(.horizontal, PBLayout.screenMargin)
            .background(alignment: .top) {
                VStack(spacing: 0) {
                    LinearGradient(
                        colors: [PBColor.bgPrimary.opacity(0), PBColor.bgPrimary],
                        startPoint: .top,
                        endPoint: .bottom
                    )
                    .frame(height: PBSpace.s24)
                    PBColor.bgPrimary
                }
                .padding(.top, -PBSpace.s24)
                .ignoresSafeArea(edges: .bottom)
                .allowsHitTesting(false)
            }
    }

    private func next() {
        guard let selection else { return }
        profileStore.update { $0.currencyCode = selection }
        router.push(.setup3)
    }
}

private struct SuggestedRow: Identifiable {
    let currency: Currency
    let subtitle: String

    var id: String { currency.code }
}

#Preview("SetupCurrencyScreen") {
    SetupCurrencyScreen()
        .environment(AppRouter())
        .environment(ProfileStore())
}
