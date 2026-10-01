import SwiftUI

/// A project (screens-projects §3–§8): budget, components, paid vs fair share, history and who owes
/// whom, with Add component pinned over a white fade while it's active. Closed, it shows the final
/// settle-up plan read-only; once that plan is paid it archives into a permanent record.
struct ProjectScreen: View {
    let groupId: GroupID

    @Environment(AppRouter.self) private var router
    @Environment(LedgerStore.self) private var ledgerStore

    /// The open Add / Edit component sheet.
    @State private var sheet: ComponentSheet.Mode?

    var body: some View {
        let page = ledgerStore.books.projectPage(groupId)
        ScrollView {
            if let page {
                ProjectContent(page: page) { sheet = .edit($0) }
                    .padding(.horizontal, PBLayout.screenMargin)
                    .padding(.top, PBSpace.s12)
                    .phoneContentWidth()
            }
        }
        .pbPinnedHeader {
            PBPushHeader(testIDPrefix: "project", onBack: router.back)
                .overlay(alignment: .trailing) {
                    if page?.isEditable == true {
                        PBIconButton(.settings, accessibilityLabel: "Project settings", style: .glass) {
                            router.open(.projectSettings(groupId))
                        }
                        .accessibilityIdentifier("project.settings")
                    }
                }
        }
        .safeAreaInset(edge: .bottom, spacing: 0) {
            if page?.isEditable == true {
                addButton
            }
        }
        .pbSheet(isPresented: Binding { sheet != nil } set: { if !$0 { sheet = nil } }, detent: .fittedScrolling) {
            if let sheet, let page {
                ComponentSheet(projectId: groupId, editing: editing(sheet), currency: page.project.currency) { self.sheet = nil }
            }
        }
        .task(id: ledgerStore.revision) {
            if ledgerStore.archiveIfSettled(groupId) {
                router.toast("Project archived")
            }
        }
        .onStartScreen([.projectAddComponent]) { _ in
            sheet = .add
        }
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier("screen.project")
        .overlay(alignment: .topLeading) {
            if let page {
                Color.clear
                    .frame(width: 1, height: 1)
                    .accessibilityElement()
                    .accessibilityLabel(page.state.rawValue)
                    .accessibilityIdentifier("project.state.\(page.state.rawValue)")
            }
        }
    }

    private func editing(_ mode: ComponentSheet.Mode) -> ProjectComponent? {
        guard case .edit(let id) = mode else { return nil }
        return ledgerStore.ledger.components.first { $0.id == id }
    }

    /// Pinned to the bottom safe-area line over the bottom 96 pt of white fade (§3.9).
    private var addButton: some View {
        PBButton("Add component", icon: .plus, fillsWidth: true) {
            sheet = .add
        }
        .accessibilityIdentifier("project.addComponent")
        .padding(.horizontal, PBLayout.screenMargin)
        .padding(.top, PBSpace.s24)
        .phoneContentWidth()
        .background(alignment: .bottom) {
            PBScrollEdgeFade(height: 96)
                .ignoresSafeArea(edges: .bottom)
        }
    }
}

#if DEBUG
#Preview("ProjectScreen · Build a Drone") {
    GroupsPreview {
        ProjectScreen(groupId: "pj-drone")
    }
}

#Preview("ProjectScreen · Over budget") {
    GroupsPreview(scenarios: Scenario.demo + ["devBuysGps"]) {
        ProjectScreen(groupId: "pj-drone")
    }
}

#Preview("ProjectScreen · Closed") {
    GroupsPreview(scenarios: Scenario.demo + ["closeDrone"]) {
        ProjectScreen(groupId: "pj-drone")
    }
}

#Preview("ProjectScreen · Archived") {
    GroupsPreview {
        ProjectScreen(groupId: "pj-hackathon")
    }
}
#endif
