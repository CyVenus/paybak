import SwiftUI

/// Edit avatar (screens-profile §3–4): the live stage with Shuffle, Boy | Girl, the category chips and
/// a 3-column grid of tiles previewing the look with each option. Save commits the look as the user's
/// avatar and pops; Back with unsaved changes asks first (and the edge swipe is off while it would).
struct EditAvatarScreen: View {
    @Environment(AppRouter.self) private var router
    @Environment(ProfileStore.self) private var profileStore
    @Environment(\.accessibilityReduceMotion) private var reduceMotion

    @State private var draft: AvatarDraft?
    @State private var isDiscardAlertPresented = false

    var body: some View {
        VStack(spacing: PBSpace.s16) {
            PBPushHeader("Edit avatar", trailing: .text("Save", action: save), testIDPrefix: "editAvatar", onBack: back)
                .padding(.horizontal, PBLayout.screenMargin)
            if let draft {
                ScrollView {
                    editor(draft)
                        .padding(.horizontal, PBLayout.screenMargin)
                        .padding(.bottom, PBSpace.s24)
                }
                .scrollBounceBehavior(.basedOnSize)
            }
            Spacer(minLength: 0)
        }
        .phoneContentWidth()
        .background(PBColor.bgPrimary)
        .navigationBarBackButtonHidden(draft?.isDirty ?? false)
        .pbAlert(
            isPresented: $isDiscardAlertPresented,
            title: "Discard changes?",
            message: "Your avatar edits won’t be saved.",
            cancelLabel: "Keep editing",
            actionLabel: "Discard",
            testIDPrefix: "editAvatar.discard",
            onAction: router.back
        )
        .sensoryFeedback(.selection, trigger: draft?.look)
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier("screen.editAvatar")
        .onAppear(perform: openDraft)
        .onStartScreen(Self.startScreens) { screen in
            #if DEBUG
            openDraft()
            draft?.apply(startScreen: screen)
            isDiscardAlertPresented = screen == .editAvatarDiscard
            #endif
        }
    }

    private func editor(_ draft: AvatarDraft) -> some View {
        VStack(spacing: PBSpace.s16) {
            PBAvatarStage(look: draft.look, testIDPrefix: "editAvatar") {
                self.draft?.shuffle()
            }
            PBSegmentedControl(
                options: ["Boy", "Girl"],
                selection: Binding {
                    draft.look.gender == .boy ? 0 : 1
                } set: { index in
                    withAnimation(reduceMotion ? nil : .easeOut(duration: 0.2)) {
                        self.draft?.select(index == 0 ? .boy : .girl)
                    }
                },
                testIDs: ["editAvatar.gender.boy", "editAvatar.gender.girl"]
            )
            AvatarCategoryChips(categories: draft.categories, selection: draft.category) { category in
                withAnimation(reduceMotion ? nil : .easeOut(duration: 0.25)) {
                    self.draft?.category = category
                }
            }
            .id(draft.look.gender)
            if let category = draft.selectedCategory {
                optionsGrid(category, look: draft.look)
            }
        }
    }

    private func optionsGrid(_ category: AvatarCatalog.Category, look: AvatarLook) -> some View {
        LazyVGrid(columns: Array(repeating: GridItem(.flexible(), spacing: 13), count: 3), spacing: 13) {
            ForEach(category.options) { option in
                PBAvatarPartTile(
                    look: look.setting(option.id, for: category.id),
                    crop: category.tileCrop,
                    name: option.name,
                    isSelected: category.option(look.pick(category.id)).id == option.id
                ) {
                    draft?.pick(option.id)
                }
                .accessibilityIdentifier("editAvatar.option.\(option.id)")
            }
        }
        .id("\(look.gender)-\(category.id)")
        .transition(.opacity)
    }

    /// The draft starts from the saved avatar once, and survives pushes back to this screen.
    private func openDraft() {
        if draft == nil {
            draft = AvatarDraft(avatar: profileStore.profile.avatar)
        }
    }

    private func back() {
        if draft?.isDirty == true {
            isDiscardAlertPresented = true
        } else {
            router.back()
        }
    }

    /// Makes the look the user's avatar (both characters' picks are kept) and pops.
    private func save() {
        if let draft, draft.isDirty {
            profileStore.update { $0.avatar = .character(draft.look) }
        }
        router.back()
    }

    private static let startScreens: Set<ScreenID> = [
        .editAvatarBoyHair, .editAvatarBoyBeard, .editAvatarBoyEyewear, .editAvatarBoyOutfit,
        .editAvatarGirlHair, .editAvatarGirlAccessory, .editAvatarGirlOutfit, .editAvatarDiscard,
    ]
}

/// The horizontally scrolling chip row: single selection, the chosen chip scrolled fully into view.
private struct AvatarCategoryChips: View {
    let categories: [AvatarCatalog.Category]
    let selection: String
    let onSelect: (String) -> Void

    var body: some View {
        ScrollViewReader { proxy in
            ScrollView(.horizontal) {
                HStack(spacing: PBSpace.s8) {
                    ForEach(categories) { category in
                        PBCategoryChip(category.label, isSelected: category.id == selection) {
                            onSelect(category.id)
                        }
                        .id(category.id)
                        .accessibilityIdentifier("editAvatar.category.\(category.id)")
                    }
                }
            }
            .scrollIndicators(.hidden)
            .onChange(of: selection, initial: true) { _, id in
                withAnimation(.easeOut(duration: 0.25)) {
                    proxy.scrollTo(id)
                }
            }
        }
        .frame(height: PBSize.buttonSm)
    }
}

#if DEBUG
extension AvatarDraft {
    /// The designed states (screens-profile §3.7): Quiff → Stubble → Square → Jacket on the Boy, then
    /// Ponytail → Bow → Striped tee on the Girl, which keeps the Boy's picks.
    mutating func apply(startScreen screen: ScreenID) {
        let boySteps: [(ScreenID, String, String)] = [
            (.editAvatarBoyHair, "hair", "quiff"), (.editAvatarBoyBeard, "beard", "stubble"),
            (.editAvatarBoyEyewear, "eyewear", "square"), (.editAvatarBoyOutfit, "outfit", "jacket"),
        ]
        let girlSteps: [(ScreenID, String, String)] = [
            (.editAvatarGirlHair, "hair", "ponytail"), (.editAvatarGirlAccessory, "accessory", "bow"),
            (.editAvatarGirlOutfit, "outfit", "striped-tee"),
        ]
        let target = screen == .editAvatarDiscard ? .editAvatarBoyOutfit : screen
        look = original
        look.gender = .boy
        for (id, category, option) in boySteps {
            look.setPick(option, for: category)
            self.category = category
            if id == target { return }
        }
        look.gender = .girl
        for (id, category, option) in girlSteps {
            look.setPick(option, for: category)
            self.category = category
            if id == target { return }
        }
    }
}
#endif

#Preview("EditAvatarScreen") {
    let profileStore = ProfileStore(defaults: UserDefaults(suiteName: "preview-avatar")!)
    profileStore.replace(with: .sample)
    return NavigationStack {
        EditAvatarScreen()
    }
    .environment(AppRouter())
    .environment(profileStore)
}
