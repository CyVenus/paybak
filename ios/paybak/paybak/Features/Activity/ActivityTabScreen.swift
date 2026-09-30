import SwiftUI

/// The Activity tab (screens-activity §3, M2): the fixed header with the Timeline | Insights
/// segments, over the timeline (lane A's `ActivityTimelineView`) or Insights (lane C's
/// `InsightsView`). The segment lives in the router so deep links and "See all" can pick it.
struct ActivityTabScreen: View {
    @Environment(AppRouter.self) private var router

    var body: some View {
        VStack(spacing: 0) {
            ActivityHeader()
                .padding(.horizontal, PBLayout.screenMargin)
                .phoneContentWidth()
            switch router.activitySegment {
            case .timeline: ActivityTimelineView()
            case .insights: InsightsView()
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .top)
        .background(PBColor.bgPrimary)
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier("screen.activity")
    }
}

/// The Activity header (Figma 167:14363 + 167:14372): "Activity" in Title/1 with the glass Restore
/// button (only while Recently deleted has something), then the full-width segmented control 16 below.
/// Test ids: `activity.recentlyDeleted`, `activity.segment.<timeline|insights>`.
struct ActivityHeader: View {
    @Environment(AppRouter.self) private var router
    @Environment(LedgerStore.self) private var ledgerStore

    var body: some View {
        @Bindable var router = router
        VStack(spacing: PBSpace.s16) {
            PBNavHeader(title: "Activity", action: restore)
            PBSegmentedControl(options: ["Timeline", "Insights"], selection: segment)
                .accessibilityIdentifier("activity.segment")
        }
        .padding(.bottom, PBSpace.s8)
    }

    private var restore: PBNavHeader.Action? {
        guard !ledgerStore.snapshot.recentlyDeleted.isEmpty else { return nil }
        return .init(icon: .restore, accessibilityLabel: "Recently deleted", testID: "activity.recentlyDeleted") {
            router.open(.recentlyDeleted)
        }
    }

    private var segment: Binding<Int> {
        Binding {
            router.activitySegment == .timeline ? 0 : 1
        } set: { index in
            router.activitySegment = index == 0 ? .timeline : .insights
        }
    }
}
