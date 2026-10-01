#if DEBUG
import SwiftUI

/// Previews of lane B's screens (Groups, Friends, Settle up): the demo at the Figma date in throwaway
/// stores, with a router and a navigation stack so pushes work.
struct GroupsPreview<Content: View>: View {
    var scenarios = Scenario.demo
    @ViewBuilder let content: Content

    @State private var stores: (profile: ProfileStore, ledger: LedgerStore, router: AppRouter)?

    var body: some View {
        Group {
            if let stores {
                NavigationStack {
                    content
                        .navigationBarHiddenKeepingSwipeBack()
                }
                .environment(stores.profile)
                .environment(stores.ledger)
                .environment(stores.router)
            }
        }
        .onAppear {
            PBFont.registerAll()
            let profile = ProfileStore(defaults: UserDefaults(suiteName: "groups-preview")!)
            let file = LedgerFile(url: FileManager.default.temporaryDirectory.appending(path: "groups-preview.json"))
            let ledger = LedgerStore(file: file, profileStore: profile)
            try? ledger.loadDemo(scenarios: scenarios)
            stores = (profile, ledger, AppRouter())
        }
    }
}
#endif
