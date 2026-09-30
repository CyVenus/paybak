import SwiftUI

/// Row / Activity (Figma 13:477, components-home §10, activity §9): a timeline or recent-activity row,
/// 64 tall at least. A category icon or a person's avatar in a 40 pt circle; a 1-line title, a subtitle
/// of up to 2 lines (3 for notification bodies) and an optional detail line; then the amount and date,
/// a status badge, or a small action. Money in is black, money out gray with "−". Plain rows sit on
/// white (#F5F5F5 circle); On Card rows sit inside a #F5F5F5 card (white circle).
struct PBActivityRow: View {
    enum Leading {
        case icon(PBIcon)
        case avatar(PBAvatar.Content)
    }

    enum Surface {
        case plain
        case onCard
    }

    enum Trailing {
        /// The amount (already signed) and a date caption.
        case amount(String, date: String?, isIncoming: Bool)
        /// A status pill ("Pending", "Draft").
        case badge(String)
        /// A small button ("Restore").
        case action(String, perform: () -> Void)
        case none
    }

    let leading: Leading
    let title: String
    let subtitle: String
    var detail: String?
    var trailing: Trailing = .none
    var surface: Surface = .plain
    /// Notification bodies wrap to 3 lines; timeline subtitles to 2.
    var subtitleLines = 2
    var isUnread = false
    var showsDivider = false

    var body: some View {
        HStack(spacing: PBSpace.s12) {
            leadingView
            VStack(alignment: .leading, spacing: PBSpace.s2) {
                Text(title)
                    .textStyle(.headline)
                    .foregroundStyle(PBColor.textPrimary)
                    .lineLimit(1)
                Text(subtitle)
                    .textStyle(.subheadline)
                    .foregroundStyle(PBColor.textSecondary)
                    .lineLimit(subtitleLines)
                if let detail {
                    Text(detail)
                        .textStyle(.footnote)
                        .foregroundStyle(PBColor.textTertiary)
                        .lineLimit(1)
                }
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            trailingView
            if isUnread {
                Circle()
                    .fill(PBColor.bgInverse)
                    .frame(width: 8, height: 8)
                    .accessibilityLabel("Unread")
            }
        }
        .padding(.vertical, PBSpace.s8)
        .frame(minHeight: 64)
        .overlay(alignment: .bottom) {
            if showsDivider {
                PBColor.borderSubtle
                    .frame(height: PBSize.hairline)
                    .padding(.leading, 52)
            }
        }
        .accessibilityElement(children: .combine)
    }

    @ViewBuilder
    private var leadingView: some View {
        let isOnCard = surface == .onCard
        switch leading {
        case .icon(let icon):
            PBIconView(icon, size: PBSize.iconMd)
                .foregroundStyle(PBColor.iconPrimary)
                .frame(width: PBSize.avatarMd, height: PBSize.avatarMd)
                .background(isOnCard ? PBColor.bgPrimary : PBColor.bgCard, in: .circle)
        case .avatar(let content):
            PBAvatar(content, diameter: PBSize.avatarMd, isOnCard: isOnCard)
        }
    }

    @ViewBuilder
    private var trailingView: some View {
        switch trailing {
        case .amount(let amount, let date, let isIncoming):
            VStack(alignment: .trailing, spacing: PBSpace.s2) {
                Text(amount)
                    .textStyle(.amountMedium)
                    .foregroundStyle(isIncoming ? PBColor.textPrimary : PBColor.textSecondary)
                if let date {
                    Text(date)
                        .textStyle(.footnote)
                        .foregroundStyle(PBColor.textTertiary)
                }
            }
            .lineLimit(1)
            .fixedSize()
        case .badge(let text):
            PBBadge(text, style: surface == .onCard ? .onCard : .muted)
        case .action(let label, let perform):
            PBButton(label, style: surface == .onCard ? .onCard : .secondary, size: .small, action: perform)
        case .none:
            EmptyView()
        }
    }
}

#Preview("PBActivityRow") {
    VStack(spacing: 0) {
        PBActivityRow(leading: .icon(.food), title: "Dinner at Olive Garden", subtitle: "You paid · 4 people",
                      trailing: .amount("₹2,800", date: "Today", isIncoming: true))
        PBActivityRow(leading: .icon(.bolt), title: "Electricity bill", subtitle: "Flat 302 · You owe",
                      trailing: .amount("−₹450", date: "26 Sep", isIncoming: false))
        PBActivityRow(leading: .avatar(.art(.priya)), title: "Priya paid you", subtitle: "UPI",
                      trailing: .amount("₹1,050", date: "Yesterday", isIncoming: true))
        PBActivityRow(leading: .icon(.flame), title: "Cooking gas draft created", subtitle: "Flat 302 · Needs an amount",
                      trailing: .badge("Draft"))
        PBActivityRow(leading: .icon(.food), title: "Snacks", subtitle: "₹300 · Goa Trip", detail: "Deleted by Priya on 24 Sep · 24 days left",
                      trailing: .action("Restore") {}, isUnread: true)
        VStack(spacing: 0) {
            PBActivityRow(leading: .avatar(.art(.rohan)), title: "You paid Rohan", subtitle: "UPI · Goa Trip",
                          trailing: .amount("−₹1,400", date: "Today", isIncoming: false), surface: .onCard, showsDivider: true)
            PBActivityRow(leading: .icon(.food), title: "Dinner at Olive Garden", subtitle: "You paid · 4 people",
                          trailing: .amount("₹2,800", date: "Today", isIncoming: true), surface: .onCard)
        }
        .padding(.horizontal, PBLayout.cardPadding)
        .pbCard(padding: 0)
    }
    .padding(PBLayout.screenMargin)
}
