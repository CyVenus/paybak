import SwiftUI

/// The Friends segment (screens-groups §3.3): the owed / owe summary (the Home totals), then one net
/// per friend, most urgent first. Only the overdue pill is red.
struct FriendsListView: View {
    @Environment(AppRouter.self) private var router
    @Environment(LedgerStore.self) private var ledgerStore

    var body: some View {
        let friends = ledgerStore.snapshot.friends
        if friends.isEmpty {
            PBEmptyState(
                illustration: .getStarted,
                title: "No friends yet.",
                message: "Add a friend to split with them.",
                primary: .init(title: "Add friend", icon: .userAdd, testID: "friends.empty.addFriend") { router.open(.addFriend) }
            )
            .accessibilityElement(children: .contain)
            .accessibilityIdentifier("friends.empty")
            .padding(.top, PBSpace.s8)
        } else {
            VStack(spacing: PBSpace.s16) {
                summary
                VStack(spacing: 0) {
                    ForEach(friends) { friend in
                        row(friend, showsDivider: friend.id != friends.last?.id)
                    }
                }
            }
        }
    }

    private var summary: some View {
        let totals = ledgerStore.snapshot.home.totals
        let currency = ledgerStore.profileStore.profile.defaultCurrency
        return HStack(spacing: PBSpace.s8) {
            HStack(spacing: PBSpace.s6) {
                Text("You’re owed")
                    .textStyle(.subheadline)
                    .foregroundStyle(PBColor.textSecondary)
                Text(Money.format(totals.owed, currency, sign: .signed))
                    .textStyle(Self.summaryAmount)
                    .foregroundStyle(totals.owed == 0 ? PBColor.textTertiary : PBColor.textPrimary)
            }
            .accessibilityElement(children: .combine)
            .accessibilityIdentifier("friends.summary.owed")
            Spacer(minLength: 0)
            HStack(spacing: PBSpace.s6) {
                Text("You owe")
                    .textStyle(.subheadline)
                    .foregroundStyle(PBColor.textSecondary)
                Text(Money.format(-totals.owe, currency, sign: .signed))
                    .textStyle(.subheadline)
                    .foregroundStyle(totals.owe == 0 ? PBColor.textTertiary : PBColor.textSecondary)
            }
            .accessibilityElement(children: .combine)
            .accessibilityIdentifier("friends.summary.owe")
        }
        .lineLimit(1)
    }

    private func row(_ friend: FriendBalance, showsDivider: Bool) -> some View {
        let copy = ledgerStore.books.friendRowCopy(friend)
        return PBPersonRow(name: friend.person.firstName, avatar: friend.person.avatarContent, subtitle: copy.subtitle,
                           tag: friend.person.isGuest ? "Guest" : nil, trailing: trailing(copy.trailing), showsDivider: showsDivider) {
            router.open(.friend(friend.id))
        }
        .pbFlushRow()
        .accessibilityIdentifier("friends.row.\(friend.id)")
    }

    private func trailing(_ trailing: FriendRowCopy.Trailing) -> PBPersonRow.Trailing {
        switch trailing {
        case .owed(let amount, let label, let overdue): .amount(amount, direction: .owed, label: label, overdue: overdue)
        case .owe(let amount, let label): .amount(amount, direction: .owe, label: label)
        case .status(let status): .status(status)
        }
    }

    /// The summary's owed amount: Manrope Bold 14/20 (Subheadline metrics, bold; no Figma style).
    private static let summaryAmount = PBTextStyle(name: "Subheadline Bold", face: .bold, size: 14, lineHeight: 20,
                                                   letterSpacing: 0, dynamicTypeStyle: .subheadline)
}

#if DEBUG
#Preview("FriendsListView") {
    GroupsPreview {
        ScrollView {
            FriendsListView().padding(PBLayout.screenMargin)
        }
    }
}
#endif
