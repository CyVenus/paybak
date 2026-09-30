import SwiftUI

@main
struct PaybakApp: App {
    @State private var profileStore: ProfileStore
    @State private var router: AppRouter

    init() {
        PBFont.registerAll()
        let profileStore = ProfileStore()
        let router = AppRouter()
        #if DEBUG
        DebugLaunchOptions().apply(to: profileStore, router: router)
        #endif
        _profileStore = State(initialValue: profileStore)
        _router = State(initialValue: router)
    }

    var body: some Scene {
        WindowGroup {
            AppFlowView()
                .environment(profileStore)
                .environment(router)
        }
    }
}
