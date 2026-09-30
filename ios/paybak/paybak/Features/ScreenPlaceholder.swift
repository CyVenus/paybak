import SwiftUI

/// Temporary scaffold for the screens later phases build: shows the screen id, the spec section, the
/// saved profile, and buttons that follow flow.md's navigation table. Delete this file once the last
/// placeholder screen is replaced.
struct ScreenPlaceholder: View {
    struct Action: Identifiable {
        let title: String
        let perform: () -> Void

        init(_ title: String, perform: @escaping () -> Void) {
            self.title = title
            self.perform = perform
        }

        var id: String { title }
    }

    let screen: ScreenID
    let spec: String
    /// Extra runtime detail, e.g. the Home greeting.
    var note: String?
    var onBack: (() -> Void)?
    var onSkip: (() -> Void)?
    var actions: [Action] = []

    @Environment(ProfileStore.self) private var profileStore

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            PBOnboardingTopBar(onBack: onBack, onSkip: onSkip)
            VStack(alignment: .leading, spacing: PBSpace.s12) {
                PBBadge("Placeholder", style: .inverse)
                Text(screen.rawValue)
                    .textStyle(.title1)
                    .foregroundStyle(PBColor.textPrimary)
                Text("Replaced in a later phase. Spec: \(spec)")
                    .textStyle(.body)
                    .foregroundStyle(PBColor.textSecondary)
                if let note {
                    Text(note)
                        .textStyle(.title3)
                        .foregroundStyle(PBColor.textPrimary)
                }
            }
            .padding(.top, PBSpace.s24)
            profileSummary
                .padding(.top, PBLayout.sectionGap)
            Spacer(minLength: PBLayout.sectionGap)
            VStack(spacing: PBSpace.s12) {
                ForEach(Array(actions.enumerated()), id: \.element.id) { index, action in
                    PBButton(action.title, style: index == 0 ? .primary : .secondary, fillsWidth: true, action: action.perform)
                }
            }
        }
        .padding(.horizontal, PBLayout.screenMargin)
        .padding(.bottom, PBSpace.s16)
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .topLeading)
        .background(PBColor.bgPrimary)
        .toolbar(.hidden, for: .navigationBar)
    }

    /// Shows what onboarding saved so far (or the debug seed).
    private var profileSummary: some View {
        let profile = profileStore.profile
        let details = [profile.currencyCode, profile.upiID.isEmpty ? nil : profile.upiID].compactMap(\.self)
        return HStack(spacing: PBSpace.s12) {
            PBAvatar(avatarContent(for: profile), isOnCard: true)
            VStack(alignment: .leading, spacing: PBSpace.s2) {
                Text(profile.name.isEmpty ? "No profile yet" : profile.name)
                    .textStyle(.headline)
                    .foregroundStyle(PBColor.textPrimary)
                if !details.isEmpty {
                    Text(details.joined(separator: " · "))
                        .textStyle(.subheadline)
                        .foregroundStyle(PBColor.textSecondary)
                }
            }
            .lineLimit(1)
        }
        .padding(PBLayout.cardPadding)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(PBColor.bgCard, in: .rect(cornerRadius: PBRadius.card))
    }

    private func avatarContent(for profile: UserProfile) -> PBAvatar.Content {
        switch profile.avatar {
        case .preset(let index) where PBPeepHead.presets.indices.contains(index):
            .art(PBPeepHead.presets[index])
        case .photo:
            profileStore.loadPhoto().map { .photo(Image(uiImage: $0)) } ?? .initials(profile.initials)
        default:
            profile.name.isEmpty ? .icon(.profile) : .initials(profile.initials)
        }
    }
}

#Preview("ScreenPlaceholder") {
    ScreenPlaceholder(
        screen: .setup2,
        spec: "screens-setup.md §2",
        onBack: {},
        actions: [.init("Continue") {}, .init("Back") {}]
    )
    .environment(ProfileStore())
}
