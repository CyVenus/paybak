import SwiftUI

/// The timeline filtered to a person, a group, a project or a category and month (activity §3.9,
/// projects §3.7, groups §6.4): the same day groups and rows under a Push Header titled
/// "Build a Drone · History" (proposal). Test ids: `activityLog.row.<event id>`, `activityLog.empty`.
struct ActivityLogScreen: View {
    let filter: ActivityFilter

    @Environment(AppRouter.self) private var router
    @Environment(LedgerStore.self) private var store

    var body: some View {
        let books = store.books
        let days = books.activityLog(filter)
        ScrollView {
            Group {
                if days.isEmpty {
                    Text("Nothing here yet.")
                        .textStyle(.body)
                        .foregroundStyle(PBColor.textSecondary)
                        .frame(maxWidth: .infinity)
                        .padding(.top, PBSpace.s24)
                        .accessibilityIdentifier("activityLog.empty")
                } else {
                    ActivityDayList(days: days, testIDPrefix: "activityLog")
                }
            }
            .padding(.bottom, PBSpace.s48)
            .pbPushContent()
        }
        .scrollIndicators(.hidden)
        .pbPinnedHeader {
            PBPushHeader(books.activityLogTitle(filter), testIDPrefix: "activityLog", onBack: router.back)
        }
        .routeTestRoot("activityLog")
    }
}

#if DEBUG
#Preview("ActivityLogScreen") {
    GroupsPreview {
        ActivityLogScreen(filter: .group("g-goa"))
    }
}
#endif
