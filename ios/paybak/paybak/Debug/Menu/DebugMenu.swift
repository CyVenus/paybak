#if DEBUG
import SwiftUI
import os

/// The debug menu (long-press the Home logo; app-architecture §3.10): the clock line, M2's data,
/// clock, Pro, friend's-side, scenario and start-screen sections, then each module's own actions.
/// A row closes the menu, then runs.
struct DebugMenu: View {
    @Environment(AppRouter.self) private var router
    @Environment(LedgerStore.self) private var ledgerStore
    @Environment(ProfileStore.self) private var profileStore

    private static let log = Logger(subsystem: "app.paybak.paybak", category: "Debug")

    var body: some View {
        PBSheet(title: "Debug", testIDPrefix: "debugMenu", onClose: router.dismissSheet) {
            ScrollView {
                VStack(alignment: .leading, spacing: PBSpace.s16) {
                    Text(clockLine)
                        .textStyle(.footnote)
                        .foregroundStyle(PBColor.textTertiary)
                    ForEach(sections) { section in
                        Text(section.title)
                            .textStyle(.headline)
                            .foregroundStyle(PBColor.textPrimary)
                        VStack(spacing: 0) {
                            ForEach(Array(section.actions.enumerated()), id: \.offset) { index, action in
                                PBSettingRow(action.title, subtitle: action.detail, showsDivider: index < section.actions.count - 1) {
                                    run(action)
                                }
                                .accessibilityIdentifier("debugMenu.\(action.title)")
                            }
                        }
                        .pbCard(padding: 0)
                    }
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(.bottom, PBSpace.s24)
            }
        }
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier("screen.debugMenu")
    }

    /// "Pinned: Wed 30 Sep, 9:15 pm" or "Real time: …".
    private var clockLine: String {
        let clock = ledgerStore.clock
        let now = clock.now
        let mode = clock.isPinned ? "Pinned" : "Real time"
        return "\(mode): \(Format.day(LocalDay(now, calendar: clock.calendar))), \(Format.time(now, calendar: clock.calendar))"
    }

    private var sections: [DebugSection] {
        let ledger = ledgerStore.ledger
        let modules = [
            DebugSection(title: "Home", actions: HomeDebugActions.actions(ledger)),
            DebugSection(title: "Add & Record", actions: AddRecordDebugActions.actions(ledger)),
            DebugSection(title: "Groups & Friends", actions: GroupsDebugActions.actions(ledger)),
            DebugSection(title: "Settle up", actions: SettleDebugActions.actions(ledger)),
            DebugSection(title: "Activity & Notifications", actions: ActivityDebugActions.actions(ledger)),
            DebugSection(title: "Projects", actions: ProjectsDebugActions.actions(ledger)),
            DebugSection(title: "Profile", actions: ProfileDebugActions.actions(ledger)),
            DebugSection(title: "Settings & Pro", actions: SettingsDebugActions.actions(ledger)),
            DebugSection(title: "Insights & AI", actions: InsightsDebugActions.actions(ledger)),
        ]
        return CoreDebugActions.sections(ledger) + modules.filter { !$0.actions.isEmpty }
    }

    /// Closes the menu, then runs the row. An action that refuses (a `LedgerError`) is logged.
    private func run(_ action: DebugAction) {
        router.dismissSheet()
        do {
            try action.perform(DebugContext(profileStore: profileStore, ledgerStore: ledgerStore, router: router))
        } catch {
            Self.log.error("\(action.title, privacy: .public): \(String(describing: error), privacy: .public)")
        }
    }
}
#endif
