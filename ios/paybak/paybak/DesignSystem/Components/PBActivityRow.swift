import SwiftUI

/// Row / Activity (Figma 13:477, components-home §10, activity §9): a timeline or recent-activity row,
/// 64 tall at least. A category icon or a person's avatar in a 40 pt circle; a title of up to 2
/// lines, a subtitle of up to 3 and an optional detail line of up to 2; then the amount and date, a
/// date alone, a status badge, or a small action. Money in is black, money out gray with "−". Plain
/// rows sit on white (#F5F5F5 circle); On Card rows sit inside a #F5F5F5 card (white circle).
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
        /// A date caption alone (notifications: "9:00 pm", "Yesterday").
        case date(String)
        /// A status pill ("Pending", "Draft"); muted (white on a card) unless a style is given.
        case badge(String, style: PBBadge.Style? = nil)
        /// An amount over a status pill (a project's parts: "₹7,500" over "Bought"). A placeholder
        /// amount (a planned part's "—") is tertiary.
        case amountBadge(String, badge: String, style: PBBadge.Style, isPlaceholder: Bool = false)
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
    /// Titles wrap to 2 lines.
    var titleLines = 2
    /// Subtitles (notification bodies, timeline details) wrap to 3 lines.
    var subtitleLines = 3
    var isUnread = false
    var showsDivider = false

    var body: some View {
        HStack(spacing: PBSpace.s12) {
            leadingView
            VStack(alignment: .leading, spacing: PBSpace.s2) {
                Text(title)
                    .textStyle(.headline)
                    .foregroundStyle(PBColor.textPrimary)
                    .lineLimit(titleLines)
                Text(subtitle)
                    .textStyle(.subheadline)
                    .foregroundStyle(PBColor.textSecondary)
                    .lineLimit(subtitleLines)
                if let detail {
                    Text(detail)
                        .textStyle(.footnote)
                        .foregroundStyle(PBColor.textTertiary)
                        .lineLimit(2)
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
        // A row with its own button keeps the button reachable; the others read as one element.
        .accessibilityElement(children: hasAction ? .contain : .combine)
    }

    private var hasAction: Bool {
        if case .action = trailing { return true }
        return false
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
        case .date(let date):
            Text(date)
                .textStyle(.footnote)
                .foregroundStyle(PBColor.textTertiary)
                .lineLimit(1)
                .fixedSize()
        case .badge(let text, let style):
            PBBadge(text, style: style ?? (surface == .onCard ? .onCard : .muted))
                .fixedSize()
        case .amountBadge(let amount, let badge, let style, let isPlaceholder):
            VStack(alignment: .trailing, spacing: PBSpace.s2) {
                Text(amount)
                    .textStyle(.amountMedium)
                    .foregroundStyle(isPlaceholder ? PBColor.textTertiary : PBColor.textPrimary)
                    .lineLimit(1)
                PBBadge(badge, style: style)
            }
            .fixedSize()
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
        PBActivityRow(leading: .icon(.calendar), title: "Payment reminder", subtitle: "You owe Kabir ₹1,400 for Goa Trip. It’s due Friday.",
                      trailing: .date("9:00 pm"), subtitleLines: 3, isUnread: true)
        PBActivityRow(leading: .avatar(.art(.rohan)), title: "Payment overdue",
                      subtitle: "Rohan owes you ₹800 for Movie tickets. It was due on 27 Sep.",
                      trailing: .badge("Overdue", style: .overdue), subtitleLines: 3)
        PBActivityRow(leading: .icon(.food), title: "Snacks", subtitle: "₹300 · Goa Trip", detail: "Deleted by Priya on 24 Sep · 24 days left",
                      trailing: .action("Restore") {}, isUnread: true)
        VStack(spacing: 0) {
            PBActivityRow(leading: .icon(.tag), title: "GPS module", subtitle: "Est. ₹6,000",
                          trailing: .amountBadge("—", badge: "Planned", style: .mutedOnCard, isPlaceholder: true), surface: .onCard, showsDivider: true)
            PBActivityRow(leading: .avatar(.art(.dev)), title: "Motors ×4", subtitle: "Dev · Est. ₹12,000",
                          trailing: .amountBadge("₹12,000", badge: "Done", style: .inverse), surface: .onCard, showsDivider: true)
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
