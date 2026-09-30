import SwiftUI

/// The Groups tab (screens-groups §3): the large "Groups" title with ＋ (New group) or user-add (Add
/// friend), the Groups | Friends segments (`router.groupsSegment`), then the groups and projects list
/// or the friends list. It scrolls under the tab bar; the inline title appears once the large one has
/// scrolled away.
struct GroupsTabScreen: View {
    @Environment(AppRouter.self) private var router
    @State private var isTitleCollapsed = false

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 0) {
                VStack(spacing: PBSpace.s16) {
                    PBNavHeader(title: "Groups", action: headerAction)
                        .accessibilityElement(children: .contain)
                        .accessibilityIdentifier("groups.title")
                    PBSegmentedControl(options: ["Groups", "Friends"], selection: segment, testIDPrefix: "groups.segment")
                }
                switch router.groupsSegment {
                case .groups:
                    GroupsListView()
                        .padding(.top, PBSpace.s24)
                case .friends:
                    FriendsListView()
                        .padding(.top, PBSpace.s16)
                }
            }
            .padding(.horizontal, PBLayout.screenMargin)
            .padding(.bottom, PBTabBar.contentInset)
            .phoneContentWidth()
        }
        .onScrollGeometryChange(for: Bool.self) { geometry in
            geometry.contentOffset.y + geometry.contentInsets.top > PBSize.tap
        } action: { _, isCollapsed in
            isTitleCollapsed = isCollapsed
        }
        .pbCollapsingTitle("Groups", isCollapsed: isTitleCollapsed)
        .background(PBColor.bgPrimary)
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier("screen.groups")
    }

    private var headerAction: PBNavHeader.Action {
        switch router.groupsSegment {
        case .groups:
            .init(icon: .plus, accessibilityLabel: "New group", testID: "groups.action") { router.open(.newGroup(.group)) }
        case .friends:
            .init(icon: .userAdd, accessibilityLabel: "Add friend", testID: "groups.action") { router.open(.addFriend) }
        }
    }

    private var segment: Binding<Int> {
        Binding {
            router.groupsSegment == .groups ? 0 : 1
        } set: { index in
            router.groupsSegment = index == 0 ? .groups : .friends
        }
    }
}

#if DEBUG
#Preview("GroupsTabScreen") {
    GroupsPreview {
        GroupsTabScreen()
    }
}
#endif
