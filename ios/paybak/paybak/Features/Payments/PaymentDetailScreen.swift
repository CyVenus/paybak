import SwiftUI

/// The payment detail (record-lend-group §3, settle §5): payer → receiver hero, the status notice
/// (pending, not received, confirmed; gray, never red), the detail rows and Cancel payment. A
/// claim sent to you shows Confirm / Not received instead.
struct PaymentDetailScreen: View {
    let paymentId: PaymentID

    @Environment(AppRouter.self) private var router
    @Environment(LedgerStore.self) private var store
    @Environment(ProfileStore.self) private var profileStore
    @State private var showsCancel = false

    var body: some View {
        let detail = store.books.paymentDetail(paymentId, myUPI: profileStore.profile.upiID)
        Group {
            if let detail {
                content(detail)
            } else {
                Text("This payment is no longer here.")
                    .textStyle(.body)
                    .foregroundStyle(PBColor.textSecondary)
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            }
        }
        .pbPinnedHeader {
            PBPushHeader("Payment", trailing: detail?.canChange == true ? .text("Edit", action: edit) : .none,
                         testIDPrefix: "paymentRecorded", onBack: router.back)
        }
        .pbAlert(isPresented: $showsCancel, title: "Cancel this payment?",
                 message: store.ledger.payment(paymentId).map(store.books.cancelPaymentMessage),
                 cancelLabel: "Keep", actionLabel: "Cancel payment", testIDPrefix: "paymentRecorded.alert", onAction: cancel)
        .onStartScreen([.paymentCancelAlert]) { _ in showsCancel = true }
        .routeTestRoot("payment")
    }

    private func content(_ detail: PaymentDetail) -> some View {
        ScrollView {
            VStack(alignment: .leading, spacing: PBLayout.sectionGap) {
                PBAmountHero(leading: .pair(from: avatar(detail.payment.fromId), to: avatar(detail.payment.toId)),
                             title: detail.title, amount: detail.amount, meta: detail.meta)
                notice(detail.notice)
                    .accessibilityIdentifier("paymentRecorded.status")
                VStack(alignment: .leading, spacing: PBSpace.s8) {
                    VStack(spacing: 0) {
                        ForEach(detail.rows, id: \.title) { row in
                            PBSettingRow(row.title, value: row.value, trailing: row.title == "Proof" && detail.payment.proof != nil ? .chevron : .none,
                                         showsDivider: row.title != detail.rows.last?.title,
                                         action: row.title == "Proof" ? detail.payment.proof.map { name in { router.open(.photoViewer(.file(name))) } } : nil)
                        }
                    }
                    .pbCard(padding: 0)
                    if let footnote = detail.footnote {
                        Text(footnote)
                            .textStyle(.footnote)
                            .foregroundStyle(PBColor.textSecondary)
                    }
                }
                if detail.canChange {
                    PBSettingRow("Cancel payment", icon: .delete, trailing: .none, tone: .destructive, showsDivider: false) {
                        showsCancel = true
                    }
                    .pbCard(padding: 0)
                    .accessibilityIdentifier("paymentRecorded.cancel")
                }
            }
            // Room for the toast under the last row (the "Scroll spacer").
            .padding(.bottom, PBSpace.s48)
            .pbPushContent()
        }
    }

    @ViewBuilder
    private func notice(_ notice: PaymentDetail.Notice) -> some View {
        switch notice {
        case .awaitingThem(let name):
            PBNoticeCard(icon: .activity, title: "Pending confirmation", message: "Waiting for \(name) to confirm")
        case .awaitingYou(let name):
            PBNoticeCard(icon: .activity, title: "Waiting for you",
                         message: "\(name) says this payment was made. Confirm once it’s reached you.",
                         primary: .init("Confirm", action: confirm),
                         secondary: .init("Not received") { router.open(.notReceived(paymentId)) })
        case .notReceived(let name, let note):
            PBNoticeCard(icon: .flag, title: "Not received",
                         message: ["\(name) says they haven’t received it", note].compactMap(\.self).joined(separator: "\n"))
        case .confirmed(let text):
            PBNoticeCard(icon: .checkCircle, title: "Confirmed", message: text)
        case .cancelled:
            PBNoticeCard(icon: .close, title: "Cancelled", message: "This payment doesn’t count anywhere.")
        }
    }

    private func avatar(_ person: PersonID) -> PBAvatar.Content {
        person == Person.me ? profileStore.avatarContent : store.ledger.person(person)?.avatarContent ?? .icon(.profile)
    }

    private func edit() {
        router.open(.recordPayment(RecordPaymentArgs(editing: paymentId)))
    }

    private func confirm() {
        try? store.confirmPayment(paymentId)
        Haptics.success()
        router.toast("Payment confirmed")
    }

    private func cancel() {
        try? store.cancelPayment(paymentId)
        router.back()
    }
}
