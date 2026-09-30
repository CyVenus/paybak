#if DEBUG
import SwiftUI

/// The debug menu (long-press the Home logo; app-architecture §3.10): the core data, clock, Pro and
/// friend's-side actions, each module's own section, "Apply scenario…", "Load screen…" and the
/// component gallery. Errors show inline.
struct DebugMenu: View {
    @Environment(AppRouter.self) private var router
    @Environment(LedgerStore.self) private var ledgerStore
    @Environment(ProfileStore.self) private var profileStore
    @State private var error: String?

    var body: some View {
        PBSheet(title: "Debug", testIDPrefix: "debugMenu", onClose: router.dismissSheet) {
            ScrollView {
                VStack(alignment: .leading, spacing: PBSpace.s20) {
                    if let error {
                        Text(error)
                            .textStyle(.footnote)
                            .foregroundStyle(PBColor.textDestructive)
                    }
                    ForEach(sections) { section in
                        VStack(alignment: .leading, spacing: PBSpace.s8) {
                            PBSectionHeader(section.title)
                            VStack(spacing: 0) {
                                ForEach(section.actions) { action in
                                    row(action)
                                }
                            }
                            .pbCard(padding: 0)
                        }
                    }
                    list("Apply scenario…", items: LedgerStore.scenarioNames) { name in
                        run(DebugAction(title: name) { try $0.ledgerStore.applyScenario(name) })
                    }
                    list("Load screen…", items: ScreenID.allCases.map(\.rawValue)) { id in
                        guard let screen = ScreenID(rawValue: id) else { return }
                        router.dismissSheet()
                        DebugStart.show(screen, profileStore: profileStore, ledgerStore: ledgerStore, router: router)
                    }
                }
                .padding(.bottom, PBSpace.s32)
            }
        }
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier("screen.debugMenu")
    }

    private var sections: [DebugSection] {
        let ledger = ledgerStore.ledger
        return CoreDebugActions.sections(ledger) + [
            DebugSection(title: "Home", actions: HomeDebugActions.actions(ledger)),
            DebugSection(title: "Add & Record", actions: AddRecordDebugActions.actions(ledger)),
            DebugSection(title: "Notifications", actions: ActivityDebugActions.actions(ledger)),
            DebugSection(title: "Groups & Friends", actions: GroupsDebugActions.actions(ledger)),
            DebugSection(title: "Settle up", actions: SettleDebugActions.actions(ledger)),
            DebugSection(title: "Projects", actions: ProjectsDebugActions.actions(ledger)),
            DebugSection(title: "Profile", actions: ProfileDebugActions.actions(ledger)),
            DebugSection(title: "Settings & Pro", actions: SettingsDebugActions.actions(ledger)),
            DebugSection(title: "Insights & AI", actions: InsightsDebugActions.actions(ledger)),
            DebugSection(title: "Tools", actions: [
                DebugAction(title: "Component gallery") { $0.router.showGallery(page: 0) },
            ]),
        ].filter { !$0.actions.isEmpty }
    }

    private func row(_ action: DebugAction) -> some View {
        Button {
            run(action)
        } label: {
            VStack(alignment: .leading, spacing: PBSpace.s2) {
                Text(action.title)
                    .textStyle(.headline)
                    .foregroundStyle(PBColor.textPrimary)
                if let detail = action.detail {
                    Text(detail)
                        .textStyle(.footnote)
                        .foregroundStyle(PBColor.textSecondary)
                }
            }
            .frame(maxWidth: .infinity, minHeight: PBSize.tap, alignment: .leading)
            .padding(.horizontal, PBLayout.cardPadding)
            .padding(.vertical, PBSpace.s8)
        }
        .buttonStyle(PBRowButtonStyle(surface: .card))
        .accessibilityIdentifier("debugMenu.\(action.title)")
    }

    private func list(_ title: String, items: [String], action: @escaping (String) -> Void) -> some View {
        DisclosureGroup {
            VStack(alignment: .leading, spacing: 0) {
                ForEach(items, id: \.self) { item in
                    Button(item) { action(item) }
                        .textStyle(.subheadline)
                        .foregroundStyle(PBColor.textPrimary)
                        .frame(maxWidth: .infinity, minHeight: 36, alignment: .leading)
                        .accessibilityIdentifier("debugMenu.item.\(item)")
                }
            }
        } label: {
            Text(title)
                .textStyle(.title3)
                .foregroundStyle(PBColor.textPrimary)
        }
        .tint(PBColor.iconPrimary)
    }

    private func run(_ action: DebugAction) {
        do {
            try action.perform(DebugContext(profileStore: profileStore, ledgerStore: ledgerStore, router: router))
            error = nil
            router.dismissSheet()
        } catch {
            self.error = "\(action.title): \(error.localizedDescription)"
        }
    }
}
#endif
