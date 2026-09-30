import SwiftUI

@main
struct PaybakApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) private var appDelegate
    @State private var profileStore: ProfileStore
    @State private var ledgerStore: LedgerStore
    @State private var router: AppRouter
    @Environment(\.scenePhase) private var scenePhase

    init() {
        PBFont.registerAll()
        let profileStore = ProfileStore()
        var pinned: Date?
        #if DEBUG
        pinned = DebugState.pinnedClock
        #endif
        let ledgerStore = LedgerStore(clock: AppClock(pinned: pinned), profileStore: profileStore)
        let router = AppRouter()
        router.isPro = { ledgerStore.isPro }
        #if DEBUG
        DebugLaunchOptions().apply(to: profileStore, ledgerStore: ledgerStore, router: router)
        #endif
        ledgerStore.tick()
        _profileStore = State(initialValue: profileStore)
        _ledgerStore = State(initialValue: ledgerStore)
        _router = State(initialValue: router)
    }

    var body: some Scene {
        WindowGroup {
            AppFlowView()
                .environment(profileStore)
                .environment(ledgerStore)
                .environment(router)
                .onAppear { appDelegate.connect(ledgerStore: ledgerStore, router: router) }
        }
        .onChange(of: scenePhase) { _, phase in
            switch phase {
            case .active: ledgerStore.tick()
            case .background: ledgerStore.flush()
            default: break
            }
        }
    }
}
