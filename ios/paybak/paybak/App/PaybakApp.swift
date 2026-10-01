import SwiftUI

@main
struct PaybakApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) private var appDelegate
    @State private var profileStore: ProfileStore
    @State private var ledgerStore: LedgerStore
    @State private var router: AppRouter
    @State private var subscriptionStore: SubscriptionStore
    @State private var paymentApprovals: PaymentApprovalPresenter
    @Environment(\.scenePhase) private var scenePhase

    init() {
        PBFont.registerAll()
        SubscriptionStore.configure()
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
        // After the launch hooks and the first tick, so the data a launch starts with never counts
        // as just approved.
        let paymentApprovals = PaymentApprovalPresenter(ledgerStore: ledgerStore, router: router)
        #if DEBUG
        DebugAutoApprover.install(on: ledgerStore)
        #endif
        _profileStore = State(initialValue: profileStore)
        _ledgerStore = State(initialValue: ledgerStore)
        _router = State(initialValue: router)
        _subscriptionStore = State(initialValue: SubscriptionStore())
        _paymentApprovals = State(initialValue: paymentApprovals)
    }

    var body: some Scene {
        WindowGroup {
            AppFlowView()
                .environment(profileStore)
                .environment(ledgerStore)
                .environment(router)
                .environment(subscriptionStore)
                .task { await subscriptionStore.observe() }
                .onChange(of: subscriptionStore.isPro, initial: true) { _, isPro in
                    ledgerStore.hasStoreEntitlement = isPro
                }
                .onAppear { appDelegate.connect(ledgerStore: ledgerStore, router: router) }
                // Every change re-plans the scheduled notifications (a newer change cancels this run).
                .task(id: ledgerStore.revision) { await NotificationService.reschedule(for: ledgerStore.books) }
                // Approvals wait for the main app (after splash or onboarding).
                .onChange(of: router.root) { paymentApprovals.presentIfReady() }
        }
        .onChange(of: scenePhase) { _, phase in
            switch phase {
            case .active:
                ledgerStore.tick()
                paymentApprovals.presentIfReady()
            case .background:
                ledgerStore.flush()
                paymentApprovals.finishNow()
            default: break
            }
        }
    }
}
