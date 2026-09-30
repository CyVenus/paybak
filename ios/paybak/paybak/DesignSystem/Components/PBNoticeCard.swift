import SwiftUI

/// Card / Notice (Figma 129:1976): an in-flow notice on #F5F5F5 (pending confirmation, invite a
/// guest, simplified debts, a dispute, a read-only project, a Pro lock). All gray and black, never
/// red.
/// - Leading: a white 40 pt icon circle, the Headline title (with an optional Inverse "Pro" pill)
///   over the Subheadline message. Without a title the message sits level with the icon.
/// - Centered: a 56 pt icon circle, the pill, a Title/3 title and the message, all centred.
/// With one action it is a full-width large primary button; with two, a small primary and a small
/// On Card button share the width.
struct PBNoticeCard: View {
    enum Layout {
        case leading
        case centered
    }

    struct Action {
        let label: String
        let action: () -> Void

        /// A leading icon (the single large button only, e.g. Share on "Send invite").
        var icon: PBIcon?

        init(_ label: String, icon: PBIcon? = nil, action: @escaping () -> Void) {
            self.label = label
            self.icon = icon
            self.action = action
        }
    }

    let icon: PBIcon
    var title: String?
    let message: String
    var badge: String?
    var layout: Layout = .leading
    var primary: Action?
    var secondary: Action?

    var body: some View {
        VStack(spacing: layout == .leading ? PBSpace.s16 : PBSpace.s12) {
            switch layout {
            case .leading: leadingContent
            case .centered: centeredContent
            }
            if let primary {
                actions(primary)
                    .padding(.top, layout == .centered ? PBSpace.s12 : 0)
            }
        }
        .padding(layout == .leading ? PBLayout.cardPadding : PBSpace.s24)
        .frame(maxWidth: .infinity)
        .background(PBColor.bgCard, in: .rect(cornerRadius: PBRadius.card))
    }

    private var leadingContent: some View {
        HStack(alignment: .top, spacing: PBSpace.s12) {
            PBAvatar(.icon(icon), diameter: PBSize.avatarMd, isOnCard: true)
            VStack(alignment: .leading, spacing: PBSpace.s2) {
                if let title {
                    HStack(spacing: PBSpace.s8) {
                        Text(title)
                            .textStyle(.headline)
                            .foregroundStyle(PBColor.textPrimary)
                            .frame(maxWidth: .infinity, alignment: .leading)
                        if let badge {
                            PBBadge(badge, style: .inverse)
                        }
                    }
                }
                Text(message)
                    .textStyle(.subheadline)
                    .foregroundStyle(PBColor.textSecondary)
                    .fixedSize(horizontal: false, vertical: true)
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .accessibilityElement(children: .combine)
        }
    }

    private var centeredContent: some View {
        VStack(spacing: PBSpace.s12) {
            PBAvatar(.icon(icon), diameter: PBSize.avatarLg, isOnCard: true)
            if let badge {
                PBBadge(badge, style: .inverse)
            }
            VStack(spacing: PBSpace.s8) {
                if let title {
                    Text(title)
                        .textStyle(.title3)
                        .foregroundStyle(PBColor.textPrimary)
                }
                Text(message)
                    .textStyle(.subheadline)
                    .foregroundStyle(PBColor.textSecondary)
            }
            .multilineTextAlignment(.center)
            .fixedSize(horizontal: false, vertical: true)
            .accessibilityElement(children: .combine)
        }
    }

    @ViewBuilder
    private func actions(_ primary: Action) -> some View {
        if let secondary {
            HStack(spacing: PBSpace.s8) {
                PBButton(primary.label, size: .small, fillsWidth: true, action: primary.action)
                PBButton(secondary.label, style: .onCard, size: .small, fillsWidth: true, action: secondary.action)
            }
        } else {
            PBButton(primary.label, icon: primary.icon, fillsWidth: true, action: primary.action)
        }
    }
}

#Preview("PBNoticeCard") {
    ScrollView {
        VStack(spacing: PBSpace.s16) {
            PBNoticeCard(icon: .activity, title: "Pending confirmation", message: "Waiting for Meera to confirm")
            PBNoticeCard(icon: .mail, title: "Arjun R isn’t on Paybak", message: "Invite them so they see what they owe.", primary: .init("Send invite", icon: .share) {})
            PBNoticeCard(icon: .flag, title: "Priya disputed this", message: "She says the amount should be ₹2,400.", primary: .init("Edit expense") {}, secondary: .init("Resolve") {})
            PBNoticeCard(icon: .lock, title: "Insights is part of Pro", message: "See where your money goes each month.", badge: "Pro", layout: .centered, primary: .init("See Pro") {}, secondary: .init("Not now") {})
        }
        .padding(PBLayout.screenMargin)
    }
}
