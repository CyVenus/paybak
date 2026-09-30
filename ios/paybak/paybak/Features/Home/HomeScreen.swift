import SwiftUI

// TODO(Home phase): replace this placeholder with the real Home (screens-home.md).
/// Home: First day after onboarding, Active with sample data, All settled; ＋ opens the Add sheet.
struct HomeScreen: View {
    @Environment(AppRouter.self) private var router
    @Environment(ProfileStore.self) private var profileStore

    var body: some View {
        ScreenPlaceholder(
            screen: screenID,
            spec: "screens-home.md",
            note: Greeting.text(firstName: profileStore.profile.firstName),
            actions: actions
        )
        .overlay {
            if router.isAddSheetPresented {
                addSheet
            }
        }
    }

    private var actions: [ScreenPlaceholder.Action] {
        var actions: [ScreenPlaceholder.Action] = [.init("Open the Add sheet") { router.isAddSheetPresented = true }]
        #if DEBUG
        // What the Home debug menu (long-press on the logo) will offer.
        actions += [
            .init("Show First day") { router.homeState = .firstDay },
            .init("Show Active") { router.homeState = .active },
            .init("Show All settled") { router.homeState = .allSettled },
            .init("Reset onboarding") {
                profileStore.reset()
                router.restartOnboarding()
            },
        ]
        #endif
        return actions
    }

    private var screenID: ScreenID {
        if router.isAddSheetPresented { return .homeAddSheet }
        return switch router.homeState {
        case .firstDay: .homeFirstDay
        case .active: .homeActive
        case .allSettled: .homeAllSettled
        }
    }

    private var addSheet: some View {
        ZStack(alignment: .bottom) {
            PBScrim { router.isAddSheetPresented = false }
            PBButton("Close the Add sheet", fillsWidth: true) { router.isAddSheetPresented = false }
                .padding(PBLayout.screenMargin)
                .background(PBColor.bgPrimary, in: .rect(cornerRadius: PBRadius.sheet))
                .padding(PBSpace.s8)
                .phoneContentWidth()
        }
    }
}
